package com.jjkeller.kmb;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.utility.DateUtility;

public class TimeZoneReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		if (intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED))
		{
			try
			{
                DateUtility.setHomeTerminalTimeDateFormatTimeZone(GlobalState.getInstance().getCurrentUser().getHomeTerminalTimeZone().toTimeZone());
			}
			catch (Exception ex)
			{
				Log.e("TimeZoneReceiver", "Error while setting time zone", ex);
			}
		}
	}
}
