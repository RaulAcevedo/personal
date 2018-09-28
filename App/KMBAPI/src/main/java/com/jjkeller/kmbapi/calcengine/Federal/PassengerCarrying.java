package com.jjkeller.kmbapi.calcengine.Federal;

import android.os.Bundle;

import com.jjkeller.kmbapi.calcengine.HoursOfServiceSummary;
import com.jjkeller.kmbapi.calcengine.RulesetProperties;
import com.jjkeller.kmbapi.calcengine.SplitSleeperCombination;

public abstract class PassengerCarrying extends FederalRulesetBase{

    private static final String REGULATORY_SECTION_WEEKLY_DUTY = "395.5(b)";
    private static final String REGULATORY_SECTION_DAILY_DRIVE = "395.5(a)(1)";
    private static final String REGULATORY_SECTION_DAILY_DUTY = "395.5(a)(2)";

	protected PassengerCarrying(RulesetProperties properties){
		super(properties);		
	}
	
    /// <summary>
    /// Initialize the calc engine.
    /// This must be called before any other methods are called on the calc engine.
    /// </summary>
	@Override
    public void Initialize(RulesetProperties properties)
    {
        super.Initialize(properties);

        this.Summary.setCombinableOffDutyPeriod(SplitSleeperCombination.ForPassengerCarryingRules());

        this.DAILY_DRIVE_TIME_ALLOWED = 10 * MILLISECONDS_PER_HOUR;
        this.DAILY_DUTY_TIME_ALLOWED = 15 * MILLISECONDS_PER_HOUR;
        this.DAILY_OFFDUTY_HOURS_FOR_RESET = 8 * MILLISECONDS_PER_HOUR;
    }

    /// <summary>
    /// Answer the duty summary of the WEEKLY rules
    /// </summary>
	@Override
    public Bundle WeeklyDutySummary(HoursOfServiceSummary summary)
    {
        Bundle bundle = super.WeeklyDutySummary(summary);
        if (!bundle.containsKey(REGSECTION) || bundle.getString(REGSECTION) == null || bundle.getString(REGSECTION).equals("")) 
        	bundle.putString(REGSECTION, REGULATORY_SECTION_WEEKLY_DUTY);
        
        return bundle;
    }

    /// <summary>
    /// Answer the duty summary of the DAILY Duty rules.
    /// </summary>
	@Override
	public Bundle DailyDutySummary(HoursOfServiceSummary summary)
	{
		Bundle bundle = super.DailyDutySummary(summary);
        if (!bundle.containsKey(REGSECTION) || bundle.getString(REGSECTION) == null || bundle.getString(REGSECTION).equals("")) 
        	bundle.putString(REGSECTION, REGULATORY_SECTION_DAILY_DUTY);
        
        return bundle;
	}
	
    /// <summary>
    /// Answer the duty summary of the DAILY Driving rules
    /// </summary>
	@Override
	public Bundle DailyDriveSummary(HoursOfServiceSummary summary)
	{
		Bundle bundle = super.DailyDriveSummary(summary);
        if (!bundle.containsKey(REGSECTION) || bundle.getString(REGSECTION) == null || bundle.getString(REGSECTION).equals("")) 
        	bundle.putString(REGSECTION, REGULATORY_SECTION_DAILY_DRIVE);
        
        return bundle;
	}
	
	@Override
    /// <summary>
    /// Determine if a daily hour reset has occurred.
    /// When a daily reset occurs, then the daily hours in the summary are reset to zero.
    /// </summary>
	protected void CalculateDailyReset(long length)
	{
		this.Summary.setDailyResetAmount(this.Summary.getDailyResetAmount() - length);
		if (this.Summary.getDailyResetAmount() <= 0)
		{
			this.Summary.setDriveTimeAccumulated(0);
			this.Summary.setDutyTimeAccumulated(0);
			this.Summary.setCombinableOffDutyPeriod(SplitSleeperCombination.ForPassengerCarryingRules());
		}
	}
	
    /// <summary>
    /// No weekly reset allowed for passenger carrying rules
    /// </summary>
	@Override
	protected void CalculateWeeklyReset(long length)
	{
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
		
		this.Summary.getCombinableOffDutyPeriod().ProcessOnDutyTime(length);
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
		
        // accumulate driving specific numbers
		this.Summary.setDriveTimeAccumulated(this.Summary.getDriveTimeAccumulated() + length);
		
		this.Summary.getCombinableOffDutyPeriod().ProcessDriveTime(length);
	}
	
    /// <summary>
    /// Process the off-duty event.
    /// </summary>
	@Override
	protected void ProcessOffDuty(long length)
	{
		this.CalculateDailyReset(length);
	}
	
    /// <summary>
    /// Process the sleeper status.
    /// </summary>
	@Override
	protected void ProcessSleepDuty(long length)
	{
        // this may be a full sleeper period 
        // determine if there has already been a two hour off-duty period that this
        // can be combined with

		this.Summary.getCombinableOffDutyPeriod().ProcessSleeperTime(length, this.Summary);
		
		this.CalculateDailyReset(length);
		this.CalculateWeeklyReset(length);
	}
	
	@Override
	protected void ProcessOffDutyWellsite(long length)
	{		
	}
}
