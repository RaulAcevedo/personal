package com.jjkeller.kmbapi.controller.dataaccess;

import android.content.Context;
import android.util.Log;

import com.jjkeller.kmbapi.common.TimeKeeper;
import com.jjkeller.kmbapi.controller.dataaccess.db.EmployeeLogEldEventPersist;
import com.jjkeller.kmbapi.controller.dataaccess.db.EmployeeLogPersist;
import com.jjkeller.kmbapi.controller.interfaces.IEmployeeLogFacade;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.enums.RuleSetTypeEnum;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class EmployeeLogFacade extends FacadeBase implements IEmployeeLogFacade {
	
	public EmployeeLogFacade(Context ctx)
	{
		super(ctx);
	}

	public EmployeeLogFacade(Context ctx, User user)
	{
		super(ctx, user);
	}
	/* (non-Javadoc)
	 * @see com.jjkeller.kmbapi.controller.dataaccess.IEmployeeLogFacade#Fetch()
	 */
	public List<EmployeeLog> Fetch()
	{
		EmployeeLogPersist<EmployeeLog> persist = new EmployeeLogPersist<EmployeeLog>(EmployeeLog.class, this.getContext());
		return persist.FetchAllLocalLogs();
	}

	/* (non-Javadoc)
	 * @see com.jjkeller.kmbapi.controller.dataaccess.IEmployeeLogFacade#GetLocalLogList()
	 */
	public List<EmployeeLog> GetLocalLogList()
	{
		EmployeeLogPersist<EmployeeLog> persist = new EmployeeLogPersist<EmployeeLog>(EmployeeLog.class, this.getContext());
		return persist.FetchLocalLogsForUser();
	}

	/* (non-Javadoc)
	 * @see com.jjkeller.kmbapi.controller.dataaccess.IEmployeeLogFacade#GetServerLogList()
	 */
	public List<EmployeeLog> GetServerLogList()
	{
		EmployeeLogPersist<EmployeeLog> persist = new EmployeeLogPersist<EmployeeLog>(EmployeeLog.class, this.getContext());
		return persist.FetchServerLogsForUser();
	}

	/* (non-Javadoc)
	 * @see com.jjkeller.kmbapi.controller.dataaccess.IEmployeeLogFacade#Save(com.jjkeller.kmbapi.proxydata.EmployeeLog, int)
	 */
	public void Save(EmployeeLog empLogData, int logSourceStatusEnum)
	{		
		Log.d("EmpLog", String.format("EmpLogFacade.Save empCode: %s logDate: %s empHomeTerm: %s Tz.default: %s", 
				this.getCurrentUser().getCredentials().getEmployeeCode(), empLogData.getLogDate(), this.getCurrentUser().getHomeTerminalTimeZone().toDMOEnum(), TimeZone.getDefault().getDisplayName()));
		EmployeeLogPersist<EmployeeLog> persist = new EmployeeLogPersist<EmployeeLog>(EmployeeLog.class, this.getContext(), this.getCurrentUser(), logSourceStatusEnum);
        persist.Persist(empLogData);
	}
	
    /* (non-Javadoc)
	 * @see com.jjkeller.kmbapi.controller.dataaccess.IEmployeeLogFacade#GetLogByDate(java.util.Date)
	 */
    public EmployeeLog GetLogByDate(Date logDate)
    {
        EmployeeLogPersist<EmployeeLog> persist = new EmployeeLogPersist<EmployeeLog>(EmployeeLog.class, this.getContext(), this.getCurrentUser());
        EmployeeLog empLog = persist.FetchByLogDateAndStatus(logDate, 1); //LogSourceStatusEnum.LocalUnsubmitted);
        if(empLog == null)
        	empLog = persist.FetchByLogDateAndStatus(logDate, 3); //LogSourceStatusEnum.DMOServerLog);
        return empLog;
    }
    
    /* (non-Javadoc)
	 * @see com.jjkeller.kmbapi.controller.dataaccess.IEmployeeLogFacade#GetLogsByDate(java.util.Date)
	 */
    public List<EmployeeLog> GetLogsByDate(Date logDate)
    {
        EmployeeLogPersist<EmployeeLog> persist = new EmployeeLogPersist<EmployeeLog>(EmployeeLog.class, this.getContext(), this.getCurrentUser());
        return persist.FetchLogListByLogDate(logDate);
    }
    
    /* (non-Javadoc)
	 * @see com.jjkeller.kmbapi.controller.dataaccess.IEmployeeLogFacade#GetLogListByDateRange(java.util.Date, java.util.Date)
	 */
    public List<EmployeeLog> GetLogListByDateRange(Date startDate, Date endDate)
    {
    	EmployeeLogPersist<EmployeeLog> persist = new EmployeeLogPersist<EmployeeLog>(EmployeeLog.class, this.getContext(), this.getCurrentUser());
        return persist.FetchLogListByLogDateRange(startDate, endDate);
    }
    
    /* (non-Javadoc)
	 * @see com.jjkeller.kmbapi.controller.dataaccess.IEmployeeLogFacade#GetLocalLogByDate(java.util.Date)
	 */
    public EmployeeLog GetLocalLogByDate(Date logDate)
    {
        EmployeeLogPersist<EmployeeLog> persist = new EmployeeLogPersist<EmployeeLog>(EmployeeLog.class, this.getContext(), this.getCurrentUser());
        return persist.FetchByLogDateAndStatus(logDate, 1); //LogSourceStatusEnum.LocalUnsubmitted);
    }
    
    /* (non-Javadoc)
	 * @see com.jjkeller.kmbapi.controller.dataaccess.IEmployeeLogFacade#GetServerLogByDate(java.util.Date)
	 */
    public EmployeeLog GetServerLogByDate(Date logDate)
    {
    	EmployeeLogPersist<EmployeeLog> persist = new EmployeeLogPersist<EmployeeLog>(EmployeeLog.class, this.getContext());
        return persist.FetchByLogDateAndStatus(logDate, 3);//LogSourceStatusEnum.DMOServerLog);
    }

    /* (non-Javadoc)
	 * @see com.jjkeller.kmbapi.controller.dataaccess.IEmployeeLogFacade#GetEmployeeLogToCertify(java.util.Date)
	 */
    public EmployeeLog GetEmployeeLogToCertify(Date logDate)
    {
        EmployeeLogPersist<EmployeeLog> persist = new EmployeeLogPersist<EmployeeLog>(EmployeeLog.class, this.getContext(), this.getCurrentUser());
        return persist.FetchEmployeeLogToCertify(logDate);
    }

    /* (non-Javadoc)
	 * @see com.jjkeller.kmbapi.controller.dataaccess.IEmployeeLogFacade#DeleteLog(com.jjkeller.kmbapi.proxydata.EmployeeLog)
	 */
    public void DeleteLog(EmployeeLog empLog)
    {
    	EmployeeLogPersist<EmployeeLog> persist = new EmployeeLogPersist<EmployeeLog>(EmployeeLog.class, this.getContext(), this.getCurrentUser());
        persist.Delete(empLog);
    }

    /// <summary>
    /// Answer the most recent log event for the current user
    /// </summary>
    /* (non-Javadoc)
	 * @see com.jjkeller.kmbapi.controller.dataaccess.IEmployeeLogFacade#FetchMostRecentLogEventForUser()
	 */
    public EmployeeLogEldEvent FetchMostRecentLogEventForUser() {
        EmployeeLogEldEventPersist<EmployeeLogEldEvent> persist = new EmployeeLogEldEventPersist<EmployeeLogEldEvent>(EmployeeLogEldEvent.class, getContext());
        Date tomorrow = DateUtility.AddDays(TimeKeeper.getInstance().now(), 1);
        ArrayList<RuleSetTypeEnum> ruleSetTypeEnumList=new ArrayList<>();
        return persist.FetchMostRecentEldEventWithRulesetCriteria(getCurrentUser().getCredentials().getEmployeeId(),tomorrow,ruleSetTypeEnumList);
    }
    
    /* (non-Javadoc)
	 * @see com.jjkeller.kmbapi.controller.dataaccess.IEmployeeLogFacade#FetchMostRecentLogEventForUser(java.util.Date)
	 */
    public EmployeeLogEldEvent FetchMostRecentLogEventForUser(Date logDate) {
        EmployeeLogEldEventPersist<EmployeeLogEldEvent> persist = new EmployeeLogEldEventPersist<EmployeeLogEldEvent>(EmployeeLogEldEvent.class, getContext());
        ArrayList<RuleSetTypeEnum> ruleSetTypeEnumList = new ArrayList<>();
        return persist.FetchMostRecentEldEventWithRulesetCriteria(getCurrentUser().getCredentials().getEmployeeId(), logDate, ruleSetTypeEnumList);
    }
    
    /// <summary>
    /// Answer the most recent log event for the current user that contains
    /// an assignment of a US Federal ruleset (US60, US70)
    /// </summary>
    /// <returns></returns>
    /* (non-Javadoc)
	 * @see com.jjkeller.kmbapi.controller.dataaccess.IEmployeeLogFacade#FetchMostRecentUSLogEventForUser()
	 */
    public EmployeeLogEldEvent FetchMostRecentUSLogEventForUser()
    {
        Date tomorrow = DateUtility.AddDays(TimeKeeper.getInstance().now(), 1);
        EmployeeLogEldEventPersist<EmployeeLogEldEvent> persist = new EmployeeLogEldEventPersist<EmployeeLogEldEvent>(EmployeeLogEldEvent.class, getContext());
        ArrayList<RuleSetTypeEnum> ruleSetTypeEnumList = new ArrayList<>();
        ruleSetTypeEnumList.add(new RuleSetTypeEnum(RuleSetTypeEnum.ALASKA_7DAY));
        ruleSetTypeEnumList.add(new RuleSetTypeEnum(RuleSetTypeEnum.ALASKA_8DAY));
        ruleSetTypeEnumList.add(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR));
        ruleSetTypeEnumList.add(new RuleSetTypeEnum(RuleSetTypeEnum.US70HOUR));
        ruleSetTypeEnumList.add(new RuleSetTypeEnum(RuleSetTypeEnum.USMOTIONPICTURE_7DAY));
        ruleSetTypeEnumList.add(new RuleSetTypeEnum(RuleSetTypeEnum.USMOTIONPICTURE_8DAY));

        return persist.FetchMostRecentEldEventWithRulesetCriteria(this.getCurrentUser().getCredentials().getEmployeeId(), tomorrow, ruleSetTypeEnumList);
    }

    /// <summary>
    /// Answer the most recent log event for the current user that contains
    /// an assignment of a Canadian ruleset (C1, C2)
    /// </summary>
    /// <returns></returns>
    /* (non-Javadoc)
	 * @see com.jjkeller.kmbapi.controller.dataaccess.IEmployeeLogFacade#FetchMostRecentCanadianLogEventForUser()
	 */
    public EmployeeLogEldEvent FetchMostRecentCanadianLogEventForUser() {
        Date tomorrow = DateUtility.AddDays(TimeKeeper.getInstance().now(), 1);
        EmployeeLogEldEventPersist<EmployeeLogEldEvent> persist = new EmployeeLogEldEventPersist<EmployeeLogEldEvent>(EmployeeLogEldEvent.class, getContext());
        ArrayList<RuleSetTypeEnum> ruleSetTypeEnumList = new ArrayList<>();
        ruleSetTypeEnumList.add(new RuleSetTypeEnum(RuleSetTypeEnum.CANADIAN_CYCLE1));
        ruleSetTypeEnumList.add(new RuleSetTypeEnum(RuleSetTypeEnum.CANADIAN_CYCLE2));
        return persist.FetchMostRecentEldEventWithRulesetCriteria(this.getCurrentUser().getCredentials().getEmployeeId(), tomorrow, ruleSetTypeEnumList);
    }
    
    /// Fetch the count of all logs for the user that are either unsubmitted, 
    /// or server logs with a logDate between the start and end date (inclusive).
    /// If a log is both a server and unsubmitted log, it will only be counted
    /// once.
    /* (non-Javadoc)
	 * @see com.jjkeller.kmbapi.controller.dataaccess.IEmployeeLogFacade#FetchLogCount(java.util.Date, java.util.Date)
	 */
    public int FetchLogCount(Date startDate, Date endDate)
    {
    	EmployeeLogPersist<EmployeeLog> persist = new EmployeeLogPersist<EmployeeLog>(EmployeeLog.class, this.getContext(),this.getCurrentUser());
        List<Date> logList = persist.FetchLogList(startDate, endDate);
        if (logList != null && logList.size() > 0)
        	return logList.toArray().length;
        else
        	return 0;
    }

    /* (non-Javadoc)
	 * @see com.jjkeller.kmbapi.controller.dataaccess.IEmployeeLogFacade#FetchLogCountByStatus(int)
	 */
    public int FetchLogCountByStatus(int status)
    {
    	EmployeeLogPersist<EmployeeLog> persist = new EmployeeLogPersist<EmployeeLog>(EmployeeLog.class, this.getContext(), this.getCurrentUser());
        return persist.FetchLogCountByStatus(status);
    }
    
    /// Fetch the list of dates for all of the logs that the user has which are
    /// either unsubmitted or server logs.  The logDate list will be between start
    /// and end date (inclusive).  If a log is both a server and unsubmitted log, 
    /// it will only be counted once.
    /* (non-Javadoc)
	 * @see com.jjkeller.kmbapi.controller.dataaccess.IEmployeeLogFacade#FetchLogList(java.util.Date, java.util.Date)
	 */
    public List<Date> FetchLogList(Date startDate, Date endDate)
    {
        EmployeeLogPersist<EmployeeLog> persist = new EmployeeLogPersist<EmployeeLog>(EmployeeLog.class, this.getContext(),this.getCurrentUser());
        return persist.FetchLogList(startDate, endDate);
    }

    
    /* (non-Javadoc)
	 * @see com.jjkeller.kmbapi.controller.dataaccess.IEmployeeLogFacade#GetLocalLogList(boolean)
	 */
    public List<EmployeeLog> GetLocalLogList(boolean excludeTodaysLog)
    {
        // create the dynamic list of local logs
        List<EmployeeLog> empLogList = new ArrayList<EmployeeLog>();

        EmployeeLogPersist<EmployeeLog> persist = new EmployeeLogPersist<EmployeeLog>(EmployeeLog.class, this.getContext(), this.getCurrentUser());
        empLogList = persist.FetchLocalLogsForUser();

        if (excludeTodaysLog)
        {
            // determine today's date, in home terminal units (with time component removed)
            Date homeTerminalDate = DateUtility.CurrentHomeTerminalTime(this.getCurrentUser());
            Date todaysDate = new Date(homeTerminalDate.getYear(),homeTerminalDate.getMonth(), homeTerminalDate.getDate());

            // Check each log's date
            for (EmployeeLog empLog : empLogList)
            {
                if (todaysDate.compareTo(empLog.getLogDate()) == 0)
                {
                    // drop this log because the logDate matches today's date
                    empLogList.remove(empLog);
                    break;
                }
            }
        }

        return empLogList;
    }


    /* (non-Javadoc)
	 * @see com.jjkeller.kmbapi.controller.dataaccess.IEmployeeLogFacade#GetLocalLogListAllUsers(boolean)
	 */
    public List<EmployeeLog> GetLocalLogListAllUsers(boolean excludeTodaysLog)
    {
        // create the dynamic list of local logs
        List<EmployeeLog> empLogList = new ArrayList<EmployeeLog>();

        EmployeeLogPersist<EmployeeLog> persist = new EmployeeLogPersist<EmployeeLog>(EmployeeLog.class, this.getContext(), this.getCurrentUser());
        empLogList = persist.FetchAllLocalLogs();

        if (excludeTodaysLog)
        {
            // determine today's date, in home terminal units
            Date currentDate = TimeKeeper.getInstance().now();
            currentDate = DateUtility.CurrentHomeTerminalTime(this.getCurrentUser());
            Date todaysDate = new Date(currentDate.getYear(), currentDate.getMonth(), currentDate.getDate());

            // Check each log's date
            for (EmployeeLog empLog : empLogList)
            {
                if (todaysDate.compareTo(empLog.getLogDate()) == 0)
                {
                    // drop this log because the logDate matches today's date
                    empLogList.remove(empLog);
                    break;
                }
            }
        }

        return empLogList;
    }
    
    /* (non-Javadoc)
	 * @see com.jjkeller.kmbapi.controller.dataaccess.IEmployeeLogFacade#GetFirstAvailableLogDate()
	 */
    public Date GetFirstAvailableLogDate()
    {
    	EmployeeLogPersist<EmployeeLog> persist = new EmployeeLogPersist<EmployeeLog>(EmployeeLog.class, this.getContext());
    	return persist.GetFirstAvailableLogDate();
    }
    
    /* (non-Javadoc)
	 * @see com.jjkeller.kmbapi.controller.dataaccess.IEmployeeLogFacade#UpdateLogStatus(com.jjkeller.kmbapi.proxydata.EmployeeLog, int)
	 */
    public void UpdateLogStatus(EmployeeLog log, int newStatus)
    {
    	EmployeeLogPersist<EmployeeLog> persist = new EmployeeLogPersist<EmployeeLog>(EmployeeLog.class, this.getContext());
        persist.UpdateLogStatus(log, newStatus);
    }
    
    /// <summary>
    /// Answer a list of all logEvents, across all logs, that require 
    /// reverse geocoding
    /// </summary>
    /// <returns></returns>
    /* (non-Javadoc)
	 * @see com.jjkeller.kmbapi.controller.dataaccess.IEmployeeLogFacade#FetchUnsubmittedLocationsThatRequireGeocoding()
	 */
    public List<EmployeeLogEldEvent> FetchUnsubmittedLocationsThatRequireGeocoding()
    {
        EmployeeLogEldEventPersist<EmployeeLogEldEvent> persist = new EmployeeLogEldEventPersist<EmployeeLogEldEvent>(EmployeeLogEldEvent.class,getContext());
        List<EmployeeLogEldEvent> queriedList = persist.FetchUnsubmittedLocationsThatRequireGeocoding();
        return  queriedList;
    }
    
    /// <summary>
    /// Update the geocoded location info for the LogEvents
    /// </summary>
    /// <param name="logEventList"></param>
    /* (non-Javadoc)
	 * @see com.jjkeller.kmbapi.controller.dataaccess.IEmployeeLogFacade#UpdateGeocodedLocations(java.util.List)
	 */
    public void UpdateGeocodedLocations(List<EmployeeLogEldEvent> logEventList)
    {
        EmployeeLogEldEventPersist<EmployeeLogEldEvent> persist = new EmployeeLogEldEventPersist<EmployeeLogEldEvent>(EmployeeLogEldEvent.class,getContext());
        for(EmployeeLogEldEvent logEvt : logEventList) {
            if (logEvt.getLocation() != null && !logEvt.getLocation().IsEmpty() && !logEvt.getLocation().getGpsInfo().IsEmpty())
                persist.UpdateEmployeeLogEldEvent(logEvt);
        }
    }

    /**
     * Updates just the end odometer of the log event
     * @param logEvent The log event to update
     */
    @Override
    public void UpdateEndOdometer(EmployeeLogEldEvent logEvent)
    {
        if (logEvent != null && logEvent.getLocation() != null)
        {
            EmployeeLogEldEventPersist<EmployeeLogEldEvent> persist = new EmployeeLogEldEventPersist<>(EmployeeLogEldEvent.class,getContext());
            persist.UpdateEmployeeLogEldEvent(logEvent);
        }
    }

    /* (non-Javadoc)
         * @see com.jjkeller.kmbapi.controller.dataaccess.IEmployeeLogFacade#PurgeOldRecords(java.util.Date)
         */
    public void PurgeOldRecords(Date cutoffDate)
    {
    	EmployeeLogPersist<EmployeeLog> persist = new EmployeeLogPersist<EmployeeLog>(EmployeeLog.class, this.getContext(), this.getCurrentUser());
    	persist.PurgeOldRecords(cutoffDate);
    }
    
    /* (non-Javadoc)
	 * @see com.jjkeller.kmbapi.controller.dataaccess.IEmployeeLogFacade#FetchWeeklyReset(java.util.Date)
	 */
    public EmployeeLog FetchWeeklyReset(Date logDate)
    {
    	EmployeeLogPersist<EmployeeLog> persist = new EmployeeLogPersist<EmployeeLog>(EmployeeLog.class, this.getContext(), this.getCurrentUser());
    	
    	Date middleLogDate = DateUtility.AddDays(logDate, 1);
    	Date endingLogDate = DateUtility.AddDays(logDate, 2);
    	
    	return persist.FetchWeeklyReset(logDate, middleLogDate, endingLogDate);
    }
    
    /* (non-Javadoc)
	 * @see com.jjkeller.kmbapi.controller.dataaccess.IEmployeeLogFacade#FetchPreviousWeeklyResetStartTimestamp(java.util.Date)
	 */
    public Date FetchPreviousWeeklyResetStartTimestamp(Date logDate)
    {
    	EmployeeLogPersist<EmployeeLog> persist = new EmployeeLogPersist<EmployeeLog>(EmployeeLog.class, this.getContext(), this.getCurrentUser());
    	return persist.FetchPreviousWeeklyResetStartTimestamp(logDate);
    }
    
    /* (non-Javadoc)
	 * @see com.jjkeller.kmbapi.controller.dataaccess.IEmployeeLogFacade#GetLogsByWeeklyResetStartTimestamp(java.util.Date)
	 */
    public List<EmployeeLog> GetLogsByWeeklyResetStartTimestamp(Date weeklyResetStartTimestamp)
    {
    	EmployeeLogPersist<EmployeeLog> persist = new EmployeeLogPersist<EmployeeLog>(EmployeeLog.class, this.getContext(), this.getCurrentUser());
    	return persist.GetLogsByWeeklyResetStartTimestamp(weeklyResetStartTimestamp);
    }
    
    /* (non-Javadoc)
	 * @see com.jjkeller.kmbapi.controller.dataaccess.IEmployeeLogFacade#DoesWeeklyResetExistNewerThan(java.util.Date)
	 */
    public boolean DoesWeeklyResetExistNewerThan(Date aDate)
    {
    	boolean answer = false;
    	
    	EmployeeLogPersist<EmployeeLog> persist = new EmployeeLogPersist<EmployeeLog>(EmployeeLog.class, this.getContext(), this.getCurrentUser());
    	Date mostRecentResetTimestamp = persist.GetMostRecentWeeklyResetStartTimestamp();
    	if(mostRecentResetTimestamp != null && aDate != null)
    		answer = aDate.compareTo(mostRecentResetTimestamp) < 0;
    	
    	return answer;
    }   
    
    /* (non-Javadoc)
	 * @see com.jjkeller.kmbapi.controller.dataaccess.IEmployeeLogFacade#ExistAnyLogEarlierThan(java.util.Date)
	 */
    public boolean ExistAnyLogEarlierThan(Date logDate)
    {
    	boolean answer = false;
    	
        EmployeeLogPersist<EmployeeLog> persist = new EmployeeLogPersist<EmployeeLog>(EmployeeLog.class, this.getContext(), this.getCurrentUser());
        answer = persist.ExistAnyLogEarlierThan(logDate);
        
        return answer;
    }

	/* (non-Javadoc)
	 * @see com.jjkeller.kmbapi.controller.dataaccess.IEmployeeLogFacade#FetchUncertifiedLogDates()
	 */
	public List<Date> FetchUncertifiedLogDates()
	{
    	EmployeeLogPersist<EmployeeLog> persist = new EmployeeLogPersist<EmployeeLog>(EmployeeLog.class, this.getContext(), this.getCurrentUser());
		return persist.FetchUncertifiedLogDates();
	}

    public List<Date> FetchCertifiedUnsubmittedLogDates()
    {
        EmployeeLogPersist<EmployeeLog> persist = new EmployeeLogPersist<EmployeeLog>(EmployeeLog.class, this.getContext(), this.getCurrentUser());
        return persist.FetchCertifiedUnsubmittedLogDates();
    }

    public List<EmployeeLog> FetchLogsWithUnreviewedEdits()
    {
        EmployeeLogPersist<EmployeeLog> persist = new EmployeeLogPersist<EmployeeLog>(EmployeeLog.class, this.getContext(), this.getCurrentUser());
        return persist.FetchLogsWithUnreviewedEdits();
    }

    public EmployeeLog FetchByKey(Integer uniqueKey)
    {
        EmployeeLogPersist<EmployeeLog> persist = new EmployeeLogPersist<EmployeeLog>(EmployeeLog.class, getContext());
        return persist.FetchByKey(uniqueKey);
    }

}
