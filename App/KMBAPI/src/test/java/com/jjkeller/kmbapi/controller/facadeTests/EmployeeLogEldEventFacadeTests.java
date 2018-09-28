package com.jjkeller.kmbapi.controller.facadeTests;


import com.jjkeller.kmbapi.common.TestBase;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.configuration.LoginCredentials;
import com.jjkeller.kmbapi.configuration.UserState;
import com.jjkeller.kmbapi.controller.dataaccess.EmployeeLogEldEventFacade;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.employeelogeldevents.EmployeeLogEldEventCode;
import com.jjkeller.kmbapi.employeelogeldevents.Enums;
import com.jjkeller.kmbapi.enums.DriverTypeEnum;
import com.jjkeller.kmbapi.enums.DutyStatusEnum;
import com.jjkeller.kmbapi.enums.RuleSetTypeEnum;
import com.jjkeller.kmbapi.enums.TimeZoneEnum;
import com.jjkeller.kmbapi.proxydata.CompanyConfigSettings;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * // * Set of Integration Tests for the TeamDriverController
 */
@RunWith(RobolectricTestRunner.class)
@Config(application = GlobalState.class,
        manifest = "/build/intermediates/bundles/encompass/debug/AndroidManifest.xml")
public class EmployeeLogEldEventFacadeTests extends TestBase {

    private GlobalState app;
    private EmployeeLogEldEventFacade employeeLogEldEventFacade;
    private User user;

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        app = (GlobalState) RuntimeEnvironment.application;

        user = mock(User.class);
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

