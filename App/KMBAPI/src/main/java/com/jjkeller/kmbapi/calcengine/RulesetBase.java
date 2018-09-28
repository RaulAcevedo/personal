package com.jjkeller.kmbapi.calcengine;

import android.os.Bundle;
import android.util.Log;

import com.jjkeller.kmbapi.calcengine.Enums.CanadaDeferralTypeEnum;
import com.jjkeller.kmbapi.calcengine.Enums.DutyStatusEnum;
import com.jjkeller.kmbapi.calcengine.Enums.RuleSetTypeEnum;
import com.jjkeller.kmbapi.controller.utility.DateUtility;

import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;

public abstract class RulesetBase implements IHosRulesetCalcEngine{

	protected static final long MILLISECONDS_PER_HOUR = 3600000;
	protected static final long MILLISECONDS_PER_DAY = 86400000;
	
	protected static final String USED = "used";
	protected static final String AVAIL = "avail";
	protected static final String ALLOWED = "allowed";
	protected static final String REGSECTION = "regsection";
	
	protected static final String SHORT_HAUL_EXCEPTION_USED = "shorthaulused";
	protected static final String WEEKLY_RESET_START_TIMESTAMP = "weeklyResetStartTimestamp";
	protected static final String WEEKLY_RESET_USED = "isweeklyresetused";
	
    /**
     * Restrict the constructor so that external use is through the RulesetFactory
     * @param properties
     */
	protected RulesetBase(RulesetProperties properties){
		this.RulesetProperties = properties;
		this.Initialize(properties);
	}
	
	/**
	 * Amount of daily drive time allowed for the ruleset in milliseconds
	 */
	protected long DAILY_DRIVE_TIME_ALLOWED;
	
    /**
     * Amount of daily duty time allowed for the ruleset in milliseconds
     */
	protected long DAILY_DUTY_TIME_ALLOWED;
	
    /**
     * Amount of weekly duty time allowed for the ruleset in milliseconds
     */
    protected long WEEKLY_DUTY_TIME_ALLOWED;

    /**
     * Amount of off-duty time required before a daily reset occurs in milliseconds
     */
    protected long DAILY_OFFDUTY_HOURS_FOR_RESET;

    /**
     * Amount of off-duty time required before a weekly reset occurs in milliseconds
     */
    protected long WEEKLY_OFFDUTY_HOURS_FOR_RESET;

    /**
     * Number of days that make up the duty period for the ruleset
     */
    protected int WEEKLY_DUTY_PERIOD_DAYS;

    protected HoursOfServiceSummary Summary;
    protected HoursOfServiceSummary PreviousSummary;
    private Hashtable<Date, DailyDutyTotal> DutyTotalsByDate;
    private RulesetProperties RulesetProperties;
    private LogProperties LogProperties;
    
    private Hashtable<Date, DailyDutyTotal> getDutyTotalsByDate()
    {
    	return DutyTotalsByDate;
    }
    private void setDutyTotalsByDate(Hashtable<Date, DailyDutyTotal> dutyTotalsByDate)
    {
    	this.DutyTotalsByDate = dutyTotalsByDate;
    }
    
    private class DailyDutyTotal
    {
    	DailyDutyTotal() { this.setAmount(0); }
    	DailyDutyTotal(long amt) { this.setAmount(amt); }
    	
    	long _amount;
    	long getAmount()
    	{
    		return _amount;
    	}
    	void setAmount(long amount)
    	{
    		this._amount = amount;
    	}
    }
    
    /// <summary>
    /// Initialize the calc engine.
    /// This must be called before any other methods are called on the calc engine.
    /// </summary>
	public void Initialize(RulesetProperties properties) 
	{		
		this.Summary = new HoursOfServiceSummary();
		
		this.Summary.setWeeklyDutyTimeAccumulated(properties.getWeeklyDutyTotal());
		this.setDutyTotalsByDate(new Hashtable<Date, DailyDutyTotal>());
	}
	
