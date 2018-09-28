package com.jjkeller.kmbapi.geotab.tests.states;

import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.MessageResult;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.AobrdEngineOff;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.AobrdStoppedNotDriving;
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
public class AobrdStoppedNotDrivingTest extends HosProcessorStateMachineTestBase {

    public AobrdStoppedNotDrivingTest() throws Exception {
    }

    @Test
    public void testProcessMessage_whenIgnnitionOff_ReturnIGNOFFEvent() throws Exception {

        State state = new AobrdStoppedNotDriving(sharedState);
        MessageResult result =  state.processMessage(HosMessages.EngineOff(datetimeFormat.parse("2016.09.19 13:00")));

        ArrayList<EventRecord> eventRecords = result.getEventRecords();
        Assert.assertEquals(2, eventRecords.size());

        Assert.assertEquals("Returned Event should be IGN_OFF", EventTypeEnum.IGNITIONOFF, eventRecords.get(0).getEventType());
        Assert.assertEquals("Returned Event should be DRIVE_OFF", EventTypeEnum.DRIVEEND, eventRecords.get(1).getEventType());

        state = result.getNewState();

        // A second data object with the same data should not produce anything since we are now in a stopped not driving state.
        result =  state.processMessage(HosMessages.EngineOff(datetimeFormat.parse("2016.09.19 13:00:02")));

        eventRecords = result.getEventRecords();
        Assert.assertTrue("Should be no event", eventRecords.size() == 0);
    }

    @Test
    public void testProcessMessage_whenIgnnitionOff_ReturnEngineOffState() throws Exception {


        AobrdStoppedNotDriving state = new AobrdStoppedNotDriving(sharedState);
        MessageResult result =  state.processMessage(HosMessages.EngineOff(datetimeFormat.parse("2016.09.19 13:00")));

        Assert.assertTrue("New state should be AobrdEngineOff", result.getNewState() instanceof AobrdEngineOff);
    }
}