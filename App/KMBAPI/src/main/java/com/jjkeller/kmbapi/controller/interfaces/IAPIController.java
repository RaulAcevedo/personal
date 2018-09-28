package com.jjkeller.kmbapi.controller.interfaces;

import android.os.Bundle;

import com.jjkeller.kmbapi.calcengine.Enums;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.LogGridSummary;
import com.jjkeller.kmbapi.controller.calcengine.LogEventSummary;
import com.jjkeller.kmbapi.controller.calcengine.LogSummary;
import com.jjkeller.kmbapi.controller.share.KmbApplicationException;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.employeelogeldevents.Enums.CompositeEmployeeLogEldEventTypeEventCodeEnum;
import com.jjkeller.kmbapi.employeelogeldevents.UnclaimedEventDTO;
import com.jjkeller.kmbapi.enums.CanadaDeferralTypeEnum;
import com.jjkeller.kmbapi.enums.DutyStatusEnum;
import com.jjkeller.kmbapi.enums.RuleSetTypeEnum;
import com.jjkeller.kmbapi.kmbeobr.EventRecord;
import com.jjkeller.kmbapi.kmbeobr.StatusRecord;
import com.jjkeller.kmbapi.kmbeobr.TripReport;
import com.jjkeller.kmbapi.kmbeobr.UnidentifiedPairedEvents;
import com.jjkeller.kmbapi.kmbeobr.VehicleLocation;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEventList;
import com.jjkeller.kmbapi.proxydata.FailureReport;
import com.jjkeller.kmbapi.proxydata.GpsLocation;
import com.jjkeller.kmbapi.proxydata.KMBEncompassUserList;
import com.jjkeller.kmbapi.proxydata.Location;
import com.jjkeller.kmbapi.proxydata.LocationCode;
import com.jjkeller.kmbapi.proxydata.TeamDriver;
import com.jjkeller.kmbapi.employeelogeldevents.Enums.*;
import com.jjkeller.kmbapi.employeelogeldevents.Enums.ActionInitiatingSaveEnum;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Base Interface for an API controller.
 */
public interface IAPIController {
    List<EmployeeLog> Fetch();

    String getOdometerData(EmployeeLog log);

    List<EmployeeLog> EmployeeLogsForDutyStatusReport(Date logDate);

    List<EmployeeLog> EmployeeLogsForDutyFailureReport(Date logDate);

    List<EmployeeLog> EmployeeLogsForReports(boolean convertToDMOFormat,
                                             List<EmployeeLog> localLogList, List<EmployeeLog> serverLogList);

    List<LogEventSummary> LogEventSummaryFor(EmployeeLog empLog);

    List<LogSummary> EmployeeLogsForDailyHoursReport();

    List<LogSummary> EmployeeLogsForDailyHoursRecapReport();

    List<LogSummary> FutureDaysForDailyHoursRecapReport(List<LogSummary> logSummaries);

    List<Date> DownloadRecordsForCompliance(User user, boolean ignoreAllErrors) throws KmbApplicationException;

    Boolean  DownloadLogsWithEditRequests(User user) throws KmbApplicationException;

    KMBEncompassUserList DownloadKMBEncompassUsers() throws KmbApplicationException;

    List<Date> GetMissingLogDateList(User user);

    Bundle GetTripInfo();

    Bundle GetTripInfo(EmployeeLog currentLog);

    EmployeeLog GetEmployeeLog(Date logDate);

    EmployeeLog GetCurrentEmployeeLogByUser(User user);

    EmployeeLog GetEmployeeLog(User user, Date logDate);

    List<EmployeeLog> GetEmployeeLogs(User user, Date logDate);

    EmployeeLog GetLocalEmployeeLogOrCreateNew(User user, Date logDate, DutyStatusEnum selectedDutyStatus);

    EmployeeLog GetLocalEmployeeLogOrCreateTransition(User usr, Date timestampNow);

