package com.jjkeller.kmbapi.controller.dataaccess;

import android.content.Context;

import com.jjkeller.kmbapi.controller.dataaccess.db.EventDataRecordPersist;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.proxydata.EventDataRecord;

import java.util.Date;
import java.util.List;

public class EventDataRecordFacade extends FacadeBase{

	public EventDataRecordFacade(Context ctx, User user)
    {
        super(ctx, user);
    }

    public EventDataRecordFacade(Context ctx)
    {
        super(ctx);
    }

    public List<EventDataRecord> FetchAllUnsubmitted()
    {
        EventDataRecordPersist<EventDataRecord> persist = new EventDataRecordPersist<EventDataRecord>(EventDataRecord.class, getContext());
        return persist.FetchAllUnsubmitted();
    }
    
    /**
     * Fetch unsubmitted entities, but only the top numberOfRecordsToFetch
     * 
     * @param numberOfRecordsToFetch
     * @return
     */
    public List<EventDataRecord> FetchUnsubmittedLimited(int numberOfRecordsToFetch)
    {
        EventDataRecordPersist<EventDataRecord> persist = new EventDataRecordPersist<EventDataRecord>(EventDataRecord.class, getContext());
        return persist.FetchUnsubmittedLimited(numberOfRecordsToFetch);
    }
    
    public void MarkAsSubmitted(List<EventDataRecord> eventRecordList)
    {
    	EventDataRecordPersist<EventDataRecord> persist = new EventDataRecordPersist<EventDataRecord>(EventDataRecord.class, getContext());
        persist.MarkAsSubmitted(eventRecordList);
    }
    
    public void Save(EventDataRecord eventDataRecord)
    {
    	EventDataRecordPersist<EventDataRecord> persist = new EventDataRecordPersist<EventDataRecord>(EventDataRecord.class, getContext());
    	persist.Persist(eventDataRecord);
    }
    
    public void PurgeOldRecords(Date cutoffDate)
    {
    	EventDataRecordPersist<EventDataRecord> persist = new EventDataRecordPersist<EventDataRecord>(EventDataRecord.class, getContext());
    	persist.PurgeOldRecords(cutoffDate);
    }
    
    public EventDataRecord FetchMostRecent(String eobrSerialNumber)
    {
    	if(eobrSerialNumber == null || eobrSerialNumber.length() == 0) return null;
        EventDataRecordPersist<EventDataRecord> persist = new EventDataRecordPersist<EventDataRecord>(EventDataRecord.class, this.getContext());
        return persist.FetchMostRecentEventRecord(eobrSerialNumber);
    }
    
    public EventDataRecord FetchMostRecent()
    {
        EventDataRecordPersist<EventDataRecord> persist = new EventDataRecordPersist<EventDataRecord>(EventDataRecord.class, this.getContext());
        return persist.FetchMostRecentEventRecord();
    }
}
