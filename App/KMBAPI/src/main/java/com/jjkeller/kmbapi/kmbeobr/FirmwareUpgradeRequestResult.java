package com.jjkeller.kmbapi.kmbeobr;

public class FirmwareUpgradeRequestResult extends EobrResponseBase {
	
	private byte status;
	
	public FirmwareUpgradeRequestResult(){}
	
	public FirmwareUpgradeRequestResult(int returnCode)
	{
		super(returnCode);
	}
		
	public byte getStatus() {
		return status;
	}
	public void setStatus(byte status) {
		this.status = status;
	}
	
	public boolean getIsSuccessful()
	{
		return this.status == 0;
	}
}
