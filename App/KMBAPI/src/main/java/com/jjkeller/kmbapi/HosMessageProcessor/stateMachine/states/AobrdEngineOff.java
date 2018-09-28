package com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states;

import com.jjkeller.kmbapi.HosMessageProcessor.interfaces.IHOSMessage;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.MessageResult;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.SharedState;
import com.jjkeller.kmbapi.kmbeobr.EventRecord;

/**
 * Created by ief5781 on 9/1/16.
 */
@SuppressWarnings("DefaultFileTemplate")
public class AobrdEngineOff extends State {

    public AobrdEngineOff(SharedState sharedState) {
        super(sharedState);
    }

    @Override
    public MessageResult processMessage(IHOSMessage message) {
        MessageResult result = new MessageResult();

        if(message == null)
            return result;

        if(message.isIgnitionOn() && message.getSpeedometer() < THRESHOLD_MPH_MOVE){
            EventRecord ignOnRecord = createIgnOnEventRecord(message);
            EventRecord stopRecord = createMoveStopEventRecord(message);
            result.getEventRecords().add(ignOnRecord);
            result.getEventRecords().add(stopRecord);

           return setStateAndRecurse(new AobrdStoppedNotDriving(sharedState), message, result);
        }

        if(message.isIgnitionOn() && message.getSpeedometer() >= THRESHOLD_MPH_MOVE){

            EventRecord ignOnRecord = createIgnOnEventRecord(message);
            EventRecord stopRecord = createMoveStopEventRecord(message);
            EventRecord moveStartRecord = createMoveStartEventRecord(message);
            result.getEventRecords().add(ignOnRecord);
            result.getEventRecords().add(stopRecord);
            result.getEventRecords().add(moveStartRecord);

            return setStateAndRecurse(new AobrdMovingNotDriving(sharedState), message, result);
        }

        return result;
    }

}

