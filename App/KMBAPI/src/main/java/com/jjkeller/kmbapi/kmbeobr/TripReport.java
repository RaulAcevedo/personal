package com.jjkeller.kmbapi.kmbeobr;

import com.jjkeller.kmbapi.configuration.GlobalState;

import java.util.Date;


public class TripReport {

    private static final float MIN_GPS_LATITUDE = -90.0F;
    private static final float MAX_GPS_LATITUDE = 90.0F;
    private static final float MIN_GPS_LONGITUDE = -180.0F;
    private static final float MAX_GPS_LONGITUDE = 180.0F;
    
	private int recordId;
	private long timecode;
	private byte ignition;
	private float odometer;
	private int runtime;
	private int tripSecs;
	private int tripNum;
	private float tripDist;
	private float latitude;
	private float longitude;
	private short fixUncert;
	private int avgHeading;
	private float avgSpeed;
	private float maxSpeed;
	private int idleSecs;
	private int halts;
	private float tripFuel;
	private byte idleFuelPcnt;
	private byte ptoFuelPcnt;
	private byte ptoActivePcnt;
	private int resets;
	private float celleration;
	private int driverid;
	private long dataTimecode;
	private int maxEngRPM;
	private int avgEngRPM;	

	public int getRecordId() {
		return this.recordId;
	}
	public void setRecordId(int recordid) {
		this.recordId = recordid;
	}

	public long getTimecode() {
		return this.timecode;
	}
	public Date getTimecodeAsDate() {
		return new Date(this.timecode);
	}
	public void setTimecode(long timecode) {
		this.timecode = timecode;
	}
	
	public byte getIgnition() {
		return this.ignition;
	}
	public void setIgnition(byte ignition) {
		this.ignition = ignition;
	}
	
	public float getOdometer() {
		return this.odometer;
	}
	public void setOdometer(float odometer) {
		this.odometer = odometer;
	}

	public int getRuntime() {
		return this.runtime;
	}
	public void setRuntime(int runtime) {
		this.runtime  = runtime;
	}
	
	public int getTripSecs() {
		return this.tripSecs;
	}
	public void setTripSecs(int tripSecs) {
		this.tripSecs = tripSecs;
	}
	
	public int getTripNum() {
		return this.tripNum;
	}
	public void setTripNum(int tripNum) {
		this.tripNum = tripNum;
	}
	
	public float getTripDist() {
		return this.tripDist;
	}
	public void setTripDist(float tripDist) {
		this.tripDist = tripDist;
	}

	public float getLatitude() {
		return this.latitude;
	}
	public void setLatitude(float latitude) {
		this.latitude = latitude;
	}
	
	public float getLongitude() {
		return this.longitude;
	}
	public void setLongitude(float longitude) {
		this.longitude = longitude;
	}
	
	public short getFixUncert() {
		return this.fixUncert;
	}
	public void setFixUncert(short fixUncert) {
		this.fixUncert = fixUncert;
	}
		
	public int getAvgHeading() {
		return this.avgHeading;
	}
	public void setAvgHeading(int avgHeading) {
		this.avgHeading = avgHeading;
	}
		
	public float getAvgSpeed() {
		return this.avgSpeed;
	}
	public void setAvgSpeed(float avgSpeed) {
		this.avgSpeed = avgSpeed;
	}
		
	public float getMaxSpeed() {
		return this.maxSpeed;
	}
	public void setMaxSpeed(float maxSpeed) {
		this.maxSpeed = maxSpeed;
	}
		
	public int getIdleSecs() {
		return this.idleSecs;
	}
	public void setIdleSecs(int idleSecs) {
		this.idleSecs = idleSecs;
	}

	public int getHalts() {
		return this.halts;
	}
	public void setHalts(int halts) {
		this.halts = halts;
	}

	public float getTripFuel() {
		return this.tripFuel;
	}
	public void setTripFuel(float tripFuel) {
		this.tripFuel = tripFuel;
	}

	public byte getIdleFuelPcnt() {
		return this.idleFuelPcnt;
	}
	public void setIdleFuelPcnt(byte idleFuelPcnt) {
		this.idleFuelPcnt = idleFuelPcnt;
	}
		
	public byte getPtoFuelPcnt() {
		return this.ptoFuelPcnt;
	}
	public void setPtoFuelPcnt(byte ptoFuelPcnt) {
		this.ptoFuelPcnt = ptoFuelPcnt;
	}

	public byte getPtoActivePcnt() {
		return this.ptoActivePcnt;
	}
	public void setPtoActivePcnt(byte ptoActivePcnt) {
		this.ptoActivePcnt = ptoActivePcnt;
	}

	public int getResets() {
		return this.resets;
	}
	public void setResets(int resets) {
		this.resets = resets;
	}

	public float getCelleration() {
		return this.celleration;
	}
	public void setCelleration(float celleration) {
		this.celleration = celleration;
	}
	
	public int getDriverId() {
		return this.driverid;
	}
	public void setDriverId(int value) {
		this.driverid = value;
	}
	
	public long getDataTimecode() {
		return this.dataTimecode;
	}
	public Date getDataTimecodeAsDate() {
		return new Date(this.dataTimecode);
	}
	public void setDataTimecode(long dataTimecode) {
		this.dataTimecode = dataTimecode;
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

	/// <summary>
    /// Answer if the GPS location info in the status record is valid.
    /// Valid GPS data means that there is no failure reported by the GPS module,
    /// a valid GPS timestamp and good satellite fix.
    /// </summary>
    /// <returns></returns>
    public boolean IsGpsLocationValid()
    {
        boolean isvalid = false;

        // validate that the LAT/LONG values are within the acceptable range
        float lat = this.getLatitude();
        float lng = this.getLongitude();
        if (lat > MIN_GPS_LATITUDE &&
            lat < MAX_GPS_LATITUDE &&
            lng > MIN_GPS_LONGITUDE &&
            lng < MAX_GPS_LONGITUDE)
        {
            isvalid = true;
        }
        else
        {
            isvalid = false;
        }

        if(!isvalid)
        	GlobalState.getInstance().setLastGPSLocation(null);
        
        return isvalid;
    }
}
