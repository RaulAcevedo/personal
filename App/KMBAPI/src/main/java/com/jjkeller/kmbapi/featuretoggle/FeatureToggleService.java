package com.jjkeller.kmbapi.featuretoggle;

import com.jjkeller.kmbapi.configuration.AppSettings;
import com.jjkeller.kmbapi.configuration.GlobalState;

public class FeatureToggleService implements IFeatureToggleService {

	public FeatureToggleService(AppSettings appSettings) {

        _selectiveFeatureToggles = new SelectiveFeatureToggle(appSettings).IsEnabled();
		_eldMandateEnabled = new ELDMandate(appSettings).IsEnabled();
		_ignoreServerTime = new IgnoreServerTime(appSettings).IsEnabled();
		_showDebugFunctions = new ShowDebugFunctions(appSettings).IsEnabled();
		_useCloudServices = new UseCloudServices(appSettings).IsEnabled();
		_defaultTripInformation = new DefaultTripInformation(appSettings).IsEnabled();
		_alkCopilotEnabled = new AlkCopilotEnabled(appSettings).IsEnabled();
		_personalConveyanceEnabled = new PersonalConveyanceEnabled(appSettings).IsEnabled();
		_ignoreFirmareUpdate = new IgnoreFirmwareUpdate(appSettings).IsEnabled();
		_forceComplianceTabletMode = new ForceComplianceTabletMode(appSettings).IsEnabled(); 
		_setMobileStartTimestampToNull = new SetMobileStartTimestampToNull(appSettings).IsEnabled();
		_hyrailEnabled = new HyrailEnabled(appSettings).IsEnabled();
		_isNonRegDrivingEnabled = new  NonRegDrivingEnabled(appSettings).IsEnabled();
		_isGeotabInjectDataStallsEnabled = new GeotabInjectDataStallsEnabled(appSettings).IsEnabled();
		_isForceCrashesEnabled = new ForceCrashesEnabled(appSettings).IsEnabled();
	}

	private boolean _selectiveFeatureToggles = false;
	@Override
	public boolean getSelectiveFeatureTogglesEnabled(){
		return _selectiveFeatureToggles;
	}
	private boolean _setMobileStartTimestampToNull = false;
	@Override
	public boolean getSetMobileStartTimestampToNull() { return _setMobileStartTimestampToNull;}
	
	private boolean _eldMandateEnabled = true;
	@Override
	public boolean getIsEldMandateEnabled(){
		return _eldMandateEnabled;
	}
	
	private boolean _ignoreServerTime = true;
	@Override
	public boolean getIgnoreServerTime(){
		return _ignoreServerTime;
	}
	
	private boolean _showDebugFunctions = false;
	@Override
	public boolean getShowDebugFunctions(){
		return _showDebugFunctions;
	}

	private boolean _useCloudServices = false;
	@Override
	public boolean getUseCloudServices(){
		return _useCloudServices;
	}
	
	private boolean _defaultTripInformation = false; 
	@Override
	public boolean getDefaultTripInformation(){
		return _defaultTripInformation; 
	}
	
	private boolean _alkCopilotEnabled = true;
	@Override
	public boolean getAlkCopilotEnabled(){
		return _alkCopilotEnabled;
	}
	
	private boolean _personalConveyanceEnabled = true;
	@Override
	public boolean getPersonalConveyanceEnabled(){
		return _personalConveyanceEnabled;
	}

	private boolean _hyrailEnabled = true;
	@Override
	public boolean getHyrailEnabled() {
		return _hyrailEnabled;
	}

	private boolean _isNonRegDrivingEnabled = true;
	@Override
	public boolean getNonRegDrivingEnabled() {
		return _isNonRegDrivingEnabled;
	}

	private boolean _isGeotabInjectDataStallsEnabled = true;
	@Override
	public boolean getGeotabInjectDataStallsEnabled() {
		return _isGeotabInjectDataStallsEnabled;
	}

	private boolean _ignoreFirmareUpdate = false;
	@Override
	public boolean getIgnoreFirmwareUpdate(){
		return _ignoreFirmareUpdate;
	}
	
	private boolean _forceComplianceTabletMode = true;
	@Override
	public boolean getForceComplianceTabletMode(){
		return _forceComplianceTabletMode; 
	}

	private boolean _isForceCrashesEnabled = false;
	@Override
	public boolean getIsForceCrashesEnabled() { return _isForceCrashesEnabled; }

	private boolean _autoAssignUnidentifiedELDEvents = true;
	@Override
	public boolean getAutoAssignUnidentifiedELDEvents() { return _autoAssignUnidentifiedELDEvents; }



