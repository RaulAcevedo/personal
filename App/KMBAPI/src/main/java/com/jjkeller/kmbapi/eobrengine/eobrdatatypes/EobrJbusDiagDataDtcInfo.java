package com.jjkeller.kmbapi.eobrengine.eobrdatatypes;

public class EobrJbusDiagDataDtcInfo {
	public static final int TYPE_OFFSET = 0;
	public static final int TYPE_LENGTH = 1;
	public static final int SOURCE_OFFSET = 1;
	public static final int SOURCE_LENGTH = 4;
	public static final int DTC_OFFSET = SOURCE_OFFSET + SOURCE_LENGTH;
	public static final int DTC_LENGTH = 4;
	public static final int SIZE = DTC_OFFSET + DTC_LENGTH;
	
	private short type;
	private int source;
	private int dtc;
	
	public short getType()
	{
		return type;
	}
	public void setType(short type)
	{
		this.type = type;
	}
	
	public int getSource()
	{
		return source;
	}
	public void setSource(int source)
	{
		this.source = source;
	}
	
	public int getDTC()
	{
		return dtc;
	}
	public void setDTC(int dtc)
	{
		this.dtc = dtc;
	}
}
