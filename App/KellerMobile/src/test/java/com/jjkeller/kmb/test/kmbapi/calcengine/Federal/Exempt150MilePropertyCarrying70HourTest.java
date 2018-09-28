package com.jjkeller.kmb.test.kmbapi.calcengine.Federal;



import com.jjkeller.kmb.test.KMBRoboElectricTestRunner;
import com.jjkeller.kmbapi.calcengine.Enums.DutyStatusEnum;
import com.jjkeller.kmbapi.calcengine.Enums.RuleSetTypeEnum;
import com.jjkeller.kmbapi.calcengine.Federal.Exempt150MilePropertyCarrying70Hour;
import com.jjkeller.kmbapi.calcengine.LogProperties;
import com.jjkeller.kmbapi.calcengine.RulesetProperties;
import com.jjkeller.kmbapi.controller.utility.DateUtility;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.ParseException;

@RunWith(KMBRoboElectricTestRunner.class)
public class Exempt150MilePropertyCarrying70HourTest extends ExemptTestBase {

    @Before
    public void setUp() throws Exception {


        RulesetProperties properties = new RulesetProperties();
        properties.setIs34HourResetAllowed(true);

        //_rulesetEngine = RulesetFactory.ForExempt150MilePropertyCarrying70Hour(true);
        _rulesetEngine = new Exempt150MilePropertyCarrying70Hour(properties, new MockLogCheckerComplianceDatesControllerAllActive());
        _ruleset = RuleSetTypeEnum.US70Hour;
    }


    @Test
    public void test_Smoke() throws ParseException {
        LogProperties logProperties = new LogProperties();
        logProperties.setLogDate(DateUtility.getDateFormat().parse("06/15/2015"));
        _rulesetEngine.PrepareStartOfLog(logProperties);

        this.processEventAndAssertResults("06/15/2015 00:00:00", "06:00:00", DutyStatusEnum.OFF, "11:00", "14:00", "70:00");
        this.processEventAndAssertResults("06/15/2015 06:00:00", "02:00:00", DutyStatusEnum.DRV, "09:00", "12:00", "68:00");
    }

    @Test
    public void test_14HoursOnDuty() throws ParseException {
        LogProperties logProperties = new LogProperties();
        logProperties.setLogDate(DateUtility.getDateFormat().parse("06/15/2015"));
        _rulesetEngine.PrepareStartOfLog(logProperties);

        this.processEventAndAssertResults("06/15/2015 00:00:00", "01:00:00", DutyStatusEnum.OFF, "11:00", "14:00", "70:00");
        this.processEventAndAssertResults("06/15/2015 01:00:00", "14:00:00", DutyStatusEnum.ON, "11:00", "00:00", "56:00");
    }

    @Test
    public void test_14HoursOnDutySpanningMidnight() throws ParseException {
        LogProperties logProperties = new LogProperties();
        logProperties.setLogDate(DateUtility.getDateFormat().parse("06/15/2015"));
        _rulesetEngine.PrepareStartOfLog(logProperties);

        this.processEventAndAssertResults("06/15/2015 00:00:00", "21:00:00", DutyStatusEnum.OFF, "11:00", "14:00", "70:00");
        this.processEventAndAssertResults("06/15/2015 21:00:00", "03:00:00", DutyStatusEnum.ON, "11:00", "11:00", "67:00");
        this.processEventAndAssertResults("06/16/2015 00:00:00", "11:00:00", DutyStatusEnum.ON, "11:00", "00:00", "56:00");
    }

    @Test
    public void test_14HoursOn10HoursOff14HoursOn() throws ParseException {
        LogProperties logProperties = new LogProperties();
        logProperties.setLogDate(DateUtility.getDateFormat().parse("06/15/2015"));
        _rulesetEngine.PrepareStartOfLog(logProperties);

        this.processEventAndAssertResults("06/15/2015 00:00:00", "01:00:00", DutyStatusEnum.OFF, "11:00", "14:00", "70:00");
        this.processEventAndAssertResults("06/15/2015 01:00:00", "14:00:00", DutyStatusEnum.ON, "11:00", "00:00", "56:00");
        this.processEventAndAssertResults("06/15/2015 15:00:00", "09:00:00", DutyStatusEnum.OFF, "11:00", "00:00", "56:00");
        this.processEventAndAssertResults("06/16/2015 00:00:00", "01:00:00", DutyStatusEnum.OFF, "11:00", "14:00", "56:00");
        this.processEventAndAssertResults("06/16/2015 01:00:00", "14:00:00", DutyStatusEnum.ON, "11:00", "00:00", "42:00");
    }

