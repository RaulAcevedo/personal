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
public class MandateMovingNotDriving extends State {

    public MandateMovingNotDriving(SharedState sharedState) {
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

        // ignition turns off -> IGN_OFF -> Engine Off Not Driving
        Boolean ignitionOff = !message.isIgnitionOn();
        if(ignitionOff){

            EventRecord ignOffEventRecord = createIgnOffEventRecord(message);
            EventRecord moveStopEventRecord = createMoveStopEventRecord(message);

            result.getEventRecords().add(ignOffEventRecord);
            result.getEventRecords().add(moveStopEventRecord);

            return setStateAndRecurse(new MandateEngineOffNotDriving(sharedState), message, result);
        }

        if(!message.isSpeedFromEngine()){
            EventRecord moveStopRecord = createMoveStopEventRecord(message);

            result.getEventRecords().add(moveStopRecord);
            return setStateAndRecurse(new MandateVSSError(sharedState, this, message.getTimestampUtc().toDate()), message, result);

        }

        // speed = 0 -> STOP -> Stopped Not Driving
        if(message.getSpeedometer() < THRESHOLD_MPH_MOVE){

            EventRecord moveStopEventRecord = createMoveStopEventRecord(message);

            result.getEventRecords().add(moveStopEventRecord);

            return setStateAndRecurse(new MandateStoppedNotDriving(sharedState), message, result);
        }

        // speed >= 5 -> DRIVE_ON -> Moving Driving
        if(message.getSpeedometer() >= sharedState.getCurrentThresholds().getDriveStartSpeed()){

            EventRecord driveOnEventRecord = createDriveOnEventRecord(message, message);

            result.getEventRecords().add(driveOnEventRecord);

            return setStateAndRecurse(new MandateMovingDriving(sharedState), message, result);
        }

        // Default
        return result;
    }
}
