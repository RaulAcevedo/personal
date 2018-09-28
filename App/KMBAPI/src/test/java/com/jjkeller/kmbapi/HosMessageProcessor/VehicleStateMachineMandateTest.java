package com.jjkeller.kmbapi.HosMessageProcessor;

import android.util.Log;

import com.jjkeller.kmbapi.HosMessageProcessor.interfaces.IHOSMessage;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.VehicleStateMachine;
import com.jjkeller.kmbapi.featuretoggle.IFeatureToggleService;
import com.jjkeller.kmbapi.geotab.HosMessages;
import com.jjkeller.kmbapi.geotab.abstraction.base.HosProcessorStateMachineTestBase;
import com.jjkeller.kmbapi.kmbeobr.Constants;
import com.jjkeller.kmbapi.kmbeobr.EventRecord;
import com.jjkeller.kmbapi.kmbeobr.EventTypeEnum;
import com.jjkeller.kmbapi.kmbeobr.Thresholds;
import com.jjkeller.kmbapi.proxydata.EventTranslationBase;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import static org.mockito.Mockito.when;

/**
 * Created by ajb6442 on 11/7/17.
 */

public class VehicleStateMachineMandateTest extends HosProcessorStateMachineTestBase {
    @Mock
    IFeatureToggleService stubFeatureToggleService;

    public VehicleStateMachineMandateTest() throws Exception {
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(stubFeatureToggleService.getIsEldMandateEnabled()).thenReturn(true);
    }

    @Test
    public void test_CanDriveSimple() throws ParseException {
        Thresholds thresholds = Thresholds.DEFAULT;
        thresholds.setDriveStartDistance(1);
        VehicleStateMachine vsm = new VehicleStateMachine(stubFeatureToggleService, thresholds);

        // Drive
        IHOSMessage messageStart = HosMessages.EngineOnMoving(datetimeFormat.parse("2017.11.07 07:00"));
        messageStart.setOdometer(0);
        messageStart.setOdometerFromEngine(true);
        ArrayList<EventRecord> results =  vsm.processMessage(messageStart);

        // Stop after 5 miles
        IHOSMessage messageDrive5 = HosMessages.EngineOnStopped(datetimeFormat.parse("2017.11.07 07:01"));
        messageDrive5.setOdometerFromEngine(true);
        messageStart.setOdometer(5);
        messageStart.setOdometerFromEngine(true);
        results.addAll(vsm.processMessage(messageDrive5));

        Assert.assertTrue("Should be: IGN_ON, STOP, MOVE, DRIVE_ON, STOP", results.size() == 5);
    }

    @Test
    public void test_GpsUncertDistances_Simple() throws ParseException {
        Thresholds thresholds = Thresholds.DEFAULT;
        thresholds.setDriveStartDistance(1);
        VehicleStateMachine vsm = new VehicleStateMachine(stubFeatureToggleService, thresholds);

        // Drive starting @ 1000 miles odometer
        IHOSMessage messageStart = HosMessages.EngineOnMoving(datetimeFormat.parse("2017.11.07 07:00"));
        messageStart.setGpsValid(false);
        ArrayList<EventRecord> results =  vsm.processMessage(messageStart);

        // Stop after 3 miles
        IHOSMessage messageStop = HosMessages.EngineOnStopped(datetimeFormat.parse("2017.11.07 07:04"));
        messageStop.setGpsValid(false);
        messageStop.setOdometer(1003);
        messageStop.setOrigOdometer(messageStop.getOdometer() * Constants.KILOMETERS_PER_MILE);
        results.addAll(vsm.processMessage(messageStop));

        Float expectedMiles = 3f;
        float expectedMeters = expectedMiles * Constants.KILOMETERS_PER_MILE * 1000;
        float actualUncertDistance = results.get(results.size() -1).getStatusRecordData().getGpsUncertDistance();

        Assert.assertEquals(expectedMeters, actualUncertDistance, .1f);
        Assert.assertEquals(expectedMiles, EventTranslationBase.calculateDistanceSinceLastValidCoordinates(actualUncertDistance), .1f);

        // Move another 2 miles with GPS on.
        IHOSMessage messageStartAgain = HosMessages.EngineOnMoving(datetimeFormat.parse("2017.11.07 07:05"));
        messageStartAgain.setGpsValid(true);
        messageStartAgain.setOdometer(1005);
        messageStartAgain.setOrigOdometer(messageStop.getOdometer() * Constants.KILOMETERS_PER_MILE);
        results.addAll(vsm.processMessage(messageStartAgain));

        // Expecting no GPS uncertainty since GPS is valid
        expectedMeters = 0;
        expectedMiles = 0f;
        actualUncertDistance = results.get(results.size() -1).getStatusRecordData().getGpsUncertDistance();

        Assert.assertEquals(expectedMeters, actualUncertDistance, 0f);
        Assert.assertEquals(expectedMiles, EventTranslationBase.calculateDistanceSinceLastValidCoordinates(actualUncertDistance), .1f);
    }

