package com.jjkeller.kmbapi.proxydata;

import android.os.Bundle;

import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.utility.ErrorLogHelper;
import com.jjkeller.kmbapi.employeelogeldevents.EmployeeLogEldEventCode;
import com.jjkeller.kmbapi.employeelogeldevents.Enums;
import com.jjkeller.kmbapi.enums.DutyStatusEnum;
import com.jjkeller.kmbapi.enums.EmployeeLogEldEnum;
import com.jjkeller.kmbapi.enums.RuleSetTypeEnum;
import com.jjkeller.kmbapi.kmbeobr.StatusRecord;

import java.util.Date;
import java.util.HashMap;

/**
 * Abstract class representing the Translation of an EmployeeLogEldEvent to a LogEvent using the
 * EventTranslationBase base class
 */
public abstract class EldEventAdapter extends EventTranslationBase<EmployeeLogEldEvent> {

    /**
     * Strings representing Keys for the hydrationMap. Allows for easier setting/retrieval of
     * objects from the valueMap
     */
    public static final String PRIMITIVEBUNDLENAME = "EmployeeLogEldEvent.Primitives";
    public static final String EVENTTYPEKEYNAME = "EmployeeLogEldEvent.EventType";
    public static final String EVENTCODEKEYNAME = "EmployeeLogEldEvent.EventCode";
    public static final String LOGKEYNAME = "EmployeeLogEldEvent.LogKey";
    public static final String EVENTDATETIMENAME = "EmployeeLogEldEvent.EventDateTime";
    public static final String MALFUNCTION_INDICATOR_STATUS_NAME = "EmployeeLogEldEvent.MalfunctionIndicatorStatus";
    public static final String DATA_DIAGNOSTIC_INDICATOR_STATUS_NAME = "EmployeeLogEldEvent.DataDiagnosticIndicatorStatus";
    public static final String LOCATIONOBJECTNAME = Location.class.getName();
    public static final String STATUSRECORDOBJECTNAME = StatusRecord.class.getName();
    public static final String EMPLOYEELOGOBJECTNAME = EmployeeLog.class.getName();

    /**
     * HashMap containing Key/Value pairs of data to be used to hydrate properties on this object
     * in situations where we may not have access to needed getters/setters
     * This field is accessed ad hoc using the hydrate() method, but will also be referenced at
     * initialization if present to add value to an object from current state objects
     */
    private transient HashMap<String, Object> internalObjectValueMap;


    /**
     * Base constructor that calls EventTranslationBase constructor
     */
    protected EldEventAdapter() {
        super();
        this._internalLocation = new LocationAdapter(this._event);
    }

    /**
     * Constructor that calls EventTranslationBase constructor with EventDateTime parameter
     *
     * @param startDateTime EventDateTime of event
     */
    protected EldEventAdapter(Date startDateTime) {
        super(startDateTime);
        this._internalLocation = new LocationAdapter(this._event);
    }

    /**
     * Public method that translates a DutyStatusEnum into it's corresponding EmployeeLogEldEvent
     * EventCode
     *
     * @param status DutyStatusEnum to be translated
     * @return ELD-Mandate specific Mandate Code analogue of status
     */
    public static int translateDutyStatusEnumToMandateStatus(DutyStatusEnum status) {
        switch (status.getValue()) {
            case DutyStatusEnum.OFFDUTY:
                return EmployeeLogEldEventCode.DutyStatus_OffDuty;
            case DutyStatusEnum.OFFDUTYWELLSITE:
                return EmployeeLogEldEventCode.DutyStatus_OffDutyWellSite;
            case DutyStatusEnum.SLEEPER:
                return EmployeeLogEldEventCode.DutyStatus_Sleeper;
            case DutyStatusEnum.DRIVING:
                return EmployeeLogEldEventCode.DutyStatus_Driving;
            case DutyStatusEnum.ONDUTY:
                return EmployeeLogEldEventCode.DutyStatus_OnDuty;
            default:
                return EmployeeLogEldEventCode.DutyStatus_NULL;
        }
    }

