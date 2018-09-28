package com.jjkeller.kmbapi.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;

import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.jjkeller.kmbapi.R;
import com.jjkeller.kmbapi.configuration.AppSettings;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.configuration.LoginCredentials;
import com.jjkeller.kmbapi.controller.EOBR.EobrReader;
import com.jjkeller.kmbapi.controller.dataaccess.UserFacade;
import com.jjkeller.kmbapi.controller.interfaces.IAPIController;
import com.jjkeller.kmbapi.controller.share.ControllerBase;
import com.jjkeller.kmbapi.controller.share.KmbApplicationException;
import com.jjkeller.kmbapi.controller.share.MandateObjectFactory;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.controller.utility.ErrorLogHelper;
import com.jjkeller.kmbapi.controller.utility.RESTWebServiceHelper;
import com.jjkeller.kmbapi.controller.utility.UserUtility;
import com.jjkeller.kmbapi.employeelogeldevents.Enums.CompositeEmployeeLogEldEventTypeEventCodeEnum;
import com.jjkeller.kmbapi.enums.SpecialDrivingCategoryConfigurationMessageEnum;
import com.jjkeller.kmbapi.featuretoggle.FeatureToggleService;
import com.jjkeller.kmbapi.proxydata.AuthenticationInfo;
import com.jjkeller.kmbapi.proxydata.CompanyConfigSettings;
import com.jjkeller.kmbapi.proxydata.EmployeeRule;
import com.jjkeller.kmbapi.proxydata.Location;
import com.jjkeller.kmbapi.proxydata.TeamDriver;
import com.jjkeller.kmbapi.proxydata.DataTransferMechanismStatus;
import com.jjkeller.kmbapi.realtime.MalfunctionManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;


public class LoginController extends ControllerBase{

	AuthenticationInfo _authInfo = null;
	private User newUser;
	
	public LoginController(Context ctx){
		super(ctx);
		newUser = new User();
	}

	public void DownloadMotionPictureAuthorites(){
		if(GlobalState.getInstance().getCompanyConfigSettings(this.getContext()).getIsMotionPictureEnabled()) {
			getMotionPictureAuthorities();
			getMotionPictureProductions();
		}
	}

	public void DownloadMotionPictureProductions(){
		if(GlobalState.getInstance().getCompanyConfigSettings(this.getContext()).getIsMotionPictureEnabled()) {
			getMotionPictureAuthorities();
			getMotionPictureProductions();
		}
	}

	@SuppressWarnings("unused")
	public boolean DownloadCompanyConfigSettings(String activationCode) throws KmbApplicationException
	{
		if(!GlobalState.getInstance().getAppSettings(this.getContext()).getAppSettingsLoaded())
		{
			this.HandleExceptionAndThrow(new KmbApplicationException(), this.getContext().getString(R.string.downloadcompanyconfigsettings), this.getContext().getString(R.string.exception_applicationunavailable), this.getContext().getString(R.string.exception_appsettingsfailure));
		}	
		
		try
		{
			RESTWebServiceHelper rwsh = new RESTWebServiceHelper(getContext());

			CompanyConfigSettings companyConfigSettings = rwsh.GetCompanyConfig(activationCode);
			GlobalState.getInstance().setCompanyConfigSettings(this.getContext(), companyConfigSettings);
			return true;
		}
		catch (JsonSyntaxException e)
		{
			this.HandleExceptionAndThrow(e, this.getContext().getString(R.string.downloadcompanyconfigsettings), this.getContext().getString(R.string.exception_webservicecommerror), this.getContext().getString(R.string.exception_serviceunavailable));
		}
		catch (JsonParseException e)
		{
			// when connected to a network, but unable to get to webservice "e" is null at times
			if (e == null)
				e = new JsonParseException(JsonParseException.class.getName());
			this.HandleExceptionAndThrow(e, this.getContext().getString(R.string.downloadcompanyconfigsettings), this.getContext().getString(R.string.exception_webservicecommerror), this.getContext().getString(R.string.exception_serviceunavailable));
		}
		catch (IOException e)
		{
			// if the exception passed back is because an invalid activation code we need to display that
			// instead of a generic web service communication error.  Let the method return the false retVal.
			if (!e.getMessage().equalsIgnoreCase(this.getContext().getString(R.string.invalid_activationcode)))
			{
				this.HandleExceptionAndThrow(e, this.getContext().getString(R.string.downloadcompanyconfigsettings), this.getContext().getString(R.string.exception_webservicecommerror), this.getContext().getString(R.string.exception_serviceunavailable));
			}
		}
		
		return false; 
	}
	
