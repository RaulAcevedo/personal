package com.jjkeller.kmbapi.controller.abstracts;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.jjkeller.kmbapi.R;
import com.jjkeller.kmbapi.calcengine.Enums;
import com.jjkeller.kmbapi.calcengine.ExemptLogValidatorFactory;
import com.jjkeller.kmbapi.calcengine.IExemptLogValidator;
import com.jjkeller.kmbapi.calcengine.IHosRulesetCalcEngine;
import com.jjkeller.kmbapi.common.TimeKeeper;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.configuration.LocationCodeDictionary;
import com.jjkeller.kmbapi.configuration.LoginCredentials;
import com.jjkeller.kmbapi.controller.ControllerFactory;
import com.jjkeller.kmbapi.controller.EOBR.EobrReader;
import com.jjkeller.kmbapi.controller.EmployeeLogRevisionController;
import com.jjkeller.kmbapi.controller.EmployeeRuleController;
import com.jjkeller.kmbapi.controller.EngineRecordController;
import com.jjkeller.kmbapi.controller.EobrConfigController;
import com.jjkeller.kmbapi.controller.ExemptLogValidationController;
import com.jjkeller.kmbapi.controller.FailureController;
import com.jjkeller.kmbapi.controller.FuelPurchaseController;
import com.jjkeller.kmbapi.controller.HosAlertController;
import com.jjkeller.kmbapi.controller.HosAuditController;
import com.jjkeller.kmbapi.controller.LocationCodeController;
import com.jjkeller.kmbapi.controller.LogEntryController;
import com.jjkeller.kmbapi.controller.LogGridSummary;
import com.jjkeller.kmbapi.controller.LogRemarksController;
import com.jjkeller.kmbapi.controller.RouteController;
import com.jjkeller.kmbapi.controller.TeamDriverController;
import com.jjkeller.kmbapi.controller.TripRecordController;
import com.jjkeller.kmbapi.controller.UnassignedPeriodController;
import com.jjkeller.kmbapi.controller.VehicleInspectionController;
import com.jjkeller.kmbapi.controller.calcengine.HOSAudit;
import com.jjkeller.kmbapi.controller.calcengine.LogEventSummary;
import com.jjkeller.kmbapi.controller.calcengine.LogSummary;
import com.jjkeller.kmbapi.controller.dataaccess.EmployeeLogEldEventFacade;
import com.jjkeller.kmbapi.controller.dataaccess.EmployeeLogFacade;
import com.jjkeller.kmbapi.controller.dataaccess.EngineRecordFacade;
import com.jjkeller.kmbapi.controller.dataaccess.EventDataRecordFacade;
import com.jjkeller.kmbapi.controller.dataaccess.FuelPurchaseFacade;
import com.jjkeller.kmbapi.controller.dataaccess.LocationDBAdapter;
import com.jjkeller.kmbapi.controller.dataaccess.LocationDBFacade;
import com.jjkeller.kmbapi.controller.dataaccess.RoutePositionFacade;
import com.jjkeller.kmbapi.controller.dataaccess.TripRecordFacade;
import com.jjkeller.kmbapi.controller.dataaccess.UnassignedDrivingPeriodFacade;
import com.jjkeller.kmbapi.controller.dataaccess.UnassignedEobrFailurePeriodFacade;
import com.jjkeller.kmbapi.controller.dataaccess.UserFacade;
import com.jjkeller.kmbapi.controller.interfaces.IAPIController;
import com.jjkeller.kmbapi.controller.interfaces.IEmployeeLogFacade;
import com.jjkeller.kmbapi.controller.interfaces.IHosAlert;
import com.jjkeller.kmbapi.controller.interfaces.IReverseGeocodeLocationListener;
import com.jjkeller.kmbapi.controller.interfaces.ISpecialDrivingController;
import com.jjkeller.kmbapi.controller.share.ExemptLogPreviousLogsConvertedToGridLogEventArgs;
import com.jjkeller.kmbapi.controller.share.KmbApplicationException;
import com.jjkeller.kmbapi.controller.share.MandateObjectFactory;
import com.jjkeller.kmbapi.controller.share.SpecialDrivingFactory;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.controller.utility.EmployeeLogUtilities;
import com.jjkeller.kmbapi.controller.utility.ErrorLogHelper;
import com.jjkeller.kmbapi.controller.utility.FailureReportComparer;
import com.jjkeller.kmbapi.controller.utility.LocationCodeComparer;
import com.jjkeller.kmbapi.controller.utility.LogComparer;
import com.jjkeller.kmbapi.controller.utility.RESTWebServiceHelper;
import com.jjkeller.kmbapi.controller.utility.ReverseGeocodeUtilities;
import com.jjkeller.kmbapi.controller.utility.TeamDriverComparer;
import com.jjkeller.kmbapi.employeelogeldevents.EmployeeLogEldEventCode;
import com.jjkeller.kmbapi.enums.CanadaDeferralTypeEnum;
import com.jjkeller.kmbapi.enums.DataProfileEnum;
import com.jjkeller.kmbapi.enums.DutyStatusEnum;
import com.jjkeller.kmbapi.enums.EmployeeLogProvisionTypeEnum;
import com.jjkeller.kmbapi.enums.EmployeeLogRevisionTypeEnum;
import com.jjkeller.kmbapi.enums.ExemptLogTypeEnum;
import com.jjkeller.kmbapi.enums.HosAlertTypeEnum;
import com.jjkeller.kmbapi.enums.LogStatus;
import com.jjkeller.kmbapi.enums.RuleSetTypeEnum;
import com.jjkeller.kmbapi.enums.TimeZoneEnum;
import com.jjkeller.kmbapi.kmbeobr.StatusRecord;
import com.jjkeller.kmbapi.proxydata.DecodedLocation;
import com.jjkeller.kmbapi.proxydata.EldEventAdapterList;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;
import com.jjkeller.kmbapi.proxydata.EmployeeLogList;
import com.jjkeller.kmbapi.proxydata.EmployeeLogReportRequest;
import com.jjkeller.kmbapi.proxydata.EventDataRecord;
import com.jjkeller.kmbapi.proxydata.FailureReport;
import com.jjkeller.kmbapi.proxydata.GpsLocation;
import com.jjkeller.kmbapi.proxydata.KMBEncompassUserList;
import com.jjkeller.kmbapi.proxydata.Location;
import com.jjkeller.kmbapi.proxydata.LocationCode;
import com.jjkeller.kmbapi.proxydata.MobileDevice;
import com.jjkeller.kmbapi.proxydata.TeamDriver;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import static com.jjkeller.kmbapi.enums.LogStatus.ACTIVE_LOCAL_LOG;

/**
 * Abstract class representing base functionality for AOBRD-specific common functions
 * This class extends the APIControllerBase with the intent of acting as a bridge between AOBRD and Mandate functionality
 */
public abstract class AOBRDControllerBase extends APIControllerBase {
    Boolean _downloadLogSkipped = false;

    public AOBRDControllerBase(Context ctx) {
        super(ctx);
    }

    public AOBRDControllerBase(Context ctx, EobrReader eobrReader) {
        super(ctx, eobrReader);
    }

    @Override
    public List<EmployeeLog> Fetch() {
        IEmployeeLogFacade logFacade = new EmployeeLogFacade(this.getContext());
        return logFacade.Fetch();
    }

    @Override
    public List<LogSummary> EmployeeLogsForDailyHoursReport() {
        List<EmployeeLog> logList = this.EmployeeLogsForReports(false);

        HOSAudit audit = new HOSAudit();
        Date currentHomeTerminalTime = DateUtility.CurrentHomeTerminalTime(this.getCurrentUser());

        return audit.PerformCompleteAudit(this.getCurrentUser(), this.getContext(), logList, currentHomeTerminalTime);
    }

    /**
     * Gets employee logs displayed on Daily Hours Recap report.
     * Includes 3 extra past days vs. normal Daily Hours report.
     *
     * @return Gets all the log summaries including up to an extra 3 days for the recap.
     */
    @Override
    public List<LogSummary> EmployeeLogsForDailyHoursRecapReport() {
        List<EmployeeLog> logList = this.EmployeeLogsRecapForReports(false);
        HOSAudit audit = new HOSAudit();
        Date currentHomeTerminalTime = DateUtility.CurrentHomeTerminalTime(this.getCurrentUser());
        List<LogSummary> logSummary = audit.PerformCompleteAudit(this.getCurrentUser(), this.getContext(), logList, currentHomeTerminalTime);
        // on check hours mode there may not be a log for Today
        if (logSummary.size() > 0) {
            Date lastLogDate = logSummary.get(0).getLogDate();
            if (!lastLogDate.equals(currentHomeTerminalTime)) {
                LogSummary newLogSummary = new LogSummary();
                newLogSummary.setLogDate(currentHomeTerminalTime);
                newLogSummary.setOnDutyDuration(0L);
                newLogSummary.setDrivingDuration(0L);
                logSummary.add(0, newLogSummary);
            }
        }
        return logSummary;
    }

    /**
     * Gets 3 future days displayed on Daily Hours Recap report and calculates the hours gained on each day.
     *
     * @param logSummaries THe log summaries to search through
     * @return Up to 3 logs summaries to display on the daily hours recap.
     */
    @Override
    public List<LogSummary> FutureDaysForDailyHoursRecapReport(List<LogSummary> logSummaries) {
        List<LogSummary> futureLogs = new ArrayList<LogSummary>();

        Date currentHomeTerminalTimeTime = DateUtility.CurrentHomeTerminalTime(this.getCurrentUser());

        IHosRulesetCalcEngine currentRulesetEngine = new HosAuditController(this.getContext()).getCalcEngine();
        Date weeklyDutyPeriodStart = DateUtility.AddDays(currentHomeTerminalTimeTime, -1 * (currentRulesetEngine.GetWeeklyDutyPeriodDays() - 1));

        Date recapLogSummaryDate = DateUtility.GetDateFromDateTime(currentHomeTerminalTimeTime);
        Date recapCalcFromLogDate = DateUtility.GetDateFromDateTime(weeklyDutyPeriodStart);
        for (int i = 1; i <= 3; i++) {
            LogSummary recapLogSummary = new LogSummary();
            futureLogs.add(recapLogSummary);

            recapLogSummary.setLogDate(recapLogSummaryDate);

            if (logSummaries != null && !logSummaries.isEmpty()) {
                for (LogSummary logSummary : logSummaries) {
                    if (logSummary.getLogDate().equals(recapCalcFromLogDate)) {
                        recapLogSummary.setLogExists(logSummary.getLogExists());
                        recapLogSummary.setOnDutyDuration(logSummary.getOnDutyDuration());
                        recapLogSummary.setDrivingDuration(logSummary.getDrivingDuration());
                    }
                }
            }

            recapLogSummaryDate = DateUtility.AddDays(recapLogSummaryDate, 1);
            recapCalcFromLogDate = DateUtility.AddDays(recapCalcFromLogDate, 1);
            recapLogSummary.setLogDate(recapLogSummaryDate);
        }

        return futureLogs;
    }

    /// <summary>
    /// Download all of the compliance records for the employee.
    /// This includes downloading employee logs, rules, and location codes.
    /// The number of days of logs that will be downloaded is dependent on
    /// the employee rule in place.
    /// Answer the list of logDates that are missing.
    /// </summary>
    /// <param name="ignoreAllErrors">controls whether exceptions are thrown when errors occur</param>
    /// <returns></returns>
    @Override
    public List<Date> DownloadRecordsForCompliance(User user, boolean ignoreAllErrors) throws KmbApplicationException {
        try {
            if (this.getIsWebServicesAvailable()) {
                // download all employee logs needed for compliance
                this.DownloadRecentLogs(user);

                this.SynchronizeUnclaimedEvents(user);

                // download location codes
                LocationCodeController locationCodeController = new LocationCodeController(this.getContext());
                locationCodeController.DownloadLocationCodes();

                // download log remark items
                LogRemarksController logRemkarksController = new LogRemarksController(this.getContext());
                logRemkarksController.DownloadLogRemarkItems();

                // perform HOS calculations whenever new logs are downloaded
                HosAuditController auditController = new HosAuditController(this.getContext());
                auditController.UpdateForCurrentLogEvent();
            }
        } catch (JsonParseException e) {
            // when connected to a network, but unable to get to webservice "e" is null
            if (!ignoreAllErrors) {
                if (e == null) {
                    e = new JsonParseException(JsonParseException.class.getName());
                }
                throw e;
            }
        } catch (KmbApplicationException kae) {
            if (!ignoreAllErrors) {
                throw kae;
            }
        }

        // return list of missing logs
        return this.GetMissingLogDateList(user);
    }

    /// <summary>
    /// This method is used from the "Check for Edit Requests button "
    /// Very similar to DownloadRecordsForCompliance but it only downloads logs
    /// Returns false if failed
    /// </summary>    
    public Boolean DownloadLogsWithEditRequests(User user) {
        boolean downloadedLogs;

        try {
            if (this.getIsWebServicesAvailable()) {
                Date today = DateUtility.CurrentHomeTerminalTime(user);
                today = EmployeeLogUtilities.CalculateLogStartTime(this.getContext(), today, user.getHomeTerminalTimeZone());
                Date endDate = today;
                Date startDate = DateUtility.AddDays(today, -7);
                this.DownloadLogs(user, startDate, endDate);
                downloadedLogs = !this.getDownloadLogSkipped();
            } else {
                downloadedLogs = false;
            }
        } catch (Exception ex) {
            Log.e("Check for Edits Error", ex.getMessage(), ex);
            downloadedLogs = false;
        }

        return downloadedLogs;
    }

    @Override
    public List<Date> GetMissingLogDateList(User user) {
        int numLogsToHaveExcludingToday = 7;

        if (user.IsCanadianRulesetAvailable()) {
            numLogsToHaveExcludingToday = 14;
        }

        Date today = DateUtility.CurrentHomeTerminalTime(user);
        today = EmployeeLogUtilities.CalculateLogStartTime(this.getContext(), today, user.getHomeTerminalTimeZone());
        Date endDate = DateUtility.AddDays(today, -1);
        Date startDate = DateUtility.AddDays(today, numLogsToHaveExcludingToday * -1);

        IEmployeeLogFacade facade = new EmployeeLogFacade(this.getContext(), user);
        int availableLogCountExcludingToday = facade.FetchLogCount(startDate, endDate);
        boolean isUpToDate = availableLogCountExcludingToday >= numLogsToHaveExcludingToday;

        if (!isUpToDate) {
            List<Date> existingLogDates = facade.FetchLogList(startDate, endDate);

            List<Date> missingLogDates = new ArrayList<Date>();
            for (Date logDate = startDate; logDate.compareTo(endDate) <= 0; logDate = DateUtility.AddDays(logDate, 1)) {
                if (existingLogDates != null && existingLogDates.size() > 0) {
                    if (!existingLogDates.contains(logDate)) {
                        missingLogDates.add(logDate);
                    }
                } else {
                    missingLogDates.add(logDate);
                }
            }

            return missingLogDates;
        } else {
            return null;
        }
    }

    /// <summary>
    /// Answer the trip info parms for the current log
    /// </summary>
    /// <param name="trailerNumbers"></param>
    /// <param name="shipmentInfo"></param>
    @Override
    public Bundle GetTripInfo() {
        LogEntryController ctrlr = new LogEntryController(this.getContext());
        EmployeeLog currentLog = ctrlr.getCurrentEmployeeLog();
        return this.GetTripInfo(currentLog);
    }

    @Override
    public Bundle GetTripInfo(EmployeeLog currentLog) {
        Bundle b = new Bundle();
        b.putString("trailerNumbers", currentLog.getTrailerNumbers());
        b.putString("trailerPlate", currentLog.getTrailerPlate());
        b.putString("tractorNumber", currentLog.getTractorNumbers());
        b.putString("shipmentInfo", currentLog.getShipmentInformation());
        b.putString("vehiclePlate", currentLog.getVehiclePlate());
        b.putBoolean("returnToWorkLocation", currentLog.getHasReturnedToLocation());
        b.putInt("canadaDeferralType", currentLog.getCanadaDeferralType().getValue());
        b.putBoolean("ishaulingexplosives", currentLog.getIsHaulingExplosives());
        b.putBoolean("isExemptFrom30MinBreakRequirement", currentLog.getIsExemptFrom30MinBreakRequirement());
        if (currentLog.getWeeklyResetStartTimestamp() != null && !currentLog.getRuleset().isAnyOilFieldRuleset()) {
            // if the weekly reset belongs on todays log, then show it
            Date resetEndingTimestamp = new Date(currentLog.getWeeklyResetStartTimestamp().getTime() + 34 * DateUtility.MILLISECONDS_PER_HOUR);
            if (currentLog.getLogDate().compareTo(resetEndingTimestamp) <= 0) {
                b.putBoolean("isweeklyresetused", currentLog.getIsWeeklyResetUsed());
            }
        }
        if (currentLog.getRuleset().isAnyOilFieldRuleset()) {
            b.putBoolean("isoperatesspecificvehiclesforoilfield", currentLog.getIsOperatesSpecificVehiclesForOilfield());
        }
        return b;
    }

    /// <summary>
    /// Answer the employee log for the specified date.  First look for an unsubmitted
    /// log.  If no log is unsubmitted, then use a downloaded log.
    /// </summary>
    @Override
    public EmployeeLog GetEmployeeLog(Date logDate) {
        return this.GetEmployeeLog(this.getCurrentUser(), logDate);
    }

