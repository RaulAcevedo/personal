package com.jjkeller.kmbapi.controller.utility;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jjkeller.kmbapi.enums.DatabusTypeEnum;
import com.jjkeller.kmbapi.proxydata.EobrConfiguration;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Created by jld5296 on 1/23/17.
 */
@RunWith(JUnit4.class)
public class EobrConfigurationSerializerTest {
    private Gson _gson;
    @Before
    public void setUp(){
        _gson = new GsonBuilder()
            .setDateFormat(
                    DateUtility.getHomeTerminalDMOSoapDateTimeFormat()
                            .toPattern())
            .registerTypeAdapter(EobrConfiguration.class,
                    new RESTWebServiceHelper.EobrConfigurationDeserializer())
            .registerTypeAdapter(EobrConfiguration.class,
                    new RESTWebServiceHelper.EobrConfigurationSerializer())
            .create();
    }

    @Test
    public void test_givenNullValueForClockSyncOffsetOrDate_itsNotIncludedInJsonOutput(){

        EobrConfiguration config = new EobrConfiguration();
        config.setDashboardOdometer(0f);
        config.setDatabusType(new DatabusTypeEnum(DatabusTypeEnum.J1850VPW));
        config.setDashboardOdometer(10000F);
        config.setDataCollectionRate(5);
        config.setEobrGeneration(7);
        config.setClockSyncDateUTC(null); // This should be missing from json elements.
        config.setClockSyncOffset(null); // This should be missing from json elements.
        config.setEobrOdometer(30000F);
        config.setSerialNumber("Big Truck");
        config.setFirmwareVersion("6.88");
        config.setOdometerCalibrationDate(new DateTime(2017, 01, 15, 4, 13, 45, 550).toDate());
        config.setLastPowerCycleResetDate(new DateTime(2017, 01, 15, 4, 13, 45, 550).toDate());

        String json = _gson.toJson(config);
        Assert.assertTrue(! json.contains("\"ClockSyncOffset\""));
        Assert.assertTrue(! json.contains("\"ClockSyncDateUTC\""));
    }

    @Test
    public void test_givenValueForClockSyncOffsetOrDate_includedInJsonOutput(){

        EobrConfiguration config = new EobrConfiguration();
        config.setDashboardOdometer(0f);
        config.setDatabusType(new DatabusTypeEnum(DatabusTypeEnum.J1850VPW));
        config.setDashboardOdometer(10000F);
        config.setDataCollectionRate(5);
        config.setEobrGeneration(7);
        config.setClockSyncDateUTC(new DateTime(2017, 01, 15, 4, 13, 45, 550)); // This should be missing from json elements.
        config.setClockSyncOffset(100l); // This should be missing from json elements.
        config.setEobrOdometer(30000F);
        config.setSerialNumber("Big Truck");
        config.setFirmwareVersion("6.88");
        config.setOdometerCalibrationDate(new DateTime(2017, 01, 15, 4, 13, 45, 550).toDate());
        config.setLastPowerCycleResetDate(new DateTime(2017, 01, 15, 4, 13, 45, 550).toDate());

        String json = _gson.toJson(config);
        Assert.assertEquals("{\"DashboardOdometer\":10000.0,\"DatabusType\":1,\"DataCollectionRate\":5,\"DiscoveryPasskey\":\"\",\"EobrOdometer\":30000.0,\"FirmwareVersion\":\"6.88\",\"HardBrakeThreshold\":7.0,\"Generation\":7,\"OdometerCalibrationDate\":\"/Date(1484475225550)/\",\"SerialNumber\":\"Big Truck\",\"SleepModeMinutes\":-1,\"SpeedometerThreshold\":65.0,\"TachometerThreshold\":1800,\"ClockSyncOffset\":100,\"ClockSyncDateUTC\":\"/Date(1484475225550)/\"}"
                , json);
    }
}
