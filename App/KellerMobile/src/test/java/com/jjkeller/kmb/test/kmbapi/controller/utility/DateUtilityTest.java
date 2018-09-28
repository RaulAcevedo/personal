package com.jjkeller.kmb.test.kmbapi.controller.utility;


import com.jjkeller.kmb.test.KMBRoboElectricTestRunner;
import com.jjkeller.kmb.test.KmbRoboTestBase;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.enums.TimeZoneEnum;

import junit.framework.Assert;

import org.joda.time.LocalDateTime;
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
public class DateUtilityTest extends KmbRoboTestBase {

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }


    @Test
    public void testDaysBetween_simple_sameday() {
        Date dte1 = new Date(Date.parse("03/10/2011"));
        Date dte2 = new Date(Date.parse("03/10/2011"));
        int days = DateUtility.DaysBetween(dte1, dte2);
        Assert.assertTrue(days == 0);
    }

    @Test
    public void testDaysBetween_simple_1day() {
        Date dte1 = new Date(Date.parse("03/10/2011"));
        Date dte2 = new Date(Date.parse("03/11/2011"));
        int days = DateUtility.DaysBetween(dte1, dte2);
        Assert.assertTrue(days == 1);
    }

    @Test
    public void testDaysBetween_simple_1day_opposite() {
        Date dte1 = new Date(Date.parse("03/11/2011"));
        Date dte2 = new Date(Date.parse("03/10/2011"));
        int days = DateUtility.DaysBetween(dte1, dte2);
        Assert.assertTrue(days == 1);
    }

    @Test
    public void testDaysBetween_monthboundary_1day() {
        Date dte1 = new Date(Date.parse("10/31/2011"));
        Date dte2 = new Date(Date.parse("11/01/2011"));
        int days = DateUtility.DaysBetween(dte1, dte2);
        Assert.assertTrue(days == 1);
    }

    @Test
    public void testDaysBetween_twomonthboundary_31day() {
        Date dte1 = new Date(Date.parse("10/31/2011"));
        Date dte2 = new Date(Date.parse("12/01/2011"));
        int days = DateUtility.DaysBetween(dte1, dte2);
        Assert.assertTrue(days == 31);
    }

    @Test
    public void testDaysBetween_twomonthboundary_31day_opposite() {
        Date dte1 = new Date(Date.parse("12/01/2011"));
        Date dte2 = new Date(Date.parse("10/31/2011"));
        int days = DateUtility.DaysBetween(dte1, dte2);
        Assert.assertTrue(days == 31);
    }

    @Test
    public void testDaysBetween_december_5day() {
        Date dte1 = new Date(Date.parse("12/26/2011"));
        Date dte2 = new Date(Date.parse("12/31/2011"));
        int days = DateUtility.DaysBetween(dte1, dte2);
        Assert.assertTrue(days == 5);
    }

    @Test
    public void testDaysBetween_sameyear_365day() {
        Date dte1 = new Date(Date.parse("01/01/2011"));
        Date dte2 = new Date(Date.parse("12/31/2011"));
        int days = DateUtility.DaysBetween(dte1, dte2);
        Assert.assertTrue(days == 364);
    }

    @Test
    public void testDaysBetween_sameyear_366day() {
        Date dte1 = new Date(Date.parse("01/01/2012"));
        Date dte2 = new Date(Date.parse("12/31/2012"));
        int days = DateUtility.DaysBetween(dte1, dte2);
        Assert.assertTrue(days == 365);
    }

    @Test
    public void testDaysBetween_oneyeardiff_lessthanyear() {
        Date dte1 = new Date(Date.parse("12/31/2011"));
        Date dte2 = new Date(Date.parse("01/01/2012"));
        int days = DateUtility.DaysBetween(dte1, dte2);
        Assert.assertTrue(days == 1);
    }

    @Test
    public void testDaysBetween_oneyeardiff_lessthanyear_opposite() {
        Date dte1 = new Date(Date.parse("01/01/2012"));
        Date dte2 = new Date(Date.parse("12/31/2011"));
        int days = DateUtility.DaysBetween(dte1, dte2);
        Assert.assertTrue(days == 1);
    }

    @Test
    public void testDaysBetween_oneyeardiff_morethanyear() {
        Date dte1 = new Date(Date.parse("01/01/2011"));
        Date dte2 = new Date(Date.parse("12/31/2012"));
        int days = DateUtility.DaysBetween(dte1, dte2);
        Assert.assertTrue(days == 730);
    }

    @Test
    public void testDaysBetween_oneyeardiff_morethanyear_opposite() {
        Date dte1 = new Date(Date.parse("12/31/2012"));
        Date dte2 = new Date(Date.parse("01/01/2011"));
        int days = DateUtility.DaysBetween(dte1, dte2);
        Assert.assertTrue(days == 730);
    }

    @Test
    public void testDaysBetween_leapyear_2011() {
        Date dte1 = new Date(Date.parse("02/01/2011"));
        Date dte2 = new Date(Date.parse("03/01/2011"));
        int days = DateUtility.DaysBetween(dte1, dte2);
        Assert.assertTrue(days == 28);
    }

    @Test
    public void testDaysBetween_leapyear_2012() {
        Date dte1 = new Date(Date.parse("02/01/2012"));
        Date dte2 = new Date(Date.parse("03/01/2012"));
        int days = DateUtility.DaysBetween(dte1, dte2);
        Assert.assertTrue(days == 29);
    }

    @Test
    public void testDaysBetween_leapyear_2010() {
        Date dte1 = new Date(Date.parse("02/01/2010"));
        Date dte2 = new Date(Date.parse("03/01/2010"));
        int days = DateUtility.DaysBetween(dte1, dte2);
        Assert.assertTrue(days == 28);
    }

    @Test
    public void testDaysBetween_leapyear_2010_diffyear() {
        Date dte1 = new Date(Date.parse("02/01/2010"));
        Date dte2 = new Date(Date.parse("03/01/2011"));
        int days = DateUtility.DaysBetween(dte1, dte2);
        Assert.assertTrue(days == 393);
    }

    @Test
    public void testDaysBetween_leapyear_2011_diffyear() {
        Date dte1 = new Date(Date.parse("02/01/2011"));
        Date dte2 = new Date(Date.parse("03/01/2012"));
        int days = DateUtility.DaysBetween(dte1, dte2);
        Assert.assertTrue(days == 394);
    }

    @Test
    public void testDaysMorningBetween_1days_000_500() {
        Date dte1 = new Date(Date.parse("07/06/2013 00:00:00"));
        Date dte2 = new Date(Date.parse("07/07/2013 05:00:00"));
        boolean answer = this.DoesPeriodContainMornings(dte1, dte2);
        Assert.assertTrue(answer);
    }

    @Test
    public void testDaysMorningBetween_1days_030_500() {
        Date dte1 = new Date(Date.parse("07/06/2013 00:30:00"));
        Date dte2 = new Date(Date.parse("07/07/2013 05:00:00"));
        boolean answer = this.DoesPeriodContainMornings(dte1, dte2);
        Assert.assertTrue(answer);
    }

    @Test
    public void testDaysMorningBetween_1days_100_500() {
        Date dte1 = new Date(Date.parse("07/06/2013 01:00:00"));
        Date dte2 = new Date(Date.parse("07/07/2013 05:00:00"));
        boolean answer = this.DoesPeriodContainMornings(dte1, dte2);
        Assert.assertTrue(answer);
    }

    @Test
    public void testDaysMorningBetween_1days_200_500() {
        Date dte1 = new Date(Date.parse("07/06/2013 02:00:00"));
        Date dte2 = new Date(Date.parse("07/07/2013 05:00:00"));
        boolean answer = this.DoesPeriodContainMornings(dte1, dte2);
        Assert.assertFalse(answer);
    }

    @Test
    public void testDaysMorningBetween_2days_200_500() {
        Date dte1 = new Date(Date.parse("07/06/2013 02:00:00"));
        Date dte2 = new Date(Date.parse("07/08/2013 05:00:00"));
        boolean answer = this.DoesPeriodContainMornings(dte1, dte2);
        Assert.assertTrue(answer);
    }

    @Test
    public void testDaysMorningBetween_1days_100_400() {
        Date dte1 = new Date(Date.parse("07/06/2013 01:00:00"));
        Date dte2 = new Date(Date.parse("07/07/2013 04:00:00"));
        boolean answer = this.DoesPeriodContainMornings(dte1, dte2);
        Assert.assertFalse(answer);
    }

    @Test
    public void testDaysMorningBetween_2days_100_400() {
        Date dte1 = new Date(Date.parse("07/06/2013 01:00:00"));
        Date dte2 = new Date(Date.parse("07/08/2013 04:00:00"));
        boolean answer = this.DoesPeriodContainMornings(dte1, dte2);
        Assert.assertTrue(answer);
    }

    @Test
    public void testDaysMorningBetween_2days_100_500() {
        Date dte1 = new Date(Date.parse("07/06/2013 01:00:00"));
        Date dte2 = new Date(Date.parse("07/08/2013 05:00:00"));
        boolean answer = this.DoesPeriodContainMornings(dte1, dte2);
        Assert.assertTrue(answer);
    }

    @Test
    public void testDaysMorningBetween_3days_100_500() {
        Date dte1 = new Date(Date.parse("07/06/2013 01:00:00"));
        Date dte2 = new Date(Date.parse("07/09/2013 05:00:00"));
        boolean answer = this.DoesPeriodContainMornings(dte1, dte2);
        Assert.assertTrue(answer);
    }

    @Test
    public void testDaysMorningBetween_2days_1900_500() {
        Date dte1 = new Date(Date.parse("07/06/2013 19:00:00"));
        Date dte2 = new Date(Date.parse("07/08/2013 05:00:00"));
        boolean answer = this.DoesPeriodContainMornings(dte1, dte2);
        Assert.assertTrue(answer);
    }

    @Test
    public void testDaysMorningBetween_1day_1900_500() {
        Date dte1 = new Date(Date.parse("07/06/2013 19:00:00"));
        Date dte2 = new Date(Date.parse("07/07/2013 05:00:00"));
        boolean answer = this.DoesPeriodContainMornings(dte1, dte2);
        Assert.assertFalse(answer);
    }

    private boolean DoesPeriodContainMornings(Date start, Date end) {

        Calendar startCal = Calendar.getInstance();
        startCal.setTime(start);

        Calendar endCal = Calendar.getInstance();
        endCal.setTime(end);

        Calendar oneAMCal = Calendar.getInstance();
        oneAMCal.setTime(start);
        oneAMCal.set(Calendar.HOUR, 1);
        oneAMCal.set(Calendar.MINUTE, 0);
        oneAMCal.set(Calendar.SECOND, 0);
        oneAMCal.set(Calendar.AM_PM, 0);

        Calendar fiveAMCal = Calendar.getInstance();
        fiveAMCal.setTime(start);
        fiveAMCal.set(Calendar.HOUR, 5);
        fiveAMCal.set(Calendar.MINUTE, 0);
        fiveAMCal.set(Calendar.SECOND, 0);
        fiveAMCal.set(Calendar.AM_PM, 0);

        @SuppressWarnings("UnusedAssignment") Date oneAMDate = oneAMCal.getTime();
        @SuppressWarnings("UnusedAssignment") Date fiveAMDate = fiveAMCal.getTime();

        int countOf1AM = 0;
        int countOf5AM = 0;
        while (oneAMCal.compareTo(endCal) < 0) {
            if (oneAMCal.compareTo(startCal) >= 0 && oneAMCal.compareTo(endCal) <= 0)
                countOf1AM++;
            if (fiveAMCal.compareTo(startCal) >= 0 && fiveAMCal.compareTo(endCal) <= 0)
                countOf5AM++;

            oneAMCal.add(Calendar.DAY_OF_MONTH, 1);
            //noinspection UnusedAssignment
            oneAMDate = oneAMCal.getTime();
            fiveAMCal.add(Calendar.DAY_OF_MONTH, 1);
            //noinspection UnusedAssignment
            fiveAMDate = fiveAMCal.getTime();
        }

        return countOf1AM >= 2 && countOf5AM >= 2;
    }

    @Test
    public void testMillisecondsFromTimespanString() throws ParseException {
        Assert.assertEquals(60 * DateUtility.MILLISECONDS_PER_HOUR, DateUtility.getMillisecondsFromHHmmss("60:00:00"));
        Assert.assertEquals(40 * DateUtility.MILLISECONDS_PER_MINUTE, DateUtility.getMillisecondsFromHHmmss("00:40:00"));

        Assert.assertEquals(80 * DateUtility.MILLISECONDS_PER_HOUR, DateUtility.getMillisecondsFromHHmm("80:00"));
        Assert.assertEquals(35 * DateUtility.MILLISECONDS_PER_MINUTE, DateUtility.getMillisecondsFromHHmm("00:35"));

        Assert.assertEquals(60 * DateUtility.MILLISECONDS_PER_HOUR + 3000, DateUtility.getMillisecondsFromHHmmss("60:00:03"));
        Assert.assertEquals(60 * DateUtility.MILLISECONDS_PER_HOUR + 4 * DateUtility.MILLISECONDS_PER_MINUTE + 3000, DateUtility.getMillisecondsFromHHmmss("60:04:03"));
    }

    @Test
    public void testTimespanStringFromMilliseconds() {
        Assert.assertEquals("60:00:03", DateUtility.getHHmmssFromMilliseconds(60 * DateUtility.MILLISECONDS_PER_HOUR + 3000));
        Assert.assertEquals("60:00:00", DateUtility.getHHmmssFromMilliseconds(60 * DateUtility.MILLISECONDS_PER_HOUR));
        Assert.assertEquals("30:00:04", DateUtility.getHHmmssFromMilliseconds(30 * DateUtility.MILLISECONDS_PER_HOUR + 4000));
        Assert.assertEquals("21:03:07", DateUtility.getHHmmssFromMilliseconds(21 * DateUtility.MILLISECONDS_PER_HOUR + 3 * DateUtility.MILLISECONDS_PER_MINUTE + 7000));
    }

    @Test
    public void testConvertToTimeZone_Atlantic_To_Alaska_Raw() {
        TimeZone sourceTimeZone = TimeZoneEnum.ATLANTIC_STANDARD_TIME.toTimeZone();
        Date sourceDate = new LocalDateTime(1992, 8, 29, 14, 07, 13).toDate(sourceTimeZone);

        TimeZone targetTimeZone = TimeZoneEnum.ALASKAS_TANDARD_TIME.toTimeZone();

        Date targetDate = new LocalDateTime(sourceDate).toDate(targetTimeZone);

        TimeZone.setDefault(sourceTimeZone);
    }

    @Test
    public void testGetDateFromDateTime()
    {
        TimeZone sourceTimeZone = TimeZoneEnum.ATLANTIC_STANDARD_TIME.toTimeZone();
        Date sourceDate = new LocalDateTime(1992, 8, 29, 14, 07, 13).toDate(sourceTimeZone);

        TimeZone.setDefault(TimeZoneEnum.EASTERN_STANDARD_TIME.toTimeZone());

        Date targetDate = DateUtility.GetDateFromDateTime(sourceDate);

        final String pattern = "MM/dd/yyyy HH:mm:ss";

        // remove timezone designation so tests can be rin in Standard and Daylight savings time
        DateFormat df = new SimpleDateFormat(pattern);
        String result = df.format(targetDate);

        Assert.assertEquals("08/29/1992 00:00:00", result);
    }
}
