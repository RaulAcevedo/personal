package com.jjkeller.kmbapi.calcengine.Canadian;

import android.os.Bundle;

import com.jjkeller.kmbapi.calcengine.Enums.RuleSetTypeEnum;
import com.jjkeller.kmbapi.calcengine.HoursOfServiceSummary;
import com.jjkeller.kmbapi.calcengine.LogProperties;
import com.jjkeller.kmbapi.calcengine.RulesetProperties;

public class Cycle2 extends CanadianRulesetBase {

    /// <summary>
    /// Amount of duty time allowed for Cycle 2 before a manadatory 24 hour 
    /// offduty is required
    /// </summary>
    protected long CYCLE2_REQUIRED_OFFDUTY_RESET;

    private static final String REGULATORY_SECTION_WEEKLY_DUTY = "(27)";
    private static final String REGULATORY_SECTION_CYCLE2_OFFDUTY = "(27)(b)";


    public Cycle2(RulesetProperties properties) 
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

        this.WEEKLY_DUTY_TIME_ALLOWED = 120 * MILLISECONDS_PER_HOUR;
        this.WEEKLY_DUTY_PERIOD_DAYS = 14;
        this.WEEKLY_OFFDUTY_HOURS_FOR_RESET = 72 * MILLISECONDS_PER_HOUR;
        this.CYCLE2_REQUIRED_OFFDUTY_RESET = 70 * MILLISECONDS_PER_HOUR;
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
		long avail = bundle.getLong(AVAIL);
		
        long cycle2RequiredOffDutyRule = summary.getCycle2OffDutyResetAmount();

        if (cycle2RequiredOffDutyRule < avail)
        {
            // less time according to required cycle 2 offduty rule        	
        	avail = cycle2RequiredOffDutyRule;
            bundle.putLong(AVAIL, cycle2RequiredOffDutyRule);
    		bundle.putLong(USED, this.CYCLE2_REQUIRED_OFFDUTY_RESET - cycle2RequiredOffDutyRule);
    		bundle.putInt(ALLOWED, (int) (this.CYCLE2_REQUIRED_OFFDUTY_RESET/MILLISECONDS_PER_HOUR));
    		bundle.putString(REGSECTION, REGULATORY_SECTION_CYCLE2_OFFDUTY);
        }

        // if avail goes negative, bring it back to zero
        if (avail < 0)
        {
        	avail = 0;
            bundle.putLong(AVAIL, avail);
        }

        if(!bundle.containsKey(REGSECTION) || bundle.getString(REGSECTION) == null || bundle.getString(REGSECTION).equals(""))
        	bundle.putString(REGSECTION, REGULATORY_SECTION_WEEKLY_DUTY);
        
        return bundle;
    }

    /// <summary>
    /// Prepare to start processing a new log
    /// </summary>
    /// <param name="logDate">timestamp of the start of the log date being processed</param>
    /// <param name="isTodaysLog">flag to indicate that this is today's log being processed</param>
    /// <param name="hasReturnedToWorkLocation">has the driver returned to the normal work location</param>
    /// <param name="isShortHaulExceptionUsed">has the short-haul exception been used</param>
    /// <param name="deferralType">Canadian ruleset off-duty deferral type for the log</param>
    @Override
    public void PrepareStartOfLog(LogProperties logProperties)
    {
        super.PrepareStartOfLog(logProperties);

        if (this.Summary.getValidityTimestamp() == null)
        {
            // this is the very first log pushed through the engine
            // Assume a 24 hour off-duty period has occurred so reset the amount of time allowed before the next one
            this.Summary.setCycle2OffDutyResetAmount(this.CYCLE2_REQUIRED_OFFDUTY_RESET);
        }
    }

    /// <summary>
    /// Process an on-duty status event.
    /// Accumulate the duty time accordingly.
    /// Keep track of all duty time towards the Cycle 2 24 OffDuty requirement.
    /// </summary>
    /// <param name="length"></param>
    @Override 
    protected void ProcessOnDuty(long length)
    {
        super.ProcessOnDuty(length);

        // accumulate the time toward the 70 hour limit before a mandatory 24 hour
        // off-duty period must occur
        this.Summary.setCycle2OffDutyResetAmount(this.Summary.getCycle2OffDutyResetAmount() - length);
    }

    /// <summary>
    /// Perform any special processing that occurs when a 24 consecutive off-duty
    /// period occurs.
    /// For Canadian Cycle 2, there is a mandatory 24 hour off-duty period required
    /// every 70 hours of duty time.   Since the 24 offduty occcurred, reset
    /// the counter for the 70 hour period.
    /// </summary>
    /// <param name="length"></param>
    @Override 
    protected void Process24HourOffDutyPeriod(long length)
    {
        super.Process24HourOffDutyPeriod(length);

        // 24 hour off-duty period has occurred so reset the amount of time allowed before the next 
        this.Summary.setCycle2OffDutyResetAmount(this.CYCLE2_REQUIRED_OFFDUTY_RESET);
    }

    /// <summary>
    /// A ruleset transition has been detected from the oldruleset to the newruleset
    /// Perform any special processing that may result because of this.
    /// For a transition from Cycle1 to Cycle2, this will cause a weekly reset.
    /// </summary>
    /// <param name="oldRuleset"></param>
    /// <param name="newRuleset"></param>
    @Override 
    protected void ProcessRulesetTransition(RuleSetTypeEnum oldRuleset, RuleSetTypeEnum newRuleset)
    {
        super.ProcessRulesetTransition(oldRuleset, newRuleset);

        if (oldRuleset == RuleSetTypeEnum.Canadian_Cycle1 && newRuleset == RuleSetTypeEnum.Canadian_Cycle2)
        {
            // when transitioning from Cycle1 to Cycle2 a full reset will occur
            this.Summary.setDriveTimeAccumulated(0);
            this.Summary.setDutyTimeAccumulated(0);
            this.Summary.setWorkShiftTimeAccumulated(0);
            this.Summary.setWeeklyDutyTimeAccumulated(0);

            // note: the hourly total for the logevent causing the ruleset transition
            // will already be recorded and can not be reset
            this.ResetDailyDutyTotalsExceptCurrent();
        }
    }
}
