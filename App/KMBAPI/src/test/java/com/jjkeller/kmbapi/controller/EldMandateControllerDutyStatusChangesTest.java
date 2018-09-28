package com.jjkeller.kmbapi.controller;

import android.provider.Settings;

import com.jjkeller.kmbapi.common.ITimeKeeper;
import com.jjkeller.kmbapi.common.TestBase;
import com.jjkeller.kmbapi.common.TimeKeeper;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.configuration.LoginCredentials;
import com.jjkeller.kmbapi.configuration.UserState;
import com.jjkeller.kmbapi.controller.dataaccess.EmployeeLogFacade;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.controller.utility.EmployeeLogUtilities;
import com.jjkeller.kmbapi.employeelogeldevents.Enums;
import com.jjkeller.kmbapi.enums.DriverTypeEnum;
import com.jjkeller.kmbapi.enums.DutyStatusEnum;
import com.jjkeller.kmbapi.enums.RuleSetTypeEnum;
import com.jjkeller.kmbapi.enums.TimeZoneEnum;
import com.jjkeller.kmbapi.featuretoggle.FeatureToggleService;
import com.jjkeller.kmbapi.proxydata.CompanyConfigSettings;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;
import com.jjkeller.kmbapi.proxydata.Location;

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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(application = GlobalState.class,
        manifest = "/build/intermediates/bundles/encompass/debug/AndroidManifest.xml"
)
public class EldMandateControllerDutyStatusChangesTest extends TestBase {

    protected GlobalState app;
    protected FeatureToggleService ftService;
    protected EmployeeLogFacade employeeLogFacade;

    protected EmployeeLogEldMandateController employeeLogEldMandateController;
    protected EmployeeLog employeeLog;


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
        settings.setDmoCompanyName("JesseCorp");
        app.setCompanyConfigSettings(app, settings);

        LoginCredentials creds = mock(LoginCredentials.class);
        when(creds.getEmployeeId()).thenReturn("00000000-0000-0000-0000-000000000000");
        when(user.getCredentials()).thenReturn(creds);

        ArrayList<User> userList = new ArrayList<User>();
        userList.add(user);
        app.setLoggedInUserList(userList);

        app.setCurrentUser(user);

        employeeLog = EmployeeLogUtilities.CreateNewLog(app, user, new Date() , DutyStatusEnum.valueOfDMOEnum(DutyStatusEnum.DmoEnum_OffDuty), null);
        app.setCurrentEmployeeLog(employeeLog);

        employeeLogEldMandateController = ControllerFactory.getInstance().getEmployeeLogEldMandateController();
        employeeLogFacade = new EmployeeLogFacade(app, user);

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

    private DutyStatusEnum OFF_DUTY = new DutyStatusEnum(DutyStatusEnum.OFFDUTY);
    private DutyStatusEnum DRIVING = new DutyStatusEnum(DutyStatusEnum.DRIVING);
    private DutyStatusEnum SLEEPER = new DutyStatusEnum(DutyStatusEnum.SLEEPER);

