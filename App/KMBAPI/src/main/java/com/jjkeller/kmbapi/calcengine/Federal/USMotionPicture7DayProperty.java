package com.jjkeller.kmbapi.calcengine.Federal;

import android.os.Bundle;

import com.jjkeller.kmbapi.calcengine.HoursOfServiceSummary;
import com.jjkeller.kmbapi.calcengine.RulesetProperties;

public class USMotionPicture7DayProperty extends USMotionPicturePropertyCarrying
{
    private static final String REGULATORY_SECTION_STANDARD = "MP60";
    
	public USMotionPicture7DayProperty(RulesetProperties properties)
	{
		super(properties);
	}
	
    @Override
    public void Initialize(RulesetProperties properties)
    {
        super.Initialize(properties);

		this.WEEKLY_DUTY_TIME_ALLOWED = 60 * MILLISECONDS_PER_HOUR;
		this.WEEKLY_DUTY_PERIOD_DAYS = 7;
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
