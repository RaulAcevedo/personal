package com.jjkeller.kmbapi.controller.share;

import android.content.Context;

import com.jjkeller.kmbapi.R;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;

/**
 * Created by T000683 on 4/21/2017.
 */

public class SelectedRecord<T> {

    public SelectedRecord(T record, T endEvent) {
        this.record = record;
        this.endEvent = endEvent;
    }

    private boolean isSelected;
    private T record;
    private T endEvent;

    public boolean getIsSelected(){return isSelected;}
    public void setIsSelected(boolean value) { isSelected = value; }

    public T getRecord() { return record; }
    public void setRecord(T value) { record = value; }

    public T getEndEvent() { return endEvent; }
    public void setEndEvent(T value) { endEvent = value; }

    public String getFormattedDateTime() {
        if (endEvent == null) {
            return ((EmployeeLogEldEvent)record).getFormattedDateTime(((EmployeeLogEldEvent)record).getEventDateTime());
        }
        else {

            EmployeeLogEldEvent driveEvent = (EmployeeLogEldEvent)record;
            EmployeeLogEldEvent ondutyEvent = (EmployeeLogEldEvent)endEvent;

            if (DateUtility.DaysBetween(driveEvent.getEventDateTime(), ondutyEvent.getEventDateTime()) == 0) {
                // same day - only show mm/dd once
                return driveEvent.getFormattedDateTime(driveEvent.getEventDateTime()) + " - " +
                        DateUtility.getHomeTerminalTime12HourFormatWithSeconds().format(ondutyEvent.getEventDateTime());
            }
            else {
                // across days - show both mm/dd
                return driveEvent.getFormattedDateTime(driveEvent.getEventDateTime()) + " - " + ondutyEvent.getFormattedDateTime(ondutyEvent.getEventDateTime());
            }
        }
    }

    public String getFormattedDistance(Context context) {
        if (endEvent == null) {
            return context.getString(R.string.notApplicable);
        }
        else {

            EmployeeLogEldEvent driveEvent = (EmployeeLogEldEvent)record;

            if (driveEvent.getDistance() == null) {
                return "0";
            }
            else {
                return driveEvent.getDistance().toString();
            }
        }
    }
}
