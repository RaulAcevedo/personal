package com.jjkeller.kmbapi.geotabengine.events;

import com.jjkeller.kmbapi.HosMessageProcessor.interfaces.IHOSMessage;
import com.jjkeller.kmbapi.kmbeobr.DataFlagEnums;
import com.jjkeller.kmbapi.kmbeobr.EventTypeEnum;

public class GpsErrorRecord extends EventRecordBase {
    public GpsErrorRecord(IHOSMessage ihosMessage, GeoTabSyntheticEventRecordData syntheticEventRecordData, DataFlagEnums.GpsEventFlags flags) {
        super(ihosMessage, syntheticEventRecordData);
        this.setEventType(EventTypeEnum.GPS);
        if(flags != null)
            this.setEventData(1 << flags.getIndex());
    }
}
