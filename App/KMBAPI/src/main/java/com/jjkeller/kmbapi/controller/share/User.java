package com.jjkeller.kmbapi.controller.share;

import android.content.Context;

import com.jjkeller.kmbapi.configuration.LoginCredentials;
import com.jjkeller.kmbapi.enums.DataProfileEnum;
import com.jjkeller.kmbapi.enums.DriverTypeEnum;
import com.jjkeller.kmbapi.enums.DrivingNotificationTypeEnum;
import com.jjkeller.kmbapi.enums.ExemptLogTypeEnum;
import com.jjkeller.kmbapi.enums.RuleSetTypeEnum;
import com.jjkeller.kmbapi.enums.SpecialDrivingCategoryConfigurationMessageEnum;
import com.jjkeller.kmbapi.enums.TimeZoneEnum;
import com.jjkeller.kmbapi.configuration.UserState;

import java.util.ArrayList;
import java.util.List;

public class User {

	private LoginCredentials credentials;
	private TimeZoneEnum homeTerminalTimeZone;
	private DriverTypeEnum driverType = new DriverTypeEnum(DriverTypeEnum.NULL);
	private RuleSetTypeEnum rulesetTypeEnum;
	private boolean isShortHaulException;
	private boolean is34HourResetAllowed;
	private double drivingStartDistanceMiles = .5;
	private int drivingStopTimeMinutes = 10;
	private DrivingNotificationTypeEnum drivingNotificationTypeEnum = new DrivingNotificationTypeEnum(DrivingNotificationTypeEnum.NULL);
	private RuleSetTypeEnum internationalUSRuleset;
	private RuleSetTypeEnum internationalCDRuleset;
	private List<RuleSetTypeEnum> availableRulesets;
	private DataProfileEnum dataProfile = new DataProfileEnum(DataProfileEnum.MINIMUMHOS);
	private String distanceUnits = "Miles";
	private long companyKey;
    private boolean isHaulingExplosivesAllowed = false;
    private boolean isHaulingExplosivesDefault = false;
	private boolean isOperatesSpecificVehiclesForOilfield = false;
	private boolean isPersonalConveyanceAllowed = false;
	// PBI #32111
	private boolean isHyrailAllowed = false;
	private boolean isMobileExemptLogAllowed = false;
	private boolean isExemptFrom30MinBreakRequirement = false;
	private ExemptLogTypeEnum exemptLogType = new ExemptLogTypeEnum(ExemptLogTypeEnum.NULL);
	private boolean exemptFromEldUse = false;
	private String exemptFromEldUseComment = "";
	private int driveStartSpeed = 5;
	private int mandateDrivingStopTimeMinutes = 5;
	private boolean yardMoveAllowed = false;
	private boolean isNonRegDrivingAllowed = false;
	private SpecialDrivingCategoryConfigurationMessageEnum _specialDrivingCategoryConfigurationMessage = SpecialDrivingCategoryConfigurationMessageEnum.NONE;
	private UserState userState = new UserState();

	public LoginCredentials getCredentials(){
		return this.credentials;
	}
	public void setCredentials(LoginCredentials credentials){
		this.credentials = credentials;
	}

	public TimeZoneEnum getHomeTerminalTimeZone(){
		return this.homeTerminalTimeZone;
	}
	public void setHomeTerminalTimeZone(TimeZoneEnum homeTerminalTimeZone){
		this.homeTerminalTimeZone = homeTerminalTimeZone;
	}

	public DriverTypeEnum getDriverType(){
		return this.driverType;
	}
	public void setDriverType(DriverTypeEnum driverType){
		this.driverType = driverType;
	}

	public RuleSetTypeEnum getRulesetTypeEnum(){
		return this.rulesetTypeEnum;
	}
	public void setRulesetTypeEnum(RuleSetTypeEnum rulesetTypeEnum){
		this.rulesetTypeEnum = rulesetTypeEnum;
	}

	public boolean getIsShorthaulException(){
		return this.isShortHaulException;
	}
	public void setIsShorthaulException(boolean isShortHaulException){
		this.isShortHaulException = isShortHaulException;
	}

