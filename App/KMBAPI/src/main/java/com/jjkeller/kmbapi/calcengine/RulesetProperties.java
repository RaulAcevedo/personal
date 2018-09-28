package com.jjkeller.kmbapi.calcengine;

public class RulesetProperties {	
	
	//Constructor
	public RulesetProperties()
	{
	}
	
	private long _weeklyDutyTotal = 0; 
	public long getWeeklyDutyTotal(){
		return _weeklyDutyTotal;
	}
	
	private boolean _isShortHaulExceptionAllowed = false;
	public boolean getIsShortHaulExceptionAllowed() {
		return _isShortHaulExceptionAllowed;
	}
	public void setIsShortHaulExceptionAllowed(boolean value) {
		_isShortHaulExceptionAllowed = value;
	}
	
	private boolean _is34HourResetAllowed = false; 
	public boolean getIs34HourResetAllowed() {
		return _is34HourResetAllowed;
	}
	public void setIs34HourResetAllowed(boolean value) {
		_is34HourResetAllowed = value;
	}
	
	private boolean _isTeamDriverPresent = false;
	public boolean getIsTeamDriverPresent() {
		return _isTeamDriverPresent;
	}
	public void setIsTeamDriverPresent(boolean value) {
		_isTeamDriverPresent = value;
	}
	
	private boolean _is8HourDrivingRuleEnabled = false;
	// Answer if the proposed 8 Hour Driving rule that includes a provision for a 30 minute break is enabled.
	// This rule is tentatively planned for enforcement on 07/01/2013
	public boolean getIs8HourDrivingRuleEnabled() {
		return _is8HourDrivingRuleEnabled;
	}
	public void setIs8HourDrivingRuleEnabled(boolean value) {
		_is8HourDrivingRuleEnabled = value;
	}
}
