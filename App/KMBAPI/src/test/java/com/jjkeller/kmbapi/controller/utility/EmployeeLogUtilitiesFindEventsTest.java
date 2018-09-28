package com.jjkeller.kmbapi.controller.utility;

import com.jjkeller.kmbapi.common.TestBase;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.configuration.LoginCredentials;
import com.jjkeller.kmbapi.configuration.UserState;
import com.jjkeller.kmbapi.controller.ControllerFactory;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@RunWith(RobolectricTestRunner.class)
@Config(application = GlobalState.class,
        manifest = "/build/intermediates/bundles/encompass/debug/AndroidManifest.xml"
)
public class EmployeeLogUtilitiesFindEventsTest extends TestBase {

    private GlobalState globalState;
    private FeatureToggleService ftService;
    protected EmployeeLogFacade employeeLogFacade;


    private static EmployeeLogEldEvent createEldEvent(Enums.EmployeeLogEldEventType eventType, Enums.EmployeeLogEldEventRecordStatus recordStatus, DateTime eventDateTime, int dutyStatusEnum) {
        EmployeeLogEldEvent eldEvent = new EmployeeLogEldEvent(eventDateTime.toDate(), new EmployeeLogEldEventCode(dutyStatusEnum), Enums.EmployeeLogEldEventType.DutyStatusChange);
        eldEvent.setEventType(eventType);
        eldEvent.setEventRecordStatus(recordStatus.getValue());
        eldEvent.setDutyStatusEnum(new DutyStatusEnum(dutyStatusEnum));
        return eldEvent;
    }


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

