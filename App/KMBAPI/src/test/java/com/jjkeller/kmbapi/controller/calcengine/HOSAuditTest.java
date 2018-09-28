package com.jjkeller.kmbapi.controller.calcengine;

import android.content.Context;

import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.configuration.LoginCredentials;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.employeelogeldevents.EmployeeLogEldEventCode;
import com.jjkeller.kmbapi.employeelogeldevents.Enums;
import com.jjkeller.kmbapi.enums.DriverTypeEnum;
import com.jjkeller.kmbapi.enums.DutyStatusEnum;
import com.jjkeller.kmbapi.enums.ExemptLogTypeEnum;
import com.jjkeller.kmbapi.enums.RuleSetTypeEnum;
import com.jjkeller.kmbapi.enums.TimeZoneEnum;
import com.jjkeller.kmbapi.proxydata.CompanyConfigSettings;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEventList;

import junit.framework.Assert;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * HOS Audit Tests
 *
 * Created by Charles Stebbins on 4/13/2017.
 */

@RunWith(RobolectricTestRunner.class)
@Config(application = GlobalState.class,
        manifest = "/build/intermediates/bundles/encompass/debug/AndroidManifest.xml"
)
public class HOSAuditTest {

    User user;
    Context context;
    HOSAudit hosAudit = new HOSAudit();

    @Before
    public void setup() throws NoSuchFieldException, IllegalAccessException {
        user = createUser();
        context = RuntimeEnvironment.application;

        GlobalState.getInstance().setCurrentUser(user);

        CompanyConfigSettings configSettings = new CompanyConfigSettings();
        configSettings.setDailyLogStartTime("00:00:00");

        //Reflection to bypass database save from a set function.
        Field companyConfig = GlobalState.getInstance().getClass().getDeclaredField("_companyConfigSettings");
        companyConfig.setAccessible(true);
        companyConfig.set(GlobalState.getInstance(), configSettings);
    }

    @After
    public void teardown(){

    }

    @Test
    public void performCompleteAudit_EmptyLogList_ShouldBeEmptyLogSummary(){

        List<EmployeeLog> emptyEmployeeLogList = new LinkedList<>();
        Date dateTime = new DateTime(2017, 2 , 3, 13, 0, 0).toDate();

        List<LogSummary> emptyLogSummaryList = hosAudit.PerformCompleteAudit(user, context, emptyEmployeeLogList, dateTime);
        Assert.assertEquals(0, emptyLogSummaryList.size());
    }

    @Test
    public void performCompleteAudit_SingleLog_ShouldBeCorrectSingleLog(){
        List<EmployeeLog> employeeLogList = new LinkedList<>();
        RuleSetTypeEnum ruleSetTypeEnum = new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR);
        EmployeeLog log = createLog(new DateTime(2017, 2, 2, 13, 0, 0), new DateTime(2017, 2, 2, 15, 0, 0), 1000f, 2000f, ruleSetTypeEnum);

        employeeLogList.add(log);

        Date dateTime = new DateTime(2017, 2 , 3, 13, 0, 0).toDate();

        List<LogSummary> logSummaryList = hosAudit.PerformCompleteAudit(user, context, employeeLogList, dateTime);

        Assert.assertEquals(1, logSummaryList.size());

