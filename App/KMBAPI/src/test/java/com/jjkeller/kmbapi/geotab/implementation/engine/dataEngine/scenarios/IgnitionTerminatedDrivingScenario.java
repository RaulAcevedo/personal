package com.jjkeller.kmbapi.geotab.implementation.engine.dataEngine.scenarios;

import com.jjkeller.kmbapi.HosMessageProcessor.interfaces.IHOSMessage;
import com.jjkeller.kmbapi.geotab.abstraction.data.ScenarioBase;
import com.jjkeller.kmbapi.geotab.implementation.data.ScenarioSettings;
import com.jjkeller.kmbapi.geotab.implementation.engine.dataEngine.ScenarioComponentBuilder;

import java.util.ArrayList;

/**
 * Scenario that implements ScenarioBase for a Driving Cycle in which the ignition is terminated
 * during movement
 */
public class IgnitionTerminatedDrivingScenario extends ScenarioBase {

    public IgnitionTerminatedDrivingScenario(ScenarioSettings settings) {
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
                .chain()
                .generateMessagesForDuration(300)
                .withVSSChangeOverAggregate(60)
                .synthesizeComponent()
                .chain()
                .generateMessagesForDuration(10)
                .withIgnitionState(false)
                .withConstantVSS(0)
                .synthesizeComponent()
                .chain()
                .generateNumberOfMessages(1)
                .withIgnitionState(true)
                .withConstantVSS(0)
                .synthesizeComponent()
                .chain()
                .generateMessagesForDuration(60)
                .withIgnitionState(true)
                .withConstantVSS(60)
                .synthesizeComponent()
                .chain()
                .generateMessagesForDuration(300)
                .withConstantVSS(60)
                .synthesizeComponent()
                .chain()
                .generateNumberOfMessages(1)
                .withIgnitionState(false)
                .withConstantVSS(0)
                .synthesizeComponent()
                .chain()
                .generateMessagesForDuration(300)
                .withConstantVSS(0)
                .synthesizeComponent()
                .chain()
                .generateMessagesForDuration(120)
                .withIgnitionState(true)
                .withConstantVSS(60)
                .synthesizeComponent();
        return new ArrayList<>(componentBuilder.getComponentData());
    }

    @Override
    protected boolean validate(IHOSMessage dataMapper) {
        return true;
    }

    @Override
    protected String emitInnerScenarioDescription() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("This is an implementation of ScenarioBase for a Driving Cycle where the ignition is turned off after getting a DRIVE_ON event.")
                .append("\n")
                .append("The user will accelerate to 60 MPH, and drive for 5 minutes, initiating a MOVE, and then a DRIVE_ON.")
                .append("\n")
                .append("While in DRIVE_ON, we will terminate the ignition (generate IGN_OFF).")
                .append("\n")
                .append("This will put us into a DRIVE_OFF state, and then immediately issue a VEHICLE_STOPPED event.")
                .append("\n")
                .append("The first pass will then immediately accelerate back up to 60 MPH, triggering a MOVE event followed immediately by a DRIVE_ON event.")
                .append("\n")
                .append("\n")
                .append("The second pass will terminate the ignition again, and the expectations remain the same up to the point we receive the VEHICLESTOPPED event.")
                .append("\n")
                .append("We will sit idle for 5 min, resetting the drive start timer. After that time, we will accelerate again to 60 MPH, and receive the MOVE and DRIVE_ON events again.")
                .append("\n")
                .append("HOWEVER, the timestamp for the DRIVE_ON event will be delayed from that of the MOVE (not the case in the first test) due to the timer reset.");
        return stringBuilder.toString();
    }

    @Override
    public ScenarioBase cleanup(Object... args) {
        return this;
    }
}
