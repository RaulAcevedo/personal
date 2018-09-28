package com.jjkeller.kmb.test.kmbapi.eldmandate;

import android.content.Context;

import com.jjkeller.kmb.test.KMBRoboElectricTestRunner;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.configuration.LoginCredentials;
import com.jjkeller.kmbapi.controller.LoginController;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.eldmandate.EventDataDiagnosticsChecker;
import com.jjkeller.kmbapi.employeelogeldevents.Enums;
import com.jjkeller.kmbapi.enums.DataDiagnosticEnum;
import com.jjkeller.kmbapi.enums.DatabusTypeEnum;
import com.jjkeller.kmbapi.enums.TimeZoneEnum;
import com.jjkeller.kmbapi.kmbeobr.StatusRecord;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;
import com.jjkeller.kmbapi.proxydata.Location;

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(KMBRoboElectricTestRunner.class)
public class EventDataDiagnosticsCheckerTest extends TestCase {
    private EventDataDiagnosticsChecker sut;

    @Before
    public void setUp(){

        sut = new EventDataDiagnosticsChecker();

        // set up a mock context for this test
        User user = new User();
        user.setCredentials(new LoginCredentials());
        user.getCredentials().setPrimaryKey(0);
        user.setHomeTerminalTimeZone(new TimeZoneEnum(TimeZoneEnum.CENTRALSTANDARDTIME));

        Context ctx = GlobalState.getInstance().getApplicationContext();
        new LoginController(ctx).setCurrentUser(user);
    }

    @Test
    public void testCheckForError_WhenLogKeyNotSet_DoesNotCreateDataDiagnosticEvent() throws Throwable {
        StatusRecord statusRecord = CreateValidStatusRecord(false);
        EmployeeLogEldEvent event = CreateValidEvent();
        event.setLogKey(0);

        DataDiagnosticEnum result = sut.CheckForError(event, false, statusRecord);

        Assert.assertNull(result);
    }

    @Test
    public void testCheckForError_WhenEobrSerialNumberNotSet_DoesNotCreateDataDiagnosticEvent() throws Throwable {
        StatusRecord statusRecord = CreateValidStatusRecord(false);
        EmployeeLogEldEvent event = CreateValidEvent();
        event.setEobrSerialNumber(null);

        DataDiagnosticEnum result = sut.CheckForError(event, false, statusRecord);

        Assert.assertNull(result);
    }

    @Test
    public void testCheckForError_WhenEngineNotRunning_DoesNotCreateDataDiagnosticEvent() throws Throwable{
        EmployeeLogEldEvent event = CreateValidEvent();
        StatusRecord statusRecord = CreateValidStatusRecord(false);
        statusRecord.setTachometer(0f);

        DataDiagnosticEnum result = sut.CheckForError(event, false, statusRecord);

        Assert.assertNull(result);
    }

    @Test
    public void testCheckForError_WhenDutyStatusEventAndNoPreviousGPSCoordinates_DoesNotCreateDataDiagnosticEvent() throws Throwable {
        StatusRecord statusRecord = CreateValidStatusRecord(false);
        EmployeeLogEldEvent event = CreateValidEvent(Enums.EmployeeLogEldEventType.DutyStatusChange);

        event.setLatitude(null);
        event.setLongitude(null);
        event.setDistanceSinceLastCoordinates(null);

        DataDiagnosticEnum result = sut.CheckForError(event, false, statusRecord);

        Assert.assertNull(result);
    }

    @Test
    public void testCheckForError_WhenDutyStatusEventAndLessThan5MilesSinceLastGPSAndNoLatLong_CreatesDataDiagnosticEvent() throws Throwable {
        StatusRecord statusRecord = CreateValidStatusRecord(false);
        EmployeeLogEldEvent event = CreateValidEvent(Enums.EmployeeLogEldEventType.DutyStatusChange);
        event.setLatitude(0d);
        event.setLongitude(0d);
        event.setDistanceSinceLastCoordinates(3f);

        DataDiagnosticEnum result = sut.CheckForError(event, false, statusRecord);

        Assert.assertEquals(DataDiagnosticEnum.MISSING_REQUIRED_DATA_ELEMENTS, result);
    }

