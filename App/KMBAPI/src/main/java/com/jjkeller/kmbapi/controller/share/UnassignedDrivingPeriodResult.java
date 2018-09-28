package com.jjkeller.kmbapi.controller.share;

import com.jjkeller.kmbapi.proxydata.UnassignedDrivingPeriod;

import java.util.Date;

public class UnassignedDrivingPeriodResult
{
	private boolean detectedAnyDrivingPeriods;
	private boolean detectedDrivingPeriodsForCurrentLog;
	private boolean detectedPreloginDrivingPeriods;
	private Date orphanedTripTime = null;
	private float orphanedOdometer = 0F;
	private float orphanedLatitude = 0F;
	private float orphanedLongitude = 0F;
	private UnassignedDrivingPeriod unassignedPeriod;
	
	public boolean getDetectedAnyDrivingPeriods() {
		return detectedAnyDrivingPeriods;
	}
	public void setDetectedAnyDrivingPeriods(boolean detectedAnyDrivingPeriods) {
		this.detectedAnyDrivingPeriods = detectedAnyDrivingPeriods;
	}
	
	public boolean getDetectedDrivingPeriodsForCurrentLog() {
		return detectedDrivingPeriodsForCurrentLog;
	}
	public void setDetectedDrivingPeriodsForCurrentLog(
			boolean detectedDrivingPeriodsForCurrentLog) {
		this.detectedDrivingPeriodsForCurrentLog = detectedDrivingPeriodsForCurrentLog;
	}
	
	public boolean getDetectedPreloginDrivingPeriods() {
		return detectedPreloginDrivingPeriods;
	}
	public void setDetectedPreloginDrivingPeriods(
			boolean detectedPreloginDrivingPeriods) {
		this.detectedPreloginDrivingPeriods = detectedPreloginDrivingPeriods;
	}
	
	public Date getOrphanedTripTime() {
		return orphanedTripTime;
	}
	public void setOrphanedTripTime(Date orphanedTripTime) {
		this.orphanedTripTime = orphanedTripTime;
	}
	
	public float getOrphanedOdometer() {
		return orphanedOdometer;
	}
	public void setOrphanedOdometer(float orphanedOdometer) {
		this.orphanedOdometer = orphanedOdometer;
	}
	
	public float getOrphanedLatitude() {
		return orphanedLatitude;
	}
	public void setOrphanedLatitude(float orphanedLatitude) {
		this.orphanedLatitude = orphanedLatitude;
	}
	
	public float getOrphanedLongitude() {
		return orphanedLongitude;
	}
	public void setOrphanedLongitude(float orphanedLongitude) {
		this.orphanedLongitude = orphanedLongitude;
	}
	
	public UnassignedDrivingPeriod getUnassignedPeriod() {
		return unassignedPeriod;
	}
	public void setUnassignedPeriod(UnassignedDrivingPeriod unassignedPeriod) {
		this.unassignedPeriod = unassignedPeriod;
	}
}
