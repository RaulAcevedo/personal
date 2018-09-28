package com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states;

import com.jjkeller.kmbapi.HosMessageProcessor.interfaces.IHOSMessage;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.MessageResult;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.SharedState;
import com.jjkeller.kmbapi.kmbeobr.EventRecord;

import java.util.List;

/**
 * Created by ief5781 on 9/1/16.
 */
@SuppressWarnings("DefaultFileTemplate")
public class MandateEngineOffNotDriving extends State {

    public MandateEngineOffNotDriving(SharedState sharedState) {
        super(sharedState);
    }

    @Override
    public MessageResult processMessage(IHOSMessage message) {

        MessageResult result = new MessageResult();

        if(message == null)
            return new MessageResult();

        //Create Error Event
        EventRecord errorRecord = createErrorRecord(message);
        if(errorRecord != null){
            result.getEventRecords().add(errorRecord);
        }

        if(message.isGpsValid())
            sharedState.setValidGpsFixMessage(message);

        calculateGpsUncertDistance(message);
        List<EventRecord> errorResult = sharedState.getGpsErrorState().processMessage(message);
        if(errorResult != null)
            result.getEventRecords().addAll(errorResult);

        if(message.isIgnitionOn()){
            EventRecord ignOnRecord = createIgnOnEventRecord(message);
            EventRecord stopRecord = createMoveStopEventRecord(message);

            result.getEventRecords().add(ignOnRecord);
            result.getEventRecords().add(stopRecord);

            return setStateAndRecurse(new MandateStoppedNotDriving(sharedState), message, result);
        }

        // Default
        return result;
    }
}
