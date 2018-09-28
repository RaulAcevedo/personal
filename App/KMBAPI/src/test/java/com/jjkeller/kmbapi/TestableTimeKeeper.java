package com.jjkeller.kmbapi;

import com.jjkeller.kmbapi.common.TimeKeeper;

import org.joda.time.DateTime;

/**
 * Created by jld5296 on 2/9/17.
 */
public class TestableTimeKeeper extends TimeKeeper {

    DateTime settableNow = new DateTime();

    public TestableTimeKeeper(DateTime overridenNow) {
        settableNow = overridenNow;
    }

    public void setSettableNow(DateTime settableNow) {
        this.settableNow = settableNow;
    }

    @Override
    public DateTime getDeviceTime() {
        return settableNow;
    }
}
