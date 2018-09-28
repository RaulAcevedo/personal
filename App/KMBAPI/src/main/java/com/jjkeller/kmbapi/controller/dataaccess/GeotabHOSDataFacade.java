package com.jjkeller.kmbapi.controller.dataaccess;

import android.content.Context;

import com.jjkeller.kmbapi.controller.dataaccess.db.GeotabHOSDataPersist;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.proxydata.GeotabHOSData;

import java.util.Date;
import java.util.List;

/**
 * Created by jhm2586 on 9/1/2016.
 */
public class GeotabHOSDataFacade extends FacadeBase{
    public GeotabHOSDataFacade(Context ctx) {
        super(ctx);
    }

    public GeotabHOSDataFacade(Context ctx, User user) {
        super(ctx, user);
    }

    public List<GeotabHOSData> FetchAllUnsubmitted()
    {
        GeotabHOSDataPersist<GeotabHOSData> persist = new GeotabHOSDataPersist<GeotabHOSData>(GeotabHOSData.class, this.getContext());
        return persist.FetchAllUnsubmitted();
    }

    public void MarkAsSubmitted(List<GeotabHOSData> geotabDataList)
    {
        GeotabHOSDataPersist<GeotabHOSData> persist = new GeotabHOSDataPersist<GeotabHOSData>(GeotabHOSData.class, this.getContext());
        persist.MarkAsSubmitted(geotabDataList);
    }

    public void Save(List<GeotabHOSData> list)
    {
        GeotabHOSDataPersist<GeotabHOSData> persist = new GeotabHOSDataPersist<GeotabHOSData>(GeotabHOSData.class, getContext());

        for(GeotabHOSData route : list)
            persist.Persist(route);
    }

    public void Save(GeotabHOSData geotabData)
    {
        GeotabHOSDataPersist<GeotabHOSData> persist = new GeotabHOSDataPersist<GeotabHOSData>(GeotabHOSData.class, getContext());
        persist.Persist(geotabData);
    }

    public void PurgeOldRecords(Date cutoffDate)
    {
        PurgeOldRecords(cutoffDate, false);
    }

    public void PurgeOldRecords(Date cutoffDate, boolean deleteUnsubmitted)
    {
        GeotabHOSDataPersist<GeotabHOSData> persist = new GeotabHOSDataPersist<GeotabHOSData>(GeotabHOSData.class, getContext());
        persist.PurgeOldRecords(cutoffDate, deleteUnsubmitted);
    }

    public GeotabHOSData FetchByTimestamp(String vehicleId, Date timestamp)
    {
        if(vehicleId == null) return null;
        GeotabHOSDataPersist<GeotabHOSData> persist = new GeotabHOSDataPersist<GeotabHOSData>(GeotabHOSData.class, this.getContext());
        return persist.FetchByTimestamp(vehicleId, timestamp);
    }

    public GeotabHOSData FetchByVehicleAndKey(String vehicleId, int key) {
        if(vehicleId == null) return null;
        GeotabHOSDataPersist<GeotabHOSData> persist = new GeotabHOSDataPersist<GeotabHOSData>(GeotabHOSData.class, this.getContext());
        return persist.FetchByVehicleAndKey(vehicleId, key);
    }

    public GeotabHOSData FetchLatestByVehicle(String vehicleId)
    {
        if(vehicleId == null) return null;
        GeotabHOSDataPersist<GeotabHOSData> persist = new GeotabHOSDataPersist<GeotabHOSData>(GeotabHOSData.class, this.getContext());
        return persist.FetchLatestByVehicle(vehicleId);
    }

    public void PurgeTable()
	{
        GeotabHOSDataPersist<GeotabHOSData> persist = new GeotabHOSDataPersist<GeotabHOSData>(GeotabHOSData.class, this.getContext());
        persist.PurgeTable();
    }
}
