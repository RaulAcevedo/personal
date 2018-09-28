package com.jjkeller.kmbapi.proxydata;

import com.jjkeller.kmbapi.R;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.eldmandate.EventDataChecksumHelper;
import com.jjkeller.kmbapi.employeelogeldevents.EmployeeLogEldEventCode;
import com.jjkeller.kmbapi.employeelogeldevents.Enums;
import com.jjkeller.kmbapi.enums.DataDiagnosticEnum;
import com.jjkeller.kmbapi.enums.EmployeeLogEldEnum;
import com.jjkeller.kmbapi.enums.RuleSetTypeEnum;
import com.jjkeller.kmbapi.enums.UnidentifiedEldEventStatus;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class EmployeeLogEldEvent extends EldEventAdapter implements Cloneable {

    public static final int ORIGIN_DRIVER_EDITED = 2;
    public static final int ORIGIN_SERVER = 3;

    public static final int RECORD_STATUS_ACTIVE = 1;
    public static final int RECORD_STATUS_INACTIVE_CHANGED = 2;
    public static final int RECORD_STATUS_INACTIVE_CHANGE_REQUESTED = 3;

    ///////////////////////////////////////////////////////////////////////////////////////
    // private members
    ///////////////////////////////////////////////////////////////////////////////////////
    private String driverOriginatorUserId;
    private String encompassOriginatorUserId;
    private String unidentifiedUserId;
    private String eobrSerialNumber;
    private Integer logKey;
    private int eventSequenceIDNumber = EmployeeLogEldEnum.DEFAULT;
    private Integer eventRecordStatus;
    private Integer eventRecordOrigin;
    private Enums.EmployeeLogEldEventType eventType;
    private int eventCode = EmployeeLogEldEventCode.DutyStatus_NULL;
    private Date eventDateTime;
    private RuleSetTypeEnum ruleSet = new RuleSetTypeEnum(RuleSetTypeEnum.NULL);
    private Integer accumulatedVehicleMiles;
    private Float odometer;
    private Integer distance;
    private Double engineHours;
    private Double latitude;
    private Double longitude;
    private String latitudeStatusCode;
    private String longitudeStatusCode;
    private boolean isGPSAtReducedPrecision;
    private Float distanceSinceLastCoordinates;
    private boolean eldMalfunctionIndicatorStatus;
    private boolean driverDataDiagnosticEventIndicatorStatus;
    private boolean malfunctionDataTransferCompliance;
    private String eventComment;
    private String driversLocationDescription = "";
    private String eventDataCheck;
    private String diagnosticCode;
    private String geolocation;
    private String tractorNumber;
    private String vehiclePlate;
    private String trailerNumber;
    private String trailerPlate;
    private String shipmentInfo;
    private String logRemark;
    private boolean originalEvent;
    private boolean reassignEvent;
    private boolean isEventDateTimeValidated;
    private Float endOdometer;
    public Date logRemarkDateTime;
    private boolean isManuallyEditedByKMBUser;
    private Date gpsTimestamp;
    private Long editDuration;
    private String motionPictureAuthorityId;
    private String motionPictureProductionId;
    private long encompassClusterPK;
    private UnidentifiedEldEventStatus unidentifiedEventStatus = UnidentifiedEldEventStatus.NONE;
    private boolean isReviewed;
    private long relatedKmbPK;
    private long relatedEncompassClusterPK;
    private boolean isEventDataCheckDirty;
    private Integer unidentifiedEventConfidenceLevel;
    private String unidentifiedEventSuggestedDriver;

    ///////////////////////////////////////////////////////////////////////////////////////
    // public get/set methods
    ///////////////////////////////////////////////////////////////////////////////////////

    public String getDriverOriginatorUserId() {
        return driverOriginatorUserId;
    }

    public void setDriverOriginatorUserId(String driverOriginatorUserId) {
        this.driverOriginatorUserId = driverOriginatorUserId;
    }

    public String getEobrSerialNumber() {
        return eobrSerialNumber;
    }

    public void setEobrSerialNumber(String eobrSerialNumber) {
        this.eobrSerialNumber = eobrSerialNumber;
    }

    public String getEncompassOriginatorUserId() {
        return encompassOriginatorUserId;
    }

    public void setEncompassOriginatorUserId(String encompassOriginatorUserId) {
        this.encompassOriginatorUserId = encompassOriginatorUserId;
    }

    public String getUnidentifiedUserId() {
        return unidentifiedUserId;
    }

    public void setUnidentifiedUserId(String unidentifiedUserId) {
        this.unidentifiedUserId = unidentifiedUserId;
    }

    public String getMotionPictureProductionId() {
        return motionPictureProductionId;
    }

    public void setMotionPictureProductionId(String motionPictureProductionId) {
        this.motionPictureProductionId = motionPictureProductionId;
    }

    public String getMotionPictureAuthorityId() {
        return motionPictureAuthorityId;
    }

    public void setMotionPictureAuthorityId(String motionPictureAuthorityId) {
        this.motionPictureAuthorityId = motionPictureAuthorityId;
    }

    public Integer getLogKey() {
        return logKey;
    }

    public void setLogKey(Integer logKey) {
        this.logKey = logKey;
    }

    public Integer getEventRecordStatus() {
        return eventRecordStatus;
    }

    public void setEventRecordStatus(Integer eventRecordStatus) {
        this.eventRecordStatus = eventRecordStatus;
    }

    public Integer getEventRecordOrigin() {
        return eventRecordOrigin;
    }

    public void setEventRecordOrigin(Integer eventRecordOrigin) {
        this.eventRecordOrigin = eventRecordOrigin;
    }

    public Enums.EmployeeLogEldEventType getEventType() {
        return eventType;
    }

    public void setEventType(Enums.EmployeeLogEldEventType eventType)
    {
        this.eventType = eventType;
    }

    public int getEventCode() {
        return eventCode;
    }

    public EmployeeLogEldEventCode getEventCodeEnum() {
        return new EmployeeLogEldEventCode(eventCode);
    }

    public void setEventCode(int eventCode) {
        this.eventCode = eventCode;
    }

    public Date getEventDateTime() {
        return eventDateTime;
    }

    public int getEventSequenceIDNumber() {
        return eventSequenceIDNumber;
    }

    public void setEventSequenceIDNumber(int eventSequenceIDNumber) {
        this.eventSequenceIDNumber = eventSequenceIDNumber;
    }

    public void setEventDateTime(Date eventDateTime) {
        this.eventDateTime = eventDateTime;
    }

    public RuleSetTypeEnum getRuleSet() {
        return ruleSet;
    }

    public void setRuleSet(RuleSetTypeEnum ruleSet) {
        this.ruleSet = ruleSet;
    }

    public Integer getAccumulatedVehicleMiles() {
        return accumulatedVehicleMiles;
    }

    public void setAccumulatedVehicleMiles(Integer accumulatedVehicleMiles) {
        setAccumulatedVehicleMiles(accumulatedVehicleMiles, false);
    }

    public void setAccumulatedVehicleMiles(Integer accumulatedVehicleMiles, boolean skipDirtyCheck) {
        if (!skipDirtyCheck) {
            if (this.accumulatedVehicleMiles != accumulatedVehicleMiles) isEventDataCheckDirty = true;
        }

        this.accumulatedVehicleMiles = accumulatedVehicleMiles;
    }

    public Float getOdometer() {
        return odometer;
    }

    public void setOdometer(Float odometer) {
            this.odometer = odometer;
    }

    public Integer getDistance() {
        return distance;
    }

    public void setDistance(Integer distance) {
        this.distance = distance;
    }

    public Double getEngineHours() {
        return engineHours;
    }

    public void setEngineHours(Double engineHours) {
        setEngineHours(engineHours, false);
    }

    public void setEngineHours(Double engineHours, boolean skipDirtyCheck) {
        if (!skipDirtyCheck) {
            if (this.engineHours != engineHours) isEventDataCheckDirty = true;
        }

        this.engineHours = engineHours;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        setLatitude(latitude, false);
    }

    public void setLatitude(Double latitude, boolean skipDirtyCheck) {
        if (!skipDirtyCheck) {
            if (this.latitude != latitude) isEventDataCheckDirty = true;
        }

        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        setLongitude(longitude, false);
    }

    public void setLongitude(Double longitude, boolean skipDirtyCheck) {
        if (!skipDirtyCheck) {
            if (this.longitude != longitude) isEventDataCheckDirty = true;
        }

        this.longitude = longitude;
    }

    public String getLatitudeStatusCode() {
        return latitudeStatusCode;
    }

    public void setLatitudeStatusCode(String latitudeStatusCode) {
        this.latitudeStatusCode = latitudeStatusCode;
    }

    public String getLongitudeStatusCode() {
        return longitudeStatusCode;
    }

    public void setLongitudeStatusCode(String longitudeStatusCode) {
        this.longitudeStatusCode = longitudeStatusCode;
    }

    public boolean getIsGPSAtReducedPrecision() {
        return isGPSAtReducedPrecision;
    }

    public void setIsGPSAtReducedPrecision(boolean isGPSAtReducedPrecision) {
        this.isGPSAtReducedPrecision = isGPSAtReducedPrecision;
    }

    public Float getDistanceSinceLastCoordinates() {
        return distanceSinceLastCoordinates;
    }

    public void setDistanceSinceLastCoordinates(Float distanceSinceLastCoordinates) {
        this.distanceSinceLastCoordinates = distanceSinceLastCoordinates;
    }

    public boolean getEldMalfunctionIndicatorStatus() {
        return eldMalfunctionIndicatorStatus;
    }

    public void setEldMalfunctionIndicatorStatus(boolean eldMalfunctionIndicatorStatus) {
        this.eldMalfunctionIndicatorStatus = eldMalfunctionIndicatorStatus;
    }

    public boolean getDriverDataDiagnosticEventIndicatorStatus() {
        return driverDataDiagnosticEventIndicatorStatus;
    }

    public void setDriverDataDiagnosticEventIndicatorStatus(boolean driverDataDiagnosticEventIndicatorStatus) {
        this.driverDataDiagnosticEventIndicatorStatus = driverDataDiagnosticEventIndicatorStatus;
    }

    public boolean getMalfunctionDataTransferComplianceStatus() {
        return malfunctionDataTransferCompliance;
    }

    public void setMalfunctionDataTransferComplianceStatus(boolean malfunctionDataTransferCompliance) {
        this.malfunctionDataTransferCompliance = malfunctionDataTransferCompliance;
    }

    public String getEventComment() {
        return eventComment;
    }

    /**
     * Also used for the annotation field
     *
     * @param eventComment Comment that will be associated with an event
     */
    public void setEventComment(String eventComment) {
        this.eventComment = eventComment;
    }

    public String getDriversLocationDescription() {
        return driversLocationDescription;
    }

    public boolean isDataDiagnosticEvent(){
        return eventType == Enums.EmployeeLogEldEventType.Malfunction_DataDiagnosticDetection
                && (eventCode == EmployeeLogEldEventCode.EldDataDiagnosticCleared || eventCode == EmployeeLogEldEventCode.EldDataDiagnosticLogged);
    }

    public void setDriversLocationDescription(String driversLocationDescription) {
        this.driversLocationDescription = driversLocationDescription;
    }

    public String getEventDataCheck() {
        if (eventDataCheck == null || eventDataCheck.length() < 1)
            return EventDataChecksumHelper.EventDataChecksum(this, GlobalState.getInstance().getCurrentUser().getCredentials().getUsername());

        if (!isEventDataCheckDirty)
            return eventDataCheck;

        String tempEventDataCheck = EventDataChecksumHelper.EventDataChecksum(this, GlobalState.getInstance().getCurrentUser().getCredentials().getUsername());
        if (tempEventDataCheck != eventDataCheck)
            return tempEventDataCheck;

        isEventDataCheckDirty = false;
        return eventDataCheck;
    }

    public void setEventDataCheck(String eventDataCheck) {
        this.eventDataCheck = eventDataCheck;
    }

    public String getDiagnosticCode() {
        return diagnosticCode;
    }

    public void setDiagnosticCode(String diagnosticCode) {
        this.diagnosticCode = diagnosticCode;
    }

    public String getGeolocation() {
        return geolocation;
    }

    public void setGeolocation(String geolocation) {
        this.geolocation = geolocation;
    }

    public String getTractorNumber() {
        return tractorNumber;
    }

    public void setTractorNumber(String tractorNumber) {
        setTractorNumber(tractorNumber, false);
    }

    public void setTractorNumber(String tractorNumber, boolean skipDirtyCheck) {
        if (!skipDirtyCheck) {
            if (this.tractorNumber != tractorNumber) isEventDataCheckDirty = true;
        }

        this.tractorNumber = tractorNumber;
    }

    public String getVehiclePlate() {
        return vehiclePlate;
    }

    public void setVehiclePlate(String vehiclePlate) {
        this.vehiclePlate = vehiclePlate;
    }

    public String getTrailerNumber() {
        return trailerNumber;
    }

    public void setTrailerNumber(String trailerNumber) {
        this.trailerNumber = trailerNumber;
    }

    public String getTrailerPlate() {
        return trailerPlate;
    }

    public void setTrailerPlate(String trailerPlate) {
        this.trailerPlate = trailerPlate;
    }

    public String getShipmentInfo() {
        return shipmentInfo;
    }

    public void setShipmentInfo(String shipmentInfo) {
        this.shipmentInfo = shipmentInfo;
    }

    public String getLogRemark() {
        return logRemark;
    }

    public void setLogRemark(String logRemark) {
        this.logRemark = logRemark;
    }

    public boolean getOriginalEvent() {
        return originalEvent;
    }

    public void setOriginalEvent(boolean originalEvent) {
        this.originalEvent = originalEvent;
    }

    public boolean getReassignEvent() {
        return reassignEvent;
    }

    public void setReassignEvent(boolean reassignEvent) {
        this.reassignEvent = reassignEvent;
    }

    public boolean getIsEventDateTimeValidated() {
        return isEventDateTimeValidated;
    }

    public void setIsEventDateTimeValidated(boolean eventDateTimeValidated) {
        isEventDateTimeValidated = eventDateTimeValidated;
    }

    public Float getEndOdometer() {
        return endOdometer;
    }

    public void setEndOdometer(Float endOdometer) {
        this.endOdometer = endOdometer;
    }

    public Date getLogRemarkDateTime() {
        return logRemarkDateTime;
    }

    public void setLogRemarkDateTime(Date logRemarkDateTime) {
        this.logRemarkDateTime = logRemarkDateTime;
    }

    public boolean getIsManuallyEditedByKMBUser() {
        return isManuallyEditedByKMBUser;
    }

    public void setIsManuallyEditedByKMBUser(boolean manuallyEditedByKMBUser) {
        isManuallyEditedByKMBUser = manuallyEditedByKMBUser;
    }

    public Date getGpsTimestamp() {
        return gpsTimestamp;
    }

    public void setGpsTimestamp(Date gpsTimestamp) {
        this.gpsTimestamp = gpsTimestamp;
    }

    public boolean getIsReviewed() {
        return isReviewed;
    }

    public void setIsReviewed(boolean reviewed) {
        isReviewed = reviewed;
    }

    public long getRelatedKmbPK() {
        return relatedKmbPK;
    }

    public void setRelatedKmbPK(long relatedKmbPK) {
        this.relatedKmbPK = relatedKmbPK;
    }

    public long getRelatedEncompassClusterPk() {
        return relatedEncompassClusterPK;
    }

    public void setRelatedEncompassClusterPK(long relatedEncompassClusterPK) {
        this.relatedEncompassClusterPK = relatedEncompassClusterPK;
    }

    /**
     * milliseconds of duration
     *
     * @return milliseconds
     */
    public Long getEditDuration() {
        return editDuration;
    }

    public Long getEditDuration(int calendarTimeUnit) {
        if (this.editDuration == null) return null;

        switch (calendarTimeUnit) {
            case Calendar.HOUR:
                return (long) DateUtility.ConvertMillisecondsToHours(editDuration);
            case Calendar.MINUTE:
                return (long) DateUtility.ConvertMillisecondsToMinutes(editDuration);
            case Calendar.SECOND:
                return (long) DateUtility.ConvertMillisecondsToSeconds(editDuration);
            case Calendar.MILLISECOND:
                return editDuration;
            default:
                throw new IllegalArgumentException("calendarTimeUnit");
        }
    }

    /**
     * milliseconds of duration
     *
     * @param editDuration milliseconds
     */
    public void setEditDuration(Long editDuration) {
        this.editDuration = editDuration;
    }

    public void setEditDuration(Long editDuration, int calendarTimeUnit) {
        if (editDuration == null) {
            this.editDuration = null;
            return;
        }

        switch (calendarTimeUnit) {
            case Calendar.HOUR:
                this.editDuration = DateUtility.MILLISECONDS_PER_HOUR * editDuration;
                break;
            case Calendar.MINUTE:
                this.editDuration = DateUtility.MILLISECONDS_PER_MINUTE * editDuration;
                break;
            case Calendar.SECOND:
                int millisecondsPerSecond = 1000;
                this.editDuration = millisecondsPerSecond * editDuration;
                break;
            case Calendar.MILLISECOND:
                this.editDuration = editDuration;
                break;
            default:
                throw new IllegalArgumentException("calendarTimeUnit");
        }
    }

    public long getEncompassClusterPK() {
        return encompassClusterPK;
    }

    public void setEncompassClusterPK(long encompassClusterPK) {
        this.encompassClusterPK = encompassClusterPK;
    }

    public UnidentifiedEldEventStatus getUnidentifiedEventStatus() {
        return unidentifiedEventStatus;
    }

    public void setUnidentifiedEventStatus(UnidentifiedEldEventStatus unidentifiedEventStatus) {
        this.unidentifiedEventStatus = unidentifiedEventStatus == null ? UnidentifiedEldEventStatus.NONE : unidentifiedEventStatus;
    }

    public Integer getUnidentifiedEventConfidenceLevel() {
        return unidentifiedEventConfidenceLevel;
    }

    public void setUnidentifiedEventConfidenceLevel(Integer unidentifiedEventConfidenceLevel) {
        this.unidentifiedEventConfidenceLevel = unidentifiedEventConfidenceLevel;
    }

    public String getUnidentifiedEventSuggestedDriver() {
        return unidentifiedEventSuggestedDriver;
    }

    public void setUnidentifiedEventSuggestedDriver(String unidentifiedEventSuggestedDriver) {
        this.unidentifiedEventSuggestedDriver = unidentifiedEventSuggestedDriver;
    }

    public EmployeeLogEldEvent() {
        this(null, null);
    }

    public EmployeeLogEldEvent(Date startDateTime) {
        this(startDateTime, null, null);
    }

    public EmployeeLogEldEvent(EmployeeLogEldEventCode initialEventCode, Enums.EmployeeLogEldEventType type) {
        this(null, initialEventCode, type);
    }

    public EmployeeLogEldEvent(Date startDateTime, EmployeeLogEldEventCode initialEventCode, Enums.EmployeeLogEldEventType type) {
        super();
        this.setInternalEventPointerReference(this);
        if (startDateTime != null) {
            this.setStartTime(startDateTime);
        }
        if (initialEventCode != null && type != null && type == Enums.EmployeeLogEldEventType.DutyStatusChange) {
            this.setDutyStatusEnum(translateMandateStatusToDutyStatus(type, initialEventCode.getValue()));
        } else {
            if (type != null)
                this.setEventType(type);

            if (initialEventCode != null)
                this.setEventCode(initialEventCode.getValue());
        }

        this.hydrate(this.mandateState);
    }

    /**
     * Generate an exact copy of the object
     */
    public Object clone() throws CloneNotSupportedException {
        return (EmployeeLogEldEvent) super.clone();
    }

    public String getCompositeEventCodeType(Enums.EmployeeLogEldEventType eventType, int eventCode) {
        String retVal = "";
        if (eventType == Enums.EmployeeLogEldEventType.DutyStatusChange) {
            switch (eventCode) {
                case 1:
                    retVal = GlobalState.getInstance().getBaseContext().getString(R.string.type1code1);
                    break;
                case 2:
                    retVal = GlobalState.getInstance().getBaseContext().getString(R.string.type1code2);
                    break;
                case 3:
                    retVal = GlobalState.getInstance().getBaseContext().getString(R.string.type1code3);
                    break;
                case 4:
                    retVal = GlobalState.getInstance().getBaseContext().getString(R.string.type1code4);
                    break;
                case 5:
                    retVal = GlobalState.getInstance().getBaseContext().getString(R.string.type1code5);
                    break;
            }
        } else if (eventType == Enums.EmployeeLogEldEventType.IntermediateLog) {
            switch (eventCode) {
                case 1:
                    retVal = GlobalState.getInstance().getBaseContext().getString(R.string.type2code1);
                    break;
                case 2:
                    retVal = GlobalState.getInstance().getBaseContext().getString(R.string.type2code2);
                    break;
            }
        } else if (eventType == Enums.EmployeeLogEldEventType.ChangeInDriversIndication) {
            switch (eventCode) {
                case 1:
                    retVal = GlobalState.getInstance().getBaseContext().getString(R.string.type3code1);
                    break;
                case 2:
                    retVal = GlobalState.getInstance().getBaseContext().getString(R.string.type3code2);
                    break;
                case 0:
                    retVal = GlobalState.getInstance().getBaseContext().getString(R.string.type3code0);
                    break;
            }
        } else if (eventType == Enums.EmployeeLogEldEventType.Certification) {
            switch (eventCode) {
                case 1:
                    retVal = GlobalState.getInstance().getBaseContext().getString(R.string.type4code1);
                    break;
                case 2:
                    retVal = GlobalState.getInstance().getBaseContext().getString(R.string.type4code2);
                    break;
            }
        } else if (eventType == Enums.EmployeeLogEldEventType.LoginLogout) {
            switch (eventCode) {
                case 1:
                    retVal = GlobalState.getInstance().getBaseContext().getString(R.string.type5code1);
                    break;
                case 2:
                    retVal = GlobalState.getInstance().getBaseContext().getString(R.string.type5code2);
                    break;
            }
        } else if (eventType == Enums.EmployeeLogEldEventType.EnginePowerUpPowerDown) {
            switch (eventCode) {
                case 1:
                    retVal = GlobalState.getInstance().getBaseContext().getString(R.string.type6code1);
                    break;
                case 2:
                    retVal = GlobalState.getInstance().getBaseContext().getString(R.string.type6code2);
                    break;
                case 3:
                    retVal = GlobalState.getInstance().getBaseContext().getString(R.string.type6code3);
                    break;
                case 4:
                    retVal = GlobalState.getInstance().getBaseContext().getString(R.string.type6code4);
                    break;
            }
        } else if (eventType == Enums.EmployeeLogEldEventType.Malfunction_DataDiagnosticDetection) {
            switch (eventCode) {
                case 1:
                    retVal = GlobalState.getInstance().getBaseContext().getString(R.string.type7code1);
                    break;
                case 2:
                    retVal = GlobalState.getInstance().getBaseContext().getString(R.string.type7code2);
                    break;
                case 3:
                    retVal = GlobalState.getInstance().getBaseContext().getString(R.string.type7code3);
                    break;
                case 4:
                    retVal = GlobalState.getInstance().getBaseContext().getString(R.string.type7code4);
                    break;
            }
        }
        return retVal;
    }

    public String getFormattedDateTime(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd h:mm:ss a", Locale.US);
        return dateFormat.format(date);
    }


    @Override
    public String toString() {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        SimpleDateFormat dateFormat = DateUtility.getHomeTerminalDMOSoapDateTimestampUTCFormat();
        dateFormat.setTimeZone(tz);

        return "EmployeeLogEldEvent{" +
                "key=" + getPrimaryKey() +
                ", driverOriginatorUserId='" + driverOriginatorUserId + '\'' +
                ", encompassOriginatorUserId='" + encompassOriginatorUserId + '\'' +
                ", unidentifiedUserId='" + unidentifiedUserId + '\'' +
                ", eobrSerialNumber='" + eobrSerialNumber + '\'' +
                ", logKey=" + logKey +
                ", eventSequenceIDNumber=" + eventSequenceIDNumber +
                ", eventRecordStatus=" + eventRecordStatus +
                ", eventRecordOrigin=" + eventRecordOrigin +
                ", eventType=" + eventType +
                ", eventCode=" + eventCode +
                ", eventDateTime=" + dateFormat.format(eventDateTime) +
                ", ruleSet=" + ruleSet +
                ", accumulatedVehicleMiles=" + accumulatedVehicleMiles +
                ", odometer=" + odometer +
                ", distance=" + distance +
                ", engineHours=" + engineHours +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", latitudeStatusCode='" + latitudeStatusCode + '\'' +
                ", longitudeStatusCode='" + longitudeStatusCode + '\'' +
                ", isGpsAtReducedPrecision=" + isGPSAtReducedPrecision +
                ", distanceSinceLastCoordinates=" + distanceSinceLastCoordinates +
                ", eldMalfunctionIndicatorStatus=" + eldMalfunctionIndicatorStatus +
                ", driverDataDiagnosticEventIndicatorStatus=" + driverDataDiagnosticEventIndicatorStatus +
                ", eventComment='" + eventComment + '\'' +
                ", driversLocationDescription='" + driversLocationDescription + '\'' +
                ", eventDataCheck='" + eventDataCheck + '\'' +
                ", diagnosticCode='" + diagnosticCode + '\'' +
                ", geolocation='" + geolocation + '\'' +
                ", tractorNumber='" + tractorNumber + '\'' +
                ", vehiclePlate='" + vehiclePlate + '\'' +
                ", trailerNumber='" + trailerNumber + '\'' +
                ", trailerPlate='" + trailerPlate + '\'' +
                ", shipmentInfo='" + shipmentInfo + '\'' +
                ", logRemark='" + logRemark + '\'' +
                ", originalEvent=" + originalEvent +
                ", reassignEvent=" + reassignEvent +
                ", isEventDateTimeValidated=" + isEventDateTimeValidated +
                ", endOdometer=" + endOdometer +
                ", logRemarkDateTime=" + logRemarkDateTime +
                ", isManuallyEditedByKMBUser=" + isManuallyEditedByKMBUser +
                ", gpsTimestamp=" + gpsTimestamp +
                ", editDuration=" + editDuration +
                ", motionPictureAuthorityId='" + motionPictureAuthorityId + '\'' +
                ", motionPictureProductionId='" + motionPictureProductionId + '\'' +
                ", encompassClusterPK=" + encompassClusterPK +
                ", isUnidentifiedEvent=" + unidentifiedEventStatus.value +
                ", unidentifiedEventConfidenceLevel=" + unidentifiedEventConfidenceLevel +
                ", unidentifiedEventSuggestedDriver=" + unidentifiedEventSuggestedDriver +
                '}';
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // Overrides
    ///////////////////////////////////////////////////////////////////////////////////////
    public String toLogString() {
        String output = String.format("DriverOriginatorUserId: {%s} " +
                        "EncompassOriginatorUserId: {%s} " +
                        "UnidentifiedUserId: {%s} " +
                        "EobrSerialNumber: {%s} " +
                        "LogKey: {%d}" +
                        "EventSequenceIDNumber: {%d} " +
                        "EventRecordStatus: {%d} " +
                        "EventRecordOrigin: {%d} " +
                        "EventType: {%d} " +
                        "EventCode: {%d} " +
                        "EventDateTime: {%s} " +
                        "RuleSet: {%d} " +
                        "AccumulatedVehicleMiles: {%d} " +
                        "Odometer: {%f} " +
                        "Distance: {%d} " +
                        "EngineHours: {%f} " +
                        "Latitude: {%f} " +
                        "Longitude: {%f} " +
                        "LatitudeStatusCode: {%s} " +
                        "LongitudeStatusCode: {%s} " +
                        "IsGPSAtReducedPrecision: {%b} " +
                        "DistanceSinceLastCordinates: {%f} " +
                        "EldMalfunctionIndicatorStatus: {%b} " +
                        "DriverDataDiagnosticEventIndicatorStatus: {%b} " +
                        "EventComment: {%s} " +
                        "DriversLocationDescription: {%s} " +
                        "EventDataCheck: {%s} " +
                        "DiagnosticCode: {%s} " +
                        "Geolocation: {%s} " +
                        "TractorNumber: {%s} " +
                        "VehiclePlate: {%s} " +
                        "TrailerNumber: {%s} " +
                        "TrailerPlate: {%s} " +
                        "ShipmentInfo: {%s} " +
                        "LogRemark: {%s} " +
                        "OriginalEvent: {%s} " +
                        "ReassignEvent: {%b} " +
                        "isEventDateTimeValidated: {%b} " +
                        "EndOdometer: {%f} " +
                        "LogRemarkDateTime: {%s} " +
                        "isManuallyEditedByKMBUser: {%b} " +
                        "editDuration: {%d} " +
                        "motionPictureAuthorityId: {%s} " +
                        "motionPictureProductionId: {%s} " +
                        "encompassClusterPK: {%d} " +
                        "isUnidentifiedEvent: {%b} " +
                        "unidentifiedEventConfidenceLevel: {%s} " +
                        "unidentifiedEventSuggestedDriver: {%s}",
                this.getDriverOriginatorUserId(),
                this.getEncompassOriginatorUserId(),
                this.getUnidentifiedUserId(),
                this.getEobrSerialNumber(),
                this.getLogKey(),
                this.getEventSequenceIDNumber(),
                this.getEventRecordStatus(),
                this.getEventRecordOrigin(),
                this.getEventType().getValue(),
                this.getEventCode(),
                this.getEventDateTime() == null ? "" : this.getEventDateTime().toString(),
                this.getRuleSet().getValue(),
                this.getAccumulatedVehicleMiles(),
                this.getOdometer(),
                this.getDistance(),
                this.getEngineHours(),
                this.getLatitude(),
                this.getLongitude(),
                this.getLatitudeStatusCode(),
                this.getLongitudeStatusCode(),
                this.getIsGPSAtReducedPrecision(),
                this.getDistanceSinceLastCoordinates(),
                this.getEldMalfunctionIndicatorStatus(),
                this.getDriverDataDiagnosticEventIndicatorStatus(),
                this.getEventComment() == null ? "" : this.getEventComment(),
                this.getDriversLocationDescription() == null ? "" : this.getDriversLocationDescription(),
                this.getEventDataCheck(),
                this.getDiagnosticCode(),
                this.getGeolocation(),
                this.getTractorNumber(),
                this.getVehiclePlate(),
                this.getTrailerNumber(),
                this.getTrailerPlate(),
                this.getShipmentInfo(),
                this.getLogRemark(),
                this.getOriginalEvent(),
                this.getReassignEvent(),
                this.getIsEventDateTimeValidated(),
                this.getEndOdometer(),
                this.getLogRemarkDateTime() == null ? "" : this.getLogRemarkDateTime().toString(),
                this.getIsManuallyEditedByKMBUser(),
                this.getEditDuration(),
                this.getMotionPictureAuthorityId(),
                this.getMotionPictureProductionId(),
                this.getEncompassClusterPK(),
                this.getUnidentifiedEventStatus(),
                this.getUnidentifiedEventConfidenceLevel() == null ? "" : this.getUnidentifiedEventConfidenceLevel().toString(),
                this.getUnidentifiedEventSuggestedDriver());
        return output;
    }

    public boolean isDrivingEvent() {
        if (this.getEventType() == Enums.EmployeeLogEldEventType.DutyStatusChange && this.getEventCode() == EmployeeLogEldEventCode.DutyStatus_Driving) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isAutomaticDrivingEvent() {
        if (isDrivingEvent() && getEventRecordOrigin() == com.jjkeller.kmbapi.employeelogeldevents.Enums.EmployeeLogEldEventRecordOrigin.AutomaticallyRecorded)
            return true;
        return false;
    }

    public boolean isManualDrivingEvent() {
        if (isDrivingEvent() && getEventRecordOrigin() != com.jjkeller.kmbapi.employeelogeldevents.Enums.EmployeeLogEldEventRecordOrigin.AutomaticallyRecorded)
            return true;
        return false;
    }

    public boolean isOnDutyNotDrivingEvent() {
        if (this.getEventType() == Enums.EmployeeLogEldEventType.DutyStatusChange && this.getEventCode() == EmployeeLogEldEventCode.DutyStatus_OnDuty) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isOffDutyEvent() {
        if (this.getEventType() == Enums.EmployeeLogEldEventType.DutyStatusChange && (this.getEventCode() == EmployeeLogEldEventCode.DutyStatus_OffDuty || this.getEventCode() == EmployeeLogEldEventCode.DutyStatus_OffDutyWellSite)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isUnidentifiedEvent() {
        return unidentifiedEventStatus == UnidentifiedEldEventStatus.LOCAL || unidentifiedEventStatus == UnidentifiedEldEventStatus.SUBMITTED;
    }
}
