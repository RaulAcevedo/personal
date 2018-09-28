package com.jjkeller.kmbapi.controller;

import com.jjkeller.kmbapi.common.TestBase;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.configuration.LoginCredentials;
import com.jjkeller.kmbapi.configuration.UserState;
import com.jjkeller.kmbapi.controller.dataaccess.EmployeeLogEldEventFacade;
import com.jjkeller.kmbapi.controller.dataaccess.db.EmployeeLogEldEventPersist;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.employeelogeldevents.Enums;
import com.jjkeller.kmbapi.enums.DriverTypeEnum;
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(application = GlobalState.class,
        manifest = "/build/intermediates/bundles/encompass/debug/AndroidManifest.xml"
)
public class EmployeeLogEldEventMalfunctionDetectionTest extends TestBase {
    private GlobalState app;
    private FeatureToggleService ftService;
    private EmployeeLogEldEventFacade eldEventFacade;


    private  EmployeeLogEldMandateController eldMandateController;


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

        app.setCurrentUser(user);

        ArrayList<User> userList = new ArrayList<User>();
        userList.add(user);
        app.setLoggedInUserList(userList);

        eldEventFacade = new EmployeeLogEldEventFacade(app, user);

        eldMandateController = ControllerFactory.getInstance().getEmployeeLogEldMandateController();
    }


    @After
    public void tearDown() throws NoSuchFieldException, IllegalAccessException {
        //needed to clean out old data with roboeletric
        //feels hacky... TODO find better way.
        EmployeeLogEldEventPersist<EmployeeLogEldEvent> persist = new EmployeeLogEldEventPersist<EmployeeLogEldEvent>(EmployeeLogEldEvent.class, app);
        persist.open();
        persist.closeDatabase();
    }


    private int getTotalMalfunctionEvents(){
        EmployeeLogEldEvent[]  logEldEvents = eldEventFacade.GetByEventTypes(-1,
                Arrays.asList(new Integer[] {Enums.EmployeeLogEldEventType.Malfunction_DataDiagnosticDetection.getValue()}),
                Arrays.asList(new Integer[] {Enums.EmployeeLogEldEventRecordStatus.Active.getValue()})
        );

        assertNotNull(logEldEvents);
        return logEldEvents.length;
    }


    private void saveMalfunction(Malfunction malfunction){
        try{
            eldMandateController.createMalfunctionForLoggedInUsers(
                    DateUtility.getCurrentDateTimeWithSecondsUTC()
                    , malfunction);

        }catch (Throwable e){

        }
    }


    private void clearMalfunction(Malfunction malfunction){

        try{
            eldMandateController.clearMalfunctionForLoggedInUsers(
                     DateUtility.getCurrentDateTimeWithSecondsUTC()
                    , malfunction);

        }catch (Throwable e){

        }
    }

    @Test
    public void testCantLogMalfunctionTwice() {
        saveMalfunction(Malfunction.TIMING_COMPLIANCE);
        saveMalfunction(Malfunction.TIMING_COMPLIANCE);
        int malfunctionsLogged = getTotalMalfunctionEvents();
        assertEquals("can only log a malfunction once", 1, malfunctionsLogged);
        EmployeeLog currentLog = GlobalState.getInstance().getCurrentEmployeeLog();
        assertTrue("should be malfunctioning", eldMandateController.isMalfunctioning(currentLog, Malfunction.TIMING_COMPLIANCE));
    }

    @Test
    public void testCanLogDifferentTypesOfMalfunctions() {
        saveMalfunction(Malfunction.TIMING_COMPLIANCE);
        saveMalfunction(Malfunction.TIMING_COMPLIANCE);
        saveMalfunction(Malfunction.POSITIONING_COMPLIANCE);
        int malfunctionsLogged = getTotalMalfunctionEvents();
        assertEquals("both malfunctions should be logged", 2, malfunctionsLogged);

        EmployeeLog currentLog = GlobalState.getInstance().getCurrentEmployeeLog();
        assertTrue("should be malfunctioning", eldMandateController.isMalfunctioning(currentLog, Malfunction.POSITIONING_COMPLIANCE));
        assertTrue("should be malfunctioning", eldMandateController.isMalfunctioning(currentLog, Malfunction.TIMING_COMPLIANCE));

    }



    @Test
    public void testCantClearAMalfunctionThatHasNotHappened() {
        clearMalfunction(Malfunction.TIMING_COMPLIANCE);
        int malfunctionsLogged = getTotalMalfunctionEvents();
        assertEquals("both malfunctions should be logged", 0, malfunctionsLogged);
    }



    @Test
    public void testDetectMalfunctionAndClear() {
        saveMalfunction(Malfunction.TIMING_COMPLIANCE);
        clearMalfunction(Malfunction.TIMING_COMPLIANCE);

        int malfunctionsLogged = getTotalMalfunctionEvents();
        assertEquals("should have 2... detect and cleared ", 2, malfunctionsLogged);

        EmployeeLog currentLog = GlobalState.getInstance().getCurrentEmployeeLog();
        assertFalse("should not be malfunctioning", eldMandateController.isMalfunctioning(currentLog, Malfunction.TIMING_COMPLIANCE));
    }





    @Test
    public void testDetectClearDetect() {
        saveMalfunction(Malfunction.TIMING_COMPLIANCE);
        clearMalfunction(Malfunction.TIMING_COMPLIANCE);
        saveMalfunction(Malfunction.TIMING_COMPLIANCE);

        int malfunctionsLogged = getTotalMalfunctionEvents();
        assertEquals("should have 3... detect and cleared detected again", 3, malfunctionsLogged);

        EmployeeLog currentLog = GlobalState.getInstance().getCurrentEmployeeLog();
        assertTrue("should not be malfunctioning", eldMandateController.isMalfunctioning(currentLog, Malfunction.TIMING_COMPLIANCE));
    }




    @Test
    public void testDeviceReallyGoingNuts() {
        saveMalfunction(Malfunction.TIMING_COMPLIANCE);
        saveMalfunction(Malfunction.TIMING_COMPLIANCE);
        saveMalfunction(Malfunction.TIMING_COMPLIANCE);

        saveMalfunction(Malfunction.POSITIONING_COMPLIANCE);
        clearMalfunction(Malfunction.POSITIONING_COMPLIANCE);

        saveMalfunction(Malfunction.TIMING_COMPLIANCE);
        saveMalfunction(Malfunction.TIMING_COMPLIANCE);
        saveMalfunction(Malfunction.TIMING_COMPLIANCE);

        saveMalfunction(Malfunction.POSITIONING_COMPLIANCE);
        saveMalfunction(Malfunction.POSITIONING_COMPLIANCE);
        saveMalfunction(Malfunction.POSITIONING_COMPLIANCE);
        saveMalfunction(Malfunction.POSITIONING_COMPLIANCE);
        clearMalfunction(Malfunction.POSITIONING_COMPLIANCE);

        saveMalfunction(Malfunction.POSITIONING_COMPLIANCE);
        clearMalfunction(Malfunction.POSITIONING_COMPLIANCE);

        saveMalfunction(Malfunction.POSITIONING_COMPLIANCE);
        clearMalfunction(Malfunction.POSITIONING_COMPLIANCE);

        saveMalfunction(Malfunction.POSITIONING_COMPLIANCE);

        saveMalfunction(Malfunction.POWER_COMPLIANCE);
        clearMalfunction(Malfunction.POWER_COMPLIANCE);
        saveMalfunction(Malfunction.POWER_COMPLIANCE);

        EmployeeLog currentLog = GlobalState.getInstance().getCurrentEmployeeLog();
        assertTrue("POSITIONINGCOMPLIANCE should be malfunctioning", eldMandateController.isMalfunctioning(currentLog, Malfunction.POSITIONING_COMPLIANCE));
        assertTrue("POWERCOMPLIANCE should be malfunctioning", eldMandateController.isMalfunctioning(currentLog, Malfunction.POWER_COMPLIANCE));
        assertTrue("TIMINGCOMPLIANCE should be malfunctioning", eldMandateController.isMalfunctioning(currentLog, Malfunction.TIMING_COMPLIANCE));

    }

}