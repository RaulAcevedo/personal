package com.jjkeller.kmbapi.domain;

import com.jjkeller.kmbapi.common.TestBase;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.configuration.LoginCredentials;
import com.jjkeller.kmbapi.configuration.UserState;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.enums.DriverTypeEnum;
import com.jjkeller.kmbapi.enums.DutyStatusEnum;
import com.jjkeller.kmbapi.enums.RuleSetTypeEnum;
import com.jjkeller.kmbapi.enums.TimeZoneEnum;
import com.jjkeller.kmbapi.proxydata.CompanyConfigSettings;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEventList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Date;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by T000684 on 7/13/2017.
 */
@RunWith(RobolectricTestRunner.class)
@Config(application = GlobalState.class,
        manifest = "/build/intermediates/bundles/encompass/debug/AndroidManifest.xml"
)
public class EmployeeLogDistanceCalcTest extends TestBase {

    private GlobalState app;

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        app = (GlobalState) RuntimeEnvironment.application;

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

    }

    @After
    public void afterTest() throws NoSuchFieldException, IllegalAccessException {
        closeDatabase(app);
    }


    private EmployeeLog buildLog(Date mobileStartTime, EmployeeLogEldEvent... events) {
        EmployeeLog employeeLog = new EmployeeLog();
        employeeLog.setMobileStartTimestamp(mobileStartTime);
        EmployeeLogEldEventList eventList = new EmployeeLogEldEventList();
        eventList.setEldEventList(events);
        employeeLog.setEldEventList(eventList);
        return employeeLog;
    }

    private EmployeeLogEldEvent getEventWithOdometer(int statusValue, float odometer, float endOdometer, int activeStatus) {
        EmployeeLogEldEvent event = new EmployeeLogEldEvent(new Date());
        event.setStartTime(new Date());
        event.setDutyStatusEnum(new DutyStatusEnum(statusValue));
        event.setOdometer(odometer);
        event.setEndOdometer(endOdometer);
        event.setEventRecordStatus(activeStatus);
        return event;
    }


    private EmployeeLogEldEvent getEventWithDistance(int statusValue, int distance, int activeStatus) {
        EmployeeLogEldEvent event = new EmployeeLogEldEvent(new Date());
        event.setStartTime(new Date());
        event.setDutyStatusEnum(new DutyStatusEnum(statusValue));
        event.setOdometer(100f);
        event.setEndOdometer(null);
        event.setDistance(distance);
        event.setEventRecordStatus(activeStatus);
        return event;
    }


    @Test
    public void testDistanceCalc() {
        EmployeeLog testLog = buildLog(new Date(), getEventWithOdometer(DutyStatusEnum.DRIVING, 55, 56.0f, 1)
                , getEventWithOdometer(DutyStatusEnum.DRIVING, 56, 58.0f, 1)
                , getEventWithOdometer(DutyStatusEnum.DRIVING, 58, 60.0f, 1));
        assertEquals("total driving isn't correct", 5f, testLog.getMobileDerivedDistance());
    }


    @Test
    public void testDistanceCalcIntRounding() {
        EmployeeLog testLog = buildLog(new Date(), getEventWithOdometer(DutyStatusEnum.DRIVING, 55, 55.5f, 1)
                , getEventWithOdometer(DutyStatusEnum.DRIVING, 55.5f, 55.9f, 1)
                , getEventWithOdometer(DutyStatusEnum.DRIVING, 55.9f, 60f, 1));

        assertEquals("total driving isn't correct", 5f, testLog.getMobileDerivedDistance());
    }


    @Test
    public void testDistanceCalcWithZeroStartingValue() {
        EmployeeLog testLog = buildLog(new Date(), getEventWithOdometer(DutyStatusEnum.DRIVING, 0, 10.5f, 1)
                , getEventWithOdometer(DutyStatusEnum.DRIVING, 10.5f, 15.33f, 1)
                , getEventWithOdometer(DutyStatusEnum.DRIVING, 15.33f, 16.77f, 1));

        assertEquals("total driving isn't correct", 16.7f, testLog.getMobileDerivedDistance());
    }


    @Test
    public void testDistanceCalcWithGap() {
        EmployeeLog testLog = buildLog(new Date(), getEventWithOdometer(DutyStatusEnum.DRIVING, 2.9999999999f, 4.99999999999f, 1)
                , getEventWithOdometer(DutyStatusEnum.DRIVING, 4.999999898989f, 10.22222f, 1)
                , getEventWithOdometer(DutyStatusEnum.DRIVING, 15, 16f, 1));

        assertEquals("total driving isn't correct", 8.2f, testLog.getMobileDerivedDistance());
    }

    @Test
    public void testDistanceCalcExcludeInactive() {
        EmployeeLog testLog = buildLog(new Date(), getEventWithOdometer(DutyStatusEnum.DRIVING, 2.9999999999f, 4.99999999999f, 1)
                , getEventWithOdometer(DutyStatusEnum.DRIVING, 4.999999898989f, 10.22222f, 2)
                , getEventWithOdometer(DutyStatusEnum.DRIVING, 15, 16f, 2));

        assertEquals("total driving isn't correct", 2f, testLog.getMobileDerivedDistance());
    }


    @Test
    public void testDistanceCalcInt() {
        EmployeeLog testLog = buildLog(new Date(), getEventWithOdometer(DutyStatusEnum.DRIVING, 400, 454, 1)
                , getEventWithOdometer(DutyStatusEnum.DRIVING, 454f, 454, 1)
                , getEventWithOdometer(DutyStatusEnum.DRIVING, 454f, 500, 1));

        assertEquals("Total was off", 100f, testLog.getMobileDerivedDistance());
    }


    @Test
    public void testDistanceWithoutOdometerValues() {
        EmployeeLog testLog = buildLog(new Date(), getEventWithOdometer(DutyStatusEnum.DRIVING, 400, 454, 1)
                , getEventWithOdometer(DutyStatusEnum.DRIVING, 454f, 454, 1)
                , getEventWithDistance(DutyStatusEnum.DRIVING, 25, 1));

        assertEquals("Total was off", 79f, testLog.getMobileDerivedDistance());
    }


    @Test
    public void testDistanceCalcMixedValues() {
        EmployeeLog testLog = buildLog(new Date(), getEventWithOdometer(DutyStatusEnum.DRIVING, 400, 454, 1)
                , getEventWithOdometer(DutyStatusEnum.DRIVING, 454f, 480, 0)
                , getEventWithOdometer(DutyStatusEnum.DRIVING, 454f, 454, 1)
                , getEventWithDistance(DutyStatusEnum.DRIVING, 20, 0)
                , getEventWithDistance(DutyStatusEnum.DRIVING, 25, 1));

        assertEquals("Total was off", 79f, testLog.getMobileDerivedDistance());
    }
}