	public void DownloadCompanyConfigSettingsIntoGlobalState() throws KmbApplicationException		
	{		
		CompanyConfigSettings companyConfigSettings = GlobalState.getInstance().getCompanyConfigSettings(getContext()); 
		String activationCode = companyConfigSettings.getActivationCode();
		
		DownloadCompanyConfigSettings(activationCode);
	}

	public void getFeatureToggleSettings() {
		Context ctx = GlobalState.getInstance().getApplicationContext();
		CompanyConfigSettings companyConfigSettings = GlobalState.getInstance().getCompanyConfigSettings(ctx);

		if(!TextUtils.isEmpty(companyConfigSettings.getDmoCompanyId())) {
			SharedPreferences userPref = ctx.getSharedPreferences(ctx.getString(R.string.sharedpreferencefile), 0);
			if (userPref.getBoolean("SelectiveFeatureToggle", true)) {
				DownloadAndApplyFeatureToggles(companyConfigSettings.getDmoCompanyId());
			}
		}
	}

	public Bundle PerformLogin(String username, String password)
	{
		CompanyConfigSettings companyConfigSettings = GlobalState.getInstance().getCompanyConfigSettings(getContext());

		Bundle bundle = new Bundle();
		bundle.putBoolean("isAuthenticated", false);
		bundle.putBoolean("isAlreadyLoggedIn", false);
		
		boolean isTeamDriverLogin = false;
		
		if (this.getCurrentUser() != null)
		{
			if (this.getLoggedInUserList().size() > 0)
			{
                isTeamDriverLogin = true;
				for (int i=0; i<this.getLoggedInUserList().size(); i++)
				{
					User user = this.getLoggedInUserList().get(i);
					if (user.getCredentials().getUsername().compareTo(username) == 0)
					{
						bundle.putBoolean("isAlreadyLoggedIn", true);
						
						return bundle;
					}
				}
			}
		}
		
		newUser.setCompanyKey(companyConfigSettings.getPrimaryKey());

		LoginCredentials credentials = this.AuthenticateUser(newUser, username, password, isTeamDriverLogin);
		
		if (credentials != null)
		{
			bundle.putBoolean("isAuthenticated", true);
			
			if (!isTeamDriverLogin && this.getIsNetworkAvailable())
			{
				if (_authInfo != null)
				{
					syncDMOTime();
				}
			}
			
            applyEmployeeRuleToUser();
			
			this.setCurrentUser(newUser);
			if (this.getCurrentDesignatedDriver() == null)
			{
				LogEntryController logEntryController = setCurrentDesignatedDriver();

				int driverIdCrc = setThresholdValues(logEntryController);

				setCrcForDriverById(driverIdCrc);
			}

			getComplianceDates();

			if(companyConfigSettings.getIsMotionPictureEnabled()) {
				getMotionPictureAuthorities();
				getMotionPictureProductions();
			}
            
            this.AddLoggedInUser(newUser);
			this.setIsNewUserBeingLoggedIn(true);
            loadDataTransferMechanismStatus();
		}

		return bundle;
	}

	private void setCrcForDriverById(int driverIdCrc) {
		this.getCurrentDesignatedDriver().getCredentials().setDriverIdCrc(driverIdCrc);
	}

