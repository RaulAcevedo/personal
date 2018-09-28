package com.jjkeller.kmbapi.calcengine.Federal;

import android.os.Bundle;

import com.jjkeller.kmbapi.calcengine.DutyInfo;
import com.jjkeller.kmbapi.calcengine.Enums.DutyStatusEnum;
import com.jjkeller.kmbapi.calcengine.HoursOfServiceSummary;
import com.jjkeller.kmbapi.calcengine.LogProperties;
import com.jjkeller.kmbapi.calcengine.RulesetProperties;
import com.jjkeller.kmbapi.calcengine.SplitSleeperCombination;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.ControllerFactory;
import com.jjkeller.kmbapi.controller.ILogCheckerComplianceDatesController;
import com.jjkeller.kmbapi.controller.dataaccess.EmployeeLogFacade;
import com.jjkeller.kmbapi.controller.interfaces.IEmployeeLogFacade;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.controller.utility.EmployeeLogUtilities;
import com.jjkeller.kmbapi.enums.ExemptLogTypeEnum;
import com.jjkeller.kmbapi.enums.LogCheckerComplianceDatesTypeEnum;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;

import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;

public abstract class PropertyCarrying extends FederalRulesetBase{
	
	/**
	 * Amount of daily duty time allowed for the ruleset in milliseconds
	 */
	protected long DAILY_DUTY_TIME_ALLOWED_STANDARD;
	/**
	 * Amount of daily duty time allowed for the ruleset in milliseconds if a short haul exception is in use
	 */
	protected long DAILY_DUTY_TIME_ALLOWED_WITH_SHORT_HAUL_ADDITIONAL_DRIVING;

	private long DAILY_DRIVE_TIME_ALLOWED_8_HOUR_RULE;
	
	private long WEEKLY_DUTY_RESET_MINIMUM_ALLOWED_BETWEEN_RESETS;
	
	private static final String REGULATORY_SECTION_WEEKLY_DUTY = "395.3(b)";
	private static final String REGULATORY_SECTION_DAILY_DRIVE = "395.3(a)(3)(i)";
	private static final String REGULATORY_SECTION_DAILY_DRIVE_8HOUR = "395.3(a)(3)(ii)";
	private static final String REGULATORY_SECTION_DAILY_DUTY = "395.3(a)(2)";
	
	private boolean _processFirstOnDutyEvent = false;
	protected boolean getProcessFirstOnDutyEvent()
	{
		return _processFirstOnDutyEvent;
	}
	protected void setProcessFirstOnDutyEvent(boolean processFirstOnDutyEvent)
	{
		this._processFirstOnDutyEvent = processFirstOnDutyEvent;
	}

	private ILogCheckerComplianceDatesController _complianceDatesController; 
	
	private Date _originalValidWeeklyResetDutyStartTimestamp;
	protected Date getOriginalValidWeeklyResetDutyStartTimestamp()
	{
		return _originalValidWeeklyResetDutyStartTimestamp;
	}
	protected void setOriginalValidWeeklyResetDutyStartTimestamp(Date firstValidWeeklyResetDutyStartTimestamp)
	{
		this._originalValidWeeklyResetDutyStartTimestamp = firstValidWeeklyResetDutyStartTimestamp;
	}

	private Date _weeklyResetStartingTimestamp;
	protected Date getWeeklyResetStartingTimestamp()
	{
		return this._weeklyResetStartingTimestamp;
	}
	protected void setWeeklyResetStartingTimestamp(Date weeklyResetTimestamp)
	{
		this._weeklyResetStartingTimestamp = weeklyResetTimestamp;
	}
	
	private Date _weeklyResetEndingTimestamp;
	protected Date getWeeklyResetEndingTimestamp()
	{
		return this._weeklyResetEndingTimestamp;
	}
	protected void setWeeklyResetEndingTimestamp(Date weeklyResetTimestamp)
	{
		this._weeklyResetEndingTimestamp = weeklyResetTimestamp;
	}
	
	//Constructor
	protected PropertyCarrying(RulesetProperties properties)
	{
		super(properties);
		_complianceDatesController = ControllerFactory.getInstance().getLogCheckerComplianceDateController();
	}
	
	protected PropertyCarrying(RulesetProperties properties, ILogCheckerComplianceDatesController complianceDateController)
	{
		super(properties);
		_complianceDatesController = complianceDateController;
	}
	
	/**
	 * Initialize the calc engine.
	 * This must be called before any other methods are called on the calc engine.
	 */
	@Override
	public void Initialize(RulesetProperties properties)
	{
		super.Initialize(properties);
		
		this.MarkSleeperCombinationReset();
		
		this.DAILY_DRIVE_TIME_ALLOWED = 11 * MILLISECONDS_PER_HOUR;	
		
		// initialize the accumulator for the proposed 8 hour driving rule tentatively going into effect 7/1/2013
		this.DAILY_DRIVE_TIME_ALLOWED_8_HOUR_RULE = 8 * MILLISECONDS_PER_HOUR;

		// initialize the minimum amount of time between weekly reset periods for the proposed 34 hour reset rule change tentatively going into effect 7/1/2013
		this.WEEKLY_DUTY_RESET_MINIMUM_ALLOWED_BETWEEN_RESETS = 168 * MILLISECONDS_PER_HOUR;
		
		this.DAILY_DUTY_TIME_ALLOWED = 14 * MILLISECONDS_PER_HOUR;
		this.DAILY_DUTY_TIME_ALLOWED_STANDARD = this.DAILY_DUTY_TIME_ALLOWED;
		this.DAILY_OFFDUTY_HOURS_FOR_RESET = 10 * MILLISECONDS_PER_HOUR;
		this.WEEKLY_OFFDUTY_HOURS_FOR_RESET = 34 * MILLISECONDS_PER_HOUR;
		this.setWeeklyResetEndingTimestamp(null);
		
		this.setDutyInfoByDate(new Hashtable<Date, DutyInfo>());
		this.Summary.setIsShortHaulExceptionAvailable(false);
		this.DAILY_DUTY_TIME_ALLOWED_WITH_SHORT_HAUL_ADDITIONAL_DRIVING = this.DAILY_DUTY_TIME_ALLOWED + (2 * MILLISECONDS_PER_HOUR);
		if (properties.getIsShortHaulExceptionAllowed())
		{
			this.SetExtendedShorthaulLimit();
		}
	}
	
