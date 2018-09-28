package com.jjkeller.kmbapi.geotab;

import com.jjkeller.kmbapi.HosMessageProcessor.interfaces.IHOSMessage;
import com.jjkeller.kmbapi.geotabengine.HOSMessage;
import com.jjkeller.kmbapi.kmbeobr.Constants;

import org.joda.time.DateTime;

import java.util.Date;

/**
 * Created by jld5296 on 10/14/16.
 */
public class HosMessages {

    private static IHOSMessage init() {
        IHOSMessage hosMessage = new HOSMessage();
        hosMessage.setIgnitionOn(true);
        hosMessage.setOdometerFromEngine(true);
        hosMessage.setOdometer(1000);
        hosMessage.setOrigOdometer(1000 * Constants.KILOMETERS_PER_MILE);
        hosMessage.setGpsLatitude(45.000000f);
        hosMessage.setGpsLongitude(45.000000f);
        hosMessage.setDatetimeValid(true);
        hosMessage.setEngineActivityDetected(true);
        hosMessage.setEngineHours(100f);
        hosMessage.setGpsValid(true);
        hosMessage.setSpeedometer(0);
        hosMessage.setTachometer(600);
        hosMessage.setTripEngineSeconds(10);
        hosMessage.setTripOdometer(100);
        hosMessage.setSpeedFromEngine(true);
        return hosMessage;
    }

    public static IHOSMessage EngineOnStopped(Date eventTime) {
        IHOSMessage hosMessage = init();
        hosMessage.setTimestampUtc(new DateTime(eventTime.getTime()));
        hosMessage.setOdometerFromEngine(true);
        hosMessage.setSpeedFromEngine(true);
        hosMessage.setIgnitionOn(true);
        hosMessage.setTachometer(2000);
        hosMessage.setEngineActivityDetected(hosMessage.isIgnitionOn());
        return hosMessage;
    }

    public static IHOSMessage EngineOff(Date eventTime) {
        IHOSMessage hosMessage = init();
        hosMessage.setTimestampUtc(new DateTime(eventTime));
        hosMessage.setOdometerFromEngine(true);
        hosMessage.setSpeedFromEngine(false);
        hosMessage.setSpeedometer(0);
        hosMessage.setTachometer(0); // can be < 100 as well
        hosMessage.setIgnitionOn(false);
        hosMessage.setEngineActivityDetected(hosMessage.isIgnitionOn());
        return hosMessage;
    }

    public static IHOSMessage EngineOffMoving(Date eventTime) {
        IHOSMessage hosMessage = init();
        hosMessage.setTimestampUtc(new DateTime(eventTime));
        hosMessage.setOdometerFromEngine(true);
        hosMessage.setSpeedFromEngine(false);
        hosMessage.setSpeedometer(30);
        hosMessage.setTachometer(5); // can be < 100 as well
        hosMessage.setIgnitionOn(false);
        hosMessage.setEngineActivityDetected(hosMessage.isIgnitionOn());
        return hosMessage;
    }

    public static IHOSMessage EngineOnMoving(Date eventTime) {
        IHOSMessage hosMessage = init();
        hosMessage.setTimestampUtc(new DateTime(eventTime));
        hosMessage.setOdometerFromEngine(true);
        hosMessage.setSpeedFromEngine(true);
        hosMessage.setSpeedometer(100);
                hosMessage.setTachometer(2000); // can be < 100 as well
        hosMessage.setIgnitionOn(true);
        hosMessage.setEngineActivityDetected(hosMessage.isIgnitionOn());
        return hosMessage;
    }

    public static IHOSMessage GPSBasedVSS(Date eventTime){
        IHOSMessage hosMessage = init();
        hosMessage.setTimestampUtc(new DateTime(eventTime));
        hosMessage.setOdometerFromEngine(true);
        hosMessage.setSpeedFromEngine(false);
        hosMessage.setSpeedometer(100);
        hosMessage.setTachometer(2000); // can be < 100 as well
        hosMessage.setIgnitionOn(true);
        hosMessage.setEngineActivityDetected(hosMessage.isIgnitionOn());
        return hosMessage;
    }
    public static IHOSMessage EngineBasedVSS(Date eventTime){
        IHOSMessage hosMessage = init();
        hosMessage.setTimestampUtc(new DateTime(eventTime));
        hosMessage.setOdometerFromEngine(true);
        hosMessage.setSpeedFromEngine(true);
        hosMessage.setSpeedometer(100);
        hosMessage.setTachometer(2000); // can be < 100 as well
        hosMessage.setIgnitionOn(true);
        hosMessage.setEngineActivityDetected(hosMessage.isIgnitionOn());
        return hosMessage;
    }
    public static IHOSMessage GPSBasedVssOdo(Date eventTime){
        IHOSMessage hosMessage = init();
        hosMessage.setTimestampUtc(new DateTime(eventTime));
        hosMessage.setOdometerFromEngine(false);
        hosMessage.setSpeedFromEngine(false);
        hosMessage.setSpeedometer(100);
        hosMessage.setTachometer(2000);
        hosMessage.setIgnitionOn(true);
        hosMessage.setEngineActivityDetected(hosMessage.isIgnitionOn());
        return hosMessage;

    }
    public static IHOSMessage GPSBasedOdoDriving(Date eventTime){
        IHOSMessage hosMessage = init();
        hosMessage.setTimestampUtc(new DateTime(eventTime));
        hosMessage.setOdometerFromEngine(false);
        hosMessage.setSpeedFromEngine(true);
        hosMessage.setSpeedometer(100);
        hosMessage.setTachometer(100);
        hosMessage.setIgnitionOn(true);
        hosMessage.setEngineActivityDetected(hosMessage.isIgnitionOn());
        return hosMessage;
    }

    public static IHOSMessage GPSBasedOdoStopped(Date eventTime){
        IHOSMessage hosMessage = init();
        hosMessage.setTimestampUtc(new DateTime(eventTime));
        hosMessage.setOdometerFromEngine(false);
        hosMessage.setSpeedFromEngine(true);
        hosMessage.setSpeedometer(0);
        hosMessage.setTachometer(100);
        hosMessage.setIgnitionOn(true);
        hosMessage.setEngineActivityDetected(hosMessage.isIgnitionOn());
        return hosMessage;
    }

    public static IHOSMessage GPSBasedOdoOffStopped(Date eventTime){
        IHOSMessage hosMessage = init();
        hosMessage.setTimestampUtc(new DateTime(eventTime));
        hosMessage.setOdometerFromEngine(false);
        hosMessage.setSpeedFromEngine(true);
        hosMessage.setSpeedometer(0);
        hosMessage.setTachometer(100);
        hosMessage.setIgnitionOn(true);
        hosMessage.setEngineActivityDetected(hosMessage.isIgnitionOn());
        return hosMessage;
    }

}