    @Test
    public void testCheckForError_WhenDutyStatusEventAndMoreThan5MilesSinceLastGPSAndNoLocationDescription_CreatesDataDiagnosticEvent() throws Throwable {
        StatusRecord statusRecord = CreateValidStatusRecord(false);
        EmployeeLogEldEvent event = CreateValidEvent(Enums.EmployeeLogEldEventType.DutyStatusChange);
        event.setDistanceSinceLastCoordinates(6f);
        event.setDriversLocationDescription(null);

        DataDiagnosticEnum result = sut.CheckForError(event, false, statusRecord);

        Assert.assertEquals(DataDiagnosticEnum.MISSING_REQUIRED_DATA_ELEMENTS, result);
    }

    @Test
    public void testCheckForError_WhenDutyStatusEventAndEventWasEditedAndNoEventComment_CreatesDataDiagnosticEvent() throws Throwable {
        StatusRecord statusRecord = CreateValidStatusRecord(false);
        EmployeeLogEldEvent event = CreateValidEvent(Enums.EmployeeLogEldEventType.DutyStatusChange);
        event.setEventComment(null);

        DataDiagnosticEnum result = sut.CheckForError(event, true, statusRecord);

        Assert.assertEquals(DataDiagnosticEnum.MISSING_REQUIRED_DATA_ELEMENTS, result);
    }

    @Test
    public void testCheckForError_WhenIntermediateEventAndLessThan5MilesSinceLastGPSAndNoLatLong_CreatesDataDiagnosticEvent() throws Throwable {
        StatusRecord statusRecord = CreateValidStatusRecord(false);
        EmployeeLogEldEvent event = CreateValidEvent(Enums.EmployeeLogEldEventType.IntermediateLog);
        event.setLatitude(null);
        event.setLongitude(null);
        event.setDistanceSinceLastCoordinates(3f);

        DataDiagnosticEnum result = sut.CheckForError(event, false, statusRecord);

        Assert.assertEquals(DataDiagnosticEnum.MISSING_REQUIRED_DATA_ELEMENTS, result);
    }

    @Test
    public void testCheckForError_WhenChangeInDriversIndicationEventAndMoreThan5MilesSinceLastGPSAndNoLocationDescription_CreatesDataDiagnosticEvent() throws Throwable {
        StatusRecord statusRecord = CreateValidStatusRecord(false);
        EmployeeLogEldEvent event = CreateValidEvent(Enums.EmployeeLogEldEventType.ChangeInDriversIndication);
        event.setDistanceSinceLastCoordinates(6f);
        event.setDriversLocationDescription(null);

        DataDiagnosticEnum result = sut.CheckForError(event, false, statusRecord);

        Assert.assertEquals(DataDiagnosticEnum.MISSING_REQUIRED_DATA_ELEMENTS, result);
    }

    @Test
    public void testCheckForError_WhenChangeInDriversIndicationEventAndNoEventComment_CreatesDataDiagnosticEvent() throws Throwable{
        StatusRecord statusRecord = CreateValidStatusRecord(false);
        EmployeeLogEldEvent event = CreateValidEvent(Enums.EmployeeLogEldEventType.ChangeInDriversIndication);
        event.setEventComment(null);

        DataDiagnosticEnum result = sut.CheckForError(event, false, statusRecord);

        Assert.assertEquals(DataDiagnosticEnum.MISSING_REQUIRED_DATA_ELEMENTS, result);
    }

    @Test
    public void testCheckForError_WhenChangeInDriversIndicationEventAndValidData_DoesNotCreateDataDiagnosticEvent() throws Throwable{
        EmployeeLogEldEvent event = CreateValidEvent(Enums.EmployeeLogEldEventType.ChangeInDriversIndication);
        StatusRecord statusRecord = CreateValidStatusRecord(false);

        DataDiagnosticEnum result = sut.CheckForError(event, false, statusRecord);

        Assert.assertNull(result);
    }



    @Test
    public void testCheckForError_WhenLoginLogoutEventAndValidData_DoesNotCreateDataDiagnosticEvent() throws Throwable {
        EmployeeLogEldEvent event = CreateValidEvent(Enums.EmployeeLogEldEventType.LoginLogout);
        StatusRecord statusRecord = CreateValidStatusRecord(false);

        DataDiagnosticEnum result = sut.CheckForError(event, false, statusRecord);

        Assert.assertNull(result);
    }

