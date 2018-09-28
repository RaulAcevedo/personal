package com.jjkeller.kmbapi.calcengine.Federal;

import android.os.Bundle;

import com.jjkeller.kmbapi.calcengine.HoursOfServiceSummary;
import com.jjkeller.kmbapi.calcengine.RulesetProperties;
import com.jjkeller.kmbapi.controller.ILogCheckerComplianceDatesController;

public class USConstruction7DayProperty extends USConstructionRulesetBase {

    private static final String REGULATORY_SECTION_STANDARD = "U7";

    public USConstruction7DayProperty(RulesetProperties properties)
    {
        super(properties);
    }

    public USConstruction7DayProperty(RulesetProperties properties, ILogCheckerComplianceDatesController complianceDateController)
    {
        super(properties, complianceDateController);
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
        super.Initialize(properties);

        this.DAILY_DRIVE_TIME_ALLOWED = 11 * MILLISECONDS_PER_HOUR;
        this.DAILY_DUTY_TIME_ALLOWED = 14 * MILLISECONDS_PER_HOUR;
        this.DAILY_OFFDUTY_HOURS_FOR_RESET = 10 * MILLISECONDS_PER_HOUR;

        this.WEEKLY_DUTY_TIME_ALLOWED = 60 * MILLISECONDS_PER_HOUR;
        this.WEEKLY_DUTY_PERIOD_DAYS = 7;
        this.WEEKLY_OFFDUTY_HOURS_FOR_RESET = 24 * MILLISECONDS_PER_HOUR;
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
