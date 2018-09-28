package com.jjkeller.kmbapi.controller;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.jjkeller.kmbapi.CodeBlocks;
import com.jjkeller.kmbapi.R;
import com.jjkeller.kmbapi.common.TimeKeeper;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.EOBR.EobrReader;
import com.jjkeller.kmbapi.controller.abstracts.AOBRDControllerBase;
import com.jjkeller.kmbapi.controller.abstracts.LogSpecialDrivingController;
import com.jjkeller.kmbapi.controller.dataaccess.EmployeeLogEldEventFacade;
import com.jjkeller.kmbapi.controller.dataaccess.EmployeeLogFacade;
import com.jjkeller.kmbapi.controller.dataaccess.EmployeeLogWithProvisionsFacade;
import com.jjkeller.kmbapi.controller.interfaces.IAPIController;
import com.jjkeller.kmbapi.controller.share.KmbApplicationException;
import com.jjkeller.kmbapi.controller.share.MandateObjectFactory;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.controller.utility.EmployeeLogUtilities;
import com.jjkeller.kmbapi.controller.utility.ErrorLogHelper;
import com.jjkeller.kmbapi.controller.utility.GenericEventComparer;
import com.jjkeller.kmbapi.controller.utility.RESTWebServiceHelper;
import com.jjkeller.kmbapi.controller.utility.UserUtility;
import com.jjkeller.kmbapi.eldmandate.EventDataDiagnosticsChecker;
import com.jjkeller.kmbapi.eldmandate.EventSequenceIdGenerator;
import com.jjkeller.kmbapi.employeelogeldevents.EmployeeLogEldEventCode;
import com.jjkeller.kmbapi.employeelogeldevents.Enums;
import com.jjkeller.kmbapi.employeelogeldevents.UnclaimedEventDTO;
import com.jjkeller.kmbapi.enums.DataDiagnosticEnum;
import com.jjkeller.kmbapi.enums.DutyStatusEnum;
import com.jjkeller.kmbapi.enums.EmployeeLogEldEnum;
import com.jjkeller.kmbapi.enums.EmployeeLogProvisionTypeEnum;
import com.jjkeller.kmbapi.enums.Malfunction;
import com.jjkeller.kmbapi.enums.RuleSetTypeEnum;
import com.jjkeller.kmbapi.enums.TimeZoneEnum;
import com.jjkeller.kmbapi.enums.UnidentifiedEldEventStatus;
import com.jjkeller.kmbapi.kmbeobr.DistanceAndHours;
import com.jjkeller.kmbapi.kmbeobr.Enums.EobrReturnCode;
import com.jjkeller.kmbapi.kmbeobr.EobrResponse;
import com.jjkeller.kmbapi.kmbeobr.EventRecord;
import com.jjkeller.kmbapi.kmbeobr.StatusBuffer;
import com.jjkeller.kmbapi.kmbeobr.StatusRecord;
import com.jjkeller.kmbapi.kmbeobr.TripReport;
import com.jjkeller.kmbapi.kmbeobr.UnidentifiedPairedEvents;
import com.jjkeller.kmbapi.kmbeobr.VehicleLocation;
import com.jjkeller.kmbapi.proxydata.DrivingEventReassignmentMapping;
import com.jjkeller.kmbapi.proxydata.EldEventAdapterList;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEventList;
import com.jjkeller.kmbapi.proxydata.EmployeeLogUnidentifiedELDEventStatus;
import com.jjkeller.kmbapi.proxydata.EmployeeLogWithProvisions;
import com.jjkeller.kmbapi.proxydata.EventTranslationBase;
import com.jjkeller.kmbapi.proxydata.GpsLocation;
import com.jjkeller.kmbapi.proxydata.Location;
import com.jjkeller.kmbapi.proxydata.compare.EmployeeLogEldEventDateComparator;
import com.jjkeller.kmbapi.realtime.malfunction.MalfunctionRealtimeManager;

import org.greenrobot.eventbus.EventBus;
import org.joda.time.DateTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TimeZone;

/**
 * APIEventControllerBase implementation for ELD Mandate EmployeeLogEldEvent objects
 */
public class EmployeeLogEldMandateController extends AOBRDControllerBase {

    SendSetDataTransferMechanismStatusRequestTask _sendSetDataTransferMechanismStatusRequestTask;
    SendGetDataTransferMechanismStatusRequestTask _sendGetDataTransferMechanismStatusRequestTask;

    public static final String LOG_TAG = "ELDMandateController";
    public static final int MAX_VALID_UNCERTAINTY_MILES = 5;
    private static final int SECOND_IN_HOUR = 3600;
    private int _dataTransferMechanismTimerValue;
    private Date _onDutyInsertedEventDateTime;
    private boolean addedProvisionEvent;
    private EventDataDiagnosticsChecker _dataDiagnosticChecker = new EventDataDiagnosticsChecker();

    public static final String POSITION_MALFUNCTION_WARNING_STATUS_CODE = "X";
    public static final String POSITION_MALFUNCTION_STATUS_CODE = "E";
    private EmployeeLogWithProvisionsFacade _provisionsFacade;
    private float secondaryEventEndOdometer = -1;

    public EmployeeLogEldMandateController(Context ctx) {
        this(ctx, EobrReader.getInstance());
    }

    public EmployeeLogEldMandateController(Context ctx, EobrReader eobrReader) {
        super(ctx, eobrReader);

        _dataTransferMechanismTimerValue = GlobalState.getInstance().getAppSettings(ctx).getDataTransferMechanismTimerValue();
    }

    public EmployeeLogEldMandateController(Context ctx, EmployeeLogEldEventFacade eventFacade, EventSequenceIdGenerator eventSequenceIdGenerator) {
        this(ctx, eventFacade, eventSequenceIdGenerator, EobrReader.getInstance());
    }

    public EmployeeLogEldMandateController(Context ctx, EmployeeLogEldEventFacade eventFacade, EventSequenceIdGenerator eventSequenceIdGenerator, EobrReader eobrReader) {
        super(ctx);
        this.eventFacade = eventFacade;
        this.eobrReader = eobrReader;
    }

    @Override
    public void CreateLoginLogoutEvent(Enums.CompositeEmployeeLogEldEventTypeEventCodeEnum eventType) throws Throwable {
        EmployeeLogEldEvent eventToSave;
        StatusRecord statusRecord = null;

        switch (eventType) {
            case LoginEvent:
                eventToSave = new EmployeeLogEldEvent(null, new EmployeeLogEldEventCode(EmployeeLogEldEventCode.Login), Enums.EmployeeLogEldEventType.LoginLogout);
                PopulateLoginLogoffEvent(eventToSave, EmployeeLogEldEventCode.Login);
                break;
            case LogoutEvent:
                eventToSave = new EmployeeLogEldEvent(null, new EmployeeLogEldEventCode(EmployeeLogEldEventCode.Logout), Enums.EmployeeLogEldEventType.LoginLogout);
                PopulateLoginLogoffEvent(eventToSave, EmployeeLogEldEventCode.Logout, globalState.getCurrentEmployeeLog());
                statusRecord = getLatestStatusRecord();
                break;
            default:
                throw new Exception("Invalid CompositeEmployeeLogEldEventTypeEventCodeEnum");
        }

        SetEngineHoursAndAccumulatedVehicleMiles(eventToSave, eventToSave.getEventDateTime(), statusRecord);

        SetReducedPrecisionGPSInfo(eventToSave, null, GlobalState.getInstance().getIsInPersonalConveyanceDutyStatus());

        SaveEvent(globalState.getCurrentEmployeeLog(), eventToSave, statusRecord, "EmployeeLogEldMandateController.CreateLoginLogoutEvent");
    }

    @Override
    public void UpdateLoginEvent() throws Throwable {
        //Update the duty status event created for login
        super.UpdateLoginEvent();
        EobrConfigController eobrController = new EobrConfigController(this.getContext(), eobrReader);
        StatusRecord statusRecord = getLatestStatusRecord();

        EmployeeLog empLog = GlobalState.getInstance().getCurrentEmployeeLog();
        if (empLog != null) {
            String serialNumber = eobrController.getSerialNumber();
            /*
            Get the most recent login event to update, as well as the most recent duty status event,
            as this will be fired:
            After selection of initial duty status
            AND
            After populating trip information
             */
            EmployeeLogEldEvent empLogLoginEldEvent = this.GetMostRecentLoginEvent(empLog);
            EmployeeLogEldEvent empLogSelectedDutyStatusEvent = EmployeeLogUtilities.GetLastEventInLog(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus, empLog);
            /*
            Since this is called from the TripInfo fragment, this code can fire when a user
            updates/modifies the Trip Info through the system menu. Thus, we only want to
            set values on the above events if the login event HASN'T been associated with the
            log yet. This is an indicator that the user is within the login process rather
            than the system menu option
             */
            if (empLogLoginEldEvent.getLogKey() != empLog.getPrimaryKey()) {
                String tractorNumbers = empLog.getTractorNumbers(),
                        trailerNumbers = empLog.getTrailerNumbers(),
                        shipmentInfo = empLog.getShipmentInformation(),
                        trailerPlate = empLog.getTrailerPlate(),
                        vehiclePlate = empLog.getVehiclePlate();
                int logPK = empLog.isPrimaryKeySet() ? (int) empLog.getPrimaryKey() : -1;
                /*Set the above to the two events generated by login
                Only need to set the ELD event's log key, as the selected DS event will already
                have that information
                */
                empLogLoginEldEvent.setLogKey(logPK);
                //Same with serial number and ruleset
                if (serialNumber != null && !serialNumber.equals(""))
                    empLogLoginEldEvent.setEobrSerialNumber(serialNumber);
                //User current user's ruleset type enum
                empLogLoginEldEvent.setRulesetType(empLog.getRuleset());

                //Set trip info for login event
                empLogLoginEldEvent.setTractorNumber(tractorNumbers);
                empLogLoginEldEvent.setTrailerNumber(trailerNumbers);
                empLogLoginEldEvent.setShipmentInfo(shipmentInfo);
                empLogLoginEldEvent.setTrailerPlate(trailerPlate);
                empLogLoginEldEvent.setVehiclePlate(vehiclePlate);
                //Set trip info for initial DS event
                empLogSelectedDutyStatusEvent.setTractorNumber(tractorNumbers);
                empLogSelectedDutyStatusEvent.setTrailerNumber(trailerNumbers);
                empLogSelectedDutyStatusEvent.setShipmentInfo(shipmentInfo);
                empLogSelectedDutyStatusEvent.setTrailerPlate(trailerPlate);
                empLogSelectedDutyStatusEvent.setVehiclePlate(vehiclePlate);

                float odom = eobrController.GetRawOdometer();
                if (odom != -1.0F && empLogLoginEldEvent.getLocation() != null && empLogLoginEldEvent.getLocation().getOdometerReading() < 0) {
                    empLogLoginEldEvent.setOdometer(odom);
                }
                empLogLoginEldEvent.setEngineHours(GetCurrentTotalEngineHours());

                SetReducedPrecisionGPSInfo(empLogLoginEldEvent, statusRecord, GlobalState.getInstance().getIsInPersonalConveyanceDutyStatus());

                if(empLog.getIsExemptFromELDUse()){
                    empLogLoginEldEvent.setEventRecordStatus(2);
                }

                //Update Login event
                eventFacade.Update(empLogLoginEldEvent);

                //Only add the Login event, as we should already have the duty status change event in log
                AddEventToLog(empLog, empLogLoginEldEvent);

                SaveLocalEmployeeLog(empLog);

                GlobalState.getInstance().setCurrentEmployeeLog(empLog);
            }


            CheckEventForMalfunction(empLog, false, empLogLoginEldEvent, statusRecord);
            addUnassignedMalfunctionsToNewLog(empLog);
        }
    }

    private void AddEventToLog(EmployeeLog log, EmployeeLogEldEvent event) {
        if (log != null) {
            if (log.getEldEventList().getEldEventList() != null) {
                ArrayList<EmployeeLogEldEvent> newList = new ArrayList<>(Arrays.asList(log.getEldEventList().getEldEventList()));
                newList.add(event);
                log.getEldEventList().setEldEventList(newList);

                // make sure that the rule of the log represents the rule of the last log event
                if (event.getEventType() == Enums.EmployeeLogEldEventType.DutyStatusChange && newList.size() > 0) {
                    EmployeeLogEldEvent lastDutyStatusEvent = EmployeeLogUtilities.GetLastEventInLog(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus, log);
                    log.setRuleset(lastDutyStatusEvent.getRulesetType());
                }
            }
        }
    }

    private EmployeeLog getUsersLog(User user) {
        EmployeeLog log = null;
        for (User loggedInUser : GlobalState.getInstance().getLoggedInUserList()) {
            if (loggedInUser.getCredentials().getEmployeeId().equals(user.getCredentials().getEmployeeId())) {
                if (GlobalState.getInstance().getCurrentDesignatedDriver() == loggedInUser) {
                    log = GlobalState.getInstance().getCurrentDriversLog();
                } else if (GlobalState.getInstance().getCurrentUser() == loggedInUser) {
                    log = GlobalState.getInstance().getCurrentEmployeeLog();
                } else {
                    log = this.GetLocalEmployeeLog(loggedInUser, TimeKeeper.getInstance().getCurrentDateTime().toDate());
                }
            }
        }
        return log;
    }

    public void addUnassignedMalfunctionsToNewLog(EmployeeLog assignToMe) {
        if (GlobalState.getInstance().getLoggedInUserList().size() == 1) {
            //user 1 scenario
            EmployeeLogEldEvent[] homelessMalfunctions = eventFacade.GetByEventTypes(-1,
                    Arrays.asList(new Integer[]{Enums.EmployeeLogEldEventType.Malfunction_DataDiagnosticDetection.getValue()}),
                    Arrays.asList(new Integer[]{Enums.EmployeeLogEldEventRecordStatus.Active.getValue()})
            );
            for (EmployeeLogEldEvent homelessMalf : homelessMalfunctions) {
                homelessMalf.setLogKey((int) assignToMe.getPrimaryKey());
                eventFacade.Update(homelessMalf);
                AddEventToLog(assignToMe, homelessMalf);
                SaveLocalEmployeeLog(assignToMe);
            }
        } else if (GlobalState.getInstance().getLoggedInUserList().size() == 2) {
            //user 2 scenario
            EmployeeLog user1Log = null;
            EmployeeLog user2Log = null;
            for (User loggedInUser : GlobalState.getInstance().getLoggedInUserList()) {
                if (loggedInUser.getCredentials().getEmployeeId().equals(assignToMe.getEmployeeId())) {
                    user2Log = getUsersLog(loggedInUser);
                } else {
                    user1Log = getUsersLog(loggedInUser);
                }
            }

            Collection<Malfunction> user1Malfunctions = getActiveMalfunctions(user1Log);
            for (Malfunction currentMalfunction : user1Malfunctions) {
                try {
                    createMalfunctionOrDataDiagnosticEvent(user2Log, TimeKeeper.getInstance().getCurrentDateTime().toDate(), EmployeeLogEldEventCode.EldMalfunctionLogged, currentMalfunction.getDmoValue(), 0);
                } catch (Throwable t) {
                    ErrorLogHelper.RecordException(t);
                    throw new RuntimeException(t);
                }
            }
        }
    }

    @Override
    public boolean HasEldMandateDrivingExceptionEnabled(DutyStatusEnum dutyStatus) {
        return (GlobalState.getInstance().getIsInYardMoveDutyStatus() && dutyStatus.getValue() == DutyStatusEnum.DRIVING) || (GlobalState.getInstance().getIsInPersonalConveyanceDutyStatus() && dutyStatus.getValue() == DutyStatusEnum.DRIVING);
    }

    private void CreateCertificationEvent(EmployeeLog logToCertify) throws Throwable {
        EmployeeLogEldEvent eventToSave = new EmployeeLogEldEvent();
        PopulateCertificationEvent(eventToSave, logToCertify);
        SaveEvent(logToCertify, eventToSave, null, "EmployeeLogEldMandateController.CreateCertificationEvent");
    }

    @Override
    public void CertifyEmployeeLog(EmployeeLog logToCertify) throws Throwable {
        this.CreateCertificationEvent(logToCertify);
        logToCertify.setIsCertified(true);
        SaveLocalEmployeeLog(logToCertify);
    }

    /**
     * Finds the end time for an event.
     *
     * @param event event to find end time for.
     * @param user  user to assign the event to.
     */
    public Date findEndTimeForEvent(EmployeeLogEldEvent event, User user) throws Throwable {

        LocalEditedEldEventLog localEditedEldEventLog = getLocalLogForEditedEldEvent(user, event.getStartTime(), event);
        EmployeeLog parentLog = localEditedEldEventLog.getEmployeeLog();
        event = localEditedEldEventLog.getEditedEldEvent();

        Date endTime = parentLog.getMobileEndTimestamp();
        EmployeeLogEldEvent[] eldEventArray = fetchEldEventsByEventTypes(event.getLogKey(),
                Arrays.asList(Enums.EmployeeLogEldEventType.DutyStatusChange.getValue()),
                Arrays.asList(Enums.EmployeeLogEldEventRecordStatus.Active.getValue()));

        EmployeeLogEldEvent nextEvent = EmployeeLogUtilities.getNextActiveEventAfterDate(Arrays.asList(eldEventArray), event.getEventDateTime());
        if (nextEvent != null) {
            endTime = nextEvent.getEventDateTime();
        }

        return endTime;
    }

    /**
     * Assigns an Unassigned Eld Event to a User.
     *
     * @param event   event to save to the user.
     * @param endTime end time of the event.  If null it will use the log.
     * @param user    user to assign the event to.
     * @throws Throwable If there was an error saving the event.
     */
    public List<EmployeeLogEldEvent> assignEldEventToUser(EmployeeLogEldEvent event, Date endTime, User user) throws CloneNotSupportedException {
        LocalEditedEldEventLog localEditedEldEventLog = getLocalLogForEditedEldEvent(user, event.getStartTime(), (EmployeeLogEldEvent) event.clone());
        EmployeeLog log = localEditedEldEventLog.getEmployeeLog();

        List<EmployeeLogEldEvent> assignedEvents = new ArrayList<>();
        Date nextLogStart = DateUtility.AddDays(log.getLogDate(), 1);
        Date currentLogMidnight = DateUtility.AddMilliseconds(nextLogStart, -1);
        try {
            if (endTime != null && endTime.after(nextLogStart)) {

                // distribute distance across logs
                int totalDistance = localEditedEldEventLog.getEditedEldEvent().getDistance() == null ? 0 : localEditedEldEventLog.getEditedEldEvent().getDistance().intValue();

                long msOnFirstLog = nextLogStart.getTime() - localEditedEldEventLog.getEditedEldEvent().getEventDateTime().getTime();
                double percentFirst = (double) msOnFirstLog / (double) (endTime.getTime() - localEditedEldEventLog.getEditedEldEvent().getEventDateTime().getTime());

                //this assumes a constant rate of travel across the entire driving period
                float milesFirst = (float) (totalDistance * percentFirst);

                localEditedEldEventLog.getEditedEldEvent().setDistance(Math.round(milesFirst));

                assignedEvents.add(assignEldEventToLog(localEditedEldEventLog, currentLogMidnight, user));

                EmployeeLogEldEvent splitEventForNextDate = (EmployeeLogEldEvent) event.clone();
                splitEventForNextDate.setPrimaryKey(-1);    // force insert
                splitEventForNextDate.setLogKey(null);
                splitEventForNextDate.setEncompassClusterPK(0L); // generate new Encompass record
                splitEventForNextDate.setEventSequenceIDNumber(EmployeeLogEldEnum.DEFAULT);
                splitEventForNextDate.setEventDateTime(nextLogStart);
                splitEventForNextDate.setEventRecordOrigin(Enums.EmployeeLogEldEventRecordOrigin.AssumedUnidentifiedDriver);
                splitEventForNextDate.setIsManuallyEditedByKMBUser(true);
                splitEventForNextDate.setIsReviewed(true);

                splitEventForNextDate.setOdometer(localEditedEldEventLog.getEditedEldEvent().getOdometer() + localEditedEldEventLog.getEditedEldEvent().getDistance());
                splitEventForNextDate.setDistance(totalDistance == 0 ? 0 : totalDistance - localEditedEldEventLog.getEditedEldEvent().getDistance());

                //this will create a new edited log..
                EmployeeLog empLog = this.GetLocalEmployeeLog(user, nextLogStart);
                if (empLog == null) {
                    //no log for this day... create with inital event as the split event.
                    empLog = EmployeeLogUtilities.CreateNewLog(this.getContext(), user, nextLogStart, splitEventForNextDate);
                    EmployeeLogEldEvent savedInitEvent = empLog.getEldEventList().getEldEventList()[0];
                    assignedEvents.add(savedInitEvent);
                } else {
                    //log exists... edit the local log.
                    LocalEditedEldEventLog nextDayLocalLog = getLocalLogForEditedEldEvent(user, nextLogStart, splitEventForNextDate);
                    assignedEvents.add(assignEldEventToLog(nextDayLocalLog, endTime, user));
                }
            } else {
                assignedEvents.add(assignEldEventToLog(localEditedEldEventLog, endTime, user));
            }
        } catch (Throwable throwable) {
            ErrorLogHelper.RecordException(throwable);
            throw new RuntimeException(throwable);
        }
        return assignedEvents;
    }

    public EmployeeLogEldEvent assignEldEventToLog(LocalEditedEldEventLog localEditedLog, Date endTime, User user) throws Throwable {
        //Use saveEldEvent
        EmployeeLog parentLog = localEditedLog.getEmployeeLog();
        EmployeeLogEldEvent event = localEditedLog.getEditedEldEvent();

        // get a list of sibling events for the employee log
        EmployeeLogEldEvent[] eldEventArray = fetchEldEventsByEventTypes((int) parentLog.getPrimaryKey(),
                Arrays.asList(Enums.EmployeeLogEldEventType.DutyStatusChange.getValue()),
                Arrays.asList(Enums.EmployeeLogEldEventRecordStatus.Active.getValue()));

        // Determine if the Claim will overlap automatically recorded driving time
        InvalidateAutomaticDriveTimeEnum invalidateAutomaticDriveTimeEnum = willEditInvalidateAutomaticDriveTime(eldEventArray, event.getPrimaryKey(), event.getStartTime(), endTime, event.getEobrSerialNumber());
        if (invalidateAutomaticDriveTimeEnum == InvalidateAutomaticDriveTimeEnum.OVERLAP_SAME_SERIALNUMBER) {
            return null;
        }

        List<EmployeeLogEldEvent> eldEventList = Arrays.asList(eldEventArray);

        // assign the rule set to the rule set of the existing event prior to the event being claimed
        if (event.getRuleSet() == null || event.getRuleSet().getValue() == RuleSetTypeEnum.NULL) {
            EmployeeLogEldEvent previousEvent = previousEldEvent(eldEventList, event.getEventDateTime());
            if (previousEvent != null) {
                event.setRuleSet(previousEvent.getRuleSet());
            } else {
                event.setRuleSet(parentLog.getRuleset());
            }
        }

        if (endTime == null) {
            endTime = parentLog.getMobileEndTimestamp();

            EmployeeLogEldEvent nextEvent = EmployeeLogUtilities.getNextActiveEventAfterDate(eldEventList, event.getEventDateTime());
            if (nextEvent != null) {
                endTime = nextEvent.getEventDateTime();
            }
        }

        //Save the event to the parent log.
        event.setDriverOriginatorUserId(user.getCredentials().getEmployeeId());
        event.setLogKey((int) parentLog.getPrimaryKey());

        //... Run the the logic to ensure the event is valid.
        return saveEldEvent(event, Enums.SpecialDrivingCategory.None, endTime, Enums.ActionInitiatingSaveEnum.ClaimUnidentifiedEvent);
    }

    @Override
    public void CreateDutyStatusChangedEventForLogin(EmployeeLog empLog, Date timestamp, DutyStatusEnum dutyStatus, Location location, boolean isStartTimeValidated, RuleSetTypeEnum ruleset, String logRemark, Date logRemarkDate, int recordOrigin, String employeeId) throws Throwable {
        //If we don't have a ruleset on the log, temporarily set it to the initial user's ruleset, and after an event has been created and hydrated
        //reset it
        RuleSetTypeEnum initialLogRuleset = empLog.getRuleset();
        boolean useTempRuleset = (initialLogRuleset == null || initialLogRuleset.getValue() == RuleSetTypeEnum.NULL);

        if (useTempRuleset)
            empLog.setRuleset(ruleset);


        //Now create the user-selected duty status
        CreateDutyStatusChangedEvent(empLog, timestamp, dutyStatus, location, isStartTimeValidated, empLog.getRuleset(), null, null, true, null, null);
        //Reset ruleset to initial value
        if (useTempRuleset)
            empLog.setRuleset(initialLogRuleset);
    }

    @Override
    public boolean ManualDutyStatusChangeShouldEndDriving(DutyStatusEnum dutyStatus, Date endOfDrivingTimestamp) {
        return false;
    }

    public Date ManualDutyStatusChangeGetTimeOfNewEvent(DutyStatusEnum dutyStatus, Date timeOfNewEvent, Date endOfDrivingTimestamp, EmployeeLogEldEvent lastEvent) {
        if(lastEvent.getEventRecordOrigin() == Enums.EmployeeLogEldEventRecordOrigin.AutomaticallyRecorded
                && lastEvent.getDutyStatusEnum().getValue() == DutyStatusEnum.DRIVING) {
            return endOfDrivingTimestamp;
        }
        return timeOfNewEvent;
    }

    @Override
    public void CreateDutyStatusChangedEvent(EmployeeLog empLog, Date timestamp, DutyStatusEnum dutyStatus, Location location, boolean isStartTimeValidated, RuleSetTypeEnum ruleset, String logRemark, Date logRemarkDate, boolean isManualDutyStatusChange, String motionPictureProductionId, String motionPictureAuthorityId) throws Throwable {
        EmployeeLogEldEvent lastEventInLog = EmployeeLogUtilities.GetLastEventInLog(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus, empLog);

        if (lastEventInLog != null) {
            boolean lastEventDriving = lastEventInLog.getDutyStatusEnum().getValue() == DutyStatusEnum.DRIVING;
            boolean isLastEventAutomaticallyRecorded = Enums.EmployeeLogEldEventRecordOrigin.AutomaticallyRecorded == lastEventInLog.getEventRecordOrigin();
            if (lastEventDriving && isLastEventAutomaticallyRecorded && isManualDutyStatusChange) {
                if(GlobalState.getInstance().getPotentialDrivingStopTimestamp() != null){
                    timestamp = GlobalState.getInstance().getPotentialDrivingStopTimestamp();
                }
            }
        }
        saveDutyStatusChangeEvent(empLog, timestamp, dutyStatus, location, isStartTimeValidated, ruleset, logRemark, logRemarkDate, isManualDutyStatusChange, motionPictureProductionId, motionPictureAuthorityId);
    }

