package com.jjkeller.kmbapi.common;

import android.annotation.TargetApi;
import android.os.Build;
import android.util.Log;

/**
 * Created by jld5296 on 10/3/16.
 */

public class LogCat {
    private static LogCat _instance;

    public static LogCat getInstance(){
        if(_instance == null)
        {
            _instance = new LogCat();
        }

        return _instance;
    }

    public static void setInstance (LogCat instance){
        _instance = instance;
    }

    public void v(String tag, String message){
        Log.v(tag, message);
    }

    public void v(String tag, String message, Throwable exception){
        Log.v(tag, message, exception);
    }

    public void i(String tag, String message){
        Log.i(tag, message);
    }

    public void i(String tag, String message, Throwable exception){
        Log.i(tag, message, exception);
    }

    public void d(String tag, String message){
        Log.d(tag, message);
    }

    public void d(String tag, String message, Throwable exception){
        Log.d(tag, message, exception);
    }

    public void w(String tag, String message){
        Log.w(tag, message);
    }

    public void w(String tag, String message, Throwable exception){
        Log.w(tag, message, exception);
    }

    @TargetApi(Build.VERSION_CODES.FROYO)
    public void wtf(String tag, String message){
        Log.wtf(tag, message);
    }


    @TargetApi(Build.VERSION_CODES.FROYO)
    public void wtf(String tag, String message, Throwable exception){
        Log.wtf(tag, message, exception);
    }

    public void e(String tag, String message){
        Log.e(tag, message);
    }

    public void e(String tag, String message, Throwable exception){
        Log.e(tag, message, exception);

    }

    public String getStackTraceString(Throwable ex){
        return Log.getStackTraceString(ex);
    }

    public boolean isLoggable(String tag, int level){
        return Log.isLoggable(tag, level);
    }

    public void println(int priority, String tag, String message){
        Log.println(priority, tag, message);
    }
}