    @Test
    public void test_14HoursOn10HoursOff14HoursOnSpanningMidnight() throws ParseException {
        LogProperties logProperties = new LogProperties();
        logProperties.setLogDate(DateUtility.getDateFormat().parse("06/15/2015"));
        _rulesetEngine.PrepareStartOfLog(logProperties);

        this.processEventAndAssertResults("06/15/2015 00:00:00", "11:00:00", DutyStatusEnum.OFF, "11:00", "14:00", "70:00");
        this.processEventAndAssertResults("06/15/2015 11:00:00", "13:00:00", DutyStatusEnum.ON, "11:00", "01:00", "57:00");
        this.processEventAndAssertResults("06/16/2015 00:00:00", "01:00:00", DutyStatusEnum.ON, "11:00", "00:00", "56:00");
        this.processEventAndAssertResults("06/16/2015 01:00:00", "10:00:00", DutyStatusEnum.OFF, "11:00", "14:00", "56:00");
        this.processEventAndAssertResults("06/16/2015 11:00:00", "14:00:00", DutyStatusEnum.ON, "11:00", "00:00", "42:00");
    }

    @Test
    public void test_11HoursDriving() throws ParseException {
        LogProperties logProperties = new LogProperties();
        logProperties.setLogDate(DateUtility.getDateFormat().parse("06/15/2015"));
        _rulesetEngine.PrepareStartOfLog(logProperties);

        this.processEventAndAssertResults("06/15/2015 00:00:00", "01:00:00", DutyStatusEnum.OFF, "11:00", "14:00", "70:00");
        this.processEventAndAssertResults("06/15/2015 01:00:00", "11:00:00", DutyStatusEnum.DRV, "00:00", "03:00", "59:00");
    }

    @Test
    public void test_12HoursOnWithResetGap() throws ParseException {
        LogProperties logProperties = new LogProperties();
        logProperties.setLogDate(DateUtility.getDateFormat().parse("06/15/2015"));
        _rulesetEngine.PrepareStartOfLog(logProperties);

        this.processEventAndAssertResults("06/15/2015 00:00:00", "01:00:00", DutyStatusEnum.OFF, "11:00", "14:00", "70:00");
        this.processEventAndAssertResults("06/15/2015 01:00:00", "01:00:00", DutyStatusEnum.ON, "11:00", "13:00", "69:00");
        this.processEventAndAssertResults("06/15/2015 02:00:00", "11:00:00", DutyStatusEnum.OFF, "11:00", "14:00", "69:00");
        this.processEventAndAssertResults("06/15/2015 13:00:00", "01:00:00", DutyStatusEnum.ON, "11:00", "13:00", "68:00");
    }

    @Test
    public void test_12HoursOnWithNonResetGap() throws ParseException {
        LogProperties logProperties = new LogProperties();
        logProperties.setLogDate(DateUtility.getDateFormat().parse("06/15/2015"));
        _rulesetEngine.PrepareStartOfLog(logProperties);

        this.processEventAndAssertResults("06/15/2015 00:00:00", "01:00:00", DutyStatusEnum.OFF, "11:00", "14:00", "70:00");
        this.processEventAndAssertResults("06/15/2015 01:00:00", "01:00:00", DutyStatusEnum.ON, "11:00", "13:00", "69:00");
        this.processEventAndAssertResults("06/15/2015 02:00:00", "07:00:00", DutyStatusEnum.OFF, "11:00", "06:00", "69:00");
        this.processEventAndAssertResults("06/15/2015 09:00:00", "01:00:00", DutyStatusEnum.ON, "11:00", "05:00", "68:00");
    }

