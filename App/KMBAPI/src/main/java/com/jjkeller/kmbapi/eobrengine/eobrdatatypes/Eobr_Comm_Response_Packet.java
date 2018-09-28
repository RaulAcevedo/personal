package com.jjkeller.kmbapi.eobrengine.eobrdatatypes;

public class Eobr_Comm_Response_Packet {

	private byte[] response;
	private int returnCode;
	
	
	// Response.
	public byte[] getResponse() {
		return this.response;
	}
	
	public void setResponse(byte[] response) {
		this.response = response;
	}
	
	
	// ReturnCode.
	public int getReturnCode() {
		return this.returnCode;
	}
	
	public void setReturnCode(int retCode) {
		this.returnCode = retCode;
	}
	
}