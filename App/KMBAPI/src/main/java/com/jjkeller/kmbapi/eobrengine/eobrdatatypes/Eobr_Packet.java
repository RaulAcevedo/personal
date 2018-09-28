package com.jjkeller.kmbapi.eobrengine.eobrdatatypes;

import com.jjkeller.kmbapi.eobrengine.EobrEngineBase;

public class Eobr_Packet 
{
	private byte bCmd;
	private short wCrc;
	private byte bLen;
	private byte[] abData = new byte[EobrEngineBase.EOBR_PAYLOAD_SIZE];
	
	public Eobr_Packet(){}
	
	public byte getCmd()
	{
		return this.bCmd;
	}
	public void setCmd(byte cmd)
	{
		this.bCmd = cmd;
	}

	public short getCrc()
	{
		return this.wCrc;
	}
	public void setCrc(short crc)
	{
		this.wCrc = crc;
	}

	public byte getLen()
	{
		return this.bLen;
	}
	public void setLen(byte len)
	{
		this.bLen = len;
	}

	public byte[] getData()
	{
		return this.abData;
	}
	public void setData(byte[] data)
	{
		this.abData = data;
	}
}
