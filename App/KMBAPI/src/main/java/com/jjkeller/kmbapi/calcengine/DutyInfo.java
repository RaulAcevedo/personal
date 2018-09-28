package com.jjkeller.kmbapi.calcengine;

import java.util.Date;

public class DutyInfo
{
    boolean _shortHaulExceptionUsed;
    boolean _hasReturnedToWorkLocation;
    boolean _isDutyTour;
    boolean _isTodaysLog;
    Date _weeklyResetStartTimestamp;
    boolean _isHaulingExplosives;
	boolean _isWeeklyResetUsed;
	boolean _isWeeklyResetUsedOverridden;
	boolean _isOperatesSpecificVehiclesForOilField;
	boolean _isNonCDLShortHaulExceptionUsed;
    
    public DutyInfo()
    {
    	this.setShortHaulExceptionUsed(false);
    	this.setHasReturnedToWorkLocation(false);
    	this.setIsDutyTour(false);
    	this.setIsTodaysLog(false);
    	this.setIsHaulingExplosives(false);
    	this.setIsOperatesSpecificVehiclesForOilField(false);
    }
    
    public boolean getShortHaulExceptionUsed()
    {
    	return this._shortHaulExceptionUsed;
    }
    public void setShortHaulExceptionUsed(boolean shortHaulExceptionUsed)
    {
    	this._shortHaulExceptionUsed = shortHaulExceptionUsed;
    }

    public boolean getHasReturnedToWorkLocation()
    {
    	return this._hasReturnedToWorkLocation;
    }
    public void setHasReturnedToWorkLocation(boolean hasReturnedToWorkLocation)
    {
    	this._hasReturnedToWorkLocation = hasReturnedToWorkLocation;
    }

    public boolean getIsDutyTour()
    {
    	return this._isDutyTour;
    }
    public void setIsDutyTour(boolean isDutyTour)
    {
    	this._isDutyTour = isDutyTour;
    }
    
    public boolean getIsTodaysLog()
    {
    	return this._isTodaysLog;
    }
    public void setIsTodaysLog(boolean isTodaysLog)
    {
    	this._isTodaysLog = isTodaysLog;
    }
    
    public Date getWeeklyResetStartTimestamp()
    {
    	return this._weeklyResetStartTimestamp;
    }
    public void setWeeklyResetStartTimestamp(Date val)
    {
    	this._weeklyResetStartTimestamp = val;
    }
    public boolean getIsHaulingExplosives()
    {
    	return this._isHaulingExplosives;
    }
    public void setIsHaulingExplosives(boolean value)
    {
    	this._isHaulingExplosives = value;
    }
    public boolean getIsWeeklyResetUsed()
    {
    	return this._isWeeklyResetUsed;
    }
    public void setIsWeeklyResetUsed(boolean value)
    {
    	this._isWeeklyResetUsed = value;
    }
    public boolean getIsWeeklyResetUsedOverridden()
    {
    	return this._isWeeklyResetUsedOverridden;
    }
    public void setIsWeeklyResetUsedOverridden(boolean value)
    {
    	this._isWeeklyResetUsedOverridden = value;
    }
    public boolean getIsOperatesSpecificVehiclesForOilField()
    {
    	return this._isOperatesSpecificVehiclesForOilField;
    }
    public void setIsOperatesSpecificVehiclesForOilField(boolean value)
    {
    	this._isOperatesSpecificVehiclesForOilField = value;
    }
    public boolean getIsNonCDLShortHaulExceptionUsed()
    {
    	return this._isNonCDLShortHaulExceptionUsed;
    }
    public void setIsNonCDLShortHaulExceptionUsed(boolean value)
    {
    	this._isNonCDLShortHaulExceptionUsed = value;
    }
}
