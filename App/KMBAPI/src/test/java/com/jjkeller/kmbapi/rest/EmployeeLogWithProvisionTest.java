package com.jjkeller.kmbapi.rest;

import com.google.gson.Gson;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.utility.RESTWebServiceHelper;
import com.jjkeller.kmbapi.proxydata.EmployeeLogWithProvisions;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Created by T000684 on 11/8/2017.
 */

@RunWith(RobolectricTestRunner.class)
@Config(application = GlobalState.class,
        manifest = "/build/intermediates/bundles/encompass/debug/AndroidManifest.xml"
)
public class EmployeeLogWithProvisionTest {

    @Test
    public void testGson(){
       String json = "[{\"EmployeeCode\":\"4410496C-9158-4D79-A950-7A7B1D178CD8\",\"LogDate\":\"11\\/6\\/17 12:00:00 AM\",\"StartTime\":\"11\\/6\\/17 11:13:57 AM\",\"EndTime\":\"11\\/6\\/17 11:15:44 AM\",\"StartLocation\":{\"Name\":\"4625.7 mi E of Ceiba, PR\",\"GpsInfo\":{\"TimestampUtc\":\"11\\/6\\/17 11:13:57 AM\",\"LatitudeDegrees\":0.00000000,\"LongitudeDegrees\":0.00000000,\"DecodedInfo\":{\"City\":\"4625.7 mi E of Ceiba\",\"State\":\"PR\",\"PostalCode\":\"\",\"Street\":\"\",\"County\":\"\",\"Country\":\"\"}},\"OdometerReading\":-1,\"EndOdometerReading\":-1},\"EndLocation\":{\"Name\":\"4625.7 mi E of Ceiba, PR\",\"GpsInfo\":{\"TimestampUtc\":\"11\\/6\\/17 11:15:44 AM\",\"LatitudeDegrees\":0.00000000,\"LongitudeDegrees\":0.00000000,\"DecodedInfo\":{\"City\":\"4625.7 mi E of Ceiba\",\"State\":\"PR\",\"PostalCode\":\"\",\"Street\":\"\",\"County\":\"\",\"Country\":\"\"}},\"OdometerReading\":-1,\"EndOdometerReading\":-1},\"TotalDistance\":2.0000,\"TractorNumber\":\"ygyh\",\"ProvisionTypeEnum\":1},{\"EmployeeCode\":\"4410496C-9158-4D79-A950-7A7B1D178CD8\",\"LogDate\":\"11\\/6\\/17 12:00:00 AM\",\"StartTime\":\"11\\/6\\/17 02:49:47 PM\",\"EndTime\":\"11\\/6\\/17 02:52:07 PM\",\"StartLocation\":{\"Name\":\"4625.7 mi E of Ceiba, PR\",\"GpsInfo\":{\"TimestampUtc\":\"11\\/6\\/17 02:49:47 PM\",\"LatitudeDegrees\":0.00000000,\"LongitudeDegrees\":0.00000000,\"DecodedInfo\":{\"City\":\"4625.7 mi E of Ceiba\",\"State\":\"PR\",\"PostalCode\":\"\",\"Street\":\"\",\"County\":\"\",\"Country\":\"\"}},\"OdometerReading\":-1,\"EndOdometerReading\":-1},\"EndLocation\":{\"Name\":\"4625.7 mi E of Ceiba, PR\",\"GpsInfo\":{\"TimestampUtc\":\"11\\/6\\/17 02:52:07 PM\",\"LatitudeDegrees\":0.00000000,\"LongitudeDegrees\":0.00000000,\"DecodedInfo\":{\"City\":\"4625.7 mi E of Ceiba\",\"State\":\"PR\",\"PostalCode\":\"\",\"Street\":\"\",\"County\":\"\",\"Country\":\"\"}},\"OdometerReading\":-1,\"EndOdometerReading\":-1},\"TotalDistance\":2.0000,\"TractorNumber\":\"ygyh\",\"ProvisionTypeEnum\":1}]\n";
       RESTWebServiceHelper helper = new RESTWebServiceHelper(GlobalState.getInstance());
       EmployeeLogWithProvisions provisions2[] = helper.getGson().fromJson(json, EmployeeLogWithProvisions[].class);
       Assert.assertEquals(2, provisions2.length);
    }
}
