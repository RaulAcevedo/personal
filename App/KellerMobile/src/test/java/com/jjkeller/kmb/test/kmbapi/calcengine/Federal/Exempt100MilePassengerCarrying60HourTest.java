package com.jjkeller.kmb.test.kmbapi.calcengine.Federal;



import com.jjkeller.kmb.test.KMBRoboElectricTestRunner;
import com.jjkeller.kmbapi.calcengine.Enums.DutyStatusEnum;
import com.jjkeller.kmbapi.calcengine.Enums.RuleSetTypeEnum;
import com.jjkeller.kmbapi.calcengine.LogProperties;
import com.jjkeller.kmbapi.calcengine.RulesetFactory;
import com.jjkeller.kmbapi.controller.utility.DateUtility;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.ParseException;

@RunWith(KMBRoboElectricTestRunner.class)
public class Exempt100MilePassengerCarrying60HourTest extends ExemptTestBase {

    @Before
    public void setUp() throws Exception {
        _rulesetEngine = RulesetFactory.ForExempt100MilePassengerCarrying60Hour();
        _ruleset = RuleSetTypeEnum.US60Hour;
    }

    @Test
    public void test_Smoke() throws ParseException {
        LogProperties logProperties = new LogProperties();
        logProperties.setLogDate(DateUtility.getDateFormat().parse("06/15/2015"));
        _rulesetEngine.PrepareStartOfLog(logProperties);

        this.processEventAndAssertResults("06/15/2015 00:00:00", "06:00:00", DutyStatusEnum.OFF, "10:00", "12:00", "60:00");
        this.processEventAndAssertResults("06/15/2015 06:00:00", "02:00:00", DutyStatusEnum.DRV, "08:00", "10:00", "58:00");
    }

    @Test
    public void test_12HoursOnDuty() throws ParseException {
        LogProperties logProperties = new LogProperties();
        logProperties.setLogDate(DateUtility.getDateFormat().parse("06/15/2015"));
        _rulesetEngine.PrepareStartOfLog(logProperties);

        this.processEventAndAssertResults("06/15/2015 00:00:00", "01:00:00", DutyStatusEnum.OFF, "10:00", "12:00", "60:00");
        this.processEventAndAssertResults("06/15/2015 01:00:00", "12:00:00", DutyStatusEnum.ON, "10:00", "00:00", "48:00");
    }

    @Test
    public void test_12HoursOnDutySpanningMidnight() throws ParseException {
        LogProperties logProperties = new LogProperties();
        logProperties.setLogDate(DateUtility.getDateFormat().parse("06/15/2015"));
        _rulesetEngine.PrepareStartOfLog(logProperties);

        this.processEventAndAssertResults("06/15/2015 00:00:00", "21:00:00", DutyStatusEnum.OFF, "10:00", "12:00", "60:00");
        this.processEventAndAssertResults("06/15/2015 21:00:00", "03:00:00", DutyStatusEnum.ON, "10:00", "09:00", "57:00");
        this.processEventAndAssertResults("06/16/2015 00:00:00", "09:00:00", DutyStatusEnum.ON, "10:00", "00:00", "48:00");
    }

    @Test
    public void test_12HoursOn10HoursOff12HoursOn() throws ParseException {
        LogProperties logProperties = new LogProperties();
        logProperties.setLogDate(DateUtility.getDateFormat().parse("06/15/2015"));
        _rulesetEngine.PrepareStartOfLog(logProperties);

        this.processEventAndAssertResults("06/15/2015 00:00:00", "11:00:00", DutyStatusEnum.OFF, "10:00", "12:00", "60:00");
        this.processEventAndAssertResults("06/15/2015 11:00:00", "12:00:00", DutyStatusEnum.ON, "10:00", "00:00", "48:00");
        this.processEventAndAssertResults("06/15/2015 23:00:00", "01:00:00", DutyStatusEnum.OFF, "10:00", "00:00", "48:00");
        this.processEventAndAssertResults("06/16/2015 00:00:00", "09:00:00", DutyStatusEnum.OFF, "10:00", "12:00", "48:00");
        this.processEventAndAssertResults("06/16/2015 09:00:00", "12:00:00", DutyStatusEnum.ON, "10:00", "00:00", "36:00");
    }