        employeeLogEldEventFacade = new EmployeeLogEldEventFacade(app, user);
    }


    @After
    public void tearDown() throws NoSuchFieldException, IllegalAccessException {
        closeDatabase(app);
    }

    @Test
    public void testSaveInMultipleThreadsDoesNotCreateDuplicates() {
        DateTimeZone tz = DateTimeZone.forID("America/Chicago");
        Date logDate = new DateTime(2018, 03, 01, 0, 0, 0, tz).toDate();

        HashMap<Date, Integer> eventTimes = new HashMap<>();

        final int logKey = 1;

        final EmployeeLogEldEvent event = new EmployeeLogEldEvent(logDate, new EmployeeLogEldEventCode(4), Enums.EmployeeLogEldEventType.DutyStatusChange);
        event.setLogKey(logKey);
        event.setEventRecordStatus(1);
        eventTimes.put(logDate, 0);

        final int iterations = 5;
        final CountDownLatch latch = new CountDownLatch(iterations);
        final ThreadPoolExecutor executor = new ThreadPoolExecutor(100, 100, 500, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<Runnable>());

        for(int i = 0; i < iterations; i++) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    employeeLogEldEventFacade.Save(event);
                    latch.countDown();
                }
            });
        }

        try {
            latch.await(10000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        verifyResults(eventTimes, logKey);
    }

    @Test
    public void testSaveListInMultipleThreadsDoesNotCreateDuplicates() {
        DateTimeZone tz = DateTimeZone.forID("America/Chicago");
        Date logDate = new DateTime(2018, 03, 01, 0, 0, 0, tz).toDate();
        Date nextDay = new DateTime(2018, 03, 02, 0, 0, 0, tz).toDate();

        HashMap<Date, Integer> eventTimes = new HashMap<>();
        ArrayList<EmployeeLogEldEvent> events = new ArrayList<>();

        int dutyStatus = 0;
        final long increment = Duration.standardMinutes(20).getMillis();
        final int logKey = 1;
        for(long time = logDate.getTime() + increment; time < nextDay.getTime(); time += increment) {
            Date date = new Date(time);
            eventTimes.put(date, 0);

            int code = EmployeeLogEldEvent.translateDutyStatusEnumToMandateStatus(new DutyStatusEnum((dutyStatus % 4) + 1));
            dutyStatus++;

            EmployeeLogEldEvent event = new EmployeeLogEldEvent(date, new EmployeeLogEldEventCode(code), Enums.EmployeeLogEldEventType.DutyStatusChange);
            event.setLogKey(logKey);
            event.setEventRecordStatus(1);

            events.add(event);
        }

        final int iterations = 5;
        final CountDownLatch latch = new CountDownLatch(iterations);
        final ThreadPoolExecutor executor = new ThreadPoolExecutor(100, 100, 500, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<Runnable>());

        final EmployeeLogEldEvent[] eventArray = events.toArray(new EmployeeLogEldEvent[events.size()]);
        for(int i = 0; i < iterations; i++) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    employeeLogEldEventFacade.SaveListInSingleTransaction(eventArray);
                    latch.countDown();
                }
            });
        }

        try {
            latch.await(10000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        verifyResults(eventTimes, logKey);
    }

    @Test
    public void testInsertDuplicateEmployeeLogEldEvent() {
        DateTimeZone tz = DateTimeZone.forID("America/Chicago");
        Date logDate = new DateTime(2018, 06, 26, 0, 0, 0, tz).toDate();
        Date eventDateTime = new DateTime(2018, 06, 26, 9, 0, 0, tz).toDate();

        HashMap<Date, Integer> eventTimes = new HashMap<>();
        eventTimes.put(logDate, 0);
        eventTimes.put(eventDateTime, 0);

        int logKey = 1;

        EmployeeLogEldEvent midnightEvent = new EmployeeLogEldEvent(logDate, new EmployeeLogEldEventCode(1), Enums.EmployeeLogEldEventType.DutyStatusChange);
        midnightEvent.setLogKey(logKey);
        midnightEvent.setEventRecordStatus(1);
        employeeLogEldEventFacade.Save(midnightEvent);

        EmployeeLogEldEvent event1 = new EmployeeLogEldEvent(eventDateTime, new EmployeeLogEldEventCode(4), Enums.EmployeeLogEldEventType.DutyStatusChange);
        event1.setLogKey(logKey);
        event1.setEventRecordStatus(1);
        employeeLogEldEventFacade.Save(event1);

        EmployeeLogEldEvent eventDuplicate = new EmployeeLogEldEvent(eventDateTime, new EmployeeLogEldEventCode(3), Enums.EmployeeLogEldEventType.DutyStatusChange);
        eventDuplicate.setLogKey(logKey);
        eventDuplicate.setEventRecordStatus(1);
        employeeLogEldEventFacade.Save(eventDuplicate);

        verifyResults(eventTimes, logKey);
    }

    @Test
    public void testUpdateDuplicateEmployeeLogEldEvent() {
        DateTimeZone tz = DateTimeZone.forID("America/Chicago");
        Date logDate = new DateTime(2018, 06, 26, 0, 0, 0, tz).toDate();
        Date eventDateTime = new DateTime(2018, 06, 26, 9, 0, 0, tz).toDate();

        HashMap<Date, Integer> eventTimes = new HashMap<>();
        eventTimes.put(logDate, 0);
        eventTimes.put(eventDateTime, 0);

        int logKey = 1;
        int logKey2 = 1;

        EmployeeLogEldEvent midnightEvent = new EmployeeLogEldEvent(logDate, new EmployeeLogEldEventCode(1), Enums.EmployeeLogEldEventType.DutyStatusChange);
        midnightEvent.setLogKey(logKey);
        midnightEvent.setEventRecordStatus(1);
        employeeLogEldEventFacade.Save(midnightEvent);

        EmployeeLogEldEvent event1 = new EmployeeLogEldEvent(eventDateTime, new EmployeeLogEldEventCode(4), Enums.EmployeeLogEldEventType.DutyStatusChange);
        event1.setLogKey(logKey);
        event1.setEventRecordStatus(1);
        employeeLogEldEventFacade.Save(event1);

        EmployeeLogEldEvent eventDuplicate = new EmployeeLogEldEvent(eventDateTime, new EmployeeLogEldEventCode(4), Enums.EmployeeLogEldEventType.DutyStatusChange);
        event1.setLogKey(logKey2);          //Saving even on a different log initially (insert should succeed)
        eventDuplicate.setEventRecordStatus(1);
        employeeLogEldEventFacade.Save(eventDuplicate);

        eventDuplicate.setLogKey(logKey);
        employeeLogEldEventFacade.Save(eventDuplicate);

        verifyResults(eventTimes, logKey);
    }

    private void verifyResults(HashMap<Date, Integer> eventTimes, int logKey) {
        List<Integer> params = Arrays.asList(new Integer[]{1});
        EmployeeLogEldEvent[] savedEvents = employeeLogEldEventFacade.GetByEventTypes(logKey, params, params);

        assertEquals("Incorrect number of ELD events retrieved.", eventTimes.size(), savedEvents.length);

        for(int i = 0; i < eventTimes.size(); i++) {
            EmployeeLogEldEvent event = savedEvents[i];

            Date key = event.getEventDateTime();
            Integer count = eventTimes.get(key);
            eventTimes.put(key, count + 1);

            if(count > 0) {
                fail("Duplicate event found.");
            }
        }
    }
}
