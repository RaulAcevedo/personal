package com.jjkeller.kmbapi.controller.dataaccess.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.jjkeller.kmbapi.controller.dataaccess.AbstractDBAdapter;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.proxydata.GeotabHOSData;

import org.joda.time.DateTime;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by jhm2586 on 9/1/2016.
 */
public class GeotabHOSDataPersist <T extends GeotabHOSData>extends AbstractDBAdapter<T> {
    ///////////////////////////////////////////////////////////////////////////////////////
    // private members
    ///////////////////////////////////////////////////////////////////////////////////////
    private static final String DRIVERID = "DriverId";
    private static final String TIMESTAMPUTC = "TimestampUtc";
    private static final String GPSLATITUDE = "GpsLatitude";
    private static final String GPSLONGITUDE = "GpsLongitude";
    private static final String SPEEDOMETER = "Speedometer";
    private static final String TACHOMETER = "Tachometer";
    private static final String ODOMETER = "Odometer";
    private static final String ORIGODOMETER = "OrigOdometer";
    private static final String TRIPODOMETER = "TripOdometer";
    private static final String ENGINEHOURS = "EngineHours";
    private static final String TRIPENGINESECONDS = "TripEngineSeconds";
    private static final String GPSVALID = "GpsValid";
    private static final String GPSUNCERT = "GpsUncertDistance";
    private static final String IGNITIONON = "IgnitionOn";
    private static final String ENGINEACTIVITYDETECTED = "EngineActivityDetected";
    private static final String DATETIMEVALID = "DateTimeValid";
    public static final String SPEEDFROMENGINE = "SpeedFromEngine";
    public static final String ODOMETERFROMENGINE = "OdometerFromEngine";
    private static final String VEHICLEID = "VehicleId";
    private static final String EVENTDATARECORDKEY = "EventDataRecordKey";
    private static final String ISSUBMITTED = "IsSubmitted";
    private static final String ORIGINALTIMESTAMPUTC = "OriginalTimestampUTC";


    private final String SQL_SELECT_PRIMARYKEY_COMMAND = "select [Key] from [GeotabHOSData] where VehicleId=? and TimestampUtc=?";
    private final String SQL_PURGE_SUBMITTED_COMMAND = "delete from [GeotabHOSData] where TimestampUtc < ? AND IsSubmitted=1";
    private final String SQL_PURGE_COMMAND = "delete from [GeotabHOSData] where TimestampUtc < ? ";
    private static final String SQL_SELECT_MOSTRECENT_COMMAND = "select * from [GeotabHOSData] where VehicleId=? order by TimestampUtc desc limit 1";
    private static final String SQL_SELECT_UNSUBMITTED_LIMITED_COMMAND = "select * from [GeotabHOSData] where IsSubmitted=0 limit ?";
    private static final String SQL_SELECT_GETBYTIMESTAMP_COMMAND = "select * from [GeotabHOSData] where VehicleId=? and TimestampUtc>=? order by TimestampUtc asc limit 1";
    private static final String SQL_SELECT_GETBYVEHICLEANDKEY_COMMAND = "select * from [GeotabHOSData] where VehicleId=? and Key=?";
    private static final String SQL_SELECT_GETLATESTBYVEHICLE_COMMAND = "select * from [GeotabHOSData] where VehicleId=? order by TimestampUtc desc limit 1";
    private static final String SQL_PURGE_HOSDATATABLE_COMMAND = "delete from GeotabHOSData";