    void AddLoginEventToCurrentLog(EmployeeLog empLog, DutyStatusEnum dutyStatus);

    boolean HasEldMandateDrivingExceptionEnabled(DutyStatusEnum dutyStatus);

    EmployeeLog CreateNewLogForTransition(User usr, EmployeeLog currentLog, Date timestamp);

    EmployeeLog CreateNewLogForTransition(User usr, EmployeeLog currentLog, Date timestamp, Date potentialDrivingStopTimestamp);

    EmployeeLog GetLocalEmployeeLog(Date logDate);

    EmployeeLog GetLocalEmployeeLog(User user, Date logDate);

    EmployeeLog GetDownloadedEmployeeLog(Date logDate);

    EmployeeLog GetDownloadedEmployeeLog(User user, Date logDate);

    void ChangeRulesetOfEntireLog(Enums.RuleTypeEnum ruleTypeEnum, RuleSetTypeEnum newRuleset);

    void CreateLoginLogoutEvent(CompositeEmployeeLogEldEventTypeEventCodeEnum eventType) throws Throwable;

    boolean IsUSOilFieldOffDutyStatusInLog();

    boolean IsUSOilFieldOffDutyStatusInLog(EmployeeLogEldEvent[] eldEvents);

    boolean isRulesetCombinationAllowed(RuleSetTypeEnum newRuleset);

    boolean isRulesetCombinationAllowed(EmployeeLogEldEvent[] eldEvents, RuleSetTypeEnum newRuleset);

    void ChangeRulesetOfEntireLog(User user, EmployeeLog empLog, Enums.RuleTypeEnum ruleTypeEnum, RuleSetTypeEnum newRuleset);

    void SaveTripInfo(String trailerNumbers, String trailerPlate, String shipmentInfo, String vehiclePlate, boolean returnToWorkLocation, CanadaDeferralTypeEnum canadaDeferralType, boolean isHaulingExplosives, boolean isOperatesSpecificVehicleForOilField, boolean is30MinBreakExempt, String tractorNumbers, String authorityId, String productionId);

    void SaveWeeklyResetUsed(boolean isWeeklyResetUsed);

    boolean IsValidToUseWeeklyResetOnCurrentLog();

    Date GetPreviousWeeklyResetStartTimestamp(Date logDate);

    boolean DoesWeeklyResetExistNewerThan(Date logDate);

    void SaveLocalEmployeeLog(EmployeeLog employeeLog);

    void SaveLocalEmployeeLog(User usr, EmployeeLog employeeLog);

    void CreateOffDutyLog(Date logDate);

    boolean SubmitAllRecords(User submittingUser, boolean excludeTodaysLog);

    boolean SubmitUsersLocalLogs(User user, List<Date> logDatesToSubmit);

    /**
     * Submits all unidentified ELD events to DMO that haven't been submitted yet.
     *
     * @return true if the records were submitted successfully and false otherwise
     */
    List<UnidentifiedPairedEvents> LoadUnidentifiedEldEventPairs(boolean _showAllUnsubmitted);

    List<UnidentifiedPairedEvents> LoadUnreviewedWithoutConfidenceLevelEvents();

    boolean SubmitUnidentifiedEldEvents();

    void SynchronizeUnclaimedEvents(User user) throws KmbApplicationException;

    boolean SubmitMobileDeviceInfo();

    void UpdateTractorNumber(String unitId, EmployeeLog empLog);

    int GetLocalLogCount();

    void ReverseGeocodeLocation(EmployeeLog empLog, GpsLocation gpsLocation);

    void ReverseGeocodeLocationAsync(EmployeeLog empLog, GpsLocation gpsLocation, IReverseGeocodeLocationListener listener);

    void ReverseGeocodeLocation(List<GpsLocation> gpsLocations);

    void ValidateAllReverseGeocoding();

    EmployeeLog getSelectedLogForReport();

    void setSelectedLogForReport(EmployeeLog empLog);

    List<TeamDriver> getTeamDriversOnLog(EmployeeLog empLog);

