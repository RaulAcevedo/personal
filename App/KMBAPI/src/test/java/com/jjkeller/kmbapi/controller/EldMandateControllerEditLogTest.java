package com.jjkeller.kmbapi.controller;

import com.jjkeller.kmbapi.common.ITimeKeeper;
import com.jjkeller.kmbapi.common.TestBase;
import com.jjkeller.kmbapi.common.TimeKeeper;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.configuration.LoginCredentials;
import com.jjkeller.kmbapi.configuration.UserState;
import com.jjkeller.kmbapi.controller.dataaccess.EmployeeLogFacade;
import com.jjkeller.kmbapi.controller.dataaccess.EmployeeLogWithProvisionsFacade;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
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
import com.jjkeller.kmbapi.proxydata.EmployeeLogWithProvisions;
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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(application = GlobalState.class,
        manifest = "/build/intermediates/bundles/encompass/debug/AndroidManifest.xml"
)
public class EldMandateControllerEditLogTest extends TestBase {

    protected GlobalState app;
    protected FeatureToggleService ftService;
    protected EmployeeLogFacade employeeLogFacade;

    protected EmployeeLogEldMandateController employeeLogEldMandateController;
    protected EmployeeLog employeeLog;
    protected LogEntryController logEntryController;

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

        employeeLog = EmployeeLogUtilities.CreateNewLog(app, user, new Date(), DutyStatusEnum.valueOfDMOEnum(DutyStatusEnum.DmoEnum_OffDuty), null);
        app.setCurrentEmployeeLog(employeeLog);

        employeeLogEldMandateController = ControllerFactory.getInstance().getEmployeeLogEldMandateController();
        logEntryController = ControllerFactory.getInstance().getLogEntryController();

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
    private DutyStatusEnum ON_DUTY = new DutyStatusEnum(DutyStatusEnum.ONDUTY);

