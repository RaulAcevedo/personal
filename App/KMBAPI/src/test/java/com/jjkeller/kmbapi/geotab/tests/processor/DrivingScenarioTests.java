package com.jjkeller.kmbapi.geotab.tests.processor;

import android.util.EventLog;

import com.jjkeller.kmbapi.HosMessageProcessor.HosMessageProcessor;
import com.jjkeller.kmbapi.HosMessageProcessor.interfaces.IHOSMessage;
import com.jjkeller.kmbapi.featuretoggle.IFeatureToggleService;
import com.jjkeller.kmbapi.geotab.HosMessages;
import com.jjkeller.kmbapi.geotab.implementation.data.ScenarioSettings;
import com.jjkeller.kmbapi.geotab.implementation.engine.dataEngine.GeotabDataBuilder;
import com.jjkeller.kmbapi.geotab.implementation.enums.ScenarioSelectorEnum;
import com.jjkeller.kmbapi.geotabengine.HOSMessage;
import com.jjkeller.kmbapi.kmbeobr.EventRecord;
import com.jjkeller.kmbapi.kmbeobr.EventTypeEnum;
import com.jjkeller.kmbapi.kmbeobr.Thresholds;
import com.jjkeller.kmbapi.common.EngineModel;
import com.jjkeller.kmbapi.common.TestBase;

import junit.framework.Assert;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import static org.mockito.Mockito.when;

/**
 * Test class designed to test processing engine against various driving scenarios.
 */
@RunWith(MockitoJUnitRunner.class)
public class DrivingScenarioTests extends TestBase {

    //region DataGeneration
    public final float MILES_IN_LAT_DEGREE = 68.94f;
    public final float MILES_IN_LONG_DEGREE = 54.583f;
    public final int SECONDS_IN_HOUR = 3600;
    public final int RPM_PER_MPH = 10;
    public final int IDLE_RPM = 900;
    private final int MESSAGE_RATE_SECONDS = 2;
    //endregion

    TimeZone gmtTimeZone = TimeZone.getTimeZone("GMT");

