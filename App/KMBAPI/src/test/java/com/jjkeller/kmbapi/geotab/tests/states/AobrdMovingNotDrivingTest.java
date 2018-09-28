package com.jjkeller.kmbapi.geotab.tests.states;

import com.jjkeller.kmbapi.HosMessageProcessor.interfaces.IHOSMessage;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.MessageResult;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.AobrdEngineOff;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.AobrdMovingDriving;
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
public class AobrdMovingNotDrivingTest extends HosProcessorStateMachineTestBase {

    public AobrdMovingNotDrivingTest() throws Exception {
    }

    // ignition turns off -> IGN_OFF -> Engine Off
    @Test
    public void testProcessMessage_whenIgnnitionOff_ReturnIGNOFFEventAndTransitionToEngineOffState() throws Exception {

        State state = new AobrdMovingNotDriving(sharedState);
        IHOSMessage message = HosMessages.EngineOff(datetimeFormat.parse("2016.09.19 13:00"));
        message.setOdometer(1000.0f);
        MessageResult result =  state.processMessage(message);

        ArrayList<EventRecord> eventRecords = result.getEventRecords();
        Assert.assertTrue("Should be IGN_OFF STOP", eventRecords.size() == 2);

        EventRecord singleEvent = eventRecords.get(0);
        Assert.assertTrue("Returned Event should be IGN_OFF", singleEvent.getEventType() == EventTypeEnum.IGNITIONOFF);
        Assert.assertTrue("New state should be AobrdEngineOff", result.getNewState() instanceof AobrdEngineOff);
        state = result.getNewState();

        // A second data object with the same data should not produce anything since we are now in a stopped not driving state.
        result =  state.processMessage(HosMessages.EngineOff(datetimeFormat.parse("2016.09.19 13:00:02")));

        eventRecords = result.getEventRecords();
        Assert.assertTrue("Should be no event", eventRecords.size() == 0);
    }

    // speed = 0	                    STOP	    Stopped Not Driving
    @Test
    public void testProcessMessage_whenSpeedUnderThreshold_ReturnStopEventAndTransitionToStoppedNotDriving() throws Exception {

        State state = new AobrdMovingNotDriving(sharedState);
        IHOSMessage message = HosMessages.EngineOnStopped(datetimeFormat.parse("2016.09.19 13:00"));
        message.setOdometer(1000.0f);
        MessageResult result =  state.processMessage(message);

        ArrayList<EventRecord> eventRecords = result.getEventRecords();
        Assert.assertTrue("Should be STOP", eventRecords.size() == 1);

        EventRecord singleEvent = eventRecords.get(0);
        Assert.assertTrue("Returned Event should be STOP", singleEvent.getEventType() == EventTypeEnum.VEHICLESTOPPED);
        Assert.assertTrue("New state should be AobrdEngineOff", result.getNewState() instanceof AobrdStoppedNotDriving);
    }

    // odometer increases > X miles*	DRIVE_ON	Moving Driving
    @Test
    public void testProcessMessage_whenOdometerIncreasesOverThreshold_ReturnDriveOnEventAndTransitionToMovingDriving() throws Exception {

        State state = new AobrdMovingNotDriving(sharedState);
        IHOSMessage movingStart = HosMessages.EngineOnMoving(datetimeFormat.parse("2016.09.19 13:00"));
        movingStart.setOdometer(1000);
        MessageResult result =  state.processMessage(movingStart);

        IHOSMessage movingOverThreshold = HosMessages.EngineOnMoving(datetimeFormat.parse("2016.09.19 13:01"));
        movingOverThreshold.setOdometer(1010);

        result =  state.processMessage(movingOverThreshold);
        ArrayList<EventRecord> eventRecords = result.getEventRecords();
        Assert.assertTrue("Should be DRIVE_ON", eventRecords.size() == 1);

        EventRecord singleEvent = eventRecords.get(0);
        Assert.assertTrue("Returned Event should be DRIVE_ON", singleEvent.getEventType() == EventTypeEnum.DRIVESTART);
        Assert.assertTrue("New state should be AobrdMovingDriving", result.getNewState() instanceof AobrdMovingDriving);
    }

    // odometer increases > X miles*	DRIVE_ON	Moving Driving
    @Test
    public void testProcessMessage_whenOdometerIncreasesOverThresholdWithInterruption_ReturnDriveOnEventAndTransitionToMovingDriving() throws Exception {
        State state = new AobrdMovingNotDriving(sharedState);
        sharedState.getCurrentThresholds().setDriveStopTime(5);

        IHOSMessage moving = HosMessages.EngineOnMoving(datetimeFormat.parse("2016.09.19 13:00"));
        moving.setOdometer(1000.0f);

        IHOSMessage stopped = HosMessages.EngineOnStopped(datetimeFormat.parse("2016.09.19 13:01"));
        stopped.setOdometer(1000.3f);

        IHOSMessage movingOverThreshold = HosMessages.EngineOnMoving(datetimeFormat.parse("2016.09.19 13:02"));
        movingOverThreshold.setOdometer(1000.9f);

        MessageResult movingResults = state.processMessage(moving);
        Assert.assertEquals(0, movingResults.getEventRecords().size());
        Assert.assertEquals(1000f, sharedState.getMoveStartMessage().getOdometer(), .001);
        Assert.assertEquals(datetimeFormat.parse("2016.09.19 13:00"), sharedState.getMoveStartMessage().getTimestampUtc().toDate());

        MessageResult stoppedResults =  state.processMessage(stopped);
        Assert.assertNotNull(stoppedResults.getNewState());
        Assert.assertEquals(AobrdStoppedNotDriving.class, stoppedResults.getNewState().getClass());
        Assert.assertEquals(EventTypeEnum.VEHICLESTOPPED, stoppedResults.getEventRecords().get(0).getEventType());

        State newState = stoppedResults.getNewState();
        MessageResult overThresholdResult = newState.processMessage(movingOverThreshold);

        ArrayList<EventRecord> eventRecords = overThresholdResult.getEventRecords();

        Assert.assertEquals("Should be MOVE,DRIVE_ON", 2,  eventRecords.size());
        Assert.assertEquals("Returned Event should be MOVE",EventTypeEnum.MOVE, eventRecords.get(0).getEventType());
        Assert.assertEquals("Returned Event should be DRIVE_ON",EventTypeEnum.DRIVESTART, eventRecords.get(1).getEventType());

    }



}