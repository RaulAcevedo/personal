package com.jjkeller.kmbapi.common;

import com.jjkeller.kmbapi.TestableTimeKeeper;
import com.jjkeller.kmbapi.configuration.GlobalState;

import junit.framework.Assert;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Date;
import java.util.TimeZone;



/**
 * Created by jld5296 on 2/8/17.
 */
@RunWith(RobolectricTestRunner.class)
@Config(application = GlobalState.class,
        manifest = "/build/intermediates/bundles/encompass/debug/AndroidManifest.xml"
)
public class TimeKeeperTest extends TestBase {



    /**
     * DeviceTime = 1:1:1.111
     * ServerTime = 1:1:12.555
     * =======================
     * Offset = -11.444 seconds
     * To catch the current time up to server
     * Subtract the offset to the deviceTime
     *
     * 1:1:1.111 - -11.444 seconds = 1:1:12.555
     */
    @Test
    public void test_syncronizeWithServerTime_whereServerIsAhead_producesNegativeOffsetValue() throws Exception {
        final DateTimeZone tz = DateTimeZone.forID("America/Los_Angeles");
        final long offsetFromServer = 11444;

        DateTime now = new DateTime(2017, 1, 1, 1, 1, 1, 111, tz);
        DateTime expected = new DateTime(2017, 1, 1, 1, 1, 12, 555, tz);

        ITimeKeeper keeper = new TestableTimeKeeper(now);
        System.out.println("DeviceTime: " + keeper.getDeviceTime());

        DateTime serverTime = now.withDurationAdded(offsetFromServer, 1);
        System.out.println("ServerTime: " + serverTime);

        keeper.synchronizeWithServerTime(serverTime);
        System.out.println("Offset: " + keeper.getCurrentOffsetFromServerTime());

        System.out.println("=======================");
        System.out.println("Expected: " + expected);
        System.out.println("Actual: " +  keeper.getCurrentDateTime());

        Assert.assertEquals(expected, keeper.getCurrentDateTime());
        Assert.assertEquals(expected.toDate(), keeper.now());

        Assert.assertEquals((-1) * offsetFromServer, keeper.getCurrentOffsetFromServerTime());

        Assert.assertTrue("Offset should be negative", keeper.getCurrentOffsetFromServerTime() < 0);

    }

    /**
     * DeviceTime = 1:1:1.111
     * ServerTime = 1:1:0.000
     * =======================
     * Offset = 1.111 seconds
     * To catch the current time up to server
     * Subtract the offset to the deviceTime
     *
     * 1:1:1.111 - 1.111 seconds = 1:1:0.000
     */
    @Test
    public void test_syncronizeWithServerTime_whereServerIsBehind_producesPositiveOffsetValue() throws Exception {
        final DateTimeZone tz = DateTimeZone.forID("America/Los_Angeles");
        final long offsetFromServer = -1111;

        DateTime now = new DateTime(2017, 1, 1, 1, 1, 1, 111, tz);
        DateTime expected = new DateTime(2017, 1, 1, 1, 1, 0, 0, tz);

        ITimeKeeper keeper = new TestableTimeKeeper(now);
        System.out.println("DeviceTime: " + keeper.getDeviceTime());

        DateTime serverTime = now.withDurationAdded(offsetFromServer, 1);
        System.out.println("ServerTime: " + serverTime);

        keeper.synchronizeWithServerTime(serverTime);
        System.out.println("Offset: " + keeper.getCurrentOffsetFromServerTime());

        System.out.println("=======================");
        System.out.println("Expected: " + expected);
        System.out.println("Actual: " +  keeper.getCurrentDateTime());

        Assert.assertEquals(expected, keeper.getCurrentDateTime());
        Assert.assertEquals(expected.toDate(), keeper.now());

        Assert.assertEquals((-1) * offsetFromServer, keeper.getCurrentOffsetFromServerTime());

        Assert.assertTrue("Offset should be positive", keeper.getCurrentOffsetFromServerTime() > 0);
    }

