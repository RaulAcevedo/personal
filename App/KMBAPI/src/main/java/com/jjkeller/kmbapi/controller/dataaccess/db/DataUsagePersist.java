package com.jjkeller.kmbapi.controller.dataaccess.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.jjkeller.kmbapi.controller.dataaccess.AbstractDBAdapter;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.controller.utility.DeviceInfo;
import com.jjkeller.kmbapi.proxydata.DataUsage;

import java.util.Date;

public class DataUsagePersist<T extends DataUsage> extends AbstractDBAdapter<T> {

	///////////////////////////////////////////////////////////////////////////////////////
	// private members
	///////////////////////////////////////////////////////////////////////////////////////
	private static final String DEVICEID = "DeviceId";
	private static final String USAGEDATE = "UsageDate";
	private static final String TRANSMITTEDBYTES = "TransmittedBytes";
	private static final String RECEIVEDBYTES = "ReceivedBytes";
	private static final String NETWORKENUM = "NetworkEnum";
	private static final String URI = "Uri";
    private static final String ISSUBMITTED = "IsSubmitted";
	
	private static final String SQL_SELECT_PRIMARYKEY_COMMAND = "select Key from DataUsage where DeviceId=? and UsageDate=? and Uri=?";
	private static final String SQL_UPDATEUSAGE_COMMAND = "Update DataUsage SET TransmittedBytes = TransmittedBytes + %d, ReceivedBytes = ReceivedBytes + %d, IsSubmitted = 0 WHERE DeviceId=? and UsageDate=? and Uri=?";
	private static final String SQL_CLEARUSAGE_COMMAND = "Delete From DataUsage";
	
	///////////////////////////////////////////////////////////////////////////////////////
	// constructors
	///////////////////////////////////////////////////////////////////////////////////////
	public DataUsagePersist(Class<T> clazz, Context ctx)
	{
		super(clazz, ctx);
		
		setDbTableName(DB_TABLE_DATAUSAGE);
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
		return new String[]{"DeviceId", DateUtility.getHomeTerminalSqlDateFormat().format(data.getUsageDate()), data.getUri() };
	}
	
	@Override
	protected T BuildObject(Cursor cursorData)
	{
		T data = super.BuildObject(cursorData);

		data.setDeviceId(ReadValue(cursorData, DEVICEID, (String)null));
		data.setUsageDate(ReadValue(cursorData, USAGEDATE, (Date)null, DateUtility.getHomeTerminalSqlDateFormat()));
		data.setTransmittedBytes(ReadValue(cursorData, TRANSMITTEDBYTES, (long)0));
		data.setReceivedBytes(ReadValue(cursorData, RECEIVEDBYTES, (long)0));
		data.setNetworkEnum(ReadValue(cursorData, NETWORKENUM, (int)0));					
		data.setUri(ReadValue(cursorData, URI, (String)null));

		return (T) data;
	}
	
	@Override
	protected ContentValues PersistContentValues(T data)
	{
		ContentValues content = super.PersistContentValues(data);
		
		PutValue(content, DEVICEID, data.getDeviceId());
		PutValue(content, USAGEDATE, DateUtility.getHomeTerminalSqlDateFormat().format(data.getUsageDate()));
		PutValue(content, TRANSMITTEDBYTES, data.getTransmittedBytes());
		PutValue(content, RECEIVEDBYTES, data.getReceivedBytes());
		PutValue(content, NETWORKENUM, data.getNetworkEnum());
		PutValue(content, URI, data.getUri());
		PutValue(content, ISSUBMITTED, 0);

		return content;
	}
	
	@SuppressWarnings("unchecked")
	public void UpdateUsage(Date usageDate, long transmitted, long received, int networkEnum, String uri)
	{
		String phoneId = DeviceInfo.GetDeviceIdentifier(getContext());
		String today = DateUtility.getHomeTerminalSqlDateFormat().format(usageDate);
		String[] pkArgs = new String[]{phoneId, today, uri };
		Long primaryKey = this.PrimaryKeyExists(pkArgs);
		if (primaryKey != null)
		{
			String sql = String.format(SQL_UPDATEUSAGE_COMMAND, transmitted, received);
			this.ExecuteQuery(sql, pkArgs);
		}
		else
		{
			DataUsage data = new DataUsage();
			data.setDeviceId(phoneId);
			data.setUsageDate(usageDate);
			data.setTransmittedBytes(transmitted);
			data.setReceivedBytes(received);
			//data.setNetworkEnum();			
			data.setUri(uri);
			this.Persist((T)data);
		}
	}
	
	public void ClearUsage()
	{
		this.ExecuteQuery(SQL_CLEARUSAGE_COMMAND, new String[0]);
	}
}
