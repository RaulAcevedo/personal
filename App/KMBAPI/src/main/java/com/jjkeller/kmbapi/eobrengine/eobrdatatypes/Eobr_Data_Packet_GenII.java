package com.jjkeller.kmbapi.eobrengine.eobrdatatypes;

public class Eobr_Data_Packet_GenII 
{
	private byte bCmd;
	private short wCrc;
	private byte bLen;
	private byte bMethod;		// record id or timestamp
	private int recordId;  		// either record id
	private long timeCode;		// msec since Unix Epoch
	private byte bMotionOption;	// next rec or next motion change
	private byte bRefTimestampOption; // reference time option (0 = no op, 1 = set ref timestamp)
	private byte eventType;		// eventtype - used for get event data
	private int eventMask;  	// bit-wise mask for event types to include in request (used instead of eventType)
								// e.g.  IGN_OFF (2), IGN_ON (3), RESET (5) would be '0...0101100' binary or 0x0000002c or 44 decimal
	
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
	
	public byte getMethod() {
		return bMethod;
	}
	public void setMethod(byte bMethod) {
		this.bMethod = bMethod;
	}
	
	public long getTimecode() {
		return timeCode;
	}
	public void setTimecode(long timecode) {
		this.timeCode = timecode;
	}
	
	public int getRecordId() {
		return recordId;
	}
	public void setRecordId(int recordId) {
		this.recordId = recordId;
	}
		
	public byte getMotionOption() {
		return bMotionOption;
	}
	public void setMotionOption(byte bMotionOption) {
		this.bMotionOption = bMotionOption;
	}
	
	public byte getRefTimestampOption() {
		return bRefTimestampOption;
	}
	public void setRefTimestampOption(byte bRefTimestampOption) {
		this.bRefTimestampOption = bRefTimestampOption;
	}
	
	public byte getEventType() {
		return this.eventType;
	}
	public void setEventType(byte eventType) {
		this.eventType = eventType;
	}

	public int getEventMask() {
		return eventMask;
	}
	public void setEventMask(int eventMask) {
		this.eventMask = eventMask;
	}
}
