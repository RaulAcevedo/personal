package com.jjkeller.kmb.test.kmbapi.calcengine;

import android.content.Context;

import com.jjkeller.kmb.test.KMBRoboElectricTestRunner;
import com.jjkeller.kmb.test.KmbRoboTestBase;
import com.jjkeller.kmbapi.calcengine.Exempt150AirMileLogValidator;
import com.jjkeller.kmbapi.calcengine.ExemptLogValidatorBase;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.configuration.LoginCredentials;
import com.jjkeller.kmbapi.controller.LoginController;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.enums.DriverTypeEnum;
import com.jjkeller.kmbapi.enums.DutyStatusEnum;
import com.jjkeller.kmbapi.enums.RuleSetTypeEnum;
import com.jjkeller.kmbapi.enums.TimeZoneEnum;
import com.jjkeller.kmbapi.proxydata.CompanyConfigSettings;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

@RunWith(KMBRoboElectricTestRunner.class)
public class Exempt150AirMileLogValidatorTest extends KmbRoboTestBase {

	/*
     * Some of the tests in this class use a mock IEmployeeLogFacade impelementation.
	 * This is because these tests were previously putting multiple days' worth of log
	 * events on a single employee log, which worked due to a happy coincidence in the validator
	 * base's initialization method. Namely, that when it tried retrieving the next day's log,
	 * it wasn't found so it created a midnight offduty event, which ended up at the very end
	 * of the log event list.  But now that the validator makes sure that the log event list
	 * is sorted in chronoligical order, this midnight event was getting inserted in the middle of
	 * the events in the test cases, and causing problems.
	 */

    @Before
    public void setUp() {


        //set up the mock global state so that we can inject our company config settings
        MockGlobalState mockGS = new MockGlobalState(GlobalState.getInstance());

        // set up a mock context for this test
        User user = new User();
        user.setCredentials(new LoginCredentials());
        user.getCredentials().setPrimaryKey(0);
        user.setHomeTerminalTimeZone(new TimeZoneEnum(TimeZoneEnum.CENTRALSTANDARDTIME));

        //Context ctx = GlobalState.getInstance().getApplicationContext();
        new LoginController(null).setCurrentUser(user);

        CompanyConfigSettings settings = new CompanyConfigSettings();
        settings.setDailyLogStartTime("00:00");
        mockGS.setCompanyConfigSettings(null, settings);

        // new LoginController(ctx).AuthenticateUser(user, "exempt", "aaaaaa", false);
    }

    // ---------------------------------------------
    // unit tests
    @Test
    public void test_GetEndTime() throws ParseException {
        EmployeeLogEldEvent[] logEvents = new EmployeeLogEldEvent[]{
                new EmployeeLogEldEvent(),
                new EmployeeLogEldEvent(),
                new EmployeeLogEldEvent()
        };

        logEvents[0].setStartTime(DateUtility.getHomeTerminalDateTimeFormat().parse("06/11/2015 08:00:00"));
        logEvents[1].setStartTime(DateUtility.getHomeTerminalDateTimeFormat().parse("06/11/2015 09:00:00"));
        logEvents[2].setStartTime(DateUtility.getHomeTerminalDateTimeFormat().parse("06/11/2015 10:00:00"));

        Date event0EndTime = ExemptLogValidatorBase.GetEndTime(logEvents, 0);
        Assert.assertEquals(0, DateUtility.getHomeTerminalDateTimeFormat().parse("06/11/2015 09:00:00").getTime() - event0EndTime.getTime());

        Date event1EndTime = ExemptLogValidatorBase.GetEndTime(logEvents, 1);
        Assert.assertEquals(0, DateUtility.getHomeTerminalDateTimeFormat().parse("06/11/2015 10:00:00").getTime() - event1EndTime.getTime());
    }

