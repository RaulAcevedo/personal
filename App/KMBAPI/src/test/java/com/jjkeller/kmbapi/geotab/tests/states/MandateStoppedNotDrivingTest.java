package com.jjkeller.kmbapi.geotab.tests.states;

import com.jjkeller.kmbapi.HosMessageProcessor.interfaces.IHOSMessage;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.MessageResult;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.MandateEngineOffNotDriving;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.MandateMovingDriving;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.MandateMovingNotDriving;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.MandateStoppedNotDriving;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.State;
import com.jjkeller.kmbapi.geotab.HosMessages;
import com.jjkeller.kmbapi.geotab.abstraction.base.HosProcessorStateMachineTestBase;
import com.jjkeller.kmbapi.kmbeobr.EventRecord;
import com.jjkeller.kmbapi.kmbeobr.EventTypeEnum;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;


/**
 * Created by jld5296 on 9/16/16.
 */
public class MandateStoppedNotDrivingTest extends HosProcessorStateMachineTestBase {

    public MandateStoppedNotDrivingTest() throws Exception {
    }


    // ignition turns off -> IGN_OFF -> Engine Off Not Driving
    @Test
    public void testProcessMessage_whenIgnnitionOff_ReturnIgnOffEventAndTransitionToMandateEngineOffNotDriving() throws Exception {
        State state = new MandateStoppedNotDriving(sharedState);
        sharedState.setLastMessage(HosMessages.EngineOff(datetimeFormat.parse("2016.09.19 13:00")));
        MessageResult result =  state.processMessage(HosMessages.EngineOff(datetimeFormat.parse("2016.09.19 13:02")));

        ArrayList<EventRecord> eventRecords = result.getEventRecords();
        Assert.assertTrue("Should only be single event", eventRecords.size() == 1);

        EventRecord singleEvent = eventRecords.get(0);
        Assert.assertTrue("Returned Event should be IGN_OFF", singleEvent.getEventType() == EventTypeEnum.IGNITIONOFF);
        Assert.assertTrue("New state should be MandateEngineOffNotDriving", result.getNewState() instanceof MandateEngineOffNotDriving);
    }

    // speed > 0 -> MOVE -> Moving Not Driving
    @Test
    public void testProcessMessage_whenSpeedGreaterThanThresholdButLessThanDrive_ReturnMoveEventAndTransitionToMandateMovingNotDriving() throws Exception {
        State state = new MandateStoppedNotDriving(sharedState);
        IHOSMessage message = HosMessages.EngineOnMoving(datetimeFormat.parse("2016.09.19 13:00"));
        message.setSpeedometer(3);
        MessageResult result =  state.processMessage(message);

        ArrayList<EventRecord> eventRecords = result.getEventRecords();
        Assert.assertTrue("Should only be single event", eventRecords.size() == 1);

        EventRecord singleEvent = eventRecords.get(0);
        Assert.assertTrue("Returned Event should be MOVE", singleEvent.getEventType() == EventTypeEnum.MOVE);
        Assert.assertTrue("New state should be MandateMovingNotDriving", result.getNewState() instanceof MandateMovingNotDriving);
    }

    // speed >= 5 -> DRIVE_ON, MOVE -> Moving Driving
    @Test
    public void testProcessMessage_whenSpeedGreaterThanThreshold_ReturnMoveEventAndTransitionToMandateMovingDriving() throws Exception {
        Date date = datetimeFormat.parse("2016.09.19 13:00");
        State state = new MandateStoppedNotDriving(sharedState);
        MessageResult result =  state.processMessage(HosMessages.EngineOnMoving(date));

        ArrayList<EventRecord> eventRecords = result.getEventRecords();
        Assert.assertTrue("Should MOVE & DRIVE_ON", eventRecords.size() == 2);

        Assert.assertTrue("Returned Event should be MOVE", eventRecords.get(0).getEventType() == EventTypeEnum.MOVE);
        Assert.assertTrue("Returned Event should be DRIVE_ON", eventRecords.get(1).getEventType() == EventTypeEnum.DRIVESTART);
        Assert.assertEquals("DRIVE_ON should correspond to time that we exceeded speed threshold", date, eventRecords.get(1).getTimecodeAsDate());
        Assert.assertTrue("New state should be MandateMovingDriving", result.getNewState() instanceof MandateMovingDriving);
    }
}