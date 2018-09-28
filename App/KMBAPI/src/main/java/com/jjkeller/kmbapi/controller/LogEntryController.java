package com.jjkeller.kmbapi.controller;

import android.app.Notification;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.jjkeller.kmbapi.R;
import com.jjkeller.kmbapi.calcengine.OffDuty;
import com.jjkeller.kmbapi.common.TimeKeeper;
import com.jjkeller.kmbapi.configuration.AppSettings;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.configuration.LocationCodeDictionary;
import com.jjkeller.kmbapi.controller.EOBR.EobrEventArgs;
import com.jjkeller.kmbapi.controller.EOBR.EobrGenIIHistoryEventArgs;
import com.jjkeller.kmbapi.controller.EOBR.EobrHistoryEventArgs;
import com.jjkeller.kmbapi.controller.EOBR.EobrReader;
import com.jjkeller.kmbapi.controller.EOBR.EobrReader.ConnectionState;
import com.jjkeller.kmbapi.controller.EOBR.UnassignedDrivingPeriodEventArgs;
import com.jjkeller.kmbapi.controller.asynctask.ClockSyncTask;
import com.jjkeller.kmbapi.controller.dataaccess.EmployeeLogEldEventFacade;
import com.jjkeller.kmbapi.controller.dataaccess.EmployeeLogFacade;
import com.jjkeller.kmbapi.controller.dataaccess.KMBEncompassUserFacade;
import com.jjkeller.kmbapi.controller.interfaces.IAPIController;
import com.jjkeller.kmbapi.controller.interfaces.IDutyStatusChangeEvent;
import com.jjkeller.kmbapi.controller.interfaces.IEmployeeLogFacade;
import com.jjkeller.kmbapi.controller.interfaces.IEobrHistoryChangeEvent;
import com.jjkeller.kmbapi.controller.interfaces.IEobrHistoryChangeEventGenII;
import com.jjkeller.kmbapi.controller.interfaces.IEobrReaderChangeEvent;
import com.jjkeller.kmbapi.controller.interfaces.IEventHandler;
import com.jjkeller.kmbapi.controller.interfaces.IReverseGeocodeLocationListener;
import com.jjkeller.kmbapi.controller.interfaces.ISpecialDrivingController;
import com.jjkeller.kmbapi.controller.share.ControllerBase;
import com.jjkeller.kmbapi.controller.share.CurrentEvent;
import com.jjkeller.kmbapi.controller.share.KmbApplicationException;
import com.jjkeller.kmbapi.controller.share.MandateObjectFactory;
import com.jjkeller.kmbapi.controller.share.SpecialDrivingFactory;
import com.jjkeller.kmbapi.controller.share.UnassignedDrivingPeriodResult;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.controller.utility.EmployeeLogUtilities;
import com.jjkeller.kmbapi.controller.utility.ErrorLogHelper;
import com.jjkeller.kmbapi.controller.utility.NotificationUtilities;
import com.jjkeller.kmbapi.employeelogeldevents.EmployeeLogEldEventCode;
import com.jjkeller.kmbapi.employeelogeldevents.Enums;
import com.jjkeller.kmbapi.enums.DataProfileEnum;
import com.jjkeller.kmbapi.enums.DutyStatusEnum;
import com.jjkeller.kmbapi.enums.EmployeeLogProvisionTypeEnum;
import com.jjkeller.kmbapi.enums.ExemptLogTypeEnum;
import com.jjkeller.kmbapi.enums.RuleSetTypeEnum;
import com.jjkeller.kmbapi.enums.TimeZoneEnum;
import com.jjkeller.kmbapi.kmbeobr.Enums.DeviceErrorFlags;
import com.jjkeller.kmbapi.kmbeobr.Enums.EobrReturnCode;
import com.jjkeller.kmbapi.kmbeobr.EventRecord;
import com.jjkeller.kmbapi.kmbeobr.EventTypeEnum;
import com.jjkeller.kmbapi.kmbeobr.StatusRecord;
import com.jjkeller.kmbapi.kmbeobr.TripReport;
import com.jjkeller.kmbapi.proxydata.CompanyConfigSettings;
import com.jjkeller.kmbapi.proxydata.EldEventAdapterList;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;
import com.jjkeller.kmbapi.proxydata.EngineRecord;
import com.jjkeller.kmbapi.proxydata.EobrConfiguration;
import com.jjkeller.kmbapi.proxydata.GpsLocation;
import com.jjkeller.kmbapi.proxydata.KMBEncompassUserList;
import com.jjkeller.kmbapi.proxydata.Location;
import com.jjkeller.kmbapi.realtime.malfunction.MalfunctionRealtimeManager;

