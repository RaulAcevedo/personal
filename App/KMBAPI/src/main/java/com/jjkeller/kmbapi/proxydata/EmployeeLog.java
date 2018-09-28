package com.jjkeller.kmbapi.proxydata;


import android.util.Log;

import com.jjkeller.kmbapi.enums.CanadaDeferralTypeEnum;
import com.jjkeller.kmbapi.enums.DriverTypeEnum;
import com.jjkeller.kmbapi.enums.DutyStatusEnum;
import com.jjkeller.kmbapi.enums.ExemptLogTypeEnum;
import com.jjkeller.kmbapi.enums.RuleSetTypeEnum;
import com.jjkeller.kmbapi.enums.TimeZoneEnum;

import java.util.Date;

/**
 * Model to represent an Employee Log.
 * <p>
 * Please ensure this file syncs with EmployeeLogData in DM.Common.Data on the DMO Web Server
 */
public class EmployeeLog extends ProxyBase {

    /**
     * Enum representing a trip information property present on either an employee log or employee
     * log eld event
     */
    public enum TripInfoPropertyKey {
        TractorNumber,
        TrailerNumber,
        TrailerPlate,
        ShipmentInfo,
        VehiclePlate
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // private members
    ///////////////////////////////////////////////////////////////////////////////////////
    private String employeeId;
    private Date logDate;
    private float totalLogDistance;
    private boolean hasReturnedToLocation;
    private DriverTypeEnum driverType = new DriverTypeEnum(DriverTypeEnum.PROPERTYCARRYING);
    private RuleSetTypeEnum ruleset = new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR);
    private EmployeeLogEldEventList eldEventList = new EmployeeLogEldEventList();
    private TimeZoneEnum timezone = new TimeZoneEnum(TimeZoneEnum.EASTERNSTANDARDTIME);
    private TeamDriverList teamDriverList = new TeamDriverList();
    private Date mobileStartTimestamp;
    private Date mobileEndTimestamp;
    private float mobileRecordedDistance;
    private String mobileEobrIdentifier;
    private String tractorNumbers;
    private String trailerNumbers;
    private String trailerPlate;
    private String shipmentInformation;
    private String vehiclePlate;
    private FailureReportList timeSyncFailureList = new FailureReportList();
    private FailureReportList eobrFailureList = new FailureReportList();
    private boolean isShortHaulExceptionUsed;
    private Date weeklyResetStartTimestamp;
    private CanadaDeferralTypeEnum canadaDeferralType = new CanadaDeferralTypeEnum(CanadaDeferralTypeEnum.NONE);
    private boolean isHaulingExplosives;
    private boolean isExemptFrom30MinBreakRequirement;
    private boolean isWeeklyResetUsed;
    private boolean isWeeklyResetUsedOverridden;
    private boolean isOperatesSpecificVehiclesForOilfield;
    private boolean isNonCDLShortHaulExceptionUsed;
    private ExemptLogTypeEnum exemptLogType = new ExemptLogTypeEnum(ExemptLogTypeEnum.NULL);
    private boolean isCertified;
    private boolean isTransitional;
    private boolean isExemptFromELDUse;
    private int logType = -1;

    //////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////
    // public get/set methods
    ///////////////////////////////////////////////////////////////////////////////////////
    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public Date getLogDate() {
        return logDate;
    }

    public void setLogDate(Date logDate) {
        this.logDate = logDate;
    }

    public float getTotalLogDistance() {
        return totalLogDistance;
    }

    public void setTotalLogDistance(float totalLogDistance) {
        this.totalLogDistance = totalLogDistance;
    }

    public boolean getHasReturnedToLocation() {
        return hasReturnedToLocation;
    }

    public void setHasReturnedToLocation(boolean hasReturnedToLocation) {
        this.hasReturnedToLocation = hasReturnedToLocation;
    }

    public DriverTypeEnum getDriverType() {
        return driverType;
    }

    public void setDriverType(DriverTypeEnum driverType) {
        this.driverType = driverType;
    }

    public RuleSetTypeEnum getRuleset() {
        return ruleset;
    }

    public void setRuleset(RuleSetTypeEnum ruleset) {
        this.ruleset = ruleset;
    }

    public TeamDriverList getTeamDriverList() {
        return teamDriverList;
    }

    public void setTeamDriverList(TeamDriverList teamDriverList) {
        this.teamDriverList = teamDriverList;
    }

    public TimeZoneEnum getTimezone() {
        return timezone;
    }

    public void setTimezone(TimeZoneEnum timezone) {
        this.timezone = timezone;
    }


    public void setEldEventList(EmployeeLogEldEventList eldEvents) {
        this.eldEventList = eldEvents;
    }

    public EmployeeLogEldEventList getEldEventList() {
        return eldEventList;
    }


    public Date getMobileStartTimestamp() {
        return mobileStartTimestamp;
    }