        employeeLogFacade = new EmployeeLogFacade(globalState, user);
    }

    @After
    public void tearDown() throws NoSuchFieldException, IllegalAccessException {
        closeDatabase(globalState);
        ftService = null;
    }

    private EmployeeLog saveLogWithEvents(Date logDate, EmployeeLogEldEvent... events) {
        EmployeeLog employeeLog = EmployeeLogUtilities.CreateNewLog(globalState, globalState.getCurrentUser(), logDate, DutyStatusEnum.valueOfDMOEnum(DutyStatusEnum.DmoEnum_OffDuty), null);
        globalState.setCurrentEmployeeLog(employeeLog);


        List<EmployeeLogEldEvent> eldEvents = new ArrayList<>();
        eldEvents.addAll(Arrays.asList(employeeLog.getEldEventList().getEldEventList()));

        for (EmployeeLogEldEvent addme : events) {
            eldEvents.add(addme);
        }

        employeeLog.getEldEventList().setEldEventList(eldEvents.toArray(new EmployeeLogEldEvent[eldEvents.size()]));

        employeeLogFacade.Save(employeeLog, 1);
        return employeeLog;
    }

    @Test
    public void testGetNextActiveEventAfterDateSorted() {
        EmployeeLog log = saveLogWithEvents(new DateTime(2017, 4, 10, 13, 30).toDate()
                , createEldEvent(Enums.EmployeeLogEldEventType.DutyStatusChange, Enums.EmployeeLogEldEventRecordStatus.Active, new DateTime(2017, 4, 10, 13, 30), DutyStatusEnum.OFFDUTY)
                , createEldEvent(Enums.EmployeeLogEldEventType.DutyStatusChange, Enums.EmployeeLogEldEventRecordStatus.Active, new DateTime(2017, 4, 10, 15, 30), DutyStatusEnum.SLEEPER)
        );

        EmployeeLogEldEvent event = EmployeeLogUtilities.getLastActiveDutyStatusChange(log);

        Assert.assertEquals(DutyStatusEnum.SLEEPER, event.getDutyStatusEnum().getValue());
        Assert.assertEquals(new DateTime(2017, 4, 10, 15, 30).toInstant().getMillis(), event.getEventDateTime().getTime());
    }


    @Test
    public void testGetNextActiveEventAfterDateUnSorted() {
        EmployeeLog log = saveLogWithEvents(new DateTime(2017, 4, 10, 13, 30).toDate()
                , createEldEvent(Enums.EmployeeLogEldEventType.DutyStatusChange, Enums.EmployeeLogEldEventRecordStatus.Active, new DateTime(2017, 4, 2, 13, 30), DutyStatusEnum.OFFDUTY)
                , createEldEvent(Enums.EmployeeLogEldEventType.DutyStatusChange, Enums.EmployeeLogEldEventRecordStatus.Active, new DateTime(2017, 4, 10, 15, 30), DutyStatusEnum.ONDUTY)
                , createEldEvent(Enums.EmployeeLogEldEventType.DutyStatusChange, Enums.EmployeeLogEldEventRecordStatus.Active, new DateTime(2017, 4, 15, 12, 31), DutyStatusEnum.SLEEPER)
                , createEldEvent(Enums.EmployeeLogEldEventType.DutyStatusChange, Enums.EmployeeLogEldEventRecordStatus.Active, new DateTime(2017, 4, 9, 11, 31), DutyStatusEnum.DRIVING)
                , createEldEvent(Enums.EmployeeLogEldEventType.DutyStatusChange, Enums.EmployeeLogEldEventRecordStatus.Active, new DateTime(2017, 4, 13, 11, 31), DutyStatusEnum.ONDUTY)

        );

        EmployeeLogEldEvent event = EmployeeLogUtilities.getLastActiveDutyStatusChange(log);

        Assert.assertEquals(DutyStatusEnum.SLEEPER, event.getDutyStatusEnum().getValue());
        Assert.assertEquals(new DateTime(2017, 4, 15, 12, 31).toInstant().getMillis(), event.getEventDateTime().getTime());

    }


    @Test
    public void testGetNextActiveWithNoActive() {
        EmployeeLog log = saveLogWithEvents(new DateTime(2017, 4, 10, 13, 30).toDate()
                , createEldEvent(Enums.EmployeeLogEldEventType.DutyStatusChange, Enums.EmployeeLogEldEventRecordStatus.InactiveChanged, new DateTime(2017, 4, 2, 13, 30), DutyStatusEnum.OFFDUTY)
                , createEldEvent(Enums.EmployeeLogEldEventType.DutyStatusChange, Enums.EmployeeLogEldEventRecordStatus.InactiveChanged, new DateTime(2017, 4, 10, 15, 30), DutyStatusEnum.ONDUTY)
                , createEldEvent(Enums.EmployeeLogEldEventType.DutyStatusChange, Enums.EmployeeLogEldEventRecordStatus.InactiveChanged, new DateTime(2017, 4, 15, 12, 31), DutyStatusEnum.SLEEPER)
                , createEldEvent(Enums.EmployeeLogEldEventType.DutyStatusChange, Enums.EmployeeLogEldEventRecordStatus.InactiveChanged, new DateTime(2017, 4, 9, 11, 31), DutyStatusEnum.DRIVING)
                , createEldEvent(Enums.EmployeeLogEldEventType.DutyStatusChange, Enums.EmployeeLogEldEventRecordStatus.InactiveChanged, new DateTime(2017, 4, 13, 11, 31), DutyStatusEnum.ONDUTY)
        );
        EmployeeLogEldEvent event = EmployeeLogUtilities.getLastActiveDutyStatusChange(log);

        Assert.assertEquals(DutyStatusEnum.OFFDUTY, event.getDutyStatusEnum().getValue());
        Assert.assertEquals(new DateTime(2017, 4, 10, 0, 0).toInstant().getMillis(), event.getEventDateTime().getTime());
    }


    @Test
    public void testGetNextActiveGrabActive() {
        EmployeeLog log = saveLogWithEvents(new DateTime(2017, 4, 10, 13, 30).toDate()
                , createEldEvent(Enums.EmployeeLogEldEventType.DutyStatusChange, Enums.EmployeeLogEldEventRecordStatus.InactiveChanged, new DateTime(2017, 4, 2, 13, 30), DutyStatusEnum.OFFDUTY)
                , createEldEvent(Enums.EmployeeLogEldEventType.DutyStatusChange, Enums.EmployeeLogEldEventRecordStatus.Active, new DateTime(2017, 4, 10, 15, 30), DutyStatusEnum.OFFDUTY)
                , createEldEvent(Enums.EmployeeLogEldEventType.DutyStatusChange, Enums.EmployeeLogEldEventRecordStatus.InactiveChanged, new DateTime(2017, 4, 15, 12, 31), DutyStatusEnum.SLEEPER)
                , createEldEvent(Enums.EmployeeLogEldEventType.DutyStatusChange, Enums.EmployeeLogEldEventRecordStatus.Active, new DateTime(2017, 4, 9, 11, 31), DutyStatusEnum.DRIVING)
                , createEldEvent(Enums.EmployeeLogEldEventType.DutyStatusChange, Enums.EmployeeLogEldEventRecordStatus.Active, new DateTime(2017, 4, 13, 11, 31), DutyStatusEnum.ONDUTY)
                , createEldEvent(Enums.EmployeeLogEldEventType.DutyStatusChange, Enums.EmployeeLogEldEventRecordStatus.InactiveChanged, new DateTime(2017, 4, 17, 12, 31), DutyStatusEnum.SLEEPER)

        );

        EmployeeLogEldEvent event = EmployeeLogUtilities.getLastActiveDutyStatusChange(log);

        Assert.assertEquals(DutyStatusEnum.ONDUTY, event.getDutyStatusEnum().getValue());
        Assert.assertEquals(new DateTime(2017, 4, 13, 11, 31).toInstant().getMillis(), event.getEventDateTime().getTime());

    }

}