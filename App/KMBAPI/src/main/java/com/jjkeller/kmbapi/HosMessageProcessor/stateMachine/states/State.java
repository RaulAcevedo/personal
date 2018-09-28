package com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states;

import android.util.EventLog;

import com.jjkeller.kmbapi.HosMessageProcessor.interfaces.IHOSMessage;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.MessageResult;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.SharedState;
import com.jjkeller.kmbapi.geotabengine.events.DriveOffEventRecord;
import com.jjkeller.kmbapi.geotabengine.events.DriveOnEventRecord;
import com.jjkeller.kmbapi.geotabengine.events.ErrorRecord;
import com.jjkeller.kmbapi.geotabengine.events.GeoTabSyntheticEventRecordData;
import com.jjkeller.kmbapi.geotabengine.events.IgnOffEventRecord;
import com.jjkeller.kmbapi.geotabengine.events.IgnOnEventRecord;
import com.jjkeller.kmbapi.geotabengine.events.MoveStartEventRecord;
import com.jjkeller.kmbapi.geotabengine.events.MoveStopEventRecord;
import com.jjkeller.kmbapi.kmbeobr.EventRecord;

import java.util.ArrayList;
import java.util.List;

import static com.jjkeller.kmbapi.kmbeobr.Constants.KM_TO_METERS;

/**
 * Created by ief5781 on 8/31/16.
 */
@SuppressWarnings("DefaultFileTemplate")
public abstract class State {
    // Constants that are not part of the Thesholds object
    protected static final float THRESHOLD_MPH_MOVE = 1.0f;

    protected SharedState sharedState;

    protected State(SharedState sharedState) {
        this.sharedState = sharedState;
    }

    public abstract MessageResult processMessage(IHOSMessage message);

    protected EventRecord createDriveOffEventRecord(IHOSMessage message) {

        saveLastKnownHOSMessageToSharedState(message);

        return createDriveOffEventRecord(message, message.getTimestampUtc().getMillis());
    }

    protected EventRecord createDriveOffEventRecord(IHOSMessage message, long timestamp) {
        sharedState.setInDriveOnState(false);

        saveLastKnownHOSMessageToSharedState(message);
        // Drive_OFF
        return new DriveOffEventRecord(
                message,
                new GeoTabSyntheticEventRecordData(sharedState.getCurrentThresholds().getDriverIdCRC()),
                timestamp);
    }

    protected EventRecord createDriveOnEventRecord(IHOSMessage message, IHOSMessage messageAtEventStart) {
        sharedState.setInDriveOnState(true);

        saveLastKnownHOSMessageToSharedState(message);
        // Drive_ON
        return new DriveOnEventRecord(
                message,
                new GeoTabSyntheticEventRecordData(sharedState.getCurrentThresholds().getDriverIdCRC()),
                messageAtEventStart
        );
    }

    protected EventRecord createMoveStopEventRecord(IHOSMessage message) {

        saveLastKnownHOSMessageToSharedState(message);
        // MOVE_STOP
        return new MoveStopEventRecord(
                message,
                new GeoTabSyntheticEventRecordData(sharedState.getCurrentThresholds().getDriverIdCRC()));
    }

    protected EventRecord createMoveStartEventRecord(IHOSMessage message) {

        saveLastKnownHOSMessageToSharedState(message);
        // MOVE_START
        return new MoveStartEventRecord(
                message,
                new GeoTabSyntheticEventRecordData(sharedState.getCurrentThresholds().getDriverIdCRC()));
    }

    protected EventRecord createIgnOffEventRecord(IHOSMessage message) {

        saveLastKnownHOSMessageToSharedState(message);
        // IGN_OFF
        return new IgnOffEventRecord(
                message,
                new GeoTabSyntheticEventRecordData(sharedState.getCurrentThresholds().getDriverIdCRC()));
    }

    protected EventRecord createIgnOnEventRecord(IHOSMessage message) {

        saveLastKnownHOSMessageToSharedState(message);

        return new IgnOnEventRecord(
                message,
                new GeoTabSyntheticEventRecordData(sharedState.getCurrentThresholds().getDriverIdCRC()));
    }


    protected  EventRecord createErrorEvent(IHOSMessage message){

        saveLastKnownHOSMessageToSharedState(message);

        return new ErrorRecord(message, new GeoTabSyntheticEventRecordData(sharedState.getCurrentThresholds().getDriverIdCRC()), false);
    }

    //This is a special Clear Error event that gets called when we are in the VSS error state and ignition turns off.
    protected EventRecord createErrorClearEvent(IHOSMessage message){
        saveLastKnownHOSMessageToSharedState(message);

        return new ErrorRecord(message, new GeoTabSyntheticEventRecordData(sharedState.getCurrentThresholds().getDriverIdCRC()), true);
    }

    protected MessageResult setStateAndRecurse(State newState, IHOSMessage message, MessageResult result) {
        MessageResult messageResult = newState.processMessage(message);
        result.getEventRecords().addAll(messageResult.getEventRecords());

        if(messageResult.getNewState() != null){
            return  setStateAndRecurse(messageResult.getNewState(), message, result);
        }

        // Update to last state
        result.setNewState(newState);
        return result;
    }

    protected EventRecord createErrorRecord(IHOSMessage message){
        EventRecord eventErrorRecord = null;

        if(sharedState.getLastMessage() != null){
            if((sharedState.getLastMessage().isIgnitionOn() && message.isIgnitionOn()) && message.checkIfEngineSyncFlagsChanged(sharedState.getLastMessage())){
                eventErrorRecord = createErrorEvent(message);
            }
        }else{
            if(message.isIgnitionOn() && (!message.isOdometerFromEngine() || !message.isSpeedFromEngine())){
                eventErrorRecord = createErrorEvent(message);
            }
        }

        return eventErrorRecord;
    }

    protected EventRecord createClearErrorRecordOnIgnOff(IHOSMessage message){
        return createErrorClearEvent(message);
    }

    /**
     * Calculates GPSUncertainty using original odometer (not converted) which is represented
     * as KM. We save the distance as meters.
     * @param currentMessage
     */
    public void calculateGpsUncertDistance(IHOSMessage currentMessage) {
        if(currentMessage != null){
            if(!currentMessage.isGpsValid()) {
                if (sharedState.getValidGpsFixMessage() != null) {
                    currentMessage.setGpsUncertDistance((currentMessage.getOrigOdometer() - sharedState.getValidGpsFixMessage().getOrigOdometer()) * KM_TO_METERS);
                } else {
                    // trip odometer wont reset until engine is off, even if app is closed and re-entered.
                    // set last known valid message to current message
                    sharedState.setValidGpsFixMessage(currentMessage);
                }
            }
        }
    }

    private void saveLastKnownHOSMessageToSharedState(IHOSMessage message){
        if(sharedState.getLastMessage() == null || !message.equals(sharedState.getLastMessage())){
            sharedState.setLastMessage(message);
        }
    }
}
