package com.jjkeller.kmb.crashlytics;


import android.util.Log;

import com.crashlytics.android.Crashlytics;

import java.util.Date;

public class Utils {

    private static final String TAG = Utils.class.getSimpleName();

    public static void LogCrashlytics(Throwable error){
        Crashlytics.logException(error);
        Log.v(TAG, "Logged in to Crashlytics: " + error.getMessage());
    }


    public static void GenerateError(){
        throw new RuntimeException(String.format("Crashlytics error generated: %s", new Date().toString()));
    }
}
