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
public class MandateStoppedNotDriving extends State {

    public MandateStoppedNotDriving(SharedState sharedState) {
        super(sharedState);
    }

    @Override
    public MessageResult processMessage(IHOSMessage message) {
        MessageResult result = new MessageResult();

        if(message == null)
            return new MessageResult();

        if(message.isGpsValid())
            sharedState.setValidGpsFixMessage(message);

        calculateGpsUncertDistance(message);
        List<EventRecord> errorResult = sharedState.getGpsErrorState().processMessage(message);
        if(errorResult != null)
            result.getEventRecords().addAll(errorResult);


        //Create Error Event
        EventRecord errorRecord = createErrorRecord(message);
        if(errorRecord != null){
            result.getEventRecords().add(errorRecord);
        }

        // ignition turns off -> IGN_OFF-> Engine Off Not Driving
        Boolean ignitionOff = !message.isIgnitionOn();
        if(ignitionOff){
            EventRecord ignOffEventRecord = createIgnOffEventRecord(message);

            result.getEventRecords().add(ignOffEventRecord);

            result.setNewState(new MandateEngineOffNotDriving(sharedState));
            result.getEventRecords().addAll(result.getNewState().processMessage(message).getEventRecords());
            return result;
        }


        if(!message.isSpeedFromEngine()){
            return setStateAndRecurse(new MandateVSSError(sharedState, this), message, result);
        }

        // speed > 0 -> MOVE -> Moving Not Driving
        if(message.getSpeedometer() > THRESHOLD_MPH_MOVE && message.getSpeedometer() < sharedState.getCurrentThresholds().getDriveStartSpeed()) {

            EventRecord moveStartEventRecord = createMoveStartEventRecord(message);

            result.getEventRecords().add(moveStartEventRecord);

            return setStateAndRecurse(new MandateMovingNotDriving(sharedState), message, result);
        }

        // speed > 5 -> DRIVE_ON, MOVE -> Moving Driving
        if(message.getSpeedometer() >= sharedState.getCurrentThresholds().getDriveStartSpeed()) {

            EventRecord moveStartEventRecord = createMoveStartEventRecord(message);
            EventRecord driveOnEventRecord = createDriveOnEventRecord(message, message);

            result.getEventRecords().add(moveStartEventRecord);
            result.getEventRecords().add(driveOnEventRecord);

            return setStateAndRecurse(new MandateMovingDriving(sharedState), message, result);
        }

        // Default
        return result;
    }

}
