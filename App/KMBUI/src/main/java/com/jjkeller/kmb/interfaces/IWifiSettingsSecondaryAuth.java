package com.jjkeller.kmb.interfaces;

public interface IWifiSettingsSecondaryAuth
{
	public interface WifiSettingsSecondaryAuthFragControllerMethods
	{
		public void testSecondaryAuthUrl();
	}
	
	public interface WifiSettingsSecondaryAuthFragActions
	{
		public void onNewUrlLoading(String url);
	}
}
