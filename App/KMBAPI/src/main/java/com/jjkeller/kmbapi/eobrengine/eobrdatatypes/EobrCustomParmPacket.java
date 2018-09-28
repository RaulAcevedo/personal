package com.jjkeller.kmbapi.eobrengine.eobrdatatypes;

import com.jjkeller.kmbapi.eobrengine.LittleEndianHelper;

public class EobrCustomParmPacket extends EobrPacketBase {
	private byte bStatus;
	private int customParam;

	public EobrCustomParmPacket(byte[] response){
		setCmd(response[0]);
		setSize(response[1]);
		setStatus(response[2]);

		setCustomParam(LittleEndianHelper.Companion.getInt(response, 3, 4));
	}

	public byte getStatus()
	{
		return this.bStatus;
	}
	public void setStatus(byte status)
	{
		this.bStatus = status;
	}

	public int getCustomParam()
	{
		return this.customParam;
	}
	public void setCustomParam(int customParam)
	{
		this.customParam = customParam;
	}
}
