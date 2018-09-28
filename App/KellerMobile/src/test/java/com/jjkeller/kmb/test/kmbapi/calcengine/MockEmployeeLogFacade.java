package com.jjkeller.kmb.test.kmbapi.calcengine;

import com.jjkeller.kmbapi.controller.interfaces.IEmployeeLogFacade;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MockEmployeeLogFacade implements IEmployeeLogFacade {

//	EmployeeLogFacade _realFacade;

    private ArrayList<EmployeeLog> _logList;

//	public MockEmployeeLogFacade(Context ctx, User user)
//	{
//		_realFacade = new EmployeeLogFacade(ctx, user);
//	}

    public void setLogList(ArrayList<EmployeeLog> logList) {
        _logList = logList;
    }

    @Override
    public List<EmployeeLog> Fetch() {
        return null;
    }

    @Override
    public List<EmployeeLog> GetLocalLogList() {
        return null;
    }


    @Override
    public List<EmployeeLog> GetServerLogList() {
        return null;
    }

    @Override
    public void Save(EmployeeLog empLogData, int logSourceStatusEnum) {

    }

    @Override
    public EmployeeLog GetLogByDate(Date logDate) {

        if (_logList != null) {
            for (EmployeeLog log : _logList) {
                if (log.getLogDate().equals(logDate))
                    return log;
            }
        }

        return null;
    }

    @Override
    public List<EmployeeLog> GetLogsByDate(Date logDate) {
        return null;
    }

    @Override
    public List<EmployeeLog> GetLogListByDateRange(Date startDate, Date endDate) {
        List<EmployeeLog> logs = new ArrayList<>();

        if (_logList != null) {
            for (EmployeeLog log : _logList) {
                if (log.getLogDate().compareTo(startDate) >= 0 && log.getLogDate().compareTo(endDate) <= 0)
                    logs.add(log);
            }
        }

        return logs;
    }

    @Override
    public EmployeeLog GetLocalLogByDate(Date logDate) {
        return null;
    }

    @Override
    public EmployeeLog GetServerLogByDate(Date logDate) {
        return null;
    }

    @Override
    public void DeleteLog(EmployeeLog empLog) {

    }

    @Override
    public EmployeeLogEldEvent FetchMostRecentLogEventForUser() {
        return null;
    }

    @Override
    public EmployeeLogEldEvent FetchMostRecentLogEventForUser(Date logDate) {
        return null;
    }

    @Override
    public EmployeeLogEldEvent FetchMostRecentUSLogEventForUser() {
        return null;
    }

    @Override
    public EmployeeLogEldEvent FetchMostRecentCanadianLogEventForUser() {
        return null;
    }

    @Override
    public int FetchLogCount(Date startDate, Date endDate) {
        return 0;
    }

    @Override
    public int FetchLogCountByStatus(int status) {
        return 0;
    }

    @Override
    public List<Date> FetchLogList(Date startDate, Date endDate) {
        return null;
    }

    @Override
    public List<EmployeeLog> GetLocalLogList(boolean excludeTodaysLog) {
        return null;
    }

    @Override
    public List<EmployeeLog> GetLocalLogListAllUsers(boolean excludeTodaysLog) {
        return null;
    }

    @Override
    public Date GetFirstAvailableLogDate() {
        return null;
    }

    @Override
    public void UpdateLogStatus(EmployeeLog log, int newStatus) {

    }

    @Override
    public List<EmployeeLogEldEvent> FetchUnsubmittedLocationsThatRequireGeocoding() {
        return null;
    }

    @Override
    public void UpdateGeocodedLocations(List<EmployeeLogEldEvent> logEventList) {

    }

    @Override
    public void UpdateEndOdometer(EmployeeLogEldEvent logEvent) {

    }

    @Override
    public void PurgeOldRecords(Date cutoffDate) {

    }

    @Override
    public EmployeeLog FetchWeeklyReset(Date logDate) {
        return null;
    }

    @Override
    public Date FetchPreviousWeeklyResetStartTimestamp(Date logDate) {
        return null;
    }

    @Override
    public List<EmployeeLog> GetLogsByWeeklyResetStartTimestamp(
            Date weeklyResetStartTimestamp) {
        return null;
    }

    @Override
    public boolean DoesWeeklyResetExistNewerThan(Date aDate) {
        return false;
    }

    @Override
    public boolean ExistAnyLogEarlierThan(Date logDate) {
        return false;
    }

    @Override
    public List<Date> FetchUncertifiedLogDates() {
        return null;
    }

    @Override
    public List<Date> FetchCertifiedUnsubmittedLogDates() {
        return null;
    }

    @Override
    public List<EmployeeLog> FetchLogsWithUnreviewedEdits() {
        return null;
    }

}