    private void saveDutyStatusChangeEvent(EmployeeLog empLog, Date timestamp, DutyStatusEnum dutyStatus, Location location, boolean isStartTimeValidated, RuleSetTypeEnum ruleset, String logRemark, Date logRemarkDate, boolean isManualDutyStatusChange, String motionPictureProductionId, String motionPictureAuthorityId) {
        try {
            StatusRecord statusRecord = null;
            if (isManualDutyStatusChange) {
                statusRecord = getLatestStatusRecord();
            } else {
                statusRecord = getStatusRecord(timestamp);

                // If we are unable to find a StatusRecord by timestamp, then we'll use the latest status record instead (ensure we have odometer/etc set properly for driving events)
                if (dutyStatus.getValue() == DutyStatusEnum.DRIVING && statusRecord == null) {
                    String msg = String.format("StatusRecord lookup by timestamp %s failed while preparing for creation of driving duty status event in EmployeeLogEldMandateController.saveDutyStatusChangeEvent(...).  Retrieving latest status record instead.", timestamp.toString());
                    ErrorLogHelper.RecordMessage(msg);
                    statusRecord = getLatestStatusRecord();
                }
            }

            EmployeeLogEldEvent eventToSave = new EmployeeLogEldEvent(timestamp, new EmployeeLogEldEventCode(EmployeeLogEldEvent.translateDutyStatusEnumToMandateStatus(dutyStatus)), Enums.EmployeeLogEldEventType.DutyStatusChange);
            eventToSave.setInternalObjectValueMap(constructMandateValuePayload(Enums.EmployeeLogEldEventType.DutyStatusChange.getValue(), EmployeeLogEldEvent.translateDutyStatusEnumToMandateStatus(dutyStatus), location, statusRecord, empLog, (int) empLog.getPrimaryKey(), timestamp));
            eventToSave.hydrate(true);
            eventToSave.setDriverOriginatorUserId(empLog.getEmployeeId());
            if (isManualDutyStatusChange) {
                eventToSave.setEventRecordOrigin(Enums.EmployeeLogEldEventRecordOrigin.EditedEnteredByDriver);
            } else {
                eventToSave.setEventRecordOrigin(Enums.EmployeeLogEldEventRecordOrigin.AutomaticallyRecorded);
            }
            SetEngineHoursAndAccumulatedVehicleMiles(eventToSave, eventToSave.getEventDateTime(), statusRecord);
            requireManualLocationIfGpsIsInvalid(statusRecord, eventToSave);
            eventToSave.setMotionPictureProductionId(motionPictureProductionId);
            eventToSave.setMotionPictureAuthorityId(motionPictureAuthorityId);
            eventToSave.setLogRemark(logRemark);
            eventToSave.setRuleSet(ruleset);

            SetReducedPrecisionGPSInfo(eventToSave, statusRecord, GlobalState.getInstance().getIsInPersonalConveyanceDutyStatus());

            // if onduty and not currently running a test, check to see if we should run a Data Transfer test
            if (dutyStatus.getValue() == DutyStatusEnum.ONDUTY) {

                //TODO:Temporary debug line. Can be removed at some point but adding for testing purposes.
                ErrorLogHelper.RecordMessage("Data Transfer Test -- Attempt Initiate");

                if (!GlobalState.getInstance().getisDataTransferMechanismStatusInProgress()) {

                    //TODO:Temporary debug line. Can be removed at some point but adding for testing purposes.
                    ErrorLogHelper.RecordMessage("Data Transfer Test -- Initiate");

                    runDataTransferMechanismStatus();
                }
            }

            if (statusRecord != null) {
                // set the distance since last valid coordinates
                eventToSave.setDistanceSinceLastCoordinates(EventTranslationBase.calculateDistanceSinceLastValidCoordinates(statusRecord.getGpsUncertDistance()));
            }
            setEventGeolocation(location, eventToSave);
            SaveEvent(empLog, eventToSave, statusRecord, "EmployeeLogEldMandateController.CreateDutyStatusChangedEvent DutyStatusChange");
        } catch (Throwable throwable) {
            ErrorLogHelper.RecordException(throwable);
            throw new RuntimeException(throwable);
        }
    }

    private void setEventGeolocation(Location location, EmployeeLogEldEvent eventToSave) {
        if (location != null) {
            eventToSave.setGeolocation(location.ToLocationString());
        } else if (GlobalState.getInstance().getLastLocation() != null) {
            eventToSave.setGeolocation(GlobalState.getInstance().getLastLocation().ToLocationString());
        } else {
            GpsLocation gpsLocation = createGeolocationValue(eventToSave.getLatitude(), eventToSave.getLongitude());
            if (gpsLocation != null) {
                eventToSave.setGeolocation(gpsLocation.ToLocationString());
            }
        }
    }

    private GpsLocation createGeolocationValue(Double latitude, Double longitude) {
        GpsLocation gpsLocation = null;
        try {
            gpsLocation = new GpsLocation(latitude.floatValue(), longitude.floatValue());
            IAPIController empLogController = MandateObjectFactory.getInstance(this.getContext(), GlobalState.getInstance().getFeatureService()).getCurrentEventController();
            empLogController.ReverseGeocodeLocation(Collections.singletonList(gpsLocation));
        } catch (Exception ex) {
            Log.e("EmployeeLogEldMandate", "Error in createGeoLocation", ex);
        }
        return gpsLocation;
    }

    private void runDataTransferMechanismStatus() throws IOException {

        // Refresh the Global Data Transfer Mechanism Status data
        DataTransferMechanismStatusController ctrlr = new DataTransferMechanismStatusController(getContext());
        ctrlr.GetKMBDataTransferMechanismStatus();
        //ctrlr.GetEncompassDataTransferMechanismStatus(kmbData.getTransferId());

        Date scheduledTransferDate = GlobalState.getInstance().getDataTransferMechanismStatus().getDateScheduledToTransfer();

        try {
            if ((DateTime.now().toDate().after(scheduledTransferDate))
                    || (DateTime.now().toDate().equals(scheduledTransferDate))) {

                GlobalState.getInstance().setisDataTransferMechanismStatusInProgress(true);
                // Begin the Async Encompass Data Transfer Mechanism process
                _sendSetDataTransferMechanismStatusRequestTask = new SendSetDataTransferMechanismStatusRequestTask();
                _sendSetDataTransferMechanismStatusRequestTask.execute(new Void[0]);
            }

        } catch (Exception e) {
            GlobalState.getInstance().setisDataTransferMechanismStatusInProgress(false);
            String msg = String.format("%s failure to execute runDataTransferMechanismStatus event:", e.getMessage());
            Log.d("MandateCtl.runDTMStatus", msg);
            ErrorLogHelper.RecordMessage(this.getContext(), msg);
            throw e;
        }
    }

    public Boolean IsDataTransferMalfunctionEventActive() {
        return eventFacade.IsDataTransferMalfunctionEventActive();
    }

    public Boolean IsMalfunctionDataTransferComplianceActive(){
        return eventFacade.IsMalfunctionDataTransferComplianceActive();
    }

    private class SendSetDataTransferMechanismStatusRequestTask extends AsyncTask<Void, String, Boolean> {
        Exception ex;

        protected void onPreExecute() {
            //LockScreenRotation();
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            // Perform Data Transfer
            return PerformSendSetDataTransfer();

        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (ex == null) {
                CreateDataTransferTimer();
            } else {
                if (ex.getClass() == KmbApplicationException.class)
                    HandleException((KmbApplicationException) ex);
            }

            //UnlockScreenRotation();
        }

    }

    /// <summary>
    /// Attempt to submit the all the records to DMO.
    /// If the network is not available, then display a message.
    /// Answer if everything was submitted successfully.
    /// </summary>
    /// <returns></returns>
    private boolean PerformSendSetDataTransfer() {
        boolean response = false;
        try {
            DataTransferMechanismStatusController ctrlr = new DataTransferMechanismStatusController(getContext());

            if (ctrlr.getIsWebServicesAvailable()) {
                //ctrlr.AddNewKMBRecord();
                ctrlr.SetEncompassDataTransferMechanismStatus();
                response = true;
            } else {
                Log.i("PerfSendSetDataTransfer", "Web Services are unavailable");
            }
        } catch (Exception ex) {
            Log.e("UnhandledCatch", ex.getMessage() + ": " + Log.getStackTraceString(ex));
        }
        return response;
    }

    private Handler _dataTransferTimerHandler;

    private void CreateDataTransferTimer() {
        // create a timer that will be set to pop at 15 minutes and get the Data Transfer Mechanism status
        if (_dataTransferTimerHandler == null) {
            _dataTransferTimerHandler = new Handler();
            _dataTransferTimerHandler.removeCallbacks(_dataTransferTimerTask);
            _dataTransferTimerHandler.postDelayed(_dataTransferTimerTask, _dataTransferMechanismTimerValue);
            //ErrorLogHelper.RecordMessage(getContext(), "Setting Timer value = " + _dataTransferMechanismTimerValue);
        }
    }

    private Runnable _dataTransferTimerTask = new Runnable() {
        public void run() {
            // message

            try {
                // Call the Async Encompass Data Transfer Mechanism get
                _sendGetDataTransferMechanismStatusRequestTask = new SendGetDataTransferMechanismStatusRequestTask();
                _sendGetDataTransferMechanismStatusRequestTask.execute(new Void[0]);
            } catch (Throwable e) {
                Log.e("UnhandledCatch", e.getMessage() + ": " + Log.getStackTraceString(e));
            }

           /* // cancel timer
            _dataTransferTimerHandler.removeCallbacks(_dataTransferTimerTask);*/
        }
    };

    private class SendGetDataTransferMechanismStatusRequestTask extends AsyncTask<Void, String, Boolean> {
        protected void onPreExecute() {
            //LockScreenRotation();

        }

        @Override
        protected Boolean doInBackground(Void... params) {

            Boolean result = null;
            try {
                // Perform Data Transfer
                DataTransferMechanismStatusController ctrlr = new DataTransferMechanismStatusController(getContext());
                if (ctrlr.getIsWebServicesAvailable()) {
                    String transferId = GlobalState.getInstance().getDataTransferMechanismStatus().getTransferId();
                    result = ctrlr.GetEncompassDataTransferMechanismStatus(transferId);
                } else {
                    Log.i("PerfSendGetDataTransfer", "Web Services are unavailable");
                }
            } catch (Exception e) {
                Log.e("PerfSendGetDataTransfer", e.getMessage());
            }
            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            try {
                if (result != null) {
                    String transferId = GlobalState.getInstance().getDataTransferMechanismStatus().getTransferId();
                    DataTransferMechanismStatusController ctrlr = new DataTransferMechanismStatusController(getContext());
                    EmployeeLogEldMandateController EmplLogEventCtrlr = new EmployeeLogEldMandateController(getContext());
                    int codeToLog = ctrlr.SetKMBNextTransferDate(transferId, result);
                    int consecutiveSuccessfulTransfer = ctrlr.GetConsecutiveSuccessfulTransfers();
                    int totalFailedTransfers = ctrlr.GetTotalFailedTransfers();

                    HandleDataDiagnosticEventCreation(result, totalFailedTransfers, consecutiveSuccessfulTransfer, EmplLogEventCtrlr);
                }
                //UnlockScreenRotation();
                // cancel timer
                _dataTransferTimerHandler.removeCallbacks(_dataTransferTimerTask);
            } catch (Throwable throwable) {
                Log.e("PerfSendGetDataTransfer", "Error while getting Encompass Data Transfer Mechanism status data in post execute", throwable);
            }

            GlobalState.getInstance().setisDataTransferMechanismStatusInProgress(false);

            //TODO:Temporary debug line. Can be removed at some point but adding for testing purposes.
            ErrorLogHelper.RecordMessage("Data Transfer Test -- Complete");
        }

    }

    protected void HandleDataDiagnosticEventCreation(Boolean result, int totalFailedTransfers, int consecutiveSuccessfulTransfer, EmployeeLogEldMandateController EmplLogEventCtrlr) throws Throwable {
        try {
            boolean isDataTransferMalfunctionEventActive = EmplLogEventCtrlr.IsDataTransferMalfunctionEventActive();
            boolean isEldMalfunctionDataCompliance = EmplLogEventCtrlr.IsMalfunctionDataTransferComplianceActive();

            //result is the value of the most recent check (e.g. today's check)
            if (result) {
                //if we do not need a diagnostic nor a data transfer compliance malfunction
                if (consecutiveSuccessfulTransfer > 2) {
                    if (isDataTransferMalfunctionEventActive) {
                        CreateDataDiagnosticClearedEvent(GlobalState.getInstance().getCurrentEmployeeLog(), DateUtility.getCurrentDateTimeWithSecondsUTC(), DataDiagnosticEnum.DATA_TRANSFER, 0);
                    }
                    if (isEldMalfunctionDataCompliance) {
                        CreateMalfunctionELDClearedEvent(GlobalState.getInstance().getCurrentEmployeeLog(), DateUtility.getCurrentDateTimeWithSecondsUTC(), Malfunction.DATA_TRANSFER_COMPLIANCE);
                    }
                }
                //if we need only to add a diagnostic
                if (totalFailedTransfers > 0 && consecutiveSuccessfulTransfer < 3) {
                    if (!isDataTransferMalfunctionEventActive) {
                        CreateDataDiagnosticEvent(GlobalState.getInstance().getCurrentEmployeeLog(), DateUtility.getCurrentDateTimeWithSecondsUTC(), DataDiagnosticEnum.DATA_TRANSFER, 4);
                    }
                }
                //if we need to add a data compliance malfunction
                if (totalFailedTransfers > 2) {
                    if (!isEldMalfunctionDataCompliance && totalFailedTransfers > 2) {
                        CreateMalfunctionELDEvent(GlobalState.getInstance().getCurrentEmployeeLog(), DateUtility.getCurrentDateTimeWithSecondsUTC(), Malfunction.DATA_TRANSFER_COMPLIANCE);
                    }
                }
            } else if (!result) {
                //if we need only to add a diagnostic
                if (totalFailedTransfers > 0 && totalFailedTransfers < 3) {
                    if (!isDataTransferMalfunctionEventActive) {
                        CreateDataDiagnosticEvent(GlobalState.getInstance().getCurrentEmployeeLog(), DateUtility.getCurrentDateTimeWithSecondsUTC(), DataDiagnosticEnum.DATA_TRANSFER, 4);
                    }
                }
                //if we need to add a data compliance malfunction
                if (totalFailedTransfers > 2) {
                    if (!isEldMalfunctionDataCompliance) {
                        CreateMalfunctionELDEvent(GlobalState.getInstance().getCurrentEmployeeLog(), DateUtility.getCurrentDateTimeWithSecondsUTC(), Malfunction.DATA_TRANSFER_COMPLIANCE);
                    }
                }
            }
        }
        catch(Exception ex){
            ErrorLogHelper.RecordMessage("Unable to HandleDataDiagnosticEventCreation. Exception: " + ex.getMessage());
        }
    }

    private void requireManualLocationIfGpsIsInvalid(StatusRecord statusRecord, EmployeeLogEldEvent event) {
        boolean hasValidLocation = false;

        if (statusRecord != null) {
            hasValidLocation = isGPSValid(statusRecord);
        }

        // If a driver has a manual location, do not clear all location data on event
        if (event.getDriversLocationDescription() != null && !event.getDriversLocationDescription().equals("")) {
            Log.d("Location Manual", String.format("Drivers Location Description:%s%nGps location is Valid:%h", event.getDriversLocationDescription(), hasValidLocation));
            hasValidLocation = true;
        }

        if (!hasValidLocation) {
            Log.d("Location", "Gps location invalid and no Drivers Location Description found: PURGING all location data from event");
            event.setRequiresManualLocation(true);
            event.setLocation(null);
        }
    }

    public boolean isGPSValid() {
        StatusRecord statusRecord = getLatestStatusRecord();
        return isGPSValid(statusRecord);
    }

    private boolean isGPSValid(StatusRecord statusRecord) {
        boolean hasValidLocation = false;

        if (statusRecord != null) {
            Float uncertainty = EventTranslationBase.calculateDistanceSinceLastValidCoordinates(statusRecord.getGpsUncertDistance());
            if (uncertainty != null && uncertainty < MAX_VALID_UNCERTAINTY_MILES) {
                hasValidLocation = true;
            }
        }
        return hasValidLocation;
    }

    @Override
    public void CheckForAndCreateEndOfPCYMWT_Event(EmployeeLog employeeLog, Date timestamp, Location location) throws Throwable {
        CheckForAndCreateEndOfPCYMWT_Event(employeeLog, timestamp, location, false);
    }

    @Override
    public void CheckForAndCreateEndOfPCYMWT_Event(EmployeeLog employeeLog, Date timestamp, Location location, boolean forMidnightTransition) throws Throwable {
        if (GlobalState.getInstance().getIsEndingActivePCYMWT_Status()) {
            GlobalState.getInstance().setIsEndingActivePCYMWT_Status(false);

            String logRemark = "failed";
            String annotation = "failed";
            int eventCode = EmployeeLogEldEventCode.ChangeInDriversIndication_PCYMWT_Cleared;
            Integer distance = null;

            ArrayList<EmployeeLogEldEvent> eldEventArray = new ArrayList<>(Arrays.asList(employeeLog.getEldEventList().getEldEventList(EldEventAdapterList.ListAccessorModifierEnum.NonDutyStatus)));
            int testIndex = eldEventArray.size() - 1;
            EmployeeLogEldEvent questForLast = null;
            boolean questing = true;

            while (questing && testIndex >= 0) {
                questForLast = eldEventArray.get(testIndex);

                if (questForLast.getEventType() == Enums.EmployeeLogEldEventType.ChangeInDriversIndication) {
                    //if the last change in driver's indication was a clear, then there's nothing for us to clear now
                    if(questForLast.getEventCode() == EmployeeLogEldEventCode.ChangeInDriversIndication_PCYMWT_Cleared) {
                        questForLast = null;
                        break;
                    } else {

                        questing = false;
                        annotation = questForLast.getEventComment();

                        float currentOdometer = getLatestOdometerReading();
                        Float startOdometer = questForLast.getEndOdometer();
                        if(startOdometer != null && startOdometer > 0 && currentOdometer > 0) {
                            distance = Math.round(currentOdometer - startOdometer);
                        }

                        switch (questForLast.getEventCode()) {
                            case EmployeeLogEldEventCode.ChangeInDriversIndication_PersonalUse:
                                logRemark = "End of PC";
                                break;
                            case EmployeeLogEldEventCode.ChangeInDriversIndication_YardMoves:
                                logRemark = "End of YM";
                                break;
                            default:
                                break;
                        }
                    }
                } else {
                    questForLast = null;
                }
                testIndex--;
            }

            if(questForLast != null) {
                CreateChangeInDriversIndicationEvent(employeeLog, timestamp, location, annotation, eventCode, logRemark, distance);

                if(!forMidnightTransition) {
                    GlobalState.getInstance().setIsInPersonalConveyanceDutyStatus(false);
                    GlobalState.getInstance().setIsInYardMoveDutyStatus(false);
                }
            }
        }
    }

    @Override
    public void CheckForAndCreatePersonalConveyanceChangedEvent(EmployeeLog empLog, Date timestamp, Location location, String annotation) throws Throwable {
        if (GlobalState.getInstance().getIsInPersonalConveyanceDutyStatus()) {
            String logRemark = "Personal Conveyance";
            int eventCode = EmployeeLogEldEventCode.ChangeInDriversIndication_PersonalUse;
            CreateChangeInDriversIndicationEvent(empLog, timestamp, location, annotation, eventCode, logRemark);
        }
    }

    @Override
    public void CheckForAndCreateYardMoveEvent(EmployeeLog employeeLog, Date timeStamp, Location location, String annotation) throws Throwable {
        // create an ELD Mandate Yard Move Event if in Yard Move Duty Status and it was a manual duty status change
        if (GlobalState.getInstance().getIsInYardMoveDutyStatus()) {
            String logRemark = "Yard Move";
            int eventCode = EmployeeLogEldEventCode.ChangeInDriversIndication_YardMoves;
            CreateChangeInDriversIndicationEvent(employeeLog, timeStamp, location, annotation, eventCode, logRemark);
        }
    }

    public void CreateChangeInDriversIndicationEvent(EmployeeLog employeeLog, Date timeStamp, Location location, String annotation, int eventCode, String logRemark) throws Throwable {
        //distance only required for ending a YM or PC event
        Integer distance = null;
        CreateChangeInDriversIndicationEvent(employeeLog, timeStamp, location, annotation, eventCode, logRemark, distance);
    }

