package com.jjkeller.kmbapi.proxydata;

import com.jjkeller.kmbapi.HosMessageProcessor.interfaces.IHOSMessage;

import org.joda.time.DateTime;

/**
 * Created by jld5296 on 9/19/16.
 */
public class HosMessageToGeoTabHosDataAdapter extends GeotabHOSData {
    private int _driverId;
    private String _vehicleId;
    private IHOSMessage _hosMessage;

    public HosMessageToGeoTabHosDataAdapter(IHOSMessage hosMessage, int driverId, String vehicleId) {
        _hosMessage = hosMessage;
        _driverId = driverId;
        _vehicleId = vehicleId;
    }

    @Override
    public float getTachometer() {
        return _hosMessage.getTachometer();
    }

    @Override
    public void setTachometer(float tachometer) {
        _hosMessage.setTachometer(tachometer);
    }

    @Override
    public float getSpeedometer() {
        return _hosMessage.getSpeedometer();
    }

    @Override
    public void setSpeedometer(float speedometer) {
        _hosMessage.setSpeedometer(speedometer);
    }

    @Override
    public DateTime getTimestampUtc() {
        return _hosMessage.getTimestampUtc();
    }

    @Override
    public void setTimestampUtc(DateTime timestampUtc) {
        _hosMessage.setTimestampUtc(timestampUtc);
    }

    @Override
    public float getEngineHours() {
        return _hosMessage.getEngineHours();
    }

    @Override
    public void setEngineHours(float engineHours) {
        _hosMessage.setEngineHours(engineHours);
    }

    @Override
    public float getGpsLatitude() {
        return _hosMessage.getGpsLatitude();
    }

    @Override
    public void setGpsLatitude(float gpsLatitude) {
        _hosMessage.setGpsLatitude(gpsLatitude);
    }

    @Override
    public float getGpsLongitude() {
        return _hosMessage.getGpsLongitude();
    }

    @Override
    public void setGpsLongitude(float gpsLongitude) {
        _hosMessage.setGpsLongitude(gpsLongitude);
    }

    @Override
    public float getOdometer() {
        return _hosMessage.getOdometer();
    }

    @Override
    public void setOdometer(float odometer) {
        _hosMessage.setOdometer(odometer);
    }

    @Override
    public float getOrigOdometer() {
        return _hosMessage.getOrigOdometer();
    }

    @Override
    public void setOrigOdometer(float odometer) {
        _hosMessage.setOrigOdometer(odometer);
    }

    @Override
    public float getTripOdometer() {
        return _hosMessage.getTripOdometer();
    }

    @Override
    public void setTripOdometer(float tripOdometer) {
        _hosMessage.setTripOdometer(tripOdometer);
    }

    @Override
    public float getTripEngineSeconds() {
        return _hosMessage.getTripEngineSeconds();
    }

    @Override
    public void setTripEngineSeconds(float tripEngineSeconds) {
        _hosMessage.setTripEngineSeconds(tripEngineSeconds);
    }

    @Override
    public String getVehicleId() { return _vehicleId; }

    @Override
    public void setVehicleId(String vehicleId) {
        _vehicleId = vehicleId;
    }

    @Override
    public boolean isGpsValid() {
        return _hosMessage.isGpsValid();
    }

    @Override
    public void setGpsValid(boolean gpsValid) {
        _hosMessage.setGpsValid(gpsValid);
    }

    @Override
    public boolean isIgnitionOn() {
        return _hosMessage.isIgnitionOn();
    }

    @Override
    public void setIgnitionOn(boolean ignitionOn) {
        _hosMessage.setIgnitionOn(ignitionOn);
    }

    @Override
    public boolean isEngineActivityDetected() {
        return _hosMessage.isEngineActivityDetected();
    }

    @Override
    public void setEngineActivityDetected(boolean engineActivityDetected) {
        _hosMessage.setEngineActivityDetected(engineActivityDetected);
    }

    @Override
    public boolean isDateTimeValid() {

        return _hosMessage.isDatetimeValid();
    }

    @Override
    public void setDateTimeValid(boolean dateTimeValid) {
        _hosMessage.setDatetimeValid(dateTimeValid);
    }

    @Override
    public boolean isSpeedFromEngine() {
        return _hosMessage.isSpeedFromEngine();
    }

    @Override
    public void setSpeedFromEngine(boolean speedFromEngine) {
        _hosMessage.setSpeedFromEngine(speedFromEngine);
    }

    @Override
    public boolean isOdometerFromEngine() {
        return _hosMessage.isOdometerFromEngine();
    }

    @Override
    public void setOdometerFromEngine(boolean odometerFromEngine) {
        _hosMessage.setOdometerFromEngine(odometerFromEngine);
    }

    @Override
    public int getDriverId() {
        return _driverId;
    }

    @Override
    public void setDriverId(int driverId) {
        super.setDriverId(driverId);
        // Does nothing
    }

    @Override
    public long getEventDataRecordKey() {
        return super.getEventDataRecordKey();
    }

    @Override
    public void setEventDataRecordKey(long eventDataRecordKey) {
        super.setEventDataRecordKey(eventDataRecordKey);
    }

    @Override
    public boolean isSubmitted() {
        return super.isSubmitted();
    }

    @Override
    public void setSubmitted(boolean submitted) {
        super.setSubmitted(submitted);
    }

    @Override
    public DateTime getOriginalTimestampUTC() {
        return _hosMessage.getOriginalTimestampUtc();
    }

    @Override
    public void setOriginalTimestampUTC(DateTime dateTime) {
        _hosMessage.setOriginalTimestampUtc(dateTime);
    }

    @Override
    public float getGpsUncertDistance() {
        return _hosMessage.getGpsUncertDistance();
    }

    @Override
    public void setGpsUncertDistance(float gpsUncertDistance) {
        super.setGpsUncertDistance(_hosMessage.getGpsUncertDistance());
    }
}
