package com.jjkeller.kmbapi.controller;

import android.content.Context;
import android.content.Intent;

import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.jjkeller.kmbapi.R;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.configuration.LoginCredentials;
import com.jjkeller.kmbapi.controller.EOBR.EobrReader;
import com.jjkeller.kmbapi.controller.dataaccess.EmployeeLogEldEventFacade;
import com.jjkeller.kmbapi.controller.dataaccess.EventReassignmentFacade;
import com.jjkeller.kmbapi.controller.interfaces.IAPIController;
import com.jjkeller.kmbapi.controller.share.ControllerBase;
import com.jjkeller.kmbapi.controller.share.KmbApplicationException;
import com.jjkeller.kmbapi.controller.share.MandateObjectFactory;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.controller.utility.EmployeeLogUtilities;
import com.jjkeller.kmbapi.controller.utility.RESTWebServiceHelper;
import com.jjkeller.kmbapi.datatransferfilestatus.DataTransferFileStatusService;
import com.jjkeller.kmbapi.enums.DutyStatusEnum;
import com.jjkeller.kmbapi.enums.ExemptLogTypeEnum;
import com.jjkeller.kmbapi.proxydata.DrivingEventReassignmentMapping;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;
import com.jjkeller.kmbapi.proxydata.TeamDriver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class TeamDriverController extends ControllerBase {

	public TeamDriverController(Context ctx){
		super(ctx);
	}
	
    public VehicleMotionDetector getVehicleMotionDetector(){
    	return GlobalState.getInstance().getVehicleMotionDetector(getContext());
    }

	/// <summary>
    /// Download the employee's fullname from DMO, given only the employeeCode
    /// to use for lookup.
    /// </summary>
    /// <param name="employeeCode"></param>
    /// <returns></returns>
	@SuppressWarnings("unused")
	public String DownloadEmployeeName(String employeeCode) throws KmbApplicationException
	{
		String name = null;
		if (this.getIsNetworkAvailable())
		{
			try
			{
				// get utc time from DMO
				RESTWebServiceHelper rwsh = new RESTWebServiceHelper(getContext());
				return rwsh.EmployeeNameForCode(employeeCode);
			}
			catch (JsonSyntaxException jse)
			{
				this.HandleExceptionAndThrow(jse, this.getContext().getString(R.string.downloademployeename), this.getContext().getString(R.string.exception_webservicecommerror), this.getContext().getString(R.string.exception_serviceunavailable));
			}
			catch (JsonParseException jpe)
			{
				// when connected to a network, but unable to get to webservice "e" is null
				if (jpe == null)
					jpe = new JsonParseException(JsonParseException.class.getName());
				this.HandleExceptionAndThrow(jpe, this.getContext().getString(R.string.downloademployeename), this.getContext().getString(R.string.exception_webservicecommerror), this.getContext().getString(R.string.exception_serviceunavailable));
			}
			catch (IOException ioe)
			{
				this.HandleExceptionAndThrow(ioe, this.getContext().getString(R.string.downloademployeename), this.getContext().getString(R.string.exception_webservicecommerror), this.getContext().getString(R.string.exception_serviceunavailable));
			}
		}
		else
		{
			//throw new KmbApplicationException("No network connection is available.");
		}

		return name;
	}
	
	@SuppressWarnings("unused")
	public String DownloadEmployeeNameForKMBUser(String userName) throws KmbApplicationException
	{
		String name = null;
		if (this.getIsWebServicesAvailable())
		{
			try
			{
				// get utc time from DMO
				RESTWebServiceHelper rwsh = new RESTWebServiceHelper(getContext());
				return rwsh.EmployeeNameForKMBUserName(userName);
			}
			catch (JsonSyntaxException jse)
			{
				this.HandleExceptionAndThrow(jse, this.getContext().getString(R.string.downloademployeename), this.getContext().getString(R.string.exception_webservicecommerror), this.getContext().getString(R.string.exception_serviceunavailable));
			}
			catch (JsonParseException jpe)
			{
				// when connected to a network, but unable to get to webservice "e" is null
				if (jpe == null)
					jpe = new JsonParseException(JsonParseException.class.getName());
				this.HandleExceptionAndThrow(jpe, this.getContext().getString(R.string.downloademployeename), this.getContext().getString(R.string.exception_webservicecommerror), this.getContext().getString(R.string.exception_serviceunavailable));
			}
			catch (IOException ioe)
			{
				this.HandleExceptionAndThrow(ioe, this.getContext().getString(R.string.downloademployeename), this.getContext().getString(R.string.exception_webservicecommerror), this.getContext().getString(R.string.exception_serviceunavailable));
			}
		}
		else
		{
			KmbApplicationException exp = new KmbApplicationException(this.getContext().getString(R.string.msg_nodataconnection)); 
			exp.setCaption(this.getContext().getString(R.string.msg_nodataconnection));
			throw exp;
		}

		return name;
	}
	
	@SuppressWarnings("unused")
	public LoginCredentials GetAuthenticationInformation(String userName) throws KmbApplicationException
	{ 
		LoginCredentials loginCred = null; 
		try
		{
			LoginController loginController = new LoginController(this.getContext());
			return loginController.GetAuthenticationInformation(userName); 
		}
		catch (JsonSyntaxException jse)
		{
			this.HandleExceptionAndThrow(jse, this.getContext().getString(R.string.downloademployeename), this.getContext().getString(R.string.exception_webservicecommerror), this.getContext().getString(R.string.exception_serviceunavailable));
		}
		catch (JsonParseException jpe)
		{
			// when connected to a network, but unable to get to webservice "e" is null
			if (jpe == null)
				jpe = new JsonParseException(JsonParseException.class.getName());
			this.HandleExceptionAndThrow(jpe, this.getContext().getString(R.string.downloademployeename), this.getContext().getString(R.string.exception_webservicecommerror), this.getContext().getString(R.string.exception_serviceunavailable));
		}
		
		return loginCred; 
		
	}
    
    /// <summary>
    /// Transfer all active team drivers from the previous days log to this log.
    /// In this case, an active team driver is one that has not been ended.
    /// </summary>
    /// <param name="empLog"></param>
    public void TransferTeamDrivers(User usr, EmployeeLog empLog)
    {
        // fetch the log for yesterday
        Date yesterday = DateUtility.AddDays(empLog.getLogDate(), -1);
        IAPIController logController = MandateObjectFactory.getInstance(this.getContext(),GlobalState.getInstance().getFeatureService()).getCurrentEventController();
        EmployeeLog yesterdaysLog = logController.GetLocalEmployeeLog(usr, yesterday);

        if (yesterdaysLog != null)
        {
            // there is a log from yesterday to look at
            if (yesterdaysLog.getTeamDriverList().getTeamDriverList() != null)
            {
                // this will be the list of all team drivers 
                // that have not been ended yet
                List<TeamDriver> list = new ArrayList<TeamDriver>();

                // see if there are any team drivers that have not been ended
                for (TeamDriver driver : yesterdaysLog.getTeamDriverList().getTeamDriverList())
                {
                    if (driver.getIsActiveAtEndOfDay())
                    {
                        // this team driver is not ended, so add it to the list
                        TeamDriver newDriver = new TeamDriver();

                        // by default, this new driver will be setup to run for the entire log, at this point
                        newDriver.setEmployeeCode(driver.getEmployeeCode());
                        newDriver.setEmployeeId(driver.getEmployeeId());
                        newDriver.setDisplayName(driver.getDisplayName());
                        newDriver.setStartTime(EmployeeLogUtilities.CalculateLogStartTime(this.getContext(), empLog.getLogDate(), this.getCurrentUser().getHomeTerminalTimeZone()));
                        newDriver.setKMBUsername(driver.getKMBUsername());

                        list.add(newDriver);
                    }
                }

                // add the list to the log
                empLog.getTeamDriverList().setTeamDriverList(list.toArray(new TeamDriver[list.size()]));
            }
        }
    }
    
    /// <summary>
    /// Logout the user as a member of the team.
    /// </summary>
    public void LogoutTeamDriver(User user)
    {
        // calc the timestamp that it is right now
        Date endTimestamp = this.getCurrentClockHomeTerminalTime();

        // end this team driver across all logged in members of the team
        this.EndTeamDriver(user.getCredentials().getEmployeeCode(), endTimestamp);
    }
    
  /// <summary>
    /// Start a co-driver on the current log.  If the start time is not specified,
    /// then assume that the co-driver starts at the beginning of the log.
    /// If the team driver is already started at the requested time,
    /// then change the start time to match the new time.
    /// 
    /// NOTE: This is used when the team drivers will use separate devices.
    /// </summary>
    /// <param name="employeeCode">DMO employee code of the co-driver</param>
    /// <param name="displayName">name of the co-driver</param>
    /// <param name="startTime">timestamp that the team arrangment began. 
    /// If not specified, then start at the beginning of the log
    /// </param>
    public void StartTeamDriver(String employeeCode, String displayName, Date teamDriverStartTime, String userName)
    {
        // fetch the current log being processed
        LogEntryController entryController = new LogEntryController(getContext());
        EmployeeLog empLog = entryController.getCurrentEmployeeLog();

        // start the team driver
        this.StartTeamDriver(empLog, employeeCode, displayName, teamDriverStartTime, userName);

        // save the log
        IAPIController empLogController = MandateObjectFactory.getInstance(this.getContext(),GlobalState.getInstance().getFeatureService()).getCurrentEventController();
        empLogController.SaveLocalEmployeeLog(empLog);
    }
    
  /// <summary>
    /// Start a co-driver on the log.  If the start time is not specified,
    /// then assume that the co-driver starts at the beginning of the log.
    /// If the team driver is already started at the requested time,
    /// then change the start time to match the new time.
    /// </summary>
    /// <param name="empLog">employee log to add the team driver to</param>
    /// <param name="employeeCode">DMO employee code of the co-driver</param>
    /// <param name="displayName">name of the co-driver</param>
    /// <param name="startTime">timestamp that the team arrangment began. 
    /// If not specified, then start at the beginning of the log
    /// </param>
    private void StartTeamDriver(EmployeeLog log, String employeeCode, String displayName, Date startTime, String userName) {
        StartTeamDriver(log, employeeCode, displayName, startTime, userName, TimeZone.getDefault());
    }

    private void StartTeamDriver(EmployeeLog empLog, String employeeCode, String displayName, Date teamDriverStartTime, String userName, TimeZone timeZone)
    {
        // determine the fully quailified starting time, including date
        if (teamDriverStartTime == null)
        {
            teamDriverStartTime = EmployeeLogUtilities.CalculateLogStartTime(getContext(), empLog.getLogDate(), this.getCurrentUser().getHomeTerminalTimeZone());
        }
        else
        {
            // remove the seconds field
            teamDriverStartTime = DateUtility.ConvertToPreviousLogEventTime(teamDriverStartTime);
            teamDriverStartTime = DateUtility.AddTime(DateUtility.GetDateFromDateTime(empLog.getLogDate()), teamDriverStartTime);
        }

        // locate the team driving record for this employee, if it exists
        TeamDriver teamDriver = null;
        if (empLog.getTeamDriverList().getTeamDriverList() != null)
        {
            // there are team drivers already on the log
            // see if this employee is already an active team driver
            for (TeamDriver driver : empLog.getTeamDriverList().getTeamDriverList())
            {
                if (driver.getEmployeeCode().compareTo(employeeCode) == 0)
                {
                    // found this team driver already defined
                    @SuppressWarnings("unused")
					Date start = DateUtility.AddTime(empLog.getLogDate(), driver.getStartTime());
                    Date end;
                    if(driver.getEndTime() != null)
                    	end = DateUtility.AddTime(empLog.getLogDate(), driver.getEndTime());
                    else
                    	end = empLog.getLogDate();

                    if (end.compareTo(empLog.getLogDate()) == 0)
                    {
                        // adjust end time so that it's the log ending time
                        end = DateUtility.AddDays(end, 1);
                    }

                    if (teamDriverStartTime.compareTo(end) <= 0)
                    {
                        // the requested time is chronologically before the end
                        // of another period
                        // this is the Team Driving record to edit
                        teamDriver = driver;
                        break;
                    }
                }
            }
        }

        // does a team driving period already exist for this employee?
        if (teamDriver == null)
        {
            // create a new team driver, and add it to the log
            teamDriver = new TeamDriver();
            teamDriver.setEmployeeCode(employeeCode);
            teamDriver.setDisplayName(displayName);
            teamDriver.setKMBUsername(userName);
            // add the team driver to the log
            empLog.getTeamDriverList().Add(teamDriver);
        }

        // set the start time on the team driver
        teamDriver.setStartTime(teamDriverStartTime);
        teamDriver.setTimeZone(timeZone);
    }
    
    /// <summary>
    /// End the co-driver at the specified time.  
    /// All members of the team that are currently logged in will end
    /// the driver at the same time.
    /// For the log of the driver being ended, all team drivers on that log
    /// will be ended.
    /// If the time is not specified, then end at the completion of the log
    /// </summary>
    public void EndTeamDriver(String employeeCode, Date teamDriverEndTime) {
        EndTeamDriver(employeeCode, teamDriverEndTime, GlobalState.getInstance().getCurrentUser());
    }

    public void EndTeamDriver(String employeeCode, Date teamDriverEndTime, User user) 
    {
        Date currentHomeTerminalTime = DateUtility.CurrentHomeTerminalTime(user);
        Date logDate = EmployeeLogUtilities.CalculateLogStartTime(getContext(), currentHomeTerminalTime, user.getHomeTerminalTimeZone());
        IAPIController empLogController = MandateObjectFactory.getInstance(this.getContext(),GlobalState.getInstance().getFeatureService()).getCurrentEventController();

        // calc the fully qualified ending time, including date
        Date endingTime = teamDriverEndTime;
        if (endingTime == null)
        {
            // calc the end time of the log (24 hours after the start of the log...doh!)
            endingTime = DateUtility.AddDays(logDate, 1);
        }
        else
        {
            // AOBRD - remove the seconds field, and add to the log date
            if(!GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled())
                endingTime = DateUtility.AddSeconds(endingTime, endingTime.getSeconds() * -1);

            endingTime = DateUtility.AddTime(logDate, endingTime);
        }

        // look at each of the logged in team drivers
        for (User usr : this.getLoggedInUserList())
        {
            // get the current log for this user
            EmployeeLog empLog = empLogController.GetLocalEmployeeLogOrCreateTransition(usr, currentHomeTerminalTime);

            // an active log has been found
            // determine is this user is the team driver to be ended
            if (usr.getCredentials().getEmployeeCode().compareTo(employeeCode) == 0)
            {
                // this is the driver being ended
                // need to end all team drivers currently on this log
                this.EndTeamDriver(empLog, null, endingTime);
            }
            else
            {
                // this user is not the team driver being ended, so 
                // end the team driver as specified
                this.EndTeamDriver(empLog, employeeCode, endingTime);
            }

            // save the log for this user
            empLogController.SaveLocalEmployeeLog(usr, empLog);

            if(usr.getCredentials().getEmployeeCode().compareTo(this.getCurrentUser().getCredentials().getEmployeeCode()) == 0) 
            {
                // this log is for the current user, update this log in state
            	GlobalState.getInstance().setCurrentEmployeeLog(empLog);
            }
            if (usr.getCredentials().getEmployeeCode().compareTo(this.getCurrentDesignatedDriver().getCredentials().getEmployeeCode()) == 0)
            {
                // this log is for the current driver, update this log in state
            	GlobalState.getInstance().setCurrentDriversLog(empLog);
            }
        }
    }
    
    /// <summary>
    /// End the specific team driver on the log, at the specified end time.
    /// If the employeeCode is null, then all team drivers will be ended.
    /// </summary>
    private void EndTeamDriver(EmployeeLog empLog, String employeeCode, Date teamDriverEndTime)
    {
        // locate the driver that needs to be ended
        boolean isFound = false;
        if (empLog.getTeamDriverList().getTeamDriverList() != null)
        {
            // look at each team driver on the log
            for (TeamDriver driver : empLog.getTeamDriverList().getTeamDriverList())
            {
            	// 12/29/11 JHM - Only update an open team driver record (without an end time)
            	if(driver.getEndTime() == null)
            	{
	                // NOTE: when the inbound employeeCode is null...then all team drivers should be ended
	                if (employeeCode == null)
	                {
	                    isFound = true;
	                    driver.setEndTime(teamDriverEndTime);
	                }
	                else
	                {
	                    // is this the team driver that matches the code?
	                    if (driver.getEmployeeCode().compareTo(employeeCode) == 0)
	                    {
	                        // found the driver that needs to be ended
	                        isFound = true;
	                        driver.setEndTime(teamDriverEndTime);
	                        break;
	                    }
	                }
            	}
            }
        }

        if (!isFound)
        {
        	// TODO Handle ApplicationException
            //throw new ApplicationException(string.Format("Team driver not found with code '{0}'", employeeCode));
        }
    }
    
    public void EndTeamDrivingOnTransition(User usr, EmployeeLog empLog)
    {
        IAPIController empLogController = MandateObjectFactory.getInstance(this.getContext(),GlobalState.getInstance().getFeatureService()).getCurrentEventController();

        // set end time to midnight
    	Date endTime = DateUtility.AddDays(empLog.getLogDate(), 1);
    	
    	// end all open team driving periods associated with current log
    	this.EndTeamDriver(empLog, null, endTime);
    	empLogController.SaveLocalEmployeeLog(usr, empLog);
    	
    	// get logs for team drivers listed on specified employee log
    	// and end the team driving periods associated with those logs
    	if (empLog.getTeamDriverList().getTeamDriverList() != null)
    	{
    		for (TeamDriver teamDriver : empLog.getTeamDriverList().getTeamDriverList())
    		{
    			// get the user for this team driver
    			User teamDriverUser = null;
    	        for (User teamUser : this.getLoggedInUserList())
    	        {
    	        	if (teamDriver.getEmployeeCode().compareTo(teamUser.getCredentials().getEmployeeCode()) == 0)
    	        	{
    	        		teamDriverUser = teamUser;
    	        		break;
    	        	}
    	        }
    	        
    	        // if user is found for team driver, get log for that user
    	        // and end team driving periods
    	        if (teamDriverUser != null)
    	        {
    	            EmployeeLog teamEmpLog = empLogController.GetLocalEmployeeLog(teamDriverUser, empLog.getLogDate());
    	            if (teamEmpLog != null)
    	            {
    	            	this.EndTeamDriver(teamEmpLog, null, endTime);
    	            	empLogController.SaveLocalEmployeeLog(teamDriverUser, teamEmpLog);
    	            }
    	        }
    		}
    	}
    }
    
  /// <summary>
    /// Perform validation on the start of the team driver.
    /// Verify that this team driver is not already active at the specified time.
    /// </summary>
    /// <param name="empCode">employee code to add as a team driver</param>
    /// <param name="displayName">display name to show on reports</param>
    /// <param name="teamDriverStartTime">requested start time</param>
    /// <returns></returns>
    public String ValidateStart(String empCode, String displayName, Date teamDriverStartTime)
    {
        String msg = null;

        // fetch the current log being processed
        LogEntryController entryController = new LogEntryController(getContext());
        EmployeeLog empLog = entryController.getCurrentEmployeeLog();

        // calc the fully qualified start time of the team, including date
        if(teamDriverStartTime != null)
        	teamDriverStartTime = DateUtility.AddTime(empLog.getLogDate(), teamDriverStartTime);
        else
            teamDriverStartTime = EmployeeLogUtilities.CalculateLogStartTime(getContext(), empLog.getLogDate(), this.getCurrentUser().getHomeTerminalTimeZone());

        if (empLog.getTeamDriverList().getTeamDriverList() != null)
        {
            // look at each member of the existing team
            for (TeamDriver driver : empLog.getTeamDriverList().getTeamDriverList())
            {
                if (driver.getEmployeeCode().compareTo(empCode) == 0)
                {
                    // found this team driver already defined
                    Date end;
                    if(driver.getEndTime() != null)
                    	end = DateUtility.AddTime(empLog.getLogDate(), driver.getEndTime());
                    else
                    	end = empLog.getLogDate();

                    if (end.compareTo(empLog.getLogDate()) == 0)
                    {
                        // adjust end time so that it's the log ending time
                        end = DateUtility.AddDays(end, 1);
                    }

                    if (teamDriverStartTime.compareTo(end) <= 0 )
                    {
                        // the requested time is chronologically before the end
                        // of another period
                        Date start = DateUtility.AddTime(empLog.getLogDate(), driver.getStartTime());
                        msg = String.format("This team driver is already started at time %s.", DateUtility.getHomeTerminalTime12HourFormat().format(start));
                        break;
                    }
                }
            }
        }

        return msg;
    }
    
  /// <summary>
    /// Perform validation on ending a team driver.
    /// Verify that the end time is chronologically after the start time
    /// for the team driving period for this employee.
    /// </summary>
    /// <param name="empCode"></param>
    /// <param name="teamDriverEndTime"></param>
    /// <returns></returns>
    public String ValidateEnd(String empCode, Date teamDriverEndTime)
    {
        String msg = null;

        // fetch the current log being processed
        LogEntryController entryController = new LogEntryController(getContext());
        EmployeeLog empLog = entryController.getCurrentEmployeeLog();

        // calc the fully qualified end time, including date
        if (teamDriverEndTime == null) 
        {
            teamDriverEndTime = DateUtility.AddDays(empLog.getLogDate(), 1);
        }
        else 
        {
            teamDriverEndTime = DateUtility.AddTime(empLog.getLogDate(), teamDriverEndTime);
        }

        if (empLog.getTeamDriverList().getTeamDriverList() != null)
        {
            // look at each member of the existing team
            for (TeamDriver driver : empLog.getTeamDriverList().getTeamDriverList())
            {
                if (driver.getEmployeeCode().compareTo(empCode) == 0 && driver.getIsActiveAtEndOfDay())
                {
                    // found this team driver already defined, and it's not ended yet
                    if (teamDriverEndTime.compareTo(driver.getStartTime()) <= 0)
                    {
                        // the requested time is chronologically before the start of the period
                        msg = String.format("The end time is before the start time of %s, and must be changed.", DateUtility.getHomeTerminalTime12HourFormat().format(driver.getStartTime()));
                        break;
                    }
                }
            }
        }

        return msg;
    }

    public boolean SendAllSavedReassignmentRequests() {
        boolean isSuccessful = true;
        boolean isProcessed = false;

        EventReassignmentFacade facade = new EventReassignmentFacade(this.getContext(), this.getCurrentUser());
        List<DrivingEventReassignmentMapping> unsubmittedReassignments = facade.FetchAllUnsubmitted();

        try {
            // Process all unsubmitted reassignments
            for (DrivingEventReassignmentMapping reassignment : unsubmittedReassignments) {
                isProcessed = false;

                RESTWebServiceHelper rwsh = new RESTWebServiceHelper(getContext());
                EmployeeLogEldEventFacade eldEventFacade = new EmployeeLogEldEventFacade(this.getContext(),this.getCurrentUser());

                EmployeeLogEldEvent eldEvent = eldEventFacade.FetchByKey(reassignment.getRelatedEvent());
                if (eldEvent != null) {
                    reassignment.setEldEvent(eldEvent);

                    // Send request to DMO
                    rwsh.SubmitDrivingEventReassignmentRequest(reassignment);

                    // Update IsSubmitted flag on table
                    reassignment.setIsSubmitted(true);
                    facade.Save(reassignment);
                }
            }

        } catch (Exception ex) {
            // log the exception to the error log and then return false - UI
            // handles displaying error based on return value
            this.HandleException(ex, this.getContext().getString(R.string.submitlocalreassignmentstodmo));
            isSuccessful = false;
        }

        return isSuccessful;
    }

    public boolean SendReassignmentRequest(DrivingEventReassignmentMapping reassignment) {
        EventReassignmentFacade facade = new EventReassignmentFacade(this.getContext(),this.getCurrentUser());
        boolean savedToEncompass = true;

        if (this.getIsWebServicesAvailable()) {
            try {
                // Send request to DMO
                RESTWebServiceHelper rwsh = new RESTWebServiceHelper(getContext());
                rwsh.SubmitDrivingEventReassignmentRequest(reassignment);

            } catch (Exception ex) {
                // We were unable to send the Reassignment request, so save it in the table
                // to be processed after logs are submitted.
                facade.Save(reassignment);
                savedToEncompass = false;
            }
        } else {
            // When there is no internet connection at the time of reassignment, the process will require
            // populating the DrivingEventReassignmentMapping table and handling it when logs are submitted.
            facade.Save(reassignment);
            savedToEncompass = false;
        }

        return savedToEncompass;
    }

    /// <summary>
    /// Answer the list of team drivers that are available to be ended.
    /// </summary>
    /// <returns></returns>
    public ArrayList<TeamDriver> TeamDriversAvailableToBeEnded()
    {
        LogEntryController entryController = new LogEntryController(getContext());
        EmployeeLog empLog = entryController.getCurrentEmployeeLog();

        // build a list of all active team drivers that are not logged in
        ArrayList<TeamDriver> list = new ArrayList<TeamDriver>();
        if (empLog.getTeamDriverList().getTeamDriverList() != null)
        {
            for (TeamDriver driver : empLog.getTeamDriverList().getTeamDriverList())
            {
                if (driver.getIsActiveAtEndOfDay())
                {
                    // found an active driver 
                    // is this driver logged in as a team driver?
                    if (this.getLoggedInUserList().size() > 1)
                    {
                        boolean isFound = false;
                        for (User usr : this.getLoggedInUserList())
                        {
                            if (usr.getCredentials().getEmployeeCode().compareTo(driver.getEmployeeCode()) == 0)
                            {
                                // this team driver is already logged in, so skip this one
                                isFound = true;
                                break;
                            }
                        }
                        if (!isFound)
                        {
                            list.add(driver);
                        }
                    }
                    else
                    {
                        // no other team drivers are logged in at all
                        list.add(driver);
                    }
                }
            }
        }

        return list;
    }

    public EmployeeLog LoginMultipleUserDriver(User newTeamDriver, Date startTime) throws KmbApplicationException
    {
        if(GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled())
            throw new KmbApplicationException("Attempt to assume duty status.");

        return LoginTeamDriver(newTeamDriver, startTime, new DutyStatusEnum(DutyStatusEnum.ONDUTY), new ExemptLogTypeEnum(ExemptLogTypeEnum.NULL), new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
    }

    /* <summary> Cody Berndt - 6/19/2015:
     * 	This method is never going to be called while the new team driver workflow is active.
     * 	Once the team driver workflow is turned on on Prod, this code should be cleaned up.
     * </summary>
     */
    public EmployeeLog LoginTeamDriver(User newTeamDriver, Date startTime) throws KmbApplicationException
    {
        if(GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled())
            throw new KmbApplicationException("Attempt to assume duty status.");

    	return LoginTeamDriver(newTeamDriver, startTime, new DutyStatusEnum(DutyStatusEnum.ONDUTY), new ExemptLogTypeEnum(ExemptLogTypeEnum.NULL), new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
    }
    /// <summary>
    /// Start the user as a team driver.
    /// This occurs during the "Login" of a new team driver.
    /// The timestamp is the time that the team should be started.
    /// The new member of the team is added to exsiting team of everyone
    /// currently logged in.    
    /// Everyone currently logged in is added as to the team of the new driver.   
    /// All of the start times for each of these team entries should 
    /// be set to the specified start time.
    /// </summary>
    /// <param name="newUser">new team driver that has just logged in</param>
    /// <param name="startTimestamp">time when the team starts</param>
    /// <param name="dutyStatus">the duty status for the login event</param>
    public EmployeeLog LoginTeamDriver(User newTeamDriver, Date startTime, DutyStatusEnum dutyStatus, ExemptLogTypeEnum exemptLogType, DutyStatusEnum initalDutyStatus) throws KmbApplicationException
    {
        IAPIController empLogController = MandateObjectFactory.getInstance(this.getContext(),GlobalState.getInstance().getFeatureService()).getCurrentEventController();

        // fetch the new users current log, or create a new one if it doesn't exist already
        Date logDate = this.getCurrentClockHomeTerminalTime();
        EmployeeLog newTeamDriversLog = empLogController.GetLocalEmployeeLogOrCreateNew(newTeamDriver, logDate, initalDutyStatus);


        if(!empLogController.getAppRestartFlag()){
            empLogController.AddLoginEventToCurrentLog(newTeamDriversLog, dutyStatus);
        }
        // look at all the currently logged in users, trying to find the ones that are not the new guy
        
        for (User otherDriver : this.getLoggedInUserList())
        {
            if (newTeamDriver.getCredentials().getEmployeeId() != otherDriver.getCredentials().getEmployeeId())
            {
                // fetch the other drivers log
                EmployeeLog otherDriversLog = empLogController.GetLocalEmployeeLog(otherDriver, logDate);
                if (otherDriversLog == null)
                {
                    // the other's guys log is not there, try yesterday's log
                    otherDriversLog = empLogController.GetLocalEmployeeLog(otherDriver, DateUtility.AddDays(logDate, -1));
                }

                if (otherDriversLog == null)
                {
                    throw new KmbApplicationException(String.format("Could not find other driver '%s' log to add team driver.", otherDriver.getCredentials().getEmployeeFullName()));
                }

                // add the new team driver as a member of the other drivers team
                this.StartTeamDriver(otherDriversLog, newTeamDriver.getCredentials().getEmployeeCode(), newTeamDriver.getCredentials().getEmployeeFullName(), startTime, newTeamDriver.getCredentials().getUsername(), newTeamDriver.getHomeTerminalTimeZone().toTimeZone());
                empLogController.SaveLocalEmployeeLog(otherDriver, otherDriversLog);

                if (otherDriver.getCredentials().getEmployeeId() == this.getCurrentDesignatedDriver().getCredentials().getEmployeeId())
                {
                    // this log is for the current driver, update this log in state
                	GlobalState.getInstance().setCurrentDriversLog(otherDriversLog);
                }

                // add the other driver on the new drivers team
                this.StartTeamDriver(newTeamDriversLog, otherDriver.getCredentials().getEmployeeCode(), otherDriver.getCredentials().getEmployeeFullName(), startTime, otherDriver.getCredentials().getUsername(), otherDriver.getHomeTerminalTimeZone().toTimeZone());
            }
        }

        // save the changes to the new team drivers log
        newTeamDriversLog.setExemptLogType(exemptLogType);
        newTeamDriversLog.setIsCertified(false);

        // exempt logs require the driver to return to their work location
        if (exemptLogType.getValue() != ExemptLogTypeEnum.NULL)
            newTeamDriversLog.setHasReturnedToLocation(true);

        empLogController.SaveLocalEmployeeLog(newTeamDriver, newTeamDriversLog);

        GlobalState.getInstance().setCurrentEmployeeLog(newTeamDriversLog);

        return newTeamDriversLog;
    }
    
    /// <summary>
    /// Perform the team driver switch.   
    /// The current user of the app will be the user specified by activeUserId.
    /// The designated driver will be the user specified by driverUserId.
    /// </summary>
    /// <param name="activeUserId">employeeId of the active user of the app</param>
    /// <param name="driverUserId">employeeId of the designated driver</param>
    public void TeamDriverSwitch(String activeUserId, String driverUserId)
    {
    	
    	List<User> list = this.getLoggedInUserList();
	    if (list != null && list.size() > 0)
	    {
	    	User nextActiveUser = null;
	        User nextDesignatedDriver = null;
	        // look at each user to find both the active, and designated driver, users
	            
	        for (User usr : list)
	        {
	        	String empId = usr.getCredentials().getEmployeeId();
	
	            if (empId == activeUserId)
	            {
	            	nextActiveUser = usr;
	            }
	            if (empId == driverUserId)
	            {
	                nextDesignatedDriver = usr;
	            }
	        }
	
	        // set the new active user of the app
	        this.SwitchUserContext(nextActiveUser);
	
	        // is a new designated driver chosen?
	        if (nextDesignatedDriver != null)
	        {
	            // the designated driver is changing.
	            LogEntryController rodsController = new LogEntryController(this.getContext());
	            rodsController.SwitchDriverContext(nextDesignatedDriver);
	
	        }

	        // Run the DataTransferFileStatus service to check if a notification needs to be displayed for the new user
            // This will immediately run PollDataTransferFileStatus(), which will perform necessary checks and stop service if it's not needed
            Intent dataTransferFileStatusIntent = new Intent(getContext(), DataTransferFileStatusService.class);
            dataTransferFileStatusIntent.putExtra("startImmediately", true);
            getContext().startService(dataTransferFileStatusIntent);
	    }

    }
    
    public String canSwitchDriver(String newDesignatedDriverId)
    { 
    	String message = "";
    	
    	if(newDesignatedDriverId != null && this.getCurrentDesignatedDriver() != null && newDesignatedDriverId != this.getCurrentDesignatedDriver().getCredentials().getEmployeeId())
    	{
    		if(this.IsVehicleInMotion())
    			message = this.getContext().getString(R.string.msg_teamdriver_cannotbechanged_motion);

            if (this.IsCurrentDriverDutyStatusDriving())
                message = this.getContext().getString(R.string.msg_teamdriver_cannotbechanged_status);
    	}
    	
    	return message;
    }
    
    public boolean IsVehicleInMotion()
    {
		boolean motion = false;
		
		// 10/16/12 JHM - Vehicle motion check differs between Gen1 & Gen2
    	if(EobrReader.getInstance().isEobrGen1())
    		motion = this.getVehicleMotionDetector().getIsVehicleInMotion();
    	else
    	{
    		motion = (GlobalState.getInstance().getPotentialDrivingStopTimestamp() == null);
    	}
    	
    	return motion;
    }
    /// <summary>
    /// Determine whether both team drivers are On Duty.   
    /// If both team drivers are, then pass true.
    /// If both team drivers aren't, then pass false.
    /// </summary>
    public boolean IsTeamDriversOnDuty()
    {
    	boolean isBothOnDuty = false;
    	
    	if (GlobalState.getInstance().getLoggedInUserList().size() > 1)
    	{
	    	IAPIController emp_log = MandateObjectFactory.getInstance(this.getContext(),GlobalState.getInstance().getFeatureService()).getCurrentEventController();
			ArrayList<User> userList = GlobalState.getInstance().getLoggedInUserList();
			
			// User 1
			User user = userList.get(0);
			Date currentHomeTerminalTime = DateUtility.CurrentHomeTerminalTime(user);
			Date todaysLogDate = EmployeeLogUtilities.CalculateLogStartTime(this.getContext(), currentHomeTerminalTime, user.getHomeTerminalTimeZone());
			EmployeeLog empLog1 = emp_log.GetLocalEmployeeLog(user, todaysLogDate);

            EmployeeLogEldEvent logEvent1 = EmployeeLogUtilities.GetLastEventInLog(empLog1);
	    	
	    	// User 2
			user = userList.get(1);
			currentHomeTerminalTime = DateUtility.CurrentHomeTerminalTime(user);
			todaysLogDate = EmployeeLogUtilities.CalculateLogStartTime(this.getContext(), currentHomeTerminalTime, user.getHomeTerminalTimeZone());
			EmployeeLog empLog2 = emp_log.GetLocalEmployeeLog(user, todaysLogDate);
            EmployeeLogEldEvent logEvent2 = EmployeeLogUtilities.GetLastEventInLog(empLog2);
	    	
	    	if(logEvent1.getDutyStatusEnum().getValue() == DutyStatusEnum.ONDUTY && logEvent2.getDutyStatusEnum().getValue() == DutyStatusEnum.ONDUTY)
	    	{
	    		isBothOnDuty = true;
	    	}
    	}
    	return isBothOnDuty;
    }
    
    public boolean IsCurrentDriverDutyStatusDriving()
    {
    	boolean isDriving = false;

        LogEntryController entryController = new LogEntryController(getContext());
        EmployeeLog empLog = entryController.getCurrentDriversLog();

        EmployeeLogEldEvent logEvent = EmployeeLogUtilities.GetLastEventInLog(empLog);

		if(logEvent.getDutyStatusEnum().getValue() == DutyStatusEnum.DRIVING)
			isDriving = true; 
		
		return isDriving; 
    }
    
    public void SetTeamDriver()
    {
    	if (GlobalState.getInstance().getLoggedInUserList().size() > 1)
    	{
	    	IAPIController emp_log = MandateObjectFactory.getInstance(this.getContext(),GlobalState.getInstance().getFeatureService()).getCurrentEventController();
			ArrayList<User> userList = GlobalState.getInstance().getLoggedInUserList();
			
			// User 1
			User user1 = userList.get(0);
			Date currentHomeTerminalTime = DateUtility.CurrentHomeTerminalTime(user1);
			Date todaysLogDate = EmployeeLogUtilities.CalculateLogStartTime(this.getContext(), currentHomeTerminalTime, user1.getHomeTerminalTimeZone());
			EmployeeLog empLog1 = emp_log.GetLocalEmployeeLog(user1, todaysLogDate);

            EmployeeLogEldEvent logEvent1 = EmployeeLogUtilities.GetLastEventInLog(empLog1);
	    	
	    	// User 2
			User user2 = userList.get(1);
			currentHomeTerminalTime = DateUtility.CurrentHomeTerminalTime(user2);
			todaysLogDate = EmployeeLogUtilities.CalculateLogStartTime(this.getContext(), currentHomeTerminalTime, user2.getHomeTerminalTimeZone());
			EmployeeLog empLog2 = emp_log.GetLocalEmployeeLog(user2, todaysLogDate);
            EmployeeLogEldEvent logEvent2 = EmployeeLogUtilities.GetLastEventInLog(empLog2);
	    	
	    	String designatedDriverId = user1.getCredentials().getEmployeeId();
	    	
	    	if((logEvent1.getDutyStatusEnum().getValue() == DutyStatusEnum.ONDUTY && logEvent2.getDutyStatusEnum().getValue() != DutyStatusEnum.ONDUTY) ||
	    			(logEvent1.getDutyStatusEnum().getValue() != DutyStatusEnum.ONDUTY && logEvent2.getDutyStatusEnum().getValue() != DutyStatusEnum.ONDUTY))
	    	{
	    		if(!this.getCurrentDesignatedDriver().getCredentials().getEmployeeId().equals(user1.getCredentials().getEmployeeId()) ||
	    				!this.getCurrentUser().getCredentials().getEmployeeId().equals(user1.getCredentials().getEmployeeId()))
	    			designatedDriverId = user1.getCredentials().getEmployeeId(); 
	    	}
	    	else if(logEvent1.getDutyStatusEnum().getValue() != DutyStatusEnum.ONDUTY && logEvent2.getDutyStatusEnum().getValue() == DutyStatusEnum.ONDUTY)
	    	{
	    		if(!this.getCurrentDesignatedDriver().getCredentials().getEmployeeId().equals(user2.getCredentials().getEmployeeId()) ||
	    				!this.getCurrentUser().getCredentials().getEmployeeId().equals(user2.getCredentials().getEmployeeId()))
	    			designatedDriverId = user2.getCredentials().getEmployeeId(); 
	    	}
	    	
	    	this.TeamDriverSwitch(designatedDriverId,designatedDriverId);
	    	
    	}
    	
    }
    
    public void SetCurrentUserToDesignatedDriver()
    {
    	String designatedDriverId = GlobalState.getInstance().getCurrentDesignatedDriver().getCredentials().getEmployeeId();
    	TeamDriverSwitch(designatedDriverId, designatedDriverId);
    }
}
