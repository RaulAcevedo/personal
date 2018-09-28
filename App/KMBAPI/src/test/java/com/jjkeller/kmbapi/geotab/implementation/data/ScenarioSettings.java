package com.jjkeller.kmbapi.geotab.implementation.data;

import com.jjkeller.kmbapi.HosMessageProcessor.interfaces.IHOSMessage;

import org.joda.time.DateTime;

import java.util.Calendar;
import java.util.Date;

/**
 * Internal public class allowing a user to provide arguments for values that will be used
 * to generate a given scenario
 */
public class ScenarioSettings {

    /*
    Constant field that represents the default polling time to simulate
     */
    private static final float DEFAULT_POLLING_TIME = 2.0f;

    /**
     * Field representing the interval that the scenario will simulate receiving/sending
     * data
     */
    public final float SimulatedPollingTime;

    /**
     * The following block of fields represent initial values that will be used as starting
     * values for the scenario
     */
    public float InitialLat;
    public float InitialLon;
    public float InitialSpeed;
    public float InitialTach;
    public float InitialOdometer;
    public boolean isGpsLatchedInitial;
    public boolean isIgnOnInitial;
    public boolean hasEngineDataInitial;
    public boolean isDateTimeValidInitial;
    public DateTime InitialDateTime;

    public ScenarioSettings(Float simulatedPollingTime) {
        this(null, simulatedPollingTime);
    }


    /**
     * Public constructor that accepts an IHOSMessage and uses it's values to create the initial
     * event for a scenario
     *
     * @param messageForInitialValues IHOSMessage with values to use
     */
    public ScenarioSettings(IHOSMessage messageForInitialValues, Float simulatedPollingTime) {
        //Set defaults for Manager settings

        //If no value is provided, or value is invalid, default to 2 seconds
        if (simulatedPollingTime == null || simulatedPollingTime <= 0)
            simulatedPollingTime = DEFAULT_POLLING_TIME;

        this.determineInitialValues(messageForInitialValues);
        this.SimulatedPollingTime = simulatedPollingTime;
    }

    /**
     * Private method that sets the initial values for a settings object from a provided IHOSMessage
     * or from default values if the message is null
     *
     * @param messageForInitialValues IHOSMessage to pull initial data from
     */
    private void determineInitialValues(IHOSMessage messageForInitialValues) {
        //Defaults
        float _initialLat = 0.0f;
        float _initialLon = 0.0f;
        float _initialSpeed = 0.0f;
        float _initialTach = 5.0f;
        float _initialOdometer = 0.0f;
        DateTime _initialDateTime = DateTime.now();
        boolean _isGpsLatchedInitial = false;
        boolean _isIgnOnInitial = false;
        boolean _hasEngineDataInitial = true;
        boolean _isDateTimeValidInitial = true;
        //Determine initial IHOSMessage values
        if (messageForInitialValues != null) {
            _initialLat = messageForInitialValues.getGpsLatitude();
            _initialLon = messageForInitialValues.getGpsLongitude();
            _initialSpeed = messageForInitialValues.getSpeedometer();
            _initialTach = messageForInitialValues.getTachometer();
            _initialOdometer = messageForInitialValues.getOdometer();
            _isGpsLatchedInitial = messageForInitialValues.isGpsValid();
            _isIgnOnInitial = messageForInitialValues.isIgnitionOn();
            _hasEngineDataInitial = messageForInitialValues.isEngineActivityDetected();
            _isDateTimeValidInitial = messageForInitialValues.isDatetimeValid();
            _initialDateTime = messageForInitialValues.getTimestampUtc();
        }

        this.hasEngineDataInitial = _hasEngineDataInitial;
        this.isDateTimeValidInitial = _isDateTimeValidInitial;
        this.isIgnOnInitial = _isIgnOnInitial;
        this.isGpsLatchedInitial = _isGpsLatchedInitial;
        this.InitialOdometer = _initialOdometer;
        this.InitialTach = _initialTach;
        this.InitialSpeed = _initialSpeed;
        this.InitialLon = _initialLon;
        this.InitialLat = _initialLat;
        this.InitialDateTime = _initialDateTime;
    }

    @Override
    public String toString() {
        @SuppressWarnings("StringBufferReplaceableByString")
        final StringBuilder sb = new StringBuilder()
                .append("\n")
                .append("SimulatedPollingTime: ").append(SimulatedPollingTime)
                .append("\n")
                .append("InitialLat: ").append(InitialLat)
                .append("\n")
                .append("InitialLon: ").append(InitialLon)
                .append("\n")
                .append("InitialSpeed: ").append(InitialSpeed)
                .append("\n")
                .append("InitialTach: ").append(InitialTach)
                .append("\n")
                .append("InitialOdometer: ").append(InitialOdometer)
                .append("\n")
                .append("isGpsLatchedInitial: ").append(isGpsLatchedInitial)
                .append("\n")
                .append("isIgnOnInitial: ").append(isIgnOnInitial)
                .append("\n")
                .append("hasEngineDataInitial: ").append(hasEngineDataInitial)
                .append("\n")
                .append("isDateTimeValidInitial: ").append(isDateTimeValidInitial)
                .append("\n")
                .append("InitialDateTime: ").append(InitialDateTime)
                .append("\n");
        return sb.toString();
    }

    /**
     * Enum representing how values will be changed during scenario generation. E.g. how large
     * will the jumps in data be
     */
    public enum ScenarioValueChangePersonality {

        /**
         * Represents a scenario generation instance that makes small jumps in values
         */
        NORMAL
    }

}
