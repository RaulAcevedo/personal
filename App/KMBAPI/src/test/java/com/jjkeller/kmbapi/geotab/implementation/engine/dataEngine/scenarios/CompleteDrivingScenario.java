package com.jjkeller.kmbapi.geotab.implementation.engine.dataEngine.scenarios;

import com.jjkeller.kmbapi.HosMessageProcessor.interfaces.IHOSMessage;
import com.jjkeller.kmbapi.geotab.implementation.data.ScenarioSettings;
import com.jjkeller.kmbapi.geotab.abstraction.data.ScenarioBase;
import com.jjkeller.kmbapi.geotab.implementation.engine.dataEngine.ScenarioComponentBuilder;

import java.util.ArrayList;

/**
 * Created by bja6001 on 10/11/16.
 */

public class CompleteDrivingScenario extends ScenarioBase {
    public CompleteDrivingScenario(ScenarioSettings settings) {
        super(settings);
    }

    @Override
    protected ScenarioBase continueGeneration(Object... args) {
        if (!this._hasSatisfiedScenarioRequirements.compareAndSet(false, true)) {
            //The satisfied requirements flag has been tripped already and we weren't aware of
            //it, throw an exception
            throw new IllegalStateException("The scenario has marked it's generation process complete, but internal validation is not in sync.");
        }
        return this;
    }

    @Override
    protected ArrayList<IHOSMessage> synthesize(Object... args) {
        ScenarioComponentBuilder componentBuilder = ScenarioComponentBuilder.getComponentBuilder(this._settings)
                .withInitialMessage(this.CurrentIHOSMessage)
                .initializeMessageGeneration()
                .generateNumberOfMessages(1)
                .withConstantVSS(0)
                .withIgnitionState(true)
                .synthesizeComponent()
                .chain()
                .generateMessagesForDuration(60)
                .withConstantVSS(0)
                .synthesizeComponent()
                .chain()
                .generateMessagesForDuration(600)
                .withConstantVSS(4)
                .synthesizeComponent()
                .chain()
                .generateMessagesForDuration(60)
                .withConstantVSS(0)
                .synthesizeComponent()
                .chain()
                .generateMessagesForDuration(240)
                .withConstantVSS(60)
                .synthesizeComponent()
                .chain()
                .generateMessagesForDuration(60)
                .withConstantVSS(0)
                .synthesizeComponent()
                .chain()
                .generateMessagesForDuration(240)
                .withConstantVSS(60)
                .synthesizeComponent()
                .chain()
                .generateMessagesForDuration(330)
                .withConstantVSS(0)
                .synthesizeComponent()
                .chain()
                .generateNumberOfMessages(1)
                .withConstantVSS(0)
                .withIgnitionState(false)
                .synthesizeComponent()
                .chain()
                .generateMessagesForDuration(200)
                .withConstantVSS(0)
                .synthesizeComponent();
        return new ArrayList<>(componentBuilder.getComponentData());
    }

    @Override
    protected boolean validate(IHOSMessage dataMapper) {
        return true;
    }

    @Override
    public ScenarioBase cleanup(Object... args) {
        return this;
    }

    @Override
    protected String emitInnerScenarioDescription() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("This is an implementation of ScenarioBase for a Complete Driving Cycle.");
        return stringBuilder.toString();
    }
}