	public boolean getIs34HourResetAllowed(){
		return this.is34HourResetAllowed;
	}
	public void setIs34HourResetAllowed(boolean is34HourResetAllowed){
		this.is34HourResetAllowed = is34HourResetAllowed;
	}

	public double getDrivingStartDistanceMiles(){
		return this.drivingStartDistanceMiles;
	}
	public void setDrivingStartDistanceMiles(double drivingStartDistanceMiles){
		this.drivingStartDistanceMiles = drivingStartDistanceMiles;
	}

	public int getDrivingStopTimeMinutes(){
		return this.drivingStopTimeMinutes;
	}
	public void setDrivingStopTimeMinutes(int drivingStopTimeMinutes){
		this.drivingStopTimeMinutes = drivingStopTimeMinutes;
	}

	public DrivingNotificationTypeEnum getDrivingNotificationTypeEnum(){
		return this.drivingNotificationTypeEnum;
	}
	public void setDrivingNotificationTypeEnum(DrivingNotificationTypeEnum drivingNotificationTypeEnum){
		this.drivingNotificationTypeEnum = drivingNotificationTypeEnum;
	}

	public RuleSetTypeEnum getInternationalUSRuleset(){
		return this.internationalUSRuleset;
	}
	public void setInternationalUSRuleset(RuleSetTypeEnum internationalUSRuleset){
		this.internationalUSRuleset = internationalUSRuleset;
	}

	public RuleSetTypeEnum getInternationalCDRuleset(){
		return this.internationalCDRuleset;
	}
	public void setInternationalCDRuleset(RuleSetTypeEnum internationalCDRuleset){
		this.internationalCDRuleset = internationalCDRuleset;
	}

	public List<RuleSetTypeEnum> getAvailableRulesets(){
		return this.availableRulesets;
	}
	public void setAvailableRulesets(List<RuleSetTypeEnum> availableRulesets){
		this.availableRulesets = availableRulesets;
	}

	public DataProfileEnum getDataProfile(){
		return this.dataProfile;
	}
	public void setDataProfile(DataProfileEnum dataProfile){
		this.dataProfile = dataProfile;
	}

	public String getDistanceUnits(){
		return this.distanceUnits;
	}
	public void setDistanceUnits(String distanceUnits){
		this.distanceUnits = distanceUnits;
	}

	public boolean getIsAuthenticated(){
		return this.credentials != null;
	}

