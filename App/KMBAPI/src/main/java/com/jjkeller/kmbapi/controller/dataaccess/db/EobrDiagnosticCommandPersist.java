package com.jjkeller.kmbapi.controller.dataaccess.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.jjkeller.kmbapi.controller.dataaccess.AbstractDBAdapter;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.proxydata.EobrDiagnosticCommand;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class EobrDiagnosticCommandPersist<T extends EobrDiagnosticCommand>  extends AbstractDBAdapter<T> {

	///////////////////////////////////////////////////////////////////////////////////////
	// private members
	///////////////////////////////////////////////////////////////////////////////////////
	private static final String COMMANDID = "DmoCommandId";
    private static final String SERIALNUMBER = "SerialNumber";
	private static final String COMMAND = "Command";
    private static final String RESPONSETIMESTAMP = "ResponseTimestamp";
    private static final String RESPONSE = "Response";

    private static final String SQL_SELECT_COMMAND = "select * from [EobrDiagnosticCommand]";
    private final String SQL_SELECT_PRIMARYKEY_COMMAND = "select [Key] from [EobrDiagnosticCommand] where DmoCommandId=?";
    private final String SQL_PURGE_COMMAND = "delete from [EobrDiagnosticCommand] where Key = ?";
    private static final String SQL_SELECT_PENDING_COMMAND = "select * from [EobrDiagnosticCommand] where SerialNumber=? and ResponseTimestamp is null";
    private static final String SQL_SELECT_COMPLETED_COMMAND = "select * from [EobrDiagnosticCommand] where ResponseTimestamp is not null";
    
	///////////////////////////////////////////////////////////////////////////////////////
	// constructors
	///////////////////////////////////////////////////////////////////////////////////////
    public EobrDiagnosticCommandPersist(Class<T> clazz, Context ctx)
    {
		super(clazz, ctx);
		
		setDbTableName(DB_TABLE_EOBRDIAGNOSTICCOMMAND);
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
		return new String[]{data.getDmoCommandId()};
	}

	@Override
	protected T BuildObject(Cursor cursorData)
	{
		T data = super.BuildObject(cursorData);

		SimpleDateFormat sqlDateTimeFormat = DateUtility.getHomeTerminalSqlDateTimeFormat();
		sqlDateTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

		data.setDmoCommandId(ReadValue(cursorData, COMMANDID, (String)null));
		data.setSerialNumber(ReadValue(cursorData, SERIALNUMBER, (String)null));
		data.setCommand(ReadValue(cursorData, COMMAND, (String)null));	
		data.setResponseTimestamp(ReadValue(cursorData, RESPONSETIMESTAMP, (Date)null, sqlDateTimeFormat));	
		data.setRespnose(ReadValue(cursorData, RESPONSE, (String)null));		
		return data;
	}
	
	@Override
	protected String getSelectCommand()
	{
		return SQL_SELECT_COMMAND;
	}

	@Override
	protected ContentValues PersistContentValues(T data)
	{
		ContentValues content = super.PersistContentValues(data);

		SimpleDateFormat sqlDateTimeFormat = DateUtility.getHomeTerminalSqlDateTimeFormat();
		sqlDateTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		PutValue(content, COMMANDID, data.getDmoCommandId());
		PutValue(content, SERIALNUMBER, data.getSerialNumber());
		PutValue(content, COMMAND, data.getCommand());
		PutValue(content, RESPONSETIMESTAMP, data.getResponseTimestamp(), sqlDateTimeFormat);
		PutValue(content, RESPONSE, data.getRespnose());

		return content;
	}
	
	public void Save(List<T> eobrDiagnosticCommands){
		for(EobrDiagnosticCommand item : eobrDiagnosticCommands){
			this.Persist((T)item);
		}
	}

	///////////////////////////////////////////////////////////////////////////////////////
	// custom methods
	///////////////////////////////////////////////////////////////////////////////////////
	
    /// <summary>
    /// Purge record 
    /// </summary>
    /// <param name="key"></param>
    public void PurgeCommand(long key)
    {
    	String sql = String.format(SQL_PURGE_COMMAND);
		ExecuteQuery(sql, new String[]{String.valueOf(key)});
    }
    
    /// <summary>
    /// Fetch pending diagnostic command.
    /// </summary>
    /// <param name="eobrSerialNumber"></param>
    /// <returns>EobrDiagnosticCommand</returns>
    public List<T> FetchAllPendingCommands(String eobrSerialNumber)
    {
    	String sql = String.format(SQL_SELECT_PENDING_COMMAND);
    	List<T> EobrDiagnosticCommand = ExecuteFetchListRawQuery(sql, new String[]{eobrSerialNumber});
		return EobrDiagnosticCommand;

    }
    
    /// <summary>
    /// Fetch completed diagnostic command.
    /// </summary>
    /// <returns>EobrDiagnosticCommand</returns>
    public List<T> FetchCompletedCommands()
    {
    	String sql = String.format(SQL_SELECT_COMPLETED_COMMAND);
    	List<T> EobrDiagnosticCommand = ExecuteFetchListRawQuery(sql, null);
		return EobrDiagnosticCommand;

    }   
}
