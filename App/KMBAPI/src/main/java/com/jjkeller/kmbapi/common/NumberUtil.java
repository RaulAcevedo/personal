package com.jjkeller.kmbapi.common;

public final class NumberUtil {

    /**
     * Converts the integer to a long by unsigned conversion.
     * Zero and positive int values are mapped to a numerically equal long value and negative int values are mapped to a long value equal to the input plus 2<sup>32</sup>.
     *
     * @param i the value to convert to an unsigned long
     * @return the integer converted to a long by unsigned conversion
     */
    public static long toUnsignedLong(int i) {
        return i & 0x00000000FFFFFFFFL;
    }

}