	public long getCompanyKey(){
		return this.companyKey;
	}
	public void setCompanyKey(long companyKey){
		this.companyKey = companyKey;
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

	public boolean getIsHyrailAllowed() {
		return isHyrailAllowed;
	}

	public void setIsHyrailAllowed(boolean value) {
		this.isHyrailAllowed = value;
	}

	public boolean getIsMobileExemptLogAllowed() {
		return isMobileExemptLogAllowed;
	}

	public void setIsMobileExemptLogAllowed(boolean value) {
		this.isMobileExemptLogAllowed = value;
	}

	public boolean getIsExemptFrom30MinBreakRequirement() { return isExemptFrom30MinBreakRequirement;}

	public void setIsExemptFrom30MinBreakRequirement(boolean value) { this.isExemptFrom30MinBreakRequirement = value;}

	public ExemptLogTypeEnum getExemptLogType(){
		return this.exemptLogType;
	}

	public void setExemptLogType(ExemptLogTypeEnum exemptLogType){
		this.exemptLogType = exemptLogType;
	}

	public SpecialDrivingCategoryConfigurationMessageEnum getSpecialDrivingCategoryConfigurationMessageEnum(){
		return _specialDrivingCategoryConfigurationMessage;
	}
	public void setSpecialDrivingCategoryConfigurationMessageEnum(SpecialDrivingCategoryConfigurationMessageEnum value){
		_specialDrivingCategoryConfigurationMessage = value;
	}

    public void AddRuleset(RuleSetTypeEnum ruleset)
    {    	
    	if (this.getAvailableRulesets() == null)
    	{
    		this.availableRulesets = new ArrayList<RuleSetTypeEnum>();
    	}

    	if (!this.getAvailableRulesets().contains(ruleset))
    	{
    		this.getAvailableRulesets().add(ruleset);
    	}    	
    }
    
    /// <summary>
    /// Answer if there is a Canadian ruleset available to the user.
    /// </summary>
    /// <returns></returns>
    public boolean IsCanadianRulesetAvailable()
    {
        boolean isCDavail = false;

        if (this.getAvailableRulesets() != null && this.getAvailableRulesets().size() > 0)
        {
            for (RuleSetTypeEnum rs : this.getAvailableRulesets())
            {            
                if (rs.isCanadianRuleset())
                {
                    isCDavail = true;
                    break;
                }
            }
        }

        return isCDavail;
    }
    
    /**
     * Return true if at least one USFederal and one Canadian ruleset is available to the user
     * @param ctx
     * @return
     */
    public boolean AreBothInternationalRulesetsAvailable(Context ctx)
    {
    	boolean isUSavail = false;
        boolean isCDavail = false;

        if (this.getAvailableRulesets() != null && this.getAvailableRulesets().size() > 0)
        {
            for (RuleSetTypeEnum rs : this.getAvailableRulesets())
            {
                if (rs.getValue() == RuleSetTypeEnum.ALASKA_7DAY
                	|| rs.getValue() == RuleSetTypeEnum.ALASKA_8DAY
                	|| rs.getValue() == RuleSetTypeEnum.US60HOUR
            		|| rs.getValue() == RuleSetTypeEnum.US70HOUR
            		|| rs.getValue() == RuleSetTypeEnum.USMOTIONPICTURE_7DAY
            		|| rs.getValue() == RuleSetTypeEnum.USMOTIONPICTURE_8DAY)
                {
                    isUSavail = true;
                }
                if (rs.isCanadianRuleset())
                {
                    isCDavail = true;
                }
            }
        }

        return isUSavail && isCDavail;
    }
    
    /**
     * Return true if this user is allowed to drive internationally between the US and Canada.
     * In order to be allowed, there must at least one US Federal or US Motion Picture and at least one Canadian ruleset available.
     * @return
     */
    public boolean IsInternationalDrivingAllowed()
    {
    	boolean internationalAllowed = false;

        if (internationalCDRuleset != null && internationalUSRuleset != null &&
        	internationalCDRuleset.getValue() != RuleSetTypeEnum.NULL &&
        	internationalUSRuleset.getValue() != RuleSetTypeEnum.NULL &&
            (
        		rulesetTypeEnum.getValue() == RuleSetTypeEnum.ALASKA_7DAY || 
        		rulesetTypeEnum.getValue() == RuleSetTypeEnum.ALASKA_8DAY ||
        		rulesetTypeEnum.getValue() == RuleSetTypeEnum.US60HOUR || 
        		rulesetTypeEnum.getValue() == RuleSetTypeEnum.US70HOUR ||
        		rulesetTypeEnum.getValue() == RuleSetTypeEnum.USMOTIONPICTURE_7DAY ||
				rulesetTypeEnum.getValue() == RuleSetTypeEnum.USMOTIONPICTURE_8DAY ||
            	rulesetTypeEnum.getValue() == RuleSetTypeEnum.CANADIAN_CYCLE1 || 
            	rulesetTypeEnum.getValue() == RuleSetTypeEnum.CANADIAN_CYCLE2 )
            )
        {
            // international is allowed when the current rule is either US or CD
            // and both international border rules are defined
            internationalAllowed = true;
        }

        return internationalAllowed;
    }

	public boolean getExemptFromEldUse() {
		return exemptFromEldUse;
	}

	public void setExemptFromEldUse(boolean exemptFromEldUse) {
		this.exemptFromEldUse = exemptFromEldUse;
	}

	public String getExemptFromEldUseComment() {
		return exemptFromEldUseComment;
	}

	public void setExemptFromEldUseComment(String exemptFromEldUseComment) {
		this.exemptFromEldUseComment = exemptFromEldUseComment;
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

	public UserState getUserState(){
		return this.userState;
	}

	public boolean isPremiumProfile() {
		return getDataProfile() != null && (this.getDataProfile().getValue() == DataProfileEnum.FULL || this.getDataProfile().getValue() == DataProfileEnum.FULLWITHGEOFENCE);
	}

	public boolean isOffDutyWellSiteAllowed() {
		return getRulesetTypeEnum().isAnyOilFieldRuleset() && getIsOperatesSpecificVehiclesForOilfield();
	}

}