    public void setMobileStartTimestamp(Date mobileStartTimestamp) {
        this.mobileStartTimestamp = mobileStartTimestamp;
    }

    public Date getMobileEndTimestamp() {
        return mobileEndTimestamp;
    }

    public void setMobileEndTimestamp(Date mobileEndTimestamp) {
        this.mobileEndTimestamp = mobileEndTimestamp;
    }

    public float getMobileRecordedDistance() {
        return mobileRecordedDistance;
    }

    public void setMobileRecordedDistance(float mobileRecordedDistance) {
        this.mobileRecordedDistance = mobileRecordedDistance;
    }

    public String getMobileEobrIdentifier() {
        return mobileEobrIdentifier;
    }

    public void setMobileEobrIdentifier(String mobileEobrIdentifier) {
        this.mobileEobrIdentifier = mobileEobrIdentifier;
    }

    public String getTractorNumbers() {
        return tractorNumbers;
    }

    public void setTractorNumbers(String tractorNumbers) {
        this.tractorNumbers = tractorNumbers;
    }

    public String getTrailerNumbers() {
        return trailerNumbers;
    }

    public void setTrailerNumbers(String trailerNumbers) {
        this.trailerNumbers = trailerNumbers;
    }

    public String getTrailerPlate() {
        return trailerPlate;
    }

    public void setTrailerPlate(String trailerPlate) {
        this.trailerPlate = trailerPlate;
    }

    public String getShipmentInformation() {
        return shipmentInformation;
    }

    public void setShipmentInformation(String shipmentInformation) {
        this.shipmentInformation = shipmentInformation;
    }

    public String getVehiclePlate() {
        return vehiclePlate;
    }

    public void setVehiclePlate(String vehiclePlate) {
        this.vehiclePlate = vehiclePlate;
    }

    public FailureReportList getEobrFailureList() {
        return eobrFailureList;
    }

    public void setEobrFailureList(FailureReportList eobrFailureList) {
        this.eobrFailureList = eobrFailureList;
    }

    public FailureReportList getTimeSyncFailureList() {
        return timeSyncFailureList;
    }

    public void setTimeSyncFailureList(FailureReportList timeSyncFailureList) {
        this.timeSyncFailureList = timeSyncFailureList;
    }

    public boolean getIsShortHaulExceptionUsed() {
        return isShortHaulExceptionUsed;
    }

    public void setIsShortHaulExceptionUsed(boolean isShortHaulExceptionUsed) {
        this.isShortHaulExceptionUsed = isShortHaulExceptionUsed;
    }

    public CanadaDeferralTypeEnum getCanadaDeferralType() {
        return canadaDeferralType;
    }

    public void setCanadaDeferralType(CanadaDeferralTypeEnum canadaDeferralType) {
        this.canadaDeferralType = canadaDeferralType;
    }

    public Date getWeeklyResetStartTimestamp() {
        return weeklyResetStartTimestamp;
    }

    public void setWeeklyResetStartTimestamp(Date timestamp) {
        this.weeklyResetStartTimestamp = timestamp;
    }

    public boolean getIsHaulingExplosives() {
        return isHaulingExplosives;
    }

    public void setIsHaulingExplosives(boolean value) {
        this.isHaulingExplosives = value;
    }

    public boolean getIsExemptFrom30MinBreakRequirement() {
        return isExemptFrom30MinBreakRequirement;
    }

    public void setIsExemptFrom30MinBreakRequirement(boolean value) {
        this.isExemptFrom30MinBreakRequirement = value;
    }

    public boolean getIsWeeklyResetUsed() {
        return isWeeklyResetUsed;
    }

    public void setIsWeeklyResetUsed(boolean value) {
        this.isWeeklyResetUsed = value;
    }

    public boolean getIsWeeklyResetUsedOverridden() {
        return isWeeklyResetUsedOverridden;
    }

    public void setIsWeeklyResetUsedOverridden(boolean value) {
        this.isWeeklyResetUsedOverridden = value;
    }

    public boolean getIsOperatesSpecificVehiclesForOilfield() {
        return isOperatesSpecificVehiclesForOilfield;
    }

    public void setIsOperatesSpecificVehiclesForOilfield(boolean value) {
        this.isOperatesSpecificVehiclesForOilfield = value;
    }

    public boolean getIsNonCDLShortHaulExceptionUsed() {
        return this.isNonCDLShortHaulExceptionUsed;
    }

    public void setIsNonCDLShortHaulExceptionUsed(boolean value) {
        this.isNonCDLShortHaulExceptionUsed = value;
    }

    public ExemptLogTypeEnum getExemptLogType() {
        return exemptLogType;
    }

    public void setExemptLogType(ExemptLogTypeEnum exemptLogType) {
        this.exemptLogType = exemptLogType;
    }