    @Test
    public void test_34HourReset() throws ParseException {
        LogProperties logProperties = new LogProperties();
        logProperties.setLogDate(DateUtility.getDateFormat().parse("06/15/2015"));
        _rulesetEngine.PrepareStartOfLog(logProperties);

        this.processEventAndAssertResults("06/15/2015 00:00:00", "09:00:00", DutyStatusEnum.OFF, "11:00", "14:00", "70:00");
        this.processEventAndAssertResults("06/15/2015 09:00:00", "12:00:00", DutyStatusEnum.ON, "11:00", "02:00", "58:00");
        this.processEventAndAssertResults("06/15/2015 21:00:00", "03:00:00", DutyStatusEnum.OFF, "11:00", "00:00", "58:00");
        this.processEventAndAssertResults("06/16/2015 00:00:00", "07:00:00", DutyStatusEnum.OFF, "11:00", "14:00", "58:00");
        this.processEventAndAssertResults("06/16/2015 07:00:00", "10:00:00", DutyStatusEnum.DRV, "01:00", "04:00", "48:00");
        this.processEventAndAssertResults("06/16/2015 17:00:00", "07:00:00", DutyStatusEnum.OFF, "01:00", "00:00", "48:00");
        this.processEventAndAssertResults("06/17/2015 00:00:00", "24:00:00", DutyStatusEnum.OFF, "11:00", "14:00", "48:00");
        this.processEventAndAssertResults("06/18/2015 00:00:00", "03:00:00", DutyStatusEnum.OFF, "11:00", "14:00", "70:00");
        this.processEventAndAssertResults("06/18/2015 03:00:00", "11:00:00", DutyStatusEnum.DRV, "00:00", "03:00", "59:00");
        this.processEventAndAssertResults("06/18/2015 14:00:00", "10:00:00", DutyStatusEnum.OFF, "11:00", "14:00", "59:00");
        this.processEventAndAssertResults("06/19/2015 00:00:00", "08:00:00", DutyStatusEnum.DRV, "03:00", "06:00", "51:00");
    }

    @Test
    public void test_3359HourBreakNoReset() throws ParseException {
        LogProperties logProperties = new LogProperties();
        logProperties.setLogDate(DateUtility.getDateFormat().parse("06/15/2015"));
        _rulesetEngine.PrepareStartOfLog(logProperties);

        this.processEventAndAssertResults("06/15/2015 00:00:00", "09:00:00", DutyStatusEnum.OFF, "11:00", "14:00", "70:00");
        this.processEventAndAssertResults("06/15/2015 09:00:00", "12:00:00", DutyStatusEnum.ON, "11:00", "02:00", "58:00");
        this.processEventAndAssertResults("06/15/2015 21:00:00", "03:00:00", DutyStatusEnum.OFF, "11:00", "00:00", "58:00");
        this.processEventAndAssertResults("06/16/2015 00:00:00", "07:00:00", DutyStatusEnum.OFF, "11:00", "14:00", "58:00");
        this.processEventAndAssertResults("06/16/2015 07:00:00", "10:00:00", DutyStatusEnum.DRV, "01:00", "04:00", "48:00");
        this.processEventAndAssertResults("06/16/2015 17:00:00", "07:00:00", DutyStatusEnum.OFF, "01:00", "00:00", "48:00");
        this.processEventAndAssertResults("06/17/2015 00:00:00", "24:00:00", DutyStatusEnum.OFF, "11:00", "14:00", "48:00");
        this.processEventAndAssertResults("06/18/2015 00:00:00", "02:59:00", DutyStatusEnum.OFF, "11:00", "14:00", "48:00");
        this.processEventAndAssertResults("06/18/2015 02:59:00", "11:00:00", DutyStatusEnum.DRV, "00:00", "03:00", "37:00");
        this.processEventAndAssertResults("06/18/2015 13:59:00", "10:01:00", DutyStatusEnum.OFF, "11:00", "14:00", "37:00");
        this.processEventAndAssertResults("06/19/2015 00:00:00", "08:00:00", DutyStatusEnum.DRV, "03:00", "06:00", "29:00");
    }

