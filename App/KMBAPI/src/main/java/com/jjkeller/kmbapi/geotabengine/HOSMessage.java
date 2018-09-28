package com.jjkeller.kmbapi.geotabengine;

import com.jjkeller.kmbapi.HosMessageProcessor.interfaces.IHOSMessage;

import org.joda.time.DateTime;

/**
 * Created by jhm2586 on 8/31/2016.
 */
public class HOSMessage implements IHOSMessage {

    private DateTime timestampUtc = null;
    private float gpsLatitude = 0.0F;
    private float gpsLongitude = 0.0F;
    private float speedometer = -1.0F;
    private float tachometer = -1.0F;
    private float odometer = -1.0F;
    private float origOdometer = -1.0F;
    private float tripOdometer = -1.0F;
    private float engineHours = -1.0F;
    private float tripEngineSeconds = -1.0F;

    private boolean gpsValid = false;
    private boolean ignitionOn = false;
    private boolean engineActivityDetected = false;
    private boolean datetimeValid = false;
    private boolean isSpeedFromEngine = false;
    private boolean isOdometerFromEngine = false;
    private DateTime originalTimestampUtc = null;
    private float gpsUncertDistance = 0f;

    public HOSMessage() {
    }

    public HOSMessage(IHOSMessage hosMessage) {
        this.timestampUtc = hosMessage.getTimestampUtc();
        this.gpsLatitude = hosMessage.getGpsLatitude();
        this.gpsLongitude = hosMessage.getGpsLongitude();
        this.speedometer = hosMessage.getSpeedometer();
        this.tachometer = hosMessage.getTachometer();
        this.odometer = hosMessage.getOdometer();
        this.tripOdometer = hosMessage.getTripOdometer();
        this.engineHours = hosMessage.getEngineHours();
        this.tripEngineSeconds = hosMessage.getTripEngineSeconds();
        this.gpsValid = hosMessage.isGpsValid();
        this.ignitionOn = hosMessage.isIgnitionOn();
        this.engineActivityDetected = hosMessage.isEngineActivityDetected();
        this.datetimeValid = hosMessage.isDatetimeValid();
        this.isSpeedFromEngine = hosMessage.isSpeedFromEngine();
        this.isOdometerFromEngine = hosMessage.isOdometerFromEngine();
        this.originalTimestampUtc = hosMessage.getOriginalTimestampUtc();
        this.gpsUncertDistance = hosMessage.getGpsUncertDistance();
    }

    @Override
    public DateTime getTimestampUtc() {
        return timestampUtc;
    }

    @Override
    public void setTimestampUtc(DateTime timestampUtc) {
        this.timestampUtc = timestampUtc;
    }

    @Override
    public float getGpsLatitude() { return gpsLatitude; }

    @Override
    public void setGpsLatitude(float gpsLatitude) {
        this.gpsLatitude = gpsLatitude;
    }

    @Override
    public float getGpsLongitude() {
        return gpsLongitude;
    }

    @Override
    public void setGpsLongitude(float gpsLongitude) {
        this.gpsLongitude = gpsLongitude;
    }

    @Override
    public float getSpeedometer() {
        return speedometer;
    }

    @Override
    public void setSpeedometer(float speedometer) {
        this.speedometer = speedometer;
    }

    @Override
    public float getTachometer() {
        return tachometer;
    }

    @Override
    public void setTachometer(float tachometer) {
        this.tachometer = tachometer;
    }

    @Override
    public float getOdometer() {
        return odometer;
    }

    @Override
    public void setOdometer(float odometer) {
        this.odometer = odometer;
    }

    @Override
    public float getOrigOdometer() {
        return origOdometer;
    }

    @Override
    public void setOrigOdometer(float odometer) {
        this.origOdometer = odometer;
    }

    @Override
    public float getTripOdometer() {
        return tripOdometer;
    }

