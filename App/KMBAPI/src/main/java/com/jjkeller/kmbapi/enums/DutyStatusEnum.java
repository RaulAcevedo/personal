package com.jjkeller.kmbapi.enums;

import android.content.Context;

import com.jjkeller.kmbapi.R;
import com.jjkeller.kmbapi.configuration.GlobalState;

public class DutyStatusEnum extends EnumBase {
	public static final int NULL = 0;
	public static final int OFFDUTY = 1;
	public static final int SLEEPER = 2;
	public static final int DRIVING = 3;
	public static final int ONDUTY = 4;
	public static final int OFFDUTYWELLSITE = 5;

	public static final String DmoEnum_Null =  "Null";
	public static final String DmoEnum_OffDuty =  "OffDuty";
	public static final String DmoEnum_Sleeping  =  "Sleeper";
	public static final String DmoEnum_Driving  =  "Driving";
	public static final String DmoEnum_OnDuty =  "OnDuty";
	public static final String DmoEnum_OffDutyWellSite =  "OffDutyWellSite";

	public static final String Friendly_OffDuty = "Off Duty";
	public static final String Friendly_Sleeping = "Sleeper Berth";
	public static final String Friendly_Driving = "Driving";
	public static final String Friendly_OnDuty = "On-Duty Not Driving";
	public static final String RODSFriendly_OnDuty = "On-Duty";
	public static final String Friendly_OffDutyWellSite = "Off Duty Well Site";
	public static final int ARRAYID = R.array.dutystatus_array;

	public DutyStatusEnum(int value)
	{
		super(value);
	}

	@Override
	public void setValue(int value) throws IndexOutOfBoundsException
	{
		switch(value)
		{
			case NULL:
			case OFFDUTY:
			case SLEEPER:
			case DRIVING:
			case ONDUTY:
			case OFFDUTYWELLSITE:
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
		if(value == OFFDUTY)
			return DmoEnum_OffDuty;
		if(value == SLEEPER)
			return DmoEnum_Sleeping;
		if(value == DRIVING)
			return DmoEnum_Driving;
		if(value == ONDUTY)
			return DmoEnum_OnDuty;
		if(value == OFFDUTYWELLSITE)
			return DmoEnum_OffDutyWellSite;
		else
			return DmoEnum_Null;
	}


	public String toFriendlyName()
	{
		if(value == NULL)
			return DmoEnum_Null;
		if(value == OFFDUTY)
			return Friendly_OffDuty;
		if(value == SLEEPER)
			return Friendly_Sleeping;
		if(value == DRIVING)
			return Friendly_Driving;
		if(value == ONDUTY)
			return Friendly_OnDuty;
		if(value == OFFDUTYWELLSITE)
			return Friendly_OffDutyWellSite;
		else
			return DmoEnum_Null;
	}

	@Override
	protected int getArrayId() {
		return ARRAYID;
	}

	public DutyStatusEnum valueOf(Context ctx, String name) throws IllegalArgumentException
	{
		String[] array = ctx.getResources().getStringArray(ARRAYID);

		for(int index = 0; index < array.length; index++)
		{
			if(name.compareTo(array[index]) == 0)
				return new DutyStatusEnum(index);
		}

		throw new IllegalArgumentException("Enum value undefined");
	}

	public static DutyStatusEnum valueOfDMOEnum(String name) throws IllegalArgumentException
	{
		if(name.compareTo(DmoEnum_Null) == 0)
			return new DutyStatusEnum(NULL);
		else if(name.compareTo(DmoEnum_OffDuty) == 0 || name.compareTo(Friendly_OffDuty)==0)
			return new DutyStatusEnum(OFFDUTY);
		else if(name.compareTo(DmoEnum_Sleeping) == 0 || name.compareTo(Friendly_Sleeping)==0)
			return new DutyStatusEnum(SLEEPER);
		else if(name.compareTo(DmoEnum_Driving) == 0 || name.compareTo(Friendly_Driving)==0)
			return new DutyStatusEnum(DRIVING);
		else if(name.compareTo(DmoEnum_OnDuty) == 0 || name.compareTo(Friendly_OnDuty)==0)
			return new DutyStatusEnum(ONDUTY);
		else if(name.compareTo(DmoEnum_OffDutyWellSite) == 0 || name.compareTo(Friendly_OffDutyWellSite)==0)
			return new DutyStatusEnum(OFFDUTYWELLSITE);
		else
			throw new IllegalArgumentException("Enum value undefined");
	}
	
	public boolean isExemptOnDutyStatus()
	{
		return value == ONDUTY || value == DRIVING;
	}

	public boolean isExemptOffDutyStatus()
	{
		return value == OFFDUTY || value == SLEEPER || value == OFFDUTYWELLSITE;
	}

    @Override
    public String toString() {
        return "DutyStatusEnum{" +
                "DMOEnum='" + toDMOEnum() + '\'' +
                ", friendlyName='" + toFriendlyName() + '\'' +
                ", exemptOnDutyStatus=" + isExemptOnDutyStatus() +
                ", exemptOffDutyStatus=" + isExemptOffDutyStatus() +
                '}';
    }
}
