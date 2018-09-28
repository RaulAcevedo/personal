package com.jjkeller.kmbapi.geotab.implementation.engine.dataEngine.scenarios;

import com.jjkeller.kmbapi.HosMessageProcessor.interfaces.IHOSMessage;
import com.jjkeller.kmbapi.geotab.abstraction.data.ScenarioBase;
import com.jjkeller.kmbapi.geotab.implementation.data.ScenarioSettings;
import com.jjkeller.kmbapi.geotab.implementation.engine.dataEngine.ScenarioComponentBuilder;

import java.util.ArrayList;

/**
 * Scenario that implements ScenarioBase for a Driving Cycle in which the start of the cycle
 * is interrupted
 */
public class InterruptedStartOfDriveCycleScenario extends ScenarioBase {

    public InterruptedStartOfDriveCycleScenario(ScenarioSettings settings) {
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
                .generateMessagesForDuration(60)
                .withConstantVSS(0)
                .synthesizeComponent()
                .chainForMultiStep()
                .generateNumberOfMessages(1)
                .withConstantVSS(3)
                .step()
                .generateMessagesForDuration(60)
                .withConstantVSS(3)
                .step()
                .generateMessagesForDuration(6)
                .withVSSChangeOverAggregate(0)
                .step()
                .generateMessagesForDuration(10)
                .withConstantVSS(0)
                .step()
                .generateNumberOfMessages(1)
                .withVSSChangeOverAggregate(3)
                .step()
                .generateNumberOfMessages(1)
                .withConstantVSS(3)
                .step()
                .generateMessagesForDuration(60)
                .withConstantVSS(3)
                .step()
                .generateMessagesForDuration(6)
                .withVSSChangeOverAggregate(0)
                .step()
                .generateMessagesForDuration(10)
                .withConstantVSS(0)
                .step()
                .generateNumberOfMessages(1)
                .withVSSChangeOverAggregate(3)
                .step()
                .generateNumberOfMessages(1)
                .withConstantVSS(3)
                .step()
                .generateMessagesForDuration(60)
                .withConstantVSS(3)
                .step()
                .generateMessagesForDuration(6)
                .withVSSChangeOverAggregate(0)
                .step()
                .generateMessagesForDuration(10)
                .withConstantVSS(0)
                .step()
                .generateNumberOfMessages(1)
                .withVSSChangeOverAggregate(3)
                .step()
                .generateNumberOfMessages(1)
                .withConstantVSS(3)
                .step()
                .generateMessagesForDuration(60)
                .withConstantVSS(3)
                .step()
                .generateMessagesForDuration(6)
                .withVSSChangeOverAggregate(0)
                .step()
                .generateMessagesForDuration(10)
                .withConstantVSS(0)
                .step()
                .generateNumberOfMessages(1)
                .withVSSChangeOverAggregate(3)
                .synthesizeComponent()
                .chain()
                .generateMessagesForDuration(60)
                .withVSSChangeOverAggregate(60)
                .synthesizeComponent()
                .chain()
                .generateMessagesForDuration(60)
                .withVSSChangeOverAggregate(0)
                .synthesizeComponent()
                .chain()
                .generateMessagesForDuration(300)
                .withConstantVSS(0)
                .synthesizeComponent()
                .chain()
                .generateMessagesForDuration(60)
                .withVSSChangeOverAggregate(45)
                .synthesizeComponent()
                .chain();
        return new ArrayList<>(componentBuilder.getComponentData());
    }

    @Override
    protected String emitInnerScenarioDescription() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("This is an implementation of ScenarioBase for a Driving Cycle ")
                .append("in which a user moves and stops multiple times, but does NOT cross ")
                .append("the DRIVE_ON threshold.")
                .append("\n")
                .append("After starting the engine, we accelerate to three (3) MPH.")
                .append("\n")
                .append("The user then moves for one minute at that speed. Following that, the user ")
                .append("decelerates to 0 MPH over 6 seconds, and stays stopped for 10 seconds.")
                .append("\n")
                .append("The user then accelerates back up to 3 MPH. This loop is repeated 4 times.")
                .append("\n")
                .append("After the 4th time, we accelerate to 60 MPH over one minute, with the expectation ")
                .append("that we will receive an immediate DRIVE_ON.")
                .append("\n")
                .append("Following that, we will decelerate down to 0 and wait 5 min to trigger a DRIVE_OFF event.")
                .append("\n")
                .append("We then start driving back to the shop, but expect that due to the Drive Off, we get a MOVE without an immediate DRIVE_ON.");
        return stringBuilder.toString();
    }

    @Override
    protected boolean validate(IHOSMessage dataMapper) {
        return true;
    }

    @Override
    public ScenarioBase cleanup(Object... args) {
        return this;
    }
}
