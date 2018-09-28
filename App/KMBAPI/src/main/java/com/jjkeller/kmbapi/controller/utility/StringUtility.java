package com.jjkeller.kmbapi.controller.utility;

public class StringUtility {
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static boolean notNullOrEmpty(String str) {
        return !isNullOrEmpty(str);
    }
}
