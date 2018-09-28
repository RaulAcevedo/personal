package com.jjkeller.kmbapi.proxydata;

import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.utility.DateUtility;

import java.util.Date;

public class LogHyrail extends ProxyBase {


    ///////////////////////////////////////////////////////////////////////////////////////
    // private members
    ///////////////////////////////////////////////////////////////////////////////////////

    private Date startTime;
    private Date endTime;
    private Location startLocation = new Location();
    private Location endLocation = new Location();
    private float totalDistance;
    private String tractorNumber;

    private String employeeCode;	// only used for submitting to the API
    private Date logDate;			// only used for submitting to the API

    ///////////////////////////////////////////////////////////////////////////////////////
    // public get/set methods
    ///////////////////////////////////////////////////////////////////////////////////////

    public Date getStartTime() {
        return startTime;
    }
    public void setStartTime(Date startTime) {
        if(GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled())
            this.startTime = startTime;
        else
            this.startTime = DateUtility.getDateTimeWithoutSecondsFromDate(startTime);
    }
    public Date getEndTime() {
        return endTime;
    }
    public void setEndTime(Date endTime) {
        if(GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled())
            this.endTime = endTime;
        else
            this.endTime = DateUtility.getDateTimeWithoutSecondsFromDate(endTime);
    }

    public Location getStartLocation() {
        if (startLocation == null) startLocation = new Location();
        return startLocation;
    }
    public void setStartLocation(Location location) {
        this.startLocation = location;
    }
    public Location getEndLocation() {
        if (endLocation == null) endLocation = new Location();
        return endLocation;
    }
    public void setEndLocation(Location location) {
        this.endLocation = location;
    }

    public float getTotalDistance() {
        return this.totalDistance;
    }
    public void setTotalDistance(float distance) {
        this.totalDistance = distance;
    }
    public String getTractorNumber() {
        return this.tractorNumber;
    }
    public void setTractorNumber(String tractorNumber) {
        this.tractorNumber = tractorNumber;
    }

    public String getEmployeeCode() {
        return this.employeeCode;
    }
    public void setEmployeeCode(String empCode) {
        this.employeeCode = empCode;
    }
    public Date getLogDate() {
        return this.logDate;
    }
    public void setLogDate(Date logDate) {
        this.logDate = logDate;
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // Custom methods
    ///////////////////////////////////////////////////////////////////////////////////////
}
