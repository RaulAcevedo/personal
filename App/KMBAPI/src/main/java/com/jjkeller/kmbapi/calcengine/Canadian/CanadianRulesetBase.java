package com.jjkeller.kmbapi.calcengine.Canadian;

import android.os.Bundle;

import com.jjkeller.kmbapi.calcengine.Enums.CanadaDeferralTypeEnum;
import com.jjkeller.kmbapi.calcengine.HoursOfServiceSummary;
import com.jjkeller.kmbapi.calcengine.LogProperties;
import com.jjkeller.kmbapi.calcengine.RulesetBase;
import com.jjkeller.kmbapi.calcengine.RulesetProperties;
import com.jjkeller.kmbapi.calcengine.SplitSleeperCombination;
import com.jjkeller.kmbapi.controller.utility.DateUtility;

import java.util.Date;

public class CanadianRulesetBase extends RulesetBase {

	CanadianRulesetBase(RulesetProperties properties) {
		super(properties);
	}

    /// <summary>
    /// Amount of duty time allowed in a single log day.
    /// </summary>
    protected long LOGDATE_DUTY_TIME_ALLOWED;

    /// <summary>
    /// Amount of drive time allowed in a single log day.
    /// </summary>
    protected long LOGDATE_DRIVE_TIME_ALLOWED;

    /// <summary>
    /// Amount of drive time allowed in a single log day.
    /// </summary>
    protected long LOGDATE_DRIVE_TIME_ALLOWED_STANDARD;

    /// <summary>
    /// Amount of duty time allowed before it is required to take a 24 offduty.
    /// </summary>
    protected long WEEKLY_TIME_UNTIL_MANDATORY_OFFDUTY;

    /// <summary>
    /// Amount of drive time allowed across the complete 2 day deferral.
    /// </summary>
    protected long DEFERRAL_DRIVE_TIME_TWO_DAY_TOTAL;

    /// <summary>
    /// Amount of time that the work shift may last
    /// </summary>
    protected long WORKSHIFT_LENGTH_ALLOWED;

    /// <summary>
    /// Indicates whether a team driver is present
    /// </summary>
    protected boolean IS_TEAM_DRIVER_PRESENT;

    private static final String REGULATORY_SECTION_WORKSHIFT_DRIVE = "(13)(1)";
    private static final String REGULATORY_SECTION_WORKSHIFT_ONDUTY = "(13)(2)";
    private static final String REGULATORY_SECTION_WORKSHIFT_ELAPSED = "(13)(3)";
    private static final String REGULATORY_SECTION_LOGDATE_DRIVE = "(12)(1)";
    private static final String REGULATORY_SECTION_LOGDATE_ONDUTY = "(12)(2)";
    private static final String REGULATORY_SECTION_WEEKLY_OFFDUTY = "(25)";
    
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

        // the amount of offduty hours required to reset the work shift
        this.DAILY_OFFDUTY_HOURS_FOR_RESET = 8 * MILLISECONDS_PER_HOUR;

        // the following is the amount of duty time allowed within the work shift
        this.DAILY_DUTY_TIME_ALLOWED = 14 * MILLISECONDS_PER_HOUR;

        // the following is the amount of drive time allowed for the work shift
        this.DAILY_DRIVE_TIME_ALLOWED = 13 * MILLISECONDS_PER_HOUR;

        // the following is the amount of duty time allowed for the log date
        this.LOGDATE_DUTY_TIME_ALLOWED = 14 * MILLISECONDS_PER_HOUR;

        // the following is the amount of drive time allowed for the log date
        this.LOGDATE_DRIVE_TIME_ALLOWED_STANDARD = 13 * MILLISECONDS_PER_HOUR;
        this.LOGDATE_DRIVE_TIME_ALLOWED = LOGDATE_DRIVE_TIME_ALLOWED_STANDARD;

