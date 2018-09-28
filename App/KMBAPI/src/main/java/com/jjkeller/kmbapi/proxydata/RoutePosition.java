package com.jjkeller.kmbapi.proxydata;

import java.util.Date;


public class RoutePosition extends ProxyBase{

	///////////////////////////////////////////////////////////////////////////////////////
	// private members
	///////////////////////////////////////////////////////////////////////////////////////
	private String eobrId;
	private String eobrSerialNumber;
	private Date gpsTimestamp;
	private float gpsLatitude;
	private float gpsLongitude;
	private float odometer;
	private boolean isUnladen;
	
	///////////////////////////////////////////////////////////////////////////////////////
	// public get/set methods
	///////////////////////////////////////////////////////////////////////////////////////
	public String getEobrId() {
		return this.eobrId;
	}
	public void setEobrId(String eobrId) {
		this.eobrId = eobrId;
	}
	
	public String getEobrSerialNumber() {
		return this.eobrSerialNumber;
	}
	public void setEobrSerialNumber(String eobrSerialNumber) {
		this.eobrSerialNumber = eobrSerialNumber;
	}
	
	public Date getGpsTimestamp() {
		return this.gpsTimestamp;
	}
	public void setGpsTimestamp(Date gpsTimestamp) {
		this.gpsTimestamp = gpsTimestamp;
	}
	
	public Float getGpsLatitude()
	{
		return this.gpsLatitude;
	}
	public void setGpsLatitude(Float gpsLatitude)
	{
		this.gpsLatitude = gpsLatitude;
	}

	public Float getGpsLongitude()
	{
		return this.gpsLongitude;
	}
	public void setGpsLongitude(Float gpsLongitude)
	{
		this.gpsLongitude = gpsLongitude;
	}

	public Float getOdometer()
	{
		return this.odometer;
	}
	public void setOdometer(Float odometer)
	{
		this.odometer = odometer;
	}

	public boolean getIsUnladen() {
		return this.isUnladen;
	}
	public void setIsUnladen(boolean isUnladen) {
		this.isUnladen = isUnladen;
	}

	
	///////////////////////////////////////////////////////////////////////////////////////
	// Custom methods
	///////////////////////////////////////////////////////////////////////////////////////
}
