package com.jjkeller.kmbapi.common;

import java.util.Locale;

/**
 * Created by jld5296 on 9/26/16.
 */

public final class Hex {

    // Converts a string to a byte array
    public static byte[] ConvertToHex(String s)
    {
        String trimmedString = s.replace(" ", "");

        int iLen = trimmedString.length();
        byte[] abHex = new byte[iLen / 2];

        for (int i = 0; i < iLen; i += 2)
        {
            abHex[i / 2] = (byte) ((Character.digit(trimmedString.charAt(i), 16) << 4) + Character.digit(trimmedString.charAt(i + 1), 16));
        }

        return abHex;
    }

    // Converts a byte array to a string
    public static StringBuffer convertToString(byte[] abIn)
    {
        StringBuffer sData = new StringBuffer();

        for (int i = 0; i < abIn.length; i++)
        {
            if ((abIn[i] >> 4) == 0)
                sData.append('0');

            sData.append(Integer.toHexString(abIn[i] & 0xFF).toUpperCase(Locale.US) + " ");
        }

        return sData;
    }
}