	@Override
	public void PrepareStartOfLog(LogProperties logProperties)
	{
		super.PrepareStartOfLog(logProperties);
		
		Date key = DateUtility.GetDateFromDateTime(logProperties.getLogDate());
		if (!this.getDutyInfoByDate().containsKey(key))
		{
			this.getDutyInfoByDate().put(key, new DutyInfo());
		}
		
		DutyInfo info = this.getDutyInfoByDate().get(key);
		info.setHasReturnedToWorkLocation(logProperties.getHasReturnedToWorkLocation());
		info.setIsHaulingExplosives(logProperties.getIsHaulingExplosives());
		info.setIsTodaysLog(logProperties.getIsTodaysLog());
		if (!logProperties.getIsTodaysLog())
		{
			info.setShortHaulExceptionUsed(logProperties.getIsShortHaulExceptionUsed() && logProperties.getExemptLogType().getValue() == ExemptLogTypeEnum.NULL);
			info.setIsNonCDLShortHaulExceptionUsed(logProperties.getIsNonCDLShortHaulExceptionUsed() && logProperties.getExemptLogType().getValue() == ExemptLogTypeEnum.EXEMPTLOGTYPE150AIRMILENONCDL);
		}
		
		if (this.getRulesetProperties().getIsShortHaulExceptionAllowed())
		{
			if (this.CanShortHaulExceptionBeUsed())
			{
				this.SetExtendedShorthaulLimit();
			}
			else
			{
				this.SetStandardDutyLimit();
			}
		}

        boolean isActive_34HrReset = _complianceDatesController.IsLogCheckerComplianceDateActive(LogCheckerComplianceDatesTypeEnum.DEC2014_UNENFORCE34HRRESET, logProperties.getLogDate(), false);
		if(isActive_34HrReset){	
			if(!logProperties.getIsTodaysLog()){
				info.setIsWeeklyResetUsed(logProperties.getIsWeeklyResetUsed());
			}
			info.setIsWeeklyResetUsedOverridden(logProperties.getIsWeeklyResetUsedOverridden());	
			if (logProperties.getIsFirstLogInCollection() && logProperties.getLastUsedWeeklyResetStartTimestamp() != null)
			{
				this.setWeeklyResetStartingTimestamp(logProperties.getLastUsedWeeklyResetStartTimestamp());
				this.setWeeklyResetEndingTimestamp(this.calculateEndingWeeklyTimestamp(this.getWeeklyResetStartingTimestamp()));
			}
			else if(logProperties.getWeeklyResetStartTimestamp() != null && info.getIsWeeklyResetUsed())
			{
				this.setWeeklyResetStartingTimestamp(logProperties.getWeeklyResetStartTimestamp());
				this.setWeeklyResetEndingTimestamp(this.calculateEndingWeeklyTimestamp(this.getWeeklyResetStartingTimestamp()));
			}
		}
	}

	@Override
    public Bundle FetchAtEndOfLog(Date logDate)
    {
		Bundle bundle = super.FetchAtEndOfLog(logDate);
        boolean isShortHaulExceptionUsed = false;
        Date key = DateUtility.GetDateFromDateTime(logDate);
        if (this.getDutyInfoByDate().containsKey(key))
        {
            isShortHaulExceptionUsed = this.getDutyInfoByDate().get(key).getShortHaulExceptionUsed();
        }
        bundle.putBoolean(SHORT_HAUL_EXCEPTION_USED, isShortHaulExceptionUsed);
        
        bundle.putLong(WEEKLY_RESET_START_TIMESTAMP, 0);
        if (this.getDutyInfoByDate().containsKey(key))
        {
        	DutyInfo dutyInfo = this.getDutyInfoByDate().get(key);
        	Date weeklyResetStartTimestamp = dutyInfo.getWeeklyResetStartTimestamp();
            if(weeklyResetStartTimestamp != null){
            	bundle.putLong(WEEKLY_RESET_START_TIMESTAMP, weeklyResetStartTimestamp.getTime());
            }
            bundle.putBoolean(WEEKLY_RESET_USED, dutyInfo.getIsWeeklyResetUsed());
        }
             
        return bundle;
    }

    /**
     * Answer the duty summary of the WEEKLY rules
     */
	@Override
    public Bundle WeeklyDutySummary(HoursOfServiceSummary summary)
    {
        Bundle bundle = super.WeeklyDutySummary(summary);
        if (!bundle.containsKey(REGSECTION) || bundle.getString(REGSECTION) == null || bundle.getString(REGSECTION).equals("")) 
        	bundle.putString(REGSECTION, REGULATORY_SECTION_WEEKLY_DUTY);
        
        return bundle;
    }

    /**
     * Answer the duty summary of the DAILY Duty rules.
     */
	@Override
    public Bundle DailyDutySummary(HoursOfServiceSummary summary)
    {
        Bundle bundle = super.DailyDutySummary(summary);
        if (!bundle.containsKey(REGSECTION) || bundle.getString(REGSECTION) == null || bundle.getString(REGSECTION).equals("")) 
        	bundle.putString(REGSECTION, REGULATORY_SECTION_DAILY_DUTY);
        
        return bundle;
    }

    /**
     * Answer the duty summary of the DAILY Driving rules
     */
	@Override
    public Bundle DailyDriveSummary(HoursOfServiceSummary summary)
    {
        Bundle bundle = super.DailyDriveSummary(summary);
        if (!bundle.containsKey(REGSECTION) || bundle.getString(REGSECTION) == null || bundle.getString(REGSECTION).equals("")) 
        	bundle.putString(REGSECTION, REGULATORY_SECTION_DAILY_DRIVE);       
        
        return bundle;
    }

