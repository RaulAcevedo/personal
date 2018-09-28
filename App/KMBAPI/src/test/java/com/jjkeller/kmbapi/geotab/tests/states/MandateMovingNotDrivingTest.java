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

/**
 * Created by jld5296 on 9/16/16.
 */
public class MandateMovingNotDrivingTest extends HosProcessorStateMachineTestBase {

    public MandateMovingNotDrivingTest() throws Exception {
    }

    // ignition turns off -> IGN_OFF, STOP -> Engine Off Not Driving
    @Test
    public void testProcessMessage_whenIgnnitionOff_ReturnIgnOffAndStopEventsAndTransitionToMandateEngineOffNotDriving() throws Exception {
        State state = new MandateMovingNotDriving(sharedState);
        sharedState.setLastMessage(HosMessages.EngineOff(datetimeFormat.parse("2016.09.19 13:00")));
        MessageResult result =  state.processMessage(HosMessages.EngineOff(datetimeFormat.parse("2016.09.19 13:01")));

        ArrayList<EventRecord> eventRecords = result.getEventRecords();
        Assert.assertTrue("Should be IGN_OFF, STOP", eventRecords.size() == 2);

        Assert.assertTrue("Returned Event should be IGN_OFF", eventRecords.get(0).getEventType() == EventTypeEnum.IGNITIONOFF);
        Assert.assertTrue("Returned Event should be STOP", eventRecords.get(1).getEventType() == EventTypeEnum.VEHICLESTOPPED);
        Assert.assertTrue("New state should be MandateEngineOffNotDriving", result.getNewState() instanceof MandateEngineOffNotDriving);
    }

    // speed = 0 -> STOP -> Stopped Not Driving
    @Test
    public void testProcessMessage_whenSpeedBelowThreshold_ReturnStoppedEventAndTransitionToMandateStoppedNotDriving() throws Exception {
        State state = new MandateMovingNotDriving(sharedState);

        IHOSMessage message = HosMessages.EngineOnMoving(datetimeFormat.parse("2016.09.19 13:00"));
        message.setSpeedometer(0);
        MessageResult result =  state.processMessage(message);

        ArrayList<EventRecord> eventRecords = result.getEventRecords();
        Assert.assertTrue("Should be STOP", eventRecords.size() == 1);

        EventRecord singleEvent = eventRecords.get(0);
        Assert.assertTrue("Returned Event should be STOP", singleEvent.getEventType() == EventTypeEnum.VEHICLESTOPPED);
        Assert.assertTrue("New state should be MandateStoppedNotDriving", result.getNewState() instanceof MandateStoppedNotDriving);
    }

    // speed >= 5 -> DRIVE_ON -> Moving Driving
    @Test
    public void testProcessMessage_whenSpeedAboveThreshold_ReturnDriveOnEventAndTransitionToMandateMovingDriving() throws Exception {
        State state = new MandateMovingNotDriving(sharedState);
        IHOSMessage message = HosMessages.EngineOnMoving(datetimeFormat.parse("2016.09.19 13:00"));
        message.setSpeedometer(50);
        MessageResult result =  state.processMessage(message);

        ArrayList<EventRecord> eventRecords = result.getEventRecords();
        Assert.assertTrue("Should be DRIVE_ON", eventRecords.size() == 1);

        EventRecord singleEvent = eventRecords.get(0);
        Assert.assertTrue("Returned Event should be DRIVE_ON", singleEvent.getEventType() == EventTypeEnum.DRIVESTART);

        // TODO: Fix This
        //Assert.assertEquals("DRIVE_ON should correspond to time that we exceeded speed threshold", message.getTimestampUtc(), singleEvent.getTimecodeAsDate());
        Assert.assertTrue("New state should be MandateMovingDriving", result.getNewState() instanceof MandateMovingDriving);
    }
}