    @Test
    public void test_9Day() throws ParseException {
        LogProperties logProperties = new LogProperties();
        logProperties.setLogDate(DateUtility.getDateFormat().parse("06/15/2015"));
        _rulesetEngine.PrepareStartOfLog(logProperties);

        this.processEventAndAssertResults("06/15/2015 00:00:00", "06:00:00", DutyStatusEnum.OFF, "11:00", "14:00", "70:00");
        this.processEventAndAssertResults("06/15/2015 06:00:00", "06:00:00", DutyStatusEnum.DRV, "05:00", "08:00", "64:00");
        this.processEventAndAssertResults("06/15/2015 12:00:00", "01:00:00", DutyStatusEnum.OFF, "05:00", "07:00", "64:00");
        this.processEventAndAssertResults("06/15/2015 13:00:00", "04:00:00", DutyStatusEnum.ON, "05:00", "03:00", "60:00");
        this.processEventAndAssertResults("06/15/2015 17:00:00", "07:00:00", DutyStatusEnum.OFF, "05:00", "00:00", "60:00");

        this.processEventAndAssertResults("06/16/2015 00:00:00", "06:00:00", DutyStatusEnum.OFF, "11:00", "14:00", "60:00");
        this.processEventAndAssertResults("06/16/2015 06:00:00", "06:00:00", DutyStatusEnum.DRV, "05:00", "08:00", "54:00");
        this.processEventAndAssertResults("06/16/2015 12:00:00", "01:00:00", DutyStatusEnum.OFF, "05:00", "07:00", "54:00");
        this.processEventAndAssertResults("06/16/2015 13:00:00", "04:00:00", DutyStatusEnum.ON, "05:00", "03:00", "50:00");
        this.processEventAndAssertResults("06/16/2015 17:00:00", "07:00:00", DutyStatusEnum.OFF, "05:00", "00:00", "50:00");

        this.processEventAndAssertResults("06/17/2015 00:00:00", "06:00:00", DutyStatusEnum.OFF, "11:00", "14:00", "50:00");
        this.processEventAndAssertResults("06/17/2015 06:00:00", "06:00:00", DutyStatusEnum.DRV, "05:00", "08:00", "44:00");
        this.processEventAndAssertResults("06/17/2015 12:00:00", "01:00:00", DutyStatusEnum.OFF, "05:00", "07:00", "44:00");
        this.processEventAndAssertResults("06/17/2015 13:00:00", "04:00:00", DutyStatusEnum.ON, "05:00", "03:00", "40:00");
        this.processEventAndAssertResults("06/17/2015 17:00:00", "07:00:00", DutyStatusEnum.OFF, "05:00", "00:00", "40:00");

        this.processEventAndAssertResults("06/18/2015 00:00:00", "06:00:00", DutyStatusEnum.OFF, "11:00", "14:00", "40:00");
        this.processEventAndAssertResults("06/18/2015 06:00:00", "06:00:00", DutyStatusEnum.DRV, "05:00", "08:00", "34:00");
        this.processEventAndAssertResults("06/18/2015 12:00:00", "01:00:00", DutyStatusEnum.OFF, "05:00", "07:00", "34:00");
        this.processEventAndAssertResults("06/18/2015 13:00:00", "04:00:00", DutyStatusEnum.ON, "05:00", "03:00", "30:00");
        this.processEventAndAssertResults("06/18/2015 17:00:00", "07:00:00", DutyStatusEnum.OFF, "05:00", "00:00", "30:00");

        this.processEventAndAssertResults("06/19/2015 00:00:00", "06:00:00", DutyStatusEnum.OFF, "11:00", "14:00", "30:00");
        this.processEventAndAssertResults("06/19/2015 06:00:00", "06:00:00", DutyStatusEnum.DRV, "05:00", "08:00", "24:00");
        this.processEventAndAssertResults("06/19/2015 12:00:00", "01:00:00", DutyStatusEnum.OFF, "05:00", "07:00", "24:00");
        this.processEventAndAssertResults("06/19/2015 13:00:00", "04:00:00", DutyStatusEnum.ON, "05:00", "03:00", "20:00");
        this.processEventAndAssertResults("06/19/2015 17:00:00", "07:00:00", DutyStatusEnum.OFF, "05:00", "00:00", "20:00");

        this.processEventAndAssertResults("06/20/2015 00:00:00", "24:00:00", DutyStatusEnum.OFF, "11:00", "14:00", "20:00");

        this.processEventAndAssertResults("06/21/2015 00:00:00", "01:00:00", DutyStatusEnum.OFF, "11:00", "14:00", "20:00");
        this.processEventAndAssertResults("06/21/2015 01:00:00", "01:00:00", DutyStatusEnum.ON, "11:00", "13:00", "19:00");
        this.processEventAndAssertResults("06/21/2015 02:00:00", "22:00:00", DutyStatusEnum.OFF, "11:00", "14:00", "19:00");

        this.processEventAndAssertResults("06/22/2015 00:00:00", "06:00:00", DutyStatusEnum.OFF, "11:00", "14:00", "19:00");
        this.processEventAndAssertResults("06/22/2015 06:00:00", "06:00:00", DutyStatusEnum.DRV, "05:00", "08:00", "13:00");
        this.processEventAndAssertResults("06/22/2015 12:00:00", "01:00:00", DutyStatusEnum.OFF, "05:00", "07:00", "13:00");
        this.processEventAndAssertResults("06/22/2015 13:00:00", "04:00:00", DutyStatusEnum.ON, "05:00", "03:00", "09:00");
        this.processEventAndAssertResults("06/22/2015 17:00:00", "07:00:00", DutyStatusEnum.OFF, "05:00", "00:00", "09:00");

        this.processEventAndAssertResults("06/23/2015 00:00:00", "06:00:00", DutyStatusEnum.OFF, "11:00", "14:00", "19:00");
        this.processEventAndAssertResults("06/23/2015 06:00:00", "06:00:00", DutyStatusEnum.DRV, "05:00", "08:00", "13:00");
        this.processEventAndAssertResults("06/23/2015 12:00:00", "01:00:00", DutyStatusEnum.OFF, "05:00", "07:00", "13:00");
        this.processEventAndAssertResults("06/23/2015 13:00:00", "04:00:00", DutyStatusEnum.ON, "05:00", "03:00", "09:00");
        this.processEventAndAssertResults("06/23/2015 17:00:00", "07:00:00", DutyStatusEnum.OFF, "05:00", "00:00", "09:00");
    }

