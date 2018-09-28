package com.jjkeller.kmbapi.eobrengine.eobrreader

import android.content.Context
import android.os.Bundle
import com.jjkeller.kmbapi.common.LogCat
import com.jjkeller.kmbapi.common.VersionUtility
import com.jjkeller.kmbapi.configuration.FirmwareUpdate
import com.jjkeller.kmbapi.eobrengine.*
import com.jjkeller.kmbapi.eobrengine.Enums.FirmwareUpgradeTypeEnum
import com.jjkeller.kmbapi.eobrengine.IEobrEngine.EOBR_MAX_YEAR
import com.jjkeller.kmbapi.eobrengine.IEobrEngine.EOBR_MIN_YEAR
import com.jjkeller.kmbapi.eobrengine.ReceivedPacketHelper.VerifyRxPacketResponseByte
import com.jjkeller.kmbapi.eobrengine.eobrdatatypes.ConnectedDevice
import com.jjkeller.kmbapi.eobrengine.eobrdatatypes.EobrBytePacket
import com.jjkeller.kmbapi.eobrengine.eobrdatatypes.EobrJbusDiagDataPacket
import com.jjkeller.kmbapi.eobrengine.eobrdatatypes.EobrIntegerPacket
import com.jjkeller.kmbapi.eobrengine.eobrdatatypes.EobrShortPacket
import com.jjkeller.kmbapi.eobrengine.eobrdatatypes.EobrStringPacket
import com.jjkeller.kmbapi.eobrengine.eobrdatatypes.Eobr_Comm_Response_Packet
import com.jjkeller.kmbapi.eobrengine.eobrdatatypes.Eobr_Driver_Event_Packet
import com.jjkeller.kmbapi.eobrengine.eobrdatatypes.Eobr_Data_Packet_GenII
import com.jjkeller.kmbapi.eobrengine.eobrdatatypes.Eobr_FW_Block_Packet
import com.jjkeller.kmbapi.eobrengine.eobrdatatypes.Eobr_Packet
import com.jjkeller.kmbapi.eobrengine.eobrreader.exceptions.EobrException
import com.jjkeller.kmbapi.firmwareupgrade.FirmwareUpdateBroadcaster
import com.jjkeller.kmbapi.configuration.GlobalState
import com.jjkeller.kmbapi.kmbeobr.Constants
import com.jjkeller.kmbapi.kmbeobr.DriveData
import com.jjkeller.kmbapi.kmbeobr.DriveDataTypeEnum
import com.jjkeller.kmbapi.kmbeobr.DTCInformation
import com.jjkeller.kmbapi.kmbeobr.Enums.EobrReturnCode
import com.jjkeller.kmbapi.kmbeobr.EobrReferenceTimestamps
import com.jjkeller.kmbapi.kmbeobr.EobrResponse
import com.jjkeller.kmbapi.kmbeobr.EventRecord
import com.jjkeller.kmbapi.kmbeobr.EventTypeEnum
import com.jjkeller.kmbapi.kmbeobr.FirmwareUpgradeRequestResult
import com.jjkeller.kmbapi.kmbeobr.FirmwareUpgradeStatusResult
import com.jjkeller.kmbapi.kmbeobr.HistogramData
import com.jjkeller.kmbapi.kmbeobr.HistogramTypeEnum
import com.jjkeller.kmbapi.kmbeobr.JbusDiagnosticData
import com.jjkeller.kmbapi.kmbeobr.StatusBuffer
import com.jjkeller.kmbapi.kmbeobr.StatusRecord
import com.jjkeller.kmbapi.kmbeobr.StatusRecordMotionOptionEnum
import com.jjkeller.kmbapi.kmbeobr.StatusRecordQueryMethodEnum
import com.jjkeller.kmbapi.kmbeobr.TripReport
import com.jjkeller.kmbapi.R
import com.jjkeller.kmbapi.realtime.MalfunctionManager
import java.io.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*
import java.util.regex.Pattern
import kotlin.experimental.and

abstract class GenIIBase : IEobrEngine, ISendReceiveBulkData {

    /**
     * Verifies device connection, or attempts to reconnect
     * @return true if the device is connected, false otherwise
     */
    abstract fun verifyDeviceConnection(): Boolean

    protected var socketVerifier: EobrCommunications.IVerifySocketConnection = object : EobrCommunications.IVerifySocketConnection {
        override fun verify(): Boolean {
            return verifyDeviceConnection()
        }
    }

    //Call lazy to avoid complaint about using "this" in abstract constructor
    val threadMgr by lazy { CommThreadManager(this) }
    private var isIgnitionTripReportSupported = false
    private var isEldMandateMode = false

    abstract fun hasActiveDevice(): Boolean
    abstract fun getActiveDevice(): ConnectedDevice?

    protected var handshakeCrc: Short = 0
    fun hasHandshakeCrc(): Boolean = handshakeCrc != 0.toShort()
    fun clearHandshakeCrc() = { handshakeCrc = 0.toShort() }

    object HistogramType {
        const val TAB_HIST_VSS = 0x01
        const val TAB_HIST_RPM = 0x02
        const val TAB_HIST_LOAD = 0x03
        const val TAB_HIST_GPS_DOP = 0x04
        const val TAB_HIST_GPS_SECS = 0x05
    }

    protected fun getActiveDeviceCrc(): Short {
        var result: Short = 0
        if (hasActiveDevice()) {
            result = getActiveDevice()!!.crc
        } else if (hasHandshakeCrc()) {
            result = handshakeCrc
        }
        return result
    }

    private fun buildEobrPacket(cmdId: Int, packetData: ByteArray = ByteArray(0)): Eobr_Packet {

        return Eobr_Packet().apply {
            cmd = cmdId.toByte()
            crc = getActiveDeviceCrc()
            len = packetData.size.toByte()
            data = packetData
        }
    }

    private fun buildEobrGenIIPacket(cmdId: Int, queryMethodType: Int? = null): Eobr_Data_Packet_GenII {
        var packetQueryType: Byte? = null
        if (queryMethodType != null) {
            packetQueryType = if (queryMethodType == StatusRecordQueryMethodEnum.RECORDID)
                RECORD_ID_TYPE
            else
                TIMESTAMP_TYPE
        }

        return Eobr_Data_Packet_GenII().apply {
            cmd = cmdId.toByte()
            crc = getActiveDeviceCrc()
            if(packetQueryType != null)
                method = packetQueryType
        }
    }

    protected fun initializeEobr(): Int {
        var errorCode: Int
        val cmdId = EUCMDType.EUCMD_INITIALIZE

        val commResponsePacket = Eobr_Comm_Response_Packet()
        errorCode = EobrCommunications.validateNoData(threadMgr, cmdId,
                getActiveDeviceCrc(), VerifyRxPacketResponseByte.EobrIntegerPacket,
                commResponsePacket, socketVerifier)

        if (errorCode != EobrReturnCode.S_SUCCESS) {
            return errorCode
        }

        val response = commResponsePacket.response
        val status = EobrIntegerPacket(response).integerVal

        //current FW versions will respond to this command
        //with a bitmask representing the commands it supports
        if (response[1].toInt() == 36)
            System.arraycopy(response, 6, currentEucmdBitmask, 0, 32)
        else
            currentEucmdBitmask = EUCMD_BITMASK_DEFAULT

        if (status != 0)
            errorCode = EobrReturnCode.S_DEV_INTERNAL_ERROR

        isIgnitionTripReportSupported = isIgnitionTripReportSupported()

        return errorCode
    }

    private fun isFirmwareVersionSupported(minSupportedMajor: Int, minSupportedMinor: Int, minSupportedPatch: Int): Boolean {
        var result = false
        val minVersion = VersionUtility.getVersionString(minSupportedMajor, minSupportedMinor, minSupportedPatch)

        val firmwareVersion = this.GetEOBRDllRevisions()
        if (firmwareVersion.getInt(Constants.RETURNCODE) != EobrReturnCode.S_SUCCESS) {
            return result
        }

        val installedFirmwareVersion = firmwareVersion.getString(Constants.MAINFIRMWAREREVISION)
        if (installedFirmwareVersion != null) {
            result = VersionUtility.compareVersions(installedFirmwareVersion, minVersion) >= 0
        }

        return result
    }

    //TODO can this be cleaned up to not have to use double-bang?
    override fun ClearActiveDeviceCrc() {
        if (hasActiveDevice())
            getActiveDevice()!!.crc = 0
    }

    /**
     * Clears status buffer including odometer and odometer offset
     * @return error code
     * @throws EobrException
     */
    @Throws(EobrException::class)
    private fun clearStatusBufferData() {
        val cmdId = EUCMDType.EUCMD_RESET_STATUS_BUFFER
        val eobrPacket = buildEobrPacket(cmdId)

        val commResponsePacket = Eobr_Comm_Response_Packet()
        val errorCode = EobrCommunications.validateWithData(threadMgr, eobrPacket,
                cmdId, getActiveDeviceCrc(), VerifyRxPacketResponseByte.EobrBytePacket,
                commResponsePacket, false, socketVerifier)

        // HACK: EOBR returns 255 even though Status Buffer reset occurs
        if (errorCode == EobrReturnCode.S_GENERAL_ERROR)
            return

        val eobrException = EobrException.getEobrExceptionFromEobrReturnCodeValue(errorCode)
        if (eobrException != null)
            throw eobrException
    }

    @Throws(EobrException::class)
    private fun clearHistogramData(histType: Int) {

        val cmdId = EUCMDType.EUCMD_CLEAR_HISTOGRAM
        val dataSize = 1
        val packetData = ByteArray(dataSize)
        packetData[0] = histType.toByte()
        val eobrPacket = buildEobrPacket(cmdId, packetData)

        val commResponsePacket = Eobr_Comm_Response_Packet()
        val errorCode = EobrCommunications.validateWithData(threadMgr, eobrPacket,
                cmdId, getActiveDeviceCrc(), VerifyRxPacketResponseByte.EobrBytePacket,
                commResponsePacket, false, socketVerifier)

        val eobrException = EobrException.getEobrExceptionFromEobrReturnCodeValue(errorCode)
        if (eobrException != null)
            throw eobrException
    }

