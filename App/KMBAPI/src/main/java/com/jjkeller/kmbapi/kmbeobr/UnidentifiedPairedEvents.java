package com.jjkeller.kmbapi.kmbeobr;

import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;

/**
 * Create pairs of startEvent and endEvents
 */

public class UnidentifiedPairedEvents {
    public EmployeeLogEldEvent startEvent;
    public EmployeeLogEldEvent endEvent;

    public UnidentifiedPairedEvents(EmployeeLogEldEvent startEvent, EmployeeLogEldEvent endEvent){
        this.startEvent = startEvent;
        this.endEvent = endEvent;
    }
}
