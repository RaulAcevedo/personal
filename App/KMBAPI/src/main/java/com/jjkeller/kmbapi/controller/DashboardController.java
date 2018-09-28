package com.jjkeller.kmbapi.controller;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.jjkeller.kmbapi.R;
import com.jjkeller.kmbapi.common.TimeKeeper;
import com.jjkeller.kmbapi.controller.EOBR.EobrReader;
import com.jjkeller.kmbapi.controller.share.ControllerBase;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.kmbeobr.StatusRecord;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

public class DashboardController extends ControllerBase{

	private static final String DASHBOARD = "Dashboard";
	private static final String CURRENTSPEED = "Current Speed";
	private static final String CURRENTODOMETER = "Current Odometer";
	private static final String CURRENTTACH = "Current Tach";
	private static final String LASTDATETIME = "Last DateTime";
	private static final String ENGINEON = "Engine On";
	private static final String UNIQUEID = "UniqueId";
	private static final String SERIALNO = "Serial Number";
	private static final String ENGINEOFFCOMMSTIMEOUT = "Sleep Mode Minutes";
	private static final String DATACOLLECTIONRATE = "Data Collection Rate";
	private static final String COMPANYPASSKEY = "Company Passkey";
	private static final String BUSTYPE = "Bus Type";
	private static final String ODOMETEROFFSET = "Odometer Offset";
	private static final String ODOMETERMULTIPLIER = "Odometer Multiplier";

	private static boolean _externalStorageAvailable;
	private static boolean _externalStorageWriteable;

	public DashboardController(Context ctx)
	{		
		super(ctx);
	}
	
