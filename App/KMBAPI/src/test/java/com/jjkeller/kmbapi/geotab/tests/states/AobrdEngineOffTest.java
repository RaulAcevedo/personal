package com.jjkeller.kmbapi.geotab.tests.states;

import com.jjkeller.kmbapi.HosMessageProcessor.interfaces.IHOSMessage;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.MessageResult;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.AobrdEngineOff;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.AobrdMovingNotDriving;
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
 * Created by jld5296 on 9/15/16.
 */
public class AobrdEngineOffTest extends HosProcessorStateMachineTestBase {

    public AobrdEngineOffTest() throws Exception {
    }

    @Test
    public void testProcessMessage_Aobrd_whenIgnnitionOn_ReturnIGNONEvent() throws Exception {
        State state = new AobrdEngineOff(sharedState);
        MessageResult result = state.processMessage(HosMessages.EngineOnStopped(datetimeFormat.parse("2016.09.19 13:00")));

        ArrayList<EventRecord> eventRecords = result.getEventRecords();
        Assert.assertEquals(2, eventRecords.size());
        Assert.assertEquals("Returned Event should be IGN_ON", EventTypeEnum.IGNITIONON, eventRecords.get(0).getEventType());
        Assert.assertEquals("Returned Event should be STOP", EventTypeEnum.VEHICLESTOPPED, eventRecords.get(1).getEventType());
        state = result.getNewState();

        Assert.assertTrue("New state should be AobrdEngineOn", result.getNewState() instanceof AobrdStoppedNotDriving);

        // A second data object with the same data should not produce anything since we are now in a stopped not driving state.
        result = state.processMessage(HosMessages.EngineOnStopped(datetimeFormat.parse("2016.09.19 13:00:02")));

        eventRecords = result.getEventRecords();
        Assert.assertTrue("Should be no event", eventRecords.size() == 0);
    }

    @Test
    public void testProcessMessage_Aobrd_whenIgnitionOnAndSpeedIncreaces_ReturnIgnOnAndMoveStartEvent() throws Exception {
        State state = new AobrdEngineOff(sharedState);

        IHOSMessage message = HosMessages.EngineOnMoving(datetimeFormat.parse("2016.09.19 13:00"));
        message.setOdometer(1000f);
        MessageResult result = state.processMessage(message);

        ArrayList<EventRecord> eventRecords = result.getEventRecords();
        Assert.assertTrue("Should IGN_ON, STOP, and MOVE event", eventRecords.size() == 3);

        Assert.assertTrue("Returned Event should be IGN_ON", eventRecords.get(0).getEventType() == EventTypeEnum.IGNITIONON);
        Assert.assertTrue("Returned Event should be STOP", eventRecords.get(1).getEventType() == EventTypeEnum.VEHICLESTOPPED);
        Assert.assertTrue("Returned Event should be MOVE", eventRecords.get(2).getEventType() == EventTypeEnum.MOVE);

        state = result.getNewState();
        Assert.assertTrue("New state should be AobrdMovingNotDriving", state instanceof AobrdMovingNotDriving);
    }
}