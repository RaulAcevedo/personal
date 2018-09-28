package com.jjkeller.kmbapi.geotabengine.events;

import com.jjkeller.kmbapi.HosMessageProcessor.interfaces.IHOSMessage;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.SharedState;
import com.jjkeller.kmbapi.enums.DatabusTypeEnum;
import com.jjkeller.kmbapi.kmbeobr.EventRecord;
import com.jjkeller.kmbapi.kmbeobr.EventTypeEnum;
import com.jjkeller.kmbapi.kmbeobr.StatusRecord;
import com.jjkeller.kmbapi.kmbeobr.TripReport;

/**
 * Specific Instance of EventRecord representing a IgnOn
 */
public class IgnOnEventRecord extends EventRecordBase {


    public IgnOnEventRecord(IHOSMessage ihosMessage, GeoTabSyntheticEventRecordData syntheticEventRecordData) {
        super(ihosMessage, syntheticEventRecordData);
        this.setEventType(EventTypeEnum.IGNITIONON);
    }
}
