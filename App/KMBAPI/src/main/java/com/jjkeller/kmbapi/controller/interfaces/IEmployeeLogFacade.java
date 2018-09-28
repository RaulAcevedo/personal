package com.jjkeller.kmbapi.controller.interfaces;

import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;

import java.util.Date;
import java.util.List;

public interface IEmployeeLogFacade {

	public abstract List<EmployeeLog> Fetch();

	public abstract List<EmployeeLog> GetLocalLogList();

	public abstract List<EmployeeLog> GetServerLogList();

	public abstract void Save(EmployeeLog empLogData, int logSourceStatusEnum);

	public abstract EmployeeLog GetLogByDate(Date logDate);

	public abstract List<EmployeeLog> GetLogsByDate(Date logDate);

	public abstract List<EmployeeLog> GetLogListByDateRange(Date startDate,
			Date endDate);

	public abstract EmployeeLog GetLocalLogByDate(Date logDate);

	public abstract EmployeeLog GetServerLogByDate(Date logDate);

	public abstract void DeleteLog(EmployeeLog empLog);

	/// <summary>
	/// Answer the most recent log event for the current user
	/// </summary>
	public abstract EmployeeLogEldEvent FetchMostRecentLogEventForUser();

	public abstract EmployeeLogEldEvent FetchMostRecentLogEventForUser(Date logDate);

	/// <summary>
	/// Answer the most recent log event for the current user that contains
	/// an assignment of a US Federal ruleset (US60, US70)
	/// </summary>
	/// <returns></returns>
	public abstract EmployeeLogEldEvent FetchMostRecentUSLogEventForUser();

	/// <summary>
	/// Answer the most recent log event for the current user that contains
	/// an assignment of a Canadian ruleset (C1, C2)
	/// </summary>
	/// <returns></returns>
	public abstract EmployeeLogEldEvent FetchMostRecentCanadianLogEventForUser();

	/// Fetch the count of all logs for the user that are either unsubmitted, 
	/// or server logs with a logDate between the start and end date (inclusive).
	/// If a log is both a server and unsubmitted log, it will only be counted
	/// once.
	public abstract int FetchLogCount(Date startDate, Date endDate);

	public abstract int FetchLogCountByStatus(int status);

	/// Fetch the list of dates for all of the logs that the user has which are
	/// either unsubmitted or server logs.  The logDate list will be between start
	/// and end date (inclusive).  If a log is both a server and unsubmitted log, 
	/// it will only be counted once.
	public abstract List<Date> FetchLogList(Date startDate, Date endDate);

	public abstract List<EmployeeLog> GetLocalLogList(boolean excludeTodaysLog);

	public abstract List<EmployeeLog> GetLocalLogListAllUsers(
			boolean excludeTodaysLog);



	public abstract Date GetFirstAvailableLogDate();

	public abstract void UpdateLogStatus(EmployeeLog log, int newStatus);

	/// <summary>
	/// Answer a list of all logEvents, across all logs, that require 
	/// reverse geocoding
	/// </summary>
	/// <returns></returns>
	public abstract List<EmployeeLogEldEvent> FetchUnsubmittedLocationsThatRequireGeocoding();

	/// <summary>
	/// Update the geocoded location info for the LogEvents
	/// </summary>
	/// <param name="logEventList"></param>
	public abstract void UpdateGeocodedLocations(List<EmployeeLogEldEvent> logEventList);

	/**
	 * Updates just the end odometer of the log event
	 * @param logEvent The log event to update
	 */
	public abstract void UpdateEndOdometer(EmployeeLogEldEvent logEvent);

	public abstract void PurgeOldRecords(Date cutoffDate);

	public abstract EmployeeLog FetchWeeklyReset(Date logDate);

	public abstract Date FetchPreviousWeeklyResetStartTimestamp(Date logDate);

	public abstract List<EmployeeLog> GetLogsByWeeklyResetStartTimestamp(
			Date weeklyResetStartTimestamp);

	public abstract boolean DoesWeeklyResetExistNewerThan(Date aDate);

	public abstract boolean ExistAnyLogEarlierThan(Date logDate);

	/**
	 * Gets all log dates that either have no ELD events or the last ELD event is not a certification event.
	 * If none exist, an empty list is returned.
	 * @return A list of log dates that have been certified but unsubmitted. If none exist, an empty list is returned.
	 */
	public abstract List<Date> FetchCertifiedUnsubmittedLogDates();

	/**
	 * Gets all log dates that either have no ELD events or the last ELD event is not a certification event.
	 * If none exist, an empty list is returned.
	 * @return A list of log dates that have not been certified. If none exist, an empty list is returned.
	 */
	public abstract List<Date> FetchUncertifiedLogDates();

    /**
     * Gets all logs with ELDEvents that  need to be reviewed for edits
     */
    public abstract List<EmployeeLog> FetchLogsWithUnreviewedEdits();

}