    /// <summary>
    /// Perform a check of the duration and answer the duration.  This is method is 
    /// not meant to be signify the end of a duty status period.   It is intended to be
    /// an informational check on the summary of hours available.
    /// Answer a summary of the duty hours available.
    /// </summary>
	public HoursOfServiceSummary CheckDutyStatusDuration(Date dutyTimestamp, DutyStatusEnum dutyStatus, long dutyLength, RuleSetTypeEnum ruleset) 
	{
        // save the existing summary so that it can be put back later
		HoursOfServiceSummary currentSummary = (HoursOfServiceSummary)this.Summary.Clone();
		HoursOfServiceSummary currentPreviousSummary = this.PreviousSummary == null ? null : (HoursOfServiceSummary)this.PreviousSummary.Clone();
		long currentDutyTotalForDate = 0;
		if (this.getDutyTotalsByDate().containsKey(DateUtility.GetDateFromDateTime(dutyTimestamp)))
		{
			currentDutyTotalForDate = this.getDutyTotalsByDate().get(DateUtility.GetDateFromDateTime(dutyTimestamp)).getAmount();
		}
		
        // process the status, as if it were ending right now
		HoursOfServiceSummary newSummary = this.EndOfDutyStatusUpdate(dutyTimestamp, dutyStatus, dutyLength, ruleset);

        Log.d("Current Duty", String.format(Locale.getDefault(), "DutyStatus: %s Min: %s", dutyStatus.toString(), newSummary.getDutyTimeAccumulated() / 60000));
        Log.v("CheckDutyStatusDuration", newSummary.toString());

        // put the old summary back in 
        this.Summary = currentSummary;
        this.PreviousSummary = currentPreviousSummary;
        this.AssignDailyDutyTotal(dutyTimestamp, currentDutyTotalForDate);

        // send back the new summary
		return newSummary;
	}

