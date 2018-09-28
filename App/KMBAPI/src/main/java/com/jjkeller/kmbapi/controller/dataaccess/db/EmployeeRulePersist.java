package com.jjkeller.kmbapi.controller.dataaccess.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.jjkeller.kmbapi.controller.dataaccess.AbstractDBAdapter;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.enums.DataProfileEnum;
import com.jjkeller.kmbapi.enums.DriverTypeEnum;
import com.jjkeller.kmbapi.enums.ExemptLogTypeEnum;
import com.jjkeller.kmbapi.enums.RuleSetTypeEnum;
import com.jjkeller.kmbapi.enums.TimeZoneEnum;
import com.jjkeller.kmbapi.proxydata.EmployeeRule;

public class EmployeeRulePersist<T extends EmployeeRule> extends AbstractDBAdapter<T> {

	///////////////////////////////////////////////////////////////////////////////////////
	// private members
	///////////////////////////////////////////////////////////////////////////////////////
	private static final String EMPLOYEEID = "EmployeeId";
	private static final String DISTANCE = "Distance";
	private static final String MINUTES = "Minutes";
	private static final String TIMEZONETYPE = "TimezoneType";
	private static final String RULESETTYPE = "RulesetType";
	private static final String DRIVERTYPE = "DriverType";
	private static final String IS34HOURRESET = "Is34HourReset";
	private static final String ISSHORTHAULEXCEPTION = "IsShortHaulException";
	private static final String ADDITIONALRULESETS = "AdditionalRulesets";
	private static final String INTCDRULESET = "IntCDRuleset";
	private static final String INTUSRULESET = "IntUSRuleset";
	private static final String DATAPROFILE = "DataProfile";
	private static final String DISTANCEUNITS = "DistanceUnits";
	private static final String ISHAULINGEXPLOSIVESALLOWED = "IsHaulingExplosivesAllowed";
	private static final String ISHAULINGEXPLOSIVESDEFAULT = "IsHaulingExplosivesDefault";
	private static final String ISOPERATESSPECIFICVEHICLESFOROILFIELD = "IsOperatesSpecificVehiclesForOilField";
	private static final String ISPERSONALCONVEYANCEALLOWED = "IsPersonalConveyanceAllowed";
    private static final String ISHYRAILUSEALLOWED = "IsHyrailUseAllowed";
	private static final String ISMOBILEEXEMPTLOGALLOWED = "IsMobileExemptLogAllowed";
	private static final String ISEXEMPTFROM30MINBREAKREQUIREMENT = "IsExemptFrom30MinBreakRequirement";
	private static final String EXEMPTLOGTYPE = "ExemptLogType";
	private static final String EXEMPTFROMELDUSE = "ExemptFromEldUse";
	private static final String EXEMPTFROMELDUSECOMMENT = "ExemptFromEldUseComment";
	private static final String DRIVESTARTSPEED = "DriveStartSpeed";
	private static final String MANDATEDRIVINGSTOPTIMEMINUTES = "MandateDrivingStopTimeMinutes";
	private static final String YARDMOVEALLOWED = "YardMoveAllowed";
    private static final String ISNONREGDRIVINGALLOWED = "IsNonRegDrivingAllowed";


    private static final String SQL_SELECT_PRIMARYKEY_COMMAND = "select [Key] from EmployeeRule where EmployeeId=?";
    
    private static final String SQL_SELECT_COMMAND = "select * from EmployeeRule where EmployeeId=?";;

	///////////////////////////////////////////////////////////////////////////////////////
	// constructors
	///////////////////////////////////////////////////////////////////////////////////////
	public EmployeeRulePersist (Class<T> clazz, Context ctx, User user)
	{
		super(clazz, ctx, user);
		
		setDbTableName(DB_TABLE_EMPLOYEERULE);
	}

	///////////////////////////////////////////////////////////////////////////////////////
	// @Override methods
	///////////////////////////////////////////////////////////////////////////////////////
	@Override
	public String getSelectPrimaryKeyCommand()
	{
		return SQL_SELECT_PRIMARYKEY_COMMAND;
	}
	
