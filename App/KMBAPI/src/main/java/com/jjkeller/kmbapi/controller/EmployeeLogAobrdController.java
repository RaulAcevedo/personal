package com.jjkeller.kmbapi.controller;

import android.content.Context;

import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.abstracts.AOBRDControllerBase;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.controller.utility.EmployeeLogUtilities;
import com.jjkeller.kmbapi.employeelogeldevents.Enums;
import com.jjkeller.kmbapi.enums.DutyStatusEnum;
import com.jjkeller.kmbapi.enums.RuleSetTypeEnum;
import com.jjkeller.kmbapi.kmbeobr.EventRecord;
import com.jjkeller.kmbapi.kmbeobr.TripReport;
import com.jjkeller.kmbapi.kmbeobr.UnidentifiedPairedEvents;
import com.jjkeller.kmbapi.kmbeobr.VehicleLocation;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;
import com.jjkeller.kmbapi.proxydata.Location;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * APIEventControllerBase implementation for existing AOBRD LogEvents
 */
public class EmployeeLogAobrdController extends AOBRDControllerBase {

    public EmployeeLogAobrdController(Context ctx) {
        super(ctx);
    }

    @Override
    public boolean HasEldMandateDrivingExceptionEnabled(DutyStatusEnum dutyStatus) {
        return false;
    }

    @Override
    public void CertifyEmployeeLog(EmployeeLog logToCertify) throws Throwable {
        // Aobrd does not require an log event to be created, or marked as certified
    }

    @Override
    public EmployeeLogEldEvent GetMostRecentLoginEvent(EmployeeLog empLog) throws Exception {
        return super.GetMostRecentLoginEvent(empLog);
    }

    @Override
    public void CreateLoginLogoutEvent(Enums.CompositeEmployeeLogEldEventTypeEventCodeEnum eventType) throws Throwable {
    }

    @Override
    public List<UnidentifiedPairedEvents> LoadUnidentifiedEldEventPairs(boolean _showAllUnsubmitted) {
        return null; //this does not apply to AOBRD so return nothing
    }

    @Override
    public List<UnidentifiedPairedEvents> LoadUnreviewedWithoutConfidenceLevelEvents(){
        return null;
    }

    @Override
    public int getEngineSecondsOffset() {
        // The concept of a engine seconds offset does not exist when using AOBRD
        return 0;
    }

    @Override
    public boolean IsDuplicateEnginePowerUpOrShutDownUnassignedEvent(EventRecord eventRecord, Enums.CompositeEmployeeLogEldEventTypeEventCodeEnum eventTypeAndCode) {
        return false;
    }

    @Override
    public void CreateEnginePowerUpOrShutDownUnassignedEvent(EventRecord eventRecord, Enums.CompositeEmployeeLogEldEventTypeEventCodeEnum eventTypeAndCode, int engineSecondsOffset) throws Throwable {
    }

    @Override
    public void CreateEnginePowerUpOrShutDownEvent(EmployeeLog employeeLog, EventRecord eventRecord, Enums.CompositeEmployeeLogEldEventTypeEventCodeEnum eventTypeAndCode) throws Throwable {
    }

    @Override
    public EmployeeLogEldEvent CreateDriveOnOrOffUnassignedEvent(VehicleLocation eventRecord, Enums.CompositeEmployeeLogEldEventTypeEventCodeEnum eventCode) {
        return null;
    }

    @Override
    public void SaveDriveOnOrOffUnassignedEvent(EmployeeLogEldEvent event) throws Throwable {
    }

    @Override
    public void CreateDutyStatusChangedEventForLogin(EmployeeLog empLog, Date timestamp, DutyStatusEnum dutyStatus, Location location, boolean isStartTimeValidated, RuleSetTypeEnum ruleset, String logRemark, Date logRemarkDate, int recordOrigin, String employeeId) throws Throwable {
        EmployeeLogUtilities.AddEventToEndOfLog(empLog, timestamp, dutyStatus, location, isStartTimeValidated, ruleset, logRemark, logRemarkDate, null, null);
    }

    @Override
    public boolean ManualDutyStatusChangeShouldEndDriving(DutyStatusEnum dutyStatus, Date endOfDrivingTimestamp) {
        // a driving period is being ended and we need to add an On-Duty event ot the end of the DRV at the correct time
        return endOfDrivingTimestamp != null && (dutyStatus.getValue() != DutyStatusEnum.ONDUTY && dutyStatus.getValue() != DutyStatusEnum.DRIVING);
    }

    @Override
    public Date ManualDutyStatusChangeGetTimeOfNewEvent(DutyStatusEnum dutyStatus, Date timeOfNewEvent, Date endOfDrivingTimestamp, EmployeeLogEldEvent lastEvent) {
        Date result = timeOfNewEvent;

        if (endOfDrivingTimestamp != null) {
            // a driving period is being ended
            if (dutyStatus.getValue() == DutyStatusEnum.ONDUTY || dutyStatus.getValue() == DutyStatusEnum.DRIVING) {
                // when an On-Duty, or Driving, is being added, set the timestamp to end the driving period at the correct time
                result = endOfDrivingTimestamp;
            }
        }
        return result;
    }

    @Override
    public void CreateDutyStatusChangedEvent(EmployeeLog empLog, Date timestamp, DutyStatusEnum dutyStatus, Location location, boolean isStartTimeValidated, RuleSetTypeEnum ruleset, String logRemark, Date logRemarkDate, boolean isManualDutyStatusChange, String motionPictureProductionId, String motionPictureAuthorityId) throws Throwable {
        EmployeeLogUtilities.AddEventToEndOfLog(empLog, timestamp, dutyStatus, location, new FailureController(this.getContext()).getIsMobileClockSynchronized(), GlobalState.getInstance().getCurrentUser().getRulesetTypeEnum(), null, null, motionPictureProductionId, motionPictureAuthorityId);
    }

