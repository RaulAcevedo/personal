package com.jjkeller.kmbapi.controller.dataaccess.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.dataaccess.AbstractDBAdapter;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.proxydata.LogCheckerComplianceDates;

import java.util.Date;


public class LogCheckerComplianceDatesPersist<T extends LogCheckerComplianceDates> extends AbstractDBAdapter<T> {

	///////////////////////////////////////////////////////////////////////////////////////
	// private members
	///////////////////////////////////////////////////////////////////////////////////////
	private static final String COMPLIANCEDATE = "ComplianceDate";
	private static final String ITEMENUM = "ItemEnum";
	private static final String DESCRIPTION = "Description";
	private static final String COMPLIANCEENDDATE = "ComplianceEndDate";

	private static final String SQL_SELECT_PRIMARYKEY_COMMAND = "SELECT [Key] FROM LogCheckerComplianceDates WHERE ItemEnum = ?";
	private static final String SQL_SELECT_COMMAND = "SELECT * FROM LogCheckerComplianceDates WHERE Key > ?";
	private static final String SQL_SELECT_MOSTRECENT = "SELECT *, IFNULL(ComplianceEndDate, ComplianceDate) AS SortCol FROM LogCheckerComplianceDates ORDER BY SortCol DESC LIMIT 1";
	private static final String SQL_SELECT_BYENUM = "SELECT * FROM LogCheckerComplianceDates WHERE ItemEnum = ?";
	
	///////////////////////////////////////////////////////////////////////////////////////
	// constructors
	///////////////////////////////////////////////////////////////////////////////////////
	public LogCheckerComplianceDatesPersist (Class<T> clazz, Context ctx)
	{
		super(clazz, ctx, GlobalState.getInstance().getCurrentUser());
		
		setDbTableName(DB_TABLE_LOGCHECKERCOMPLIANCEDATES);
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
		return new String[]{ Integer.toString(data.getItemEnum())};
	}

	@Override
	protected String getSelectCommand()
	{
		return SQL_SELECT_COMMAND;
	}
	
	@Override
	protected String[] getSelectArgs()
	{
		// return all records - key > 0
		return new String[]{"0"};
	}
	
	@Override
	protected T BuildObject(Cursor cursorData)
	{
		T data = super.BuildObject(cursorData);

		data.setComplianceDate(ReadValue(cursorData, COMPLIANCEDATE, (Date)null, DateUtility.getHomeTerminalSqlDateFormat()));
		data.setItemEnum(ReadValue(cursorData, ITEMENUM, (int)0));
		data.setDescription(ReadValue(cursorData, DESCRIPTION, (String)null));
		data.setComplianceEndDate(ReadValue(cursorData, COMPLIANCEENDDATE, (Date)null, DateUtility.getHomeTerminalSqlDateFormat()));
		
		return data;
	}

	@Override
	public ContentValues PersistContentValues(T data)
	{		
		ContentValues content = super.PersistContentValues(data);
		
		PutValue(content, COMPLIANCEDATE, DateUtility.getHomeTerminalSqlDateFormat().format(data.getComplianceDate()));
		PutValue(content, ITEMENUM, data.getItemEnum());
		PutValue(content, DESCRIPTION, data.getDescription());
		PutValue(content, COMPLIANCEENDDATE, data.getComplianceEndDate() != null ? DateUtility.getHomeTerminalSqlDateFormat().format(data.getComplianceEndDate()) : null);

		return content;
	}
	
	///////////////////////////////////////////////////////////////////////////////////////
	// custom methods
	///////////////////////////////////////////////////////////////////////////////////////
	
	
	public LogCheckerComplianceDates FetchMostRecentLogCheckerComplianceDates()
	{
		LogCheckerComplianceDates compliance = ExecuteFetchRawQuery(SQL_SELECT_MOSTRECENT, null);

		return compliance;			
	}
	
	
	public LogCheckerComplianceDates FetchLogCheckerComplianceDatesByEnum(int complianceDatesType)
	{
		LogCheckerComplianceDates compliance = ExecuteFetchRawQuery(SQL_SELECT_BYENUM, new String[]{Integer.toString(complianceDatesType)});

		return compliance;			
	}
}
