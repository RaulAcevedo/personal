package com.jjkeller.kmbapi.controller;

import com.jjkeller.kmbapi.TestableTimeKeeper;
import com.jjkeller.kmbapi.common.TestBase;
import com.jjkeller.kmbapi.common.TimeKeeper;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.configuration.LoginCredentials;
import com.jjkeller.kmbapi.controller.abstracts.AOBRDControllerBase;
import com.jjkeller.kmbapi.controller.dataaccess.EmployeeLogEldEventFacade;
import com.jjkeller.kmbapi.controller.dataaccess.EmployeeLogFacade;
import com.jjkeller.kmbapi.controller.interfaces.IAPIController;
import com.jjkeller.kmbapi.controller.share.MandateObjectFactory;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.controller.utility.EmployeeLogUtilities;
import com.jjkeller.kmbapi.eldmandate.EventDataDiagnosticsChecker;
import com.jjkeller.kmbapi.eldmandate.EventSequenceIdGenerator;
import com.jjkeller.kmbapi.employeelogeldevents.EmployeeLogEldEventCode;
import com.jjkeller.kmbapi.employeelogeldevents.Enums;
import com.jjkeller.kmbapi.employeelogeldevents.UnclaimedEventDTO;
import com.jjkeller.kmbapi.enums.DataDiagnosticEnum;
import com.jjkeller.kmbapi.enums.DriverTypeEnum;
import com.jjkeller.kmbapi.enums.DutyStatusEnum;
import com.jjkeller.kmbapi.enums.Malfunction;
import com.jjkeller.kmbapi.enums.RuleSetTypeEnum;
import com.jjkeller.kmbapi.enums.TimeZoneEnum;
import com.jjkeller.kmbapi.featuretoggle.FeatureToggleService;
import com.jjkeller.kmbapi.kmbeobr.StatusRecord;
import com.jjkeller.kmbapi.proxydata.CompanyConfigSettings;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEventList;
import com.jjkeller.kmbapi.proxydata.Location;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(application = GlobalState.class,
        manifest = "/build/intermediates/bundles/encompass/debug/AndroidManifest.xml"
)
public class EmployeeLogEldMandateControllerTest extends TestBase {

    private GlobalState app;
    private Integer sequenceId;
    User user;

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        app = (GlobalState) RuntimeEnvironment.application;

        FeatureToggleService ftService = mock(FeatureToggleService.class);
        when(ftService.getIsEldMandateEnabled()).thenReturn(true);

        Field field = GlobalState.class.getDeclaredField("_featureToggleService");
        field.setAccessible(true);
        field.set(app, ftService);

        CompanyConfigSettings settings = new CompanyConfigSettings();
        settings.setDailyLogStartTime("00:00");
        settings.setDmoCompanyName("DefaultCorp");
        app.setCompanyConfigSettings(app, settings);

        user = new User();
        LoginCredentials creds = new LoginCredentials();
        creds.setEmployeeId("00000000-0000-0000-0000-000000000000");

        user.setCredentials(creds);
        user.setHomeTerminalTimeZone(TimeZoneEnum.UTC);
        user.setRulesetTypeEnum(new RuleSetTypeEnum(RuleSetTypeEnum.US70HOUR));
        user.setDriverType(new DriverTypeEnum(DriverTypeEnum.PROPERTYCARRYING));

