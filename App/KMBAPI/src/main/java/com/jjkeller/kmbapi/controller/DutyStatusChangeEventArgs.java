package com.jjkeller.kmbapi.controller;

import com.jjkeller.kmbapi.enums.DutyStatusEnum;
import com.jjkeller.kmbapi.enums.RuleSetTypeEnum;
import com.jjkeller.kmbapi.proxydata.Location;

import java.util.Date;

public class DutyStatusChangeEventArgs {

	public DutyStatusChangeEventArgs(){}
	
	private Date logDate;
	private Date timestamp;
	private DutyStatusEnum dutyStatus;
	private Location location;
	private RuleSetTypeEnum ruleset;
	
	public Date getLogDate() {
		return logDate;
	}
	public void setLogDate(Date logDate) {
		this.logDate = logDate;
	}
	
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	
	public DutyStatusEnum getDutyStatus() {
		return dutyStatus;
	}
	public void setDutyStatus(DutyStatusEnum dutyStatus) {
		this.dutyStatus = dutyStatus;
	}
	
	public Location getLocation() {
		return location;
	}
	public void setLocation(Location location) {
		this.location = location;
	}
	
	public RuleSetTypeEnum getRuleset() {
		return ruleset;
	}
	public void setRuleset(RuleSetTypeEnum ruleset) {
		this.ruleset = ruleset;
	}
}
