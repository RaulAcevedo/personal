package com.jjkeller.kmbapi.proxydata;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.utility.DateUtility;

import java.util.Date;

/**
 * Created by jar5943 on 4/21/2016.
 */
public class EmployeeLogWithProvisions extends ProxyBase {
///////////////////////////////////////////////////////////////////////////////////////
    // private members
    ///////////////////////////////////////////////////////////////////////////////////////
    @Expose(deserialize = false, serialize = false)
    private int employeeLogEldEventId;
    @SerializedName("StartTime")
    private Date startTime;
    @SerializedName("EndTime")
    private Date endTime;
    @SerializedName("StartLocation")
    private Location startLocation = new Location();
    @SerializedName("EndLocation")
    private Location endLocation = new Location();
    @SerializedName("TotalDistance")
    private float totalDistance;
    @SerializedName("TractorNumber")
    private String tractorNumber;

    @SerializedName("EmployeeCode")
    private String employeeCode;	// only used for submitting to the API

    @SerializedName("LogDate")
    private Date logDate;			// only used for submitting to the API

    @SerializedName("ProvisionTypeEnum")
    private int provisionTypeEnum;

    ///////////////////////////////////////////////////////////////////////////////////////
    // public get/set methods
    ///////////////////////////////////////////////////////////////////////////////////////

    public int getEmployeeLogEldEventId() {
        return employeeLogEldEventId;
    }

    public void setEmployeeLogEldEventId(int employeeLogEldEventId) {
        this.employeeLogEldEventId = employeeLogEldEventId;
    }

    public int getProvisionTypeEnum() { return provisionTypeEnum; }
    public void setProvisionTypeEnum(int provisionTypeEnum){
        this.provisionTypeEnum = provisionTypeEnum;
    }

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

