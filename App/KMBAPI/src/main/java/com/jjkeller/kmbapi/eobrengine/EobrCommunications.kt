package com.jjkeller.kmbapi.eobrengine

import android.os.Bundle
import com.jjkeller.kmbapi.common.LogCat
import com.jjkeller.kmbapi.eobrengine.ReceivedPacketHelper.VerifyRxPacketResponseByte
import com.jjkeller.kmbapi.eobrengine.eobrdatatypes.*
import com.jjkeller.kmbapi.kmbeobr.Constants
import com.jjkeller.kmbapi.kmbeobr.Enums.EobrReturnCode

class EobrCommunications {
    companion object {
        fun validate(threadMgr: CommThreadManager, eobrDataPacket: Eobr_Data_Packet_GenII, cmdId: Int, activeDeviceCrc: Short
                     , commResponsePacket: Eobr_Comm_Response_Packet, verifySocketConnection: IVerifySocketConnection): Int {
            val sendCommand = { _: Int -> threadMgr.SendEOBRDataPacketGenII(eobrDataPacket) }
            val verifyPacket = { response: ByteArray -> ReceivedPacketHelper.verifyPacket(cmdId, response[0]) }
            return sendAndValidate(cmdId, activeDeviceCrc, commResponsePacket, verifySocketConnection, sendCommand, verifyPacket)
        }

        @JvmOverloads
        fun validateWithData(threadMgr: CommThreadManager, eobrPacket: Eobr_Packet, cmdId: Int,
                             activeDeviceCrc: Short, packetType: VerifyRxPacketResponseByte,
                             commResponsePacket: Eobr_Comm_Response_Packet,
                             extendBarrierTime: Boolean?,
                             verifySocketConnection: IVerifySocketConnection,
                             numberOfAttempts: Int = 5): Int {
            val sendCommand = { _: Int -> threadMgr.SendCommandWithData(eobrPacket, extendBarrierTime) }
            val verifyPacket = { response: ByteArray -> ReceivedPacketHelper.verifyPacket(cmdId, packetType, response) }

            return sendAndValidate(cmdId, activeDeviceCrc, commResponsePacket,
                    verifySocketConnection, sendCommand, verifyPacket, numberOfAttempts)
        }

        fun validateNoData(threadMgr: CommThreadManager, cmdId: Int, activeDeviceCrc: Short,
                           packetType: VerifyRxPacketResponseByte,
                           commResponsePacket: Eobr_Comm_Response_Packet,
                           verifySocketConnection: IVerifySocketConnection): Int {
            val sendCommand = { _: Int -> threadMgr.SendCommand(cmdId, activeDeviceCrc) }
            val verifyPacket = { response: ByteArray -> ReceivedPacketHelper.verifyPacket(cmdId, packetType, response) }

            return sendAndValidate(cmdId, activeDeviceCrc, commResponsePacket, verifySocketConnection, sendCommand, verifyPacket)
        }

        fun validateGenI(threadMgr: CommThreadManager, eobrDataPacket: Eobr_Data_Packet, cmdId: Int,
                         activeDeviceCrc: Short, commResponsePacket: Eobr_Comm_Response_Packet,
                         resetReferenceTimestampToCurrent: Boolean, verifySocketConnection: IVerifySocketConnection): Int {
            val sendCommand = { _: Int -> threadMgr.SendEOBRDataPacket(eobrDataPacket, resetReferenceTimestampToCurrent) }
            val verifyPacket = { response: ByteArray -> ReceivedPacketHelper.verifyPacket(cmdId, response[0]) }
            return sendAndValidate(cmdId, activeDeviceCrc, commResponsePacket, verifySocketConnection, sendCommand, verifyPacket)
        }

        fun sendAndConfirmFirmwarePacket(threadMgr: CommThreadManager, activeDeviceCrc: Short,
                                         fwBlockPacket: Eobr_FW_Block_Packet,
                                         commResponsePacket: Eobr_Comm_Response_Packet,
                                         verifySocketConnection: IVerifySocketConnection) {
            val firstAttemptTimeout = 2000
            val longerTimeout = 5000
            val sendCommand = { attemptCounter: Int ->
                val timeout = if (attemptCounter > 0) longerTimeout else firstAttemptTimeout
                threadMgr.sendEOBRFWUpdateDataPacket(fwBlockPacket, timeout)
            }
            val verifyPacket = { response: ByteArray -> ReceivedPacketHelper.verifyPacket(EUCMDType.EUCMD_FW_IMAGE_BLOCK, response[0]) }
            sendAndValidate(fwBlockPacket.cmd.toInt(), activeDeviceCrc, commResponsePacket, verifySocketConnection, sendCommand, verifyPacket)
        }

        fun sendAndConfirmDriverEvent(threadMgr: CommThreadManager,
                                      eobrDriverDataPacket: Eobr_Driver_Event_Packet, cmdId: Int,
                                      activeDeviceCrc: Short,
                                      commResponsePacket: Eobr_Comm_Response_Packet,
                                      verifySocketConnection: IVerifySocketConnection): Int {
            val sendCommand = { _: Int -> threadMgr.SendEOBRDriverEventPacket(eobrDriverDataPacket) }
            val verifyPacket = { response: ByteArray -> ReceivedPacketHelper.verifyPacket(cmdId, response[0]) }
            return sendAndValidate(cmdId, activeDeviceCrc, commResponsePacket, verifySocketConnection, sendCommand, verifyPacket)
        }

        fun processResponse(_threadMgr: CommThreadManager, cmdId: Int, activeDeviceCrc: Short,
                            verifySocketConnection: IVerifySocketConnection): Bundle {
            var answer = Bundle()
            var attemptCounter = 0
            var breakOut = false

            // Check for valid cmdId/crc combination and log/fail call immediately if
            // invalid (the alternative is we have to wait for the command to timeout)
            if (!CRCHelper.checkAndLogValidCommandAndCrcCombination(cmdId, activeDeviceCrc)) {
                breakOut = true
                answer.putInt(Constants.RETURNCODE, EobrReturnCode.S_DEV_NOT_CONNECTED)
                answer.putString(Constants.RETURNVALUE, null)
            }

            while (!breakOut && attemptCounter < 5) {

                if (attemptCounter > 0)
                    LogCat.getInstance().d("btRetry", "Retry $attemptCounter for command $cmdId")

                // Send command to EOBR and get response.
                val bundle = _threadMgr.SendCommand(cmdId, activeDeviceCrc)

                // Validate response from EOBR was successfully read.
                val bulkDataBytes = tryGetCommThreadReturnBundle(bundle)
                if (bulkDataBytes != null) {
                    answer = processStringResponse(cmdId, bulkDataBytes)

                    // Only break out if successful... otherwise retry
                    if (answer.getInt(Constants.RETURNCODE) == EobrReturnCode.S_SUCCESS
                            && answer.getString(Constants.RETURNVALUE) != null) {
                        breakOut = true
                    }

                    LogCat.getInstance().v("Barrier", "SendCommand_Resp: ${answer.getString(Constants.RETURNVALUE)}")
                } else {
                    LogCat.getInstance().v("Barrier", "SendCommand_Resp:  BulkDataBytes NULL")

                    answer.putInt(Constants.RETURNCODE, EobrReturnCode.S_DEV_NOT_CONNECTED)
                    answer.putString(Constants.RETURNVALUE, null)
                }

                attemptCounter++

                if (!breakOut && attemptCounter < 5) {
                    val commOK = verifySocketConnection.verify()

                    if (!commOK)
                        break
                }
            }

            return answer
        }

        private fun sendAndValidate(cmdId: Int, activeDeviceCrc: Short, commResponsePacket: Eobr_Comm_Response_Packet, verifySocketConnection: IVerifySocketConnection
                                    , sendDataCommand: (attemptCounter: Int) -> Bundle,
                                    validateResponseCommandId: (ByteArray) -> Int, numberOfAttempts: Int = 5): Int {
            var returnCode = EobrReturnCode.S_SUCCESS
            var attemptCounter = 0
            var breakOut = false

            if (numberOfAttempts == 1) {
                LogCat.getInstance().d("btRetry", "Sending command $cmdId")
            }

            // Check for valid cmdId/crc combination and log/fail call immediately if invalid (the alternative is we have to wait for the command to timeout)
            if (!CRCHelper.checkAndLogValidCommandAndCrcCombination(cmdId, activeDeviceCrc)) {
                breakOut = true
                returnCode = EobrReturnCode.S_DEV_NOT_CONNECTED
            }

            while (!breakOut && attemptCounter < numberOfAttempts) {

                if (attemptCounter > 0)
                    LogCat.getInstance().d("btRetry", "Retry $attemptCounter for command $cmdId")

                // Send command to EOBR and get response.
                val bundle = sendDataCommand(attemptCounter)

                // Validate response from EOBR was successfully read.
                val response = tryGetCommThreadReturnBundle(bundle)
                if (response != null) {
                    commResponsePacket.response = response

                    // Validate that the command id passed in is what was returned.
                    returnCode = validateResponseCommandId(response)

                    // Only break out if successful... otherwise retry
                    if (returnCode == EobrReturnCode.S_SUCCESS) {
                        breakOut = true
                    }
                } else {
                    returnCode = EobrReturnCode.S_DEV_NOT_CONNECTED
                }

                attemptCounter++

                if (!breakOut && attemptCounter < numberOfAttempts) {
                    val commOK = verifySocketConnection.verify()

                    if (!commOK)
                        break
                }
            }

            commResponsePacket.returnCode = returnCode
            return returnCode
        }

        private fun verifyCommThreadReturnBundle(bundle: Bundle): Boolean {
            return bundle.containsKey(EobrEngineBase.COMMTHREAD_RESPONSE)
                    && bundle.containsKey(EobrEngineBase.COMMTHREAD_RETURNCODE)
                    && bundle.getInt(EobrEngineBase.COMMTHREAD_RETURNCODE) == 0
        }

        private fun tryGetCommThreadReturnBundle(bundle: Bundle): ByteArray? {
            if (!verifyCommThreadReturnBundle(bundle))
                return null

            return bundle.getByteArray(EobrEngineBase.COMMTHREAD_RESPONSE)
        }

        // Process string response - should contain 62 bytes:
        //    1st byte = command (should match command passed in)
        //    2nd byte = size (size of string returned)
        //    Next size bytes (up to max of 60) contain string value
        private fun processStringResponse(requestCmd: Int, response: ByteArray): Bundle {
            var errorCode: Int
            val eobrStringPacket = EobrStringPacket.build(response)
            val returnVal = StringBuilder()

            if (eobrStringPacket == null) {
                errorCode = EobrReturnCode.S_GENERAL_ERROR
            } else {
                errorCode = ReceivedPacketHelper.verifyPacket(requestCmd, eobrStringPacket.cmd)
                if (errorCode == EobrReturnCode.S_SUCCESS) {
                    val size = eobrStringPacket.size
                    if (size <= EobrEngineBase.EOBR_PAYLOAD_SIZE) {
                        for (i in 0 until size) {
                            returnVal.append(eobrStringPacket.stringVal[i].toChar())
                        }
                    } else {
                        errorCode = EobrReturnCode.S_DEV_INTERNAL_ERROR
                    }
                }
            }

            val bundle = Bundle()
            bundle.putInt(Constants.RETURNCODE, errorCode)
            bundle.putString(Constants.RETURNVALUE, returnVal.toString())

            return bundle
        }
    }

    /**
     * This is used in place of a lambda expression because the code is called from Java code that
     * currently doesn't support lambdas. Once upgraded to Java 1.8, it can be replaced
     */
    interface IVerifySocketConnection {
        fun verify(): Boolean
    }
}