    /// <summary>
    /// The end of a duty status period has occured.   Calculate a summary of the 
    /// duty hours available.
    /// Answer a summary of the duty hours available.
    /// </summary>
	public HoursOfServiceSummary EndOfDutyStatusUpdate(Date dutyTimestamp, DutyStatusEnum dutyStatus, long dutyLength, RuleSetTypeEnum ruleset)
	{
		Date originalDutyTimestamp = dutyTimestamp;
		long originalDutyLength = dutyLength;
		
		if (this.Summary == null)
		{
			//TODO throw exception
            //throw new KmbApplicationException("Initialize must be called before use.");
		}
		
		if (this.Summary.getValidityTimestamp() != null)
		{
            // verify that log event was received in order
            // the new log event should be in the future relative to the current summary
			if (this.Summary.getValidityTimestamp().compareTo(dutyTimestamp) > 0)
			{
				//TODO throw exception
                // events came in out of sequence
                //throw new KmbApplicationException(string.Format("Log event for '{0}' processed out of sequence", dutyTimestamp));
			}		
			
			// see if an entire log has been missed
			int daysBetween = DateUtility.DaysBetween(dutyTimestamp, this.Summary.getValidityTimestamp());			
			if (daysBetween > 1)
			{
                // missing logs have been detected
                // process each missing date as if it were a 24-hour off-duty log
				int numLogsMissing = daysBetween - 1;
				Date startDate = DateUtility.GetDateFromDateTime(this.Summary.getValidityTimestamp());
				for (int index = 1; index <= numLogsMissing; index++)
				{
					Date missingLogDate = DateUtility.AddDays(startDate, index);
					this.EndOfDutyStatusUpdate(missingLogDate, DutyStatusEnum.OFF, MILLISECONDS_PER_DAY, ruleset);
				}
			}
			
            // determine if we're continuing the off-duty period last seen
			boolean beginNewStatus = true;
			if ((dutyStatus == DutyStatusEnum.OFF ||
					dutyStatus == DutyStatusEnum.SLP ||
					dutyStatus == DutyStatusEnum.OFFWLLST) &&
					this.Summary.getRecentDutyStatus() == dutyStatus &&
					this.PreviousSummary != null &&
					this.PreviousSummary.getRecentDutyTimestamp() != null)
			{
                // continuing the same status used the last time we calc'ed
                // combine the length of the two periods and run that new length
                // through the old summary
				dutyLength = this.Summary.getRecentDutyLength() + dutyLength;
				dutyTimestamp = this.Summary.getRecentDutyTimestamp();
				
                // transfer these settings from the current summary to the new previous one
				boolean isShortHaul = this.Summary.getIsShortHaulExceptionAvailable();
				Date logStart = this.Summary.getLogStartTimestamp();
				CanadaDeferralTypeEnum deferral = this.Summary.getCanadaDeferralType();
				
				this.Summary = (HoursOfServiceSummary)this.PreviousSummary.Clone();
				this.Summary.setIsShortHaulExceptionAvailable(isShortHaul);
				this.Summary.setLogStartTimestamp(logStart);
				this.Summary.setCanadaDeferralType(deferral);
				
				beginNewStatus = false;
			}
			
			boolean beginNewLog = false;
			if (this.PreviousSummary != null && this.Summary.getLogStartTimestamp().compareTo(this.PreviousSummary.getLogStartTimestamp()) >0)
			{
                // the log dates need to be checked before moving the current to Previous in the next step
				beginNewLog = true;
			}
			
			if (beginNewStatus)
			{
                // a new status to process, so move the current to the previous
				this.PreviousSummary = (HoursOfServiceSummary)this.Summary.Clone();
			}
			
            // determine if this is an event on a new log date
			if (DateUtility.GetDateFromDateTime(originalDutyTimestamp).compareTo(DateUtility.GetDateFromDateTime(this.Summary.getRecentDutyTimestamp())) > 0)
			{
                // calculate the initial value of the Weekly Duty Total to start with
                // this is a "rolling" value across many log dates, so it needs
                // to be recalculated when a new date comes in
                this.InitializeWeeklyDutyTotal(originalDutyTimestamp);				
			}
			
			if (beginNewLog)
			{
                // these accumulators reset because a new log is starting
                // the log date of the current summary is different than the previous one
				this.Summary.setLogDateDriveTimeAccumulated(0);
				this.Summary.setLogDateDutyTimeAccumulated(0);
			}
		}
		
		if (dutyStatus == DutyStatusEnum.ON || dutyStatus == DutyStatusEnum.DRV)
		{
            // the weekly duty total needs to be calculated using the original duty 
            // length passed in, not the dutyLength which may be combined with
            // duty time from a previous log
			this.IncrementDailyDutyTotal(dutyTimestamp, originalDutyLength);
		}
		
		this.Summary.setValidityTimestamp(originalDutyTimestamp);
		this.ProcessEndOfStatus(dutyTimestamp, dutyStatus, dutyLength, ruleset);
		
		return this.Summary;
	}

    /// <summary>
    /// Answer the date of the start of the duty period specified by the ending date.
    /// This is for the purpose of performing a Duty Service Audit.
    /// </summary>
	public Date DateOfAuditPeriodStart(Date fromEndingDate) 
	{
		return DateUtility.AddDays(fromEndingDate, (this.WEEKLY_DUTY_PERIOD_DAYS - 1) * -1);
	}

    /// <summary>
    /// Answer the end of the duty period specified by the starting date
    /// </summary>
	public Date DateOfAuditPeriodEnd(Date fromStartingDate) 
	{
		return DateUtility.AddDays(fromStartingDate, this.WEEKLY_DUTY_PERIOD_DAYS - 1);
	}

	public int GetWeeklyDutyPeriodDays()
	{
		return this.WEEKLY_DUTY_PERIOD_DAYS;
	}

	public HoursOfServiceSummary getDutySummary()
	{
		return this.Summary;
	}

