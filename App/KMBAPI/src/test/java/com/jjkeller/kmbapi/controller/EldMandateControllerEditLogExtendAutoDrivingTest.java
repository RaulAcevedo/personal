package com.jjkeller.kmbapi.controller;

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
import org.junit.Ignore;
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

public class EldMandateControllerEditLogExtendAutoDrivingTest extends TestBase {

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

    private void addDutyStatusChange(Date time, DutyStatusEnum status, boolean manual){
        try {
            employeeLogEldMandateController.CreateDutyStatusChangedEvent(employeeLog,time, status, new Location(), false, ruleType, null, null , manual, null, null);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }



    /**
     * sets up log with the following events:
     * @param driveStart
     * @param stopMoveTime
     */
    public void createTestLogWithAutomaticDriveTime(Date offDuty, Date driveStart, Date stopMoveTime, Date manualDutyStatusChange){
        addDutyStatusChange(offDuty, OFF_DUTY, true);
        addDutyStatusChange(driveStart, DRIVING, false);
        GlobalState.getInstance().setPotentialDrivingStopTimestamp(stopMoveTime);
        addDutyStatusChange(manualDutyStatusChange, SLEEPER, true);
    }

    public EmployeeLogEldEvent getAutomaticDriveEvent(){
        EmployeeLogEldEvent driveEvent = null;
        EmployeeLog log = employeeLogFacade.GetLocalLogByDate(TimeKeeper.getInstance().getCurrentDateTime().toDate());
        for(EmployeeLogEldEvent event : log.getEldEventList().getEldEventList()){
            if(event.getEventRecordOrigin() == Enums.EmployeeLogEldEventRecordOrigin.AutomaticallyRecorded && event.getDutyStatusEnum().toDMOEnum().equals(DutyStatusEnum.DmoEnum_Driving)) {
                driveEvent = event;
                break;
            }
        }
        return driveEvent;
    }

    /**
     * Example:
     * Before Edit:
     *  Off Duty - 00:00
     *  Drive Auto - 10:00
     *  Drive Manual - 11:00
     *  Sleeper - 11:05
     *
     *  Edited to:
     *  Off Duty - 00:00
     *  Drive Auto - 10:00
     *  Drive Manual (extended)- 11:00
     *  Drive Manual (adjusted) - 11:04
     *  Sleeper - 11:05
     *
     *  inactive record for DriveManual 11:00
     */
    @Test
    public void extendAutomaticDrivePeriodEnd() {
        Date offDutyTime =  DateUtility.AddMinutes(employeeLog.getLogDate(), 5);
        Date driveStart = TimeKeeper.getInstance().getCurrentDateTime().withMillisOfSecond(0).toDate();
        Date moveEnd = DateUtility.AddHours(driveStart, 1);
        Date manualDutyStatusChange = DateUtility.AddMinutes(moveEnd, 5);

        createTestLogWithAutomaticDriveTime(offDutyTime, driveStart, moveEnd, manualDutyStatusChange);
        EmployeeLogEldEvent autoDriveEvent = getAutomaticDriveEvent();
        Date editedMoveEndTime = DateUtility.AddMinutes(moveEnd, 4);

        EmployeeLog uneditedLog = employeeLogFacade.GetLocalLogByDate(TimeKeeper.getInstance().getCurrentDateTime().toDate());
        EmployeeLogEldEvent[] originalEvents = uneditedLog.getEldEventList().getEldEventList();
        assertEquals("unexpected number of events", 4, originalEvents.length);

        try {
            employeeLogEldMandateController.saveEldEvent(autoDriveEvent,editedMoveEndTime);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }

        EmployeeLog editedLog = employeeLogFacade.GetLocalLogByDate(TimeKeeper.getInstance().getCurrentDateTime().toDate());
        EmployeeLogEldEvent[] savedEvents = editedLog.getEldEventList().getEldEventList();
        assertEquals("unexpected number of events", 6, savedEvents.length);

        EmployeeLogEldEvent event0 = savedEvents[0];
        assertEquals("time of event0 is incorrect", employeeLog.getLogDate().getTime(), event0.getEventDateTime().getTime());

        EmployeeLogEldEvent event1 = savedEvents[1];
        EmployeeLogEldEvent event2 = savedEvents[2];
        EmployeeLogEldEvent event3 = savedEvents[3];
        EmployeeLogEldEvent event4 = savedEvents[4];
        //EmployeeLogEldEvent event5 = savedEvents[5];
        EmployeeLogEldEvent event6 = savedEvents[5];


        assertEquals("time of event1 is incorrect", offDutyTime.getTime(), event1.getEventDateTime().getTime());
        assertEquals("duty status of event1 is incorrect", DutyStatusEnum.OFFDUTY, event1.getDutyStatusEnum().getValue());
        assertEquals("record origin of event1 is incorrect", Enums.EmployeeLogEldEventRecordOrigin.EditedEnteredByDriver, event1.getEventRecordOrigin().intValue());
        assertEquals("record status of event1 is incorrect", Enums.EmployeeLogEldEventRecordStatus.Active.getValue(), event1.getEventRecordStatus().intValue());

        //unchanged automatic driving event
        assertEquals("time of event2 is incorrect", driveStart.getTime(), event2.getEventDateTime().getTime());
        assertEquals("duty status of event2 is incorrect", DutyStatusEnum.DRIVING, event2.getDutyStatusEnum().getValue());
        assertEquals("record origin of event2 is incorrect", Enums.EmployeeLogEldEventRecordOrigin.AutomaticallyRecorded, event2.getEventRecordOrigin().intValue());
        assertEquals("record status of event2 is incorrect", Enums.EmployeeLogEldEventRecordStatus.Active.getValue(), event2.getEventRecordStatus().intValue());

        //new extened driving event
        assertEquals("time of event3 is incorrect", moveEnd.getTime(), event3.getEventDateTime().getTime());
        //assertEquals("duty status of event3 is incorrect", DutyStatusEnum.DRIVING, event3.getDutyStatusEnum().getValue());
        assertEquals("record origin of event3 is incorrect", Enums.EmployeeLogEldEventRecordOrigin.EditedEnteredByDriver, event3.getEventRecordOrigin().intValue());
        //assertEquals("record status of event3 is incorrect", Enums.EmployeeLogEldEventRecordStatus.Active.getValue(), event3.getEventRecordStatus().intValue());

        //inactive original manual drive event
        assertEquals("time of event4 is incorrect", moveEnd.getTime(), event4.getEventDateTime().getTime());
        assertEquals("duty status of event4 is incorrect", DutyStatusEnum.DRIVING, event4.getDutyStatusEnum().getValue());
        assertEquals("record origin of event4 is incorrect", Enums.EmployeeLogEldEventRecordOrigin.EditedEnteredByDriver, event4.getEventRecordOrigin().intValue());
        //assertEquals("record status of event4 is incorrect", Enums.EmployeeLogEldEventRecordStatus.InactiveChanged.getValue(), event4.getEventRecordStatus().intValue());

        //adjusted manual driving event
        //assertEquals("time of event5 is incorrect", editedMoveEndTime.getTime(), event5.getEventDateTime().getTime());
        //assertEquals("duty status of event5 is incorrect", DutyStatusEnum.DRIVING, event5.getDutyStatusEnum().getValue());
        //assertEquals("record origin of event5 is incorrect", Enums.EmployeeLogEldEventRecordOrigin.EditedEnteredByDriver, event5.getEventRecordOrigin().intValue());
        //assertEquals("record status of event5 is incorrect", Enums.EmployeeLogEldEventRecordStatus.Active.getValue(), event5.getEventRecordStatus().intValue());

        //unmodified sleeper time
        //assertEquals("time of event6 is incorrect", manualDutyStatusChange.getTime(), event6.getEventDateTime().getTime());
        assertEquals("duty status of event6 is incorrect", DutyStatusEnum.SLEEPER, event6.getDutyStatusEnum().getValue());
        assertEquals("record origin of event6 is incorrect", Enums.EmployeeLogEldEventRecordOrigin.EditedEnteredByDriver, event6.getEventRecordOrigin().intValue());
        assertEquals("record status of event6 is incorrect", Enums.EmployeeLogEldEventRecordStatus.Active.getValue(), event6.getEventRecordStatus().intValue());

    }

    /**
     * Example:
     * Before Edit:
     *  Off Duty - 00:05
     *  Drive Auto - 10:00
     *  Drive Manual - 11:00
     *  Sleeper - 11:05
     *
     *  Edited to:
     *  Off Duty - 00:05
     *  Drive Manual (extended)- 10:50
     *  Drive Auto - 10:00
     *  Drive Manual - 11:00
     *  Sleeper - 11:05
     *
     */
    @Test
    public void extendAutomaticDriveStartTime() {
        Date offDutyTime =  DateUtility.AddMinutes(employeeLog.getLogDate(), 5);
        Date driveStart = TimeKeeper.getInstance().getCurrentDateTime().withMillisOfSecond(0).toDate();
        Date moveEnd = DateUtility.AddHours(driveStart, 1);
        Date manualDutyStatusChange = DateUtility.AddMinutes(moveEnd, 5);

        createTestLogWithAutomaticDriveTime(offDutyTime, driveStart, moveEnd, manualDutyStatusChange);
        EmployeeLogEldEvent autoDriveEvent = getAutomaticDriveEvent();
        Date editedMoveStartTime = DateUtility.AddMinutes(driveStart, -10);

        autoDriveEvent.setEventDateTime(editedMoveStartTime);


        EmployeeLog uneditedLog = employeeLogFacade.GetLocalLogByDate(TimeKeeper.getInstance().getCurrentDateTime().toDate());
        EmployeeLogEldEvent[] originalEvents = uneditedLog.getEldEventList().getEldEventList();
        assertEquals("unexpected number of events", 4, originalEvents.length);

        try {
            employeeLogEldMandateController.saveEldEvent(autoDriveEvent, moveEnd);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }

        EmployeeLog editedLog = employeeLogFacade.GetLocalLogByDate(TimeKeeper.getInstance().getCurrentDateTime().toDate());
        EmployeeLogEldEvent[] savedEvents = editedLog.getEldEventList().getEldEventList();
        assertEquals("unexpected number of events", 5, savedEvents.length);

        EmployeeLogEldEvent event0 = savedEvents[0];
        assertEquals("time of event0 is incorrect", employeeLog.getLogDate().getTime(), event0.getEventDateTime().getTime());

        EmployeeLogEldEvent event1 = savedEvents[1];
        EmployeeLogEldEvent event2 = savedEvents[2];
        EmployeeLogEldEvent event3 = savedEvents[3];
        //EmployeeLogEldEvent event4 = savedEvents[4];
        EmployeeLogEldEvent event5 = savedEvents[4];

        assertEquals("time of event1 is incorrect", offDutyTime.getTime(), event1.getEventDateTime().getTime());
        assertEquals("duty status of event1 is incorrect", DutyStatusEnum.OFFDUTY, event1.getDutyStatusEnum().getValue());
        assertEquals("record origin of event1 is incorrect", Enums.EmployeeLogEldEventRecordOrigin.EditedEnteredByDriver, event1.getEventRecordOrigin().intValue());
        assertEquals("record status of event1 is incorrect", Enums.EmployeeLogEldEventRecordStatus.Active.getValue(), event1.getEventRecordStatus().intValue());


        //new extened driving event
        assertEquals("time of event2 is incorrect", editedMoveStartTime.getTime(), event2.getEventDateTime().getTime());
        assertEquals("duty status of event2 is incorrect", DutyStatusEnum.DRIVING, event2.getDutyStatusEnum().getValue());
        assertEquals("record origin of event2 is incorrect", Enums.EmployeeLogEldEventRecordOrigin.EditedEnteredByDriver, event2.getEventRecordOrigin().intValue());
        assertEquals("record status of event2 is incorrect", Enums.EmployeeLogEldEventRecordStatus.Active.getValue(), event2.getEventRecordStatus().intValue());

        //unchanged automatic driving event
        assertEquals("time of event3 is incorrect", driveStart.getTime(), event3.getEventDateTime().getTime());
        assertEquals("duty status of event3 is incorrect", DutyStatusEnum.DRIVING, event3.getDutyStatusEnum().getValue());
        assertEquals("record origin of event3 is incorrect", Enums.EmployeeLogEldEventRecordOrigin.AutomaticallyRecorded, event3.getEventRecordOrigin().intValue());
        assertEquals("record status of event3 is incorrect", Enums.EmployeeLogEldEventRecordStatus.Active.getValue(), event3.getEventRecordStatus().intValue());

        //original manual drive event unchanged
        //assertEquals("time of event4 is incorrect", moveEnd.getTime(), event4.getEventDateTime().getTime());
        //assertEquals("duty status of event4 is incorrect", DutyStatusEnum.DRIVING, event4.getDutyStatusEnum().getValue());
        //assertEquals("record origin of event4 is incorrect", Enums.EmployeeLogEldEventRecordOrigin.EditedEnteredByDriver, event4.getEventRecordOrigin().intValue());
        //assertEquals("record status of event4 is incorrect", Enums.EmployeeLogEldEventRecordStatus.Active.getValue(), event4.getEventRecordStatus().intValue());

        //unmodified sleeper time
        //assertEquals("time of event5 is incorrect", manualDutyStatusChange.getTime(), event5.getEventDateTime().getTime());
        assertEquals("duty status of event5 is incorrect", DutyStatusEnum.SLEEPER, event5.getDutyStatusEnum().getValue());
        assertEquals("record origin of event5 is incorrect", Enums.EmployeeLogEldEventRecordOrigin.EditedEnteredByDriver, event5.getEventRecordOrigin().intValue());
        assertEquals("record status of event5 is incorrect", Enums.EmployeeLogEldEventRecordStatus.Active.getValue(), event5.getEventRecordStatus().intValue());

    }

    /**
     * Example:
     * Before Edit:
     *  Off Duty - 00:05
     *  Drive Auto - 10:00
     *  Drive Manual - 11:00
     *  Sleeper - 11:05
     *
     *  Edited to:
     *  Off Duty - 00:05
     *  Drive Manual (extended)- 10:50
     *  Drive Auto - 10:00
     *  Drive Manual (extended)- 11:00
     *  Drive Manual (adjusted) - 11:04
     *  Sleeper - 11:05
     *
     *  inactive record for DriveManual 11:00
     */

    @Test
    public void extendAutomaticDriveStartAndEndTime() {
        Date offDutyTime =  DateUtility.AddMinutes(employeeLog.getLogDate(), 5);
        Date driveStart = TimeKeeper.getInstance().getCurrentDateTime().withMillisOfSecond(0).toDate();
        Date moveEnd = DateUtility.AddHours(driveStart, 1);
        Date manualDutyStatusChange = DateUtility.AddMinutes(moveEnd, 5);

        createTestLogWithAutomaticDriveTime(offDutyTime, driveStart, moveEnd, manualDutyStatusChange);
        EmployeeLogEldEvent autoDriveEvent = getAutomaticDriveEvent();
        Date editedMoveStartTime = DateUtility.AddMinutes(driveStart, -10);

        autoDriveEvent.setEventDateTime(editedMoveStartTime);

        Date editedMoveEndTime = DateUtility.AddMinutes(moveEnd, 4);

        EmployeeLog uneditedLog = employeeLogFacade.GetLocalLogByDate(TimeKeeper.getInstance().getCurrentDateTime().toDate());
        EmployeeLogEldEvent[] originalEvents = uneditedLog.getEldEventList().getEldEventList();
        assertEquals("unexpected number of events", 4, originalEvents.length);

        try {
            employeeLogEldMandateController.saveEldEvent(autoDriveEvent, editedMoveEndTime);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }

        EmployeeLog editedLog = employeeLogFacade.GetLocalLogByDate(TimeKeeper.getInstance().getCurrentDateTime().toDate());
        EmployeeLogEldEvent[] savedEvents = editedLog.getEldEventList().getEldEventList();
        assertEquals("unexpected number of events", 7, savedEvents.length);

        EmployeeLogEldEvent event0 = savedEvents[0];
        assertEquals("time of event0 is incorrect", employeeLog.getLogDate().getTime(), event0.getEventDateTime().getTime());

        EmployeeLogEldEvent event1 = savedEvents[1];
        EmployeeLogEldEvent event2 = savedEvents[2];
        EmployeeLogEldEvent event3 = savedEvents[3];
        EmployeeLogEldEvent event4 = savedEvents[4];
        EmployeeLogEldEvent event5 = savedEvents[5];
        //EmployeeLogEldEvent event6 = savedEvents[6];
        EmployeeLogEldEvent event7 = savedEvents[6];

        assertEquals("time of event1 is incorrect", offDutyTime.getTime(), event1.getEventDateTime().getTime());
        assertEquals("duty status of event1 is incorrect", DutyStatusEnum.OFFDUTY, event1.getDutyStatusEnum().getValue());
        assertEquals("record origin of event1 is incorrect", Enums.EmployeeLogEldEventRecordOrigin.EditedEnteredByDriver, event1.getEventRecordOrigin().intValue());
        assertEquals("record status of event1 is incorrect", Enums.EmployeeLogEldEventRecordStatus.Active.getValue(), event1.getEventRecordStatus().intValue());


        //new extened driving event
        assertEquals("time of event2 is incorrect", editedMoveStartTime.getTime(), event2.getEventDateTime().getTime());
        assertEquals("duty status of event2 is incorrect", DutyStatusEnum.DRIVING, event2.getDutyStatusEnum().getValue());
        assertEquals("record origin of event2 is incorrect", Enums.EmployeeLogEldEventRecordOrigin.EditedEnteredByDriver, event2.getEventRecordOrigin().intValue());
        assertEquals("record status of event2 is incorrect", Enums.EmployeeLogEldEventRecordStatus.Active.getValue(), event2.getEventRecordStatus().intValue());

        //unchanged automatic driving event
        assertEquals("time of event3 is incorrect", driveStart.getTime(), event3.getEventDateTime().getTime());
        assertEquals("duty status of event3 is incorrect", DutyStatusEnum.DRIVING, event3.getDutyStatusEnum().getValue());
        assertEquals("record origin of event3 is incorrect", Enums.EmployeeLogEldEventRecordOrigin.AutomaticallyRecorded, event3.getEventRecordOrigin().intValue());
        assertEquals("record status of event3 is incorrect", Enums.EmployeeLogEldEventRecordStatus.Active.getValue(), event3.getEventRecordStatus().intValue());

        //new extened driving ending event
        assertEquals("time of event4 is incorrect", moveEnd.getTime(), event4.getEventDateTime().getTime());
        //assertEquals("duty status of event4 is incorrect", DutyStatusEnum.DRIVING, event4.getDutyStatusEnum().getValue());
        assertEquals("record origin of event4 is incorrect", Enums.EmployeeLogEldEventRecordOrigin.EditedEnteredByDriver, event4.getEventRecordOrigin().intValue());
        //assertEquals("record status of event4 is incorrect", Enums.EmployeeLogEldEventRecordStatus.Active.getValue(), event4.getEventRecordStatus().intValue());

        //inactive original manual drive event
        assertEquals("time of event5 is incorrect", moveEnd.getTime(), event5.getEventDateTime().getTime());
        assertEquals("duty status of event5 is incorrect", DutyStatusEnum.DRIVING, event5.getDutyStatusEnum().getValue());
        assertEquals("record origin of event5 is incorrect", Enums.EmployeeLogEldEventRecordOrigin.EditedEnteredByDriver, event5.getEventRecordOrigin().intValue());
        //assertEquals("record status of event5 is incorrect", Enums.EmployeeLogEldEventRecordStatus.InactiveChanged.getValue(), event5.getEventRecordStatus().intValue());

        //adjusted manual driving event
        //assertEquals("time of event6 is incorrect", editedMoveEndTime.getTime(), event6.getEventDateTime().getTime());
        //assertEquals("duty status of event6 is incorrect", DutyStatusEnum.DRIVING, event6.getDutyStatusEnum().getValue());
        //assertEquals("record origin of event6 is incorrect", Enums.EmployeeLogEldEventRecordOrigin.EditedEnteredByDriver, event6.getEventRecordOrigin().intValue());
        //assertEquals("record status of event6 is incorrect", Enums.EmployeeLogEldEventRecordStatus.Active.getValue(), event6.getEventRecordStatus().intValue());

        //unmodified sleeper time
        //assertEquals("time of event7 is incorrect", manualDutyStatusChange.getTime(), event7.getEventDateTime().getTime());
        assertEquals("duty status of event7 is incorrect", DutyStatusEnum.SLEEPER, event7.getDutyStatusEnum().getValue());
        assertEquals("record origin of event7 is incorrect", Enums.EmployeeLogEldEventRecordOrigin.EditedEnteredByDriver, event7.getEventRecordOrigin().intValue());
        assertEquals("record status of event7 is incorrect", Enums.EmployeeLogEldEventRecordStatus.Active.getValue(), event7.getEventRecordStatus().intValue());

    }

    @Test
    public void testIsEditingAutomaticDrivingEvent_reduceAutoDrivingTime() {
        EmployeeLogEldMandateController.InvalidateAutomaticDriveTimeEnum result;

        Date offDutyTime =  DateUtility.AddMinutes(employeeLog.getLogDate(), 5);
        Date driveStart =  new Date(2018,07,06, 10, 00, 00);
                        // TimeKeeper.getInstance().getCurrentDateTime().withMillisOfSecond(0).toDate();
        Date moveEnd = DateUtility.AddHours(driveStart, 1);
        Date manualDutyStatusChange = DateUtility.AddMinutes(moveEnd, 5);

        createTestLogWithAutomaticDriveTime(offDutyTime, driveStart, moveEnd, manualDutyStatusChange);
        EmployeeLogEldEvent autoDriveEvent = getAutomaticDriveEvent();
        autoDriveEvent.setEobrSerialNumber("sss");

        //Date editedMoveEndTime = DateUtility.AddMinutes(moveEnd, 4);
        Date editedMoveStartTime = DateUtility.AddMinutes(driveStart, 10);

        EmployeeLog uneditedLog = employeeLogFacade.GetLocalLogByDate(TimeKeeper.getInstance().getCurrentDateTime().toDate());
        EmployeeLogEldEvent[] originalEvents = uneditedLog.getEldEventList().getEldEventList();
        assertEquals("unexpected number of events", 4, originalEvents.length);

        //--------------
        //Try editing the off duty event before automatic driving event -- change the time to later time, so automatic drive time is reduced.
        autoDriveEvent.setEventDateTime(editedMoveStartTime);

        assertEquals("Expected number of events", 4, originalEvents.length);

        try {
           result = employeeLogEldMandateController.willEditInvalidateAutomaticDriveTime(originalEvents, autoDriveEvent.getPrimaryKey(), editedMoveStartTime, moveEnd, "sss");
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }

        assertEquals("duty status of event0 is unchanged", EmployeeLogEldMandateController.InvalidateAutomaticDriveTimeEnum.NO_OVERLAP, result);

        EmployeeLog editedLog = employeeLogFacade.GetLocalLogByDate(TimeKeeper.getInstance().getCurrentDateTime().toDate());
        EmployeeLogEldEvent[] savedEvents = editedLog.getEldEventList().getEldEventList();
        assertEquals("Expected number of events", 4, savedEvents.length);

        EmployeeLogEldEvent event0 = savedEvents[0];
        EmployeeLogEldEvent event1 = savedEvents[1];
        EmployeeLogEldEvent event2 = savedEvents[2];
        EmployeeLogEldEvent event3 = savedEvents[3];

        assertEquals("duty status of event0 is unchanged", DutyStatusEnum.OFFDUTY, event0.getDutyStatusEnum().getValue());

        assertEquals("duty status of event1 is unchanged", DutyStatusEnum.OFFDUTY, event1.getDutyStatusEnum().getValue());
        assertEquals("record origin of event1 is unchanged", Enums.EmployeeLogEldEventRecordOrigin.EditedEnteredByDriver, event1.getEventRecordOrigin().intValue());
        assertEquals("record status of event1 is unchanged", Enums.EmployeeLogEldEventRecordStatus.Active.getValue(), event1.getEventRecordStatus().intValue());

        // unchanged automatic driving event - as we cannot reduce the automatic driving time
        assertEquals("duty status of event0 is unchanged", DutyStatusEnum.DRIVING, event2.getDutyStatusEnum().getValue());
        assertEquals("record origin of event3 is unchanged", Enums.EmployeeLogEldEventRecordOrigin.AutomaticallyRecorded, event2.getEventRecordOrigin().intValue());
        assertEquals("record status of event3 is unchanged", Enums.EmployeeLogEldEventRecordStatus.Active.getValue(), event2.getEventRecordStatus().intValue());

        assertEquals("duty status of event0 is unchanged", DutyStatusEnum.SLEEPER, event3.getDutyStatusEnum().getValue());
    }
}