package com.jjkeller.kmbapi.controller;

import android.content.Context;

import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.jjkeller.kmbapi.R;
import com.jjkeller.kmbapi.controller.dataaccess.LogCheckerComplianceDatesFacade;
import com.jjkeller.kmbapi.controller.share.ControllerBase;
import com.jjkeller.kmbapi.controller.share.KmbApplicationException;
import com.jjkeller.kmbapi.controller.utility.RESTWebServiceHelper;
import com.jjkeller.kmbapi.proxydata.LogCheckerComplianceDates;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LogCheckerComplianceDatesController extends ControllerBase implements ILogCheckerComplianceDatesController {

	public LogCheckerComplianceDatesController(Context ctx){
		super(ctx);
	}
    
	/// <summary>
    /// Download the log checker compliance dates from the DMO server
    /// </summary>
    @SuppressWarnings("unused")
	public void DownloadLogCheckerComplianceDates() throws KmbApplicationException
    {
    	ArrayList<LogCheckerComplianceDates> complianceDates = new ArrayList<LogCheckerComplianceDates>();
        LogCheckerComplianceDates recentComplianceDate = LoadLogCheckerComplianceDates();         
        
        Date recentDate = new Date(0L);
        
        if(recentComplianceDate != null)
        {
        	recentDate = recentComplianceDate.getComplianceDate();
        	if (recentComplianceDate.getComplianceEndDate() != null)
        		recentDate = recentComplianceDate.getComplianceEndDate();
        }
        
		try
		{
			RESTWebServiceHelper rwsh = new RESTWebServiceHelper(getContext());
			complianceDates = rwsh.DownloadLogCheckerComplianceDates(recentDate);
		}
		catch (JsonSyntaxException jse)
		{
			this.HandleExceptionAndThrow(jse, this.getContext().getString(R.string.downloadlogcheckercompliancedates), this.getContext().getString(R.string.exception_webservicecommerror), this.getContext().getString(R.string.exception_serviceunavailable));
		}
		catch (JsonParseException jpe)
		{
			// when connected to a network, but unable to get to webservice "e" is null
			if (jpe == null)
				jpe = new JsonParseException(JsonParseException.class.getName());
			this.HandleExceptionAndThrow(jpe, this.getContext().getString(R.string.downloadlogcheckercompliancedates), this.getContext().getString(R.string.exception_webservicecommerror), this.getContext().getString(R.string.exception_serviceunavailable));
		}
		catch (IOException ioe)
		{
			this.HandleExceptionAndThrow(ioe, this.getContext().getString(R.string.downloadlogcheckercompliancedates), this.getContext().getString(R.string.exception_webservicecommerror), this.getContext().getString(R.string.exception_serviceunavailable));
		}

        // persist the codes
        this.SaveLogCheckerComplianceDates(complianceDates);
           
    }
    

    private LogCheckerComplianceDates LoadLogCheckerComplianceDates()
    {
        LogCheckerComplianceDatesFacade facade = new LogCheckerComplianceDatesFacade(this.getContext());
        LogCheckerComplianceDates compDate = facade.FetchMostRecentComplianceDate();
        return compDate;
    }


    private void SaveLogCheckerComplianceDates(List<LogCheckerComplianceDates> complianceDates)
    {
        LogCheckerComplianceDatesFacade facade = new LogCheckerComplianceDatesFacade(this.getContext());
        facade.Save(complianceDates);
    }
    
    
    private LogCheckerComplianceDates GetLogCheckerComplianceDatesByEnum(int complianceDatesType)
    {
        LogCheckerComplianceDatesFacade facade = new LogCheckerComplianceDatesFacade(this.getContext());
        LogCheckerComplianceDates compDate = facade.FetchLogCheckerComplianceDatesByEnum(complianceDatesType);
        return compDate;
    }

    public boolean IsLogCheckerComplianceDateActive(int complianceDatesType, Date dateToCheck, boolean dateRangeDefinesActivePeriod)
    {
    	// NOTE:  New ComplianceDate for Dec. 2014 Suspension of 34 hour reset provisions defines a date range in
    	// which the provisions are inactive.  Previously, the ComplianceDates were used to identify when 
    	// some rule was active.  So need to return isActive value based on whether the complianceDateType
    	// defines an active or inactive period.
    	boolean isActive = false;    	
    	LogCheckerComplianceDates compliance = GetLogCheckerComplianceDatesByEnum(complianceDatesType);
    	if(compliance == null || compliance.getComplianceDate() == null){
    		isActive = false;
    	}
    	else if (compliance.getComplianceDate().compareTo(dateToCheck) < 0)
    	{    	
    		if (compliance.getComplianceEndDate() == null)
    			isActive = dateRangeDefinesActivePeriod ? true : false;
    		
    		else if (compliance.getComplianceEndDate().compareTo(dateToCheck) >= 0)
    			isActive = dateRangeDefinesActivePeriod ? true : false;
    		else
    			isActive = dateRangeDefinesActivePeriod ? false : true;
    	}
    	else
    	{
    		isActive = dateRangeDefinesActivePeriod ? false : true;
    	}
    	    	    	
		return isActive;    	
    }   
}
