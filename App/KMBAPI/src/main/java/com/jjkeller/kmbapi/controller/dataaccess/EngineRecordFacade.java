package com.jjkeller.kmbapi.controller.dataaccess;

import android.content.Context;

import com.jjkeller.kmbapi.controller.dataaccess.db.EngineRecordPersist;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.proxydata.EngineRecord;

import java.util.Date;
import java.util.List;

public class EngineRecordFacade extends FacadeBase{

	public EngineRecordFacade(Context ctx, User user)
	{
		super(ctx, user);
	}

    public List<EngineRecord> FetchAllUnsubmitted()
    {
        EngineRecordPersist<EngineRecord> persist = new EngineRecordPersist<EngineRecord>(EngineRecord.class, getContext());
        return persist.FetchAllUnsubmitted();
    }
    
    /**
     * Fetch unsubmitted entities, but only the top numberOfRecordsToFetch
     * 
     * @param numberOfRecordsToFetch
     * @return
     */
    public List<EngineRecord> FetchUnsubmittedLimited(int numberOfRecordsToFetch)
    {
        EngineRecordPersist<EngineRecord> persist = new EngineRecordPersist<EngineRecord>(EngineRecord.class, getContext());
        return persist.FetchUnsubmittedLimited(numberOfRecordsToFetch);
    }
    
    public void MarkAsSubmitted(List<EngineRecord> engineRecordList)
    {
    	EngineRecordPersist<EngineRecord> persist = new EngineRecordPersist<EngineRecord>(EngineRecord.class, getContext());
        persist.MarkAsSubmitted(engineRecordList);
    }
    
    public void Save(EngineRecord engineRecord)
    {
    	EngineRecordPersist<EngineRecord> persist = new EngineRecordPersist<EngineRecord>(EngineRecord.class, getContext());
    	persist.Persist(engineRecord);
    }
    
    public void PurgeOldRecords(Date cutoffDate)
    {
    	EngineRecordPersist<EngineRecord> persist = new EngineRecordPersist<EngineRecord>(EngineRecord.class, getContext());
    	persist.PurgeOldRecords(cutoffDate);
    }
    
    public EngineRecord FetchMostRecent(String eobrSerialNumber)
    {
    	if(eobrSerialNumber == null || eobrSerialNumber.length() == 0) return null;
        EngineRecordPersist<EngineRecord> persist = new EngineRecordPersist<EngineRecord>(EngineRecord.class, this.getContext());
        return persist.FetchMostRecentEngineRecord(eobrSerialNumber);
    }
}
