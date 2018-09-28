package com.jjkeller.kmbapi.controller;

import android.content.Context;

import com.jjkeller.kmbapi.common.TimeKeeper;
import com.jjkeller.kmbapi.controller.dataaccess.DataUsageFacade;
import com.jjkeller.kmbapi.controller.share.ControllerBase;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.proxydata.DataUsageSummary;

import java.util.Date;
import java.util.List;

public class DataUsageController extends ControllerBase {

	public DataUsageController(Context ctx) {
		super(ctx);
	}

	public void UpdateDataUsage(long transmitted, long received)
	{
		this.UpdateDataUsage(transmitted, received, 0, null);
	}
	
	public void UpdateDataUsage(long transmitted, long received, String uri)
	{
		this.UpdateDataUsage(transmitted, received, 0, uri);
	}
	
	public void UpdateDataUsage(long transmitted, long received, int networkEnum, String uri)
	{
		DataUsageFacade usageFacade = new DataUsageFacade(this.getContext());
		Date current;
		if (this.getCurrentUser() != null && this.getCurrentUser().getHomeTerminalTimeZone() != null) 
			current = DateUtility.CurrentHomeTerminalTime(this.getCurrentUser());
		else current = TimeKeeper.getInstance().now();
		usageFacade.UpdateUsage(current, transmitted, received, networkEnum, uri);
	}

	public List<DataUsageSummary> getDataUsageForReport()
	{
		DataUsageFacade usageFacade = new DataUsageFacade(this.getContext());
		return usageFacade.FetchUsageSummary(null, null);
	}
	
	public void ClearDataUsage()
	{
		DataUsageFacade usageFacade = new DataUsageFacade(this.getContext());
		usageFacade.ClearUsage();
	}
}
