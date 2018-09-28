package com.jjkeller.kmbapi.proxydata;

import java.util.List;





public class EventDataRecordList extends ProxyBase {

	///////////////////////////////////////////////////////////////////////////////////////
	// private members
	///////////////////////////////////////////////////////////////////////////////////////
	private String driverEmployeeId;
	private String eobrSerialNumber = null;    
    public EventDataRecord[] eventDataRecord;

	///////////////////////////////////////////////////////////////////////////////////////
	// Constructors
	///////////////////////////////////////////////////////////////////////////////////////
    public EventDataRecordList()
    {
    }
    public EventDataRecordList(List<EventDataRecord> list)
    {
        if( list != null ) 
        this.setEventRecords((EventDataRecord[])list.toArray());
    }
    
    
	///////////////////////////////////////////////////////////////////////////////////////
	// public get/set methods
	///////////////////////////////////////////////////////////////////////////////////////
	public String getDriverEmployeeId() {
		return driverEmployeeId;
	}
	public void setDriverEmployeeId(String driverEmployeeId) {
		this.driverEmployeeId = driverEmployeeId;
	}
    public String getEobrSerialNumber() {
		return eobrSerialNumber;
	}
	public void setEobrSerialNumber(String eobrSerialNumber) {
		this.eobrSerialNumber = eobrSerialNumber;
	}
	public EventDataRecord[] getEventRecords() {
		return eventDataRecord;
	}
	public void setEventRecords(EventDataRecord[] eventDataRecord) {
		this.eventDataRecord = eventDataRecord;
	}
	
	
	///////////////////////////////////////////////////////////////////////////////////////
	// Custom methods
	///////////////////////////////////////////////////////////////////////////////////////
}
