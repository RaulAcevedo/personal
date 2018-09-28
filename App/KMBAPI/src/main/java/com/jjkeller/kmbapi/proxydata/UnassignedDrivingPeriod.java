package com.jjkeller.kmbapi.proxydata;

import java.util.Date;


public class UnassignedDrivingPeriod extends ProxyBase {

	///////////////////////////////////////////////////////////////////////////////////////
	// private members
	///////////////////////////////////////////////////////////////////////////////////////
	private String eobrId;
	private String eobrSerialNumber;
	private Date startTime;
	private Date stopTime;
	private GpsLocation startLocation = new GpsLocation();
	private GpsLocation stopLocation = new GpsLocation();
	private float distance = 0;
	private float startOdometer = -1;
	private float stopOdometer = -1;
	private String motionPictureAuthorityId;
	private String motionPictureProductionId;
	private String EncompassId;
	private boolean IsClaimed;
	private boolean IsSubmitted;

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

	public GpsLocation getStartLocation() {
        if (startLocation == null) startLocation = new GpsLocation();
		return startLocation;
	}
	public void setStartLocation(GpsLocation value) {
		this.startLocation = value;
	}

	public GpsLocation getStopLocation() {
        if (stopLocation == null) stopLocation = new GpsLocation();
		return stopLocation;
	}
	public void setStopLocation(GpsLocation value) {
		this.stopLocation = value;
	}

	public float getDistance() {
		return distance;
	}
	public void setDistance(float value) {
		this.distance = value;
	}

	public float getStartOdometer() {
		return startOdometer;
	}
	public void setStartOdometer(float value) {
		this.startOdometer = value;
	}

	public float getStopOdometer() {
		return stopOdometer;
	}
	public void setStopOdometer(float value) {
		this.stopOdometer = value;
	}

	public String getMotionPictureAuthorityId() { return motionPictureAuthorityId; }
	public void setMotionPictureAuthorityId(String value) { this.motionPictureAuthorityId = value; }

	public String getMotionPictureProductionId() { return motionPictureProductionId; }
	public void setMotionPictureProductionId(String value) { this.motionPictureProductionId = value; }

	public String getEncompassId() { return EncompassId; }
	public void setEncompassId(String value) { this.EncompassId = value; }

	public boolean getIsClaimed() { return IsClaimed; }
	public void setIsClaimed(boolean value) { this.IsClaimed = value; }

	public boolean getIsSubmitted() { return IsSubmitted; }
	public void setIsSubmitted(boolean value) { this.IsSubmitted = value; }
	///////////////////////////////////////////////////////////////////////////////////////
	// Custom methods
	///////////////////////////////////////////////////////////////////////////////////////
}
