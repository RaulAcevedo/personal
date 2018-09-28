
package com.jjkeller.kmbapi.geotab.implementation.engine.dataEngine.scenarioManagers;

import com.jjkeller.kmbapi.HosMessageProcessor.interfaces.IHOSMessage;
import com.jjkeller.kmbapi.geotab.abstraction.data.ScenarioManagerBase;
import com.jjkeller.kmbapi.geotab.implementation.enums.ScenarioSelectorEnum;

/**
 * Public class that implements the DataValidatorBase class and validates using Approvals
 */
public class ApprovalScenarioManager extends ScenarioManagerBase {
    public ApprovalScenarioManager(ScenarioSelectorEnum validationType) {
        super(validationType);
    }

    @Override
    public void validate(IHOSMessage mapper) {

    }
}
