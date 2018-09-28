package com.jjkeller.kmbapi.proxydata;


import com.jjkeller.kmbapi.R;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.controller.utility.DeviceInfo;

public class MobileDevice extends ProxyBase {

	///////////////////////////////////////////////////////////////////////////////////////
	// private members
	///////////////////////////////////////////////////////////////////////////////////////
	
	private String deviceIMEI = null;
	private String model = null;
	private String osType = null;
	private String releaseVersion = null;
	private String kmbVersion = null;
	private boolean isComplianceTablet = false;
	private boolean coPilot = false;
	private String lastEmployeeId = null;
	private String lastKMBEOBRDeviceSerialNumber = null;


	///////////////////////////////////////////////////////////////////////////////////////
	// public get/set methods
	///////////////////////////////////////////////////////////////////////////////////////

	public String getDeviceIMEI() {
		return deviceIMEI;
	}

	public void setDeviceIMEI(String deviceIMEI) {
		this.deviceIMEI = deviceIMEI;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}
	
	public String getOsType() {
		return osType;
	}

	public void setOsType(String osType) {
		this.osType = osType;
	}

	public String getReleaseVersion() {
		return releaseVersion;
	}

	public void setReleaseVersion(String releaseVersion) {
		this.releaseVersion = releaseVersion;
	}

	public String getKmbVersion() {
		return kmbVersion;
	}

	public void setKmbVersion(String kmbVersion) {
		this.kmbVersion = kmbVersion;
	}

	public boolean isComplianceTablet() {
		return isComplianceTablet;
	}

	public void setComplianceTablet(boolean isComplianceTablet) {
		this.isComplianceTablet = isComplianceTablet;
	}

	public boolean isCoPilot() {
		return coPilot;
	}

	public void setCoPilot(boolean coPilot) {
		this.coPilot = coPilot;
	}

	public String getLastEmployeeId() {
		return lastEmployeeId;
	}

	public void setLastEmployeeId(String lastEmployeeId) {
		this.lastEmployeeId = lastEmployeeId;
	}

	public String getLastKMBEOBRDeviceSerialNumber() {
		return lastKMBEOBRDeviceSerialNumber;
	}

	public void setLastKMBEOBRDeviceSerialNumber(String lastKMBEOBRDeviceSerialNumber) {
		this.lastKMBEOBRDeviceSerialNumber = lastKMBEOBRDeviceSerialNumber;
	}

	///////////////////////////////////////////////////////////////////////////////////////
	// Custom methods
	///////////////////////////////////////////////////////////////////////////////////////

    public static MobileDevice forCurrentDevice(GlobalState globalState) {
        MobileDevice mobileInfo = new MobileDevice();
        mobileInfo.setDeviceIMEI(DeviceInfo.GetDeviceIMEI(globalState));
        mobileInfo.setModel(DeviceInfo.GetDeviceModel());
        mobileInfo.setOsType(globalState.getString(R.string.ostypetext));
        mobileInfo.setReleaseVersion(DeviceInfo.GetReleaseVersion());
        mobileInfo.setKmbVersion(globalState.getPackageVersionName());
        mobileInfo.setComplianceTablet(DeviceInfo.IsComplianceTablet());
		User currentUser = globalState.getCurrentUser();
		if (currentUser != null)
            mobileInfo.setLastEmployeeId(currentUser.getCredentials().getEmployeeId());
        return mobileInfo;
    }
}
