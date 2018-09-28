package com.jjkeller.kmbapi.enums;

import android.content.Context;

import com.jjkeller.kmbapi.R;

public class DatabusTypeEnum extends EnumBase {
	public static final int NULL = -1;
	public static final int UNKNOWN = 0;
	public static final int J1850VPW = 1;
	public static final int J1850PWM = 2;
	public static final int ISO91412 = 3;
	public static final int KWP2000 = 4;
	public static final int BIT11CANBUS250 = 5;
	public static final int BIT29CANBUS250 = 6;
	public static final int BIT11CANBUS500 = 7;
	public static final int BIT29CANBUS500 = 8;
	public static final int J1708 = 9;
	public static final int J1939 = 10;
	public static final int DUALMODEJ1708J1939 = 11;
	public static final int GPSONLY = 12;
	public static final int J1939F = 13;
	public static final int DUALMODEJ1708J1939F = 14;

	private static final String DmoEnum_Null = "Null";
	private static final String DmoEnum_AutoDetect = "Auto-Detect";
    private static final String DmoEnum_OBD = "Auto-Detect Databus";
    private static final String DmoEnum_J1850VPW = "J1850 VPW (GMC)";
    private static final String DmoEnum_J1850PWM = "J1850 PWM (Ford)";
    private static final String DmoEnum_ISO91412 = "ISO-9141-2";
    private static final String DmoEnum_KWP2000 = "KWP2000";
    private static final String DmoEnum_BIT11CANBUS250 = "11-bit CAN bus (250 Kbps)";
    private static final String DmoEnum_BIT29CANBUS250 = "29-bit CAN bus (250 Kbps)";
    private static final String DmoEnum_BIT11CANBUS500 = "11-bit CAN bus (500 Kbps)";
    private static final String DmoEnum_BIT29CANBUS500 = "29-bit CAN bus (500 Kbps)";
    private static final String DmoEnum_J1708 = "J1708";
    private static final String DmoEnum_J1939 = "J1939";
    private static final String DmoEnum_DUALMODEJ1708J1939 = "Dual mode (J1708 and J1939)";
	private static final String DmoEnum_J1939F = "J1939F (500 kbps)";
	private static final String DmoEnum_DUALMODEJ1708J1939F = "Dual mode fast (J1708 and J1939F)";

    public static final int ARRAYID = R.array.databustype_array;

	public DatabusTypeEnum(int value) {
		super(value);
	}

    @Override
    public void setValue(int value) throws IndexOutOfBoundsException
    {
    	switch(value)
    	{
    		case NULL:
	    	case UNKNOWN:
	    	case J1850VPW:
	    	case J1850PWM:
	    	case ISO91412:
	    	case KWP2000:
	    	case BIT11CANBUS250:
	    	case BIT11CANBUS500:
	    	case BIT29CANBUS250:
	    	case BIT29CANBUS500:
	    	case J1708:
	    	case J1939:
	    	case DUALMODEJ1708J1939:
	    	case GPSONLY:
			case J1939F:
			case DUALMODEJ1708J1939F:
	    		this.value = value;
	    		break;
			default:
				super.setValue(value);
    	}
    }

    @Override
	public String toDMOEnum() {
    	if (value == NULL)
    		return DmoEnum_Null;
    	else if(value == UNKNOWN)
    		return DmoEnum_OBD;
    	else if(value == J1850VPW)
    		return DmoEnum_J1850VPW;
    	else if(value == J1850PWM)
    		return DmoEnum_J1850PWM;
    	else if(value == ISO91412)
    		return DmoEnum_ISO91412;
    	else if(value == KWP2000)
    		return DmoEnum_KWP2000;
    	else if(value == BIT11CANBUS250)
    		return DmoEnum_BIT11CANBUS250;
    	else if(value == BIT11CANBUS500)
    		return DmoEnum_BIT11CANBUS500;
    	else if(value == BIT29CANBUS250)
    		return DmoEnum_BIT29CANBUS250;
    	else if(value == BIT29CANBUS500)
    		return DmoEnum_BIT29CANBUS500;
    	else if(value == J1708)
    		return DmoEnum_J1708;
    	else if(value == J1939)
    		return DmoEnum_J1939;
    	else if(value == DUALMODEJ1708J1939)
    		return DmoEnum_DUALMODEJ1708J1939;
    	else if (value == GPSONLY)
    		return DmoEnum_AutoDetect;
    	else if (value == J1939F)
			return DmoEnum_J1939F;
		else if (value == DUALMODEJ1708J1939F)
			return DmoEnum_DUALMODEJ1708J1939F;
    		return DmoEnum_Null;
	}

