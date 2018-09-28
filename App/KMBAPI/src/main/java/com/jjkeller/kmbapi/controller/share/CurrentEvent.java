package com.jjkeller.kmbapi.controller.share;

import android.content.Context;
import android.os.Bundle;

import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.enums.DutyStatusEnum;

import java.util.Date;

/**
 * Created by jld5296 on 10/31/16.
 */
public class CurrentEvent {
    public Date logDate;
    public Date eventTimestamp;
    public DutyStatusEnum dutyStatusEnum;
    public boolean isAutomaticDrivingEvent;

    @Override
    public String toString() {
        return "CurrentEvent{" +
                "logDate=" + logDate +
                ", eventTimestamp=" + eventTimestamp +
                ", dutyStatusEnum=" + dutyStatusEnum +
                ", isAutomaticDrivingEvent=" + isAutomaticDrivingEvent +
                '}';
    }

    public Bundle toBundle(String logDateKey, String startTimeKey, String dutyStatusKey, String isAutomaticDrivingEventKey, Context ctx) {
        Bundle currentEvent = new Bundle();
        currentEvent.putString(logDateKey, DateUtility.getHomeTerminalDateFormat().format(logDate));
        currentEvent.putString(startTimeKey, DateUtility.getHomeTerminalDateTimeFormat().format(eventTimestamp));
        currentEvent.putString(dutyStatusKey, dutyStatusEnum.getString(ctx));
        currentEvent.putBoolean(isAutomaticDrivingEventKey, isAutomaticDrivingEvent);

        return currentEvent;
    }
}
