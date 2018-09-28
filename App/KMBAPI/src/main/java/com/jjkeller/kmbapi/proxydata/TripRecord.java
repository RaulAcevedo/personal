package com.jjkeller.kmbapi.proxydata;

import java.util.Date;

public class TripRecord extends ProxyBase
{
	///////////////////////////////////////////////////////////////////////////////////////
	// private members
	///////////////////////////////////////////////////////////////////////////////////////
	
	private String employeeId;
	private String eobrSerialNumber;
	private String eobrTractorNumber;
	private int tripNumber;
	private int ignitionState;
	private float odometer;
	private int tripSecs;
	private float tripDist;
	private int idleSecs;
	private float gpsLatitude;
	private float gpsLongitude;
	private float maxSpeed;
	private float tripFuel;
	private Date timestamp;
	private float allowedSpeed;
	private float allowedTach;
	private boolean isSubmitted;
	private int maxEngRPM;
	private int avgEngRPM;

	///////////////////////////////////////////////////////////////////////////////////////
	// public get/set methods
	///////////////////////////////////////////////////////////////////////////////////////
	
	public String getEmployeeId()
	{
		return employeeId;
	}

	public void setEmployeeId(String employeeId)
	{
		this.employeeId = employeeId;
	}
	
	public String getEobrSerialNumber()
	{
		return eobrSerialNumber;
	}

	public void setEobrSerialNumber(String eobrSerialNumber)
	{
		this.eobrSerialNumber = eobrSerialNumber;
	}

	public String getEobrTractorNumber()
	{
		return eobrTractorNumber;
	}

	public void setEobrTractorNumber(String eobrTractorNumber)
	{
		this.eobrTractorNumber = eobrTractorNumber;
	}
	
	public int getTripNumber()
	{
		return tripNumber;
	}

	public void setTripNumber(int tripNumber)
	{
		this.tripNumber = tripNumber;
	}

	public int getIgnitionState()
	{
		return ignitionState;
	}

	public void setIgnitionState(int ignitionState)
	{
		this.ignitionState = ignitionState;
	}

	public float getOdometer()
	{
		return odometer;
	}

	public void setOdometer(float odometer)
	{
		this.odometer = odometer;
	}

	public int getTripSecs()
	{
		return tripSecs;
	}

	public void setTripSecs(int tripSecs)
	{
		this.tripSecs = tripSecs;
	}

	public float getTripDist()
	{
		return tripDist;
	}

	public void setTripDist(float tripDist)
	{
		this.tripDist = tripDist;
	}

	public int getIdleSecs()
	{
		return idleSecs;
	}

	public void setIdleSecs(int idleSecs)
	{
		this.idleSecs = idleSecs;
	}

	public float getGpsLatitude()
	{
		return gpsLatitude;
	}

	public void setGpsLatitude(float gpsLatitude)
	{
		this.gpsLatitude = gpsLatitude;
	}

	public float getGpsLongitude()
	{
		return gpsLongitude;
	}

	public void setGpsLongitude(float gpsLongitude)
	{
		this.gpsLongitude = gpsLongitude;
	}

	public float getMaxSpeed()
	{
		return maxSpeed;
	}

	public void setMaxSpeed(float maxSpeed)
	{
		this.maxSpeed = maxSpeed;
	}

	public float getTripFuel()
	{
		return tripFuel;
	}

	public void setTripFuel(float tripFuel)
	{
		this.tripFuel = tripFuel;
	}

	public Date getTimestamp()
	{
		return timestamp;
	}

	public void setTimestamp(Date timestamp)
	{
		this.timestamp = timestamp;
	}

	public float getAllowedSpeed()
	{
		return allowedSpeed;
	}

	public void setAllowedSpeed(float allowedSpeed)
	{
		this.allowedSpeed = allowedSpeed;
	}

	public float getAllowedTach()
	{
		return allowedTach;
	}

	public void setAllowedTach(float allowedTach)
	{
		this.allowedTach = allowedTach;
	}

	public boolean getIsSubmitted()
	{
		return isSubmitted;
	}

	public void setIsSubmitted(boolean isSubmitted)
	{
		this.isSubmitted = isSubmitted;
	}
	
	// Max Tach
	public int getMaxEngRPM() {
		return maxEngRPM;
	}	
	public void setMaxEngRPM(int maxTach) {
		this.maxEngRPM = maxTach;
	}
	
	// Avg Tach
	public int getAvgEngRPM() {
		return avgEngRPM;
	}	
	public void setAvgEngRPM(int avgTach) {
		this.avgEngRPM = avgTach;
	}

	///////////////////////////////////////////////////////////////////////////////////////
	// Custom methods
	///////////////////////////////////////////////////////////////////////////////////////
}