    /**
     * Public method that translates an EventCode into it's corresponding AOBRD Specific
     * DutyStatusEnum representation
     *
     * @param mandateStatus EventCode to be translated
     * @return AOBRD specific DutyStatusEnum analogue of mandateStatus
     */
    public static DutyStatusEnum translateMandateStatusToDutyStatus(Enums.EmployeeLogEldEventType code, int mandateStatus) {
        if (code != Enums.EmployeeLogEldEventType.DutyStatusChange)
            return new DutyStatusEnum(DutyStatusEnum.NULL);
        switch (mandateStatus) {
            case EmployeeLogEldEventCode.DutyStatus_OffDuty:
                return new DutyStatusEnum(DutyStatusEnum.OFFDUTY);
            case EmployeeLogEldEventCode.DutyStatus_OffDutyWellSite:
                return new DutyStatusEnum(DutyStatusEnum.OFFDUTYWELLSITE);
            case EmployeeLogEldEventCode.DutyStatus_Sleeper:
                return new DutyStatusEnum(DutyStatusEnum.SLEEPER);
            case EmployeeLogEldEventCode.DutyStatus_Driving:
                return new DutyStatusEnum(DutyStatusEnum.DRIVING);
            case EmployeeLogEldEventCode.DutyStatus_OnDuty:
                return new DutyStatusEnum(DutyStatusEnum.ONDUTY);
            case EmployeeLogEldEventCode.DutyStatus_NULL:
            default:
                return new DutyStatusEnum(DutyStatusEnum.NULL);
        }
    }


    /**
     * Method that allows us to set the hydrationMap of this object
     *
     * @param hashMap HashMap representing values to set on this object
     */
    public void setInternalObjectValueMap(HashMap<String, Object> hashMap) {
        this.internalObjectValueMap = hashMap;
    }

    /**
     * Method that returns this object's internalObjectValueMap
     *
     * @return HashMap representing values to set on this object
     */
    public HashMap<String, Object> getInternalObjectValueMap() {
        return this.internalObjectValueMap;
    }

    /**
     * Method that defines how an object extending this class will set it's values
     * after super() has been called
     *
     * @param mandateMode boolean value determining which process to use
     *                    (ELD-MANDATE or AOBRD)
     */
    @Override
    public void hydrate(boolean mandateMode) {
        this.addBaseProps(mandateMode, this._event);
        try {
            this.populateMandateProps(this._event, _event.getInternalObjectValueMap(), mandateMode);
        } catch (Exception ex) {
            ErrorLogHelper.RecordMessage(ex.getMessage());
        }
    }

    /*
     The following block of methods are overrides of the LogEvent base class's getters and setters.
     This block defines how we will use the EmployeeLogEldEvent object to store values for the
     LogEvent object. Further, it defines how we will construct object(s) that are present on a
    LogEvent but not on an EmployeeLogEldEvent, given the properties we have.

    Overrides requiring additional explanation will have method-level documentation as well
      */
    @Override
    public long getPrimaryKey() {
        return super.getPrimaryKey();
    }

    @Override
    public void setPrimaryKey(long primaryKey) {
        super.setPrimaryKey(primaryKey);
    }

    @Override
    public boolean isPrimaryKeySet() {
        return super.isPrimaryKeySet();
    }

    @Override
    public String getEobrSerialNumber() {
        if (this._event != null)
            return this._event.getEobrSerialNumber();
        return super.getEobrSerialNumber();
    }

    @Override
    public void setEobrSerialNumber(String eobrSerialNumber) {
        super.setEobrSerialNumber(eobrSerialNumber);
        if (this._event != null)
            this._event.setEobrSerialNumber(super.getEobrSerialNumber());
    }

    @Override
    public Date getStartTime() {
        if (this._event != null)
            return this._event.getEventDateTime();
        else
            return super.getStartTime();
    }

    @Override
    public void setStartTime(Date startTime) {
        super.setStartTime(startTime);
        if (this._event != null)
            this._event.setEventDateTime(startTime);
    }

    /**
     * Getter that translates an event code present on an EldEventAdapter to a corresponding Duty
     * Status Enum. Passes event type to ensure primitive matching doesn't return false values
     *
     * @return
     */
    @Override
    public DutyStatusEnum getDutyStatusEnum() {
        return translateMandateStatusToDutyStatus(this._event.getEventType(), this._event.getEventCode());
    }