    /// <summary>
    /// Answer the employee log for the specified date.  First look for an unsubmitted
    /// log.  If no log is unsubmitted, then use a downloaded log.
    /// </summary>
    @Override
    public EmployeeLog GetEmployeeLog(User user, Date logDate) {
        EmployeeLog empLog = this.GetLocalEmployeeLog(user, logDate);
        if (empLog == null) {
            empLog = this.GetDownloadedEmployeeLog(user, logDate);
        }

        return empLog;
    }

    @Override
    public List<EmployeeLog> GetEmployeeLogs(User user, Date logDate) {
        return new EmployeeLogFacade(getContext(), user).GetLogsByDate(logDate);
    }

    /// <summary>
    /// Return the active EmployeeLog the new Certification Event should be added to.
    /// </summary>
    @Override
    public EmployeeLog GetEmployeeLogToCertify(Date logDate) {
        return new EmployeeLogFacade(getContext(), this.getCurrentUser()).GetEmployeeLogToCertify(logDate);
    }

    /// <summary>
    /// Answer the local, unsubmitted, employee log for the user
    /// for the specified date.
    /// If no log is found locally, then a new one is created.
    /// </summary>
    /// <param name="logDate">date to search for</param>
    /// <returns>EmployeeLog matching the date, if no log exists for the date, a new one is created</returns>
    @Override
    public EmployeeLog GetLocalEmployeeLogOrCreateTransition(User usr, Date timestampNow) {
        Date logDate = null;
        logDate = EmployeeLogUtilities.CalculateLogStartTime(this.getContext(), timestampNow, usr.getHomeTerminalTimeZone());

        // fetch the log for the current user, for the specified log date
        EmployeeLog empLog = this.GetLocalEmployeeLog(usr, logDate);

        if (empLog == null) {
            // todays log does not exist
            // try to transition yesterdays info into a new log
            Date yesterday = DateUtility.AddDays(logDate, -1);
            EmployeeLog yesterdaysLog = this.GetLocalEmployeeLog(usr, yesterday);
            if (yesterdaysLog != null) {
                // transition yesterday's info over to today
                empLog = this.CreateNewLogForTransition(usr, yesterdaysLog, timestampNow);
            } else {
                // no log for yesterday, just create a new one
                empLog = EmployeeLogUtilities.CreateNewLog(getContext(), usr, logDate, new DutyStatusEnum(DutyStatusEnum.OFFDUTY), null);
            }

            empLog.setIsExemptFrom30MinBreakRequirement(yesterdaysLog.getIsExemptFrom30MinBreakRequirement());

            this.SaveLocalEmployeeLog(usr, empLog);
        }

        return empLog;
    }

    ///<summary>
    /// Gets the Employee Log for the Current user for the current date of the users device/timezone
    ///<param name="user">need to pass a user to get the current log/param>
    ///</summary>
    public EmployeeLog GetCurrentEmployeeLogByUser(User user) {
        Date now = DateUtility.getCurrentDateTime().toDate();
        Date todaysDate = EmployeeLogUtilities.CalculateLogStartTime(this.getContext(), now, user.getHomeTerminalTimeZone());
        EmployeeLog log = this.GetEmployeeLog(user, todaysDate);

        return log;
    }

    /// <summary>
    /// Adds an On-Duty event to the current log, at the current time.
    /// This will cause all events after the current time to be removed
    /// from the log.
    /// This is used when the user logs into the system.
    /// <param name="empLog">log to add the on duty event to</param>
    /// </summary>
    @Override
    public void AddLoginEventToCurrentLog(EmployeeLog empLog, DutyStatusEnum dutyStatus) {
        // convert the UTC time into the timezome of the driver's home terminal
        Date loginTimestamp = this.getCurrentClockHomeTerminalTime();
        ErrorLogHelper.RecordMessage(getContext(), String.format("LogEntryController.AddLoginEventToCurrentLog at: {%s} :: timekeeper.offset {%s}", DateUtility.getHomeTerminalDateTimeFormat().format(loginTimestamp), DateUtility.ClockTimestampOffset()));
        String CurrEobrSerialNumber = "";
        if (GlobalState.getInstance() != null) {
            CurrEobrSerialNumber = GlobalState.getInstance().getCurrentEobrSerialNumber();
        }
        Date loginTimePlus30Minutes = DateUtility.AddMinutes(loginTimestamp, 30);
        if (EmployeeLogUtilities.DrivingEventExistsBetween(empLog, loginTimestamp, loginTimePlus30Minutes)) {
            // 2014.05.16 sjn - there is a driving event on the current log that occur with 30 minutes after the login timestamp
            // If the ELD clock has been running fast, the ELD clock may be in "drift".
            // This means that driving events may be created in the future of current time.

            // KMB only allows a tab clock to run 30 minutes fast, so if a driving event exists,
            // then it must be here because of a fast running ELD clock

            // When this happens, change the login time to the time of the last event on the log
            EmployeeLogEldEvent lastEvent = null;
            try {
                lastEvent = EmployeeLogUtilities.GetLastEventInLog(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus, empLog);
                loginTimestamp = DateUtility.AddMinutes(lastEvent.getStartTime(), 1);
                ErrorLogHelper.RecordMessage(getContext(), String.format("LogEntryController.AddLoginEventToCurrentLog detected driving events within 30 minutes of login...changing login time to: {%s} :: timekeeper.offset {%s}", DateUtility.getHomeTerminalDateTimeFormat().format(loginTimestamp), DateUtility.ClockTimestampOffset()));
            } catch (Throwable e) {
                Log.e("ELD Mandate Duty Error", "Did not retrieve eld mandate login event due to exception.", e);
            }
        }

        EmployeeLogUtilities.RemoveAllEventsAfter(empLog, loginTimestamp);

        // fetch the last event on the log to get the info from the last known location
        try {
            EmployeeLogEldEvent lastKnownEvent = this.GetMostRecentLoginEvent(empLog);
            Location lastKnownLocation = null;
            if (lastKnownEvent != null) {
                lastKnownLocation = lastKnownEvent.getLocation();
            }

            Location newLocation = new Location();
            if (lastKnownLocation != null) {
                if (!GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
                    newLocation.setName(lastKnownLocation.getName());
                }
                newLocation.setGpsInfo(lastKnownLocation.getGpsInfo());
                if (lastKnownEvent.getEobrSerialNumber() == CurrEobrSerialNumber) {
                    newLocation.setOdometerReading(lastKnownLocation.getOdometerReading());
                }

            }

            LogEntryController logEntryController = new LogEntryController(this.getContext());
            if (logEntryController.getLastValidGPSLocation() != null) {
                // update the GPS reading at login, if available
                newLocation.setGpsInfo(logEntryController.getLastValidGPSLocation());
                this.ReverseGeocodeLocation(empLog, newLocation.getGpsInfo());
            }

            if (logEntryController.getLastValidOdometerReading() > 0) {
                // update the odometer reading from login, when it's available
                if (CurrEobrSerialNumber == logEntryController.getCurrentEobrSerialNumber()) {
                    newLocation.setOdometerReading(logEntryController.getLastValidOdometerReading());
                }
            }

            if (lastKnownEvent != null && loginTimestamp.compareTo(lastKnownEvent.getStartTime()) < 0) {
                // if the login time is before the last event on the log
                // then adjust the loginTimestamp to be that time of the last event
                loginTimestamp = lastKnownEvent.getStartTime();
            }
            IAPIController eventController = MandateObjectFactory.getInstance(getContext(), GlobalState.getInstance().getFeatureService()).getCurrentEventController();

            int recordOrigin = com.jjkeller.kmbapi.employeelogeldevents.Enums.EmployeeLogEldEventRecordOrigin.EditedEnteredByDriver;
            //Create our login event. Under AOBRD, this will be an OnDutyEvent
            //Under Mandate, this will be a driver-selectable duty status
            eventController.CreateDutyStatusChangedEventForLogin(empLog, loginTimestamp, dutyStatus, newLocation, GlobalState.getInstance().getIsMobileClockSynchronized(), getCurrentUser().getRulesetTypeEnum(), null, null, recordOrigin, empLog.getEmployeeId());
        } catch (Throwable e) {
            Log.e("ELD Mandate Duty Error", "Unable to retrieve last event for processing.", e);
        }

        if (empLog.getMobileStartTimestamp() == null) {
            // mobile start not assigned yet, give it the current login event
            try {

                EmployeeLogEldEvent loginDutyStatusEvent = EmployeeLogUtilities.GetLastEventInLog(empLog);
                Date logStartTimestamp = loginDutyStatusEvent.getStartTime();
                empLog.setMobileStartTimestamp(logStartTimestamp);
            } catch (Throwable e) {
                Log.e("ELD Mandate Duty Error", "Did not retrieve eld mandate login event due to exception.", e);
            }
        } else {
            // 2013.10.02 sjn
            // if the mobileLogStartTimestamp is in the future with respect to the login time, then the mobileLogStartTimestamp needs to be set to the login time
            // this is done so that any log events created in the current KMB session will be submitted to DMO and included when merging into an existing DMO log
            if (loginTimestamp.getTime() < empLog.getMobileStartTimestamp().getTime()) {
                empLog.setMobileStartTimestamp(loginTimestamp);
            }
        }
    }

    @Override
    public EmployeeLogEldEvent GetMostRecentLoginEvent(EmployeeLog employeeLog) throws Exception {
        return EmployeeLogUtilities.GetLastEventInLog(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus, employeeLog);
    }

    @Override
    public EmployeeLog CreateNewLogForTransition(User usr, EmployeeLog currentLog, Date timestamp) {
        return this.CreateNewLogForTransition(usr, currentLog, timestamp, null);
    }


    /// <summary>
    /// Create the new employee log due to a transition from one log date to another.
    /// Typically this will happen when the first report from the EOBR comes in for
    /// the next log date.
    /// </summary>
    /// <param name="currentLog"></param>
    /// <param name="timestamp"></param>
    /// <returns></returns>
    @Override
    public EmployeeLog CreateNewLogForTransition(User usr, EmployeeLog currentLog, Date timestamp, Date potentialDrivingStopTimestamp) {
        ErrorLogHelper.RecordMessage(getContext(), String.format("LogEntryController.CreateNewLogForTransition from dt: {%s} emp: {%s}", DateUtility.getHomeTerminalDateFormat().format(currentLog.getLogDate()), usr.getCredentials().getEmployeeId()));

        // first fetch the last known event from the current log
        EmployeeLogEldEvent lastKnownEvent = EmployeeLogUtilities.GetLastEventInLog(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus, currentLog);
        // calc the timestamp for the end of the current log
        Date currentLogStartTime = EmployeeLogUtilities.CalculateLogStartTime(this.getContext(), currentLog.getLogDate(), usr.getHomeTerminalTimeZone());
        currentLog.setMobileEndTimestamp(DateUtility.AddDays(currentLogStartTime, 1));

        // transition driving status, if appropriate (either for PC/NonReg driving, or standard driving)
        LogEntryController logEntryCtrlr = new LogEntryController(this.getContext());
        boolean shouldTransitionDriving = lastKnownEvent.getDutyStatusEnum().getValue() == DutyStatusEnum.DRIVING
                || GlobalState.getInstance().getIsInPersonalConveyanceDrivingSegment()
                || GlobalState.getInstance().getIsInNonRegDrivingSegment()
                || GlobalState.getInstance().getIsInHyrailDrivingSegment();

        if(GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
            //always need to end a PC status regardless of driving segments in the mandate
            shouldTransitionDriving |= GlobalState.getInstance().getIsInPersonalConveyanceDutyStatus();
            shouldTransitionDriving |= GlobalState.getInstance().getIsInYardMoveDutyStatus();
        }

        if (shouldTransitionDriving) {
            float startOdometer = lastKnownEvent.getLocation().getOdometerReading();
            float currentOdometer = logEntryCtrlr.getLastValidOdometerReading();
            float distanceInDrivingPeriod = currentOdometer - startOdometer;

            // now, update the mileage on the current log for the driving segment
            lastKnownEvent.getLocation().setEndOdometerReading(currentOdometer);

            // special note: the currentLog should be the "drivers" log if this
            // is a team-driving situation because a driving period is being ended

            String debugMessage = String.format("Transition final mileage segment: drivingStartOdom '%f' currentOdom '%f' distance '%f' mobileDerivedDist '%f'.", startOdometer, currentOdometer, distanceInDrivingPeriod, currentLog.getMobileDerivedDistance());
            ErrorLogHelper.RecordMessage(this.getContext(), debugMessage);

            // For genI we are using the VehicleMotionDetector, so if the potentialDrivingStopTimestamp wasn't
            // set by GenII, check if the VehicleMotionDetector has a potentialDrivingStopTimestamp.
            if (potentialDrivingStopTimestamp == null && logEntryCtrlr.getVehicleMotionDetector().getPotentialDrivingStopTimestamp() != null) {
                potentialDrivingStopTimestamp = logEntryCtrlr.getVehicleMotionDetector().getPotentialDrivingStopTimestamp();
            }

            if (potentialDrivingStopTimestamp != null && !logEntryCtrlr.getHasExtendedDrivingSegment()) {
                // The motion detector has sensed a potential stop
                // this means that the vehicle has just stopped, but not long enough to
                // to end the driving segment
                // The driving segment needs to be ended right now because we're trying
                // to transition across midnight
                // Add an On-Duty at the time of the potential stop
                // NOTE:  if there is a potential driving stop timestamp, but the driving segment has
                // been manually extended, do not stop the driving event and create an on duty event.
                Location newLoc = new Location(lastKnownEvent.getLocation().getName());
                newLoc.setGpsInfo(logEntryCtrlr.getLastValidGPSLocation());
                newLoc.setOdometerReading(logEntryCtrlr.getLastValidOdometerReading());

                // the last event now on the log is the driving that is about to be added
                // this needs to be driving status to continue the next lag of the trip.
                lastKnownEvent = EmployeeLogUtilities.AddEventToLog(currentLog, potentialDrivingStopTimestamp, new DutyStatusEnum(DutyStatusEnum.ONDUTY), newLoc, true, usr.getRulesetTypeEnum(), null, null);
                lastKnownEvent.setDutyStatusEnum(new DutyStatusEnum(DutyStatusEnum.DRIVING));
            }

            //end 1 second before the beginning of the next log
            Date endTime = DateUtility.AddSeconds(currentLogStartTime, DateUtility.SECONDS_PER_DAY - 1);
            endSpecialDrivingForMidnightTransition(currentLog, endTime, lastKnownEvent.getLocation());
        }

        // update the location with GPS and odometer readings from the current location
        Location newLocation = new Location(lastKnownEvent.getLocation().ToLocationString());
        newLocation.setOdometerReading(logEntryCtrlr.getLastValidOdometerReading());

        if (lastKnownEvent.getDutyStatusEnum().getValue() == DutyStatusEnum.DRIVING || GlobalState.getInstance().getIsInPersonalConveyanceDrivingSegment() == true || GlobalState.getInstance().getIsInNonRegDrivingSegment() == true) {
            //if driving or in pc/non-reg driving segment
            newLocation.setGpsInfo(logEntryCtrlr.getLastValidGPSLocation());
        }

        // create a new log, for the event, initialized with the information from the last known event
        EmployeeLog newLog = EmployeeLogUtilities.CreateNewLog(this.getContext(), usr, timestamp, lastKnownEvent.getDutyStatusEnum(), newLocation, lastKnownEvent.getEventRecordOrigin());
        //Set flag denoting log is a transitional log.
        newLog.setIsTransitionalLog(true);
        // assign the mobile start timestamp to the start of the log
        newLog.setMobileStartTimestamp(EmployeeLogUtilities.CalculateLogStartTime(this.getContext(), newLog.getLogDate(), usr.getHomeTerminalTimeZone()));

        // transfer all outstanding failures from the old log to the new one
        FailureController failureCtrlr = new FailureController(this.getContext());
        failureCtrlr.TransferFailures(currentLog, newLog);

        // transfer trip info from old log to the new log
        newLog.setTractorNumbers(currentLog.getTractorNumbers());
        newLog.setTrailerNumbers(currentLog.getTrailerNumbers());
        newLog.setTrailerPlate(currentLog.getTrailerPlate());
        newLog.setShipmentInformation(currentLog.getShipmentInformation());
        newLog.setVehiclePlate(currentLog.getVehiclePlate());
        newLog.setExemptLogType(currentLog.getExemptLogType());


        //Set all events' EOBRSN to what it was previously
        for (EmployeeLogEldEvent evt : newLog.getEldEventList().getEldEventList()) {
            evt.setEobrSerialNumber(lastKnownEvent.getEobrSerialNumber());
        }

        // copy any team drivers from yesterdays log to the new log
        TeamDriverController teamDriverController = new TeamDriverController(this.getContext());
        teamDriverController.EndTeamDrivingOnTransition(usr, currentLog);
        teamDriverController.TransferTeamDrivers(usr, newLog);

        // persist the changes to the current day's log, just in case they have not been committed
        this.SaveLocalEmployeeLog(usr, currentLog);

        return newLog;
    }

