package com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states;

import android.util.EventLog;

import com.jjkeller.kmbapi.HosMessageProcessor.interfaces.IHOSMessage;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.MessageResult;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.SharedState;
import com.jjkeller.kmbapi.kmbeobr.EventRecord;

import java.util.Date;

/**
 * Created by rab5795 on 12/28/17.
 */

public class MandateVSSError extends State {

    private Date startTime;

    public MandateVSSError(SharedState sharedState, State lastState) {
        super(sharedState);
    }

    public MandateVSSError(SharedState sharedState, State lastState, Date stopTimeStamp) {
        super(sharedState);
        startTime = stopTimeStamp;
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

        if(message.isSpeedFromEngine()){
            if(message.isIgnitionOn()){
                if((sharedState.getIsInDriveOnState())){
                    if(message.getSpeedometer() >= THRESHOLD_MPH_MOVE) {
                        message.setTimestampUtc(message.getTimestampUtc().plusSeconds(1));
                        EventRecord moveStartEventRecord = createMoveStartEventRecord(message);
                        result.getEventRecords().add(moveStartEventRecord);
                        return setStateAndRecurse(new MandateMovingDriving(sharedState), message, result);
                    } else {
                        return setStateAndRecurse(new MandateStoppedDriving(sharedState, message.getTimestampUtc().toDate()), message, result);
                    }
                } else {
                    if(message.getSpeedometer() >= THRESHOLD_MPH_MOVE) {
                        message.setTimestampUtc(message.getTimestampUtc().plusSeconds(1));
                        EventRecord moveStartEventRecord = createMoveStartEventRecord(message);
                        result.getEventRecords().add(moveStartEventRecord);
                        return setStateAndRecurse(new MandateMovingNotDriving(sharedState), message, result);
                    } else {
                        return setStateAndRecurse(new MandateStoppedNotDriving(sharedState), message, result);
                    }
                }
            } else {
                if(sharedState.getIsInDriveOnState()) {
                    return setStateAndRecurse(new MandateEngineOffDriving(sharedState, message.getTimestampUtc().toDate()), message, result);
                } else {
                    return setStateAndRecurse(new MandateEngineOffNotDriving(sharedState), message, result);
                }
            }
        }

        Boolean ignitionOff = !message.isIgnitionOn();
        if(ignitionOff){
            EventRecord ignOffEventRecord = createIgnOffEventRecord(message);
            EventRecord clearErrorEvent = createClearErrorRecordOnIgnOff(message);
            result.getEventRecords().add(clearErrorEvent);
            result.getEventRecords().add(ignOffEventRecord);

            return setStateAndRecurse(new MandateEngineOffDriving(sharedState, message.getTimestampUtc().toDate()), message, result);
        }

        if(startTime != null){
            // remain stopped for X minutes -> DRIVE_OFF -> Engine Off Not Driving
            long millisecondsSinceDriveStart = message.getTimestampUtc().getMillis() - startTime.getTime();
            int minutesSinceInitialStop = ((Double)(((double)millisecondsSinceDriveStart / 1000) / 60)).intValue();
            if(sharedState.getIsInDriveOnState() && minutesSinceInitialStop >= sharedState.getCurrentThresholds().getDriveStopTime())
            {
                EventRecord driverOffEventRecord = createDriveOffEventRecord(message, startTime.getTime());
                result.getEventRecords().add(driverOffEventRecord);
            }
        }

        return result;
    }
}
