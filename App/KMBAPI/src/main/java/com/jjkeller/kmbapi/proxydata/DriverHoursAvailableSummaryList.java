package com.jjkeller.kmbapi.proxydata;



public class DriverHoursAvailableSummaryList extends ProxyBase {

	///////////////////////////////////////////////////////////////////////////////////////
	// private members
	///////////////////////////////////////////////////////////////////////////////////////
	private DriverHoursAvailableSummary[] hoursSummary = null;

	///////////////////////////////////////////////////////////////////////////////////////
	// public get/set methods
	///////////////////////////////////////////////////////////////////////////////////////
    public DriverHoursAvailableSummary[] getDriverHoursAvailableSummaryList()
    {
    	return hoursSummary;
    }
    public void setDriverHoursAvailableSummaryList(DriverHoursAvailableSummary[] hoursSummary)
    {
    	this.hoursSummary = hoursSummary;
    }
    
}
