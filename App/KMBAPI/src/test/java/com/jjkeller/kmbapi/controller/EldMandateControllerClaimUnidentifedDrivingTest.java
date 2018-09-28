package com.jjkeller.kmbapi.controller;

import com.jjkeller.kmbapi.R;
import com.jjkeller.kmbapi.common.ITimeKeeper;
import com.jjkeller.kmbapi.common.TestBase;
import com.jjkeller.kmbapi.common.TimeKeeper;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.configuration.LoginCredentials;
import com.jjkeller.kmbapi.configuration.UserState;
import com.jjkeller.kmbapi.controller.dataaccess.EmployeeLogEldEventFacade;
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
import com.jjkeller.kmbapi.kmbeobr.GpsFix;
import com.jjkeller.kmbapi.kmbeobr.VehicleLocation;
import com.jjkeller.kmbapi.proxydata.CompanyConfigSettings;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;
import com.jjkeller.kmbapi.proxydata.compare.EmployeeLogEldEventDateComparator;

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
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(application = GlobalState.class,
        manifest = "/build/intermediates/bundles/encompass/debug/AndroidManifest.xml"
)
public class EldMandateControllerClaimUnidentifedDrivingTest extends TestBase {

    private GlobalState app;
    private EmployeeLogFacade employeeLogFacade;
    private EmployeeLogEldEventFacade employeeLogEventFacade;

    private EmployeeLogEldMandateController employeeLogEldMandateController;


    private Date today = new Date();
    private Date yesterday = DateUtility.AddDays(new Date(), -1);

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        app = (GlobalState) RuntimeEnvironment.application;

        FeatureToggleService ftService = mock(FeatureToggleService.class);
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

        employeeLogEldMandateController = ControllerFactory.getInstance().getEmployeeLogEldMandateController();
        employeeLogFacade = new EmployeeLogFacade(app, user);

        employeeLogEventFacade = new EmployeeLogEldEventFacade(app, user);
;
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

    private DutyStatusEnum DRIVING = new DutyStatusEnum(DutyStatusEnum.DRIVING);
    private DutyStatusEnum ON_DUTY = new DutyStatusEnum(DutyStatusEnum.ONDUTY);