    @Override
    public void setTripOdometer(float tripOdometer) {
        this.tripOdometer = tripOdometer;
    }

    @Override
    public float getEngineHours() {
        return engineHours;
    }

    @Override
    public void setEngineHours(float engineHours) {
        this.engineHours = engineHours;
    }

    @Override
    public float getTripEngineSeconds()
    {
        return tripEngineSeconds;
    }

    @Override
    public void setTripEngineSeconds (float tripEngineSeconds)
    {
        this.tripEngineSeconds = tripEngineSeconds;
    }

    @Override
    public boolean isGpsValid() {
        return gpsValid;
    }

    @Override
    public void setGpsValid(boolean gpsValid) {
        this.gpsValid = gpsValid;
    }

    @Override
    public boolean isIgnitionOn() {
        return ignitionOn;
    }

    @Override
    public void setIgnitionOn(boolean ignitionOn) {
        this.ignitionOn = ignitionOn;
    }

    public boolean isEngineActivityDetected() {
        return engineActivityDetected;
    }

    public void setEngineActivityDetected(boolean engineActivityDetected) {
        this.engineActivityDetected = engineActivityDetected;
    }

    @Override
    public boolean isDatetimeValid() {
        return datetimeValid;
    }

    @Override
    public void setDatetimeValid(boolean datetimeValid) {
        this.datetimeValid = datetimeValid;
    }

    @Override
    public int getSerialNumberCrc() {
        return 0;
    }

    @Override
    public void setSerialNumberCrc(int serialNumberCrc) {

    }

    @Override
    public float getGpsUncertDistance() {
        return this.gpsUncertDistance;
    }

    @Override
    public void setGpsUncertDistance(float gpsUncertDistance) {

        this.gpsUncertDistance = gpsUncertDistance;
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

    @Override
    public boolean equals(Object o) {
        if(this.getClass() != o.getClass()) {
            return false;
        }

        HOSMessage otherMessage = (HOSMessage)o;

        if(this.getEngineHours()!= otherMessage.getEngineHours()) { return false; }
        if(this.getGpsLatitude() != otherMessage.getGpsLatitude()) { return false; }
        if(this.getGpsLongitude() != otherMessage.getGpsLongitude()) { return false; }
        if(this.getOdometer() != otherMessage.getOdometer()) { return false; }
        if(this.getSerialNumberCrc() != otherMessage.getSerialNumberCrc()) { return false; }
        if(this.getSpeedometer() != otherMessage.getSpeedometer()) { return false; }
        if(this.getTachometer() != otherMessage.getTachometer()) { return false; }
        if(this.getTimestampUtc() != otherMessage.getTimestampUtc()) { return false; }
        if(this.getTripEngineSeconds() != otherMessage.getTripEngineSeconds()) { return false; }
        if(this.getTripOdometer() != otherMessage.getTripOdometer()) { return false; }
        if(this.getGpsUncertDistance() != otherMessage.getGpsUncertDistance()) { return false; }
        if(this.isSpeedFromEngine() != otherMessage.isSpeedFromEngine()) { return false;}
        if(this.isOdometerFromEngine() != otherMessage.isOdometerFromEngine()) {return false;}
        if(this.isIgnitionOn() != otherMessage.isIgnitionOn()) {return false;}

        return true;
    }

    @Override
    public int hashCode() {
        return (int)(this.getTimestampUtc().getMillis() / 1000) * 5;
    }

    @Override
    public DateTime getOriginalTimestampUtc() {
        return originalTimestampUtc;
    }

    @Override
    public void setOriginalTimestampUtc(DateTime originalTimestampUtc) {
        this.originalTimestampUtc = originalTimestampUtc;
    }

    @Override
    public boolean checkIfEngineSyncFlagsChanged(IHOSMessage message){
        return (this.isSpeedFromEngine() == message.isSpeedFromEngine() && this.isOdometerFromEngine() == message.isOdometerFromEngine()) ? false : true;
    }
}
