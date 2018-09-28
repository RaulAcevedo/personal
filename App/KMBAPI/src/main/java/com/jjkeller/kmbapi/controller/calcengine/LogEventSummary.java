package com.jjkeller.kmbapi.controller.calcengine;

import com.jjkeller.kmbapi.enums.DutyStatusEnum;
import com.jjkeller.kmbapi.enums.RuleSetTypeEnum;

import java.util.Date;

public class LogEventSummary {

	public LogEventSummary(){}
	
	private Date _startTime;
	public Date getStartTime()
	{
		return this._startTime;
	}
	public void setStartTime(Date startTime)
	{
		this._startTime = startTime;
	}
	
	private DutyStatusEnum _dutyStatusEnum;
	public DutyStatusEnum getDutyStatusEnum()
	{
		return this._dutyStatusEnum;
	}
	public void setDutyStatus(DutyStatusEnum dutyStatusEnum)
	{
		this._dutyStatusEnum = dutyStatusEnum;
	}
	
	private Long _duration;
	public Long getDuration(){
		return this._duration;
	}
	public void setDuration(Long duration){
		this._duration = duration;
	}
		
	private String _location;
	public String getLocation()
	{
		return this._location;
	}
	public void setLocation(String location)
	{
		this._location = location;
	}
	
	private RuleSetTypeEnum _ruleSetTypeEnum;
	public RuleSetTypeEnum getRuleSetTypeEnum()
	{
		return this._ruleSetTypeEnum;
	}
	public void setRuleSetTypeEnum(RuleSetTypeEnum ruleSetTypeEnum)
	{
		this._ruleSetTypeEnum = ruleSetTypeEnum;
	}

	private String _motionPictureAuthorityId;
	public String getMotionPictureAuthorityId()
	{
		return this._motionPictureAuthorityId;
	}
	public void setMotionPictureAuthorityId(String motionPictureAuthorityId)
	{
		this._motionPictureAuthorityId = motionPictureAuthorityId;
	}

	private String _motionPictureProductionId;
	public String getMotionPictureProductionId()
	{
		return this._motionPictureProductionId;
	}
	public void setMotionPictureProductionId(String motionPictureAuthorityId)
	{
		this._motionPictureProductionId = motionPictureAuthorityId;
	}

	private String _remarks;
	public String getRemarks()
	{
		return this._remarks;
	}
	public void setRemarks(String remarks)
	{
		this._remarks = remarks;
	}
}
