package com.jjkeller.kmbapi.proxydata;


import com.jjkeller.kmbapi.enums.CanadaDeferralTypeEnum;
import com.jjkeller.kmbapi.enums.DriverTypeEnum;
import com.jjkeller.kmbapi.enums.DutyStatusEnum;
import com.jjkeller.kmbapi.enums.ExemptLogTypeEnum;
import com.jjkeller.kmbapi.enums.RuleSetTypeEnum;
import com.jjkeller.kmbapi.enums.TimeZoneEnum;

import java.util.Date;

public class EldRegistrationInfo extends ProxyBase {

	///////////////////////////////////////////////////////////////////////////////////////
	// private members
	///////////////////////////////////////////////////////////////////////////////////////
	private String fmcsaRegistrationId;
	private String fmcsaProviderName;
	private String name;
	private String eldIdentifier;
	private String firmwareType;
	private String minAppVersion;
	private String maxAppVersion;
	private Date changeDate;

	//////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	// public get/set methods
	///////////////////////////////////////////////////////////////////////////////////////
	public String getFmcsaRegistrationId() {
		return fmcsaRegistrationId;
	}
	public void setFmcsaRegistrationId(String fmcsaRegistrationId) {
		this.fmcsaRegistrationId = fmcsaRegistrationId;
	}

	public String getFmcsaProviderName() {
		return fmcsaProviderName;
	}
	public void setFmcsaProviderName(String fmcsaProviderName) {
		this.fmcsaProviderName = fmcsaProviderName;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public String getEldIdentifier() {
		return eldIdentifier;
	}
	public void setEldIdentifier(String eldIdentifier) {
		this.eldIdentifier = eldIdentifier;
	}

	public String getFirmwareType() {
		return firmwareType;
	}
	public void setFirmwareType(String firmwareType) {
		this.firmwareType = firmwareType;
	}

	public String getMinAppVersion() {
		return minAppVersion;
	}
	public void setMinAppVersion(String minAppVersion) {
		this.minAppVersion = minAppVersion;
	}

	public String getMaxAppVersion() {
		return maxAppVersion;
	}
	public void setMaxAppVersion(String maxAppVersion) {
		this.maxAppVersion = maxAppVersion;
	}

	public Date getChangeDate() {
		return changeDate;
	}
	public void setChangeDate(Date changeDate) {
		this.changeDate = changeDate;
	}

	@Override
    public String toString() {
        return "EldRegistrationInfo{" +
				"fmcsaRegistrationId=" + fmcsaRegistrationId +
                ", fmcsaProviderName=" + fmcsaProviderName +
                ", fmcsaRegistrationId=" + fmcsaRegistrationId +
				", name=" + name +
				", eldIdentifier=" + eldIdentifier +
				", firmwareType=" + firmwareType +
				", minAppVersion=" + minAppVersion +
				", maxAppVersion=" + maxAppVersion +
				", changeDate=" + changeDate +
                '}';
    }

    ///////////////////////////////////////////////////////////////////////////////////////
	// Custom methods
	///////////////////////////////////////////////////////////////////////////////////////

}
