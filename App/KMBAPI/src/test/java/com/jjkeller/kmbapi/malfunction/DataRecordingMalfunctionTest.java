package com.jjkeller.kmbapi.malfunction;

import android.content.Intent;

import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.enums.Malfunction;
import com.jjkeller.kmbapi.realtime.malfunction.DataRecordingMalfunction;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;


public class DataRecordingMalfunctionTest extends BaseMalfunctionTestConfig {

    private DataRecordingMalfunction dataRecordingMalfunction;

    @Before
    public void setupDataRecTest(){
        dataRecordingMalfunction = new DataRecordingMalfunction();
    }

    @Test
    public void testDataRecordingMalfunction_readError() {
        dataRecordingMalfunction.checkDataRecordingMalfunction(504, 0, 200);
        assertTrue("returned record id of 0 when query by id should trigger malfunction",  employeeLogEldMandateController.isMalfunctioning(employeeLog, Malfunction.DATA_RECORDING_COMPLIANCE));
    }

    @Test
    public void testDataRecordingMalfunction_ignoreQueriesNotDoneById() {
        dataRecordingMalfunction.checkDataRecordingMalfunction(0, 500, 200);
        assertFalse("query for latest should not trigger malfunction",  employeeLogEldMandateController.isMalfunctioning(employeeLog, Malfunction.DATA_RECORDING_COMPLIANCE));
    }

    @Test
    public void testDataRecordingMalfunction_ignoreCheckNonMandate() {
        when(ftService.getIsEldMandateEnabled()).thenReturn(false);
        dataRecordingMalfunction.checkDataRecordingMalfunction(5, 0, 10);
        assertFalse("non mandate should not malfunction",  employeeLogEldMandateController.isMalfunctioning(employeeLog, Malfunction.DATA_RECORDING_COMPLIANCE));
    }

    @Test
    public void testDataRecordingMalfunction_fail_then_clear() {

        dataRecordingMalfunction.checkDataRecordingMalfunction(5, 0, 3);
        dataRecordingMalfunction.checkDataRecordingMalfunction(10, 0, 5);
        dataRecordingMalfunction.checkDataRecordingMalfunction(2, 0, 1);

        dataRecordingMalfunction.checkDataRecordingMalfunction(0, 234, 1);

        assertTrue("several failures",  employeeLogEldMandateController.isMalfunctioning(employeeLog, Malfunction.DATA_RECORDING_COMPLIANCE));
        Assert.assertEquals("only 1 malfunction should be added to log", 1,  getTotalMalfunctionEvents(Malfunction.DATA_RECORDING_COMPLIANCE));

        dataRecordingMalfunction.checkDataRecordingMalfunction(543, 543, 200);
        assertFalse("should have cleared malfunction",  employeeLogEldMandateController.isMalfunctioning(employeeLog, Malfunction.DATA_RECORDING_COMPLIANCE));
        assertEquals("create and clear should be added to log.", 2,  getTotalMalfunctionEvents(Malfunction.DATA_RECORDING_COMPLIANCE));
    }

    @Test
    public void testDataRecordingMalfunction_storageLow(){
        assertFalse("should not be malfunctioning at start", employeeLogEldMandateController.isMalfunctioning(employeeLog, Malfunction.DATA_RECORDING_COMPLIANCE));

        Intent intent = new Intent(Intent.ACTION_DEVICE_STORAGE_LOW);
        GlobalState.getInstance().sendStickyBroadcast(intent);

        assertTrue("should be malfunctioning when storage is low", employeeLogEldMandateController.isMalfunctioning(employeeLog, Malfunction.DATA_RECORDING_COMPLIANCE));
    }

    @Test
    public void testDataRecordingMalfunction_storageOkay(){
        assertFalse("should not be malfunctioning at start", employeeLogEldMandateController.isMalfunctioning(employeeLog, Malfunction.DATA_RECORDING_COMPLIANCE));

        Intent storageLowIntent = new Intent(Intent.ACTION_DEVICE_STORAGE_LOW);
        GlobalState.getInstance().sendStickyBroadcast(storageLowIntent);

        assertTrue("should activate when storage is low", employeeLogEldMandateController.isMalfunctioning(employeeLog, Malfunction.DATA_RECORDING_COMPLIANCE));

        Intent storageOkIntent = new Intent(Intent.ACTION_DEVICE_STORAGE_OK);
        GlobalState.getInstance().sendStickyBroadcast(storageOkIntent);

        assertFalse("should be cleared when storage is ok", employeeLogEldMandateController.isMalfunctioning(employeeLog, Malfunction.DATA_RECORDING_COMPLIANCE));
    }