    @Test
    public void test_GetDuration() throws ParseException {
        EmployeeLogEldEvent[] logEvents = new EmployeeLogEldEvent[]{
                new EmployeeLogEldEvent(),
                new EmployeeLogEldEvent(),
                new EmployeeLogEldEvent()
        };

        logEvents[0].setStartTime(DateUtility.getHomeTerminalDateTimeFormat().parse("06/11/2015 08:00:00"));
        logEvents[1].setStartTime(DateUtility.getHomeTerminalDateTimeFormat().parse("06/11/2015 09:00:00"));
        logEvents[2].setStartTime(DateUtility.getHomeTerminalDateTimeFormat().parse("06/11/2015 10:00:00"));

        double event0DurationHours = DateUtility.ConvertMillisecondsToHours(ExemptLogValidatorBase.GetDuration(logEvents, 0));
        Assert.assertEquals(1.0, event0DurationHours);

        double event1DurationHours = DateUtility.ConvertMillisecondsToHours(ExemptLogValidatorBase.GetDuration(logEvents, 1));
        Assert.assertEquals(1.0, event1DurationHours);
    }

    private EmployeeLog CreateEmployeeLog(RuleSetTypeEnum ruleSet, DriverTypeEnum driverType, String logDate, Map<String, DutyStatusEnum> statuses, SimpleDateFormat dateFormat) throws ParseException {
        if (dateFormat == null)
            dateFormat = DateUtility.getHomeTerminalDateTimeFormat();

        // create employee log
        EmployeeLog employeeLog = new EmployeeLog();
        employeeLog.setRuleset(ruleSet);
        employeeLog.setDriverType(driverType);
        employeeLog.setLogDate(DateUtility.getHomeTerminalShortDateFormat().parse(logDate));
        employeeLog.setHasReturnedToLocation(true);

        List<EmployeeLogEldEvent> logEventList = new ArrayList<>();

        for (Map.Entry<String, DutyStatusEnum> status : statuses.entrySet()) {
            String timeString = status.getKey();
            DutyStatusEnum dutyStatus = status.getValue();

            EmployeeLogEldEvent logEvent = new EmployeeLogEldEvent();
            logEvent.setStartTime(dateFormat.parse(timeString));
            logEvent.setDutyStatusEnum(dutyStatus);

            logEventList.add(logEvent);
        }

        EmployeeLogEldEvent[] logEvents = new EmployeeLogEldEvent[logEventList.size()];
        logEvents = logEventList.toArray(logEvents);


        employeeLog.getEldEventList().setEldEventList(logEvents);

        return employeeLog;
    }

    private EmployeeLog CreateEmployeeLog(RuleSetTypeEnum ruleSet, DriverTypeEnum driverType, String logDate, Map<String, DutyStatusEnum> statuses) throws ParseException {
        return CreateEmployeeLog(ruleSet, driverType, logDate, statuses, null);
    }

