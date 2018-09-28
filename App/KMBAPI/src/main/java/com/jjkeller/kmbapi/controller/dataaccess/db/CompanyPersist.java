package com.jjkeller.kmbapi.controller.dataaccess.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.jjkeller.kmbapi.common.TimeKeeper;
import com.jjkeller.kmbapi.controller.dataaccess.AbstractDBAdapter;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.enums.EobrCommunicationModeEnum;
import com.jjkeller.kmbapi.proxydata.CompanyConfigSettings;

import java.util.Date;

public class CompanyPersist<T extends CompanyConfigSettings> extends AbstractDBAdapter<T> {

	///////////////////////////////////////////////////////////////////////////////////////
	// private members
	///////////////////////////////////////////////////////////////////////////////////////
	private static final String COMPANYNAME = "CompanyName";
	private static final String COMPANYID = "CompanyId";
	private static final String USERNAME = "Username";
	private static final String PASSWORD = "Password";
	private static final String DAILYLOGSTARTTIME = "DailyLogStartTime";
	private static final String LOGPURGEDAYCOUNT = "LogPurgeDayCount";
	private static final String EOBRDISCOVERYPASSKEY = "EobrDiscoveryPasskey";
	private static final String EOBRCOMMUNICATIONMODE = "EobrCommunicationMode";
	private static final String EOBRSLEEPMODEMINUTES = "EobrSleepModeMinutes";
	private static final String EOBRCOLLLECTIONRATESECONDS = "EobrCollectionRateSeconds";
	private static final String ACTIVATIONCODE = "ActivationCode";
	private static final String ISACTIVATED = "IsActivated";
	private static final String ACTIVATIONDATE = "ActivationDate";
	private static final String ALLOWDRIVERSCOMPLETEDVIR = "AllowDriversCompleteDVIR";
	private static final String GENERATEPRETRIPDVIRWITHDEFECTALERT = "GeneratePreTripDVIRWithDefectAlert";
	private static final String DRIVERSTARTDISTANCE = "DriverStartDistance";
	private static final String DRIVERSTOPMINUTES = "DriverStopMinutes";
	private static final String MAXACCEPTABLESPEED = "MaxAcceptableSpeed";
	private static final String MAXACCEPTABLETACH = "MaxAcceptableTach";
	private static final String HARDBRAKEDECELERATIONSPEED = "HardBrakeDecelerationSpeed";
	private static final String USEKMBWEBAPISERVICES = "UseKmbWebApiServices";
	private static final String MULTIPLEUSERSALLOWED = "MultipleUsersAllowed";
	private static final String DRIVESTARTSPEED = "DriveStartSpeed";
	private static final String MANDATEDRIVINGSTOPTIMEMINUTES = "MandateDrivingStopTimeMinutes";
	private static final String ISGEOTABENABLED = "IsGeotabEnabled";
	private static final String SQL_SELECT_PRIMARYKEY_COMMAND = "select Key from Company where CompanyName=?";
	private static final String SQL_SELECTACTIVATED_COMMAND = "select * from Company where IsActivated=?";
	private static final String ISMOTIONPICTUREENABLED = "IsMotionPictureEnabled";
	private static final String ISAUTOASSIGNUNIDENTIFIEDEVENTS = "IsAutoAssignUnIdentifiedEvents";

	///////////////////////////////////////////////////////////////////////////////////////
	// constructors
	///////////////////////////////////////////////////////////////////////////////////////
	public CompanyPersist(Class<T> clazz, Context ctx)
	{
		super(clazz, ctx);
		
		setDbTableName(DB_TABLE_COMPANY);
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
		return new String[]{data.getDmoCompanyName()};
	}
	
	@Override
	protected String getSelectCommand()
	{
		return SQL_SELECTACTIVATED_COMMAND;
	}
	
	@Override
	protected String[] getSelectArgs()
	{
		return new String[]{"1"};
	}

