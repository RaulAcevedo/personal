package com.jjkeller.kmbapi.controller.dataaccess.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.dataaccess.AbstractDBAdapter;
import com.jjkeller.kmbapi.controller.dataaccess.ApplicationStateFacade;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.controller.utility.EmployeeLogUtilities;
import com.jjkeller.kmbapi.eldmandate.EventSequenceIdGenerator;
import com.jjkeller.kmbapi.employeelogeldevents.EmployeeLogEldEventCode;
import com.jjkeller.kmbapi.employeelogeldevents.Enums;
import com.jjkeller.kmbapi.employeelogeldevents.UnclaimedEventDTO;
import com.jjkeller.kmbapi.enums.EmployeeLogEldEnum;
import com.jjkeller.kmbapi.enums.RuleSetTypeEnum;
import com.jjkeller.kmbapi.enums.UnidentifiedEldEventStatus;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.jjkeller.kmbapi.common.Constants.RELATED_EVENT;

public class EmployeeLogEldEventPersist<T extends EmployeeLogEldEvent> extends AbstractDBAdapter<T> {

    private long _employeeLogKey = -1;
    ///////////////////////////////////////////////////////////////////////////////////////
    // private members
    ///////////////////////////////////////////////////////////////////////////////////////
    private static final String DRIVERORIGINATORUSERID = "DriverOriginatorUserId";
    private static final String ENCOMPASSORIGINATORUSERID = "EncompassOriginatorUserId";
    private static final String UNIDENTIFIEDUSERID = "UnidentifiedUserId";
    private static final String EOBRSERIALNUMBER = "EobrSerialNumber";
    private static final String LOGKEY = "LogKey";
    private static final String EVENTSEQUENCEIDNUMBER = "EventSequenceIdNumber";
    private static final String EVENTRECORDSTATUS = "EventRecordStatus";
    private static final String EVENTRECORDORIGIN = "EventRecordOrigin";
    private static final String EVENTTYPE = "EventType";
    private static final String EVENTCODE = "EventCode";
    private static final String EVENTDATETIME = "EventDateTime";
    private static final String RULESET = "RuleSet";
    private static final String ACCUMULATEDVEHICLEMILES = "AccumulatedVehicleMiles";
    private static final String ODOMETER = "Odometer";
    private static final String DISTANCE = "Distance";
    private static final String ENGINEHOURS = "EngineHours";
    private static final String LATITUDE = "Latitude";
    private static final String LONGITUDE = "Longitude";
    private static final String LATITUDESTATUSCODE = "LatitudeStatusCode";
    private static final String LONGITUDESTATUSCODE = "LongitudeStatusCode";
    private static final String ISGPSATREDUCEDPRECISION = "IsGpsAtReducedPrecision";
    private static final String DISTANCESINCELASTCOORDINATES = "DistanceSinceLastCoordinates";
    private static final String ELDMALFUNCTIONINDICATORSTATUS = "EldMalfunctionIndicatorStatus";
    private static final String DRIVERDATADIAGNOSTICEVENTINDICATORSTATUS = "DriverDataDiagnosticEventIndicatorStatus";
    private static final String EVENTCOMMENT = "EventComment";
    private static final String DRIVERSLOCATIONDESCRIPTION = "DriversLocationDescription";
    private static final String EVENTDATACHECK = "EventDataCheck";
    private static final String DIAGNOSTICCODE = "DiagnosticCode";
    private static final String GEOLOCATION = "Geolocation";
    private static final String TRACTORNUMBER = "TractorNumber";
    private static final String VEHICLEPLATE = "VehiclePlate";
    private static final String TRAILERNUMBER = "TrailerNumber";
    private static final String TRAILERPLATE = "TrailerPlate";
    private static final String SHIPMENTINFO = "ShipmentInfo";
    private static final String LOGREMARK = "LogRemark";
    private static final String ORIGINALEVENT = "OriginalEvent";
    private static final String ISEVENTDATETIMEVALIDATED = "IsEventDateTimeValidated";
    private static final String ENDODOMETER = "EndOdometer";
    private static final String LOGREMARKDATETIME = "LogRemarkDateTime";
    private static final String ISMANUALLYEDITEDBYKMBUSER = "IsManuallyEditedByKMBUser";
    private static final String GPSTIMESTAMP = "GpsTimestamp";
    private static final String EDITDURATION = "EditDuration";
    private static final String MOTIONPICTUREAUTHORITYID = "MotionPictureAuthorityId";
    private static final String MOTIONPICTUREPRODUCTIONID = "MotionPictureProductionId";
    private static final String ENCOMPASSCLUSTERPK = "EncompassClusterPK";
    private static final String UNIDENTIFIEDEVENTSTATUS = "UnidentifiedEventStatus";
    private static final String IS_REVIEWED = "IsReviewed";
    private static final String UNIDENTIFIEDEVENTCONFIDENCELEVEL = "UnidentifiedEventConfidenceLevel";
    private static final String UNIDENTIFIEDEVENTSUGGESTEDDRIVER = "UnidentifiedEventSuggestedDriver";

    private static final int ADD_NEW = -1;

    private final String SQL_SELECT_PRIMARYKEY_COMMAND = "select [Key] from [EmployeeLogEldEvent] where LogKey=? AND EventDateTime=? AND EventType=? AND EventCode=? AND EventRecordStatus=? AND EventSequenceIdNumber=?";

    private final String SQL_IS_VALID_INSERT_COMMAND = "select COUNT(1) from [EmployeeLogEldEvent] where EventType = 1 AND EventRecordStatus = 1 AND LogKey = ? AND EventDateTime = ?";
    private final String SQL_IS_VALID_UPDATE_COMMAND = "select COUNT(1) from [EmployeeLogEldEvent] where EventType = 1 AND EventRecordStatus = 1 AND LogKey = ? AND EventDateTime = ? AND Key <> ?";

    private static final String SQL_SELECT_CERTIFICATIONCOUNTBYLOGKEY_COMMAND = "select count(1) from EmployeeLogEldEvent where EventType = 4 AND LogKey = ?";

    private static final String SQL_SELECT_COMMAND = "select * from EmployeeLogEldEvent where LogKey=? order by EventDateTime, EventType, EventCode, EventRecordStatus, EventSequenceIdNumber desc";

    private static final String SQL_SELECT_KEY_COMMAND = "select * from EmployeeLogEldEvent where Key=?";

