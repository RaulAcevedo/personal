package com.jjkeller.kmbapi.enums;

import android.content.Context;

import com.jjkeller.kmbapi.R;

public class EobrCommunicationModeEnum extends EnumBase {

	public static final int NULL = 0;
	public static final int USB = 1;
	public static final int BT = 2;
	public static final int USB_BT = 3;
	private static final String DmoEnum_Null = "Null";
	private static final String DmoEnum_USB = "USB";
	private static final String DmoEnum_BT = "BT";
	private static final String DmoEnum_USB_BT = "USB_BT";
    public static final int ARRAYID = R.array.eobrcommunicationmode_array;
	
    public EobrCommunicationModeEnum(int value)
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
	    	case USB:
	    	case BT:
	    	case USB_BT:
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
    	else if(value == USB)
    		return DmoEnum_USB;
    	else if(value == BT)
    		return DmoEnum_BT;
    	else if(value == USB_BT)
    		return DmoEnum_USB_BT;
    	else
    		return DmoEnum_Null;
    }
    
    public static EobrCommunicationModeEnum valueOf(Context ctx, String name) throws IllegalArgumentException
    {
    	String[] array = ctx.getResources().getStringArray(ARRAYID);
    	
    	for(int index = 0; index < array.length; index++)
    	{
    		if(name.compareTo(array[index]) == 0)
    			return new EobrCommunicationModeEnum(index);
    	}
    	
   		throw new IllegalArgumentException("Enum value undefined");
    }

	public static EobrCommunicationModeEnum valueOfDMOEnum(String name) throws IllegalArgumentException
    {
    	if(name.compareTo(DmoEnum_Null) == 0)
    		return new EobrCommunicationModeEnum(NULL);
    	else if(name.compareTo(DmoEnum_USB) == 0)
    		return new EobrCommunicationModeEnum(USB);
    	else if(name.compareTo(DmoEnum_BT) == 0)
    		return new EobrCommunicationModeEnum(BT);
    	else if(name.compareTo(DmoEnum_USB_BT) == 0)
    		return new EobrCommunicationModeEnum(USB_BT);
    	else
    		throw new IllegalArgumentException("Enum value undefined");
    }
}