    @Override
    public void CheckForAndCreatePersonalConveyanceChangedEvent(EmployeeLog empLog, Date timestamp, Location location, String annotation) throws Throwable {
        // Personal Conveyance Records are created when the user starts driving when using AOBRD, so there is nothing to do when the user changes duty status
    }

    @Override
    public void CheckForAndCreateYardMoveEvent(EmployeeLog employeeLog, Date timeStampOfEvent, Location location, String annotation) throws Throwable {
        // The concept of a yard move does not exist when using AOBRD
    }

    @Override
    public void CreateIntermediateEvent(Enums.CompositeEmployeeLogEldEventTypeEventCodeEnum eventType, TripReport tripData) throws Throwable {
        // The concept of an Intermediate Event does not exist when using AOBRD
    }

    @Override
    public EmployeeLogEldEvent saveEldEvent(EmployeeLogEldEvent editedEvent, Date editedEventEndTime) throws Throwable {
        return null;
    }

    /**
     * Returns a List<EmployeeLogEldEvent> reconciled based on requested Enum
     * Enum.Original         - returns all Active Eld Events
     * Enum.Accept_Preview   - reconciles date overlaps to return a preview of what the Eld Events would look like if you Accepted all Change Requests
     * Enum.Accept_Database  - Accepts all Change Requests and returns a database ready list of ONLY those Eld Events that need to be updated or added to the local database
     * Enum.Reject_Database  - Rejects all Change Requests and returns a database ready list of ONLY those Eld Events that need to be updated or added to the local database
     */
    @Override
    public List<EmployeeLogEldEvent> getReconcileChangeRequestedEldEvents(int employeeLogKey, com.jjkeller.kmbapi.calcengine.Enums.ReconcileChangeRequestedEldEventsEnum reconcileEnum) {
        return null;
    }

    /**
     * Save multiple records at once so they are wrapped in the same transaction. All will succeed or fail together.
     */
    public void SaveListInSingleTransaction(EmployeeLogEldEvent[] eldEventList) {
        eventFacade.SaveListInSingleTransaction(eldEventList);
    }

    public void CheckForAndCreateEndOfPCYMWT_Event(EmployeeLog employeeLog, Date timestamp, Location location) throws Throwable {
        // The concept of a PC, YM, or WT Clear Event does not exist when using AOBRD
    }

    public void CheckForAndCreateEndOfPCYMWT_Event(EmployeeLog employeeLog, Date timestamp, Location location, boolean forMidnightTransition) throws Throwable {
        // The concept of a PC, YM, or WT Clear Event does not exist when using AOBRD
    }

    @Override
    public boolean reassignEldEventTeamDriving(int reassignEldEventPrimaryKey, int eventCodeInsteadOfDriving, GlobalState.TeamDriverModeEnum teamDriverMode, String kmbUserName, String annotation) throws Throwable {
        // do nothing
        return true;
    }

    @Override
    // always returns true
    public boolean CheckAndHandleDriveOffWhenThereIsANewDutyStatus(DutyStatusEnum currentDutyStatus) {
        // when the eld mandate feature toggle is off, we will always want to create the on duty status right away. As as opposed to prompting the user and
        // asking them if they would like to continue driving
        return true;
    }

    public void CreateChangeInDriversIndicationEvent(EmployeeLog employeeLog, Date timeStamp, Location location, String annotation, int eventCode, String logRemark, Integer distance) throws Throwable {
    }

    @Override
    public void UpdateLoginEvent() throws Throwable {
        // Aobrd does not have login events to modify
    }

    /**
     * AOBRD Implementation of method to return trip info for a given log
     *
     * @param employeeLog Employee Log object to pull trip info from
     * @return Map of PropertyName/Strings representing trip info
     */
    @Override
    public HashMap<EmployeeLog.TripInfoPropertyKey, HashSet<String>> GetTripInfoForLog(EmployeeLog employeeLog) {
        HashMap<EmployeeLog.TripInfoPropertyKey, HashSet<String>> tripInfoHashMap = super.GetTripInfoForLog(employeeLog);
        /*
		Get array of current eld events on log. If the EldEventList object isn't null, pull the
		events into the array. Otherwise, just provide null here and fall into default behavior
		 */
        if (employeeLog.getShipmentInformation() != null)
            tripInfoHashMap.get(EmployeeLog.TripInfoPropertyKey.ShipmentInfo).add(employeeLog.getShipmentInformation());
        if (employeeLog.getTractorNumbers() != null)
            tripInfoHashMap.get(EmployeeLog.TripInfoPropertyKey.TractorNumber).add(employeeLog.getTractorNumbers());
        if (employeeLog.getTrailerNumbers() != null)
            tripInfoHashMap.get(EmployeeLog.TripInfoPropertyKey.TrailerNumber).add(employeeLog.getTrailerNumbers());
        if (employeeLog.getTrailerPlate() != null)
            tripInfoHashMap.get(EmployeeLog.TripInfoPropertyKey.TrailerPlate).add(employeeLog.getTrailerPlate());
        if (employeeLog.getVehiclePlate() != null)
            tripInfoHashMap.get(EmployeeLog.TripInfoPropertyKey.VehiclePlate).add(employeeLog.getVehiclePlate());

        return tripInfoHashMap;
    }

    @Override
    public boolean SubmitUnidentifiedEldEvents() {
        return true;
    }

    @Override
    public void SynchronizeUnclaimedEvents(User user) {
        // do nothing: this is currently for mandate only
    }

    public void driverElectedToContinueDriving() {
        //do nothing.. mandate only.
    }
}
