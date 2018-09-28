package com.jjkeller.kmbapi.proxydata;

import org.joda.time.DateTime;

/**
 * Created by jhm2586 on 9/1/2016.
 */
public class GeotabHOSData extends ProxyBase{
    private DateTime timestampUtc= null;
    private float gpsLatitude = 0f;
    private float gpsLongitude = 0f;
    private float speedometer = -1.0f;
    private float tachometer = -1.0f;
    private float odometer = -1.0F;
    private float origOdometer = -1.0F;
    private float tripOdometer = -1.0F;
    private float engineHours = -1.0F;
    private float tripEngineSeconds = -1.0F;
    private String vehicleId;
    private boolean gpsValid;
    private boolean ignitionOn;
    private boolean engineActivityDetected;
    private boolean dateTimeValid;
    private int driverId;
    private long eventDataRecordKey;
    private boolean isSubmitted;
    private DateTime originalTimestampUTC;
    private boolean isSpeedFromEngine;
    private boolean isOdometerFromEngine;
    private float gpsUncertDistance = 0f;

    public DateTime getTimestampUtc() {
        return timestampUtc;
    }

    public void setTimestampUtc(DateTime timestampUtc) {
        this.timestampUtc = timestampUtc;
    }

    public float getGpsLatitude() {
        return gpsLatitude;
    }

    public void setGpsLatitude(float gpsLatitude) {
        this.gpsLatitude = gpsLatitude;
    }

    public float getGpsLongitude() {
        return gpsLongitude;
    }

    public void setGpsLongitude(float gpsLongitude) {
        this.gpsLongitude = gpsLongitude;
    }

    public float getSpeedometer() {
        return speedometer;
    }

    public void setSpeedometer(float speedometer) {
        this.speedometer = speedometer;
    }

    public float getTachometer() {
        return tachometer;
    }

    public void setTachometer(float tachometer) {
        this.tachometer = tachometer;
    }

    public float getOdometer() {
        return odometer;
    }

    public void setOdometer(float odometer) {
        this.odometer = odometer;
    }

    public float getOrigOdometer() {
        return origOdometer;
    }

    public void setOrigOdometer(float odometer) {
        this.origOdometer = odometer;
    }

    public float getTripOdometer() {
        return tripOdometer;
    }

    public void setTripOdometer(float tripOdometer) {
        this.tripOdometer = tripOdometer;
    }

    public float getEngineHours() {
        return engineHours;
    }

    public void setEngineHours(float engineHours) {
        this.engineHours = engineHours;
    }

    public float getTripEngineSeconds() {
        return tripEngineSeconds;
    }

    public void setTripEngineSeconds(float tripEngineSeconds) {
        this.tripEngineSeconds = tripEngineSeconds;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public boolean isGpsValid() {
        return gpsValid;
    }

    public void setGpsValid(boolean gpsValid) {
        this.gpsValid = gpsValid;
    }

    public boolean isIgnitionOn() {
        return ignitionOn;
    }

    public void setIgnitionOn(boolean ignitionOn) {
        this.ignitionOn = ignitionOn;
    }

    public boolean isEngineActivityDetected() {
        return engineActivityDetected;
    }

    public void setEngineActivityDetected(boolean engineActivityDetected) {
        this.engineActivityDetected = engineActivityDetected;
    }

    public boolean isDateTimeValid() {
        return dateTimeValid;
    }

    public void setDateTimeValid(boolean dateTimeValid) {
        this.dateTimeValid = dateTimeValid;
    }

    public boolean isSpeedFromEngine() {
        return isSpeedFromEngine;
    }

    public void setSpeedFromEngine(boolean speedFromEngine) {
        isSpeedFromEngine = speedFromEngine;
    }

    public boolean isOdometerFromEngine() {
        return isOdometerFromEngine;
    }

    public void setOdometerFromEngine(boolean odometerFromEngine) {
        isOdometerFromEngine = odometerFromEngine;
    }

    public int getDriverId() {
        return driverId;
    }

    public void setDriverId(int driverId) {
        this.driverId = driverId;
    }

    public long getEventDataRecordKey() {
        return eventDataRecordKey;
    }

    public void setEventDataRecordKey(long eventDataRecordKey) {
        this.eventDataRecordKey = eventDataRecordKey;
    }

    public boolean isSubmitted() {
        return isSubmitted;
    }

    public void setSubmitted(boolean submitted) {
        isSubmitted = submitted;
    }

    public DateTime getOriginalTimestampUTC() {
        return originalTimestampUTC;
    }

    public void setOriginalTimestampUTC(DateTime originalTimestampUTC) {
        this.originalTimestampUTC = originalTimestampUTC;
    }

    public float getGpsUncertDistance() {
        return gpsUncertDistance;
    }

    public void setGpsUncertDistance(float gpsUncertDistance) {
        this.gpsUncertDistance = gpsUncertDistance;
    }

    @Override
    public String toString() {
        return "GeotabHOSData{" +
                "timestampUtc=" + timestampUtc +
                ", gpsLatitude=" + gpsLatitude +
                ", gpsLongitude=" + gpsLongitude +
                ", speedometer=" + speedometer +
                ", tachometer=" + tachometer +
                ", odometer=" + odometer +
                ", tripOdometer=" + tripOdometer +
                ", engineHours=" + engineHours +
                ", tripEngineSeconds=" + tripEngineSeconds +
                ", vehicleId='" + vehicleId + '\'' +
                ", gpsValid=" + gpsValid +
                ", ignitionOn=" + ignitionOn +
                ", engineActivityDetected=" + engineActivityDetected +
                ", dateTimeValid=" + dateTimeValid +
                ", driverId=" + driverId +
                ", eventDataRecordKey=" + eventDataRecordKey +
                ", isSubmitted=" + isSubmitted +
                ", originalTimestampUTC=" + originalTimestampUTC +
                ", submitted=" + isSubmitted() +
                '}';
    }
}
