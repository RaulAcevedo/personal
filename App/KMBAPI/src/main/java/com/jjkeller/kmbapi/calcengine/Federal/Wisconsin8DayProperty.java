package com.jjkeller.kmbapi.calcengine.Federal;

import android.os.Bundle;

import com.jjkeller.kmbapi.calcengine.HoursOfServiceSummary;
import com.jjkeller.kmbapi.calcengine.RulesetProperties;

public class Wisconsin8DayProperty extends PropertyCarrying {

    private static final String REGULATORY_SECTION_STANDARD = "WI8";

    public Wisconsin8DayProperty(RulesetProperties properties)
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
        properties.setIs34HourResetAllowed(false);
        super.Initialize(properties);

        this.DAILY_DRIVE_TIME_ALLOWED = 12 * MILLISECONDS_PER_HOUR;
        this.DAILY_DUTY_TIME_ALLOWED = 16 * MILLISECONDS_PER_HOUR;
        this.DAILY_OFFDUTY_HOURS_FOR_RESET = 10 * MILLISECONDS_PER_HOUR;

        this.WEEKLY_DUTY_TIME_ALLOWED = 80 * MILLISECONDS_PER_HOUR;
        this.WEEKLY_DUTY_PERIOD_DAYS = 8;
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
}
