package com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states;

import com.jjkeller.kmbapi.HosMessageProcessor.interfaces.IHOSMessage;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.SharedState;
import com.jjkeller.kmbapi.geotabengine.events.GeoTabSyntheticEventRecordData;
import com.jjkeller.kmbapi.geotabengine.events.GpsErrorRecord;
import com.jjkeller.kmbapi.kmbeobr.DataFlagEnums;
import com.jjkeller.kmbapi.kmbeobr.EventRecord;

import java.util.ArrayList;
import java.util.List;

import static com.jjkeller.kmbapi.kmbeobr.Constants.MILES_TO_METERS;

public class MandateGPSError {

    private IHOSMessage gpsUncertaintyExceededMessage = null;
    private SharedState _sharedState;
    protected static final float MAX_VALID_UNCERTAINTY_METERS = 5f * MILES_TO_METERS;

    public MandateGPSError(SharedState sharedState) { _sharedState = sharedState; }

    public boolean hasExceededGpsUncertainty() {return this.gpsUncertaintyExceededMessage != null;}

    public List<EventRecord> processMessage(IHOSMessage message) {
        List<EventRecord> result = new ArrayList<>();

        if(message == null)
            return result;

        // When Gps Uncertainty is calculated here, we need to evaluate if an EventRecord needs to be created for the GPS Error
        if(!hasExceededGpsUncertainty() && message.getGpsUncertDistance() >= MAX_VALID_UNCERTAINTY_METERS)
        {
            gpsUncertaintyExceededMessage = message;
            result.add(new GpsErrorRecord(message,
                    new GeoTabSyntheticEventRecordData(_sharedState.getCurrentThresholds().getDriverIdCRC()),
                    DataFlagEnums.GpsEventFlags.GPS_FAULT));
        }

        if(hasExceededGpsUncertainty() && message.getGpsUncertDistance() < MAX_VALID_UNCERTAINTY_METERS) {
            gpsUncertaintyExceededMessage = null;
            result.add(new GpsErrorRecord(message,
                    new GeoTabSyntheticEventRecordData(_sharedState.getCurrentThresholds().getDriverIdCRC()),
                    null));
        }
        return result;
    }

}
