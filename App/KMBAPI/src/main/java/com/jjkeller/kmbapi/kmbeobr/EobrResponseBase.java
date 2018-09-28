package com.jjkeller.kmbapi.kmbeobr;

public class EobrResponseBase {
	
	private int returnCode;
	
	public EobrResponseBase(){}
	
	public EobrResponseBase(int returnCode)
	{
		this.returnCode = returnCode;
	}
	
	public int getReturnCode() {
		return returnCode;
	}
	public void setReturnCode(int returnCode) {
		this.returnCode = returnCode;
	}	
	    
}