	private int setThresholdValues(LogEntryController logEntryController) {
		// AMO - 12/11/12 Set the EOBR threshold values using the Unit Rules or Company Rules
		// Upon login, get the most current settings from DMO
		EobrConfigController eobrConfigController = new EobrConfigController(this.getContext());                
		eobrConfigController.DownloadConfigFromDMO();
		int driverIdCrc = logEntryController.SetThresholdValues(newUser, true, false);
		return driverIdCrc;
	}

	private LogEntryController setCurrentDesignatedDriver() {
		// if the designated driver has not been assigned, then do it
		this.setCurrentDesignatedDriver(newUser);
		LogEntryController logEntryController = new LogEntryController(this.getContext());
		return logEntryController;
	}

	private void applyEmployeeRuleToUser() {
		// apply the Employee Rules to the user
		EmployeeRuleController ruleController = new EmployeeRuleController(this.getContext());
		EmployeeRule employeeRule = ruleController.EmployeeRuleForUser(newUser);
		ruleController.TransferEmployeeRuleToUser(newUser, employeeRule);
		ruleController.AssignInitialRulesetForUser(newUser);
        DateUtility.setHomeTerminalTimeDateFormatTimeZone(newUser.getHomeTerminalTimeZone().toTimeZone());
	}
	
	private void getComplianceDates() {
		// AMO - 04/04/2013 Get compliance dates
		try {
			LogCheckerComplianceDatesController ctrl = new LogCheckerComplianceDatesController(this.getContext());

			if (ctrl.getIsWebServicesAvailable()) {
				ctrl.DownloadLogCheckerComplianceDates();
			}
		} catch (KmbApplicationException e) {
			this.HandleException(e);
		}
	}

	private void getMotionPictureAuthorities(){
		try {
			MotionPictureController motionPictureController = new MotionPictureController(this.getContext());
			if (motionPictureController.getIsWebServicesAvailable()){
				motionPictureController.DownloadMotionPictureAuthorities();
			}
		} catch (KmbApplicationException e){
			this.HandleException(e);
		}
	}

	private void getMotionPictureProductions(){
		try {
			MotionPictureController motionPictureController = new MotionPictureController(this.getContext());
			if (motionPictureController.getIsWebServicesAvailable()){
				motionPictureController.DownloadMotionPictureProductions();
			}

		} catch (KmbApplicationException e){
			this.HandleException(e);
		}
	}

	// Load next transfer date
    private void loadDataTransferMechanismStatus(){
        try {
			// Refresh the Global Data Transfer Mechanism Status data
			DataTransferMechanismStatusController ctrlr = new DataTransferMechanismStatusController(getContext());
			DataTransferMechanismStatus kmbData = ctrlr.GetKMBDataTransferMechanismStatus();
			if (kmbData.getDateTransferred() != null) {
				ctrlr.GetEncompassDataTransferMechanismStatus(kmbData.getTransferId());
			}

        } catch (Exception e){
            this.HandleException(e);
        }
    }


	private LoginCredentials persistCredentials(String username, String password) {
		LoginCredentials credentials;
		// attempt to read the credentials from the db
		UserFacade facade = new UserFacade(this.getContext(), newUser);		
		credentials = facade.Fetch(username);
		
		if(credentials == null){
			// no credentials stored yet, so create a new one
			credentials = new LoginCredentials();
		}

		credentials.setUsername(username);
		credentials.setPassword(password);
		credentials.setEmployeeId(_authInfo.getEmployeeId());
		credentials.setEmployeeCode(_authInfo.getEmployeeCode());
		credentials.setEmployeeFullName(_authInfo.getEmployeeFullName());
		credentials.setLastLoginTimestampUtc(DateUtility.getCurrentDateTimeUTC());
		credentials.setHomeTerminalDOTNumber(_authInfo.getHomeTerminalDOTNumber());
		credentials.setHomeTerminalAddressLine1(_authInfo.getHomeTerminalAddressLine1());
		credentials.setHomeTerminalAddressLine2(_authInfo.getHomeTerminalAddressLine2());
		credentials.setHomeTerminalCity(_authInfo.getHomeTerminalCity());
		credentials.setHomeTerminalStateAbbrev(_authInfo.getHomeTerminalStateAbbrev());
		credentials.setHomeTerminalZipCode(_authInfo.getHomeTerminalZipCode());
		credentials.setDriverLicenseNumber(_authInfo.getDriverLicenseNumber());
		credentials.setDriverLicenseState(_authInfo.getDriverLicenseState());
		credentials.setFirstName(_authInfo.getFirstName());
		credentials.setLastName(_authInfo.getLastName());
		
		facade.Save(credentials);
		return credentials;
	}