	@Override
	protected String[] getSelectPrimaryKeyArgs(T data) {
		return new String[]{this.getCurrentUser().getCredentials().getEmployeeId()};
	}
	
	@Override
	protected String getSelectCommand()
	{
		return SQL_SELECT_COMMAND;
	}
	
	@Override
	protected String[] getSelectArgs()
	{
		return new String[]{this.getCurrentUser().getCredentials().getEmployeeId()};
	}

	@Override
	public T BuildObject(Cursor cursorData)
	{
		T data = super.BuildObject(cursorData);
		
		data.setDrivingStartDistanceMiles(ReadValue(cursorData, DISTANCE, (float)0.5));
		data.setDrivingStopTimeMinutes(ReadValue(cursorData, MINUTES, (int)10));
		data.getHomeTerminalTimeZone().setValue(ReadValue(cursorData, TIMEZONETYPE, TimeZoneEnum.EASTERNSTANDARDTIME));
		data.getRuleset().setValue(ReadValue(cursorData, RULESETTYPE, RuleSetTypeEnum.US60HOUR));
		data.getDriverType().setValue(ReadValue(cursorData, DRIVERTYPE, DriverTypeEnum.PROPERTYCARRYING));
		data.setIs34HourResetAllowed(ReadValue(cursorData, IS34HOURRESET, false));
		data.setIsShortHaulException(ReadValue(cursorData, ISSHORTHAULEXCEPTION, false));
		String addRulesets = ReadValue(cursorData, ADDITIONALRULESETS, (String)null);
		if(addRulesets != null)
		{
			String[] tokens = addRulesets.split(",");
			for(String tok : tokens)
			{
				if(tok != null && tok.length() > 0)
				{
					data.AddRuleset(new RuleSetTypeEnum(Integer.parseInt(tok)));
				}
			}
		}
		else data.setAdditionalRulesets(null);
		data.getIntCDRuleset().setValue(ReadValue(cursorData, INTCDRULESET, RuleSetTypeEnum.NULL));
		data.getIntUSRuleset().setValue(ReadValue(cursorData, INTUSRULESET, RuleSetTypeEnum.NULL));
		data.getDataProfile().setValue(ReadValue(cursorData, DATAPROFILE, DataProfileEnum.MINIMUMHOS));
		data.setDistanceUnits(ReadValue(cursorData, DISTANCEUNITS, "M"));
		data.setIsHaulingExplosivesAllowed(ReadValue(cursorData, ISHAULINGEXPLOSIVESALLOWED, false));
		data.setIsHaulingExplosivesDefault(ReadValue(cursorData, ISHAULINGEXPLOSIVESDEFAULT, false));
		data.setIsOperatesSpecificVehiclesForOilfield(ReadValue(cursorData, ISOPERATESSPECIFICVEHICLESFOROILFIELD, false));
		data.setIsPersonalConveyanceAllowed(ReadValue(cursorData, ISPERSONALCONVEYANCEALLOWED, false));
        data.setIsHyrailUseAllowed(ReadValue(cursorData, ISHYRAILUSEALLOWED, false));
		data.setIsMobileExemptLogAllowed(ReadValue(cursorData, ISMOBILEEXEMPTLOGALLOWED, false));
		data.setIsExemptFrom30MinBreakRequirement(ReadValue(cursorData, ISEXEMPTFROM30MINBREAKREQUIREMENT, false));
		data.getExemptLogType().setValue(ReadValue(cursorData, EXEMPTLOGTYPE, ExemptLogTypeEnum.NULL));
		data.setExemptFromEldUse(ReadValue(cursorData, EXEMPTFROMELDUSE, false));
		data.setExemptFromEldUseComment(ReadValue(cursorData, EXEMPTFROMELDUSECOMMENT, ""));
		data.setDriveStartSpeed(ReadValue(cursorData, DRIVESTARTSPEED, 5));
		data.setMandateDrivingStopTimeMinutes(ReadValue(cursorData, MANDATEDRIVINGSTOPTIMEMINUTES, 5));
		data.setYardMoveAllowed(ReadValue(cursorData, YARDMOVEALLOWED, false));
        data.setIsNonRegDrivingAllowed(ReadValue(cursorData, ISNONREGDRIVINGALLOWED, false));

		return data;
	}