    @Override
    public String getString(Context ctx)
    {
    	return this.toDMOEnum();
    }
    
	@Override
	protected int getArrayId() {
		return ARRAYID;
	}

	public DatabusTypeEnum valueOf(Context ctx, String name) throws IllegalArgumentException
	{
    	String[] array = ctx.getResources().getStringArray(ARRAYID);
    	
    	for(int index = 0; index < array.length; index++)
    	{
    		if(name.compareTo(array[index]) == 0)
    			return new DatabusTypeEnum(index);
    	}
    	
   		throw new IllegalArgumentException("Enum value undefined");
	}

	public static DatabusTypeEnum valueOfDMOEnum(String name) throws IllegalArgumentException
    {
		if (name.compareTo(DmoEnum_Null) == 0)
			return new DatabusTypeEnum(NULL);
		else if(name.compareTo(DmoEnum_OBD) == 0)
    		return new DatabusTypeEnum(UNKNOWN);
    	else if(name.compareTo(DmoEnum_J1850VPW) == 0)
    		return new DatabusTypeEnum(J1850VPW);
    	else if(name.compareTo(DmoEnum_J1850PWM) == 0)
    		return new DatabusTypeEnum(J1850PWM);
    	else if(name.compareTo(DmoEnum_ISO91412) == 0)
    		return new DatabusTypeEnum(ISO91412);
    	else if(name.compareTo(DmoEnum_KWP2000) == 0)
    		return new DatabusTypeEnum(KWP2000);
    	else if(name.compareTo(DmoEnum_BIT11CANBUS250) == 0)
    		return new DatabusTypeEnum(BIT11CANBUS250);
    	else if(name.compareTo(DmoEnum_BIT11CANBUS500) == 0)
    		return new DatabusTypeEnum(BIT11CANBUS500);
    	else if(name.compareTo(DmoEnum_BIT29CANBUS250) == 0)
    		return new DatabusTypeEnum(BIT29CANBUS250);
    	else if(name.compareTo(DmoEnum_BIT29CANBUS500) == 0)
    		return new DatabusTypeEnum(BIT29CANBUS500);
    	else if(name.compareTo(DmoEnum_J1708) == 0)
    		return new DatabusTypeEnum(J1708);
    	else if(name.compareTo(DmoEnum_J1939) == 0)
    		return new DatabusTypeEnum(J1939);
    	else if(name.compareTo(DmoEnum_DUALMODEJ1708J1939) == 0)
    		return new DatabusTypeEnum(DUALMODEJ1708J1939);
    	else if(name.compareTo(DmoEnum_AutoDetect) == 0)
    		return new DatabusTypeEnum(GPSONLY);
		else if (name.compareTo(DmoEnum_J1939F) == 0)
			return new DatabusTypeEnum(J1939F);
		else if (name.compareTo(DmoEnum_DUALMODEJ1708J1939F) == 0)
			return new DatabusTypeEnum(DUALMODEJ1708J1939F);
    	else
    		throw new IllegalArgumentException("Enum value undefined");
    }

    public boolean isHeavyBusType() {
		return value == J1939 || value == J1939F || value == J1708 || value == DUALMODEJ1708J1939 || value == DUALMODEJ1708J1939F;
	}
}