    List<FailureReport> getFailuresForDutyFailureReport(EmployeeLog empLog);

    boolean getAppRestartFlag();

    void setAppRestartFlag(boolean appRestart);

    List<Date> GetLogDateList();

    Date GetFirstAvailableLogDate();

    EmployeeLog GetEmployeeLogToCertify(Date logDate);

    List<Date> GetLogDateListForReport();

    List<Date> GetUncertifiedLogDates();

    List<Date> GetCertifiedUnsubmittedLogDates();

    List<Date> GetUncertifiedLogDatesExceptToday(User driver);

    List<EmployeeLog> GetLogsWithUnreviewedEdits(User driver);

    List<LocationCode> getLocationCodeListForReport();

    LogGridSummary GetLogGridSummary(EmployeeLog log);

    void AutoPurgeOldRecords(User user);

    boolean IsOdometerCalibrationRequired();

    boolean ValidatedEventsExistForTodaysLog();

    boolean TransitionExemptLogToStandardLogIfNecessary(EmployeeLog log);

    boolean TransitionExemptLogToStandardLogIfNecessary(EmployeeLog log, boolean hasUserBeenNotified);

    boolean IsLogExemptEligible(EmployeeLog log);

    void TransitionPreviousLogsFromExemptToGridLog(List<Date> listOfLogDatesToConvert);

    void NotifyAboutTransitionCurrentLogFromExemptToGrid(EmployeeLog log, boolean hasConvertedCurrentLog, List<Date> logDateList);

    boolean RequestLogsEmail(String emailAddress, Date startDate, Date endDate);

    int getEngineSecondsOffset();

    boolean IsDuplicateEnginePowerUpOrShutDownUnassignedEvent(EventRecord eventRecord, CompositeEmployeeLogEldEventTypeEventCodeEnum eventCode);

    void CreateEnginePowerUpOrShutDownUnassignedEvent(EventRecord eventRecord, CompositeEmployeeLogEldEventTypeEventCodeEnum eventCode, int engineSecondsOffset) throws Throwable;

    void CreateEnginePowerUpOrShutDownEvent(EmployeeLog employeeLog, EventRecord eventRecord, CompositeEmployeeLogEldEventTypeEventCodeEnum eventType) throws Throwable;

    EmployeeLogEldEvent CreateDriveOnOrOffUnassignedEvent(VehicleLocation eventRecord, CompositeEmployeeLogEldEventTypeEventCodeEnum eventCode);

    void SaveDriveOnOrOffUnassignedEvent(EmployeeLogEldEvent event) throws Throwable;

    boolean logHasCanadianRulesets(EmployeeLog log);

    DutyStatusEnum GetLastEventDutyStatus(EmployeeLog empLog);

    void UpdateLoginEvent() throws Throwable;

    void CertifyEmployeeLog(EmployeeLog certificationemployeelog) throws Throwable;

    boolean ManualDutyStatusChangeShouldEndDriving(DutyStatusEnum dutyStatus, Date endOfDrivingTimestamp);

    Date ManualDutyStatusChangeGetTimeOfNewEvent(DutyStatusEnum dutyStatus, Date timeOfNewEvent, Date endOfDrivingTimestamp, EmployeeLogEldEvent lastEvent);

    void CheckForAndCreatePersonalConveyanceChangedEvent(EmployeeLog empLog, Date timestamp, Location location, String EventComment) throws Throwable;

    void CreateDutyStatusChangedEvent(EmployeeLog empLog, Date timestamp, DutyStatusEnum dutyStatus, Location location, boolean isStartTimeValidated, RuleSetTypeEnum ruleset, String logRemark, Date logRemarkDate, boolean isManualDutyStatusChange, String motionPictureProductionId, String motionPictureAuthorityId) throws Throwable;

