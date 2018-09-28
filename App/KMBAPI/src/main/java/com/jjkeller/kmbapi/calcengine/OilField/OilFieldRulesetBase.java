package com.jjkeller.kmbapi.calcengine.OilField;

import android.os.Bundle;

import com.jjkeller.kmbapi.calcengine.DutyInfo;
import com.jjkeller.kmbapi.calcengine.Enums.DutyStatusEnum;
import com.jjkeller.kmbapi.calcengine.HoursOfServiceSummary;
import com.jjkeller.kmbapi.calcengine.LogProperties;
import com.jjkeller.kmbapi.calcengine.RulesetBase;
import com.jjkeller.kmbapi.calcengine.RulesetProperties;
import com.jjkeller.kmbapi.controller.utility.DateUtility;

import java.util.Date;
import java.util.Hashtable;

public abstract class OilFieldRulesetBase extends RulesetBase {

	private long DAILY_DRIVE_TIME_ALLOWED_8_HOUR_RULE;
	private long DAILY_DUTY_TIME_ALLOWED_STANDARD;
	private long DAILY_DUTY_TIME_ALLOWED_WITH_SHORT_HAUL_ADDITIONAL_DRIVING;
	protected long OILFIELD_SHORTHAUL_RESET;
	
	protected abstract void MarkDailySleeperCombinationReset();

    private Hashtable<Date, DutyInfo> _dutyInfoByDate;    
    protected Hashtable<Date, DutyInfo> getDutyInfoByDate()
    {
    	return this._dutyInfoByDate;
    }
    protected void setDutyInfoByDate(Hashtable<Date, DutyInfo> dutyInfoByDate)
    {
    	this._dutyInfoByDate = dutyInfoByDate;
    }
    
	protected OilFieldRulesetBase(RulesetProperties properties)
	{
		super(properties);
	}
	
	/// <summary>
	/// Initialize the calc engine.
	/// This must be called before any other methods are called on the calc engine.
	/// NOTE:  OilFieldRulesetBase is setup to handle US and TX oilfield
	/// </summary>
	@Override
	public void Initialize(RulesetProperties properties)
	{
		super.Initialize(properties);
			
		this.WEEKLY_DUTY_TIME_ALLOWED = 70 * MILLISECONDS_PER_HOUR;
		this.WEEKLY_OFFDUTY_HOURS_FOR_RESET = 24 * MILLISECONDS_PER_HOUR;
		this.WEEKLY_DUTY_PERIOD_DAYS = 8;

		// initialize the accumulator for the proposed 8 hour driving rule tentatively going into effect 7/1/2013
		this.DAILY_DRIVE_TIME_ALLOWED_8_HOUR_RULE = 8 * MILLISECONDS_PER_HOUR;
	
		this.DAILY_DUTY_TIME_ALLOWED_STANDARD = 14 * MILLISECONDS_PER_HOUR;
		this.DAILY_DUTY_TIME_ALLOWED_WITH_SHORT_HAUL_ADDITIONAL_DRIVING = this.DAILY_DUTY_TIME_ALLOWED_STANDARD + (2 * MILLISECONDS_PER_HOUR);
		
		this.setDutyInfoByDate(new Hashtable<Date, DutyInfo>());
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
		info.setIsTodaysLog(logProperties.getIsTodaysLog());
		info.setIsHaulingExplosives(logProperties.getIsHaulingExplosives());
		info.setIsOperatesSpecificVehiclesForOilField((logProperties.getIsOperatesSpecificVehiclesForOilField()));
		info.setShortHaulExceptionUsed(logProperties.getIsShortHaulExceptionUsed());
		
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
	}
	
    /// <summary>
    /// Process the off-duty event.
    /// Determine if a split-sleeper period can be combined for a full 10-hour break
    /// </summary>
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

        this.ProcessOffDutyFor8HourDrivingRule(length);
        
