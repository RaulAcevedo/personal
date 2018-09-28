package com.jjkeller.kmbapi.employeelogeldevents;

import java.util.Date;

/**
 * Created by t000620 on 7/11/2017.
 */

public class UnclaimedEventDTO {

    public UnclaimedEventDTO(Integer eventCode, Date eventDate) {
        _eventCode = eventCode;
        _eventDate = eventDate;
    }

    private Integer _eventCode;
    public Integer getEventCode() { return _eventCode; }
    public void setEventCode(Integer value) { _eventCode = value; }

    private Date _eventDate;
    public Date getEventDate() { return _eventDate; }
    public void setEventDate(Date value) { _eventDate = value; }
}
