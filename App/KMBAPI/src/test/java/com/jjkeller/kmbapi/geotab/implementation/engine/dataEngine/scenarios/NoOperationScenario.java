package com.jjkeller.kmbapi.geotab.implementation.engine.dataEngine.scenarios;

import com.jjkeller.kmbapi.HosMessageProcessor.interfaces.IHOSMessage;
import com.jjkeller.kmbapi.geotab.abstraction.data.ScenarioBase;
import com.jjkeller.kmbapi.geotab.implementation.data.ScenarioSettings;

import java.util.ArrayList;

/**
 * Scenario implementation that does nothing (used for default)
 */
public class NoOperationScenario extends ScenarioBase {
    public NoOperationScenario(ScenarioSettings settings) {
        super(settings);
    }

    @Override
    protected ScenarioBase continueGeneration(Object... args) {
        return null;
    }

    @Override
    protected ArrayList<IHOSMessage> synthesize(Object... args) {
        return null;
    }

    @Override
    protected boolean validate(IHOSMessage dataMapper) {
        return false;
    }

    @Override
    public ScenarioBase cleanup(Object... args) {
        return null;
    }

    @Override
    protected String emitInnerScenarioDescription() {
        return "This scenario does literally nothing.";
    }
}
