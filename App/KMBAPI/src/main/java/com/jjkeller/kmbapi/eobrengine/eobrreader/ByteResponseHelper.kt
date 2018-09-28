package com.jjkeller.kmbapi.eobrengine.eobrreader

import android.util.Log

class ByteResponseHelper {
    companion object {
        fun getIntegerFromCharResponse(response: ByteArray, startPos: Int, endPos: Int): Int {
            val sb = StringBuilder()

            for (i in startPos..endPos) {
                if (response[i].toInt() != 0) {
                    try {
                        sb.append(response[i].toChar())
                    } catch (ex: Exception) {

                        Log.e("UnhandledCatch", ex.message + ": " + Log.getStackTraceString(ex))
                    }
                    // if invalid character - just skip it
                }
            }

            return if (sb.isNotEmpty()) {
                try {
                    Integer.valueOf(sb.toString())
                } catch (ex: NumberFormatException) {
                    -1
                }
                // if can't convert to integer return -1
            } else
                -1
        }

        fun getFloatFromResponse(response: ByteArray, startPos: Int, endPos: Int): Float {
            val sb = StringBuilder()

            for (i in startPos..endPos) {
                if (response[i].toInt() != 0) {
                    try {
                        sb.append(response[i].toChar())
                    } catch (ex: Exception) {

                        Log.e("UnhandledCatch", ex.message + ": " + Log.getStackTraceString(ex))
                    }
                    // if invalid character - just skip it
                }
            }

            return if (sb.isNotEmpty()) {
                try {
                    java.lang.Float.valueOf(sb.toString())!!
                } catch (ex: NumberFormatException) {
                    -1f
                }

            } else
                -1f
        }
    }
}