package com.jjkeller.kmbapi.controller;

import android.app.Application;

import com.jjkeller.kmbapi.common.ITimeKeeper;
import com.jjkeller.kmbapi.common.TimeKeeper;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.configuration.LoginCredentials;
import com.jjkeller.kmbapi.configuration.UserState;
import com.jjkeller.kmbapi.controller.dataaccess.EmployeeLogEldEventFacade;
import com.jjkeller.kmbapi.controller.dataaccess.EmployeeLogFacade;
import com.jjkeller.kmbapi.controller.dataaccess.db.EmployeeLogEldEventPersist;
import com.jjkeller.kmbapi.controller.interfaces.IAPIController;
import com.jjkeller.kmbapi.controller.interfaces.IAutoAssignedELDCalls;
import com.jjkeller.kmbapi.controller.share.MandateObjectFactory;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.employeelogeldevents.Enums;
import com.jjkeller.kmbapi.enums.DriverTypeEnum;
import com.jjkeller.kmbapi.enums.RuleSetTypeEnum;
import com.jjkeller.kmbapi.enums.TimeZoneEnum;
import com.jjkeller.kmbapi.featuretoggle.FeatureToggleService;
import com.jjkeller.kmbapi.kmbeobr.DriverCountResponse;
import com.jjkeller.kmbapi.kmbeobr.EventRecord;
import com.jjkeller.kmbapi.kmbeobr.EventTypeEnum;
import com.jjkeller.kmbapi.kmbeobr.GpsFix;
import com.jjkeller.kmbapi.kmbeobr.StatusRecord;
import com.jjkeller.kmbapi.kmbeobr.UnidentifiedPairedEvents;
import com.jjkeller.kmbapi.kmbeobr.VehicleLocation;
import com.jjkeller.kmbapi.proxydata.CompanyConfigSettings;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Created by rab5795 on 7/24/18.
 */

@RunWith(RobolectricTestRunner.class)
@Config(application = GlobalState.class,
        manifest = "/build/intermediates/bundles/encompass/debug/AndroidManifest.xml"
)
public class AutoAssignUnassignedEventsTest {
    private GlobalState app;
    private EmployeeLogEldMandateController employeeLogEldMandateController;

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        app = (GlobalState) RuntimeEnvironment.application;

        FeatureToggleService ftService = mock(FeatureToggleService.class);
        when(ftService.getIsEldMandateEnabled()).thenReturn(true);

        Field field = GlobalState.class.getDeclaredField("_featureToggleService");
        field.setAccessible(true);
        field.set(app, ftService);