        // the following is the amount of elapsed time that the work shift is allowed to be
        this.WORKSHIFT_LENGTH_ALLOWED = 16 * MILLISECONDS_PER_HOUR;

        // the following is the amount of time before a required 24 off duty period must be taken 
        this.WEEKLY_TIME_UNTIL_MANDATORY_OFFDUTY = 15 * MILLISECONDS_PER_DAY;

        // the following is the total maximum amount of drive time across a two day deferral
        this.DEFERRAL_DRIVE_TIME_TWO_DAY_TOTAL = 26 * MILLISECONDS_PER_HOUR;

        this.IS_TEAM_DRIVER_PRESENT = properties.getIsTeamDriverPresent();

        this.Summary.setCombinableOffDutyPeriod(SplitSleeperCombination.ForCanadianRules(this.IS_TEAM_DRIVER_PRESENT));

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
		long used = 0;
		long avail = 0;
		int allowedHours = (int)(this.WEEKLY_DUTY_TIME_ALLOWED/MILLISECONDS_PER_HOUR);
		String regulatorySection = null;

        used = summary.getWeeklyDutyTimeAccumulated();
        if (this.WEEKLY_DUTY_TIME_ALLOWED > used)
        {
            avail = this.WEEKLY_DUTY_TIME_ALLOWED - used;
        }

        // Check to see if there needs to be 24hour offduty period coming up     
        Date startOfNextLogDay = DateUtility.AddDays(summary.getLogStartTimestamp(), 1);
        long timeUntilWeeklyOffDuty = startOfNextLogDay.getTime() - summary.getRecent24HourOffDutyPeriod().getTime();
        long weeklyOffDutyRule = WEEKLY_TIME_UNTIL_MANDATORY_OFFDUTY - timeUntilWeeklyOffDuty;
        if (weeklyOffDutyRule <avail)
        {
            avail = weeklyOffDutyRule;
            regulatorySection = REGULATORY_SECTION_WEEKLY_OFFDUTY;
        }

        // if avail goes negative, bring it back to zero
        if (avail < 0) avail = 0;
        
		Bundle bundle = new Bundle();
		bundle.putLong(USED, used);
		bundle.putLong(AVAIL, avail);
		bundle.putInt(ALLOWED, allowedHours);
		bundle.putString(REGSECTION, regulatorySection);
		
