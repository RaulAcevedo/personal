package com.jjkeller.kmbapi.geotab.tests.states;


import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.MessageResult;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.MandateEngineOffDriving;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.MandateMovingDriving;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.MandateStoppedDriving;
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
public class MandateMovingDrivingTest extends HosProcessorStateMachineTestBase {

    public MandateMovingDrivingTest() throws Exception {
    }


    // ignition turns off -> STOP, IGN_OFF -> Engine Off Driving
    @Test
    public void testProcessMessage_whenIgnnitionOff_ReturnIgnOffAndStopEventsAndTransitionsToMandateEngineOffDriving() throws Exception {
        State state = new MandateMovingDriving(sharedState);
        sharedState.setLastMessage(HosMessages.EngineOff(datetimeFormat.parse("2016.09.19 12:59")));
        MessageResult result =  state.processMessage(HosMessages.EngineOff(datetimeFormat.parse("2016.09.19 13:00")));

        ArrayList<EventRecord> eventRecords = result.getEventRecords();
        Assert.assertTrue("IGN_OFF STOP", eventRecords.size() == 2);

        EventRecord singleEvent = eventRecords.get(0);
        Assert.assertTrue("Returned Event should be IGN_OFF", singleEvent.getEventType() == EventTypeEnum.IGNITIONOFF);
        Assert.assertTrue("New state should be MandateEngineOffDriving", result.getNewState() instanceof MandateEngineOffDriving);
        state = result.getNewState();

        // A second data object with the same data should not produce anything since we are now in a stopped not driving state.
        result =  state.processMessage(HosMessages.EngineOff(datetimeFormat.parse("2016.09.19 13:00:02")));

        eventRecords = result.getEventRecords();
        Assert.assertTrue("Should be no event", eventRecords.size() == 0);
    }

    // speed = 0 -> STOP -> Stopped Driving
    @Test
    public void testProcessMessage_whenSpeedBelowThreshold_ReturnStopEventAndTransitionsToMandateStoppedDriving() throws Exception {
        State state = new MandateMovingDriving(sharedState);
        MessageResult result =  state.processMessage(HosMessages.EngineOnStopped(datetimeFormat.parse("2016.09.19 13:00")));

        ArrayList<EventRecord> eventRecords = result.getEventRecords();
        Assert.assertTrue("STOP", eventRecords.size() == 1);

        EventRecord singleEvent = eventRecords.get(0);
        Assert.assertTrue("Returned Event should be STOP", singleEvent.getEventType() == EventTypeEnum.VEHICLESTOPPED);
        Assert.assertTrue("New state should be MandateEngineOffDriving", result.getNewState() instanceof MandateStoppedDriving);


    }
}