    /**
     * Setter that translates a given duty status enum to it's corresponding Event Code
     *
     * @param dutyStatusEnum Duty status enum to translate
     */
    @Override
    public void setDutyStatusEnum(DutyStatusEnum dutyStatusEnum) {
        super.setDutyStatusEnum(dutyStatusEnum);
        if (this._event != null)
            this._event.setEventCode(translateDutyStatusEnumToMandateStatus(super.getDutyStatusEnum()));
    }

    /**
     * Getter that returns the internal LocationAdapter object. If the current location object is
     * null, it assigns a new LocationAdapter to the field and then returns it.
     *
     * @return LocationAdapter representing the current Location of an Event
     */
    @Override
    public LocationAdapter getLocation() {
        if (this._internalLocation == null || this._internalLocation._parentAdapter == null)
            this._internalLocation = new LocationAdapter(this._event);
        return this._internalLocation;
    }

    /**
     * Setter that accepts a Location object and translates it into an Adapter object. That object
     * is then set as the internal location for the event. Used as a bridge between AOBRD
     * and mandate mode
     *
     * @param location Location object to translate
     */
    @Override
    public void setLocation(Location location) {
        super.setLocation(location);
        if (this._event != null) {
            this._internalLocation = LocationAdapter.getAdapterFromBase(this, location);
        }
    }


    @Override
    public boolean getIsStartTimeValidated() {
        if (this._event != null)
            return this._event.getIsEventDateTimeValidated();
        return super.getIsStartTimeValidated();
    }

    @Override
    public void setIsStartTimeValidated(boolean isStartTimeValidated) {
        super.setIsStartTimeValidated(isStartTimeValidated);
        if (this._event != null)
            this._event.setIsEventDateTimeValidated(super.getIsStartTimeValidated());
    }

    @Override
    public RuleSetTypeEnum getRulesetType() {
        if (this._event != null)
            return this._event.getRuleSet();
        return super.getRulesetType();
    }

    @Override
    public void setRulesetType(RuleSetTypeEnum rulesetType) {
        super.setRulesetType(rulesetType);
        if (this._event != null)
            this._event.setRuleSet(super.getRulesetType());
    }

    /**
     * Override of base method for visibility
     * @return boolean
     */
    @Override
    public boolean getRequiresManualLocation() {
        return super.getRequiresManualLocation();
    }

    /**
     * Setter that sets the base property determining if we need manual location on an event.
     * Additionally sets status codes based on state of passed in parameter.
     *
     * @param requiresManualLocation boolean value representing state of event's manual location
     *                               requirement.
     */
    @Override
    public void setRequiresManualLocation(boolean requiresManualLocation) {
        super.setRequiresManualLocation(requiresManualLocation);
    }

    @Override
    public String getLogRemark() {
        if (this._event != null)
            return this._event.getLogRemark();
        return super.getLogRemark();
    }

    @Override
    public void setLogRemark(String logRemark) {
        super.setLogRemark(logRemark);
        if (this._event != null)
            this._event.setLogRemark(super.getLogRemark());
    }

    @Override
    public Date getLogRemarkDate() {
        if (this._event != null)
            return this._event.getLogRemarkDateTime();
        return super.getLogRemarkDate();
    }

    @Override
    public void setLogRemarkDate(Date logRemarkDate) {
        super.setLogRemarkDate(logRemarkDate);
        if (this._event != null)
            this._event.setLogRemarkDateTime(super.getLogRemarkDate());
    }

    @Override
    public boolean isExemptOffDutyStatus() {
        if (this._event != null)
            return translateMandateStatusToDutyStatus(this._event.getEventType(), this._event.getEventCode()).isExemptOffDutyStatus();
        return super.isExemptOffDutyStatus();
    }

    @Override
    public boolean isExemptOnDutyStatus() {
        if (this._event != null)
            return translateMandateStatusToDutyStatus(this._event.getEventType(), this._event.getEventCode()).isExemptOnDutyStatus();
        return super.isExemptOnDutyStatus();
    }
        /*
     The above block of methods are overrides of the LogEvent base class's getters and setters.
     This block defines how we will use the EmployeeLogEldEvent object to store values for the
     LogEvent object. Further, it defines how we will construct object(s) that are present on a
    LogEvent but not on an EmployeeLogEldEvent, given the properties we have.
      */

