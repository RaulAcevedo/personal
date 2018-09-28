package com.jjkeller.kmbapi.datatransferfilestatus;

public class DataTransferFileStatusEvent {
    public enum DataTransferFileStatusEventType {
        NO_RESPONSE, SUCCESSFUL, FAILURE
    }
    public DataTransferFileStatusEventType eventType;

    public DataTransferFileStatusEvent(DataTransferFileStatusEventType eventType) {
        this.eventType = eventType;
    }
}
