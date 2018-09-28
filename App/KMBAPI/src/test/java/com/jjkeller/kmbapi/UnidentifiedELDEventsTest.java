package com.jjkeller.kmbapi;

import com.jjkeller.kmbapi.common.TestBase;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.configuration.LoginCredentials;
import com.jjkeller.kmbapi.configuration.UserState;
import com.jjkeller.kmbapi.controller.EobrConfigController;
import com.jjkeller.kmbapi.controller.dataaccess.EmployeeLogEldEventFacade;
import com.jjkeller.kmbapi.controller.dataaccess.db.EmployeeLogEldEventPersist;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.employeelogeldevents.Enums;
import com.jjkeller.kmbapi.enums.DriverTypeEnum;
import com.jjkeller.kmbapi.enums.RuleSetTypeEnum;
import com.jjkeller.kmbapi.enums.TimeZoneEnum;
import com.jjkeller.kmbapi.enums.UnidentifiedEldEventStatus;
import com.jjkeller.kmbapi.featuretoggle.FeatureToggleService;
import com.jjkeller.kmbapi.proxydata.CompanyConfigSettings;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by crogers on 4/4/2017.
 * Test methods: GetUnreviewedUnidentifiedEvents(), GetUnsubmittedUnidentifiedEvents(), UpdateEmployeeLogEldEvent(log)
 */

@RunWith(RobolectricTestRunner.class)
@Config(application = GlobalState.class,
        manifest = "/build/intermediates/bundles/encompass/debug/AndroidManifest.xml"
)
public class UnidentifiedELDEventsTest extends TestBase {

    private GlobalState app;
    private FeatureToggleService ftService;

    private EobrConfigController eobrConfig;
    private EmployeeLogEldEventFacade eldEventFacade;

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        app = (GlobalState) RuntimeEnvironment.application;

        ftService = mock(FeatureToggleService.class);
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

        CompanyConfigSettings settings = new CompanyConfigSettings();
        settings.setDailyLogStartTime("00:00");
        settings.setDmoCompanyName("Test Company");
        app.setCompanyConfigSettings(app, settings);

        LoginCredentials creds = mock(LoginCredentials.class);
        when(creds.getEmployeeId()).thenReturn("00000000-0000-0000-0000-000000000001");
        when(user.getCredentials()).thenReturn(creds);

        app.setCurrentUser(user);

        ArrayList<User> userList = new ArrayList<User>();
        userList.add(user);
        app.setLoggedInUserList(userList);

        app.setCurrentUser(user);

        eobrConfig = mock(EobrConfigController.class);
        when(eobrConfig.getSerialNumber()).thenReturn("number");

        eldEventFacade = new EmployeeLogEldEventFacade(app, user);
        eldEventFacade.Save(setEvent("06/26/2018 08:00:00", UnidentifiedEldEventStatus.LOCAL, false));
        eldEventFacade.Save(setEvent("06/26/2018 08:05:00", UnidentifiedEldEventStatus.LOCAL, false));
        eldEventFacade.Save(setEvent("06/26/2018 08:10:00", UnidentifiedEldEventStatus.LOCAL, false));
        eldEventFacade.Save(setEvent("06/26/2018 08:15:00", UnidentifiedEldEventStatus.LOCAL, true));
        eldEventFacade.Save(setEvent("06/26/2018 08:20:00", UnidentifiedEldEventStatus.SUBMITTED, false));
        eldEventFacade.Save(setEvent("06/26/2018 08:25:00", UnidentifiedEldEventStatus.SUBMITTED, true));

    }

    @After
    public void tearDown() throws NoSuchFieldException, IllegalAccessException {
        closeDatabase(app);
    }

    @Test
    public void testFetchUnidentifiedELDEventRecords() throws Throwable {

        List<EmployeeLogEldEvent> unidentifiedEvents = eldEventFacade.GetUnreviewedUnidentifiedEvents();
        assertEquals("Fetch list should return 3 of the 4 test records", unidentifiedEvents.size(), 3);
        for (EmployeeLogEldEvent log : unidentifiedEvents) {
            assertEquals("EmployeeLogEldEvent/UnidentifiedEventStatus should be false on fetch", log.getUnidentifiedEventStatus(), UnidentifiedEldEventStatus.LOCAL);
        }

    }

    @Test
    public void testUpdateUnidentifiedELDEventRecords() throws Throwable {

        List<EmployeeLogEldEvent> unidentifiedEvents = eldEventFacade.GetUnreviewedUnidentifiedEvents();
        assertEquals("Fetch list should return 3 of the 6 test records", 3, unidentifiedEvents.size());
        for (EmployeeLogEldEvent log : unidentifiedEvents) {
            log.setIsReviewed(true);
            EmployeeLogEldEventPersist<EmployeeLogEldEvent> persist = new EmployeeLogEldEventPersist<>(EmployeeLogEldEvent.class, app.getBaseContext());
            persist.UpdateEmployeeLogEldEvent(log);
        }

        List<EmployeeLogEldEvent> updatedLogs = eldEventFacade.GetUnsubmittedUnidentifiedEvents();
        assertEquals("Update list should return 4 of the 6 test records", 4, updatedLogs.size());
        for (EmployeeLogEldEvent updatedLog : updatedLogs) {
            assertEquals("EmployeeLogEldEvent/IsReviewed should be true after update", updatedLog.getIsReviewed(), true);
        }

    }

    private EmployeeLogEldEvent setEvent(String eventDateTime, UnidentifiedEldEventStatus unidentifiedEventStatus, boolean isReviewed){
        TimeZone tz = TimeZone.getTimeZone("UTC");
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

        EmployeeLogEldEvent event = new EmployeeLogEldEvent();
        event.setLogKey(10);
        event.setIsManuallyEditedByKMBUser(false);
        event.setIsEventDateTimeValidated(true);
        event.setEventDateTime( DateUtility.getDateTimeFromString(eventDateTime, format, tz));
        event.setEventType(Enums.EmployeeLogEldEventType.DutyStatusChange);
        event.setEventCode(3);
        event.setEventRecordStatus(1);
        event.setUnidentifiedEventStatus(unidentifiedEventStatus);
        event.setIsReviewed(isReviewed);

        event.setEventSequenceIDNumber(1);
        return event;
    }


}