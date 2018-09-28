package com.jjkeller.kmbapi.HosMessageProcessor.interfaces;

import android.content.Context;
import android.os.Bundle;

import com.jjkeller.kmbapi.CodeBlocks;
import com.jjkeller.kmbapi.kmbeobr.EobrReferenceTimestamps;
import com.jjkeller.kmbapi.kmbeobr.EventRecord;
import com.jjkeller.kmbapi.kmbeobr.EventTypeEnum;
import com.jjkeller.kmbapi.kmbeobr.HistogramData;
import com.jjkeller.kmbapi.kmbeobr.HistogramTypeEnum;
import com.jjkeller.kmbapi.kmbeobr.StatusRecord;
import com.jjkeller.kmbapi.kmbeobr.StatusRecordQueryMethodEnum;
import com.jjkeller.kmbapi.kmbeobr.Thresholds;
import com.jjkeller.kmbapi.kmbeobr.TripDistanceHours;
import com.jjkeller.kmbapi.kmbeobr.TripReport;

import java.util.ArrayList;


/**
 * Created by ief5781 on 8/31/16.
 */
public interface IHosMessageProcessor {
    ArrayList<EventRecord> processHosMessage(IHOSMessage hosMessage);
    void resetVehicleState();

    //not accepting motion option parameter at this time - that's GenI only
    void getEobrData(Context ctx, StatusRecord statusRecord, StatusRecordQueryMethodEnum queryMethod, int recordId, long timestamp, boolean setRefTime);
    void getEventData(Context ctx, EventRecord eventRecord, StatusRecordQueryMethodEnum queryMethod, int recordId, long timestamp, EventTypeEnum eventType, boolean setRefTime);
    void getEventData(Context ctx, EventRecord eventRecord, StatusRecordQueryMethodEnum queryMethod, int recordId, long timestamp, EventTypeEnum eventType, boolean setRefTime, int eventMask);
    void getTripData(Context ctx, TripReport tripReport, StatusRecordQueryMethodEnum queryMethod, int recordId, long timestamp, boolean setRefTime);
    void getHistogramData(Context ctx, HistogramData histogramData, StatusRecordQueryMethodEnum queryMethod, int recordId, long timestamp, HistogramTypeEnum histogramType, boolean setRefTime);
    TripDistanceHours getTripDistanceHours(long timestamp);

    EobrReferenceTimestamps getReferenceTimestamp();
    void setReferenceTimestamp(EobrReferenceTimestamps referenceTimestamps);

    void setThresholdValues(Thresholds thresholds);

    void clearAllRecordData(int clearFlags);
    void clearAllEobrData();
}
