package com.jjkeller.kmbapi.eobrengine.eobrdatatypes;

public class Eobr_Driver_Event_Packet {
    private byte bCmd;
    private short wCrc;
    private byte bLen;
    private long startTimeCode;		// msec since Unix Epoch
    private long endTimeCode;		// msec since Unix Epoch
    private int eventMask;  	// bit-wise mask for event types to include in request (used instead of eventType)
    // e.g.  IGN_OFF (2), IGN_ON (3), RESET (5) would be '0...0101100' binary or 0x0000002c or 44 decimal

    private byte searchMethod; // 0: only include events with Driver Ids, >0: include all events

    public byte getCmd() {
        return bCmd;
    }
    public void setCmd(byte bCmd) {
        this.bCmd = bCmd;
    }

    public short getCrc() {
        return wCrc;
    }
    public void setCrc(short wCrc) {
        this.wCrc = wCrc;
    }

    public byte getLen() {
        return bLen;
    }
    public void setLen(byte bLen) {
        this.bLen = bLen;
    }

    public long getStartTimeCode() {
        return startTimeCode;
    }
    public void setStartTimeCode(long startTimeCode) {
        this.startTimeCode = startTimeCode;
    }

    public long getEndTimeCode() {
        return endTimeCode;
    }
    public void setEndTimeCode(long endTimeCode) {
        this.endTimeCode = endTimeCode;
    }

    public int getEventMask() {
        return eventMask;
    }
    public void setEventMask(int eventMask) {
        this.eventMask = eventMask;
    }

    public byte getSearchMethod() {
        return searchMethod;
    }

    public void setSearchMethod(byte searchMethod) {
        this.searchMethod = searchMethod;
    }
}
