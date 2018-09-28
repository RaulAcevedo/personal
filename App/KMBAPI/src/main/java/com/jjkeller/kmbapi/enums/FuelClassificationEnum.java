package com.jjkeller.kmbapi.enums;

import android.content.Context;

import com.jjkeller.kmbapi.R;

public class FuelClassificationEnum extends EnumBase {
	public static final int NULL = 0;
	public static final int RECEIPTED = 1;
	public static final int NONRECEIPTED = 2;
	public static final int TAXPAIDBULK = 3;
	public static final int TAXNOTPAIDBULK = 4;
	public static final int REEFER = 5;
    private static final String DmoEnum_Null = "Null";
    private static final String DmoEnum_Receipted = "Receipted";
    private static final String DmoEnum_NonReceipted = "NonReceipted";
    private static final String DmoEnum_TaxPaidBulk = "TaxPaidBulk";
    private static final String DmoEnum_TaxNotPaidBulk = "TaxNotPaidBulk";
    private static final String DmoEnum_Reefer = "Reefer";
    public static final int ARRAYID = R.array.fuelclassification_array;
    public static final int ARRAYABBRID = R.array.fuelclassification_abbr_array;

	public FuelClassificationEnum(int value) {
		super(value);
	}

	@Override
	public void setValue(int value) throws IndexOutOfBoundsException {
    	switch(value)
    	{
	    	case NULL:
	    	case RECEIPTED:
	    	case NONRECEIPTED:
	    	case TAXPAIDBULK:
	    	case TAXNOTPAIDBULK:
	    	case REEFER:
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
	    	case RECEIPTED:
	    		return DmoEnum_Receipted;
	    	case NONRECEIPTED:
	    		return DmoEnum_NonReceipted;
	    	case TAXPAIDBULK:
	    		return DmoEnum_TaxPaidBulk;
	    	case TAXNOTPAIDBULK:
	    		return DmoEnum_TaxNotPaidBulk;
	    	case REEFER:
	    		return DmoEnum_Reefer;
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
	
	public FuelClassificationEnum valueOf(Context ctx, String name) throws IllegalArgumentException
	{
    	String[] array = ctx.getResources().getStringArray(ARRAYID);
    	
    	for(int index = 0; index < array.length; index++)
    	{
    		if(name.compareTo(array[index]) == 0)
    			return new FuelClassificationEnum(index);
    	}
    	
   		throw new IllegalArgumentException("Enum value undefined");
	}

	public static FuelClassificationEnum valueOfDMOEnum(String name) throws IllegalArgumentException
    {
    	if(name.compareTo(DmoEnum_Null) == 0)
    		return new FuelClassificationEnum(NULL);
    	else if(name.compareTo(DmoEnum_Receipted) == 0)
    		return new FuelClassificationEnum(RECEIPTED);
    	else if(name.compareTo(DmoEnum_NonReceipted) == 0)
    		return new FuelClassificationEnum(NONRECEIPTED);
    	else if(name.compareTo(DmoEnum_TaxPaidBulk) == 0)
    		return new FuelClassificationEnum(TAXPAIDBULK);
    	else if(name.compareTo(DmoEnum_TaxNotPaidBulk) == 0)
    		return new FuelClassificationEnum(TAXNOTPAIDBULK);
    	else if(name.compareTo(DmoEnum_Reefer) == 0)
    		return new FuelClassificationEnum(REEFER);
    	else
    		throw new IllegalArgumentException("Enum value undefined");
    }
}
