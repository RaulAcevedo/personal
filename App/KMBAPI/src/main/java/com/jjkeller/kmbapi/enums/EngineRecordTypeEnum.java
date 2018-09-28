package com.jjkeller.kmbapi.enums;

import android.content.Context;

import com.jjkeller.kmbapi.R;

public class EngineRecordTypeEnum extends EnumBase {
	public static final int NULL = 0;
	public static final int STANDARD = 1;
	public static final int ENGINERUNNINGCHANGE = 2;
	public static final int VEHICLEMOVEMENTCHANGE = 3;
	public static final int HARDBRAKEEVENT = 4;
	public static final int HARDBRAKESURROUNDING = 5;
	public static final int THRESHOLDEXCEEDED = 6;
    private static final String DmoEnum_Null = "Null";
    private static final String DmoEnum_Standard = "Standard";
    private static final String DmoEnum_EngineRunningChange = "EngineRunningChange";
    private static final String DmoEnum_VehicleMovementChange = "VehicleMovementChange";
    private static final String DmoEnum_HardBrakeEvent = "HardBrakeEvent";
    private static final String DmoEnum_HardBrakeSurrounding = "HardBrakeSurrounding";
    private static final String DmoEnum_ThresholdExceeded = "ThresholdExceeded";
    public static final int ARRAYID = R.array.enginerecordtype_array;

	public EngineRecordTypeEnum(int value) {
		super(value);
	}

    @Override
    public void setValue(int value) throws IndexOutOfBoundsException
    {
    	switch(value)
    	{
    	
	    	case NULL:
	    	case STANDARD:
	    	case ENGINERUNNINGCHANGE:
	    	case VEHICLEMOVEMENTCHANGE:
	    	case HARDBRAKEEVENT:
	    	case HARDBRAKESURROUNDING:
	    	case THRESHOLDEXCEEDED:
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
    	else if(value == STANDARD)
    		return DmoEnum_Standard;
    	else if(value == ENGINERUNNINGCHANGE)
    		return DmoEnum_EngineRunningChange;
    	else if(value == VEHICLEMOVEMENTCHANGE)
    		return DmoEnum_VehicleMovementChange;
    	else if(value == HARDBRAKEEVENT)
    		return DmoEnum_HardBrakeEvent;
    	else if(value == HARDBRAKESURROUNDING)
    		return DmoEnum_HardBrakeSurrounding;
    	else if(value == THRESHOLDEXCEEDED)
    		return DmoEnum_ThresholdExceeded;
    	else
    		return DmoEnum_Null;

	}

	@Override
	protected int getArrayId() {
		return ARRAYID;
	}

	public EngineRecordTypeEnum valueOf(Context ctx, String name) throws IllegalArgumentException
	{
    	String[] array = ctx.getResources().getStringArray(ARRAYID);
    	
    	for(int index = 0; index < array.length; index++)
    	{
    		if(name.compareTo(array[index]) == 0)
    			return new EngineRecordTypeEnum(index);
    	}
    	
   		throw new IllegalArgumentException("Enum value undefined");
	}

	public static EngineRecordTypeEnum valueOfDMOEnum(String name) throws IllegalArgumentException
    {
    	if(name.compareTo(DmoEnum_Null) == 0)
    		return new EngineRecordTypeEnum(NULL);
    	else if(name.compareTo(DmoEnum_Standard) == 0)
    		return new EngineRecordTypeEnum(STANDARD);
    	else if(name.compareTo(DmoEnum_EngineRunningChange) == 0)
    		return new EngineRecordTypeEnum(ENGINERUNNINGCHANGE);
    	else if(name.compareTo(DmoEnum_VehicleMovementChange) == 0)
    		return new EngineRecordTypeEnum(VEHICLEMOVEMENTCHANGE);
    	else if(name.compareTo(DmoEnum_HardBrakeEvent) == 0)
    		return new EngineRecordTypeEnum(HARDBRAKEEVENT);
    	else if(name.compareTo(DmoEnum_HardBrakeSurrounding) == 0)
    		return new EngineRecordTypeEnum(HARDBRAKESURROUNDING);
    	else if(name.compareTo(DmoEnum_ThresholdExceeded) == 0)
    		return new EngineRecordTypeEnum(THRESHOLDEXCEEDED);
    	else
    		throw new IllegalArgumentException("Enum value undefined");
    }
}
