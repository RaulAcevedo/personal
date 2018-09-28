package com.jjkeller.kmbapi.eobrengine.eobrdatatypes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class EobrLongPacket extends EobrPacketBase {
	private long LongVal;

	public EobrLongPacket(byte[] response){
		setCmd(response[0]);
		setSize(response[1]);

		ByteBuffer buffer = ByteBuffer.wrap(response, 2, 9);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		setLongVal(buffer.getLong());
	}

	public long getLongVal()
	{
		return this.LongVal;
	}
	public void setLongVal(long longVal)
	{
		this.LongVal = longVal;
	}
}

