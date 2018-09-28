package com.jjkeller.kmbapi.enums;

import android.content.Context;

import com.jjkeller.kmbapi.R;

public class CanadaDeferralTypeEnum extends EnumBase{

    public static final int NONE = 0;
    public static final int DAYONE = 1;
    public static final int DAYTWO = 2;
    private static final String DmoEnum_None = "None";
    private static final String DmoEnum_DayOne = "DayOne";
    private static final String DmoEnum_DayTwo = "DayTwo";
    public static final int ARRAYID = R.array.cddeferral_array;
    
    public CanadaDeferralTypeEnum(int value)
    {
    	super(value);
    }

    @Override
    protected int getArrayId() {
		return ARRAYID;
	}
    
    @Override
    public void setValue(int value) throws IndexOutOfBoundsException
    {
    	switch(value)
    	{
	    	case NONE:
	    	case DAYONE:
	    	case DAYTWO:
	    		this.value = value;
	    		break;
    		default:
    			super.setValue(value);
    	}
    }

    @Override
    public String toDMOEnum()
    {
    	if(value == NONE)
    		return DmoEnum_None;
    	else if(value == DAYONE)
    		return DmoEnum_DayOne;
    	else if(value == DAYTWO)
    		return DmoEnum_DayTwo;
    	else
    		return DmoEnum_None;
    }
    
    public static CanadaDeferralTypeEnum valueOf(Context ctx, String name) throws IllegalArgumentException
    {
    	String[] array = ctx.getResources().getStringArray(ARRAYID);
    	
    	for(int index = 0; index < array.length; index++)
    	{
    		if(name.compareTo(array[index]) == 0)
    			return new CanadaDeferralTypeEnum(index);
    	}
    	
   		throw new IllegalArgumentException("Enum value undefined");
    }

	public static CanadaDeferralTypeEnum valueOfDMOEnum(String name) throws IllegalArgumentException
    {
    	if(name.compareTo(DmoEnum_None) == 0)
    		return new CanadaDeferralTypeEnum(NONE);
    	else if(name.compareTo(DmoEnum_DayOne) == 0)
    		return new CanadaDeferralTypeEnum(DAYONE);
    	else if(name.compareTo(DmoEnum_DayTwo) == 0)
    		return new CanadaDeferralTypeEnum(DAYTWO);
    	else
    		throw new IllegalArgumentException("Enum value undefined");
    }
}