    private void endSpecialDrivingForMidnightTransition(EmployeeLog currentLog, Date timestamp, Location location) {
        ISpecialDrivingController specialDrivingController = SpecialDrivingFactory.getControllerInDrivingSegment();
        if(specialDrivingController == null) {
            //under the mandate, for PC and YM we need to end the category at the end of the log
            //regardless of whether there's a driving segment
            ISpecialDrivingController pcController = SpecialDrivingFactory.getControllerForDrivingCategory(EmployeeLogProvisionTypeEnum.PERSONALCONVEYANCE);
            if(pcController.getIsInSpecialDutyStatus()) {
                specialDrivingController = pcController;
            }
        }

        if (specialDrivingController != null) {
            // we're in special mode, so end the current special driving segment
            // the end time of the special event will be the end of the current log
            specialDrivingController.EndSpecialDrivingStatus(timestamp, location, currentLog, true);

            // keep the special driving mode going since we're only transitioning across midnight
            specialDrivingController.setIsInSpecialDutyStatus(true);
            specialDrivingController.setIsInSpecialDrivingSegment(true);
        } else {
            EmployeeLogEldEvent lastSpecialDrivingStartEvent = EmployeeLogUtilities.GetLastEventInLog(
                    currentLog, com.jjkeller.kmbapi.employeelogeldevents.Enums.EmployeeLogEldEventType.ChangeInDriversIndication);

            if(lastSpecialDrivingStartEvent != null
                    && lastSpecialDrivingStartEvent.getEventCode() == EmployeeLogEldEventCode.ChangeInDriversIndication_YardMoves) {

                try {
                    GlobalState.getInstance().setIsEndingActivePCYMWT_Status(true);
                    CheckForAndCreateEndOfPCYMWT_Event(currentLog, timestamp, null, true);
                } catch(Throwable t) {
                    HandleException(new Exception("Error trying to create new yard move event during midnight transition.", t), "LogEntryController");
                }
            }
        }
    }

    /// <summary>
    /// Answer the local, unsubmitted, employee log for the current logged in
    /// user, for the specified date.
    /// If no log is found for the date, answer null.
    /// </summary>
    /// <param name="logDate">date to search for</param>
    /// <returns>EmployeeLog matching the date, otherwise returns null if not found</returns>
    @Override
    public EmployeeLog GetLocalEmployeeLog(Date logDate) {
        return this.GetLocalEmployeeLog(this.getCurrentUser(), logDate);
    }

    /// <summary>
    /// Answer the local, unsubmitted, employee log for the user,
    /// for the specified date.  If no log is found, answer null.
    /// </summary>
    /// <param name="user">user to look for logs</param>
    /// <param name="logDate">date to search for</param>
    /// <returns>EmployeeLog matching the date, otherwise returns null if not found</returns>
    @Override
    public EmployeeLog GetLocalEmployeeLog(User user, Date logDate) {
        EmployeeLog empLog = this.GetLocalLog(user, logDate);
        return empLog;
    }

    /// Answer the downloaded employee log for the specified date
    /// <param name="logDate">date to search for</param>
    /// <returns>EmployeeLog matching the date, otherwise returns null if not found</returns>
    @Override
    public EmployeeLog GetDownloadedEmployeeLog(Date logDate) {
        return this.GetDownloadedEmployeeLog(this.getCurrentUser(), logDate);
    }

    /// Answer the downloaded employee log for the specified date
    /// <param name="user">user to look for log</param>
    /// <param name="logDate">date to search for</param>
    /// <returns>EmployeeLog matching the date, otherwise returns null if not found</returns>
    @Override
    public EmployeeLog GetDownloadedEmployeeLog(User user, Date logDate) {
        EmployeeLog empLog = this.GetServerLog(user, logDate);
        return empLog;
    }

    /**
     * Apply a new ruleset to the entire log of the current user.
     * Each log event in the log will be changed to the new rulest.
     *
     * @param newRuleset
     */
    @Override
    public void ChangeRulesetOfEntireLog(Enums.RuleTypeEnum ruleTypeEnum, RuleSetTypeEnum newRuleset) {
        LogEntryController logEntryCtrlr = new LogEntryController(this.getContext());
        ChangeRulesetOfEntireLog(this.getCurrentUser(), logEntryCtrlr.getCurrentEmployeeLog(), ruleTypeEnum, newRuleset);

        // 8/8/11 JHM - Update rulesets when entire log is updated so future border crossings are accurate
        new EmployeeRuleController(getContext()).UpdateInternationalRulesetsForUser(getCurrentUser());
    }

    @Override
    public boolean IsUSOilFieldOffDutyStatusInLog() {
        LogEntryController logEntryCtrlr = new LogEntryController(this.getContext());
        EmployeeLogEldEvent[] eldEvents = logEntryCtrlr.getCurrentEmployeeLog().getEldEventList().getEldEventList(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus);

        return IsUSOilFieldOffDutyStatusInLog(eldEvents);
    }

    @Override
    public boolean IsUSOilFieldOffDutyStatusInLog(EmployeeLogEldEvent[] eldEvents) {
        boolean isUSOilfieldOffDutyStatus = false;

        for (EmployeeLogEldEvent logEvt : eldEvents) {
            if (logEvt.getDutyStatusEnum().getValue() == DutyStatusEnum.OFFDUTYWELLSITE) {
                isUSOilfieldOffDutyStatus = true;
                break;
            }
        }

        return isUSOilfieldOffDutyStatus;
    }

    @Override
    public boolean isRulesetCombinationAllowed(RuleSetTypeEnum newRuleset) {
        // If non-US 60/70 exists on log, don't allow user to switch to Canadian rulesets.
        // If Canadian exists on log, don't allow user to switch to non-US 60/70
        if (!this.getCurrentUser().IsCanadianRulesetAvailable()) {
            return true;
        }

        LogEntryController logEntryCtrlr = new LogEntryController(this.getContext());
        EmployeeLogEldEvent[] eldEvents = logEntryCtrlr.getCurrentEmployeeLog().getEldEventList().getEldEventList(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus);

        return isRulesetCombinationAllowed(eldEvents, newRuleset);
    }

    @Override
    public boolean isRulesetCombinationAllowed(EmployeeLogEldEvent[] eldEvents, RuleSetTypeEnum newRuleset) {
        boolean isRulesetCombinationAllowed = true;
        boolean isCanadianRulesetOnLog = false;
        boolean isStateRulesetOnLog = false;

        for (EmployeeLogEldEvent logEvt : eldEvents) {
            switch (logEvt.getRulesetType().getValue()) {
                case RuleSetTypeEnum.CANADIAN_CYCLE1:
                case RuleSetTypeEnum.CANADIAN_CYCLE2:
                    isCanadianRulesetOnLog = true;
                    break;
                case RuleSetTypeEnum.FLORIDA_7DAY:
                case RuleSetTypeEnum.FLORIDA_8DAY:
                case RuleSetTypeEnum.WISCONSIN_7DAY:
                case RuleSetTypeEnum.WISCONSIN_8DAY:
                case RuleSetTypeEnum.USCONSTRUCTION_7DAY:
                case RuleSetTypeEnum.USCONSTRUCTION_8DAY:
                case RuleSetTypeEnum.TEXAS:
                case RuleSetTypeEnum.USOILFIELD:
                    isStateRulesetOnLog = true;
                    break;
                default:
                    break;
            }
        }

        if (isCanadianRulesetOnLog) {
            switch (newRuleset.getValue()) {
                case RuleSetTypeEnum.ALASKA_7DAY:
                case RuleSetTypeEnum.ALASKA_8DAY:
                case RuleSetTypeEnum.US60HOUR:
                case RuleSetTypeEnum.US70HOUR:
                case RuleSetTypeEnum.USMOTIONPICTURE_7DAY:
                case RuleSetTypeEnum.USMOTIONPICTURE_8DAY:
                case RuleSetTypeEnum.CANADIAN_CYCLE1:
                case RuleSetTypeEnum.CANADIAN_CYCLE2:
                    isRulesetCombinationAllowed = true;
                    break;
                default:
                    isRulesetCombinationAllowed = false;
                    break;
            }
        } else if (isStateRulesetOnLog) {
            switch (newRuleset.getValue()) {
                case RuleSetTypeEnum.CANADIAN_CYCLE1:
                case RuleSetTypeEnum.CANADIAN_CYCLE2:
                    isRulesetCombinationAllowed = false;
                    break;
                default:
                    isRulesetCombinationAllowed = true;
                    break;
            }
        }

        return isRulesetCombinationAllowed;

    }

    /// <summary>
    /// Apply a new ruleset to the entire log.  Each log event in the log will be
    /// changed to the new rulest.
    /// </summary>
    /// <param name="user"></param>
    /// <param name="empLog"></param>
    /// <param name="newRuleset"></param>
    @Override
    public void ChangeRulesetOfEntireLog(User user, EmployeeLog empLog, Enums.RuleTypeEnum ruleTypeEnum, RuleSetTypeEnum newRuleset) {
        if (newRuleset.getValue() == RuleSetTypeEnum.NULL) {
            return;
        }

        // Change current employee/log ruleset only if the user selected to change the entire log
        // or the ruleset type(US OR CAN) that matches the current ruleset type of the log.
        if ((ruleTypeEnum == Enums.RuleTypeEnum.CDOnly && empLog.getRuleset().isCanadianRuleset()) || (ruleTypeEnum == Enums.RuleTypeEnum.USOnly && !empLog.getRuleset().isCanadianRuleset() || ruleTypeEnum == Enums.RuleTypeEnum.Both)) {
            user.setRulesetTypeEnum(newRuleset);

            // if switching from oil field ruleset, turn off oilfield specific vehicle flag on current log
            if (empLog.getRuleset().isAnyOilFieldRuleset() && !newRuleset.isAnyOilFieldRuleset()) {
                empLog.setIsOperatesSpecificVehiclesForOilfield(false);
            }
            // if switching to oil field ruleset, set oilfield specific vehicle flag based on user setting (from employee rule)
            else if (!empLog.getRuleset().isAnyOilFieldRuleset() && newRuleset.isAnyOilFieldRuleset()) {
                empLog.setIsOperatesSpecificVehiclesForOilfield(user.getIsOperatesSpecificVehiclesForOilfield());
            }

            empLog.setRuleset(newRuleset);
        }

        for (EmployeeLogEldEvent logEvt : empLog.getEldEventList().getEldEventList(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus)) {
            if (ruleTypeEnum == Enums.RuleTypeEnum.CDOnly && logEvt.getRulesetType().isCanadianRuleset()) {
                logEvt.setRulesetType(newRuleset);
            } else if (ruleTypeEnum == Enums.RuleTypeEnum.USOnly && !logEvt.getRulesetType().isCanadianRuleset()) {
                logEvt.setRulesetType(newRuleset);
            } else if (ruleTypeEnum == Enums.RuleTypeEnum.Both) {
                logEvt.setRulesetType(newRuleset);
            }
        }

        // perform audit - if switch to/from oil field, need to remove/calc weekly reset
        HosAuditController controller = new HosAuditController(getContext());
        controller.UpdateForCurrentLogEvent();

        this.SaveLocalEmployeeLog(user, empLog);
    }

    /// <summary>
    /// Save the trip info
    /// </summary>
    /// <param name="tractorNumbers"></param>
    /// <param name="trailerNumbers"></param>
    /// <param name="shipmentInfo"></param>
    @Override
    public void SaveTripInfo(String trailerNumbers, String trailerPlate, String shipmentInfo, String vehiclePlate, boolean returnToWorkLocation, CanadaDeferralTypeEnum canadaDeferralType, boolean isHaulingExplosives, boolean isOperatesSpecificVehicleForOilField, boolean is30MinBreakExempt, String tractorNumbers, String authorityId, String productionId) {
        LogEntryController ctrlr = new LogEntryController(this.getContext());
        EmployeeLog currentLog = ctrlr.getCurrentEmployeeLog();

        // save the info into the current log
        currentLog.setTrailerNumbers(trailerNumbers);
        currentLog.setTrailerPlate(trailerPlate);
        currentLog.setShipmentInformation(shipmentInfo);
        currentLog.setVehiclePlate(vehiclePlate);
        currentLog.setHasReturnedToLocation(returnToWorkLocation);
        currentLog.setCanadaDeferralType(canadaDeferralType);
        currentLog.setIsHaulingExplosives(isHaulingExplosives);
        currentLog.setIsOperatesSpecificVehiclesForOilfield(isOperatesSpecificVehicleForOilField);
        currentLog.setIsExemptFrom30MinBreakRequirement(is30MinBreakExempt);
        currentLog.setTractorNumbers(tractorNumbers);

        // Only update the current duty status event when motion picture data is supplied
        if (authorityId != null && productionId != null) {
            EmployeeLogEldEvent e = EmployeeLogUtilities.GetLogEventForEdit(currentLog, true);
            if (!e.isDrivingEvent()) {
                e.setMotionPictureAuthorityId(authorityId);
                e.setMotionPictureProductionId(productionId);
            }
        }

        // save the log
        ctrlr.setCurrentEmployeeLog(currentLog);
        this.SaveLocalEmployeeLog(currentLog);

        if (currentLog.getExemptLogType().getValue() != ExemptLogTypeEnum.NULL && currentLog.getHasReturnedToLocation() == false) {
            // for exempt log, when return to work location gets turns off, it invalidates the log
            ExemptLogValidationController exemptLogValidationCtrlr = new ExemptLogValidationController(this.getContext());
            exemptLogValidationCtrlr.PerformCompleteValidationForCurrentLog(currentLog, false);
        }
    }

    /// <summary>
    /// Save the weekly reset field on the log
    /// </summary>
    /// <param name="tractorNumbers"></param>
    /// <param name="trailerNumbers"></param>
    /// <param name="shipmentInfo"></param>
    @Override
    public void SaveWeeklyResetUsed(boolean isWeeklyResetUsed) {
        LogEntryController ctrlr = new LogEntryController(this.getContext());
        EmployeeLog currentLog = ctrlr.getCurrentEmployeeLog();

        // save the info into the current log
        currentLog.setIsWeeklyResetUsed(isWeeklyResetUsed);
        currentLog.setIsWeeklyResetUsedOverridden(!isWeeklyResetUsed);

        // save the log
        ctrlr.setCurrentEmployeeLog(currentLog);
        this.SaveLocalEmployeeLog(currentLog);
    }

    /// <summary>
    /// Answer if it is valid to use the weekly reset on the current log.
    /// The reset is valid to use as long as another one does not exist 168 hours ago.
    /// </summary>
    @Override
    public boolean IsValidToUseWeeklyResetOnCurrentLog() {
        boolean answer = false;
        LogEntryController ctrlr = new LogEntryController(this.getContext());
        EmployeeLog currentLog = ctrlr.getCurrentEmployeeLog();
        if (currentLog.getWeeklyResetStartTimestamp() != null) {

            Date previousWeeklyResetTimestamp = this.GetPreviousWeeklyResetStartTimestamp(currentLog.getLogDate());
            if (previousWeeklyResetTimestamp == null || (previousWeeklyResetTimestamp.compareTo(currentLog.getWeeklyResetStartTimestamp()) == 0))
            // either there is not previous reset, or the reset is the same as the current log's reset
            {
                answer = true;
            } else {
                // is the previous weekly reset more than 168 hours prior to the current one
                long diff = currentLog.getWeeklyResetStartTimestamp().getTime() - previousWeeklyResetTimestamp.getTime();
                if (diff >= 168 * DateUtility.MILLISECONDS_PER_HOUR) {
                    answer = true;
                }
            }
        }

        return answer;
    }

    @Override
    public Date GetPreviousWeeklyResetStartTimestamp(Date logDate) {
        IEmployeeLogFacade facade = new EmployeeLogFacade(this.getContext(), this.getCurrentUser());
        return facade.FetchPreviousWeeklyResetStartTimestamp(logDate);
    }

    @Override
    public boolean DoesWeeklyResetExistNewerThan(Date logDate) {
        IEmployeeLogFacade facade = new EmployeeLogFacade(this.getContext(), this.getCurrentUser());
        return facade.DoesWeeklyResetExistNewerThan(logDate);
    }

    /// <summary>
    /// Create an off-duty log for the logDate.
    /// </summary>
    @Override
    public void CreateOffDutyLog(Date logDate) {
        EmployeeLog offDutyLog = EmployeeLogUtilities.CreateNewLog(this.getContext(), this.getCurrentUser(), logDate, new DutyStatusEnum(DutyStatusEnum.OFFDUTY), null);
        this.SaveLocalEmployeeLog(offDutyLog);
    }

