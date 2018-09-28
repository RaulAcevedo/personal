package com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states;

import com.jjkeller.kmbapi.HosMessageProcessor.interfaces.IHOSMessage;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.MessageResult;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.SharedState;
import com.jjkeller.kmbapi.kmbeobr.EventRecord;

/**
 * Created by ief5781 on 9/1/16.
 */
@SuppressWarnings("DefaultFileTemplate")
public class AobrdMovingDriving extends State {

    public AobrdMovingDriving(SharedState sharedState) {
        super(sharedState);

        // reset move start
        sharedState.setMoveStartMessage(null);
    }

    @Override
    public MessageResult processMessage(IHOSMessage message) {
        MessageResult result = new MessageResult();

        if(message == null)
            return new MessageResult();

        Boolean ignitionOff = !message.isIgnitionOn();
        if(ignitionOff){
            EventRecord ignOffRecord = createIgnOffEventRecord(message);
            EventRecord moveStopEventRecord = createMoveStopEventRecord(message);
            EventRecord driveOffEventRecord = createDriveOffEventRecord(message);

            result.getEventRecords().add(ignOffRecord);
            result.getEventRecords().add(moveStopEventRecord);
            result.getEventRecords().add(driveOffEventRecord);

            return setStateAndRecurse(new AobrdEngineOff(sharedState), message, result);
        }

        if(message.getSpeedometer() < THRESHOLD_MPH_MOVE) {
            EventRecord moveStopRecord = createMoveStopEventRecord(message);
            result.getEventRecords().add(moveStopRecord);

            return setStateAndRecurse(new AobrdStoppedDriving(sharedState, message.getTimestampUtc().toDate()), message, result);
        }

        return result;
    }
}