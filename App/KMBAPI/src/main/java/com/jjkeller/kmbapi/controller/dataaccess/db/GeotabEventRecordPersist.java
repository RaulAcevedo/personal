package com.jjkeller.kmbapi.controller.dataaccess.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.jjkeller.kmbapi.controller.dataaccess.AbstractDBAdapter;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.kmbeobr.EventTypeEnum;
import com.jjkeller.kmbapi.proxydata.GeotabEventRecord;
import com.jjkeller.kmbapi.proxydata.GeotabHOSData;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by jhm2586 on 9/20/2016.
 */
public class GeotabEventRecordPersist <T extends GeotabEventRecord>extends AbstractDBAdapter<T> {
    ///////////////////////////////////////////////////////////////////////////////////////
    // private members
    ///////////////////////////////////////////////////////////////////////////////////////
    private static final String TIMESTAMPUTC = "TimestampUtc";
    private static final String DRIVERID = "DriverId";
    private static final String VEHICLEID = "VehicleId";
    private static final String EVENTTYPE = "EventType";
    private static final String EVENTDATA = "EventData";
    private static final String GEOTABHOSDATAKEY = "GeotabHOSDataKey";

    private final String SQL_SELECT_PRIMARYKEY_COMMAND = "select [Key] from [GeotabEventRecord] where VehicleId=? and TimestampUtc=?";
    private static final String SQL_SELECT_GETBYTIMESTAMP_COMMAND = "select * from [GeotabEventRecord] where VehicleId=? and TimestampUtc>=? order by TimestampUtc asc limit 1";
    private static final String SQL_SELECT_GET_BY_TIMESTAMP_FRAME_COMMAND = "SELECT * FROM [GeotabEventRecord] WHERE VehicleId=? AND EventType=? AND TimestampUtc>=? AND TimestampUtc<=? order by TimestampUtc DESC LIMIT 1";
    private static final String SQL_SELECT_GETLATESTBYVEHICLE_COMMAND = "select * from [GeotabEventRecord] where VehicleId=? order by TimestampUtc desc limit 1";
    private static final String SQL_SELECT_GETBYTIMESTAMPANDEVENTTYPE_COMMAND = "select * from [GeotabEventRecord] where VehicleId=? and TimestampUtc>=? and EventType IN (?) order by TimestampUtc asc limit 1";
    private static final String SQL_PURGE_EVENTRECORDTABLE_COMMAND = "delete from GeotabEventRecord";
    private final String SQL_PURGE_COMMAND = "delete from [GeotabEventRecord] where TimestampUtc < ? ";

