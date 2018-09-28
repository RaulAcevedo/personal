package com.jjkeller.kmbapi.eobrengine.eobrdatatypes;

import com.jjkeller.kmbapi.eobrengine.EobrEngineBase;

public class Eobr_FW_Block_Packet 
{
	private byte bCmd;
	private short wCrc;
	private byte bLen;
	private byte bFWType;
	private byte[] abData = new byte[EobrEngineBase.EOBR_FW_BLOCK_CODE_SIZE + EobrEngineBase.EOBR_FW_BLOCK_CODE_ADDRESS_SIZE];
	
	public Eobr_FW_Block_Packet(){}
	
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

	public byte getFWType()
	{
		return this.bFWType;
	}
	public void setFWType(byte val)
	{
		this.bFWType = val;
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

