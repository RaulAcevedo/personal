package com.jjkeller.kmbapi.eobrengine.eobrdatatypes;

import com.jjkeller.kmbapi.eobrengine.LittleEndianHelper;

public class EobrShortPacket extends EobrPacketBase {
	private short shortVal;

	public EobrShortPacket(byte[] response) {
		setCmd(response[0]);
		setSize(response[1]);

		setShortVal(LittleEndianHelper.Companion.getShort(response, 2, 2));
	}

	public short getShortVal()
	{
		return this.shortVal;
	}
	public void setShortVal(short shortVal)
	{
		this.shortVal = shortVal;
	}
}
