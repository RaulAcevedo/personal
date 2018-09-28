package com.jjkeller.kmbapi.geotab.implementation.engine.dataEngine.scenarioManagers;

import com.jjkeller.kmbapi.HosMessageProcessor.interfaces.IHOSMessage;
import com.jjkeller.kmbapi.geotab.abstraction.data.ScenarioManagerBase;
import com.jjkeller.kmbapi.geotab.implementation.enums.ScenarioSelectorEnum;

/**
 * Public class that implements the DataValidatorBase class and validates using traditional
 * Assert logic
 */
public class BasicScenarioManager extends ScenarioManagerBase {

    public BasicScenarioManager(ScenarioSelectorEnum validationType) {
        super(validationType);
    }

    @Override
    public void validate(IHOSMessage mapper) {

    }
}
