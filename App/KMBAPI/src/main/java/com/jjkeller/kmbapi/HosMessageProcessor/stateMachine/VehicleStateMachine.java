package com.jjkeller.kmbapi.HosMessageProcessor.stateMachine;

import android.util.Log;

import com.jjkeller.kmbapi.HosMessageProcessor.interfaces.IHOSMessage;
import com.jjkeller.kmbapi.HosMessageProcessor.interfaces.IVehicleStateMachine;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.AobrdEngineOff;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.MandateEngineOffNotDriving;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.State;
import com.jjkeller.kmbapi.common.LogCat;
import com.jjkeller.kmbapi.featuretoggle.IFeatureToggleService;
import com.jjkeller.kmbapi.kmbeobr.EventRecord;
import com.jjkeller.kmbapi.kmbeobr.Thresholds;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Date;


/**
 * Created by ief5781 on 8/31/16.
 */
public class VehicleStateMachine implements IVehicleStateMachine {
    private State currentState;
    private IFeatureToggleService featureToggleService;
    private SharedState sharedState;

    private static final String TAG = "VehicleStateMachine";

    public VehicleStateMachine(IFeatureToggleService featureToggleService, Thresholds thresholds) {
        sharedState = new SharedState();
        setThresholds(thresholds);

        this.featureToggleService = featureToggleService;

        resetVehicleState();
    }

    public void resetVehicleState(){

        //Clear Error state before reset
        if(sharedState != null){
            sharedState.setInErrorState(false);
        }

        if(this.featureToggleService.getIsEldMandateEnabled()) {

            currentState = new MandateEngineOffNotDriving(sharedState);
        }
        else {
            currentState = new AobrdEngineOff(sharedState);
        }
    }

    @Override
    public void setThresholds(Thresholds thresholds) {

        if(sharedState.getCurrentThresholds() != null && !sharedState.getCurrentThresholds().getDriverId().equals(thresholds.getDriverId())
                && !sharedState.getCurrentThresholds().getDriverId().isEmpty() && !thresholds.getDriverId().isEmpty() ){
            resetVehicleState();
        }

        sharedState.setThresholds(thresholds);
    }

    @Override
    public ArrayList<EventRecord> processMessage(IHOSMessage message) {
        IHOSMessage moveStartMessage = sharedState.getMoveStartMessage();
        float odometer = moveStartMessage == null ? -1 : moveStartMessage.getOdometer();
        DateTime timestamp = moveStartMessage == null ? null : moveStartMessage.getTimestampUtc();

        MessageResult result = currentState.processMessage(message);

        if(result.getNewState() != null) {
            currentState = result.getNewState();

            LogCat.getInstance().d(TAG, getLogString(message));
            LogCat.getInstance().d(TAG, String.format("Transitioning to state %s", currentState.getClass().getName()));
            LogCat.getInstance().d(TAG, String.format("Previous shared odometer: %s, timestamp: %s", odometer, timestamp));

            moveStartMessage = sharedState.getMoveStartMessage();
            odometer = moveStartMessage == null ? -1 : moveStartMessage.getOdometer();
            timestamp = moveStartMessage == null ? null : moveStartMessage.getTimestampUtc();

            LogCat.getInstance().d(TAG, String.format("Current shared odometer: %s, timestamp: %s", odometer, timestamp));

            String eventTypes = "";
            for(EventRecord event : result.getEventRecords()) {
                eventTypes += String.format(" %d", event.getEventType());
            }

            LogCat.getInstance().d(TAG, String.format("Created events: %s", eventTypes));
        }

        return result.getEventRecords();
    }

    private String getLogString(IHOSMessage message) {
        return String.format("Time: %s\nOdometer: %s\nSpeed: %s\nTach: %s\nLat: %s\nLon: %s\nSerNo: %s\nEngHrs: %s\nTripEngSec: %s\nTripOdo: %s",
                message.getTimestampUtc(),
                message.getOdometer(),
                message.getSpeedometer(),
                message.getTachometer(),
                message.getGpsLatitude(),
                message.getGpsLongitude(),
                message.getSerialNumberCrc(),
                message.getEngineHours(),
                message.getTripEngineSeconds(),
                message.getTripOdometer()
        );
    }
}
