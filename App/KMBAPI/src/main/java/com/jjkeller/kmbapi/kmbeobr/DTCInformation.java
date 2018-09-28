package com.jjkeller.kmbapi.kmbeobr;

public class DTCInformation
{
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
	
	public int getDtc()
	{
		return dtc;
	}
	public void setDtc(int dtc)
	{
		this.dtc = dtc;
	}
}