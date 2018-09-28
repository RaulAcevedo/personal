package com.jjkeller.kmbapi.controller;

import android.content.Context;

import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.jjkeller.kmbapi.R;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.dataaccess.EmployeeLogFacade;
import com.jjkeller.kmbapi.controller.dataaccess.EmployeeRuleFacade;
import com.jjkeller.kmbapi.controller.interfaces.IEmployeeLogFacade;
import com.jjkeller.kmbapi.controller.share.ControllerBase;
import com.jjkeller.kmbapi.controller.share.KmbApplicationException;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.controller.utility.RESTWebServiceHelper;
import com.jjkeller.kmbapi.enums.DataProfileEnum;
import com.jjkeller.kmbapi.enums.DriverTypeEnum;
import com.jjkeller.kmbapi.enums.DrivingNotificationTypeEnum;
import com.jjkeller.kmbapi.enums.ExemptLogTypeEnum;
import com.jjkeller.kmbapi.enums.RuleSetTypeEnum;
import com.jjkeller.kmbapi.enums.SpecialDrivingCategoryConfigurationMessageEnum;
import com.jjkeller.kmbapi.enums.TimeZoneEnum;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;
import com.jjkeller.kmbapi.proxydata.EmployeeRule;
import com.jjkeller.kmbapi.proxydata.EobrConfiguration;

import java.io.IOException;
import java.util.Date;

public class EmployeeRuleController extends ControllerBase {

	public EmployeeRuleController(Context ctx)
	{
		super(ctx);
	}
	
	public void ApplyEmployeeRule(EmployeeRule empRule)
	{
		if (empRule != null)
		{
			this.SaveEmployeeRule(empRule);

			User user = GlobalState.getInstance().getCurrentUser();
			this.TransferEmployeeRuleToUser(user, empRule);
			
			GlobalState.getInstance().setCurrentUser(user);
			
            LogEntryController ctrlr = new LogEntryController(getContext());
            ctrlr.UpdateCurrentLogForEmployeeRuleChanges();
		}
	}
	
    /// <summary>
    /// Assign the initial ruleset for the user.
	/// This involves determining what was the most recent ruleset being followed
	/// by the user.
	/// If no ruleset info is found, then no change is made.
    /// </summary>
    public void AssignInitialRulesetForUser(User user)
    {
        IEmployeeLogFacade empLogFacade = new EmployeeLogFacade(getContext(), user);
		EmployeeLogEldEvent logEvent = empLogFacade.FetchMostRecentLogEventForUser();
        if (logEvent != null && logEvent.getRulesetType().getValue() != RuleSetTypeEnum.NULL)
        {
            // load the user's ruleset with the most recent ruleset 
            // assigned to a logevent
            user.setRulesetTypeEnum(logEvent.getRulesetType());
        }

        // if the user has both US and CD rules installed, then setup
        // border crossing rulesets
        if (user.AreBothInternationalRulesetsAvailable(getContext()))
        {
            // determine what the last USFederal ruleset was
            logEvent = empLogFacade.FetchMostRecentUSLogEventForUser();
            if (logEvent != null)
            {
                // set the user's US ruleset with the most recent US ruleset 
                // assigned to a logevent
                user.setInternationalUSRuleset(logEvent.getRulesetType());
            }
            else
            {
                // default is US70 when no event is found
                user.setInternationalUSRuleset(new RuleSetTypeEnum(RuleSetTypeEnum.US70HOUR));
            }

            // determine what the last CD ruleset was
            logEvent = empLogFacade.FetchMostRecentCanadianLogEventForUser();
            if (logEvent != null)
            {
                // set the user's CD ruleset with the most recent CD ruleset 
                // assigned to a logevent
                user.setInternationalCDRuleset(logEvent.getRulesetType());
            }
            else
            {
                // default is Cycle1 when no log event is found
                user.setInternationalCDRuleset(new RuleSetTypeEnum(RuleSetTypeEnum.CANADIAN_CYCLE1));
            }
        }
    }
    
