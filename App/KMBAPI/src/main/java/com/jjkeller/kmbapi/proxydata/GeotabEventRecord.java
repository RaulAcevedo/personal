package com.jjkeller.kmbapi.proxydata;

import java.util.Date;

/**
 * Created by jhm2586 on 9/20/2016.
 */
public class GeotabEventRecord extends ProxyBase{
    private Date timestampUtc= null;
    private int driverId;
    private String vehicleId;
    private int eventType = -1;
    private int eventData;
    private int geotabHosDataKey;
    private GeotabHOSData hosData;

    public Date getTimestampUtc() {
        return timestampUtc;
    }

    public void setTimestampUtc(Date timestampUtc) {
        this.timestampUtc = timestampUtc;
    }

    public int getDriverId() {
        return driverId;
    }

    public void setDriverId(int driverId) {
        this.driverId = driverId;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public int getEventType() {
        return eventType;
    }

    public void setEventType(int eventType) {
        this.eventType = eventType;
    }

    public int getEventData() {
        return eventData;
    }

    public void setEventData(int eventData) {
        this.eventData = eventData;
    }

    public int getGeotabHosDataKey() {
        return geotabHosDataKey;
    }

    public void setGeotabHosDataKey(int geotabHosDataKey) {
        this.geotabHosDataKey = geotabHosDataKey;
    }

    public GeotabHOSData getHosData() {
        return hosData;
    }

    public void setHosData(GeotabHOSData hosData) {
        this.hosData = hosData;
    }
}
