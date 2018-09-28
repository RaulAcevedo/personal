package com.jjkeller.kmbapi.calcengine;

import com.jjkeller.kmbapi.calcengine.Enums.CanadaDeferralTypeEnum;
import com.jjkeller.kmbapi.enums.ExemptLogTypeEnum;

import java.util.Date;

public class LogProperties {
		
	public LogProperties(){
		this.setIsTodaysLog(false);
		this.setHasReturnedToWorkLocation(false);
		this.setIsShortHaulExceptionUsed(false);
		this.setCanadaDeferralType(CanadaDeferralTypeEnum.None);
		this.setWeeklyResetStartTimestamp(null);
		this.setIsHaulingExplosives(false);
		this.setIsWeeklyResetUsed(false);
		this.setIsWeeklyResetUsedOverridden(false);
		this.setIsOperatesSpecificVehiclesForOilField(false);
		this.setIsFirstLogInCollection(false);
		this.setLastUsedWeeklyResetStartTimestamp(null);
		this.setExemptLogType(new ExemptLogTypeEnum(ExemptLogTypeEnum.NULL));
		this.setIsNonCDLShortHaulExceptionUsed(false);
        this.setIs30MinuteRestBreakExempt(false);
	}
	
	public LogProperties(Date logDate, boolean isTodaysLog, boolean hasReturnedToWorkLocation, boolean isShortHaulExceptionUsed, CanadaDeferralTypeEnum deferralType, Date weeklyResetStartTimestamp, boolean isHaulingExplosives, boolean isWeeklyResetUsed, boolean IsWeeklyResetUsedOverridden, boolean isOperatesSpecificVehiclesForOilField, ExemptLogTypeEnum exemptLogType, boolean isNonCDLShortHaulExceptionUsed, boolean is30MinuteRestBreakExempt)
	{
		this.setLogDate(logDate);
		this.setIsTodaysLog(isTodaysLog);
		this.setHasReturnedToWorkLocation(hasReturnedToWorkLocation);
		this.setIsShortHaulExceptionUsed(isShortHaulExceptionUsed);
		this.setCanadaDeferralType(deferralType);
		this.setWeeklyResetStartTimestamp(weeklyResetStartTimestamp);
		this.setIsHaulingExplosives(isHaulingExplosives);
		this.setIsWeeklyResetUsed(isWeeklyResetUsed);
		this.setIsWeeklyResetUsedOverridden(IsWeeklyResetUsedOverridden);
		this.setIsOperatesSpecificVehiclesForOilField(isOperatesSpecificVehiclesForOilField);
		this.setIsFirstLogInCollection(false);
		this.setLastUsedWeeklyResetStartTimestamp(null);
		this.setExemptLogType(exemptLogType);
		this.setIsNonCDLShortHaulExceptionUsed(isNonCDLShortHaulExceptionUsed);
        this.setIs30MinuteRestBreakExempt(is30MinuteRestBreakExempt);
	}
	
	private Date logDate;
    public Date getLogDate()
    {
    	return this.logDate;
    }
    public void setLogDate(Date value)
    {
    	this.logDate = value;
    }    
    
	private boolean isTodaysLog;
    public boolean getIsTodaysLog()
    {
    	return this.isTodaysLog;
    }
    public void setIsTodaysLog(boolean value)
    {
    	this.isTodaysLog = value;
    }
    
	private boolean hasReturnedToWorkLocation;
    public boolean getHasReturnedToWorkLocation()
    {
    	return this.hasReturnedToWorkLocation;
    }
    public void setHasReturnedToWorkLocation(boolean value)
    {
    	this.hasReturnedToWorkLocation = value;
    }
    
	private boolean isShortHaulExceptionUsed;    
    public boolean getIsShortHaulExceptionUsed()
    {
    	return this.isShortHaulExceptionUsed;
    }
    public void setIsShortHaulExceptionUsed(boolean value)
    {
    	this.isShortHaulExceptionUsed = value;
    }
    
	private Date weeklyResetStartTimestamp;
    public Date getWeeklyResetStartTimestamp()
    {
    	return this.weeklyResetStartTimestamp;
    }
    public void setWeeklyResetStartTimestamp(Date value)
    {
    	this.weeklyResetStartTimestamp = value;
    }
    
	private boolean isWeeklyResetUsed;
    public boolean getIsWeeklyResetUsed()
    {
    	return this.isWeeklyResetUsed;
    }
    public void setIsWeeklyResetUsed(boolean value)
    {
    	this.isWeeklyResetUsed = value;
    }
    
	private boolean isWeeklyResetUsedOverridden;
	public boolean getIsWeeklyResetUsedOverridden()
    {
    	return this.isWeeklyResetUsedOverridden;
    }
    public void setIsWeeklyResetUsedOverridden(boolean value)
    {
    	this.isWeeklyResetUsedOverridden = value;
    }
    
	private boolean isHaulingExplosives;
    public boolean getIsHaulingExplosives()
    {
    	return this.isHaulingExplosives;
    }
    public void setIsHaulingExplosives(boolean value)
    {
    	this.isHaulingExplosives = value;
    }
    
    private CanadaDeferralTypeEnum canadaDeferralType;
    public CanadaDeferralTypeEnum getCanadaDeferralType()
    {
    	return this.canadaDeferralType;
    }
    public void setCanadaDeferralType(CanadaDeferralTypeEnum value)
    {
    	this.canadaDeferralType = value;
    }    

	private boolean isOperatesSpecificVehiclesForOilField;
    public boolean getIsOperatesSpecificVehiclesForOilField()
    {
    	return this.isOperatesSpecificVehiclesForOilField;
    }
    public void setIsOperatesSpecificVehiclesForOilField(boolean value)
    {
    	this.isOperatesSpecificVehiclesForOilField = value;
    }
    
    private boolean isFirstLogInCollection;
    public boolean getIsFirstLogInCollection()
    {
    	return this.isFirstLogInCollection;
    }
    public void setIsFirstLogInCollection(boolean value)
    {
    	this.isFirstLogInCollection = value;
    }
    
    private Date lastUsedWeeklyResetStartTimestamp;
    public Date getLastUsedWeeklyResetStartTimestamp()
    {
    	return this.lastUsedWeeklyResetStartTimestamp;
    }
    public void setLastUsedWeeklyResetStartTimestamp(Date value)
    {
    	this.lastUsedWeeklyResetStartTimestamp = value;
    }
    
    private ExemptLogTypeEnum exemptLogType;
    public ExemptLogTypeEnum getExemptLogType()
    {
    	return exemptLogType;
    }
    public void setExemptLogType(ExemptLogTypeEnum value)
    {
    	exemptLogType = value;
    }
    
    private boolean isNonCDLShortHaulExceptionUsed;
    public boolean getIsNonCDLShortHaulExceptionUsed()
    {
    	return isNonCDLShortHaulExceptionUsed;
    }
    public void setIsNonCDLShortHaulExceptionUsed(boolean value)
    {
    	isNonCDLShortHaulExceptionUsed = value;
    }

    private boolean is30MinuteRestBreakExempt;
    public boolean getIs30MinuteRestBreakExempt() { return is30MinuteRestBreakExempt;}
    public void setIs30MinuteRestBreakExempt(boolean value){
        is30MinuteRestBreakExempt = value;
    }
}