	@Override
	protected T BuildObject(Cursor cursorData)
	{
		T data = super.BuildObject(cursorData);

		data.setActivationCode(ReadValue(cursorData, ACTIVATIONCODE, (String)null)); 
		data.setDmoCompanyName(ReadValue(cursorData, COMPANYNAME, (String)null));
		data.setDmoCompanyId(ReadValue(cursorData, COMPANYID, (String)null));
		data.setDmoUsername(ReadValue(cursorData, USERNAME, (String)null));
		data.setDmoPasswordEncrypt(ReadValue(cursorData, PASSWORD, (String)null));
		data.setDailyLogStartTime(ReadValue(cursorData, DAILYLOGSTARTTIME, "-1"));
		data.setLogPurgeDayCount(ReadValue(cursorData, LOGPURGEDAYCOUNT, (int)2));					
		data.setEobrDiscoveryPasskey(ReadValue(cursorData, EOBRDISCOVERYPASSKEY, (String)null));
		data.getEobrCommunicationMode().setValue((ReadValue(cursorData, EOBRCOMMUNICATIONMODE, EobrCommunicationModeEnum.USB_BT)));
		data.setEobrSleepModeMinutes(ReadValue(cursorData, EOBRSLEEPMODEMINUTES, (int)-1));
		data.setEobrDataCollectionRateSeconds(ReadValue(cursorData, EOBRCOLLLECTIONRATESECONDS, (int)1));
		data.setAllowDriversCompleteDVIR(ReadValue(cursorData, ALLOWDRIVERSCOMPLETEDVIR, true));
		data.setGeneratePreTripDVIRWithDefectAlert(ReadValue(cursorData, GENERATEPRETRIPDVIRWITHDEFECTALERT, true));
		data.setDriverStartDistance(ReadValue(cursorData, DRIVERSTARTDISTANCE, (float)0));
		data.setDriverStopMinutes(ReadValue(cursorData, DRIVERSTOPMINUTES, (int)0));
        data.setMaxAcceptableSpeed(ReadValue(cursorData, MAXACCEPTABLESPEED, (float)0));
        data.setMaxAcceptableTach(ReadValue(cursorData, MAXACCEPTABLETACH, (int)0));
        data.setHardBrakeDecelerationSpeed(ReadValue(cursorData, HARDBRAKEDECELERATIONSPEED, (float)0));
		data.setUseKmbWebApiServices(ReadValue(cursorData, USEKMBWEBAPISERVICES, false));
		data.setMultipleUsersAllowed(ReadValue(cursorData, MULTIPLEUSERSALLOWED, false));		
		data.setDriveStartSpeed(ReadValue(cursorData, DRIVESTARTSPEED, 5));
		data.setMandateDrivingStopTimeMinutes(ReadValue(cursorData, MANDATEDRIVINGSTOPTIMEMINUTES, 5));
		data.setIsGeotabEnabled(ReadValue(cursorData, ISGEOTABENABLED, false));
		data.setIsMotionPictureEnabled(ReadValue(cursorData, ISMOTIONPICTUREENABLED, false));
		data.setIsAutoAssignUnIdentifiedEvents(ReadValue(cursorData, ISAUTOASSIGNUNIDENTIFIEDEVENTS, false));

		return (T) data;
	}
	
	@Override
	protected ContentValues PersistContentValues(T data)
	{
		ContentValues content = super.PersistContentValues(data);
		
		PutValue(content, ACTIVATIONCODE, data.getActivationCode());
		PutValue(content, COMPANYNAME, data.getDmoCompanyName());
		PutValue(content, COMPANYID, data.getDmoCompanyId());
		PutValue(content, USERNAME, data.getDmoUsername());
		PutValue(content, PASSWORD, data.getDmoPasswordEncrypt());
		PutValue(content, DAILYLOGSTARTTIME, data.getDailyLogStartTime());
		PutValue(content, LOGPURGEDAYCOUNT, data.getLogPurgeDayCount());
		PutValue(content, ISACTIVATED, 1);
		PutValue(content, EOBRDISCOVERYPASSKEY, data.getEobrDiscoveryPasskey());
		PutValue(content, EOBRCOMMUNICATIONMODE, data.getEobrCommunicationMode().getValue());
		PutValue(content, EOBRSLEEPMODEMINUTES, data.getEobrSleepModeMinutes());
		PutValue(content, EOBRCOLLLECTIONRATESECONDS, data.getEobrDataCollectionRateSeconds());
		PutValue(content, ACTIVATIONDATE, DateUtility.getHomeTerminalDateTimeFormat24Hour().format(TimeKeeper.getInstance().now()));
		PutValue(content, ALLOWDRIVERSCOMPLETEDVIR, data.getAllowDriversCompleteDVIR());
		PutValue(content, GENERATEPRETRIPDVIRWITHDEFECTALERT, data.getGeneratePreTripDVIRWithDefectAlert());
		PutValue(content, DRIVERSTARTDISTANCE, data.getDriverStartDistance());
		PutValue(content, DRIVERSTOPMINUTES, data.getDriverStopMinutes());
		PutValue(content, MAXACCEPTABLESPEED, data.getMaxAcceptableSpeed());
		PutValue(content, MAXACCEPTABLETACH, data.getMaxAcceptableTach());
		PutValue(content, HARDBRAKEDECELERATIONSPEED, data.getHardBrakeDecelerationSpeed());
		PutValue(content, USEKMBWEBAPISERVICES, data.getUseKmbWebApiServices());
		PutValue(content, MULTIPLEUSERSALLOWED, data.getMultipleUsersAllowed());
		PutValue(content, DRIVESTARTSPEED, data.getDriveStartSpeed());
		PutValue(content, MANDATEDRIVINGSTOPTIMEMINUTES, data.getMandateDrivingStopTimeMinutes());
		PutValue(content, ISGEOTABENABLED, data.getIsGeotabEnabled());
		PutValue(content, ISMOTIONPICTUREENABLED, data.getIsMotionPictureEnabled());
		PutValue(content, ISAUTOASSIGNUNIDENTIFIEDEVENTS, data.getIsAutoAssignUnIdentifiedEvents());
		return content;
	}

	///////////////////////////////////////////////////////////////////////////////////////
	// custom methods
	///////////////////////////////////////////////////////////////////////////////////////
}
