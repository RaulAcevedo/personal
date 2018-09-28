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
public class AobrdStoppedDriving extends State {

    public AobrdStoppedDriving(SharedState sharedState, Date stopTime) {
        super(sharedState);
        _startTime = stopTime;
    }

    private Date _startTime;

    @Override
    public MessageResult processMessage(IHOSMessage message) {
        MessageResult result = new MessageResult();

        if(message == null)
            return new MessageResult();

        // ignition turns off -> IGN_OFF, DRIVE_OFF -> Engine Off
        Boolean ignitionOff = !message.isIgnitionOn();
        if(ignitionOff){
            EventRecord ignOffRecord = createIgnOffEventRecord(message);
            EventRecord driveOffEventRecord = createDriveOffEventRecord(message, _startTime.getTime());

            result.getEventRecords().add(ignOffRecord);
            result.getEventRecords().add(driveOffEventRecord);

            result.setNewState(new AobrdEngineOff(sharedState));
            result.getEventRecords().addAll(result.getNewState().processMessage(message).getEventRecords());

            return result;
        }

        // speed > 0 -> MOVE -> Moving Driving
        if(message.getSpeedometer() >= THRESHOLD_MPH_MOVE) {

            EventRecord moveStartRecord = createMoveStartEventRecord(message);

            result.getEventRecords().add(moveStartRecord);

            result.setNewState(new AobrdMovingDriving(sharedState));
            result.getEventRecords().addAll(result.getNewState().processMessage(message).getEventRecords());
            return result;
        }

        // remain stopped for X minutes	-> DRIVE_OFF -> Stopped Not Driving
        final int MILLISECONDS_IN_MINUTE = 60000;
        long millisecondsSinceInitialStop = message.getTimestampUtc().getMillis() - _startTime.getTime();
        if(millisecondsSinceInitialStop >= sharedState.getCurrentThresholds().getDriveStopTime()*MILLISECONDS_IN_MINUTE) {

            EventRecord driverOffEventRecord = createDriveOffEventRecord(message, _startTime.getTime());

            result.getEventRecords().add(driverOffEventRecord);

            return setStateAndRecurse(new AobrdStoppedNotDriving(sharedState), message, result);
        }

        return result;
    }
}