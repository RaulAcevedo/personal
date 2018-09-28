package com.jjkeller.kmbapi.controller.utility;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jjkeller.kmbapi.common.TestBase;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.configuration.LoginCredentials;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.enums.DutyStatusEnum;
import com.jjkeller.kmbapi.enums.EmployeeLogEldEnum;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEventList;
import com.jjkeller.kmbapi.proxydata.EmployeeLogList;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Testes for RestWebServiceHelper
 *
 * Created by jld5296 on 1/26/17.
 */
@RunWith(RobolectricTestRunner.class)
@Config(application = GlobalState.class,
        manifest = "/build/intermediates/bundles/encompass/debug/AndroidManifest.xml"
)
public class RESTWebServiceHelperTest extends TestBase {

    private DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    private GlobalState app;

    @Before
    public void setup(){

        User currentUser = new User();
        LoginCredentials credentials = new LoginCredentials();
        credentials.setEmployeeId("foobar!");

        currentUser.setCredentials(credentials);

        app = (GlobalState) RuntimeEnvironment.application;
        app.setCurrentUser(currentUser);
    }

    @Test
    public void parseDateTimeWireFormat_NegativeTimeZoneAdjustment() {
        DateTime date = RESTWebServiceHelper.parseDateTimeWireFormat("/Date(1485463201111-0700)/");
        String expected = new DateTime( 2017,1,26,7,40,1,111).toString(fmt);
        String actual = date.toString(fmt);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void parseDateTimeWireFormat_PositiveTimeZoneAdjustment() {
        DateTime date = RESTWebServiceHelper.parseDateTimeWireFormat("/Date(1485463201111+0700)/");
        String expected = new DateTime( 2017,1,26,21,40,1,111).toString(fmt);
        String actual = date.toString(fmt);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void parseDateTimeWireFormat_NoTimeZone() {
        DateTime date = RESTWebServiceHelper.parseDateTimeWireFormat("/Date(1485463201111)/");
        String expected =new DateTime(2017,1,26,14,40,1,111).toString(fmt);
        String actual = date.toString(fmt);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void parseDateTimeWireFormat_Empty() {
        DateTime actual = RESTWebServiceHelper.parseDateTimeWireFormat("");
        Assert.assertEquals(null, actual);
    }

    @Test
    public void parseDateTimeWireFormat_Null() {
        DateTime actual = RESTWebServiceHelper.parseDateTimeWireFormat(null);
        //noinspection ConstantConditions
        Assert.assertEquals(null, actual);
    }

    @Test
    public void testDownloadLogsInDateRange_editDurationIsCorrect() throws IOException {
        HttpHelper mockedHttpHelper = mock(HttpHelper.class);
        when(mockedHttpHelper.Get(anyString(), anyMapOf(String.class, String.class), anyInt())).thenReturn("{\"EmployeeLogs\":[{\"EmployeeId\":\"f4fad4de-42cc-4a92-8dbf-95a2f76ca99d\",\"LogDate\":\"5\\/30\\/17 12:00:00 AM\",\"TotalLogDistance\":0.0000,\"HasReturnedToLocation\":false,\"DriverType\":2,\"Ruleset\":26,\"LogEventList\":{\"LogEvents\":[]},\"Timezone\":3,\"TeamDriverList\":{\"TeamDrivers\":[]},\"MobileStartTimestamp\":null,\"MobileEndTimestamp\":null,\"MobileRecordedDistance\":0.0,\"MobileEobrIdentifier\":null,\"TractorNumbers\":\"hhh\",\"TrailerNumbers\":\"ddd\",\"ShipmentInformation\":\"ddd\",\"TimeSyncFailureList\":{\"FailureReports\":[]},\"EobrFailureList\":{\"FailureReports\":[]},\"IsShortHaulExceptionUsed\":false,\"CanadaDeferralType\":0,\"WeeklyResetStartTimestamp\":null,\"IsHaulingExplosives\":false,\"IsWeeklyResetUsed\":false,\"IsWeeklyResetUsedOverridden\":false,\"IsOperatesSpecificVehiclesForOilField\":false,\"ExemptLogType\":0,\"IsNonCDLShortHaulExceptionUsed\":false,\"IsExemptFrom30MinBreakRequirement\":true,\"TrailerPlate\":null,\"VehiclePlate\":null,\"EldEventList\":{\"EldEvents\":[{\"AccumulatedVehicleMiles\":null,\"DiagnosticCode\":null,\"Distance\":null,\"DistanceSinceLastCoordinates\":null,\"DriverDataDiagnosticEventIndicatorStatus\":false,\"DriverLocationDescription\":null,\"DriverOriginatorUserId\":\"f4fad4de-42cc-4a92-8dbf-95a2f76ca99d\",\"EditDuration\":1000,\"EldMalfunctionIndicatorStatus\":false,\"EncompassClusterPK\":84707,\"EncompassOriginatorUserId\":null,\"EndMoveDateTime\":null,\"EngineHours\":null,\"EobrSerialNumber\":\"121537-1732\",\"EventCode\":1,\"EventComment\":null,\"EventDataCheck\":\"83\",\"EventDateTime\":\"5\\/30\\/2017 12:00:00 AM\",\"EventRecordOrigin\":1,\"EventRecordStatus\":1,\"EventSequenceIdNumber\":1,\"EventType\":1,\"Geolocation\":null,\"IsGpsAtReducedPrecision\":false,\"IsUnidentifiedEvent\":false,\"KmbPrimaryKey\":null,\"Latitude\":null,\"LatitudeStatusCode\":null,\"Location\":{\"Name\":null,\"GpsInfo\":{\"TimestampUtc\":null,\"LatitudeDegrees\":0.0,\"LongitudeDegrees\":0.0,\"DecodedInfo\":{\"City\":\"\",\"State\":\"\",\"PostalCode\":\"\",\"Street\":\"\",\"County\":\"\",\"Country\":\"\"}},\"OdometerReading\":-1,\"EndOdometerReading\":-1},\"LogRemark\":null,\"Longitude\":null,\"LongitudeStatusCode\":null,\"MotionPictureAuthorityId\":null,\"MotionPictureProductionId\":null,\"Odometer\":1354,\"OriginalEvent\":true,\"ReassignEvent\":false,\"RelatedEventEncompassClusterPK\":null,\"RelatedEventKmbPrimaryKey\":null,\"RuleSetId\":26,\"ShipmentInfo\":null,\"TractorNumber\":null,\"TrailerNumber\":null,\"TrailerPlate\":null,\"UnidentifiedUserId\":null,\"VehiclePlate\":null},{\"AccumulatedVehicleMiles\":null,\"DiagnosticCode\":null,\"Distance\":null,\"DistanceSinceLastCoordinates\":0.000000,\"DriverDataDiagnosticEventIndicatorStatus\":false,\"DriverLocationDescription\":null,\"DriverOriginatorUserId\":\"f4fad4de-42cc-4a92-8dbf-95a2f76ca99d\",\"EditDuration\":null,\"EldMalfunctionIndicatorStatus\":false,\"EncompassClusterPK\":84708,\"EncompassOriginatorUserId\":null,\"EndMoveDateTime\":null,\"EngineHours\":300.70,\"EobrSerialNumber\":\"121537-1732\",\"EventCode\":1,\"EventComment\":null,\"EventDataCheck\":\"c3\",\"EventDateTime\":\"5\\/30\\/2017 10:07:27 PM\",\"EventRecordOrigin\":1,\"EventRecordStatus\":1,\"EventSequenceIdNumber\":0,\"EventType\":5,\"Geolocation\":null,\"IsGpsAtReducedPrecision\":false,\"IsUnidentifiedEvent\":false,\"KmbPrimaryKey\":null,\"Latitude\":43.080000,\"LatitudeStatusCode\":null,\"Location\":{\"Name\":null,\"GpsInfo\":{\"TimestampUtc\":null,\"LatitudeDegrees\":0.0,\"LongitudeDegrees\":0.0,\"DecodedInfo\":{\"City\":\"\",\"State\":\"\",\"PostalCode\":\"\",\"Street\":\"\",\"County\":\"\",\"Country\":\"\"}},\"OdometerReading\":-1,\"EndOdometerReading\":-1},\"LogRemark\":null,\"Longitude\":-89.360000,\"LongitudeStatusCode\":null,\"MotionPictureAuthorityId\":null,\"MotionPictureProductionId\":null,\"Odometer\":1354,\"OriginalEvent\":true,\"ReassignEvent\":false,\"RelatedEventEncompassClusterPK\":null,\"RelatedEventKmbPrimaryKey\":null,\"RuleSetId\":26,\"ShipmentInfo\":\"ddd\",\"TractorNumber\":\"hhh\",\"TrailerNumber\":\"ddd\",\"TrailerPlate\":null,\"UnidentifiedUserId\":null,\"VehiclePlate\":null}]},\"Id\":\"d5734892-5fc4-4862-b816-1d8a7ccf8f91\",\"IsCertified\":true,\"IsExemptFromELDUse\":false,\"LogType\":-1}]}");

        RESTWebServiceHelper webServiceHelper = new RESTWebServiceHelper(app, mockedHttpHelper);

        EmployeeLogList employeeLogList = webServiceHelper.DownloadLogsInDateRange(
                (new DateTime(2017, 4, 4, 20, 12 ,14)).toDate(),
                (new DateTime(2017, 4, 6, 20, 12 ,14)).toDate(),
                null,
                true);

        Assert.assertEquals(1, employeeLogList.getEmployeeLogList().length);

        EmployeeLog employeelog = employeeLogList.getEmployeeLogList()[0];

        Assert.assertEquals(TimeUnit.MILLISECONDS.convert(1000, TimeUnit.SECONDS), employeelog.getEldEventList().getEldEventList()[0].getEditDuration().longValue());
        Assert.assertNull(employeelog.getEldEventList().getEldEventList()[1].getEditDuration());
    }
    
    @Test
    public void testSubmitLogs_editDurationIsCorrect() throws IOException {
        HttpHelper mockedHttpHelper = mock(HttpHelper.class);
        //We are fetching from the Get to make a nice EmployeeLog payload so then we can serialize it again and check the data.
        when(mockedHttpHelper.Get(anyString(), anyMapOf(String.class, String.class), anyInt())).thenReturn("{\"EmployeeLogs\":[{\"EmployeeId\":\"f4fad4de-42cc-4a92-8dbf-95a2f76ca99d\",\"LogDate\":\"5\\/30\\/17 12:00:00 AM\",\"TotalLogDistance\":0.0000,\"HasReturnedToLocation\":false,\"DriverType\":2,\"Ruleset\":26,\"LogEventList\":{\"LogEvents\":[]},\"Timezone\":3,\"TeamDriverList\":{\"TeamDrivers\":[]},\"MobileStartTimestamp\":null,\"MobileEndTimestamp\":null,\"MobileRecordedDistance\":0.0,\"MobileEobrIdentifier\":null,\"TractorNumbers\":\"hhh\",\"TrailerNumbers\":\"ddd\",\"ShipmentInformation\":\"ddd\",\"TimeSyncFailureList\":{\"FailureReports\":[]},\"EobrFailureList\":{\"FailureReports\":[]},\"IsShortHaulExceptionUsed\":false,\"CanadaDeferralType\":0,\"WeeklyResetStartTimestamp\":null,\"IsHaulingExplosives\":false,\"IsWeeklyResetUsed\":false,\"IsWeeklyResetUsedOverridden\":false,\"IsOperatesSpecificVehiclesForOilField\":false,\"ExemptLogType\":0,\"IsNonCDLShortHaulExceptionUsed\":false,\"IsExemptFrom30MinBreakRequirement\":true,\"TrailerPlate\":null,\"VehiclePlate\":null,\"EldEventList\":{\"EldEvents\":[{\"AccumulatedVehicleMiles\":null,\"DiagnosticCode\":null,\"Distance\":null,\"DistanceSinceLastCoordinates\":null,\"DriverDataDiagnosticEventIndicatorStatus\":false,\"DriverLocationDescription\":null,\"DriverOriginatorUserId\":\"f4fad4de-42cc-4a92-8dbf-95a2f76ca99d\",\"EditDuration\":1000,\"EldMalfunctionIndicatorStatus\":false,\"EncompassClusterPK\":84707,\"EncompassOriginatorUserId\":null,\"EndMoveDateTime\":null,\"EngineHours\":null,\"EobrSerialNumber\":\"121537-1732\",\"EventCode\":1,\"EventComment\":null,\"EventDataCheck\":\"83\",\"EventDateTime\":\"5\\/30\\/2017 12:00:00 AM\",\"EventRecordOrigin\":1,\"EventRecordStatus\":1,\"EventSequenceIdNumber\":1,\"EventType\":1,\"Geolocation\":null,\"IsGpsAtReducedPrecision\":false,\"IsUnidentifiedEvent\":false,\"KmbPrimaryKey\":null,\"Latitude\":null,\"LatitudeStatusCode\":null,\"Location\":{\"Name\":null,\"GpsInfo\":{\"TimestampUtc\":null,\"LatitudeDegrees\":0.0,\"LongitudeDegrees\":0.0,\"DecodedInfo\":{\"City\":\"\",\"State\":\"\",\"PostalCode\":\"\",\"Street\":\"\",\"County\":\"\",\"Country\":\"\"}},\"OdometerReading\":-1,\"EndOdometerReading\":-1},\"LogRemark\":null,\"Longitude\":null,\"LongitudeStatusCode\":null,\"MotionPictureAuthorityId\":null,\"MotionPictureProductionId\":null,\"Odometer\":1354,\"OriginalEvent\":true,\"ReassignEvent\":false,\"RelatedEventEncompassClusterPK\":null,\"RelatedEventKmbPrimaryKey\":null,\"RuleSetId\":26,\"ShipmentInfo\":null,\"TractorNumber\":null,\"TrailerNumber\":null,\"TrailerPlate\":null,\"UnidentifiedUserId\":null,\"VehiclePlate\":null},{\"AccumulatedVehicleMiles\":null,\"DiagnosticCode\":null,\"Distance\":null,\"DistanceSinceLastCoordinates\":0.000000,\"DriverDataDiagnosticEventIndicatorStatus\":false,\"DriverLocationDescription\":null,\"DriverOriginatorUserId\":\"f4fad4de-42cc-4a92-8dbf-95a2f76ca99d\",\"EditDuration\":null,\"EldMalfunctionIndicatorStatus\":false,\"EncompassClusterPK\":84708,\"EncompassOriginatorUserId\":null,\"EndMoveDateTime\":null,\"EngineHours\":300.70,\"EobrSerialNumber\":\"121537-1732\",\"EventCode\":1,\"EventComment\":null,\"EventDataCheck\":\"c3\",\"EventDateTime\":\"5\\/30\\/2017 10:07:27 PM\",\"EventRecordOrigin\":1,\"EventRecordStatus\":1,\"EventSequenceIdNumber\":0,\"EventType\":5,\"Geolocation\":null,\"IsGpsAtReducedPrecision\":false,\"IsUnidentifiedEvent\":false,\"KmbPrimaryKey\":null,\"Latitude\":43.080000,\"LatitudeStatusCode\":null,\"Location\":{\"Name\":null,\"GpsInfo\":{\"TimestampUtc\":null,\"LatitudeDegrees\":0.0,\"LongitudeDegrees\":0.0,\"DecodedInfo\":{\"City\":\"\",\"State\":\"\",\"PostalCode\":\"\",\"Street\":\"\",\"County\":\"\",\"Country\":\"\"}},\"OdometerReading\":-1,\"EndOdometerReading\":-1},\"LogRemark\":null,\"Longitude\":-89.360000,\"LongitudeStatusCode\":null,\"MotionPictureAuthorityId\":null,\"MotionPictureProductionId\":null,\"Odometer\":1354,\"OriginalEvent\":true,\"ReassignEvent\":false,\"RelatedEventEncompassClusterPK\":null,\"RelatedEventKmbPrimaryKey\":null,\"RuleSetId\":26,\"ShipmentInfo\":\"ddd\",\"TractorNumber\":\"hhh\",\"TrailerNumber\":\"ddd\",\"TrailerPlate\":null,\"UnidentifiedUserId\":null,\"VehiclePlate\":null}]},\"Id\":\"d5734892-5fc4-4862-b816-1d8a7ccf8f91\",\"IsCertified\":true,\"IsExemptFromELDUse\":false,\"LogType\":-1}]}");
        when(mockedHttpHelper.Post(anyString(), anyMapOf(String.class, String.class), anyString(), anyInt())).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) {
                //When we send Post, we'll grab the 2nd argument since that's what sent to the server.
                //Then we can then do tests on it.
                String jsonPayload = (String) invocation.getArguments()[2];
                JsonElement jsonElement = new JsonParser().parse(jsonPayload);


                JsonArray firstEmployeeEldEventList = jsonElement.getAsJsonObject().getAsJsonArray("EmployeeLogs")
                        .get(0).getAsJsonObject()
                        .getAsJsonObject("EldEventList").getAsJsonArray("EldEvents").getAsJsonArray();

                assertEquals(1000, firstEmployeeEldEventList.get(0).getAsJsonObject().get("EditDuration").getAsInt());
                assertNull(firstEmployeeEldEventList.get(1).getAsJsonObject().get("EditDuration"));

                return (String) invocation.getArguments()[2];
            }
        });

        RESTWebServiceHelper webServiceHelper = new RESTWebServiceHelper(app, mockedHttpHelper);

        EmployeeLogList employeeLogList = webServiceHelper.DownloadLogsInDateRange(
                (new DateTime(2017, 4, 4, 20, 12 ,14)).toDate(),
                (new DateTime(2017, 4, 6, 20, 12 ,14)).toDate(),
                null,
                true);

        webServiceHelper.SubmitLogs(employeeLogList);
    }

