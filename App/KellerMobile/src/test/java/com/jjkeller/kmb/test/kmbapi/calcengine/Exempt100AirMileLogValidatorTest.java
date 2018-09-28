package com.jjkeller.kmb.test.kmbapi.calcengine;

import android.content.Context;

import com.jjkeller.kmb.test.KMBRoboElectricTestRunner;
import com.jjkeller.kmb.test.KmbRoboTestBase;
import com.jjkeller.kmbapi.calcengine.Exempt100AirMileLogValidator;
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
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RunWith(KMBRoboElectricTestRunner.class)
public class Exempt100AirMileLogValidatorTest extends KmbRoboTestBase {
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


        // set up a mock context for this test
        User user = new User();
        user.setCredentials(new LoginCredentials());
        user.getCredentials().setPrimaryKey(0);
        user.setHomeTerminalTimeZone(new TimeZoneEnum(TimeZoneEnum.CENTRALSTANDARDTIME));

        Context ctx = GlobalState.getInstance().getApplicationContext();
        new LoginController(ctx).setCurrentUser(user);

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

    private EmployeeLog CreateEmployeeLog(RuleSetTypeEnum ruleSet, DriverTypeEnum driverType, String logDate, Map<String, DutyStatusEnum> statuses) throws ParseException {
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
            logEvent.setStartTime(DateUtility.getHomeTerminalDateTimeFormat().parse(timeString));
            logEvent.setDutyStatusEnum(dutyStatus);

            logEventList.add(logEvent);
        }

        EmployeeLogEldEvent[] logEvents = new EmployeeLogEldEvent[logEventList.size()];
        logEvents = logEventList.toArray(logEvents);


        employeeLog.getEldEventList().setEldEventList(logEvents);

