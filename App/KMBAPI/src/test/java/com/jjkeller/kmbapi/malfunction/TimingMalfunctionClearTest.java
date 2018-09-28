package com.jjkeller.kmbapi.malfunction;

import android.content.Context;

import com.jjkeller.kmbapi.controller.ControllerFactory;
import com.jjkeller.kmbapi.controller.EOBR.EobrReader;
import com.jjkeller.kmbapi.controller.FailureController;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.enums.Malfunction;
import com.jjkeller.kmbapi.realtime.malfunction.ClearTimingMalfunctionProcess;

import org.junit.Test;
import org.mockito.Mockito;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;


public class TimingMalfunctionClearTest extends BaseMalfunctionTestConfig {


    private void saveMalfunction(Malfunction malfunction){
        try{
            ControllerFactory.getInstance().getEmployeeLogEldMandateController().createMalfunctionForLoggedInUsers(
                     DateUtility.getCurrentDateTimeWithSecondsUTC()
                    , malfunction);
        }catch (Throwable e){

        }
    }

    @Test
    public void testResolveTimingMalfunction() {
        EobrReader reader = Mockito.mock(EobrReader.class);

        FailureController failureController = ControllerFactory.getInstance().getFailureController();
        failureController = Mockito.spy(failureController);

        Date eldTime = DateUtility.AddMinutes(new Date(), 9);
        doReturn(eldTime).when(reader).Technician_ReadClockUniversalTime(any(Context.class));

        Date dmoTime = new Date();
        doReturn(dmoTime).when(failureController).GetDmoTime();

        saveMalfunction(Malfunction.TIMING_COMPLIANCE);

        assertEquals("should have 1 ", 1,  getTotalMalfunctionEvents(Malfunction.TIMING_COMPLIANCE));

        ClearTimingMalfunctionProcess process = new ClearTimingMalfunctionProcess(reader, failureController, employeeLogEldMandateController);
        process.run();

        assertEquals("should have 2 ", 2,  getTotalMalfunctionEvents(Malfunction.TIMING_COMPLIANCE));
    }


    @Test
    public void testResolveTimingMalfunctionNotResolved() {
        EobrReader reader = Mockito.mock(EobrReader.class);

        FailureController failureController = ControllerFactory.getInstance().getFailureController();
        failureController = Mockito.spy(failureController);

        Date eldTime = DateUtility.AddMinutes(new Date(), 11);
        doReturn(eldTime).when(reader).Technician_ReadClockUniversalTime(any(Context.class));


        Date dmoTime = new Date();
        doReturn(dmoTime).when(failureController).GetDmoTime();

        saveMalfunction(Malfunction.TIMING_COMPLIANCE);

        assertEquals("should have 1 ", 1,  getTotalMalfunctionEvents(Malfunction.TIMING_COMPLIANCE));

        ClearTimingMalfunctionProcess process = new ClearTimingMalfunctionProcess(reader, failureController, employeeLogEldMandateController);
        process.run();

        assertEquals("should have 1 should not clear ", 1,  getTotalMalfunctionEvents(Malfunction.TIMING_COMPLIANCE));

    }


    @Test
    public void testResolveTimingMalfunctionNoMalfunctionPresent() {
        EobrReader reader = Mockito.mock(EobrReader.class);

        FailureController failureController = ControllerFactory.getInstance().getFailureController();
        failureController = Mockito.spy(failureController);

        Date eldTime = DateUtility.AddMinutes(new Date(), 11);
        doReturn(eldTime).when(reader).Technician_ReadClockUniversalTime(any(Context.class));

        Date dmoTime = new Date();
        doReturn(dmoTime).when(failureController).GetDmoTime();

        assertEquals("no malfunctions should be found ", 0,  getTotalMalfunctionEvents(Malfunction.TIMING_COMPLIANCE));

        ClearTimingMalfunctionProcess process = new ClearTimingMalfunctionProcess(reader, failureController, employeeLogEldMandateController);
        process.run();

        assertEquals("no malfunctions should be found", 0,  getTotalMalfunctionEvents(Malfunction.TIMING_COMPLIANCE));

    }



    
    @Test
    public void testResolveTimingMalfunctionCantAccessDMO() {
        EobrReader reader = Mockito.mock(EobrReader.class);

        FailureController failureController = ControllerFactory.getInstance().getFailureController();
        failureController = Mockito.spy(failureController);

        Date eldTime = DateUtility.AddMinutes(new Date(), 11);
        doReturn(eldTime).when(reader).Technician_ReadClockUniversalTime(any(Context.class));

        doReturn(null).when(failureController).GetDmoTime();

        saveMalfunction(Malfunction.TIMING_COMPLIANCE);

        assertEquals("should have 1 ", 1,  getTotalMalfunctionEvents(Malfunction.TIMING_COMPLIANCE));

        ClearTimingMalfunctionProcess process = new ClearTimingMalfunctionProcess(reader, failureController, employeeLogEldMandateController);
        process.run();

        assertEquals("should have 1 should not clear ", 1,  getTotalMalfunctionEvents(Malfunction.TIMING_COMPLIANCE));

    }


    
    @Test
    public void testResolveTimingMalfunctionCantGetTimeFromEld() {
        EobrReader reader = Mockito.mock(EobrReader.class);

        FailureController failureController = ControllerFactory.getInstance().getFailureController();
        failureController = Mockito.spy(failureController);

        doReturn(null).when(reader).Technician_ReadClockUniversalTime(any(Context.class));

        Date dmoTime = new Date();
        doReturn(dmoTime).when(failureController).GetDmoTime();

        saveMalfunction(Malfunction.TIMING_COMPLIANCE);

        assertEquals("should have 1 ", 1,  getTotalMalfunctionEvents(Malfunction.TIMING_COMPLIANCE));

        ClearTimingMalfunctionProcess process = new ClearTimingMalfunctionProcess(reader, failureController, employeeLogEldMandateController);
        process.run();

        assertEquals("should have 1 should not clear ", 1,  getTotalMalfunctionEvents(Malfunction.TIMING_COMPLIANCE));

    }
}