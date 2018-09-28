package com.jjkeller.kmbapi.enums;

import android.content.Context;

import com.jjkeller.kmbapi.R;

public class ExemptLogTypeEnum extends EnumBase {

	public static final int UNDEFINED = -1;  //"for use in determining if this is a new log object or an existing standard grid log"
	public static final int NULL = 0;
	public static final int EXEMPTLOGTYPE100AIRMILE = 1;
	public static final int EXEMPTLOGTYPE150AIRMILENONCDL = 2;
	
	public static final String DmoEnum_Null = "Null";
	public static final String DmoEnum_ExemptLogType100AirMile = "100 Air-mile Exempt";
	public static final String DmoEnum_ExemptLogType150AirMileNonCDL = "150 Air-mile Non-CDL Exempt";
	public static final int ARRAYID = R.array.databustype_array;
	
	public ExemptLogTypeEnum (int value)
	{
		super(value);
	}
	
	@Override
	public void setValue(int value) throws IndexOutOfBoundsException
	{
		switch(value)
		{
			case UNDEFINED:
			case NULL:
			case EXEMPTLOGTYPE100AIRMILE:
			case EXEMPTLOGTYPE150AIRMILENONCDL:
				this.value = value;
				break;
			default:
				super.setValue(value);
		}
	}
	
	@Override
	public String toDMOEnum()
	{
		switch (value)
		{
			case EXEMPTLOGTYPE100AIRMILE:
				return DmoEnum_ExemptLogType100AirMile;
			case EXEMPTLOGTYPE150AIRMILENONCDL:
				return DmoEnum_ExemptLogType150AirMileNonCDL;
			default:
				return DmoEnum_Null;
		}
	}
	
	@Override
	protected int getArrayId()
	{
		return ARRAYID;
	}
	
	@Override
	public String getString(Context ctx)
	{
		String retVal;
		switch(value)
		{
			case NULL:
				retVal = "";
				break;
			case EXEMPTLOGTYPE100AIRMILE:
				retVal = ctx.getResources().getString(R.string.exemptlogtype_exemptlogtype100airmile);
				break;
			case EXEMPTLOGTYPE150AIRMILENONCDL:
				retVal = ctx.getResources().getString(R.string.exemptlogtype_exemptlogtype150airmilenoncdl);
				break;
			default:
				retVal = "";
				break;
		}
		
		return retVal;
	}
	
	public ExemptLogTypeEnum valueOf(Context ctx, String name) throws IllegalArgumentException
	{
		String[] array = ctx.getResources().getStringArray(ARRAYID);
		
		for(int index = 0; index < array.length; index++)
		{
			if(name.compareTo(array[index]) == 0)
				return new ExemptLogTypeEnum(index);
		}
		
		throw new IllegalArgumentException("Enum value undefined");
	}
	
	public static ExemptLogTypeEnum valueOfDMOEnum(String name) throws IllegalArgumentException
	{
		if (name.compareTo(DmoEnum_Null) == 0)
			return new ExemptLogTypeEnum(NULL);
		else if (name.compareTo(DmoEnum_ExemptLogType100AirMile) == 0)
			return new ExemptLogTypeEnum(EXEMPTLOGTYPE100AIRMILE);
		else if (name.compareTo(DmoEnum_ExemptLogType150AirMileNonCDL) == 0)
			return new ExemptLogTypeEnum(EXEMPTLOGTYPE150AIRMILENONCDL);
		else
			throw new IllegalArgumentException("Enum value undefined");
	}
}
