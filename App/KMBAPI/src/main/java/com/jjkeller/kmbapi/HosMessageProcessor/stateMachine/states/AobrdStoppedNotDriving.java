package com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states;

import com.jjkeller.kmbapi.HosMessageProcessor.interfaces.IHOSMessage;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.MessageResult;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.SharedState;
import com.jjkeller.kmbapi.kmbeobr.EventRecord;

import java.util.Date;

/**
 * Created by ief5781 on 9/1/16.
 */
@SuppressWarnings("DefaultFileTemplate")
public class AobrdStoppedNotDriving extends State {

    private Date stopTime = new Date(0);

    public AobrdStoppedNotDriving(SharedState sharedState) {
        super(sharedState);
    }

    @Override
    public MessageResult processMessage(IHOSMessage message) {
        MessageResult result = new MessageResult();

        if(message == null)
            return result;

        trackInitialStop(message);

        HandleMoveReset(message);

        // ignition off
        Boolean ignitionOff = !message.isIgnitionOn();
        if(ignitionOff){
            EventRecord ignOffRecord = createIgnOffEventRecord(message);
            EventRecord driveOffEventRecord = createDriveOffEventRecord(message);

            result.getEventRecords().add(ignOffRecord);
            result.getEventRecords().add(driveOffEventRecord);

            return setStateAndRecurse(new AobrdEngineOff(sharedState), message, result);
        }

        // speed is over threshold, MOVE
        if(message.getSpeedometer() >= THRESHOLD_MPH_MOVE) {

            EventRecord moveStartEvent = createMoveStartEventRecord(message);

            result.getEventRecords().add(moveStartEvent);

            return setStateAndRecurse(new AobrdMovingNotDriving(sharedState), message, result);
        }

        return result;
    }

    private void trackInitialStop(IHOSMessage message) {
        if(stopTime.getTime() == 0)
        {
            stopTime = message.getTimestampUtc().toDate();
        }
    }

    private void HandleMoveReset(IHOSMessage message) {
        // Handle scenario when we started moving but stopped and remained stopped for > stop threshold
        if(sharedState.getMoveStartMessage() != null)
        {
            long elapsedMilliseconds = message.getTimestampUtc().getMillis() - stopTime.getTime();
            long elapsedSeconds = (elapsedMilliseconds / 1000);
            long thresholdDriveStopSeconds = sharedState.getCurrentThresholds().getDriveStopTime() * 60;

            if(elapsedSeconds > thresholdDriveStopSeconds)
            {
                // reset move start
                sharedState.setMoveStartMessage(null);
            }
        }
    }
}