    override fun SetupActiveDevice(deviceName: String?, btAddress: String?, eobrGen: Int, crc: Short) {

        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun GetActiveDeviceAddress(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun OpenDevice(deviceName: String?): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun CloseDevice(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun PingEobrDevice(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun GetEobrSerialNumber(): Bundle {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun GetClockUTC(): Bundle {
        val returnCode: Int
        val cmdId = EUCMDType.EUCMD_GET_CLOCK_UTC
        val answer = Bundle()
        var eobrClock: Date? = null
        val eobrPacket = buildEobrPacket(cmdId)

        val commResponsePacket = Eobr_Comm_Response_Packet()
        returnCode = EobrCommunications.validateWithData(threadMgr, eobrPacket, cmdId,
                getActiveDeviceCrc(), VerifyRxPacketResponseByte.EobrStringPacket,
                commResponsePacket, false, socketVerifier)

        if (returnCode == EobrReturnCode.S_SUCCESS) {
            // convert UNIX Epoch seconds into milliseconds, then date
            val rawEobrClock = LittleEndianHelper.getInt(
                    commResponsePacket.response, 2, 4)

            eobrClock = Date((rawEobrClock * 1000).toLong())
        }

        answer.putInt(Constants.RETURNCODE, returnCode)
        if (eobrClock != null)
            answer.putLong(Constants.RETURNVALUE, eobrClock.time)

        return answer
    }

    override fun GetGPSTimestamp(): EobrResponse<Date> {
        val returnCode: Int
        val cmdId = EUCMDType.EUCMD_GET_CLOCK_GPS
        var gpsTime: Date? = null
        val eobrPacket = buildEobrPacket(cmdId)

        val commResponsePacket = Eobr_Comm_Response_Packet()
        returnCode = EobrCommunications.validateWithData(threadMgr, eobrPacket, cmdId,
                getActiveDeviceCrc(), VerifyRxPacketResponseByte.EobrStringPacket,
                commResponsePacket, false, socketVerifier)

        val response = EobrResponse<Date>(returnCode)

        if (returnCode == EobrReturnCode.S_SUCCESS) {
            // convert UNIX Epoch seconds into milliseconds, then date
            val returnValue = LittleEndianHelper.getInt(
                    commResponsePacket.response, 2, 4)
            if (returnValue != 0)
                gpsTime = Date((returnValue * 1000).toLong())
        }

        if (gpsTime != null)
            response.data = gpsTime

        return response
    }

    override fun SetClockUTC(newClock: Date?): Int {
        val cmdId = EUCMDType.EUCMD_SET_CLOCK_UTC

        val cal = Calendar.getInstance().apply {
            timeZone = TimeZone.getTimeZone("GMT")
            time = newClock
        }

        if (cal.get(Calendar.YEAR) < EOBR_MIN_YEAR || cal.get(Calendar.YEAR) > EOBR_MAX_YEAR)
            return EobrReturnCode.S_INVALID_DATE_TIME

        val dateSeconds = Integer.valueOf((cal.timeInMillis / 1000).toString())
        val packetData = intToByteArray(dateSeconds)
        val eobrPacket = buildEobrPacket(cmdId, packetData)

        val commResponsePacket = Eobr_Comm_Response_Packet()
        return EobrCommunications.validateWithData(threadMgr, eobrPacket,
                cmdId, getActiveDeviceCrc(), VerifyRxPacketResponseByte.EobrNullPacket,
                commResponsePacket, false, socketVerifier)
    }

    override fun GetCompanyPasskey(): Bundle {
        val answer = Bundle()
        answer.putInt(Constants.RETURNCODE, EobrReturnCode.S_FUNC_NOT_IMPLEMENTED)

        return answer
    }

    override fun SetCompanyPasskey(passkey: String?): Int {
        //TODO handle this from BTGenII
        return EobrReturnCode.S_FUNC_NOT_IMPLEMENTED
    }

    override fun GetCustomParameter(customParameterIndex: Int): Bundle {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun SetCustomParameter(customParameter: Int, customParameterIndex: Int): Int {
        var errorCode: Int
        val cmdId = EUCMDType.EUCMD_SET_CUSTOM_PARAMETER

        // Arrange the byte into order before send out
        val customParam = intToByteArray(customParameter)
        // 4 bytes for the custom parameter, 1 byte for the index.  EOBR allocated 64 bytes of space
        val packetData = byteArrayOf(customParameterIndex.toByte(), customParam[0], customParam[1], customParam[2], customParam[3])
        val eobrPacket = buildEobrPacket(cmdId, packetData)

        val commResponsePacket = Eobr_Comm_Response_Packet()
        errorCode = EobrCommunications.validateWithData(threadMgr, eobrPacket, cmdId,
                getActiveDeviceCrc(), VerifyRxPacketResponseByte.EobrNullPacket,
                commResponsePacket, false, socketVerifier)

        if (errorCode == 1) {
            errorCode = EobrReturnCode.S_GENERAL_ERROR
        }

        return errorCode
    }

    override fun GetEobrOdometerOffset(): Bundle {
        var errorCode: Int
        val cmdId = EUCMDType.EUCMD_GET_ODOMETER_OFFSET
        val answer = Bundle()
        var offset = 0f

        val commResponsePacket = Eobr_Comm_Response_Packet()
        errorCode = EobrCommunications.validateNoData(threadMgr, cmdId, getActiveDeviceCrc(),
                VerifyRxPacketResponseByte.EobrIntegerPacket, commResponsePacket, socketVerifier)

        if (errorCode == EobrReturnCode.S_SUCCESS) {
            val offsetPacket = EobrIntegerPacket(commResponsePacket.response)

            // get and convert to mi.
            offset = Math.round(offsetPacket.integerVal / Constants.MILES_TO_METERS * 10).toFloat() / 10

            // Byte pattern sent by EOBR to indicate failure
            if (offsetPacket.size.toInt() == 0xFF) {
                errorCode = EobrReturnCode.S_DEV_INTERNAL_ERROR
            }
        }

        answer.putInt(Constants.RETURNCODE, errorCode)
        answer.putFloat(Constants.RETURNVALUE, offset)

        return answer
    }

    override fun SetEobrOdometerOffset(offset: Float): Int {
        val errorCode: Int
        val cmdId = EUCMDType.EUCMD_SET_ODOMETER_OFFSET
        val packetData = intToByteArray((offset * Constants.MILES_TO_METERS).toInt())
        val eobrPacket = buildEobrPacket(cmdId, packetData)

        val commResponsePacket = Eobr_Comm_Response_Packet()
        errorCode = EobrCommunications.validateWithData(threadMgr, eobrPacket, cmdId,
                getActiveDeviceCrc(), VerifyRxPacketResponseByte.EobrNullPacket,
                commResponsePacket, false, socketVerifier)

        return errorCode
    }

    override fun GetUnitId(): Bundle {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun SetUnitId(unitId: String?): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun GetOdometerCalibration(): Bundle {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun SetOdometerCalibration(offset: Float, multiplier: Float): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun GetEobrData(statusRec: StatusRecord?, queryMethod: StatusRecordQueryMethodEnum?, recordId: Int, timeCode: Date?, motionOption: StatusRecordMotionOptionEnum?, resetReferenceTimestampToCurrent: Boolean): Int {
        //TODO pull from BTGenII
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun GetEngineOffCommsTimeout(): Bundle {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun SetEngineOffCommsTimeout(timeoutInMinutes: Int): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun ReadDataCollectionRate(): Bundle {
        var errorCode: Int
        val cmdId = EUCMDType.EUCMD_GET_DATA_RATE
        val answer = Bundle()
        var dataRate = 0

        val commResponsePacket = Eobr_Comm_Response_Packet()
        errorCode = EobrCommunications.validateNoData(threadMgr, cmdId,
                getActiveDeviceCrc(),
                VerifyRxPacketResponseByte.EobrIntegerPacket, commResponsePacket, socketVerifier)

        if (errorCode == EobrReturnCode.S_SUCCESS) {
            val eobrDataCollectionRatePacket = EobrIntegerPacket(commResponsePacket.response)
            dataRate = eobrDataCollectionRatePacket.integerVal

            // Byte pattern sent by EOBR to indicate failure
            // Error happen, size is 1
            if (eobrDataCollectionRatePacket.size.toInt() == 1 && dataRate == 1)
                errorCode = EobrReturnCode.S_DEV_INTERNAL_ERROR

            dataRate /= 1000
        }

        answer.putInt(Constants.RETURNCODE, errorCode)
        answer.putInt(Constants.RETURNVALUE, dataRate)

        return answer
    }

    override fun ChangeDataCollectionRate(newDataRate: Int): Int {
        val errorCode: Int
        val cmdId = EUCMDType.EUCMD_CHANGE_DATA_RATE

        // Gen II stores in milliseconds
        val adjustedDataRate = newDataRate * 1000
        val packetData = intToByteArray(adjustedDataRate)
        val eobrPacket = buildEobrPacket(cmdId, packetData)

        val commResponsePacket = Eobr_Comm_Response_Packet()
        errorCode = EobrCommunications.validateWithData(threadMgr, eobrPacket, cmdId,
                getActiveDeviceCrc(), VerifyRxPacketResponseByte.EobrNullPacket,
                commResponsePacket, false, socketVerifier)

        return errorCode
    }

    override fun GetReferenceTimestamp(): Bundle {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun GetDistHours(timecode: Long): Bundle {
        //TODO pull from BTGenII
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun GetActiveBusType(): Bundle {
        var errorCode: Int
        val cmdId = EUCMDType.EUCMD_GET_ACTIVE_BUS
        val answer = Bundle()
        var currentBusType = 0

        val commResponsePacket = Eobr_Comm_Response_Packet()
        errorCode = EobrCommunications.validateNoData(threadMgr, cmdId,
                getActiveDeviceCrc(), VerifyRxPacketResponseByte.EobrStringPacket,
                commResponsePacket, socketVerifier)

        if (errorCode == EobrReturnCode.S_SUCCESS) {
            val eobrBusTypePacket = EobrStringPacket.build(commResponsePacket.response)

            // NOTE:  response contains 2 bytes - first byte is Target Bus Type (what we previously set)
            // second byte is the current active bus type, that's what we are interested in.
            currentBusType = eobrBusTypePacket.stringVal[1].toInt()

            // Byte pattern sent by EOBR to indicate failure
            // Error happen, size is 1
            if (eobrBusTypePacket.size.toInt() == 0xFF && currentBusType == 1)
                errorCode = EobrReturnCode.S_DEV_INTERNAL_ERROR
        }

        answer.putInt(Constants.RETURNCODE, errorCode)
        answer.putInt(Constants.RETURNVALUE, currentBusType)

        return answer
    }

    override fun GetVin(): Bundle {
        val cmdId = EUCMDType.EUCMD_GET_DEV_INFO_ON_JBUS
        val answer = Bundle()
        var devInfo = ""
        var vin = ""

        //Leaving make and model in the code for easy future reference
        //String make = "";
        //String model = "";
        val eobrPacket = buildEobrPacket(cmdId, ByteArray(1))

        val commResponsePacket = Eobr_Comm_Response_Packet()
        var retVal = EobrCommunications.validateWithData(threadMgr, eobrPacket, cmdId,
                getActiveDeviceCrc(), VerifyRxPacketResponseByte.EobrStringPacket,
                commResponsePacket, false, socketVerifier)

        if (retVal == EobrReturnCode.S_SUCCESS) {
            val eobrStrPacket = EobrStringPacket.build(commResponsePacket.response)

            if (eobrStrPacket == null) {
                retVal = EobrReturnCode.S_GENERAL_ERROR
            } else {
                try {
                    devInfo = String(eobrStrPacket.stringVal, Charsets.US_ASCII)
                } catch (e: UnsupportedEncodingException) {
                    e.printStackTrace()
                }

                if (devInfo.isNotEmpty()) {
                    //Leaving make and model in the code for easy future reference
                    //make = devInfo.substring(1, 6).trim();
                    //model = devInfo.substring(6, 31).trim();
                    vin = devInfo.substring(31).trim()
                    if (vin.length != 17) {
                        vin = ""
                    }
                }
            }
        }

        answer.putInt(Constants.RETURNCODE, retVal)
        answer.putString(Constants.RETURNVALUE, vin)

        return answer
    }

    override fun ChangeActiveBusType(newBusType: Int): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun GetEOBRDllRevisions(): Bundle {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun SetDebugFlags(debugFlags: Int): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun SendConsoleCommandToDevice(command: String?): Bundle {
        return performSendConsoleCommandToDevice(command, true)
    }

    override fun SendConsoleCommandToDeviceWithNoRetry(command: String?): Bundle {
        return performSendConsoleCommandToDevice(command, false)
    }

    private fun performSendConsoleCommandToDevice(command: String?, retryIfNotSuccessful: Boolean): Bundle {
        val answer = Bundle()
        var errorCode: Int
        val cmdId = EUCMDType.EUCMD_CONSOLE_COMMAND
        val consoleLogBuilder = StringBuilder()
        val data = AndroidBTBase.stringToByteArray(command)
        val eobrPacket = buildEobrPacket(cmdId, data)

        val commResponsePacket = Eobr_Comm_Response_Packet()
        if (retryIfNotSuccessful)
            errorCode = EobrCommunications.validateWithData(threadMgr, eobrPacket,
                    cmdId, getActiveDeviceCrc(), VerifyRxPacketResponseByte.EobrStringPacket,
                    commResponsePacket, false, socketVerifier)
        else
            errorCode = EobrCommunications.validateWithData(threadMgr, eobrPacket,
                    cmdId, getActiveDeviceCrc(), VerifyRxPacketResponseByte.EobrStringPacket,
                    commResponsePacket, false, socketVerifier, 1)

        if (errorCode == EobrReturnCode.S_SUCCESS) {
            val eobrStrPacket = EobrStringPacket.build(commResponsePacket.response)
            if (eobrStrPacket == null) {
                errorCode = EobrReturnCode.S_GENERAL_ERROR
            } else {
                var nextRecordId: Int
                val finalRecordId = 0xFFFFFFFF.toInt()

                nextRecordId = LittleEndianHelper.getInt(eobrStrPacket.stringVal, 0, 4)

                if (eobrStrPacket.size > 4) {
                    for (i in 4 until eobrStrPacket.size)
                        consoleLogBuilder.append(eobrStrPacket.stringVal[i].toChar())
                }

                // Keep getting the console log 250 bytes at a time until we hit the end
                while (nextRecordId != finalRecordId) {
                    // Get the next segment of the console log
                    val bundle = getConsoleLog(nextRecordId)
                    errorCode = bundle.getInt(Constants.RETURNCODE)
                    if (errorCode == EobrReturnCode.S_SUCCESS) {
                        // Keep track of what the next segment is
                        nextRecordId = bundle.getInt(GenIIBase.NEXT_CONSOLE_LOG_RECORD_ID)

                        if (nextRecordId != finalRecordId)
                        // Save this segment
                            consoleLogBuilder.append(bundle.getString(Constants.RETURNVALUE))
                    } else {
                        break
                    }
                }
            }
        }

        answer.putInt(Constants.RETURNCODE, errorCode)
        answer.putString(Constants.RETURNVALUE, consoleLogBuilder.toString())

        return answer
    }

    override fun SetSelfTest(): Boolean {
        val cmdId = EUCMDType.EUCMD_SET_SELF_TEST
        val errorCode: Int

        val commResponsePacket = Eobr_Comm_Response_Packet()
        errorCode = EobrCommunications.validateNoData(threadMgr, cmdId,
                getActiveDeviceCrc(),
                VerifyRxPacketResponseByte.EobrNullPacket, commResponsePacket, socketVerifier)

        return errorCode == EobrReturnCode.S_SUCCESS
    }

    override fun GetSelfTest(): Bundle {
        val cmdId = EUCMDType.EUCMD_GET_SELF_TEST
        val errorCode: Int
        var testResult = 1
        val answer = Bundle()

        val commResponsePacket = Eobr_Comm_Response_Packet()
        errorCode = EobrCommunications.validateNoData(threadMgr, cmdId,
                getActiveDeviceCrc(),
                VerifyRxPacketResponseByte.EobrShortPacket, commResponsePacket, socketVerifier)

        if (errorCode == EobrReturnCode.S_SUCCESS) {
            val selfTestPacket = EobrShortPacket(commResponsePacket.response)
            testResult = selfTestPacket.shortVal.toInt()
        }

        answer.putInt(Constants.RETURNCODE, errorCode)
        answer.putInt(Constants.RETURNVALUE, testResult)

        return answer
    }

    override fun ClearAllRecordData(clearFlags: Int): Int {
        val errorCode: Int
        val cmdId = EUCMDType.EUCMD_CLEAR_RECORD_DATA
        val eobrPacket = buildEobrPacket(cmdId)

        val commResponsePacket = Eobr_Comm_Response_Packet()
        errorCode = EobrCommunications.validateWithData(threadMgr, eobrPacket, cmdId,
                getActiveDeviceCrc(), VerifyRxPacketResponseByte.EobrNullPacket,
                commResponsePacket, false, socketVerifier)

        return errorCode
    }

    override fun DownloadFirmwareUpdate(firmwareUpdateFile: InputStream, firmwareUpgradeType: Enums.FirmwareUpgradeTypeEnum?, broadcaster: FirmwareUpdateBroadcaster?, firmwareUpdateConfig: FirmwareUpdate?): Int {
        // Only allow application firmware upgrades
        if (firmwareUpgradeType != FirmwareUpgradeTypeEnum.APP)
            return EobrReturnCode.S_FUNC_NOT_IMPLEMENTED

        var errorCode: Int

        val upgradeTypeValue = FW_APPLICATION

        errorCode = setFirmwareUpgradeMode(FW_UPGRADE_BEGIN, upgradeTypeValue)
        if (errorCode == EobrReturnCode.S_SUCCESS) {
            try {
                eobrFirmwareBlockCodeSize = EOBR_FW_BLOCK_CODE_SIZE_SMALL

                // determine whether the large packet sizes will work
                if (firmwareUpdateConfig != null && firmwareUpdateConfig.supportsLargeBlockDownload)
                    eobrFirmwareBlockCodeSize = EOBR_FW_BLOCK_CODE_SIZE_LARGE

                eobrFirmwarePayloadSize = eobrFirmwareBlockCodeSize + EOBR_FW_BLOCK_CODE_ADDRESS_SIZE
                eobrFirmwarePacketSize = eobrFirmwarePayloadSize + 5

                errorCode = sendFirmwareImageToDevice(firmwareUpdateFile, upgradeTypeValue, broadcaster)
            } catch (e: Exception) {
                LogCat.getInstance().e("firmwareUpdate", "Error sending firmware to the device..", e)
            }

            if (errorCode == EobrReturnCode.S_SUCCESS) {
                // End the firmware upgrade
                errorCode = setFirmwareUpgradeMode(FW_UPGRADE_END, upgradeTypeValue)
            }

            if (errorCode == EobrReturnCode.S_SUCCESS) {
                // Reset the EOBR
                errorCode = setFirmwareUpgradeMode(FW_UPGRADE_RESET, upgradeTypeValue)
                try {
                    Thread.sleep(15000)
                } catch (e: InterruptedException) {
                    LogCat.getInstance().e("UnhandledCatch",
                            "${e.message}: ${LogCat.getInstance().getStackTraceString(e)}")
                }

            }
        }

        return errorCode
    }

    /**
     * Sets the firmware upgrade mode for the given upgrade type.
     * @param upgradeMode The mode to set it to. Should be FW_UPGRADE_BEGIN, FW_UPGRADE_END, or FW_UPGRADE_RESET.
     * @param upgradeType The upgrade type for which to set the mode. Should be FW_APPLICATION or FW_BOOTLOADER.
     * @return The return code returned from the EOBR
     */
    private fun setFirmwareUpgradeMode(upgradeMode: Int, upgradeType: Int): Int {
        val errorCode: Int
        val cmdId = EUCMDType.EUCMD_START_FW_UPGRADE
        val packetData = byteArrayOf(upgradeMode.toByte(), upgradeType.toByte())
        val eobrPacket = buildEobrPacket(cmdId, packetData)

        val commResponsePacket = Eobr_Comm_Response_Packet()
        errorCode = EobrCommunications.validateWithData(threadMgr, eobrPacket, cmdId,
                getActiveDeviceCrc(), VerifyRxPacketResponseByte.EobrNullPacket,
                commResponsePacket, false, socketVerifier)

        return errorCode
    }

    /**
     * Sends an open input stream of a new firmware image to the device.
     * @param firmwareImageStream The open input stream of the new firmware image. The stream will be left open.
     * @param upgradeType The upgrade type that should be updated. Should be FW_APPLICATION or FW_BOOTLOADER.
     * @param broadcaster An optional listener that will get progress updates as the firmware gets updated
     * @return The return code from the EOBR
     */
    private fun sendFirmwareImageToDevice(firmwareImageStream: InputStream, upgradeType: Int, broadcaster: FirmwareUpdateBroadcaster?): Int {
        val cmdId = EUCMDType.EUCMD_FW_IMAGE_BLOCK
        var errorCode = EobrReturnCode.S_SUCCESS

        val eobrPacket = Eobr_FW_Block_Packet()
        eobrPacket.cmd = cmdId.toByte()
        eobrPacket.crc = getActiveDeviceCrc()

        eobrPacket.len = (eobrFirmwareBlockCodeSize + EOBR_FW_BLOCK_CODE_ADDRESS_SIZE + EOBR_FW_BLOCK_CODE_TYPE_SIZE).toByte()
        eobrPacket.fwType = upgradeType.toByte()

        try {
            // Get all the bytes for the new firmware image
            val firmwareImageBytes = readEntireInputStream(firmwareImageStream)

            // Send the entire image in blocks
            var eobrAddress = 0x0 // Start at address 0x0
            var imagePosition = 0
            while (imagePosition < firmwareImageBytes!!.size) {
                val buffer = ByteArray(eobrFirmwareBlockCodeSize + EOBR_FW_BLOCK_CODE_ADDRESS_SIZE)
                val eobrAddressBytes = intToByteArray(eobrAddress)
                System.arraycopy(eobrAddressBytes, 0, buffer, 0, 4)
                val imageByteCountToCopy = Math.min(eobrFirmwareBlockCodeSize, firmwareImageBytes.size - imagePosition)
                System.arraycopy(firmwareImageBytes, imagePosition, buffer, EOBR_FW_BLOCK_CODE_ADDRESS_SIZE, imageByteCountToCopy)

                eobrPacket.data = buffer

                val responsePacket = Eobr_Comm_Response_Packet()
                EobrCommunications.sendAndConfirmFirmwarePacket(threadMgr, getActiveDeviceCrc(),
                        eobrPacket, responsePacket, socketVerifier)

                errorCode = responsePacket.returnCode
                if (errorCode != EobrReturnCode.S_SUCCESS) {
                    broadcaster!!.onFirmwareUpdateFinished(false)
                    break
                }

                broadcaster?.onDownloadFirmwareProgress((imagePosition.toDouble() / firmwareImageBytes.size.toDouble() * 100.0).toInt())
                eobrAddress += eobrFirmwareBlockCodeSize
                imagePosition += eobrFirmwareBlockCodeSize
            }

            broadcaster?.onDownloadFirmwareProgress(100)
        } catch (e: Exception) {
            LogCat.getInstance().e("FirmwareUpdate", "Error updating firmware", e)
            errorCode = EobrReturnCode.S_DEV_INTERNAL_ERROR
        }

        return errorCode
    }

    @Throws(IOException::class)
    private fun readEntireInputStream(inputStream: InputStream): ByteArray? {
        val firmwareImageBytes: ByteArray?
        val bufferedInputStream: BufferedInputStream?
        var firmwareImageByteStream: ByteArrayOutputStream? = null
        try {
            bufferedInputStream = BufferedInputStream(inputStream)
            firmwareImageByteStream = ByteArrayOutputStream()

            var bytesRead: Int
            val buffer = ByteArray(16384)

            while(true) {
                bytesRead = bufferedInputStream.read(buffer, 0, buffer.size)
                if (bytesRead <= 0) break
                firmwareImageByteStream.write(buffer, 0, bytesRead)
            }

            firmwareImageByteStream.flush()
            firmwareImageBytes = firmwareImageByteStream.toByteArray()
        } finally {
            firmwareImageByteStream?.close()
        }

        return firmwareImageBytes
    }

    override fun GetThresholdValues(thresholdType: Int): Bundle {
        val needsPreMandateDataSize = isDriveStartSpeedUnsupported()
        val errorCode: Int
        val cmdId = EUCMDType.EUCMD_GET_THRESHOLDS
        val answer = Bundle()
        var rpmThreshold = 0
        var speedThreshold = 0f
        var hardBrakeThreshold = 0f
        var driveStartDistanceThreshold = 0f
        var driveStopTimeThreshold = 0
        var eventBlankingThreshold = 0
        var driverIdCRC = 0
        val dataSize = 4
        var driveStartSpeed = 0f

        val packetData = ByteArray(dataSize)

        // add thresholdType value to packetdata
        val value = intToByteArray(thresholdType)
        packetData[0] = value[0]
        packetData[1] = value[1]
        packetData[2] = value[2]
        packetData[3] = value[3]

        val eobrPacket = buildEobrPacket(cmdId, packetData)

        val commResponsePacket = Eobr_Comm_Response_Packet()
        errorCode = EobrCommunications.validateWithData(threadMgr, eobrPacket, cmdId,
                getActiveDeviceCrc(), VerifyRxPacketResponseByte.EobrStringPacket,
                commResponsePacket, false, socketVerifier)

        if (errorCode == EobrReturnCode.S_SUCCESS) {
            val eobrResponsePacket = EobrStringPacket.build(commResponsePacket.response)
            val eobrResponse = eobrResponsePacket.stringVal
            fun convertToMph(kmTimes100: Short): Float {
                return Math.round(kmTimes100.toFloat() / 100f / Constants.MILES_TO_KILOMETERS * 10).toFloat() / 10
            }
            // Threshold data is sent in 10 bytes - 2 bytes for each threshold value
            // Get RPM Threshold

            val rawRpmThreshold = LittleEndianHelper.getShort(eobrResponse, 0, 2)
            rpmThreshold = (rawRpmThreshold + 0.5).toInt()
            // Get Speedometer Threshold - stored as KM * 100 in TAB

            val rawSpeedThreshold = LittleEndianHelper.getShort(eobrResponse, 2, 2)
            speedThreshold = convertToMph(rawSpeedThreshold)
            // Get Hardbrake Threshold - stored as KM * 100 in TAB
            val rawHardBrakeThreshold = LittleEndianHelper.getShort(eobrResponse, 4, 2)
            hardBrakeThreshold = convertToMph(rawHardBrakeThreshold)
            // Get DriveStartDistance Threshold - stored as KM * 100 in TAB
            val rawDriveStartDistance = LittleEndianHelper.getShort(eobrResponse, 6, 2)
            driveStartDistanceThreshold = convertToMph(rawDriveStartDistance)
            // Get DriveStopTime Threshold - convert seconds to minutes
            val rawDriveStopTime = LittleEndianHelper.getShort(eobrResponse, 8, 2)
            driveStopTimeThreshold = (rawDriveStopTime / 60 + 0.5).toInt()
            // Get RPM Threshold
            val rawEventBlankingThreshold = LittleEndianHelper.getShort(eobrResponse, 10, 2)
            eventBlankingThreshold = (rawEventBlankingThreshold + 0.5).toInt()
            // Get DriverIdCRC
            driverIdCRC = LittleEndianHelper.getInt(eobrResponse, 12, 4)
            // Get DriveStartSpeed
            if (!needsPreMandateDataSize) {
                val rawDriveStartSpeed = LittleEndianHelper.getShort(eobrResponse, 16, 2)
                driveStartSpeed = convertToMph(rawDriveStartSpeed)
            }
        }

        answer.apply {
            putInt(Constants.RETURNCODE, errorCode)
            putInt(Constants.RPMTHRESHOLD, rpmThreshold)
            putFloat(Constants.SPEEDTHRESHOLD, speedThreshold)
            putFloat(Constants.HARDBRAKETHRESHOLD, hardBrakeThreshold)
            putFloat(Constants.DRIVESTARTDISTANCETHRESHOLD, driveStartDistanceThreshold)
            putInt(Constants.DRIVESTOPTIMETHRESHOLD, driveStopTimeThreshold)
            putInt(Constants.EVENTBLANKINGTHRESHOLD, eventBlankingThreshold)
            putFloat(Constants.DRIVESTARTSPEED, driveStartSpeed)
            putInt(Constants.DRIVERIDCRC, driverIdCRC)
        }

        return answer
    }

    override fun SetThresholdValues(rpmThreshold: Int, speedThreshold: Float, hardBrakeThreshold: Float, driveStartDistance: Float, driveStopTime: Int, eventBlanking: Int, driverId: String, driveStartSpeed: Float): Bundle {
        val needsPreMandateDataSize = isDriveStartSpeedUnsupported()
        val errorCode: Int
        val dataSize: Int
        val cmdId = EUCMDType.EUCMD_SET_THRESHOLDS
        var driverIdCrc = 0

        if (needsPreMandateDataSize)
            dataSize = 16
        else
            dataSize = 18

        val packetData = ByteArray(dataSize)

        // add rpm threshold to packetdata
        var value = shortToByteArray(rpmThreshold.toShort())
        packetData[0] = value[0]
        packetData[1] = value[1]

        // add speed threshold to packetdata
        // value needs to be converted to KM and multiplied by 100 (per protocol
        // doc) - send the resulting int value to the TAB
        var convertedValue = (speedThreshold * Constants.MILES_TO_KILOMETERS * 100f).toShort()
        value = shortToByteArray(convertedValue)
        packetData[2] = value[0]
        packetData[3] = value[1]

        // add hardbrake threshold to packetdata
        // value needs to be converted to KM and multiplied by 100 (per protocol
        // doc) - send the resulting int value to the TAB
        convertedValue = (hardBrakeThreshold * Constants.MILES_TO_KILOMETERS * 100f).toShort()
        value = shortToByteArray(convertedValue)
        packetData[4] = value[0]
        packetData[5] = value[1]

        // add drive start threshold to packetdata
        // value needs to be converted to KM and multiplied by 100 (per protocol
        // doc) - send the resulting int value to the TAB
        convertedValue = (driveStartDistance * Constants.MILES_TO_KILOMETERS * 100f).toShort()
        value = shortToByteArray(convertedValue)
        packetData[6] = value[0]
        packetData[7] = value[1]

        // add drive stop threshold to packetdata - convert minutes to seconds
        value = shortToByteArray((driveStopTime * 60).toShort())
        packetData[8] = value[0]
        packetData[9] = value[1]

        // add event blanking value to packetdata
        value = shortToByteArray(eventBlanking.toShort())
        packetData[10] = value[0]
        packetData[11] = value[1]

        // if driverId is specified, calc CRC and add value to packetData
        if (driverId.isNotEmpty()) {
            driverIdCrc = CalcCRC.Calculate(driverId, driverId.length.toLong())
            value = intToByteArray(driverIdCrc)
            packetData[12] = value[0]
            packetData[13] = value[1]
            packetData[14] = value[2]
            packetData[15] = value[3]
        } else {// set default threshold values - set driver id to 0xffffffff
            packetData[12] = 0xff.toByte()
            packetData[13] = 0xff.toByte()
            packetData[14] = 0xff.toByte()
            packetData[15] = 0xff.toByte()
        }

        if (!needsPreMandateDataSize) {
            convertedValue = (driveStartSpeed * Constants.MILES_TO_KILOMETERS * 100f).toShort()
            value = shortToByteArray(convertedValue)
            packetData[16] = value[0]
            packetData[17] = value[1]
        }

        val eobrPacket = buildEobrPacket(cmdId, packetData)
        val commResponsePacket = Eobr_Comm_Response_Packet()
        errorCode = EobrCommunications.validateWithData(threadMgr, eobrPacket, cmdId,
                getActiveDeviceCrc(), VerifyRxPacketResponseByte.EobrNullPacket,
                commResponsePacket, false, socketVerifier)

        val response = Bundle()
        response.putInt(Constants.RETURNCODE, errorCode)
        response.putInt(Constants.DRIVERIDCRC, driverIdCrc)

        return response
    }

    private fun isDriveStartSpeedUnsupported(): Boolean {
        val firmwareVersion = GetEOBRDllRevisions()

        if (firmwareVersion.getInt(Constants.RETURNCODE) == EobrReturnCode.S_SUCCESS) {

            //starting in 6.88, the TAB became capable of accepting
            //a new "drive start speed" for the mandate.
            val minVersion = 6.88

            val revision = firmwareVersion.getString(Constants.MAINFIRMWAREREVISION)

            if (revision != null) {
                val pattern = Pattern.compile("\\d+\\.\\d+")
                val matcher = pattern.matcher(revision)

                if (matcher.find()) {
                    val currentVersion = java.lang.Double.parseDouble(matcher.group())

                    return currentVersion < minVersion
                }
            }
        }

        return true
    }

    override fun GetEventData(eventData: EventRecord, queryMethod: StatusRecordQueryMethodEnum, recordId: Int, timeCode: Long, eventType: EventTypeEnum, resetReferenceTimestampToCurrent: Boolean): Int {
        return GetEventData(eventData, queryMethod, recordId, timeCode, eventType, resetReferenceTimestampToCurrent, -1)
    }

    override fun GetEventData(eventData: EventRecord, queryMethod: StatusRecordQueryMethodEnum, recordId: Int, timeCode: Long, eventType: EventTypeEnum, resetReferenceTimestampToCurrent: Boolean, eventMask: Int): Int {
        val cmdId = EUCMDType.EUCMD_GET_EVENT_DATA
        var retVal: Int
        val eobrEventPacket = buildEobrGenIIPacket(cmdId, queryMethod.value)

        // If eventMask is supplied, then we will vary how we format the command
        if (eventMask >= 0) {
            eobrEventPacket.len = 19.toByte()
            eobrEventPacket.eventType = EventTypeEnum.ANYTYPE.toByte()
            eobrEventPacket.eventMask = eventMask
        } else {
            eobrEventPacket.len = 15.toByte()
            eobrEventPacket.eventType = eventType.value.toByte()
        }

        if (queryMethod.value == StatusRecordQueryMethodEnum.RECORDID) {
            eobrEventPacket.recordId = recordId
            eobrEventPacket.timecode = 0
        } else {
            eobrEventPacket.recordId = 0
            eobrEventPacket.timecode = timeCode
        }

        eobrEventPacket.refTimestampOption = (if (resetReferenceTimestampToCurrent) 1 else 0).toByte()

        val commResponsePacket = Eobr_Comm_Response_Packet()
        retVal = EobrCommunications.validate(threadMgr, eobrEventPacket, cmdId,
                getActiveDeviceCrc(), commResponsePacket, socketVerifier)

        if (retVal != EobrReturnCode.S_SUCCESS) {
            return retVal
        }

        val response = commResponsePacket.response
        eventData.recordId = LittleEndianHelper.getInt(response, 2, 4)
        // timestamp stored in TAB as # of milliseconds since 1/1/1970
        eventData.timecode = LittleEndianHelper.getLong(response, 6, 8)
        eventData.eventType = response[14].toInt()
        eventData.eventData = LittleEndianHelper.getInt(response, 15, 4)
        eventData.driverId = LittleEndianHelper.getInt(response, 19, 4)
        eventData.eobrId = LittleEndianHelper.getInt(response, 23, 4)

        val tripData = TripReport()
        val lookupRecordId = eventData.eventData
        val driveEvents = arrayOf(EventTypeEnum.DRIVESTART, EventTypeEnum.DRIVEEND)
        // Get associated trip report data for Drive Start/End events.
        if (eventData.eventData > 0 && driveEvents.contains(eventData.eventType)) {
            retVal = GetTripData(tripData, StatusRecordQueryMethodEnum(StatusRecordQueryMethodEnum.RECORDID), lookupRecordId, -1, false)
            prepareForDataRecordingMalfunctionCheck(lookupRecordId, tripData.recordId)
            eventData.tripReportData = tripData
        }

        val ignitionEvents = arrayOf(EventTypeEnum.IGNITIONON, EventTypeEnum.IGNITIONOFF)
        // Get associated trip report data for Ignition On/Off events.
        if (isIgnitionTripReportSupported) {
            if (eventData.eventData > 0 && ignitionEvents.contains(eventData.eventType)) {
                retVal = GetTripData(tripData, StatusRecordQueryMethodEnum(StatusRecordQueryMethodEnum.RECORDID), lookupRecordId, -1, false)
                prepareForDataRecordingMalfunctionCheck(lookupRecordId, tripData.recordId)
                eventData.tripReportData = tripData
            }
        }

        return retVal
    }

    private fun prepareForDataRecordingMalfunctionCheck(queryRecordId: Int, resultRecordId: Int) {
        if (isEldMandateMode) {
            var statusBufferNumberOfTrips: Int? = null

            // If the lookup failed for a valid queryRecordId, then we need to go get the number of trips from the status buffer to help determine if a malfunction should be created or not
            if (queryRecordId != 0 && resultRecordId <= 0) {
                val sb = GetStatusBuffer().data
                statusBufferNumberOfTrips = sb.numberOfTrips
            }
            MalfunctionManager.getInstance().checkDataRecordingMalfunction(queryRecordId, resultRecordId, statusBufferNumberOfTrips)
        }
    }

    override fun GetTripData(eventData: TripReport, queryMethod: StatusRecordQueryMethodEnum, recordId: Int, timeCode: Long, resetReferenceTimestampToCurrent: Boolean): Int {
        val cmdId = EUCMDType.EUCMD_GET_TRIP_REPORT
        val eobrEventPacket = buildEobrGenIIPacket(cmdId, queryMethod.value)
        eobrEventPacket.len = 14.toByte()
        eobrEventPacket.recordId = recordId
        eobrEventPacket.refTimestampOption = (if (resetReferenceTimestampToCurrent) 1 else 0).toByte()

        // time code only provided for historical data or next motion change data
        if (timeCode > 0) {
            eobrEventPacket.timecode = timeCode
        }

        val commResponsePacket = Eobr_Comm_Response_Packet()
        val retVal = EobrCommunications.validate(threadMgr, eobrEventPacket, cmdId,
                getActiveDeviceCrc(), commResponsePacket, socketVerifier)

        if (retVal == EobrReturnCode.S_SUCCESS) {
            ProcessDataHelper.processTripReportDataResponse(commResponsePacket.response, eventData)
        } else {
            LogCat.getInstance().e("BTGenII", "Error Reading Trip Data!")
        }

        return retVal
    }

    override fun GetHistogramData(histogramData: HistogramData, queryMethod: StatusRecordQueryMethodEnum, recordId: Int, timeCode: Long, histogramType: HistogramTypeEnum, setRefTime: Boolean): Int {
        val cmdId = EUCMDType.EUCMD_GET_HISTOGRAM
        val retVal: Int

        val eobrHistogramReportPacket = buildEobrGenIIPacket(cmdId, queryMethod.value)
        eobrHistogramReportPacket.len = 15.toByte()
        eobrHistogramReportPacket.recordId = recordId
        eobrHistogramReportPacket.refTimestampOption = (if (setRefTime) 1 else 0).toByte()
        eobrHistogramReportPacket.eventType = histogramType.value.toByte()

        // time code only provided if retrieving data by timestamp
        if (timeCode > 0) {
            eobrHistogramReportPacket.timecode = timeCode
        }

        val commResponsePacket = Eobr_Comm_Response_Packet()
        retVal = EobrCommunications.validate(threadMgr, eobrHistogramReportPacket,
                cmdId, getActiveDeviceCrc(), commResponsePacket, socketVerifier)

        if (retVal == EobrReturnCode.S_SUCCESS) {
            ProcessDataHelper.processHistogramDataResponse(commResponsePacket.response, histogramData)
        }

        return retVal
    }

    override fun GetJBusDiagnosticDataFromDevice(diagnosticData: JbusDiagnosticData, queryMethod: StatusRecordQueryMethodEnum, recordId: Int, timestamp: Long, setRefTime: Boolean): Int {
        val cmdId = EUCMDType.EUCMD_GET_JBUS_DIAG_DATA
        val retVal: Int

        val eobrDataPacket = buildEobrGenIIPacket(cmdId, queryMethod.value)
        eobrDataPacket.len = 14.toByte()
        eobrDataPacket.recordId = recordId
        eobrDataPacket.refTimestampOption = (if (setRefTime == true) 1 else 0).toByte()

        if (timestamp > 0) {
            eobrDataPacket.timecode = timestamp
        }

        val commResponsePacket = Eobr_Comm_Response_Packet()
        retVal = EobrCommunications.validate(threadMgr, eobrDataPacket, cmdId,
                getActiveDeviceCrc(), commResponsePacket, socketVerifier)

        if (retVal != EobrReturnCode.S_SUCCESS) {
            return retVal
        }

        val responsePacket = EobrJbusDiagDataPacket(commResponsePacket.response)

        // Fill in the results
        diagnosticData.associatedEobrRecordId = responsePacket.associatedEobrRecordId
        diagnosticData.timestamp = Date(responsePacket.timecode)
        diagnosticData.recordId = responsePacket.recordId

        val dtcList = ArrayList<DTCInformation>()
        val responseDTCList = responsePacket.dtcList
        for (dtcInfo in responseDTCList) {
            val dtc = DTCInformation().apply {
                type = dtcInfo.type
                source = dtcInfo.source
                dtc = dtcInfo.dtc
            }
            dtcList.add(dtc)
        }
        diagnosticData.dtcList = dtcList

        return retVal
    }

    override fun GetConsoleLog(startDate: Date?, endDate: Date?): Bundle {
        val returnBundle = Bundle()
        var nextRecordId: Int
        val finalRecordId: Int
        val consoleLog = StringBuilder()

        //get the first segment of the console log
        val startBundle = getConsoleLog(startDate)

        if (startBundle.getInt(Constants.RETURNCODE) != EobrReturnCode.S_SUCCESS) {
            returnBundle.putInt(Constants.RETURNCODE, startBundle.getInt(Constants.RETURNCODE))
            return returnBundle
        }

        //get the ID of the next console log segment
        nextRecordId = startBundle.getInt(NEXT_CONSOLE_LOG_RECORD_ID)

        //capture the first segment of the console log
        consoleLog.append(startBundle.getString(Constants.RETURNVALUE))

        //find the record ID of the console log at the requested end date
        val endBundle = getConsoleLog(endDate)

        if (endBundle.getInt(Constants.RETURNCODE) == EobrReturnCode.S_SUCCESS) {
            finalRecordId = endBundle.getInt(NEXT_CONSOLE_LOG_RECORD_ID)

            //keep getting the console log 250 bytes at a time
            //until we hit the requested end
            while (nextRecordId < finalRecordId) {
                //get the next segment of the console log
                val bundle = getConsoleLog(nextRecordId)

                if (bundle.getInt(Constants.RETURNCODE) == EobrReturnCode.S_SUCCESS) {
                    //keep track of what the next segment is
                    nextRecordId = bundle.getInt(NEXT_CONSOLE_LOG_RECORD_ID)

                    //save this segment
                    consoleLog.append(bundle.getString(Constants.RETURNVALUE))
                } else {
                    returnBundle.putInt(Constants.RETURNCODE, bundle.getInt(Constants.RETURNCODE))
                    break
                }
            }

            if (!returnBundle.containsKey(Constants.RETURNCODE)) {
                returnBundle.putInt(Constants.RETURNCODE, EobrReturnCode.S_SUCCESS)
                returnBundle.putString(Constants.RETURNVALUE, consoleLog.toString())
            }
        } else {
            returnBundle.putInt(Constants.RETURNCODE, endBundle.getInt(Constants.RETURNCODE))
        }

        return returnBundle
    }

    /**
     * Gets the first 250 bytes of the console log at (approximately) the specified timestamp
     *
     * @param timestamp
     * @return bundle with return code, the record ID for the next 250 bytes of the log, and the 250 bytes itself
     */
    private fun getConsoleLog(timestamp: Date?): Bundle {
        return getConsoleLog(StatusRecordQueryMethodEnum(StatusRecordQueryMethodEnum.TIMESTAMP), 0, timestamp)
    }

    /**
     * Gets the first 250 bytes of the console log at the specified record ID
     *
     * @param recordId
     * @return bundle with return code, the record ID for the next 250 bytes of the log, and the 250 bytes itself
     */
    private fun getConsoleLog(recordId: Int): Bundle {
        return getConsoleLog(StatusRecordQueryMethodEnum(StatusRecordQueryMethodEnum.RECORDID), recordId, null)
    }

    /**
     * Gets the first 250 bytes of the console log at the specified timestamp or record ID
     *
     * @param queryMethod
     * @param recordId
     * @param timestamp
     * @return bundle with return code, the record ID for the next 250 bytes of the log, and the 250 bytes itself
     */
    private fun getConsoleLog(queryMethod: StatusRecordQueryMethodEnum, recordId: Int, timestamp: Date?): Bundle {
        var retVal: Int
        val cmdId = EUCMDType.EUCMD_GET_CONSOLE_LOG

        val eobrDataPacket = buildEobrGenIIPacket(cmdId, queryMethod.value)
        eobrDataPacket.len = 9.toByte()
        eobrDataPacket.recordId = recordId

        if (timestamp != null) {
            eobrDataPacket.timecode = timestamp.time
        }

        val commResponsePacket = Eobr_Comm_Response_Packet()
        retVal = EobrCommunications.validate(threadMgr, eobrDataPacket, cmdId,
                getActiveDeviceCrc(), commResponsePacket, socketVerifier)

        var consoleLog: String? = null
        var nextConsoleLogRecordId = 0

        if (retVal == EobrReturnCode.S_SUCCESS) {
            val eobrStrPacket = EobrStringPacket.build(commResponsePacket.response)

            if (eobrStrPacket == null)
                retVal = EobrReturnCode.S_GENERAL_ERROR
            else {
                nextConsoleLogRecordId = LittleEndianHelper.getInt(eobrStrPacket.stringVal, 0, 4)

                if (eobrStrPacket.size > 4) {
                    val consoleLogSb = StringBuilder()

                    for (i in 4 until eobrStrPacket.size)
                        consoleLogSb.append(eobrStrPacket.stringVal[i].toChar())

                    consoleLog = consoleLogSb.toString()
                }
            }
        }

        val answer = Bundle()
        answer.putInt(Constants.RETURNCODE, retVal)
        answer.putInt(NEXT_CONSOLE_LOG_RECORD_ID, nextConsoleLogRecordId)
        answer.putString(Constants.RETURNVALUE, consoleLog)

        return answer
    }

    override fun GetEobrGeneration(): Int = 2

    override fun ClearAllEobrData() {
        clearVehicleSpeedHistogramData()
        clearEngineSpeedHistogramData()
        clearEngineLoadHistogramData()
        clearGpsDopHistogramData()
        clearGpsSecondsToFirstFixHistogramData()
        clearAllRecordData()
        clearStatusBufferData()
    }

    private fun clearAllRecordData() {
        val rc = ClearAllRecordData(0x00)
        val eobrException = EobrException.getEobrExceptionFromEobrReturnCodeValue(rc)
        if (eobrException != null)
            throw eobrException
    }

    private fun clearGpsSecondsToFirstFixHistogramData() {
        clearHistogramData(HistogramType.TAB_HIST_GPS_SECS)
    }

    private fun clearGpsDopHistogramData() {
        clearHistogramData(HistogramType.TAB_HIST_GPS_DOP)
    }

    private fun clearEngineLoadHistogramData() {

        clearHistogramData(HistogramType.TAB_HIST_LOAD)
    }

    private fun clearEngineSpeedHistogramData() {
        clearHistogramData(HistogramType.TAB_HIST_RPM)
    }

    private fun clearVehicleSpeedHistogramData() {
        clearHistogramData(HistogramType.TAB_HIST_VSS)
    }

    override fun IsJJK(ctx: Context): Boolean {
        var isJJK = true
        val bundle = GetEobrHardware()
        val status = bundle.getInt(ctx.getString(R.string.rc))

        // if return code is successful, then the ELD is NOT a JJK ELD
        if (status == EobrReturnCode.S_SUCCESS && bundle.containsKey(ctx.getString(R.string.returnvalue)))
            isJJK = bundle.getBoolean(ctx.getString(R.string.returnvalue))

        return isJJK
    }

    override fun RequestFirmwareUpgrade(firmwarePatchId: Long): FirmwareUpgradeRequestResult {
        val cmdId = EUCMDType.EUCMD_GET_EOBR_UPGRADE_REQ

        //the 32-bit patch ID is stored as a long because Java lacks unsigned types
        //and we needed to be able to treat it as its actual positive value.
        //but here the bits are just bits
        val packetData = intToByteArray(firmwarePatchId.toInt())
        val request = buildEobrPacket(cmdId, packetData)

        val response = Eobr_Comm_Response_Packet()
        val returnCode = EobrCommunications.validateWithData(threadMgr, request, cmdId,
                getActiveDeviceCrc(), VerifyRxPacketResponseByte.EobrBytePacket,
                response, false, socketVerifier)

        val result = FirmwareUpgradeRequestResult(returnCode)

        if (returnCode == EobrReturnCode.S_SUCCESS) {
            val packet = EobrBytePacket(response.response)

            result.status = packet.byteVal
        }

        return result
    }

    override fun GetFirmwareUpgradeStatus(): FirmwareUpgradeStatusResult {
        val cmdId = EUCMDType.EUCMD_GET_EOBR_UPGRADE_STATUS
        val returnCode: Int

        val responsePacket = Eobr_Comm_Response_Packet()
        returnCode = EobrCommunications.validateNoData(threadMgr, cmdId,
                getActiveDeviceCrc(),
                VerifyRxPacketResponseByte.EobrStringPacket, responsePacket, socketVerifier)

        val result = FirmwareUpgradeStatusResult(returnCode)

        if (returnCode == EobrReturnCode.S_SUCCESS) {
            val stagedFirmwarePatchId = LittleEndianHelper.getInt(responsePacket.response, 2, 4) and 0xFFFFFFFFL.toInt()
            result.stagedFirmwarePatchId = stagedFirmwarePatchId.toLong()
            val requestedFirmwarePatchId = LittleEndianHelper.getInt(responsePacket.response, 6, 4) and 0xFFFFFFFFL.toInt()
            result.requestedFirmwarePatchId = requestedFirmwarePatchId.toLong()
            val currentFirmwarePatchId = LittleEndianHelper.getInt(responsePacket.response, 10, 4) and 0xFFFFFFFFL.toInt()
            result.currentFirmwarePatchId = currentFirmwarePatchId.toLong()
        }

        return result
    }

    override fun GetDriveData(typeEnum: DriveDataTypeEnum?, timeCode: Long, timeStep: Short, maxUncertainty: Short): EobrResponse<DriveData> {
        val dataSize = 15
        val minStop: Short = 0 //set to 0 to disable period merging in the ELD
        val cmdId = EUCMDType.EUCMD_GET_DRIVE_DATA

        val data = ByteBuffer.allocate(dataSize).apply {
            order(ByteOrder.LITTLE_ENDIAN)
            put((if (typeEnum == DriveDataTypeEnum.DRIVEPERIOD) 0 else 1).toByte())
            putLong(timeCode)
            putShort(timeStep)
            putShort(minStop)
            putShort(maxUncertainty)
        }

        val bytes = ByteArray(dataSize)
        data.position(0)
        data.get(bytes)
        val eobrPacket = buildEobrPacket(cmdId, bytes)

        val commResponsePacket = Eobr_Comm_Response_Packet()
        // extendBarrierTime indicates to use 15s threshold for send command with data
        val errorCode = EobrCommunications.validateWithData(threadMgr, eobrPacket, cmdId,
                getActiveDeviceCrc(), VerifyRxPacketResponseByte.EobrCustomParmPacket,
                commResponsePacket, true, socketVerifier)

        val result = EobrResponse<DriveData>(errorCode)

        if (result.returnCode == EobrReturnCode.S_SUCCESS) {
            val response = commResponsePacket.response

            val payload = ByteBuffer.wrap(response, 2, response[1].toInt() and 0xFF)
            val driveData = DriveData.FromByteBuffer(payload)
            result.data = driveData
        }

        return result
    }

    override fun SetReferenceTimestamps(timestamps: EobrReferenceTimestamps): Int {
        val dataSize = 40
        val cmdId = EUCMDType.EUCMD_SET_REF_TIMESTAMP

        val data = ByteBuffer.allocate(dataSize).apply {
            order(ByteOrder.LITTLE_ENDIAN)
            putLong(timestamps.eobrReferenceTime)
            putLong(timestamps.eventReferenceTime)
            putLong(timestamps.histogramReferenceTime)
            putLong(timestamps.tripReferenceTime)
            putLong(timestamps.dtcReferenceTime)
        }

        val bytes = ByteArray(dataSize)
        data.position(0)
        data.get(bytes)

        val eobrPacket = buildEobrPacket(cmdId, bytes)

        val commResponsePacket = Eobr_Comm_Response_Packet()
        var errorCode = EobrCommunications.validateWithData(threadMgr, eobrPacket, cmdId,
                getActiveDeviceCrc(), VerifyRxPacketResponseByte.EobrBytePacket,
                commResponsePacket, false, socketVerifier)

        if (errorCode == EobrReturnCode.S_SUCCESS) {
            val packet = EobrBytePacket(commResponsePacket.response)

            //0 is success
            if (packet.byteVal.toInt() != 0)
                errorCode = EobrReturnCode.S_GENERAL_ERROR
        }

        return errorCode
    }

    override fun IsGetDriveDataSupported(): Boolean {
        return isCommandSupported(EUCMDType.EUCMD_GET_DRIVE_DATA)
    }

    override fun IsGetEventDataEventMaskSupported(): Boolean {
        var result = false
        // We don't currently support this on the BTE (Gen2 & !isJJK)
        if (IsJJK(GlobalState.getInstance().applicationContext)) {
            // Starting in 6.89.5, the TAB became capable of accepting the "EventMask"
            // in the "Cmd_EUCMD_GET_EVENT_DATA" command for the mandate.
            // the mandate firmware version was set to 6.88.110 (which corresponds to 6.89.34)
            result = isFirmwareVersionSupported(6, 88, 110)
        }
        return result
    }

    override fun SetIsEldMandate(isEldMandate: Boolean): Int {
        isEldMandateMode = isEldMandate
        return SetCustomParameter(if (isEldMandate) 1 else 0, CUSTOM_PARAMETER_ELDMANDATE)
    }

    override fun SetDisableReadEldVin(isEldReadingVin: Boolean): Int {
        var answer = EobrReturnCode.S_GENERAL_ERROR

        // first we need to read the current parameter value
        val bundle = this.GetCustomParameter(CUSTOM_PARAMETER_ELDREADVIN)

        if (bundle.containsKey(Constants.RETURNCODE)) {
            val rc = bundle.getInt(Constants.RETURNCODE)
            if (rc == EobrReturnCode.S_SUCCESS) {
                // now we need to set the bit correctly
                var parmValue = bundle.getInt(Constants.RETURNVALUE)
                if (isEldReadingVin)
                // turn on the bit
                    parmValue = parmValue or FLAG_CUSTPARM10_READ_VEHICLE_VIN
                else
                // turn off the bit
                    parmValue = parmValue and FLAG_CUSTPARM10_READ_VEHICLE_VIN.inv()

                answer = this.SetCustomParameter(parmValue, CUSTOM_PARAMETER_ELDREADVIN)
            }
        }

        return answer
    }

    override fun GetDisableReadEldVin(): Bundle {
        val answer = Bundle()

        // setup the answer with default values
        answer.putBoolean(Constants.RETURNVALUE, false)
        answer.putInt(Constants.RETURNCODE, EobrReturnCode.S_DEV_INTERNAL_ERROR)

        val bundle = this.GetCustomParameter(CUSTOM_PARAMETER_ELDREADVIN)

        if (bundle.containsKey(Constants.RETURNCODE)) {
            val rc = bundle.getInt(Constants.RETURNCODE)
            answer.putInt(Constants.RETURNCODE, rc)
            if (rc == EobrReturnCode.S_SUCCESS) {
                val parmValue = bundle.getInt(Constants.RETURNVALUE)
                if (0 != parmValue and FLAG_CUSTPARM10_READ_VEHICLE_VIN) {
                    // the bit is turned on, so the return value is true
                    // THIS MEANS READING OF VIN IS DISABLED!!!!!
                    answer.putBoolean(Constants.RETURNVALUE, true)
                }
            }
        }

        return answer
    }

    override fun GetStatusBuffer(): EobrResponse<StatusBuffer> {
        val cmdId = EUCMDType.EUCMD_GET_STATUS_BUFFER
        val eobrPacket = buildEobrPacket(cmdId)

        val commResponsePacket = Eobr_Comm_Response_Packet()
        val errorCode = EobrCommunications.validateWithData(threadMgr, eobrPacket,
                cmdId, getActiveDeviceCrc(), VerifyRxPacketResponseByte.EobrCustomParmPacket,
                commResponsePacket, false, socketVerifier)

        val result = EobrResponse<StatusBuffer>(errorCode)
        if (result.returnCode == EobrReturnCode.S_SUCCESS) {
            val response = commResponsePacket.response
            val responsePayloadLength = response[1] and 0xFF.toByte()
            val payload = ByteBuffer.wrap(response, 2, responsePayloadLength.toInt())

            val statusBuffer = StatusBuffer.FromByteBuffer(payload)
            result.data = statusBuffer
        }
        return result
    }

    override fun GetEobrHardware(): Bundle {
        var errorCode = EobrReturnCode.S_FUNC_NOT_IMPLEMENTED
        val cmdId = EUCMDType.EUCMD_GET_EOBR_HARDWARE
        val answer = Bundle()
        var isJJK = false

        // 2014.09.17 sjn - The Get_Eobr_Hardware command is not implemented on all Gen 2 ELDs.
        //                  Verify that the command is supported before sending it.
        //                  The IsCommandSupported design is also not implemented universally yet.
        //                  If the IsCommandSupported=false then read the firmware version and only send the command on BTE firmware
        //                  The issue is that the current JJK Gen2 firmware in prod (6.60) does not support this command
        //                  and there are many attempts to send this command. Each time it is sent to 6.60, it fails.
        //                  This is a workaround to eliminate the failures form 6.60.
        var sendCommand = isCommandSupported(EUCMDType.EUCMD_GET_EOBR_HARDWARE)

        if (!sendCommand) {
            val firmwareVersion = this.GetEOBRDllRevisions()
            if (firmwareVersion.getInt(Constants.RETURNCODE) == EobrReturnCode.S_SUCCESS
                    && firmwareVersion.containsKey(Constants.MAINFIRMWAREREVISION)
                    && firmwareVersion.getString(Constants.MAINFIRMWAREREVISION).startsWith("2.")) {
                sendCommand = true
            }
        }

        if (sendCommand) {
            // The EUCMD_GET_EOBR_HARDWARE command is implemented in the JJK ELD as of FW 6.70
            val commResponsePacket = Eobr_Comm_Response_Packet()
            errorCode = EobrCommunications.validateNoData(threadMgr, cmdId,
                    getActiveDeviceCrc(), VerifyRxPacketResponseByte.EobrStringPacket,
                    commResponsePacket, socketVerifier)

            if (errorCode == EobrReturnCode.S_SUCCESS) {
                val eobrHardwarePacket = EobrStringPacket.build(commResponsePacket.response)

                /*This information is returned as a single variable-length, NULL-terminated, ASCII string
	    		  with multiple data fields, separated by hash marks (#).

	    		  The returned data fields include the following:
	    			(1)	Hardware maker (e.g., "Networkfleet")
	    			(2)	Hardware model (e.g., "BTE")
	    			(3)	Hardware PCB version (e.g., "1.0")

	    			The following is a sample returned "Info" string:
	    			    "Networkfleet#BTE#1.0"
	    		*/

                // eobrHardware contains information about the EOBR device
                val eobrHardware = String(eobrHardwarePacket!!.stringVal)
                if (eobrHardware.isNotEmpty()) {
                    val info = eobrHardware.split("#")
                    info.size
                    val maker = info[0]
                    // Not being used currently
                    val model = info[1]
                    // Not being used currently
                    val version = info[2]

                    answer.putString(EobrEngineBase.EOBR_HARDWARE_MANUFACTURER, maker)
                    answer.putString(EobrEngineBase.EOBR_HARDWARE_MODEL, model)
                    answer.putString(EobrEngineBase.EOBR_HARDWARE_VERSION, version)

                    if (maker.equals("JJKeller", ignoreCase = true))
                        isJJK = true
                    else if (maker.equals("Networkfleet", ignoreCase = true))
                        isJJK = false
                }
            }
        }

        answer.putInt(Constants.RETURNCODE, errorCode)
        answer.putBoolean(Constants.RETURNVALUE, isJJK)

        return answer
    }

    private fun isCommandSupported(command: Int): Boolean {
        var valid = 0

        if (command < 256) {
            val b = currentEucmdBitmask[command / 8]

            valid = b.toInt().shr(7 - (command and 7)) and 1
        }

        return valid > 0
    }

    private fun isIgnitionTripReportSupported(): Boolean {
        var result = false
        // We don't currently support this on the BTE (Gen2 & !isJJK)
        if (IsJJK(GlobalState.getInstance().applicationContext)) {
            // Starting in 6.89.14, the TAB associated trip records to ignition on events
            // the mandate firmware version was set to 6.88.110 (which corresponds to 6.89.34)
            result = isFirmwareVersionSupported(6, 88, 110)
        }

        return result
    }

    override fun GetDriverEvent(eventData: EventRecord, startTimeCode: Long, endTimeCode: Long, eventMask: Int, includeEventsWithoutDriverId: Boolean): Int {
        val cmdId = EUCMDType.EUCMD_GET_DRIVER_EVENT
        var retVal: Int

        val eobrDriverPacket = Eobr_Driver_Event_Packet()
        eobrDriverPacket.cmd = cmdId.toByte()
        eobrDriverPacket.crc = getActiveDeviceCrc()
        eobrDriverPacket.len = 21.toByte()
        eobrDriverPacket.eventMask = eventMask
        eobrDriverPacket.startTimeCode = startTimeCode
        eobrDriverPacket.endTimeCode = endTimeCode
        if (includeEventsWithoutDriverId) {
            eobrDriverPacket.searchMethod = 1.toByte()
        } else {
            eobrDriverPacket.searchMethod = 0.toByte()
        }

        val commResponsePacket = Eobr_Comm_Response_Packet()
        retVal = EobrCommunications.sendAndConfirmDriverEvent(threadMgr,
                eobrDriverPacket, cmdId, getActiveDeviceCrc(), commResponsePacket, socketVerifier)

        if (retVal != EobrReturnCode.S_SUCCESS) {
            return retVal
        }

        val response = commResponsePacket.response

        // Check the "Status" included in the command response
        if (LittleEndianHelper.getInt(response, 2, 1) == 1)
            retVal = EobrReturnCode.S_GENERAL_ERROR

        eventData.recordId = LittleEndianHelper.getInt(response, 3, 4)
        // timestamp stored in TAB as # of milliseconds since 1/1/1970
        eventData.timecode = LittleEndianHelper.getLong(response, 7, 8)
        eventData.eventType = response[15].toInt()
        eventData.eventData = LittleEndianHelper.getInt(response, 16, 4)
        eventData.driverId = LittleEndianHelper.getInt(response, 20, 4)
        eventData.eobrId = LittleEndianHelper.getInt(response, 24, 4)

        return retVal
    }

    override fun GetDriverCount(startTimeCode: Long, endTimeCode: Long): Bundle {
        val cmdId = EUCMDType.EUCMD_GET_DRIVER_COUNT
        var returnCode: Int
        var eventCount = 0
        val driverIds = IntArray(16)

        val eobrDriverPacket = Eobr_Driver_Event_Packet()
        eobrDriverPacket.cmd = cmdId.toByte()
        eobrDriverPacket.crc = getActiveDeviceCrc()
        eobrDriverPacket.len = 16.toByte()
        eobrDriverPacket.startTimeCode = startTimeCode
        eobrDriverPacket.endTimeCode = endTimeCode

        val commResponsePacket = Eobr_Comm_Response_Packet()
        returnCode = EobrCommunications.sendAndConfirmDriverEvent(threadMgr,
                eobrDriverPacket, cmdId, getActiveDeviceCrc(), commResponsePacket, socketVerifier)

        if (returnCode == EobrReturnCode.S_SUCCESS) {
            val response = commResponsePacket.response

            // Check the "Status" included in the command response
            if (LittleEndianHelper.getInt(response, 2, 1) == 1)
                returnCode = EobrReturnCode.S_GENERAL_ERROR

            eventCount = LittleEndianHelper.getShort(response, 3, 2).toInt()

            val endOfArrayMarker: Short = -1
            var tempVal: Int
            for (i in 0..15) {
                tempVal = LittleEndianHelper.getInt(response, 5 + i * 4, 4)
                driverIds[i] = if (tempVal == endOfArrayMarker.toInt()) 0 else tempVal
            }
        }

        return Bundle().apply {
            putInt(Constants.RETURNCODE, returnCode)
            putIntArray(Constants.DRIVERIDS, driverIds)
            putInt(Constants.RETURNVALUE, eventCount)
        }
    }

    companion object {
        protected const val RECORD_ID_TYPE = 0.toByte()
        protected const val TIMESTAMP_TYPE = 1.toByte()   // query the data given a timestamp
        protected const val NEXT_CONSOLE_LOG_RECORD_ID = "NextConsoleLogRecordId"
        const val EOBR_FW_BLOCK_CODE_ADDRESS_SIZE = 4
        const val EOBR_FW_BLOCK_CODE_TYPE_SIZE = 1
        protected const val FW_APPLICATION = 1
        // The Firmware Upgrade Start and End commands
        protected const val FW_UPGRADE_BEGIN = 1
        protected const val FW_UPGRADE_END = 0
        protected const val FW_UPGRADE_RESET = 2

        protected const val EOBR_FW_BLOCK_CODE_SIZE_SMALL = 48
        protected const val EOBR_FW_BLOCK_CODE_SIZE_LARGE = 250

        var eobrFirmwareBlockCodeSize = 52
        protected var eobrFirmwarePayloadSize = 56
        protected var eobrFirmwarePacketSize = 61

        protected const val CUSTOM_PARAMETER_ELDREADVIN = 10
        protected const val CUSTOM_PARAMETER_ELDMANDATE = 11

        //bitmask values for enable/disable reading vehicle vin
        private const val FLAG_CUSTPARM10_READ_VEHICLE_VIN = 1 shl 0   // 0x00000001


        protected var currentEucmdBitmask = ByteArray(32)
        protected val EUCMD_BITMASK_DEFAULT = byteArrayOf(0x7f, 0xff.toByte(), 0xff.toByte(),
                0xff.toByte(), 0xff.toByte(), 0xc8.toByte(), 0xff.toByte(), 0xc0.toByte(),
                0xfc.toByte(), 0x00, 0x00, 0x00, 0x00, 0x00, 0xfc.toByte(), 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x01)

        protected fun intToByteArray(value: Int): ByteArray {
            //NOTE:  Data on eobr is LITTLE_ENDIAN
            return byteArrayOf(value.toByte(),
                                value.ushr(8).toByte(),
                                value.ushr(16).toByte(),
                                value.ushr(24).toByte())
        }

        protected fun shortToByteArray(value: Short): ByteArray {
            //NOTE:  Data on eobr is LITTLE_ENDIAN
            return byteArrayOf(value.toByte(), value.toInt().ushr(8).toByte())
        }
    }
}