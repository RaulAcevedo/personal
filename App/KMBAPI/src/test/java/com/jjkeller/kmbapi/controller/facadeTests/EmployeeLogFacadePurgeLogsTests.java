package com.jjkeller.kmbapi.controller.facadeTests;


import com.jjkeller.kmbapi.common.ITimeKeeper;
import com.jjkeller.kmbapi.common.TestBase;
import com.jjkeller.kmbapi.common.TimeKeeper;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.configuration.LoginCredentials;
import com.jjkeller.kmbapi.configuration.UserState;
import com.jjkeller.kmbapi.controller.ControllerFactory;
import com.jjkeller.kmbapi.controller.EmployeeLogEldMandateController;
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

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * // * Set of Integration Tests for the TeamDriverController
 */
@RunWith(RobolectricTestRunner.class)
@Config(application = GlobalState.class,
        manifest = "/build/intermediates/bundles/encompass/debug/AndroidManifest.xml")
public class EmployeeLogFacadePurgeLogsTests extends TestBase {

    private GlobalState app;
    private FeatureToggleService ftService;
    private EmployeeLogFacade employeeLogFacade;
    private EmployeeLogEldMandateController employeeLogEldMandateController;
    private User user;


    private Date now;
    private Date yesterday;
    private Date twoDaysAgo;
    private Date threeDaysAgo;
    private Date fourDaysAgo;

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

        employeeLogEldMandateController = ControllerFactory.getInstance().getEmployeeLogEldMandateController();
        employeeLogFacade = new EmployeeLogFacade(app, user);

        DateTime nowWithMillis = TimeKeeper.getInstance().getCurrentDateTime();
        now = nowWithMillis.withMillisOfSecond(0).toDate();
        yesterday = nowWithMillis.withMillisOfSecond(0).minusDays(1).toDate();
        twoDaysAgo = nowWithMillis.withMillisOfSecond(0).minusDays(2).toDate();
        threeDaysAgo = nowWithMillis.withMillisOfSecond(0).minusDays(3).toDate();
        fourDaysAgo = nowWithMillis.withMillisOfSecond(0).minusDays(4).toDate();

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