    /// <summary>
    /// Prepare to start processing a new log
    /// </summary>
    /// <param name="logDate">timestamp of the start of the log date being processed</param>
    /// <param name="isTodaysLog">flag to indicate that this is today's log being processed</param>
    /// <param name="hasReturnedToWorkLocation">has the driver returned to the normal work location</param>
    /// <param name="isShortHaulExceptionUsed">has the short-haul exception been used</param>
	public void PrepareStartOfLog(LogProperties logProperties) 
	{
		LogProperties = logProperties;
		this.Summary.setLogStartTimestamp(logProperties.getLogDate());
		if (this.Summary.getRecent24HourOffDutyPeriod() == null)
		{
            // if there is no 24 offduty period recorded yet, then assume it
            // happened yesterday
			this.Summary.setRecent24HourOffDutyPeriod(DateUtility.AddDays(logProperties.getLogDate(), -1));
		}
		
	}

    /// <summary>
    /// At the end of processing the complete log, return the state data that may have
    /// changed as a result of the calculations
    /// </summary>
	public Bundle FetchAtEndOfLog(Date logDate) 
	{
		Bundle bundle = new Bundle();
		return bundle;
	}

    /// <summary>
    /// Calculate the amount of driving time that remains for the specified summary.
    /// Answer that amount.
    /// The amount of driving time is the smallest of the following numbers:
    /// 1. Daily driving limit
    /// 2. Daily duty limit
    /// 3. Weekly duty limit
    /// </summary>
	public long CalculateDriveTimeRemaining(HoursOfServiceSummary summary) 
	{
		long avail = 0;
		
		Bundle dailyDriveBundle = this.DailyDriveSummary(summary);
		Bundle dailyDutyBundle = this.DailyDutySummary(summary);
		Bundle wkDutyBundle = this.WeeklyDutySummary(summary);

		avail = dailyDriveBundle.getLong(AVAIL);
		long dutyAvail = dailyDutyBundle.getLong(AVAIL);
		long wkDutyAvail = wkDutyBundle.getLong(AVAIL);
		
		if (dutyAvail < avail)
		{
			avail = dutyAvail;
		}
		
		if (wkDutyAvail < avail)
		{
			avail = wkDutyAvail;
		}
		
		if (avail < 0) avail = 0;
		
		return avail;
	}
	
	
	/// <summary>
    /// Calculate the amount of rest break time that remains for the specified summary.
    /// </summary>
	public long CalculateRestBreakTimeRemaining(HoursOfServiceSummary summary) 
	{
		Bundle dailyRestBreakBundle = this.DailyDriveRestBreakSummary(summary);
		long dailyRestBreak = dailyRestBreakBundle.getLong(AVAIL);
		
		return dailyRestBreak;
	}
	

    /// <summary>
    /// A ruleset transition has been detected from the oldruleset to the newruleset
    /// Perform any special processing that may result because of this.
    /// </summary>
    protected void ProcessRulesetTransition(RuleSetTypeEnum oldRuleset, RuleSetTypeEnum newRuleset)
    {
    }

    protected abstract void ProcessOnDuty(long length);
    protected abstract void ProcessOffDuty(long length);
    protected abstract void ProcessSleepDuty(long length);
    protected abstract void ProcessDriveDuty(long length);
    protected abstract void ProcessOffDutyWellsite(long length);
    protected abstract void CalculateDailyReset(long length);
    protected abstract void CalculateWeeklyReset(long length);

    public abstract Bundle WeeklyDutySummary(HoursOfServiceSummary summary);
    public abstract Bundle DailyDutySummary(HoursOfServiceSummary summary);
    public abstract Bundle DailyDriveSummary(HoursOfServiceSummary summary);
    public Bundle DailyDriveRestBreakSummary(HoursOfServiceSummary summary){
    	return null;
    }
    
