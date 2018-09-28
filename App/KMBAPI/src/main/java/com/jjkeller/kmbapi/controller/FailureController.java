package com.jjkeller.kmbapi.controller;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.jjkeller.kmbapi.R;
import com.jjkeller.kmbapi.common.TimeKeeper;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.EOBR.EobrReader;
import com.jjkeller.kmbapi.controller.EOBR.EobrReader.ConnectionState;
import com.jjkeller.kmbapi.controller.interfaces.IAPIController;
import com.jjkeller.kmbapi.controller.share.ControllerBase;
import com.jjkeller.kmbapi.controller.share.MandateObjectFactory;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.controller.utility.EmployeeLogUtilities;
import com.jjkeller.kmbapi.controller.utility.ErrorLogHelper;
import com.jjkeller.kmbapi.controller.utility.NotificationUtilities;
import com.jjkeller.kmbapi.controller.utility.RESTWebServiceHelper;
import com.jjkeller.kmbapi.enums.FailureCategoryEnum;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.proxydata.FailureReport;
import com.jjkeller.kmbapi.proxydata.FailureReportList;
import com.jjkeller.kmbapi.realtime.MalfunctionManager;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class FailureController extends ControllerBase{
	
	public FailureController(Context ctx) {
		super(ctx);
	}
	
    /// <summary>
    /// Amount of time that the two clocks (DMO time, and eobr) are allowed
    /// to be out of sync before it's considered a failure.  This value
    /// acts as a threshhold.   When the two clocks are different by
    /// a value less than this threshhold, then everything is OK.  
    /// However, if the two clocks are ever off by more than this amount,
    /// generate a failure report.
    /// </summary>
    private static long _clockSyncFailureThreshholdMillis = 30 * 60 * 1000;	// 30 minutes, as a millisecond value


    //This date is used to check GPS clock validity. Starting April 2019 ELDs with older Trimble GPS modules (All Gen 2.0s and Gen 2.5s with serial numbers < 51078-0736680)
    // will report invalid GPS time unless the ELD has firmware version > or = v6.88.121. See PBI 62036 The invalid GPS time cannot be used to set the RTC
    private Date dateToCheckGPSValidity = DateUtility.getDateTimeFromString("12/31/2015 23:59:00", new SimpleDateFormat("MM/dd/yyyy HH:mm:ss"), TimeZone.getTimeZone("UTC"));


    /// Amount of time that the two clocks (DMO time, and eobr) are allowed
    /// to be out of sync.  This value acts as a threshhold.   
    /// When the two clocks are different by a value less than this threshhold, 
    /// then everything is OK.  
    /// However, if the two clocks are ever off by more than this amount,
    /// attempt to set the eobr clock to DMO time
    /// </summary>
    private static long _clockSyncThreshholdMillis = 30 * 1000;	// 30 seconds, as a millisecond value
    
    /// <summary>
    /// Answer if the mobile clock has been synchronized with either 
    /// DMO or the EOBR device.  This is used to identify if the mobile
    /// clock can be trusted.
    /// </summary>
    public boolean getIsMobileClockSynchronized()
    {
    	return GlobalState.getInstance().getIsMobileClockSynchronized();
    }
    public void setIsMobileClockSynchronized(boolean isMobileClockSynchronized)
    {
    	GlobalState.getInstance().setIsMobileClockSynchronized(isMobileClockSynchronized);
    }

    /// <summary>
    /// Answer the delayed failure.   Delayed failures are
    /// created when a failure needs to be stopped, but the current
    /// day's log has not be created yet.
    /// </summary>
	public List<FailureReport> getRecentFailureReportList()
	{
        // try to read from state
        List<FailureReport> answer = (List<FailureReport>)(GlobalState.getInstance().getRecentFailureReportList()); 
        if (answer == null)
        {
            answer = new ArrayList<FailureReport>();
        }
        return answer;
	}
	public void setRecentFailureReportList(List<FailureReport> list)
	{
		GlobalState.getInstance().setRecentFailureReportList(list);
	}

    /// <summary>
    /// Answer the delayed failure.   Delayed failures are
    /// created when a failure needs to be stopped, but the current
    /// day's log has not be created yet.
    /// </summary>
	private FailureReport getDelayedFailureReport()
	{
		return GlobalState.getInstance().getDelayedFailureReport();
	}
	private void setDelayedFailureReport(FailureReport failureReport)
	{
		GlobalState.getInstance().setDelayedFailureReport(failureReport);
	}
	
    /// <summary>
    /// Stop any delayed failures that may be present.
    /// A 'delayed' failure is one that was stopped before the
    /// current employee log was created.   For example, at system startup
    /// the Time Sync failure will be stopped when the EOBR is connected.
    /// </summary>
    public void ApplyDelayedFailures(EmployeeLog empLog)
    {
        FailureReport report = getDelayedFailureReport();
        if (report != null)
        {
            // a delayed failure was detected, need to stop the failure now
            DateTime failureTimestampUtc = report.getStopTime();
            FailureCategoryEnum category = report.getCategory();
            EmployeeLogUtilities.StopPendingFailure(empLog, category, failureTimestampUtc);
        }

    }
    
    /// <summary>
    /// Transfer all outstanding, not stopped, failures from the old
    /// log to the new log
    /// </summary>
    /// <param name="oldLog"></param>
    /// <param name="newLog"></param>
    public void TransferFailures(EmployeeLog oldLog, EmployeeLog newLog)
    {
        // new failures will all start at the beginning of the log
        Date newLogStartTime = EmployeeLogUtilities.CalculateLogStartTime(this.getContext(), newLog.getLogDate(), this.getCurrentUser().getHomeTerminalTimeZone());
        Date previousLogEndTime = EmployeeLogUtilities.CalculateLogStartTime(this.getContext(), DateUtility.AddDays(oldLog.getLogDate(), 1), this.getCurrentUser().getHomeTerminalTimeZone());

        // look at the time sync failures and transfer those
        if (oldLog.getTimeSyncFailureList() != null && oldLog.getTimeSyncFailureList().getFailureReportList() != null)
        {
            List<FailureReport> newList = new ArrayList<FailureReport>();

            for (FailureReport report : oldLog.getTimeSyncFailureList().getFailureReportList())
            {
                if (report.getStopTime() == null)
                {
                    // found an open report
                    FailureReport newReport = new FailureReport();
                    newReport.setCategory(report.getCategory());
                    newReport.setMessage(report.getMessage());
                    newReport.setStartTime(new DateTime(newLogStartTime));
                    newList.add(newReport);

                    // set the end time as the ending log time
                    report.setStopTime(new DateTime(previousLogEndTime));
                }
            }
            // put the new reports in the new log
            newLog.getTimeSyncFailureList().setFailureReportList(newList.toArray(new FailureReport[newList.size()]));
        }

        // look at the EOBR failures and transfer those
        if (oldLog.getEobrFailureList() != null && oldLog.getEobrFailureList().getFailureReportList() != null)
        {
            List<FailureReport> newList = new ArrayList<FailureReport>();
            for (FailureReport report : oldLog.getEobrFailureList().getFailureReportList())
            {
                if (report.getStopTime() == null)
                {
                    // found an open report
                    FailureReport newReport = new FailureReport();
                    newReport.setCategory(report.getCategory());
                    newReport.setMessage(report.getMessage());
                    newReport.setStartTime(new DateTime(newLogStartTime));
                    newList.add(newReport);

                    // set the end time as the ending log time
                    report.setStopTime(new DateTime(previousLogEndTime));
                }
            }
            // put the new reports in the new log
            newLog.getEobrFailureList().setFailureReportList(newList.toArray(new FailureReport[newList.size()]));
        }
    }

    /// <summary>
    /// Answer if there are any failures that require confirmation from the user.
    /// Only the newly started failures should require confirmation.
    /// </summary>
    /// <returns></returns>
    public boolean AnyFailuresRequireConfirmation()
    {
        boolean answer = this.getRecentFailureReportList().size() > 0;
        return answer;
    }

    /// <summary>
    /// Answer if there are any open, or not stopped, failures on the log.
    /// </summary>
    /// <returns></returns>
	public boolean AnyFailuresToReport(EmployeeLog empLog)
	{
        boolean answer = false;

        if (this.getRecentFailureReportList().size() > 0)
        {
            answer = true;
        }
        else
        {
            // are there any failures already stored on the log?
            answer = this.AnyFailuresToReport(empLog.getTimeSyncFailureList());
            if (!answer)
            {
                // no time sync failures exist, do any EOBR failures exist
                answer = this.AnyFailuresToReport(empLog.getEobrFailureList());
            }
        }

        return answer;
	}

    /// <summary>
    /// Answer if there are any open failures in the list.
    /// An 'open' failure is defined as one that has not been stopped yet.
    /// </summary>
    /// <param name="list"></param>
    /// <returns></returns>
	private boolean AnyFailuresToReport(FailureReportList list)
	{
        boolean answer = false;
        if (list.getFailureReportList() != null && list.getFailureReportList().length > 0)
        {
            for (FailureReport report : list.getFailureReportList())
            {
                if (report.getStopTime() == null)
                {
                    answer = true;
                    break;
                }
            }
        }
        return answer;
	}

    /// <summary>
    /// Answer a message for the most recent failure on the log.
    /// Only the most recent failure for the log will generate a message.
    /// If there are no failures, then return null
    /// </summary>
    /// <param name="empLog"></param>
    /// <returns></returns>
	public String MessageForMostRecentFailure(EmployeeLog empLog)
	{
        String msg = null;
        FailureReport report = this.RecentFailureReport(empLog);
        if (report != null)
        {
            StringBuilder sb = new StringBuilder();
            sb.append(report.getMessage());
            sb.append(" ");
            sb.append(this.getContext().getString(R.string.msg_eobrfailure_paperlogsrequired));
            sb.append(" ");
            sb.append(DateUtility.getHomeTerminalTime12HourFormat().format(report.getStartTime().toDate()));
            if (report.getStopTime() != null)
            {
                sb.append(" ");
                sb.append(this.getContext().getString(R.string.msg_eobrfailure_until));
                sb.append(" ");
                sb.append(DateUtility.getHomeTerminalTime12HourFormat().format(report.getStopTime().toDate()));
            }
            sb.append(".");
            msg = sb.toString();
        }
        return msg;
	}

	private FailureReport RecentFailureReport(EmployeeLog empLog)
	{
        FailureReport report = null;

        if (this.getRecentFailureReportList().size() == 0)
        {
            // no recent failures to report, are any already on the log
            if (this.AnyFailuresToReport(empLog.getTimeSyncFailureList()))
            {
                report = this.MostRecentFailure(empLog.getTimeSyncFailureList());
            }
            else if (this.AnyFailuresToReport(empLog.getEobrFailureList()))
            {
                report = this.MostRecentFailure(empLog.getEobrFailureList());
            }
        }
        else
        {
            // there are recently added reports, 
            // so pull the first report, 
            // and then remove it from the list
            List<FailureReport> list = this.getRecentFailureReportList();
            report = list.get(0);
            list.remove(0);
            this.setRecentFailureReportList(list);
        }

        return report;
	}
	
    /// <summary>
    /// Answer the most recent failure in the list
    /// </summary>
    /// <param name="reportList"></param>
    /// <returns></returns>
    private FailureReport MostRecentFailure(FailureReportList reportList)
    {
        FailureReport answer = null;
        if (reportList.getFailureReportList() != null && reportList.getFailureReportList().length > 0)
        {
            answer = reportList.getFailureReportList()[reportList.getFailureReportList().length - 1];
        }
        return answer;
    }
    
    /// Determine if EOBR Clock synchronization is necessary, and answer the amount of synchronization required.
    /// The syncTimestampUtc is the current eobr clock timestamp
    /// The syncGpsTimestampUtc is the current eobr GPS clock timestamp
    /// Read the dmo clock and determine if the eobr clock needs synchronization, or if a failure condition exists.
    /// If clock synchronization should occur, then answer the amount that the eobr is different from dmo
    /// NOTE: a positive number difference means that the EOBR is behind DMO (eobr clock is running slow) and 
    ///       a negative number means that the EOBR is ahead of DMO (eobr clock is running fast)
    public Bundle DetermineIfEOBRClockSyncIsNecessary(Date syncTimestampUtc, Date syncGpsTimestampUtc){
    	Bundle answer = new Bundle();
        Date syncDmoTimestampUtc = this.GetDmoTime();
        Date deviceClockUtc = GetSystemTime();

        Date sourceOfTruthTime = syncDmoTimestampUtc == null ? ((syncGpsTimestampUtc == null ||  syncGpsTimestampUtc.before(dateToCheckGPSValidity))? deviceClockUtc : syncGpsTimestampUtc) : syncDmoTimestampUtc;

    	boolean requiresSynchronization = false;
    	boolean isEobrClockInFailure = true;
    	long clockDifference = 0;

        if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
            answer = MalfunctionManager.getInstance().validateELDMandateTimingCompliance(sourceOfTruthTime, syncTimestampUtc);
            isEobrClockInFailure = !answer.getBoolean("isCompliant", false);
            // this is handled in TimingMalfunction for mandate.
            clockDifference = answer.getLong("clockDifference", 0);
            requiresSynchronization = answer.getBoolean("requiresSynchronization", false);
        } else {
            String clockNotSetCorrectlyMessage = getContext().getString(R.string.eobr_clock_not_set_correctly);
            // unable to read DMO or GPS or Device Time
            if (sourceOfTruthTime == null) {
                clockDifference = syncTimestampUtc.getTime() - DateUtility.getCurrentDateTimeUTC().getTime();
                if(Math.abs(clockDifference) > _clockSyncFailureThreshholdMillis){
                    this.ReportClockSyncFailure(syncTimestampUtc, clockNotSetCorrectlyMessage);
                    requiresSynchronization = false;
                }else{
                    isEobrClockInFailure = false;
                    requiresSynchronization = false;
                }
            } else {
                clockDifference = sourceOfTruthTime.getTime() - syncTimestampUtc.getTime();
                if(Math.abs(clockDifference) > _clockSyncFailureThreshholdMillis){
                    // create a failure because the two clocks are off by more than the failure threshold
                    this.ReportClockSyncFailure(syncTimestampUtc, clockNotSetCorrectlyMessage);
                    requiresSynchronization = true;
                } else {
                    // the clocks are within tolerance right now, so remove any time sync failures
                    this.StopPendingClockSyncFailure(syncTimestampUtc);
                    isEobrClockInFailure = false;
                }
            }
        }

    	String msg = String.format("FailureController.DetermineIfEOBRClockSyncIsNecessary syncDmoTimestampUtc: {%s} eobrTime: {%s} diff: {%s} sync required: {%s}", syncDmoTimestampUtc, syncTimestampUtc, clockDifference, requiresSynchronization );
    	Log.d("TimeSync", msg);
    	ErrorLogHelper.RecordMessage(this.getContext(), msg);
    	
        clockDifference = requiresSynchronization ? clockDifference : 0;

        if(GlobalState.getInstance().getFeatureService().getIgnoreServerTime()) {
            // ignoreServerTime
            answer.putBoolean("isInFailure", false);
            answer.putBoolean("requiresSynchronization", false);
            answer.putLong("clockDifference", 0);
        }else{
            answer.putBoolean("isInFailure", isEobrClockInFailure);
            answer.putBoolean("requiresSynchronization", requiresSynchronization);
            answer.putLong("clockDifference", clockDifference);
        }
    	return answer;
    }

    public Date GetDmoTime(){
		Date dmoTime = null;

		// ignoreServerTime
		if(GlobalState.getInstance().getFeatureService().getIgnoreServerTime()) {
            return TimeKeeper.getInstance().now();
		}
		
		try {
            // get utc time from DMO
			RESTWebServiceHelper rwsh = new RESTWebServiceHelper(getContext());
			dmoTime = rwsh.GetDMOTime();

			// SetSystemTime when DMO time is obtained from webservice.
			if (dmoTime != null) {
                DateUtility.SetSystemTime(dmoTime);
            }
       	} catch (Exception ioe) {
            ErrorLogHelper.RecordException(ioe);
			dmoTime = null;
		} 
		return dmoTime;
    }
    protected Date GetSystemTime(){
        return new Date(System.currentTimeMillis());
    }

	public boolean StopPendingEobrFailure(EmployeeLog empLog, Date endTimestampUtc) {
        boolean answer = false;
        if (EobrReader.getInstance().getCurrentConnectionState() != ConnectionState.DEVICEFAILURE)
        {
            answer = this.StopFailureOnCurrentLog(empLog, new FailureCategoryEnum(FailureCategoryEnum.EOBRDEVICE), endTimestampUtc);
        }
        return answer;
	}

    /// <summary>
    /// Stop any open failures for the category on the current 
    /// employee log.
    /// </summary>
    /// <param name="category"></param>
    /// <param name="stopTimestampUtc"></param>
    @SuppressLint("LongLogTag")
    private boolean StopFailureOnCurrentLog(EmployeeLog empLog, FailureCategoryEnum category, Date stopTimestampUtc)
    {
        boolean wasFailureStopped = false;

        // when no log is passed in, try to retrieve the current log
        if (empLog == null)
        {
            // must be working with the current log, fetch it
            LogEntryController ctrlr = new LogEntryController(getContext());
            if (ctrlr.IsCurrentLogCreated())
            {
                // only get the current log if it'a already been created
                empLog = ctrlr.getCurrentEmployeeLog();
            }
        }

        if (empLog != null)
        {
            // determine if an open, or not stopped, failure exists already for the category
            boolean openFailureExists = false;
            switch (category.getValue())
            {
                case FailureCategoryEnum.CLOCKSYNCHRONIZATION:
                    openFailureExists = this.AnyFailuresToReport(empLog.getTimeSyncFailureList());
                    break;
                case FailureCategoryEnum.EOBRDEVICE:
                    openFailureExists = this.AnyFailuresToReport(empLog.getEobrFailureList());
                    break;
            }
            
            // convert the timestamp of the failure to local time
            DateTime stopTimestampHomeTerminal;
            switch (category.getValue())
            {
                case FailureCategoryEnum.CLOCKSYNCHRONIZATION:
                    DateTimeZone tz = DateTimeZone.forTimeZone(this.getCurrentUser().getHomeTerminalTimeZone().toTimeZone());
                	stopTimestampHomeTerminal = new DateTime(stopTimestampUtc.getTime()).toDateTime(tz);
                    break;
            	default:
                	// Using value directly because ProcessEobrReaderEvent already did conversion to home terminal time.
                    stopTimestampHomeTerminal = new DateTime(stopTimestampUtc.getTime());
                    break;
            }

            boolean changesMadeToLog = false;
            if (openFailureExists)
            {
                // there are open failures that can be stopped
                changesMadeToLog = EmployeeLogUtilities.StopPendingFailure(empLog, category, stopTimestampHomeTerminal);
                wasFailureStopped = true;
            }

            if (category.getValue() == FailureCategoryEnum.CLOCKSYNCHRONIZATION)
            {
                // on a clock sync error, remove any events on the log that may be in the future
                if (EmployeeLogUtilities.RemoveAllEventsAfter(empLog, stopTimestampHomeTerminal.toDate()))
                    changesMadeToLog = true;
            }

            // persist the changes to the log 
            if (changesMadeToLog)
            {
                IAPIController cntrlr = MandateObjectFactory.getInstance(this.getContext(),GlobalState.getInstance().getFeatureService()).getCurrentEventController();
                cntrlr.SaveLocalEmployeeLog(empLog);
            }

        }
        else
        {
            // the current log is not created yet.
            // keep track of the failure info, so that once the log is created, that
            // the failure can be stopped correctlty.
            FailureReport report = this.CreateFailure(category, stopTimestampUtc, null);
            this.setDelayedFailureReport(report);
            wasFailureStopped = true;
        }

        if (wasFailureStopped)
        {
            Log.d("FailureController.StopFailureOnCurrentLog", String.format("FailureController stop failure: {%s} {%s}", category.getString(getContext()), DateUtility.getHomeTerminalDateTimeFormat12Hour().format(stopTimestampUtc)));

            // Cancel the EOBRFAILURE Notification
			NotificationUtilities.CancelNotification(this.getContext(), NotificationUtilities.EOBRFAILURE_ID);
        }

        return wasFailureStopped;
    }

    /// <summary>
    /// Report a failure on the EOBR device.  This failure may be 
    /// either hardware or a software failure.
    /// </summary>
    /// <param name="startTimestampUtc"></param>
    /// <param name="message"></param>
    public void ReportEobrFailure(EmployeeLog empLog, Date startTimestampUtc, String message)
    {
        FailureReport report = this.CreateFailure(new FailureCategoryEnum(FailureCategoryEnum.EOBRDEVICE), startTimestampUtc, message);
        this.StartFailureOnCurrentLog(empLog, report);
        
        if (this.AnyFailuresRequireConfirmation())
        {
			String alertMessage = this.MessageForMostRecentFailure(empLog);
			int flags = Notification.FLAG_AUTO_CANCEL;
			int defaults = Notification.DEFAULT_ALL;

			// Show the alert message
			NotificationUtilities.AddNotification(this.getContext(),
					GlobalState.getInstance()
							.getNotificationHoursAvailableClass(),
					NotificationUtilities.EOBRFAILURE_ID,
					R.drawable.stat_notify_error,
					this.getContext().getString(R.string.app_name_api),
					alertMessage, flags, defaults,
					R.layout.alert_notification_layout);
		}
    }

    /// <summary>
    /// Report a Clock Synchronization failure
    /// </summary>
    /// <param name="startTimestampUtc"></param>
    /// <param name="message"></param>
    private void ReportClockSyncFailure(Date startTimestampUtc, String message)  {
    	this.setIsMobileClockSynchronized(false);
        FailureReport report = this.CreateFailure(new FailureCategoryEnum(FailureCategoryEnum.CLOCKSYNCHRONIZATION), startTimestampUtc, message);
        this.StartFailureOnCurrentLog(null, report);
    }

    /// <summary>
    /// Stop any pending Clock Synchronization failures.  
    /// The only way to stop a clock sync failure is to synchronize
    /// with either DMO or the EOBR device.
    /// There may not be any outstanding clock sync failures to stop.
    /// Answer if a failure was stopped.
    /// </summary>
    /// <param name="endTimestampUtc"></param>
    /// <returns>true if a failure was stopped, false otherwise</returns>
    public boolean StopPendingClockSyncFailure(Date endTimestampUtc)
    {
        this.setIsMobileClockSynchronized(true);
        return this.StopFailureOnCurrentLog(null, new FailureCategoryEnum(FailureCategoryEnum.CLOCKSYNCHRONIZATION), endTimestampUtc);
    }
    
    /// <summary>
    /// Verify that the last saved logout time is earlier than the 
    /// current login time.   If not, report a Clock Sync failure.
    /// </summary>
    public void VerifyLoginTimeConsistency(Context ctx, User user)
    {
		// ignoreServerTime
		if(GlobalState.getInstance().getFeatureService().getIgnoreServerTime()){
			return;	
		}
    	
        // only verify the login times if the mobile clock has not been 
        // synchronized with either DMO or the EOBR device
        if (!this.getIsMobileClockSynchronized()){
            // is there a logout time to deal with?
            if (user.getCredentials().getLastLogoutTimestampUtc() != null) {
                Date loginTime = user.getCredentials().getLastLoginTimestampUtc();
                Date logoutTime = user.getCredentials().getLastLogoutTimestampUtc();
                if (loginTime.getTime() < logoutTime.getTime()) {
                    // login time was earlier than the last logout time
                    // this is a major error in clock synchronization
                    // see if there are any validated events on the 
                    // current days log
                    IAPIController ctrlr = MandateObjectFactory.getInstance(this.getContext(),GlobalState.getInstance().getFeatureService()).getCurrentEventController();
                    if (ctrlr.ValidatedEventsExistForTodaysLog()){
                        // valid events have already been placed on the log
                        // reset the clock to the logout time
                        DateUtility.SetSystemTime(logoutTime);

                        this.ReportClockSyncFailure(logoutTime, String.format("Setting clock to LOGOUT time %s.", logoutTime));
                    } else {
                        // either no log events for current day, or nothing is validated on the log yet
                    	// in this case, the time sync failure starts at the login time
                        this.ReportClockSyncFailure(loginTime, "Detected login earlier than logout.");
                    }
                }
            }
        }
    }

    /// <summary>
    /// Answer a newly created FailureReport for the category and the timestamp
    /// </summary>
    /// <param name="failureCategory">category of the failure</param>
    /// <param name="startTimestampUtc">timestamp that the failure started</param>
    /// <param name="message"></param>
    /// <returns>FailureReport</returns>
    private FailureReport CreateFailure(FailureCategoryEnum failureCategory, Date startTimestampUtc, String message)
    {
        FailureReport rpt = new FailureReport();
        rpt.setCategory(failureCategory);
        rpt.setMessage(message);
        rpt.setStartTime(new DateTime(startTimestampUtc));

        return rpt;
    }

    /// <summary>
    /// Start a new failure and save it to the current employee 
    /// log being worked with.
    /// A new failure will only be added to the log, it no other 
    /// simliar failures exist on the log already.
    /// </summary>
    /// <param name="report"></param>
    @SuppressLint("LongLogTag")
    private void StartFailureOnCurrentLog(EmployeeLog empLog, FailureReport report)
    {
        // when no log is passed in, try to retrieve the current log
        if (empLog == null)
        {
            // must be working with the current log, fetch it
            LogEntryController ctrlr = new LogEntryController(getContext());
            empLog = ctrlr.getCurrentEmployeeLog();
        }

        if (empLog != null)
        {
            // determine if an open, or not stopped, failure exists for the category
            boolean openFailureExists = false;
            switch (report.getCategory().getValue())
            {
                case FailureCategoryEnum.CLOCKSYNCHRONIZATION:
                    openFailureExists = this.AnyFailuresToReport(empLog.getTimeSyncFailureList());
                    break;
                case FailureCategoryEnum.EOBRDEVICE:
                    openFailureExists = this.AnyFailuresToReport(empLog.getEobrFailureList());
                    break;
            }

            if (!openFailureExists)
            {               
                // add the failure to the log because one does not alreay exist
                EmployeeLogUtilities.AddFailureReport(empLog, report);

                // persist the changes to the log 
                IAPIController logCtrlr = MandateObjectFactory.getInstance(this.getContext(),GlobalState.getInstance().getFeatureService()).getCurrentEventController();
                logCtrlr.SaveLocalEmployeeLog(empLog);

                // add the report to the list of recent failures 
                List<FailureReport> list = this.getRecentFailureReportList();
                list.add(report);
                this.setRecentFailureReportList(list);

                String msg = String.format("FailureController start failure: {%s} {%s} {%s}", report.getCategory().getString(getContext()), DateUtility.getHomeTerminalDateTimeFormat().format(report.getStartTime().toDate()), report.getMessage());
                Log.d("FailureController.StartFailureOnCurrentLog", msg);
                ErrorLogHelper.RecordMessage(this.getContext(), msg);

            }
        }
    }

}
