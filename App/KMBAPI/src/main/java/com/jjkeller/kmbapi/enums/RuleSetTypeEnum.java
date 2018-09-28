package com.jjkeller.kmbapi.enums;

import android.content.Context;

import com.jjkeller.kmbapi.R;

public class RuleSetTypeEnum extends EnumBase {
	public static final int NULL = 0;
	public static final int ALASKA_7DAY = 1;
	public static final int ALASKA_8DAY = 2;
	public static final int US60HOUR = 26;
	public static final int US70HOUR = 28;
	public static final int CANADIAN_CYCLE1 = 39;
	public static final int CANADIAN_CYCLE2 = 40;
	public static final int FLORIDA_7DAY = 41;
	public static final int FLORIDA_8DAY = 42;
	public static final int WISCONSIN_7DAY = 43;
	public static final int WISCONSIN_8DAY = 44;
	public static final int TEXAS = 22;
	public static final int USOILFIELD = 30;
	public static final int TEXASOILFIELD = 24;
	public static final int CALIFORNIA_INTRASTATE = 47;
	public static final int USMOTIONPICTURE_7DAY = 48;
	public static final int USMOTIONPICTURE_8DAY = 49;
	public static final int USCONSTRUCTION_7DAY = 25;
	public static final int USCONSTRUCTION_8DAY = 27;
	public static final int CALIFORNIA_MP_80 = 50;
	private static final String DmoEnum_Null = "Null";
	private static final String DmoEnum_Alaska_7Day = "Alaska_7Day";
	private static final String DmoEnum_Alaska_8Day = "Alaska_8Day";
	private static final String DmoEnum_US60Hour = "US60Hour";
	private static final String DmoEnum_US70Hour = "US70Hour";
	private static final String DmoEnum_Canadian_Cycle1 = "Canadian_Cycle1";
	private static final String DmoEnum_Canadian_Cycle2 = "Canadian_Cycle2";
	private static final String DmoEnum_Florida_7Day = "Florida_7Day";
	private static final String DmoEnum_Florida_8Day = "Florida_8Day";
	private static final String DmoEnum_Wisconsin_7Day = "Wisconsin_7Day";
	private static final String DmoEnum_Wisconsin_8Day = "Wisconsin_8Day";
	private static final String DmoEnum_USConstruction_7Day = "USConstruction_7Day";
	private static final String DmoEnum_USConstruction_8Day = "USConstruction_8Day";
	private static final String DmoEnum_Texas = "Texas";
	private static final String DmoEnum_USOilField = "USOilField";
	private static final String DmoEnum_TexasOilField = "TexasOilField";
	private static final String DmoEnum_CaliforniaIntrastate = "CaliforniaIntrastate";
	private static final String DmoEnum_USMotionPicture_7Day = "USMotionPicture7Day";
	private static final String DmoEnum_USMotionPicture_8Day = "USMotionPicture8Day";
	private static final String DmoEnum_CaliforniaMP_80 = "California_MP_80";
    public static final int ARRAYID = R.array.rulesettype_array;
    public static final int ARRAYABBRID = R.array.rulesettype_abbr_array;

