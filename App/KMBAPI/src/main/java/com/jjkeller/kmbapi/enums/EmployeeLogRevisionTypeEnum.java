package com.jjkeller.kmbapi.enums;

import android.content.Context;

import com.jjkeller.kmbapi.R;

public class EmployeeLogRevisionTypeEnum extends EnumBase {
	public static final int NULL = 0;
	public static final int NONE = 1;
	public static final int EXEMPTLOGCONVERSION = 2;
	public static final String DmoEnum_Null = "Null";
	public static final String DmoEnum_None = "None";
	public static final String DmoEnum_ExemptLogConversion = "ExemptLogConversion";
	public static final int ARRAYID = R.array.revisiontype_array;
	
	public EmployeeLogRevisionTypeEnum(int value)
	{
		super(value);
	}
	
	@Override
	public void setValue(int value) throws IndexOutOfBoundsException
	{
		switch(value)
		{
			case NULL:
			case NONE:
			case EXEMPTLOGCONVERSION:
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
		if(value == NONE)
			return DmoEnum_None;
		if(value == EXEMPTLOGCONVERSION)
			return DmoEnum_ExemptLogConversion;
		else
			return DmoEnum_Null;
	}

	@Override
	protected int getArrayId() {
		return ARRAYID;
	}

	public EmployeeLogRevisionTypeEnum valueOf(Context ctx, String name) throws IllegalArgumentException
	{
		String[] array = ctx.getResources().getStringArray(ARRAYID);
		
		for(int index = 0; index < array.length; index++)
		{
			if(name.compareTo(array[index]) == 0)
				return new EmployeeLogRevisionTypeEnum(index);
		}
		
		throw new IllegalArgumentException("Enum value undefined");
	}
	
	public static EmployeeLogRevisionTypeEnum valueOfDMOEnum(String name) throws IllegalArgumentException
	{
		if(name.compareTo(DmoEnum_Null) == 0)
			return new EmployeeLogRevisionTypeEnum(NULL);
		else if(name.compareTo(DmoEnum_None) == 0)
			return new EmployeeLogRevisionTypeEnum(NONE);
		else if(name.compareTo(DmoEnum_ExemptLogConversion) == 0)
			return new EmployeeLogRevisionTypeEnum(EXEMPTLOGCONVERSION);

		else
			throw new IllegalArgumentException("Enum value undefined");
	}
}
