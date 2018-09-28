package com.jjkeller.kmbapi.kmbeobr;

public class EobrSelfTestResult
{
	private boolean isSuccessful;
	private int errorCode;

	public boolean isSuccessful()
	{
		return isSuccessful;
	}

	public void setSuccessful(boolean isSuccessful)
	{
		this.isSuccessful = isSuccessful;
	}

	public int getErrorCode()
	{
		return errorCode;
	}

	public void setErrorCode(int errorCode)
	{
		this.errorCode = errorCode;
	}
}