    /**
     * Perform all the actions surrounding the submission of records to DMO.
     * After all records have been submitted, then attempt to download the logs from DMO so that the most accurate copy is available.
     * At the end, try to purge anything is old from the database.
     * Returns true if the records were successfully submitted and false otherwise.
     *
     * @param submittingUser   the user to submit records for
     * @param excludeTodaysLog if true, records for today's log won't be submitted
     * @return true if the records were successfully submitted and false otherwise
     */
    @Override
    public boolean SubmitAllRecords(User submittingUser, boolean excludeTodaysLog) {
        boolean isSuccessful = true;

        // check if need to transition across midnight before submitting
        LogEntryController logEntryController = new LogEntryController(getContext());
        Date now = logEntryController.getCurrentClockHomeTerminalTime();
        logEntryController.CreateNewLogIfNecessary(submittingUser, logEntryController.getCurrentEmployeeLog(), now);

        // If no other users are logged in, submit all logs that are local.
        // If other users are still logged in, only submit for the current.
        boolean logsSubmitted;
        if (this.getLoggedInUserList().size() > 0) {
            // 2015.06.15 sjn - for team driver, the User Contenxt has already been switched to the remaining user
            // first, need to change the home terminal timezone before submitting logs so that the timestamps of all events
            // are in the correct timezone
            DateUtility.setHomeTerminalTimeDateFormatTimeZone(submittingUser.getHomeTerminalTimeZone().toTimeZone());

            // Submit only user's local logs.
            logsSubmitted = this.SubmitUsersLocalLogs(submittingUser, excludeTodaysLog);

            // 2015.06.15 sjn - now set the home terminal TZ back again
            DateUtility.setHomeTerminalTimeDateFormatTimeZone(this.getCurrentUser().getHomeTerminalTimeZone().toTimeZone());

        } else {
            // Upload all of the local logs to DMO
            logsSubmitted = this.SubmitAllLocalLogs(excludeTodaysLog);
        }
        if (!logsSubmitted) {
            isSuccessful = false;
        }

        boolean unidentifiedEldEventsSubmitted = this.SubmitUnidentifiedEldEvents();
        if (!unidentifiedEldEventsSubmitted) {
            isSuccessful = false;
        }

        // Upload all unassigned periods (both driving and failure) to DMO
        UnassignedPeriodController unassignedController = ControllerFactory.getInstance().getUnassignedPeriodController();
        boolean unassignedPeriodsSubmitted = unassignedController.SubmitUnassignedPeriodsToDMO(excludeTodaysLog);
        if (!unassignedPeriodsSubmitted) {
            isSuccessful = false;
        }

        // Upload all unsubmitted Route Positions to DMO
        RouteController routeController = new RouteController(this.getContext());
        boolean routePositionsSubmitted = routeController.SubmitRoutePositionsToDMO();
        if (!routePositionsSubmitted) {
            isSuccessful = false;
        }

        if (!SubmitFuelPurchases()) {
            isSuccessful = false;
        }
        if (!SubmitEventDataRecords()) {
            isSuccessful = false;
        }
        if (!SubmitTripRecords()) {
            isSuccessful = false;
        }
        if (!SubmitEngineRecords()) {
            isSuccessful = false;
        }

        // Upload all unsubmitted Eobr Configurations to DMO
        EobrConfigController eobrConfigController = new EobrConfigController(this.getContext());
        boolean eobrConfigSubmitted = eobrConfigController.SubmitEobrConfigurationsToDMO();
        if (!eobrConfigSubmitted) {
            isSuccessful = false;
        }

        // Upload all unsubmitted DVIRs to DMO
        VehicleInspectionController vehicleInspectionController = new VehicleInspectionController(this.getContext());
        vehicleInspectionController.SubmitAllInspectionsToDMO();

        for (ISpecialDrivingController controller : SpecialDrivingFactory.getAllControllers()) {
            boolean submitted = controller.SubmitAllSpecialDrivingItemsToDMO();
            if (!submitted) {
                isSuccessful = false;
            }
        }

        // Upload all unsubmitted EmployeeLogRevisions to DMO
        EmployeeLogRevisionController empLogRevCtrlr = new EmployeeLogRevisionController(this.getContext());
        boolean empLogRevSubmitted = empLogRevCtrlr.SubmitAllEmployeeLogRevisionItemsToDMO();
        if (!empLogRevSubmitted) {
            isSuccessful = false;
        }

        try {
            // Download all records to be in compliance
            this.DownloadRecordsForCompliance(submittingUser, true);
        } catch (JsonParseException e) {
            isSuccessful = false;
            Log.e("SubmitAllRecords Json", e.getMessage() + ": " + Log.getStackTraceString(e));
        } catch (KmbApplicationException kae) {
            Log.e("UnhandledCatch", kae.getMessage() + ": " + Log.getStackTraceString(kae));

            // shouldn't ever get an error here - exceptions will be ignored since
            // the parameter to the DownloadRecordsForCompliance is set to true
        }

        if (isSuccessful) {
            // since the submit was completely successful,
            // then update the submit timestamp to the current UTC time
            UserFacade facade = new UserFacade(getContext(), submittingUser);
            LoginCredentials cred = submittingUser.getCredentials();
            cred.setLastSubmitTimestampUtc(DateUtility.getCurrentDateTimeWithSecondsUTC());
            facade.Save(cred);
        }

        // TODO now purge everything that is old
        this.AutoPurgeOldRecords(submittingUser);

        return isSuccessful;
    }

    private boolean SubmitFuelPurchases() {
        boolean isSuccessful;
        switch (this.getCurrentDesignatedDriver().getDataProfile().getValue()) {
            case DataProfileEnum.MINIMUMHOS:
            case DataProfileEnum.MINIMUMHOSWITHGPS:
                isSuccessful = true;
                break;
            default:
                // Upload all unsubmitted Fuel Purchases to DMO
                FuelPurchaseController fuelPurchController = new FuelPurchaseController(this.getContext());
                isSuccessful = fuelPurchController.SubmitFuelPurchasesToDMO();
                break;
        }
        return isSuccessful;
    }

    // Gen II Only
    private boolean SubmitEventDataRecords() {
        boolean isSuccessful;
        switch (this.getCurrentDesignatedDriver().getDataProfile().getValue()) {
            case DataProfileEnum.MINIMUMHOS:
            case DataProfileEnum.MINIMUMHOSWITHFUELTAX:
                isSuccessful = true;
                break;
            default:
                // Upload all unsubmitted Event Data Records to DMO
                EngineRecordController engineRecordController = new EngineRecordController(this.getContext(), GlobalState.getInstance().getAppSettings(this.getContext()));
                isSuccessful = engineRecordController.SubmitEventDataRecordsToDMO();
                break;
        }
        return isSuccessful;
    }

    // Gen II Only
    private boolean SubmitTripRecords() {
        boolean isSuccessful;
        switch (this.getCurrentDesignatedDriver().getDataProfile().getValue()) {
            case DataProfileEnum.MINIMUMHOS:
            case DataProfileEnum.MINIMUMHOSWITHGPS:
            case DataProfileEnum.MINIMUMHOSWITHFUELTAX:
            case DataProfileEnum.MINIMUMHOSWITHFUELTAXANDGPS:
                isSuccessful = true;
                break;

            default:
                //Upload all unsubmitted trip records to DMO
                TripRecordController tripRecordController = new TripRecordController(this.getContext());
                isSuccessful = tripRecordController.SubmitTripRecordsToDMO();
                break;
        }
        return isSuccessful;
    }

    // Gen I Only
    private boolean SubmitEngineRecords() {
        boolean isSuccessful;
        switch (this.getCurrentDesignatedDriver().getDataProfile().getValue()) {
            case DataProfileEnum.MINIMUMHOS:
            case DataProfileEnum.MINIMUMHOSWITHGPS:
            case DataProfileEnum.MINIMUMHOSWITHFUELTAX:
            case DataProfileEnum.MINIMUMHOSWITHFUELTAXANDGPS:
                isSuccessful = true;
                break;

            default:
                // Upload all unsubmitted Engine Records to DMO
                EngineRecordController engineRecordController = new EngineRecordController(this.getContext(), GlobalState.getInstance().getAppSettings(this.getContext()));
                isSuccessful = engineRecordController.SubmitEngineRecordsToDMO();
                break;
        }
        return isSuccessful;
    }

    /// <summary>
    /// Submits only the local logs that are present in the list of dates that is passed in for a single user, to DMO.
    /// Return TRUE if all of the logs were successfully submitted to DMO.
    /// </summary>
    /// <param name="logDatesToSubmit"></param>
    @Override
    public boolean SubmitUsersLocalLogs(User user, List<Date> logDatesToSubmit) {
        boolean isSuccessful = true;

        // fetch the list of all local logs for the employee
        IEmployeeLogFacade facade = new EmployeeLogFacade(this.getContext(), user);
        List<EmployeeLog> localLogList = facade.GetLocalLogList(false);

        List<EmployeeLog> localLogsToSubmit = new ArrayList<EmployeeLog>();

        // loop through the log dates that are requested to be submitted, and find the corresponding log. If the log is found, add it to the localLogsToSubmit list
        for (Date date : logDatesToSubmit) {
            for (EmployeeLog log : localLogList) {
                if (log.getLogDate().equals(date)) {
                    // found log!
                    localLogsToSubmit.add(log);
                    break;
                }
            }
        }

        if (localLogsToSubmit.size() > 0) {
            // submit the local logs
            boolean submittedToDMOSuccessfully = this.SubmitLocalLogsToDMO(localLogsToSubmit);
            if (!submittedToDMOSuccessfully) {
                isSuccessful = false;
            }
        }

        // reset the webservice back to it's original, logged in, employeeId
        GlobalState.getInstance().getKmbUserInfo().setDmoEmployeeId(this.getCurrentUser().getCredentials().getEmployeeId());

        return isSuccessful;
    }

    @Override
    public boolean SubmitMobileDeviceInfo() {
        boolean isSuccessful = false;

        MobileDevice mobileInfo = MobileDevice.forCurrentDevice(GlobalState.getInstance());

        // Get the last EventDataRecord by the EmployeeId
        EventDataRecordFacade facade = new EventDataRecordFacade(getContext(), this.getCurrentUser());
        EventDataRecord event = facade.FetchMostRecent();
        if (event != null) {
            mobileInfo.setLastKMBEOBRDeviceSerialNumber(event.getEobrSerialNumber());
        }

        try {
            RESTWebServiceHelper rwsh = new RESTWebServiceHelper(getContext());
            MobileDevice result = rwsh.SubmitMobileDevice(mobileInfo);
            if (result != null) {
                GlobalState.getInstance().setAlkCoPilotActivated(result.isCoPilot());
            }
            isSuccessful = true;
        } catch (IOException | JsonParseException e) {
            this.HandleException(e, this.getContext().getString(R.string.uploadmobiledeviceinfo));
        }

        return isSuccessful;
    }

    /// <summary>
    /// Update the trip info's tractor number with the unit Id reported from the EOBR.
    /// This will happen when there is a transition to driving on the log.
    /// </summary>
    /// <param name="unitId"></param>
    /// <param name="empLog"></param>
    @Override
    public void UpdateTractorNumber(String unitId, EmployeeLog empLog) {
        if (unitId == null || unitId.length() == 0) {
            return;
        }

        if (empLog.getTractorNumbers() == null || empLog.getTractorNumbers().equals("")) {
            // this is the first unit assigned to the log
            empLog.setTractorNumbers(unitId);

            // save the changes to the log
            this.SaveLocalEmployeeLog(empLog);
        } else if (!GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
            // this log has multiple units assigned to the tractor
            // append this unitId to the end, as long as it's not already in there
            if (empLog.getTractorNumbers().indexOf(unitId) < 0) {
                StringBuilder additionalTractorId = new StringBuilder(empLog.getTractorNumbers());
                additionalTractorId.append(", ");
                additionalTractorId.append(unitId);
                empLog.setTractorNumbers(additionalTractorId.toString());

                // save the changes to the log
                this.SaveLocalEmployeeLog(empLog);
            }
        }
    }

    @Override
    public void ReverseGeocodeLocation(EmployeeLog empLog, GpsLocation gpsLocation) {
        // if a valid lcoation is specified that hasn't been decoded yet, decode it
        if (gpsLocation != null && !gpsLocation.IsEmpty() && gpsLocation.getDecodedInfo().IsEmpty()) {
            // If reverse geocoding from local DB or network is available, attempt to reverse geocode
            if (GlobalState.getInstance().getAppSettings(this.getContext()).getReverseGeocodeFromLocalDB() || GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled() || this.getIsWebServicesAvailable()) {
                // the network is available and this point has not been decoded yet
                List<GpsLocation> listToDecode = new ArrayList<GpsLocation>();
                listToDecode.add(gpsLocation);

                // try to find additional locations on the log that may need decoding
                for (EmployeeLogEldEvent logEvt : empLog.getEldEventList().getEldEventList()) {
                    GpsLocation loc = logEvt.getLocation().getGpsInfo();
                    if (loc != null && !loc.IsEmpty() && loc.getDecodedInfo().IsEmpty()) {
                        // found a GPS location that is valid, but has not yet been decoded
                        // add it to the list
                        listToDecode.add(loc);
                    }
                }

                // decode all of the events that need decoding
                this.ReverseGeocodeLocation(listToDecode);
            }
        }
    }

    /**
     * Asynchronously reverse geocode a location.
     * The listener will be notified when it completes.
     *
     * @param empLog
     * @param gpsLocation The {@link GpsLocation} to be reverse geocoded
     * @param listener    The listener to notify when complete
     */
    @Override
    public void ReverseGeocodeLocationAsync(EmployeeLog empLog, GpsLocation gpsLocation, IReverseGeocodeLocationListener listener) {
        EmployeeLogUtilities.executeNewReverseGeocodeTaskAsync(getContext(), empLog, gpsLocation, listener);
    }

    @Override
    public void ReverseGeocodeLocation(List<GpsLocation> gpsLocations) {
        // if using local db to reverse geocode, build list of closest matches
        // from the db and then find the closest match
        if (GlobalState.getInstance().getAppSettings(this.getContext()).getReverseGeocodeFromLocalDB() || GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
            LocationDBFacade locationDBFacade = new LocationDBFacade(this.getContext());

            for (GpsLocation gpsLoc : gpsLocations) {
                List<LocationDBAdapter.LocationDBLocation> dbLocationList = locationDBFacade.FetchList(gpsLoc.getLatitudeDegrees(), gpsLoc.getLongitudeDegrees());

                this.ReverseGeocodeFromLocationDB(gpsLoc, dbLocationList);
            }
        }
        // else, reverse geocode through web services
        else {
            if (this.getIsWebServicesAvailable() &&
                    gpsLocations != null &&
                    gpsLocations.size() > 0) {
                // the network is available and the point has not been decoded yet
                GpsLocation[] locationsToSend = gpsLocations.toArray(new GpsLocation[gpsLocations.size()]);

                ArrayList<GpsLocation> locationArray = new ArrayList<GpsLocation>();

                try {
                    RESTWebServiceHelper rwsh = new RESTWebServiceHelper(getContext());
                    locationArray = rwsh.ReverseGeoCode(locationsToSend);
                } catch (JsonSyntaxException e) {
                    this.HandleException(e, this.getContext().getString(R.string.reversegeocodelocation));
                } catch (JsonParseException e) {
                    // when connected to a network, but unable to get to webservice "e" is null
                    if (e == null) {
                        e = new JsonParseException(JsonParseException.class.getName());
                    }
                    this.HandleException(e, this.getContext().getString(R.string.reversegeocodelocation));
                } catch (IOException e) {
                    this.HandleException(e, this.getContext().getString(R.string.reversegeocodelocation));
                }

                for (int index = 0; index < gpsLocations.size(); index++) {
                    GpsLocation inboundLoc = gpsLocations.get(index);
                    if (locationArray.size() > index) {
                        GpsLocation convertedLoc = locationArray.get(index);
                        inboundLoc.setDecodedInfo(convertedLoc.getDecodedInfo());
                    }
                }
            }
        }
    }

    /// <summary>
    /// Valiated that every unsubmitted log event has been reverse geocoded.
    /// If any events need geocoding, then submit to DMO for processing.
    /// </summary>
    @Override
    public void ValidateAllReverseGeocoding() {
        if (this.getIsNetworkAvailable()) {
            // fetch a list of all events that need to be geocoded
            IEmployeeLogFacade empLogFacade = new EmployeeLogFacade(getContext(), getCurrentUser());

            List<EmployeeLogEldEvent> logEvents = empLogFacade.FetchUnsubmittedLocationsThatRequireGeocoding();
            if (logEvents != null && logEvents.size() > 0) {
                // there are some locations that need to be decoded
                ArrayList<GpsLocation> listToDecode = new ArrayList<GpsLocation>();

                for (EmployeeLogEldEvent logEvt : logEvents) {
                    // find locations in the events that have valid GPS
                    GpsLocation loc = logEvt.getLocation().getGpsInfo();
                    if (loc != null && !loc.IsEmpty() && loc.getDecodedInfo().IsEmpty()) {
                        // found a GPS location that is valid, but has not yet been decoded
                        // add it to the list
                        listToDecode.add(loc);
                    }
                }

                // perform the actual decoding
                this.ReverseGeocodeLocation(listToDecode);

                // save the logEvents back into the db
                empLogFacade.UpdateGeocodedLocations(logEvents);
            }
        }
    }

    @Override
    public EmployeeLog getSelectedLogForReport() {

        EmployeeLog empLog = GlobalState.getInstance().getSelectedLogForReport();
        return empLog;
    }

    @Override
    public void setSelectedLogForReport(EmployeeLog empLog) {
        GlobalState.getInstance().setSelectedLogForReport(empLog);
    }

    /// <summary>
    /// Answer the complete list of team drivers on the current log.
    /// The list will be sorted ascending on the start time.
    /// </summary>
    /// <returns></returns>
    @Override
    public List<TeamDriver> getTeamDriversOnLog(EmployeeLog empLog) {
        List<TeamDriver> list = new ArrayList<TeamDriver>();
        if (empLog.getTeamDriverList().getTeamDriverList().length > 0) {
//            list.addAll(empLog.getTeamDriverList().getTeamDriverList());
            int i = 0;
            do {
                list.add(empLog.getTeamDriverList().getTeamDriverList()[i]);
                i++;
            } while (i < empLog.getTeamDriverList().getTeamDriverList().length);

            // sort the list ascending on the start time
            TeamDriverComparer teamDriverComparer = new TeamDriverComparer("Ascending");
            Collections.sort(list, teamDriverComparer);
        }

        return list;
    }