    void CreateDutyStatusChangedEventForLogin(EmployeeLog empLog, Date timestamp, DutyStatusEnum dutyStatus, Location location, boolean isStartTimeValidated, RuleSetTypeEnum ruleset, String logRemark, Date logRemarkDate, int recordOrigin, String employeeId) throws Throwable;

    void CreateIntermediateEvent(CompositeEmployeeLogEldEventTypeEventCodeEnum eventType, TripReport tripData) throws Throwable;

    void CreateInitialEventForLog(EmployeeLog empLog, DutyStatusEnum dutyStatusEnum, Date timestamp, User currentUser, Location location, RuleSetTypeEnum ruleSetTypeEnum,  String motionPictureProductionId, String motionPictureAuthorityId);

    void CheckForAndCreateYardMoveEvent(EmployeeLog employeeLog, Date timeStampOfEvent, Location location, String annotation) throws Throwable;

    boolean CheckAndHandleDriveOffWhenThereIsANewDutyStatus(DutyStatusEnum currentDutyStatus);

    EmployeeLogEldEvent GetMostRecentLoginEvent(EmployeeLog employeeLog) throws Exception;

    EmployeeLogEldEvent[] fetchEldEventsByEventTypes(int employeeLogKey, List<Integer> eventTypes, List<Integer> eventRecordStatuses);

    EmployeeLogEldEvent fetchEldEventByKey(Integer uniqueKey);

    EmployeeLogEldEvent [] fetchEldPCRecords(Integer uniqueKey);

    EmployeeLogEldEvent saveEldEvent(EmployeeLogEldEvent editedEvent, Date editedEventEndTime) throws Throwable;

    EmployeeLogEldEvent saveEldEvent(EmployeeLogEldEvent editedEvent, SpecialDrivingCategory subCategory, Date editedEventEndTime, ActionInitiatingSaveEnum actionInitiatingSave) throws Throwable;

    boolean reassignEldEventTeamDriving(int reassignEldEventPrimaryKey, int eventCodeInsteadOfDriving, GlobalState.TeamDriverModeEnum teamDriverMode, String kmbUserName, String annotation) throws Throwable;

    List<EmployeeLogEldEvent> getReconcileChangeRequestedEldEvents(int employeeLogKey, Enums.ReconcileChangeRequestedEldEventsEnum reconcileEnum);

    void SaveListInSingleTransaction(EmployeeLogEldEvent[] eldEventList);

    void CreateChangeInDriversIndicationEvent(EmployeeLog employeeLog, Date timeStamp, Location location, String annotation, int eventCode, String logRemark, Integer distance) throws Throwable;

    void CheckForAndCreateEndOfPCYMWT_Event(EmployeeLog employeeLog, Date timestamp, Location location) throws Throwable;

    void CheckForAndCreateEndOfPCYMWT_Event(EmployeeLog employeeLog, Date timestamp, Location location, boolean forMidnightTransition) throws Throwable;

    HashMap<EmployeeLog.TripInfoPropertyKey, HashSet<String>> GetTripInfoForLog(EmployeeLog employeeLog);

    EmployeeLogEldEvent[] GetAllEventsByEmployeeLogKey(long employeeLogKey);

    EmployeeLogEldEvent[] fetchPreviousWeekUnidentifiedDriverEvents(String serialNumber);

    List<UnclaimedEventDTO> fetchUnidentifiedDrivingEventsByELD(String serialNumber, Date LogRangeStartTime);

    Boolean isUnidentifiedDrivingDiagnosticEventByELDActive(String serialNumber, Date LogRangeStartTime);

    List<EmployeeLogEldEvent> fetchUnreviewedUnidentifiedEvents();

    List<EmployeeLogEldEvent> fetchUnreviewedWithoutConfidenceLevelEvents();

    List<EmployeeLogEldEvent> fetchUnsubmittedUnidentifiedEvents();

    Date GetEarliestLogDate();

    void driverElectedToContinueDriving();

    StatusRecord getStatusRecord(Date timestamp);

    Boolean getDownloadLogSkipped();

}
