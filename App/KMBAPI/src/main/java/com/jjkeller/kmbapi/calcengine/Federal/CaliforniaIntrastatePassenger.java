package com.jjkeller.kmbapi.calcengine.Federal;

import android.os.Bundle;

import com.jjkeller.kmbapi.calcengine.HoursOfServiceSummary;

public class CaliforniaIntrastatePassenger extends PassengerCarrying
{
	private static final String REGULATORY_SECTION_STANDARD = "C3";

	public CaliforniaIntrastatePassenger(com.jjkeller.kmbapi.calcengine.RulesetProperties properties)
	{
		super(properties);
	}

	@Override
	public void Initialize(com.jjkeller.kmbapi.calcengine.RulesetProperties properties)
	{
		super.Initialize(properties);

		this.DAILY_DRIVE_TIME_ALLOWED = 10 * MILLISECONDS_PER_HOUR;
		this.DAILY_DUTY_TIME_ALLOWED = 15 * MILLISECONDS_PER_HOUR;
		this.DAILY_OFFDUTY_HOURS_FOR_RESET = 8 * MILLISECONDS_PER_HOUR;
		
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