    @Test
    public void test_ExemptNonexemptExempt() throws ParseException {
        LogProperties logProperties = new LogProperties();
        logProperties.setLogDate(DateUtility.getDateFormat().parse("06/15/2015"));
        _rulesetEngine.PrepareStartOfLog(logProperties);

        this.processEventAndAssertResults("06/15/2015 00:00:00", "06:00:00", DutyStatusEnum.OFF, "11:00", "14:00", "70:00");
        this.processEventAndAssertResults("06/15/2015 06:00:00", "10:00:00", DutyStatusEnum.DRV, "01:00", "04:00", "60:00");
        this.processEventAndAssertResults("06/15/2015 16:00:00", "02:00:00", DutyStatusEnum.ON, "01:00", "02:00", "58:00");
        this.processEventAndAssertResults("06/15/2015 18:00:00", "06:00:00", DutyStatusEnum.OFF, "01:00", "00:00", "58:00");

        // non-exempt day
        this.processEventAndAssertResults("06/16/2015 00:00:00", "05:00:00", DutyStatusEnum.OFF, "11:00", "14:00", "58:00");
        this.processEventAndAssertResults("06/16/2015 05:00:00", "15:00:00", DutyStatusEnum.ON, "11:00", "00:00", "43:00");
        this.processEventAndAssertResults("06/16/2015 20:00:00", "04:00:00", DutyStatusEnum.OFF, "11:00", "00:00", "43:00");

        this.processEventAndAssertResults("06/17/2015 00:00:00", "06:00:00", DutyStatusEnum.OFF, "11:00", "14:00", "43:00");
        this.processEventAndAssertResults("06/17/2015 06:00:00", "12:00:00", DutyStatusEnum.DRV, "00:00", "02:00", "31:00");
        this.processEventAndAssertResults("06/17/2015 18:00:00", "06:00:00", DutyStatusEnum.OFF, "00:00", "00:00", "31:00");

        this.processEventAndAssertResults("06/18/2015 00:00:00", "06:00:00", DutyStatusEnum.OFF, "11:00", "14:00", "31:00");
        this.processEventAndAssertResults("06/18/2015 06:00:00", "09:00:00", DutyStatusEnum.DRV, "02:00", "05:00", "22:00");
        this.processEventAndAssertResults("06/18/2015 09:00:00", "02:00:00", DutyStatusEnum.ON, "02:00", "03:00", "20:00");
        this.processEventAndAssertResults("06/18/2015 17:00:00", "07:00:00", DutyStatusEnum.OFF, "02:00", "00:00", "20:00");

        this.processEventAndAssertResults("06/19/2015 00:00:00", "07:00:00", DutyStatusEnum.OFF, "11:00", "14:00", "20:00");
        this.processEventAndAssertResults("06/19/2015 07:00:00", "08:00:00", DutyStatusEnum.DRV, "03:00", "06:00", "12:00");
        this.processEventAndAssertResults("06/19/2015 15:00:00", "02:00:00", DutyStatusEnum.ON, "03:00", "04:00", "10:00");
        this.processEventAndAssertResults("06/19/2015 17:00:00", "07:00:00", DutyStatusEnum.OFF, "03:00", "00:00", "10:00");

        // non-exempt day
        this.processEventAndAssertResults("06/20/2015 00:00:00", "06:00:00", DutyStatusEnum.OFF, "11:00", "14:00", "10:00");
        this.processEventAndAssertResults("06/20/2015 06:00:00", "13:00:00", DutyStatusEnum.ON, "11:00", "01:00", "00:00");
        this.processEventAndAssertResults("06/20/2015 19:00:00", "05:00:00", DutyStatusEnum.OFF, "11:00", "00:00", "00:00");
    }