	public RuleSetTypeEnum(int value)
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
    		case ALASKA_7DAY:
    		case ALASKA_8DAY:
    		case CALIFORNIA_INTRASTATE:
    		case US60HOUR:
    		case US70HOUR:
			case USCONSTRUCTION_7DAY:
			case USCONSTRUCTION_8DAY:
    		case CANADIAN_CYCLE1:
    		case CANADIAN_CYCLE2:
    		case FLORIDA_7DAY:
    		case FLORIDA_8DAY:
			case WISCONSIN_7DAY:
			case WISCONSIN_8DAY:
    		case TEXAS:
    		case USOILFIELD:
    		case TEXASOILFIELD:
    		case USMOTIONPICTURE_7DAY:
    		case USMOTIONPICTURE_8DAY:
			case CALIFORNIA_MP_80:
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
			case NULL:
	    		return DmoEnum_Null;
			case ALASKA_7DAY:
	    		return DmoEnum_Alaska_7Day;
        	case ALASKA_8DAY:
        		return DmoEnum_Alaska_8Day;
        	case CALIFORNIA_INTRASTATE:
        		return DmoEnum_CaliforniaIntrastate;
			case US60HOUR:
	    		return DmoEnum_US60Hour;
        	case US70HOUR:
        		return DmoEnum_US70Hour;
        	case CANADIAN_CYCLE1:
        		return DmoEnum_Canadian_Cycle1;
        	case CANADIAN_CYCLE2:
        		return DmoEnum_Canadian_Cycle2;
        	case FLORIDA_7DAY:
        		return DmoEnum_Florida_7Day;
        	case FLORIDA_8DAY:
        		return DmoEnum_Florida_8Day;
			case WISCONSIN_7DAY:
				return DmoEnum_Wisconsin_7Day;
			case WISCONSIN_8DAY:
				return DmoEnum_Wisconsin_8Day;
			case USCONSTRUCTION_7DAY:
				return DmoEnum_USConstruction_7Day;
			case USCONSTRUCTION_8DAY:
				return DmoEnum_USConstruction_8Day;
        	case TEXAS:
        		return DmoEnum_Texas;
        	case USOILFIELD:
        		return DmoEnum_USOilField;
        	case TEXASOILFIELD:
        		return DmoEnum_TexasOilField;
        	case USMOTIONPICTURE_7DAY:
        		return DmoEnum_USMotionPicture_7Day;
        	case USMOTIONPICTURE_8DAY:
        		return DmoEnum_USMotionPicture_8Day;
			case CALIFORNIA_MP_80:
				return  DmoEnum_CaliforniaMP_80;
        	default:
        		return DmoEnum_Null;
		}
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
    		case ALASKA_7DAY:
    			retVal = ctx.getResources().getString(R.string.rulesettype_alaska_7day);
    			break;
    		case ALASKA_8DAY:
    			retVal = ctx.getResources().getString(R.string.rulesettype_alaska_8day);
    			break;
    		case CALIFORNIA_INTRASTATE:
    			retVal = ctx.getResources().getString(R.string.rulesettype_californiaintrastate);
    			break;
    		case US60HOUR:
    			retVal = ctx.getResources().getString(R.string.rulesettype_us60hour);
    			break;
    		case US70HOUR:
    			retVal = ctx.getResources().getString(R.string.rulesettype_us70hour);
    			break;
    		case CANADIAN_CYCLE1:
    			retVal = ctx.getResources().getString(R.string.rulesettype_canadian_cycle1);
    			break;
    		case CANADIAN_CYCLE2:
    			retVal = ctx.getResources().getString(R.string.rulesettype_canadian_cycle2);
    			break;
    		case FLORIDA_7DAY:
    			retVal = ctx.getResources().getString(R.string.rulesettype_florida_7day);
    			break;
    		case FLORIDA_8DAY:
    			retVal = ctx.getResources().getString(R.string.rulesettype_florida_8day);
    			break;
			case WISCONSIN_7DAY:
				retVal = ctx.getResources().getString(R.string.rulesettype_wisconsin_7day);
				break;
			case WISCONSIN_8DAY:
				retVal = ctx.getResources().getString(R.string.rulesettype_wisconsin_8day);
				break;
			case USCONSTRUCTION_7DAY:
				retVal = ctx.getResources().getString(R.string.rulesettype_usconstruction_7day);
				break;
			case USCONSTRUCTION_8DAY:
				retVal = ctx.getResources().getString(R.string.rulesettype_usconstruction_8day);
				break;
    		case TEXAS:
    			retVal = ctx.getResources().getString(R.string.rulesettype_texas);
    			break;
    		case USOILFIELD:
    			retVal = ctx.getResources().getString(R.string.rulesettype_usoilfield);
    			break;
    		case TEXASOILFIELD:
    			retVal = ctx.getResources().getString(R.string.rulesettype_texasoilfield);
    			break;
    		case USMOTIONPICTURE_7DAY:
    			retVal = ctx.getResources().getString(R.string.rulesettype_usmotionpicture_7day);
    			break;
    		case USMOTIONPICTURE_8DAY:
    			retVal = ctx.getResources().getString(R.string.rulesettype_usmotionpicture_8day);
    			break;
			case CALIFORNIA_MP_80:
				retVal = ctx.getResources().getString(R.string.rulesettype_california_motion_picture_80hour);
				break;
    		default:
    			retVal = "";
    			break;
    	}

    	return retVal;
	}

	public String getStringAbbr(Context ctx)
	{
		String retVal;
    	switch(value)
    	{
    		case NULL:
    			retVal = "";
    			break;
    		case ALASKA_7DAY:
    			retVal = ctx.getResources().getString(R.string.rulesettype_alaska_7day_abbr);
    			break;
    		case ALASKA_8DAY:
    			retVal = ctx.getResources().getString(R.string.rulesettype_alaska_8day_abbr);
    			break;
    		case CALIFORNIA_INTRASTATE:
    			retVal = ctx.getResources().getString(R.string.rulesettype_californiaintrastate_abbr);
    			break;
    		case US60HOUR:
    			retVal = ctx.getResources().getString(R.string.rulesettype_us60hour_abbr);
    			break;
    		case US70HOUR:
    			retVal = ctx.getResources().getString(R.string.rulesettype_us70hour_abbr);
    			break;
    		case CANADIAN_CYCLE1:
    			retVal = ctx.getResources().getString(R.string.rulesettype_canadian_cycle1_abbr);
    			break;
    		case CANADIAN_CYCLE2:
    			retVal = ctx.getResources().getString(R.string.rulesettype_canadian_cycle2_abbr);
    			break;
    		case FLORIDA_7DAY:
    			retVal = ctx.getResources().getString(R.string.rulesettype_florida_7day_abbr);
    			break;
    		case FLORIDA_8DAY:
    			retVal = ctx.getResources().getString(R.string.rulesettype_florida_8day_abbr);
    			break;
			case WISCONSIN_7DAY:
				retVal = ctx.getResources().getString(R.string.rulesettype_wisconsin_7day_abbr);
				break;
			case WISCONSIN_8DAY:
				retVal = ctx.getResources().getString(R.string.rulesettype_wisconsin_8day_abbr);
				break;
			case USCONSTRUCTION_7DAY:
				retVal = ctx.getResources().getString(R.string.rulesettype_usconstruction_7day_abbr);
				break;
			case USCONSTRUCTION_8DAY:
				retVal = ctx.getResources().getString(R.string.rulesettype_usconstruction_8day_abbr);
				break;
    		case TEXAS:
    			retVal = ctx.getResources().getString(R.string.rulesettype_texas_abbr);
    			break;
    		case USOILFIELD:
    			retVal = ctx.getResources().getString(R.string.rulesettype_usoilfield_abbr);
    			break;
    		case TEXASOILFIELD:
    			retVal = ctx.getResources().getString(R.string.rulesettype_texasoilfield_abbr);
    			break;
    		case USMOTIONPICTURE_7DAY:
    			retVal = ctx.getResources().getString(R.string.rulesettype_usmotionpicture_7day_abbr);
    			break;
    		case USMOTIONPICTURE_8DAY:
    			retVal = ctx.getResources().getString(R.string.rulesettype_usmotionpicture_8day_abbr);
    			break;
			case CALIFORNIA_MP_80:
				retVal = ctx.getResources().getString(R.string.rulesettype_california_motion_picture_80hour_abbr);
				break;
    		default:
    			retVal = "";
    			break;
    	}

    	return retVal;
	}
	
	public static RuleSetTypeEnum valueOf(Context ctx, String name) throws IllegalArgumentException
	{
		if (name.compareTo(DmoEnum_Null) == 0)
			return new RuleSetTypeEnum(NULL);
		else if (name.compareTo(ctx.getResources().getString(R.string.rulesettype_alaska_7day)) == 0)
			return new RuleSetTypeEnum(ALASKA_7DAY);
		else if (name.compareTo(ctx.getResources().getString(R.string.rulesettype_alaska_8day)) == 0)
			return new RuleSetTypeEnum(ALASKA_8DAY);
		else if (name.compareTo(ctx.getResources().getString(R.string.rulesettype_californiaintrastate)) == 0)
			return new RuleSetTypeEnum(CALIFORNIA_INTRASTATE);
		else if (name.compareTo(ctx.getResources().getString(R.string.rulesettype_us60hour)) == 0)
			return new RuleSetTypeEnum(US60HOUR);
		else if (name.compareTo(ctx.getResources().getString(R.string.rulesettype_us70hour)) == 0)
			return new RuleSetTypeEnum(US70HOUR);
		else if (name.compareTo(ctx.getResources().getString(R.string.rulesettype_canadian_cycle1)) == 0)
			return new RuleSetTypeEnum(CANADIAN_CYCLE1);
		else if (name.compareTo(ctx.getResources().getString(R.string.rulesettype_canadian_cycle2)) == 0)
			return new RuleSetTypeEnum(CANADIAN_CYCLE2);
		else if (name.compareTo(ctx.getResources().getString(R.string.rulesettype_florida_7day)) == 0)
			return new RuleSetTypeEnum(FLORIDA_7DAY);
		else if (name.compareTo(ctx.getResources().getString(R.string.rulesettype_florida_8day)) == 0)
			return new RuleSetTypeEnum(FLORIDA_8DAY);
		else if (name.compareTo(ctx.getResources().getString(R.string.rulesettype_wisconsin_7day)) == 0)
			return new RuleSetTypeEnum(WISCONSIN_7DAY);
		else if (name.compareTo(ctx.getResources().getString(R.string.rulesettype_wisconsin_8day)) == 0)
			return new RuleSetTypeEnum(WISCONSIN_8DAY);
		else if (name.compareTo(ctx.getResources().getString(R.string.rulesettype_usconstruction_7day)) == 0)
			return new RuleSetTypeEnum(USCONSTRUCTION_7DAY);
		else if (name.compareTo(ctx.getResources().getString(R.string.rulesettype_usconstruction_8day)) == 0)
			return new RuleSetTypeEnum(USCONSTRUCTION_8DAY);
		else if (name.compareTo(ctx.getResources().getString(R.string.rulesettype_texas)) == 0)
			return new RuleSetTypeEnum(TEXAS);
		else if (name.compareTo(ctx.getResources().getString(R.string.rulesettype_usoilfield)) == 0)
			return new RuleSetTypeEnum(USOILFIELD);
		else if (name.compareTo(ctx.getResources().getString(R.string.rulesettype_texasoilfield)) == 0)
			return new RuleSetTypeEnum(TEXASOILFIELD);
		else if (name.compareTo(ctx.getResources().getString(R.string.rulesettype_usmotionpicture_7day)) == 0)
			return new RuleSetTypeEnum(USMOTIONPICTURE_7DAY);
		else if (name.compareTo(ctx.getResources().getString(R.string.rulesettype_usmotionpicture_8day)) == 0)
			return new RuleSetTypeEnum(USMOTIONPICTURE_8DAY);
		else if (name.compareTo(ctx.getResources().getString(R.string.rulesettype_california_motion_picture_80hour)) == 0)
			return new RuleSetTypeEnum(CALIFORNIA_MP_80);
		else
			throw new IllegalArgumentException("Enum value undefined");
	}
	
	public static RuleSetTypeEnum valueOfDMOEnum(String name) throws IllegalArgumentException
    {
		if (name.compareTo(DmoEnum_Null) == 0)
			return new RuleSetTypeEnum(NULL);
		else if (name.compareTo(DmoEnum_Alaska_7Day) == 0)
			return new RuleSetTypeEnum(ALASKA_7DAY);
		else if (name.compareTo(DmoEnum_Alaska_8Day) == 0)
			return new RuleSetTypeEnum(ALASKA_8DAY);
		else if (name.compareTo(DmoEnum_CaliforniaIntrastate) == 0)
			return new RuleSetTypeEnum(CALIFORNIA_INTRASTATE);
		else if (name.compareTo(DmoEnum_US60Hour) == 0)
			return new RuleSetTypeEnum(US60HOUR);
		else if (name.compareTo(DmoEnum_US70Hour) == 0)
			return new RuleSetTypeEnum(US70HOUR);
		else if (name.compareTo(DmoEnum_Canadian_Cycle1) == 0)
			return new RuleSetTypeEnum(CANADIAN_CYCLE1);
		else if (name.compareTo(DmoEnum_Canadian_Cycle2) == 0)
			return new RuleSetTypeEnum(CANADIAN_CYCLE2);
		else if (name.compareTo(DmoEnum_Florida_7Day) == 0)
			return new RuleSetTypeEnum(FLORIDA_7DAY);
		else if (name.compareTo(DmoEnum_Florida_8Day) == 0)
			return new RuleSetTypeEnum(FLORIDA_8DAY);
		else if (name.compareTo(DmoEnum_Wisconsin_7Day) == 0)
			return new RuleSetTypeEnum(WISCONSIN_7DAY);
		else if (name.compareTo(DmoEnum_Wisconsin_8Day) == 0)
			return new RuleSetTypeEnum(WISCONSIN_8DAY);
		else if (name.compareTo(DmoEnum_USConstruction_7Day) == 0)
			return new RuleSetTypeEnum(USCONSTRUCTION_7DAY);
		else if (name.compareTo(DmoEnum_USConstruction_8Day) == 0)
			return new RuleSetTypeEnum(USCONSTRUCTION_8DAY);
		else if (name.compareTo(DmoEnum_Texas) == 0)
			return new RuleSetTypeEnum(TEXAS);
		else if (name.compareTo(DmoEnum_USOilField) == 0)
			return new RuleSetTypeEnum(USOILFIELD);
		else if (name.compareTo(DmoEnum_TexasOilField) == 0)
			return new RuleSetTypeEnum(TEXASOILFIELD);
		else if (name.compareTo(DmoEnum_USMotionPicture_7Day) == 0)
			return new RuleSetTypeEnum(USMOTIONPICTURE_7DAY);
		else if (name.compareTo(DmoEnum_USMotionPicture_8Day) == 0)
			return new RuleSetTypeEnum(USMOTIONPICTURE_8DAY);
		else if (name.compareTo(DmoEnum_CaliforniaMP_80) == 0)
			return new RuleSetTypeEnum(CALIFORNIA_MP_80);
		else
			throw new IllegalArgumentException("Enum value undefined");
    }

