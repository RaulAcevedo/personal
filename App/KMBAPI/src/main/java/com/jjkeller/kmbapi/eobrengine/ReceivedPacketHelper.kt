package com.jjkeller.kmbapi.eobrengine

import com.jjkeller.kmbapi.eobrengine.eobrdatatypes.EobrBytePacket
import com.jjkeller.kmbapi.eobrengine.eobrdatatypes.EobrClockPacket
import com.jjkeller.kmbapi.eobrengine.eobrdatatypes.EobrCustomParmPacket
import com.jjkeller.kmbapi.eobrengine.eobrdatatypes.EobrIntegerPacket
import com.jjkeller.kmbapi.eobrengine.eobrdatatypes.EobrPacketBase
import com.jjkeller.kmbapi.eobrengine.eobrdatatypes.EobrShortPacket
import com.jjkeller.kmbapi.eobrengine.eobrdatatypes.EobrStringPacket
import com.jjkeller.kmbapi.kmbeobr.Enums.EobrReturnCode

class ReceivedPacketHelper {
    enum class VerifyRxPacketResponseByte {
        EobrStringPacket,
        EobrIntegerPacket,
        EobrShortPacket,
        EobrCustomParmPacket,
        EobrNullPacket,
        EobrClockPacket,
        EobrBytePacket
        //EobrLongPacket,
    }

    companion object {
        private const val cmdMsb = 0x80    // The MSB of the command
        private const val cmdMsbZero = 0x7F   // This is used to set the MSB to zero
        private const val replyDevNotConnected = 0x40

        fun verifyPacket(cmdId: Int, response: Byte): Int {
            var responseInt = response.toInt()

            if (responseInt and 0xFF == cmdId) {
                return EobrReturnCode.S_SUCCESS
            }

            val errorCode: Int
            // CRC signature error
            if ((responseInt and cmdMsb) > 0) {
                // Zero the MSB bit, because if CRC error, we only set the MSB bit to 1
                responseInt = responseInt and cmdMsbZero

                if (responseInt == cmdId) {
                    errorCode = EobrReturnCode.S_WRONG_SIGNATURE_CRC
                } else {
                    errorCode = EobrReturnCode.S_WRONG_COMMAND_BACK
                }
            } else if (responseInt and replyDevNotConnected > 0) {
                errorCode = EobrReturnCode.S_DEV_NOT_CONNECTED
            } else {
                errorCode = EobrReturnCode.S_WRONG_COMMAND_BACK
            }

            return errorCode
        }

        fun verifyPacket(cmdId: Int, packetType: VerifyRxPacketResponseByte, response: ByteArray): Int {
            // Validate that the command id passed in is what was returned.
            val eobrPacket: EobrPacketBase

            when (packetType) {
                VerifyRxPacketResponseByte.EobrStringPacket -> {
                    eobrPacket = EobrStringPacket.build(response)

                    if (eobrPacket == null)
                        return EobrReturnCode.S_NO_DATA
                }
                VerifyRxPacketResponseByte.EobrIntegerPacket ->
                    eobrPacket = EobrIntegerPacket(response)
                VerifyRxPacketResponseByte.EobrShortPacket ->
                    eobrPacket = EobrShortPacket(response)
                VerifyRxPacketResponseByte.EobrCustomParmPacket ->
                    eobrPacket = EobrCustomParmPacket(response)
                VerifyRxPacketResponseByte.EobrClockPacket ->
                    eobrPacket = EobrClockPacket(response)
                VerifyRxPacketResponseByte.EobrBytePacket ->
                    eobrPacket = EobrBytePacket(response)
                VerifyRxPacketResponseByte.EobrNullPacket ->
                    return getStatus(cmdId, response)
            }

            return verifyPacket(cmdId, eobrPacket.cmd)
        }

        fun getStatus(cmd: Int, response: ByteArray): Int {
            var eobrStatus: Int

            when (cmd) {
                EUCMDType.EUCMD_SET_CLOCK_UTC,
                EUCMDType.EUCMD_TEST_CONNECTION,
                EUCMDType.EUCMD_CHANGE_ACTIVE_BUS,
                EUCMDType.EUCMD_CHANGE_DATA_RATE,
                EUCMDType.EUCMD_SET_UNIT_ID,
                EUCMDType.EUCMD_SET_ENGINE_OFF_COMMS_TIMEOUT,
                EUCMDType.EUCMD_CLEAR_RECORD_DATA,
                EUCMDType.EUCMD_START_FW_UPGRADE,
                EUCMDType.EUCMD_GET_FW_IMAGE_REQ,
                EUCMDType.EUCMD_FW_IMAGE_BLOCK,
                EUCMDType.EUCMD_SET_DEBUG_FLAGS,
                EUCMDType.EUCMD_SET_COMPANY_PASSKEY,
                EUCMDType.EUCMD_SHUTDOWN,
                EUCMDType.EUCMD_SET_CUSTOM_PARAMETER,
                EUCMDType.EUCMD_SET_ODOMETER_OFFSET,
                EUCMDType.EUCMD_SET_SELF_TEST,
                EUCMDType.EUCMD_SET_THRESHOLDS -> {
                    eobrStatus = verifyPacket(cmd, response[0])
                    if (eobrStatus == EobrReturnCode.S_SUCCESS)
                        eobrStatus = response[2].toInt()
                }
                else ->
                    eobrStatus = EobrReturnCode.S_GENERAL_ERROR
            }

            return eobrStatus
        }
    }
}