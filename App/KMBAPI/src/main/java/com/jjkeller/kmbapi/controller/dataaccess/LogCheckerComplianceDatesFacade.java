package com.jjkeller.kmbapi.controller.dataaccess;

import android.content.Context;

import com.jjkeller.kmbapi.controller.dataaccess.db.LogCheckerComplianceDatesPersist;
import com.jjkeller.kmbapi.proxydata.LogCheckerComplianceDates;

import java.util.List;

public class LogCheckerComplianceDatesFacade extends FacadeBase {

	public LogCheckerComplianceDatesFacade(Context ctx) {
		super(ctx);
	}

	public List<LogCheckerComplianceDates> Fetch()
	{
		LogCheckerComplianceDatesPersist<LogCheckerComplianceDates> persist = new LogCheckerComplianceDatesPersist<LogCheckerComplianceDates>(LogCheckerComplianceDates.class, this.getContext());
		return persist.FetchList();
	}
	
	public void Save(List<LogCheckerComplianceDates> logCheckerComplianceDates)
	{
		LogCheckerComplianceDatesPersist<LogCheckerComplianceDates> persist = new LogCheckerComplianceDatesPersist<LogCheckerComplianceDates>(LogCheckerComplianceDates.class, this.getContext());
        for(LogCheckerComplianceDates complianceDate : logCheckerComplianceDates )
        {
        	persist.Persist(complianceDate);
        }
	}
	
    public LogCheckerComplianceDates FetchMostRecentComplianceDate()
    {
    	LogCheckerComplianceDatesPersist<LogCheckerComplianceDates> persist = new LogCheckerComplianceDatesPersist<LogCheckerComplianceDates>(LogCheckerComplianceDates.class, this.getContext());
        return persist.FetchMostRecentLogCheckerComplianceDates();
    }
    
    public LogCheckerComplianceDates FetchLogCheckerComplianceDatesByEnum(int complianceDatesType)
    {
    	LogCheckerComplianceDatesPersist<LogCheckerComplianceDates> persist = new LogCheckerComplianceDatesPersist<LogCheckerComplianceDates>(LogCheckerComplianceDates.class, this.getContext());
        return persist.FetchLogCheckerComplianceDatesByEnum(complianceDatesType);
    }
}
