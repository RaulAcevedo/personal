package com.jjkeller.kmbapi.realtime.malfunction;

import android.provider.Settings;

import com.jjkeller.kmbapi.TestableTimeKeeper;
import com.jjkeller.kmbapi.common.ITimeKeeper;
import com.jjkeller.kmbapi.common.TimeKeeper;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.EOBR.ErrorAccumulator;
import com.jjkeller.kmbapi.controller.EmployeeLogEldMandateController;
import com.jjkeller.kmbapi.enums.Malfunction;
import com.jjkeller.kmbapi.kmbeobr.DataFlagEnums;
import com.jjkeller.kmbapi.kmbeobr.EventRecord;
import com.jjkeller.kmbapi.kmbeobr.EventTypeEnum;
import com.jjkeller.kmbapi.malfunction.BaseMalfunctionTestConfig;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.Date;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests for PositionMalfunctionRealtimeProcess
 *
 * Created by Charles Stebbins on 2/27/2017.
 */

public class PositionMalfunctionRealtimeProcessTest extends BaseMalfunctionTestConfig {
    // Setup is stuff is done in the EobrService.

    Field gpsErrorAccumulatorField;
    Field employeeLogField;
    Field eldMandateControllerField;
    Field timeKeeperField;

    EmployeeLog employeeLog;
    EmployeeLogEldMandateController eldMandateController;
    PositionMalfunctionRealtimeProcess malfunctionProcess;
    ErrorAccumulator testErrorAccumulator;
    TestableTimeKeeper timekeeper;

    @Before
    public void setup() throws NoSuchFieldException, IllegalAccessException {
        malfunctionProcess = spy(new PositionMalfunctionRealtimeProcess(60));
        malfunctionProcess.setup();

        gpsErrorAccumulatorField = PositionMalfunctionRealtimeProcess.class.getDeclaredField("gpsErrorAccumulator");
        gpsErrorAccumulatorField.setAccessible(true);

        employeeLogField = PositionMalfunctionRealtimeProcess.class.getDeclaredField("employeeLog");
        employeeLogField.setAccessible(true);

        eldMandateControllerField = PositionMalfunctionRealtimeProcess.class.getDeclaredField("eldMandateController");
        eldMandateControllerField.setAccessible(true);

        timeKeeperField = PositionMalfunctionRealtimeProcess.class.getDeclaredField("timeKeeper");
        timeKeeperField.setAccessible(true);

        employeeLog = mock(EmployeeLog.class);
        eldMandateController = mock(EmployeeLogEldMandateController.class);

        timekeeper = new TestableTimeKeeper(DateTime.parse("2017-02-20T14:00"));

        DateTime startWindow = DateTime.parse("2017-02-20T2:00");
        testErrorAccumulator = spy(new ErrorAccumulator(startWindow, new EventTypeEnum(EventTypeEnum.GPS), DataFlagEnums.GpsEventFlags.GPS_FAULT, timekeeper));

        employeeLogField.set(malfunctionProcess, employeeLog);
        eldMandateControllerField.set(malfunctionProcess, eldMandateController);
        timeKeeperField.set(malfunctionProcess, timekeeper);
        gpsErrorAccumulatorField.set(malfunctionProcess, testErrorAccumulator);

        doReturn(false).when(malfunctionProcess).hasNullDependencies();

    }




    //---------------- Test process event.

    //Test processEvent with non gps
    @Test
    public void testProcessEventNonGpsEvent() {
        EventRecord vssRecord = createVssFault(DateTime.parse("2017-02-20T13:00"), true);

        malfunctionProcess.processEvent(vssRecord);

        verify(malfunctionProcess, times(0)).hasNullDependencies();

    }

    //Test processEvent with null dependencies
    @Test
    public void testProcessEventNullDependencies() throws IllegalAccessException {
        doReturn(true).when(malfunctionProcess).hasNullDependencies();

        timeKeeperField.set(malfunctionProcess, null);
        gpsErrorAccumulatorField.set(malfunctionProcess, null);

        EventRecord gpsRecord = createGpsFault(DateTime.parse("2017-02-20T13:00"), true);
        malfunctionProcess.processEvent(gpsRecord);

        verify(malfunctionProcess, times(1)).hasNullDependencies();
        verify(testErrorAccumulator, times(0)).processEvent(gpsRecord);

        gpsErrorAccumulatorField.set(malfunctionProcess, testErrorAccumulator);
        malfunctionProcess.processEvent(gpsRecord);
        verify(testErrorAccumulator, times(1)).processEvent(gpsRecord);
        verify(malfunctionProcess, times(1)).post();
        verify(eldMandateController, times(0)).isMalfunctioning(employeeLog, Malfunction.POSITIONING_COMPLIANCE);
    }


