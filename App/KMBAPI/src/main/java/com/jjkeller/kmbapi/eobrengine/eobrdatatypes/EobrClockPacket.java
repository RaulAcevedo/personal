package com.jjkeller.kmbapi.eobrengine.eobrdatatypes;

public class EobrClockPacket extends EobrPacketBase {
	private int Year;
	private int Month;
	private int Day;
	private int Hour;
	private int Minute;
	private int Second;

	public EobrClockPacket(byte[] response){
		setCmd(response[0]);
		setSize(response[1]);
		setYear(response[2]);
		setMonth(response[3]);
		setDay(response[4]);
		setHour(response[5]);
		setMinute(response[6]);
		setSecond(response[7]);
	}

	public int getYear()
	{
		return this.Year;
	}
	public void setYear(int year)
	{
		this.Year = year;
	}

	public int getMonth()
	{
		return this.Month;
	}
	public void setMonth(int month)
	{
		this.Month = month;
	}

	public int getDay()
	{
		return this.Day;
	}
	public void setDay(int day)
	{
		this.Day = day;
	}

	public int getHour()
	{
		return this.Hour;
	}
	public void setHour(int hour)
	{
		this.Hour = hour;
	}

	public int getMinute()
	{
		return this.Minute;
	}
	public void setMinute(int minute)
	{
		this.Minute = minute;
	}

	public int getSecond()
	{
		return this.Second;
	}
	public void setSecond(int second)
	{
		this.Second = second;
	}
}