    /// <summary>
    /// Update the international rulesets for the user.
    /// This involves determining what was the most recent ruleset being followed
    /// by the user.
    /// If no ruleset info is found, then no change is made.
    /// </summary>
    public void UpdateInternationalRulesetsForUser(User user)
    {
        IEmployeeLogFacade empLogFacade = new EmployeeLogFacade(getContext(), getCurrentUser());
		EmployeeLogEldEvent logEvent;

        // if the user has both US and CD rules installed, then setup
        // border crossing rulesets
        if (user.AreBothInternationalRulesetsAvailable(getContext()))
        {
            // determine what the last USFederal ruleset was
            logEvent = empLogFacade.FetchMostRecentUSLogEventForUser();
            if (logEvent != null)
            {
                // set the user's US ruleset with the most recent US ruleset 
                // assigned to a logevent
                user.setInternationalUSRuleset(logEvent.getRulesetType());
            }
            else
            {
                // default is US70 when no event is found
                user.setInternationalUSRuleset(new RuleSetTypeEnum(RuleSetTypeEnum.US70HOUR));
            }

            // determine what the last CD ruleset was
            logEvent = empLogFacade.FetchMostRecentCanadianLogEventForUser();
            if (logEvent != null)
            {
                // set the user's CD ruleset with the most recent CD ruleset 
                // assigned to a logevent
                user.setInternationalCDRuleset(logEvent.getRulesetType());
            }
            else
            {
                // default is Cycle1 when no log event is found
                user.setInternationalCDRuleset(new RuleSetTypeEnum(RuleSetTypeEnum.CANADIAN_CYCLE1));
            }
        }
    }
    