    @Test
    public void test_now_givenOffsetServerAhead_nowReflectsServerTime() throws Exception {
        final DateTimeZone tz = DateTimeZone.forID("America/Los_Angeles");
        final long offsetFromServer = -1111;

        DateTime now = new DateTime(2017, 1, 1, 1, 1, 1, 111, tz);
        DateTime expected = new DateTime(2017, 1, 1, 1, 1, 0, 0, tz);

        ITimeKeeper keeper = new TestableTimeKeeper(now);
        System.out.println("DeviceTime: " + keeper.getDeviceTime());

        DateTime serverTime = now.withDurationAdded(offsetFromServer, 1);
        System.out.println("ServerTime: " + serverTime);

        keeper.synchronizeWithServerTime(serverTime);
        System.out.println("Offset: " + keeper.getCurrentOffsetFromServerTime());

        System.out.println("=======================");
        System.out.println("Expected: " + expected);
        System.out.println("Actual: " +  keeper.getCurrentDateTime());

        Assert.assertEquals(expected.toDate().getTime(), keeper.now().getTime());
    }

    @Test
    public void test_now_givenOffsetServerBehind_nowReflectsServerTime() throws Exception {
        final DateTimeZone tz = DateTimeZone.forID("America/Los_Angeles");
        final long offsetFromServer = 1111;

        DateTime now = new DateTime(2017, 1, 1, 1, 1, 1, 111, tz);
        DateTime expected = new DateTime(2017, 1, 1, 1, 1, 2, 222, tz);

        ITimeKeeper keeper = new TestableTimeKeeper(now);
        System.out.println("DeviceTime: " + keeper.getDeviceTime());

        DateTime serverTime = now.withDurationAdded(offsetFromServer, 1);
        System.out.println("ServerTime: " + serverTime);

        keeper.synchronizeWithServerTime(serverTime);
        System.out.println("Offset: " + keeper.getCurrentOffsetFromServerTime());

        System.out.println("=======================");
        System.out.println("Expected: " + expected);
        System.out.println("Actual: " +  keeper.getCurrentDateTime());

        Assert.assertEquals(expected.toDate().getTime(), keeper.now().getTime());
    }

    @Test
    public void test_getCurrentDateTime_givenOffsetServerAhead_nowReflectsServerTime() throws Exception {
        final DateTimeZone tz = DateTimeZone.forID("America/Los_Angeles");
        final long offsetFromServer = -1111;

        DateTime now = new DateTime(2017, 1, 1, 1, 1, 1, 111, tz);
        DateTime expected = new DateTime(2017, 1, 1, 1, 1, 0, 0, tz);

        ITimeKeeper keeper = new TestableTimeKeeper(now);
        System.out.println("DeviceTime: " + keeper.getDeviceTime());

        DateTime serverTime = now.withDurationAdded(offsetFromServer, 1);
        System.out.println("ServerTime: " + serverTime);

        keeper.synchronizeWithServerTime(serverTime);
        System.out.println("Offset: " + keeper.getCurrentOffsetFromServerTime());

        System.out.println("=======================");
        System.out.println("Expected: " + expected);
        System.out.println("Actual: " +  keeper.getCurrentDateTime());

        Assert.assertEquals(expected, keeper.getCurrentDateTime());
    }

    @Test
    public void test_getCurrentDateTime_givenOffsetServerBehind_nowReflectsServerTime() throws Exception {
        final DateTimeZone tz = DateTimeZone.forID("America/Los_Angeles");
        final long offsetFromServer = 1111;

        DateTime now = new DateTime(2017, 1, 1, 1, 1, 1, 111, tz);
        DateTime expected = new DateTime(2017, 1, 1, 1, 1, 2, 222, tz);

        ITimeKeeper keeper = new TestableTimeKeeper(now);
        System.out.println("DeviceTime: " + keeper.getDeviceTime());

        DateTime serverTime = now.withDurationAdded(offsetFromServer, 1);
        System.out.println("ServerTime: " + serverTime);

        keeper.synchronizeWithServerTime(serverTime);
        System.out.println("Offset: " + keeper.getCurrentOffsetFromServerTime());

        System.out.println("=======================");
        System.out.println("Expected: " + expected);
        System.out.println("Actual: " +  keeper.getCurrentDateTime());

        Assert.assertEquals(expected, keeper.getCurrentDateTime());
    }
}