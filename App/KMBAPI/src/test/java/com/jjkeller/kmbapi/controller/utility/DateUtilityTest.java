package com.jjkeller.kmbapi.controller.utility;

import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.enums.TimeZoneEnum;
import com.jjkeller.kmbapi.featuretoggle.FeatureToggleService;
import com.jjkeller.kmbapi.featuretoggle.IFeatureToggleService;

import junit.framework.Assert;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for DateUtility
 *
 * Created by jld5296 on 10/24/16.
 */
@RunWith(JUnit4.class)
public class DateUtilityTest extends com.jjkeller.kmbapi.common.TestBase {

    DateTimeZone previousDateTimezone;
    TimeZone previousTimeZone;

    @Before
    public void setUp(){
        previousTimeZone = TimeZone.getDefault();
        previousDateTimezone = DateTimeZone.getDefault();

        TimeZone utcTz = TimeZone.getTimeZone("UTC");
        DateUtility.setHomeTerminalTimeDateFormatTimeZone(utcTz);
        DateTimeZone.setDefault(DateTimeZone.UTC);
    }

    @After
    public void tearDown(){
        DateUtility.setHomeTerminalTimeDateFormatTimeZone(previousTimeZone);
        DateTimeZone.setDefault(previousDateTimezone);
    }

    @Test
    public void getDateTimeFromString() throws Exception {
        // MM/dd/yyyy HH:mm:ss
        Date expected = new Date(1475338530000L); // 10/01/2016 16:15:30 GMT

        TimeZone utcTz = TimeZone.getTimeZone("UTC");
        DateUtility.setHomeTerminalTimeDateFormatTimeZone(utcTz);
        Date actual = DateUtility.getDateTimeFromString("10/01/2016 16:15:30");
        assertEquals(expected, actual);
    }

    @Test
    public void getEndOfLogDate() throws Exception {
        DateTimeZone tz = DateTimeZone.forTimeZone(TimeZoneEnum.PACIFIC_STANDARD_TIME.toTimeZone());

        // MM/dd/yyyy HH:mm:ss
        SimpleDateFormat fmt = DateUtility.getHomeTerminalDateTimeFormat24Hour();
        fmt.setTimeZone(tz.toTimeZone());

        DateTime input = new DateTime(fmt.parse("10/01/2016 06:01:15").getTime(), tz);
        Date expected = fmt.parse("10/01/2016 23:59:59");

        User user = mock(User.class);
        when(user.getHomeTerminalTimeZone()).thenReturn(TimeZoneEnum.PACIFIC_STANDARD_TIME);

        DateTime actual = DateUtility.getEndOfLogDate(input);

        assertEquals(fmt.format(expected), fmt.format(actual.toDate()));
    }

    @Test
    public void getEndOfLogDate_onFallDaylightSavingsTransition() throws Exception {
        DateTimeZone tz = DateTimeZone.forTimeZone(TimeZoneEnum.PACIFIC_STANDARD_TIME.toTimeZone());

        // MM/dd/yyyy HH:mm:ss
        SimpleDateFormat fmt = DateUtility.getHomeTerminalDateTimeFormat24Hour();
        fmt.setTimeZone(tz.toTimeZone());

        DateTime input = new DateTime(fmt.parse("10/06/2016 06:01:15").getTime(), tz);
        Date expected = fmt.parse("10/06/2016 23:59:59");

        User user = mock(User.class);
        when(user.getHomeTerminalTimeZone()).thenReturn(TimeZoneEnum.PACIFIC_STANDARD_TIME);

        DateTime actual = DateUtility.getEndOfLogDate(input);

        assertEquals(fmt.format(expected), fmt.format(actual.toDate()));
    }
    @Test
    public void getEndOfLogDate_onSpringDaylightSavingsTransition() throws Exception {
        DateTimeZone tz = DateTimeZone.forTimeZone(TimeZoneEnum.PACIFIC_STANDARD_TIME.toTimeZone());
        // MM/dd/yyyy HH:mm:ss
        SimpleDateFormat fmt = DateUtility.getHomeTerminalDateTimeFormat24Hour();
        fmt.setTimeZone(tz.toTimeZone());

        DateTime  input = new DateTime( fmt.parse("03/12/2017 06:01:15"), tz);
        Date expected = fmt.parse("03/12/2017 23:59:59");

        User user = mock(User.class);
        when(user.getHomeTerminalTimeZone()).thenReturn(TimeZoneEnum.PACIFIC_STANDARD_TIME);

        DateTime actual = DateUtility.getEndOfLogDate(input);

        assertEquals(fmt.format(expected), fmt.format(actual.toDate()));
    }

