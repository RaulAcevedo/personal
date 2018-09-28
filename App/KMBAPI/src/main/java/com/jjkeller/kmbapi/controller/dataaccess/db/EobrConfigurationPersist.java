package com.jjkeller.kmbapi.controller.dataaccess.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.jjkeller.kmbapi.controller.dataaccess.AbstractDBAdapter;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.enums.DatabusTypeEnum;
import com.jjkeller.kmbapi.proxydata.EobrConfiguration;

import org.joda.time.DateTime;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class EobrConfigurationPersist<T extends EobrConfiguration> extends AbstractDBAdapter<T>{

	///////////////////////////////////////////////////////////////////////////////////////
	// private members
	///////////////////////////////////////////////////////////////////////////////////////
	private static final String SERIALNUMBER = "SerialNumber";
	private static final String TRACTORNUMBER = "TractorNumber";
	private static final String DATABUSTYPE = "DatabusType";
	private static final String SLEEPMODEMINUTES = "SleepModeMinutes";
	private static final String DATACOLLECTIONRATE = "DataCollectionRate";
	private static final String FIRMWAREVERSION = "FirmwareVersion";
	private static final String MAJORFIRMWAREVERSION = "MajorFirmwareVersion";
	private static final String MINORFIRMWAREVERSION = "MinorFirmwareVersion";
	private static final String PATCHFIRMWAREVERSION = "PatchFirmwareVersion";
	private static final String DISCOVERYPASSKEY = "DiscoveryPasskey";
	private static final String ODOMETERCALIBRATIONDATE = "OdometerCalibrationDate";
	private static final String EOBRODOMETER = "EobrOdometer";
	private static final String DASHBOARDODOMETER = "DashboardOdometer";
	private static final String SPEEDOMETERTHRESHOLD = "SpeedometerThreshold";
	private static final String TACHOMETERTHRESHOLD = "TachometerThreshold";
	private static final String HARDBRAKETHRESHOLD = "HardBrakeThreshold";
	private static final String EOBRGENERATION = "Generation";
	private static final String LASTPOWERCYCLERESETDATE = "LastPowerCycleResetDate";
	private static final String CLOCKSYNCOFFSET = "ClockSyncOffset";
	private static final String CLOCKSYNCDATEUTC = "ClockSyncDateUTC";
	private static final String VIN = "VIN";
	private static final String LASTEVENTREFERENCETIMESTAMP = "LastEventReferenceTimestamp";

    private static final String SQL_SELECT_PRIMARYKEY_COMMAND = "select [Key] from [EobrDevice] where SerialNumber=?";
    private static final String SQL_SELECT_COMMAND = "select * from [EobrDevice] where SerialNumber=?";
    private static final String SQL_SELECT_FETCHALL_COMMAND = "select * from [EobrDevice]";
	private static final String SQL_SELECT_LAST_CONNECTED_EOBRDEVICE = "SELECT dev.* FROM employeelogeldevent AS log INNER JOIN EobrDevice AS dev ON log.EobrSerialNumber = dev.SerialNumber WHERE eobrserialnumber IS NOT NULL AND eventrecordstatus = 1 ORDER BY EventDateTime DESC LIMIT 1";

	private String _eobrSerialNumber;
    
	///////////////////////////////////////////////////////////////////////////////////////
	// constructors
	///////////////////////////////////////////////////////////////////////////////////////
	public EobrConfigurationPersist(Class<T> clazz, Context ctx) {
		super(clazz, ctx);
		
		setDbTableName(DB_TABLE_EOBRDEVICE);
	}
	
	public EobrConfigurationPersist(Class<T> clazz, Context ctx, User user) {
		super(clazz, ctx, user);
		
		setDbTableName(DB_TABLE_EOBRDEVICE);
	}

	public EobrConfigurationPersist(Class<T> clazz, Context ctx, User user, String eobrSerialNumber) {
		super(clazz, ctx, user);
		_eobrSerialNumber = eobrSerialNumber;
		
		setDbTableName(DB_TABLE_EOBRDEVICE);
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
		return new String[]{_eobrSerialNumber};
	}

	@Override
	protected String getSelectCommand()
	{
		return SQL_SELECT_COMMAND;
	}
	
	@Override
	protected String[] getSelectArgs()
	{
		return new String[]{_eobrSerialNumber};
	}
	
	@Override
	protected T BuildObject(Cursor cursorData)
	{
		T data = super.BuildObject(cursorData);

		SimpleDateFormat sqlDateTimeFormat = DateUtility.getSqlDateTimeFormatMS();
		sqlDateTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        data.setSerialNumber(ReadValue(cursorData, SERIALNUMBER, (String)null));
        data.setTractorNumber(ReadValue(cursorData, TRACTORNUMBER, (String)null));
        data.getDatabusType().setValue(ReadValue(cursorData, DATABUSTYPE, DatabusTypeEnum.NULL));
        data.setSleepModeMinutes(ReadValue(cursorData, SLEEPMODEMINUTES, (int)0));
        data.setDataCollectionRate(ReadValue(cursorData, DATACOLLECTIONRATE, (int)0));
        data.setDiscoveryPasskey(ReadValue(cursorData, DISCOVERYPASSKEY, (String)null));
        data.setFirmwareVersion(ReadValue(cursorData, FIRMWAREVERSION, (String)null));
		data.setMajorFirmwareVersion(ReadValue(cursorData, MAJORFIRMWAREVERSION, (int)0));
		data.setMinorFirmwareVersion(ReadValue(cursorData, MINORFIRMWAREVERSION, (int)0));
		data.setPatchFirmwareVersion(ReadValue(cursorData, PATCHFIRMWAREVERSION, (int)0));
		data.setOdometerCalibrationDate(ReadValue(cursorData, ODOMETERCALIBRATIONDATE, (Date)null, DateUtility.getHomeTerminalSqlDateTimeFormat()));
        data.setEobrOdometer(ReadValue(cursorData, EOBRODOMETER, (float)0));
        data.setDashboardOdometer(ReadValue(cursorData, DASHBOARDODOMETER, (float)0));
        data.setSpeedometerThreshold(ReadValue(cursorData, SPEEDOMETERTHRESHOLD, (float)0));
        data.setTachometerThreshold(ReadValue(cursorData, TACHOMETERTHRESHOLD, (int)0));
        data.setHardBrakeThreshold(ReadValue(cursorData, HARDBRAKETHRESHOLD, (float)0));
		data.setEobrGeneration(ReadValue(cursorData,EOBRGENERATION,(int)0));
		data.setLastPowerCycleResetDate(ReadValue(cursorData, LASTPOWERCYCLERESETDATE, (Date)null, DateUtility.getHomeTerminalSqlDateTimeFormat()));
        data.setClockSyncOffset(ReadValue(cursorData, CLOCKSYNCOFFSET, (long)0));
        data.setClockSyncDateUTC(ReadValue(cursorData, CLOCKSYNCDATEUTC, (DateTime)null, sqlDateTimeFormat));
		data.setVIN(ReadValue(cursorData, VIN, (String)null));
		data.setLastEventReferenceTimestamp(ReadValue(cursorData, LASTEVENTREFERENCETIMESTAMP, (Date)null, DateUtility.getHomeTerminalSqlDateTimeFormat()));

		return data;
	}


	@Override
	public ContentValues PersistContentValues(T data)
	{
		ContentValues content = super.PersistContentValues(data);

		SimpleDateFormat sqlDateTimeFormat = DateUtility.getSqlDateTimeFormatMS();
		sqlDateTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

		PutValue(content, SERIALNUMBER, data.getSerialNumber());
		PutValue(content, TRACTORNUMBER, data.getTractorNumber());
		PutValue(content, DATABUSTYPE, data.getDatabusType().getValue());
		PutValue(content, SLEEPMODEMINUTES, data.getSleepModeMinutes());
		PutValue(content, DATACOLLECTIONRATE, data.getDataCollectionRate());
		PutValue(content, FIRMWAREVERSION, data.getFirmwareVersion());
		PutValue(content, MAJORFIRMWAREVERSION, data.getMajorFirmwareVersion());
		PutValue(content, MINORFIRMWAREVERSION, data.getMinorFirmwareVersion());
		PutValue(content, PATCHFIRMWAREVERSION, data.getPatchFirmwareVersion());
		// For Gen II - DiscoveryPasskey is null - null not allowed in local db or encompass db - insert a dash
		if (data.getDiscoveryPasskey() == null)
			PutValue(content, DISCOVERYPASSKEY, "-");
		else
			PutValue(content, DISCOVERYPASSKEY, data.getDiscoveryPasskey());
		PutValue(content, ODOMETERCALIBRATIONDATE, data.getOdometerCalibrationDate(), DateUtility.getHomeTerminalSqlDateTimeFormat());
		PutValue(content, EOBRODOMETER, data.getEobrOdometer());
		PutValue(content, DASHBOARDODOMETER, data.getDasboardOdometer());	
		PutValue(content, SPEEDOMETERTHRESHOLD, data.getSpeedometerThreshold());
		PutValue(content, TACHOMETERTHRESHOLD, data.getTachometerThreshold());
		PutValue(content, HARDBRAKETHRESHOLD, data.getHardBrakeThreshold());
		PutValue(content, EOBRGENERATION, data.getEobrGeneration());
		PutValue(content, ISSUBMITTED, 0);
		PutValue(content, LASTPOWERCYCLERESETDATE, data.getLastPowerCycleResetDate(), DateUtility.getHomeTerminalSqlDateTimeFormat());
        PutValue(content, CLOCKSYNCOFFSET, data.getClockSyncOffset());
        PutValue(content, CLOCKSYNCDATEUTC, data.getClockSyncDateUTC(), sqlDateTimeFormat);
		PutValue(content, VIN, data.getVIN());
		PutValue(content, LASTEVENTREFERENCETIMESTAMP, data.getLastEventReferenceTimestamp(), DateUtility.getHomeTerminalSqlDateTimeFormat());

		return content;
	}

	///////////////////////////////////////////////////////////////////////////////////////
	// custom methods
	///////////////////////////////////////////////////////////////////////////////////////
   
	/// <summary>
    /// Fetch a list of all the eobr devices.
    /// </summary>
    /// <returns></returns>
    public List<T> FetchAll()
    {
    	String sql = SQL_SELECT_FETCHALL_COMMAND;

    	List<T> list = ExecuteFetchListRawQuery(sql, null);
    	
    	return list;
    }

	/**
	 * When not connected to an ELD, return the mostly recently connected EOBR Config
	 */
	public T FetchMostRecentlyConnectedEobr() {
		String sql = SQL_SELECT_LAST_CONNECTED_EOBRDEVICE;

		return ExecuteFetchRawQuery(sql, null);
	}

}
