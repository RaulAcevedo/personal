package com.jjkeller.kmbapi.controller.dataaccess;

import android.content.Context;

import com.jjkeller.kmbapi.controller.dataaccess.db.LogRemarkItemPersist;
import com.jjkeller.kmbapi.proxydata.LogRemarkItem;

import java.util.Date;
import java.util.List;

public class LogRemarkItemFacade extends FacadeBase {

	public LogRemarkItemFacade(Context ctx)
	{
		super(ctx);
	}

	public void Save(List<LogRemarkItem> logRemarkItems)
	{
		LogRemarkItemPersist<LogRemarkItem> persist = new LogRemarkItemPersist<LogRemarkItem>(LogRemarkItem.class, this.getContext());
		persist.Save(logRemarkItems);		
	}	
	
	public List<LogRemarkItem> FetchAllActive()
	{
		LogRemarkItemPersist<LogRemarkItem> persist = new LogRemarkItemPersist<LogRemarkItem>(LogRemarkItem.class, this.getContext());
		return persist.FetchAllActive();
	}

	public Date GetMostRecentChangeDate()
	{
		LogRemarkItemPersist<LogRemarkItem> persist = new LogRemarkItemPersist<LogRemarkItem>(LogRemarkItem.class, this.getContext());
		return persist.GetMostRecentChangeDate();
	}
}