    @Test
    public void test_14HoursDriving3Of7Days() throws ParseException {
        LogProperties logProperties = new LogProperties();
        logProperties.setLogDate(DateUtility.getDateFormat().parse("06/15/2015"));
        _rulesetEngine.PrepareStartOfLog(logProperties);

        this.processEventAndAssertResults("06/15/2015 00:00:00", "11:00:00", DutyStatusEnum.OFF, "11:00", "14:00", "70:00");
        this.processEventAndAssertResults("06/15/2015 11:00:00", "01:00:00", DutyStatusEnum.ON, "11:00", "13:00", "69:00");
        this.processEventAndAssertResults("06/15/2015 12:00:00", "04:00:00", DutyStatusEnum.DRV, "07:00", "09:00", "65:00");
        this.processEventAndAssertResults("06/15/2015 16:00:00", "08:00:00", DutyStatusEnum.OFF, "07:00", "01:00", "65:00");

        this.processEventAndAssertResults("06/16/2015 00:00:00", "02:00:00", DutyStatusEnum.OFF, "11:00", "14:00", "65:00");
        this.processEventAndAssertResults("06/16/2015 02:00:00", "07:00:00", DutyStatusEnum.ON, "11:00", "07:00", "58:00");
        this.processEventAndAssertResults("06/16/2015 07:00:00", "01:00:00", DutyStatusEnum.OFF, "11:00", "06:00", "58:00");
        this.processEventAndAssertResults("06/16/2015 08:00:00", "08:00:00", DutyStatusEnum.ON, "11:00", "00:00", "50:00");
        this.processEventAndAssertResults("06/16/2015 16:00:00", "01:00:00", DutyStatusEnum.DRV, "10:00", "00:00", "49:00");
        this.processEventAndAssertResults("06/16/2015 17:00:00", "07:00:00", DutyStatusEnum.OFF, "10:00", "00:00", "49:00");

        this.processEventAndAssertResults("06/17/2015 00:00:00", "04:00:00", DutyStatusEnum.OFF, "11:00", "14:00", "49:00");
        this.processEventAndAssertResults("06/17/2015 04:00:00", "01:00:00", DutyStatusEnum.ON, "11:00", "13:00", "48:00");
        this.processEventAndAssertResults("06/17/2015 05:00:00", "10:00:00", DutyStatusEnum.DRV, "01:00", "03:00", "38:00");
        this.processEventAndAssertResults("06/17/2015 15:00:00", "04:00:00", DutyStatusEnum.ON, "01:00", "00:00", "34:00");
        this.processEventAndAssertResults("06/17/2015 19:00:00", "01:00:00", DutyStatusEnum.DRV, "00:00", "00:00", "33:00");
        this.processEventAndAssertResults("06/17/2015 20:00:00", "04:00:00", DutyStatusEnum.OFF, "00:00", "00:00", "33:00");

        this.processEventAndAssertResults("06/18/2015 00:00:00", "11:00:00", DutyStatusEnum.OFF, "11:00", "14:00", "33:00");
        this.processEventAndAssertResults("06/18/2015 11:00:00", "01:00:00", DutyStatusEnum.ON, "11:00", "13:00", "32:00");
        this.processEventAndAssertResults("06/18/2015 12:00:00", "12:00:00", DutyStatusEnum.OFF, "11:00", "14:00", "32:00");

        this.processEventAndAssertResults("06/19/2015 00:00:00", "11:00:00", DutyStatusEnum.OFF, "11:00", "14:00", "32:00");
        this.processEventAndAssertResults("06/19/2015 11:00:00", "01:00:00", DutyStatusEnum.ON, "11:00", "13:00", "31:00");
        this.processEventAndAssertResults("06/19/2015 12:00:00", "12:00:00", DutyStatusEnum.OFF, "11:00", "14:00", "31:00");

        this.processEventAndAssertResults("06/20/2015 00:00:00", "06:00:00", DutyStatusEnum.OFF, "11:00", "14:00", "31:00");
        this.processEventAndAssertResults("06/20/2015 06:00:00", "01:00:00", DutyStatusEnum.DRV, "10:00", "13:00", "30:00");
        this.processEventAndAssertResults("06/20/2015 07:00:00", "14:00:00", DutyStatusEnum.ON, "10:00", "00:00", "16:00");
        this.processEventAndAssertResults("06/20/2015 21:00:00", "03:00:00", DutyStatusEnum.OFF, "10:00", "00:00", "16:00");
    }
}
