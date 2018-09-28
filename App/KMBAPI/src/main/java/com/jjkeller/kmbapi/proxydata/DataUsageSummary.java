package com.jjkeller.kmbapi.proxydata;

import java.util.Date;



public class DataUsageSummary extends ProxyBase {

	private String deviceId;
	private Date usageDate;
	private long sentBytes;
	private long receivedBytes;
	
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
	
	public long getSentBytes() {
		return sentBytes;
	}
	
	public void setSentBytes(long sentBytes) {
		this.sentBytes = sentBytes;
	}
	
	public long getReceivedBytes() {
		return receivedBytes;
	}
	
	public void setReceivedBytes(long receivedBytes) {
		this.receivedBytes = receivedBytes;
	}
	
	

}
