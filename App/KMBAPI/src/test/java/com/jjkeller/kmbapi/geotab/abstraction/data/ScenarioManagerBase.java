package com.jjkeller.kmbapi.geotab.abstraction.data;

import com.jjkeller.kmbapi.HosMessageProcessor.interfaces.IHOSMessage;
import com.jjkeller.kmbapi.geotab.implementation.data.ScenarioSettings;
import com.jjkeller.kmbapi.geotab.implementation.engine.dataEngine.scenarios.NoOperationScenario;
import com.jjkeller.kmbapi.geotab.implementation.engine.dataEngine.scenarios.NormalDrivingScenario;
import com.jjkeller.kmbapi.geotab.implementation.engine.dataEngine.scenarios.CompleteDrivingScenario;
import com.jjkeller.kmbapi.geotab.implementation.engine.dataEngine.scenarios.IgnitionTerminatedDrivingScenario;
import com.jjkeller.kmbapi.geotab.implementation.engine.dataEngine.scenarios.InterruptedStartOfDriveCycleScenario;
import com.jjkeller.kmbapi.geotab.implementation.engine.dataEngine.scenarios.SimpleDrivingScenario;
import com.jjkeller.kmbapi.geotab.implementation.enums.ScenarioSelectorEnum;

import java.util.EnumSet;

/**
 * Public class representing validity information for an IHOSMessage and/or it's corresponding
 * raw data
 */
public abstract class ScenarioManagerBase {

    /**
     * Field that represents the type of validation that should occur
     * Default to NONE (No validation)
     */
    private ScenarioSelectorEnum _scenarioSelector = ScenarioSelectorEnum.NONE;

    private ScenarioBase _scenario;

    /**
     * Constructor for operation
     *
     * @param validationType Enum representing the type of validation used in this operation
     */
    public ScenarioManagerBase(ScenarioSelectorEnum validationType) {
        this._scenarioSelector = validationType == null ? ScenarioSelectorEnum.NONE : validationType;
    }

    /**
     * Instance method that returns the current scenario represented as an enum that the
     * object is creating
     *
     * @return DataValidationTypeTarget enum
     */
    public ScenarioSelectorEnum getCurrentScenarioType() {
        return this._scenarioSelector;
    }

    /**
     * Instance method that returns the current scenario object the manager is using
     *
     * @return ScenarioBase object
     */
    public ScenarioBase getCurrentScenario() {
        if (this._scenario == null) {
            throw new IllegalStateException("Current Manager object does not have a scenario object. Did you initialize the scenario?");
        }
        return this._scenario;
    }

    /**
     * Public instance method that returns a scenario's description, optionally returning a dump
     * of the message content
     *
     * @param includeGeneratedMessageData Boolean representing if we would like a dump of messages
     *                                    currently present in the scenario
     * @return Returns a String representing the scenario data
     */
    public String getCurrentScenarioDescription(boolean includeGeneratedMessageData) {
        if (this._scenario == null) {
            throw new IllegalStateException("Current Manager object does not have a scenario object. Did you initialize the scenario?");
        }
        return this._scenario.getScenarioDescription(includeGeneratedMessageData);
    }

    /**
     * Instance method that sets the current validation strategy to be used
     *
     * @param strategy DataValidationTypeTarget enum defining the type of validation to use
     */
    public void setCurrentValidationStrategy(ScenarioSelectorEnum strategy) {
        this._scenarioSelector = strategy;
    }

    /**
     * Instance method that validates a given data mapper object against a given scenario
     * @param mapper DataMapper object to process
     */
    public boolean validateAgainstCurrentScenario(IHOSMessage mapper) {
        if (this._scenario == null) {
            throw new IllegalStateException("Current Manager object does not have a scenario object. Can not validateAgainstCurrentScenario data.");
        }
        return this._scenario.validate(mapper);
    }

    /**
     * Public method that initializes a scenario using the provided settings
     *
     * @param settings Settings to pass to scenario
     * @return ScenarioManagerBase with an initialized scenario
     */
    public ScenarioManagerBase initializeScenario(ScenarioSettings settings) {
        if (settings == null) {
            throw new IllegalArgumentException("ScenarioSettings object is null, can not create scenario.");
        }
        EnumSet scenarioEnums = EnumSet.allOf(ScenarioSelectorEnum.class);
        if (this._scenarioSelector == null || !scenarioEnums.contains(this._scenarioSelector)) {
            throw new IllegalArgumentException("The scenario provided to the data operation object was null or not a valid target.");
        }

        //TODO: Implement class that maps enums to Scenario to avoid having to futz with this
        switch (this._scenarioSelector) {
            //Do nothing, just return empty
            case NONE:
                this._scenario = new NoOperationScenario(settings);
            case DRIVING_CYCLE_SIMPLE:
                this._scenario = new SimpleDrivingScenario(settings);
                break;
            case DRIVING_CYCLE_NORMAL:
                this._scenario = new NormalDrivingScenario(settings);
                break;
            case DRIVING_CYCLE_COMPLETE:
                this._scenario = new CompleteDrivingScenario(settings);
                break;
            case DRIVING_CYCLE_IGN_TERMINATED:
                this._scenario = new IgnitionTerminatedDrivingScenario(settings);
                break;
            case START_CYCLE_INTERRUPTED:
                this._scenario = new InterruptedStartOfDriveCycleScenario(settings);
                break;
            default:
                throw new UnsupportedOperationException("ScenarioSelectorEnum was not of correct type.");
        }
        return this;
    }

    //TODO: Implement method to pass in unit tests and assert against data

    /**
     * Instance method that generates a scenario from the given array of arguments in both Raw Data
     * and IHOSMessage format, and returns the amalgamation of those two processes
     *
     * @param args Arguments for creation of a scenario
     * @return DataMapper object with synthesized data describing a scenario
     */
    public ScenarioManagerBase generateScenario(Object... args) {
        //Ensure we have a valid scenario enum, or are doing nothing
        if (this._scenario == null) {
            throw new IllegalStateException("Current Manager object does not have a scenario object. Can not generate scenario.");
        }
        //start scenario generation
        try {
            this._scenario.createScenarioData(args);
        } finally {
            this._scenario.cleanup(args);
        }
        return this;
    }

    /**
     * Public abstract instance method that validates a given mapping
     *
     * @param mapper Mapper to validate
     */
    public abstract void validate(IHOSMessage mapper);
}
