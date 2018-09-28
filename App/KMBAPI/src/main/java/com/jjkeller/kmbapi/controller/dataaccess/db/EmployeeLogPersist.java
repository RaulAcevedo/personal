package com.jjkeller.kmbapi.controller.dataaccess.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.jjkeller.kmbapi.controller.dataaccess.AbstractDBAdapter;
import com.jjkeller.kmbapi.controller.dataaccess.ApplicationStateFacade;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.eldmandate.EventSequenceIdGenerator;
import com.jjkeller.kmbapi.enums.CanadaDeferralTypeEnum;
import com.jjkeller.kmbapi.enums.DriverTypeEnum;
import com.jjkeller.kmbapi.enums.ExemptLogTypeEnum;
import com.jjkeller.kmbapi.enums.FailureCategoryEnum;
import com.jjkeller.kmbapi.enums.RuleSetTypeEnum;
import com.jjkeller.kmbapi.enums.TimeZoneEnum;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;
import com.jjkeller.kmbapi.proxydata.FailureReport;
import com.jjkeller.kmbapi.proxydata.TeamDriver;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class EmployeeLogPersist<T extends EmployeeLog> extends AbstractDBAdapter<T> {

    ///////////////////////////////////////////////////////////////////////////////////////
    // private members
    ///////////////////////////////////////////////////////////////////////////////////////
    private static final String USERKEY = "UserKey";
    private static final String LOGDATE = "LogDate";
    private static final String TOTALLOGDISTANCE = "TotalLogDistance";
    private static final String HASRETURNEDTOLOCATION = "HasReturnedToLocation";
    private static final String DRIVERTYPE = "DriverType";
    private static final String RULESET = "RuleSet";
    private static final String TIMEZONE = "Timezone";
    private static final String MOBILESTARTTIMESTAMP = "MobileStartTimestamp";
    private static final String MOBILEENDTIMESTAMP = "MobileEndTimestamp";
    private static final String MOBILERECORDEDDISTANCE = "MobileRecordedDistance";
    private static final String MOBILEEOBRIDENTIFIER = "MobileEOBRIdentifier";
    private static final String TRACTORNUMBERS = "TractorNumbers";
    private static final String TRAILERNUMBERS = "TrailerNumbers";
    private static final String TRAILERPLATE = "TrailerPlate";
    private static final String SHIPMENTINFO = "ShipmentInfo";
    private static final String VEHICLEPLATE = "VehiclePlate";
    private static final String LOGSOURCESTATUSENUM = "LogSourceStatusEnum";
    private static final String ISSHORTHAULEXCEPTIONUSED = "IsShortHaulExceptionUsed";
    private static final String CANADADEFERRALTYPE = "CanadaDeferralType";
    private static final String WEEKLYRESETSTARTTIMESTAMP = "WeeklyResetStartTimestamp";
    private static final String EMPLOYEEID = "EmployeeId";
    private static final String ISHAULINGEXPLOSIVES = "IsHaulingExplosives";
    private static final String ISEXEMPTFROM30MINBREAKREQUIREMENT = "IsExemptFrom30MinBreakRequirement";
    private static final String ISWEEKLYRESETUSED = "IsWeeklyResetUsed";
    private static final String ISWEEKLYRESETUSEDOVERRIDDEN = "IsWeeklyResetUsedOverridden";
    private static final String ISOPERATESSPECIFICVEHICLESFOROILFIELD = "IsOperatesSpecificVehiclesForOilField";
    private static final String EXEMPTLOGTYPE = "ExemptLogType";
    private static final String ISNONCDLSHORTHAULEXCEPTIONUSED = "IsNonCDLShortHaulExceptionUsed";
    private static final String ISCERTIFIED = "IsCertified";
    private static final String IS_EXEMPT_FROM_ELD_USE = "IsExemptFromELDUse";
    private static final String LOG_TYPE = "LogType";

    private int _logSourceStatusEnum;

    private static final String SQL_SELECT_PRIMARYKEY_COMMAND = "select [Key] from EmployeeLog where UserKey=? and LogDate=? AND LogSourceStatusEnum=?";

    private static final String SQL_SELECT_LOGLISTBETWEENDATES_COMMAND = "select DISTINCT LogDate from EmployeeLog where UserKey=? AND LogSourceStatusEnum in (1,3) AND LogDate BETWEEN ? AND ? ORDER BY LogDate DESC";

    private static final String SQL_SELECT_LOGLIST_COMMAND = "select DISTINCT LogDate from EmployeeLog where UserKey=? AND LogSourceStatusEnum in (1,3) ORDER BY LogDate DESC";

    private static String SQL_SELECT_LOCALLOGS_COMMAND = "select EmployeeLog.*, [User].EmployeeId from EmployeeLog LEFT JOIN [User] ON EmployeeLog.UserKey = [User].[Key] where LogSourceStatusEnum=1 order by EmployeeLog.LogDate";

    private static String SQL_SELECT_LOCALLOGS_BYUSER_COMMAND = "select EmployeeLog.*, [User].EmployeeId from EmployeeLog LEFT JOIN [User] ON EmployeeLog.UserKey = [User].[Key] where UserKey=? AND LogSourceStatusEnum=1 order by EmployeeLog.LogDate";

    private static final String SQL_SELECT_BYDATESTATUS_COMMAND = "select EmployeeLog.*, [User].EmployeeId from EmployeeLog LEFT JOIN [User] ON EmployeeLog.UserKey = [User].[Key] where UserKey=? AND LogDate=? AND LogSourceStatusEnum=?";
    private static final String SQL_SELECT_BYDATE_COMMAND = "select EmployeeLog.*, [User].EmployeeId from EmployeeLog LEFT JOIN [User] ON EmployeeLog.UserKey = [User].[Key] where UserKey=? AND LogDate=?";
    private static final String SQL_SELECT_BYDATERANGE_COMMAND = "select EmployeeLog.*, [User].EmployeeId from EmployeeLog LEFT JOIN [User] ON EmployeeLog.UserKey = [User].[Key] where UserKey=? AND LogDate>=? AND LogDate<=?";

    private static final String SQL_SELECT_LOGCOUNTBYSTATUS_COMMAND = "select count(1) from EmployeeLog where LogSourceStatusEnum = ?";

    private static final String SQL_SELECT_LOGCOUNT_PRIORTO_COMMAND = "select count(1) from EmployeeLog where LogSourceStatusEnum in (1,3) AND LogDate < ? ";

    private static final String SQL_SELECTTOPURGE_COMMAND = " select distinct key " +
            "from " +
            " (select lg.key, lg.isCertified , lg.logDate, logsourcestatusenum, sum(case when eve.EventRecordStatus = 3 then 1 else 0 end) as totalPendingEdits " +
            " from employeelog lg left join employeelogeldevent eve on eve.LogKey = lg.key  " +
            " group by lg.key, lg.iscertified, logsourcestatusenum, lg.logDate " +
            " ) sub " +
            " where ((sub.isCertified = 1 and sub.totalPendingEdits = 0) or logsourcestatusenum <> 1) and logdate < ? ";


    private static final String SQL_PURGE_COMMAND = "delete from EmployeeLog where Key = ?";
    private static final String SQL_PURGE_EMPLOYEELOGELDEVENT_COMMAND = "delete from EmployeeLogEldEvent where LogKey = ? ";
    private static final String SQL_PURGE_LOGFAILUREREPORT_COMMAND = "delete from LogFailureReport where EmployeeLogKey < ? ";
    private static final String SQL_PURGE_LOGTEAMDRIVER_COMMAND = "delete from LogTeamDriver where EmployeeLogKey < ? ";

    private static final String SQL_SELECT_WEEKLYRESET_COMMAND = "select * from EmployeeLog where UserKey = ? AND LogSourceStatusEnum in (1,3) AND WeeklyResetStartTimestamp = (select MAX(el.[WeeklyResetStartTimestamp]) from EmployeeLog el where el.UserKey = ? AND el.LogDate BETWEEN ? AND ? and el.[WeeklyResetStartTimestamp] < ? AND LogSourceStatusEnum in (1,3))";
    private static final String SQL_SELECT_LOGSBYWEEKLYRESET_COMMAND = "select * from EmployeeLog where UserKey = ? AND WeeklyResetStartTimestamp = ?";
    private static final String SQL_SELECT_PREVIOUSWEEKLYRESET_COMMAND = "select WeeklyResetStartTimestamp from EmployeeLog where UserKey = ? AND LogSourceStatusEnum in (1,3) AND LogDate < ? AND WeeklyResetStartTimestamp IS NOT NULL AND IsWeeklyResetUsed=1";
    private static final String SQL_SELECT_MOSTRECENTWEEKLYRESET_COMMAND = "select MAX(WeeklyResetStartTimestamp) from EmployeeLog where UserKey = ? AND LogSourceStatusEnum in (1,3)";
    private static final String SQL_SELECT_EMPLOYEELOG_FIRSTLOGDATE = "select LogDate from EmployeeLog where UserKey=? AND LogSourceStatusEnum in (1,3) ORDER BY LogDate LIMIT 1";

    private static final String SQL_SELECT_EMPLOYEELOG_TOCERTIFY_COMMAND = "select * from (select * from EmployeeLog L where UserKey=? AND LogDate>=? AND LogDate<=? order by L.LogDate, L.LogSourceStatusEnum=1, L.Key) group by LogDate";
    private static final String SQL_SELECT_UNCERTIFIED_LOGS_COMMAND = "select * from (select * from (select L.LogDate, L.IsCertified from EmployeeLog L where UserKey=? order by L.LogDate, L.LogSourceStatusEnum=1, L.Key) group by LogDate) where IsCertified=0 order by LogDate DESC";
    private static final String SQL_SELECT_UNCERTIFIED_NOT_EXEMPT_LOGS_COMMAND = "select * from (select * from (select L.LogDate, L.IsCertified, L.IsExemptFromELDUse from EmployeeLog L where UserKey=? order by L.LogDate, L.LogSourceStatusEnum=1, L.Key) group by LogDate) where IsCertified=0 and isExemptFromELDUse=0 order by LogDate DESC";
    private static final String SQL_SELECT_CERTIFIED_UNSUBMITTED_NOT_EXEMPT_LOGS_COMMAND = "select * from (select * from (select L.LogDate, L.IsCertified, L.LogSourceStatusEnum, L.IsExemptFromELDUse from EmployeeLog L where UserKey=? order by L.LogDate, L.LogSourceStatusEnum=1, L.Key) group by LogDate) where IsCertified=1 and isExemptFromELDUse=0 and logSourceStatusEnum=1 order by LogDate DESC";
    private static final String SQL_SELECT_LOGS_UNREVIEWED_EDITS = "select * from EmployeeLog where key in (select LogKey from EmployeeLogEldEvent where EventRecordStatus=3 and LogKey IN (SELECT MAX(key) FROM EmployeeLog where UserKey=? group by LogDate))";

    private static final String SQL_SELECT_KEY_COMMAND = "select * from EmployeeLog where Key=?";

    private final EventSequenceIdGenerator eventSequenceIdGenerator;
    ///////////////////////////////////////////////////////////////////////////////////////
    // constructors
    ///////////////////////////////////////////////////////////////////////////////////////
    public EmployeeLogPersist(Class<T> clazz, Context ctx) {
        super(clazz, ctx);

        _logSourceStatusEnum = 1; //LogSourceStatusEnum.LocalUnsubmitted;
        eventSequenceIdGenerator = new EventSequenceIdGenerator(new ApplicationStateFacade(this.getContext()));

        setDbTableName(DB_TABLE_EMPLOYEELOG);
    }

    public EmployeeLogPersist(Class<T> clazz, Context ctx, int logSourceStatusEnum) {
        super(clazz, ctx);

        _logSourceStatusEnum = logSourceStatusEnum;
        eventSequenceIdGenerator = new EventSequenceIdGenerator(new ApplicationStateFacade(this.getContext()));

        setDbTableName(DB_TABLE_EMPLOYEELOG);
    }

    public EmployeeLogPersist(Class<T> clazz, Context ctx, User user) {
        super(clazz, ctx, user);
        setDbTableName(DB_TABLE_EMPLOYEELOG);
        eventSequenceIdGenerator = new EventSequenceIdGenerator(new ApplicationStateFacade(this.getContext()));
    }

    public EmployeeLogPersist(Class<T> clazz, Context ctx, User user, int logSourceStatusEnum) {
        super(clazz, ctx, user);

        _logSourceStatusEnum = logSourceStatusEnum;
        eventSequenceIdGenerator = new EventSequenceIdGenerator(new ApplicationStateFacade(this.getContext()));

        setDbTableName(DB_TABLE_EMPLOYEELOG);
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // @Override methods
    ///////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected String[] getSelectPrimaryKeyArgs(T data) {
        String[] args;
        if (data.getLogDate() == null) {
            args = new String[]{Long.toString(this.getCurrentUser().getCredentials().getPrimaryKey()), "", Integer.toString(_logSourceStatusEnum)};
        } else {
            args = new String[]{Long.toString(this.getCurrentUser().getCredentials().getPrimaryKey()), DateUtility.getHomeTerminalSqlDateFormat(this.getCurrentUser()).format(data.getLogDate()), Integer.toString(_logSourceStatusEnum)};
        }

        return args;
    }

    @Override
    public String getSelectPrimaryKeyCommand() {
        return SQL_SELECT_PRIMARYKEY_COMMAND;
    }

    @Override
    protected T BuildObject(Cursor cursorData) {
        T data = super.BuildObject(cursorData);

        data.setLogDate(ReadValue(cursorData, LOGDATE, (Date) null, DateUtility.getHomeTerminalSqlDateFormat(this.getCurrentUser())));
        data.setTotalLogDistance(ReadValue(cursorData, TOTALLOGDISTANCE, (float) 0));
        data.setHasReturnedToLocation(ReadValue(cursorData, HASRETURNEDTOLOCATION, false));
        data.getDriverType().setValue(ReadValue(cursorData, DRIVERTYPE, DriverTypeEnum.NULL));
        data.getRuleset().setValue(ReadValue(cursorData, RULESET, RuleSetTypeEnum.NULL));
        data.getTimezone().setValue(ReadValue(cursorData, TIMEZONE, TimeZoneEnum.NULL));
        data.setMobileStartTimestamp(ReadValue(cursorData, MOBILESTARTTIMESTAMP, (Date) null, DateUtility.getHomeTerminalSqlDateTimeFormat(this.getCurrentUser())));
        data.setMobileEndTimestamp(ReadValue(cursorData, MOBILEENDTIMESTAMP, (Date) null, DateUtility.getHomeTerminalSqlDateTimeFormat(this.getCurrentUser())));
        data.setMobileRecordedDistance(ReadValue(cursorData, MOBILERECORDEDDISTANCE, (float) 0));
        data.setMobileEobrIdentifier(ReadValue(cursorData, MOBILEEOBRIDENTIFIER, (String) null));
        data.setTractorNumbers(ReadValue(cursorData, TRACTORNUMBERS, (String) null));
        data.setTrailerNumbers(ReadValue(cursorData, TRAILERNUMBERS, (String) null));
        data.setTrailerPlate(ReadValue(cursorData, TRAILERPLATE, (String) null));
        data.setShipmentInformation(ReadValue(cursorData, SHIPMENTINFO, (String) null));
        data.setVehiclePlate(ReadValue(cursorData, VEHICLEPLATE, (String) null));
        data.setIsShortHaulExceptionUsed(ReadValue(cursorData, ISSHORTHAULEXCEPTIONUSED, false));
        data.getCanadaDeferralType().setValue(ReadValue(cursorData, CANADADEFERRALTYPE, CanadaDeferralTypeEnum.NONE));
        data.setWeeklyResetStartTimestamp(ReadValue(cursorData, WEEKLYRESETSTARTTIMESTAMP, (Date) null, DateUtility.getHomeTerminalSqlDateTimeFormat(this.getCurrentUser())));
        data.setIsHaulingExplosives(ReadValue(cursorData, ISHAULINGEXPLOSIVES, false));
        data.setIsExemptFrom30MinBreakRequirement(ReadValue(cursorData, ISEXEMPTFROM30MINBREAKREQUIREMENT, false));
        data.setIsWeeklyResetUsed(ReadValue(cursorData, ISWEEKLYRESETUSED, false));
        data.setIsWeeklyResetUsedOverridden(ReadValue(cursorData, ISWEEKLYRESETUSEDOVERRIDDEN, false));
        data.setIsOperatesSpecificVehiclesForOilfield(ReadValue(cursorData, ISOPERATESSPECIFICVEHICLESFOROILFIELD, false));
        data.getExemptLogType().setValue(ReadValue(cursorData, EXEMPTLOGTYPE, ExemptLogTypeEnum.NULL));
        data.setIsNonCDLShortHaulExceptionUsed(ReadValue(cursorData, ISNONCDLSHORTHAULEXCEPTIONUSED, false));
        data.setIsCertified(ReadValue(cursorData, ISCERTIFIED, false));
        data.setIsExemptFromELDUse(ReadValue(cursorData, IS_EXEMPT_FROM_ELD_USE, false));

        // 3/22/11 JHM - Set the EmployeeId if it exists.  Used for submitting logs to DMO.
        data.setEmployeeId(ReadValue(cursorData, EMPLOYEEID, (String) null));

        return data;
    }

    @Override
    public void Persist(T data) {
        synchronized (EmployeeLogPersist.class) {
            super.Persist(data);
        }
    }

    @Override
    protected ContentValues PersistContentValues(T data) {
        ContentValues content = super.PersistContentValues(data);

        long userKey = this.getCurrentUser().getCredentials().getPrimaryKey();

        // 7/8/11 JHM - Modified for Team Driver login (UserKey has changed and was updating log incorrectly)
        if (!data.isPrimaryKeySet()) {
            PutValue(content, USERKEY, userKey);
        }

        PutValue(content, LOGDATE, data.getLogDate(), DateUtility.getHomeTerminalSqlDateFormat(this.getCurrentUser()));
        PutValue(content, TOTALLOGDISTANCE, data.getTotalLogDistance());
        PutValue(content, HASRETURNEDTOLOCATION, data.getHasReturnedToLocation());
        PutValue(content, DRIVERTYPE, data.getDriverType().getValue());
        PutValue(content, RULESET, data.getRuleset().getValue());
        PutValue(content, TIMEZONE, data.getTimezone().getValue());
        PutValue(content, MOBILESTARTTIMESTAMP, data.getMobileStartTimestamp(), DateUtility.getHomeTerminalSqlDateTimeFormat(this.getCurrentUser()));
        PutValue(content, MOBILEENDTIMESTAMP, data.getMobileEndTimestamp(), DateUtility.getHomeTerminalSqlDateTimeFormat(this.getCurrentUser()));
        PutValue(content, MOBILERECORDEDDISTANCE, data.getMobileRecordedDistance());
        PutValue(content, MOBILEEOBRIDENTIFIER, data.getMobileEobrIdentifier());
        PutValue(content, TRACTORNUMBERS, data.getTractorNumbers());
        PutValue(content, TRAILERNUMBERS, data.getTrailerNumbers());
        PutValue(content, TRAILERPLATE, data.getTrailerPlate());
        PutValue(content, SHIPMENTINFO, data.getShipmentInformation());
        PutValue(content, VEHICLEPLATE, data.getVehiclePlate());
        PutValue(content, LOGSOURCESTATUSENUM, this._logSourceStatusEnum);
        PutValue(content, ISSHORTHAULEXCEPTIONUSED, data.getIsShortHaulExceptionUsed());
        PutValue(content, CANADADEFERRALTYPE, data.getCanadaDeferralType().getValue());
        PutValue(content, WEEKLYRESETSTARTTIMESTAMP, data.getWeeklyResetStartTimestamp(), DateUtility.getHomeTerminalSqlDateTimeFormat(this.getCurrentUser()));
        PutValue(content, ISHAULINGEXPLOSIVES, data.getIsHaulingExplosives());
        PutValue(content, ISEXEMPTFROM30MINBREAKREQUIREMENT, data.getIsExemptFrom30MinBreakRequirement());
        PutValue(content, ISWEEKLYRESETUSED, data.getIsWeeklyResetUsed());
        PutValue(content, ISWEEKLYRESETUSEDOVERRIDDEN, data.getIsWeeklyResetUsedOverridden());
        PutValue(content, ISOPERATESSPECIFICVEHICLESFOROILFIELD, data.getIsOperatesSpecificVehiclesForOilfield());
        PutValue(content, EXEMPTLOGTYPE, data.getExemptLogType().getValue());
        PutValue(content, ISNONCDLSHORTHAULEXCEPTIONUSED, data.getIsNonCDLShortHaulExceptionUsed());
        PutValue(content, ISCERTIFIED, data.getIsCertified());
        PutValue(content, IS_EXEMPT_FROM_ELD_USE, data.getIsExemptFromELDUse());

        return content;
    }

    @Override
    protected void GetAdditionalData(T log) {
        super.GetAdditionalData(log);
        if (log != null) {
            // Get eld events for the log.
            EmployeeLogEldEventPersist<EmployeeLogEldEvent> eldEventPersist = new EmployeeLogEldEventPersist(EmployeeLogEldEvent.class, this.getContext(), this.getCurrentUser(), log.getPrimaryKey());
            List<EmployeeLogEldEvent> eldEventList = eldEventPersist.FetchList();
            if (eldEventList != null) {
                EmployeeLogEldEvent[] eldEventArray = eldEventList.toArray(new EmployeeLogEldEvent[eldEventList.size()]);
                log.getEldEventList().setEldEventList(eldEventArray);
            }

            // Get team drivers for the log.
            LogTeamDriverPersist<TeamDriver> teamDriverPersist = new LogTeamDriverPersist<TeamDriver>(TeamDriver.class, this.getContext(), this.getCurrentUser(), log.getPrimaryKey());
            List<TeamDriver> teamDriverList = teamDriverPersist.FetchList();
            if (teamDriverList != null) {
                TeamDriver[] teamArray = teamDriverList.toArray(new TeamDriver[teamDriverList.size()]);
                log.getTeamDriverList().setTeamDriverList(teamArray);
            }

            // Get eobr failures for the log.
            LogFailureReportPersist<FailureReport> eobrFailurePersist = new LogFailureReportPersist<FailureReport>(FailureReport.class, this.getContext(), this.getCurrentUser(), log.getPrimaryKey(), new FailureCategoryEnum(FailureCategoryEnum.EOBRDEVICE));
            List<FailureReport> eobrFailureList = eobrFailurePersist.FetchList();

            if (eobrFailureList != null) {
                FailureReport[] eobrFailureArray = eobrFailureList.toArray(new FailureReport[eobrFailureList.size()]);
                log.getEobrFailureList().setFailureReportList(eobrFailureArray);
            }

            // Get timesync failures for the log
            LogFailureReportPersist<FailureReport> timeSyncFailurePersist = new LogFailureReportPersist<FailureReport>(FailureReport.class, this.getContext(), this.getCurrentUser(), log.getPrimaryKey(), new FailureCategoryEnum(FailureCategoryEnum.CLOCKSYNCHRONIZATION));
            List<FailureReport> timesyncFailureList = timeSyncFailurePersist.FetchList();
            if (timesyncFailureList != null) {
                FailureReport[] timesyncFailureArray = timesyncFailureList.toArray(new FailureReport[timesyncFailureList.size()]);
                log.getTimeSyncFailureList().setFailureReportList(timesyncFailureArray);
            }
        }
    }

    @Override
    protected void SaveRelatedData(T empLogData) {
        super.SaveRelatedData(empLogData);

        long logKey = empLogData.getPrimaryKey();

        if (!empLogData.getTeamDriverList().IsEmpty()) {
            LogTeamDriverPersist<TeamDriver> logTeamDriverPersist = new LogTeamDriverPersist<TeamDriver>(TeamDriver.class, this.getContext(), this.getCurrentUser(), logKey);
            logTeamDriverPersist.Persist(empLogData.getTeamDriverList());
        }

        if (!empLogData.getEobrFailureList().IsEmpty()) {
            LogFailureReportPersist<FailureReport> eobrFailurePersist = new LogFailureReportPersist<FailureReport>(FailureReport.class, this.getContext(), this.getCurrentUser(), logKey, new FailureCategoryEnum(FailureCategoryEnum.EOBRDEVICE));
            eobrFailurePersist.Persist(empLogData.getEobrFailureList());
        }

        if (!empLogData.getTimeSyncFailureList().IsEmpty()) {
            LogFailureReportPersist<FailureReport> timeSyncPersist = new LogFailureReportPersist<FailureReport>(FailureReport.class, this.getContext(), this.getCurrentUser(), logKey, new FailureCategoryEnum(FailureCategoryEnum.CLOCKSYNCHRONIZATION));
            timeSyncPersist.Persist(empLogData.getTimeSyncFailureList());
        }

        if (empLogData.getEldEventList() != null && empLogData.getEldEventList().getEldEventList() != null) {
            EmployeeLogEldEventPersist<EmployeeLogEldEvent> eldEventSyncPersist = new EmployeeLogEldEventPersist<>(EmployeeLogEldEvent.class, this.getContext(), this.getCurrentUser(), logKey);

            /**
             * Persist list MUST be in the same order as FetchList [SQL_SELECT_COMMAND] because
             * AbstractDBAdapter compares inboundList against dbRecordList or PrimaryKey's will be swapped.
             *
             * empLogData.getEldEventList().getEldEventList() goes through GenericEventComparer
             * which returns order based on rules that are not easy to replicate in SQL.
             *
             * Currently, SQL_SELECT_COMMAND sorts by "EventDateTime, EventRecordStatus, EventSequenceIdNumber". So we
             * need to re-sort the list to return in the same order as the SQL_SELECT_COMMAND
             */
            // Events ordered by EventDateTime, EventType, EventCode, EventRecordStatus, EventSequenceIdNumber (in that order)
            EmployeeLogEldEvent[] eventList = empLogData.getEldEventList().getEldEventList();
            Arrays.sort(eventList, new Comparator<EmployeeLogEldEvent>() {

                public int compare(EmployeeLogEldEvent event1, EmployeeLogEldEvent event2) {
                    int result = event1.getEventDateTime().compareTo(event2.getEventDateTime());
                    if (result == 0) {
                        result = event1.getEventType().getValue() < event2.getEventType().getValue() ? -1 : 1;
                        if (result == 0) {
                            result = event1.getEventCode() < event2.getEventCode() ? -1 : 1;
                            if (result == 0) {
                                result = event1.getEventRecordStatus().compareTo(event2.getEventRecordStatus());
                                if (result == 0) {
                                    // same EventDateTime and EventRecordStatus, now check EventSequenceIdNumber
                                    result = event1.getEventSequenceIDNumber() < event2.getEventSequenceIDNumber() ? -1 : 1;
                                }
                            }
                        }
                    }
                    return result;
                }
            });

            eldEventSyncPersist.Persist(eventList);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // custom methods
    ///////////////////////////////////////////////////////////////////////////////////////
    public void UpdateLogStatus(T data, int newStatusEnum) {
        ContentValues content = new ContentValues();
        PutValue(content, LOGSOURCESTATUSENUM, newStatusEnum);
        String errorMsg = "Can't set log status without PK.";

        ExecuteUpdate(data, content, errorMsg);
    }

    public List<T> FetchAllLocalLogs() {
        String sql = SQL_SELECT_LOCALLOGS_COMMAND;
        String[] selectionArgs = new String[0];

        List<T> empLogList = ExecuteFetchListRawQuery(sql, selectionArgs);

        return empLogList;
    }

    public List<T> FetchLocalLogsForUser() {
        String sql = SQL_SELECT_LOCALLOGS_BYUSER_COMMAND;
        String[] selectionArgs = new String[]{String.valueOf(this.getCurrentUser().getCredentials().getPrimaryKey())};

        List<T> empLogList = ExecuteFetchListRawQuery(sql, selectionArgs);

        return empLogList;
    }

    public List<T> FetchServerLogsForUser() {
        String[] columns = new String[]{KEY, USERKEY, LOGDATE, TOTALLOGDISTANCE, HASRETURNEDTOLOCATION, DRIVERTYPE, RULESET, TIMEZONE, MOBILESTARTTIMESTAMP, MOBILEENDTIMESTAMP, MOBILERECORDEDDISTANCE, MOBILEEOBRIDENTIFIER, TRACTORNUMBERS, TRAILERNUMBERS, TRAILERPLATE, SHIPMENTINFO, VEHICLEPLATE, LOGSOURCESTATUSENUM, ISSHORTHAULEXCEPTIONUSED, CANADADEFERRALTYPE, EXEMPTLOGTYPE, ISNONCDLSHORTHAULEXCEPTIONUSED};
        String selection = "LogSourceStatusEnum = ? AND UserKey = ?";
        String[] selectionArgs = new String[]{"3", String.valueOf(this.getCurrentUser().getCredentials().getPrimaryKey())};

        List<T> empLogList = ExecuteFetchListQuery(columns, selection, selectionArgs, null, null, null);

        return empLogList;
    }

    public T FetchByLogDateAndStatus(Date logDate, int status) //LogSourceStatusEnum status)
    {
        String[] selectionArgs = new String[]{String.valueOf(this.getCurrentUser().getCredentials().getPrimaryKey()), DateUtility.getHomeTerminalSqlDateFormat(this.getCurrentUser()).format(logDate), String.valueOf(status)};

        T empLog = ExecuteFetchRawQuery(SQL_SELECT_BYDATESTATUS_COMMAND, selectionArgs);

        return empLog;
    }

    public List<T> FetchLogListByLogDate(Date logDate) {
        String[] selectionArgs = new String[]{String.valueOf(this.getCurrentUser().getCredentials().getPrimaryKey()), DateUtility.getHomeTerminalSqlDateFormat(this.getCurrentUser()).format(logDate)};

        List<T> empLogList = ExecuteFetchListRawQuery(SQL_SELECT_BYDATE_COMMAND, selectionArgs);

        return empLogList;
    }

    public List<T> FetchLogListByLogDateRange(Date startDate, Date endDate) {
        String[] selectionArgs = new String[]{String.valueOf(this.getCurrentUser().getCredentials().getPrimaryKey()), DateUtility.getHomeTerminalSqlDateFormat(this.getCurrentUser()).format(startDate), DateUtility.getHomeTerminalSqlDateFormat(this.getCurrentUser()).format(endDate)};

        List<T> empLogList = ExecuteFetchListRawQuery(SQL_SELECT_BYDATERANGE_COMMAND, selectionArgs);

        return empLogList;
    }

    public List<Date> FetchLogList(Date startDate, Date endDate) {
        List<Date> logDateList = null;
        String sql;
        String[] selectionArgs;

        if (startDate == null && endDate == null) {
            sql = SQL_SELECT_LOGLIST_COMMAND;
            selectionArgs = new String[]{Long.toString(this.getCurrentUser().getCredentials().getPrimaryKey())};
        } else {
            sql = SQL_SELECT_LOGLISTBETWEENDATES_COMMAND;
            selectionArgs = new String[]{Long.toString(this.getCurrentUser().getCredentials().getPrimaryKey()), DateUtility.getHomeTerminalSqlDateFormat(this.getCurrentUser()).format(startDate), DateUtility.getHomeTerminalSqlDateFormat(this.getCurrentUser()).format(endDate)};
        }

        this.open();
        Cursor cursor = this.ExecuteRawQuery(sql, selectionArgs);

        if (cursor != null && cursor.moveToFirst()) {
            logDateList = new ArrayList<Date>(cursor.getCount());

            while (!cursor.isAfterLast()) {
                String logDate = cursor.getString(0);

                // format date retrieved from database in driver's home terminal timezone
                try {
                    logDateList.add(DateUtility.getHomeTerminalSqlDateFormat(this.getCurrentUser()).parse(logDate));
                } catch (ParseException ex) {

                    Log.e("UnhandledCatch", ex.getMessage() + ": " + Log.getStackTraceString(ex));
                }

                cursor.moveToNext();
            }
        }

        cursor.close();
        this.close();

        return logDateList;
    }

    public int FetchLogCountByStatus(int status) {
        return GetScalar(SQL_SELECT_LOGCOUNTBYSTATUS_COMMAND, new String[]{Integer.toString(status)});
    }

    /// <summary>
    /// Purge any old records, based on the cutoff date, using the PURGE command
    /// A parm of @cutoffDate will be added to the command.
    /// </summary>
    /// <param name="cutoffDate"></param>
    public void PurgeOldRecords(Date cutoffDate) {
        String sql;
        String[] selectionArgs;

        sql = SQL_SELECTTOPURGE_COMMAND;
        selectionArgs = new String[]{DateUtility.getHomeTerminalSqlDateFormat(this.getCurrentUser()).format(cutoffDate)};

        this.open();
        Cursor cursor = this.ExecuteRawQuery(sql, selectionArgs);

        if (cursor != null && cursor.moveToFirst()) {

            while (!cursor.isAfterLast()) {
                String logKey = cursor.getString(0);
                selectionArgs = new String[]{logKey};

                sql = SQL_PURGE_EMPLOYEELOGELDEVENT_COMMAND;
                ExecuteQuery(sql, selectionArgs);

                sql = SQL_PURGE_LOGFAILUREREPORT_COMMAND;
                ExecuteQuery(sql, selectionArgs);

                sql = SQL_PURGE_LOGTEAMDRIVER_COMMAND;
                ExecuteQuery(sql, selectionArgs);

                sql = SQL_PURGE_COMMAND;
                ExecuteQuery(sql, selectionArgs);

                cursor.moveToNext();
            }
        }

    }

    // Fetch the weekly reset info for the logs that span a 34 hour reset period
    // the StartingLogDate is the log that we're trying to get info for.
    // The endingLogDate should always be 2 days after the startingLogDate
    // The middleLogDate should always be the first day after the startingLogDate
    public T FetchWeeklyReset(Date startingLogDate, Date middleLogDate, Date endingLogDate) {

        String sql = SQL_SELECT_WEEKLYRESET_COMMAND;
        String[] selectionArgs = new String[]{String.valueOf(this.getCurrentUser().getCredentials().getPrimaryKey()), String.valueOf(this.getCurrentUser().getCredentials().getPrimaryKey()), DateUtility.getHomeTerminalSqlDateFormat(this.getCurrentUser()).format(startingLogDate), DateUtility.getHomeTerminalSqlDateFormat(this.getCurrentUser()).format(endingLogDate), DateUtility.getHomeTerminalSqlDateFormat(this.getCurrentUser()).format(middleLogDate)};

        T empLog = this.ExecuteFetchRawQuery(sql, selectionArgs);
        return empLog;
    }

    // Fetch the weekly reset info for the logs that span a 34 hour reset period
    // the StartingLogDate is the log that we're trying to get info for.
    // The endingLogDate should always be 2 days after the startingLogDate
    // The middleLogDate should always be the first day after the startingLogDate
    public Date FetchPreviousWeeklyResetStartTimestamp(Date logDate) {

        Date previousWeeklyResetTimestamp = null;
        String sql = SQL_SELECT_PREVIOUSWEEKLYRESET_COMMAND;
        String[] selectionArgs = new String[]{String.valueOf(this.getCurrentUser().getCredentials().getPrimaryKey()), DateUtility.getHomeTerminalSqlDateFormat(this.getCurrentUser()).format(logDate)};

        this.open();
        Cursor cursor = this.ExecuteRawQuery(sql, selectionArgs);

        if (cursor != null && cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                String weeklyResetDate = cursor.getString(0);
                try {
                    previousWeeklyResetTimestamp = DateUtility.getHomeTerminalSqlDateTimeFormat(this.getCurrentUser()).parse(weeklyResetDate);
                } catch (ParseException ex) {

                    Log.e("UnhandledCatch", ex.getMessage() + ": " + Log.getStackTraceString(ex));
                }

                cursor.moveToNext();
            }
        }

        cursor.close();
        this.close();

        return previousWeeklyResetTimestamp;
    }

    public List<T> GetLogsByWeeklyResetStartTimestamp(Date weeklyResetStartTimestamp) {
        String sql = SQL_SELECT_LOGSBYWEEKLYRESET_COMMAND;
        String[] selectionArgs = new String[]{String.valueOf(this.getCurrentUser().getCredentials().getPrimaryKey()), DateUtility.getHomeTerminalSqlDateTimeFormat(this.getCurrentUser()).format(weeklyResetStartTimestamp)};

        List<T> empLogList = ExecuteFetchListRawQuery(sql, selectionArgs);
        return empLogList;
    }

    public boolean ExistAnyLogEarlierThan(Date checkDate) {
        String sql = SQL_SELECT_LOGCOUNT_PRIORTO_COMMAND;
        String[] selectionArgs = new String[]{DateUtility.getHomeTerminalSqlDateTimeFormat(this.getCurrentUser()).format(checkDate)};

        int count = this.GetScalar(sql, selectionArgs);
        return count > 0;
    }

    // Get the most recent weekly reset time for the logs of the current user
    public Date GetMostRecentWeeklyResetStartTimestamp() {

        Date weeklyResetTimestamp = null;
        String sql = SQL_SELECT_MOSTRECENTWEEKLYRESET_COMMAND;
        String[] selectionArgs = new String[]{String.valueOf(this.getCurrentUser().getCredentials().getPrimaryKey())};

        this.open();
        Cursor cursor = this.ExecuteRawQuery(sql, selectionArgs);

        if (cursor != null && cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                String weeklyResetDate = cursor.getString(0);
                try {
                    if (weeklyResetDate != null) {
                        weeklyResetTimestamp = DateUtility.getHomeTerminalSqlDateTimeFormat(this.getCurrentUser()).parse(weeklyResetDate);
                    }
                } catch (ParseException ex) {

                    Log.e("UnhandledCatch", ex.getMessage() + ": " + Log.getStackTraceString(ex));
                }

                cursor.moveToNext();
            }
        }

        cursor.close();
        this.close();

        return weeklyResetTimestamp;
    }

    public Date GetFirstAvailableLogDate() {
        Date logDate = null;
        String sql;
        String[] selectionArgs;

        sql = SQL_SELECT_EMPLOYEELOG_FIRSTLOGDATE;
        selectionArgs = new String[]{Long.toString(this.getCurrentUser().getCredentials().getPrimaryKey())};

        this.open();
        Cursor cursor = this.ExecuteRawQuery(sql, selectionArgs);

        if (cursor != null && cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                String date = cursor.getString(0);
                try {
                    logDate = DateUtility.getHomeTerminalSqlDateTimeFormat(this.getCurrentUser()).parse(date);
                } catch (ParseException ex) {

                    Log.e("UnhandledCatch", ex.getMessage() + ": " + Log.getStackTraceString(ex));
                }

                cursor.moveToNext();
            }
        }

        cursor.close();
        this.close();

        return logDate;
    }

    /**
     * Return the active EmployeeLog the new Certification Event should be added to.
     */
    public T FetchEmployeeLogToCertify(Date logDate) {
        String[] selectionArgs = new String[]{String.valueOf(this.getCurrentUser().getCredentials().getPrimaryKey()), DateUtility.getHomeTerminalSqlDateFormat(this.getCurrentUser()).format(logDate), DateUtility.getHomeTerminalSqlDateFormat(this.getCurrentUser()).format(logDate)};

        // Return LogSourceStatusEnum=1 (local) if it exists; otherwise return highest Primary Key which presumable means the most recent data
        T empLog = ExecuteFetchRawQuery(SQL_SELECT_EMPLOYEELOG_TOCERTIFY_COMMAND, selectionArgs);

        return empLog;
    }

    /**
     * Gets all log dates that are not marked as certified based on the IsCertified flag in the EmployeeLog Table
     *
     * @return A list of log dates that have not been certified. If none exist, an empty list is returned.
     */
    public List<Date> FetchUncertifiedLogDates() {
        List<Date> result = new ArrayList<Date>();

        // Return LogSourceStatusEnum=1 (local) if it exists; otherwise return highest Primary Key which presumable means the most recent data
        //String sql = SQL_SELECT_UNCERTIFIED_LOGS_COMMAND;
        String sql = SQL_SELECT_UNCERTIFIED_NOT_EXEMPT_LOGS_COMMAND;
        String[] selectionArgs = new String[]{String.valueOf(this.getCurrentUser().getCredentials().getPrimaryKey())};

        this.open();
        Cursor cursor = this.ExecuteRawQuery(sql, selectionArgs);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                // format date retrieved from database in driver's home terminal timezone
                try {
                    result.add(DateUtility.getHomeTerminalSqlDateFormat(this.getCurrentUser()).parse(cursor.getString(0)));
                } catch (ParseException ex) {
                    Log.e("UnhandledCatch", ex.getMessage() + ": " + Log.getStackTraceString(ex));
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        this.close();

        return result;
    }

    /**
     * Gets all log dates that already marked certified but have NOT been submitted
     *
     * @return A list of log dates that have been certified but unsubmitted. If none exist, an empty list is returned.
     */
    public List<Date> FetchCertifiedUnsubmittedLogDates() {

        List<Date> result = new ArrayList<Date>();
        String sql = SQL_SELECT_CERTIFIED_UNSUBMITTED_NOT_EXEMPT_LOGS_COMMAND;
        String[] selectionArgs = new String[]{String.valueOf(this.getCurrentUser().getCredentials().getPrimaryKey())};

        this.open();
        Cursor cursor = this.ExecuteRawQuery(sql, selectionArgs);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                try {
                    result.add(DateUtility.getHomeTerminalSqlDateFormat(this.getCurrentUser()).parse(cursor.getString(0)));
                } catch (ParseException ex) {
                    Log.e("UnhandledCatch", ex.getMessage() + ": " + Log.getStackTraceString(ex));
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        this.close();

        return result;
    }

    /**
     * Gets all logs that have ELDEvents marked for review
     */
    public List<T> FetchLogsWithUnreviewedEdits() {
        String sql = SQL_SELECT_LOGS_UNREVIEWED_EDITS;
        String[] selectionArgs = new String[]{String.valueOf(this.getCurrentUser().getCredentials().getPrimaryKey())};

        List<T> empLogList = ExecuteFetchListRawQuery(sql, selectionArgs);
        return empLogList;
    }

    /**
     * Select EmployeeLog record based on it's primary key.
     */
    public EmployeeLog FetchByKey(int uniqueKey) {
        EmployeeLog log = ExecuteFetchRawQuery(SQL_SELECT_KEY_COMMAND, new String[]{Integer.toString(uniqueKey)});
        return log;
    }

}
