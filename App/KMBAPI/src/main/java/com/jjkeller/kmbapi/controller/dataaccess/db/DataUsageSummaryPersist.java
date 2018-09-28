package com.jjkeller.kmbapi.controller.dataaccess.db;

import android.content.Context;
import android.database.Cursor;

import com.jjkeller.kmbapi.controller.dataaccess.AbstractDBAdapter;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.controller.utility.DeviceInfo;
import com.jjkeller.kmbapi.proxydata.DataUsageSummary;

import java.util.Date;
import java.util.List;

public class DataUsageSummaryPersist <T extends DataUsageSummary> extends AbstractDBAdapter<T> {

	private static final String DEVICEID = "DeviceId";
	private static final String USAGEDATE = "UsageDate";
	private static final String SUMTRANSMITTEDBYTES = "SumTransmittedBytes";
	private static final String SUMRECEIVEDBYTES = "SumReceivedBytes";
    
	private static final String SQL_FETCHUSAGESUMMARY = "SELECT DeviceId, UsageDate, SUM(TransmittedBytes) as SumTransmittedBytes, SUM(ReceivedBytes) as SumReceivedBytes FROM DataUsage WHERE DeviceId=? GROUP BY UsageDate";
	private static final String SQL_FETCHUSAGESUMMARY_BYDATE = "SELECT DeviceId, UsageDate, SUM(TransmittedBytes) as SumTransmittedBytes, SUM(ReceivedBytes) as SumReceivedBytes FROM DataUsage WHERE DeviceId=? AND UsageDate <= ? AND UsageDate >= ? GROUP BY UsageDate";

	///////////////////////////////////////////////////////////////////////////////////////
	// constructors
	///////////////////////////////////////////////////////////////////////////////////////
	public DataUsageSummaryPersist(Class<T> clazz, Context ctx)
	{
		super(clazz, ctx);
	}
	
	///////////////////////////////////////////////////////////////////////////////////////
	// @Override methods
	///////////////////////////////////////////////////////////////////////////////////////
	@Override
	public String getSelectPrimaryKeyCommand()
	{
		return null;
	}
	
	@Override
	protected String[] getSelectPrimaryKeyArgs(T data) {
		return null;
	}
	
	@Override
	protected T BuildObject(Cursor cursorData)
	{
		T data = super.BuildObject(cursorData);

		data.setDeviceId(ReadValue(cursorData, DEVICEID, (String)null));
		data.setUsageDate(ReadValue(cursorData, USAGEDATE, (Date)null, DateUtility.getHomeTerminalSqlDateFormat()));
		data.setSentBytes(ReadValue(cursorData, SUMTRANSMITTEDBYTES, (long)0));
		data.setReceivedBytes(ReadValue(cursorData, SUMRECEIVEDBYTES, (long)0));

		return (T) data;
	}
	
	public List<T> FetchUsageSummary(Date startDate, Date endDate)
	{
		String phoneId = DeviceInfo.GetDeviceIdentifier(getContext());
		List<T> objList;
		if(startDate == null && endDate == null)
			objList = this.ExecuteFetchListRawQuery(SQL_FETCHUSAGESUMMARY, new String[]{phoneId});
		else
			objList = this.ExecuteFetchListRawQuery(SQL_FETCHUSAGESUMMARY_BYDATE, new String[]{phoneId, DateUtility.getHomeTerminalSqlDateFormat().format(startDate), DateUtility.getHomeTerminalSqlDateFormat().format(endDate)});
		
		return objList;
	}
}
