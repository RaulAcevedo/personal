package com.jjkeller.kmbapi.geotab.tests.states;

import android.os.Message;

import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.MessageResult;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.MandateMovingDriving;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.MandateMovingNotDriving;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.State;
import com.jjkeller.kmbapi.geotab.HosMessages;
import com.jjkeller.kmbapi.geotab.abstraction.base.HosProcessorStateMachineTestBase;
import com.jjkeller.kmbapi.kmbeobr.EventRecord;
import com.jjkeller.kmbapi.kmbeobr.EventTypeEnum;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

/**
 * Created by rab5795 on 4/11/18.
 */

public class MandateOdoErrorTest extends HosProcessorStateMachineTestBase {

    public MandateOdoErrorTest() throws Exception{
    }

    @Test
    public void testProcessMessage_whenEngineOdoIsFalse() throws Exception{
        sharedState.setLastMessage(HosMessages.EngineBasedVSS(datetimeFormat.parse("2017.12.29 12:00")));
        State state = new MandateMovingDriving(sharedState);
        MessageResult result = state.processMessage(HosMessages.GPSBasedOdoOffStopped(datetimeFormat.parse("2017.12.29 13:00")));
        ArrayList<EventRecord> eventRecords = result.getEventRecords();

        Assert.assertTrue("Returned Event should be Odo Error on:", eventRecords.get(0).getEventType() == EventTypeEnum.ERROR);
        Assert.assertTrue("Return event should be 1024: ", eventRecords.get(0).getEventData() == 1024);
        Assert.assertTrue("Returned Event should be Drive on:", eventRecords.get(1).getEventType() == EventTypeEnum.VEHICLESTOPPED);

    }

    @Test
    public void testProcessMessage_whenEngineOdoIsFalseFollowedByEngineOdoTrue() throws Exception {
        sharedState.setLastMessage(HosMessages.EngineBasedVSS(datetimeFormat.parse("2017.12.29 12:00")));
        State state = new MandateMovingNotDriving(sharedState);
        MessageResult result = state.processMessage(HosMessages.GPSBasedOdoDriving(datetimeFormat.parse("2017.12.29 13:00")));

        ArrayList<EventRecord> eventRecords = result.getEventRecords();

        result = state.processMessage(HosMessages.EngineOnStopped(datetimeFormat.parse("2017.12.29 14:15")));

        eventRecords.addAll(result.getEventRecords());

        Assert.assertTrue("Returned Event should be Odo Error on:", eventRecords.get(0).getEventType() == EventTypeEnum.ERROR);
        Assert.assertTrue("Return event should be 1024: ", eventRecords.get(0).getEventData() == 1024);
        Assert.assertTrue("Returned Event should be Drive on:", eventRecords.get(1).getEventType() == EventTypeEnum.DRIVESTART);
        Assert.assertTrue("Returned Event should be Error off:", eventRecords.get(2).getEventType() == EventTypeEnum.ERROR);
        Assert.assertTrue("Return event should be 0: ", eventRecords.get(2).getEventData() == 0);
        Assert.assertTrue("Returned Event should be Odo Move Stop:", eventRecords.get(3).getEventType() == EventTypeEnum.VEHICLESTOPPED);

    }

    @Test
    public void testProcessMessage_whenEngineOdoIsFalseFollowedByEngineOdoTrue2() throws Exception {
        sharedState.setLastMessage(HosMessages.EngineBasedVSS(datetimeFormat.parse("2017.12.29 12:00")));
        State state = new MandateMovingNotDriving(sharedState);
        MessageResult result = state.processMessage(HosMessages.GPSBasedOdoDriving(datetimeFormat.parse("2017.12.29 13:00")));

        ArrayList<EventRecord> eventRecords = result.getEventRecords();

        result = state.processMessage(HosMessages.EngineOnStopped(datetimeFormat.parse("2017.12.29 14:15")));

        eventRecords.addAll(result.getEventRecords());

        Assert.assertTrue("Returned Event should be Odo Error on:", eventRecords.get(0).getEventType() == EventTypeEnum.ERROR);
        Assert.assertTrue("Return event should be 1024: ", eventRecords.get(0).getEventData() == 1024);
        Assert.assertTrue("Returned Event should be Drive on:", eventRecords.get(1).getEventType() == EventTypeEnum.DRIVESTART);
        Assert.assertTrue("Returned Event should be Error off:", eventRecords.get(2).getEventType() == EventTypeEnum.ERROR);
        Assert.assertTrue("Return event should be 0: ", eventRecords.get(2).getEventData() == 0);
        Assert.assertTrue("Returned Event should be Odo Move Stop:", eventRecords.get(3).getEventType() == EventTypeEnum.VEHICLESTOPPED);

    }

}
