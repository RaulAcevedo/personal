package com.jjkeller.kmbapi.controller.abstracts;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.ControllerFactory;
import com.jjkeller.kmbapi.controller.EOBR.EobrReader;
import com.jjkeller.kmbapi.controller.EmployeeLogEldMandateController;
import com.jjkeller.kmbapi.controller.dataaccess.EmployeeLogEldEventFacade;
import com.jjkeller.kmbapi.controller.interfaces.IAPIController;
import com.jjkeller.kmbapi.controller.share.ControllerBase;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.controller.utility.EmployeeLogUtilities;
import com.jjkeller.kmbapi.controller.utility.ErrorLogHelper;
import com.jjkeller.kmbapi.controller.utility.GenericEventComparer;
import com.jjkeller.kmbapi.employeelogeldevents.EmployeeLogEldEventCode;
import com.jjkeller.kmbapi.employeelogeldevents.Enums;
import com.jjkeller.kmbapi.employeelogeldevents.Enums.EmployeeLogEldEventType;
import com.jjkeller.kmbapi.employeelogeldevents.UnclaimedEventDTO;
import com.jjkeller.kmbapi.enums.DataDiagnosticEnum;
import com.jjkeller.kmbapi.enums.DutyStatusEnum;
import com.jjkeller.kmbapi.enums.Malfunction;
import com.jjkeller.kmbapi.enums.RuleSetTypeEnum;
import com.jjkeller.kmbapi.kmbeobr.GpsFix;
import com.jjkeller.kmbapi.kmbeobr.StatusRecord;
import com.jjkeller.kmbapi.kmbeobr.EventRecord;
import com.jjkeller.kmbapi.kmbeobr.TripReport;
import com.jjkeller.kmbapi.kmbeobr.VehicleLocation;
import com.jjkeller.kmbapi.proxydata.EldEventAdapterList;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;
import com.jjkeller.kmbapi.proxydata.EventTranslationBase;
import com.jjkeller.kmbapi.proxydata.GpsLocation;
import com.jjkeller.kmbapi.proxydata.Location;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;


/**
 * Abstract Base Class for an APIController, accepting a type which extends the ProxyBase class
 * This base class exists to provide an area to refactor common controller methods as they are identified
 * This base class is extended by an AOBR-specific base class as a bridge.
 */
public abstract class APIControllerBase extends ControllerBase implements IAPIController {

    //Protected Fields
    protected GlobalState globalState = null;
    protected EmployeeLogEldEventFacade eventFacade = null;
    protected EobrReader eobrReader;
    //Protected Fields

    public APIControllerBase(Context ctx) {
        super(ctx);
        globalState = GlobalState.getInstance();
        eventFacade = new EmployeeLogEldEventFacade(ctx, globalState.getCurrentUser());
        eobrReader = EobrReader.getInstance();
    }

    public APIControllerBase(Context ctx, EobrReader eobrReader) {
        this(ctx);

        this.eobrReader = eobrReader;
    }

    //ELD MANDATE CONTROLLER METHODS
    @Override
    public DutyStatusEnum GetLastEventDutyStatus(EmployeeLog empLog) {
        EmployeeLogEldEvent lastEvt =  EmployeeLogUtilities.GetLastEventInLog(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus,empLog);
        DutyStatusEnum returnEnum = new DutyStatusEnum(DutyStatusEnum.NULL);
        if (lastEvt != null)
            returnEnum = lastEvt.getDutyStatusEnum();
        return returnEnum;
    }



    @Override
    public EmployeeLogEldEvent[] fetchEldEventsByEventTypes(int employeeLogKey, List<Integer> eventTypes, List<Integer> eventRecordStatuses) {
        return eventFacade.GetByEventTypes(employeeLogKey, eventTypes, eventRecordStatuses);
    }

    @Override
    public EmployeeLogEldEvent [] fetchEldPCRecords(Integer uniqueKey){
        return eventFacade.FetchEldPCRecords(uniqueKey);
    }

    @Override
    public EmployeeLogEldEvent[] fetchPreviousWeekUnidentifiedDriverEvents(String serialNumber){
        return eventFacade.getPreviousWeekUnidentifiedEvents(serialNumber);
    }

    @Override
    public List<UnclaimedEventDTO> fetchUnidentifiedDrivingEventsByELD(String serialNumber, Date LogRangeStartTime){
        return eventFacade.getUnidentifiedDrivingEventsByELD(serialNumber, LogRangeStartTime);
    }

    @Override
    public Boolean isUnidentifiedDrivingDiagnosticEventByELDActive(String serialNumber, Date LogRangeStartTime){
        return eventFacade.isUnidentifiedDrivingDiagnosticEventByELDActive(serialNumber, LogRangeStartTime);
    }