    //Test 1 gps event with 59 minutes of error. Should not toggle cleared event.
    @Test
    public void testProcessEventOneEvent59MinutesExactly() throws Throwable {
        when(eldMandateController.isMalfunctioning(employeeLog, Malfunction.POSITIONING_COMPLIANCE)).thenReturn(false);
        EventRecord gpsRecord = createGpsFault(DateTime.parse("2017-02-20T13:01"), true);
        malfunctionProcess.processEvent(gpsRecord);

        verify(eldMandateController, times(0)).createMalfunctionForLoggedInUsers(timekeeper.getCurrentDateTime().toDate(), Malfunction.POSITIONING_COMPLIANCE);
        verify(eldMandateController, times(0)).clearMalfunctionForLoggedInUsers(timekeeper.getCurrentDateTime().toDate(), Malfunction.POSITIONING_COMPLIANCE);
    }


    //Test 1 gps event with 60 minutes of error. Should toggle cleared event.
    @Test
    public void testProcessEventOneEvent60MinutesExactly() throws Throwable {
        when(eldMandateController.isMalfunctioning(employeeLog, Malfunction.POSITIONING_COMPLIANCE)).thenReturn(false);
        EventRecord gpsRecord = createGpsFault(DateTime.parse("2017-02-20T13:00"), true);
        malfunctionProcess.processEvent(gpsRecord);

        verify(eldMandateController, times(1)).createMalfunctionForLoggedInUsers(timekeeper.getCurrentDateTime().toDate(), Malfunction.POSITIONING_COMPLIANCE);
        verify(eldMandateController, times(0)).clearMalfunctionForLoggedInUsers(timekeeper.getCurrentDateTime().toDate(), Malfunction.POSITIONING_COMPLIANCE);
        when(eldMandateController.isMalfunctioning(employeeLog, Malfunction.POSITIONING_COMPLIANCE)).thenReturn(true);


        EventRecord gpsRecordCleared = createGpsFault(DateTime.parse("2017-02-20T14:00"), true);
        malfunctionProcess.processEvent(gpsRecordCleared);

        verify(eldMandateController, times(1)).createMalfunctionForLoggedInUsers(timekeeper.getCurrentDateTime().toDate(), Malfunction.POSITIONING_COMPLIANCE);
        verify(eldMandateController, times(0)).clearMalfunctionForLoggedInUsers(timekeeper.getCurrentDateTime().toDate(), Malfunction.POSITIONING_COMPLIANCE);

    }

    //Test many posts
    @Test
    public void testPostGenerateMalfunctionIdempotent() throws Throwable {
        when(eldMandateController.isMalfunctioning(employeeLog, Malfunction.POSITIONING_COMPLIANCE)).thenReturn(false);
        EventRecord gpsRecord = createGpsFault(DateTime.parse("2017-02-20T13:30"), true);
        malfunctionProcess.processEvent(gpsRecord);
        for(int i = 0; i < 50; i++){
            malfunctionProcess.post();
        }

        verify(eldMandateController, times(0)).createMalfunctionForLoggedInUsers(timekeeper.getCurrentDateTime().toDate(), Malfunction.POSITIONING_COMPLIANCE);
        verify(eldMandateController, times(0)).clearMalfunctionForLoggedInUsers(timekeeper.getCurrentDateTime().toDate(), Malfunction.POSITIONING_COMPLIANCE);

        timekeeper.setSettableNow(DateTime.parse("2017-02-20T14:20"));
        for(int i = 0; i < 50; i++){
            malfunctionProcess.post();
        }

        verify(eldMandateController, times(0)).createMalfunctionForLoggedInUsers(timekeeper.getCurrentDateTime().toDate(), Malfunction.POSITIONING_COMPLIANCE);
        verify(eldMandateController, times(0)).clearMalfunctionForLoggedInUsers(timekeeper.getCurrentDateTime().toDate(), Malfunction.POSITIONING_COMPLIANCE);

        timekeeper.setSettableNow(DateTime.parse("2017-02-20T14:50"));
        malfunctionProcess.post();

        verify(eldMandateController, times(1)).createMalfunctionForLoggedInUsers(timekeeper.getCurrentDateTime().toDate(), Malfunction.POSITIONING_COMPLIANCE);
        verify(eldMandateController, times(0)).clearMalfunctionForLoggedInUsers(timekeeper.getCurrentDateTime().toDate(), Malfunction.POSITIONING_COMPLIANCE);
        when(eldMandateController.isMalfunctioning(employeeLog, Malfunction.POSITIONING_COMPLIANCE)).thenReturn(true);
        for(int i = 0; i < 50; i++){
            malfunctionProcess.post();
        }

        verify(eldMandateController, times(1)).createMalfunctionForLoggedInUsers(timekeeper.getCurrentDateTime().toDate(), Malfunction.POSITIONING_COMPLIANCE);
        verify(eldMandateController, times(0)).clearMalfunctionForLoggedInUsers(timekeeper.getCurrentDateTime().toDate(), Malfunction.POSITIONING_COMPLIANCE);
    }

