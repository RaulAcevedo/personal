package com.jjkeller.kmbapi.HosMessageProcessor.stateMachine;

import com.jjkeller.kmbapi.HosMessageProcessor.interfaces.IHOSMessage;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.MandateGPSError;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.State;
import com.jjkeller.kmbapi.kmbeobr.Thresholds;

/**
 * Created by ief5781 on 9/8/16.
 */
public class SharedState {
    private Thresholds currentThresholds;
    private IHOSMessage moveStartMessage = null;
    private IHOSMessage validGpsFixMessage = null;
    private MandateGPSError gpsErrorState = new MandateGPSError(this);
    private boolean inDriveOnState = false;
    private boolean isInErrorState = false;

    public IHOSMessage getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(IHOSMessage lastMessage) {
        this.lastMessage = lastMessage;
    }

    private IHOSMessage lastMessage = null;

    public boolean isInErrorState() {
        return isInErrorState;
    }

    public void setInErrorState(boolean inErrorState) {
        isInErrorState = inErrorState;
    }

    public Thresholds getCurrentThresholds() {
        return currentThresholds;
    }
    public void setThresholds(Thresholds currentThresholds) {
        this.currentThresholds = currentThresholds;
    }

    public IHOSMessage getMoveStartMessage() {
        return moveStartMessage;
    }

    public void setMoveStartMessage(IHOSMessage moveStartMessage) {
        this.moveStartMessage = moveStartMessage;
    }

    public IHOSMessage getValidGpsFixMessage() {
        return validGpsFixMessage;
    }

    public void setValidGpsFixMessage(IHOSMessage validGpsFixMessage) {
        this.validGpsFixMessage = validGpsFixMessage;
    }

    public boolean getIsInDriveOnState() {
        return inDriveOnState;
    }

    public void setInDriveOnState(boolean inDriveOnState) {
        this.inDriveOnState = inDriveOnState;
    }

    public MandateGPSError getGpsErrorState() {return gpsErrorState;}
}
