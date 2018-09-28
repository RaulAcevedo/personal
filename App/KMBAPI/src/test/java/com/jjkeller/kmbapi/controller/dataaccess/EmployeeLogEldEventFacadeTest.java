package com.jjkeller.kmbapi.controller.dataaccess;

import com.jjkeller.kmbapi.common.TestBase;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.configuration.LoginCredentials;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.employeelogeldevents.Enums;
import com.jjkeller.kmbapi.enums.TimeZoneEnum;
import com.jjkeller.kmbapi.proxydata.CompanyConfigSettings;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings({"SpellCheckingInspection", "unused", "unchecked"})
@RunWith(RobolectricTestRunner.class)
@Config(application = GlobalState.class,
        manifest = "/build/intermediates/bundles/encompass/debug/AndroidManifest.xml"
)
public class EmployeeLogEldEventFacadeTest extends TestBase {
    ///region Constants
    private static final int EVENTTYPE_DUTYSTATUS = Enums.EmployeeLogEldEventType.DutyStatusChange.getValue();
    private static final int EVENTTYPE_DRV_INDICATION = Enums.EmployeeLogEldEventType.ChangeInDriversIndication.getValue();


    private static final int EVENTCODE_OFFDUTY = 1;
    private static final int EVENTCODE_SLEEPER = 2;
    private static final int EVENTCODE_DRIVING = 3;
    private static final int EVENTCODE_ONDUTY = 4;

    private static final int EVENTCODE_PC = 1;
    private static final int EVENTCODE_YM = 2;
    private static final int EVENTCODE_CLR_PCYM = 0;

    private static final int EVENTRECORDSTATUS_ACTIVE = 1;
    private static final int EVENTRECORDSTATUS_INACTIVECHANGED = 2;
    private static final int EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED = 3;
    private static final int EVENTRECORDSTATUS_INACTIVECHANGEREJECTED = 4;

    private static final int EVENTRECORDORIGIN_AUTOMATICALLYRECORDEDBYELD = 1;
    private static final int EVENTRECORDORIGIN_EDITEDBYDRIVER = 2;
    private static final int EVENTRECORDORIGIN_EDITEDOTHERTHANDRIVER = 3;
    @SuppressWarnings("unused")
    private static final int EVENTRECORDORIGIN_UNIDENTIFIEDDRIVERPROFILE = 4;
    private static final long MILLISECONDS_PER_MINUTE = 60000L;
    ///endregion

    ///region setUp / tearDown
    private GlobalState globalState;

    @Before
    public void setUp() throws Exception {
        globalState = (GlobalState) RuntimeEnvironment.application;

        User user = mock(User.class);
        when(user.getHomeTerminalTimeZone()).thenReturn(TimeZoneEnum.CENTRAL_STANDARD_TIME);

        LoginCredentials creds = mock(LoginCredentials.class);
        when(creds.getEmployeeId()).thenReturn("00000000-0000-0000-0000-000000000001");
        when(creds.getPrimaryKey()).thenReturn((long)0);
        when(user.getCredentials()).thenReturn(creds);

        ArrayList<User> userList = new ArrayList<User>();
        userList.add(user);
        globalState.setLoggedInUserList(userList);

        globalState.setCurrentUser(user);

        CompanyConfigSettings settings = new CompanyConfigSettings();
        settings.setDailyLogStartTime("00:00");
        settings.setDmoCompanyName("Test Company");
        globalState.setCompanyConfigSettings(globalState, settings);
    }

    @After
    public void tearDown() throws NoSuchFieldException, IllegalAccessException {
        closeDatabase(globalState);
    }
    ///endregion

    ///region getOriginalEldEvents Tests
    @Test
    public void test_getOriginalEldEvents_empty() {
        List<EmployeeLogEldEvent> inputEvents = new ArrayList<>();

        List<EmployeeLogEldEvent> actualResults = (List<EmployeeLogEldEvent>) invokePrivateMethod(new EmployeeLogEldEventFacade(GlobalState.getInstance().getApplicationContext(), null), "getOriginalEldEvents", inputEvents);

        Assert.assertEquals(0, actualResults.size());
    }

