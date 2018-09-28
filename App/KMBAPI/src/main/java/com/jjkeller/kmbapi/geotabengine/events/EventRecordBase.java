package com.jjkeller.kmbapi.geotabengine.events;

import com.jjkeller.kmbapi.HosMessageProcessor.interfaces.IHOSMessage;
import com.jjkeller.kmbapi.enums.DatabusTypeEnum;
import com.jjkeller.kmbapi.kmbeobr.EventRecord;
import com.jjkeller.kmbapi.kmbeobr.EventTypeEnum;
import com.jjkeller.kmbapi.kmbeobr.StatusRecord;
import com.jjkeller.kmbapi.kmbeobr.TripReport;

public class EventRecordBase extends EventRecord {
    public EventRecordBase(IHOSMessage ihosMessage, GeoTabSyntheticEventRecordData syntheticEventRecordData){
        this(ihosMessage, ihosMessage, syntheticEventRecordData);
    }

    public EventRecordBase(IHOSMessage ihosMessage, IHOSMessage tripReportSource, GeoTabSyntheticEventRecordData syntheticEventRecordData) {

        this.setTimecode(ihosMessage.getTimestampUtc().getMillis());
        this.setEventType(EventTypeEnum.ANYTYPE);
        this.setIsVehicleInMotion(ihosMessage.getSpeedometer() >= 0);
        this.setDriverId(syntheticEventRecordData.getDriverIdCrc());
        this.setEobrId(ihosMessage.getSerialNumberCrc());
        this.setEventData(syntheticEventRecordData.getEventData());

        StatusRecord statusRecord = new StatusRecord();
        statusRecord.setTimestampUtc(ihosMessage.getTimestampUtc().toDate());
        statusRecord.setActiveBusType(DatabusTypeEnum.UNKNOWN);
        statusRecord.setGpsLatitude(ihosMessage.getGpsLatitude());
        statusRecord.setGpsLongitude(ihosMessage.getGpsLongitude());
        statusRecord.setGpsUncertDistance(ihosMessage.getGpsUncertDistance());
        statusRecord.setOdometerReading(ihosMessage.getOdometer());
        statusRecord.setIsEngineOn(ihosMessage.isEngineActivityDetected());

        TripReport tripReport = new TripReport();
        tripReport.setDriverId(syntheticEventRecordData.getDriverIdCrc());
        tripReport.setLatitude(tripReportSource.getGpsLatitude());
        tripReport.setLongitude(tripReportSource.getGpsLongitude());
        tripReport.setOdometer(tripReportSource.getOdometer());
        tripReport.setRuntime((int) tripReportSource.getEngineHours());
        tripReport.setTripDist(tripReportSource.getTripOdometer());
        tripReport.setDataTimecode(tripReportSource.getTimestampUtc().getMillis());
        tripReport.setTimecode(ihosMessage.getTimestampUtc().getMillis());
        tripReport.setFixUncert((short) ihosMessage.getGpsUncertDistance());

        this.setStatusRecordData(statusRecord);
        this.setTripReportData(tripReport);
    }
}