//	public int GetArrayPos(Context ctx)
//	{
//		int arrayPos = -1;
//		String ruleset = getString(ctx);
//		String[] array = ctx.getResources().getStringArray(R.array.rulesettype_array);
//		for(int index = 0; index < array.length; index++)
//		{
//			if(array[index].compareTo(ruleset) == 0)
//				arrayPos = index;
//		}
//		return arrayPos;
//		
//	}

	public boolean isCanadianRuleset()
	{
		return value == RuleSetTypeEnum.CANADIAN_CYCLE1 || value == RuleSetTypeEnum.CANADIAN_CYCLE2;
	}
	
	public boolean isUSFederalRuleset()
	{
		return value == RuleSetTypeEnum.US60HOUR || value == RuleSetTypeEnum.US70HOUR;
	}
	
	public boolean isAnyOilFieldRuleset()
	{
		return value == RuleSetTypeEnum.USOILFIELD || value == RuleSetTypeEnum.TEXASOILFIELD;
	}

	public boolean is30MinuteBreakExemptValid()
	{
		return value == RuleSetTypeEnum.US60HOUR || value == RuleSetTypeEnum.US70HOUR || value == RuleSetTypeEnum.USOILFIELD;
	}

	/**
	 * Return the ClassificationEnum associated to a RuleSetType
     */
	public RuleSetClassificationEnum getRuleSetClassificationEnum() {

		switch(value)
		{
			case NULL:
				return RuleSetClassificationEnum.NULL;

			case US60HOUR:
			case US70HOUR:
				return RuleSetClassificationEnum.USFEDERAL;

			case CANADIAN_CYCLE1:
			case CANADIAN_CYCLE2:
				return RuleSetClassificationEnum.CANADIANFEDERAL;

			case USOILFIELD:
			case TEXASOILFIELD:
				return RuleSetClassificationEnum.USFEDERAL_EXEMPTION_OILFIELD;

			case USMOTIONPICTURE_7DAY:
			case USMOTIONPICTURE_8DAY:
				return RuleSetClassificationEnum.USMOTIONPICTURE;

			default:
				return RuleSetClassificationEnum.USFEDERAL_EXEMPTION;
		}
	}
}
