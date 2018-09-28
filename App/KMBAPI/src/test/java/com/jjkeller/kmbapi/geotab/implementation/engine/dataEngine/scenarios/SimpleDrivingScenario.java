package com.jjkeller.kmbapi.geotab.implementation.engine.dataEngine.scenarios;

import com.jjkeller.kmbapi.HosMessageProcessor.interfaces.IHOSMessage;
import com.jjkeller.kmbapi.geotab.implementation.data.ScenarioSettings;
import com.jjkeller.kmbapi.geotab.abstraction.data.ScenarioBase;

import java.util.ArrayList;

/**
 * Created by bja6001 on 10/11/16.
 */

public class SimpleDrivingScenario extends ScenarioBase {
    public SimpleDrivingScenario(ScenarioSettings settings) {
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
        return null;
    }
}
