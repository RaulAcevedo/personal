package com.jjkeller.kmbapi.controller;

import android.app.Application;

import com.jjkeller.kmbapi.common.ITimeKeeper;
import com.jjkeller.kmbapi.common.TimeKeeper;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.configuration.LoginCredentials;
import com.jjkeller.kmbapi.configuration.UserState;
import com.jjkeller.kmbapi.controller.dataaccess.db.EmployeeLogEldEventPersist;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.enums.DriverTypeEnum;
import com.jjkeller.kmbapi.enums.RuleSetTypeEnum;
import com.jjkeller.kmbapi.enums.TimeZoneEnum;
import com.jjkeller.kmbapi.featuretoggle.FeatureToggleService;
import com.jjkeller.kmbapi.kmbeobr.StatusBuffer;
import com.jjkeller.kmbapi.kmbeobr.StatusRecord;
import com.jjkeller.kmbapi.proxydata.CompanyConfigSettings;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;

import junit.framework.Assert;

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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(application = GlobalState.class,
        manifest = "/build/intermediates/bundles/encompass/debug/AndroidManifest.xml"
)
public class EmployeeLogEldMandateControllerUnitTest {
    protected GlobalState app;
    protected FeatureToggleService ftService;
    protected EmployeeLogEldMandateController employeeLogEldMandateController;

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

        employeeLogEldMandateController = ControllerFactory.getInstance().getEmployeeLogEldMandateController();

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

    @Test
    public void GetTotalEngineHours_should_return_null() {
        Double result = employeeLogEldMandateController.GetTotalEngineHours(null);
        Assert.assertNull(result);
    }

    @Test
    public void GetTotalEngineHours_should_handle_zero() {
        StatusBuffer statusBuffer = new StatusBuffer();
        statusBuffer.setEngineOnTimeSeconds(0);
        Double result = employeeLogEldMandateController.GetTotalEngineHours(statusBuffer);
        Assert.assertEquals(0.0, result);
    }

    @Test
    public void GetTotalEngineHours_should_round_down() {
        StatusBuffer statusBuffer = new StatusBuffer();
        Double result;

        statusBuffer.setEngineOnTimeSeconds(1);
        result = employeeLogEldMandateController.GetTotalEngineHours(statusBuffer);
        Assert.assertEquals(0.0, result);

        statusBuffer.setEngineOnTimeSeconds(359);
        result = employeeLogEldMandateController.GetTotalEngineHours(statusBuffer);
        Assert.assertEquals(0.0, result);

        statusBuffer.setEngineOnTimeSeconds(360);
        result = employeeLogEldMandateController.GetTotalEngineHours(statusBuffer);
        Assert.assertEquals(0.1, result);

        statusBuffer.setEngineOnTimeSeconds(361);
        result = employeeLogEldMandateController.GetTotalEngineHours(statusBuffer);
        Assert.assertEquals(0.1, result);

        statusBuffer.setEngineOnTimeSeconds((12345 * 3600) + (42 * 60) - 1); // 1 second before 12345.7 hours = 12345.6
        result = employeeLogEldMandateController.GetTotalEngineHours(statusBuffer);
        Assert.assertEquals(12345.6, result);
    }

    @Test
    public void GetTotalEngineHours_should_fall_back_to_runTimeSeconds_with_zero_engineOnTime() {
        StatusBuffer statusBuffer = new StatusBuffer();
        statusBuffer.setEngineOnTimeSeconds(0);
        statusBuffer.setRunTimeSeconds(7200);

        Double result = employeeLogEldMandateController.GetTotalEngineHours(statusBuffer);
        Assert.assertEquals(2.0, result);
    }

    @Test
    public void GetTotalVehicleMiles_should_return_null() {
        Float result = employeeLogEldMandateController.GetTotalVehicleMiles(null);
        Assert.assertNull(result);
    }

    @Test
    public void GetTotalVehicleMiles_should_return_correct_odometer_miles() {
        StatusRecord statusRecord = new StatusRecord();
        statusRecord.setOdometerReading(123.9f);
        Float result = employeeLogEldMandateController.GetTotalVehicleMiles(statusRecord);

        Assert.assertEquals(123.9f, result);
    }

    public void closeDatabase(Application app) throws NoSuchFieldException, IllegalAccessException {
        //needed to clean out old data with roboeletric
        //feels hacky... TODO find better way.
        EmployeeLogEldEventPersist<EmployeeLogEldEvent> persist = new EmployeeLogEldEventPersist<EmployeeLogEldEvent>(EmployeeLogEldEvent.class, app);
        persist.open();
        persist.closeDatabase();
    }
}
