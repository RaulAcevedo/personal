package com.jjkeller.kmbapi.calcengine.Canadian;

import android.os.Bundle;

import com.jjkeller.kmbapi.calcengine.HoursOfServiceSummary;
import com.jjkeller.kmbapi.calcengine.RulesetProperties;
import com.jjkeller.kmbapi.controller.utility.DateUtility;

import java.util.Date;

public class Cycle1 extends CanadianRulesetBase {

    private static final String WEEKLY_DUTY_REGULATORY_SECTION = "(26)";
    private static final int AUDIT_PERIOD_DAYS = 14;

    public Cycle1(RulesetProperties properties) 
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
        super.Initialize(properties);

        this.WEEKLY_DUTY_TIME_ALLOWED = 70 * MILLISECONDS_PER_HOUR;
        this.WEEKLY_DUTY_PERIOD_DAYS = 7;
        this.WEEKLY_OFFDUTY_HOURS_FOR_RESET = 36 * MILLISECONDS_PER_HOUR;
    }

    /// <summary>
    /// Answer the date of the start of the duty period specified by the ending date.
    /// This is for the purpose of performing a Duty Service Audit.
    /// </summary>
    /// <param name="endingDate">Ending date of the duty period</param>
    /// <returns></returns>
    @Override 
    public Date DateOfAuditPeriodStart(Date fromEndingDate)
    {
        // 14 days preceding the date
        return DateUtility.AddDays(fromEndingDate, AUDIT_PERIOD_DAYS * -1);
    }

    /// <summary>
    /// Answer the duty summary of the WEEKLY rules
    /// </summary>
    /// <param name="summary">summary to look at</param>
    /// <param name="used">amount of time used under the rules</param>
    /// <param name="avail">amount of time remaining under the rules</param>
    /// <param name="allowedHours">allowed amount of time under the rules</param>
    @Override 
    public Bundle WeeklyDutySummary(HoursOfServiceSummary summary)
    {
        Bundle bundle = super.WeeklyDutySummary(summary);
        if(!bundle.containsKey(REGSECTION) || bundle.getString(REGSECTION) == null || bundle.getString(REGSECTION).equals(""))
        	bundle.putString(REGSECTION, WEEKLY_DUTY_REGULATORY_SECTION);
        return bundle;
    }
}
