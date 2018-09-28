package com.jjkeller.kmbapi.controller;

import android.content.Context;

import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.jjkeller.kmbapi.R;
import com.jjkeller.kmbapi.controller.share.ControllerBase;
import com.jjkeller.kmbapi.controller.share.KmbApplicationException;
import com.jjkeller.kmbapi.controller.utility.RESTWebServiceHelper;
import com.jjkeller.kmbapi.proxydata.ConditionalFirmwareUpgrade;

import java.io.IOException;

public class FirmwareUpgradeController extends ControllerBase {

	public FirmwareUpgradeController(Context ctx) {
		super(ctx);
	}

    /// <summary>
    /// Record the conditional firmware upgrade
    /// </summary>
    /// <returns></returns>
	public void RecordConditionalFirmwareUpgrade(ConditionalFirmwareUpgrade cfu) throws KmbApplicationException
	{
		try
		{
			RESTWebServiceHelper rwsh = new RESTWebServiceHelper(getContext());
			rwsh.RecordConditionalFirmwareUpgrade(cfu);
		}
		catch (JsonSyntaxException jse)
		{

			this.HandleExceptionAndThrow(jse, this.getContext().getString(R.string.recordConditionalFirmwareUpgrade), this.getContext().getString(R.string.exception_webservicecommerror), this.getContext().getString(R.string.exception_serviceunavailable));
		}
		catch (JsonParseException e)
		{
			// when connected to a network, but unable to get to webservice "e" is null at times
			if (e == null)
				e = new JsonParseException(JsonParseException.class.getName());
			this.HandleExceptionAndThrow(e, this.getContext().getString(R.string.recordConditionalFirmwareUpgrade), this.getContext().getString(R.string.exception_webservicecommerror), this.getContext().getString(R.string.exception_serviceunavailable));
		}
		catch (IOException ioe)
		{
			this.HandleExceptionAndThrow(ioe, this.getContext().getString(R.string.recordConditionalFirmwareUpgrade), this.getContext().getString(R.string.exception_webservicecommerror), this.getContext().getString(R.string.exception_serviceunavailable));
		}
	}
}
