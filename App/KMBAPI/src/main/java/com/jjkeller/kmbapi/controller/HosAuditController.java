package com.jjkeller.kmbapi.controller;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.jjkeller.kmbapi.R;
import com.jjkeller.kmbapi.calcengine.Enums.CanadaDeferralTypeEnum;
import com.jjkeller.kmbapi.calcengine.HoursOfServiceSummary;
import com.jjkeller.kmbapi.calcengine.IHosRulesetCalcEngine;
import com.jjkeller.kmbapi.calcengine.LogProperties;
import com.jjkeller.kmbapi.calcengine.OffDuty;
import com.jjkeller.kmbapi.calcengine.RulesetFactory;
import com.jjkeller.kmbapi.common.TimeKeeper;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.interfaces.IAPIController;
import com.jjkeller.kmbapi.controller.interfaces.IHosAlert;
import com.jjkeller.kmbapi.controller.share.ControllerBase;
import com.jjkeller.kmbapi.controller.share.CurrentEvent;
import com.jjkeller.kmbapi.controller.share.DateOnlyEqualityComparator;
import com.jjkeller.kmbapi.controller.share.MandateObjectFactory;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.controller.utility.EmployeeLogUtilities;
import com.jjkeller.kmbapi.controller.utility.ErrorLogHelper;
import com.jjkeller.kmbapi.controller.utility.RESTWebServiceHelper;
import com.jjkeller.kmbapi.enums.DriverTypeEnum;
import com.jjkeller.kmbapi.enums.DrivingNotificationTypeEnum;
import com.jjkeller.kmbapi.enums.DutyStatusEnum;
import com.jjkeller.kmbapi.enums.ExemptLogTypeEnum;
import com.jjkeller.kmbapi.enums.HosAlertTypeEnum;
import com.jjkeller.kmbapi.enums.LogCheckerComplianceDatesTypeEnum;
import com.jjkeller.kmbapi.enums.RuleSetTypeEnum;
import com.jjkeller.kmbapi.proxydata.DriverHoursAvailableSummary;
import com.jjkeller.kmbapi.proxydata.DriverHoursAvailableSummaryList;
import com.jjkeller.kmbapi.proxydata.DutySummary;
import com.jjkeller.kmbapi.proxydata.EldEventAdapterList;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEventList;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HosAuditController extends ControllerBase {

	private HosAlertController _alertController;
	
	public HosAuditController(Context ctx) {
		super(ctx);

		_alertController = new HosAlertController(ctx);
		
		int minutes = GlobalState.getInstance().getAppSettings(ctx).getHoursAvailableUpdateMinutes();
		if (minutes < 0) {
			this.setIsEnabled(false);
			this.setRecordingIntervalMinutes(-1);
		} else {
			this.setIsEnabled(true);
			this.setRecordingIntervalMinutes(minutes);
		}
	}

	private static final long ONE_HOUR = 3600000;
	private static final long TWO_HOURS = 2 * ONE_HOUR;
	private static final long HALF_HOUR = (long) (.5 * ONE_HOUR);

	private boolean _isEnabled;

	private boolean getIsEnabled() {
		return this._isEnabled;
	}

	private void setIsEnabled(boolean isEnabled) {
		this._isEnabled = isEnabled;
	}

	private static final int _recordingIntervalTenMinutes = 10;
	private int _recordingIntervalMinutes = 60;

	private int getRecordingIntervalMinutes() {
		return GlobalState.getInstance().getCurrentUser().isPremiumProfile() ? _recordingIntervalTenMinutes : _recordingIntervalMinutes;
	}

	private void setRecordingIntervalMinutes(int recordingIntervalMinutes) {
		this._recordingIntervalMinutes = recordingIntervalMinutes;
	}
	
	public IHosRulesetCalcEngine getCalcEngine() {
		IHosRulesetCalcEngine calcEngine;
		Object val = GlobalState.getInstance().getRulesetCalcEngine();
		if (val == null) {
			calcEngine = this.CreateCalcEngineFor(this.getCurrentUser());
			this.setCalcEngine(calcEngine);
		} else {
			calcEngine = (IHosRulesetCalcEngine) val;
		}

		return calcEngine;
	}

	public OffDuty getOffDutyInfo() {
        try {
            OffDuty od = new OffDuty();
            Date now = DateUtility.CurrentHomeTerminalTime(this.getCurrentUser());
            Date startingLogDate = DateUtility.GetDateFromDateTime(DateUtility.AddDays(now, -2));  // go back 2 days to handle 34+ hours correctly (see PBI 49816 attachment)
            Date lastLogDate = EmployeeLogUtilities.CalculateLogStartTime(this.getContext(), now, this.getCurrentUser().getHomeTerminalTimeZone());
            Date todaysDate = lastLogDate;
            DutyStatusEnum lastDutyStatus = null;
            EmployeeLog lastLog=null;
            IAPIController empLogController = MandateObjectFactory.getInstance(this.getContext(),GlobalState.getInstance().getFeatureService()).getCurrentEventController();
            boolean KeepLooking =true;
            boolean IsLatestStatus = true;
            while (KeepLooking && lastLogDate.compareTo(startingLogDate)>=0 )
            {
                lastLog = empLogController.GetEmployeeLog(this.getCurrentUser(), lastLogDate);
                if (lastLog != null)
                {
                    EmployeeLogEldEventList eventLst = lastLog.getEldEventList();
                    EmployeeLogEldEvent[] events = eventLst.getEldEventList(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus);
                    int i = events.length-1;
                    while (KeepLooking  && i >= 0) {
                        lastDutyStatus = events[i].getDutyStatusEnum();
                        if (IsLatestStatus) {
                            od.setIsOffDuty(lastDutyStatus.isExemptOffDutyStatus());
                            IsLatestStatus=false;
                        }
                        if (lastDutyStatus.isExemptOnDutyStatus()) {
                            KeepLooking = false;
                        }
                        else
                            od.setLastOffDutyTime(events[i].getStartTime());
                        i--;
                    }
                }

                lastLogDate = DateUtility.AddDays(lastLogDate, -1);
            }
            if (IsLatestStatus){
                od.setIsOffDuty(true);
                if (lastLog==null)
                    od.setLastOffDutyTime(startingLogDate);
                else
                    od.setLastOffDutyTime(lastLogDate);
            }
            return od;
        }
        catch (Exception excp)
        {
            return null;
        }
    }

	private void setCalcEngine(IHosRulesetCalcEngine ihosRulesetCalcEngine) {
		GlobalState.getInstance().setRulesetCalcEngine(ihosRulesetCalcEngine);
	}

	public HoursOfServiceSummary getCalcEngineSummary() {
		return (HoursOfServiceSummary) GlobalState.getInstance().getCalcEngineSummary();
	}

	private void setCalcEngineSummary(HoursOfServiceSummary summary) {
		GlobalState.getInstance().setCalcEngineSummary(summary);
	}

	private Date getNextServerUpdateTimestamp() {
		Date timestamp;
		Object val = GlobalState.getInstance().getNextServerUpdateTimestamp();
		if (val == null) {
			timestamp = null;
		} else {
			timestamp = (Date) val;
		}

		return timestamp;
	}

	private void setNextServerUpdateTimestamp(Date timestamp) {
		GlobalState.getInstance().setNextServerUpdateTimestamp(timestamp);
	}

	// / <summary>
	// / Update the calcEngine to include time spent on the current log event
	// / for the current user.
	public void UpdateForCurrentLogEvent() {
		LogEntryController logEntryController = new LogEntryController(this.getContext());

		//the current log could be null if we're in view only mode, there were no local logs for the user,
		//and there weren't any to download.  if that's the case there's nothing to do here.
		if(GlobalState.getInstance().getIsViewOnlyMode() && logEntryController.getCurrentEmployeeLog() == null)
			return;

		// reset the calcengine so that all of the logs are loaded again
		this.setCalcEngine(null);

		Date logEventStartTimestamp;
		com.jjkeller.kmbapi.enums.DutyStatusEnum logEventDutyStatus;
		com.jjkeller.kmbapi.enums.RuleSetTypeEnum logEventRuleset;

		Date now = DateUtility.CurrentHomeTerminalTime(this.getCurrentUser());
		now = DateUtility.ConvertToPreviousLogEventTime(now);
		Date nowLogDate = EmployeeLogUtilities.CalculateLogStartTime(this.getContext(), now, this.getCurrentUser().getHomeTerminalTimeZone());

		//if the log is for today
		if(new DateOnlyEqualityComparator(logEntryController.getCurrentEmployeeLog().getLogDate(), nowLogDate).areEqual())
		{
			// fetch the recent event info for the current user
			CurrentEvent currentEvent = logEntryController.GetCurrentEventValues();
			logEventStartTimestamp = currentEvent.eventTimestamp;
			logEventDutyStatus = currentEvent.dutyStatusEnum;

			logEventRuleset = logEntryController.GetCurrentEventValues_RulesetType();
		} else
		{
			//if the current log is not from today (we're in view only mode, which doesn't create the current log on login)
			//then create a pseudo-off-duty event for when today's log would begin
			logEventStartTimestamp = nowLogDate;
			logEventDutyStatus = new DutyStatusEnum(DutyStatusEnum.OFFDUTY);
			logEventRuleset = logEntryController.getCurrentEmployeeLog().getRuleset();
		}
		// determine the event duration and duty status

		Date logEventLogDate = EmployeeLogUtilities.CalculateLogStartTime(this.getContext(), logEventStartTimestamp, this.getCurrentUser().getHomeTerminalTimeZone());

		// if current time is on a new log date as compared to last log event,
		// set event duration
		// to current time duration since midnight, otherwise set event duration
		// to duration since
		// last event
		long eventDuration = 0;
		if (nowLogDate.compareTo(logEventLogDate) > 0)
			eventDuration = now.getTime() - nowLogDate.getTime();
		else
			eventDuration = now.getTime() - logEventStartTimestamp.getTime();

		if (eventDuration < 0) {
			// 2013.04.05 sjn seen a scenario when during a driving violation,
			// when the truck stops that the event duration goes negative
			// because
			// now is before logEventStartTimestamp creating a negative duration
			// In this case, it seems like duration should be 0
			eventDuration = 0;
		}

		com.jjkeller.kmbapi.calcengine.Enums.DutyStatusEnum dutyStatus = this.Convert(logEventDutyStatus);
		com.jjkeller.kmbapi.calcengine.Enums.RuleSetTypeEnum ruleset = this.Convert(logEventRuleset);

		this.setCalcEngineSummary(this.getCalcEngine().CheckDutyStatusDuration(logEventStartTimestamp, dutyStatus, eventDuration, ruleset));
	}

	// / <summary>
	// / Answer the current weekly summary.
	// / In order for this to represent the most current information, a call to
	// / 'UpdateForCurrentLogEvent' needs to made before doing this.
	// / </summary>
	public DutySummary WeeklyDutySummary() {
		DutySummary weeklyDutySummary = new DutySummary();

		IHosRulesetCalcEngine calcEngine = this.getCalcEngine();
		if (calcEngine != null) {
            weeklyDutySummary = DutySummary.createFromBundle(calcEngine.WeeklyDutySummary(this.getCalcEngineSummary()), this.getContext());
		}

		return weeklyDutySummary;
	}

	// / <summary>
	// / Answer the current daily summary
	// / In order for this to represent the most current information, a call to
	// / 'UpdateForCurrentLogEvent' needs to made before doing this.
	// / </summary>
	public DutySummary DailyDutySummary() {
		DutySummary dailySummary = new DutySummary();

		IHosRulesetCalcEngine calcEngine = this.getCalcEngine();
		if (calcEngine != null) {
			dailySummary = DutySummary.createFromBundle(calcEngine.DailyDutySummary(this.getCalcEngineSummary()), this.getContext());
		}

		return dailySummary;
	}

	// / <summary>
	// / Answer the current drive time summary
	// / In order for this to represent the most current information, a call to
	// / 'UpdateForCurrentLogEvent' needs to made before doing this.
	// / </summary>
	public DutySummary DriveTimeSummary() {
		DutySummary driveSummary = new DutySummary();

		IHosRulesetCalcEngine calcEngine = this.getCalcEngine();
		if (calcEngine != null) {
			driveSummary = DutySummary.createFromBundle(calcEngine.DailyDriveSummary(this.getCalcEngineSummary()), this.getContext());
		}

		return driveSummary;
	}

	// / <summary>
	// / Answer the current drive time summary against the 8 hour rule.
	// / If is expected that the Rest Break summary will return null if the Rest
	// Break rule is not enforced
	// / </summary>
	public DutySummary DriveTimeRestBreakSummary() {
		DutySummary driveBreakSummary = null;

		LogEntryController logEntryController = new LogEntryController(this.getContext());
		EmployeeLog currentLog = logEntryController.getCurrentEmployeeLog();

		// return null if user is exempt from 8 hour rule
		if(currentLog != null) {
			if (currentLog.getIsExemptFrom30MinBreakRequirement())
				return null;
		}
		else{
			//In case the current log process fails we can always resort to checking the employee rule.
			if (this.getCurrentDesignatedDriver().getIsExemptFrom30MinBreakRequirement())
				return null;
		}

		IHosRulesetCalcEngine calcEngine = this.getCalcEngine();
		if (calcEngine != null) {
            driveBreakSummary = DutySummary.createFromBundle(calcEngine.DailyDriveRestBreakSummary(this.getCalcEngineSummary()), this.getContext());
		}

		return driveBreakSummary;
	}

	/**
	 * Attempt to update the DMO server with the calc engine summary for each logged in user with an appropriate data profile.
	 * The DMO update will only happen periodically.
	 * The period of the update is controlled by the config setting 'HoursAvailableUpdateMinutes'.
	 */
	public void PerformPeriodicServerUpdate() {
		Date now = DateUtility.getCurrentDateTimeUTC();
		if (this.ShouldGenerateServerUpdate(now)) {
			this.PerformServerUpdateAsync();
			Date nextTimeToUpdate = DateUtility.AddMinutes(now, this.getRecordingIntervalMinutes());
			this.setNextServerUpdateTimestamp(nextTimeToUpdate);
		}
	}

	/**
	 * Update the DMO server with the driver's hours available summary for all users with a sufficient profile
	 * @return true if successful and false otherwise
	 */
	public boolean PerformServerUpdate() {
		boolean isSuccessful = false;
		List<DriverHoursAvailableSummary> listToSend = this.CreateSummariesToSend();

		// Send them all up to DMO
		if (listToSend.size() > 0) {
			isSuccessful = this.SubmitHourSummariesToDMO(listToSend);
		} else {
			//data profile checking has been moved to CreateSummariesToSend
			//so it's valid that we may have an empty list
			isSuccessful = true;
		}

		return isSuccessful;
	}

	/**
	 * Attempt to do an asynchronous update to the DMO server with the calc
	 * engine summary for each logged in user.
	 */
	public void PerformServerUpdateAsync() {
		new PerformServerUpdateTask(getContext()).execute();
	}

	// / <summary>
	// / Create the correct calcEngine for the user based on the rules for the
	// user.
	// / </summary>
	private IHosRulesetCalcEngine CreateCalcEngineFor(User user) {
		IHosRulesetCalcEngine calcEngine = null;

		// for Canada, determine if there is a team driver at the present time
		boolean isTeamDriverPresent = false;
		EmployeeLog currentLog = GlobalState.getInstance().getCurrentEmployeeLog();
		Date now = DateUtility.CurrentHomeTerminalTime(user);
		if(currentLog == null) {
			IAPIController empLogController = MandateObjectFactory.getInstance(this.getContext(), GlobalState.getInstance().getFeatureService()).getCurrentEventController();
			currentLog = empLogController.GetEmployeeLog(user, DateUtility.GetDateFromDateTime(now));
		}
		if (user.getRulesetTypeEnum().isCanadianRuleset()
				&& currentLog != null
				&& currentLog.getTeamDriverList() != null
				&& currentLog.getTeamDriverList().IsTeamDriverPresent(now)) {
					isTeamDriverPresent = true;
		}

		// Determine if the compliance dates are active
		LogCheckerComplianceDatesController complianceDatesController = new LogCheckerComplianceDatesController(this.getContext());
		boolean isActive_8HrDriving = complianceDatesController.IsLogCheckerComplianceDateActive(LogCheckerComplianceDatesTypeEnum.JULY2013_8HRDRIVING, TimeKeeper.getInstance().now(), true);

		// determine if the current log is exempt (this is used to determine which audit engine to use)
		ExemptLogTypeEnum currentLogExemptType;

		if(currentLog == null) {
			currentLogExemptType = user.getExemptLogType();
		} else {
			currentLogExemptType = currentLog.getExemptLogType();
		}

		// determine which calc engine is required
		switch (user.getRulesetTypeEnum().getValue()) {
		case RuleSetTypeEnum.ALASKA_7DAY:
			if (user.getDriverType().getValue() == DriverTypeEnum.PASSENGERCARRYING) {
				calcEngine = RulesetFactory.ForAlaska7DayPassenger();
			} else {
				calcEngine = RulesetFactory.ForAlaska7DayProperty(user.getIs34HourResetAllowed());
			}
			break;

		case RuleSetTypeEnum.ALASKA_8DAY:
			if (user.getDriverType().getValue() == DriverTypeEnum.PASSENGERCARRYING) {
				calcEngine = RulesetFactory.ForAlaska8DayPassenger();
			} else {
				calcEngine = RulesetFactory.ForAlaska8DayProperty(user.getIs34HourResetAllowed());
			}
			break;

		case RuleSetTypeEnum.CALIFORNIA_INTRASTATE:
			if (user.getDriverType().getValue() == DriverTypeEnum.PASSENGERCARRYING) {
				calcEngine = RulesetFactory.ForCaliforniaIntrastatePassenger();
			} else {
				calcEngine = RulesetFactory.ForCaliforniaIntrastateProperty(user.getIs34HourResetAllowed());
			}
			break;

		case RuleSetTypeEnum.US60HOUR:
			if (user.getDriverType().getValue() == DriverTypeEnum.PASSENGERCARRYING) {
				if(currentLogExemptType.getValue() == ExemptLogTypeEnum.EXEMPTLOGTYPE100AIRMILE)
					calcEngine = RulesetFactory.ForExempt100MilePassengerCarrying60Hour();

				//note: there is no concept of Passenger Carrying 150 mile exempt logs, so if this happens the standard USFederal ruleset will be used

				if(calcEngine == null)
					calcEngine = RulesetFactory.ForUS60Passenger();
			} else {
				if(currentLogExemptType.getValue() == ExemptLogTypeEnum.EXEMPTLOGTYPE100AIRMILE)
					calcEngine = RulesetFactory.ForExempt100MilePropertyCarrying60Hour(user.getIs34HourResetAllowed());

				if(currentLogExemptType.getValue() == ExemptLogTypeEnum.EXEMPTLOGTYPE150AIRMILENONCDL)
					calcEngine = RulesetFactory.ForExempt150MilePropertyCarrying60Hour(user.getIs34HourResetAllowed());

				if(calcEngine == null)
					calcEngine = RulesetFactory.ForUS60Property(user.getIsShorthaulException(), user.getIs34HourResetAllowed(), isActive_8HrDriving);
			}
			break;

		case RuleSetTypeEnum.US70HOUR:
			if (user.getDriverType().getValue() == DriverTypeEnum.PASSENGERCARRYING) {
				if(currentLogExemptType.getValue() == ExemptLogTypeEnum.EXEMPTLOGTYPE100AIRMILE)
					calcEngine = RulesetFactory.ForExempt100MilePassengerCarrying70Hour();

				//note: there is no concept of Passenger Carrying 150 mile exempt logs, so if this happens the standard USFederal ruleset will be used

				if(calcEngine == null)
					calcEngine = RulesetFactory.ForUS70Passenger();
			} else {
				if(currentLogExemptType.getValue() == ExemptLogTypeEnum.EXEMPTLOGTYPE100AIRMILE)
					calcEngine = RulesetFactory.ForExempt100MilePropertyCarrying70Hour(user.getIs34HourResetAllowed());

				if(currentLogExemptType.getValue() == ExemptLogTypeEnum.EXEMPTLOGTYPE150AIRMILENONCDL)
					calcEngine = RulesetFactory.ForExempt150MilePropertyCarrying70Hour(user.getIs34HourResetAllowed());

				if(calcEngine == null)
					calcEngine = RulesetFactory.ForUS70Property(user.getIsShorthaulException(), user.getIs34HourResetAllowed(), isActive_8HrDriving);
			}
			break;

		case RuleSetTypeEnum.CANADIAN_CYCLE1:
			calcEngine = RulesetFactory.ForCanadianCycle1(isTeamDriverPresent);
			break;

		case RuleSetTypeEnum.CANADIAN_CYCLE2:
			calcEngine = RulesetFactory.ForCanadianCycle2(isTeamDriverPresent);
			break;

		case RuleSetTypeEnum.FLORIDA_7DAY:
			if (user.getDriverType().getValue() == DriverTypeEnum.PROPERTYCARRYING) {
				calcEngine = RulesetFactory.ForFlorida7DayProperty(user.getIs34HourResetAllowed());
			} else {
				calcEngine = RulesetFactory.ForFlorida7DayPassenger();
			}
			break;

		case RuleSetTypeEnum.FLORIDA_8DAY:
			if (user.getDriverType().getValue() == DriverTypeEnum.PROPERTYCARRYING) {
				calcEngine = RulesetFactory.ForFlorida8DayProperty(user.getIs34HourResetAllowed());
			} else {
				calcEngine = RulesetFactory.ForFlorida8DayPassenger();
			}
			break;

			case RuleSetTypeEnum.WISCONSIN_7DAY:
				if (user.getDriverType().getValue() == DriverTypeEnum.PASSENGERCARRYING) {
					calcEngine = RulesetFactory.ForWisconsin7DayPassenger();
				} else {
					calcEngine = RulesetFactory.ForWisconsin7DayProperty();
				}
				break;

			case RuleSetTypeEnum.WISCONSIN_8DAY:
				if (user.getDriverType().getValue() == DriverTypeEnum.PASSENGERCARRYING) {
					calcEngine = RulesetFactory.ForWisconsin8DayPassenger();
				} else {
					calcEngine = RulesetFactory.ForWisconsin8DayProperty();
				}
				break;

			case RuleSetTypeEnum.USCONSTRUCTION_7DAY:
					calcEngine = RulesetFactory.ForUSConstruction7DayProperty(user.getIs34HourResetAllowed(), isActive_8HrDriving, user.getIsShorthaulException());
				break;

			case RuleSetTypeEnum.USCONSTRUCTION_8DAY:
					calcEngine = RulesetFactory.ForUSConstruction8DayProperty(user.getIs34HourResetAllowed(), isActive_8HrDriving, user.getIsShorthaulException());
				break;

			case RuleSetTypeEnum.TEXAS:
			calcEngine = RulesetFactory.ForTexas(user.getIs34HourResetAllowed());
			break;

		case RuleSetTypeEnum.USOILFIELD:
			calcEngine = RulesetFactory.ForUSOilField(user.getIsShorthaulException(), user.getIs34HourResetAllowed(), isActive_8HrDriving);
			break;

		case RuleSetTypeEnum.TEXASOILFIELD:
			calcEngine = RulesetFactory.ForTexasOilField(user.getIs34HourResetAllowed());
			break;

		case RuleSetTypeEnum.USMOTIONPICTURE_7DAY:
			if (user.getDriverType().getValue() == DriverTypeEnum.PASSENGERCARRYING)
				calcEngine = RulesetFactory.ForUSMotionPicture7DayPassenger();
			else
				calcEngine = RulesetFactory.ForUSMotionPicture7DayProperty(user.getIs34HourResetAllowed());
			break;

		case RuleSetTypeEnum.USMOTIONPICTURE_8DAY:
			if (user.getDriverType().getValue() == DriverTypeEnum.PASSENGERCARRYING)
				calcEngine = RulesetFactory.ForUSMotionPicture8DayPassenger();
			else
				calcEngine = RulesetFactory.ForUSMotionPicture8DayProperty(user.getIs34HourResetAllowed());
			break;

		case RuleSetTypeEnum.CALIFORNIA_MP_80:
			calcEngine = RulesetFactory.ForCaliforniaMotionPicture80HourProperty(user.getIs34HourResetAllowed());
			break;

		default:
			throw new RuntimeException("Unable to determine ruleset");
		}

		this.InitializeCalcEngine(calcEngine, user);
		return calcEngine;
	}

	// / <summary>
	// / Initialize the calc engine by adding each log for the user into the
	// calcEngine.
	// / All of the logs included in the duty period will be added to the
	// calcEngine.
	// / </summary>
	private void InitializeCalcEngine(IHosRulesetCalcEngine calcEngine, User user) {
		HoursOfServiceSummary summary = null;
        String TAG = "InitializeCalcEngine";

		// fetch the logs for the current user that represent the duty period
		// and load them into the calc engine
		Date now = DateUtility.CurrentHomeTerminalTime(user);

		Date startingLogDate = calcEngine.DateOfAuditPeriodStart(now);
		Date endingLogDate = EmployeeLogUtilities.CalculateLogStartTime(this.getContext(), now, user.getHomeTerminalTimeZone());
		Date lastLogDate = endingLogDate;
		DutyStatusEnum lastDutyStatus = null;
        Log.v(TAG, String.format("now: %s startingLogDate: %s endingLogDate: %s lastLogDate: %s", now, startingLogDate, endingLogDate, lastLogDate));

		// get the last log available in the database in startingLogDate to
		// endingLogDate range
		IAPIController empLogController = MandateObjectFactory.getInstance(this.getContext(),GlobalState.getInstance().getFeatureService()).getCurrentEventController();
		EmployeeLog lastLog = empLogController.GetEmployeeLog(user, lastLogDate);

        Log.v(TAG, String.format("lastLog: %s", lastLog));

		while (lastLog == null && lastLogDate.compareTo(startingLogDate) >= 0) {
			lastLogDate = DateUtility.AddDays(lastLogDate, -1);
			lastLog = empLogController.GetEmployeeLog(user, lastLogDate);
		}

        Log.v(TAG, String.format("lastLog: %s", lastLog));

		//we need to retain the last duty status from the last valid log
		if(lastLog != null) {
			EmployeeLogEldEvent[] logEvtList = lastLog.getEldEventList().getEldEventList(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus);
			if (logEvtList != null && logEvtList.length > 0) {
				lastDutyStatus = logEvtList[logEvtList.length - 1].getDutyStatusEnum();
			}
		}
		else
			lastDutyStatus = new DutyStatusEnum(DutyStatusEnum.OFFDUTY);

        Log.v(TAG, String.format("lastDutyStatus: %s", lastDutyStatus));

        boolean firstLoginCollection = true;
        Date logDate = EmployeeLogUtilities.CalculateLogStartTime(this.getContext(), startingLogDate, user.getHomeTerminalTimeZone());
        Log.v(TAG, String.format("logDate: %s", logDate));
		Date previousWeeklyResetDate = empLogController.GetPreviousWeeklyResetStartTimestamp(startingLogDate);
        Log.v(TAG, String.format("previousWeeklyResetDate: %s", previousWeeklyResetDate));

        while (logDate.compareTo(now) <= 0) {
			// fetch the log by date, going chronologically forward
			EmployeeLog empLog = empLogController.GetEmployeeLog(user, logDate);
            Log.v(TAG, String.format("empLog: %s", empLog));
			// if log doesn't exist and the current logdate is after the last
			// log
			// in the range, create a temporary log for the current log date
			// carrying over the status at the end of the last log
			if (empLog == null && logDate.compareTo(lastLogDate) > 0) {
				EmployeeLog tempEmpLog = EmployeeLogUtilities.CreateNewLog(this.getContext(), user, logDate, lastDutyStatus, null);
				if (tempEmpLog != null) {
                    empLog = tempEmpLog;
                    Log.v(TAG, String.format("tempEmpLog: %s", tempEmpLog));
                }
			}
			if (empLog != null) {
				boolean hasLogCompleted = true;
				Date empLogEndTimestamp = DateUtility.AddDays(EmployeeLogUtilities.CalculateLogStartTime(this.getContext(), empLog.getLogDate(), user.getHomeTerminalTimeZone()), 1);
                Log.v(TAG, String.format("empLogEndTimestamp1: %s", empLogEndTimestamp));

                Date logDate1 = empLog.getLogDate();
                boolean areEqual = new DateOnlyEqualityComparator(endingLogDate, logDate1).areEqual();
                if (areEqual) {
					// this is the last log event to add
					// as such, this event has not really ended yet so simply
					// calc the event duration based on the time now
					hasLogCompleted = false;
					empLogEndTimestamp = now;
                    Log.v(TAG, String.format("empLogEndTimestamp2: %s", empLogEndTimestamp));
				}

				summary = this.AddLogToCalcEngine(calcEngine, empLog, empLogEndTimestamp, hasLogCompleted, firstLoginCollection, previousWeeklyResetDate);
				firstLoginCollection = false;
				previousWeeklyResetDate = null;

				if (logDate.equals(endingLogDate)) {
					// when loading the last log in the bunch, then fetch the
					// ending
					// log values and persist them if different.
					Bundle bundle = calcEngine.FetchAtEndOfLog(logDate);
					boolean isShortHaulUsed = bundle.getBoolean(this.getContext().getString(R.string.shorthaulexception_used), false);

					Date weeklyResetStartTimestampDate = null;
					long weeklyResetStartTimestamp = bundle.getLong(this.getContext().getString(R.string.weeklyResetStartTimestamp), 0);
					if (weeklyResetStartTimestamp > 0)
						weeklyResetStartTimestampDate = new Date(weeklyResetStartTimestamp);

					long empLogWeeklyResetStartTimestamp = 0;
					if (empLog.getWeeklyResetStartTimestamp() != null)
						empLogWeeklyResetStartTimestamp = empLog.getWeeklyResetStartTimestamp().getTime();

					boolean isWeeklyResetUsed = bundle.getBoolean(this.getContext().getString(R.string.isweeklyresetused), false);

					if (empLog.getIsShortHaulExceptionUsed() != isShortHaulUsed || empLogWeeklyResetStartTimestamp != weeklyResetStartTimestamp || empLog.getIsWeeklyResetUsed() != isWeeklyResetUsed) {
						// the fields on the log are different than before,
						// so save these values into the log
						empLog.setIsShortHaulExceptionUsed(isShortHaulUsed);

						// EmployeeLogFacade empLogFacade = new
						// EmployeeLogFacade(this.getContext(),
						// this.getCurrentUser());
						// boolean shouldUpdateResetOnLog =
						// !empLogFacade.DoesWeeklyResetExistNewerThan(weeklyResetStartTimestampDate);
						boolean isLessThan2DaysAgo = true;
						Date twoDaysAgo = DateUtility.AddDays(empLog.getLogDate(), -2);
						if (weeklyResetStartTimestampDate != null && weeklyResetStartTimestampDate.compareTo(twoDaysAgo) < 0)
							isLessThan2DaysAgo = false;

						if (isLessThan2DaysAgo) {
							// as long as the calculated reset is the most
							// recent reset, then update the current log
							empLog.setWeeklyResetStartTimestamp(weeklyResetStartTimestampDate);
							if (weeklyResetStartTimestampDate != null) {
								// when saving a calculated reset time, then set
								// the reset to used and remove the overridden
								// flag
								empLog.setIsWeeklyResetUsed(isWeeklyResetUsed);
								empLog.setIsWeeklyResetUsedOverridden(false);
							}
						}

						//don't actually save the log in view only mode
						if(!GlobalState.getInstance().getIsViewOnlyMode())
							empLogController.SaveLocalEmployeeLog(user, empLog);

						LogEntryController logEntryCntrlr = new LogEntryController(this.getContext());
						if (logEntryCntrlr.IsTheDriver(user)) {
							// update the driver's log because it exists in
							// state
							EmployeeLog driversLog = logEntryCntrlr.getCurrentDriversLog();

							driversLog.setIsShortHaulExceptionUsed(empLog.getIsShortHaulExceptionUsed());
							driversLog.setWeeklyResetStartTimestamp(empLog.getWeeklyResetStartTimestamp());
							driversLog.setIsWeeklyResetUsed(empLog.getIsWeeklyResetUsed());
							driversLog.setIsWeeklyResetUsedOverridden(empLog.getIsWeeklyResetUsedOverridden());
						}

						if(logEntryCntrlr.IsTheCurrentActiveUser(user))
						{
							//update the current user's log because it exists in state
							EmployeeLog usersLog = logEntryCntrlr.getCurrentEmployeeLog();

							usersLog.setIsShortHaulExceptionUsed(empLog.getIsShortHaulExceptionUsed());
							usersLog.setWeeklyResetStartTimestamp(empLog.getWeeklyResetStartTimestamp());
							usersLog.setIsWeeklyResetUsed(empLog.getIsWeeklyResetUsed());
							usersLog.setIsWeeklyResetUsedOverridden(empLog.getIsWeeklyResetUsedOverridden());
						}
					}

				}

				// remember duty status of last event on log

				EmployeeLogEldEvent lastEvt = EmployeeLogUtilities.GetLastEventInLog(empLog);
				if (lastEvt != null)
				lastDutyStatus = lastEvt.getDutyStatusEnum();
			}
			logDate = DateUtility.AddDays(logDate, 1);
		}

		this.setCalcEngineSummary(summary);
	}

    // / <summary>
	// / Add all of the log events from the log into the calcEngine.
	// / When processing the last event on the log, the duration of that event
	// lasts
	// / until the end of the log. If the last event on the log has not
	// completed,
	// / it will only be checked, but not ended.
	// / </summary>
	private HoursOfServiceSummary AddLogToCalcEngine(IHosRulesetCalcEngine calcEngine, EmployeeLog empLog, Date empLogEndingTimestamp, boolean hasLogCompleted, boolean firstLogInCollection, Date previousWeeklyResetTimestamp) {
		HoursOfServiceSummary summary = null;
		boolean isLastEventOnLog = false;

		// determine the CD deferral type
		CanadaDeferralTypeEnum deferralType = CanadaDeferralTypeEnum.None;
		if (empLog.getCanadaDeferralType().getValue() == com.jjkeller.kmbapi.enums.CanadaDeferralTypeEnum.DAYONE) {
			deferralType = CanadaDeferralTypeEnum.DayOne;
		}
		if (empLog.getCanadaDeferralType().getValue() == com.jjkeller.kmbapi.enums.CanadaDeferralTypeEnum.DAYTWO) {
			deferralType = CanadaDeferralTypeEnum.DayTwo;
		}


		LogProperties logProperties = new LogProperties(EmployeeLogUtilities.CalculateLogStartTime(this.getContext(), empLog.getLogDate(), this.getCurrentUser().getHomeTerminalTimeZone()),
				!hasLogCompleted, empLog.getHasReturnedToLocation(), empLog.getIsShortHaulExceptionUsed(), deferralType, empLog.getWeeklyResetStartTimestamp(), empLog.getIsHaulingExplosives(),
				empLog.getIsWeeklyResetUsed(), empLog.getIsWeeklyResetUsedOverridden(), empLog.getIsOperatesSpecificVehiclesForOilfield(), empLog.getExemptLogType(), empLog.getIsNonCDLShortHaulExceptionUsed(),
				empLog.getIsExemptFrom30MinBreakRequirement());

		if(firstLogInCollection)
		{
			logProperties.setIsFirstLogInCollection(true);
			logProperties.setLastUsedWeeklyResetStartTimestamp(previousWeeklyResetTimestamp);
		}


		calcEngine.PrepareStartOfLog(logProperties);
		EmployeeLogEldEvent[] activeEvents = empLog.getEldEventList().getActiveEldEventList(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus);
		for (int index = 0; index < activeEvents.length; index++) {
			EmployeeLogEldEvent logEvent = activeEvents[index];
			long eventDuration = 0;
			if (index + 1 < activeEvents.length) {
				// for a normal event, the duration is the time lapsed between
				// the two events
				EmployeeLogEldEvent nextEvent = activeEvents[index + 1];
				eventDuration = nextEvent.getStartTime().getTime() - logEvent.getStartTime().getTime();

                Log.d("AddLogToCalcEngine",String.format("PrimaryKey: %s EvtType: %s EvtFrom: %s EvtTo: %s EventDuration: %s", logEvent.getPrimaryKey(), logEvent.getEventCode(), logEvent.getStartTime(), nextEvent.getStartTime(), eventDuration));
            } else {
				// processing the last log event for the log
				// the duration of this event is calc'ed until the end of the
				// log
				eventDuration = empLogEndingTimestamp.getTime() - logEvent.getStartTime().getTime();

                Log.d("AddLogToCalcEngine",String.format("PrimaryKey: %s EvtType: %s EvtFrom: %s EvtTo: %s EventDuration: %s", logEvent.getPrimaryKey(), logEvent.getEventCode(), logEvent.getStartTime(), empLogEndingTimestamp, eventDuration));
                Log.d("AddLogToCalcEngine", "isLastEventOnLog = true");
				isLastEventOnLog = true;
			}

            // translate the duty status enum from one package to the other
			com.jjkeller.kmbapi.calcengine.Enums.DutyStatusEnum dutyStatus = this.Convert(logEvent.getDutyStatusEnum());
			com.jjkeller.kmbapi.calcengine.Enums.RuleSetTypeEnum ruleset = this.Convert(logEvent.getRulesetType());

			if (eventDuration != 0) {
				// determine if processing a log event that has been
				// 'completed'.
				// a completed log event is one which there is a chronologically
				// newer
				// event. The newer event may be either on this log, or a newer
				// log.
				if (hasLogCompleted || !isLastEventOnLog) {
					summary = calcEngine.EndOfDutyStatusUpdate(logEvent.getStartTime(), dutyStatus, eventDuration, ruleset);
				} else {
					summary = calcEngine.CheckDutyStatusDuration(logEvent.getStartTime(), dutyStatus, eventDuration, ruleset);
				}
			}
		}

		return summary;
	}

	// / <summary>
	// / Create a driver hours summary for each of the currently logged in users
	// of KMB.
	// / Answer the list of summaries.
	// / </summary>
	// / <returns></returns>
	private List<DriverHoursAvailableSummary> CreateSummariesToSend() {
		ArrayList<DriverHoursAvailableSummary> listToSend = new ArrayList<DriverHoursAvailableSummary>();
		try {
			for (User usr : this.getLoggedInUserList()) {
				
				IHosRulesetCalcEngine calcEngine = this.CreateCalcEngineFor(usr);
				HoursOfServiceSummary calcEngineSummary = this.getCalcEngineSummary();

				// load the DMO summary for the user
				DriverHoursAvailableSummary summary = new DriverHoursAvailableSummary();
				summary.setEmployeeId(usr.getCredentials().getEmployeeId());

				Bundle bundle = calcEngine.WeeklyDutySummary(calcEngineSummary);
				summary.setWeeklyDutyHoursAllowed(bundle.getInt(this.getContext().getString(R.string.summary_allowed)));
				long usedMS = bundle.getLong(this.getContext().getString(R.string.summary_used));
				int minutes = (int) DateUtility.ConvertMillisecondsToMinutes(usedMS);
				summary.setWeeklyDutyTimeUsed(String.format("%d.%02d:%02d:00", minutes / 1440, minutes % 1440 / 60, minutes % 1440 % 60));

				bundle = calcEngine.DailyDutySummary(calcEngineSummary);
				summary.setDailyDutyHoursAllowed(bundle.getInt(this.getContext().getString(R.string.summary_allowed)));
				usedMS = bundle.getLong(this.getContext().getString(R.string.summary_used));
				minutes = (int) DateUtility.ConvertMillisecondsToMinutes(usedMS);
				summary.setDailyDutyTimeUsed(String.format("%d.%02d:%02d:00", minutes / 1440, minutes % 1440 / 60, minutes % 1440 % 60));

				bundle = calcEngine.DailyDriveSummary(calcEngineSummary);
				summary.setDrivingHoursAllowed(bundle.getInt(this.getContext().getString(R.string.summary_allowed)));
				usedMS = bundle.getLong(this.getContext().getString(R.string.summary_used));
				minutes = (int) DateUtility.ConvertMillisecondsToMinutes(usedMS);
				summary.setDrivingTimeUsed(String.format("%d.%02d:%02d:00", minutes / 1440, minutes % 1440 / 60, minutes % 1440 % 60));

				bundle = calcEngine.DailyDriveRestBreakSummary(calcEngineSummary);
				if (bundle != null && bundle.containsKey(this.getContext().getString(R.string.summary_used))) {
					// as long as the rest break hours are available, and have not gone "n/a", then report them
					summary.setDrivingHoursRestBreakAllowed(bundle.getInt(this.getContext().getString(R.string.summary_allowed)));
					usedMS = bundle.getLong(this.getContext().getString(R.string.summary_used));
					minutes = (int) DateUtility.ConvertMillisecondsToMinutes(usedMS);
					summary.setDrivingTimeRestBreakUsed(String.format("%d.%02d:%02d:00", minutes / 1440, minutes % 1440 / 60, minutes % 1440 % 60));
				}

				summary.setIsShortHaulExceptionAvailable(calcEngineSummary.getIsShortHaulExceptionAvailable());
				summary.setCalculationTimestamp(DateUtility.AddMilliseconds(calcEngineSummary.getRecentDutyTimestamp(), (int) calcEngineSummary.getRecentDutyLength()));

				IAPIController logController = MandateObjectFactory.getInstance(this.getContext(),GlobalState.getInstance().getFeatureService()).getCurrentEventController();
				EmployeeLog log = logController.GetCurrentEmployeeLogByUser(usr);


				ExemptLogTypeEnum exemptType;
				if(log == null) {
					exemptType = usr.getExemptLogType();
				} else {
					exemptType = log.getExemptLogType();
				}

				// for team driving, need to get the exempt log type for each of the current logged in employees SHOULD be the same.
				// need to use getCurrentEmployeeLog(), not getCurrentDriverLog
				summary.setExemptLogTypeEnum(exemptType);


				if (log != null) {
					// log might be null if the midnight transition has not occurred yet
					EmployeeLogEldEvent lastEldEvent = EmployeeLogUtilities.GetLastEventInLog(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus, log);

					summary.setLastMotionPictureProductionId(lastEldEvent.getMotionPictureProductionId());
					summary.setLastMotionPictureAuthorityId(lastEldEvent.getMotionPictureAuthorityId());
					if(usr.isPremiumProfile())
						summary.setDutyStatusEnum(lastEldEvent.getDutyStatusEnum());
				}
				if(usr.isPremiumProfile()){
					summary.setTrailerNumber(GlobalState.getInstance().get_currentTrailerNumbers());
					summary.setEobrSerialNumber(GlobalState.getInstance().getCurrentEobrSerialNumber());
					if(GlobalState.getInstance().getLastGPSLocation() != null){
						summary.setGpsLatitude(GlobalState.getInstance().getLastGPSLocation().getLatitudeDegrees());
						summary.setGpsLongitude(GlobalState.getInstance().getLastGPSLocation().getLongitudeDegrees());
					}
				}
				listToSend.add(summary);
			}
		} catch (Exception excp) {
			this.HandleException(excp, this.getContext().getString(R.string.createsummariestosend));
		}

		return listToSend;
	}

	// / <summary>
	// / Submit the list of summaries up to DMO.
	// / Answer if this was completed successfully.
	// / </summary>
	// / <returns></returns>
	private boolean SubmitHourSummariesToDMO(List<DriverHoursAvailableSummary> list) {
		boolean isSuccessful = false;
		if (this.getIsWebServicesAvailable()) {
			// second, attempt to send this batch to DMO
			DriverHoursAvailableSummaryList listToSend = new DriverHoursAvailableSummaryList();
			listToSend.setDriverHoursAvailableSummaryList(list.toArray(new DriverHoursAvailableSummary[list.size()]));

			RESTWebServiceHelper rwsh = new RESTWebServiceHelper(getContext());

			try {
				rwsh.SubmitDriverHoursAvailableSummaries(listToSend.getDriverHoursAvailableSummaryList());
				isSuccessful = true;
			} catch (IOException e) {
				ErrorLogHelper.RecordException(this.getContext(), e);
				e.printStackTrace();
			}
		}

		return isSuccessful;
	}

	// / <summary>
	// / Mark the beginning of a driving period.
	// / Calculate the time that the driving violation will occur and the
	// / number of message alerts that need to be shown to the driver.
	// / The time of the violation is calculated as the start of the
	// / driving segment plus the amount of driving time currently available.
	// / The number of message alerts to show depends on the employee's rule and
	// / the amount of time until the violation will occur.
	// / </summary>
	// / <returns></returns>
	public void MarkBeginningOfDrivingPeriod(Date drivingStartTime) {
		// ignore if the driver should not get alerts
		if (this.getCurrentDesignatedDriver().getDrivingNotificationTypeEnum().getValue() == DrivingNotificationTypeEnum.NOALERT)
			return;

		Date vioTime = null;
		IHosAlert drivingAlert = _alertController.GetAlert(HosAlertTypeEnum.StandardDriving);

		this.UpdateForCurrentLogEvent();
		if (this.getCalcEngine() != null && this.getCalcEngineSummary() != null) {
			long drvTimeAvail = this.getCalcEngine().CalculateDriveTimeRemaining(this.getCalcEngineSummary());
			
			// note: the violation time calculated will be localized to the
			// phone
			// rather than localized to the driver's home terminal timezone

			// note: the call to UpdateForCurrentLogEvent calcs the amount of
			// drive time available as of the current time...rather than from
			// the
			// start of the driving period. Simply add the drive time available
			// to the time it is right now to determine when the violation will
			// occur.
			Date now = DateUtility.getCurrentDateTimeUTC();
			vioTime = new Date(now.getTime() + drvTimeAvail);

			drivingAlert.setNextAlertTiming(drvTimeAvail);
		}

		drivingAlert.setAlertTime(vioTime);
		
		EmployeeLog log = GlobalState.getInstance().getCurrentEmployeeLog();
		if(log.getExemptLogType().getValue() == ExemptLogTypeEnum.EXEMPTLOGTYPE150AIRMILENONCDL) {
			// for 150 air mile exempt logs, the driving period violation also marks the exempt duty period violation
	        // note: the exempt alert message type should be the same as the driving alert type because they both expire at the same time
			IHosAlert exemptAlert = _alertController.GetAlert(HosAlertTypeEnum.ExemptDuty);
			exemptAlert.setAlertTime(vioTime);
			exemptAlert.setAlertTimingEnum(drivingAlert.getAlertTimingEnum());
		}
		
		//If there is a exempt duty alert that ends at exactly the same time, then turn the driving alert off
		IHosAlert exemptAlert = _alertController.GetAlert(HosAlertTypeEnum.ExemptDuty);
		if(exemptAlert.getAlertTime() != null && DateOnlyEqualityComparator.areEqual(exemptAlert.getAlertTime(), vioTime))
		{
			drivingAlert.Clear();
		}
	}

	// / <summary>
	// / Mark the end of a driving segment.
	// / Reset the violation timestamp being tracked.
	// / </summary>
	public void MarkEndOfDrivingPeriod(Date drivingEndTime) {
		IHosAlert alert = _alertController.GetAlert(HosAlertTypeEnum.StandardDriving);
		alert.Clear();
		
		alert = _alertController.GetAlert(HosAlertTypeEnum.RestBreak);
		alert.Clear();
		
		EmployeeLog log = GlobalState.getInstance().getCurrentEmployeeLog();
		if(log.getExemptLogType().getValue() == ExemptLogTypeEnum.EXEMPTLOGTYPE150AIRMILENONCDL) {
	        // for 150 air mile exempt logs, the end of a driving period also marks the end of the exempt duty period
	        MarkEndOfExemptDutyPeriod();
		}
	}
	
	
	// / <summary>
	// / Mark the beginning of a rest break period.
	// / Calculate the time that the driving violation will occur and the
	// / number of message alerts that need to be shown to the driver.
	// / The time of the violation is calculated as the start of the
	// / driving segment plus the amount of driving time currently available.
	// / The number of message alerts to show depends on the employee's rule and
	// / the amount of time until the violation will occur.
	// / </summary>
	// / <returns></returns>
	public void MarkBeginningOfRestBreakPeriod(Date drivingStartTime) {
		// ignore if the driver should not get alerts
		if (this.getCurrentDesignatedDriver().getDrivingNotificationTypeEnum().getValue() == DrivingNotificationTypeEnum.NOALERT)
			return;

		// ignore if the driver is exempt from 30 min rest break rule
		if (this.getCurrentDesignatedDriver().getIsExemptFrom30MinBreakRequirement())
			return;

		DutySummary restBreakSummary = this.DriveTimeRestBreakSummary();
		if(restBreakSummary == null)
			return;
		
		long restUsed = restBreakSummary.getUsedMilliseconds();
		long restAvail = restBreakSummary.getAvailableMilliseconds();
		
		boolean restBreak = (restUsed == 0 && restAvail == 0) ? false : true;
		
		IHosAlert alert = _alertController.GetAlert(HosAlertTypeEnum.RestBreak);
		Date vioTime = null;

		this.UpdateForCurrentLogEvent();
		if (this.getCalcEngine() != null && this.getCalcEngineSummary() != null &&  restBreak) {
			long restBreakTimeAvail = this.getCalcEngine().CalculateRestBreakTimeRemaining(this.getCalcEngineSummary());
			
			// note: the violation time calculated will be localized to the
			// phone
			// rather than localized to the driver's home terminal timezone

			// note: the call to UpdateForCurrentLogEvent calcs the amount of
			// drive time available as of the current time...rather than from
			// the
			// start of the driving period. Simply add the drive time available
			// to the time it is right now to determine when the violation will
			// occur.
			Date now = DateUtility.getCurrentDateTimeUTC();
			vioTime = new Date(now.getTime() + restBreakTimeAvail);
			
			alert.setNextAlertTiming(restBreakTimeAvail);
		}
		
		alert.setAlertTime(vioTime);
	}

	// / <summary>
	// / Mark the end of a rest break segment.
	// / Reset the violation timestamp being tracked.
	// / </summary>
	public void MarkEndOfRestBreakPeriod(Date drivingEndTime) {
		IHosAlert alert = _alertController.GetAlert(HosAlertTypeEnum.RestBreak);
		alert.Clear();
	}

	public void MarkBeginningOfExemptDutyPeriod() {
		if (this.getCurrentDesignatedDriver().getDrivingNotificationTypeEnum().getValue() == DrivingNotificationTypeEnum.NOALERT)
			return;
		
		if(GlobalState.getInstance().getCurrentDriversLog().getExemptLogType().getValue() == ExemptLogTypeEnum.NULL)
			return;
		
		UpdateForCurrentLogEvent();
		
		DutySummary dailyDutySummary = DailyDutySummary();
		if(dailyDutySummary == null)
			return;
		
		long dailyDutyAvail = dailyDutySummary.getAvailableMilliseconds();
		
		Date now = DateUtility.getCurrentDateTimeUTC();
		Date vioTime = new Date(now.getTime() + dailyDutyAvail);
		
		IHosAlert dutyAlert = _alertController.GetAlert(HosAlertTypeEnum.ExemptDuty);
		dutyAlert.setAlertTime(vioTime);
		dutyAlert.setNextAlertTiming(dailyDutyAvail);
	}

	public void MarkEndOfExemptDutyPeriod() {
		IHosAlert alert = _alertController.GetAlert(HosAlertTypeEnum.ExemptDuty);
		alert.Clear();
	}
	

	
	// / <summary>
	// / Convert the enumerated type from the KMB representation to the
	// CalcEngine
	// / representation
	// / </summary>
	private com.jjkeller.kmbapi.calcengine.Enums.DutyStatusEnum Convert(com.jjkeller.kmbapi.enums.DutyStatusEnum dutyStatusToConvert) {
		com.jjkeller.kmbapi.calcengine.Enums.DutyStatusEnum dutyStatus = com.jjkeller.kmbapi.calcengine.Enums.DutyStatusEnum.OFF;
		switch (dutyStatusToConvert.getValue()) {
		case com.jjkeller.kmbapi.enums.DutyStatusEnum.OFFDUTY:
			dutyStatus = com.jjkeller.kmbapi.calcengine.Enums.DutyStatusEnum.OFF;
			break;
		case com.jjkeller.kmbapi.enums.DutyStatusEnum.SLEEPER:
			dutyStatus = com.jjkeller.kmbapi.calcengine.Enums.DutyStatusEnum.SLP;
			break;
		case com.jjkeller.kmbapi.enums.DutyStatusEnum.DRIVING:
			dutyStatus = com.jjkeller.kmbapi.calcengine.Enums.DutyStatusEnum.DRV;
			break;
		case com.jjkeller.kmbapi.enums.DutyStatusEnum.ONDUTY:
			dutyStatus = com.jjkeller.kmbapi.calcengine.Enums.DutyStatusEnum.ON;
			break;
		case com.jjkeller.kmbapi.enums.DutyStatusEnum.OFFDUTYWELLSITE:
			dutyStatus = com.jjkeller.kmbapi.calcengine.Enums.DutyStatusEnum.OFFWLLST;
			break;
		}

		return dutyStatus;
	}

	// / <summary>
	// / Convert the enumerated type from the KMB representation to the
	// CalcEngine
	// / representation
	// / </summary>
	private com.jjkeller.kmbapi.calcengine.Enums.RuleSetTypeEnum Convert(com.jjkeller.kmbapi.enums.RuleSetTypeEnum rulesetToConvert) {
		com.jjkeller.kmbapi.calcengine.Enums.RuleSetTypeEnum ruleset = com.jjkeller.kmbapi.calcengine.Enums.RuleSetTypeEnum.US60Hour;
		switch (rulesetToConvert.getValue()) {
		case com.jjkeller.kmbapi.enums.RuleSetTypeEnum.ALASKA_7DAY:
			ruleset = com.jjkeller.kmbapi.calcengine.Enums.RuleSetTypeEnum.Alaska_7Day;
			break;
		case com.jjkeller.kmbapi.enums.RuleSetTypeEnum.ALASKA_8DAY:
			ruleset = com.jjkeller.kmbapi.calcengine.Enums.RuleSetTypeEnum.Alaska_8Day;
			break;
		case com.jjkeller.kmbapi.enums.RuleSetTypeEnum.US60HOUR:
			ruleset = com.jjkeller.kmbapi.calcengine.Enums.RuleSetTypeEnum.US60Hour;
			break;
		case com.jjkeller.kmbapi.enums.RuleSetTypeEnum.US70HOUR:
			ruleset = com.jjkeller.kmbapi.calcengine.Enums.RuleSetTypeEnum.US70Hour;
			break;
		case com.jjkeller.kmbapi.enums.RuleSetTypeEnum.CANADIAN_CYCLE1:
			ruleset = com.jjkeller.kmbapi.calcengine.Enums.RuleSetTypeEnum.Canadian_Cycle1;
			break;
		case com.jjkeller.kmbapi.enums.RuleSetTypeEnum.CANADIAN_CYCLE2:
			ruleset = com.jjkeller.kmbapi.calcengine.Enums.RuleSetTypeEnum.Canadian_Cycle2;
			break;
		case com.jjkeller.kmbapi.enums.RuleSetTypeEnum.FLORIDA_7DAY:
			ruleset = com.jjkeller.kmbapi.calcengine.Enums.RuleSetTypeEnum.Florida_7Day;
			break;
		case com.jjkeller.kmbapi.enums.RuleSetTypeEnum.FLORIDA_8DAY:
			ruleset = com.jjkeller.kmbapi.calcengine.Enums.RuleSetTypeEnum.Florida_8Day;
			break;
		case com.jjkeller.kmbapi.enums.RuleSetTypeEnum.WISCONSIN_7DAY:
			ruleset = com.jjkeller.kmbapi.calcengine.Enums.RuleSetTypeEnum.Wisconsin_7Day;
			break;
		case com.jjkeller.kmbapi.enums.RuleSetTypeEnum.WISCONSIN_8DAY:
			ruleset = com.jjkeller.kmbapi.calcengine.Enums.RuleSetTypeEnum.Wisconsin_8Day;
			break;
		case com.jjkeller.kmbapi.enums.RuleSetTypeEnum.TEXAS:
			ruleset = com.jjkeller.kmbapi.calcengine.Enums.RuleSetTypeEnum.Texas;
			break;
		case com.jjkeller.kmbapi.enums.RuleSetTypeEnum.USOILFIELD:
			ruleset = com.jjkeller.kmbapi.calcengine.Enums.RuleSetTypeEnum.USOilField;
			break;
		case com.jjkeller.kmbapi.enums.RuleSetTypeEnum.TEXASOILFIELD:
			ruleset = com.jjkeller.kmbapi.calcengine.Enums.RuleSetTypeEnum.TexasOilField;
			break;
		case com.jjkeller.kmbapi.enums.RuleSetTypeEnum.USMOTIONPICTURE_7DAY:
			ruleset = com.jjkeller.kmbapi.calcengine.Enums.RuleSetTypeEnum.USMotionPicture_7Day;
			break;
		case com.jjkeller.kmbapi.enums.RuleSetTypeEnum.USMOTIONPICTURE_8DAY:
			ruleset = com.jjkeller.kmbapi.calcengine.Enums.RuleSetTypeEnum.USMotionPicture_8Day;
			break;
		case com.jjkeller.kmbapi.enums.RuleSetTypeEnum.CALIFORNIA_MP_80:
			ruleset = com.jjkeller.kmbapi.calcengine.Enums.RuleSetTypeEnum.California_MP_80;
			break;
		}

		return ruleset;
	}

	private static boolean _forceServerUpdate = false;
	/**
	 * Triggers an available hours update on the next check
	 */
	public void ForceServerUpdate()
	{
		_forceServerUpdate = true;
	}
	
	/**
	 * Returns true if the timestamp indicates that it is time to perform a server update
	 * @param timestamp The timestamp to check
	 * @return True if an update should be performed and false otherwise
	 */
	private boolean ShouldGenerateServerUpdate(Date timestamp) {
		if (!this.getIsEnabled())
			return false;
		
		boolean shouldProcess = false;
		
		if(_forceServerUpdate)
			shouldProcess = true;
		else
		{
			Date nextTimestamp = this.getNextServerUpdateTimestamp();
			if (nextTimestamp == null)
				shouldProcess = true;
			else
			{
				// should only generate summaries if the timestamp has expired
				shouldProcess = nextTimestamp.compareTo(timestamp) <= 0;
			}
		}

		// if the network is unavailable, then do not process this request
		if (shouldProcess && !this.getIsNetworkAvailable())
			shouldProcess = false;

		//only clear this flag if we're updating now
		//otherwise try again next time
		if(shouldProcess)
			_forceServerUpdate = false;
		
		return shouldProcess;
	}

	private static class PerformServerUpdateTask extends AsyncTask<Void, Void, Void> {
		private final WeakReference<Context> context;

		public PerformServerUpdateTask(Context context) {
			this.context = new WeakReference<Context>(context);
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				new HosAuditController(context.get()).PerformServerUpdate();
			} finally {
			}
			return null;
		}

	}
	
	public void HandleStartupCompletion() {
		EmployeeLog currentLog = GlobalState.getInstance().getCurrentEmployeeLog();
		boolean isCurrentLogConvertedFromExemptToGrid = false;
		
		// Need to get the ExemptLogtype of the currentLog, Need this to create the proper validator
		// If the currentLog is converted to a Grid Log then the exemptLogTypeEnum = NULL and the wrong Validator can be created. 
		ExemptLogTypeEnum exemptLogType = currentLog.getExemptLogType();
		
		if(currentLog.getExemptLogType().getValue() != ExemptLogTypeEnum.NULL)
		{
			IAPIController logController = MandateObjectFactory.getInstance(this.getContext(),GlobalState.getInstance().getFeatureService()).getCurrentEventController();
			isCurrentLogConvertedFromExemptToGrid = logController.TransitionExemptLogToStandardLogIfNecessary(currentLog, true);
			
			if(currentLog.getExemptLogType().getValue() == ExemptLogTypeEnum.EXEMPTLOGTYPE100AIRMILE)
			{
				//for 100 air mile exempt logs, at system startup time determine how much time is left for the day
				EmployeeLogEldEvent lastEvent = EmployeeLogUtilities.GetLastEventInLog(currentLog);
				
				if(lastEvent.isExemptOnDutyStatus())
					MarkBeginningOfExemptDutyPeriod();
				if(lastEvent.isExemptOffDutyStatus())
					MarkEndOfExemptDutyPeriod();
			}
		}
		
		if(this.getCurrentUser().getIsMobileExemptLogAllowed())
		{ 
			// since the user is allowed to claim exempt logs, validate old logs
			// note: it may be possible that today's log is Gridded and not exempt, but previous logs may be affected by what happens on today's log
			
			//first, determine if any prior log are no longer exempt eligible 
			ExemptLogValidationController exmptCtrlr = new ExemptLogValidationController(this.getContext());
			List<Date> listOfIneligibleLogs = exmptCtrlr.ValidatePreviousLogsForCurrentLogs(currentLog, exemptLogType); 
			
			// now, try to transition each of those found to a grid log 
			IAPIController logCtrlr = MandateObjectFactory.getInstance(this.getContext(),GlobalState.getInstance().getFeatureService()).getCurrentEventController();
			logCtrlr.TransitionPreviousLogsFromExemptToGridLog(listOfIneligibleLogs);

			//	lastly notify the user
			logCtrlr.NotifyAboutTransitionCurrentLogFromExemptToGrid(currentLog, isCurrentLogConvertedFromExemptToGrid, listOfIneligibleLogs);
			
		}
	}

}