	/**
	 * Answer the duty summary of the DAILY Driving rules for Rest Break
	 */
	@Override
    public Bundle DailyDriveRestBreakSummary(HoursOfServiceSummary summary)
    {
        Bundle bundle = null;
        
        if(this.getRulesetProperties().getIs8HourDrivingRuleEnabled()){
        	Bundle driveTimeBundle = this.DailyDriveSummary(summary);
        	
        	int allowedHours = (int)(this.DAILY_DRIVE_TIME_ALLOWED_8_HOUR_RULE/MILLISECONDS_PER_HOUR);
        	driveTimeBundle.putInt(ALLOWED, allowedHours);
        	driveTimeBundle.putString(REGSECTION, REGULATORY_SECTION_DAILY_DRIVE_8HOUR);
        	
        	// for the proposed 8 hour driving rule tentatively planned for 7/1/2013, the available driving time is the lesser of:
        	// 1. the available drive time against the 8 hour rule
        	// 2. the available drive time against the 11 hour rule
        	
        	// The value currently in the bundle will the drive time against the standard rule (11 hour)
        	long driveAvailStandardRule = driveTimeBundle.getLong(AVAIL);
        	
        	// now, determine the drive time available against the 8 hour rule
    		long used = summary.getDriveTimeAccumulated8HourRule();
    		long driveAvailable8HourRule = 0;
    		if (this.DAILY_DRIVE_TIME_ALLOWED_8_HOUR_RULE > used)
    		{
    			driveAvailable8HourRule = this.DAILY_DRIVE_TIME_ALLOWED_8_HOUR_RULE - used;
    		}    		
    		if (driveAvailable8HourRule < 0) driveAvailable8HourRule = 0;    		
    		
    		if(driveAvailable8HourRule < driveAvailStandardRule){
    			// the drive time available under the 8 hour rule is less than the standard drive time, so replace the available driving time    		    			
    			driveTimeBundle.putLong(USED, used);
    			driveTimeBundle.putLong(AVAIL, driveAvailable8HourRule);    			    		    		
    		} 
    		else{
    			// the standard driving rule is in force now, so do not return any values for the rest break rules of USED/AVAIL
    			driveTimeBundle.remove(USED);
    			driveTimeBundle.remove(AVAIL);
    		}
    		bundle = driveTimeBundle;
        }
        
        return bundle;
    }
	
	/**
	 * Determine if a daily hour reset has occurred.
	 * When a daily reset occurs, then the daily hours in the summary are reset to zero.
	 */
	@Override
    protected void CalculateDailyReset(long length)
    {
        this.Summary.setDailyResetAmount(this.Summary.getDailyResetAmount() - length);
        if (DateUtility.ConvertMillisecondsToMinutes(this.Summary.getDailyResetAmount()) <= 0.0)
        {
        	this.MarkDailyReset();
        }
    }
	
	/**
	 * Determine if a weekly hour reset has occurred.
	 * When the reset occurs, then the summary is completely reset to zero.
	 * Also, allow the short-haul exception to be used again.
	 */
	@Override
    protected void CalculateWeeklyReset(long length)
    {
        this.Summary.setWeeklyResetAmount(this.Summary.getWeeklyResetAmount() - length);
        
        long startTimestamp;
		if (this.getOriginalValidWeeklyResetDutyStartTimestamp() != null)
			startTimestamp = this.getOriginalValidWeeklyResetDutyStartTimestamp().getTime();
		else
			startTimestamp = this.Summary.getRecentDutyTimestamp().getTime();
        long endTimestamp = this.Summary.getRecentDutyTimestamp().getTime() + this.Summary.getRecentDutyLength();        
        
		if (this.IsWeeklyResetValid(startTimestamp, endTimestamp))
		{
			DutyInfo dutyInfo = this.GetCurrentDutyInfo();
			if (!dutyInfo.getIsWeeklyResetUsedOverridden())
			{
				this.Summary.setDriveTimeAccumulated(0);
				this.Summary.setDutyTimeAccumulated(0);
				this.Summary.setWeeklyDutyTimeAccumulated(0);
				this.MarkSleeperCombinationReset();

				if (this.getRulesetProperties().getIs8HourDrivingRuleEnabled())
				{
					this.Summary.setDriveTimeAccumulated8HourRule(0);
				}

				this.ResetDailyDutyTotals();
				this.MarkWeeklyReset(new Date(startTimestamp));
				this.Summary.setConsecutiveOffDutyAccumulated(0);
			}
		}
    }

