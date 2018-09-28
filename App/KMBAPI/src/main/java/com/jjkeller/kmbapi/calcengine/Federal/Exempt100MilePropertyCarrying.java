package com.jjkeller.kmbapi.calcengine.Federal;

import android.os.Bundle;

import com.jjkeller.kmbapi.calcengine.HoursOfServiceSummary;
import com.jjkeller.kmbapi.controller.ILogCheckerComplianceDatesController;
import com.jjkeller.kmbapi.controller.utility.DateUtility;

public abstract class Exempt100MilePropertyCarrying extends PropertyCarrying {

	private static final String REGULATORY_SECTION_DAILY_DUTY_100Mile = "395.1(e)(1)";
	
	protected Exempt100MilePropertyCarrying(com.jjkeller.kmbapi.calcengine.RulesetProperties properties) {
		super(properties);
	}
	
	protected Exempt100MilePropertyCarrying(com.jjkeller.kmbapi.calcengine.RulesetProperties properties, ILogCheckerComplianceDatesController complianceDateController) {
		super(properties, complianceDateController);
	}

    @Override
    public Bundle DailyDutySummary(HoursOfServiceSummary summary)
    {
    	Bundle bundle = super.DailyDutySummary(summary);
    	bundle.putString(REGSECTION, REGULATORY_SECTION_DAILY_DUTY_100Mile);
    	return bundle;
    }
	
	/**
	 * Process an on-duty status event.
	 * Accumulate the duty time accordingly.
	 */
	@Override
    protected void ProcessOnDuty(long length)
    {
        this.Summary.setDutyTimeAccumulated(this.Summary.getDutyTimeAccumulated() + length);
        this.Summary.setWeeklyDutyTimeAccumulated(this.Summary.getWeeklyDutyTimeAccumulated() + length);
        this.Summary.setDailyResetAmount(DAILY_OFFDUTY_HOURS_FOR_RESET);
        this.Summary.setWeeklyResetAmount(WEEKLY_OFFDUTY_HOURS_FOR_RESET);
        this.setOriginalValidWeeklyResetDutyStartTimestamp(null);

        this.MarkAsDutyTour();

        this.Summary.getCombinableOffDutyPeriod().ProcessOnDutyTime(length);
    }
	
	/**
	 * Process the off-duty event.
	 */
	@Override
    protected void ProcessOffDuty(long length)
    {
		// when less than 10 hours, accumulate as a duty event
        if (DateUtility.ConvertMillisecondsToHours(length) < 10.0)
            this.Summary.setDutyTimeAccumulated(this.Summary.getDutyTimeAccumulated() + length);

        if (this.getOriginalValidWeeklyResetDutyStartTimestamp() == null)
        	this.setOriginalValidWeeklyResetDutyStartTimestamp(this.Summary.getRecentDutyTimestamp());
        
        this.CalculateDailyReset(length);
        this.CalculateWeeklyReset(length);
    }
}
