package com.jjkeller.kmbapi.calcengine.Federal;

import android.os.Bundle;

import com.jjkeller.kmbapi.calcengine.HoursOfServiceSummary;
import com.jjkeller.kmbapi.calcengine.RulesetProperties;
import com.jjkeller.kmbapi.calcengine.SplitSleeperCombination;
import com.jjkeller.kmbapi.controller.utility.DateUtility;

public class Texas extends PassengerCarrying {

    private static final String REGULATORY_SECTION_STANDARD = "TX";

	public Texas(RulesetProperties properties)
	{
		super(properties);
	}

    /// <summary>
    /// Initialize the calc engine.
    /// This must be called before any other methods are called on the calc engine.
    /// </summary>
    /// <param name="weeklyDutyTotal"></param>
    /// <param name="isShortHaulExceptionAllowed"></param>
    /// <param name="isShortHaulExceptionAvailable"></param>
	@Override
    public void Initialize(RulesetProperties properties)
    {
        // For Texas, short-haul exception is not allowed
		super.Initialize(properties);

		this.WEEKLY_DUTY_TIME_ALLOWED = 70 * MILLISECONDS_PER_HOUR;
		this.WEEKLY_DUTY_PERIOD_DAYS = 7;
        this.DAILY_DRIVE_TIME_ALLOWED = 12 * MILLISECONDS_PER_HOUR;
        this.DAILY_DUTY_TIME_ALLOWED = 15 * MILLISECONDS_PER_HOUR;
        this.WEEKLY_OFFDUTY_HOURS_FOR_RESET = 34 * MILLISECONDS_PER_HOUR;
    }
	
    @Override
    public Bundle DailyDriveSummary(HoursOfServiceSummary summary)
    {
    	Bundle bundle = super.DailyDriveSummary(summary);
    	bundle.putString(REGSECTION, REGULATORY_SECTION_STANDARD);
    	return bundle;
    }

    @Override
    public Bundle DailyDutySummary(HoursOfServiceSummary summary)
    {
    	Bundle bundle = super.DailyDutySummary(summary);
    	bundle.putString(REGSECTION, REGULATORY_SECTION_STANDARD);
    	return bundle;
    }

    @Override
    public Bundle WeeklyDutySummary(HoursOfServiceSummary summary)
    {
    	Bundle bundle = super.WeeklyDutySummary(summary);
    	bundle.putString(REGSECTION, REGULATORY_SECTION_STANDARD);
    	return bundle;
    }

    /// <summary>
    /// Process the off-duty event.
    /// Texas follows the passenger carrying limits for the daily drive and daily
    /// duty limits, but it does allow a weekly reset after 34 hours consecutive
    /// off duty.  Override the ProcessOffDuty method to check for a weekly reset
    /// </summary>
    /// <param name="length"></param>
    @Override
    protected void ProcessOffDuty(long length)
    {
    	super.ProcessOffDuty(length);
        this.CalculateWeeklyReset(length);
    }

    /// <summary>
    /// Process an on-duty status event.
    /// Accumulate the duty time accordingly.
    /// Texas follows the passenger carrying limits for the daily drive and daily
    /// duty limits, but it does allow a weekly reset after 34 hours consecutive
    /// off duty.  Override the ProcessOnDuty method to reset the WeeklyResetAmount
    /// </summary>
    /// <param name="length"></param>
    @Override
    protected void ProcessOnDuty(long length)
    {
        super.ProcessOnDuty(length);
        this.Summary.setWeeklyResetAmount(WEEKLY_OFFDUTY_HOURS_FOR_RESET);
    }

    /// <summary>
    /// Determine if a weekly hour reset has occurred.
    /// When the reset occurs, then the summary is completely reset to zero.
    /// Texas follows the passenger carrying limits for the daily drive and daily
    /// duty limits, but it does allow a weekly reset after 34 hours consecutive
    /// off duty.  Override the CalculateWeeklyReset method to check for a reset
    /// </summary>
    /// <param name="length"></param>
    @Override
    protected void CalculateWeeklyReset(long length)
    {
        this.Summary.setWeeklyResetAmount(this.Summary.getWeeklyResetAmount() - length);
        if (DateUtility.ConvertMillisecondsToMinutes(this.Summary.getWeeklyResetAmount()) <= 0.0)
        {
            this.Summary.setDriveTimeAccumulated(0);
            this.Summary.setDutyTimeAccumulated(0);
            this.Summary.setWeeklyDutyTimeAccumulated(0);
            this.Summary.setCombinableOffDutyPeriod(SplitSleeperCombination.ForPassengerCarryingRules());

            this.ResetDailyDutyTotals();
        }
    }

}
