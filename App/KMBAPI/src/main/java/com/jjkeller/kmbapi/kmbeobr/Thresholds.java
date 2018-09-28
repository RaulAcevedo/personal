package com.jjkeller.kmbapi.kmbeobr;

import android.os.Bundle;

import com.jjkeller.kmbapi.eobrengine.CalcCRC;

public class Thresholds {
    private int rpm, driveStopTime, eventBlanking;
    private float speed, hardBrake, driveStartDistance, driveStartSpeed;
    private String driverId;
    private ThresholdTypeEnum thresholdType;

    public Thresholds() {}
    public Thresholds(int rpm, float speed, float hardBrake, float driveStartDistance, float driveStartSpeed, int driveStopTime, String driverId, int eventBlanking) {
        setRpm(rpm);
        setSpeed(speed);
        setHardBrake(hardBrake);
        setDriveStartDistance(driveStartDistance);
        setDriveStartSpeed(driveStartSpeed);
        setDriveStopTime(driveStopTime);
        setDriverId(driverId);
        setEventBlanking(eventBlanking);
    }

    public static final Thresholds DEFAULT = new Thresholds(3000, 200, 200, .5f, 5, 1, "", 10 );

    public int getRpm() {
        return rpm;
    }
    public void setRpm(int rpm) {
        this.rpm = rpm;
    }

    public int getDriveStopTime() {
        return driveStopTime;
    }
    public void setDriveStopTime(int driveStopTime) {
        this.driveStopTime = driveStopTime;
    }

    public int getEventBlanking() {
        return eventBlanking;
    }
    public void setEventBlanking(int eventBlanking) {
        this.eventBlanking = eventBlanking;
    }

    public float getSpeed() {
        return speed;
    }
    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getHardBrake() {
        return hardBrake;
    }
    public void setHardBrake(float hardBrake) {
        this.hardBrake = hardBrake;
    }

    public float getDriveStartDistance() {
        return driveStartDistance;
    }
    public void setDriveStartDistance(float driveStartDistance) {
        this.driveStartDistance = driveStartDistance;
    }

    public float getDriveStartSpeed() {
        return driveStartSpeed;
    }
    public void setDriveStartSpeed(float driveStartSpeed) {
        this.driveStartSpeed = driveStartSpeed;
    }

    public String getDriverId() {
        return driverId;
    }
    public void setDriverId(String driverId) {
        this.driverId = driverId;

        if(driverId == null || driverId.length() == 0)
            thresholdType = ThresholdTypeEnum.DEFAULT;
        else
            thresholdType = ThresholdTypeEnum.DRIVER;
    }

    public ThresholdTypeEnum getThresholdType() {
        return thresholdType;
    }
    public void setThresholdType(ThresholdTypeEnum thresholdType) {
        this.thresholdType = thresholdType;
    }

    public int getDriverIdCRC() {
        if(driverId != null && driverId.length() > 0)
            return CalcCRC.Calculate(driverId, driverId.length());

        return 0;
    }

    public BundleBuilder toBundleBuilder() {
        return BundleBuilder.empty()
                .withValue(Constants.RPMTHRESHOLD, getRpm())
                .withValue(Constants.SPEEDTHRESHOLD, getSpeed())
                .withValue(Constants.HARDBRAKETHRESHOLD, getHardBrake())
                .withValue(Constants.DRIVESTARTDISTANCETHRESHOLD, getDriveStartDistance())
                .withValue(Constants.DRIVESTOPTIMETHRESHOLD, getDriveStopTime())
                .withValue(Constants.EVENTBLANKINGTHRESHOLD, getEventBlanking())
                .withValue(Constants.DRIVESTARTSPEED, getDriveStartSpeed())
                .withValue(Constants.DRIVERIDCRC, getDriverIdCRC());
    }
}
