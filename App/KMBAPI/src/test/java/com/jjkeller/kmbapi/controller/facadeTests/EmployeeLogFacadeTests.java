package com.jjkeller.kmbapi.controller.facadeTests;


import com.jjkeller.kmbapi.common.TestBase;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.configuration.LoginCredentials;
import com.jjkeller.kmbapi.configuration.UserState;
import com.jjkeller.kmbapi.controller.dataaccess.EmployeeLogFacade;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.controller.utility.EmployeeLogUtilities;
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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
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
public class EmployeeLogFacadeTests extends TestBase {

    private GlobalState app;
    private FeatureToggleService ftService;
    private EmployeeLogFacade employeeLogFacade;
    private User user;

    private final DutyStatusEnum OFF_DUTY = new DutyStatusEnum(DutyStatusEnum.OFFDUTY);

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        app = (GlobalState) RuntimeEnvironment.application;

        ftService = mock(FeatureToggleService.class);
        when(ftService.getIsEldMandateEnabled()).thenReturn(true);

        Field field = GlobalState.class.getDeclaredField("_featureToggleService");
        field.setAccessible(true);
        field.set(app, ftService);

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

        employeeLogFacade = new EmployeeLogFacade(app, user);
    }


    @After
    public void tearDown() throws NoSuchFieldException, IllegalAccessException {
        closeDatabase(app);
    }

    private void addDutyStatusChange(EmployeeLog log, Date time, DutyStatusEnum status) {
        EmployeeLogEldEvent event = new EmployeeLogEldEvent(time, new EmployeeLogEldEventCode(EmployeeLogEldEvent.translateDutyStatusEnumToMandateStatus(status)), Enums.EmployeeLogEldEventType.DutyStatusChange);
        if (log != null) {
            if (log.getEldEventList().getEldEventList() != null) {
                ArrayList<EmployeeLogEldEvent> newList = new ArrayList<>(Arrays.asList(log.getEldEventList().getEldEventList()));
                newList.add(event);
                log.getEldEventList().setEldEventList(newList);
            }
        }
    }

    @Test
    public void testSavingInMultipleThreadsDoesNotCreateDuplicates() {
        DateTimeZone tz = DateTimeZone.forID("America/Chicago");
        Date logDate = new DateTime(2018, 03, 01, 0, 0, 0, tz).toDate();
        Date nextDay = new DateTime(2018, 03, 02, 0, 0, 0, tz).toDate();

        final EmployeeLog log = EmployeeLogUtilities.CreateNewLog(app, user, logDate, OFF_DUTY, null);
        employeeLogFacade.Save(log, 1);

        HashMap<Date, Integer> eventTimes = new HashMap<>();
        eventTimes.put(logDate, 0);

        int dutyStatus = 0;
        long increment = Duration.standardMinutes(20).getMillis();
        for(long time = logDate.getTime() + increment; time < nextDay.getTime(); time += increment) {
            Date date = new Date(time);
            eventTimes.put(date, 0);

            addDutyStatusChange(log, date, new DutyStatusEnum((dutyStatus % 4) + 1));

            dutyStatus++;
        }

        final int iterations = 5;
        final CountDownLatch latch = new CountDownLatch(iterations);
        final ThreadPoolExecutor executor = new ThreadPoolExecutor(100, 100, 500, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<Runnable>());

        for(int i = 0; i < iterations; i++) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    employeeLogFacade.Save(log, 1);
                    latch.countDown();
                }
            });
        }

        try {
            latch.await(10000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        EmployeeLog savedLog = employeeLogFacade.GetLocalLogByDate(logDate);

        EmployeeLogEldEvent[] events = savedLog.getEldEventList().getEldEventList();

        assertEquals("Incorrect number of ELD events retrieved.", eventTimes.size(), events.length);

        int lastSequenceNumber = -1;
        for(int i = 0; i < eventTimes.size(); i++) {
            EmployeeLogEldEvent event = events[i];

            Date key = event.getEventDateTime();
            Integer count = eventTimes.get(key);
            eventTimes.put(key, count + 1);

            if(count > 0) {
                fail("Duplicate event found.");
            }

            if(event.getEventSequenceIDNumber() != lastSequenceNumber + 1) {
                fail("Non-sequential sequence ID number found");
            }
            lastSequenceNumber = event.getEventSequenceIDNumber();
        }

    }
}
