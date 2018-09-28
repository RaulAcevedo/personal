package com.jjkeller.kmbapi.calcengine;

import android.os.Bundle;

import com.jjkeller.kmbapi.calcengine.Enums.DutyStatusEnum;
import com.jjkeller.kmbapi.calcengine.Enums.RuleSetTypeEnum;

import java.util.Date;

public interface IHosRulesetCalcEngine {

    /// <summary>
    /// Initialize the calc engine.
    /// This must be called before any other methods are called on the calc engine.
    /// </summary>
	void Initialize(RulesetProperties properties);
	
    /// <summary>
    /// Perform a check of the duration and answer the duration.  This is method is 
    /// not meant to be signify the end of a duty status period.   It is intended to be
    /// an informational check on the summary of hours available.
    /// Answer a summary of the duty hours available.
    /// </summary>
    /// <param name="dutyTimestamp">timestamp of the event</param>
    /// <param name="dutyStatus">duty status of the event</param>
    /// <param name="dutyLength">length of the event</param>
    /// <param name="ruleset">ruleset that the event was under</param>
    /// <returns>summary of hours available</returns>
	HoursOfServiceSummary CheckDutyStatusDuration(Date dutyTimestamp, DutyStatusEnum dutyStatus, long dutyLength, RuleSetTypeEnum ruleset);
	
    /// <summary>
    /// The end of a duty status period has occured.   Calculate a summary of the 
    /// duty hours available.
    /// Answer a summary of the duty hours available.
    /// </summary>
    /// <param name="dutyTimestamp">timestamp of the event</param>
    /// <param name="dutyStatus">duty status of the event</param>
    /// <param name="dutyLength">length of the event</param>
    /// <param name="ruleset">ruleset that the event was under</param>
    /// <returns>summary of hours available</returns>
    HoursOfServiceSummary EndOfDutyStatusUpdate(Date dutyTimestamp, DutyStatusEnum dutyStatus, long dutyLength, RuleSetTypeEnum ruleset);
	
    /// <summary>
    /// Answer the date of the start of the duty period specified by the ending date.
    /// </summary>
    /// <param name="endingDate">Ending date of the duty period</param>
    /// <returns></returns>
    Date DateOfAuditPeriodStart(Date endingDate);

    /// <summary>
    /// Answer the end of the duty period specified by the starting date
    /// </summary>
    /// <param name="startingDate">Starting date of the duty period</param>
    /// <returns></returns>
    Date DateOfAuditPeriodEnd(Date startingDate);

    /**
     * Gets the number of days that make up the duty period for the ruleset
     * @return The number of days that make up the duty period for the ruleset
     */
    int GetWeeklyDutyPeriodDays();

    /// <summary>
    /// Answer the Hours of Service summary for the engine
    /// </summary>
    HoursOfServiceSummary getDutySummary();

    /// <summary>
    /// Prepare to start processing a new log
    /// </summary>
    /// <param name="logDate">log date being processed</param>
    /// <param name="isTodaysLog">flag to indicate that this is today's log being processed</param>
    /// <param name="hasReturnedToWorkLocation">has the driver returned to the normal work location</param>
    /// <param name="isShortHaulExceptionUsed">has the short-haul exception been used</param>
    /// <param name="deferralType">Canadian ruleset off-duty deferral type for the log</param>
    /// <param name="weeklyResetStartTimestamp">Date that a valid weekly reset was started.  When this is set, it means that this log contains a reset</param>
    /// <param name="isHaulingExplosives">flag to indicate whether explosives are being hauled, this enables a special provision of the 8 hour driving rule</param>
    //void PrepareStartOfLog(Date logDate, boolean isTodaysLog, boolean hasReturnedToWorkLocation, boolean isShortHaulExceptionUsed, CanadaDeferralTypeEnum deferralType, Date weeklyResetStartTimestamp, boolean isHaulingExplosives, boolean isWeeklyResetUsed, boolean IsWeeklyResetUsedOverridden);
    void PrepareStartOfLog(LogProperties logProperties);

    /// <summary>
    /// When the log has completed being pushed into the calcEngine, remaining
    /// state data values can be returned.   These values may have changed through
    /// the calculation process for that days log.
    /// </summary>
    /// <param name="logDate"></param>
    /// Returns:  boolean indicating if short haul exception was used
    Bundle FetchAtEndOfLog(Date logDate);

    /// <summary>
    /// Answer the duty summary of the WEEKLY rules
    /// </summary>
    /// <param name="summary">summary to look at</param>
    /// Returns:  Bundle containing:
    /// 	- used(long): amount of time used under the rules
    /// 	- avail(long): amount of time remaining under the rules
    /// 	- allowedHours(int): allowed amount of time under the rules
    ///		- regulatorySection(string): string identifying regulatory section of rules
    Bundle WeeklyDutySummary(HoursOfServiceSummary summary);

    /// <summary>
    /// Answer the duty summary of the DAILY Duty rules.
    /// </summary>
    /// <param name="summary">summary to look at</param>
    /// Returns:  Bundle containing:
    /// 	- used(long): amount of time used under the rules
    /// 	- avail(long): amount of time remaining under the rules
    /// 	- allowedHours(int): allowed amount of time under the rules
    ///		- regulatorySection(string): string identifying regulatory section of rules
    Bundle DailyDutySummary(HoursOfServiceSummary summary);

    /// <summary>
    /// Answer the duty summary of the DAILY Driving rules.
    /// </summary>
    /// <param name="summary">summary to look at</param>
    /// Returns:  Bundle containing:
    /// 	- used(long): amount of time used under the rules
    /// 	- avail(long): amount of time remaining under the rules
    /// 	- allowedHours(int): allowed amount of time under the rules
    ///		- regulatorySection(string): string identifying regulatory section of rules
    Bundle DailyDriveSummary(HoursOfServiceSummary summary);

    /// <summary>
    /// Answer the duty summary of the DAILY Driving rules.
    /// </summary>
    /// <param name="summary">summary to look at</param>
    /// Returns:  Bundle containing:
    /// 	- used(long): amount of time used under the rules
    /// 	- avail(long): amount of time remaining under the rules
    /// 	- allowedHours(int): allowed amount of time under the rules
    ///		- regulatorySection(string): string identifying regulatory section of rules
    Bundle DailyDriveRestBreakSummary(HoursOfServiceSummary summary);
    
    /// <summary>
    /// Calculate the amount of drive time available for the specified summary (in milliseconds)
    /// </summary>
    /// <param name="summary"></param>
    /// <returns></returns>
    long CalculateDriveTimeRemaining(HoursOfServiceSummary summary);
    
  /// <summary>
    /// Calculate the amount of rest break time available for the specified summary (in milliseconds)
    /// </summary>
    /// <param name="summary"></param>
    /// <returns></returns>
    long CalculateRestBreakTimeRemaining(HoursOfServiceSummary summary);

}
