package com.jjkeller.kmbapi.geotab.implementation.engine.dataEngine.scenarios;

import com.jjkeller.kmbapi.HosMessageProcessor.interfaces.IHOSMessage;
import com.jjkeller.kmbapi.geotab.abstraction.data.ScenarioBase;
import com.jjkeller.kmbapi.geotab.implementation.data.ScenarioSettings;
import com.jjkeller.kmbapi.geotab.implementation.engine.dataEngine.ScenarioComponentBuilder;

import java.util.ArrayList;


/**
 * Class that implements ScenarioBase for a Normal driving scenario
 */
public class NormalDrivingScenario extends ScenarioBase {

    public NormalDrivingScenario(ScenarioSettings settings) {
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
        ArrayList<IHOSMessage> returnList;

        ScenarioComponentBuilder componentBuilder = ScenarioComponentBuilder.getComponentBuilder(this._settings)
                .withInitialMessage(this.CurrentIHOSMessage)
                .initializeMessageGeneration()
                .generateMessagesForDuration(60)
                .withConstantVSS(0)
                .synthesizeComponent()
                .chain()
                .generateMessagesForDuration(60)
                .withVSSChangeOverAggregate(30)
                .synthesizeComponent()
                .chain()
                .generateMessagesForDuration(60)
                .withVSSChangeOverAggregate(0)
                .synthesizeComponent().chain()
                .generateMessagesForDuration(120)
                .withVSSChangeOverAggregate(60)
                .synthesizeComponent().chain()
                .generateMessagesForDuration(300)
                .withConstantVSS(60)
                .synthesizeComponent().chain()
                .generateMessagesForDuration(120)
                .withVSSChangeOverAggregate(0)
                .synthesizeComponent().chain()
                .generateMessagesForDuration(300)
                .withConstantVSS(0)
                .synthesizeComponent();
        returnList = componentBuilder.getComponentData();
        return returnList;
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
        StringBuilder normalScenarioSB = new StringBuilder();
        normalScenarioSB.append("This is an implementation of ScenarioBase for a Normal Driving Cycle Scenario.").append("\n");
        normalScenarioSB.append("In this scenario, we will start with ignition off, and then turn on the ignition.").append("\n");
        normalScenarioSB.append("Following that, we will sit idle with engine on for one minute (60 seconds).").append("\n");
        normalScenarioSB.append("We will then accelerate to 30 MPH, and immediately decelerate back down to 0, triggering a MOVESTOP event.").append("\n");
        normalScenarioSB.append("After reaching a stopped state, we then accelerate up to 60 MPH over the course of two minutes (120 seconds).").append("\n");
        normalScenarioSB.append("We then drive for 5 minutes (300 seconds). Following that, we decelerate back down to 0 MPH over 2 minutes (120 seconds).").append("\n");
        normalScenarioSB.append("After reaching 0 MPH, we sit idle for 5 minutes (300 seconds).").append("\n");
        return normalScenarioSB.toString();
    }
}
