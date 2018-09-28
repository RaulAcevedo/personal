package com.jjkeller.kmbapi.proxydata;

import com.jjkeller.kmbapi.enums.DutyStatusEnum;
import com.jjkeller.kmbapi.enums.ExemptLogTypeEnum;

import java.util.Date;


public class DriverHoursAvailableSummary extends ProxyBase {

	///////////////////////////////////////////////////////////////////////////////////////
	// private members
	///////////////////////////////////////////////////////////////////////////////////////
    private String employeeId = null;
    private int weeklyDutyHoursAllowed = 0;
    private String weeklyDutyTimeUsed = "";
    private int dailyDutyHoursAllowed = 0;
    private String dailyDutyTimeUsed = "";
    private int drivingHoursAllowed = 0;
    private String drivingTimeUsed = "";
    private Date calculationTimestamp = null;
    private boolean isShortHaulExceptionAvailable = false;
    private int drivingHoursRestBreakAllowed = 0;
    private String drivingTimeRestBreakUsed = "";
    private ExemptLogTypeEnum exemptLogType = new ExemptLogTypeEnum(ExemptLogTypeEnum.NULL);
	private String lastMotionPictureAuthorityId = "";
	private String lastMotionPictureProductionId = "";

	//Premium Data Profiles
	// 100 – HOS Compliance and Performance
	// 110 – HOS Compliance, Performance & Geo-fencing
	///////////////////////////////////////////////////////////////////////////////////////
	private double gpsLatitude = 0;
	private double gpsLongitude = 0;
	private DutyStatusEnum dutyStatusEnum = new DutyStatusEnum(DutyStatusEnum.NULL);
	private String eobrSerialNumber = "";
    private String trailerNumber = "";

	///////////////////////////////////////////////////////////////////////////////////////
	// public get/set methods
	///////////////////////////////////////////////////////////////////////////////////////
	public String getEmployeeId() {
		return employeeId;
	}
	public void setEmployeeId(String employeeId) {
		this.employeeId = employeeId;
	}
	public int getWeeklyDutyHoursAllowed() {
		return weeklyDutyHoursAllowed;
	}
	public void setWeeklyDutyHoursAllowed(int weeklyDutyHoursAllowed) {
		this.weeklyDutyHoursAllowed = weeklyDutyHoursAllowed;
	}
	public String getWeeklyDutyTimeUsed() {
		return weeklyDutyTimeUsed;
	}
	public void setWeeklyDutyTimeUsed(String weeklyDutyTimeUsed) {
		this.weeklyDutyTimeUsed = weeklyDutyTimeUsed;
	}
	public int getDailyDutyHoursAllowed() {
		return dailyDutyHoursAllowed;
	}
	public void setDailyDutyHoursAllowed(int dailyDutyHoursAllowed) {
		this.dailyDutyHoursAllowed = dailyDutyHoursAllowed;
	}
	public String getDailyDutyTimeUsed() {
		return dailyDutyTimeUsed;
	}
	public void setDailyDutyTimeUsed(String dailyDutyTimeUsed) {
		this.dailyDutyTimeUsed = dailyDutyTimeUsed;
	}
	public int getDrivingHoursAllowed() {
		return drivingHoursAllowed;
	}
	public void setDrivingHoursAllowed(int drivingHoursAllowed) {
		this.drivingHoursAllowed = drivingHoursAllowed;
	}
	public String getDrivingTimeUsed() {
		return drivingTimeUsed;
	}
	public void setDrivingTimeUsed(String drivingTimeUsed) {
		this.drivingTimeUsed = drivingTimeUsed;
	}
	public Date getCalculationTimestamp() {
		return calculationTimestamp;
	}
	public void setCalculationTimestamp(Date calculationTimestamp) {
		this.calculationTimestamp = calculationTimestamp;
	}
	public boolean getIsShortHaulExceptionAvailable() {
		return isShortHaulExceptionAvailable;
	}
	public void setIsShortHaulExceptionAvailable(boolean isShortHaulExceptionAvailable) {
		this.isShortHaulExceptionAvailable = isShortHaulExceptionAvailable;
	}
	public int getDrivingHoursRestBreakAllowed() {
		return drivingHoursRestBreakAllowed;
	}
	public void setDrivingHoursRestBreakAllowed(int drivingHoursAllowed) {
		this.drivingHoursRestBreakAllowed = drivingHoursAllowed;
	}
	public String getDrivingTimeRestBreakUsed() {
		return drivingTimeRestBreakUsed;
	}
	public void setDrivingTimeRestBreakUsed(String drivingTimeUsed) {
		this.drivingTimeRestBreakUsed = drivingTimeUsed;
	}
	public ExemptLogTypeEnum getExemptLogTypeEnum() {
		return exemptLogType;
	}
	public void setExemptLogTypeEnum(ExemptLogTypeEnum exemptLogType){
		this.exemptLogType = exemptLogType;
	}
	public String getLastMotionPictureAuthorityId() {
		return lastMotionPictureAuthorityId;
	}
	public void setLastMotionPictureAuthorityId(String lastMotionPictureAuthorityId){
		this.lastMotionPictureAuthorityId = lastMotionPictureAuthorityId;
	}
	public String getLastMotionPictureProductionId() {
		return lastMotionPictureProductionId;
	}
	public void setLastMotionPictureProductionId(String lastMotionPictureProductionId){
		this.lastMotionPictureProductionId = lastMotionPictureProductionId;
	}

	public double getGpsLatitude() {
		return gpsLatitude;
	}

	public void setGpsLatitude(double gpsLatitude) {
		this.gpsLatitude = gpsLatitude;
	}

	public double getGpsLongitude() {
		return gpsLongitude;
	}

	public void setGpsLongitude(double gpsLongitude) {
		this.gpsLongitude = gpsLongitude;
	}

	public DutyStatusEnum getDutyStatusEnum() {
		return dutyStatusEnum;
	}

	public void setDutyStatusEnum(DutyStatusEnum dutyStatusEnum) {
		this.dutyStatusEnum = dutyStatusEnum;
	}

	public String getEobrSerialNumber() {
		return eobrSerialNumber;
	}

	public void setEobrSerialNumber(String eobrSerialNumber) {
		this.eobrSerialNumber = eobrSerialNumber;
	}

    public String getTrailerNumber() {
        return trailerNumber;
    }

    public void setTrailerNumber(String trailerNumber) {
        this.trailerNumber = trailerNumber;
    }

    ///////////////////////////////////////////////////////////////////////////////////////
	// Custom methods
	///////////////////////////////////////////////////////////////////////////////////////
}