		return bundle;
    }

    /// <summary>
    /// Answer the duty summary of the DAILY Duty rules.
    /// Compare the duty time under the Workshift rules and the Log day rules
    /// to determine which is less.  
    /// </summary>
    /// <param name="summary">summary to look at</param>
    /// <param name="used">amount of time used under the rules</param>
    /// <param name="avail">amount of time remaining under the rules</param>
    /// <param name="allowedHours">allowed amount of time under the rules</param>
    @Override
    public Bundle DailyDutySummary(HoursOfServiceSummary summary)
    {
		long used = 0;
		long avail = 0;
		int allowedHours = (int)(this.DAILY_DUTY_TIME_ALLOWED/MILLISECONDS_PER_HOUR);
		String regulatorySection = null;

        long dailyDutyRule = DAILY_DUTY_TIME_ALLOWED - summary.getDutyTimeAccumulated();
        long logDayDutyRule = LOGDATE_DUTY_TIME_ALLOWED - summary.getLogDateDutyTimeAccumulated();
        long workshiftElapsedRule = WORKSHIFT_LENGTH_ALLOWED - summary.getWorkShiftTimeAccumulated();

        // assume it's the Daily Duty (workshift) rules to start with
        avail = dailyDutyRule;
        used = summary.getDutyTimeAccumulated();
        regulatorySection = REGULATORY_SECTION_WORKSHIFT_ONDUTY;

        if (this.Summary.getCanadaDeferralType() == CanadaDeferralTypeEnum.None &&
            logDayDutyRule < avail)
        {
            // less time according to log day rule, and not in deferral mode
            // when in a deferral, the log date duty time rule is skipped
            avail = logDayDutyRule;
            used = summary.getLogDateDutyTimeAccumulated();
            regulatorySection = REGULATORY_SECTION_LOGDATE_ONDUTY;
            allowedHours = (int)DateUtility.ConvertMillisecondsToHours(this.LOGDATE_DUTY_TIME_ALLOWED);
        }

        if (workshiftElapsedRule < avail)
        {
            // less time according to work shift elapsed time rule (16 hours in shift)
            avail = workshiftElapsedRule;
            used = summary.getWorkShiftTimeAccumulated();
            regulatorySection = REGULATORY_SECTION_WORKSHIFT_ELAPSED;
            allowedHours = (int)DateUtility.ConvertMillisecondsToHours(this.WORKSHIFT_LENGTH_ALLOWED);
        }

        // if avail goes negative, bring it back to zero
        if (avail < 0) avail = 0;

		Bundle bundle = new Bundle();
		bundle.putLong(USED, used);
		bundle.putLong(AVAIL, avail);
		bundle.putInt(ALLOWED, allowedHours);
		bundle.putString(REGSECTION, regulatorySection);
		
		return bundle;		
    }

    /// <summary>
    /// Answer the duty summary of the DAILY Duty rules.
    /// Compare the duty time under the Workshift rules and the Log day rules
    /// to determine which is less.  
    /// </summary>
    /// <param name="summary">summary to look at</param>
    /// <param name="used">amount of time used under the rules</param>
    /// <param name="avail">amount of time remaining under the rules</param>
    /// <param name="allowedHours">allowed amount of time under the rules</param>
    @Override
    public Bundle DailyDriveSummary(HoursOfServiceSummary summary)
    {
		long used = 0;
		long avail = 0;
		int allowedHours = (int)(this.DAILY_DRIVE_TIME_ALLOWED/MILLISECONDS_PER_HOUR);
		String regulatorySection = null;
		
        long dailyDriveRule = DAILY_DRIVE_TIME_ALLOWED - summary.getDriveTimeAccumulated();
        long logDayDriveRule = LOGDATE_DRIVE_TIME_ALLOWED - summary.getLogDateDriveTimeAccumulated();

        // assume it's the Daily Duty (workshift) rules to start with
        avail = dailyDriveRule;
        used = summary.getDriveTimeAccumulated();
        regulatorySection = REGULATORY_SECTION_WORKSHIFT_DRIVE;

        if (this.Summary.getCanadaDeferralType() != CanadaDeferralTypeEnum.DayOne &&
            logDayDriveRule < avail)
        {
            // less time according to log day rule, and not in DayOne deferral mode
            // the "day one" logs skip the log date checking
            avail = logDayDriveRule;
            used = summary.getLogDateDriveTimeAccumulated();
            regulatorySection = REGULATORY_SECTION_LOGDATE_DRIVE;
        }

        // if avail goes negative, bring it back to zero
        if (avail < 0) avail = 0;

		Bundle bundle = new Bundle();
		bundle.putLong(USED, used);
		bundle.putLong(AVAIL, avail);
		bundle.putInt(ALLOWED, allowedHours);
		bundle.putString(REGSECTION, regulatorySection);
		
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
        this.LOGDATE_DRIVE_TIME_ALLOWED = this.LOGDATE_DRIVE_TIME_ALLOWED_STANDARD;
        this.Summary.setCanadaDeferralType(logProperties.getCanadaDeferralType());

        if (logProperties.getCanadaDeferralType() == CanadaDeferralTypeEnum.DayTwo)
        {
            // on the DayTwo deferral, the drive time allowed for the logdate
            // is adjusted based on what happened the previous day
            this.LOGDATE_DRIVE_TIME_ALLOWED = this.LOGDATE_DRIVE_TIME_ALLOWED_STANDARD;

            if (this.Summary != null && 
                this.Summary.getLogStartTimestamp() != null && 
                this.Summary.getLogStartTimestamp().compareTo(DateUtility.AddDays(logProperties.getLogDate(), -1)) == 0)
            {
                // have just processed yesterdays log through the engine
                // determine how much drive time is available for today
                long dayTwoAllowed = this.DEFERRAL_DRIVE_TIME_TWO_DAY_TOTAL - this.Summary.getLogDateDriveTimeAccumulated();
                if (dayTwoAllowed < 0)
                    dayTwoAllowed = 0;
                this.LOGDATE_DRIVE_TIME_ALLOWED = dayTwoAllowed;
            }
        }

        super.PrepareStartOfLog(logProperties);
    }

    /// <summary>
    /// Process an on-duty status event.
    /// Accumulate the duty time accordingly.
    /// </summary>
    /// <param name="length"></param>
    @Override
    protected void ProcessOnDuty(long length)
    {
        this.Summary.setDutyTimeAccumulated(this.Summary.getDutyTimeAccumulated() + length);
        this.Summary.setWeeklyDutyTimeAccumulated(this.Summary.getWeeklyDutyTimeAccumulated() + length);
        this.Summary.setWorkShiftTimeAccumulated(this.Summary.getWorkShiftTimeAccumulated() + length);

        this.Summary.setDailyResetAmount(DAILY_OFFDUTY_HOURS_FOR_RESET);
        this.Summary.setWeeklyResetAmount(WEEKLY_OFFDUTY_HOURS_FOR_RESET);

        this.Summary.getCombinableOffDutyPeriod().ProcessOnDutyTime(length);

        // accumulate only the amount of time that occurred in the current log
        long eventLengthInLogDay = length;
        if (this.Summary.getLogStartTimestamp().compareTo(this.Summary.getRecentDutyTimestamp()) > 0)
        {
            // the current event started before the beginning of the log
            // shorten the length of the event by the amount that happened yesterday
            long diffPriorToStartOfLog = this.Summary.getLogStartTimestamp().getTime() - this.Summary.getRecentDutyTimestamp().getTime();
            eventLengthInLogDay = length - diffPriorToStartOfLog;
        }
        this.Summary.setLogDateDutyTimeAccumulated(this.Summary.getLogDateDutyTimeAccumulated() + eventLengthInLogDay);
    }

    /// <summary>
    /// Process the driving status event.
    /// Accumulate the driving time as an on-duty event also.
    /// </summary>
    /// <param name="length"></param>
    @Override
    protected void ProcessDriveDuty(long length)
    {
        // process as an on-duty event first
        this.ProcessOnDuty(length);

        // accumulate driving specific numbers
        this.Summary.setDriveTimeAccumulated(this.Summary.getDriveTimeAccumulated() + length);

        this.Summary.getCombinableOffDutyPeriod().ProcessDriveTime(length);

        // accumulate only the amount of time that occurred in the current log
        long eventLengthInLogDay = length;
        if (this.Summary.getLogStartTimestamp().compareTo(this.Summary.getRecentDutyTimestamp()) > 0)
        {
            // the current event started before the beginning of the log
            // shorten the length of the event by the amount that happened yesterday
            long diffPriorToStartOfLog = this.Summary.getLogStartTimestamp().getTime() - this.Summary.getRecentDutyTimestamp().getTime();
            eventLengthInLogDay = length - diffPriorToStartOfLog;
        }
        this.Summary.setLogDateDriveTimeAccumulated(this.Summary.getLogDateDriveTimeAccumulated() + eventLengthInLogDay);
    }

    /// <summary>
    /// Process the off-duty event.
    /// </summary>
    /// <param name="length"></param>
    @Override
    protected void ProcessOffDuty(long length)
    {
        this.Summary.setWorkShiftTimeAccumulated(this.Summary.getWorkShiftTimeAccumulated() + length);

        this.Summary.getCombinableOffDutyPeriod().ProcessOffDutyTime(length, this.Summary);

        this.CalculateDailyReset(length);
        this.CalculateWeeklyReset(length);
    }

    /// <summary>
    /// Process the sleeper status.
    /// </summary>
    /// <param name="length"></param>
    @Override
    protected void ProcessSleepDuty(long length)
    {
        if (DateUtility.ConvertMillisecondsToHours(length) < 8.0)
        {
            this.Summary.setWorkShiftTimeAccumulated(this.Summary.getWorkShiftTimeAccumulated() + length);

            this.Summary.getCombinableOffDutyPeriod().ProcessSleeperTime(length, this.Summary);

            this.CalculateDailyReset(length);
            this.CalculateWeeklyReset(length);
        }
        else
        {
            if (this.Summary.getRecentDutyTimestamp().compareTo(this.Summary.getLogStartTimestamp()) != 0)
            {
                // as long as the off duty event doesn't start at the beginning of the log day
                // then accumulate it
                this.Summary.setWorkShiftTimeAccumulated(this.Summary.getWorkShiftTimeAccumulated() + length);
            }

            this.CalculateDailyReset(length);
            this.CalculateWeeklyReset(length);

            if (DateUtility.ConvertMillisecondsToHours(length) < 10.0)
            {
                this.Summary.getCombinableOffDutyPeriod().ProcessSleeperTime(length, this.Summary);
            }
        }
    }

	@Override
	protected void ProcessOffDutyWellsite(long length) {
		// nothing to do for Canada for off duty wellsite
	}

    /// <summary>
    /// Determine if a daily hour reset has occurred.
    /// When a daily reset occurs, then the daily hours in the summary are reset to zero.
    /// </summary>
    /// <param name="length"></param>
    @Override
    protected void CalculateDailyReset(long length)
    {
        this.Summary.setDailyResetAmount(this.Summary.getDailyResetAmount() - length);
        if (DateUtility.ConvertMillisecondsToMinutes(this.Summary.getDailyResetAmount()) <= 0.0)
        {
            this.Summary.setDriveTimeAccumulated(0);
            this.Summary.setDutyTimeAccumulated(0);
            this.Summary.setWorkShiftTimeAccumulated(0);
            this.Summary.setCombinableOffDutyPeriod(SplitSleeperCombination.ForCanadianRules(this.IS_TEAM_DRIVER_PRESENT));
        }

        if (DateUtility.ConvertMillisecondsToHours(length) >= 24.0)
        {
            this.Process24HourOffDutyPeriod(length);
        }
    }

    /// <summary>
    /// Determine if a weekly hour reset has occurred.
    /// When the reset occurs, then the summary is completely reset to zero.
    /// Also, allow the short-haul exception to be used again.
    /// </summary>
    /// <param name="length"></param>
    @Override
    protected void CalculateWeeklyReset(long length)
    {
        this.Summary.setWeeklyResetAmount(this.Summary.getWeeklyResetAmount() - length);
        if (DateUtility.ConvertMillisecondsToMinutes(this.Summary.getWeeklyResetAmount()) <= 0.0)
        {
            this.Summary.setDriveTimeAccumulated(0);
            this.Summary.setDutyTimeAccumulated(0);
            this.Summary.setWorkShiftTimeAccumulated(0);
            this.Summary.setWeeklyDutyTimeAccumulated(0);

            this.Summary.setCombinableOffDutyPeriod(SplitSleeperCombination.ForCanadianRules(this.IS_TEAM_DRIVER_PRESENT));

            this.ResetDailyDutyTotals();
        }
    }

    /// <summary>
    /// Perform any special processing that occurs when a 24 consecutive off-duty
    /// period occurs.
    /// </summary>
    /// <param name="length"></param>
    protected void Process24HourOffDutyPeriod(long length)
    {
        this.Summary.setRecent24HourOffDutyPeriod(this.Summary.getLogStartTimestamp());
    }
}
