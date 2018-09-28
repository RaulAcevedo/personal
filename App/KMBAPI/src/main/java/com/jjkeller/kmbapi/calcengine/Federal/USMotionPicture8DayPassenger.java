package com.jjkeller.kmbapi.calcengine.Federal;

import android.os.Bundle;

import com.jjkeller.kmbapi.calcengine.HoursOfServiceSummary;
import com.jjkeller.kmbapi.calcengine.RulesetProperties;

public class USMotionPicture8DayPassenger extends PassengerCarrying70Hour
{
	private static final String REGULATORY_SECTION_STANDARD = "MP70";
	
    public USMotionPicture8DayPassenger(RulesetProperties properties)
	{
		super(properties);
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
