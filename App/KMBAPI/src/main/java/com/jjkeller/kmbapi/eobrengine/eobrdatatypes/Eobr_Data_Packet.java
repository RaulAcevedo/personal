package com.jjkeller.kmbapi.eobrengine.eobrdatatypes;

public class Eobr_Data_Packet 
{
	private byte bCmd;
	private short wCrc;
	private byte bLen;
	private byte bMethod;
	private int recordIdUnion;  // either record id or block number
	private byte bYearUnion;	// either year or page number
	private byte bMonthUnion;	// either month or upperPage1LowerPage0
	private byte bDayUnion;		// either day or entryNumber
	private byte bHour;
	private byte bMinute;
	private byte bSecond;
	private byte bTimeOrMotionOption;
	private byte bRefTimestampOption;
	
	public Eobr_Data_Packet(){}
	
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
	
	public int getRecordIdUnion() {
		return recordIdUnion;
	}
	public void setRecordIdUnion(int recordIdUnion) {
		this.recordIdUnion = recordIdUnion;
	}
		
	public byte getYearUnion() {
		return bYearUnion;
	}
	public void setYearUnion(byte bYearUnion) {
		this.bYearUnion = bYearUnion;
	}
			
	public byte getMonthUnion() {
		return bMonthUnion;
	}
	public void setMonthUnion(byte bMonthUnion) {
		this.bMonthUnion = bMonthUnion;
	}
	
	public byte getDayUnion() {
		return bDayUnion;
	}
	public void setDayUnion(byte bDayUnion) {
		this.bDayUnion = bDayUnion;
	}
	
	public byte getHour() {
		return bHour;
	}
	public void setHour(byte bHour) {
		this.bHour = bHour;
	}
	
	public byte getMinute() {
		return bMinute;
	}
	public void setMinute(byte bMinute) {
		this.bMinute = bMinute;
	}
	
	public byte getSecond() {
		return bSecond;
	}
	public void setSecond(byte bSecond) {
		this.bSecond = bSecond;
	}
	
	public byte getTimeOrMotionOption() {
		return bTimeOrMotionOption;
	}
	public void setTimeOrMotionOption(byte bTimeOrMotionOption) {
		this.bTimeOrMotionOption = bTimeOrMotionOption;
	}
	
	public byte getRefTimestampOption() {
		return bRefTimestampOption;
	}
	public void setRefTimestampOption(byte bRefTimestampOption) {
		this.bRefTimestampOption = bRefTimestampOption;
	}
}
