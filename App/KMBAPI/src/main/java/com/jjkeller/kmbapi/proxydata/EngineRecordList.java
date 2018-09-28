package com.jjkeller.kmbapi.proxydata;

import java.util.List;





public class EngineRecordList extends ProxyBase {

	///////////////////////////////////////////////////////////////////////////////////////
	// private members
	///////////////////////////////////////////////////////////////////////////////////////
    private String eobrSerialNumber = null;
    private String eobrTractorNumber = null;
    private String driverEmployeeId;
    public EngineRecord[] engineRecords;

	///////////////////////////////////////////////////////////////////////////////////////
	// Constructors
	///////////////////////////////////////////////////////////////////////////////////////
    public EngineRecordList()
    {
    }
    public EngineRecordList(List<EngineRecord> list)
    {
        if( list != null ) 
        this.setEngineRecords((EngineRecord[])list.toArray());
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
	public void setDriverEmployeeId(String driverEmployeeId) {
		this.driverEmployeeId = driverEmployeeId;
	}
	public EngineRecord[] getEngineRecords() {
		return engineRecords;
	}
	public void setEngineRecords(EngineRecord[] engineRecords) {
		this.engineRecords = engineRecords;
	}
	
	
	///////////////////////////////////////////////////////////////////////////////////////
	// Custom methods
	///////////////////////////////////////////////////////////////////////////////////////
}