        this.CalculateDailyReset(length);
        this.CalculateWeeklyReset(length);
    }

    /// <summary>
    /// Process the sleeper status.
    /// </summary>
    /// <param name="length"></param>
	@Override
    protected void ProcessSleepDuty(long length)
    {
        if (DateUtility.ConvertMillisecondsToHours(length) < 2.0)
        {
            // when the length of the sleeper is less than 8 hours, process as an off-duty
            this.ProcessOffDuty(length);
        }
        else
        {
        	this.ProcessOffDutyFor8HourDrivingRule(length);
        	
        	// if this sleeper period is not combined with another sleeper period, process as
        	// an off-duty period
        	boolean hasCombinedSleeper = this.Summary.getCombinableOffDutyPeriod().ProcessSleeperTime(length, this.Summary);            
            if (hasCombinedSleeper){
            	this.Summary.setHasDrivingOccurredAfterDailyReset(false);
            } 
            else{
            	this.Summary.setDutyTimeAccumulated(this.Summary.getDutyTimeAccumulated() + length);
            }
            
            this.CalculateDailyReset(length);
            this.CalculateWeeklyReset(length);
        }
    }

    /// <summary>
    /// Process the driving status event.
    /// Accumulate the driving time as an on-duty event also.
    /// </summary>
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

    /// <summary>
    /// Process an on-duty status event.
    /// Accumulate the duty time accordingly.
    /// </summary>
	@Override
    protected void ProcessOnDuty(long length)
    {
        this.Summary.setDutyTimeAccumulated(this.Summary.getDutyTimeAccumulated() + length);
        this.Summary.setWeeklyDutyTimeAccumulated(this.Summary.getWeeklyDutyTimeAccumulated() + length);
        this.Summary.setDailyResetAmount(DAILY_OFFDUTY_HOURS_FOR_RESET);
        this.Summary.setWeeklyResetAmount(WEEKLY_OFFDUTY_HOURS_FOR_RESET);
        this.Summary.setOilFieldShortHaulReset(OILFIELD_SHORTHAUL_RESET);
       
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
                
        this.Summary.getCombinableOffDutyPeriod().ProcessOnDutyTime(length);
        if(shouldOffDutyTimeReset)
        	this.Summary.setConsecutiveOffDutyAccumulated(0);        
    }

	@Override
	protected void ProcessOffDutyWellsite(long length)
	{
        if (DateUtility.ConvertMillisecondsToHours(length) >= 2.0 && DateUtility.ConvertMillisecondsToHours(length) < 10.0)
        {
    		// if off duty wellsite period is a combinable length (2 - 10), attempt to combine
        	// it as a sleeper - can be combined with other off duty wellsite time or
        	// sleeper time
        	this.Summary.getCombinableOffDutyPeriod().ProcessSleeperTime(length, this.Summary, true);
        	
        	// NOTE:  Do not count against duty time - off duty wellsite time is never 
        	// counted against duty time regardless of the length               
        }

        this.ProcessOffDutyFor8HourDrivingRule(length);
        
        this.CalculateDailyReset(length);
        this.CalculateWeeklyReset(length);
	}

	private void ProcessOffDutyFor8HourDrivingRule(long length){
		
        if(this.getRulesetProperties().getIs8HourDrivingRuleEnabled()){
        	// for the proposed 8 hour driving rule, if the off duty period is less than 15 minutes, it counts against the available driving time
        	// otherwise it resets the available driving time back to 8 hours
        	
        	// keep track of the consecutive off duty periods, because when combined, they might qualify
    		this.Summary.setConsecutiveOffDutyAccumulated(this.Summary.getConsecutiveOffDutyAccumulated() + length);

	        if(DateUtility.ConvertMillisecondsToMinutes(this.Summary.getConsecutiveOffDutyAccumulated()) < 30.0){
	        	// accumulate the OffDuty time against the 8 hour driving rule when it's less than 30 minutes
	        	this.Summary.setDriveTimeAccumulated8HourRule(this.Summary.getDriveTimeAccumulated8HourRule() + length);
	        }
	        else{
	        	// At least a 30 minute break detected, so reset the 8 hour rule's driving time accumulator
	        	this.Summary.setDriveTimeAccumulated8HourRule(0);
	        	this.Summary.setConsecutiveOffDutyAccumulated(0);
	        }
        }
	}

    /// <summary>
    /// Shorthaul exception just became available - allow use of it and set extended duty hours
    /// </summary>
    protected void SetExtendedShorthaulLimit()
    {
        this.Summary.setIsShortHaulExceptionAvailable(true);
        this.DAILY_DUTY_TIME_ALLOWED = this.DAILY_DUTY_TIME_ALLOWED_WITH_SHORT_HAUL_ADDITIONAL_DRIVING;
    }

    /// <summary>
    /// Shorthaul exception cannot be used - don't allow use and set standard limit
    /// </summary>
    private void SetStandardDutyLimit()
    {
        this.Summary.setIsShortHaulExceptionAvailable(false);
        this.DAILY_DUTY_TIME_ALLOWED = this.DAILY_DUTY_TIME_ALLOWED_STANDARD;
    }

    /// <summary>
    /// Determine if a daily hour reset has occurred.
    /// When a daily reset occurs, then the daily hours in the summary are reset to zero.
    /// </summary>
	@Override
    protected void CalculateDailyReset(long length)
    {
        this.Summary.setDailyResetAmount(this.Summary.getDailyResetAmount() - length);
        if (DateUtility.ConvertMillisecondsToMinutes(this.Summary.getDailyResetAmount()) <= 0.0)
        {
        	this.MarkDailyReset();
        	this.MarkDailySleeperCombinationReset();
        }
    }

    /// <summary>
    /// Determine if a weekly hour reset has occurred.
    /// When the reset occurs, then the summary is completely reset to zero.
    /// Also, allow the short-haul exception to be used again.
    /// </summary>
	@Override
    protected void CalculateWeeklyReset(long length)
    {
        this.Summary.setWeeklyResetAmount(this.Summary.getWeeklyResetAmount() - length);
        if (this.getRulesetProperties().getIs34HourResetAllowed() && DateUtility.ConvertMillisecondsToMinutes(this.Summary.getWeeklyResetAmount()) <= 0.0)
        {
            this.Summary.setDriveTimeAccumulated(0);
            this.Summary.setDutyTimeAccumulated(0);
            this.Summary.setWeeklyDutyTimeAccumulated(0);

            if(this.getRulesetProperties().getIs8HourDrivingRuleEnabled()){
            	this.Summary.setDriveTimeAccumulated8HourRule(0);
            }
            
            this.ResetDailyDutyTotals();
            this.Summary.setConsecutiveOffDutyAccumulated(0);            
        }    
    }

    /// <summary>
    /// Answer the duty summary of the WEEKLY rules
    /// </summary>
	@Override
    public Bundle WeeklyDutySummary(HoursOfServiceSummary summary)
    {
		long used = 0;
		long avail = 0;
		int allowedHours = (int)(this.WEEKLY_DUTY_TIME_ALLOWED/MILLISECONDS_PER_HOUR);
		String regulatorySection = null;
		
		used = summary.getWeeklyDutyTimeAccumulated();
		if (this.WEEKLY_DUTY_TIME_ALLOWED > used)
		{
			avail = this.WEEKLY_DUTY_TIME_ALLOWED - used;
		}
		
		if (avail < 0) avail = 0;
		
		Bundle bundle = new Bundle();
		bundle.putLong(USED, used);
		bundle.putLong(AVAIL, avail);
		bundle.putInt(ALLOWED, allowedHours);
		bundle.putString(REGSECTION, regulatorySection);
		        
        return bundle;
    }

	@Override
	public Bundle DailyDutySummary(HoursOfServiceSummary summary)
	{
		long used = 0;
		long avail = 0;
		int allowedHours = (int)(this.DAILY_DUTY_TIME_ALLOWED/MILLISECONDS_PER_HOUR);
		String regulatorySection = null;
		
		used = summary.getDutyTimeAccumulated();
		if (this.DAILY_DUTY_TIME_ALLOWED > used)
		{
			avail = this.DAILY_DUTY_TIME_ALLOWED - used;
		}
		
		if (avail < 0) avail = 0;
		
		Bundle bundle = new Bundle();
		bundle.putLong(USED, used);
		bundle.putLong(AVAIL, avail);
		bundle.putInt(ALLOWED, allowedHours);
		bundle.putString(REGSECTION, regulatorySection);
		
		return bundle;		
	}
	
	@Override
	public Bundle DailyDriveSummary(HoursOfServiceSummary summary)
	{
		long used = 0;
		long avail = 0;
		int allowedHours = (int)(this.DAILY_DRIVE_TIME_ALLOWED/MILLISECONDS_PER_HOUR);
		String regulatorySection = null;
		
		used = summary.getDriveTimeAccumulated();
		if (this.DAILY_DRIVE_TIME_ALLOWED > used)
		{
			avail = this.DAILY_DRIVE_TIME_ALLOWED - used;
		}
		
		if (avail < 0) avail = 0;

		Bundle bundle = new Bundle();
		bundle.putLong(USED, used);
		bundle.putLong(AVAIL, avail);
		bundle.putInt(ALLOWED, allowedHours);
		bundle.putString(REGSECTION, regulatorySection);		
        
		return bundle;		
	}
	
    /// <summary>
    /// Answer the duty summary of the DAILY Driving rules for Rest Break
    /// </summary>
	@Override
    public Bundle DailyDriveRestBreakSummary(HoursOfServiceSummary summary)
    {
        Bundle bundle = null;
        
        if(this.getRulesetProperties().getIs8HourDrivingRuleEnabled()){
        	Bundle driveTimeBundle = this.DailyDriveSummary(summary);
        	
        	int allowedHours = (int)(this.DAILY_DRIVE_TIME_ALLOWED_8_HOUR_RULE/MILLISECONDS_PER_HOUR);
        	driveTimeBundle.putInt(ALLOWED, allowedHours);
        	
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
	
	private void MarkDailyReset(){
        this.Summary.setDriveTimeAccumulated(0);
        this.Summary.setDutyTimeAccumulated(0);
        if(this.getRulesetProperties().getIs8HourDrivingRuleEnabled()){
        	this.Summary.setDriveTimeAccumulated8HourRule(0);
        }
        this.Summary.setConsecutiveOffDutyAccumulated(0);
        this.Summary.setHasDrivingOccurredAfterDailyReset(false);
	}

    /// <summary>
    /// Mark that the short haul exception was used on the current log date.
    /// The end of the duty period that claimed the short-haul exception is the
    /// log date used.
    /// </summary>
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

    /// <summary>
    /// Answer if the short haul exception can be used on the current log date.
    /// The following conditions are verified:
    /// 1. The short-haul exception feature is enabled for the driver
    /// 2. The driver has returned to the work location during the previous
    ///    5 duty tours.
    /// 3. The short-haul exception was not already used within the previous 6 days
    /// </summary>
    /// <returns></returns>
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
                // if operates specific vehicle for oil field is turned on, shorthaul exception cannot be used
                else if (this.getDutyInfoByDate().get(logDate).getIsOperatesSpecificVehiclesForOilField())
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
                        if (!info.getHasReturnedToWorkLocation() && dutyTours <= 5)
                        {
                            // today is considered a duty tour, so look for 6 total tours
                            // found a duty tour where the driver did not return to the 
                            // normal work location
                            canExceptionBeUsed = false;
                            done = true;
                        }
                        else if (dutyTours >= 5)
                        {
                            // this means that 5 previous duty tours have all returned to 
                            // work location without detecting - this requirement is met,
                            // no longer need to check anymore duty tours
                            checkDutyTours = false;
                        }
                    }

                    // if we find a weekly reset, we no longer need to check if the shorthaul 
                    // has been used 
                    if (!done && checkShorthaulUsed && this.getDutyInfoByDate().containsKey(logDate) && this.getDutyInfoByDate().get(logDate).getIsWeeklyResetUsed())
                    {
                        checkShorthaulUsed = false;
                    }
                }
                logDate = DateUtility.AddDays(logDate, -1);
                if (logDaysExamined >= this.WEEKLY_DUTY_PERIOD_DAYS) done = true;
            }
        }

        return canExceptionBeUsed;
    }
}
