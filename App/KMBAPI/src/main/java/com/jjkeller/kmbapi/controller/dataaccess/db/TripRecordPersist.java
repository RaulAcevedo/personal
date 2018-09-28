package com.jjkeller.kmbapi.controller.dataaccess.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.jjkeller.kmbapi.controller.dataaccess.AbstractDBAdapter;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.proxydata.TripRecord;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class TripRecordPersist<T extends TripRecord>  extends AbstractDBAdapter<T>
{
	///////////////////////////////////////////////////////////////////////////////////////
	// private members
	///////////////////////////////////////////////////////////////////////////////////////
    private static final String EMPLOYEE_ID = "EmployeeId";
    private static final String EOBR_SERIAL_NUMBER = "EobrSerialNumber";
    private static final String EOBR_TRACTOR_NUMBER = "EobrTractorNumber";
    private static final String TRIP_NUMBER = "TripNumber";
    private static final String IGNITION_STATE = "IgnitionState";
    private static final String ODOMETER = "Odometer";
    private static final String TRIP_SECS = "TripSecs";
    private static final String TRIP_DIST = "TripDist";
    private static final String IDLE_SECS = "IdleSecs";
    private static final String GPS_LATITUDE = "GpsLatitude";
    private static final String GPS_LONGITUDE = "GpsLongitude";
    private static final String MAX_SPEED = "MaxSpeed";
    private static final String TRIP_FUEL = "TripFuel";
    private static final String TIMESTAMP = "Timestamp";
    private static final String ALLOWED_SPEED = "AllowedSpeed";
    private static final String ALLOWED_TACH = "AllowedTach";
    private static final String MAX_TACH = "MaxEngRPM";
    private static final String AVG_TACH = "AvgEngRPM";

    private static final String SQL_SELECT_PRIMARYKEY_COMMAND = "select [Key] from [" + DB_TABLE_TRIPRECORD + "] where " + EMPLOYEE_ID + "=? and " + TIMESTAMP + "=? and " + IGNITION_STATE + "=?";
    private static final String SQL_SELECT_UNSUBMITTED_LIMITED_COMMAND = "select * from [" + DB_TABLE_TRIPRECORD + "] where IsSubmitted=0 limit ?";
    private final String SQL_PURGE_COMMAND = "delete from TripRecord where Timestamp < ? AND IsSubmitted=1";
	///////////////////////////////////////////////////////////////////////////////////////
	// constructors
	///////////////////////////////////////////////////////////////////////////////////////
    public TripRecordPersist(Class<T> clazz, Context ctx)
    {
		super(clazz, ctx);
		setDbTableName(DB_TABLE_TRIPRECORD);
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
	protected String[] getSelectPrimaryKeyArgs(T data)
	{
		String timestamp;
		if (data.getTimestamp() == null)
		{
			timestamp = "";
		}
		else
		{
			SimpleDateFormat sqlDateTimeFormat = DateUtility.getHomeTerminalSqlDateTimeFormat();
			sqlDateTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
			timestamp = sqlDateTimeFormat.format(data.getTimestamp());
		}
		
		return new String[] { data.getEmployeeId(), timestamp, String.valueOf(data.getIgnitionState()) };
	}
	
	@Override
	protected T BuildObject(Cursor cursorData)
	{
		T data = super.BuildObject(cursorData);

		SimpleDateFormat sqlDateTimeFormat = DateUtility.getHomeTerminalSqlDateTimeFormat();
		sqlDateTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		data.setEmployeeId(ReadValue(cursorData, EMPLOYEE_ID, "00000000-0000-0000-0000-000000000000"));
		data.setEobrSerialNumber(ReadValue(cursorData, EOBR_SERIAL_NUMBER, (String)null));
		data.setEobrTractorNumber(ReadValue(cursorData, EOBR_TRACTOR_NUMBER, (String)null));
		data.setTripNumber(ReadValue(cursorData, TRIP_NUMBER, 0));
		data.setIgnitionState(ReadValue(cursorData, IGNITION_STATE, 0));
		data.setOdometer(ReadValue(cursorData, ODOMETER, 0f));
		data.setTripSecs(ReadValue(cursorData, TRIP_SECS, 0));
		data.setTripDist(ReadValue(cursorData, TRIP_DIST, 0f));
		data.setIdleSecs(ReadValue(cursorData, IDLE_SECS, 0));
		data.setGpsLatitude(ReadValue(cursorData, GPS_LATITUDE, Float.NaN));
		data.setGpsLongitude(ReadValue(cursorData, GPS_LONGITUDE, Float.NaN));
		data.setMaxSpeed(ReadValue(cursorData, MAX_SPEED, 0f));
		data.setTripFuel(ReadValue(cursorData, TRIP_FUEL, 0f));
		data.setTimestamp(ReadValue(cursorData, TIMESTAMP, (Date)null, sqlDateTimeFormat));
		data.setAllowedSpeed(ReadValue(cursorData, ALLOWED_SPEED, 0f));
		data.setAllowedTach(ReadValue(cursorData, ALLOWED_TACH, 0f));
		data.setIsSubmitted(ReadValue(cursorData, ISSUBMITTED, false));
		data.setMaxEngRPM(ReadValue(cursorData, MAX_TACH, 0));
		data.setAvgEngRPM(ReadValue(cursorData, AVG_TACH, 0));
		
		return data;
	}

	@Override
	protected ContentValues PersistContentValues(T data)
	{
		ContentValues content = super.PersistContentValues(data);

		SimpleDateFormat sqlDateTimeFormat = DateUtility.getHomeTerminalSqlDateTimeFormat();
		sqlDateTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		PutValue(content, EMPLOYEE_ID, data.getEmployeeId());
		PutValue(content, EOBR_SERIAL_NUMBER, data.getEobrSerialNumber());
		PutValue(content, EOBR_TRACTOR_NUMBER, data.getEobrTractorNumber());
		PutValue(content, TRIP_NUMBER, data.getTripNumber());
		PutValue(content, IGNITION_STATE, data.getIgnitionState());
		PutValue(content, ODOMETER, data.getOdometer());
		PutValue(content, TRIP_SECS, data.getTripSecs());
		PutValue(content, TRIP_DIST, data.getTripDist());
		PutValue(content, IDLE_SECS, data.getIdleSecs());
		PutValue(content, GPS_LATITUDE, data.getGpsLatitude());
		PutValue(content, GPS_LONGITUDE, data.getGpsLongitude());
		PutValue(content, MAX_SPEED, data.getMaxSpeed());
		PutValue(content, TRIP_FUEL, data.getTripFuel());
		PutValue(content, TIMESTAMP, data.getTimestamp(), sqlDateTimeFormat);
		PutValue(content, ALLOWED_SPEED, data.getAllowedSpeed());
		PutValue(content, ALLOWED_TACH, data.getAllowedTach());
		PutValue(content, ISSUBMITTED, data.getIsSubmitted());
		PutValue(content, MAX_TACH, data.getMaxEngRPM());
		PutValue(content, AVG_TACH, data.getAvgEngRPM());

		return content;
	}
	
	///////////////////////////////////////////////////////////////////////////////////////
	// custom methods
	///////////////////////////////////////////////////////////////////////////////////////
	
    /// <summary>
    /// Fetch up to X unsubmitted trip records
    /// </summary>
    /// <returns>TripRecord</returns>
    public List<T> FetchUnsubmittedLimited(int numberOfRecordsToFetch)
    {
		String[] selectionArgs = {String.valueOf(numberOfRecordsToFetch)};
		List<T> tripRecords = ExecuteFetchListRawQuery(SQL_SELECT_UNSUBMITTED_LIMITED_COMMAND, selectionArgs);
		return tripRecords;
    }   

    /// <summary>
    /// Purge any old records, based on the cutoff date, using the PURGE command
    /// A parm of @cutoffDate will be added to the command.
    /// </summary>
    /// <param name="cutoffDate"></param>
    public void PurgeOldRecords(Date cutoffDate)
    {
		SimpleDateFormat sqlDateTimeFormat = DateUtility.getHomeTerminalSqlDateTimeFormat();
		sqlDateTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

    	String sql = SQL_PURGE_COMMAND;
		String[] selectionArgs =  new String[]{sqlDateTimeFormat.format(cutoffDate)};

		ExecuteQuery(sql, selectionArgs);
    }
}