        app.setCurrentUser(user);
    }

    @Override
    public String toString() {
        return "EmployeeLogEldMandateControllerTest{" +
                "app=" + app +
                '}';
    }

    @Test
    public void test() {
        EmployeeLogEldEventFacade employeeLogEldEventFacade = mock(EmployeeLogEldEventFacade.class);

        TimeZone tz = TimeZone.getTimeZone("PST");
        Now(2016, Calendar.JULY, 30, 13, 30, 15, tz);

        List<EmployeeLogEldEvent> inputEvents = new ArrayList<>();
        inputEvents.add(createEmployeeLogEldEvent(1L, "2016-07-30T00:00:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EmployeeLogEldEventCode.DutyStatus_Driving, 1, 1, "8564", new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR)));
        inputEvents.add(createEmployeeLogEldEvent(2L, "2016-07-30T05:00:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EmployeeLogEldEventCode.DutyStatus_Driving, 1, 1, "8564", new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR)));
        inputEvents.add(createEmployeeLogEldEvent(3L, "2016-07-30T06:30:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EmployeeLogEldEventCode.DutyStatus_OnDuty, 1, 2, "8564", new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR)));
        inputEvents.add(createEmployeeLogEldEvent(4L, "2016-07-30T11:00:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EmployeeLogEldEventCode.DutyStatus_OnDuty, 1, 1, "8564", new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR)));
        inputEvents.add(createEmployeeLogEldEvent(5L, "2016-07-30T12:30:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EmployeeLogEldEventCode.DutyStatus_OnDuty, 1, 1, "8564", new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR)));
        inputEvents.add(createEmployeeLogEldEvent(6L, "2016-07-30T16:00:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EmployeeLogEldEventCode.DutyStatus_OnDuty, 1, 1, "8564", new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR)));
        inputEvents.add(createEmployeeLogEldEvent(7L, "2016-07-30T16:30:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EmployeeLogEldEventCode.DutyStatus_OnDuty, 1, 1, "8564", new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR)));
        inputEvents.add(createEmployeeLogEldEvent(8L, "2016-07-30T18:30:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EmployeeLogEldEventCode.DutyStatus_OnDuty, 1, 1, "8564", new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR)));
        inputEvents.add(createEmployeeLogEldEvent(9L, "2016-07-30T18:30:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EmployeeLogEldEventCode.DutyStatus_OnDuty, 1, 1, "8564", new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR)));
        inputEvents.add(createEmployeeLogEldEvent(10L, "2016-07-30T19:00:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EmployeeLogEldEventCode.DutyStatus_OnDuty, 1, 1, "8564", new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR)));
        inputEvents.add(createEmployeeLogEldEvent(11L, "2016-07-30T19:30:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EmployeeLogEldEventCode.DutyStatus_OnDuty, 1, 1, "8564", new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR)));
        inputEvents.add(createEmployeeLogEldEvent(12L, "2016-07-30T20:00:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EmployeeLogEldEventCode.DutyStatus_OnDuty, 1, 1, "8564", new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR)));
        inputEvents.add(createEmployeeLogEldEvent(13L, "2016-07-30T19:00:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EmployeeLogEldEventCode.DutyStatus_OnDuty, 1, 1, "8564", new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR)));
        inputEvents.add(createEmployeeLogEldEvent(14L, "2016-07-30T19:30:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EmployeeLogEldEventCode.DutyStatus_OnDuty, 1, 1, "8564", new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR)));
        inputEvents.add(createEmployeeLogEldEvent(15L, "2016-07-30T20:00:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EmployeeLogEldEventCode.DutyStatus_OnDuty, 1, 1, "8564", new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR)));

        // Can't include milliseconds in these times, or we won't be able to predict the "editDuration" in the expected result below
        Calendar cal = Calendar.getInstance(tz);
        cal.set(2016, Calendar.JULY, 30, 12, 30, 15);
        Date originalStartTime = cal.getTime();

        cal.set(2016, Calendar.JULY, 30, 14, 30, 15);
        cal.set(Calendar.MILLISECOND, 0);
        Date endTime = cal.getTime();

        EmployeeLogEldEvent eldEvent = createEmployeeLogEldEvent(0L, "2016-07-30T20:00:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EmployeeLogEldEventCode.DutyStatus_OnDuty, 1, 1, "8564", new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR));
        when(employeeLogEldEventFacade.FetchByKey(any(Integer.class))).thenReturn(eldEvent);

        boolean isEditingAutomaticDriveTime = false;
        EventSequenceIdGenerator sequenceIdGenerator = mock(EventSequenceIdGenerator.class);

        sequenceId = 0;
        when(sequenceIdGenerator.GetNextSequenceNumber()).then(new Answer<Integer>() {
            @Override
            public Integer answer(InvocationOnMock invocation) throws Throwable {
                return EmployeeLogEldMandateControllerTest.this.sequenceId++;
            }
        });

        List<EmployeeLogEldEvent> results = new EmployeeLogEldMandateController(app.getBaseContext(), employeeLogEldEventFacade, sequenceIdGenerator)
                .reconcileEvents(eldEvent, originalStartTime, endTime, inputEvents, isEditingAutomaticDriveTime);

        StringBuilder outBuilder = new StringBuilder();
        for (EmployeeLogEldEvent evt : results) {
            outBuilder.append(evt + "\n");
        }

        String expected =
                "EmployeeLogEldEvent{driverOriginatorUserId='00000000-0000-0000-0000-000000000000', encompassOriginatorUserId='null', unidentifiedUserId='null', eobrSerialNumber='null', logKey=null, eventSequenceIDNumber=0, eventRecordStatus=1, eventRecordOrigin=1, eventType=DutyStatusChange, eventCode=3, eventDateTime=2016-07-30T00:00:00Z, ruleSet=EnumBase{value=26}, accumulatedVehicleMiles=null, odometer=null, distance=null, engineHours=null, latitude=null, longitude=null, latitudeStatusCode='null', longitudeStatusCode='null', isGpsAtReducedPrecision=false, distanceSinceLastCoordinates=null, eldMalfunctionIndicatorStatus=false, driverDataDiagnosticEventIndicatorStatus=false, eventComment='null', driversLocationDescription='', eventDataCheck='null', diagnosticCode='null', geolocation='null', tractorNumber='8564', vehiclePlate='null', trailerNumber='null', trailerPlate='null', shipmentInfo='null', logRemark='null', originalEvent=true, endMoveDateTime=null, isEventDateTimeValidated=true, endOdometer=null, logRemarkDateTime=null, isManuallyEditedByKMBUser=false, gpsTimestamp=null, editDuration=null, motionPictureAuthorityId='null', motionPictureProductionId='null', encompassClusterPK=0, isUnidentifiedEvent=0}\n" +
                        "EmployeeLogEldEvent{driverOriginatorUserId='00000000-0000-0000-0000-000000000000', encompassOriginatorUserId='null', unidentifiedUserId='null', eobrSerialNumber='null', logKey=null, eventSequenceIDNumber=1, eventRecordStatus=1, eventRecordOrigin=1, eventType=DutyStatusChange, eventCode=3, eventDateTime=2016-07-30T05:00:00Z, ruleSet=EnumBase{value=26}, accumulatedVehicleMiles=null, odometer=null, distance=null, engineHours=null, latitude=null, longitude=null, latitudeStatusCode='null', longitudeStatusCode='null', isGpsAtReducedPrecision=false, distanceSinceLastCoordinates=null, eldMalfunctionIndicatorStatus=false, driverDataDiagnosticEventIndicatorStatus=false, eventComment='null', driversLocationDescription='', eventDataCheck='null', diagnosticCode='null', geolocation='null', tractorNumber='8564', vehiclePlate='null', trailerNumber='null', trailerPlate='null', shipmentInfo='null', logRemark='null', originalEvent=true, endMoveDateTime=null, isEventDateTimeValidated=true, endOdometer=null, logRemarkDateTime=null, isManuallyEditedByKMBUser=false, gpsTimestamp=null, editDuration=null, motionPictureAuthorityId='null', motionPictureProductionId='null', encompassClusterPK=0, isUnidentifiedEvent=0}\n" +
                        "EmployeeLogEldEvent{driverOriginatorUserId='00000000-0000-0000-0000-000000000000', encompassOriginatorUserId='null', unidentifiedUserId='null', eobrSerialNumber='null', logKey=null, eventSequenceIDNumber=2, eventRecordStatus=1, eventRecordOrigin=2, eventType=DutyStatusChange, eventCode=4, eventDateTime=2016-07-30T06:30:00Z, ruleSet=EnumBase{value=26}, accumulatedVehicleMiles=null, odometer=null, distance=null, engineHours=null, latitude=null, longitude=null, latitudeStatusCode='null', longitudeStatusCode='null', isGpsAtReducedPrecision=false, distanceSinceLastCoordinates=null, eldMalfunctionIndicatorStatus=false, driverDataDiagnosticEventIndicatorStatus=false, eventComment='null', driversLocationDescription='', eventDataCheck='null', diagnosticCode='null', geolocation='null', tractorNumber='8564', vehiclePlate='null', trailerNumber='null', trailerPlate='null', shipmentInfo='null', logRemark='null', originalEvent=true, endMoveDateTime=null, isEventDateTimeValidated=true, endOdometer=null, logRemarkDateTime=null, isManuallyEditedByKMBUser=false, gpsTimestamp=null, editDuration=null, motionPictureAuthorityId='null', motionPictureProductionId='null', encompassClusterPK=0, isUnidentifiedEvent=0}\n" +
                        "EmployeeLogEldEvent{driverOriginatorUserId='00000000-0000-0000-0000-000000000000', encompassOriginatorUserId='null', unidentifiedUserId='null', eobrSerialNumber='null', logKey=null, eventSequenceIDNumber=3, eventRecordStatus=1, eventRecordOrigin=1, eventType=DutyStatusChange, eventCode=4, eventDateTime=2016-07-30T11:00:00Z, ruleSet=EnumBase{value=26}, accumulatedVehicleMiles=null, odometer=null, distance=null, engineHours=null, latitude=null, longitude=null, latitudeStatusCode='null', longitudeStatusCode='null', isGpsAtReducedPrecision=false, distanceSinceLastCoordinates=null, eldMalfunctionIndicatorStatus=false, driverDataDiagnosticEventIndicatorStatus=false, eventComment='null', driversLocationDescription='', eventDataCheck='null', diagnosticCode='null', geolocation='null', tractorNumber='8564', vehiclePlate='null', trailerNumber='null', trailerPlate='null', shipmentInfo='null', logRemark='null', originalEvent=true, endMoveDateTime=null, isEventDateTimeValidated=true, endOdometer=null, logRemarkDateTime=null, isManuallyEditedByKMBUser=false, gpsTimestamp=null, editDuration=null, motionPictureAuthorityId='null', motionPictureProductionId='null', encompassClusterPK=0, isUnidentifiedEvent=0}\n" +
                        "EmployeeLogEldEvent{driverOriginatorUserId='00000000-0000-0000-0000-000000000000', encompassOriginatorUserId='null', unidentifiedUserId='null', eobrSerialNumber='null', logKey=null, eventSequenceIDNumber=4, eventRecordStatus=1, eventRecordOrigin=1, eventType=DutyStatusChange, eventCode=4, eventDateTime=2016-07-30T12:30:00Z, ruleSet=EnumBase{value=26}, accumulatedVehicleMiles=null, odometer=null, distance=null, engineHours=null, latitude=null, longitude=null, latitudeStatusCode='null', longitudeStatusCode='null', isGpsAtReducedPrecision=false, distanceSinceLastCoordinates=null, eldMalfunctionIndicatorStatus=false, driverDataDiagnosticEventIndicatorStatus=false, eventComment='null', driversLocationDescription='', eventDataCheck='null', diagnosticCode='null', geolocation='null', tractorNumber='8564', vehiclePlate='null', trailerNumber='null', trailerPlate='null', shipmentInfo='null', logRemark='null', originalEvent=true, endMoveDateTime=null, isEventDateTimeValidated=true, endOdometer=null, logRemarkDateTime=null, isManuallyEditedByKMBUser=false, gpsTimestamp=null, editDuration=null, motionPictureAuthorityId='null', motionPictureProductionId='null', encompassClusterPK=0, isUnidentifiedEvent=0}\n" +
                        "EmployeeLogEldEvent{driverOriginatorUserId='00000000-0000-0000-0000-000000000000', encompassOriginatorUserId='null', unidentifiedUserId='null', eobrSerialNumber='null', logKey=null, eventSequenceIDNumber=5, eventRecordStatus=1, eventRecordOrigin=1, eventType=DutyStatusChange, eventCode=4, eventDateTime=2016-07-30T16:00:00Z, ruleSet=EnumBase{value=26}, accumulatedVehicleMiles=null, odometer=null, distance=null, engineHours=null, latitude=null, longitude=null, latitudeStatusCode='null', longitudeStatusCode='null', isGpsAtReducedPrecision=false, distanceSinceLastCoordinates=null, eldMalfunctionIndicatorStatus=false, driverDataDiagnosticEventIndicatorStatus=false, eventComment='null', driversLocationDescription='', eventDataCheck='null', diagnosticCode='null', geolocation='null', tractorNumber='8564', vehiclePlate='null', trailerNumber='null', trailerPlate='null', shipmentInfo='null', logRemark='null', originalEvent=true, endMoveDateTime=null, isEventDateTimeValidated=true, endOdometer=null, logRemarkDateTime=null, isManuallyEditedByKMBUser=false, gpsTimestamp=null, editDuration=null, motionPictureAuthorityId='null', motionPictureProductionId='null', encompassClusterPK=0, isUnidentifiedEvent=0}\n" +
                        "EmployeeLogEldEvent{driverOriginatorUserId='00000000-0000-0000-0000-000000000000', encompassOriginatorUserId='null', unidentifiedUserId='null', eobrSerialNumber='null', logKey=null, eventSequenceIDNumber=6, eventRecordStatus=1, eventRecordOrigin=1, eventType=DutyStatusChange, eventCode=4, eventDateTime=2016-07-30T16:30:00Z, ruleSet=EnumBase{value=26}, accumulatedVehicleMiles=null, odometer=null, distance=null, engineHours=null, latitude=null, longitude=null, latitudeStatusCode='null', longitudeStatusCode='null', isGpsAtReducedPrecision=false, distanceSinceLastCoordinates=null, eldMalfunctionIndicatorStatus=false, driverDataDiagnosticEventIndicatorStatus=false, eventComment='null', driversLocationDescription='', eventDataCheck='null', diagnosticCode='null', geolocation='null', tractorNumber='8564', vehiclePlate='null', trailerNumber='null', trailerPlate='null', shipmentInfo='null', logRemark='null', originalEvent=true, endMoveDateTime=null, isEventDateTimeValidated=true, endOdometer=null, logRemarkDateTime=null, isManuallyEditedByKMBUser=false, gpsTimestamp=null, editDuration=null, motionPictureAuthorityId='null', motionPictureProductionId='null', encompassClusterPK=0, isUnidentifiedEvent=0}\n" +
                        "EmployeeLogEldEvent{driverOriginatorUserId='00000000-0000-0000-0000-000000000000', encompassOriginatorUserId='null', unidentifiedUserId='null', eobrSerialNumber='null', logKey=null, eventSequenceIDNumber=7, eventRecordStatus=1, eventRecordOrigin=1, eventType=DutyStatusChange, eventCode=4, eventDateTime=2016-07-30T18:30:00Z, ruleSet=EnumBase{value=26}, accumulatedVehicleMiles=null, odometer=null, distance=null, engineHours=null, latitude=null, longitude=null, latitudeStatusCode='null', longitudeStatusCode='null', isGpsAtReducedPrecision=false, distanceSinceLastCoordinates=null, eldMalfunctionIndicatorStatus=false, driverDataDiagnosticEventIndicatorStatus=false, eventComment='null', driversLocationDescription='', eventDataCheck='null', diagnosticCode='null', geolocation='null', tractorNumber='8564', vehiclePlate='null', trailerNumber='null', trailerPlate='null', shipmentInfo='null', logRemark='null', originalEvent=true, endMoveDateTime=null, isEventDateTimeValidated=true, endOdometer=null, logRemarkDateTime=null, isManuallyEditedByKMBUser=false, gpsTimestamp=null, editDuration=null, motionPictureAuthorityId='null', motionPictureProductionId='null', encompassClusterPK=0, isUnidentifiedEvent=0}\n" +
                        "EmployeeLogEldEvent{driverOriginatorUserId='00000000-0000-0000-0000-000000000000', encompassOriginatorUserId='null', unidentifiedUserId='null', eobrSerialNumber='null', logKey=null, eventSequenceIDNumber=8, eventRecordStatus=1, eventRecordOrigin=1, eventType=DutyStatusChange, eventCode=4, eventDateTime=2016-07-30T18:30:00Z, ruleSet=EnumBase{value=26}, accumulatedVehicleMiles=null, odometer=null, distance=null, engineHours=null, latitude=null, longitude=null, latitudeStatusCode='null', longitudeStatusCode='null', isGpsAtReducedPrecision=false, distanceSinceLastCoordinates=null, eldMalfunctionIndicatorStatus=false, driverDataDiagnosticEventIndicatorStatus=false, eventComment='null', driversLocationDescription='', eventDataCheck='null', diagnosticCode='null', geolocation='null', tractorNumber='8564', vehiclePlate='null', trailerNumber='null', trailerPlate='null', shipmentInfo='null', logRemark='null', originalEvent=true, endMoveDateTime=null, isEventDateTimeValidated=true, endOdometer=null, logRemarkDateTime=null, isManuallyEditedByKMBUser=false, gpsTimestamp=null, editDuration=null, motionPictureAuthorityId='null', motionPictureProductionId='null', encompassClusterPK=0, isUnidentifiedEvent=0}\n" +
                        "EmployeeLogEldEvent{driverOriginatorUserId='00000000-0000-0000-0000-000000000000', encompassOriginatorUserId='null', unidentifiedUserId='null', eobrSerialNumber='null', logKey=null, eventSequenceIDNumber=9, eventRecordStatus=1, eventRecordOrigin=1, eventType=DutyStatusChange, eventCode=4, eventDateTime=2016-07-30T19:00:00Z, ruleSet=EnumBase{value=26}, accumulatedVehicleMiles=null, odometer=null, distance=null, engineHours=null, latitude=null, longitude=null, latitudeStatusCode='null', longitudeStatusCode='null', isGpsAtReducedPrecision=false, distanceSinceLastCoordinates=null, eldMalfunctionIndicatorStatus=false, driverDataDiagnosticEventIndicatorStatus=false, eventComment='null', driversLocationDescription='', eventDataCheck='null', diagnosticCode='null', geolocation='null', tractorNumber='8564', vehiclePlate='null', trailerNumber='null', trailerPlate='null', shipmentInfo='null', logRemark='null', originalEvent=true, endMoveDateTime=null, isEventDateTimeValidated=true, endOdometer=null, logRemarkDateTime=null, isManuallyEditedByKMBUser=false, gpsTimestamp=null, editDuration=null, motionPictureAuthorityId='null', motionPictureProductionId='null', encompassClusterPK=0, isUnidentifiedEvent=0}\n" +
                        "EmployeeLogEldEvent{driverOriginatorUserId='00000000-0000-0000-0000-000000000000', encompassOriginatorUserId='null', unidentifiedUserId='null', eobrSerialNumber='null', logKey=null, eventSequenceIDNumber=10, eventRecordStatus=1, eventRecordOrigin=1, eventType=DutyStatusChange, eventCode=4, eventDateTime=2016-07-30T19:30:00Z, ruleSet=EnumBase{value=26}, accumulatedVehicleMiles=null, odometer=null, distance=null, engineHours=null, latitude=null, longitude=null, latitudeStatusCode='null', longitudeStatusCode='null', isGpsAtReducedPrecision=false, distanceSinceLastCoordinates=null, eldMalfunctionIndicatorStatus=false, driverDataDiagnosticEventIndicatorStatus=false, eventComment='null', driversLocationDescription='', eventDataCheck='null', diagnosticCode='null', geolocation='null', tractorNumber='8564', vehiclePlate='null', trailerNumber='null', trailerPlate='null', shipmentInfo='null', logRemark='null', originalEvent=true, endMoveDateTime=null, isEventDateTimeValidated=true, endOdometer=null, logRemarkDateTime=null, isManuallyEditedByKMBUser=false, gpsTimestamp=null, editDuration=null, motionPictureAuthorityId='null', motionPictureProductionId='null', encompassClusterPK=0, isUnidentifiedEvent=0}\n" +
                        "EmployeeLogEldEvent{driverOriginatorUserId='00000000-0000-0000-0000-000000000000', encompassOriginatorUserId='null', unidentifiedUserId='null', eobrSerialNumber='null', logKey=null, eventSequenceIDNumber=11, eventRecordStatus=1, eventRecordOrigin=1, eventType=DutyStatusChange, eventCode=4, eventDateTime=2016-07-30T20:00:00Z, ruleSet=EnumBase{value=26}, accumulatedVehicleMiles=null, odometer=null, distance=null, engineHours=null, latitude=null, longitude=null, latitudeStatusCode='null', longitudeStatusCode='null', isGpsAtReducedPrecision=false, distanceSinceLastCoordinates=null, eldMalfunctionIndicatorStatus=false, driverDataDiagnosticEventIndicatorStatus=false, eventComment='null', driversLocationDescription='', eventDataCheck='null', diagnosticCode='null', geolocation='null', tractorNumber='8564', vehiclePlate='null', trailerNumber='null', trailerPlate='null', shipmentInfo='null', logRemark='null', originalEvent=true, endMoveDateTime=null, isEventDateTimeValidated=true, endOdometer=null, logRemarkDateTime=null, isManuallyEditedByKMBUser=false, gpsTimestamp=null, editDuration=null, motionPictureAuthorityId='null', motionPictureProductionId='null', encompassClusterPK=0, isUnidentifiedEvent=0}\n" +
                        "EmployeeLogEldEvent{driverOriginatorUserId='00000000-0000-0000-0000-000000000000', encompassOriginatorUserId='null', unidentifiedUserId='null', eobrSerialNumber='null', logKey=null, eventSequenceIDNumber=12, eventRecordStatus=1, eventRecordOrigin=1, eventType=DutyStatusChange, eventCode=4, eventDateTime=2016-07-30T19:00:00Z, ruleSet=EnumBase{value=26}, accumulatedVehicleMiles=null, odometer=null, distance=null, engineHours=null, latitude=null, longitude=null, latitudeStatusCode='null', longitudeStatusCode='null', isGpsAtReducedPrecision=false, distanceSinceLastCoordinates=null, eldMalfunctionIndicatorStatus=false, driverDataDiagnosticEventIndicatorStatus=false, eventComment='null', driversLocationDescription='', eventDataCheck='null', diagnosticCode='null', geolocation='null', tractorNumber='8564', vehiclePlate='null', trailerNumber='null', trailerPlate='null', shipmentInfo='null', logRemark='null', originalEvent=true, endMoveDateTime=null, isEventDateTimeValidated=true, endOdometer=null, logRemarkDateTime=null, isManuallyEditedByKMBUser=false, gpsTimestamp=null, editDuration=null, motionPictureAuthorityId='null', motionPictureProductionId='null', encompassClusterPK=0, isUnidentifiedEvent=0}\n" +
                        "EmployeeLogEldEvent{driverOriginatorUserId='00000000-0000-0000-0000-000000000000', encompassOriginatorUserId='null', unidentifiedUserId='null', eobrSerialNumber='null', logKey=null, eventSequenceIDNumber=13, eventRecordStatus=1, eventRecordOrigin=1, eventType=DutyStatusChange, eventCode=4, eventDateTime=2016-07-30T19:30:00Z, ruleSet=EnumBase{value=26}, accumulatedVehicleMiles=null, odometer=null, distance=null, engineHours=null, latitude=null, longitude=null, latitudeStatusCode='null', longitudeStatusCode='null', isGpsAtReducedPrecision=false, distanceSinceLastCoordinates=null, eldMalfunctionIndicatorStatus=false, driverDataDiagnosticEventIndicatorStatus=false, eventComment='null', driversLocationDescription='', eventDataCheck='null', diagnosticCode='null', geolocation='null', tractorNumber='8564', vehiclePlate='null', trailerNumber='null', trailerPlate='null', shipmentInfo='null', logRemark='null', originalEvent=true, endMoveDateTime=null, isEventDateTimeValidated=true, endOdometer=null, logRemarkDateTime=null, isManuallyEditedByKMBUser=false, gpsTimestamp=null, editDuration=null, motionPictureAuthorityId='null', motionPictureProductionId='null', encompassClusterPK=0, isUnidentifiedEvent=0}\n" +
                        "EmployeeLogEldEvent{driverOriginatorUserId='00000000-0000-0000-0000-000000000000', encompassOriginatorUserId='null', unidentifiedUserId='null', eobrSerialNumber='null', logKey=null, eventSequenceIDNumber=14, eventRecordStatus=1, eventRecordOrigin=1, eventType=DutyStatusChange, eventCode=4, eventDateTime=2016-07-30T20:00:00Z, ruleSet=EnumBase{value=26}, accumulatedVehicleMiles=null, odometer=null, distance=null, engineHours=null, latitude=null, longitude=null, latitudeStatusCode='null', longitudeStatusCode='null', isGpsAtReducedPrecision=false, distanceSinceLastCoordinates=null, eldMalfunctionIndicatorStatus=false, driverDataDiagnosticEventIndicatorStatus=false, eventComment='null', driversLocationDescription='', eventDataCheck='null', diagnosticCode='null', geolocation='null', tractorNumber='8564', vehiclePlate='null', trailerNumber='null', trailerPlate='null', shipmentInfo='null', logRemark='null', originalEvent=true, endMoveDateTime=null, isEventDateTimeValidated=true, endOdometer=null, logRemarkDateTime=null, isManuallyEditedByKMBUser=false, gpsTimestamp=null, editDuration=null, motionPictureAuthorityId='null', motionPictureProductionId='null', encompassClusterPK=0, isUnidentifiedEvent=0}\n" +
                        "EmployeeLogEldEvent{driverOriginatorUserId='00000000-0000-0000-0000-000000000000', encompassOriginatorUserId='null', unidentifiedUserId='null', eobrSerialNumber='null', logKey=null, eventSequenceIDNumber=0, eventRecordStatus=3, eventRecordOrigin=2, eventType=DutyStatusChange, eventCode=4, eventDateTime=2016-07-30T21:30:15Z, ruleSet=EnumBase{value=26}, accumulatedVehicleMiles=null, odometer=null, distance=null, engineHours=null, latitude=null, longitude=null, latitudeStatusCode='null', longitudeStatusCode='null', isGpsAtReducedPrecision=false, distanceSinceLastCoordinates=null, eldMalfunctionIndicatorStatus=false, driverDataDiagnosticEventIndicatorStatus=false, eventComment='null', driversLocationDescription='', eventDataCheck='null', diagnosticCode='null', geolocation='null', tractorNumber='8564', vehiclePlate='null', trailerNumber='null', trailerPlate='null', shipmentInfo='null', logRemark='null', originalEvent=true, endMoveDateTime=2016-07-30T21:30:15Z, isEventDateTimeValidated=true, endOdometer=null, logRemarkDateTime=null, isManuallyEditedByKMBUser=true, gpsTimestamp=null, editDuration=30584000, motionPictureAuthorityId='null', motionPictureProductionId='null', encompassClusterPK=0, isUnidentifiedEvent=0}\n" +
                        "EmployeeLogEldEvent{driverOriginatorUserId='00000000-0000-0000-0000-000000000000', encompassOriginatorUserId='null', unidentifiedUserId='null', eobrSerialNumber='null', logKey=null, eventSequenceIDNumber=0, eventRecordStatus=3, eventRecordOrigin=2, eventType=DutyStatusChange, eventCode=4, eventDateTime=2016-07-30T21:30:15Z, ruleSet=EnumBase{value=26}, accumulatedVehicleMiles=null, odometer=null, distance=null, engineHours=null, latitude=null, longitude=null, latitudeStatusCode='null', longitudeStatusCode='null', isGpsAtReducedPrecision=false, distanceSinceLastCoordinates=null, eldMalfunctionIndicatorStatus=false, driverDataDiagnosticEventIndicatorStatus=false, eventComment='null', driversLocationDescription='', eventDataCheck='null', diagnosticCode='null', geolocation='null', tractorNumber='8564', vehiclePlate='null', trailerNumber='null', trailerPlate='null', shipmentInfo='null', logRemark='null', originalEvent=true, endMoveDateTime=2016-07-30T21:30:15Z, isEventDateTimeValidated=true, endOdometer=null, logRemarkDateTime=null, isManuallyEditedByKMBUser=true, gpsTimestamp=null, editDuration=30584000, motionPictureAuthorityId='null', motionPictureProductionId='null', encompassClusterPK=0, isUnidentifiedEvent=0}\n";
        String actual = outBuilder.toString();

        //TODO: This is too fragile
        // Assert.assertEquals(expected, actual);
    }

    @Test
    public void testUpdateMissingDataDiagnostic() throws Throwable {

        TimeKeeper.setTimeKeeper(new TestableTimeKeeper(new DateTime(2016, 7, 30, 11, 10)));
        EmployeeLogEldMandateController mandateController = ControllerFactory.getInstance().getEmployeeLogEldMandateController();

        mandateController = Mockito.spy(mandateController);

        //Setup the log and events to use.
        EmployeeLogEldEvent baseEvent = createEmployeeLogEldEvent(1L, "2016-07-30T00:00:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EmployeeLogEldEventCode.DutyStatus_Driving, 1, 1, "8564", new RuleSetTypeEnum(RuleSetTypeEnum.US70HOUR));
        baseEvent.setEobrSerialNumber("hhh");
        baseEvent.setTractorNumber("Tractor Good");
        baseEvent.setTrailerNumber("Trailer Good");
        baseEvent.setShipmentInfo("Super awesome shipment");

        StatusRecord baseStatusRecord = new StatusRecord();
        baseStatusRecord.setTachometer(1000.0f);
        baseStatusRecord.setIsEngineOn(true);

        StatusRecord badStatusRecord = new StatusRecord();
        badStatusRecord.setGpsUncertDistance(10000f);
        badStatusRecord.setIsEngineOn(true);
        badStatusRecord.setTachometer(1000.0f);

        //These are adjusted for time.
        Mockito.when(mandateController.getStatusRecord(new DateTime(2016, 7, 29, 19, 0, 0).toDate())).thenReturn(baseStatusRecord);
        Mockito.when(mandateController.getStatusRecord(new DateTime(2016, 7, 30, 6, 30, 0).toDate())).thenReturn(badStatusRecord);

        EmployeeLogEldEvent badEvent = createEmployeeLogEldEvent(3L, "2016-07-30T06:30:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EmployeeLogEldEventCode.DutyStatus_OnDuty, 1, 2, "8564", new RuleSetTypeEnum(RuleSetTypeEnum.US70HOUR));
        badEvent.setLatitudeStatusCode("M");
        badEvent.setLongitudeStatusCode("M");
        badEvent.setDriversLocationDescription("");
        badEvent.setDistanceSinceLastCoordinates(6.0f);
        badEvent.setRequiresManualLocation(true);
        badEvent.setLogKey(3);
        badEvent.setEobrSerialNumber("hhh");
        badEvent.setTractorNumber("ee");
        badEvent.setShipmentInfo("Huzzah");
        badEvent.setTrailerNumber("aoeu");

        EventDataDiagnosticsChecker eventDataDiagnosticsChecker = new EventDataDiagnosticsChecker();
        Assert.assertNotNull(eventDataDiagnosticsChecker.CheckForError(badEvent, false, badStatusRecord));

        EmployeeLogEldEvent[] eventArray = new EmployeeLogEldEvent[2];
        eventArray[0] = baseEvent;
        eventArray[1] = badEvent;

        EmployeeLogEldEventList eldEventList = new EmployeeLogEldEventList();
        eldEventList.setEldEventList(eventArray);


        EmployeeLog employeeLog = EmployeeLogUtilities.CreateNewLog(app, app.getCurrentUser(), new DateTime(2016, 7, 30, 1, 1).toDate(), baseEvent);
        employeeLog.setEldEventList(eldEventList);

        EmployeeLogFacade facade = new EmployeeLogFacade(app, app.getCurrentUser());
        facade.Save(employeeLog, 1);

        //Create the initial data diagnostic event and verify.
        mandateController.CreateDataDiagnosticEvent(employeeLog, new DateTime(2016, 7, 30, 6, 39).toDate(), DataDiagnosticEnum.MISSING_REQUIRED_DATA_ELEMENTS, badEvent.getPrimaryKey());
        Assert.assertTrue(mandateController.IsDataDiagnosticActive(DataDiagnosticEnum.MISSING_REQUIRED_DATA_ELEMENTS));

        mandateController.updateMissingDataDiagnosticForEvent(employeeLog, baseEvent);
        TimeKeeper.setTimeKeeper(new TestableTimeKeeper(new DateTime(2016, 7, 30, 11, 15)));
        Assert.assertTrue(mandateController.IsDataDiagnosticActive(DataDiagnosticEnum.MISSING_REQUIRED_DATA_ELEMENTS));


        //Verify the badEvent is indeed bad.
        Assert.assertNotNull(eventDataDiagnosticsChecker.CheckForError(badEvent, false, null));
        mandateController.updateMissingDataDiagnosticForEvent(employeeLog, baseEvent);
        TimeKeeper.setTimeKeeper(new TestableTimeKeeper(new DateTime(2016, 7, 30, 11, 16)));
        Assert.assertTrue(mandateController.IsDataDiagnosticActive(DataDiagnosticEnum.MISSING_REQUIRED_DATA_ELEMENTS));

        //Fix the bad event and save it back.
        badEvent.setDriversLocationDescription("Neenah");
        Assert.assertNull(eventDataDiagnosticsChecker.CheckForError(badEvent, false, null));

        eventArray[1] = badEvent;
        eldEventList = new EmployeeLogEldEventList();
        eldEventList.setEldEventList(eventArray);
        employeeLog.setEldEventList(eldEventList);

        //Make sure if we send in the event that did not trigger the diagnostic event, it will not clear it.
        mandateController.updateMissingDataDiagnosticForEvent(employeeLog, baseEvent);
        TimeKeeper.setTimeKeeper(new TestableTimeKeeper(new DateTime(2016, 7, 30, 11, 19)));
        Assert.assertTrue(mandateController.IsDataDiagnosticActive(DataDiagnosticEnum.MISSING_REQUIRED_DATA_ELEMENTS));

        //If we send in the fixed bad event and it was the bad event that originally generated the missing data issue, it will clear.
        mandateController.updateMissingDataDiagnosticForEvent(employeeLog, badEvent);
        TimeKeeper.setTimeKeeper(new TestableTimeKeeper(new DateTime(2016, 7, 30, 11, 20)));
        Assert.assertFalse(mandateController.IsDataDiagnosticActive(DataDiagnosticEnum.MISSING_REQUIRED_DATA_ELEMENTS));
    }

    @Test
    public void testGetActiveDataDiagnostics() throws Throwable {
        EmployeeLogEldMandateController mandateController = ControllerFactory.getInstance().getEmployeeLogEldMandateController();

        EmployeeLogEldEvent baseEvent = createEmployeeLogEldEvent(1L, "2016-07-30T00:00:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EmployeeLogEldEventCode.DutyStatus_Driving, 1, 1, "8564", new RuleSetTypeEnum(RuleSetTypeEnum.US70HOUR));
        baseEvent.setEobrSerialNumber("hhh");
        baseEvent.setTractorNumber("Tractor Good");
        baseEvent.setTrailerNumber("Trailer Good");
        baseEvent.setShipmentInfo("Super awesome shipment");
        EmployeeLog employeeLog = EmployeeLogUtilities.CreateNewLog(app, app.getCurrentUser(), new DateTime(2016, 7, 30, 1,1).toDate(), baseEvent);

        EmployeeLogFacade facade = new EmployeeLogFacade(app, app.getCurrentUser());
        facade.Save(employeeLog, 1);

        Assert.assertTrue(mandateController.getActiveDataDiagnostics(employeeLog).isEmpty());


        mandateController.CreateDataDiagnosticEvent(employeeLog, new DateTime(2016, 7, 30, 1, 4).toDate(), DataDiagnosticEnum.MISSING_REQUIRED_DATA_ELEMENTS, 10);
        Assert.assertTrue(mandateController.getActiveDataDiagnostics(employeeLog).contains(DataDiagnosticEnum.MISSING_REQUIRED_DATA_ELEMENTS));

        mandateController.CreateDataDiagnosticClearedEvent(employeeLog, new DateTime(2016, 7, 30, 1,7).toDate(), DataDiagnosticEnum.MISSING_REQUIRED_DATA_ELEMENTS, 10);
        Assert.assertTrue(!mandateController.getActiveDataDiagnostics(employeeLog).contains(DataDiagnosticEnum.MISSING_REQUIRED_DATA_ELEMENTS));
        Assert.assertTrue(mandateController.getActiveDataDiagnostics(employeeLog).isEmpty());

        tearDown();
    }

    @Test
    public void testClearingOfDiagnosticEvent() throws Throwable{
        Date todaysDate = DateUtility.getCurrentDateTimeWithSecondsUTC();
        String todaysDateString = dateToStringConverter(todaysDate);

        EmployeeLogEldEvent baseEvent = createEmployeeLogEldEvent(1L, todaysDateString, Enums.EmployeeLogEldEventType.DutyStatusChange, EmployeeLogEldEventCode.DutyStatus_Driving, 1, 1, "Tractor number", new RuleSetTypeEnum(RuleSetTypeEnum.US70HOUR));
        baseEvent.setEobrSerialNumber("007");
        baseEvent.setTractorNumber("Tractor number");
        baseEvent.setTrailerNumber("Trail number");
        baseEvent.setShipmentInfo("Ship Info");

        EmployeeLogEldEvent diagnosticLogged = createEmployeeLogEldEvent(2L, todaysDateString, Enums.EmployeeLogEldEventType.Malfunction_DataDiagnosticDetection, EmployeeLogEldEventCode.EldDataDiagnosticLogged, 1, 1,"Tractor number", new RuleSetTypeEnum(RuleSetTypeEnum.US70HOUR));
        diagnosticLogged.setEobrSerialNumber("007");
        diagnosticLogged.setTractorNumber("Tractor number");
        diagnosticLogged.setTrailerNumber("Trail number");
        diagnosticLogged.setShipmentInfo("Ship Info");
        diagnosticLogged.setDiagnosticCode(DataDiagnosticEnum.DATA_TRANSFER.toDMOEnum());

        EmployeeLogEldEvent[] eventArray = new EmployeeLogEldEvent[2];
        eventArray[0] = baseEvent;
        eventArray[1] = diagnosticLogged;

        EmployeeLogEldEventList eldEventList = new EmployeeLogEldEventList();
        eldEventList.setEldEventList(eventArray);

        EmployeeLog employeeLog = EmployeeLogUtilities.CreateNewLog(app, app.getCurrentUser(), todaysDate, baseEvent);
        employeeLog.setEldEventList(eldEventList);

        EmployeeLogFacade facade = new EmployeeLogFacade(app, app.getCurrentUser());
        facade.Save(employeeLog, 1);

        EmployeeLogEldMandateController mandateController = new EmployeeLogEldMandateController(app);
        app.setCurrentEmployeeLog(employeeLog);

        Assert.assertTrue(mandateController.getActiveDataDiagnostics(employeeLog).contains(DataDiagnosticEnum.DATA_TRANSFER));

        mandateController.HandleDataDiagnosticEventCreation(true, 0, 3, mandateController);

        Assert.assertTrue(!mandateController.getActiveDataDiagnostics(employeeLog).contains(DataDiagnosticEnum.DATA_TRANSFER));
        Assert.assertEquals(facade.FetchMostRecentLogEventForUser().getEventType(), Enums.EmployeeLogEldEventType.Malfunction_DataDiagnosticDetection);
        Assert.assertEquals(facade.FetchMostRecentLogEventForUser().getEventCode(), EmployeeLogEldEventCode.EldDataDiagnosticCleared);

        tearDown();

    }

    @Test
    public void testClearingOfDiagnosticAndMalfunctionEvents() throws Throwable{
        Date todaysDate = DateUtility.getCurrentDateTimeWithSecondsUTC();
        String todaysDateString = dateToStringConverter(todaysDate);

        EmployeeLogEldEvent baseEvent = createEmployeeLogEldEvent(1L, todaysDateString, Enums.EmployeeLogEldEventType.DutyStatusChange, EmployeeLogEldEventCode.DutyStatus_Driving, 1, 1, "Tractor number", new RuleSetTypeEnum(RuleSetTypeEnum.US70HOUR));
        baseEvent.setEobrSerialNumber("007");
        baseEvent.setTractorNumber("Tractor number");
        baseEvent.setTrailerNumber("Trail number");
        baseEvent.setShipmentInfo("Ship Info");


        EmployeeLogEldEvent diagnosticLogged = createEmployeeLogEldEvent(2L, todaysDateString, Enums.EmployeeLogEldEventType.Malfunction_DataDiagnosticDetection, EmployeeLogEldEventCode.EldDataDiagnosticLogged, 1, 1,"Tractor number", new RuleSetTypeEnum(RuleSetTypeEnum.US70HOUR));
        diagnosticLogged.setEobrSerialNumber("007");
        diagnosticLogged.setTractorNumber("Tractor number");
        diagnosticLogged.setTrailerNumber("Trail number");
        diagnosticLogged.setShipmentInfo("Ship Info");
        diagnosticLogged.setDiagnosticCode(DataDiagnosticEnum.DATA_TRANSFER.toDMOEnum());

        EmployeeLogEldEvent malfunctionLogged = createEmployeeLogEldEvent( 3L, todaysDateString, Enums.EmployeeLogEldEventType.Malfunction_DataDiagnosticDetection, EmployeeLogEldEventCode.EldMalfunctionLogged, 1, 1, " Tracto Number", new RuleSetTypeEnum(RuleSetTypeEnum.US70HOUR));
        malfunctionLogged.setEobrSerialNumber("007");
        malfunctionLogged.setTractorNumber("Tractor number");
        malfunctionLogged.setTrailerNumber("Trail number");
        malfunctionLogged.setShipmentInfo("Ship Info");
        malfunctionLogged.setDiagnosticCode(Malfunction.DATA_TRANSFER_COMPLIANCE.getDmoValue());


        EmployeeLogEldEvent[] eventArray = new EmployeeLogEldEvent[3];
        eventArray[0] = baseEvent;
        eventArray[1] = diagnosticLogged;
        eventArray[2] = malfunctionLogged;

        EmployeeLogEldEventList eldEventList = new EmployeeLogEldEventList();
        eldEventList.setEldEventList(eventArray);

        EmployeeLog employeeLog = EmployeeLogUtilities.CreateNewLog(app, app.getCurrentUser(), todaysDate, baseEvent);
        employeeLog.setEldEventList(eldEventList);

        EmployeeLogFacade facade = new EmployeeLogFacade(app, app.getCurrentUser());
        facade.Save(employeeLog, 1);

        EmployeeLogEldMandateController mandateController = new EmployeeLogEldMandateController(app);
        app.setCurrentEmployeeLog(employeeLog);


        Assert.assertTrue(!mandateController.getActiveMalfunctions(employeeLog).isEmpty());

        Assert.assertTrue(mandateController.getActiveDataDiagnostics(employeeLog).contains(DataDiagnosticEnum.DATA_TRANSFER));
        Assert.assertTrue(!mandateController.getActiveMalfunctions(employeeLog).isEmpty());

        mandateController.HandleDataDiagnosticEventCreation(true, 0, 4, mandateController);

        Assert.assertTrue(!mandateController.getActiveDataDiagnostics(employeeLog).contains(DataDiagnosticEnum.DATA_TRANSFER));
        Assert.assertTrue(mandateController.getActiveMalfunctions(employeeLog).isEmpty());

        tearDown();
    }

    @Test
    public void testCreationOfDataDiagnosticAndMalfEventsAfterEncompassTransferMechanismFailed() throws Throwable{
        Date todaysDate = DateUtility.getCurrentDateTimeWithSecondsUTC();
        String todaysDateString = dateToStringConverter(todaysDate);

        EmployeeLogEldEvent baseEvent = createEmployeeLogEldEvent(1L, todaysDateString, Enums.EmployeeLogEldEventType.DutyStatusChange, EmployeeLogEldEventCode.DutyStatus_Driving, 1, 1, "Tractor number", new RuleSetTypeEnum(RuleSetTypeEnum.US70HOUR));
        baseEvent.setEobrSerialNumber("007");
        baseEvent.setTractorNumber("Tractor number");
        baseEvent.setTrailerNumber("Trail number");
        baseEvent.setShipmentInfo("Ship Info");


        EmployeeLogEldEvent[] eventArray = new EmployeeLogEldEvent[3];
        eventArray[0] = baseEvent;

        EmployeeLogEldEventList eldEventList = new EmployeeLogEldEventList();
        eldEventList.setEldEventList(eventArray);

        EmployeeLog employeeLog = EmployeeLogUtilities.CreateNewLog(app, app.getCurrentUser(), todaysDate, baseEvent);
        employeeLog.setEldEventList(eldEventList);

        EmployeeLogFacade facade = new EmployeeLogFacade(app, app.getCurrentUser());
        facade.Save(employeeLog, 1);

        EmployeeLogEldMandateController mandateController = new EmployeeLogEldMandateController(app);
        app.setCurrentEmployeeLog(employeeLog);

        Assert.assertTrue(!mandateController.getActiveDataDiagnostics(employeeLog).contains(DataDiagnosticEnum.DATA_TRANSFER));

        mandateController.HandleDataDiagnosticEventCreation(false, 1, 0, mandateController);

        Assert.assertTrue(mandateController.getActiveDataDiagnostics(employeeLog).contains(DataDiagnosticEnum.DATA_TRANSFER));
        Assert.assertTrue(mandateController.getActiveMalfunctions(employeeLog).isEmpty());

        mandateController.HandleDataDiagnosticEventCreation(false, 3, 0, mandateController);
        Assert.assertTrue(!mandateController.getActiveMalfunctions(employeeLog).isEmpty());

        Assert.assertEquals(mandateController.getActiveMalfunctions(employeeLog).size(), 1);
        Assert.assertEquals(mandateController.getActiveDataDiagnostics(employeeLog).size(), 1);
        //check to see if we will create another diagnostic event

        mandateController.HandleDataDiagnosticEventCreation(false, 4, 0, mandateController);


        //Verifying that we only create one malfunction and one diagnostic event
        Assert.assertEquals(mandateController.getActiveMalfunctions(employeeLog).size(), 1);
        Assert.assertEquals(mandateController.getActiveDataDiagnostics(employeeLog).size(), 1);

    }


    private String dateToStringConverter(Date date){
        SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        return simpleFormat.format(date);
    }



    /**
     * HELPER METHODS
     */
    private int id = 0;

    private EmployeeLogEldEvent createEmployeeLogEldEvent(long primaryKey, String eventDateTime, Enums.EmployeeLogEldEventType eventType, int eventCode, Integer eventRecordStatus, Integer eventRecordOrigin, String tractorNumber, RuleSetTypeEnum ruleSetTypeEnum, long encompassPK, long encompassRelatedEvent) {
        return createEmployeeLogEldEvent(primaryKey, eventDateTime, eventType, eventCode, eventRecordStatus, eventRecordOrigin, tractorNumber, ruleSetTypeEnum, TimeZone.getTimeZone("UTC"), encompassPK, encompassRelatedEvent);
    }

    private EmployeeLogEldEvent createEmployeeLogEldEvent(long primaryKey, String eventDateTime, Enums.EmployeeLogEldEventType eventType, int eventCode, Integer eventRecordStatus, Integer eventRecordOrigin, String tractorNumber, RuleSetTypeEnum ruleSetTypeEnum) {
        return createEmployeeLogEldEvent(primaryKey, eventDateTime, eventType, eventCode, eventRecordStatus, eventRecordOrigin, tractorNumber, ruleSetTypeEnum, TimeZone.getTimeZone("UTC"), 000000L, 9999L);
    }

    private EmployeeLogEldEvent createEmployeeLogEldEvent(long primaryKey, String eventDateTime, Enums.EmployeeLogEldEventType eventType, int eventCode, Integer eventRecordStatus, Integer eventRecordOrigin, String tractorNumber, RuleSetTypeEnum ruleSetTypeEnum, TimeZone tz, long encompassPK, long encompassRelatedEvent) {
        //	EventType
        //	1 = Duty Status Change
        //	3 = Change in driver’s indication of PC or YM

        //	EventCode for Duty Status Changes
        //	1 = Off Duty
        //	2 = Sleeper
        //	3 = Driving
        //	4 = On Duty

        // EventRecordStatus
        // 1 = Active
        // 2 = Inactive – Changed
        // 3 = Inactive – Change Requested
        // 4 = Inactive – Change Rejected

        Date startDateTime = null;

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        format.setTimeZone(tz);
        try {
            startDateTime = format.parse(eventDateTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }


        EmployeeLogEldEvent eldEvent = new EmployeeLogEldEvent(startDateTime);
        eldEvent.setEventSequenceIDNumber(id++);
        eldEvent.setPrimaryKey(primaryKey);
        eldEvent.setEventType(eventType);
        eldEvent.setEventCode(eventCode);
        eldEvent.setEventRecordStatus(eventRecordStatus);
        eldEvent.setEventRecordOrigin(eventRecordOrigin);
        eldEvent.setTractorNumber(tractorNumber);
        eldEvent.setRuleSet(ruleSetTypeEnum);
        eldEvent.setEncompassClusterPK(encompassPK);
        eldEvent.setRelatedEncompassClusterPK(encompassRelatedEvent);

        return eldEvent;
    }

    @Test
    public void testReverseRelatedEventMapping() {
        AOBRDControllerBase base = new EmployeeLogEldMandateController(app);
        EmployeeLogEldEvent[] inputEvents = new EmployeeLogEldEvent[6];
        inputEvents[0] = createEmployeeLogEldEvent(1L, "2016-07-30T00:00:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EmployeeLogEldEventCode.DutyStatus_Driving, 1, 1, "8564", new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), 111L, 333L);
        inputEvents[1] = createEmployeeLogEldEvent(2L, "2016-07-30T05:00:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EmployeeLogEldEventCode.DutyStatus_Driving, 1, 1, "8564", new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), 333L, 111L);
        inputEvents[2] = createEmployeeLogEldEvent(3L, "2016-07-30T06:30:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EmployeeLogEldEventCode.DutyStatus_OnDuty, 1, 2, "8564", new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), 44L, 223L);
        inputEvents[3] = createEmployeeLogEldEvent(4L, "2016-07-30T16:00:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EmployeeLogEldEventCode.DutyStatus_OnDuty, 1, 1, "8564", new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), 223L, 44L);
        inputEvents[4] = createEmployeeLogEldEvent(5L, "2016-07-30T11:00:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EmployeeLogEldEventCode.DutyStatus_OnDuty, 1, 1, "8564", new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), 66L, 22L);
        inputEvents[5] = createEmployeeLogEldEvent(6L, "2016-07-30T12:30:00Z", Enums.EmployeeLogEldEventType.DutyStatusChange, EmployeeLogEldEventCode.DutyStatus_OnDuty, 1, 1, "8564", new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR), 22L, 66L);

        base.reverseMapRelateEvents(inputEvents, inputEvents);
        Assert.assertEquals(inputEvents[0].getPrimaryKey(), inputEvents[1].getRelatedKmbPK());
        Assert.assertEquals(inputEvents[1].getPrimaryKey(), inputEvents[0].getRelatedKmbPK());
        Assert.assertEquals(inputEvents[2].getPrimaryKey(), inputEvents[3].getRelatedKmbPK());
        Assert.assertEquals(inputEvents[3].getPrimaryKey(), inputEvents[2].getRelatedKmbPK());
        Assert.assertEquals(inputEvents[4].getPrimaryKey(), inputEvents[5].getRelatedKmbPK());
        Assert.assertEquals(inputEvents[5].getPrimaryKey(), inputEvents[4].getRelatedKmbPK());

        for (EmployeeLogEldEvent event : inputEvents) {
            Assert.assertNotEquals(event.getRelatedKmbPK(), 0);
        }
    }

    @Test
    public void testIsAddUnclaimedDrivingDurationDataDiagnostic_NoDiagnosticNeeded() {

        int previousLogDayCount = 7;
        Date currentLogStartTime = DateTime.parse("2017-07-13T00:00:00.000+01:00").toDate();
        Date firstUnclaimedLogStartTime = DateTime.parse("2017-07-10T00:01:00.000+01:00").toDate();

        ArrayList<UnclaimedEventDTO> events = new ArrayList<>();

        // skipping these because of the first unclaimed log start time
        events.add(new UnclaimedEventDTO(3, DateTime.parse("2017-07-09T02:00:00.000+01:00").toDate()));
        events.add(new UnclaimedEventDTO(4, DateTime.parse("2017-07-09T02:31:00.000+01:00").toDate()));

        // these will be accumulated, but none gets over 30 minutes
        events.add(new UnclaimedEventDTO(3, DateTime.parse("2017-07-10T01:00:00.000+01:00").toDate()));
        events.add(new UnclaimedEventDTO(4, DateTime.parse("2017-07-10T01:15:00.000+01:00").toDate()));
        events.add(new UnclaimedEventDTO(3, DateTime.parse("2017-07-10T02:00:00.000+01:00").toDate()));
        events.add(new UnclaimedEventDTO(4, DateTime.parse("2017-07-10T02:14:00.000+01:00").toDate()));

        events.add(new UnclaimedEventDTO(3, DateTime.parse("2017-07-11T01:00:00.000+01:00").toDate()));
        events.add(new UnclaimedEventDTO(4, DateTime.parse("2017-07-11T01:15:00.000+01:00").toDate()));
        events.add(new UnclaimedEventDTO(3, DateTime.parse("2017-07-11T02:00:00.000+01:00").toDate()));
        events.add(new UnclaimedEventDTO(4, DateTime.parse("2017-07-11T02:14:00.000+01:00").toDate()));

        EmployeeLogEldMandateController mandateController = ControllerFactory.getInstance().getEmployeeLogEldMandateController();
        Boolean response = mandateController.IsAddUnclaimedDrivingDurationDataDiagnostic(currentLogStartTime, firstUnclaimedLogStartTime, previousLogDayCount, events.toArray(new UnclaimedEventDTO[events.size()]));

        Assert.assertFalse(response);
    }

    @Test
    public void testIsAddUnclaimedDrivingDurationDataDiagnostic_2amLogStart_NoDiagnosticNeeded() {

        int previousLogDayCount = 7;
        Date currentLogStartTime = DateTime.parse("2017-07-13T02:00:00.000+01:00").toDate();
        Date firstUnclaimedLogStartTime = DateTime.parse("2017-07-10T02:00:00.000+01:00").toDate();

        ArrayList<UnclaimedEventDTO> events = new ArrayList<>();

        // skipping these because of the first unclaimed log start time
        events.add(new UnclaimedEventDTO(3, DateTime.parse("2017-07-09T02:00:00.000+01:00").toDate()));
        events.add(new UnclaimedEventDTO(4, DateTime.parse("2017-07-09T02:31:00.000+01:00").toDate()));
        events.add(new UnclaimedEventDTO(3, DateTime.parse("2017-07-10T01:00:00.000+01:00").toDate()));
        events.add(new UnclaimedEventDTO(4, DateTime.parse("2017-07-10T01:15:00.000+01:00").toDate()));

        // these will be accumulated, but none gets over 30 minutes
        events.add(new UnclaimedEventDTO(3, DateTime.parse("2017-07-10T02:00:00.000+01:00").toDate()));
        events.add(new UnclaimedEventDTO(4, DateTime.parse("2017-07-10T02:15:00.000+01:00").toDate()));
        events.add(new UnclaimedEventDTO(3, DateTime.parse("2017-07-10T03:00:00.000+01:00").toDate()));
        events.add(new UnclaimedEventDTO(4, DateTime.parse("2017-07-10T03:14:00.000+01:00").toDate()));

        events.add(new UnclaimedEventDTO(3, DateTime.parse("2017-07-11T02:00:00.000+01:00").toDate()));
        events.add(new UnclaimedEventDTO(4, DateTime.parse("2017-07-11T02:15:00.000+01:00").toDate()));
        events.add(new UnclaimedEventDTO(3, DateTime.parse("2017-07-11T03:00:00.000+01:00").toDate()));
        events.add(new UnclaimedEventDTO(4, DateTime.parse("2017-07-11T03:14:00.000+01:00").toDate()));

        EmployeeLogEldMandateController mandateController = ControllerFactory.getInstance().getEmployeeLogEldMandateController();
        Boolean response = mandateController.IsAddUnclaimedDrivingDurationDataDiagnostic(currentLogStartTime, firstUnclaimedLogStartTime, previousLogDayCount, events.toArray(new UnclaimedEventDTO[4]));

        Assert.assertFalse(response);
    }

    @Test
    public void testIsAddUnclaimedDrivingDurationDataDiagnostic_2amLogStart_DrivingOverStart_NoDiagnosticNeeded() {

        int previousLogDayCount = 7;
        Date currentLogStartTime = DateTime.parse("2017-07-13T02:00:00.000+01:00").toDate();
        Date firstUnclaimedLogStartTime = DateTime.parse("2017-07-10T02:14:00.000+01:00").toDate();

        ArrayList<UnclaimedEventDTO> events = new ArrayList<>();

        // skipping these because of the first unclaimed log start time
        events.add(new UnclaimedEventDTO(3, DateTime.parse("2017-07-09T01:45:00.000+01:00").toDate()));
        events.add(new UnclaimedEventDTO(4, DateTime.parse("2017-07-09T02:31:00.000+01:00").toDate()));
        events.add(new UnclaimedEventDTO(3, DateTime.parse("2017-07-10T01:00:00.000+01:00").toDate()));
        events.add(new UnclaimedEventDTO(4, DateTime.parse("2017-07-10T01:15:00.000+01:00").toDate()));

        // these will be accumulated, but none gets over 30 minutes
        events.add(new UnclaimedEventDTO(3, DateTime.parse("2017-07-10T01:50:00.000+01:00").toDate()));
        events.add(new UnclaimedEventDTO(4, DateTime.parse("2017-07-10T02:14:00.000+01:00").toDate()));

        events.add(new UnclaimedEventDTO(3, DateTime.parse("2017-07-11T01:55:00.000+01:00").toDate()));
        events.add(new UnclaimedEventDTO(4, DateTime.parse("2017-07-11T02:05:00.000+01:00").toDate()));
        events.add(new UnclaimedEventDTO(3, DateTime.parse("2017-07-11T03:00:00.000+01:00").toDate()));
        events.add(new UnclaimedEventDTO(4, DateTime.parse("2017-07-11T03:25:00.000+01:00").toDate()));

        EmployeeLogEldMandateController mandateController = ControllerFactory.getInstance().getEmployeeLogEldMandateController();
        Boolean response = mandateController.IsAddUnclaimedDrivingDurationDataDiagnostic(currentLogStartTime, firstUnclaimedLogStartTime, previousLogDayCount, events.toArray(new UnclaimedEventDTO[4]));

        Assert.assertFalse(response);
    }

    @Test
    public void testIsAddUnclaimedDrivingDurationDataDiagnostic_2amLogStart_DrivingOverStart_NoInitialDrive_NoDiagnosticNeeded() {

        int previousLogDayCount = 7;
        Date currentLogStartTime = DateTime.parse("2017-07-13T02:00:00.000+01:00").toDate();
        Date firstUnclaimedLogStartTime = DateTime.parse("2017-07-11T02:05:00.000+01:00").toDate();

        ArrayList<UnclaimedEventDTO> events = new ArrayList<>();

        // these will be accumulated, but none gets over 30 minutes
        events.add(new UnclaimedEventDTO(4, DateTime.parse("2017-07-10T02:14:00.000+01:00").toDate()));

        events.add(new UnclaimedEventDTO(3, DateTime.parse("2017-07-11T01:55:00.000+01:00").toDate()));
        events.add(new UnclaimedEventDTO(4, DateTime.parse("2017-07-11T02:05:00.000+01:00").toDate()));
        events.add(new UnclaimedEventDTO(3, DateTime.parse("2017-07-11T03:00:00.000+01:00").toDate()));
        events.add(new UnclaimedEventDTO(4, DateTime.parse("2017-07-11T03:25:00.000+01:00").toDate()));

        EmployeeLogEldMandateController mandateController = ControllerFactory.getInstance().getEmployeeLogEldMandateController();
        Boolean response = mandateController.IsAddUnclaimedDrivingDurationDataDiagnostic(currentLogStartTime, firstUnclaimedLogStartTime, previousLogDayCount, events.toArray(new UnclaimedEventDTO[4]));

        Assert.assertFalse(response);
    }

    @Test
    public void testIsAddUnclaimedDrivingDurationDataDiagnostic_DiagnosticNeeded() {

        int previousLogDayCount = 7;
        Date currentLogStartTime = DateTime.parse("2017-07-13T00:00:00.000+01:00").toDate();
        Date firstUnclaimedLogStartTime = DateTime.parse("2017-07-10T01:00:00.000+01:00").toDate();

        ArrayList<UnclaimedEventDTO> events = new ArrayList<>();
        events.add(new UnclaimedEventDTO(3, DateTime.parse("2017-07-10T01:00:00.000+01:00").toDate()));
        events.add(new UnclaimedEventDTO(4, DateTime.parse("2017-07-10T01:15:00.000+01:00").toDate()));
        events.add(new UnclaimedEventDTO(3, DateTime.parse("2017-07-10T02:00:00.000+01:00").toDate()));
        events.add(new UnclaimedEventDTO(4, DateTime.parse("2017-07-10T02:16:00.000+01:00").toDate()));

        EmployeeLogEldMandateController mandateController = ControllerFactory.getInstance().getEmployeeLogEldMandateController();
        Boolean response = mandateController.IsAddUnclaimedDrivingDurationDataDiagnostic(currentLogStartTime, firstUnclaimedLogStartTime, previousLogDayCount, events.toArray(new UnclaimedEventDTO[4]));

        Assert.assertTrue(response);
    }

    @Test
    public void testIsAddUnclaimedDrivingDurationDataDiagnostic_3amLogStart_DiagnosticNeeded() {

        int previousLogDayCount = 7;
        Date currentLogStartTime = DateTime.parse("2017-07-13T03:00:00.000+01:00").toDate();
        Date firstUnclaimedLogStartTime = DateTime.parse("2017-07-10T04:00:00.000+01:00").toDate();

        ArrayList<UnclaimedEventDTO> events = new ArrayList<>();

        events.add(new UnclaimedEventDTO(3, DateTime.parse("2017-07-10T04:00:00.000+01:00").toDate()));
        events.add(new UnclaimedEventDTO(4, DateTime.parse("2017-07-10T04:15:00.000+01:00").toDate()));
        events.add(new UnclaimedEventDTO(3, DateTime.parse("2017-07-10T05:00:00.000+01:00").toDate()));
        events.add(new UnclaimedEventDTO(4, DateTime.parse("2017-07-10T05:16:00.000+01:00").toDate()));

        EmployeeLogEldMandateController mandateController = ControllerFactory.getInstance().getEmployeeLogEldMandateController();
        Boolean response = mandateController.IsAddUnclaimedDrivingDurationDataDiagnostic(currentLogStartTime, firstUnclaimedLogStartTime, previousLogDayCount, events.toArray(new UnclaimedEventDTO[4]));

        Assert.assertTrue(response);
    }

    @Test
    public void testIsAddUnclaimedDrivingDurationDataDiagnostic_3amLogStart_DrivingOverStart_DiagnosticNeeded() {

        int previousLogDayCount = 7;
        Date currentLogStartTime = DateTime.parse("2017-07-13T03:00:00.000+01:00").toDate();
        Date firstUnclaimedLogStartTime = DateTime.parse("2017-07-10T03:15:00.000+01:00").toDate();

        ArrayList<UnclaimedEventDTO> events = new ArrayList<>();

        events.add(new UnclaimedEventDTO(3, DateTime.parse("2017-07-10T02:45:00.000+01:00").toDate()));
        events.add(new UnclaimedEventDTO(4, DateTime.parse("2017-07-10T03:15:00.000+01:00").toDate()));
        events.add(new UnclaimedEventDTO(3, DateTime.parse("2017-07-10T05:00:00.000+01:00").toDate()));
        events.add(new UnclaimedEventDTO(4, DateTime.parse("2017-07-10T05:16:00.000+01:00").toDate()));

        EmployeeLogEldMandateController mandateController = ControllerFactory.getInstance().getEmployeeLogEldMandateController();
        Boolean response = mandateController.IsAddUnclaimedDrivingDurationDataDiagnostic(currentLogStartTime, firstUnclaimedLogStartTime, previousLogDayCount, events.toArray(new UnclaimedEventDTO[4]));

        Assert.assertTrue(response);
    }

    @Test
    public void testIsAddUnclaimedDrivingDurationDataDiagnostic_3amLogStart_DrivingOverStart_NoInitialDrive_DiagnosticNeeded() {

        int previousLogDayCount = 7;
        Date currentLogStartTime = DateTime.parse("2017-07-13T03:00:00.000+01:00").toDate();
        Date firstUnclaimedLogStartTime = DateTime.parse("2017-07-10T03:15:00.000+01:00").toDate();

        ArrayList<UnclaimedEventDTO> events = new ArrayList<>();

        events.add(new UnclaimedEventDTO(4, DateTime.parse("2017-07-10T03:15:00.000+01:00").toDate()));
        events.add(new UnclaimedEventDTO(3, DateTime.parse("2017-07-10T05:00:00.000+01:00").toDate()));
        events.add(new UnclaimedEventDTO(4, DateTime.parse("2017-07-10T05:16:00.000+01:00").toDate()));

        EmployeeLogEldMandateController mandateController = ControllerFactory.getInstance().getEmployeeLogEldMandateController();
        Boolean response = mandateController.IsAddUnclaimedDrivingDurationDataDiagnostic(currentLogStartTime, firstUnclaimedLogStartTime, previousLogDayCount, events.toArray(new UnclaimedEventDTO[4]));

        Assert.assertTrue(response);
    }

    @After
    public void tearDown() throws NoSuchFieldException, IllegalAccessException {
        closeDatabase(app);
    }
}