package com.jjkeller.kmbapi.proxydata;

import java.util.Date;

public class DataUsage extends ProxyBase { 

	private String deviceId;
	private Date usageDate;
	private long transmittedBytes;
	private long receivedBytes;
	private int networkEnum = 0;
	private String uri;

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public Date getUsageDate() {
		return usageDate;
	}

	public void setUsageDate(Date usageDate) {
		this.usageDate = usageDate;
	}

	public long getTransmittedBytes() {
		return transmittedBytes;
	}

	public void setTransmittedBytes(long transmittedBytes) {
		this.transmittedBytes = transmittedBytes;
	}

	public long getReceivedBytes() {
		return receivedBytes;
	}

	public void setReceivedBytes(long receivedBytes) {
		this.receivedBytes = receivedBytes;
	}

	public int getNetworkEnum() {
		return networkEnum;
	}

	public void setNetworkEnum(int networkEnum) {
		this.networkEnum = networkEnum;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}
	

}