    private RuleSetTypeEnum ruleType = new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR);

    private void addDutyStatyChange(Date time, DutyStatusEnum status, boolean manual){
        try {
            employeeLogEldMandateController.CreateDutyStatusChangedEvent(employeeLog,time, status, new Location(), false, ruleType, null, null , manual, null, null);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }


    /**
     * Covers scenario when a
     * 1. driver starts driving
     * 2. stops driving
     * 3. manually changes duty status before continue driving popup is displayed
     *
     * also covers
     * 1. driver starts driving
     * 2. stops driving
     * 3. manually changes duty status after selecting no from popup.
     *
     * both sencarios are covered because selecting no on the continue driving prompt just routes you to the manual duty status update page.
     */
    @Test
    public void stopDrivingAndManuallyChangeStatus() {
       DateTime nowWithMillis =  TimeKeeper.getInstance().getCurrentDateTime();
       Date now = nowWithMillis.withMillisOfSecond(0).toDate();

       Date oneHour = DateUtility.AddHours(now, 1);
       Date twoHours = DateUtility.AddHours(now, 2);

       Date stopMoveTime = DateUtility.AddMinutes(twoHours, -5);

       addDutyStatyChange(now, OFF_DUTY, true);
       addDutyStatyChange(oneHour, DRIVING, false);
       GlobalState.getInstance().setPotentialDrivingStopTimestamp(stopMoveTime);

       addDutyStatyChange(twoHours, SLEEPER, true);

       EmployeeLog updatedLog = employeeLogFacade.GetLocalLogByDate(now);

       EmployeeLogEldEvent[] savedEvents = updatedLog.getEldEventList().getEldEventList();
       //assertEquals("unexpected number of events", 5, savedEvents.length);


        EmployeeLogEldEvent event0 = savedEvents[0];
        assertEquals("time of event0 is incorrect", employeeLog.getLogDate().getTime(), event0.getEventDateTime().getTime());

        EmployeeLogEldEvent event1 = savedEvents[1];
        EmployeeLogEldEvent event2 = savedEvents[2];
        //EmployeeLogEldEvent event3 = savedEvents[3];
        EmployeeLogEldEvent event4 = savedEvents[3];


        assertEquals("time of event1 is incorrect", now.getTime(), event1.getEventDateTime().getTime());
        assertEquals("duty status of event1 is incorrect", DutyStatusEnum.OFFDUTY, event1.getDutyStatusEnum().getValue());
        assertEquals("record origin of event1 is incorrect", Enums.EmployeeLogEldEventRecordOrigin.EditedEnteredByDriver, event1.getEventRecordOrigin().intValue());

        assertEquals("time of event2 is incorrect", oneHour.getTime(), event2.getEventDateTime().getTime());
        assertEquals("duty status of event2 is incorrect", DutyStatusEnum.DRIVING, event2.getDutyStatusEnum().getValue());
        assertEquals("record origin of event2 is incorrect", Enums.EmployeeLogEldEventRecordOrigin.AutomaticallyRecorded, event2.getEventRecordOrigin().intValue());

        //assertEquals("time of event3 is incorrect", stopMoveTime.getTime(), event3.getEventDateTime().getTime());
        //assertEquals("duty status of event3 is incorrect", DutyStatusEnum.DRIVING, event3.getDutyStatusEnum().getValue());
        //assertEquals("record origin of event3 is incorrect", Enums.EmployeeLogEldEventRecordOrigin.EditedEnteredByDriver, event3.getEventRecordOrigin().intValue());


        assertEquals("time of event4 is incorrect", stopMoveTime.getTime(), event4.getEventDateTime().getTime());
        assertEquals("duty status of event4 is incorrect", DutyStatusEnum.SLEEPER, event4.getDutyStatusEnum().getValue());
        assertEquals("record origin of event4 is incorrect", Enums.EmployeeLogEldEventRecordOrigin.EditedEnteredByDriver, event4.getEventRecordOrigin().intValue());

    }


    /**
     * Covers scenario when a
     * 1. driver starts driving
     * 2. stops driving
     * 3. driver is prompted and chooses to continue driving
     */
    @Test
    public void stopDrivingAndElectToStayDriving() {

        GlobalState.getInstance().setCurrentDriversLog(employeeLog);

        DateTime nowWithMillis =  TimeKeeper.getInstance().getCurrentDateTime();
        Date now = nowWithMillis.withMillisOfSecond(0).toDate();

        Date oneHour = DateUtility.AddHours(now, 1);
        Date twoHours = DateUtility.AddHours(now, 2);

        Date stopMoveTime = DateUtility.AddMinutes(twoHours, -5);

        addDutyStatyChange(now, OFF_DUTY, true);
        addDutyStatyChange(oneHour, DRIVING, false);
        GlobalState.getInstance().setPotentialDrivingStopTimestamp(stopMoveTime);

        //will get get executed when driver chooses to continue driving
        employeeLogEldMandateController.driverElectedToContinueDriving();

        EmployeeLog updatedLog = employeeLogFacade.GetLocalLogByDate(now);

        EmployeeLogEldEvent[] savedEvents = updatedLog.getEldEventList().getEldEventList();
        assertEquals("unexpected number of events", 4, savedEvents.length);


        EmployeeLogEldEvent event0 = savedEvents[0];
        assertEquals("time of event0 is incorrect", employeeLog.getLogDate().getTime(), event0.getEventDateTime().getTime());

        EmployeeLogEldEvent event1 = savedEvents[1];
        EmployeeLogEldEvent event2 = savedEvents[2];
        EmployeeLogEldEvent event3 = savedEvents[3];

        assertEquals("time of event1 is incorrect", now.getTime(), event1.getEventDateTime().getTime());
        assertEquals("duty status of event1 is incorrect", DutyStatusEnum.OFFDUTY, event1.getDutyStatusEnum().getValue());
        assertEquals("record origin of event1 is incorrect", Enums.EmployeeLogEldEventRecordOrigin.EditedEnteredByDriver, event1.getEventRecordOrigin().intValue());

        assertEquals("time of event2 is incorrect", oneHour.getTime(), event2.getEventDateTime().getTime());
        assertEquals("duty status of event2 is incorrect", DutyStatusEnum.DRIVING, event2.getDutyStatusEnum().getValue());
        assertEquals("record origin of event2 is incorrect", Enums.EmployeeLogEldEventRecordOrigin.AutomaticallyRecorded, event2.getEventRecordOrigin().intValue());

        assertEquals("time of event3 is incorrect", stopMoveTime.getTime(), event3.getEventDateTime().getTime());
        assertEquals("duty status of event3 is incorrect", DutyStatusEnum.DRIVING, event3.getDutyStatusEnum().getValue());
        assertEquals("record origin of event3 is incorrect", Enums.EmployeeLogEldEventRecordOrigin.EditedEnteredByDriver, event3.getEventRecordOrigin().intValue());

    }




    /**
     * Covers scenario when a
     * 1. driver starts driving
     * 2. stops driving
     * 3. driver is prompted to decide if they want to continue driving and ignores the prompt
     */
    @Test
    public void stopDrivingAndIngorePrompt() {
        DateTime nowWithMillis =  TimeKeeper.getInstance().getCurrentDateTime();
        Date now = nowWithMillis.withMillisOfSecond(0).toDate();

        Date oneHour = DateUtility.AddHours(now, 1);
        Date twoHours = DateUtility.AddHours(now, 2);

        Date stopMoveTime = DateUtility.AddMinutes(twoHours, -5);

        addDutyStatyChange(now, OFF_DUTY, true);
        addDutyStatyChange(oneHour, DRIVING, false);
        GlobalState.getInstance().setPotentialDrivingStopTimestamp(stopMoveTime);

        addDutyStatyChange(stopMoveTime, OFF_DUTY, false);


        EmployeeLog updatedLog = employeeLogFacade.GetLocalLogByDate(now);

        EmployeeLogEldEvent[] savedEvents = updatedLog.getEldEventList().getEldEventList();
        assertEquals("unexpected number of events", 4, savedEvents.length);


        EmployeeLogEldEvent event0 = savedEvents[0];
        assertEquals("time of event0 is incorrect", employeeLog.getLogDate().getTime(), event0.getEventDateTime().getTime());

        EmployeeLogEldEvent event1 = savedEvents[1];
        EmployeeLogEldEvent event2 = savedEvents[2];
        EmployeeLogEldEvent event3 = savedEvents[3];

        assertEquals("time of event1 is incorrect", now.getTime(), event1.getEventDateTime().getTime());
        assertEquals("duty status of event1 is incorrect", DutyStatusEnum.OFFDUTY, event1.getDutyStatusEnum().getValue());
        assertEquals("record origin of event1 is incorrect", Enums.EmployeeLogEldEventRecordOrigin.EditedEnteredByDriver, event1.getEventRecordOrigin().intValue());

        assertEquals("time of event2 is incorrect", oneHour.getTime(), event2.getEventDateTime().getTime());
        assertEquals("duty status of event2 is incorrect", DutyStatusEnum.DRIVING, event2.getDutyStatusEnum().getValue());
        assertEquals("record origin of event2 is incorrect", Enums.EmployeeLogEldEventRecordOrigin.AutomaticallyRecorded, event2.getEventRecordOrigin().intValue());

        assertEquals("time of event3 is incorrect", stopMoveTime.getTime(), event3.getEventDateTime().getTime());
        assertEquals("duty status of event3 is incorrect", DutyStatusEnum.OFFDUTY, event3.getDutyStatusEnum().getValue());
        assertEquals("record origin of event3 is incorrect", Enums.EmployeeLogEldEventRecordOrigin.AutomaticallyRecorded, event3.getEventRecordOrigin().intValue());

    }
}