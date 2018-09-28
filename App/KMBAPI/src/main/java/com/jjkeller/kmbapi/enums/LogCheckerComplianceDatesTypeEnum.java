package com.jjkeller.kmbapi.enums;

import android.content.Context;

import com.jjkeller.kmbapi.R;

public class LogCheckerComplianceDatesTypeEnum extends EnumBase{

	public static final int NULL = 0;
	public static final int JULY2013_8HRDRIVING = 3;
    public static final int JULY2013_34HRRESET = 4;
    public static final int DEC2014_UNENFORCE34HRRESET = 5;
    private static final String DmoEnum_Null = "Null";
    private static final String DmoEnum_July2013_8HrDriving = "July2013_8HrDriving";
    private static final String DmoEnum_July2013_34HrReset = "July2013_34HrDriving";
    private static final String DmoEnum_DEC2014_Unenforce34HrReset = "Dec2014_Unenforce34HrReset";
    public static final int ARRAYID = R.array.compliancedate_array;
    
    public LogCheckerComplianceDatesTypeEnum(int value)
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
    		case NULL:
    		case JULY2013_8HRDRIVING:
	    	case JULY2013_34HRRESET:
	    	case DEC2014_UNENFORCE34HRRESET:
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
    	else if(value == JULY2013_8HRDRIVING)
    		return DmoEnum_July2013_8HrDriving;
    	else if(value == JULY2013_34HRRESET)
    		return DmoEnum_July2013_34HrReset;
    	else if (value == DEC2014_UNENFORCE34HRRESET)
    		return DmoEnum_DEC2014_Unenforce34HrReset;
    	else
    		return DmoEnum_Null;
    }
    
    public static LogCheckerComplianceDatesTypeEnum valueOf(Context ctx, String name) throws IllegalArgumentException
    {
    	String[] array = ctx.getResources().getStringArray(ARRAYID);
    	
    	for(int index = 0; index < array.length; index++)
    	{
    		if(name.compareTo(array[index]) == 0)
    			return new LogCheckerComplianceDatesTypeEnum(index);
    	}
    	
   		throw new IllegalArgumentException("Enum value undefined");
    }

	public static LogCheckerComplianceDatesTypeEnum valueOfDMOEnum(String name) throws IllegalArgumentException
    {
		if(name.compareTo(DmoEnum_Null) == 0)
    		return new LogCheckerComplianceDatesTypeEnum(NULL);
		else if(name.compareTo(DmoEnum_July2013_8HrDriving) == 0)
    		return new LogCheckerComplianceDatesTypeEnum(JULY2013_8HRDRIVING);
		else if(name.compareTo(DmoEnum_July2013_34HrReset) == 0)
    		return new LogCheckerComplianceDatesTypeEnum(JULY2013_34HRRESET);
		else if (name.compareTo(DmoEnum_DEC2014_Unenforce34HrReset) == 0)
			return new LogCheckerComplianceDatesTypeEnum(DEC2014_UNENFORCE34HRRESET);
    	else
    		throw new IllegalArgumentException("Enum value undefined");
    }
}