	public void CheckExternalStorageState()
	{
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state))
		{
			_externalStorageAvailable = _externalStorageWriteable = true;
		}
		else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state))
		{
			_externalStorageAvailable = true;
			_externalStorageWriteable = false;
		}
		else
		{
			_externalStorageAvailable = _externalStorageWriteable = false;			
		}
	}
	
	public File getExternalDashboardDir()
	{
		File sdCard = Environment.getExternalStorageDirectory();
		File dir = new File(String.format("%s/%s", sdCard.getAbsolutePath(), DASHBOARD));
		
		if (!dir.exists())
			dir.mkdir();
		
		return dir;
	}

	public void writeSDFile(Bundle curValues, boolean updateOdometer)
	{
		float currentOdometer = curValues.getFloat(CURRENTODOMETER);
		Date currentDateTime = new Date(curValues.getLong(LASTDATETIME));
		
		if (_externalStorageAvailable && _externalStorageWriteable)
		{
			// get dashboard file (sdCard\dashboard\dashboard
			// if file exists, delete it and re-write with current values
    		File dashboardFile = new File(this.getExternalDashboardDir(), DASHBOARD);
    		if (dashboardFile.exists())
    		{
    			if (updateOdometer)
    			{
    				currentDateTime = TimeKeeper.getInstance().now();
    				currentOdometer = calcOdometer(curValues, currentDateTime);
    			}
    			
    			dashboardFile.delete();
    		}
    		
    		try
    		{
    			int speed = curValues.getInt(CURRENTSPEED);
    			
	    		BufferedWriter wrtr = new BufferedWriter(new FileWriter(dashboardFile));	    		
	    		wrtr.write(String.format("%s|%s\r\n", UNIQUEID, curValues.getString(UNIQUEID)));
	    		wrtr.write(String.format("%s|%s\r\n", SERIALNO, curValues.getString(SERIALNO)));
	    		wrtr.write(String.format("%s|%s\r\n", ENGINEON, curValues.getBoolean(ENGINEON)));
	    		wrtr.write(String.format("%s|%s\r\n", CURRENTSPEED, speed));
	    		
	    		// if current odometer isn't defined, create a hardcoded value
	    		if (currentOdometer == 0F)
	    			currentOdometer = 156328.4F;
	    		
	    		wrtr.write(String.format("%s|%s\r\n", CURRENTODOMETER, currentOdometer));
	    		wrtr.write(String.format("%s|%s\r\n", LASTDATETIME, currentDateTime.getTime()));
	    		
	    		if (speed == 0)
	    		{
	    			if (curValues.getBoolean(ENGINEON))
		    			wrtr.write(String.format("%s|%s\r\n", CURRENTTACH, "700"));
	    			else
	    				wrtr.write(String.format("%s|%s\r\n", CURRENTTACH, "0"));
	    		}
	    		else if (speed < 25)
	    			wrtr.write(String.format("%s|%s\r\n", CURRENTTACH, "1100"));
	    		else if (speed < 50)
	    			wrtr.write(String.format("%s|%s\r\n", CURRENTTACH, "1500"));
	    		else
	    			wrtr.write(String.format("%s|%s\r\n", CURRENTTACH, "1700"));   
	    		
	    		wrtr.write(String.format("%s|%s\r\n", ENGINEOFFCOMMSTIMEOUT, curValues.getInt(ENGINEOFFCOMMSTIMEOUT)));
	    		wrtr.write(String.format("%s|%s\r\n", DATACOLLECTIONRATE, curValues.getInt(DATACOLLECTIONRATE)));
	    		wrtr.write(String.format("%s|%s\r\n", COMPANYPASSKEY, curValues.getString(COMPANYPASSKEY)));
	    		wrtr.write(String.format("%s|%s\r\n", BUSTYPE, curValues.getByte(BUSTYPE)));
	    		wrtr.write(String.format("%s|%s\r\n", ODOMETEROFFSET, curValues.getInt(ODOMETEROFFSET)));
	    		wrtr.write(String.format("%s|%s\r\n", ODOMETERMULTIPLIER, curValues.getInt(ODOMETERMULTIPLIER)));
	    		wrtr.close();
    		}
    		catch(IOException ioe){
            	Log.e("UnhandledCatch", ioe.getMessage() + ": " + Log.getStackTraceString(ioe));
    		}
		}
	}

	public Bundle getCurrentValues()
	{
		Bundle retBundle = new Bundle();
		
		this.CheckExternalStorageState();
		if (_externalStorageAvailable && _externalStorageWriteable)
		{
    		File dashboardFile = new File(this.getExternalDashboardDir(), DASHBOARD);
    		if (!dashboardFile.exists())    			
    		{
    			// create default file - set date to 18 hours in past
    			Date newDate = DateUtility.getCurrentDateTimeUTC();
    			//newDate = DateUtility.AddHours(newDate, -12);
    			
    			retBundle.putString(SERIALNO, "609037M070015");
    			retBundle.putString(UNIQUEID, "609037M070015");
    			retBundle.putLong(LASTDATETIME, newDate.getTime());
    			retBundle.putBoolean(ENGINEON, false);
    			retBundle.putInt(CURRENTSPEED, 0);
    			retBundle.putFloat(CURRENTODOMETER, 156328.4F);
    			retBundle.putFloat(CURRENTTACH, 0);
    			retBundle.putInt(ENGINEOFFCOMMSTIMEOUT, -1);
    			retBundle.putInt(DATACOLLECTIONRATE, 1);
    			retBundle.putString(COMPANYPASSKEY, "abc");
    			retBundle.putByte(BUSTYPE, (byte)1);
    			retBundle.putInt(ODOMETEROFFSET, 0);
    			retBundle.putInt(ODOMETERMULTIPLIER, 0);
    			
    			this.writeSDFile(retBundle, false);
    		}
    		
			try
			{
				retBundle.clear();
				
				BufferedReader rdr = null;
				boolean done = false;
				String line = null;
				for (int i = 0; i<10; i++)
				{
					rdr = new BufferedReader(new FileReader(dashboardFile));
					line = rdr.readLine();
					
					if (line != null)
						break;
				}
				
				done = (line == null);
				
				while (!done)
				{
					String[] data = line.split("\\|");
		
					if (data.length == 2)
					{
						if (data[0].equalsIgnoreCase(UNIQUEID))
							retBundle.putString(UNIQUEID, String.valueOf(data[1]));

						if (data[0].equalsIgnoreCase(SERIALNO))
							retBundle.putString(SERIALNO, String.valueOf(data[1]));

						if (data[0].equalsIgnoreCase(ENGINEON))
							retBundle.putBoolean(ENGINEON, Boolean.valueOf(data[1]));
							
						if (data[0].equalsIgnoreCase(CURRENTSPEED))
							retBundle.putInt(CURRENTSPEED, Integer.valueOf(data[1]));
							
						if (data[0].equalsIgnoreCase(CURRENTODOMETER))
							retBundle.putFloat(CURRENTODOMETER, Float.valueOf(data[1]));
							
						if (data[0].equalsIgnoreCase(LASTDATETIME))
							retBundle.putLong(LASTDATETIME, Long.valueOf(data[1]));
							
						if (data[0].equalsIgnoreCase(CURRENTTACH))
							retBundle.putFloat(CURRENTTACH, Float.valueOf(data[1]));
						
						if (data[0].equalsIgnoreCase(ENGINEOFFCOMMSTIMEOUT))
							retBundle.putInt(ENGINEOFFCOMMSTIMEOUT, Integer.valueOf(data[1]));
						
						if (data[0].equalsIgnoreCase(DATACOLLECTIONRATE))
							retBundle.putInt(DATACOLLECTIONRATE, Integer.valueOf(data[1]));
						
						if (data[0].equalsIgnoreCase(BUSTYPE))
							retBundle.putByte(BUSTYPE, Byte.valueOf(data[1]));
						
						if (data[0].equalsIgnoreCase(COMPANYPASSKEY))
							retBundle.putString(COMPANYPASSKEY, String.valueOf(data[1]));
						
						if (data[0].equalsIgnoreCase(ODOMETEROFFSET))
							retBundle.putInt(ODOMETEROFFSET, Integer.valueOf(data[1]));

						if (data[0].equalsIgnoreCase(ODOMETERMULTIPLIER))
							retBundle.putInt(ODOMETERMULTIPLIER, Integer.valueOf(data[1]));
					}
					
					line = rdr.readLine();
					if (line == null)
						done = true;
				}
				
				boolean updateDefaultValues = false;
				if (!retBundle.containsKey(UNIQUEID))
				{
					retBundle.putString(UNIQUEID, "70015");
					updateDefaultValues = true;
				}
				
				if (!retBundle.containsKey(SERIALNO))
				{
					retBundle.putString(SERIALNO, "609037M070015");
					updateDefaultValues = true;
				}
				
				if (!retBundle.containsKey(ENGINEON))
				{
					retBundle.putBoolean(ENGINEON, false);
					updateDefaultValues = true;					
				}

				if (!retBundle.containsKey(CURRENTSPEED))
				{
					retBundle.putInt(CURRENTSPEED, 0);
					updateDefaultValues = true;					
				}
				
				if (!retBundle.containsKey(CURRENTODOMETER))
				{
					retBundle.putFloat(CURRENTODOMETER, 156328.4F);
					updateDefaultValues = true;					
				}
				
				if (!retBundle.containsKey(CURRENTTACH))
				{
					retBundle.putFloat(CURRENTTACH, 0F);
					updateDefaultValues = true;					
				}
				
				if (!retBundle.containsKey(LASTDATETIME))
				{
	    			Date newDate = DateUtility.getCurrentDateTimeUTC();
	    			//newDate = DateUtility.AddHours(newDate, -12);

					retBundle.putLong(LASTDATETIME, newDate.getTime());
					updateDefaultValues = true;					
				}		
				
				if (!retBundle.containsKey(ENGINEOFFCOMMSTIMEOUT))
				{
					retBundle.putInt(ENGINEOFFCOMMSTIMEOUT, -1);
					updateDefaultValues = true;
				}
				
				if (!retBundle.containsKey(DATACOLLECTIONRATE))
				{
	    			retBundle.putInt(DATACOLLECTIONRATE, 1);
	    			updateDefaultValues = true;
				}
				
				if (!retBundle.containsKey(COMPANYPASSKEY))
    			{
					retBundle.putString(COMPANYPASSKEY, "abc");
					updateDefaultValues = true;
    			}
				
				if (!retBundle.containsKey(BUSTYPE))
    			{
					retBundle.putByte(BUSTYPE, (byte)1);
					updateDefaultValues = true;
    			}
			
				if (!retBundle.containsKey(ODOMETEROFFSET))
				{
					retBundle.putInt(ODOMETEROFFSET, 0);
					updateDefaultValues = true;
				}

				if (!retBundle.containsKey(ODOMETERMULTIPLIER))
				{
					retBundle.putInt(ODOMETERMULTIPLIER, 0);
					updateDefaultValues = true;
				}
				
				if (updateDefaultValues)
		   			this.writeSDFile(retBundle, false);

			}
			catch(IOException ioe)
			{
				retBundle = null;
			}
		}
		
		return retBundle;
	}
	
	public float calcOdometer(Bundle prevValues, Date currentDateTime)
	{
		float retValue = 0.0000F;
		if (prevValues != null)
		{
			int currentSpeed = prevValues.getInt(CURRENTSPEED);
			float currentOdometer = prevValues.getFloat(CURRENTODOMETER);
			
			retValue = currentOdometer;
			
			// if there was previously a speed defined, update the odometer
			// value based on speed and amount of time driven
			if (currentSpeed > 0)
			{
				Date prevDate = new Date(prevValues.getLong(LASTDATETIME));			
            	long diffInMs = currentDateTime.getTime() - prevDate.getTime();
                
            	retValue += ((float)(currentSpeed * diffInMs) / (float)3600000);
			}
		}
		
		return retValue;
	}
	
    public int SendCode(int flag)
    {
        int rc = -1;

        if (EobrReader.getInstance() != null)
        {
	        try
	        {
                rc = EobrReader.getInstance().Technician_SetDebugFlag(flag);
	        }
	        catch (Exception excp)
	        {
	            this.HandleException(excp);
	        }
        }
        return rc;
    }
    
    
	public int sendConsoleCommand(String command) {
		
		int response = -1;
		
		try {
			
			response = EobrReader.getInstance().SendConsoleCommand(command).getInt(this.getContext().getString(R.string.rc));
		}
		catch (Exception ex) {
			this.HandleException(ex);
		}
		
		return response;
		
	}
	
    
    public int GetCurrentData(StatusRecord record)
    {
        int rc = -1;

        if (EobrReader.getInstance() != null)
        {
	        try
	        {
                rc = EobrReader.getInstance().Technician_GetCurrentData(record, false);
	        }
	        catch (Exception excp)
	        {
	            this.HandleException(excp);
	        }
        }
        return rc;
    }
    
	public void SuspendReading()
	{
        // get eobr reader and suspend reading
        EobrReader eobr = EobrReader.getInstance();
        eobr.SuspendReading();
	}
	
	public void ResumeReading()
	{
        // get eobr reader and resume reading
        EobrReader eobr = EobrReader.getInstance();
        eobr.ResumeReading();		
	}
}