    private static final String SQL_SELECT_KEY_PC_RECORDS = "SELECT * FROM EmployeeLogEldEvent WHERE EventDateTime = (SELECT EventDateTime from EmployeeLogEldEvent WHERE  Key = ?) AND EventType = 3 AND EventCode = 1 UNION SELECT * FROM EmployeeLogEldEvent WHERE  Key = ? UNION SELECT * FROM (SELECT * FROM EmployeeLogEldEvent WHERE Key > ? AND EventType = 3 AND EventCode = 0 LIMIT 1)";

    private static final String SQL_SELECT_LAST_BY_EVENT_TYPE = "SELECT * FROM EmployeeLogEldEvent WHERE EventType = ? ORDER BY [KEY] DESC LIMIT 1";

    private static final String SQL_SELECT_MALFUNCTION_EVENT_TYPE = "SELECT * FROM EmployeeLogEldEvent WHERE EventType = 7 ORDER BY [KEY] DESC";

    private static final String SQL_SELECT_MALFUNCTION_DATA_COMPLIANCE = "SELECT * FROM EmployeeLogEldEvent WHERE EventType = 7 ORDER BY [KEY] DESC";

    private static final String SQL_SELECT_ACTIVE_NATURALKEY_COMMAND = "SELECT * FROM EmployeeLogEldEvent WHERE LogKey=? AND EventType=? AND EventCode=? AND EventDateTime=? AND EventRecordStatus = 1";

    private static final String SQL_LAST_LOGIN_EVENT = "select * from EmployeeLogEldEvent where DriverOriginatorUserId = ? AND EventType = ? AND EventCode = ? AND (LogKey = NULL OR LogKey < 1) order by [EventDateTime] DESC LIMIT 1";

    private static final String SQL_SELECT_UNASSIGNED_EVENTS_TO_PURGE_COMMAND = "select Key from EmployeeLogEldEvent where EventDateTime < ? and (UnidentifiedEventStatus = ? or UnidentifiedEventStatus = ?)";
    private static final String SQL_PURGE_ELD_EVENT = "delete from EmployeeLogEldEvent where Key = ?";

    //+++++++++++++++++++++THIS IS DIFFERENT+++++++++++++++++++++
    /*
    2016.06.08 banderson: Added special SQL to deal with Adaption between LogEvent and EmployeeLogEldEvent
    This uses a placeholder to conditionally add an IN clause based on absence or presence of RuleSetTypeEnum specifications
     */
    private static final String SQL_MOSTRECENT_ELDEVENT_WITH_RULESET_PARAMS_COMMAND = "SELECT EmployeeLogEldEvent.* FROM EmployeeLogEldEvent " +
            "INNER JOIN EmployeeLog on EmployeeLogEldEvent.LogKey = EmployeeLog.[Key] " +
            "WHERE EmployeeLogEldEvent.DriverOriginatorUserId = ? " +
            "AND EmployeeLogEldEvent.EventRecordStatus = 1 " +  /* 1 = Active */
            "AND EmployeeLog.LogSourceStatusEnum in (1,3) " +
            "AND EmployeeLog.LogDate < ?" +
            "{0}" +
            "ORDER BY EmployeeLogEldEvent.EventDateTime DESC";
    //+++++++++++++++++++++THIS IS DIFFERENT+++++++++++++++++++++
    private static final String SQL_SELECT_BYEVENTTYPES_COMMAND = "select * from [EmployeeLogEldEvent] where [LogKey]=? and [EventType] in (?) and [EventRecordStatus] in (?) order by EventDateTime asc";
    private static final String SQL_SELECT_UNSUBMITTED_LOCATIONS_FOR_GEOCODE_COMMAND = "SELECT EmployeeLogEldEvent.* FROM EmployeeLogEldEvent " +
            "INNER JOIN EmployeeLog on EmployeeLogEldEvent.LogKey = EmployeeLog.[Key] " +
            "WHERE EmployeeLog.LogSourceStatusEnum = 1 AND " +
            "EmployeeLogEldEvent.DriversLocationDescription IS NOT NULL AND " +
            "EmployeeLogEldEvent.EventRecordStatus = 1 AND " +  /* 1 = Active */
            " (EmployeeLogEldEvent.GEOLOCATION IS NULL OR EmployeeLogEldEvent.GEOLOCATION = '')";

    private static final String SQL_SELECT_ACTIVE_BY_USER_DIAGNOSTIC_MALFUNCTIONS_COMMAND = "SELECT EmployeeLogEldEvent.* " +
            "FROM EmployeeLogEldEvent " +
            "INNER JOIN EmployeeLog on EmployeeLog.Key = EmployeeLogEldEvent.LogKey " +
            "WHERE EmployeeLog.UserKey = ? " +
            "  AND EmployeeLogEldEvent.EventType = ? " +
            "  AND EmployeeLogEldEvent.DiagnosticCode = ? " +
            "  AND EmployeeLogEldEvent.EventDateTime < ? " +
            "  AND EmployeeLogEldEvent.EventRecordStatus = 1 " +  // 1 = Active
            "ORDER BY EventDateTime asc";
    private static final String SQL_SELECT_MOSTRECENT_CHANGEINDRIVERSINDICATION_BYDRIVERID = "Select * From EmployeeLogEldEvent WHERE EventType = 3 AND DriverOriginatorUserId = ? ORDER BY EventDateTime DESC LIMIT 1 ";
    private static final String SQL_SELECT_MOSTRECENT_IGNITIONOFF_BYDRIVERID = "select * from EmployeeLogEldEvent where DriverOriginatorUserId = ? and EventType = 6 and (EventCode = 3 OR EventCode = 4) ORDER BY EventDateTime DESC limit 1";
    private static final String SQL_SELECT_MOSTRECENT_IGNITIONON_BYDRIVERID = "select * from EmployeeLogEldEvent where DriverOriginatorUserId = ? and EventType = 6 and (EventCode = 1 OR EventCode = 2) ORDER BY EventDateTime DESC limit 1";

    // used by RodsEntryBase.java hasUnidentifiedEmployeeEventsToReview()
    // displays unidentified events screen
    private static final String SQL_SELECT_UNREVIEWED_UNIDENTIFIED_EVENTS = "SELECT * " +
            "FROM EmployeeLogEldEvent " +
            "WHERE IsReviewed = 0 AND UnidentifiedEventStatus = " + UnidentifiedEldEventStatus.LOCAL.value +
            " AND EventRecordStatus = 1 " +  // 1 = Active
            " AND EventType <> 6 " +     // Filter out 6 (Engine events: Power-up/shut-down)
            " ORDER BY EventDateTime desc, EventSequenceIdNumber desc";

