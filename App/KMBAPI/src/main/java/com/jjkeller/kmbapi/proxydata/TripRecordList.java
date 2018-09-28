package com.jjkeller.kmbapi.proxydata;

import java.util.List;

public class TripRecordList extends ProxyBase {

	///////////////////////////////////////////////////////////////////////////////////////
	// private members
	///////////////////////////////////////////////////////////////////////////////////////
    private String eobrSerialNumber = null;
    private String eobrTractorNumber = null;
    private String driverEmployeeId;
    public TripRecord[] tripRecords;

	///////////////////////////////////////////////////////////////////////////////////////
	// Constructors
	///////////////////////////////////////////////////////////////////////////////////////
    public TripRecordList()
    {
    }
    public TripRecordList(List<TripRecord> list)
    {
        if( list != null ) 
        this.setTripRecords((TripRecord[])list.toArray());
    }
    
    
	///////////////////////////////////////////////////////////////////////////////////////
	// public get/set methods
	///////////////////////////////////////////////////////////////////////////////////////
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
	public void setDriverEmployeeId(String employeeId) {
		this.driverEmployeeId = employeeId;
	}
	public TripRecord[] getTripRecords() {
		return tripRecords;
	}
	public void setTripRecords(TripRecord[] tripRecords) {
		this.tripRecords = tripRecords;
	}
	
	
	///////////////////////////////////////////////////////////////////////////////////////
	// Custom methods
	///////////////////////////////////////////////////////////////////////////////////////
}
