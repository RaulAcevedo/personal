package com.jjkeller.kmbapi.geotab.tests.data;

import android.annotation.SuppressLint;

import com.jjkeller.kmbapi.common.Hex;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.featuretoggle.FeatureToggleService;
import com.jjkeller.kmbapi.geotabengine.GeotabData;
import com.jjkeller.kmbapi.common.TestBase;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by jld5296 on 9/30/16.
 */

@RunWith(RobolectricTestRunner.class)
@Config(application = GlobalState.class,
        manifest = "/build/intermediates/bundles/encompass/debug/AndroidManifest.xml"
)
public class GeotabDataTest extends TestBase {

    private DateFormat simpleDateFormat;
    private GlobalState app;

    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        app = (GlobalState) RuntimeEnvironment.application;

        FeatureToggleService ftService = mock(FeatureToggleService.class);
        when(ftService.getIsEldMandateEnabled()).thenReturn(false);
    }

    @SuppressLint("SimpleDateFormat")
    public GeotabDataTest(){
        simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    @Test
    public void fromByteArray() throws Exception {
        GeotabData data = GeotabData.FromByteArray(Hex.ConvertToHex("02 21 24   4F D4 C2 1B   F4 45 64 1A   49 B7 46 CB   28   D0 07   E8 03 00 00   0F   01 00 00 00   00 00 00 00   00 00 00 00   59 1B D9 20   F4 35   03"));
        GeotabData expected = new GeotabData(){{
            setDatetime((465753167L + DATE_TIME_BASE_OFFSET_SECONDS) * 1000);
            setOdometer(62.1f);
            setSpeedometer(24.85f);
            setLatitude(44.27792f);
            setLongitude(-88.45589f);
            setTachometer(500f);
            setStatus((byte)15);
        }};

        Assert.assertEquals(expected.getDatetime(), data.getDatetime());
        Assert.assertEquals(expected.getOdometer(), data.getOdometer(), 0.05);
        Assert.assertEquals(expected.getSpeedometer(), data.getSpeedometer(), 0.005);
        Assert.assertEquals(expected.getLatitude(), data.getLatitude(), 0.005);
        Assert.assertEquals(expected.getLongitude(), data.getLongitude(), 0.005);
        Assert.assertEquals(expected.getLongitude(), data.getLongitude(), 0.005);
        Assert.assertEquals(expected.getTachometer(), data.getTachometer(), 0.005);
        Assert.assertEquals(expected.getStatus(), data.getStatus());
    }

    @Test
    public void fromByteArraySpeedOver80UnsignedConversion()throws Exception {
        GeotabData data = GeotabData.FromByteArray(Hex.ConvertToHex("02 21 24   4F D4 C2 1B   F4 45 64 1A   49 B7 46 CB   80   D0 07   E8 03 00 00   0F   01 00 00 00   00 00 00 00   00 00 00 00   59 1B D9 20   F4 35   03"));
        GeotabData expected = new GeotabData(){{
            setDatetime((465753167L + DATE_TIME_BASE_OFFSET_SECONDS) * 1000);
            setOdometer(62.1f);
            setSpeedometer(79.53f);
            setLatitude(44.27792f);
            setLongitude(-88.45589f);
            setTachometer(500f);
            setStatus((byte)15);
        }};

        Assert.assertEquals(expected.getDatetime(), data.getDatetime());
        Assert.assertEquals(expected.getOdometer(), data.getOdometer(), 0.05);
        Assert.assertEquals(expected.getSpeedometer(), data.getSpeedometer(), 0.05);
        Assert.assertEquals(expected.getLatitude(), data.getLatitude(), 0.005);
        Assert.assertEquals(expected.getLongitude(), data.getLongitude(), 0.005);
        Assert.assertEquals(expected.getLongitude(), data.getLongitude(), 0.005);
        Assert.assertEquals(expected.getTachometer(), data.getTachometer(), 0.005);
        Assert.assertEquals(expected.getStatus(), data.getStatus());
    }

    @Test
    public void extractInt() throws Exception {
        long value = GeotabData.extractUnsignedInt(Hex.ConvertToHex("F4 45 64 1A"), 0);
        Assert.assertEquals(442779124, value);
    }

    @Test
    public void extractInt_Max() throws Exception {
        long value = GeotabData.extractUnsignedInt(Hex.ConvertToHex("FF FF FF FF"), 0);
        Assert.assertEquals(4294967295L, value);
    }

    @Test
    public void extractShort() throws Exception {
        int value = GeotabData.extractUnsignedShort(Hex.ConvertToHex("F4 45"), 0);
        Assert.assertEquals(17908, value);
    }

    @Test
    public void extractShort_Max() throws Exception {
        int value = GeotabData.extractUnsignedShort(Hex.ConvertToHex("FF FF"), 0);
        Assert.assertEquals(65535, value);
    }

    @Test
    public void populateInt() throws Exception {
        byte[] value = GeotabData.populateInt(442779124);
        byte[] expected = Hex.ConvertToHex("F4 45 64 1A");
        Assert.assertArrayEquals(expected, value);
    }

    @Test
    public void populateShort() throws Exception {
        byte[] value = GeotabData.populateShort((short)17908);
        Assert.assertArrayEquals(Hex.ConvertToHex("F4 45"), value);
    }


    @Test
    public void extractDate_whenGivenDatetimeAsBytes() throws Exception {
        Date date =  GeotabData.extractDate(Hex.ConvertToHex("4F D4 C2 1B"), 0);
        Date expected = simpleDateFormat.parse("10/04/2016 15:52:47");
        Assert.assertEquals(expected, date);
    }

    @Test
    public void extractDate_whenGivenDatetimeAsBytes_Max() throws Exception {
        Date date =  GeotabData.extractDate(Hex.ConvertToHex("FF FF FF FF"), 0);
        Assert.assertEquals(simpleDateFormat.parse("02/07/2138 06:28:15"), date);
    }

    @Test
    public void populateDate_whenGivenDatetime() throws Exception {
        byte[] bytes = GeotabData.populateDate(simpleDateFormat.parse("10/04/2016 15:52:47"));
        Assert.assertArrayEquals(Hex.ConvertToHex("4F D4 C2 1B"), bytes);
    }

    @Test
    public void populateDate_whenGivenDatetime_Min() throws Exception {
        byte[] bytes = GeotabData.populateDate(simpleDateFormat.parse("01/01/2002 00:00:00"));
        Assert.assertArrayEquals(Hex.ConvertToHex("00 00 00 00"), bytes);
    }

    @Test
    public void testSignedToUnsignedInt(){
        long unsignedInt = 2485439567L;
        int signedInt = (int)unsignedInt;
        long unsignedIntReturn = signedInt & 0xffffffffL;
        Assert.assertEquals(unsignedInt, unsignedIntReturn);

        // How Bitmasking Works
//        System.out.println(Long.toString(unsignedInt, 2));
//        System.out.println(Integer.toString(signedInt, 2));
//        System.out.println(Integer.toBinaryString(0xffffffff));
//        System.out.println(Long.toString(unsignedIntReturn, 2));
    }

    @Test
    public void testSignedToUnsignedShort(){
        int unsignedShort = 50000;
        short signedInt = (short)unsignedShort;
        int unsignedShortReturn = signedInt & 0xffff;
        Assert.assertEquals(unsignedShort, unsignedShortReturn);
    }

    @Test
    public void hexTest(){
        byte[] value = Hex.ConvertToHex("01 0F 18 B1");
        assertEquals(1, value[0]);
        assertEquals(15, value[1]);
        assertEquals(24, value[2]);
        assertEquals(177, value[3] & 0xFF);
    }
}