    @Test
    public void testExceptionDatabase() throws Throwable {
        when(eldMandateController.isMalfunctioning(employeeLog, Malfunction.POSITIONING_COMPLIANCE)).thenReturn(false);
        doThrow(new Exception("UnitTest: Database exception")).when(eldMandateController).clearMalfunctionForLoggedInUsers(timekeeper.getCurrentDateTime().toDate(), Malfunction.POSITIONING_COMPLIANCE);

        EventRecord gpsRecord = createGpsFault(DateTime.parse("2017-02-20T12:30"), true);

        //Should process and hit, but not crash.
        malfunctionProcess.processEvent(gpsRecord);

        verify(eldMandateController, times(1)).createMalfunctionForLoggedInUsers(timekeeper.getCurrentDateTime().toDate(), Malfunction.POSITIONING_COMPLIANCE);
        verify(eldMandateController, times(0)).clearMalfunctionForLoggedInUsers(timekeeper.getCurrentDateTime().toDate(), Malfunction.POSITIONING_COMPLIANCE);
    }

    @Test
    public void testOverDayMark() throws Throwable {
        //After 24 hours from the start time log we should set it back to not malfunctioning.
        when(eldMandateController.isMalfunctioning(employeeLog, Malfunction.POSITIONING_COMPLIANCE)).thenReturn(false);

        //Setup our time and cause a fault.
        timekeeper.setSettableNow(DateTime.parse("2017-02-20T11:31"));
        EventRecord gpsRecord = createGpsFault(DateTime.parse("2017-02-20T11:30"), true);
        malfunctionProcess.processEvent(gpsRecord);
        verify(eldMandateController, times(0)).createMalfunctionForLoggedInUsers(timekeeper.getCurrentDateTime().toDate(), Malfunction.POSITIONING_COMPLIANCE);
        verify(eldMandateController, times(0)).clearMalfunctionForLoggedInUsers(timekeeper.getCurrentDateTime().toDate(), Malfunction.POSITIONING_COMPLIANCE);

        //Turn the fault to malfunction.
        timekeeper.setSettableNow(DateTime.parse("2017-02-20T12:30"));
        malfunctionProcess.post();
        verify(eldMandateController, times(1)).createMalfunctionForLoggedInUsers(timekeeper.getCurrentDateTime().toDate(), Malfunction.POSITIONING_COMPLIANCE);
        verify(eldMandateController, times(0)).clearMalfunctionForLoggedInUsers(timekeeper.getCurrentDateTime().toDate(), Malfunction.POSITIONING_COMPLIANCE);
        when(eldMandateController.isMalfunctioning(employeeLog, Malfunction.POSITIONING_COMPLIANCE)).thenReturn(true);

        //Create non fault.
        timekeeper.setSettableNow(DateTime.parse("2017-02-20T12:33"));
        gpsRecord = createGpsFault(DateTime.parse("2017-02-20T12:33"), false);
        malfunctionProcess.processEvent(gpsRecord);

        malfunctionProcess.post();
        verify(eldMandateController, times(0)).createMalfunctionForLoggedInUsers(timekeeper.getCurrentDateTime().toDate(), Malfunction.POSITIONING_COMPLIANCE);
        verify(eldMandateController, times(0)).clearMalfunctionForLoggedInUsers(timekeeper.getCurrentDateTime().toDate(), Malfunction.POSITIONING_COMPLIANCE);

        //Go to the next day
        timekeeper.setSettableNow(DateTime.parse("2017-02-21T02:01"));
        malfunctionProcess.post();
        verify(eldMandateController, times(0)).createMalfunctionForLoggedInUsers(timekeeper.getCurrentDateTime().toDate(), Malfunction.POSITIONING_COMPLIANCE);
        verify(eldMandateController, times(1)).clearMalfunctionForLoggedInUsers(timekeeper.getCurrentDateTime().toDate(), Malfunction.POSITIONING_COMPLIANCE);
        when(eldMandateController.isMalfunctioning(employeeLog, Malfunction.POSITIONING_COMPLIANCE)).thenReturn(false);

    }

