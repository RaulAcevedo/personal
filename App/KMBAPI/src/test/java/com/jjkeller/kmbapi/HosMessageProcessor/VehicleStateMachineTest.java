package com.jjkeller.kmbapi.HosMessageProcessor;

import com.jjkeller.kmbapi.HosMessageProcessor.interfaces.IHOSMessage;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.VehicleStateMachine;
import com.jjkeller.kmbapi.featuretoggle.IFeatureToggleService;
import com.jjkeller.kmbapi.kmbeobr.EventRecord;
import com.jjkeller.kmbapi.kmbeobr.EventTypeEnum;
import com.jjkeller.kmbapi.kmbeobr.Thresholds;
import com.jjkeller.kmbapi.geotab.HosMessages;
import com.jjkeller.kmbapi.geotab.abstraction.base.HosProcessorStateMachineTestBase;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Date;

import static org.mockito.Mockito.when;

/**
 * Created by jld5296 on 10/17/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class VehicleStateMachineTest extends HosProcessorStateMachineTestBase {

    @Mock
    IFeatureToggleService stubFeatureToggleService;

    public VehicleStateMachineTest() throws Exception {
    }

    @Before
    public void init(){
        MockitoAnnotations.initMocks(this);
        when(stubFeatureToggleService.getIsEldMandateEnabled()).thenReturn(false);
    }


    // odometer increases > X miles with interruptions*	DRIVE_ON	Moving Driving
    @Test
    public void testProcessMessage_whenOdometerIncreasesOverThresholdWithInterruptions_ReturnDriveOnEventAndTransitionToMovingDriving() throws Exception {
        Thresholds thresholds = Thresholds.DEFAULT;
        thresholds.setDriveStartDistance(5);
        VehicleStateMachine stateMachine = new VehicleStateMachine(stubFeatureToggleService, thresholds);
        // Move 3 miles
        IHOSMessage moving = HosMessages.EngineOnMoving(datetimeFormat.parse("2016.09.19 13:00"));
        moving.setOdometer(1000);
        ArrayList<EventRecord> result =  stateMachine.processMessage(moving);

        // Stop
        IHOSMessage stopped = HosMessages.EngineOnStopped(datetimeFormat.parse("2016.09.19 13:02"));
        stopped.setOdometer(1003);
        result.addAll(stateMachine.processMessage(stopped));

        // Move 3 more miles
        IHOSMessage moving2 = HosMessages.EngineOnMoving(datetimeFormat.parse("2016.09.19 13:03"));
        moving2.setOdometer(1006);
        result.addAll(stateMachine.processMessage(moving2));

        Assert.assertTrue("Should be IGN_ON, STOP, MOVE, STOP, MOVE, DRIVE_ON", result.size() == 6);

        int index = 0;
        Assert.assertEquals(EventTypeEnum.IGNITIONON, result.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.VEHICLESTOPPED, result.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.MOVE, result.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.VEHICLESTOPPED, result.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.MOVE, result.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.DRIVESTART, result.get(index++).getEventType());
    }

    // Starts moving, but stops longer than driver stop threshold reset
    @Test
    public void testProcessMessage_whenMovingAndStopsLongerThanDriveStopTheshold_ResetMoveStart() throws Exception {
        Thresholds thresholds = Thresholds.DEFAULT;
        thresholds.setDriveStartDistance(5);
        VehicleStateMachine stateMachine = new VehicleStateMachine(stubFeatureToggleService, thresholds);
        // Move 3 miles
        IHOSMessage moving = HosMessages.EngineOnMoving(datetimeFormat.parse("2016.09.19 13:00"));
        moving.setOdometer(1000);
        ArrayList<EventRecord> result =  stateMachine.processMessage(moving);

        // Stop
        IHOSMessage stopped = HosMessages.EngineOnStopped(datetimeFormat.parse("2016.09.19 13:03"));
        stopped.setOdometer(1003);
        result.addAll(stateMachine.processMessage(stopped));

        // Move 3 more miles
        IHOSMessage moving2 = HosMessages.EngineOnMoving(datetimeFormat.parse("2016.09.19 13:09"));
        moving2.setOdometer(1006);
        result.addAll(stateMachine.processMessage(moving2));

        Assert.assertTrue("Should be IGN_ON, STOP, MOVE, STOP, MOVE", result.size() == 5);

        int index = 0;
        Assert.assertEquals(EventTypeEnum.IGNITIONON, result.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.VEHICLESTOPPED, result.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.MOVE, result.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.VEHICLESTOPPED, result.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.MOVE, result.get(index++).getEventType());
    }


    @Test
    public void testProcessMessage_DriveonStartTimeComesFromMoveTime() throws Exception {
        Thresholds thresholds = Thresholds.DEFAULT;
        thresholds.setDriveStartDistance(5);
        VehicleStateMachine stateMachine = new VehicleStateMachine(stubFeatureToggleService, thresholds);
        // Move 3 miles
        IHOSMessage moving = HosMessages.EngineOnMoving(datetimeFormat.parse("2017.07.18 12:05"));
        moving.setOdometer(1000);
        ArrayList<EventRecord> result =  stateMachine.processMessage(moving);

        // Stop
        IHOSMessage stopped = HosMessages.EngineOnStopped(datetimeFormat.parse("2017.07.18  12:07"));
        stopped.setOdometer(1003);
        result.addAll(stateMachine.processMessage(stopped));

        // Move 3 more miles
        IHOSMessage moving2 = HosMessages.EngineOnMoving(datetimeFormat.parse("2017.07.18  12:08"));
        moving2.setOdometer(1006);
        result.addAll(stateMachine.processMessage(moving2));

        Assert.assertTrue("Should be IGN_ON, STOP, MOVE, STOP, MOVE, DRIVE_ON", result.size() == 6);

        Date timeOfInitialMoveOnTheDriveon = result.get(result.size()-1).getTripReportData().getDataTimecodeAsDate();
        Assert.assertEquals(datetimeFormat.parse("2017.07.18 12:05"), timeOfInitialMoveOnTheDriveon);
    }

    @Test
    public void testOricessMessage_TripRecordDateOfTheDriveonEventComesFromMoveEvent() throws  Exception {
        Thresholds thresholds = Thresholds.DEFAULT;
        thresholds.setDriveStartDistance(5);
        VehicleStateMachine stateMachine = new VehicleStateMachine(stubFeatureToggleService, thresholds);
        // Move 3 miles
        IHOSMessage moving = HosMessages.EngineOnMoving(datetimeFormat.parse("2017.07.18 12:05"));
        moving.setOdometer(1000);
        moving.setGpsLatitude(-12.34321f);
        moving.setGpsLongitude(34.1122f);
        ArrayList<EventRecord> result =  stateMachine.processMessage(moving);

        // Stop
        IHOSMessage stopped = HosMessages.EngineOnStopped(datetimeFormat.parse("2017.07.18  12:07"));
        stopped.setOdometer(1003);
        stopped.setGpsLatitude(-12.35555f);
        stopped.setGpsLongitude(34.12222f);
        result.addAll(stateMachine.processMessage(stopped));

        // Move 3 more miles
        IHOSMessage moving2 = HosMessages.EngineOnMoving(datetimeFormat.parse("2017.07.18  12:08"));
        moving2.setOdometer(1006);
        moving2.setGpsLatitude(-12.36666f);
        moving2.setGpsLongitude(34.13333f);

        result.addAll(stateMachine.processMessage(moving2));

        Assert.assertTrue("Should be IGN_ON, STOP, MOVE, STOP, MOVE, DRIVE_ON", result.size() == 6);

        Date timeOfInitialMoveOnTheDriveon = result.get(result.size()-1).getTripReportData().getDataTimecodeAsDate();
        Assert.assertEquals(datetimeFormat.parse("2017.07.18 12:05"), timeOfInitialMoveOnTheDriveon);
        Assert.assertEquals(result.get(result.size()-1).getTripReportData().getLatitude(), moving.getGpsLatitude(), 0.00001f);
        Assert.assertEquals(result.get(result.size()-1).getTripReportData().getLongitude(), moving.getGpsLongitude(), 0.00001f);
        Assert.assertEquals(result.get(result.size()-1).getTripReportData().getOdometer(), moving.getOdometer(), 0.001f);
    }



}