	private void syncDMOTime() {
		// Sync time between device and DMO.
		Date syncTimeUtc = _authInfo.getClockSyncTimestamp();
		// ignoreServerTime
		if(GlobalState.getInstance().getFeatureService().getIgnoreServerTime())
		{
			 		
		}else
		{
			DateUtility.SetSystemTime(syncTimeUtc);   
		}
	}
	
	public LoginCredentials AuthenticateUser(User newUser, String username, String password, boolean isTeamDriverLogin)
	{
		LoginCredentials credentials = null;
		
		if (this.getIsWebServicesAvailable())
		{		
			try
			{
				RESTWebServiceHelper rwsh = new RESTWebServiceHelper(getContext());
				_authInfo = rwsh.IsAuthenticated(username, password);
			}
			catch (JsonSyntaxException e)
			{
				this.HandleException(e, this.getContext().getString(R.string.authenticateuser));
				credentials = this.IsLocallyAuthenticated(newUser, username, password);
			}
			catch (JsonParseException e)
			{
				// when connected to a network, but unable to get to webservice "e" is null
				if (e == null)
					e = new JsonParseException(JsonParseException.class.getName());
				this.HandleException(e, this.getContext().getString(R.string.authenticateuser));
				credentials = this.IsLocallyAuthenticated(newUser, username, password);
			}
			catch (IOException e)
			{
				this.HandleException(e, this.getContext().getString(R.string.authenticateuser));
				credentials = this.IsLocallyAuthenticated(newUser, username, password);
			}
		
			if(_authInfo == null){
				credentials = this.IsLocallyAuthenticated(newUser, username, password);				
			}			
			else if (_authInfo.getIsAuthenticated())
			{
	            if (!isTeamDriverLogin && this.getIsNetworkAvailable())
	            {
	            	if (_authInfo != null)
	            	{
	            		// Sync time between device and DMO.
	            		Date syncTimeUtc = _authInfo.getClockSyncTimestamp();
	            		// ignoreServerTime
	            		if(GlobalState.getInstance().getFeatureService().getIgnoreServerTime())
	            		{
	            			 		
	            		}else
	            		{
	            			DateUtility.SetSystemTime(syncTimeUtc);   
	            		}
	            	}
	            }
	            
	            credentials = this.persistCredentials(username, password);
				newUser.setCredentials(credentials);
				this.setCurrentUser(newUser);
				this.UpdateLogCheckerWebServiceCredentials(credentials);

				GlobalState.getInstance().setCurrentEmployeeLog(null);

				if (GlobalState.getInstance().isNewActivation())
					this.getCurrentUser()
							.setSpecialDrivingCategoryConfigurationMessageEnum(
									SpecialDrivingCategoryConfigurationMessageEnum.ACTIVATION);

				EmployeeRuleController empRuleController = new EmployeeRuleController(this.getContext());
				empRuleController.TransferEmployeeRuleToUser(GlobalState.getInstance().getCurrentUser(), _authInfo.getEmployeeRule());
				empRuleController.AssignSpecialDrivingCategoryConfigurationForLogin(_authInfo.getEmployeeRule());
				empRuleController.ApplyEmployeeRule(_authInfo.getEmployeeRule());
			}
		}
		else
		{
			credentials = this.IsLocallyAuthenticated(newUser, username, password);
		}
		
		return credentials;
	}
	