    @Test
    public void testSubmitLogs_splitsMandateAobrdLogsOnSerialization() throws IOException {
        EmployeeLogEldEvent aobrdEvent = buildLogEvent(EmployeeLogEldEnum.AOBRD);

        DateTime startDate = new DateTime(2017, 5, 29, 12, 0);
        EmployeeLog aobrdLog = buildLog(startDate.toDate(), aobrdEvent);
        EmployeeLogEldEvent mandateEvent = buildLogEvent(1);
        EmployeeLog mandateLog = buildLog(startDate.plusDays(1).toDate(), mandateEvent);
        EmployeeLog[] logs = new EmployeeLog[]{aobrdLog, mandateLog};
        EmployeeLogList employeeLogList = new EmployeeLogList();
        employeeLogList.setEmployeeLogList(logs);

        HttpHelper mockedHttpHelper = mock(HttpHelper.class);
        when(mockedHttpHelper.Post(anyString(), anyMapOf(String.class, String.class), anyString(),
                anyInt())).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) {
                //When we send Post, we'll grab the 2nd argument since that's what sent to the
                // server. Then we can then do tests on it.
                String jsonPayload = (String) invocation.getArguments()[2];
                JsonElement jsonElement = new JsonParser().parse(jsonPayload);

                JsonObject submittedAobrdLog =
                        jsonElement.getAsJsonObject().getAsJsonArray("EmployeeLogs")
                                .get(0).getAsJsonObject();

                JsonObject submittedMandateLog =
                        jsonElement.getAsJsonObject().getAsJsonArray("EmployeeLogs")
                                .get(1).getAsJsonObject();

                assertEquals(1, submittedAobrdLog.getAsJsonObject("LogEventList")
                        .getAsJsonArray("LogEvents").getAsJsonArray().size());
                assertNull(submittedAobrdLog.getAsJsonObject("EldEventList"));

                assertNull(submittedMandateLog.getAsJsonObject("LogEventList"));
                assertEquals(1, submittedMandateLog.getAsJsonObject("EldEventList")
                        .getAsJsonArray("EldEvents").getAsJsonArray().size());

                return (String) invocation.getArguments()[2];
            }
        });

        RESTWebServiceHelper webServiceHelper = new RESTWebServiceHelper(app, mockedHttpHelper);

        webServiceHelper.SubmitLogs(employeeLogList);
    }

    private EmployeeLog buildLog(Date mobileStartTime, EmployeeLogEldEvent event) {
        EmployeeLog employeeLog = new EmployeeLog();
        employeeLog.setMobileStartTimestamp(mobileStartTime);
        EmployeeLogEldEventList eventList = new EmployeeLogEldEventList();
        eventList.setEldEventList(new EmployeeLogEldEvent[]{event});
        employeeLog.setEldEventList(eventList);
        return employeeLog;
    }

    private EmployeeLogEldEvent buildLogEvent(int sequenceIdNumber) {
        EmployeeLogEldEvent event = new EmployeeLogEldEvent(new Date());
        event.setDutyStatusEnum(new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
        event.setEventSequenceIDNumber(sequenceIdNumber);
        return event;
    }

    @After
    public void tearDown() throws NoSuchFieldException, IllegalAccessException {
        closeDatabase(app);
    }
}