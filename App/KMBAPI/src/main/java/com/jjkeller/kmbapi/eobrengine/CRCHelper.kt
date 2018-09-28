package com.jjkeller.kmbapi.eobrengine

import com.jjkeller.kmbapi.common.LogCat
import com.jjkeller.kmbapi.controller.utility.ErrorLogHelper

class CRCHelper {
    companion object {

        fun isValidCRCValue(crc: Short): Boolean {
            return crc.toInt() != 0
        }

        fun checkAndLogValidCommandAndCrcCombination(cmdId: Int, crc: Short): Boolean {
            val result = isValidCommandAndCrcCombination(cmdId, crc)
            if (!result) {
                val stackTrace = LogCat.getInstance().getStackTraceString(Exception())
                val msg = "Invalid CRC $crc for command $cmdId. Failed and will return S_DEV_NOT_CONNECTED. StackTrace: $stackTrace"
                ErrorLogHelper.RecordMessage(msg)
            }
            return result
        }

        private fun isCommandValidWithoutCRC(cmdId: Int): Boolean {
            // The following commands DO NOT require a valid CRC, but all others DO.  We don't currently issue any *_KEEP_ALIVE commands from Android right now,
            // but they were included to make the following check more complete given feedback/research from Bruce on the matter.
            return cmdId == EUCMDType.EUCMD_GET_SERIAL_NUMBER || cmdId == EUCMDType.EUCMD_INIT_KEEP_ALIVE
                    || cmdId == EUCMDType.EUCMD_START_KEEP_ALIVE || cmdId == EUCMDType.EUCMD_STOP_KEEP_ALIVE

        }

        private fun isValidCommandAndCrcCombination(cmdId: Int, crc: Short): Boolean {
            return isCommandValidWithoutCRC(cmdId) || isValidCRCValue(crc)
        }
    }
}