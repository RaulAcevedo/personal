package com.jjkeller.kmbapi.controller.dataaccess.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.jjkeller.kmbapi.configuration.LoginCredentials;
import com.jjkeller.kmbapi.controller.dataaccess.AbstractDBAdapter;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.controller.utility.DateUtility;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class UserPersist<T extends LoginCredentials> extends AbstractDBAdapter<T> {

	///////////////////////////////////////////////////////////////////////////////////////
	// private members
	///////////////////////////////////////////////////////////////////////////////////////
	private static final String EMPLOYEEID = "EmployeeId";
	private static final String COMPANYKEY = "CompanyKey";
	private static final String EMPLOYEECODE = "EmployeeCode";
	private static final String FULLNAME = "FullName";
	private static final String USERNAME = "UserName";
	private static final String PASSWORD = "Password";
	private static final String LASTLOGINTIMESTAMP = "LastLoginTimestamp";
	private static final String LASTLOGOUTTIMESTAMP = "LastLogoutTimestamp";
	private static final String LASTSUBMITTIMESTAMP = "LastSubmitTimestamp";
	private static final String HOMETERMINALDOTNUMBER = "HomeTerminalDOTNumber";
	private static final String HOMETERMINALADDRESSLINE1 = "HomeTerminalAddressLine1";
	private static final String HOMETERMINALADDRESSLINE2 = "HomeTerminalAddressLine2";
	private static final String HOMETERMINALCITY = "HomeTerminalCity";
	private static final String HOMETERMINALSTATEABBREV = "HomeTerminalStateAbbrev";
	private static final String HOMETERMINALZIPCODE = "HomeTerminalZipCode";
	private static final String DRIVERLICENSESTATE = "DriverLicenseState";
	private static final String DRIVERLICENSENUMBER = "DriverLicenseNumber";
	private static final String FIRSTNAME = "FirstName";
	private static final String LASTNAME = "LastName";
	
	private static final String SQL_SELECT_PRIMARYKEY_COMMAND = "select [Key] from [User] where EmployeeId=?";
	
    private static final String SQL_SELECT_COMMAND = "select * from [User] where UserName=?";
    
    private String _userName;

	///////////////////////////////////////////////////////////////////////////////////////
	// constructors
	///////////////////////////////////////////////////////////////////////////////////////
	public UserPersist(Class<T> clazz, Context ctx, User user)
	{
		super(clazz, ctx, user);
		
		setDbTableName(DB_TABLE_USER);
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
		return new String[]{data.getEmployeeId().toString()};
	}
	
	@Override
	protected String getSelectCommand() {
		return SQL_SELECT_COMMAND;
	}
	
	@Override
	protected String[] getSelectArgs() {
		return new String[]{_userName};
	}
	
	@Override
	protected T BuildObject(Cursor cursorData)
	{
		T data = super.BuildObject(cursorData);
		
		SimpleDateFormat sqlDateTimeFormat = DateUtility.getHomeTerminalSqlDateTimeFormat();
		sqlDateTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

		data.setEmployeeId(ReadValue(cursorData, EMPLOYEEID, "00000000-0000-0000-0000-000000000000"));
		data.setEmployeeCode(ReadValue(cursorData, EMPLOYEECODE, (String)null));
		data.setEmployeeFullName(ReadValue(cursorData, FULLNAME, (String)null));
		data.setUsername(ReadValue(cursorData, USERNAME, (String)null));
		data.setPassword(ReadValue(cursorData, PASSWORD, (String)null));					
		data.setLastLoginTimestampUtc(ReadValue(cursorData, LASTLOGINTIMESTAMP, (Date) null, sqlDateTimeFormat));
		data.setLastLogoutTimestampUtc(ReadValue(cursorData, LASTLOGOUTTIMESTAMP, (Date) null, sqlDateTimeFormat));
		data.setLastSubmitTimestampUtc(ReadValue(cursorData, LASTSUBMITTIMESTAMP, (Date) null, sqlDateTimeFormat));
		data.setHomeTerminalDOTNumber(ReadValue(cursorData, HOMETERMINALDOTNUMBER, (String)null));
		data.setHomeTerminalAddressLine1(ReadValue(cursorData, HOMETERMINALADDRESSLINE1, (String)null));
		data.setHomeTerminalAddressLine2(ReadValue(cursorData, HOMETERMINALADDRESSLINE2, (String)null));
		data.setHomeTerminalCity(ReadValue(cursorData, HOMETERMINALCITY, (String)null));
		data.setHomeTerminalStateAbbrev(ReadValue(cursorData, HOMETERMINALSTATEABBREV, (String)null));
		data.setHomeTerminalZipCode(ReadValue(cursorData, HOMETERMINALZIPCODE, (String)null));
		data.setLastName(ReadValue(cursorData, LASTNAME, (String)null));
		data.setFirstName(ReadValue(cursorData, FIRSTNAME, (String)null));
		data.setDriverLicenseState(ReadValue(cursorData, DRIVERLICENSESTATE, (String)null));
		data.setDriverLicenseNumber(ReadValue(cursorData, DRIVERLICENSENUMBER, (String)null));
		
		return data;
	}

	@Override
	public ContentValues PersistContentValues(T data)
	{				
		ContentValues content = super.PersistContentValues(data);
		
		SimpleDateFormat sqlDateTimeFormat = DateUtility.getHomeTerminalSqlDateTimeFormat();
		sqlDateTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

		PutValue(content, EMPLOYEEID, data.getEmployeeId());
		PutValue(content, COMPANYKEY, this.getCurrentUser().getCompanyKey());
		PutValue(content, EMPLOYEECODE, data.getEmployeeCode());
		PutValue(content, FULLNAME, data.getEmployeeFullName());
		PutValue(content, USERNAME, data.getUsername());
		PutValue(content, PASSWORD, data.getPassword());
		PutValue(content, HOMETERMINALDOTNUMBER, data.getHomeTerminalDOTNumber());
		PutValue(content, HOMETERMINALADDRESSLINE1, data.getHomeTerminalAddressLine1());
		PutValue(content, HOMETERMINALADDRESSLINE2, data.getHomeTerminalAddressLine2());
		PutValue(content, HOMETERMINALCITY, data.getHomeTerminalCity());
		PutValue(content, HOMETERMINALSTATEABBREV, data.getHomeTerminalStateAbbrev());
		PutValue(content, HOMETERMINALZIPCODE, data.getHomeTerminalZipCode());
		//PutValue(content, LASTLOGINTIMESTAMP, data.getLastLoginTimestampUtc(), DateUtility.DateTimeFormat);
		PutValue(content, LASTLOGINTIMESTAMP, data.getLastLoginTimestampUtc(), sqlDateTimeFormat);
		PutValue(content, DRIVERLICENSENUMBER, data.getDriverLicenseNumber());
		PutValue(content, DRIVERLICENSESTATE, data.getDriverLicenseState());
		PutValue(content, FIRSTNAME, data.getFirstName());
		PutValue(content, LASTNAME, data.getLastName());
		
		if (data.getLastLogoutTimestampUtc() != null)
			//PutValue(content, LASTLOGOUTTIMESTAMP, data.getLastLogoutTimestampUtc(), DateUtility.DateTimeFormat);
			PutValue(content, LASTLOGOUTTIMESTAMP, data.getLastLogoutTimestampUtc(), sqlDateTimeFormat);

		if (data.getLastSubmitTimestampUtc() != null)
			//PutValue(content, LASTSUBMITTIMESTAMP, data.getLastSubmitTimestampUtc(), DateUtility.DateTimeFormat);
			PutValue(content, LASTSUBMITTIMESTAMP, data.getLastSubmitTimestampUtc(), sqlDateTimeFormat);

		return content;
	}
	
	///////////////////////////////////////////////////////////////////////////////////////
	// custom methods
	///////////////////////////////////////////////////////////////////////////////////////
	public T Fetch(String userName)
	{
		_userName = userName;
		return Fetch();
	}
	
	// TODO Commented code out - should be removed before going to production
/*
 	public void LoadSampleData()
 
	{
		this.loadSampleData();
	}
*/
}