    @Test
    public void test_GpsUncertDistances_Complex() throws ParseException {
        Thresholds thresholds = Thresholds.DEFAULT;
        thresholds.setDriveStartDistance(1);
        VehicleStateMachine vsm = new VehicleStateMachine(stubFeatureToggleService, thresholds);

        // Drive starting @ 1000 miles odometer with GPS
        IHOSMessage messageStart = HosMessages.EngineOnMoving(datetimeFormat.parse("2017.11.07 07:00"));
        ArrayList<EventRecord> results = vsm.processMessage(messageStart);

        // Stop after 3 miles with GPS
        IHOSMessage messageStop = HosMessages.EngineOnStopped(datetimeFormat.parse("2017.11.07 07:04"));
        messageStop.setGpsValid(true);
        messageStop.setOdometer(1003f);
        messageStop.setOrigOdometer(messageStop.getOdometer() * Constants.KILOMETERS_PER_MILE);
        results.addAll(vsm.processMessage(messageStop));

        // Turn off engine after half a mile without GPS
        IHOSMessage messageMove = HosMessages.EngineOnMoving(datetimeFormat.parse("2017.11.07 07:05"));
        messageMove.setGpsValid(false);
        messageMove.setOdometer(1003.5f);
        messageMove.setOrigOdometer(messageMove.getOdometer() * Constants.KILOMETERS_PER_MILE);
        results.addAll(vsm.processMessage(messageMove));

        IHOSMessage messageEngOff = HosMessages.EngineOff(datetimeFormat.parse("2017.11.07 07:06"));
        messageEngOff.setGpsValid(false);
        messageEngOff.setOdometer(1003.5f);
        messageEngOff.setOrigOdometer(messageEngOff.getOdometer() * Constants.KILOMETERS_PER_MILE);
        results.addAll(vsm.processMessage(messageEngOff));

        // Turn on engine and drive for 4.5 miles without GPS
        IHOSMessage messageEngOn = HosMessages.EngineOnMoving(datetimeFormat.parse("2017.11.07 07:16"));
        messageEngOn.setGpsValid(false);
        messageEngOn.setOdometer(1008f);
        messageEngOn.setOrigOdometer(messageEngOn.getOdometer() * Constants.KILOMETERS_PER_MILE);
        results.addAll(vsm.processMessage(messageEngOn));

        Float expectedMiles = 5f;
        float expectedMeters = expectedMiles * Constants.KILOMETERS_PER_MILE * 1000;
        float actualUncertDistance = results.get(results.size() -1).getStatusRecordData().getGpsUncertDistance();

        Assert.assertEquals(expectedMeters, actualUncertDistance, .1f);
        Assert.assertEquals(expectedMiles, EventTranslationBase.calculateDistanceSinceLastValidCoordinates(actualUncertDistance), .1f);
    }

