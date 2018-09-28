package com.jjkeller.kmbapi.proxydata;


import com.jjkeller.kmbapi.controller.utility.DateUtility;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Public class representing a mapping between:
 * The driving event to be assigned
 * A driver who is being assigned to the event
 */
public class DrivingEventReassignmentMapping extends ProxyBase {
    private int _relatedEvent;
    private EmployeeLogEldEvent _eldEvent;
    private String _driverToAssignEventTo;
    private String _eventComment;
    private boolean _isSubmitted;


    public int getRelatedEvent() {
        return this._relatedEvent;
    }

    public EmployeeLogEldEvent getEldEvent() { return this._eldEvent; }

    public String getDriverToAssignEventTo() {
        return this._driverToAssignEventTo;
    }

    public String getEventComment() {
        return this._eventComment;
    }

    public boolean getIsSubmitted() {
        return this._isSubmitted;
    }


    public void setRelatedEvent(int value) {
        this._relatedEvent = value;
    }

    public void setEldEvent(EmployeeLogEldEvent value) { this._eldEvent = value; }

    public void setDriverToAssignEventTo(String value) {
        this._driverToAssignEventTo = value;
    }

    public void setEventComment(String value) {
        this._eventComment = value;
    }

    public void setIsSubmitted(boolean value) {
        this._isSubmitted = value;
    }


    @Override
    public String toString() {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        SimpleDateFormat dateFormat = DateUtility.getHomeTerminalDMOSoapDateTimestampUTCFormat();
        dateFormat.setTimeZone(tz);

        final StringBuilder sb = new StringBuilder("DrivingEventReassignmentMapping{");
        sb.append("_relatedEvent=").append(_relatedEvent);
        sb.append(", _driverToAssignEventTo='").append(_driverToAssignEventTo).append('\'');
        sb.append(", _eventComment='").append(_eventComment).append('\'');
        sb.append(", _isSubmitted=").append(_isSubmitted);
        sb.append('}');
        return sb.toString();
    }
}
