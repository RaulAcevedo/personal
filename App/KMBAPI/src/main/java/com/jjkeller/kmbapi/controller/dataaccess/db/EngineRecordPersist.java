package com.jjkeller.kmbapi.controller.dataaccess.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.jjkeller.kmbapi.controller.dataaccess.AbstractDBAdapter;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.enums.EngineRecordTypeEnum;
import com.jjkeller.kmbapi.proxydata.EngineRecord;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class EngineRecordPersist<T extends EngineRecord>  extends AbstractDBAdapter<T> {

	///////////////////////////////////////////////////////////////////////////////////////
	// private members
	///////////////////////////////////////////////////////////////////////////////////////
    private static final String EOBRTIMESTAMP = "EobrTimestamp";
    private static final String GPSTIMESTAMP = "GpsTimestamp";
    private static final String GPSLATITUDE = "GpsLatitude";
    private static final String GPSLONGITUDE = "GpsLongitude";
    private static final String SPEEDOMETER = "Speedometer";
    private static final String ODOMETER = "Odometer";
    private static final String TACHOMETER = "Tachometer";
    private static final String EOBROVERALLSTATUS = "EobrOverallStatus";
    private static final String RECORDTYPE = "RecordType";
    private static final String FUELECONOMYINSTANT = "FuelEconomyInstant";
    private static final String FUELECONOMYAVERAGE = "FuelEconomyAverage";
    private static final String CRUISECONTROLSET = "CruiseControlSet";
    private static final String FUELUSETOTAL = "FuelUseTotal";
    private static final String BRAKEPRESSURE = "BrakePressure";
    private static final String TRANSMISSIONSELECTED = "TransmissionSelected";
    private static final String TRANSMISSIONATTAINED = "TransmissionAttained";
    private static final String EOBRSERIALNUMBER = "EobrSerialNumber";
    private static final String EOBRTRACTORNUMBER = "EobrTractorNumber";
    private static final String DRIVEREMPLOYEEID = "DriverEmployeeId";
    private static final String ISSUBMITTED = "IsSubmitted";

    private final String SQL_SELECT_PRIMARYKEY_COMMAND = "select [Key] from [EngineRecord] where EobrSerialNumber=? and EobrTimestamp=?";
    private final String SQL_PURGE_COMMAND = "delete from EngineRecord where EobrTimestamp < ? AND IsSubmitted=1";
    private static final String SQL_SELECT_MOSTRECENT_COMMAND = "select * from [EngineRecord] where EobrSerialNumber=? order by EobrTimeStamp desc limit 1";
    private static final String SQL_SELECT_UNSUBMITTED_LIMITED_COMMAND = "select * from [EngineRecord] where IsSubmitted=0 limit ?";
    
	///////////////////////////////////////////////////////////////////////////////////////
	// constructors
	///////////////////////////////////////////////////////////////////////////////////////
    public EngineRecordPersist(Class<T> clazz, Context ctx)
    {
		super(clazz, ctx);
		
		setDbTableName(DB_TABLE_ENGINERECORD);
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
		if(data.getEobrTimestamp() == null)
			args = new String[]{data.getEobrSerialNumber(), ""};
		else
		{
			SimpleDateFormat sqlDateTimeFormat = DateUtility.getHomeTerminalSqlDateTimeFormat();
			sqlDateTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

			args = new String[]{data.getEobrSerialNumber(), sqlDateTimeFormat.format(data.getEobrTimestamp())};			
		}
		
		return args;
	}

	@Override
	protected T BuildObject(Cursor cursorData)
	{
		T data = super.BuildObject(cursorData);

		SimpleDateFormat sqlDateTimeFormat = DateUtility.getHomeTerminalSqlDateTimeFormat();
		sqlDateTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

		data.setEobrSerialNumber(ReadValue(cursorData, EOBRSERIALNUMBER, (String)null));
		data.setEobrTractorNumber(ReadValue(cursorData, EOBRTRACTORNUMBER, (String)null));
		data.setEobrTimestamp(ReadValue(cursorData, EOBRTIMESTAMP, (Date)null, sqlDateTimeFormat));
		data.setDriverEmployeeId(ReadValue(cursorData, DRIVEREMPLOYEEID, (String)null));
		data.setSpeedometer(ReadValue(cursorData, SPEEDOMETER, (float)0));
		data.setOdometer(ReadValue(cursorData, ODOMETER, (float)0));
		data.setTachometer(ReadValue(cursorData, TACHOMETER, (float)0));
		data.setGpsTimestamp(ReadValue(cursorData, GPSTIMESTAMP, (Date)null, sqlDateTimeFormat));
		data.setGpsLatitude(ReadValue(cursorData, GPSLATITUDE, (float)0));
		data.setGpsLongitude(ReadValue(cursorData, GPSLONGITUDE, (float)0));
		data.setEobrOverallStatus(ReadValue(cursorData, EOBROVERALLSTATUS, (int)0));
		data.getRecordType().setValue(ReadValue(cursorData, RECORDTYPE, EngineRecordTypeEnum.NULL));
		data.setFuelEconomyAverage(ReadValue(cursorData, FUELECONOMYAVERAGE, (float)0));
		data.setFuelEconomyInstant(ReadValue(cursorData, FUELECONOMYINSTANT, (float)0));
		data.setFuelUseTotal(ReadValue(cursorData, FUELUSETOTAL, (float)0));
		data.setCruiseControlSet(ReadValue(cursorData, CRUISECONTROLSET, true));
		data.setTransmissionSelected(ReadValue(cursorData, TRANSMISSIONSELECTED, (String)null));
		data.setTransmissionAttained(ReadValue(cursorData, TRANSMISSIONATTAINED, (String)null));
		data.setBrakePressure(ReadValue(cursorData, BRAKEPRESSURE, (float)0));
	    
		return data;
	}

	@Override
	protected ContentValues PersistContentValues(T data)
	{
		ContentValues content = super.PersistContentValues(data);

		SimpleDateFormat sqlDateTimeFormat = DateUtility.getHomeTerminalSqlDateTimeFormat();
		sqlDateTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		PutValue(content, EOBRSERIALNUMBER, data.getEobrSerialNumber());
		PutValue(content, EOBRTRACTORNUMBER, data.getEobrTractorNumber());
		PutValue(content, EOBRTIMESTAMP, data.getEobrTimestamp(), sqlDateTimeFormat);
		PutValue(content, DRIVEREMPLOYEEID, data.getDriverEmployeeId());
		PutValue(content, SPEEDOMETER, data.getSpeedometer());
		PutValue(content, ODOMETER, data.getOdometer());
		PutValue(content, TACHOMETER, data.getTachometer());
		PutValue(content, GPSTIMESTAMP, data.getGpsTimestamp(), sqlDateTimeFormat);
		PutValue(content, GPSLATITUDE, data.getGpsLatitude());
		PutValue(content, GPSLONGITUDE, data.getGpsLongitude());
		PutValue(content, EOBROVERALLSTATUS, data.getEobrOverallStatus());
		PutValue(content, RECORDTYPE, data.getRecordType().getValue());
		
		// 11/29/11 JHM Set large outlying values to -1
		if(Math.abs(data.getFuelEconomyAverage()) >= 10000000F)
			PutValue(content, FUELECONOMYAVERAGE, -1F);
		else
			PutValue(content, FUELECONOMYAVERAGE, data.getFuelEconomyAverage());
		
		if(Math.abs(data.getFuelEconomyInstant()) >= 10000000F)
			PutValue(content, FUELECONOMYINSTANT, -1F);
		else
			PutValue(content, FUELECONOMYINSTANT, data.getFuelEconomyInstant());
		
		if(Math.abs(data.getFuelUseTotal()) >= 10000000F)
			PutValue(content, FUELUSETOTAL, -1F);
		else
			PutValue(content, FUELUSETOTAL, data.getFuelUseTotal());
		
		PutValue(content, CRUISECONTROLSET, data.getCruiseControlSet());
		PutValue(content, TRANSMISSIONSELECTED, data.getTransmissionSelected());
		PutValue(content, TRANSMISSIONATTAINED, data.getTransmissionAttained());
		
		if(Math.abs(data.getBrakePressure()) >= 10000000F)
			PutValue(content, BRAKEPRESSURE, -1F);
		else
			PutValue(content, BRAKEPRESSURE, data.getBrakePressure());
		
		PutValue(content, ISSUBMITTED, 0);

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
		SimpleDateFormat sqlDateTimeFormat = DateUtility.getHomeTerminalSqlDateTimeFormat();
		sqlDateTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

    	String sql = SQL_PURGE_COMMAND;
		String[] selectionArgs =  new String[]{sqlDateTimeFormat.format(cutoffDate)};

		ExecuteQuery(sql, selectionArgs);
    }
    
    /// <summary>
    /// Fetch most recent engine record.
    /// </summary>
    /// <returns>EngineRecord</returns>
    public EngineRecord FetchMostRecentEngineRecord(String eobrSerialNumber)
    {
		EngineRecord engineRecord = ExecuteFetchRawQuery(SQL_SELECT_MOSTRECENT_COMMAND, new String[]{eobrSerialNumber});
		return engineRecord;

    }
    
    /// <summary>
    /// Fetch most recent engine record.
    /// </summary>
    /// <returns>EngineRecord</returns>
    public List<T> FetchUnsubmittedLimited(int numberOfRecordsToFetch)
    {
    	String[] selectionArgs = {String.valueOf(numberOfRecordsToFetch)};
		List<T> engineRecords = ExecuteFetchListRawQuery(SQL_SELECT_UNSUBMITTED_LIMITED_COMMAND, selectionArgs);
		return engineRecords;

    }   
}
