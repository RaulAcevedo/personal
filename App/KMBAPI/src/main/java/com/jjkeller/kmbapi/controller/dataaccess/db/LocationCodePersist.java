package com.jjkeller.kmbapi.controller.dataaccess.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.dataaccess.AbstractDBAdapter;
import com.jjkeller.kmbapi.proxydata.LocationCode;

public class LocationCodePersist<T extends LocationCode> extends AbstractDBAdapter<T> {

	///////////////////////////////////////////////////////////////////////////////////////
	// private members
	///////////////////////////////////////////////////////////////////////////////////////
	private static final String COMPANYKEY = "CompanyKey";
	private static final String LOCATION = "Location";
	private static final String CODE = "Code";

	private static final String SQL_SELECT_PRIMARYKEY_COMMAND = "select [Key] from LocationCode where Code=? and CompanyKey=?";

	private static final String SQL_SELECT_COMMAND = "select * from LocationCode where CompanyKey=?";
	
	///////////////////////////////////////////////////////////////////////////////////////
	// constructors
	///////////////////////////////////////////////////////////////////////////////////////
	public LocationCodePersist (Class<T> clazz, Context ctx)
	{
		super(clazz, ctx, GlobalState.getInstance().getCurrentUser());
		
		setDbTableName(DB_TABLE_LOCATIONCODE);
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
		return new String[]{data.getCode(), String.valueOf(this.getCurrentUser().getCompanyKey())};
	}

	@Override
	protected String getSelectCommand()
	{
		return SQL_SELECT_COMMAND;
	}
	
	@Override
	protected String[] getSelectArgs()
	{
		return new String[]{Long.toString(this.getCurrentUser().getCompanyKey())};
	}
	
	@Override
	protected T BuildObject(Cursor cursorData)
	{
		T data = super.BuildObject(cursorData);

		data.setCompanyKey(ReadValue(cursorData, COMPANYKEY, (int)0));
		data.setLocation(ReadValue(cursorData, LOCATION, (String)null));
		data.setCode(ReadValue(cursorData, CODE, (String)null));

		return data;
	}

	@Override
	public ContentValues PersistContentValues(T data)
	{		
		ContentValues content = super.PersistContentValues(data);
		
		PutValue(content, COMPANYKEY, this.getCurrentUser().getCompanyKey());
		PutValue(content, LOCATION, data.getLocation());
		PutValue(content, CODE, data.getCode());

		return content;
	}
	
	///////////////////////////////////////////////////////////////////////////////////////
	// custom methods
	///////////////////////////////////////////////////////////////////////////////////////
}