    @Test
    public void getDateFromString() throws Exception {
        // MM/dd/yyyy HH:mm:ss
        Date expected = new Date(1475280000000L); // 10/01/2016 16:15:30 GMT

        TimeZone utcTz = TimeZone.getTimeZone("UTC");
        DateUtility.setHomeTerminalTimeDateFormatTimeZone(utcTz);

        Date actual = DateUtility.getDateFromString("10/01/2016 16:15:30");
        assertEquals(expected, actual);
    }

    @Test
    public void currentHomeTerminalTime() throws Exception {
        // I'm not sure why we have this function it does exactly the same thing as getCurrentDateTimeUTC()
        // Java Date has no concept of TimeZone that's what the DateTimeFormat object does.
        // In fact the Date class bases it's values from milliseconds elapsed since Unix Epoch Time. (1/1/1970 00:00:00)

        Now(1477339650000L); // 10/22/2016 15:07:30

        User user = mock(User.class);
        when(user.getHomeTerminalTimeZone()).thenReturn(TimeZoneEnum.PACIFIC_STANDARD_TIME);

        Date actual = DateUtility.CurrentHomeTerminalTime(user);
        assertEquals(new Date(1477339650000L), actual);
    }


    @Test
    public void convertMillisecondsToHours() throws Exception {
        long input = 123456899999L;

        int hours = (int)DateUtility.ConvertMillisecondsToHours(input);

        Assert.assertEquals(34293, hours);
    }

    @Test
    public void convertMillisecondsToMinutes() throws Exception {
        long input = 123456899999L;

        int minutes = (int)DateUtility.ConvertMillisecondsToMinutes(input);

        Assert.assertEquals(2057614, minutes);
    }

    @Test
    public void convertMillisecondsToSeconds() throws Exception {
        long input = 123456899999L;

        int seconds = (int)DateUtility.ConvertMillisecondsToSeconds(input);

        Assert.assertEquals(123456899, seconds);
    }


    @Test
    public void getHomeTerminalDateFormat() throws Exception {
        SimpleDateFormat fmt = DateUtility.getHomeTerminalDateFormat();
        assertEquals("MM/dd/yyyy", fmt.toPattern());
    }

    @Test
    public void getHomeTerminalShortDateFormat() throws Exception {
        SimpleDateFormat fmt = DateUtility.getHomeTerminalShortDateFormat();
        assertEquals("MM/dd/yy", fmt.toPattern());
    }

    @Test
    public void getHomeTerminalDateTimeFormat() throws Exception {
        SimpleDateFormat fmt = DateUtility.getHomeTerminalDateTimeFormat();
        assertEquals("MM/dd/yyyy HH:mm:ss", fmt.toPattern());
    }

    @Test
    public void getHomeTerminalDateTimeFormat12Hour() throws Exception {
        SimpleDateFormat fmt = DateUtility.getHomeTerminalDateTimeFormat12Hour();
        assertEquals("MM/dd/yyyy hh:mm aa", fmt.toPattern());
    }

    @Test
    public void getHomeTerminalDateTimeFormat24Hour() throws Exception {
        SimpleDateFormat fmt = DateUtility.getHomeTerminalDateTimeFormat24Hour();
        assertEquals("MM/dd/yyyy HH:mm:ss", fmt.toPattern());
    }

