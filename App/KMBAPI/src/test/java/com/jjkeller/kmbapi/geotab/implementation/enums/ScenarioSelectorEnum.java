package com.jjkeller.kmbapi.geotab.implementation.enums;

/**
 * Public enum that represents a type of validation for a given piece of data.
 */
public enum ScenarioSelectorEnum {
    /**
     * Default enum value. Will not evaluate anything
     */
    NONE,
    /**
     * Allows the definition of a custom operation
     */
    CUSTOM,
    /**
     * Defines a simple driving cycle
     */
    DRIVING_CYCLE_SIMPLE,
    /**
     * Defines a normal driving cycle
     */
    DRIVING_CYCLE_NORMAL,
    /**
     * Defines a complete driving cycle
     */
    DRIVING_CYCLE_COMPLETE,
    /**
     * Defines a driving cycle where the ignition is switched off
     */
    DRIVING_CYCLE_IGN_TERMINATED,
    /**
     * Defines a driving cycle where the driver changes causing a termination
     */
    DRIVING_CYCLE_DRIVER_CHANGE_TERMINATED,
    /**
     * Defines behavior where start cycle is interrupted
     */
    START_CYCLE_INTERRUPTED,
    /**
     * Defines behavior where end cycle is interrupted
     */
    END_CYCLE_INTERRUPTED
}
