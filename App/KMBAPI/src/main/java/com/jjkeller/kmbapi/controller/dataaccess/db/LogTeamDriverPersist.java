package com.jjkeller.kmbapi.controller.dataaccess.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.jjkeller.kmbapi.controller.dataaccess.AbstractDBAdapter;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.enums.TimeZoneEnum;
import com.jjkeller.kmbapi.proxydata.TeamDriver;
import com.jjkeller.kmbapi.proxydata.TeamDriverList;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class LogTeamDriverPersist<T extends TeamDriver> extends AbstractDBAdapter<T> {

    ///////////////////////////////////////////////////////////////////////////////////////
    // private members
    ///////////////////////////////////////////////////////////////////////////////////////
    private long _employeeLogKey;

    private static String EMPLOYEELOGKEY = "EmployeeLogKey";
    private static String STARTTIME = "StartTime";
    private static String ENDTIME = "EndTime";
    private static String EMPLOYEECODE = "EmployeeCode";
    private static String DISPLAYNAME = "DisplayName";
    private static String KMBUSERNAME = "KMBUsername";
    private static String TIMEZONE = "TimeZone";

	private static final String SQL_SELECT_PRIMARYKEY_COMMAND = "select [Key] from LogTeamDriver where EmployeeLogKey=? AND StartTime=?";
	
	private static final String SQL_SELECT_COMMAND = "select * from LogTeamDriver where EmployeeLogKey=? order by LogTeamDriver.StartTime, LogTeamDriver.EndTime";

	///////////////////////////////////////////////////////////////////////////////////////
	// constructors
	///////////////////////////////////////////////////////////////////////////////////////
	public LogTeamDriverPersist(Class<T> clazz, Context ctx)
	{
		super(clazz, ctx);
		
		setDbTableName(DB_TABLE_LOGTEAMDRIVER);
	}
	
	public LogTeamDriverPersist(Class<T> clazz, Context ctx, User user, long employeeLogKey)
	{
		super (clazz, ctx, user);
		_employeeLogKey = employeeLogKey;
		
		setDbTableName(DB_TABLE_LOGTEAMDRIVER);
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
		if(data.getStartTime() == null)
			args = new String[]{Long.toString(this._employeeLogKey), ""};
		else
			args = new String[]{Long.toString(this._employeeLogKey), DateUtility.getHomeTerminalSqlDateTimeFormat().format(data.getStartTime())};
		
		return args;
	}

	@Override
	protected String getSelectCommand()
	{
		return SQL_SELECT_COMMAND;
	}
	
	@Override
	protected String[] getSelectArgs()
	{
		return new String[]{String.valueOf(_employeeLogKey)};
	}

    @Override
    protected T BuildObject(Cursor cursor) {
        T data = super.BuildObject(cursor);

		data.setEmployeeCode(ReadValue(cursor, EMPLOYEECODE, (String) null));
		data.setDisplayName(ReadValue(cursor, DISPLAYNAME, (String) null));
		data.setKMBUsername(ReadValue(cursor, KMBUSERNAME, (String) null));

		TimeZoneEnum timeZoneEnum = new TimeZoneEnum(TimeZoneEnum.NULL);
		timeZoneEnum.setValue(ReadValue(cursor, TIMEZONE, timeZoneEnum.getValue()));
		data.setTimeZone(timeZoneEnum.toTimeZone());

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		String startTimeString = ReadValue(cursor, STARTTIME, (String)null);
		Date startTime = null;
		if (startTimeString != null)
			startTime = DateUtility.getDateTimeFromString(startTimeString, dateFormat, timeZoneEnum.toTimeZone());
        data.setStartTime(startTime);

		String endTimeString = ReadValue(cursor, ENDTIME, (String)null);;
		Date endTime = null;
		if (endTimeString != null)
			endTime = DateUtility.getDateTimeFromString(endTimeString, dateFormat, timeZoneEnum.toTimeZone());
        data.setEndTime(endTime);

        return data;
    }

    @Override
    protected ContentValues PersistContentValues(T data) {
        ContentValues content = super.PersistContentValues(data);

		PutValue(content, EMPLOYEELOGKEY, _employeeLogKey);
		PutValue(content, EMPLOYEECODE, data.getEmployeeCode());
		PutValue(content, DISPLAYNAME, data.getDisplayName());
		PutValue(content, KMBUSERNAME, data.getKMBUsername());

		TimeZoneEnum timeZoneEnum = new TimeZoneEnum(TimeZoneEnum.NULL);
		timeZoneEnum.fromTimeZone(data.getTimeZone());
		PutValue(content, TIMEZONE, timeZoneEnum.getValue());

		SimpleDateFormat dateFormat = DateUtility.getHomeTerminalSqlDateTimeFormat(timeZoneEnum.toTimeZone());
		PutValue(content, STARTTIME, data.getStartTime(), dateFormat);
        PutValue(content, ENDTIME, data.getEndTime(), dateFormat);

		return content;
    }

	///////////////////////////////////////////////////////////////////////////////////////
	// custom methods
	///////////////////////////////////////////////////////////////////////////////////////
	public void Persist(TeamDriverList teamDriverList)
	{
		if(teamDriverList != null)
		{
			@SuppressWarnings("unchecked")
			T[] list = (T[])teamDriverList.getTeamDriverList();
			Persist(list); 
		}
	}
}