    public GeotabEventRecordPersist(Class<T> clazz, Context ctx)
    {
        super(clazz, ctx);

        setDbTableName(DB_TABLE_GEOTABEVENTRECORD);
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
        else if(data.getVehicleId() == null)
            args = new String[]{"", sqlDateTimeFormat.format(data.getTimestampUtc())};
        else if(data.getTimestampUtc() == null)
            args = new String[]{data.getVehicleId(), ""};
        else
        {
            args = new String[]{data.getVehicleId(), sqlDateTimeFormat.format(data.getTimestampUtc())};
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
        data.setTimestampUtc(ReadValue(cursorData, TIMESTAMPUTC, (Date)null, sqlDateTimeFormat));
        data.setVehicleId(ReadValue(cursorData, VEHICLEID, (String)null));
        data.setEventType(ReadValue(cursorData, EVENTTYPE, (int)-1));
        data.setEventData(ReadValue(cursorData, EVENTDATA, (int)0));
        data.setGeotabHosDataKey(ReadValue(cursorData, GEOTABHOSDATAKEY, (int)0));

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
        PutValue(content, VEHICLEID, data.getVehicleId());
        PutValue(content, EVENTTYPE, data.getEventType());
        PutValue(content, EVENTDATA, data.getEventData());
        PutValue(content, GEOTABHOSDATAKEY, data.getGeotabHosDataKey());

        return content;
    }


    @Override
    protected void GetAdditionalData(T event)
    {
        super.GetAdditionalData(event);
        if (event != null)
        {
            // Get GeotabHosData for the event
            // Check key for valid value
            if(event.getGeotabHosDataKey() > 0) {
                GeotabHOSDataPersist<GeotabHOSData> geotabHosDataPersist = new GeotabHOSDataPersist<GeotabHOSData>(GeotabHOSData.class, this.getContext());
                GeotabHOSData geotabHosData = geotabHosDataPersist.FetchByVehicleAndKey(event.getVehicleId(), event.getGeotabHosDataKey());
                event.setHosData(geotabHosData);
            }
        }
    }
    ///////////////////////////////////////////////////////////////////////////////////////
    // custom methods
    ///////////////////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// Fetch engine record by timestamp.
    /// </summary>
    /// <returns>EngineRecord</returns>
    public GeotabEventRecord FetchByTimestamp(String vehicleId, Date timestamp)
    {
        SimpleDateFormat sqlDateTimeFormat = DateUtility.getSqlDateTimeFormatMS();
        sqlDateTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        GeotabEventRecord geotabEventRecord = ExecuteFetchRawQuery(SQL_SELECT_GETBYTIMESTAMP_COMMAND, new String[]{vehicleId,sqlDateTimeFormat.format(timestamp)});
        return geotabEventRecord;
    }

    public GeotabEventRecord FetchByTimestampFrame(String vehicleId, int eventType, Date initialTimestamp, Date finalTimestamp)
    {
        SimpleDateFormat sqlDateTimeFormat = DateUtility.getSqlDateTimeFormatMS();
        sqlDateTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        GeotabEventRecord geotabEventRecord = ExecuteFetchRawQuery(SQL_SELECT_GET_BY_TIMESTAMP_FRAME_COMMAND,
                new String[]{vehicleId, Integer.toString(eventType), sqlDateTimeFormat.format(initialTimestamp), sqlDateTimeFormat.format(finalTimestamp)});
        return geotabEventRecord;
    }

    public GeotabEventRecord FetchLatestByVehicle(String vehicleId)
    {
        SimpleDateFormat sqlDateTimeFormat = DateUtility.getSqlDateTimeFormatMS();
        sqlDateTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        GeotabEventRecord geotabEventRecord = ExecuteFetchRawQuery(SQL_SELECT_GETLATESTBYVEHICLE_COMMAND, new String[]{ vehicleId });
        return geotabEventRecord;
    }

    public GeotabEventRecord FetchByTimestampAndEventType(String vehicleId, Date initialTimestamp, List<Integer> eventTypes)
    {
        SimpleDateFormat sqlDateTimeFormat = DateUtility.getSqlDateTimeFormatMS();
        sqlDateTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        StringBuilder eventTypesCommaDelim = new StringBuilder();
        for (Integer val : eventTypes)
        {
            if(eventTypesCommaDelim.length() > 0)
                eventTypesCommaDelim.append(",");
            eventTypesCommaDelim.append(Integer.toString(val));
        }

        String sql = SQL_SELECT_GETBYTIMESTAMPANDEVENTTYPE_COMMAND.replace("(?)", "(" + eventTypesCommaDelim.toString() + ")");
        GeotabEventRecord geotabEventRecord = ExecuteFetchRawQuery(sql,
                new String[]{vehicleId, sqlDateTimeFormat.format(initialTimestamp)});
        return geotabEventRecord;
    }

    public void PurgeTable()
	{
        this.ExecuteQuery(SQL_PURGE_EVENTRECORDTABLE_COMMAND, new String[0]);
    }

    public void PurgeOldRecords(Date cutoffDate)
    {
        SimpleDateFormat sqlDateTimeFormat = DateUtility.getSqlDateTimeFormatMS();
        sqlDateTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        String sql = SQL_PURGE_COMMAND;

        String[] selectionArgs =  new String[]{sqlDateTimeFormat.format(cutoffDate)};

        ExecuteQuery(sql, selectionArgs);
    }
}
