package com.jjkeller.kmbapi.proxydata;



import com.jjkeller.kmbapi.enums.InspectionDefectType;

public class VehicleInspectionDefect extends ProxyBase {

	///////////////////////////////////////////////////////////////////////////////////////
	// private members
	///////////////////////////////////////////////////////////////////////////////////////
	private String vehicleInspectionKey;
	private InspectionDefectType inspectionDefectType = new InspectionDefectType(InspectionDefectType.NULL);
	
	///////////////////////////////////////////////////////////////////////////////////////
	// public get/set methods
	///////////////////////////////////////////////////////////////////////////////////////
	public String getVehicleInspectionKey()
	{
		return this.vehicleInspectionKey;
	}
	public void setVehicleInspectionKey(String vehicleInspectionKey)
	{
		this.vehicleInspectionKey = vehicleInspectionKey;
	}
	
	public InspectionDefectType getInspectionDefectType()
	{
		return this.inspectionDefectType;
	}
	public void setInspectionDefectType(InspectionDefectType inspectionDefectType)
	{
		this.inspectionDefectType = inspectionDefectType;
	}
	
	
}
