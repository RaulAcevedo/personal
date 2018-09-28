package com.jjkeller.kmbapi.controller.dataaccess.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.jjkeller.kmbapi.controller.dataaccess.AbstractDBAdapter;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.proxydata.UnassignedEobrFailurePeriod;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class UnassignedEobrFailurePeriodPersist<T extends UnassignedEobrFailurePeriod> extends AbstractDBAdapter<T> {

	///////////////////////////////////////////////////////////////////////////////////////
	// private members
	///////////////////////////////////////////////////////////////////////////////////////
	private static final String EOBRIDENTIFIER = "EobrIdentifier";
	private static final String STARTTIME = "StartTime";
	private static final String STOPTIME = "StopTime";
	private static final String MESSAGE = "Message";
	private static final String EOBRSERIALNUMBER = "EobrSerialNumber";

    private static final String SQL_SELECT_PRIMARYKEY_COMMAND = "select [Key] from UnassignedEobrFailurePeriod where EobrSerialNumber=? and StartTime=?";
	private static final String SQL_SELECTUNSUBMITTED_COMMAND = "select * from UnassignedEobrFailurePeriod where IsSubmitted = 0";
	private static final String SQL_PURGE_COMMAND = "delete from UnassignedEobrFailurePeriod where startTime < ? and isSubmitted=1";
	
	///////////////////////////////////////////////////////////////////////////////////////
	// constructors
	///////////////////////////////////////////////////////////////////////////////////////
	public UnassignedEobrFailurePeriodPersist(Class<T> clazz, Context ctx) {
		super(clazz, ctx);
		
		setDbTableName(DB_TABLE_UNASSIGNEDEOBRFAILUREPERIOD);
	}

	public UnassignedEobrFailurePeriodPersist(Class<T> clazz, Context ctx, User user) {
		super(clazz, ctx, user);
		
		setDbTableName(DB_TABLE_UNASSIGNEDEOBRFAILUREPERIOD);
	}

	///////////////////////////////////////////////////////////////////////////////////////
	// @Override methods
	///////////////////////////////////////////////////////////////////////////////////////
	@Override
	protected String getSelectCommand() {
		return SQL_SELECTUNSUBMITTED_COMMAND;
	}
	
	@Override
	public String getSelectPrimaryKeyCommand() {
		return SQL_SELECT_PRIMARYKEY_COMMAND;
	}

	@Override
	protected String[] getSelectPrimaryKeyArgs(T data) {
		return new String[]{Long.toString(data.getPrimaryKey())};
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
		data.setMessage(ReadValue(cursorData, MESSAGE, (String)null));
		
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
		PutValue(content, STOPTIME, data.getStopTime(), sqlDateTimeFormat);
		PutValue(content, MESSAGE, data.getMessage());
		PutValue(content, ISSUBMITTED, 0);
		PutValue(content, EOBRSERIALNUMBER, data.getEobrSerialNumber());
		
		return content ;
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
