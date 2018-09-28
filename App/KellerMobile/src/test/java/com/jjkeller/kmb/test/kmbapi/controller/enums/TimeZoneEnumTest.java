package com.jjkeller.kmb.test.kmbapi.controller.enums;


import com.jjkeller.kmb.test.KMBRoboElectricTestRunner;
import com.jjkeller.kmb.test.KmbRoboTestBase;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.enums.TimeZoneEnum;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

@RunWith(KMBRoboElectricTestRunner.class)
public class TimeZoneEnumTest extends KmbRoboTestBase {

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }


    @Test
    public void testTimeZoneEnum() {
        TimeZone timeZone = TimeZone.getTimeZone("US/Central");;
        TimeZoneEnum timeZoneEnum = new TimeZoneEnum(TimeZoneEnum.CENTRALSTANDARDTIME); // just an initial value
        timeZoneEnum.fromTimeZone(timeZone);
        Assert.assertEquals(TimeZoneEnum.CENTRALSTANDARDTIME, timeZoneEnum.getValue());
    }

    @Test
    public void testTimeZoneEnum_Eastern() {
        TimeZone timeZone = TimeZone.getTimeZone("US/Eastern");;
        TimeZoneEnum timeZoneEnum = new TimeZoneEnum(TimeZoneEnum.CENTRALSTANDARDTIME); // just an initial value
        timeZoneEnum.fromTimeZone(timeZone);
        Assert.assertEquals(TimeZoneEnum.EASTERNSTANDARDTIME, timeZoneEnum.getValue());
    }

    @Test
    public void testTimeZoneEnum_Default() {
        TimeZone timeZone = TimeZone.getTimeZone("UTC");;
        TimeZoneEnum timeZoneEnum = new TimeZoneEnum(TimeZoneEnum.CENTRALSTANDARDTIME); // just an initial value
        timeZoneEnum.fromTimeZone(timeZone);
        Assert.assertEquals(TimeZoneEnum.NULL, timeZoneEnum.getValue());
    }
}