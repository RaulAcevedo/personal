package com.jjkeller.kmbapi.HosMessageProcessor.utility;

import com.jjkeller.kmbapi.kmbeobr.TripReport;
import com.jjkeller.kmbapi.proxydata.GeotabHOSData;

import static java.lang.Math.round;

/**
 * Created by jhm2586 on 9/28/2016.
 */

public class GeotabHosDataToTripRecordUtility {

    public static void updateTripReportFromGeotabHosData(TripReport tripReport, GeotabHOSData data)
    {
        if(data == null) return;

        tripReport.setTimecode(data.getTimestampUtc().getMillis());
        tripReport.setOdometer(data.getOdometer());
        tripReport.setTripDist(data.getTripOdometer());
        tripReport.setTripSecs(round(data.getTripEngineSeconds()));
        tripReport.setLatitude(data.getGpsLatitude());
        tripReport.setLongitude(data.getGpsLongitude());
        tripReport.setDriverId(data.getDriverId());
        tripReport.setDataTimecode(data.getTimestampUtc().getMillis());
    }
}