    @Test
    public void test_12HoursOn10HoursOff12HoursOnSpanningMidnight() throws ParseException {
        LogProperties logProperties = new LogProperties();
        logProperties.setLogDate(DateUtility.getDateFormat().parse("06/15/2015"));
        _rulesetEngine.PrepareStartOfLog(logProperties);

        this.processEventAndAssertResults("06/15/2015 00:00:00", "11:00:00", DutyStatusEnum.OFF, "10:00", "12:00", "60:00");
        this.processEventAndAssertResults("06/15/2015 11:00:00", "12:00:00", DutyStatusEnum.ON, "10:00", "00:00", "48:00");
        this.processEventAndAssertResults("06/15/2015 23:00:00", "01:00:00", DutyStatusEnum.OFF, "10:00", "00:00", "48:00");
        this.processEventAndAssertResults("06/16/2015 00:00:00", "09:00:00", DutyStatusEnum.OFF, "10:00", "12:00", "48:00");
        this.processEventAndAssertResults("06/16/2015 09:00:00", "12:00:00", DutyStatusEnum.ON, "10:00", "00:00", "36:00");
    }

    @Test
    public void test_4HoursOn12HoursOff5HoursOn() throws ParseException {
        LogProperties logProperties = new LogProperties();
        logProperties.setLogDate(DateUtility.getDateFormat().parse("06/15/2015"));
        _rulesetEngine.PrepareStartOfLog(logProperties);

        this.processEventAndAssertResults("06/15/2015 00:00:00", "01:00:00", DutyStatusEnum.OFF, "10:00", "12:00", "60:00");
        this.processEventAndAssertResults("06/15/2015 01:00:00", "04:00:00", DutyStatusEnum.ON, "10:00", "08:00", "56:00");
        this.processEventAndAssertResults("06/15/2015 05:00:00", "12:00:00", DutyStatusEnum.OFF, "10:00", "12:00", "56:00");
        this.processEventAndAssertResults("06/15/2015 17:00:00", "05:00:00", DutyStatusEnum.ON, "10:00", "07:00", "51:00");
    }

    @Test
    public void test_10HoursDriving() throws ParseException {
        LogProperties logProperties = new LogProperties();
        logProperties.setLogDate(DateUtility.getDateFormat().parse("06/15/2015"));
        _rulesetEngine.PrepareStartOfLog(logProperties);

        this.processEventAndAssertResults("06/15/2015 00:00:00", "01:00:00", DutyStatusEnum.OFF, "10:00", "12:00", "60:00");
        this.processEventAndAssertResults("06/15/2015 01:00:00", "10:00:00", DutyStatusEnum.DRV, "00:00", "02:00", "50:00");
    }

    @Test
    public void test_12HoursOnWithResetGap() throws ParseException {
        LogProperties logProperties = new LogProperties();
        logProperties.setLogDate(DateUtility.getDateFormat().parse("06/15/2015"));
        _rulesetEngine.PrepareStartOfLog(logProperties);

        this.processEventAndAssertResults("06/15/2015 00:00:00", "01:00:00", DutyStatusEnum.OFF, "10:00", "12:00", "60:00");
        this.processEventAndAssertResults("06/15/2015 01:00:00", "01:00:00", DutyStatusEnum.ON, "10:00", "11:00", "59:00");
        this.processEventAndAssertResults("06/15/2015 02:00:00", "11:00:00", DutyStatusEnum.OFF, "10:00", "12:00", "59:00");
        this.processEventAndAssertResults("06/15/2015 13:00:00", "01:00:00", DutyStatusEnum.ON, "10:00", "11:00", "58:00");
    }

    @Test
    public void test_12HoursOnWithNonResetGap() throws ParseException {
        LogProperties logProperties = new LogProperties();
        logProperties.setLogDate(DateUtility.getDateFormat().parse("06/15/2015"));
        _rulesetEngine.PrepareStartOfLog(logProperties);

        this.processEventAndAssertResults("06/15/2015 00:00:00", "01:00:00", DutyStatusEnum.OFF, "10:00", "12:00", "60:00");
        this.processEventAndAssertResults("06/15/2015 01:00:00", "01:00:00", DutyStatusEnum.ON, "10:00", "11:00", "59:00");
        this.processEventAndAssertResults("06/15/2015 02:00:00", "07:00:00", DutyStatusEnum.OFF, "10:00", "04:00", "59:00");
        this.processEventAndAssertResults("06/15/2015 09:00:00", "01:00:00", DutyStatusEnum.ON, "10:00", "03:00", "58:00");
    }

