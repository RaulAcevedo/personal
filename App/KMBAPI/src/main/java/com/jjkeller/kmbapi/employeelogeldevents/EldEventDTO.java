package com.jjkeller.kmbapi.employeelogeldevents;

import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;

import java.util.Date;

/**
 * Wrap multiple return values into a DTO.
 */
public class EldEventDTO {
    private EmployeeLogEldEvent _eldEvent;
    private Date _endDateTime;

    public EmployeeLogEldEvent getEmployeeLogEldEvent() { return _eldEvent; }

    public Date getEndDateTime() { return _endDateTime; }

    public EldEventDTO(EmployeeLogEldEvent eldEvent, Date endDateTime) {
        _eldEvent = eldEvent;
        _endDateTime = endDateTime;
    }
}
