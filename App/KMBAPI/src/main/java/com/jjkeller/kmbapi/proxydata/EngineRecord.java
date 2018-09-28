package com.jjkeller.kmbapi.proxydata;


import com.jjkeller.kmbapi.enums.EngineRecordTypeEnum;

import java.util.Date;

public class EngineRecord extends ProxyBase {

	///////////////////////////////////////////////////////////////////////////////////////
	// private members
	///////////////////////////////////////////////////////////////////////////////////////
    private Date eobrTimestamp = null;
    private Date gpsTimestamp = null;
    private float gpsLatitude = 0f;
    private float gpsLongitude = 0f;
    private float speedometer = 0f;
    private float odometer = 0f;
    private float tachometer = 0f;
    private int eobrOverallStatus = 0;
    private EngineRecordTypeEnum recordType = new EngineRecordTypeEnum(EngineRecordTypeEnum.NULL);
    private float fuelEconomyInstant = -1.0f;
    private float fuelEconomyAverage = -1.0f;
    private boolean cruiseControlSet = false;
    private float fuelUseTotal = -1.0f;
    private float brakePressure = -1.0f;
    private String transmissionSelected = null;
    private String transmissionAttained = null;
    private String eobrSerialNumber = null;
    private String eobrTractorNumber = null;
    private String driverEmployeeId;
	
	///////////////////////////////////////////////////////////////////////////////////////
	// public get/set methods
	///////////////////////////////////////////////////////////////////////////////////////
	public Date getEobrTimestamp() {
		return eobrTimestamp;
	}

	public void setEobrTimestamp(Date eobrTimestamp) {
		this.eobrTimestamp = eobrTimestamp;
	}

	public Date getGpsTimestamp() {
		return gpsTimestamp;
	}

	public void setGpsTimestamp(Date gpsTimestamp) {
		this.gpsTimestamp = gpsTimestamp;
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

	public float getSpeedometer() {
		return speedometer;
	}

	public void setSpeedometer(float speedometer) {
		this.speedometer = speedometer;
	}

	public float getOdometer() {
		return odometer;
	}

	public void setOdometer(float odometer) {
		this.odometer = odometer;
	}

	public float getTachometer() {
		return tachometer;
	}

	public void setTachometer(float tachometer) {
		this.tachometer = tachometer;
	}

	public int getEobrOverallStatus() {
		return eobrOverallStatus;
	}

	public void setEobrOverallStatus(int eobrOverallStatus) {
		this.eobrOverallStatus = eobrOverallStatus;
	}

	public EngineRecordTypeEnum getRecordType() {
		return recordType;
	}

	public void setRecordType(EngineRecordTypeEnum recordType) {
		this.recordType = recordType;
	}

	public float getFuelEconomyInstant() {
		return fuelEconomyInstant;
	}

	public void setFuelEconomyInstant(float fuelEconomyInstant) {
		this.fuelEconomyInstant = fuelEconomyInstant;
	}

	public float getFuelEconomyAverage() {
		return fuelEconomyAverage;
	}

	public void setFuelEconomyAverage(float fuelEconomyAverage) {
		this.fuelEconomyAverage = fuelEconomyAverage;
	}

	public boolean getCruiseControlSet() {
		return cruiseControlSet;
	}

	public void setCruiseControlSet(boolean cruiseControlSet) {
		this.cruiseControlSet = cruiseControlSet;
	}

	public float getFuelUseTotal() {
		return fuelUseTotal;
	}

	public void setFuelUseTotal(float fuelUseTotal) {
		this.fuelUseTotal = fuelUseTotal;
	}

	public float getBrakePressure() {
		return brakePressure;
	}

	public void setBrakePressure(float brakePressure) {
		this.brakePressure = brakePressure;
	}

	public String getTransmissionSelected() {
		return transmissionSelected;
	}

	public void setTransmissionSelected(String transmissionSelected) {
		this.transmissionSelected = transmissionSelected;
	}

	public String getTransmissionAttained() {
		return transmissionAttained;
	}

	public void setTransmissionAttained(String transmissionAttained) {
		this.transmissionAttained = transmissionAttained;
	}

	public String getEobrSerialNumber() {
		return eobrSerialNumber;
	}

	public void setEobrSerialNumber(String eobrSerialNumber) {
		this.eobrSerialNumber = eobrSerialNumber;
	}

	public String getEobrTractorNumber() {
		return eobrTractorNumber;
	}

	public void setEobrTractorNumber(String eobrTractorNumber) {
		this.eobrTractorNumber = eobrTractorNumber;
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
