package com.jjkeller.kmbapi.common;


import android.util.Log;

import org.joda.time.DateTime;

import java.util.Calendar;
import java.util.Date;

public class TimeKeeper implements ITimeKeeper {
    protected long _offsetFromServerTime;
    private boolean _hasClockBeenSynchronized;

    private static final String LOG_TAG = "TimeKeeper";

    protected TimeKeeper(){

    }

    public void synchronizeWithServerTime(DateTime serverTime){
        if(serverTime != null) {
            _offsetFromServerTime = getDeviceTime().getMillis() - serverTime.getMillis();
            _hasClockBeenSynchronized = true;
            Log.w(LOG_TAG, "Setting timekeeper time:" + serverTime.toString());
        }
    }

    @Override
    public void synchronizeWithGPSTime(DateTime gpsTime){
        if(!_hasClockBeenSynchronized && gpsTime != null) {
            Log.w(LOG_TAG, "Setting timekeeper to gps time:" + gpsTime.toString());
            _offsetFromServerTime = getDeviceTime().getMillis() - gpsTime.getMillis();
        }else{
            Log.w(LOG_TAG, "Server time already established. Not setting TimeKeeper to GPS time.");
        }
    }


    @Deprecated
    public void synchronizeWithServerTime(Date serverTime){
        synchronizeWithServerTime(new DateTime(serverTime));
    }

    private static ITimeKeeper timeKeeper;
    public static ITimeKeeper getInstance(){
        if(timeKeeper == null){
            timeKeeper = new TimeKeeper();
        }

        return timeKeeper;
    }

    public static void setTimeKeeper(ITimeKeeper keeper)
    {
        timeKeeper = keeper;
    }

    @Override
    public Date now(){
        return getCurrentDateTime().toDate();
    }

    @Override
    public DateTime getCurrentDateTime() {
        return getDeviceTime().withDurationAdded(_offsetFromServerTime, -1);
    }

    @Deprecated
    @Override
    public Date midnight() {
        Calendar result = Calendar.getInstance();
        result.setTime(now());
        result.set(Calendar.HOUR, 0);
        result.set(Calendar.MINUTE, 0);
        result.set(Calendar.SECOND, 0);
        result.set(Calendar.AM_PM, 0);
        return result.getTime();
    }

    @Override
    public long getCurrentOffsetFromServerTime() {
        return _offsetFromServerTime;
    }

    @Override
    public DateTime getDeviceTime(){
        return DateTime.now();
    }
}