    /// <summary>
    /// Process the ending of a duty status period.
    /// If the same duty status comes through repeatedly, then combine with the
    /// previous duty status length and retry.
    /// </summary>
    private void ProcessEndOfStatus(Date dutyTimestamp, DutyStatusEnum dutyStatus, long dutyLength, RuleSetTypeEnum ruleset)
    {
    	// 2014.09.08 sjn - Removed due to TC55 performance of string.format with dates
    	//Log.d("CalcEngine", String.format("RulesetBase.ProcessEndOfStatus from: {%s} to: {%s} st: {%s}", dutyTimestamp, new Date(dutyTimestamp.getTime() + dutyLength), dutyStatus));
    	
        this.Summary.setRecentDutyTimestamp(dutyTimestamp);
        this.Summary.setRecentDutyStatus(dutyStatus);
        this.Summary.setRecentDutyLength(dutyLength);

        if (this.Summary.getRecentDutyRuleset() != RuleSetTypeEnum.Null &&
            this.Summary.getRecentDutyRuleset() != ruleset)
        {
            this.ProcessRulesetTransition(this.Summary.getRecentDutyRuleset(), ruleset);
        }
        this.Summary.setRecentDutyRuleset(ruleset);

        switch (dutyStatus)
        {
            case OFF:
                this.ProcessOffDuty(dutyLength);
                break;
            case SLP:
                this.ProcessSleepDuty(dutyLength);
                break;
            case DRV:
                this.ProcessDriveDuty(dutyLength);
                break;
            case ON:
                this.ProcessOnDuty(dutyLength);
                break;
            case OFFWLLST:
            	this.ProcessOffDutyWellsite(dutyLength);
            	break;
        }
    }

    /// <summary>
    /// Reset the daily duty totals list
    /// </summary>
   protected void ResetDailyDutyTotals()
    {
    	this.setDutyTotalsByDate(new Hashtable<Date, RulesetBase.DailyDutyTotal>());
    }
    
   /// <summary>
   /// Reset the daily duty totals list, except for the entry that is the
   /// most current one.
   /// </summary>
   protected void ResetDailyDutyTotalsExceptCurrent()
   {
	   Date key = DateUtility.GetDateFromDateTime(this.Summary.getRecentDutyTimestamp());
	   DailyDutyTotal todaysTotal = null;
	   
	   if (this.getDutyTotalsByDate().containsKey(key))
	   {
		   todaysTotal = this.getDutyTotalsByDate().get(key);
	   }
	   
	   this.setDutyTotalsByDate(new Hashtable<Date, RulesetBase.DailyDutyTotal>());
	   if (todaysTotal != null)
	   {
		   this.getDutyTotalsByDate().put(key, todaysTotal);
	   }
   }
   
   public RulesetProperties getRulesetProperties(){
	   return this.RulesetProperties;
   }
   
   protected LogProperties getLogProperties(){
	   return this.LogProperties;
   }
   
    /// <summary>
    /// Increment the daily duty total hours for the log date that the log event
    /// occurred on.
    /// Sum the dutyLengths that occur on the same day.
    /// </summary>
    private void IncrementDailyDutyTotal(Date dutyTimestamp, long dutyLength)
    {
        Date key = DateUtility.GetDateFromDateTime(dutyTimestamp);
        if (!this.getDutyTotalsByDate().containsKey(key))
        {
        	this.getDutyTotalsByDate().put(key, new DailyDutyTotal());
        }
        
        this.getDutyTotalsByDate().get(key).setAmount(this.getDutyTotalsByDate().get(key).getAmount() + dutyLength);
    }

    private void AssignDailyDutyTotal(Date dutyTimestamp, long dutyLength)
    {
    	Date key = DateUtility.GetDateFromDateTime(dutyTimestamp);
    	this.getDutyTotalsByDate().put(key, new DailyDutyTotal(dutyLength));
    }

    /// <summary>
    /// Initialize the weekly duty total accumlator by looking back through
    /// the most recent duty totals and summing up the period of time specified
    /// by the WeeklyDutyPeriod.
    /// </summary>
    private void InitializeWeeklyDutyTotal(Date dutyTimestamp)
    {
        long weeklyTotal = 0;
        Date logDate = DateUtility.GetDateFromDateTime(dutyTimestamp);
        for (int index = 1; index < this.WEEKLY_DUTY_PERIOD_DAYS; index++)
        {
            logDate = DateUtility.AddDays(logDate, -1);
            if (this.getDutyTotalsByDate().containsKey(logDate))
            {
                weeklyTotal += this.getDutyTotalsByDate().get(logDate).getAmount();
            }
        }

        this.Summary.setWeeklyDutyTimeAccumulated(weeklyTotal);
    }
}
