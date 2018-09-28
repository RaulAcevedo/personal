package com.jjkeller.kmbapi.geotabengine.events;

import com.jjkeller.kmbapi.HosMessageProcessor.interfaces.IHOSMessage;
import com.jjkeller.kmbapi.enums.DatabusTypeEnum;
import com.jjkeller.kmbapi.kmbeobr.EventRecord;
import com.jjkeller.kmbapi.kmbeobr.EventTypeEnum;
import com.jjkeller.kmbapi.kmbeobr.StatusRecord;
import com.jjkeller.kmbapi.kmbeobr.TripReport;

public class DriveOffEventRecord extends EventRecordBase {

    public DriveOffEventRecord(IHOSMessage ihosMessage, GeoTabSyntheticEventRecordData syntheticEventRecordData, long timestamp) {
        super(ihosMessage, syntheticEventRecordData);
        this.setEventType(EventTypeEnum.DRIVEEND);
        this.setTimecode(timestamp);
    }
}