    @Mock
    IFeatureToggleService stubFeatureToggleService;
    private EngineModel engine = new EngineModel(1000);

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        when(stubFeatureToggleService.getIsEldMandateEnabled()).thenReturn(false);
    }

    @Test
    public void test_DrivingScenario_Simple_ForAOBRD() {

        ArrayList<IHOSMessage> messages = new ArrayList<>();
        IHOSMessage tempHosM = new HOSMessage();

        //10/10/2016 10:10:00
        Date startingDate = new Date(1476094200000L);

        //engine off
        tempHosM.setTimestampUtc(new DateTime(startingDate));
        tempHosM.setGpsLatitude(34.7f);
        tempHosM.setGpsLongitude(40f);
        tempHosM.setSpeedometer(0);
        tempHosM.setTachometer(0);
        tempHosM.setOdometer(0);
        tempHosM.setTripOdometer(0);
        tempHosM.setEngineHours(0);
        tempHosM.setTripEngineSeconds(0);
        tempHosM.setGpsValid(true);
        tempHosM.setIgnitionOn(false);
        tempHosM.setEngineActivityDetected(false);
        tempHosM.setDatetimeValid(true);
        tempHosM.setSerialNumberCrc(5496);
        tempHosM.setSpeedFromEngine(true);
        tempHosM.setOdometerFromEngine(true);

        messages.add(tempHosM);

        //1min engine off
        for(int i = 0; i<30; i++){
            tempHosM = changesOver2sec(tempHosM);
            messages.add(tempHosM);
        }

        //IgnitionOn
        tempHosM = clone(tempHosM);
        tempHosM.setIgnitionOn(true);
        tempHosM.setTachometer(IDLE_RPM);
        tempHosM.setEngineActivityDetected(true);
        tempHosM = changesOver2sec(tempHosM);
        messages.add(tempHosM);

        //1min idling
        for(int i = 0; i<30; i++){
            tempHosM = changesOver2sec(tempHosM);
            messages.add(tempHosM);
        }

        //4sec 0-4
        ArrayList<IHOSMessage> tempAccel = acceleration(tempHosM, 4, 4);
        for(IHOSMessage message:tempAccel){
            messages.add(message);
            tempHosM = message;
        }
        //2sec 4-0
        tempAccel = acceleration(tempHosM, 2, 0);
        for(IHOSMessage message:tempAccel){
            messages.add(message);
            tempHosM = message;
        }

        //30sec idle
        for(int i = 0; i<15; i++){
            tempHosM = changesOver2sec(tempHosM);
            messages.add(tempHosM);
        }

        //1min 0-60
        tempAccel = acceleration(tempHosM, 60, 60);
        for(IHOSMessage message:tempAccel){
            messages.add(message);
            tempHosM = message;
        }

        //5min @60
        for(int i = 0; i<150; i++){
            tempHosM = changesOver2sec(tempHosM);
            messages.add(tempHosM);
        }

        //1min 60-0
        tempAccel = acceleration(tempHosM, 60, 0);
        for(IHOSMessage message:tempAccel){
            messages.add(message);
            tempHosM = message;
        }

        //30sec idle
        for(int i = 0; i<15; i++){
            tempHosM = changesOver2sec(tempHosM);
            messages.add(tempHosM);
        }

        //1min 0-60
        tempAccel = acceleration(tempHosM, 60, 60);
        for(IHOSMessage message:tempAccel){
            messages.add(message);
            tempHosM = message;
        }

        //3min @60
        for(int i = 0; i<90; i++){
            tempHosM = changesOver2sec(tempHosM);
            messages.add(tempHosM);
        }

        //1min 60-0
        tempAccel = acceleration(tempHosM, 60, 0);
        for(IHOSMessage message:tempAccel){
            messages.add(message);
            tempHosM = message;
        }

        //10min idle
        for(int i = 0; i<300; i++){
            tempHosM = changesOver2sec(tempHosM);
            messages.add(tempHosM);
        }

        //Ignition off
        tempHosM = clone(tempHosM);
        tempHosM.setIgnitionOn(false);
        tempHosM.setTachometer(0);
        tempHosM.setEngineActivityDetected(false);
        tempHosM = changesOver2sec(tempHosM);
        messages.add(tempHosM);

        //1min engine off
        for(int i = 0; i<30; i++){
            tempHosM = changesOver2sec(tempHosM);
            messages.add(tempHosM);
        }

        when(stubFeatureToggleService.getIsEldMandateEnabled()).thenReturn(true);
        HosMessageProcessor processor = new HosMessageProcessor(stubFeatureToggleService, Thresholds.DEFAULT);

        ArrayList<EventRecord> eventRecords = processMessages(messages, processor);

        /*
        HOSData
        @10:10:00
            engine off
            1min engine off
        @10:11:00
            2sec Ignition on
            1min idle
        @10:12:02
            4sec 0-4
            2sec 4-0
        @10:12:08
            30sec idle
        @10:12:38
            1min 0-60
            5min @60
            1min 60-0
        @10:19:38
            30sec idle
        @10:20:08
            1min 0-60
            3min @60
            1min 60-0
        @10:25:08
            10min idle
        @10:35:08
            2sec Ignition off
            1min engine off
        @10:36:10
        */

        int index = 0;
        Assert.assertEquals(EventTypeEnum.IGNITIONON,eventRecords.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.VEHICLESTOPPED, eventRecords.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.MOVE, eventRecords.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.VEHICLESTOPPED, eventRecords.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.MOVE, eventRecords.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.DRIVESTART, eventRecords.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.VEHICLESTOPPED, eventRecords.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.MOVE, eventRecords.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.VEHICLESTOPPED, eventRecords.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.DRIVEEND, eventRecords.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.IGNITIONOFF, eventRecords.get(index++).getEventType());
    }

    @Test
    public void test_DrivingScenario_Simple_ForMandate() {

        when(stubFeatureToggleService.getIsEldMandateEnabled()).thenReturn(true);

        ArrayList<IHOSMessage> messages = new ArrayList<>();
        IHOSMessage tempHosM = new HOSMessage();

        //10/10/2016 10:10:00
        Date startingDate = new Date(1476094200000L);

        //engine off
        tempHosM.setTimestampUtc(new DateTime(startingDate));
        tempHosM.setGpsLatitude(34.7f);
        tempHosM.setGpsLongitude(40f);
        tempHosM.setSpeedometer(0);
        tempHosM.setTachometer(0);
        tempHosM.setOdometer(0);
        tempHosM.setTripOdometer(0);
        tempHosM.setEngineHours(0);
        tempHosM.setTripEngineSeconds(0);
        tempHosM.setGpsValid(true);
        tempHosM.setIgnitionOn(false);
        tempHosM.setEngineActivityDetected(false);
        tempHosM.setDatetimeValid(true);
        tempHosM.setSerialNumberCrc(5496);
        tempHosM.setSpeedFromEngine(true);
        tempHosM.setOdometerFromEngine(true);


        messages.add(tempHosM);

        //1min engine off
        for(int i = 0; i<30; i++){
            tempHosM = changesOver2sec(tempHosM);
            messages.add(tempHosM);
        }

        //IgnitionOn
        tempHosM = clone(tempHosM);
        tempHosM.setIgnitionOn(true);
        tempHosM.setTachometer(IDLE_RPM);
        tempHosM.setEngineActivityDetected(true);
        tempHosM = changesOver2sec(tempHosM);
        messages.add(tempHosM);

        //1min idling
        for(int i = 0; i<30; i++){
            tempHosM = changesOver2sec(tempHosM);
            messages.add(tempHosM);
        }

        //4sec 0-4
        ArrayList<IHOSMessage> tempAccel = acceleration(tempHosM, 4, 4);
        for(IHOSMessage message:tempAccel){
            messages.add(message);
            tempHosM = message;
        }
        //2sec 4-0
        tempAccel = acceleration(tempHosM, 2, 0);
        for(IHOSMessage message:tempAccel){
            messages.add(message);
            tempHosM = message;
        }

        //30sec idle
        for(int i = 0; i<15; i++){
            tempHosM = changesOver2sec(tempHosM);
            messages.add(tempHosM);
        }

        //1min 0-60
        tempAccel = acceleration(tempHosM, 60, 60);
        for(IHOSMessage message:tempAccel){
            messages.add(message);
            tempHosM = message;
        }

        //5min @60
        for(int i = 0; i<150; i++){
            tempHosM = changesOver2sec(tempHosM);
            messages.add(tempHosM);
        }

        //1min 60-0
        tempAccel = acceleration(tempHosM, 60, 0);
        for(IHOSMessage message:tempAccel){
            messages.add(message);
            tempHosM = message;
        }

        //30sec idle
        for(int i = 0; i<15; i++){
            tempHosM = changesOver2sec(tempHosM);
            messages.add(tempHosM);
        }

        //1min 0-60
        tempAccel = acceleration(tempHosM, 60, 60);
        for(IHOSMessage message:tempAccel){
            messages.add(message);
            tempHosM = message;
        }

        //3min @60
        for(int i = 0; i<90; i++){
            tempHosM = changesOver2sec(tempHosM);
            messages.add(tempHosM);
        }

        //1min 60-0
        tempAccel = acceleration(tempHosM, 60, 0);
        for(IHOSMessage message:tempAccel){
            messages.add(message);
            tempHosM = message;
        }

        //10min idle
        for(int i = 0; i<300; i++){
            tempHosM = changesOver2sec(tempHosM);
            messages.add(tempHosM);
        }

        //Ignition off
        tempHosM = clone(tempHosM);
        tempHosM.setIgnitionOn(false);
        tempHosM.setTachometer(0);
        tempHosM.setEngineActivityDetected(false);
        tempHosM = changesOver2sec(tempHosM);
        messages.add(tempHosM);

        //1min engine off
        for(int i = 0; i<30; i++){
            tempHosM = changesOver2sec(tempHosM);
            messages.add(tempHosM);
        }

        when(stubFeatureToggleService.getIsEldMandateEnabled()).thenReturn(true);
        HosMessageProcessor processor = new HosMessageProcessor(stubFeatureToggleService, Thresholds.DEFAULT);

        ArrayList<EventRecord> eventRecords = processMessages(messages, processor);

        /*
        HOSData
        @10:10:00
            engine off
            1min engine off
        @10:11:00
            2sec Ignition on
            1min idle
        @10:12:02
            4sec 0-4
            2sec 4-0
        @10:12:08
            30sec idle
        @10:12:38
            1min 0-60
            5min @60
            1min 60-0
        @10:19:38
            30sec idle
        @10:20:08
            1min 0-60
            3min @60
            1min 60-0
        @10:25:08
            10min idle
        @10:35:08
            2sec Ignition off
            1min engine off
        @10:36:10
        */

        int index = 0;
        Assert.assertEquals(EventTypeEnum.IGNITIONON,eventRecords.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.VEHICLESTOPPED, eventRecords.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.MOVE, eventRecords.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.VEHICLESTOPPED, eventRecords.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.MOVE, eventRecords.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.DRIVESTART, eventRecords.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.VEHICLESTOPPED, eventRecords.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.MOVE, eventRecords.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.VEHICLESTOPPED, eventRecords.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.DRIVEEND, eventRecords.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.IGNITIONOFF, eventRecords.get(index).getEventType());
    }

    @Test
    public void test_DrivingScenario_Complete_ForAOBRD() {
        Now(2016,8,1,0,0,0, gmtTimeZone);

        // Arrange
        when(stubFeatureToggleService.getIsEldMandateEnabled()).thenReturn(false);
        HosMessageProcessor hosMessageProcessor = new HosMessageProcessor(stubFeatureToggleService, Thresholds.DEFAULT);
        ArrayList<IHOSMessage> ihosMessages = new ArrayList<>();
        // ==================
        // Drive Scenario 1 - Full Cycle Driving w/ Interrupted Drive_ON & w/ Interrupted Drive_OFF
        // ==================
        ihosMessages.addAll(generateMessages(HosMessages.EngineOff(Now()), 60));

        // T0 - Engine Started {KEY_STATE_OFF} -> [IGN_ON] -> {KEY_STATE_ON}
        // T1 - Stopped {KEY_STATE_ON} -> [STOP] -> {KEY_STATE_STOP}
        ihosMessages.addAll(generateMessages(HosMessages.EngineOnStopped(Now()), 60));

        // T2 - 10 min Begin Moving < Drive Threshold {KEY_STATE_STOPPED} -> [MOVING] -> {KEY_STATE_MOVING}
        ihosMessages.addAll(generateMessages(HosMessages.EngineOnMoving(Now()), 4, 55));

        // 1 Min Stop {KEY_STATE_MOVING} -> [STOP] -> {KEY_STATE_MOVING}
        ihosMessages.addAll(generateMessages(HosMessages.EngineOnStopped(Now()), 60));

        // T3 -  4 min Cross Speed Threshold to Start Driving (Speed > Speed Threshold) {KEY_STATE_MOVING} -> [MOVE,DRIVE_ON] -> {KEY_STATE_DRIVE_ON}
        ihosMessages.addAll(generateMessages(HosMessages.EngineOnMoving(Now()), 240, 60));

        // T4 - Stop for longer than Threshold (Stopped Moving < 5 Min) {KEY_STATE_DRIVE_ON} -> [STOP] -> {KEY_STATE_DRIVE_HALTED}
        ihosMessages.addAll(generateMessages(HosMessages.EngineOnStopped(Now()), 60));

        // Start Driving Again {KEY_STATE_DRIVE_HALTED} -> [MOVE] -> {KEY_STATE_DRIVE_ON}
        ihosMessages.addAll(generateMessages(HosMessages.EngineOnMoving(Now()), 240, 60));

        // Stop Again {KEY_STATE_DRIVE_ON} -> [STOP] -> {KEY_STATE_DRIVE_HALTED}
        // T5 - Stop for Stopped_Time >= Drive_End_Stop_Threshold {KEY_STATE_DRIVE_HALTED} -> [DRIVE_OFF] -> {KEY_STATE_DRIVE_OFF}
        ihosMessages.addAll(generateMessages(HosMessages.EngineOnStopped(Now()), 330));

        // T6 - Ignition Off  {KEY_STATE_DRIVE_OFF} -> [IGN_OFF] -> {KEY_STATE_OFF}
        ihosMessages.addAll(generateMessages(HosMessages.EngineOff(Now()), 200));

        // Act
        ArrayList<EventRecord> results1 = processMessages(ihosMessages, hosMessageProcessor);

        ArrayList<EventRecord> results = results1;
        // Assert
        int index = 0;
        // Scenario 1 - Full drive cycle w/ Interrupted Drive Start and w/ Interrupted Drive Stop
        Assert.assertEquals(EventTypeEnum.IGNITIONON, results.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.VEHICLESTOPPED, results.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.MOVE, results.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.VEHICLESTOPPED, results.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.MOVE, results.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.DRIVESTART, results.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.VEHICLESTOPPED, results.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.MOVE, results.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.VEHICLESTOPPED, results.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.DRIVEEND, results.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.IGNITIONOFF, results.get(index++).getEventType());


        // ==================
        // Drive Scenario 2 - Ign Interrupt form all states
        // ==================
        int index11 = 0;
        ihosMessages.clear();

        int index4 = index11;
        // Cycle 1 - Ign On
        ihosMessages.addAll(generateMessages(HosMessages.EngineOff(Now()), 60));
        ihosMessages.addAll(generateMessages(HosMessages.EngineOnStopped(Now()), 60));
        ihosMessages.addAll(generateMessages(HosMessages.EngineOff(Now()), 60));

        // Act
        ArrayList<EventRecord> results3 = processMessages(ihosMessages, hosMessageProcessor);

        // Cycle 1 - Ign On
        Assert.assertEquals(EventTypeEnum.IGNITIONON, results3.get(index4++).getEventType());
        Assert.assertEquals(EventTypeEnum.VEHICLESTOPPED, results3.get(index4++).getEventType());
        Assert.assertEquals(EventTypeEnum.IGNITIONOFF, results3.get(index4++).getEventType());

        index11 = 0;
        ihosMessages.clear();

        int index3 = index11;
        ArrayList<EventRecord> results2;

        // Cycle 2 - Move
        ihosMessages.add(HosMessages.EngineOff(Now()));
        Now(Now().getTime() + 120000);

        IHOSMessage engineOnMoving = HosMessages.EngineOnMoving(Now());
        engineOnMoving.setSpeedometer(4);
        ihosMessages.add(engineOnMoving);
        Now(Now().getTime() + 120000);
        engineOnMoving = HosMessages.EngineOnMoving(Now());
        engineOnMoving.setSpeedometer(4);
        ihosMessages.add(engineOnMoving);
        Now(Now().getTime() + 120000);
        ihosMessages.add(HosMessages.EngineOff(Now()));

        // Act
        results2 = processMessages(ihosMessages, hosMessageProcessor);

        // Cycle 2 - Move
        Assert.assertEquals(EventTypeEnum.IGNITIONON, results2.get(index3++).getEventType());
        Assert.assertEquals(EventTypeEnum.VEHICLESTOPPED, results2.get(index3++).getEventType());
        Assert.assertEquals(EventTypeEnum.MOVE, results2.get(index3++).getEventType());
        Assert.assertEquals(EventTypeEnum.IGNITIONOFF, results2.get(index3++).getEventType());
        Assert.assertEquals(EventTypeEnum.VEHICLESTOPPED, results2.get(index3++).getEventType());


        index11 = 0;
        ihosMessages.clear();

        int index2 = index11;
        ArrayList<EventRecord> results11;// Cycle 3 - DriveStart
        ihosMessages.addAll(generateMessages(HosMessages.EngineOff(Now()), 60));
        ihosMessages.addAll(generateMessages(HosMessages.EngineOnMoving(Now()), 330, 40));
        ihosMessages.addAll(generateMessages(HosMessages.EngineOff(Now()), 60));

        // Act
        results11 = processMessages(ihosMessages, hosMessageProcessor);

        // Cycle 3 - DriveStop
        Assert.assertEquals(EventTypeEnum.IGNITIONON, results11.get(index2++).getEventType());
        Assert.assertEquals(EventTypeEnum.VEHICLESTOPPED, results11.get(index2++).getEventType());
        Assert.assertEquals(EventTypeEnum.MOVE, results11.get(index2++).getEventType());
        Assert.assertEquals(EventTypeEnum.DRIVESTART, results11.get(index2++).getEventType());
        Assert.assertEquals(EventTypeEnum.IGNITIONOFF, results11.get(index2++).getEventType());
        Assert.assertEquals(EventTypeEnum.VEHICLESTOPPED, results11.get(index2++).getEventType());


        index11 = 0;
        ihosMessages.clear();

        int index1 = index11;
        ArrayList<EventRecord> results21;// Cycle 4 - DriveStop
        ihosMessages.addAll(generateMessages(HosMessages.EngineOff(Now()), 60));
        ihosMessages.addAll(generateMessages(HosMessages.EngineOnMoving(Now()), 330, 40));
        ihosMessages.addAll(generateMessages(HosMessages.EngineOnStopped(Now()), 330));
        ihosMessages.addAll(generateMessages(HosMessages.EngineOff(Now()), 60));

        // Act
        results21 = processMessages(ihosMessages, hosMessageProcessor);

        // Cycle 4 - DriveStop
        // From Cycle3 State
        Assert.assertEquals(EventTypeEnum.IGNITIONON, results21.get(index1++).getEventType());
        Assert.assertEquals(EventTypeEnum.VEHICLESTOPPED, results21.get(index1++).getEventType());
        Assert.assertEquals(EventTypeEnum.MOVE, results21.get(index1++).getEventType());
        Assert.assertEquals(EventTypeEnum.DRIVESTART, results21.get(index1++).getEventType());
        Assert.assertEquals(EventTypeEnum.VEHICLESTOPPED, results21.get(index1++).getEventType());
        Assert.assertEquals(EventTypeEnum.DRIVEEND, results21.get(index1++).getEventType());
        Assert.assertEquals(EventTypeEnum.IGNITIONOFF, results21.get(index1++).getEventType());
        ihosMessages.clear();
    }

    @Test
    public void test_DrivingScenario_Complete_ForMandate() {
        Now(2016,8,1,0,0,0, gmtTimeZone);

        // Arrange
        when(stubFeatureToggleService.getIsEldMandateEnabled()).thenReturn(true);
        HosMessageProcessor hosMessageProcessor = new HosMessageProcessor(stubFeatureToggleService, Thresholds.DEFAULT);
        ArrayList<IHOSMessage> ihosMessages = new ArrayList<>();

        // ==================
        // Drive Scenario 1 - Full Cycle Driving w/ Interrupted Drive_ON & w/ Interrupted Drive_OFF
        // ==================
        ihosMessages.addAll(generateMessages(HosMessages.EngineOff(Now()), 60));

        // T0 - Engine Started {KEY_STATE_OFF} -> [IGN_ON] -> {KEY_STATE_ON}
        // T1 - Stopped {KEY_STATE_ON} -> [STOP] -> {KEY_STATE_STOP}
        ihosMessages.addAll(generateMessages(HosMessages.EngineOnStopped(Now()), 60));

        // T2 - 10 min Begin Moving < Drive Threshold {KEY_STATE_STOPPED} -> [MOVING] -> {KEY_STATE_MOVING}
        ihosMessages.addAll(generateMessages(HosMessages.EngineOnMoving(Now()), 600, 4));

        // 1 Min Stop {KEY_STATE_MOVING} -> [STOP] -> {KEY_STATE_MOVING}
        ihosMessages.addAll(generateMessages(HosMessages.EngineOnStopped(Now()), 60));

        // T3 -  4 min Cross Speed Threshold to Start Driving (Speed > Speed Threshold) {KEY_STATE_MOVING} -> [MOVE,DRIVE_ON] -> {KEY_STATE_DRIVE_ON}
        ihosMessages.addAll(generateMessages(HosMessages.EngineOnMoving(Now()), 240, 60));

        // T4 - Stop for longer than Threshold (Stopped Moving < 5 Min) {KEY_STATE_DRIVE_ON} -> [STOP] -> {KEY_STATE_DRIVE_HALTED}
        ihosMessages.addAll(generateMessages(HosMessages.EngineOnStopped(Now()), 60));

        // Start Driving Again {KEY_STATE_DRIVE_HALTED} -> [MOVE] -> {KEY_STATE_DRIVE_ON}
        ihosMessages.addAll(generateMessages(HosMessages.EngineOnMoving(Now()), 240, 60));

        // Stop Again {KEY_STATE_DRIVE_ON} -> [STOP] -> {KEY_STATE_DRIVE_HALTED}
        // T5 - Stop for Stopped_Time >= Drive_End_Stop_Threshold {KEY_STATE_DRIVE_HALTED} -> [DRIVE_OFF] -> {KEY_STATE_DRIVE_OFF}
        ihosMessages.addAll(generateMessages(HosMessages.EngineOnStopped(Now()), 330));

        // T6 - Ignition Off  {KEY_STATE_DRIVE_OFF} -> [IGN_OFF] -> {KEY_STATE_OFF}
        ihosMessages.addAll(generateMessages(HosMessages.EngineOff(Now()), 200));

        // Act
        ArrayList<EventRecord> results1 = processMessages(ihosMessages, hosMessageProcessor);

        ArrayList<EventRecord> results = results1;
        // Assert
        int index = 0;
        // Scenario 1 - Full drive cycle w/ Interrupted Drive Start and w/ Interrupted Drive Stop
        Assert.assertEquals(EventTypeEnum.IGNITIONON, results.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.VEHICLESTOPPED, results.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.MOVE, results.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.VEHICLESTOPPED, results.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.MOVE, results.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.DRIVESTART, results.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.VEHICLESTOPPED, results.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.MOVE, results.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.VEHICLESTOPPED, results.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.DRIVEEND, results.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.IGNITIONOFF, results.get(index++).getEventType());


        // ==================
        // Drive Scenario 2 - Ign Interrupt form all states
        // ==================
        int index11 = 0;
        ihosMessages.clear();

        int index4 = index11;
        // Cycle 1 - Ign On
        ihosMessages.addAll(generateMessages(HosMessages.EngineOff(Now()), 60));
        ihosMessages.addAll(generateMessages(HosMessages.EngineOnStopped(Now()), 60));
        ihosMessages.addAll(generateMessages(HosMessages.EngineOff(Now()), 60));

        // Act
        ArrayList<EventRecord> results3 = processMessages(ihosMessages, hosMessageProcessor);

        // Cycle 1 - Ign On
        Assert.assertEquals(EventTypeEnum.IGNITIONON, results3.get(index4++).getEventType());
        Assert.assertEquals(EventTypeEnum.VEHICLESTOPPED, results3.get(index4++).getEventType());
        Assert.assertEquals(EventTypeEnum.IGNITIONOFF, results3.get(index4++).getEventType());

        index11 = 0;
        ihosMessages.clear();

        int index3 = index11;
        ArrayList<EventRecord> results2;

        // Cycle 2 - Move
        ihosMessages.add(HosMessages.EngineOff(Now()));
        Now(Now().getTime() + 120000);

        IHOSMessage engineOnMoving = HosMessages.EngineOnMoving(Now());
        engineOnMoving.setSpeedometer(4);
        ihosMessages.add(engineOnMoving);
        Now(Now().getTime() + 120000);
        engineOnMoving = HosMessages.EngineOnMoving(Now());
        engineOnMoving.setSpeedometer(4);
        ihosMessages.add(engineOnMoving);
        Now(Now().getTime() + 120000);
        ihosMessages.add(HosMessages.EngineOff(Now()));

        // Act
        results2 = processMessages(ihosMessages, hosMessageProcessor);

        // Cycle 2 - Move
        Assert.assertEquals(EventTypeEnum.IGNITIONON, results2.get(index3++).getEventType());
        Assert.assertEquals(EventTypeEnum.VEHICLESTOPPED, results2.get(index3++).getEventType());
        Assert.assertEquals(EventTypeEnum.MOVE, results2.get(index3++).getEventType());
        Assert.assertEquals(EventTypeEnum.IGNITIONOFF, results2.get(index3++).getEventType());
        Assert.assertEquals(EventTypeEnum.VEHICLESTOPPED, results2.get(index3++).getEventType());


        index11 = 0;
        ihosMessages.clear();

        int index2 = index11;
        ArrayList<EventRecord> results11;// Cycle 3 - DriveStart
        ihosMessages.addAll(generateMessages(HosMessages.EngineOff(Now()), 60));
        ihosMessages.addAll(generateMessages(HosMessages.EngineOnMoving(Now()), 330, 40));
        ihosMessages.addAll(generateMessages(HosMessages.EngineOff(Now()), 60));

        // Act
        results11 = processMessages(ihosMessages, hosMessageProcessor);

        // Cycle 3 - DriveStop
        Assert.assertEquals(EventTypeEnum.IGNITIONON, results11.get(index2++).getEventType());
        Assert.assertEquals(EventTypeEnum.VEHICLESTOPPED, results11.get(index2++).getEventType());
        Assert.assertEquals(EventTypeEnum.MOVE, results11.get(index2++).getEventType());
        Assert.assertEquals(EventTypeEnum.DRIVESTART, results11.get(index2++).getEventType());
        Assert.assertEquals(EventTypeEnum.IGNITIONOFF, results11.get(index2++).getEventType());
        Assert.assertEquals(EventTypeEnum.VEHICLESTOPPED, results11.get(index2++).getEventType());


        index11 = 0;
        ihosMessages.clear();

        int index1 = index11;
        ArrayList<EventRecord> results21;// Cycle 4 - DriveStop
        ihosMessages.addAll(generateMessages(HosMessages.EngineOff(Now()), 60));
        ihosMessages.addAll(generateMessages(HosMessages.EngineOnMoving(Now()), 330, 40));
        ihosMessages.addAll(generateMessages(HosMessages.EngineOnStopped(Now()), 330));
        ihosMessages.addAll(generateMessages(HosMessages.EngineOff(Now()), 60));

        // Act
        results21 = processMessages(ihosMessages, hosMessageProcessor);

        // Cycle 4 - DriveStop
        Assert.assertEquals(EventTypeEnum.DRIVEEND, results21.get(index1++).getEventType());  // From Cycle3 State
        Assert.assertEquals(EventTypeEnum.IGNITIONON, results21.get(index1++).getEventType());
        Assert.assertEquals(EventTypeEnum.VEHICLESTOPPED, results21.get(index1++).getEventType());
        Assert.assertEquals(EventTypeEnum.MOVE, results21.get(index1++).getEventType());
        Assert.assertEquals(EventTypeEnum.DRIVESTART, results21.get(index1++).getEventType());
        Assert.assertEquals(EventTypeEnum.VEHICLESTOPPED, results21.get(index1++).getEventType());
        Assert.assertEquals(EventTypeEnum.DRIVEEND, results21.get(index1++).getEventType());
        Assert.assertEquals(EventTypeEnum.IGNITIONOFF, results21.get(index1++).getEventType());
        ihosMessages.clear();
    }


    @Test
    public void test_DrivingScenario_Normal_ForAOBRD() {
        //Assemble
        Now(2016, 8, 1, 0, 0, 0, gmtTimeZone);
        IHOSMessage initialMessage = HosMessages.EngineOnStopped(Now());
        ScenarioSettings settings = new ScenarioSettings(initialMessage, 2.0f);

        // Arrange
        when(stubFeatureToggleService.getIsEldMandateEnabled()).thenReturn(true);
        HosMessageProcessor hosMessageProcessor = new HosMessageProcessor(stubFeatureToggleService, Thresholds.DEFAULT);


        //Act
        GeotabDataBuilder scenarioBuilder = GeotabDataBuilder.getDataBuilder()
                .usingDataOperationScheme(GeotabDataBuilder.DataOperationScheme.BASIC)
                .setScenarioType(ScenarioSelectorEnum.DRIVING_CYCLE_NORMAL)
                .withProvidedMessage(initialMessage)
                .initializeScenario(settings)
                .generateScenario();
        //Get ordered scenario
        ArrayList<IHOSMessage> scenario = new ArrayList<>(scenarioBuilder.getScenarioAsMessages().values());
        //Emit the scenario description to the console
        System.out.print(scenarioBuilder.getCurrentScenarioManager().getCurrentScenarioDescription(true));
        ArrayList<EventRecord> results = this.processMessages(scenario, hosMessageProcessor);


        //Assert
        //Assert the first record is IGN_ON
        Assert.assertEquals("Assert that the first event type of the first event record return is IGN_ON.", EventTypeEnum.IGNITIONON, results.get(0).getEventType());
        //Assert next record is VEHICLESTOPPED
        Assert.assertEquals("Assert that the first event type after IGN_ON is a VEHICLESTOPPED event.", EventTypeEnum.VEHICLESTOPPED, results.get(1).getEventType());
        //Assert next record is move
        Assert.assertEquals("Assert MOVE event after stopped period.", EventTypeEnum.MOVE, results.get(2).getEventType());
        //We accelerate up to 30 MPH over a minute, putting us over the drive on threshold
        Assert.assertEquals("Assert DRIVE_ON event after acceleration to 30 MPH.", EventTypeEnum.DRIVESTART, results.get(3).getEventType());
        //We decelerate down to 0 MPH over a minute, Assert next event is VEHICLE STOPPED
        Assert.assertEquals("Assert VEHICLESTOPPED event after deceleration to 0 MPH.", EventTypeEnum.VEHICLESTOPPED, results.get(4).getEventType());
        /*
        We are currently in VEHICLESTOPPED, but will begin to accelerate to 60 MPH
        A MOVE event will fire as soon as we start moving again (simulating stopping at stopsign,etc)
         */
        Assert.assertEquals("Assert MOVE event after acceleration to 60 MPH.", EventTypeEnum.MOVE, results.get(5).getEventType());
        /*
        At 60 MPH, we drive for a while (5 min), and then begin to decelerate to 0 over 2 minutes
         */
        Assert.assertEquals("Assert VEHICLESTOPPED event after return to stopped state.", EventTypeEnum.VEHICLESTOPPED, results.get(6).getEventType());
        /*
        At rest, the next event should end the driving period
         */
        Assert.assertEquals("Assert DRIVE_OFF event after drive off timer has been satisfied.", EventTypeEnum.DRIVEEND, results.get(7).getEventType());
    }

    @Test
    public void test_DrivingScenario_Normal_ForMandate() {
        //Assemble
        Now(2016, 8, 1, 0, 0, 0, gmtTimeZone);
        IHOSMessage initialMessage = HosMessages.EngineOnStopped(Now());
        ScenarioSettings settings = new ScenarioSettings(initialMessage, 2.0f);

        // Arrange
        when(stubFeatureToggleService.getIsEldMandateEnabled()).thenReturn(true);
        HosMessageProcessor hosMessageProcessor = new HosMessageProcessor(stubFeatureToggleService, Thresholds.DEFAULT);


        //Act
        GeotabDataBuilder scenarioBuilder = GeotabDataBuilder.getDataBuilder()
                .usingDataOperationScheme(GeotabDataBuilder.DataOperationScheme.BASIC)
                .setScenarioType(ScenarioSelectorEnum.DRIVING_CYCLE_NORMAL)
                .withProvidedMessage(initialMessage)
                .initializeScenario(settings)
                .generateScenario();
        //Get ordered scenario
        ArrayList<IHOSMessage> scenario = new ArrayList<>(scenarioBuilder.getScenarioAsMessages().values());
        //Emit the scenario description to the console
        System.out.print(scenarioBuilder.getCurrentScenarioManager().getCurrentScenarioDescription(true));
        ArrayList<EventRecord> results = this.processMessages(scenario, hosMessageProcessor);


        //Assert
        //Assert the first record is IGN_ON
        Assert.assertEquals("Assert that the first event type of the first event record return is IGN_ON.", EventTypeEnum.IGNITIONON, results.get(0).getEventType());
        //Assert next record is VEHICLESTOPPED
        Assert.assertEquals("Assert that the first event type of the first event record return is IGN_ON.", EventTypeEnum.VEHICLESTOPPED, results.get(1).getEventType());
        //Assert next record is move
        Assert.assertEquals("Assert MOVE event after stopped period.", EventTypeEnum.MOVE, results.get(2).getEventType());
        //We accelerate up to 30 MPH over a minute, putting us over the drive on threshold
        Assert.assertEquals("Assert DRIVE_ON event after acceleration to 30 MPH.", EventTypeEnum.DRIVESTART, results.get(3).getEventType());
        //We decelerate down to 0 MPH over a minute, Assert next event is VEHICLE STOPPED
        Assert.assertEquals("Assert VEHICLESTOPPED event after deceleration to 0 MPH.", EventTypeEnum.VEHICLESTOPPED, results.get(4).getEventType());
        /*
        We are currently in VEHICLESTOPPED, but will begin to accelerate to 60 MPH
        A MOVE event will fire as soon as we start moving again (simulating stopping at stopsign,etc)
         */
        Assert.assertEquals("Assert MOVE event after acceleration to 60 MPH.", EventTypeEnum.MOVE, results.get(5).getEventType());
        /*
        At 60 MPH, we drive for a while (5 min), and then begin to decelerate to 0 over 2 minutes
         */
        Assert.assertEquals("Assert VEHICLESTOPPED event after return to stopped state.", EventTypeEnum.VEHICLESTOPPED, results.get(6).getEventType());
        /*
        At rest, the next event should end the driving period
         */
        Assert.assertEquals("Assert DRIVE_OFF event after drive off timer has been satisfied.", EventTypeEnum.DRIVEEND, results.get(7).getEventType());
    }

    //@Test
    @Ignore
    public void test_DrivingScenario_Interrupted_Start_ForAOBRD() {
        //Assemble
        Now(2016, 8, 1, 0, 0, 0, gmtTimeZone);
        IHOSMessage initialMessage = HosMessages.EngineOnStopped(Now());
        ScenarioSettings settings = new ScenarioSettings(initialMessage, 2.0f);

        // Arrange
        when(stubFeatureToggleService.getIsEldMandateEnabled()).thenReturn(true);
        HosMessageProcessor hosMessageProcessor = new HosMessageProcessor(stubFeatureToggleService, Thresholds.DEFAULT);


        //Act
        GeotabDataBuilder scenarioBuilder = GeotabDataBuilder.getDataBuilder()
                .usingDataOperationScheme(GeotabDataBuilder.DataOperationScheme.BASIC)
                .setScenarioType(ScenarioSelectorEnum.START_CYCLE_INTERRUPTED)
                .withProvidedMessage(initialMessage)
                .initializeScenario(settings)
                .generateScenario();
        //Get ordered scenario
        ArrayList<IHOSMessage> scenario = new ArrayList<>(scenarioBuilder.getScenarioAsMessages().values());
        //Emit the scenario description to the console
        System.out.print(scenarioBuilder.getCurrentScenarioManager().getCurrentScenarioDescription(true));
        ArrayList<EventRecord> results = this.processMessages(scenario, hosMessageProcessor);

        //Assert
        //Assert the first record is IGN_ON
        Assert.assertEquals("Assert that the first event type of the first event record return is IGN_ON.", EventTypeEnum.IGNITIONON, results.get(0).getEventType());
        //Assert next record is VEHICLESTOPPED
        Assert.assertEquals("Assert that the first event after IGN_ON is a VEHICLESTOPPED event.", EventTypeEnum.VEHICLESTOPPED, results.get(1).getEventType());
        /*
        We now want to assert that the loop properly generates move : stop combinations correctly.
        For four (4) iterations, we expect 8 total events, four of each
         */
        //Start at the next position
        int interruptLoopAssertIndex = 2;
        boolean isStop = false;
        do {
            Assert.assertEquals("Assert that this record is a " + (isStop ? EventTypeEnum.VEHICLESTOPPED : EventTypeEnum.MOVE) + " EventRecord", isStop ? EventTypeEnum.VEHICLESTOPPED : EventTypeEnum.MOVE, results.get(interruptLoopAssertIndex).getEventType());
            isStop = !isStop;
            interruptLoopAssertIndex++;
        } while (interruptLoopAssertIndex < 10);
        /*
        After the above interrupt loop, we start driving again, and expect:
        A MOVE event followed immediately by a DRIVE_ON event
         */
        Assert.assertEquals("Assert the first event after interrupt loop is a MOVE event.", EventTypeEnum.MOVE, results.get(interruptLoopAssertIndex).getEventType());
        interruptLoopAssertIndex++;
        Assert.assertEquals("Assert the next event is a DRIVE_ON event.", EventTypeEnum.DRIVESTART, results.get(interruptLoopAssertIndex).getEventType());
        interruptLoopAssertIndex++;
        Assert.assertEquals("Assert that the next event is VEHICLESTOPPED", EventTypeEnum.VEHICLESTOPPED, results.get(interruptLoopAssertIndex).getEventType());
        interruptLoopAssertIndex++;
        Assert.assertEquals("Assert that the next event is DRIVE_OFF from 5 min of stopped time.", EventTypeEnum.DRIVEEND, results.get(interruptLoopAssertIndex).getEventType());
        interruptLoopAssertIndex++;
        Assert.assertEquals("Assert that the next event generated due to movement is a MOVE event.", EventTypeEnum.MOVE, results.get(interruptLoopAssertIndex).getEventType());
        interruptLoopAssertIndex++;
        Assert.assertEquals("Assert that the last event generated due to movement is a DRIVE_ON event.", EventTypeEnum.DRIVESTART, results.get(interruptLoopAssertIndex).getEventType());
    }

    @Test
    public void test_DrivingScenario_Interrupted_End_ForAOBRD() {

        ArrayList<IHOSMessage> messages = new ArrayList<>();
        IHOSMessage tempHosM = new HOSMessage();

        //10/10/2016 10:10:00
        Date startingDate = new Date(1476094200000L);

        //engine off
        tempHosM.setTimestampUtc(new DateTime(startingDate));
        tempHosM.setGpsLatitude(34.7f);
        tempHosM.setGpsLongitude(40f);
        tempHosM.setSpeedometer(0);
        tempHosM.setTachometer(0);
        tempHosM.setOdometer(0);
        tempHosM.setTripOdometer(0);
        tempHosM.setEngineHours(0);
        tempHosM.setTripEngineSeconds(0);
        tempHosM.setGpsValid(true);
        tempHosM.setIgnitionOn(false);
        tempHosM.setEngineActivityDetected(false);
        tempHosM.setDatetimeValid(true);
        tempHosM.setSerialNumberCrc(5496);

        messages.add(tempHosM);

        //1min engine off
        for(int i = 0; i<30; i++){
            tempHosM = changesOver2sec(tempHosM);
            messages.add(tempHosM);
        }

        //IgnitionOn
        tempHosM = clone(tempHosM);
        tempHosM.setIgnitionOn(true);
        tempHosM.setTachometer(IDLE_RPM);
        tempHosM.setEngineActivityDetected(true);
        tempHosM = changesOver2sec(tempHosM);
        messages.add(tempHosM);

        //1min idling
        for(int i = 0; i<30; i++){
            tempHosM = changesOver2sec(tempHosM);
            messages.add(tempHosM);
        }

        //1min 0-60
        ArrayList<IHOSMessage> tempAccel = acceleration(tempHosM, 60, 60);
        for(IHOSMessage message:tempAccel){
            messages.add(message);
            tempHosM = message;
        }

        //5min @60
        for(int i = 0; i<150; i++){
            tempHosM = changesOver2sec(tempHosM);
            messages.add(tempHosM);
        }

        //1min 60-0
        tempAccel = acceleration(tempHosM, 60, 0);
        for(IHOSMessage message:tempAccel){
            messages.add(message);
            tempHosM = message;
        }

        //30sec idle
        for(int i = 0; i<15; i++){
            tempHosM = changesOver2sec(tempHosM);
            messages.add(tempHosM);
        }

        //1min 0-60
        tempAccel = acceleration(tempHosM, 60, 60);
        for(IHOSMessage message:tempAccel){
            messages.add(message);
            tempHosM = message;
        }

        //3min @60
        for(int i = 0; i<90; i++){
            tempHosM = changesOver2sec(tempHosM);
            messages.add(tempHosM);
        }

        //1min 60-0
        tempAccel = acceleration(tempHosM, 60, 0);
        for(IHOSMessage message:tempAccel){
            messages.add(message);
            tempHosM = message;
        }

        //10min idle
        for(int i = 0; i<300; i++){
            tempHosM = changesOver2sec(tempHosM);
            messages.add(tempHosM);
        }

        //Ignition off
        tempHosM = clone(tempHosM);
        tempHosM.setIgnitionOn(false);
        tempHosM.setTachometer(0);
        tempHosM.setEngineActivityDetected(false);
        tempHosM = changesOver2sec(tempHosM);
        messages.add(tempHosM);

        //1min engine off
        for(int i = 0; i<30; i++){
            tempHosM = changesOver2sec(tempHosM);
            messages.add(tempHosM);
        }

        HosMessageProcessor processor = new HosMessageProcessor(stubFeatureToggleService, Thresholds.DEFAULT);

        ArrayList<EventRecord> eventRecords = processMessages(messages, processor);

        /*
        HOSData
        @10:10:00
            engine off
            1min engine off
        @10:11:00
            2sec Ignition on
            1min idle
        @10:12:02
            1min 0-60
            5min @60
            1min 60-0
        @10:19:02
            30sec idle
        @10:19:32
            1min 0-60
            3min @60
            1min 60-0
        @10:24:32
            10min idle
        @10:34:32
            2sec Ignition off
            1min engine off
        @10:35:34
        */


        int index = 0;
        Assert.assertEquals(EventTypeEnum.IGNITIONON,eventRecords.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.VEHICLESTOPPED, eventRecords.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.MOVE, eventRecords.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.DRIVESTART, eventRecords.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.VEHICLESTOPPED, eventRecords.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.MOVE, eventRecords.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.VEHICLESTOPPED, eventRecords.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.DRIVEEND, eventRecords.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.IGNITIONOFF, eventRecords.get(index).getEventType());
    }

    @Test
    public void test_DrivingScenario_Interrupted_End_ForMandate() {

        when(stubFeatureToggleService.getIsEldMandateEnabled()).thenReturn(true);

        ArrayList<IHOSMessage> messages = new ArrayList<>();
        IHOSMessage tempHosM = new HOSMessage();

        //10/10/2016 10:10:00
        Date startingDate = new Date(1476094200000L);

        //engine off
        tempHosM.setTimestampUtc(new DateTime(startingDate));
        tempHosM.setGpsLatitude(34.7f);
        tempHosM.setGpsLongitude(40f);
        tempHosM.setSpeedometer(0);
        tempHosM.setTachometer(0);
        tempHosM.setOdometer(0);
        tempHosM.setTripOdometer(0);
        tempHosM.setEngineHours(0);
        tempHosM.setTripEngineSeconds(0);
        tempHosM.setGpsValid(true);
        tempHosM.setIgnitionOn(false);
        tempHosM.setEngineActivityDetected(false);
        tempHosM.setDatetimeValid(true);
        tempHosM.setSerialNumberCrc(5496);
        tempHosM.setSpeedFromEngine(true);
        tempHosM.setOdometerFromEngine(true);

        messages.add(tempHosM);

        //1min engine off
        for(int i = 0; i<30; i++){
            tempHosM = changesOver2sec(tempHosM);
            messages.add(tempHosM);
        }

        //IgnitionOn
        tempHosM = clone(tempHosM);
        tempHosM.setIgnitionOn(true);
        tempHosM.setTachometer(IDLE_RPM);
        tempHosM.setEngineActivityDetected(true);
        tempHosM = changesOver2sec(tempHosM);
        messages.add(tempHosM);

        //1min idling
        for(int i = 0; i<30; i++){
            tempHosM = changesOver2sec(tempHosM);
            messages.add(tempHosM);
        }

        //1min 0-60
        ArrayList<IHOSMessage> tempAccel = acceleration(tempHosM, 60, 60);
        for(IHOSMessage message:tempAccel){
            messages.add(message);
            tempHosM = message;
        }

        //5min @60
        for(int i = 0; i<150; i++){
            tempHosM = changesOver2sec(tempHosM);
            messages.add(tempHosM);
        }

        //1min 60-0
        tempAccel = acceleration(tempHosM, 60, 0);
        for(IHOSMessage message:tempAccel){
            messages.add(message);
            tempHosM = message;
        }

        //1min idle
        for(int i = 0; i<15; i++){
            tempHosM = changesOver2sec(tempHosM);
            messages.add(tempHosM);
        }

        //1min 0-60
        tempAccel = acceleration(tempHosM, 60, 60);
        for(IHOSMessage message:tempAccel){
            messages.add(message);
            tempHosM = message;
        }

        //3min @60
        for(int i = 0; i<90; i++){
            tempHosM = changesOver2sec(tempHosM);
            messages.add(tempHosM);
        }

        //1min 60-0
        tempAccel = acceleration(tempHosM, 60, 0);
        for(IHOSMessage message:tempAccel){
            messages.add(message);
            tempHosM = message;
        }

        //10min idle
        for(int i = 0; i<300; i++){
            tempHosM = changesOver2sec(tempHosM);
            messages.add(tempHosM);
        }

        //Ignition off
        tempHosM = clone(tempHosM);
        tempHosM.setIgnitionOn(false);
        tempHosM.setTachometer(0);
        tempHosM.setEngineActivityDetected(false);
        tempHosM = changesOver2sec(tempHosM);
        messages.add(tempHosM);

        //1min engine off
        for(int i = 0; i<30; i++){
            tempHosM = changesOver2sec(tempHosM);
            messages.add(tempHosM);
        }

        HosMessageProcessor processor = new HosMessageProcessor(stubFeatureToggleService, Thresholds.DEFAULT);

        ArrayList<EventRecord> eventRecords = processMessages(messages, processor);

        /*
        HOSData
        @10:10:00
            engine off
            1min engine off
        @10:11:00
            2sec Ignition on
            1min idle
        @10:12:02
            1min 0-60
            5min @60
            1min 60-0
        @10:19:02
            30sec idle
        @10:19:32
            1min 0-60
            3min @60
            1min 60-0
        @10:24:32
            10min idle
        @10:34:32
            2sec Ignition off
            1min engine off
        @10:35:34
        */

        int index = 0;
        Assert.assertEquals(EventTypeEnum.IGNITIONON,eventRecords.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.VEHICLESTOPPED, eventRecords.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.MOVE, eventRecords.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.DRIVESTART, eventRecords.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.VEHICLESTOPPED, eventRecords.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.MOVE, eventRecords.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.VEHICLESTOPPED, eventRecords.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.DRIVEEND, eventRecords.get(index++).getEventType());
        Assert.assertEquals(EventTypeEnum.IGNITIONOFF, eventRecords.get(index).getEventType());
    }

    // @Test
    @Ignore
    public void test_DrivingScenario_Ignition_Terminated_ForAOBRD() {
        //Assemble
        Now(2016, 8, 1, 0, 0, 0, gmtTimeZone);
        IHOSMessage initialMessage = HosMessages.EngineOnStopped(Now());
        ScenarioSettings settings = new ScenarioSettings(initialMessage, 2.0f);

        // Arrange
        when(stubFeatureToggleService.getIsEldMandateEnabled()).thenReturn(false);
        HosMessageProcessor hosMessageProcessor = new HosMessageProcessor(stubFeatureToggleService, Thresholds.DEFAULT);


        //Act
        GeotabDataBuilder scenarioBuilder = GeotabDataBuilder.getDataBuilder()
                .usingDataOperationScheme(GeotabDataBuilder.DataOperationScheme.BASIC)
                .setScenarioType(ScenarioSelectorEnum.DRIVING_CYCLE_IGN_TERMINATED)
                .withProvidedMessage(initialMessage)
                .initializeScenario(settings)
                .generateScenario();
        //Get ordered scenario
        ArrayList<IHOSMessage> scenario = new ArrayList<>(scenarioBuilder.getScenarioAsMessages().values());
        System.out.print(scenarioBuilder.getCurrentScenarioManager().getCurrentScenarioDescription(true));
        ArrayList<EventRecord> results = this.processMessages(scenario, hosMessageProcessor);
        Assert.assertEquals(EventTypeEnum.IGNITIONON, results.get(0).getEventType());
        Assert.assertEquals(EventTypeEnum.VEHICLESTOPPED, results.get(1).getEventType());
        Assert.assertEquals(EventTypeEnum.MOVE, results.get(2).getEventType());
        Assert.assertEquals(EventTypeEnum.DRIVESTART, results.get(3).getEventType());
        Assert.assertEquals(EventTypeEnum.IGNITIONOFF, results.get(4).getEventType());
        Assert.assertEquals(EventTypeEnum.VEHICLESTOPPED, results.get(5).getEventType());
        Assert.assertEquals(EventTypeEnum.DRIVEEND, results.get(6).getEventType());
        Assert.assertEquals(EventTypeEnum.IGNITIONON, results.get(7).getEventType());
        Assert.assertEquals(EventTypeEnum.VEHICLESTOPPED, results.get(8).getEventType());
        Assert.assertEquals(EventTypeEnum.MOVE, results.get(9).getEventType());
        Assert.assertEquals(EventTypeEnum.DRIVESTART, results.get(10).getEventType());
        Assert.assertEquals(EventTypeEnum.IGNITIONOFF, results.get(11).getEventType());
        Assert.assertEquals(EventTypeEnum.VEHICLESTOPPED, results.get(12).getEventType());
        Assert.assertEquals(EventTypeEnum.DRIVEEND, results.get(13).getEventType());
        Assert.assertEquals(EventTypeEnum.IGNITIONON, results.get(14).getEventType());
        Assert.assertEquals(EventTypeEnum.VEHICLESTOPPED, results.get(15).getEventType());
    }


    //@Test
    @Ignore
    public void test_DrivingScenario_Ignition_Terminated_ForMandate() {
        //Assemble
        Now(2016, 8, 1, 0, 0, 0, gmtTimeZone);
        IHOSMessage initialMessage = HosMessages.EngineOnStopped(Now());
        ScenarioSettings settings = new ScenarioSettings(initialMessage, 2.0f);

        // Arrange
        when(stubFeatureToggleService.getIsEldMandateEnabled()).thenReturn(true);
        HosMessageProcessor hosMessageProcessor = new HosMessageProcessor(stubFeatureToggleService, Thresholds.DEFAULT);


        //Act
        GeotabDataBuilder scenarioBuilder = GeotabDataBuilder.getDataBuilder()
                .usingDataOperationScheme(GeotabDataBuilder.DataOperationScheme.BASIC)
                .setScenarioType(ScenarioSelectorEnum.DRIVING_CYCLE_IGN_TERMINATED)
                .withProvidedMessage(initialMessage)
                .initializeScenario(settings)
                .generateScenario();
        //Get ordered scenario
        ArrayList<IHOSMessage> scenario = new ArrayList<>(scenarioBuilder.getScenarioAsMessages().values());
        //Emit the scenario description to the console
        System.out.print(scenarioBuilder.getCurrentScenarioManager().getCurrentScenarioDescription(true));
        ArrayList<EventRecord> results = this.processMessages(scenario, hosMessageProcessor);
        Assert.assertEquals(EventTypeEnum.IGNITIONON, results.get(0).getEventType());
        Assert.assertEquals(EventTypeEnum.VEHICLESTOPPED, results.get(1).getEventType());
        Assert.assertEquals(EventTypeEnum.MOVE, results.get(2).getEventType());
        Assert.assertEquals(EventTypeEnum.DRIVESTART, results.get(3).getEventType());
        Assert.assertEquals(EventTypeEnum.IGNITIONOFF, results.get(4).getEventType());
        Assert.assertEquals(EventTypeEnum.VEHICLESTOPPED, results.get(5).getEventType());
        Assert.assertEquals(EventTypeEnum.IGNITIONON, results.get(6).getEventType());
        Assert.assertEquals(EventTypeEnum.VEHICLESTOPPED, results.get(7).getEventType());
        Assert.assertEquals(EventTypeEnum.MOVE, results.get(8).getEventType());
        Assert.assertEquals(EventTypeEnum.IGNITIONOFF, results.get(9).getEventType());
        Assert.assertEquals(EventTypeEnum.VEHICLESTOPPED, results.get(10).getEventType());
        Assert.assertEquals(EventTypeEnum.DRIVEEND, results.get(11).getEventType());
        Assert.assertEquals(EventTypeEnum.IGNITIONON, results.get(12).getEventType());
        Assert.assertEquals(EventTypeEnum.VEHICLESTOPPED, results.get(13).getEventType());
        Assert.assertEquals(EventTypeEnum.MOVE, results.get(14).getEventType());
        Assert.assertEquals(EventTypeEnum.DRIVESTART, results.get(15).getEventType());
    }

    @Test
    public void test_DrivingScenario_SpeedFromEngineFlags_ForMandate() {

        when(stubFeatureToggleService.getIsEldMandateEnabled()).thenReturn(true);

        ArrayList<IHOSMessage> messages = new ArrayList<>();
        IHOSMessage tempHosM = new HOSMessage();

        //10/10/2016 10:10:00
        Date startingDate = new Date(1476094200000L);

        //engine off
        tempHosM.setTimestampUtc(new DateTime(startingDate));
        tempHosM.setGpsLatitude(34.7f);
        tempHosM.setGpsLongitude(40f);
        tempHosM.setSpeedometer(0);
        tempHosM.setTachometer(0);
        tempHosM.setOdometer(0);
        tempHosM.setTripOdometer(0);
        tempHosM.setEngineHours(0);
        tempHosM.setTripEngineSeconds(0);
        tempHosM.setGpsValid(true);
        tempHosM.setIgnitionOn(false);
        tempHosM.setEngineActivityDetected(false);
        tempHosM.setDatetimeValid(true);
        tempHosM.setSerialNumberCrc(5496);
        tempHosM.setOdometerFromEngine(true);
        tempHosM.setSpeedFromEngine(true);

        messages.add(tempHosM);

        //1min engine off
        for(int i = 0; i<30; i++){
            tempHosM = changesOver2sec(tempHosM);
            messages.add(tempHosM);
        }

        //IgnitionOn
        tempHosM = clone(tempHosM);
        tempHosM.setIgnitionOn(true);
        tempHosM.setSpeedFromEngine(true);
        tempHosM.setTachometer(IDLE_RPM);
        tempHosM.setEngineActivityDetected(true);
        tempHosM = changesOver2sec(tempHosM);
        messages.add(tempHosM);

        //1min idling
        for(int i = 0; i<30; i++){
            tempHosM = changesOver2sec(tempHosM);
            messages.add(tempHosM);
        }

        //1min 0-60
        ArrayList<IHOSMessage> tempAccel = acceleration(tempHosM, 60, 60);
        for(IHOSMessage message:tempAccel){
            messages.add(message);
            tempHosM = message;
        }

        //5min @60
        for(int i = 0; i<150; i++){
            tempHosM = changesOver2sec(tempHosM);
            messages.add(tempHosM);
        }

        //1min 60-0 - losing speed from engine after 30 secs
        tempAccel = acceleration(tempHosM, 30, 30);
        for(IHOSMessage message:tempAccel){
            messages.add(message);
            tempHosM = message;
        }

        tempHosM.setSpeedFromEngine(false);
        tempAccel = acceleration(tempHosM, 30, 0);
        for(IHOSMessage message:tempAccel){
            messages.add(message);
            tempHosM = message;
        }


        tempHosM.setSpeedFromEngine(true);
        //30sec idle
        for(int i = 0; i<15; i++){
            tempHosM = changesOver2sec(tempHosM);
            messages.add(tempHosM);
        }

        //Ignition off
        tempHosM = clone(tempHosM);
        tempHosM.setIgnitionOn(false);
        tempHosM.setTachometer(0);
        tempHosM.setEngineActivityDetected(false);
        tempHosM = changesOver2sec(tempHosM);
        messages.add(tempHosM);

        //1min engine off
        for(int i = 0; i<30; i++){
            tempHosM = changesOver2sec(tempHosM);
            messages.add(tempHosM);
        }

        when(stubFeatureToggleService.getIsEldMandateEnabled()).thenReturn(true);
        HosMessageProcessor processor = new HosMessageProcessor(stubFeatureToggleService, Thresholds.DEFAULT);

        ArrayList<EventRecord> eventRecords = processMessages(messages, processor);

        int i = 0;

        Assert.assertEquals(EventTypeEnum.IGNITIONON,eventRecords.get(i++).getEventType());
        Assert.assertEquals(EventTypeEnum.VEHICLESTOPPED, eventRecords.get(i++).getEventType());
        Assert.assertEquals(EventTypeEnum.MOVE,eventRecords.get(i++).getEventType());
        Assert.assertEquals(EventTypeEnum.DRIVESTART, eventRecords.get(i++).getEventType());
        Assert.assertEquals(EventTypeEnum.ERROR, eventRecords.get(i).getEventType());
        Assert.assertEquals(512, eventRecords.get(i++).getEventData());
        Assert.assertEquals(EventTypeEnum.VEHICLESTOPPED,eventRecords.get(i++).getEventType());
        Assert.assertEquals(EventTypeEnum.ERROR,eventRecords.get(i).getEventType());
        Assert.assertEquals(0, eventRecords.get(i++).getEventData());
        Assert.assertEquals(EventTypeEnum.IGNITIONOFF, eventRecords.get(i++).getEventType());
        Assert.assertEquals(EventTypeEnum.DRIVEEND,eventRecords.get(i++).getEventType());
    }

    @Test
    public void test_DrivingScenario_SpeedFromEngineFlag_ExceedingStopTimeThresholdDuringErrorState_ForMandate() {

        when(stubFeatureToggleService.getIsEldMandateEnabled()).thenReturn(true);

        ArrayList<IHOSMessage> messages = new ArrayList<>();
        IHOSMessage tempHosM = new HOSMessage();

        //10/10/2016 10:10:00
        Date startingDate = new Date(1476094200000L);

        //engine off
        tempHosM.setTimestampUtc(new DateTime(startingDate));
        tempHosM.setGpsLatitude(34.7f);
        tempHosM.setGpsLongitude(40f);
        tempHosM.setSpeedometer(0);
        tempHosM.setTachometer(0);
        tempHosM.setOdometer(0);
        tempHosM.setTripOdometer(0);
        tempHosM.setEngineHours(0);
        tempHosM.setTripEngineSeconds(0);
        tempHosM.setGpsValid(true);
        tempHosM.setIgnitionOn(false);
        tempHosM.setEngineActivityDetected(false);
        tempHosM.setDatetimeValid(true);
        tempHosM.setSerialNumberCrc(5496);
        tempHosM.setOdometerFromEngine(true);
        tempHosM.setSpeedFromEngine(true);

        messages.add(tempHosM);

        //1min engine off
        for(int i = 0; i<30; i++){
            tempHosM = changesOver2sec(tempHosM);
            messages.add(tempHosM);
        }

        //IgnitionOn
        tempHosM = clone(tempHosM);
        tempHosM.setIgnitionOn(true);
        tempHosM.setSpeedFromEngine(true);
        tempHosM.setTachometer(IDLE_RPM);
        tempHosM.setEngineActivityDetected(true);
        tempHosM = changesOver2sec(tempHosM);
        messages.add(tempHosM);

        //1min idling
        for(int i = 0; i<30; i++){
            tempHosM = changesOver2sec(tempHosM);
            messages.add(tempHosM);
        }

        //1min 0-60
        ArrayList<IHOSMessage> tempAccel = acceleration(tempHosM, 60, 60);
        for(IHOSMessage message:tempAccel){
            messages.add(message);
            tempHosM = message;
        }

        //5min @60
        for(int i = 0; i<150; i++){
            tempHosM = changesOver2sec(tempHosM);
            messages.add(tempHosM);
        }

        //1min 60-0 - losing speed from engine after 30 secs
        tempHosM.setSpeedFromEngine(false);
        tempAccel = acceleration(tempHosM, 30, 0);
        for(IHOSMessage message:tempAccel){
            messages.add(message);
            tempHosM = message;
        }



        tempHosM.setSpeedFromEngine(false);
        tempHosM = clone(tempHosM);
        tempHosM.setEngineActivityDetected(true);
        tempHosM = changesOver2sec(tempHosM);
        messages.add(tempHosM);

        for(int i = 0; i<200; i++){
            tempHosM = changesOver2sec(tempHosM);
            messages.add(tempHosM);
        }


        tempHosM.setSpeedFromEngine(true);
        //1min 0-60
        tempAccel = acceleration(tempHosM, 60, 60);
        for(IHOSMessage message:tempAccel){
            messages.add(message);
            tempHosM = message;
        }


        when(stubFeatureToggleService.getIsEldMandateEnabled()).thenReturn(true);
        HosMessageProcessor processor = new HosMessageProcessor(stubFeatureToggleService, Thresholds.DEFAULT);

        ArrayList<EventRecord> eventRecords = processMessages(messages, processor);

        int i = 0;

        Assert.assertEquals(EventTypeEnum.IGNITIONON,eventRecords.get(i++).getEventType());
        Assert.assertEquals(EventTypeEnum.VEHICLESTOPPED, eventRecords.get(i++).getEventType());
        Assert.assertEquals(EventTypeEnum.MOVE, eventRecords.get(i++).getEventType());
        Assert.assertEquals(EventTypeEnum.DRIVESTART, eventRecords.get(i++).getEventType());
        Assert.assertEquals(EventTypeEnum.ERROR, eventRecords.get(i).getEventType());
        Assert.assertEquals(512, eventRecords.get(i++).getEventData());
        Assert.assertEquals(EventTypeEnum.VEHICLESTOPPED, eventRecords.get(i++).getEventType());
        Assert.assertEquals(EventTypeEnum.DRIVEEND, eventRecords.get(i++).getEventType());
        Assert.assertEquals(EventTypeEnum.ERROR, eventRecords.get(i).getEventType());
        Assert.assertEquals(0, eventRecords.get(i++).getEventData());
        Assert.assertEquals(EventTypeEnum.MOVE, eventRecords.get(i++).getEventType());
        Assert.assertEquals(EventTypeEnum.DRIVESTART, eventRecords.get(i++).getEventType());
    }

    @Test
    public void test_DrivingScenario_SpeedFromEngineFlag_WhileDrivingBeforeStopTimeThreashold_ForMandate() {

        when(stubFeatureToggleService.getIsEldMandateEnabled()).thenReturn(true);

        ArrayList<IHOSMessage> messages = new ArrayList<>();
        IHOSMessage tempHosM = new HOSMessage();

        //10/10/2016 10:10:00
        Date startingDate = new Date(1476094200000L);

        //engine off
        tempHosM.setTimestampUtc(new DateTime(startingDate));
        tempHosM.setGpsLatitude(34.7f);
        tempHosM.setGpsLongitude(40f);
        tempHosM.setSpeedometer(0);
        tempHosM.setTachometer(0);
        tempHosM.setOdometer(0);
        tempHosM.setTripOdometer(0);
        tempHosM.setEngineHours(0);
        tempHosM.setTripEngineSeconds(0);
        tempHosM.setGpsValid(true);
        tempHosM.setIgnitionOn(false);
        tempHosM.setEngineActivityDetected(false);
        tempHosM.setDatetimeValid(true);
        tempHosM.setSerialNumberCrc(5496);
        tempHosM.setOdometerFromEngine(true);
        tempHosM.setSpeedFromEngine(true);

        messages.add(tempHosM);

        //1min engine off
        for(int i = 0; i<30; i++){
            tempHosM = changesOver2sec(tempHosM);
            messages.add(tempHosM);
        }

        //IgnitionOn
        tempHosM = clone(tempHosM);
        tempHosM.setIgnitionOn(true);
        tempHosM.setSpeedFromEngine(true);
        tempHosM.setTachometer(IDLE_RPM);
        tempHosM.setEngineActivityDetected(true);
        tempHosM = changesOver2sec(tempHosM);
        messages.add(tempHosM);

        //1min idling
        for(int i = 0; i<30; i++){
            tempHosM = changesOver2sec(tempHosM);
            messages.add(tempHosM);
        }

        //1min 0-60
        ArrayList<IHOSMessage> tempAccel = acceleration(tempHosM, 60, 60);
        for(IHOSMessage message:tempAccel){
            messages.add(message);
            tempHosM = message;
        }

        //5min @60
        for(int i = 0; i<150; i++){
            tempHosM = changesOver2sec(tempHosM);
            messages.add(tempHosM);
        }

        //1min 60-0 - losing speed from engine after 30 secs
        tempHosM.setSpeedFromEngine(false);
        tempAccel = acceleration(tempHosM, 30, 0);
        for(IHOSMessage message:tempAccel){
            messages.add(message);
            tempHosM = message;
        }


        //Ignition off
        tempHosM.setSpeedFromEngine(false);
        tempHosM = clone(tempHosM);
        tempHosM.setIgnitionOn(false);
        tempHosM.setTachometer(0);
        tempHosM.setEngineActivityDetected(true);
        tempHosM = changesOver2sec(tempHosM);
        messages.add(tempHosM);

        for(int i = 0; i<300; i++){
            tempHosM = changesOver2sec(tempHosM);
            messages.add(tempHosM);
        }


        when(stubFeatureToggleService.getIsEldMandateEnabled()).thenReturn(true);
        HosMessageProcessor processor = new HosMessageProcessor(stubFeatureToggleService, Thresholds.DEFAULT);

        ArrayList<EventRecord> eventRecords = processMessages(messages, processor);

        int i = 0;

        Assert.assertEquals(EventTypeEnum.IGNITIONON,eventRecords.get(i++).getEventType());
        Assert.assertEquals(EventTypeEnum.VEHICLESTOPPED, eventRecords.get(i++).getEventType());
        Assert.assertEquals(EventTypeEnum.MOVE, eventRecords.get(i++).getEventType());
        Assert.assertEquals(EventTypeEnum.DRIVESTART, eventRecords.get(i++).getEventType());
        Assert.assertEquals(EventTypeEnum.ERROR, eventRecords.get(i).getEventType());
        Assert.assertEquals(512, eventRecords.get(i++).getEventData());
        Assert.assertEquals(EventTypeEnum.VEHICLESTOPPED, eventRecords.get(i++).getEventType());
        Assert.assertEquals(EventTypeEnum.ERROR, eventRecords.get(i).getEventType());
        Assert.assertEquals(0, eventRecords.get(i++).getEventData());
        Assert.assertEquals(EventTypeEnum.IGNITIONOFF, eventRecords.get(i++).getEventType());
        Assert.assertEquals(EventTypeEnum.DRIVEEND, eventRecords.get(i++).getEventType());
    }

    @Test
    public void test_DrivingScenario_SpeedFromEngineFlags_GenerateMoveDuringErrorStateWhenComingOutOfDrivingState_ForMandate() {

        when(stubFeatureToggleService.getIsEldMandateEnabled()).thenReturn(true);

        ArrayList<IHOSMessage> messages = new ArrayList<>();
        IHOSMessage tempHosM = new HOSMessage();

        //10/10/2016 10:10:00
        Date startingDate = new Date(1476094200000L);

        //engine off
        tempHosM.setTimestampUtc(new DateTime(startingDate));
        tempHosM.setGpsLatitude(34.7f);
        tempHosM.setGpsLongitude(40f);
        tempHosM.setSpeedometer(0);
        tempHosM.setTachometer(0);
        tempHosM.setOdometer(0);
        tempHosM.setTripOdometer(0);
        tempHosM.setEngineHours(0);
        tempHosM.setTripEngineSeconds(0);
        tempHosM.setGpsValid(true);
        tempHosM.setIgnitionOn(false);
        tempHosM.setEngineActivityDetected(false);
        tempHosM.setDatetimeValid(true);
        tempHosM.setSerialNumberCrc(5496);
        tempHosM.setOdometerFromEngine(true);
        tempHosM.setSpeedFromEngine(true);

        messages.add(tempHosM);

        //1min engine off
        for(int i = 0; i<30; i++){
            tempHosM = changesOver2sec(tempHosM);
            messages.add(tempHosM);
        }

        //IgnitionOn
        tempHosM = clone(tempHosM);
        tempHosM.setIgnitionOn(true);
        tempHosM.setSpeedFromEngine(true);
        tempHosM.setTachometer(IDLE_RPM);
        tempHosM.setEngineActivityDetected(true);
        tempHosM = changesOver2sec(tempHosM);
        messages.add(tempHosM);

        //1min idling
        for(int i = 0; i<30; i++){
            tempHosM = changesOver2sec(tempHosM);
            messages.add(tempHosM);
        }

        //1min 0-60
        ArrayList<IHOSMessage> tempAccel = acceleration(tempHosM, 60, 60);
        for(IHOSMessage message:tempAccel){
            messages.add(message);
            tempHosM = message;
        }

        //5min @60
        for(int i = 0; i<150; i++){
            tempHosM = changesOver2sec(tempHosM);
            messages.add(tempHosM);
        }


        tempAccel = acceleration(tempHosM, 60, 30);
        for(IHOSMessage message:tempAccel){
            messages.add(message);
            tempHosM = message;
        }

        tempHosM.setSpeedFromEngine(false);
        tempAccel = acceleration(tempHosM, 60, 60);
        for(IHOSMessage message:tempAccel){
            messages.add(message);
            tempHosM = message;
        }

        tempHosM.setSpeedFromEngine(true);

        //5min @60
        for(int i = 0; i<150; i++){
            tempHosM = changesOver2sec(tempHosM);
            messages.add(tempHosM);
        }

        //1min 60-0
        tempAccel = acceleration(tempHosM, 60, 0);
        for(IHOSMessage message:tempAccel){
            messages.add(message);
            tempHosM = message;
        }

        //30sec idle
        for(int i = 0; i<15; i++){
            tempHosM = changesOver2sec(tempHosM);
            messages.add(tempHosM);
        }

        //Ignition off
        tempHosM = clone(tempHosM);
        tempHosM.setIgnitionOn(false);
        tempHosM.setTachometer(0);
        tempHosM.setEngineActivityDetected(false);
        tempHosM = changesOver2sec(tempHosM);
        messages.add(tempHosM);

        //1min engine off
        for(int i = 0; i<30; i++){
            tempHosM = changesOver2sec(tempHosM);
            messages.add(tempHosM);
        }

        when(stubFeatureToggleService.getIsEldMandateEnabled()).thenReturn(true);
        HosMessageProcessor processor = new HosMessageProcessor(stubFeatureToggleService, Thresholds.DEFAULT);

        ArrayList<EventRecord> eventRecords = processMessages(messages, processor);

        int i = 0;

        Assert.assertEquals(EventTypeEnum.IGNITIONON,eventRecords.get(i++).getEventType());
        Assert.assertEquals(EventTypeEnum.VEHICLESTOPPED, eventRecords.get(i++).getEventType());
        Assert.assertEquals(EventTypeEnum.MOVE,eventRecords.get(i++).getEventType());
        Assert.assertEquals(EventTypeEnum.DRIVESTART, eventRecords.get(i++).getEventType());
        Assert.assertEquals(EventTypeEnum.ERROR,eventRecords.get(i).getEventType());
        Assert.assertEquals(512, eventRecords.get(i++).getEventData());
        Assert.assertEquals(EventTypeEnum.VEHICLESTOPPED,eventRecords.get(i++).getEventType());
        Assert.assertEquals(EventTypeEnum.ERROR, eventRecords.get(i).getEventType());
        Assert.assertEquals(0, eventRecords.get(i++).getEventData());
        Assert.assertEquals(EventTypeEnum.MOVE, eventRecords.get(i++).getEventType());
        Assert.assertEquals(EventTypeEnum.VEHICLESTOPPED,eventRecords.get(i++).getEventType());
        Assert.assertEquals(EventTypeEnum.IGNITIONOFF, eventRecords.get(i++).getEventType());
        Assert.assertEquals(EventTypeEnum.DRIVEEND,eventRecords.get(i++).getEventType());
    }

    @Test
    public void test_DrivingScenario_SpeedFromEngineFlags_GeneratingErrorStateWhileMovingNotDriving_ForMandate() {

        when(stubFeatureToggleService.getIsEldMandateEnabled()).thenReturn(true);

        ArrayList<IHOSMessage> messages = new ArrayList<>();
        IHOSMessage tempHosM = new HOSMessage();

        //10/10/2016 10:10:00
        Date startingDate = new Date(1476094200000L);

        //engine off
        tempHosM.setTimestampUtc(new DateTime(startingDate));
        tempHosM.setGpsLatitude(34.7f);
        tempHosM.setGpsLongitude(40f);
        tempHosM.setSpeedometer(0);
        tempHosM.setTachometer(0);
        tempHosM.setOdometer(0);
        tempHosM.setTripOdometer(0);
        tempHosM.setEngineHours(0);
        tempHosM.setTripEngineSeconds(0);
        tempHosM.setGpsValid(true);
        tempHosM.setIgnitionOn(false);
        tempHosM.setEngineActivityDetected(false);
        tempHosM.setDatetimeValid(true);
        tempHosM.setSerialNumberCrc(5496);
        tempHosM.setOdometerFromEngine(true);
        tempHosM.setSpeedFromEngine(true);
        messages.add(tempHosM);

        //1min engine off
        for(int i = 0; i<30; i++){
            tempHosM = changesOver2sec(tempHosM);
            messages.add(tempHosM);
        }

        //IgnitionOn
        tempHosM = clone(tempHosM);
        tempHosM.setIgnitionOn(true);
        tempHosM.setSpeedFromEngine(true);
        tempHosM.setTachometer(IDLE_RPM);
        tempHosM.setEngineActivityDetected(true);
        tempHosM = changesOver2sec(tempHosM);
        messages.add(tempHosM);

        //1min idling
        for(int i = 0; i<30; i++){
            tempHosM = changesOver2sec(tempHosM);
            messages.add(tempHosM);
        }

        //1min 0-60
        ArrayList<IHOSMessage> tempAccel = acceleration(tempHosM, 60, 3);
        for(IHOSMessage message:tempAccel){
            messages.add(message);
            tempHosM = message;
        }

        tempHosM.setSpeedFromEngine(false);
        tempAccel = acceleration(tempHosM, 30, 20);
        for(IHOSMessage message:tempAccel) {
            messages.add(message);
            tempHosM = message;
        }

        tempHosM.setSpeedFromEngine(true);

        //5min @60
        for(int i = 0; i<150; i++){
            tempHosM = changesOver2sec(tempHosM);
            messages.add(tempHosM);
        }

        //1min 60-0
        tempAccel = acceleration(tempHosM, 60, 0);
        for(IHOSMessage message:tempAccel){
            messages.add(message);
            tempHosM = message;
        }

        //30sec idle
        for(int i = 0; i<15; i++){
            tempHosM = changesOver2sec(tempHosM);
            messages.add(tempHosM);
        }

        //Ignition off
        tempHosM = clone(tempHosM);
        tempHosM.setIgnitionOn(false);
        tempHosM.setTachometer(0);
        tempHosM.setEngineActivityDetected(false);
        tempHosM = changesOver2sec(tempHosM);
        messages.add(tempHosM);

        //1min engine off
        for(int i = 0; i<30; i++){
            tempHosM = changesOver2sec(tempHosM);
            messages.add(tempHosM);
        }

        when(stubFeatureToggleService.getIsEldMandateEnabled()).thenReturn(true);
        HosMessageProcessor processor = new HosMessageProcessor(stubFeatureToggleService, Thresholds.DEFAULT);

        ArrayList<EventRecord> eventRecords = processMessages(messages, processor);

        int i = 0;

        Assert.assertEquals(EventTypeEnum.IGNITIONON,eventRecords.get(i++).getEventType());
        Assert.assertEquals(EventTypeEnum.VEHICLESTOPPED, eventRecords.get(i++).getEventType());
        Assert.assertEquals(EventTypeEnum.MOVE,eventRecords.get(i++).getEventType());
        Assert.assertEquals(EventTypeEnum.ERROR,eventRecords.get(i).getEventType());
        Assert.assertEquals(512, eventRecords.get(i++).getEventData());
        Assert.assertEquals(EventTypeEnum.VEHICLESTOPPED,eventRecords.get(i++).getEventType());
        Assert.assertEquals(EventTypeEnum.ERROR, eventRecords.get(i).getEventType());
        Assert.assertEquals(0, eventRecords.get(i++).getEventData());
        Assert.assertEquals(EventTypeEnum.MOVE, eventRecords.get(i++).getEventType());
        Assert.assertEquals(EventTypeEnum.DRIVESTART,eventRecords.get(i++).getEventType());
        Assert.assertEquals(EventTypeEnum.VEHICLESTOPPED,eventRecords.get(i++).getEventType());
        Assert.assertEquals(EventTypeEnum.IGNITIONOFF, eventRecords.get(i++).getEventType());
        Assert.assertEquals(EventTypeEnum.DRIVEEND,eventRecords.get(i++).getEventType());
    }


    @Test
    public void test_DrivingScenario_OdoErrorOnAndOffDuringDriving_ForMandate() {

        when(stubFeatureToggleService.getIsEldMandateEnabled()).thenReturn(true);

        ArrayList<IHOSMessage> messages = new ArrayList<>();
        IHOSMessage tempHosM = new HOSMessage();

        //10/10/2016 10:10:00
        Date startingDate = new Date(1476094200000L);

        //engine off
        tempHosM.setTimestampUtc(new DateTime(startingDate));
        tempHosM.setGpsLatitude(34.7f);
        tempHosM.setGpsLongitude(40f);
        tempHosM.setSpeedometer(0);
        tempHosM.setTachometer(0);
        tempHosM.setOdometer(0);
        tempHosM.setTripOdometer(0);
        tempHosM.setEngineHours(0);
        tempHosM.setTripEngineSeconds(0);
        tempHosM.setGpsValid(true);
        tempHosM.setIgnitionOn(false);
        tempHosM.setEngineActivityDetected(false);
        tempHosM.setDatetimeValid(true);
        tempHosM.setSerialNumberCrc(5496);
        tempHosM.setOdometerFromEngine(true);
        tempHosM.setSpeedFromEngine(true);

        messages.add(tempHosM);

        //1min engine off
        for(int i = 0; i<30; i++){
            tempHosM = changesOver2sec(tempHosM);
            messages.add(tempHosM);
        }

        //IgnitionOn
        tempHosM = clone(tempHosM);
        tempHosM.setIgnitionOn(true);
        tempHosM.setSpeedFromEngine(true);
        tempHosM.setTachometer(IDLE_RPM);
        tempHosM.setEngineActivityDetected(true);
        tempHosM = changesOver2sec(tempHosM);
        messages.add(tempHosM);

        //1min idling
        for(int i = 0; i<30; i++){
            tempHosM = changesOver2sec(tempHosM);
            messages.add(tempHosM);
        }

        //1min 0-60
        ArrayList<IHOSMessage> tempAccel = acceleration(tempHosM, 60, 60);
        for(IHOSMessage message:tempAccel){

            messages.add(message);
            tempHosM = message;
        }

        //Setting Odometer from Engine to false for 5 min
        tempHosM.setOdometerFromEngine(false);
        //5min @60
        for(int i = 0; i<150; i++){
            tempHosM = changesOver2sec(tempHosM);
            messages.add(tempHosM);
        }
        //Setting Odometer from Engine to true
        tempHosM.setOdometerFromEngine(true);

        //1min 60-0 - losing speed from engine after 30 secs
        tempAccel = acceleration(tempHosM, 30, 30);
        for(IHOSMessage message:tempAccel){
            messages.add(message);
            tempHosM = message;
        }

        tempHosM.setSpeedFromEngine(false);
        tempAccel = acceleration(tempHosM, 30, 0);
        for(IHOSMessage message:tempAccel){
            messages.add(message);
            tempHosM = message;
        }

        tempHosM.setSpeedFromEngine(true);

        tempAccel = acceleration(tempHosM, 60, 60);
        for(IHOSMessage message:tempAccel){
            messages.add(message);
            tempHosM = message;
        }



        tempHosM.setSpeedFromEngine(false);
        tempHosM.setOdometerFromEngine(false);
        tempAccel = acceleration(tempHosM, 30, 0);
        for(IHOSMessage message:tempAccel){
            messages.add(message);
            tempHosM = message;
        }

        tempHosM.setSpeedFromEngine(true);
        tempHosM.setOdometerFromEngine(true);
        //30sec idle
        for(int i = 0; i<15; i++){
            tempHosM = changesOver2sec(tempHosM);
            messages.add(tempHosM);
        }

        //Ignition off
        tempHosM = clone(tempHosM);
        tempHosM.setIgnitionOn(false);
        tempHosM.setTachometer(0);
        tempHosM.setEngineActivityDetected(false);
        tempHosM = changesOver2sec(tempHosM);
        messages.add(tempHosM);

        //1min engine off
        for(int i = 0; i<30; i++){
            tempHosM = changesOver2sec(tempHosM);
            messages.add(tempHosM);
        }

        when(stubFeatureToggleService.getIsEldMandateEnabled()).thenReturn(true);
        HosMessageProcessor processor = new HosMessageProcessor(stubFeatureToggleService, Thresholds.DEFAULT);

        ArrayList<EventRecord> eventRecords = processMessages(messages, processor);

        int i = 0;

        Assert.assertEquals(EventTypeEnum.IGNITIONON,eventRecords.get(i++).getEventType());
        Assert.assertEquals(EventTypeEnum.VEHICLESTOPPED, eventRecords.get(i++).getEventType());
        Assert.assertEquals(EventTypeEnum.MOVE,eventRecords.get(i++).getEventType());
        Assert.assertEquals(EventTypeEnum.DRIVESTART, eventRecords.get(i++).getEventType());
        Assert.assertEquals(EventTypeEnum.ERROR, eventRecords.get(i).getEventType()); //Odometer Error is On
        Assert.assertEquals(1024, eventRecords.get(i++).getEventData());
        Assert.assertEquals(EventTypeEnum.ERROR, eventRecords.get(i).getEventType());  //Odometer Error is Off
        Assert.assertEquals(0, eventRecords.get(i++).getEventData());
        Assert.assertEquals(EventTypeEnum.ERROR, eventRecords.get(i).getEventType());
        Assert.assertEquals(512, eventRecords.get(i++).getEventData());
        Assert.assertEquals(EventTypeEnum.VEHICLESTOPPED,eventRecords.get(i++).getEventType());
        Assert.assertEquals(EventTypeEnum.ERROR,eventRecords.get(i).getEventType());
        Assert.assertEquals(0, eventRecords.get(i++).getEventData());
        Assert.assertEquals(EventTypeEnum.MOVE, eventRecords.get(i++).getEventType());
        Assert.assertEquals(EventTypeEnum.ERROR, eventRecords.get(i).getEventType());
        Assert.assertEquals(1536, eventRecords.get(i++).getEventData());
        Assert.assertEquals(EventTypeEnum.VEHICLESTOPPED, eventRecords.get(i++).getEventType());
        Assert.assertEquals(EventTypeEnum.ERROR, eventRecords.get(i).getEventType());
        Assert.assertEquals(0, eventRecords.get(i++).getEventData());
        Assert.assertEquals(EventTypeEnum.IGNITIONOFF, eventRecords.get(i++).getEventType());
        Assert.assertEquals(EventTypeEnum.DRIVEEND,eventRecords.get(i++).getEventType());
    }

    public int rpmForSpeed(float speed){

        float rpm = IDLE_RPM;
        rpm += speed * RPM_PER_MPH;

        return Math.round(rpm);
    }

    public ArrayList<IHOSMessage> acceleration(IHOSMessage lastHosM, int durationSeconds, float endingSpeed){

        float speedChange = (endingSpeed-lastHosM.getSpeedometer())*2/durationSeconds;
        ArrayList<IHOSMessage> hosMessages = new ArrayList<>();

        for(int i = 0; i<durationSeconds; i+=2){

            IHOSMessage hosMessage = clone(lastHosM);

            hosMessage.setSpeedometer(hosMessage.getSpeedometer() + speedChange);

            hosMessage.setTachometer(rpmForSpeed(hosMessage.getSpeedometer()));

            hosMessage = changesOver2sec(hosMessage);

            hosMessages.add(hosMessage);

            lastHosM = hosMessage;
        }
        return hosMessages;
    }

    public IHOSMessage changesOver2sec( IHOSMessage lastHosM){

        IHOSMessage hosMessage = clone(lastHosM);
        int timeSpanSeconds = 2;
        float time = (float) timeSpanSeconds/ SECONDS_IN_HOUR;
        float speed = hosMessage.getSpeedometer();
        float distance = time * speed;

        hosMessage.setOdometer(hosMessage.getOdometer() + distance);
        hosMessage.setTripOdometer(hosMessage.getTripOdometer() + distance);

        Date newTime = new Date(hosMessage.getTimestampUtc().getMillis() + 2000);
        hosMessage.setTimestampUtc(new DateTime(newTime));

        float newLon = hosMessage.getGpsLongitude() + distance/ MILES_IN_LONG_DEGREE;
        hosMessage.setGpsLongitude(newLon);

        if(hosMessage.isIgnitionOn()) {
            hosMessage.setEngineHours(hosMessage.getEngineHours() + time);
            hosMessage.setTripEngineSeconds(hosMessage.getTripEngineSeconds() + timeSpanSeconds);
        }
        return hosMessage;
    }

    private ArrayList<EventRecord> processMessages(ArrayList<IHOSMessage> ihosMessages, HosMessageProcessor hosMessageProcessor) {
        ArrayList<EventRecord> resultEventsCollector = new ArrayList<>();
        for (IHOSMessage hosMessage : ihosMessages) {
            Now(hosMessage.getTimestampUtc().getMillis());
            ArrayList<EventRecord> results = hosMessageProcessor.processHosMessage(hosMessage);
            resultEventsCollector.addAll(results);
            if(results.size() > 0) {
                results.size();
            }
        }
        return resultEventsCollector;
    }

    private IHOSMessage clone(IHOSMessage lastHosM){

        IHOSMessage hosM = new HOSMessage();

        hosM.setTimestampUtc(lastHosM.getTimestampUtc());
        hosM.setGpsLatitude(lastHosM.getGpsLatitude());
        hosM.setGpsLongitude(lastHosM.getGpsLongitude());
        hosM.setSpeedometer(lastHosM.getSpeedometer());
        hosM.setTachometer(lastHosM.getTachometer());
        hosM.setOdometer(lastHosM.getOdometer());
        hosM.setTripOdometer(lastHosM.getTripOdometer());
        hosM.setEngineHours(lastHosM.getEngineHours());
        hosM.setTripEngineSeconds(lastHosM.getTripEngineSeconds());
        hosM.setGpsValid(lastHosM.isGpsValid());
        hosM.setIgnitionOn(lastHosM.isIgnitionOn());
        hosM.setEngineActivityDetected(lastHosM.isEngineActivityDetected());
        hosM.setDatetimeValid(lastHosM.isDatetimeValid());
        hosM.setSerialNumberCrc(lastHosM.getSerialNumberCrc());
        hosM.setSpeedFromEngine(lastHosM.isSpeedFromEngine());
        hosM.setOdometerFromEngine(lastHosM.isOdometerFromEngine());

        return hosM;
    }
    //endregion

    public ArrayList<IHOSMessage> generateMessages(IHOSMessage hosMessageTemplate, int duration){
        return generateMessages(hosMessageTemplate, duration, 0f);
    }

    public ArrayList<IHOSMessage> generateMessages(IHOSMessage hosMessageTemplate, int duration, float endingSpeed){
        ArrayList<IHOSMessage> hosMessages = new ArrayList<>();
        for(int i = 0; i < duration; i+= MESSAGE_RATE_SECONDS){
            IHOSMessage hosMessage = new HOSMessage(hosMessageTemplate);
            hosMessage.setTimestampUtc(new DateTime(hosMessageTemplate.getTimestampUtc().getMillis() + ( i * 1000 )));
            Now(hosMessage.getTimestampUtc().getMillis());

            engine.vss(endingSpeed);

            hosMessage.setOdometer(engine.getOdometer());
            hosMessage.setSpeedometer(engine.getVss());
            hosMessage.setTachometer(engine.getRpm());
            hosMessages.add(hosMessage);
        }
        return hosMessages;
    }

}
