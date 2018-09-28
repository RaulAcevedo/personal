package com.jjkeller.kmbapi.HosMessageProcessor;

import android.content.Context;

import com.jjkeller.kmbapi.HosMessageProcessor.interfaces.IHOSMessage;
import com.jjkeller.kmbapi.HosMessageProcessor.interfaces.IHosMessageProcessor;
import com.jjkeller.kmbapi.HosMessageProcessor.interfaces.IVehicleStateMachine;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.VehicleStateMachine;
import com.jjkeller.kmbapi.common.TimeKeeper;
import com.jjkeller.kmbapi.eobrengine.eobrreader.exceptions.FunctionNotImplmentedEobrException;
import com.jjkeller.kmbapi.featuretoggle.IFeatureToggleService;
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
import java.util.Date;

public class HosMessageProcessor implements IHosMessageProcessor {
    protected IFeatureToggleService featureToggleService;
    protected IVehicleStateMachine vehicleStateMachine;

    protected EobrReferenceTimestamps referenceTimestamps = new EobrReferenceTimestamps();
    protected Thresholds initialThresholds = new Thresholds();

    public HosMessageProcessor(IFeatureToggleService featureToggleService, Thresholds thresholds) {
        this(featureToggleService, new VehicleStateMachine(featureToggleService, thresholds));

        this.setThresholdValues(thresholds);
    }

    public HosMessageProcessor(IFeatureToggleService featureToggleService, IVehicleStateMachine vehicleStateMachine) {
        this.featureToggleService = featureToggleService;
        this.vehicleStateMachine = vehicleStateMachine;
    }

    @Override
    public ArrayList<EventRecord> processHosMessage(IHOSMessage hosMessage) {
        return vehicleStateMachine.processMessage(hosMessage);
    }

    @Override
    public void resetVehicleState() {
        vehicleStateMachine.resetVehicleState();
    }

    @Override
    public void getEobrData(Context ctx, StatusRecord statusRecord, StatusRecordQueryMethodEnum queryMethod, int recordId, long timestamp, boolean setRefTime) {

    }

    @Override
    public void getEventData(Context ctx, EventRecord eventRecord, StatusRecordQueryMethodEnum queryMethod, int recordId, long timestamp, EventTypeEnum eventType, boolean setRefTime) {

    }

    @Override
    public void getEventData(Context ctx, EventRecord eventRecord, StatusRecordQueryMethodEnum queryMethod, int recordId, long timestamp, EventTypeEnum eventType, boolean setRefTime, int eventMask) {

    }

    @Override
    public void getTripData(Context ctx, TripReport tripReport, StatusRecordQueryMethodEnum queryMethod, int recordId, long timestamp, boolean setRefTime) {
        //TODO: implement
        switch(queryMethod.getValue())
        {
            case StatusRecordQueryMethodEnum.TIMESTAMP:
                break;
            default:
                break;
        }

        //TODO: Handle reference timestamp - just test code
        if(setRefTime)
            this.referenceTimestamps.setTripReferenceTime(TimeKeeper.getInstance().now().getTime());

    }

    @Override
    public void getHistogramData(Context ctx, HistogramData histogramData, StatusRecordQueryMethodEnum queryMethod, int recordId, long timestamp, HistogramTypeEnum histogramType, boolean setRefTime) {
        //Not implementing this functionality.  The app reads this data but throws it away.

        if(setRefTime)
            this.referenceTimestamps.setHistogramReferenceTime(TimeKeeper.getInstance().now().getTime());
    }

    @Override
    public TripDistanceHours getTripDistanceHours(long timestamp) {
        return new TripDistanceHours();
    }

    @Override
    public EobrReferenceTimestamps getReferenceTimestamp() {
        return referenceTimestamps;
    }

    @Override
    public void setReferenceTimestamp(EobrReferenceTimestamps referenceTimestamps) {
        if(this.referenceTimestamps == null)
            this.referenceTimestamps = referenceTimestamps;
        else
            this.referenceTimestamps.updateTimestampsFrom(referenceTimestamps);

    }

    @Override
    public void setThresholdValues(Thresholds thresholds) {
        vehicleStateMachine.setThresholds(thresholds);
        initialThresholds = thresholds;
    }

    @Override
    public void clearAllRecordData(int clearFlags) {
        throw new FunctionNotImplmentedEobrException();
    }

    @Override
    public void clearAllEobrData() {
        vehicleStateMachine = new VehicleStateMachine(featureToggleService, initialThresholds);
    }

}
