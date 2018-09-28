package com.jjkeller.kmbapi.geotab.tests.states;

import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.MessageResult;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.MandateEngineOffNotDriving;
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
public class MandateEngineOffNotDrivingTest extends HosProcessorStateMachineTestBase {

    public MandateEngineOffNotDrivingTest() throws Exception {
    }

    // ignition turns on -> IGN_ON -> Stopped Not Driving
    @Test
    public void testProcessMessage_whenIgnnitionOn_ReturnIGNONEvent() throws Exception {
        State state = new MandateEngineOffNotDriving(sharedState);
        MessageResult result =  state.processMessage(HosMessages.EngineOnStopped(datetimeFormat.parse("2016.09.19 13:00")));

        ArrayList<EventRecord> eventRecords = result.getEventRecords();
        Assert.assertEquals(2, eventRecords.size());

        Assert.assertTrue("Returned Event should be IGN_ON", eventRecords.get(0).getEventType() == EventTypeEnum.IGNITIONON);
        Assert.assertTrue("Returned Event should be STOP", eventRecords.get(1).getEventType() == EventTypeEnum.VEHICLESTOPPED);
        Assert.assertTrue("New state should be MandateStoppedNotDriving", result.getNewState() instanceof MandateStoppedNotDriving);

        state = result.getNewState();

        // A second data object with the same data should not produce anything since we are now in a stopped not driving state.
        result =  state.processMessage(HosMessages.EngineOnStopped(datetimeFormat.parse("2016.09.19 13:00:02")));

        eventRecords = result.getEventRecords();
        Assert.assertTrue("Should be no event", eventRecords.size() == 0);
    }
}