    @Test
    public void getHomeTerminalTime12HourFormat() throws Exception {
        SimpleDateFormat fmt = DateUtility.getHomeTerminalTime12HourFormat();
        assertEquals("hh:mm aa", fmt.toPattern());
    }

    @Test
    public void getHomeTerminalTime24HourFormat() throws Exception {
        SimpleDateFormat fmt = DateUtility.getHomeTerminalTime24HourFormat();
        assertEquals("HH:mm:ss", fmt.toPattern());
    }

    @Test
    public void getHomeTerminalReferenceTimestampFormat() throws Exception {
        SimpleDateFormat fmt = DateUtility.getHomeTerminalReferenceTimestampFormat();
        assertEquals("MM/dd/yy hh:mm:ss a", fmt.toPattern());
    }

    @Test
    public void getHomeTerminalDMOSoapDateTimeFormat() throws Exception {
        SimpleDateFormat fmt = DateUtility.getHomeTerminalDMOSoapDateTimeFormat();
        assertEquals("M/d/yy hh:mm:ss a", fmt.toPattern());
    }

    @Test
    public void getHomeTerminalDMOSoapDateTimestampUTCFormat() throws Exception {
        SimpleDateFormat fmt = DateUtility.getHomeTerminalDMOSoapDateTimestampUTCFormat();
        assertEquals("yyyy-MM-dd'T'HH:mm:ss'Z'", fmt.toPattern());
    }

    @Test
    public void getHomeTerminalDMOWebApiDateTimeFormatUtcOffset() throws Exception {
        SimpleDateFormat fmt = DateUtility.getHomeTerminalDMOWebApiDateTimeFormatUtcOffset();
        assertEquals("MM/dd/yyyy hh:mm:ss a Z", fmt.toPattern());
    }

    @Test
    public void getHomeTerminalSqlDateFormat() throws Exception {
        SimpleDateFormat fmt = DateUtility.getHomeTerminalReferenceTimestampFormat();
        assertEquals("MM/dd/yy hh:mm:ss a", fmt.toPattern());
    }

    @Test
    public void getHomeTerminalSqlDateTimeFormat() throws Exception {
        SimpleDateFormat fmt = DateUtility.getHomeTerminalSqlDateTimeFormat();
        assertEquals("yyyy-MM-dd HH:mm:ss", fmt.toPattern());
    }

    @Test
    public void getDateFormat() throws Exception {
        SimpleDateFormat fmt = DateUtility.getDateFormat();
        assertEquals("MM/dd/yyyy", fmt.toPattern());
    }

    @Test
    public void getDateNoSeparatorsFormat() throws Exception {
        SimpleDateFormat fmt = DateUtility.getDateNoSeparatorsFormat();
        assertEquals("MMddyyyy", fmt.toPattern());
    }

    @Test
    public void getTime24HourFormat() throws Exception {
        SimpleDateFormat fmt = DateUtility.getTime24HourFormat();
        assertEquals("HH:mm:ss", fmt.toPattern());
    }

    @Test
    public void getDMOSoapDateTimeFormat() throws Exception {
        SimpleDateFormat fmt = DateUtility.getDMOSoapDateTimeFormat();
        assertEquals("M/d/yy hh:mm:ss a", fmt.toPattern());
    }

    @Test
    public void getDMOSoapDateTimestampFormat() throws Exception {
        SimpleDateFormat fmt = DateUtility.getDMOSoapDateTimestampFormat();
        assertEquals("yyyy-MM-dd'T'HH:mm:ss", fmt.toPattern());
    }

    @Test
    public void getDMOSoapDateTimestampUTCFormat() throws Exception {
        SimpleDateFormat fmt = DateUtility.getDMOSoapDateTimestampUTCFormat();
        assertEquals("yyyy-MM-dd'T'HH:mm:ss'Z'", fmt.toPattern());
    }

    @Test
    public void getHomeTerminalFullMonthDateFormat() throws Exception {
        SimpleDateFormat fmt = DateUtility.getHomeTerminalFullMonthDateFormat();
        assertEquals("MMMM dd, yyyy", fmt.toPattern());
    }

