package com.jjkeller.kmbapi.enums;

import android.content.Context;

import com.jjkeller.kmbapi.R;
import com.jjkeller.kmbapi.controller.utility.DateUtility;

import java.util.TimeZone;

public class TimeZoneEnum extends EnumBase {
	public static final int NULL = 0;
	public static final int ATLANTICSTANDARDTIME = 1;
	public static final int EASTERNSTANDARDTIME = 2;
	public static final int CENTRALSTANDARDTIME = 3;
	public static final int MOUNTAINSTANDARDTIME = 4;
	public static final int PACIFICSTANDARDTIME = 5;
	public static final int ALASKASTANDARDTIME = 6;
    public static final TimeZoneEnum UTC = new TimeZoneEnum(NULL);
    public static final TimeZoneEnum ATLANTIC_STANDARD_TIME = new TimeZoneEnum(1);
    public static final TimeZoneEnum EASTERN_STANDARD_TIME = new TimeZoneEnum(2);
    public static final TimeZoneEnum CENTRAL_STANDARD_TIME = new TimeZoneEnum(3);
    public static final TimeZoneEnum MOUNTAIN_STANDARD_TIME = new TimeZoneEnum(4);
    public static final TimeZoneEnum PACIFIC_STANDARD_TIME = new TimeZoneEnum(5);
    public static final TimeZoneEnum ALASKAS_TANDARD_TIME = new TimeZoneEnum(6);
	public static final String DmoEnum_Null = "Null";
	public static final String DmoEnum_AlaskaStandardTime = "AlaskaStandardTime";
	public static final String DmoEnum_AtlanticStandardTime = "AtlanticStandardTime";
	public static final String DmoEnum_CentralStandardTime = "CentralStandardTime";
	public static final String DmoEnum_EasternStandardTime = "EasternStandardTime";
	public static final String DmoEnum_MountainStandardTime = "MountainStandardTime";
	public static final String DmoEnum_PacificStandardTime = "PacificStandardTime";
	public static final int ARRAYID = R.array.timezone_array;
	
	public TimeZoneEnum(int value) {
		super(value);
	}

	@Override
	public void setValue(int value) throws IndexOutOfBoundsException
	{
		switch(value)
		{
			case NULL:
			case ALASKASTANDARDTIME:
			case ATLANTICSTANDARDTIME:
			case CENTRALSTANDARDTIME:
			case EASTERNSTANDARDTIME:
			case MOUNTAINSTANDARDTIME:
			case PACIFICSTANDARDTIME:
				this.value = value;
				break;
			default:
				super.setValue(value);
		}
	}
	
	@Override
	public String toDMOEnum() 
	{
		if(value == NULL)
			return DmoEnum_Null;
		else if(value == ALASKASTANDARDTIME)
			return DmoEnum_AlaskaStandardTime;
		else if(value == ATLANTICSTANDARDTIME)
			return DmoEnum_AtlanticStandardTime;
		else if(value == EASTERNSTANDARDTIME)
			return DmoEnum_EasternStandardTime;
		else if(value == CENTRALSTANDARDTIME)
			return DmoEnum_CentralStandardTime;
		else if(value == MOUNTAINSTANDARDTIME)
			return DmoEnum_MountainStandardTime;
		else if(value == PACIFICSTANDARDTIME)
			return DmoEnum_PacificStandardTime;
		else
			return DmoEnum_Null;
	}

	@Override
	protected int getArrayId() {
		return ARRAYID;
	}

	public TimeZoneEnum valueOf(Context ctx, String name) throws IllegalArgumentException
	{
		String[] array = ctx.getResources().getStringArray(ARRAYID);
		
		for (int index = 0; index < array.length; index++) 
		{
			if(name.compareTo(array[index]) == 0)
				return new TimeZoneEnum(index);
		}
		
		throw new IllegalArgumentException("Enum value undefined");
	}
	
	public static TimeZoneEnum valueOfDMOEnum(String name) throws IllegalArgumentException
	{
		if(name.compareTo(DmoEnum_Null) == 0)
			return new TimeZoneEnum(NULL);
		else if(name.compareTo(DmoEnum_AlaskaStandardTime) == 0)
			return new TimeZoneEnum(ALASKASTANDARDTIME);
		else if(name.compareTo(DmoEnum_AtlanticStandardTime) == 0)
			return new TimeZoneEnum(ATLANTICSTANDARDTIME);
		else if(name.compareTo(DmoEnum_CentralStandardTime) == 0)
			return new TimeZoneEnum(CENTRALSTANDARDTIME);
		else if(name.compareTo(DmoEnum_EasternStandardTime) == 0)
			return new TimeZoneEnum(EASTERNSTANDARDTIME);
		else if(name.compareTo(DmoEnum_MountainStandardTime) == 0)
			return new TimeZoneEnum(MOUNTAINSTANDARDTIME);
		else if(name.compareTo(DmoEnum_PacificStandardTime) == 0)
			return new TimeZoneEnum(PACIFICSTANDARDTIME);
		else
			throw new IllegalArgumentException("Enum value undefined");
	}

    public TimeZone toTimeZone(){
        if(value == NULL)
            return TimeZone.getTimeZone("UTC");
        else if(value == ALASKASTANDARDTIME)
            return TimeZone.getTimeZone("US/Alaska");
        else if(value == ATLANTICSTANDARDTIME)
            return TimeZone.getTimeZone("Canada/Atlantic");
        else if(value == EASTERNSTANDARDTIME)
            return TimeZone.getTimeZone("US/Eastern");
        else if(value == CENTRALSTANDARDTIME)
            return TimeZone.getTimeZone("US/Central");
        else if(value == MOUNTAINSTANDARDTIME)
            return TimeZone.getTimeZone("US/Mountain");
        else if(value == PACIFICSTANDARDTIME)
            return TimeZone.getTimeZone("US/Pacific");
        else
            return TimeZone.getTimeZone("UTC");
    }

    public void fromTimeZone(TimeZone timeZone) {
        if (timeZone == null)
            setValue(NULL);
        else if (timeZone.getID() == "UTC")
            setValue(NULL);
        else if (timeZone.getID() == "US/Alaska")
            setValue(ALASKASTANDARDTIME);
        else if (timeZone.getID() == "Canada/Atlantic")
            setValue(ATLANTICSTANDARDTIME);
        else if (timeZone.getID() == "US/Eastern")
            setValue(EASTERNSTANDARDTIME);
        else if (timeZone.getID() == "US/Central")
            setValue(CENTRALSTANDARDTIME);
        else if (timeZone.getID() == "US/Mountain")
            setValue(MOUNTAINSTANDARDTIME);
        else if (timeZone.getID() == "US/Pacific")
            setValue(PACIFICSTANDARDTIME);
        else
            setValue(NULL);
    }
}
