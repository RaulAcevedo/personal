package com.jjkeller.kmbapi.controller.dataaccess.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.jjkeller.kmbapi.common.TimeKeeper;
import com.jjkeller.kmbapi.controller.dataaccess.AbstractDBAdapter;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.proxydata.LogRemarkItem;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

public class LogRemarkItemPersist <T extends LogRemarkItem> extends AbstractDBAdapter<T> {

	///////////////////////////////////////////////////////////////////////////////////////
	// private members
	///////////////////////////////////////////////////////////////////////////////////////
	
	private static final String NAME= "Name";
	private static final String ITEMENUM = "ItemEnum";
	private static final String LKUPLOGREMARKID = "LkupLogRemarkId";
	private static final String ISACTIVE = "IsActive";
	private static final String CHANGEDATE = "ChangeDate";
	
	private static final String SQL_SELECT_PRIMARYKEY_COMMAND = "select [Key] from [LogRemarkItem] where LkupLogRemarkId=?";
	private static final String SQL_SELECT_COMMAND = "select * from [LogRemarkItem]";
	private static final String SQL_SELECT_FETCHALLACTIVEONLY_COMMAND = "select * from [LogRemarkItem] where IsActive=1";	
	private static final String SQL_SELECT_MOSTRECENTCHANGEDATE_COMMAND = "SELECT ChangeDate FROM [LogRemarkItem] ORDER BY ChangeDate DESC LIMIT 1 ";

	
    ///////////////////////////////////////////////////////////////////////////////////////
	// constructors
	///////////////////////////////////////////////////////////////////////////////////////

	public LogRemarkItemPersist(Class<T> clazz, Context ctx) {
		super(clazz, ctx);

		setDbTableName(DB_TABLE_LOGREMARKITEM);
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
		return new String[]{ data.getLkupLogRemarkId() };
	}
	
	@Override
	protected String getSelectCommand()
	{
		return SQL_SELECT_COMMAND;
	}
	
	protected ContentValues PersistContentValues(T data)
	{
		ContentValues content = super.PersistContentValues(data);
		
		PutValue(content, NAME, data.getName());
		PutValue(content, ITEMENUM, data.getItemEnum());
		PutValue(content, LKUPLOGREMARKID, data.getLkupLogRemarkId());
		PutValue(content, ISACTIVE, data.getIsActive());
		PutValue(content, CHANGEDATE, TimeKeeper.getInstance().now(), DateUtility.getHomeTerminalSqlDateTimeFormat());
		
		return content;
	}
	
	protected T BuildObject(Cursor cursorData)
	{
		T data = super.BuildObject(cursorData);

        data.setName(ReadValue(cursorData, NAME, (String)null));
        data.setItemEnum(ReadValue(cursorData, ITEMENUM, -1));
        data.setIsActive(ReadValue(cursorData, ISACTIVE, true));
        data.setLkupLogRemarkId(ReadValue(cursorData, LKUPLOGREMARKID, (String)null));
        
		return data;
	}
	
	public void Save(List<T> logRemarkItems){
		for(LogRemarkItem item : logRemarkItems){
			this.Persist((T)item);
		}
		
//		LogRemarkItem[] items = new LogRemarkItem[logRemarkItems.size()];
//		logRemarkItems.toArray(items);
//		
//		this.Persist((T[])items);
	}
	
    /// <summary>
    /// Fetch all active lookup items
    /// </summary>
    /// <returns>EventDataRecord</returns>
    public List<T> FetchAllActive()
    {
    	List<T> list = ExecuteFetchListRawQuery(SQL_SELECT_FETCHALLACTIVEONLY_COMMAND, null);
		return list;
    }
	

    public Date GetMostRecentChangeDate()
    {
    	Date changeDate = null;
		String sql = SQL_SELECT_MOSTRECENTCHANGEDATE_COMMAND;
	
    	this.open();	
    	Cursor cursor = this.ExecuteRawQuery(sql, null);

		if (cursor != null && cursor.moveToFirst())
		{
			while(!cursor.isAfterLast())
			{
				String changeDateString = cursor.getString(0);
				try {
					changeDate = DateUtility.getHomeTerminalSqlDateTimeFormat().parse(changeDateString);
				} catch (ParseException ex) {
					
		        	Log.e("UnhandledCatch", ex.getMessage() + ": " + Log.getStackTraceString(ex));
				}
				
				cursor.moveToNext();
			}
		}	

		cursor.close();
		this.close();
		
		return changeDate;

    }
}
