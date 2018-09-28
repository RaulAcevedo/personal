package com.jjkeller.kmbapi.common;

import org.joda.time.DateTime;

import java.util.Date;
import java.util.TimeZone;

/**
 * Created by jld5296 on 10/13/16.
 */
public interface ITimeKeeper {

    /**
     * @deprecated Use version with JodaTime instead.
     */
    @Deprecated
    void synchronizeWithServerTime(Date serverTime);
    void synchronizeWithServerTime(DateTime serverTime);

    void synchronizeWithGPSTime(DateTime serverTime);

    /**
     * @deprecated Use getCurrentDateTime() instead. It uses JodaTime.
     */
    @Deprecated
    Date now();

    DateTime getCurrentDateTime();

    long getCurrentOffsetFromServerTime();

    /**
     *
     * @deprecated This doesn't handle milliseconds and shouldn't be in the TimeKeeper.
     * This relies on the TimeZone.setDefault() to have been set correctly. Which cannot be counted
     * on according to Android Framework notes. This is fragile and should NOT be used.
     */
    @Deprecated
    Date midnight();

    DateTime getDeviceTime();
}
