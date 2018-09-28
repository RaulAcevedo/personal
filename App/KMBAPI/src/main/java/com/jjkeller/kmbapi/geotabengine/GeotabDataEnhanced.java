package com.jjkeller.kmbapi.geotabengine;

import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.kmbeobr.Constants;

import java.util.Locale;

public class GeotabDataEnhanced extends GeotabData {
    protected final float HoursConversionFactor = 1f / 10f;

    private float tripOdometer = -1.0F;
    private float engineHours = -1.0F;
    private float tripEngineSeconds = -1.0F;
    private String vehicleId = "";

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

    public boolean isSpeedFromEngine() {
        return ((getStatus() & (1 << 4)) != 0) && !GlobalState.getInstance().getForceGeotabInvalidVss();
    }
    public boolean isOdometerFromEngine() {
        return ((getStatus() & (1 << 5)) != 0) && !GlobalState.getInstance().getForceGeotabInvalidOdo();
    }


    @Override
    public HOSMessage toHOSMessage() {
        HOSMessage message = super.toHOSMessage();

        message.setTripOdometer(getTripOdometer());
        message.setEngineHours(getEngineHours());
        message.setTripEngineSeconds(getTripEngineSeconds());
        message.setSpeedFromEngine(isSpeedFromEngine());
        message.setOdometerFromEngine(isOdometerFromEngine());

        return message;
    }

    public GeotabDataEnhanced() {
        this(new byte[42]);
    }

    public GeotabDataEnhanced(byte[] abData) {
        super(abData);

        float tripOdometer = extractUnsignedInt(abData, 23);
        tripOdometer *= OdometerConversionFactor; // Convert to km (Units given in 0.1/km)
        tripOdometer *= Constants.MILES_PER_KILOMETER; //convert to miles
        this.setTripOdometer(tripOdometer);

        long engineHours = extractUnsignedInt(abData, 27);
        this.setEngineHours(engineHours * HoursConversionFactor); // Convert to hours (Units given in 0.1/hr)

        long tripEngineSeconds = extractUnsignedInt(abData, 31);
        this.setTripEngineSeconds(tripEngineSeconds); //Units given in seconds)

        //Get Go device prefix
        Integer hardwareId = GeotabDataHelper.GetHardwareIdFromHexString(extractString(abData, 35));
        String fullSN = GeotabDataHelper.EncodeSerialNumber(GeotabDataHelper.Go7ProductId, hardwareId);

        this.setVehicleId(fullSN);
    }

    @Override
    public String toString() {
        return super.toString() + String.format(Locale.getDefault(), "\nTrip Odometer: %.1f\nEngine Hours: %.1f\nTrip Engine Seconds: %d\nVehicle ID: %s\nSpeed from Engine: %b\nOdo from Engine: %b",
                getTripOdometer(),
                getEngineHours(),
                (int) getTripEngineSeconds(),
                getVehicleId(),
                isSpeedFromEngine(),
                isOdometerFromEngine());
    }
}
