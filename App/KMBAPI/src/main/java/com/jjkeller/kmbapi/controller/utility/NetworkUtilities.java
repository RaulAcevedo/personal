package com.jjkeller.kmbapi.controller.utility;

import android.content.Context;
import android.net.ConnectivityManager;
import android.util.Log;

import java.util.Date;

public class NetworkUtilities
{
	public static boolean VerifyNetworkConnection(Context ctx)
	{
		ConnectivityManager conMgr = (ConnectivityManager)ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
	
		return ( conMgr.getActiveNetworkInfo() != null &&
				 conMgr.getActiveNetworkInfo().isAvailable() &&
				 conMgr.getActiveNetworkInfo().isConnected() );
	}
	
	public static boolean VerifyWebServiceConnection(Context ctx)
	{
		ConnectivityManager conMgr = (ConnectivityManager)ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		
		boolean isAvailable = ( conMgr.getActiveNetworkInfo() != null &&
				 conMgr.getActiveNetworkInfo().isAvailable() &&
				 conMgr.getActiveNetworkInfo().isConnected() );
		
		// if network connection exists, attempt to execute
		// GetDMOTime web service method to verify web service is available.
		if (isAvailable)
		{
			try
			{			
				RESTWebServiceHelper rwsh = new RESTWebServiceHelper(ctx);
				Date dmoTime = rwsh.GetDMOTime();
				if (dmoTime != null)
					isAvailable = true;
				else
					isAvailable = false;
			}
			catch (Exception ex)
			{
				isAvailable = false;			
			}
		}

		return isAvailable;		
	}

	/**
	 * Tests a connection to the web services and checks if it is redirected to
	 * a secondary authentication page, like a Wi-Fi network sign on page. If
	 * there does seem to be a secondary authentication page, it returns the URL
	 * to send the user to. Otherwise, it returns null.
	 * 
	 * @return The secondary authentication URL, or null
	 */
	public static String SecondaryAuthenticationUrl(Context ctx)
	{
		try
		{
			return new RESTWebServiceHelper(ctx).SecondaryAuthenticationUrl();
		}
		catch (Exception ex)
		{
			Log.d("UnhandledCatch", ex.getMessage(), ex);
			return null;
		}
	}
}