    @Test
    public void test_HasNotReturnedToLocation() throws ParseException {
        Map<String, DutyStatusEnum> dutyStatuses = new LinkedHashMap<>();
        dutyStatuses.put("06/15/2015 01:00:00", new DutyStatusEnum(DutyStatusEnum.ONDUTY));
        dutyStatuses.put("06/15/2015 02:00:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        EmployeeLog employeeLog = CreateEmployeeLog(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), new DriverTypeEnum(DriverTypeEnum.PROPERTYCARRYING), "06/15/2015", dutyStatuses);

        Exempt150AirMileLogValidator validator = new Exempt150AirMileLogValidator();
        boolean isExemptLogEligible = validator.IsExemptLogEligible(employeeLog, employeeLog.getEldEventList());
        Assert.assertTrue(isExemptLogEligible);

        employeeLog.setHasReturnedToLocation(false);
        isExemptLogEligible = validator.IsExemptLogEligible(employeeLog, employeeLog.getEldEventList());
        Assert.assertFalse(isExemptLogEligible);
    }

    @Test
    public void test_Smoke() throws ParseException {
        Map<String, DutyStatusEnum> dutyStatuses = new LinkedHashMap<>();
        dutyStatuses.put("06/15/2015 01:00:00", new DutyStatusEnum(DutyStatusEnum.ONDUTY));
        dutyStatuses.put("06/15/2015 02:00:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        EmployeeLog employeeLog = CreateEmployeeLog(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), new DriverTypeEnum(DriverTypeEnum.PROPERTYCARRYING), "06/15/2015", dutyStatuses);

        Exempt150AirMileLogValidator validator = new Exempt150AirMileLogValidator();
        boolean isExemptLogEligible = validator.IsExemptLogEligible(employeeLog, employeeLog.getEldEventList());
        Assert.assertTrue(isExemptLogEligible);
    }

    @Test
    public void test_Passenger() throws ParseException {
        Map<String, DutyStatusEnum> dutyStatuses = new LinkedHashMap<>();
        dutyStatuses.put("06/15/2015 01:00:00", new DutyStatusEnum(DutyStatusEnum.ONDUTY));
        dutyStatuses.put("06/15/2015 02:00:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        EmployeeLog employeeLog = CreateEmployeeLog(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), new DriverTypeEnum(DriverTypeEnum.PASSENGERCARRYING), "06/15/2015", dutyStatuses);

        Exempt150AirMileLogValidator validator = new Exempt150AirMileLogValidator();
        boolean isExemptLogEligible = validator.IsExemptLogEligible(employeeLog, employeeLog.getEldEventList());
        Assert.assertFalse(isExemptLogEligible);
    }

    @Test
    public void test_Property12hOn() throws ParseException {
        Map<String, DutyStatusEnum> dutyStatuses = new LinkedHashMap<>();
        dutyStatuses.put("06/15/2015 01:00:00", new DutyStatusEnum(DutyStatusEnum.ONDUTY));
        dutyStatuses.put("06/15/2015 13:00:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        EmployeeLog employeeLog = CreateEmployeeLog(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), new DriverTypeEnum(DriverTypeEnum.PROPERTYCARRYING), "06/15/2015", dutyStatuses);

        Exempt150AirMileLogValidator validator = new Exempt150AirMileLogValidator();
        boolean isExemptLogEligible = validator.IsExemptLogEligible(employeeLog, employeeLog.getEldEventList());
        Assert.assertTrue(isExemptLogEligible);
    }

    @Test
    public void test_Property13hOn() throws ParseException {
        Map<String, DutyStatusEnum> dutyStatuses = new LinkedHashMap<>();
        dutyStatuses.put("06/15/2015 01:00:00", new DutyStatusEnum(DutyStatusEnum.ONDUTY));
        dutyStatuses.put("06/15/2015 14:00:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        EmployeeLog employeeLog = CreateEmployeeLog(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), new DriverTypeEnum(DriverTypeEnum.PROPERTYCARRYING), "06/15/2015", dutyStatuses);

        Exempt150AirMileLogValidator validator = new Exempt150AirMileLogValidator();
        boolean isExemptLogEligible = validator.IsExemptLogEligible(employeeLog, employeeLog.getEldEventList());
        Assert.assertTrue(isExemptLogEligible);
    }

    @Test
    public void test_Property14hOn() throws ParseException {
        Map<String, DutyStatusEnum> dutyStatuses = new LinkedHashMap<>();
        dutyStatuses.put("06/15/2015 01:00:00", new DutyStatusEnum(DutyStatusEnum.ONDUTY));
        dutyStatuses.put("06/15/2015 15:00:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        EmployeeLog employeeLog = CreateEmployeeLog(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), new DriverTypeEnum(DriverTypeEnum.PROPERTYCARRYING), "06/15/2015", dutyStatuses);

        Exempt150AirMileLogValidator validator = new Exempt150AirMileLogValidator();
        boolean isExemptLogEligible = validator.IsExemptLogEligible(employeeLog, employeeLog.getEldEventList());
        Assert.assertTrue(isExemptLogEligible);
    }

    @Test
    public void test_Property15hOn() throws ParseException {
        Map<String, DutyStatusEnum> dutyStatuses = new LinkedHashMap<>();
        dutyStatuses.put("06/15/2015 01:00:00", new DutyStatusEnum(DutyStatusEnum.ONDUTY));
        dutyStatuses.put("06/15/2015 16:00:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        EmployeeLog employeeLog = CreateEmployeeLog(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), new DriverTypeEnum(DriverTypeEnum.PROPERTYCARRYING), "06/15/2015", dutyStatuses);

        Exempt150AirMileLogValidator validator = new Exempt150AirMileLogValidator();
        boolean isExemptLogEligible = validator.IsExemptLogEligible(employeeLog, employeeLog.getEldEventList());
        Assert.assertTrue(isExemptLogEligible);
    }

    @Test
    public void test_Property16hOn() throws ParseException {
        Map<String, DutyStatusEnum> dutyStatuses = new LinkedHashMap<>();
        dutyStatuses.put("06/15/2015 01:00:00", new DutyStatusEnum(DutyStatusEnum.ONDUTY));
        dutyStatuses.put("06/15/2015 17:00:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        EmployeeLog employeeLog = CreateEmployeeLog(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), new DriverTypeEnum(DriverTypeEnum.PROPERTYCARRYING), "06/15/2015", dutyStatuses);

        Exempt150AirMileLogValidator validator = new Exempt150AirMileLogValidator();
        boolean isExemptLogEligible = validator.IsExemptLogEligible(employeeLog, employeeLog.getEldEventList());
        Assert.assertTrue(isExemptLogEligible);
    }

    @Test
    public void test_Property20hOn() throws ParseException {
        Map<String, DutyStatusEnum> dutyStatuses = new LinkedHashMap<>();
        dutyStatuses.put("06/15/2015 01:00:00", new DutyStatusEnum(DutyStatusEnum.ONDUTY));
        dutyStatuses.put("06/15/2015 21:00:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        EmployeeLog employeeLog = CreateEmployeeLog(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), new DriverTypeEnum(DriverTypeEnum.PROPERTYCARRYING), "06/15/2015", dutyStatuses);

        Exempt150AirMileLogValidator validator = new Exempt150AirMileLogValidator();
        boolean isExemptLogEligible = validator.IsExemptLogEligible(employeeLog, employeeLog.getEldEventList());
        Assert.assertTrue(isExemptLogEligible);
    }

    @Test
    public void test_Property12hOnSpanningMidnight() throws ParseException {
        Map<String, DutyStatusEnum> dutyStatuses = new LinkedHashMap<>();
        dutyStatuses.put("06/15/2015 21:00:00", new DutyStatusEnum(DutyStatusEnum.ONDUTY));
        dutyStatuses.put("06/16/2015 09:00:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        EmployeeLog employeeLog = CreateEmployeeLog(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), new DriverTypeEnum(DriverTypeEnum.PROPERTYCARRYING), "06/15/2015", dutyStatuses);

        Exempt150AirMileLogValidator validator = new Exempt150AirMileLogValidator();
        boolean isExemptLogEligible = validator.IsExemptLogEligible(employeeLog, employeeLog.getEldEventList());
        Assert.assertTrue(isExemptLogEligible);
    }

    @Test
    public void test_Property12hOn10hOff12hOn() throws ParseException {
        Map<String, DutyStatusEnum> dutyStatuses = new LinkedHashMap<>();
        dutyStatuses.put("06/15/2015 11:00:00", new DutyStatusEnum(DutyStatusEnum.ONDUTY));
        dutyStatuses.put("06/15/2015 23:00:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        dutyStatuses.put("06/16/2015 09:00:00", new DutyStatusEnum(DutyStatusEnum.ONDUTY));
        dutyStatuses.put("06/16/2015 21:00:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        EmployeeLog employeeLog = CreateEmployeeLog(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), new DriverTypeEnum(DriverTypeEnum.PROPERTYCARRYING), "06/15/2015", dutyStatuses);

        Exempt150AirMileLogValidator validator = new Exempt150AirMileLogValidator();
        boolean isExemptLogEligible = validator.IsExemptLogEligible(employeeLog, employeeLog.getEldEventList());
        Assert.assertTrue(isExemptLogEligible);
    }

    @Test
    public void test_Property12hOn10hOff12hOnSpanningMidnight() throws ParseException {
        Map<String, DutyStatusEnum> dutyStatuses = new LinkedHashMap<>();
        dutyStatuses.put("06/15/2015 13:00:00", new DutyStatusEnum(DutyStatusEnum.ONDUTY));
        dutyStatuses.put("06/16/2015 01:00:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        dutyStatuses.put("06/16/2015 11:00:00", new DutyStatusEnum(DutyStatusEnum.ONDUTY));
        dutyStatuses.put("06/16/2015 23:00:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        EmployeeLog employeeLog = CreateEmployeeLog(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), new DriverTypeEnum(DriverTypeEnum.PROPERTYCARRYING), "06/15/2015", dutyStatuses);

        Exempt150AirMileLogValidator validator = new Exempt150AirMileLogValidator();
        boolean isExemptLogEligible = validator.IsExemptLogEligible(employeeLog, employeeLog.getEldEventList());
        Assert.assertTrue(isExemptLogEligible);
    }

    @Test
    public void test_Property12hPlusOnSpanningMidnight() throws ParseException {
        Map<String, DutyStatusEnum> dutyStatuses = new LinkedHashMap<>();
        dutyStatuses.put("06/15/2015 21:00:00", new DutyStatusEnum(DutyStatusEnum.ONDUTY));
        dutyStatuses.put("06/16/2015 09:01:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        EmployeeLog employeeLog = CreateEmployeeLog(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), new DriverTypeEnum(DriverTypeEnum.PROPERTYCARRYING), "06/15/2015", dutyStatuses);

        Exempt150AirMileLogValidator validator = new Exempt150AirMileLogValidator();
        boolean isExemptLogEligible = validator.IsExemptLogEligible(employeeLog, employeeLog.getEldEventList());
        Assert.assertTrue(isExemptLogEligible);
    }

    @Test
    public void test_Property12hOn9hOff12hOn() throws ParseException {
        Map<String, DutyStatusEnum> dutyStatuses = new LinkedHashMap<>();
        dutyStatuses.put("06/15/2015 11:00:00", new DutyStatusEnum(DutyStatusEnum.ONDUTY));
        dutyStatuses.put("06/15/2015 23:00:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        dutyStatuses.put("06/16/2015 08:00:00", new DutyStatusEnum(DutyStatusEnum.ONDUTY));
        dutyStatuses.put("06/16/2015 20:00:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        EmployeeLog employeeLog = CreateEmployeeLog(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), new DriverTypeEnum(DriverTypeEnum.PROPERTYCARRYING), "06/15/2015", dutyStatuses);

        Exempt150AirMileLogValidator validator = new Exempt150AirMileLogValidator();
        boolean isExemptLogEligible = validator.IsExemptLogEligible(employeeLog, employeeLog.getEldEventList());
        Assert.assertTrue(isExemptLogEligible);
    }

    @Test
    public void test_Property12hOn9hOff12hOnSpanningMidnight() throws ParseException {
        Map<String, DutyStatusEnum> dutyStatuses = new LinkedHashMap<>();
        dutyStatuses.put("06/15/2015 13:00:00", new DutyStatusEnum(DutyStatusEnum.ONDUTY));
        dutyStatuses.put("06/16/2015 01:00:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        dutyStatuses.put("06/16/2015 10:00:00", new DutyStatusEnum(DutyStatusEnum.ONDUTY));
        dutyStatuses.put("06/16/2015 22:00:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        EmployeeLog employeeLog = CreateEmployeeLog(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), new DriverTypeEnum(DriverTypeEnum.PROPERTYCARRYING), "06/15/2015", dutyStatuses);

        Exempt150AirMileLogValidator validator = new Exempt150AirMileLogValidator();
        boolean isExemptLogEligible = validator.IsExemptLogEligible(employeeLog, employeeLog.getEldEventList());
        Assert.assertTrue(isExemptLogEligible);
    }

    @Test
    public void test_Property12hOn9hOff12HourDriving() throws ParseException {
        Map<String, DutyStatusEnum> dutyStatuses = new LinkedHashMap<>();
        dutyStatuses.put("06/15/2015 11:00:00", new DutyStatusEnum(DutyStatusEnum.ONDUTY));
        dutyStatuses.put("06/15/2015 23:00:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        EmployeeLog log15 = CreateEmployeeLog(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), new DriverTypeEnum(DriverTypeEnum.PROPERTYCARRYING), "06/15/2015", dutyStatuses);

        dutyStatuses.clear();
        dutyStatuses.put("06/16/2015 08:00:00", new DutyStatusEnum(DutyStatusEnum.DRIVING));
        dutyStatuses.put("06/16/2015 20:00:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        EmployeeLog log16 = CreateEmployeeLog(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), new DriverTypeEnum(DriverTypeEnum.PROPERTYCARRYING), "06/16/2015", dutyStatuses);

        ArrayList<EmployeeLog> logList = new ArrayList<>();
        logList.add(log15);
        logList.add(log16);
        MockEmployeeLogFacade mockFacade = new MockEmployeeLogFacade();
        mockFacade.setLogList(logList);

        Exempt150AirMileLogValidator validator = new Exempt150AirMileLogValidator(mockFacade);
        boolean isExemptLogEligible = validator.IsExemptLogEligible(log16, log16.getEldEventList());
        Assert.assertFalse(isExemptLogEligible);
    }

    @Test
    public void test_Property12hOn9hOff12HourDrivingSpanningMidnight() throws ParseException {
        Map<String, DutyStatusEnum> dutyStatuses = new LinkedHashMap<>();
        dutyStatuses.put("06/15/2015 13:00:00", new DutyStatusEnum(DutyStatusEnum.ONDUTY));
        EmployeeLog employeeLog = CreateEmployeeLog(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), new DriverTypeEnum(DriverTypeEnum.PROPERTYCARRYING), "06/15/2015", dutyStatuses);

        dutyStatuses.clear();
        dutyStatuses.put("06/16/2015 01:00:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        dutyStatuses.put("06/16/2015 10:00:00", new DutyStatusEnum(DutyStatusEnum.DRIVING));
        dutyStatuses.put("06/16/2015 22:00:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        EmployeeLog nextLog = CreateEmployeeLog(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), new DriverTypeEnum(DriverTypeEnum.PROPERTYCARRYING), "06/16/2015", dutyStatuses);

        ArrayList<EmployeeLog> logList = new ArrayList<>();
        logList.add(employeeLog);
        logList.add(nextLog);
        MockEmployeeLogFacade mockFacade = new MockEmployeeLogFacade();
        mockFacade.setLogList(logList);

        Exempt150AirMileLogValidator validator = new Exempt150AirMileLogValidator(mockFacade);
        boolean isExemptLogEligible = validator.IsExemptLogEligible(employeeLog, employeeLog.getEldEventList());
        Assert.assertFalse(isExemptLogEligible);
    }

    @Test
    public void test_Property12hPlusOnWithBigGap() throws ParseException {
        Map<String, DutyStatusEnum> dutyStatuses = new LinkedHashMap<>();
        dutyStatuses.put("06/15/2015 01:00:00", new DutyStatusEnum(DutyStatusEnum.ONDUTY));
        dutyStatuses.put("06/15/2015 02:00:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        dutyStatuses.put("06/15/2015 10:00:00", new DutyStatusEnum(DutyStatusEnum.ONDUTY));
        dutyStatuses.put("06/15/2015 13:01:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        EmployeeLog employeeLog = CreateEmployeeLog(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), new DriverTypeEnum(DriverTypeEnum.PROPERTYCARRYING), "06/15/2015", dutyStatuses);

        Exempt150AirMileLogValidator validator = new Exempt150AirMileLogValidator();
        boolean isExemptLogEligible = validator.IsExemptLogEligible(employeeLog, employeeLog.getEldEventList());
        Assert.assertTrue(isExemptLogEligible);
    }

    @Test
    public void test_Property11hDriving() throws ParseException {
        Map<String, DutyStatusEnum> dutyStatuses = new LinkedHashMap<>();
        dutyStatuses.put("06/15/2015 01:00:00", new DutyStatusEnum(DutyStatusEnum.DRIVING));
        dutyStatuses.put("06/15/2015 12:00:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        EmployeeLog employeeLog = CreateEmployeeLog(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), new DriverTypeEnum(DriverTypeEnum.PROPERTYCARRYING), "06/15/2015", dutyStatuses);

        Exempt150AirMileLogValidator validator = new Exempt150AirMileLogValidator();
        boolean isExemptLogEligible = validator.IsExemptLogEligible(employeeLog, employeeLog.getEldEventList());
        Assert.assertTrue(isExemptLogEligible);
    }

    @Test
    public void test_Property11hPlusDriving() throws ParseException {
        Map<String, DutyStatusEnum> dutyStatuses = new LinkedHashMap<>();
        dutyStatuses.put("06/15/2015 01:00:00", new DutyStatusEnum(DutyStatusEnum.DRIVING));
        dutyStatuses.put("06/15/2015 12:01:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        EmployeeLog employeeLog = CreateEmployeeLog(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), new DriverTypeEnum(DriverTypeEnum.PROPERTYCARRYING), "06/15/2015", dutyStatuses);

        Exempt150AirMileLogValidator validator = new Exempt150AirMileLogValidator();
        boolean isExemptLogEligible = validator.IsExemptLogEligible(employeeLog, employeeLog.getEldEventList());
        Assert.assertFalse(isExemptLogEligible);
    }

    @Test
    public void test_Property16hExceptionAllowed() throws ParseException {

        //HACK: The short haul exception logic depends on the "date" component of the calculated log
        //start time to be correct.  In older Android builds, particularly in the emulators that run
        //our unit tests, the start date is supposed to be, for example, "2015-06-15 00:00 CDT" but it's
        //being treated as "2015-06-14 23:00 CST".  Technically the same exact moment in time, but when we
        //take the date component to use it as a key in a dictionary, we don't find a log for the 14th.
        //Setting the time zone explicitly when we're generating our log events fixes this issue.
        //However, we can't do that across the board because it causes some of our other unit tests to fail
        //and I don't have time to iron that out right now.
        SimpleDateFormat dateFormat = DateUtility.getHomeTerminalDateTimeFormat();
        dateFormat.setTimeZone(TimeZone.getTimeZone("America/Chicago"));

        Map<String, DutyStatusEnum> dutyStatuses = new LinkedHashMap<>();
        dutyStatuses.put("06/15/2015 13:00:00", new DutyStatusEnum(DutyStatusEnum.ONDUTY));
        dutyStatuses.put("06/15/2015 14:00:00", new DutyStatusEnum(DutyStatusEnum.DRIVING));
        dutyStatuses.put("06/15/2015 15:00:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        EmployeeLog log15 = CreateEmployeeLog(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), new DriverTypeEnum(DriverTypeEnum.PROPERTYCARRYING), "06/15/2015", dutyStatuses, dateFormat);

        //this log will use one of the 16 hour exceptions
        dutyStatuses.clear();
        dutyStatuses.put("06/16/2015 01:00:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY)); //10 hour break
        dutyStatuses.put("06/16/2015 02:00:00", new DutyStatusEnum(DutyStatusEnum.ONDUTY));
        dutyStatuses.put("06/16/2015 16:00:00", new DutyStatusEnum(DutyStatusEnum.DRIVING)); //driving at 14 hours
        dutyStatuses.put("06/16/2015 18:00:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY)); //driving until 16 hours
        EmployeeLog log16 = CreateEmployeeLog(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), new DriverTypeEnum(DriverTypeEnum.PROPERTYCARRYING), "06/16/2015", dutyStatuses, dateFormat);
        log16.setIsNonCDLShortHaulExceptionUsed(true);

        //this log will use the second 16 hour exception
        dutyStatuses.clear();
        dutyStatuses.put("06/17/2015 04:00:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY)); //10 hour break
        dutyStatuses.put("06/17/2015 06:00:00", new DutyStatusEnum(DutyStatusEnum.ONDUTY));
        dutyStatuses.put("06/17/2015 20:00:00", new DutyStatusEnum(DutyStatusEnum.DRIVING)); //driving at 14 hours
        dutyStatuses.put("06/17/2015 22:00:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY)); //driving until 16 hours
        EmployeeLog log17 = CreateEmployeeLog(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), new DriverTypeEnum(DriverTypeEnum.PROPERTYCARRYING), "06/17/2015", dutyStatuses, dateFormat);
        log17.setIsNonCDLShortHaulExceptionUsed(true);

        ArrayList<EmployeeLog> logs = new ArrayList<>();
        logs.add(log15);
        logs.add(log16);
        logs.add(log17);

        MockEmployeeLogFacade mockFacade = new MockEmployeeLogFacade();
        mockFacade.setLogList(logs);

        //log16 and log17 should both be valid
        Exempt150AirMileLogValidator validator = new Exempt150AirMileLogValidator(mockFacade);
        boolean isExemptLogEligible = validator.IsExemptLogEligible(log16, log16.getEldEventList());
        Assert.assertTrue(isExemptLogEligible);

        validator = new Exempt150AirMileLogValidator(mockFacade);
        isExemptLogEligible = validator.IsExemptLogEligible(log17, log17.getEldEventList());
        Assert.assertTrue(isExemptLogEligible);
    }

    @Test
    public void test_Property16hExceptionDisallowed() throws ParseException {

        //see comment in test_Property16hExceptionAllowed, above.
        SimpleDateFormat dateFormat = DateUtility.getHomeTerminalDateTimeFormat();
        dateFormat.setTimeZone(TimeZone.getTimeZone("America/Chicago"));

        //this log will use one of the 16 hour exceptions
        Map<String, DutyStatusEnum> dutyStatuses = new LinkedHashMap<>();
        dutyStatuses.put("06/15/2015 01:00:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        dutyStatuses.put("06/15/2015 02:00:00", new DutyStatusEnum(DutyStatusEnum.ONDUTY));
        dutyStatuses.put("06/15/2015 16:00:00", new DutyStatusEnum(DutyStatusEnum.DRIVING));
        dutyStatuses.put("06/15/2015 18:00:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        EmployeeLog log15 = CreateEmployeeLog(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), new DriverTypeEnum(DriverTypeEnum.PROPERTYCARRYING), "06/15/2015", dutyStatuses, dateFormat);
        log15.setIsNonCDLShortHaulExceptionUsed(true);

        //this log will use the 2nd 16 hour exceptions
        dutyStatuses.clear();
        dutyStatuses.put("06/16/2015 04:00:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY)); //10 hour break
        dutyStatuses.put("06/16/2015 06:00:00", new DutyStatusEnum(DutyStatusEnum.ONDUTY));
        dutyStatuses.put("06/16/2015 20:00:00", new DutyStatusEnum(DutyStatusEnum.DRIVING)); //driving at 14 hours
        dutyStatuses.put("06/16/2015 22:00:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY)); //driving until 16 hours
        EmployeeLog log16 = CreateEmployeeLog(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), new DriverTypeEnum(DriverTypeEnum.PROPERTYCARRYING), "06/16/2015", dutyStatuses, dateFormat);
        log16.setIsNonCDLShortHaulExceptionUsed(true);

        //this log will attempt to use a 16 hour exception but will be in violation
        dutyStatuses.clear();
        dutyStatuses.put("06/17/2015 08:00:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY)); //10 hour break
        dutyStatuses.put("06/17/2015 08:30:00", new DutyStatusEnum(DutyStatusEnum.ONDUTY));
        dutyStatuses.put("06/17/2015 22:30:00", new DutyStatusEnum(DutyStatusEnum.DRIVING)); //driving at 14 hours
        dutyStatuses.put("06/17/2015 23:30:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY)); //driving until 16 hours
        EmployeeLog log17 = CreateEmployeeLog(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), new DriverTypeEnum(DriverTypeEnum.PROPERTYCARRYING), "06/17/2015", dutyStatuses, dateFormat);
        log17.setIsNonCDLShortHaulExceptionUsed(true);

        ArrayList<EmployeeLog> logs = new ArrayList<>();
        logs.add(log15);
        logs.add(log16);
        logs.add(log17);

        MockEmployeeLogFacade mockFacade = new MockEmployeeLogFacade();
        mockFacade.setLogList(logs);

        //the first 2 logs should be valid but the third one should be invalid
        Exempt150AirMileLogValidator validator = new Exempt150AirMileLogValidator(mockFacade);
        boolean isExemptLogEligible = validator.IsExemptLogEligible(log15, log15.getEldEventList());
        Assert.assertTrue(isExemptLogEligible);

        validator = new Exempt150AirMileLogValidator(mockFacade);
        isExemptLogEligible = validator.IsExemptLogEligible(log16, log16.getEldEventList());
        Assert.assertTrue(isExemptLogEligible);

        validator = new Exempt150AirMileLogValidator(mockFacade);
        isExemptLogEligible = validator.IsExemptLogEligible(log17, log17.getEldEventList());
        Assert.assertFalse(isExemptLogEligible);
    }

    private class MockGlobalState extends GlobalState {
        final GlobalState _originalGS;

        public MockGlobalState(GlobalState originalGS) {
            _originalGS = originalGS;

            //replace the instance with this fake one
            singleton = this;
        }

        @Override
        public Context getApplicationContext() {
            return _originalGS.getApplicationContext();
        }

        @Override
        public void setCompanyConfigSettings(Context ctx, CompanyConfigSettings config) {
            _companyConfigSettings = config;
        }
    }
}
