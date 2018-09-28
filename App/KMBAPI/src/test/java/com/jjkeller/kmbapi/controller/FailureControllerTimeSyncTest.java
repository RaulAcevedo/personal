package com.jjkeller.kmbapi.controller;

import android.os.Bundle;

import com.jjkeller.kmbapi.common.TestBase;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.configuration.LoginCredentials;
import com.jjkeller.kmbapi.configuration.UserState;
import com.jjkeller.kmbapi.controller.dataaccess.EmployeeLogEldEventFacade;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.employeelogeldevents.EmployeeLogEldEventCode;
import com.jjkeller.kmbapi.employeelogeldevents.Enums;
import com.jjkeller.kmbapi.enums.DriverTypeEnum;
import com.jjkeller.kmbapi.enums.Malfunction;
import com.jjkeller.kmbapi.enums.RuleSetTypeEnum;
import com.jjkeller.kmbapi.enums.TimeZoneEnum;
import com.jjkeller.kmbapi.featuretoggle.FeatureToggleService;
import com.jjkeller.kmbapi.proxydata.CompanyConfigSettings;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;
import com.jjkeller.kmbapi.realtime.MalfunctionManager;


import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(application = GlobalState.class,
        manifest = "/build/intermediates/bundles/encompass/debug/AndroidManifest.xml"
)
public class FailureControllerTimeSyncTest extends TestBase {

    private GlobalState app;
    private FeatureToggleService ftService;
    private EmployeeLogEldEventFacade eldEventFacade;
    private boolean eldMandateEnabled = true;

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        app = (GlobalState) RuntimeEnvironment.application;

