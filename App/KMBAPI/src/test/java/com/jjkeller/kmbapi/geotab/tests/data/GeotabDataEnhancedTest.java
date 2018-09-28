package com.jjkeller.kmbapi.geotab.tests.data;

import com.jjkeller.kmbapi.common.Hex;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.featuretoggle.FeatureToggleService;
import com.jjkeller.kmbapi.geotabengine.GeotabDataEnhanced;
import com.jjkeller.kmbapi.geotabengine.GeotabUsbService;
import com.jjkeller.kmbapi.geotabengine.ThirdParty;
import com.jjkeller.kmbapi.common.TestBase;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by jld5296 on 10/4/16.
 */
@RunWith(RobolectricTestRunner.class)
@Config(application = GlobalState.class,
        manifest = "/build/intermediates/bundles/encompass/debug/AndroidManifest.xml"
)
public class GeotabDataEnhancedTest extends TestBase {

    private GlobalState app;

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        app = (GlobalState) RuntimeEnvironment.application;

        FeatureToggleService ftService = mock(FeatureToggleService.class);
        when(ftService.getIsEldMandateEnabled()).thenReturn(false);
    }

    @Test
    public void GeoTabDataEnhanced_constructorTest(){
        byte[] abData = Hex.ConvertToHex("02 21 24   4F D4 C2 1B   F4 45 64 1A   DD B3 46 CB   28   D0 07   E8 03 00 00   0F   11 11 00 00   11 11 00 00   11 11 00 00   10 1A D9 20   F3 35   03");
        GeotabDataEnhanced data = new GeotabDataEnhanced(abData);

        GeotabUsbService service = mock(GeotabUsbService.class);

        ThirdParty tp = new ThirdParty(service);
        tp.ExtractEnhancedHOSData(abData);
        GeotabDataEnhanced expected1 = tp.getGeotabData();


        Assert.assertEquals(expected1.getDatetime(), data.getDatetime());
        Assert.assertEquals(expected1.getOdometer(), data.getOdometer(), 0.00001);
        Assert.assertEquals(expected1.getSpeedometer(), data.getSpeedometer(), 0.00001);
        Assert.assertEquals(expected1.getLatitude(), data.getLatitude(), 0.000000001);
        Assert.assertEquals(expected1.getLongitude(), data.getLongitude(), 0.000000001);
        Assert.assertEquals(expected1.getTachometer(), data.getTachometer(), 0.00001);
        Assert.assertEquals(expected1.getStatus(), data.getStatus());
        Assert.assertEquals(expected1.getTripOdometer(), data.getTripOdometer(), 0.00001);
        Assert.assertEquals(expected1.getTripEngineSeconds(), data.getTripEngineSeconds(), 0.00001);
        Assert.assertEquals(expected1.getEngineHours(), data.getEngineHours(), 0.00001);
        Assert.assertEquals(expected1.getVehicleId(), data.getVehicleId());

        boolean areEqual = expected1.equals(data);
        Assert.assertTrue("Expected and Actual don't equate", areEqual);

        GeotabDataEnhanced expected2 = new GeotabDataEnhanced(){{
            setDatetime(1475596367000L);
            setLatitude(44.277912f);
            setLongitude(-88.455894f);
            setOdometer(62.1f);
            setSpeedometer(24.854849f);
            setTachometer(500.0f);
            setStatus((byte)15);
            setEngineHours(436.9f);
            setTripOdometer(436.9f);
            setTripEngineSeconds(436.9f);
            setVehicleId("G7F320D91A10");
        }};

        boolean areEqual2 = expected2.equals(data);
        Assert.assertTrue("Expected and Actual don't equate", areEqual2);
    }

    @Test
    public void GeoTabDataEnhanced_constructorTestExtraData(){
        //note the extra 4 bytes of data here (separated with extra spaces)
        //we need to make sure it doesn't prevent us from parsing the rest
        byte[] abData = Hex.ConvertToHex("02 21 28 08 35 59 1C 8A 45 64 1A 78 B4 46 CB 00 00 00 15 00 00 00 3F 14 00 00 00 01 00 00 00 00 00 00 00 69 1B D9 20   00 00 00 00   6D C8 03");
        GeotabDataEnhanced data = new GeotabDataEnhanced(abData);

        GeotabDataEnhanced expected1 = new GeotabDataEnhanced() {{
            setDatetime(1485451528000L);
            setLatitude(44.27790f);
            setLongitude(-88.45587f);
            setOdometer(1.3f);
            setSpeedometer(0f);
            setTachometer(0f);
            setStatus((byte)0x3F);
            setEngineHours(0.1f);
            setTripOdometer(1.2f);
            setTripEngineSeconds(0.0f);
            setVehicleId("G78D20D91B69");
        }};


        Assert.assertEquals(expected1.getDatetime(), data.getDatetime());
        Assert.assertEquals(expected1.getOdometer(), data.getOdometer(), 0.01);
        Assert.assertEquals(expected1.getSpeedometer(), data.getSpeedometer(), 0.01);
        Assert.assertEquals(expected1.getLatitude(), data.getLatitude(), 0.00001);
        Assert.assertEquals(expected1.getLongitude(), data.getLongitude(), 0.00001);
        Assert.assertEquals(expected1.getTachometer(), data.getTachometer(), 0.01);
        Assert.assertEquals(expected1.getStatus(), data.getStatus());
        Assert.assertEquals(expected1.getTripOdometer(), data.getTripOdometer(), 0.05);
        Assert.assertEquals(expected1.getTripEngineSeconds(), data.getTripEngineSeconds(), 0.001);
        Assert.assertEquals(expected1.getEngineHours(), data.getEngineHours(), 0.001);
        Assert.assertEquals(expected1.getVehicleId(), data.getVehicleId());
    }

    @Test
    public void GeotabDataEnhanced_statusBitsCorrect() {
        GeotabDataEnhanced data = new GeotabDataEnhanced();

        for(int status = 0; status <= 0xFF; status++) {
            data.setStatus((byte)status);
            assertStatus(data, (byte)status);
        }
    }

    private void assertStatus(GeotabDataEnhanced data, byte status) {
        Assert.assertEquals(String.format("Problem with status 0x%02X", status), data.isGpsLatched(), (status & (1 << 0)) != 0);
        Assert.assertEquals(String.format("Problem with status 0x%02X", status), data.isIgnitionOn(), (status & (1 << 1)) != 0);
        Assert.assertEquals(String.format("Problem with status 0x%02X", status), data.hasEngineData(), (status & (1 << 2)) != 0);
        Assert.assertEquals(String.format("Problem with status 0x%02X", status), data.isDateTimeValid(), (status & (1 << 3)) != 0);
        Assert.assertEquals(String.format("Problem with status 0x%02X", status), data.isSpeedFromEngine(), (status & (1 << 4)) != 0);
        Assert.assertEquals(String.format("Problem with status 0x%02X", status), data.isOdometerFromEngine(), (status & (1 << 5)) != 0);
    }

}