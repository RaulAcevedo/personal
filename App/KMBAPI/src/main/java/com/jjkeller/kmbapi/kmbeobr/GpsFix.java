package com.jjkeller.kmbapi.kmbeobr;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Date;

public class GpsFix {
	public long timeCode;
	public float latitude;
	public float longitude;
	public float dop; // Dilution of Precision - expected values between 0.0 - 25.4
	public byte heading;
	public float speed;
	public short altitude;
	public int uncert;
	
	public long getTimeCode() {
		return this.timeCode;
	}
	public Date getTimecodeAsDate() {
		return new Date(this.timeCode);
	}
	public void setTimeCode(long time) {
		this.timeCode = time;
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
	
	public float getDOP() {
		return this.dop;
	}
	public void setDOP(float dop) {
		this.dop = dop;
	}
	
	public byte getHeading() {
		return this.heading;
	}
	public void setHeading(byte heading) {
		this.heading = heading;
	}
	
	public float getSpeed() {
		return this.speed;
	}
	public void setSpeed(float speed) {
		this.speed = speed;
	}
	
	public short getAltitude() {
		return this.altitude;
	}
	public void setAltitude(short altitude) {
		this.altitude = altitude;
	}
	
	public int getUncert() {
		return this.uncert;
	}
	public void setUncert(int uncert) {
		this.uncert = uncert;
	}
	
	public static GpsFix FromByteBuffer(ByteBuffer buffer)
	{
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		
    	GpsFix gpsFix = new GpsFix();   	
    	
    	// timestamp stored in TAB as # of milliseconds since 1/1/1970
    	gpsFix.setTimeCode(buffer.getLong());
    	
    	// lat/long stored in TAB as # of milliseconds
    	gpsFix.setLatitude((float)buffer.getInt()/Constants.MILLISECONDS_TO_DEGREES);
    	gpsFix.setLongitude((float)buffer.getInt()/Constants.MILLISECONDS_TO_DEGREES);
    	
    	// DOP stored in TAB as (value * 10)
    	gpsFix.setDOP((float)buffer.get()/10);
    	
    	gpsFix.setHeading(buffer.get());
    	
    	// Speed stored in TAB as (KPH * 100) - convert to MPH
    	gpsFix.setSpeed(((float)buffer.getShort() / 100f) * Constants.KPH_TO_MPH);

    	// Altitude stored in meters
    	gpsFix.setAltitude(buffer.getShort());
    	
    	// Uncertainty - distance traveled since last fix - stored in TAB as meters
    	gpsFix.setUncert(buffer.getShort() & 0xffff);
    	
    	return gpsFix;
	}
	
	public static int RecordLength()
	{
		return 24;
	}
}
