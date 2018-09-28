package com.jjkeller.kmbapi.proxydata;


import java.util.Date;

public class EventDataRecord extends ProxyBase {

	///////////////////////////////////////////////////////////////////////////////////////
	// private members
	///////////////////////////////////////////////////////////////////////////////////////
	private String driverEmployeeId;
	private String eobrSerialNumber = null;
	private Date eobrTimestamp = null;
    private int eventType;
    private int eventData;
    private float odometer = 0f;
    private float speedometer = 0f;
    private float tachometer = 0f;
    private float gpsLatitude = 0f;
    private float gpsLongitude = 0f;

	///////////////////////////////////////////////////////////////////////////////////////
	// public get/set methods
	///////////////////////////////////////////////////////////////////////////////////////
	public Date getEobrTimestamp() {
		return eobrTimestamp;
	}

	public void setEobrTimestamp(Date eobrTimestamp) {
		this.eobrTimestamp = eobrTimestamp;
	}

	public float getGpsLatitude() {
		return gpsLatitude;
	}

	public void setGpsLatitude(float gpsLatitude) {
		this.gpsLatitude = gpsLatitude;
	}

	public float getGpsLongitude() {
		return gpsLongitude;
	}

	public void setGpsLongitude(float gpsLongitude) {
		this.gpsLongitude = gpsLongitude;
	}

	public float getOdometer() {
		return odometer;
	}

	public void setOdometer(float odometer) {
		this.odometer = odometer;
	}
	
	public float getSpeedometer() {
		return speedometer;
	}

	public void setSpeedometer(float speedometer) {
		this.speedometer = speedometer;
	}
	
	public float getTachometer() {
		return tachometer;
	}

	public void setTachometer(float tachometer) {
		this.tachometer = tachometer;
	}
	
	public int getEventType() {
		return eventType;
	}

	public void setEventType(int eventType) {
		this.eventType = eventType;
	}
	
	public int getEventData() {
		return eventData;
	}

	public void setEventData(int eventData) {
		this.eventData = eventData;
	}

	public String getEobrSerialNumber() {
		return eobrSerialNumber;
	}

	public void setEobrSerialNumber(String eobrSerialNumber) {
		this.eobrSerialNumber = eobrSerialNumber;
	}

	public String getDriverEmployeeId() {
		return driverEmployeeId;
	}

	public void setDriverEmployeeId(String driverEmployeeId) {
		this.driverEmployeeId = driverEmployeeId;
	}
	
	///////////////////////////////////////////////////////////////////////////////////////
	// Custom methods
	///////////////////////////////////////////////////////////////////////////////////////
}
