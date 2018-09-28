package com.jjkeller.kmbapi.enums;

import android.content.Context;

import com.jjkeller.kmbapi.R;

public class FuelUnitEnum extends EnumBase {
	public static final int NULL = 0;
	public static final int GALLONS = 1;
	public static final int LITERS = 2;
    private static final String DmoEnum_Null = "Null";
    private static final String DmoEnum_Gallons = "Gallons";
    private static final String DmoEnum_Liters = "Liters";
    public static final int ARRAYID = R.array.fuelunit_array;
    public static final int ARRAYABBRID = R.array.fuelunit_abbr_array;

	public FuelUnitEnum(int value) {
		super(value);
	}

	@Override
	public void setValue(int value) throws IndexOutOfBoundsException {
    	switch(value)
    	{
	    	case NULL:
	    	case GALLONS:
	    	case LITERS:
	    		this.value = value;
	    		break;
    		default:
    			super.setValue(value);
    	}
	}
	
	@Override
	public String toDMOEnum() {
		switch(value)
		{
	    	case GALLONS:
	    		return DmoEnum_Gallons;
	    	case LITERS:
	    		return DmoEnum_Liters;
	    	case NULL:
    		default:
    			return DmoEnum_Null;
		}
	}

	@Override
	protected int getArrayId() {
		return ARRAYID;
	}

	public String getStringAbbr(Context ctx)
	{
    	String[] array = ctx.getResources().getStringArray(ARRAYABBRID);
    	return array[value];
	}

	public FuelUnitEnum valueOf(Context ctx, String name) throws IllegalArgumentException
	{
    	String[] array = ctx.getResources().getStringArray(ARRAYID);
    	
    	for(int index = 0; index < array.length; index++)
    	{
    		if(name.compareTo(array[index]) == 0)
    			return new FuelUnitEnum(index);
    	}
    	
   		throw new IllegalArgumentException("Enum value undefined");
	}

	public static FuelUnitEnum valueOfDMOEnum(String name) throws IllegalArgumentException
    {
    	if(name.compareTo(DmoEnum_Null) == 0)
    		return new FuelUnitEnum(NULL);
    	else if(name.compareTo(DmoEnum_Gallons) == 0)
    		return new FuelUnitEnum(GALLONS);
    	else if(name.compareTo(DmoEnum_Liters) == 0)
    		return new FuelUnitEnum(LITERS);
    	else
    		throw new IllegalArgumentException("Enum value undefined");
    }
}