	private LoginCredentials IsLocallyAuthenticated(User newUser, String username, String password)
	{
		UserFacade facade = new UserFacade(this.getContext(), newUser);
		LoginCredentials credentials = facade.IsAuthenticated(username, password);
		
		if(credentials != null)
		{
			this.setCurrentUser(newUser);
			this.UpdateLogCheckerWebServiceCredentials(credentials);
		}
		
		return credentials;
	}
	
	@SuppressWarnings("unused")
	public LoginCredentials GetAuthenticationInformation(String username) 
	{ 
		LoginCredentials credentials = null;
		User user = this.getCurrentUser(); 
		
		if (this.getIsWebServicesAvailable())
		{		
			try
			{
				RESTWebServiceHelper rwsh = new RESTWebServiceHelper(getContext());
				_authInfo = rwsh.GetAuthenticationInformation(username);
			}
			catch (JsonSyntaxException e)
			{
				this.HandleException(e, this.getContext().getString(R.string.authenticateuser));
				credentials = this.GetLocalAuthenticatedInformation(user, username, username);
			}
			catch (JsonParseException e)
			{
				// when connected to a network, but unable to get to webservice "e" is null
				if (e == null)
					e = new JsonParseException(JsonParseException.class.getName());
				this.HandleException(e, this.getContext().getString(R.string.authenticateuser));
				credentials = this.GetLocalAuthenticatedInformation(user, username, username);
			}
			catch (IOException e)
			{
				this.HandleException(e, this.getContext().getString(R.string.authenticateuser));
				credentials = this.GetLocalAuthenticatedInformation(user, username, username);
			}
		
			if(!_authInfo.getIsAuthenticated()){
				credentials = this.GetLocalAuthenticatedInformation(user, username, username);				
			}			
			else
			{
				// Save Credentials to the database.  
	            credentials = this.persistCredentials(username, username);
			}
		}
		else
		{
			credentials = this.GetLocalAuthenticatedInformation(user, username, username);
		}
		
		return credentials;
	}
	
	private LoginCredentials GetLocalAuthenticatedInformation(User newUser, String username, String password)
	{
		UserFacade facade = new UserFacade(this.getContext(), newUser);
		LoginCredentials credentials = facade.IsAuthenticated(username, password);

		return credentials;
	}
	
	private void AddLoggedInUser(User user)
	{
		ArrayList<User> list = this.getLoggedInUserList();
		
		if (!list.contains(user))
		{
			list.add(user);
						
			this.setLoggedInUserList(list);
			
			//trigger uploading of available hours
			new HosAuditController(getContext()).ForceServerUpdate();
		}
	}
	
	public void PerformReadOnlyLogout()
	{
        ErrorLogHelper.RecordMessage(this.getContext(), this.getContext().getString(R.string.errorlog_logincontrollerreadonlylogout));
        
        // save the logout time
        UserFacade facade = new UserFacade(getContext(), this.getCurrentUser());
        facade.Logout();
        
        RemoveCurrentUserFromLoginList();
        
        if(GlobalState.getInstance().getLoggedInUserList().size() > 0)
        {
            User nextArbitraryUser = this.getLoggedInUserList().get(0);
            this.SwitchUserContext(nextArbitraryUser);
            LogEntryController logEntryCntrlr = new LogEntryController(getContext());

            if (logEntryCntrlr.IsTheDriver(nextArbitraryUser))            		
                logEntryCntrlr.SwitchDriverContext(nextArbitraryUser);
        } else
        {
        	GlobalState.getInstance().setCurrentUser(null);
        	GlobalState.getInstance().setCurrentDesignatedDriver(null);
        	GlobalState.getInstance().setCurrentEmployeeLog(null);
        	GlobalState.getInstance().setCurrentDriversLog(null);
        }
	}
	
