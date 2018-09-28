package com.jjkeller.kmbapi.enums;

import android.content.Context;

import com.jjkeller.kmbapi.R;

public class DataProfileEnum extends EnumBase {

	public static final int NULL = 0;
	public static final int MINIMUMHOS = 1;
	public static final int MINIMUMHOSWITHGPS = 5;
	public static final int MINIMUMHOSWITHFUELTAX = 10;
	public static final int MINIMUMHOSWITHFUELTAXANDGPS = 15;
	public static final int HOSWITHFUELTAXANDMAPPING = 20;
	public static final int FULL = 100;
	public static final int FULLWITHGEOFENCE = 110;
	public static final String DmoEnum_Null = "Null";
	public static final String DmoEnum_MinimumHOS = "MinimumHOS";
	public static final String DmoEnum_MinimumHOSWithFuelTax = "MinimumHOSWithFuelTax";
	public static final String DmoEnum_MinimumHOSWithFuelTaxAndGPS = "MinimumHOSWithFuelTaxAndGPS";
	public static final String DmoEnum_MinimumHOSWithGPS = "MinimumHOSWithGPS";
	public static final String DmoEnum_HOSWithFuelTaxAndMapping = "HOSWithFuelTaxAndMapping";
	public static final String DmoEnum_Full = "Full";
	public static final String DmoEnum_FullWithGeofence = "FullWithGeofence";
	public static final int ARRAYID = R.array.dataprofile_array;
	
	public DataProfileEnum(int value)
	{
		super(value);
	}
	
	@Override
	public void setValue(int value) throws IndexOutOfBoundsException
	{
		switch(value)
		{
			case NULL:
			case MINIMUMHOS:
			case MINIMUMHOSWITHGPS:
			case MINIMUMHOSWITHFUELTAX:
			case MINIMUMHOSWITHFUELTAXANDGPS:
			case HOSWITHFUELTAXANDMAPPING:
			case FULL:
			case FULLWITHGEOFENCE:
				this.value = value;
				break;
			default:
				super.setValue(value);
		}
	}
	
	@Override
	public String toDMOEnum() 
	{
		switch (value)
		{
			case MINIMUMHOS:
				return DmoEnum_MinimumHOS;
			case MINIMUMHOSWITHFUELTAX:
				return DmoEnum_MinimumHOSWithFuelTax;
			case MINIMUMHOSWITHFUELTAXANDGPS:
				return DmoEnum_MinimumHOSWithFuelTaxAndGPS;
			case MINIMUMHOSWITHGPS:
				return DmoEnum_MinimumHOSWithGPS;
			case HOSWITHFUELTAXANDMAPPING:
				return DmoEnum_HOSWithFuelTaxAndMapping;
			case FULL:
				return DmoEnum_Full;
			case FULLWITHGEOFENCE:
				return DmoEnum_FullWithGeofence;
			default:
				return DmoEnum_Null;
		}
	}

	@Override
	protected int getArrayId() {
		return ARRAYID;
	}

	@Override
	public String getString(Context ctx)
	{
		String retVal;
    	switch(value)
    	{
    		case NULL:
    			retVal = "";
    			break;
			case MINIMUMHOS:
    			retVal = ctx.getResources().getString(R.string.dataprofile_minimumhos);
    			break;
			case MINIMUMHOSWITHFUELTAX:
    			retVal = ctx.getResources().getString(R.string.dataprofile_minimumhoswithfueltax);
    			break;
			case MINIMUMHOSWITHFUELTAXANDGPS:
				retVal = ctx.getResources().getString(R.string.dataprofile_minimumhoswithfueltaxandgps);
				break;
			case MINIMUMHOSWITHGPS:
				retVal = ctx.getResources().getString(R.string.dataprofile_minimumhoswithgps);
				break;
			case HOSWITHFUELTAXANDMAPPING:
    			retVal = ctx.getResources().getString(R.string.dataprofile_hoswithfueltaxandmapping);
    			break;
			case FULL:
    			retVal = ctx.getResources().getString(R.string.dataprofile_full);
    			break;
			case FULLWITHGEOFENCE:
				retVal = ctx.getResources().getString(R.string.dataprofile_fullwithgeofence);
				break;
    		default:
    			retVal = "";
    			break;
    	}

    	return retVal;
	}
	
	public DataProfileEnum valueOf(Context ctx, String name) throws IllegalArgumentException
	{
		String[] array = ctx.getResources().getStringArray(ARRAYID);
		
		for(int index = 0; index < array.length; index++)
		{
			if(name.compareTo(array[index]) == 0)
				return new DataProfileEnum(index);
		}
		
		throw new IllegalArgumentException("Enum value undefined");
	}
	
	public static DataProfileEnum valueOfDMOEnum(String name) throws IllegalArgumentException
	{
		if (name.compareTo(DmoEnum_Null) == 0)
			return new DataProfileEnum(NULL);
		else if (name.compareTo(DmoEnum_MinimumHOS) == 0)
			return new DataProfileEnum(MINIMUMHOS);
		else if (name.compareTo(DmoEnum_MinimumHOSWithFuelTax) == 0)
			return new DataProfileEnum(MINIMUMHOSWITHFUELTAX);
		else if (name.compareTo(DmoEnum_MinimumHOSWithFuelTaxAndGPS) == 0)
			return new DataProfileEnum(MINIMUMHOSWITHFUELTAXANDGPS);
		else if (name.compareTo(DmoEnum_MinimumHOSWithGPS) == 0)
			return new DataProfileEnum(MINIMUMHOSWITHGPS);
		else if (name.compareTo(DmoEnum_HOSWithFuelTaxAndMapping) == 0)
			return new DataProfileEnum(HOSWITHFUELTAXANDMAPPING);
		else if (name.compareTo(DmoEnum_Full) == 0)
			return new DataProfileEnum(FULL);
		else if (name.compareTo(DmoEnum_FullWithGeofence) == 0)
			return new DataProfileEnum(FULLWITHGEOFENCE);
		else
			throw new IllegalArgumentException("Enum value undefined");
	}
}
