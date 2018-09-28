package com.jjkeller.kmbapi.eobrengine;

import java.nio.ByteBuffer;

public class FirmwareCRC {

    static int[] crcTable;

    public static boolean isHashEqual(byte[] bytes, int signedExpectedCRC) {
        long calculatedCrc = calculateHash(bytes);
        long expectedUnsignedCRC = getUnsignedInt(signedExpectedCRC);

        return calculatedCrc == expectedUnsignedCRC;
    }

    public static long calculateHash(byte[] bytes) {
        createCrcTable();

        int start = 0, size = bytes.length;
        final int seed = 429496729;
        int crc = seed;

        for (int i = start; i < size - start; i++) {
            short thisItem = getUnsignedByte(bytes[i]);
            crc = (crc >> 8) ^ crcTable[thisItem ^ crc & 0xff];
        }

        int crcA = crc ^ -1;
        short[] unsignedBytes = crcToUnsignedBytes(crcA);
        return getUnsignedInt(unsignedBytes);
    }

    private static void createCrcTable() {
        if (crcTable != null) return;

        final int DefaultPolynomial = 79764919;
        crcTable = new int[256];

        for (int i = 0; i < crcTable.length; i++) {
            int entry = i;
            for (int j = 0; j < 8; j++) {
                if ((entry & 1) == 1)
                    entry = (entry >> 1) ^ DefaultPolynomial;
                else
                    entry = entry >> 1;
            }
            crcTable[i] = entry;
        }
    }

    private static short[] crcToUnsignedBytes(int crc) {
        ByteBuffer bytes = ByteBuffer.allocate(4);
        bytes.putInt(crc);
        byte[] signedByteArray = bytes.array();

        short[] unsignedBytes = new short[4];
        for(int index = 0; index < signedByteArray.length; index++) {
            unsignedBytes[index] = getUnsignedByte(signedByteArray[index]);
        }

        return unsignedBytes;
    }

    private static short getUnsignedByte(byte signed) {
        return (short)(signed & 0xff);
    }

    private static long getUnsignedInt(short[] unsignedBytes) {
        long value = 0;
        for (int i = 0; i < unsignedBytes.length; i++)
        {
            value += ((long) unsignedBytes[i] & 0xffL) << (8 * i);
        }
        return value;
    }

    public static long getUnsignedInt(int i) {
        return i & 0x00000000ffffffffL;
    }
}
