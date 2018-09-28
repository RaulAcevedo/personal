package com.jjkeller.kmbapi.enums;

import android.content.Context;

import com.jjkeller.kmbapi.R;

public class FailureCategoryEnum extends EnumBase {
	public static final int NULL = 0;
	public static final int CLOCKSYNCHRONIZATION = 1;
	public static final int EOBRDEVICE = 2;
    private static final String DmoEnum_Null = "Null";
    private static final String DmoEnum_ClockSynchronization = "ClockSynchronization";
    private static final String DmoEnum_EobrDevice = "EobrDevice";
    public static final int ARRAYID = R.array.failurecategory_array;

	public FailureCategoryEnum(int value) {
		super(value);
	}

    @Override
    public void setValue(int value) throws IndexOutOfBoundsException
    {
    	switch(value)
    	{
    	
	    	case NULL:
	    	case CLOCKSYNCHRONIZATION:
	    	case EOBRDEVICE:
	    		this.value = value;
	    		break;
    		default:
    			super.setValue(value);
    	}
    }

	@Override
	public String toDMOEnum() {
    	if(value == NULL)
    		return DmoEnum_Null;
    	else if(value == CLOCKSYNCHRONIZATION)
    		return DmoEnum_ClockSynchronization;
    	else if(value == EOBRDEVICE)
    		return DmoEnum_EobrDevice;
    	else
    		return DmoEnum_Null;
	}

	@Override
	protected int getArrayId() {
		return ARRAYID;
	}

	public FailureCategoryEnum valueOf(Context ctx, String name) throws IllegalArgumentException
	{
    	String[] array = ctx.getResources().getStringArray(ARRAYID);
    	
    	for(int index = 0; index < array.length; index++)
    	{
    		if(name.compareTo(array[index]) == 0)
    			return new FailureCategoryEnum(index);
    	}
    	
   		throw new IllegalArgumentException("Enum value undefined");
	}

	public static FailureCategoryEnum valueOfDMOEnum(String name) throws IllegalArgumentException
    {
    	if(name.compareTo(DmoEnum_Null) == 0)
    		return new FailureCategoryEnum(NULL);
    	else if(name.compareTo(DmoEnum_ClockSynchronization) == 0)
    		return new FailureCategoryEnum(CLOCKSYNCHRONIZATION);
    	else if(name.compareTo(DmoEnum_EobrDevice) == 0)
    		return new FailureCategoryEnum(EOBRDEVICE);
    	else
    		throw new IllegalArgumentException("Enum value undefined");
    }
}