    /**
     * Method that adds base properties to an EldEventAdapter object, corresponding to required
     * properties for EmployeeLogEldEvents
     *
     * @param useMandateDefaults boolean representing if AOBRD or Mandate defaults will be used
     * @param adapter            EldEventAdapter object to use as values in AOBRD specific processes
     */
    private void addBaseProps(boolean useMandateDefaults, EldEventAdapter adapter) {

        //Set driver originator Id based on available data
        this.getDriverOriginatorUserIdForEvent(this);

        /*
        Set AOBRD properties if we're under AOBRD constraints
         */
        if (!useMandateDefaults) {
            this.addAOBRDBaseProps(adapter);
        }

        /*
        At present, this isn't implemented explicitly in Mandate mode, so AOBRD processes will need
        to overwrite/modify this property in calling code
         */
        this._event.setIsStartTimeValidated(true);

        //Default to original event
        this._event.setOriginalEvent(true);
        //Default to a duty status change
        if (this._event.getEventType() == null)
            this._event.setEventType(Enums.EmployeeLogEldEventType.DutyStatusChange);

        if (this._event.getEventRecordStatus() == null)
            this._event.setEventRecordStatus(Enums.EmployeeLogEldEventRecordStatus.Active.getValue());

        if (this._event.getEventRecordOrigin() == null)
            this._event.setEventRecordOrigin(Enums.EmployeeLogEldEventRecordOrigin.AutomaticallyRecorded);
    }

    /**
     * Method that adds the AOBRD base properties, as they differ from the ELD-MANDATE specific
     * ones. Allows for overriding base properties set in initial addBaseProps call
     *
     * @param adapter object to pull values from and assign to this object
     */
    private void addAOBRDBaseProps(EldEventAdapter adapter) {
        //Always set the sequence Id for AOBRD events to the AOBRD value
        this._event.setEventSequenceIDNumber(EmployeeLogEldEnum.AOBRD);

        if (this._event.getEventCode() == EmployeeLogEldEventCode.DutyStatus_NULL)
            this._event.setEventCode(translateDutyStatusEnumToMandateStatus(adapter.getDutyStatusEnum()));


        if (!this._event.isPrimaryKeySet() && adapter.isPrimaryKeySet())
            this._event.setPrimaryKey(adapter.getPrimaryKey());

        if (this._event.getEventRecordStatus() == null)
            this._event.setEventRecordStatus(adapter._event.getEventRecordStatus());

        if (this._event.getEventRecordOrigin() == null)
            this._event.setEventRecordOrigin(adapter._event.getEventRecordOrigin());
    }