    private RuleSetTypeEnum ruleType = new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR);

    private void addUnassigendDriving(Date start, Date end){
        try {


            VehicleLocation location = new VehicleLocation();
            GpsFix fix = new GpsFix();
            fix.setTimeCode(start.getTime());
            location.setGpsFix(fix);
            EmployeeLogEldEvent unassignedDrive = employeeLogEldMandateController.CreateDriveOnOrOffUnassignedEvent(location, Enums.CompositeEmployeeLogEldEventTypeEventCodeEnum.DrivingEvent);
            employeeLogEldMandateController.SaveDriveOnOrOffUnassignedEvent(unassignedDrive);

            VehicleLocation location2 = new VehicleLocation();
            GpsFix fix2 = new GpsFix();
            fix2.setTimeCode(end.getTime());
            location2.setGpsFix(fix2);
            EmployeeLogEldEvent unassignedDriveOff = employeeLogEldMandateController.CreateDriveOnOrOffUnassignedEvent(location2, Enums.CompositeEmployeeLogEldEventTypeEventCodeEnum.OnDutyEvent);
            employeeLogEldMandateController.SaveDriveOnOrOffUnassignedEvent(unassignedDriveOff);

        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    @Test
    public void testClaimDriving() throws CloneNotSupportedException {
        baseClaimeUnidentifiedEvents(false);
    }

    @Test
    public void testClaimDrivingOverMidnightNoLogCreatedToday() throws CloneNotSupportedException {
        EmployeeLog employeeLog = EmployeeLogUtilities.CreateNewLog(app, app.getCurrentUser(), yesterday, DutyStatusEnum.valueOfDMOEnum(DutyStatusEnum.DmoEnum_OffDuty), null);
        employeeLogFacade.Save(employeeLog, 1);


        Date yesterday = TimeKeeper.getInstance().getCurrentDateTime().withMillisOfSecond(0).minusDays(1).toDate();
        Date hourAfterMidnight = TimeKeeper.getInstance().getCurrentDateTime().withMillisOfSecond(0).withTimeAtStartOfDay().plusHours(1).toDate();

        Date driveStart = DateUtility.AddMinutes(yesterday, -50);
        Date driveEnd = DateUtility.AddMinutes(hourAfterMidnight, -20);

        addUnassigendDriving(driveStart, driveEnd);

        List<EmployeeLogEldEvent> employeeLogEldEventList = employeeLogEventFacade.GetUnreviewedUnidentifiedEvents();
        Collections.sort(employeeLogEldEventList, new EmployeeLogEldEventDateComparator());
        assertEquals("should be 2 unreviewed items" , 2, employeeLogEldEventList.size());

        EmployeeLogEldEvent unassignedEvent = employeeLogEldEventList.get(0);
        EmployeeLogEldEvent unassignedEvent1 = employeeLogEldEventList.get(1);

        //process first event
        List<EmployeeLogEldEvent> saved1 = employeeLogEldMandateController.assignEldEventToUser(unassignedEvent, unassignedEvent1.getEventDateTime(), app.getCurrentUser());
        assertEquals("should be 2 unreviewed items" , 2, saved1.size());
        EmployeeLogEldEvent yesterdayEvent = saved1.get(0);
        EmployeeLogEldEvent todayEvent = saved1.get(1);

        assertEquals("logId should be set" , 1, yesterdayEvent.getLogKey().intValue());
        assertEquals("should be driving status" , DRIVING.toDMOEnum(), yesterdayEvent.getDutyStatusEnum().toDMOEnum());
        assertEquals("event time off" , driveStart.getTime(), yesterdayEvent.getEventDateTime().getTime());

        assertEquals("logId should be set" , 2, todayEvent.getLogKey().intValue());
        assertEquals("should be driving status" , DRIVING.toDMOEnum(), todayEvent.getDutyStatusEnum().toDMOEnum());
        assertEquals("should be start day" , TimeKeeper.getInstance().getCurrentDateTime().withTimeAtStartOfDay().getMillis(), todayEvent.getEventDateTime().getTime());


        //process second event
        List<EmployeeLogEldEvent> saved2 = employeeLogEldMandateController.assignEldEventToUser(unassignedEvent1, null, app.getCurrentUser());
        assertEquals("should be 1 saved items" , 1, saved2.size());
        EmployeeLogEldEvent todayEvent2 = saved2.get(0);

        assertEquals("logId should be set" , 2, todayEvent2.getLogKey().intValue());
        assertEquals("should be driving status" , ON_DUTY.toDMOEnum(), todayEvent2.getDutyStatusEnum().toDMOEnum());
        assertEquals("event time off" , driveEnd.getTime(), todayEvent2.getEventDateTime().getTime());

        // verify events on logs
        EmployeeLog yesterdaysLog = employeeLogFacade.GetLocalLogByDate(yesterday);
        EmployeeLog todaysLog = employeeLogFacade.GetLocalLogByDate(today);

        EmployeeLogEldEvent[] todaysEvents = todaysLog.getEldEventList().getEldEventList();
        //assertEquals("unexpected number of events", 3, todaysEvents.length);

        //todo assert these values here dont print em out.

        //inital driving...
        //drive end
        printEvents(yesterdaysLog);

        System.out.println("------ ");
        printEvents(todaysLog);

        EmployeeLogEldEvent event0 = todaysEvents[0];
        assertEquals("time of event0 is incorrect", todaysLog.getLogDate().getTime(), event0.getEventDateTime().getTime());
    }

    public void baseClaimeUnidentifiedEvents(boolean exemptFromEldUse) throws CloneNotSupportedException {
        if (exemptFromEldUse) {
            EmployeeLog employeeLog = EmployeeLogUtilities.CreateNewLog(app, app.getCurrentUser(), today, DutyStatusEnum.valueOfDMOEnum(DutyStatusEnum.DmoEnum_OffDuty), null);
            employeeLog.setIsExemptFromELDUse(true);
            employeeLogFacade.Save(employeeLog, 1);
        }

        Date currentNoMillis =  TimeKeeper.getInstance().getCurrentDateTime().withMillisOfSecond(0).toDate();

        Date driveStart = DateUtility.AddMinutes(currentNoMillis, -50);
        Date driveEnd = DateUtility.AddMinutes(currentNoMillis, -20);

        addUnassigendDriving(driveStart, driveEnd);

        List<EmployeeLogEldEvent> employeeLogEldEventList = employeeLogEventFacade.GetUnreviewedUnidentifiedEvents();
        Collections.sort(employeeLogEldEventList, new EmployeeLogEldEventDateComparator());
        assertEquals("should be 2 unreviewed items" , 2, employeeLogEldEventList.size());

        EmployeeLogEldEvent unassignedEvent = employeeLogEldEventList.get(0);
        EmployeeLogEldEvent unassignedEvent1 = employeeLogEldEventList.get(1);

        List<EmployeeLogEldEvent> saved1 = employeeLogEldMandateController.assignEldEventToUser(unassignedEvent, unassignedEvent1.getEventDateTime(), app.getCurrentUser());
        assertEquals("should be 1 unreviewed items" , 1, saved1.size());
        EmployeeLogEldEvent firstEvent = saved1.get(0);
        assertEquals("logId should be set" , 1, firstEvent.getLogKey().intValue());
        assertEquals("should be driving status" , DRIVING.toDMOEnum(), firstEvent.getDutyStatusEnum().toDMOEnum());
        assertEquals("event time off" , driveStart.getTime(), firstEvent.getEventDateTime().getTime());

        List<EmployeeLogEldEvent> saved2 = employeeLogEldMandateController.assignEldEventToUser(unassignedEvent1, null, app.getCurrentUser());
        assertEquals("should be 1 unreviewed items" , 1, saved2.size());
        EmployeeLogEldEvent firstEvent2 = saved2.get(0);
        assertEquals("logId should be set" , 1, firstEvent2.getLogKey().intValue());
        assertEquals("should be driving status" , ON_DUTY.toDMOEnum(), firstEvent2.getDutyStatusEnum().toDMOEnum());
        assertEquals("event time off" , driveEnd.getTime(), firstEvent2.getEventDateTime().getTime());
        if (exemptFromEldUse) {
            long saveRecordStatus = firstEvent.getEventRecordStatus();
            assertEquals("record status should be inactive", 2, saveRecordStatus);
            assertEquals("event comment  is not correct", app.getString(R.string.exemptFromEldUse), firstEvent.getEventComment());
            long saveRecordStatus2 = firstEvent2.getEventRecordStatus();
            assertEquals("record status should be inactive", 2, saveRecordStatus2);
            assertEquals("event comment is not correct", app.getString(R.string.exemptFromEldUse), firstEvent2.getEventComment());

        }
    }

    @Test
    public void testClaimUnidentifiedExemptFromELDUseEvents() throws CloneNotSupportedException {
        baseClaimeUnidentifiedEvents(true);
    }
    @Test
    public void testClaimExemptFromEldUseForOnlyTodayForEventOverMidnight() throws CloneNotSupportedException {
        baseClaimDrivingOverMidnightLogAlreadyCreated(false, true);
    }
    @Test
    public void testClaimExemptFromEldUseForOnlyYesterdayForEventOverMidnight() throws CloneNotSupportedException {
        baseClaimDrivingOverMidnightLogAlreadyCreated(true, false);
    }
    @Test
    public void testClaimExemptFromEldUseForEventOverMidnight() throws CloneNotSupportedException {
        baseClaimDrivingOverMidnightLogAlreadyCreated(true, true);
    }

    @Test
    public void testClaimDrivingOverMidnightLogAlreadyCreated() throws CloneNotSupportedException {
        baseClaimDrivingOverMidnightLogAlreadyCreated(false, false);
    }

    public void baseClaimDrivingOverMidnightLogAlreadyCreated(boolean isYesterdayExemptFromELDUse, boolean isTodayExemptFromELDUse) throws CloneNotSupportedException {
        EmployeeLog employeeLog = EmployeeLogUtilities.CreateNewLog(app, app.getCurrentUser(), yesterday, DutyStatusEnum.valueOfDMOEnum(DutyStatusEnum.DmoEnum_OffDuty), null);
        employeeLog.setIsExemptFromELDUse(isYesterdayExemptFromELDUse);
        employeeLogFacade.Save(employeeLog, 1);

        EmployeeLog todaysEmployeeLog = EmployeeLogUtilities.CreateNewLog(app, app.getCurrentUser(), today, DutyStatusEnum.valueOfDMOEnum(DutyStatusEnum.DmoEnum_OffDuty), null);
        todaysEmployeeLog.setIsExemptFromELDUse(isTodayExemptFromELDUse);
        employeeLogFacade.Save(todaysEmployeeLog, 1);
        app.setCurrentEmployeeLog(todaysEmployeeLog);


        Date yesterday = TimeKeeper.getInstance().getCurrentDateTime().withMillisOfSecond(0).minusDays(1).toDate();
        Date hourAfterMidnight = TimeKeeper.getInstance().getCurrentDateTime().withMillisOfSecond(0).withTimeAtStartOfDay().plusHours(1).toDate();

        Date driveStart = DateUtility.AddMinutes(yesterday, -50);
        Date driveEnd = DateUtility.AddMinutes(hourAfterMidnight, -20);

        addUnassigendDriving(driveStart, driveEnd);

        List<EmployeeLogEldEvent> employeeLogEldEventList = employeeLogEventFacade.GetUnreviewedUnidentifiedEvents();
        Collections.sort(employeeLogEldEventList, new EmployeeLogEldEventDateComparator());
        assertEquals("should be 2 unreviewed items" , 2, employeeLogEldEventList.size());

        EmployeeLogEldEvent unassignedEvent = employeeLogEldEventList.get(0);
        EmployeeLogEldEvent unassignedEvent1 = employeeLogEldEventList.get(1);

        //process first event
        List<EmployeeLogEldEvent> saved1 = employeeLogEldMandateController.assignEldEventToUser(unassignedEvent, unassignedEvent1.getEventDateTime(), app.getCurrentUser());
        assertEquals("should be 2 unreviewed items" , 2, saved1.size());
        EmployeeLogEldEvent yesterdayEvent = saved1.get(0);
        EmployeeLogEldEvent todayEvent = saved1.get(1);

        assertEquals("logId should be set" , 1, yesterdayEvent.getLogKey().intValue());
        assertEquals("should be driving status" , DRIVING.toDMOEnum(), yesterdayEvent.getDutyStatusEnum().toDMOEnum());
        assertEquals("event time off" , driveStart.getTime(), yesterdayEvent.getEventDateTime().getTime());

        assertEquals("logId should be set" , 2, todayEvent.getLogKey().intValue());
        assertEquals("should be driving status" , DRIVING.toDMOEnum(), todayEvent.getDutyStatusEnum().toDMOEnum());
        assertEquals("should be start day" , TimeKeeper.getInstance().getCurrentDateTime().withTimeAtStartOfDay().getMillis(), todayEvent.getEventDateTime().getTime());

        //process second event
        List<EmployeeLogEldEvent> saved2 = employeeLogEldMandateController.assignEldEventToUser(unassignedEvent1, null, app.getCurrentUser());
        assertEquals("should be 1 saved items" , 1, saved2.size());
        EmployeeLogEldEvent todayEvent2 = saved2.get(0);

        assertEquals("logId should be set" , 2, todayEvent2.getLogKey().intValue());
        assertEquals("should be driving status" , ON_DUTY.toDMOEnum(), todayEvent2.getDutyStatusEnum().toDMOEnum());
        assertEquals("event time off" , driveEnd.getTime(), todayEvent2.getEventDateTime().getTime());

        if (isYesterdayExemptFromELDUse) {
            long yesterdaySaveRecordStatus = yesterdayEvent.getEventRecordStatus();
            assertEquals("record status should be inactive", 2, yesterdaySaveRecordStatus);
            assertEquals("event comment  is not correct", app.getString(R.string.exemptFromEldUse), yesterdayEvent.getEventComment());
        }

        if (isTodayExemptFromELDUse) {
            long todaySaveRecordStatus = todayEvent.getEventRecordStatus();
            assertEquals("record status should be inactive", 2, todaySaveRecordStatus);
            assertEquals("event comment  is not correct", app.getString(R.string.exemptFromEldUse), todayEvent.getEventComment());
            long todayEvent2SaveRecordStatus = todayEvent2.getEventRecordStatus();
            assertEquals("record status should be inactive", 2, todayEvent2SaveRecordStatus);
            assertEquals("event comment  is not correct", app.getString(R.string.exemptFromEldUse), todayEvent2.getEventComment());
        }
        // verify events on logs
        EmployeeLog yesterdaysLog = employeeLogFacade.GetLocalLogByDate(yesterday);
        EmployeeLog todaysLog = employeeLogFacade.GetLocalLogByDate(today);

        EmployeeLogEldEvent[] todaysEvents = todaysLog.getEldEventList().getEldEventList();
//        assertEquals("unexpected number of events", 2, todaysEvents.length);

        //todo assert these values here dont print em out.

        //inital driving...
        //drive end
        printEvents(yesterdaysLog);

        System.out.println("------ ");
        printEvents(todaysLog);

        EmployeeLogEldEvent event0 = todaysEvents[0];
        assertEquals("time of event0 is incorrect", todaysLog.getLogDate().getTime(), event0.getEventDateTime().getTime());
    }


    private void printEvents(EmployeeLog log){
        for(EmployeeLogEldEvent events : log.getEldEventList().getEldEventList()){
            System.out.println("dude." + events.getDutyStatusEnum().toFriendlyName() + " - " + events.getEventDateTime() + " " + events.getEventRecordStatus());

        }

    }

}