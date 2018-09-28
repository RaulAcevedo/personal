package com.jjkeller.kmbapi.eobrengine.eobrdatatypes;

import com.jjkeller.kmbapi.eobrengine.LittleEndianHelper;

public class EobrIntegerPacket extends EobrPacketBase {
	private int bIntegerVal;

	public EobrIntegerPacket(byte[] response){
		setCmd(response[0]);
		setSize(response[1]);

		setIntegerVal(LittleEndianHelper.Companion.getInt(response, 2, 4));
	}

	public int getIntegerVal()
	{
		return this.bIntegerVal;
	}

	public void setIntegerVal(int integerVal)
	{
		this.bIntegerVal = integerVal;
	}
}