    /**
     * Method that uses a HashMap containing objects to populate various properties on an event.
     * This will use all information present in the HashMap, and will differ to GlobalState values
     * should they not be present in the map object.
     *
     * @param evtToAdd      Event to add properties to
     * @param objectHashMap HashMap of objects containing values to be set
     * @throws Exception If the hash map isn't null but doesn't have base required values, an
     *                   exception will be generated
     */
    private void populateMandateProps(EldEventAdapter evtToAdd, HashMap<String, Object> objectHashMap, boolean isMandateMode) throws Exception {
        //Variable dec
        Exception notEnoughEventData;
        EmployeeLog empLog = null;
        GlobalState currentGlobalState = GlobalState.getInstance();
        StatusRecord record = null;
        Location location = null;
        Enums.EmployeeLogEldEventType eventType = null;
        Bundle primitiveValueBundle;
        int eventCode, logKey = -1;
        String tractorNumbers = null, trailerNumbers = null, shipmentInfo = null, trailerPlate = null, vehiclePlate = null, eobrSerialNumber = null;

        /*It is entirely possible that we are calling this during initialization of the event,
        in which case we just want to return rather than blowing up
        If we HAVE an object that's instantiated, but it's empty, something went wrong and the
        following exceptions can handle that.
        Also define a boolean to represent if we are to use the (optional) provided EmployeeLog
        to pull values*/
        boolean empLogNull = true, hydrateWithProvidedProps = !(objectHashMap == null);

        /*If we have a map object, but the object is empty, we will throw an exception.
        * There should NEVER be a case where we'd want to pass an empty map object, it should
        * just be null or have at minimum the required primitive bundle*/
        if (hydrateWithProvidedProps && objectHashMap.isEmpty()) {
            notEnoughEventData = new Exception("No value objects provided.");
            ErrorLogHelper.RecordMessage(notEnoughEventData.getMessage());
            throw notEnoughEventData;
        }

        //Parse out HashMap to objects
        if (hydrateWithProvidedProps) {
            //EmployeeLog
            empLogNull = (empLog = (EmployeeLog) objectHashMap.get(EMPLOYEELOGOBJECTNAME)) == null;
            //StatusRecord
            record = (StatusRecord) objectHashMap.get(STATUSRECORDOBJECTNAME);
            //Location
            location = (Location) objectHashMap.get(LOCATIONOBJECTNAME);
            /*Primitive value bundle containing values for event type, log key, event date time,
            * and event code*/
            primitiveValueBundle = (Bundle) objectHashMap.get(PRIMITIVEBUNDLENAME);

            if (primitiveValueBundle != null) {
                Object eventTypeObj = primitiveValueBundle.get(EVENTTYPEKEYNAME);
                Object logKeyObj = primitiveValueBundle.get(LOGKEYNAME);
                Object eventDateTimeObj = primitiveValueBundle.get(EVENTDATETIMENAME);

                if (logKeyObj != null)
                    logKey = (int) logKeyObj;

                /*Don't allow the overwriting of a start time for an event, but allow parameter-less
                constructor create objects to pass in this value for assignment*/
                if (eventDateTimeObj != null && eventDateTimeObj.getClass().equals(Date.class) && evtToAdd.getStartTime() == null)
                    evtToAdd.setStartTime((Date) eventDateTimeObj);

                eventCode = primitiveValueBundle.getInt(EVENTCODEKEYNAME);

                if (eventTypeObj != null)
                    eventType = Enums.EmployeeLogEldEventType.setFromInt((int) eventTypeObj);

                evtToAdd._event.setEventCode(eventCode);
                evtToAdd._event.setEventType(eventType);
                evtToAdd._event.setLogKey(logKey);
                evtToAdd._event.setEldMalfunctionIndicatorStatus(primitiveValueBundle.getBoolean(MALFUNCTION_INDICATOR_STATUS_NAME, evtToAdd._event.getEldMalfunctionIndicatorStatus()));
                evtToAdd._event.setDriverDataDiagnosticEventIndicatorStatus(primitiveValueBundle.getBoolean(DATA_DIAGNOSTIC_INDICATOR_STATUS_NAME, evtToAdd._event.getDriverDataDiagnosticEventIndicatorStatus()));
            }


            /*
            If we have an employee log, use it to pull trip info, eobr serial number, ruleset info,
            and the log key.
             */
            if (!empLogNull) {
                trailerPlate = empLog.getTrailerPlate();
                vehiclePlate = empLog.getVehiclePlate();
                shipmentInfo = empLog.getShipmentInformation();
                tractorNumbers = empLog.getTractorNumbers();
                trailerNumbers = empLog.getTrailerNumbers();
                //If we are passing a log that isn't null in with an event, we should assume that we want to associate that event with that log,
                //provided we haven't already associated it with a log and the log has a primary key set
                if (evtToAdd._event.getLogKey() == null || evtToAdd._event.getLogKey() == -1 && empLog.isPrimaryKeySet())
                    evtToAdd._event.setLogKey((int) empLog.getPrimaryKey());
                evtToAdd.setRulesetType(empLog.getRuleset());
                eobrSerialNumber = empLog.getMobileEobrIdentifier();
            }
        }

        /*
        If the event's LogKey or the passed in EmployeeLog's Id are that of the current log, use GlobalState as defaults
        If the above trip info and/or eobr/trip info is blank, try pulling it from the current GlobalState
        object present on this object.
        The exception to this rule is going to be if we're in a team driver/multi-user scenario, as there is potential for a user to be driver but not current user
         */
        boolean canUseGlobalStateTripInfo = (currentGlobalState.getCurrentEmployeeLog() != null &&
                evtToAdd._event.getLogKey() != null &&
                evtToAdd._event.getLogKey() == currentGlobalState.getCurrentEmployeeLog().getPrimaryKey()) ||
                (!empLogNull && currentGlobalState.getCurrentEmployeeLog() != null && empLog.getPrimaryKey() == currentGlobalState.getCurrentEmployeeLog().getPrimaryKey()) &&
                (currentGlobalState.getTeamDriverMode() == GlobalState.TeamDriverModeEnum.NONE);

        if (canUseGlobalStateTripInfo) {
            if (trailerPlate == null || !(trailerPlate.length() > 0))
                trailerPlate = currentGlobalState.get_currentTrailerPlate();
            if (vehiclePlate == null || !(vehiclePlate.length() > 0))
                vehiclePlate = currentGlobalState.get_currentVehiclePlate();
            if (shipmentInfo == null || !(shipmentInfo.length() > 0))
                shipmentInfo = currentGlobalState.get_currentShipmentInfo();
            if (tractorNumbers == null || !(tractorNumbers.length() > 0))
                tractorNumbers = currentGlobalState.get_currentTractorNumbers();
            if (trailerNumbers == null || !(trailerNumbers.length() > 0))
                trailerNumbers = currentGlobalState.get_currentTrailerNumbers();
            if (eobrSerialNumber == null || !(eobrSerialNumber.length() > 0))
                eobrSerialNumber = currentGlobalState.getCurrentEobrSerialNumber();
        }

        //Set TripInfo data if under AOBRD or if value is blank under Mandate (new event)
        if (evtToAdd._event.getTrailerPlate() == null || evtToAdd._event.getTrailerPlate().equals("") || !isMandateMode)
            evtToAdd._event.setTrailerPlate(trailerPlate);
        if (evtToAdd._event.getVehiclePlate() == null || evtToAdd._event.getVehiclePlate().equals("") || !isMandateMode)
            evtToAdd._event.setVehiclePlate(vehiclePlate);
        if (evtToAdd._event.getShipmentInfo() == null || evtToAdd._event.getShipmentInfo().equals("") || !isMandateMode)
            evtToAdd._event.setShipmentInfo(shipmentInfo);
        if (evtToAdd._event.getTractorNumber() == null || evtToAdd._event.getTractorNumber().equals("") || !isMandateMode)
            evtToAdd._event.setTractorNumber(tractorNumbers);
        if (evtToAdd._event.getTrailerNumber() == null || evtToAdd._event.getTrailerNumber().equals("") || !isMandateMode)
            evtToAdd._event.setTrailerNumber(trailerNumbers);

        evtToAdd._event.setEobrSerialNumber(eobrSerialNumber);

        if (location != null)
            evtToAdd.setLocation(location);

        /*
        If a status record was provided, calculate distance since last coordinates. Additional
        functionality related to StatusRecords can be added
         */
        if (record != null) {
            evtToAdd._event.setDistanceSinceLastCoordinates(calculateDistanceSinceLastValidCoordinates(record.getGpsUncertDistance()));
        }
    }

