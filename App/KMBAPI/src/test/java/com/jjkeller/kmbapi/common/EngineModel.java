package com.jjkeller.kmbapi.common;

import com.jjkeller.kmbapi.common.TimeKeeper;

import java.util.Date;

/**
 * Created by jld5296 on 10/13/16.
 */
public class EngineModel {
    private Date lastSpeedChange;
    private float rpm;
    private float vss;
    private float odometer;
    private boolean ignitionTurnedOffManually;

    public EngineModel(float startOdometer) {
        lastSpeedChange = TimeKeeper.getInstance().now();
        this.odometer = startOdometer;
    }

    public void vss(float speedInMph) {
        float rpm = 0.0f;
        if (!this.ignitionTurnedOffManually) {
            if (speedInMph <= 25)
                rpm = 1000;
            if (speedInMph > 25 && speedInMph <= 60)
                rpm = 2000;
            if (speedInMph > 60)
                rpm = 2500;
        }
        this.rpm = rpm;
        odometer += getOdometerChangeSinceLastUpdate();
        lastSpeedChange = TimeKeeper.getInstance().now();
        this.vss = speedInMph;
    }

    private float getOdometerChangeSinceLastUpdate() {
        Date now = TimeKeeper.getInstance().now();
        long elapsedMilliseconds = now.getTime() - lastSpeedChange.getTime();
        float change = ((float) elapsedMilliseconds / 3600000f) * this.vss;
        return change;
    }

    public float getOdometer() {
        return odometer + getOdometerChangeSinceLastUpdate();
    }

    public void turnIgnitionOff() {
        this.rpm = 0;
        this.ignitionTurnedOffManually = true;
    }

    public void turnIgnitionOn() {
        this.rpm = 100;
        this.ignitionTurnedOffManually = false;
    }

    public boolean isIgnitionOn() {
        return this.rpm >= 100;
    }

    public float getVss() {
        return vss;
    }

    public float getRpm() {
        return rpm;
    }
}
