package com.jjkeller.kmbapi.controller.dataaccess;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.jjkeller.kmbapi.common.Constants;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.dataaccess.db.GeotabHOSDataPersist;
import com.jjkeller.kmbapi.controller.dataaccess.db.PersistBase;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.controller.utility.ErrorLogHelper;
import com.jjkeller.kmbapi.enums.EmployeeLogEldEnum;
import com.jjkeller.kmbapi.enums.EmployeeLogEldIdentifierEnum;
import com.jjkeller.kmbapi.featuretoggle.IFeatureToggleService;
import com.jjkeller.kmbapi.proxydata.ProxyBase;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractDBAdapter<T extends ProxyBase> extends PersistBase<T> {

    protected static final String TAG = "KMBDBAdapter";

    private static DatabaseHelper _dbHelper;
    private SQLiteDatabase _database;

    public static final String DATABASE_PATH = "/data/data/com.jjkeller.kmb/databases/";
    public static final String DATABASE_NAME = "kmb";

    // NOTE: the intent of the BUILD_VERSION constants is to collect the definitive list of
    //       "official" customer versions that KMB needs to support a database upgrade from
    // The build version value is the number of days since 1/1/2000 {SQL Statement:  PRINT DATEDIFF(DAY, '1/1/2000', GETDATE()) }
    //here is the powershell command for reference: (((Get-Date).Date) - (Get-Date -Date "1/1/2000")).Days

    protected static final int BUILD_VERSION_000 = 1;
    protected static final int BUILD_VERSION_001 = 4330;    // 2011.11.29
    protected static final int BUILD_VERSION_002 = 4386;    // 2012.01.04
    protected static final int BUILD_VERSION_003 = 4687;    // 2012.10.31
    protected static final int BUILD_VERSION_004 = 4744;    // 2012.12.27
    protected static final int BUILD_VERSION_005 = 4771;    // 2013.01.23
    protected static final int BUILD_VERSION_006 = 4783;    // 2013.02.04
    protected static final int BUILD_VERSION_007 = 4870;    // 2013.05.02
    protected static final int BUILD_VERSION_008 = 4913;    // 2013.06.14
    protected static final int BUILD_VERSION_009 = 4955;    // 2013.07.26
    protected static final int BUILD_VERSION_010 = 4975;    // 2013.08.15
    protected static final int BUILD_VERSION_011 = 5045;    // 2013.10.24
    protected static final int BUILD_VERSION_012 = 5073;    // 2013.11.21
    protected static final int BUILD_VERSION_013 = 5161;    // 2014.02.17
    protected static final int BUILD_VERSION_014 = 5253;    // 2014.05.20
    protected static final int BUILD_VERSION_015 = 5317;    // 2014.07.23
    protected static final int BUILD_VERSION_016 = 5381;    // 2014.09.25
    protected static final int BUILD_VERSION_017 = 5452;    // 2014.12.06
    protected static final int BUILD_VERSION_018 = 5500;    // 2015.01.22
    protected static final int BUILD_VERSION_019 = 5522;    // 2015.02.13
    protected static final int BUILD_VERSION_020 = 5557;    // 2015.03.20
    protected static final int BUILD_VERSION_021 = 5625;    // 2015.05.27
    protected static final int BUILD_VERSION_022 = 5637;    // 2015.06.08
    protected static final int BUILD_VERSION_023 = 5690;    // 2015.07.31
    protected static final int BUILD_VERSION_024 = 5805;    // 2015.11.23
    protected static final int BUILD_VERSION_025 = 5861;    // 2016.01.18
    protected static final int BUILD_VERSION_026 = 5886;    // 2016.02.12
    protected static final int BUILD_VERSION_027 = 5905;    // 2016.03.02
    protected static final int BUILD_VERSION_028 = 5912;    // 2016.03.09
    protected static final int BUILD_VERSION_029 = 5918;    // 2016.03.15
    protected static final int BUILD_VERSION_030 = 5920;    // 2016.03.17
    protected static final int BUILD_VERSION_031 = 5948;    // 2016.04.14
    protected static final int BUILD_VERSION_032 = 5949;    // 2016.04.15
    protected static final int BUILD_VERSION_033 = 5959;    // 2016.04.25
    protected static final int BUILD_VERSION_034 = 5960;    // 2016.04.26
    protected static final int BUILD_VERSION_035 = 5963;    // 2016.04.29
    protected static final int BUILD_VERSION_036 = 5964;    // 2016.05.05
    protected static final int BUILD_VERSION_037 = 6002;    // 2016.06.07
    protected static final int BUILD_VERSION_038 = 6003;    // 2016.06.08
    protected static final int BUILD_VERSION_039 = 6008;    // 2016.06.10
    protected static final int BUILD_VERSION_040 = 6016;    // 2016.06.21
    protected static final int BUILD_VERSION_041 = 6018;    // 2016.06.23
    protected static final int BUILD_VERSION_042 = 6088;    // 2016.09.01
    protected static final int BUILD_VERSION_043 = 6107;    // 2016.09.20
    protected static final int BUILD_VERSION_044 = 6108;    // 2016.09.21
    protected static final int BUILD_VERSION_045 = 6114;    // 2016.09.27
    protected static final int BUILD_VERSION_046 = 6116;    // 2016.09.29
    protected static final int BUILD_VERSION_047 = 6117;    // 2016.09.30
    protected static final int BUILD_VERSION_048 = 6118;    // 2016.10.06
    protected static final int BUILD_VERSION_049 = 6127;    // 2016.10.10
    protected static final int BUILD_VERSION_050 = 6148;    // 2016.10.31
    protected static final int BUILD_VERSION_051 = 6225;    // 2017.01.16
    protected static final int BUILD_VERSION_052 = 6227;    // 2017.01.18
    protected static final int BUILD_VERSION_053 = 6235;    // 2017.01.26
    protected static final int BUILD_VERSION_054 = 6248;    // 2017.02.08
    protected static final int BUILD_VERSION_055 = 6250;    // 2017.02.10
    protected static final int BUILD_VERSION_056 = 6254;    // 2017.02.14
    protected static final int BUILD_VERSION_057 = 6267;    // 2017.02.27
    protected static final int BUILD_VERSION_058 = 6277;    // 2017.03.09
    protected static final int BUILD_VERSION_059 = 6282;    // 2017.03.14
    protected static final int BUILD_VERSION_060 = 6283;    // 2017.03.15
    protected static final int BUILD_VERSION_061 = 6320;    // 2017.04.21
    protected static final int BUILD_VERSION_062 = 6330;    // 2017.05.01
    protected static final int BUILD_VERSION_063 = 6333;    // 2017.05.04
    protected static final int BUILD_VERSION_064 = 6337;    // 2017.05.08
    protected static final int BUILD_VERSION_065 = 6341;    // 2017.05.12
    protected static final int BUILD_VERSION_066 = 6344;    // 2017.05.15
    protected static final int BUILD_VERSION_067 = 6375;    // 2017.06.15
    protected static final int BUILD_VERSION_068 = 6409;    // 2017.07.19
    protected static final int BUILD_VERSION_069 = 6432;    // 2017.08.11
    protected static final int BUILD_VERSION_070 = 6506;    // 2017.10.24
    protected static final int BUILD_VERSION_071 = 6515;    // 2017.11.02
    protected static final int BUILD_VERSION_072 = 6547;    // 2017.12.04
    protected static final int BUILD_VERSION_073 = 6564;    // 2017.12.20
    protected static final int BUILD_VERSION_074 = 6603;    // 2018.01.29
    protected static final int BUILD_VERSION_075 = 6725;    // 2018.05.31
    protected static final int BUILD_VERSION_076 = 6767;    // 2018.07.12
    protected static final int BUILD_VERSION_077 = 6768;    // 2018.07.13
    protected static final int BUILD_VERSION_078 = 9999;

    // The build that the database is considered current through
    public static final int BUILD_VERSION_CURRENT = BUILD_VERSION_077;

    // Field that designates which Table is used for derived class
    private String _databaseTableName;

    // Table name constants
    protected static final String DB_TABLE_COMPANY = "Company";
    protected static final String DB_TABLE_USER = "User";
    protected static final String DB_TABLE_EMPLOYEELOG = "EmployeeLog";
    protected static final String DB_TABLE_LOGEVENT = "LogEvent";
    protected static final String DB_TABLE_EMPLOYEERULE = "EmployeeRule";
    protected static final String DB_TABLE_EOBRDEVICE = "EobrDevice";
    protected static final String DB_TABLE_EOBRDIAGNOSTICCOMMAND = "EobrDiagnosticCommand";
    protected static final String DB_TABLE_ENGINERECORD = "EngineRecord";
    protected static final String DB_TABLE_EVENTDATARECORD = "EventDataRecord";
    protected static final String DB_TABLE_FUELPURCHASE = "FuelPurchase";
    protected static final String DB_TABLE_LOCATIONCODE = "LocationCode";
    protected static final String DB_TABLE_LOGFAILUREREPORT = "LogFailureReport";
    protected static final String DB_TABLE_LOGTEAMDRIVER = "LogTeamDriver";
    protected static final String DB_TABLE_LOGCHECKERCOMPLIANCEDATES = "LogCheckerComplianceDates";
    protected static final String DB_TABLE_ROUTEPOSITION = "RoutePosition";
    protected static final String DB_TABLE_TRIPRECORD = "TripRecord";
    protected static final String DB_TABLE_UNASSIGNEDDRIVINGPERIOD = "UnassignedDrivingPeriod";
    protected static final String DB_TABLE_UNASSIGNEDEOBRFAILUREPERIOD = "UnassignedEobrFailurePeriod";
    protected static final String DB_TABLE_DATAUSAGE = "DataUsage";
    protected static final String DB_TABLE_VEHICLEINSPECTION = "VehicleInspection";
    protected static final String DB_TABLE_VEHICLEINSPECTIONDEFECT = "VehicleInspectionDefect";
    protected static final String DB_TABLE_ZSVERSION = "ZsVersion";
    protected static final String DB_TABLE_LOGREMARKITEM = "LogRemarkItem";
    protected static final String DB_TABLE_ELDEVENT = "EldEvent";    // old table -- dropped
    protected static final String DB_TABLE_EMPLOYEELOGELDEVENT = "EmployeeLogEldEvent";
    protected static final String DB_TABLE_LOGPERSONALCONVEYANCE = "LogPersonalConveyance";     // old table -- renamed
    protected static final String DB_TABLE_LOGHYRAIL = "LogHyrail";
    protected static final String DB_TABLE_EMPLOYEELOGREVISION = "EmployeeLogRevision";
    protected static final String DB_TABLE_ApplicationState = "ApplicationState";
    protected static final String DB_TABLE_EMPLOYEELOGWITHPROVISIONS = "EmployeeLogWithProvisions";
    protected static final String DB_TABLE_KMBENCOMPASSUSER = "KMBEncompassUser";
    protected static final String DB_TABLE_GEOTABHOSDATA = "GeotabHOSData";
    protected static final String DB_TABLE_GEOTABEVENTRECORD = "GeotabEventRecord";
    protected static final String DB_TABLE_MOTIONPICTUREAUTHORITY = "MotionPictureAuthority";
    protected static final String DB_TABLE_MOTIONPICTUREPRODUCTION = "MotionPictureProduction";
    protected static final String DB_TABLE_DATATRANSFERMECHANISMSTATUS = "DataTransferMechanismStatus";
    protected static final String DB_TABLE_DATATRANSFERFILESTATUS = "DataTransferFileStatus";
    protected static final String DB_TABLE_DRIVINGEVENTREASSIGNMENTMAPPING = "DrivingEventReassignmentMapping";
    protected static final String DB_TABLE_ELDREGISTRATIONINFO = "EldRegistrationInfo";
    protected static final String DB_TABLE_FEATURETOGGLE = "FeatureToggle";

    //DO NOT UPDATE THIS QUERY
    //This will only be used by 062 version
    private static final String DB_CREATE_COMPANY_FOR_VERSION_062 =
            "create table IF NOT EXISTS \"Company\" (Key integer primary key autoincrement, "
                    + "ActivationCode nvarchar(50) not null,"
                    + "CompanyName nvarchar(60) not null, "
                    + "CompanyId uniqueidentifier null,"
                    + "Username nvarchar(100) not null, "
                    + "Password nvarchar(50) not null, "
                    + "DailyLogStartTime integer not null, "
                    + "LogPurgeDayCount integer not null, "
                    + "IsActivated boolean not null, "
                    + "EobrDiscoveryPasskey nvarchar(50) not null, "
                    + "EobrCommunicationMode integer not null, "
                    + "EobrSleepModeMinutes integer not null, "
                    + "EobrCollectionRateSeconds integer not null, "
                    + "ActivationDate datetime not null,"
                    + "AllowDriversCompleteDVIR boolean not null,"
                    + "GeneratePreTripDVIRWithDefectAlert boolean not null,"
                    + "DriverStartDistance numeric not null, "
                    + "DriverStopMinutes integer not null, "
                    + "MaxAcceptableSpeed numeric not null, "
                    + "MaxAcceptableTach integer not null, "
                    + "HardBrakeDecelerationSpeed numeric not null,"
                    + "UseKmbWebApiServices boolean not null,"
                    + "MultipleUsersAllowed boolean not null,"
                    + "DriveStartSpeed integer not null default 5,"
                    + "MandateDrivingStopTimeMinutes integer not null default 5,"
                    + "IsGeotabEnabled boolean not null DEFAULT false, "
                    + "IsMotionPictureEnabled boolean not null default false)";
    
    // Table create constants
    private static final String DB_CREATE_COMPANY =
            "create table IF NOT EXISTS \"Company\" (Key integer primary key autoincrement, "
                    + "ActivationCode nvarchar(50) not null,"
                    + "CompanyName nvarchar(60) not null, "
                    + "CompanyId uniqueidentifier null,"
                    + "Username nvarchar(100) not null, "
                    + "Password nvarchar(50) not null, "
                    + "DailyLogStartTime integer not null, "
                    + "LogPurgeDayCount integer not null, "
                    + "IsActivated boolean not null, "
                    + "EobrDiscoveryPasskey nvarchar(50) not null, "
                    + "EobrCommunicationMode integer not null, "
                    + "EobrSleepModeMinutes integer not null, "
                    + "EobrCollectionRateSeconds integer not null, "
                    + "ActivationDate datetime not null,"
                    + "AllowDriversCompleteDVIR boolean not null,"
                    + "GeneratePreTripDVIRWithDefectAlert boolean not null,"
                    + "DriverStartDistance numeric not null, "
                    + "DriverStopMinutes integer not null, "
                    + "MaxAcceptableSpeed numeric not null, "
                    + "MaxAcceptableTach integer not null, "
                    + "HardBrakeDecelerationSpeed numeric not null,"
                    + "UseKmbWebApiServices boolean not null,"
                    + "MultipleUsersAllowed boolean not null,"
                    + "DriveStartSpeed integer not null default 5,"
                    + "MandateDrivingStopTimeMinutes integer not null default 5,"
                    + "IsGeotabEnabled boolean not null DEFAULT false, "
                    + "IsMotionPictureEnabled boolean not null default false, "
                    + "IsAutoAssignUnIdentifiedEvents boolean not null default false);";


    private static final String DB_CREATE_USER =
            "create table IF NOT EXISTS User (Key integer primary key autoincrement, "
                    + "EmployeeId uniqueidentifier, "
                    + "CompanyKey integer not null, "
                    + "EmployeeCode nvarchar(11) not null, "
                    + "FullName nvarchar(39) not null, "
                    + "UserName nvarchar(50) not null, "
                    + "Password nvarchar(50) not null, "
                    + "LastLoginTimestamp datetime not null, "
                    + "LastLogoutTimestamp datetime null, "
                    + "LastSubmitTimestamp datetime null, "
                    + "HomeTerminalDOTNumber nvarchar(10) not null, "
                    + "HomeTerminalAddressLine1 nvarchar(30) not null, "
                    + "HomeTerminalAddressLine2 nvarchar(30) not null, "
                    + "HomeTerminalCity nvarchar(20) not null, "
                    + "HomeTerminalStateAbbrev nvarchar(2) not null, "
                    + "HomeTerminalZipCode nvarchar(10) not null, "
                    + "DriverLicenseState nvarchar(2) not null default '', "
                    + "DriverLicenseNumber nvarchar(25) not null default '', "
                    + "LastName nvarchar(20) not null default '', "
                    + "FirstName nvarchar(15) not null default ''); ";

    private static final String DB_CREATE_EMPLOYEELOG =
            "create table IF NOT EXISTS \"EmployeeLog\" (Key integer primary key autoincrement, "
                    + "UserKey integer not null,"
                    + "LogDate datetime not null, "
                    + "TotalLogDistance numeric not null, "
                    + "HasReturnedToLocation boolean not null, "
                    + "DriverType integer not null, "
                    + "RuleSet integer not null, "
                    + "Timezone integer not null, "
                    + "MobileStartTimestamp datetime null, "
                    + "MobileEndTimestamp datetime null, "
                    + "MobileRecordedDistance numeric null, "
                    + "MobileEOBRIdentifier nvarchar(50) null, "
                    + "TractorNumbers nvarchar(100) null, "
                    + "TrailerNumbers nvarchar(100) null, "
                    + "TrailerPlate nvarchar(100) null, "
                    + "ShipmentInfo nvarchar(100) null, "
                    + "VehiclePlate nvarchar(100) null, "
                    + "LogSourceStatusEnum integer not null, "
                    + "IsShortHaulExceptionUsed boolean not null, "
                    + "WeeklyResetStartTimestamp datetime null, "
                    + "CanadaDeferralType integer not null,"
                    + "IsHaulingExplosives boolean not null,"
                    + "IsWeeklyResetUsed boolean not null,"
                    + "IsWeeklyResetUsedOverridden boolean not null,"
                    + "IsExemptFrom30MinBreakRequirement boolean not null,"
                    + "IsOperatesSpecificVehiclesForOilField boolean not null,"
                    + "ExemptLogType integer not null,"
                    + "IsNonCDLShortHaulExceptionUsed boolean not null,"
                    + "IsCertified boolean not null default false, "
                    + "IsExemptFromELDUse boolean not null default false"
                    + ");";

    private static final String DB_CREATE_EMPLOYEERULE =
            "create table IF NOT EXISTS EmployeeRule (Key integer primary key autoincrement, "
                    + "Distance numeric not null, "
                    + "Minutes integer not null, "
                    + "EmployeeId uniqueidentifier unique not null, "
                    + "TimezoneType integer not null, "
                    + "RulesetType integer not null, "
                    + "DriverType integer not null, "
                    + "Is34HourReset boolean not null, "
                    + "IsShortHaulException boolean not null, "
                    + "AdditionalRulesets nvarchar(100) null, "
                    + "IntCDRuleset integer not null, "
                    + "IntUSRuleset integer not null, "
                    + "DataProfile integer not null, "
                    + "IsHaulingExplosivesAllowed boolean not null, "
                    + "IsHaulingExplosivesDefault boolean not null, "
                    + "IsOperatesSpecificVehiclesForOilField boolean not null, "
                    + "DistanceUnits string not null,"
                    + "IsPersonalConveyanceAllowed boolean not null,"
                    + "IsHyrailUseAllowed boolean not null,"
                    + "IsMobileExemptLogAllowed boolean not null,"
                    + "ExemptLogType integer not null,"
                    + "IsExemptFrom30MinBreakRequirement boolean not null,"
                    + "ExemptFromEldUse boolean not null default false,"
                    + "ExemptFromEldUseComment nvarchar(4000) not null default '',"
                    + "DriveStartSpeed integer not null default 5,"
                    + "MandateDrivingStopTimeMinutes integer not null default 5,"
                    + "YardMoveAllowed boolean not null default false,"
                    + "IsNonRegDrivingAllowed boolean not null default false)";

    private static final String DB_CREATE_EOBRDDEVICE =
            "create table IF NOT EXISTS EobrDevice (Key integer primary key autoincrement, "
                    + "SerialNumber nvarchar(50) not null,"
                    + "TractorNumber nvarchar(50) not null, "
                    + "DatabusType integer not null, "
                    + "SleepModeMinutes integer not null, "
                    + "DataCollectionRate integer not null, "
                    + "FirmwareVersion nvarchar(50) not null, "
                    + "DiscoveryPasskey nvarchar(50) not null, "
                    + "OdometerCalibrationDate datetime null, "
                    + "EobrOdometer numeric null, "
                    + "DashboardOdometer numeric null, "
                    + "SpeedometerThreshold numeric not null, "
                    + "TachometerThreshold numeric not null, "
                    + "HardBrakeThreshold numeric not null, "
                    + "Generation integer not null default 0, "
                    + "IsSubmitted boolean not null, "
                    + "LastPowerCycleResetDate datetime null, "
                    + "ClockSyncOffset long null default null, "
                    + "ClockSyncDateUTC datetime null default null, "  // ClockSyncDate is UTC (As all timestamps should be)
                    + "MajorFirmwareVersion integer not null default 0, "
                    + "MinorFirmwareVersion integer not null default 0, "
                    + "PatchFirmwareVersion integer not null default 0, "
                    + "VIN nvarchar(20) null, "
                    + "LastEventReferenceTimestamp datetime null);";

    private static final String DB_CREATE_EOBRDIAGNOSTICCOMMAND =
            "create table IF NOT EXISTS EobrDiagnosticCommand (Key integer primary key autoincrement, "
                    + "DmoCommandId uniqueidentifier unique not null, "
                    + "SerialNumber nvarchar(50) not null,"
                    + "Command text not null,"
                    + "ResponseTimestamp datetime null, "
                    + "Response text null);";

    private static final String DB_CREATE_ENGINERECORD =
            "create table IF NOT EXISTS EngineRecord (Key integer primary key autoincrement, "
                    + "EobrSerialNumber nvarchar(100) not null,"
                    + "EobrTractorNumber nvarchar(100) not null, "
                    + "EobrTimestamp datetime not null, "
                    + "DriverEmployeeId nvarchar(100) not null, "
                    + "Speedometer numeric not null, "
                    + "Odometer numeric not null, "
                    + "Tachometer numeric not null, "
                    + "GpsTimestamp datetime null, "
                    + "GpsLatitude numeric null, "
                    + "GpsLongitude numeric null, "
                    + "EobrOverallStatus integer not null, "
                    + "RecordType integer not null, "
                    + "FuelEconomyAverage numeric null, "
                    + "FuelEconomyInstant numeric null, "
                    + "FuelUseTotal numeric null, "
                    + "CruiseControlSet boolean null, "
                    + "TransmissionSelected nvarchar(2) null, "
                    + "TransmissionAttained nvarchar(2) null, "
                    + "BrakePressure numeric null, "
                    + "IsSubmitted boolean not null);";

    private static final String DB_CREATE_EVENTDATARECORD =
            "create table IF NOT EXISTS EventDataRecord (Key integer primary key autoincrement, "
                    + "DriverEmployeeId nvarchar(100) not null, "
                    + "EobrSerialNumber nvarchar(100) not null,"
                    + "EobrTimestamp datetime not null, "
                    + "EventType integer not null, "
                    + "EventData integer null, "
                    + "Odometer numeric not null, "
                    + "GpsLatitude numeric null, "
                    + "GpsLongitude numeric null, "
                    + "IsSubmitted boolean not null, "
                    + "Speedometer numeric null, "
                    + "Tachometer numeric null);";

    private static final String DB_CREATE_FUELPURCHASE =
            "create table IF NOT EXISTS FuelPurchase (Key integer primary key autoincrement, "
                    + "FuelAmount numeric not null, "
                    + "FuelUnit integer not null, "
                    + "FuelClassification integer not null, "
                    + "PurchaseDate datetime unique not null, "
                    + "StateCode nvarchar(2) not null, "
                    + "VendorName nvarchar(50) null, "
                    + "InvoiceNumber nvarchar(50) null, "
                    + "FuelCost numeric null, "
                    + "TractorNumber nvarchar(50) not null, "
                    + "IsSubmitted boolean not null);";

    private static final String DB_CREATE_LOCATIONCODE =
            "create table IF NOT EXISTS LocationCode (Key integer primary key autoincrement, "
                    + "CompanyKey integer not null, "
                    + "Code nvarchar(50) not null, "
                    + "Location nvarchar(100) not null);";

    private static final String DB_CREATE_LOGFAILUREREPORT =
            "create table IF NOT EXISTS LogFailureReport (Key integer primary key autoincrement, "
                    + "EmployeeLogKey integer not null, "
                    + "Category integer not null, "
                    + "StartTime datetime not null, "
                    + "StopTime datetime null, "
                    + "Message nvarchar(100) null);";

    private static final String DB_CREATE_LOGTEAMDRIVER =
            "create table IF NOT EXISTS LogTeamDriver (Key integer primary key autoincrement, "
                    + "EmployeeLogKey integer not null, "
                    + "StartTime datetime not null, "
                    + "EndTime datetime null, "
                    + "EmployeeCode nvarchar(11) not null, "
                    + "DisplayName nvarchar(50) not null, "
                    + "KMBUsername nvarchar(60) not null DEFAULT '', "
                    + "TimeZone integer not null default 0);";

    private static final String DB_CREATE_LOGCHECKERCOMPLIANCEDATES =
            "create table IF NOT EXISTS LogCheckerComplianceDates (Key integer primary key autoincrement, "
                    + "ComplianceDate datetime not null, "
                    + "ItemEnum integer not null, "
                    + "Description nvarchar(100) not null, "
                    + "ComplianceEndDate datetime null);";

    private static final String DB_CREATE_ROUTEPOSITION =
            "create table IF NOT EXISTS RoutePosition (Key integer primary key autoincrement, "
                    + "EobrId nvarchar(50) not null, "
                    + "GpsTimestamp datetime not null, "
                    + "GpsLatitude numeric not null, "
                    + "GpsLongitude numeric not null, "
                    + "Odometer numeric not null, "
                    + "IsSubmitted boolean not null, "
                    + "EobrSerialNumber nvarchar(50) not null);";

    private static final String DB_CREATE_TRIPRECORD =
            "create table IF NOT EXISTS " + DB_TABLE_TRIPRECORD + " (Key integer primary key autoincrement, "
                    + "EmployeeId uniqueidentifier not null, "
                    + "EobrSerialNumber nvarchar(50) not null, "
                    + "EobrTractorNumber nvarchar(100) not null, "
                    + "TripNumber integer not null, "
                    + "IgnitionState integer not null, "
                    + "Odometer numeric not null, "
                    + "TripSecs integer not null, "
                    + "TripDist numeric not null, "
                    + "IdleSecs integer not null, "
                    + "GpsLatitude numeric null, "
                    + "GpsLongitude numeric null, "
                    + "MaxSpeed numeric not null, "
                    + "TripFuel numeric not null, "
                    + "Timestamp datetime not null, "
                    + "AllowedSpeed numeric not null, "
                    + "AllowedTach numeric not null, "
                    + "IsSubmitted boolean not null,"
                    + "MaxEngRPM integer not null,"
                    + "AvgEngRPM integer not null);";

    private static final String DB_CREATE_UNASSIGNEDDRIVINGPERIOD =
            "create table IF NOT EXISTS UnassignedDrivingPeriod (Key integer primary key autoincrement, "
                    + "EobrIdentifier nvarchar(50) not null,"
                    + "StartTime datetime not null, "
                    + "StartGPSTimestamp datetime null, "
                    + "StartLatitudeDegrees numeric null, "
                    + "StartLongitudeDegrees numeric null, "
                    + "StopTime datetime null, "
                    + "StopGPSTimestamp datetime null, "
                    + "StopLatitudeDegrees numeric null, "
                    + "StopLongitudeDegrees numeric null, "
                    + "Distance numeric null, "
                    + "IsSubmitted boolean not null, "
                    + "IsClaimed boolean not null, "
                    + "EobrSerialNumber nvarchar(50) not null, "
                    + "StartOdometer numeric null, "
                    + "StopOdometer numeric null, "
                    + "EncompassId uniqueidentifier null);";


    private static final String DB_CREATE_UNASSIGNEDEOBRFAILUREPERIOD =
            "create table IF NOT EXISTS UnassignedEobrFailurePeriod (Key integer primary key autoincrement, "
                    + "EobrIdentifier nvarchar(50) not null, "
                    + "StartTime datetime not null, "
                    + "StopTime datetime null, "
                    + "Message nvarchar(100) null, "
                    + "IsSubmitted boolean not null, "
                    + "EobrSerialNumber nvarchar(50) not null);";

    private static final String DB_CREATE_DATAUSAGE =
            "create table IF NOT EXISTS DataUsage (Key integer primary key autoincrement, "
                    + "DeviceId nvarchar(100) not null, "
                    + "UsageDate datetime not null, "
                    + "TransmittedBytes numeric not null, "
                    + "ReceivedBytes numeric not null, "
                    + "NetworkEnum integer not null, "
                    + "Uri nvarchar(250) null, "
                    + "IsSubmitted boolean not null);";

    private static final String DB_CREATE_VEHICLEINSPECTION =
            "create table IF NOT EXISTS VehicleInspection (Key integer primary key autoincrement, "
                    + "EobrSerialNumber nvarchar(100) null, "
                    + "EobrTractorNumber nvarchar(100) null, "
                    + "TrailerNumber nvarchar(100) null, "
                    + "InspectionTimestamp datetime null, "
                    + "OdometerReading numeric null, "
                    + "IsConditionSatisfactory  boolean not null, "
                    + "AreDefectsCorrected  boolean not null, "
                    + "AreCorrectionsNotNeeded  boolean null, "
                    + "ReviewedByName  nvarchar(100) null, "
                    + "ReviewedByDate datetime null, "
                    + "Notes text null, "
                    + "IsPoweredUnit  boolean not null, "
                    + "CreatedByUserKey integer not null, "
                    + "InspectionType integer not null, "
                    + "CertifiedByName nvarchar(100) null, "
                    + "CertifiedByDate datetime null, "
                    + "IsSubmitted boolean not null, "
                    + "SubmitTimestamp datetime null, "
                    + "ReviewedByEmployeeId nvarchar(36) null);";

    private static final String DB_CREATE_VEHICLEINSPECTIONDEFECT =
            "create table IF NOT EXISTS VehicleInspectionDefect (Key integer primary key autoincrement, "
                    + "VehicleInspectionKey integer null, "
                    + "Defect integer null);";

    private static final String DB_CREATE_ZSVERSION =
            "create table IF NOT EXISTS zsVersion (CurrentMajor integer not null, "
                    + "CurrentMinor integer not null, "
                    + "CurrentBuild integer not null, "
                    + "CurrentRevision integer not null, "
                    + "ChangeDate datetime not null);";

    private static final String DB_CREATE_LOGREMARKITEM =
            "create table IF NOT EXISTS LogRemarkItem (Key integer primary key autoincrement, "
                    + "Name nvarchar(100) not null, "
                    + "ItemEnum integer not null,"
                    + "LkupLogRemarkId nvarchar(100) not null, "
                    + "IsActive boolean not null, "
                    + "ChangeDate datetime not null); ";

    private static final String DB_CREATE_EMPLOYEELOGELDEVENT =
            "create table IF NOT EXISTS \"EmployeeLogEldEvent\" (Key integer primary key autoincrement, "
                    + "DriverOriginatorUserId uniqueidentifier null, "
                    + "EncompassOriginatorUserId uniqueidentifier null, "
                    + "UnidentifiedUserId uniqueidentifier null, "
                    + "EobrSerialNumber nvarchar(50) null, "
                    + "LogKey integer null, "
                    + "EventSequenceIdNumber integer not null, "
                    + "EventRecordStatus integer null, "
                    + "EventRecordOrigin integer null, "
                    + "EventType integer not null, "
                    + "EventCode integer not null, "
                    + "EventDateTime datetime not null, "
                    + "RuleSet integer not null, "
                    + "AccumulatedVehicleMiles integer null, "
                    + "Odometer integer null, "
                    + "Distance integer null, "
                    + "EngineHours numeric null, "
                    + "Latitude numeric null, "
                    + "Longitude numeric null, "
                    + "LatitudeStatusCode nvarchar(1) null, "
                    + "LongitudeStatusCode nvarchar(1) null, "
                    + "IsGpsAtReducedPrecision boolean null, "
                    + "DistanceSinceLastCoordinates numeric null, "
                    + "EldMalfunctionIndicatorStatus boolean null, "
                    + "DriverDataDiagnosticEventIndicatorStatus boolean null, "
                    + "EventComment nvarchar(60) null, "
                    + "DriversLocationDescription nvarchar(60) null, "
                    + "EventDataCheck nvarchar(2) null, "
                    + "DiagnosticCode nvarchar(1) null, "
                    + "Geolocation nvarchar(100) null, "
                    + "TractorNumber nvarchar(50) null, "
                    + "VehiclePlate nvarchar(100) null, "
                    + "TrailerNumber nvarchar(50) null, "
                    + "TrailerPlate nvarchar(100) null, "
                    + "ShipmentInfo nvarchar(100) null, "
                    + "LogRemark text null, "
                    + "OriginalEvent boolean null, "
                    + "IsEventDateTimeValidated boolean not null, "
                    + "EndOdometer numeric null, "
                    + "LogRemarkDateTime datetime null,"
                    + "IsManuallyEditedByKMBUser boolean not null,"
                    + "GpsTimestamp datetime null, "
                    + "EditDuration integer null, "
                    + "MotionPictureAuthorityId uniqueidentifier null, "
                    + "MotionPictureProductionId uniqueidentifier null, "
                    + "EncompassClusterPK int64 not null default 0, "
                    + "UnidentifiedEventStatus integer not null default 0, "
                    + "IsReviewed boolean not null default 0, "
                    + "RelatedEvent numeric not null default 0, "
                    + "UnidentifiedEventConfidenceLevel integer null, "
                    + "UnidentifiedEventSuggestedDriver uniqueidentifier null)";

    private static final String DB_CREATE_EMPLOYEELOGWITHPROVISIONS =
            "create table IF NOT EXISTS EmployeeLogWithProvisions (Key integer primary key autoincrement, "
                    + "EmployeeLogKey integer not null, "
                    + "EmployeeLogEldEventKey integer not null default 0, "
                    + "StartTime datetime not null, "
                    + "StartOdometer numeric not null, "
                    + "StartLocationName nvarchar(100) not null, "
                    + "StartLatitudeDegrees numeric null, "
                    + "StartLongitudeDegrees numeric null, "
                    + "EndTime datetime null, "
                    + "EndOdometer numeric null,"
                    + "EndLocationName nvarchar(100) null,"
                    + "EndLatitudeDegrees numeric null,"
                    + "EndLongitudeDegrees numeric null,"
                    + "TotalDistance numeric null,"
                    + "TractorNumber nvarchar(50) not null,"
                    + "IsSubmitted boolean not null, "
                    + "ProvisionTypeEnum integer not null) ";

    private static final String DB_CREATE_LOGPERSONALCONVEYANCE =
            "create table IF NOT EXISTS LogPersonalConveyance (Key integer primary key autoincrement, "
                    + "EmployeeLogKey integer not null, "
                    + "StartTime datetime not null, "
                    + "StartOdometer numeric not null, "
                    + "StartLocationName nvarchar(100) not null, "
                    + "StartLatitudeDegrees numeric null, "
                    + "StartLongitudeDegrees numeric null, "
                    + "EndTime datetime null, "
                    + "EndOdometer numeric null,"
                    + "EndLocationName nvarchar(100) null,"
                    + "EndLatitudeDegrees numeric null,"
                    + "EndLongitudeDegrees numeric null,"
                    + "TotalDistance numeric null,"
                    + "TractorNumber nvarchar(50) not null,"
                    + "IsSubmitted boolean not null ) ";

    private static final String DB_CREATE_LOGHYRAIL =
            "create table IF NOT EXISTS LogHyrail (Key integer primary key autoincrement, "
                    + "EmployeeLogKey integer not null, "
                    + "StartTime datetime not null, "
                    + "StartOdometer numeric not null, "
                    + "StartLocationName nvarchar(100) not null, "
                    + "StartLatitudeDegrees numeric null, "
                    + "StartLongitudeDegrees numeric null, "
                    + "EndTime datetime null, "
                    + "EndOdometer numeric null,"
                    + "EndLocationName nvarchar(100) null,"
                    + "EndLatitudeDegrees numeric null,"
                    + "EndLongitudeDegrees numeric null,"
                    + "TotalDistance numeric null,"
                    + "TractorNumber nvarchar(50) not null,"
                    + "IsSubmitted boolean not null ) ";

    private static final String DB_CREATE_EMPLOYEELOGREVISION =
            "create table if NOT EXISTS EmployeeLogRevision (Key integer primary key autoincrement, "
                    + "EmployeeLogDate datetime not null, "
                    + "EmployeeCode nvarchar(20) not null, "
                    + "RevisionType integer not null,"
                    + "IsSubmitted boolean not null ) ";

    private static final String DB_CREATE_ApplicationState =
            "create table if NOT EXISTS ApplicationState (Key integer primary key autoincrement,"
                    + "EventSequenceId integer not null);";

    private static final String DB_CREATE_KMBENCOMPASSUSER =
            "create table if NOT EXISTS KMBEncompassUser (Key integer primary key autoincrement, "
                    + "UserId uniqueidentifier null, "
                    + "LastName nvarchar(20) not null, "
                    + "FirstName nvarchar(20) not null, "
                    + "UserName nvarchar(50) not null ) ";

    private static final String DB_CREATE_GEOTABHOSDATA =
            "create table if NOT EXISTS GeotabHOSData (Key integer primary key autoincrement,"
                    + "DriverId numeric not null, "
                    + "TimestampUtc datetime not null, "
                    + "GpsLatitude numeric null, "
                    + "GpsLongitude numeric null, "
                    + "Speedometer numeric not null, "
                    + "Tachometer numeric not null, "
                    + "Odometer numeric not null, "
                    + "TripOdometer numeric null, "
                    + "EngineHours numeric null, "
                    + "TripEngineSeconds numeric null, "
                    + "GpsValid boolean not null, "
                    + "IgnitionOn boolean not null, "
                    + "EngineActivityDetected boolean not null, "
                    + "DateTimeValid boolean not null, "
                    + "VehicleId numeric not null, "
                    + "EventDataRecordKey integer not null, "
                    + "IsSubmitted boolean not null, "
                    + "SpeedFromEngine boolean not null default 0, "
                    + "OdometerFromEngine boolean not null default 0, "
                    + "OriginalTimestampUtc datetime null default null, "
                    + "GpsUncertDistance numeric not null default 0, "
                    + "OrigOdometer numeric not null default 0)";

    public static final String DB_CREATE_GEOTABEVENTRECORD =
            "create table if NOT EXISTS GeotabEventRecord (Key integer primary key autoincrement,"
                    + "DriverId numeric not null, "
                    + "VehicleId numeric not null, "
                    + "TimestampUtc datetime not null, "
                    + "EventType integer not null, "
                    + "EventData integer null, "
                    + "GeotabHOSDataKey numeric null);";

    public static final String DB_CREATE_MOTIONPICTUREAUTHORITY =
            "create table if NOT EXISTS MotionPictureAuthority (Key integer primary key autoincrement,"
                    + "MotionPictureAuthorityId uniqueidentifier not null, "
                    + "Name nvarchar(30) not null, "
                    + "AddressLine1 nvarchar(30) null, "
                    + "AddressLine2 nvarchar(30) null, "
                    + "City nvarchar(20) null, "
                    + "State nvarchar(2) null, "
                    + "ZipCode nvarchar(10) null, "
                    + "BusinessHours nvarchar(15) null, "
                    + "DOTNumber nvarchar(10) not null, "
                    + "CompanyKey numeric not null, "
                    + "IsActive boolean not null);";

    public static final String DB_CREATE_MOTIONPICTUREPRODUCTION =
            "create table if NOT EXISTS MotionPictureProduction (Key integer primary key autoincrement,"
                    + "MotionPictureProductionId uniqueidentifier not null, "
                    + "Name nvarchar(30) not null, "
                    + "AddressLine1 nvarchar(30) null, "
                    + "AddressLine2 nvarchar(30) null, "
                    + "City nvarchar(20) null, "
                    + "State nvarchar(2) null, "
                    + "ZipCode nvarchar(10) null, "
                    + "BusinessHours nvarchar(15) null, "
                    + "MotionPictureAuthorityId uniqueidentifier not null, "
                    + "CompanyKey numeric not null, "
                    + "IsActive boolean not null);";

    public static final String DB_CREATE_DATATRANSFERMECHANISMSTATUS =
            "create table if NOT EXISTS DataTransferMechanismStatus (Key integer primary key autoincrement,"
                    + "TransferId uniqueidentifier not null, "
                    + "DateScheduledToTransfer date not null, "
                    + "DateTransferred date null, "
                    + "DateOfNextTransfer date null, "
                    + "WasSuccessful boolean not null);";

    public static final String DB_CREATE_DATATRANSFERFILESTATUS =
            "create table if NOT EXISTS DataTransferFileStatus (Key integer primary key autoincrement, "
                    + "Filename nvarchar(250) not null, "
                    + "CreateDate datetime not null, "
                    + "UserKey integer not null, "
                    + "AttemptCount integer not null, "
                    + "Status integer not null, "
                    + "RoadsideDataTransferMethod integer not null, "
                    + "WasNotificationDisplayed boolean not null);";

    public static final String DB_CREATE_DRIVINGEVENTREASSIGNMENTMAPPING =
            "CREATE TABLE IF NOT EXISTS \"" + DB_TABLE_DRIVINGEVENTREASSIGNMENTMAPPING + "\"(Key INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "DriverToAssignEventToId uniqueidentifier NOT NULL," +
                    "EventComment nvarchar(60) NOT NULL, " +
                    "RelatedEvent integer not null default 0, " +
                    "IsSubmitted boolean not null default 0)";

    public static final String DB_CREATE_ELDREGISTRATIONINFO =
            "create table if NOT EXISTS EldRegistrationInfo (Key integer primary key autoincrement, "
                    + "FmcsaRegistrationId nvarchar(4) not null, "
                    + "FmcsaProviderName nvarchar(120) not null, "
                    + "Name nvarchar(100) not null, "
                    + "EldIdentifier nvarchar(6) not null, "
                    + "FirmwareType nvarchar(10) not null, "
                    + "MinAppVersion nvarchar(50) not null, "
                    + "MaxAppVersion nvarchar(50) null, "
                    + "ChangeDate datetime not null);";

    private static final String DB_CREATE_FEATURETOGGLE =
            "create table if NOT EXISTS FeatureToggle (Key integer primary key autoincrement,"
                    + "Name varchar(100) not null, "
                    + "State boolean not null default 0);";

    private static final String SQL_SELECT_UNSUBMITTED_COMMAND = "select * from [%s] where IsSubmitted=0";

    public AbstractDBAdapter(Class<T> clazz, Context ctx) {
        super(clazz, ctx);
    }

    public AbstractDBAdapter(Class<T> clazz, Context ctx, User user) {
        super(clazz, ctx, user);
    }

    protected String getDbTableName() {
        return _databaseTableName;
    }

    protected void setDbTableName(String value) {
        _databaseTableName = value;
    }

    public static class DatabaseHelper extends SQLiteOpenHelper {
        private final Context _ctx;

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, BUILD_VERSION_CURRENT);
            _ctx = context;
            _featureToggleService = GlobalState.getInstance().getFeatureService();
        }

        IFeatureToggleService _featureToggleService = null;

        @Override
        public void onCreate(SQLiteDatabase db) {
            ErrorLogHelper.RecordMessage(_ctx, String.format("Creating database version: %s", BUILD_VERSION_CURRENT));

            db.execSQL(DB_CREATE_COMPANY);
            db.execSQL(DB_CREATE_USER);
            db.execSQL(DB_CREATE_EMPLOYEELOG);
            db.execSQL(DB_CREATE_EMPLOYEERULE);
            db.execSQL(DB_CREATE_EOBRDDEVICE);
            db.execSQL(DB_CREATE_EOBRDIAGNOSTICCOMMAND);
            db.execSQL(DB_CREATE_EVENTDATARECORD);
            db.execSQL(DB_CREATE_ENGINERECORD);
            db.execSQL(DB_CREATE_FUELPURCHASE);
            db.execSQL(DB_CREATE_LOCATIONCODE);
            db.execSQL(DB_CREATE_LOGFAILUREREPORT);
            db.execSQL(DB_CREATE_LOGTEAMDRIVER);
            db.execSQL(DB_CREATE_LOGCHECKERCOMPLIANCEDATES);
            db.execSQL(DB_CREATE_ROUTEPOSITION);
            db.execSQL(DB_CREATE_TRIPRECORD);
            db.execSQL(DB_CREATE_UNASSIGNEDDRIVINGPERIOD);
            db.execSQL(DB_CREATE_UNASSIGNEDEOBRFAILUREPERIOD);
            db.execSQL(DB_CREATE_DATAUSAGE);
            db.execSQL(DB_CREATE_VEHICLEINSPECTION);
            db.execSQL(DB_CREATE_VEHICLEINSPECTIONDEFECT);
            db.execSQL(DB_CREATE_ZSVERSION);
            db.execSQL(DB_CREATE_LOGREMARKITEM);
            db.execSQL(DB_CREATE_EMPLOYEELOGREVISION);
            db.execSQL(DB_CREATE_EMPLOYEELOGELDEVENT);
            db.execSQL(DB_CREATE_ApplicationState);
            db.execSQL(DB_CREATE_EMPLOYEELOGWITHPROVISIONS);
            db.execSQL(DB_CREATE_KMBENCOMPASSUSER);
            db.execSQL(DB_CREATE_GEOTABHOSDATA);
            db.execSQL(DB_CREATE_GEOTABEVENTRECORD);
            db.execSQL(DB_CREATE_MOTIONPICTUREAUTHORITY);
            db.execSQL(DB_CREATE_MOTIONPICTUREPRODUCTION);
            db.execSQL(DB_CREATE_DATATRANSFERMECHANISMSTATUS);
            db.execSQL(DB_CREATE_DRIVINGEVENTREASSIGNMENTMAPPING);
            db.execSQL(DB_CREATE_ELDREGISTRATIONINFO);
            db.execSQL(DB_CREATE_FEATURETOGGLE);
            db.execSQL(DB_CREATE_DATATRANSFERFILESTATUS);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            String msg = String.format("Upgrading database from version: %s to: %s", oldVersion, newVersion);
            Log.w(TAG, msg);
            ErrorLogHelper.RecordMessage(_ctx, msg);

            if (oldVersion <= BUILD_VERSION_000) {
                // no upgrade from the original system, simply rebuild and return
                this.ReactivateDatabase(db);
                return;
            }

            if (oldVersion < BUILD_VERSION_001) {
                // 2011.11.28 SJN database changes
                // create DataUsage table
                db.execSQL(DB_CREATE_DATAUSAGE);

                // create DVIR tables
                db.execSQL(DB_CREATE_VEHICLEINSPECTION);
                db.execSQL(DB_CREATE_VEHICLEINSPECTIONDEFECT);

                // change Company schema to add DVIR settings
                this.AddColumn(db, DB_TABLE_COMPANY, "AllowDriversCompleteDVIR", "BOOLEAN NOT NULL DEFAULT TRUE");
                this.AddColumn(db, DB_TABLE_COMPANY, "GeneratePreTripDVIRWithDefectAlert", "BOOLEAN NOT NULL DEFAULT TRUE");
            }

            if (oldVersion < BUILD_VERSION_002) {
                // 2012.01.04 SJN
                // change date format from "MM/dd/yyyy" to "yyyy-MM-dd"
                // change datetime format from "MM/dd/yyyy HH:mm:ss" to "yyyy-MM-dd HH:mm:ss"
                this.ChangeDateColumnFormat(db, DB_TABLE_DATAUSAGE, "UsageDate");
                this.ChangeDateColumnFormat(db, DB_TABLE_EMPLOYEELOG, "LogDate");
                this.ChangeDateTimeColumnFormat(db, DB_TABLE_EMPLOYEELOG, "MobileStartTimestamp");
                this.ChangeDateTimeColumnFormat(db, DB_TABLE_EMPLOYEELOG, "MobileEndTimestamp");
                this.ChangeDateTimeColumnFormat(db, DB_TABLE_ENGINERECORD, "EobrTimestamp");
                this.ChangeDateTimeColumnFormat(db, DB_TABLE_ENGINERECORD, "GpsTimestamp");
                this.ChangeDateTimeColumnFormat(db, DB_TABLE_EOBRDEVICE, "OdometerCalibrationDate");
                this.ChangeDateTimeColumnFormat(db, DB_TABLE_FUELPURCHASE, "PurchaseDate");
                this.ChangeDateTimeColumnFormat(db, DB_TABLE_LOGEVENT, "StartTime");
                this.ChangeDateTimeColumnFormat(db, DB_TABLE_LOGEVENT, "GPSTimestamp");
                this.ChangeDateTimeColumnFormat(db, DB_TABLE_LOGFAILUREREPORT, "StartTime");
                this.ChangeDateTimeColumnFormat(db, DB_TABLE_LOGFAILUREREPORT, "StopTime");
                this.ChangeDateTimeColumnFormat(db, DB_TABLE_LOGTEAMDRIVER, "StartTime");
                this.ChangeDateTimeColumnFormat(db, DB_TABLE_LOGTEAMDRIVER, "EndTime");
                this.ChangeDateTimeColumnFormat(db, DB_TABLE_ROUTEPOSITION, "GpsTimestamp");
                this.ChangeDateTimeColumnFormat(db, DB_TABLE_UNASSIGNEDDRIVINGPERIOD, "StartTime");
                this.ChangeDateTimeColumnFormat(db, DB_TABLE_UNASSIGNEDDRIVINGPERIOD, "StopTime");
                this.ChangeDateTimeColumnFormat(db, DB_TABLE_UNASSIGNEDDRIVINGPERIOD, "StartGPSTimestamp");
                this.ChangeDateTimeColumnFormat(db, DB_TABLE_UNASSIGNEDDRIVINGPERIOD, "StopGPSTimestamp");
                this.ChangeDateTimeColumnFormat(db, DB_TABLE_UNASSIGNEDEOBRFAILUREPERIOD, "StartTime");
                this.ChangeDateTimeColumnFormat(db, DB_TABLE_UNASSIGNEDEOBRFAILUREPERIOD, "StopTime");
                this.ChangeDateTimeColumnFormat(db, DB_TABLE_USER, "LastLoginTimestamp");
                this.ChangeDateTimeColumnFormat(db, DB_TABLE_USER, "LastLogoutTimestamp");
                this.ChangeDateTimeColumnFormat(db, DB_TABLE_USER, "LastSubmitTimestamp");
                this.ChangeDateTimeColumnFormat(db, DB_TABLE_VEHICLEINSPECTION, "InspectionTimestamp");
                this.ChangeDateTimeColumnFormat(db, DB_TABLE_VEHICLEINSPECTION, "ReviewedByDate");
                this.ChangeDateTimeColumnFormat(db, DB_TABLE_VEHICLEINSPECTION, "CertifiedByDate");
                this.ChangeDateTimeColumnFormat(db, DB_TABLE_VEHICLEINSPECTION, "SubmitTimestamp");

                this.AddColumn(db, DB_TABLE_VEHICLEINSPECTION, "OdometerReading", "NUMERIC NULL");
                this.AddColumn(db, DB_TABLE_EMPLOYEERULE, "DistanceUnits", "STRING NOT NULL DEFAULT 'M'");
            }

            if (oldVersion < BUILD_VERSION_003) {
                // Change Company schema to add new threshold values coming in from Encompass.
                this.AddColumn(db, DB_TABLE_COMPANY, "DriverStartDistance", "NUMERIC NOT NULL DEFAULT .5");
                this.AddColumn(db, DB_TABLE_COMPANY, "DriverStopMinutes", "INTEGER NOT NULL DEFAULT 5");
                this.AddColumn(db, DB_TABLE_COMPANY, "MaxAcceptableSpeed", "NUMERIC NOT NULL DEFAULT 70");
                this.AddColumn(db, DB_TABLE_COMPANY, "MaxAcceptableTach", "INTEGER NOT NULL DEFAULT 1800");
                this.AddColumn(db, DB_TABLE_COMPANY, "HardBrakeDecelerationSpeed", "NUMERIC NOT NULL DEFAULT 7");
            }

            if (oldVersion < BUILD_VERSION_004) {
                // Create the trip report table
                db.execSQL(DB_CREATE_TRIPRECORD);
                db.execSQL(DB_CREATE_EVENTDATARECORD);
            }
            if (oldVersion < BUILD_VERSION_005) {
                // Add Generation Column to EobrDevice table
                this.AddColumn(db, DB_TABLE_EOBRDEVICE, "Generation", "INTEGER NOT NULL DEFAULT 0");
            }
            if (oldVersion < BUILD_VERSION_006) {
                // If the oldVersion is will cause the table to be created, it is unnecessary to add the columns
                // because they will have been created with the DB_CREATE_TRIPRECORD creation.
                if (oldVersion > BUILD_VERSION_004) {
                    // Change Trip Record schema to add new MaxEngRPM and AvgEngRPM columns.
                    this.AddColumn(db, DB_TABLE_TRIPRECORD, "MaxEngRPM", "INTEGER NOT NULL DEFAULT 0");
                    this.AddColumn(db, DB_TABLE_TRIPRECORD, "AvgEngRPM", "INTEGER NOT NULL DEFAULT 0");
                }
            }

            if (oldVersion < BUILD_VERSION_007) {
                db.execSQL(DB_CREATE_LOGCHECKERCOMPLIANCEDATES);
                this.AddColumn(db, DB_TABLE_EMPLOYEELOG, "WeeklyResetStartTimestamp", "DATETIME NULL");
                this.AddColumn(db, DB_TABLE_EMPLOYEELOG, "IsHaulingExplosives", "BOOLEAN NOT NULL DEFAULT FALSE");
                this.AddColumn(db, DB_TABLE_EMPLOYEELOG, "IsWeeklyResetUsed", "BOOLEAN NOT NULL DEFAULT FALSE");
                this.AddColumn(db, DB_TABLE_EMPLOYEELOG, "IsWeeklyResetUsedOverridden", "BOOLEAN NOT NULL DEFAULT FALSE");
                this.AddColumn(db, DB_TABLE_EMPLOYEERULE, "IsHaulingExplosivesAllowed", "BOOLEAN NOT NULL DEFAULT FALSE");
                this.AddColumn(db, DB_TABLE_EMPLOYEERULE, "IsHaulingExplosivesDefault", "BOOLEAN NOT NULL DEFAULT FALSE");
            }

            if (oldVersion < BUILD_VERSION_008) {
                this.AddColumn(db, DB_TABLE_EVENTDATARECORD, "DashboardOdometer", "NUMERIC NOT NULL DEFAULT -1");
                this.AddColumn(db, DB_TABLE_TRIPRECORD, "DashboardOdometer", "NUMERIC NOT NULL DEFAULT -1");
                this.AddColumn(db, DB_TABLE_ROUTEPOSITION, "DashboardOdometer", "NUMERIC NOT NULL DEFAULT -1");
                this.AddColumn(db, DB_TABLE_LOGEVENT, "DashboardOdometerReading", "NUMERIC NULL DEFAULT -1");
                this.AddColumn(db, DB_TABLE_LOGEVENT, "EndDashboardOdometerReading", "NUMERIC NULL");
            }

            if (oldVersion < BUILD_VERSION_009) {
                this.AddColumn(db, DB_TABLE_LOGEVENT, "Remarks", "TEXT NULL");
                this.AddColumn(db, DB_TABLE_LOGEVENT, "RemarksDate", "datetime NULL");
                db.execSQL(DB_CREATE_LOGREMARKITEM);
            }

            if (oldVersion < BUILD_VERSION_010) {
                this.AddColumn(db, DB_TABLE_COMPANY, "UseKmbWebApiServices", "BOOLEAN NOT NULL DEFAULT FALSE");
            }

            if (oldVersion < BUILD_VERSION_011) {
                this.AddColumn(db, DB_TABLE_EMPLOYEERULE, "IsOperatesSpecificVehiclesForOilField", "BOOLEAN NOT NULL DEFAULT FALSE");
                this.AddColumn(db, DB_TABLE_EMPLOYEELOG, "IsOperatesSpecificVehiclesForOilField", "BOOLEAN NOT NULL DEFAULT FALSE");
            }

            if (oldVersion < BUILD_VERSION_012) {
                this.AddColumn(db, DB_TABLE_EVENTDATARECORD, "Speedometer", "NUMERIC NULL");
                this.AddColumn(db, DB_TABLE_EVENTDATARECORD, "Tachometer", "NUMERIC NULL");
            }

            if (oldVersion < BUILD_VERSION_013) {
                db.execSQL(DB_CREATE_EOBRDIAGNOSTICCOMMAND);
            }

            if (oldVersion < BUILD_VERSION_014) {
                this.AddColumn(db, DB_TABLE_COMPANY, "CompanyId", "uniqueidentifier NULL");
            }

            if (oldVersion < BUILD_VERSION_015) {
                // 2014.07.23 sjn - added the LastPowerCycleResetDate to the EobrDevice table
                this.AddColumn(db, DB_TABLE_EOBRDEVICE, "LastPowerCycleResetDate", "datetime NULL");
            }

            if (oldVersion < BUILD_VERSION_016) {
                this.RecreateTableFromSQL(db, DB_TABLE_EVENTDATARECORD, DB_CREATE_EVENTDATARECORD);
                this.RecreateTableFromSQL(db, DB_TABLE_ROUTEPOSITION, DB_CREATE_ROUTEPOSITION);
                this.RecreateTableFromSQL(db, DB_TABLE_TRIPRECORD, DB_CREATE_TRIPRECORD);
            }

            if (oldVersion < BUILD_VERSION_017) {
                this.AddColumn(db, DB_TABLE_LOGCHECKERCOMPLIANCEDATES, "ComplianceEndDate", "datetime NULL");
            }

            if (oldVersion < BUILD_VERSION_018) {
                this.AddColumn(db, DB_TABLE_USER, "HomeTerminalDOTNumber", "nvarchar(10) not null DEFAULT ''");
                this.AddColumn(db, DB_TABLE_USER, "HomeTerminalAddressLine1", "nvarchar(30) not null DEFAULT ''");
                this.AddColumn(db, DB_TABLE_USER, "HomeTerminalAddressLine2", "nvarchar(30) not null DEFAULT ''");
                this.AddColumn(db, DB_TABLE_USER, "HomeTerminalCity", "nvarchar(20) not null DEFAULT ''");
                this.AddColumn(db, DB_TABLE_USER, "HomeTerminalStateAbbrev", "nvarchar(2) not null DEFAULT ''");
                this.AddColumn(db, DB_TABLE_USER, "HomeTerminalZipCode", "nvarchar(10) not null DEFAULT ''");
            }


            if (oldVersion < BUILD_VERSION_019) {
                this.AddColumn(db, DB_TABLE_EMPLOYEERULE, "IsPersonalConveyanceAllowed", "BOOLEAN NOT NULL DEFAULT FALSE");
            }

            if (oldVersion < BUILD_VERSION_020) {
                db.execSQL(DB_CREATE_LOGPERSONALCONVEYANCE);
            }

            if (oldVersion < BUILD_VERSION_021) {
                this.AddColumn(db, DB_TABLE_EMPLOYEERULE, "IsMobileExemptLogAllowed", "BOOLEAN NOT NULL DEFAULT FALSE");
                this.AddColumn(db, DB_TABLE_EMPLOYEERULE, "ExemptLogType", "INTEGER NOT NULL DEFAULT 0");
            }

            if (oldVersion < BUILD_VERSION_022) {
                this.AddColumn(db, DB_TABLE_EMPLOYEELOG, "ExemptLogType", "INTEGER NOT NULL DEFAULT 0");
                this.AddColumn(db, DB_TABLE_EMPLOYEELOG, "IsNonCDLShortHaulExceptionUsed", "BOOLEAN NOT NULL DEFAULT FALSE");
            }

            if (oldVersion < BUILD_VERSION_023) {
                db.execSQL(DB_CREATE_EMPLOYEELOGREVISION);
            }

            if (oldVersion < BUILD_VERSION_024) {
                this.AddColumn(db, DB_TABLE_EMPLOYEERULE, "IsExemptFrom30MinBreakRequirement", "BOOLEAN NOT NULL DEFAULT FALSE");
            }

            if (oldVersion < BUILD_VERSION_025) {
                this.AddColumn(db, DB_TABLE_LOGEVENT, "EobrSerialNumber", "nvarchar(50) null");
            }

            if (oldVersion < BUILD_VERSION_026) {
                this.AddColumn(db, DB_TABLE_EMPLOYEELOG, "IsExemptFrom30MinBreakRequirement", "BOOLEAN NOT NULL DEFAULT FALSE");
            }

            if (oldVersion < BUILD_VERSION_027) {
                this.AddColumn(db, DB_TABLE_EMPLOYEERULE, "IsHyrailUseAllowed", "BOOLEAN NOT NULL DEFAULT FALSE");
            }
            if (oldVersion < BUILD_VERSION_028) {
                db.execSQL(DB_CREATE_LOGHYRAIL);
            }
            if (oldVersion < BUILD_VERSION_029) {
                this.AddColumn(db, DB_TABLE_EMPLOYEELOG, "TrailerPlate", "nvarchar(100) null");
                this.AddColumn(db, DB_TABLE_EMPLOYEELOG, "VehiclePlate", "nvarchar(100) null");
            }
            if (oldVersion < BUILD_VERSION_030) {
                this.AddColumn(db, DB_TABLE_COMPANY, "MultipleUsersAllowed", "BOOLEAN NOT NULL DEFAULT FALSE");
            }
            if (oldVersion < BUILD_VERSION_032) {
                this.AddColumn(db, DB_TABLE_COMPANY, "DriveStartSpeed", "INTEGER NOT NULL DEFAULT 5");
                this.AddColumn(db, DB_TABLE_COMPANY, "MandateDrivingStopTimeMinutes", "INTEGER NOT NULL DEFAULT 5");
                this.AddColumn(db, DB_TABLE_EMPLOYEERULE, "ExemptFromEldUse", "BOOLEAN NOT NULL DEFAULT FALSE");
                this.AddColumn(db, DB_TABLE_EMPLOYEERULE, "ExemptFromEldUseComment", "NVARCHAR(4000) NOT NULL DEFAULT ''");
                this.AddColumn(db, DB_TABLE_EMPLOYEERULE, "DriveStartSpeed", "INTEGER NOT NULL DEFAULT 5");
                this.AddColumn(db, DB_TABLE_EMPLOYEERULE, "MandateDrivingStopTimeMinutes", "INTEGER NOT NULL DEFAULT 5");
                this.AddColumn(db, DB_TABLE_EMPLOYEERULE, "YardMoveAllowed", "BOOLEAN NOT NULL DEFAULT FALSE");
                db.execSQL(DB_CREATE_ApplicationState);
            }

            if (oldVersion < BUILD_VERSION_035) {
                db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_ELDEVENT);
                db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_EMPLOYEELOGELDEVENT);
                db.execSQL(DB_CREATE_EMPLOYEELOGELDEVENT);
                this.RenameTable(db, DB_TABLE_LOGPERSONALCONVEYANCE, DB_TABLE_EMPLOYEELOGWITHPROVISIONS);
                this.AddColumn(db, DB_TABLE_EMPLOYEELOGWITHPROVISIONS, "ProvisionTypeEnum", "INTEGER NOT NULL DEFAULT 0");
                db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_LOGHYRAIL);
            }

            if (oldVersion < BUILD_VERSION_036) {
                this.AddColumn(db, DB_TABLE_USER, "DriverLicenseState", "nvarchar(2) not null DEFAULT ''");
                this.AddColumn(db, DB_TABLE_USER, "DriverLicenseNumber", "nvarchar(25) not null DEFAULT ''");
                this.AddColumn(db, DB_TABLE_USER, "LastName", "nvarchar(20) not null DEFAULT ''");
                this.AddColumn(db, DB_TABLE_USER, "FirstName", "nvarchar(15) not null DEFAULT ''");
            }

            if (oldVersion < BUILD_VERSION_037) {
                this.AddColumn(db, DB_TABLE_EMPLOYEELOGELDEVENT, "IsEventDateTimeValidated", "boolean not null DEFAULT false");
                this.AddColumn(db, DB_TABLE_EMPLOYEELOGELDEVENT, "EndOdometer", "numeric null");
                this.AddColumn(db, DB_TABLE_EMPLOYEELOGELDEVENT, "LogRemarkDateTime", "datetime null");
            }

            if (oldVersion < BUILD_VERSION_038) {
                db.execSQL(DB_CREATE_KMBENCOMPASSUSER);
            }

            if (oldVersion < BUILD_VERSION_040) {
                this.AddColumn(db, DB_TABLE_COMPANY, "IsGeotabEnabled", "boolean not null DEFAULT false");
                this.AddColumn(db, DB_TABLE_EMPLOYEELOG, "IsCertified", "boolean not null DEFAULT false");
            }

            if (oldVersion < BUILD_VERSION_041) {
                this.AddColumn(db, DB_TABLE_EMPLOYEELOGELDEVENT, "IsManuallyEditedByKMBUser", "boolean not null DEFAULT false");
                this.AddColumn(db, DB_TABLE_EMPLOYEELOGELDEVENT, "GpsTimestamp", "datetime null DEFAULT null");
                this.AddColumn(db, DB_TABLE_EMPLOYEELOGELDEVENT, "EditDuration", "integer null DEFAULT null");
                this.MigrateLogEventToEmployeeLogEldEvent(db);
            }

            if (oldVersion < BUILD_VERSION_042) {
                db.execSQL(DB_CREATE_GEOTABHOSDATA);
                if (!this.hasColumn(db, "EditDuration", DB_TABLE_EMPLOYEELOGELDEVENT)) {
                    this.AddColumn(db, DB_TABLE_EMPLOYEELOGELDEVENT, "EditDuration", "integer null DEFAULT null");
                }
            }

            if (oldVersion < BUILD_VERSION_043) {
                this.AddColumn(db, DB_TABLE_EMPLOYEERULE, "IsNonRegDrivingAllowed", "BOOLEAN NOT NULL DEFAULT FALSE");
            }

            if (oldVersion < BUILD_VERSION_044) {
                db.execSQL(DB_CREATE_GEOTABEVENTRECORD);
            }

            if (oldVersion < BUILD_VERSION_045) {
                // Changing columns and datatypes.  Drop & Recreate tables as no data exists yet.
                db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_GEOTABHOSDATA);
                db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_GEOTABEVENTRECORD);
                db.execSQL(DB_CREATE_GEOTABHOSDATA);
                db.execSQL(DB_CREATE_GEOTABEVENTRECORD);
            }

            if (oldVersion < BUILD_VERSION_046) {
                this.AddColumn(db, DB_TABLE_EMPLOYEELOGELDEVENT, "MotionPictureAuthorityId", "uniqueidentifier null");
                this.AddColumn(db, DB_TABLE_EMPLOYEELOGELDEVENT, "MotionPictureProductionId", "uniqueidentifier null");
            }

            if (oldVersion < BUILD_VERSION_047) {
                db.execSQL(DB_CREATE_MOTIONPICTUREAUTHORITY);
                db.execSQL(DB_CREATE_MOTIONPICTUREPRODUCTION);
            }

            if (oldVersion < BUILD_VERSION_048) {
                this.AddColumn(db, DB_TABLE_COMPANY, "IsMotionPictureEnabled", "boolean not null default false");
            }

            if (oldVersion < BUILD_VERSION_049) {
                this.AddColumn(db, DB_TABLE_EMPLOYEELOGELDEVENT, "EncompassClusterPK", "int64 not null default 0");
            }

            if (oldVersion < BUILD_VERSION_050) {
                // Express Lane issue Product Backlog Item 45679:EXPRESS LANE [KMB-Q4 Release (QB 1825)] Activation code required after update to .114 - Android
                if (!this.hasColumn(db, "EditDuration", DB_TABLE_EMPLOYEELOGELDEVENT)) {
                    this.AddColumn(db, DB_TABLE_EMPLOYEELOGELDEVENT, "EditDuration", "integer null DEFAULT null");
                }
            }

            if (oldVersion < BUILD_VERSION_051) {
                if (!this.hasColumn(db, "IsUnidentifiedEvent", DB_TABLE_EMPLOYEELOGELDEVENT)) {
                    this.AddColumn(db, DB_TABLE_EMPLOYEELOGELDEVENT, "IsUnidentifiedEvent", "boolean not null default false");
                }
            }

            if (oldVersion < BUILD_VERSION_052) {
                if (!this.hasColumn(db, "KMBUsername", DB_TABLE_LOGTEAMDRIVER)) {
                    this.AddColumn(db, DB_TABLE_LOGTEAMDRIVER, "KMBUsername", "nvarchar(60) not null DEFAULT ''");
                }
            }

            if (oldVersion < BUILD_VERSION_053) {
                if (!this.hasColumn(db, GeotabHOSDataPersist.SPEEDFROMENGINE, DB_TABLE_GEOTABHOSDATA)) {
                    this.AddColumn(db, DB_TABLE_GEOTABHOSDATA, GeotabHOSDataPersist.SPEEDFROMENGINE, "boolean not null default 0");
                }
                if (!this.hasColumn(db, GeotabHOSDataPersist.ODOMETERFROMENGINE, DB_TABLE_GEOTABHOSDATA)) {
                    this.AddColumn(db, DB_TABLE_GEOTABHOSDATA, GeotabHOSDataPersist.ODOMETERFROMENGINE, "boolean not null default 0");
                }

                if (!this.hasColumn(db, "ClockSyncOffset", DB_TABLE_EOBRDEVICE)) {
                    this.AddColumn(db, DB_TABLE_EOBRDEVICE, "ClockSyncOffset", "long null default null");
                }

                // ClockSyncDate is UTC (As all timestamps should be)
                if (!this.hasColumn(db, "ClockSyncDateUTC", DB_TABLE_EOBRDEVICE)) {
                    this.AddColumn(db, DB_TABLE_EOBRDEVICE, "ClockSyncDateUTC", "datetime null default null");
                }

                // OriginalTimestampUtc is UTC (As all timestamps should be)
                if (!this.hasColumn(db, "OriginalTimestampUtc", DB_TABLE_GEOTABHOSDATA)) {
                    this.AddColumn(db, DB_TABLE_GEOTABHOSDATA, "OriginalTimestampUtc", "datetime null default null");
                }
            }

            if (oldVersion < BUILD_VERSION_054) {
                // replaced IsUnidentifiedEvent boolean with UnidentifiedEventStatus integer
                DropColumn(db, DB_TABLE_EMPLOYEELOGELDEVENT, "IsUnidentifiedEvent");
                AddColumn(db, DB_TABLE_EMPLOYEELOGELDEVENT, "UnidentifiedEventStatus", "integer not null default 0");
            }

            if (oldVersion < BUILD_VERSION_055) {
                db.execSQL(DB_CREATE_DATATRANSFERMECHANISMSTATUS);
            }

            if (oldVersion < BUILD_VERSION_056) {
                if (!this.hasColumn(db, "IsExemptFromELDUse", DB_TABLE_EMPLOYEELOG)) {
                    this.AddColumn(db, DB_TABLE_EMPLOYEELOG, "IsExemptFromELDUse", "boolean not null default false");
                }
            }

            if (oldVersion < BUILD_VERSION_057) {
                this.AddColumn(db, DB_TABLE_EMPLOYEELOG, "EncompassId", "uniqueidentifier null");
                // If this instance of EmployeeLogEldEvent has a ReassignEvent column, begin removal process
                if (this.hasColumn(db, "ReassignEvent", DB_TABLE_EMPLOYEELOGELDEVENT)) {
                    DropColumn(db, DB_TABLE_EMPLOYEELOGELDEVENT, "ReassignEvent");
                }
                db.execSQL(DB_CREATE_DRIVINGEVENTREASSIGNMENTMAPPING);
            }

            if (oldVersion < BUILD_VERSION_059) {
                this.AddColumn(db, DB_TABLE_EMPLOYEELOGELDEVENT, "IsReviewed", "boolean not null default 0");
            }

            if (oldVersion < BUILD_VERSION_060) {
                if (!this.hasColumn(db, "EmployeeLogEldEventKey", DB_TABLE_EMPLOYEELOGWITHPROVISIONS)) {
                    this.AddColumn(db, DB_TABLE_EMPLOYEELOGWITHPROVISIONS, "EmployeeLogEldEventKey", "integer not null default 0");
                }
            }

            if (oldVersion < BUILD_VERSION_061) {
                // We should never need to check for columns, but upgrade step version 057 broke the schema
                // DBs created before 057 will have it. DBs created after 057 won't.
                if (hasColumn(db, "EncompassId", DB_TABLE_EMPLOYEELOG)) {
                    DropColumn(db, DB_TABLE_EMPLOYEELOG, "EncompassId");
                }
                AddColumn(db, DB_TABLE_UNASSIGNEDDRIVINGPERIOD, "EncompassId", "uniqueidentifier null");
            }

            if (oldVersion < BUILD_VERSION_062) {
                // Recreate table to change CompanyName column from nvarchar(30) to nvarchar(60)
                this.RecreateTableFromSQL(db, DB_TABLE_COMPANY,  DB_CREATE_COMPANY_FOR_VERSION_062);
            }

            if (oldVersion < BUILD_VERSION_063) {
                this.AddColumn(db, DB_TABLE_EMPLOYEELOGELDEVENT, Constants.RELATED_EVENT, "numeric not null default 0");
            }

            if (oldVersion < BUILD_VERSION_064) {
                this.AddColumn(db, DB_TABLE_EOBRDEVICE, "MajorFirmwareVersion", "integer not null default 0");
                this.AddColumn(db, DB_TABLE_EOBRDEVICE, "MinorFirmwareVersion", "integer not null default 0");
                this.AddColumn(db, DB_TABLE_EOBRDEVICE, "PatchFirmwareVersion", "integer not null default 0");
            }


            if (oldVersion < BUILD_VERSION_065) {
                if (hasColumn(db, "EndMoveDateTime", DB_TABLE_EMPLOYEELOGELDEVENT)) {
                    DropColumn(db, DB_TABLE_EMPLOYEELOGELDEVENT, "EndMoveDateTime");
                }
            }

            if (oldVersion < BUILD_VERSION_066) {
                this.AddColumn(db, DB_TABLE_LOGTEAMDRIVER, "TimeZone", "integer not null default 0");
            }

            if (oldVersion < BUILD_VERSION_067) {
                if (hasColumn(db, "ClusterPK", DB_TABLE_DRIVINGEVENTREASSIGNMENTMAPPING)) {
                    DropColumn(db, DB_TABLE_DRIVINGEVENTREASSIGNMENTMAPPING, "ClusterPK");
                }
                AddColumn(db, DB_TABLE_DRIVINGEVENTREASSIGNMENTMAPPING, "RelatedEvent", "integer not null default 0");
                AddColumn(db, DB_TABLE_DRIVINGEVENTREASSIGNMENTMAPPING, "IsSubmitted", "boolean not null default 0");
            }

            if (oldVersion < BUILD_VERSION_068) {
                db.execSQL(DB_CREATE_ELDREGISTRATIONINFO);
            }

            if (oldVersion < BUILD_VERSION_069) {
                db.execSQL(DB_CREATE_FEATURETOGGLE);
            }

            if (oldVersion < BUILD_VERSION_070) {
                this.RenameColumn(db, DB_TABLE_GEOTABHOSDATA, "TripEngineHours", "TripEngineSeconds");
            }

            if (oldVersion < BUILD_VERSION_071) {
                this.AddColumn(db, DB_TABLE_GEOTABHOSDATA, "GpsUncertDistance", "numeric not null default 0");
                this.AddColumn(db, DB_TABLE_GEOTABHOSDATA, "OrigOdometer", "numeric not null default 0");
            }

            if (oldVersion < BUILD_VERSION_072) {
                this.AddColumn(db, DB_TABLE_EOBRDEVICE, "VIN", "nvarchar(20) null");
            }

            if (oldVersion < BUILD_VERSION_073) {
                if (hasColumn(db, "EldIdentifier", DB_TABLE_EMPLOYEELOGELDEVENT)) {
                    DropColumn(db, DB_TABLE_EMPLOYEELOGELDEVENT, "EldIdentifier");
                }
            }

            if (oldVersion < BUILD_VERSION_074) {
                this.AddColumn(db, DB_TABLE_EOBRDEVICE, "LastEventReferenceTimestamp", "datetime null");
            }

            if (oldVersion < BUILD_VERSION_075) {
                db.execSQL(DB_CREATE_DATATRANSFERFILESTATUS);
            }

            if(oldVersion < BUILD_VERSION_076){

                this.AddColumn(db, DB_TABLE_EMPLOYEELOGELDEVENT, "UnidentifiedEventConfidenceLevel", "integer null");
                this.AddColumn(db, DB_TABLE_EMPLOYEELOGELDEVENT, "UnidentifiedEventSuggestedDriver", "uniqueidentifier null");
            }

            if (oldVersion < BUILD_VERSION_077) {
                this.AddColumn(db, DB_TABLE_COMPANY, "IsAutoAssignUnIdentifiedEvents", "boolean not null default false");
            }

            if (oldVersion < BUILD_VERSION_078) {

                // Placeholder for future release
            }
        }

        private void ReactivateDatabase(SQLiteDatabase db) {
            String msg = "Drop and rebuild complete database (requires re-activation).";
            Log.w(TAG, msg);
            ErrorLogHelper.RecordMessage(_ctx, msg);

            db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_COMPANY);
            db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_USER);
            db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_EMPLOYEELOG);
            db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_EMPLOYEERULE);
            db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_EOBRDEVICE);
            db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_EOBRDIAGNOSTICCOMMAND);
            db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_ENGINERECORD);
            db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_EVENTDATARECORD);
            db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_FUELPURCHASE);
            db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_LOCATIONCODE);
            db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_LOGFAILUREREPORT);
            db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_LOGTEAMDRIVER);
            db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_LOGCHECKERCOMPLIANCEDATES);
            db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_ROUTEPOSITION);
            db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_TRIPRECORD);
            db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_UNASSIGNEDDRIVINGPERIOD);
            db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_UNASSIGNEDEOBRFAILUREPERIOD);
            db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_DATAUSAGE);
            db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_VEHICLEINSPECTION);
            db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_VEHICLEINSPECTIONDEFECT);
            db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_ZSVERSION);
            db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_LOGREMARKITEM);
            db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_EMPLOYEELOGELDEVENT);
            db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_EMPLOYEELOGREVISION);
            db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_EMPLOYEELOGWITHPROVISIONS);
            db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_KMBENCOMPASSUSER);
            db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_GEOTABHOSDATA);
            db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_GEOTABEVENTRECORD);
            db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_MOTIONPICTUREAUTHORITY);
            db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_MOTIONPICTUREPRODUCTION);
            db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_DATATRANSFERMECHANISMSTATUS);
            db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_DRIVINGEVENTREASSIGNMENTMAPPING);
            db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_ELDREGISTRATIONINFO);
            db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_DATATRANSFERFILESTATUS);
            onCreate(db);
        }

        private void AddColumn(SQLiteDatabase db, String tableName, String columnName, String columnDefinition) {
            if (!hasColumn(db, columnName, tableName)) {
                String sql = String.format("ALTER TABLE %s ADD COLUMN %s %s", tableName, columnName, columnDefinition);
                try {
                    db.execSQL(sql);
                } catch (SQLException e) {
                    ErrorLogHelper.RecordException(_ctx, e);
                    e.printStackTrace();
                }
            }
        }

        private boolean hasColumn(SQLiteDatabase db, String name, String tableName) {
            ArrayList<String> tableColumns = GetTableColumns(db, tableName);
            for (String tableCol : tableColumns) {
                if (tableCol.equalsIgnoreCase(name)) {
                    return true;
                }
            }
            return false;
        }

        private void DropColumn(SQLiteDatabase db, String tableName, String columnToDrop) {
            Log.d(TAG, "Dropping column " + columnToDrop);
            String newTableCreateStatement = GetCreateTableStatementWithoutColumn(db, tableName, columnToDrop);
            Log.d(TAG, "Recreating table with SQL to drop the column:");
            Log.d(TAG, newTableCreateStatement);
            RecreateTableFromSQL(db, tableName, newTableCreateStatement);
        }

        private String GetCreateTableStatementWithoutColumn(SQLiteDatabase db, String tableName, String columnToDrop) {
            Cursor cursor = db.rawQuery("select sql from sqlite_master where name = ?", new String[]{tableName});
            try {
                cursor.moveToNext();
                String originalCreateStatement = cursor.getString(0);
                Log.d(TAG, "Got original SQL create statement from sqlite_master:");
                Log.d(TAG, originalCreateStatement);
                return removeColumnFromCreateStatement(originalCreateStatement, columnToDrop);
            } finally {
                cursor.close();
            }
        }

        /**
         * Note:  	There isn't a DROP COLUMN alter statement that can be executed against
         * a SQLite database.  Therefore the following is done to drop a column:
         * - 1) create new table w/o dropped column(s)
         * - 2) copy data from existing table to new table excluding dropped columns
         * - 3) drop existing table
         * - 4) rename new table to existing table
         */
        private void RecreateTableFromSQL(SQLiteDatabase db, String existingTableName, String createTableSQL) {
            String newTableName = existingTableName + "_new";
            boolean success = CreateNewTable(db, newTableName, createTableSQL.replaceFirst(existingTableName, newTableName));

            if (success) {
                ArrayList<String> tableColumns = GetTableColumns(db, newTableName);
                success = CopyDataToNewTable(db, existingTableName, newTableName, tableColumns);
            }

            if (success) {
                success = DropExistingTable(db, existingTableName);
            }

            if (success) {
                RenameTable(db, newTableName, existingTableName);
            }
        }

        private ArrayList<String> GetTableColumns(SQLiteDatabase db, String tableName) {
            ArrayList<String> tableColList = new ArrayList<String>();
            Cursor cursor = db.rawQuery("Pragma table_info(" + tableName + ")", null);

            try {
                int nameIdx = cursor.getColumnIndex("name");

                // if valid column index is found, populate table column list
                if (nameIdx > -1) {
                    while (cursor.moveToNext()) {
                        tableColList.add(cursor.getString(nameIdx));
                    }
                }
            } finally {
                cursor.close();
            }

            return tableColList;
        }

        private boolean CreateNewTable(SQLiteDatabase db, String newTableName, String createNewTableSQL) {
            boolean success = true;
            try {
                db.execSQL(createNewTableSQL);
            } catch (Exception e) {
                ErrorLogHelper.RecordException(_ctx, e, "Create new table, " + newTableName + ", failed.");
                success = false;
            }

            return success;
        }

        private boolean CopyDataToNewTable(SQLiteDatabase db, String existingTableName, String newTableName, ArrayList<String> tableColList) {
            boolean success = true;
            StringBuilder sb = new StringBuilder();
            sb.append("INSERT INTO ").append(newTableName);
            sb.append(" SELECT ");

            for (String tableCol : tableColList) {
                sb.append(tableCol).append(", ");
            }

            // remove last comma
            sb.delete(sb.length() - 2, sb.length() - 1);
            sb.append(" FROM ").append(existingTableName);

            try {
                db.execSQL(sb.toString());
            } catch (Exception e) {
                ErrorLogHelper.RecordException(_ctx, e, "Copy records to new table, " + newTableName + ", failed.");
                success = false;
            }

            return success;
        }

        private boolean DropExistingTable(SQLiteDatabase db, String tableName) {
            boolean success = true;

            try {
                db.execSQL("DROP TABLE " + tableName);
            } catch (Exception e) {
                ErrorLogHelper.RecordException(_ctx, e, "Drop existing table, " + tableName + ", failed.");
                success = false;
            }

            return success;
        }

        private void RenameTable(SQLiteDatabase db, String existingTableName, String newTableName) {
            try {
                db.execSQL("ALTER TABLE " + existingTableName + " RENAME TO " + newTableName);
            } catch (Exception e) {
                ErrorLogHelper.RecordException(_ctx, e, "Rename new table, " + existingTableName + " to " + newTableName + " failed.");
            }
        }

        /**
         * Note:  	There isn't a RENAME COLUMN alter statement that can be executed against
         * a SQLite database.  Therefore the following is done to rename one column at the time:
         * - 1) Check if the existingColumnName exist
         * - 2) Get create statement and replace the existingColumnName for the newColumnName
         * - 2) Rename the tableName to tableName_original
         * - 3) Create new table tableName with the correct column names
         * - 4) Copy data from tableName_original to new tableName by column names
         * - 5) Delete the tableName_original
         * @param db
         * @param tableName
         * @param existingColumnName
         * @param newColumnName
         */
        private void RenameColumn(SQLiteDatabase db, String tableName, String existingColumnName, String newColumnName) {
            try {
                if(this.hasColumn(db,existingColumnName,tableName)){

                    //Get create statement and replace the existingColumnName for the newColumnName
                    String createNewTableSQL = getCreateStatementFromTable(db, tableName);
                    createNewTableSQL = createNewTableSQL.replaceFirst(existingColumnName, newColumnName);

                    //Rename the tableName
                    String temporalTableName =  tableName.concat("_original");
                    this.RenameTable(db, tableName, temporalTableName);

                    //Create new table tableName with the correct column names
                    this.CreateNewTable(db, tableName, createNewTableSQL);

                    //Copy data from tableName_original to new tableName by column names
                    ArrayList<String> tableColumns = GetTableColumns(db,  temporalTableName);
                    ArrayList<String> newTableColumns = GetTableColumns(db, tableName);
                    StringBuilder sb = new StringBuilder();
                    sb.append("INSERT INTO ").append(tableName);
                    sb.append("(");
                    for (String tableCol: newTableColumns) {
                        sb.append(tableCol).append(", ");
                    }
                    // remove last comma
                    sb.delete(sb.length() - 2, sb.length() - 1);
                    sb.append(")");
                    sb.append(" SELECT ");
                    for (String tableCol : tableColumns) {
                        sb.append(tableCol).append(", ");
                    }
                    // remove last comma
                    sb.delete(sb.length() - 2, sb.length() - 1);
                    sb.append(" FROM ").append(temporalTableName);
                    db.execSQL(sb.toString());

                    //Delete the tableName_original
                    this.DropExistingTable(db, tableName.concat("_original"));
                }
            } catch (Exception e) {
                ErrorLogHelper.RecordException(_ctx, e, "Rename column, " + existingColumnName + " to " + newColumnName + " from table "+ tableName + " failed.");
            }
        }

        private String getCreateStatementFromTable(SQLiteDatabase db, String tableName){
            //Get the create statement of the current tableName from sqlite_master
            Cursor c = db.rawQuery("SELECT sql FROM sqlite_master WHERE type='table' AND name ='"+ tableName + "'", null);
            c.moveToFirst();
            return c.getString(0);
        }

        private void ChangeDateColumnFormat(SQLiteDatabase db, String tableName, String columnName) {
            //                  1234567890
            // old date format: MM/dd/yyyy
            // new date format: yyyy-MM-dd

            // from old to new
            String mm = String.format("SUBSTR( %s, 1, 2 )", columnName);
            String dd = String.format("SUBSTR( %s, 4, 2 )", columnName);
            String yyyy = String.format("SUBSTR( %s, 7, 4 )", columnName);
            String newDateFormat = String.format("%s||'-'||%s||'-'||%s", yyyy, mm, dd);

            // from new to old
            //String mm = String.format("SUBSTR( %s, 6, 2 )", columnName);
            //String dd = String.format("SUBSTR( %s, 9, 2 )", columnName);
            //String yyyy = String.format("SUBSTR( %s, 1, 4 )", columnName);
            //String newDateFormat = String.format("%s||'/'||%s||'/'||%s", mm, dd, yyyy);

            String sql = String.format("UPDATE [%s] SET %s = %s WHERE %s IS NOT NULL AND LENGTH(%s)>0", tableName, columnName, newDateFormat, columnName, columnName);
            try {
                db.execSQL(sql);
            } catch (SQLException e) {
                ErrorLogHelper.RecordException(_ctx, e);
                e.printStackTrace();
            }
        }

        private void ChangeDateTimeColumnFormat(SQLiteDatabase db, String tableName, String columnName) {
            //                  1234567890123456789
            // old date format: MM/dd/yyyy HH:mm:ss
            // new date format: yyyy-MM-dd HH:mm:ss

            // from old to new
            String mm = String.format("SUBSTR( %s, 1, 2 )", columnName);
            String dd = String.format("SUBSTR( %s, 4, 2 )", columnName);
            String yyyy = String.format("SUBSTR( %s, 7, 4 )", columnName);
            String time = String.format("SUBSTR( %s, 12)", columnName);
            String newDateFormat = String.format("%s||'-'||%s||'-'||%s||' '||%s", yyyy, mm, dd, time);

            // from new to old
            //String mm = String.format("SUBSTR( %s, 6, 2 )", columnName);
            //String dd = String.format("SUBSTR( %s, 9, 2 )", columnName);
            //String yyyy = String.format("SUBSTR( %s, 1, 4 )", columnName);
            //String time = String.format("SUBSTR( %s, 12)", columnName);
            //String newDateFormat = String.format("%s||'/'||%s||'/'||%s||' '||%s", mm, dd, yyyy, time);

            String sql = String.format("UPDATE [%s] SET %s = %s WHERE %s IS NOT NULL AND LENGTH(%s)>0", tableName, columnName, newDateFormat, columnName, columnName);
            try {
                db.execSQL(sql);
            } catch (SQLException e) {
                ErrorLogHelper.RecordException(_ctx, e);
                e.printStackTrace();
            }
        }

        private void MigrateLogEventToEmployeeLogEldEvent(SQLiteDatabase db) {
            if (CheckIfLogEventTableExists(db)) {
                String sql = "insert into EmployeeLogEldEvent (DriverOriginatorUserId, EobrSerialNumber, LogKey, EventSequenceIdNumber, EventType, EventCode, EventRecordOrigin, EventRecordStatus, EventDateTime, RuleSet, Odometer, EndOdometer, Latitude, Longitude, " +
                        "TractorNumber, VehiclePlate, TrailerNumber, TrailerPlate, ShipmentInfo, LogRemark, LogRemarkDateTime, EldIdentifier, IsEventDateTimeValidated, OriginalEvent, DriversLocationDescription, Geolocation, GpsTimestamp, EditDuration) " +
                        "select u.EmployeeId, le.EobrSerialNumber, le.EmployeeLogKey, " + EmployeeLogEldEnum.AOBRD + " , 1,le.DutyStatusEnum, 1, 1, le.StartTime, le.RulesetType, le.OdometerReading, le.EndOdometerReading, le.LatitudeDegrees, le.LongitudeDegrees, " +
                        "el.TractorNumbers, el.VehiclePlate, el.TrailerNumbers, el.TrailerPlate, el.ShipmentInfo, le.Remarks, le.RemarksDate, '" + EmployeeLogEldIdentifierEnum.AOBRD.toString() + "', le.IsStartTimeValidated, 0, le.LocationName, le.GPSDecodedLocation, le.GPSTimestamp, null from LogEvent as le " +
                        "join EmployeeLog as el on le.EmployeeLogKey = el.Key " +
                        "join User as u on el.UserKey = u.Key;";

                try {
                    db.execSQL(sql);

                    db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_LOGEVENT);
                } catch (SQLException e) {
                    ErrorLogHelper.RecordException(_ctx, e);
                    e.printStackTrace();
                }
            }
        }

        private boolean CheckIfLogEventTableExists(SQLiteDatabase db) {
            Boolean tableExists = false;

            String sql = "SELECT count(*) checkLogEvent FROM sqlite_master WHERE type='table' AND name='LogEvent'";
            int LogEventInt = 0;
            Cursor cursor = db.rawQuery(sql, null);

            try {
                int nameIdx = cursor.getColumnIndex("checkLogEvent");

                // if valid column index is found, populate table column list
                if (nameIdx > -1) {

                    while (cursor.moveToFirst()) {
                        LogEventInt = cursor.getInt(nameIdx);
                        break;
                    }
                }
            } finally {
                cursor.close();
            }
            if (LogEventInt != 0) {
                tableExists = true;
            }

            return tableExists;
        }
    }

    static String removeColumnFromCreateStatement(String originalCreateStatement, String columnToRemove) throws IllegalArgumentException {
        int columnIndex = originalCreateStatement.indexOf(columnToRemove);
        if (columnIndex < 0) {
            throw new IllegalArgumentException(String.format("The table doesn't have a column named %s to remove", columnToRemove));
        }
        int commaBeforeIndex = originalCreateStatement.substring(0, columnIndex).lastIndexOf(",");
        int commaAfterIndex = originalCreateStatement.indexOf(",", columnIndex);
        if (commaBeforeIndex < 0 && commaAfterIndex > 0) {
            return originalCreateStatement.substring(0, columnIndex) + originalCreateStatement.substring(commaAfterIndex + 1);
        } else if (commaBeforeIndex > 0) {
            int afterColumnIndex = (commaAfterIndex > 0) ? commaAfterIndex : originalCreateStatement.indexOf(")", columnIndex);
            return originalCreateStatement.substring(0, commaBeforeIndex) + originalCreateStatement.substring(afterColumnIndex);
        } else {
            throw new IllegalArgumentException(String.format("Cannot remove the last column (%s) from the table", columnToRemove));
        }
    }

    public static DatabaseHelper getHelperForTesting(Context context) {
        return new DatabaseHelper(context);
    }

    private static synchronized DatabaseHelper getHelper(Context context) {
        if (_dbHelper == null)
            _dbHelper = new DatabaseHelper(context.getApplicationContext());
        return _dbHelper;
    }

    //---opens the database---
    public AbstractDBAdapter<T> open() throws SQLException {
        _database = getHelper(getContext()).getWritableDatabase();
        return this;
    }

    //TODO find better way to do this for testing... Jesse
    public void closeDatabase() {
        _database.close();
    }

    //---closes the database---
    public void close() {
        // Leave the database open to prevent locking issues
    }

    public abstract String getSelectPrimaryKeyCommand();

    protected abstract String[] getSelectPrimaryKeyArgs(T data);

    protected String[] getPrimaryKeyArgs(T data) {
        return new String[]{Long.toString(data.getPrimaryKey())};
    }

    @Override
    protected String getSelectCommand() {
        return null;
    }

    protected String[] getSelectArgs() {
        return null;
    }

    protected String getSelectUnsubmittedCommand() {
        return SQL_SELECT_UNSUBMITTED_COMMAND;
    }

    protected String[] getSelectUnsubmittedArgs() {
        return null;
    }

    public Long PrimaryKeyExists(String[] args) {
        Long retVal = null;

        this.open();
        Cursor cursor = _database.rawQuery(this.getSelectPrimaryKeyCommand(), args);
        if (cursor != null && cursor.moveToFirst()) {
            retVal = cursor.getLong(0);
        }

        cursor.close();
        this.close();
        return retVal;
    }

    protected Boolean isValidInsert(T data) { return true; }

    protected Boolean isValidUpdate(T data) { return true; }

    @Override
    public void Persist(T data) {
        ContentValues content = PersistContentValues(data);

        if (!data.isPrimaryKeySet()) {
            Long primaryKey = this.PrimaryKeyExists(getSelectPrimaryKeyArgs(data));

            if (primaryKey != null) {
                data.setPrimaryKey(primaryKey);
            }
        }

        if (!data.isPrimaryKeySet()) {
            if (isValidInsert(data)){
                ExecuteInsert(data, content, String.format("Unable to insert %s data", this.getProxyDataClassName()));
            }
            else {
                String msg = "Failed isValidInsert check for data: %s \r\nStackTrace: %s";
                ErrorLogHelper.RecordMessage(String.format(msg, data.toString(), Log.getStackTraceString(new Exception("DEBUG"))));
                return;
            }
        } else {
            if (isValidUpdate(data)){
                ExecuteUpdate(data, content, String.format("Can't save %s without PK.", this.getProxyDataClassName()));
            }
            else {
                String msg = "Failed isValidUpdate check for data: %s \r\nStackTrace: %s";
                ErrorLogHelper.RecordMessage(String.format(msg, data.toString(), Log.getStackTraceString(new Exception("DEBUG"))));
                return;
            }
        }

        SaveRelatedData(data);
    }

    public void Persist(T[] dataList) {
        ArrayList<T> inboundList = new ArrayList<T>();
        for (T obj : dataList) inboundList.add(obj);

        Persist(inboundList);
    }

    public void Persist(List<T> inboundList) {
        if (inboundList == null || inboundList.size() == 0)
            return;

        int maxSize = -1;

        // Create a new collection which will be the list to perform
        // the saves on.  Will combine inbound list with DB list
        // (Primary keys reuse mainly from DB list).
        List<T> saveList = new ArrayList<T>();

        // Get the collection from the database
        List<T> dbRecordList = this.FetchList();

        // Identify the larger collection (inbound or DB)
        if (inboundList.size() >= dbRecordList.size())
            maxSize = inboundList.size();
        else
            maxSize = dbRecordList.size();

        // Loop over the records of the collections
        for (int indexCurrent = 0; indexCurrent < maxSize; indexCurrent++) {
            // If there are more records inbound to save than already
            // exist in the DB, add to the collection
            if (indexCurrent >= dbRecordList.size()) {
                T theItem = inboundList.get(indexCurrent);
                // need to ensure that this treated as an insert, not an update
                theItem.setPrimaryKey(-1);
                saveList.add(theItem);
            } else if (indexCurrent >= inboundList.size()) {
                // Remove extra DB records that no longer exist
                this.Delete(dbRecordList.get(indexCurrent));
            } else {
                // Update an existing record, reusing primary keys from the DB
                T theItem = inboundList.get(indexCurrent);
                T dbItem = dbRecordList.get(indexCurrent);

                theItem.setPrimaryKey(dbItem.getPrimaryKey());
                saveList.add(theItem);
            }
        }

        // Save the updated collection
        for (T obj : saveList) this.Persist(obj);
    }

    /**
     * Wraps multiple database updates in a single transaction so they either all succeed or all fail together.
     */
    public void PersistListInSingleTransaction(T[] inboundList) {
        this.open();

        _database.beginTransaction();

        try {
            for (T obj : inboundList) this.Persist(obj);
            _database.setTransactionSuccessful();
        } finally {
            _database.endTransaction();
            this.close();
        }
    }

    public List<T> FetchList() {
        List<T> list = null;
        try {
            list = ExecuteFetchListRawQuery(getSelectCommand(), getSelectArgs());
        } catch (Throwable e) {
            ErrorLogHelper.RecordException(this.getContext(), e);
            e.printStackTrace();
        }

        return list;
    }

    public T Fetch() {
        T obj = null;
        try {
            obj = ExecuteFetchRawQuery(getSelectCommand(), getSelectArgs());
        } catch (Throwable e) {
            ErrorLogHelper.RecordException(this.getContext(), e);
            e.printStackTrace();
        }

        return obj;
    }

    /// <summary>
    /// Fetch a list of all the unsubmitted items.
    /// </summary>
    /// <returns></returns>
    public List<T> FetchAllUnsubmitted() {
        List<T> list = ExecuteFetchListRawQuery(String.format(getSelectUnsubmittedCommand(), getDbTableName()), getSelectUnsubmittedArgs());

        return list;
    }

    public void Delete(T data) {
        this.open();
        _database.delete(getDbTableName(), "KEY=?", getPrimaryKeyArgs(data));
        this.close();
    }

    public void MarkAsSubmitted(List<T> list) {
        for (T pos : list) {
            this.MarkAsSubmitted(pos);
        }
    }

    protected void MarkAsSubmitted(T obj) {
        ContentValues content = new ContentValues();
        PutValue(content, this.ISSUBMITTED, 1);
        String errorMsg = "Can't mark as submitted without PK.";

        ExecuteUpdate(obj, content, errorMsg);
    }

    @Override
    protected ContentValues PersistContentValues(T data) {
        ContentValues content = new ContentValues();
        return content;
    }

    protected void ExecuteInsert(T data, ContentValues content, String errorMsg) {
        this.open();

        try {
            long key = _database.insert(getDbTableName(), null, content);

            if (key >= 0)
                data.setPrimaryKey(key);
        } catch (SQLException sqlEx) {
            ErrorLogHelper.RecordException(this.getContext(), sqlEx);
            sqlEx.printStackTrace();
        }

        this.close();
    }

    protected void ExecuteUpdate(T data, ContentValues content, String errorMsg) {
        this.open();
        try {
            if (data.isPrimaryKeySet()) {
                _database.update(getDbTableName(), content, "Key=?", getPrimaryKeyArgs(data));
            } else {
                throw new Exception(errorMsg);
            }
        } catch (SQLException sqlExcp) {
            ErrorLogHelper.RecordException(this.getContext(), sqlExcp);
            sqlExcp.printStackTrace();
        } catch (Throwable excp) {
            ErrorLogHelper.RecordException(this.getContext(), excp);
        }

        this.close();
    }

    protected void ExecuteQuery(String sql, String[] selectionArgs) {
        this.open();
        try {
            _database.execSQL(sql, selectionArgs);
        } catch (SQLException sqlExcp) {
            ErrorLogHelper.RecordException(this.getContext(), sqlExcp);
            sqlExcp.printStackTrace();
        } catch (Throwable excp) {
            ErrorLogHelper.RecordException(this.getContext(), excp);
            excp.printStackTrace();
        }
        this.close();
    }

    protected List<T> ExecuteFetchListQuery(String[] columns, String selection, String[] selectionArgs,
                                            String groupBy, String having, String orderBy) {
        this.open();
        Cursor cursorData = null;

        try {
            cursorData = _database.query(getDbTableName(), columns, selection, selectionArgs,
                    groupBy, having, orderBy);
        } catch (IllegalArgumentException e) {
            cursorData = null;
        }

        List<T> dataList = this.CreateListFromCursor(cursorData);

        if (cursorData != null)
            cursorData.close();

        this.close();
        return dataList;
    }

    protected List<T> ExecuteFetchListRawQuery(String sql, String[] selectionArgs) {
        this.open();
        Cursor cursorData = null;

        try {
            cursorData = _database.rawQuery(sql, selectionArgs);
        } catch (IllegalArgumentException e) {
            cursorData = null;
        }

        List<T> dataList = this.CreateListFromCursor(cursorData);

        if (cursorData != null)
            cursorData.close();

        this.close();
        return dataList;
    }

    protected T ExecuteFetchQuery(String[] columns, String selection, String[] selectionArgs,
                                  String groupBy, String having, String orderBy) {
        this.open();

        Cursor cursorData = null;

        try {
            cursorData = _database.query(getDbTableName(), columns, selection, selectionArgs,
                    groupBy, having, orderBy);
        } catch (IllegalArgumentException e) {
            cursorData = null;
        }

        T obj = this.CreateFromCursor(cursorData);

        if (cursorData != null)
            cursorData.close();

        this.close();
        return obj;
    }

    protected T ExecuteFetchRawQuery(String sql, String[] selectionArgs) {
        this.open();
        Cursor cursorData = null;
        try {
            cursorData = _database.rawQuery(sql, selectionArgs);
        } catch (IllegalArgumentException ex) {
            cursorData = null;
        }

        T data = this.CreateFromCursor(cursorData);

        if (cursorData != null)
            cursorData.close();

        this.close();
        return data;
    }

    protected int GetScalar(String sql, String[] selectionArgs) {
        int result = -1;

        this.open();

        Cursor cursor = null;
        try {
            cursor = _database.rawQuery(sql, selectionArgs);
        } catch (IllegalArgumentException ex) {
            cursor = null;
        }

        if (cursor != null && cursor.moveToFirst()) {
            result = cursor.getInt(0);
        }

        if (cursor != null)
            cursor.close();

        this.close();
        return result;
    }

    protected Cursor ExecuteRawQuery(String sql, String[] selectionArgs) {
        return _database.rawQuery(sql, selectionArgs);
    }
}
