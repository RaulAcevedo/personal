package com.jjkeller.kmbapi.kmbeobr;

import android.content.Context;

import com.jjkeller.kmbapi.R;
import com.jjkeller.kmbapi.enums.EnumBase;

public class StatusRecordQueryMethodEnum extends EnumBase {
	public static final int NULL = 0;
	public static final int RECORDID = 1;
	public static final int TIMESTAMP = 2;
	public static final String DmoEnum_Null = "Null";
	public static final String DmoEnum_RecordId = "RecordId";
	public static final String DmoEnum_Timestamp = "Timestamp";
	public static final int ARRAYID = R.array.statusrecordquerymethod_array;
	
	public StatusRecordQueryMethodEnum(int value) {
		super(value);
	}

    @Override
    public void setValue(int value) throws IndexOutOfBoundsException
    {
    	switch(value)
    	{
	    	case NULL:
	    	case RECORDID:
	    	case TIMESTAMP:
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
    	else if(value == RECORDID)
    		return DmoEnum_RecordId;
    	else if(value == TIMESTAMP)
    		return DmoEnum_Timestamp;
    	else
    		return DmoEnum_Null;
	}

	@Override
	protected int getArrayId() {
		return ARRAYID;
	}

	public StatusRecordQueryMethodEnum valueOf(Context ctx, String name) throws IllegalArgumentException
	{
    	String[] array = ctx.getResources().getStringArray(ARRAYID);
    	
    	for(int index = 0; index < array.length; index++)
    	{
    		if(name.compareTo(array[index]) == 0)
    			return new StatusRecordQueryMethodEnum(index);
    	}
    	
   		throw new IllegalArgumentException("Enum value undefined");
	}

	public static StatusRecordQueryMethodEnum valueOfDMOEnum(String name) throws IllegalArgumentException
    {
    	if(name.compareTo(DmoEnum_Null) == 0)
    		return new StatusRecordQueryMethodEnum(NULL);
    	else if(name.compareTo(DmoEnum_RecordId) == 0)
    		return new StatusRecordQueryMethodEnum(RECORDID);
    	else if(name.compareTo(DmoEnum_Timestamp) == 0)
    		return new StatusRecordQueryMethodEnum(TIMESTAMP);
    	else
    		throw new IllegalArgumentException("Enum value undefined");
    }
}
