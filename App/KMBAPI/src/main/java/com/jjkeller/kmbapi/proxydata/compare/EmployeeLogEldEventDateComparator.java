package com.jjkeller.kmbapi.proxydata.compare;

import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;

import java.util.Comparator;

/**
 * Created by T000684 on 5/11/2017.
 */

public class EmployeeLogEldEventDateComparator implements Comparator<EmployeeLogEldEvent> {

    @Override
    public int compare(EmployeeLogEldEvent left, EmployeeLogEldEvent right) {
        return left.getEventDateTime().compareTo(right.getEventDateTime());
    }
}