    private static final String SQL_SELECT_UNREVIEWED_WITHOUTCONFIDENCELEVEL_UNIDENTIFIED_EVENTS = "SELECT * " +
            "FROM EmployeeLogEldEvent " +
            "WHERE IsReviewed = 0 AND UnidentifiedEventStatus = " + UnidentifiedEldEventStatus.LOCAL.value +
            " AND UNIDENTIFIEDEVENTCONFIDENCELEVEL IS NULL " +
            " AND EventRecordStatus = 1 " +  // 1 = Active
            " AND EventType <> 6 " +     // Filter out 6 (Engine events: Power-up/shut-down)
            " AND EventDateTime >= DATETIME('now','-7 day')" +
            " ORDER BY EventDateTime desc, EventSequenceIdNumber desc";

    // System Menu, Records, Unidentified ELD Events
    private static final String SQL_SELECT_UNSUBMITTED_UNIDENTIFIED_EVENTS = "SELECT * " +
            "FROM EmployeeLogEldEvent " +
            "WHERE UnidentifiedEventStatus = " + UnidentifiedEldEventStatus.LOCAL.value +
            " AND EventRecordStatus = 1 " +  // 1 = Active
            " AND IsReviewed = 1 " +
            " AND EventType <> 6 " +     // Filter out 6 (Engine events: Power-up/shut-down)
            "ORDER BY EventDateTime desc, EventSequenceIdNumber desc";

    //used by EmployeeLogEldMandateController.java SubmitUnidentifiedEldEvents()
    //submit to encompass
    private static final String SQL_SELECT_UNSUBMITTED_UNIDENTIFIED_EVENTS_TOSUBMITLOGS = "SELECT * " +
            "FROM EmployeeLogEldEvent " +
            "WHERE UnidentifiedEventStatus = " + UnidentifiedEldEventStatus.LOCAL.value +
            " AND EventRecordStatus = 1 " +  // 1 = Active
            " AND (IsReviewed = 1 OR EventType = 6) " +  // EventType 6 records are hidden from being reviewed by the driver but still need to go to Encompass
            "ORDER BY EventDateTime desc, EventSequenceIdNumber desc";

    private static final String SQL_SELECT_SUBMITTED_UNSYNCHRONIZED_UNIDENTIFIED_EVENTS = "SELECT * " +
            "FROM EmployeeLogEldEvent " +
            "WHERE UnidentifiedEventStatus = " + UnidentifiedEldEventStatus.SUBMITTED.value +
            " AND EncompassClusterPK < 1 ";
    private static final String SQL_SELECT_SUBMITTED_UNIDENTIFIED_EVENTS = "SELECT * " +
            "FROM EmployeeLogEldEvent " +
            "WHERE UnidentifiedEventStatus = " + UnidentifiedEldEventStatus.SUBMITTED.value +
            " AND EncompassClusterPK > 0";
    private String SQL_SELECT_PREVIOUS_WEEK_UNIDENTIFIED_EVENTS = "SELECT * " +
            "FROM EmployeeLogEldEvent " +
            "WHERE UnidentifiedEventStatus IN ( " +
            UnidentifiedEldEventStatus.LOCAL.value + ", " +
            UnidentifiedEldEventStatus.SUBMITTED.value +
            ") AND EventDateTime >= date('now','-6 day') " +
            "AND " + EOBRSERIALNUMBER + " = ?" +
            " ORDER BY EventDateTime desc";
    private String SQL_SELECT_UNIDENTIFIED_DRIVING_EVENTS_BY_ELD = "SELECT * " +
            "FROM EmployeeLogEldEvent " +
            "WHERE UnidentifiedEventStatus IN ( " +
            UnidentifiedEldEventStatus.LOCAL.value + ", " +
            UnidentifiedEldEventStatus.SUBMITTED.value +
            ") AND EventRecordStatus = 1 " +  // 1 = Active
            "AND EventType = 1 " +
            "AND EventCode in (3,4) " +
            "AND " + EOBRSERIALNUMBER + " = ? " +
            "AND EventDateTime >= ? " +
            " ORDER BY EventDateTime, EventSequenceIdNumber";
    private String SQL_SELECT_UNIDENTIFIED_DRIVING_DIAGNOSTIC_EVENTS_BY_ELD = "SELECT * " +
            "FROM EmployeeLogEldEvent " +
            "WHERE EventRecordStatus = 1 " +  // 1 = Active
            "AND EventType = 7 " +
            "AND EventCode in (3,4) " +
            "AND DiagnosticCode = 5 " +
            "AND " + EOBRSERIALNUMBER + " = ? " +
            "AND EventDateTime >= ? " +
            "ORDER BY EventDateTime DESC";

    private EventSequenceIdGenerator eventSequenceIdGenerator;
    ///////////////////////////////////////////////////////////////////////////////////////
    // constructors
    ///////////////////////////////////////////////////////////////////////////////////////
    public EmployeeLogEldEventPersist(Class<T> clazz, Context ctx) {
        super(clazz, ctx);

        setDbTableName(DB_TABLE_EMPLOYEELOGELDEVENT);

        eventSequenceIdGenerator = new EventSequenceIdGenerator(new ApplicationStateFacade(this.getContext()));
    }