    //@Test
    public void testAfterDayWhileMalfunctioning() throws Throwable {
        when(eldMandateController.isMalfunctioning(employeeLog, Malfunction.POSITIONING_COMPLIANCE)).thenReturn(false);

        //Setup gps malfunction.
        timekeeper.setSettableNow(DateTime.parse("2017-02-20T11:31"));
        EventRecord gpsRecord = createGpsFault(DateTime.parse("2017-02-20T11:30"), true);
        malfunctionProcess.processEvent(gpsRecord);

        //Create gps malfunction
        timekeeper.setSettableNow(DateTime.parse("2017-02-20T12:30"));
        malfunctionProcess.post();
        verify(eldMandateController, times(1)).createMalfunctionForLoggedInUsers(timekeeper.getCurrentDateTime().toDate(), Malfunction.POSITIONING_COMPLIANCE);
        verify(eldMandateController, times(0)).clearMalfunctionForLoggedInUsers(timekeeper.getCurrentDateTime().toDate(), Malfunction.POSITIONING_COMPLIANCE);
        when(eldMandateController.isMalfunctioning(employeeLog, Malfunction.POSITIONING_COMPLIANCE)).thenReturn(true);

        //Go over the day. No longer malfunctioning but we're accumulating.
        timekeeper.setSettableNow(DateTime.parse("2017-02-21T02:01"));
        malfunctionProcess.post();
        verify(eldMandateController, times(0)).createMalfunctionForLoggedInUsers(timekeeper.getCurrentDateTime().toDate(), Malfunction.POSITIONING_COMPLIANCE);
        verify(eldMandateController, times(1)).clearMalfunctionForLoggedInUsers(timekeeper.getCurrentDateTime().toDate(), Malfunction.POSITIONING_COMPLIANCE);
        when(eldMandateController.isMalfunctioning(employeeLog, Malfunction.POSITIONING_COMPLIANCE)).thenReturn(false);

        //Go over an hour of borked time.
        timekeeper.setSettableNow(DateTime.parse("2017-02-21T03:00"));
        malfunctionProcess.post();
        verify(eldMandateController, times(1)).createMalfunctionForLoggedInUsers(timekeeper.getCurrentDateTime().toDate(), Malfunction.POSITIONING_COMPLIANCE);
        verify(eldMandateController, times(0)).clearMalfunctionForLoggedInUsers(timekeeper.getCurrentDateTime().toDate(), Malfunction.POSITIONING_COMPLIANCE);
        when(eldMandateController.isMalfunctioning(employeeLog, Malfunction.POSITIONING_COMPLIANCE)).thenReturn(true);

    }

