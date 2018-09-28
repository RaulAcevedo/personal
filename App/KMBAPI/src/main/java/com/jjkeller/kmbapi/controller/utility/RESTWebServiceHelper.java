package com.jjkeller.kmbapi.controller.utility;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import com.jjkeller.kmbapi.common.TimeKeeper;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.configuration.KmbUserInfo;
import com.jjkeller.kmbapi.controller.KMBEncompassUser;
import com.jjkeller.kmbapi.controller.interfaces.IIoTHubSettingsCreator;
import com.jjkeller.kmbapi.controller.share.CreateIoTHubSettingsCommand;
import com.jjkeller.kmbapi.controller.share.IoTHubSettings;
import com.jjkeller.kmbapi.employeelogeldevents.Enums;
import com.jjkeller.kmbapi.enums.CanadaDeferralTypeEnum;
import com.jjkeller.kmbapi.enums.DataProfileEnum;
import com.jjkeller.kmbapi.enums.DataTransferFileStatusEnum;
import com.jjkeller.kmbapi.enums.DatabusTypeEnum;
import com.jjkeller.kmbapi.enums.DriverTypeEnum;
import com.jjkeller.kmbapi.enums.DrivingNotificationTypeEnum;
import com.jjkeller.kmbapi.enums.DutyStatusEnum;
import com.jjkeller.kmbapi.enums.EmployeeLogEldEnum;
import com.jjkeller.kmbapi.enums.EngineRecordTypeEnum;
import com.jjkeller.kmbapi.enums.EobrCommunicationModeEnum;
import com.jjkeller.kmbapi.enums.ExemptLogTypeEnum;
import com.jjkeller.kmbapi.enums.FailureCategoryEnum;
import com.jjkeller.kmbapi.enums.FuelClassificationEnum;
import com.jjkeller.kmbapi.enums.FuelUnitEnum;
import com.jjkeller.kmbapi.enums.InspectionDefectType;
import com.jjkeller.kmbapi.enums.InspectionTypeEnum;
import com.jjkeller.kmbapi.enums.RoadsideDataTransferMethodEnum;
import com.jjkeller.kmbapi.enums.RuleSetTypeEnum;
import com.jjkeller.kmbapi.enums.TimeZoneEnum;
import com.jjkeller.kmbapi.kmbeobr.EventTypeEnum;
import com.jjkeller.kmbapi.proxydata.ApplicationUpdateInfo;
import com.jjkeller.kmbapi.proxydata.AuthenticationInfo;
import com.jjkeller.kmbapi.proxydata.CompanyConfigSettings;
import com.jjkeller.kmbapi.proxydata.ConditionalFirmwareUpgrade;
import com.jjkeller.kmbapi.proxydata.DataTransferFileStatusResponse;
import com.jjkeller.kmbapi.proxydata.DecodedLocation;
import com.jjkeller.kmbapi.proxydata.DefectList;
import com.jjkeller.kmbapi.proxydata.DriverHoursAvailableSummary;
import com.jjkeller.kmbapi.proxydata.DrivingEventReassignmentMapping;
import com.jjkeller.kmbapi.proxydata.EldEventAdapterList;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEventList;
import com.jjkeller.kmbapi.proxydata.EmployeeLogList;
import com.jjkeller.kmbapi.proxydata.EmployeeLogReportRequest;
import com.jjkeller.kmbapi.proxydata.EmployeeLogRevision;
import com.jjkeller.kmbapi.proxydata.EmployeeLogUnidentifiedELDEventStatus;
import com.jjkeller.kmbapi.proxydata.EmployeeLogWithProvisions;
import com.jjkeller.kmbapi.proxydata.EmployeeRule;
import com.jjkeller.kmbapi.proxydata.EngineRecordList;
import com.jjkeller.kmbapi.proxydata.EobrConfiguration;
import com.jjkeller.kmbapi.proxydata.EobrDiagnosticCommand;
import com.jjkeller.kmbapi.proxydata.EventDataRecordList;
import com.jjkeller.kmbapi.proxydata.FailureReport;
import com.jjkeller.kmbapi.proxydata.FailureReportList;
import com.jjkeller.kmbapi.proxydata.FeatureToggle;
import com.jjkeller.kmbapi.proxydata.FirmwareVersion;
import com.jjkeller.kmbapi.proxydata.FmcsaEldProviderInfo;
import com.jjkeller.kmbapi.proxydata.FmcsaEldRegistrationInfo;
import com.jjkeller.kmbapi.proxydata.FuelPurchase;
import com.jjkeller.kmbapi.proxydata.GeotabDriver;
import com.jjkeller.kmbapi.proxydata.GpsLocation;
import com.jjkeller.kmbapi.proxydata.KMBEncompassUserList;
import com.jjkeller.kmbapi.proxydata.KMBUnassignedPeriodIsClaimable;
import com.jjkeller.kmbapi.proxydata.ListProxy;
import com.jjkeller.kmbapi.proxydata.Location;
import com.jjkeller.kmbapi.proxydata.LocationCode;
import com.jjkeller.kmbapi.proxydata.LogCheckerComplianceDates;
import com.jjkeller.kmbapi.proxydata.LogEvent;
import com.jjkeller.kmbapi.proxydata.LogEventList;
import com.jjkeller.kmbapi.proxydata.LogRemarkItem;
import com.jjkeller.kmbapi.proxydata.MobileDevice;
import com.jjkeller.kmbapi.proxydata.MotionPictureAuthority;
import com.jjkeller.kmbapi.proxydata.MotionPictureProduction;
import com.jjkeller.kmbapi.proxydata.ProxyBase;
import com.jjkeller.kmbapi.proxydata.RoutePosition;
import com.jjkeller.kmbapi.proxydata.RoutePositionList;
import com.jjkeller.kmbapi.proxydata.TeamDriver;
import com.jjkeller.kmbapi.proxydata.TeamDriverList;
import com.jjkeller.kmbapi.proxydata.TripRecordList;
import com.jjkeller.kmbapi.proxydata.UnassignedDrivingPeriod;
import com.jjkeller.kmbapi.proxydata.UnassignedEobrFailurePeriod;
import com.jjkeller.kmbapi.proxydata.VehicleInspection;
import com.jjkeller.kmbapi.proxydata.VehicleInspectionDefect;
import com.jjkeller.kmbapi.proxydata.VehicleInspectionList;

import org.joda.time.DateTime;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import static com.jjkeller.kmbapi.common.Constants.KMB_PRIMARY_KEY;
import static com.jjkeller.kmbapi.common.Constants.RELATED_EVENT_CLUSTER_PRIMARY_KEY;
import static com.jjkeller.kmbapi.common.Constants.RELATED_EVENT_KMB_PRIMARY_KEY;

