package com.jjkeller.kmbapi.calcengine.OilField;

import android.os.Bundle;

import com.jjkeller.kmbapi.calcengine.HoursOfServiceSummary;
import com.jjkeller.kmbapi.calcengine.RulesetProperties;
import com.jjkeller.kmbapi.calcengine.SplitSleeperCombination;
import com.jjkeller.kmbapi.controller.utility.DateUtility;

public class TexasOilField extends OilFieldRulesetBase {

	private static final String REGULATORY_SECTION_STANDARD = "Texas Oil Field";
	
	public TexasOilField(RulesetProperties properties)
	{
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
		
		this.Summary.setCombinableOffDutyPeriod(SplitSleeperCombination.ForTexasOilField());
	
		this.DAILY_DRIVE_TIME_ALLOWED = 12 * MILLISECONDS_PER_HOUR;
		this.DAILY_DUTY_TIME_ALLOWED = 15 * MILLISECONDS_PER_HOUR;
		this.DAILY_OFFDUTY_HOURS_FOR_RESET = 8 * MILLISECONDS_PER_HOUR;
		
		this.WEEKLY_DUTY_PERIOD_DAYS = 7;
	}
	
    /// <summary>
    /// Process the off-duty event.
    /// Off duty time cannot be used as combinable period under TX Oil Field
	/// Off duty also does not count against 15 hour on duty limit
    /// </summary>
	@Override
    protected void ProcessOffDuty(long length)
    {        
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
        	// if this sleeper period is not combined with another sleeper period, process as
        	// an off-duty period
        	boolean hasCombinedSleeper = this.Summary.getCombinableOffDutyPeriod().ProcessSleeperTime(length, this.Summary);            
            if (hasCombinedSleeper){
            	this.Summary.setHasDrivingOccurredAfterDailyReset(false);
            } 
            
            this.CalculateDailyReset(length);
            this.CalculateWeeklyReset(length);
        }
    }

	@Override
	protected void ProcessOffDutyWellsite(long length)
	{
        if (DateUtility.ConvertMillisecondsToHours(length) >= 2.0 && DateUtility.ConvertMillisecondsToHours(length) < 8.0)
        {
    		// if off duty wellsite period is a combinable length (>=2 and < 8), attempt to combine
        	// it as a sleeper - can be combined with other off duty wellsite time or
        	// sleeper time
        	this.Summary.getCombinableOffDutyPeriod().ProcessSleeperTime(length, this.Summary, true);
        }
        
        this.CalculateDailyReset(length);
        this.CalculateWeeklyReset(length);
	}

	// reinitialize sleeper combination
	protected void MarkDailySleeperCombinationReset()
	{
        this.Summary.setCombinableOffDutyPeriod(SplitSleeperCombination.ForTexasOilField());
	}

    /// <summary>
    /// Answer the duty summary of the WEEKLY rules
    /// </summary>
	@Override
    public Bundle WeeklyDutySummary(HoursOfServiceSummary summary)
    {
        Bundle bundle = super.WeeklyDutySummary(summary);
        if (!bundle.containsKey(REGSECTION) || bundle.getString(REGSECTION) == null || bundle.getString(REGSECTION).equals("")) 
        	bundle.putString(REGSECTION, REGULATORY_SECTION_STANDARD);
        
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
        	bundle.putString(REGSECTION, REGULATORY_SECTION_STANDARD);
        
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
        	bundle.putString(REGSECTION, REGULATORY_SECTION_STANDARD);
        
        return bundle;
    }	
}