    public EmployeeLogEldEventPersist(Class<T> clazz, Context ctx, User user, long employeeLogKey) {
        super(clazz, ctx, user);

        _employeeLogKey = employeeLogKey;

        setDbTableName(DB_TABLE_EMPLOYEELOGELDEVENT);

        eventSequenceIdGenerator = new EventSequenceIdGenerator(new ApplicationStateFacade(this.getContext()));
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // @Override methods
    ///////////////////////////////////////////////////////////////////////////////////////
    @Override
    public String getSelectPrimaryKeyCommand() {
        return SQL_SELECT_PRIMARYKEY_COMMAND;
    }

    @Override
    protected String[] getSelectPrimaryKeyArgs(T data) {
        String logKey = "";
        if (data.getLogKey() != null)
            logKey = Integer.toString(data.getLogKey());

        String evtDateTime = DateUtility.getHomeTerminalSqlDateTimeFormat(this.getCurrentUser()).format(data.getEventDateTime());
        String evtType = Integer.toString(data.getEventType().getValue());
        String evtCode = Integer.toString(data.getEventCode());
        String evtRecStatus = Integer.toString(data.getEventRecordStatus());
        String evtSeqIdNum = Integer.toString(data.getEventSequenceIDNumber());

        return new String[]{logKey, evtDateTime, evtType, evtCode, evtRecStatus, evtSeqIdNum};
    }

    protected String getSelectCommand() {
        return SQL_SELECT_COMMAND;
    }

    @Override
    protected String[] getSelectArgs() {
        return new String[]{String.valueOf(_employeeLogKey)};
    }

    @Override
    protected T BuildObject(Cursor cursorData) {
        Date eventDateTime = ReadValue(cursorData, EVENTDATETIME, (Date) null, DateUtility.getHomeTerminalSqlDateTimeFormat(this.getCurrentUser()));
        int eventCode = ReadValue(cursorData, EVENTCODE, 0);
        Enums.EmployeeLogEldEventType type = Enums.EmployeeLogEldEventType.setFromInt(ReadValue(cursorData, EVENTTYPE, 1));
        //Use the above two non-null values to construct and initialize an EmployeeLogEldEvent for our return
        EmployeeLogEldEvent data = new EmployeeLogEldEvent(eventDateTime, new EmployeeLogEldEventCode(eventCode), type);

        data.setPrimaryKey(ReadValue(cursorData, KEY, (long) 0));
        data.setDriverOriginatorUserId(ReadValue(cursorData, DRIVERORIGINATORUSERID, (String) null));
        data.setEncompassOriginatorUserId(ReadValue(cursorData, ENCOMPASSORIGINATORUSERID, (String) null));
        data.setUnidentifiedUserId(ReadValue(cursorData, UNIDENTIFIEDUSERID, (String) null));
        data.setEobrSerialNumber(ReadValue(cursorData, EOBRSERIALNUMBER, (String) null));
        data.setLogKey(ReadValue(cursorData, LOGKEY, (Integer) null));
        data.setEventSequenceIDNumber(ReadValue(cursorData, EVENTSEQUENCEIDNUMBER, EmployeeLogEldEnum.DEFAULT));
        data.setEventRecordStatus(ReadValue(cursorData, EVENTRECORDSTATUS, (Integer) null));
        data.setEventRecordOrigin(ReadValue(cursorData, EVENTRECORDORIGIN, (Integer) null));
        data.getRuleSet().setValue(ReadValue(cursorData, RULESET, RuleSetTypeEnum.NULL));
        data.setAccumulatedVehicleMiles(ReadValue(cursorData, ACCUMULATEDVEHICLEMILES, (Integer) null));
        data.setOdometer(ReadValue(cursorData, ODOMETER, (Float) null));
        data.setDistance(ReadValue(cursorData, DISTANCE, (Integer) null));
        data.setEngineHours(ReadValue(cursorData, ENGINEHOURS, (Double) null));
        data.setLatitude(ReadValue(cursorData, LATITUDE, (Double) null));
        data.setLongitude(ReadValue(cursorData, LONGITUDE, (Double) null));
        data.setLatitudeStatusCode(ReadValue(cursorData, LATITUDESTATUSCODE, (String) null));
        data.setLongitudeStatusCode(ReadValue(cursorData, LONGITUDESTATUSCODE, (String) null));
        data.setIsGPSAtReducedPrecision(ReadValue(cursorData, ISGPSATREDUCEDPRECISION, false));
        data.setDistanceSinceLastCoordinates(ReadValue(cursorData, DISTANCESINCELASTCOORDINATES, (Float) null));
        data.setEldMalfunctionIndicatorStatus(ReadValue(cursorData, ELDMALFUNCTIONINDICATORSTATUS, false));
        data.setDriverDataDiagnosticEventIndicatorStatus(ReadValue(cursorData, DRIVERDATADIAGNOSTICEVENTINDICATORSTATUS, false));
        data.setEventComment(ReadValue(cursorData, EVENTCOMMENT, (String) null));
        data.setDriversLocationDescription(ReadValue(cursorData, DRIVERSLOCATIONDESCRIPTION, (String) null));
        data.setEventDataCheck(ReadValue(cursorData, EVENTDATACHECK, (String) null));
        data.setDiagnosticCode(ReadValue(cursorData, DIAGNOSTICCODE, (String) null));
        data.setGeolocation(ReadValue(cursorData, GEOLOCATION, (String) null));
        data.setTractorNumber(ReadValue(cursorData, TRACTORNUMBER, (String) null), true);
        data.setVehiclePlate(ReadValue(cursorData, VEHICLEPLATE, (String) null));
        data.setTrailerNumber(ReadValue(cursorData, TRAILERNUMBER, (String) null));
        data.setTrailerPlate(ReadValue(cursorData, TRAILERPLATE, (String) null));
        data.setShipmentInfo(ReadValue(cursorData, SHIPMENTINFO, (String) null));
        data.setLogRemark(ReadValue(cursorData, LOGREMARK, (String) null));
        data.setOriginalEvent(ReadValue(cursorData, ORIGINALEVENT, false));
        data.setIsEventDateTimeValidated(ReadValue(cursorData, ISEVENTDATETIMEVALIDATED, false));
        data.setEndOdometer(ReadValue(cursorData, ENDODOMETER, (Float) null));
        data.setLogRemarkDateTime(ReadValue(cursorData, LOGREMARKDATETIME, (Date) null, DateUtility.getHomeTerminalSqlDateTimeFormat(this.getCurrentUser())));
        data.setIsManuallyEditedByKMBUser(ReadValue(cursorData, ISMANUALLYEDITEDBYKMBUSER, false));
        data.setGpsTimestamp(ReadValue(cursorData, GPSTIMESTAMP, (Date) null, DateUtility.getHomeTerminalSqlDateTimeFormat()));
        Integer val = ReadValue(cursorData, EDITDURATION, (Integer) null);
        data.setEditDuration(val != null ? Long.valueOf(val) : null, Calendar.SECOND);
        data.setMotionPictureAuthorityId(ReadValue(cursorData, MOTIONPICTUREAUTHORITYID, (String) null));
        data.setMotionPictureProductionId(ReadValue(cursorData, MOTIONPICTUREPRODUCTIONID, (String) null));
        data.setEncompassClusterPK(ReadValue(cursorData, ENCOMPASSCLUSTERPK, 0L));
        data.setUnidentifiedEventStatus(UnidentifiedEldEventStatus.fromValue(ReadValue(cursorData, UNIDENTIFIEDEVENTSTATUS, UnidentifiedEldEventStatus.NONE.value)));
        data.setIsReviewed(ReadValue(cursorData, IS_REVIEWED, false));
        data.setRelatedKmbPK(ReadValue(cursorData, RELATED_EVENT, 0));
        data.setUnidentifiedEventConfidenceLevel(ReadValue(cursorData, UNIDENTIFIEDEVENTCONFIDENCELEVEL, (Integer) null));
        data.setUnidentifiedEventSuggestedDriver(ReadValue(cursorData, UNIDENTIFIEDEVENTSUGGESTEDDRIVER, (String) null));

        boolean reducedPrecision = data.getIsGPSAtReducedPrecision();
        if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
            if (data.getLatitude() != null) {
                data.setLatitude(EmployeeLogUtilities.GetReducedPrecisionGPSForDouble(data.getLatitude(), reducedPrecision), true);
            }
            if (data.getLongitude() != null) {
                data.setLongitude(EmployeeLogUtilities.GetReducedPrecisionGPSForDouble(data.getLongitude(), reducedPrecision), true);
            }
        }

        return (T) data;
    }