	protected boolean IsWeeklyResetValid(long startTimestamp, long endTimestamp)
	{
		boolean isValid = false;
		
		if(this.getRulesetProperties().getIs34HourResetAllowed() && DateUtility.ConvertMillisecondsToMinutes(this.Summary.getWeeklyResetAmount()) <= 0.0)
		{						
			// determine if 34 hour reset provisions required as of the endTimestamp
			boolean isActive_34HrReset = _complianceDatesController.IsLogCheckerComplianceDateActive(LogCheckerComplianceDatesTypeEnum.DEC2014_UNENFORCE34HRRESET, new Date(endTimestamp), false);
			
			if(isActive_34HrReset)
			{
		        DutyInfo dutyInfo = this.GetCurrentDutyInfo();
				boolean doesContainTwoMorningPeriods = this.DoesPeriodContainTwoMornings(startTimestamp, endTimestamp);
				
				Date startDate = new Date(startTimestamp);
				
		        if (doesContainTwoMorningPeriods)
		        {
			        // because there is a valid reset here, make sure to record the fact that the reset exists
		        	dutyInfo.setWeeklyResetStartTimestamp(startDate);
		        }
				
		    	// 2014.09.08 sjn - Removed due to TC55 performance of string.format with dates
		        //Log.d(TAG, String.format("PropertyCarrying.IsWeeklyResetValid from: {%s} to: {%s} prevResetStart: {%s} prevResetEnd: {%s}", startDate, endDate, previousResetStart, previousResetEnd));
				
				// first, verify that the reset has only occurred once within the last 168 hours
				if (this.getWeeklyResetStartingTimestamp() != null)
				{
					// calculate the time period of the next weekly reset
					long dateForNextValidResetToStart = this.getWeeklyResetStartingTimestamp().getTime() + WEEKLY_DUTY_RESET_MINIMUM_ALLOWED_BETWEEN_RESETS;
					
					long dateForNextValidResetToEnd = dateForNextValidResetToStart + this.WEEKLY_OFFDUTY_HOURS_FOR_RESET;
					
			    	// 2014.09.08 sjn - Removed due to TC55 performance of string.format with dates
					//Log.d(TAG, String.format("PropertyCarrying.IsWeeklyResetValid nextResetStart: {%s} nextResetEnd: {%s}", nextResetStart, nextResetEnd));
					//Log.d(TAG, String.format("PropertyCarrying.IsWeeklyResetValid is weekly reset used? %s", (dutyInfo.getIsWeeklyResetUsed() ? "Yes" : "No")));
					
					if (startTimestamp <= dateForNextValidResetToStart && endTimestamp >= dateForNextValidResetToEnd)
						// if the current log event completely contains the next valid reset period
						isValid = true;
					else if (endTimestamp >= dateForNextValidResetToEnd)
						//  if it's not completely contained, then at least make sure the logEvent ends after the reset period
						isValid = true;
					else if (dutyInfo.getIsWeeklyResetUsed())
						// if the duty info says that the reset is used, then the reset is valid.
						// This can happen when the log gets downloaded from DMO and the reset flag is set by DMO.
						isValid = true;
				}
				else
				{
					// this is the first time we've reset
					// this will be valid as long is there is not an override that exists in the time period of the reset
					// when an override exists, it means that the reset period should not be used.
					boolean doesOverrideExist = this.DoesWeeklyResetOverrideExists(startTimestamp, endTimestamp);
					if(doesOverrideExist)
						isValid = false;
					else
						isValid = true;
				}
				
				// if valid so far, then check to see if the period spans 2 periods from 1:00AM - 5:00AM
				if (isValid)
				{
					isValid = dutyInfo.getIsWeeklyResetUsed() || doesContainTwoMorningPeriods;
				}
			}
			else
			{
				// the new 34 hour reset rules are not being enforced yet, so it's always valid
				isValid = true;
			}
		}
		
    	// 2014.09.08 sjn - Removed due to TC55 performance 
		//Log.d(TAG, String.format("PropertyCarrying.IsWeeklyResetValid isValid: {%s} ", isValid));
		return isValid;
	}
	
	/**
	 * Answer if there is a DutyInfo for a date that occurs between the start and ending timestamps where the dutyInfo.override is true and the dutyInfo.weeklyResetUsed is false
	 * This means that the reset is overridden to be off for that period.
	 */
	private boolean DoesWeeklyResetOverrideExists(long startTimestamp, long endTimestamp){		
		if(this.getDutyInfoByDate() == null) return false;
				
		boolean answer = false;
		// iterate through each date stored in the dutyInfo list
		// For each dutyInfo of a date that is between the start and end, look for an override that may exists
		Enumeration<Date> list = this.getDutyInfoByDate().keys();
		while(list.hasMoreElements()){
			Date dte = list.nextElement();
			long dteValue = dte.getTime();
			if(startTimestamp<= dteValue && dteValue <= endTimestamp){
				DutyInfo dutyInfo = this.getDutyInfoByDate().get(dte);
				if(dutyInfo.getIsWeeklyResetUsedOverridden() && !dutyInfo.getIsWeeklyResetUsed())
					return true;
			}
		}
		
		return answer;
	}
	
	/**
	 * Answer if the period contains 2 period of time 1-5 AM
	 */
	private boolean DoesPeriodContainTwoMornings(long start, long end)
	{
		Calendar startCal = Calendar.getInstance();
		startCal.setTimeInMillis(start);

		Calendar endCal = Calendar.getInstance();
		endCal.setTimeInMillis(end);

		Calendar oneAMCal = Calendar.getInstance();
		oneAMCal.setTimeInMillis(start);
		oneAMCal.set(Calendar.HOUR, 1);
		oneAMCal.set(Calendar.MINUTE, 0);
		oneAMCal.set(Calendar.SECOND, 0);
		oneAMCal.set(Calendar.AM_PM, 0);

		Calendar fiveAMCal = Calendar.getInstance();
		fiveAMCal.setTimeInMillis(start);
		fiveAMCal.set(Calendar.HOUR, 5);
		fiveAMCal.set(Calendar.MINUTE, 0);
		fiveAMCal.set(Calendar.SECOND, 0);
		fiveAMCal.set(Calendar.AM_PM, 0);

		int countOf1AM = 0;
		int countOf5AM = 0;
		while (oneAMCal.compareTo(endCal) < 0)
		{
			if (oneAMCal.compareTo(startCal) >= 0 && oneAMCal.compareTo(endCal) <= 0)
				countOf1AM++;
			if (fiveAMCal.compareTo(startCal) >= 0 && fiveAMCal.compareTo(endCal) <= 0)
				countOf5AM++;

			oneAMCal.add(Calendar.DAY_OF_MONTH, 1);
			fiveAMCal.add(Calendar.DAY_OF_MONTH, 1);
		}

		return countOf1AM >= 2 && countOf5AM >= 2;
	}
	
