package com.jjkeller.kmbapi.eobrengine.eobrdatatypes;

public class ConnectedBluetoothDevice extends ConnectedDevice {
	String btAddr;

	public String getAddress() {
		return this.btAddr;
	}

	public void setAddress(String address) {
		this.btAddr = address;
	}
}