    @Override
    public void Persist(T data) {
        synchronized (EmployeeLogEldEventPersist.class) {
            super.Persist(data);
        }
    }

    private boolean shouldCheckInvalid(T data) {
        return data.getEventRecordStatus() == Enums.EmployeeLogEldEventRecordStatus.Active.getValue() &&
                data.getEventType() == Enums.EmployeeLogEldEventType.DutyStatusChange &&
                data.getLogKey() != null && data.getLogKey() > 0;
    }

    @Override
    protected Boolean isValidInsert(T data) {
        Boolean result = true;

        if (this.shouldCheckInvalid(data)) {
            String[] selectionArgs = new String[]{
                    Integer.toString(data.getLogKey()),
                    DateUtility.getHomeTerminalSqlDateTimeFormat(this.getCurrentUser()).format(data.getEventDateTime())
            };
            result = this.GetScalar(SQL_IS_VALID_INSERT_COMMAND, selectionArgs) == 0;
        }

        return result;
    }

    @Override
    protected Boolean isValidUpdate(T data) {
        Boolean result = true;

        if (this.shouldCheckInvalid(data)) {
            String[] selectionArgs = new String[]{
                    Integer.toString(data.getLogKey()),
                    DateUtility.getHomeTerminalSqlDateTimeFormat(this.getCurrentUser()).format(data.getEventDateTime()),
                    String.valueOf(data.getPrimaryKey())
            };
            result = this.GetScalar(SQL_IS_VALID_UPDATE_COMMAND, selectionArgs) == 0;
        }

        return result;
    }

    @Override
    protected ContentValues PersistContentValues(T data) {

        ContentValues content = super.PersistContentValues(data);
        PutValue(content, DRIVERORIGINATORUSERID, data.getDriverOriginatorUserId());
        PutValue(content, ENCOMPASSORIGINATORUSERID, data.getEncompassOriginatorUserId());
        PutValue(content, UNIDENTIFIEDUSERID, data.getUnidentifiedUserId());
        PutValue(content, EOBRSERIALNUMBER, data.getEobrSerialNumber());

        if (_employeeLogKey == -1) {
            PutValue(content, LOGKEY, data.getLogKey());   // employeeLogKey set when persisting downloaded parent EmployeeLog
        } else {
            data.setLogKey((int)_employeeLogKey);
            PutValue(content, LOGKEY, _employeeLogKey);    // for some unknown reason - I would get java.lang.NullPointerException when this was inline ? :
        }

        if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
            boolean reducedPrecision = data.getIsGPSAtReducedPrecision();
            if (data.getLatitude() != null) {
                PutValue(content, LATITUDE, EmployeeLogUtilities.GetReducedPrecisionGPSForDouble(data.getLatitude(), reducedPrecision));
            }
            else {
                PutValue(content, LATITUDE, null);
            }
            if (data.getLongitude() != null) {
                PutValue(content, LONGITUDE, EmployeeLogUtilities.GetReducedPrecisionGPSForDouble(data.getLongitude(), reducedPrecision));
            }
            else {
                PutValue(content, LONGITUDE, null);
            }
        } else {
            PutValue(content, LATITUDE, data.getLatitude());
            PutValue(content, LONGITUDE, data.getLongitude());
        }
        
        if (data.getEventSequenceIDNumber() == EmployeeLogEldEnum.DEFAULT) {
            data.setEventSequenceIDNumber(eventSequenceIdGenerator.GetNextSequenceNumber());
        }
        PutValue(content, EVENTSEQUENCEIDNUMBER, data.getEventSequenceIDNumber());
        PutValue(content, EVENTRECORDSTATUS, data.getEventRecordStatus());
        PutValue(content, EVENTRECORDORIGIN, data.getEventRecordOrigin());
        PutValue(content, EVENTTYPE, data.getEventType().getValue());
        PutValue(content, EVENTCODE, data.getEventCode());
        PutValue(content, EVENTDATETIME, data.getEventDateTime(), DateUtility.getHomeTerminalSqlDateTimeFormat(this.getCurrentUser()));
        PutValue(content, RULESET, data.getRuleSet().getValue());
        PutValue(content, ACCUMULATEDVEHICLEMILES, data.getAccumulatedVehicleMiles());
        PutValue(content, ODOMETER, data.getOdometer());
        PutValue(content, DISTANCE, data.getDistance());
        PutValue(content, ENGINEHOURS, data.getEngineHours());
        PutValue(content, LATITUDESTATUSCODE, data.getLatitudeStatusCode());
        PutValue(content, LONGITUDESTATUSCODE, data.getLongitudeStatusCode());
        PutValue(content, ISGPSATREDUCEDPRECISION, data.getIsGPSAtReducedPrecision());
        PutValue(content, DISTANCESINCELASTCOORDINATES, data.getDistanceSinceLastCoordinates());
        PutValue(content, ELDMALFUNCTIONINDICATORSTATUS, data.getEldMalfunctionIndicatorStatus());
        PutValue(content, DRIVERDATADIAGNOSTICEVENTINDICATORSTATUS, data.getDriverDataDiagnosticEventIndicatorStatus());
        PutValue(content, EVENTCOMMENT, data.getEventComment());
        PutValue(content, DRIVERSLOCATIONDESCRIPTION, data.getDriversLocationDescription());
        PutValue(content, DIAGNOSTICCODE, data.getDiagnosticCode());
        PutValue(content, GEOLOCATION, data.getGeolocation());
        PutValue(content, TRACTORNUMBER, data.getTractorNumber());
        PutValue(content, VEHICLEPLATE, data.getVehiclePlate());
        PutValue(content, TRAILERNUMBER, data.getTrailerNumber());
        PutValue(content, TRAILERPLATE, data.getTrailerPlate());
        PutValue(content, SHIPMENTINFO, data.getShipmentInfo());
        PutValue(content, LOGREMARK, data.getLogRemark());
        PutValue(content, ORIGINALEVENT, data.getOriginalEvent());
        PutValue(content, EVENTDATACHECK, data.getEventDataCheck());
        PutValue(content, ISEVENTDATETIMEVALIDATED, data.getIsEventDateTimeValidated());
        PutValue(content, ENDODOMETER, data.getEndOdometer());
        PutValue(content, LOGREMARKDATETIME, data.getLogRemarkDateTime(), DateUtility.getHomeTerminalSqlDateTimeFormat(this.getCurrentUser()));
        PutValue(content, ISMANUALLYEDITEDBYKMBUSER, data.getIsManuallyEditedByKMBUser());
        PutValue(content, GPSTIMESTAMP, data.getGpsTimestamp(), DateUtility.getHomeTerminalSqlDateTimeFormat());
        PutValue(content, EDITDURATION, data.getEditDuration(Calendar.SECOND));
        PutValue(content, MOTIONPICTUREAUTHORITYID, data.getMotionPictureAuthorityId());
        PutValue(content, MOTIONPICTUREPRODUCTIONID, data.getMotionPictureProductionId());
        PutValue(content, ENCOMPASSCLUSTERPK, GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled() ? data.getEncompassClusterPK() : -1L);
        PutValue(content, UNIDENTIFIEDEVENTSTATUS, data.getUnidentifiedEventStatus().value);
        PutValue(content, IS_REVIEWED, data.getIsReviewed());
        PutValue(content, RELATED_EVENT, data.getRelatedKmbPK());
        PutValue(content, UNIDENTIFIEDEVENTCONFIDENCELEVEL, data.getUnidentifiedEventConfidenceLevel());
        PutValue(content, UNIDENTIFIEDEVENTSUGGESTEDDRIVER, data.getUnidentifiedEventSuggestedDriver());

