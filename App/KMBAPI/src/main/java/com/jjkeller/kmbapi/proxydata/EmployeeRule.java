package com.jjkeller.kmbapi.proxydata;

import com.jjkeller.kmbapi.enums.DataProfileEnum;
import com.jjkeller.kmbapi.enums.DriverTypeEnum;
import com.jjkeller.kmbapi.enums.ExemptLogTypeEnum;
import com.jjkeller.kmbapi.enums.RuleSetTypeEnum;
import com.jjkeller.kmbapi.enums.TimeZoneEnum;

import java.util.ArrayList;
import java.util.List;

public class EmployeeRule extends ProxyBase {

	///////////////////////////////////////////////////////////////////////////////////////
	// private members
	///////////////////////////////////////////////////////////////////////////////////////
    private TimeZoneEnum homeTerminalTimeZone = new TimeZoneEnum(TimeZoneEnum.EASTERNSTANDARDTIME);
    private DriverTypeEnum driverType = new DriverTypeEnum(DriverTypeEnum.PROPERTYCARRYING);
	private RuleSetTypeEnum rulesetTypeEnum = new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR);
    private boolean isShortHaulException = false;
    private boolean is34HourResetAllowed = false;
    private float drivingStartDistanceMiles = 0.5f;
    private int drivingStopTimeMinutes = 10;
    private RuleSetTypeEnum[] additionalRulesets = null;
    private RuleSetTypeEnum internationalCDRuleset = new RuleSetTypeEnum(RuleSetTypeEnum.NULL);
    private RuleSetTypeEnum internationalUSRuleset = new RuleSetTypeEnum(RuleSetTypeEnum.NULL);
    private DataProfileEnum dataProfile = new DataProfileEnum(DataProfileEnum.MINIMUMHOS);
    private String distanceUnits = "M";
    private boolean isHaulingExplosivesAllowed = true;
    private boolean isHaulingExplosivesDefault = false;
    private boolean isOperatesSpecificVehiclesForOilfield = false;
    private boolean isPersonalConveyanceAllowed = false;
    private boolean isHyrailUseAllowed = false;
    private boolean isMobileExemptLogAllowed = false;
	private boolean isExemptFrom30MinBreakRequirement = false;
    private ExemptLogTypeEnum exemptLogType = new ExemptLogTypeEnum(ExemptLogTypeEnum.NULL);
	private boolean exemptFromEldUse = false;
	private String exemptFromEldUseComment = "";
	private int driveStartSpeed = 5;
	private int mandateDrivingStopTimeMinutes = 5;
	private boolean yardMoveAllowed = false;
	private boolean isNonRegDrivingAllowed = false;
    
	///////////////////////////////////////////////////////////////////////////////////////
	// public get/set methods
	///////////////////////////////////////////////////////////////////////////////////////
	public TimeZoneEnum getHomeTerminalTimeZone() {
		return homeTerminalTimeZone;
	}

	public void setHomeTerminalTimeZone(TimeZoneEnum homeTerminalTimeZone) {
		this.homeTerminalTimeZone = homeTerminalTimeZone;
	}

	public DriverTypeEnum getDriverType() {
		return driverType;
	}

	public void setDriverType(DriverTypeEnum driverType) {
		this.driverType = driverType;
	}

	public RuleSetTypeEnum getRuleset() {
		return rulesetTypeEnum;
	}

	public void setRuleset(RuleSetTypeEnum ruleset) {
		this.rulesetTypeEnum = ruleset;
	}

	public boolean getIsShortHaulException() {
		return isShortHaulException;
	}

	public void setIsShortHaulException(boolean isShortHaulException) {
		this.isShortHaulException = isShortHaulException;
	}

	public boolean getIs34HourResetAllowed() {
		return is34HourResetAllowed;
	}

	public void setIs34HourResetAllowed(boolean is34HourResetAllowed) {
		this.is34HourResetAllowed = is34HourResetAllowed;
	}

	public float getDrivingStartDistanceMiles() {
		return drivingStartDistanceMiles;
	}

	public void setDrivingStartDistanceMiles(float drivingStartDistanceMiles) {
		this.drivingStartDistanceMiles = drivingStartDistanceMiles;
	}

	public int getDrivingStopTimeMinutes() {
		return drivingStopTimeMinutes;
	}

	public void setDrivingStopTimeMinutes(int drivingStopTimeMinutes) {
		this.drivingStopTimeMinutes = drivingStopTimeMinutes;
	}

	public RuleSetTypeEnum[] getAdditionalRulesets() {
		return additionalRulesets;
	}

	public void setAdditionalRulesets(RuleSetTypeEnum[] additionalRulesets) {
		this.additionalRulesets = additionalRulesets;
	}

	public RuleSetTypeEnum getIntCDRuleset() {
		return internationalCDRuleset;
	}

	public void setIntCDRuleset(RuleSetTypeEnum intCDRuleset) {
		this.internationalCDRuleset = intCDRuleset;
	}

	public RuleSetTypeEnum getIntUSRuleset() {
		return internationalUSRuleset;
	}

	public void setIntUSRuleset(RuleSetTypeEnum intUSRuleset) {
		this.internationalUSRuleset = intUSRuleset;
	}

	public DataProfileEnum getDataProfile() {
		return dataProfile;
	}

	public void setDataProfile(DataProfileEnum dataProfile) {
		this.dataProfile = dataProfile;
	}
	
	public String getDistanceUnits()
	{
		return this.distanceUnits;
	}
	
	public void setDistanceUnits(String distanceUnits)
	{
		this.distanceUnits = distanceUnits;
	}
	
	public boolean getIsHaulingExplosivesAllowed() {
		return isHaulingExplosivesAllowed;
	}

	public void setIsHaulingExplosivesAllowed(boolean value) {
		this.isHaulingExplosivesAllowed = value;
	}

	public boolean getIsHaulingExplosivesDefault() {
		return isHaulingExplosivesDefault;
	}

	public void setIsHaulingExplosivesDefault(boolean value) {
		this.isHaulingExplosivesDefault = value;
	}

	public boolean getIsOperatesSpecificVehiclesForOilfield() {
		return isOperatesSpecificVehiclesForOilfield;
	}

	public void setIsOperatesSpecificVehiclesForOilfield(boolean value) {
		this.isOperatesSpecificVehiclesForOilfield = value;
	}
	
	public boolean getIsPersonalConveyanceAllowed() {
		return isPersonalConveyanceAllowed;
	}
    public void setIsPersonalConveyanceAllowed(boolean value) {
        this.isPersonalConveyanceAllowed = value;
    }
	public void setIsHyrailUseAllowed(boolean value) {
		this.isHyrailUseAllowed = value;
	}

    public boolean getIsHyrailUseAllowed() {
        return isHyrailUseAllowed;
    }

	public boolean getIsMobileExemptLogAllowed() {
		return isMobileExemptLogAllowed;
	}

	public void setIsMobileExemptLogAllowed(boolean value) {
		this.isMobileExemptLogAllowed = value;
	}

	public boolean getIsExemptFrom30MinBreakRequirement() {return isExemptFrom30MinBreakRequirement;}

	public void setIsExemptFrom30MinBreakRequirement(boolean value) {this.isExemptFrom30MinBreakRequirement = value;}
	
	public ExemptLogTypeEnum getExemptLogType() {
		return exemptLogType;
	}

	public void setExemptLogType(ExemptLogTypeEnum exemptLogType) {
		this.exemptLogType = exemptLogType;
	}
	
	public void AddRuleset(RuleSetTypeEnum ruleset)
	{
		List<RuleSetTypeEnum> list = new ArrayList<RuleSetTypeEnum>();
		if(additionalRulesets != null)
		{
			for(RuleSetTypeEnum item : additionalRulesets)
			{
				list.add(item);
			}
		}
		
		if(!list.contains(ruleset))
		{
			list.add(ruleset);
			additionalRulesets = list.toArray(new RuleSetTypeEnum[list.size()]);
		}
	}

	public boolean getExemptFromEldUse() {
		return this.exemptFromEldUse;
	}

	public void setExemptFromEldUse(boolean value) {
		this.exemptFromEldUse = value;
	}

	public String getExemptFromEldUseComment() {
		return this.exemptFromEldUseComment;
	}

	public void setExemptFromEldUseComment(String value) {
		this.exemptFromEldUseComment = value;
	}

	public int getDriveStartSpeed() {
		return driveStartSpeed;
	}

	public void setDriveStartSpeed(int driveStartSpeed) {
		this.driveStartSpeed = driveStartSpeed;
	}

	public int getMandateDrivingStopTimeMinutes() {
		return mandateDrivingStopTimeMinutes;
	}

	public void setMandateDrivingStopTimeMinutes(int mandateDrivingStopTimeMinutes) {
		this.mandateDrivingStopTimeMinutes = mandateDrivingStopTimeMinutes;
	}

	public boolean getYardMoveAllowed() {
		return yardMoveAllowed;
	}

	public void setYardMoveAllowed(boolean yardMoveAllowed) {
		this.yardMoveAllowed = yardMoveAllowed;
	}

	public boolean getIsNonRegDrivingAllowed() {
		return isNonRegDrivingAllowed;
	}

	public void setIsNonRegDrivingAllowed(boolean isNonRegDrivingAllowed) {
		this.isNonRegDrivingAllowed = isNonRegDrivingAllowed;
	}

}
