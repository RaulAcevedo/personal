package com.jjkeller.kmbapi.controller;

import android.content.Context;

import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.jjkeller.kmbapi.R;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.configuration.LocationCodeDictionary;
import com.jjkeller.kmbapi.controller.dataaccess.LocationCodeFacade;
import com.jjkeller.kmbapi.controller.share.ControllerBase;
import com.jjkeller.kmbapi.controller.share.KmbApplicationException;
import com.jjkeller.kmbapi.controller.utility.RESTWebServiceHelper;
import com.jjkeller.kmbapi.proxydata.LocationCode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LocationCodeController extends ControllerBase {

	public LocationCodeController(Context ctx){
		super(ctx);
	}
	
	
    /// <summary>
    /// Answer the current LocationCode mappings previously downloaded from DMO.
    /// This is managed in State data.
    /// </summary>
    public LocationCodeDictionary getLocationCodes()
    {
        LocationCodeDictionary dict = GlobalState.getInstance().getLocationCodeDictionary();
        if (dict == null)
        {
            // try to load the codes from the file
            List<LocationCode> list = this.LoadLocationCodes();
            dict = new LocationCodeDictionary(list);

            // save them to state
            GlobalState.getInstance().setLocationCodeDictionary(dict);
        }
        return dict;
    }
    
    public void setLocationCodes(LocationCodeDictionary value)
    {
	    // add the new codes to state, and persist them to disk
        GlobalState.getInstance().setLocationCodeDictionary(value);
    }
    
	/// <summary>
    /// Download the location code mappings from the DMO server
    /// </summary>
    @SuppressWarnings("unused")
	public void DownloadLocationCodes() throws KmbApplicationException
    {
        ArrayList<LocationCode> locationCodeList = new ArrayList<LocationCode>();

		try
		{
			RESTWebServiceHelper rwsh = new RESTWebServiceHelper(getContext());
			Date changeTimestampUTC = this.getCurrentUser().getCredentials().getLastSubmitTimestampUtc();
			locationCodeList = rwsh.DownloadLocationCodes(changeTimestampUTC);
		}
		catch (JsonSyntaxException jse)
		{
			this.HandleExceptionAndThrow(jse, this.getContext().getString(R.string.downloadlocationcodes), this.getContext().getString(R.string.exception_webservicecommerror), this.getContext().getString(R.string.exception_serviceunavailable));
		}
		catch (JsonParseException jpe)
		{
			// when connected to a network, but unable to get to webservice "e" is null
			if (jpe == null)
				jpe = new JsonParseException(JsonParseException.class.getName());
			this.HandleExceptionAndThrow(jpe, this.getContext().getString(R.string.downloadlocationcodes), this.getContext().getString(R.string.exception_webservicecommerror), this.getContext().getString(R.string.exception_serviceunavailable));
		}
		catch (IOException ioe)
		{
			this.HandleExceptionAndThrow(ioe, this.getContext().getString(R.string.downloadlocationcodes), this.getContext().getString(R.string.exception_webservicecommerror), this.getContext().getString(R.string.exception_serviceunavailable));
		}

        // persist the codes
        this.SaveLocationCodes(locationCodeList);
        
        LocationCodeDictionary dict = new LocationCodeDictionary(locationCodeList);
        setLocationCodes(dict);        
    }
    
    /// <summary>
    /// Load the location codes from the serialized data file
    /// </summary>
    private List<LocationCode> LoadLocationCodes()
    {
        LocationCodeFacade facade = new LocationCodeFacade(this.getContext());
        List<LocationCode> list = facade.FetchForUser();
        return list;
    }

    /// <summary>
    /// Save the location code list to the serialized data file
    /// </summary>
    private void SaveLocationCodes(List<LocationCode> list)
    {
        LocationCodeFacade facade = new LocationCodeFacade(this.getContext());
        facade.SaveToUser(list);
    }
    
    /// <summary>
    /// Answer if there are location codes available.
    /// This will be used to enable UI functions for entering the codes.
    /// </summary>
    public boolean getAreLocationCodesAvailable()
    {
    	LocationCodeDictionary dict = this.getLocationCodes();
    	return !dict.IsEmpty();
    }
    
}