        LogSummary currentTestLog = logSummaryList.get(0);
        Assert.assertEquals(new DateTime(2017, 2, 2, 0, 0, 0).toDate(), currentTestLog.getLogDate());
        Assert.assertEquals(new DriverTypeEnum(DriverTypeEnum.PROPERTYCARRYING), currentTestLog.getDriverType());
        Assert.assertEquals(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), currentTestLog.getRuleset());
        Assert.assertEquals(new ExemptLogTypeEnum(ExemptLogTypeEnum.NULL), currentTestLog.getExemptLogType());
        Assert.assertEquals(true, currentTestLog.getLogExists());
        Assert.assertEquals(true, currentTestLog.getIsComplete());
        Assert.assertEquals(TimeUnit.MILLISECONDS.convert(90, TimeUnit.MINUTES), currentTestLog.getDrivingDuration().longValue()); //Total Minutes
        Assert.assertEquals(TimeUnit.MILLISECONDS.convert(30, TimeUnit.MINUTES), currentTestLog.getOnDutyDuration().longValue());
        Assert.assertEquals(2000f, currentTestLog.getDistance());
        Assert.assertEquals(TimeUnit.MILLISECONDS.convert(120, TimeUnit.MINUTES), currentTestLog.getWeeklyTotalDuration().longValue());
        Assert.assertEquals(0, currentTestLog.getOffDutyStart());
        Assert.assertEquals(1320, currentTestLog.getOffDutyEnd());
        Assert.assertEquals(TimeUnit.MILLISECONDS.convert(120, TimeUnit.MINUTES), currentTestLog.getDailyDurationTotal().longValue());
    }

    @Test
    public void performCompleteAudit_TwoLogs_ShouldBeCorrect(){
        List<EmployeeLog> employeeLogList = new LinkedList<>();
        RuleSetTypeEnum ruleSetTypeEnum = new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR);
        EmployeeLog log0 = createLog(new DateTime(2017, 2, 1, 14, 0, 0), new DateTime(2017, 2, 1, 17, 0, 0), 1000f, 6000f, ruleSetTypeEnum);
        EmployeeLog log1 = createLog(new DateTime(2017, 2, 2,  9, 0, 0), new DateTime(2017, 2, 2, 17, 0, 0), 1000f, 10000f, ruleSetTypeEnum);

        employeeLogList.add(log0);
        employeeLogList.add(log1);

        Date dateTime = new DateTime(2017, 2 , 2, 18, 0, 0).toDate();

        List<LogSummary> logSummaryList = hosAudit.PerformCompleteAudit(user, context, employeeLogList, dateTime);

        Assert.assertEquals(2, logSummaryList.size());

        LogSummary currentTestLog = logSummaryList.get(0);
        Assert.assertEquals(new DateTime(2017, 2, 2, 0, 0, 0).toDate(), currentTestLog.getLogDate());
        Assert.assertEquals(new DriverTypeEnum(DriverTypeEnum.PROPERTYCARRYING), currentTestLog.getDriverType());
        Assert.assertEquals(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), currentTestLog.getRuleset());
        Assert.assertEquals(new ExemptLogTypeEnum(ExemptLogTypeEnum.NULL), currentTestLog.getExemptLogType());
        Assert.assertEquals(true, currentTestLog.getLogExists());
        Assert.assertEquals(true, currentTestLog.getIsComplete());
        Assert.assertEquals(TimeUnit.MILLISECONDS.convert(450, TimeUnit.MINUTES), currentTestLog.getDrivingDuration().longValue()); //Total Minutes
        Assert.assertEquals(TimeUnit.MILLISECONDS.convert(30, TimeUnit.MINUTES), currentTestLog.getOnDutyDuration().longValue());
        Assert.assertEquals(TimeUnit.MILLISECONDS.convert(660, TimeUnit.MINUTES), currentTestLog.getWeeklyTotalDuration().longValue());
        Assert.assertEquals(0, currentTestLog.getOffDutyStart());
        Assert.assertEquals(960, currentTestLog.getOffDutyEnd());
        Assert.assertEquals(TimeUnit.MILLISECONDS.convert(480, TimeUnit.MINUTES), currentTestLog.getDailyDurationTotal().longValue());


        currentTestLog = logSummaryList.get(1);
        Assert.assertEquals(new DateTime(2017, 2, 1, 0, 0, 0).toDate(), currentTestLog.getLogDate());
        Assert.assertEquals(new DriverTypeEnum(DriverTypeEnum.PROPERTYCARRYING), currentTestLog.getDriverType());
        Assert.assertEquals(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), currentTestLog.getRuleset());
        Assert.assertEquals(new ExemptLogTypeEnum(ExemptLogTypeEnum.NULL), currentTestLog.getExemptLogType());
        Assert.assertEquals(true, currentTestLog.getLogExists());
        Assert.assertEquals(true, currentTestLog.getIsComplete());
        Assert.assertEquals(TimeUnit.MILLISECONDS.convert(150, TimeUnit.MINUTES), currentTestLog.getDrivingDuration().longValue()); //Total Minutes
        Assert.assertEquals(TimeUnit.MILLISECONDS.convert(30, TimeUnit.MINUTES), currentTestLog.getOnDutyDuration().longValue());
        Assert.assertEquals(null, currentTestLog.getWeeklyTotalDuration());
        Assert.assertEquals(0, currentTestLog.getOffDutyStart());
        Assert.assertEquals(TimeUnit.MILLISECONDS.convert(180, TimeUnit.MINUTES), currentTestLog.getDailyDurationTotal().longValue());
    }

    @Test
    public void performCompleteAudit_WeekOfLogs_ShouldCorrectlyHaveWholeWeek(){
        List<EmployeeLog> employeeLogList = buildWeekOfLogs(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR));

        Date dateTime = new DateTime(2017, 2 , 14, 13, 0, 0).toDate();

        List<LogSummary> logSummaryList = hosAudit.PerformCompleteAudit(user, context, employeeLogList, dateTime);

        Assert.assertEquals(8, logSummaryList.size());

        LogSummary currentTestLog = logSummaryList.get(0);
        Assert.assertEquals(new DateTime(2017, 2, 14, 0, 0, 0).toDate(), currentTestLog.getLogDate());
        Assert.assertEquals(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), currentTestLog.getRuleset());
        Assert.assertEquals(true,   currentTestLog.getLogExists());
        Assert.assertEquals(true,   currentTestLog.getIsComplete());
        Assert.assertEquals(TimeUnit.MILLISECONDS.convert(90, TimeUnit.MINUTES), currentTestLog.getDrivingDuration().longValue());//Total Minutes
        Assert.assertEquals(TimeUnit.MILLISECONDS.convert(30, TimeUnit.MINUTES), currentTestLog.getOnDutyDuration().longValue());
        Assert.assertEquals(2000f,  currentTestLog.getDistance());
        Assert.assertEquals(TimeUnit.MILLISECONDS.convert(840, TimeUnit.MINUTES),    currentTestLog.getWeeklyTotalDuration().longValue());
        Assert.assertEquals(0,      currentTestLog.getOffDutyStart());
        Assert.assertEquals(1320,   currentTestLog.getOffDutyEnd());
        Assert.assertEquals(TimeUnit.MILLISECONDS.convert(120, TimeUnit.MINUTES), currentTestLog.getDailyDurationTotal().longValue());

        currentTestLog = logSummaryList.get(1);
        Assert.assertEquals(new DateTime(2017, 2, 13, 0, 0, 0).toDate(), currentTestLog.getLogDate());
        Assert.assertEquals(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), currentTestLog.getRuleset());
        Assert.assertEquals(true,   currentTestLog.getLogExists());
        Assert.assertEquals(true,   currentTestLog.getIsComplete());
        Assert.assertEquals(TimeUnit.MILLISECONDS.convert(90, TimeUnit.MINUTES), currentTestLog.getDrivingDuration().longValue());//Total Minutes
        Assert.assertEquals(TimeUnit.MILLISECONDS.convert(30, TimeUnit.MINUTES), currentTestLog.getOnDutyDuration().longValue());
        Assert.assertEquals(2000f,  currentTestLog.getDistance());
        Assert.assertEquals(null,    currentTestLog.getWeeklyTotalDuration()); //Weekly total is only for the end date.
        Assert.assertEquals(0,      currentTestLog.getOffDutyStart());
        Assert.assertEquals(1320,   currentTestLog.getOffDutyEnd());
        Assert.assertEquals(TimeUnit.MILLISECONDS.convert(120, TimeUnit.MINUTES), currentTestLog.getDailyDurationTotal().longValue());

        currentTestLog = logSummaryList.get(6);
        Assert.assertEquals(new DateTime(2017, 2, 8, 0, 0, 0).toDate(), currentTestLog.getLogDate());
        Assert.assertEquals(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), currentTestLog.getRuleset());
        Assert.assertEquals(true,   currentTestLog.getLogExists());
        Assert.assertEquals(true,   currentTestLog.getIsComplete());
        Assert.assertEquals(TimeUnit.MILLISECONDS.convert(90, TimeUnit.MINUTES), currentTestLog.getDrivingDuration().longValue());//Total Minutes
        Assert.assertEquals(TimeUnit.MILLISECONDS.convert(30, TimeUnit.MINUTES), currentTestLog.getOnDutyDuration().longValue());
        Assert.assertEquals(2000f,  currentTestLog.getDistance());
        Assert.assertEquals(null,    currentTestLog.getWeeklyTotalDuration()); //Weekly total is only for the end date.
        Assert.assertEquals(0,      currentTestLog.getOffDutyStart());
        Assert.assertEquals(1320,   currentTestLog.getOffDutyEnd());
        Assert.assertEquals(TimeUnit.MILLISECONDS.convert(120, TimeUnit.MINUTES), currentTestLog.getDailyDurationTotal().longValue());
    }

    @Test
    public void performCompleteAudit_Canadian2Week_ShouldHave2WeeksCalculated() {

        RuleSetTypeEnum ruleType = new RuleSetTypeEnum(RuleSetTypeEnum.CANADIAN_CYCLE2);
        EmployeeLog log0 = createLog(new DateTime(2017, 2, 11, 13, 0, 0), new DateTime(2017, 2, 11, 15, 0, 0), 1000f, 2000f, ruleType);
        EmployeeLog log1 = createLog(new DateTime(2017, 2, 12, 13, 0, 0), new DateTime(2017, 2, 12, 15, 0, 0), 1000f, 2000f, ruleType);
        EmployeeLog log2 = createLog(new DateTime(2017, 2, 20, 13, 0, 0), new DateTime(2017, 2, 20, 15, 0, 0), 1000f, 2000f, ruleType);
        EmployeeLog log3 = createLog(new DateTime(2017, 2, 25, 13, 0, 0), new DateTime(2017, 2, 25, 15, 0, 0), 1000f, 2000f, ruleType);

        List<EmployeeLog> employeeLogList = new LinkedList<>();
        employeeLogList.add(log0);
        employeeLogList.add(log1);
        employeeLogList.add(log2);
        employeeLogList.add(log3);
        Date dateTime = new DateTime(2017, 2, 25, 0, 0, 0).toDate();

        List<LogSummary> logSummaryList = hosAudit.PerformCompleteAudit(user, context, employeeLogList, dateTime);

        Assert.assertEquals(15, logSummaryList.size());

        LogSummary currentTestLog = logSummaryList.get(0);
        Assert.assertEquals(new DateTime(2017, 2, 25, 0, 0, 0).toDate(), currentTestLog.getLogDate());
        Assert.assertEquals(new RuleSetTypeEnum(RuleSetTypeEnum.CANADIAN_CYCLE2), currentTestLog.getRuleset());
        Assert.assertEquals(true,   currentTestLog.getLogExists());
        Assert.assertEquals(true,   currentTestLog.getIsComplete());
        Assert.assertEquals(TimeUnit.MILLISECONDS.convert(90, TimeUnit.MINUTES), currentTestLog.getDrivingDuration().longValue());//Total Minutes
        Assert.assertEquals(TimeUnit.MILLISECONDS.convert(30, TimeUnit.MINUTES), currentTestLog.getOnDutyDuration().longValue());
        Assert.assertEquals(2000f,  currentTestLog.getDistance());
        Assert.assertEquals(TimeUnit.MILLISECONDS.convert(360, TimeUnit.MINUTES),    currentTestLog.getWeeklyTotalDuration().longValue());
        Assert.assertEquals(0,      currentTestLog.getOffDutyStart());
        Assert.assertEquals(1320,   currentTestLog.getOffDutyEnd());
        Assert.assertEquals(TimeUnit.MILLISECONDS.convert(120, TimeUnit.MINUTES), currentTestLog.getDailyDurationTotal().longValue());
    }

    @Test
    public void performCompleteAudit_7WeekOfLogs_ShouldCorrectlyHaveWholeWeek(){

        Date dateTime = new DateTime(2017, 2 , 14, 13, 0, 0).toDate();

        List<EmployeeLog> employeeLogList = buildWeekOfLogs(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR));
        List<LogSummary> logSummaryList = hosAudit.PerformCompleteAudit(user, context, employeeLogList, dateTime);
        test7DayCorrect(logSummaryList);

        employeeLogList = buildWeekOfLogs(new RuleSetTypeEnum(RuleSetTypeEnum.CANADIAN_CYCLE1));
        logSummaryList = hosAudit.PerformCompleteAudit(user, context, employeeLogList, dateTime);
        test7DayCorrect(logSummaryList);

        employeeLogList = buildWeekOfLogs(new RuleSetTypeEnum(RuleSetTypeEnum.TEXAS));
        logSummaryList = hosAudit.PerformCompleteAudit(user, context, employeeLogList, dateTime);
        test7DayCorrect(logSummaryList);

        employeeLogList = buildWeekOfLogs(new RuleSetTypeEnum(RuleSetTypeEnum.FLORIDA_7DAY));
        logSummaryList = hosAudit.PerformCompleteAudit(user, context, employeeLogList, dateTime);
        test7DayCorrect(logSummaryList);

        employeeLogList = buildWeekOfLogs(new RuleSetTypeEnum(RuleSetTypeEnum.WISCONSIN_7DAY));
        logSummaryList = hosAudit.PerformCompleteAudit(user, context, employeeLogList, dateTime);
        test7DayCorrect(logSummaryList);

        employeeLogList = buildWeekOfLogs(new RuleSetTypeEnum(RuleSetTypeEnum.USCONSTRUCTION_7DAY));
        logSummaryList = hosAudit.PerformCompleteAudit(user, context, employeeLogList, dateTime);
        test7DayCorrect(logSummaryList);
    }

    @Test
    public void performCompleteAudit_8WeekOfLogs_ShouldCorrectlyHaveWholeWeek() {

        Date dateTime = new DateTime(2017, 2, 14, 13, 0, 0).toDate();
        List<EmployeeLog> employeeLogList = buildWeekOfLogs(new RuleSetTypeEnum(RuleSetTypeEnum.US70HOUR));
        List<LogSummary> logSummaryList = hosAudit.PerformCompleteAudit(user, context, employeeLogList, dateTime);
        test8DayCorrect(logSummaryList);

        employeeLogList = buildWeekOfLogs(new RuleSetTypeEnum(RuleSetTypeEnum.FLORIDA_8DAY));
        logSummaryList = hosAudit.PerformCompleteAudit(user, context, employeeLogList, dateTime);
        test8DayCorrect(logSummaryList);

        employeeLogList = buildWeekOfLogs(new RuleSetTypeEnum(RuleSetTypeEnum.WISCONSIN_8DAY));
        logSummaryList = hosAudit.PerformCompleteAudit(user, context, employeeLogList, dateTime);
        test8DayCorrect(logSummaryList);

        employeeLogList = buildWeekOfLogs(new RuleSetTypeEnum(RuleSetTypeEnum.USOILFIELD));
        logSummaryList = hosAudit.PerformCompleteAudit(user, context, employeeLogList, dateTime);
        test8DayCorrect(logSummaryList);

        employeeLogList = buildWeekOfLogs(new RuleSetTypeEnum(RuleSetTypeEnum.TEXASOILFIELD));
        logSummaryList = hosAudit.PerformCompleteAudit(user, context, employeeLogList, dateTime);
        test8DayCorrect(logSummaryList);

        employeeLogList = buildWeekOfLogs(new RuleSetTypeEnum(RuleSetTypeEnum.USCONSTRUCTION_8DAY));
        logSummaryList = hosAudit.PerformCompleteAudit(user, context, employeeLogList, dateTime);
        test8DayCorrect(logSummaryList);
    }

    @Test
    public void performCompleteAudit_MissingLogs_ShouldHaveEmptylogs(){
        RuleSetTypeEnum ruleType = new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR);
        EmployeeLog log0 = createLog(new DateTime(2017, 2, 9, 13, 0, 0), new DateTime(2017, 2, 9, 15, 0, 0), 1000f, 2000f, ruleType);
        EmployeeLog log1 = createLog(new DateTime(2017, 2, 10, 13, 0, 0), new DateTime(2017, 2, 10, 15, 0, 0), 1000f, 2000f, ruleType);
        EmployeeLog log2 = createLog(new DateTime(2017, 2, 14, 13, 0, 0), new DateTime(2017, 2, 14, 15, 0, 0), 1000f, 2000f, ruleType);

        List<EmployeeLog> employeeLogList = new LinkedList<>();
        employeeLogList.add(log0);
        employeeLogList.add(log1);
        employeeLogList.add(log2);
        Date dateTime = new DateTime(2017, 2, 14, 13, 0, 0).toDate();

        List<LogSummary> logSummaryList = hosAudit.PerformCompleteAudit(user, context, employeeLogList, dateTime);


        Assert.assertEquals(6, logSummaryList.size());

        LogSummary currentSummary = logSummaryList.get(0);
        Assert.assertEquals(new DateTime(2017, 2, 14, 0, 0, 0).toDate(), currentSummary.getLogDate());
        Assert.assertEquals(true, currentSummary.getLogExists());
        Assert.assertEquals(true, currentSummary.getIsComplete());
        Assert.assertEquals(TimeUnit.MILLISECONDS.convert(360, TimeUnit.MINUTES), currentSummary.getWeeklyTotalDuration().longValue());

        currentSummary = logSummaryList.get(2);
        Assert.assertEquals(new DateTime(2017, 2, 12, 0, 0, 0).toDate(), currentSummary.getLogDate());
        Assert.assertEquals(false, currentSummary.getLogExists());
        Assert.assertEquals(false, currentSummary.getIsComplete());
        Assert.assertEquals(null, currentSummary.getDrivingDuration());
        Assert.assertEquals(null, currentSummary.getOnDutyDuration());
        Assert.assertEquals(-1f, currentSummary.getDistance());
        Assert.assertEquals(null, currentSummary.getWeeklyTotalDuration());

        currentSummary = logSummaryList.get(5);
        Assert.assertEquals(new DateTime(2017, 2, 9, 0, 0, 0).toDate(), currentSummary.getLogDate());
        Assert.assertEquals(true, currentSummary.getLogExists());
        Assert.assertEquals(true, currentSummary.getIsComplete());
    }

    private void test7DayCorrect(List<LogSummary> logSummaryList){
        LogSummary currentTestLog = logSummaryList.get(0);
        Assert.assertEquals(new DateTime(2017, 2, 14, 0, 0, 0).toDate(), currentTestLog.getLogDate());
        Assert.assertEquals(TimeUnit.MILLISECONDS.convert(90, TimeUnit.MINUTES), currentTestLog.getDrivingDuration().longValue());//Total Minutes
        Assert.assertEquals(TimeUnit.MILLISECONDS.convert(30, TimeUnit.MINUTES), currentTestLog.getOnDutyDuration().longValue());
        Assert.assertEquals(2000f,  currentTestLog.getDistance());
        Assert.assertEquals(TimeUnit.MILLISECONDS.convert(840, TimeUnit.MINUTES),    currentTestLog.getWeeklyTotalDuration().longValue());
        Assert.assertEquals(0,      currentTestLog.getOffDutyStart());
        Assert.assertEquals(1320,   currentTestLog.getOffDutyEnd());
        Assert.assertEquals(TimeUnit.MILLISECONDS.convert(120, TimeUnit.MINUTES), currentTestLog.getDailyDurationTotal().longValue());

        currentTestLog = logSummaryList.get(7);
        Assert.assertEquals(new DateTime(2017, 2, 7, 0, 0, 0).toDate(), currentTestLog.getLogDate());
        Assert.assertEquals(TimeUnit.MILLISECONDS.convert(90, TimeUnit.MINUTES), currentTestLog.getDrivingDuration().longValue());//Total Minutes
        Assert.assertEquals(TimeUnit.MILLISECONDS.convert(30, TimeUnit.MINUTES), currentTestLog.getOnDutyDuration().longValue());
        Assert.assertEquals(2000f,  currentTestLog.getDistance());
        Assert.assertEquals(null,    currentTestLog.getWeeklyTotalDuration());
        Assert.assertEquals(0,      currentTestLog.getOffDutyStart());
        Assert.assertEquals(1320,   currentTestLog.getOffDutyEnd());
        Assert.assertEquals(TimeUnit.MILLISECONDS.convert(120, TimeUnit.MINUTES), currentTestLog.getDailyDurationTotal().longValue());
    }

    private void test8DayCorrect(List<LogSummary> logSummaryList){
        LogSummary currentTestLog = logSummaryList.get(0);
        Assert.assertEquals(new DateTime(2017, 2, 14, 0, 0, 0).toDate(), currentTestLog.getLogDate());
        Assert.assertEquals(TimeUnit.MILLISECONDS.convert(90, TimeUnit.MINUTES), currentTestLog.getDrivingDuration().longValue()); //Total Minutes
        Assert.assertEquals(TimeUnit.MILLISECONDS.convert(30, TimeUnit.MINUTES), currentTestLog.getOnDutyDuration().longValue());
        Assert.assertEquals(2000f,  currentTestLog.getDistance());
        Assert.assertEquals(TimeUnit.MILLISECONDS.convert(960, TimeUnit.MINUTES),    currentTestLog.getWeeklyTotalDuration().longValue());
        Assert.assertEquals(0,      currentTestLog.getOffDutyStart());
        Assert.assertEquals(1320,   currentTestLog.getOffDutyEnd());
        Assert.assertEquals(TimeUnit.MILLISECONDS.convert(120, TimeUnit.MINUTES), currentTestLog.getDailyDurationTotal().longValue());

        currentTestLog = logSummaryList.get(7);
        Assert.assertEquals(new DateTime(2017, 2, 7, 0, 0, 0).toDate(), currentTestLog.getLogDate());
        Assert.assertEquals(TimeUnit.MILLISECONDS.convert(90, TimeUnit.MINUTES), currentTestLog.getDrivingDuration().longValue());//Total Minutes
        Assert.assertEquals(TimeUnit.MILLISECONDS.convert(30, TimeUnit.MINUTES), currentTestLog.getOnDutyDuration().longValue());
        Assert.assertEquals(2000f,  currentTestLog.getDistance());
        Assert.assertEquals(null,    currentTestLog.getWeeklyTotalDuration());
        Assert.assertEquals(0,      currentTestLog.getOffDutyStart());
        Assert.assertEquals(1320,   currentTestLog.getOffDutyEnd());
        Assert.assertEquals(TimeUnit.MILLISECONDS.convert(120, TimeUnit.MINUTES), currentTestLog.getDailyDurationTotal().longValue());
    }

    private List<EmployeeLog> buildWeekOfLogs(RuleSetTypeEnum ruleType){
        List<EmployeeLog> employeeLogList = new LinkedList<>();
        EmployeeLog log0 = createLog(new DateTime(2017, 2, 7, 13, 0, 0), new DateTime(2017, 2, 7, 15, 0, 0), 1000f, 2000f, ruleType);
        EmployeeLog log1 = createLog(new DateTime(2017, 2, 8, 13, 0, 0), new DateTime(2017, 2, 8, 15, 0, 0), 1000f, 2000f, ruleType);
        EmployeeLog log2 = createLog(new DateTime(2017, 2, 9, 13, 0, 0), new DateTime(2017, 2, 9, 15, 0, 0), 1000f, 2000f, ruleType);
        EmployeeLog log3 = createLog(new DateTime(2017, 2, 10, 13, 0, 0), new DateTime(2017, 2, 10, 15, 0, 0), 1000f, 2000f, ruleType);
        EmployeeLog log4 = createLog(new DateTime(2017, 2, 11, 13, 0, 0), new DateTime(2017, 2, 11, 15, 0, 0), 1000f, 2000f, ruleType);
        EmployeeLog log5 = createLog(new DateTime(2017, 2, 12, 13, 0, 0), new DateTime(2017, 2, 12, 15, 0, 0), 1000f, 2000f, ruleType);
        EmployeeLog log6 = createLog(new DateTime(2017, 2, 13, 13, 0, 0), new DateTime(2017, 2, 13, 15, 0, 0), 1000f, 2000f, ruleType);
        EmployeeLog log7 = createLog(new DateTime(2017, 2, 14, 13, 0, 0), new DateTime(2017, 2, 14, 15, 0, 0), 1000f, 2000f, ruleType);

        employeeLogList.add(log0);
        employeeLogList.add(log1);
        employeeLogList.add(log2);
        employeeLogList.add(log3);
        employeeLogList.add(log4);
        employeeLogList.add(log5);
        employeeLogList.add(log6);
        employeeLogList.add(log7);

        return employeeLogList;
    }


    private User createUser(){
        User user = new User();
        LoginCredentials credentials = new LoginCredentials();
        credentials.setEmployeeId("Super Cool Unit Test Employee");
        user.setHomeTerminalTimeZone(TimeZoneEnum.CENTRAL_STANDARD_TIME);
        user.setCredentials(credentials);

        return user;
    }


    private EmployeeLog createLog(DateTime dateTime, DateTime endTime, float logDistance, float mobileDistance, RuleSetTypeEnum ruleSetTypeEnum){
        EmployeeLog log = new EmployeeLog();
        log.setLogDate(dateTime.withHourOfDay(0).toDate());
        log.setTotalLogDistance(logDistance);
        log.setMobileStartTimestamp(dateTime.toDate());
        log.setEldEventList(createEldEventList(dateTime, endTime, mobileDistance));
        log.setRuleset(ruleSetTypeEnum);

        return log;
    }

    private static EmployeeLogEldEventList createEldEventList(DateTime startTime, DateTime endTime, float totalDistance){
        EmployeeLogEldEventList eldEventList = new EmployeeLogEldEventList();
        EmployeeLogEldEvent[] eldEvents = new EmployeeLogEldEvent[5];

        eldEvents[0] = createEldEvent(startTime.plusHours(0), DutyStatusEnum.ONDUTY, 0, 0);
        eldEvents[1] = createEldEvent(startTime.plusMinutes(30), DutyStatusEnum.DRIVING, 0, totalDistance / 4);
        eldEvents[2] = createEldEvent(startTime.plusMinutes(30), DutyStatusEnum.DRIVING, totalDistance / 4, totalDistance / 2);
        eldEvents[3] = createEldEvent(startTime.plusHours(1), DutyStatusEnum.DRIVING, totalDistance / 2, totalDistance * 2);
        eldEvents[3] = createEldEvent(endTime, DutyStatusEnum.OFFDUTY, totalDistance, totalDistance);


        eldEventList.setEldEventList(eldEvents);

        return eldEventList;
    }

    private static EmployeeLogEldEvent createEldEvent(DateTime eventDateTime, int dutyStatusEnum, float startOdometer, float endOdometer){
        EmployeeLogEldEvent eldEvent = new EmployeeLogEldEvent(eventDateTime.toDate(), new EmployeeLogEldEventCode(dutyStatusEnum), Enums.EmployeeLogEldEventType.DutyStatusChange);
        eldEvent.setEventType(Enums.EmployeeLogEldEventType.DutyStatusChange);
        eldEvent.setDutyStatusEnum(new DutyStatusEnum(dutyStatusEnum));
        eldEvent.setOdometer(startOdometer);
        eldEvent.setEndOdometer(endOdometer);
        return eldEvent;
    }

}
