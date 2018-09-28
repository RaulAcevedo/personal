package com.jjkeller.kmbapi.geotabengine.events;

import com.jjkeller.kmbapi.HosMessageProcessor.interfaces.IHOSMessage;
import com.jjkeller.kmbapi.kmbeobr.DataFlagEnums;
import com.jjkeller.kmbapi.kmbeobr.EventTypeEnum;

/**
 * Created by rab5795 on 4/13/18.
 */

public class ErrorRecord extends EventRecordBase {
    public ErrorRecord (IHOSMessage ihosMessage, GeoTabSyntheticEventRecordData syntheticEventRecordData, boolean isIgnitionOff){
        super(ihosMessage, syntheticEventRecordData);
        this.setEventType(EventTypeEnum.ERROR);

        int odo = ihosMessage.isOdometerFromEngine() ? 0 : 1;
        int vss = ihosMessage.isSpeedFromEngine() ? 0 : 1;

        this.setEventData(isIgnitionOff ? 0 : (vss << DataFlagEnums.ErrorEventFlags.VSS_FAULT.getIndex()) | (odo << DataFlagEnums.ErrorEventFlags.ODO_FAULT.getIndex()));
    }
}