	/**
	 * Process an on-duty status event.
	 * Accumulate the duty time accordingly.
	 */
	@Override
    protected void ProcessOnDuty(long length)
    {
        this.Summary.setDutyTimeAccumulated(this.Summary.getDutyTimeAccumulated() + length);
        this.Summary.setWeeklyDutyTimeAccumulated(this.Summary.getWeeklyDutyTimeAccumulated() + length);
        this.Summary.setDailyResetAmount(DAILY_OFFDUTY_HOURS_FOR_RESET);
        this.Summary.setWeeklyResetAmount(WEEKLY_OFFDUTY_HOURS_FOR_RESET);
        this.setOriginalValidWeeklyResetDutyStartTimestamp(null);

        boolean shouldOffDutyTimeReset = true;
        if(this.getRulesetProperties().getIs8HourDrivingRuleEnabled()){
        	boolean isHaulingExplosives = false;

            Date logDate = DateUtility.GetDateFromDateTime(this.Summary.getLogStartTimestamp());  
            if (this.getDutyInfoByDate().containsKey(logDate))
            {
                DutyInfo info = this.getDutyInfoByDate().get(logDate);  
                isHaulingExplosives = info.getIsHaulingExplosives();
            }

            if(isHaulingExplosives && this.Summary.getRecentDutyStatus() == DutyStatusEnum.ON && this.Summary.getHasDrivingOccurredAfterDailyReset()){
            	// when hauling explosives, the on-duty time is processed like off-duty time (with respect to the 30 minute break provision) after some driving time has been logged for the day
            	this.ProcessOffDutyFor8HourDrivingRule(length);
            	shouldOffDutyTimeReset = false;
            }            	
            else
	        	// for the proposed 8 hour driving rule, duty time (which includes both OnDuty and Driving) counts against the 8 hours of available driving
	        	this.Summary.setDriveTimeAccumulated8HourRule(this.Summary.getDriveTimeAccumulated8HourRule() + length);
        }
        
        this.MarkAsDutyTour();

        this.Summary.getCombinableOffDutyPeriod().ProcessOnDutyTime(length);
        if(shouldOffDutyTimeReset)
        	this.Summary.setConsecutiveOffDutyAccumulated(0);
        
        if (!_processFirstOnDutyEvent)
        	_processFirstOnDutyEvent = true;
    }

	/**
	 * Process the driving status event.
	 * Accumulate the driving time as an on-duty event also.
	 */
	@Override
    protected void ProcessDriveDuty(long length)
    {
        // process as an on-duty event first
        this.ProcessOnDuty(length);

        this.Summary.setHasDrivingOccurredAfterDailyReset(true);
        
        // accumulate driving specific numbers
        this.Summary.setDriveTimeAccumulated(this.Summary.getDriveTimeAccumulated() + length);
        
        // determine if a short-haul exception should be used
        if (this.CanShortHaulExceptionBeUsed())
        {
            this.MarkShortHaulExceptionUsed();
        }

        this.Summary.getCombinableOffDutyPeriod().ProcessDriveTime(length);
    }

	/**
	 * Process the off-duty event.
	 * Determine if a split-sleeper period can be combined for a full 10-hour break
	 */
	@Override
    protected void ProcessOffDuty(long length)
    {
        if (DateUtility.ConvertMillisecondsToHours(length) < 10.0)
        {
            // when less than 10 hours, accumulate as a duty event
            boolean hasCombinedSleeper = this.Summary.getCombinableOffDutyPeriod().ProcessOffDutyTime(length, this.Summary);
            if(hasCombinedSleeper)
            	this.Summary.setHasDrivingOccurredAfterDailyReset(false);
            
            this.Summary.setDutyTimeAccumulated(this.Summary.getDutyTimeAccumulated() + length);
        }
        else
        {
        	boolean hasCombinedSleeper = this.Summary.getCombinableOffDutyPeriod().ProcessOffDutyTime(length, this.Summary);
            if(hasCombinedSleeper)
            	this.Summary.setHasDrivingOccurredAfterDailyReset(false);
        }
        
        this.ProcessOffDutyFor8HourDrivingRule(length);
        
        if (this.getOriginalValidWeeklyResetDutyStartTimestamp() == null)
        {
        	this.setOriginalValidWeeklyResetDutyStartTimestamp(this.Summary.getRecentDutyTimestamp());
        }
        
        this.CalculateDailyReset(length);
        this.CalculateWeeklyReset(length);
    }

	/**
	 * Process the sleeper status.
	 */
	@Override
    protected void ProcessSleepDuty(long length)
    {
        if (DateUtility.ConvertMillisecondsToHours(length) < 8.0)
        {
            // when the length of the sleeper is less than 8 hours, process as an off-duty
            this.ProcessOffDuty(length);
        }
        else
        {
        	this.ProcessOffDutyFor8HourDrivingRule(length);
        	
        	boolean hasCombinedSleeper = this.Summary.getCombinableOffDutyPeriod().ProcessSleeperTime(length, this.Summary);            
            if(hasCombinedSleeper)
            	this.Summary.setHasDrivingOccurredAfterDailyReset(false);

            if (this.getOriginalValidWeeklyResetDutyStartTimestamp() == null)
            {
            	this.setOriginalValidWeeklyResetDutyStartTimestamp(this.Summary.getRecentDutyTimestamp());
            }
            
            this.CalculateDailyReset(length);
            this.CalculateWeeklyReset(length);
        }
    }

	@Override
	protected void ProcessOffDutyWellsite(long length)
	{		
	}

	private void ProcessOffDutyFor8HourDrivingRule(long length)
	{
        if(this.getRulesetProperties().getIs8HourDrivingRuleEnabled())
        {
        	// for the proposed 8 hour driving rule, if the off duty period is less than 15 minutes, it counts against the available driving time
        	// otherwise it resets the available driving time back to 8 hours
        	
        	// keep track of the consecutive off duty periods, because when combined, they might qualify
        	this.Summary.setConsecutiveOffDutyAccumulated(this.Summary.getConsecutiveOffDutyAccumulated() + length);

	        if(DateUtility.ConvertMillisecondsToMinutes(this.Summary.getConsecutiveOffDutyAccumulated()) < 30.0)
	        {
	        	// accumulate the OffDuty time against the 8 hour driving rule when it's less than 30 minutes
	        	this.Summary.setDriveTimeAccumulated8HourRule(this.Summary.getDriveTimeAccumulated8HourRule() + length);
	        }
	        else
	        {
	        	// At least a 30 minute break detected, so reset the 8 hour rule's driving time accumulator
	        	this.Summary.setDriveTimeAccumulated8HourRule(0);
	        	this.Summary.setConsecutiveOffDutyAccumulated(0);
	        }
        }
	}
	