        return content;
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // custom methods
    ///////////////////////////////////////////////////////////////////////////////////////

    /**
     * Select EldEvent record based on it's primary key.
     */
    public EmployeeLogEldEvent FetchByKey(int uniqueKey) {
        return ExecuteFetchRawQuery(SQL_SELECT_KEY_COMMAND, new String[]{Integer.toString(uniqueKey)});
    }

    public EmployeeLogEldEvent[] FetchEldPCRecords(int uniqueKey) {
        List<T> empLogELDEventList = ExecuteFetchListRawQuery(SQL_SELECT_KEY_PC_RECORDS, new String[]{Integer.toString(uniqueKey), Integer.toString(uniqueKey), Integer.toString(uniqueKey)});
        return empLogELDEventList.toArray(new EmployeeLogEldEvent[empLogELDEventList.size()]);
    }

    public EmployeeLogEldEvent FetchLastByEventType(int eventType) {
        return ExecuteFetchRawQuery(SQL_SELECT_LAST_BY_EVENT_TYPE, new String[]{Integer.toString(eventType)});
    }

    public EmployeeLogEldEvent FetchActiveByNaturalKey(int logKey, int eventType, int eventCode, Date eventDateTime) {
        String[] selectionArgs = new String[]{Integer.toString(logKey), Integer.toString(eventType), Integer.toString(eventCode), DateUtility.getHomeTerminalSqlDateTimeFormat(this.getCurrentUser()).format(eventDateTime)};
        return ExecuteFetchRawQuery(SQL_SELECT_ACTIVE_NATURALKEY_COMMAND, selectionArgs);
    }

    public int FetchCertificationCountByLogKey(int logKey) {
        return GetScalar(SQL_SELECT_CERTIFICATIONCOUNTBYLOGKEY_COMMAND, new String[]{Integer.toString(logKey)});
    }

    public List<T> FetchLoginDutyStatusEvents(String currentUserId) {
        String[] selectionArgs = new String[]{currentUserId, Integer.toString(Enums.EmployeeLogEldEventType.LoginLogout.getValue()), Integer.toString(EmployeeLogEldEventCode.Login)};

        return ExecuteFetchListRawQuery(SQL_LAST_LOGIN_EVENT, selectionArgs);
    }

    public T FetchMostRecentEldEventWithRulesetCriteria(String currentUserId, Date dateFloor, ArrayList<RuleSetTypeEnum> ruleSetTypeCriteria) {
        StringBuilder sb = new StringBuilder();
        String prefix = "";
        String dynamicParameterInsertFormat = "AND EmployeeLogEldEvent.RuleSet IN ({0}) ";

        //If we don't have ruleSet arguments, get everything
        if (!(ruleSetTypeCriteria.size() > 0))
            dynamicParameterInsertFormat = " ";


        for (RuleSetTypeEnum rs : ruleSetTypeCriteria) {
            sb.append(prefix).append(rs.getValue());
            prefix = ",";
        }

        dynamicParameterInsertFormat = MessageFormat.format(dynamicParameterInsertFormat, sb.toString());

        //Instead of passing 0-N arguments, we will create the IN clause with the StringBuilder
        String argumentFormattedSql = MessageFormat.format(SQL_MOSTRECENT_ELDEVENT_WITH_RULESET_PARAMS_COMMAND, dynamicParameterInsertFormat);
        String[] selectionArgs = new String[]{currentUserId, dateFloor.toString()};

        return ExecuteFetchRawQuery(argumentFormattedSql, selectionArgs);
    }

    public List<T> FetchUnsubmittedLocationsThatRequireGeocoding() {
        return ExecuteFetchListRawQuery(SQL_SELECT_UNSUBMITTED_LOCATIONS_FOR_GEOCODE_COMMAND, null);
    }

    public EmployeeLogEldEvent[] FetchByEventTypes(int employeeLogKey, List<Integer> eventTypes, List<Integer> eventRecordStatuses) {
        // create 'IN' clause EventType in (x,x,x)
        String eventTypesInClause = "";
        for (int eventType : eventTypes) {
            if (eventTypesInClause.length() > 0)
                eventTypesInClause += ",";

            eventTypesInClause += Integer.toString(eventType);
        }

        // create 'IN' clause EventRecordStatus in (x,x,x)
        String eventRecordStatusesInClause = "";
        for (int eventRecordStatus : eventRecordStatuses) {
            if (eventRecordStatusesInClause.length() > 0)
                eventRecordStatusesInClause += ",";

            eventRecordStatusesInClause += Integer.toString(eventRecordStatus);
        }

        String[] selectionArgs = new String[]{Integer.toString(employeeLogKey)};

        String sql_select_byeventtypes_command = SQL_SELECT_BYEVENTTYPES_COMMAND.replace("[EventType] in (?)", "[EventType] in (" + eventTypesInClause + ")");
        sql_select_byeventtypes_command = sql_select_byeventtypes_command.replace("[EventRecordStatus] in (?)", "[EventRecordStatus] in (" + eventRecordStatusesInClause + ")");
        List<T> empLogELDEventList = ExecuteFetchListRawQuery(sql_select_byeventtypes_command, selectionArgs);

        return empLogELDEventList.toArray(new EmployeeLogEldEvent[empLogELDEventList.size()]);
    }