	public void TransferEmployeeRuleToUser(User user, EmployeeRule empRule)
	{
		if (empRule != null)
		{
			user.setDriverType(empRule.getDriverType());
			user.setHomeTerminalTimeZone(empRule.getHomeTerminalTimeZone());
			user.setIs34HourResetAllowed(empRule.getIs34HourResetAllowed());
			user.setIsShorthaulException(empRule.getIsShortHaulException());
			user.setDrivingStartDistanceMiles(empRule.getDrivingStartDistanceMiles());
			user.setDrivingStopTimeMinutes(empRule.getDrivingStopTimeMinutes());
			user.setDrivingNotificationTypeEnum(new DrivingNotificationTypeEnum(DrivingNotificationTypeEnum.ONEHOUR));
			user.setIsHaulingExplosivesAllowed(empRule.getIsHaulingExplosivesAllowed());
			user.setIsHaulingExplosivesDefault(empRule.getIsHaulingExplosivesDefault());
			user.setIsOperatesSpecificVehiclesForOilfield(empRule.getIsOperatesSpecificVehiclesForOilfield());
			user.setIsPersonalConveyanceAllowed(empRule.getIsPersonalConveyanceAllowed());
            user.setIsHyrailAllowed(empRule.getIsHyrailUseAllowed());
			user.setIsNonRegDrivingAllowed(empRule.getIsNonRegDrivingAllowed());
			user.setIsMobileExemptLogAllowed(empRule.getIsMobileExemptLogAllowed());
			user.setIsExemptFrom30MinBreakRequirement(empRule.getIsExemptFrom30MinBreakRequirement());

			user.setExemptLogType(empRule.getExemptLogType());
			
			if (user.getRulesetTypeEnum() == null)
			{
				user.setRulesetTypeEnum(empRule.getRuleset());
			}
			
			user.setInternationalCDRuleset(empRule.getIntCDRuleset());
			user.setInternationalUSRuleset(empRule.getIntUSRuleset());
			
			user.AddRuleset(empRule.getRuleset());
			user.AddRuleset(empRule.getIntCDRuleset());
			user.AddRuleset(empRule.getIntUSRuleset());
			
			if (empRule.getAdditionalRulesets() != null && empRule.getAdditionalRulesets().length > 0)
			{
				for (int i=0; i<empRule.getAdditionalRulesets().length; i++)
				{
					user.AddRuleset(empRule.getAdditionalRulesets()[i]);
				}
			}
			user.setDataProfile(empRule.getDataProfile());
			
			if (empRule.getDistanceUnits().equalsIgnoreCase("K"))
				user.setDistanceUnits(this.getContext().getString(R.string.kilometers));
			else
				user.setDistanceUnits(this.getContext().getString(R.string.miles));

			user.setExemptFromEldUse(empRule.getExemptFromEldUse());
			user.setExemptFromEldUseComment(empRule.getExemptFromEldUseComment());
			user.setDriveStartSpeed(empRule.getDriveStartSpeed());
			user.setMandateDrivingStopTimeMinutes(empRule.getMandateDrivingStopTimeMinutes());
			user.setYardMoveAllowed(empRule.getYardMoveAllowed());
		}
		else
		{
            // add default rule values, but this probably shouldn't be allowed to happen
            // because the user must authenticate to DMO at least once in which
            // case the EmployeeRule from DMO will be downloaded.
            user.setDriverType(new DriverTypeEnum(DriverTypeEnum.PROPERTYCARRYING));
            user.setHomeTerminalTimeZone(TimeZoneEnum.valueOfDMOEnum(TimeZoneEnum.DmoEnum_EasternStandardTime));
            user.setIs34HourResetAllowed(true);
            user.setIsShorthaulException(true);
            user.setDrivingStartDistanceMiles(0.5);
            user.setDrivingStopTimeMinutes(10);
            user.setDrivingNotificationTypeEnum(new DrivingNotificationTypeEnum(DrivingNotificationTypeEnum.ONEHOUR));
            user.setRulesetTypeEnum(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR));
            user.setDataProfile(new DataProfileEnum(DataProfileEnum.MINIMUMHOS));
            user.setAvailableRulesets(null);
            user.AddRuleset(user.getRulesetTypeEnum());
            user.setDistanceUnits(this.getContext().getString(R.string.miles));
            user.setIsMobileExemptLogAllowed(false);
			user.setIsExemptFrom30MinBreakRequirement(false);
            user.setExemptLogType(new ExemptLogTypeEnum(ExemptLogTypeEnum.NULL));
			user.setExemptFromEldUse(false);
			user.setExemptFromEldUseComment("");
			user.setDriveStartSpeed(5);
			user.setMandateDrivingStopTimeMinutes(5);
			user.setYardMoveAllowed(false);
		}
	}

	public void AssignSpecialDrivingCategoryConfigurationForLogin(EmployeeRule empRule) {
		if (GlobalState.getInstance().getCurrentUser().getSpecialDrivingCategoryConfigurationMessageEnum()
				== SpecialDrivingCategoryConfigurationMessageEnum.ACTIVATION) return;

		AssignSpecialDrivingCategoryConfigurationForRuleDownload(empRule);
	}

	public void AssignSpecialDrivingCategoryConfigurationForRuleDownload(EmployeeRule empRule) {
		setSpecialDrivingCategoryConfigurationMessageEnum(SpecialDrivingCategoryConfigurationMessageEnum.NONE);
		User user = GlobalState.getInstance().getCurrentUser();

		if (!GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) return;

		if (empRule == null) return;

		if (user.getYardMoveAllowed() != empRule.getYardMoveAllowed()
				&& user.getIsPersonalConveyanceAllowed() != empRule.getIsPersonalConveyanceAllowed()) {
			setSpecialDrivingCategoryConfigurationMessageEnum(
					SpecialDrivingCategoryConfigurationMessageEnum.PERSONALCONVEYANCEANDYARDMOVE);
			return;
		}

		if (user.getYardMoveAllowed() != empRule.getYardMoveAllowed()) {
			setSpecialDrivingCategoryConfigurationMessageEnum(SpecialDrivingCategoryConfigurationMessageEnum.YARDMOVE);
			return;
		}

		if (user.getIsPersonalConveyanceAllowed() != empRule.getIsPersonalConveyanceAllowed()) {
			setSpecialDrivingCategoryConfigurationMessageEnum(SpecialDrivingCategoryConfigurationMessageEnum.PERSONALCONVEYANCE);
			return;
		}
	}

	private void setSpecialDrivingCategoryConfigurationMessageEnum(
			SpecialDrivingCategoryConfigurationMessageEnum specialDrivingCategoryConfigurationMessageEnum) {
		User user = GlobalState.getInstance().getCurrentUser();
		user.setSpecialDrivingCategoryConfigurationMessageEnum(specialDrivingCategoryConfigurationMessageEnum);
	}

	public EmployeeRule EmployeeRuleForUser(User user)
	{
		EmployeeRuleFacade facade = new EmployeeRuleFacade(this.getContext(), user);
		EmployeeRule empRule = facade.Fetch();
		
		return empRule;
	}
	
	private void SaveEmployeeRule(EmployeeRule empRule)
	{
		EmployeeRuleFacade facade = new EmployeeRuleFacade(this.getContext(), GlobalState.getInstance().getCurrentUser());
		facade.Save(empRule);
	}


	public void DownloadEmployeeRules() throws KmbApplicationException
	{
		EmployeeRule empRule = new EmployeeRule();

		try
		{
			RESTWebServiceHelper rwsh = new RESTWebServiceHelper(getContext());
			Date changeTimestampUTC = this.getCurrentUser().getCredentials().getLastSubmitTimestampUtc();
			empRule = rwsh.DownloadEmployeeRuleSettings(changeTimestampUTC);
		}
		catch (JsonSyntaxException jse)
		{
			this.HandleExceptionAndThrow(jse, this.getContext().getString(R.string.downloademployeerules), this.getContext().getString(R.string.exception_webservicecommerror), this.getContext().getString(R.string.exception_serviceunavailable));
		}
		catch (JsonParseException e)
		{
			// when connected to a network, but unable to get to webservice "e" is null at times
			if (e == null)
				e = new JsonParseException(JsonParseException.class.getName());
			this.HandleExceptionAndThrow(e, this.getContext().getString(R.string.downloademployeerules), this.getContext().getString(R.string.exception_webservicecommerror), this.getContext().getString(R.string.exception_serviceunavailable));
		}
		catch (IOException ioe)
		{
			this.HandleExceptionAndThrow(ioe, this.getContext().getString(R.string.downloademployeerules), this.getContext().getString(R.string.exception_webservicecommerror), this.getContext().getString(R.string.exception_serviceunavailable));
		}

		this.AssignSpecialDrivingCategoryConfigurationForRuleDownload(empRule);
		this.ApplyEmployeeRule(empRule);
		
		// 9/12/11 JHM - When rules are downloaded, refresh the rulesets (initial and international)
		this.AssignInitialRulesetForUser(getCurrentUser());

        // download the eobr config, which is sort of like a 'rule' due
        // to the unit rules included in there
        EobrConfigController eobrConfigController = new EobrConfigController(this.getContext());
		EobrConfiguration config = eobrConfigController.DownloadConfigFromDMO();
		
        // download the company config in case there are any changes to that need to come down
        LoginController loginController = new LoginController(this.getContext());
        loginController.DownloadCompanyConfigSettings(GlobalState.getInstance().getCompanyConfigSettings(this.getContext()).getActivationCode());
        
        // if current user is the designated driver - update the thresholds in the TAB and assign the
        // driverIdCrc in the users credentials - in the event that new values were downloaded
        if (this.getCurrentUser().equals(this.getCurrentDesignatedDriver()))
        {
        	// AMO - 12/11/12 Set the EOBR threshold values using the Unit Rules or Company Rules
        	LogEntryController logCtrlr = new LogEntryController(this.getContext());
            int driverIdCrc = logCtrlr.SetThresholdValues(this.getCurrentUser());     	
        	
        	this.getCurrentDesignatedDriver().getCredentials().setDriverIdCrc(driverIdCrc);
        }

		if(GlobalState.getInstance().getCompanyConfigSettings(this.getContext()).getIsMotionPictureEnabled()) {
			loginController.DownloadMotionPictureAuthorites();
			loginController.DownloadMotionPictureProductions();
		}
	}
}
