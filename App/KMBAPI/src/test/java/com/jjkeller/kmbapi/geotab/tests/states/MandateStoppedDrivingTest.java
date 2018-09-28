package com.jjkeller.kmbapi.geotab.tests.states;

import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.MessageResult;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.MandateEngineOffDriving;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.MandateMovingDriving;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.MandateStoppedDriving;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.MandateStoppedNotDriving;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.State;
import com.jjkeller.kmbapi.geotab.HosMessages;
import com.jjkeller.kmbapi.kmbeobr.EventTypeEnum;
import com.jjkeller.kmbapi.geotab.abstraction.base.HosProcessorStateMachineTestBase;

import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

/**
 * Created by jld5296 on 9/16/16.
 */
public class MandateStoppedDrivingTest extends HosProcessorStateMachineTestBase {

    public MandateStoppedDrivingTest() throws Exception {
    }


    // ignition turns off -> IGN_OFF -> Engine Off Driving
    @Test
    public void testProcessMessage_whenIgnitionOff_ReturnIGNOFFEvent() throws Exception {
        sharedState.setLastMessage(HosMessages.EngineOff(datetimeFormat.parse("2016.09.19 13:00:00")));
        State state = new MandateStoppedDriving(sharedState, datetimeFormat.parse("2016.09.19 13:01"));
        MessageResult result =  state.processMessage(HosMessages.EngineOff(datetimeFormat.parse("2016.09.19 13:00:02")));

        Assert.assertTrue("Returned Event should be IGN_OFF", result.getEventRecords().get(0).getEventType() == EventTypeEnum.IGNITIONOFF);
        Assert.assertTrue("New state should be MandateEngineOffDriving", result.getNewState() instanceof MandateEngineOffDriving);
    }

    // remain stopped for X minutes -> DRIVE_OFF -> Stopped Not Driving
    @Test
    public void testProcessMessage_whenStoppedPassedThreshold_ReturnDriveOffEventAndTransitionToMandateStoppedNotDriving() throws Exception {
        Date stopTime = datetimeFormat.parse("2016.09.19 13:00");
        State state = new MandateStoppedDriving(sharedState, stopTime);
        MessageResult result =  state.processMessage(HosMessages.EngineOnStopped(datetimeFormat.parse("2016.09.19 13:06:02")));

        Assert.assertTrue("Returned Event should be DRIVE_OFF", result.getEventRecords().get(0).getEventType() == EventTypeEnum.DRIVEEND);
        Assert.assertEquals("Returned DRIVE_OFF should correspond with initial stop time.", stopTime, result.getEventRecords().get(0).getTimecodeAsDate());
        Assert.assertTrue("New state should be MandateStoppedNotDriving", result.getNewState() instanceof MandateStoppedNotDriving);
    }


    // speed > 0 -> MOVE -> Moving Driving
    @Test
    public void testProcessMessage_whenSpeedGreaterThanThreshold_ReturnMoveEventAndTransitionToMandateMovingDriving() throws Exception {
        State state = new MandateStoppedDriving(sharedState, datetimeFormat.parse("2016.09.19 13:00"));
        MessageResult result =  state.processMessage(HosMessages.EngineOnMoving(datetimeFormat.parse("2016.09.19 13:00:50")));

        Assert.assertTrue("Returned Event should be MOVE", result.getEventRecords().get(0).getEventType() == EventTypeEnum.MOVE);
        Assert.assertTrue("New state should be MandateMovingDriving", result.getNewState() instanceof MandateMovingDriving);
    }
}