    @Test
    public void testProcessMessage_whenOdometerIncreasesOverThresholdWithInterruptions_ReturnDriveOnEventAndTransitionToMovingDriving() throws Exception {
        Thresholds thresholds = Thresholds.DEFAULT;
        thresholds.setDriveStartDistance(5);
        VehicleStateMachine stateMachine = new VehicleStateMachine(stubFeatureToggleService, thresholds);
        // Move 3 miles
        IHOSMessage moving = HosMessages.EngineOnMoving(datetimeFormat.parse("2016.09.19 13:00"));
        moving.setOdometerFromEngine(true);
        moving.setOdometer(1000);
        ArrayList<EventRecord> result =  stateMachine.processMessage(moving);

        // Stop
        IHOSMessage stopped = HosMessages.EngineOnStopped(datetimeFormat.parse("2016.09.19 13:02"));
        stopped.setOdometerFromEngine(true);
        stopped.setOdometer(1003);
        result.addAll(stateMachine.processMessage(stopped));

        // Move 3 more miles
        IHOSMessage moving2 = HosMessages.EngineOnMoving(datetimeFormat.parse("2016.09.19 13:03"));
        moving2.setOdometerFromEngine(true);
        moving2.setOdometer(1006);
        result.addAll(stateMachine.processMessage(moving2));

        Assert.assertTrue("Should be IGN_ON, STOP, MOVE, DRIVE_ON, STOP, DRIVE_OFF, MOVE, DRIVE_ON", result.size() == 8);

        int index = 0;
        Assert.assertEquals(EventTypeEnum.IGNITIONON, result.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.VEHICLESTOPPED, result.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.MOVE, result.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.DRIVESTART, result.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.VEHICLESTOPPED, result.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.DRIVEEND, result.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.MOVE, result.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.DRIVESTART, result.get(index++).getEventType());
    }

    @Test
    public void testProcessMessage_whenMovingAndStopsLongerThanDriveStopTheshold_ResetMoveStart() throws Exception {
        Thresholds thresholds = Thresholds.DEFAULT;
        thresholds.setDriveStartDistance(5);
        VehicleStateMachine stateMachine = new VehicleStateMachine(stubFeatureToggleService, thresholds);
        // Move 3 miles
        IHOSMessage moving = HosMessages.EngineOnMoving(datetimeFormat.parse("2016.09.19 13:00"));
        moving.setOdometerFromEngine(true);
        moving.setOdometer(1000);
        ArrayList<EventRecord> result =  stateMachine.processMessage(moving);

        // Stop
        IHOSMessage stopped = HosMessages.EngineOnStopped(datetimeFormat.parse("2016.09.19 13:03"));
        stopped.setOdometerFromEngine(true);
        stopped.setOdometer(1003);
        result.addAll(stateMachine.processMessage(stopped));

        // Move 3 more miles
        IHOSMessage moving2 = HosMessages.EngineOnMoving(datetimeFormat.parse("2016.09.19 13:09"));
        moving2.setOdometerFromEngine(true);
        moving2.setOdometer(1006);
        result.addAll(stateMachine.processMessage(moving2));

        Assert.assertTrue("Should be IGN_ON, STOP, MOVE, DRIVE_ON, STOP, DRIVE_OFF, MOVE, DRIVE_ON", result.size() == 8);

        int index = 0;
        Assert.assertEquals(EventTypeEnum.IGNITIONON, result.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.VEHICLESTOPPED, result.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.MOVE, result.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.DRIVESTART, result.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.VEHICLESTOPPED, result.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.DRIVEEND, result.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.MOVE, result.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.DRIVESTART, result.get(index++).getEventType());
    }

