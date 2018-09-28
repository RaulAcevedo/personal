package com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states;

import com.jjkeller.kmbapi.HosMessageProcessor.interfaces.IHOSMessage;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.MessageResult;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.SharedState;
import com.jjkeller.kmbapi.kmbeobr.EventRecord;

import java.util.Date;
import java.util.List;

/**
 * Created by ief5781 on 9/1/16.
 */
@SuppressWarnings("DefaultFileTemplate")
public class MandateStoppedDriving extends State {

    private Date _startTime;

    public MandateStoppedDriving(SharedState sharedState, Date timestamp) {
        super(sharedState);
        _startTime = timestamp;
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

        // ignition turns off -> IGN_OFF, DRIVE_OFF -> Engine Off Driving
        Boolean ignitionOff = !message.isIgnitionOn();
        if(ignitionOff){
            EventRecord ignOffEventRecord = createIgnOffEventRecord(message);

            result.getEventRecords().add(ignOffEventRecord);

            return setStateAndRecurse(new MandateEngineOffDriving(sharedState, message.getTimestampUtc().toDate()), message, result);
        }

        if(!message.isSpeedFromEngine()){
            return setStateAndRecurse(new MandateVSSError(sharedState, this, message.getTimestampUtc().toDate()), message, result);
        }
        // remain stopped for X minutes -> DRIVE_OFF -> Stopped Not Driving
        long millisecondsSinceInitialStop = message.getTimestampUtc().getMillis() - _startTime.getTime();
        int minutesSinceInitialStop = ((Double)(((double)millisecondsSinceInitialStop / 1000) / 60)).intValue();
        if(minutesSinceInitialStop >= sharedState.getCurrentThresholds().getDriveStopTime()) {

            EventRecord driveOffEventRecord = createDriveOffEventRecord(message, _startTime.getTime());

            result.getEventRecords().add(driveOffEventRecord);

            return setStateAndRecurse(new MandateStoppedNotDriving(sharedState), message, result);
        }

        // speed > 0 -> MOVE -> Moving Driving
        if(message.getSpeedometer() >= THRESHOLD_MPH_MOVE ) {

            EventRecord moveStartEventRecord = createMoveStartEventRecord(message);

            result.getEventRecords().add(moveStartEventRecord);

            return setStateAndRecurse(new MandateMovingDriving(sharedState), message, result);
        }

        // Default
        return result;
    }
}