    @Override
    public List<EmployeeLogEldEvent> fetchUnreviewedUnidentifiedEvents(){
        return eventFacade.GetUnreviewedUnidentifiedEvents();
    }

    @Override
    public List<EmployeeLogEldEvent> fetchUnreviewedWithoutConfidenceLevelEvents(){
        return eventFacade.GetUnreviewedWithoutConfidenceLevelEvents();
    }

    @Override
    public List<EmployeeLogEldEvent> fetchUnsubmittedUnidentifiedEvents(){
        return eventFacade.GetUnsubmittedUnidentifiedEvents();
    }

    @Override
    public EmployeeLogEldEvent fetchEldEventByKey(Integer uniqueKey) {
        return eventFacade.FetchByKey(uniqueKey);
    }

    @Override
    public void CreateInitialEventForLog(EmployeeLog empLog,DutyStatusEnum dutyStatusEnum, Date timestamp, User currentUser,Location location,RuleSetTypeEnum ruleSetTypeEnum, String motionPictureProductionId, String motionPictureAuthorityId) {
        // build a 24 hour event, using the status passed in
        Date logStartTime = EmployeeLogUtilities.CalculateLogStartTime(getContext(), timestamp, getCurrentUser().getHomeTerminalTimeZone());
        EmployeeLogUtilities.AddEventToLog(empLog, logStartTime, dutyStatusEnum, location, true, ruleSetTypeEnum, null, null, motionPictureProductionId, motionPictureAuthorityId);
    }

    @Override
    public EmployeeLogEldEvent saveEldEvent(EmployeeLogEldEvent editedEvent, Date editedEventEndTime) throws Throwable {
        return saveEldEvent(editedEvent, Enums.SpecialDrivingCategory.None, editedEventEndTime, Enums.ActionInitiatingSaveEnum.EditLog);
    }

    @Override
    public EmployeeLogEldEvent saveEldEvent(EmployeeLogEldEvent editedEvent, Enums.SpecialDrivingCategory subCategory, Date editedEventEndTime, Enums.ActionInitiatingSaveEnum actionInitiatingSave) throws Throwable {
        return null;
    }

