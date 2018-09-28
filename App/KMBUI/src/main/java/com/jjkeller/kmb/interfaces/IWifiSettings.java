package com.jjkeller.kmb.interfaces;

public interface IWifiSettings
{
	public interface WifiSettingsFragControllerMethods
	{
		public void updateWifiStatusViews();
		public void updateAvailableNetworksList();
		public void showIsScanning(boolean isScanning);
	}
	
	public interface WifiSettingsFragActions
	{
		public void handleIsEnabledClicked();
		public void handleAvailableNetworkClicked(int position);
		public void handleAvailableNetworkLongClicked(int position);
	}
}
