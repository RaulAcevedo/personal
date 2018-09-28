package com.jjkeller.kmbapi.kmbeobr;


import android.content.Context;

import com.jjkeller.kmbapi.R;
import com.jjkeller.kmbapi.enums.EnumBase;


public class HistogramTypeEnum extends EnumBase {

	public static final int VEHICLESPEED = 1;
	public static final int ENGINESPEED = 2;
	public static final int ENGINELOAD = 3;
	public static final int GPSFIXDOP = 4;
	public static final int GPSFIRSTFIX = 5;
	private static final String DmoEnum_VEHICLESPEED = "VEHICLESPEED";
    private static final String DmoEnum_ENGINESPEED = "ENGINESPEED";
    private static final String DmoEnum_ENGINELOAD = "ENGINELOAD";
    private static final String DmoEnum_GPSFIXDOP = "GPSFIXDOP";
    private static final String DmoEnum_GPSFIRSTFIX = "GPSFIRSTFIX";

    public static final int ARRAYID = R.array.histogramtype_array;

	public HistogramTypeEnum(int value) {
			super(value);
		}

	@Override
	public void setValue(int value) throws IndexOutOfBoundsException
	{
    	switch(value)
    	{
	    	case VEHICLESPEED:
	    	case ENGINESPEED:
	    	case ENGINELOAD:
	    	case GPSFIXDOP:
	    	case GPSFIRSTFIX:
		    		this.value = value;
		    		break;
    		default:
    			super.setValue(value);
    	}
    }

    @Override
	public String toDMOEnum() {
    	if(value == VEHICLESPEED)
    		return DmoEnum_VEHICLESPEED;
    	else if(value == ENGINESPEED)
    		return DmoEnum_ENGINESPEED;
    	else if(value == ENGINELOAD)
    		return DmoEnum_ENGINELOAD;
    	else if(value == GPSFIXDOP)
    		return DmoEnum_GPSFIXDOP;
    	else if(value == GPSFIRSTFIX)
    		return DmoEnum_GPSFIRSTFIX;
    	else
    		return DmoEnum_VEHICLESPEED;
	}

	@Override
	protected int getArrayId() {
		return ARRAYID;
	}

	public HistogramTypeEnum valueOf(Context ctx, String name) throws IllegalArgumentException
	{
    	String[] array = ctx.getResources().getStringArray(ARRAYID);
	    	
    	for(int index = 0; index < array.length; index++)
    	{
    		if(name.compareTo(array[index]) == 0)
    			return new HistogramTypeEnum(index);
    	}
    	
   		throw new IllegalArgumentException("Enum value undefined");
	}

	public static HistogramTypeEnum valueOfDMOEnum(String name) throws IllegalArgumentException
    {
    	if(name.compareTo(DmoEnum_VEHICLESPEED) == 0)
    		return new HistogramTypeEnum(VEHICLESPEED);
    	else if(name.compareTo(DmoEnum_ENGINESPEED) == 0)
    		return new HistogramTypeEnum(ENGINESPEED);
    	else if(name.compareTo(DmoEnum_ENGINELOAD) == 0)
    		return new HistogramTypeEnum(ENGINELOAD);
    	else if(name.compareTo(DmoEnum_GPSFIXDOP) == 0)
    		return new HistogramTypeEnum(GPSFIXDOP);
    	else if(name.compareTo(DmoEnum_GPSFIRSTFIX) == 0)
    		return new HistogramTypeEnum(GPSFIRSTFIX);
    	else
    		throw new IllegalArgumentException("Enum value undefined");
    }
		
	public String toStringDisplay(Context ctx)
	{
		if(value == VEHICLESPEED)
    		return ctx.getResources().getString(R.string.histogramtype_vehiclespeed);
    	else if(value == ENGINESPEED)
    		return ctx.getResources().getString(R.string.histogramtype_enginespeed);
    	else if(value == ENGINELOAD)
    		return ctx.getResources().getString(R.string.histogramtype_engineload);
    	else if(value == GPSFIXDOP)
    		return ctx.getResources().getString(R.string.histogramtype_gpsfixdop);
    	else if(value == GPSFIRSTFIX)
    		return ctx.getResources().getString(R.string.histogramtype_gpsfirstfix);
    	else
    		return ctx.getResources().getString(R.string.histogramtype_vehiclespeed);
	}
}
