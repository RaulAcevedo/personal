package com.jjkeller.kmbapi.geotab.tests.states;

import com.jjkeller.kmbapi.HosMessageProcessor.interfaces.IHOSMessage;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.MessageResult;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.AobrdEngineOff;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.AobrdMovingDriving;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.AobrdStoppedDriving;
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
public class AobrdMovingDrivingTest extends HosProcessorStateMachineTestBase {

    public AobrdMovingDrivingTest() throws Exception {
    }

    @Test
    public void testProcessMessage_whenIgnnitionOff_ReturnIGNOFFEvent() throws Exception {

        State state = new AobrdMovingDriving(sharedState);
        MessageResult result =  state.processMessage(HosMessages.EngineOff(datetimeFormat.parse("2016.09.19 13:00")));

        ArrayList<EventRecord> eventRecords = result.getEventRecords();
        Assert.assertTrue("DRIVE_OFF, STOP, IGN_OFF", eventRecords.size() == 3);

        EventRecord singleEvent = eventRecords.get(0);
        Assert.assertTrue("Returned Event should be IGN_OFF", singleEvent.getEventType() == EventTypeEnum.IGNITIONOFF);
        Assert.assertTrue("New state should be AobrdEngineOff", result.getNewState() instanceof AobrdEngineOff);
    }

    @Test
    public void testProcessMessage_whenDuplicateMessages_SecondMessageDoesNothing() throws Exception {

        State state = new AobrdMovingDriving(sharedState);
        MessageResult result =  state.processMessage(HosMessages.EngineOff(datetimeFormat.parse("2016.09.19 13:00")));
        state = result.getNewState();
        // A second data object with the same data should not produce anything since we are now in a stopped not driving state.
        result =  state.processMessage(HosMessages.EngineOff(datetimeFormat.parse("2016.09.19 13:00:02")));

        ArrayList<EventRecord> eventRecords = result.getEventRecords();
        Assert.assertTrue("Should be no event", eventRecords.size() == 0);
        Assert.assertTrue("No new State", result.getNewState() == null);
    }

    @Test
    public void testProcessMessage_whenSpeedIsUnderMoveThreshold_StopEventAndTransitionToAobrdStoppedDriving() throws Exception {

        State state = new AobrdMovingDriving(sharedState);
        IHOSMessage msg = HosMessages.EngineOnStopped(datetimeFormat.parse("2016.09.19 13:00"));
        MessageResult result =  state.processMessage(msg);

        ArrayList<EventRecord> eventRecords = result.getEventRecords();
        Assert.assertTrue("STOP", eventRecords.size() == 1);

        Assert.assertEquals(EventTypeEnum.VEHICLESTOPPED, eventRecords.get(0).getEventType());
        Assert.assertEquals(AobrdStoppedDriving.class,  result.getNewState().getClass());
    }
}