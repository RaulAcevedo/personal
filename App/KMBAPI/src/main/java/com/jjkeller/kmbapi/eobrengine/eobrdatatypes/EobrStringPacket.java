package com.jjkeller.kmbapi.eobrengine.eobrdatatypes;

import com.jjkeller.kmbapi.eobrengine.EobrEngineBase;

public class EobrStringPacket extends EobrPacketBase {
	private byte[] aStringVal = new byte[EobrEngineBase.EOBR_PAYLOAD_SIZE];

	public static EobrStringPacket build(byte[] response) {
		int stringSize = response[1] & 0xFF; //anding with FF to make it interpret the byte as unsigned
		if(stringSize <= 0)	return null;

		byte[] data = new byte[stringSize];

		EobrStringPacket eobrStringPacket = new EobrStringPacket();
		eobrStringPacket.setCmd(response[0]);
		eobrStringPacket.setSize(response[1]);

		for (int i=0; i<stringSize; i++)
		{
			data[i] = response[i+2];
		}

		eobrStringPacket.setStringVal(data);

		return eobrStringPacket;
	}

	public byte[] getStringVal()
	{
		return this.aStringVal;
	}
	public void setStringVal(byte[] stringVal)
	{
		this.aStringVal = stringVal;
	}
}