    public List<T> FetchActiveByUserAndDiagnosticCode(String diagnosticCode, Date timestamp) {
        String[] selectionArgs = {
                String.valueOf(getCurrentUser().getCredentials().getPrimaryKey()),
                String.valueOf(Enums.EmployeeLogEldEventType.Malfunction_DataDiagnosticDetection.getValue()),
                diagnosticCode,
                DateUtility.getHomeTerminalSqlDateTimeFormat(this.getCurrentUser()).format(timestamp)
        };
        return ExecuteFetchListRawQuery(SQL_SELECT_ACTIVE_BY_USER_DIAGNOSTIC_MALFUNCTIONS_COMMAND, selectionArgs);
    }


    /**
     * Method that updates an Eld event with data provided, and ignores null data
     *
     * @param employeeLogEldEvent event to update
     */
    public void UpdateEmployeeLogEldEvent(T employeeLogEldEvent) {
        //If this event is null or doesn't have a PK, we shouldn't be updating it
        if (employeeLogEldEvent == null || !employeeLogEldEvent.isPrimaryKeySet())
            return;
        //Base update properties
        ContentValues content = new ContentValues();
        if (employeeLogEldEvent.getTractorNumber() != null)
            PutValue(content, TRACTORNUMBER, employeeLogEldEvent.getTractorNumber());
        if (employeeLogEldEvent.getTrailerNumber() != null)
            PutValue(content, TRAILERNUMBER, employeeLogEldEvent.getTrailerNumber());
        if (employeeLogEldEvent.getTrailerPlate() != null)
            PutValue(content, TRAILERPLATE, employeeLogEldEvent.getTrailerPlate());
        if (employeeLogEldEvent.getShipmentInfo() != null)
            PutValue(content, SHIPMENTINFO, employeeLogEldEvent.getShipmentInfo());

        if (employeeLogEldEvent.getLogKey() != null)
            PutValue(content, LOGKEY, employeeLogEldEvent.getLogKey());
        if (employeeLogEldEvent.getRuleSet() != null)
            PutValue(content, RULESET, employeeLogEldEvent.getRuleSet().getValue());

        //EOBR stuff
        if (employeeLogEldEvent.getEobrSerialNumber() != null && !employeeLogEldEvent.getEobrSerialNumber().equals("")) {
            PutValue(content, EOBRSERIALNUMBER, employeeLogEldEvent.getEobrSerialNumber());
        }

        //Geocode stuff
        PutValue(content, DRIVERSLOCATIONDESCRIPTION, employeeLogEldEvent.getDriversLocationDescription());
        PutValue(content, GEOLOCATION, employeeLogEldEvent.getGeolocation());
        PutValue(content, LATITUDE, employeeLogEldEvent.getLatitude());
        PutValue(content, LONGITUDE, employeeLogEldEvent.getLongitude());
        PutValue(content, GPSTIMESTAMP, employeeLogEldEvent.getGpsTimestamp());

        //Odometer stuff
        if (employeeLogEldEvent.getOdometer() != null && employeeLogEldEvent.getOdometer() != -1) {
            PutValue(content, ODOMETER, employeeLogEldEvent.getOdometer());
        }
        if (employeeLogEldEvent.getEndOdometer() != null && employeeLogEldEvent.getEndOdometer() != -1) {
            PutValue(content, ENDODOMETER, employeeLogEldEvent.getEndOdometer());
        }

        // has event been reviewed
        if (employeeLogEldEvent.getIsReviewed()) {
            PutValue(content, IS_REVIEWED, employeeLogEldEvent.getIsReviewed());
        }
        if (employeeLogEldEvent.getEventRecordStatus() != null) {
            PutValue(content, EVENTRECORDSTATUS, employeeLogEldEvent.getEventRecordStatus());
        }
        if (employeeLogEldEvent.getUnidentifiedEventStatus() != null) {
            PutValue(content, UNIDENTIFIEDEVENTSTATUS, employeeLogEldEvent.getUnidentifiedEventStatus().value);
        }

        if (employeeLogEldEvent.getEditDuration() != null)
            PutValue(content, EDITDURATION, employeeLogEldEvent.getEditDuration());


        PutValue(content, ENCOMPASSCLUSTERPK, GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled() ? employeeLogEldEvent.getEncompassClusterPK() : -1L);

        if (employeeLogEldEvent.getUnidentifiedEventConfidenceLevel() != null) {
            PutValue(content, UNIDENTIFIEDEVENTCONFIDENCELEVEL, employeeLogEldEvent.getUnidentifiedEventConfidenceLevel());
        }

        if (employeeLogEldEvent.getUnidentifiedEventSuggestedDriver() != null) {
            PutValue(content, UNIDENTIFIEDEVENTSUGGESTEDDRIVER, employeeLogEldEvent.getUnidentifiedEventSuggestedDriver());
        }

        String errorMsg = "Can't update EmployeeLogEldEvent record";

        ExecuteUpdate(employeeLogEldEvent, content, errorMsg);
    }

    public EmployeeLogEldEvent FetchMostRecentDriverIndicationEventByDriverID(String driverId) {
        return ExecuteFetchRawQuery(SQL_SELECT_MOSTRECENT_CHANGEINDRIVERSINDICATION_BYDRIVERID, new String[]{driverId});
    }

    public Date FetchMostRecentDateforIgnitionOffByDriverId(String driverId) {
        EmployeeLogEldEvent event = ExecuteFetchRawQuery(SQL_SELECT_MOSTRECENT_IGNITIONOFF_BYDRIVERID, new String[]{driverId});

        Date date = null;
        if (event != null) date = event.getEventDateTime();

        return date;
    }

    public Date FetchMostRecentDateforIgnitionOnByDriverId(String driverId) {
        EmployeeLogEldEvent event = ExecuteFetchRawQuery(SQL_SELECT_MOSTRECENT_IGNITIONON_BYDRIVERID, new String[]{driverId});

        Date date = null;
        if (event != null) date = event.getEventDateTime();

        return date;
    }

