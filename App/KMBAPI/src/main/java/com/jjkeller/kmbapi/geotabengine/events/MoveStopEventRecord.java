package com.jjkeller.kmbapi.geotabengine.events;

import com.jjkeller.kmbapi.HosMessageProcessor.interfaces.IHOSMessage;
import com.jjkeller.kmbapi.enums.DatabusTypeEnum;
import com.jjkeller.kmbapi.kmbeobr.EventRecord;
import com.jjkeller.kmbapi.kmbeobr.EventTypeEnum;
import com.jjkeller.kmbapi.kmbeobr.StatusRecord;
import com.jjkeller.kmbapi.kmbeobr.TripReport;

/**
 * Created by jld5296 on 9/28/16.
 */
public class MoveStopEventRecord extends EventRecordBase {

    public MoveStopEventRecord(IHOSMessage ihosMessage, GeoTabSyntheticEventRecordData syntheticEventRecordData) {
        super(ihosMessage, syntheticEventRecordData);
        this.setEventType(EventTypeEnum.VEHICLESTOPPED);
    }
}


