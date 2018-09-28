package com.jjkeller.kmbapi.controller.dataaccess.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.dataaccess.AbstractDBAdapter;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.kmbeobr.EventTypeEnum;
import com.jjkeller.kmbapi.proxydata.EventDataRecord;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class EventDataRecordPersist<T extends EventDataRecord>  extends AbstractDBAdapter<T> {

	///////////////////////////////////////////////////////////////////////////////////////
	// private members
	///////////////////////////////////////////////////////////////////////////////////////
    private static final String DRIVEREMPLOYEEID = "DriverEmployeeId";
    private static final String EOBRSERIALNUMBER = "EobrSerialNumber";
	private static final String EOBRTIMESTAMP = "EobrTimestamp";
    private static final String EVENTTYPE = "EventType";
    private static final String EVENTDATA = "EventData";
    private static final String ODOMETER = "Odometer";
    private static final String GPSLATITUDE = "GpsLatitude";
    private static final String GPSLONGITUDE = "GpsLongitude";
    private static final String ISSUBMITTED = "IsSubmitted";
    private static final String SPEEDOMETER = "Speedometer";
    private static final String TACHOMETER = "Tachometer";

    private final String SQL_SELECT_PRIMARYKEY_COMMAND = "select [Key] from [EventDataRecord] where EobrSerialNumber=? and EobrTimestamp=?";
    private final String SQL_PURGE_COMMAND = "delete from EventDataRecord where EobrTimestamp < ? AND IsSubmitted=1";
    private static final String SQL_SELECT_MOSTRECENT_COMMAND = "select * from [EventDataRecord] where EobrSerialNumber=? order by EobrTimeStamp desc limit 1";
    private static final String SQL_SELECT_MOSTRECENTOFALL_COMMAND = "select * from [EventDataRecord] order by EobrTimeStamp desc limit 1";
    private static final String SQL_SELECT_UNSUBMITTED_LIMITED_COMMAND = "select * from [EventDataRecord] where IsSubmitted=0 limit ?";
    
	///////////////////////////////////////////////////////////////////////////////////////
	// constructors
	///////////////////////////////////////////////////////////////////////////////////////
    public EventDataRecordPersist(Class<T> clazz, Context ctx)
    {
		super(clazz, ctx);
		
		setDbTableName(DB_TABLE_EVENTDATARECORD);
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
		int eventType = data.getEventType();
		if(data.getEobrTimestamp() == null)
			args = new String[]{Integer.toString(eventType), ""};
		else
		{
			SimpleDateFormat sqlDateTimeFormat = getSimpleDateFormat();

			args = new String[]{Integer.toString(eventType), sqlDateTimeFormat.format(data.getEobrTimestamp())};			
		}
		
		return args;
	}

	private SimpleDateFormat getSimpleDateFormat() {
		SimpleDateFormat sqlDateTimeFormat;
		if(GlobalState.getInstance().getCompanyConfigSettings(getContext()).getIsGeotabEnabled()){
            sqlDateTimeFormat = DateUtility.getSqlDateTimeFormatMS();
        }else{
            sqlDateTimeFormat = DateUtility.getHomeTerminalSqlDateTimeFormat();
        }
		sqlDateTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		return sqlDateTimeFormat;
	}

	@Override
	protected T BuildObject(Cursor cursorData)
	{
		T data = super.BuildObject(cursorData);

		SimpleDateFormat sqlDateTimeFormat = getSimpleDateFormat();

		data.setDriverEmployeeId(ReadValue(cursorData, DRIVEREMPLOYEEID, (String)null));
		data.setEobrSerialNumber(ReadValue(cursorData, EOBRSERIALNUMBER, (String)null));
		data.setEobrTimestamp(ReadValue(cursorData, EOBRTIMESTAMP, (Date)null, sqlDateTimeFormat));		
		data.setEventType((ReadValue(cursorData, EVENTTYPE, EventTypeEnum.ANYTYPE)));
		data.setEventData(ReadValue(cursorData, EVENTDATA, (int)0));
		data.setOdometer(ReadValue(cursorData, ODOMETER, (float)0));
		data.setGpsLatitude(ReadValue(cursorData, GPSLATITUDE, Float.NaN));
		data.setGpsLongitude(ReadValue(cursorData, GPSLONGITUDE, Float.NaN));
		data.setSpeedometer(ReadValue(cursorData, SPEEDOMETER, (float)0));
		data.setTachometer(ReadValue(cursorData, TACHOMETER,  (float)0));
	    
		return data;
	}

	@Override
	protected ContentValues PersistContentValues(T data)
	{
		ContentValues content = super.PersistContentValues(data);

		SimpleDateFormat sqlDateTimeFormat = getSimpleDateFormat();
		
		PutValue(content, DRIVEREMPLOYEEID, data.getDriverEmployeeId());
		PutValue(content, EOBRSERIALNUMBER, data.getEobrSerialNumber());
		PutValue(content, EOBRTIMESTAMP, data.getEobrTimestamp(), sqlDateTimeFormat);
		PutValue(content, EVENTTYPE, data.getEventType());
		PutValue(content, EVENTDATA, data.getEventData());
		PutValue(content, ODOMETER, data.getOdometer());
		PutValue(content, GPSLATITUDE, data.getGpsLatitude());
		PutValue(content, GPSLONGITUDE, data.getGpsLongitude());
		PutValue(content, ISSUBMITTED, 0);
		PutValue(content, SPEEDOMETER, data.getSpeedometer());
		PutValue(content, TACHOMETER, data.getTachometer());

		return content;
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
		SimpleDateFormat sqlDateTimeFormat = getSimpleDateFormat();

    	String sql = SQL_PURGE_COMMAND;
		String[] selectionArgs =  new String[]{sqlDateTimeFormat.format(cutoffDate)};

		ExecuteQuery(sql, selectionArgs);
    }
    
    /// <summary>
    /// Fetch most recent event data.
    /// </summary>
    /// <returns>EventDataRecord</returns>
    public EventDataRecord FetchMostRecentEventRecord(String eobrSerialNumber)
    {
    	EventDataRecord eventDataRecord = ExecuteFetchRawQuery(SQL_SELECT_MOSTRECENT_COMMAND, new String[]{eobrSerialNumber});
		return eventDataRecord;

    }
    
    /// <summary>
    /// Fetch most recent event data from all available history.
    /// </summary>
    /// <returns>EventDataRecord</returns>
    public EventDataRecord FetchMostRecentEventRecord()
    {
    	EventDataRecord eventDataRecord = ExecuteFetchRawQuery(SQL_SELECT_MOSTRECENTOFALL_COMMAND, null);
		return eventDataRecord;

    }
    
    /// <summary>
    /// Fetch most recent event data.
    /// </summary>
    /// <returns>EventDataRecord</returns>
    public List<T> FetchUnsubmittedLimited(int numberOfRecordsToFetch)
    {
		String[] selectionArgs = {String.valueOf(numberOfRecordsToFetch)};
		List<T> eventDataRecord = ExecuteFetchListRawQuery(SQL_SELECT_UNSUBMITTED_LIMITED_COMMAND, selectionArgs);
		return eventDataRecord;

    }
}
