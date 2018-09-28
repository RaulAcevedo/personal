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
 * <p>
 * Created by jld5296 on 10/26/16.
 */
@RunWith(RobolectricTestRunner.class)
@Config(application = GlobalState.class,
        manifest = "/build/intermediates/bundles/encompass/debug/AndroidManifest.xml")
public class EmployeeLogUtilitiesAddEventTest extends TestBase {

    private GlobalState globalState;
    private EmployeeLog employeeLog;
    private RuleSetTypeEnum ruleType = new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR);

    private EmployeeLogFacade employeeLogFacade;

    private DutyStatusEnum OFF_DUTY = new DutyStatusEnum(DutyStatusEnum.OFFDUTY);
    private DutyStatusEnum ON_DUTY = new DutyStatusEnum(DutyStatusEnum.ONDUTY);
    private DutyStatusEnum DRIVING = new DutyStatusEnum(DutyStatusEnum.DRIVING);
    private DutyStatusEnum SLEEPER = new DutyStatusEnum(DutyStatusEnum.SLEEPER);

    private FeatureToggleService ftService;

    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm", Locale.US);


    @Before
    public void setUp() throws Exception {
        globalState = (GlobalState) RuntimeEnvironment.application;

        ftService = mock(FeatureToggleService.class);
        when(ftService.getIsEldMandateEnabled()).thenReturn(true);


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

        Date logStartDate = dateFormat.parse("2017-09-15 00:00");
        employeeLog = EmployeeLogUtilities.CreateNewLog(globalState, user, logStartDate, DutyStatusEnum.valueOfDMOEnum(DutyStatusEnum.DmoEnum_OffDuty), null);
        globalState.setCurrentEmployeeLog(employeeLog);


    }

    @After
    public void tearDown() throws NoSuchFieldException, IllegalAccessException {
        closeDatabase(globalState);
        employeeLog = null;
    }

    public Date getDateForTime(int hour, int mins) {
        Date logStart = employeeLog.getLogDate();
        Calendar c = Calendar.getInstance();
        c.setTime(logStart);
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, mins);
        return c.getTime();
    }

    public void assertEventTimeSequence(EmployeeLog log, Date... dates) {
        EmployeeLogEldEvent[] events = log.getEldEventList().getEldEventList();

        assertEquals("Total number of events is wrong", dates.length, log.getEldEventList().getEldEventList().length);

        for (int i = 0; i < events.length; i++) {
            assertEquals("event date incoorect or out of order at index:" + i, dates[i], events[i].getEventDateTime());
        }
    }


    @Test
    public void addEvent() throws Exception {
        Date eventTime1 = getDateForTime(10, 15);
        Date eventTime2 = getDateForTime(11, 15);

        EmployeeLogUtilities.AddEventToLog(employeeLog, eventTime1, ON_DUTY, null, true, ruleType, "remark", eventTime1);
        EmployeeLogUtilities.AddEventToLog(employeeLog, eventTime2, OFF_DUTY, null, true, ruleType, "remark", eventTime2);

        assertEventTimeSequence(employeeLog, employeeLog.getLogDate(), eventTime1, eventTime2);
    }


    @Test
    public void addOutOfOrder() throws Exception {
        Date eventTime1 = getDateForTime(10, 15);
        Date eventTime2 = getDateForTime(12, 15);

        EmployeeLogUtilities.AddEventToLog(employeeLog, eventTime2, OFF_DUTY, null, true, ruleType, "remark", eventTime2);
        EmployeeLogUtilities.AddEventToLog(employeeLog, eventTime1, ON_DUTY, null, true, ruleType, "remark", eventTime1);

        assertEventTimeSequence(employeeLog, employeeLog.getLogDate(), eventTime1, eventTime2);
    }

    @Test
    public void addDup() throws Exception {
        Date eventTime1 = getDateForTime(10, 15);

        EmployeeLogUtilities.AddEventToLog(employeeLog, eventTime1, ON_DUTY, null, true, ruleType, "remark", eventTime1);
        EmployeeLogUtilities.AddEventToLog(employeeLog, eventTime1, ON_DUTY, null, true, ruleType, "remark", eventTime1);

        assertEventTimeSequence(employeeLog, employeeLog.getLogDate(), eventTime1);
    }


    @Test
    public void addEventAOBRD() throws Exception {
        when(ftService.getIsEldMandateEnabled()).thenReturn(false);

        Date eventTime1 = getDateForTime(10, 15);
        Date eventTime2 = getDateForTime(11, 15);

        EmployeeLogUtilities.AddEventToLog(employeeLog, eventTime1, ON_DUTY, null, true, ruleType, "remark", eventTime1);
        EmployeeLogUtilities.AddEventToLog(employeeLog, eventTime2, OFF_DUTY, null, true, ruleType, "remark", eventTime2);

        assertEventTimeSequence(employeeLog, employeeLog.getLogDate(), eventTime1, eventTime2);
    }


    @Test
    public void addOutOfOrderAobrd() throws Exception {
        when(ftService.getIsEldMandateEnabled()).thenReturn(false);

        Date eventTime1 = getDateForTime(10, 15);
        Date eventTime2 = getDateForTime(12, 15);

        EmployeeLogUtilities.AddEventToLog(employeeLog, eventTime2, OFF_DUTY, null, true, ruleType, "remark", eventTime2);
        EmployeeLogUtilities.AddEventToLog(employeeLog, eventTime1, ON_DUTY, null, true, ruleType, "remark", eventTime1);

        assertEventTimeSequence(employeeLog, employeeLog.getLogDate(), eventTime1, eventTime2);
    }

    @Test
    public void addDupAobrd() throws Exception {
        when(ftService.getIsEldMandateEnabled()).thenReturn(false);
        Date eventTime1 = getDateForTime(10, 15);

        EmployeeLogUtilities.AddEventToLog(employeeLog, eventTime1, ON_DUTY, null, true, ruleType, "remark", eventTime1);
        EmployeeLogUtilities.AddEventToLog(employeeLog, eventTime1, ON_DUTY, null, true, ruleType, "remark", eventTime1);

        assertEventTimeSequence(employeeLog, employeeLog.getLogDate(), eventTime1);
    }


    @Test
    public void addFirstTimeDupDupAobrd() throws Exception {
        when(ftService.getIsEldMandateEnabled()).thenReturn(false);
        Date eventTime1 = getDateForTime(10, 15);
        Date eventTime2 = getDateForTime(12, 16);

        EmployeeLogUtilities.AddEventToLog(employeeLog, eventTime1, ON_DUTY, null, true, ruleType, "remark", eventTime1);
        EmployeeLogUtilities.AddEventToLog(employeeLog, eventTime2, OFF_DUTY, null, true, ruleType, "remark", eventTime2);


        EmployeeLogEldEvent firstEvent = employeeLog.getEldEventList().getEldEventList()[0];

        EmployeeLogUtilities.AddEventToLog(employeeLog, firstEvent.getEventDateTime(), firstEvent.getDutyStatusEnum(), null, true, firstEvent.getRuleSet(), "remark", firstEvent.getEventDateTime());

        assertEventTimeSequence(employeeLog, employeeLog.getLogDate(), eventTime1, eventTime2);
    }

    @Test
    public void addFirstTimeDup() throws Exception {
        when(ftService.getIsEldMandateEnabled()).thenReturn(true);
        Date eventTime1 = getDateForTime(10, 15);
        Date eventTime2 = getDateForTime(12, 16);

        EmployeeLogUtilities.AddEventToLog(employeeLog, eventTime1, ON_DUTY, null, true, ruleType, "remark", eventTime1);
        EmployeeLogUtilities.AddEventToLog(employeeLog, eventTime2, OFF_DUTY, null, true, ruleType, "remark", eventTime2);

        EmployeeLogEldEvent firstEvent = employeeLog.getEldEventList().getEldEventList()[0];

        EmployeeLogUtilities.AddEventToLog(employeeLog, firstEvent.getEventDateTime(), firstEvent.getDutyStatusEnum(), null, true, firstEvent.getRuleSet(), "remark", firstEvent.getEventDateTime());

        assertEventTimeSequence(employeeLog, employeeLog.getLogDate(), eventTime1, eventTime2);
    }

}