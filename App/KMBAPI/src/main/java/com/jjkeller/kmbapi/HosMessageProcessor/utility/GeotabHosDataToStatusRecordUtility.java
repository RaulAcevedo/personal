package com.jjkeller.kmbapi.HosMessageProcessor.utility;

import com.jjkeller.kmbapi.kmbeobr.StatusRecord;
import com.jjkeller.kmbapi.proxydata.GeotabHOSData;

/**
 * Created by jhm2586 on 9/28/2016.
 */

public class GeotabHosDataToStatusRecordUtility {

    public static void UpdateStatusRecordFromGeotabHosData(StatusRecord statusRecord, GeotabHOSData data)
    {
        if(data == null) return;

        statusRecord.setRecordId(safeLongToInt(data.getPrimaryKey()));
        statusRecord.setTimestampUtc(data.getTimestampUtc().toDate());
        statusRecord.setIsEngineOn(data.isEngineActivityDetected()); // Should this be data.isIgnitionOn()?
        statusRecord.setSpeedometerReading(data.getSpeedometer());
        statusRecord.setOdometerReading(data.getOdometer());
        statusRecord.setTachometer(data.getTachometer());
        statusRecord.setGpsTimestampUtc(data.getTimestampUtc().toDate());
        statusRecord.setGpsLatitude(data.getGpsLatitude());
        statusRecord.setGpsLongitude(data.getGpsLongitude());
        statusRecord.setGpsUncertDistance(data.getGpsUncertDistance());
        statusRecord.setTripEngineSeconds(data.getTripEngineSeconds());
        statusRecord.setTripOdometer(data.getTripOdometer());
    }

    private static int safeLongToInt(long l) {
        if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
            throw new IllegalArgumentException
                    (l + " cannot be cast to int without changing its value.");
        }
        return (int) l;
    }
}