	public void PerformLogout(String timeOffset, Date exactTime, int dutyStatus, String locationText, String annotation)
	{
		IAPIController empLogController = MandateObjectFactory.getInstance(getContext(), GlobalState.getInstance().getFeatureService()).getCurrentEventController();
		try {
			empLogController.CreateLoginLogoutEvent(CompositeEmployeeLogEldEventTypeEventCodeEnum.LogoutEvent);
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			// DO NOTHING FOR NOW.  Email being sent from TH to TD
			e.printStackTrace();
		}
		
        ErrorLogHelper.RecordMessage(this.getContext(), this.getContext().getString(R.string.errorlog_logincontrollerlogout));

        // first suspend the EOBR so that updates don't come through after we've logged off
        LogEntryController logEntryCntrlr = new LogEntryController(getContext());
        logEntryCntrlr.SuspendUpdates();

        boolean isCurrentUserTheDriver = logEntryCntrlr.IsCurrentUserTheDriver();

        // If there is more than one user logged in, get the next available one
        User nextArbitraryUser = null;
    	if (this.getLoggedInUserList().size() > 1)
    	{
            for (User user : this.getLoggedInUserList())
            {
            	if (!logEntryCntrlr.IsTheCurrentActiveUser(user))
            	{
            		nextArbitraryUser = user;
            		break;
            	}
            }
    	}
        
        // If current user is the driver and is logging out while in driving status, clear thresholds and force a read to get a generated DRIVE_OFF event
        if (isCurrentUserTheDriver)
        {
            if (nextArbitraryUser != null)
            {
            	logEntryCntrlr.SetThresholdValues(nextArbitraryUser, true, false);
            }
        	else if (EobrReader.getIsEobrDevicePhysicallyConnected())
        	{
        		new SystemStartupController(getContext()).SetUnassignedDriverThreshold();
        	}
    		logEntryCntrlr.ForceImmediateRead();
        }

        // determine the number of minutes to offset the start time
        int offsetMinutes = 0;
		Date now = this.getCurrentClockHomeTerminalTime();
        if(timeOffset.compareToIgnoreCase(this.getContext().getString(com.jjkeller.kmbapi.R.string.timeoffsetkind_15minutes)) == 0)
        {
            offsetMinutes = 15;
        }
        else if(timeOffset.compareToIgnoreCase(this.getContext().getString(com.jjkeller.kmbapi.R.string.timeoffsetkind_30minutes)) == 0)
        {
            offsetMinutes = 30;
        }
        else if(timeOffset.compareToIgnoreCase(this.getContext().getString(com.jjkeller.kmbapi.R.string.timeoffsetkind_1hour)) == 0)
        {
            offsetMinutes = 60;
        }
        else if(timeOffset.compareToIgnoreCase(this.getContext().getString(com.jjkeller.kmbapi.R.string.timeoffsetkind_exacttime)) == 0)
        {
            if (exactTime.compareTo(now) <= 0)
            {
            	// TODO Handle raising exception when Exact time is not in the future.
                //throw new ApplicationException("Logout time must be in the future.");
            }
            long diff = exactTime.getTime() - now.getTime();
            offsetMinutes = (int)(diff / 60000);  // Convert milliseconds diff to minutes
        }
        		
        // determine the offduty location
        Location lastKnownLocation = logEntryCntrlr.GetCurrentEventValues_Location();

		// When a manual location is entered, use that.
		if (locationText.length() > 0) {
			lastKnownLocation.setName(locationText);
		}

		// add the duty status change event to the current log, passing in the selected dutystatus
		logEntryCntrlr.AddLogoutEventToCurrentLog(offsetMinutes, lastKnownLocation, dutyStatus, annotation);

		// Update DMO with the latest duty hours available
        // Note: data profiles are still checked for each user - it's just at a lower level now.
        HosAuditController auditController = new HosAuditController(getContext());
        boolean hoursAvailableSubmitted = auditController.PerformServerUpdate();
        if (!hoursAvailableSubmitted)
        {
			String msg = this.getContext().getString(com.jjkeller.kmbapi.R.string.msglogincontroller_failedtosubmitdutyhoursavailable);
            ErrorLogHelper.RecordMessage(this.getContext(), msg);
        }
        
        /*
        // save the preference of the off duty offset
        Globals.UserPreference.OffDutyOffset = timeOffset;
        Globals.UserPreference.Save();
		*/
        
        // save the logout time
        UserFacade facade = new UserFacade(getContext(), this.getCurrentUser());
        facade.Logout();

        // are there team drivers logged in?
        if (this.getLoggedInUserList().size() > 1 && !GlobalState.getInstance().getCompanyConfigSettings(this.getContext()).getMultipleUsersAllowed())
        {
            TeamDriverController teamController = new TeamDriverController(getContext());
            teamController.LogoutTeamDriver(this.getCurrentUser());
        }
        
       boolean isSeparateDevice = GlobalState.getInstance().getTeamDriverMode() == GlobalState.TeamDriverModeEnum.SEPARATEDEVICE;
		
        // New TeamDriver workflow 
        if (isSeparateDevice && !GlobalState.getInstance().getCompanyConfigSettings(this.getContext()).getMultipleUsersAllowed()){
        	TeamDriverController teamController = new TeamDriverController(getContext()); 
        	ArrayList<TeamDriver> teamDriverList = teamController.TeamDriversAvailableToBeEnded();
        	
            for (int index = 0; index < teamDriverList.size(); index++)
            {
            	 // calc the timestamp that it is right now
                Date endTimestamp = this.getCurrentClockHomeTerminalTime();

                // end this team driver across all logged in members of the team
				User teamDriverUser = UserUtility.getUserForKMBUserName(this.getContext(), teamDriverList.get(index).getKMBUsername());
				teamController.EndTeamDriver(teamDriverList.get(index).getEmployeeCode(), endTimestamp, teamDriverUser);
            }
        }

        // before we clear the logged in user list, we need to clear any active malfunctions or data diagnostic events
		EobrReader eobr = EobrReader.getInstance();
		if (eobr != null) {
			if (eobr.getCurrentConnectionState() == EobrReader.ConnectionState.ONLINE) {
				MalfunctionManager.getInstance().clearActiveMalfunctionsWhenDoneWithTab();
				MalfunctionManager.getInstance().clearActiveDataDiagnosticsWhenDoneWithTab();
			}
		}

        RemoveCurrentUserFromLoginList();
        
        // are there team drivers still logged in?
        if (nextArbitraryUser != null) {
			// switch context to the next available one
            this.SwitchUserContext(nextArbitraryUser);
            if ((isCurrentUserTheDriver && this.getLoggedInUserList().size() >= 1) || logEntryCntrlr.IsTheDriver(nextArbitraryUser))
            {
                logEntryCntrlr.SwitchDriverContext(nextArbitraryUser);
            }

            // resume updates from the EOBR because there are still live users
            logEntryCntrlr.ResumeUpdates();
        }

		if(GlobalState.getInstance().getCompanyConfigSettings(this.getContext()).getIsMotionPictureEnabled()) {
			getMotionPictureAuthorities();
			getMotionPictureProductions();
		}
	}