    ///////////////////////////////////////////////////////////////////////////////////////
    // constructors
    ///////////////////////////////////////////////////////////////////////////////////////
    public GeotabHOSDataPersist(Class<T> clazz, Context ctx)
    {
        super(clazz, ctx);

        setDbTableName(DB_TABLE_GEOTABHOSDATA);
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // @Override methods
    ///////////////////////////////////////////////////////////////////////////////////////
    @Override
    public String getSelectPrimaryKeyCommand()
    {
        return SQL_SELECT_PRIMARYKEY_COMMAND;
    }

    @Override
    protected String[] getSelectPrimaryKeyArgs(T data) {
        String [] args;
        SimpleDateFormat sqlDateTimeFormat = DateUtility.getSqlDateTimeFormatMS();
        sqlDateTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        if(data.getVehicleId() == null && data.getTimestampUtc() == null)
            args = new String[]{"", ""};
        else if (data.getTimestampUtc() == null)
            args = new String[]{"", sqlDateTimeFormat.format(data.getTimestampUtc().toDate())};
        else if(data.getTimestampUtc() == null)
            args = new String[]{data.getVehicleId(), ""};
        else
        {
            args = new String[]{data.getVehicleId(), sqlDateTimeFormat.format(data.getTimestampUtc().toDate())};
        }

        return args;
    }

    @Override
    protected T BuildObject(Cursor cursorData)
    {
        T data = super.BuildObject(cursorData);

        SimpleDateFormat sqlDateTimeFormat = DateUtility.getSqlDateTimeFormatMS();
        sqlDateTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        data.setDriverId(ReadValue(cursorData, DRIVERID, (int)0));
        data.setTimestampUtc(ReadValue(cursorData, TIMESTAMPUTC, (DateTime)null, sqlDateTimeFormat));
        data.setGpsLatitude(ReadValue(cursorData, GPSLATITUDE, (float)0));
        data.setGpsLongitude(ReadValue(cursorData, GPSLONGITUDE, (float)0));
        data.setSpeedometer(ReadValue(cursorData, SPEEDOMETER, (float)0));
        data.setOdometer(ReadValue(cursorData, ODOMETER, (float)0));
        data.setOrigOdometer(ReadValue(cursorData, ORIGODOMETER, (float)0));
        data.setTachometer(ReadValue(cursorData, TACHOMETER, (float)0));
        data.setTripOdometer(ReadValue(cursorData, TRIPODOMETER, (float)0));
        data.setEngineHours(ReadValue(cursorData, ENGINEHOURS, (float)0));
        data.setTripEngineSeconds(ReadValue(cursorData, TRIPENGINESECONDS, (float)0));
        data.setGpsValid(ReadValue(cursorData, GPSVALID, false));
        data.setGpsUncertDistance(ReadValue(cursorData, GPSUNCERT, (float)0));
        data.setIgnitionOn(ReadValue(cursorData, IGNITIONON, false));
        data.setEngineActivityDetected(ReadValue(cursorData, ENGINEACTIVITYDETECTED, false));
        data.setDateTimeValid(ReadValue(cursorData, DATETIMEVALID, false));
        data.setSpeedFromEngine(ReadValue(cursorData, SPEEDFROMENGINE, false));
        data.setOdometerFromEngine(ReadValue(cursorData, ODOMETERFROMENGINE, false));
        data.setVehicleId(ReadValue(cursorData, VEHICLEID, (String)null));
        data.setEventDataRecordKey(ReadValue(cursorData, EVENTDATARECORDKEY, (int)0));
        data.setSubmitted(ReadValue(cursorData, ISSUBMITTED, false));
        data.setOriginalTimestampUTC(ReadValue(cursorData, ORIGINALTIMESTAMPUTC, (DateTime)null, sqlDateTimeFormat));

        return data;
    }

    @Override
    protected ContentValues PersistContentValues(T data)
    {
        ContentValues content = super.PersistContentValues(data);

        SimpleDateFormat sqlDateTimeFormat = DateUtility.getSqlDateTimeFormatMS();
        sqlDateTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        PutValue(content, DRIVERID, data.getDriverId());
        PutValue(content, TIMESTAMPUTC, data.getTimestampUtc(), sqlDateTimeFormat);
        PutValue(content, GPSLATITUDE, data.getGpsLatitude());
        PutValue(content, GPSLONGITUDE, data.getGpsLongitude());
        PutValue(content, SPEEDOMETER, data.getSpeedometer());
        PutValue(content, ODOMETER, data.getOdometer());
        PutValue(content, ORIGODOMETER, data.getOrigOdometer());
        PutValue(content, TACHOMETER, data.getTachometer());
        PutValue(content, TRIPODOMETER, data.getTripOdometer());
        PutValue(content, ENGINEHOURS, data.getEngineHours());
        PutValue(content, TRIPENGINESECONDS, data.getTripEngineSeconds());
        PutValue(content, GPSVALID, data.isGpsValid());
        PutValue(content, GPSUNCERT, data.getGpsUncertDistance());
        PutValue(content, IGNITIONON, data.isIgnitionOn());
        PutValue(content, ENGINEACTIVITYDETECTED, data.isEngineActivityDetected());
        PutValue(content, DATETIMEVALID, data.isDateTimeValid());
        PutValue(content, SPEEDFROMENGINE, data.isSpeedFromEngine());
        PutValue(content, ODOMETERFROMENGINE, data.isOdometerFromEngine());
        PutValue(content, VEHICLEID, data.getVehicleId());
        PutValue(content, EVENTDATARECORDKEY, data.getEventDataRecordKey());
        PutValue(content, ISSUBMITTED, 0);
        PutValue(content, ORIGINALTIMESTAMPUTC, data.getOriginalTimestampUTC(), sqlDateTimeFormat);

        return content;
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // custom methods
    ///////////////////////////////////////////////////////////////////////////////////////

    public void PurgeOldRecords(Date cutoffDate)
    {
        PurgeOldRecords(cutoffDate,false);
    }

    /// <summary>
    /// Purge any old records, based on the cutoff date, using the PURGE command
    /// A parm of @cutoffDate will be added to the command.
    /// </summary>
    /// <param name="cutoffDate"></param>
    /// <param name="deleteUnsubmitted"></param>
    public void PurgeOldRecords(Date cutoffDate, boolean deleteUnsubmitted)
    {
        SimpleDateFormat sqlDateTimeFormat = DateUtility.getSqlDateTimeFormatMS();
        sqlDateTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        String sql = SQL_PURGE_SUBMITTED_COMMAND;

        if(deleteUnsubmitted){
            sql = SQL_PURGE_COMMAND;
        }
        String[] selectionArgs =  new String[]{sqlDateTimeFormat.format(cutoffDate)};

        ExecuteQuery(sql, selectionArgs);
    }

    /// <summary>
    /// Fetch most recent engine record.
    /// </summary>
    /// <returns>EngineRecord</returns>
    public GeotabHOSData FetchMostRecentEngineRecord(String vehicleId)
    {
        GeotabHOSData geotabHOSDataRecord = ExecuteFetchRawQuery(SQL_SELECT_MOSTRECENT_COMMAND, new String[]{vehicleId});
        return geotabHOSDataRecord;

    }

    /// <summary>
    /// Fetch most recent engine record.
    /// </summary>
    /// <returns>EngineRecord</returns>
    public List<T> FetchUnsubmittedLimited(int numberOfRecordsToFetch)
    {
        String[] selectionArgs = {String.valueOf(numberOfRecordsToFetch)};
        List<T> geotabHOSDataRecords = ExecuteFetchListRawQuery(SQL_SELECT_UNSUBMITTED_LIMITED_COMMAND, selectionArgs);
        return geotabHOSDataRecords;

    }

    /// <summary>
    /// Fetch engine record by timestamp.
    /// </summary>
    /// <returns>EngineRecord</returns>
    public GeotabHOSData FetchByTimestamp(String vehicleId, Date timestamp)
    {
        SimpleDateFormat sqlDateTimeFormat = DateUtility.getSqlDateTimeFormatMS();
        sqlDateTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        GeotabHOSData geotabHosData = ExecuteFetchRawQuery(SQL_SELECT_GETBYTIMESTAMP_COMMAND, new String[]{vehicleId,sqlDateTimeFormat.format(timestamp)});
        return geotabHosData;
    }

    public GeotabHOSData FetchByVehicleAndKey(String vehicleId, int key)
    {
        GeotabHOSData geotabHosData = ExecuteFetchRawQuery(SQL_SELECT_GETBYVEHICLEANDKEY_COMMAND, new String[]{vehicleId,Integer.toString(key)});
        return geotabHosData;
    }

    public GeotabHOSData FetchLatestByVehicle(String vehicleId) {
        GeotabHOSData geotabHOSData = ExecuteFetchRawQuery(SQL_SELECT_GETLATESTBYVEHICLE_COMMAND, new String[] { vehicleId });
        return geotabHOSData;
    }

    public void PurgeTable()
    {
        this.ExecuteQuery(SQL_PURGE_HOSDATATABLE_COMMAND, new String[0]);
    }
}