	/**
	 * Keep track of the fact that there is duty time on this log.
	 * This log is now considered a duty tour.
	 * This means that the use of the short haul exception may be allowed
	 * because there is duty time.
	 */
    protected void MarkAsDutyTour()
    {
        Date key = DateUtility.GetDateFromDateTime(this.Summary.getRecentDutyTimestamp());
        if (!this.getDutyInfoByDate().containsKey(key))
        {
            this.getDutyInfoByDate().put(key, new DutyInfo());
        }
        this.getDutyInfoByDate().get(key).setIsDutyTour(true);

        // determine if the short-haul exception period has expired
        if (this.getRulesetProperties().getIsShortHaulExceptionAllowed() && this.Summary.getIsShortHaulExceptionAvailable())
        {
            // the short-haul period ends when there is more daily duty time accumulated
            // then what is allowed
            if (this.Summary.getDutyTimeAccumulated() > this.DAILY_DUTY_TIME_ALLOWED)
            {
                this.SetStandardDutyLimit();
            }
        }
    }

    /**
     * Mark that the short haul exception was used on the current log date.
     * The end of the duty period that claimed the short-haul exception is the
     * log date used.
     */
    private void MarkShortHaulExceptionUsed()
    {
        // if we are over the standard duty 
        if (this.Summary.getWeeklyDutyTimeAccumulated() <= this.WEEKLY_DUTY_TIME_ALLOWED &&
            this.Summary.getDutyTimeAccumulated() > this.DAILY_DUTY_TIME_ALLOWED_STANDARD &&
            this.Summary.getDutyTimeAccumulated() <= this.DAILY_DUTY_TIME_ALLOWED_WITH_SHORT_HAUL_ADDITIONAL_DRIVING)
        {
            Date key = DateUtility.GetDateFromDateTime(new Date(this.Summary.getRecentDutyTimestamp().getTime() +  this.Summary.getRecentDutyLength()));
            if (!this.getDutyInfoByDate().containsKey(key))
            {
                this.getDutyInfoByDate().put(key, new DutyInfo());
            }
            this.getDutyInfoByDate().get(key).setShortHaulExceptionUsed(true);
        }
    }

    private void MarkWeekyDutyStartTimestamp()
    {
        Date key = DateUtility.GetDateFromDateTime(new Date(this.Summary.getRecentDutyTimestamp().getTime() +  this.Summary.getRecentDutyLength()));
        if (!this.getDutyInfoByDate().containsKey(key))
        {
            this.getDutyInfoByDate().put(key, new DutyInfo());
        }
        this.getDutyInfoByDate().get(key).setWeeklyResetStartTimestamp(this.getWeeklyResetStartingTimestamp());        
	}   

    /**
     * Mark that the daily reset has occurred
     */
	private void MarkDailyReset(){
        this.Summary.setDriveTimeAccumulated(0);
        this.Summary.setDutyTimeAccumulated(0);
        this.MarkSleeperCombinationReset();
        if(this.getRulesetProperties().getIs8HourDrivingRuleEnabled()){
        	this.Summary.setDriveTimeAccumulated8HourRule(0);
        }
        this.Summary.setConsecutiveOffDutyAccumulated(0); 
        this.Summary.setHasDrivingOccurredAfterDailyReset(false);
	}

	/**
	 * Mark that a weekly reset has occurred
	 */
    protected void MarkWeeklyReset(Date weeklyResetStart)
    {    	
		Date currentLogEventEnd = new Date(this.Summary.getRecentDutyTimestamp().getTime() +this.Summary.getRecentDutyLength());
		
    	// 2014.09.08 sjn - Removed due to TC55 performance of string.format with dates
		//Log.d(TAG, String.format("PropertyCarrying.MarkWeeklyReset weeklyResetStart: {%s} logEventEnd: {%s} ", weeklyResetStart, currentLogEventEnd));
		
		// set weeklyResetEndingTime to end of current log event
		Date weeklyResetEndingTime = currentLogEventEnd;
		
		// if 34 hour provisions are active at weeklyResetStart - calculate optimal weekly reset ending time
		boolean isActive_34HrReset_AtResetEnd = _complianceDatesController.IsLogCheckerComplianceDateActive(LogCheckerComplianceDatesTypeEnum.DEC2014_UNENFORCE34HRRESET, weeklyResetEndingTime, false);
		if (isActive_34HrReset_AtResetEnd)
			weeklyResetEndingTime = this.CalculateOptimalWeeklyResetEnd(weeklyResetStart.getTime(), currentLogEventEnd.getTime());
		
        this.setWeeklyResetEndingTimestamp(weeklyResetEndingTime);   		
        
        // if 34 hour provisions are active at calculated weekly reset end time, set weekly reset start timestamp
		if (_processFirstOnDutyEvent && isActive_34HrReset_AtResetEnd)
		{
		   	// working backward from the end of the reset, determine the exact start of the reset 
	    	this.setWeeklyResetStartingTimestamp(new Date(weeklyResetEndingTime.getTime() - this.WEEKLY_OFFDUTY_HOURS_FOR_RESET));
	    	this.MarkWeekyDutyStartTimestamp();
		}
		
        // determine if the short-haul exception is allowed on the day of the
        // weekly reset.  Typically it is allowed on that day, but
        // if the driver does not return the the work location on the day of the 
        // weekly reset then the short-haul exception is not available.
        Date key = DateUtility.GetDateFromDateTime(this.getWeeklyResetEndingTimestamp());		
        if (!this.getDutyInfoByDate().containsKey(key))
        {
        	this.getDutyInfoByDate().put(key, new DutyInfo());
        }
        DutyInfo info = this.getDutyInfoByDate().get(key);
        
        if (CanShortHaulExceptionBeUsed())
        { 
            // the short-haul exception is allowed for use going forward
            this.SetExtendedShorthaulLimit();
        }
        
        // if 34 hour 
        if(isActive_34HrReset_AtResetEnd)
        {
        	info.setIsWeeklyResetUsed(true);
        	info.setIsWeeklyResetUsedOverridden(false);
        }
    }
    