	private void RemoveCurrentUserFromLoginList() {
		// remove the current user from the login list
        ArrayList<User> list = this.getLoggedInUserList();
        String currentUserName = this.getCurrentUser().getCredentials().getUsername();
        int currentUserIndex = -1;
        
        // figure out where the current user is in the list of logged in users
        for (int index = 0; index < list.size(); index++)
        {
            if (list.get(index).getCredentials().getUsername().compareTo(currentUserName) == 0)
            {
                // found the index of the current user
                currentUserIndex = index;
                break;
            }
        }
        
        if (currentUserIndex >= 0)
        {
            // remove the user, and resave the list of logged in users
            list.remove(currentUserIndex);
            this.setLoggedInUserList(list);
        }
	}
	
    /// <summary>
    /// Submit all records to DMO.  This include not only logs, but unassigned
    /// items, engine records, etc.
    /// Answer if everything was successfully submitted.
    /// </summary>
    public boolean SubmitAllRecords(User submittingUser, boolean excludeTodaysLog)
    {
        IAPIController empLogController = MandateObjectFactory.getInstance(this.getContext(),GlobalState.getInstance().getFeatureService()).getCurrentEventController();
        return empLogController.SubmitAllRecords(submittingUser, excludeTodaysLog);
    }

    @SuppressWarnings("unused")
	public boolean ChangePassword(String newPassword)
    {
        boolean isSuccessful = false;

		try
		{
			RESTWebServiceHelper rwsh = new RESTWebServiceHelper(getContext());
			_authInfo = rwsh.ChangePassword(newPassword);
			isSuccessful = true;
		}
		catch (JsonSyntaxException e)
		{
			this.HandleException(e, this.getContext().getString(R.string.authenticateuser));
		}
		catch (JsonParseException e)
		{
			// when connected to a network, but unable to get to webservice "e" is null at times
			if (e == null)
				e = new JsonParseException(JsonParseException.class.getName());
			this.HandleException(e, this.getContext().getString(R.string.authenticateuser));
		}
		catch (IOException e)
		{
			this.HandleException(e, this.getContext().getString(R.string.authenticateuser));
		}

        // If successfully updated on DMO, update the new password in the local login database
		if(isSuccessful)
		{
			try
			{
		        LoginCredentials credentials = this.getCurrentUser().getCredentials();
		        credentials.setPassword(newPassword);
		        UserFacade facade = new UserFacade(this.getContext(), this.getCurrentUser());
		        facade.Save(credentials);
		
		        this.getCurrentUser().setCredentials(credentials);
			}
			catch(Throwable ex)
			{
				ErrorLogHelper.RecordMessage("Setting new password in locally DB failed.  Password updated on DMO.");
			}
		}
		
        return isSuccessful;
    }

