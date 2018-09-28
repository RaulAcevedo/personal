package com.jjkeller.kmbapi.kmbeobr;

import android.content.Context;

import com.jjkeller.kmbapi.R;
import com.jjkeller.kmbapi.enums.EnumBase;

public class StatusRecordMotionOptionEnum extends EnumBase {
	public static final int NULL = 0;
	public static final int NEXTRECORD = 1;
	public static final int NEXTMOTIONCHANGE = 2;
	public static final String DmoEnum_Null = "Null";
	public static final String DmoEnum_NextRecord = "NextRecord";
	public static final String DmoEnum_NextMotionChange = "NextMotionChange";
	public static final int ARRAYID = R.array.statusrecordmotionoption_array;
	
	public StatusRecordMotionOptionEnum(int value) {
		super(value);
	}

    @Override
    public void setValue(int value) throws IndexOutOfBoundsException
    {
    	switch(value)
    	{
	    	case NULL:
	    	case NEXTRECORD:
	    	case NEXTMOTIONCHANGE:
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
    	else if(value == NEXTRECORD)
    		return DmoEnum_NextRecord;
    	else if(value == NEXTMOTIONCHANGE)
    		return DmoEnum_NextMotionChange;
    	else
    		return DmoEnum_Null;
	}

	@Override
	protected int getArrayId() {
		return ARRAYID;
	}

	public StatusRecordMotionOptionEnum valueOf(Context ctx, String name) throws IllegalArgumentException
	{
    	String[] array = ctx.getResources().getStringArray(ARRAYID);
    	
    	for(int index = 0; index < array.length; index++)
    	{
    		if(name.compareTo(array[index]) == 0)
    			return new StatusRecordMotionOptionEnum(index);
    	}
    	
   		throw new IllegalArgumentException("Enum value undefined");
	}

	public static StatusRecordMotionOptionEnum valueOfDMOEnum(String name) throws IllegalArgumentException
    {
    	if(name.compareTo(DmoEnum_Null) == 0)
    		return new StatusRecordMotionOptionEnum(NULL);
    	else if(name.compareTo(DmoEnum_NextRecord) == 0)
    		return new StatusRecordMotionOptionEnum(NEXTRECORD);
    	else if(name.compareTo(DmoEnum_NextMotionChange) == 0)
    		return new StatusRecordMotionOptionEnum(NEXTMOTIONCHANGE);
    	else
    		throw new IllegalArgumentException("Enum value undefined");
    }
}
