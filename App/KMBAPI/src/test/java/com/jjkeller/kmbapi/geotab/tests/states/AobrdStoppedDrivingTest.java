package com.jjkeller.kmbapi.geotab.tests.states;

import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.MessageResult;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.AobrdEngineOff;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.AobrdMovingDriving;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.AobrdStoppedDriving;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.AobrdStoppedNotDriving;
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
public class AobrdStoppedDrivingTest extends HosProcessorStateMachineTestBase {

    public AobrdStoppedDrivingTest() throws Exception {
    }

    // ignition turns off -> IGN_OFF, DRIVE_OFF -> Engine Off
    @Test
    public void testProcessMessage_whenIgnitionOff_ReturnIgnOffAndDriveOffEventsAndTransitionsToAobrdEngineOff() throws Exception {
        Date stopTime = datetimeFormat.parse("2016.09.19 13:00");
        State state = new AobrdStoppedDriving(sharedState, stopTime);
        MessageResult result =  state.processMessage(HosMessages.EngineOff(datetimeFormat.parse("2016.09.19 13:01")));

        ArrayList<EventRecord> eventRecords = result.getEventRecords();
        Assert.assertTrue("Should be IGN_OFF and DRIVE_OFF", eventRecords.size() == 2);

        Assert.assertTrue("Returned Event should be IGN_OFF", eventRecords.get(0).getEventType() == EventTypeEnum.IGNITIONOFF);
        Assert.assertTrue("Returned Event should be DRIVE_OFF", eventRecords.get(1).getEventType() == EventTypeEnum.DRIVEEND);
        Assert.assertEquals("Returned DRIVE_OFF should correspond with initial stop time.", stopTime, eventRecords.get(1).getTimecodeAsDate());
        Assert.assertTrue("New state should be AobrdEngineOff", result.getNewState() instanceof AobrdEngineOff);
    }

    // remain stopped for X minutes -> DRIVE_OFF -> Stopped Not Driving
    @Test
    public void testProcessMessage_whenStoppedForMoreThanThreshold_ReturnDriveOffEventAndTransitionsToAobrdStoppedNotDriving() throws Exception {
        Date stopTime = datetimeFormat.parse("2016.09.19 13:00");
        State state = new AobrdStoppedDriving(sharedState, stopTime);
        MessageResult result =  state.processMessage(HosMessages.EngineOnStopped(datetimeFormat.parse("2016.09.19 13:06")));

        ArrayList<EventRecord> eventRecords = result.getEventRecords();
        Assert.assertTrue("Should be DRIVE_OFF", eventRecords.size() == 1);

        Assert.assertTrue("Returned Event should be DRIVE_OFF", eventRecords.get(0).getEventType() == EventTypeEnum.DRIVEEND);
        Assert.assertEquals("Returned DRIVE_OFF should correspond with initial stop time.", stopTime, eventRecords.get(0).getTimecodeAsDate());
        Assert.assertTrue("New state should be AobrdEngineOff", result.getNewState() instanceof AobrdStoppedNotDriving);
    }

    // speed > 0 -> MOVE -> Moving Driving
    @Test
    public void testProcessMessage_whenSpeedMoreThanThreshold_ReturnMoveEventAndTransitionsToAobrdMovingDriving() throws Exception {

        State state = new AobrdStoppedDriving(sharedState, datetimeFormat.parse("2016.09.19 13:00"));
        MessageResult result =  state.processMessage(HosMessages.EngineOnMoving(datetimeFormat.parse("2016.09.19 13:06")));

        ArrayList<EventRecord> eventRecords = result.getEventRecords();
        Assert.assertTrue("Should be MOVE", eventRecords.size() == 1);

        Assert.assertTrue("Returned Event should be MOVE", eventRecords.get(0).getEventType() == EventTypeEnum.MOVE);
        Assert.assertTrue("New state should be AobrdMovingDriving", result.getNewState() instanceof AobrdMovingDriving);
    }
}