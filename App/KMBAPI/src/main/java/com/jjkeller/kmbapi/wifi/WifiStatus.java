package com.jjkeller.kmbapi.wifi;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;

import com.jjkeller.kmbapi.R;

public class WifiStatus
{
	public static NetworkInfo.DetailedState getCurrentDetailedState(Context context)
	{
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivityManager != null)
		{
			NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			if (wifiNetworkInfo != null)
			{
				return wifiNetworkInfo.getDetailedState();
			}
		}
		return NetworkInfo.DetailedState.DISCONNECTED;
	}

	public static boolean isConnected(Context context)
	{
		return getCurrentDetailedState(context) == DetailedState.CONNECTED;
	}

	public static String getPrintable(Context context, NetworkInfo.DetailedState detailedState)
	{
		switch (detailedState)
		{
			case AUTHENTICATING:
				return context.getString(R.string.wifi_status_authenticating);
			case CONNECTED:
				return context.getString(R.string.wifi_status_connected);
			case CONNECTING:
				return context.getString(R.string.wifi_status_connecting);
			case DISCONNECTED:
				return context.getString(R.string.wifi_status_disconnected);
			case DISCONNECTING:
				return context.getString(R.string.wifi_status_disconnecting);
			case FAILED:
				return context.getString(R.string.wifi_status_failed);
			case OBTAINING_IPADDR:
				return context.getString(R.string.wifi_status_obtaining_ip);
			case SCANNING:
				return context.getString(R.string.wifi_status_scanning);
			default:
				return null;
		}
	}
}