    @Test
    public void test_4Day() throws ParseException {
        LogProperties logProperties = new LogProperties();
        logProperties.setLogDate(DateUtility.getDateFormat().parse("06/15/2015"));
        _rulesetEngine.PrepareStartOfLog(logProperties);

        this.processEventAndAssertResults("06/15/2015 00:00:00", "14:00:00", DutyStatusEnum.OFF, "10:00", "12:00", "60:00");
        this.processEventAndAssertResults("06/15/2015 14:00:00", "10:00:00", DutyStatusEnum.ON, "10:00", "02:00", "50:00");
        this.processEventAndAssertResults("06/16/2015 00:00:00", "02:00:00", DutyStatusEnum.ON, "10:00", "00:00", "48:00");
        this.processEventAndAssertResults("06/16/2015 02:00:00", "08:00:00", DutyStatusEnum.OFF, "10:00", "12:00", "48:00");
        this.processEventAndAssertResults("06/16/2015 10:00:00", "12:00:00", DutyStatusEnum.ON, "10:00", "00:00", "36:00");
        this.processEventAndAssertResults("06/16/2015 22:00:00", "02:00:00", DutyStatusEnum.OFF, "10:00", "00:00", "36:00");
        this.processEventAndAssertResults("06/17/2015 00:00:00", "06:00:00", DutyStatusEnum.OFF, "10:00", "12:00", "36:00");
        this.processEventAndAssertResults("06/17/2015 06:00:00", "10:00:00", DutyStatusEnum.DRV, "00:00", "02:00", "26:00");
        this.processEventAndAssertResults("06/17/2015 16:00:00", "08:00:00", DutyStatusEnum.OFF, "10:00", "12:00", "26:00");
        this.processEventAndAssertResults("06/18/2015 00:00:00", "08:00:00", DutyStatusEnum.DRV, "02:00", "04:00", "18:00");
    }

    @Test
    public void test_34HourReset_NotValid() throws ParseException {
        LogProperties logProperties = new LogProperties();
        logProperties.setLogDate(DateUtility.getDateFormat().parse("06/15/2015"));
        _rulesetEngine.PrepareStartOfLog(logProperties);

        this.processEventAndAssertResults("06/15/2015 00:00:00", "12:00:00", DutyStatusEnum.OFF, "10:00", "12:00", "60:00");
        this.processEventAndAssertResults("06/15/2015 12:00:00", "12:00:00", DutyStatusEnum.ON, "10:00", "00:00", "48:00");
        this.processEventAndAssertResults("06/16/2015 00:00:00", "08:00:00", DutyStatusEnum.OFF, "10:00", "12:00", "48:00");
        this.processEventAndAssertResults("06/16/2015 08:00:00", "12:00:00", DutyStatusEnum.ON, "10:00", "00:00", "36:00");
        this.processEventAndAssertResults("06/16/2015 20:00:00", "04:00:00", DutyStatusEnum.OFF, "10:00", "00:00", "36:00");
        this.processEventAndAssertResults("06/17/2015 00:00:00", "24:00:00", DutyStatusEnum.OFF, "10:00", "12:00", "36:00");
        this.processEventAndAssertResults("06/18/2015 00:00:00", "06:00:00", DutyStatusEnum.OFF, "10:00", "12:00", "36:00");
        this.processEventAndAssertResults("06/18/2015 06:00:00", "10:00:00", DutyStatusEnum.DRV, "00:00", "02:00", "26:00");
        this.processEventAndAssertResults("06/18/2015 16:00:00", "08:00:00", DutyStatusEnum.OFF, "10:00", "12:00", "26:00");
        this.processEventAndAssertResults("06/19/2015 00:00:00", "08:00:00", DutyStatusEnum.DRV, "02:00", "04:00", "18:00");
    }

