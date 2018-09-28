package com.jjkeller.kmbapi.enums;

import android.content.Context;

import com.jjkeller.kmbapi.R;

public class DriverTypeEnum extends EnumBase {
	public static final int NULL = 0;
	public static final int PASSENGERCARRYING = 1;
	public static final int PROPERTYCARRYING = 2;
    private static final String DmoEnum_Null = "Null";
    private static final String DmoEnum_PassengerCarrying = "PassengerCarrying";
    private static final String DmoEnum_PropertyCarrying = "PropertyCarrying";
    public static final int ARRAYID = R.array.drivertype_array;

	public DriverTypeEnum(int value) {
		super(value);
	}

    @Override
    public void setValue(int value) throws IndexOutOfBoundsException
    {
    	switch(value)
    	{
    	
	    	case NULL:
	    	case PASSENGERCARRYING:
	    	case PROPERTYCARRYING:
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
    	else if(value == PASSENGERCARRYING)
    		return DmoEnum_PassengerCarrying;
    	else if(value == PROPERTYCARRYING)
    		return DmoEnum_PropertyCarrying;
    	else
    		return DmoEnum_Null;
	}

	@Override
	protected int getArrayId() {
		return ARRAYID;
	}

	public DriverTypeEnum valueOf(Context ctx, String name) throws IllegalArgumentException
	{
    	String[] array = ctx.getResources().getStringArray(ARRAYID);
    	
    	for(int index = 0; index < array.length; index++)
    	{
    		if(name.compareTo(array[index]) == 0)
    			return new DriverTypeEnum(index);
    	}
    	
   		throw new IllegalArgumentException("Enum value undefined");
	}

	public static DriverTypeEnum valueOfDMOEnum(String name) throws IllegalArgumentException
    {
    	if(name.compareTo(DmoEnum_Null) == 0)
    		return new DriverTypeEnum(NULL);
    	else if(name.compareTo(DmoEnum_PassengerCarrying) == 0)
    		return new DriverTypeEnum(PASSENGERCARRYING);
    	else if(name.compareTo(DmoEnum_PropertyCarrying) == 0)
    		return new DriverTypeEnum(PROPERTYCARRYING);
    	else
    		throw new IllegalArgumentException("Enum value undefined");
    }
}
