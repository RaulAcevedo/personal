package com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states;

import com.jjkeller.kmbapi.HosMessageProcessor.interfaces.IHOSMessage;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.MessageResult;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.SharedState;
import com.jjkeller.kmbapi.kmbeobr.EventRecord;

/**
 * Created by ief5781 on 9/1/16.
 */
@SuppressWarnings("DefaultFileTemplate")
public class AobrdMovingNotDriving extends State {
    public AobrdMovingNotDriving(SharedState sharedState) {
        super(sharedState);
    }

    @Override
    public MessageResult processMessage(IHOSMessage message) {
        MessageResult result = new MessageResult();

        if(message == null)
            return new MessageResult();


        if( sharedState.getMoveStartMessage() == null) {
            sharedState.setMoveStartMessage(message);
        }

        Boolean ignitionOff = !message.isIgnitionOn();
        if(ignitionOff){
            EventRecord ignOffRecord = createIgnOffEventRecord(message);
            EventRecord moveStopEventRecord = createMoveStopEventRecord(message);

            result.getEventRecords().add(ignOffRecord);
            result.getEventRecords().add(moveStopEventRecord);

            return setStateAndRecurse(new AobrdEngineOff(sharedState), message, result);
        }

        if(message.getSpeedometer() < THRESHOLD_MPH_MOVE) {

            EventRecord moveStopRecord = createMoveStopEventRecord(message);

            result.getEventRecords().add(moveStopRecord);

            return setStateAndRecurse(new AobrdStoppedNotDriving(sharedState), message, result);
        }

        float odometerChange = message.getOdometer() - sharedState.getMoveStartMessage().getOdometer();
        if (odometerChange > sharedState.getCurrentThresholds().getDriveStartDistance()) {
            EventRecord driveOnEventRecord = createDriveOnEventRecord(message, sharedState.getMoveStartMessage());

            result.getEventRecords().add(driveOnEventRecord);

            return setStateAndRecurse(new AobrdMovingDriving(sharedState), message, result);
        }

        return result;
    }
}
