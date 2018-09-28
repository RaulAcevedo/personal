package com.jjkeller.kmbapi.eobrengine.eobrdatatypes;

public class EobrBytePacket extends EobrPacketBase {
	private byte byteVal;

	public EobrBytePacket(byte[] response){
		setCmd(response[0]);
		setSize(response[1]);
		setByteVal(response[2]);
	}

	public byte getByteVal()
	{
		return this.byteVal;
	}
	public void setByteVal(byte byteVal)
	{
		this.byteVal = byteVal;
	}		
}

