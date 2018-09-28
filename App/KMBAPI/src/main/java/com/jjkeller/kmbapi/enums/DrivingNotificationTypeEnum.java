package com.jjkeller.kmbapi.enums;

import android.content.Context;

import com.jjkeller.kmbapi.R;

public class DrivingNotificationTypeEnum extends EnumBase {
	public static final int NULL = 0;
	public static final int NOALERT = 1;
	public static final int ONEHOUR = 2;
	public static final int TWOHOUR = 3;
    private static final String DmoEnum_Null = "Null";
    private static final String DmoEnum_NoAlert= "NoAlert";
    private static final String DmoEnum_OneHour = "OneHour";
    private static final String DmoEnum_TwoHour = "TwoHour";
    public static final int ARRAYID = R.array.drivingnotificationtype_array;

	public DrivingNotificationTypeEnum(int value) {
		super(value);
	}

	@Override
	public void setValue(int value) throws IndexOutOfBoundsException {
    	switch(value)
    	{
	    	case NULL:
	    	case NOALERT:
	    	case ONEHOUR:
	    	case TWOHOUR:
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
	    	case NOALERT:
	    		return DmoEnum_NoAlert;
	    	case ONEHOUR:
	    		return DmoEnum_OneHour;
	    	case TWOHOUR:
	    		return DmoEnum_TwoHour;
	    	case NULL:
    		default:
    			return DmoEnum_Null;
		}
	}

	@Override
	protected int getArrayId() {
		return ARRAYID;
	}

	public DrivingNotificationTypeEnum valueOf(Context ctx, String name) throws IllegalArgumentException
	{
    	String[] array = ctx.getResources().getStringArray(ARRAYID);
    	
    	for(int index = 0; index < array.length; index++)
    	{
    		if(name.compareTo(array[index]) == 0)
    			return new DrivingNotificationTypeEnum(index);
    	}
    	
   		throw new IllegalArgumentException("Enum value undefined");
	}

	public static DrivingNotificationTypeEnum valueOfDMOEnum(String name) throws IllegalArgumentException
    {
    	if(name.compareTo(DmoEnum_Null) == 0)
    		return new DrivingNotificationTypeEnum(NULL);
    	else if(name.compareTo(DmoEnum_NoAlert) == 0)
    		return new DrivingNotificationTypeEnum(NOALERT);
    	else if(name.compareTo(DmoEnum_OneHour) == 0)
    		return new DrivingNotificationTypeEnum(ONEHOUR);
    	else if(name.compareTo(DmoEnum_TwoHour) == 0)
    		return new DrivingNotificationTypeEnum(TWOHOUR);
    	else
    		throw new IllegalArgumentException("Enum value undefined");
    }
}