    @Test
    public void test_8Day() throws ParseException {
        LogProperties logProperties = new LogProperties();
        logProperties.setLogDate(DateUtility.getDateFormat().parse("06/15/2015"));
        _rulesetEngine.PrepareStartOfLog(logProperties);

        this.processEventAndAssertResults("06/15/2015 00:00:00", "06:00:00", DutyStatusEnum.OFF, "10:00", "12:00", "60:00");
        this.processEventAndAssertResults("06/15/2015 06:00:00", "06:00:00", DutyStatusEnum.DRV, "04:00", "06:00", "54:00");
        this.processEventAndAssertResults("06/15/2015 12:00:00", "01:00:00", DutyStatusEnum.OFF, "04:00", "05:00", "54:00");
        this.processEventAndAssertResults("06/15/2015 13:00:00", "04:00:00", DutyStatusEnum.ON, "04:00", "01:00", "50:00");
        this.processEventAndAssertResults("06/15/2015 17:00:00", "07:00:00", DutyStatusEnum.OFF, "04:00", "00:00", "50:00");

        this.processEventAndAssertResults("06/16/2015 00:00:00", "06:00:00", DutyStatusEnum.OFF, "10:00", "12:00", "50:00");
        this.processEventAndAssertResults("06/16/2015 06:00:00", "06:00:00", DutyStatusEnum.DRV, "04:00", "06:00", "44:00");
        this.processEventAndAssertResults("06/16/2015 12:00:00", "01:00:00", DutyStatusEnum.OFF, "04:00", "05:00", "44:00");
        this.processEventAndAssertResults("06/16/2015 13:00:00", "04:00:00", DutyStatusEnum.ON, "04:00", "01:00", "40:00");
        this.processEventAndAssertResults("06/16/2015 17:00:00", "07:00:00", DutyStatusEnum.OFF, "04:00", "00:00", "40:00");

        this.processEventAndAssertResults("06/17/2015 00:00:00", "06:00:00", DutyStatusEnum.OFF, "10:00", "12:00", "40:00");
        this.processEventAndAssertResults("06/17/2015 06:00:00", "06:00:00", DutyStatusEnum.DRV, "04:00", "06:00", "34:00");
        this.processEventAndAssertResults("06/17/2015 12:00:00", "01:00:00", DutyStatusEnum.OFF, "04:00", "05:00", "34:00");
        this.processEventAndAssertResults("06/17/2015 13:00:00", "04:00:00", DutyStatusEnum.ON, "04:00", "01:00", "30:00");
        this.processEventAndAssertResults("06/17/2015 17:00:00", "07:00:00", DutyStatusEnum.OFF, "04:00", "00:00", "30:00");

        this.processEventAndAssertResults("06/18/2015 00:00:00", "06:00:00", DutyStatusEnum.OFF, "10:00", "12:00", "30:00");
        this.processEventAndAssertResults("06/18/2015 06:00:00", "06:00:00", DutyStatusEnum.DRV, "04:00", "06:00", "24:00");
        this.processEventAndAssertResults("06/18/2015 12:00:00", "01:00:00", DutyStatusEnum.OFF, "04:00", "05:00", "24:00");
        this.processEventAndAssertResults("06/18/2015 13:00:00", "04:00:00", DutyStatusEnum.ON, "04:00", "01:00", "20:00");
        this.processEventAndAssertResults("06/18/2015 17:00:00", "07:00:00", DutyStatusEnum.OFF, "04:00", "00:00", "20:00");

        this.processEventAndAssertResults("06/19/2015 00:00:00", "06:00:00", DutyStatusEnum.OFF, "10:00", "12:00", "20:00");
        this.processEventAndAssertResults("06/19/2015 06:00:00", "06:00:00", DutyStatusEnum.DRV, "04:00", "06:00", "14:00");
        this.processEventAndAssertResults("06/19/2015 12:00:00", "01:00:00", DutyStatusEnum.OFF, "04:00", "05:00", "14:00");
        this.processEventAndAssertResults("06/19/2015 13:00:00", "04:00:00", DutyStatusEnum.ON, "04:00", "01:00", "10:00");
        this.processEventAndAssertResults("06/19/2015 17:00:00", "07:00:00", DutyStatusEnum.OFF, "04:00", "00:00", "10:00");

        this.processEventAndAssertResults("06/20/2015 00:00:00", "24:00:00", DutyStatusEnum.OFF, "10:00", "12:00", "10:00");

        this.processEventAndAssertResults("06/21/2015 00:00:00", "01:00:00", DutyStatusEnum.OFF, "10:00", "12:00", "10:00");
        this.processEventAndAssertResults("06/21/2015 01:00:00", "01:00:00", DutyStatusEnum.ON, "10:00", "11:00", "09:00");
        this.processEventAndAssertResults("06/21/2015 02:00:00", "22:00:00", DutyStatusEnum.OFF, "10:00", "12:00", "09:00");

        this.processEventAndAssertResults("06/22/2015 00:00:00", "06:00:00", DutyStatusEnum.OFF, "10:00", "12:00", "19:00");
        this.processEventAndAssertResults("06/22/2015 06:00:00", "06:00:00", DutyStatusEnum.DRV, "04:00", "06:00", "13:00");
        this.processEventAndAssertResults("06/22/2015 12:00:00", "01:00:00", DutyStatusEnum.OFF, "04:00", "05:00", "13:00");
        this.processEventAndAssertResults("06/22/2015 13:00:00", "04:00:00", DutyStatusEnum.ON, "04:00", "01:00", "09:00");
        this.processEventAndAssertResults("06/22/2015 17:00:00", "07:00:00", DutyStatusEnum.OFF, "04:00", "00:00", "09:00");
    }