    /**
     * Method that populates the DriverOriginatorUserId for an event based on event properties
     *
     * @param evtToAdd Event to populate
     */
    private void getDriverOriginatorUserIdForEvent(EldEventAdapter evtToAdd) {
        GlobalState currentState = GlobalState.getInstance();

        //Default to current user
        String originatorId = currentState.getCurrentUser().getCredentials().getEmployeeId();

        //If this event is a ELD-generated event (driving, login/logout, diag, power event)
        //Use the current log's employee Id
        if ((evtToAdd._event.getEventType() == Enums.EmployeeLogEldEventType.IntermediateLog ||
                evtToAdd._event.getEventType() == Enums.EmployeeLogEldEventType.EnginePowerUpPowerDown ||
                evtToAdd._event.getEventType() == Enums.EmployeeLogEldEventType.Malfunction_DataDiagnosticDetection ||
                (evtToAdd._event.getEventType() == Enums.EmployeeLogEldEventType.DutyStatusChange && evtToAdd._event.getEventCode() == EmployeeLogEldEventCode.DutyStatus_Driving))) {
            EmployeeLog currentLog = currentState.getCurrentDriversLog();
            if (currentLog != null) {
                originatorId = currentLog.getEmployeeId();
            }
        }

        //Going to always use current user at present
        evtToAdd._event.setDriverOriginatorUserId(originatorId);
    }
}
