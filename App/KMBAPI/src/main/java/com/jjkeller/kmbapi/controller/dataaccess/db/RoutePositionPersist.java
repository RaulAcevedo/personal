package com.jjkeller.kmbapi.controller.dataaccess.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.jjkeller.kmbapi.controller.dataaccess.AbstractDBAdapter;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.proxydata.RoutePosition;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class RoutePositionPersist<T extends RoutePosition> extends AbstractDBAdapter<T> {

	///////////////////////////////////////////////////////////////////////////////////////
	// private members
	///////////////////////////////////////////////////////////////////////////////////////
	private static final String EOBRID = "EobrId";
	private static final String GPSTIMESTAMP = "GpsTimestamp";
	private static final String GPSLATITUDE = "GpsLatitude";
	private static final String GPSLONGITUDE = "GpsLongitude";
	private static final String ODOMETER = "Odometer";
	private static final String ISSUBMITTED = "IsSubmitted";
	private static final String EOBRSERIALNUMBER = "EobrSerialNumber";
	
    private static final String SQL_SELECT_PRIMARYKEY_COMMAND = "select [Key] from [RoutePosition] where EobrSerialNumber=? and GpsTimestamp=?";
    private static final String SQL_PURGE_COMMAND = "delete from [RoutePosition] where GpsTimestamp < ? AND IsSubmitted=1";

    ///////////////////////////////////////////////////////////////////////////////////////
	// constructors
	///////////////////////////////////////////////////////////////////////////////////////
	public RoutePositionPersist(Class<T> clazz, Context ctx) {
		super(clazz, ctx);
		
		setDbTableName(DB_TABLE_ROUTEPOSITION);
	}

	public RoutePositionPersist(Class<T> clazz, Context ctx, User user) {
		super(clazz, ctx, user);
		
		setDbTableName(DB_TABLE_ROUTEPOSITION);
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
	protected T BuildObject(Cursor cursor)
	{
		T data = super.BuildObject(cursor);

		SimpleDateFormat sqlDateTimeFormat = DateUtility.getHomeTerminalSqlDateTimeFormat();
		sqlDateTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

		data.setEobrId(ReadValue(cursor, EOBRID, (String)null));
		data.setGpsTimestamp(ReadValue(cursor, GPSTIMESTAMP, (Date)null, sqlDateTimeFormat));
		data.setGpsLatitude(ReadValue(cursor, GPSLATITUDE, (float)0));
		data.setGpsLongitude(ReadValue(cursor, GPSLONGITUDE, (float)0));
		data.setOdometer(ReadValue(cursor, ODOMETER, (float)0));
		data.setEobrSerialNumber(ReadValue(cursor, EOBRSERIALNUMBER, (String)null));
		
		return data;
	}

    @Override
    protected ContentValues PersistContentValues(T data)
    {
    	ContentValues content = super.PersistContentValues(data);
    	
		SimpleDateFormat sqlDateTimeFormat = DateUtility.getHomeTerminalSqlDateTimeFormat();
		sqlDateTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

    	PutValue(content, EOBRID, data.getEobrId());
    	PutValue(content, GPSTIMESTAMP, data.getGpsTimestamp(), sqlDateTimeFormat);
    	PutValue(content, GPSLATITUDE, data.getGpsLatitude());
    	PutValue(content, GPSLONGITUDE, data.getGpsLongitude());
    	PutValue(content, ODOMETER, data.getOdometer());
    	PutValue(content, ISSUBMITTED, 0);
    	PutValue(content, EOBRSERIALNUMBER, data.getEobrSerialNumber());
    	
    	return content;
    }

	///////////////////////////////////////////////////////////////////////////////////////
	// custom methods
	///////////////////////////////////////////////////////////////////////////////////////
    
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
