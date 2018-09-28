package com.jjkeller.kmbapi.HosMessageProcessor.interfaces;

import org.joda.time.DateTime;

import java.util.Date;

/**
 * Created by jhm2586 on 8/31/2016.
 */
public interface IHOSMessage {

    DateTime getTimestampUtc();
    void setTimestampUtc(DateTime timestampUtc);

    float getGpsLatitude();
    void setGpsLatitude(float gpsLatitude);

    float getGpsLongitude();
    void setGpsLongitude(float gpsLongitude);

    float getSpeedometer();
    void setSpeedometer(float speedometer);

    float getTachometer();
    void setTachometer(float tachometer);

    float getOdometer();
    void setOdometer(float odometer);

    float getOrigOdometer();
    void setOrigOdometer(float odometer);

    float getTripOdometer();
    void setTripOdometer(float tripOdometer);

    float getEngineHours();
    void setEngineHours(float engineHours);

    float getTripEngineSeconds();
    void setTripEngineSeconds (float tripEngineSeconds);

    boolean isGpsValid();
    void setGpsValid(boolean gpsValid);

    boolean isIgnitionOn();
    void setIgnitionOn(boolean ignitionOn);

    boolean isEngineActivityDetected();
    void setEngineActivityDetected(boolean engineActivity);

    boolean isDatetimeValid();
    void setDatetimeValid(boolean datetimeValid);

    int getSerialNumberCrc();
    void setSerialNumberCrc(int serialNumberCrc);

    DateTime getOriginalTimestampUtc();

    void setOriginalTimestampUtc(DateTime originalTimestampUtc);

    boolean isSpeedFromEngine();
    void setSpeedFromEngine(boolean speedFromEngine);

    boolean isOdometerFromEngine();
    void setOdometerFromEngine(boolean odometerFromEngine);

    float getGpsUncertDistance();
    void setGpsUncertDistance(float gpsUncertDistance);

    boolean checkIfEngineSyncFlagsChanged(IHOSMessage message);

  
}