    public EmployeeLogEldEvent[] FetchPreviousWeekUnidentifiedEvents(String serialNumber) {
        List<T> empLogELDEventList = ExecuteFetchListRawQuery(SQL_SELECT_PREVIOUS_WEEK_UNIDENTIFIED_EVENTS, new String[]{serialNumber});
        return ExecuteFetchListRawQuery(SQL_SELECT_PREVIOUS_WEEK_UNIDENTIFIED_EVENTS, new String[]{serialNumber}).toArray(new EmployeeLogEldEvent[empLogELDEventList.size()]);
    }

    public List<UnclaimedEventDTO> FetchUnidentifiedDrivingEventsByELD(String serialNumber, Date LogRangeStartTime) {
        String[] selectionArgs = {
                serialNumber,
                DateUtility.getHomeTerminalSqlDateTimeFormat(this.getCurrentUser()).format(LogRangeStartTime)
        };

        List<T> unidentifiedEvents = ExecuteFetchListRawQuery(SQL_SELECT_UNIDENTIFIED_DRIVING_EVENTS_BY_ELD, selectionArgs);
        List<UnclaimedEventDTO> returnSet = new ArrayList<UnclaimedEventDTO>();
        for(EmployeeLogEldEvent e : unidentifiedEvents){
            returnSet.add(new UnclaimedEventDTO(e.getEventCode(), e.getEventDateTime()));
        }

        return returnSet;
    }

    public Boolean isUnidentifiedDrivingDiagnosticEventByELDActive(String serialNumber, Date LogRangeStartTime) {

        Boolean hasActiveEvent = false;

        String[] selectionArgs = {
                serialNumber,
                DateUtility.getHomeTerminalSqlDateTimeFormat(this.getCurrentUser()).format(LogRangeStartTime)
        };

        List<T> unidentifiedDrivingDiagnosticEvents = ExecuteFetchListRawQuery(SQL_SELECT_UNIDENTIFIED_DRIVING_DIAGNOSTIC_EVENTS_BY_ELD, selectionArgs);

        for (EmployeeLogEldEvent diagnosticEvent : unidentifiedDrivingDiagnosticEvents) {
            if (diagnosticEvent.getEventCode() == EmployeeLogEldEventCode.EldDataDiagnosticLogged) {
                hasActiveEvent = true;
                break;
            } else if (diagnosticEvent.getEventCode() == EmployeeLogEldEventCode.EldDataDiagnosticCleared) {
                hasActiveEvent = false;
                break;
            }
        }

        return hasActiveEvent;
    }

    public List<T> FetchUnsubmittedUnidentifiedEvents() {
        return ExecuteFetchListRawQuery(SQL_SELECT_UNSUBMITTED_UNIDENTIFIED_EVENTS, new String[0]);
    }

    public List<T> FetchUnsubmittedUnidentifiedEventsToSubmitLogs() {
        return ExecuteFetchListRawQuery(SQL_SELECT_UNSUBMITTED_UNIDENTIFIED_EVENTS_TOSUBMITLOGS, new String[0]);
    }

    public List<T> FetchUnreviewedUnidentifiedEvents() {
        return ExecuteFetchListRawQuery(SQL_SELECT_UNREVIEWED_UNIDENTIFIED_EVENTS, new String[0]);
    }

    public List<T> FetchUnreviewedWithoutConfidenceLevelEvents() {
        return ExecuteFetchListRawQuery(SQL_SELECT_UNREVIEWED_WITHOUTCONFIDENCELEVEL_UNIDENTIFIED_EVENTS, new String[0]);
    }

    public List<T> FetchSubmittedUnidentifiedEvents() {
        return ExecuteFetchListRawQuery(SQL_SELECT_SUBMITTED_UNIDENTIFIED_EVENTS, new String[0]);
    }

    public List<T> FetchUnsynchronizedUnidentifiedEvents() {
        return ExecuteFetchListRawQuery(SQL_SELECT_SUBMITTED_UNSYNCHRONIZED_UNIDENTIFIED_EVENTS, new String[0]);
    }

    public Boolean IsDataTransferMalfunctionEventActive() {
        Boolean response = false;
        List<T> dataTransferMalfunctionEvents = ExecuteFetchListRawQuery(SQL_SELECT_MALFUNCTION_EVENT_TYPE, new String[0]);
        for (EmployeeLogEldEvent dataTransferMalfunctionEvent : dataTransferMalfunctionEvents) {
            if (dataTransferMalfunctionEvent.getEventCode() == EmployeeLogEldEventCode.EldDataDiagnosticLogged) {
                response = true;
                break;
            } else if (dataTransferMalfunctionEvent.getEventCode() == EmployeeLogEldEventCode.EldDataDiagnosticCleared) {
                response = false;
                break;
            }
        }
        return response;
    }

    public Boolean IsMalfunctionDataTransferComplianceActive(){
        Boolean response = false;

        List<T> dataTransferComplianceEvents = ExecuteFetchListRawQuery(SQL_SELECT_MALFUNCTION_DATA_COMPLIANCE, new String[0]);
        for (EmployeeLogEldEvent dataTransferMalfunctionEvent : dataTransferComplianceEvents) {
            if (dataTransferMalfunctionEvent.getEventCode() == EmployeeLogEldEventCode.EldMalfunctionLogged) {
                response = true;
                break;
            } else if (dataTransferMalfunctionEvent.getEventCode() == EmployeeLogEldEventCode.EldMalfunctionCleared) {
                response = false;
                break;
            }
        }
        return response;
    }

    /// <summary>
    /// Purge any old records, based on the cutoff date, using the PURGE command
    /// A parm of @cutoffDate will be added to the command.
    /// </summary>
    /// <param name="cutoffDate"></param>
    public void PurgeUnidentifiedEvents(Date cutoffDate) {
        String sql;
        String[] selectionArgs;

        sql = SQL_SELECT_UNASSIGNED_EVENTS_TO_PURGE_COMMAND;
        selectionArgs = new String[]{
            DateUtility.getHomeTerminalSqlDateFormat(this.getCurrentUser()).format(cutoffDate),
            Integer.toString(UnidentifiedEldEventStatus.SUBMITTED.value),
            Integer.toString(UnidentifiedEldEventStatus.CLAIMED.value)
        };

        this.open();
        Cursor cursor = this.ExecuteRawQuery(sql, selectionArgs);

        if (cursor != null && cursor.moveToFirst()) {

            while (!cursor.isAfterLast()) {
                String key = cursor.getString(0);
                selectionArgs = new String[]{ key };

                sql = SQL_PURGE_ELD_EVENT;
                ExecuteQuery(sql, selectionArgs);

                cursor.moveToNext();
            }
        }

    }
}
