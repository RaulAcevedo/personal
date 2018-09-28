package com.jjkeller.kmbapi.kmbeobr;

import java.util.Date;
import java.util.List;

public class EventRecord {

	private int recordId;
	private long timecode;
	private int eventType = -1;
	private int eventData;
	private int driverId;
	private int eobrId;
	private TripReport tripReportData;
	private StatusRecord statusRecordData;

	public int getGeotabHOSDataKey() {
		return geotabHOSDataKey;
	}

	public void setGeotabHOSDataKey(int geotabHOSDataKey) {
		this.geotabHOSDataKey = geotabHOSDataKey;
	}

	private int geotabHOSDataKey;

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
	
	public int getEventType() {
		return this.eventType;
	}
	public void setEventType(int eventType) {
		this.eventType  = eventType;
	}
	
	public int getEventData() {
		return this.eventData;
	}
	public void setEventData(int eventData) {
		this.eventData = eventData;
	}
	
	public int getDriverId() {
		return driverId;
	}
	public void setDriverId(int driverId) {
		this.driverId = driverId;
	}

	public int getEobrId() {
		return eobrId;
	}
	public void setEobrId(int eobrId) {
		this.eobrId = eobrId;
	}
	
	public void setTripReportData(TripReport tripReportData) {
		this.tripReportData = tripReportData;
	}
	public TripReport getTripReportData() {
		return tripReportData;
	}
	
	public void setStatusRecordData(StatusRecord statusRecord) {
		this.statusRecordData = statusRecord;
	}
	public StatusRecord getStatusRecordData() {
		return statusRecordData;
	}


    /* Used by Gen2 to handle notifying UI of motion change for DOT clocks when there isn't an event */
	private boolean _isVehicleInMotion = false;
	public boolean getIsVehicleInMotion()
	{
		return _isVehicleInMotion;
	}
    public void setIsVehicleInMotion(boolean isVehicleInMotion)
    {
		_isVehicleInMotion = isVehicleInMotion;
	}

	public boolean isDrivingEvent()
	{
		if(eventType == EventTypeEnum.DRIVESTART || eventType == EventTypeEnum.DRIVEEND)
			return true;
		else return false;
	}

	public boolean hasDataFlag(List<IDataFlag> flags) {
		boolean answer = false;
		for (IDataFlag f : flags) {
			if (hasDataFlag(f)) {
				answer = true;
				break;
			}
		}
		return answer;
	}

	public boolean hasDataFlag(IDataFlag flag) {
		return hasDataFlag(flag.getIndex());
	}

	public boolean hasDataFlag(int index) {
		int shifted = 1 << index;

		return (getEventData() & shifted) == shifted;
	}
}
