package com.jjkeller.kmbapi.proxydata;

import com.jjkeller.kmbapi.kmbeobr.EventRecord;

import java.util.Date;
import java.util.UUID;

/**
 * Created by jld5296 on 9/19/16.
 */
public class EventRecordToGeotabEventRecordAdapter extends GeotabEventRecord {
    private int _driverId;
    private String _vehicleId;
    private Long _geotabHosDataKey;

    public EventRecordToGeotabEventRecordAdapter(EventRecord eventRecord, int driverId, String vehicleId, Long geotabHosDataKey) {
        _eventRecord = eventRecord;
        _driverId = driverId;
        _vehicleId = vehicleId;
        _geotabHosDataKey = geotabHosDataKey;
    }

    private EventRecord _eventRecord;

    @Override
    public int getDriverId() {

        return _driverId;
    }

    @Override
    public void setDriverId(int driverId) {
        // Does nothing
    }

    @Override
    public String getVehicleId() { return _vehicleId; }

    @Override
    public void setVehicleId(String vehicleId){ _vehicleId = vehicleId; }

    @Override
    public int getEventType() {
        return _eventRecord.getEventType();
    }

    @Override
    public void setEventType(int eventType) {
        _eventRecord.setEventType(eventType);
    }

    @Override
    public int getEventData() {
        return _eventRecord.getEventData();
    }

    @Override
    public void setEventData(int eventData) {
        _eventRecord.setEventData(eventData);
    }

    @Override
    public int getGeotabHosDataKey() {
        return safeLongToInt(_geotabHosDataKey);
    }

    @Override
    public void setGeotabHosDataKey(int geotabHosDataKey) {
        _geotabHosDataKey = Long.valueOf(geotabHosDataKey);
    }

    @Override
    public Date getTimestampUtc() {
        return _eventRecord.getTimecodeAsDate();
    }

    @Override
    public void setTimestampUtc(Date timestampUtc) {
        _eventRecord.setTimecode(timestampUtc.getTime() / 1000);  // Seconds
    }

    private static int safeLongToInt(long l) {
        if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
            throw new IllegalArgumentException
                    (l + " cannot be cast to int without changing its value.");
        }
        return (int) l;
    }
}