    @Test
    public void testDataRecordingMalfunction_mixedStorageAndRead(){
        dataRecordingMalfunction.checkDataRecordingMalfunction(504, 0, 200);
        assertTrue("returned record id of 0 when query by id should trigger malfunction",  employeeLogEldMandateController.isMalfunctioning(employeeLog, Malfunction.DATA_RECORDING_COMPLIANCE));

        Intent storageLowIntent = new Intent(Intent.ACTION_DEVICE_STORAGE_LOW);
        GlobalState.getInstance().sendStickyBroadcast(storageLowIntent);
        assertTrue("should still be malfunctioning with both data recording and storage issues", employeeLogEldMandateController.isMalfunctioning(employeeLog, Malfunction.DATA_RECORDING_COMPLIANCE));

        dataRecordingMalfunction.checkDataRecordingMalfunction(543, 543, 200);
        assertTrue("should still be malfunctioning with a data recording issue", employeeLogEldMandateController.isMalfunctioning(employeeLog, Malfunction.DATA_RECORDING_COMPLIANCE));

        Intent storageOkIntent = new Intent(Intent.ACTION_DEVICE_STORAGE_OK);
        GlobalState.getInstance().sendStickyBroadcast(storageOkIntent);
        assertFalse("both issues should be cleared", employeeLogEldMandateController.isMalfunctioning(employeeLog, Malfunction.DATA_RECORDING_COMPLIANCE));

        //Let's try clearing the other way too.
        dataRecordingMalfunction.checkDataRecordingMalfunction(504, 0, 200);
        assertTrue("returned record id of 0 when query by id should trigger malfunction",  employeeLogEldMandateController.isMalfunctioning(employeeLog, Malfunction.DATA_RECORDING_COMPLIANCE));

        GlobalState.getInstance().sendStickyBroadcast(storageLowIntent);
        assertTrue("should still be malfunctioning with both data recording and storage issues", employeeLogEldMandateController.isMalfunctioning(employeeLog, Malfunction.DATA_RECORDING_COMPLIANCE));

        GlobalState.getInstance().sendStickyBroadcast(storageOkIntent);
        assertTrue("both issues should be cleared.", employeeLogEldMandateController.isMalfunctioning(employeeLog, Malfunction.DATA_RECORDING_COMPLIANCE));

        dataRecordingMalfunction.checkDataRecordingMalfunction(543, 543, 200);
        assertFalse("should still be malfunctioning with a data recording issue", employeeLogEldMandateController.isMalfunctioning(employeeLog, Malfunction.DATA_RECORDING_COMPLIANCE));
    }

    @Test
    public void testDataRecordingMalfunction_NoMalfunctionNullStatusBufferNumberOfTrips() {
        dataRecordingMalfunction.checkDataRecordingMalfunction(120, 0, null);
        assertFalse("no malfunction if unable to retrieve number of trips from status buffer",  employeeLogEldMandateController.isMalfunctioning(employeeLog, Malfunction.DATA_RECORDING_COMPLIANCE));
    }

    @Test
    public void testDataRecordingMalfunction_NoMalfunctionWithResultRecordId() {
        dataRecordingMalfunction.checkDataRecordingMalfunction(120, 180, 100);
        assertFalse("no malfunction if query record Id > status buffer trips and result status record Id > 0",  employeeLogEldMandateController.isMalfunctioning(employeeLog, Malfunction.DATA_RECORDING_COMPLIANCE));
    }

    @Test
    public void testDataRecordingMalfunction_NoMalfunctionQueryRecordIdLessThanStatusBufferNumberOfTrips() {
        dataRecordingMalfunction.checkDataRecordingMalfunction(120, 0, 200);
        assertFalse("no malfunction if query record Id < status buffer trips even though record Id is 0 (6.88.0 firmware will do this)",  employeeLogEldMandateController.isMalfunctioning(employeeLog, Malfunction.DATA_RECORDING_COMPLIANCE));
    }
}