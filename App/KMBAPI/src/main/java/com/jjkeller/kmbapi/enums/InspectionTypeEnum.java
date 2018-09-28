package com.jjkeller.kmbapi.enums;

import android.content.Context;

import com.jjkeller.kmbapi.R;

public class InspectionTypeEnum extends EnumBase {

	public static final int NULL = 0;
	public static final int PRETRIP = 1;
	public static final int POSTTRIP = 2;
    private static final String DmoEnum_Null = "Null";
    private static final String DmoEnum_PreTrip = "PreTrip";
    private static final String DmoEnum_PostTrip = "PostTrip";
    public static final int ARRAYID = R.array.inspectiontype_array;

	public InspectionTypeEnum(int value) {
		super(value);
	}

    @Override
    public void setValue(int value) throws IndexOutOfBoundsException
    {
    	switch(value)
    	{    	
	    	case NULL:
	    	case POSTTRIP:
	    	case PRETRIP:
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
    	else if(value == POSTTRIP)
    		return DmoEnum_PostTrip;
    	else if(value == PRETRIP)
    		return DmoEnum_PreTrip;
    	else
    		return DmoEnum_Null;
	}

	@Override
	protected int getArrayId() {
		return ARRAYID;
	}

	public InspectionTypeEnum valueOf(Context ctx, String name) throws IllegalArgumentException
	{
    	String[] array = ctx.getResources().getStringArray(ARRAYID);
    	
    	for(int index = 0; index < array.length; index++)
    	{
    		if(name.compareTo(array[index]) == 0)
    			return new InspectionTypeEnum(index);
    	}
    	
   		throw new IllegalArgumentException("Enum value undefined");
	}

	public static InspectionTypeEnum valueOfDMOEnum(String name) throws IllegalArgumentException
    {
    	if(name.compareTo(DmoEnum_Null) == 0)
    		return new InspectionTypeEnum(NULL);
    	else if(name.compareTo(DmoEnum_PostTrip) == 0)
    		return new InspectionTypeEnum(POSTTRIP);
    	else if(name.compareTo(DmoEnum_PreTrip) == 0)
    		return new InspectionTypeEnum(PRETRIP);
    	else
    		throw new IllegalArgumentException("Enum value undefined");
    }

}