	@Override
	public ContentValues PersistContentValues(T data)
	{
		ContentValues initialValues = super.PersistContentValues(data);

		PutValue(initialValues, DISTANCE, data.getDrivingStartDistanceMiles());
		PutValue(initialValues, MINUTES, data.getDrivingStopTimeMinutes());
		PutValue(initialValues, EMPLOYEEID, this.getCurrentUser().getCredentials().getEmployeeId());
		PutValue(initialValues, TIMEZONETYPE, data.getHomeTerminalTimeZone().getValue());
		PutValue(initialValues, RULESETTYPE, data.getRuleset().getValue());
		PutValue(initialValues, DRIVERTYPE, data.getDriverType().getValue());
		PutValue(initialValues, IS34HOURRESET, data.getIs34HourResetAllowed());
		PutValue(initialValues, ISSHORTHAULEXCEPTION, data.getIsShortHaulException());
		RuleSetTypeEnum[] addRulesetsArray = data.getAdditionalRulesets();
		if(addRulesetsArray != null && addRulesetsArray.length > 0)
		{
			StringBuilder addRulesets = new StringBuilder(); 
			for(RuleSetTypeEnum rulesetName:addRulesetsArray)
			{
				if(addRulesets != null && addRulesets.length() > 0)
				{
					addRulesets.append(",");			
				}
				addRulesets.append(rulesetName.getValue());
				
			}
			PutValue(initialValues, ADDITIONALRULESETS, addRulesets.toString());
		}
		else
			PutValue(initialValues, ADDITIONALRULESETS, null);
		
		PutValue(initialValues, INTCDRULESET, data.getIntCDRuleset().getValue());
		PutValue(initialValues, INTUSRULESET, data.getIntUSRuleset().getValue());
		PutValue(initialValues, DATAPROFILE, data.getDataProfile().getValue());
		PutValue(initialValues, DISTANCEUNITS, data.getDistanceUnits());
		PutValue(initialValues, ISHAULINGEXPLOSIVESALLOWED, data.getIsHaulingExplosivesAllowed());
		PutValue(initialValues, ISHAULINGEXPLOSIVESDEFAULT, data.getIsHaulingExplosivesDefault());
		PutValue(initialValues, ISOPERATESSPECIFICVEHICLESFOROILFIELD, data.getIsOperatesSpecificVehiclesForOilfield());
		PutValue(initialValues, ISPERSONALCONVEYANCEALLOWED, data.getIsPersonalConveyanceAllowed());
        PutValue(initialValues, ISHYRAILUSEALLOWED, data.getIsHyrailUseAllowed());
		PutValue(initialValues, ISMOBILEEXEMPTLOGALLOWED, data.getIsMobileExemptLogAllowed());
		PutValue(initialValues, ISEXEMPTFROM30MINBREAKREQUIREMENT, data.getIsExemptFrom30MinBreakRequirement());
		PutValue(initialValues, EXEMPTLOGTYPE, data.getExemptLogType().getValue());
		PutValue(initialValues, EXEMPTFROMELDUSE, data.getExemptFromEldUse());
		PutValue(initialValues, EXEMPTFROMELDUSECOMMENT, data.getExemptFromEldUseComment());
		PutValue(initialValues, DRIVESTARTSPEED, data.getDriveStartSpeed());
		PutValue(initialValues, MANDATEDRIVINGSTOPTIMEMINUTES, data.getMandateDrivingStopTimeMinutes());
		PutValue(initialValues, YARDMOVEALLOWED, data.getYardMoveAllowed());
        PutValue(initialValues, ISNONREGDRIVINGALLOWED, data.getIsNonRegDrivingAllowed());

		return initialValues;
	}

	///////////////////////////////////////////////////////////////////////////////////////
	// custom methods
	///////////////////////////////////////////////////////////////////////////////////////
}
