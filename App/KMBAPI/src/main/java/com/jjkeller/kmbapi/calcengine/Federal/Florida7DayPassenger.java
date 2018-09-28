package com.jjkeller.kmbapi.calcengine.Federal;

import android.os.Bundle;

import com.jjkeller.kmbapi.calcengine.HoursOfServiceSummary;
import com.jjkeller.kmbapi.calcengine.RulesetProperties;

public class Florida7DayPassenger extends PassengerCarrying60Hour {

    private static final String REGULATORY_SECTION_STANDARD = "FL7";

    /// <summary>
    /// Restrict the constructor so that external use is through the RulesetFactory
    /// </summary>
    public Florida7DayPassenger(RulesetProperties properties) 
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
