package com.jjkeller.kmbapi.controller;

import android.content.Context;

import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.jjkeller.kmbapi.R;
import com.jjkeller.kmbapi.controller.dataaccess.LogRemarkItemFacade;
import com.jjkeller.kmbapi.controller.share.ControllerBase;
import com.jjkeller.kmbapi.controller.share.KmbApplicationException;
import com.jjkeller.kmbapi.controller.utility.RESTWebServiceHelper;
import com.jjkeller.kmbapi.proxydata.LogRemarkItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LogRemarksController extends ControllerBase {

	public LogRemarksController(Context ctx){
		super(ctx);
	}
	
	/// <summary>
    /// Download the log remark items from the DMO server
    /// </summary>
    @SuppressWarnings("unused")
	public void DownloadLogRemarkItems() throws KmbApplicationException
    {
    	List<LogRemarkItem> logRemarkItems = new ArrayList<LogRemarkItem>();      
        
        Date changeDate = new Date(0L);
        
    	LogRemarkItemFacade facade = new LogRemarkItemFacade(this.getContext());
    	Date logRemarkChangeDate = facade.GetMostRecentChangeDate();
    	if(logRemarkChangeDate != null)
    		changeDate = logRemarkChangeDate;
    	
		try
		{
			RESTWebServiceHelper rwsh = new RESTWebServiceHelper(getContext());
			logRemarkItems = rwsh.DownloadLogRemarkItems(changeDate);
		}
		catch (JsonSyntaxException jse)
		{
			this.HandleExceptionAndThrow(jse, this.getContext().getString(R.string.downloadlogremarkitems), this.getContext().getString(R.string.exception_webservicecommerror), this.getContext().getString(R.string.exception_serviceunavailable));
		}
		catch (JsonParseException jpe)
		{
			// when connected to a network, but unable to get to webservice "e" is null
			if (jpe == null)
				jpe = new JsonParseException(JsonParseException.class.getName());
			this.HandleExceptionAndThrow(jpe, this.getContext().getString(R.string.downloadlogremarkitems), this.getContext().getString(R.string.exception_webservicecommerror), this.getContext().getString(R.string.exception_serviceunavailable));
		}
		catch (IOException ioe)
		{
			this.HandleExceptionAndThrow(ioe, this.getContext().getString(R.string.downloadlogremarkitems), this.getContext().getString(R.string.exception_webservicecommerror), this.getContext().getString(R.string.exception_serviceunavailable));
		}

        // persist the items
    	if(logRemarkItems != null && logRemarkItems.size() > 0)
    		this.SaveLogRemarkItems(logRemarkItems);           
    }
    
    public List<LogRemarkItem> FetchAll(){
    	LogRemarkItemFacade facade = new LogRemarkItemFacade(this.getContext());
    	return facade.FetchAllActive();    	
    }
    
    private void SaveLogRemarkItems(List<LogRemarkItem> logRemarkItems)
    {    	
    	LogRemarkItemFacade facade = new LogRemarkItemFacade(this.getContext());
    	facade.Save(logRemarkItems);
    	
    	//List<LogRemarkItem> list = facade.FetchAllActive();
    }
}