    /**
     * Reset the sleeper combination
     */
    protected void MarkSleeperCombinationReset()
    {
    	this.Summary.setCombinableOffDutyPeriod(SplitSleeperCombination.ForPropertyCarryingRules());
    }

    /**
     * Answer the ending date that creates an optimal weekly reset period.
     * An optimal reset period is the earliest possible reset period after the startDate that contains 2 mornings (1-5AM periods)
     * @param startTimestamp The start of the range that contains the weekly reset
     * @param endTimestamp The end of the range that contains the weekly reset
     * @return
     */
	private Date CalculateOptimalWeeklyResetEnd(long startTimestamp, long endTimestamp)
	{
		// by definition the first possible period will always be 34 hours after the startdate
		long answer = startTimestamp + this.WEEKLY_OFFDUTY_HOURS_FOR_RESET;

		if (!this.IsWeeklyResetValid(startTimestamp, answer))
		{
			// the first period is not a valid reset

			// create a date which is the date of the answer, but with the time of the current reset
			Calendar optimalEndingCal = Calendar.getInstance();
			optimalEndingCal.setTimeInMillis(answer);

			Date e = this.getWeeklyResetEndingTimestamp();
			if (e != null)
			{
				optimalEndingCal.set(Calendar.HOUR, e.getHours());
				optimalEndingCal.set(Calendar.MINUTE, e.getMinutes());
				optimalEndingCal.set(Calendar.SECOND, 0);
				int am_pm = Calendar.AM;
				if (e.getHours() > 12)
					am_pm = Calendar.PM;
				optimalEndingCal.set(Calendar.AM_PM, am_pm);
			}

			while (!this.IsWeeklyResetValid(startTimestamp, optimalEndingCal.getTimeInMillis()))
			{
				// if the period of time is not a valid reset, add 24 hours to try the next day
				optimalEndingCal.add(Calendar.HOUR, 24);
			}

			if (optimalEndingCal.getTimeInMillis() < endTimestamp)
				// guard against the case where the optimal time has moved past the ending timestamp.
				// this might happen because of the add 24 hours loop above
				answer = optimalEndingCal.getTimeInMillis();
			else
				answer = endTimestamp;
			
			// Now check if we can get closer to 5 AM
			optimalEndingCal.setTimeInMillis(answer);
			optimalEndingCal.set(Calendar.HOUR_OF_DAY, 5);
			optimalEndingCal.set(Calendar.MINUTE, 0);
			while (optimalEndingCal.getTimeInMillis() < endTimestamp)
			{
				if (this.IsWeeklyResetValid(startTimestamp, optimalEndingCal.getTimeInMillis()))
				{
					// Found a valid reset closer to 5 AM
					answer = optimalEndingCal.getTimeInMillis();
					break;
				}
				optimalEndingCal.add(Calendar.HOUR_OF_DAY, 1);
			}
		}
		return new Date(answer);
	}
    