        ftService = mock(FeatureToggleService.class);
        when(ftService.getIsEldMandateEnabled()).thenAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                return eldMandateEnabled;
            }
        });

        Field field = GlobalState.class.getDeclaredField("_featureToggleService");
        field.setAccessible(true);
        field.set(app, ftService);

        User user = mock(User.class);
        UserState userState = new UserState();
        when(user.getUserState()).thenReturn(userState);
        when(user.getHomeTerminalTimeZone()).thenReturn(TimeZoneEnum.CENTRAL_STANDARD_TIME);
        when(user.getRulesetTypeEnum()).thenReturn(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR));
        when(user.getDriverType()).thenReturn(new DriverTypeEnum(DriverTypeEnum.PROPERTYCARRYING));

        CompanyConfigSettings settings = new CompanyConfigSettings();
        settings.setDailyLogStartTime("00:00");
        settings.setDmoCompanyName("JesseCorp");
        app.setCompanyConfigSettings(app, settings);

        LoginCredentials creds = mock(LoginCredentials.class);
        when(creds.getEmployeeId()).thenReturn("00000000-0000-0000-0000-000000000000");
        when(user.getCredentials()).thenReturn(creds);

        ArrayList<User> userList = new ArrayList<User>();
        userList.add(user);
        app.setLoggedInUserList(userList);
        app.setCurrentUser(user);

        MalfunctionManager.getInstance().init();

        eldEventFacade = new EmployeeLogEldEventFacade(app, user);
    }


    @After
    public void tearDown() throws NoSuchFieldException, IllegalAccessException {
        closeDatabase(app);
    }

    private void assertTimingMalfunctionEventWasCreated() {
        EmployeeLogEldEvent[] logEldEvents = eldEventFacade.GetByEventTypes(-1,
                Arrays.asList(new Integer[]{Enums.EmployeeLogEldEventType.Malfunction_DataDiagnosticDetection.getValue()}),
                Arrays.asList(new Integer[]{Enums.EmployeeLogEldEventRecordStatus.Active.getValue()})
        );

        assertNotNull(logEldEvents);
        assertEquals("expected to find log events", 1, logEldEvents.length);

        EmployeeLogEldEvent loggedEldEvent = logEldEvents[0];

        assertEquals("event code incorrect", EmployeeLogEldEventCode.EldMalfunctionLogged, loggedEldEvent.getEventCode());
        assertEquals("event type incorrect", Enums.EmployeeLogEldEventType.Malfunction_DataDiagnosticDetection, loggedEldEvent.getEventType());
        assertEquals("malfunction code incorrect",  Malfunction.TIMING_COMPLIANCE.getDmoValue(), loggedEldEvent.getDiagnosticCode());
    }

    private void assertTimingMalfunctionEventNotPresent() {
        EmployeeLogEldEvent[] logEldEvents = eldEventFacade.GetByEventTypes(-1,
                Arrays.asList(new Integer[]{Enums.EmployeeLogEldEventType.Malfunction_DataDiagnosticDetection.getValue()}),
                Arrays.asList(new Integer[]{Enums.EmployeeLogEldEventRecordStatus.Active.getValue()})
        );

        assertNotNull(logEldEvents);
        assertEquals("expected to find log events", 0, logEldEvents.length);
    }



    @Test
    public void testTimeCheckWithIgnoreServerTime() {
        when(ftService.getIgnoreServerTime()).thenReturn(true);

        FailureController failureController = ControllerFactory.getInstance().getFailureController();
        Date d = DateUtility.AddMinutes(new Date(), 10);
        Date gpsTime = new Date();

        assertTimingMalfunctionEventNotPresent();

        Bundle responseBundle = failureController.DetermineIfEOBRClockSyncIsNecessary(d, gpsTime);
        assertTimingMalfunctionEventWasCreated();

        assertEquals("bundle isInFailure incorrect", Boolean.FALSE, responseBundle.getBoolean("isInFailure"));
        assertEquals("bundle requiresSynchronization incorrect", Boolean.FALSE, responseBundle.getBoolean("requiresSynchronization"));
        assertEquals("bundle clockDifference incorrect", 0l, responseBundle.getLong("clockDifference"));
    }

    @Test
    public void testTimeCheckWithIgnoreServerTimeVALIDTime() {
        when(ftService.getIgnoreServerTime()).thenReturn(true);

        FailureController failureController = ControllerFactory.getInstance().getFailureController();
        Date d = DateUtility.AddMinutes(new Date(), 5);
        Date gpsTime = new Date();

        assertTimingMalfunctionEventNotPresent();

        Bundle responseBundle = failureController.DetermineIfEOBRClockSyncIsNecessary(d, gpsTime);

        assertTimingMalfunctionEventNotPresent();

        assertEquals("bundle isInFailure incorrect", Boolean.FALSE, responseBundle.getBoolean("isInFailure"));
        assertEquals("bundle requiresSynchronization incorrect", Boolean.FALSE, responseBundle.getBoolean("requiresSynchronization"));
        assertEquals("bundle clockDifference incorrect", 0l, responseBundle.getLong("clockDifference"));
    }

    @Test
    public void testServerTimeAhead() {
        when(ftService.getIgnoreServerTime()).thenReturn(false);

        FailureController origFailController = ControllerFactory.getInstance().getFailureController();
        FailureController failureController = Mockito.spy(origFailController);

        Date now = new Date();
        Date d = DateUtility.AddDays(new Date(), 20);
        Date gpsTime = new Date();

        doReturn(d).when(failureController).GetDmoTime();

        assertTimingMalfunctionEventNotPresent();

        Bundle responseBundle = failureController.DetermineIfEOBRClockSyncIsNecessary(now, gpsTime);
        assertTimingMalfunctionEventWasCreated();

        //verify response in bundles
        assertEquals("bundle isInFailure incorrect", Boolean.TRUE, responseBundle.getBoolean("isInFailure"));
        assertEquals("bundle requiresSynchronization incorrect", Boolean.TRUE, responseBundle.getBoolean("requiresSynchronization"));
        assertTrue(responseBundle.getLong("clockDifference") > 0);
    }


    @Test
    public void testELDMandateDisabled() {
        eldMandateEnabled = false;

        FailureController origFailController = ControllerFactory.getInstance().getFailureController();
        FailureController failureController = Mockito.spy(origFailController);

        Date now = new Date();
        Date d = DateUtility.AddDays(new Date(), 20);
        Date gpsTime = new Date();

        doReturn(d).when(failureController).GetDmoTime();

        assertTimingMalfunctionEventNotPresent();

        Bundle responseBundle = failureController.DetermineIfEOBRClockSyncIsNecessary(now, gpsTime);

        assertTimingMalfunctionEventNotPresent();

        //verify response in bundles
        assertEquals("bundle isInFailure incorrect", Boolean.TRUE, responseBundle.getBoolean("isInFailure"));
        assertEquals("bundle requiresSynchronization incorrect", Boolean.TRUE, responseBundle.getBoolean("requiresSynchronization"));
        assertTrue(responseBundle.getLong("clockDifference") > 0);
    }

    @Test
    public void testServerTimeInSync() {
        when(ftService.getIgnoreServerTime()).thenReturn(false);

        FailureController origFailController = ControllerFactory.getInstance().getFailureController();
        FailureController failureController = Mockito.spy(origFailController);

        Date now = new Date();
        Date d = new Date();
        Date gpsTime = new Date();

        doReturn(d).when(failureController).GetDmoTime();

        assertTimingMalfunctionEventNotPresent();

        Bundle responseBundle = failureController.DetermineIfEOBRClockSyncIsNecessary(now, gpsTime);

        assertTimingMalfunctionEventNotPresent();

        //verify response in bundles
        assertEquals("bundle isInFailure incorrect", Boolean.FALSE, responseBundle.getBoolean("isInFailure"));
        assertEquals("bundle requiresSynchronization incorrect", Boolean.FALSE, responseBundle.getBoolean("requiresSynchronization"));
        assertEquals("bundle clockDifference incorrect", 0l, responseBundle.getLong("clockDifference"));
    }

    @Test
    public void testServerTimeWithin9Mins() {
        when(ftService.getIgnoreServerTime()).thenReturn(false);

        FailureController origFailController = ControllerFactory.getInstance().getFailureController();
        FailureController failureController = Mockito.spy(origFailController);

        Date now = new Date();
        Date d = DateUtility.AddMinutes(new Date(), 9);
        Date gpsTime = new Date();

        doReturn(d).when(failureController).GetDmoTime();

        assertTimingMalfunctionEventNotPresent();

        Bundle responseBundle = failureController.DetermineIfEOBRClockSyncIsNecessary(now, gpsTime);

        assertTimingMalfunctionEventNotPresent();

        assertEquals("bundle isInFailure incorrect", Boolean.FALSE, responseBundle.getBoolean("isInFailure"));

        assertEquals("bundle requiresSynchronization incorrect", Boolean.FALSE, responseBundle.getBoolean("requiresSynchronization"));

        assertEquals("bundle clockDifference incorrect", 0, responseBundle.getLong("clockDifference"));
    }

    @Test
    public void testServerTimeNotBetweenEldMandateAndLegacyThreshold() {
        when(ftService.getIgnoreServerTime()).thenReturn(false);

        FailureController origFailController = ControllerFactory.getInstance().getFailureController();
        FailureController failureController = Mockito.spy(origFailController);

        Date now = new Date();
        Date d = DateUtility.AddMinutes(new Date(), 15);
        Date gpsTime = new Date();

        doReturn(d).when(failureController).GetDmoTime();

        assertTimingMalfunctionEventNotPresent();

        Bundle responseBundle = failureController.DetermineIfEOBRClockSyncIsNecessary(now, gpsTime);

        assertTimingMalfunctionEventWasCreated();

        assertEquals("bundle isInFailure incorrect", Boolean.TRUE, responseBundle.getBoolean("isInFailure"));

        assertEquals("bundle requiresSynchronization incorrect", Boolean.TRUE, responseBundle.getBoolean("requiresSynchronization"));
        assertTrue(responseBundle.getLong("clockDifference") > 0);
    }

    @Test
    public void testServerTimeOver30mins() {
        when(ftService.getIgnoreServerTime()).thenReturn(false);

        FailureController origFailController = ControllerFactory.getInstance().getFailureController();
        FailureController failureController = Mockito.spy(origFailController);

        Date now = new Date();
        Date d = DateUtility.AddMinutes(new Date(), 31);
        Date gpsTime = new Date();

        doReturn(d).when(failureController).GetDmoTime();

        assertTimingMalfunctionEventNotPresent();

        Bundle responseBundle = failureController.DetermineIfEOBRClockSyncIsNecessary(now, gpsTime);

        assertTimingMalfunctionEventWasCreated();

        //verify response in bundles
        assertEquals("bundle isInFailure incorrect", Boolean.TRUE, responseBundle.getBoolean("isInFailure"));
        // just fail here  dont sync time.
        assertEquals("bundle requiresSynchronization incorrect", Boolean.TRUE, responseBundle.getBoolean("requiresSynchronization"));
        assertTrue(responseBundle.getLong("clockDifference") > 0);
    }

    /**
     * cant access dmo server.
     * GPS clock is in sync with ELD
     */
    @Test
    public void testCantGetToDMO_InSync() {
        when(ftService.getIgnoreServerTime()).thenReturn(false);


        FailureController origFailController = ControllerFactory.getInstance().getFailureController();
        FailureController failureController = Mockito.spy(origFailController);

        Date d = new Date();
        doReturn(null).when(failureController).GetDmoTime();
        Date gpsTime = new Date();

        assertTimingMalfunctionEventNotPresent();

        Bundle responseBundle = failureController.DetermineIfEOBRClockSyncIsNecessary(d, gpsTime);

        assertTimingMalfunctionEventNotPresent();

        //verify response in bundles
        assertEquals("bundle isInFailure incorrect", Boolean.FALSE, responseBundle.getBoolean("isInFailure"));
        assertEquals("bundle requiresSynchronization incorrect", Boolean.FALSE, responseBundle.getBoolean("requiresSynchronization"));
        assertEquals("bundle clockDifference incorrect", 0l, responseBundle.getLong("clockDifference"));
    }
    /**
     * cant access dmo server, or gps time.
     * android local clock is in sync with ELD
     */
    @Test
    public void testCantGetToDMOorGPS_InSync(){
        when(ftService.getIgnoreServerTime()).thenReturn(false);


        FailureController origFailController = ControllerFactory.getInstance().getFailureController();
        FailureController failureController = Mockito.spy(origFailController);

        Date d = new Date();
        doReturn(null).when(failureController).GetDmoTime();
        Date gpsTime = null;

        assertTimingMalfunctionEventNotPresent();

        Bundle responseBundle = failureController.DetermineIfEOBRClockSyncIsNecessary(d, gpsTime);

        assertTimingMalfunctionEventNotPresent();

        //verify response in bundles
        assertEquals("bundle isInFailure incorrect", Boolean.FALSE, responseBundle.getBoolean("isInFailure"));
        assertEquals("bundle requiresSynchronization incorrect", Boolean.FALSE, responseBundle.getBoolean("requiresSynchronization"));
        assertEquals("bundle clockDifference incorrect", 0l, responseBundle.getLong("clockDifference"));
    }

    @Test
    public void testAndroidSystemClockTimeOver30minsWhenDMOandGPSisNotAvailable() {
        when(ftService.getIgnoreServerTime()).thenReturn(false);

        FailureController origFailController = ControllerFactory.getInstance().getFailureController();
        FailureController failureController = Mockito.spy(origFailController);

        Date now = new Date();
        Date d = DateUtility.AddMinutes(new Date(), 31);

        doReturn(null).when(failureController).GetDmoTime();
        doReturn(d).when(failureController).GetSystemTime();

        assertTimingMalfunctionEventNotPresent();

        Bundle responseBundle = failureController.DetermineIfEOBRClockSyncIsNecessary(now, null);

        assertTimingMalfunctionEventWasCreated();

        //verify response in bundles
        assertEquals("bundle isInFailure incorrect", Boolean.TRUE, responseBundle.getBoolean("isInFailure"));
        // just fail here  dont sync time.
        assertEquals("bundle requiresSynchronization incorrect", Boolean.TRUE, responseBundle.getBoolean("requiresSynchronization"));
        assertTrue(responseBundle.getLong("clockDifference") > 0);
    }

    /**
     * cant access dmo server.
     * android local clock is
     * 1. over the 10 min threshold for eld mandate
     * 2. not over the 30 min threshold for device timing malfunction.
     */
    @Test
    public void testCantGetToDMO_OverMandateThreshold_WithinFailureThreshold() {
        when(ftService.getIgnoreServerTime()).thenReturn(false);

        FailureController origFailController = ControllerFactory.getInstance().getFailureController();
        FailureController failureController = Mockito.spy(origFailController);

        Date d = DateUtility.AddMinutes(new Date(), 10);
        doReturn(null).when(failureController).GetDmoTime();
        Date gpsTime = new Date();

        assertTimingMalfunctionEventNotPresent();

        Bundle responseBundle = failureController.DetermineIfEOBRClockSyncIsNecessary(d, gpsTime);

        //do not log an malfunction if we cant get the time from DMO
        assertTimingMalfunctionEventNotPresent();

        //verify response in bundles
        assertEquals("bundle isInFailure incorrect", Boolean.FALSE, responseBundle.getBoolean("isInFailure"));
        // no sync here because we are not using DMO TIME
        assertEquals("bundle requiresSynchronization incorrect", Boolean.FALSE, responseBundle.getBoolean("requiresSynchronization"));
        assertEquals("bundle clockDifference incorrect", 0l, responseBundle.getLong("clockDifference"));

        assertTrue("bundle failure controller flag is set under these conditions", !failureController.getIsMobileClockSynchronized());

    }


    /**
     * cant access dmo server.
     * android local clock is
     * 1. over the 10 min threshold for eld mandate
     * 2. not over the 30 min threshold for device timing malfunction.
     */
    @Test
    public void testCantGetToDMO_OverFailureThreshold() {
        when(ftService.getIgnoreServerTime()).thenReturn(false);

        FailureController origFailController = ControllerFactory.getInstance().getFailureController();
        FailureController failureController = Mockito.spy(origFailController);

        Date d = DateUtility.AddMinutes(new Date(), 31);
        doReturn(null).when(failureController).GetDmoTime();
        doReturn(null).when(failureController).GetSystemTime();

        assertTimingMalfunctionEventNotPresent();

        Bundle responseBundle = failureController.DetermineIfEOBRClockSyncIsNecessary(d, null);

        //do not log an malfunction if we cant get the time from DMO
        assertTimingMalfunctionEventWasCreated();

        //verify response in bundles
        assertEquals("bundle isInFailure incorrect", Boolean.TRUE, responseBundle.getBoolean("isInFailure"));
        assertEquals("bundle requiresSynchronization incorrect", Boolean.FALSE, responseBundle.getBoolean("requiresSynchronization"));
        assertEquals("bundle clockDifference incorrect", 0l, responseBundle.getLong("clockDifference"));

        assertFalse("bundle failure controller flag is set under these conditions", failureController.getIsMobileClockSynchronized());

    }


    @Test
    public void verify2MalfunctionRecordsWontBeCreated() {
        when(ftService.getIgnoreServerTime()).thenReturn(false);

        FailureController origFailController = ControllerFactory.getInstance().getFailureController();
        FailureController failureController = Mockito.spy(origFailController);

        Date d = DateUtility.AddMinutes(new Date(), 31);
        doReturn(d).when(failureController).GetDmoTime();
        Date gpsTime = new Date();

        assertTimingMalfunctionEventNotPresent();


        Date now = new Date();
        Bundle responseBundle = failureController.DetermineIfEOBRClockSyncIsNecessary(now, gpsTime);
        Bundle responseBundle2 = failureController.DetermineIfEOBRClockSyncIsNecessary(now, gpsTime);

        //do not log an malfunction if we cant get the time from DMO
        assertEquals("should only create one malf detected row", 1, getTotalMalfunctionEvents());

    }

    private int getTotalMalfunctionEvents() {
        EmployeeLogEldEvent[] logEldEvents = eldEventFacade.GetByEventTypes(-1,
                Arrays.asList(new Integer[]{Enums.EmployeeLogEldEventType.Malfunction_DataDiagnosticDetection.getValue()}),
                Arrays.asList(new Integer[]{Enums.EmployeeLogEldEventRecordStatus.Active.getValue()})
        );

        assertNotNull(logEldEvents);
        return logEldEvents.length;
    }

}