public class RESTWebServiceHelper implements IWebAPIServiceHelper, IIoTHubSettingsCreator,
        IRESTWebServiceHelper {

    private static final int TIMEOUT_DOWNLOAD_LOGS =  45000; //45 second Custom timeout for download logs.

    private Gson _geotabUnidentifiedEmployeeLogEldEventGson = new GsonBuilder()
            .setDateFormat(DateUtility.getHomeTerminalDMOSoapDateTimeFormat().toPattern())
            .registerTypeAdapter(EmployeeLogEldEventList.class,
                    new GeotabUnidentifiedEmployeeLogEldEventListDeserializer())
            .registerTypeAdapter(EmployeeLogEldEvent.class,
                    new GeotabUnidentifiedEmployeeLogEldEventDeserializer())
            .create();


    private Gson _gson = new GsonBuilder()
            .setDateFormat(DateUtility.getHomeTerminalDMOSoapDateTimeFormat().toPattern())
            .registerTypeAdapter(ApplicationUpdateInfo.class,
                    new ApplicationUpdateInfoDeserializer())
            .registerTypeAdapter(AuthenticationInfo.class,
                    new AuthenticationInfoDeserializer())
            .registerTypeAdapter(CompanyConfigSettings.class,
                    new CompanyConfigSettingsDeserializer())
            .registerTypeAdapter(Date.class, new DateDeserializer())
            .registerTypeAdapter(MotionPictureAuthority.class,
                    new MotionPictureAuthorityDeserializer())
            .registerTypeAdapter(MotionPictureAuthority.class,
                    new MotionPictureAuthoritySerializer())
            .registerTypeAdapter(MotionPictureAuthority[].class,
                    new MotionPictureAuthorityArrayDeserializer())

            .registerTypeAdapter(MotionPictureProduction.class,
                    new MotionPictureProductionDeserializer())
            .registerTypeAdapter(MotionPictureProduction.class,
                    new MotionPictureProductionSerializer())
            .registerTypeAdapter(MotionPictureProduction[].class,
                    new MotionPictureProductionArrayDeserializer())

            .registerTypeAdapter(DecodedLocation.class,
                    new DecodedLocationSerializer())
            .registerTypeAdapter(DriverHoursAvailableSummary[].class,
                    new DriverHoursAvailableSummaryArraySerializer())
            .registerTypeAdapter(DriverHoursAvailableSummary.class,
                    new DriverHoursAvailableSummarySerializer())
            .registerTypeAdapter(EmployeeLogEldEvent.class,
                    new EmployeeLogEldEventDeserializer())
            .registerTypeAdapter(EmployeeLogEldEventList.class,
                    new EmployeeLogEldEventListDeserializer())
            .registerTypeAdapter(EmployeeLogList.class,
                    new EmployeeLogListDeserializer())
            .registerTypeAdapter(KMBEncompassUser.class,
                    new KMBEncompassUserDeserializer())
            .registerTypeAdapter(KMBEncompassUserList.class,
                    new KMBEncompassUserListDeserializer())
            .registerTypeAdapter(EmployeeLog.class,
                    new EmployeeLogDeserializer())
            .registerTypeAdapter(EmployeeRule.class,
                    new EmployeeRuleDeserializer())
            .registerTypeAdapter(EobrConfiguration.class,
                    new EobrConfigurationDeserializer())
            .registerTypeAdapter(EobrConfiguration.class,
                    new EobrConfigurationSerializer())
            .registerTypeAdapter(EobrDiagnosticCommand.class,
                    new EobrDiagnosticCommandDeserializer())
            .registerTypeAdapter(EobrDiagnosticCommand.class,
                    new EobrDiagnosticCommandSerializer())
            .registerTypeAdapter(FailureReport.class,
                    new FailureReportDeserializer())
            .registerTypeAdapter(FuelPurchase[].class,
                    new FuelPurchaseArraySerializer())
            .registerTypeAdapter(FuelPurchase.class,
                    new FuelPurchaseSerializer())
            .registerTypeAdapter(GpsLocation.class,
                    new GpsLocationDeserializer())
            .registerTypeAdapter(GpsLocation.class, new GpsLocationSerializer())
            .registerTypeAdapter(GpsLocation[].class,
                    new GpsLocationListDeserializer())
            .registerTypeAdapter(LogEvent.class, new LogEventDeserializer())
            .registerTypeAdapter(Location.class, new LocationDeserializer())
            .registerTypeAdapter(LocationCode.class,
                    new LocationCodeDeserializer())
            .registerTypeAdapter(LocationCode[].class,
                    new LocationCodeArrayDeserializer())
            .registerTypeAdapter(TeamDriver.class, new TeamDriverDeserializer())
            .registerTypeAdapter(UnassignedDrivingPeriod.class,
                    new UnassignedDrivingPeriodSerializer())
            .registerTypeAdapter(UnassignedDrivingPeriod[].class,
                    new UnassignedDrivingPeriodArraySerializer())
            .registerTypeAdapter(UnassignedDrivingPeriod.class,
                    new UnassignedDrivingPeriodDeserializer())
            .registerTypeAdapter(KMBUnassignedPeriodIsClaimable.class,
                    new KMBUnassignedPeriodIsClaimableDeserializer())
            .registerTypeAdapter(KMBUnassignedPeriodIsClaimable[].class,
                    new GenericArrayDeserializer(KMBUnassignedPeriodIsClaimable.class))
            .registerTypeAdapter(UnassignedDrivingPeriod[].class,
                    new UnassignedDrivingPeriodArrayDeserializer())
            .registerTypeAdapter(UnassignedEobrFailurePeriod.class,
                    new UnassignedEobrFailurePeriodSerializer())
            .registerTypeAdapter(UnassignedEobrFailurePeriod[].class,
                    new UnassignedEobrFailurePeriodArraySerializer())
            .registerTypeAdapter(ListProxy.class,
                    new ListProxySerializer<>())
            .registerTypeAdapter(EmployeeLogList.class,
                    new EmployeeLogListSerializer())
            .registerTypeAdapter(EmployeeLog[].class,
                    new EmployeeLogArraySerializer())
            .registerTypeAdapter(EmployeeLog.class, new EmployeeLogSerializer())
            .registerTypeAdapter(EmployeeLogEldEventList.class,
                    new EmployeeLogEldEventListSerializer())
            .registerTypeAdapter(EmployeeLogEldEvent[].class,
                    new EmployeeLogEldEventArraySerializer())

            .registerTypeAdapter(EmployeeLogEldEvent.class, new EmployeeLogEldEventSerializer())
            .registerTypeAdapter(LogEventList.class, new LogEventListSerializer())
            .registerTypeAdapter(LogEvent[].class, new LogEventArraySerializer())
            .registerTypeAdapter(LogEvent.class, new LogEventSerializer())
            .registerTypeAdapter(LogCheckerComplianceDates.class,
                    new LogCheckerComplianceDatesArrayDeserializer())
            .registerTypeAdapter(LogCheckerComplianceDates.class,
                    new LogCheckerComplianceDatesDeserializer())
            .registerTypeAdapter(TeamDriverList.class,
                    new TeamDriverListSerializer())
            .registerTypeAdapter(TeamDriver[].class,
                    new TeamDriverArraySerializer())
            .registerTypeAdapter(TeamDriver.class, new TeamDriverSerializer())
            .registerTypeAdapter(FailureReportList.class,
                    new FailureReportListSerializer())
            .registerTypeAdapter(FailureReport[].class,
                    new FailureReportArraySerializer())
            .registerTypeAdapter(FailureReport.class,
                    new FailureReportSerializer())
            .registerTypeAdapter(Location.class, new LocationSerializer())
            .registerTypeAdapter(VehicleInspectionList.class,
                    new VehicleInspectionListSerializer())
            .registerTypeAdapter(VehicleInspection[].class,
                    new VehicleInspectionArraySerializer())
            .registerTypeAdapter(VehicleInspectionList.class,
                    new VehicleInspectionListDeserializer())
            .registerTypeAdapter(VehicleInspection.class,
                    new VehicleInspectionSerializer())
            .registerTypeAdapter(VehicleInspection.class,
                    new VehicleInspectionDeserializer())
            .registerTypeAdapter(VehicleInspectionDefect.class,
                    new VehicleInspectionDefectSerializer())
            .registerTypeAdapter(VehicleInspectionDefect.class,
                    new VehicleInspectionDefectDeserializer())
            .registerTypeAdapter(DefectList.class, new DefectListSerializer())
            .registerTypeAdapter(VehicleInspectionDefect[].class,
                    new VehicleInspectionDefectArraySerializer())
            .registerTypeAdapter(DefectList.class, new DefectListDeserializer())
            .registerTypeAdapter(LogRemarkItem[].class,
                    new LogRemarkItemArrayDeserializer())
            .registerTypeAdapter(LogRemarkItem.class,
                    new LogRemarkItemDeserializer())
            .registerTypeAdapter(MobileDevice.class,
                    new MobileDeviceSerializer())
            .registerTypeAdapter(MobileDevice.class,
                    new MobileDeviceDeserializer())
            .registerTypeAdapter(EmployeeLogWithProvisions.class,
                    new EmployeeLogWithProvisionsSerializer())
            .registerTypeAdapter(DrivingEventReassignmentMapping.class,
                    new DrivingEventReassignmentMappingSerializer())
            .registerTypeAdapter(DrivingEventReassignmentMapping.class,
                    new DrivingEventReassignmentMappingDeserializer())
            .registerTypeAdapter(EmployeeLogReportRequest.class, new ReportRequestSerializer())
            .registerTypeAdapter(CreateIoTHubSettingsCommand.class, new CreateIoTHubSettingsCommandSerializer())
            .registerTypeAdapter(IoTHubSettings.class, new IoTHubSettingsDeserializer())
            .registerTypeAdapter(FeatureToggle.class,
                    new FeatureToggleDeserializer())
            .registerTypeAdapter(FeatureToggle.class, new FeatureToggleSerializer())
            .registerTypeAdapter(FeatureToggle[].class, new FeatureToggleArrayDeserializer())
            .registerTypeAdapter(DataTransferFileStatusResponse.class, new DataTransferFileStatusResponseDeserializer())
            .create();

    private static final String METHODNAME_ISAUTHENTICATED = "%s/Authenticate/%s";
    private static final String METHODNAME_GETAUTHENTICATIONINFORMATION = "%s/GetAuthenticateInformation/%s";
    private static final String METHODNAME_GETCOMPANYCONFIG = "%s/Activate/%s";
    private static final String METHODNAME_CREATEIOTHUBSETTINGS = "%s/CreateIoTHubSettings";
    private static final String METHODNAME_DOWNLOADMOTIONPICTUREAUTHORITIES = "%s/DownloadMotionPictureAuthorities";
    private static final String METHODNAME_DOWNLOADMOTIONPICTUREPRODUCTIONS = "%s/DownloadMotionPictureProductions";
    private static final String METHODNAME_DOWNLOADLOGSINDATERANGE = "%s/Logs?startDate=%s&endDate=%s&changeTimestampUTC=%s&isShortHaulExceptionAllowed=%s";
    private static final String METHODNAME_DOWNLOADKMBENCOMPASSUSERS = "%s/GetKMBEncompassUsers";
    private static final String METHODNAME_DOWNLOADEMPLOYEERULESETTINGS = "%s/EmployeeRule?changeTimestampUTC=%s";
    private static final String METHODNAME_DOWNLOADLOCATIONCODES = "%s/LocationCodes?changeTimestampUTC=%s";
    private static final String METHODNAME_DOWNLOADEOBRCONFIGURATION = "%s/EobrConfiguration/%s?changeTimestampUTC=%s";
    private static final String METHODNAME_CHANGEPASSWORD = "%s/ChangePassword";
    private static final String METHODNAME_GETDMOTIME = "%s/DMOTime";
    private static final String METHODNAME_EMPLOYEENAMEFORCODE = "%s/EmployeeName/%s";
    private static final String METHODNAME_EMPLOYEENAMEFORUSERNAME = "%s/EmployeeNameForKMBUser/%s";
    private static final String METHODNAME_INITIATEFILEUPLOAD = "%s/FileUpload/Initiate";
    private static final String METHODNAME_SUBMITEOBRCONFIGURATION = "%s/SubmitEobrConfiguration";
    private static final String METHODNAME_CHECKFORUPDATES = "%s/CheckForUpdates?applicationName=%s&platform=%s&architecture=%s&versonNumber=%s&autoUpdateEnabled=%s";
    private static final String METHODNAME_SUBMITROUTEPOSITIONS = "%s/SubmitRoutePositions";
    private static final String METHODNAME_SUBMITUNASSIGNEDDRIVINGPERIODS = "%s/UnassignedDrivingPeriods";
    private static final String METHODNAME_SUBMITUNASSIGNEDEOBRFAILUREPERIODS = "%s/UnassignedEobrFailurePeriods";
    private static final String METHODNAME_REVERSEGEOCODE = "%s/ReverseGeocode";
    private static final String METHODNAME_SUBMITDRIVERHOURSAVAILABLESUMMARIES = "%s/SubmitDriverHoursAvailableSummaries";
    private static final String METHODNAME_SUBMITFUELPURCHASES = "%s/SubmitFuelPurchases";
    private static final String METHODNAME_SUBMITENGINERECORDS = "%s/SubmitEngineRecords";
    private static final String METHODNAME_SUBMITTRIPRECORDS = "%s/SubmitTripRecords";
    private static final String METHODNAME_SUBMITEVENTDATARECORD = "%s/SubmitEventDataRecords";
    private static final String METHODNAME_SUBMITLOGS = "%s/SubmitLogs";
    private static final String METHODNAME_SETDATATRANSFERMECHANISMSTATUS = "%s/SetDataTransferMechanismStatus?Id=%s";
    private static final String METHODNAME_GETDATATRANSFERMECHANISMSTATUS = "%s/GetDataTransferMechanismStatus?Id=%s";
    private static final String METHODNAME_SUBMITBVEHICLEINSPECTIONS = "%s/SubmitVehicleInspections";
    private static final String METHODNAME_SUBMITEMPLOYEELOGREPORTREQUEST = "%s/SubmitEmployeeLogReportRequest";
    private static final String METHODNAME_UPLOADFILE = "%s/UploadFile/%s/%s";
    private static final String METHODNAME_DOWNLOADRECENTVEHICLEINSPECTION = "%s/DownloadRecentVehicleInspection/%s?changeTimestampUTC=%s";
    private static final String METHODNAME_DOWNLOADRECENTVEHICLEPREINSPECTION = "%s/DownloadRecentVehiclePreInspection/%s?changeTimestampUTC=%s";
    private static final String METHODNAME_DOWNLOADRECENTTRAILERINSPECTION = "%s/DownloadRecentTrailerInspection/%s?changeTimestampUTC=%s";
    private static final String METHODNAME_DOWNLOADRECENTTRAILERPREINSPECTION = "%s/DownloadRecentTrailerPreInspection/%s?changeTimestampUTC=%s";
    private static final String METHODNAME_GETEOBRUNITLICENSEPLATENUMBER = "%s/GetEobrUnitLicensePlateNumber?eobrSerialNumber=%s";
    private static final String METHODNAME_CHECKFOROPENDVIR = "%s/CheckForOpenDVIR/%s?changeTimestampUTC=%s";
    private static final String METHODNAME_DOWNLOADLOGCHECKERCOMPLIANCEDATES = "%s/DownloadLogCheckerComplianceDates?complianceDate=%s";
    private static final String METHODNAME_DOWNLOADLOGREMARKITEMS = "%s/DownloadLogRemarkItems?changeDate=%s";
    private static final String METHODNAME_GETPENDINGEOBRDIAGNOSTICCOMMANDS = "%s/GetPendingELDDiagnosticCommands?eobrSerialNumber=%s";
    private static final String METHODNAME_SUBMITEOBRDIAGNOSTICRESULTS = "%s/SubmitELDDiagnosticResults";
    private static final String METHODNAME_SUBMITMOBILEDEVICE = "%s/SubmitMobileConfiguration";
    private static final String METHODNAME_SUBMITEMPLOYEELOGWITHPROVISIONS = "%s/SubmitEmployeeLogWithProvisions";
    private static final String METHODNAME_DOWNLOAD_EMPLOYEE_LOG_WITH_PROVISIONS = "%s/GetEmployeeLogWithProvisionsByDateRange?EmployeeId=%s&StartDate=%s&EndDate=%s";

    private static final String METHODNAME_SUBMITEMPLOYEELOGREVISIONS = "%s/SubmitEmployeeLogRevisions";
    private static final String METHODNAME_GETDATATRANSFERROADSIDEFILE = "%s/GetDataTransferRoadsideFile?transferMethod=%d&startDate=%s&endDate=%s&comment=%s&eldIdentifier=%s";
    private static final String METHODNAME_GETROADSIDEFILESTATUSBYFILENAME = "%s/GetRoadsideFileStatusByFilename?filename=%s&transferMethod=%d";
    private static final String METHODNAME_SUBMITUNASSIGNEDELDEVENTS = "%s/SubmitUnidentifiedEldEvents";
    private static final String METHODNAME_GETDISCONNECTEDDRIVINGPERIODS = "%s/GetDisconnectedDrivingPeriods?eobrSerialNumber=%s&startTimeUTC=%s";
    private static final String METHODNAME_SUBMITREASSIGNMENTREQUEST = "%s/CreateReassignedEldEvent";
    private static final String METHODNAME_CLAIMDISCONNECTEDDRIVINGPERIODS = "%s/ClaimDisconnectedDrivingPeriods";
    private static final String METHODNAME_RECORDCONDITIONALFIRMWAREUPGRADE = "%s/RecordConditionalFirmwareUpgrade";
    private static final String METHODNAME_CHECKFORFIRMWAREUPDATE = "%s/CheckForFirmwareUpdate?eobrSerialNumber=%s&majorVersion=%d&minorVersion=%d";
    private static final String METHODNAME_DOWNLOADFIRMWAREUPDATE = "%s/DownloadFirmwareUpdate?firmwareVersionId=%s";
    private static final String METHODNAME_DOWNLOADUNIDENTIFIEDEVENTSINDATERANGE = "%s/UnidentifiedEvents?startDate=%s&endDate=%s";
    private static final String METHODNAME_DOWNLOADFMCSAELDREGISTRATIONINFO = "%s/DownloadFmcsaEldRegistrationInfo?appPlatform=0&changeDate=%s";
    private static final String METHODNAME_DOWNLOADFMCSAELDPROVIDERINFO = "%s/DownloadFmcsaEldProviderInfo";
    private static final String METHODNAME_GETSTATUSOFUNIDENTIFIEDELDEVENTS = "%s/GetStatusOfUnidentifiedEldEvents?unidentifiedEldEventClusterPKs=%s";
    private static final String METHODNAME_DOWNLOADFEATURETOGGLES = "%s/DownloadFeatureToggles?appPlatform=%s&companyId=%s";
    private static final String METHODNAME_SUBMITGEOTABDRIVERCHANGE = "%s/GeotabDriverChange";
    private static final String METHODNAME_GETGEOTABUNIDENTIFIEDEVENTS = "%s/GetGeotabUnidentifiedEventsBySerialNumber/%s";

    private static final String WALLED_GARDEN_URL = "http://clients3.google.com/generate_204";
    private static final int WALLED_GARDEN_SOCKET_TIMEOUT_MS = 10000;

    private String _baseUrl;
    private HttpHelper _httpHelper;
    private Context _callingContext;

    public RESTWebServiceHelper(Context c, HttpHelper httpHelper) {
        this._callingContext = c;
        this._httpHelper = httpHelper;

        _baseUrl = GlobalState.getInstance().getAppSettings(this._callingContext).getKmbWebServiceRESTUrl();
    }

    public RESTWebServiceHelper(Context c) {
        this(c, new HttpHelper(c));
    }


    private static Context mContext;

    public Gson getGson(){
        return _gson;
    }
    
    public boolean verify(String hostname, SSLSession session) {
        Log.i("RestUtilImpl", "Approving certificate for " + hostname);
        return true;
    }

    static {
        HttpsURLConnection.setDefaultSSLSocketFactory(new NoSSLv3Factory());


        /* THE FOLLOWING CODE WAS PUT IN PLACE TO ALLOW ANDROID 4.X-5.X DEVICES TO RESOLVE HOSTNAMES IN AZURE.
         * THIS CODE SHOULD BE REMOVED ONCE OUR CUSTOMERS HAVE UPGRADED TO ANDROID 6 OR HIGHER*/


        if(Build.VERSION.SDK_INT < 23) {
            // Install the all-trusting trust manager
            try {
                //Allow Android 4.x-5.x to resolve hostname for Azure environment
                HttpsURLConnection.setDefaultHostnameVerifier(new NullHostNameVerifier());
                SSLContext sc = SSLContext.getInstance("TLS");
                sc.init(null, null, new java.security.SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            } catch (GeneralSecurityException e) {
                ErrorLogHelper.RecordMessage(e.getMessage());
            }
        }
    }

    private static String convertToLogCheckerMandateUrl(String url) {
        return url.replace("LogCheckerService.svc", "LogCheckerMandateService.svc");
    }

    public CompanyConfigSettings GetCompanyConfig(String activationCode) throws IOException, JsonSyntaxException {
        String URL = String.format(METHODNAME_GETCOMPANYCONFIG, GlobalState.getInstance().getAppSettings(this._callingContext).getKmbActivationRESTUrl(), AddURLEncoding(activationCode));

        String result = HTTPURLGetRequest(URL, null);
        CompanyConfigSettings companyConfig = null;
        try {
            companyConfig = _gson.fromJson(result, CompanyConfigSettings.class);
        } catch (JsonSyntaxException jsonEx) {
            ErrorLogHelper.RecordException(_callingContext, jsonEx);
        }

        return companyConfig;
    }

    public AuthenticationInfo IsAuthenticated(String username, String password) throws IOException, JsonSyntaxException {

        String URL = String.format(METHODNAME_ISAUTHENTICATED, _baseUrl, AddURLEncoding(username));

        String result = HTTPURLGetRequest(URL, "UserAuthPW," + password);
        AuthenticationInfo authInfo = null;
        try {
            authInfo = _gson.fromJson(result, AuthenticationInfo.class);
        } catch (JsonSyntaxException jsonEx) {
            ErrorLogHelper.RecordException(_callingContext, jsonEx);
        }

        return authInfo;
    }

    public AuthenticationInfo GetAuthenticationInformation(String username) throws IOException, JsonSyntaxException {

        String URL = String.format(METHODNAME_GETAUTHENTICATIONINFORMATION, _baseUrl, AddURLEncoding(username));

        String result = HTTPURLGetRequest(URL, null);
        AuthenticationInfo authInfo = null;
        try {
            authInfo = _gson.fromJson(result, AuthenticationInfo.class);
        } catch (JsonSyntaxException jsonEx) {
            ErrorLogHelper.RecordException(_callingContext, jsonEx);
        }

        return authInfo;
    }

    public IoTHubSettings CreateIoTHubSettings(MobileDevice mobileDevice) throws IOException {
        String url = String.format(METHODNAME_CREATEIOTHUBSETTINGS, _baseUrl);

        CreateIoTHubSettingsCommand command = new CreateIoTHubSettingsCommand();
        command.setDeviceId(mobileDevice.getDeviceIMEI());
        String body = _gson.toJson(command);

        String result = HTTPURLPostRequest(url, false, null, body);

        IoTHubSettings iotHubSettings = null;
        try {
            iotHubSettings = _gson.fromJson(result, IoTHubSettings.class);
        } catch (JsonSyntaxException jsonEx) {
            ErrorLogHelper.RecordException(_callingContext, jsonEx);
        }
        return iotHubSettings;
    }

    private static class DateDeserializer implements JsonDeserializer<Date> {
        public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            String JSONDateToMilliseconds = "\\/(Date\\((.*?)(\\+.*)?\\))\\/";
            Pattern pattern = Pattern.compile(JSONDateToMilliseconds);
            String jsonValue = json.getAsJsonPrimitive().getAsString();
            Matcher matcher = pattern.matcher(jsonValue);
            Date value = null;
            if (matcher.find()) {
                String result = matcher.replaceAll("$2");
                // 8/16/11 JHM - Handle value representing DateTime.MinValue (1/1/0001)
                // Get milliseconds value, compare to 1/1/1900. If <=, set value as Null.
                Long milliseconds;
                if (result.charAt(result.length() - 5) == '-')
                // Strip off appended timezone offset (i.e. -0500)
                {
                    milliseconds = Long.valueOf(result.substring(0, result.length() - 5));
                } else {
                    milliseconds = Long.valueOf(result);
                }

                if (milliseconds > -2208945600000L) {
                    value = new Date(milliseconds);
                }//else reutrn null;
            } else {
                try {
                    value = DateUtility.getHomeTerminalDMOSoapDateTimeFormat().parse(jsonValue);
                } catch (ParseException parseException){
                    throw new JsonParseException("error parsing date", parseException);
                }
            }
            return value;
        }
    }

    public AuthenticationInfo ChangePassword(String password) throws IOException, JsonSyntaxException {

        String URL = String.format(METHODNAME_CHANGEPASSWORD, _baseUrl);

        String result = HTTPURLGetRequest(URL, "newPassword," + password);
        AuthenticationInfo gsonResult = null;
        try {
            gsonResult = _gson.fromJson(result, AuthenticationInfo.class);
        } catch (JsonSyntaxException jsonEx) {
            ErrorLogHelper.RecordException(_callingContext, jsonEx);
        }

        return gsonResult;
    }

    public Date GetDMOTime() throws IOException {
        // ignoreServerTime
        if (GlobalState.getInstance().getFeatureService().getIgnoreServerTime()) {
            return TimeKeeper.getInstance().now();
        }

        String URL = String.format(METHODNAME_GETDMOTIME, _baseUrl);

        String result = HTTPURLGetRequest(URL, null);
        Date dmoTime = null;

        try {
            dmoTime = _gson.fromJson(result, Date.class);
        } catch (JsonSyntaxException jsonEx) {
            ErrorLogHelper.RecordException(_callingContext, jsonEx);
        }

        return dmoTime;
    }

    public String EmployeeNameForCode(String employeeCode) throws IOException {

        String URL = String.format(METHODNAME_EMPLOYEENAMEFORCODE, _baseUrl, AddURLEncoding(employeeCode));

        String result = HTTPURLGetRequest(URL, null);
        String empName = null;

        try {
            empName = _gson.fromJson(result, String.class);
        } catch (JsonSyntaxException jsonEx) {
            ErrorLogHelper.RecordException(_callingContext, jsonEx);
        }

        return empName;
    }

    public String EmployeeNameForKMBUserName(String userName) throws IOException {

        String URL = String.format(METHODNAME_EMPLOYEENAMEFORUSERNAME, _baseUrl, AddURLEncoding(userName));

        String result = HTTPURLGetRequest(URL, null);
        String empName = null;

        try {
            empName = _gson.fromJson(result, String.class);
        } catch (JsonSyntaxException jsonEx) {
            ErrorLogHelper.RecordException(_callingContext, jsonEx);
        }

        return empName;
    }

    public String InitiateFileUpload() throws IOException {

        String URL = String.format(METHODNAME_INITIATEFILEUPLOAD, _baseUrl);

        String result = HTTPURLGetRequest(URL, null);
        String fileDir = null;

        try {
            fileDir = _gson.fromJson(result, String.class);
        } catch (JsonSyntaxException jsonEx) {
            ErrorLogHelper.RecordException(_callingContext, jsonEx);
        }

        return fileDir;
    }

    public KMBEncompassUserList DownloadKmbEncompassUsers() throws IOException, JsonSyntaxException {
        KMBEncompassUserList encompassUserList = null;
        String URL = String.format(METHODNAME_DOWNLOADKMBENCOMPASSUSERS, _baseUrl);
        if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
            URL = convertToLogCheckerMandateUrl(URL);
        }
        try {
            String result = HTTPURLGetRequest(URL, null);

            encompassUserList = _gson.fromJson(result, KMBEncompassUserList.class);
        } catch (JsonSyntaxException jsonEx) {
            ErrorLogHelper.RecordException(_callingContext, jsonEx);
        }
        return encompassUserList;
    }

    public EmployeeLogList DownloadLogsInDateRange(Date startDate, Date endDate, Date changeTimestampUTC, boolean isShortHaulExceptionAllowed) throws IOException, JsonSyntaxException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMddyyyy");
        dateFormat.setTimeZone(DateUtility.getHomeTerminalDateFormat().getTimeZone());

        String changeTimestamp = changeTimestampUTC == null ? "" : new SimpleDateFormat("MM-dd-yyyyHH:mm:ss").format(changeTimestampUTC);
        String URL = String.format(METHODNAME_DOWNLOADLOGSINDATERANGE, _baseUrl, dateFormat.format(startDate), dateFormat.format(endDate), changeTimestamp, isShortHaulExceptionAllowed);

        if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
            URL = convertToLogCheckerMandateUrl(URL);
        }
        String result = HTTPURLGetRequest(URL, null, TIMEOUT_DOWNLOAD_LOGS);
        EmployeeLogList employeeLogList = null;
        try {
            employeeLogList = _gson.fromJson(result, EmployeeLogList.class);
        } catch (JsonSyntaxException jsonEx) {
            ErrorLogHelper.RecordException(_callingContext, jsonEx);
        }

        return employeeLogList;
    }

    public EmployeeRule DownloadEmployeeRuleSettings(Date changeTimestampUTC) throws IOException {

        String changeTimestamp = changeTimestampUTC == null ? "" : new SimpleDateFormat("MM-dd-yyyyHH:mm:ss").format(changeTimestampUTC);
        String URL = String.format(METHODNAME_DOWNLOADEMPLOYEERULESETTINGS, _baseUrl, changeTimestamp);

        String result = HTTPURLGetRequest(URL, null);
        EmployeeRule employeeRule = null;
        try {
            employeeRule = _gson.fromJson(result, EmployeeRule.class);
        } catch (JsonSyntaxException jsonEx) {
            ErrorLogHelper.RecordException(_callingContext, jsonEx);
        }

        return employeeRule;
    }

    public ArrayList<MotionPictureAuthority> DownloadMotionPictureAuthorities() throws IOException {

        String URL = String.format(METHODNAME_DOWNLOADMOTIONPICTUREAUTHORITIES, _baseUrl);
        String result = HTTPURLGetRequest(URL, null);
        MotionPictureAuthority[] motionPictureAuthorities = null;
        try {
            motionPictureAuthorities = _gson.fromJson(result, MotionPictureAuthority[].class);
        } catch (JsonSyntaxException jsonEx) {
            ErrorLogHelper.RecordException(_callingContext, jsonEx);
        }

        ArrayList<MotionPictureAuthority> list = new ArrayList<>(Arrays.asList(motionPictureAuthorities));

        return list;
    }

    public ArrayList<MotionPictureProduction> DownloadMotionPictureProductions() throws IOException {
        String URL = String.format(METHODNAME_DOWNLOADMOTIONPICTUREPRODUCTIONS, _baseUrl);
        String result = HTTPURLGetRequest(URL, null);
        MotionPictureProduction[] motionPictureProductions = null;

        try {
            motionPictureProductions = _gson.fromJson(result, MotionPictureProduction[].class);
        } catch (JsonSyntaxException jsonEx) {
            ErrorLogHelper.RecordException(_callingContext, jsonEx);
        }

        ArrayList<MotionPictureProduction> list = new ArrayList<>(Arrays.asList(motionPictureProductions));

        return list;
    }

    public ArrayList<LocationCode> DownloadLocationCodes(Date changeTimestampUTC) throws IOException {

        String changeTimestamp = changeTimestampUTC == null ? "" : new SimpleDateFormat("MM-dd-yyyyHH:mm:ss").format(changeTimestampUTC);
        String URL = String.format(METHODNAME_DOWNLOADLOCATIONCODES, _baseUrl, changeTimestamp);

        String result = HTTPURLGetRequest(URL, null);

        LocationCode[] gpsLocationList = _gson.fromJson(result, LocationCode[].class);

        ArrayList<LocationCode> list = new ArrayList<LocationCode>(Arrays.asList(gpsLocationList));

        return list;
    }

    public ArrayList<LogCheckerComplianceDates> DownloadLogCheckerComplianceDates(Date complianceDate) throws IOException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyyHH:mm:ss");
        dateFormat.setTimeZone(DateUtility.getHomeTerminalDateFormat().getTimeZone());

        String URL = String.format(METHODNAME_DOWNLOADLOGCHECKERCOMPLIANCEDATES, _baseUrl, dateFormat.format(complianceDate));

        String result = HTTPURLGetRequest(URL, null);
        LogCheckerComplianceDates[] complianceDates = null;
        try {
            complianceDates = _gson.fromJson(result, LogCheckerComplianceDates[].class);
        } catch (JsonSyntaxException jsonEx) {
            ErrorLogHelper.RecordException(_callingContext, jsonEx);
        }

        ArrayList<LogCheckerComplianceDates> list = new ArrayList<LogCheckerComplianceDates>(Arrays.asList(complianceDates));

        return list;
    }

    public ArrayList<EobrDiagnosticCommand> GetPendingEOBRDiagnosticCommands(String serialNumber) throws IOException {

        String URL = String.format(METHODNAME_GETPENDINGEOBRDIAGNOSTICCOMMANDS, _baseUrl, serialNumber);

        String result = HTTPURLGetRequest(URL, null);
        EobrDiagnosticCommand[] eobrCommands = null;
        try {
            eobrCommands = _gson.fromJson(result, EobrDiagnosticCommand[].class);
        } catch (JsonSyntaxException jsonEx) {
            ErrorLogHelper.RecordException(_callingContext, jsonEx);
        }

        ArrayList<EobrDiagnosticCommand> list = null;

        if (eobrCommands != null) {
            list = new ArrayList<EobrDiagnosticCommand>(Arrays.asList(eobrCommands));
        }

        return list;
    }

    public void SubmitEOBRDiagnosticResults(EobrDiagnosticCommand eobrCommand) throws IOException {

        String URL = String.format(METHODNAME_SUBMITEOBRDIAGNOSTICRESULTS, _baseUrl);
        String json = _gson.toJson(eobrCommand);

        HTTPURLPostRequest(URL, false, null, json);
    }

    public void SubmitEmployeeLogReportRequest(EmployeeLogReportRequest employeeLogReportRequest) throws IOException {

        String URL = String.format(METHODNAME_SUBMITEMPLOYEELOGREPORTREQUEST, _baseUrl);

        String json = _gson.toJson(employeeLogReportRequest);

        HTTPURLPostRequest(URL, false, null, json);
    }

    public EobrConfiguration DownloadEobrConfiguration(String serialNumber, Date changeTimestampUTC) throws IOException {

        String changeTimestamp = changeTimestampUTC == null ? "" : new SimpleDateFormat("MM-dd-yyyyHH:mm:ss").format(changeTimestampUTC);
        String URL = String.format(METHODNAME_DOWNLOADEOBRCONFIGURATION, _baseUrl, AddURLEncoding(serialNumber), changeTimestamp);

        String result = HTTPURLGetRequest(URL, null);
        EobrConfiguration eobrConfig = null;
        try {
            eobrConfig = _gson.fromJson(result, EobrConfiguration.class);
        } catch (JsonSyntaxException jsonEx) {
            ErrorLogHelper.RecordException(_callingContext, jsonEx);
        }

        return eobrConfig;
    }

    public ArrayList<LogRemarkItem> DownloadLogRemarkItems(Date changeDate) throws IOException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyyHH:mm:ss");
        dateFormat.setTimeZone(DateUtility.getHomeTerminalDateFormat().getTimeZone());

        String URL = String.format(METHODNAME_DOWNLOADLOGREMARKITEMS, _baseUrl, dateFormat.format(changeDate));

        String result = HTTPURLGetRequest(URL, null);
        LogRemarkItem[] logRemarkItems = null;
        try {
            logRemarkItems = _gson.fromJson(result, LogRemarkItem[].class);
        } catch (JsonSyntaxException jsonEx) {
            ErrorLogHelper.RecordException(_callingContext, jsonEx);
        }

        ArrayList<LogRemarkItem> list = null;

        if (logRemarkItems != null) {
            list = new ArrayList<LogRemarkItem>(Arrays.asList(logRemarkItems));
        }

        return list;
    }

    public VehicleInspection DownloadRecentVehicleInspection(String serialNumber, Date changeTimestampUTC) throws IOException {

        String changeTimestamp = new SimpleDateFormat("MM-dd-yyyyHH:mm:ss").format(new Date());
        String URL = String.format(METHODNAME_DOWNLOADRECENTVEHICLEINSPECTION, _baseUrl, AddURLEncoding(serialNumber), changeTimestamp);

        String result = HTTPURLGetRequest(URL, null);

        VehicleInspection vehicleInspection = null;
        try {
            vehicleInspection = _gson.fromJson(result, VehicleInspection.class);
        } catch (JsonSyntaxException jsonEx) {
            ErrorLogHelper.RecordException(_callingContext, jsonEx);
        }

        return vehicleInspection;
    }

    public VehicleInspection DownloadRecentVehiclePreInspection(String serialNumber, Date changeTimestampUTC) throws IOException {

        String changeTimestamp = changeTimestampUTC == null ? "" : new SimpleDateFormat("MM-dd-yyyyHH:mm:ss").format(changeTimestampUTC);
        String URL = String.format(METHODNAME_DOWNLOADRECENTVEHICLEPREINSPECTION, _baseUrl, AddURLEncoding(serialNumber), changeTimestamp);

        String result = HTTPURLGetRequest(URL, null);

        VehicleInspection vehicleInspection = null;
        try {
            vehicleInspection = _gson.fromJson(result, VehicleInspection.class);
        } catch (JsonSyntaxException jsonEx) {
            ErrorLogHelper.RecordException(_callingContext, jsonEx);
        }

        return vehicleInspection;
    }

    public boolean CheckForOpenDVIR(String eobrSerialNumber, Date changeTimestampUTC) throws IOException {
        // Build URL
        // if return is true, there is an open DVIR
        boolean check = true;
        String changeTimestamp = changeTimestampUTC == null ? "" : new SimpleDateFormat("MM-dd-yyyyHH:mm:ss").format(changeTimestampUTC);
        String URL = String.format(METHODNAME_CHECKFOROPENDVIR, _baseUrl, AddURLEncoding(eobrSerialNumber), changeTimestamp);

        String result = HTTPURLGetRequest(URL, null);
        try {
            check = Boolean.parseBoolean(result);
        } catch (JsonSyntaxException jsonEx) {
            ErrorLogHelper.RecordException(_callingContext, jsonEx);
        }
        return check;
    }

    public VehicleInspection DownloadRecentTrailerInspection(String unitCodeForTrailer, Date changeTimestampUTC) throws IOException {

        String changeTimestamp = changeTimestampUTC == null ? "" : new SimpleDateFormat("MM-dd-yyyyHH:mm:ss").format(changeTimestampUTC);
        String URL = String.format(METHODNAME_DOWNLOADRECENTTRAILERINSPECTION, _baseUrl, AddURLEncoding(unitCodeForTrailer), changeTimestamp);

        String result = HTTPURLGetRequest(URL, null);

        return parseJsonResponse(result, VehicleInspection.class);
    }

    public VehicleInspection DownloadRecentTrailerPreInspection(String unitCodeForTrailer, Date changeTimestampUTC) throws IOException {

        String changeTimestamp = changeTimestampUTC == null ? "" : new SimpleDateFormat("MM-dd-yyyyHH:mm:ss").format(changeTimestampUTC);
        String URL = String.format(METHODNAME_DOWNLOADRECENTTRAILERPREINSPECTION, _baseUrl, AddURLEncoding(unitCodeForTrailer), changeTimestamp);

        String result = HTTPURLGetRequest(URL, null);

        return parseJsonResponse(result, VehicleInspection.class);
    }

    /**
     * Calls the REST API to get the License Plate Number for the Unit currently associated to the active EOBR
     */
    public String GetEobrUnitLicensePlateNumber(String eobrSerialNumber) throws IOException {

        String URL = String.format(METHODNAME_GETEOBRUNITLICENSEPLATENUMBER, _baseUrl, AddURLEncoding(eobrSerialNumber));

        String result = HTTPURLGetRequest(URL, null);

        String licensePlate = null;
        try {
            licensePlate = _gson.fromJson(result, String.class);
        } catch (JsonSyntaxException jsonEx) {
            ErrorLogHelper.RecordException(_callingContext, jsonEx);
        }

        return licensePlate;
    }

    public void SubmitRoutePositions(RoutePositionList routePositions) throws IOException {

        String URL = String.format(METHODNAME_SUBMITROUTEPOSITIONS, _baseUrl);

        IotDataSerialization serialization = new IotDataSerialization();

        // Break this out as KMB Rest expects an Array, but Data Services has a
        // RoutePositionList object
        RoutePosition[] positions = routePositions.getRoutePositions();

        HTTPURLPostRequest(URL, false, null, serialization.getGson().toJsonTree(positions).toString());
    }

    public void SubmitUnassignedDrivingPeriods(UnassignedDrivingPeriod[] periods) throws IOException {

        String URL = String.format(METHODNAME_SUBMITUNASSIGNEDDRIVINGPERIODS, _baseUrl);

        HTTPURLPostRequest(URL, false, null, _gson.toJsonTree(periods).toString());
    }

    public void SubmitUnassignedEobrFailurePeriods(UnassignedEobrFailurePeriod[] periods) throws IOException {

        String URL = String.format(METHODNAME_SUBMITUNASSIGNEDEOBRFAILUREPERIODS, _baseUrl);

        HTTPURLPostRequest(URL, false, null, _gson.toJsonTree(periods).toString());
    }

    public void SubmitUnidentifiedEldEvents(EmployeeLogEldEvent[] eldEvents) throws IOException {
        String url = convertToLogCheckerMandateUrl(String.format(METHODNAME_SUBMITUNASSIGNEDELDEVENTS, _baseUrl));
        HTTPURLPostRequest(url, false, null, _gson.toJson(eldEvents));
    }

    public EmployeeLogEldEventList GetUnidentifiedEldEventsInDateRange(Date startDate, Date endDate) throws IOException, JsonSyntaxException {

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMddyyyy");
        dateFormat.setTimeZone(DateUtility.getHomeTerminalDateFormat().getTimeZone());

        String URL = String.format(METHODNAME_DOWNLOADUNIDENTIFIEDEVENTSINDATERANGE, _baseUrl, dateFormat.format(startDate), dateFormat.format(endDate));

        if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
            URL = convertToLogCheckerMandateUrl(URL);
        }

        String result = HTTPURLGetRequest(URL, null);

        EmployeeLogEldEventList events = null;
        try {
            events = _gson.fromJson(result, EmployeeLogEldEventList.class);
        } catch (JsonSyntaxException jsonEx) {
            ErrorLogHelper.RecordException(_callingContext, jsonEx);
        }

        return events;
    }

    public EmployeeLogUnidentifiedELDEventStatus[] GetSubmittedUnidentifiedEldEventStatus(Long clusterPKs[]) throws IOException, JsonSyntaxException {

        IotDataSerialization serialization = new IotDataSerialization();

        String URL = String.format(METHODNAME_GETSTATUSOFUNIDENTIFIEDELDEVENTS, _baseUrl, serialization.getGson().toJsonTree(clusterPKs).toString());

        if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
            URL = convertToLogCheckerMandateUrl(URL);
        }
        String result = HTTPURLPostRequest(URL, false, null, serialization.getGson().toJsonTree(clusterPKs).toString());

        EmployeeLogUnidentifiedELDEventStatus[] statuses = null;
        try {
            statuses = _gson.fromJson(result, EmployeeLogUnidentifiedELDEventStatus[].class);
        } catch (JsonSyntaxException jsonEx) {
            ErrorLogHelper.RecordException(_callingContext, jsonEx);
        }

        return statuses;
    }

    public List<UnassignedDrivingPeriod> GetDisconnectedDrivingPeriods(String eobrSerialNumber, String startTimeUTC) throws IOException {

        String URL = String.format(METHODNAME_GETDISCONNECTEDDRIVINGPERIODS, _baseUrl, eobrSerialNumber, startTimeUTC);
        String result = HTTPURLGetRequest(URL, null);
        UnassignedDrivingPeriod[] unassignedDrivingPeriods = null;
        try {
            unassignedDrivingPeriods = _gson.fromJson(result, UnassignedDrivingPeriod[].class);
        } catch (JsonSyntaxException jsonEx) {
            ErrorLogHelper.RecordException(_callingContext, jsonEx);
        }

        return Arrays.asList(unassignedDrivingPeriods);
    }

    public EmployeeLogEldEventList GetGeotabUnidentfiedELDEvents(String eobrSerialNumber) throws IOException
    {
        String URL = convertToLogCheckerMandateUrl(String.format(METHODNAME_GETGEOTABUNIDENTIFIEDEVENTS, _baseUrl, eobrSerialNumber));
        String result = HTTPURLGetRequest(URL, null);

        EmployeeLogEldEventList events = null;
        try {
            events = _geotabUnidentifiedEmployeeLogEldEventGson.fromJson(result, EmployeeLogEldEventList.class);
        } catch (JsonSyntaxException jsonEx) {
            ErrorLogHelper.RecordException(_callingContext, jsonEx);
        }

        return events;
    }

    public KMBUnassignedPeriodIsClaimable[] ClaimDisconnectedDrivingPeriods(String[] drivingPeriodIds) throws IOException {
        String URL = String.format(METHODNAME_CLAIMDISCONNECTEDDRIVINGPERIODS, _baseUrl);

        ListProxy list = new ListProxy();
        list.setList(drivingPeriodIds);

        String json = _gson.toJson(list);
        String result = HTTPURLPostRequest(URL, false, null, json);

        KMBUnassignedPeriodIsClaimable[] periods = _gson.fromJson(result, KMBUnassignedPeriodIsClaimable[].class);

        return periods;
    }

    public void SubmitDrivingEventReassignmentRequest(DrivingEventReassignmentMapping reassignment) throws IOException {
        String URL = convertToLogCheckerMandateUrl(String.format(METHODNAME_SUBMITREASSIGNMENTREQUEST, _baseUrl));
        String json = _gson.toJson(reassignment, DrivingEventReassignmentMapping.class);
        HTTPURLPostRequest(URL, false, null, json);
    }

    public void RecordConditionalFirmwareUpgrade(ConditionalFirmwareUpgrade cfu) throws IOException {
        String URL = String.format(METHODNAME_RECORDCONDITIONALFIRMWAREUPGRADE, _baseUrl);

        try {
            String json = _gson.toJson(cfu, ConditionalFirmwareUpgrade.class);
            HTTPURLPostRequest(URL, true, null, json);
        } catch (JsonSyntaxException jsonEx) {
            ErrorLogHelper.RecordException(_callingContext, jsonEx);
        }
    }

    @Override
    public FirmwareVersion CheckForFirmwareUpdate(String eobrSerialNumber, int majorVersion,
                                                  int minorVersion) throws IOException {
        String URL = String.format(METHODNAME_CHECKFORFIRMWAREUPDATE, _baseUrl, eobrSerialNumber, majorVersion, minorVersion);

        String result = HTTPURLGetRequest(URL, null);

        return parseJsonResponse(result, FirmwareVersion.class);
    }

    public FmcsaEldRegistrationInfo[] DownloadFmcsaEldRegistrationInfo(Date updateDate) throws IOException {

        String updateDateFormatted = updateDate == null ? "" : new SimpleDateFormat("MM-dd-yyyyHH:mm:ss").format(updateDate);

        String url = String.format(METHODNAME_DOWNLOADFMCSAELDREGISTRATIONINFO, _baseUrl, updateDateFormatted);

        String result = HTTPURLGetRequest(url, null);

        return _gson.fromJson(result, FmcsaEldRegistrationInfo[].class);
    }

    public FmcsaEldProviderInfo DownloadFmcsaEldProviderInfo() throws IOException {
        String url = String.format(METHODNAME_DOWNLOADFMCSAELDPROVIDERINFO, _baseUrl);

        String result = HTTPURLGetRequest(url, null);

        return parseJsonResponse(result, FmcsaEldProviderInfo.class);
    }

    private <T extends ProxyBase> T parseJsonResponse(String result, Class<T> type) {
        T parsedResult = null;
        try {
            parsedResult = _gson.fromJson(result, type);
        } catch (JsonSyntaxException jsonEx) {
            ErrorLogHelper.RecordException(_callingContext, jsonEx);
        } catch (JsonParseException jpe) {
            // when connected to a network, but unable to get to webservice "e"
            // is null
            if (jpe == null) {
                jpe = new JsonParseException(JsonParseException.class.getName());
            }
            ErrorLogHelper.RecordException(_callingContext, jpe);
        }
        return parsedResult;
    }

    public byte[] DownloadFirmwareUpdate(String firmwareVersionId) throws IOException {
        String URL = String.format(METHODNAME_DOWNLOADFIRMWAREUPDATE, _baseUrl, firmwareVersionId);
        return HTTPURLByteStreamGetRequest(URL, null);
    }

    private static class NullHostNameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            Log.i("RestUtilImpl", "Approving certificate for " + hostname);
            return true;
        }
    }

    private class DrivingEventReassignmentMappingDeserializer implements JsonDeserializer<DrivingEventReassignmentMapping> {
        @Override
        public DrivingEventReassignmentMapping deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObj = json.getAsJsonObject();
            DrivingEventReassignmentMapping mapping = new DrivingEventReassignmentMapping();
            mapping.setEldEvent(_gson.fromJson(GetElement(jsonObj, "EldEvent"), EmployeeLogEldEvent.class));
            mapping.setDriverToAssignEventTo(GetString(jsonObj, "DriverToAssignEventTo"));
            mapping.setEventComment(GetString(jsonObj, "EventComment"));
            return mapping;
        }
    }

    private class AuthenticationInfoDeserializer implements JsonDeserializer<AuthenticationInfo> {
        public AuthenticationInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObj = json.getAsJsonObject();
            AuthenticationInfo o = new AuthenticationInfo();
            o.setIsAuthenticated(GetBoolean(jsonObj, "IsAuthenticated", o.getIsAuthenticated()));
            o.setEmployeeId(GetString(jsonObj, "EmployeeId"));
            o.setEmployeeFullName(GetString(jsonObj, "EmployeeFullName"));
            o.setEmployeeCode(GetString(jsonObj, "EmployeeCode"));
            o.setRequiredToChangePassword(GetBoolean(jsonObj, "RequiredToChangePassword", o.getRequiredToChangePassword()));
            o.setIsClientAppVersionCurrent(GetBoolean(jsonObj, "IsClientAppVersionCurrent", o.getIsClientAppVersionCurrent()));

            o.setHomeTerminalDOTNumber(GetString(jsonObj, "HomeTerminalDOTNumber"));
            o.setHomeTerminalAddressLine1(GetString(jsonObj, "HomeTerminalAddressLine1"));
            o.setHomeTerminalAddressLine2(GetString(jsonObj, "HomeTerminalAddressLine2"));
            o.setHomeTerminalCity(GetString(jsonObj, "HomeTerminalCity"));
            o.setHomeTerminalStateAbbrev(GetString(jsonObj, "HomeTerminalStateAbbrev"));
            o.setHomeTerminalZipCode(GetString(jsonObj, "HomeTerminalZipCode"));

            o.setDriverLicenseNumber(GetString(jsonObj, "DriverLicenseNumber"));
            o.setDriverLicenseState(GetString(jsonObj, "DriverLicenseState"));
            o.setFirstName(GetString(jsonObj, "FirstName"));
            o.setLastName(GetString(jsonObj, "LastName"));

            // format clock sync time as a UTC time
            SimpleDateFormat dmoSoapDateFormat = DateUtility.getDMOSoapDateTimeFormat();
            dmoSoapDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

            o.setClockSyncTimestamp(GetFormattedDate(jsonObj, "ClockSyncTimestamp", dmoSoapDateFormat));

            // Deserialize the EmployeeRule object contained within
            // AuthenticationInfo
            JsonElement empRuleJson = GetElement(jsonObj, "EmployeeRule");
            EmployeeRule empRuleObj = _gson.fromJson(empRuleJson, EmployeeRule.class);
            o.setEmployeeRule(empRuleObj);

            return o;
        }
    }

    private class ApplicationUpdateInfoDeserializer implements JsonDeserializer<ApplicationUpdateInfo> {
        public ApplicationUpdateInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObj = json.getAsJsonObject();
            ApplicationUpdateInfo o = new ApplicationUpdateInfo();

            o.setDownloadUrl(GetString(jsonObj, "DownloadUrl"));
            o.setIncludesFirmwareUpdate(GetBoolean(jsonObj, "IncludesFirmwareUpdate", o.getIncludesFirmwareUpdate()));
            o.setIsAvailable(GetBoolean(jsonObj, "IsAvailable", o.getIsAvailable()));
            o.setNewVersion(GetString(jsonObj, "NewVersion"));
            o.setWifiRequired(GetBoolean(jsonObj, "RequiresWifi", true));

            return o;
        }
    }

    private class EmployeeRuleDeserializer implements JsonDeserializer<EmployeeRule> {
        public EmployeeRule deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            EmployeeRule o = new EmployeeRule();
            JsonObject jsonObj = json.getAsJsonObject();

            o.setHomeTerminalTimeZone((TimeZoneEnum) GetEnum(jsonObj, "HomeTerminalTimeZone", TimeZoneEnum.class));
            o.setDriverType((DriverTypeEnum) GetEnum(jsonObj, "DriverType", DriverTypeEnum.class));
            o.setRuleset((RuleSetTypeEnum) GetEnum(jsonObj, "RulesetTypeEnum", RuleSetTypeEnum.class));
            o.setIsShortHaulException(GetBoolean(jsonObj, "IsShortHaulException", o.getIsShortHaulException()));
            o.setIs34HourResetAllowed(GetBoolean(jsonObj, "Is34HourResetAllowed", o.getIs34HourResetAllowed()));
            o.setDrivingStartDistanceMiles(GetFloat(jsonObj, "DrivingStartDistanceMiles", o.getDrivingStartDistanceMiles()));
            o.setDrivingStopTimeMinutes(GetInteger(jsonObj, "DrivingStopTimeMinutes", o.getDrivingStopTimeMinutes()));
            o.setAdditionalRulesets(ParseRuleSetTypeEnumArray(GetArray(jsonObj, "AdditionalRulesets")));
            o.setIntCDRuleset((RuleSetTypeEnum) GetEnum(jsonObj, "InternationalCDRuleset", RuleSetTypeEnum.class));
            o.setIntUSRuleset((RuleSetTypeEnum) GetEnum(jsonObj, "InternationalUSRuleset", RuleSetTypeEnum.class));
            o.setDataProfile((DataProfileEnum) GetEnum(jsonObj, "DataProfile", DataProfileEnum.class));
            o.setIsHaulingExplosivesAllowed(GetBoolean(jsonObj, "IsHaulingExplosivesAllowed", o.getIsHaulingExplosivesAllowed()));
            o.setIsHaulingExplosivesDefault(GetBoolean(jsonObj, "IsHaulingExplosivesDefault", o.getIsHaulingExplosivesDefault()));
            o.setIsOperatesSpecificVehiclesForOilfield(GetBoolean(jsonObj, "IsOperatesSpecificVehiclesForOilField", o.getIsOperatesSpecificVehiclesForOilfield()));
            o.setIsPersonalConveyanceAllowed(GetBoolean(jsonObj, "IsPersonalConveyanceAllowed", o.getIsPersonalConveyanceAllowed()));
            o.setIsHyrailUseAllowed(GetBoolean(jsonObj, "IsHyrailAllowed", o.getIsHyrailUseAllowed()));
            o.setIsNonRegDrivingAllowed(GetBoolean(jsonObj, "IsNonRegDrivingAllowed", o.getIsNonRegDrivingAllowed()));
            o.setIsMobileExemptLogAllowed(GetBoolean(jsonObj, "IsMobileExemptLogAllowed", o.getIsMobileExemptLogAllowed()));
            o.setExemptLogType((ExemptLogTypeEnum) GetEnum(jsonObj, "ExemptLogType", ExemptLogTypeEnum.class));
            o.setIsExemptFrom30MinBreakRequirement((GetBoolean(jsonObj, "IsExemptFrom30MinBreakRequirement", o.getIsExemptFrom30MinBreakRequirement())));

            // 2/17/12 JHM - Check the Distance Units value for null so it
            // doesn't result in the insert to the DB failing.
            if (GetString(jsonObj, "DistanceUnits") != null) {
                o.setDistanceUnits(GetString(jsonObj, "DistanceUnits"));
            }

            o.setExemptFromEldUse(GetBoolean(jsonObj, "ExemptFromEldUse", o.getExemptFromEldUse()));
            if (GetString(jsonObj, "ExemptFromEldUseComment") != null) {
                o.setExemptFromEldUseComment(GetString(jsonObj, "ExemptFromEldUseComment"));
            }
            o.setDriveStartSpeed(GetInteger(jsonObj, "DriveStartSpeed", o.getDriveStartSpeed()));
            o.setMandateDrivingStopTimeMinutes(GetInteger(jsonObj, "MandateDrivingStopTimeMinutes", o.getMandateDrivingStopTimeMinutes()));
            o.setYardMoveAllowed(GetBoolean(jsonObj, "YardMoveAllowed", o.getYardMoveAllowed()));

            return o;
        }
    }

    private class CompanyConfigSettingsDeserializer implements JsonDeserializer<CompanyConfigSettings> {
        public CompanyConfigSettings deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            CompanyConfigSettings o = new CompanyConfigSettings();
            JsonObject jsonObj = json.getAsJsonObject();

            o.setDmoCompanyName(GetString(jsonObj, "DmoCompanyName"));
            o.setDmoCompanyId(GetString(jsonObj, "DmoCompanyId"));
            o.setDmoUsername(GetString(jsonObj, "DmoUsername"));
            o.setDmoPasswordEncrypt(GetString(jsonObj, "DmoPasswordEncrypt"));

            o.setDailyLogStartTime(GetString(jsonObj, "DailyLogStartTime"));
            o.setLogPurgeDayCount(GetInteger(jsonObj, "LogPurgeDayCount", o.getLogPurgeDayCount()));
            o.setEobrDiscoveryPasskey(GetString(jsonObj, "EobrDiscoveryPasskey"));
            o.setEobrCommunicationMode((EobrCommunicationModeEnum) GetEnum(jsonObj, "EobrCommunicationMode", EobrCommunicationModeEnum.class));
            o.setEobrSleepModeMinutes(GetInteger(jsonObj, "EobrSleepModeMinutes", o.getEobrSleepModeMinutes()));
            o.setEobrDataCollectionRateSeconds(GetInteger(jsonObj, "EobrDataCollectionRateSeconds", o.getEobrDataCollectionRateSeconds()));
            o.setAllowDriversCompleteDVIR(GetBoolean(jsonObj, "AllowDriversCompleteDVIR", o.getAllowDriversCompleteDVIR()));
            o.setGeneratePreTripDVIRWithDefectAlert(GetBoolean(jsonObj, "GeneratePreTripDVIRWithDefectAlert", o.getGeneratePreTripDVIRWithDefectAlert()));
            o.setActivationCode(GetString(jsonObj, "ActivationCode"));
            o.setDriverStartDistance(GetFloat(jsonObj, "DriverStartDistance", o.getDriverStartDistance()));
            o.setDriverStopMinutes(GetInteger(jsonObj, "DriverStopMinutes", o.getDriverStopMinutes()));
            o.setMaxAcceptableSpeed(GetFloat(jsonObj, "MaxAcceptableSpeed", o.getMaxAcceptableSpeed()));
            o.setMaxAcceptableTach(GetInteger(jsonObj, "MaxAcceptableTach", o.getMaxAcceptableTach()));
            o.setHardBrakeDecelerationSpeed(GetFloat(jsonObj, "HardBrakeDecelerationSpeed", o.getHardBrakeDecelerationSpeed()));
            o.setUseKmbWebApiServices(GetBoolean(jsonObj, "UseKmbWebApiServices", o.getUseKmbWebApiServices()));
            o.setMultipleUsersAllowed(GetBoolean(jsonObj, "MultipleUsersAllowed", o.getMultipleUsersAllowed()));
            o.setDriveStartSpeed(GetInteger(jsonObj, "DriveStartSpeed", o.getDriveStartSpeed()));
            o.setMandateDrivingStopTimeMinutes(GetInteger(jsonObj, "MandateDrivingStopTimeMinutes", o.getMandateDrivingStopTimeMinutes()));
            o.setIsGeotabEnabled(GetBoolean(jsonObj, "IsGeotabEnabled", o.getIsGeotabEnabled()));
            o.setIsMotionPictureEnabled(GetBoolean(jsonObj, "IsMotionPictureEnabled", o.getIsMotionPictureEnabled()));
            o.setIsAutoAssignUnIdentifiedEvents(GetBoolean(jsonObj, "IsAutoAssignUnIdentifiedEvents", o.getIsAutoAssignUnIdentifiedEvents()));
            return o;
        }
    }

    private static class CreateIoTHubSettingsCommandSerializer implements JsonSerializer<CreateIoTHubSettingsCommand> {
        @Override
        public JsonElement serialize(CreateIoTHubSettingsCommand src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObj = new JsonObject();
            jsonObj.addProperty("DeviceId", src.getDeviceId());
            return jsonObj;
        }
    }

    private static class IoTHubSettingsDeserializer implements JsonDeserializer<IoTHubSettings> {
        @Override
        public IoTHubSettings deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObj = json.getAsJsonObject();
            IoTHubSettings settings = new IoTHubSettings();
            settings.setDeviceId(GetString(jsonObj, "DeviceId"));
            settings.setToken(GetString(jsonObj, "Token"));
            DateTime expirationUtc = GetDateTime(jsonObj, "ExpirationUtc");
            if (expirationUtc == null) {
                settings.setExpirationUtc(null);
            } else {
                settings.setExpirationUtc(expirationUtc.toInstant());
            }
            settings.setEventsUri(GetString(jsonObj, "EventsUri"));
            return settings;
        }
    }

    private class MotionPictureAuthorityDeserializer implements JsonDeserializer<MotionPictureAuthority> {
        public MotionPictureAuthority deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            MotionPictureAuthority o = new MotionPictureAuthority();
            JsonObject jsonObj = json.getAsJsonObject();

            o.setMotionPictureAuthorityId(GetString(jsonObj, "MotionPictureAuthorityId"));
            o.setName(GetString(jsonObj, "Name"));
            o.setAddressLine1(GetString(jsonObj, "AddressLine1"));
            o.setAddressLine2(GetString(jsonObj, "AddressLine2"));
            o.setCity(GetString(jsonObj, "City"));
            o.setState(GetString(jsonObj, "State"));
            o.setZipCode(GetString(jsonObj, "ZipCode"));
            o.setBusinessHours(GetString(jsonObj, "BusinessHours"));
            o.setDOTNumber(GetString(jsonObj, "DOTNumber"));
            o.setCompanyKey(0);
            o.setIsActive(GetBoolean(jsonObj, "IsActive", o.getIsActive()));
            return o;
        }
    }

    private class UnassignedDrivingPeriodDeserializer implements JsonDeserializer<UnassignedDrivingPeriod> {
        @Override
        public UnassignedDrivingPeriod deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            UnassignedDrivingPeriod unassignedDrivingPeriod = new UnassignedDrivingPeriod();
            JsonObject jsonObject = json.getAsJsonObject();

            unassignedDrivingPeriod.setEobrId(GetString(jsonObject, "EobrIdentifier"));
            unassignedDrivingPeriod.setEobrSerialNumber(GetString(jsonObject, "KMBEobrDeviceId"));
            Date startTime = GetFormattedDate(jsonObject, "StartTime", DateUtility.getHomeTerminalDMOSoapDateTimeFormatUTC());
            Date stopTime = GetFormattedDate(jsonObject, "StopTime", DateUtility.getHomeTerminalDMOSoapDateTimeFormatUTC());
            unassignedDrivingPeriod.setStartTime(startTime);
            unassignedDrivingPeriod.setStopTime(stopTime);
            unassignedDrivingPeriod.setDistance(GetFloat(jsonObject, "DrivingDistance", 0.0F));

            float startLatitudeDegrees = GetFloat(jsonObject, "StartLatitudeDegrees", 0.0F);
            float startLongitudeDegrees = GetFloat(jsonObject, "StartLongitudeDegrees", 0.0F);
            GpsLocation startLocation = new GpsLocation(startTime, startLatitudeDegrees, startLongitudeDegrees);
            unassignedDrivingPeriod.setStartLocation(startLocation);

            float stopLatitudeDegrees = GetFloat(jsonObject, "StopLatitudeDegrees", 0.0F);
            float stopLongitudeDegrees = GetFloat(jsonObject, "StopLongitudeDegrees", 0.0F);
            GpsLocation stopLocation = new GpsLocation(stopTime, stopLatitudeDegrees, stopLongitudeDegrees);
            unassignedDrivingPeriod.setStopLocation(stopLocation);

            unassignedDrivingPeriod.setEncompassId(GetString(jsonObject, "Id"));
            String EmployeeLogId = GetString(jsonObject, "EmployeeLogId");
            unassignedDrivingPeriod.setIsClaimed(EmployeeLogId != null && !EmployeeLogId.isEmpty());
            return unassignedDrivingPeriod;
        }
    }

    private class KMBUnassignedPeriodIsClaimableDeserializer implements JsonDeserializer<KMBUnassignedPeriodIsClaimable> {
        @Override
        public KMBUnassignedPeriodIsClaimable deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();

            KMBUnassignedPeriodIsClaimable claimable = new KMBUnassignedPeriodIsClaimable();
            claimable.setId(GetString(jsonObject, "Id"));
            claimable.setClaimable(GetBoolean(jsonObject, "IsClaimable", false));

            return claimable;
        }
    }

    private class GenericArrayDeserializer<T> implements JsonDeserializer<T[]> {
        private Class clazz;

        public <T> GenericArrayDeserializer(Class<T> clazz) {
            this.clazz = clazz;
        }

        @Override
        public T[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonArray array = json.getAsJsonArray();

            T[] list = (T[]) Array.newInstance(clazz, array.size());
            for (int i = 0; i < list.length; i++) {
                list[i] = (T) (_gson.fromJson(array.get(i), clazz));
            }

            return list;
        }
    }

    private class ListProxySerializer<T extends ListProxy> implements JsonSerializer<T> {

        @Override
        public JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject o = new JsonObject();

            try {
                Object array = src.getList();
                if (array != null) {
                    JsonElement jsonArray = _gson.toJsonTree(array, array.getClass());
                    o.add("List", jsonArray);
                }
            } catch (Exception e) {
                return o;
            }

            return o;
        }
    }

    private class UnassignedDrivingPeriodArrayDeserializer implements JsonDeserializer<UnassignedDrivingPeriod[]> {

        @Override
        public UnassignedDrivingPeriod[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonArray array = json.getAsJsonArray();

            UnassignedDrivingPeriod[] list = new UnassignedDrivingPeriod[array.size()];
            for (int i = 0; i < list.length; i++) {
                list[i] = (_gson.fromJson(array.get(i), UnassignedDrivingPeriod.class));
            }

            return list;
        }
    }

    private class DrivingEventReassignmentMappingSerializer implements JsonSerializer<DrivingEventReassignmentMapping> {
        @Override
        public JsonElement serialize(DrivingEventReassignmentMapping src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObj = new JsonObject();
            jsonObj.add("EldEvent", _gson.toJsonTree(src.getEldEvent(), EmployeeLogEldEvent.class));
            jsonObj.addProperty("DriverToAssignEventTo", src.getDriverToAssignEventTo());
            jsonObj.addProperty("EventComment", src.getEventComment());
            return jsonObj;
        }
    }

    private class MotionPictureAuthoritySerializer implements JsonSerializer<MotionPictureAuthority> {
        public JsonElement serialize(MotionPictureAuthority src, Type typeOfT, JsonSerializationContext context) {
            JsonObject o = new JsonObject();

            o.addProperty("MotionPictureAuthorityId", src.getMotionPictureAuthorityId());
            o.addProperty("Name", src.getName());
            o.addProperty("AddressLine1", src.getAddressLine1());
            o.addProperty("AddressLine2", src.getAddressLine2());
            o.addProperty("City", src.getCity());
            o.addProperty("State", src.getState());
            o.addProperty("ZipCode", src.getZipCode());
            o.addProperty("BusinessHours", src.getBusinessHours());
            o.addProperty("DOTNumber", src.getDOTNumber());
            o.addProperty("CompanyKey", src.getCompanyKey());
            o.addProperty("IsActive", src.getIsActive());

            return o;
        }
    }

    private class MotionPictureAuthorityArrayDeserializer implements JsonDeserializer<MotionPictureAuthority[]> {
        public MotionPictureAuthority[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

            JsonArray array = json.getAsJsonArray();

            MotionPictureAuthority[] list = new MotionPictureAuthority[array.size()];
            for (int i = 0; i < list.length; i++) {
                list[i] = (_gson.fromJson(array.get(i), MotionPictureAuthority.class));
            }

            return list;
        }
    }


    private static class MotionPictureProductionDeserializer implements JsonDeserializer<MotionPictureProduction> {
        public MotionPictureProduction deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            MotionPictureProduction o = new MotionPictureProduction();
            JsonObject jsonObj = json.getAsJsonObject();

            o.setMotionPictureProductionId(GetString(jsonObj, "MotionPictureProductionId"));
            o.setName(GetString(jsonObj, "Name"));
            o.setAddressLine1(GetString(jsonObj, "AddressLine1"));
            o.setAddressLine2(GetString(jsonObj, "AddressLine2"));
            o.setCity(GetString(jsonObj, "City"));
            o.setState(GetString(jsonObj, "State"));
            o.setZipCode(GetString(jsonObj, "ZipCode"));
            o.setBusinessHours(GetString(jsonObj, "BusinessHours"));
            o.setMotionPictureAuthorityId(GetString(jsonObj, "MotionPictureAuthorityId"));
            o.setCompanyKey(0);
            o.setIsActive(GetBoolean(jsonObj, "IsActive", o.getIsActive()));
            return o;
        }
    }

    private static class MotionPictureProductionSerializer implements JsonSerializer<MotionPictureProduction> {
        public JsonElement serialize(MotionPictureProduction src, Type typeOfT, JsonSerializationContext context) {
            JsonObject o = new JsonObject();

            o.addProperty("MotionPictureProductionId", src.getMotionPictureProductionId());
            o.addProperty("Name", src.getName());
            o.addProperty("AddressLine1", src.getAddressLine1());
            o.addProperty("AddressLine2", src.getAddressLine2());
            o.addProperty("City", src.getCity());
            o.addProperty("State", src.getState());
            o.addProperty("ZipCode", src.getZipCode());
            o.addProperty("BusinessHours", src.getBusinessHours());
            o.addProperty("MotionPictureAuthorityId", src.getMotionPictureAuthorityId());
            o.addProperty("CompanyKey", src.getCompanyKey());
            o.addProperty("IsActive", src.getIsActive());

            return o;
        }
    }

    private class MotionPictureProductionArrayDeserializer implements JsonDeserializer<MotionPictureProduction[]> {
        public MotionPictureProduction[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

            JsonArray array = json.getAsJsonArray();

            MotionPictureProduction[] list = new MotionPictureProduction[array.size()];
            for (int i = 0; i < list.length; i++) {
                list[i] = (_gson.fromJson(array.get(i), MotionPictureProduction.class));
            }

            return list;
        }
    }

    private class FeatureToggleDeserializer implements
            JsonDeserializer<FeatureToggle> {
        public FeatureToggle deserialize(JsonElement json,
                                                  Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            FeatureToggle o = new FeatureToggle();
            JsonObject jsonObj = json.getAsJsonObject();

            o.setName(GetString(jsonObj, "Name"));
            o.setState(GetBoolean(jsonObj, "State", o.getState()));

            return o;
        }
    }

    private class FeatureToggleSerializer implements
            JsonSerializer<FeatureToggle> {
        public JsonElement serialize(FeatureToggle src, Type typeOfT,
                                     JsonSerializationContext context) {
            JsonObject o = new JsonObject();

            o.addProperty("Name", src.getName());
            o.addProperty("State", src.getState());

            return o;
        }
    }

    private class FeatureToggleArrayDeserializer implements
            JsonDeserializer<FeatureToggle[]> {
        public FeatureToggle[] deserialize(JsonElement json, Type typeOfT,
                                                    JsonDeserializationContext context) throws JsonParseException {

            JsonArray array = json.getAsJsonArray();

            FeatureToggle[] list = new FeatureToggle[array.size()];
            for (int i = 0; i < list.length; i++) {
                list[i] = (_gson.fromJson(array.get(i), FeatureToggle.class));
            }

            return list;
        }
    }

    private class EmployeeLogListSerializer implements
            JsonSerializer<EmployeeLogList> {
        public JsonElement serialize(EmployeeLogList src, Type typeOfT,
                                     JsonSerializationContext context) {
            JsonObject o = new JsonObject();
            EmployeeLog[] employeeLogs = src.getEmployeeLogList();
            try {
                if (employeeLogs != null) {
                    JsonElement jsonArray = _gson.toJsonTree(employeeLogs, EmployeeLog[].class);
                    o.add("EmployeeLogs", jsonArray);
                }

            } catch (Exception e) {
                return o;
            }

            return o;
        }
    }

    private class EmployeeLogArraySerializer implements JsonSerializer<EmployeeLog[]> {
        public JsonElement serialize(EmployeeLog[] src, Type typeOfT, JsonSerializationContext context) {
            JsonArray array = new JsonArray();

            try {
                for (EmployeeLog empLog : src) {
                    array.add(_gson.toJsonTree(empLog, EmployeeLog.class));
                }
            } catch (Exception e) {
                return array;
            }
            return array;
        }
    }

    private class EmployeeLogSerializer implements JsonSerializer<EmployeeLog> {
        public JsonElement serialize(EmployeeLog src, Type typeOfT, JsonSerializationContext context) {
            JsonObject o = new JsonObject();

            o.addProperty("EmployeeId", src.getEmployeeId());
            if (src.getLogDate() == null) {
                o.addProperty("LogDate", "");
            } else {
                o.addProperty("LogDate", DateUtility.getHomeTerminalDMOSoapDateTimeFormat().format(src.getLogDate()));
            }
            o.addProperty("TotalLogDistance", src.getTotalLogDistance());
            o.addProperty("HasReturnedToLocation", src.getHasReturnedToLocation());
            o.addProperty("DriverType", src.getDriverType().getValue());
            o.addProperty("Ruleset", src.getRuleset().getValue());
            o.addProperty("Timezone", src.getTimezone().getValue());
            if (src.getMobileStartTimestamp() == null) {
                o.addProperty("MobileStartTimestamp", "");
            } else {
                o.addProperty("MobileStartTimestamp", DateUtility.getHomeTerminalDMOSoapDateTimeFormat().format(src.getMobileStartTimestamp()));
            }
            if (src.getMobileEndTimestamp() == null) {
                o.addProperty("MobileEndTimestamp", "");
            } else {
                o.addProperty("MobileEndTimestamp", DateUtility.getHomeTerminalDMOSoapDateTimeFormat().format(src.getMobileEndTimestamp()));
            }
            o.addProperty("MobileRecordedDistance", src.getMobileRecordedDistance());
            o.addProperty("MobileEobrIdentifier", src.getMobileEobrIdentifier());
            o.addProperty("TractorNumbers", src.getTractorNumbers());
            o.addProperty("TrailerNumbers", src.getTrailerNumbers());
            o.addProperty("TrailerPlate", src.getTrailerPlate());
            o.addProperty("ShipmentInformation", src.getShipmentInformation());
            o.addProperty("VehiclePlate", src.getVehiclePlate());
            o.addProperty("IsShortHaulExceptionUsed", src.getIsShortHaulExceptionUsed());
            o.addProperty("CanadaDeferralType", src.getCanadaDeferralType().getValue());
            if (src.getWeeklyResetStartTimestamp() == null) {
                o.addProperty("WeeklyResetStartTimestamp", "");
            } else {
                o.addProperty("WeeklyResetStartTimestamp", DateUtility.getHomeTerminalDMOSoapDateTimeFormat().format(src.getWeeklyResetStartTimestamp()));
            }
            o.addProperty("IsHaulingExplosives", src.getIsHaulingExplosives());
            o.addProperty("IsWeeklyResetUsed", src.getIsWeeklyResetUsed());
            o.addProperty("IsWeeklyResetUsedOverridden", src.getIsWeeklyResetUsedOverridden());
            o.addProperty("IsOperatesSpecificVehiclesForOilField", src.getIsOperatesSpecificVehiclesForOilfield());
            o.addProperty("ExemptLogType", src.getExemptLogType().getValue());
            o.addProperty("IsNonCDLShortHaulExceptionUsed", src.getIsNonCDLShortHaulExceptionUsed());
            o.addProperty("IsExemptFrom30MinBreakRequirement", src.getIsExemptFrom30MinBreakRequirement());
            o.addProperty("IsCertified", src.getIsCertified());
            o.addProperty("IsExemptFromELDUse", src.getIsExemptFromELDUse());

            EmployeeLogEldEventList allEldEvents = src.getEldEventList();

            if (allEldEvents.getEldEventList() != null && allEldEvents.getEldEventList().length > 0) {
                serializeLogEvents(o, allEldEvents);
            }

            JsonElement jsonArray;
            jsonArray = _gson.toJsonTree(src.getTeamDriverList(), TeamDriverList.class);
            o.add("TeamDriverList", jsonArray);

            jsonArray = _gson.toJsonTree(src.getTimeSyncFailureList(), FailureReportList.class);
            o.add("TimeSyncFailureList", jsonArray);

            jsonArray = _gson.toJsonTree(src.getEobrFailureList(), FailureReportList.class);
            o.add("EobrFailureList", jsonArray);

            return o;
        }

        private void serializeLogEvents(JsonObject o, EmployeeLogEldEventList logEvents) {
            JsonElement jsonArray;
            EmployeeLogEldEvent firstEvent = logEvents.getEldEventList()[0];

            if (firstEvent.getEventSequenceIDNumber() == EmployeeLogEldEnum.AOBRD) {
                //Serialize AOBRD events
                ArrayList<LogEvent> aobrdEventArrayList = new ArrayList<>();
                Collections.addAll(aobrdEventArrayList, logEvents.getEldEventList(
                        EldEventAdapterList.ListAccessorModifierEnum.DutyStatus));
                LogEventList aobrdEvents = new LogEventList();
                aobrdEvents.setLogEventList(aobrdEventArrayList.toArray(new LogEvent[0]));

                jsonArray = _gson.toJsonTree(aobrdEvents, LogEventList.class);
                o.add("LogEventList", jsonArray);
            } else {
                //Serialize Mandate events
                jsonArray = _gson.toJsonTree(logEvents, EmployeeLogEldEventList.class);
                o.add("EldEventList", jsonArray);
            }
        }
    }

    private class LogEventSerializer implements JsonSerializer<LogEvent> {
        public JsonElement serialize(LogEvent src, Type typeOfT, JsonSerializationContext context) {
            JsonObject o = new JsonObject();

            o.addProperty("EobrSerialNumber", src.getEobrSerialNumber());
            if (src.getStartTime() == null) {
                o.addProperty("StartTime", "");
            } else {
                o.addProperty("StartTime", DateUtility.getHomeTerminalDMOSoapDateTimeFormat().format(src.getStartTime()));
            }
            o.addProperty("DutyStatusEnum", src.getDutyStatusEnum().getValue());
            o.addProperty("IsStartTimeValidated", src.getIsStartTimeValidated());
            o.addProperty("RulesetType", src.getRulesetType().getValue());
            o.addProperty("Note", src.getLogRemark());

            if (src.getLogRemarkDate() == null) {
                o.addProperty("NoteDate", "");
            } else {
                o.addProperty("NoteDate", DateUtility.getHomeTerminalDMOSoapDateTimeFormat().format(src.getLogRemarkDate()));
            }

            // Location
            if (src.getLocation() != null) {
                JsonElement json = _gson.toJsonTree(src.getLocation(), Location.class);
                o.add("Location", json);
            }

            o.addProperty("MotionPictureAuthorityId", src.getMotionPictureAuthorityId());
            o.addProperty("MotionPictureProductionId", src.getMotionPictureProductionId());

            return o;
        }
    }

    private class LogEventListSerializer implements JsonSerializer<LogEventList> {
        public JsonElement serialize(LogEventList src, Type typeOfT, JsonSerializationContext context) {
            JsonObject o = new JsonObject();

            LogEvent[] logEvents = src.getLogEventList();

            if (logEvents != null) {
                JsonElement jsonArray = _gson.toJsonTree(logEvents, LogEvent[].class);
                o.add("LogEvents", jsonArray);
            }

            return o;
        }
    }

    private class LogEventArraySerializer implements JsonSerializer<LogEvent[]> {
        public JsonElement serialize(LogEvent[] src, Type typeOfT, JsonSerializationContext context) {
            JsonArray array = new JsonArray();
            for (LogEvent evt : src) {
                array.add(_gson.toJsonTree(evt, LogEvent.class));
            }

            return array;
        }
    }

    private class EmployeeLogEldEventListSerializer implements JsonSerializer<EmployeeLogEldEventList> {
        public JsonElement serialize(EmployeeLogEldEventList src, Type typeOfT, JsonSerializationContext context) {
            JsonObject o = new JsonObject();

            EmployeeLogEldEvent[] eldEvents = src.getEldEventList();

            if (eldEvents != null) {
                JsonElement jsonArray = _gson.toJsonTree(eldEvents, EmployeeLogEldEvent[].class);
                o.add("EldEvents", jsonArray);
            }

            return o;
        }
    }

    private class EmployeeLogEldEventArraySerializer implements JsonSerializer<EmployeeLogEldEvent[]> {
        public JsonElement serialize(EmployeeLogEldEvent[] src, Type typeOfT, JsonSerializationContext context) {
            JsonArray array = new JsonArray();
            for (EmployeeLogEldEvent evt : src) {
                array.add(_gson.toJsonTree(evt, EmployeeLogEldEvent.class));
            }

            return array;
        }
    }

    private class EmployeeLogEldEventSerializer implements JsonSerializer<EmployeeLogEldEvent> {
        public JsonElement serialize(EmployeeLogEldEvent src, Type typeOfT, JsonSerializationContext context) {
            JsonObject o = new JsonObject();

            o.addProperty("DriverOriginatorUserId", src.getDriverOriginatorUserId());
            o.addProperty("EncompassOriginatorUserId", src.getEncompassOriginatorUserId());
            o.addProperty("UnidentifiedUserId", src.getUnidentifiedUserId());
            o.addProperty("EobrSerialNumber", src.getEobrSerialNumber());
            o.addProperty("EventSequenceIdNumber", src.getEventSequenceIDNumber());
            o.addProperty("EventRecordStatus", src.getEventRecordStatus());
            o.addProperty("EventRecordOrigin", src.getEventRecordOrigin());
            o.addProperty("EventType", src.getEventType().getValue());
            o.addProperty("EventCode", src.getEventCode());
            o.addProperty("TractorNumber", src.getTractorNumber());
            o.addProperty("VehiclePlate", src.getVehiclePlate());
            o.addProperty("TrailerNumber", src.getTrailerNumber());
            o.addProperty("ShipmentInfo", src.getShipmentInfo());
            o.addProperty("TrailerPlate", src.getTrailerPlate());
            o.addProperty("RuleSetId", src.getRuleSet().getValue());
            o.addProperty("AccumulatedVehicleMiles", src.getAccumulatedVehicleMiles());
            o.addProperty("DiagnosticCode", src.getDiagnosticCode());
            o.addProperty("Distance", src.getDistance());
            o.addProperty("DistanceSinceLastCoordinates", src.getDistanceSinceLastCoordinates());
            o.addProperty("DriverDataDiagnosticEventIndicatorStatus", src.getDriverDataDiagnosticEventIndicatorStatus());
            o.addProperty("DriverLocationDescription", src.getDriversLocationDescription());
            o.addProperty("EldMalfunctionIndicatorStatus", src.getEldMalfunctionIndicatorStatus());
            o.addProperty("EngineHours", src.getEngineHours());
            o.addProperty("EventComment", src.getEventComment());
            o.addProperty("EventDataCheck", src.getEventDataCheck());
            o.addProperty("Geolocation", src.getGeolocation());
            o.addProperty("IsGpsAtReducedPrecision", src.getIsGPSAtReducedPrecision());
            o.addProperty("Latitude", src.getLatitude());
            o.addProperty("LatitudeStatusCode", src.getLatitudeStatusCode());
            o.addProperty("Longitude", src.getLongitude());
            o.addProperty("LongitudeStatusCode", src.getLongitudeStatusCode());
            o.addProperty("LogKey", src.getLogKey());
            o.addProperty("LogRemark", src.getLogRemark());
            o.addProperty("Odometer", src.getOdometer() == null ? null : src.getOdometer().intValue());
            o.addProperty("OriginalEvent", src.getOriginalEvent());
            o.addProperty("ReassignEvent", src.getReassignEvent());
            o.addProperty("MotionPictureAuthorityId", src.getMotionPictureAuthorityId());
            o.addProperty("MotionPictureProductionId", src.getMotionPictureProductionId());
            o.addProperty(RELATED_EVENT_KMB_PRIMARY_KEY, src.getRelatedKmbPK());
            o.addProperty(KMB_PRIMARY_KEY, src.getPrimaryKey());

            if (src.getLocation() != null) {
                JsonElement json = _gson.toJsonTree(src.getLocation(), Location.class);
                o.add("Location", json);
            }

            SimpleDateFormat dateTimestampUTCFormat = DateUtility.getHomeTerminalDMOSoapDateTimestampUTCFormat();
            dateTimestampUTCFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

            o.addProperty("EventDateTime", dateTimestampUTCFormat.format(src.getEventDateTime()));

            dateTimestampUTCFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

            Date gpsTimestamp = src.getGpsTimestamp();
            if (gpsTimestamp == null) {
                o.addProperty("GpsTimestamp", "0001-01-01T00:00:00");
            } else {
                o.addProperty("GpsTimestamp", dateTimestampUTCFormat.format(gpsTimestamp));
            }

            o.addProperty("EditDuration", src.getEditDuration(Calendar.SECOND));
            o.addProperty("EncompassClusterPK", src.getEncompassClusterPK());
            o.addProperty("IsUnidentifiedEvent", src.isUnidentifiedEvent());
            o.addProperty("UnidentifiedEventConfidenceLevel", src.getUnidentifiedEventConfidenceLevel());
            o.addProperty("UnidentifiedEventSuggestedDriver", src.getUnidentifiedEventSuggestedDriver());

            return o;
        }
    }

    private class LocationSerializer implements JsonSerializer<Location> {
        public JsonElement serialize(Location src, Type typeOfT, JsonSerializationContext context) {
            JsonObject o = new JsonObject();

            o.addProperty("Name", src.getName());
            o.addProperty("OdometerReading", src.getOdometerReading());
            o.addProperty("EndOdometerReading", src.getEndOdometerReading());

            // GpsInfo
            if (src.getGpsInfo() != null) {
                JsonElement jsonElement = _gson.toJsonTree(src.getGpsInfo(), GpsLocation.class);
                o.add("GpsInfo", jsonElement);
            }

            return o;
        }
    }

    private class TeamDriverListSerializer implements JsonSerializer<TeamDriverList> {
        public JsonElement serialize(TeamDriverList src, Type typeOfT, JsonSerializationContext context) {
            JsonObject o = new JsonObject();

            TeamDriver[] teamDrivers = src.getTeamDriverList();

            if (teamDrivers != null) {
                JsonElement jsonArray = _gson.toJsonTree(teamDrivers, TeamDriver[].class);
                o.add("TeamDrivers", jsonArray);
            }

            return o;
        }
    }

    private class TeamDriverArraySerializer implements JsonSerializer<TeamDriver[]> {
        public JsonElement serialize(TeamDriver[] src, Type typeOfT, JsonSerializationContext context) {
            JsonArray array = new JsonArray();
            for (TeamDriver evt : src) {
                array.add(_gson.toJsonTree(evt, TeamDriver.class));
            }

            return array;
        }
    }

    private static class TeamDriverSerializer implements JsonSerializer<TeamDriver> {
        public JsonElement serialize(TeamDriver src, Type typeOfT, JsonSerializationContext context) {
            JsonObject o = new JsonObject();

            if (src.getStartTime() == null) {
                o.addProperty("StartTime", "");
            } else {
                o.addProperty("StartTime", DateUtility.getHomeTerminalDMOSoapDateTimeFormat().format(src.getStartTime()));
            }

            if (src.getEndTime() == null) {
                o.addProperty("EndTime", "");
            } else {
                o.addProperty("EndTime", DateUtility.getHomeTerminalDMOSoapDateTimeFormat().format(src.getEndTime()));
            }

            o.addProperty("EmployeeCode", src.getEmployeeCode());
            o.addProperty("EmployeeId", src.getEmployeeId());
            o.addProperty("DisplayName", src.getDisplayName());
            o.addProperty("KMBUsername", src.getKMBUsername());

            return o;
        }
    }

    private class FailureReportListSerializer implements JsonSerializer<FailureReportList> {
        public JsonElement serialize(FailureReportList src, Type typeOfT, JsonSerializationContext context) {
            JsonObject o = new JsonObject();

            FailureReport[] failureReports = src.getFailureReportList();

            if (failureReports != null) {
                JsonElement jsonArray = _gson.toJsonTree(failureReports, FailureReport[].class);
                o.add("FailureReports", jsonArray);
            }

            return o;
        }
    }

    private class FailureReportArraySerializer implements JsonSerializer<FailureReport[]> {
        public JsonElement serialize(FailureReport[] src, Type typeOfT, JsonSerializationContext context) {
            JsonArray array = new JsonArray();
            for (FailureReport evt : src) {
                array.add(_gson.toJsonTree(evt, FailureReport.class));
            }

            return array;
        }
    }

    private static class FailureReportSerializer implements JsonSerializer<FailureReport> {
        public JsonElement serialize(FailureReport src, Type typeOfT, JsonSerializationContext context) {
            JsonObject o = new JsonObject();

            o.addProperty("Category", src.getCategory().getValue());

            if (src.getStartTime() == null) {
                o.addProperty("StartTime", "");
            } else {
                o.addProperty("StartTime", DateUtility.getHomeTerminalDMOSoapDateTimeFormat().format(src.getStartTime().toDate()));
            }

            if (src.getStopTime() == null) {
                o.addProperty("StopTime", "");
            } else {
                o.addProperty("StopTime", DateUtility.getHomeTerminalDMOSoapDateTimeFormat().format(src.getStopTime().toDate()));
            }

            o.addProperty("Message", src.getMessage());

            return o;
        }
    }

    private class KMBEncompassUserListDeserializer implements JsonDeserializer<KMBEncompassUserList> {
        public KMBEncompassUserList deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            KMBEncompassUserList o = new KMBEncompassUserList();
            JsonObject jsonObj = json.getAsJsonObject();
            JsonArray array = jsonObj.getAsJsonArray("EncompassUsers");
            KMBEncompassUser[] list = new KMBEncompassUser[array.size()];
            for (int i = 0; i < list.length; i++) {
                list[i] = (_gson.fromJson(array.get(i), KMBEncompassUser.class));
            }
            o.setEncompassUserList(list);
            return o;
        }
    }

    private class EmployeeLogListDeserializer implements JsonDeserializer<EmployeeLogList> {
        public EmployeeLogList deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            EmployeeLogList o = new EmployeeLogList();
            JsonObject jsonObj = json.getAsJsonObject();

            JsonArray array = jsonObj.getAsJsonArray("EmployeeLogs");
            EmployeeLog[] list = new EmployeeLog[array.size()];
            for (int i = 0; i < list.length; i++) {
                list[i] = _gson.fromJson(array.get(i), EmployeeLog.class);
            }
            o.setEmployeeLogList(list);

            return o;
        }
    }

    private class EmployeeLogDeserializer implements JsonDeserializer<EmployeeLog> {
        public EmployeeLog deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            EmployeeLog o = new EmployeeLog();
            JsonObject jsonObj = json.getAsJsonObject();

            o.setEmployeeId(GetString(jsonObj, "EmployeeId"));
            o.setLogDate(GetFormattedDate(jsonObj, "LogDate", DateUtility.getHomeTerminalDMOSoapDateTimeFormat()));
            o.setTotalLogDistance(GetFloat(jsonObj, "TotalLogDistance", o.getTotalLogDistance()));
            o.setHasReturnedToLocation(GetBoolean(jsonObj, "HasReturnedToLocation", o.getHasReturnedToLocation()));
            o.setDriverType((DriverTypeEnum) GetEnum(jsonObj, "DriverType", DriverTypeEnum.class));
            o.setRuleset((RuleSetTypeEnum) GetEnum(jsonObj, "Ruleset", RuleSetTypeEnum.class));
            o.setTimezone((TimeZoneEnum) GetEnum(jsonObj, "TimeZone", TimeZoneEnum.class));
            o.setMobileStartTimestamp(GetFormattedDate(jsonObj, "MobileStartTimestamp", DateUtility.getHomeTerminalDMOSoapDateTimeFormat()));
            o.setMobileEndTimestamp(GetFormattedDate(jsonObj, "MobileEndTimestamp", DateUtility.getHomeTerminalDMOSoapDateTimeFormat()));
            o.setMobileRecordedDistance(GetFloat(jsonObj, "MobileRecordedDistance", o.getMobileRecordedDistance()));
            o.setMobileEobrIdentifier(GetString(jsonObj, "MobileEobrIdentifier"));
            o.setTractorNumbers(GetString(jsonObj, "TractorNumbers"));
            o.setTrailerNumbers(GetString(jsonObj, "TrailerNumbers"));
            o.setTrailerPlate(GetString(jsonObj, "TrailerPlate"));
            o.setShipmentInformation(GetString(jsonObj, "ShipmentInformation"));
            o.setVehiclePlate(GetString(jsonObj, "VehiclePlate"));
            o.setIsShortHaulExceptionUsed(GetBoolean(jsonObj, "IsShortHaulExceptionUsed", o.getIsShortHaulExceptionUsed()));
            o.setIsHaulingExplosives(GetBoolean(jsonObj, "IsHaulingExplosives", o.getIsHaulingExplosives()));
            o.setCanadaDeferralType((CanadaDeferralTypeEnum) GetEnum(jsonObj, "CanadaDeferralType", CanadaDeferralTypeEnum.class));
            o.setWeeklyResetStartTimestamp(GetFormattedDate(jsonObj, "WeeklyResetStartTimestamp", DateUtility.getHomeTerminalDMOSoapDateTimeFormat()));
            o.setIsWeeklyResetUsed(GetBoolean(jsonObj, "IsWeeklyResetUsed", o.getIsWeeklyResetUsed()));
            o.setIsWeeklyResetUsedOverridden(GetBoolean(jsonObj, "IsWeeklyResetUsedOverridden", o.getIsWeeklyResetUsedOverridden()));
            o.setIsOperatesSpecificVehiclesForOilfield(GetBoolean(jsonObj, "IsOperatesSpecificVehiclesForOilField", o.getIsOperatesSpecificVehiclesForOilfield()));
            o.setExemptLogType((ExemptLogTypeEnum) GetEnum(jsonObj, "ExemptLogType", ExemptLogTypeEnum.class));
            o.setIsNonCDLShortHaulExceptionUsed(GetBoolean(jsonObj, "IsNonCDLShortHaulExceptionUsed", o.getIsNonCDLShortHaulExceptionUsed()));
            o.setIsExemptFrom30MinBreakRequirement(GetBoolean(jsonObj, "IsExemptFrom30MinBreakRequirement", o.getIsExemptFrom30MinBreakRequirement()));
            o.setIsCertified(GetBoolean(jsonObj, "IsCertified", o.getIsCertified()));
            o.setIsExemptFromELDUse(GetBoolean(jsonObj, "IsExemptFromELDUse", o.getIsExemptFromELDUse()));

            //++++++++++++++++++++Eld Event/Log Event deserialization++++++++++++++++++++
            //ELD Events
            o.setEldEventList(new EmployeeLogEldEventList());
            JsonObject eldEventListObj = jsonObj.getAsJsonObject("EldEventList");
            JsonArray eldEventArray = eldEventListObj.getAsJsonArray("EldEvents");

            //AOBRD-specific ELD Events
            JsonObject logEventsListObj = jsonObj.getAsJsonObject("LogEventList");
            JsonArray logEventsArray = logEventsListObj.getAsJsonArray("LogEvents");
            //Collections for AOBRD and Mandate events
            Set<EmployeeLogEldEvent> mandateEvents = new HashSet<>();
            Set<EmployeeLogEldEvent> aobrdEvents = new HashSet<>();

            //Add AOBRD events in ELDEventList to AOBRD list
            if (eldEventArray.size() > 0) {
                for (JsonElement jsonEvt : eldEventArray) {
                    //deserialize event
                    EmployeeLogEldEvent evt = _gson.fromJson(jsonEvt, EmployeeLogEldEvent.class);
                    //place
                    if (evt.getEventSequenceIDNumber() == EmployeeLogEldEnum.AOBRD) {
                        aobrdEvents.add(evt);
                    } else {
                        mandateEvents.add(evt);
                    }
                }
            }

            //Add pure AOBRD events to AOBRD list
            if (logEventsArray.size() > 0) {
                for (JsonElement jsonEvt : logEventsArray) {
                    aobrdEvents.add((EmployeeLogEldEvent) _gson.fromJson(jsonEvt, LogEvent.class));
                }
            }
            ArrayList<EmployeeLogEldEvent> aggregateEldEventsList = new ArrayList<>();
            aggregateEldEventsList.addAll(mandateEvents);
            aggregateEldEventsList.addAll(aobrdEvents);
            o.getEldEventList().setEldEventList(aggregateEldEventsList.toArray(new EmployeeLogEldEvent[mandateEvents.size() + aobrdEvents.size()]));
            //++++++++++++++++++++Eld Event/Log Event deserialization++++++++++++++++++++

            // Team drivers
            JsonObject teamDriverListObj = jsonObj.getAsJsonObject("TeamDriverList");
            JsonArray teamDriversArray = teamDriverListObj.getAsJsonArray("TeamDrivers");
            if (teamDriversArray.size() > 0) {
                TeamDriver[] teamDriversList = new TeamDriver[teamDriversArray.size()];
                for (int i = 0; i < teamDriversList.length; i++) {
                    teamDriversList[i] = _gson.fromJson(teamDriversArray.get(i), TeamDriver.class);
                }
                o.getTeamDriverList().setTeamDriverList(teamDriversList);
            }

            // Failure lists
            // TimeSyncFailureList
            JsonObject timeSyncFailureListObj = jsonObj.getAsJsonObject("TimeSyncFailureList");
            JsonArray timeSyncFailureArray = timeSyncFailureListObj.getAsJsonArray("FailureReports");
            if (timeSyncFailureArray.size() > 0) {
                FailureReport[] timeSyncFailureList = new FailureReport[timeSyncFailureArray.size()];
                for (int i = 0; i < timeSyncFailureList.length; i++) {
                    timeSyncFailureList[i] = _gson.fromJson(timeSyncFailureArray.get(i), FailureReport.class);
                }
                o.getTimeSyncFailureList().setFailureReportList(timeSyncFailureList);
            }

            // EobrFailureList
            JsonObject eobrFailureListObj = jsonObj.getAsJsonObject("EobrFailureList");
            JsonArray eobrFailureArray = eobrFailureListObj.getAsJsonArray("FailureReports");
            if (eobrFailureArray.size() > 0) {
                FailureReport[] eobrFailureList = new FailureReport[eobrFailureArray.size()];
                for (int i = 0; i < eobrFailureList.length; i++) {
                    eobrFailureList[i] = _gson.fromJson(eobrFailureArray.get(i), FailureReport.class);
                }
                o.getEobrFailureList().setFailureReportList(eobrFailureList);
            }

            return o;
        }
    }

    private static class EmployeeLogEldEventDeserializer implements JsonDeserializer<EmployeeLogEldEvent> {
        public EmployeeLogEldEvent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            EmployeeLogEldEvent o = new EmployeeLogEldEvent();
            JsonObject jsonObj = json.getAsJsonObject();

            o.setAccumulatedVehicleMiles(GetInteger(jsonObj, "AccumulatedVehicleMiles", null), true);
            o.setDiagnosticCode(GetString(jsonObj, "DiagnosticCode"));
            o.setDistance(GetInteger(jsonObj, "Distance", null));
            o.setDistanceSinceLastCoordinates(GetFloat(jsonObj, "DistanceSinceLastCoordinates", null));
            o.setDriverDataDiagnosticEventIndicatorStatus(GetBoolean(jsonObj, "DriverDataDiagnosticEventIndicatorStatus", false));
            o.setDriversLocationDescription(GetString(jsonObj, "DriverLocationDescription"));
            o.setDriverOriginatorUserId(GetString(jsonObj, "DriverOriginatorUserId"));
            o.setEldMalfunctionIndicatorStatus(GetBoolean(jsonObj, "EldMalfunctionIndicatorStatus", false));
            o.setEncompassOriginatorUserId(GetString(jsonObj, "EncompassOriginatorUserId"));
            o.setEngineHours(GetDouble(jsonObj, "EngineHours", null), true);
            o.setEobrSerialNumber(GetString(jsonObj, "EobrSerialNumber"));
            o.setEventCode(GetInteger(jsonObj, "EventCode", null));
            o.setEventComment(GetString(jsonObj, "EventComment"));
            o.setEventDataCheck(GetString(jsonObj, "EventDataCheck"));
            o.setEventDateTime(GetFormattedDate(jsonObj, "EventDateTime", DateUtility.getHomeTerminalDMOSoapDateTimeFormat()));
            o.setEventRecordOrigin(GetInteger(jsonObj, "EventRecordOrigin", null));
            o.setEventRecordStatus(GetInteger(jsonObj, "EventRecordStatus", null));
            o.setEventSequenceIDNumber(GetInteger(jsonObj, "EventSequenceIdNumber", EmployeeLogEldEnum.DEFAULT));
            o.setEventType(Enums.EmployeeLogEldEventType.setFromInt(GetInteger(jsonObj, "EventType", 1)));
            o.setGeolocation(GetString(jsonObj, "Geolocation"));
            o.setIsGPSAtReducedPrecision(GetBoolean(jsonObj, "IsGpsAtReducedPrecision", false));
            o.setLatitude(GetDouble(jsonObj, "Latitude", null), true);
            o.setLatitudeStatusCode(GetString(jsonObj, "LatitudeStatusCode"));
            o.setLogKey(GetInteger(jsonObj, "LogKey", -1));    // passed in during Persist as part of the parent Log save
            o.setLogRemark(GetString(jsonObj, "LogRemark"));
            o.setLongitude(GetDouble(jsonObj, "Longitude", null), true);
            o.setLongitudeStatusCode(GetString(jsonObj, "LongitudeStatusCode"));
            //backend is int for eld mandate.  converting until now.  AOBRD uses float so keeping the kbm data type as float for now.
            String s = GetString(jsonObj, "Odometer");
            if (s != null && s.length() > 0) {
                o.setOdometer(Float.parseFloat(s));
            }
            o.setOriginalEvent(GetBoolean(jsonObj, "OriginalEvent", false));
            o.setReassignEvent(GetBoolean(jsonObj, "ReassignEvent", false));
            o.setRuleSet((RuleSetTypeEnum) GetEnum(jsonObj, "RuleSetId", RuleSetTypeEnum.class));
            o.setShipmentInfo(GetString(jsonObj, "ShipmentInfo"));
            o.setTractorNumber(GetString(jsonObj, "TractorNumber"), true);
            o.setTrailerNumber(GetString(jsonObj, "TrailerNumber"));
            o.setTrailerPlate(GetString(jsonObj, "TrailerPlate"));
            o.setUnidentifiedUserId(GetString(jsonObj, "UnidentifiedUserId"));
            o.setVehiclePlate(GetString(jsonObj, "VehiclePlate"));

            SimpleDateFormat dateTimeFormat = DateUtility.getDMOSoapDateTimeFormat();
            dateTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

            o.setGpsTimestamp(GetFormattedDate(jsonObj, "GpsTimestamp", dateTimeFormat));

            Integer editDurationSeconds = GetInteger(jsonObj, "EditDuration", null);
            if (editDurationSeconds != null) {
                o.setEditDuration((long) editDurationSeconds, Calendar.SECOND);
            } else {
                o.setEditDuration(null);
            }

            o.setMotionPictureAuthorityId(GetString(jsonObj, "MotionPictureAuthorityId"));
            o.setMotionPictureProductionId(GetString(jsonObj, "MotionPictureProductionId"));
            o.setEncompassClusterPK(GetLong(jsonObj, "EncompassClusterPK", 0));
            o.setRelatedEncompassClusterPK(GetLong(jsonObj, RELATED_EVENT_CLUSTER_PRIMARY_KEY, 0));

            return o;
        }
    }

    private class EmployeeLogEldEventListDeserializer implements JsonDeserializer<EmployeeLogEldEventList> {
        public EmployeeLogEldEventList deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            EmployeeLogEldEventList o = new EmployeeLogEldEventList();
            JsonObject jsonObj = json.getAsJsonObject();

            JsonArray array = jsonObj.getAsJsonArray("EldEvents");

            EmployeeLogEldEvent[] list = new EmployeeLogEldEvent[array.size()];
            for (int i = 0; i < list.length; i++) {
                list[i] = _gson.fromJson(array.get(i), EmployeeLogEldEvent.class);
            }
            o.setEldEventList(list);

            return o;
        }
    }

    private class GeotabUnidentifiedEmployeeLogEldEventListDeserializer implements JsonDeserializer<EmployeeLogEldEventList> {
        public EmployeeLogEldEventList deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            EmployeeLogEldEventList o = new EmployeeLogEldEventList();
            JsonObject jsonObj = json.getAsJsonObject();

            JsonArray array = jsonObj.getAsJsonArray("EldEvents");

            EmployeeLogEldEvent[] list = new EmployeeLogEldEvent[array.size()];
            for (int i = 0; i < list.length; i++) {
                list[i] = _geotabUnidentifiedEmployeeLogEldEventGson.fromJson(array.get(i), EmployeeLogEldEvent.class);
            }
            o.setEldEventList(list);

            return o;
        }
    }

    private static class GeotabUnidentifiedEmployeeLogEldEventDeserializer implements JsonDeserializer<EmployeeLogEldEvent> {
        public EmployeeLogEldEvent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            EmployeeLogEldEvent o = new EmployeeLogEldEvent();
            JsonObject jsonObj = json.getAsJsonObject();

            o.setAccumulatedVehicleMiles(GetInteger(jsonObj, "AccumulatedVehicleMiles", null), true);
            o.setDiagnosticCode(GetString(jsonObj, "DiagnosticCode"));
            o.setDistance(GetInteger(jsonObj, "Distance", null));
            o.setDistanceSinceLastCoordinates(GetFloat(jsonObj, "DistanceSinceLastCoordinates", null));
            o.setDriverDataDiagnosticEventIndicatorStatus(GetBoolean(jsonObj, "DriverDataDiagnosticEventIndicatorStatus", false));
            o.setDriversLocationDescription(GetString(jsonObj, "DriverLocationDescription"));
            o.setDriverOriginatorUserId(GetString(jsonObj, "DriverOriginatorUserId"));
            o.setEldMalfunctionIndicatorStatus(GetBoolean(jsonObj, "EldMalfunctionIndicatorStatus", false));
            o.setEncompassOriginatorUserId(GetString(jsonObj, "EncompassOriginatorUserId"));
            o.setEngineHours(GetDouble(jsonObj, "EngineHours", null), true);
            o.setEobrSerialNumber(GetString(jsonObj, "EobrSerialNumber"));
            o.setEventCode(GetInteger(jsonObj, "EventCode", null));
            o.setEventComment(GetString(jsonObj, "EventComment"));
            o.setEventDataCheck(GetString(jsonObj, "EventDataCheck"));
            o.setEventDateTime(GetFormattedDate(jsonObj, "EventDateTime", DateUtility.getHomeTerminalDMOSoapDateTimeFormatUTC()));
            o.setEventRecordOrigin(GetInteger(jsonObj, "EventRecordOrigin", null));
            o.setEventRecordStatus(GetInteger(jsonObj, "EventRecordStatus", null));
            o.setEventSequenceIDNumber(GetInteger(jsonObj, "EventSequenceIdNumber", EmployeeLogEldEnum.DEFAULT));
            o.setEventType(Enums.EmployeeLogEldEventType.setFromInt(GetInteger(jsonObj, "EventType", 1)));
            o.setGeolocation(GetString(jsonObj, "Geolocation"));
            o.setIsGPSAtReducedPrecision(GetBoolean(jsonObj, "IsGpsAtReducedPrecision", false));
            o.setLatitude(GetDouble(jsonObj, "Latitude", null), true);
            o.setLatitudeStatusCode(GetString(jsonObj, "LatitudeStatusCode"));
            o.setLogKey(GetInteger(jsonObj, "LogKey", -1));    // passed in during Persist as part of the parent Log save
            o.setLogRemark(GetString(jsonObj, "LogRemark"));
            o.setLongitude(GetDouble(jsonObj, "Longitude", null), true);
            o.setLongitudeStatusCode(GetString(jsonObj, "LongitudeStatusCode"));
            //backend is int for eld mandate.  converting until now.  AOBRD uses float so keeping the kbm data type as float for now.
            String s = GetString(jsonObj, "Odometer");
            if (s != null && s.length() > 0) {
                o.setOdometer(Float.parseFloat(s));
            }
            o.setOriginalEvent(GetBoolean(jsonObj, "OriginalEvent", false));
            o.setReassignEvent(GetBoolean(jsonObj, "ReassignEvent", false));
            o.setRuleSet((RuleSetTypeEnum) GetEnum(jsonObj, "RuleSetId", RuleSetTypeEnum.class));
            o.setShipmentInfo(GetString(jsonObj, "ShipmentInfo"));
            o.setTractorNumber(GetString(jsonObj, "TractorNumber"), true);
            o.setTrailerNumber(GetString(jsonObj, "TrailerNumber"));
            o.setTrailerPlate(GetString(jsonObj, "TrailerPlate"));
            o.setUnidentifiedUserId(GetString(jsonObj, "UnidentifiedUserId"));
            o.setVehiclePlate(GetString(jsonObj, "VehiclePlate"));

            SimpleDateFormat dateTimeFormat = DateUtility.getDMOSoapDateTimeFormat();
            dateTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

            o.setGpsTimestamp(GetFormattedDate(jsonObj, "GpsTimestamp", dateTimeFormat));

            Integer editDurationSeconds = GetInteger(jsonObj, "EditDuration", null);
            if (editDurationSeconds != null) {
                o.setEditDuration((long) editDurationSeconds, Calendar.SECOND);
            } else {
                o.setEditDuration(null);
            }

            o.setMotionPictureAuthorityId(GetString(jsonObj, "MotionPictureAuthorityId"));
            o.setMotionPictureProductionId(GetString(jsonObj, "MotionPictureProductionId"));
            o.setEncompassClusterPK(GetLong(jsonObj, "EncompassClusterPK", 0));
            o.setRelatedEncompassClusterPK(GetLong(jsonObj, RELATED_EVENT_CLUSTER_PRIMARY_KEY, 0));

            return o;
        }
    }

    //2016.20.06 banderson
    //The two deserialization classes below allow the deserialization of old LogEvents into an EmployeeLogEldEvent
    //At present (above date) we need to still send LogEvents to the Services, as we don't have logic to deal with the new use of EmployeeLogEldEvents
    //in the AOBRD workflow
    private class LogEventDeserializerForWebApi implements JsonDeserializer<EmployeeLogEldEvent> {
        public EmployeeLogEldEvent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            EmployeeLogEldEvent o = new EmployeeLogEldEvent();
            JsonObject jsonObj = json.getAsJsonObject();

            o.setEobrSerialNumber(GetString(jsonObj, "EobrSerialNumber"));
            o.setStartTime(GetFormattedDate(jsonObj, "StartTime", DateUtility.getHomeTerminalDMOWebApiDateTimeFormatUtcOffset()));
            o.setDutyStatusEnum((DutyStatusEnum) GetEnum(jsonObj, "DutyStatusEnum", DutyStatusEnum.class));
            o.setIsStartTimeValidated(GetBoolean(jsonObj, "IsStartTimeValidated", o.getIsStartTimeValidated()));
            o.setRulesetType((RuleSetTypeEnum) GetEnum(jsonObj, "RulesetType", RuleSetTypeEnum.class));
            o.setLogRemark(GetString(jsonObj, "Note"));
            o.setLogRemarkDate(GetFormattedDate(jsonObj, "NoteDate", DateUtility.getHomeTerminalDMOSoapDateTimeFormat()));
            // Location
            JsonObject location = jsonObj.getAsJsonObject("Location");
            o.setLocation(_gson.fromJson(location, Location.class));

            return o;
        }
    }

    private static class KMBEncompassUserDeserializer implements JsonDeserializer<KMBEncompassUser> {
        public KMBEncompassUser deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            KMBEncompassUser o = new KMBEncompassUser();
            JsonObject jsonObj = json.getAsJsonObject();
            o.setFirstName(GetString(jsonObj, "FirstName"));
            o.setLastName(GetString(jsonObj, "LastName"));
            o.setUserName(GetString(jsonObj, "UserName"));
            o.setUserId(GetString(jsonObj, "UserId"));
            return o;
        }
    }

    private class LogEventDeserializer implements JsonDeserializer<EmployeeLogEldEvent> {
        public EmployeeLogEldEvent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            EmployeeLogEldEvent o = new EmployeeLogEldEvent();
            JsonObject jsonObj = json.getAsJsonObject();

            o.setEobrSerialNumber(GetString(jsonObj, "EobrSerialNumber"));
            o.setStartTime(GetFormattedDate(jsonObj, "StartTime", DateUtility.getHomeTerminalDMOSoapDateTimeFormat()));
            o.setDutyStatusEnum((DutyStatusEnum) GetEnum(jsonObj, "DutyStatusEnum", DutyStatusEnum.class));
            o.setIsStartTimeValidated(GetBoolean(jsonObj, "IsStartTimeValidated", o.getIsStartTimeValidated()));
            o.setRulesetType((RuleSetTypeEnum) GetEnum(jsonObj, "RulesetType", RuleSetTypeEnum.class));
            o.setLogRemark(GetString(jsonObj, "Note"));
            o.setLogRemarkDate(GetFormattedDate(jsonObj, "NoteDate", DateUtility.getHomeTerminalDMOSoapDateTimeFormat()));

            // Location
            JsonObject location = jsonObj.getAsJsonObject("Location");
            if (location != null) {
                o.setLocation(_gson.fromJson(location, Location.class));
            }

            o.setMotionPictureProductionId(GetString(jsonObj, "MotionPictureProductionId"));
            o.setMotionPictureAuthorityId(GetString(jsonObj, "MotionPictureAuthorityId"));

            return o;
        }
    }

    public static class EobrConfigurationDeserializer implements JsonDeserializer<EobrConfiguration> {
        public EobrConfiguration deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            EobrConfiguration o = new EobrConfiguration();
            JsonObject jsonObj = json.getAsJsonObject();

            o.setDashboardOdometer(GetFloat(jsonObj, "DashboardOdometer", 0.0F));
            o.setDatabusType((DatabusTypeEnum) GetEnum(jsonObj, "DatabusType", DatabusTypeEnum.class));
            o.setDataCollectionRate(GetInteger(jsonObj, "DataCollectionRate", 0));
            o.setDiscoveryPasskey(GetString(jsonObj, "DiscoveryPasskey"));
            o.setEobrOdometer(GetFloat(jsonObj, "EobrOdometer", 0.0F));
            o.setFirmwareVersion(GetString(jsonObj, "FirmwareVersion"));
            o.setHardBrakeThreshold(GetFloat(jsonObj, "HardBrakeThreshold", 0.0F));
            o.setEobrGeneration(GetInteger(jsonObj, "Generation", 0));

            // TODO: This is a recipe for disaster. Use GetDate() !!
            // Date stored in Encompass is in UTC. The primitive date value
            // downloaded
            // will contain the number of milliseconds and the denoted time
            // zone(optional), for example:
            // 13445300000-0500.
            // We need to convert from UTC using the identified time zone
            String JSONDateToMilliseconds = "\\/(Date\\((-*.*?)([\\+\\-].*)?\\))\\/";
            Pattern pattern = Pattern.compile(JSONDateToMilliseconds);
            Matcher matcher = pattern.matcher(jsonObj.getAsJsonPrimitive("OdometerCalibrationDate").getAsString());
            matcher.matches();
            String tzone = matcher.group(3);
            String result = matcher.replaceAll("$2");

            int hour = 0;
            int minute = 0;
            if (tzone != null && tzone.length() == 5) {
                hour = Integer.parseInt(tzone.substring(0, 3));
                minute = Integer.parseInt(tzone.substring(3, 5));
            }

            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(Long.parseLong(result));
            cal.add(Calendar.HOUR_OF_DAY, hour);
            cal.add(Calendar.MINUTE, minute);

            Date calibrationDate = cal.getTime();
            o.setOdometerCalibrationDate(calibrationDate);
            o.setSerialNumber(GetString(jsonObj, "SerialNumber"));
            o.setSleepModeMinutes(GetInteger(jsonObj, "SleepModeMinutes", 0));
            o.setSpeedometerThreshold(GetFloat(jsonObj, "SpeedometerThreshold", 0.0F));
            o.setTachometerThreshold(GetInteger(jsonObj, "TachometerThreshold", 0));
            o.setTractorNumber(GetString(jsonObj, "TractorNumber"));

            o.setClockSyncOffset(GetLong(jsonObj, "ClockSyncOffset", 0));
            o.setClockSyncDateUTC(GetDateTime(jsonObj, "ClockSyncDateUTC"));
            o.setVIN(GetString(jsonObj, "VIN"));
            return o;
        }
    }

    public static class EobrConfigurationSerializer implements JsonSerializer<EobrConfiguration> {
        public JsonElement serialize(EobrConfiguration src, Type typeOfT, JsonSerializationContext context) {
            JsonObject o = new JsonObject();

            o.addProperty("DashboardOdometer", src.getDasboardOdometer());
            o.addProperty("DatabusType", src.getDatabusType().getValue());
            o.addProperty("DataCollectionRate", src.getDataCollectionRate());
            o.addProperty("DiscoveryPasskey", src.getDiscoveryPasskey());
            o.addProperty("EobrOdometer", src.getEobrOdometer());
            o.addProperty("FirmwareVersion", src.getFirmwareVersion());
            o.addProperty("HardBrakeThreshold", src.getHardBrakeThreshold());
            o.addProperty("Generation", src.getEobrGeneration());
            // Check for null before creating a JsonPrimitive based on
            // OdometerCalibrationDate value
            if (src.getOdometerCalibrationDate() == null) {
                o.add("OdometerCalibrationDate", null);
            } else {
                // Serialize odometercalibrationdate to UTC value - stored in
                // Encompass as UTC
                o.add("OdometerCalibrationDate", new JsonPrimitive("/Date(" + src.getOdometerCalibrationDate().getTime() + ")/"));
            }
            o.addProperty("SerialNumber", src.getSerialNumber());
            o.addProperty("SleepModeMinutes", src.getSleepModeMinutes());
            o.addProperty("SpeedometerThreshold", src.getSpeedometerThreshold());
            o.addProperty("TachometerThreshold", src.getTachometerThreshold());
            o.addProperty("TractorNumber", src.getTractorNumber());
            o.addProperty("ClockSyncOffset", src.getClockSyncOffset());
            if (src.getClockSyncDateUTC() == null) {
                o.add("ClockSyncDateUTC", null);
            } else {
                o.add("ClockSyncDateUTC", new JsonPrimitive("/Date(" + src.getClockSyncDateUTC().getMillis() + ")/"));
            }
            o.addProperty("VIN", src.getVIN());

            return o;
        }
    }

    private static class EobrDiagnosticCommandDeserializer implements JsonDeserializer<EobrDiagnosticCommand> {
        public EobrDiagnosticCommand deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            EobrDiagnosticCommand o = new EobrDiagnosticCommand();
            JsonObject jsonObj = json.getAsJsonObject();
            o.setDmoCommandId(GetString(jsonObj, "DmoCommandId"));
            o.setSerialNumber(GetString(jsonObj, "EobrSerialNumber"));
            o.setCommand(GetString(jsonObj, "Command"));

            return o;
        }
    }

    private class EobrDiagnosticCommandSerializer implements JsonSerializer<EobrDiagnosticCommand> {
        public JsonElement serialize(EobrDiagnosticCommand src, Type typeOfT, JsonSerializationContext context) {
            JsonObject o = new JsonObject();
            o.addProperty("DmoCommandId", src.getDmoCommandId());
            o.addProperty("Response", src.getRespnose());
            // Check for null before creating a JsonPrimitive based on
            // OdometerCalibrationDate value
            if (src.getResponseTimestamp() == null) {
                o.add("ResponseTimestamp", null);
            } else {
                // Serialize odometercalibrationdate to UTC value - stored in
                // Encompass as UTC
                o.addProperty("ResponseTimestamp", DateUtility.getHomeTerminalDMOSoapDateTimeFormat().format(src.getResponseTimestamp()));
            }

            return o;
        }
    }

    private class VehicleInspectionListSerializer implements JsonSerializer<VehicleInspectionList> {
        public JsonElement serialize(VehicleInspectionList src, Type typeOfT, JsonSerializationContext context) {
            JsonObject o = new JsonObject();

            VehicleInspection[] inspections = src.getInspectionList();

            try {
                if (inspections != null) {
                    JsonElement jsonArray = _gson.toJsonTree(inspections, VehicleInspection[].class);
                    o.add("VehicleInspections", jsonArray);
                }

            } catch (Exception e) {
                return o;
            }

            return o;
        }
    }

    private class VehicleInspectionArraySerializer implements JsonSerializer<VehicleInspection[]> {
        public JsonElement serialize(VehicleInspection[] src, Type typeOfT, JsonSerializationContext context) {
            JsonArray array = new JsonArray();

            try {

                for (VehicleInspection inspection : src) {
                    array.add(_gson.toJsonTree(inspection, VehicleInspection.class));
                }

            } catch (Exception e) {
                return array;
            }
            return array;
        }
    }

    private class VehicleInspectionListDeserializer implements JsonDeserializer<VehicleInspectionList> {
        public VehicleInspectionList deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            VehicleInspectionList o = new VehicleInspectionList();
            JsonObject jsonObj = json.getAsJsonObject();

            JsonArray array = jsonObj.getAsJsonArray("VehicleInspections");
            VehicleInspection[] list = new VehicleInspection[array.size()];
            for (int i = 0; i < list.length; i++) {
                list[i] = _gson.fromJson(array.get(i), VehicleInspection.class);
            }
            o.setInspectionList(list);

            return o;
        }
    }

    private class VehicleInspectionSerializer implements JsonSerializer<VehicleInspection> {
        public JsonElement serialize(VehicleInspection src, Type typeOfT, JsonSerializationContext context) {
            JsonObject o = new JsonObject();

            o.addProperty("AreCorrectionsNotNeeded", src.getAreCorrectionsNotNeeded());
            o.addProperty("AreDefectsCorrected", src.getAreDefectsCorrected());
            if (src.getCertifiedByDate() == null) {
                o.add("CertifiedByDate", null);
            } else {
                o.addProperty("CertifiedByDate", DateUtility.getHomeTerminalDMOSoapDateTimeFormat().format(src.getCertifiedByDate()));
            }

            o.addProperty("CertifiedByName", src.getCertifiedByName());
            if (src.getInspectionTimeStamp() == null) {
                o.add("InspectionTimestamp", null);
            } else {
                o.addProperty("InspectionTimestamp", DateUtility.getHomeTerminalDMOSoapDateTimeFormat().format(src.getInspectionTimeStamp()));
            }

            o.addProperty("OdometerReading", src.getInspectionOdometer());
            o.addProperty("InspectionType", src.getInspectionTypeEnum().getValue());
            o.addProperty("IsConditionSatisfactory", src.getIsConditionSatisfactory());
            o.addProperty("IsPoweredUnit", src.getIsPoweredUnit());
            o.addProperty("Notes", src.getNotes());
            if (src.getReviewedByDate() == null) {
                o.add("ReviewedByDate", null);
            } else {
                o.addProperty("ReviewedByDate", DateUtility.getHomeTerminalDMOSoapDateTimeFormat().format(src.getReviewedByDate()));
            }
            o.addProperty("ReviewedByEmployeeId", src.getReviewedByEmployeeId());
            o.addProperty("ReviewedByName", src.getReviewedByName());
            o.addProperty("EobrSerialNumber", src.getSerialNumber());
            o.addProperty("EobrTractorNumber", src.getTractorNumber());
            o.addProperty("TrailerNumber", src.getTrailerNumber());

            JsonElement jsonArray = _gson.toJsonTree(src.getDefectList(), DefectList.class);
            o.add("DefectList", jsonArray);

            return o;
        }
    }

    private class VehicleInspectionDeserializer implements JsonDeserializer<VehicleInspection> {
        public VehicleInspection deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            VehicleInspection o = new VehicleInspection();
            JsonObject jsonObj = json.getAsJsonObject();

            o.setAreCorrectionsNotNeeded(GetBoolean(jsonObj, "AreCorrectionsNotNeeded", o.getAreCorrectionsNotNeeded()));
            o.setAreDefectsCorrected(GetBoolean(jsonObj, "AreDefectsCorrected", o.getAreDefectsCorrected()));
            o.setCertifiedByDate(GetFormattedDate(jsonObj, "CertifiedByDate", DateUtility.getHomeTerminalDMOSoapDateTimeFormat()));
            o.setCertifiedByName(GetString(jsonObj, "CertifiedByName"));
            o.setCreatedByUserKey(GetInteger(jsonObj, "CreatedByEmployeeId", 0));
            o.setInspectionTimeStamp(GetFormattedDate(jsonObj, "InspectionTimestamp", DateUtility.getHomeTerminalDMOSoapDateTimeFormat()));
            o.setInspectionTypeEnum((InspectionTypeEnum) GetEnum(jsonObj, "InspectionType", InspectionTypeEnum.class));
            o.setIsConditionSatisfactory(GetBoolean(jsonObj, "IsConditionSatisfactory", o.getIsConditionSatisfactory()));
            o.setIsPoweredUnit(GetBoolean(jsonObj, "IsPoweredUnit", o.getIsPoweredUnit()));
            o.setNotes(GetString(jsonObj, "Notes"));
            o.setReviewedByDate(GetFormattedDate(jsonObj, "ReviewedByDate", DateUtility.getHomeTerminalDMOSoapDateTimeFormat()));
            o.setReviewedByEmployeeId(GetString(jsonObj, "ReviewedByEmployeeId"));
            o.setReviewedByName(GetString(jsonObj, "ReviewedByName"));
            o.setSerialNumber(GetString(jsonObj, "EobrSerialNumber"));
            o.setSubmitTimestamp(GetFormattedDate(jsonObj, "SubmitTimestamp", DateUtility.getHomeTerminalDMOSoapDateTimeFormat()));
            o.setTractorNumber(GetString(jsonObj, "EobrTractorNumber"));
            o.setTrailerNumber(GetString(jsonObj, "TrailerNumber"));
            o.setInspectionOdometer(GetFloat(jsonObj, "OdometerReading", o.getInspectionOdometer()));

            // Vehicle Inspection Defects
            JsonObject DefectListObj = jsonObj.getAsJsonObject("DefectList");
            JsonElement defectListElement = DefectListObj.get("VehicleInspectionDefects");
            if (defectListElement.isJsonArray()) {
                JsonArray inspectionDefectsArray = defectListElement.getAsJsonArray();
                if (inspectionDefectsArray.size() > 0) {
                    VehicleInspectionDefect[] vehicleInspectionDefects = new VehicleInspectionDefect[inspectionDefectsArray.size()];
                    for (int i = 0; i < vehicleInspectionDefects.length; i++) {
                        vehicleInspectionDefects[i] = _gson.fromJson(inspectionDefectsArray.get(i), VehicleInspectionDefect.class);
                    }
                    o.getDefectList().setDefectList(vehicleInspectionDefects);
                }
            }

            return o;
        }
    }

    private class DefectListSerializer implements JsonSerializer<DefectList> {
        public JsonElement serialize(DefectList src, Type typeOfT, JsonSerializationContext context) {
            JsonObject o = new JsonObject();

            VehicleInspectionDefect[] defects = src.getDefectList();

            try {
                if (defects != null) {
                    JsonElement jsonArray = _gson.toJsonTree(defects, VehicleInspectionDefect[].class);
                    o.add("VehicleInspectionDefects", jsonArray);
                }

            } catch (Exception e) {
                return o;
            }

            return o;
        }
    }

    private class VehicleInspectionDefectArraySerializer implements JsonSerializer<VehicleInspectionDefect[]> {
        public JsonElement serialize(VehicleInspectionDefect[] src, Type typeOfT, JsonSerializationContext context) {
            JsonArray array = new JsonArray();

            try {

                for (VehicleInspectionDefect defect : src) {
                    array.add(_gson.toJsonTree(defect, VehicleInspectionDefect.class));
                }

            } catch (Exception e) {
                return array;
            }
            return array;
        }
    }

    private class DefectListDeserializer implements JsonDeserializer<DefectList> {
        public DefectList deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            DefectList o = new DefectList();
            JsonObject jsonObj = json.getAsJsonObject();

            JsonArray array = jsonObj.getAsJsonArray("VehicleInspectionDefects");
            VehicleInspectionDefect[] list = new VehicleInspectionDefect[array.size()];
            for (int i = 0; i < list.length; i++) {
                list[i] = _gson.fromJson(array.get(i), VehicleInspectionDefect.class);
            }
            o.setDefectList(list);

            return o;
        }
    }

    private static class VehicleInspectionDefectSerializer implements JsonSerializer<VehicleInspectionDefect> {
        public JsonElement serialize(VehicleInspectionDefect src, Type typeOfT, JsonSerializationContext context) {
            JsonObject o = new JsonObject();

            o.addProperty("InspectionDefectType", src.getInspectionDefectType().getValue());

            return o;
        }
    }

    private static class VehicleInspectionDefectDeserializer implements JsonDeserializer<VehicleInspectionDefect> {
        public VehicleInspectionDefect deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            VehicleInspectionDefect o = new VehicleInspectionDefect();
            JsonObject jsonObj = json.getAsJsonObject();

            o.setInspectionDefectType((InspectionDefectType) GetEnum(jsonObj, "InspectionDefectType", InspectionDefectType.class));

            return o;
        }
    }

    private static class TeamDriverDeserializer implements JsonDeserializer<TeamDriver> {
        public TeamDriver deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            TeamDriver o = new TeamDriver();
            JsonObject jsonObj = json.getAsJsonObject();

            o.setStartTime(GetFormattedDate(jsonObj, "StartTime", DateUtility.getHomeTerminalDMOSoapDateTimeFormat()));
            o.setEndTime(GetFormattedDate(jsonObj, "EndTime", DateUtility.getHomeTerminalDMOSoapDateTimeFormat()));
            o.setEmployeeCode(GetString(jsonObj, "EmployeeCode"));
            o.setEmployeeId(GetString(jsonObj, "EmployeeId"));
            o.setDisplayName(GetString(jsonObj, "DisplayName"));
            o.setKMBUsername(GetString(jsonObj, "KMBUsername"));

            return o;
        }
    }

    private class FailureReportDeserializer implements JsonDeserializer<FailureReport> {
        public FailureReport deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            FailureReport o = new FailureReport();
            JsonObject jsonObj = json.getAsJsonObject();

            o.setCategory((FailureCategoryEnum) GetEnum(jsonObj, "Category", FailureCategoryEnum.class));
            o.setStartTime(GetFormattedDateTime(jsonObj, "StartTime", DateUtility.getHomeTerminalDMOSoapDateTimeFormat()));
            o.setStopTime(GetFormattedDateTime(jsonObj, "StopTime", DateUtility.getHomeTerminalDMOSoapDateTimeFormat()));
            o.setMessage(GetString(jsonObj, "Message"));

            return o;
        }
    }

    private class LocationDeserializer implements JsonDeserializer<Location> {
        public Location deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            Location o = new Location();
            JsonObject jsonObj = json.getAsJsonObject();

            o.setName(GetString(jsonObj, "Name"));
            o.setOdometerReading(GetFloat(jsonObj, "OdometerReading", o.getOdometerReading()));
            o.setEndOdometerReading(GetFloat(jsonObj, "EndOdometerReading", o.getEndOdometerReading()));

            // GpsInfo
            JsonObject gpsInfo = jsonObj.getAsJsonObject("GpsInfo");
            if (gpsInfo != null) {
                o.setGpsInfo(_gson.fromJson(gpsInfo, GpsLocation.class));
            }

            return o;
        }
    }

    private class LocationCodeArrayDeserializer implements JsonDeserializer<LocationCode[]> {
        public LocationCode[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

            JsonArray array = json.getAsJsonArray();

            LocationCode[] list = new LocationCode[array.size()];
            for (int i = 0; i < list.length; i++) {
                list[i] = (_gson.fromJson(array.get(i), LocationCode.class));
            }

            return list;
        }
    }

    private static class LocationCodeDeserializer implements JsonDeserializer<LocationCode> {
        public LocationCode deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            LocationCode o = new LocationCode();
            JsonObject jsonObj = json.getAsJsonObject();

            o.setCode(GetString(jsonObj, "Code"));
            o.setLocation(GetString(jsonObj, "Location"));

            return o;
        }
    }

    private class LogCheckerComplianceDatesArrayDeserializer implements JsonDeserializer<LogCheckerComplianceDates[]> {
        public LogCheckerComplianceDates[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

            JsonArray array = json.getAsJsonArray();

            LogCheckerComplianceDates[] list = new LogCheckerComplianceDates[array.size()];
            for (int i = 0; i < list.length; i++) {
                list[i] = (_gson.fromJson(array.get(i), LogCheckerComplianceDates.class));
            }

            return list;
        }
    }

    private static class LogCheckerComplianceDatesDeserializer implements JsonDeserializer<LogCheckerComplianceDates> {
        public LogCheckerComplianceDates deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            LogCheckerComplianceDates o = new LogCheckerComplianceDates();
            JsonObject jsonObj = json.getAsJsonObject();

            SimpleDateFormat dateTimeFormat = DateUtility.getDMOSoapDateTimeFormat();
            dateTimeFormat.setTimeZone(DateUtility.getHomeTerminalDateFormat().getTimeZone());

            o.setComplianceDate(GetFormattedDate(jsonObj, "ComplianceDate", dateTimeFormat));
            o.setItemEnum(GetInteger(jsonObj, "ItemEnum", o.getItemEnum()));
            o.setDescription(GetString(jsonObj, "Description"));
            o.setComplianceEndDate(GetFormattedDate(jsonObj, "ComplianceEndDate", dateTimeFormat));
            return o;
        }
    }

    private class GpsLocationListDeserializer implements JsonDeserializer<GpsLocation[]> {
        public GpsLocation[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

            JsonArray array = json.getAsJsonArray();

            GpsLocation[] list = new GpsLocation[array.size()];
            for (int i = 0; i < list.length; i++) {
                list[i] = (_gson.fromJson(array.get(i), GpsLocation.class));
            }

            return list;
        }
    }

    private static class GpsLocationDeserializer implements JsonDeserializer<GpsLocation> {
        public GpsLocation deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            GpsLocation o = new GpsLocation();
            JsonObject jsonObj = json.getAsJsonObject();

            SimpleDateFormat dateTimeFormat = DateUtility.getDMOSoapDateTimeFormat();
            dateTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

            o.setTimestampUtc(GetFormattedDate(jsonObj, "TimestampUtc", dateTimeFormat));
            o.setLatitudeDegrees(GetFloat(jsonObj, "LatitudeDegrees", o.getLatitudeDegrees()));
            o.setLongitudeDegrees(GetFloat(jsonObj, "LongitudeDegrees", o.getLongitudeDegrees()));

            // Decoded Info
            JsonObject jsonDecodedInfo = jsonObj.getAsJsonObject("DecodedInfo");
            if (jsonDecodedInfo != null) {
                DecodedLocation decodedLoc = new DecodedLocation();
                decodedLoc.setCity(GetString(jsonDecodedInfo, "City"));
                decodedLoc.setState(GetString(jsonDecodedInfo, "State"));
                decodedLoc.setPostalCode(GetString(jsonDecodedInfo, "PostalCode"));
                decodedLoc.setStreet(GetString(jsonDecodedInfo, "Street"));
                decodedLoc.setCounty(GetString(jsonDecodedInfo, "County"));
                decodedLoc.setCountry(GetString(jsonDecodedInfo, "Country"));
                o.setDecodedInfo(decodedLoc);
            }
            return o;
        }
    }

    private class UnassignedEobrFailurePeriodArraySerializer implements JsonSerializer<UnassignedEobrFailurePeriod[]> {
        public JsonElement serialize(UnassignedEobrFailurePeriod[] src, Type typeOfT, JsonSerializationContext context) {
            JsonArray array = new JsonArray();
            for (UnassignedEobrFailurePeriod ufp : src) {
                array.add(_gson.toJsonTree(ufp, UnassignedEobrFailurePeriod.class));
            }

            return array;
        }
    }

    private static class UnassignedEobrFailurePeriodSerializer implements JsonSerializer<UnassignedEobrFailurePeriod> {
        public JsonElement serialize(UnassignedEobrFailurePeriod src, Type typeOfT, JsonSerializationContext context) {
            JsonObject o = new JsonObject();

            o.addProperty("EobrIdentifier", src.getEobrId());
            o.addProperty("EobrSerialNumber", src.getEobrSerialNumber());
            o.addProperty("StartTime", DateUtility.getHomeTerminalDMOSoapDateTimeFormat().format(src.getStartTime()));
            o.addProperty("StopTime", DateUtility.getHomeTerminalDMOSoapDateTimeFormat().format(src.getStopTime()));
            o.addProperty("Message", src.getMessage());

            return o;
        }
    }

    private class LogRemarkItemArrayDeserializer implements JsonDeserializer<LogRemarkItem[]> {
        public LogRemarkItem[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

            JsonArray array = json.getAsJsonArray();

            LogRemarkItem[] list = new LogRemarkItem[array.size()];
            for (int i = 0; i < list.length; i++) {
                list[i] = (_gson.fromJson(array.get(i), LogRemarkItem.class));
            }

            return list;
        }
    }

    private static class LogRemarkItemDeserializer implements JsonDeserializer<LogRemarkItem> {
        public LogRemarkItem deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            LogRemarkItem o = new LogRemarkItem();
            JsonObject jsonObj = json.getAsJsonObject();

            o.setName(GetString(jsonObj, "Name"));
            o.setItemEnum(GetInteger(jsonObj, "ItemEnum", o.getItemEnum()));
            o.setIsActive(GetBoolean(jsonObj, "IsActive", o.getIsActive()));
            o.setLkupLogRemarkId(GetString(jsonObj, "LkupLogRemarkId"));

            return o;
        }
    }

    private class UnassignedDrivingPeriodArraySerializer implements JsonSerializer<UnassignedDrivingPeriod[]> {
        public JsonElement serialize(UnassignedDrivingPeriod[] src, Type typeOfT, JsonSerializationContext context) {
            JsonArray array = new JsonArray();
            for (UnassignedDrivingPeriod udp : src) {
                array.add(_gson.toJsonTree(udp, UnassignedDrivingPeriod.class));
            }

            return array;
        }
    }

    private class UnassignedDrivingPeriodSerializer implements JsonSerializer<UnassignedDrivingPeriod> {
        public JsonElement serialize(UnassignedDrivingPeriod src, Type typeOfT, JsonSerializationContext context) {
            JsonObject o = new JsonObject();

            SimpleDateFormat dateTimeFormat = DateUtility.getDMOSoapDateTimeFormat();
            dateTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

            o.addProperty("EobrIdentifier", src.getEobrId());
            o.addProperty("EobrSerialNumber", src.getEobrSerialNumber());
            if (src.getStartTime() == null) {
                o.addProperty("StartTime", "null");
            } else {
                o.addProperty("StartTime", dateTimeFormat.format(src.getStartTime()));
            }
            if (src.getStopTime() == null) {
                o.addProperty("StopTime", "null");
            } else {
                o.addProperty("StopTime", dateTimeFormat.format(src.getStopTime()));
            }
            o.add("StartLocation", _gson.toJsonTree(src.getStartLocation()));
            o.add("StopLocation", _gson.toJsonTree(src.getStopLocation()));
            o.addProperty("Distance", Float.toString(src.getDistance()));
            o.addProperty("StartOdometer", Float.toString(src.getStartOdometer()));
            o.addProperty("StopOdometer", Float.toString(src.getStopOdometer()));
            o.addProperty("EncompassId", src.getEncompassId());

            return o;
        }
    }

    private class FuelPurchaseArraySerializer implements JsonSerializer<FuelPurchase[]> {
        public JsonElement serialize(FuelPurchase[] src, Type typeOfT, JsonSerializationContext context) {
            JsonArray array = new JsonArray();
            for (FuelPurchase ufp : src) {
                array.add(_gson.toJsonTree(ufp, FuelPurchase.class));
            }

            return array;
        }
    }

    private static class FuelPurchaseSerializer implements JsonSerializer<FuelPurchase> {
        public JsonElement serialize(FuelPurchase src, Type typeOfT, JsonSerializationContext context) {
            JsonObject o = new JsonObject();

            o.addProperty("InvoiceNumber", src.getInvoiceNumber());
            o.addProperty("StateCode", src.getStateCode());
            o.addProperty("TractorNumber", src.getTractorNumber());
            o.addProperty("VendorName", src.getVendorName());
            o.addProperty("FuelAmount", src.getFuelAmount());
            o.addProperty("FuelCost", src.getFuelCost());
            o.addProperty("FuelClassification", src.getFuelClassification().getValue());
            o.addProperty("FuelUnit", src.getFuelUnit().getValue());
            if (src.getPurchaseDate() == null) {
                o.addProperty("PurchaseDate", "null");
            } else {
                o.addProperty("PurchaseDate", DateUtility.getHomeTerminalDMOSoapDateTimeFormat().format(src.getPurchaseDate()));
            }

            return o;
        }
    }

    private class DriverHoursAvailableSummaryArraySerializer implements JsonSerializer<DriverHoursAvailableSummary[]> {
        public JsonElement serialize(DriverHoursAvailableSummary[] src, Type typeOfT, JsonSerializationContext context) {
            JsonArray array = new JsonArray();
            for (DriverHoursAvailableSummary ufp : src) {
                array.add(_gson.toJsonTree(ufp, DriverHoursAvailableSummary.class));
            }

            return array;
        }
    }

    private static class DriverHoursAvailableSummarySerializer implements JsonSerializer<DriverHoursAvailableSummary> {
        public JsonElement serialize(DriverHoursAvailableSummary src, Type typeOfT, JsonSerializationContext context) {
            JsonObject o = new JsonObject();

            o.addProperty("DailyDutyTimeUsed", src.getDailyDutyTimeUsed());
            o.addProperty("DrivingTimeUsed", src.getDrivingTimeUsed());
            o.addProperty("EmployeeId", src.getEmployeeId());
            o.addProperty("DailyDutyHoursAllowed", src.getDailyDutyHoursAllowed());
            o.addProperty("WeeklyDutyTimeUsed", src.getWeeklyDutyTimeUsed());
            o.addProperty("DrivingHoursAllowed", Integer.toString(src.getDrivingHoursAllowed()));
            o.addProperty("IsShortHaulExceptionAvailable", Boolean.toString(src.getIsShortHaulExceptionAvailable()));
            o.addProperty("WeeklyDutyHoursAllowed", Integer.toString(src.getWeeklyDutyHoursAllowed()));
            if (src.getCalculationTimestamp() == null) {
                o.addProperty("CalculationTimestamp", "null");
            } else {
                o.addProperty("CalculationTimestamp", DateUtility.getHomeTerminalDMOSoapDateTimeFormat().format(src.getCalculationTimestamp()));
            }
            o.addProperty("DrivingHoursRestBreakAllowed", src.getDrivingHoursRestBreakAllowed());
            o.addProperty("DrivingTimeRestBreakUsed", src.getDrivingTimeRestBreakUsed());
            o.addProperty("ExemptLogTypeEnum", Integer.toString(src.getExemptLogTypeEnum().getValue()));
            o.addProperty("LastMotionPictureAuthorityId", src.getLastMotionPictureAuthorityId());
            o.addProperty("LastMotionPictureProductionId", src.getLastMotionPictureProductionId());
            o.addProperty("TrailerNumber", src.getTrailerNumber());
            o.addProperty("GPSLatitude", Double.toString(src.getGpsLatitude()));
            o.addProperty("GPSLongitude", Double.toString(src.getGpsLongitude()));
            o.addProperty("DutyStatusEnum", src.getDutyStatusEnum().getValue());
            o.addProperty("EobrSerialNumber", src.getEobrSerialNumber());
            return o;
        }
    }

    private class GpsLocationSerializer implements JsonSerializer<GpsLocation> {
        public JsonElement serialize(GpsLocation src, Type typeOfT, JsonSerializationContext context) {
            JsonObject o = new JsonObject();

            SimpleDateFormat dateTimestampUTCFormat = DateUtility.getDMOSoapDateTimestampUTCFormat();
            dateTimestampUTCFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

            if (src.getTimestampUtc() == null) {
                o.addProperty("TimestampUtc", "0001-01-01T00:00:00");
            } else {
                o.addProperty("TimestampUtc", dateTimestampUTCFormat.format(src.getTimestampUtc()));
            }
            o.addProperty("LatitudeDegrees", Float.toString(src.getLatitudeDegrees()));
            o.addProperty("LongitudeDegrees", Float.toString(src.getLongitudeDegrees()));
            o.add("DecodedInfo", _gson.toJsonTree(src.getDecodedInfo(), DecodedLocation.class));

            return o;
        }
    }

    private static class DecodedLocationSerializer implements JsonSerializer<DecodedLocation> {
        public JsonElement serialize(DecodedLocation src, Type typeOfT, JsonSerializationContext context) {
            JsonObject o = new JsonObject();

            o.addProperty("City", src.getCity());
            o.addProperty("State", src.getState());
            o.addProperty("PostalCode", src.getPostalCode());
            o.addProperty("Street", src.getStreet());
            o.addProperty("County", src.getCounty());
            o.addProperty("Country", src.getCountry());

            return o;
        }
    }

    private static class MobileDeviceSerializer implements JsonSerializer<MobileDevice> {
        public JsonElement serialize(MobileDevice src, Type typeOfT, JsonSerializationContext context) {
            JsonObject o = new JsonObject();
            o.addProperty("SerialNumber", src.getDeviceIMEI());
            o.addProperty("ModelNumber", src.getModel());
            o.addProperty("OSVersion", src.getOsType() + " " + src.getReleaseVersion());
            o.addProperty("KMBVersion", src.getKmbVersion());
            o.addProperty("IsComplianceTablet", src.isComplianceTablet());
            o.addProperty("IsCoPilotInstalled", src.isCoPilot());
            o.addProperty("EmployeeId", src.getLastEmployeeId());
            o.addProperty("KMBEobrSerialNumber", src.getLastKMBEOBRDeviceSerialNumber());
            return o;
        }
    }

    private static class MobileDeviceDeserializer implements JsonDeserializer<MobileDevice> {
        public MobileDevice deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObj = json.getAsJsonObject();
            MobileDevice o = new MobileDevice();
            o.setDeviceIMEI(GetString(jsonObj, "SerialNumber"));
            o.setModel(GetString(jsonObj, "ModelNumber"));
            o.setKmbVersion(GetString(jsonObj, "KMBVersion"));
            o.setComplianceTablet(GetBoolean(jsonObj, "IsComplianceTablet", false));
            o.setCoPilot(GetBoolean(jsonObj, "IsCoPilotInstalled", false));
            o.setLastEmployeeId(GetString(jsonObj, "EmployeeId"));
            o.setLastKMBEOBRDeviceSerialNumber(GetString(jsonObj, "KMBEobrSerialNumber"));
            return o;
        }
    }

    private static class ReportRequestSerializer implements JsonSerializer<EmployeeLogReportRequest> {
        public JsonElement serialize(EmployeeLogReportRequest src, Type typeOfT, JsonSerializationContext context) {
            SimpleDateFormat dateTimeFormat = DateUtility.getDMOSoapDateTimeFormat();
            dateTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

            JsonObject o = new JsonObject();

            o.addProperty("Email", src.getEmail());
            o.addProperty("BeginDate", dateTimeFormat.format(src.getBeginDate()));
            o.addProperty("EndDate", dateTimeFormat.format(src.getEndDate()));

            return o;
        }
    }

    private class EmployeeLogWithProvisionsSerializer implements JsonSerializer<EmployeeLogWithProvisions> {
        public JsonElement serialize(EmployeeLogWithProvisions src, Type typeOfT, JsonSerializationContext context) {
            JsonObject o = new JsonObject();

            o.addProperty("EmployeeCode", src.getEmployeeCode());
            o.addProperty("LogDate", DateUtility.getHomeTerminalDMOSoapDateTimeFormat().format(src.getLogDate()));

            if (src.getStartTime() != null) {
                o.addProperty("StartTime", DateUtility.getHomeTerminalDMOSoapDateTimeFormat().format(src.getStartTime()));
            }
            if (src.getEndTime() != null) {
                o.addProperty("EndTime", DateUtility.getHomeTerminalDMOSoapDateTimeFormat().format(src.getEndTime()));
            }

            if (src.getStartLocation() != null) {
                o.add("StartLocation", _gson.toJsonTree(src.getStartLocation()));
            }
            if (src.getEndLocation() != null) {
                o.add("EndLocation", _gson.toJsonTree(src.getEndLocation()));
            }

            o.addProperty("TotalDistance", Float.toString(src.getTotalDistance()));
            o.addProperty("TractorNumber", src.getTractorNumber());
            o.addProperty("ProvisionTypeEnum", src.getProvisionTypeEnum());

            return o;
        }
    }

    private class DataTransferFileStatusResponseDeserializer implements JsonDeserializer<DataTransferFileStatusResponse> {
        public DataTransferFileStatusResponse deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObj = json.getAsJsonObject();

            DataTransferFileStatusResponse o = new DataTransferFileStatusResponse();
            o.setStatus((DataTransferFileStatusEnum)GetEnum(jsonObj,  "Status", DataTransferFileStatusEnum.class));
            JsonArray array = GetArray(jsonObj, "ErrorMessageLineItems");
            String[] messageList = new String[0];
            if (array != null) {
                messageList = new String[array.size()];
                for (int i = 0; i < messageList.length; i++) {
                    messageList[i] = array.get(i).toString();
                }
            }

            o.setErrorMessageLineItems(messageList);

            return o;
        }
    }

    /**
     * Use HTTPURLConnection to communicate with webservice
     *
     * @param calledURL          = webservice URL
     * @param addRequestProperty = To add a single property to header (ex.
     *                           "newpassword,"+password) would add a property with a name of
     *                           newpassword and value of password
     * @return String from successfully creating the http request
     * @throws IOException
     */
    private String HTTPURLGetRequest(String calledURL, String addRequestProperty) throws IOException {
        return HTTPURLGetRequest(calledURL, addRequestProperty, HttpHelper.TIMEOUT_MS);
    }

    private String HTTPURLGetRequest(String calledURL, String addRequestProperty, int timeout) throws IOException {
        HashMap<String, String> headers = createHttpRequestHeaders(addRequestProperty);
        return _httpHelper.Get(calledURL, headers, timeout);
    }

    private byte[] HTTPURLByteStreamGetRequest(String calledURL, String addRequestProperty) throws IOException {
        HashMap<String, String> headers = createHttpRequestHeaders(addRequestProperty);
        return _httpHelper.GetByteStream(calledURL, headers, HttpHelper.TIMEOUT_MS);
    }

    private HashMap<String, String> createHttpRequestHeaders(String addRequestProperty) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("KmbUserToken", CreateKmbUserToken());

        if (addRequestProperty != null) {
            String[] property = addRequestProperty.split(",");
            headers.put(property[0], property[1]);
        }

        return headers;
    }

    /**
     * Use HTTPURLConnection to send info to the webservice
     *
     * @param calledURL          = webservice URL
     * @param addGzip            = add gzip compression or not
     * @param addRequestProperty = To add a single property to header (ex.
     *                           "newpassword,"+password) would add a property with a name of
     *                           newpassword and value of password
     * @param json               = JSON string to be sent up
     * @return the response body
     * @throws IOException
     */

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private String HTTPURLPostRequest(String calledURL, boolean addGzip, String addRequestProperty, String json) throws IOException {
        return HTTPURLPostRequest(calledURL, addGzip, addRequestProperty, json, HttpHelper.TIMEOUT_MS);
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private String HTTPURLPostRequest(String calledURL, boolean addGzip, String addRequestProperty, String json, int timeoutMs) throws IOException {
        HashMap<String, String> headers = createHttpRequestHeaders(addRequestProperty);
        //HTTPHelper Adds Content-Type header
        return _httpHelper.Post(calledURL, headers, json, timeoutMs);
    }

    private String CreateKmbUserToken() {
        try {
            JsonObject jsonObj = new JsonObject();
            Gson gson = new Gson();

            KmbUserInfo kmbUser = GlobalState.getInstance().getKmbUserInfo();
            if (kmbUser != null) {
                jsonObj.addProperty("KmbUsername", kmbUser.getKmbUsername());
                jsonObj.addProperty("KmbPassword", kmbUser.getKmbPassword());
                jsonObj.addProperty("DmoEmployeeId", kmbUser.getDmoEmployeeId());
                jsonObj.addProperty("KmbVersionNumber", kmbUser.getKmbVersionNumber());
            }

            CompanyConfigSettings companyConfig = GlobalState.getInstance().getCompanyConfigSettings(this._callingContext);
            if (companyConfig != null) {
                jsonObj.addProperty("DmoUsername", companyConfig.getDmoUsername());
                jsonObj.addProperty("DmoPassword", companyConfig.getDmoPasswordEncrypt());
                jsonObj.addProperty("CompanyName", companyConfig.getDmoCompanyName());
                jsonObj.addProperty("CompanyId", companyConfig.getDmoCompanyId());
            }

            return gson.toJson(jsonObj);
        } catch (Exception e) {
            return "";
        }
    }

    // 2/6/12 JHM - Currently only encodes spaces, but this could be a spot to
    // add others if needed.
    // Note: Tried the following and found they didn't need to be encoded: _-'.,
    private String AddURLEncoding(String s) {
        return s.replaceAll(" ", "%20");
    }

    //9/26/2016 AAZ - Adding Full Url Encoding because there is a scenario to send
    // User Entered Text (60 Chars) as a Querystring
    // this should handle any character
    private String EncodeStringForUrl(String s) {
        String encodedString;
        try {
            encodedString = URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.i("RESTWebServiceHelper", "Failed to Encode String  " + s);
            //Attempt to just replace spaces
            encodedString = AddURLEncoding(s);
        }
        return encodedString;
    }

    public EobrConfiguration SubmitEobrConfiguration(EobrConfiguration eobrConf) throws IOException {

        String URL = String.format(METHODNAME_SUBMITEOBRCONFIGURATION, _baseUrl);

        String json = _gson.toJson(eobrConf, EobrConfiguration.class);

        String result = HTTPURLPostRequest(URL, true, null, json);
        eobrConf = _gson.fromJson(result, EobrConfiguration.class);

        return eobrConf;
    }

    /* JSON data parsing helper functions */
    private static JsonArray GetArray(JsonObject jsonObj, String element) {
        if (jsonObj.has(element) && !jsonObj.get(element).isJsonNull()) {
            return jsonObj.getAsJsonArray(element);
        } else {
            return null;
        }
    }

    private static JsonElement GetElement(JsonObject jsonObj, String element) {
        if (jsonObj.has(element) && !jsonObj.get(element).isJsonNull()) {
            return jsonObj.get(element);
        } else {
            return null;
        }
    }

    private static boolean GetBoolean(JsonObject jsonObj, String element, boolean defaultVal) {
        if (jsonObj.has(element) && !jsonObj.get(element).isJsonNull()) {
            return jsonObj.get(element).getAsBoolean();
        } else {
            return defaultVal;
        }
    }

    private static int GetInteger(JsonObject jsonObj, String element, int defaultVal) {
        if (jsonObj.has(element) && !jsonObj.get(element).isJsonNull()) {
            return jsonObj.get(element).getAsInt();
        } else {
            return defaultVal;
        }
    }

    /* Integer vs. int - Integer allows for NULL values */
    private static Integer GetInteger(JsonObject jsonObj, String element, Integer defaultVal) {
        if (jsonObj.has(element) && !jsonObj.get(element).isJsonNull()) {
            return jsonObj.get(element).getAsInt();
        } else {
            return defaultVal;
        }
    }

    private static float GetFloat(JsonObject jsonObj, String element, float defaultVal) {
        if (jsonObj.has(element) && !jsonObj.get(element).isJsonNull()) {
            return jsonObj.get(element).getAsFloat();
        } else {
            return defaultVal;
        }
    }

    /* Float vs. float - Float allows for NULL values */
    private static Float GetFloat(JsonObject jsonObj, String element, Float defaultVal) {
        if (jsonObj.has(element) && !jsonObj.get(element).isJsonNull()) {
            return jsonObj.get(element).getAsFloat();
        } else {
            return defaultVal;
        }
    }

    private static Double GetDouble(JsonObject jsonObj, String element, Double defaultVal) {
        if (jsonObj.has(element) && !jsonObj.get(element).isJsonNull()) {
            return jsonObj.get(element).getAsDouble();
        } else {
            return defaultVal;
        }
    }

    private static long GetLong(JsonObject jsonObj, String element, long defaultVal) {
        if (jsonObj.has(element) && !jsonObj.get(element).isJsonNull()) {
            return jsonObj.get(element).getAsLong();
        } else {
            return defaultVal;
        }
    }

    private static String GetString(JsonObject jsonObj, String element) {
        if (jsonObj.has(element) && !jsonObj.get(element).isJsonNull()) {
            return jsonObj.get(element).getAsString();
        } else {
            return null;
        }
    }

    /***
     * Converts a DateTime Write Format element to a DateTime object. Can additionally be converted
     * to a Date if needed by calling toDate() on the DateTime object if it's not null.
     *
     * @param jsonObj JsoneElement
     * @param element Name of element as string
     * @return JodaTime DateTime or null
     */
    private static DateTime GetDateTime(JsonObject jsonObj, String element) {
        if (jsonObj.has(element) && !jsonObj.get(element).isJsonNull()) {
            try {
                String dateText = jsonObj.get(element).getAsJsonPrimitive().getAsString();
                return parseDateTimeWireFormat(dateText);

            } catch (Exception e) {
                Log.i("RESTWebServiceHelper", "Failed to parse to [date] for element " + element);
            }
        }

        return null;
    }

    public static DateTime parseDateTimeWireFormat(String inputString) {
        // Guard Clause
        if (inputString == null || inputString.isEmpty()) {
            return null;
        }

        String regexPattern = "\\/Date\\((\\d+)([\\+\\-])?(\\d{2})?(\\d{2})?\\)\\/";
        Matcher result = Pattern.compile(regexPattern).matcher(inputString);
        if (result.find()) {

            long millsSinceEpoch = 0;
            int zoneOffsetMinutes = 0;
            boolean shouldAdd = false;

            if (result.group(1) != null) {
                // No Time Zone Offset
                String millsSinceEpochString = result.group(1);
                millsSinceEpoch = Long.parseLong(millsSinceEpochString);
            }
            if (result.group(2) != null) {
                // Time Zone Offset
                shouldAdd = result.group(2).equals("+");
                String offsetHoursString = result.group(3);
                String offsetMinutesString = result.group(4);
                zoneOffsetMinutes = (Integer.parseInt(offsetHoursString) * 60) + Integer.parseInt(offsetMinutesString);
            }

            DateTime dateTime = new DateTime(millsSinceEpoch);

            // Handle TimeZone
            if (zoneOffsetMinutes > 0) {
                if (shouldAdd) {
                    dateTime = dateTime.plusMinutes(zoneOffsetMinutes);
                } else {
                    dateTime = dateTime.minusMinutes(zoneOffsetMinutes);
                }
            }
            return dateTime;
        }

        return null;
    }

    private static Date GetFormattedDate(JsonObject jsonObj, String element, SimpleDateFormat dateFormat) {
        if (jsonObj.has(element) && !jsonObj.get(element).isJsonNull()) {
            try {
                return dateFormat.parse(GetString(jsonObj, element));
            } catch (ParseException e) {
                Log.i("RESTWebServiceHelper", "Failed to parse to [date] for element " + element);
            }
        }

        return null;
    }

    private DateTime GetFormattedDateTime(JsonObject jsonObj, String element, SimpleDateFormat dateFormat) {
        if (jsonObj.has(element) && !jsonObj.get(element).isJsonNull()) {
            try {
                return new DateTime(dateFormat.parse(GetString(jsonObj, element)));
            } catch (ParseException e) {
                Log.i("RESTWebServiceHelper", "Failed to parse to [date] for element " + element);
            }
        }

        return null;
    }

    private static Object GetEnum(JsonObject jsonObj, String element, Class<?> c) {
        if (c == CanadaDeferralTypeEnum.class) {
            if (!jsonObj.has(element)) {
                return new CanadaDeferralTypeEnum(CanadaDeferralTypeEnum.NONE);
            } else {
                return new CanadaDeferralTypeEnum(GetInteger(jsonObj, element, CanadaDeferralTypeEnum.NONE));
            }
        } else if (c == DatabusTypeEnum.class) {
            if (!jsonObj.has(element)) {
                return new DatabusTypeEnum(DatabusTypeEnum.NULL);
            } else {
                return new DatabusTypeEnum(GetInteger(jsonObj, element, DatabusTypeEnum.NULL));
            }
        } else if (c == DataProfileEnum.class) {
            if (!jsonObj.has(element)) {
                return new DataProfileEnum(DataProfileEnum.NULL);
            } else {
                return new DataProfileEnum(GetInteger(jsonObj, element, DataProfileEnum.NULL));
            }
        } else if (c == DriverTypeEnum.class) {
            if (!jsonObj.has(element)) {
                return new DriverTypeEnum(DriverTypeEnum.NULL);
            } else {
                return new DriverTypeEnum(GetInteger(jsonObj, element, DriverTypeEnum.NULL));
            }
        } else if (c == DrivingNotificationTypeEnum.class) {
            if (!jsonObj.has(element)) {
                return new DrivingNotificationTypeEnum(DrivingNotificationTypeEnum.NULL);
            } else {
                return new DrivingNotificationTypeEnum(GetInteger(jsonObj, element, DrivingNotificationTypeEnum.NULL));
            }
        } else if (c == DutyStatusEnum.class) {
            if (!jsonObj.has(element)) {
                return new DutyStatusEnum(DutyStatusEnum.NULL);
            } else {
                return new DutyStatusEnum(GetInteger(jsonObj, element, DutyStatusEnum.NULL));
            }
        } else if (c == EngineRecordTypeEnum.class) {
            if (!jsonObj.has(element)) {
                return new EngineRecordTypeEnum(EngineRecordTypeEnum.NULL);
            } else {
                return new EngineRecordTypeEnum(GetInteger(jsonObj, element, EngineRecordTypeEnum.NULL));
            }
        } else if (c == EventTypeEnum.class) {
            if (!jsonObj.has(element)) {
                return new EventTypeEnum(EventTypeEnum.ANYTYPE);
            } else {
                return new EventTypeEnum(GetInteger(jsonObj, element, EventTypeEnum.ANYTYPE));
            }
        } else if (c == EobrCommunicationModeEnum.class) {
            if (!jsonObj.has(element)) {
                return new EobrCommunicationModeEnum(EobrCommunicationModeEnum.NULL);
            } else {
                return new EobrCommunicationModeEnum(GetInteger(jsonObj, element, EobrCommunicationModeEnum.NULL));
            }
        } else if (c == FailureCategoryEnum.class) {
            if (!jsonObj.has(element)) {
                return new FailureCategoryEnum(FailureCategoryEnum.NULL);
            } else {
                return new FailureCategoryEnum(GetInteger(jsonObj, element, FailureCategoryEnum.NULL));
            }
        } else if (c == FuelClassificationEnum.class) {
            if (!jsonObj.has(element)) {
                return new FuelClassificationEnum(FuelClassificationEnum.NULL);
            } else {
                return new FuelClassificationEnum(GetInteger(jsonObj, element, FuelClassificationEnum.NULL));
            }
        } else if (c == FuelUnitEnum.class) {
            if (!jsonObj.has(element)) {
                return new FuelUnitEnum(FuelUnitEnum.NULL);
            } else {
                return new FuelUnitEnum(GetInteger(jsonObj, element, FuelUnitEnum.NULL));
            }
        } else if (c == RuleSetTypeEnum.class) {
            if (!jsonObj.has(element)) {
                return new RuleSetTypeEnum(RuleSetTypeEnum.NULL);
            } else {
                return new RuleSetTypeEnum(GetInteger(jsonObj, element, RuleSetTypeEnum.NULL));
            }
        } else if (c == TimeZoneEnum.class) {
            if (!jsonObj.has(element)) {
                return new TimeZoneEnum(TimeZoneEnum.NULL);
            } else {
                return new TimeZoneEnum(GetInteger(jsonObj, element, TimeZoneEnum.NULL));
            }
        } else if (c == InspectionTypeEnum.class) {
            if (!jsonObj.has(element)) {
                return new InspectionTypeEnum(InspectionTypeEnum.NULL);
            } else {
                return new InspectionTypeEnum(GetInteger(jsonObj, element, InspectionTypeEnum.NULL));
            }
        } else if (c == InspectionDefectType.class) {
            if (!jsonObj.has(element)) {
                return new InspectionDefectType(InspectionDefectType.NULL);
            } else {
                return new InspectionDefectType(GetInteger(jsonObj, element, InspectionDefectType.NULL));
            }
        } else if (c == ExemptLogTypeEnum.class) {
            if (!jsonObj.has(element)) {
                return new ExemptLogTypeEnum(ExemptLogTypeEnum.NULL);
            } else {
                return new ExemptLogTypeEnum(GetInteger(jsonObj, element, ExemptLogTypeEnum.NULL));
            }
        }else if (c == DataTransferFileStatusEnum.class) {
            if (!jsonObj.has(element)) {
                return new DataTransferFileStatusEnum(DataTransferFileStatusEnum.UNKNOWN);
            } else {
                return new DataTransferFileStatusEnum(GetInteger(jsonObj, element, DataTransferFileStatusEnum.UNKNOWN));
            }
        } else {
            return null;
        }
    }

    private RuleSetTypeEnum[] ParseRuleSetTypeEnumArray(JsonArray array) {
        RuleSetTypeEnum[] returnVal = new RuleSetTypeEnum[array.size()];
        for (int i = 0; i < array.size(); i++) {
            int value = array.get(i).getAsInt();
            if (value >= 0) {
                returnVal[i] = new RuleSetTypeEnum(value);
            } else {
                returnVal[i] = null;
            }
        }
        return returnVal;
    }

    public ApplicationUpdateInfo CheckForUpdates(String applicationName, String platform, String architecture, String versionNumber, boolean autoUpdateEnabled) throws IOException {

        String URL = String.format(METHODNAME_CHECKFORUPDATES, GlobalState.getInstance().getAppSettings(this._callingContext).getKmbActivationRESTUrl(), applicationName, platform, architecture, versionNumber, Boolean.toString(autoUpdateEnabled));

        ApplicationUpdateInfo appUpdateInfo = null;
        try {
            String result = HTTPURLGetRequest(URL, null);
            appUpdateInfo = _gson.fromJson(result, ApplicationUpdateInfo.class);
        } catch (Exception e) {

            Log.e("UnhandledCatch", e.getMessage() + ": " + Log.getStackTraceString(e));
        }

        return appUpdateInfo;

    }

    public ArrayList<GpsLocation> ReverseGeoCode(GpsLocation[] gpsLocations) throws IOException {

        String URL = String.format(METHODNAME_REVERSEGEOCODE, _baseUrl);

        String json = _gson.toJson(gpsLocations);

        String result = HTTPURLPostRequest(URL, true, null, json);

        GpsLocation[] gpsLocationList = _gson.fromJson(result, GpsLocation[].class);

        ArrayList<GpsLocation> list = new ArrayList<GpsLocation>(Arrays.asList(gpsLocationList));

        return list;
    }

    public void SubmitDriverHoursAvailableSummaries(DriverHoursAvailableSummary[] hoursAvailableSummaries) throws IOException {

        String URL = String.format(METHODNAME_SUBMITDRIVERHOURSAVAILABLESUMMARIES, _baseUrl);

        String json = _gson.toJson(hoursAvailableSummaries);
        HTTPURLPostRequest(URL, false, null, json);
    }

    public void SubmitFuelPurchases(FuelPurchase[] fuelPurchases) throws IOException {

        String URL = String.format(METHODNAME_SUBMITFUELPURCHASES, _baseUrl);

        String json = _gson.toJson(fuelPurchases);

        HTTPURLPostRequest(URL, false, null, json);
    }

    public void SubmitEngineRecords(EngineRecordList engineRecords) throws IOException {

        String URL = String.format(METHODNAME_SUBMITENGINERECORDS, _baseUrl);

        IotDataSerialization serializer = new IotDataSerialization();

        String json = serializer.getGson().toJson(engineRecords);

        HTTPURLPostRequest(URL, false, null, json);
    }

    public void SubmitTripRecords(TripRecordList tripRecords) throws IOException {

        String URL = String.format(METHODNAME_SUBMITTRIPRECORDS, _baseUrl);

        IotDataSerialization serializer = new IotDataSerialization();

        String json = serializer.getGson().toJson(tripRecords);

        HTTPURLPostRequest(URL, false, null, json);
    }

    public void SubmitEventDataRecords(EventDataRecordList eventDataRecords) throws IOException {

        String URL = String.format(METHODNAME_SUBMITEVENTDATARECORD, _baseUrl);

        IotDataSerialization serializer = new IotDataSerialization();

        String json = serializer.getGson().toJson(eventDataRecords);

        HTTPURLPostRequest(URL, false, null, json);
    }

    public void SubmitLogs(EmployeeLogList employeeLogs) throws IOException {

        String URL = String.format(METHODNAME_SUBMITLOGS, _baseUrl);

        if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
            URL = convertToLogCheckerMandateUrl(URL);
        }
        //FEATURE TOGGLE
        if (GlobalState.getInstance().getFeatureService().getSetMobileStartTimestampToNull()) {
            for (EmployeeLog log : employeeLogs.getEmployeeLogList()) {
                log.setMobileStartTimestamp(null);
            }
        }

        String json = _gson.toJson(employeeLogs);
        HTTPURLPostRequest(URL, false, null, json, TIMEOUT_DOWNLOAD_LOGS);

    }

    public Boolean SendGetDataTransferMechanismStatus(String transferId) throws IOException {

        String URL = String.format(METHODNAME_GETDATATRANSFERMECHANISMSTATUS, _baseUrl, transferId);

        if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
            URL = convertToLogCheckerMandateUrl(URL);
        }

        String result = HTTPURLGetRequest(URL, null);
        Boolean response = false;

        try {
            response = _gson.fromJson(result, Boolean.class);

        } catch (JsonSyntaxException jsonEx) {
            ErrorLogHelper.RecordException(_callingContext, jsonEx);
        }

        return response;
    }

    public void SendSetDataTransferMechanismStatus(String transferId) throws IOException {
        String URL = String.format(METHODNAME_SETDATATRANSFERMECHANISMSTATUS, _baseUrl, transferId);
        if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
            URL = convertToLogCheckerMandateUrl(URL);
        }

        HTTPURLGetRequest(URL, null);
    }

    public void SubmitVehicleInspections(VehicleInspectionList inspectionList) throws IOException {

        String URL = String.format(METHODNAME_SUBMITBVEHICLEINSPECTIONS, _baseUrl);

        String json = _gson.toJson(inspectionList);

        HTTPURLPostRequest(URL, false, null, json);
    }

    public MobileDevice SubmitMobileDevice(MobileDevice mobileDevice) throws IOException {

        String URL = String.format(METHODNAME_SUBMITMOBILEDEVICE, _baseUrl);

        String json = _gson.toJson(mobileDevice, MobileDevice.class);
        String resultJson = HTTPURLPostRequest(URL, false, null, json);
        MobileDevice result = null;
        try {
            result = _gson.fromJson(resultJson, MobileDevice.class);
        } catch (JsonSyntaxException jsonEx) {
            ErrorLogHelper.RecordException(_callingContext, jsonEx);
        }

        return result;
    }

    public EmployeeLogWithProvisions[] getEmployeeLogWithProvisions(String employeeId, Date startDate, Date endDate) throws IOException {
        String startDateString = DateUtility.getDateNoSeparatorsFormat().format(startDate);
        String endDateString = DateUtility.getDateNoSeparatorsFormat().format(endDate);
        String url = String.format(METHODNAME_DOWNLOAD_EMPLOYEE_LOG_WITH_PROVISIONS, _baseUrl, employeeId, startDateString, endDateString);
        url = convertToLogCheckerMandateUrl(url);

        String response = HTTPURLGetRequest(url, null);
        EmployeeLogWithProvisions[] provisionsList =  null;
        try {
             provisionsList = _gson.fromJson(response, EmployeeLogWithProvisions[].class);
        } catch (JsonSyntaxException jsonEx) {
            ErrorLogHelper.RecordException(_callingContext, jsonEx);
        }catch (Exception e) {
            ErrorLogHelper.RecordException(_callingContext, e);
        }
        return provisionsList;
    }

    public void SubmitEmployeeLogWithProvisions(EmployeeLogWithProvisions[] employeeLogWithProvisions) throws IOException {
        String URL = String.format(METHODNAME_SUBMITEMPLOYEELOGWITHPROVISIONS, _baseUrl);
        String json = _gson.toJson(employeeLogWithProvisions);
        HTTPURLPostRequest(URL, false, null, json);
    }

    public void SubmitEmployeeLogRevisions(EmployeeLogRevision[] empLogRevisions) throws IOException {
        String URL = String.format(METHODNAME_SUBMITEMPLOYEELOGREVISIONS, _baseUrl);
        String json = _gson.toJson(empLogRevisions);
        HTTPURLPostRequest(URL, false, null, json);
    }

    public String GetDataTransferRoadsideFile(RoadsideDataTransferMethodEnum transferMethod, Date startDate, Date endDate, String outputFileComment, String eldIdentifier) {
        String startDateString = DateUtility.getDateNoSeparatorsFormat().format(startDate);
        String endDateString = DateUtility.getDateNoSeparatorsFormat().format(endDate);
        String encodedComment = EncodeStringForUrl(outputFileComment);
        String encodedEldIdentifier = EncodeStringForUrl(eldIdentifier);
        String url = convertToLogCheckerMandateUrl(String.format(METHODNAME_GETDATATRANSFERROADSIDEFILE, _baseUrl, transferMethod.getValue(), startDateString, endDateString, encodedComment, encodedEldIdentifier));

        String filename = null;
        try {
            String response = HTTPURLGetRequest(url, null, TIMEOUT_DOWNLOAD_LOGS);
            filename = _gson.fromJson(response, String.class);
        } catch (Exception e) {
            Log.e("UnhandledCatch", e.getMessage() + ": " + Log.getStackTraceString(e));
        }
        return filename;
    }

    public DataTransferFileStatusResponse GetDataTransferFileStatus(String filename, RoadsideDataTransferMethodEnum transferMethod) throws IOException {
        String url = convertToLogCheckerMandateUrl(String.format(METHODNAME_GETROADSIDEFILESTATUSBYFILENAME, _baseUrl, filename, transferMethod.getValue()));

        DataTransferFileStatusResponse result = null;
        try {
            String response = HTTPURLGetRequest(url, null);
            result = _gson.fromJson(response, DataTransferFileStatusResponse.class);
        } catch (Exception e) {
            Log.e("UnhandledCatch", e.getMessage() + ": " + Log.getStackTraceString(e));
        }
        return result;
    }

    /**
     * Tests a connection to the web services and checks if it is redirected to
     * a secondary authentication page, like a Wi-Fi network sign on page. If
     * there does seem to be a secondary authentication page, it returns the URL
     * to send the user to. Otherwise, it returns null. <br />
     * <br />
     * TODO As of Android 5.1, network communication will automatically fall
     * back to the mobile network if wifi fails. That will make it seem like a
     * wifi network doesn't have secondary authentication even though it does.
     *
     * @return The secondary authentication URL, or null
     */
    public String SecondaryAuthenticationUrl() {
        String secondaryAuthUrl = null;

        // This is a similar implementation as Android's
        // WifiWatchdogStateMachine.isWalledGardenConnection()
        // Tries to get a 204 status from a Google URL over HTTP
        // Most walled gardens have issues with HTTPS before authentication
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(WALLED_GARDEN_URL);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setInstanceFollowRedirects(false);
            urlConnection.setConnectTimeout(WALLED_GARDEN_SOCKET_TIMEOUT_MS);
            urlConnection.setReadTimeout(WALLED_GARDEN_SOCKET_TIMEOUT_MS);
            urlConnection.setUseCaches(false);
            urlConnection.getInputStream();
            // We got a valid response, but not from the real google
            if (urlConnection.getResponseCode() != 204) {
                String locationHeader = urlConnection.getHeaderField("Location");
                if (locationHeader != null) {
                    URL locationHeaderUrl = new URL(locationHeader);
                    if (!url.getHost().equals(locationHeaderUrl.getHost())) {
                        secondaryAuthUrl = locationHeaderUrl.toString();
                    }
                }
            }
        } catch (Exception e) {
            Log.d("UnhandledCatch", e.getMessage(), e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return secondaryAuthUrl;
    }

    public void UploadFile(String uploadToken, String clientFilename, long totalBytesToSend, byte[] fileChunkToAppend) throws IOException {

        String URL = String.format(METHODNAME_UPLOADFILE, _baseUrl, uploadToken, AddURLEncoding(clientFilename));

        String strFileChunk = Base64.encodeBytes(fileChunkToAppend);
        JsonPrimitive prim = new JsonPrimitive(strFileChunk);

        HTTPURLPostRequest(URL, false, "totalBytesToSend," + Long.toString(totalBytesToSend), prim.toString());
    }

    public ArrayList<FeatureToggle> DownloadFeatureToggles(String companyId)  throws IOException {
        String URL = String.format(METHODNAME_DOWNLOADFEATURETOGGLES, _baseUrl, 0, companyId); //AppPlatform is 0 for Android
        String result = HTTPURLGetRequest(URL, null);
        FeatureToggle[] featureToggles = null;

        try {
            featureToggles = _gson.fromJson(result, FeatureToggle[].class);
        } catch (JsonSyntaxException jsonEx) {
            ErrorLogHelper.RecordException(_callingContext, jsonEx);
        }

        ArrayList<FeatureToggle> list = new ArrayList<FeatureToggle>(
                Arrays.asList(featureToggles));

        return list;
    }

    public boolean SubmitGeotabDriverChange(GeotabDriver geotabDriver) throws IOException{
        boolean isSubmited = false;
        String result = "";
        String URL = String.format(METHODNAME_SUBMITGEOTABDRIVERCHANGE, _baseUrl);
        String json = _gson.toJson(geotabDriver);

        result = HTTPURLPostRequest(URL, false, null, json);
        try {
            GeotabDriver geoTabDriverReturn = null;
            geoTabDriverReturn = _gson.fromJson(result, GeotabDriver.class);
            if(geoTabDriverReturn.getResponseMessage().isEmpty()){
                isSubmited = true;
            }else {
                ErrorLogHelper.RecordMessage(geoTabDriverReturn.getResponseMessage());
            }
        } catch (JsonSyntaxException jsonEx) {
            ErrorLogHelper.RecordException(_callingContext, jsonEx);
        }

        return isSubmited;
    }

}
