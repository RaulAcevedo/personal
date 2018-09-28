package com.jjkeller.kmbapi.controller.dataaccess.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.jjkeller.kmbapi.controller.dataaccess.AbstractDBAdapter;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.proxydata.GpsLocation;
import com.jjkeller.kmbapi.proxydata.UnassignedDrivingPeriod;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class UnassignedDrivingPeriodPersist<T extends UnassignedDrivingPeriod> extends AbstractDBAdapter<T> {

	///////////////////////////////////////////////////////////////////////////////////////
	// private members
	///////////////////////////////////////////////////////////////////////////////////////
	private static final String EOBRIDENTIFIER = "EobrIdentifier";
	private static final String STARTTIME = "StartTime";
	private static final String STARTGPSTIMESTAMP = "StartGPSTimestamp";
	private static final String STARTLATITUDEDEGREES = "StartLatitudeDegrees";
	private static final String STARTLONGITUDEDEGREES = "StartLongitudeDegrees";
	private static final String STOPTIME = "StopTime";
	private static final String STOPGPSTIMESTAMP = "StopGPSTimestamp";
	private static final String STOPLATITUDEDEGREES = "StopLatitudeDegrees";
	private static final String STOPLONGITUDEDEGREES = "StopLongitudeDegrees";
	private static final String DISTANCE = "Distance";
	private static final String ISCLAIMED = "IsClaimed";
	private static final String EOBRSERIALNUMBER = "EobrSerialNumber";
	private static final String STARTODOMETER = "StartOdometer";
	private static final String STOPODOMETER = "StopOdometer";
	private static final String ENCOMPASSID = "EncompassId";

	private static final String SQL_SELECT_PRIMARYKEY_COMMAND = "select [Key] from UnassignedDrivingPeriod where EobrSerialNumber=? and StartTime=?";
	private static final String SQL_SELECTUNSUBMITTED_COMMAND = "select * from UnassignedDrivingPeriod where IsClaimed = 0 and IsSubmitted = 0";
	private static final String SQL_SELECTUNCLAIMED_COMMAND = "select * from UnassignedDrivingPeriod where IsClaimed = 0 and (IsSubmitted = 0 or EncompassId is not null)";
    private static final String SQL_SELECTBYDATE_COMMAND = "select * from UnassignedDrivingPeriod where StartTime >= ? and IsClaimed = 0 and (IsSubmitted = 0 or EncompassId is not null)";

	private static final String SQL_PURGE_COMMAND = "delete from UnassignedDrivingPeriod where startTime < ? and (isSubmitted=1 or isClaimed=1)";
	// For geotab we need to compare downloaded records with existing ones regardless of submitted or not
	private static final String SQL_SELECTALLBYDATE_COMMAND = "select * from UnassignedDrivingPeriod where StartTime >= ? ";
    
    ///////////////////////////////////////////////////////////////////////////////////////
	// constructors
	///////////////////////////////////////////////////////////////////////////////////////
	public UnassignedDrivingPeriodPersist(Class<T> clazz, Context ctx) {
		super(clazz, ctx);
		
		setDbTableName(DB_TABLE_UNASSIGNEDDRIVINGPERIOD);
	}

	public UnassignedDrivingPeriodPersist(Class<T> clazz, Context ctx, User user) {
		super(clazz, ctx, user);
		
		setDbTableName(DB_TABLE_UNASSIGNEDDRIVINGPERIOD);
	}

	///////////////////////////////////////////////////////////////////////////////////////
	// @Override methods
	///////////////////////////////////////////////////////////////////////////////////////
	@Override
	public String getSelectPrimaryKeyCommand() {
		return SQL_SELECT_PRIMARYKEY_COMMAND;
	}

	@Override
	protected String[] getSelectPrimaryKeyArgs(T data) {
		return new String[]{Long.toString(data.getPrimaryKey())};
	}
	
	@Override
	protected String getSelectCommand() {
		return SQL_SELECTUNSUBMITTED_COMMAND;
	}

	@Override
	protected String getSelectUnsubmittedCommand() {
		return SQL_SELECTUNSUBMITTED_COMMAND;
	}

	@Override
	protected T BuildObject(Cursor cursorData)
	{
		T data = super.BuildObject(cursorData);
		
		SimpleDateFormat sqlDateTimeFormat = DateUtility.getHomeTerminalSqlDateTimeFormat();
		sqlDateTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		data.setEobrId(ReadValue(cursorData, EOBRIDENTIFIER, (String)null));
		data.setEobrSerialNumber(ReadValue(cursorData, EOBRSERIALNUMBER, (String)null));
		data.setStartTime(ReadValue(cursorData, STARTTIME, (Date)null, sqlDateTimeFormat));
		data.setStopTime(ReadValue(cursorData, STOPTIME, (Date)null, sqlDateTimeFormat));
		
		GpsLocation gpsStartLoc = new GpsLocation();
		gpsStartLoc.setTimestampUtc(ReadValue(cursorData, STARTGPSTIMESTAMP, (Date)null, sqlDateTimeFormat));
		gpsStartLoc.setLatitudeDegrees(ReadValue(cursorData, STARTLATITUDEDEGREES, (float)0));
		gpsStartLoc.setLongitudeDegrees(ReadValue(cursorData, STARTLONGITUDEDEGREES, (float)0));
		data.setStartLocation(gpsStartLoc);

		GpsLocation gpsStopLoc = new GpsLocation();
		gpsStopLoc.setTimestampUtc(ReadValue(cursorData, STOPGPSTIMESTAMP, (Date)null, sqlDateTimeFormat));
		gpsStopLoc.setLatitudeDegrees(ReadValue(cursorData, STOPLATITUDEDEGREES, (float)0));
		gpsStopLoc.setLongitudeDegrees(ReadValue(cursorData, STOPLONGITUDEDEGREES, (float)0));
		data.setStopLocation(gpsStopLoc);
		
		data.setDistance(ReadValue(cursorData, DISTANCE, (float)-1));
		data.setStartOdometer(ReadValue(cursorData, STARTODOMETER, (float)-1));
		data.setStopOdometer(ReadValue(cursorData, STOPODOMETER, (float)-1));
		data.setEncompassId(ReadValue(cursorData,ENCOMPASSID, (String) null ));
		data.setIsClaimed(ReadValue(cursorData, ISCLAIMED, false ));
		
		return data;
	}
	
	@Override
	protected ContentValues PersistContentValues(T data)
	{
		ContentValues content = super.PersistContentValues(data);

		SimpleDateFormat sqlDateTimeFormat = DateUtility.getHomeTerminalSqlDateTimeFormat();
		sqlDateTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

		PutValue(content, EOBRIDENTIFIER, data.getEobrId());
		PutValue(content, STARTTIME, data.getStartTime(), sqlDateTimeFormat);
		if(data.getStartLocation() != null && !data.getStartLocation().IsEmpty())
		{
			PutValue(content, STARTGPSTIMESTAMP, data.getStartLocation().getTimestampUtc(), sqlDateTimeFormat);
			PutValue(content, STARTLATITUDEDEGREES, data.getStartLocation().getLatitudeDegrees());
			PutValue(content, STARTLONGITUDEDEGREES, data.getStartLocation().getLongitudeDegrees());
		}
		PutValue(content, STOPTIME, data.getStopTime(), sqlDateTimeFormat);
		if(data.getStartLocation() != null && !data.getStartLocation().IsEmpty())
		{
			PutValue(content, STOPGPSTIMESTAMP, data.getStopLocation().getTimestampUtc(), sqlDateTimeFormat);
			PutValue(content, STOPLATITUDEDEGREES, data.getStopLocation().getLatitudeDegrees());
			PutValue(content, STOPLONGITUDEDEGREES, data.getStopLocation().getLongitudeDegrees());
		}
		PutValue(content, DISTANCE, data.getDistance());
		PutValue(content, ISSUBMITTED, data.getIsSubmitted());
		PutValue(content, ISCLAIMED, 0);
		PutValue(content, EOBRSERIALNUMBER, data.getEobrSerialNumber());
		PutValue(content, STARTODOMETER, data.getStartOdometer());
		PutValue(content, STOPODOMETER, data.getStopOdometer());
		PutValue (content, ENCOMPASSID, data.getEncompassId());

		return content;
	}

	///////////////////////////////////////////////////////////////////////////////////////
	// custom methods
	///////////////////////////////////////////////////////////////////////////////////////
	public void MarkAsClaimed(T obj)
	{
		ContentValues content = new ContentValues();
		PutValue(content, ISCLAIMED, 1);
		String errorMsg = "Can't mark as claimed without PK.";
	
		ExecuteUpdate(obj, content, errorMsg);
	}

	public List<T> FetchAllUnClaimed(){
		List<T> list = ExecuteFetchListRawQuery(String.format(SQL_SELECTUNCLAIMED_COMMAND, getDbTableName()), null);
		return list;
	}

	public List<T> FetchUnsubmittedByDate(java.util.Date date) {
		DateTimeZone dateTimeZone = DateTimeZone.forTimeZone(TimeZone.getTimeZone("UTC"));
		DateTime dateTime = new DateTime(date).toDateTime(dateTimeZone);
		DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("Y-MM-dd kk:mm:ss");
		String sql = SQL_SELECTBYDATE_COMMAND;
		String formattedDateTimeString = dateTimeFormatter.print(dateTime);
		String[] selectionArgs = new String[]{formattedDateTimeString};
		List<T> list = ExecuteFetchListRawQuery(sql, selectionArgs);
		return list;
	}
	//  Alex -> Since we already had the date in a string format I simplified this a bit
	public List<T> FetchAllByDate(String startTimeUTC) {
		String sql = SQL_SELECTALLBYDATE_COMMAND;
		String[] selectionArgs = new String[]{startTimeUTC};
		List<T> list = ExecuteFetchListRawQuery(sql, selectionArgs);
		return list;
	}

	
    /// <summary>
    /// Purge any old records, based on the cutoff date, using the PURGE command
    /// A parm of @cutoffDate will be added to the command.
    /// </summary>
    /// <param name="cutoffDate"></param>
    public void PurgeOldRecords(Date cutoffDate)
    {
		String sql = SQL_PURGE_COMMAND;
		String[] selectionArgs =  new String[]{DateUtility.getHomeTerminalSqlDateTimeFormat().format(cutoffDate)};

		ExecuteQuery(sql, selectionArgs);
    }
    
}
