package com.jjkeller.kmbapi.eobrengine

import java.nio.ByteBuffer
import java.nio.ByteOrder

class LittleEndianHelper {
    companion object {
        fun getInt(response: ByteArray, start: Int, byteCount: Int): Int {
            return getBuffer(response, start, byteCount).int
        }

        fun getLong(response: ByteArray, start: Int, byteCount: Int): Long {
            return getBuffer(response, start, byteCount).long
        }

        fun getShort(response: ByteArray, start: Int, byteCount: Int): Short {
            return getBuffer(response, start, byteCount).short
        }

        fun getFloat(response: ByteArray, start: Int, byteCount: Int): Float {
            return getBuffer(response, start, byteCount).float
        }

        fun getBuffer(response: ByteArray, start: Int, byteCount: Int): ByteBuffer {
            val buffer = ByteBuffer.wrap(
                    response,
                    start,
                    byteCount
            )
            buffer.order(ByteOrder.LITTLE_ENDIAN)
            return buffer
        }
    }
}