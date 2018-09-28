package com.jjkeller.kmbapi.controller.calcengine;

import com.jjkeller.kmbapi.enums.DriverTypeEnum;
import com.jjkeller.kmbapi.enums.ExemptLogTypeEnum;
import com.jjkeller.kmbapi.enums.RuleSetTypeEnum;

import org.joda.time.Duration;

import java.util.Date;

/**
 * Log Summary
 *
 * Summary of an employee log for a view to consume.
 */
public class LogSummary {

	private Date _logDate;
	private DriverTypeEnum _driverType;
	private RuleSetTypeEnum _ruleset;
	private ExemptLogTypeEnum _exemptLogType;
	private boolean _logExists;
	private boolean _isComplete;

    private Long onDutyDurationMilliseconds;
    private Long drivingDurationMilliseconds;

	private float _distance;
	private Long weeklyTotalDurationMilliseconds;
	private int _offDutyStart;
	private int _offDutyEnd;
	
	public Date getLogDate() {
		return _logDate;
	}
	public void setLogDate(Date logDate) {
		this._logDate = logDate;
	}
	
	public DriverTypeEnum getDriverType() {
		return _driverType;
	}
	public void setDriverType(DriverTypeEnum driverType) {
		this._driverType = driverType;
	}
	
	public RuleSetTypeEnum getRuleset() {
		return _ruleset;
	}
	public void setRuleset(RuleSetTypeEnum ruleset) {
		this._ruleset = ruleset;
	}
	
	public ExemptLogTypeEnum getExemptLogType() {
		return _exemptLogType;
	}
	public void setExemptLogType(ExemptLogTypeEnum exemptLogType) {
		this._exemptLogType = exemptLogType;
	}

	public boolean getLogExists() {
		return _logExists;
	}
	public void setLogExists(boolean logExists) {
		this._logExists = logExists;
	}
	
	public boolean getIsComplete() {
		return _isComplete;
	}
	public void setIsComplete(boolean isComplete) {
		this._isComplete = isComplete;
	}

    public Long getOnDutyDuration(){
        return this.onDutyDurationMilliseconds;
    }

    public void setOnDutyDuration(Long millisecondDuration){
        this.onDutyDurationMilliseconds = millisecondDuration;
    }

    public Long getDrivingDuration(){
        return this.drivingDurationMilliseconds;
    }

    public void setDrivingDuration(Long millisecondDuration){
        this.drivingDurationMilliseconds = millisecondDuration;
    }
	
	public float getDistance() {
		return _distance;
	}
	public void setDistance(float distance) {
		this._distance = distance;
	}
	
	public Long getWeeklyTotalDuration() {
		return weeklyTotalDurationMilliseconds;
	}
	public void setWeeklyTotalDuration(Long weeklyTotal) {
		this.weeklyTotalDurationMilliseconds = weeklyTotal;
	}
	
	public int getOffDutyStart() {
		return _offDutyStart;
	}
	public void setOffDutyStart(int offDutyStart) {
		this._offDutyStart = offDutyStart;
	}
	
	public int getOffDutyEnd() {
		return _offDutyEnd;
	}
	public void setOffDutyEnd(int offDutyEnd) {
		this._offDutyEnd = offDutyEnd;
	}
	
	public Long getDailyDurationTotal()
	{
        if (onDutyDurationMilliseconds == null || drivingDurationMilliseconds == null){
            return null;
        }

        return onDutyDurationMilliseconds + drivingDurationMilliseconds;
	}
}
