package com.jjkeller.kmbapi.proxydata;

import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.enums.DutyStatusEnum;
import com.jjkeller.kmbapi.enums.RuleSetTypeEnum;

import java.util.Date;

public abstract class LogEvent extends ProxyBase {

	///////////////////////////////////////////////////////////////////////////////////////
	// private members
	///////////////////////////////////////////////////////////////////////////////////////
	private String _eobrSerialNumber;
	private Date _startTime;
	private DutyStatusEnum _dutyStatusEnum = new DutyStatusEnum(DutyStatusEnum.NULL);
	private Location _location = new Location();
	private boolean _isStartTimeValidated;
	private RuleSetTypeEnum _rulesetType = new RuleSetTypeEnum(RuleSetTypeEnum.NULL);
	private boolean _requiresManualLocation;
	private String _logRemark;
	private Date _logRemarkDate;
	private String _motionPictureAuthorityId;
	private String _motionPictureProductionId;

	public LogEvent() {}
	public LogEvent(Date startTime)
	{
		setStartTime(startTime);
	}
	
	///////////////////////////////////////////////////////////////////////////////////////
	// public get/set methods
	///////////////////////////////////////////////////////////////////////////////////////

	public String getEobrSerialNumber() {
		return _eobrSerialNumber;
	}
	public void setEobrSerialNumber(String eobrSerialNumber) { this._eobrSerialNumber = eobrSerialNumber; }
	public Date getStartTime() {
		return _startTime;
	}
	public void setStartTime(Date startTime) {
        if(GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled())
		    this._startTime = startTime;
        else
            this._startTime = DateUtility.getDateTimeWithoutSecondsFromDate(startTime);
    }
	public DutyStatusEnum getDutyStatusEnum() {
		return _dutyStatusEnum;
	}
	public void setDutyStatusEnum(DutyStatusEnum dutyStatusEnum) {
		this._dutyStatusEnum = dutyStatusEnum;
	}
	public Location getLocation() {
        if (_location == null) _location = new Location();
		return _location;
	}
	public void setLocation(Location location) {
		this._location = location;
	}
	public boolean getIsStartTimeValidated() {
		return _isStartTimeValidated;
	}
	public void setIsStartTimeValidated(boolean isStartTimeValidated) {
		this._isStartTimeValidated = isStartTimeValidated;
	}
	public RuleSetTypeEnum getRulesetType() {
		return _rulesetType;
	}
	public void setRulesetType(RuleSetTypeEnum rulesetType) {
		this._rulesetType = rulesetType;
	}
	public boolean getRequiresManualLocation() {
		return this._requiresManualLocation;
	}
	public void setRequiresManualLocation(boolean requiresManualLocation) {
		this._requiresManualLocation = requiresManualLocation;
	}
	public String getLogRemark() {
		return this._logRemark;
	}
	public void setLogRemark(String logRemark) {
		this._logRemark = logRemark;
	}
	public Date getLogRemarkDate() {
		return this._logRemarkDate;
	}
	public void setLogRemarkDate(Date logRemarkDate) {
		this._logRemarkDate = logRemarkDate;
	}
	public boolean isExemptOffDutyStatus()
	{
		return getDutyStatusEnum().isExemptOffDutyStatus();
	}
	public boolean isExemptOnDutyStatus()
	{
		return getDutyStatusEnum().isExemptOnDutyStatus();
	}

	public String getMotionPictureProductionId() {
		return this._motionPictureProductionId;
	}

	public void setMotionPictureProductionId(String motionPictureProductionId) {
		this._motionPictureProductionId = motionPictureProductionId;
	}

	public String getMotionPictureAuthorityId() {
		return this._motionPictureAuthorityId;
	}

	public void setMotionPictureAuthorityId(String motionPictureAuthorityId) {
		this._motionPictureAuthorityId = motionPictureAuthorityId;
	}
	///////////////////////////////////////////////////////////////////////////////////////
	// Custom methods
	///////////////////////////////////////////////////////////////////////////////////////
}