    private RuleSetTypeEnum ruleType = new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR);

    private void addDutyStatusChange(Date time, DutyStatusEnum status, boolean manual) {
        try {
            employeeLogEldMandateController.CreateDutyStatusChangedEvent(employeeLog, time, status, new Location(), false, ruleType, null, null, manual, null, null);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    private void addChangeInDrivingIndication(Date time, String annotation, int eventCode, String logRemark) {
        try {
            employeeLogEldMandateController.CreateChangeInDriversIndicationEvent(employeeLog, time, new Location(), annotation, eventCode, logRemark);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    private void addHyrailNonReg(EmployeeLog empLog, Date time, String logRemark, int provisionType) {
        try {
            logEntryController.PerformManualStatusChange(time, ON_DUTY, "Arvada", logRemark);
            EmployeeLogEldEvent lastEventLog = EmployeeLogUtilities.GetLastEventInLog(empLog, Enums.EmployeeLogEldEventType.DutyStatusChange);
            EmployeeLogWithProvisionsFacade facade = new EmployeeLogWithProvisionsFacade(app);
            EmployeeLogWithProvisions provisions = new EmployeeLogWithProvisions();
            provisions.setStartLocation(new Location());
            provisions.setTractorNumber("324234");
            provisions.setProvisionTypeEnum(provisionType);
            provisions.setEmployeeLogEldEventId((int) lastEventLog.getPrimaryKey());
            facade.Save(provisions, empLog);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public void testYMorPCTrailerTractorShippingInfoChange(int eventCode) {
        Date offDutyTime = DateUtility.AddMinutes(employeeLog.getLogDate(), 5);
        Enums.SpecialDrivingCategory specialDrivingCategory;
        addDutyStatusChange(offDutyTime, OFF_DUTY, true);
        Date firstEventTime = DateUtility.AddMinutes(employeeLog.getLogDate(), 10);
        if (eventCode == EmployeeLogEldEventCode.ChangeInDriversIndication_YardMoves) {
            addDutyStatusChange(firstEventTime, ON_DUTY, true);
            addChangeInDrivingIndication(firstEventTime, "YM start", EmployeeLogEldEventCode.ChangeInDriversIndication_YardMoves, "Yard Move");
            specialDrivingCategory = Enums.SpecialDrivingCategory.YardMove;
        } else {
            addDutyStatusChange(firstEventTime, OFF_DUTY, true);
            addChangeInDrivingIndication(firstEventTime, "PC start", EmployeeLogEldEventCode.ChangeInDriversIndication_PersonalUse, "Personal Conveyance");
            specialDrivingCategory = Enums.SpecialDrivingCategory.PersonalConveyance;

        }
        EmployeeLog uneditedLog = employeeLogFacade.GetLocalLogByDate(TimeKeeper.getInstance().getCurrentDateTime().toDate());
        EmployeeLogEldEvent[] originalEvents = uneditedLog.getEldEventList().getEldEventList();
        EmployeeLogEldEvent ymEvent1 = originalEvents[2];
        EmployeeLogEldEvent ymEvent2 = originalEvents[3];
        assertEquals("unexpected number of events", 4, originalEvents.length);
        assertEquals("event code is supposed to be DutyStatus_OffDuty", originalEvents[0].getEventCode(), EmployeeLogEldEventCode.DutyStatus_OffDuty);
        assertEquals("event code is supposed to be DutyStatus_OffDuty", originalEvents[1].getEventCode(), EmployeeLogEldEventCode.DutyStatus_OffDuty);
        assertEquals("event type is supposed to be ChangeInDriversIndication", ymEvent1.getEventType(), Enums.EmployeeLogEldEventType.ChangeInDriversIndication);
        assertEquals("event type is supposed to be DutyStatusChange", ymEvent2.getEventType(), Enums.EmployeeLogEldEventType.DutyStatusChange);

        if (eventCode == EmployeeLogEldEventCode.ChangeInDriversIndication_YardMoves) {
            assertEquals("event code is supposed to be ChangeInDriversIndication_YardMoves", ymEvent1.getEventCode(), EmployeeLogEldEventCode.ChangeInDriversIndication_YardMoves);
            assertEquals("event code is supposed to be DutyStatus_OnDuty", ymEvent2.getEventCode(), EmployeeLogEldEventCode.DutyStatus_OnDuty);
        } else {
            assertEquals("event code is supposed to be ChangeInDriversIndication_PersonalUse", ymEvent1.getEventCode(), EmployeeLogEldEventCode.ChangeInDriversIndication_PersonalUse);
            assertEquals("event code is supposed to be DutyStatus_OffDuty", ymEvent2.getEventCode(), EmployeeLogEldEventCode.DutyStatus_OffDuty);
        }
        try {
            ymEvent2.setTractorNumber("000");
            ymEvent2.setTrailerNumber("111");
            ymEvent2.setShipmentInfo("napkins");
            employeeLogEldMandateController.saveEldEvent(ymEvent2, specialDrivingCategory, null, Enums.ActionInitiatingSaveEnum.EditLog);
            ymEvent2.setTractorNumber("3242");
            ymEvent2.setTrailerNumber("2333");
            ymEvent2.setShipmentInfo("metal objects");
            employeeLogEldMandateController.saveEldEvent(ymEvent2, specialDrivingCategory, null, Enums.ActionInitiatingSaveEnum.EditLog);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }

        EmployeeLog editedLog = employeeLogFacade.GetLocalLogByDate(TimeKeeper.getInstance().getCurrentDateTime().toDate());
        List<EmployeeLogEldEvent> savedEvents = Arrays.asList(editedLog.getEldEventList().getEldEventList());
        Collections.sort(savedEvents, new Comparator<EmployeeLogEldEvent>() {
            @Override
            public int compare(EmployeeLogEldEvent p1, EmployeeLogEldEvent p2) {
                return p1.getEventSequenceIDNumber() - p2.getEventSequenceIDNumber(); // Ascending
            }
        });
        assertEquals("unexpected number of events after edit", 8, savedEvents.size());

        EmployeeLogEldEvent yardToYard1 = savedEvents.get(4);
        EmployeeLogEldEvent yardToYard2 = savedEvents.get(5);
        EmployeeLogEldEvent yardToYard3 = savedEvents.get(6);
        EmployeeLogEldEvent yardToYard4 = savedEvents.get(7);
        if (eventCode == EmployeeLogEldEventCode.ChangeInDriversIndication_YardMoves) {
            assertEquals("event code of yardToYard1 is incorrect", EmployeeLogEldEventCode.DutyStatus_OnDuty, yardToYard1.getEventCode());
            assertEquals("event code of yardToYard2 is incorrect", EmployeeLogEldEventCode.ChangeInDriversIndication_YardMoves, yardToYard2.getEventCode());
            assertEquals("event code of yardToYard3 is incorrect", EmployeeLogEldEventCode.DutyStatus_OnDuty, yardToYard3.getEventCode());
            assertEquals("event code of yardToYard4 is incorrect", EmployeeLogEldEventCode.ChangeInDriversIndication_YardMoves, yardToYard4.getEventCode());

        } else {
            assertEquals("event code of yardToYard1 is incorrect", EmployeeLogEldEventCode.DutyStatus_OffDuty, yardToYard1.getEventCode());
            assertEquals("event code of yardToYard2 is incorrect", EmployeeLogEldEventCode.ChangeInDriversIndication_PersonalUse, yardToYard2.getEventCode());
            assertEquals("event code of yardToYard3 is incorrect", EmployeeLogEldEventCode.DutyStatus_OffDuty, yardToYard3.getEventCode());
            assertEquals("event code of yardToYard4 is incorrect", EmployeeLogEldEventCode.ChangeInDriversIndication_PersonalUse, yardToYard4.getEventCode());

        }
        assertEquals("event type of yardToYard1 is incorrect", Enums.EmployeeLogEldEventType.DutyStatusChange, yardToYard1.getEventType());
        int yardToYard1EventRecordStatus = yardToYard1.getEventRecordStatus();
        assertEquals("record status of yardToYard1 is incorrect", Enums.EmployeeLogEldEventRecordStatus.InactiveChanged.getValue(), yardToYard1EventRecordStatus);

        assertEquals("event type of yardToYard2 is incorrect", Enums.EmployeeLogEldEventType.ChangeInDriversIndication, yardToYard2.getEventType());
        int yardToYard2EventRecordStatus = yardToYard2.getEventRecordStatus();
        assertEquals("record status of yardToYard2 is incorrect", Enums.EmployeeLogEldEventRecordStatus.InactiveChanged.getValue(), yardToYard2EventRecordStatus);

        assertEquals("event type of yardToYard3 is incorrect", Enums.EmployeeLogEldEventType.DutyStatusChange, yardToYard3.getEventType());
        int yardToYard3EventRecordStatus = yardToYard3.getEventRecordStatus();
        assertEquals("record status of yardToYard3 is incorrect", Enums.EmployeeLogEldEventRecordStatus.Active.getValue(), yardToYard3EventRecordStatus);

        assertEquals("event type of yardToYard4 is incorrect", Enums.EmployeeLogEldEventType.ChangeInDriversIndication, yardToYard4.getEventType());
        int yardToYard4EventRecordStatus = yardToYard4.getEventRecordStatus();
        assertEquals("record status of yardToYard4 is incorrect", Enums.EmployeeLogEldEventRecordStatus.Active.getValue(), yardToYard4EventRecordStatus);
    }

    @Test
    public void testYMToYMEdit() {
        testYMorPCTrailerTractorShippingInfoChange(EmployeeLogEldEventCode.ChangeInDriversIndication_YardMoves);
    }

    @Test
    public void testPCToPCEdit() {
        testYMorPCTrailerTractorShippingInfoChange(EmployeeLogEldEventCode.ChangeInDriversIndication_PersonalUse);
    }

    @Test
    public void testYMToSleeperEdit() {
        testYMPCToRegularEdit(ON_DUTY, EmployeeLogEldEventCode.DutyStatus_Sleeper);
    }

    public void testYMPCToRegularEdit(DutyStatusEnum origEventDutyStatusEnum, int newEventCode) {
        Date offDutyTime = DateUtility.AddMinutes(employeeLog.getLogDate(), 5);
        addDutyStatusChange(offDutyTime, OFF_DUTY, true);
        Date origEventTime = DateUtility.AddMinutes(employeeLog.getLogDate(), 10);
        addDutyStatusChange(origEventTime, origEventDutyStatusEnum, true);
        int origSecondEventCode = EmployeeLogEldEventCode.ChangeInDriversIndication_YardMoves;
        int origFirstEventCode = EmployeeLogEldEventCode.DutyStatus_OnDuty;

        if (origEventDutyStatusEnum == OFF_DUTY) {
            origSecondEventCode = EmployeeLogEldEventCode.ChangeInDriversIndication_PersonalUse;
            origFirstEventCode = EmployeeLogEldEventCode.DutyStatus_OffDuty;
            addChangeInDrivingIndication(origEventTime, "PC start", origSecondEventCode, "Personal Conveyance");

        } else {
            addChangeInDrivingIndication(origEventTime, "YM start", origSecondEventCode, "Yard Move");
        }
        EmployeeLog uneditedLog = employeeLogFacade.GetLocalLogByDate(TimeKeeper.getInstance().getCurrentDateTime().toDate());
        EmployeeLogEldEvent[] originalEvents = uneditedLog.getEldEventList().getEldEventList();
        EmployeeLogEldEvent ymEvent1 = originalEvents[2];
        EmployeeLogEldEvent ymEvent2 = originalEvents[3];
        assertEquals("unexpected number of events", 4, originalEvents.length);
        assertEquals("event code is supposed to be DutyStatus_OffDuty", originalEvents[0].getEventCode(), EmployeeLogEldEventCode.DutyStatus_OffDuty);
        assertEquals("event code is supposed to be DutyStatus_OffDuty", originalEvents[1].getEventCode(), EmployeeLogEldEventCode.DutyStatus_OffDuty);
        assertEquals("event type is supposed to be ChangeInDriversIndication", ymEvent1.getEventType(), Enums.EmployeeLogEldEventType.ChangeInDriversIndication);
        assertEquals("event code is supposed to be ChangeInDriversIndication_YardMoves", ymEvent1.getEventCode(), origSecondEventCode);
        assertEquals("event type is supposed to be DutyStatusChange", ymEvent2.getEventType(), Enums.EmployeeLogEldEventType.DutyStatusChange);
        assertEquals("event code is supposed to be DutyStatus_OnDuty", ymEvent2.getEventCode(), origFirstEventCode);

        try {
            ymEvent2.setEventCode(newEventCode);
            employeeLogEldMandateController.saveEldEvent(ymEvent2, Enums.SpecialDrivingCategory.None, null, Enums.ActionInitiatingSaveEnum.EditLog);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }

        EmployeeLog editedLog = employeeLogFacade.GetLocalLogByDate(TimeKeeper.getInstance().getCurrentDateTime().toDate());
        List<EmployeeLogEldEvent> savedEvents = Arrays.asList(editedLog.getEldEventList().getEldEventList());
        Collections.sort(savedEvents, new Comparator<EmployeeLogEldEvent>() {
            @Override
            public int compare(EmployeeLogEldEvent p1, EmployeeLogEldEvent p2) {
                return p1.getEventSequenceIDNumber() - p2.getEventSequenceIDNumber(); // Ascending
            }
        });
        assertEquals("unexpected number of events after edit", 5, savedEvents.size());

        EmployeeLogEldEvent ymToSleeper1 = savedEvents.get(2);
        EmployeeLogEldEvent ymToSleeper2 = savedEvents.get(3);
        EmployeeLogEldEvent sleeper = savedEvents.get(4);
/*
        assertEquals("event type of ymToSleeper1 is incorrect", Enums.EmployeeLogEldEventType.DutyStatusChange, ymToSleeper1.getEventType());
        assertEquals("event code of ymToSleeper1 is incorrect", origFirstEventCode, ymToSleeper1.getEventCode());
        int pcToSleeper1EventRecordStatus = ymToSleeper1.getEventRecordStatus();
        assertEquals("record status of ymToSleeper1 is incorrect", Enums.EmployeeLogEldEventRecordStatus.InactiveChanged.getValue(), pcToSleeper1EventRecordStatus);

        assertEquals("event type of ymToSleeper2 is incorrect", Enums.EmployeeLogEldEventType.ChangeInDriversIndication, ymToSleeper2.getEventType());
        assertEquals("event code of ymToSleeper2 is incorrect", origSecondEventCode, ymToSleeper2.getEventCode());
        int yardToYard2EventRecordStatus = ymToSleeper2.getEventRecordStatus();
        assertEquals("record status of ymToSleeper2 is incorrect", Enums.EmployeeLogEldEventRecordStatus.InactiveChanged.getValue(), yardToYard2EventRecordStatus);

        assertEquals("event type of sleeper is incorrect", Enums.EmployeeLogEldEventType.DutyStatusChange, sleeper.getEventType());
        assertEquals("event code of sleeper is incorrect", newEventCode, sleeper.getEventCode());
        int sleeperEventRecordStatus = sleeper.getEventRecordStatus();
        assertEquals("record status of sleeper is incorrect", Enums.EmployeeLogEldEventRecordStatus.Active.getValue(), sleeperEventRecordStatus);
        */
    }

    public void testYMorPCToOpposite(int eventCode) {
        Date offDutyTime = DateUtility.AddMinutes(employeeLog.getLogDate(), 5);
        addDutyStatusChange(offDutyTime, OFF_DUTY, true);
        Date firstEventTime = DateUtility.AddMinutes(employeeLog.getLogDate(), 10);
        if (eventCode == EmployeeLogEldEventCode.ChangeInDriversIndication_PersonalUse) {
            addDutyStatusChange(firstEventTime, ON_DUTY, true);
            addChangeInDrivingIndication(firstEventTime, "YM start", EmployeeLogEldEventCode.ChangeInDriversIndication_YardMoves, "Yard Move");
        } else {
            addDutyStatusChange(firstEventTime, OFF_DUTY, true);
            addChangeInDrivingIndication(firstEventTime, "PC start", EmployeeLogEldEventCode.ChangeInDriversIndication_PersonalUse, "Personal Conveyance");

        }
        EmployeeLog uneditedLog = employeeLogFacade.GetLocalLogByDate(TimeKeeper.getInstance().getCurrentDateTime().toDate());
        EmployeeLogEldEvent[] originalEvents = uneditedLog.getEldEventList().getEldEventList();
        EmployeeLogEldEvent ymEvent1 = originalEvents[2];
        EmployeeLogEldEvent ymEvent2 = originalEvents[3];
        assertEquals("unexpected number of events", 4, originalEvents.length);
        assertEquals("event code is supposed to be DutyStatus_OffDuty", originalEvents[0].getEventCode(), EmployeeLogEldEventCode.DutyStatus_OffDuty);
        assertEquals("event code is supposed to be DutyStatus_OffDuty", originalEvents[1].getEventCode(), EmployeeLogEldEventCode.DutyStatus_OffDuty);
        assertEquals("event type is supposed to be ChangeInDriversIndication", ymEvent1.getEventType(), Enums.EmployeeLogEldEventType.ChangeInDriversIndication);
        assertEquals("event type is supposed to be DutyStatusChange", ymEvent2.getEventType(), Enums.EmployeeLogEldEventType.DutyStatusChange);

        if (eventCode == EmployeeLogEldEventCode.ChangeInDriversIndication_PersonalUse) {
            assertEquals("event code is supposed to be ChangeInDriversIndication_YardMoves", ymEvent1.getEventCode(), EmployeeLogEldEventCode.ChangeInDriversIndication_YardMoves);
            assertEquals("event code is supposed to be DutyStatus_OnDuty", ymEvent2.getEventCode(), EmployeeLogEldEventCode.DutyStatus_OnDuty);
        } else {
            assertEquals("event code is supposed to be ChangeInDriversIndication_PersonalUse", ymEvent1.getEventCode(), EmployeeLogEldEventCode.ChangeInDriversIndication_PersonalUse);
            assertEquals("event code is supposed to be DutyStatus_OffDuty", ymEvent2.getEventCode(), EmployeeLogEldEventCode.DutyStatus_OffDuty);
        }

        try {
            if (eventCode == EmployeeLogEldEventCode.ChangeInDriversIndication_PersonalUse) {
                ymEvent2.setEventCode(EmployeeLogEldEventCode.DutyStatus_OffDuty);
                employeeLogEldMandateController.saveEldEvent(ymEvent2, Enums.SpecialDrivingCategory.PersonalConveyance, null, Enums.ActionInitiatingSaveEnum.EditLog);
                ymEvent2.setEventCode(EmployeeLogEldEventCode.DutyStatus_OnDuty);
                employeeLogEldMandateController.saveEldEvent(ymEvent2, Enums.SpecialDrivingCategory.YardMove, null, Enums.ActionInitiatingSaveEnum.EditLog);
            } else {
                ymEvent2.setEventCode(EmployeeLogEldEventCode.DutyStatus_OnDuty);
                employeeLogEldMandateController.saveEldEvent(ymEvent2, Enums.SpecialDrivingCategory.YardMove, null, Enums.ActionInitiatingSaveEnum.EditLog);
                ymEvent2.setEventCode(EmployeeLogEldEventCode.DutyStatus_OffDuty);
                employeeLogEldMandateController.saveEldEvent(ymEvent2, Enums.SpecialDrivingCategory.PersonalConveyance, null, Enums.ActionInitiatingSaveEnum.EditLog);
            }

        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }

        EmployeeLog editedLog = employeeLogFacade.GetLocalLogByDate(TimeKeeper.getInstance().getCurrentDateTime().toDate());
        List<EmployeeLogEldEvent> savedEvents = Arrays.asList(editedLog.getEldEventList().getEldEventList());
        Collections.sort(savedEvents, new Comparator<EmployeeLogEldEvent>() {
            @Override
            public int compare(EmployeeLogEldEvent p1, EmployeeLogEldEvent p2) {
                return p1.getEventSequenceIDNumber() - p2.getEventSequenceIDNumber(); // Ascending
            }
        });
        assertEquals("unexpected number of events after edit", 8, savedEvents.size());

        EmployeeLogEldEvent yardToPC1 = savedEvents.get(4);
        EmployeeLogEldEvent yardToPC2 = savedEvents.get(5);
        EmployeeLogEldEvent pcToYard1 = savedEvents.get(6);
        EmployeeLogEldEvent pcToYard2 = savedEvents.get(7);
        if (eventCode == EmployeeLogEldEventCode.ChangeInDriversIndication_PersonalUse) {
            assertEquals("event code of  pcToYard1 is incorrect", EmployeeLogEldEventCode.DutyStatus_OnDuty, pcToYard1.getEventCode());
            assertEquals("event code of pcToYard2 is incorrect", EmployeeLogEldEventCode.ChangeInDriversIndication_YardMoves, pcToYard2.getEventCode());
            assertEquals("event code of yardToPC1 is incorrect", EmployeeLogEldEventCode.DutyStatus_OffDuty, yardToPC1.getEventCode());
            assertEquals("event code of yardToPC2 is incorrect", EmployeeLogEldEventCode.ChangeInDriversIndication_PersonalUse, yardToPC2.getEventCode());

        } else {
            assertEquals("event code of  pcToYard1 is incorrect", EmployeeLogEldEventCode.DutyStatus_OffDuty, pcToYard1.getEventCode());
            assertEquals("event code of pcToYard2 is incorrect", EmployeeLogEldEventCode.ChangeInDriversIndication_PersonalUse, pcToYard2.getEventCode());
            assertEquals("event code of yardToPC1 is incorrect", EmployeeLogEldEventCode.DutyStatus_OnDuty, yardToPC1.getEventCode());
            assertEquals("event code of yardToPC2 is incorrect", EmployeeLogEldEventCode.ChangeInDriversIndication_YardMoves, yardToPC2.getEventCode());
        }
        assertEquals("event type of  pcToYard1 is incorrect", Enums.EmployeeLogEldEventType.DutyStatusChange, pcToYard1.getEventType());
        int pcToYard1EventRecordStatus = pcToYard1.getEventRecordStatus();
        assertEquals("record status of  pcToYard1 is incorrect", Enums.EmployeeLogEldEventRecordStatus.Active.getValue(), pcToYard1EventRecordStatus);

        assertEquals("event type of pcToYard2 is incorrect", Enums.EmployeeLogEldEventType.ChangeInDriversIndication, pcToYard2.getEventType());
        int pcToYard2EventRecordStatus = pcToYard2.getEventRecordStatus();
        assertEquals("record status of pcToYard2 is incorrect", Enums.EmployeeLogEldEventRecordStatus.Active.getValue(), pcToYard2EventRecordStatus);

        assertEquals("event type of yardToPC1 is incorrect", Enums.EmployeeLogEldEventType.DutyStatusChange, yardToPC1.getEventType());
        int yardToPC1EventRecordStatus = yardToPC1.getEventRecordStatus();
        assertEquals("record status of yardToPC1 is incorrect", Enums.EmployeeLogEldEventRecordStatus.InactiveChanged.getValue(), yardToPC1EventRecordStatus);

        assertEquals("event type of yardToPC2 is incorrect", Enums.EmployeeLogEldEventType.ChangeInDriversIndication, yardToPC2.getEventType());
        int yardToPC2EventRecordStatus = yardToPC2.getEventRecordStatus();
        assertEquals("record status of yardToPC2 is incorrect", Enums.EmployeeLogEldEventRecordStatus.InactiveChanged.getValue(), yardToPC2EventRecordStatus);
    }

    @Test
    public void testYMToPCToYMEdit() {
        testYMorPCToOpposite(EmployeeLogEldEventCode.ChangeInDriversIndication_PersonalUse);
    }

    @Test
    public void testPCToYMToPCEdit() {
        testYMorPCToOpposite(EmployeeLogEldEventCode.ChangeInDriversIndication_YardMoves);

    }
    @Test
    public void testHOSAuditClocksOffDutyToOnDuty() {
        testHOSAuditClocks(OFF_DUTY,  EmployeeLogEldEventCode.DutyStatus_OnDuty);
    }
    @Test
    public void testHOSAuditClocksOnDutyToOffDuty() {
        testHOSAuditClocks(ON_DUTY,  EmployeeLogEldEventCode.DutyStatus_OffDuty);
    }

    public void testHOSAuditClocks(DutyStatusEnum origEventDutyStatusEnum, int newEventCode) {
        Date origEventTime = DateUtility.AddMinutes(employeeLog.getLogDate(), 10);
        addDutyStatusChange(origEventTime, origEventDutyStatusEnum, true);

        EmployeeLog uneditedLog = employeeLogFacade.GetLocalLogByDate(TimeKeeper.getInstance().getCurrentDateTime().toDate());
        EmployeeLogEldEvent[] originalEvents = uneditedLog.getEldEventList().getEldEventList();
        EmployeeLogEldEvent secondEvent = originalEvents[1];
        assertEquals("unexpected number of events", 2, originalEvents.length);
        int originalEventCode;
        if (origEventDutyStatusEnum == ON_DUTY) {
            originalEventCode = EmployeeLogEldEventCode.DutyStatus_OnDuty;
            assertEquals("event code is supposed to be DutyStatus_OffDuty", secondEvent.getEventCode(), originalEventCode);
        } else {
            originalEventCode = EmployeeLogEldEventCode.DutyStatus_OffDuty;
            assertEquals("event code is supposed to be DutyStatus_OffDuty", secondEvent.getEventCode(),originalEventCode);
        }
        HosAuditController hosAuditController = new HosAuditController(app);
        long originalAvailableMilliseconds = hosAuditController.DailyDutySummary().getAvailableMilliseconds();


        try {
            secondEvent.setEventCode(newEventCode);
            employeeLogEldMandateController.saveEldEvent(secondEvent, Enums.SpecialDrivingCategory.None, null, Enums.ActionInitiatingSaveEnum.EditLog);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
        EmployeeLog editedLog = employeeLogFacade.GetLocalLogByDate(TimeKeeper.getInstance().getCurrentDateTime().toDate());
        List<EmployeeLogEldEvent> savedEvents = Arrays.asList(editedLog.getEldEventList().getEldEventList());
        Collections.sort(savedEvents, new Comparator<EmployeeLogEldEvent>() {
            @Override
            public int compare(EmployeeLogEldEvent p1, EmployeeLogEldEvent p2) {
                return p1.getEventSequenceIDNumber() - p2.getEventSequenceIDNumber(); // Ascending
            }
        });
        assertEquals("unexpected number of events after edit", 3, savedEvents.size());

        EmployeeLogEldEvent originalEvent = savedEvents.get(1);
        EmployeeLogEldEvent newEvent = savedEvents.get(2);

        assertEquals("event type of originalEvent is incorrect", Enums.EmployeeLogEldEventType.DutyStatusChange, originalEvent.getEventType());
        assertEquals("event code of originalEvent is incorrect", originalEventCode, originalEvent.getEventCode());
        assertEquals("event type of newEvent is incorrect", Enums.EmployeeLogEldEventType.DutyStatusChange, newEvent.getEventType());
        assertEquals("event code of newEvent is incorrect", newEventCode, newEvent.getEventCode());
        hosAuditController.UpdateForCurrentLogEvent();

        long newAvailableMilliseconds = hosAuditController.DailyDutySummary().getAvailableMilliseconds();
        assertNotEquals("available milliseconds should have changed",originalAvailableMilliseconds, newAvailableMilliseconds);
        if (newEventCode == EmployeeLogEldEventCode.DutyStatus_OnDuty) {
            assertTrue("newAvailableMilliseconds should be less than originalAvailableMilliseconds", newAvailableMilliseconds < originalAvailableMilliseconds);
        } else {
            assertTrue("newAvailableMilliseconds should be more than originalAvailableMilliseconds", newAvailableMilliseconds > originalAvailableMilliseconds);
        }
    }

    @Test
    public void testPCToDrivingEdit() {
        testYMPCToRegularEdit(OFF_DUTY, EmployeeLogEldEventCode.DutyStatus_Driving);
    }

    public void testHyrailOrNonRegToYardOrPC(Enums.SpecialDrivingCategory intialSpecialDrivingCategory, Enums.SpecialDrivingCategory newSpecialDrivingCategory) {
        Date offDutyTime = DateUtility.AddMinutes(employeeLog.getLogDate(), 5);
        addDutyStatusChange(offDutyTime, OFF_DUTY, true);
        Date provisionTime = DateUtility.AddMinutes(employeeLog.getLogDate(), 40);
        if (intialSpecialDrivingCategory == Enums.SpecialDrivingCategory.Hyrail) {
            addHyrailNonReg(employeeLog, provisionTime, "Hyrail Started", 1);
        } else {
            addHyrailNonReg(employeeLog, provisionTime, "Non-Regulated Started", 2);
        }

        EmployeeLog uneditedLog = employeeLogFacade.GetLocalLogByDate(TimeKeeper.getInstance().getCurrentDateTime().toDate());
        EmployeeLogEldEvent[] originalEvents = uneditedLog.getEldEventList().getEldEventList();
        EmployeeLogEldEvent provisionEvent = originalEvents[2];
        assertEquals("unexpected number of events", 3, originalEvents.length);
        assertEquals("event code is supposed to be DutyStatus_OffDuty", originalEvents[0].getEventCode(), EmployeeLogEldEventCode.DutyStatus_OffDuty);
        assertEquals("event code is supposed to be DutyStatus_OffDuty", originalEvents[1].getEventCode(), EmployeeLogEldEventCode.DutyStatus_OffDuty);
        assertEquals("event type is supposed to be DutyStatusChange", provisionEvent.getEventType(), Enums.EmployeeLogEldEventType.DutyStatusChange);
        assertEquals("event code is supposed to be DutyStatus_OnDuty", provisionEvent.getEventCode(), EmployeeLogEldEventCode.DutyStatus_OnDuty);
        try {
            if (newSpecialDrivingCategory == Enums.SpecialDrivingCategory.PersonalConveyance) {
                provisionEvent.setEventCode(EmployeeLogEldEventCode.DutyStatus_OffDuty);
            } else {
                provisionEvent.setEventCode(EmployeeLogEldEventCode.DutyStatus_OnDuty);
            }
            employeeLogEldMandateController.saveEldEvent(provisionEvent, newSpecialDrivingCategory, null, Enums.ActionInitiatingSaveEnum.EditLog);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }

        EmployeeLog editedLog = employeeLogFacade.GetLocalLogByDate(TimeKeeper.getInstance().getCurrentDateTime().toDate());
        List<EmployeeLogEldEvent> savedEvents = Arrays.asList(editedLog.getEldEventList().getEldEventList());
        Collections.sort(savedEvents, new Comparator<EmployeeLogEldEvent>() {
            @Override
            public int compare(EmployeeLogEldEvent p1, EmployeeLogEldEvent p2) {
                return p1.getEventSequenceIDNumber() - p2.getEventSequenceIDNumber(); // Ascending
            }
        });
        assertEquals("unexpected number of events after edit", 5, savedEvents.size());
        EmployeeLogEldEvent hyrailToPc = savedEvents.get(2);
        EmployeeLogEldEvent ym1 = savedEvents.get(3);
        EmployeeLogEldEvent ym2 = savedEvents.get(4);
        if (newSpecialDrivingCategory == Enums.SpecialDrivingCategory.PersonalConveyance) {
            assertEquals("event code of ym1 is incorrect", EmployeeLogEldEventCode.DutyStatus_OffDuty, ym1.getEventCode());
            assertEquals("event code of ym2 is incorrect", EmployeeLogEldEventCode.ChangeInDriversIndication_PersonalUse, ym2.getEventCode());
        } else {
            assertEquals("event code of ym1 is incorrect", EmployeeLogEldEventCode.DutyStatus_OnDuty, ym1.getEventCode());
            assertEquals("event code of ym2 is incorrect", EmployeeLogEldEventCode.ChangeInDriversIndication_YardMoves, ym2.getEventCode());
        }

        assertEquals("event type of hyrailToPc is incorrect", Enums.EmployeeLogEldEventType.DutyStatusChange, hyrailToPc.getEventType());
        assertEquals("event code of hyrailToPc is incorrect", EmployeeLogEldEventCode.DutyStatus_OnDuty, hyrailToPc.getEventCode());
        int pcToSleeper1EventRecordStatus = hyrailToPc.getEventRecordStatus();
        assertEquals("record status of hyrailToPc is incorrect", Enums.EmployeeLogEldEventRecordStatus.InactiveChanged.getValue(), pcToSleeper1EventRecordStatus);

        assertEquals("event type of ym1 is incorrect", Enums.EmployeeLogEldEventType.DutyStatusChange, ym1.getEventType());
        int drivingEventRecordStatus = ym1.getEventRecordStatus();
        assertEquals("record status of ym1 is incorrect", Enums.EmployeeLogEldEventRecordStatus.Active.getValue(), drivingEventRecordStatus);

        assertEquals("event type of ym2 is incorrect", Enums.EmployeeLogEldEventType.ChangeInDriversIndication, ym2.getEventType());
        int yardToYard2EventRecordStatus = ym2.getEventRecordStatus();
        assertEquals("record status of ym2 is incorrect", Enums.EmployeeLogEldEventRecordStatus.Active.getValue(), yardToYard2EventRecordStatus);
    }

    @Test
    public void testHyrailToPCEdit() {
        testHyrailOrNonRegToYardOrPC(Enums.SpecialDrivingCategory.Hyrail, Enums.SpecialDrivingCategory.PersonalConveyance);
    }

    @Test
    public void testNonRegToYard() {
        testHyrailOrNonRegToYardOrPC(Enums.SpecialDrivingCategory.NonRegulated, Enums.SpecialDrivingCategory.YardMove);

    }

    public void overMidnightToOffDutyTest(boolean updateStartTime) {
        Date editedStartTime = DateUtility.AddHours(employeeLog.getLogDate(), 15);
        Date onDutyOverMidnight = DateUtility.AddHours(employeeLog.getLogDate(), 19);

        Date offDutyNextDay = DateUtility.AddHours(employeeLog.getLogDate(), 24);
        addDutyStatusChange(editedStartTime, ON_DUTY, true);
        addDutyStatusChange(onDutyOverMidnight, ON_DUTY, false);
        addDutyStatusChange(offDutyNextDay, OFF_DUTY, true);
        EmployeeLog uneditedLog = employeeLogFacade.GetLocalLogByDate(TimeKeeper.getInstance().getCurrentDateTime().toDate());
        List<EmployeeLogEldEvent> originalEvents = Arrays.asList(uneditedLog.getEldEventList().getEldEventList());
        Collections.sort(originalEvents, new Comparator<EmployeeLogEldEvent>() {
            @Override
            public int compare(EmployeeLogEldEvent p1, EmployeeLogEldEvent p2) {
                return p1.getEventSequenceIDNumber() - p2.getEventSequenceIDNumber(); // Ascending
            }
        });
        assertEquals("unexpected number of events", 4, originalEvents.size());
        EmployeeLogEldEvent onDutyEventToMidnightEdit = originalEvents.get(1);
        EmployeeLogEldEvent onDutyNextDay = originalEvents.get(2);
        if (updateStartTime) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(employeeLog.getLogDate());
            calendar.setTimeZone(TimeZone.getDefault());
            calendar.set(Calendar.HOUR_OF_DAY, 11);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 59);
            editedStartTime = calendar.getTime();
        }
        Calendar calendarEnd = Calendar.getInstance();
        calendarEnd.setTime(employeeLog.getLogDate());
        calendarEnd.setTimeZone(TimeZone.getDefault());
        calendarEnd.set(Calendar.HOUR_OF_DAY, 23);
        calendarEnd.set(Calendar.MINUTE, 59);
        calendarEnd.set(Calendar.SECOND, 59);
        Date editedEndTime = calendarEnd.getTime();
        try {
            onDutyEventToMidnightEdit.setEventCode(EmployeeLogEldEventCode.DutyStatus_OffDuty);
            onDutyNextDay.setEventCode(EmployeeLogEldEventCode.DutyStatus_OffDuty);
            onDutyEventToMidnightEdit.setStartTime(editedStartTime);
            employeeLogEldMandateController.saveEldEvent(onDutyEventToMidnightEdit, editedEndTime);
            employeeLogEldMandateController.saveEldEvent(onDutyNextDay, offDutyNextDay);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
        EmployeeLog editedLog = employeeLogFacade.GetLocalLogByDate(TimeKeeper.getInstance().getCurrentDateTime().toDate());
        List<EmployeeLogEldEvent> savedEvents = Arrays.asList(editedLog.getEldEventList().getEldEventList());
        Collections.sort(savedEvents, new Comparator<EmployeeLogEldEvent>() {
            @Override
            public int compare(EmployeeLogEldEvent p1, EmployeeLogEldEvent p2) {
                return p1.getEventSequenceIDNumber() - p2.getEventSequenceIDNumber(); // Ascending
            }
        });
        assertEquals("unexpected number of events", 6, savedEvents.size());
        int origRecordStatus1 = savedEvents.get(1).getEventRecordStatus();
        int origRecordStatus2 = savedEvents.get(2).getEventRecordStatus();
        int editedRecordStatus1 = savedEvents.get(4).getEventRecordStatus();
        int editedRecordStatus2 = savedEvents.get(5).getEventRecordStatus();
        assertEquals("event is supposed to be inactive", 2, origRecordStatus1);
        assertEquals("event is supposed to be inactive", 2, origRecordStatus2);
        assertEquals("event is supposed to be active", 1, editedRecordStatus1);
        assertEquals("event is supposed to be active", 1, editedRecordStatus2);
        if (updateStartTime) {
            assertEquals("event date times are equal", true, savedEvents.get(1).getEventDateTime().compareTo(savedEvents.get(4).getEventDateTime()) != 0);
        } else {
            assertEquals("event date times are equal", true, savedEvents.get(1).getEventDateTime().compareTo(savedEvents.get(4).getEventDateTime()) == 0);
        }
        assertEquals("event date times are not equal", true, savedEvents.get(2).getEventDateTime().compareTo(savedEvents.get(5).getEventDateTime()) == 0);
    }

    @Test
    public void testOverMidnightToOffDutyAndNonChangeToStartTime() {
        overMidnightToOffDutyTest(false);
    }

    @Test
    public void testOverMidnightToOffDutyAndChangeStartTIme() {
        overMidnightToOffDutyTest(true);
    }
}