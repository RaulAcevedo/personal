package com.jjkeller.kmbapi.controller.dataaccess;

import android.content.Context;

import com.jjkeller.kmbapi.controller.dataaccess.db.TripRecordPersist;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.proxydata.TripRecord;

import java.util.Date;
import java.util.List;

public class TripRecordFacade extends FacadeBase
{
	public TripRecordFacade(Context ctx, User user)
	{
		super(ctx, user);
	}
	
	public void Save(TripRecord tripRecord)
	{
		TripRecordPersist<TripRecord> persist = new TripRecordPersist<TripRecord>(TripRecord.class, getContext());
		persist.Persist(tripRecord);
	}
	
    public List<TripRecord> FetchAllUnsubmitted()
    {
    	TripRecordPersist<TripRecord> persist = new TripRecordPersist<TripRecord>(TripRecord.class, getContext());
        return persist.FetchAllUnsubmitted();
    }
    
    /**
     * Fetch unsubmitted entities, but only the top numberOfRecordsToFetch
     * 
     * @param numberOfRecordsToFetch
     * @return
     */
    public List<TripRecord> FetchUnsubmittedLimited(int numberOfRecordsToFetch)
    {
    	TripRecordPersist<TripRecord> persist = new TripRecordPersist<TripRecord>(TripRecord.class, getContext());
        return persist.FetchUnsubmittedLimited(numberOfRecordsToFetch);
    }
    
    public void MarkAsSubmitted(List<TripRecord> tripRecordList)
    {
    	TripRecordPersist<TripRecord> persist = new TripRecordPersist<TripRecord>(TripRecord.class, getContext());
        persist.MarkAsSubmitted(tripRecordList);
    }
    
    public void PurgeOldRecords(Date cutoffDate)
    {
    	TripRecordPersist<TripRecord> persist = new TripRecordPersist<TripRecord>(TripRecord.class, getContext());
    	persist.PurgeOldRecords(cutoffDate);
    }
}
