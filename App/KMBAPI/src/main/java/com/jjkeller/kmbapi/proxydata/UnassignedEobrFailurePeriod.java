package com.jjkeller.kmbapi.proxydata;

import java.util.Date;

public class UnassignedEobrFailurePeriod extends ProxyBase {

	///////////////////////////////////////////////////////////////////////////////////////
	// private members
	///////////////////////////////////////////////////////////////////////////////////////
	private String eobrId;
	private String eobrSerialNumber;
	private Date startTime;
	private Date stopTime;
	private String message;
	
	///////////////////////////////////////////////////////////////////////////////////////
	// public get/set methods
	///////////////////////////////////////////////////////////////////////////////////////
	public String getEobrId() {
		return eobrId;
	}
	public void setEobrId(String value) {
		this.eobrId = value;
	}

	public String getEobrSerialNumber() {
		return eobrSerialNumber;
	}
	public void setEobrSerialNumber(String value) {
		this.eobrSerialNumber = value;
	}

	public Date getStartTime() {
		return startTime;
	}
	public void setStartTime(Date value) {
		this.startTime = value;
	}

	public Date getStopTime() {
		return stopTime;
	}
	public void setStopTime(Date value) {
		this.stopTime = value;
	}

	public String getMessage() {
		return message;
	}
	public void setMessage(String value) {
		this.message = value;
	}

	

	///////////////////////////////////////////////////////////////////////////////////////
	// Custom methods
	///////////////////////////////////////////////////////////////////////////////////////
}
