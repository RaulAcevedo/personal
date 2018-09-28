package com.jjkeller.kmbapi.kmbeobr;

import com.jjkeller.kmbapi.common.TabDataConversionUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class VehicleLocation {
	private int eventType;
	private int recordId;
	private float odometer;
	private float speedometer;
	private float tachometer;
	private GpsFix gpsFix;
	
	public int getEventType() {
		return eventType;
	}
	public void setEventType(int eventType) {
		this.eventType = eventType;
	}

	public int getRecordId() {
		return recordId;
	}
	public void setRecordId(int recordId) {
		this.recordId = recordId;
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

	public GpsFix getGpsFix() {
		return gpsFix;
	}
	public void setGpsFix(GpsFix gpsFix) {
		this.gpsFix = gpsFix;
	}

	public static int RecordLength()
	{
		return 37;
	}
	
	public static VehicleLocation FromByteBuffer(ByteBuffer buffer)
	{
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		
		VehicleLocation location = new VehicleLocation();
		location.setEventType(buffer.get());
		location.setRecordId(buffer.getInt());
		location.setOdometer(TabDataConversionUtil.convertOdometerReading(buffer.getInt()));
		location.setSpeedometer(((float)buffer.getShort() / 100f) * Constants.KPH_TO_MPH);
		location.setTachometer(buffer.getShort());
		
		byte[] gpsData = new byte[GpsFix.RecordLength()];
		buffer.get(gpsData);
		ByteBuffer gpsBuffer = ByteBuffer.wrap(gpsData);

		location.setGpsFix(GpsFix.FromByteBuffer(gpsBuffer));
		
		return location;
	}
}
