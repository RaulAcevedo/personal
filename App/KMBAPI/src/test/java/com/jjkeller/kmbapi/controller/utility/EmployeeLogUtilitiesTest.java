package com.jjkeller.kmbapi.controller.utility;

import com.jjkeller.kmbapi.common.TestBase;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.configuration.LoginCredentials;
import com.jjkeller.kmbapi.configuration.UserState;
import com.jjkeller.kmbapi.controller.ControllerFactory;
import com.jjkeller.kmbapi.controller.EmployeeLogAobrdController;
import com.jjkeller.kmbapi.controller.dataaccess.EmployeeLogFacade;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.employeelogeldevents.EmployeeLogEldEventCode;
import com.jjkeller.kmbapi.employeelogeldevents.Enums;
import com.jjkeller.kmbapi.enums.DriverTypeEnum;
import com.jjkeller.kmbapi.enums.DutyStatusEnum;
import com.jjkeller.kmbapi.enums.RuleSetTypeEnum;
import com.jjkeller.kmbapi.enums.TimeZoneEnum;
import com.jjkeller.kmbapi.featuretoggle.FeatureToggleService;
import com.jjkeller.kmbapi.proxydata.CompanyConfigSettings;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEventList;
import com.jjkeller.kmbapi.proxydata.Location;

import junit.framework.Assert;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for EmployeeLogUtilities.java
 *
 * Created by jld5296 on 10/26/16.
 */
@RunWith(RobolectricTestRunner.class)
@Config(application = GlobalState.class,
        manifest = "/build/intermediates/bundles/encompass/debug/AndroidManifest.xml"
)
public class EmployeeLogUtilitiesTest extends TestBase {