    private void addDutyStatusChange(EmployeeLog lg, Date time, DutyStatusEnum status) {
        try {
            employeeLogEldMandateController.CreateDutyStatusChangedEvent(lg, time, status, new Location(), false, ruleType, null, null, false, null, null);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    private void addCertifiedLog(Date d) {
        Date t1 = DateUtility.AddHours(d, 1);
        Date t2 = DateUtility.AddHours(d, 2);
        Date t3 = DateUtility.AddMinutes(d, 5);

        EmployeeLog employeeLog = EmployeeLogUtilities.CreateNewLog(app, user, d, DutyStatusEnum.valueOfDMOEnum(DutyStatusEnum.DmoEnum_OffDuty), null);


        addDutyStatusChange(employeeLog, t1, OFF_DUTY);
        addDutyStatusChange(employeeLog, t2, DRIVING);
        addDutyStatusChange(employeeLog, t3, SLEEPER);
        employeeLog.setIsCertified(true);
        employeeLogFacade.Save(employeeLog, 1);
    }

    @Test
    public void testDateCutOff() {
        addCertifiedLog(now);
        addCertifiedLog(yesterday);
        addCertifiedLog(twoDaysAgo);

        EmployeeLog todaysLog = employeeLogFacade.GetLocalLogByDate(now);
        EmployeeLog yesterdaysLog = employeeLogFacade.GetLocalLogByDate(yesterday);
        EmployeeLog twoDaysAgoLog = employeeLogFacade.GetLocalLogByDate(twoDaysAgo);

        employeeLogFacade.PurgeOldRecords(yesterday);

        todaysLog = employeeLogFacade.GetLocalLogByDate(now);
        yesterdaysLog = employeeLogFacade.GetLocalLogByDate(yesterday);
        twoDaysAgoLog = employeeLogFacade.GetLocalLogByDate(twoDaysAgo);

        assertNotNull("should not be purged", todaysLog);
        assertNotNull("should not be purged", yesterdaysLog);
        assertNull("Should be purged", twoDaysAgoLog);

    }


    @Test
    public void testCertifiedLogsStay() {
        addCertifiedLog(now);
        addCertifiedLog(yesterday);
        addCertifiedLog(twoDaysAgo);
        addCertifiedLog(threeDaysAgo);
        addCertifiedLog(fourDaysAgo);

        EmployeeLog twoDaysAgoLog = employeeLogFacade.GetLocalLogByDate(twoDaysAgo);
        twoDaysAgoLog.setIsCertified(false);
        employeeLogFacade.Save(twoDaysAgoLog, 1);

        employeeLogFacade.PurgeOldRecords(yesterday);

        EmployeeLog todaysLog = employeeLogFacade.GetLocalLogByDate(now);
        EmployeeLog yesterdaysLog = employeeLogFacade.GetLocalLogByDate(yesterday);
        twoDaysAgoLog = employeeLogFacade.GetLocalLogByDate(twoDaysAgo);
        EmployeeLog threeDayAgoLog = employeeLogFacade.GetLocalLogByDate(threeDaysAgo);
        EmployeeLog fourDaysAgoLog = employeeLogFacade.GetLocalLogByDate(fourDaysAgo);

        assertNotNull("should not be purged", todaysLog);
        assertNotNull("should not be purged", yesterdaysLog);
        assertNotNull("should not be purged", twoDaysAgoLog);
        assertNull("Should be purged", threeDayAgoLog);
        assertNull("Should be purged", fourDaysAgoLog);

    }



    @Test
    public void testLogsWithEditRequestsLogsStay() {
        addCertifiedLog(now);
        addCertifiedLog(yesterday);
        addCertifiedLog(twoDaysAgo);
        addCertifiedLog(threeDaysAgo);
        addCertifiedLog(fourDaysAgo);

        EmployeeLog threeDayAgoLog = employeeLogFacade.GetLocalLogByDate(threeDaysAgo);
        threeDayAgoLog.getEldEventList().getEldEventList()[0].setEventRecordStatus(Enums.EmployeeLogEldEventRecordStatus.InactiveChangeRequested.getValue());

        employeeLogFacade.Save(threeDayAgoLog, 1);
        employeeLogFacade.PurgeOldRecords(yesterday);

        EmployeeLog todaysLog = employeeLogFacade.GetLocalLogByDate(now);
        EmployeeLog yesterdaysLog = employeeLogFacade.GetLocalLogByDate(yesterday);
        EmployeeLog twoDaysAgoLog = employeeLogFacade.GetLocalLogByDate(twoDaysAgo);
        threeDayAgoLog = employeeLogFacade.GetLocalLogByDate(threeDaysAgo);
        EmployeeLog fourDaysAgoLog = employeeLogFacade.GetLocalLogByDate(fourDaysAgo);


        assertNotNull("should not be purged", todaysLog);
        assertNotNull("should not be purged", yesterdaysLog);
        assertNull("should be purged", twoDaysAgoLog);
        assertNotNull("Should not be purged", threeDayAgoLog);
        assertNull("Should be purged", fourDaysAgoLog);

    }


    @Test
    public void testCertifiedAndEditedOff() {
        addCertifiedLog(now);
        addCertifiedLog(yesterday);
        addCertifiedLog(twoDaysAgo);
        addCertifiedLog(threeDaysAgo);
        addCertifiedLog(fourDaysAgo);

        EmployeeLog threeDayAgoLog = employeeLogFacade.GetLocalLogByDate(threeDaysAgo);
        threeDayAgoLog.setIsCertified(false);
        threeDayAgoLog.getEldEventList().getEldEventList()[0].setEventRecordStatus(Enums.EmployeeLogEldEventRecordStatus.InactiveChangeRequested.getValue());
        employeeLogFacade.Save(threeDayAgoLog, 1);

        employeeLogFacade.PurgeOldRecords(yesterday);

        EmployeeLog todaysLog = employeeLogFacade.GetLocalLogByDate(now);
        EmployeeLog yesterdaysLog = employeeLogFacade.GetLocalLogByDate(yesterday);
        EmployeeLog twoDaysAgoLog = employeeLogFacade.GetLocalLogByDate(twoDaysAgo);
        threeDayAgoLog = employeeLogFacade.GetLocalLogByDate(threeDaysAgo);
        EmployeeLog fourDaysAgoLog = employeeLogFacade.GetLocalLogByDate(fourDaysAgo);

        assertNotNull("should not be purged", todaysLog);
        assertNotNull("should not be purged", yesterdaysLog);
        assertNull("Should be purged", twoDaysAgoLog);
        assertNotNull("Should not be purged", threeDayAgoLog);
        assertNull("Should be purged", fourDaysAgoLog);
    }


    @Test
    public void testRejectedEditsWillStillPurge() {
        addCertifiedLog(now);
        addCertifiedLog(yesterday);
        addCertifiedLog(twoDaysAgo);
        addCertifiedLog(threeDaysAgo);
        addCertifiedLog(fourDaysAgo);

        EmployeeLog threeDayAgoLog = employeeLogFacade.GetLocalLogByDate(threeDaysAgo);
        threeDayAgoLog.getEldEventList().getEldEventList()[0].setEventRecordStatus(Enums.EmployeeLogEldEventRecordStatus.InactiveChangeRejected.getValue());
        employeeLogFacade.Save(threeDayAgoLog, 1);
        employeeLogFacade.PurgeOldRecords(yesterday);


        EmployeeLog todaysLog = employeeLogFacade.GetLocalLogByDate(now);
        EmployeeLog yesterdaysLog = employeeLogFacade.GetLocalLogByDate(yesterday);
        EmployeeLog twoDaysAgoLog = employeeLogFacade.GetLocalLogByDate(twoDaysAgo);
        threeDayAgoLog = employeeLogFacade.GetLocalLogByDate(threeDaysAgo);
        EmployeeLog fourDaysAgoLog = employeeLogFacade.GetLocalLogByDate(fourDaysAgo);

        assertNotNull("should not be purged", todaysLog);
        assertNotNull("should not be purged", yesterdaysLog);
        assertNull("Should be purged", twoDaysAgoLog);
        assertNull("Should be purged", threeDayAgoLog);
        assertNull("Should be purged", fourDaysAgoLog);
    }



}
