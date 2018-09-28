package com.jjkeller.kmbapi.HosMessageProcessor.utility;

import com.jjkeller.kmbapi.kmbeobr.EventRecord;
import com.jjkeller.kmbapi.kmbeobr.StatusRecord;
import com.jjkeller.kmbapi.kmbeobr.TripReport;
import com.jjkeller.kmbapi.proxydata.GeotabEventRecord;

/**
 * Created by jhm2586 on 9/26/2016.
 */

public class GeotabEventRecordToEventRecordUtility {

    public static void UpdateEventRecordFromGeotabEventRecord(EventRecord eventRecord, GeotabEventRecord geotabEventRecord)
    {
        if(geotabEventRecord == null) return;

        eventRecord.setRecordId(safeLongToInt(geotabEventRecord.getPrimaryKey()));
        eventRecord.setTimecode(geotabEventRecord.getTimestampUtc().getTime());
        eventRecord.setEventType(geotabEventRecord.getEventType());
        eventRecord.setEventData(geotabEventRecord.getEventData());
        eventRecord.setDriverId(geotabEventRecord.getDriverId());
        eventRecord.setEobrId(geotabEventRecord.getDriverId());
        eventRecord.setGeotabHOSDataKey(geotabEventRecord.getGeotabHosDataKey());

        StatusRecord statusRecord = new StatusRecord();
        GeotabHosDataToStatusRecordUtility.UpdateStatusRecordFromGeotabHosData(statusRecord,geotabEventRecord.getHosData());
        eventRecord.setStatusRecordData(statusRecord);

        TripReport tripReport = new TripReport();
        GeotabHosDataToTripRecordUtility.updateTripReportFromGeotabHosData(tripReport, geotabEventRecord.getHosData());
        eventRecord.setTripReportData(tripReport);
    }

    private static int safeLongToInt(long l) {
        if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
            throw new IllegalArgumentException
                    (l + " cannot be cast to int without changing its value.");
        }
        return (int) l;
    }
}