    @Test
    public void testCheckForError_WhenLoginLogoutEventAndMissingOdometer_CreatesDataDiagnosticEvent() throws Throwable {
        EmployeeLogEldEvent event = CreateValidEvent(Enums.EmployeeLogEldEventType.LoginLogout);
        event.setOdometer(null);

        StatusRecord statusRecord = CreateValidStatusRecord(false);

        DataDiagnosticEnum result = sut.CheckForError(event, false, statusRecord);

        Assert.assertEquals(DataDiagnosticEnum.MISSING_REQUIRED_DATA_ELEMENTS, result);
    }

    @Test
    public void testCheckForError_WhenLoginLogoutEventAndMissingEngOnTimeHeavyBus_CreatesDataDiagnosticEvent() throws Throwable {
        EmployeeLogEldEvent event = CreateValidEvent(Enums.EmployeeLogEldEventType.LoginLogout);
        event.setEngineHours(null);

        StatusRecord statusRecord = CreateValidStatusRecord(true);

        DataDiagnosticEnum result = sut.CheckForError(event, false, statusRecord);

        Assert.assertEquals(DataDiagnosticEnum.MISSING_REQUIRED_DATA_ELEMENTS, result);
    }

    @Test
    public void testCheckForError_WhenLoginLogoutEventAndMissingEngOnTimeLightBus_DoesNotCreateDataDiagnosticEvent() throws Throwable {
        EmployeeLogEldEvent event = CreateValidEvent(Enums.EmployeeLogEldEventType.LoginLogout);
        event.setEngineHours(null);

        StatusRecord statusRecord = CreateValidStatusRecord(true);

        DataDiagnosticEnum result = sut.CheckForError(event, false, statusRecord);

        Assert.assertEquals(DataDiagnosticEnum.MISSING_REQUIRED_DATA_ELEMENTS, result);
    }

    @Test
    public void testCheckForError_WhenPowerupPowerdownEventAndValidData_DoesNotCreateDataDiagnosticEvent() throws Throwable {
        EmployeeLogEldEvent event = CreateValidEvent(Enums.EmployeeLogEldEventType.EnginePowerUpPowerDown);
        
        StatusRecord statusRecord = CreateValidStatusRecord(true);

        DataDiagnosticEnum result = sut.CheckForError(event, false, statusRecord);

        Assert.assertNull(result);
    }

    @Test
    public void testCheckForError_WhenPowerupPowerdownEventAndMissingOdometer_CreatesDataDiagnosticEvent() throws Throwable {
        EmployeeLogEldEvent event = CreateValidEvent(Enums.EmployeeLogEldEventType.EnginePowerUpPowerDown);
        event.setOdometer(null);

        StatusRecord statusRecord = CreateValidStatusRecord(false);

        DataDiagnosticEnum result = sut.CheckForError(event, false, statusRecord);

        Assert.assertEquals(DataDiagnosticEnum.MISSING_REQUIRED_DATA_ELEMENTS, result);
    }

    @Test
    public void testCheckForError_WhenPowerupPowerdownEventAndMissingEngOnTimeHeavyBus_CreatesDataDiagnosticEvent() throws Throwable {
        EmployeeLogEldEvent event = CreateValidEvent(Enums.EmployeeLogEldEventType.EnginePowerUpPowerDown);
        event.setEngineHours(null);

        StatusRecord statusRecord = CreateValidStatusRecord(true);

        DataDiagnosticEnum result = sut.CheckForError(event, false, statusRecord);

        Assert.assertEquals(DataDiagnosticEnum.MISSING_REQUIRED_DATA_ELEMENTS, result);
    }

    @Test
    public void testCheckForError_WhenPowerupPowerdownEventAndMissingEngOnTimeLightBus_DoesNotCreateDataDiagnosticEvent() throws Throwable {
        EmployeeLogEldEvent event = CreateValidEvent(Enums.EmployeeLogEldEventType.EnginePowerUpPowerDown);
        event.setEngineHours(null);

        StatusRecord statusRecord = CreateValidStatusRecord(true);

        DataDiagnosticEnum result = sut.CheckForError(event, false, statusRecord);

        Assert.assertEquals(DataDiagnosticEnum.MISSING_REQUIRED_DATA_ELEMENTS, result);
    }