    private GlobalState globalState;
    private EmployeeLogAobrdController employeeLogController;
    private EmployeeLog employeeLog;
    private RuleSetTypeEnum ruleType = new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR);

    private EmployeeLogFacade employeeLogFacade;

    private DutyStatusEnum OFF_DUTY = new DutyStatusEnum(DutyStatusEnum.OFFDUTY);
    private DutyStatusEnum ON_DUTY = new DutyStatusEnum(DutyStatusEnum.ONDUTY);
    private DutyStatusEnum DRIVING = new DutyStatusEnum(DutyStatusEnum.DRIVING);
    private DutyStatusEnum SLEEPER = new DutyStatusEnum(DutyStatusEnum.SLEEPER);


    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm", Locale.US);


    private void addDutyStatyChange(Date time, DutyStatusEnum status, boolean manual){
        try {
            employeeLogController.CreateDutyStatusChangedEvent(employeeLog,time, status, new Location(), true, ruleType, null, null , manual, null, null);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }

    }

    private static EmployeeLogEldEvent createEldEvent(Enums.EmployeeLogEldEventType eventType, Enums.EmployeeLogEldEventRecordStatus recordStatus, DateTime eventDateTime, int dutyStatusEnum){


        EmployeeLogEldEvent eldEvent = new EmployeeLogEldEvent(eventDateTime.toDate(), new EmployeeLogEldEventCode(dutyStatusEnum), Enums.EmployeeLogEldEventType.DutyStatusChange);
        eldEvent.setEventType(eventType);
        eldEvent.setEventRecordStatus(recordStatus.getValue());
        eldEvent.setDutyStatusEnum(new DutyStatusEnum(dutyStatusEnum));

        return eldEvent;
    }


    @Before
    public void setUp() throws Exception {
        globalState = (GlobalState) RuntimeEnvironment.application;

        FeatureToggleService ftService = mock(FeatureToggleService.class);
        when(ftService.getIsEldMandateEnabled()).thenReturn(false);


        Field field = GlobalState.class.getDeclaredField("_featureToggleService");
        field.setAccessible(true);
        field.set(globalState, ftService);

        LoginCredentials credentials = mock(LoginCredentials.class);
        when(credentials.getEmployeeId()).thenReturn("12345678-1234-1234-1234-123456789012");


        User user = mock(User.class);
        UserState userState = new UserState();
        when(user.getCredentials()).thenReturn(credentials);
        when(user.getCredentials()).thenReturn(credentials);
        when(user.getUserState()).thenReturn(userState);
        when(user.getHomeTerminalTimeZone()).thenReturn(TimeZoneEnum.CENTRAL_STANDARD_TIME);
        when(user.getRulesetTypeEnum()).thenReturn(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR));
        when(user.getDriverType()).thenReturn(new DriverTypeEnum(DriverTypeEnum.PROPERTYCARRYING));

        CompanyConfigSettings settings = new CompanyConfigSettings();
        settings.setDailyLogStartTime("00:00");
        settings.setDmoCompanyName("Roman Corp");
        globalState.setCompanyConfigSettings(globalState, settings);


        ArrayList<User> userList = new ArrayList<User>();
        userList.add(user);
        globalState.setLoggedInUserList(userList);
        globalState.setCurrentUser(user);

        Date date = dateFormat.parse("2017-07-01 00:00");
        employeeLog = EmployeeLogUtilities.CreateNewLog(globalState, user, date, DutyStatusEnum.valueOfDMOEnum(DutyStatusEnum.DmoEnum_OffDuty), null);
        globalState.setCurrentEmployeeLog(employeeLog);

        employeeLogController = ControllerFactory.getInstance().getEmployeeLogAobrdController();
        employeeLogFacade = new EmployeeLogFacade(globalState, user);


    }

    @After
    public void tearDown() throws NoSuchFieldException, IllegalAccessException {
        closeDatabase(globalState);
        employeeLog = null;
    }

    @Test
    public void calculateLogEndTime() throws Exception {
        String dailyLogStartime = "02:29:45";

        TimeZoneEnum homeTerm = TimeZoneEnum.PACIFIC_STANDARD_TIME;

        Calendar cal = Calendar.getInstance(homeTerm.toTimeZone());
        cal.set(2016, Calendar.OCTOBER, 20, 16, 25, 55);
        Date input = cal.getTime();

        cal.set(2016, Calendar.OCTOBER, 21, 2, 29, 44);
        Date expected = cal.getTime();

        Date actual = EmployeeLogUtilities.CalculateLogEndTime(dailyLogStartime, input, homeTerm);

        AssertDateAndTimeIsEqual(expected, actual);
    }

    @Test
    public void calculateLogStartTime() throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2016, Calendar.OCTOBER, 26, 16, 45, 5);
        calendar.setTimeZone(TimeZone.getTimeZone("PST"));
        Date logDate = calendar.getTime();

        Date actual = EmployeeLogUtilities.CalculateLogStartTime("02:30:15", logDate, TimeZoneEnum.PACIFIC_STANDARD_TIME);

        calendar.set(2016, Calendar.OCTOBER, 26, 2, 30, 15);
        Date expected = calendar.getTime();

        AssertDateAndTimeIsEqual(expected, actual);
    }

    // Need to actually test the components of the Date and Time, because Assert.Equals(date, date) does a reference comparison, not actual values
    private void AssertDateAndTimeIsEqual(Date expected, Date actual) {
        Calendar calExpected = Calendar.getInstance();
        calExpected.setTime(expected);

        Calendar calActual = Calendar.getInstance();
        calActual.setTime(actual);
        Assert.assertEquals(calExpected.get(Calendar.YEAR), calActual.get(Calendar.YEAR));
        Assert.assertEquals(calExpected.get(Calendar.DAY_OF_YEAR), calActual.get(Calendar.DAY_OF_YEAR));
        Assert.assertEquals(calExpected.get(Calendar.HOUR_OF_DAY), calActual.get(Calendar.HOUR_OF_DAY));
        Assert.assertEquals(calExpected.get(Calendar.MINUTE), calActual.get(Calendar.MINUTE));
        Assert.assertEquals(calExpected.get(Calendar.SECOND), calActual.get(Calendar.SECOND));
    }



    @Test
    public void CalculateLogEventTotal_FilledLog_CorrectCalculationsAreDone(){
        User user = new User();
        LoginCredentials credentials = new LoginCredentials();
        credentials.setEmployeeId("Unit Testing Employee");

        user.setCredentials(credentials);
        GlobalState.getInstance().setCurrentUser(user);

        EmployeeLog employeeLog = new EmployeeLog();
        DateTime currentClockTime = new DateTime(2017, 4, 10, 14, 0);

        EmployeeLogEldEvent[] eldEvents = new EmployeeLogEldEvent[6];
        eldEvents[0] = createEldEvent(Enums.EmployeeLogEldEventType.DutyStatusChange, Enums.EmployeeLogEldEventRecordStatus.Active, new DateTime(2017, 4, 10, 10, 30), DutyStatusEnum.ONDUTY);
        eldEvents[1] = createEldEvent(Enums.EmployeeLogEldEventType.DutyStatusChange, Enums.EmployeeLogEldEventRecordStatus.Active, new DateTime(2017, 4, 10, 10, 40), DutyStatusEnum.DRIVING);
        eldEvents[2] = createEldEvent(Enums.EmployeeLogEldEventType.DutyStatusChange, Enums.EmployeeLogEldEventRecordStatus.Active, new DateTime(2017, 4, 10, 10, 50), DutyStatusEnum.OFFDUTY);
        eldEvents[3] = createEldEvent(Enums.EmployeeLogEldEventType.DutyStatusChange, Enums.EmployeeLogEldEventRecordStatus.Active, new DateTime(2017, 4, 10, 11,  0), DutyStatusEnum.ONDUTY);
        eldEvents[4] = createEldEvent(Enums.EmployeeLogEldEventType.DutyStatusChange, Enums.EmployeeLogEldEventRecordStatus.Active, new DateTime(2017, 4, 10, 11, 30), DutyStatusEnum.DRIVING);
        eldEvents[5] = createEldEvent(Enums.EmployeeLogEldEventType.DutyStatusChange, Enums.EmployeeLogEldEventRecordStatus.Active, new DateTime(2017, 4, 10, 13, 30), DutyStatusEnum.OFFDUTY);


        EmployeeLogEldEventList eldEventList = new EmployeeLogEldEventList();
        eldEventList.setEldEventList(eldEvents);

        employeeLog.setEldEventList(eldEventList);


        Assert.assertEquals(130, EmployeeLogUtilities.CalculateLogEventTotal(employeeLog, new DutyStatusEnum(DutyStatusEnum.DRIVING), currentClockTime.toDate(), TimeUnit.MINUTES));
        Assert.assertEquals(40, EmployeeLogUtilities.CalculateLogEventTotal(employeeLog, new DutyStatusEnum(DutyStatusEnum.ONDUTY), currentClockTime.toDate(), TimeUnit.MINUTES));
        Assert.assertEquals(40, EmployeeLogUtilities.CalculateLogEventTotal(employeeLog, new DutyStatusEnum(DutyStatusEnum.OFFDUTY), currentClockTime.toDate(), TimeUnit.MINUTES));

        Assert.assertEquals(7800000, EmployeeLogUtilities.CalculateLogEventTotal(employeeLog, new DutyStatusEnum(DutyStatusEnum.DRIVING), currentClockTime.toDate(), TimeUnit.MILLISECONDS));
        Assert.assertEquals(2400000, EmployeeLogUtilities.CalculateLogEventTotal(employeeLog, new DutyStatusEnum(DutyStatusEnum.ONDUTY), currentClockTime.toDate(), TimeUnit.MILLISECONDS));
        Assert.assertEquals(2400000, EmployeeLogUtilities.CalculateLogEventTotal(employeeLog, new DutyStatusEnum(DutyStatusEnum.OFFDUTY), currentClockTime.toDate(), TimeUnit.MILLISECONDS));
    }




    @Test
    public void getNextActiveEventAfterDateSortedTest(){
        User user = new User();
        LoginCredentials credentials = new LoginCredentials();
        credentials.setEmployeeId("Unit Testing Employee");

        user.setCredentials(credentials);
        GlobalState.getInstance().setCurrentUser(user);

        List<EmployeeLogEldEvent> eldEvents = new ArrayList<>();

        eldEvents.add(createEldEvent(Enums.EmployeeLogEldEventType.DutyStatusChange, Enums.EmployeeLogEldEventRecordStatus.Active, new DateTime(2017, 5, 11, 10, 30), DutyStatusEnum.ONDUTY));
        eldEvents.add(createEldEvent(Enums.EmployeeLogEldEventType.DutyStatusChange, Enums.EmployeeLogEldEventRecordStatus.Active, new DateTime(2017, 4, 11, 10, 40), DutyStatusEnum.DRIVING));
        eldEvents.add(createEldEvent(Enums.EmployeeLogEldEventType.DutyStatusChange, Enums.EmployeeLogEldEventRecordStatus.Active, new DateTime(2017, 4, 11, 10, 51), DutyStatusEnum.OFFDUTY));
        eldEvents.add(createEldEvent(Enums.EmployeeLogEldEventType.DutyStatusChange, Enums.EmployeeLogEldEventRecordStatus.Active, new DateTime(2017, 2, 10, 11,  0), DutyStatusEnum.ONDUTY));
        eldEvents.add(createEldEvent(Enums.EmployeeLogEldEventType.DutyStatusChange, Enums.EmployeeLogEldEventRecordStatus.Active, new DateTime(2017, 4, 13, 11, 30), DutyStatusEnum.DRIVING));
        eldEvents.add(createEldEvent(Enums.EmployeeLogEldEventType.DutyStatusChange, Enums.EmployeeLogEldEventRecordStatus.Active, new DateTime(2017, 3, 18, 13, 30), DutyStatusEnum.OFFDUTY));

        EmployeeLogEldEvent next = EmployeeLogUtilities.getNextActiveEventAfterDate(eldEvents, new DateTime(2017, 5, 11, 10, 30).toDate());
        assertNull("last event", next);

        // date way in the future
        next = EmployeeLogUtilities.getNextActiveEventAfterDate(eldEvents, new DateTime(2019, 12, 11, 2, 30).toDate());
        assertNull("last event", next);

        next = EmployeeLogUtilities.getNextActiveEventAfterDate(eldEvents, new DateTime(2017, 4, 11, 10, 40).toDate());
        assertEquals("wrong event returned", next.getEventDateTime().getTime(), new DateTime(2017, 4, 11, 10, 51).getMillis());


        next = EmployeeLogUtilities.getNextActiveEventAfterDate(eldEvents, new DateTime(2017, 3, 18, 13, 30).toDate());
        assertEquals("wrong event returned", next.getEventDateTime().getTime(), new DateTime(2017, 4, 11, 10, 40).getMillis());

        /// date not in list..
        next = EmployeeLogUtilities.getNextActiveEventAfterDate(eldEvents, new DateTime(2015, 1, 1, 1, 1).toDate());
        assertEquals("wrong event returned", next.getEventDateTime().getTime(), new DateTime(2017, 2, 10, 11,  0).getMillis());

    }


    @Test
    public void test_CombineLogEvents_SingleLogEvent() {
        EmployeeLogEldEvent[] originalEvents = new EmployeeLogEldEvent[]{new EmployeeLogEldEvent(), new EmployeeLogEldEvent()};
        EmployeeLogEldEventList originalEventList = new EmployeeLogEldEventList();
        originalEventList.setEldEventList(originalEvents);

        EmployeeLogEldEventList target = EmployeeLogUtilities.CombineLogEvents(originalEventList, new EmployeeLogEldEvent());

        Assert.assertEquals(3, target.getEldEventList().length);
    }

    @Test
    public void test_CombineLogEvents_SingleLogEvent_NullLogEvent() {
        EmployeeLogEldEvent[] originalEvents = new EmployeeLogEldEvent[]{new EmployeeLogEldEvent(), new EmployeeLogEldEvent()};
        EmployeeLogEldEventList originalEventList = new EmployeeLogEldEventList();
        originalEventList.setEldEventList(originalEvents);

        EmployeeLogEldEventList target = EmployeeLogUtilities.CombineLogEvents(originalEventList, (EmployeeLogEldEvent) null);

        Assert.assertEquals(2, target.getEldEventList().length);
    }

    @Test
    public void test_CombineLogEvents_SingleLogEvent_EmptyList() {
        EmployeeLogEldEventList originalEventList = new EmployeeLogEldEventList();

        EmployeeLogEldEventList target = EmployeeLogUtilities.CombineLogEvents(originalEventList, new EmployeeLogEldEvent());

        Assert.assertEquals(1, target.getEldEventList().length);
    }

    @Test
    public void test_CombineLogEvents_Lists() throws ParseException {
        EmployeeLogEldEvent[] originalEvents;

        originalEvents = new EmployeeLogEldEvent[]{new EmployeeLogEldEvent(dateFormat.parse("2015-08-01 00:00")), new EmployeeLogEldEvent(dateFormat.parse("2015-08-02 00:00"))};

        EmployeeLogEldEventList originalEventList = new EmployeeLogEldEventList();
        originalEventList.setEldEventList(originalEvents);

        EmployeeLogEldEvent[] additionalEvents = new EmployeeLogEldEvent[]{new EmployeeLogEldEvent(dateFormat.parse("2015-08-03 00:00")), new EmployeeLogEldEvent(dateFormat.parse("2015-08-04 00:00"))};
        EmployeeLogEldEventList additionalEventList = new EmployeeLogEldEventList();
        additionalEventList.setEldEventList(additionalEvents);

        EmployeeLogEldEventList target = EmployeeLogUtilities.CombineLogEvents(originalEventList, additionalEventList);

        Assert.assertEquals(4, target.getEldEventList().length);
    }

    @Test
    public void test_CombineLogEvents_EmptyOriginalList() {
        EmployeeLogEldEvent[] originalEvents = new EmployeeLogEldEvent[]{new EmployeeLogEldEvent(), new EmployeeLogEldEvent()};
        EmployeeLogEldEventList originalEventList = new EmployeeLogEldEventList();
        originalEventList.setEldEventList(originalEvents);

        EmployeeLogEldEventList additionalEventList = new EmployeeLogEldEventList();

        EmployeeLogEldEventList target = EmployeeLogUtilities.CombineLogEvents(originalEventList, additionalEventList);

        Assert.assertEquals(2, target.getEldEventList().length);
    }

    @Test
    public void test_CombineLogEvents_EmptyAdditionalList() {
        EmployeeLogEldEventList originalEventList = new EmployeeLogEldEventList();

        EmployeeLogEldEvent[] additionalEvents = new EmployeeLogEldEvent[]{new EmployeeLogEldEvent(), new EmployeeLogEldEvent()};
        EmployeeLogEldEventList additionalEventList = new EmployeeLogEldEventList();
        additionalEventList.setEldEventList(additionalEvents);

        EmployeeLogEldEventList target = EmployeeLogUtilities.CombineLogEvents(originalEventList, additionalEventList);

        Assert.assertEquals(2, target.getEldEventList().length);
    }

    @Test
    public void test_CombineLogEvents_DontAddDuplicates() throws ParseException {
        EmployeeLogEldEvent[] originalEvents = new EmployeeLogEldEvent[]{new EmployeeLogEldEvent(dateFormat.parse("2015-08-01 00:00")), new EmployeeLogEldEvent(dateFormat.parse("2015-08-02 00:00"))};
        EmployeeLogEldEventList originalEventList = new EmployeeLogEldEventList();
        originalEventList.setEldEventList(originalEvents);

        EmployeeLogEldEvent[] additionalEvents = new EmployeeLogEldEvent[]{new EmployeeLogEldEvent(dateFormat.parse("2015-08-02 00:00")), new EmployeeLogEldEvent(dateFormat.parse("2015-08-03 00:00"))};
        EmployeeLogEldEventList additionalEventList = new EmployeeLogEldEventList();
        additionalEventList.setEldEventList(additionalEvents);

        EmployeeLogEldEventList target = EmployeeLogUtilities.CombineLogEvents(originalEventList, additionalEventList);

        //only 3, because 2015-08-02 was duplicated
        Assert.assertEquals(3, target.getEldEventList().length);
    }

    @Test
    public void test_RemoveDuplicateEventIfExistsAOBRD() throws ParseException{
        GlobalState.getInstance().setCurrentDriversLog(employeeLog);

        Date offDutyTime1 = dateFormat.parse("2017-07-01 01:10");
        addDutyStatyChange(offDutyTime1, OFF_DUTY, true);

        Date onDutyTime1 = dateFormat.parse("2017-07-01 01:13");
        addDutyStatyChange(onDutyTime1, ON_DUTY, true);

        Date drivingTime1 = dateFormat.parse("2017-07-01 01:13");
        addDutyStatyChange(drivingTime1, DRIVING, true);

        EmployeeLogEldEvent[] events = employeeLog.getEldEventList().getEldEventList();

        Assert.assertEquals(3, events.length);

    }

    @Test
    public void AddEventToLogAOBRD() throws ParseException{
        Date offDutyTime1 = dateFormat.parse("2017-07-01 01:10");
        addDutyStatyChange(offDutyTime1, OFF_DUTY, true);
        Assert.assertEquals(2, employeeLog.getEldEventList().getEldEventList().length);

        addDutyStatyChange(dateFormat.parse("2017-07-01 01:12"), ON_DUTY, true);
        Assert.assertEquals(3, employeeLog.getEldEventList().getEldEventList().length);

        //Duplicate event should not be added to list
        addDutyStatyChange(dateFormat.parse("2017-07-01 01:12"), DRIVING, true);
        Assert.assertEquals(3, employeeLog.getEldEventList().getEldEventList().length);

        Assert.assertEquals(employeeLog.getEldEventList().getEldEventList()[ employeeLog.getEldEventList().getEldEventList().length -1 ].getDutyStatusEnum(), DRIVING);

        addDutyStatyChange(dateFormat.parse("2017-07-01 01:15"), DRIVING, true);
        Assert.assertEquals(4, employeeLog.getEldEventList().getEldEventList().length);

        addDutyStatyChange(dateFormat.parse("2017-07-01 01:16"), SLEEPER, true);
        Assert.assertEquals(5, employeeLog.getEldEventList().getEldEventList().length);
    }
}