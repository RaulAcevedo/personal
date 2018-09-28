package com.jjkeller.kmbapi.calcengine.Federal;

import android.os.Bundle;

import com.jjkeller.kmbapi.calcengine.HoursOfServiceSummary;
import com.jjkeller.kmbapi.calcengine.RulesetBase;
import com.jjkeller.kmbapi.calcengine.RulesetProperties;

public abstract class FederalRulesetBase extends RulesetBase{

	FederalRulesetBase(RulesetProperties properties) {
		super(properties);
	}

    /// <summary>
    /// Answer the duty summary of the WEEKLY rules
    /// </summary>
	@Override
	public Bundle WeeklyDutySummary(HoursOfServiceSummary summary)
	{
		long used = 0;
		long avail = 0;
		int allowedHours = (int)(this.WEEKLY_DUTY_TIME_ALLOWED/MILLISECONDS_PER_HOUR);
		String regulatorySection = null;
		
		used = summary.getWeeklyDutyTimeAccumulated();
		if (this.WEEKLY_DUTY_TIME_ALLOWED > used)
		{
			avail = this.WEEKLY_DUTY_TIME_ALLOWED - used;
		}
		
		if (avail < 0) avail = 0;
		
		Bundle bundle = new Bundle();
		bundle.putLong(USED, used);
		bundle.putLong(AVAIL, avail);
		bundle.putInt(ALLOWED, allowedHours);
		bundle.putString(REGSECTION, regulatorySection);
		
		return bundle;
	}
	
	@Override
	public Bundle DailyDutySummary(HoursOfServiceSummary summary)
	{
		long used = 0;
		long avail = 0;
		int allowedHours = (int)(this.DAILY_DUTY_TIME_ALLOWED/MILLISECONDS_PER_HOUR);
		String regulatorySection = null;
		
		used = summary.getDutyTimeAccumulated();
		if (this.DAILY_DUTY_TIME_ALLOWED > used)
		{
			avail = this.DAILY_DUTY_TIME_ALLOWED - used;
		}
		
		if (avail < 0) avail = 0;
		
		Bundle bundle = new Bundle();
		bundle.putLong(USED, used);
		bundle.putLong(AVAIL, avail);
		bundle.putInt(ALLOWED, allowedHours);
		bundle.putString(REGSECTION, regulatorySection);
		
		return bundle;		
	}
	
	@Override
	public Bundle DailyDriveSummary(HoursOfServiceSummary summary)
	{
		long used = 0;
		long avail = 0;
		int allowedHours = (int)(this.DAILY_DRIVE_TIME_ALLOWED/MILLISECONDS_PER_HOUR);
		String regulatorySection = null;
		
		used = summary.getDriveTimeAccumulated();
		if (this.DAILY_DRIVE_TIME_ALLOWED > used)
		{
			avail = this.DAILY_DRIVE_TIME_ALLOWED - used;
		}
		
		if (avail < 0) avail = 0;

		Bundle bundle = new Bundle();
		bundle.putLong(USED, used);
		bundle.putLong(AVAIL, avail);
		bundle.putInt(ALLOWED, allowedHours);
		bundle.putString(REGSECTION, regulatorySection);
		
		return bundle;		
	}
}
