package com.jjkeller.kmbapi.kmbeobr;

public class FirmwareUpgradeStatusResult extends EobrResponseBase {
	
	private long stagedFirmwarePatchId;
	private long currentFirmwarePatchId;
	private long requestedFirmwarePatchId;
	
	public FirmwareUpgradeStatusResult(){}
	
	public FirmwareUpgradeStatusResult(int returnCode)
	{
		super(returnCode);
	}
		
	public long getStagedFirmwarePatchId() {
		return stagedFirmwarePatchId;
	}
	public void setStagedFirmwarePatchId(long firmwarePatchId) {
		this.stagedFirmwarePatchId = firmwarePatchId;
	}
	
	public long getRequestedFirmwarePatchId() {
		return requestedFirmwarePatchId;
	}

	public void setRequestedFirmwarePatchId(long requestedFirmwarePatchId) {
		this.requestedFirmwarePatchId = requestedFirmwarePatchId;
	}
	
	public long getCurrentFirmwarePatchId() {
		return currentFirmwarePatchId;
	}
	public void setCurrentFirmwarePatchId(long firmwarePatchId) {
		this.currentFirmwarePatchId = firmwarePatchId;
	}
	
	public boolean getIsUpgradeInProgress()
	{
		return this.stagedFirmwarePatchId == 0xFFFFFFFF;
	}
	
	public boolean getIsUpgradeFailed()
	{
		return this.stagedFirmwarePatchId == 0x0;
	}
}