    @Test
    public void test_ExemptNonexemptExempt() throws ParseException {
        LogProperties logProperties = new LogProperties();
        logProperties.setLogDate(DateUtility.getDateFormat().parse("06/15/2015"));
        _rulesetEngine.PrepareStartOfLog(logProperties);

        this.processEventAndAssertResults("06/15/2015 00:00:00", "06:00:00", DutyStatusEnum.OFF, "10:00", "12:00", "60:00");
        this.processEventAndAssertResults("06/15/2015 06:00:00", "10:00:00", DutyStatusEnum.DRV, "00:00", "02:00", "50:00");
        this.processEventAndAssertResults("06/15/2015 16:00:00", "02:00:00", DutyStatusEnum.ON, "00:00", "00:00", "48:00");
        this.processEventAndAssertResults("06/15/2015 18:00:00", "06:00:00", DutyStatusEnum.OFF, "00:00", "00:00", "48:00");

        // non-exempt day
        this.processEventAndAssertResults("06/16/2015 00:00:00", "06:00:00", DutyStatusEnum.OFF, "10:00", "12:00", "48:00");
        this.processEventAndAssertResults("06/16/2015 06:00:00", "13:00:00", DutyStatusEnum.ON, "10:00", "00:00", "35:00");
        this.processEventAndAssertResults("06/16/2015 19:00:00", "05:00:00", DutyStatusEnum.OFF, "10:00", "00:00", "35:00");

        // non-exempt day
        this.processEventAndAssertResults("06/17/2015 00:00:00", "06:00:00", DutyStatusEnum.OFF, "10:00", "12:00", "35:00");
        this.processEventAndAssertResults("06/17/2015 06:00:00", "12:00:00", DutyStatusEnum.DRV, "00:00", "00:00", "23:00");
        this.processEventAndAssertResults("06/17/2015 18:00:00", "06:00:00", DutyStatusEnum.OFF, "00:00", "00:00", "23:00");

        this.processEventAndAssertResults("06/18/2015 00:00:00", "06:00:00", DutyStatusEnum.OFF, "10:00", "12:00", "23:00");
        this.processEventAndAssertResults("06/18/2015 06:00:00", "09:00:00", DutyStatusEnum.DRV, "01:00", "03:00", "14:00");
        this.processEventAndAssertResults("06/18/2015 09:00:00", "02:00:00", DutyStatusEnum.ON, "01:00", "01:00", "12:00");
        this.processEventAndAssertResults("06/18/2015 17:00:00", "07:00:00", DutyStatusEnum.OFF, "01:00", "00:00", "12:00");

        this.processEventAndAssertResults("06/19/2015 00:00:00", "07:00:00", DutyStatusEnum.OFF, "10:00", "12:00", "12:00");
        this.processEventAndAssertResults("06/19/2015 07:00:00", "08:00:00", DutyStatusEnum.DRV, "02:00", "04:00", "04:00");
        this.processEventAndAssertResults("06/19/2015 15:00:00", "02:00:00", DutyStatusEnum.ON, "02:00", "02:00", "02:00");
        this.processEventAndAssertResults("06/19/2015 17:00:00", "07:00:00", DutyStatusEnum.OFF, "02:00", "00:00", "02:00");

        // non-exempt day
        this.processEventAndAssertResults("06/20/2015 00:00:00", "06:00:00", DutyStatusEnum.OFF, "10:00", "12:00", "02:00");
        this.processEventAndAssertResults("06/20/2015 06:00:00", "13:00:00", DutyStatusEnum.ON, "10:00", "00:00", "00:00");
        this.processEventAndAssertResults("06/20/2015 19:00:00", "05:00:00", DutyStatusEnum.OFF, "10:00", "00:00", "00:00");
    }
}