    public boolean getIsNewUserBeingLoggedIn()
    {
    	return GlobalState.getInstance().getIsNewUserLogin();
    }
    public void setIsNewUserBeingLoggedIn(boolean isNewUserLogin)
    {
    	GlobalState.getInstance().setIsNewUserLogin(isNewUserLogin);
    }   
    
    public boolean IsEobrConfigurationNeeded()
    {
    	boolean isConfigNeeded = false;
    	if (EobrReader.getIsEobrDeviceAvailable())
    	{
    		isConfigNeeded = EobrReader.getInstance().IsEobrConfigurationNeeded();
    	}
    	
    	return isConfigNeeded;
    }
    
    public boolean getIsUserRequiredToChangePassword()
    {
    	boolean answer = false;
   
    	if (_authInfo != null)
    	{
    		if (_authInfo.getRequiredToChangePassword())
    			answer = true;
    	}
    	
    	return answer;
    }
    
    /// <summary>
    /// Login the current user as a team driver.
    /// The current logged in user is the new member of the team.
    /// The timestamp is the time that the team should be started.
    /// </summary>
    /// <param name="startTimestamp">time when the team starts</param>
    public void LoginNewTeamDriver(Date startTimestamp)
    {
        // save the who the current user is
        //User newTeamDriver = this.CurrentUser;
        User newTeamDriver = this.getCurrentUser();

        // login and start the team driver across all of the active logs
        TeamDriverController ctrlr = new TeamDriverController(this.getContext());
        
        try {
			if(GlobalState.getInstance().getTeamDriverMode() != GlobalState.TeamDriverModeEnum.NONE)
            	ctrlr.LoginTeamDriver(newTeamDriver, startTimestamp);
			else
				ctrlr.LoginMultipleUserDriver(newTeamDriver, startTimestamp);
		} catch (Exception e) {
			this.HandleException(e);
		}
        

        // switch back to this user after all the team driving relationships are complete
        this.SwitchUserContext(newTeamDriver);
    }

    public void DownloadAndApplyFeatureToggles(String companyId)
	{
		try{
			FeatureToggleController controller = new FeatureToggleController(this.getContext());
			if(controller.getIsWebServicesAvailable())
				controller.DownloadAndApplyFeatureToggles(companyId);
		}
		catch (KmbApplicationException e){
			this.HandleException(e);
		}
	}
}