	@Override
    public void setSelectiveFeatureTogglesEnabled(boolean isEnabled)
    {
        _selectiveFeatureToggles = isEnabled;

        new SelectiveFeatureToggle(GlobalState.getInstance().getAppSettings(GlobalState.getInstance().getApplicationContext())).setIsEnabled(isEnabled);
    }
    @Override
	public void setIsEldMandateEnabled(boolean isEnabled){
		_eldMandateEnabled = isEnabled;
		
		new ELDMandate(GlobalState.getInstance().getAppSettings(GlobalState.getInstance().getApplicationContext())).setIsEnabled(isEnabled);
	}
	
	@Override
	public void setIgnoreServerTime(boolean isEnabled){
		_ignoreServerTime = isEnabled;
		
		new IgnoreServerTime(GlobalState.getInstance().getAppSettings(GlobalState.getInstance().getApplicationContext())).setIsEnabled(isEnabled);
	}
	
	@Override
	public void setShowDebugFunctions(boolean isEnabled){
		_showDebugFunctions = isEnabled;
		
		new ShowDebugFunctions(GlobalState.getInstance().getAppSettings(GlobalState.getInstance().getApplicationContext())).setIsEnabled(isEnabled);
	}
	
	@Override
	public void setUseCloudServices(boolean isEnabled){
		_useCloudServices = isEnabled;
		
		new UseCloudServices(GlobalState.getInstance().getAppSettings(GlobalState.getInstance().getApplicationContext())).setIsEnabled(isEnabled);
	}
	
	@Override
	public void setDefaultTripInformation(boolean isEnabled){
		_defaultTripInformation = isEnabled; 
		
		new DefaultTripInformation(GlobalState.getInstance().getAppSettings(GlobalState.getInstance().getApplicationContext())).setIsEnabled(isEnabled);
	}
	
	@Override
	public void setAlkCopilotEnabled(boolean isEnabled)
	{
		_alkCopilotEnabled = isEnabled;
		
		new AlkCopilotEnabled(GlobalState.getInstance().getAppSettings(GlobalState.getInstance().getApplicationContext())).setIsEnabled(isEnabled);
	}
	
	@Override
	public void setPersonalConveyanceEnabled(boolean isEnabled)
	{
		_personalConveyanceEnabled = isEnabled;
		
		new PersonalConveyanceEnabled(GlobalState.getInstance().getAppSettings(GlobalState.getInstance().getApplicationContext())).setIsEnabled(isEnabled);
	}

	@Override
	public void setIgnoreFirmwareUpdate(boolean isEnabled){
		_ignoreFirmareUpdate = isEnabled;
		
		new IgnoreFirmwareUpdate(GlobalState.getInstance().getAppSettings(GlobalState.getInstance().getApplicationContext())).setIsEnabled(isEnabled);
	}
	
	@Override
	public void setForceComplianceTabletMode(boolean isEnabled){
		_forceComplianceTabletMode = isEnabled;
		
		new ForceComplianceTabletMode(GlobalState.getInstance().getAppSettings(GlobalState.getInstance().getApplicationContext())).setIsEnabled(isEnabled);
	}

	@Override
	public void setSetMobileStartTimestampToNull(boolean isEnabled) {
		_setMobileStartTimestampToNull = isEnabled;

		new SetMobileStartTimestampToNull(GlobalState.getInstance().getAppSettings(GlobalState.getInstance().getApplicationContext())).setIsEnabled(isEnabled);
	}

	@Override
	public void setHyrailEnabled(boolean isEnabled)
	{
		_hyrailEnabled = isEnabled;

		new HyrailEnabled(GlobalState.getInstance().getAppSettings(GlobalState.getInstance().getApplicationContext())).setIsEnabled(isEnabled);
	}

	@Override
	public void setNonRegDrivingEnabled(boolean isEnabled)
	{
		_isNonRegDrivingEnabled = isEnabled;

		new NonRegDrivingEnabled(GlobalState.getInstance().getAppSettings(GlobalState.getInstance().getApplicationContext())).setIsEnabled(isEnabled);
	}

	@Override
	public void setGeotabInjectDataStallsEnabled(boolean isEnabled)
	{
		_isGeotabInjectDataStallsEnabled = isEnabled;

		new GeotabInjectDataStallsEnabled(GlobalState.getInstance().getAppSettings(GlobalState.getInstance().getApplicationContext())).setIsEnabled(isEnabled);
	}

	@Override
	public void setIsForceCrashesEnabled(boolean isEnabled)
	{
		_isForceCrashesEnabled = isEnabled;

		new ForceCrashesEnabled(GlobalState.getInstance().getAppSettings(GlobalState.getInstance().getApplicationContext())).setIsEnabled(isEnabled);
	}
}