    public float GetGpsUncertaintyMiles()
    {
        StatusRecord statusRecord = new StatusRecord();
        if (eobrReader.getIsEobrDevicePhysicallyConnected()) {
            eobrReader.Technician_GetCurrentData(statusRecord, false);
            return EventTranslationBase.calculateDistanceSinceLastValidCoordinates(statusRecord.getGpsUncertDistance());
        } else {
//            Log.e(ELD_MANDATE_DUTY_ERROR_TAG, "The Eobr Device is not connected");
            return -1;
        }
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++Protected Methods++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    protected HashMap<String, Object> constructMandateValuePayload(int eventType, int eventCode, Location loc, StatusRecord record, EmployeeLog log, Integer logKey, Date eventDateTime) {
        boolean malfunctionIndicator;
        boolean dataDiagnosticIndicator;

        EmployeeLogEldMandateController mandateController = ControllerFactory.getInstance().getEmployeeLogEldMandateController();

        dataDiagnosticIndicator = !mandateController.getActiveDataDiagnostics(log).isEmpty();
        malfunctionIndicator = !mandateController.getActiveMalfunctions(log).isEmpty();

        HashMap<String, Object> payload = new HashMap<>();
        Bundle primitiveBundle = new Bundle();
        primitiveBundle.putSerializable(EmployeeLogEldEvent.EVENTTYPEKEYNAME, eventType);
        primitiveBundle.putInt(EmployeeLogEldEvent.EVENTCODEKEYNAME, eventCode);
        primitiveBundle.putSerializable(EmployeeLogEldEvent.LOGKEYNAME, logKey);
        primitiveBundle.putSerializable(EmployeeLogEldEvent.EVENTDATETIMENAME, eventDateTime);
        primitiveBundle.putBoolean(EmployeeLogEldEvent.MALFUNCTION_INDICATOR_STATUS_NAME, malfunctionIndicator);
        primitiveBundle.putBoolean(EmployeeLogEldEvent.DATA_DIAGNOSTIC_INDICATOR_STATUS_NAME, dataDiagnosticIndicator);
        payload.put(EmployeeLogEldEvent.PRIMITIVEBUNDLENAME, primitiveBundle);
        payload.put(EmployeeLogEldEvent.LOCATIONOBJECTNAME, loc);
        payload.put(EmployeeLogEldEvent.STATUSRECORDOBJECTNAME, record);
        payload.put(EmployeeLogEldEvent.EMPLOYEELOGOBJECTNAME, log);
        return payload;
    }


    protected void PopulateLoginLogoffEvent(EmployeeLogEldEvent eventToSave, int eventCode) throws Exception {
        PopulateLoginLogoffEvent(eventToSave, eventCode, null);
    }

    protected void PopulateLoginLogoffEvent(EmployeeLogEldEvent eventToSave, int eventCode, EmployeeLog log) throws Exception {
        //Add mandate-specific properties to event
        //Use class level instances of objects
        try {
            Date currentHomeTerminalTime = DateUtility.CurrentHomeTerminalTime(GlobalState.getInstance().getCurrentUser());
            HashMap<String, Object> objectHashMap = constructMandateValuePayload(EmployeeLogEldEventType.LoginLogout.getValue(), eventCode, null, null, log, null, currentHomeTerminalTime);
            eventToSave.setInternalObjectValueMap(objectHashMap);
            eventToSave.hydrate(true);
        } catch (Exception e) {
            String errorLoggingTag = "LoginLogoffEvent";
            String msg = String.format("%s failure to save event: {%s}  EmployeeLogEldEvent: {%s}", errorLoggingTag, e.getMessage(), eventToSave.toLogString());
            Log.d(errorLoggingTag, msg);
            ErrorLogHelper.RecordMessage(this.getContext(), msg);
            throw e;
        }
    }

    protected void PopulateCertificationEvent(EmployeeLogEldEvent eventToSave, EmployeeLog employeeLogToCertify) {
        // set the event code
        int certCount = eventFacade.GetCertificationCountByLogKey((int) employeeLogToCertify.getPrimaryKey());
        if (certCount < com.jjkeller.kmbapi.employeelogeldevents.EmployeeLogEldEventCode.Certification_MaxValue)
            certCount++;
        else
            certCount = com.jjkeller.kmbapi.employeelogeldevents.EmployeeLogEldEventCode.Certification_MaxValue;

        HashMap<String, Object> objectHashMap = constructMandateValuePayload(EmployeeLogEldEventType.Certification.getValue(), certCount, null, null, employeeLogToCertify, (int) employeeLogToCertify.getPrimaryKey(), getCurrentClockHomeTerminalTime());
        eventToSave.setInternalObjectValueMap(objectHashMap);
        eventToSave.hydrate(true);

        // Set the event record origin 2 = Edited or Entered by Driver
        eventToSave.setEventRecordOrigin(2);

        // if the parent EmployeeLog has EobrSerialNumber, the hydrate calls populateMandateProps which will automatically setEobrSerialNumber()
        if (!DateUtility.IsToday(employeeLogToCertify.getLogDate(), GlobalState.getInstance().getCurrentUser()))
            eventToSave.setEobrSerialNumber(null);

        eventToSave.setDriverOriginatorUserId(employeeLogToCertify.getEmployeeId());
    }

    protected void SetConventionalPrecisionGPSInfo(EmployeeLogEldEvent logEldEvent, StatusRecord statusRecord){
        if (statusRecord != null) {
            logEldEvent.setLatitude(EmployeeLogUtilities.GetReducedPrecisionGPSForDouble(new Double(statusRecord.getGpsLatitude()), false));
            logEldEvent.setLongitude(EmployeeLogUtilities.GetReducedPrecisionGPSForDouble(new Double(statusRecord.getGpsLongitude()), false));
            logEldEvent.setGpsTimestamp(statusRecord.getGpsTimestampUtc());
            logEldEvent.setIsGPSAtReducedPrecision(false);
            logEldEvent.setDistanceSinceLastCoordinates(EventTranslationBase.calculateDistanceSinceLastValidCoordinates(statusRecord.getGpsUncertDistance()));
        }
    }

    protected void SetConventionalPrecisionGPSInfo(EmployeeLogEldEvent logEldEvent, GpsFix gps) {
        if (gps != null) {
            logEldEvent.setLatitude(EmployeeLogUtilities.GetReducedPrecisionGPSForDouble(new Double(gps.getLatitude()), false));
            logEldEvent.setLongitude(EmployeeLogUtilities.GetReducedPrecisionGPSForDouble(new Double(gps.getLongitude()), false));
            logEldEvent.setGpsTimestamp(gps.getTimecodeAsDate());
            logEldEvent.setIsGPSAtReducedPrecision(false);
            logEldEvent.setDistanceSinceLastCoordinates(EventTranslationBase.calculateDistanceSinceLastValidCoordinates(gps.getUncert()));
        }
    }

    protected void SetReducedPrecisionGPSInfo(EmployeeLogEldEvent logEldEvent, StatusRecord statusRecord, boolean isInPersonalConveyance){
        Double latitude = null;
        Double longitude = null;
        Date gpsTimestampUtc = null;

        if(logEldEvent.getLatitude() == null || logEldEvent.getLongitude() == null) {
            GpsLocation lastGPSLocation = GlobalState.getInstance().getLastGPSLocation();
            if(statusRecord != null) {
                if(statusRecord.getGpsLatitude() != 0.0) {
                    latitude = new Double(statusRecord.getGpsLatitude());
                    longitude = new Double(statusRecord.getGpsLongitude());
                    gpsTimestampUtc = statusRecord.getGpsTimestampUtc();
                }
            }
            else if(lastGPSLocation != null) {
                latitude = new Double(String.valueOf(lastGPSLocation.getLatitudeDegrees()));
                longitude = new Double(String.valueOf(lastGPSLocation.getLongitudeDegrees()));
                gpsTimestampUtc = lastGPSLocation.getTimestampUtc();
            }
        }
        else {
            latitude = logEldEvent.getLatitude();
            longitude = logEldEvent.getLongitude();
            gpsTimestampUtc = logEldEvent.getGpsTimestamp();
        }

        if (latitude != null) {
            logEldEvent.setLatitude(EmployeeLogUtilities.GetReducedPrecisionGPSForDouble(latitude, isInPersonalConveyance));
            logEldEvent.setLongitude(EmployeeLogUtilities.GetReducedPrecisionGPSForDouble(longitude, isInPersonalConveyance));
            logEldEvent.setGpsTimestamp(gpsTimestampUtc);
            if(isInPersonalConveyance){
                logEldEvent.setIsGPSAtReducedPrecision(true);
            }

        }

        if (statusRecord != null) {
            logEldEvent.setDistanceSinceLastCoordinates(EventTranslationBase.calculateDistanceSinceLastValidCoordinates(statusRecord.getGpsUncertDistance()));
        }
    }

    protected void PopulateEngineOnOrOffEvent(EmployeeLog employeeLog, EmployeeLogEldEvent eventToSave, int eventCode) {
        Integer potentialLogKey = employeeLog != null && employeeLog.isPrimaryKeySet() ?(int)employeeLog.getPrimaryKey() : -1;
        HashMap<String, Object> objectHashMap = constructMandateValuePayload(EmployeeLogEldEventType.EnginePowerUpPowerDown.getValue(), eventCode, null, null, employeeLog, potentialLogKey, DateUtility.CurrentHomeTerminalTime(GlobalState.getInstance().getCurrentUser()));
        eventToSave.setInternalObjectValueMap(objectHashMap);
        eventToSave.hydrate(true);
    }

    protected void PopulateEngineOnOrOffEvent(EmployeeLog employeeLog, EmployeeLogEldEvent eventToSave, int eventCode, EventRecord eventRecord, StatusRecord statusRecord) {
        Integer potentialLogKey = employeeLog != null && employeeLog.isPrimaryKeySet() ? (int)employeeLog.getPrimaryKey() : -1;
        eventToSave.setInternalObjectValueMap(constructMandateValuePayload(EmployeeLogEldEventType.EnginePowerUpPowerDown.getValue(), eventCode, null, null, employeeLog, potentialLogKey,eventRecord.getTimecodeAsDate()));
        eventToSave.hydrate(true);
        SetConventionalPrecisionGPSInfo(eventToSave, statusRecord);
    }

    protected void PopulateDriveOnOrOffEvent(EmployeeLog employeeLog, EmployeeLogEldEvent eventToSave, int eventCode, VehicleLocation vehicleLocation) {
        Integer potentialLogKey = employeeLog != null && employeeLog.isPrimaryKeySet() ?(int)employeeLog.getPrimaryKey() : -1;
        HashMap<String, Object> objectHashMap = constructMandateValuePayload(EmployeeLogEldEventType.DutyStatusChange.getValue(), eventCode, null, null, employeeLog, potentialLogKey, vehicleLocation.getGpsFix().getTimecodeAsDate());
        eventToSave.setInternalObjectValueMap(objectHashMap);
        eventToSave.hydrate(true);
        SetConventionalPrecisionGPSInfo(eventToSave, vehicleLocation.getGpsFix());
    }

    // 4.5.1.1 Event: Change in Driver's Duty Status
    protected void PopulateIntermediateEvent(EmployeeLog employeeLog,EmployeeLogEldEvent eventToSave,int eventCode, TripReport tripData){
        int logKey = -1;
        if (employeeLog.isPrimaryKeySet())
            logKey = (int)employeeLog.getPrimaryKey();
        eventToSave.setInternalObjectValueMap(constructMandateValuePayload(EmployeeLogEldEventType.IntermediateLog.getValue(), eventCode, null, null, employeeLog, logKey, tripData.getDataTimecodeAsDate()));
        eventToSave.hydrate(true);
        eventToSave.setLatitude((double) tripData.getLatitude());
        eventToSave.setLongitude( (double)tripData.getLongitude());
        eventToSave.setDistanceSinceLastCoordinates(EventTranslationBase.calculateDistanceSinceLastValidCoordinates(tripData.getFixUncert()));
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++Protected Methods++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
}