        User user = mock(User.class);
        UserState userState = new UserState();
        when(user.getUserState()).thenReturn(userState);
        when(user.getHomeTerminalTimeZone()).thenReturn(TimeZoneEnum.CENTRAL_STANDARD_TIME);
        when(user.getRulesetTypeEnum()).thenReturn(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR));
        when(user.getDriverType()).thenReturn(new DriverTypeEnum(DriverTypeEnum.PROPERTYCARRYING));

        User currentDesignatedDriver = new User();
        LoginCredentials lgc = new LoginCredentials();
        lgc.setDriverIdCrc(1987);
        lgc.setEmployeeId("1234");
        currentDesignatedDriver.setCredentials(lgc);

        Field filed2 = GlobalState.class.getDeclaredField("_currentDesignatedDriver");
        filed2.setAccessible(true);
        filed2.set(app, currentDesignatedDriver);

        CompanyConfigSettings settings = new CompanyConfigSettings();
        settings.setDailyLogStartTime("00:00");
        settings.setDmoCompanyName("My Corp");
        app.setCompanyConfigSettings(app, settings);

        LoginCredentials creds = mock(LoginCredentials.class);
        when(creds.getEmployeeId()).thenReturn("00000000-0000-0000-0000-000000000000");
        when(user.getCredentials()).thenReturn(creds);

        ArrayList<User> userList = new ArrayList<User>();
        userList.add(user);
        app.setLoggedInUserList(userList);
        app.setCurrentUser(user);

        employeeLogEldMandateController = ControllerFactory.getInstance().getEmployeeLogEldMandateController();

        ITimeKeeper timeKeeper = mock(ITimeKeeper.class);
        TimeKeeper.setTimeKeeper(timeKeeper);
        when(timeKeeper.now()).thenAnswer(new Answer<Date>() {
            @Override
            public Date answer(InvocationOnMock invocation) throws Throwable {
                return new Date();
            }
        });
        when(timeKeeper.getCurrentDateTime()).thenAnswer(new Answer<DateTime>() {
            @Override
            public DateTime answer(InvocationOnMock invocation) throws Throwable {
                return new DateTime();
            }
        });

    }

    @After
    public void tearDown() throws NoSuchFieldException, IllegalAccessException {
        closeDatabase(app);
    }

    public void closeDatabase(Application app) throws NoSuchFieldException, IllegalAccessException {
        EmployeeLogEldEventPersist<EmployeeLogEldEvent> persist = new EmployeeLogEldEventPersist<EmployeeLogEldEvent>(EmployeeLogEldEvent.class, app);
        persist.open();
        List<UnidentifiedPairedEvents> unidentifiedPairedEvents = employeeLogEldMandateController.LoadUnidentifiedEldEventPairs(false);
        for (int i = 0; i < unidentifiedPairedEvents.size(); i++){
            persist.Delete(unidentifiedPairedEvents.get(i).startEvent);
            persist.Delete(unidentifiedPairedEvents.get(i).endEvent);
        }
        persist.closeDatabase();
    }

    private void addUnassignedDriving(Date start, Date end){
        try {
            VehicleLocation location = new VehicleLocation();
            GpsFix fix = new GpsFix();
            fix.setTimeCode(start.getTime());
            location.setGpsFix(fix);
            EmployeeLogEldEvent unassignedDrive = employeeLogEldMandateController.CreateDriveOnOrOffUnassignedEvent(location, Enums.CompositeEmployeeLogEldEventTypeEventCodeEnum.DrivingEvent);
            employeeLogEldMandateController.SaveDriveOnOrOffUnassignedEvent(unassignedDrive);

            VehicleLocation location2 = new VehicleLocation();
            GpsFix fix2 = new GpsFix();
            fix2.setTimeCode(end.getTime());
            location2.setGpsFix(fix2);
            EmployeeLogEldEvent unassignedDriveOff = employeeLogEldMandateController.CreateDriveOnOrOffUnassignedEvent(location2, Enums.CompositeEmployeeLogEldEventTypeEventCodeEnum.OnDutyEvent);
            employeeLogEldMandateController.SaveDriveOnOrOffUnassignedEvent(unassignedDriveOff);

        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    @Test
    public void TestScenario1UnassignedDrivingWithoutStopping() {
        Date currentNoMillis =  TimeKeeper.getInstance().getCurrentDateTime().withMillisOfSecond(0).toDate();

        Date driveStart = DateUtility.AddMinutes(currentNoMillis, -50);
        Date driveEnd = DateUtility.AddMinutes(currentNoMillis, -20);

        addUnassignedDriving(driveStart, driveEnd);

        EventRecord lastEventRecord = new EventRecord();
        lastEventRecord.setDriverId(1987);
        lastEventRecord.setTimecode(DateUtility.AddMinutes(currentNoMillis, -51).getTime());

        StatusRecord lastEventStatusRecord = new StatusRecord();

        //Vehicle is moving/driving
        lastEventStatusRecord.setSpeedometerReading(30);
        lastEventRecord.setStatusRecordData(lastEventStatusRecord);

        EventRecord lastStopRecord = new EventRecord();
        lastStopRecord.setRecordId(111);
        //Stop happened before disconnected driving period
        lastStopRecord.setTimecode(DateUtility.AddMinutes(currentNoMillis, 0).getTime());

        MockingAutoAssignedELDCalls maaec = new MockingAutoAssignedELDCalls(lastEventRecord, lastStopRecord, null);
        AutoAssignUnassignedEventsController aauec = spy(new AutoAssignUnassignedEventsController(app, maaec));
        aauec.AutoAssignUnassignedEvents();

        IAPIController empLogController = MandateObjectFactory.getInstance(app, GlobalState.getInstance().getFeatureService()).getCurrentEventController();
        List<UnidentifiedPairedEvents> unidentifiedPairedEvents = empLogController.LoadUnidentifiedEldEventPairs(false);

        assertEquals("First Scenario: Driving Event 100% Confidence Level: ", 100, unidentifiedPairedEvents.get(0).startEvent.getUnidentifiedEventConfidenceLevel().longValue());

        assertEquals("First Scenario: Duty Status Event 100% Confidence Level: ", 100, unidentifiedPairedEvents.get(0).endEvent.getUnidentifiedEventConfidenceLevel().longValue());

    }

    //First Scenario 0% Confidence Level test
    //Current driver is not the same as the driver from last identified event
    @Test
    public void TestScenario1UnassignedDrivingWithoutStoppingDriverChange() {
        Date currentNoMillis =  TimeKeeper.getInstance().getCurrentDateTime().withMillisOfSecond(0).toDate();

        Date driveStart = DateUtility.AddMinutes(currentNoMillis, -50);
        Date driveEnd = DateUtility.AddMinutes(currentNoMillis, -20);

        addUnassignedDriving(driveStart, driveEnd);

        EventRecord lastEventRecord = new EventRecord();
        lastEventRecord.setDriverId(1986);
        lastEventRecord.setTimecode(DateUtility.AddMinutes(currentNoMillis, -51).getTime());

        StatusRecord lastEventStatusRecord = new StatusRecord();

        //Vehicle is moving/driving
        lastEventStatusRecord.setSpeedometerReading(30);
        lastEventRecord.setStatusRecordData(lastEventStatusRecord);

        EventRecord lastStopRecord = new EventRecord();
        lastStopRecord.setRecordId(111);
        //Stop happened before disconnected driving period
        lastStopRecord.setTimecode(DateUtility.AddMinutes(currentNoMillis, 0).getTime());

        MockingAutoAssignedELDCalls maaec = new MockingAutoAssignedELDCalls(lastEventRecord, lastStopRecord, null);
        AutoAssignUnassignedEventsController aauec = spy(new AutoAssignUnassignedEventsController(app, maaec));
        aauec.AutoAssignUnassignedEvents();

        IAPIController empLogController = MandateObjectFactory.getInstance(app, GlobalState.getInstance().getFeatureService()).getCurrentEventController();
        List<UnidentifiedPairedEvents> unidentifiedPairedEvents = empLogController.LoadUnidentifiedEldEventPairs(false);

        assertEquals("First Scenario: 0% Confidence Level, Current driver is not the same as the driver from last identified event", 0, unidentifiedPairedEvents.get(0).startEvent.getUnidentifiedEventConfidenceLevel().longValue());

        assertEquals("First Scenario: 0% Confidence Level, Current driver is not the same as the driver from last identified event", 0, unidentifiedPairedEvents.get(0).endEvent.getUnidentifiedEventConfidenceLevel().longValue());

    }

    //After First scenario faild determine number of drivers for prior 7days from first unidentified event
    //If there has only been one driver in the vehicle, assign the confidence level to 90%
    @Test
    public void TestScenario2CheckingFormultipleDriversAfterScenario1Fails() {
        Date currentNoMillis =  TimeKeeper.getInstance().getCurrentDateTime().withMillisOfSecond(0).toDate();

        Date driveStart = DateUtility.AddMinutes(currentNoMillis, -50);
        Date driveEnd = DateUtility.AddMinutes(currentNoMillis, -20);

        addUnassignedDriving(driveStart, driveEnd);

        EventRecord lastEventRecord = new EventRecord();
        lastEventRecord.setDriverId(1987);
        lastEventRecord.setTimecode(DateUtility.AddMinutes(currentNoMillis, -55).getTime());

        StatusRecord lastEventStatusRecord = new StatusRecord();

        //Vehicle is moving/driving
        lastEventStatusRecord.setSpeedometerReading(30);
        lastEventRecord.setStatusRecordData(lastEventStatusRecord);

        EventRecord lastStopRecord = new EventRecord();
        lastStopRecord.setRecordId(111);

        lastStopRecord.setTimecode(DateUtility.AddMinutes(currentNoMillis, -54).getTime());

        //Only one driver has been driving this vehicle since the first unassigned driving event minus 7 days
        DriverCountResponse driverCountResponse = new DriverCountResponse();
        driverCountResponse.setDriverCount(1);
        driverCountResponse.setDriverIds(new int[]{1987});

        MockingAutoAssignedELDCalls maaec = new MockingAutoAssignedELDCalls(lastEventRecord, lastStopRecord, driverCountResponse);
        AutoAssignUnassignedEventsController aauec = spy(new AutoAssignUnassignedEventsController(app, maaec));
        aauec.AutoAssignUnassignedEvents();

        IAPIController empLogController = MandateObjectFactory.getInstance(app, GlobalState.getInstance().getFeatureService()).getCurrentEventController();
        List<UnidentifiedPairedEvents> unidentifiedPairedEvents = empLogController.LoadUnidentifiedEldEventPairs(false);

        assertEquals("Second Scenario: Driving Event should be at 90% Confidence Level: ", 90, unidentifiedPairedEvents.get(0).startEvent.getUnidentifiedEventConfidenceLevel().longValue());

        assertEquals("Second Scenario: Duty Status Event should be at 90% Confidence Level: ", 90, unidentifiedPairedEvents.get(0).endEvent.getUnidentifiedEventConfidenceLevel().longValue());

    }

    @Test
    public void testScenario2CheckingForMultipleDriversAfterScenario1FailsForYesterday(){
        Date yesterday = TimeKeeper.getInstance().getCurrentDateTime().withMillisOfSecond(0).withTimeAtStartOfDay().minusDays(1).toDate();
        Date today = TimeKeeper.getInstance().getCurrentDateTime().toDate();

        Date driveStartYesterday = DateUtility.AddMinutes(yesterday, -100);
        Date driveEndYesterday = DateUtility.AddMinutes(yesterday, -50);

        addUnassignedDriving(driveStartYesterday, driveEndYesterday);

        Date driveStartYesterday2 = DateUtility.AddMinutes(yesterday, -30);
        Date driveEndYesterday2 = DateUtility.AddMinutes(yesterday, -10);

        addUnassignedDriving(driveStartYesterday2, driveEndYesterday2);

        Date driveStartToday = DateUtility.AddMinutes(today, -100);
        Date driveEndToday = DateUtility.AddMinutes(today, -50);

        addUnassignedDriving(driveStartToday, driveEndToday);

        EventRecord lastEventRecord = new EventRecord();
        lastEventRecord.setDriverId(1987);
        lastEventRecord.setTimecode(DateUtility.AddMinutes(yesterday, -102).getTime());

        StatusRecord lastEventStatusRecord = new StatusRecord();

        //Vehicle is moving/driving
        lastEventStatusRecord.setSpeedometerReading(30);
        lastEventRecord.setStatusRecordData(lastEventStatusRecord);

        EventRecord lastStopRecord = new EventRecord();
        lastStopRecord.setRecordId(111);

        lastStopRecord.setTimecode(DateUtility.AddMinutes(yesterday, -101).getTime());

        //Only one driver has been driving this vehicle since the first unassigned driving event minus 7 days
        DriverCountResponse driverCountResponse = new DriverCountResponse();
        driverCountResponse.setDriverCount(1);
        driverCountResponse.setDriverIds(new int[]{1987});

        MockingAutoAssignedELDCalls maaec = new MockingAutoAssignedELDCalls(lastEventRecord, lastStopRecord, driverCountResponse);
        AutoAssignUnassignedEventsController aauec = spy(new AutoAssignUnassignedEventsController(app, maaec));
        aauec.AutoAssignUnassignedEvents();

        IAPIController empLogController = MandateObjectFactory.getInstance(app, GlobalState.getInstance().getFeatureService()).getCurrentEventController();
        List<UnidentifiedPairedEvents> unidentifiedPairedEvents = empLogController.LoadUnidentifiedEldEventPairs(false);

        assertEquals("Second Scenario: Driving Event should be at 90% Confidence Level: ", 90, unidentifiedPairedEvents.get(0).startEvent.getUnidentifiedEventConfidenceLevel().longValue());

        assertEquals("Second Scenario: Duty Status Event should be at 90% Confidence Level: ", 90, unidentifiedPairedEvents.get(0).endEvent.getUnidentifiedEventConfidenceLevel().longValue());

        assertEquals("Second Scenario: Driving Event should be at 90% Confidence Level: ", 90, unidentifiedPairedEvents.get(1).startEvent.getUnidentifiedEventConfidenceLevel().longValue());

        assertEquals("Second Scenario: Duty Status Event should be at 90% Confidence Level: ", 90, unidentifiedPairedEvents.get(1).endEvent.getUnidentifiedEventConfidenceLevel().longValue());

        assertEquals("Second Scenario: Driving Event should be at 90% Confidence Level: ", 90, unidentifiedPairedEvents.get(2).startEvent.getUnidentifiedEventConfidenceLevel().longValue());

        assertEquals("Second Scenario: Duty Status Event should be at 90% Confidence Level: ", 90, unidentifiedPairedEvents.get(2).endEvent.getUnidentifiedEventConfidenceLevel().longValue());
    }

    //Vehicle stopped 17 minutes before unidentified event
    @Test
    public void TestScenario3CheckingForIgnitionAfterScenario2Fails70percent() {

        Date currentNoMillis =  TimeKeeper.getInstance().getCurrentDateTime().withMillisOfSecond(0).toDate();

        Date driveStart = DateUtility.AddMinutes(currentNoMillis, -50);
        Date driveEnd = DateUtility.AddMinutes(currentNoMillis, -20);

        addUnassignedDriving(driveStart, driveEnd);

        EventRecord lastEventRecord = new EventRecord();
        lastEventRecord.setEventType(EventTypeEnum.DRIVER);
        lastEventRecord.setDriverId(1987);
        lastEventRecord.setRecordId(123);
        lastEventRecord.setTimecode(DateUtility.AddMinutes(currentNoMillis, -67).getTime());

        StatusRecord lastEventStatusRecord = new StatusRecord();

        //Vehicle is not continuously moving
        lastEventStatusRecord.setSpeedometerReading(0);
        lastEventRecord.setStatusRecordData(lastEventStatusRecord);

        //Two drivers have been in this vehicle since the first unassigned driving event minus 7 days
        DriverCountResponse driverCountResponse = new DriverCountResponse();
        driverCountResponse.setDriverCount(2);
        driverCountResponse.setDriverIds(new int[]{1987, 1985});

        EventRecord ignEvent = new EventRecord();
        ignEvent.setEventType(EventTypeEnum.IGNITIONON);
        ignEvent.setDriverId(1987);
        ignEvent.setRecordId(122);
        ignEvent.setTimecode(DateUtility.AddMinutes(currentNoMillis, -68).getTime());

        MockingAutoAssignedELDCalls maaec = new MockingAutoAssignedELDCalls(lastEventRecord, null, driverCountResponse, ignEvent);

        AutoAssignUnassignedEventsController aauec = spy(new AutoAssignUnassignedEventsController(app, maaec));
        aauec.AutoAssignUnassignedEvents();

        IAPIController empLogController = MandateObjectFactory.getInstance(app, GlobalState.getInstance().getFeatureService()).getCurrentEventController();
        List<UnidentifiedPairedEvents> unidentifiedPairedEvents = empLogController.LoadUnidentifiedEldEventPairs(false);

        assertEquals("Second Scenario: Driving Event should be at 70% Confidence Level: ", 70, unidentifiedPairedEvents.get(0).startEvent.getUnidentifiedEventConfidenceLevel().longValue());

        assertEquals("Second Scenario: Duty Status Event should be at 70% Confidence Level: ", 70, unidentifiedPairedEvents.get(0).endEvent.getUnidentifiedEventConfidenceLevel().longValue());

    }

    //Vehicle stopped 40 minutes before unidentified event
    @Test
    public void TestScenario3CheckingForIgnitionAfterScenario2Fails60percent() {

        Date currentNoMillis =  TimeKeeper.getInstance().getCurrentDateTime().withMillisOfSecond(0).toDate();

        Date driveStart = DateUtility.AddMinutes(currentNoMillis, -50);
        Date driveEnd = DateUtility.AddMinutes(currentNoMillis, -20);

        addUnassignedDriving(driveStart, driveEnd);

        EventRecord lastEventRecord = new EventRecord();
        lastEventRecord.setEventType(EventTypeEnum.DRIVER);
        lastEventRecord.setDriverId(1987);
        lastEventRecord.setRecordId(123);
        lastEventRecord.setTimecode(DateUtility.AddMinutes(currentNoMillis, -90).getTime());

        StatusRecord lastEventStatusRecord = new StatusRecord();

        //Vehicle is not continuously moving
        lastEventStatusRecord.setSpeedometerReading(0);
        lastEventRecord.setStatusRecordData(lastEventStatusRecord);

        EventRecord ignEvent = new EventRecord();
        ignEvent.setEventType(EventTypeEnum.IGNITIONON);
        ignEvent.setDriverId(1987);
        ignEvent.setRecordId(122);
        ignEvent.setTimecode(DateUtility.AddMinutes(currentNoMillis, -68).getTime());

        //Two drivers have been in this vehicle since the first unassigned driving event minus 7 days
        DriverCountResponse driverCountResponse = new DriverCountResponse();
        driverCountResponse.setDriverCount(2);
        driverCountResponse.setDriverIds(new int[]{1987, 1985});

        MockingAutoAssignedELDCalls maaec = new MockingAutoAssignedELDCalls(lastEventRecord, null, driverCountResponse, ignEvent);
        AutoAssignUnassignedEventsController aauec = spy(new AutoAssignUnassignedEventsController(app, maaec));
        aauec.AutoAssignUnassignedEvents();

        IAPIController empLogController = MandateObjectFactory.getInstance(app, GlobalState.getInstance().getFeatureService()).getCurrentEventController();
        List<UnidentifiedPairedEvents> unidentifiedPairedEvents =empLogController.LoadUnidentifiedEldEventPairs(false);

        assertEquals("Second Scenario: Driving Event should be at 60% Confidence Level: ", 60, unidentifiedPairedEvents.get(0).startEvent.getUnidentifiedEventConfidenceLevel().longValue());

        assertEquals("Second Scenario: Duty Status Event should be at 60% Confidence Level: ", 60, unidentifiedPairedEvents.get(0).endEvent.getUnidentifiedEventConfidenceLevel().longValue());

    }

    // Time between ignition off and start of unidentified driving < 15 mins
    @Test
    public void TestScenario4IgnitionOffExists70Confidence() {
        Date currentNoMillis =  TimeKeeper.getInstance().getCurrentDateTime().withMillisOfSecond(0).toDate();

        Date driveStart = DateUtility.AddMinutes(currentNoMillis, -20);
        Date driveEnd = DateUtility.AddMinutes(currentNoMillis, -5);

        addUnassignedDriving(driveStart, driveEnd);

        EventRecord lastEventRecord = new EventRecord();
        lastEventRecord.setDriverId(1987);
        lastEventRecord.setRecordId(123);
        lastEventRecord.setTimecode(DateUtility.AddMinutes(currentNoMillis, -30).getTime());

        EventRecord ignEvent = new EventRecord();
        ignEvent.setEventType(EventTypeEnum.IGNITIONON);
        ignEvent.setDriverId(1987);
        ignEvent.setRecordId(122);
        ignEvent.setTimecode(DateUtility.AddMinutes(currentNoMillis, -60).getTime());

        EventRecord ignitionOffRecord = new EventRecord();
        ignitionOffRecord.setRecordId(111);
        // Ignition off happened before driving
        ignitionOffRecord.setTimecode(DateUtility.AddMinutes(currentNoMillis, -25).getTime());
        ignitionOffRecord.setEventType(EventTypeEnum.IGNITIONOFF);

        // Two drivers in last 7 days
        DriverCountResponse driverCountResponse = new DriverCountResponse();
        driverCountResponse.setDriverCount(2);
        driverCountResponse.setDriverIds(new int[]{1987, 1234});

        MockingAutoAssignedELDCalls maaec = new MockingAutoAssignedELDCalls(lastEventRecord, ignitionOffRecord, driverCountResponse, ignEvent);
        AutoAssignUnassignedEventsController aauec = spy(new AutoAssignUnassignedEventsController(app, maaec));
        aauec.AutoAssignUnassignedEvents();

        IAPIController empLogController = MandateObjectFactory.getInstance(app, GlobalState.getInstance().getFeatureService()).getCurrentEventController();
        List<UnidentifiedPairedEvents> unidentifiedPairedEvents = empLogController.LoadUnidentifiedEldEventPairs(false);

        assertEquals("Fourth Scenario: Driving Event should be at 70% Confidence Level: ", 70, unidentifiedPairedEvents.get(0).startEvent.getUnidentifiedEventConfidenceLevel().longValue());
        assertEquals("Fourth Scenario: Driving Event should be have suggested employee of 1234", "1234", unidentifiedPairedEvents.get(0).startEvent.getUnidentifiedEventSuggestedDriver());
        assertEquals("Fourth Scenario: Duty Status Event should be at 70% Confidence Level: ", 70, unidentifiedPairedEvents.get(0).endEvent.getUnidentifiedEventConfidenceLevel().longValue());
        assertEquals("Fourth Scenario: Driving Event should be have suggested employee of 1234", "1234", unidentifiedPairedEvents.get(0).endEvent.getUnidentifiedEventSuggestedDriver());
    }

    // Time between ignition off and start of unidentified driving >= 15 min and < 30 min
    @Test
    public void TestScenario4IgnitionOffExists60Confidence() {
        Date currentNoMillis =  TimeKeeper.getInstance().getCurrentDateTime().withMillisOfSecond(0).toDate();

        Date driveStart = DateUtility.AddMinutes(currentNoMillis, -20);
        Date driveEnd = DateUtility.AddMinutes(currentNoMillis, -5);

        addUnassignedDriving(driveStart, driveEnd);

        EventRecord lastEventRecord = new EventRecord();
        lastEventRecord.setDriverId(1987);
        lastEventRecord.setRecordId(123);
        lastEventRecord.setTimecode(DateUtility.AddMinutes(currentNoMillis, -40).getTime());

        EventRecord ignEvent = new EventRecord();
        ignEvent.setEventType(EventTypeEnum.IGNITIONON);
        ignEvent.setDriverId(1987);
        ignEvent.setRecordId(122);
        ignEvent.setTimecode(DateUtility.AddMinutes(currentNoMillis, -60).getTime());

        EventRecord ignitionOffRecord = new EventRecord();
        ignitionOffRecord.setRecordId(111);
        // Ignition off happened before driving
        ignitionOffRecord.setTimecode(DateUtility.AddMinutes(currentNoMillis, -25).getTime());
        ignitionOffRecord.setEventType(EventTypeEnum.IGNITIONOFF);

        // Two drivers in last 7 days
        DriverCountResponse driverCountResponse = new DriverCountResponse();
        driverCountResponse.setDriverCount(2);
        driverCountResponse.setDriverIds(new int[]{1987, 1234});

        MockingAutoAssignedELDCalls maaec = new MockingAutoAssignedELDCalls(lastEventRecord, ignitionOffRecord, driverCountResponse, ignEvent);
        AutoAssignUnassignedEventsController aauec = spy(new AutoAssignUnassignedEventsController(app, maaec));
        aauec.AutoAssignUnassignedEvents();

        IAPIController empLogController = MandateObjectFactory.getInstance(app, GlobalState.getInstance().getFeatureService()).getCurrentEventController();
        List<UnidentifiedPairedEvents> unidentifiedPairedEvents = empLogController.LoadUnidentifiedEldEventPairs(false);

        assertEquals("Fourth Scenario: Driving Event should be at 60% Confidence Level: ", 60, unidentifiedPairedEvents.get(0).startEvent.getUnidentifiedEventConfidenceLevel().longValue());
        assertEquals("Fourth Scenario: Driving Event should be have suggested employee of 1234", "1234", unidentifiedPairedEvents.get(0).startEvent.getUnidentifiedEventSuggestedDriver());
        assertEquals("Fourth Scenario: Duty Status Event should be at 60% Confidence Level: ", 60, unidentifiedPairedEvents.get(0).endEvent.getUnidentifiedEventConfidenceLevel().longValue());
        assertEquals("Fourth Scenario: Duty Status Event should be have suggested employee of 1234", "1234", unidentifiedPairedEvents.get(0).endEvent.getUnidentifiedEventSuggestedDriver());
    }

    // Time between ignition off and start of unidentified driving >= 30 min and < 1 hr
    @Test
    public void TestScenario4IgnitionOffExists50Confidence() {
        Date currentNoMillis =  TimeKeeper.getInstance().getCurrentDateTime().withMillisOfSecond(0).toDate();

        Date driveStart = DateUtility.AddMinutes(currentNoMillis, -20);
        Date driveEnd = DateUtility.AddMinutes(currentNoMillis, -5);

        addUnassignedDriving(driveStart, driveEnd);

        EventRecord lastEventRecord = new EventRecord();
        lastEventRecord.setDriverId(1987);
        lastEventRecord.setRecordId(123);
        lastEventRecord.setTimecode(DateUtility.AddMinutes(currentNoMillis, -60).getTime());

        EventRecord ignEvent = new EventRecord();
        ignEvent.setEventType(EventTypeEnum.IGNITIONON);
        ignEvent.setDriverId(1987);
        ignEvent.setRecordId(122);
        ignEvent.setTimecode(DateUtility.AddMinutes(currentNoMillis, -75).getTime());

        EventRecord ignitionOffRecord = new EventRecord();
        ignitionOffRecord.setRecordId(111);
        // Ignition off happened before driving
        ignitionOffRecord.setTimecode(DateUtility.AddMinutes(currentNoMillis, -25).getTime());
        ignitionOffRecord.setEventType(EventTypeEnum.IGNITIONOFF);

        // Two drivers in last 7 days
        DriverCountResponse driverCountResponse = new DriverCountResponse();
        driverCountResponse.setDriverCount(2);
        driverCountResponse.setDriverIds(new int[]{1987, 1234});

        MockingAutoAssignedELDCalls maaec = new MockingAutoAssignedELDCalls(lastEventRecord, ignitionOffRecord, driverCountResponse, ignEvent);
        AutoAssignUnassignedEventsController aauec = spy(new AutoAssignUnassignedEventsController(app, maaec));
        aauec.AutoAssignUnassignedEvents();

        IAPIController empLogController = MandateObjectFactory.getInstance(app, GlobalState.getInstance().getFeatureService()).getCurrentEventController();
        List<UnidentifiedPairedEvents> unidentifiedPairedEvents = empLogController.LoadUnidentifiedEldEventPairs(false);

        assertEquals("Fourth Scenario: Driving Event should be at 50% Confidence Level: ", 50, unidentifiedPairedEvents.get(0).startEvent.getUnidentifiedEventConfidenceLevel().longValue());
        assertEquals("Fourth Scenario: Driving Event should be have suggested employee of 1234", "1234", unidentifiedPairedEvents.get(0).startEvent.getUnidentifiedEventSuggestedDriver());
        assertEquals("Fourth Scenario: Duty Status Event should be at 50% Confidence Level: ", 50, unidentifiedPairedEvents.get(0).endEvent.getUnidentifiedEventConfidenceLevel().longValue());
        assertEquals("Fourth Scenario: Driving Event should be have suggested employee of 1234", "1234", unidentifiedPairedEvents.get(0).endEvent.getUnidentifiedEventSuggestedDriver());
    }

    // Time between ignition off and start of unidentified driving >= 1 hr and < 2 hrs
    @Test
    public void TestScenario4IgnitionOffExists30Confidence() {
        Date currentNoMillis =  TimeKeeper.getInstance().getCurrentDateTime().withMillisOfSecond(0).toDate();

        Date driveStart = DateUtility.AddMinutes(currentNoMillis, -20);
        Date driveEnd = DateUtility.AddMinutes(currentNoMillis, -5);

        addUnassignedDriving(driveStart, driveEnd);

        EventRecord lastEventRecord = new EventRecord();
        lastEventRecord.setDriverId(1987);
        lastEventRecord.setRecordId(123);
        lastEventRecord.setTimecode(DateUtility.AddMinutes(currentNoMillis, -100).getTime());

        EventRecord ignEvent = new EventRecord();
        ignEvent.setEventType(EventTypeEnum.IGNITIONON);
        ignEvent.setDriverId(1987);
        ignEvent.setRecordId(122);
        ignEvent.setTimecode(DateUtility.AddMinutes(currentNoMillis, -110).getTime());

        EventRecord ignitionOffRecord = new EventRecord();
        ignitionOffRecord.setRecordId(111);
        // Ignition off happened before driving
        ignitionOffRecord.setTimecode(DateUtility.AddMinutes(currentNoMillis, -25).getTime());
        ignitionOffRecord.setEventType(EventTypeEnum.IGNITIONOFF);

        // Two drivers in last 7 days
        DriverCountResponse driverCountResponse = new DriverCountResponse();
        driverCountResponse.setDriverCount(2);
        driverCountResponse.setDriverIds(new int[]{1987, 1234});

        MockingAutoAssignedELDCalls maaec = new MockingAutoAssignedELDCalls(lastEventRecord, ignitionOffRecord, driverCountResponse, ignEvent);
        AutoAssignUnassignedEventsController aauec = spy(new AutoAssignUnassignedEventsController(app, maaec));
        aauec.AutoAssignUnassignedEvents();

        IAPIController empLogController = MandateObjectFactory.getInstance(app, GlobalState.getInstance().getFeatureService()).getCurrentEventController();
        List<UnidentifiedPairedEvents> unidentifiedPairedEvents = empLogController.LoadUnidentifiedEldEventPairs(false);

        assertEquals("Fourth Scenario: Driving Event should be at 30% Confidence Level: ", 30, unidentifiedPairedEvents.get(0).startEvent.getUnidentifiedEventConfidenceLevel().longValue());
        assertEquals("Fourth Scenario: Driving Event should be have suggested employee of 1234", "1234", unidentifiedPairedEvents.get(0).startEvent.getUnidentifiedEventSuggestedDriver());
        assertEquals("Fourth Scenario: Duty Status Event should be at 30% Confidence Level: ", 30, unidentifiedPairedEvents.get(0).endEvent.getUnidentifiedEventConfidenceLevel().longValue());
        assertEquals("Fourth Scenario: Driving Event should be have suggested employee of 1234", "1234", unidentifiedPairedEvents.get(0).endEvent.getUnidentifiedEventSuggestedDriver());
    }

    // Time between ignition off and start of unidentified driving >= 2 hrs and < 4 hrs
    @Test
    public void TestScenario4IgnitionOffExists10Confidence() {
        Date currentNoMillis =  TimeKeeper.getInstance().getCurrentDateTime().withMillisOfSecond(0).toDate();

        Date driveStart = DateUtility.AddMinutes(currentNoMillis, -20);
        Date driveEnd = DateUtility.AddMinutes(currentNoMillis, -5);

        addUnassignedDriving(driveStart, driveEnd);

        EventRecord lastEventRecord = new EventRecord();
        lastEventRecord.setDriverId(1987);
        lastEventRecord.setRecordId(123);
        lastEventRecord.setTimecode(DateUtility.AddMinutes(currentNoMillis, -160).getTime());

        EventRecord ignEvent = new EventRecord();
        ignEvent.setEventType(EventTypeEnum.IGNITIONON);
        ignEvent.setDriverId(1987);
        ignEvent.setRecordId(122);
        ignEvent.setTimecode(DateUtility.AddMinutes(currentNoMillis, -170).getTime());

        EventRecord ignitionOffRecord = new EventRecord();
        ignitionOffRecord.setRecordId(111);
        // Ignition off happened before driving
        ignitionOffRecord.setTimecode(DateUtility.AddMinutes(currentNoMillis, -25).getTime());
        ignitionOffRecord.setEventType(EventTypeEnum.IGNITIONOFF);

        // Two drivers in last 7 days
        DriverCountResponse driverCountResponse = new DriverCountResponse();
        driverCountResponse.setDriverCount(2);
        driverCountResponse.setDriverIds(new int[]{1987, 1234});

        MockingAutoAssignedELDCalls maaec = new MockingAutoAssignedELDCalls(lastEventRecord, ignitionOffRecord, driverCountResponse, ignEvent);
        AutoAssignUnassignedEventsController aauec = spy(new AutoAssignUnassignedEventsController(app, maaec));
        aauec.AutoAssignUnassignedEvents();

        IAPIController empLogController = MandateObjectFactory.getInstance(app, GlobalState.getInstance().getFeatureService()).getCurrentEventController();
        List<UnidentifiedPairedEvents> unidentifiedPairedEvents = empLogController.LoadUnidentifiedEldEventPairs(false);

        assertEquals("Fourth Scenario: Driving Event should be at 10% Confidence Level: ", 10, unidentifiedPairedEvents.get(0).startEvent.getUnidentifiedEventConfidenceLevel().longValue());
        assertEquals("Fourth Scenario: Driving Event should be have suggested employee of 1234", "1234", unidentifiedPairedEvents.get(0).startEvent.getUnidentifiedEventSuggestedDriver());
        assertEquals("Fourth Scenario: Duty Status Event should be at 10% Confidence Level: ", 10, unidentifiedPairedEvents.get(0).endEvent.getUnidentifiedEventConfidenceLevel().longValue());
        assertEquals("Fourth Scenario: Driving Event should be have suggested employee of 1234", "1234", unidentifiedPairedEvents.get(0).endEvent.getUnidentifiedEventSuggestedDriver());
    }

    // Time between ignition off and start of unidentified driving >= 4 hrs and < 8 hrs
    @Test
    public void TestScenario4IgnitionOffExists5Confidence() {
        Date currentNoMillis =  TimeKeeper.getInstance().getCurrentDateTime().withMillisOfSecond(0).toDate();

        Date driveStart = DateUtility.AddMinutes(currentNoMillis, -20);
        Date driveEnd = DateUtility.AddMinutes(currentNoMillis, -5);

        addUnassignedDriving(driveStart, driveEnd);

        EventRecord lastEventRecord = new EventRecord();
        lastEventRecord.setDriverId(1987);
        lastEventRecord.setRecordId(123);
        lastEventRecord.setTimecode(DateUtility.AddMinutes(currentNoMillis, -280).getTime());

        EventRecord ignEvent = new EventRecord();
        ignEvent.setEventType(EventTypeEnum.IGNITIONON);
        ignEvent.setDriverId(1987);
        ignEvent.setRecordId(122);
        ignEvent.setTimecode(DateUtility.AddMinutes(currentNoMillis, -300).getTime());

        EventRecord ignitionOffRecord = new EventRecord();
        ignitionOffRecord.setRecordId(111);
        // Ignition off happened before driving
        ignitionOffRecord.setTimecode(DateUtility.AddMinutes(currentNoMillis, -25).getTime());
        ignitionOffRecord.setEventType(EventTypeEnum.IGNITIONOFF);

        // Two drivers in last 7 days
        DriverCountResponse driverCountResponse = new DriverCountResponse();
        driverCountResponse.setDriverCount(2);
        driverCountResponse.setDriverIds(new int[]{1987, 1234});

        MockingAutoAssignedELDCalls maaec = new MockingAutoAssignedELDCalls(lastEventRecord, ignitionOffRecord, driverCountResponse, ignEvent);
        AutoAssignUnassignedEventsController aauec = spy(new AutoAssignUnassignedEventsController(app, maaec));
        aauec.AutoAssignUnassignedEvents();

        IAPIController empLogController = MandateObjectFactory.getInstance(app, GlobalState.getInstance().getFeatureService()).getCurrentEventController();
        List<UnidentifiedPairedEvents> unidentifiedPairedEvents = empLogController.LoadUnidentifiedEldEventPairs(false);

        assertEquals("Fourth Scenario: Driving Event should be at 5% Confidence Level: ", 5, unidentifiedPairedEvents.get(0).startEvent.getUnidentifiedEventConfidenceLevel().longValue());
        assertEquals("Fourth Scenario: Driving Event should be have suggested employee of 1234", "1234", unidentifiedPairedEvents.get(0).startEvent.getUnidentifiedEventSuggestedDriver());
        assertEquals("Fourth Scenario: Duty Status Event should be at 5% Confidence Level: ", 5, unidentifiedPairedEvents.get(0).endEvent.getUnidentifiedEventConfidenceLevel().longValue());
        assertEquals("Fourth Scenario: Driving Event should be have suggested employee of 1234", "1234", unidentifiedPairedEvents.get(0).endEvent.getUnidentifiedEventSuggestedDriver());
    }

    // Time between ignition off and start of unidentified driving >=8 hrs
    @Test
    public void TestScenario4IgnitionOffExists0Confidence() {
        Date currentNoMillis =  TimeKeeper.getInstance().getCurrentDateTime().withMillisOfSecond(0).toDate();

        Date driveStart = DateUtility.AddMinutes(currentNoMillis, -20);
        Date driveEnd = DateUtility.AddMinutes(currentNoMillis, -5);

        addUnassignedDriving(driveStart, driveEnd);

        EventRecord lastEventRecord = new EventRecord();
        lastEventRecord.setDriverId(1987);
        lastEventRecord.setRecordId(123);
        lastEventRecord.setTimecode(DateUtility.AddMinutes(currentNoMillis, -560).getTime());

        EventRecord ignEvent = new EventRecord();
        ignEvent.setEventType(EventTypeEnum.IGNITIONON);
        ignEvent.setDriverId(1987);
        ignEvent.setRecordId(122);
        ignEvent.setTimecode(DateUtility.AddMinutes(currentNoMillis, -580).getTime());

        EventRecord ignitionOffRecord = new EventRecord();
        ignitionOffRecord.setRecordId(111);
        // Ignition off happened before driving
        ignitionOffRecord.setTimecode(DateUtility.AddMinutes(currentNoMillis, -25).getTime());
        ignitionOffRecord.setEventType(EventTypeEnum.IGNITIONOFF);

        // Two drivers in last 7 days
        DriverCountResponse driverCountResponse = new DriverCountResponse();
        driverCountResponse.setDriverCount(2);
        driverCountResponse.setDriverIds(new int[]{1987, 1234});

        MockingAutoAssignedELDCalls maaec = new MockingAutoAssignedELDCalls(lastEventRecord, ignitionOffRecord, driverCountResponse, ignEvent);
        AutoAssignUnassignedEventsController aauec = spy(new AutoAssignUnassignedEventsController(app, maaec));
        aauec.AutoAssignUnassignedEvents();

        IAPIController empLogController = MandateObjectFactory.getInstance(app, GlobalState.getInstance().getFeatureService()).getCurrentEventController();
        List<UnidentifiedPairedEvents> unidentifiedPairedEvents = empLogController.LoadUnidentifiedEldEventPairs(false);

        assertEquals("Fourth Scenario: Driving Event should be at 0% Confidence Level: ", 0, unidentifiedPairedEvents.get(0).startEvent.getUnidentifiedEventConfidenceLevel().longValue());
        assertNull("Fourth Scenario: Driving Event suggested driver should be null", unidentifiedPairedEvents.get(0).startEvent.getUnidentifiedEventSuggestedDriver());
        assertEquals("Fourth Scenario: Duty Status Event should be at 0% Confidence Level: ", 0, unidentifiedPairedEvents.get(0).endEvent.getUnidentifiedEventConfidenceLevel().longValue());
        assertNull("Fourth Scenario: Driving Event suggested driver should be null", unidentifiedPairedEvents.get(0).endEvent.getUnidentifiedEventSuggestedDriver());
    }

    public class MockingAutoAssignedELDCalls implements IAutoAssignedELDCalls {

        EventRecord lastEventWithDriverId;
        EventRecord nextEvent;
        EventRecord lastIgnEvent;
        DriverCountResponse driverCountResponse;

        public MockingAutoAssignedELDCalls(EventRecord lastEventWithDriverId, EventRecord nextEvent, DriverCountResponse driverCountResponse){
            this.lastEventWithDriverId = lastEventWithDriverId;
            this.nextEvent = nextEvent;
            this.driverCountResponse = driverCountResponse;
        }

        public MockingAutoAssignedELDCalls(EventRecord lastEventWithDriverId, EventRecord nextEvent, DriverCountResponse driverCountResponse, EventRecord lastIgnEvent){
            this.lastEventWithDriverId = lastEventWithDriverId;
            this.nextEvent = nextEvent;
            this.driverCountResponse = driverCountResponse;
            this.lastIgnEvent = lastIgnEvent;
        }
        //get statusRecord of the last Driver Event
        public StatusRecord GetStatusRecordForEobrId(int eobrId){
           return lastEventWithDriverId.getStatusRecordData();
        }

        public EventRecord GetLastDriverEvent(long startRefTimestamp, long endRefTimestamp, EventTypeEnum... eventTypes){
            return lastEventWithDriverId;
        }

        @Override
        public EventRecord GetLastIgnitionEvent(long startRefTimestamp, long endRefTimestamp) {
            return lastIgnEvent;
        }

        public EventRecord GetNextEvent(long startTimeCode, EventTypeEnum... eventTypes){
            return nextEvent;
        }

        public DriverCountResponse GetDriverCount(long startRefTimestamp, long endRefTimestamp) {
            return driverCountResponse;
        }


    }


}
