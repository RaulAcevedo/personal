package com.jjkeller.kmbapi.geotab.tests.states;

import android.os.Message;

import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.MessageResult;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.MandateEngineOffNotDriving;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.MandateMovingDriving;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.MandateMovingNotDriving;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.MandateVSSError;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.State;
import com.jjkeller.kmbapi.geotab.HosMessages;
import com.jjkeller.kmbapi.geotab.abstraction.base.HosProcessorStateMachineTestBase;
import com.jjkeller.kmbapi.kmbeobr.EventRecord;
import com.jjkeller.kmbapi.kmbeobr.EventTypeEnum;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

/**
 * Created by rab5795 on 12/29/17.
 */

public class MandateVSSErrorTest extends HosProcessorStateMachineTestBase {
    public MandateVSSErrorTest() throws Exception{
    }

    @Test
    public void testProcessMessage_whenVSSIsNotFromEngine_ReturnVSSError() throws Exception {
        sharedState.setLastMessage(HosMessages.EngineBasedVSS(datetimeFormat.parse("2017.12.29 12:00")));
        State state = new MandateVSSError(sharedState, new MandateMovingDriving(sharedState));
        MessageResult result = state.processMessage(HosMessages.GPSBasedVSS(datetimeFormat.parse("2017.12.29 13:00")));

        ArrayList<EventRecord> eventRecords = result.getEventRecords();
        Assert.assertTrue("Should only be a single event", eventRecords.size() == 1);

        EventRecord singleEvent = eventRecords.get(0);
        Assert.assertTrue("Returned Event should be IGN_OFF", singleEvent.getEventType() == EventTypeEnum.ERROR);

    }

    @Test
    public void testProcessMessage_whenVSSFromEngineStartsWorkingInVSSErrorState() throws  Exception {
        sharedState.setLastMessage(HosMessages.EngineBasedVSS(datetimeFormat.parse("2017.12.29 12:00")));
        State state = new MandateVSSError(sharedState, new MandateMovingNotDriving(sharedState));
        MessageResult result = state.processMessage(HosMessages.EngineOnMoving(datetimeFormat.parse("2018.04.17 13:00")));

        ArrayList<EventRecord> eventRecords = result.getEventRecords();

        Assert.assertTrue("Returned Move Event", eventRecords.get(0).getEventType() == EventTypeEnum.MOVE);
        Assert.assertTrue("Returned Drive Event", eventRecords.get(1).getEventType() == EventTypeEnum.DRIVESTART);

    }

    @Test
    public void testProcessMessage_whenTransitioningFromDrivingToVssError() throws Exception{
        sharedState.setLastMessage(HosMessages.EngineBasedVSS(datetimeFormat.parse("2017.12.29 12:00")));
        State state = new MandateMovingDriving(sharedState);
        MessageResult result = state.processMessage(HosMessages.GPSBasedVSS(datetimeFormat.parse("2018.04.17 13:00")));
        ArrayList<EventRecord> eventRecords = result.getEventRecords();

        Assert.assertTrue("Returned Error Event", eventRecords.get(0).getEventType() == EventTypeEnum.ERROR);
        Assert.assertTrue("Returned Vss 512 Event", 512 == eventRecords.get(0).getEventData());
        Assert.assertTrue("Returned Stop Event", eventRecords.get(1).getEventType() == EventTypeEnum.VEHICLESTOPPED);

    }
}
