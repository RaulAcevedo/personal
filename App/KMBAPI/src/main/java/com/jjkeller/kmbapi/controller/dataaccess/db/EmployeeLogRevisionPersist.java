package com.jjkeller.kmbapi.controller.dataaccess.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.jjkeller.kmbapi.controller.dataaccess.AbstractDBAdapter;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.enums.EmployeeLogRevisionTypeEnum;
import com.jjkeller.kmbapi.proxydata.EmployeeLogRevision;

import java.util.Date;

;

public class EmployeeLogRevisionPersist<T extends EmployeeLogRevision> extends AbstractDBAdapter<T> {

	///////////////////////////////////////////////////////////////////////////////////////
	// private members
	///////////////////////////////////////////////////////////////////////////////////////
	private static final String EMPLOYEECODE = "EmployeeCode";
	private static final String EMPLOYEELOGDATE = "EmployeeLogDate";
	private static final String REVISIONTYPE = "RevisionType";

    private static final String SQL_SELECT_PRIMARYKEY_COMMAND = "select [Key] from EmployeeLogRevision where EmployeeLogDate=? and EmployeeCode=? and RevisionType=?";
	private static final String SQL_SELECT_COMMAND = "select * from EmployeeLogRevision where EmployeeCode=? order by EmployeeLogRevision.EmployeeLogDate";
    private static final String SQL_UPDATE_SUBMITTED = "update EmployeeLogRevision set IsSubmitted = 1 where Key=?";	

	///////////////////////////////////////////////////////////////////////////////////////
	// constructors
	///////////////////////////////////////////////////////////////////////////////////////
	public EmployeeLogRevisionPersist (Class<T> clazz, Context ctx, User user)
	{
		super(clazz, ctx, user);
		
		setDbTableName(DB_TABLE_EMPLOYEELOGREVISION);
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
			args = new String[]{DateUtility.getHomeTerminalSqlDateTimeFormat().format(data.getEmployeeLogDate()), data.getEmployeeCode(), Integer.toString(data.getRevisionType().getValue())};
		
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
		return new String[]{this.getCurrentUser().getCredentials().getEmployeeCode()};
	}

	@Override
	public T BuildObject(Cursor cursorData)
	{
		T data = super.BuildObject(cursorData);
		
		data.setEmployeeLogDate(ReadValue(cursorData, EMPLOYEELOGDATE, (Date)null, DateUtility.getHomeTerminalSqlDateFormat(this.getCurrentUser())));
		data.setEmployeeCode(ReadValue(cursorData, EMPLOYEECODE, (String)null));
		data.getRevisionType().setValue(ReadValue(cursorData, REVISIONTYPE, EmployeeLogRevisionTypeEnum.NONE));
		return data;
	}

	@Override
	public ContentValues PersistContentValues(T data)
	{
		ContentValues content = super.PersistContentValues(data);
		
		PutValue(content, EMPLOYEELOGDATE, data.getEmployeeLogDate(), DateUtility.getHomeTerminalSqlDateFormat(this.getCurrentUser()));
		PutValue(content, EMPLOYEECODE, data.getEmployeeCode());
		PutValue(content, REVISIONTYPE, data.getRevisionType().getValue());
		
		return content;
	}

	///////////////////////////////////////////////////////////////////////////////////////
	// custom methods
	///////////////////////////////////////////////////////////////////////////////////////

}
