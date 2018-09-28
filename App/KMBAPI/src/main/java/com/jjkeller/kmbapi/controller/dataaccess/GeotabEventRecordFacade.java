package com.jjkeller.kmbapi.controller.dataaccess;

import android.content.Context;

import com.jjkeller.kmbapi.controller.dataaccess.db.GeotabEventRecordPersist;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.proxydata.GeotabEventRecord;

import java.util.Date;
import java.util.List;

/**
 * Created by jhm2586 on 9/20/2016.
 */
public class GeotabEventRecordFacade extends FacadeBase{
    public GeotabEventRecordFacade(Context ctx) {
        super(ctx);
    }

    public GeotabEventRecordFacade(Context ctx, User user) {
        super(ctx, user);
    }

    public List<GeotabEventRecord> FetchAllUnsubmitted()
    {
        GeotabEventRecordPersist<GeotabEventRecord> persist = new GeotabEventRecordPersist<GeotabEventRecord>(GeotabEventRecord.class, this.getContext());
        return persist.FetchAllUnsubmitted();
    }

    public void MarkAsSubmitted(List<GeotabEventRecord> geotabDataList)
    {
        GeotabEventRecordPersist<GeotabEventRecord> persist = new GeotabEventRecordPersist<GeotabEventRecord>(GeotabEventRecord.class, this.getContext());
        persist.MarkAsSubmitted(geotabDataList);
    }

    public void Save(List<GeotabEventRecord> list)
    {
        GeotabEventRecordPersist<GeotabEventRecord> persist = new GeotabEventRecordPersist<GeotabEventRecord>(GeotabEventRecord.class, getContext());

        for(GeotabEventRecord route : list)
            persist.Persist(route);
    }

    public void Save(GeotabEventRecord geotabData)
    {
        GeotabEventRecordPersist<GeotabEventRecord> persist = new GeotabEventRecordPersist<GeotabEventRecord>(GeotabEventRecord.class, getContext());
        persist.Persist(geotabData);
    }

    public GeotabEventRecord FetchByTimestamp(String vehicleId, Date timestamp)
    {
        if(vehicleId == null) return null;
        GeotabEventRecordPersist<GeotabEventRecord> persist = new GeotabEventRecordPersist<GeotabEventRecord>(GeotabEventRecord.class, this.getContext());
        return persist.FetchByTimestamp(vehicleId, timestamp);
    }

    public GeotabEventRecord FetchByTimestampFrame(String vehicleId, int eventType, Date initialTimestamp, Date finalTimestamp)
    {
        if(vehicleId == null) return null;
        GeotabEventRecordPersist<GeotabEventRecord> persist = new GeotabEventRecordPersist<GeotabEventRecord>(GeotabEventRecord.class, this.getContext());
        return persist.FetchByTimestampFrame(vehicleId, eventType, initialTimestamp, finalTimestamp);
    }

    public GeotabEventRecord FetchLatestByVehicle(String vehicleId)
    {
        if(vehicleId == null) return null;
        GeotabEventRecordPersist<GeotabEventRecord> persist = new GeotabEventRecordPersist<GeotabEventRecord>(GeotabEventRecord.class, this.getContext());
        return persist.FetchLatestByVehicle(vehicleId);
    }

    public GeotabEventRecord FetchByTimestampAndEventType(String vehicleId, Date initialTimestamp, List<Integer> eventTypes)
    {
        if (vehicleId == null) return null;
        GeotabEventRecordPersist<GeotabEventRecord> persist = new GeotabEventRecordPersist<GeotabEventRecord>(GeotabEventRecord.class, this.getContext());
        return persist.FetchByTimestampAndEventType(vehicleId, initialTimestamp, eventTypes);
    }

    public void PurgeTable()
	{
        GeotabEventRecordPersist<GeotabEventRecord> persist = new GeotabEventRecordPersist<GeotabEventRecord>(GeotabEventRecord.class, this.getContext());
        persist.PurgeTable();
    }

    public void PurgeOldRecords(Date cutoffDate)
    {
        GeotabEventRecordPersist<GeotabEventRecord> persist = new GeotabEventRecordPersist<GeotabEventRecord>(GeotabEventRecord.class, this.getContext());
        persist.PurgeOldRecords(cutoffDate);
    }
}