    @Test
    public void getCurrentDateTimeUTC() throws Exception {
        EnableEldMandate(true);

        Now(1477339650000L); // 10/22/2016 15:07:30
        DateUtility.setHomeTerminalTimeDateFormatTimeZone(TimeZone.getTimeZone("US/Pacific"));

        Date actual = DateUtility.getCurrentDateTimeUTC();
        assertEquals(new Date(1477339650000L), actual);
    }

    @Test
    public void getSixMonthDateTimeUTC() throws Exception {
        EnableEldMandate(true);
        Now(1477339650000L); // 10/22/2016 15:07:30

        Date actual = DateUtility.getSixMonthDateTimeUTC();
        assertEquals(new Date(1461528450000L), actual);
    }

    @Test
    public void testProveTharCalendarWithoutTimeZoneSet_isDangerous(){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("US/Pacific"));
        calendar.set(2016, Calendar.OCTOBER, 20, 13, 30, 15);
        Date inputDate = calendar.getTime();
        calendar.set(2016, Calendar.OCTOBER, 20, 23, 59, 59);
        Date expected = calendar.getTime();

        calendar = Calendar.getInstance();
        calendar.setTime(inputDate);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        Date actual = calendar.getTime();

        Assert.assertNotSame(expected, actual);
    }

    @Test
    public void createHomeTerminalTimeString_handlesNull(){
        Assert.assertEquals("", DateUtility.createHomeTerminalTimeString(null, true));
        Assert.assertEquals("", DateUtility.createHomeTerminalTimeString(null, false));
    }

    @Test
    public void createHomeTerminalTimeString_NonMandate_ShowHoursMinutes(){

        DateTime zeroDateTime = new DateTime(2000, 1, 1,   0, 0, 0);
        DateTime nonZeroDateTime = new DateTime(2017, 3, 20,   15, 33, 54);
        DateTime zeroDate = new DateTime(2000, 1, 1,   15, 32, 54);
        DateTime noon = new DateTime(2017, 3, 20,   12, 0, 0);

        Assert.assertEquals("12:00 AM", DateUtility.createHomeTerminalTimeString(zeroDateTime.toDate(), false));
        Assert.assertEquals("03:33 PM", DateUtility.createHomeTerminalTimeString(nonZeroDateTime.toDate(), false));
        Assert.assertEquals("03:32 PM", DateUtility.createHomeTerminalTimeString(zeroDate.toDate(), false));
        Assert.assertEquals("12:00 PM", DateUtility.createHomeTerminalTimeString(noon.toDate(), false));
    }

    @Test
    public void createHomeTerminalTimeString_Mandate_ShowHoursMinutesSeconds(){
        DateTime zeroDateTime = new DateTime(2000, 1, 1,   0, 0, 0);
        DateTime nonZeroDateTime = new DateTime(2017, 3, 20,   15, 33, 54);
        DateTime zeroDate = new DateTime(2000, 1, 1,   15, 32, 28);
        DateTime noon = new DateTime(2017, 3, 20,   12, 0, 0);

        Assert.assertEquals("12:00:00 AM", DateUtility.createHomeTerminalTimeString(zeroDateTime.toDate(), true));
        Assert.assertEquals("03:33:54 PM", DateUtility.createHomeTerminalTimeString(nonZeroDateTime.toDate(), true));
        Assert.assertEquals("03:32:28 PM", DateUtility.createHomeTerminalTimeString(zeroDate.toDate(), true));
        Assert.assertEquals("12:00:00 PM", DateUtility.createHomeTerminalTimeString(noon.toDate(), true));
    }

    @Test
    public void createTimeDurationString_NonMandate_ShowHoursMinutes(){
        Long zeroDuration = 0L;
        Long secondsDuration = 28000L; //28 Seconds
        Long minutesDuration = 900000L; //15 Minutes
        Long hoursDuration = 72000000L; //20 hours
        Long miscDuration = 52147852L; //14:29:07.852

        Assert.assertEquals("0:00", DateUtility.createTimeDurationString(zeroDuration, false, false));
        Assert.assertEquals("0:00", DateUtility.createTimeDurationString(secondsDuration, false, false));
        Assert.assertEquals("0:15", DateUtility.createTimeDurationString(minutesDuration, false, false));
        Assert.assertEquals("20:00", DateUtility.createTimeDurationString(hoursDuration, false, false));
        Assert.assertEquals("14:29", DateUtility.createTimeDurationString(miscDuration, false, false));
    }

    @Test
    public void createTimeDurationString_Mandate_ShowHoursMinutesSeconds(){
        Long zeroDuration = 0L;
        Long secondsDuration = 28000L; //28 Seconds
        Long minutesDuration = 900000L; //15 Minutes
        Long hoursDuration = 72000000L; //20 hours
        Long miscDuration = 52147852L; //14:29:07.852

        Assert.assertEquals("0:00:00", DateUtility.createTimeDurationString(zeroDuration, true, false));
        Assert.assertEquals("0:00:28", DateUtility.createTimeDurationString(secondsDuration, true, false));
        Assert.assertEquals("0:15:00", DateUtility.createTimeDurationString(minutesDuration, true, false));
        Assert.assertEquals("20:00:00", DateUtility.createTimeDurationString(hoursDuration, true, false));
        Assert.assertEquals("14:29:07", DateUtility.createTimeDurationString(miscDuration, true, false));
    }

    @Test
    public void createTimeDurationString_NotMandate_ShouldRoundProperly() {
        Long hours0minutes20seconds10 = 1210000L;
        Long hours0minutes20milliseconds1 = 1200001L;
        Long hours9minutes59seconds1 = 35941000L;
        Long minute1Exact = 60000L;

        Assert.assertEquals("0:20", DateUtility.createTimeDurationString(hours0minutes20seconds10, false, false));
        Assert.assertEquals("0:21", DateUtility.createTimeDurationString(hours0minutes20seconds10, false, true));

        Assert.assertEquals("0:20", DateUtility.createTimeDurationString(hours0minutes20milliseconds1, false, false));
        Assert.assertEquals("0:20", DateUtility.createTimeDurationString(hours0minutes20milliseconds1, false, true));

        Assert.assertEquals("9:59", DateUtility.createTimeDurationString(hours9minutes59seconds1, false, false));
        Assert.assertEquals("10:00", DateUtility.createTimeDurationString(hours9minutes59seconds1, false, true));

        Assert.assertEquals("0:01", DateUtility.createTimeDurationString(minute1Exact, false, false));
        Assert.assertEquals("0:01", DateUtility.createTimeDurationString(minute1Exact, false, true));
    }


    @Test
    public void createTimeDurationString_Mandate_ShouldRoundProperly() {
        Long hours0minutes20seconds10milli50 = 1210050L;
        Long hours0minutes20milliseconds1 = 1200001L;
        Long hours9minutes59seconds1milli1 = 35999001L;
        Long second1Exact = 1000L;

        Assert.assertEquals("0:20:10", DateUtility.createTimeDurationString(hours0minutes20seconds10milli50, true, false));
        Assert.assertEquals("0:20:11", DateUtility.createTimeDurationString(hours0minutes20seconds10milli50, true, true));

        Assert.assertEquals("0:20:00", DateUtility.createTimeDurationString(hours0minutes20milliseconds1, true, false));
        Assert.assertEquals("0:20:01", DateUtility.createTimeDurationString(hours0minutes20milliseconds1, true, true));

        Assert.assertEquals("9:59:59", DateUtility.createTimeDurationString(hours9minutes59seconds1milli1, true, false));
        Assert.assertEquals("10:00:00", DateUtility.createTimeDurationString(hours9minutes59seconds1milli1, true, true));

        Assert.assertEquals("0:00:01", DateUtility.createTimeDurationString(second1Exact, true, false));
        Assert.assertEquals("0:00:01", DateUtility.createTimeDurationString(second1Exact, true, true));
    }

}