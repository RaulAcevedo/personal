package com.jjkeller.kmbapi.proxydata;


import com.jjkeller.kmbapi.enums.FailureCategoryEnum;

import org.joda.time.DateTime;

import java.util.Date;

public class FailureReport extends ProxyBase {
    @Override
    public String toString() {
        return "FailureReport{" +
                "category=" + category +
                ", startTime=" + startTime +
                ", stopTime=" + stopTime +
                ", message='" + message + '\'' +
                '}';
    }

    ///////////////////////////////////////////////////////////////////////////////////////
	// private members
	///////////////////////////////////////////////////////////////////////////////////////
	private FailureCategoryEnum category = new FailureCategoryEnum(FailureCategoryEnum.NULL);
	private DateTime startTime = new DateTime(0);
	private DateTime stopTime = new DateTime(0);
	private String message = "";
	
	///////////////////////////////////////////////////////////////////////////////////////
	// public get/set methods
	///////////////////////////////////////////////////////////////////////////////////////
    public FailureCategoryEnum getCategory()
    {
    	return this.category;
    }
    public void setCategory(FailureCategoryEnum category)
    {
    	this.category = category;
    }

    public DateTime getStartTime()
    {
    	return this.startTime;
    }
    public void setStartTime(DateTime startTime)
    {
    	this.startTime = startTime;
    }

    public DateTime getStopTime()
    {
    	return this.stopTime;
    }
    public void setStopTime(DateTime stopTime)
    {
    	this.stopTime = stopTime;
    }

    public String getMessage()
    {
    	return this.message;
    }
    public void setMessage(String message)
    {
    	this.message = message;
    }

   
	///////////////////////////////////////////////////////////////////////////////////////
	// Custom methods
	///////////////////////////////////////////////////////////////////////////////////////
}
