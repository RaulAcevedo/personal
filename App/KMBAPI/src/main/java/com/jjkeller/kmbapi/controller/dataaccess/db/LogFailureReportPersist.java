package com.jjkeller.kmbapi.controller.dataaccess.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.jjkeller.kmbapi.controller.dataaccess.AbstractDBAdapter;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.enums.FailureCategoryEnum;
import com.jjkeller.kmbapi.proxydata.FailureReport;
import com.jjkeller.kmbapi.proxydata.FailureReportList;

import org.joda.time.DateTime;

public class LogFailureReportPersist<T extends FailureReport> extends AbstractDBAdapter<T> {

	///////////////////////////////////////////////////////////////////////////////////////
	// private members
	///////////////////////////////////////////////////////////////////////////////////////
	private long _employeeLogKey;
	private FailureCategoryEnum _category;

	private static String EMPLOYEELOGKEY = "EmployeeLogKey";
	private static String CATEGORY = "Category";
	private static String STARTTIME = "StartTime";
	private static String STOPTIME = "StopTime";
	private static String MESSAGE = "Message";

	private static final String SQL_SELECT_PRIMARYKEY_COMMAND = "select [Key] from LogFailureReport where EmployeeLogKey=? AND StartTime=? AND Category=?";

	private static final String SQL_SELECT_COMMAND = "select * from LogFailureReport where EmployeeLogKey=? AND Category=? order by LogFailureReport.StartTime, LogFailureReport.StopTime";

	///////////////////////////////////////////////////////////////////////////////////////
	// constructors
	///////////////////////////////////////////////////////////////////////////////////////
	public LogFailureReportPersist(Class<T> clazz, Context ctx)
	{
		super(clazz, ctx);
		
		setDbTableName(DB_TABLE_LOGFAILUREREPORT);
	}
	
	public LogFailureReportPersist(Class<T> clazz, Context ctx, User user, long employeeLogKey, FailureCategoryEnum category)
	{
		super (clazz, ctx, user);
		_employeeLogKey = employeeLogKey;
		_category = category;
		
		setDbTableName(DB_TABLE_LOGFAILUREREPORT);
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
			args = new String[]{Long.toString(this._employeeLogKey), "", Integer.toString(this._category.getValue())};
		else
			args = new String[]{Long.toString(this._employeeLogKey), DateUtility.getHomeTerminalSqlDateTimeFormat().format(data.getStartTime().toDate()), Integer.toString(this._category.getValue())};
		
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
		return new String[]{String.valueOf(_employeeLogKey), Integer.toString(this._category.getValue())};
	}

	@Override
	protected T BuildObject(Cursor cursor)
	{		
		T data = super.BuildObject(cursor);

		data.getCategory().setValue(ReadValue(cursor, CATEGORY, FailureCategoryEnum.NULL));
		data.setStartTime(ReadValue(cursor, STARTTIME, (DateTime)null, DateUtility.getHomeTerminalSqlDateTimeFormat()));
		data.setStopTime(ReadValue(cursor, STOPTIME, (DateTime)null, DateUtility.getHomeTerminalSqlDateTimeFormat()));
		data.setMessage(ReadValue(cursor, MESSAGE, (String)null));

		return data;
	}

	@Override
	public ContentValues PersistContentValues(T data)
	{
		ContentValues content = super.PersistContentValues(data);
			
		PutValue(content, EMPLOYEELOGKEY, _employeeLogKey);
		PutValue(content, CATEGORY, _category.getValue());
		PutValue(content, STARTTIME, data.getStartTime(), DateUtility.getHomeTerminalSqlDateTimeFormat());
		PutValue(content, STOPTIME, data.getStopTime(), DateUtility.getHomeTerminalSqlDateTimeFormat());
		PutValue(content, MESSAGE, data.getMessage());

		return content;
	}
	
	///////////////////////////////////////////////////////////////////////////////////////
	// custom methods
	///////////////////////////////////////////////////////////////////////////////////////
	public void Persist(FailureReportList failureReportList)
	{
		if(failureReportList != null)
		{
			@SuppressWarnings("unchecked")
			T[] list = (T[])failureReportList.getFailureReportList();
			Persist(list); 
		}
	}
}
