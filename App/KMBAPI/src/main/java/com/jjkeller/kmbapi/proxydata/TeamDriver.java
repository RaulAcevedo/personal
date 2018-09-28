package com.jjkeller.kmbapi.proxydata;


import java.util.Date;
import java.util.TimeZone;

public class TeamDriver extends ProxyBase {

	///////////////////////////////////////////////////////////////////////////////////////
	// private members
	///////////////////////////////////////////////////////////////////////////////////////
	private Date startTime = null;
	private Date endTime = null;
	private String employeeCode = "";
	private String employeeId = "";
	private String displayName = "";
    private String kmbUsername = "";
    private TimeZone timeZone = null;
    
	///////////////////////////////////////////////////////////////////////////////////////
	// public get/set methods
	///////////////////////////////////////////////////////////////////////////////////////
    public Date getStartTime()
    {
    	return this.startTime;
    }
    public void setStartTime(Date startTime)
    {
    	this.startTime = startTime;
    }

    public Date getEndTime()
    {
    	return this.endTime;
    }
    public void setEndTime(Date endTime)
    {
    	this.endTime = endTime;
    }
    
    public String getEmployeeCode()
    {
    	return this.employeeCode;
    }
    public void setEmployeeCode(String employeeCode)
    {
    	this.employeeCode = employeeCode;
    }

    public String getEmployeeId()
    {
    	return this.employeeId;
    }
    public void setEmployeeId(String employeeId)
    {
    	this.employeeId = employeeId;
    }

    public String getDisplayName()
    {
    	return this.displayName;
    }
    public void setDisplayName(String displayName)
    {
    	this.displayName = displayName;
    }

    public String getKMBUsername()
    {
        return this.kmbUsername;
    }
    public void setKMBUsername(String kmbUsername)
    {
        this.kmbUsername = kmbUsername;
    }

    public TimeZone getTimeZone()
    {
        return this.timeZone;
    }
    public void setTimeZone(TimeZone timeZone)
    {
        this.timeZone = timeZone;
    }
    
 	///////////////////////////////////////////////////////////////////////////////////////
	// Custom methods
	///////////////////////////////////////////////////////////////////////////////////////

    /// <summary>
    /// Answer if the team driver is active at the end of the day.
    /// This means that there is no end time defined, or it's set to midnight.
    /// </summary>
    public boolean getIsActiveAtEndOfDay()
    {
        boolean isActive = false;
        if (this.endTime == null)
        {
            isActive = true;
        }
        else
        {
            // if the time is not 12:00am, then it's also considered ended
            isActive = (this.endTime.getHours() == 0 && this.endTime.getMinutes() == 0 && this.endTime.getSeconds() == 0);
        }
        
        return isActive;
    }
    
    /// <summary>
    /// Answer if the team driver is active at the start of the day.
    /// This means that there is no start time defined, or it's set to midnight.
    /// </summary>
    public boolean getIsActiveAtStartOfDay()
    {
            boolean isActive = false;
            if (this.startTime == null)
            {
                isActive = true;
            }
            else
            {
                // if the time is not 12:00am, then it's also considered ended
            	isActive = (this.startTime.getHours() == 0 && this.startTime.getMinutes() == 0 && this.startTime.getSeconds() == 0);
            }
            return isActive;
    }

    @Override
    public String toString() {
        return "TeamDriver{" +
                "startTime=" + startTime +
                ", endTime=" + endTime +
                ", employeeCode='" + employeeCode + '\'' +
                ", employeeId='" + employeeId + '\'' +
                ", displayName='" + displayName + '\'' +
                ", kmbUsername='" + kmbUsername + '\'' +
                ", timeZone='" + timeZone + '\'' +
                '}';
    }
}
