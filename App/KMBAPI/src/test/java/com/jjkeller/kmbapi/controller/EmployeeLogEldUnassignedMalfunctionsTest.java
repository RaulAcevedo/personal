package com.jjkeller.kmbapi.controller;

import com.jjkeller.kmbapi.common.TestBase;
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
import com.jjkeller.kmbapi.enums.Malfunction;
import com.jjkeller.kmbapi.enums.RuleSetTypeEnum;
import com.jjkeller.kmbapi.enums.TimeZoneEnum;
import com.jjkeller.kmbapi.featuretoggle.FeatureToggleService;
import com.jjkeller.kmbapi.proxydata.CompanyConfigSettings;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;

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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(application = GlobalState.class,
        manifest = "/build/intermediates/bundles/encompass/debug/AndroidManifest.xml"
)
public class EmployeeLogEldUnassignedMalfunctionsTest extends TestBase {
    private GlobalState app;
    private FeatureToggleService ftService;
    private EmployeeLogEldEventFacade eldEventFacade;
    private EmployeeLogEldMandateController eldMandateController;
    private EmployeeLogFacade employeeLogFacade;

    private User user1;

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        app = (GlobalState) RuntimeEnvironment.application;

        ftService = mock(FeatureToggleService.class);
        when(ftService.getIsEldMandateEnabled()).thenReturn(true);

        Field field = GlobalState.class.getDeclaredField("_featureToggleService");
        field.setAccessible(true);
        field.set(app, ftService);


        CompanyConfigSettings settings = new CompanyConfigSettings();
        settings.setDailyLogStartTime("00:00");
        settings.setDmoCompanyName("JesseCorp");
        app.setCompanyConfigSettings(app, settings);

        ArrayList<User> userList = new ArrayList<User>();
        app.setLoggedInUserList(userList);

        user1 = addMockTestUser(1);

        app.setCurrentUser(user1);


        eldEventFacade = new EmployeeLogEldEventFacade(app, user1);
        employeeLogFacade = new EmployeeLogFacade(app, user1);

