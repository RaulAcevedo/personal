package com.jjkeller.kmbapi;
import android.os.Bundle;

import com.jjkeller.kmbapi.common.TestBase;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.configuration.LoginCredentials;
import com.jjkeller.kmbapi.configuration.UserState;
import com.jjkeller.kmbapi.controller.ControllerFactory;
import com.jjkeller.kmbapi.controller.EOBR.EobrReader;
import com.jjkeller.kmbapi.controller.EmployeeLogEldMandateController;
import com.jjkeller.kmbapi.controller.EobrConfigController;
import com.jjkeller.kmbapi.controller.dataaccess.EmployeeLogEldEventFacade;
import com.jjkeller.kmbapi.controller.dataaccess.db.EmployeeLogEldEventPersist;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.controller.utility.EmployeeLogUtilities;
import com.jjkeller.kmbapi.employeelogeldevents.Enums;
import com.jjkeller.kmbapi.enums.DriverTypeEnum;

import com.jjkeller.kmbapi.enums.RuleSetTypeEnum;
import com.jjkeller.kmbapi.enums.TimeZoneEnum;
import com.jjkeller.kmbapi.featuretoggle.FeatureToggleService;
import org.robolectric.RuntimeEnvironment;
import com.jjkeller.kmbapi.proxydata.CompanyConfigSettings;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;

import junit.framework.Assert;

import org.joda.time.IllegalInstantException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Calendar;
import java.util.NoSuchElementException;

import static org.junit.Assert.assertEquals;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
/**
 * Created by rab5795 on 3/24/2017.
 */

@RunWith(RobolectricTestRunner.class)
@Config(application = GlobalState.class,
        manifest = "/build/intermediates/bundles/encompass/debug/AndroidManifest.xml"
)
public class GpsPrecisionTest extends TestBase {

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

    }

    @After
    public void tearDown() throws NoSuchFieldException, IllegalAccessException {
        closeDatabase(app);
    }

    @Test
    public void testReducedGpsPrecision() throws Throwable {
        Date startDateTime = DateUtility.getCurrentDateTimeUTC();

        float latitude = 44.223153f;
        float longitude = -88.387922f;

        eldEventFacade.Save(setEvent(startDateTime, latitude, longitude));

        latitude = 45.155544f;
        longitude = -87.491922f;
        eldEventFacade.Save(setEvent(addSeconds(startDateTime, 10), latitude, longitude));

        latitude = -18.81083f;
        longitude = -87.30394f;
        eldEventFacade.Save(setEvent(addSeconds(startDateTime, 20), latitude, longitude));

        latitude = 43.7826936f;
        longitude = -89.5582753f;
        eldEventFacade.Save(setEvent(addSeconds(startDateTime, 30), latitude, longitude));


        EmployeeLogEldEvent[] logEldEvents = eldEventFacade.GetByEventTypes(10, Arrays.asList(new Integer[]{Enums.EmployeeLogEldEventType.EnginePowerUpPowerDown.getValue(),
                        Enums.EmployeeLogEldEventType.LoginLogout.getValue()}),
                Arrays.asList(new Integer[]{Enums.EmployeeLogEldEventRecordStatus.Active.getValue()}));

        double latFromDatabaseToEvent = logEldEvents[0].getLatitude();
        double longFromDatabaseToEvent = logEldEvents[0].getLongitude();

        Assert.assertEquals(44.2, latFromDatabaseToEvent, 0.0001f);
        Assert.assertEquals(-88.4, longFromDatabaseToEvent, 0.0001f);

        latFromDatabaseToEvent = logEldEvents[1].getLatitude();
        longFromDatabaseToEvent = logEldEvents[1].getLongitude();

        Assert.assertEquals(45.2, latFromDatabaseToEvent, 0.0001f);
        Assert.assertEquals(-87.5, longFromDatabaseToEvent, 0.0001f);

        latFromDatabaseToEvent = logEldEvents[2].getLatitude();
        longFromDatabaseToEvent = logEldEvents[2].getLongitude();

        Assert.assertEquals(-18.8, latFromDatabaseToEvent, 0.0001f);
        Assert.assertEquals(-87.3, longFromDatabaseToEvent, 0.0001f);

        latFromDatabaseToEvent = logEldEvents[3].getLatitude();
        longFromDatabaseToEvent = logEldEvents[3].getLongitude();

        Assert.assertEquals(43.8, latFromDatabaseToEvent, 0.0001f);
        Assert.assertEquals(-89.6, longFromDatabaseToEvent, 0.0001f);

        String.valueOf(longFromDatabaseToEvent);
        String.valueOf(latFromDatabaseToEvent);
    }

    private Date addSeconds(Date dt, Integer seconds) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        cal.add(Calendar.SECOND, seconds);
        return cal.getTime();
    }

    private EmployeeLogEldEvent setEvent(Date date, float latitude, float longitude){
        //lat and long are float in status record

        //Reduce Precision
        double lat = EmployeeLogUtilities.GetReducedPrecisionGPSForDouble( new Double(latitude), true);
        double lon = EmployeeLogUtilities.GetReducedPrecisionGPSForDouble(new Double(longitude), true);


        EmployeeLogEldEvent event = new EmployeeLogEldEvent();
        event.setLogKey(10);
        event.setLatitude(lat);
        event.setLongitude(lon);
        event.setIsManuallyEditedByKMBUser(false);
        event.setIsEventDateTimeValidated(true);
        event.setEventDateTime(date);
        event.setEventCode(1);
        event.setEventRecordStatus(1);
        event.setEventType(Enums.EmployeeLogEldEventType.EnginePowerUpPowerDown);

        event.setEventSequenceIDNumber(1);
        return event;
    }


}
