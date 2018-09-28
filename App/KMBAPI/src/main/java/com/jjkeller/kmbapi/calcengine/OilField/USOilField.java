package com.jjkeller.kmbapi.calcengine.OilField;

import android.os.Bundle;

import com.jjkeller.kmbapi.calcengine.DutyInfo;
import com.jjkeller.kmbapi.calcengine.HoursOfServiceSummary;
import com.jjkeller.kmbapi.calcengine.RulesetProperties;
import com.jjkeller.kmbapi.calcengine.SplitSleeperCombination;
import com.jjkeller.kmbapi.controller.utility.DateUtility;

import java.util.Date;

public class USOilField extends OilFieldRulesetBase {

	private static final String REGULATORY_SECTION_STANDARD = "US Oil Field";
	
	public USOilField(RulesetProperties properties)
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
		
		this.Summary.setCombinableOffDutyPeriod(SplitSleeperCombination.ForUSOilField());
	
		this.DAILY_DRIVE_TIME_ALLOWED = 11 * MILLISECONDS_PER_HOUR;
		this.DAILY_DUTY_TIME_ALLOWED = 14 * MILLISECONDS_PER_HOUR;
		this.DAILY_OFFDUTY_HOURS_FOR_RESET = 10 * MILLISECONDS_PER_HOUR;

		// need a 34 hour off duty period in order to reuse shorthaul exception prior to 7 days
		this.OILFIELD_SHORTHAUL_RESET = 34 * MILLISECONDS_PER_HOUR;

        this.Summary.setOilFieldShortHaulReset(OILFIELD_SHORTHAUL_RESET);
	}
	
    /// <summary>
    /// Short-haul exception is allowed for US Oil Field rule set, but a 34 hour reset
	/// is required - not the 24 hour reset utilized by US Oil Field to reset weekly hours
    /// </summary>
	@Override
    protected void CalculateWeeklyReset(long length)
    {
		super.CalculateWeeklyReset(length);
		
        if (this.getRulesetProperties().getIs34HourResetAllowed() && DateUtility.ConvertMillisecondsToMinutes(this.Summary.getWeeklyResetAmount()) <= 0.0)
        {
            this.Summary.setCombinableOffDutyPeriod(SplitSleeperCombination.ForUSOilField());
        }
        
        // if shorthaul exception is allowed, check for a 34 hour reset to determine if short haul can 
        // be used again.  Weekly reset for us oilfield is 24, but a 34 hour reset is needed in order to 
        // use short haul prior to waiting for 7 days.
        if (this.getRulesetProperties().getIsShortHaulExceptionAllowed())
        {	       
	        this.Summary.setOilFieldShortHaulReset(this.Summary.getOilFieldShortHaulReset() - length);
	        if (DateUtility.ConvertMillisecondsToMinutes(this.Summary.getOilFieldShortHaulReset()) <= 0.0)
	        {
	            Date key = DateUtility.GetDateFromDateTime(DateUtility.AddMinutes((this.Summary.getRecentDutyTimestamp()), (int)DateUtility.ConvertMillisecondsToMinutes(length)));
	            DutyInfo info = this.getDutyInfoByDate().get(key);
	            
	            if (info != null)
	            {
	            	info.setIsWeeklyResetUsed(true);
	            	
	            	// if end of reset is on today's log, make short haul available
	                if (this.getDutyInfoByDate().get(key).getIsTodaysLog())
	                {
	                	if (this.CanShortHaulExceptionBeUsed())
	                		this.SetExtendedShorthaulLimit();
	                }
	            }
	        }
        }        
    }

	// reinitialize sleeper combination
	protected void MarkDailySleeperCombinationReset()
	{
        this.Summary.setCombinableOffDutyPeriod(SplitSleeperCombination.ForUSOilField());
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
