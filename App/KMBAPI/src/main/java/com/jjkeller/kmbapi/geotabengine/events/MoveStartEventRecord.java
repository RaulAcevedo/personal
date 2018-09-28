package com.jjkeller.kmbapi.geotabengine.events;

import com.jjkeller.kmbapi.HosMessageProcessor.interfaces.IHOSMessage;
import com.jjkeller.kmbapi.enums.DatabusTypeEnum;
import com.jjkeller.kmbapi.kmbeobr.EventRecord;
import com.jjkeller.kmbapi.kmbeobr.EventTypeEnum;
import com.jjkeller.kmbapi.kmbeobr.StatusRecord;
import com.jjkeller.kmbapi.kmbeobr.TripReport;

public class MoveStartEventRecord extends EventRecordBase {

    public MoveStartEventRecord(IHOSMessage ihosMessage, GeoTabSyntheticEventRecordData syntheticEventRecordData) {
        super(ihosMessage, syntheticEventRecordData);
        this.setEventType(EventTypeEnum.MOVE);
    }
}