    @Test
    public void testCheckForError_WhenPowerupPowerdownEventAndLessThan5MilesSinceLastGPSAndNoLatLong_CreatesDataDiagnosticEvent() throws Throwable {
        StatusRecord statusRecord = CreateValidStatusRecord(false);
        EmployeeLogEldEvent event = CreateValidEvent(Enums.EmployeeLogEldEventType.EnginePowerUpPowerDown);
        event.setLatitude(0d);
        event.setLongitude(0d);
        event.setDistanceSinceLastCoordinates(3f);

        DataDiagnosticEnum result = sut.CheckForError(event, false, statusRecord);

        Assert.assertEquals(DataDiagnosticEnum.MISSING_REQUIRED_DATA_ELEMENTS, result);
    }

    @Test
    public void testCheckForError_WhenDiagnosticEventAndValidData_DoesNotCreateDataDiagnosticEvent() throws Throwable {
        EmployeeLogEldEvent event = CreateValidEvent(Enums.EmployeeLogEldEventType.Malfunction_DataDiagnosticDetection);
        StatusRecord statusRecord = CreateValidStatusRecord(false);

        DataDiagnosticEnum result = sut.CheckForError(event, false, statusRecord);

        Assert.assertNull(result);
    }

    @Test
    public void testCheckForError_WhenDiagnosticEventAndMissingOdometer_CreatesDataDiagnosticEvent() throws Throwable {
        EmployeeLogEldEvent event = CreateValidEvent(Enums.EmployeeLogEldEventType.Malfunction_DataDiagnosticDetection);
        event.setOdometer(null);

        StatusRecord statusRecord = CreateValidStatusRecord(false);

        DataDiagnosticEnum result = sut.CheckForError(event, false, statusRecord);

        Assert.assertEquals(DataDiagnosticEnum.MISSING_REQUIRED_DATA_ELEMENTS, result);
    }

    @Test
    public void testCheckForError_WhenDiagnosticEventAndMissingEngOnTimeHeavyBus_CreatesDataDiagnosticEvent() throws Throwable {
        EmployeeLogEldEvent event = CreateValidEvent(Enums.EmployeeLogEldEventType.Malfunction_DataDiagnosticDetection);
        event.setEngineHours(null);

        StatusRecord statusRecord = CreateValidStatusRecord(true);

        DataDiagnosticEnum result = sut.CheckForError(event, false, statusRecord);

        Assert.assertEquals(DataDiagnosticEnum.MISSING_REQUIRED_DATA_ELEMENTS, result);
    }

    @Test
    public void testCheckForError_WhenDiagnosticEventAndMissingEngOnTimeLightBus_DoesNotCreateDataDiagnosticEvent() throws Throwable {
        EmployeeLogEldEvent event = CreateValidEvent(Enums.EmployeeLogEldEventType.Malfunction_DataDiagnosticDetection);
        event.setEngineHours(null);

        StatusRecord statusRecord = CreateValidStatusRecord(true);

        DataDiagnosticEnum result = sut.CheckForError(event, false, statusRecord);

        Assert.assertEquals(DataDiagnosticEnum.MISSING_REQUIRED_DATA_ELEMENTS, result);
    }
    
    private StatusRecord CreateValidStatusRecord(boolean heavyBus) {
        StatusRecord statusRecord = new StatusRecord();
        statusRecord.setTachometer(1000f);
        
        if(heavyBus) statusRecord.setActiveBusType(DatabusTypeEnum.J1708);

        return statusRecord;
    }

    private EmployeeLogEldEvent CreateValidEvent() {
        return CreateValidEvent(Enums.EmployeeLogEldEventType.DutyStatusChange);
    }

    private EmployeeLogEldEvent CreateValidEvent(Enums.EmployeeLogEldEventType eventType) {
        EmployeeLogEldEvent event = new EmployeeLogEldEvent(DateUtility.getCurrentDateTimeUTC());
        event.setEventType(eventType);
        event.setLogKey(1);
        event.setEobrSerialNumber("1");
        event.setLatitude(10d);
        event.setLongitude(10d);
        event.setDistanceSinceLastCoordinates(1f);
        event.setEventComment("text");
        event.setEngineHours(10d);
        event.setOdometer(100f);
        event.setTractorNumber("1552");
        event.setTrailerNumber("85841");
        event.setShipmentInfo("100 MW LiPo batteries");

        return event;
    }
}