    @Test
    public void testProcessMessage_DriveonStartTimeComesFromMoveTime() throws Exception {
        Thresholds thresholds = Thresholds.DEFAULT;
        thresholds.setDriveStartDistance(5);
        VehicleStateMachine stateMachine = new VehicleStateMachine(stubFeatureToggleService, thresholds);
        // Move 3 miles
        IHOSMessage moving = HosMessages.EngineOnMoving(datetimeFormat.parse("2017.07.18 12:05"));
        moving.setOdometerFromEngine(true);
        moving.setOdometer(1000);
        ArrayList<EventRecord> result =  stateMachine.processMessage(moving);

        // Stop
        IHOSMessage stopped = HosMessages.EngineOnStopped(datetimeFormat.parse("2017.07.18  12:07"));
        stopped.setOdometerFromEngine(true);
        stopped.setOdometer(1003);
        result.addAll(stateMachine.processMessage(stopped));

        // Move 3 more miles
        IHOSMessage moving2 = HosMessages.EngineOnMoving(datetimeFormat.parse("2017.07.18  12:08"));
        moving2.setOdometerFromEngine(true);
        moving2.setOdometer(1006);
        result.addAll(stateMachine.processMessage(moving2));

        Assert.assertTrue("Should be IGN_ON, STOP, MOVE, DRIVE_ON, STOP, DRIVE_OFF, MOVE, DRIVE_ON", result.size() == 8);

        EventRecord actualEventData = result.get(3);
        Date timeOfInitialMoveOnTheDriveon = actualEventData.getTripReportData().getDataTimecodeAsDate();

        Assert.assertEquals(datetimeFormat.parse("2017.07.18 12:05"), timeOfInitialMoveOnTheDriveon);
    }

    @Test
    public void testProcessMessage_TripRecordDateOfTheDriveonEventComesFromMoveEvent() throws  Exception {
        Thresholds thresholds = Thresholds.DEFAULT;
        thresholds.setDriveStartDistance(5);
        VehicleStateMachine stateMachine = new VehicleStateMachine(stubFeatureToggleService, thresholds);
        // Move 3 miles
        IHOSMessage moving = HosMessages.EngineOnMoving(datetimeFormat.parse("2017.07.18 12:05"));
        moving.setOdometerFromEngine(true);
        moving.setOdometer(1000);
        moving.setGpsLatitude(-12.34321f);
        moving.setGpsLongitude(34.1122f);
        ArrayList<EventRecord> result =  stateMachine.processMessage(moving);

        // Stop
        IHOSMessage stopped = HosMessages.EngineOnStopped(datetimeFormat.parse("2017.07.18  12:07"));
        stopped.setOdometerFromEngine(true);
        stopped.setOdometer(1003);
        stopped.setGpsLatitude(-12.35555f);
        stopped.setGpsLongitude(34.12222f);
        result.addAll(stateMachine.processMessage(stopped));

        // Move 3 more miles
        IHOSMessage moving2 = HosMessages.EngineOnMoving(datetimeFormat.parse("2017.07.18  12:08"));
        moving2.setOdometerFromEngine(true);
        moving2.setOdometer(1006);
        moving2.setGpsLatitude(-12.36666f);
        moving2.setGpsLongitude(34.13333f);

        result.addAll(stateMachine.processMessage(moving2));

        Assert.assertTrue("Should be IGN_ON, STOP, MOVE, DRIVE_ON, STOP, DRIVE_OFF, MOVE, DRIVE_ON", result.size() == 8);

        EventRecord actualEventData = result.get(3);
        Date timeOfInitialMoveOnTheDriveon = actualEventData.getTripReportData().getDataTimecodeAsDate();

        Assert.assertEquals(datetimeFormat.parse("2017.07.18 12:05"), timeOfInitialMoveOnTheDriveon);
        Assert.assertEquals(actualEventData.getTripReportData().getLatitude(), moving.getGpsLatitude(), 0.00001f);
        Assert.assertEquals(actualEventData.getTripReportData().getLongitude(), moving.getGpsLongitude(), 0.00001f);
        Assert.assertEquals(actualEventData.getTripReportData().getOdometer(), moving.getOdometer(), 0.001f);
    }

}