    // returns true if we should proceed to create duty status change or false if we should prevent the duty status change
    // when the eld mandate feature toggle is turned on and there is a drive off event that results in an onduty status change,
    // we need prevent the creation of the on duty status and instead display a dialog box that asks the user if they want to
    // continue in a driving duty status
    @Override
    public boolean CheckAndHandleDriveOffWhenThereIsANewDutyStatus(DutyStatusEnum currentDutyStatus) {
        if (currentDutyStatus.getValue() != DutyStatusEnum.DRIVING) {
            // the user was driving, however, they have now stopped driving
            eobrReader.PublishVerifyDrivingEnd();
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void CreateChangeInDriversIndicationEvent(EmployeeLog employeeLog, Date timeStamp, Location location, String annotation, int eventCode, String logRemark, Integer distance) throws Throwable {
        EmployeeLogEldEvent eventToSave = new EmployeeLogEldEvent(timeStamp);
        StatusRecord statusRecord = getLatestStatusRecord();

        eventToSave.setInternalObjectValueMap(constructMandateValuePayload(Enums.EmployeeLogEldEventType.ChangeInDriversIndication.getValue(), eventCode, location, statusRecord, employeeLog, (int) employeeLog.getPrimaryKey(), timeStamp));
        eventToSave.hydrate(true);

        // 4.3.2.2.2 (d) The ELD must prompt the driver to enter an annotation upon selection of a category from Table 2 of this appendix and record the driver’s entry.
        eventToSave.setEventComment(annotation);

        // 4.4.4.2.2(b) Edited or enter by the driver
        eventToSave.setEventRecordOrigin(2);

        // This distance field is not required for the eld mandate, but will be used to make reporting simpler
        eventToSave.setDistance(distance);

        if (statusRecord != null) {
            // set the distance since last valid coordinates
            eventToSave.setDistanceSinceLastCoordinates(EventTranslationBase.calculateDistanceSinceLastValidCoordinates(statusRecord.getGpsUncertDistance()));
        }

        eventToSave.setEndOdometer(getLatestOdometerReading());
        eventToSave.setLogRemark(logRemark);
        SetEngineHoursAndAccumulatedVehicleMiles(eventToSave, eventToSave.getEventDateTime(), null);

        SetReducedPrecisionGPSInfo(eventToSave, statusRecord, GlobalState.getInstance().getIsInPersonalConveyanceDutyStatus());

        setEventGeolocation(location, eventToSave);

        SaveEvent(employeeLog, eventToSave, statusRecord, "EmployeeLogEldMandateController.CreateChangeInDriversIndicationEvent");
    }

    /**
     * Tries to get the current engine hours and accumulated vehicle miles from the EOBR.
     * Returns null if it fails.
     *
     * @return {@link DistanceAndHours} with the total engine hours and accumulated vehicle miles if successful and null otherwise.
     */
    public DistanceAndHours GetEngineHoursAndAccumulatedVehicleMiles(EmployeeLog log) {
        return GetEngineHoursAndAccumulatedVehicleMiles(DateUtility.getCurrentDateTimeWithSecondsUTC(), log);
    }

    public DistanceAndHours GetEngineHoursAndAccumulatedVehicleMiles(Date utcTimestamp) {
        return GetEngineHoursAndAccumulatedVehicleMiles(utcTimestamp, null);
    }

    /**
     * Tries to get the engine hours and accumulated vehicle miles from the EOBR at a specific time.
     * Returns null if it fails.
     *
     * @param utcTimestamp The timestamp to get the values from
     * @return {@link DistanceAndHours} with the total engine hours and accumulated vehicle miles if successful and null otherwise.
     */
    public DistanceAndHours GetEngineHoursAndAccumulatedVehicleMiles(Date utcTimestamp, EmployeeLog employeeLog) {
        DistanceAndHours result = null;
        if (EobrReader.getIsEobrDevicePhysicallyConnected()) {
            Bundle eobrResult = eobrReader.Technician_GetDistHours(utcTimestamp.getTime());
            if (eobrResult.getInt(getContext().getString(R.string.rc)) == EobrReturnCode.S_SUCCESS) {
                result = new DistanceAndHours();
                result.setAccumulatedVehicleMiles(eobrResult.getInt(getContext().getString(R.string.tripdistance)));

                double engineHours = eobrResult.getInt(getContext().getString(R.string.runtime)) / 60.0;
                // we need to reduce the precision to 1 decimal place
                engineHours = Math.round(engineHours * 10.0) / 10.0;
                result.setEngineHours(engineHours);

                if (employeeLog != null) {
                    float dist = employeeLog.getTotalLogDistance() + employeeLog.getMobileDerivedDistance();
                    String distanceUnits = getCurrentUser().getDistanceUnits();
                    if (distanceUnits.equals(getContext().getString(R.string.kilometers))) {
                        dist = dist * GlobalState.MilesToKilometers;
                    }
                    result.setTotalVehicleMiles(Math.round(dist));
                }
            } else {
                // Add eld malfunction for this event
                Log.e(LOG_TAG, "Failed to read the Accumulated Vehicle Miles / Engine hours from the ELD");
            }
        } else {
            // Add eld malfunction for this event
            Log.e(LOG_TAG, "The Eobr Device is not connected");
        }
        return result;
    }

    /**
     * Returns the total vehicle miles from the given status record
     *
     * @return {@link Integer} with the total vehicle miles if available and null otherwise
     */
    Float GetTotalVehicleMiles(StatusRecord statusRecord) {
        if (statusRecord != null) {
            return  statusRecord.getOdometerReadingMI();
        }
        return null;
    }

    /**
     * Gets the total engine hours from the trip record offset + runtime.
     * If the trip record is null, it returns null.
     * The total engine hours are the hours the engine has been in operation since its inception.
     * The result is rounded down to the previous 0.1 hour.
     *
     * @return {@link Double} the total engine hours if available and null otherwise
     */
    private Double GetTotalEngineHoursUsingOffset(EventRecord eventRecord, int offset) {
        TripReport tr = eventRecord.getTripReportData();
        if (tr != null) {
            return ((offset + tr.getRuntime()) / 360) / 10.0;
        }

        return null;
    }

    /**
     * Gets the current total engine hours from the EOBR.
     * If the EOBR is not connected of communication fails, it returns null.
     * The total engine hours are the hours the engine has been in operation since its inception.
     * The result is rounded down to the lowest 0.1 hour.
     *
     * @return {@link Double} the total engine hours if available and null otherwise
     */
    private Double GetCurrentTotalEngineHours() {
        if (EobrReader.getIsEobrDevicePhysicallyConnected()) {
            StatusBuffer statusBuffer = eobrReader.GetStatusBuffer().getData();
            return GetTotalEngineHours(statusBuffer);
        }
        return null;
    }

    /**
     * Gets the total engine hours from the given status buffer.
     * If the status buffer is null, it returns null.
     * The total engine hours are the hours the engine has been in operation since its inception.
     * The result is rounded down to the previous 0.1 hour.
     *
     * @return {@link Double} the total engine hours if available and null otherwise
     */
    Double GetTotalEngineHours(StatusBuffer statusBuffer) {
        if (statusBuffer != null) {
            int engineHourSeconds = statusBuffer.getEngineOnTimeSeconds();
            if(engineHourSeconds <= 0) {
                engineHourSeconds = statusBuffer.getRunTimeSeconds();
            }
            return (engineHourSeconds / 360) / 10.0;
        }
        return null;
    }

    public void SetEngineHoursAndAccumulatedVehicleMiles(EmployeeLogEldEvent event, Date utcTimeStamp, StatusRecord statusRecord) {
        // Guarding against us clearing the existing odometer that may already have been set on this event.
        Float totalVehicleMiles = GetTotalVehicleMiles(statusRecord);
        if (totalVehicleMiles != null && totalVehicleMiles > 0
                && (event.getOdometer() == null || event.getOdometer() <= 0)) {
            event.setOdometer(totalVehicleMiles);
        }

        DistanceAndHours distanceAndHours = null;
        switch (event.getEventType()) {
            case DutyStatusChange:{
                // With EventType = 1 and is Geotab enabled the Accumulated vehicle miles and
                // Engine hours are set to be stored in EmployeeLogEldEvent table
                if (GlobalState.getInstance().getCompanyConfigSettings(this.getContext()).getIsGeotabEnabled()){
                    if(statusRecord != null) {
                        event.setAccumulatedVehicleMiles(Math.round(statusRecord.getTripOdometer() *  GlobalState.MilesToKilometers));
                        event.setEngineHours((double)(statusRecord.getTripEngineSeconds() / SECOND_IN_HOUR));
                    } else {
                        event.setAccumulatedVehicleMiles(null);
                        event.setEngineHours(null);
                    }

                    break;
                }
            }
            case IntermediateLog:
            case ChangeInDriversIndication:
                distanceAndHours = GetEngineHoursAndAccumulatedVehicleMiles(utcTimeStamp);
                boolean isPersonalConveyance = GlobalState.getInstance().getIsInPersonalConveyanceDutyStatus();
                if (distanceAndHours == null || isPersonalConveyance) {
                    event.setAccumulatedVehicleMiles(null);
                    event.setEngineHours(null);
                } else {
                    event.setAccumulatedVehicleMiles(distanceAndHours.getAccumulatedVehicleMiles());
                    event.setEngineHours(distanceAndHours.getEngineHours());
                }
                break;
            case LoginLogout:
            case EnginePowerUpPowerDown:
                distanceAndHours = GetEngineHoursAndAccumulatedVehicleMiles(utcTimeStamp);
                if (distanceAndHours != null) {
                    event.setAccumulatedVehicleMiles(distanceAndHours.getAccumulatedVehicleMiles());
                }
                event.setEngineHours(GetCurrentTotalEngineHours());

                break;
            case Malfunction_DataDiagnosticDetection:
                event.setAccumulatedVehicleMiles(null);
                event.setEngineHours(GetCurrentTotalEngineHours());
                break;
            default:
                break;
        }
    }

    private void SetTractorNumberTrailerNumberShipmentInfo(EmployeeLogEldEvent logEldEvent, String tractorNumber, String trailerNumber, String shipmentInfo) {
        logEldEvent.setTractorNumber(tractorNumber);
        logEldEvent.setTrailerNumber(trailerNumber);
        logEldEvent.setShipmentInfo(shipmentInfo);
    }

    @Override
    public int getEngineSecondsOffset() {
        StatusBuffer statusBuffer = eobrReader.GetStatusBuffer().getData();

        // for heavy truck bus types, returns the difference between engine on time and runtime
        // this value is later used to determine a point in time total engine hours
        if(statusBuffer != null) {
            int engineOnTime = statusBuffer.getEngineOnTimeSeconds();
            if (engineOnTime > 0) {
                return engineOnTime - statusBuffer.getRunTimeSeconds();
            }
        }
        return 0;
    }

    @Override
    public boolean IsDuplicateEnginePowerUpOrShutDownUnassignedEvent(EventRecord eventRecord, Enums.CompositeEmployeeLogEldEventTypeEventCodeEnum eventTypeAndCode) {
        EmployeeLogEldEventCode eventCode = eventTypeAndCode == Enums.CompositeEmployeeLogEldEventTypeEventCodeEnum.EnginePowerUpEvent ?
                new EmployeeLogEldEventCode(EmployeeLogEldEventCode.PowerUp_ConventionalLocationPrecision) : new EmployeeLogEldEventCode(EmployeeLogEldEventCode.PowerDown_ConventionalLocationPrecision);
        EmployeeLogEldEvent existingEvent = eventFacade.FetchActiveByNaturalKey(-1, Enums.EmployeeLogEldEventType.EnginePowerUpPowerDown, eventCode, eventRecord.getTimecodeAsDate());
        return existingEvent != null && existingEvent.getEobrSerialNumber().equalsIgnoreCase(GlobalState.getInstance().getCurrentEobrSerialNumber());
    }

    @Override
    public void CreateEnginePowerUpOrShutDownUnassignedEvent(EventRecord eventRecord, Enums.CompositeEmployeeLogEldEventTypeEventCodeEnum eventTypeAndCode, int engineSecondsOffset) throws Throwable {
        StatusRecord statusRecord = eventRecord.getStatusRecordData();
        int eventTypeCode = eventTypeAndCode == Enums.CompositeEmployeeLogEldEventTypeEventCodeEnum.EnginePowerUpEvent ?
                EmployeeLogEldEventCode.PowerUp_ConventionalLocationPrecision : EmployeeLogEldEventCode.PowerDown_ConventionalLocationPrecision;

        EmployeeLogEldEvent eventToSave = new EmployeeLogEldEvent();
        // populate from TRIP INFO, which has already been called
        eventToSave.setLatitude((double) eventRecord.getTripReportData().getLatitude());
        eventToSave.setLongitude((double) eventRecord.getTripReportData().getLongitude());
        eventToSave.setDistanceSinceLastCoordinates(EventTranslationBase.calculateDistanceSinceLastValidCoordinates(eventRecord.getTripReportData().getFixUncert()));
        eventToSave.setOdometer(eventRecord.getTripReportData().getOdometer());
        eventToSave.setEngineHours(GetTotalEngineHoursUsingOffset(eventRecord, engineSecondsOffset));

        PopulateEngineOnOrOffEvent(null, eventToSave, eventTypeCode, eventRecord, statusRecord);

        String eobrSerialNumber = EobrReader.getInstance().getEobrSerialNumber();
        eventToSave.setEobrSerialNumber(eobrSerialNumber);

        // reverse geocode using local db
        if (statusRecord != null && statusRecord.IsGpsLocationValid()) {

            // reverse geocode using local db
            GpsLocation gpsLocation = new GpsLocation(statusRecord.getGpsTimestampUtc(), statusRecord.getGpsLatitude(), statusRecord.getGpsLongitude());
            IAPIController empLogController = MandateObjectFactory.getInstance(this.getContext(), GlobalState.getInstance().getFeatureService()).getCurrentEventController();
            empLogController.ReverseGeocodeLocation(Collections.singletonList(gpsLocation));
            if (gpsLocation != null) {
                eventToSave.setGeolocation(gpsLocation.ToLocationString());
            }
        }

        eventToSave.setDriverOriginatorUserId(null);
        eventToSave.setTractorNumber(GlobalState.getInstance().get_currentTractorNumbers());
        eventToSave.setVehiclePlate(null);
        eventToSave.setTrailerNumber(null);
        eventToSave.setTrailerPlate(null);
        eventToSave.setShipmentInfo(null);
        eventToSave.setUnidentifiedEventStatus(UnidentifiedEldEventStatus.LOCAL);

        SaveEvent(null, eventToSave, statusRecord, "EmployeeLogEldMandateController.CreateEnginePowerUpShutdownEvent");
    }

    public void CreateEnginePowerUpOrShutDownEvent(EmployeeLog employeeLog, EventRecord eventRecord, Enums.CompositeEmployeeLogEldEventTypeEventCodeEnum eventTypeAndCode) throws Throwable {
        Date logDate = eventRecord.getTimecodeAsDate();
        EmployeeLog empLog = employeeLog;
        boolean useGlobalStateLog = empLog == null || !empLog.isPrimaryKeySet();
        if (useGlobalStateLog) {
            empLog = new LogEntryController(GlobalState.getInstance()).getCurrentDriversLog();
        }

        StatusRecord statusRecord = eventRecord.getStatusRecordData();
        if (statusRecord == null) {
            // Get status record based on the event time as a fallback if it isn't available directly on the event record
            statusRecord = getStatusRecord(eventRecord.getTimecodeAsDate());
        }

        EmployeeLogEldEvent eventToSave = new EmployeeLogEldEvent(logDate);
        int eldEventCode;

        boolean isInPersonalConveyance = GlobalState.getInstance().getIsInPersonalConveyanceDutyStatus();
        if (isInPersonalConveyance) {
            eldEventCode = eventTypeAndCode == Enums.CompositeEmployeeLogEldEventTypeEventCodeEnum.EnginePowerUpEvent ?
                    EmployeeLogEldEventCode.PowerUp_ReducedLocationPrecision : EmployeeLogEldEventCode.PowerDown_ReducedLocationPrecision;
        } else {
            eldEventCode = eventTypeAndCode == Enums.CompositeEmployeeLogEldEventTypeEventCodeEnum.EnginePowerUpEvent ?
                    EmployeeLogEldEventCode.PowerUp_ConventionalLocationPrecision : EmployeeLogEldEventCode.PowerDown_ConventionalLocationPrecision;
        }
        SetReducedPrecisionGPSInfo(eventToSave, statusRecord, isInPersonalConveyance);

        PopulateEngineOnOrOffEvent(empLog, eventToSave, eldEventCode);
        this.SetTractorNumberTrailerNumberShipmentInfo(eventToSave, empLog.getTractorNumbers(), empLog.getTrailerNumbers(), empLog.getShipmentInformation());

        // when i'm connected i want the current event
        this.SetEngineHoursAndAccumulatedVehicleMiles(eventToSave, eventRecord.getTimecodeAsDate(), statusRecord);

        //We calculate and set the distance since last valid coordinates
        eventToSave.setDistanceSinceLastCoordinates(EventTranslationBase.calculateDistanceSinceLastValidCoordinates(statusRecord.getGpsUncertDistance()));

        // reverse geocode using local db
        if (statusRecord != null && statusRecord.IsGpsLocationValid()) {
            // reverse geocode using local db
            GpsLocation gpsLocation = new GpsLocation(statusRecord.getGpsTimestampUtc(), statusRecord.getGpsLatitude(), statusRecord.getGpsLongitude());
            IAPIController empLogController = MandateObjectFactory.getInstance(this.getContext(), GlobalState.getInstance().getFeatureService()).getCurrentEventController();
            empLogController.ReverseGeocodeLocation(Collections.singletonList(gpsLocation));
            eventToSave.setGeolocation(gpsLocation.ToLocationString());
        }
        SaveEvent(empLog, eventToSave, statusRecord, "EmployeeLogEldMandateController.CreateEnginePowerUpOrShutDownEvent");
    }

    @Override
    public EmployeeLogEldEvent CreateDriveOnOrOffUnassignedEvent(VehicleLocation location, Enums.CompositeEmployeeLogEldEventTypeEventCodeEnum eventTypeAndCode) {
        int eventTypeCode = eventTypeAndCode == Enums.CompositeEmployeeLogEldEventTypeEventCodeEnum.DrivingEvent ? EmployeeLogEldEventCode.DutyStatus_Driving : EmployeeLogEldEventCode.DutyStatus_OnDuty;

        EmployeeLogEldEvent eventToSave = new EmployeeLogEldEvent();
        PopulateDriveOnOrOffEvent(null, eventToSave, eventTypeCode, location);
        this.SetEngineHoursAndAccumulatedVehicleMiles(eventToSave, location.getGpsFix().getTimecodeAsDate(), null);
        eventToSave.setOdometer(location.getOdometer());

        eventToSave.setDriverOriginatorUserId(null);
        eventToSave.setTractorNumber(GlobalState.getInstance().get_currentTractorNumbers());
        eventToSave.setVehiclePlate(null);
        eventToSave.setTrailerNumber(null);
        eventToSave.setTrailerPlate(null);
        eventToSave.setShipmentInfo(null);
        eventToSave.setUnidentifiedEventStatus(UnidentifiedEldEventStatus.LOCAL);

        //SaveEvent(null, eventToSave, "EmployeeLogEldMandateController.CreateDriveOnOrOffUnassignedEvent");
        return eventToSave;
    }

    @Override
    public void SaveDriveOnOrOffUnassignedEvent(EmployeeLogEldEvent event) throws Throwable {
        if (event != null)
            SaveEvent(null, event, null, "EmployeeLogEldMandateController.SaveDriveOnOffUnassignedEvent");

    }


    @Override
    public void CreateIntermediateEvent(Enums.CompositeEmployeeLogEldEventTypeEventCodeEnum eventType, TripReport tripData) throws Throwable {
        // An Intermediate Event will be created if,
        // Not an unassigned period
        // The minute = 0 (indicates that it is on the hour)
        // The vehicle is in motion
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(tripData.getDataTimecodeAsDate());
        int minutes = calendar.get(Calendar.MINUTE);

        EmployeeLog empLog = new LogEntryController(GlobalState.getInstance()).getCurrentDriversLog();
        boolean isPersonalConveyance = GlobalState.getInstance().getIsInPersonalConveyanceDutyStatus();

        if (tripData.getDriverId() > 0 && minutes == 0 && GlobalState.getInstance().getPotentialDrivingStopTimestamp() == null) {
            EmployeeLogEldEvent eventToSave = new EmployeeLogEldEvent();

            StatusRecord statusRecord = getStatusRecord(tripData.getTimecodeAsDate());

            switch (eventType) {
                case IntermediateLog:
                    if (statusRecord != null) {
                        this.SetEngineHoursAndAccumulatedVehicleMiles(eventToSave, statusRecord.getTimestampUtc(), statusRecord);
                    } else {
                        // Add eld malfunction for this event
                        Log.e(LOG_TAG, "The Eobr Device is not connected");
                    }
                    PopulateIntermediateEvent(empLog, eventToSave, isPersonalConveyance ?  //if is Personal Conveyance the precision of GPS is reduced
                            EmployeeLogEldEventCode.IntermediateLog_ReducedLocationPrecision : EmployeeLogEldEventCode.IntermediateLog_ConventionalLocationPrecision, tripData);
                    if (statusRecord != null) {
                        // set the distance since last valid coordinates
                        eventToSave.setDistanceSinceLastCoordinates(EventTranslationBase.calculateDistanceSinceLastValidCoordinates(statusRecord.getGpsUncertDistance()));
                    }

                    SetReducedPrecisionGPSInfo(eventToSave, statusRecord, GlobalState.getInstance().getIsInPersonalConveyanceDutyStatus());

                    break;
                default:
                    throw new Exception("Invalid CompositeEmployeeLogEldEventTypeEventCodeEnum");
            }
            setEventGeolocation(null, eventToSave);

            if (GlobalState.getInstance().getCompanyConfigSettings(getContext()).getIsGeotabEnabled()) {
                // Check if there is already an event in the table
                EmployeeLogEldEventFacade employeeLogEldEventFacade = new EmployeeLogEldEventFacade(getContext(), globalState.getCurrentUser());
                EmployeeLogEldEvent previousIntermediateEvent = employeeLogEldEventFacade.FetchLastByEventType(Enums.EmployeeLogEldEventType.IntermediateLog.getValue());
                if (previousIntermediateEvent == null) {
                    //If there isn't an intermediate event, insert one
                    SaveEvent(empLog, eventToSave, statusRecord, "EmployeeLogEldMandateController.CreateIntermediateEvent");
                } else if (!DateUtility.IsSameDayWithoutSeconds(previousIntermediateEvent.getEventDateTime(), eventToSave.getEventDateTime())) {
                    //If new event date is NOT the same as the previous intermediate event, insert another intermediate event
                    SaveEvent(empLog, eventToSave, statusRecord, "EmployeeLogEldMandateController.CreateIntermediateEvent");
                }
            } else {
                SaveEvent(empLog, eventToSave, statusRecord, "EmployeeLogEldMandateController.CreateIntermediateEvent");
            }
        }
    }

    @Override
    public EmployeeLogEldEvent GetMostRecentLoginEvent(EmployeeLog empLog) throws Exception {
        //Need to use the user id that is present on the log we're trying to update
        String userEmployeeId;
        //Use empLog employee Id, or default to global state if null
        if (empLog == null)
            userEmployeeId = globalState.getCurrentUser().getCredentials().getEmployeeId();
        else
            userEmployeeId = empLog.getEmployeeId();
        List<EmployeeLogEldEvent> empLogEldEvents = eventFacade.GetLoginDutyStatusEvents(userEmployeeId);
        EmployeeLogEldEvent logEldEvent = null;
        if (empLogEldEvents.size() > 0)
            logEldEvent = empLogEldEvents.get(0);
        //We found a DB entry, let's see if a copy of said event exists
        if (empLog != null && empLog.getEldEventList() != null) {
            ArrayList<EmployeeLogEldEvent> sortedComparisonList = new ArrayList<>(Arrays.asList(empLog.getEldEventList().getEldEventList()));
            int loginEventOnLogIndex;
            boolean exists = (loginEventOnLogIndex = Collections.binarySearch(sortedComparisonList, logEldEvent, new GenericEventComparer())) >= 0;
            if (logEldEvent == null) {
                //We have already associated the Login event with the log, but need to further update it
                //Return the in memory version
                for (EmployeeLogEldEvent evt : empLog.getEldEventList().getEldEventList(EldEventAdapterList.ListAccessorModifierEnum.NonDutyStatus)) {
                    if (evt.getEventType() == Enums.EmployeeLogEldEventType.LoginLogout) {
                        logEldEvent = evt;
                        break;
                    }
                }
            } else if (exists)
                logEldEvent = empLog.getEldEventList().getEldEventList()[loginEventOnLogIndex];
        }
        if (logEldEvent == null) {
            String noLoginEvent = "Unable to retrieve login event in database or in memory.";
            Log.d("GetMostRecentLoginEvent", noLoginEvent);
            ErrorLogHelper.RecordMessage(this.getContext(), noLoginEvent);
        }

        return logEldEvent;
    }

    public void createMalfunctionForLoggedInUsers(Date timestamp, Malfunction malfunction) throws Throwable {
        List<User> users = GlobalState.getInstance().getLoggedInUserList();
        for (User loggedInUser : users) {
            EmployeeLog log = null;

            if (GlobalState.getInstance().getCurrentDesignatedDriver() == loggedInUser) {
                log = GlobalState.getInstance().getCurrentDriversLog();
            } else if (GlobalState.getInstance().getCurrentUser() == loggedInUser) {
                log = GlobalState.getInstance().getCurrentEmployeeLog();
            }

            if (log == null)
                log = this.GetLocalEmployeeLog(loggedInUser, timestamp);

            CreateMalfunctionEvent(log, timestamp, malfunction);
        }
    }

    public void clearMalfunctionForLoggedInUsers(Date timestamp, Malfunction malfunction) throws Throwable {
        List<User> users = GlobalState.getInstance().getLoggedInUserList();
        for (User loggedInUser : users) {
            EmployeeLog log = null;
            if (GlobalState.getInstance().getCurrentDesignatedDriver() == loggedInUser) {
                log = GlobalState.getInstance().getCurrentDriversLog();
            } else if (GlobalState.getInstance().getCurrentUser() == loggedInUser) {
                log = GlobalState.getInstance().getCurrentEmployeeLog();
            }

            if (log == null)
                log = this.GetLocalEmployeeLog(loggedInUser, timestamp);

            CreateMalfunctionClearedEvent(log, timestamp, malfunction);
        }
    }

    public void clearDataDiagnosticForLoggedInUsers(Date timestamp) throws Throwable {
        List<User> users = GlobalState.getInstance().getLoggedInUserList();
        for (User loggedInUser : users) {
            EmployeeLog log = this.GetLocalEmployeeLog(loggedInUser, timestamp);
            if (IsDataDiagnosticActive(log)) {
                CreateDataDiagnosticClearedEvent(log, TimeKeeper.getInstance().getCurrentDateTime().toDate(), DataDiagnosticEnum.ENGINE_SYNCHRONIZATION, 0);
            }
        }
    }

    private void CreateMalfunctionEvent(EmployeeLog employeeLog, Date timestamp, Malfunction malfunction) throws Throwable {
        if (!isMalfunctioning(employeeLog, malfunction)) {
            createMalfunctionOrDataDiagnosticEvent(employeeLog, timestamp, EmployeeLogEldEventCode.EldMalfunctionLogged, malfunction.getDmoValue(), 0);
            EventBus.getDefault().post(malfunction);
        }
    }

    private void CreateMalfunctionClearedEvent(EmployeeLog employeeLog, Date timestamp, Malfunction malfunction) throws Throwable {
        if (isMalfunctioning(employeeLog, malfunction)) {
            createMalfunctionOrDataDiagnosticEvent(employeeLog, timestamp, EmployeeLogEldEventCode.EldMalfunctionCleared, malfunction.getDmoValue(), 0);
            EventBus.getDefault().post(malfunction);
        }
    }

    public void CreateMalfunctionELDEvent(EmployeeLog employeeLog, Date timestamp, Malfunction malfunction) throws Throwable {
        createMalfunctionOrDataDiagnosticEvent(employeeLog, timestamp, EmployeeLogEldEventCode.EldMalfunctionLogged, malfunction.getDmoValue(), 0);
        EventBus.getDefault().post(malfunction);
    }

    public void CreateMalfunctionELDClearedEvent(EmployeeLog employeeLog, Date timestamp, Malfunction malfunction) throws Throwable {
        createMalfunctionOrDataDiagnosticEvent(employeeLog, timestamp, EmployeeLogEldEventCode.EldMalfunctionCleared, malfunction.getDmoValue(), 0);
        EventBus.getDefault().post(malfunction);
    }

    public void CreateDataDiagnosticEvent(EmployeeLog employeeLog, Date timestamp, DataDiagnosticEnum dataDiagnostic, long relatedEventPrimaryKey) throws Throwable {
        createMalfunctionOrDataDiagnosticEvent(employeeLog, timestamp, EmployeeLogEldEventCode.EldDataDiagnosticLogged, dataDiagnostic.toDMOEnum(), relatedEventPrimaryKey);
        EventBus.getDefault().post(dataDiagnostic);
    }

    public void CreateDataDiagnosticClearedEvent(EmployeeLog employeeLog, Date timestamp, DataDiagnosticEnum dataDiagnostic, long relatedEventPrimaryKey) throws Throwable {
        createMalfunctionOrDataDiagnosticEvent(employeeLog, timestamp, EmployeeLogEldEventCode.EldDataDiagnosticCleared, dataDiagnostic.toDMOEnum(), relatedEventPrimaryKey);
        EventBus.getDefault().post(dataDiagnostic);
    }

    private void createMalfunctionOrDataDiagnosticEvent(EmployeeLog employeeLog, Date timestamp, int eventCode, String malfunctionOrDiagnosticCode, long relatedEventPrimaryKey) throws Throwable {
        if(employeeLog == null || !employeeLog.getIsExemptFromELDUse()) {
            // 4.5.1.7. Event: ELD Malfunction and Data Diagnostics Occurrence
            EmployeeLogEldEvent eventToSave = new EmployeeLogEldEvent();
            StatusRecord statusRecord = getLatestStatusRecord(false);

            Integer logKey = (employeeLog == null) ? null : Integer.valueOf((int) employeeLog.getPrimaryKey());
            eventToSave.setInternalObjectValueMap(constructMandateValuePayload(Enums.EmployeeLogEldEventType.Malfunction_DataDiagnosticDetection.getValue(), eventCode, null, statusRecord, employeeLog, logKey, timestamp));
            eventToSave.hydrate(true);
            if (employeeLog != null) {
                SetTractorNumberTrailerNumberShipmentInfo(eventToSave, employeeLog.getTractorNumbers(), employeeLog.getTrailerNumbers(), employeeLog.getShipmentInformation());
            }
            eventToSave.setEobrSerialNumber(this.eobrReader.getEobrSerialNumber());
            eventToSave.setDiagnosticCode(malfunctionOrDiagnosticCode);
            if (eventCode == EmployeeLogEldEventCode.EldMalfunctionLogged) {
                eventToSave.setEldMalfunctionIndicatorStatus(true);
            } else if (eventCode == EmployeeLogEldEventCode.EldDataDiagnosticLogged || eventCode == EmployeeLogEldEventCode.EldDataDiagnosticCleared) {
                eventToSave.setRelatedKmbPK(relatedEventPrimaryKey);
            }

            SetEngineHoursAndAccumulatedVehicleMiles(eventToSave, timestamp, statusRecord);

            SaveEvent(employeeLog, eventToSave, statusRecord, "EmployeeLogEldMandateController.createMalfunctionOrDataDiagnosticEvent");
        }
    }

    /**
     * Attempts to save an event. If saving fails, an error message is logged
     * with the given error logging tag and the exception is thrown.
     *
     * @param event           The ELD event to save
     * @param errorLoggingTag A tag to use when logging failure messages in the event of an error
     * @throws Throwable The exception thrown when trying to save
     */
    private void SaveEvent(EmployeeLog log, EmployeeLogEldEvent event, StatusRecord statusRecord, String errorLoggingTag) throws Throwable {
        try {

            if (log != null && log.getIsExemptFromELDUse()) {
                event.setEventComment(getContext().getString(R.string.exemptFromEldUse));
                event.setEventRecordStatus(Enums.EmployeeLogEldEventRecordStatus.InactiveChanged.getValue());
            }

            // Update lat/log status code if position compliance malfunction or location uncertainty
            checkForPositionComplianceMalfunction(log, event, statusRecord);

            //If we don't have a log, the event is of type PowerUp/PowerDown or Login/Logout, and can
            //exist independently of a log
            if (log == null) {
                eventFacade.Save(event);
            } else {
                //Otherwise our "Save" will occur as a function of the event being present on a log
                //when the log itself is persisted
                AddEventToLog(log, event);

                SaveLocalEmployeeLog(log);
                if (log.getEmployeeId() != null) {
                    if (log.getEmployeeId().equals(GlobalState.getInstance().getCurrentUser().getCredentials().getEmployeeId())) {
                        GlobalState.getInstance().setCurrentEmployeeLog(log);
                    }
                    if (GlobalState.getInstance().getCurrentDesignatedDriver() != null && log.getEmployeeId().equals(GlobalState.getInstance().getCurrentDesignatedDriver().getCredentials().getEmployeeId())) {
                        GlobalState.getInstance().setCurrentDriversLog(log);
                    }
                }
            }

            CheckEventForMalfunction(log, false, event, statusRecord);
        } catch (Exception e) {
            String msg = String.format("%s failure to save event: {%s}  EmployeeLogEldEvent: {%s}", errorLoggingTag, e.getMessage(), event.toLogString());
            Log.d(errorLoggingTag, msg);
            ErrorLogHelper.RecordMessage(this.getContext(), msg);
            throw e;
        }
    }

    private boolean CheckEventForMalfunction(EmployeeLog log, boolean isEditedEvent, EmployeeLogEldEvent event, StatusRecord statusRecord) throws Throwable {
        DataDiagnosticEnum errorFound = _dataDiagnosticChecker.CheckForError(event, isEditedEvent, statusRecord);
        if (errorFound != null && errorFound != DataDiagnosticEnum.NONE) {
            Log.d("EldMandateController", String.format("Creating %s event for EventType %d EventCode %d at %s",
                    getContext().getString(errorFound.getDescriptionKey()), event.getEventType().getValue(), event.getEventCode(), event.getEventDateTime()));

            event.setDriverDataDiagnosticEventIndicatorStatus(true);
            long foreignKey = 0;
            if (errorFound == DataDiagnosticEnum.MISSING_REQUIRED_DATA_ELEMENTS) {
                foreignKey = event.getPrimaryKey();
            }
            CreateDataDiagnosticEvent(log, DateUtility.getCurrentDateTimeWithSecondsUTC(), errorFound, foreignKey);

            return true;
        }

        return false;
    }

    private void checkForPositionComplianceMalfunction(EmployeeLog log, EmployeeLogEldEvent event, StatusRecord statusRecord) {
        if (isMalfunctioning(log, Malfunction.POSITIONING_COMPLIANCE)) {
            event.setLatitudeStatusCode(POSITION_MALFUNCTION_STATUS_CODE);
            event.setLongitudeStatusCode(POSITION_MALFUNCTION_STATUS_CODE);

        } else if (statusRecord != null && !isGPSValid(statusRecord) && event.getDriversLocationDescription().length() == 0) {
            event.setLatitudeStatusCode(POSITION_MALFUNCTION_WARNING_STATUS_CODE);
            event.setLongitudeStatusCode(POSITION_MALFUNCTION_WARNING_STATUS_CODE);
        }
    }

    public boolean isMalfunctioning(EmployeeLog employeeLog, Malfunction malfunction) {
        return getActiveMalfunctions(employeeLog).contains(malfunction);
    }

    public Collection<Malfunction> getActiveMalfunctions(EmployeeLog employeeLog) {
        HashSet<Malfunction> malfunctionSet = new HashSet<>();

        int key = employeeLog == null ? -1 : (int) employeeLog.getPrimaryKey();
        final EmployeeLogEldEventFacade facade = new EmployeeLogEldEventFacade(getContext(), getCurrentUser());

        EmployeeLogEldEvent[] logEldEventArray = facade.GetByEventTypes(key,
                Arrays.asList(new Integer[]{Enums.EmployeeLogEldEventType.Malfunction_DataDiagnosticDetection.getValue()}),
                Arrays.asList(new Integer[]{Enums.EmployeeLogEldEventRecordStatus.Active.getValue()})
        );

        //checking for active malfunctions needs the events in chronological order
        //since it wouldn't work right if we process a clear prior to a set
        Arrays.sort(logEldEventArray, new EmployeeLogEldEventDateComparator());

        if (logEldEventArray.length > 0) {
            //Remove inspection to show this has to go from the start index
            //noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < logEldEventArray.length; i++) {
                Malfunction malfunction = null;
                try {
                    malfunction = Malfunction.valueOfDMOEnum(logEldEventArray[i].getDiagnosticCode());
                } catch (IllegalArgumentException e) {
                    //Just log it.  It's due to it being a DataDiagnostic thing.
                    Log.v("EldMandateController", "MalfunctionException DiagnosticCode: " + logEldEventArray[i].getDiagnosticCode());
                }

                if (malfunction == null) {
                    continue;
                }

                if (EmployeeLogEldEventCode.EldMalfunctionLogged == logEldEventArray[i].getEventCode()) {
                    malfunctionSet.add(malfunction);
                } else if (EmployeeLogEldEventCode.EldMalfunctionCleared == logEldEventArray[i].getEventCode()) {
                    malfunctionSet.remove(malfunction);
                }
            }
        }
        return malfunctionSet;
    }

    public Collection<DataDiagnosticEnum> getActiveDataDiagnostics(EmployeeLog employeeLog) {
        HashSet<DataDiagnosticEnum> dataDiagnosticSet = new HashSet<>();

        HashSet<DataDiagnosticEnum> dataDiagnosticIdSet = new HashSet<>();

        int key = employeeLog == null ? -1 : (int) employeeLog.getPrimaryKey();
        final EmployeeLogEldEventFacade facade = new EmployeeLogEldEventFacade(getContext(), getCurrentUser());

        EmployeeLogEldEvent[] logEldEventArray = facade.GetByEventTypes(key,
                Arrays.asList(new Integer[]{Enums.EmployeeLogEldEventType.Malfunction_DataDiagnosticDetection.getValue()}),
                Arrays.asList(new Integer[]{Enums.EmployeeLogEldEventRecordStatus.Active.getValue()})
        );

        if (logEldEventArray.length > 0) {
            //Remove inspection to show this has to go from the start index
            //noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < logEldEventArray.length; i++) {
                if (! logEldEventArray[i].isDataDiagnosticEvent()){
                    continue;
                }

                DataDiagnosticEnum dataDiagnosticEnum = DataDiagnosticEnum.getByDmoValue(logEldEventArray[i].getDiagnosticCode());

                if (EmployeeLogEldEventCode.EldDataDiagnosticLogged == logEldEventArray[i].getEventCode() && dataDiagnosticEnum != DataDiagnosticEnum.UNIDENTIFIED_DRIVING_RECORDS) {
                    dataDiagnosticIdSet.add(dataDiagnosticEnum);
                } else if (EmployeeLogEldEventCode.EldDataDiagnosticCleared == logEldEventArray[i].getEventCode()) {
                    dataDiagnosticIdSet.remove(dataDiagnosticEnum);
                }
            }
        }

        for(DataDiagnosticEnum diagnosticPair : dataDiagnosticIdSet){
            dataDiagnosticSet.add(diagnosticPair);
        }

        // Display the Unidentified Driving Records Data Diagnostic if it is active for the Eobr Device(mandate 4.6.1.6 (c)).
        // Need currentlog to determine the correct date/time range to check for active diagnostic event. However, this method may fire before getting
        // to the RODS screen and cause an exception. Currentlog has to be present when we get to Rods screen (where we see active diagnostic errors), so skip
        // if currentlog is null.
        EmployeeLog currentLog = GlobalState.getInstance().getCurrentDriversLog();

        if (eobrReader != null && eobrReader.getIsEobrDevicePhysicallyConnected() && eobrReader.getEobrSerialNumber() != null && currentLog != null) {
            User currentUser = GlobalState.getInstance().getCurrentUser();
            String dailyLogStartTime = GlobalState.getInstance().getCompanyConfigSettings(this.getContext()).getDailyLogStartTime();
            Date fromDate = EmployeeLogUtilities.CalculateLogStartTime(dailyLogStartTime, currentLog.getLogDate(), currentUser.getHomeTerminalTimeZone());
            Boolean isActiveUnidentifiedDrivingDataDiagnosticEvent = facade.isUnidentifiedDrivingDiagnosticEventByELDActive(eobrReader.getEobrSerialNumber(), DateUtility.AddDays(fromDate, -7));

            if (isActiveUnidentifiedDrivingDataDiagnosticEvent) {
                dataDiagnosticSet.add(DataDiagnosticEnum.UNIDENTIFIED_DRIVING_RECORDS);
            }
        }

        return dataDiagnosticSet;
    }

