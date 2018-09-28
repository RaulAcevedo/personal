package com.jjkeller.kmbapi.controller.dataaccess;

import android.content.Context;

import com.jjkeller.kmbapi.controller.dataaccess.db.DataUsagePersist;
import com.jjkeller.kmbapi.controller.dataaccess.db.DataUsageSummaryPersist;
import com.jjkeller.kmbapi.proxydata.CompanyConfigSettings;
import com.jjkeller.kmbapi.proxydata.DataUsage;
import com.jjkeller.kmbapi.proxydata.DataUsageSummary;

import java.util.Date;
import java.util.List;

public class DataUsageFacade extends FacadeBase {

	public DataUsageFacade(Context ctx) {
		super(ctx);
	}

	public DataUsage Fetch()
	{
		//CompanyPersist<DataUsage> persist = new CompanyPersist<DataUsage>(CompanyConfigSettings.class, this.getContext());
		return null;//persist.Fetch();
	}
	
	public void Save(CompanyConfigSettings companyConfigSettings)
	{
		//CompanyPersist<DataUsage> persist = new CompanyPersist<DataUsage>(CompanyConfigSettings.class, this.getContext());
		//persist.Persist(companyConfigSettings);
	}
	
	public void UpdateUsage(Date usageDate, long transmitted, long received, int networkEnum, String uri)
	{
		DataUsagePersist<DataUsage> persist = new DataUsagePersist<DataUsage>(DataUsage.class, this.getContext());
		persist.UpdateUsage(usageDate, transmitted, received, networkEnum, uri);		
	}
	
    /// Fetch the list of dates for all of the logs that the user has which are
    /// either unsubmitted or server logs.  The logDate list will be between start
    /// and end date (inclusive).  If a log is both a server and unsubmitted log, 
    /// it will only be counted once.
    public List<DataUsageSummary> FetchUsageSummary(Date startDate, Date endDate)
    {
    	DataUsageSummaryPersist<DataUsageSummary> persist = new DataUsageSummaryPersist<DataUsageSummary>(DataUsageSummary.class, this.getContext());
        return persist.FetchUsageSummary(startDate, endDate);
    }
    
    public void ClearUsage()
    {
		DataUsagePersist<DataUsage> persist = new DataUsagePersist<DataUsage>(DataUsage.class, this.getContext());
		persist.ClearUsage();		
    }
}
