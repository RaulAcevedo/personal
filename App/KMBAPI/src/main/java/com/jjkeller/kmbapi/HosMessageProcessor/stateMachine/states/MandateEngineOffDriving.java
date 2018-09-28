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
public class MandateEngineOffDriving extends State {

    private Date _startTime;

    public MandateEngineOffDriving(SharedState sharedState, Date timestamp) {
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

        //Create Error Event
        EventRecord errorRecord = createErrorRecord(message);
        if(errorRecord != null){
            result.getEventRecords().add(errorRecord);
        }


        calculateGpsUncertDistance(message);
        List<EventRecord> errorResult = sharedState.getGpsErrorState().processMessage(message);
        if(errorResult != null)
            result.getEventRecords().addAll(errorResult);

        // ignition turns on -> IGN_ON -> Stopped Driving
        if(message.isIgnitionOn()){
            EventRecord ignOnEventRecord = createIgnOnEventRecord(message);
            EventRecord stopEventRecord = createMoveStopEventRecord(message);
            result.getEventRecords().add(ignOnEventRecord);
            result.getEventRecords().add(stopEventRecord);

            return setStateAndRecurse(new MandateStoppedDriving(sharedState, message.getTimestampUtc().toDate()), message, result);
        }

        // remain stopped for X minutes -> DRIVE_OFF -> Engine Off Not Driving
        long millisecondsSinceDriveStart = message.getTimestampUtc().getMillis() - _startTime.getTime();
        int minutesSinceInitialStop = ((Double)(((double)millisecondsSinceDriveStart / 1000) / 60)).intValue();
        if(minutesSinceInitialStop >= sharedState.getCurrentThresholds().getDriveStopTime())
        {
            EventRecord driverOffEventRecord = createDriveOffEventRecord(message, _startTime.getTime());

            result.getEventRecords().add(driverOffEventRecord);

            return setStateAndRecurse(new MandateEngineOffNotDriving(sharedState), message, result);
        }

        // Default
        return result;
    }
}