    public String getTrailerNumber(EmployeeLogEldEvent lastEldEvent) {
        if(lastEldEvent != null) {
            if (lastEldEvent.getTrailerNumber() != null
                    && !lastEldEvent.getTrailerNumber().isEmpty()) {
                return lastEldEvent.getTrailerNumber();
            }
        }
        return "";
    }

    public boolean isMalfunctioning(int key) {
        // if not connected to eld, short circuit
        if (eobrReader.getIsEobrDevicePhysicallyConnected() == false) {
            return false;
        }

        final EmployeeLogEldEventFacade facade = new EmployeeLogEldEventFacade(getContext(), getCurrentUser());

        EmployeeLogEldEvent[] logEldEventArray = facade.GetByEventTypes(key,
                Arrays.asList(new Integer[]{Enums.EmployeeLogEldEventType.Malfunction_DataDiagnosticDetection.getValue()}),
                Arrays.asList(new Integer[]{Enums.EmployeeLogEldEventRecordStatus.Active.getValue()})
        );

        // account for the eobr
        String eobrSerialNumber = eobrReader.getEobrSerialNumber();

        if (logEldEventArray.length > 0) {
            for (int i = logEldEventArray.length - 1; i >= 0; i--) {
                EmployeeLogEldEvent eldEvent = logEldEventArray[i];
                if (eobrSerialNumber == null || eldEvent.getEobrSerialNumber() == null || eldEvent.getEobrSerialNumber().compareToIgnoreCase(eobrSerialNumber) == 0) {
                    if (Malfunction.ENGINE_SYNCHRONIZATION_COMPLIANCE.getDmoValue().equals(eldEvent.getDiagnosticCode())) {
                        if (EmployeeLogEldEventCode.EldMalfunctionLogged == eldEvent.getEventCode()) {
                            return true;
                        } else if (EmployeeLogEldEventCode.EldMalfunctionCleared == eldEvent.getEventCode()) {
                            return false;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean IsDataDiagnosticActive(EmployeeLog employeeLog) throws Throwable {
        int key = employeeLog == null ? -1 : (int) employeeLog.getPrimaryKey();
        final EmployeeLogEldEventFacade facade = new EmployeeLogEldEventFacade(getContext(), getCurrentUser());

        EmployeeLogEldEvent[] logEldEventArray = facade.GetByEventTypes(key,
                Arrays.asList(new Integer[]{Enums.EmployeeLogEldEventType.Malfunction_DataDiagnosticDetection.getValue()}),
                Arrays.asList(new Integer[]{Enums.EmployeeLogEldEventRecordStatus.Active.getValue()})
        );

        if (logEldEventArray.length > 0) {
            for (int i = logEldEventArray.length - 1; i >= 0; i--) {
                EmployeeLogEldEvent eldEvent = logEldEventArray[i];
                if (DataDiagnosticEnum.ENGINE_SYNCHRONIZATION.toDMOEnum().equals(eldEvent.getDiagnosticCode())) {
                    if (EmployeeLogEldEventCode.EldDataDiagnosticLogged == eldEvent.getEventCode()) {
                        return true;
                    } else if (EmployeeLogEldEventCode.EldDataDiagnosticCleared == eldEvent.getEventCode()) {
                        return false;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Returns true if a data diagnostic is currently active for the current user and false otherwise
     *
     * @param dataDiagnostic The data diagnostic to check
     * @return true if a data diagnostic is currently active and false otherwise
     */
    public boolean IsDataDiagnosticActive(DataDiagnosticEnum dataDiagnostic) {
        return IsDataDiagnosticActive(dataDiagnostic, DateUtility.getCurrentDateTimeWithSecondsUTC());
    }

    /**
     * Returns true if a data diagnostic was active for the current user before a timestamp and false otherwise.
     *
     * @param dataDiagnostic  The data diagnostic to check
     * @param beforeTimestamp The exclusive timestamp to search before
     * @return true if the data diagnostic was active and false otherwise
     */
    public boolean IsDataDiagnosticActive(DataDiagnosticEnum dataDiagnostic, Date beforeTimestamp) {
        List<EmployeeLogEldEvent> dataDiagnosticEvents = eventFacade.GetActiveByUserAndDiagnosticCode(dataDiagnostic.toDMOEnum(), beforeTimestamp);
        if (!dataDiagnosticEvents.isEmpty()) {
            EmployeeLogEldEvent lastEvent = dataDiagnosticEvents.get(dataDiagnosticEvents.size() - 1);
            return lastEvent.getEventCode() == EmployeeLogEldEventCode.EldDataDiagnosticLogged;
        }
        return false;
    }

    /**
     * Determine if the Edit will overlap automatically recorded driving time
     */
    public enum InvalidateAutomaticDriveTimeEnum {
        NO_OVERLAP,
        OVERLAP_SAME_SERIALNUMBER,
        OVERLAP_DIFFERENT_SERIALNUMBER
    }

    public InvalidateAutomaticDriveTimeEnum willEditInvalidateAutomaticDriveTime(EmployeeLogEldEvent[] eldEvents, Long proposedPrimaryKey, Date proposedStartTime, Date proposedEndTime, String proposedEobrSerialNumber) {
        // SPECIAL CASE: You can't overwrite automatically generated driving time.
        List<EmployeeLogEldEvent> eventsAsList = Arrays.asList(eldEvents);
        for (int i = 0; i < eldEvents.length; i++) {
            EmployeeLogEldEvent eldEvent = eldEvents[i];
            EmployeeLogEldEvent nextEvent = EmployeeLogUtilities.getNextActiveEventAfterDate(eventsAsList, eldEvent.getEventDateTime());

            // We should skip the "eldEvent" if it isn't an active, driving duty status change event that is automatically recorded.
            // We also skip it if "eldEvent" is the same key as the one being proposed
            if ((eldEvent.getEventType().getValue() != Enums.EmployeeLogEldEventType.DutyStatusChange.getValue()) ||
                    (eldEvent.getEventRecordStatus() != Enums.EmployeeLogEldEventRecordStatus.Active.getValue())||
                    (eldEvent.getEventRecordOrigin() != Enums.EmployeeLogEldEventRecordOrigin.AutomaticallyRecorded) ||
                    (eldEvent.getEventCode() != EmployeeLogEldEventCode.DutyStatus_Driving) ||
                    (proposedPrimaryKey == eldEvent.getPrimaryKey())) {
                continue;
            }

            // determine end of day for the company
            String dailyLogStart = GlobalState.getInstance().getCompanyConfigSettings(GlobalState.getInstance().getEobrService()).getDailyLogStartTime();
            TimeZoneEnum timeZone = GlobalState.getInstance().getCurrentUser().getHomeTerminalTimeZone();
            Date logEndTime = EmployeeLogUtilities.CalculateLogEndTime(dailyLogStart, eldEvent.getEventDateTime(), timeZone);

            if (proposedStartTime != null && proposedEndTime != null && eldEvent.getEventDateTime() != null) {
                if (proposedStartTime.getTime() < ((nextEvent != null && nextEvent.getEventDateTime() != null) ? nextEvent.getEventDateTime().getTime() : logEndTime.getTime()) //if we dont have a next event, compare against the log end time
                        && proposedEndTime.getTime() > eldEvent.getEventDateTime().getTime()) {
                    if (proposedEobrSerialNumber == null || proposedEobrSerialNumber.equalsIgnoreCase(eldEvent.getEobrSerialNumber())) {
                        return InvalidateAutomaticDriveTimeEnum.OVERLAP_SAME_SERIALNUMBER;
                    }
                    else {
                        return InvalidateAutomaticDriveTimeEnum.OVERLAP_DIFFERENT_SERIALNUMBER;
                    }
                }
            }
        }

        return InvalidateAutomaticDriveTimeEnum.NO_OVERLAP;
    }

    public EmployeeLogEldEvent saveEldEvent(EmployeeLogEldEvent editedEvent, Date editedEventEndTime) throws Throwable {
        return saveEldEvent(editedEvent, Enums.SpecialDrivingCategory.None, editedEventEndTime, Enums.ActionInitiatingSaveEnum.EditLog);
    }

    /**
     * Saves the EldEvent record after being edited by the mobile user. Will inactive the previous record and clone a new record with latest changes.
     * If Start or End dates have changed and overlap existing EldEvents for that day, those records will be either updated or deleted.
     */
    public EmployeeLogEldEvent saveEldEvent(EmployeeLogEldEvent editedEvent, Enums.SpecialDrivingCategory subStatus, Date editedEventEndTime, Enums.ActionInitiatingSaveEnum actionInitiatingSave) throws Throwable {
        try {
            secondaryEventEndOdometer = -1;
            // when editing an Eld Event, make sure it's parent EmployeeLog is a local log (LogSourceStatusEnum=1)
            LocalEditedEldEventLog localLogInfo = getLocalLogForEditedEldEvent(globalState.getCurrentUser(), editedEvent.getEventDateTime(), editedEvent);
            EmployeeLog empLog = localLogInfo.getEmployeeLog();
            editedEvent = localLogInfo.getEditedEldEvent();

            EmployeeLogEldEvent[] eldEventArray = fetchEldEventsByEventTypes(editedEvent.getLogKey(), Arrays.asList(Enums.EmployeeLogEldEventType.DutyStatusChange.getValue(), Enums.EmployeeLogEldEventType.ChangeInDriversIndication.getValue()), Arrays.asList(Enums.EmployeeLogEldEventRecordStatus.Active.getValue()));
            // lists are easier to use than arrays :)
            List<EmployeeLogEldEvent> eldEventList = new ArrayList<>();
            for (int i = 0; i < eldEventArray.length; i++) {
                eldEventList.add(eldEventArray[i]);
            }
            EmployeeLogEldEvent[] persistList;
            EmployeeLogEldEvent origEvent = null;

            boolean isEditingAutomaticDriveTime = false;
            boolean isAutomaticDriveTimeChangingToSpecialCategory = false;

            if (editedEvent.getPrimaryKey() == -1) {  // Adding new ELD Event
                if (editedEventEndTime != null) {
                    // calculate EditDuration (timespan in milliseconds)
                    long duration = Math.abs(editedEvent.getEventDateTime().getTime() - editedEventEndTime.getTime());
                    editedEvent.setEditDuration(duration);
                }

                // insert new record
                editedEvent.setLogKey((int) empLog.getPrimaryKey());
                editedEvent.setIsManuallyEditedByKMBUser(true);

                if (TextUtils.isEmpty(editedEvent.getVehiclePlate())) {
                    editedEvent.setVehiclePlate(empLog.getVehiclePlate());
                }

                if (TextUtils.isEmpty(editedEvent.getTrailerPlate())) {
                    editedEvent.setTrailerPlate(empLog.getTrailerPlate());
                }

                editedEvent.setEventRecordOrigin(Enums.EmployeeLogEldEventRecordOrigin.EditedEnteredByDriver);

                // reconcile events unless event end time is null or the event is unidentified
                if (editedEventEndTime != null && !editedEvent.isUnidentifiedEvent()) {
                    eldEventList = reconcileEvents(editedEvent, editedEvent.getEventDateTime(), editedEventEndTime, eldEventList, isEditingAutomaticDriveTime);
                } else {
                    editedEvent.setUnidentifiedEventStatus(UnidentifiedEldEventStatus.NONE);
                    if (empLog.getIsExemptFromELDUse()) {
                        // if employee log is exempt from eld use
                        editedEvent.setEventRecordStatus(Enums.EmployeeLogEldEventRecordStatus.InactiveChanged.getValue());
                        editedEvent.setEventComment(getContext().getString(R.string.exemptFromEldUse));
                    } else {
                        editedEvent.setEventRecordStatus(Enums.EmployeeLogEldEventRecordStatus.InactiveChangeRequested.getValue());
                    }
                    eldEventList.add(editedEvent);
                }
                //inactive events who's time is overlapped by a change request
                if (editedEventEndTime != null) {
                    eldEventList = eventFacade.handleChangeRequestsThatOverlapActiveEvents(eldEventList, null);
                } else {
                    eldEventList = eventFacade.handleChangeRequestsThatOverlapActiveEvents(eldEventList, editedEvent.getStartTime());
                }

                //if ruleset is changed, convert compatible logs to new ruleset
                eldEventList = changeRulesetOfCompatibleEldEvents(eldEventList, editedEvent.getRuleSet());

                //return list of items which need to be updated in database
                eldEventList = eventFacade.eldEventsToUpdateInDatabase(eldEventList);

            } else {  // Edit existing ELD Event
                origEvent = eventFacade.FetchByKey((int) editedEvent.getPrimaryKey());

                // mark the original event as Inactive - except the case of extending automatic driving period
                if (Enums.EmployeeLogEldEventRecordOrigin.AutomaticallyRecorded != origEvent.getEventRecordOrigin() ||
                        origEvent.isUnidentifiedEvent() /*can claim automatic driving period*/) {
                    editedEvent.setEventRecordOrigin(Enums.EmployeeLogEldEventRecordOrigin.EditedEnteredByDriver);
                    eldEventList = handleEventsWithSameDateTime(eldEventList, origEvent.getEventDateTime());
                } else if ((origEvent.getEventType() == Enums.EmployeeLogEldEventType.DutyStatusChange || origEvent.getEventType() == Enums.EmployeeLogEldEventType.ChangeInDriversIndication) &&
                        origEvent.getEventCode() != EmployeeLogEldEventCode.DutyStatus_Driving) {
                    editedEvent.setEventRecordOrigin(Enums.EmployeeLogEldEventRecordOrigin.EditedEnteredByDriver);
                    eldEventList = handleEventsWithSameDateTime(eldEventList, origEvent.getEventDateTime());
                }

                Date startTime = origEvent.getEventDateTime();

                if (origEvent.getEventRecordOrigin() == Enums.EmployeeLogEldEventRecordOrigin.AutomaticallyRecorded &&
                        origEvent.getEventType() == Enums.EmployeeLogEldEventType.DutyStatusChange &&
                        origEvent.getEventCode() == 3 /* Driving */ &&
                        actionInitiatingSave != Enums.ActionInitiatingSaveEnum.ClaimUnidentifiedEvent) {

                    if (subStatus == null || subStatus == Enums.SpecialDrivingCategory.None) {
                        // SPECIAL CASE: If editing a automatically added driving time, don't inactivate the automatically generated driving time (EventRecordOrigin = 1). These
                        //               two consecutive driving periods need to be maintained separately because one is automatically generated and one is manually entered.
                        ExtendAutomaticallyGeneratedDriveTimeInfo manualDrivingInfo = saveEldEventExtendAutomaticallyGeneratedDriveTime(empLog, origEvent, editedEvent, editedEventEndTime, eldEventList);

                        if (manualDrivingInfo != null) {
                            isEditingAutomaticDriveTime = true;

                            editedEvent = manualDrivingInfo.getEditedEvent();
                            startTime = manualDrivingInfo.getStartTime();
                            editedEventEndTime = manualDrivingInfo.getEndTime();

                            // add the newly created manual driving events to the master list to be committed to the database
                            if (manualDrivingInfo.getNewEvent() != null) {
                                eldEventList.add(manualDrivingInfo.getNewEvent());
                            }
                        }
                    }
                }

                if (!isAutomaticDriveTimeChangingToSpecialCategory) {

                    editedEvent.setPrimaryKey(-1L);    // force insert as a new Active record (crude clone method)
                    editedEvent.setEventSequenceIDNumber(EmployeeLogEldEnum.DEFAULT);   // force new sequence number to be generated in EmployeeLogEldEventPersist
                    editedEvent.setEventRecordStatus(Enums.EmployeeLogEldEventRecordStatus.InactiveChangeRequested.getValue());  // acceptChangeRequest looks at status=changeRequest so temporarily change it here
                    editedEvent.setEncompassClusterPK(0);
                    editedEvent.setLogRemark(null);

                    if (origEvent.isUnidentifiedEvent()) {
                        editedEvent.setEventRecordOrigin(Enums.EmployeeLogEldEventRecordOrigin.AssumedUnidentifiedDriver);
                        editedEvent.setUnidentifiedEventStatus(UnidentifiedEldEventStatus.NONE);
                        editedEvent.setIsReviewed(true);

                        // mark the original event as Inactive
                        origEvent.setEventRecordStatus(Enums.EmployeeLogEldEventRecordStatus.InactiveChanged.getValue());
                        eldEventList.add(origEvent);    // not in the list because originally didn't have parent log defined
                    } else if (Enums.EmployeeLogEldEventType.DutyStatusChange != origEvent.getEventType()) {  // claiming Unidentified PowerUp/PowerDown missing data
                        // mark the original event as Inactive
                        origEvent.setEventRecordStatus(Enums.EmployeeLogEldEventRecordStatus.InactiveChanged.getValue());
                        eldEventList.add(origEvent);    // not in the list because list only shows Duty Status events
                    }

                    editedEvent.setIsManuallyEditedByKMBUser(true);

                    long duration = 0;
                    if (empLog.getIsExemptFromELDUse() && origEvent.isUnidentifiedEvent()) {
                        //if the employee log is exempt from eld use and the original event is unidentified, then we need to set the record to inactive and add a annotation
                        editedEvent.setEventRecordStatus(Enums.EmployeeLogEldEventRecordStatus.InactiveChanged.getValue());
                        editedEvent.setEventComment(getContext().getString(R.string.exemptFromEldUse));
                    }
                    if (editedEventEndTime != null && !origEvent.isUnidentifiedEvent()) {
                        duration = Math.abs(editedEvent.getEventDateTime().getTime() - editedEventEndTime.getTime());
                        editedEvent.setEditDuration(duration);
                        eldEventList = reconcileEvents(editedEvent, startTime, editedEventEndTime, eldEventList, isEditingAutomaticDriveTime);
                    } else {
                        eldEventList.add(editedEvent);
                    }
                    if (editedEventEndTime != null) {
                        //inactive events who's time is overlapped by a change request
                        eldEventList = eventFacade.handleChangeRequestsThatOverlapActiveEvents(eldEventList, null);
                    } else if (editedEvent.getEventType() == Enums.EmployeeLogEldEventType.DutyStatusChange) {
                        // 9/28/17 JMoen - Only allow DutyStatusChange events to update other duty status events
                        // Previously an Ignition event could inactivate a DutyStatus event while claiming unidentified periods
                        eldEventList = eventFacade.handleChangeRequestsThatOverlapActiveEvents(eldEventList, editedEvent.getStartTime());
                    }
                    // must check for provisional records in case event was originally created as a result of special category or is about to become a special category.
                    eldEventList = handleSpecialDrivingCategoryEdit(subStatus, eldEventList, origEvent, editedEvent, empLog, secondaryEventEndOdometer);
                }

                // if the RuleSet has changed, may need to update additional events that have the same original RulesetClassification
                if (origEvent.getRuleSet().getValue() != editedEvent.getRuleSet().getValue()) {
                    //if ruleset is changed, convert compatible logs to new ruleset
                    eldEventList = changeRulesetOfCompatibleEldEvents(eldEventList, editedEvent.getRuleSet());
                }

                boolean isManuallyEdited = true;
                //When we are claiming an unidentified event, we want to change it's status to claimed. This was added to support the 30 minute rule. 4.6.1.6 (c) of fmcsa.
                if(actionInitiatingSave == Enums.ActionInitiatingSaveEnum.ClaimUnidentifiedEvent &&
                        origEvent.getEventRecordStatus() == Enums.EmployeeLogEldEventRecordStatus.InactiveChanged.getValue() &&
                        origEvent.isUnidentifiedEvent())
                {
                    origEvent.setUnidentifiedEventStatus(UnidentifiedEldEventStatus.CLAIMED);
                    isManuallyEdited = false;
                }

                //return list of items which need to be updated in database
                eldEventList = eventFacade.eldEventsToUpdateInDatabase(eldEventList, true, isManuallyEdited);
            } // end Edit


            // want the updates to be in the same transaction so if one fails they both will fail.
            persistList = new EmployeeLogEldEvent[eldEventList.size()];

            for (int i = 0; i < eldEventList.size(); i++) {
                persistList[i] = eldEventList.get(i);
            }

            eventFacade.SaveListInSingleTransaction(persistList);

            // requery to pick up events saved in previous SaveListInSingleTransaction
            empLog = this.GetLocalEmployeeLog(globalState.getCurrentUser(), empLog.getLogDate());

            // Check editedEvent for Malfunctions
            // (must be done after events are saved so inserts will get PrimaryKey assigned for RelatedEvent column)
            saveEldEventCheckEventForMalfunction(empLog, persistList, actionInitiatingSave);

            // try and keep the RuleSet identified on the EmployeeLog in sync with the logs
            saveEldEventSyncEmployeeLogRuleset(empLog, editedEvent);

            // If we have a reference to a newly created eld event date (which is for a on duty hyrail/non-regulated event) use its date to
            // create the relationship between EmployeeLogEldEvent and EmployeeLogWithProvisions
            if (_onDutyInsertedEventDateTime != null) {
                updateProvisionEmployeeLogEldEventId(empLog, _onDutyInsertedEventDateTime);
            }

            if (addedProvisionEvent) {
                updateProvisionEmployeeLogEldEventId(empLog, null);
                addedProvisionEvent = false;
            }

            // if adding or editing a Manual Drive event, update parent EmployeeLog's TotalLogDistance
            if (editedEvent.isManualDrivingEvent() || (origEvent != null && origEvent.isManualDrivingEvent())) {
                updateTotalLogDistanceFromManualDrive(origEvent, editedEvent);
            }

            // If editing today's login, refresh global state otherwise logout saves global log and will overwrite changes
            updateGlobalStateEmployeeLogs(globalState.getCurrentUser(), editedEvent.getEventDateTime());

            return editedEvent;

        } catch (Exception e) {
            String msg = String.format("%s failure to save event: {%s}  EmployeeLogEldEvent: {%s}", "EmployeeLogEldMandateController.saveEldEvent", e.getMessage(), editedEvent.toLogString());
            Log.d("MandateCtl.saveEldEvent", msg);
            ErrorLogHelper.RecordMessage(this.getContext(), msg);
            throw e;
        }
    }


    /**
     * Inactivates event with same EventDateTime
     *
     * @param eldEventList
     * @param origEventDate
     * @return List<EmployeeLogEldEvent>
     */
    private List<EmployeeLogEldEvent> handleEventsWithSameDateTime(List<EmployeeLogEldEvent> eldEventList, Date origEventDate) {
        for (EmployeeLogEldEvent event : eldEventList) {
            if (event.getEventDateTime().compareTo(origEventDate) == 0) {
                // 9/28/17 JMoen - Don't modify event at same time when event belongs to the Unidentified Driver profile
                // Resulted from testing unidentified event claim with different event types at the same time (i.e. Ignition ON & Drive ON)
                if(event.getEventRecordOrigin().equals(Enums.EmployeeLogEldEventRecordOrigin.AssumedUnidentifiedDriver))
                    continue;
                else
                    event.setEventRecordStatus(Enums.EmployeeLogEldEventRecordStatus.InactiveChanged.getValue());

                if (event.getEventType() == Enums.EmployeeLogEldEventType.ChangeInDriversIndication) {
                    secondaryEventEndOdometer = event.getEndOdometer();
                }
            }
        }
        return eldEventList;
    }

    /**
     * Updates the Provision event's EmployeeLogEldEventKey with the appropriate Eld event primary key
     *
     * @param empLog
     * @param provisionEventDateTime
     */
    public void updateProvisionEmployeeLogEldEventId(EmployeeLog empLog, Date provisionEventDateTime) {
        EmployeeLogWithProvisionsFacade facade = new EmployeeLogWithProvisionsFacade(getContext());
        EmployeeLogWithProvisions provision = facade.FetchLastLogWithProvisions();
        if (provisionEventDateTime == null) {
            provisionEventDateTime = provision.getStartTime();
        }
        long localEventPk = findLogEventForProvision(empLog, provisionEventDateTime);
        if(localEventPk > 0) {
            facade.UpdateLogEldEventKey((int) provision.getPrimaryKey(), (int) localEventPk);
        }
    }

    private int findLogEventForProvision(EmployeeLog empLog, Date provisionStart){
        int localEventId = -1;
        try {
            for (EmployeeLogEldEvent localEvent : empLog.getEldEventList().getEldEventList()) {
                if (localEvent.getEventDateTime().equals(provisionStart) &&
                        localEvent.getEventRecordStatus() == Enums.EmployeeLogEldEventRecordStatus.Active.getValue() &&
                        localEvent.getEventType() == Enums.EmployeeLogEldEventType.DutyStatusChange &&
                        localEvent.getEventCode() == 4) {
                    localEventId = (int) localEvent.getPrimaryKey();
                    break;
                }
            }
        } catch(Exception e){
            Log.e("EldMandate", "error findLogEventForProvision ",e);
        }
        return localEventId;
    }

    /**
     * Returns the Provision facade. If the _provisionsFacade is null, a new EmployeeLogWithProvisionsFacade is assigned to it.
     *
     * @return EmployeeLogWithProvisionsFacade
     */
    private EmployeeLogWithProvisionsFacade getProvisionsFacade() {
        if (_provisionsFacade == null) {
            _provisionsFacade = new EmployeeLogWithProvisionsFacade(getContext());
        }

        return _provisionsFacade;
    }

    /**
     * Handles edits involving Hyrail/Non-Regulated duty status changes
     *
     * @param relatedProvision
     * @param eldEventList
     * @param editedEvent
     * @param origEvent
     * @param provisionTypeEnum
     * @param empLog
     */
    private void handleHyrailNonRegEdit(EmployeeLogWithProvisions relatedProvision, List<EmployeeLogEldEvent> eldEventList, EmployeeLogEldEvent editedEvent, EmployeeLogEldEvent origEvent, EmployeeLogProvisionTypeEnum provisionTypeEnum, EmployeeLog empLog) {
        if (relatedProvision != null) {
            // editing duty status from Hyrail/NonReg to Hyrail/NonReg
            updateProvisionEvent(editedEvent, eldEventList, relatedProvision, empLog, provisionTypeEnum);
            updateEldEventForUpdatedHyrailOrNonReg(editedEvent, origEvent, eldEventList, provisionTypeEnum);
        } else {
            // editing duty status from YM/PC to Hyrail/NonReg
            addProvisionsEvent(empLog, editedEvent, provisionTypeEnum);
            updateEldEventForNewHyrailOrNonReg(editedEvent, eldEventList, provisionTypeEnum);
        }
        addedProvisionEvent = true;
    }

    /**
     * Handles edits involving YM/PC duty status changes
     *
     * @param relatedProvision
     * @param editedEvent
     * @param eventCode
     * @param logRemark
     * @return EmployeeLogEldEvent
     */
    public EmployeeLogEldEvent handleYMPCEdit(EmployeeLogWithProvisions relatedProvision, EmployeeLogEldEvent editedEvent, int eventCode, String logRemark, float secondaryEventEndOdometer) throws CloneNotSupportedException {
        if (relatedProvision != null) {
            // editing duty status from Hyrail/NonReg  to YM/PC
            deleteProvisionEvent(relatedProvision);
        }
        // add new YM/PC event
        return addYMOrPCEvent(editedEvent, eventCode, logRemark, secondaryEventEndOdometer);
    }

    /**
     * Handles edits involving special driving category duty status changes
     *
     * @param subStatus
     * @param eldEventList
     * @param origEvent
     * @param editedEvent
     * @param empLog
     * @return List<EmployeeLogEldEvent>
     */
    public List<EmployeeLogEldEvent> handleSpecialDrivingCategoryEdit(Enums.SpecialDrivingCategory subStatus, List<EmployeeLogEldEvent> eldEventList, EmployeeLogEldEvent origEvent, EmployeeLogEldEvent editedEvent, EmployeeLog empLog, float secondaryEventEndOdometer) throws CloneNotSupportedException {
        EmployeeLogWithProvisions relatedProvision = null;
        // only go after the relatedProvision when the original event is a 1,4
        if (origEvent.getEventType() == Enums.EmployeeLogEldEventType.DutyStatusChange && origEvent.getEventCode() == EmployeeLogEldEventCode.DutyStatus_OnDuty) {
            relatedProvision = getProvisionsFacade().FetchForLogEldEventKey(empLog, origEvent.getPrimaryKey());
        }
        if (subStatus != null && subStatus != Enums.SpecialDrivingCategory.None) {
            switch (subStatus) {
                case PersonalConveyance:
                    eldEventList.add(handleYMPCEdit(relatedProvision, editedEvent, EmployeeLogEldEventCode.ChangeInDriversIndication_PersonalUse, getContext().getString(R.string.personalconveyance), secondaryEventEndOdometer));
                    break;
                case YardMove:
                    eldEventList.add(handleYMPCEdit(relatedProvision, editedEvent, EmployeeLogEldEventCode.ChangeInDriversIndication_YardMoves, getContext().getString(R.string.type3code2), secondaryEventEndOdometer));
                    break;
                case Hyrail:
                    handleHyrailNonRegEdit(relatedProvision, eldEventList, editedEvent, origEvent, EmployeeLogProvisionTypeEnum.HYRAIL, empLog);
                    break;
                case NonRegulated:
                    handleHyrailNonRegEdit(relatedProvision, eldEventList, editedEvent, origEvent, EmployeeLogProvisionTypeEnum.NONREGULATED, empLog);
                    break;
                default:
                    break;
            }
        } else {
            if (relatedProvision != null) {
                // origEvent is a Hyrail/Non Reg and since this is a duty status change to a regular driving category, the provision table record needs to be deleted
                deleteProvisionEvent(relatedProvision);
            }
        }
        return eldEventList;
    }

    /**
     * Keep the RuleSet identified on the EmployeeLog in sync with the logs
     */
    private void saveEldEventSyncEmployeeLogRuleset(EmployeeLog employeeLog, EmployeeLogEldEvent editedEvent) {

        if ((employeeLog.getRuleset().isCanadianRuleset() && editedEvent.getRuleSet().isCanadianRuleset()) || (!employeeLog.getRuleset().isCanadianRuleset() && !editedEvent.getRuleSet().isCanadianRuleset())) {

            boolean hasChanged = false;

            // requery to pick up events saved in previous malfunction check
            employeeLog = this.GetLocalEmployeeLog(globalState.getCurrentUser(), employeeLog.getLogDate());

            // if switching from oil field ruleset, turn off oilfield specific vehicle flag on current log
            if (employeeLog.getRuleset().isAnyOilFieldRuleset() && !editedEvent.getRuleSet().isAnyOilFieldRuleset()) {
                hasChanged = true;
                employeeLog.setIsOperatesSpecificVehiclesForOilfield(false);
            }
            // if switching to oil field ruleset, set oilfield flag
            else if (!employeeLog.getRuleset().isAnyOilFieldRuleset() && editedEvent.getRuleSet().isAnyOilFieldRuleset()) {
                hasChanged = true;
                employeeLog.setIsOperatesSpecificVehiclesForOilfield(true);
            }

            if (employeeLog.getRuleset() == null || employeeLog.getRuleset().getValue() != editedEvent.getRuleSet().getValue()) {
                hasChanged = true;
                employeeLog.setRuleset(editedEvent.getRuleSet());
            }

            if (hasChanged) {
                SaveLocalEmployeeLog(employeeLog);
            }
        }
    }

    /**
     * Check editedEvent for Malfunctions
     */
    private void saveEldEventCheckEventForMalfunction(EmployeeLog employeeLog, EmployeeLogEldEvent[] persistList, Enums.ActionInitiatingSaveEnum actionInitiatingSave) throws Throwable {

        boolean eventMalfunctionCreated = false;

        for (int i = 0; i < persistList.length; i++) {

            EmployeeLogEldEvent event = persistList[i];

            if (event.getEventRecordStatus() == Enums.EmployeeLogEldEventRecordStatus.Active.getValue()) {

                boolean dutyStatusCheckForMissingEventComment = true;

                // Claiming Unidentified events or saving Unidentified Missing Data doesn't require EventComment (i.e. Annotation)
                if (event.getEventType() == Enums.EmployeeLogEldEventType.DutyStatusChange && actionInitiatingSave == Enums.ActionInitiatingSaveEnum.ClaimUnidentifiedEvent) {
                    dutyStatusCheckForMissingEventComment = false;
                }

                // Check event types 1-3 for missing data and generate diagnostic event
                if (CheckEventForMalfunction(employeeLog, dutyStatusCheckForMissingEventComment, event, null))
                    eventMalfunctionCreated = true;
            }
        }


        // re-Save to persist setDriverDataDiagnosticEventIndicatorStatus set on the event if a corresponding Malfunction was created
        if (eventMalfunctionCreated) {
            eventFacade.SaveListInSingleTransaction(persistList);
        }
    }

    /**
     * Creates the secondary YM/PC event and returns it to be added to the eldEventList
     *
     * @param editedEvent
     * @param eventCode
     * @param logRemark
     * @return EmployeeLogEldEvent
     */
    public EmployeeLogEldEvent addYMOrPCEvent(EmployeeLogEldEvent editedEvent, int eventCode, String logRemark, float secondaryEventEndOdometer) throws CloneNotSupportedException {
        EmployeeLogEldEvent secondarySDCEvent = (EmployeeLogEldEvent) editedEvent.clone();
        secondarySDCEvent.setPrimaryKey(-1L);    // force insert as a new Active record
        secondarySDCEvent.setEventRecordOrigin(2); // Edited or entered by the Driver
        secondarySDCEvent.setEventSequenceIDNumber(EmployeeLogEldEnum.DEFAULT);
        secondarySDCEvent.setEventType(Enums.EmployeeLogEldEventType.ChangeInDriversIndication);
        secondarySDCEvent.setEventCode(eventCode);
        secondarySDCEvent.setOdometer(null);
        secondarySDCEvent.setEndOdometer(secondaryEventEndOdometer);
        secondarySDCEvent.setLogRemark(logRemark);
        return secondarySDCEvent;
    }

    /**
     * Removes the editedEvent from the list since this is for updating from one Hyrail/Non-Regulated event to another.
     * Also it updates the Provision event's provisionTypeEnum
     *
     * @param editedEvent
     * @param eldEventList
     * @param provision
     * @param empLog
     * @param provisionTypeEnum
     */
    private void updateProvisionEvent(EmployeeLogEldEvent editedEvent, List<EmployeeLogEldEvent> eldEventList, EmployeeLogWithProvisions provision, EmployeeLog empLog, EmployeeLogProvisionTypeEnum provisionTypeEnum) {
        EmployeeLogWithProvisionsFacade facade = new EmployeeLogWithProvisionsFacade(getContext());
        eldEventList.remove(editedEvent);
        provision.setProvisionTypeEnum(provisionTypeEnum.getValue());
        provision.setTractorNumber(editedEvent.getTractorNumber());
        provision.setStartLocation(editedEvent.getLocation());
        facade.Save(provision, empLog);
    }

    /**
     * Updates the original Hyrail/Non-Regulated Eld event's values
     *
     * @param editedEvent
     * @param origEvent
     * @param eldEventList
     * @param provisionTypeEnum
     */
    private void updateEldEventForUpdatedHyrailOrNonReg(EmployeeLogEldEvent editedEvent, EmployeeLogEldEvent origEvent, List<EmployeeLogEldEvent> eldEventList, EmployeeLogProvisionTypeEnum provisionTypeEnum) {
        for (EmployeeLogEldEvent event : eldEventList) {
            if (event.getPrimaryKey() == origEvent.getPrimaryKey()) {
                String logRemark = getContext().getString(R.string.special_driving_category_started, provisionTypeEnum.getString(), DateUtility.getHomeTerminalTime12HourFormat().format(event.getEventDateTime()));
                event.setLogRemark(logRemark);
                event.setEventRecordStatus(Enums.EmployeeLogEldEventRecordStatus.InactiveChangeRequested.getValue());
                event.setLocation(editedEvent.getLocation());
                event.setEventRecordOrigin(Enums.EmployeeLogEldEventRecordOrigin.EditedEnteredByDriver);
                event.setTractorNumber(editedEvent.getTractorNumber());
                event.setTrailerNumber(editedEvent.getTrailerNumber());
                event.setShipmentInfo(editedEvent.getShipmentInfo());
                event.setRuleSet(editedEvent.getRuleSet());
                event.setLogRemarkDateTime(editedEvent.getLogRemarkDateTime());
                event.setEventComment(editedEvent.getEventComment());
                break;
            }
        }
    }

    /**
     * Updates the new Hyrail/Non-Regulated Eld event's log remark
     *
     * @param editedEvent
     * @param eldEventList
     * @param provisionTypeEnum
     */
    private void updateEldEventForNewHyrailOrNonReg(EmployeeLogEldEvent editedEvent, List<EmployeeLogEldEvent> eldEventList, EmployeeLogProvisionTypeEnum provisionTypeEnum) {
        for (EmployeeLogEldEvent event : eldEventList) {
            if (event.getPrimaryKey() == editedEvent.getPrimaryKey()) {
                String logRemark = getContext().getString(R.string.special_driving_category_started, provisionTypeEnum.getString(), DateUtility.getHomeTerminalTime12HourFormat().format(event.getEventDateTime()));
                event.setLogRemark(logRemark);
                break;
            }
        }
    }

    /**
     * Deletes the Provision event from the Provision table
     *
     * @param provision
     */
    private void deleteProvisionEvent(EmployeeLogWithProvisions provision) {
        getProvisionsFacade().Delete(provision);
    }

    /**
     * Creates a Provision event and populates it with the correct data and then saves it to the Provision table
     *
     * @param empLog
     * @param editedEvent
     * @param provisionTypeEnum
     */
    private void addProvisionsEvent(EmployeeLog empLog, EmployeeLogEldEvent editedEvent, EmployeeLogProvisionTypeEnum provisionTypeEnum) {
        EmployeeLogWithProvisions provision = new EmployeeLogWithProvisions();
        provision.setProvisionTypeEnum(provisionTypeEnum.getValue());
        provision.setStartTime(editedEvent.getStartTime());
        provision.setStartLocation(editedEvent.getLocation());
        provision.setEmployeeLogEldEventId((int) editedEvent.getPrimaryKey());
        provision.setStartLocation(editedEvent.getLocation());
        provision.setTractorNumber(editedEvent.getTractorNumber());
        provision.setEndLocation(editedEvent.getLocation());
        provision.getEndLocation().setOdometerReading(editedEvent.getEndOdometer());
        if (editedEvent.getDistance() != null) {
            provision.setTotalDistance(editedEvent.getDistance());
        } else {
            provision.setTotalDistance(0);
        }
        getProvisionsFacade().Save(provision, empLog);
    }

    public List<EmployeeLogEldEvent> reconcileEvents(EmployeeLogEldEvent editedEvent, Date originalStartTime, Date endTime, List<EmployeeLogEldEvent> eldEvents, boolean isEditingAutomaticDriveTime) {
        return reconcileEvents(editedEvent, originalStartTime, endTime, eldEvents, isEditingAutomaticDriveTime, false);
    }

    public List<EmployeeLogEldEvent> reconcileEvents(EmployeeLogEldEvent editedEvent, Date originalStartTime, Date endTime, List<EmployeeLogEldEvent> eldEvents, boolean isEditingAutomaticDriveTime, boolean isReviewingChangeRequests) {
        String[] split = GlobalState.getInstance().getCompanyConfigSettings(getContext()).getDailyLogStartTime().split(":");
        int companyStartTimeHour = Integer.parseInt(split[0]);
        int companyStartTimeMinute = Integer.parseInt(split[1]);
        Calendar companyEndTimeCalendar = Calendar.getInstance();
        companyEndTimeCalendar.setTime((Date) endTime.clone());
        companyEndTimeCalendar.setTimeZone(TimeZone.getDefault());
        companyEndTimeCalendar.set(Calendar.HOUR_OF_DAY, companyStartTimeHour);
        companyEndTimeCalendar.set(Calendar.MINUTE, companyStartTimeMinute);
        companyEndTimeCalendar.set(Calendar.SECOND, 0);
        companyEndTimeCalendar.add(Calendar.HOUR_OF_DAY, 23);
        companyEndTimeCalendar.add(Calendar.MINUTE, 59);
        companyEndTimeCalendar.add(Calendar.SECOND, 59);
        Date companyEndTime = companyEndTimeCalendar.getTime();

        if (endTime.compareTo(companyEndTime) != 0) {
            List<EmployeeLogEldEvent> dutyStatusEvents = getDutyStatusChangeEvents(eldEvents);
            List<Integer> eventRecordStatuses = getRecordStatuses(isReviewingChangeRequests);

            // get the next duty status event with one of the specified statuses
            EmployeeLogEldEvent nextEvent = editedEvent.getPrimaryKey() == -1 && !isEditingAutomaticDriveTime ? null : EmployeeLogUtilities.getNextEventAfterDate(dutyStatusEvents, originalStartTime, eventRecordStatuses);
            if (nextEvent == null || nextEvent.getEventDateTime().compareTo(endTime) != 0) {
                EmployeeLogEldEvent previousEvent = previousEldEvent(dutyStatusEvents, endTime);
                if (previousEvent != null && (nextEvent == null || !previousEvent.getEventDateTime().equals(nextEvent.getEventDateTime()))) {
                    //to duplicate this record, fetch a new instance
                    previousEvent = eventFacade.FetchByKey((int) previousEvent.getPrimaryKey());
                    previousEvent.setEventDateTime(endTime);
                    previousEvent.setEventRecordStatus(Enums.EmployeeLogEldEventRecordStatus.InactiveChangeRequested.getValue());
                    previousEvent.setEventRecordOrigin(Enums.EmployeeLogEldEventRecordOrigin.EditedEnteredByDriver);
                    previousEvent.setEventComment(editedEvent.getEventComment());
                    previousEvent.setEncompassOriginatorUserId(null);
                    previousEvent.setPrimaryKey(-1L);       // force insert as a new Active record
                    previousEvent.setEncompassClusterPK(0); // new event will not be related to existing ENC event
                    previousEvent.setEventSequenceIDNumber(EmployeeLogEldEnum.DEFAULT);   // force new sequence number to be generated in EmployeeLogEldEventPersist
                    previousEvent.setIsManuallyEditedByKMBUser(true);

                    // determine end of day for the company
                    String dailyLogStart = GlobalState.getInstance().getCompanyConfigSettings(GlobalState.getInstance().getEobrService()).getDailyLogStartTime();
                    TimeZoneEnum timeZone = GlobalState.getInstance().getCurrentUser().getHomeTerminalTimeZone();
                    Date logEndTime = EmployeeLogUtilities.CalculateLogEndTime(dailyLogStart, editedEvent.getEventDateTime(), timeZone);

                    if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
                        // calculate EditDuration (timespan in milliseconds)
                        Date nextStartTime;
                        nextEvent = EmployeeLogUtilities.getNextActiveEventAfterDate(dutyStatusEvents, endTime);

                        if (nextEvent == null) {
                            nextStartTime = logEndTime;
                        } else {
                            nextStartTime = nextEvent.getEventDateTime();
                        }

                        // calculate EditDuration (timespan in milliseconds)
                        long duration = Math.abs(nextStartTime.getTime() - endTime.getTime());
                        previousEvent.setEditDuration(duration);
                    } else {
                        // calculate EditDuration (timespan in minutes)
                        Date nextStartTime;
                        nextEvent = EmployeeLogUtilities.getNextActiveEventAfterDate(dutyStatusEvents, endTime);

                        if (nextEvent == null) {
                            nextStartTime = logEndTime;
                        } else {
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(nextEvent.getEventDateTime());
                            nextStartTime = calendar.getTime();
                        }

                        // calculate EditDuration (timespan in minutes)
                        long duration = Math.abs(nextStartTime.getTime() - endTime.getTime());
                        previousEvent.setEditDuration(duration);
                    }
                    eldEvents.add(previousEvent);
                }
            }
        }

        // When reviewing change requests, the edited/reassigned event is already in eldEvents.
        if (!isReviewingChangeRequests) {
            //acceptChangeRequest looks at status=changeRequest so temporarily change it here
            editedEvent.setEventRecordStatus(Enums.EmployeeLogEldEventRecordStatus.InactiveChangeRequested.getValue());
            eldEvents.add(editedEvent);
        }

        return eldEvents;
    }

    /**
     * If saveEldEvent is adding or altering a Manual Drive event, adjust the parent EmployeeLog's TotalLogDistance value
     */
    private void updateTotalLogDistanceFromManualDrive(EmployeeLogEldEvent originalEvent, EmployeeLogEldEvent manualDriveEvent) {

        Integer totalLogDistanceAdjustment = null;

        // adding a new Manual Drive event
        if (originalEvent == null) {
            if (manualDriveEvent.isManualDrivingEvent()) {
                totalLogDistanceAdjustment = manualDriveEvent.getDistance();
            }
        }
        else {
            // reverting Manual Drive to non-Drive Duty Status
            if (originalEvent.isManualDrivingEvent() && !manualDriveEvent.isManualDrivingEvent()) {
                if (originalEvent.getDistance() != null) {
                    totalLogDistanceAdjustment = originalEvent.getDistance() * -1;  // subtract former Manual Drive distance
                }
            }
            // changing non-Drive Duty Status to Manual Drive
            else if (!originalEvent.isManualDrivingEvent() && manualDriveEvent.isManualDrivingEvent()) {
                totalLogDistanceAdjustment = manualDriveEvent.getDistance();
            }
            // editing existing Manual Drive
            else if (originalEvent.isManualDrivingEvent() && manualDriveEvent.isManualDrivingEvent()) {
                totalLogDistanceAdjustment = (manualDriveEvent.getDistance() == null ? 0 : manualDriveEvent.getDistance()) - (originalEvent.getDistance() == null ? 0 : originalEvent.getDistance());
            }
        }

        if (totalLogDistanceAdjustment != null && totalLogDistanceAdjustment != 0) {
            // requery to pick up latest log events
            EmployeeLogFacade employeeLogFacade =  new EmployeeLogFacade(getContext(), getCurrentUser());
            EmployeeLog employeeLog = this.GetLocalEmployeeLog(globalState.getCurrentUser(), manualDriveEvent.getEventDateTime());
            if (employeeLog != null) {
                float newTotalLogDistance = employeeLog.getTotalLogDistance() + totalLogDistanceAdjustment;
                if (newTotalLogDistance < 0) {
                    newTotalLogDistance = 0.0f;
                }
                employeeLog.setTotalLogDistance(newTotalLogDistance);
                employeeLogFacade.Save(employeeLog, 1 /*local*/);
            }
        }
    }

    private List<EmployeeLogEldEvent> getDutyStatusChangeEvents(List<EmployeeLogEldEvent> eldEvents) {
        List<EmployeeLogEldEvent> dutyStatusEvents = new ArrayList<EmployeeLogEldEvent>();

        for (EmployeeLogEldEvent event : eldEvents) {
            if (event.getEventType() == Enums.EmployeeLogEldEventType.DutyStatusChange) {
                dutyStatusEvents.add(event);
            }
        }
        return dutyStatusEvents;
    }

    private List<Integer> getRecordStatuses(boolean isReviewingChangeRequests) {
        if (isReviewingChangeRequests) {
            return Arrays.asList(Enums.EmployeeLogEldEventRecordStatus.Active.getValue(), Enums.EmployeeLogEldEventRecordStatus.InactiveChangeRequested.getValue());
        } else {
            return Arrays.asList(Enums.EmployeeLogEldEventRecordStatus.Active.getValue());
        }
    }

    private EmployeeLogEldEvent previousEldEvent(List<EmployeeLogEldEvent> eldEventList, Date eldRequestedTime) {
        if (eldEventList == null || eldEventList.size() == 0) {
            return null;
        }

        Date impliedEndTime = null;

        for (int i = eldEventList.size() - 1; i >= 0; i--) {
            EmployeeLogEldEvent eldEvent = eldEventList.get(i);
            if (eldEvent.getEventDateTime().before(eldRequestedTime) && eldEvent.getEventRecordStatus() != null && eldEvent.getEventRecordStatus() == Enums.EmployeeLogEldEventRecordStatus.Active.getValue()) {
                // we've found a previous event < edited EndTime, now check it's implied EndTime to see if it exactly ends at the same time as the edited EndTime. If so, we won't need to split the event
                if (impliedEndTime != null && impliedEndTime.compareTo(eldRequestedTime) == 0) {
                    return null;
                }
                return eldEvent;
            }

            // keep track of 'implied' end time
            if (eldEvent.getEventRecordStatus() != null && eldEvent.getEventRecordStatus() == Enums.EmployeeLogEldEventRecordStatus.Active.getValue()) {
                impliedEndTime = eldEvent.getEventDateTime();
            }
        }
        return null;
    }

    /**
     * Used by UI to prompt user "Changing to this ruleset will convert compatible logs to this same ruleset. Continue with changing to the new ruleset?"
     */
    public boolean doesRulesetChangeUpdateOtherEldEvents(EmployeeLogEldEvent[] eldEvents, RuleSetTypeEnum newRuleSetTypeEnum) {

        List<EmployeeLogEldEvent> eldEventList = new ArrayList<EmployeeLogEldEvent>();

        // list are easier to work with than array's so convert array to a list
        for (int i = 0; i < eldEvents.length; i++) {
            eldEventList.add(eldEvents[i]);
        }

        // assume if any RuleSet changes, changeRulesetOfCompatibleEldEvents will add a new 'cloned' event so count will increase
        return changeRulesetOfCompatibleEldEvents(eldEventList, newRuleSetTypeEnum).size() > eldEvents.length;
    }

    /**
     * Inactivate ELD Events whose RuleSet needs to change and insert new Active ELD Events.
     */
    private List<EmployeeLogEldEvent> changeRulesetOfCompatibleEldEvents(List<EmployeeLogEldEvent> eldEventList, RuleSetTypeEnum newRuleSetTypeEnum) {

        // temporary list of additions so we don't add new items while iterating the list
        List<EmployeeLogEldEvent> tempListOfAdditions = new ArrayList<EmployeeLogEldEvent>();

        for (EmployeeLogEldEvent eldEvent : eldEventList) {
            if (eldEvent.getEventType() != Enums.EmployeeLogEldEventType.DutyStatusChange)
                continue;

            if (eldEvent.getEventRecordStatus() != Enums.EmployeeLogEldEventRecordStatus.Active.getValue() && eldEvent.getEventRecordStatus() != Enums.EmployeeLogEldEventRecordStatus.InactiveChangeRequested.getValue())
                continue;

            if ((eldEvent.getRuleSet().isCanadianRuleset() && newRuleSetTypeEnum.isCanadianRuleset()) || (!eldEvent.getRuleSet().isCanadianRuleset() && !newRuleSetTypeEnum.isCanadianRuleset())) {
                if (newRuleSetTypeEnum.getValue() != eldEvent.getRuleSet().getValue()) {

                    if (eldEvent.getEventRecordStatus() == Enums.EmployeeLogEldEventRecordStatus.Active.getValue()) {
                        // Inactivate the currently active event
                        eldEvent.setEventRecordStatus(Enums.EmployeeLogEldEventRecordStatus.InactiveChanged.getValue());

                        // Clone the event
                        EmployeeLogEldEvent cloneEvent = eventFacade.FetchByKey((int) eldEvent.getPrimaryKey());
                        cloneEvent.setPrimaryKey(-1);    // force insert as a new Active record
                        cloneEvent.setEventSequenceIDNumber(EmployeeLogEldEnum.DEFAULT);   // force new sequence number to be generated in EmployeeLogEldEventPersist
                        cloneEvent.setEventRecordStatus(Enums.EmployeeLogEldEventRecordStatus.InactiveChangeRequested.getValue());
                        cloneEvent.setIsManuallyEditedByKMBUser(true);
                        cloneEvent.setRuleSet(newRuleSetTypeEnum);

                        tempListOfAdditions.add(cloneEvent);
                    } else if (eldEvent.getEventRecordStatus() == Enums.EmployeeLogEldEventRecordStatus.InactiveChangeRequested.getValue()) {
                        // probably a new time 'gap' record so no need to clone
                        eldEvent.setRuleSet(newRuleSetTypeEnum);
                    }
                }
            }
        }

        // safely add new items to the list
        eldEventList.addAll(tempListOfAdditions);

        return eldEventList;
    }

    private class ExtendAutomaticallyGeneratedDriveTimeInfo {
        private EmployeeLogEldEvent editedEvent;
        private EmployeeLogEldEvent newEvent;
        private Date startTime;
        private Date endTime;

        public EmployeeLogEldEvent getEditedEvent() {
            return editedEvent;
        }

        public EmployeeLogEldEvent getNewEvent() {
            return newEvent;
        }

        public Date getStartTime() {
            return startTime;
        }

        public Date getEndTime() {
            return endTime;
        }

        public ExtendAutomaticallyGeneratedDriveTimeInfo(EmployeeLogEldEvent editedEvent, EmployeeLogEldEvent newEvent, Date startTime, Date endTime) {
            this.editedEvent = editedEvent;
            this.newEvent = newEvent;
            this.startTime = startTime;
            this.endTime = endTime;
        }
    }

    /**
     * Changing automatic drive to special driving category, create appropriate Start/End records
     */
    private ExtendAutomaticallyGeneratedDriveTimeInfo saveEldEventExtendAutomaticallyGeneratedDriveTime(EmployeeLog empLog, EmployeeLogEldEvent origDrivingEvent, EmployeeLogEldEvent editedEvent, Date editedEventEndTime, List<EmployeeLogEldEvent> eldEventList) {
        Date startTime;
        // calculate EditDuration (timespan in minutes)
        Date originalEndTime;
        EmployeeLogEldEvent nextEvent = EmployeeLogUtilities.getNextActiveEventAfterDate(eldEventList, origDrivingEvent.getEventDateTime());
        Calendar calendar = Calendar.getInstance();

        // figure out the original EndTime
        if (nextEvent == null) {
            // determine end of day for the company
            String dailyLogStart = GlobalState.getInstance().getCompanyConfigSettings(GlobalState.getInstance().getEobrService()).getDailyLogStartTime();
            TimeZoneEnum timeZone = GlobalState.getInstance().getCurrentUser().getHomeTerminalTimeZone();
            originalEndTime = EmployeeLogUtilities.CalculateLogEndTime(dailyLogStart, editedEvent.getEventDateTime(), timeZone);
        } else {
            calendar.setTime(nextEvent.getEventDateTime());
            originalEndTime = calendar.getTime();
        }

        if (editedEvent.getEventDateTime().before(origDrivingEvent.getEventDateTime()) && (editedEventEndTime == null || editedEventEndTime.compareTo(originalEndTime) == 0)) {
            // if changing ONLY StartTime to earlier, origDrivingEvent manual EndTime should be the automatically generated StartTime
            calendar.setTime(origDrivingEvent.getEventDateTime());
            editedEventEndTime = calendar.getTime();
            startTime = editedEvent.getStartTime();
            editedEvent.setEventRecordOrigin(Enums.EmployeeLogEldEventRecordOrigin.EditedEnteredByDriver);

            // this is a manually edited copy so we don't want to accumulate miles
            editedEvent.setDistance(null);
            editedEvent.setEndOdometer(editedEvent.getOdometer());

            return new ExtendAutomaticallyGeneratedDriveTimeInfo(editedEvent, null, startTime, editedEventEndTime);
        } else if (editedEvent.getEventDateTime().compareTo(origDrivingEvent.getEventDateTime()) == 0 && editedEventEndTime != null && editedEventEndTime.after(originalEndTime)) {
            // if changing ONLY EndTime to later, then manual StartTime should be the next events start time.

            startTime = nextEvent.getStartTime();
            editedEvent.setEventDateTime(startTime);
            editedEvent.setEventRecordOrigin(Enums.EmployeeLogEldEventRecordOrigin.EditedEnteredByDriver);

            // this is a manually edited copy so we don't want to accumulate miles
            editedEvent.setDistance(null);
            editedEvent.setEndOdometer(editedEvent.getOdometer());

            return new ExtendAutomaticallyGeneratedDriveTimeInfo(editedEvent, null, startTime, editedEventEndTime);
        } else if (editedEvent.getEventDateTime().before(origDrivingEvent.getEventDateTime()) && editedEventEndTime != null && editedEventEndTime.after(originalEndTime)) {
            // if changing BOTH StartTime and EndTime, need to create two records

            //make the original event a duplicate
            EmployeeLogEldEvent dupEvent = null;
            try {
                dupEvent = (EmployeeLogEldEvent) origDrivingEvent.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
            calendar.setTime(editedEvent.getEventDateTime());
            dupEvent.setEventDateTime(calendar.getTime());
            long duration = Math.abs(dupEvent.getEventDateTime().getTime() - origDrivingEvent.getEventDateTime().getTime());
            dupEvent.setEditDuration(duration);
            dupEvent.setPrimaryKey(-1L);
            dupEvent.setEventSequenceIDNumber(EmployeeLogEldEnum.DEFAULT);   // force new sequence number to be generated in EmployeeLogEldEventPersist
            dupEvent.setEventRecordStatus(Enums.EmployeeLogEldEventRecordStatus.InactiveChangeRequested.getValue());
            dupEvent.setEventRecordOrigin(Enums.EmployeeLogEldEventRecordOrigin.EditedEnteredByDriver);
            dupEvent.setDriversLocationDescription(editedEvent.getDriversLocationDescription());
            dupEvent.setTractorNumber(editedEvent.getTractorNumber());
            dupEvent.setTrailerNumber(editedEvent.getTrailerNumber());
            dupEvent.setShipmentInfo(editedEvent.getShipmentInfo());
            dupEvent.setRuleSet(editedEvent.getRuleSet());
            dupEvent.setEventComment(editedEvent.getEventComment());
            dupEvent.setEncompassClusterPK(0L); // generate new Encompass record

            // this is a manually edited copy so we don't want to accumulate miles
            dupEvent.setDistance(null);
            dupEvent.setEndOdometer(editedEvent.getOdometer());

            startTime = nextEvent.getStartTime();
            editedEvent.setEventDateTime(startTime);
            editedEvent.setEventRecordOrigin(Enums.EmployeeLogEldEventRecordOrigin.EditedEnteredByDriver);

            // this is a manually edited copy so we don't want to accumulate miles
            editedEvent.setDistance(null);
            editedEvent.setEndOdometer(origDrivingEvent.getOdometer());

            return new ExtendAutomaticallyGeneratedDriveTimeInfo(editedEvent, dupEvent, startTime, editedEventEndTime);
        }

        return null;
    }

    /**
     * Changing automatic drive to special driving category, create appropriate Start/End records
     */
    private List<EmployeeLogEldEvent> saveEldEventSpecialDrivingCategory(EmployeeLog empLog, EmployeeLogEldEvent origDrivingEvent, EmployeeLogEldEvent editedEvent, Enums.SpecialDrivingCategory subStatus, Date editedEventEndTime, List<EmployeeLogEldEvent> eldEventList) {
        // subtract Driving distance from EmployeeLog TotalLogDistance
        float totalLogDistance = empLog.getTotalLogDistance();
        if (origDrivingEvent.getEndOdometer() != null && origDrivingEvent.getOdometer() != null) {
            float eventMiles = origDrivingEvent.getEndOdometer() - origDrivingEvent.getOdometer();
            float newLogDistance = totalLogDistance - eventMiles;
            empLog.setTotalLogDistance(newLogDistance < 0.0f ? 0.0f : newLogDistance);

            final EmployeeLogFacade empLogFacade = new EmployeeLogFacade(getContext(), getCurrentUser());
            empLogFacade.Save(empLog, 1 /*local*/);
        }

        List<EmployeeLogEldEvent> specialCategoryEventList = new ArrayList<EmployeeLogEldEvent>();

        // Setting the end time of PC/YM to the start of the next event is probably the cleanest way to handle this
        EmployeeLogEldEvent nextEldEvent = EmployeeLogUtilities.getNextActiveEventAfterDate(eldEventList, origDrivingEvent.getEventDateTime());

        //Create events for PC, YM, Hyrail and non-regulated
        switch (subStatus) {
            case PersonalConveyance:
                specialCategoryEventList = editAutomaticDrivingToSpecialCategory(
                        editedEvent,
                        editedEventEndTime,
                        subStatus,
                        EmployeeLogEldEventCode.ChangeInDriversIndication_PersonalUse,
                        getContext().getString(R.string.personalconveyance),
                        EmployeeLogEldEventCode.DutyStatus_OffDuty,
                        getContext().getString(R.string.endofpc), nextEldEvent);
                break;
            case YardMove:
                specialCategoryEventList = editAutomaticDrivingToSpecialCategory(
                        editedEvent,
                        editedEventEndTime,
                        subStatus,
                        EmployeeLogEldEventCode.ChangeInDriversIndication_YardMoves,
                        getContext().getString(R.string.type3code2),
                        EmployeeLogEldEventCode.DutyStatus_OnDuty,
                        getContext().getString(R.string.endofym), nextEldEvent);
                break;
            case Hyrail:
                specialCategoryEventList = editAutomaticDrivingToSpecialCategory(
                        editedEvent,
                        editedEventEndTime,
                        subStatus,
                        0,
                        getContext().getString(R.string.special_driving_category_started, getContext().getString(R.string.type1code4ProvisionsType1), DateUtility.getHomeTerminalTime12HourFormat().format(origDrivingEvent.getEventDateTime())),
                        EmployeeLogEldEventCode.DutyStatus_OnDuty,
                        null,
                        nextEldEvent);
                break;
            case NonRegulated:
                specialCategoryEventList = editAutomaticDrivingToSpecialCategory(
                        editedEvent,
                        editedEventEndTime,
                        subStatus,
                        0,
                        getContext().getString(R.string.special_driving_category_started, getContext().getString(R.string.type1code4ProvisionsType2), DateUtility.getHomeTerminalTime12HourFormat().format(origDrivingEvent.getEventDateTime())),
                        EmployeeLogEldEventCode.DutyStatus_OnDuty,
                        null,
                        nextEldEvent);
                break;
            default:
                break;
        }

        return specialCategoryEventList;
    }

    /**
     * Editing of automatically generated driving to log driving time under Yard Move duty status so create the corresponding Start/OnDuty/End records.
     */
    private List<EmployeeLogEldEvent> editAutomaticDrivingToSpecialCategory(EmployeeLogEldEvent editedEvent, Date endTime, Enums.SpecialDrivingCategory subStatus, int startEventCode, String startEventRemark, int dutyStatusEventCode, String endEventRemark, EmployeeLogEldEvent nextEldEvent) {

        List<EmployeeLogEldEvent> specialCategoryEventList = new ArrayList<>();

        if (subStatus == Enums.SpecialDrivingCategory.PersonalConveyance || subStatus == Enums.SpecialDrivingCategory.YardMove) {
            try {
                Date currentClockHomeTerminalTime = DateUtility.CurrentHomeTerminalTime(this.getCurrentUser());

            /*
             * Create the Start event
             */
                EmployeeLogEldEvent startEvent = (EmployeeLogEldEvent) editedEvent.clone();
                startEvent.setPrimaryKey(-1L);    // force insert as a new Active record
                startEvent.setEventSequenceIDNumber(EmployeeLogEldEnum.DEFAULT);   // force new sequence number to be generated in EmployeeLogEldEventPersist
                startEvent.setEventRecordStatus(Enums.EmployeeLogEldEventRecordStatus.InactiveChangeRequested.getValue());  // acceptChangeRequest looks at status=changeRequest so temporarily change it here
                startEvent.setEventRecordOrigin(2); // Edited or entered by the Driver
                startEvent.setEventType(Enums.EmployeeLogEldEventType.ChangeInDriversIndication);
                startEvent.setEventCode(startEventCode);
                startEvent.setLogRemark(startEventRemark);
                startEvent.setLogRemarkDateTime(currentClockHomeTerminalTime);
                startEvent.setIsManuallyEditedByKMBUser(true);
                startEvent.setEncompassClusterPK(0L); // generate new Encompass record
                SetEngineHoursAndAccumulatedVehicleMiles(startEvent, startEvent.getEventDateTime(), null);

                // set the distance since last valid coordinates
                StatusRecord statusRecord = getStatusRecord(startEvent.getEventDateTime());
                if (statusRecord != null)
                    startEvent.setDistanceSinceLastCoordinates(EventTranslationBase.calculateDistanceSinceLastValidCoordinates(statusRecord.getGpsUncertDistance()));

                // set reduced precision GPS
                if (startEventCode == EmployeeLogEldEventCode.ChangeInDriversIndication_PersonalUse && startEvent.getLatitude() != null && startEvent.getLongitude() != null) {
                    startEvent.setLatitude(EmployeeLogUtilities.GetReducedPrecisionGPSForDouble(startEvent.getLatitude(), true));
                    startEvent.setLongitude(EmployeeLogUtilities.GetReducedPrecisionGPSForDouble(startEvent.getLongitude(), true));
                    startEvent.setIsGPSAtReducedPrecision(true);
                }

                specialCategoryEventList.add(startEvent);

            /*
             * Create the Duty Status event
             */
                EmployeeLogEldEvent dutyStatusEvent = (EmployeeLogEldEvent) editedEvent.clone();
                dutyStatusEvent.setPrimaryKey(-1L);    // force insert as a new Active record
                dutyStatusEvent.setEventSequenceIDNumber(EmployeeLogEldEnum.DEFAULT);   // force new sequence number to be generated in EmployeeLogEldEventPersist
                dutyStatusEvent.setEventRecordStatus(Enums.EmployeeLogEldEventRecordStatus.InactiveChangeRequested.getValue());  // acceptChangeRequest looks at status=changeRequest so temporarily change it here
                dutyStatusEvent.setEventRecordOrigin(2); // Edited or entered by the Driver
                dutyStatusEvent.setEventType(Enums.EmployeeLogEldEventType.DutyStatusChange);
                dutyStatusEvent.setEventCode(dutyStatusEventCode);
                dutyStatusEvent.setIsManuallyEditedByKMBUser(true);
                dutyStatusEvent.setEncompassClusterPK(0L); // generate new Encompass record

                specialCategoryEventList.add(dutyStatusEvent);

            /*
             * Create the End event
             */
                EmployeeLogEldEvent endEvent = (EmployeeLogEldEvent) editedEvent.clone();
                endEvent.setPrimaryKey(-1L);    // force insert as a new Active record
                endEvent.setEventSequenceIDNumber(EmployeeLogEldEnum.DEFAULT);   // force new sequence number to be generated in EmployeeLogEldEventPersist
                endEvent.setEventRecordStatus(Enums.EmployeeLogEldEventRecordStatus.InactiveChangeRequested.getValue());  // acceptChangeRequest looks at status=changeRequest so temporarily change it here
                endEvent.setEventRecordOrigin(2); // Edited or entered by the Driver
                endEvent.setEventType(Enums.EmployeeLogEldEventType.ChangeInDriversIndication);
                endEvent.setEventCode(EmployeeLogEldEventCode.ChangeInDriversIndication_PCYMWT_Cleared);
                endEvent.setLogRemark(endEventRemark);
                endEvent.setLogRemarkDateTime(currentClockHomeTerminalTime);
                endEvent.setIsManuallyEditedByKMBUser(true);
                endEvent.setEncompassClusterPK(0L); // generate new Encompass record

                // Setting the end time of YM/PC to the start of the next event is probably the cleanest way to handle this
                if (nextEldEvent != null) {
                    boolean isInPersonalConveyance = GlobalState.getInstance().getIsInPersonalConveyanceDutyStatus();
                    endEvent.setEventDateTime(nextEldEvent.getEventDateTime());
                    endEvent.setLatitude(EmployeeLogUtilities.GetReducedPrecisionGPSForDouble(nextEldEvent.getLatitude(), isInPersonalConveyance));
                    endEvent.setLatitudeStatusCode(nextEldEvent.getLatitudeStatusCode());
                    endEvent.setLongitude(EmployeeLogUtilities.GetReducedPrecisionGPSForDouble(nextEldEvent.getLongitude(), isInPersonalConveyance));
                    endEvent.setLongitudeStatusCode(nextEldEvent.getLongitudeStatusCode());
                    endEvent.setGpsTimestamp(nextEldEvent.getGpsTimestamp());
                    endEvent.setIsGPSAtReducedPrecision(nextEldEvent.getIsGPSAtReducedPrecision());

                    if (endEvent.getOdometer() != null && endEvent.getOdometer() >= 0 && nextEldEvent.getOdometer() != null && nextEldEvent.getOdometer() >= 0)
                        endEvent.setDistance(Math.round(nextEldEvent.getOdometer() - endEvent.getOdometer()));
                } else {
                    // determine end of day for the company
                    TimeZoneEnum timeZoneEnum = GlobalState.getInstance().getCurrentUser().getHomeTerminalTimeZone();
                    String dailyLogStart = GlobalState.getInstance().getCompanyConfigSettings(GlobalState.getInstance().getEobrService()).getDailyLogStartTime();
                    endEvent.setEventDateTime(EmployeeLogUtilities.CalculateLogEndTime(dailyLogStart, editedEvent.getEventDateTime(), timeZoneEnum));
                }

                SetEngineHoursAndAccumulatedVehicleMiles(endEvent, endEvent.getEventDateTime(), null);

                // set the distance since last valid coordinates
                statusRecord = getStatusRecord(endEvent.getEventDateTime());
                if (statusRecord != null)
                    endEvent.setDistanceSinceLastCoordinates(EventTranslationBase.calculateDistanceSinceLastValidCoordinates(statusRecord.getGpsUncertDistance()));

                // set reduced precision GPS
                if (startEventCode == EmployeeLogEldEventCode.ChangeInDriversIndication_PersonalUse && endEvent.getLatitude() != null && endEvent.getLongitude() != null) {
                    endEvent.setLatitude(EmployeeLogUtilities.GetReducedPrecisionGPSForDouble(endEvent.getLatitude(), true));
                    endEvent.setLongitude(EmployeeLogUtilities.GetReducedPrecisionGPSForDouble(endEvent.getLongitude(), true));
                    endEvent.setIsGPSAtReducedPrecision(true);
                }

                specialCategoryEventList.add(endEvent);
            } catch (Exception e) {
                String msg = String.format("EmployeeLogEldMandateController.editAutomaticDrivingToSpecialCategory Exception: {%s}", e.getMessage());
                ErrorLogHelper.RecordMessage(msg);
            }
        } else if (subStatus == Enums.SpecialDrivingCategory.Hyrail || subStatus == Enums.SpecialDrivingCategory.NonRegulated) {

            try {
                /*
                * Create the On-Duty Status event
                */
                EmployeeLogEldEvent onDutyEvent = (EmployeeLogEldEvent) editedEvent.clone();
                onDutyEvent.setPrimaryKey(-1L);    // force insert as a new Active record
                onDutyEvent.setEventSequenceIDNumber(EmployeeLogEldEnum.DEFAULT);   // force new sequence number to be generated in EmployeeLogEldEventPersist
                onDutyEvent.setEventRecordStatus(Enums.EmployeeLogEldEventRecordStatus.InactiveChangeRequested.getValue());  // acceptChangeRequest looks at status=changeRequest so temporarily change it here
                onDutyEvent.setEventRecordOrigin(2); // Edited or entered by the Driver
                onDutyEvent.setEventType(Enums.EmployeeLogEldEventType.DutyStatusChange);
                onDutyEvent.setEventCode(dutyStatusEventCode);
                onDutyEvent.setIsManuallyEditedByKMBUser(true);
                onDutyEvent.setEncompassClusterPK(0L); // generate new Encompass record

                // this is a manually edited copy so we don't want to accumulate miles
                onDutyEvent.setDistance(null);
                onDutyEvent.setEndOdometer(editedEvent.getOdometer());

                //Add log remark
                onDutyEvent.setLogRemark(startEventRemark);
                onDutyEvent.setLogRemarkDateTime(endTime);

                //Keep a reference to this newly created event so it can linked to a LogWithProvisions
                _onDutyInsertedEventDateTime = onDutyEvent.getEventDateTime();

                //Create Provisions event
                LogSpecialDrivingController specialDrivingController = subStatus == Enums.SpecialDrivingCategory.Hyrail ? new LogHyrailController(getContext()) : new LogNonRegulatedDrivingController(getContext());
                EmployeeLog employeeLog = GetEmployeeLog(new Date());

                specialDrivingController.StartSpecialDrivingStatus(editedEvent.getStartTime(), editedEvent.getLocation(), employeeLog);
                specialDrivingController.EndSpecialDrivingStatus(endTime, editedEvent.getLocation(), employeeLog);

                specialCategoryEventList.add(onDutyEvent);
            } catch (CloneNotSupportedException e) {
                String msg = String.format("EmployeeLogEldMandateController.editAutomaticDrivingToSpecialCategory Exception: {%s}", e.getMessage());
                ErrorLogHelper.RecordMessage(msg);
            }
        }

        return specialCategoryEventList;
    }

    /**
     * Returns a List<EmployeeLogEldEvent> reconciled based on requested Enum
     * Enum.Original         - returns all Active Eld Events
     * Enum.Accept_Preview   - reconciles date overlaps to return a preview of what the Eld Events would look like if you Accepted all Change Requests
     * Enum.Accept_Database  - Accepts all Change Requests and returns a database ready list of ONLY those Eld Events that need to be updated or added to the local database
     * Enum.Reject_Database  - Rejects all Change Requests and returns a database ready list of ONLY those Eld Events that need to be updated or added to the local database
     */
    public List<EmployeeLogEldEvent> getReconcileChangeRequestedEldEvents(int employeeLogKey, com.jjkeller.kmbapi.calcengine.Enums.ReconcileChangeRequestedEldEventsEnum reconcileEnum) {
        return eventFacade.getReconcileChangeRequestedEldEvents(employeeLogKey, reconcileEnum);
    }

    /**
     * Save multiple records at once so they are wrapped in the same transaction. All will succeed or fail together.
     */
    public void SaveListInSingleTransaction(EmployeeLogEldEvent[] eldEventList) {
        eventFacade.SaveListInSingleTransaction(eldEventList);
    }


    public class LocalEditedEldEventLog {
        private EmployeeLog _employeeLog;
        private EmployeeLogEldEvent _editedEldEvent;

        public EmployeeLog getEmployeeLog() {
            return _employeeLog;
        }

        public EmployeeLogEldEvent getEditedEldEvent() {
            return _editedEldEvent;
        }

        public LocalEditedEldEventLog(EmployeeLog employeeLog, EmployeeLogEldEvent editedEldEvent) {
            _employeeLog = employeeLog;
            _editedEldEvent = editedEldEvent;
        }
    }

    /**
     * When editing an Eld Event, we have to make sure it's parent EmployeeLog is a local log (LogSourceStatusEnum=1).
     * If the EmployeeLog is a Server Log (LogSourceStatusEnum=3) - we need to convert it to a local log first (LogSourceStatusEnum=1)
     * because only local versions will be submitted back to Encompass. When we do convert it to a local log, the child
     * Eld Event's will get assigned new primary keys so we need to swap the editedEldEvent old key with it's new key.
     */
    public LocalEditedEldEventLog getLocalLogForEditedEldEvent(User user, Date logDateTime, EmployeeLogEldEvent editedEldEvent) {

        EmployeeLog empLog = this.GetLocalEmployeeLog(user, logDateTime);
        if (empLog == null) {
            empLog = this.GetEmployeeLog(user, logDateTime);
            if (empLog != null) {

                // Any edits to the EmployeeLog will cause the IsCertified flag to be reset to False.
                empLog.setIsCertified(false);
                empLog.setPrimaryKey(-1L);

                SaveLocalEmployeeLog(empLog);

                if (editedEldEvent != null) {
                    EmployeeLogEldEvent origEvent = eventFacade.FetchByKey((int) editedEldEvent.getPrimaryKey());

                    // SaveLocalEmployeeLog duplicates the Log with a new PrimaryKey
                    empLog = this.GetLocalEmployeeLog(user, empLog.getLogDate());
                    editedEldEvent.setLogKey((int) empLog.getPrimaryKey());

                    // use the natural key to find the newly inserted ELD Event that corresponds to the editedEvent (same EventDateTime but different PrimaryKey)
                    if (editedEldEvent.getPrimaryKey() != -1) {
                        for (int i = 0; i < empLog.getEldEventList().getEldEventList().length; i++) {
                            EmployeeLogEldEvent localEvent = empLog.getEldEventList().getEldEventList()[i];
                            if (localEvent.getEventDateTime().equals(origEvent.getEventDateTime()) && localEvent.getEventRecordStatus() == Enums.EmployeeLogEldEventRecordStatus.Active.getValue()) {
                                editedEldEvent.setPrimaryKey(localEvent.getPrimaryKey());
                                break;
                            }
                        }
                    }
                }
            } else {
                Date startOfDay = EmployeeLogUtilities.CalculateLogStartTime(getContext(), logDateTime, this.getCurrentUser().getHomeTerminalTimeZone());
                if (startOfDay.compareTo(logDateTime) == 0) {
                    // logDateTime starts EXACTLY at the Start of Day so we don't need a filler 'Off Duty' event
                    empLog = EmployeeLogUtilities.CreateNewLog(this.getContext(), user, logDateTime, null);
                } else {
                    // create a new log, for the event, initialized with the information from the last known event
                    empLog = EmployeeLogUtilities.CreateNewLog(this.getContext(), user, logDateTime, new DutyStatusEnum(DutyStatusEnum.OFFDUTY), null);
                }

                SaveLocalEmployeeLog(empLog);
            }
        }

        return new LocalEditedEldEventLog(empLog, editedEldEvent);
    }

    /**
     * Reassign Driving event from one driver to another. Handles both SHARED and SEPARATE team driver modes.
     */
    public boolean reassignEldEventTeamDriving(int reassignEldEventPrimaryKey, int eventCodeInsteadOfDriving, GlobalState.TeamDriverModeEnum teamDriverMode, String kmbUserName, String annotation) throws Throwable {
        boolean success = true;

        try {
            final EmployeeLogEldEventFacade eventFacade = new EmployeeLogEldEventFacade(getContext(), getCurrentUser());
            EmployeeLogEldEvent teamDrivingEldEventToInactivate = eventFacade.FetchByKey(reassignEldEventPrimaryKey);
            if (teamDrivingEldEventToInactivate == null)
                throw new Exception("Team Driving Eld Event not found");

            // when editing an Eld Event, make sure it's parent EmployeeLog is a local log (LogSourceStatusEnum=1)
            LocalEditedEldEventLog localLogInfo = getLocalLogForEditedEldEvent(globalState.getCurrentUser(), teamDrivingEldEventToInactivate.getEventDateTime(), teamDrivingEldEventToInactivate);

            // mark the log being edited as non certified
            EmployeeLog employeeLog = localLogInfo.getEmployeeLog();
            employeeLog.setIsCertified(false);

            final EmployeeLogFacade empLogFacade = new EmployeeLogFacade(getContext(), getCurrentUser());

            // set employeeLog total log distance minus (event log end minus start)
            float totalLogDistance = employeeLog.getTotalLogDistance();
            float eventMiles = 0;
            if (teamDrivingEldEventToInactivate.getEndOdometer() != null && teamDrivingEldEventToInactivate.getOdometer() != null) {
                eventMiles = teamDrivingEldEventToInactivate.getEndOdometer() - teamDrivingEldEventToInactivate.getOdometer();
                float newLogDistance = totalLogDistance - eventMiles;
                employeeLog.setTotalLogDistance(newLogDistance < 0.0f ? 0.0f : newLogDistance);
            }
            empLogFacade.Save(employeeLog, 1 /*local*/);

            // create a new EmployeeLogEldEvent record for the original driver for the new duty status that was selected to replace the driving event
            teamDrivingEldEventToInactivate = localLogInfo.getEditedEldEvent();

            List<EmployeeLogEldEvent> eldEventsToPersist = new ArrayList<>();

            EmployeeLogEldEvent dutyStatusInsteadOfDriving = (EmployeeLogEldEvent) teamDrivingEldEventToInactivate.clone();
            dutyStatusInsteadOfDriving.setPrimaryKey(-1L);
            dutyStatusInsteadOfDriving.setEventSequenceIDNumber(EmployeeLogEldEnum.DEFAULT);   // force new sequence number to be generated in EmployeeLogEldEventPersist
            dutyStatusInsteadOfDriving.setEncompassClusterPK(0L);
            dutyStatusInsteadOfDriving.setEventCode(eventCodeInsteadOfDriving);
            dutyStatusInsteadOfDriving.setEventRecordOrigin(Enums.EmployeeLogEldEventRecordOrigin.EditedEnteredByDriver);
            dutyStatusInsteadOfDriving.setDistance(null);
            dutyStatusInsteadOfDriving.setOdometer(0f);
            dutyStatusInsteadOfDriving.setEndOdometer(null);
            dutyStatusInsteadOfDriving.setEventComment(annotation);
            dutyStatusInsteadOfDriving.setIsManuallyEditedByKMBUser(true);
            dutyStatusInsteadOfDriving.setEncompassClusterPK(0L); // generate new Encompass record
            eldEventsToPersist.add(dutyStatusInsteadOfDriving);

            // generate a new User for the team driver
            User teamDriverUser = UserUtility.getUserForKMBUserName(getContext(), kmbUserName);

            // determine the implied endTime for driving
            Date impliedDrivingEndTime = null;
            List<EmployeeLogEldEvent> eldEventList = eventFacade.getOriginalEldEvents(new ArrayList<>(Arrays.asList(employeeLog.getEldEventList().getEldEventList())));

            EmployeeLogEldEvent nextEldEvent = EmployeeLogUtilities.getNextActiveEventAfterDate(eldEventList, teamDrivingEldEventToInactivate.getEventDateTime());
            if (nextEldEvent == null) {
                // determine end of day for the company
                String dailyLogStart = GlobalState.getInstance().getCompanyConfigSettings(GlobalState.getInstance().getEobrService()).getDailyLogStartTime();
                TimeZoneEnum timeZone = GlobalState.getInstance().getCurrentUser().getHomeTerminalTimeZone();
                impliedDrivingEndTime = EmployeeLogUtilities.CalculateLogEndTime(dailyLogStart, teamDrivingEldEventToInactivate.getEventDateTime(), timeZone);
            } else {
                impliedDrivingEndTime = nextEldEvent.getEventDateTime();
            }

            // Reassign based on Shared or Separates devices
            if (teamDriverMode == GlobalState.TeamDriverModeEnum.SHAREDDEVICE) {
                eldEventsToPersist.addAll(reassignEldEventTeamDrivingSharedMode(teamDrivingEldEventToInactivate, teamDriverUser, impliedDrivingEndTime, annotation));
            } else {
                // The reassignment object will only contain relatedEventKey (which will be used to retrieve the
                // original event when it is passed to DMO).  But we will need some additional info filled in
                // for the reassignment process when using separate devices.
                teamDrivingEldEventToInactivate.setDistance((int) eventMiles);
                long duration = Math.abs(teamDrivingEldEventToInactivate.getEventDateTime().getTime() - impliedDrivingEndTime.getTime());
                teamDrivingEldEventToInactivate.setEditDuration(duration);

                success = reassignEldEventTeamDrivingSeparateMode(teamDrivingEldEventToInactivate, teamDriverUser.getCredentials().getEmployeeId(), annotation);
            }

            // set the EventRecordStatus of the driving event for the current driver to “Inactive – Changed”
            teamDrivingEldEventToInactivate.setEventRecordStatus(Enums.EmployeeLogEldEventRecordStatus.InactiveChanged.getValue());
            teamDrivingEldEventToInactivate.setEventComment(annotation);
            teamDrivingEldEventToInactivate.setIsManuallyEditedByKMBUser(true);
            eldEventsToPersist.add(teamDrivingEldEventToInactivate);

            EmployeeLogEldEvent[] persistList = new EmployeeLogEldEvent[eldEventsToPersist.size()];
            eldEventsToPersist.toArray(persistList);

            // save multiple records at once so they are wrapped in the same transaction
            eventFacade.SaveListInSingleTransaction(persistList);

            // If editing today's login, refresh global state otherwise logout saves global log and will overwrite changes
            updateGlobalStateEmployeeLogs(globalState.getCurrentUser(), teamDrivingEldEventToInactivate.getEventDateTime());

        } catch (Exception e) {
            String msg = String.format("%s failure to reassign Team Driving event: {%s}", "EmployeeLogEldMandateController::reassignEldEventTeamDriving", e.getMessage());
            Log.d("MandateCtl.reassignEldE", msg);
            ErrorLogHelper.RecordMessage(this.getContext(), msg);
            throw e;
        }

        return success;
    }

    /**
     * The team driver that is selected from that list is currently one of the logged in users,
     * then you have a team driver in shared device mode – the reassignment can occur directly on the device without submitting anything to Encompass.
     */
    private List<EmployeeLogEldEvent> reassignEldEventTeamDrivingSharedMode(EmployeeLogEldEvent teamDrivingEldEventToInactivate, User teamDriverUser, Date impliedDrivingEndTime, String annotation) throws Throwable {

        // when editing an Eld Event, make sure it's parent EmployeeLog is a local log (LogSourceStatusEnum=1)
        LocalEditedEldEventLog localLogInfo = getLocalLogForEditedEldEvent(teamDriverUser, teamDrivingEldEventToInactivate.getEventDateTime(), null);

        // mark the log being edited as non certified
        EmployeeLog employeeLog = localLogInfo.getEmployeeLog();
        employeeLog.setIsCertified(false);

        final EmployeeLogFacade empLogFacade = new EmployeeLogFacade(getContext(), teamDriverUser);

        // set employeeLog total log distance minus (event log end minus start)
        float totalLogDistance = employeeLog.getTotalLogDistance();
        if (teamDrivingEldEventToInactivate.getEndOdometer() != null && teamDrivingEldEventToInactivate.getOdometer() != null) {
            float eventMiles = teamDrivingEldEventToInactivate.getEndOdometer() - teamDrivingEldEventToInactivate.getOdometer();
            employeeLog.setTotalLogDistance(totalLogDistance + eventMiles);
        }
        empLogFacade.Save(employeeLog, 1 /*local*/);

        EmployeeLogEldEvent drivingEldEventToReassign = (EmployeeLogEldEvent) teamDrivingEldEventToInactivate.clone();

        drivingEldEventToReassign.setPrimaryKey(-1L);   // force insert
        drivingEldEventToReassign.setEventSequenceIDNumber(EmployeeLogEldEnum.DEFAULT);   // force new sequence number to be generated in EmployeeLogEldEventPersist
        drivingEldEventToReassign.setLogKey((int) employeeLog.getPrimaryKey());
        drivingEldEventToReassign.setEncompassClusterPK(0L);
        drivingEldEventToReassign.setEventRecordOrigin(Enums.EmployeeLogEldEventRecordOrigin.EditedEnteredByDriver);
        drivingEldEventToReassign.setEventComment(annotation);
        drivingEldEventToReassign.setIsManuallyEditedByKMBUser(true);
        drivingEldEventToReassign.setDriverOriginatorUserId(teamDriverUser.getCredentials().getEmployeeId());
        drivingEldEventToReassign.setRuleSet(teamDriverUser.getRulesetTypeEnum());
        drivingEldEventToReassign.setEncompassClusterPK(0L); // generate new Encompass record

        // if reassigning to a driver in a different Home TimeZone - adjust event time
        // using a hack... this time really should not be adjusted, but needs to because while
        // the Java Date object sort of has a timezone on it, the timezone is not kept when we persist the value
        // so we have to hack this in
        if (teamDriverUser.getCredentials().getEmployeeId() != globalState.getCurrentUser().getCredentials().getEmployeeId()) {
            if (teamDriverUser.getHomeTerminalTimeZone().getValue() != globalState.getCurrentUser().getHomeTerminalTimeZone().getValue()) {
                drivingEldEventToReassign.setEventDateTime(DateUtility.convertToTimezone(drivingEldEventToReassign.getEventDateTime(), teamDriverUser.getHomeTerminalTimeZone().toTimeZone()));
                drivingEldEventToReassign.setGpsTimestamp(DateUtility.convertToTimezone(drivingEldEventToReassign.getGpsTimestamp(), teamDriverUser.getHomeTerminalTimeZone().toTimeZone()));
                impliedDrivingEndTime = DateUtility.convertToTimezone(impliedDrivingEndTime, teamDriverUser.getHomeTerminalTimeZone().toTimeZone());
            }
        }

        // calculate EditDuration (timespan in milliseconds)
        long duration = Math.abs(drivingEldEventToReassign.getEventDateTime().getTime() - impliedDrivingEndTime.getTime());
        drivingEldEventToReassign.setEditDuration(duration);

        List<EmployeeLogEldEvent> eldEventList = new ArrayList<>(Arrays.asList(employeeLog.getEldEventList().getEldEventList()));

        eldEventList = reconcileEvents(drivingEldEventToReassign, drivingEldEventToReassign.getEventDateTime(), impliedDrivingEndTime, eldEventList, false);

        // find any Intermediate events
        List<EmployeeLogEldEvent> intermediateEvents = getAssociatedIntermediateEvents(drivingEldEventToReassign.getEventDateTime(), impliedDrivingEndTime, teamDrivingEldEventToInactivate.getLogKey());
        for (EmployeeLogEldEvent e : intermediateEvents) {
            try {
                // add new event record to 'eldEventList'
                EmployeeLogEldEvent eventToSave = e;
                eventToSave.setPrimaryKey(-1L);
                eventToSave.setEventRecordStatus(Enums.EmployeeLogEldEventRecordStatus.InactiveChangeRequested.getValue());
                eventToSave.setEventSequenceIDNumber(EmployeeLogEldEnum.DEFAULT);   // force new sequence number to be generated in EmployeeLogEldEventPersist
                eventToSave.setEventRecordOrigin(Enums.EmployeeLogEldEventRecordOrigin.EditedEnteredByDriver);
                eventToSave.setDriverOriginatorUserId(teamDriverUser.getCredentials().getEmployeeId());
                eventToSave.setEncompassClusterPK(0L);
                eventToSave.setEventComment(annotation);
                eventToSave.setLogKey((int) employeeLog.getPrimaryKey());
                eldEventList.add(eventToSave);
                // update existing record (set inactive change requested)
                e.setEventRecordStatus(Enums.EmployeeLogEldEventRecordStatus.InactiveChangeRequested.getValue());
                eldEventList.add(e);
            } catch (Exception ex) {
                //do something TODO
                ErrorLogHelper.RecordException(ex);
            }
        }

        //inactive events who's time is overlapped by a change request
        final EmployeeLogEldEventFacade eventFacade = new EmployeeLogEldEventFacade(getContext(), teamDriverUser);
        eldEventList = eventFacade.handleChangeRequestsThatOverlapActiveEvents(eldEventList, null);

        //return list of items which need to be updated in database
        eldEventList = eventFacade.eldEventsToUpdateInDatabase(eldEventList, false);

        // when that driver logs into the mobile app or out, he/she will be prompted with edits to
        // review and will be able to accept or reject the driving period from team driver 1.
        for (EmployeeLogEldEvent event : eldEventList) {
            event.setEventRecordStatus(Enums.EmployeeLogEldEventRecordStatus.InactiveChangeRequested.getValue());
        }

        return eldEventList;
    }

    /**
     * Return a list of eld INTERMEDIATE events that fall between a start/end driving event
     */
    public List<EmployeeLogEldEvent> getAssociatedIntermediateEvents(Date startDate, Date endDate, int logKey) {
        List<EmployeeLogEldEvent> returnEvents = new ArrayList<>();
        EmployeeLogEldEvent[] allIntermediateEldEventsArray = GetIntermediateEventsByEmployeeLogKey(logKey);
        List<EmployeeLogEldEvent> intermediateEvents = Arrays.asList(allIntermediateEldEventsArray);

        for (EmployeeLogEldEvent e : intermediateEvents) {
            if (e.getEventDateTime().getTime() > startDate.getTime() && e.getEventDateTime().getTime() < endDate.getTime()) {
                returnEvents.add(e);
            }
        }

        return returnEvents;
    }

    /**
     * If the team driver that is selected is not currently one of the logged in users, then you have a team driver in separate device mode and
     * that requires submitting to Encompass to perform the reassignment.
     */
    private boolean reassignEldEventTeamDrivingSeparateMode(EmployeeLogEldEvent reassignEvent, String driverToAssignEventTo, String annotation) {

        // Create the reassignment object to pass
        DrivingEventReassignmentMapping reassignment = new DrivingEventReassignmentMapping();
        reassignment.setRelatedEvent((int) reassignEvent.getPrimaryKey());
        reassignment.setEldEvent(reassignEvent);
        reassignment.setDriverToAssignEventTo(driverToAssignEventTo);
        reassignment.setEventComment(annotation);
        reassignment.setIsSubmitted(false);

        // Send reassignment info to Encompass to create the new driving event for the "assigned to" driver.
        TeamDriverController ctrl = new TeamDriverController(getContext());

        return ctrl.SendReassignmentRequest(reassignment);
    }

    /**
     * If editing today's login, refresh global state otherwise logout saves global log and will overwrite changes
     */
    private void updateGlobalStateEmployeeLogs(User user, Date logDate) {
        EmployeeLog currentEmpLog = this.GetLocalEmployeeLog(user, logDate);

        EmployeeLog globalStateCurrentEmployeeLog = GlobalState.getInstance().getCurrentEmployeeLog();
        if (globalStateCurrentEmployeeLog != null && globalStateCurrentEmployeeLog.getPrimaryKey() == currentEmpLog.getPrimaryKey()) {
            GlobalState.getInstance().setCurrentEmployeeLog(currentEmpLog);
        }

        EmployeeLog globalStateCurrentDriversLog = GlobalState.getInstance().getCurrentDriversLog();
        if (globalStateCurrentDriversLog != null && globalStateCurrentDriversLog.getPrimaryKey() == currentEmpLog.getPrimaryKey()) {
            GlobalState.getInstance().setCurrentDriversLog(currentEmpLog);
        }
    }

    private StatusRecord getLatestStatusRecord() {
        return getLatestStatusRecord(true);
    }

    /***
     * this should be used to query the tab for the most current status record... when we want the "now" data
     * Per Bruce L the 2 step process should be better than the search technique.
     * with a small data set on the tab, the one search query (get historical data by record id with id 0xffffffff) will be faster than one query.
     * but with more data the search will be slower and less reliable.
     *
     * @return status latest tatus record
     */
    private StatusRecord getLatestStatusRecord(boolean performDataRecordingCheck) {
        if (EobrReader.getIsEobrDevicePhysicallyConnected()) {
            StatusRecord statusRecord = new StatusRecord();
            EobrResponse<StatusBuffer> statusBuffer = eobrReader.GetStatusBuffer();
            int lastEobrId = statusBuffer.getData().getLastEobrId();
            int resultCode = eobrReader.Technician_GetHistoricalData(statusRecord, lastEobrId, performDataRecordingCheck);
            if (resultCode == EobrReturnCode.S_SUCCESS && !statusRecord.IsBlank()) {
                return statusRecord;
            }

        } else {
            Log.e(LOG_TAG, "The Eobr Device is not connected");
        }
        return null;
    }

    /**
     * Mandate implementation of TripInfo generation for a given log
     *
     * @param employeeLog Employee Log object to pull trip info from
     * @return Map of PropertyName/Strings representing trip info
     */
    @Override
    public HashMap<EmployeeLog.TripInfoPropertyKey, HashSet<String>> GetTripInfoForLog(EmployeeLog employeeLog) {
        //Construct hashmap to be used by derived class implementations
        HashMap<EmployeeLog.TripInfoPropertyKey, HashSet<String>> tripInfoHashMap = super.GetTripInfoForLog(employeeLog);
        /*
        Get array of current eld events on log. If the EldEventList object isn't null, pull the
		events into the array. Otherwise, just provide null here and fall into default behavior
		 */
        EmployeeLogEldEvent[] currentEventList = employeeLog.getEldEventList() != null ? employeeLog.getEldEventList().getEldEventList() : null;
        if (currentEventList != null) {
            for (EmployeeLogEldEvent evt : currentEventList) {
                if (evt.getShipmentInfo() != null)
                    tripInfoHashMap.get(EmployeeLog.TripInfoPropertyKey.ShipmentInfo).add(evt.getShipmentInfo());
                if (evt.getTractorNumber() != null)
                    tripInfoHashMap.get(EmployeeLog.TripInfoPropertyKey.TractorNumber).add(evt.getTractorNumber());
                if (evt.getTrailerNumber() != null)
                    tripInfoHashMap.get(EmployeeLog.TripInfoPropertyKey.TrailerNumber).add(evt.getTrailerNumber());
                if (evt.getTrailerPlate() != null)
                    tripInfoHashMap.get(EmployeeLog.TripInfoPropertyKey.TrailerPlate).add(evt.getTrailerPlate());
                if (evt.getVehiclePlate() != null)
                    tripInfoHashMap.get(EmployeeLog.TripInfoPropertyKey.VehiclePlate).add(evt.getVehiclePlate());
            }
        }
        return tripInfoHashMap;
    }

    @Override
    public EmployeeLogEldEvent[] GetAllEventsByEmployeeLogKey(long employeeLogKey) {
        EmployeeLogEldEvent[] eldEventArray = this.fetchEldEventsByEventTypes(
                (int) employeeLogKey,
                Arrays.asList(
                        com.jjkeller.kmbapi.employeelogeldevents.Enums.EmployeeLogEldEventType.DutyStatusChange.getValue(),
                        com.jjkeller.kmbapi.employeelogeldevents.Enums.EmployeeLogEldEventType.IntermediateLog.getValue(),
                        com.jjkeller.kmbapi.employeelogeldevents.Enums.EmployeeLogEldEventType.ChangeInDriversIndication.getValue(),
                        com.jjkeller.kmbapi.employeelogeldevents.Enums.EmployeeLogEldEventType.Certification.getValue(),
                        com.jjkeller.kmbapi.employeelogeldevents.Enums.EmployeeLogEldEventType.LoginLogout.getValue(),
                        com.jjkeller.kmbapi.employeelogeldevents.Enums.EmployeeLogEldEventType.EnginePowerUpPowerDown.getValue(),
                        com.jjkeller.kmbapi.employeelogeldevents.Enums.EmployeeLogEldEventType.Malfunction_DataDiagnosticDetection.getValue()
                ),
                Arrays.asList(
                        com.jjkeller.kmbapi.employeelogeldevents.Enums.EmployeeLogEldEventRecordStatus.Active.getValue(),
                        com.jjkeller.kmbapi.employeelogeldevents.Enums.EmployeeLogEldEventRecordStatus.InactiveChanged.getValue(),
                        com.jjkeller.kmbapi.employeelogeldevents.Enums.EmployeeLogEldEventRecordStatus.InactiveChangeRejected.getValue(),
                        com.jjkeller.kmbapi.employeelogeldevents.Enums.EmployeeLogEldEventRecordStatus.InactiveChangeRequested.getValue()
                )
        );

        return eldEventArray;
    }

    public EmployeeLogEldEvent[] GetIntermediateEventsByEmployeeLogKey(long employeeLogKey) {
        EmployeeLogEldEvent[] eldEventArray = this.fetchEldEventsByEventTypes(
                (int) employeeLogKey,
                Arrays.asList(
                        com.jjkeller.kmbapi.employeelogeldevents.Enums.EmployeeLogEldEventType.IntermediateLog.getValue()
                ),
                Arrays.asList(
                        com.jjkeller.kmbapi.employeelogeldevents.Enums.EmployeeLogEldEventRecordStatus.Active.getValue()
                )
        );

        return eldEventArray;
    }
    /**
     * Get unidentified ELD events and return a filtered list of paired events for Auto-Assign UE
     * @return
     */
    public List<UnidentifiedPairedEvents> LoadUnreviewedWithoutConfidenceLevelEvents(){
        List<EmployeeLogEldEvent> unidentifiedEvents = fetchUnreviewedWithoutConfidenceLevelEvents();

        return processUnidentifiedEldEventPairs(unidentifiedEvents);
    }

    /**
     * Get unidentified ELD events and return a list of paired events for the claim
     * unidentified ELD events screen.
     * @param _showAllUnsubmitted
     * @return
     */
    public List<UnidentifiedPairedEvents> LoadUnidentifiedEldEventPairs(boolean _showAllUnsubmitted){
        List<EmployeeLogEldEvent> unidentifiedEvents = null;

        // Fetch UEE records to populate the listview.  If an intent is found to indicate we should be showing all unsubmitted events, load them,
        // otherwise only load those that haven't been reviewed yet.
        if (_showAllUnsubmitted) {
            unidentifiedEvents =  fetchUnsubmittedUnidentifiedEvents();
        } else {
            unidentifiedEvents = fetchUnreviewedUnidentifiedEvents();
        }

      return processUnidentifiedEldEventPairs(unidentifiedEvents);
    }

    private List<UnidentifiedPairedEvents> processUnidentifiedEldEventPairs(List<EmployeeLogEldEvent> unidentifiedEvents){
        List<UnidentifiedPairedEvents> pairResult = new ArrayList<>();
        // unidentifiedEvents contain Drive and OnDuty events ordered EventDateTime DESC, EventSequenceIdNumber DESC
        //     - OnDuty and Drive will always be in pairs. The claiming process should just allow us to claim the driving event.
        EmployeeLogEldEvent onDutyEvent = null;

        for (int i = 0; i < unidentifiedEvents.size(); i++) {
            EmployeeLogEldEvent event = unidentifiedEvents.get(i);

            // only concerned with Duty Status events
            if (event.getEventType() != Enums.EmployeeLogEldEventType.DutyStatusChange) {
                continue;
            }

            // pair OnDuty and Drive event
            if (event.getEventCode() == EmployeeLogEldEventCode.DutyStatus_OnDuty) {
                int lookAheadIndex = 1;
                while (i + lookAheadIndex < unidentifiedEvents.size()) {
                    // look ahead to see if the next event is the start Drive event

                    EmployeeLogEldEvent nextEvent = unidentifiedEvents.get(i + lookAheadIndex);
                    if (nextEvent.getEventType() == Enums.EmployeeLogEldEventType.DutyStatusChange && nextEvent.getEventCode() == EmployeeLogEldEventCode.DutyStatus_Driving) {
                        // found the Drive/OnDuty pair -- now determine if the Drive will overlap existing automatic Drive but with a DIFFERENT Serial Number
                        // (see scenario described in method willUndentifiedDriveOverlapAutomaticDriveWithDifferentSerialNumber)
                        if (willUndentifiedDriveOverlapAutomaticDriveWithDifferentSerialNumber(nextEvent, event)) {
                            if (nextEvent.getIsReviewed() == false) {
                                EmployeeLogEldEvent[] overlapEventsToMarkAsReviewed = new EmployeeLogEldEvent[2];    // single array entry contains BOTH Drive and corresponding OnDuty event

                                // Mark Unidentified Events from another ELD as Reviewed so they will be submitted to Encompass to be claimed for another driver
                                nextEvent.setIsReviewed(true);
                                event.setIsReviewed(true);
                                overlapEventsToMarkAsReviewed[0] = nextEvent;    // Drive event
                                overlapEventsToMarkAsReviewed[1] = event;        // OnDuty event

                                eventFacade.SaveListInSingleTransaction(overlapEventsToMarkAsReviewed);
                            }

                        } else {
                            pairResult.add(new UnidentifiedPairedEvents(nextEvent, event));
                        }
                        break;
                    }
                    else { lookAheadIndex++; }  // Advance to look at the next record;
                }
            }
        }

        return pairResult;
    }

    /**
     * Scenario - Driver is in Vehicle 1 in the morning and Drives from 8-10; in the afternoon, hops in Vehicle 2 (different ELD) and Reading History generates
     * Unidentified Events which includes Drive from 8-10. Since Vehicle's 2 Unidentified Events overlap the automatically recorded Drive events, you won't be
     * allowed to claim them. We decided that if the overlaps are from another ELD Serial Number (i.e. Vehicle's 2 ELD) then weed them out and don't even present
     * them to the user to claim -- they will be uploaded to Encompass during SubmitLogs and an Encompass Admin will have to Claim them for the another driver.
     */
    private boolean willUndentifiedDriveOverlapAutomaticDriveWithDifferentSerialNumber(EmployeeLogEldEvent automaticDrive, EmployeeLogEldEvent endOnDuty) {

        User user = GlobalState.getInstance().getCurrentUser();

        if (automaticDrive != null && endOnDuty != null) {
            // get the parent EmployeeLog for the Drive event
            EmployeeLogEldMandateController.LocalEditedEldEventLog localEditedEldEventLog = getLocalLogForEditedEldEvent(user, automaticDrive.getStartTime(), null);
            EmployeeLog parentLog = localEditedEldEventLog.getEmployeeLog();

            if (parentLog != null) {
                Date nextLogStart = DateUtility.AddDays(parentLog.getLogDate(), 1);
                Date currentLogEndOfDay = DateUtility.AddMilliseconds(nextLogStart, -1);

                try {

                    // check if the Claim Drive will overlap automatically recorded driving time but with a different Eobr Serial Number
                    boolean drivingAcrossEndOfDay = endOnDuty.getEventDateTime() != null && endOnDuty.getEventDateTime().after(nextLogStart);

                    // check if parent log has any automatically generated Drive events that will be overlapped if we Claim this Drive event with a different Eobr Serial Number
                    if (willClaimInvalidateAutomaticDriveTimeFromAnotherEobrSerialNumber(parentLog, automaticDrive.getPrimaryKey(), automaticDrive.getEventDateTime(), drivingAcrossEndOfDay ? currentLogEndOfDay : endOnDuty.getEventDateTime(), automaticDrive.getEobrSerialNumber())) {
                        return true;    // don't show this Drive/OnDuty to be claimed since it overlaps existing automatic Drive but with a different Eobr Serial Number
                    }

                    // if the Drive spans the End of Day - check to see if the Drive event will overlap any automatic Drive time on the next day's log
                    if (drivingAcrossEndOfDay) {

                        // get next day's parent log
                        parentLog = GetLocalEmployeeLog(user, nextLogStart);
                        if (parentLog != null) {	// if null - there will be no ELD Events so no overlaps can exist
                            // check if the Claim Drive will overlap automatically recorded driving time but with a different Eobr Serial Number
                            if (willClaimInvalidateAutomaticDriveTimeFromAnotherEobrSerialNumber(parentLog, automaticDrive.getPrimaryKey(), nextLogStart, endOnDuty.getEventDateTime(), automaticDrive.getEobrSerialNumber())) {
                                return true;    // don't show this Drive/OnDuty to be claimed since it overlaps existing automatic Drive but with a different Eobr Serial Number
                            }
                        }
                    }
                } catch (Throwable throwable) {
                    ErrorLogHelper.RecordException(throwable);
                    throw new RuntimeException(throwable);
                }
            }
        }

        return false;
    }

    /**
     *  Determine if the Claim Drive will overlap automatically recorded driving time but with a different Eobr Serial Number
     */
    private boolean willClaimInvalidateAutomaticDriveTimeFromAnotherEobrSerialNumber(EmployeeLog parentLog, Long drivePrimaryKey, Date startTime, Date endTime, String driveEobrSerialNumber) throws Throwable {
        EmployeeLogEldEvent[] eldEventArray = fetchEldEventsByEventTypes((int) parentLog.getPrimaryKey(),
                Arrays.asList(Enums.EmployeeLogEldEventType.DutyStatusChange.getValue()),
                Arrays.asList(Enums.EmployeeLogEldEventRecordStatus.Active.getValue()));

        return willEditInvalidateAutomaticDriveTime(eldEventArray, drivePrimaryKey, startTime, endTime, driveEobrSerialNumber) == EmployeeLogEldMandateController.InvalidateAutomaticDriveTimeEnum.OVERLAP_DIFFERENT_SERIALNUMBER;
    }

    /**
     * Submits all unidentified ELD events to DMO that haven't been submitted yet.
     *
     * @return true if the records were submitted successfully and false otherwise
     */
    public boolean SubmitUnidentifiedEldEvents() {
        final EmployeeLogEldEventFacade facade = new EmployeeLogEldEventFacade(getContext(), getCurrentUser());
        final List<EmployeeLogEldEvent> unsubmittedEvents = facade.GetUnsubmittedUnidentifiedEventsToSubmitLogs();
        final RESTWebServiceHelper webServiceHelper = new RESTWebServiceHelper(getContext());
        return runInBatches(unsubmittedEvents, 50, new CodeBlocks.Func1<List<EmployeeLogEldEvent>, Boolean>() {
            @Override
            public Boolean execute(List<EmployeeLogEldEvent> batch) {
                try {
                    webServiceHelper.SubmitUnidentifiedEldEvents(batch.toArray(new EmployeeLogEldEvent[batch.size()]));
                    markUnidentifiedEventsAsSubmitted(batch, facade);
                    return true;
                } catch (IOException e) {
                    HandleException(e, "SubmitUnidentifiedEldEvents");
                    return false;
                }
            }
        });
    }

    @Override
    public void SynchronizeUnclaimedEvents(User user) throws KmbApplicationException {
        final EmployeeLogEldEventFacade facade = new EmployeeLogEldEventFacade(getContext(), getCurrentUser());
        final List<EmployeeLogEldEvent> localEvents = facade.GetUnsynchronizedUnidentifiedEvents();

        if (localEvents != null && localEvents.size() > 0) {
            Collections.sort(localEvents, new Comparator<EmployeeLogEldEvent>() {
                @Override
                public int compare(EmployeeLogEldEvent p1, EmployeeLogEldEvent p2) {
                    return p1.getEventDateTime().compareTo(p2.getEventDateTime()); // Ascending
                }
            });

            Date startDate = localEvents.get(0).getEventDateTime();
            Date endDate = localEvents.get(localEvents.size() - 1).getEventDateTime();

            EmployeeLogEldEvent[] serverEvents = null;
            try {
                RESTWebServiceHelper webServiceHelper = new RESTWebServiceHelper(getContext());
                EmployeeLogEldEventList serverEventList = webServiceHelper.GetUnidentifiedEldEventsInDateRange(startDate, endDate);
                serverEvents = serverEventList.getEldEventList();
            } catch (IOException ioe) {
                this.HandleExceptionAndThrow(ioe, this.getContext().getString(R.string.downloademployeelogs), this.getContext().getString(R.string.exception_webservicecommerror), this.getContext().getString(R.string.exception_serviceunavailable));
            }

            if (serverEvents != null && serverEvents.length > 0) {
                for (EmployeeLogEldEvent localEvent : localEvents) {
                    for (EmployeeLogEldEvent serverEvent : serverEvents)
                    {
                        if (serverEvent.getEventSequenceIDNumber() == localEvent.getEventSequenceIDNumber()) {
                            localEvent.setEncompassClusterPK(serverEvent.getEncompassClusterPK());
                            facade.Save(localEvent);
                            break;
                        }
                    }
                }
            }
        }

        // Do not update the unidentified events when logging out.
        if(!this.globalState.getIsUserLoggingOut()) {
            UpdateUnidentifiedEvents();
        }
    }

    public int DownloadLogs(User user, Date startDate, Date endDate) throws KmbApplicationException {
        int logsDownloaded = super.DownloadLogs(user, startDate, endDate);
        try {
            RESTWebServiceHelper rwsh = new RESTWebServiceHelper(getContext());
            EmployeeLogWithProvisions[] provisions = rwsh.getEmployeeLogWithProvisions(user.getCredentials().getEmployeeId(), startDate, endDate);
            EmployeeLogWithProvisionsFacade provFacade = new EmployeeLogWithProvisionsFacade(getContext());

            HashMap<Date, EmployeeLog> logHashMap = new HashMap<Date, EmployeeLog>();
            EmployeeLogFacade logFacade = new EmployeeLogFacade(getContext(), GlobalState.getInstance().getCurrentUser());

            for(EmployeeLogWithProvisions prov : provisions){
                if(logHashMap.get(prov.getLogDate()) == null){
                    logHashMap.put(prov.getLogDate(), logFacade.GetLocalLogByDate(prov.getLogDate()));
                }
                EmployeeLog localLog = logHashMap.get(prov.getLogDate());
                if(localLog != null) {
                    prov.setEmployeeLogEldEventId(findLogEventForProvision(localLog, prov.getStartTime()));
                    provFacade.Save(prov, localLog);
                }
            }
        } catch (IOException ex){
            Log.e("DownloadLogs", "error downloading provisions", ex);
        }
        return logsDownloaded;
    }

    private void UpdateUnidentifiedEvents() throws KmbApplicationException {
        final EmployeeLogEldEventFacade facade = new EmployeeLogEldEventFacade(getContext(), getCurrentUser());

        List<EmployeeLogEldEvent> submittedEvents = facade.GetSubmittedUnidentifiedEvents();
        if (submittedEvents != null && submittedEvents.size() > 0) {
            Long clusterPKs[] = new Long[submittedEvents.size()];
            int count = 0;

            for (EmployeeLogEldEvent submittedEvent : submittedEvents) {
                clusterPKs[count] = submittedEvent.getEncompassClusterPK();
                count += 1;
            }

            EmployeeLogUnidentifiedELDEventStatus[] eventStatusList = null;
            try {
                RESTWebServiceHelper webServiceHelper = new RESTWebServiceHelper(getContext());
                eventStatusList = webServiceHelper.GetSubmittedUnidentifiedEldEventStatus(clusterPKs);
            } catch (IOException ioe) {
                this.HandleExceptionAndThrow(ioe, this.getContext().getString(R.string.updateunidentifiedevents), this.getContext().getString(R.string.exception_webservicecommerror), this.getContext().getString(R.string.exception_serviceunavailable));
            }

            boolean runClearMethod = false;
            if (eventStatusList != null && eventStatusList.length > 0) {
                for (EmployeeLogUnidentifiedELDEventStatus eventStatus : eventStatusList) {
                    for (EmployeeLogEldEvent submittedEvent : submittedEvents) {
                        if (eventStatus.getClusterPK() == submittedEvent.getEncompassClusterPK()) {
                            submittedEvent.setUnidentifiedEventStatus(UnidentifiedEldEventStatus.CLAIMED);
                            facade.Save(submittedEvent);
                            runClearMethod = true;
                            break;
                        }
                    }
                }
            }
            if(runClearMethod) {
                // Try to clear the Unidentified Driving Data Diagnostic Event
                TryClearUnidentifiedDrivingTimeDataDiagnosticEvent();
            }
        }
    }


    /**
     * Takes batches from the given list and runs a listener for each batch.
     * Returns false if the listener returns false for any of the batches.
     * Returns true otherwise.
     *
     * @param list      the list to break into batches
     * @param batchSize the size of each batch
     * @param onBatch   The listener to run for each batch. If any runs of the listener return false, the returned overall result will be false.
     * @return false if any batches returned false. otherwise, true is returned.
     */
    private <T> boolean runInBatches(final List<T> list, final int batchSize, final CodeBlocks.Func1<List<T>, Boolean> onBatch) {
        boolean allSuccessful = true;
        if (list != null && !list.isEmpty()) {
            for (int startIndex = 0; startIndex < list.size(); startIndex += batchSize) {
                int endIndex = Math.min(list.size(), startIndex + batchSize);
                Boolean batchResult = onBatch.execute(list.subList(startIndex, endIndex));
                if (batchResult != null && !batchResult) {
                    allSuccessful = false;
                }
            }
        }
        return allSuccessful;
    }

    private void markUnidentifiedEventsAsSubmitted(List<EmployeeLogEldEvent> unidentifiedEvents, EmployeeLogEldEventFacade facade) {
        for (EmployeeLogEldEvent event : unidentifiedEvents) {
            event.setUnidentifiedEventStatus(UnidentifiedEldEventStatus.SUBMITTED);
            facade.Save(event);
        }
    }

    @Override
    public void driverElectedToContinueDriving() {
        //end drive...
        EmployeeLog log = GlobalState.getInstance().getCurrentDriversLog();
        DutyStatusEnum newDutyStatus = DutyStatusEnum.valueOfDMOEnum(DutyStatusEnum.DmoEnum_Driving);
        RuleSetTypeEnum ruleSetTypeEnum = log.getRuleset();
        Location location = GlobalState.getInstance().getLastLocation();

        try {
            //if this crossed midnight, logic elsewhere handles the transition
            Date stopTime = GlobalState.getInstance().getPotentialDrivingStopTimestamp();
            if(DateUtility.IsSameDay(log.getLogDate(), stopTime)) {
                this.saveDutyStatusChangeEvent(log, stopTime, newDutyStatus, location,
                        true, ruleSetTypeEnum, null, null,
                        true, null, null);
            } else {
                User currentDriver = GlobalState.getInstance().getCurrentDesignatedDriver();
                Date yesterdaysLogDate = DateUtility.AddDays(log.getLogDate(), -1);
                EmployeeLog yesterdaysLog = this.GetEmployeeLog(currentDriver, yesterdaysLogDate);

                this.saveDutyStatusChangeEvent(yesterdaysLog, stopTime, newDutyStatus, location,
                        true, ruleSetTypeEnum, null, null,
                        true, null, null);

                EmployeeLogEldEvent[] events = log.getEldEventList().getActiveEldEventList(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus);
                if(events.length == 1) {
                    EmployeeLogEldEvent todayEvent = events[0];
                    if (todayEvent.getDutyStatusEnum().getValue() == DutyStatusEnum.DRIVING) {
                        todayEvent.setEventRecordOrigin(Enums.EmployeeLogEldEventRecordOrigin.EditedEnteredByDriver);
                        todayEvent.setLocation(location);

                        this.SaveLocalEmployeeLog(currentDriver, log);
                    }
                }
            }
        } catch (Throwable t) {
            throw new RuntimeException("Error in stop driving prompt answered", t);
        }
    }

    public boolean isLogCreatedForDate(Date logDate) {
        final EmployeeLogFacade empLogFacade = new EmployeeLogFacade(getContext(), getCurrentUser());
        int count = empLogFacade.FetchLogCount(logDate, logDate);
        return count == 1;
    }

    /**
     * Clears the missing data diagnostic if the MissingDataDiagnostic event was caused by the given event and the event is now clear.
     * This should only be called on the latest event generated.
     * @param employeeLog EmployeeLog to check for the missing data diagnostic event.
     * @param eventTriggered EldEvent to test against
     */
    public void updateMissingDataDiagnosticForEvent(EmployeeLog employeeLog, EmployeeLogEldEvent eventTriggered) throws Throwable {
        EventDataDiagnosticsChecker dataDiagnosticsChecker = new EventDataDiagnosticsChecker();

        if (dataDiagnosticsChecker.CheckForError(eventTriggered, eventTriggered.isManualDrivingEvent(), null) == DataDiagnosticEnum.MISSING_REQUIRED_DATA_ELEMENTS
                || !getActiveDataDiagnostics(employeeLog).contains(DataDiagnosticEnum.MISSING_REQUIRED_DATA_ELEMENTS)){
            //This can only clear an active missing data diagnostic event.
            //If the event is still missing data or if there's not an active missing data diagnostic... just return.
            return;
        }

        //Grab the most recent data diagnostic issue.
        int key = employeeLog == null ? -1 : (int) employeeLog.getPrimaryKey();
        final EmployeeLogEldEventFacade facade = new EmployeeLogEldEventFacade(getContext(), getCurrentUser());

        EmployeeLogEldEvent[] logEldEventArray = facade.GetByEventTypes(key,
                Arrays.asList(new Integer[]{Enums.EmployeeLogEldEventType.Malfunction_DataDiagnosticDetection.getValue()}),
                Arrays.asList(new Integer[]{Enums.EmployeeLogEldEventRecordStatus.Active.getValue()})
        );

        if (logEldEventArray.length > 0) {
            //Remove inspection to show this has to go from the start index
            //noinspection ForLoopReplaceableByForEach
            for (int i = logEldEventArray.length - 1; i >= 0; i--) {
                EmployeeLogEldEvent currentEvent = logEldEventArray[i];
                if (logEldEventArray[i].isDataDiagnosticEvent()
                        && logEldEventArray[i].getEventCode() == EmployeeLogEldEventCode.EldDataDiagnosticLogged
                        && DataDiagnosticEnum.MISSING_REQUIRED_DATA_ELEMENTS.toDMOEnum().equals(logEldEventArray[i].getDiagnosticCode())){
                    if (currentEvent.getRelatedKmbPK() == eventTriggered.getPrimaryKey()){
                        CreateDataDiagnosticClearedEvent(employeeLog,
                                TimeKeeper.getInstance().getCurrentDateTime().toDate(),
                                DataDiagnosticEnum.MISSING_REQUIRED_DATA_ELEMENTS, eventTriggered.getPrimaryKey());
                    }
                    return;
                }
            }
        }
    }

    /**
     * Per Mandate, if the current day and/or any of the previous 7 days logs(8 days total) contain more than 30 minutes(total time per day) of unassigned driving time,
     * we need to generate a data diagnostic event.
     */
    public void TrySetUnidentifiedDrivingTimeDataDiagnosticEvent(Date firstUnidentifiedEventDate)
    {
        int previousLogDaysCount = 7;

        IAPIController controllerEvt = MandateObjectFactory.getInstance(this.getContext(), GlobalState.getInstance().getFeatureService()).getCurrentEventController();
        EobrConfigController eobrController = new EobrConfigController(this.getContext(), eobrReader);
        String serialNumber = eobrController.getSerialNumber();
        EmployeeLog currentLog = GlobalState.getInstance().getCurrentDriversLog();
        User currentUser = GlobalState.getInstance().getCurrentUser();
        String dailyLogStartTime = GlobalState.getInstance().getCompanyConfigSettings(this.getContext()).getDailyLogStartTime();
        Date currentLogStartTime = EmployeeLogUtilities.CalculateLogStartTime(dailyLogStartTime, currentLog.getLogDate(), currentUser.getHomeTerminalTimeZone());

        if(serialNumber != null && !serialNumber.isEmpty()) {
            List<UnclaimedEventDTO> unidentifiedEvents = controllerEvt.fetchUnidentifiedDrivingEventsByELD(serialNumber, DateUtility.AddDays(currentLogStartTime, -previousLogDaysCount));
            Boolean activeUnidentifiedDrivingDiagnosticEvents = controllerEvt.isUnidentifiedDrivingDiagnosticEventByELDActive(serialNumber, DateUtility.AddDays(currentLogStartTime, -previousLogDaysCount));

            if (!activeUnidentifiedDrivingDiagnosticEvents && IsAddUnclaimedDrivingDurationDataDiagnostic(currentLogStartTime, firstUnidentifiedEventDate, previousLogDaysCount, unidentifiedEvents.toArray(new UnclaimedEventDTO[unidentifiedEvents.size()]))) {
                try {
                    CreateDataDiagnosticEvent(GlobalState.getInstance().getCurrentEmployeeLog(), DateUtility.getCurrentDateTimeWithSecondsUTC(), DataDiagnosticEnum.UNIDENTIFIED_DRIVING_RECORDS, 0);
                } catch (Throwable throwable) {
                    Log.e(LOG_TAG, "Error while trying to set unidentified driving data diagnostic event", throwable);
                }
            }
        }
    }

    /**
     * Per Mandate, if the current day + previous 7 days logs(8 days total) contain less than 15 minutes(combined time over 8 log days) of unassigned driving time,
     * we need to clear any active unassigned driving data diagnostic event.
     */
    public void TryClearUnidentifiedDrivingTimeDataDiagnosticEvent()
    {
        int previousLogDaysCount = 7;

        IAPIController controllerEvt = MandateObjectFactory.getInstance(this.getContext(), GlobalState.getInstance().getFeatureService()).getCurrentEventController();
        EobrConfigController eobrController = new EobrConfigController(this.getContext(), eobrReader);
        String serialNumber = eobrController.getSerialNumber();
        EmployeeLog currentLog = GlobalState.getInstance().getCurrentDriversLog();
        User currentUser = GlobalState.getInstance().getCurrentUser();
        String dailyLogStartTime = GlobalState.getInstance().getCompanyConfigSettings(this.getContext()).getDailyLogStartTime();
        Date currentLogStartTime = EmployeeLogUtilities.CalculateLogStartTime(dailyLogStartTime, currentLog.getLogDate(), currentUser.getHomeTerminalTimeZone());

        if(serialNumber != null && !serialNumber.isEmpty()) {
            List<UnclaimedEventDTO> unidentifiedEvents = controllerEvt.fetchUnidentifiedDrivingEventsByELD(serialNumber, DateUtility.AddDays(currentLogStartTime, -previousLogDaysCount));
            Boolean activeUnidentifiedDrivingDiagnosticEvents = controllerEvt.isUnidentifiedDrivingDiagnosticEventByELDActive(serialNumber, DateUtility.AddDays(currentLogStartTime, -previousLogDaysCount));

            if(activeUnidentifiedDrivingDiagnosticEvents && IsClearUnclaimedDrivingDurationDataDiagnostic(currentLogStartTime, previousLogDaysCount, unidentifiedEvents.toArray(new UnclaimedEventDTO[unidentifiedEvents.size()]))){
                try {
                    CreateDataDiagnosticClearedEvent(GlobalState.getInstance().getCurrentEmployeeLog(),DateUtility.getCurrentDateTimeWithSecondsUTC(),DataDiagnosticEnum.UNIDENTIFIED_DRIVING_RECORDS, 0L);
                } catch (Throwable throwable) {
                    Log.e(LOG_TAG, "Error while trying to clear unidentified driving data diagnostic event", throwable);
                }
            }
        }
    }

    public Boolean IsAddUnclaimedDrivingDurationDataDiagnostic(Date currentLogStartTime, Date firstUnidentifiedEventDate, int previousLogDaysCount, UnclaimedEventDTO[] events) {

        Long accumulatedSeconds = 0L;
        Boolean isFirstEvent = true;
        Date driveStart = null;
        Date logDayStart = currentLogStartTime;

        //Determine log date containing the first unidentified event read from ELD. Don't need to process any log days before that.
        for(int i = 1; i <= previousLogDaysCount; i++){
            if(logDayStart.getTime() > firstUnidentifiedEventDate.getTime()){
                logDayStart = DateUtility.AddDays(currentLogStartTime, -i);
            }
        }

        Date logDayEnd = DateUtility.AddDays(logDayStart, 1);
        logDayEnd = DateUtility.AddSeconds(logDayEnd, -1);

        for (UnclaimedEventDTO unclaimedEvent : events) {

            if (unclaimedEvent.getEventDate().before(logDayStart)) continue; // skip everything before our first unreviewed items

            //If gone past end of log day, finish log day and advance to new log day.
            if(unclaimedEvent.getEventDate().after(logDayEnd)){

                //Previous log day ended with a driving event, finish acumulated time check.
                if(driveStart != null)
                {
                    accumulatedSeconds += (logDayEnd.getTime() - driveStart.getTime()) / 1000;
                    driveStart = null;

                    if (accumulatedSeconds > 1800)
                        return true;
                }

                logDayStart = DateUtility.AddDays(logDayStart, 1);
                logDayEnd =  DateUtility.AddDays(logDayEnd, 1);
                logDayEnd = DateUtility.AddSeconds(logDayEnd, -1);
                isFirstEvent = true;
                accumulatedSeconds = 0L;
            }

            //Always check first event of log day to account for driving across log days.
            //This will be an On-Duty event as first event of the day.
            if (isFirstEvent && unclaimedEvent.getEventCode() == 4) {
                accumulatedSeconds += (unclaimedEvent.getEventDate().getTime() - logDayStart.getTime())/1000;
                isFirstEvent = false;
            }
            else if (unclaimedEvent.getEventCode() == 3) { //Driving event only.
                driveStart = unclaimedEvent.getEventDate();
            }
            else { // Driving and on-duty event combo. Calculate duration.
                accumulatedSeconds += (unclaimedEvent.getEventDate().getTime() - driveStart.getTime()) / 1000;
                driveStart = null;
            }

            isFirstEvent = false;

            if (accumulatedSeconds > 1800)
                return true;
        }

        return false;
    }

    public Boolean IsClearUnclaimedDrivingDurationDataDiagnostic(Date currentLogStartTime, int previousLogDayCount, UnclaimedEventDTO[] events) {

        Long accumulatedSeconds = 0L;
        Boolean isFirstEvent = true;
        Date driveStart = null;
        Date logDayStart = currentLogStartTime;

        // If no events exist, they must have all been claimed to we can clear the Data Diagnostic event.
        if (events.length == 0) {
            return true;
        }

        //Determine log date containing the oldest unidentified event. Don't need to process any log days before that.
        for (int i = 1; i <= previousLogDayCount; i++) {
            if (logDayStart.getTime() > events[0].getEventDate().getTime()) {
                logDayStart = DateUtility.AddDays(currentLogStartTime, -i);
            }
        }

        for (UnclaimedEventDTO unclaimedEvent : events) {

            if (isFirstEvent && unclaimedEvent.getEventCode() == 4) {
                accumulatedSeconds += (unclaimedEvent.getEventDate().getTime() - logDayStart.getTime()) / 1000;
            } else if (unclaimedEvent.getEventCode() == 3) { //Driving event only.
                driveStart = unclaimedEvent.getEventDate();
            } else { // Driving and on-duty event combo. Calculate duration.
                accumulatedSeconds += (unclaimedEvent.getEventDate().getTime() - driveStart.getTime()) / 1000;
                driveStart = null;
            }

            isFirstEvent = false;

            if (accumulatedSeconds > 900)
                return false;
        }

        return true;
    }

    private float getLatestOdometerReading() {
        StatusRecord sr = getLatestStatusRecord();
        if(sr != null) {
            return sr.getOdometerReading();
        }

        return GlobalState.getInstance().getLastValidOdometerReading();
    }

}