    /**
     * Answer if the short haul exception can be used on the current log date.
     * The following conditions are verified:
     * 1. The short-haul exception feature is enabled for the driver
     * 2. The driver has returned to the work location during the previous 5 duty tours.
     * 3. The short-haul exception was not already used within the previous 6 days
     * @return <code>true</code> if the short haul exception can be used, and <code>false</code> otherwise
     */
    protected boolean CanShortHaulExceptionBeUsed()
    {
        boolean canExceptionBeUsed = false;

        if (this.getRulesetProperties().getIsShortHaulExceptionAllowed())
        {
            canExceptionBeUsed = true;
            Date logDate = DateUtility.GetDateFromDateTime(this.Summary.getLogStartTimestamp());
            boolean done = false;
            int logDaysExamined = 0;
            int dutyTours = 0;

            // unless working with today's log, the short haul exception can not be used
            if (this.getDutyInfoByDate().containsKey(logDate))
            {
                if (!this.getDutyInfoByDate().get(logDate).getIsTodaysLog())
                {
                    done = true;
                    canExceptionBeUsed = false;
                }
            }

            boolean checkShorthaulUsed = true;
            boolean checkDutyTours = true;
            while (!done)
            {
                // try to look chronologically backwards for one of the following:
                // 1. a weekly reset
                // 2. a short-haul exception that's used within the last 7 days (excluding today)
                // 3. during the 5 previous duty tours, is there a returnedToWorkLocation=false
                logDaysExamined++;
                
                // 2014.09.25 sjn - Populate the DutyInfoByDate if it doesn't exist
                if(this.getDutyInfoByDate().containsKey(logDate) == false)
                {
                	// if it doesn't exist it means that it is outside of the normal audit date range
                	// try to read the log from the db, and populate the duty info on the fly
                	IEmployeeLogFacade empLogFacade = new EmployeeLogFacade(GlobalState.getInstance().getApplicationContext(), GlobalState.getInstance().getCurrentUser());
                	EmployeeLog empLog = null;
                	
                	try{
                		empLog = empLogFacade.GetLogByDate(logDate);
                	}
                	catch(Exception ex){
                		// if something bad happens getting the log, just ignore it
                		// this catch handler is primarily in place to allow the unit tests to run
                	}
                	
                	if(empLog != null){
                		DutyInfo dutyInfo = new DutyInfo();
                		
                		// set the duty info properties from this log
                		dutyInfo.setHasReturnedToWorkLocation(empLog.getHasReturnedToLocation());
                		dutyInfo.setShortHaulExceptionUsed(empLog.getIsShortHaulExceptionUsed());
                		dutyInfo.setIsDutyTour(EmployeeLogUtilities.ContainsDrivingOrOnDutyEvent(empLog));
                		dutyInfo.setIsNonCDLShortHaulExceptionUsed(empLog.getIsNonCDLShortHaulExceptionUsed());
                		
                		this.getDutyInfoByDate().put(logDate, dutyInfo);
                	}
                	else
                	{
                		// no log was found for the date, are there any more in the db that are earlier than this logDate
                		boolean anyMoreLogsExist = false;
                		try{
                			anyMoreLogsExist = empLogFacade.ExistAnyLogEarlierThan(logDate);
                		}
                    	catch(Exception ex){
                    		// if something bad happens getting the log, just ignore it
                    		// this catch handler is primarily in place to allow the unit tests to run
                    	}
                		
                		if(anyMoreLogsExist == false){
                			// no more exist in the database, check to see if they're in the duty info list
                			// this is primarily so that the unit tests can load up the duty info array
                			// try to detect an earlier logDate in the dutyInfo list than the current one we're working with
                			for(Enumeration<Date> e = this.getDutyInfoByDate().keys(); e.hasMoreElements();)
                			{
                				Date dte = e.nextElement();
                				if(dte.compareTo(logDate) < 0){
                					// found one
                					anyMoreLogsExist = true;
                					break;
                				}
                			}
                		}
                		
                		if(anyMoreLogsExist == false){
                			// if no more logs exist earlier than this in the database, or in the dutyInfo array, then we're done
                			done = true;
                		}
                	}
                }
                
                
                if (this.getDutyInfoByDate().containsKey(logDate))
                {
                    DutyInfo info = this.getDutyInfoByDate().get(logDate);
                    if (info.getIsDutyTour()) dutyTours++;
                    if (!done && checkShorthaulUsed)
                    {
                    	if (logDaysExamined <= 7 && info.getShortHaulExceptionUsed() && !info.getIsTodaysLog())
                        {
                            // found a usage of the short-haul exception within the last 7 days
                            // and it's not today's log
                            canExceptionBeUsed = false;
                            done = true;
                        }
                    }
                    if (!done && info.getIsTodaysLog() && !info.getHasReturnedToWorkLocation())
                    {
                        // if current log hasn't returned to current location, exception cannot be used
                        canExceptionBeUsed = false;
                        done = true;
                    }
                    if (!done && checkDutyTours && info.getIsDutyTour())
                    {
                        if (!info.getHasReturnedToWorkLocation() && dutyTours < 6)
                        {
                            // found a duty tour where the driver did not return to the 
                            // normal work location
                        	// the current log (today's) is considered a duty tour, so need to look for 6 total tours
                            canExceptionBeUsed = false;
                            done = true;
                        }
                        else if (dutyTours >= 6)
                        {
                            // this means that 5 previous duty tours have all returned to 
                            // work location without detecting - this requirement is met,
                            // no longer need to check anymore duty tours
                            checkDutyTours = false;
                        }
                    }

                    // if we find a weekly reset, we no longer need to check if the short haul exception 
                    // has been used 
                    if (!done && checkShorthaulUsed && this.getWeeklyResetEndingTimestamp() != null && DateUtility.GetDateFromDateTime(this.getWeeklyResetEndingTimestamp()).compareTo(logDate) == 0)
                    {
                        checkShorthaulUsed = false;
                    }
                }
                logDate = DateUtility.AddDays(logDate, -1);
                
                // 2014.09.25 sjn - this needs to change.  We need to try and evaluate the 5 previous duty tours, but that means that more logs the WeeklyDutyPeriodDays
                //if (logDaysExamined >= this.WEEKLY_DUTY_PERIOD_DAYS) done = true;   
                
                // once we've processed enough duty tours, then we're done
                // because the current log is considered a tour also, we need to look at 6 tours instead of 5
                if(!done && dutyTours >= 6) done = true;

                // if we figure out that the exception cannot be used, then we're done
                if(!done && !canExceptionBeUsed) done = true;
            }
            
            // 5/30/14 JHM - If 5 duty tours have not been evaluated, short haul is not applicable
            if (dutyTours < 5) canExceptionBeUsed = false;
        }

        return canExceptionBeUsed;
    }

    /**
     * Shorthaul exception just became available - allow use of it and set extended duty hours
     */
    private void SetExtendedShorthaulLimit()
    {
        this.Summary.setIsShortHaulExceptionAvailable(true);
        this.DAILY_DUTY_TIME_ALLOWED = this.DAILY_DUTY_TIME_ALLOWED_WITH_SHORT_HAUL_ADDITIONAL_DRIVING;
    }

    /**
     * Shorthaul exception cannot be used - don't allow use and set standard limit
     */
    private void SetStandardDutyLimit()
    {
        this.Summary.setIsShortHaulExceptionAvailable(false);
        this.DAILY_DUTY_TIME_ALLOWED = this.DAILY_DUTY_TIME_ALLOWED_STANDARD;
    }
	
    private Hashtable<Date, DutyInfo> _dutyInfoByDate;    
    protected Hashtable<Date, DutyInfo> getDutyInfoByDate()
    {
    	return this._dutyInfoByDate;
    }
    protected void setDutyInfoByDate(Hashtable<Date, DutyInfo> dutyInfoByDate)
    {
    	this._dutyInfoByDate = dutyInfoByDate;
    }

    protected DutyInfo GetCurrentDutyInfo(){
    	Date key = this.getLogProperties().getLogDate();
        if (!this.getDutyInfoByDate().containsKey(key))
        {
            this.getDutyInfoByDate().put(key, new DutyInfo());
        }
        return this.getDutyInfoByDate().get(key);
    }

    private Date calculateEndingWeeklyTimestamp(Date startingTimestamp)
    {
    	Date dateForNextResetToEnd = new Date(startingTimestamp.getTime() +WEEKLY_OFFDUTY_HOURS_FOR_RESET);
    	return dateForNextResetToEnd;
    }
}