import org.greenrobot.eventbus.EventBus;
import org.joda.time.DateTime;
import org.joda.time.Days;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class LogEntryController extends ControllerBase{

    private IDutyStatusChangeEvent _dutyStatusChangeEventHandler = null;
    private int _eobrIntervalMS;
    protected static final int HOURS_PER_DAY = 24;
    private static final int MAP_POSITION_INTERVAL = 5;
    private String loggingDeviceDisconnectedMessage;
    private String loggingDeviceDisconnectedMessage2;
    private String loggingDeviceOnlineMessage;
    private String readingHistoryMessage;
    private String loggingDeviceFailureMessage;
    private String loggingDeviceFirmwareUpdateMessage;



    public void DownloadKMBEncompassUsers(){
        IAPIController empLogController = MandateObjectFactory.getInstance(this.getContext(),GlobalState.getInstance().getFeatureService()).getCurrentEventController();
        KMBEncompassUserList lst= new KMBEncompassUserList();
        try {
            lst = empLogController.DownloadKMBEncompassUsers();
        }
        catch(Throwable e){
        }
        KMBEncompassUserFacade facade = new KMBEncompassUserFacade(this.getContext());
        if (lst.getEncompassUserList()!=null && lst.getEncompassUserList().length>0)
            for (int i=0; i<lst.getEncompassUserList().length; i++)
                facade.Save(lst.getEncompassUserList()[i]);
    }

    public LogEntryController(Context ctx){
        super(ctx);
        _eobrIntervalMS = new AppSettings().getEobrIntervalMS();
        if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
            loggingDeviceDisconnectedMessage = ctx.getString(R.string.msgelddisconnected);
            loggingDeviceDisconnectedMessage2 = ctx.getString(R.string.msgelddisconnected2);
            loggingDeviceOnlineMessage = ctx.getString(R.string.msgeldonline);
            readingHistoryMessage = ctx.getString(R.string.msgeldreadinghistory);
            loggingDeviceFailureMessage = ctx.getString(R.string.msgelddevicefailure);
            loggingDeviceFirmwareUpdateMessage = ctx.getString(R.string.msgeldfirmwareupdate);
        } else {
            loggingDeviceDisconnectedMessage = ctx.getString(R.string.msgeobrdisconnected);
            loggingDeviceDisconnectedMessage2 = ctx.getString(R.string.msgeobrdisconnected2);
            loggingDeviceOnlineMessage = ctx.getString(R.string.msgeobronline);
            readingHistoryMessage = ctx.getString(R.string.msgeobrreadinghistory);
            loggingDeviceFailureMessage = ctx.getString(R.string.msgeobrdevicefailure);
            loggingDeviceFirmwareUpdateMessage = ctx.getString(R.string.msgeobrfirmwareupdate);
        }
    }

    /// <summary>
    /// Download all of the recent logs for the current employee.
    /// Answer if the employee is in compliance, from an audit perspective.
    /// </summary>
    /// <returns></returns>
    public List<Date> DownloadRecordsForCompliance(User user) throws KmbApplicationException
    {
        IAPIController empLogController = MandateObjectFactory.getInstance(this.getContext(),GlobalState.getInstance().getFeatureService()).getCurrentEventController();
        return empLogController.DownloadRecordsForCompliance(user, true);
    }


    public EmployeeLog getCurrentEmployeeLog() {
        return getCurrentEmployeeLog(null, null);
    }

    /// Answer the current employee log being processed.
    /// If a log has not already been started for the day, then create one.
    public EmployeeLog getCurrentEmployeeLog(DutyStatusEnum dutyStatusEnum, DutyStatusEnum initialDutyStatus)  {
        // try to read the log from state
        EmployeeLog log = GlobalState.getInstance().getCurrentEmployeeLog();

        // 11/3/14 JHM - Do this check for when date is changed backwards in settings and we end up with no log events in the collection.
        if(log != null && log.getEldEventList() != null
                && log.getEldEventList().getEldEventList(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus) != null
                && log.getEldEventList().getEldEventList(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus).length == 0
                && this.getCurrentClockHomeTerminalTime().compareTo(log.getLogDate()) < 0)
        {
            // Delete the log - The current log has no events and is a future date
            IEmployeeLogFacade empLogFacade = new EmployeeLogFacade(this.getContext(), this.getCurrentUser());
            String logMessage = String.format("Deleting EmployeeLog at:{%s}, Reasons: No log events, Future log date.", log.getLogDate());
            Log.v("EmployeeLog", logMessage);
            ErrorLogHelper.RecordMessage(logMessage);
            empLogFacade.DeleteLog(log);
            log = null;
        }

        if (log == null)
        {
            // not found in state
            if(!GlobalState.getInstance().getIsViewOnlyMode())
            {
                // need to find (if it exists already), or create one
                if(dutyStatusEnum != null) {
                    log = this.CreateCurrentLog(initialDutyStatus, dutyStatusEnum, new ExemptLogTypeEnum(ExemptLogTypeEnum.NULL));
                } else {
                    log = this.CreateCurrentLog();
                }
            } else
            {
                //if in view only mode then look for the most recent
                //log that we have, within the bounds of whatever ruleset the user has
                //(if the user has canadian rules available to them, just assume the last log is canadian... look back 14 days)
                IAPIController logController = MandateObjectFactory.getInstance(this.getContext(),GlobalState.getInstance().getFeatureService()).getCurrentEventController();
                List<Date> logDateList = logController.GetLogDateListForReport();
                Date logDate = null;

                //the log list should already be sorted in descending order
                if(logDateList != null && logDateList.size() > 0)
                {
                    logDate = logDateList.get(0);

                    if(logDate != null)
                    {
                        List<EmployeeLog> logList = logController.EmployeeLogsForDutyStatusReport(logDate);

                        if(logList != null && logList.size() > 0)
                            log = logList.get(0);
                    }
                }

                //now check to see if the last log really was canadian...
                //if not, and it's older than 7 days, discard it
                if(log != null && !log.getRuleset().isCanadianRuleset())
                {
                    Date cutOff = getCurrentClockHomeTerminalTime();
                    cutOff = EmployeeLogUtilities.CalculateLogStartTime(this.getContext(), cutOff, this.getCurrentUser().getHomeTerminalTimeZone());
                    cutOff = DateUtility.AddDays(cutOff, -7);

                    //if older than 7 days ago, discard.
                    if(log.getLogDate().compareTo(cutOff) < 0)
                        log = null;
                }
            }

            // add it to state
            this.setCurrentEmployeeLog(log);
        }

        return log;
    }

    public void setCurrentEmployeeLog(EmployeeLog log)
    {
        GlobalState.getInstance().setCurrentEmployeeLog(log);

        // if the current user is also the driver, then update the drivers log as well
        // this is needed so that the EOBR changes are recorded correctly
        if (this.IsCurrentUserTheDriver())
        {
            GlobalState.getInstance().setCurrentDriversLog(log);
        }
    }


    /// <summary>
    /// Answer the current designated drivers log being processed.
    /// </summary>
    public EmployeeLog getCurrentDriversLog()
    {
        // try to read the log from state
        EmployeeLog empLog = GlobalState.getInstance().getCurrentDriversLog();

        if (empLog == null || empLog.getEmployeeId().compareTo(this.getCurrentDesignatedDriver().getCredentials().getEmployeeId()) != 0)
        {
            // either the log has not been loaded yet, or it's for the wrong user
            // 9/21/11 JHM - getLogDate from getCurrentEmployeeLog and not GlobalState
            Date logDate = this.getCurrentEmployeeLog().getLogDate();
            IAPIController logController = MandateObjectFactory.getInstance(this.getContext(),GlobalState.getInstance().getFeatureService()).getCurrentEventController();
            empLog = logController.GetLocalEmployeeLog(this.getCurrentDesignatedDriver(), logDate);
            this.setCurrentDriversLog(empLog);
        }

        return empLog;
    }

    public String getCurrentEobrSerialNumber(){
        return GlobalState.getInstance().getCurrentEobrSerialNumber();
    }

    private Date getNextEobrReaderEventProcessingTimestamp(){
        return GlobalState.getInstance().getNextEobrReaderEventProcessingTimestamp();
    }
    private void setNextEobrReaderEventProcessingTimestamp(Date nextEobrReaderEventProcessingTimestamp){
        GlobalState.getInstance().setNextEobrReaderEventProcessingTimestamp(nextEobrReaderEventProcessingTimestamp);
    }

    public VehicleMotionDetector getVehicleMotionDetector(){
        return GlobalState.getInstance().getVehicleMotionDetector(getContext());
    }
    public void setVehicleMotionDetector(VehicleMotionDetector detector){
        GlobalState.getInstance().setVehicleMotionDetector(detector);
    }

    public void RegisterForUpdates(Context ctx, IDutyStatusChangeEvent dutyStatusChangeEventHandler, IEobrReaderChangeEvent eobrReaderChangeEventHandler,
                                   IEobrHistoryChangeEvent eobrHistoryChangeEventHandler, IEobrHistoryChangeEventGenII eobrHistoryChangeEventHandlerGenII,
                                   IEventHandler<UnassignedDrivingPeriodEventArgs> unassignedDrivingPeriodEventHandler)
    {
        this._dutyStatusChangeEventHandler = dutyStatusChangeEventHandler;

        EobrReader eobrReader = EobrReader.getInstance();
        eobrReader.setEobrReaderChangeEventHandler(eobrReaderChangeEventHandler);
        eobrReader.setEobrHistoryChangeEventHandler(eobrHistoryChangeEventHandler);
        eobrReader.setEobrHistoryChangeEventHandlerGenII(eobrHistoryChangeEventHandlerGenII);
        eobrReader.setUnassignedDrivingPeriodEventHandler(unassignedDrivingPeriodEventHandler);


        new ClockSyncTask(eobrReader, ctx).execute();

        // compare login/logout times for consistency
        FailureController ctrlr = new FailureController(ctx);
        ctrlr.VerifyLoginTimeConsistency(ctx, this.getCurrentUser());

        this.SetLastEobrOdometer();
    }

    /**
     * Perform the process of manually entering an event into the current log.
     *
     * @param timeOfNewEvent The time of the new event
     * @param dutyStatus     The duty status to change to
     * @param location       The location description
     * @param eventComment   The event comment
     */
    public void PerformManualStatusChange(Date timeOfNewEvent, DutyStatusEnum dutyStatus, String location, String eventComment) {
        this.PerformManualStatusChange(timeOfNewEvent, dutyStatus, location, null, null, eventComment);
    }

    /**
     * Perform the process of manually entering an event into the current log.
     *
     * @param timeOfNewEvent The time of the new event
     * @param dutyStatus     The duty status to change to
     * @param location       The location description
     * @param latitude       The latitude to use for the location (debug only)
     * @param longitude      The longitude to use for the location (debug only)
     * @param eventComment   The event comment
     */
    public void PerformManualStatusChange(Date timeOfNewEvent, DutyStatusEnum dutyStatus, String location, String latitude, String longitude, String eventComment) {
        this.PerformManualStatusChange(timeOfNewEvent, dutyStatus, location, latitude, longitude, eventComment, null, null, null);
    }

    /**
     * Perform the process of manually entering an event into the current log.
     *
     * @param timeOfNewEvent            The time of the new event
     * @param dutyStatus                The duty status to change to
     * @param location                  The location description
     * @param latitude                  The latitude to use for the location (debug only)
     * @param longitude                 The longitude to use for the location (debug only)
     * @param eventComment              The event comment
     * @param motionPictureProductionId The name of event's designated Production
     * @param motionPictureAuthorityId  The name of event's designated DOT Authority
     */
    public void PerformManualStatusChange(Date timeOfNewEvent, DutyStatusEnum dutyStatus, String location, String latitude, String longitude, String eventComment, String motionPictureProductionId, String motionPictureAuthorityId, DutyStatusEnum initialDutyStatus) {
        PerformManualStatusChange(timeOfNewEvent, dutyStatus, location, latitude, longitude, eventComment, motionPictureProductionId, motionPictureAuthorityId, initialDutyStatus, null);
    }

    /***
     * Perform the process of manually entering an event into the specified log
     * @param timeOfNewEvent            The time of the new event
     * @param dutyStatus                The duty status to change to
     * @param location                  The location description
     * @param latitude                  The latitude to use for the location (debug only)
     * @param longitude                 The longitude to use for the location (debug only)
     * @param eventComment              The event comment
     * @param motionPictureProductionId The name of event's designated Production
     * @param motionPictureAuthorityId  The name of event's designated DOT Authority
     * @param empLog                    The log that should have the status change
     */
    public void PerformManualStatusChange(Date timeOfNewEvent, DutyStatusEnum dutyStatus, String location, String latitude, String longitude, String eventComment, String motionPictureProductionId, String motionPictureAuthorityId, DutyStatusEnum initialDutyStatus, EmployeeLog empLog)
    {
        IAPIController empLogEventController = MandateObjectFactory.getInstance(this.getContext(),GlobalState.getInstance().getFeatureService()).getCurrentEventController();
        if(empLog == null) {
            empLog = this.getCurrentEmployeeLog(dutyStatus, initialDutyStatus);
        }

        // verify that the vehicle did not just recently come to a stop
        EmployeeLogEldEvent lastEvent = EmployeeLogUtilities.GetLastEventInLog(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus, empLog);
        Date endOfDrivingTimestamp = null;

        if( IsDrivingOrHyrail(lastEvent) || IsDrivingOrNonRegulatedDriving(lastEvent)){
            // for gen 1 eobr, check vehicle motion detector
            if (EobrReader.getInstance().getEobrGeneration() == 1)
            {
                // if there is an unconfirmed stop in the VMD, then use the potential stop timestamp from the VMD for the new
                // duty status.  This is done so that the driving period is ended at the correct time.
                VehicleMotionDetector vmd = this.getVehicleMotionDetector();
                if (!this.getHasExtendedDrivingSegment() && vmd.getPotentialDrivingStopTimestamp() != null)
                {
                    endOfDrivingTimestamp = DateUtility.ConvertToPreviousLogEventTime(vmd.getPotentialDrivingStopTimestamp());
                }
            }
            // else, gen 2 eobr, check potential stop timestamp to determine when driving stopped
            else
            {
                if (!this.getHasExtendedDrivingSegment() && GlobalState.getInstance().getPotentialDrivingStopTimestamp() != null)
                {
                    endOfDrivingTimestamp = DateUtility.ConvertToPreviousLogEventTime(GlobalState.getInstance().getPotentialDrivingStopTimestamp());
                }
            }

            // 2014.04.17 sjn - the user has just manually ended a driving status
            //                  keep track of the endOfDrivingTime because if the vehicle starts moving soon enough, we'll want to put the log back in driving status
            String msg = String.format("LogEventController.PerformManualStatusChange user ended DRV status at endOfDrivingTimestamp: {%s}", endOfDrivingTimestamp);
            Log.v("LogEvent", msg);
            ErrorLogHelper.RecordMessage(this.getContext(), msg);
            GlobalState.getInstance().setSavedManualDrivingStopTimestamp(endOfDrivingTimestamp);

            // when a manual status change occurs directly after a driving period
            // then make sure to drop the location because there should be
            // a new location as a result of the driving action
            // For mandate, a manual location is sometimes entered on a manual duty status change, so don't clear it.
            if (!GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
                location = null;
            }
        }

        Location newLoc = new Location(location);

        // attempt to put in last known values for odometer and GPS into the location
        newLoc.setOdometerReading(this.getLastValidOdometerReading());

        // DEBUG Code - if gps coordinates passed in, use those
        if (latitude != null && longitude != null)
        {
            newLoc.setGpsInfo(new GpsLocation(TimeKeeper.getInstance().now(), Float.valueOf(latitude), Float.valueOf(longitude)));
            // END Debug Code
        }
        else
        {
            newLoc.setGpsInfo(this.getLastValidGPSLocation());
        }

        // attempt to have the location geocoded with the correct location name
        empLogEventController.ReverseGeocodeLocation(empLog, newLoc.getGpsInfo());


        EmployeeLog currentLog = this.getCurrentEmployeeLog(dutyStatus, initialDutyStatus);
        boolean isCurrentOrFutureLog = !empLog.getLogDate().before(currentLog.getLogDate());

        if (empLogEventController.ManualDutyStatusChangeShouldEndDriving(dutyStatus, endOfDrivingTimestamp)) {
            // first add an On-Duty event to end the DRV at the correct time
            endOfDrivingTimestamp = DateUtility.ConvertToPreviousLogEventTime(endOfDrivingTimestamp);
            empLog = this.PerformStatusChange(this.getCurrentUser(), empLog, endOfDrivingTimestamp, new DutyStatusEnum(DutyStatusEnum.ONDUTY), newLoc, isCurrentOrFutureLog, true, eventComment, motionPictureProductionId, motionPictureAuthorityId);
        }
        timeOfNewEvent = empLogEventController.ManualDutyStatusChangeGetTimeOfNewEvent(dutyStatus, timeOfNewEvent, endOfDrivingTimestamp, lastEvent);

        if(empLog.getMobileStartTimestamp() == null || timeOfNewEvent.compareTo(empLog.getMobileStartTimestamp()) < 0)
            empLog.setMobileStartTimestamp(timeOfNewEvent);


        this.EndSpecialDriving();
        // add the new duty status event to the log
        empLog = this.PerformStatusChange(this.getCurrentUser(), empLog, timeOfNewEvent, dutyStatus, newLoc, isCurrentOrFutureLog, true, eventComment, motionPictureProductionId, motionPictureAuthorityId);

        //don't set a previous log as the current log
        if(isCurrentOrFutureLog) {
            //re-set the current log on this controller
            this.setCurrentEmployeeLog(empLog);
            this.setLogEventForEdit(EmployeeLogUtilities.GetLastEventInLog(empLog));
        }
        // reset the motion sensor when a manual status occurs
        // this is because a manual status change indicates the
        // end of a driving period.
        // resetting the VMD may be redundant, but it should be safe in all cases
        if( dutyStatus.getValue() != DutyStatusEnum.DRIVING)
            this.setVehicleMotionDetector(null);

        // reset the manual driving segment extension flags
        this.setIsExtendDrivingSegmentEnabled(false);
        this.setHasExtendedDrivingSegment(false);

    }

    public EmployeeLog PerformStatusChange(User user, EmployeeLog empLog, Date timestampOfNewEvent, DutyStatusEnum dutyStatus, Location location)
    {
        return this.PerformStatusChange(user, empLog, timestampOfNewEvent, dutyStatus, location, true);
    }

    public EmployeeLog PerformStatusChange(User user, EmployeeLog empLog, Date timestampOfNewEvent, DutyStatusEnum dutyStatus, Location location, boolean publishDutyStatusChange)
    {
        return this.PerformStatusChange(user, empLog, timestampOfNewEvent, dutyStatus, location, publishDutyStatusChange, false, "");
    }

    public EmployeeLog PerformStatusChange(User user, EmployeeLog empLog, Date timestampOfNewEvent, DutyStatusEnum dutyStatus, Location location, boolean publishDutyStatusChange, boolean manual, String eventComment)
    {
        return this.PerformStatusChange(user, empLog, timestampOfNewEvent, dutyStatus, location, publishDutyStatusChange, manual, eventComment, null, null);
    }

    public EmployeeLog PerformStatusChange(User user, EmployeeLog empLog, Date timestampOfNewEvent, DutyStatusEnum dutyStatus, Location location, boolean publishDutyStatusChange, boolean manual, String eventComment, String motionPictureProductionId, String motionPictureAuthorityId)
    {
        return this.PerformStatusChange(user, empLog, timestampOfNewEvent, dutyStatus, location, publishDutyStatusChange, manual, "", eventComment, motionPictureProductionId, motionPictureAuthorityId);
    }

    private EmployeeLog PerformStatusChange(User user, EmployeeLog empLog, Date timestampOfNewEvent, DutyStatusEnum dutyStatus, Location location, boolean publishDutyStatusChange,
                                            boolean isManualDutyStatusChange, String currEobrSerialNumber, String eventComment, String motionPictureProductionId, String motionPictureAuthorityId) {

        IAPIController empLogEventController = MandateObjectFactory.getInstance(this.getContext(), GlobalState.getInstance().getFeatureService()).getCurrentEventController();

        // Determine if Event is from before current log.  If so, don't add it to the log
        Date logStartTime = EmployeeLogUtilities.CalculateLogStartTime(this.getContext(), empLog.getLogDate(), user.getHomeTerminalTimeZone());
        Date eventDate = EmployeeLogUtilities.CalculateLogStartTime(this.getContext(), timestampOfNewEvent, user.getHomeTerminalTimeZone());

        if (eventDate.before(logStartTime)) {
            String debugMessage = String.format("Attempted to add Log Event ds: '{%s}' From '{%s}' to Current Log '{%s}'",
                    dutyStatus.getString(getContext()),
                    DateUtility.getHomeTerminalDateTimeFormat().format(timestampOfNewEvent),
                    DateUtility.getHomeTerminalDateTimeFormat().format(empLog.getLogDate()));
            Log.d("LogEvent", debugMessage);
            ErrorLogHelper.RecordMessage(getContext(), debugMessage);

            Date previousLogStartTime = DateUtility.AddDays(logStartTime, -1);
            Date previousLogDate = DateUtility.AddDays(empLog.getLogDate(), -1);
            EmployeeLog previousLog = empLogEventController.GetLocalEmployeeLog(user, previousLogDate);

            boolean allowAddToPriorLog = this.checkForOnDutyTransitionAcrossMidnight(empLog, previousLog, previousLogStartTime, dutyStatus, timestampOfNewEvent, eventDate);

            if(allowAddToPriorLog) {
                return PerformStatusChangeOnPreviousAndCurrentLog(user, empLog, timestampOfNewEvent, dutyStatus, location, publishDutyStatusChange, isManualDutyStatusChange,
                        currEobrSerialNumber, eventComment, motionPictureProductionId, motionPictureAuthorityId, previousLog, logStartTime, empLogEventController);
            } else {
                return empLog;
            }
        } else {
            return PerformStatusChange(user, empLog, timestampOfNewEvent, dutyStatus, location, publishDutyStatusChange, isManualDutyStatusChange,
                    currEobrSerialNumber, eventComment, motionPictureProductionId, motionPictureAuthorityId, empLogEventController);
        }
    }

    private EmployeeLog PerformStatusChangeOnPreviousAndCurrentLog(User user, EmployeeLog empLog, Date timestampOfNewEvent, DutyStatusEnum dutyStatus, Location location, boolean publishDutyStatusChange,
                                            boolean isManualDutyStatusChange, String currEobrSerialNumber, String eventComment, String motionPictureProductionId, String motionPictureAuthorityId) {
        IAPIController empLogEventController = MandateObjectFactory.getInstance(this.getContext(), GlobalState.getInstance().getFeatureService()).getCurrentEventController();
        Date previousLogDate = DateUtility.AddDays(empLog.getLogDate(), -1);
        EmployeeLog previousLog = empLogEventController.GetLocalEmployeeLog(user, previousLogDate);
        Date empLogStartTime = EmployeeLogUtilities.CalculateLogStartTime(this.getContext(), empLog.getLogDate(), user.getHomeTerminalTimeZone());

        return PerformStatusChangeOnPreviousAndCurrentLog(user, empLog, timestampOfNewEvent, dutyStatus, location, publishDutyStatusChange, isManualDutyStatusChange,
                currEobrSerialNumber, eventComment, motionPictureProductionId, motionPictureAuthorityId, previousLog, empLogStartTime, empLogEventController);
    }

    private EmployeeLog PerformStatusChangeOnPreviousAndCurrentLog(User user, EmployeeLog empLog, Date timestampOfNewEvent, DutyStatusEnum dutyStatus, Location location, boolean publishDutyStatusChange,
                                            boolean isManualDutyStatusChange, String currEobrSerialNumber, String eventComment, String motionPictureProductionId, String motionPictureAuthorityId,
                                            EmployeeLog previousLog, Date empLogStartTime, IAPIController empLogEventController) {
        if(previousLog != null) {
            //perform the status change on the previous log
            PerformStatusChange(user, previousLog, timestampOfNewEvent, dutyStatus, location);

            //perform the status change at the beginning of the current log
            timestampOfNewEvent = empLogStartTime;
            location.setOdometerReading(location.getEndOdometerReading());
            location.setEndOdometerReading(-1);
            return PerformStatusChange(user, empLog, timestampOfNewEvent, dutyStatus, location, publishDutyStatusChange, isManualDutyStatusChange,
                    currEobrSerialNumber, eventComment, motionPictureProductionId, motionPictureAuthorityId, empLogEventController);
        } else {
            return empLog;
        }
    }
    /**
     * Perform the status change by adding a new event to the current log.
     * Publish the new status so that the UI can update correctly.
     * All event changes to the log go through this routine, both manual (from the user) and automatic (from the ELD).
     * @param user user that is changing status
     * @param empLog log that the status change is for
     * @param timestampOfNewEvent timestamp of the new log event, in Home Terminal Timezone units
     * @param dutyStatus duty status of the new log event
     * @param location location of the status change
     * @param publishDutyStatusChange whether or not the duty status should be published to the UI
     * @param isManualDutyStatusChange true if the change was manually done by the user, or false if it was from the ELD
     * @return the employee log after the change is processed
     */
    private EmployeeLog PerformStatusChange(User user, EmployeeLog empLog, Date timestampOfNewEvent, DutyStatusEnum dutyStatus, Location location, boolean publishDutyStatusChange,
                                            boolean isManualDutyStatusChange, String currEobrSerialNumber, String eventComment, String motionPictureProductionId, String motionPictureAuthorityId,
                                            IAPIController empLogEventController) {
        HosAuditController auditCtrlr = new HosAuditController(this.getContext());

        // determine if this event belongs on a future log
        empLog = this.CreateNewLogIfNecessary(user, empLog, timestampOfNewEvent);

        if (dutyStatus.getValue() == DutyStatusEnum.DRIVING) {
            //Make sure all log events that occur after this new event are removed from the log.  This happens when a manual duty status change
            //was done after the vehicle started moving.  But the drive on event didn't happen.  We need to remove this event to enable the driving
            //event to show correctly in the log.
            EmployeeLogUtilities.RemoveAllEventsAfter(empLog, timestampOfNewEvent, true);
        }

        // if the last status event on the log is driving, and a new status comes through,
        // then reset the motion sensor
        float currentOdometer = this.getLastValidOdometerReading();
        EmployeeLogEldEvent previousEvent = EmployeeLogUtilities.GetLastEventInLog(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus,empLog);
        if (previousEvent.getDutyStatusEnum().getValue() == DutyStatusEnum.DRIVING) {
            // since a driving period just ended,
            // need to save the mileage driven in the log

            // first, determine how much mileage occurred during the driving period
            float startOdometer = previousEvent.getLocation().getOdometerReading();
            float distanceInDrivingPeriod = currentOdometer - startOdometer;
            if (startOdometer >= 0 && currentOdometer > 0 && distanceInDrivingPeriod > 0) {

                // update the ending odometer of the driving segment
                if (previousEvent.getEobrSerialNumber() == currEobrSerialNumber)
                    previousEvent.getLocation().setEndOdometerReading(currentOdometer);

                String debugMessage = String.format("PerformStatusChange ts: '{%s}' ds: '{%s}' drv start '{%s}' drv startOdom '{%f}' drv endOdom '{%f}' curOdom '{%f}' dist '{%f}' mobileDerivedDist '{%f}' location:'{%s}'.", DateUtility.getHomeTerminalDateTimeFormat().format(timestampOfNewEvent), dutyStatus.getString(getContext()), DateUtility.getHomeTerminalDateTimeFormat12Hour().format(previousEvent.getStartTime()), previousEvent.getLocation().getOdometerReading(), previousEvent.getLocation().getEndOdometerReading(), currentOdometer, distanceInDrivingPeriod, empLog.getMobileDerivedDistance(), location != null ? location.ToLocationString() : "");
                Log.d("LogEvent", debugMessage);
                ErrorLogHelper.RecordMessage(getContext(), debugMessage);
            }

            // as long the new status is not driving, then reset the motion detector
            if (dutyStatus.getValue() != DutyStatusEnum.DRIVING) {
                this.setVehicleMotionDetector(null);
            }

            auditCtrlr.MarkEndOfDrivingPeriod(timestampOfNewEvent);
            auditCtrlr.MarkEndOfRestBreakPeriod(timestampOfNewEvent);

            // Cancel the HOURSWARNING Notification
            NotificationUtilities.CancelNotification(this.getContext(), NotificationUtilities.HOURSWARNING_ID);
        }

        // since the previous event is ending, add the odometer to it.
        if (previousEvent.getEobrSerialNumber() == currEobrSerialNumber)
            previousEvent.getLocation().setEndOdometerReading(currentOdometer);

        if (dutyStatus.getValue() == DutyStatusEnum.DRIVING || previousEvent.getDutyStatusEnum().getValue() == DutyStatusEnum.DRIVING) {
            // just started a new driving segment, or just ended a driving segment
            // force the route controller to process the location and create a GPS tracking
            // entry for the boundary of the driving segment

            if (location != null && EobrReader.getInstance() != null) {
                RouteController routeCntrlr = new RouteController(getContext());
                routeCntrlr.ProcessNewLocation(EobrReader.getInstance().getEobrSerialNumber(), EobrReader.getInstance().getEobrIdentifier(), location.getGpsInfo(), location.getOdometerReading(), true);
            }
        }

        if (empLogEventController.HasEldMandateDrivingExceptionEnabled(dutyStatus)) {
            // we will not be recording driving events when in yard move duty status or pc duty status
            PublishDrivingDutyStatusChange(empLog.getLogDate(), timestampOfNewEvent, location, empLog.getRuleset());
        } else {
            // add the new event to the current log being processed
            FailureController ctrlr = new FailureController(this.getContext());
            String debugMessage = String.format("Attempting to add Log Event to the log %s %s %s :: Log %s :: timekeeper.offset {%s}", timestampOfNewEvent, dutyStatus.getString(getContext()), location == null ? "" : location.ToLocationString(), empLog.getLogDate(), DateUtility.ClockTimestampOffset());
            ErrorLogHelper.RecordMessage(this.getContext(), debugMessage);
            Log.d("LogEvent", debugMessage);

            try {
                empLogEventController.CreateDutyStatusChangedEvent(empLog, timestampOfNewEvent, dutyStatus, location, ctrlr.getIsMobileClockSynchronized(), user.getRulesetTypeEnum(), null, null, isManualDutyStatusChange, motionPictureProductionId, motionPictureAuthorityId);
            } catch (Throwable e) {
                Log.e("ELD Mandate Duty Error", "Did not save eld mandate duty status change due to exception.", e);
            }

            try {
                empLogEventController.CheckForAndCreateEndOfPCYMWT_Event(empLog, timestampOfNewEvent, location);
            } catch (Throwable e) {
                Log.e("ELD Mandate Duty Error", "Did not save eld mandate personal conveyance change due to exception.", e);
            }

            try {
                empLogEventController.CheckForAndCreatePersonalConveyanceChangedEvent(empLog, timestampOfNewEvent, location, eventComment);
            } catch (Throwable e) {
                Log.e("ELD Mandate Duty Error", "Did not save eld mandate personal conveyance change due to exception.", e);
            }

            try {
                empLogEventController.CheckForAndCreateYardMoveEvent(empLog, timestampOfNewEvent, location, eventComment);
            } catch (Throwable e) {
                Log.e("ELD Mandate Duty Error", "Did not save eld mandate yard move event due to exception.", e);
            }
            // Alex: (2/7/17)  The current log was not marked as un-certified after adding new events
            empLog.setIsCertified(false);

            // persist the changes to the current day's log, just in case they have not been committed
            empLogEventController.SaveLocalEmployeeLog(user, empLog);

            if (dutyStatus.getValue() == DutyStatusEnum.DRIVING && this.IsTheDriver(user) && this.IsTheCurrentActiveUser(user)) {
                // new duty status is driving
                // alert the audit controller to start tracking a new period
                auditCtrlr.MarkBeginningOfDrivingPeriod(timestampOfNewEvent);
                auditCtrlr.MarkBeginningOfRestBreakPeriod(timestampOfNewEvent);
            }

            if(empLog.getExemptLogType().getValue() == ExemptLogTypeEnum.EXEMPTLOGTYPE100AIRMILE)
            {
                //for logs that are 100 air mile exempt, then mark the beginning or end of the duty period
                if(dutyStatus.isExemptOnDutyStatus())
                    auditCtrlr.MarkBeginningOfExemptDutyPeriod();

                if(dutyStatus.isExemptOffDutyStatus())
                    auditCtrlr.MarkEndOfExemptDutyPeriod();
            }

            // publish the status change to all the delegates
            if (publishDutyStatusChange)
                this.PublishDutyStatusChange(empLog.getLogDate(), this.getLogEventForEdit());
        }
        return empLog;
    }

    /***
     * Typically events don't get added to prior logs.
     * One exception is if the driver stopped driving before midnight and the On Duty isn't processed until after midnight because of driving stop time.
     * The event should only be added to the prior day's log if the following conditions are met:
     * <ul>
     * 	<li>The event to be added is an On Duty event</li>
     * 	<li>The event to be added is for yesterday's log (no more than 1 day)</li>
     * 	<li>Yesterday's log ends with a Driving event</li>
     * 	<li>The current day's log only has one event and the event is a Driving event, so driving has definitely gone over midnight</li>
     * </ul>
     * @param empLog the current log
     * @param previousLog the previous day's log
     * @param previousLogStartTime the start time of the previous log
     * @param dutyStatus the duty status to be added
     * @param timestampOfNewEvent the timestamp of the event to be added
     * @param newEventStartTime the timestamp of timestampOfNewEvent, converted to the driver's timezone
     * @return true if adding to the prior log should be allowed
     */
    private boolean checkForOnDutyTransitionAcrossMidnight(EmployeeLog empLog, EmployeeLog previousLog, Date previousLogStartTime,
                                                           DutyStatusEnum dutyStatus, Date timestampOfNewEvent, Date newEventStartTime) {
        boolean allowAddToPriorLog = false;
        if(previousLog == null) {
            return allowAddToPriorLog;
        }

        EmployeeLogEldEvent[] currentLogEvents = empLog.getEldEventList().getEldEventList(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus);
        if (dutyStatus.getValue() == DutyStatusEnum.ONDUTY && currentLogEvents.length == 1 && currentLogEvents[0].getDutyStatusEnum().getValue() == DutyStatusEnum.DRIVING)
        {
            if (!newEventStartTime.before(previousLogStartTime))
            {
                EmployeeLogEldEvent previousLogLastEvent = EmployeeLogUtilities.GetLastEventInLog(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus, previousLog);
                if (previousLogLastEvent.getDutyStatusEnum().getValue() == DutyStatusEnum.DRIVING)
                {
                    String logMessage = String.format("Handling the On Duty transition across midnight by allowing add of event %s to log %s instead of log %s", timestampOfNewEvent, previousLog.getLogDate(), empLog.getLogDate());
                    ErrorLogHelper.RecordMessage(logMessage);
                    Log.d("LogEvent", logMessage);

                    allowAddToPriorLog = true;
                }
            }
        }

        return allowAddToPriorLog;
    }

    /// <summary>
    /// Update the event location for the event at the specific time.
    /// Handles the location being decoded asynchronously - after the
    /// event was alread added to the log
    /// </summary>
    /// <param name="user">user that involves the status change</param>
    /// <param name="empLog">log that involves the status change</param>
    /// <param name="timestampOfNewEvent">timestamp of the new log event, in Home Terminal Timezone units</param>
    /// <param name="dutyStatus">duty status of the new log event</param>
    /// <param name="location">location of the status change</param>
    /// <param name="isVehicleInMotion">true if the vehicle is moving, false otherwise</param>
    private void UpdateEventLocation(User user, EmployeeLog empLog, Date timestampOfNewEvent, DutyStatusEnum dutyStatus, Location location)
    {
        IAPIController empLogController = MandateObjectFactory.getInstance(this.getContext(),GlobalState.getInstance().getFeatureService()).getCurrentEventController();

        // The status change to Driving (at start of driving period) or to On Duty (at end of driving
        // period) should have already occurred.  Get the second to last event on the log.  If changing
        // to driving or from driving, create a route position
        EmployeeLogEldEvent previousEvent = EmployeeLogUtilities.GetEventPriorToTime(empLog, timestampOfNewEvent);

        if (dutyStatus.getValue() == DutyStatusEnum.DRIVING || (previousEvent != null && (previousEvent.getDutyStatusEnum().getValue() == DutyStatusEnum.DRIVING)))
        {
            // just started a new driving segment, or just ended a driving segment
            // force the route controller to process the location and create a GPS tracking
            // entry for the boundary of the driving segment

            if (location != null && EobrReader.getInstance() != null)
            {
                RouteController routeCntrlr = new RouteController(getContext());
                routeCntrlr.ProcessNewLocation(EobrReader.getInstance().getEobrSerialNumber(), EobrReader.getInstance().getEobrIdentifier(), location.getGpsInfo(), location.getOdometerReading(), true);
            }
        }

        // Update the location of the event at the specified timestamp
        if (location != null)
        {
            EmployeeLogEldEvent evt = EmployeeLogUtilities.UpdateEventLocationAtTime(empLog, location, timestampOfNewEvent);

            // persist the changes to the current day's log, just in case they have not been committed
            // and update the current employeelog in state
            empLogController.SaveLocalEmployeeLog(user, empLog);
            if(this.IsTheDriver(user))
                this.setCurrentDriversLog(empLog);
            if(this.IsTheCurrentActiveUser(user))
                this.setCurrentEmployeeLog(empLog);

            // publish the status change to all the delegates to update the location
            if (evt != null)
                this.PublishDutyStatusChange(empLog.getLogDate(), evt);
        }
    }

    /// <summary>
    /// Process the event status from the EOBR.
    /// This is designed to be called from the foreground UI thread,
    /// although the EOBR read is occurring on a background thread.
    /// This method is invoked from the RODS entry screen.
    /// </summary>
    /// <param name="e"></param>
    public void ProcessEobrReaderEvent(EobrEventArgs e)
    {
        // fetch the current event from the current log
        IAPIController empLogController = MandateObjectFactory.getInstance(this.getContext(),GlobalState.getInstance().getFeatureService()).getCurrentEventController();
        EmployeeLog empLog = this.getCurrentDriversLog();
        EmployeeLogEldEvent evt = EmployeeLogUtilities.GetLastEventInLog(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus,empLog);

        this.SetCurrentEobrSerialNumber();

        switch (GlobalState.getInstance().getCurrentDesignatedDriver().getDataProfile().getValue()) {
            case DataProfileEnum.MINIMUMHOS:
            case DataProfileEnum.MINIMUMHOSWITHFUELTAX:
                // Do not call EngineRecordController.ProcessNewStatusRecord()
                break;

            default:
                EngineRecordController engRecCntrl = new EngineRecordController(getContext(), GlobalState.getInstance().getAppSettings(getContext()));
                engRecCntrl.ProcessNewStatusRecord(EobrReader.getInstance().getEobrSerialNumber(), EobrReader.getInstance().getEobrIdentifier(), this.getCurrentDesignatedDriver(), empLog, e);
                break;
        }

        if (e.getStatusRecord() != null && !e.getStatusRecord().IsEmpty())
        {
            // a status report came through
            // first thing to do is determine if we should process this one
            Date nextTimestamp = this.getNextEobrReaderEventProcessingTimestamp();

            if (e.getReturnCode() == 0 && nextTimestamp != null && nextTimestamp.compareTo(e.getStatusRecord().getTimestampUtc()) > 0)
            {
                // skip this record because it came in too quickly
                // the timestamp of the EOBR record has not past the
                // next timestamp we're looking for
                return;
            }
            else
            {
                // calc the next time that an EOBR record should be fully processed
                this.setNextEobrReaderEventProcessingTimestamp(DateUtility.AddMilliseconds(e.getStatusRecord().getTimestampUtc(), _eobrIntervalMS));
            }

            // determine if the vehicle is in DRIVING and the timestamp belongs on a future log
            Date timestampOfNewEvent = e.getStatusRecord().getTimestampUtc();
            if (evt.getDutyStatusEnum().getValue() == DutyStatusEnum.DRIVING && this.BelongOnFutureLog(this.getCurrentDesignatedDriver(), empLog, timestampOfNewEvent))
            {
                // this event belongs on a future log, and the vehicle is being driven.
                // create a new log
                empLog = this.CreateNewLogIfNecessary(this.getCurrentDesignatedDriver(), empLog, timestampOfNewEvent);
            }
        }

        if (e.getReturnCode() == 0 && e.getStatusRecord() != null && !e.getStatusRecord().IsSignificantDeviceFailureDetected())
        {
            // successful read of EOBR status reported, and the device is functioning properly

            // first, stop any pending EOBR failures that may have been started previously
            FailureController ctrlr = new FailureController(getContext());
            boolean failureStopped = ctrlr.StopPendingEobrFailure(empLog, e.getStatusRecord().getTimestampUtc());

            Date newEventTime = e.getStatusRecord().getTimestampUtc();
            Log.v("LogEvent", String.format(">>EOBR ts:{%s} e:{%s} s:{%f} o:{%f}", newEventTime, e.getStatusRecord().getIsEngineRunning(), e.getStatusRecord().getSpeedometerReadingMPH(), e.getStatusRecord().getOdometerReadingMI()));

            // second, determine if the status report results in an automatic change in Duty Status
            DutyStatusEnum newDutyStatus = new DutyStatusEnum(DutyStatusEnum.NULL);
            DutyStatusEnum currentDutyStatus = new DutyStatusEnum(DutyStatusEnum.NULL);
            boolean isVehicleInMotion = false;
            Location newEventLocation = new Location();

            // is the odometer reading in the status report reliable?
            if (!e.getStatusRecord().IsFailureDetected(DeviceErrorFlags.Odometer) && e.getStatusRecord().getOdometerReading() > 0F)
            {
                this.setLastValidOdometerReading(e.getStatusRecord().getOdometerReading());
            }

            // try to get the GPS coordinates of the current location
            if (e.getStatusRecord().IsGpsLocationValid())
            {
                // GPS is functional, with a satellite fix and a valid timestamp,
                // so get the coordinates of where we are now
                try
                {
                    GpsLocation gpsLoc = new GpsLocation(e.getStatusRecord().getGpsTimestampUtc(), e.getStatusRecord().getGpsLatitude(), e.getStatusRecord().getNorthSouthInd(), e.getStatusRecord().getGpsLongitude(), e.getStatusRecord().getEastWestInd());

                    // the GPS location is valid, so save it for use later on
                    this.setLastValidGPSLocation(gpsLoc);

                    // let the route controller process the location
                    RouteController routeCntrlr = new RouteController(getContext());
                    routeCntrlr.ProcessNewLocation(EobrReader.getInstance().getEobrSerialNumber(), EobrReader.getInstance().getEobrIdentifier(), gpsLoc, this.getLastValidOdometerReading(), false);
                }
                catch (Exception excp)
                {
                    // something about the GPS position is not valid, record the error
                    this.HandleException(excp);
                }
            }

            // save the last known, and valid, GPS position and Odometer into the location
            newEventLocation.setGpsInfo(this.getLastValidGPSLocation());
            newEventLocation.setOdometerReading(this.getLastValidOdometerReading());

            if (evt != null)
            {
                // the location of the new event will default to the location of the last log event
                if (newEventLocation.IsEmpty() && !GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
                    newEventLocation.setName(evt.getLocation().getName());
                }

                // get the current duty status for that current event in the log
                currentDutyStatus = evt.getDutyStatusEnum();

                // allow the motion detector to process the status record
                // and determine if the vehicle is moving right now
                VehicleMotionDetector vmd = this.getVehicleMotionDetector();
                vmd.ProcessStatusRecord(this.getCurrentDesignatedDriver(), e.getStatusRecord());
                isVehicleInMotion = vmd.getIsVehicleInMotion();

                // based on the current duty status from the current log event
                // determine what the next event status should be
                switch (currentDutyStatus.getValue())
                {
                    case DutyStatusEnum.OFFDUTY:
                    case DutyStatusEnum.OFFDUTYWELLSITE:
                    case DutyStatusEnum.SLEEPER:
                    case DutyStatusEnum.ONDUTY:
                        // the last logevent was a non-driving status
                        if (vmd.getIsConfirmedDrivingPeriodStart())
                        {
                            // the motion detector has confirmed this as a driving period
                            // change the status to Driving
                            newDutyStatus = new DutyStatusEnum(DutyStatusEnum.DRIVING);

                            // the timestamp, location, and odometer should be from where the vehicle *first* started moving,
                            // not where the vehicle is *now*
                            newEventTime = vmd.getConfirmedDrivingStartTimestamp();
                            newEventTime = DateUtility.ConvertToPreviousLogEventTime(newEventTime);
                            newEventLocation.setGpsInfo(vmd.getDrivingPeriodStartLocation());
                            newEventLocation.setOdometerReading(vmd.getDrivingPeriodStartOdometer());
                        }

                        break;

                    case DutyStatusEnum.DRIVING:
                        // if the last logevent was a 'driving' status
                        if (isVehicleInMotion)
                        {
                            // whenever the vehicle is in motion, reset the manual driving segment indicator
                            //
                            this.setIsExtendDrivingSegmentEnabled(false);
                            this.setHasExtendedDrivingSegment(false);
                        }

                        if (!isVehicleInMotion && vmd.getIsConfirmedDrivingPeriodStop())
                        {
                            // the motion detector has confirmed that the vehicle has stopped
                            // now, we're on-duty
                            newDutyStatus = new DutyStatusEnum(DutyStatusEnum.ONDUTY);

                            // the timestamp, location and odometer are from where the vehicle *first* stopped,
                            // not where the vehicle is now.
                            newEventTime = vmd.getConfirmedDrivingStopTimestamp();
                            newEventTime = DateUtility.ConvertToPreviousLogEventTime(newEventTime);
                            newEventLocation.setGpsInfo( vmd.getDrivingPeriodStopLocation());
                            newEventLocation.setOdometerReading( vmd.getDrivingPeriodStopOdometer());

                            // remove the current location's name when stopping driving,
                            // because conceivably you've driven somewhere else...doh!
                            newEventLocation.setName(null);

                            // allow the driver to manually extend the driving period (due to traffic jam, etc.)
                            this.setIsExtendDrivingSegmentEnabled(true);
                            this.setHasExtendedDrivingSegment(false);
                        }
                        break;
                }

                // save the VMD to state in case something has changed
                this.setVehicleMotionDetector(vmd);
                if (evt.getEobrSerialNumber()==getCurrentEobrSerialNumber())
                    evt.getLocation().setEndOdometerReading(this.getLastValidOdometerReading());
            }

            if (newDutyStatus.getValue() != DutyStatusEnum.NULL)
            {
                final User currentDesignatedDriver = this.getCurrentDesignatedDriver();

                // ensure that the new location is geocoded in the background
                empLogController.ReverseGeocodeLocationAsync(empLog, newEventLocation.getGpsInfo(), new ReverseGeocodeAndPublishLocation(currentDesignatedDriver, empLog, newEventTime, newDutyStatus, newEventLocation));

                // a new duty status was found
                Log.d("LogEvent", String.format("--New Status ts:{%s} s:{%s} l:'{%s}' dd:{%s}", DateUtility.getHomeTerminalDateTimeFormat12Hour().format(newEventTime), newDutyStatus.getString(getContext()), newEventLocation == null ? "" : newEventLocation.ToLocationString(), this.getCurrentDesignatedDriver().getCredentials().getEmployeeCode()));

                // perform the status change on the log for the current driver to the
                // new log event status and location

                if (GlobalState.getInstance().getCompanyConfigSettings(this.getContext()).getIsMotionPictureEnabled()){
                    String ProductionId = GlobalState.getInstance().getCurrentDesignatedDriver().getUserState().getMotionPictureProductionId();
                    String AuthorityId = GlobalState.getInstance().getCurrentDesignatedDriver().getUserState().getMotionPictureAuthorityId();
                    empLog = this.PerformStatusChange(currentDesignatedDriver, empLog, newEventTime, newDutyStatus, newEventLocation, true, true, EobrReader.getInstance().getEobrSerialNumber(), ProductionId, AuthorityId);
                }
                else {
                    empLog = this.PerformStatusChange(currentDesignatedDriver, empLog, newEventTime, newDutyStatus, newEventLocation);
                }

                if (newDutyStatus.getValue() == DutyStatusEnum.DRIVING)
                {
                    // when a driving period starts, update the tractor number on
                    // the trip info of the current log with the EOBR unitId
                    String tractorNumber = EobrReader.getInstance().getEobrIdentifier();
                    // need old log here because just started driving
                    empLogController.UpdateTractorNumber(tractorNumber, empLog);
                }

            }
            else
            {
                if (failureStopped)
                {
                    // a failure was stopped, but did not otherwise result in a duty status change
                    // just publish to the UI to update the screen
                    this.PublishDutyStatusChange();
                }
                else
                {
                    // no failures have been stopped, but no change in event status has occurred either
                    // update the UI so that all messages and motion sensitive actions are taken care of
                    this.PublishDutyStatusChange(null, null);
                }
            }
        }
        else
        {
            // either the return code from the read is not successful,
            // or there is a significant hardware device failure reported

            // determine what type of error has occurred and report
            // the device failure to the manager
            boolean isFailureDetected = false;
            String failureMessage = null;

            // was the return code was the problem
            int rc = e.getReturnCode();
            if (rc != EobrReturnCode.S_SUCCESS && rc != EobrReturnCode.S_DEV_NOT_CONNECTED)
            {
                // any return code other than 'not connected' causes a failure
                isFailureDetected = true;
                failureMessage = String.format("Device failed with return code of '0x%x'", e.getReturnCode());
            }

            // check the overall hardware device status of the EOBR
            if (e.getStatusRecord() != null && !e.getStatusRecord().IsEmpty() && e.getStatusRecord().IsSignificantDeviceFailureDetected())
            {
                // some significant eobr failure has been detected,
                // but it's not *only* a failure of the GPS
                isFailureDetected = true;
                failureMessage = String.format("Device failed with status of '0x%x'", e.getStatusRecord().getOverallStatus());
            }

            // if there's been a failure, then send the error report to the manager
            if (isFailureDetected)
            {
                FailureController ctrlr = new FailureController(getContext());
                Date failureStartTimestamp = this.getCurrentClockHomeTerminalTime();
                ctrlr.ReportEobrFailure(empLog, failureStartTimestamp, failureMessage);
            }

            // reset the motion detector because we've forcefully stopped driving
            this.setVehicleMotionDetector(null);

            // since we're processing a failure situation, determine if we're currently
            // driving.
            // TCH - 2/25/10 - if driving AND moving to OFFLINE state, then change status
            // to on duty.  After autoclaiming an unassigned event, we are driving, but
            // just moved to ONLINE state and don't want to change status to on duty
            if (evt.getDutyStatusEnum().getValue() == DutyStatusEnum.DRIVING && e.getConnectionState() != ConnectionState.ONLINE)
            {
                // we're currently driving and a failure has occurred,

                // need to transition the driver to On-Duty right now because an
                // EOBR failure has occurred
                Date currentTimestamp = this.getCurrentClockHomeTerminalTime();
                Location newLoc = new Location();
                newLoc.setGpsInfo(this.getLastValidGPSLocation());
                newLoc.setOdometerReading(this.getLastValidOdometerReading());
                empLog = this.PerformStatusChange(this.getCurrentDesignatedDriver(), empLog, currentTimestamp, new DutyStatusEnum(DutyStatusEnum.ONDUTY), newLoc);
            }
            else
            {
                // if not driving right now, just publish to the UI to update the screen
                this.PublishDutyStatusChange();
            }
        }

        updateDriverHoursAvailable();

        // update the drivers log in state
        this.setCurrentDriversLog(empLog);
        if (this.IsCurrentUserTheDriver())
        {
            // the driver is also the current active user, so update the regular log as well
            this.setCurrentEmployeeLog(empLog);
        }
    }

    /// <summary>
    /// Process the event status from the EOBR.
    /// This is designed to be called from the foreground UI thread,
    /// although the EOBR read is occurring on a background thread.
    /// This method is invoked from the RODS entry screen.
    /// </summary>
    /// <param name="e"></param>
    public void ProcessEobrReaderEvent_Gen2(EobrEventArgs e)
    {
        // fetch the current event from the current log
        IAPIController empLogController = MandateObjectFactory.getInstance(this.getContext(),GlobalState.getInstance().getFeatureService()).getCurrentEventController();

        GlobalState globalState = GlobalState.getInstance();

        this.SetCurrentEobrSerialNumber();

        EventRecord eventRecord = e.getEventRecord();
        StatusRecord statusRecord = eventRecord.getStatusRecordData();
        TripReport tripReportData = eventRecord.getTripReportData();

        updateLastOdometer(statusRecord, tripReportData);

        MalfunctionRealtimeManager.getInstance().processEvent(eventRecord);

        EmployeeLog empLog = transitionEmployeeLogIfRequired(eventRecord, statusRecord);
        EmployeeLogEldEvent lastEmployeeLogEldEvent = EmployeeLogUtilities.GetLastEventInLog(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus,empLog);

        if(eventRecord.getEventType() == EventTypeEnum.ROUTEPOSITION)
        {
            if(statusRecord != null)
            {
                if(statusRecord.IsGpsLocationValid())
                {
                    try
                    {
                        GpsLocation gpsLoc = new GpsLocation(statusRecord.getTimestampUtc(), statusRecord.getGpsLatitude(), statusRecord.getGpsLongitude());

                        // the GPS location is valid, so save it for use later on
                        this.setLastValidGPSLocation(gpsLoc);

                        // let the route controller process the location
                        RouteController routeCntrlr = new RouteController(getContext());
                        routeCntrlr.ProcessNewLocation(EobrReader.getInstance().getEobrSerialNumber(), EobrReader.getInstance().getEobrIdentifier(), gpsLoc, this.getLastValidOdometerReading(), false);
                    }
                    catch (Exception excp)
                    {
                        // something about the GPS position is not valid, record the error
                        this.HandleException(excp);
                    }
                }
            }

            if (IsDrivingOrPersonalConveyance(lastEmployeeLogEldEvent) || IsDrivingOrHyrail(lastEmployeeLogEldEvent) || IsDrivingOrNonRegulatedDriving(lastEmployeeLogEldEvent))
                this.UpdateEventEndOdometer(empLog, lastEmployeeLogEldEvent, this.getLastValidOdometerReading());

            if (e.getPublishDutyStatusChange())
                this.PublishDutyStatusChange();

            updateDriverHoursAvailable();
            return;
        }

        // if we have a valid event, attempt to process it
        if (eventRecord.getTimecode() != 0)
        {
            // Process Status Records and Event Data Records
            EngineRecordController engRecCntrl = new EngineRecordController(getContext(), GlobalState.getInstance().getAppSettings(getContext()));
            engRecCntrl.ProcessNewStatusRecord(EobrReader.getInstance().getEobrSerialNumber(), EobrReader.getInstance().getEobrIdentifier(), this.getCurrentDesignatedDriver(), empLog, e);
        }

        //this is what ends YM
        this.createEmployeeLogEldEventIfNeeded(empLog, eventRecord);

        if(eventRecord.getEventType() == EventTypeEnum.VEHICLESTOPPED){
            GlobalState.getInstance().setPotentialDrivingStopTimestamp(eventRecord.getTimecodeAsDate());
        }
        else if(eventRecord.getEventType() == EventTypeEnum.MOVE) {

            if (GlobalState.getInstance().getSavedManualDrivingStopTimestamp() != null) {
                // 2014.04.17 sjn
                // MOVE event detected (the tab has detected the vehicle has started moving), but this has occurred after the user manually ended a DRIVING status
                // look for two things right now
                //    1. verify that the current log event not a DRIVING
                //    2. verify that the amount of time elapsed since the driving ended is not more than the driver's stop rule
                Date manualDriveStopTime = GlobalState.getInstance().getSavedManualDrivingStopTimestamp();
                if (lastEmployeeLogEldEvent.getDutyStatusEnum().getValue() != DutyStatusEnum.DRIVING) {
                    // the most recent duty status is not driving,

                    boolean hasDriveOffOccurred = this.HasUserStopTimeElapsedSince(this.getCurrentUser(), manualDriveStopTime, eventRecord);

                    // if user was Driving, manually changed Duty Status because of special driving period (Hyrail, PC or Non-Regulated) then
                    // resumed Driving [MOVE], don't RemoveAllEventsAfter but keep the OnDuty special event
                    boolean isInSpecialDrivingSegment = GlobalState.getInstance().getIsInHyrailDutyStatus() ||
                            GlobalState.getInstance().getIsInNonRegDrivingDutyStatus() ||
                            GlobalState.getInstance().getIsInPersonalConveyanceDutyStatus();

                    if (!hasDriveOffOccurred && !isInSpecialDrivingSegment) {
                        if (globalState.getIsInDriveOnPeriod()) {
                            createDrivingEventIfNecessary(empLog, lastEmployeeLogEldEvent, eventRecord.getTimecodeAsDate());
                        }
                    }
                }

                //2016.07.05 banderson
                /*
                It is entirely possible that a user manually ended a DRIVING status, and switched to a Personal Conveyance status
                In this case, we need to create a Personal ConveyanceDrivingStatus event based on the last known location,
                as that location will have been generated by an event which caused this controller to persist the GPS info
                 */
                if (GlobalState.getInstance().getIsUsingDrivingOverride()) {
                    Location lastKnownGoodLocation = new Location();
                    //Since this was generated by a Move event, we will need to use last known good location
                    lastKnownGoodLocation.setGpsInfo(this.getLastValidGPSLocation());
                    lastKnownGoodLocation.setOdometerReading(this.getLastValidOdometerReading());
                    // Update Personal Conveyance
                    // We should only fire this during a MOVE event if we aren't already in a PersonalConveyanceDrivingSegment

                    ISpecialDrivingController specialDrivingController = SpecialDrivingFactory.getControllerInDutyStatusButNotDrivingSegment();
                    if(specialDrivingController != null) {
                        specialDrivingController.StartSpecialDrivingStatus(eventRecord.getTimecodeAsDate(), lastKnownGoodLocation, empLog);
                    }
                }

                // we don't need the manual drive stop time anymore
                GlobalState.getInstance().setSavedManualDrivingStopTimestamp(null);
            }
            else if (GlobalState.getInstance().getIsInYardMoveDutyStatus()) {
                GlobalState.getInstance().setIsInYardMoveDrivingSegment(true);
            }
            // else, we are in a drive-on period already - need to determine if we need to create a driving duty status event
            else if (globalState.getIsInDriveOnPeriod()) {
                createDrivingEventIfNecessary(empLog, lastEmployeeLogEldEvent, eventRecord.getTimecodeAsDate());
            }

            if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
                // the KMB app can stop monitoring against the 5-minute threshold if the vehicle starts moving again
                GlobalState.getInstance().getEobrService().getBluetoothDrivingManager().setVehicleDriving();
            }

            GlobalState.getInstance().setPotentialDrivingStopTimestamp(null);
        }
        else if(eventRecord.getEventType() == EventTypeEnum.HOURLYTRIPRECORD){
            try {
                empLogController.CreateIntermediateEvent(Enums.CompositeEmployeeLogEldEventTypeEventCodeEnum.IntermediateLog, tripReportData);
            } catch (Throwable ex) {
                HandleException(new Exception("Error creating intermediate event", ex), "LogEntryController");
            }
        }
        else if (eventRecord.getEventType() == EventTypeEnum.MAPPOSITION){
            if(statusRecord != null)
            {
                Date currentTimestamp = statusRecord.getTimestampUtc();
                Date nextTimestamp = DateUtility.AddMinutes(currentTimestamp, MAP_POSITION_INTERVAL);
                globalState.setNextMapPositionTimestamp(nextTimestamp);
            }
        }
        else if(eventRecord.getEventType() == EventTypeEnum.DRIVEEND){
            // Drive_Off event
            // whenever the tab detects a driveOff, reset the manual driving stop timestamp
            GlobalState.getInstance().setSavedManualDrivingStopTimestamp(null);
            GlobalState.getInstance().setIsInDriveOnPeriod(false);
        }

        else if (eventRecord.getEventType() == EventTypeEnum.DRIVESTART){
            GlobalState.getInstance().setIsInDriveOnPeriod(true);
        }

        if(GlobalState.getInstance().getSavedManualDrivingStopTimestamp() != null){
            // 2014.04.18 sjn
            // if there is manual end to a driving period, and enough time has elapsed since, then remove the tracking of the manual end
            if(this.HasUserStopTimeElapsedSince(this.getCurrentUser(), GlobalState.getInstance().getSavedManualDrivingStopTimestamp(), eventRecord))
                GlobalState.getInstance().setSavedManualDrivingStopTimestamp(null);
        }

        if (IsDrivingOrPersonalConveyance(lastEmployeeLogEldEvent) || IsDrivingOrHyrail(lastEmployeeLogEldEvent) || IsDrivingOrNonRegulatedDriving(lastEmployeeLogEldEvent))
            this.UpdateEventEndOdometer(empLog, lastEmployeeLogEldEvent, this.getLastValidOdometerReading());

        // TODO How will Gen2 designate IsSignificantDeviceFailureDetected
        if (e.getReturnCode() == 0 && eventRecord.getTimecode() > 0) //&& !e.getStatusRecord().IsSignificantDeviceFailureDetected())
        {
            // successful read of EOBR status reported, and the device is functioning properly

            // first, stop any pending EOBR failures that may have been started previously
            FailureController ctrlr = new FailureController(getContext());

            // 2014.07.31 sjn - instead of ending the failure at the time of the event, end the failure right *now*,
            //                  since we've just received good communication from the ELD, we must be done with anything bad
            //                  that have happened previously
            //boolean failureStopped = ctrlr.StopPendingEobrFailure(empLog, new Date(eventRecord.getTimecode()));
            boolean failureStopped = ctrlr.StopPendingEobrFailure(empLog, DateUtility.getCurrentDateTimeUTC());

            Date newEventTime = eventRecord.getTimecodeAsDate();
            Log.v("LogEvent", String.format(">>EOBR ts:{%s} evt:{%s}", newEventTime, eventRecord.getEventType()));

            // second, determine if the status report results in an automatic change in Duty Status
            DutyStatusEnum newDutyStatus = new DutyStatusEnum(DutyStatusEnum.NULL);
            DutyStatusEnum currentDutyStatus = new DutyStatusEnum(DutyStatusEnum.NULL);
            Location newEventLocation = new Location();
            boolean addEventToPriorLog = false;

            // Update odometer and GPS from trip data, if present
            if (tripReportData != null)
            {
                // try to get the GPS coordinates of the current location
                if (tripReportData.IsGpsLocationValid())
                {
                    // GPS is functional, with a satellite fix and a valid timestamp,
                    // so get the coordinates of where we are now
                    try
                    {
                        GpsLocation gpsLoc = new GpsLocation(new Date(eventRecord.getTimecode()), tripReportData.getLatitude(), tripReportData.getLongitude());

                        // the GPS location is valid, so save it for use later on
                        this.setLastValidGPSLocation(gpsLoc);

                        // let the route controller process the location
                        RouteController routeCntrlr = new RouteController(getContext());
                        routeCntrlr.ProcessNewLocation(EobrReader.getInstance().getEobrSerialNumber(), EobrReader.getInstance().getEobrIdentifier(), gpsLoc, this.getLastValidOdometerReading(), false);
                    }
                    catch (Exception excp)
                    {
                        // something about the GPS position is not valid, record the error
                        this.HandleException(excp);

                    }
                }
            }

            // save the last known, and valid, GPS position and Odometer into the location
            newEventLocation.setGpsInfo(this.getLastValidGPSLocation());
            newEventLocation.setOdometerReading(this.getLastValidOdometerReading());

            // Check if this event is related to driving status (on/off)
            if (eventRecord.isDrivingEvent() && lastEmployeeLogEldEvent != null){

                if (!GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
                    // the location of the new event will default to the location of the last log event
                    newEventLocation.setName(lastEmployeeLogEldEvent.getLocation().getName());
                }

                // get the current duty status for that current event in the log
                currentDutyStatus = lastEmployeeLogEldEvent.getDutyStatusEnum();

                //if operating in eld exempt mode, driving event will be inacitve so current status wont be driving,
                if(eventRecord.getEventType() == EventTypeEnum.DRIVEEND && empLog.getIsExemptFromELDUse()) {
                    currentDutyStatus = new DutyStatusEnum(DutyStatusEnum.DRIVING);
                }

                if(eventRecord.getEventType() == EventTypeEnum.DRIVESTART) {
                    //if the vehicle started motion prior to midnight but the DRIVE_ON didn't fire until after midnight,
                    //we need to add the driving status to both logs.
                    if(eventRecord.getTimecodeAsDate().after(empLog.getLogDate()) && tripReportData.getDataTimecodeAsDate().before(empLog.getLogDate())) {
                        addEventToPriorLog = true;
                        StatusRecord sr = empLogController.getStatusRecord(empLog.getLogDate());
                        if(sr != null) {
                            newEventLocation.setEndOdometerReading(sr.getOdometerReading());
                        }
                    }
                }
                // based on the current duty status from the current log event
                // determine what the next event status should be
                switch (currentDutyStatus.getValue())
                {
                    case DutyStatusEnum.OFFDUTY:
                    case DutyStatusEnum.OFFDUTYWELLSITE:
                    case DutyStatusEnum.SLEEPER:
                    case DutyStatusEnum.ONDUTY:

                        // OnDuty or OffDuty is still considered driving if your in Special Driving status
                        boolean isInSpecialDrivingSegment = GlobalState.getInstance().getIsInYardMoveDrivingSegment() ||
                                GlobalState.getInstance().getIsInPersonalConveyanceDrivingSegment() ||
                                GlobalState.getInstance().getIsInHyrailDrivingSegment() ||
                                GlobalState.getInstance().getIsInNonRegDrivingSegment();

                        // the last logevent was a non-driving status
                        if (!isInSpecialDrivingSegment && eventRecord.getEventType() == EventTypeEnum.DRIVESTART)
                        {
                            // the motion detector has confirmed this as a driving period
                            // change the status to Driving
                            newDutyStatus = new DutyStatusEnum(DutyStatusEnum.DRIVING);

                            // the timestamp, location, and odometer should be from where the vehicle *first* started moving,
                            // not where the vehicle is *now*
                            newEventTime = tripReportData.getDataTimecodeAsDate();
                            newEventLocation.setGpsInfo( new GpsLocation(newEventTime, tripReportData.getLatitude(), tripReportData.getLongitude()));
                            newEventLocation.setOdometerReading(tripReportData.getOdometer());

                            // Reset potentialDrivingStopTimestamp when vehicle starts driving again.
                            GlobalState.getInstance().setPotentialDrivingStopTimestamp(null);
                        }

                        break;

                    case DutyStatusEnum.DRIVING:
                        // if the last logevent was a 'driving' status
                        if (eventRecord.getEventType() == EventTypeEnum.DRIVESTART)
                        {
                            // whenever the vehicle is in motion, reset the manual driving segment indicator
                            //
                            this.setIsExtendDrivingSegmentEnabled(false);
                            this.setHasExtendedDrivingSegment(false);

                            // if last event was a Manual Drive (i.e. replied Yes to 'Continue in driving status?')
                            // but now the vehicle starts driving again, create a new automatic drive event
                            if (lastEmployeeLogEldEvent.getEventRecordOrigin() == Enums.EmployeeLogEldEventRecordOrigin.EditedEnteredByDriver ) {
                                // the motion detector has confirmed this as a driving period
                                // change the status to Driving
                                newDutyStatus = new DutyStatusEnum(DutyStatusEnum.DRIVING);

                                // the timestamp, location, and odometer should be from where the vehicle *first* started moving,
                                // not where the vehicle is *now*
                                newEventTime = tripReportData.getDataTimecodeAsDate();
                                newEventLocation.setGpsInfo( new GpsLocation(newEventTime, tripReportData.getLatitude(), tripReportData.getLongitude()));
                                newEventLocation.setOdometerReading(tripReportData.getOdometer());

                                // Reset potentialDrivingStopTimestamp when vehicle starts driving again.
                                GlobalState.getInstance().setPotentialDrivingStopTimestamp(null);
                            }
                        }

                        if (eventRecord.getEventType() == EventTypeEnum.DRIVEEND)
                        {
                            boolean consumeDriveOffEvent = true;

                            if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
                                // if Bluetooth disconnected and now we've reconnected to the ELD, we want the KMB app to continue to monitor the 5-minute threshold.
                                // That means that when the DRIVE_OFF event is consumed, we need to ignore it when KMB is monitoring the 5-minute threshold
                                if (GlobalState.getInstance().getEobrService().getBluetoothDrivingManager().isThresholdTimerRunning()) {
                                    consumeDriveOffEvent = false;   // ignore the event
                                }
                            }

                            if (consumeDriveOffEvent) {
                                // the motion detector has confirmed that the vehicle has stopped
                                // now, we're on-duty
                                newDutyStatus = new DutyStatusEnum(DutyStatusEnum.ONDUTY);

                                // the timestamp, location and odometer are from where the vehicle *first* stopped,
                                // not where the vehicle is now.
                                newEventTime = tripReportData.getDataTimecodeAsDate();
                                newEventLocation.setGpsInfo(new GpsLocation(newEventTime, tripReportData.getLatitude(), tripReportData.getLongitude()));
                                newEventLocation.setOdometerReading(tripReportData.getOdometer());

                                // remove the current location's name when stopping driving,
                                // because conceivably you've driven somewhere else...doh!
                                newEventLocation.setName(null);

                                // allow the driver to manually extend the driving period (due to traffic jam, etc.)
                                this.setIsExtendDrivingSegmentEnabled(true);
                                this.setHasExtendedDrivingSegment(false);
                            }
                        }
                        break;
                }

                // save the VMD to state in case something has changed
                if (lastEmployeeLogEldEvent.getEobrSerialNumber()==getCurrentEobrSerialNumber())
                    lastEmployeeLogEldEvent.getLocation().setEndOdometerReading(this.getLastValidOdometerReading());
            }

            if (newDutyStatus.getValue() != DutyStatusEnum.NULL)
            {
                final User currentDesignatedDriver = this.getCurrentDesignatedDriver();

                // a new duty status was found
                Log.d("LogEvent", String.format("--New Status ts:{%s} s:{%s} l:'{%s}' dd:{%s}", DateUtility.getHomeTerminalDateTimeFormat12Hour().format(newEventTime), newDutyStatus.getString(getContext()), newEventLocation == null ? "" : newEventLocation.ToLocationString(), this.getCurrentDesignatedDriver().getCredentials().getEmployeeCode()));

                // perform the status change on the log for the current driver to the
                // new log event status and location
                newEventTime = DateUtility.ConvertToPreviousLogEventTime(newEventTime);

                boolean shouldPerformADutyStatusChange;
                DutyStatusEnum dutyStatusToBroadcast = newDutyStatus;

                ISpecialDrivingController specialDrivingController = SpecialDrivingFactory.getControllerInDutyStatus();
                if(specialDrivingController != null) {
                    specialDrivingController.ProcessNewDutyStatus(dutyStatusToBroadcast, newEventTime, newEventLocation, empLog, eventRecord);

                    // When using a driving override we don't want any new events added
                    // to the log
                    if (specialDrivingController.getIsInSpecialDutyStatus()) {
                        EmployeeLogEldEvent lastEventOnLog = EmployeeLogUtilities.GetLastEventInLog(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus,empLog);
                        dutyStatusToBroadcast = lastEventOnLog.getDutyStatusEnum();

                        if ((specialDrivingController instanceof LogHyrailController) || specialDrivingController instanceof LogNonRegulatedDrivingController){
                            shouldPerformADutyStatusChange = true;
                        }else{
                            shouldPerformADutyStatusChange = false;
                        }
                    }
                    else {
                        shouldPerformADutyStatusChange = true;
                    }
                } else {
                    // if logging out - assume we are submitting logs so record StatusChange event
                    shouldPerformADutyStatusChange = GlobalState.getInstance().getIsUserLoggingOut() ? true : empLogController.CheckAndHandleDriveOffWhenThereIsANewDutyStatus(dutyStatusToBroadcast);
                }

                if (shouldPerformADutyStatusChange) {
                    // don't perform a status change

                    String productionId = null;
                    String authorityId = null;
                    if (GlobalState.getInstance().getCompanyConfigSettings(this.getContext()).getIsMotionPictureEnabled()){

                        productionId = GlobalState.getInstance().getCurrentDesignatedDriver().getUserState().getMotionPictureProductionId();
                        authorityId = GlobalState.getInstance().getCurrentDesignatedDriver().getUserState().getMotionPictureAuthorityId();
                    }

                    String serialNumber = EobrReader.getInstance().getEobrSerialNumber();
                    if(addEventToPriorLog) {
                        empLog = this.PerformStatusChangeOnPreviousAndCurrentLog(currentDesignatedDriver, empLog, newEventTime, dutyStatusToBroadcast, newEventLocation,
                                e.getPublishDutyStatusChange(), false, serialNumber, serialNumber, productionId, authorityId);
                    } else {
                        empLog = this.PerformStatusChange(currentDesignatedDriver, empLog, newEventTime, dutyStatusToBroadcast, newEventLocation, e.getPublishDutyStatusChange(),
                                false,  serialNumber, serialNumber, productionId, authorityId);
                    }
                }


                Date logStartTime = EmployeeLogUtilities.CalculateLogStartTime(this.getContext(), empLog.getLogDate(), currentDesignatedDriver.getHomeTerminalTimeZone());
                if (empLog.getMobileStartTimestamp() != null &&
                        newEventTime.compareTo(empLog.getMobileStartTimestamp()) < 0 &&
                        !newEventTime.before(logStartTime))
                {
                    // if the new event time is prior to the mobileLogStart, then update the current event time
                    // note: this seems weird, but it can happen due to the clock drift algorithm of the ELD.
                    //       If the ELD clock is sync'ed forward, the event times will lag behind until the drift has completed
                    //       When this happens, it's possible that a driving event could happen before the login event, so the mobileLogStartTimestamp needs to be updated accordingly
                    this.UpdateCurrentEventTimestamp(newEventTime);
                }

                // ensure that the new location is geocoded in the background
                // processing the results of the async reverse geocode expect the status change to have
                // occurred, so move the reverse geocoding to after performing the status change, to
                // ensure the status change occurred
                empLogController.ReverseGeocodeLocationAsync(empLog, newEventLocation.getGpsInfo(), new ReverseGeocodeAndPublishLocation(currentDesignatedDriver, empLog, newEventTime, dutyStatusToBroadcast, newEventLocation));
                GlobalState.getInstance().setLastLocation(newEventLocation);

                if (newDutyStatus.getValue() == DutyStatusEnum.DRIVING)
                {
                    // when a driving period starts, update the tractor number on
                    // the trip info of the current log with the EOBR unitId
                    String tractorNumber = EobrReader.getInstance().getEobrIdentifier();
                    // need old log here because just started driving
                    empLogController.UpdateTractorNumber(tractorNumber, empLog);
                }
            }
            else
            {
                if (failureStopped)
                {
                    // a failure was stopped, but did not otherwise result in a duty status change
                    // just publish to the UI to update the screen
                    if (e.getPublishDutyStatusChange())
                        this.PublishDutyStatusChange();
                }
                else {
                    ISpecialDrivingController controller = SpecialDrivingFactory.getControllerInDrivingSegment();

                    if(controller != null) {
                        newEventLocation.setName(null);
                        controller.ProcessEvent(eventRecord, newEventLocation, empLog);
                    } else {
                        // no failures have been stopped, but no change in event status has occurred either
                        // update the UI so that all messages and motion sensitive actions are taken care of
                        if (e.getPublishDutyStatusChange())
                            this.PublishDutyStatusChange(null, null);
                    }
                }
            }
            if (!GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
                ISpecialDrivingController specialDrivingController = SpecialDrivingFactory.getControllerInDrivingSegment();
                EmployeeLogEldEvent lastEventLog = EmployeeLogUtilities.GetLastEventInLog(empLog, Enums.EmployeeLogEldEventType.DutyStatusChange);
                if (specialDrivingController != null && lastEventLog.getLogRemark() == null) {
                    specialDrivingController.AddAobrdStartingLogRemark(empLog, eventRecord.getTimecodeAsDate(), lastEventLog);
                }
            }
        }
        else
        {
            // either the return code from the read is not successful,
            // or there is a significant hardware device failure reported

            // determine what type of error has occurred and report
            // the device failure to the manager
            boolean isFailureDetected = false;
            String failureMessage = null;

            // was the return code was the problem
            int rc = e.getReturnCode();
            if (rc != EobrReturnCode.S_SUCCESS && rc != EobrReturnCode.S_DEV_NOT_CONNECTED)
            {
                // any return code other than 'not connected' causes a failure
                isFailureDetected = true;
                failureMessage = String.format("Device failed with return code of '0x%x'", e.getReturnCode());
            }

            // check the overall hardware device status of the EOBR
            // TODO How do we check device failure
            /*
            if (eventRecord != null)// && !e.getStatusRecord().IsEmpty() && e.getStatusRecord().IsSignificantDeviceFailureDetected())
            {
                // some significant eobr failure has been detected,
                // but it's not *only* a failure of the GPS
                isFailureDetected = true;
                // TODO How do we check device failure message
                failureMessage = String.format("Device failed with status of '0x%x'", e.getReturnCode());
            }
			*/

            // if there's been a failure, then send the error report to the manager
            if (isFailureDetected)
            {
                FailureController ctrlr = new FailureController(getContext());
                Date failureStartTimestamp = this.getCurrentClockHomeTerminalTime();
                ctrlr.ReportEobrFailure(empLog, failureStartTimestamp, failureMessage);
            }

            // reset the motion detector because we've forcefully stopped driving
            this.setVehicleMotionDetector(null);

            // since we're processing a failure situation, determine if we're currently
            // driving.
            // TCH - 2/25/10 - if driving AND moving to OFFLINE state, then change status
            // to on duty.  After autoclaiming an unassigned event, we are driving, but
            // just moved to ONLINE state and don't want to change status to on duty
            if (lastEmployeeLogEldEvent != null
                    && lastEmployeeLogEldEvent.getDutyStatusEnum() != null
                    && (IsDrivingOrYardMove(lastEmployeeLogEldEvent) || IsDrivingOrPersonalConveyance(lastEmployeeLogEldEvent) || IsDrivingOrHyrail(lastEmployeeLogEldEvent) || IsDrivingOrNonRegulatedDriving(lastEmployeeLogEldEvent))
                    && e != null
                    && e.getConnectionState() != ConnectionState.ONLINE)
                {
                // we're currently driving and a failure has occurred,

                Date currentTimestamp = this.getCurrentClockHomeTerminalTime();

                if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {

                    // ProcessEobrReaderEvent_Gen2 is called multiple times so check if timer has already started
                    if (!GlobalState.getInstance().getEobrService().getBluetoothDrivingManager().isThresholdTimerRunning()) {
                        long drivingStopTimeMillis = TimeUnit.MINUTES.toMillis(GlobalState.getInstance().getCompanyConfigSettings(this.getContext()).getMandateDrivingStopTimeMinutes());   // default 5 minutes

                        // driver may have already stopped driving when the bluetooth disconnect occurred so use the current PotentialDrivingStopTimestamp
                        if (GlobalState.getInstance().getPotentialDrivingStopTimestamp() != null) {

                            // timer should be from the PotentialDrivingStopTimestamp value
                            Calendar currentCalendar = Calendar.getInstance();
                            currentCalendar.setTime(currentTimestamp);

                            Calendar potentialDrivingStopCalendar = Calendar.getInstance();
                            potentialDrivingStopCalendar.setTime(GlobalState.getInstance().getPotentialDrivingStopTimestamp());

                            // KMB timer should only tick for the remaining duration left on the 5-minute timer
                            drivingStopTimeMillis -= currentCalendar.getTimeInMillis() - potentialDrivingStopCalendar.getTimeInMillis();

                            currentTimestamp = GlobalState.getInstance().getPotentialDrivingStopTimestamp();
                        }

                        // Properly track when CMV has not been in-motion for 5 consecutive minutes when a Bluetooth
                        // disconnection occurs when current duty status is an automatic driving duty status.
                        GlobalState.getInstance().getEobrService().getBluetoothDrivingManager().setBluetoothDisconnectedDuringDriving(currentTimestamp, drivingStopTimeMillis, GlobalState.getInstance().getIsInYardMoveDutyStatus(), GlobalState.getInstance().getIsInPersonalConveyanceDrivingSegment(), GlobalState.getInstance().getIsInHyrailDrivingSegment(), GlobalState.getInstance().getIsInNonRegDrivingSegment());

                        GlobalState.getInstance().setPotentialDrivingStopTimestamp(currentTimestamp);

                        // publish to the UI to update the screen that Bluetooth is disconnected
                        this.PublishDutyStatusChange();
                    }
                }
                else {
                    // need to transition the driver to On-Duty right now because an
                    // EOBR failure has occurred
                    // Since we don't have a connection to the EOBR, don't pass along the last GPS location
                    Location newLoc = new Location();
                    newLoc.setOdometerReading(this.getLastValidOdometerReading());
                    empLog = this.PerformStatusChange(this.getCurrentDesignatedDriver(), empLog, currentTimestamp, new DutyStatusEnum(DutyStatusEnum.ONDUTY), newLoc);

                    GlobalState.getInstance().setPotentialDrivingStopTimestamp(currentTimestamp);
                }
            }
            else
            {
                // if not driving right now, just publish to the UI to update the screen
                if (e.getPublishDutyStatusChange())
                    this.PublishDutyStatusChange();
            }
        }

        updateDriverHoursAvailable();

        // update the drivers log in state
        this.setCurrentDriversLog(empLog);
        if (this.IsCurrentUserTheDriver())
        {
            // the driver is also the current active user, so update the regular log as well
            this.setCurrentEmployeeLog(empLog);
        }

    }

    private void updateLastOdometer(StatusRecord statusRecord, TripReport tripReport) {
        Date srTime = null;
        Date trTime = null;
        if(statusRecord != null) {
            // is the odometer reading in the status report reliable?
            if(statusRecord.getOdometerReading() > 0F) {
                srTime = statusRecord.getTimestampUtc();
            }
        }
        if(tripReport != null) {
            if(tripReport.getOdometer() > 0F) {
                trTime = tripReport.getDataTimecodeAsDate();
            }
        }

        Float odometer = null;
        if(statusRecord != null && trTime != null) {
            if(srTime.after(trTime)) {
                odometer = statusRecord.getOdometerReading();
            } else {
                odometer = tripReport.getOdometer();
            }
        } else if(srTime != null) {
            odometer = statusRecord.getOdometerReading();
        } else if(trTime != null) {
            odometer = tripReport.getOdometer();
        }

        if(odometer != null) {
            this.setLastValidOdometerReading(odometer);
        }
    }

    private EmployeeLog transitionEmployeeLogIfRequired(EventRecord eventRecord, StatusRecord statusRecord) {
        Date timestampOfNewEvent;
        //note: not relying on status record, since the status record associated
        //with an ignition on represents the _last_ time the engine was running
        //and could have been on a prior log date
        if(eventRecord != null && eventRecord.getTimecode() > 0) {
            timestampOfNewEvent = eventRecord.getTimecodeAsDate();
        } else {
            timestampOfNewEvent = DateUtility.getCurrentHomeTerminalTime(getCurrentDesignatedDriver());
        }
        EmployeeLog empLog = this.getCurrentDriversLog();
        empLog = this.CreateNewLogIfNecessary(this.getCurrentDesignatedDriver(), empLog, timestampOfNewEvent);

        // determine if the vehicle timestamp belongs on a future log for geoTab
        // 2018-02-22 ifitzgerald: I'm not sure why Geotab cares about the StatusRecord instead of the event
        // but I don't have time to dig into it at the moment. This may be redundant.
        if(statusRecord != null && statusRecord.getTimestampUtc() != null
                && GlobalState.getInstance().getCompanyConfigSettings(this.getContext()).getIsGeotabEnabled() ) {

            // this event belongs on a future log, and the vehicle is being driven.
            // create a new log for the day
            timestampOfNewEvent = statusRecord.getTimestampUtc();
            empLog = this.CreateNewLogIfNecessary(this.getCurrentDesignatedDriver(), empLog, timestampOfNewEvent);
        }
        return empLog;
    }

    /*
    A move event has occurred and we are in a DRIVE_ON period (DRIVE_ON has previously fired).
    Need to determine if a driving duty status needs to be created - a driving duty status
    would need to be created if we were in a special driving category, stopped driving, ended
    special driving category and started driving again prior to the DRIVE_OFF.
 */
    private void createDrivingEventIfNecessary(EmployeeLog empLog, EmployeeLogEldEvent lastEvent, Date timeOfEvent)
    {
        // if we are not in a driving duty status and we are not in a special driving category
        // then a driving event should be created
        if (lastEvent.getDutyStatusEnum().getValue() != DutyStatusEnum.DRIVING && !GlobalState.getInstance().getIsUsingDrivingOverride()) {
            // save the last known, and valid, GPS position and Odometer into the location
            Location newEventLocation = new Location();
            newEventLocation.setGpsInfo(this.getLastValidGPSLocation());
            newEventLocation.setOdometerReading(this.getLastValidOdometerReading());

            Date newEventTime = DateUtility.ConvertToPreviousLogEventTime(timeOfEvent);

            empLog = this.PerformStatusChange(this.getCurrentDesignatedDriver(), empLog, newEventTime, new DutyStatusEnum(DutyStatusEnum.DRIVING), newEventLocation);
        }
    }



    private void SetCurrentEobrSerialNumber() {
        GlobalState globalState = GlobalState.getInstance();
        String CurrentEobrSerialNumber = "";
        if (EobrReader.getInstance()!=null)
            CurrentEobrSerialNumber = EobrReader.getInstance().getEobrSerialNumber();

        globalState.setCurrentEobrSerialNumber(CurrentEobrSerialNumber);
    }

    private void createEmployeeLogEldEventIfNeeded(EmployeeLog empLog, EventRecord eventRecord)
    {
        try {
            IAPIController empLogController = MandateObjectFactory.getInstance(getContext(), GlobalState.getInstance().getFeatureService()).getCurrentEventController();
            switch (eventRecord.getEventType()) {
                case EventTypeEnum.IGNITIONON:
                    this.createIgnitionEvent(empLogController, empLog, eventRecord, Enums.CompositeEmployeeLogEldEventTypeEventCodeEnum.EnginePowerUpEvent);

                    if (GlobalState.getInstance().getIsInYardMoveDutyStatus()) {
                        EmployeeLogEldEventFacade eldEventFacade = new EmployeeLogEldEventFacade(this.getContext(), getCurrentDesignatedDriver());

                        EmployeeLogEldEvent eldEvent = eldEventFacade.getMostRecentDriverIndicationEventByDriverID(empLog.getEmployeeId());
                        if (eldEvent.getEventCode() == 2) {
                            Date StartTimestamp = eldEvent.getEventDateTime();
                            //this ends YM
                            if (hasIngitionOffCycleOccuredSince(StartTimestamp)) {
                                EndYardMoveAndChangeDutyStatus(empLog, empLogController, eventRecord, "Ignition Off Cycle/End YM");
                            }
                        }
                    }
                    break;
                case EventTypeEnum.IGNITIONOFF:
                    this.createIgnitionEvent(empLogController, empLog, eventRecord, Enums.CompositeEmployeeLogEldEventTypeEventCodeEnum.EngineShutDownEvent);
                    break;
            }
        }
        catch (Throwable e) {
            String eventType = new EventTypeEnum(eventRecord.getEventType()).toStringDisplay(getContext());
            Log.e("UnhandledCatch", String.format("Failed to create an ELD event for event record type %s", eventType), e);
        }
    }

    // During reading history, we read all ignition events from ReferenceTimestamp --> current time (leaving ReferenceTimestamp alone).
    // Then we read driving events from ReferenceTimestamp --> time of last driving event (advancing the ReferenceTimestamp as we go).
    // It is possible that we have read ignition events AFTER the time of the last driving event (where the ReferenceTimestamp is),
    // and we would be reading these events again in a connected state, so we are ensuring we don't create duplicate events here
    private void createIgnitionEvent(IAPIController empLogController, EmployeeLog empLog, EventRecord event, Enums.CompositeEmployeeLogEldEventTypeEventCodeEnum eventTypeAndCode) throws Throwable {
        if (!empLogController.IsDuplicateEnginePowerUpOrShutDownUnassignedEvent(event, eventTypeAndCode)) {
            empLogController.CreateEnginePowerUpOrShutDownEvent(empLog, event, eventTypeAndCode);
        }
    }

    /*
     * Determine if Personal Conveyance should end after a bluetooth disconnect / reconnect scenario
     */
    public Date hasIngitionOffOnCycleOccuredSince(Date timeToCompare){
        if(timeToCompare == null) {
            return null;
        }

        String driverId = GlobalState.getInstance().getCurrentDriversLog().getEmployeeId();

        Date lastIgnitionOff = lastIgnitionOffByDriver(driverId);
        if(lastIgnitionOff == null) {
            return null;
        }

        Date lastIgnitionOn = lastIgnitionOnByDriver(driverId);
        if(lastIgnitionOn == null) {
            return null;
        }

        if (lastIgnitionOff.after(timeToCompare) && lastIgnitionOn.after(timeToCompare)) {
            return lastIgnitionOn;
        }

        return null;
    }

    private boolean hasIngitionOffCycleOccuredSince(Date timeToCompare){
        String driverId = GlobalState.getInstance().getCurrentDriversLog().getEmployeeId();

        Date offTime = lastIgnitionOffByDriver(driverId);
        if(offTime != null && offTime.after(timeToCompare)) {
            return true;
        }

        return false;
    }

    //retrieve latest Engine Status of parameter's timestamp
    public Date lastIgnitionOffByDriver(String driverId){
        //gathering User and EOBR data from GlobalState
        EmployeeLogEldEventFacade employeeLogEldEventFacade = new EmployeeLogEldEventFacade(GlobalState.getInstance().getApplicationContext(), getCurrentDesignatedDriver());
        Date ignitionTime = employeeLogEldEventFacade.GetMostRecentDateforIgnitionOffbyDriverId(driverId);

        return ignitionTime;
    }

    //retrieve latest Engine Status of parameter's timestamp
    private Date lastIgnitionOnByDriver(String driverId){
        //gathering User and EOBR data from GlobalState
        EmployeeLogEldEventFacade employeeLogEldEventFacade = new EmployeeLogEldEventFacade(GlobalState.getInstance().getApplicationContext(), getCurrentDesignatedDriver());
        return employeeLogEldEventFacade.GetMostRecentDateforIgnitionOnbyDriverId(driverId);
    }

    public void EndYardMoveAndChangeDutyStatus(EmployeeLog empLog, IAPIController empLogController, EventRecord eventRecord, String logRemark){
        try{
            RuleSetTypeEnum ruleSetTypeEnum = empLog.getRuleset();
            Date timestamp = this.getCurrentClockHomeTerminalTime();
            DutyStatusEnum dutyStatus = new DutyStatusEnum(DutyStatusEnum.ONDUTY);
            EmployeeLogEldEvent event = EmployeeLogUtilities.GetLastEventInLog(empLog);
            Location location = event.getLocation();

            empLogController.CreateDutyStatusChangedEvent(empLog, timestamp, dutyStatus, location, true, ruleSetTypeEnum , logRemark , timestamp, true, null, null);
            GlobalState.getInstance().setIsEndingActivePCYMWT_Status(true);
            empLogController.CheckForAndCreateEndOfPCYMWT_Event(empLog, this.getCurrentClockHomeTerminalTime(), location);
            GlobalState.getInstance().setIsInYardMoveDutyStatus(false);
            GlobalState.getInstance().setIsInYardMoveDrivingSegment(false);
        }
        catch (Throwable e)
        {
            String eventType = new EventTypeEnum(eventRecord.getEventType()).toStringDisplay(getContext());
            Log.e("UnhandledCatch", String.format("Failed to create ELD events for event record type %s associated with: %s", eventType, logRemark), e);
        }
    }

    private boolean IsDrivingOrPersonalConveyance(EmployeeLogEldEvent evt)
    {
        return evt.getDutyStatusEnum().getValue() == DutyStatusEnum.DRIVING || GlobalState.getInstance().getIsInPersonalConveyanceDrivingSegment();
    }

    private boolean IsDrivingOrYardMove(EmployeeLogEldEvent evt)
    {
        return evt.getDutyStatusEnum().getValue() == DutyStatusEnum.DRIVING || GlobalState.getInstance().getIsInYardMoveDrivingSegment();
    }

    private boolean IsDrivingOrHyrail(EmployeeLogEldEvent evt)
    {
        return evt.getDutyStatusEnum().getValue() == DutyStatusEnum.DRIVING || GlobalState.getInstance().getIsInHyrailDrivingSegment();
    }

    private boolean IsDrivingOrNonRegulatedDriving(EmployeeLogEldEvent evt)
    {
        return evt.getDutyStatusEnum().getValue() == DutyStatusEnum.DRIVING || GlobalState.getInstance().getIsInNonRegDrivingSegment();
    }

    /*
     * Answer if the user's rule for Stop time has elapsed since the comparison time.
     * This is trying to detect if a DriveOff should have occurred since compareTo time relative to the eventRecord.
     */
    private boolean HasUserStopTimeElapsedSince(User user, Date compareTo, EventRecord eventRecord) {
        boolean answer = false;

        Date expirationTime = DateUtility.AddMinutes(compareTo, user.getDrivingStopTimeMinutes());
        //Date now = DateUtility.CurrentHomeTerminalTime(this.getCurrentUser());

        Date expirationTimeTrimmed = DateUtility.ConvertToPreviousLogEventTime(expirationTime);
        Date nowTrimmed = DateUtility.ConvertToPreviousLogEventTime(eventRecord.getTimecodeAsDate());

        if(nowTrimmed.compareTo(expirationTimeTrimmed) > 0)
            answer = true;

        return answer;
    }

    public UnassignedDrivingPeriodResult ProcessUnassignedDrivingPeriodEvent(UnassignedDrivingPeriodEventArgs e)
    {
        EmployeeLog empLog = this.getCurrentDriversLog();
        UnassignedDrivingPeriodResult result = e.getResult();

        //generating, merging, autoclaiming, etc of the periods is handled by the EobrService which calls the UnassignedPeriod controller

        ProcessUnassignedDrivingPeriodResult(result, empLog);

        return result;
    }

    public UnassignedDrivingPeriodResult ProcessEobrReaderHistoryEventGenII(EobrGenIIHistoryEventArgs e) throws KmbApplicationException
    {
        EmployeeLog empLog = this.getCurrentDriversLog();
        UnassignedDrivingPeriodResult result = new UnassignedDrivingPeriodResult(); //init with default values - no detection

        if (e.getEventList() == null) {
            if (e.getHasFailureOccurred()) {
                // Report an EOBR failure so that paper logs are required.
                FailureController failureCtrlr = new FailureController(getContext());
                failureCtrlr.ReportEobrFailure(empLog, this.getCurrentClockHomeTerminalTime(), "Failed to read historical driving records.");
            }
        }
        else
        {
            ArrayList<EventRecord> currentEvents = e.getEventList();

            UnassignedPeriodController controller = ControllerFactory.getInstance().getUnassignedPeriodController();
            result = controller.ProcessEobrReaderHistoryGenII(currentEvents, empLog);

            ProcessUnassignedDrivingPeriodResult(result, empLog);

            // We need to get the last odometer and gps location and set them.  Begin by finding the last DRIVE_OFF event from the history list.
            float lastOdometer = 0F;
            GpsLocation lastGpsLoc = null;

            TripReport tripRpt = null;

            for(EventRecord currentEvent : currentEvents) {
                if (currentEvent.getEventType() == EventTypeEnum.DRIVEEND) {
                    tripRpt = currentEvent.getTripReportData();

                    lastOdometer = tripRpt.getOdometer();
                    lastGpsLoc = new GpsLocation(tripRpt.getDataTimecodeAsDate(), tripRpt.getLatitude(), tripRpt.getLongitude());
                }
            }

            if (lastOdometer > 0F) this.setLastValidOdometerReading(lastOdometer);
            if (lastGpsLoc != null) this.setLastValidGPSLocation(lastGpsLoc);
        }

        return result;
    }

    private void ProcessUnassignedDrivingPeriodResult(UnassignedDrivingPeriodResult result, EmployeeLog empLog)
    {
        // We are doing this for the case in which a driving period has started, but the driver is NOT logged into KMB yet.  Once the driver
        // logs into KMB, the "pre-login" driving time is created as an unassigned driving period and a new DRIVE_ON is fired.  Typically, this
        // DRIVE_ON will happen during the "read history".  In that case the DRIVE_ON is "orphaned".  We keep track of it and deal with it here.
        if (result.getOrphanedTripTime() != null) {
            User currentDriver = this.getCurrentDesignatedDriver();
            Date orphanedTripTime = result.getOrphanedTripTime();
            float orphanedOdometer = result.getOrphanedOdometer();
            float orphanedLatitude = result.getOrphanedLatitude();
            float orphanedLongitude = result.getOrphanedLongitude();

            GpsLocation gpsLoc = null;
            if (orphanedLatitude != 0F && orphanedLongitude != 0F)
            {
                gpsLoc = new GpsLocation(orphanedTripTime, orphanedLatitude, orphanedLongitude);
            }

            Location location = new Location();
            location.setGpsInfo(gpsLoc);
            location.setOdometerReading(orphanedOdometer);

            this.setLastValidOdometerReading(orphanedOdometer);
            this.setLastValidGPSLocation(gpsLoc);

            orphanedTripTime = DateUtility.ConvertToPreviousLogEventTime(orphanedTripTime);

            if (location.getGpsInfo() != null)
            {
                IAPIController empLogController = MandateObjectFactory.getInstance(this.getContext(),GlobalState.getInstance().getFeatureService()).getCurrentEventController();
                empLogController.ReverseGeocodeLocationAsync(empLog, gpsLoc, new ReverseGeocodeAndPublishLocation(currentDriver, empLog, orphanedTripTime, new DutyStatusEnum(DutyStatusEnum.DRIVING), location));
            }

            empLog = this.PerformStatusChange(currentDriver, empLog, orphanedTripTime, new DutyStatusEnum(DutyStatusEnum.DRIVING), location, true);
            GlobalState.getInstance().setPotentialDrivingStopTimestamp(null);
        }

        // Update the drivers login state.
        this.setCurrentDriversLog(empLog);
        if (this.IsCurrentUserTheDriver()) {
            // the driver is also the current active user, so update the regular log as well
            this.setCurrentEmployeeLog(empLog);
        }
    }

    /// <summary>
    /// Process the historical data records from the EOBR looking for
    /// unassigned driving periods.
    /// Answer if there are any driving periods that may impact the
    /// current log being processed.   A driving period may impact the
    /// current log if it occurred after time that the user logged in
    /// and caused the current log to be created.
    /// </summary>
    /// <param name="e"></param>
    /// <returns></returns>
    public Bundle ProcessEobrReaderHistoryEvent(EobrHistoryEventArgs e) throws KmbApplicationException
    {
        Bundle retVal = new Bundle();
        retVal.putBoolean(EobrReader.DETECTEDDRIVINGPERIODS, false);
        retVal.putBoolean(EobrReader.DETECTEDPRELOGINDRIVINGPERIODS, false);
        UnassignedPeriodController ctrlr = ControllerFactory.getInstance().getUnassignedPeriodController();
        EmployeeLog empLog = this.getCurrentDriversLog();

        if (e.getHistoryList() == null)
        {
            // when the history list is null, this means that an error may have
            // occurred reading history
            retVal.putBoolean(EobrReader.DETECTEDDRIVINGPERIODS, false);
            retVal.putBoolean(EobrReader.DETECTEDPRELOGINDRIVINGPERIODS, false);

            if (e.getHasFailureOccurred())
            {
                // Report an EOBR failure so that paper logs are required.
                FailureController failureCtrlr = new FailureController(getContext());
                failureCtrlr.ReportEobrFailure(empLog, this.getCurrentClockHomeTerminalTime(), "Failed to read historical driving records.");
            }
        }
        else
        {
            Bundle b = ctrlr.ProcessEobrReaderHistoryEvent(e, this.getCurrentDesignatedDriver(), empLog);

            retVal.putBoolean(EobrReader.DETECTEDDRIVINGPERIODS, b.getBoolean(EobrReader.DETECTEDDRIVINGPERIODS));
            retVal.putBoolean(EobrReader.DETECTEDPRELOGINDRIVINGPERIODS, b.getBoolean(EobrReader.DETECTEDPRELOGINDRIVINGPERIODS));

            // update the drivers log in state
            this.setCurrentDriversLog(empLog);
            if (this.IsCurrentUserTheDriver())
            {
                // the driver is also the current active user, so update the regular log as well
                this.setCurrentEmployeeLog(empLog);
            }

            // next, look through the history to update the last valid odometer and GPS readings
            List<StatusRecord> recList = e.getHistoryList();
            float odometer = 0F;
            GpsLocation gpsLoc = null;
            for (int index = recList.size() - 1; index >= 0; index--)
            {
                StatusRecord rec = recList.get(index);

                if (odometer == 0F && rec.getOdometerReading() > 0F)
                {
                    // found the last odometer
                    odometer = rec.getOdometerReading();
                }

                if (gpsLoc == null && rec.IsGpsLocationValid())
                {
                    // found the last gps value
                    gpsLoc = new GpsLocation(rec.getGpsTimestampUtc(), rec.getGpsLatitude(), rec.getNorthSouthInd(), rec.getGpsLongitude(), rec.getEastWestInd());
                }

                if (odometer > 0F && gpsLoc != null)
                {
                    // once both have been found, then break out
                    break;
                }
            }

            if (odometer > 0F) this.setLastValidOdometerReading(odometer);
            if (gpsLoc != null) this.setLastValidGPSLocation(gpsLoc);
        }

        return retVal;
    }

    public Bundle GetCurrentEventValues(String logDateKey, String startTimeKey, String dutyStatusKey)
    {
        EmployeeLog empLog = this.getCurrentEmployeeLog();
        EmployeeLogEldEvent evt = EmployeeLogUtilities.GetLastEventInLog(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus,empLog);

        Bundle currentEvent = new Bundle();
        currentEvent.putString(logDateKey, DateUtility.getHomeTerminalDateFormat().format(empLog.getLogDate()));
        currentEvent.putString(startTimeKey, DateUtility.getHomeTerminalDateTimeFormat().format(evt.getStartTime()));
        currentEvent.putString(dutyStatusKey, evt.getDutyStatusEnum().getString(getContext()));

        return currentEvent;
    }

    public CurrentEvent GetCurrentEventValues() {
        final EmployeeLog empLog = this.getCurrentEmployeeLog();
        EmployeeLogEldEvent evt = EmployeeLogUtilities.GetLastEventInLog(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus,empLog);

        CurrentEvent event = new CurrentEvent();
        event.logDate = empLog.getLogDate();
        event.eventTimestamp = evt.getStartTime();
        event.dutyStatusEnum = evt.getDutyStatusEnum();
        event.isAutomaticDrivingEvent = evt.isAutomaticDrivingEvent();

        Log.v("GetCurrentEventValues", event.toString());

        return event;
    }

    public RuleSetTypeEnum GetCurrentEventValues_RulesetType()
    {
        EmployeeLog empLog = this.getCurrentEmployeeLog();
        EmployeeLogEldEvent evt = EmployeeLogUtilities.GetLastEventInLog(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus,empLog);
        return evt.getRulesetType();
    }

    public Location GetCurrentEventValues_Location()
    {
        if(this.getCurrentUser() == null)
            return null;

        EmployeeLog empLog = this.getCurrentEmployeeLog();
        EmployeeLogEldEvent evt = EmployeeLogUtilities.GetLastEventInLog(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus,empLog);
        return evt.getLocation();
    }

    public boolean getIsExtendDrivingSegmentEnabled()
    {
        return GlobalState.getInstance().getIsExtendDrivingSegmentEnabled();
    }
    public void setIsExtendDrivingSegmentEnabled(boolean isExtendDrivingSegmentEnabled)
    {
        GlobalState.getInstance().setIsExtendDrivingSegmentEnabled(isExtendDrivingSegmentEnabled);
    }

    public boolean getHasExtendedDrivingSegment()
    {
        return GlobalState.getInstance().getHasExtendedDrivingSegment();
    }
    public void setHasExtendedDrivingSegment(boolean hasExtendedDrivingSegment)
    {
        GlobalState.getInstance().setHasExtendedDrivingSegment(hasExtendedDrivingSegment);
    }

    public void setCurrentDriversLog(EmployeeLog log)
    {
        // save it to state
        GlobalState.getInstance().setCurrentDriversLog(log);
    }

    public EmployeeLog CreateCurrentLog(DutyStatusEnum dutyStatus, ExemptLogTypeEnum exemptLogType)
    {
        return CreateCurrentLog(new DutyStatusEnum(DutyStatusEnum.OFFDUTY), dutyStatus, exemptLogType);
    }

    /// Create the employee log for current day.
    /// First, look to see if today's log exists already
    /// If not, then a new one will be created.
    /// Automatically add an on-duty event to the log if the user specified
    /// this at login time.
    private EmployeeLog CreateCurrentLog() {
        return this.CreateCurrentLog(new DutyStatusEnum(DutyStatusEnum.OFFDUTY), new DutyStatusEnum(DutyStatusEnum.ONDUTY), new ExemptLogTypeEnum(ExemptLogTypeEnum.NULL));
    }

    public EmployeeLog CreateCurrentLog(DutyStatusEnum initialDutyStatus, DutyStatusEnum loginEventDutyStatus, ExemptLogTypeEnum exemptLogType)  {
        // convert the UTC time into the timezome of the driver's home terminal
        Date now = this.getCurrentClockHomeTerminalTime();

        // fetch the local log for today, or create a new one
        IAPIController cntlr = MandateObjectFactory.getInstance(this.getContext(), GlobalState.getInstance().getFeatureService()).getCurrentEventController();
        EmployeeLog empLog = cntlr.GetLocalEmployeeLogOrCreateNew(this.getCurrentUser(), now, initialDutyStatus);

        // automatically add an on-duty event to the log using the offset
        // the driver specified at login time
        // TCH - 2/7/2012 - if restarting the app because the app process
        // was killed, don't add login event to current log
        // 2/20/12 JHM - Added the login event during an app restart when
        // in a Driving duty status (for auto-claim logic)

        boolean isLoggingInSpecialDriving = GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled() && ((GlobalState.getInstance().getIsInPersonalConveyanceDutyStatus() && loginEventDutyStatus.getValue() == 1) || (loginEventDutyStatus.getValue() == 4 && (GlobalState.getInstance().getIsInYardMoveDutyStatus() || GlobalState.getInstance().getIsInHyrailDutyStatus() || GlobalState.getInstance().getIsInNonRegDrivingDutyStatus())));

        //added check here for special driving, since a login event is already created in this case
        EmployeeLogEldEvent lastLogEldEvent = EmployeeLogUtilities.GetLastEventInLog(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus, empLog);
        boolean isDutyStatusOfLastEventInLogEqualsDriving = lastLogEldEvent != null && lastLogEldEvent.getDutyStatusEnum() == new DutyStatusEnum(DutyStatusEnum.DRIVING);
        boolean shouldAddLoginEvent =
                (!cntlr.getAppRestartFlag() && !isLoggingInSpecialDriving ||
                                (cntlr.getAppRestartFlag() && isDutyStatusOfLastEventInLogEqualsDriving)
                ) && !empLog.getIsExemptFromELDUse();

        if (shouldAddLoginEvent && !empLog.getIsTransitionalLog()) {
            cntlr.AddLoginEventToCurrentLog(empLog, loginEventDutyStatus);
        }

        empLog.setExemptLogType(exemptLogType);
        empLog.setIsCertified(false);

        // exempt logs require the driver to return to their work location
        if (exemptLogType.getValue() != ExemptLogTypeEnum.NULL)
            empLog.setHasReturnedToLocation(true);

        // save the current log to state
        this.setCurrentEmployeeLog(empLog);

        // check to see if any delayed failures need to be applied
        FailureController failurCntlr = new FailureController(getContext());
        failurCntlr.ApplyDelayedFailures(empLog);

        // persist the log
        cntlr.SaveLocalEmployeeLog(empLog);

        return empLog;
    }

    public EmployeeLog CreateNewLogIfNecessary(User usr, EmployeeLog empLog, Date timestamp){
        return CreateNewLogIfNecessary(usr, empLog, timestamp, null);
    }

    /// <summary>
    /// Determine if the timestamp belongs on a future log.
    /// If so, create and save the future log.
    /// The current log will be transitioned into the future log.
    /// </summary>
    /// <param name="usr"></param>
    /// <param name="empLog"></param>
    /// <param name="timestamp"></param>
    public EmployeeLog CreateNewLogIfNecessary(User usr, EmployeeLog empLog, Date timestamp, Date potentialDrivingStopTimestamp)
    {
        // first, determine if the timestamp belongs on a different log than the current one
        if (this.BelongOnFutureLog(usr, empLog, timestamp))
        {
            IAPIController empLogController = MandateObjectFactory.getInstance(this.getContext(),GlobalState.getInstance().getFeatureService()).getCurrentEventController();

            EmployeeLog newLog = empLog;
            Date newLogDate = newLog.getLogDate();
            DateTime eventTime = new DateTime(timestamp);
            DateTime currentLogDate = new DateTime(empLog.getLogDate());

            int daysBetween = Days.daysBetween(currentLogDate, eventTime).getDays();
            newLogDate = DateUtility.AddDays(newLogDate, daysBetween);

            //We do not want to overwrite logs
            EmployeeLog existingLog = empLogController.GetLocalEmployeeLog(usr, newLogDate);
            if(existingLog == null) {
                List<EmployeeLog> logsForDate = empLogController.GetEmployeeLogs(usr, newLogDate);

                //sort by log key - if there are multiple downloaded logs,
                //we want the most recently downloaded one
                if(logsForDate.size() > 1) {
                    Collections.sort(logsForDate, new Comparator<EmployeeLog>() {
                        @Override
                        public int compare(EmployeeLog el1, EmployeeLog el2) {
                            int result = 1;
                            if (el1.getPrimaryKey() == el2.getPrimaryKey()) {
                                result = 0;
                            } else if (el1.getPrimaryKey() > el2.getPrimaryKey()) {
                                result = -1;
                            }

                            return result;
                        }
                    });
                }

                if(logsForDate.size() > 0) {
                    existingLog = logsForDate.get(0);
                }
            }

            if(existingLog == null) {
                newLog = empLogController.CreateNewLogForTransition(usr, newLog, newLogDate, potentialDrivingStopTimestamp);
            } else {
                newLog = existingLog;
            }
            //this switches the status to a local active log if the log exists already
            empLogController.SaveLocalEmployeeLog(usr, newLog);

            // because there is a new log, need to update the log in state data
            // save the new one to state, as the current log being processed
            if (this.IsTheDriver(usr))
            {
                this.setCurrentDriversLog(newLog);
            }
            if(this.IsTheCurrentActiveUser(usr))
            {
                this.setCurrentEmployeeLog(newLog);
            }

            // reset the motion detector to pick up new values from the new log date
            // the next time it processes a status record
            this.setVehicleMotionDetector(null);

            migrateSpecialDriving(empLogController, newLog, empLog);

            // return the new log back
            return newLog;
        }
        else
            return empLog;
    }

    private void migrateSpecialDriving(IAPIController empLogController, EmployeeLog newLog, EmployeeLog currentLog) {
        // when in special driving mode like PC, Hyrail, or NonReg mode, we need to start it again on the new log
        // the start time of the event will be the beginning of the new log
        ISpecialDrivingController specialDrivingController = SpecialDrivingFactory.getControllerInDrivingSegment();
        if(specialDrivingController == null) {
            //under the mandate, for PC and YM we need to end the category at the end of the log
            //regardless of whether there's a duty segment
            ISpecialDrivingController pcController = SpecialDrivingFactory.getControllerForDrivingCategory(EmployeeLogProvisionTypeEnum.PERSONALCONVEYANCE);
            if(pcController.getIsInSpecialDutyStatus()) {
                specialDrivingController = pcController;
            }
        }

        EmployeeLogEldEvent lastEventInLog = EmployeeLogUtilities.GetLastEventInLog(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus, newLog);
        if(specialDrivingController != null) {
            // note: pass in 'null' for location so that the current location values are used
            specialDrivingController.StartSpecialDrivingStatus(lastEventInLog.getStartTime(), null, newLog, true, currentLog);
        } else {
            EmployeeLogEldEvent ymEventToMigrate = getYardMoveEventToMigrate(currentLog);
            if(ymEventToMigrate != null) {
                try {
                    empLogController.CheckForAndCreateYardMoveEvent(newLog, lastEventInLog.getStartTime(), null, ymEventToMigrate.getEventComment());
                } catch(Throwable t) {
                    HandleException(new Exception("Error trying to create new yard move event during midnight transition.", t), "LogEntryController");
                }
            }
        }
    }

    private EmployeeLogEldEvent getYardMoveEventToMigrate(EmployeeLog currentLog) {
        ArrayList<EmployeeLogEldEvent> eldEventArray = new ArrayList<>(Arrays.asList(currentLog.getEldEventList().getEldEventList(EldEventAdapterList.ListAccessorModifierEnum.NonDutyStatus)));
        int testIndex = eldEventArray.size() - 1;
        EmployeeLogEldEvent event = null;
        boolean foundYM = false;
        boolean foundClear = false;

        User user = GlobalState.getInstance().getLoggedInUser(currentLog.getEmployeeId());
        Date logStartTime = EmployeeLogUtilities.CalculateLogStartTime(this.getContext(), currentLog.getLogDate(), user.getHomeTerminalTimeZone());
        Date endOfLogTime = DateUtility.AddSeconds(logStartTime, DateUtility.SECONDS_PER_DAY - 1);

        while (testIndex >= 0) {
            event = eldEventArray.get(testIndex);

            if (event.getEventType() == Enums.EmployeeLogEldEventType.ChangeInDriversIndication) {
                if (event.getEventCode() == EmployeeLogEldEventCode.ChangeInDriversIndication_PCYMWT_Cleared) {

                    //If a special driving category was in effect at the end of the day we need to go back
                    //further to see if it was YM or PC. If it wasn't in effect up until the end then we
                    //don't need to migrate it to the new log.
                    if(event.getEventDateTime().equals(endOfLogTime)) {
                        foundClear = true;
                    } else {
                        break;
                    }
                } else {
                    foundYM = event.getEventCode() == EmployeeLogEldEventCode.ChangeInDriversIndication_YardMoves;
                    break;
                }
            }
            testIndex--;
        }

        if(foundYM && foundClear) {
            return event;
        }

        return null;
    }

    /// <summary>
    /// Perform verification on the timestamp that it's valid for insert of a
    /// new log event.  The following is being checked for;
    ///
    /// 1.  timestamp is not before than current event on the log
    /// 2.  timestamp is not in the future
    /// </summary>
    /// <param name="timestamp">timestamp to examine.</param>
    /// <returns></returns>
    public String VerifyTimestampForNewEvent(Date timestamp)
    {
        String answer = null;

        // convert the timestamp to the previous event boundary
        Date timeToCompare = DateUtility.ConvertToPreviousLogEventTime(timestamp);

        EmployeeLog empLog = this.getCurrentEmployeeLog();
        EmployeeLogEldEvent currentEvent = EmployeeLogUtilities.GetLastEventInLog(empLog);

        // 1. verify that the new time is earlier than the current event on the log
        if (timeToCompare.compareTo(currentEvent.getStartTime()) <= 0)
        {
            answer = String.format(this.getContext().getString(R.string.msgnewtimecannotbeearlierthancurrenteventime), DateUtility.getHomeTerminalTime12HourFormat().format(timestamp), DateUtility.getHomeTerminalTime12HourFormat().format(currentEvent.getStartTime()));
        }

        // 2. verify that the new time is not in the future
        Date currentHomeTerminalTime = this.getCurrentClockHomeTerminalTime();
        if (timeToCompare.compareTo(currentHomeTerminalTime) > 0)
        {
            answer = String.format(this.getContext().getString(R.string.msgnewtimecannotbenewerthancurrenttime), DateUtility.getHomeTerminalTime12HourFormat().format(timeToCompare), DateUtility.getHomeTerminalTime12HourFormat().format(currentHomeTerminalTime));
        }

        return answer;
    }

    /// <summary>
    /// Perform verification on the timestamp that it's valid for update of the
    /// last log event.  The following is being checked for;
    ///
    /// 1.  A driving state can not be effected, either the current or previous
    ///     event is driving
    /// 2.  timestamp is not newer than current event on the log
    /// 3.  timestamp is newer than previous event in the log.
    ///     If there is no previous event, then make sure that timestamp
    ///     is at least newer than the log start time.
    /// </summary>
    /// <param name="timestamp">timestamp to examine.</param>
    /// <returns></returns>
    public String VerifyTimestampForUpdate(Date timestamp)
    {
        String answer = null;

        Date timeToCompare = DateUtility.ConvertToPreviousLogEventTime(timestamp);

        EmployeeLog empLog = this.getCurrentEmployeeLog();
        EmployeeLogEldEvent currentEvent = EmployeeLogUtilities.GetLastEventInLog(empLog);
        EmployeeLogEldEvent previousEvent = EmployeeLogUtilities.GetSecondToLastEventInLog(empLog);

        // 1. Verify the neither the current or previous event is driving
        if (currentEvent.getDutyStatusEnum().getValue() == DutyStatusEnum.DRIVING
                || (previousEvent != null && previousEvent.getDutyStatusEnum().getValue() == DutyStatusEnum.DRIVING))
        {
            // if the current event is driving, or the previous event is driving, this is not allowed
            answer = this.getContext().getString(R.string.msgtimecannotbechanged);
        }
        else
        {
            // 2. verify that the new time is not newer than the current event on the log
            if (timeToCompare.compareTo(currentEvent.getStartTime()) > 0)
            {
                answer = String.format(this.getContext().getString(R.string.msgnewtimecannotbenewerthatcurrenteventime), DateUtility.getHomeTerminalTime12HourFormat().format(timestamp), DateUtility.getHomeTerminalTime12HourFormat().format(currentEvent.getStartTime()));
            }
            else
            {
                // 3. verify that the new time is newer than the previous log event
                if (previousEvent != null)
                {
                    // there is an event defined previous to the one we're looking at
                    if (timeToCompare.compareTo(previousEvent.getStartTime()) <= 0)
                    {
                        answer = String.format(this.getContext().getString(R.string.msgnewtimemustbenewerthanpreveventtime), DateUtility.getHomeTerminalTime12HourFormat().format(timestamp), DateUtility.getHomeTerminalTime12HourFormat().format(previousEvent.getStartTime()));
                    }
                }
                else
                {
                    // not enough events, to do this check
                    // verify that the time is not older very first event on the log
                    // calc the log start time
                    Date currentHomeTerminalTime = this.getCurrentClockHomeTerminalTime();
                    Date logStartTime = EmployeeLogUtilities.CalculateLogStartTime( this.getContext(), currentHomeTerminalTime, this.getCurrentUser().getHomeTerminalTimeZone() );
                    if (timeToCompare.compareTo(logStartTime) < 0)
                    {
                        answer = String.format(this.getContext().getString(R.string.msgnewtimemustbenewerthanlogstarttime), DateUtility.getHomeTerminalTime12HourFormat().format(timestamp), DateUtility.getHomeTerminalTime12HourFormat().format(logStartTime));
                    }
                }
            }
        }

        return answer;
    }

    /// <summary>
    /// Update the starttime in the most recent event
    /// </summary>
    /// <param name="location">location value to assign to most recent event</param>
    public void UpdateCurrentEventTimestamp(Date timestamp)
    {
        // fetch the most recent log event on the current log being built
        EmployeeLog empLog = this.getCurrentEmployeeLog();
        EmployeeLogEldEvent evt = EmployeeLogUtilities.GetLastEventInLog(EldEventAdapterList.ListAccessorModifierEnum.ALL,empLog);

        // convert the timestamp to the previous event boundary
        Date nextEventTime = DateUtility.ConvertToPreviousLogEventTime(timestamp);
        evt.setStartTime(nextEventTime);

        if (nextEventTime.compareTo(empLog.getMobileStartTimestamp()) < 0)
        {
            // if the next event is earlier than the mobile start time,
            // then move the start time back to this time
            empLog.setMobileStartTimestamp(nextEventTime);
        }

        if(doesLastEventNeedManualLocation(evt, timestamp))
        {
            evt.setLocation(new Location());
            evt.setRequiresManualLocation(true);
        }

        if( GlobalState.getInstance().getCurrentUser().getIsMobileExemptLogAllowed()) {
            // validate that all the exempt logs are still valid
            ExemptLogValidationController ctrlr = new ExemptLogValidationController(this.getContext());
            ctrlr.PerformCompleteValidationForCurrentLog(empLog, true);
        }

        // persist the changes to the current day's log, just in case they have not been committed
        IAPIController empLogController = MandateObjectFactory.getInstance(this.getContext(),GlobalState.getInstance().getFeatureService()).getCurrentEventController();
        empLogController.SaveLocalEmployeeLog(empLog);
        this.setCurrentEmployeeLog(empLog);
    }

    /**
     * Determines if the last event in the log should have the location manually set
     *
     * @param timestamp
     * @return
     */
    private boolean doesLastEventNeedManualLocation(EmployeeLogEldEvent event, Date timestamp)
    {
        boolean manualLocation = false;

        // convert the timestamp to the previous event boundary
        Date nextEventTime = DateUtility.ConvertToPreviousLogEventTime(timestamp);

        //if this event has a GPS location and there is an unassigned driving period
        //that ends after the new time then clear out the location - we can't assume
        //the driver was actually at this location
        if(!event.getLocation().getGpsInfo().IsEmpty())
        {
            UnassignedPeriodController upController = ControllerFactory.getInstance().getUnassignedPeriodController();
            List<UnclaimedDrivingPeriod> unclaimedPeriods = upController.GetUnclaimedDrivingPeriodsForCurrentLog();

            for(UnclaimedDrivingPeriod unclaimedPeriod : unclaimedPeriods)
            {
                if(unclaimedPeriod.getUnassignedDrivingPeriod().getStopTime().compareTo(nextEventTime) > 0)
                {
                    manualLocation = true;
                    break;
                }
            }
        }

        return manualLocation;
    }

    /// <summary>
    /// Answer if the timestamp belongs on a future log.   This means that the
    /// timestamp is in the future with respect to the log date of EmployeeLog.
    /// </summary>
    /// <param name="empLog"></param>
    /// <param name="timestamp"></param>
    /// <returns></returns>
    public boolean BelongOnFutureLog(User user, EmployeeLog empLog, Date timestamp)
    {
        return BelongOnFutureLog(user, empLog.getLogDate(), timestamp);
    }

    public boolean BelongOnFutureLog(User user, Date currentLogDate, Date timestamp)
    {
        boolean answer = false;

        // first, calculate the start time of tomorrows log
        Date nextLogDate = DateUtility.AddDays(currentLogDate, 1);
        Date nextLogStartDate = EmployeeLogUtilities.CalculateLogStartTime(this.getContext(), nextLogDate, user.getHomeTerminalTimeZone());

        // second, determine if the timestamp belongs on a future log
        if (nextLogStartDate.compareTo(timestamp) < 0)
        {
            // timestamp belongs on future log
            answer = true;
        }
        return answer;
    }

    public Date GetLogDateForEvent(EmployeeLogEldEvent logEvent)
    {
        TimeZoneEnum timeZone = GlobalState.getInstance().getCurrentUser().getHomeTerminalTimeZone();

        //using the date component of this event, figure out when the log with that log date will begin
        Date logStartTime = EmployeeLogUtilities.CalculateLogStartTime(getContext(), logEvent.getStartTime(), timeZone);

        //if the event begins after the calculated log date, then we know it belongs on this log
        // e.g. if the event time is 2015-08-02 08:30, and the calculated log start time is 2015-08-02 08:00,
        // then the log date is 2015-08-02.  But if the calculated log start time is 2015-08-02 09:00, then
        // this event actually belongs on the log with a log date of 2015-08-01.
        Date logDate;
        if(logEvent.getStartTime().compareTo(logStartTime) >= 0)
            logDate = DateUtility.GetDateFromDateTime(logStartTime);
        else
            logDate = DateUtility.GetDateFromDateTime(DateUtility.AddDays(logStartTime, -1));

        return logDate;
    }

    /// <summary>
    /// If there is a log being used already, then update it with this new one.
    /// Otherwise, do nothing.   The next time that the current log is fetched it
    /// will use the new one anyways.
    /// </summary>
    /// <param name="newLocalLog"></param>
    public void UpdateCurrentLog(EmployeeLog newLocalLog)
    {
        EmployeeLog log = GlobalState.getInstance().getCurrentEmployeeLog();
        if (log != null)
        {
            this.setCurrentEmployeeLog(newLocalLog);
        }
    }

    /// <summary>
    /// Answer the last known, valid, GPS position
    /// </summary>
    public GpsLocation getLastValidGPSLocation()
    {
        // read from state
        GpsLocation answer = null;
        if (GlobalState.getInstance().getLastGPSLocation()!= null)
        {
            // found in state
            answer = GlobalState.getInstance().getLastGPSLocation();
        }
        return answer;
    }

    public void setLastValidGPSLocation(GpsLocation gpsLocation)
    {
        // save to state
        GlobalState.getInstance().setLastGPSLocation(gpsLocation);
    }

    public boolean getCurrentGPSLocation(Context context, DutyStatusEnum dutyStatus, Location location) {
        // 11/27/12 AMO: When connected to an EOBR that is providing GPS
        // coords, do not ask for manual location entry when logging in to system
        boolean gpsAvail = false;
        StatusRecord rec = new StatusRecord();
        try {
            // If we are connected to an EOBR then try and get the current data
            if (EobrReader.getInstance().getCurrentConnectionState() == ConnectionState.ONLINE) {
                EobrReader.getInstance().Technician_GetCurrentData(rec, false);

                // If there is a status record, then try and get the gpsinformation
                GpsLocation gpsLoc = null;
                if (rec != null && rec.IsGpsLocationValid()) {
                    if (rec.getNorthSouthInd() == '-' || rec.getEastWestInd() == '-') {
                        // Gen2
                        gpsLoc = new GpsLocation(rec.getGpsTimestampUtc(), rec.getGpsLatitude(), rec.getGpsLongitude());
                    } else {
                        // Gen1
                        gpsLoc = new GpsLocation(rec.getGpsTimestampUtc(), rec.getGpsLatitude(), rec.getNorthSouthInd(), rec.getGpsLongitude(), rec.getEastWestInd());
                    }

                    // 01/02/13 AMO: If there is no GPS data then prompt for location
                    if(rec.getGpsTimestampUtc().getTime() > 0)
                        gpsAvail = true;
                }

                // Decode the GPS location information
                location.setGpsInfo(gpsLoc);
                EmployeeLog empLog = GlobalState.getInstance().getCurrentEmployeeLog();
                IAPIController empLogController = MandateObjectFactory.getInstance(this.getContext(),GlobalState.getInstance().getFeatureService()).getCurrentEventController();
                if(EmployeeLogUtilities.ShouldUseReducedGpsPrecision())
                {
                    gpsLoc.setLatitudeDegrees(EmployeeLogUtilities.GetReducedPrecisionGPSForFloat(rec.getGpsLatitude()));
                    gpsLoc.setLongitudeDegrees(EmployeeLogUtilities.GetReducedPrecisionGPSForFloat(rec.getGpsLongitude()));

                    location.getGpsInfo().setLatitudeDegrees(EmployeeLogUtilities.GetReducedPrecisionGPSForFloat(rec.getGpsLatitude()));
                    location.getGpsInfo().setLongitudeDegrees(EmployeeLogUtilities.GetReducedPrecisionGPSForFloat(rec.getGpsLongitude()));

                }
                empLogController.ReverseGeocodeLocationAsync(empLog, gpsLoc, new ReverseGeocodeAndUpdateCurrentEventLocation(location));

            }
        } catch (Exception ex) {
            // Exception
            gpsAvail = false;
            Log.e("LogEntryController", "Error in getCurrentGPSLocation: " + ex.getMessage() + " " + TimeKeeper.getInstance().now().toString());
        }

        return gpsAvail;
    }

    /**
     * Gets the current GPS location from the EOBR and tries to reverse geocode it synchronously
     *
     * @return The {@link GpsLocation} if successfully retrieved and null otherwise
     */
    public GpsLocation getCurrentGPSLocation() {
        GpsLocation gpsLocation = null;
        try {
            StatusRecord rec = new StatusRecord();
            // If we are connected to an EOBR then try and get the current data
            if (EobrReader.getInstance().getCurrentConnectionState() == ConnectionState.ONLINE) {
                EobrReader.getInstance().Technician_GetCurrentData(rec, false);

                // If there is a status record, then try and get the GPS information
                if (rec.IsGpsLocationValid() && rec.getGpsTimestampUtc().getTime() > 0) {
                    if (rec.getNorthSouthInd() == '-' || rec.getEastWestInd() == '-') {
                        // Gen2
                        gpsLocation = new GpsLocation(rec.getGpsTimestampUtc(), rec.getGpsLatitude(), rec.getGpsLongitude());
                    } else {
                        // Gen1
                        gpsLocation = new GpsLocation(rec.getGpsTimestampUtc(), rec.getGpsLatitude(), rec.getNorthSouthInd(), rec.getGpsLongitude(), rec.getEastWestInd());
                    }
                    IAPIController empLogController = MandateObjectFactory.getInstance(this.getContext(), GlobalState.getInstance().getFeatureService()).getCurrentEventController();
                    empLogController.ReverseGeocodeLocation(Collections.singletonList(gpsLocation));
                }
            }
        } catch (Exception ex) {
            Log.e("LogEntryController", "Error in getCurrentGPSLocation", ex);
        }
        return gpsLocation;
    }

    /// <summary>
    /// Answer the last valid odometer reading that was reported from the EOBR
    /// </summary>
    public float getLastValidOdometerReading()
    {
        return GlobalState.getInstance().getLastValidOdometerReading();
    }

    public void setLastValidOdometerReading(float odometerReading)
    {
        // save to state
        GlobalState.getInstance().setLastValidOdometerReading(odometerReading);
    }

    public boolean ShouldDownloadRecords()
    {
        LoginController controller = new LoginController(this.getContext());
        boolean newUser = controller.getIsNewUserBeingLoggedIn();

        controller.setIsNewUserBeingLoggedIn(false);

        return newUser;
    }

    public boolean IsCurrentUserTheDriver()
    {
        return this.IsTheDriver(this.getCurrentUser());
    }

    public boolean IsTheDriver(User user)
    {
        boolean isDriver = false;
        User dd = this.getCurrentDesignatedDriver();
        if (dd != null && user != null && user.getCredentials() != null && user.getCredentials().getEmployeeId().compareTo(dd.getCredentials().getEmployeeId())==0)
        {
            isDriver = true;
        }

        return isDriver;
    }

    /// <summary>
    /// Answer if the user is also the currently active driver.
    /// </summary>
    /// <param name="usr">User to check</param>
    /// <returns></returns>
    public boolean IsTheCurrentActiveUser(User usr)
    {
        boolean isCurrentUser = false;
        User currentUser = this.getCurrentUser();
        if (currentUser != null &&
                usr != null &&
                usr.getCredentials() != null &&
                usr.getCredentials().getEmployeeId().equals(currentUser.getCredentials().getEmployeeId()))
        {
            isCurrentUser = true;
        }
        return isCurrentUser;
    }

    /// <summary>
    /// Answer if it's OK to shutdown the app
    /// It's OK to shutdown once all users have been logged out.
    /// </summary>
    /// <returns></returns>
    public boolean IsOKToShutdownApp()
    {
        boolean okToShutdown = false;

        // check if all of the team drivers have logged out already
        if (this.getLoggedInUserList().size() == 0)
        {
            okToShutdown = true;
        }

        return okToShutdown;
    }

    /// <summary>
    /// Answer if the current log (today's log) has already been created.
    /// </summary>
    /// <returns></returns>
    public boolean IsCurrentLogCreated()
    {
        EmployeeLog log = GlobalState.getInstance().getCurrentEmployeeLog();
        return log != null;
    }

    public boolean IsVehicleInMotion()
    {
        boolean motion = false;

        // 10/16/12 JHM - Vehicle motion check differs between Gen1 & Gen2
        if(EobrReader.getInstance().isEobrGen1())
            motion = this.getVehicleMotionDetector().getIsVehicleInMotion();
        else
        {
            motion = (GlobalState.getInstance().getPotentialDrivingStopTimestamp() == null);
        }

        return motion;
    }

    public boolean getRoadsideInspectionMode()
    {
        return GlobalState.getInstance().getRoadsideInspectionMode();
    }
    public void setRoadsideInspectionMode(boolean roadsideInspectionMode)
    {
        GlobalState.getInstance().setRoadsideInspectionMode(roadsideInspectionMode);
    }

    /// <summary>
    /// Turn on/off the Roadside Inspection mode of the application when
    /// the password matches the current user's login password credentials.
    /// Roadside inspection mode effects the actions available on various
    /// screens, such as the main menu of the application.
    /// </summary>
    /// <param name="password">user's login password credentials</param>
    /// <returns>true if the password credentials match, false otherwise</returns>
    public boolean ToggleRoadsideInspectionMode(String password)
    {
        boolean isSuccessful = false;

        // does the password match the user's login credentials?
        if (password.compareTo(this.getCurrentUser().getCredentials().getPassword()) == 0)
        {
            // toggle the road-side inspection mode
            this.setRoadsideInspectionMode(!this.getRoadsideInspectionMode());

            if (this.getRoadsideInspectionMode())
            {
                // if roadside inspection is just turned on
                // then verify that any reverse geo-coding

                IAPIController empLogCntrlr = MandateObjectFactory.getInstance(this.getContext(),GlobalState.getInstance().getFeatureService()).getCurrentEventController();
                empLogCntrlr.ValidateAllReverseGeocoding();
            }

            isSuccessful = true;
        }

        return isSuccessful;
    }

    /// <summary>
    /// Answer if all locations in the database have been geocoded.
    /// </summary>
    /// <returns></returns>
    public boolean IsLocationGeocodingComplete()
    {
        IEmployeeLogFacade empLogFacade = new EmployeeLogFacade(this.getContext(), this.getCurrentUser());
        List<EmployeeLogEldEvent> logEventList = empLogFacade.FetchUnsubmittedLocationsThatRequireGeocoding();

        return logEventList.size() == 0;
    }

    /// <summary>
    /// Answer the current connection status for the EOBR device
    /// </summary>
    public String getStatusMessage()
    {
        String msg = null;
        FailureController ctrlr = new FailureController(getContext());
        EmployeeLog empLog = this.getCurrentEmployeeLog();
        boolean shouldLogMessage = false;

        // check if there are any failure messages to display
        if (empLog != null && ctrlr.AnyFailuresToReport(empLog))
        {
            msg = ctrlr.MessageForMostRecentFailure(empLog);
            shouldLogMessage = true;
        }
        else
        {
            // no application failures to display at this time
            String eobrId;
            switch (EobrReader.getInstance().getCurrentConnectionState())
            {
                case OFFLINE:
                case SHUTDOWN:
                    eobrId = EobrReader.getInstance().getEobrIdentifier();
                    if (eobrId == null || eobrId.length() == 0) {
                        msg = loggingDeviceDisconnectedMessage;
                    } else {
                        msg = String.format(loggingDeviceDisconnectedMessage2, eobrId);
                    }
                    break;
                case ONLINE:
                    eobrId = EobrReader.getInstance().getEobrIdentifier();
                    msg = String.format(loggingDeviceOnlineMessage, eobrId);
                    break;
                case READINGHISTORICAL:
                    eobrId = EobrReader.getInstance().getEobrIdentifier();
                    msg = String.format(readingHistoryMessage, eobrId);
                    break;
                case DEVICEFAILURE:
                    shouldLogMessage = true;
                    msg = loggingDeviceFailureMessage;
                    break;
                case FIRMWAREUPDATE:
                    eobrId = EobrReader.getInstance().getEobrIdentifier();
                    msg = String.format(loggingDeviceFirmwareUpdateMessage, eobrId);
                    break;
            }

            if (this.getAnyLocationsMissingOnCurrentLog())
                msg = msg + this.getContext().getString(R.string.msgsomelocationsaremissing);
        }

        if(shouldLogMessage && msg != null && msg.length() > 0){
            // log the message
            ErrorLogHelper.RecordMessage(this.getContext(), String.format("LogEntryController status: %s", msg));
        }

        return msg;
    }

    public EmployeeLogEldEvent getLogEventForEdit()
    {
        if (GlobalState.getInstance().getLogEventForEdit() != null
                && GlobalState.getInstance().getLogEventForEdit().getEventType() == Enums.EmployeeLogEldEventType.DutyStatusChange) {
            return GlobalState.getInstance().getLogEventForEdit();
        } else {
            return EmployeeLogUtilities.GetLastEventInLog(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus, getCurrentEmployeeLog());
        }
    }

    public void setLogEventForEdit(EmployeeLogEldEvent logEvent)
    {
        GlobalState.getInstance().setLogEventForEdit(logEvent);
    }

    /// <summary>
    /// Perform the edit of a specific log location.  This is used from the
    /// 'Edit Log Locations' screen.
    /// </summary>
    /// <param name="logEvent"></param>
    public void PerformEditLogLocation(EmployeeLogEldEvent logEvent)
    {
        this.setLogEventForEdit(logEvent);
    }

    /// <summary>
    /// Perform the edit of a specific log remark.  This is used from the
    /// 'View Log Remarks' screen.
    /// </summary>
    /// <param name="logEvent"></param>
    public void PerformEditLogRemark(EmployeeLogEldEvent logEvent)
    {
        this.setLogEventForEdit(logEvent);
    }

    /// <summary>
    /// Answer if there are any location codes available.
    /// This will be used to enable UI functions to enter codes.
    /// </summary>
    public boolean getAreLocationCodesAvailable()
    {
        LocationCodeController ctrlr = new LocationCodeController(getContext());
        return ctrlr.getAreLocationCodesAvailable();
    }

    /// <summary>
    /// Answer the full location string given the code for that location
    /// </summary>
    /// <param name="locationCode"></param>
    /// <returns></returns>
    public String getLocationForCode(String locationCode)
    {
        LocationCodeController ctrlr = new LocationCodeController(this.getContext());
        LocationCodeDictionary dict = ctrlr.getLocationCodes();
        String location = dict.LocationFromCode(locationCode);
        return location;
    }

    /// <summary>
    /// Update the location in the most recent event
    /// </summary>
    /// <param name="location">location value to assign to most recent event</param>
    public void UpdateCurrentEventLocation(String location, boolean manualLocationEntry) {
        // fetch the most recent log event on the current log being built
        EmployeeLog empLog = this.getCurrentEmployeeLog();
        EmployeeLogEldEvent evt = this.getLogEventForEdit();

        if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
            if(manualLocationEntry) {
                evt.getLocation().setName(location);
            }
        } else {
            evt.getLocation().setName(location);
        }

        if (!EobrReader.getIsEobrDeviceAvailable())
            evt.getLocation().setGpsInfo(null);


        // persist the changes to the current day's log, just in case they have not been committed
        IAPIController empLogController = MandateObjectFactory.getInstance(this.getContext(), GlobalState.getInstance().getFeatureService()).getCurrentEventController();
        empLogController.SaveLocalEmployeeLog(empLog);
        this.setCurrentEmployeeLog(empLog);

        // by confirming a location, the driver has ending the driving period
        this.setIsExtendDrivingSegmentEnabled(false);
    }

    /// <summary>
    /// Update the lat/lon status codes and the location on the automatic on-duty event after a driving event.
    /// </summary>
    public void MandateUpdateLatLongStatusCode(String latLonStatus) {
        MandateUpdateLatLongStatusCode(latLonStatus, null);
    }

    public void MandateUpdateLatLongStatusCode(String latLonStatus, String location)
    {
        // fetch the most recent log event on the current log being built
        EmployeeLog empLog = this.getCurrentEmployeeLog();
        EmployeeLogEldEvent evt = this.getLogEventForEdit();

        evt.setLatitudeStatusCode(latLonStatus);
        evt.setLongitudeStatusCode(latLonStatus);

        if (location != null && location.length() > 0) {
            evt.getLocation().setName(location);
        }

        // if latlonstatus is 'M' (or 'X' or 'E'), we don't need to record GPS or geolocation
        if (!TextUtils.isEmpty(latLonStatus)) {
            switch (latLonStatus.toUpperCase()) {
                case "M":   // manual location
                case "X":   // malfunction warning
                case "E":   // malfunction status
                    evt.setLocation(new Location());
                    evt.setLatitude(null);
                    evt.setLongitude(null);
                    evt.setGeolocation(null);

                    if (latLonStatus.toUpperCase() == "M" && !TextUtils.isEmpty(location)) {
                        evt.setDriversLocationDescription(location);
                    }
                    break;
            }
        }

        // persist the changes to the current day's log, just in case they have not been committed
        IAPIController empLogController = MandateObjectFactory.getInstance(this.getContext(),GlobalState.getInstance().getFeatureService()).getCurrentEventController();
        empLogController.SaveLocalEmployeeLog(empLog);
        this.setCurrentEmployeeLog(empLog);

        // by confirming a location, the driver has ending the driving period
        this.setIsExtendDrivingSegmentEnabled(false);
    }

    private void UpdateEventEndOdometer(EmployeeLog log, EmployeeLogEldEvent logEvent, float odometer) {
        if (log == null || logEvent == null || logEvent.getLocation() == null)
            return;

        if (logEvent.getLocation().getEndOdometerReading() < odometer && logEvent.getEobrSerialNumber()==getCurrentEobrSerialNumber()) {
            logEvent.getLocation().setEndOdometerReading(odometer);

            IEmployeeLogFacade empLogFacade = new EmployeeLogFacade(this.getContext(), this.getCurrentUser());
            empLogFacade.UpdateEndOdometer(logEvent);

            updateSpecialDrivingEndData();
        }
    }

    /**
     * Set the current event to require a manual location
     */
    public void UpdateCurrentEventToRequireManualLocation()
    {
        EmployeeLogEldEvent evt = this.getLogEventForEdit();
        if (evt != null)
        {
            evt.setRequiresManualLocation(true);
        }
    }

    /// <summary>
    /// Update the remarks in the most recent event
    /// </summary>
    /// <param name="remarks">remarks value to assign to most recent event</param>
    public void UpdateCurrentEventRemarks(String remarks)
    {
        // fetch the most recent log event on the current log being built
        EmployeeLog empLog = this.getCurrentEmployeeLog();
        EmployeeLogEldEvent evt = this.getLogEventForEdit();

        // put the new remarks in the event
        evt.setLogRemark(remarks);
        evt.setLogRemarkDate(this.getCurrentClockHomeTerminalTime());

        // persist the changes to the current day's log, just in case they have not been committed
        IAPIController empLogController = MandateObjectFactory.getInstance(this.getContext(),GlobalState.getInstance().getFeatureService()).getCurrentEventController();
        empLogController.SaveLocalEmployeeLog(empLog);
    }

    /// <summary>
    /// Extend the last driving segment by removing the on-duty period that
    /// is currently on the log
    /// </summary>
    public void ManuallyExtendDrivingSegment()
    {
        // reset this flag because the user has chosen to extend the driving period
        this.setIsExtendDrivingSegmentEnabled(false);
        this.setHasExtendedDrivingSegment(true);

        // temporarily disable the motion sensor from continually reporting
        // a confirmed stop
        GlobalState.getInstance().getVehicleMotionDetector(getContext()).DisableStopPeriodConfirmation();

        // fetch the last event on the log, if it's an on-duty event, keep going
        // the on-duty event gets in there once the vehicle is a confirmed stop
        EmployeeLog empLog = this.getCurrentEmployeeLog();
        EmployeeLogEldEvent lastevent = EmployeeLogUtilities.GetLastEventInLog(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus,empLog);
        if (lastevent.getDutyStatusEnum().getValue() == DutyStatusEnum.ONDUTY)
        {
            // the last event on the log is on-duty
            // now, look at the event just before this one
            EmployeeLogEldEvent priorevent = EmployeeLogUtilities.GetEventPriorToTime(empLog, lastevent.getStartTime());
            if (priorevent != null && priorevent.getDutyStatusEnum().getValue() == DutyStatusEnum.DRIVING)
            {
                // the normal case is when the event prior to the on-duty is a driving
                // when this happens, simply removing the on-duty event will keep the
                // driving segment enabled
                EmployeeLogUtilities.RemoveEventFromLog(empLog, lastevent.getStartTime());
            }
            else
            {
                // the prior event is not a driving event
                // this will happen if the driving event started during the same
                // time period as when the vehicle stopped.
                // change the duty status of the last event to driving in this case
                lastevent.setDutyStatusEnum(new DutyStatusEnum(DutyStatusEnum.DRIVING));
            }

            // persist the changes to the current day's log, just in case they have not been committed
            IAPIController empLogController = MandateObjectFactory.getInstance(this.getContext(),GlobalState.getInstance().getFeatureService()).getCurrentEventController();
            empLogController.SaveLocalEmployeeLog(empLog);
            this.setCurrentEmployeeLog(empLog);
        }
    }

    /// <summary>
    /// Make the appropriate changes to the current log being processed
    /// because employee rule changes may have been downloaded.
    /// 1. If the user's timezone changed, then update all log events
    /// </summary>
    public void UpdateCurrentLogForEmployeeRuleChanges()
    {
        EmployeeLog log = GlobalState.getInstance().getCurrentEmployeeLog();

        if (log != null)
        {
            // there is a log already started
            EmployeeLog empLog = this.getCurrentEmployeeLog();

            // timezone on the log may be different than the user
            if (!empLog.getTimezone().equals(this.getCurrentUser().getHomeTerminalTimeZone()))
            {
                // adjust all of the events on the log to reflect the new timezone
                EmployeeLogUtilities.AdjustLogEventsForTimezoneChange(this.getContext(), this.getCurrentUser(), empLog);

                // Change the home terminal date formatters to the new user time zone
                DateUtility.setHomeTerminalTimeDateFormatTimeZone(this.getCurrentUser().getHomeTerminalTimeZone().toTimeZone());

                // persist changes to save the log
                IAPIController ctrlr = MandateObjectFactory.getInstance(this.getContext(),GlobalState.getInstance().getFeatureService()).getCurrentEventController();
                ctrlr.SaveLocalEmployeeLog(empLog);
                this.setCurrentEmployeeLog(empLog);
            }
        }
    }

    public Boolean getAnyLocationsMissingOnCurrentLog()
    {
        boolean answer = false;
        EmployeeLog empLog = this.getCurrentDriversLog();
        String dailyLogStartTime = GlobalState.getInstance().getCompanyConfigSettings(this.getContext()).getDailyLogStartTime();
        // check if any locations are missing on logs that are active, duty status, and not initial
        for (EmployeeLogEldEvent logEvt : empLog.getEldEventList().getActiveEldEventList(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus))
        {
            if(isLocationRequired(logEvt, dailyLogStartTime) && logEvt.getLocation().IsEmpty())
            {
                answer = true;
                break;
            }
        }
        return answer;
    }

    public Boolean isLocationRequired(EmployeeLogEldEvent log, String dailyLogStartTime) {
        if(DateUtility.getHomeTerminalTime24HourFormat().format(log.getStartTime()).compareTo(dailyLogStartTime) == 0) {
            return false;
        } else {
            return true;
        }
    }

    /// <summary>
    /// Adds an Off-Duty event to the current log at the given time offset.
    /// To calculate the actual time of the event, the offset
    /// is added to the current clock time.
    /// This is used when the user logs out.
    /// </summary>
    /// <param name="offDutyOffset"></param>
    /// <param name="location"></param>
    public void AddLogoutEventToCurrentLog(int offDutyOffset, Location location, int dutyStatusEnum, String annotation)
    {
        // convert the UTC time into the timezome of the driver's home terminal
        Date offDutyTime = this.getCurrentClockHomeTerminalTime();

        // calculate the actual event time by adding the offset
        if (offDutyOffset != 0)
        {
            offDutyTime = DateUtility.AddMinutes(offDutyTime, offDutyOffset);
        }

        // add an off-duty event to the current log at the specified time
        if(dutyStatusEnum < 0)
            dutyStatusEnum = DutyStatusEnum.OFFDUTY;

        String productionId = null, authorityId = null;
        if (GlobalState.getInstance().getCompanyConfigSettings(getContext()).getIsMotionPictureEnabled()) {
            productionId=GlobalState.getInstance().get_currentMotionPictureProductionId();
            authorityId=GlobalState.getInstance().get_currentMotionPictureAuthorityId();

        }

        this.PerformManualStatusChange(offDutyTime, new DutyStatusEnum(dutyStatusEnum), location.getName(), annotation, null, null, productionId, authorityId, null);

        // set the ending timestamp of the mobile log which is the timestamp of the last event
        EmployeeLog empLog = this.getCurrentEmployeeLog();
        EmployeeLogEldEvent evt = EmployeeLogUtilities.GetLastEventInLog(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus,empLog);
        empLog.setMobileEndTimestamp(evt.getStartTime());

        // persist all changes to the log
        IAPIController cntrlr = MandateObjectFactory.getInstance(this.getContext(),GlobalState.getInstance().getFeatureService()).getCurrentEventController();
        cntrlr.SaveLocalEmployeeLog(empLog);
    }

    /// <summary>
    /// Switch to a new logged in user.
    /// This involves changing the current log being processed.
    /// </summary>
    public void SwitchUserContext()
    {
        // convert the UTC time into the timezome of the driver's home terminal
        Date now = this.getCurrentClockHomeTerminalTime();

        // change the webservice user
        this.UpdateLogCheckerWebServiceCredentials(this.getCurrentUser().getCredentials());

        // fetch the local log for today
        IAPIController cntlr = MandateObjectFactory.getInstance(this.getContext(),GlobalState.getInstance().getFeatureService()).getCurrentEventController();
        EmployeeLog empLog = cntlr.GetLocalEmployeeLog(this.getCurrentUser(), now);
        if (empLog == null)
        {
            // the log is not there yet, try to get yesterday's log
            empLog = cntlr.GetLocalEmployeeLog(this.getCurrentUser(), DateUtility.AddDays(now, -1));
        }

        if (empLog == null)
        {
            // if there is no log found yet, then as a last resort, create a transition log
            // this probably should never happen
            empLog = cntlr.GetLocalEmployeeLogOrCreateTransition(this.getCurrentUser(), now);
        }

        // save the log as the current one
        this.setCurrentEmployeeLog(empLog);
    }

    /// <summary>
    /// Prepare to switch to a new designated driver.
    /// This involves changing the current drivers log being processed.
    /// </summary>
    public void SwitchDriverContext(User newDriver)
    {
        // convert the UTC time into the timezone of the driver's home terminal
        Date currentTimestamp = this.getCurrentClockHomeTerminalTime();
        User currentDriver = this.getCurrentDesignatedDriver();

        // fetch the current drivers local log for today
        IAPIController cntlr = MandateObjectFactory.getInstance(this.getContext(),GlobalState.getInstance().getFeatureService()).getCurrentEventController();
        EmployeeLog empLog = cntlr.GetLocalEmployeeLogOrCreateTransition(currentDriver, currentTimestamp);
        EmployeeLogEldEvent evt = EmployeeLogUtilities.GetLastEventInLog(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus,empLog);

        // is there a driver change taking place?
        if (newDriver.getCredentials().getEmployeeId().compareTo(currentDriver.getCredentials().getEmployeeId()) != 0)
        {
            // there is a new driver


            if (GlobalState.getInstance().getCompanyConfigSettings(this.getContext()).getIsGeotabEnabled()) {

                String serialNumber = GlobalState.getInstance().getCurrentEobrSerialNumber();

                if (serialNumber != null && serialNumber.length() > 0) {
                    // Alert the Geotab Service that a new driver is using the device
                    GeotabController geotabController = new GeotabController(getContext());
                    geotabController.SubmitGeotabDriverChangeTaskRun(GlobalState.getInstance().getCurrentEobrSerialNumber(), newDriver.getCredentials().getEmployeeCode());
                }
            }


            //check to see if current driver is under a special duty status, and end it.
            if(currentDriver.getUserState().getIsInPersonalConveyanceDutyStatus() || GlobalState.getInstance().getIsInPersonalConveyanceDrivingSegment()){
                GlobalState.getInstance().setIsEndingActivePCYMWT_Status(true);
                empLog = this.PerformStatusChange(currentDriver, empLog, currentTimestamp, new DutyStatusEnum(DutyStatusEnum.OFFDUTY), null);
            }
            if(currentDriver.getUserState().getIsInYardMoveDutyStatus()){
                GlobalState.getInstance().setIsEndingActivePCYMWT_Status(true);
                empLog = this.PerformStatusChange(currentDriver, empLog, currentTimestamp, new DutyStatusEnum(DutyStatusEnum.ONDUTY), null);
            }

            // check to see if the current drivers log is already driving
            if(evt.getDutyStatusEnum().getValue() == DutyStatusEnum.DRIVING)
            {
                // Need to end the driving period for the current drivers log
                // before performing the driver switch.
                // Make the current driver go OnDuty right now.
                // Assume that the vehicle is in motion right now because the
                // current driver's log is in "driving" status.
                empLog = this.PerformStatusChange(currentDriver, empLog, currentTimestamp, new DutyStatusEnum(DutyStatusEnum.ONDUTY), null);

            }

            // save the settings for the new driver
            this.setCurrentDesignatedDriver(newDriver);

            // Set threshold values in TAB to new driver's values

            int driverIdCrc = this.SetThresholdValues(newDriver,true,false);

            this.getCurrentDesignatedDriver().getCredentials().setDriverIdCrc(driverIdCrc);

            // TCH - 9/11/2012 - need to set the CurrentDriversLog state data to the new driver's log, not the
            // current driver that we are switching from.  Get the new drivers log and set in state data
            EmployeeLog newEmpLog = cntlr.GetLocalEmployeeLog(newDriver, currentTimestamp);
            if (newEmpLog != null)
                this.setCurrentDriversLog(newEmpLog);

            // handle the new designated driver for PC mode
            // PC mode should be ended on the switch to a new driver
            ISpecialDrivingController specialDrivingController = SpecialDrivingFactory.getController(currentDriver.getUserState());
            if(specialDrivingController != null) {
                Date endTime = DateUtility.CurrentHomeTerminalTime(currentDriver);
                specialDrivingController.EndSpecialDrivingStatus(endTime, null, empLog);
            }

            verifySpecialStatusesForTeamDriver(GlobalState.getInstance().getCurrentDesignatedDriver());
        }
        else
        {
            verifySpecialStatusesForTeamDriver(currentDriver);

            // If switching employees but not drivers, handle updating HOSAudit when Driving
            if (evt.getDutyStatusEnum().getValue() == DutyStatusEnum.DRIVING)
            {
                HosAuditController hosAuditController = new HosAuditController(this.getContext());
                hosAuditController.MarkBeginningOfDrivingPeriod(evt.getStartTime());
                hosAuditController.MarkBeginningOfRestBreakPeriod(evt.getStartTime());
            }
        }

    }

    /// <summary>
    /// Determine the rulesets for US/Canadian border crossings.
    /// The current ruleset is whatever the user is currently following.
    /// The new ruleset will be the "opposite" one that needs to be followed.
    /// For example, if the current rule is a US Federal, then the new one
    /// will be  Canadian one.
    /// </summary>
    /// <param name="currentRuleset"></param>
    /// <param name="newRuleset"></param>
    public RuleSetTypeEnum GetBorderCrossingRuleset(RuleSetTypeEnum currentRuleset)
    {
        RuleSetTypeEnum newRuleset = new RuleSetTypeEnum(RuleSetTypeEnum.NULL);

        switch (currentRuleset.getValue())
        {
            case RuleSetTypeEnum.ALASKA_7DAY:
            case RuleSetTypeEnum.ALASKA_8DAY:
            case RuleSetTypeEnum.US60HOUR:
            case RuleSetTypeEnum.US70HOUR:
            case RuleSetTypeEnum.USMOTIONPICTURE_7DAY:
            case RuleSetTypeEnum.USMOTIONPICTURE_8DAY:
                // currently US Federal, switch to Canada
                newRuleset = this.getCurrentUser().getInternationalCDRuleset();
                break;

            case RuleSetTypeEnum.CANADIAN_CYCLE1:
            case RuleSetTypeEnum.CANADIAN_CYCLE2:
                // currently Canadian, switch to US Federal
                newRuleset = this.getCurrentUser().getInternationalUSRuleset();
                break;
        }
        return newRuleset;
    }

    /// <summary>
    /// Publish the Duty Status changes to the registered delegates in the UI
    /// </summary>
    /// <param name="logDate"></param>
    /// <param name="logEvent"></param>
    private void PublishDutyStatusChange( Date logDate, EmployeeLogEldEvent logEvent )
    {
        // is there an event handler
        if (this._dutyStatusChangeEventHandler != null)
        {
            // verify that the driver is the current active user
            // only publish the status change when the user is the driver
            if (this.IsCurrentUserTheDriver())
            {
                // create the event args
                DutyStatusChangeEventArgs eventArgs = new DutyStatusChangeEventArgs();

                eventArgs.setLogDate(logDate);
                if (logEvent != null)
                {
                    eventArgs.setTimestamp(logEvent.getStartTime());
                    eventArgs.setDutyStatus(logEvent.getDutyStatusEnum());
                    eventArgs.setLocation(logEvent.getLocation());
                    eventArgs.setRuleset(logEvent.getRulesetType());
                }

                // Invokes the delegates.
                this._dutyStatusChangeEventHandler.onDutyStatusChanged(eventArgs);
            }
        }
    }

    private void PublishDrivingDutyStatusChange(Date logDate, Date startTime, Location location, RuleSetTypeEnum ruleSetTypeEnum)
    {
        // is there an event handler
        if (this._dutyStatusChangeEventHandler != null)
        {
            // verify that the driver is the current active user
            // only publish the status change when the user is the driver
            if (this.IsCurrentUserTheDriver())
            {
                // create the event args
                DutyStatusChangeEventArgs eventArgs = new DutyStatusChangeEventArgs();

                eventArgs.setLogDate(logDate);

                eventArgs.setTimestamp(startTime);
                eventArgs.setDutyStatus(new DutyStatusEnum(DutyStatusEnum.DRIVING));
                eventArgs.setLocation(location);
                eventArgs.setRuleset(ruleSetTypeEnum);

                // Invokes the delegates.
                this._dutyStatusChangeEventHandler.onDutyStatusChanged(eventArgs);
                EventBus.getDefault().post(eventArgs.getDutyStatus());
            }
        }
    }

    /// <summary>
    /// As long as the current user is setup to drive internationally, then
    /// perform a border crossing at a specific time.
    /// The current rule being followed, will be switched to the other
    /// country's rule.
    /// </summary>
    /// <param name="timeOfNewEvent">time of the border crossing</param>
    public void PerformBorderCrossingChange(Date timeOfNewEvent)
    {
        if (this.getCurrentUser().IsInternationalDrivingAllowed())
        {
            // verify that the vehicle did not just recently come to a stop
            EmployeeLogEldEvent lastEvent = EmployeeLogUtilities.GetLastEventInLog(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus,this.getCurrentEmployeeLog());

            // change the ruleset of the user to be the 'other' rule
            RuleSetTypeEnum newRule = this.GetBorderCrossingRuleset(lastEvent.getRulesetType());
            if (newRule.getValue() == RuleSetTypeEnum.NULL) return;

            this.getCurrentUser().setRulesetTypeEnum(newRule);

            this.PerformManualStatusChange(timeOfNewEvent, lastEvent.getDutyStatusEnum(), null, "");
        }
    }

    /// <summary>
    /// Publish the Duty Status changes to the registered delegates in the UI
    /// </summary>
    /// <param name="logDate"></param>
    /// <param name="logEvent"></param>
    public void PublishDutyStatusChange()
    {
        // is there a registered event handler
        if (this._dutyStatusChangeEventHandler != null)
        {
            // verify that the driver is the current active user
            // only publish the status change when the user is the driver
            if (this.IsCurrentUserTheDriver())
            {
                // create the event args
                DutyStatusChangeEventArgs eventArgs = new DutyStatusChangeEventArgs();

                eventArgs.setLogDate(null);

                // Invokes the delegates.
                this._dutyStatusChangeEventHandler.onDutyStatusChanged(eventArgs);
            }
        }
    }

    public void SuspendUpdates() {
        try
        {
            if (EobrReader.getIsEobrDeviceAvailable())
            {
                EobrReader.getInstance().SuspendReading();
            }
        }
        catch (Exception excp)
        {
            this.HandleException(excp);
        }
    }

    /// <summary>
    /// Resume processing the EOBR
    /// </summary>
    public void ResumeUpdates()
    {
        try
        {
            if (EobrReader.getIsEobrDeviceAvailable())
            {
                EobrReader.getInstance().ResumeReading();
            }
        }
        catch (Exception excp)
        {
            this.HandleException(excp);
        }
    }

    /**
     * Suspends periodic reading and forces an immediate read from the EOBR.
     * This will only be done for Gen II EOBR's.
     * <br /><br />
     * This is a blocking call that will won't finish until all outstanding EOBR records have been read and processed.
     */
    public void ForceImmediateRead()
    {
        try
        {
            if (EobrReader.getIsEobrDeviceAvailable())
            {
                final Object lock = new Object();
                synchronized (lock)
                {
                    EobrReader.getInstance().ForceImmediateRead(lock);
                    lock.wait(10000);
                }
            }
        }
        catch (Exception excp)
        {
            this.HandleException(excp);
        }
    }

    /// <summary>
    /// Setup the last valid eobr odometer value and date of last valid eobr odometer
    /// in the eobr reader used to validate odometers being read from the eobr.
    /// </summary>
    /// <returns></returns>\
    public void SetLastEobrOdometer()
    {
        float lastEobrOdometer = -1;
        Date lastEobrOdometerDateTime = null;
        Bundle bundle = this.GetLastEobrOdometer();

        lastEobrOdometer = bundle.getFloat(this.getContext().getString(R.string.lasteobrodometer));
        if (bundle.getString(this.getContext().getString(R.string.lasteobrodometertimestamp)) != null)
        {
            lastEobrOdometerDateTime = null;
            try {
                lastEobrOdometerDateTime = DateUtility.getHomeTerminalDateTimeFormat24Hour().parse(bundle.getString(this.getContext().getString(R.string.lasteobrodometertimestamp)));
            } catch (ParseException ex) {
                Log.e("UnhandledCatch", ex.getMessage() + ": " + Log.getStackTraceString(ex));
            }
        }

        EobrReader.getInstance().setLastEobrOdometer(lastEobrOdometer);
        EobrReader.getInstance().setLastEobrOdometerUTCTime(lastEobrOdometerDateTime);
    }

    /// <summary>
    /// Get last eobr odometer.  Get last value from engine record table
    /// and last calibrated value
    /// </summary>
    /// <returns></returns>
    public Bundle GetLastEobrOdometer()
    {
        float lastEobrOdometer = -1.0F;
        Date lastEobrOdometerDateTime = null;
        EobrConfiguration config = null;
        EngineRecord engineRec = null;

        Bundle bundleEobrOdometer = new Bundle();

        EobrConfigController configController = new EobrConfigController(this.getContext());

        // check if online to eobr - if not online use serial num of last connected eobr if exists
        // if online or no previous serial num available, attempt to read the serial number from the eobr
        EobrReader eobr = EobrReader.getInstance();
        String eobrSerialNum = null;
        if (eobr.getCurrentConnectionState() != ConnectionState.ONLINE)
            eobrSerialNum = eobr.getEobrSerialNumber();

        if (eobrSerialNum == null)
            // get config by attempting to read serial number from eobr
            config = configController.GetConfigFromDB();
        else
            // get config for previous eobr online to
            config = configController.GetConfigFromDB(eobrSerialNum);

        EngineRecordController erController = new EngineRecordController(getContext(), GlobalState.getInstance().getAppSettings(getContext()));
        engineRec = erController.GetLastEngineRecord();

        // if odometer calibration has occurred (config != null) and there are engine
        // records in the database, seed the last odometer value and date based on
        // calibration values compared to last engine record values
        if (config != null && config.getEobrOdometer() > 0.0F && engineRec != null && engineRec.getOdometer() > 0.0F)
        {
            // Note:  this could be a negative value if last engine record is prior
            // to last calibration date - this would require the eobr to have been
            // disconnected
            long timeSinceLastCalibration = engineRec.getEobrTimestamp().getTime() - config.getOdometerCalibrationDate().getTime();
            double daysSinceLastCalibration = DateUtility.ConvertMillisecondsToHours(timeSinceLastCalibration) / HOURS_PER_DAY;


            float startRange = 0.0F;
            float endRange = 0.0F;

            // Note: 2040 = 24 hours @ 85 MPH
            if (daysSinceLastCalibration < 0)
            {
                startRange = config.getEobrOdometer() + (float)((daysSinceLastCalibration) * 2040);
                endRange = config.getEobrOdometer();
            }
            else
            {
                startRange = config.getEobrOdometer();
                endRange = config.getEobrOdometer() + (float)((daysSinceLastCalibration) * 2040);
            }

            // set minimimum range of 2 miles
            if (endRange - startRange < 2.0F)
                endRange = startRange + 2.0F;

            if (engineRec.getOdometer() >= startRange && engineRec.getOdometer() <= endRange)
            {
                lastEobrOdometer = engineRec.getOdometer();
                lastEobrOdometerDateTime = engineRec.getEobrTimestamp();
            }
            else
            {
                lastEobrOdometer = config.getEobrOdometer();
                lastEobrOdometerDateTime = config.getOdometerCalibrationDate();
            }
        }
        // else if config exists, but no engine record (initial startup after activation),
        // seed last odometer value and date from config values
        else if (config != null && config.getEobrOdometer() > 0.0F)
        {
            lastEobrOdometer = config.getEobrOdometer();
            lastEobrOdometerDateTime = config.getOdometerCalibrationDate();
        }

        bundleEobrOdometer.putFloat(this.getContext().getString(R.string.lasteobrodometer), lastEobrOdometer);

        if (lastEobrOdometerDateTime != null)
            bundleEobrOdometer.putString(this.getContext().getString(R.string.lasteobrodometertimestamp), DateUtility.getHomeTerminalDateTimeFormat24Hour().format(lastEobrOdometerDateTime));
        else
            bundleEobrOdometer.putString(this.getContext().getString(R.string.lasteobrodometertimestamp), null);

        return bundleEobrOdometer;
    }

    public OffDuty getOffDutyInfo(){
        HosAuditController auditCtrlr = new HosAuditController(getContext());
        return auditCtrlr.getOffDutyInfo();
    }

    /// <summary>
    /// Answer if any messages that need confirmation from the user.
    /// </summary>
    /// <returns></returns>
    public String getAlertMessage() {
        String msg = "";

        // are there any failures that require an alert
        FailureController failureCtrlr = new FailureController(getContext());
        if (failureCtrlr.AnyFailuresRequireConfirmation())
        {
            msg = failureCtrlr.MessageForMostRecentFailure(this.getCurrentEmployeeLog());
        }

        //if there's a clock update pending then hold off
        //on this until the next timer pop
        if(!EobrReader.getInstance().IsClockUpdatePending())
        {
            // are there any audit messages that require an alert
            //long startTime = TimeKeeper.getInstance().now().getTime();

            HosAuditController auditCtrlr = new HosAuditController(getContext());

            auditCtrlr.UpdateForCurrentLogEvent();
            HosAlertController alertController = new HosAlertController(getContext());

            //long endTime = TimeKeeper.getInstance().now().getTime();
            //Log.d("CalcEngine", String.format("LogEntryController.getAlertMessage - CalcEngine took %s (ms)" , endTime - startTime));

            if ((msg.compareTo("") == 0) && alertController.AnyMessageRequiresConfirmation())
            {
                msg = alertController.AlertMessage();

                if(msg != null && msg.length() > 0)
                {
                    // Show the alert message
                    NotificationUtilities.AddNotification(this.getContext(),
                            GlobalState.getInstance().getNotificationHoursAvailableClass(),
                            NotificationUtilities.HOURSWARNING_ID, R.drawable.stat_notify_alarm,
                            this.getContext().getString(R.string.app_name_api), msg, Notification.FLAG_AUTO_CANCEL,
                            Notification.DEFAULT_ALL, R.layout.alert_notification_layout);
                }
            }
        }

        if(msg != null && msg.length() > 0){
            // log the message
            ErrorLogHelper.RecordMessage(this.getContext(), String.format("LogEntryController alert: %s", msg));
        }

        return msg;

    }

    /**
     * Remove all log events from the current employee's log that may occur after the
     * synchronization time.
     * @param ctx
     * @param syncTimestamp
     */
    public void RemoveLogEventsFromCurrentLogAfter(Context ctx, Date syncTimestamp ){
        EmployeeLog empLog = this.getCurrentEmployeeLog();

        EmployeeLogUtilities.RemoveAllEventsAfter(empLog, syncTimestamp);

        // persist all changes to the log
        IAPIController cntrlr = MandateObjectFactory.getInstance(this.getContext(),GlobalState.getInstance().getFeatureService()).getCurrentEventController();
        cntrlr.SaveLocalEmployeeLog(empLog);
    }

    /*
     * Default SetThresholdValues method - requires eobr connection state to be online
     */
    public int SetThresholdValues(User driver) {
        return this.SetThresholdValues(driver, true, true);
    }

    /*
     * Default SetThresholdValues method - check device availability
     */
    public int SetThresholdValues(User driver, boolean checkDeviceAvailibility) {
        return this.SetThresholdValues(driver, checkDeviceAvailibility, true);
    }

    /// <summary>
    /// Set the threshold values for the EOBR.
    /// If we do not have the EobrConfiguration from the database,
    /// then use the CompanyConfigurationSettings to get the company default threshold values
    /// </summary>
    /// <returns>DriverIdCrc</returns>
    public int SetThresholdValues(User driver, boolean checkDeviceAvailibility, boolean setCompanyThresholds){
        int driverIdCrc = -1;

        if(setCompanyThresholds)
            new SystemStartupController(getContext()).SetUnassignedDriverThreshold();

        try {
            // Attempt to get the EobrConfiguration from the database
            EobrConfiguration config = null;
            if(EobrReader.getInstance() != null)
                config = (new EobrConfigController(getContext())).GetConfigFromDB(EobrReader.getInstance().getEobrSerialNumber());

            // Get the company config for default Start/Stop values
            CompanyConfigSettings companyConfigSettings = GlobalState.getInstance().getCompanyConfigSettings(getContext());

            // Handle setting Start, Stop, and EmpCode values when driver is null (pre-login)
            String driverEmpCode;
            float driverStartMiles;
            int driverStopMinutes;
            int mandateDrivingStopMinutes;
            int driveStartSpeed;
            if(driver != null)
            {
                driverEmpCode = driver.getCredentials().getEmployeeCode();
                driverStartMiles = (float)driver.getDrivingStartDistanceMiles();
                driverStopMinutes = driver.getDrivingStopTimeMinutes();
                mandateDrivingStopMinutes = driver.getMandateDrivingStopTimeMinutes();
                driveStartSpeed = driver.getDriveStartSpeed();
            }
            else
            {
                driverEmpCode = "";

                if(companyConfigSettings != null)
                {
                    driverStartMiles = companyConfigSettings.getDriverStartDistance();
                    driverStopMinutes = companyConfigSettings.getDriverStopMinutes();
                    mandateDrivingStopMinutes = companyConfigSettings.getMandateDrivingStopTimeMinutes();
                    driveStartSpeed = companyConfigSettings.getDriveStartSpeed();

                }
                else
                {
                    driverStartMiles = 0.5F;
                    driverStopMinutes = 10;
                    mandateDrivingStopMinutes = 5;
                    driveStartSpeed = 5;
                }
            }

            if (config == null) {
                // If the EOBR config is not available, then try and use the company thresholds
                if (companyConfigSettings != null) {
                    // Set the threshold values with the company values
                    driverIdCrc = this.SetThresholdValues(this.getContext(), companyConfigSettings.getMaxAcceptableTach(), companyConfigSettings.getMaxAcceptableSpeed(), companyConfigSettings.getHardBrakeDecelerationSpeed(), driverStartMiles, driverStopMinutes, 10, driverEmpCode, checkDeviceAvailibility, mandateDrivingStopMinutes, driveStartSpeed, setCompanyThresholds);
                }
            } else {
                // Set the threshold values with the unit values
                driverIdCrc = this.SetThresholdValues(this.getContext(), config.getTachometerThreshold(), config.getSpeedometerThreshold(), config.getHardBrakeThreshold(), driverStartMiles, driverStopMinutes, 10, driverEmpCode, checkDeviceAvailibility, mandateDrivingStopMinutes, driveStartSpeed, setCompanyThresholds);
            }
        } catch (Exception ex) {
            Log.e("SetThresholdValues", ex.getMessage() + ": " + Log.getStackTraceString(ex));
        }

        return driverIdCrc;
    }

    /**
     * Set threshold values in Gen II TAB.
     * @param ctx
     * @param rpmThreshold
     * @param speedThreshold
     * @param hardBrakeThreshold
     * @param driveStartDistance
     * @param driveStopTime
     * @param eventBlanking
     * @param driverId
     * @return driverIdCrc value that is calculated
     */
    private int SetThresholdValues(Context ctx, int rpmThreshold, float speedThreshold, float hardBrakeThreshold, float driveStartDistance, int driveStopTime, int eventBlanking, String driverId, boolean checkDeviceAvailability, int mandateDrivingStopMinutes, float driveStartSpeed, boolean setCompanyThresholds)
    {
        int driverIdCrc = -1;

        // if eobr device is available, set threshold values
        if (!checkDeviceAvailability || EobrReader.getIsEobrDevicePhysicallyConnected())
        {
            if(setCompanyThresholds)
                new SystemStartupController(getContext()).SetUnassignedDriverThreshold();

            EobrConfigController configController = new EobrConfigController(this.getContext());

            Bundle bundle = configController.SetThresholdValues(ctx, rpmThreshold, speedThreshold, hardBrakeThreshold, driveStartDistance, driveStopTime, eventBlanking, driverId, mandateDrivingStopMinutes, driveStartSpeed);

            if (bundle != null && bundle.getInt(ctx.getString(R.string.rc)) == EobrReturnCode.S_SUCCESS)
            {
                driverIdCrc = bundle.getInt(ctx.getString(R.string.driveridcrc));
            }
        }

        return driverIdCrc;
    }

    /**
     * Performs a server update for the driver's hours available if necessary
     */
    private void updateDriverHoursAvailable()
    {
        new HosAuditController(getContext()).PerformPeriodicServerUpdate();
    }

    private void verifySpecialStatusesForTeamDriver(User currentDriver){
        //purge all special duty statuses for all non driver users.
        for(User user: GlobalState.getInstance().getLoggedInUserList()){
            if(user != currentDriver){
                user.getUserState().setIsInHyrailDutyStatus(false);
                user.getUserState().setIsInYardMoveDutyStatus(false);
                user.getUserState().setIsInPersonalConveyanceDutyStatus(false);
                user.getUserState().setIsInNonRegDrivingDutyStatus(false);
            }
        }
    }

    /**
     * A helper class that reverse geocodes a location in the background and publishes a status change after it's complete
     * in order to process the event with the location name
     */
    private class ReverseGeocodeAndPublishLocation implements IReverseGeocodeLocationListener
    {
        private final User currentDriver;
        private final EmployeeLog employeeLog;
        private final Date newEventTime;
        private final DutyStatusEnum newDutyStatus;
        private final Location newEventLocation;

        public ReverseGeocodeAndPublishLocation(final User currentDriver, final EmployeeLog employeeLog, final Date newEventTime, final DutyStatusEnum newDutyStatus, final Location newEventLocation)
        {
            this.currentDriver = currentDriver;
            this.employeeLog = employeeLog;
            this.newEventTime = newEventTime;
            this.newDutyStatus = newDutyStatus;
            this.newEventLocation = newEventLocation;
        }

        public void onResult(EmployeeLog log, GpsLocation location)
        {
            UpdateEventLocation(currentDriver, employeeLog, newEventTime, newDutyStatus, newEventLocation);
        }
    }

    /**
     * A helper class that reverse geocodes a location in the background and publishes a status change after it's complete
     * in order to process the event with the location name
     */
    private class ReverseGeocodeAndUpdateCurrentEventLocation implements IReverseGeocodeLocationListener
    {
        private final Location location;

        public ReverseGeocodeAndUpdateCurrentEventLocation(final Location location)
        {
            this.location = location;
        }

        public void onResult(EmployeeLog log, GpsLocation location)
        {
            if (location != null && location.getDecodedInfo() != null && location.getDecodedInfo().IsEmpty())
            {
                // Decoded successfully, but location is empty, so require manual entry
                UpdateCurrentEventToRequireManualLocation();
            }
            else
            {
                UpdateCurrentEventLocation(this.location.ToLocationString(), false);
            }
        }
    }

    private void EndSpecialDriving()
    {
        if(!GlobalState.getInstance().getIsInPersonalConveyanceDutyStatus()){
            LogPersonalConveyanceController logPersonalConveyanceController = new LogPersonalConveyanceController(this.getContext());
            logPersonalConveyanceController.EndSpecialDrivingStatus();
        }

        if (!GlobalState.getInstance().getIsInNonRegDrivingDutyStatus() && GlobalState.getInstance().getIsInNonRegDrivingSegment()) {
            LogNonRegulatedDrivingController logNonRegDrivingCtrlr = new LogNonRegulatedDrivingController(this.getContext());
            logNonRegDrivingCtrlr.EndSpecialDrivingStatus();
        }

        if (!GlobalState.getInstance().getIsInHyrailDutyStatus() && GlobalState.getInstance().getIsInHyrailDrivingSegment()) {
            LogHyrailController logHyrailController = new LogHyrailController(this.getContext());
            logHyrailController.EndSpecialDrivingStatus();
        }
        if (!GlobalState.getInstance().getIsInYardMoveDutyStatus() && GlobalState.getInstance().getIsInYardMoveDrivingSegment()) {
            GlobalState.getInstance().setIsInYardMoveDrivingSegment(false);
        }
    }

    private void updateSpecialDrivingEndData() {
        EmployeeLog empLog = GlobalState.getInstance().getCurrentEmployeeLog();
        Date endTime = DateUtility.CurrentHomeTerminalTime(getCurrentDesignatedDriver());

        if(GlobalState.getInstance().getIsInPersonalConveyanceDrivingSegment()){
            LogPersonalConveyanceController logPersonalConveyanceController = new LogPersonalConveyanceController(this.getContext());
            logPersonalConveyanceController.updateSpecialDrivingStatus(endTime, null, empLog);
        }

        if (GlobalState.getInstance().getIsInNonRegDrivingSegment()) {
            LogNonRegulatedDrivingController logNonRegulatedDrivingController = new LogNonRegulatedDrivingController(this.getContext());
            logNonRegulatedDrivingController.updateSpecialDrivingStatus(endTime, null, empLog);
        }

        if (GlobalState.getInstance().getIsInHyrailDrivingSegment()) {
            LogHyrailController logHyrailController = new LogHyrailController(this.getContext());
            logHyrailController.updateSpecialDrivingStatus(endTime, null, empLog);
        }
    }
}
