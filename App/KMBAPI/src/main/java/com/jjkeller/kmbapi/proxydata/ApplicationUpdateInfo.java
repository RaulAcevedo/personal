package com.jjkeller.kmbapi.proxydata;


public class ApplicationUpdateInfo extends ProxyBase {

	///////////////////////////////////////////////////////////////////////////////////////
	// private members
	///////////////////////////////////////////////////////////////////////////////////////
	private boolean isAvailable = false;
	private String newVersion = null;
	private boolean includesFirmwareUpdate = false;
	private String downloadUrl = null;
	private boolean isWifiRequired = true;

	///////////////////////////////////////////////////////////////////////////////////////
	// public get/set methods
	///////////////////////////////////////////////////////////////////////////////////////
	public boolean getIsAvailable() {
		return isAvailable;
	}

	public void setIsAvailable(boolean isAvailable) {
		this.isAvailable = isAvailable;
	}

	public String getNewVersion() {
		return newVersion;
	}

	public void setNewVersion(String newVersion) {
		this.newVersion = newVersion;
	}

	public boolean getIncludesFirmwareUpdate() {
		return includesFirmwareUpdate;
	}

	public void setIncludesFirmwareUpdate(boolean includesFirmwareUpdate) {
		this.includesFirmwareUpdate = includesFirmwareUpdate;
	}

	public String getDownloadUrl() {
		return downloadUrl;
	}

	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}

	public boolean isWifiRequired()
	{
		return isWifiRequired;
	}

	public void setWifiRequired(boolean isWifiRequired)
	{
		this.isWifiRequired = isWifiRequired;
	}

	
	

	///////////////////////////////////////////////////////////////////////////////////////
	// Custom methods
	///////////////////////////////////////////////////////////////////////////////////////
}