        return employeeLog;
    }

    @Test
    public void test_HasNotReturnedToLocation() throws ParseException {
        Map<String, DutyStatusEnum> dutyStatuses = new LinkedHashMap<>();
        dutyStatuses.put("06/15/2015 01:00:00", new DutyStatusEnum(DutyStatusEnum.ONDUTY));
        dutyStatuses.put("06/15/2015 02:00:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        EmployeeLog employeeLog = CreateEmployeeLog(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), new DriverTypeEnum(DriverTypeEnum.PASSENGERCARRYING), "06/15/2015", dutyStatuses);

        Exempt100AirMileLogValidator validator = new Exempt100AirMileLogValidator();
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
        EmployeeLog employeeLog = CreateEmployeeLog(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), new DriverTypeEnum(DriverTypeEnum.PASSENGERCARRYING), "06/15/2015", dutyStatuses);

        Exempt100AirMileLogValidator validator = new Exempt100AirMileLogValidator();
        boolean isExemptLogEligible = validator.IsExemptLogEligible(employeeLog, employeeLog.getEldEventList());
        Assert.assertTrue(isExemptLogEligible);
    }

    @Test
    public void test_Passenger12hOn() throws ParseException {
        Map<String, DutyStatusEnum> dutyStatuses = new LinkedHashMap<>();
        dutyStatuses.put("06/15/2015 01:00:00", new DutyStatusEnum(DutyStatusEnum.ONDUTY));
        dutyStatuses.put("06/15/2015 13:00:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        EmployeeLog employeeLog = CreateEmployeeLog(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), new DriverTypeEnum(DriverTypeEnum.PASSENGERCARRYING), "06/15/2015", dutyStatuses);

        Exempt100AirMileLogValidator validator = new Exempt100AirMileLogValidator();
        boolean isExemptLogEligible = validator.IsExemptLogEligible(employeeLog, employeeLog.getEldEventList());
        Assert.assertTrue(isExemptLogEligible);
    }

    @Test
    public void test_Passenger12hOnSpanningMidnight() throws ParseException {
        Map<String, DutyStatusEnum> dutyStatuses = new LinkedHashMap<>();
        dutyStatuses.put("06/15/2015 21:00:00", new DutyStatusEnum(DutyStatusEnum.ONDUTY));
        dutyStatuses.put("06/16/2015 09:00:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        EmployeeLog employeeLog = CreateEmployeeLog(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), new DriverTypeEnum(DriverTypeEnum.PASSENGERCARRYING), "06/15/2015", dutyStatuses);

        Exempt100AirMileLogValidator validator = new Exempt100AirMileLogValidator();
        boolean isExemptLogEligible = validator.IsExemptLogEligible(employeeLog, employeeLog.getEldEventList());
        Assert.assertTrue(isExemptLogEligible);
    }

    @Test
    public void test_Passenger12hOn8hOff12hOn() throws ParseException {
        Map<String, DutyStatusEnum> dutyStatuses = new LinkedHashMap<>();
        dutyStatuses.put("06/15/2015 11:00:00", new DutyStatusEnum(DutyStatusEnum.ONDUTY));
        dutyStatuses.put("06/15/2015 23:00:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        dutyStatuses.put("06/16/2015 07:00:00", new DutyStatusEnum(DutyStatusEnum.ONDUTY));
        dutyStatuses.put("06/16/2015 19:00:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        EmployeeLog employeeLog = CreateEmployeeLog(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), new DriverTypeEnum(DriverTypeEnum.PASSENGERCARRYING), "06/15/2015", dutyStatuses);

        Exempt100AirMileLogValidator validator = new Exempt100AirMileLogValidator();
        boolean isExemptLogEligible = validator.IsExemptLogEligible(employeeLog, employeeLog.getEldEventList());
        Assert.assertTrue(isExemptLogEligible);
    }

    @Test
    public void test_Passenger12hOn8hOff12hOnSpanningMidnight() throws ParseException {
        Map<String, DutyStatusEnum> dutyStatuses = new LinkedHashMap<>();
        dutyStatuses.put("06/15/2015 13:00:00", new DutyStatusEnum(DutyStatusEnum.ONDUTY));
        dutyStatuses.put("06/16/2015 01:00:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        dutyStatuses.put("06/16/2015 09:00:00", new DutyStatusEnum(DutyStatusEnum.ONDUTY));
        dutyStatuses.put("06/16/2015 21:00:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        EmployeeLog employeeLog = CreateEmployeeLog(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), new DriverTypeEnum(DriverTypeEnum.PASSENGERCARRYING), "06/15/2015", dutyStatuses);

        Exempt100AirMileLogValidator validator = new Exempt100AirMileLogValidator();
        boolean isExemptLogEligible = validator.IsExemptLogEligible(employeeLog, employeeLog.getEldEventList());
        Assert.assertTrue(isExemptLogEligible);
    }

    @Test
    public void test_Passenger12hPlusOn() throws ParseException {
        Map<String, DutyStatusEnum> dutyStatuses = new LinkedHashMap<>();
        dutyStatuses.put("06/15/2015 01:00:00", new DutyStatusEnum(DutyStatusEnum.ONDUTY));
        dutyStatuses.put("06/15/2015 13:01:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        EmployeeLog employeeLog = CreateEmployeeLog(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), new DriverTypeEnum(DriverTypeEnum.PASSENGERCARRYING), "06/15/2015", dutyStatuses);

        Exempt100AirMileLogValidator validator = new Exempt100AirMileLogValidator();
        boolean isExemptLogEligible = validator.IsExemptLogEligible(employeeLog, employeeLog.getEldEventList());
        Assert.assertFalse(isExemptLogEligible);
    }

    @Test
    public void test_Passenger12hPlusOnSpanningMidnight() throws ParseException {
        Map<String, DutyStatusEnum> dutyStatuses = new LinkedHashMap<>();
        dutyStatuses.put("06/15/2015 21:00:00", new DutyStatusEnum(DutyStatusEnum.ONDUTY));
        EmployeeLog employeeLog = CreateEmployeeLog(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), new DriverTypeEnum(DriverTypeEnum.PASSENGERCARRYING), "06/15/2015", dutyStatuses);

        dutyStatuses.clear();
        dutyStatuses.put("06/16/2015 09:01:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        EmployeeLog nextLog = CreateEmployeeLog(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), new DriverTypeEnum(DriverTypeEnum.PASSENGERCARRYING), "06/16/2015", dutyStatuses);

        ArrayList<EmployeeLog> logList = new ArrayList<>();
        logList.add(employeeLog);
        logList.add(nextLog);
        MockEmployeeLogFacade mockFacade = new MockEmployeeLogFacade();
        mockFacade.setLogList(logList);

        Exempt100AirMileLogValidator validator = new Exempt100AirMileLogValidator(mockFacade);
        boolean isExemptLogEligible = validator.IsExemptLogEligible(employeeLog, employeeLog.getEldEventList());
        Assert.assertFalse(isExemptLogEligible);
    }

    @Test
    public void test_Passenger12hOn7hOff12hOn() throws ParseException {
        Map<String, DutyStatusEnum> dutyStatuses = new LinkedHashMap<>();
        dutyStatuses.put("06/15/2015 11:00:00", new DutyStatusEnum(DutyStatusEnum.ONDUTY));
        dutyStatuses.put("06/15/2015 23:00:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        dutyStatuses.put("06/16/2015 06:00:00", new DutyStatusEnum(DutyStatusEnum.ONDUTY));
        dutyStatuses.put("06/16/2015 18:00:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        EmployeeLog employeeLog = CreateEmployeeLog(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), new DriverTypeEnum(DriverTypeEnum.PASSENGERCARRYING), "06/15/2015", dutyStatuses);

        Exempt100AirMileLogValidator validator = new Exempt100AirMileLogValidator();
        boolean isExemptLogEligible = validator.IsExemptLogEligible(employeeLog, employeeLog.getEldEventList());
        Assert.assertFalse(isExemptLogEligible);
    }

    @Test
    public void test_Passenger12hOn7hOff12hOnSpanningMidnight() throws ParseException {
        Map<String, DutyStatusEnum> dutyStatuses = new LinkedHashMap<>();
        dutyStatuses.put("06/15/2015 13:00:00", new DutyStatusEnum(DutyStatusEnum.ONDUTY));
        EmployeeLog employeeLog = CreateEmployeeLog(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), new DriverTypeEnum(DriverTypeEnum.PASSENGERCARRYING), "06/15/2015", dutyStatuses);

        dutyStatuses.clear();
        dutyStatuses.put("06/16/2015 01:00:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        dutyStatuses.put("06/16/2015 08:00:00", new DutyStatusEnum(DutyStatusEnum.ONDUTY));
        dutyStatuses.put("06/16/2015 20:00:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        EmployeeLog nextLog = CreateEmployeeLog(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), new DriverTypeEnum(DriverTypeEnum.PASSENGERCARRYING), "06/16/2015", dutyStatuses);

        ArrayList<EmployeeLog> logList = new ArrayList<>();
        logList.add(employeeLog);
        logList.add(nextLog);
        MockEmployeeLogFacade mockFacade = new MockEmployeeLogFacade();
        mockFacade.setLogList(logList);

        Exempt100AirMileLogValidator validator = new Exempt100AirMileLogValidator(mockFacade);
        boolean isExemptLogEligible = validator.IsExemptLogEligible(employeeLog, employeeLog.getEldEventList());
        Assert.assertFalse(isExemptLogEligible);
    }

    @Test
    public void test_Passenger12hOn7hOff12HourDriving() throws ParseException {
        Map<String, DutyStatusEnum> dutyStatuses = new LinkedHashMap<>();
        dutyStatuses.put("06/15/2015 11:00:00", new DutyStatusEnum(DutyStatusEnum.ONDUTY));
        dutyStatuses.put("06/15/2015 23:00:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        dutyStatuses.put("06/16/2015 06:00:00", new DutyStatusEnum(DutyStatusEnum.DRIVING));
        dutyStatuses.put("06/16/2015 18:00:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        EmployeeLog employeeLog = CreateEmployeeLog(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), new DriverTypeEnum(DriverTypeEnum.PASSENGERCARRYING), "06/15/2015", dutyStatuses);

        Exempt100AirMileLogValidator validator = new Exempt100AirMileLogValidator();
        boolean isExemptLogEligible = validator.IsExemptLogEligible(employeeLog, employeeLog.getEldEventList());
        Assert.assertFalse(isExemptLogEligible);
    }

    @Test
    public void test_Passenger12hOn7hOff12HourDrivingSpanningMidnight() throws ParseException {
        Map<String, DutyStatusEnum> dutyStatuses = new LinkedHashMap<>();
        dutyStatuses.put("06/15/2015 13:00:00", new DutyStatusEnum(DutyStatusEnum.ONDUTY));
        EmployeeLog employeeLog = CreateEmployeeLog(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), new DriverTypeEnum(DriverTypeEnum.PASSENGERCARRYING), "06/15/2015", dutyStatuses);

        dutyStatuses.clear();
        dutyStatuses.put("06/16/2015 01:00:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        dutyStatuses.put("06/16/2015 08:00:00", new DutyStatusEnum(DutyStatusEnum.DRIVING));
        dutyStatuses.put("06/16/2015 20:00:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        EmployeeLog nextLog = CreateEmployeeLog(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), new DriverTypeEnum(DriverTypeEnum.PASSENGERCARRYING), "06/16/2015", dutyStatuses);

        ArrayList<EmployeeLog> logList = new ArrayList<>();
        logList.add(employeeLog);
        logList.add(nextLog);
        MockEmployeeLogFacade mockFacade = new MockEmployeeLogFacade();
        mockFacade.setLogList(logList);

        Exempt100AirMileLogValidator validator = new Exempt100AirMileLogValidator(mockFacade);
        boolean isExemptLogEligible = validator.IsExemptLogEligible(employeeLog, employeeLog.getEldEventList());
        Assert.assertFalse(isExemptLogEligible);
    }

    @Test
    public void test_Passenger13hPlusOn() throws ParseException {
        Map<String, DutyStatusEnum> dutyStatuses = new LinkedHashMap<>();
        dutyStatuses.put("06/15/2015 01:00:00", new DutyStatusEnum(DutyStatusEnum.ONDUTY));
        dutyStatuses.put("06/15/2015 13:01:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        EmployeeLog employeeLog = CreateEmployeeLog(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), new DriverTypeEnum(DriverTypeEnum.PASSENGERCARRYING), "06/15/2015", dutyStatuses);

        Exempt100AirMileLogValidator validator = new Exempt100AirMileLogValidator();
        boolean isExemptLogEligible = validator.IsExemptLogEligible(employeeLog, employeeLog.getEldEventList());
        Assert.assertFalse(isExemptLogEligible);
    }

    @Test
    public void test_Passenger12hWithBigGap() throws ParseException {
        Map<String, DutyStatusEnum> dutyStatuses = new LinkedHashMap<>();
        dutyStatuses.put("06/15/2015 01:00:00", new DutyStatusEnum(DutyStatusEnum.ONDUTY));
        dutyStatuses.put("06/15/2015 02:00:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        dutyStatuses.put("06/15/2015 13:00:00", new DutyStatusEnum(DutyStatusEnum.ONDUTY));
        dutyStatuses.put("06/15/2015 14:00:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        EmployeeLog employeeLog = CreateEmployeeLog(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), new DriverTypeEnum(DriverTypeEnum.PASSENGERCARRYING), "06/15/2015", dutyStatuses);

        Exempt100AirMileLogValidator validator = new Exempt100AirMileLogValidator();
        boolean isExemptLogEligible = validator.IsExemptLogEligible(employeeLog, employeeLog.getEldEventList());
        Assert.assertTrue(isExemptLogEligible);
    }

    @Test
    public void test_Passenger10hDriving() throws ParseException {
        Map<String, DutyStatusEnum> dutyStatuses = new LinkedHashMap<>();
        dutyStatuses.put("06/15/2015 01:00:00", new DutyStatusEnum(DutyStatusEnum.DRIVING));
        dutyStatuses.put("06/15/2015 11:00:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        EmployeeLog employeeLog = CreateEmployeeLog(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), new DriverTypeEnum(DriverTypeEnum.PASSENGERCARRYING), "06/15/2015", dutyStatuses);

        Exempt100AirMileLogValidator validator = new Exempt100AirMileLogValidator();
        boolean isExemptLogEligible = validator.IsExemptLogEligible(employeeLog, employeeLog.getEldEventList());
        Assert.assertTrue(isExemptLogEligible);
    }

    @Test
    public void test_Passenger10hPlusDriving() throws ParseException {
        Map<String, DutyStatusEnum> dutyStatuses = new LinkedHashMap<>();
        dutyStatuses.put("06/15/2015 01:00:00", new DutyStatusEnum(DutyStatusEnum.DRIVING));
        dutyStatuses.put("06/15/2015 11:01:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        EmployeeLog employeeLog = CreateEmployeeLog(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), new DriverTypeEnum(DriverTypeEnum.PASSENGERCARRYING), "06/15/2015", dutyStatuses);

        Exempt100AirMileLogValidator validator = new Exempt100AirMileLogValidator();
        boolean isExemptLogEligible = validator.IsExemptLogEligible(employeeLog, employeeLog.getEldEventList());
        Assert.assertFalse(isExemptLogEligible);
    }

    @Test
    public void test_Passenger4hOn12hOff5hOn() throws ParseException {
        Map<String, DutyStatusEnum> dutyStatuses = new LinkedHashMap<>();
        dutyStatuses.put("06/15/2015 01:00:00", new DutyStatusEnum(DutyStatusEnum.ONDUTY));
        dutyStatuses.put("06/15/2015 05:00:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        dutyStatuses.put("06/15/2015 17:00:00", new DutyStatusEnum(DutyStatusEnum.ONDUTY));
        dutyStatuses.put("06/15/2015 22:00:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        EmployeeLog employeeLog = CreateEmployeeLog(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), new DriverTypeEnum(DriverTypeEnum.PASSENGERCARRYING), "06/15/2015", dutyStatuses);

        Exempt100AirMileLogValidator validator = new Exempt100AirMileLogValidator();
        boolean isExemptLogEligible = validator.IsExemptLogEligible(employeeLog, employeeLog.getEldEventList());
        Assert.assertTrue(isExemptLogEligible);
    }

    @Test
    public void test_Passenger4hDrive5hOff5hOn() throws ParseException {
        Map<String, DutyStatusEnum> dutyStatuses = new LinkedHashMap<>();
        dutyStatuses.put("06/15/2015 01:00:00", new DutyStatusEnum(DutyStatusEnum.DRIVING));
        dutyStatuses.put("06/15/2015 05:00:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        dutyStatuses.put("06/15/2015 12:00:00", new DutyStatusEnum(DutyStatusEnum.ONDUTY));
        dutyStatuses.put("06/15/2015 17:00:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        EmployeeLog employeeLog = CreateEmployeeLog(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), new DriverTypeEnum(DriverTypeEnum.PASSENGERCARRYING), "06/15/2015", dutyStatuses);

        Exempt100AirMileLogValidator validator = new Exempt100AirMileLogValidator();
        boolean isExemptLogEligible = validator.IsExemptLogEligible(employeeLog, employeeLog.getEldEventList());
        Assert.assertFalse(isExemptLogEligible);
    }

    //-------------------
    @Test
    public void test_Property12hOn() throws ParseException {
        Map<String, DutyStatusEnum> dutyStatuses = new LinkedHashMap<>();
        dutyStatuses.put("06/15/2015 01:00:00", new DutyStatusEnum(DutyStatusEnum.ONDUTY));
        dutyStatuses.put("06/15/2015 13:00:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        EmployeeLog employeeLog = CreateEmployeeLog(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), new DriverTypeEnum(DriverTypeEnum.PROPERTYCARRYING), "06/15/2015", dutyStatuses);

        Exempt100AirMileLogValidator validator = new Exempt100AirMileLogValidator();
        boolean isExemptLogEligible = validator.IsExemptLogEligible(employeeLog, employeeLog.getEldEventList());
        Assert.assertTrue(isExemptLogEligible);
    }

    @Test
    public void test_Property12hOnSpanningMidnight() throws ParseException {
        Map<String, DutyStatusEnum> dutyStatuses = new LinkedHashMap<>();
        dutyStatuses.put("06/15/2015 21:00:00", new DutyStatusEnum(DutyStatusEnum.ONDUTY));
        dutyStatuses.put("06/16/2015 09:00:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        EmployeeLog employeeLog = CreateEmployeeLog(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), new DriverTypeEnum(DriverTypeEnum.PROPERTYCARRYING), "06/15/2015", dutyStatuses);

        Exempt100AirMileLogValidator validator = new Exempt100AirMileLogValidator();
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

        Exempt100AirMileLogValidator validator = new Exempt100AirMileLogValidator();
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

        Exempt100AirMileLogValidator validator = new Exempt100AirMileLogValidator();
        boolean isExemptLogEligible = validator.IsExemptLogEligible(employeeLog, employeeLog.getEldEventList());
        Assert.assertTrue(isExemptLogEligible);
    }

    @Test
    public void test_Property12hPlusOnSpanningMidnight() throws ParseException {
        Map<String, DutyStatusEnum> dutyStatuses = new LinkedHashMap<>();
        dutyStatuses.put("06/15/2015 21:00:00", new DutyStatusEnum(DutyStatusEnum.ONDUTY));
        EmployeeLog employeeLog = CreateEmployeeLog(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), new DriverTypeEnum(DriverTypeEnum.PROPERTYCARRYING), "06/15/2015", dutyStatuses);

        dutyStatuses.clear();
        dutyStatuses.put("06/16/2015 09:01:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        EmployeeLog nextLog = CreateEmployeeLog(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), new DriverTypeEnum(DriverTypeEnum.PROPERTYCARRYING), "06/16/2015", dutyStatuses);

        ArrayList<EmployeeLog> logList = new ArrayList<>();
        logList.add(employeeLog);
        logList.add(nextLog);
        MockEmployeeLogFacade mockFacade = new MockEmployeeLogFacade();
        mockFacade.setLogList(logList);

        Exempt100AirMileLogValidator validator = new Exempt100AirMileLogValidator(mockFacade);
        boolean isExemptLogEligible = validator.IsExemptLogEligible(employeeLog, employeeLog.getEldEventList());
        Assert.assertFalse(isExemptLogEligible);
    }

    @Test
    public void test_Property12hOn9hOff12hOn() throws ParseException {
        Map<String, DutyStatusEnum> dutyStatuses = new LinkedHashMap<>();
        dutyStatuses.put("06/15/2015 11:00:00", new DutyStatusEnum(DutyStatusEnum.ONDUTY));
        dutyStatuses.put("06/15/2015 23:00:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        dutyStatuses.put("06/16/2015 08:00:00", new DutyStatusEnum(DutyStatusEnum.ONDUTY));
        dutyStatuses.put("06/16/2015 20:00:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        EmployeeLog employeeLog = CreateEmployeeLog(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), new DriverTypeEnum(DriverTypeEnum.PROPERTYCARRYING), "06/15/2015", dutyStatuses);

        Exempt100AirMileLogValidator validator = new Exempt100AirMileLogValidator();
        boolean isExemptLogEligible = validator.IsExemptLogEligible(employeeLog, employeeLog.getEldEventList());
        Assert.assertFalse(isExemptLogEligible);
    }

    @Test
    public void test_Property12hOn9hOff12hOnSpanningMidnight() throws ParseException {
        Map<String, DutyStatusEnum> dutyStatuses = new LinkedHashMap<>();
        dutyStatuses.put("06/15/2015 13:00:00", new DutyStatusEnum(DutyStatusEnum.ONDUTY));
        EmployeeLog employeeLog = CreateEmployeeLog(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), new DriverTypeEnum(DriverTypeEnum.PROPERTYCARRYING), "06/15/2015", dutyStatuses);

        dutyStatuses.clear();
        dutyStatuses.put("06/16/2015 01:00:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        dutyStatuses.put("06/16/2015 10:00:00", new DutyStatusEnum(DutyStatusEnum.ONDUTY));
        dutyStatuses.put("06/16/2015 22:00:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        EmployeeLog nextLog = CreateEmployeeLog(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), new DriverTypeEnum(DriverTypeEnum.PROPERTYCARRYING), "06/16/2015", dutyStatuses);

        ArrayList<EmployeeLog> logList = new ArrayList<>();
        logList.add(employeeLog);
        logList.add(nextLog);
        MockEmployeeLogFacade mockFacade = new MockEmployeeLogFacade();
        mockFacade.setLogList(logList);

        Exempt100AirMileLogValidator validator = new Exempt100AirMileLogValidator(mockFacade);
        boolean isExemptLogEligible = validator.IsExemptLogEligible(employeeLog, employeeLog.getEldEventList());
        Assert.assertFalse(isExemptLogEligible);
    }

    @Test
    public void test_Property12hOn9hOff12HourDriving() throws ParseException {
        Map<String, DutyStatusEnum> dutyStatuses = new LinkedHashMap<>();
        dutyStatuses.put("06/15/2015 11:00:00", new DutyStatusEnum(DutyStatusEnum.ONDUTY));
        dutyStatuses.put("06/15/2015 23:00:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        dutyStatuses.put("06/16/2015 08:00:00", new DutyStatusEnum(DutyStatusEnum.DRIVING));
        dutyStatuses.put("06/16/2015 20:00:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        EmployeeLog employeeLog = CreateEmployeeLog(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), new DriverTypeEnum(DriverTypeEnum.PROPERTYCARRYING), "06/15/2015", dutyStatuses);

        Exempt100AirMileLogValidator validator = new Exempt100AirMileLogValidator();
        boolean isExemptLogEligible = validator.IsExemptLogEligible(employeeLog, employeeLog.getEldEventList());
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

        Exempt100AirMileLogValidator validator = new Exempt100AirMileLogValidator(mockFacade);
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

        Exempt100AirMileLogValidator validator = new Exempt100AirMileLogValidator();
        boolean isExemptLogEligible = validator.IsExemptLogEligible(employeeLog, employeeLog.getEldEventList());
        Assert.assertFalse(isExemptLogEligible);
    }

    @Test
    public void test_Property11hDriving() throws ParseException {
        Map<String, DutyStatusEnum> dutyStatuses = new LinkedHashMap<>();
        dutyStatuses.put("06/15/2015 01:00:00", new DutyStatusEnum(DutyStatusEnum.DRIVING));
        dutyStatuses.put("06/15/2015 12:00:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        EmployeeLog employeeLog = CreateEmployeeLog(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), new DriverTypeEnum(DriverTypeEnum.PROPERTYCARRYING), "06/15/2015", dutyStatuses);

        Exempt100AirMileLogValidator validator = new Exempt100AirMileLogValidator();
        boolean isExemptLogEligible = validator.IsExemptLogEligible(employeeLog, employeeLog.getEldEventList());
        Assert.assertTrue(isExemptLogEligible);
    }

    @Test
    public void test_Property11hPlusDriving() throws ParseException {
        Map<String, DutyStatusEnum> dutyStatuses = new LinkedHashMap<>();
        dutyStatuses.put("06/15/2015 01:00:00", new DutyStatusEnum(DutyStatusEnum.DRIVING));
        dutyStatuses.put("06/15/2015 12:01:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        EmployeeLog employeeLog = CreateEmployeeLog(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), new DriverTypeEnum(DriverTypeEnum.PROPERTYCARRYING), "06/15/2015", dutyStatuses);

        Exempt100AirMileLogValidator validator = new Exempt100AirMileLogValidator();
        boolean isExemptLogEligible = validator.IsExemptLogEligible(employeeLog, employeeLog.getEldEventList());
        Assert.assertFalse(isExemptLogEligible);
    }

    @Test
    public void test_Property4hOn12hOff5hOn() throws ParseException {
        Map<String, DutyStatusEnum> dutyStatuses = new LinkedHashMap<>();
        dutyStatuses.put("06/15/2015 01:00:00", new DutyStatusEnum(DutyStatusEnum.ONDUTY));
        dutyStatuses.put("06/15/2015 05:00:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        dutyStatuses.put("06/15/2015 17:00:00", new DutyStatusEnum(DutyStatusEnum.ONDUTY));
        dutyStatuses.put("06/15/2015 22:00:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        EmployeeLog employeeLog = CreateEmployeeLog(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), new DriverTypeEnum(DriverTypeEnum.PROPERTYCARRYING), "06/15/2015", dutyStatuses);

        Exempt100AirMileLogValidator validator = new Exempt100AirMileLogValidator();
        boolean isExemptLogEligible = validator.IsExemptLogEligible(employeeLog, employeeLog.getEldEventList());
        Assert.assertTrue(isExemptLogEligible);
    }

    @Test
    public void test_Property4hOn18hOff() throws ParseException {
        Map<String, DutyStatusEnum> dutyStatuses = new LinkedHashMap<>();
        dutyStatuses.put("06/09/2015 01:00:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        dutyStatuses.put("06/09/2015 05:00:00", new DutyStatusEnum(DutyStatusEnum.ONDUTY));
        dutyStatuses.put("06/09/2015 17:00:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        EmployeeLog employeeLog = CreateEmployeeLog(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), new DriverTypeEnum(DriverTypeEnum.PROPERTYCARRYING), "06/09/2015", dutyStatuses);

        Exempt100AirMileLogValidator validator = new Exempt100AirMileLogValidator();
        boolean isExemptLogEligible = validator.IsExemptLogEligible(employeeLog, employeeLog.getEldEventList());
        Assert.assertTrue(isExemptLogEligible);
    }

    @Test
    public void test_Property4hDrive5hOff5hOn() throws ParseException {
        Map<String, DutyStatusEnum> dutyStatuses = new LinkedHashMap<>();
        dutyStatuses.put("06/15/2015 01:00:00", new DutyStatusEnum(DutyStatusEnum.DRIVING));
        dutyStatuses.put("06/15/2015 05:00:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        dutyStatuses.put("06/15/2015 12:00:00", new DutyStatusEnum(DutyStatusEnum.ONDUTY));
        dutyStatuses.put("06/15/2015 17:00:00", new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        EmployeeLog employeeLog = CreateEmployeeLog(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), new DriverTypeEnum(DriverTypeEnum.PROPERTYCARRYING), "06/15/2015", dutyStatuses);

        Exempt100AirMileLogValidator validator = new Exempt100AirMileLogValidator();
        boolean isExemptLogEligible = validator.IsExemptLogEligible(employeeLog, employeeLog.getEldEventList());
        Assert.assertFalse(isExemptLogEligible);
    }
}