    /// <summary>
    /// Answer the failures on the specified log for use on
    /// the Failure Report.   The list of failures should be reported
    /// in the Home Terminal timezone of the user.   Also the list should
    /// be sorted ascending on the start time of the failures.
    /// </summary>
    /// <returns></returns>
    @Override
    public List<FailureReport> getFailuresForDutyFailureReport(EmployeeLog empLog) {
        List<FailureReport> reportList = new ArrayList<FailureReport>();
        FailureReport report = null;

        if (empLog.getTimeSyncFailureList() != null && empLog.getTimeSyncFailureList().getFailureReportList() != null) {
            for (FailureReport failureReport : empLog.getTimeSyncFailureList().getFailureReportList()) {
                report = new FailureReport();

                report.setCategory(failureReport.getCategory());
                report.setStartTime(failureReport.getStartTime());
                if (failureReport.getStopTime() != null) {
                    report.setStopTime(failureReport.getStopTime());
                }
                report.setMessage(failureReport.getMessage());

                reportList.add(failureReport);

            }
        }

        if (empLog.getEobrFailureList() != null && empLog.getEobrFailureList().getFailureReportList() != null) {
            // add the eobr device failures
            for (FailureReport failureReport : empLog.getEobrFailureList().getFailureReportList()) {
                report = new FailureReport();

                report.setCategory(failureReport.getCategory());
                report.setStartTime(failureReport.getStartTime());
                if (failureReport.getStopTime() != null) {
                    report.setStopTime(failureReport.getStopTime());
                }
                report.setMessage(failureReport.getMessage());

                reportList.add(report);
            }
        }

        if (!reportList.isEmpty()) {
            // sort the list of failures ascending on the start time
            FailureReportComparer failureReportComparer = new FailureReportComparer("Ascending");
            Collections.sort(reportList, failureReportComparer);
        }

        return reportList;
    }

    @Override
    public List<Date> GetLogDateList() {
        IEmployeeLogFacade facade = new EmployeeLogFacade(this.getContext(), this.getCurrentUser());
        return facade.FetchLogList(null, null);
    }

    @Override
    public Date GetFirstAvailableLogDate() {
        IEmployeeLogFacade facade = new EmployeeLogFacade(this.getContext(), this.getCurrentUser());
        return facade.GetFirstAvailableLogDate();
    }

    @Override
    public List<Date> GetLogDateListForReport() {
        Date cutoffDate = null;
        LogEntryController cntrlr = new LogEntryController(getContext());

        if (cntrlr.getRoadsideInspectionMode() || GlobalState.getInstance().getIsViewOnlyMode()) {
            // When the app is in 'road-side inspection' mode or 'view-only' mode, then
            // only show 8 totals days appropriate worth of logs.
            // Remove all logs from the list that are older than that.
            cutoffDate = DateUtility.CurrentHomeTerminalTime(getCurrentUser());
            // Get date portion only - Remove time from home terminal time
            cutoffDate = EmployeeLogUtilities.CalculateLogStartTime(this.getContext(), cutoffDate, this.getCurrentUser().getHomeTerminalTimeZone());

            boolean isCanadianRuleSet = false;

            //if we're not in view only mode, or if there's currently a log in state that we can work with
            if (!GlobalState.getInstance().getIsViewOnlyMode() || GlobalState.getInstance().getCurrentEmployeeLog() != null) {
                // get current event values
                isCanadianRuleSet = cntrlr.GetCurrentEventValues_RulesetType().isCanadianRuleset();
            } else {
                //otherwise, if in view only mode this method is called as part of trying to figure out what log we're working
                //with, so just check to see if the user has a Canadian ruleset available to them so we can look back 14 days

                for (RuleSetTypeEnum ruleset : GlobalState.getInstance().getCurrentUser().getAvailableRulesets()) {
                    if (ruleset.isCanadianRuleset()) {
                        isCanadianRuleSet = true;
                        break;
                    }
                }
            }

            // if current event is for CD rule set, cutoff day is 14 days previous, for all other rule sets
            // cutoff day is 7 days previous, making for 8 total days of logs
            if (isCanadianRuleSet) {
                cutoffDate = DateUtility.AddDays(cutoffDate, -14);
            } else {
                cutoffDate = DateUtility.AddDays(cutoffDate, -7);
            }
        }

        Date endDate = (cutoffDate == null) ? null : TimeKeeper.getInstance().now();

        IEmployeeLogFacade facade = new EmployeeLogFacade(this.getContext(), this.getCurrentUser());
        return facade.FetchLogList(cutoffDate, endDate);
    }

    /**
     * Gets all log dates that either have no ELD events or the last ELD event is not a certification event.
     * If none exist, an empty list is returned.
     *
     * @return A list of log dates that have not been certified. If none exist, an empty list is returned.
     */
    @Override
    public List<Date> GetUncertifiedLogDates() {
        IEmployeeLogFacade facade = new EmployeeLogFacade(this.getContext(), this.getCurrentUser());
        return facade.FetchUncertifiedLogDates();
    }

    /**
     * Gets all log dates that have been certified but not submitted.
     * If none exist, an empty list is returned.
     *
     * @return A list of log dates that have been certified but are unsubmitted. If none exist, an empty list is returned.
     */
    @Override
    public List<Date> GetCertifiedUnsubmittedLogDates() {
        IEmployeeLogFacade facade = new EmployeeLogFacade(this.getContext(), this.getCurrentUser());
        return facade.FetchCertifiedUnsubmittedLogDates();
    }

    /**
     * Gets all log dates that either have no ELD events or the last ELD event is not a certification event.
     * If none exist, an empty list is returned.
     *
     * @return A list of log dates that have not been certified. If none exist, an empty list is returned.
     */
    @Override
    public List<Date> GetUncertifiedLogDatesExceptToday(User driver) {
        IEmployeeLogFacade facade = new EmployeeLogFacade(this.getContext(), driver);
        List<Date> logDates = facade.FetchUncertifiedLogDates();
        List<Date> logDatesExcludingToday = new ArrayList<>();
        for (Date date : logDates) {
            if (!DateUtility.IsToday(date, driver)) {
                logDatesExcludingToday.add(date);
            }
        }
        return logDatesExcludingToday;
    }

    @Override
    public List<EmployeeLog> GetLogsWithUnreviewedEdits(User driver) {
        IEmployeeLogFacade facade = new EmployeeLogFacade(this.getContext(), driver);
        return facade.FetchLogsWithUnreviewedEdits();
    }

    /// <summary>
    /// Answer a list of all Location Codes for the report.
    /// </summary>
    /// <returns></returns>
    @Override
    public List<LocationCode> getLocationCodeListForReport() {
        LocationCodeController ctrlr = new LocationCodeController(this.getContext());
        LocationCodeDictionary dict = ctrlr.getLocationCodes();

        LocationCodeComparer comparer = new LocationCodeComparer(this.getContext().getString(R.string.ascending));

        List<LocationCode> locationList = dict.AllCodes();
        Collections.sort(locationList, comparer);

        return locationList;
    }

    @Override
    public LogGridSummary GetLogGridSummary(EmployeeLog log) {
        LogGridSummary summary = new LogGridSummary();

        Date currentHomeTerminalTime = getCurrentClockHomeTerminalTime();
        // If calculating for a past log, ignore the current clock time
        if (new LogEntryController(getContext()).BelongOnFutureLog(this.getCurrentUser(), log, currentHomeTerminalTime)) {
            currentHomeTerminalTime = null;
        }

        TimeUnit timeUnit = TimeUnit.MINUTES;
        if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
            timeUnit = TimeUnit.MILLISECONDS;
        }

        summary.setOffDutyMinutesTotal((int) EmployeeLogUtilities.CalculateLogEventTotal(log, new DutyStatusEnum(DutyStatusEnum.OFFDUTY), currentHomeTerminalTime, timeUnit));
        summary.setSleeperMinutesTotal((int) EmployeeLogUtilities.CalculateLogEventTotal(log, new DutyStatusEnum(DutyStatusEnum.SLEEPER), currentHomeTerminalTime, timeUnit));
        summary.setDrivingMinutesTotal((int) EmployeeLogUtilities.CalculateLogEventTotal(log, new DutyStatusEnum(DutyStatusEnum.DRIVING), currentHomeTerminalTime, timeUnit));
        summary.setOnDutyMinutesTotal((int) EmployeeLogUtilities.CalculateLogEventTotal(log, new DutyStatusEnum(DutyStatusEnum.ONDUTY), currentHomeTerminalTime, timeUnit));
        summary.setOffDutyWellsiteMinutesTotal((int) EmployeeLogUtilities.CalculateLogEventTotal(log, new DutyStatusEnum(DutyStatusEnum.OFFDUTYWELLSITE), currentHomeTerminalTime, timeUnit));

