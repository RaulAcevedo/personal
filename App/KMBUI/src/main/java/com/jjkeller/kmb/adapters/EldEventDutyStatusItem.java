package com.jjkeller.kmb.adapters;

import com.jjkeller.kmbapi.employeelogeldevents.Enums;
import com.jjkeller.kmbapi.enums.EmployeeLogProvisionTypeEnum;

/**
 * Represents info about DutyStatus spinner data
 */
public class EldEventDutyStatusItem {
    private String _name;
    private String _subName;
    private Enums.EmployeeLogEldEventType _eventType;
    private int _eventCode;
    private Enums.SpecialDrivingCategory _subStatus;

    public String getName() {
        return _name;
    }

    public String getSubName() {
        return _subName;
    }

    public Enums.EmployeeLogEldEventType getEventType() {
        return _eventType;
    }

    public int getEventCode() {
        return _eventCode;
    }

    public Enums.SpecialDrivingCategory getSubStatus() {
        return _subStatus;
    }



    public EldEventDutyStatusItem(String name, String subName, Enums.EmployeeLogEldEventType eventType, int eventCode, Enums.SpecialDrivingCategory subStatus) {
        this._name = name;
        this._subName = subName;
        this._eventType = eventType;
        this._eventCode = eventCode;
        this._subStatus = subStatus;
    }
}