    public boolean getIsCertified() {
        return isCertified;
    }

    public void setIsCertified(boolean isCertified) {
        this.isCertified = isCertified;
    }

    public boolean getIsTransitionalLog() {
        return this.isTransitional;
    }

    public void setIsTransitionalLog(boolean isTransitionalLog) {
        this.isTransitional = isTransitionalLog;
    }

    public boolean getIsExemptFromELDUse() {
        return isExemptFromELDUse;
    }

    public void setIsExemptFromELDUse(boolean exemptFromELDUse) {
        isExemptFromELDUse = exemptFromELDUse;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("EmployeeLog{");
        sb.append("employeeId='").append(employeeId).append('\'');
        sb.append(", logDate=").append(logDate);
        sb.append(", totalLogDistance=").append(totalLogDistance);
        sb.append(", hasReturnedToLocation=").append(hasReturnedToLocation);
        sb.append(", driverType=").append(driverType);
        sb.append(", ruleset=").append(ruleset);
        sb.append(", eldEventList=").append(eldEventList);
        sb.append(", timezone=").append(timezone);
        sb.append(", teamDriverList=").append(teamDriverList);
        sb.append(", mobileStartTimestamp=").append(mobileStartTimestamp);
        sb.append(", mobileEndTimestamp=").append(mobileEndTimestamp);
        sb.append(", mobileRecordedDistance=").append(mobileRecordedDistance);
        sb.append(", mobileEobrIdentifier='").append(mobileEobrIdentifier).append('\'');
        sb.append(", tractorNumbers='").append(tractorNumbers).append('\'');
        sb.append(", trailerNumbers='").append(trailerNumbers).append('\'');
        sb.append(", trailerPlate='").append(trailerPlate).append('\'');
        sb.append(", shipmentInformation='").append(shipmentInformation).append('\'');
        sb.append(", vehiclePlate='").append(vehiclePlate).append('\'');
        sb.append(", timeSyncFailureList=").append(timeSyncFailureList);
        sb.append(", eobrFailureList=").append(eobrFailureList);
        sb.append(", isShortHaulExceptionUsed=").append(isShortHaulExceptionUsed);
        sb.append(", weeklyResetStartTimestamp=").append(weeklyResetStartTimestamp);
        sb.append(", canadaDeferralType=").append(canadaDeferralType);
        sb.append(", isHaulingExplosives=").append(isHaulingExplosives);
        sb.append(", isExemptFrom30MinBreakRequirement=").append(isExemptFrom30MinBreakRequirement);
        sb.append(", isWeeklyResetUsed=").append(isWeeklyResetUsed);
        sb.append(", isWeeklyResetUsedOverridden=").append(isWeeklyResetUsedOverridden);
        sb.append(", isOperatesSpecificVehiclesForOilfield=").append(isOperatesSpecificVehiclesForOilfield);
        sb.append(", isNonCDLShortHaulExceptionUsed=").append(isNonCDLShortHaulExceptionUsed);
        sb.append(", exemptLogType=").append(exemptLogType);
        sb.append(", isCertified=").append(isCertified);
        sb.append(", isTransitional=").append(isTransitional);
        sb.append(", isExemptFromELDUse=").append(isExemptFromELDUse);
        sb.append(", logType=").append(logType);
        sb.append('}');
        return sb.toString();
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // Custom methods
    ///////////////////////////////////////////////////////////////////////////////////////

    private static final int PRECISION_VALUE = 10;

    /// <summary>
    /// Answer the calculated distance on the log by looking at the odometer readings
    /// in the driving statuses that occur after the MobileStartTimestamp.
    /// </summary>
    /// <returns></returns>
    public float getMobileDerivedDistance() {
        int totalDistanceTimes10 = 0;
        if (this.getMobileStartTimestamp() != null && this.getEldEventList() != null) {
            EmployeeLogEldEvent[] activeEvents = this.getEldEventList().getActiveEldEventList(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus);
            for (EmployeeLogEldEvent evt : activeEvents) {
                if (evt.getDutyStatusEnum().getValue() == DutyStatusEnum.DRIVING && evt.getStartTime().compareTo(this.getMobileStartTimestamp()) >= 0) {
                    if (evt.getLocation().getOdometerReading() >= 0.0F && evt.getLocation().getEndOdometerReading() > 0.0F) {
                        int intEnd = (int) (evt.getLocation().getEndOdometerReading() * PRECISION_VALUE);
                        int intStart = (int) (evt.getLocation().getOdometerReading() * PRECISION_VALUE);
                        totalDistanceTimes10 += intEnd - intStart;
                    } else if (evt.getDistance() != null) {
                        totalDistanceTimes10 += evt.getDistance().intValue() * PRECISION_VALUE;
                    }
                }
            }
        }
        return (float) totalDistanceTimes10 / (float) PRECISION_VALUE;
    }
}