        return summary;
    }

    /// <summary>
    /// Purge all old records from the database.
    /// </summary>
    @Override
    public void AutoPurgeOldRecords(User user) {
        int dayCount = GlobalState.getInstance().getCompanyConfigSettings(this.getContext()).getLogPurgeDayCount();

        Date currentHomeTerminalTime = DateUtility.CurrentHomeTerminalTime(user);
        Date todaysDate = new Date(currentHomeTerminalTime.getYear(), currentHomeTerminalTime.getMonth(), currentHomeTerminalTime.getDate());
        Date cutoffDate = DateUtility.AddDays(todaysDate, -dayCount);

        // purge old logs
        IEmployeeLogFacade logFacade = new EmployeeLogFacade(this.getContext(), user);
        logFacade.PurgeOldRecords(cutoffDate);

        EmployeeLogEldEventFacade eldEventFacade = new EmployeeLogEldEventFacade(getContext(), user);
        eldEventFacade.PurgeUnidentifiedEvents(cutoffDate);

        // purge submitted route positions that are old
        RoutePositionFacade routeFacade = new RoutePositionFacade(this.getContext(), user);
        routeFacade.PurgeOldRecords(cutoffDate);

        // purge submitted route positions that are old
        FuelPurchaseFacade fuelPurchaseFacade = new FuelPurchaseFacade(this.getContext(), user);
        fuelPurchaseFacade.PurgeOldRecords(cutoffDate);

        // purge engine records that are old
        EngineRecordFacade engineRecordFacade = new EngineRecordFacade(this.getContext(), user);
        engineRecordFacade.PurgeOldRecords(cutoffDate);

        // purge old Event Data Records
        EventDataRecordFacade eventDataRecordFacade = new EventDataRecordFacade(this.getContext(), this.getCurrentUser());
        eventDataRecordFacade.PurgeOldRecords(cutoffDate);

        // purge old Unassigned Driving Periods
        UnassignedDrivingPeriodFacade unassignedDrivingFacade = new UnassignedDrivingPeriodFacade(this.getContext());
        unassignedDrivingFacade.PurgeOldRecords(cutoffDate);

        // purge old Unassigned Failure Periods
        UnassignedEobrFailurePeriodFacade unassignedFailureFacade = new UnassignedEobrFailurePeriodFacade(this.getContext());
        unassignedFailureFacade.PurgeOldRecords(cutoffDate);

        //puge submitted trip records that are old
        TripRecordFacade tripFacade = new TripRecordFacade(this.getContext(), user);
        tripFacade.PurgeOldRecords(cutoffDate);

    }

    /// <summary>
    /// Answer if dashboard odometer calibration is required.
    /// </summary>
    /// <returns></returns>
    @Override
    public boolean IsOdometerCalibrationRequired() {
        EobrConfigController controller = new EobrConfigController(this.getContext());
        return controller.IsOdometerCalibrationRequired();
    }

    /// <summary>
    /// Answer if there are validated events in today's log already.
    /// A validate event is one where the time of the event was
    /// read from a synchronized clock.  In other words, the time source
    /// can be trusted to be valid.
    /// </summary>
    /// <returns></returns>
    @Override
    public boolean ValidatedEventsExistForTodaysLog() {
        boolean answer = false;

        // fetch today's log to check
        Date homeTerminalTime = DateUtility.CurrentHomeTerminalTime(this.getCurrentUser());
        EmployeeLog empLog = this.GetLocalEmployeeLog(homeTerminalTime);
        if (empLog != null) {
            answer = EmployeeLogUtilities.ValidatedEventExists(empLog);
        }

        return answer;
    }

    @Override
    public boolean IsLogExemptEligible(EmployeeLog log) {
        if (log.getExemptLogType().getValue() == ExemptLogTypeEnum.NULL) {
            return false;
        }

        IExemptLogValidator validator = ExemptLogValidatorFactory.GetExemptLogValidator(log);
        if (validator == null) {
            return false;
        }

        boolean isEligible = false;
        // add a minute from now so that this event does not conflict with one that might already be there
        Date asOfNow = DateUtility.getCurrentDateTimeUTC();
        asOfNow = DateUtility.AddMinutes(asOfNow, 1);

        try {
            isEligible = validator.IsExemptLogEligible(log, log.getEldEventList(), asOfNow);
        } catch (ParseException e) {
            ErrorLogHelper.RecordException(getContext(), e);
        }

        return isEligible;
    }

    @Override
    public void TransitionPreviousLogsFromExemptToGridLog(List<Date> listOfLogDatesToConvert) {

        if (!listOfLogDatesToConvert.isEmpty()) {
            StringBuilder logListInfo = new StringBuilder("");

            for (Date logDate : listOfLogDatesToConvert) {

                if (logListInfo.length() > 0) {
                    logListInfo.append(", ");
                }

                logListInfo.append(logDate);

                IEmployeeLogFacade facade = new EmployeeLogFacade(this.getContext(), this.getCurrentUser());
                EmployeeLog empLog = facade.GetLogByDate(logDate);
                empLog.setExemptLogType(new ExemptLogTypeEnum(ExemptLogTypeEnum.NULL));
                SaveLocalEmployeeLog(empLog);

                EmployeeLogRevisionController empLogRevisionCtrlr = new EmployeeLogRevisionController(this.getContext());
                empLogRevisionCtrlr.CreateRevisionFor(empLog, EmployeeLogRevisionTypeEnum.EXEMPTLOGCONVERSION);

            }

            ErrorLogHelper.RecordMessage(String.format(getContext().getString(R.string.exemptLogPreviousLogsMessage), logListInfo.toString()));
        }
    }

    @Override
    public void NotifyAboutTransitionCurrentLogFromExemptToGrid(EmployeeLog log, boolean hasConvertedCurrentLog, List<Date> logDateList) {
        if (EmployeeLogUtilities.hasValidHandler(EmployeeLogUtilities.UtilityHandlerType.EXEMPTLOGPREVIOUSLOGCONVERTEDHANDLER) && (hasConvertedCurrentLog || !logDateList.isEmpty())) {
            EmployeeLogUtilities.assignEventListenerToHandler(new ExemptLogPreviousLogsConvertedToGridLogEventArgs(log, logDateList, hasConvertedCurrentLog), EmployeeLogUtilities.UtilityHandlerType.EXEMPTLOGPREVIOUSLOGCONVERTEDHANDLER);
        }
    }

    @Override
    public boolean RequestLogsEmail(String emailAddress, Date startDate, Date endDate) {
        try {
            EmployeeLogReportRequest rr = new EmployeeLogReportRequest();
            rr.setEmail(emailAddress);
            rr.setBeginDate((startDate));
            rr.setEndDate(endDate);

            RESTWebServiceHelper rwsh = new RESTWebServiceHelper(getContext());
            rwsh.SubmitEmployeeLogReportRequest(rr);
            return true;
        } catch (JsonSyntaxException e) {
            this.HandleException(e, this.getContext().getString(R.string.uploadmobiledeviceinfo));
            return false;
        } catch (JsonParseException e) {
            // when connected to a network, but unable to get to webservice "e" is null at times
            if (e == null) {
                e = new JsonParseException(JsonParseException.class.getName());
            }
            this.HandleException(e, this.getContext().getString(R.string.uploadmobiledeviceinfo));
            return false;
        } catch (IOException e) {
            this.HandleException(e, this.getContext().getString(R.string.authenticateuser));
            return false;
        }
    }

    @Override
    public boolean logHasCanadianRulesets(EmployeeLog log) {

        if (log == null || log.getEldEventList() == null || log.getEldEventList().getEldEventList() == null) {
            return false;
        }

        for (EmployeeLogEldEvent evt : log.getEldEventList().getEldEventList()) {
            if (evt.getRulesetType().isCanadianRuleset()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean TransitionExemptLogToStandardLogIfNecessary(EmployeeLog log) {
        return TransitionExemptLogToStandardLogIfNecessary(log, false);
    }

    @Override
    public boolean TransitionExemptLogToStandardLogIfNecessary(EmployeeLog log, boolean hasUserBeenNotified) {
        boolean didConvertToGridLog = false;
        if (!IsLogExemptEligible(log)) {
            // the exempt log is no longer eligible
            didConvertToGridLog = true;

            //clear the calc engine out of global state to get it reinitialized with the right one
            GlobalState.getInstance().setRulesetCalcEngine(null);

            //clear any further exempt alerts (in case the transition was triggered by drive time instead of duty time)
            IHosAlert exemptAlert = new HosAlertController(getContext()).GetAlert(HosAlertTypeEnum.ExemptDuty);
            exemptAlert.Clear();

            ErrorLogHelper.RecordMessage(String.format(getContext().getString(R.string.exemptLogConversionTrace), DateUtility.getHomeTerminalDateTimeFormat().format(log.getLogDate())));

            log.setExemptLogType(new ExemptLogTypeEnum(ExemptLogTypeEnum.NULL));
            SaveLocalEmployeeLog(log);
        }

        return didConvertToGridLog;
    }

    @Override
    public String getOdometerData(EmployeeLog log) {
        float begOdo = 0;
        float endOdo = 0;
        String EobrNum = "";
        String RetVal = "";
        String rowEobrNum = "";
        float rowOdo = 0;
        int r = 0;
        boolean doShow = false;
        EmployeeLogEldEvent[] eventList = log.getEldEventList().getEldEventList();
        while (r < eventList.length) {
            rowOdo = eventList[r].getLocation().getOdometerReading();
            rowOdo = rowOdo < 0 ? -1 : rowOdo;
            rowEobrNum = eventList[r].getEobrSerialNumber();
            rowEobrNum = rowEobrNum == null ? "" : rowEobrNum;
            if (rowOdo != -1 && rowEobrNum.length() > 0) {
                if (EobrNum.compareTo(rowEobrNum) != 0) {
                    if (EobrNum.length() > 0) {
                        if (RetVal.length() > 0) {
                            RetVal += "\r\n";
                        }
                        RetVal += "\r\n" + "EOBR: " + EobrNum + "\r\nBeg. Odo: " + String.format("%.0f", begOdo) + " (ECM)" + "\r\n" + "End Odo: " + String.format("%.0f", endOdo) + " (ECM)" + "\r\n";
                    }
                    doShow = true;
                    EobrNum = rowEobrNum;
                    begOdo = rowOdo;
                }
                endOdo = rowOdo;
            }
            r++;
        }
        if (EobrNum.length() > 0) {
            if (RetVal.length() > 0) {
                RetVal += "\r\n";
            }
            RetVal += "EOBR: " + EobrNum + "\r\nBeg. Odo: " + String.format("%.0f", begOdo) + " (ECM)" + "\r\n" + "End Odo: " + String.format("%.0f", endOdo) + " (ECM)" + "\r\n";
        }
        if (!doShow) {
            RetVal = "EOBR: N/A\r\nBeg. Odo: N/A\r\nEnd Odo: N/A";
        }
        return RetVal;
    }


    @Override
    public List<EmployeeLog> EmployeeLogsForDutyStatusReport(Date logDate) {

        // make sure that there is a user, otherwise return an empty list
        if (this.getCurrentUser() == null) {
            return new ArrayList<EmployeeLog>();
        }

        IEmployeeLogFacade facade = new EmployeeLogFacade(this.getContext(), this.getCurrentUser());

        List<EmployeeLog> localLogList = new ArrayList<EmployeeLog>();
        EmployeeLog localLog = facade.GetLocalLogByDate(logDate);
        if (localLog != null) {
            localLogList.add(localLog);
        }

        List<EmployeeLog> serverLogList = new ArrayList<EmployeeLog>();
        EmployeeLog serverLog = facade.GetServerLogByDate(logDate);
        if (serverLog != null) {
            serverLogList.add(serverLog);
        }

        List<EmployeeLog> logList = EmployeeLogsForReports(false, localLogList, serverLogList);

        for (EmployeeLog log : logList) {
            // for the 34 hour weekly reset, need to determine if this log is involved in a reset
            // fetch the log that has the weekly reset info
            EmployeeLog empLogWithReset = facade.FetchWeeklyReset(log.getLogDate());
            if (empLogWithReset != null) {
                log.setWeeklyResetStartTimestamp(empLogWithReset.getWeeklyResetStartTimestamp());
                log.setIsWeeklyResetUsed(empLogWithReset.getIsWeeklyResetUsed());
            }
        }

        LogComparer logComparer = new LogComparer("Descending");
        Collections.sort(logList, logComparer);

        return logList;
    }

    /// <summary>
    /// Answer the list of employee logs to use for the Duty Failure report.
    /// The list of logs will be sorted descending based on the log date.
    /// </summary>
    /// <returns></returns>
    @Override
    public List<EmployeeLog> EmployeeLogsForDutyFailureReport(Date logDate) {
        // make sure that there is a user, otherwise return an empty list

        if (this.getCurrentUser() == null) {
            return new ArrayList<EmployeeLog>();
        }

        IEmployeeLogFacade facade = new EmployeeLogFacade(this.getContext(), this.getCurrentUser());

        List<EmployeeLog> localLogList = new ArrayList<EmployeeLog>();
        EmployeeLog localLog = facade.GetLocalLogByDate(logDate);
        if (localLog != null) {
            localLogList.add(localLog);
        }

        List<EmployeeLog> serverLogList = new ArrayList<EmployeeLog>();
        EmployeeLog serverLog = facade.GetServerLogByDate(logDate);
        if (serverLog != null) {
            serverLogList.add(serverLog);
        }

        List<EmployeeLog> logList = EmployeeLogsForReports(false, localLogList, serverLogList);

        LogComparer logComparer = new LogComparer("Descending");
        Collections.sort(logList, logComparer);

        return logList;
    }

    @Override
    public List<EmployeeLog> EmployeeLogsForReports(boolean convertToDMOFormat, List<EmployeeLog> localLogList, List<EmployeeLog> serverLogList) {
        // combine the two lists into one.
        // the local logs will all be in the list, the server logs will be used
        // when a local one doesn't exist

        List<EmployeeLog> empLogList = EmployeeLogUtilities.CombineLogLists(localLogList, serverLogList);

        LogEntryController cntrlr = new LogEntryController(getContext());

        if (cntrlr.getRoadsideInspectionMode() == true) {
            // When the app is in 'road-side inspection' mode, then
            // only show 8 totals days appropriate worth of logs.
            // Remove all logs from the list that are older than that.
            Date cutoffDate = DateUtility.CurrentHomeTerminalTime(getCurrentUser());
            // Get date portion only - Remove time from home terminal time
            cutoffDate = EmployeeLogUtilities.CalculateLogStartTime(this.getContext(), cutoffDate, this.getCurrentUser().getHomeTerminalTimeZone());

            // get current event values
            RuleSetTypeEnum curEvntRulesetTypeEnum = cntrlr.GetCurrentEventValues_RulesetType();

            // if current event is for CD rule set, cutoff day is 14 days previous, for all other rule sets
            // cutoff day is 7 days previous, making for 8 total days of logs
            if (curEvntRulesetTypeEnum.isCanadianRuleset()) {
                cutoffDate = DateUtility.AddDays(cutoffDate, -14);
            } else {
                cutoffDate = DateUtility.AddDays(cutoffDate, -7);
            }

            List<EmployeeLog> newList = new ArrayList<EmployeeLog>();
            for (EmployeeLog employeeLog : empLogList) {
                if (employeeLog.getLogDate().compareTo(cutoffDate) >= 0) {
                    newList.add(employeeLog);
                }
            }
            empLogList = newList;
        }

        if (convertToDMOFormat) {
            // summarize all of the log events for the 15 minute reporting boundary

            // TODO c# code, calc engine is not implemented yet...
//            CalcEngine.LogEventConverter converter = new LogEventConverter();
//            foreach (EmployeeLog empLog in empLogList)
//            {
//                converter.ConvertLogForReporting(empLog);
//            }
        }
        return empLogList;
    }

    /// <summary>
    /// Answer the summary of log events for the given log.
    /// This is used on the Duty Status report.
    /// </summary>
    /// <param name="empLog"></param>
    /// <returns></returns>
    @Override
    public List<LogEventSummary> LogEventSummaryFor(EmployeeLog empLog) {
        List<LogEventSummary> eventList = new ArrayList<LogEventSummary>();
        Date currentHomeTerminalTime = DateUtility.CurrentHomeTerminalTime(getCurrentUser());
        Date todaysDate = EmployeeLogUtilities.CalculateLogStartTime(this.getContext(), currentHomeTerminalTime, this.getCurrentUser().getHomeTerminalTimeZone());
        boolean isProcessingTodaysLog = empLog.getLogDate().compareTo(todaysDate) == 0;

        Date logStartTime = EmployeeLogUtilities.CalculateLogStartTime(this.getContext(), empLog.getLogDate(), getCurrentUser().getHomeTerminalTimeZone());

        //Gets only EventRecordStatus = 1 which is ACTIVE events
        EmployeeLogEldEvent[] logEvents = empLog.getEldEventList().getActiveEldEventList(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus);

        for (int index = 0; index < logEvents.length; index++) {

            // for each event in the log
            EmployeeLogEldEvent evt = logEvents[index];
            LogEventSummary eventSummary = new LogEventSummary();

            long duration = Long.MIN_VALUE;
            boolean isLastEvent = index >= logEvents.length - 1;
            if (!isLastEvent) {
                EmployeeLogEldEvent nextEvt = logEvents[index + 1];
                duration = nextEvt.getStartTime().getTime() - evt.getStartTime().getTime();
            } else {
                // Handle the very last event in the day6
                // ignore the last event of current day's log
                if (!isProcessingTodaysLog) {
                    // if start time of last event == daily log start time, this is a 24 hour off duty period
                    Calendar c = Calendar.getInstance();
                    c.setTime(logStartTime);
                    c.add(Calendar.DATE, 1);
                    String dailyLogStartTime = GlobalState.getInstance().getCompanyConfigSettings(this.getContext()).getDailyLogStartTime();

                    if (DateUtility.getHomeTerminalTime24HourFormat().format(evt.getStartTime()).compareTo(dailyLogStartTime) == 0) {
                        // is this the only event on the log (i.e. a 24-hour offduty)
                        if (logEvents.length == 1) {
                            // 24 hour off-duty event duration
                            // We are setting this to an artificial value
                            // in order to not care about Daylight Savings where
                            // we could have a 23 hour day in the spring or 25 hour day in fall
                            duration = DateUtility.MILLISECONDS_PER_DAY;
                        } else {
                            // normal midnight event at the end of the log day (probably driving)
                            Date cal = c.getTime();

                            // This is the converted date in GMT time zone.  getTimeInMillis() is calculated from 1-1-1970 GMT
                            // not CST like the date we're passing in. If we don't do this the duration is off by the times offset.
                            Calendar newCalendar = Calendar.getInstance();
                            newCalendar.setTimeZone(TimeZone.getTimeZone("GMT"));
                            newCalendar.set(cal.getYear(), cal.getMonth(), cal.getDate(), cal.getHours(), cal.getMinutes(), cal.getSeconds());

                            duration = newCalendar.getTimeInMillis();
                        }
                    } else {
                        // otherwise subtract start time of event from midnight
                        duration = c.getTimeInMillis() - evt.getStartTime().getTime();
                    }

                }
            }


            eventSummary.setStartTime(evt.getStartTime());
            eventSummary.setDutyStatus(evt.getDutyStatusEnum());
            eventSummary.setLocation(evt.getLocation().ToLocationString());
            eventSummary.setDuration(duration);
            eventSummary.setRuleSetTypeEnum(evt.getRulesetType());
            eventSummary.setMotionPictureAuthorityId(evt.getMotionPictureAuthorityId());
            eventSummary.setMotionPictureProductionId(evt.getMotionPictureProductionId());
            eventSummary.setRemarks(evt.getLogRemark());

            if (empLog.getExemptLogType().getValue() == ExemptLogTypeEnum.NULL) {
                eventList.add(eventSummary);
            } else {
                // for an exempt log, the log event view will change a little
                // The DutyStatus shown may not exactly match the logevent's status
                // Also, the log event will only be shown if it's a different status, or there is a log remark on it

                // convert the driving status to OnDuty
                if (evt.isExemptOnDutyStatus()) {
                    eventSummary.setDutyStatus(new DutyStatusEnum(DutyStatusEnum.ONDUTY));
                }

                // convert the sleeper and offDutyWellsite status to OffDuty
                if (evt.isExemptOffDutyStatus()) {
                    eventSummary.setDutyStatus(new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
                }

                LogEventSummary previousEvent = null;
                if (eventList.size() > 0) {
                    previousEvent = eventList.get(eventList.size() - 1);
                }

                boolean hasDutyStatusChanged = (previousEvent == null || previousEvent.getDutyStatusEnum().getValue() != eventSummary.getDutyStatusEnum().getValue());

                // for exempt logs, only add the event if the duty status has changed, or if there is a log remark
                if (hasDutyStatusChanged || (eventSummary.getRemarks() != null && eventSummary.getRemarks().length() > 0)) {
                    eventList.add(eventSummary);
                } else {
                    // if the status has not changed, then combine this eventSummary's duration with the previous summary
                    if (isLastEvent && hasDutyStatusChanged == false) {
                        previousEvent.setDuration(eventSummary.getDuration());
                    } else {
                        previousEvent.setDuration(previousEvent.getDuration() + eventSummary.getDuration());

                    }

                }

            }
        }

        return eventList;

    }

    /// Answer the local, unsubmitted, employee log for the user
    /// for the specified date.
    /// If no log is found locally, then a new one is created.
    /// <param name="logDate">date to search for</param>
    /// <returns>EmployeeLog matching the date, if no log exists for the date, a new one is created</returns>
    @Override
    public EmployeeLog GetLocalEmployeeLogOrCreateNew(User user, Date logDate, DutyStatusEnum selectedDutyStatus) {
        // fetch the log for the current user, for the specified log date
        EmployeeLog empLog = this.GetLocalEmployeeLog(user, logDate);

        if (empLog == null) {
            // no current log found for the user on the logDate
            // first, see if there is a downloaded employee log that can be used
            empLog = this.GetDownloadedEmployeeLog(user, logDate);
            if (empLog == null) {
                // no downloaded log either
                // if in app restart scenario - we have crossed date boundary
                // to a new log - get last duty status from previous log and
                // carry that forward
                // Note: only look for previous day's log - don't carry status
                // across multiple days
                if (this.getAppRestartFlag()) {
                    EmployeeLog previousEmployeeLog = this.GetEmployeeLog(user, DateUtility.AddDays(logDate, -1));
                    if (previousEmployeeLog != null) {

                        DutyStatusEnum dutyStatus = EmployeeLogUtilities.GetLastEventInLog(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus, previousEmployeeLog).getDutyStatusEnum();
                        empLog = EmployeeLogUtilities.CreateNewLog(this.getContext(), this.getCurrentUser(), logDate, dutyStatus, null);

                        // in app restart scenario that crossed the midnight boundary in a non-driving status, the mobile start time stamp
                        // was not initialized.  Calculation of driving distance utilizes the mobile start timestamp, so no total distance
                        // was getting assigned to the log even though driving time occurred.
                        // set the mobile start timestamp to the start of the log
                        if (empLog.getMobileStartTimestamp() == null) {
                            empLog.setMobileStartTimestamp(logDate);
                        }
                    }
                }

                // if we haven't created a log yet, create an off duty log
                if (empLog == null) {
                    empLog = EmployeeLogUtilities.CreateNewLog(this.getContext(), this.getCurrentUser(), logDate, selectedDutyStatus, null);
                }
            }
        }
        // local employee log exists
        else {
            // set mobileendtimestamp to null
            empLog.setMobileEndTimestamp(null);

            // if the user's home terminal time zone has changed, update the timezone
            // stored on the log to the users home terminal time zone
            if (empLog.getTimezone() != user.getHomeTerminalTimeZone()) {
                empLog.setTimezone(user.getHomeTerminalTimeZone());
            }

            this.SaveLocalEmployeeLog(empLog);
        }
        return empLog;
    }

    /// <summary>
    /// Persist the employee log to the "local" logs directory
    /// </summary>
    /// <param name="employeeLog">EmployeeLog to save</param>
    @Override
    public void SaveLocalEmployeeLog(EmployeeLog employeeLog) {
        this.SaveLocalEmployeeLog(this.getCurrentUser(), employeeLog);
    }

    /// <summary>
    /// Persist the employee log to the "local" logs directory
    /// </summary>
    /// <param name="user">user to save the log to</param>
    /// <param name="employeeLog">EmployeeLog to save</param>
    @Override
    public void SaveLocalEmployeeLog(User usr, EmployeeLog employeeLog) {
        this.SaveLog(usr, employeeLog, ACTIVE_LOCAL_LOG.getValue());//LogSourceStatusEnum.LocalUnsubmitted);
    }

    /// <summary>
    /// Answer the count of all unsubmitted local logs
    /// </summary>
    /// <returns></returns>
    @Override
    public int GetLocalLogCount() {
        IEmployeeLogFacade facade = new EmployeeLogFacade(this.getContext(), this.getCurrentUser());
        int logCount = facade.FetchLogCountByStatus(ACTIVE_LOCAL_LOG.getValue());
        return logCount;
    }

    @Override
    public boolean getAppRestartFlag() {
        return GlobalState.getInstance().getAppRestartFlag();
    }

    @Override
    public void setAppRestartFlag(boolean appRestart) {
        GlobalState.getInstance().setAppRestartFlag(appRestart);
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++Protected Methods++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    /// <summary>
    /// Answer the list of employee logs to use for the reporting functions.
    /// This list of logs downloaded from DMO (server logs), and the locally
    /// created logs (from KellerMobile) will be combined.  If the log is
    /// defined in both places, then the server log is used.
    /// When the application is in 'Road-side Inspection' mode, this limits
    /// the list of logs to the exact set based on the user's employee rule.
    /// </summary>
    /// <returns>list of EmployeeLogs</returns>
    protected List<EmployeeLog> EmployeeLogsForReports(boolean convertToDMOFormat) {

        // make sure that there is a user, otherwise return an empty list
        if (this.getCurrentUser() == null) {
            return new ArrayList<EmployeeLog>();
        }

        IEmployeeLogFacade facade = new EmployeeLogFacade(this.getContext());
        List<EmployeeLog> localLogList = facade.GetLocalLogList();
        List<EmployeeLog> serverLogList = facade.GetServerLogList();

        List<EmployeeLog> empLogList = EmployeeLogsForReports(convertToDMOFormat, localLogList, serverLogList);

        return empLogList;
    }

    /// <summary>
    /// Get log data needed for Daily Hours Recap report.
    /// </summary>
    /// <param name="empLog"></param>
    /// <returns></returns>
    protected List<EmployeeLog> EmployeeLogsRecapForReports(boolean convertToDMOFormat) {
        // make sure that there is a user, otherwise return an empty list
        if (this.getCurrentUser() == null) {
            return new ArrayList<EmployeeLog>();
        }

        IEmployeeLogFacade facade = new EmployeeLogFacade(this.getContext());
        List<EmployeeLog> localLogList = facade.GetLocalLogList();
        List<EmployeeLog> serverLogList = facade.GetServerLogList();

        // combine the two lists into one.
        // the local logs will all be in the list, the server logs will be used
        // when a local one doesn't exist
        List<EmployeeLog> empLogList = EmployeeLogUtilities.CombineLogLists(localLogList, serverLogList);

        Date cutoffDate = DateUtility.CurrentHomeTerminalTime(getCurrentUser());
        // Get date portion only - Remove time from home terminal time
        cutoffDate = EmployeeLogUtilities.CalculateLogStartTime(this.getContext(), cutoffDate, this.getCurrentUser().getHomeTerminalTimeZone());

        HosAuditController cntrlr = new HosAuditController(getContext());

        //Need to get calcengine in order to use methods which provide us with cutoff date
        IHosRulesetCalcEngine calcEngine = cntrlr.getCalcEngine();

        //adjust cutoff date based on ruleset
        cutoffDate = calcEngine.DateOfAuditPeriodStart(cutoffDate);

        //tack on 3 additional days to support recap
        cutoffDate = DateUtility.AddDays(cutoffDate, -3);

        List<EmployeeLog> newList = new ArrayList<EmployeeLog>();
        for (EmployeeLog employeeLog : empLogList) {
            if (employeeLog.getLogDate().compareTo(cutoffDate) >= 0) {
                newList.add(employeeLog);
            }
        }
        empLogList = newList;


        //todo convertToDMOFormat ignored for now, when implemnented in EmployeeLogsForReports this should follow suit

        return empLogList;
    }

    protected int DownloadRecentLogs(User user) throws KmbApplicationException {
        Date today = DateUtility.CurrentHomeTerminalTime(user);
        today = EmployeeLogUtilities.CalculateLogStartTime(this.getContext(), today, user.getHomeTerminalTimeZone());
        Date endDate = today;

        Date startDate = DateUtility.AddDays(today, -7);

        int logCount = this.DownloadLogs(user, startDate, endDate);

        if (user.IsCanadianRulesetAvailable()) {
            startDate = DateUtility.AddDays(today, -14);
            endDate = DateUtility.AddDays(today, -8);

            logCount += this.DownloadLogs(user, startDate, endDate);
        } else if (user.getIs34HourResetAllowed()) {
            Date resetDate = this.GetPreviousWeeklyResetStartTimestamp(endDate);
            if (resetDate == null && this.DoesWeeklyResetExistNewerThan(startDate)) {
                startDate = DateUtility.AddDays(today, -14);
                endDate = DateUtility.AddDays(today, -8);
                logCount += this.DownloadLogs(user, startDate, endDate);
            }
        }

        return logCount;
    }

    public void reverseMapRelateEvents(EmployeeLogEldEvent[] employeeLogEldEvents, EmployeeLogEldEvent[] downloadedEvents) {
        HashMap<Long, EmployeeLogEldEvent> localEventsByClusterPK = new HashMap<>();
        for (EmployeeLogEldEvent event : employeeLogEldEvents) {
            localEventsByClusterPK.put(event.getEncompassClusterPK(), event);
        }

        for (EmployeeLogEldEvent event : downloadedEvents) {
            if (event.getRelatedEncompassClusterPk() != 0) {
                EmployeeLogEldEvent relatedEvent = localEventsByClusterPK.get(event.getRelatedEncompassClusterPk());
                EmployeeLogEldEvent localEvent = localEventsByClusterPK.get(event.getEncompassClusterPK());

                if (relatedEvent != null && localEvent != null) {
                    localEvent.setRelatedKmbPK(relatedEvent.getPrimaryKey());
                }
            }
        }
    }

    // Added a new flag that is used for Check for Edit Requests to see if
    // we are not downloading an that has been already been processed but still not uploaded

    protected int DownloadLogs(User user, Date startDate, Date endDate) throws KmbApplicationException {
        int downloadedLogCount = -1;

        EmployeeLogList logs = this.DownloadEmployeeLogs(user, startDate, endDate);
        downloadedLogCount = logs.getEmployeeLogList().length;

        IEmployeeLogFacade logFacade = new EmployeeLogFacade(getContext(), user);
        Date currentHomeTerminalTime = DateUtility.CurrentHomeTerminalTime(user);

        // First get the start of the log, then strip it down to just the log date.
        Date todaysLogDate = EmployeeLogUtilities.CalculateLogStartTime(this.getContext(), currentHomeTerminalTime, user.getHomeTerminalTimeZone());
        todaysLogDate = DateUtility.GetDateFromDateTime(todaysLogDate);

        LogEntryController logEntryCtrlr = new LogEntryController(this.getContext());
        for (EmployeeLog downloadedLog : logs.getEmployeeLogList()) {
            // If checkForPreviousEditRequests flag check if there's a previous request for edits that has not been submitted
            if (DoPreviouslyResolvedLocalEditRequestsExistForDownloadedLog(user, downloadedLog)) {
                // when previously resolved edit requests exist, do not save over the local log
                _downloadLogSkipped = true;
                continue;
            }
            else if (DoLocalEditsExistForDownloadedLog(user, downloadedLog)){
                // when previously accepted edit requests have not been submitted to Encompass,
                // do not save over the local log
                _downloadLogSkipped = true;
                continue;
            }

            // verify that the ruleset is assigned across the log completely.
            if (downloadedLog.getRuleset().getValue() == RuleSetTypeEnum.NULL) {
                downloadedLog.setRuleset(this.getCurrentUser().getRulesetTypeEnum());
            }

            //TODO comment in winmobile KMB code indicates in transition period around release
            // of 1.2.3368.3 it might be possible where the ruleset is not assigned to the
            // log events.  That appears to be happening in all cases (in winmobile as well).
            // This means the rule set of all log events is set to whatever the rule set of
            // the parent employee log is set to.  This will not work for a log that switches
            // between US and CD rulesets (or for a driver that switches rulesets on different
            // days).  Need to look into this and figure out why the ruleset of the log events
            // isn't downloading from DMO
            for (EmployeeLogEldEvent event : downloadedLog.getEldEventList().getEldEventList()) {
                if (event != null && event.getRulesetType().getValue() == RuleSetTypeEnum.NULL) {
                    event.setRulesetType(downloadedLog.getRuleset());
                }

                // mandate events with Latitude and Longitude values should have a Geolocation populated
                if(GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()
                        && ReverseGeocodeUtilities.eventIsMissingGeolocation(event)) {
                    String geolocation = ReverseGeocodeUtilities
                            .getGeolocationFromEventData(event.getEventDateTime(), event.getLatitude(), event.getLongitude());
                    if(!geolocation.isEmpty()) { event.setGeolocation(geolocation); }
                }
            }

            // set the timezone property on log downloaded from DMO
            if (downloadedLog.getTimezone().equals(TimeZoneEnum.valueOfDMOEnum(TimeZoneEnum.DmoEnum_Null))) {
                downloadedLog.setTimezone(user.getHomeTerminalTimeZone());
            }

            // First get the start of the log, then strip it down to just the log date.
            Date localLogDate = EmployeeLogUtilities.CalculateLogStartTime(this.getContext(), downloadedLog.getLogDate(), downloadedLog.getTimezone());
            localLogDate = DateUtility.GetDateFromDateTime(localLogDate);
            EmployeeLog localLog = this.GetLocalEmployeeLog(user, localLogDate);

            // Determine if a local log exists for downloaded log's date
            if (localLog != null) {
                // there's a local log already (One that has not been uploaded to encompass)!!!
                EmployeeLogUtilities.mergeLog(getContext(), localLog, downloadedLog);
                reverseMapRelateEvents(localLog.getEldEventList().getEldEventList(), downloadedLog.getEldEventList().getEldEventList());
                this.SaveLog(user, localLog, 1);
            } else {
                // there are no local logs (logs that have not been uploaded to encompass)
                LogStatus logStatus;
                if (todaysLogDate.compareTo(downloadedLog.getLogDate()) == 0 && !GlobalState.getInstance().getIsViewOnlyMode()) {
                    // Make the downloaded log for today Active, when not in check hours mode
                    logStatus = LogStatus.ACTIVE_LOCAL_LOG;
                } else {
                    logStatus = LogStatus.SERVER_COPY;
                }
                reverseMapRelateEvents(downloadedLog.getEldEventList().getEldEventList(), downloadedLog.getEldEventList().getEldEventList());
                SaveLog(user, downloadedLog, logStatus.getValue());
                localLog = downloadedLog;
            }

            ///update global state if the local log saved is the same log date as global state.
            if (localLog != null && todaysLogDate.compareTo(localLog.getLogDate()) == 0 && logEntryCtrlr.IsTheCurrentActiveUser(user)) {
                logEntryCtrlr.UpdateCurrentLog(localLog);
            }
        }

        EmployeeLog localLogForToday = this.GetLocalEmployeeLog(user, todaysLogDate);
        if (localLogForToday != null) {
            if (localLogForToday.getEldEventList().getEldEventList(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus).length == 2) {
                // there are only two events on todays log
                // the two events might be an OffDuty at 12:00am and the OnDuty at login time
                // check to see if this log was just newly created
                EmployeeLogEldEvent lastEventOnTodaysLog = EmployeeLogUtilities.GetLastEventInLog(localLogForToday);
                if (localLogForToday.getMobileStartTimestamp() != null && lastEventOnTodaysLog.getStartTime().compareTo(localLogForToday.getMobileStartTimestamp()) == 0) {
                    // since the last event on todays log is the start time on the log,
                    // then this log was just newly created
                    EmployeeLogEldEvent lastEventPriorToToday = logFacade.FetchMostRecentLogEventForUser(localLogForToday.getLogDate());
                    if (lastEventPriorToToday != null) {
                        EmployeeLogEldEvent firstEvent = EmployeeLogUtilities.GetFirstEventInLog(localLogForToday);
                        if (firstEvent.getRulesetType().getValue() != lastEventPriorToToday.getRulesetType().getValue()) {
                            this.ChangeRulesetOfEntireLog(user, localLogForToday, Enums.RuleTypeEnum.Both, lastEventPriorToToday.getRulesetType());
                            logEntryCtrlr.UpdateCurrentLog(localLogForToday);
                        }
                    }
                }
            } else {
                // more than just two events on the log - this may happen when
                // the log for today is downloaded from DMO - apply the ruleset
                // found on the event that occurs just before login.
                // This allows DMO to control which ruleset is used for the log.

                // 8/2/11 JHM - Added check to skip this ruleset change if only the initial log event (=LogStartTime) is prior to login.
                // This means if all events are created on the handheld, no rulesets will be changed

                EmployeeLogEldEvent eventOnTodaysLogPriorToLogin = EmployeeLogUtilities.GetEventPriorToTime(localLogForToday, localLogForToday.getMobileStartTimestamp());

                if (eventOnTodaysLogPriorToLogin != null && eventOnTodaysLogPriorToLogin.getStartTime().compareTo(EmployeeLogUtilities.CalculateLogStartTime(getContext(), localLogForToday.getLogDate(), user.getHomeTerminalTimeZone())) != 0 && user.getRulesetTypeEnum().getValue() != eventOnTodaysLogPriorToLogin.getRulesetType().getValue()) {
                    EmployeeLogEldEvent lastEventInLocalLog = EmployeeLogUtilities.GetLastEventInLog(localLogForToday);
                    lastEventInLocalLog.setRulesetType(eventOnTodaysLogPriorToLogin.getRulesetType());

                    this.SaveLocalEmployeeLog(user, localLogForToday);

                    // ensure that the current log in use for LogEntry is updated with this log
                    logEntryCtrlr.UpdateCurrentLog(localLogForToday);
                }
            }
        }

        // 8/2/11 JHM - Re-run the AssignInitialRulesetForUser method so user's current & international
        // rulesets are updated.  Refresh user object in state.
        EmployeeRuleController empRuleController = new EmployeeRuleController(getContext());
        empRuleController.AssignInitialRulesetForUser(user);

        return downloadedLogCount;
    }

    // Review existing local logs to see if an edit request exists and has been processed
    private Boolean DoPreviouslyResolvedLocalEditRequestsExistForDownloadedLog(User user, EmployeeLog employeeLogFromServer) {

        EmployeeLog localEmployeeLog = this.GetLocalLog(user, employeeLogFromServer.getLogDate());
        boolean serverEditRequestHasBeenHandled = false;

        if (localEmployeeLog != null) {
            EmployeeLogEldEvent[] serverEldEventEldRequestsThatMayNeedToBeAddressed = employeeLogFromServer.getEldEventList().getInactiveChangeRequestedEldEventList();

            for (EmployeeLogEldEvent serverEldEventEldRequestThatMayNeedToBeAddressed : serverEldEventEldRequestsThatMayNeedToBeAddressed) {
                // This eld event has already been addressed on the local log for this day! Do not merge these events!
                // we could throw an exception here so that the UI could present the user with an error message
                serverEditRequestHasBeenHandled = HasEldEventEditRequestAlreadyBeenAddressed(serverEldEventEldRequestThatMayNeedToBeAddressed, localEmployeeLog);
                if (serverEditRequestHasBeenHandled) {
                    break;
                }
            }
        }

        return serverEditRequestHasBeenHandled;
    }

    private Boolean DoLocalEditsExistForDownloadedLog(User user, EmployeeLog downloadedEmployeeLog) {

        EmployeeLog localEmployeeLog = this.GetLocalLog(user, downloadedEmployeeLog.getLogDate());
        boolean hasLocalEditsForLog = false;

        if (localEmployeeLog != null) {
            EmployeeLogEldEvent[] localInactiveChangedEvents = localEmployeeLog.getEldEventList().getInactiveChangedEldEventList();

            for (EmployeeLogEldEvent changedEvent : localInactiveChangedEvents){
                hasLocalEditsForLog = HasEldEventEditRequestNotBeenSubmitted(changedEvent, downloadedEmployeeLog);
                if (hasLocalEditsForLog) {
                    break;
                }
            }
        }

        return  hasLocalEditsForLog;
    }

    private boolean HasEldEventEditRequestAlreadyBeenAddressed(EmployeeLogEldEvent eldEventFromServer, EmployeeLog localEmployeeLog) {
        EmployeeLogEldEvent localEldEvent = localEmployeeLog.getEldEventList().getEldEventByClusteredKey(eldEventFromServer.getEncompassClusterPK());
        Boolean hasEldEventEditRequestAlreadyBeenAddressed = false;

        if (localEldEvent != null) {
            if (localEldEvent.getEventRecordStatus() != com.jjkeller.kmbapi.employeelogeldevents.Enums.EmployeeLogEldEventRecordStatus.InactiveChangeRequested.getValue()) {
                hasEldEventEditRequestAlreadyBeenAddressed = true;
            }
        }

        return hasEldEventEditRequestAlreadyBeenAddressed;
    }

    private boolean HasEldEventEditRequestNotBeenSubmitted(EmployeeLogEldEvent localChangedEldEvent, EmployeeLog downloadedEmployeeLog) {
        EmployeeLogEldEvent serverEldEvent = downloadedEmployeeLog.getEldEventList().getEldEventByClusteredKey(localChangedEldEvent.getEncompassClusterPK());
        boolean hasEldEditRequestNotBeenSubmitted = false;

        if (serverEldEvent != null) {
            if (serverEldEvent.getEventRecordStatus() != com.jjkeller.kmbapi.employeelogeldevents.Enums.EmployeeLogEldEventRecordStatus.InactiveChanged.getValue()) {
                hasEldEditRequestNotBeenSubmitted = true;
            }
        }

        return  hasEldEditRequestNotBeenSubmitted;
    }

    public KMBEncompassUserList DownloadKMBEncompassUsers() throws KmbApplicationException {
        KMBEncompassUserList kmbEncompassUserList = new KMBEncompassUserList();
        try {

            RESTWebServiceHelper rwsh = new RESTWebServiceHelper(getContext());
            kmbEncompassUserList = rwsh.DownloadKmbEncompassUsers();
        } catch (JsonSyntaxException jse) {
            this.HandleExceptionAndThrow(jse, this.getContext().getString(R.string.downloademployeelogs), this.getContext().getString(R.string.exception_webservicecommerror), this.getContext().getString(R.string.exception_serviceunavailable));
        } catch (JsonParseException e) {
            // when connected to a network, but unable to get to webservice "e" is null
            if (e == null) {
                e = new JsonParseException(JsonParseException.class.getName());
            }
            this.HandleExceptionAndThrow(e, this.getContext().getString(R.string.downloademployeelogs), this.getContext().getString(R.string.exception_webservicecommerror), this.getContext().getString(R.string.exception_serviceunavailable));
        } catch (IOException ioe) {
            this.HandleExceptionAndThrow(ioe, this.getContext().getString(R.string.downloademployeelogs), this.getContext().getString(R.string.exception_webservicecommerror), this.getContext().getString(R.string.exception_serviceunavailable));
        }
        return kmbEncompassUserList;

    }

    // Answer the list of logDates that are missing for the current user.
    // All users are required to have 8 days worth of logs (14 for CD rulesets),
    //regardless of the Federal Ruleset being followed.
    protected EmployeeLogList DownloadEmployeeLogs(User user, Date startDate, Date endDate) throws KmbApplicationException {
        EmployeeLogList logList = new EmployeeLogList();

        // TCH - 9/11/2012 - in a team driver scenario where one member of team logs out and submits, after submitting logs,
        // logs are downloaded from Encompass.  In this scenario, since the driver has already been logged out, the KMBUserInfo
        // credentials have been changed to the other driver.  Need to set the DMOEmployeeId of the KMBUserInfo object to
        // the value of the user whose logs are being downloaded.
        GlobalState.getInstance().getKmbUserInfo().setKmbUsername(user.getCredentials().getUsername());
        GlobalState.getInstance().getKmbUserInfo().setKmbPassword(user.getCredentials().getPassword());
        GlobalState.getInstance().getKmbUserInfo().setDmoEmployeeId(user.getCredentials().getEmployeeId());

        try {
            RESTWebServiceHelper rwsh = new RESTWebServiceHelper(getContext());
            Date changeTimestampUTC = user.getCredentials().getLastSubmitTimestampUtc();
            logList = rwsh.DownloadLogsInDateRange(startDate, endDate, changeTimestampUTC, user.getIsShorthaulException());
        } catch (JsonSyntaxException jse) {
            this.HandleExceptionAndThrow(jse, this.getContext().getString(R.string.downloademployeelogs), this.getContext().getString(R.string.exception_webservicecommerror), this.getContext().getString(R.string.exception_serviceunavailable));
        } catch (JsonParseException e) {
            // when connected to a network, but unable to get to webservice "e" is null
            if (e == null) {
                e = new JsonParseException(JsonParseException.class.getName());
            }
            this.HandleExceptionAndThrow(e, this.getContext().getString(R.string.downloademployeelogs), this.getContext().getString(R.string.exception_webservicecommerror), this.getContext().getString(R.string.exception_serviceunavailable));
        } catch (IOException ioe) {
            this.HandleExceptionAndThrow(ioe, this.getContext().getString(R.string.downloademployeelogs), this.getContext().getString(R.string.exception_webservicecommerror), this.getContext().getString(R.string.exception_serviceunavailable));
        } finally {
            // set KMBUserInfo back to the current user's value
            GlobalState.getInstance().getKmbUserInfo().setKmbUsername(this.getCurrentUser().getCredentials().getUsername());
            GlobalState.getInstance().getKmbUserInfo().setKmbPassword(this.getCurrentUser().getCredentials().getPassword());
            GlobalState.getInstance().getKmbUserInfo().setDmoEmployeeId(this.getCurrentUser().getCredentials().getEmployeeId());
        }

        return logList;
    }

    /// <summary>
    /// Answer the log from the database for the specified user and log date.
    /// The log will be a Local log (LogSourceStatusEnum.LocalUnsubmitted).
    /// </summary>
    /// <param name="user">user to fetch the log for</param>
    /// <param name="logDate">log date to read</param>
    /// <returns>the EmployeeLog matching the log date, otherwise return null if not found</returns>
    protected EmployeeLog GetLocalLog(User user, Date logDate) {
        IEmployeeLogFacade facade = new EmployeeLogFacade(this.getContext(), user);
        EmployeeLog empLog = facade.GetLocalLogByDate(logDate);
        return empLog;
    }

    /// Answer the log from the database for the specified user and log date.
    /// The log will be a Server log (LogSourceStatusEnum.LocalUnsubmitted).
    /// <param name="user">user to fetch the log for</param>
    /// <param name="logDate">log date to read</param>
    /// <returns>the EmployeeLog matching the log date, otherwise return null if not found</returns>
    protected EmployeeLog GetServerLog(User user, Date logDate) {
        IEmployeeLogFacade facade = new EmployeeLogFacade(this.getContext());
        EmployeeLog empLog = facade.GetServerLogByDate(logDate);
        return empLog;
    }

    protected void PurgeDownloadedLog(User user, EmployeeLog empLog) {
        IEmployeeLogFacade facade = new EmployeeLogFacade(this.getContext());
        facade.DeleteLog(empLog);
    }

    /// Save the log to the database.
    /// The source of the log is used to indicate the purpose of the log.
    /// </summary>
    /// <param name="empLog">log to save</param>
    /// <param name="logSourceStatusEnum">source of the log</param>
    protected void SaveLog(User usr, EmployeeLog empLog, int logSourceStatusEnum) {
        IEmployeeLogFacade facade = new EmployeeLogFacade(this.getContext(), usr);
        facade.Save(empLog, logSourceStatusEnum);
    }

    /// <summary>
    /// Submit all of the local logs, for every user, to DMO.
    /// Answer if all of the logs were successfully submitted to DMO.
    /// </summary>
    /// <param name="excludeTodaysLogs"></param>
    protected boolean SubmitAllLocalLogs(boolean excludeTodaysLogs) {
        boolean isSuccessful = true;


        // fetch the list of all local logs for the employee
        IEmployeeLogFacade facade = new EmployeeLogFacade(this.getContext(), this.getCurrentUser());
        List<EmployeeLog> localLogListComplete = facade.GetLocalLogListAllUsers(excludeTodaysLogs);
        if (localLogListComplete.size() > 0) {
            // submit the local logs
            boolean submittedToDMOSuccessfully = this.SubmitLocalLogsToDMO(localLogListComplete);
            if (!submittedToDMOSuccessfully) {
                isSuccessful = false;
            }
        }

        // reset the webservice back to it's original, logged in, employeeId
        GlobalState.getInstance().getKmbUserInfo().setDmoEmployeeId(this.getCurrentUser().getCredentials().getEmployeeId());

        return isSuccessful;
    }

    /// <summary>
    /// Submits all the local logs that for a single user, to DMO.
    /// Return TRUE if all of the logs were successfully submitted to DMO.
    /// </summary>
    /// <param name="excludeTodaysLogs"></param>
    protected boolean SubmitUsersLocalLogs(User user, boolean excludeTodaysLogs) {
        boolean isSuccessful = true;

        // fetch the list of all local logs for the employee
        IEmployeeLogFacade facade = new EmployeeLogFacade(this.getContext(), user);
        List<EmployeeLog> localLogList = facade.GetLocalLogList(excludeTodaysLogs);

        if (localLogList.size() > 0) {
            // submit the local logs
            boolean submittedToDMOSuccessfully = this.SubmitLocalLogsToDMO(localLogList);
            if (!submittedToDMOSuccessfully) {
                isSuccessful = false;
            }
        }

        // reset the webservice back to it's original, logged in, employeeId
        GlobalState.getInstance().getKmbUserInfo().setDmoEmployeeId(this.getCurrentUser().getCredentials().getEmployeeId());

        return isSuccessful;
    }

    /// <summary>
    /// Submit a list of logs for a single employee to DMO.
    /// The employee log will be converted to the summarized reporting format
    /// of using 15 minute boundaries for the log events.
    /// The logs will be individually submitted to DMO, so that if the
    /// single log was successfully processed by DMO, it can be purged locally.
    /// Answer if every log in the list was successfully submitted to DMO.
    /// </summary>
    /// <param name="empId">employee to submit the logs for</param>
    /// <param name="localLogList">list of logs to submit</param>
    /// <returns></returns>
    protected boolean SubmitLocalLogsToDMO(List<EmployeeLog> localLogList) {
        boolean isSuccessful = false;

        try {
            // remove KMBWebService from state data so it will recreate
            // and use the new send timeout value
            //this.AddToState(StateConsts.KMBWebService, null);
            //this.BindingSendTimeout = new TimeSpan(0, 3, 0);

            //CalcEngine.LogEventConverter converter = new LogEventConverter();
            boolean errorOccurredSubmittingToDMO = false;
            for (EmployeeLog empLog : localLogList) {
                // set the webservice credentials to reflect the employee being submitted
                GlobalState.getInstance().getKmbUserInfo().setDmoEmployeeId(empLog.getEmployeeId());

                // transfer the derived distance into the log
                float derivedDist = empLog.getMobileDerivedDistance();
                empLog.setMobileRecordedDistance(derivedDist);

                // 7/17/12 JHM - End any open team driving periods
                if (!empLog.getTeamDriverList().IsEmpty()) {
                    for (TeamDriver driver : empLog.getTeamDriverList().getTeamDriverList()) {
                        if (driver.getEndTime() == null) {
                            driver.setEndTime(DateUtility.AddDays(EmployeeLogUtilities.CalculateLogStartTime(getContext(), empLog.getLogDate(), this.getCurrentUser().getHomeTerminalTimeZone()), 1));
                        }
                    }
                }


                for (EmployeeLogEldEvent logEvent : empLog.getEldEventList().getEldEventList(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus)) {
                    Location location = logEvent.getLocation();
                    if (location.getOdometerReading() < 0) {
                        location.setOdometerReading(0);
                        logEvent.setLocation(location);
                    }

                }

                EmployeeLogList logList = new EmployeeLogList();
                logList.setEmployeeLogList(new EmployeeLog[]{empLog});

                RESTWebServiceHelper rwsh = new RESTWebServiceHelper(getContext());
                rwsh.SubmitLogs(logList);

                this.ChangeLogStatusToSubmitted(empLog);
            }

            // if there were no errors submitting to DMO, then the entire
            // operation was successful
            if (!errorOccurredSubmittingToDMO) {
                isSuccessful = true;
            }

            if (isSuccessful) {
                // After submitting logs, process all reassignments
                TeamDriverController teamDriverController = new TeamDriverController(this.getContext());
                isSuccessful = teamDriverController.SendAllSavedReassignmentRequests();

                if (!isSuccessful) {
                    // ToDo:  Notify the driver?
                }
            }

        } catch (IOException ioe) {
            // log the exception to the error log and then return false - UI
            // handles displaying error based on return value
            this.HandleException(ioe, this.getContext().getString(R.string.submitlocallogstodmo));
            isSuccessful = false;
        } catch (Exception ex) {
            // log the exception to the error log and then return false - UI
            // handles displaying error based on return value
            this.HandleException(ex, this.getContext().getString(R.string.submitlocallogstodmo));
            isSuccessful = false;
        } finally {
            // remove KMBWebService from state data so it will recreate.
            // set binding send timeout back to default of 1 minute
            //this.AddToState(StateConsts.KMBWebService, null);
            //this.BindingSendTimeout = new TimeSpan(0, 1, 0);
        }

        return isSuccessful;
    }

    /// <summary>
    /// Change the status of the log to LogSourceStatusEnum.LocalSubmitted.
    /// This is used to indicate that the log has been successfully submitted to DMO.
    /// </summary>
    /// <param name="log"></param>
    protected void ChangeLogStatusToSubmitted(EmployeeLog log) {
        IEmployeeLogFacade facade = new EmployeeLogFacade(this.getContext(), this.getCurrentUser());
        facade.UpdateLogStatus(log, 2);

        // Upon successful upload to DMO - reset any isManuallyEditedByKMBUser Eld Event logs back to false.
        // This flag is used by the View Grid so it can color code the changed events differently.
        if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
            EmployeeLogEldEventFacade eventFacade = new EmployeeLogEldEventFacade(this.getContext(), this.getCurrentUser());
            for (EmployeeLogEldEvent event : log.getEldEventList().getEldEventList()) {
                if (event.getIsManuallyEditedByKMBUser()) {
                    event.setIsManuallyEditedByKMBUser(false);
                    eventFacade.Save(event);
                }
            }
        }
    }


    // Find closest match to point being decoded from provided list and decode
    // the city and state - including direction and bearing
    protected void ReverseGeocodeFromLocationDB(GpsLocation gpsLocToDecode, List<LocationDBAdapter.LocationDBLocation> locationList) {
        String closestCity = "";
        String closestState = "";
        double distance = 0;
        double closestDistance = -1;
        double closestLat = 0;
        double closestLon = 0;

        for (LocationDBAdapter.LocationDBLocation location : locationList) {
            distance = ReverseGeocodeUtilities.GetDistanceBetweenPoints(gpsLocToDecode.getLatitudeDegrees(), gpsLocToDecode.getLongitudeDegrees(), location.getLatitude(), location.getLongitude());

            if (closestDistance == -1 || distance < closestDistance) {
                closestDistance = distance;
                closestCity = location.getCity();
                closestState = location.getState();
                closestLat = location.getLatitude();
                closestLon = location.getLongitude();
            }
        }

        // if closestCity is defined - get direction and bearing
        if (closestCity != null && closestCity.length() > 0) {
            String bearing = ReverseGeocodeUtilities.GetBearing(gpsLocToDecode.getLatitudeDegrees(), gpsLocToDecode.getLongitudeDegrees(), closestLat, closestLon);
            String decodedCity = "";

            if (this.getCurrentUser().getDistanceUnits().equalsIgnoreCase(this.getContext().getString(R.string.kilometers))) {
                decodedCity = String.format(this.getContext().getString(R.string.decodedlocation_km), closestDistance, bearing, closestCity);
            } else {
                decodedCity = String.format(this.getContext().getString(R.string.decodedlocation_miles), closestDistance / GlobalState.MilesToKilometers, bearing, closestCity);
            }

            DecodedLocation decodedLocation = new DecodedLocation();
            decodedLocation.setCity(decodedCity);
            decodedLocation.setState(closestState);

            gpsLocToDecode.setDecodedInfo(decodedLocation);
        }
    }

    @Override
    public void UpdateLoginEvent() throws Throwable {
        EobrConfigController eobrController = new EobrConfigController(this.getContext(), eobrReader);

        EmployeeLog empLog = GlobalState.getInstance().getCurrentEmployeeLog();
        float odom = eobrController.GetRawOdometer();
        String serialNumber = eobrController.getSerialNumber();

        if (empLog != null) {
            //Update the duty status events in the log
            for (EmployeeLogEldEvent dutyStatusEvent : empLog.getEldEventList().getEldEventList(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus)) {

                //Set serial number if one is present AND event doesn't already have one assigned
                if (serialNumber != null && !serialNumber.equals("") && (dutyStatusEvent.getEobrSerialNumber() == null || dutyStatusEvent.getEobrSerialNumber().equals(""))) {
                    dutyStatusEvent.setEobrSerialNumber(serialNumber);
                }

                //Only update the odometer if it is less than 0 (read: not set)
                if (odom != -1.0F && dutyStatusEvent.getLocation() != null && dutyStatusEvent.getLocation().getOdometerReading() < 0) {
                    dutyStatusEvent.setOdometer(odom);
                }
            }
        }
    }

    /**
     * Base implementation of method that returns a HashMap representing the trip info for a given
     * employee log. Under mandate, this will be a comma delimited list of all distinct values
     * present on eld events on the given log.
     * Under AOBRD, this will be the values present on the log (e.g. current implementation)
     *
     * @param employeeLog Employee Log object to pull trip info from
     * @return HashMap containing HashSet of strings representing each respective field.
     * String Builders will default to empty string ("") if no values are found
     * values are found. Keys are set == to the property name they represent (e.g. ShipmentInfo, TrailerPlate, etc)
     */
    @Override
    public HashMap<EmployeeLog.TripInfoPropertyKey, HashSet<String>> GetTripInfoForLog(EmployeeLog employeeLog) {
        //Construct hashmap to be used by derived class implementations
        HashMap<EmployeeLog.TripInfoPropertyKey, HashSet<String>> tripInfoStringMap = new HashMap<>();

        //Add keys and a new HashSet
        tripInfoStringMap.put(EmployeeLog.TripInfoPropertyKey.TractorNumber, new HashSet<String>());
        tripInfoStringMap.put(EmployeeLog.TripInfoPropertyKey.TrailerNumber, new HashSet<String>());
        tripInfoStringMap.put(EmployeeLog.TripInfoPropertyKey.TrailerPlate, new HashSet<String>());
        tripInfoStringMap.put(EmployeeLog.TripInfoPropertyKey.ShipmentInfo, new HashSet<String>());
        tripInfoStringMap.put(EmployeeLog.TripInfoPropertyKey.VehiclePlate, new HashSet<String>());
        //Return HashMap with keys in place for derived classes
        return tripInfoStringMap;
    }

    @Override
    public EmployeeLogEldEvent[] GetAllEventsByEmployeeLogKey(long employeeLogKey) {
        EmployeeLogEldEvent[] eldEventArray = this.fetchEldEventsByEventTypes((int) employeeLogKey, Arrays.asList(com.jjkeller.kmbapi.employeelogeldevents.Enums.EmployeeLogEldEventType.DutyStatusChange.getValue()), Arrays.asList(com.jjkeller.kmbapi.employeelogeldevents.Enums.EmployeeLogEldEventRecordStatus.Active.getValue()));
        return eldEventArray;
    }

    @Override
    public Date GetEarliestLogDate() {
        List<Date> logDateList = GetLogDateList();
        Date earliestLogDate = DateUtility.getCurrentDateTime().toDate();
        if (logDateList != null && logDateList.size() > 0) {
            earliestLogDate = logDateList.get(logDateList.size() - 1);
        }
        return earliestLogDate;

    }

    /**
     * WARNING.... this should be used when we have timestamps generated from the tab
     * if we dont, we could have a timestamp missmatch and have the tab not return data.
     *
     * @param timestamp
     * @return
     */
    public StatusRecord getStatusRecord(Date timestamp) {
        if (EobrReader.getIsEobrDevicePhysicallyConnected()) {
            StatusRecord statusRecord = new StatusRecord();
            int resultCode = eobrReader.Technician_GetHistoricalData(statusRecord, timestamp);
            if (resultCode == com.jjkeller.kmbapi.kmbeobr.Enums.EobrReturnCode.S_SUCCESS && !statusRecord.IsBlank()) {
                return statusRecord;
            }

        } else {
            Log.e("AOBRDControllerBase", "The Eobr Device is not connected");
        }

        return null;
    }

    public Boolean getDownloadLogSkipped() {
        return _downloadLogSkipped;
    }
}