    @Test
    public void test_getOriginalEldEvents() {
        List<EmployeeLogEldEvent> inputEvents = getOriginalData();

        // add some Change Requests
        inputEvents.add(6, createEmployeeLogEldEvent("2016-07-30T16:15:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EVENTCODE_OFFDUTY, EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED, EVENTRECORDORIGIN_EDITEDBYDRIVER, null, null));
        inputEvents.add(createEmployeeLogEldEvent("2016-07-30T18:15:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EVENTCODE_ONDUTY, EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED, EVENTRECORDORIGIN_EDITEDBYDRIVER, "8564", null));


        List<EmployeeLogEldEvent> actualResults = (List<EmployeeLogEldEvent>) invokePrivateMethod(new EmployeeLogEldEventFacade(GlobalState.getInstance().getApplicationContext(), null), "getOriginalEldEvents", inputEvents);


        Assert.assertEquals(7, actualResults.size());
        Assert.assertEquals("Sat Jul 30 00:00:00 CDT 2016", actualResults.get(0).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_ACTIVE, (int) actualResults.get(0).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 05:30:00 CDT 2016", actualResults.get(1).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_ACTIVE, (int) actualResults.get(1).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 06:00:00 CDT 2016", actualResults.get(2).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_ACTIVE, (int) actualResults.get(2).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 11:30:00 CDT 2016", actualResults.get(3).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_ACTIVE, (int) actualResults.get(3).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 12:30:00 CDT 2016", actualResults.get(4).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_ACTIVE, (int) actualResults.get(4).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 16:00:00 CDT 2016", actualResults.get(5).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_ACTIVE, (int) actualResults.get(5).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 16:30:00 CDT 2016", actualResults.get(6).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_ACTIVE, (int) actualResults.get(6).getEventRecordStatus());
    }
    ///endregion

    ///region previewChangeRequests Tests
    @Test
    public void test_previewChangeRequests_empty() {
        List<EmployeeLogEldEvent> inputEvents = new ArrayList<>();

        List<EmployeeLogEldEvent> actualResults = (List<EmployeeLogEldEvent>) invokePrivateMethod(new EmployeeLogEldEventFacade(GlobalState.getInstance().getApplicationContext(), null), "previewChangeRequests", inputEvents);

        Assert.assertEquals(0, actualResults.size());
    }

    @Test
    public void test_previewChangeRequests_addNewDutyStatus_noOverlap() {
        List<EmployeeLogEldEvent> inputEvents = getOriginalData();

        // add some Change Requests
        inputEvents.add(4, createEmployeeLogEldEvent("2016-07-30T11:45:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EVENTCODE_OFFDUTY, EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED, EVENTRECORDORIGIN_EDITEDBYDRIVER, null, 30 * MILLISECONDS_PER_MINUTE));

        List<EmployeeLogEldEvent> actualResults = (List<EmployeeLogEldEvent>) invokePrivateMethod(new EmployeeLogEldEventFacade(GlobalState.getInstance().getApplicationContext(), null), "previewChangeRequests", inputEvents);


        Assert.assertEquals(8, actualResults.size());

        Assert.assertEquals("Sat Jul 30 00:00:00 CDT 2016", actualResults.get(0).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_ACTIVE, (int) actualResults.get(0).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 05:30:00 CDT 2016", actualResults.get(1).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_ACTIVE, (int) actualResults.get(1).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 06:00:00 CDT 2016", actualResults.get(2).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_ACTIVE, (int) actualResults.get(2).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 11:30:00 CDT 2016", actualResults.get(3).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_ACTIVE, (int) actualResults.get(3).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 11:45:00 CDT 2016", actualResults.get(4).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED, (int) actualResults.get(4).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 12:30:00 CDT 2016", actualResults.get(5).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_ACTIVE, (int) actualResults.get(5).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 16:00:00 CDT 2016", actualResults.get(6).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_ACTIVE, (int) actualResults.get(6).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 16:30:00 CDT 2016", actualResults.get(7).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_ACTIVE, (int) actualResults.get(7).getEventRecordStatus());
    }

    @Test
    public void test_previewChangeRequests_addNewDutyStatus_overlapExistingRecords() {
        List<EmployeeLogEldEvent> inputEvents = getOriginalData();

        // add some Change Requests
        inputEvents.add(6, createEmployeeLogEldEvent("2016-07-30T16:15:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EVENTCODE_SLEEPER, EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED, EVENTRECORDORIGIN_EDITEDBYDRIVER, null, 120 * MILLISECONDS_PER_MINUTE));
        inputEvents.add(7, createEmployeeLogEldEvent("2016-07-30T18:15:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EVENTCODE_OFFDUTY, EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED, EVENTRECORDORIGIN_EDITEDBYDRIVER, null, 344 * MILLISECONDS_PER_MINUTE));

        List<EmployeeLogEldEvent> actualResults = (List<EmployeeLogEldEvent>) invokePrivateMethod(new EmployeeLogEldEventFacade(GlobalState.getInstance().getApplicationContext(), null), "previewChangeRequests", inputEvents);


        Assert.assertEquals(8, actualResults.size());

        Assert.assertEquals("Sat Jul 30 00:00:00 CDT 2016", actualResults.get(0).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_ACTIVE, (int) actualResults.get(0).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 05:30:00 CDT 2016", actualResults.get(1).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_ACTIVE, (int) actualResults.get(1).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 06:00:00 CDT 2016", actualResults.get(2).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_ACTIVE, (int) actualResults.get(2).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 11:30:00 CDT 2016", actualResults.get(3).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_ACTIVE, (int) actualResults.get(3).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 12:30:00 CDT 2016", actualResults.get(4).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_ACTIVE, (int) actualResults.get(4).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 16:00:00 CDT 2016", actualResults.get(5).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_ACTIVE, (int) actualResults.get(5).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 16:15:00 CDT 2016", actualResults.get(6).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED, (int) actualResults.get(6).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 18:15:00 CDT 2016", actualResults.get(7).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED, (int) actualResults.get(7).getEventRecordStatus());
    }

    @Test
    public void test_previewChangeRequests_editStartTimeOfExistingRecord() {
        List<EmployeeLogEldEvent> inputEvents = getOriginalData();

        // change 5:30 am - 6:00 am to 4:45 am - 6:00 am
        inputEvents.add(6, createEmployeeLogEldEvent("2016-07-30T04:45:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EVENTCODE_SLEEPER, EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED, EVENTRECORDORIGIN_EDITEDBYDRIVER, null, 75 * MILLISECONDS_PER_MINUTE));

        List<EmployeeLogEldEvent> actualResults = (List<EmployeeLogEldEvent>) invokePrivateMethod(new EmployeeLogEldEventFacade(GlobalState.getInstance().getApplicationContext(), null), "previewChangeRequests", inputEvents);


        Assert.assertEquals(7, actualResults.size());

        Assert.assertEquals("Sat Jul 30 00:00:00 CDT 2016", actualResults.get(0).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_ACTIVE, (int) actualResults.get(0).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 04:45:00 CDT 2016", actualResults.get(1).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED, (int) actualResults.get(1).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 06:00:00 CDT 2016", actualResults.get(2).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_ACTIVE, (int) actualResults.get(2).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 11:30:00 CDT 2016", actualResults.get(3).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_ACTIVE, (int) actualResults.get(3).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 12:30:00 CDT 2016", actualResults.get(4).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_ACTIVE, (int) actualResults.get(4).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 16:00:00 CDT 2016", actualResults.get(5).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_ACTIVE, (int) actualResults.get(5).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 16:30:00 CDT 2016", actualResults.get(6).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_ACTIVE, (int) actualResults.get(6).getEventRecordStatus());
    }

    @Test
    public void test_previewChangeRequests_editEndTimeOfExistingRecord() {
        List<EmployeeLogEldEvent> inputEvents = getOriginalData();

        // change 4:00 pm - 4:30 pm to 4:00 pm - 6:00 pm
        inputEvents.add(6, createEmployeeLogEldEvent("2016-07-30T16:00:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EVENTCODE_ONDUTY, EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED, EVENTRECORDORIGIN_EDITEDBYDRIVER, null, 120 * MILLISECONDS_PER_MINUTE));
        inputEvents.add(7, createEmployeeLogEldEvent("2016-07-30T18:00:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EVENTCODE_OFFDUTY, EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED, EVENTRECORDORIGIN_EDITEDBYDRIVER, null, 359 * MILLISECONDS_PER_MINUTE));

        List<EmployeeLogEldEvent> actualResults = (List<EmployeeLogEldEvent>) invokePrivateMethod(new EmployeeLogEldEventFacade(GlobalState.getInstance().getApplicationContext(), null), "previewChangeRequests", inputEvents);


        Assert.assertEquals(7, actualResults.size());

        Assert.assertEquals("Sat Jul 30 00:00:00 CDT 2016", actualResults.get(0).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_ACTIVE, (int) actualResults.get(0).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 05:30:00 CDT 2016", actualResults.get(1).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_ACTIVE, (int) actualResults.get(1).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 06:00:00 CDT 2016", actualResults.get(2).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_ACTIVE, (int) actualResults.get(2).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 11:30:00 CDT 2016", actualResults.get(3).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_ACTIVE, (int) actualResults.get(3).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 12:30:00 CDT 2016", actualResults.get(4).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_ACTIVE, (int) actualResults.get(4).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 16:00:00 CDT 2016", actualResults.get(5).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED, (int) actualResults.get(5).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 18:00:00 CDT 2016", actualResults.get(6).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED, (int) actualResults.get(6).getEventRecordStatus());
    }

    @Test
    public void test_previewChangeRequests_addPersonalConveyance() {
        List<EmployeeLogEldEvent> inputEvents = getOriginalData();

        // add PC 4:32 am - 9:45 am
        inputEvents.add(6, createEmployeeLogEldEvent("2016-07-30T04:32:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EVENTCODE_OFFDUTY, EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED, EVENTRECORDORIGIN_EDITEDOTHERTHANDRIVER, null, 313 * MILLISECONDS_PER_MINUTE));
        inputEvents.add(7, createEmployeeLogEldEvent("2016-07-30T04:32:00Z", Enums.EmployeeLogEldEventType.ChangeInDriversIndication, EVENTCODE_PC, EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED, EVENTRECORDORIGIN_EDITEDOTHERTHANDRIVER, null, 313 * MILLISECONDS_PER_MINUTE));
        inputEvents.add(8, createEmployeeLogEldEvent("2016-07-30T09:45:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EVENTCODE_DRIVING, EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED, EVENTRECORDORIGIN_EDITEDOTHERTHANDRIVER, null, 105 * MILLISECONDS_PER_MINUTE));
        inputEvents.add(9, createEmployeeLogEldEvent("2016-07-30T09:45:00Z", Enums.EmployeeLogEldEventType.ChangeInDriversIndication, EVENTCODE_CLR_PCYM, EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED, EVENTRECORDORIGIN_EDITEDOTHERTHANDRIVER, null, 105 * MILLISECONDS_PER_MINUTE));

        List<EmployeeLogEldEvent> actualResults = (List<EmployeeLogEldEvent>) invokePrivateMethod(new EmployeeLogEldEventFacade(GlobalState.getInstance().getApplicationContext(), null), "previewChangeRequests", inputEvents);

        Assert.assertEquals(9, actualResults.size());

        assertEvent(actualResults.get(0), "Sat Jul 30 00:00:00 CDT 2016", EVENTRECORDSTATUS_ACTIVE, EVENTTYPE_DUTYSTATUS, EVENTCODE_OFFDUTY);
        assertEvent(actualResults.get(1), "Sat Jul 30 04:32:00 CDT 2016", EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED, EVENTTYPE_DUTYSTATUS, EVENTCODE_OFFDUTY);
        assertEvent(actualResults.get(2), "Sat Jul 30 04:32:00 CDT 2016", EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED, EVENTTYPE_DRV_INDICATION, EVENTCODE_PC);
        assertEvent(actualResults.get(3), "Sat Jul 30 09:45:00 CDT 2016", EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED, EVENTTYPE_DUTYSTATUS, EVENTCODE_DRIVING);
        assertEvent(actualResults.get(4), "Sat Jul 30 09:45:00 CDT 2016", EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED, EVENTTYPE_DRV_INDICATION, EVENTCODE_CLR_PCYM);
        assertEvent(actualResults.get(5), "Sat Jul 30 11:30:00 CDT 2016", EVENTRECORDSTATUS_ACTIVE, EVENTTYPE_DUTYSTATUS, EVENTCODE_ONDUTY);
        assertEvent(actualResults.get(6), "Sat Jul 30 12:30:00 CDT 2016", EVENTRECORDSTATUS_ACTIVE, EVENTTYPE_DUTYSTATUS, EVENTCODE_DRIVING);
        assertEvent(actualResults.get(7), "Sat Jul 30 16:00:00 CDT 2016", EVENTRECORDSTATUS_ACTIVE, EVENTTYPE_DUTYSTATUS, EVENTCODE_ONDUTY);
        assertEvent(actualResults.get(8), "Sat Jul 30 16:30:00 CDT 2016", EVENTRECORDSTATUS_ACTIVE, EVENTTYPE_DUTYSTATUS, EVENTCODE_OFFDUTY);
    }

    @Test
    public void test_previewChangeRequests_DriverIndication_PCtoYM() {
        List<EmployeeLogEldEvent> inputEvents = getDriverIndicationData();

        // add YM 12:00 pm - 4:30 pm
        inputEvents.add(11, createEmployeeLogEldEvent("2016-07-30T12:00:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EVENTCODE_ONDUTY, EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED, EVENTRECORDORIGIN_EDITEDOTHERTHANDRIVER, "8564", 270 * MILLISECONDS_PER_MINUTE));
        inputEvents.add(12, createEmployeeLogEldEvent("2016-07-30T12:00:00Z", Enums.EmployeeLogEldEventType.ChangeInDriversIndication, EVENTCODE_YM, EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED, EVENTRECORDORIGIN_EDITEDOTHERTHANDRIVER, null, 0 * MILLISECONDS_PER_MINUTE));
        inputEvents.add(15, createEmployeeLogEldEvent("2016-07-30T16:30:00Z", Enums.EmployeeLogEldEventType.ChangeInDriversIndication, EVENTCODE_CLR_PCYM, EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED, EVENTRECORDORIGIN_EDITEDOTHERTHANDRIVER, null, 0 * MILLISECONDS_PER_MINUTE));

        List<EmployeeLogEldEvent> actualResults = (List<EmployeeLogEldEvent>) invokePrivateMethod(new EmployeeLogEldEventFacade(GlobalState.getInstance().getApplicationContext(), null), "previewChangeRequests", inputEvents);

        Assert.assertEquals(13, actualResults.size());

        assertEvent(actualResults.get(8), "Sat Jul 30 11:30:00 CDT 2016", EVENTRECORDSTATUS_ACTIVE, EVENTTYPE_DUTYSTATUS, EVENTCODE_ONDUTY);
        assertEvent(actualResults.get(9), "Sat Jul 30 12:00:00 CDT 2016", EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED, EVENTTYPE_DUTYSTATUS, EVENTCODE_ONDUTY);
        assertEvent(actualResults.get(10), "Sat Jul 30 12:00:00 CDT 2016", EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED, EVENTTYPE_DRV_INDICATION, EVENTCODE_YM);
        assertEvent(actualResults.get(11), "Sat Jul 30 16:30:00 CDT 2016", EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED, EVENTTYPE_DRV_INDICATION, EVENTCODE_CLR_PCYM);
        assertEvent(actualResults.get(12), "Sat Jul 30 16:30:00 CDT 2016", EVENTRECORDSTATUS_ACTIVE, EVENTTYPE_DUTYSTATUS, EVENTCODE_OFFDUTY);
    }

    @Test
    public void test_previewChangeRequests_manualDrivingTime_editStartTime() {
        List<EmployeeLogEldEvent> inputEvents = getOriginalData();

        // change automatically generated Driving time 12:30 pm - 4:00 pm to 12:05 - 4:00
        inputEvents.add(6, createEmployeeLogEldEvent("2016-07-30T12:05:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EVENTCODE_DRIVING, EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED, EVENTRECORDORIGIN_EDITEDOTHERTHANDRIVER, null, 25 * MILLISECONDS_PER_MINUTE));

        List<EmployeeLogEldEvent> actualResults = (List<EmployeeLogEldEvent>) invokePrivateMethod(new EmployeeLogEldEventFacade(GlobalState.getInstance().getApplicationContext(), null), "previewChangeRequests", inputEvents);


        Assert.assertEquals(8, actualResults.size());

        Assert.assertEquals("Sat Jul 30 00:00:00 CDT 2016", actualResults.get(0).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_ACTIVE, (int) actualResults.get(0).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 05:30:00 CDT 2016", actualResults.get(1).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_ACTIVE, (int) actualResults.get(1).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 06:00:00 CDT 2016", actualResults.get(2).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_ACTIVE, (int) actualResults.get(2).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 11:30:00 CDT 2016", actualResults.get(3).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_ACTIVE, (int) actualResults.get(3).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 12:05:00 CDT 2016", actualResults.get(4).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED, (int) actualResults.get(4).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 12:30:00 CDT 2016", actualResults.get(5).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_ACTIVE, (int) actualResults.get(5).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 16:00:00 CDT 2016", actualResults.get(6).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_ACTIVE, (int) actualResults.get(6).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 16:30:00 CDT 2016", actualResults.get(7).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_ACTIVE, (int) actualResults.get(7).getEventRecordStatus());
    }

    @Test
    public void test_previewChangeRequests_manualDrivingTime_editEndTime() {
        List<EmployeeLogEldEvent> inputEvents = getOriginalData();

        // change automatically generated Driving time 12:30 pm - 4:00 pm to 12:30 - 4:15
        inputEvents.add(6, createEmployeeLogEldEvent("2016-07-30T16:00:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EVENTCODE_DRIVING, EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED, EVENTRECORDORIGIN_EDITEDOTHERTHANDRIVER, null, 15 * MILLISECONDS_PER_MINUTE));
        inputEvents.add(7, createEmployeeLogEldEvent("2016-07-30T16:15:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EVENTCODE_ONDUTY, EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED, EVENTRECORDORIGIN_EDITEDOTHERTHANDRIVER, null, 15 * MILLISECONDS_PER_MINUTE));

        List<EmployeeLogEldEvent> actualResults = (List<EmployeeLogEldEvent>) invokePrivateMethod(new EmployeeLogEldEventFacade(GlobalState.getInstance().getApplicationContext(), null), "previewChangeRequests", inputEvents);


        Assert.assertEquals(8, actualResults.size());

        Assert.assertEquals("Sat Jul 30 00:00:00 CDT 2016", actualResults.get(0).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_ACTIVE, (int) actualResults.get(0).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 05:30:00 CDT 2016", actualResults.get(1).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_ACTIVE, (int) actualResults.get(1).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 06:00:00 CDT 2016", actualResults.get(2).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_ACTIVE, (int) actualResults.get(2).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 11:30:00 CDT 2016", actualResults.get(3).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_ACTIVE, (int) actualResults.get(3).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 12:30:00 CDT 2016", actualResults.get(4).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_ACTIVE, (int) actualResults.get(4).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 16:00:00 CDT 2016", actualResults.get(5).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED, (int) actualResults.get(5).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 16:15:00 CDT 2016", actualResults.get(6).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED, (int) actualResults.get(6).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 16:30:00 CDT 2016", actualResults.get(7).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_ACTIVE, (int) actualResults.get(7).getEventRecordStatus());
    }

    @Test
    public void test_previewChangeRequests_manualDrivingTime_editBothStartTimeEndTime() {
        List<EmployeeLogEldEvent> inputEvents = getOriginalData();

        // change automatically generated Driving time 12:30 pm - 4:00 pm to 12:05 - 4:15
        inputEvents.add(6, createEmployeeLogEldEvent("2016-07-30T12:05:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EVENTCODE_DRIVING, EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED, EVENTRECORDORIGIN_EDITEDOTHERTHANDRIVER, null, 25 * MILLISECONDS_PER_MINUTE));
        inputEvents.add(7, createEmployeeLogEldEvent("2016-07-30T16:00:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EVENTCODE_DRIVING, EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED, EVENTRECORDORIGIN_EDITEDOTHERTHANDRIVER, null, 15 * MILLISECONDS_PER_MINUTE));
        inputEvents.add(8, createEmployeeLogEldEvent("2016-07-30T16:15:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EVENTCODE_ONDUTY, EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED, EVENTRECORDORIGIN_EDITEDOTHERTHANDRIVER, null, 15 * MILLISECONDS_PER_MINUTE));

        List<EmployeeLogEldEvent> actualResults = (List<EmployeeLogEldEvent>) invokePrivateMethod(new EmployeeLogEldEventFacade(GlobalState.getInstance().getApplicationContext(), null), "previewChangeRequests", inputEvents);


        Assert.assertEquals(9, actualResults.size());

        Assert.assertEquals("Sat Jul 30 00:00:00 CDT 2016", actualResults.get(0).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_ACTIVE, (int) actualResults.get(0).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 05:30:00 CDT 2016", actualResults.get(1).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_ACTIVE, (int) actualResults.get(1).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 06:00:00 CDT 2016", actualResults.get(2).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_ACTIVE, (int) actualResults.get(2).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 11:30:00 CDT 2016", actualResults.get(3).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_ACTIVE, (int) actualResults.get(3).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 12:30:00 CDT 2016", actualResults.get(5).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_ACTIVE, (int) actualResults.get(5).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 16:00:00 CDT 2016", actualResults.get(6).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED, (int) actualResults.get(6).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 16:15:00 CDT 2016", actualResults.get(7).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED, (int) actualResults.get(7).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 16:30:00 CDT 2016", actualResults.get(8).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_ACTIVE, (int) actualResults.get(8).getEventRecordStatus());
    }

    @Test
    public void test_previewChangeRequests_editDataOtherThanDutyStatusData() {
        List<EmployeeLogEldEvent> inputEvents = getOriginalData();

        // change Tractor Number for 12:30 pm
        EmployeeLogEldEvent eldEvent = createEmployeeLogEldEvent("2016-07-30T12:30:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EVENTCODE_DRIVING, EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED, EVENTRECORDORIGIN_EDITEDOTHERTHANDRIVER, null, 210 * MILLISECONDS_PER_MINUTE);
        eldEvent.setTractorNumber("1382");
        inputEvents.add(6, eldEvent);

        List<EmployeeLogEldEvent> actualResults = (List<EmployeeLogEldEvent>) invokePrivateMethod(new EmployeeLogEldEventFacade(GlobalState.getInstance().getApplicationContext(), null), "previewChangeRequests", inputEvents);


        Assert.assertEquals(7, actualResults.size());


        Assert.assertEquals("Sat Jul 30 00:00:00 CDT 2016", actualResults.get(0).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_ACTIVE, (int) actualResults.get(0).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 05:30:00 CDT 2016", actualResults.get(1).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_ACTIVE, (int) actualResults.get(1).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 06:00:00 CDT 2016", actualResults.get(2).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_ACTIVE, (int) actualResults.get(2).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 11:30:00 CDT 2016", actualResults.get(3).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_ACTIVE, (int) actualResults.get(3).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 12:30:00 CDT 2016", actualResults.get(4).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED, (int) actualResults.get(4).getEventRecordStatus());
        Assert.assertEquals("1382", actualResults.get(4).getTractorNumber());

        Assert.assertEquals("Sat Jul 30 16:00:00 CDT 2016", actualResults.get(5).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_ACTIVE, (int) actualResults.get(5).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 16:30:00 CDT 2016", actualResults.get(6).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_ACTIVE, (int) actualResults.get(6).getEventRecordStatus());
    }

    @Test
    public void test_previewChangeRequests_overlapEntireDay() {
        List<EmployeeLogEldEvent> inputEvents = getOriginalData();

        // add new Driving 12:00 am - 12:59 pm
        inputEvents.add(6, createEmployeeLogEldEvent("2016-07-30T00:00:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EVENTCODE_DRIVING, EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED, EVENTRECORDORIGIN_EDITEDOTHERTHANDRIVER, null, 1439 * MILLISECONDS_PER_MINUTE));

        List<EmployeeLogEldEvent> actualResults = (List<EmployeeLogEldEvent>) invokePrivateMethod(new EmployeeLogEldEventFacade(GlobalState.getInstance().getApplicationContext(), null), "previewChangeRequests", inputEvents);


        Assert.assertEquals(1, actualResults.size());

        Assert.assertEquals("Sat Jul 30 00:00:00 CDT 2016", actualResults.get(0).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED, (int) actualResults.get(0).getEventRecordStatus());
    }
    ///endregion

    ///region acceptChangeRequests Tests
    @Test
    public void test_acceptChangeRequests_empty() {
        List<EmployeeLogEldEvent> inputEvents = new ArrayList<>();

        List<EmployeeLogEldEvent> actualResults = (List<EmployeeLogEldEvent>) invokePrivateMethod(new EmployeeLogEldEventFacade(GlobalState.getInstance().getApplicationContext(), null), "acceptChangeRequests", inputEvents);

        Assert.assertEquals(0, actualResults.size());
    }

    @Test
    public void test_acceptChangeRequests_addNewDutyStatus_noOverlap() {
        List<EmployeeLogEldEvent> inputEvents = getOriginalData();

        // add some Change Requests
        inputEvents.add(6, createEmployeeLogEldEvent("2016-07-30T11:45:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EVENTCODE_OFFDUTY, EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED, EVENTRECORDORIGIN_EDITEDBYDRIVER, null, 30 * MILLISECONDS_PER_MINUTE));

        List<EmployeeLogEldEvent> actualResults = (List<EmployeeLogEldEvent>) invokePrivateMethod(new EmployeeLogEldEventFacade(GlobalState.getInstance().getApplicationContext(), null), "acceptChangeRequests", inputEvents);


        Assert.assertEquals(1, actualResults.size());

        Assert.assertEquals("Sat Jul 30 11:45:00 CDT 2016", actualResults.get(0).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_ACTIVE, (int) actualResults.get(0).getEventRecordStatus());
    }

    @Test
    public void test_acceptChangeRequests_addNewDutyStatus_overlapExistingRecords() {
        List<EmployeeLogEldEvent> inputEvents = getOriginalData();

        // add some Change Requests
        inputEvents.add(6, createEmployeeLogEldEvent("2016-07-30T16:15:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EVENTCODE_SLEEPER, EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED, EVENTRECORDORIGIN_EDITEDBYDRIVER, null, 120 * MILLISECONDS_PER_MINUTE));
        inputEvents.add(7, createEmployeeLogEldEvent("2016-07-30T18:15:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EVENTCODE_OFFDUTY, EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED, EVENTRECORDORIGIN_EDITEDBYDRIVER, null, 344 * MILLISECONDS_PER_MINUTE));

        List<EmployeeLogEldEvent> actualResults = (List<EmployeeLogEldEvent>) invokePrivateMethod(new EmployeeLogEldEventFacade(GlobalState.getInstance().getApplicationContext(), null), "acceptChangeRequests", inputEvents);


        Assert.assertEquals(3, actualResults.size());

        Assert.assertEquals("Sat Jul 30 16:15:00 CDT 2016", actualResults.get(0).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_ACTIVE, (int) actualResults.get(0).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 16:30:00 CDT 2016", actualResults.get(1).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_INACTIVECHANGED, (int) actualResults.get(1).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 18:15:00 CDT 2016", actualResults.get(2).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_ACTIVE, (int) actualResults.get(2).getEventRecordStatus());
    }

    @Test
    public void test_acceptChangeRequests_editStartTimeOfExistingRecord() {
        List<EmployeeLogEldEvent> inputEvents = getOriginalData();

        // change 5:30 am - 6:00 am to 4:45 am - 6:00 am
        inputEvents.add(6, createEmployeeLogEldEvent("2016-07-30T04:45:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EVENTCODE_SLEEPER, EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED, EVENTRECORDORIGIN_EDITEDBYDRIVER, null, 75 * MILLISECONDS_PER_MINUTE));

        List<EmployeeLogEldEvent> actualResults = (List<EmployeeLogEldEvent>) invokePrivateMethod(new EmployeeLogEldEventFacade(GlobalState.getInstance().getApplicationContext(), null), "acceptChangeRequests", inputEvents);


        Assert.assertEquals(2, actualResults.size());

        Assert.assertEquals("Sat Jul 30 04:45:00 CDT 2016", actualResults.get(0).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_ACTIVE, (int) actualResults.get(0).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 05:30:00 CDT 2016", actualResults.get(1).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_INACTIVECHANGED, (int) actualResults.get(1).getEventRecordStatus());
    }

    @Test
    public void test_acceptChangeRequests_editEndTimeOfExistingRecord() {
        List<EmployeeLogEldEvent> inputEvents = getOriginalData();

        // change 4:00 pm - 4:30 pm to 4:00 pm - 6:00 pm
        inputEvents.add(6, createEmployeeLogEldEvent("2016-07-30T16:00:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EVENTCODE_ONDUTY, EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED, EVENTRECORDORIGIN_EDITEDBYDRIVER, null, 120 * MILLISECONDS_PER_MINUTE));
        inputEvents.add(7, createEmployeeLogEldEvent("2016-07-30T18:00:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EVENTCODE_OFFDUTY, EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED, EVENTRECORDORIGIN_EDITEDBYDRIVER, null, 359 * MILLISECONDS_PER_MINUTE));

        List<EmployeeLogEldEvent> actualResults = (List<EmployeeLogEldEvent>) invokePrivateMethod(new EmployeeLogEldEventFacade(GlobalState.getInstance().getApplicationContext(), null), "acceptChangeRequests", inputEvents);


        Assert.assertEquals(4, actualResults.size());

        Assert.assertEquals("Sat Jul 30 16:00:00 CDT 2016", actualResults.get(0).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_INACTIVECHANGED, (int) actualResults.get(0).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 16:00:00 CDT 2016", actualResults.get(1).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_ACTIVE, (int) actualResults.get(1).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 16:30:00 CDT 2016", actualResults.get(2).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_INACTIVECHANGED, (int) actualResults.get(2).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 18:00:00 CDT 2016", actualResults.get(3).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_ACTIVE, (int) actualResults.get(3).getEventRecordStatus());
    }

    @Test
    public void test_acceptChangeRequests_addPersonalConveyance() {
        List<EmployeeLogEldEvent> inputEvents = getOriginalData();

        // add PC 4:32 am - 9:45 am
        inputEvents.add(6, createEmployeeLogEldEvent("2016-07-30T04:32:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EVENTCODE_OFFDUTY, EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED, EVENTRECORDORIGIN_EDITEDOTHERTHANDRIVER, null, 313 * MILLISECONDS_PER_MINUTE));
        inputEvents.add(7, createEmployeeLogEldEvent("2016-07-30T04:32:00Z", Enums.EmployeeLogEldEventType.ChangeInDriversIndication, EVENTCODE_PC, EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED, EVENTRECORDORIGIN_EDITEDOTHERTHANDRIVER, null, 313 * MILLISECONDS_PER_MINUTE));
        inputEvents.add(8, createEmployeeLogEldEvent("2016-07-30T09:45:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EVENTCODE_DRIVING, EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED, EVENTRECORDORIGIN_EDITEDOTHERTHANDRIVER, null, 105 * MILLISECONDS_PER_MINUTE));
        inputEvents.add(9, createEmployeeLogEldEvent("2016-07-30T09:45:00Z", Enums.EmployeeLogEldEventType.ChangeInDriversIndication, EVENTCODE_CLR_PCYM, EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED, EVENTRECORDORIGIN_EDITEDOTHERTHANDRIVER, null, 105 * MILLISECONDS_PER_MINUTE));

        List<EmployeeLogEldEvent> actualResults = (List<EmployeeLogEldEvent>) invokePrivateMethod(new EmployeeLogEldEventFacade(GlobalState.getInstance().getApplicationContext(), null), "acceptChangeRequests", inputEvents);


        Assert.assertEquals(6, actualResults.size());

        assertEvent(actualResults.get(0), "Sat Jul 30 04:32:00 CDT 2016", EVENTRECORDSTATUS_ACTIVE, EVENTTYPE_DUTYSTATUS, EVENTCODE_OFFDUTY);
        assertEvent(actualResults.get(1), "Sat Jul 30 04:32:00 CDT 2016", EVENTRECORDSTATUS_ACTIVE, EVENTTYPE_DRV_INDICATION, EVENTCODE_PC);
        assertEvent(actualResults.get(2), "Sat Jul 30 05:30:00 CDT 2016", EVENTRECORDSTATUS_INACTIVECHANGED, EVENTTYPE_DUTYSTATUS, EVENTCODE_ONDUTY);
        assertEvent(actualResults.get(3), "Sat Jul 30 06:00:00 CDT 2016", EVENTRECORDSTATUS_INACTIVECHANGED, EVENTTYPE_DUTYSTATUS, EVENTCODE_DRIVING);
        assertEvent(actualResults.get(4), "Sat Jul 30 09:45:00 CDT 2016", EVENTRECORDSTATUS_ACTIVE, EVENTTYPE_DUTYSTATUS, EVENTCODE_DRIVING);
        assertEvent(actualResults.get(5), "Sat Jul 30 09:45:00 CDT 2016", EVENTRECORDSTATUS_ACTIVE, EVENTTYPE_DRV_INDICATION, EVENTCODE_CLR_PCYM);
    }

    @Test
    public void test_acceptChangeRequests_DriverIndication_PCtoYM() {
        List<EmployeeLogEldEvent> inputEvents = getDriverIndicationData();

        // add YM 12:00 pm - 4:30 pm
        inputEvents.add(11, createEmployeeLogEldEvent("2016-07-30T12:00:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EVENTCODE_ONDUTY, EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED, EVENTRECORDORIGIN_EDITEDOTHERTHANDRIVER, "8564", 270 * MILLISECONDS_PER_MINUTE));
        inputEvents.add(12, createEmployeeLogEldEvent("2016-07-30T12:00:00Z", Enums.EmployeeLogEldEventType.ChangeInDriversIndication, EVENTCODE_YM, EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED, EVENTRECORDORIGIN_EDITEDOTHERTHANDRIVER, null, 0 * MILLISECONDS_PER_MINUTE));
        inputEvents.add(15, createEmployeeLogEldEvent("2016-07-30T16:30:00Z", Enums.EmployeeLogEldEventType.ChangeInDriversIndication, EVENTCODE_CLR_PCYM, EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED, EVENTRECORDORIGIN_EDITEDOTHERTHANDRIVER, null, 0 * MILLISECONDS_PER_MINUTE));

        List<EmployeeLogEldEvent> actualResults = (List<EmployeeLogEldEvent>) invokePrivateMethod(new EmployeeLogEldEventFacade(GlobalState.getInstance().getApplicationContext(), null), "acceptChangeRequests", inputEvents);

        Assert.assertEquals(5, actualResults.size());

        assertEvent(actualResults.get(0), "Sat Jul 30 12:00:00 CDT 2016", EVENTRECORDSTATUS_INACTIVECHANGED, EVENTTYPE_DUTYSTATUS, EVENTCODE_OFFDUTY);
        assertEvent(actualResults.get(1), "Sat Jul 30 12:00:00 CDT 2016", EVENTRECORDSTATUS_INACTIVECHANGED, EVENTTYPE_DRV_INDICATION, EVENTCODE_PC);
        assertEvent(actualResults.get(2), "Sat Jul 30 12:00:00 CDT 2016", EVENTRECORDSTATUS_ACTIVE, EVENTTYPE_DUTYSTATUS, EVENTCODE_ONDUTY);
        assertEvent(actualResults.get(3), "Sat Jul 30 12:00:00 CDT 2016", EVENTRECORDSTATUS_ACTIVE, EVENTTYPE_DRV_INDICATION, EVENTCODE_YM);
        assertEvent(actualResults.get(4), "Sat Jul 30 16:30:00 CDT 2016", EVENTRECORDSTATUS_ACTIVE, EVENTTYPE_DRV_INDICATION, EVENTCODE_CLR_PCYM);
    }

    @Test
    public void test_acceptChangeRequests_manualDrivingTime_editStartTime() {
        List<EmployeeLogEldEvent> inputEvents = getOriginalData();

        // change automatically generated Driving time 12:30 pm - 4:00 pm to 12:05 - 4:00
        inputEvents.add(6, createEmployeeLogEldEvent("2016-07-30T12:05:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EVENTCODE_DRIVING, EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED, EVENTRECORDORIGIN_EDITEDOTHERTHANDRIVER, null, 25 * MILLISECONDS_PER_MINUTE));

        List<EmployeeLogEldEvent> actualResults = (List<EmployeeLogEldEvent>) invokePrivateMethod(new EmployeeLogEldEventFacade(GlobalState.getInstance().getApplicationContext(), null), "acceptChangeRequests", inputEvents);


        Assert.assertEquals(1, actualResults.size());

        Assert.assertEquals("Sat Jul 30 12:05:00 CDT 2016", actualResults.get(0).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_ACTIVE, (int) actualResults.get(0).getEventRecordStatus());
    }

    @Test
    public void test_acceptChangeRequests_manualDrivingTime_editEndTime() {
        List<EmployeeLogEldEvent> inputEvents = getOriginalData();

        // change automatically generated Driving time 12:30 pm - 4:00 pm to 12:30 - 4:15
        inputEvents.add(6, createEmployeeLogEldEvent("2016-07-30T16:00:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EVENTCODE_DRIVING, EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED, EVENTRECORDORIGIN_EDITEDOTHERTHANDRIVER, null, 15 * MILLISECONDS_PER_MINUTE));
        inputEvents.add(7, createEmployeeLogEldEvent("2016-07-30T16:15:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EVENTCODE_ONDUTY, EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED, EVENTRECORDORIGIN_EDITEDOTHERTHANDRIVER, null, 15 * MILLISECONDS_PER_MINUTE));

        List<EmployeeLogEldEvent> actualResults = (List<EmployeeLogEldEvent>) invokePrivateMethod(new EmployeeLogEldEventFacade(GlobalState.getInstance().getApplicationContext(), null), "acceptChangeRequests", inputEvents);


        Assert.assertEquals(3, actualResults.size());

        Assert.assertEquals("Sat Jul 30 16:00:00 CDT 2016", actualResults.get(0).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_INACTIVECHANGED, (int) actualResults.get(0).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 16:00:00 CDT 2016", actualResults.get(1).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_ACTIVE, (int) actualResults.get(1).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 16:15:00 CDT 2016", actualResults.get(2).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_ACTIVE, (int) actualResults.get(2).getEventRecordStatus());
    }

    @Test
    public void test_acceptChangeRequests_manualDrivingTime_editBothStartTimeEndTime() {
        List<EmployeeLogEldEvent> inputEvents = getOriginalData();

        // change automatically generated Driving time 12:30 pm - 4:00 pm to 12:05 - 4:15
        inputEvents.add(6, createEmployeeLogEldEvent("2016-07-30T12:05:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EVENTCODE_DRIVING, EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED, EVENTRECORDORIGIN_EDITEDOTHERTHANDRIVER, null, 25 * MILLISECONDS_PER_MINUTE));
        inputEvents.add(7, createEmployeeLogEldEvent("2016-07-30T16:00:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EVENTCODE_DRIVING, EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED, EVENTRECORDORIGIN_EDITEDOTHERTHANDRIVER, null, 15 * MILLISECONDS_PER_MINUTE));
        inputEvents.add(8, createEmployeeLogEldEvent("2016-07-30T16:15:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EVENTCODE_ONDUTY, EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED, EVENTRECORDORIGIN_EDITEDOTHERTHANDRIVER, null, 15 * MILLISECONDS_PER_MINUTE));

        List<EmployeeLogEldEvent> actualResults = (List<EmployeeLogEldEvent>) invokePrivateMethod(new EmployeeLogEldEventFacade(GlobalState.getInstance().getApplicationContext(), null), "acceptChangeRequests", inputEvents);


        Assert.assertEquals(4, actualResults.size());

        Assert.assertEquals("Sat Jul 30 12:05:00 CDT 2016", actualResults.get(0).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_ACTIVE, (int) actualResults.get(0).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 16:00:00 CDT 2016", actualResults.get(1).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_INACTIVECHANGED, (int) actualResults.get(1).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 16:00:00 CDT 2016", actualResults.get(2).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_ACTIVE, (int) actualResults.get(2).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 16:15:00 CDT 2016", actualResults.get(3).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_ACTIVE, (int) actualResults.get(3).getEventRecordStatus());
    }

    @Test
    public void test_acceptChangeRequests_editDataOtherThanDutyStatusData() {
        List<EmployeeLogEldEvent> inputEvents = getOriginalData();

        // change Tractor Number for 12:30 pm
        EmployeeLogEldEvent eldEvent = createEmployeeLogEldEvent("2016-07-30T12:30:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EVENTCODE_DRIVING, EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED, EVENTRECORDORIGIN_EDITEDOTHERTHANDRIVER, null, 210 * MILLISECONDS_PER_MINUTE);
        eldEvent.setTractorNumber("1382");
        inputEvents.add(6, eldEvent);

        List<EmployeeLogEldEvent> actualResults = (List<EmployeeLogEldEvent>) invokePrivateMethod(new EmployeeLogEldEventFacade(GlobalState.getInstance().getApplicationContext(), null), "acceptChangeRequests", inputEvents);


        Assert.assertEquals(2, actualResults.size());

        Assert.assertEquals("Sat Jul 30 12:30:00 CDT 2016", actualResults.get(0).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_INACTIVECHANGED, (int) actualResults.get(0).getEventRecordStatus());
        Assert.assertEquals("8564", actualResults.get(0).getTractorNumber());

        Assert.assertEquals("Sat Jul 30 12:30:00 CDT 2016", actualResults.get(1).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_ACTIVE, (int) actualResults.get(1).getEventRecordStatus());
        Assert.assertEquals("1382", actualResults.get(1).getTractorNumber());
    }

    @Test
    public void test_acceptChangeRequests_overlapEntireDay() {
        List<EmployeeLogEldEvent> inputEvents = getOriginalData();

        // add new Driving 12:00 am - 12:59 pm
        inputEvents.add(6, createEmployeeLogEldEvent("2016-07-30T00:00:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EVENTCODE_DRIVING, EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED, EVENTRECORDORIGIN_EDITEDOTHERTHANDRIVER, null, 1439 * MILLISECONDS_PER_MINUTE));

        List<EmployeeLogEldEvent> actualResults = (List<EmployeeLogEldEvent>) invokePrivateMethod(new EmployeeLogEldEventFacade(GlobalState.getInstance().getApplicationContext(), null), "acceptChangeRequests", inputEvents);


        Assert.assertEquals(8, actualResults.size());

        Assert.assertEquals("Sat Jul 30 00:00:00 CDT 2016", actualResults.get(0).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_INACTIVECHANGED, (int) actualResults.get(0).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 00:00:00 CDT 2016", actualResults.get(1).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_ACTIVE, (int) actualResults.get(1).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 05:30:00 CDT 2016", actualResults.get(2).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_INACTIVECHANGED, (int) actualResults.get(2).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 06:00:00 CDT 2016", actualResults.get(3).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_INACTIVECHANGED, (int) actualResults.get(3).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 11:30:00 CDT 2016", actualResults.get(4).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_INACTIVECHANGED, (int) actualResults.get(4).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 12:30:00 CDT 2016", actualResults.get(5).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_INACTIVECHANGED, (int) actualResults.get(5).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 16:00:00 CDT 2016", actualResults.get(6).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_INACTIVECHANGED, (int) actualResults.get(6).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 16:30:00 CDT 2016", actualResults.get(7).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_INACTIVECHANGED, (int) actualResults.get(7).getEventRecordStatus());
    }
    ///endregion

    ///region rejectChangeRequests Tests
    @Test
    public void test_rejectChangeRequests_empty() {
        List<EmployeeLogEldEvent> inputEvents = new ArrayList<>();

        List<EmployeeLogEldEvent> actualResults = (List<EmployeeLogEldEvent>) invokePrivateMethod(new EmployeeLogEldEventFacade(GlobalState.getInstance().getApplicationContext(), null), "rejectChangeRequests", inputEvents);

        Assert.assertEquals(0, actualResults.size());
    }

    @Test
    public void test_rejectChangeRequests_TwoEdits() {
        List<EmployeeLogEldEvent> inputEvents = getOriginalData();

        // add some Change Requests
        inputEvents.add(6, createEmployeeLogEldEvent("2016-07-30T16:15:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EVENTCODE_OFFDUTY, EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED, EVENTRECORDORIGIN_EDITEDBYDRIVER, null, null));
        inputEvents.add(createEmployeeLogEldEvent("2016-07-30T18:15:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EVENTCODE_ONDUTY, EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED, EVENTRECORDORIGIN_EDITEDBYDRIVER, "8564", null));


        List<EmployeeLogEldEvent> actualResults = (List<EmployeeLogEldEvent>) invokePrivateMethod(new EmployeeLogEldEventFacade(GlobalState.getInstance().getApplicationContext(), null), "rejectChangeRequests", inputEvents);


        Assert.assertEquals(2, actualResults.size());
        Assert.assertEquals("Sat Jul 30 16:15:00 CDT 2016", actualResults.get(0).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_INACTIVECHANGEREJECTED, (int) actualResults.get(0).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 18:15:00 CDT 2016", actualResults.get(1).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_INACTIVECHANGEREJECTED, (int) actualResults.get(1).getEventRecordStatus());
    }

    @Test
    public void test_rejectChangeRequests_FourEdits() {
        List<EmployeeLogEldEvent> inputEvents = getOriginalData();

        // add some Change Requests
        inputEvents.add(1, createEmployeeLogEldEvent("2016-07-30T05:00:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EVENTCODE_ONDUTY, EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED, EVENTRECORDORIGIN_EDITEDBYDRIVER, null, null));
        inputEvents.add(6, createEmployeeLogEldEvent("2016-07-30T16:00:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EVENTCODE_OFFDUTY, EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED, EVENTRECORDORIGIN_EDITEDBYDRIVER, null, null));
        inputEvents.add(createEmployeeLogEldEvent("2016-07-30T16:15:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EVENTCODE_ONDUTY, EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED, EVENTRECORDORIGIN_EDITEDBYDRIVER, "8564", null));
        inputEvents.add(createEmployeeLogEldEvent("2016-07-30T17:15:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EVENTCODE_OFFDUTY, EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED, EVENTRECORDORIGIN_EDITEDBYDRIVER, "8564", null));


        List<EmployeeLogEldEvent> actualResults = (List<EmployeeLogEldEvent>) invokePrivateMethod(new EmployeeLogEldEventFacade(GlobalState.getInstance().getApplicationContext(), null), "rejectChangeRequests", inputEvents);


        Assert.assertEquals(4, actualResults.size());
        Assert.assertEquals("Sat Jul 30 05:00:00 CDT 2016", actualResults.get(0).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_INACTIVECHANGEREJECTED, (int) actualResults.get(0).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 16:00:00 CDT 2016", actualResults.get(1).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_INACTIVECHANGEREJECTED, (int) actualResults.get(1).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 16:15:00 CDT 2016", actualResults.get(2).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_INACTIVECHANGEREJECTED, (int) actualResults.get(2).getEventRecordStatus());

        Assert.assertEquals("Sat Jul 30 17:15:00 CDT 2016", actualResults.get(3).getEventDateTime().toString());
        Assert.assertEquals(EVENTRECORDSTATUS_INACTIVECHANGEREJECTED, (int) actualResults.get(3).getEventRecordStatus());
    }
    ///endregion

    ///region prepareDutyStatusLists Tests
    @Test
    public void test_prepareDutyStatusLists_empty() {
        List<EmployeeLogEldEvent> inputEvents = new ArrayList<>();

        EmployeeLogEldEventFacade.DutyStatusLists actualResults = (EmployeeLogEldEventFacade.DutyStatusLists) invokePrivateMethod(new EmployeeLogEldEventFacade(GlobalState.getInstance().getApplicationContext(), null), "prepareDutyStatusLists", inputEvents);

        Assert.assertEquals(0, actualResults.getActiveList().size());
        Assert.assertEquals(0, actualResults.getChangeRequestsList().size());
    }

    @Test
    public void test_prepareDutyStatusLists() {
        List<EmployeeLogEldEvent> inputEvents = getOriginalDataRandomOrder();

        // add some Change Requests
        inputEvents.add(6, createEmployeeLogEldEvent("2016-07-30T16:15:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EVENTCODE_OFFDUTY, EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED, EVENTRECORDORIGIN_EDITEDBYDRIVER, null, 15 * MILLISECONDS_PER_MINUTE));
        inputEvents.add(createEmployeeLogEldEvent("2016-07-30T18:15:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EVENTCODE_ONDUTY, EVENTRECORDSTATUS_INACTIVECHANGEREQUESTED, EVENTRECORDORIGIN_EDITEDBYDRIVER, "8564", 60 * MILLISECONDS_PER_MINUTE));

        EmployeeLogEldEventFacade.DutyStatusLists actualResults = (EmployeeLogEldEventFacade.DutyStatusLists) invokePrivateMethod(new EmployeeLogEldEventFacade(GlobalState.getInstance().getApplicationContext(), null), "prepareDutyStatusLists", inputEvents);

        Assert.assertEquals(7, actualResults.getActiveList().size());
        Assert.assertEquals(2, actualResults.getChangeRequestsList().size());

        // Active
        Assert.assertEquals("Sat Jul 30 00:00:00 CDT 2016", actualResults.getActiveList().get(0).getStartDateTime().toString());
        Assert.assertEquals("Sat Jul 30 05:30:00 CDT 2016", actualResults.getActiveList().get(0).getEndDateTime().toString());

        Assert.assertEquals("Sat Jul 30 05:30:00 CDT 2016", actualResults.getActiveList().get(1).getStartDateTime().toString());
        Assert.assertEquals("Sat Jul 30 06:00:00 CDT 2016", actualResults.getActiveList().get(1).getEndDateTime().toString());

        Assert.assertEquals("Sat Jul 30 06:00:00 CDT 2016", actualResults.getActiveList().get(2).getStartDateTime().toString());
        Assert.assertEquals("Sat Jul 30 11:30:00 CDT 2016", actualResults.getActiveList().get(2).getEndDateTime().toString());

        Assert.assertEquals("Sat Jul 30 11:30:00 CDT 2016", actualResults.getActiveList().get(3).getStartDateTime().toString());
        Assert.assertEquals("Sat Jul 30 12:30:00 CDT 2016", actualResults.getActiveList().get(3).getEndDateTime().toString());

        Assert.assertEquals("Sat Jul 30 12:30:00 CDT 2016", actualResults.getActiveList().get(4).getStartDateTime().toString());
        Assert.assertEquals("Sat Jul 30 16:00:00 CDT 2016", actualResults.getActiveList().get(4).getEndDateTime().toString());

        Assert.assertEquals("Sat Jul 30 16:00:00 CDT 2016", actualResults.getActiveList().get(5).getStartDateTime().toString());
        Assert.assertEquals("Sat Jul 30 16:30:00 CDT 2016", actualResults.getActiveList().get(5).getEndDateTime().toString());

        Assert.assertEquals("Sat Jul 30 16:30:00 CDT 2016", actualResults.getActiveList().get(6).getStartDateTime().toString());
        Assert.assertEquals("Sat Jul 30 23:59:59 CDT 2016", actualResults.getActiveList().get(6).getEndDateTime().toString());

        // ChangeRequest
        Assert.assertEquals("Sat Jul 30 16:15:00 CDT 2016", actualResults.getChangeRequestsList().get(0).getStartDateTime().toString());
        Assert.assertEquals("Sat Jul 30 16:30:00 CDT 2016", actualResults.getChangeRequestsList().get(0).getEndDateTime().toString());

        Assert.assertEquals("Sat Jul 30 18:15:00 CDT 2016", actualResults.getChangeRequestsList().get(1).getStartDateTime().toString());
        Assert.assertEquals("Sat Jul 30 19:15:00 CDT 2016", actualResults.getChangeRequestsList().get(1).getEndDateTime().toString());
    }
    ///endregion

    ///region Helper Methods
    private List<EmployeeLogEldEvent> getOriginalData() {
        List<EmployeeLogEldEvent> results = new ArrayList<>();

        results.add(createEmployeeLogEldEvent("2016-07-30T00:00:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EVENTCODE_OFFDUTY, EVENTRECORDSTATUS_ACTIVE, EVENTRECORDORIGIN_EDITEDBYDRIVER, null, null));
        results.add(createEmployeeLogEldEvent("2016-07-30T05:30:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EVENTCODE_ONDUTY, EVENTRECORDSTATUS_ACTIVE, EVENTRECORDORIGIN_EDITEDBYDRIVER, "8564", null));
        results.add(createEmployeeLogEldEvent("2016-07-30T06:00:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EVENTCODE_DRIVING, EVENTRECORDSTATUS_ACTIVE, EVENTRECORDORIGIN_AUTOMATICALLYRECORDEDBYELD, "8564", null));
        results.add(createEmployeeLogEldEvent("2016-07-30T11:30:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EVENTCODE_ONDUTY, EVENTRECORDSTATUS_ACTIVE, EVENTRECORDORIGIN_EDITEDBYDRIVER, "8564", null));
        results.add(createEmployeeLogEldEvent("2016-07-30T12:30:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EVENTCODE_DRIVING, EVENTRECORDSTATUS_ACTIVE, EVENTRECORDORIGIN_AUTOMATICALLYRECORDEDBYELD, "8564", null));
        results.add(createEmployeeLogEldEvent("2016-07-30T16:00:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EVENTCODE_ONDUTY, EVENTRECORDSTATUS_ACTIVE, EVENTRECORDORIGIN_EDITEDBYDRIVER, "8564", null));
        results.add(createEmployeeLogEldEvent("2016-07-30T16:30:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EVENTCODE_OFFDUTY, EVENTRECORDSTATUS_ACTIVE, EVENTRECORDORIGIN_EDITEDBYDRIVER, "8564", null));

        return results;
    }

    private List<EmployeeLogEldEvent> getOriginalDataRandomOrder() {
        List<EmployeeLogEldEvent> results = new ArrayList<>();

        results.add(createEmployeeLogEldEvent("2016-07-30T05:30:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EVENTCODE_ONDUTY, EVENTRECORDSTATUS_ACTIVE, EVENTRECORDORIGIN_EDITEDBYDRIVER, "8564", null));
        results.add(createEmployeeLogEldEvent("2016-07-30T00:00:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EVENTCODE_OFFDUTY, EVENTRECORDSTATUS_ACTIVE, EVENTRECORDORIGIN_EDITEDBYDRIVER, null, null));
        results.add(createEmployeeLogEldEvent("2016-07-30T16:30:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EVENTCODE_OFFDUTY, EVENTRECORDSTATUS_ACTIVE, EVENTRECORDORIGIN_EDITEDBYDRIVER, "8564", null));
        results.add(createEmployeeLogEldEvent("2016-07-30T11:30:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EVENTCODE_ONDUTY, EVENTRECORDSTATUS_ACTIVE, EVENTRECORDORIGIN_EDITEDBYDRIVER, "8564", null));
        results.add(createEmployeeLogEldEvent("2016-07-30T16:00:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EVENTCODE_ONDUTY, EVENTRECORDSTATUS_ACTIVE, EVENTRECORDORIGIN_EDITEDBYDRIVER, "8564", null));
        results.add(createEmployeeLogEldEvent("2016-07-30T06:00:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EVENTCODE_DRIVING, EVENTRECORDSTATUS_ACTIVE, EVENTRECORDORIGIN_AUTOMATICALLYRECORDEDBYELD, "8564", null));
        results.add(createEmployeeLogEldEvent("2016-07-30T12:30:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EVENTCODE_DRIVING, EVENTRECORDSTATUS_ACTIVE, EVENTRECORDORIGIN_AUTOMATICALLYRECORDEDBYELD, "8564", null));

        return results;
    }

    private List<EmployeeLogEldEvent> getDriverIndicationData() {
        List<EmployeeLogEldEvent> results = new ArrayList<>();

        results.add(createEmployeeLogEldEvent("2016-07-30T00:00:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EVENTCODE_OFFDUTY, EVENTRECORDSTATUS_ACTIVE, EVENTRECORDORIGIN_EDITEDBYDRIVER, null, null));
        results.add(createEmployeeLogEldEvent("2016-07-30T05:30:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EVENTCODE_ONDUTY, EVENTRECORDSTATUS_ACTIVE, EVENTRECORDORIGIN_EDITEDBYDRIVER, "8564", null));
        results.add(createEmployeeLogEldEvent("2016-07-30T06:00:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EVENTCODE_ONDUTY, EVENTRECORDSTATUS_ACTIVE, EVENTRECORDORIGIN_EDITEDBYDRIVER, "8564", null));
        results.add(createEmployeeLogEldEvent("2016-07-30T06:00:00Z", Enums.EmployeeLogEldEventType.ChangeInDriversIndication, EVENTCODE_YM, EVENTRECORDSTATUS_ACTIVE, EVENTRECORDORIGIN_EDITEDBYDRIVER, "8564", null));
        results.add(createEmployeeLogEldEvent("2016-07-30T07:00:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EVENTCODE_ONDUTY, EVENTRECORDSTATUS_ACTIVE, EVENTRECORDORIGIN_EDITEDBYDRIVER, "8564", null));
        results.add(createEmployeeLogEldEvent("2016-07-30T07:00:00Z", Enums.EmployeeLogEldEventType.ChangeInDriversIndication, EVENTCODE_CLR_PCYM, EVENTRECORDSTATUS_ACTIVE, EVENTRECORDORIGIN_EDITEDBYDRIVER, "8564", null));
        results.add(createEmployeeLogEldEvent("2016-07-30T07:30:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EVENTCODE_DRIVING, EVENTRECORDSTATUS_ACTIVE, EVENTRECORDORIGIN_AUTOMATICALLYRECORDEDBYELD, "8564", null));
        results.add(createEmployeeLogEldEvent("2016-07-30T11:25:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EVENTCODE_DRIVING, EVENTRECORDSTATUS_ACTIVE, EVENTRECORDORIGIN_EDITEDBYDRIVER, "8564", null));
        results.add(createEmployeeLogEldEvent("2016-07-30T11:30:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EVENTCODE_ONDUTY, EVENTRECORDSTATUS_ACTIVE, EVENTRECORDORIGIN_EDITEDBYDRIVER, "8564", null));
        results.add(createEmployeeLogEldEvent("2016-07-30T12:00:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EVENTCODE_OFFDUTY, EVENTRECORDSTATUS_ACTIVE, EVENTRECORDORIGIN_EDITEDBYDRIVER, "8564", null));
        results.add(createEmployeeLogEldEvent("2016-07-30T12:00:00Z", Enums.EmployeeLogEldEventType.ChangeInDriversIndication, EVENTCODE_PC, EVENTRECORDSTATUS_ACTIVE, EVENTRECORDORIGIN_EDITEDBYDRIVER, "8564", null));
        results.add(createEmployeeLogEldEvent("2016-07-30T16:30:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EVENTCODE_OFFDUTY, EVENTRECORDSTATUS_ACTIVE, EVENTRECORDORIGIN_EDITEDBYDRIVER, "8564", null));
        results.add(createEmployeeLogEldEvent("2016-07-30T16:30:00Z", Enums.EmployeeLogEldEventType.ChangeInDriversIndication, EVENTCODE_CLR_PCYM, EVENTRECORDSTATUS_ACTIVE, EVENTRECORDORIGIN_EDITEDBYDRIVER, "8564", null));

        return results;
    }

    private EmployeeLogEldEvent createEmployeeLogEldEvent(String eventDateTime, Enums.EmployeeLogEldEventType eventType, int eventCode, Integer eventRecordStatus, Integer eventRecordOrigin, String tractorNumber, Long editDuration) {

        //	EventType
        //	1 = Duty Status Change
        //	3 = Change in driver’s indication of PC or YM

        //	EventCode for Duty Status Changes
        //	1 = Off Duty
        //	2 = Sleeper
        //	3 = Driving
        //	4 = On Duty

        // EventRecordStatus
        // 1 = Active
        // 2 = Inactive – Changed
        // 3 = Inactive – Change Requested
        // 4 = Inactive – Change Rejected

        Date startDateTime = null;

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        try {
            startDateTime = format.parse(eventDateTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        EmployeeLogEldEvent eldEvent = new EmployeeLogEldEvent(startDateTime);

        eldEvent.setEventType(eventType);
        eldEvent.setEventCode(eventCode);
        eldEvent.setEventRecordStatus(eventRecordStatus);
        eldEvent.setEventRecordOrigin(eventRecordOrigin);
        eldEvent.setTractorNumber(tractorNumber);
        eldEvent.setEditDuration(editDuration);

        return eldEvent;
    }

    private void assertEvent(EmployeeLogEldEvent event, String eventDateTimeString, int eventRecordStatus, int eventType, int eventCode){
        Assert.assertEquals(eventDateTimeString, event.getEventDateTime().toString());
        Assert.assertEquals(eventRecordStatus, (int)event.getEventRecordStatus());
        Assert.assertEquals(eventType, event.getEventType().getValue());
        Assert.assertEquals(eventCode, event.getEventCode());
    }

    private Object invokePrivateMethod(Object obj, String methodName,
                                       Object... params) {
        Method method;
        Object requiredObj = null;
        try {
            method = obj.getClass().getDeclaredMethod(methodName, List.class);
            method.setAccessible(true);
            requiredObj = method.invoke(obj, params);
        } catch (NoSuchMethodException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return requiredObj;
    }
    ///endregion
}
