package com.jjkeller.kmbapi.geotab.tests.states;

import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.MessageResult;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.MandateEngineOffDriving;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.MandateEngineOffNotDriving;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.MandateStoppedDriving;
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
public class MandateEngineOffDrivingTest extends HosProcessorStateMachineTestBase {

    public MandateEngineOffDrivingTest() throws Exception {
    }

    // ignition turns on -> IGN_ON -> Stopped Driving
    @Test
    public void testProcessMessage_whenIgnitionOn_ReturnIGNONEvent() throws Exception {
        State state = new MandateEngineOffDriving(sharedState, datetimeFormat.parse("2016.09.19 13:00"));
        MessageResult result =  state.processMessage(HosMessages.EngineOnStopped(datetimeFormat.parse("2016.09.19 13:00:02")));
        ArrayList<EventRecord> eventRecords = result.getEventRecords();
        Assert.assertEquals("Should only be single event", 2, eventRecords.size());
        Assert.assertEquals("Returned Event should be IGN_ON", EventTypeEnum.IGNITIONON, eventRecords.get(0).getEventType());
        Assert.assertEquals("Returned Event should be STOP", EventTypeEnum.VEHICLESTOPPED, eventRecords.get(1).getEventType());
        Assert.assertTrue("New state should be MandateStoppedDriving", result.getNewState() instanceof MandateStoppedDriving);
    }

    // remain stopped for X minutes -> DRIVE_OFF -> Engine Off Not Driving
    @Test
    public void testProcessMessage_whenStoppedMoreThanThreshold_ReturnDriveOffEventAndTransitionToMandateEngineOffNotDriving() throws Exception {
        sharedState.setLastMessage(HosMessages.EngineBasedVSS(datetimeFormat.parse("2016.09.19 13:05:00")));
        Date stopTime = datetimeFormat.parse("2016.09.19 13:00");
        State state = new MandateEngineOffDriving(sharedState, stopTime);
        MessageResult result =  state.processMessage(HosMessages.EngineOff(datetimeFormat.parse("2016.09.19 13:06:00")));

        ArrayList<EventRecord> eventRecords = result.getEventRecords();
        Assert.assertTrue("Should only be DRIVE_OFF ", eventRecords.size() == 1);

        EventRecord singleEvent = eventRecords.get(0);
        Assert.assertTrue("Returned Event should be DRIVE_OFF", singleEvent.getEventType() == EventTypeEnum.DRIVEEND);
        Assert.assertEquals("Returned DRIVE_OFF should correspond with initial stop time.", stopTime, eventRecords.get(0).getTimecodeAsDate());
        Assert.assertTrue("New state should be MandateEngineOffNotDriving", result.getNewState() instanceof MandateEngineOffNotDriving);
    }


}