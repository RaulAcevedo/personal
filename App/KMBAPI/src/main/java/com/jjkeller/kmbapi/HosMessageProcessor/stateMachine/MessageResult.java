package com.jjkeller.kmbapi.HosMessageProcessor.stateMachine;

import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.State;
import com.jjkeller.kmbapi.kmbeobr.EventRecord;

import java.util.ArrayList;

/**
 * Created by ief5781 on 8/31/16.
 */
public class MessageResult {
    private State newState;
    private ArrayList<EventRecord> eventRecords = new ArrayList<>();

    public State getNewState() {
        return newState;
    }

    public void setNewState(State newState) {
        this.newState = newState;
    }

    public ArrayList<EventRecord> getEventRecords() {
        return eventRecords;
    }

    public void setEventRecord(ArrayList<EventRecord> eventRecords) {
        this.eventRecords = eventRecords;
    }
}
