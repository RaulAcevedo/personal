package com.jjkeller.kmbapi.proxydata;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;





public class FailureReportList extends ProxyBase {
    @Override
    public String toString() {
        return "FailureReportList{" +
                "failureReports=" + Arrays.toString(failureReports) +
                '}';
    }

    ///////////////////////////////////////////////////////////////////////////////////////
	// private members
	///////////////////////////////////////////////////////////////////////////////////////
	private FailureReport[] failureReports = null;

	///////////////////////////////////////////////////////////////////////////////////////
	// public get/set methods
	///////////////////////////////////////////////////////////////////////////////////////
	public FailureReport[] getFailureReportList()
    {
    	return failureReports;
    }
    public void setFailureReportList(FailureReport[] failureReports)
    {
    	this.failureReports = failureReports;
    }
  
	///////////////////////////////////////////////////////////////////////////////////////
	// Custom methods
	///////////////////////////////////////////////////////////////////////////////////////
	public void Add(FailureReport failure)
	{
    	if (this.failureReports == null)
    	{
    		this.failureReports = new FailureReport[1];
    		this.failureReports[0] = failure;
    	}
    	else
    	{
            List<FailureReport> list = new LinkedList<FailureReport>(Arrays.asList(this.failureReports));
            list.add(failure);
            
            this.failureReports = list.toArray(new FailureReport[list.size()]);
    	}
	}
	
    public boolean IsEmpty()
    {
    	return this.failureReports == null || this.failureReports.length == 0;
    }
}
