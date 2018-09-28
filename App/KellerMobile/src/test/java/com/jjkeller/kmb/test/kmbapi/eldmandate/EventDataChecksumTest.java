package com.jjkeller.kmb.test.kmbapi.eldmandate;


import com.jjkeller.kmb.test.KMBRoboElectricTestRunner;
import com.jjkeller.kmb.test.KmbRoboTestBase;
import com.jjkeller.kmbapi.eldmandate.EventDataChecksumHelper;
import com.jjkeller.kmbapi.employeelogeldevents.Enums;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;


/**
 * Created by rab5795 on 4/21/2016.
 */
@RunWith(KMBRoboElectricTestRunner.class)
public class EventDataChecksumTest extends KmbRoboTestBase {

    private EmployeeLogEldEvent employeeLogEldEvent;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        employeeLogEldEvent = new EmployeeLogEldEvent();
        employeeLogEldEvent.setEventType(Enums.EmployeeLogEldEventType.LoginLogout);
        employeeLogEldEvent.setEventCode(1);
        employeeLogEldEvent.setEventDateTime(new Date(Date.parse("04/21/2016 12:45:04")));
        employeeLogEldEvent.setLogKey(1);
        employeeLogEldEvent.setAccumulatedVehicleMiles(454333);
        employeeLogEldEvent.setEngineHours(143433d);
        employeeLogEldEvent.setLatitude(51.5043070d);
        employeeLogEldEvent.setLongitude(-0.1275920d);//138
        employeeLogEldEvent.setTractorNumber("410 unit"); //CMV number //451
    }

    @Test
    public void testEventDataChecksum() throws Exception {
        Assert.assertEquals("2b", EventDataChecksumHelper.EventDataChecksum(employeeLogEldEvent, "drv1"));
    }

    @Test
    public void testEventDataChecksum2() throws Exception {
        EmployeeLogEldEvent empLogEldEvent = new EmployeeLogEldEvent();
        empLogEldEvent.setEventType(Enums.EmployeeLogEldEventType.IntermediateLog);
        empLogEldEvent.setEventCode(2);
        empLogEldEvent.setLogKey(1);
        empLogEldEvent.setEventDateTime(new Date(Date.parse("04/25/2016 12:01:33")));
        empLogEldEvent.setAccumulatedVehicleMiles(1445664);
        empLogEldEvent.setEngineHours(4667d);
        empLogEldEvent.setLatitude(52.5043070d);
        empLogEldEvent.setLongitude(-0.2275921d);
        empLogEldEvent.setTractorNumber("TEST");
        Assert.assertEquals("f9", EventDataChecksumHelper.EventDataChecksum(empLogEldEvent, "thisdriver"));
    }

    @Test
    public void testEventDataChecksum3() throws Exception {
        EmployeeLogEldEvent empLogEldEvent = new EmployeeLogEldEvent();
        empLogEldEvent.setEventType(Enums.EmployeeLogEldEventType.IntermediateLog);
        empLogEldEvent.setEventCode(1);
        empLogEldEvent.setLogKey(1);
        empLogEldEvent.setEventDateTime(new Date(Date.parse("08/11/2010 06:55:09")));
        empLogEldEvent.setAccumulatedVehicleMiles(8766439);
        empLogEldEvent.setEngineHours(10d);
        empLogEldEvent.setLatitude(44.154130d);
        empLogEldEvent.setLongitude(-88.547510d);
        empLogEldEvent.setTractorNumber("189"); //440
        Assert.assertEquals("07", EventDataChecksumHelper.EventDataChecksum(empLogEldEvent, "myEld"));
    }

    @Test
    public void testEventDataChecksum4() throws Exception {
        EmployeeLogEldEvent empLogEldEvent = new EmployeeLogEldEvent();
        empLogEldEvent.setEventType(Enums.EmployeeLogEldEventType.DutyStatusChange);
        empLogEldEvent.setEventCode(1);
        empLogEldEvent.setLogKey(1);
        empLogEldEvent.setEventDateTime(new Date(Date.parse("06/20/2017 15:57:16")));
        empLogEldEvent.setAccumulatedVehicleMiles(1);
        empLogEldEvent.setEngineHours(0.1d);
        empLogEldEvent.setLatitude(44.26d);
        empLogEldEvent.setLongitude(-88.41d);
        empLogEldEvent.setTractorNumber("Maj01"); //440
        Assert.assertEquals("26", EventDataChecksumHelper.EventDataChecksum(empLogEldEvent, "dedt"));
    }
}
