package com.jjkeller.kmbapi.controller.utility;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.TrafficStats;
import android.os.Process;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;

import com.jjkeller.kmbapi.configuration.GlobalState;

public class DeviceInfo {
	
	public static String CheckDevicePhoneNumber(Context c)
	{
		TelephonyManager tm = (TelephonyManager)c.getSystemService(Context.TELEPHONY_SERVICE);
		String phoneID = null;
		if(tm != null)
		{
			phoneID = tm.getLine1Number();
			// If the number is null or less than 7 digits, then try getting the number via VM
			if(phoneID == null || phoneID.length() < 7)
				phoneID = tm.getVoiceMailNumber();
			
			// If the number is not null and less than 7 digits, discard it (could be shortcode like *98)
			if(phoneID != null && phoneID.length() < 7)
				phoneID = null;
		}
		return phoneID;		
	}
	
	public static String GetDeviceIdentifier(Context c)
	{
		// Get phone number.  If not found, just use the drivers id
		String deviceId = CheckDevicePhoneNumber(c);
		if(deviceId == null){
			if(GlobalState.getInstance().getCurrentUser() != null &&
					GlobalState.getInstance().getCurrentUser().getCredentials() != null &&
					GlobalState.getInstance().getCurrentUser().getCredentials().getEmployeeId() != null)
				deviceId = GlobalState.getInstance().getCurrentUser().getCredentials().getEmployeeId();
			else 
				deviceId = "Default";
		}
		return deviceId;
	}
	
	public static boolean IsAppSideloaded(PackageManager pm) {
		boolean isSideloaded = true;
		try {
			// 3/21/14 MEC: Sideloaded apps will return a null value.  If from the google play store the
			// value will not be null
			String installer = pm.getInstallerPackageName(GlobalState.getInstance().getPackageName());
			//installer can sometimes be the native android installer, so, if it is not null, let's validate that the
			//value do not correspond to the native installer.
			if (installer != null && !installer.equals("com.android.packageinstaller")) {
				isSideloaded = false;
			}
		}
		catch(Exception e) {
			// TO DO:
		}
		return isSideloaded;
	}
	
	public static String GetDeviceIMEI(Context c)
	{
		String str = "";
		TelephonyManager tm = (TelephonyManager)c.getSystemService(Context.TELEPHONY_SERVICE);
		
		if (tm != null && tm.getDeviceId() != null)
			str = tm.getDeviceId();
		else 
			str = Secure.getString(c.getContentResolver(), Secure.ANDROID_ID);
		
		return str;
	}
	
	public static String GetDeviceModel()
	{
		return android.os.Build.MODEL;
	}
	
	public static String GetReleaseVersion()
	{
		return android.os.Build.VERSION.RELEASE;
	}
	
	public static String GetSDKVersion()
	{
		return android.os.Build.VERSION.SDK;
	}
	
	//4-10-2014 MEC: Included to test if the Compliance Tablet file exists from the CDW install
	public static boolean IsComplianceTablet() {
		if(GlobalState.getInstance().getFeatureService().getForceComplianceTabletMode())
			return true;
		
		return FileUtility.IsComplianceTabletFileInstalled();
	}
	
	public static boolean IsAPIAvailable(int value)
	{
		return android.os.Build.VERSION.SDK_INT >= value;
	}
	
	public static long GetTxBytes()
	{
		return TrafficStats.getUidTxBytes(Process.myUid());
	}
	
	public static long GetRxBytes()
	{
		return TrafficStats.getUidRxBytes(Process.myUid());
	}

	/**
	 * Helper method which returns the device's current orientation
	 * @param context A context to access device's services
	 * @return Portrait {@link Configuration#ORIENTATION_PORTRAIT} or Landscape {@link Configuration#ORIENTATION_LANDSCAPE}
	 */
	public static int GetDeviceOrientation(Context context){
		return context.getResources().getConfiguration().orientation;
	}

}