        eldMandateController = ControllerFactory.getInstance().getEmployeeLogEldMandateController();
    }

    @After
    public void tearDown() throws NoSuchFieldException, IllegalAccessException {
        closeDatabase(app);
    }


    private User addMockTestUser(int userNumber){
        User user = mock(User.class);
        UserState userState = new UserState();
        when(user.getUserState()).thenReturn(userState);
        when(user.getHomeTerminalTimeZone()).thenReturn(TimeZoneEnum.CENTRAL_STANDARD_TIME);
        when(user.getRulesetTypeEnum()).thenReturn(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR));
        when(user.getDriverType()).thenReturn(new DriverTypeEnum(DriverTypeEnum.PROPERTYCARRYING));

        LoginCredentials creds = mock(LoginCredentials.class);
        when(creds.getPrimaryKey()).thenReturn((long)userNumber);
        when(creds.getEmployeeId()).thenReturn("000-"+userNumber);
        when(user.getCredentials()).thenReturn(creds);

        app.getLoggedInUserList().add(user);
        return user;
    }

    private EmployeeLog createLog(User user){
        GlobalState.getInstance().setCurrentUser(user);
        EmployeeLog log = EmployeeLogUtilities.CreateNewLog(app, user, DateUtility.getCurrentDateTimeWithSecondsUTC(), DutyStatusEnum.valueOfDMOEnum(DutyStatusEnum.DmoEnum_Driving), null);
        employeeLogFacade.setCurrentUser(user);
        employeeLogFacade.Save(log, 1);
        return log;
    }

    protected int getTotalMalfunctionEvents(long logId, Malfunction malfunction) {
        EmployeeLogEldEvent[] logEldEvents = eldEventFacade.GetByEventTypes((int)logId,
                Arrays.asList(new Integer[]{Enums.EmployeeLogEldEventType.Malfunction_DataDiagnosticDetection.getValue()}),
                Arrays.asList(new Integer[]{Enums.EmployeeLogEldEventRecordStatus.Active.getValue()})
        );

        int counter = 0;
        for(EmployeeLogEldEvent event : logEldEvents){
            if(Malfunction.valueOfDMOEnum(event.getDiagnosticCode()) == malfunction){
                counter++;
            }
        }
        return counter;
    }




    private void saveMalfunction(Malfunction malfunction){
        try{
            eldMandateController.createMalfunctionForLoggedInUsers(DateUtility.getCurrentDateTimeWithSecondsUTC(), malfunction);
        } catch (Throwable e){
            throw new RuntimeException(e);
        }
    }

    private void clearMalfunction(Malfunction malfunction){
        try{
            eldMandateController.clearMalfunctionForLoggedInUsers(DateUtility.getCurrentDateTimeWithSecondsUTC(), malfunction);
        }catch (Throwable e){
            throw new RuntimeException(e);
        }
    }

    @Test
    public void addUnassignedMalfunctionToLog_single_user() {
        saveMalfunction(Malfunction.TIMING_COMPLIANCE);

        EmployeeLog log = createLog(user1);

        int unassignedLogMalfunctions = getTotalMalfunctionEvents(-1, Malfunction.TIMING_COMPLIANCE);
        assertEquals("one unassignned malfunction", 1, unassignedLogMalfunctions);
        int empLogMalfunctions = getTotalMalfunctionEvents(log.getPrimaryKey(), Malfunction.TIMING_COMPLIANCE);
        assertEquals("malfunction wont be on new log", 0, empLogMalfunctions);

        eldMandateController.addUnassignedMalfunctionsToNewLog(log);

        unassignedLogMalfunctions = getTotalMalfunctionEvents(0, Malfunction.TIMING_COMPLIANCE);
        assertEquals("one unassignned malfunction", 0, unassignedLogMalfunctions);

        empLogMalfunctions = getTotalMalfunctionEvents(log.getPrimaryKey(), Malfunction.TIMING_COMPLIANCE);
        assertEquals("malfunction wont be on new log", 1, empLogMalfunctions);
    }


    @Test
    public void addUnassignedMalfunctionToLog_two_users() {
        saveMalfunction(Malfunction.TIMING_COMPLIANCE);


        EmployeeLog log = createLog(user1);

        int unassignedLogMalfunctions = getTotalMalfunctionEvents(-1, Malfunction.TIMING_COMPLIANCE);
        assertEquals("one unassignned malfunction", 1, unassignedLogMalfunctions);

        int empLogMalfunctions = getTotalMalfunctionEvents(log.getPrimaryKey(), Malfunction.TIMING_COMPLIANCE);
        assertEquals("malfunction wont be on new log", 0, empLogMalfunctions);

        eldMandateController.addUnassignedMalfunctionsToNewLog(log);

        unassignedLogMalfunctions = getTotalMalfunctionEvents(0, Malfunction.TIMING_COMPLIANCE);
        assertEquals("one unassigned malfunction", 0, unassignedLogMalfunctions);

        empLogMalfunctions = getTotalMalfunctionEvents(log.getPrimaryKey(), Malfunction.TIMING_COMPLIANCE);
        assertEquals("malfunction should on new log", 1, empLogMalfunctions);


        User user2 = addMockTestUser(2);
        EmployeeLog log2 = createLog(user2);
        //this is kinda gross... it is what the app does...
        GlobalState.getInstance().setCurrentEmployeeLog(log2);
        GlobalState.getInstance().setCurrentUser(user2);

        eldMandateController.addUnassignedMalfunctionsToNewLog(log2);

        unassignedLogMalfunctions = getTotalMalfunctionEvents(0, Malfunction.TIMING_COMPLIANCE);
        assertEquals("one unassignned malfunction", 0, unassignedLogMalfunctions);

        empLogMalfunctions = getTotalMalfunctionEvents(log.getPrimaryKey(), Malfunction.TIMING_COMPLIANCE);
        assertEquals("malfunction should on new log", 1, empLogMalfunctions);

        int emp2LogMalfunctions = getTotalMalfunctionEvents(log2.getPrimaryKey(), Malfunction.TIMING_COMPLIANCE);
        assertEquals("malfunction should on new log", 1, emp2LogMalfunctions);
    }


}