    //@Test
    public void testAfterDayWhileNotMalfunctioning() throws Throwable {
        when(eldMandateController.isMalfunctioning(employeeLog, Malfunction.POSITIONING_COMPLIANCE)).thenReturn(false);

        //Setup start time and cause a fault.
        timekeeper.setSettableNow(DateTime.parse("2017-02-20T11:31"));
        EventRecord gpsRecord = createGpsFault(DateTime.parse("2017-02-20T11:30"), true);
        malfunctionProcess.processEvent(gpsRecord);
        verify(eldMandateController, times(0)).createMalfunctionForLoggedInUsers(timekeeper.getCurrentDateTime().toDate(), Malfunction.POSITIONING_COMPLIANCE);
        verify(eldMandateController, times(0)).clearMalfunctionForLoggedInUsers(timekeeper.getCurrentDateTime().toDate(), Malfunction.POSITIONING_COMPLIANCE);

        //Turn the fault into a malfunction.
        timekeeper.setSettableNow(DateTime.parse("2017-02-20T12:30"));
        malfunctionProcess.post();
        verify(eldMandateController, times(1)).createMalfunctionForLoggedInUsers(timekeeper.getCurrentDateTime().toDate(), Malfunction.POSITIONING_COMPLIANCE);
        verify(eldMandateController, times(0)).clearMalfunctionForLoggedInUsers(timekeeper.getCurrentDateTime().toDate(), Malfunction.POSITIONING_COMPLIANCE);
        when(eldMandateController.isMalfunctioning(employeeLog, Malfunction.POSITIONING_COMPLIANCE)).thenReturn(true);

        //Fast forward for good luck.
        timekeeper.setSettableNow(DateTime.parse("2017-02-20T16:30"));
        malfunctionProcess.post();
        verify(eldMandateController, times(0)).createMalfunctionForLoggedInUsers(timekeeper.getCurrentDateTime().toDate(), Malfunction.POSITIONING_COMPLIANCE);
        verify(eldMandateController, times(0)).clearMalfunctionForLoggedInUsers(timekeeper.getCurrentDateTime().toDate(), Malfunction.POSITIONING_COMPLIANCE);

        //Process a valid gps.  Shouldn't kill the malfunction.
        EventRecord gpsRecordGood = createGpsFault(DateTime.parse("2017-02-20T16:30"), false);
        malfunctionProcess.processEvent(gpsRecordGood);
        verify(eldMandateController, times(0)).createMalfunctionForLoggedInUsers(timekeeper.getCurrentDateTime().toDate(), Malfunction.POSITIONING_COMPLIANCE);
        verify(eldMandateController, times(0)).clearMalfunctionForLoggedInUsers(timekeeper.getCurrentDateTime().toDate(), Malfunction.POSITIONING_COMPLIANCE);

        //Goto the next day, malfunction should be cleared with a good gps signal.
        timekeeper.setSettableNow(DateTime.parse("2017-02-21T02:20"));
        malfunctionProcess.post();
        verify(eldMandateController, times(0)).createMalfunctionForLoggedInUsers(timekeeper.getCurrentDateTime().toDate(), Malfunction.POSITIONING_COMPLIANCE);
        verify(eldMandateController, times(1)).clearMalfunctionForLoggedInUsers(timekeeper.getCurrentDateTime().toDate(), Malfunction.POSITIONING_COMPLIANCE);
        when(eldMandateController.isMalfunctioning(employeeLog, Malfunction.POSITIONING_COMPLIANCE)).thenReturn(false);

        //Fast forward and verify we're still in a good day.
        timekeeper.setSettableNow(DateTime.parse("2017-02-21T20:20"));
        malfunctionProcess.post();
        verify(eldMandateController, times(0)).createMalfunctionForLoggedInUsers(timekeeper.getCurrentDateTime().toDate(), Malfunction.POSITIONING_COMPLIANCE);
        verify(eldMandateController, times(0)).clearMalfunctionForLoggedInUsers(timekeeper.getCurrentDateTime().toDate(), Malfunction.POSITIONING_COMPLIANCE);
    }




    private EventRecord createGpsFault(DateTime start, boolean errorState) {
        EventRecord event = new EventRecord();
        event.setEventType(EventTypeEnum.GPS);
        event.setTimecode(start.getMillis());

        if(errorState)
            event.setEventData(1 << DataFlagEnums.GpsEventFlags.GPS_FAULT.getIndex());

        return event;
    }

    private EventRecord createVssFault(DateTime start, boolean errorState) {
        EventRecord event = new EventRecord();
        event.setEventType(EventTypeEnum.ERROR);
        event.setTimecode(start.getMillis());

        if(errorState)
            event.setEventData(1 << DataFlagEnums.ErrorEventFlags.VSS_FAULT.getIndex());

        return event;
    }

}
