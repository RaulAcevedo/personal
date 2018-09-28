package com.jjkeller.kmbapi.malfunction;

import com.jjkeller.kmbapi.common.ITimeKeeper;
import com.jjkeller.kmbapi.common.TimeKeeper;
import com.jjkeller.kmbapi.controller.EOBR.EobrReader;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.enums.Malfunction;
import com.jjkeller.kmbapi.realtime.malfunction.TimingMalfunction;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;


public class TimingMalfunctionTest extends BaseMalfunctionTestConfig {

    private TimingMalfunction timingMalfunction;
    private EobrReader eobrReader;

    @Before
    public void setupTimingMalfunciton(){
        eobrReader = Mockito.mock(EobrReader.class);
        timingMalfunction = new TimingMalfunction(eobrReader, employeeLogEldMandateController);
    }


    @Test
    public void testServerTimeAhead() {
        Date tabTime = DateUtility.AddDays(new Date(), -20);
        Date dmoTime = new Date();
        when(eobrReader.Technician_ReadGPSTime()).thenReturn(dmoTime);
        timingMalfunction.validateTimingMalfunction(dmoTime, tabTime);
        assertEquals("timing malfunction should fire", 1,  getTotalMalfunctionEvents(Malfunction.TIMING_COMPLIANCE) );
    }


    @Test
    public void testServerTimeBehind() {
        Date tabTime = DateUtility.AddDays(new Date(), 20);
        Date dmoTime = new Date();
        timingMalfunction.validateTimingMalfunction(dmoTime, tabTime);
        assertEquals("timing malfunction should fire", 1,  getTotalMalfunctionEvents(Malfunction.TIMING_COMPLIANCE) );
    }


    @Test
    public void testServerTimeInSync() {
        Date dmoTime = new Date();
        Date tabTime = new Date();
        timingMalfunction.validateTimingMalfunction(dmoTime, tabTime);
        assertEquals("clock are in sync should be no malfunction", 0,  getTotalMalfunctionEvents(Malfunction.TIMING_COMPLIANCE) );
  }

    @Test
    public void testServerTimeWithin10Mins() {
        Date dmoTime = new Date();
        Date tabTime =  DateUtility.AddMinutes(new Date(), 9);
        timingMalfunction.validateTimingMalfunction(dmoTime, tabTime);
        assertEquals("clock are within 10 min be no malfunction", 0,  getTotalMalfunctionEvents(Malfunction.TIMING_COMPLIANCE) );
    }

    @Test
    public void testCantGetToDMO_ValidGPS() {
        Date gpsTime = DateUtility.AddMinutes(new Date(), 9);

        Date tabTime = new Date();
        timingMalfunction.validateTimingMalfunction(gpsTime, tabTime);

        assertEquals("no dmo time and gps time within threshold, cant validate anything for sure", 0,  getTotalMalfunctionEvents(Malfunction.TIMING_COMPLIANCE) );
    }



    @Test
    public void testCantGetToDMO_GPS_OutOFDate() {
        Date gpsTime =  DateUtility.AddMinutes(new Date(), 11);
        when(eobrReader.Technician_ReadGPSTime()).thenReturn(gpsTime);

        Date tabTime =  new Date();
        timingMalfunction.validateTimingMalfunction(gpsTime, tabTime);

        assertEquals("no dmo time and no gps, cant validate anything for sure", 1,  getTotalMalfunctionEvents(Malfunction.TIMING_COMPLIANCE) );
    }

    @Test
    public void testGetToDMO_AndroidClockOffDateGPS() {
        Date gpsTime = DateUtility.AddMinutes(new Date(), 20);

        Date tabTime =  DateUtility.AddMinutes(new Date(), 20);
        timingMalfunction.validateTimingMalfunction(gpsTime, tabTime);

        assertEquals("GPS and TAB are equal but we don`t have DMO", 0,  getTotalMalfunctionEvents(Malfunction.TIMING_COMPLIANCE) );
    }


    @Test
    public void testCanGetToDMO_TabOutOfDateGPS() {
        Date gpsTime =  DateUtility.AddMinutes(new Date(), 20);
        when(eobrReader.Technician_ReadGPSTime()).thenReturn(gpsTime);

        Date dmoTime =  new Date();
        Date tabTime =  new Date();
        timingMalfunction.validateTimingMalfunction(dmoTime, tabTime);

        assertEquals("dont care about gps time. we have dmo time", 0,  getTotalMalfunctionEvents(Malfunction.TIMING_COMPLIANCE) );
    }

    @Test
    public void testCantGetToDMO_CantGetGPS() {
        when(eobrReader.Technician_ReadGPSTime()).thenReturn(null);

        Date tabTime =  new Date();
        timingMalfunction.validateTimingMalfunction(null, tabTime);

        assertEquals("no dmo time and no gps, cant validate anything for sure", 0,  getTotalMalfunctionEvents(Malfunction.TIMING_COMPLIANCE) );
    }





    @Test
    public void testTimeKeeperSetWithGPSTime() {
        TimeKeeper.setTimeKeeper(null);
        ITimeKeeper keeper = TimeKeeper.getInstance();

        Date tabTime = DateUtility.AddDays(new Date(), -20);
        Date gpsTime = DateUtility.AddDays(new Date(), 1);
        when(eobrReader.Technician_ReadGPSTime()).thenReturn(gpsTime);
        timingMalfunction.validateTimingMalfunction(null, tabTime);

        DateTime timeKeeperClock = keeper.getCurrentDateTime();
        long diff = timeKeeperClock.getMillis() - gpsTime.getTime();
        assertTrue("Time keeper clock is not within 1 second of expected time.  ", diff < 1000);

        Date dmoTime = new Date();
        //When DMO is called to get the UTC time ... it will set time using the following command.
        DateUtility.SetSystemTime(dmoTime);

        long diff2 = keeper.getCurrentDateTime().getMillis() - dmoTime.getTime();
        assertTrue("Time keeper clock is not within 1 second of expected time. ", diff2 < 1000);

    }



    @Test
    public void testTimeKeeperAlreadySetWithDMOTime() {
        TimeKeeper.setTimeKeeper(null);
        ITimeKeeper keeper = TimeKeeper.getInstance();

        Date dmoTime = new Date();
        //When DMO is called to get the UTC time ... it will set time using the following command.
        DateUtility.SetSystemTime(dmoTime);

        Date tabTime = DateUtility.AddDays(new Date(), -20);
        Date gpsTime = DateUtility.AddDays(new Date(), 1);
        when(eobrReader.Technician_ReadGPSTime()).thenReturn(gpsTime);
        timingMalfunction.validateTimingMalfunction(null, tabTime);

        long diff = keeper.getCurrentDateTime().getMillis() - System.currentTimeMillis();
        assertTrue("Time keeper should not have been adjusted if we already had a dmo time ", diff < 10);
    }

}