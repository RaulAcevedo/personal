package com.jjkeller.kmbapi.controller.utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.jjkeller.kmbapi.R;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.EOBR.EobrReader;
import com.jjkeller.kmbapi.controller.EmployeeLogWithProvisionsController;
import com.jjkeller.kmbapi.controller.LogEntryController;
import com.jjkeller.kmbapi.controller.dataaccess.EmployeeLogEldEventFacade;
import com.jjkeller.kmbapi.controller.dataaccess.EmployeeLogFacade;
import com.jjkeller.kmbapi.controller.interfaces.IAPIController;
import com.jjkeller.kmbapi.controller.interfaces.IEventHandler;
import com.jjkeller.kmbapi.controller.interfaces.IReverseGeocodeLocationListener;
import com.jjkeller.kmbapi.controller.share.ExemptLogConvertedToGridLogEventArgs;
import com.jjkeller.kmbapi.controller.share.ExemptLogPreviousLogsConvertedToGridLogEventArgs;
import com.jjkeller.kmbapi.controller.share.MandateObjectFactory;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.employeelogeldevents.EmployeeLogEldEventCode;
import com.jjkeller.kmbapi.employeelogeldevents.Enums;
import com.jjkeller.kmbapi.enums.CanadaDeferralTypeEnum;
import com.jjkeller.kmbapi.enums.DutyStatusEnum;
import com.jjkeller.kmbapi.enums.EmployeeLogProvisionTypeEnum;
import com.jjkeller.kmbapi.enums.EnumBase;
import com.jjkeller.kmbapi.enums.FailureCategoryEnum;
import com.jjkeller.kmbapi.enums.RuleSetTypeEnum;
import com.jjkeller.kmbapi.enums.TimeZoneEnum;
import com.jjkeller.kmbapi.proxydata.EldEventAdapterList;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEventList;
import com.jjkeller.kmbapi.proxydata.EmployeeLogWithProvisions;
import com.jjkeller.kmbapi.proxydata.FailureReport;
import com.jjkeller.kmbapi.proxydata.FailureReportList;
import com.jjkeller.kmbapi.proxydata.GpsLocation;
import com.jjkeller.kmbapi.proxydata.Location;
import com.jjkeller.kmbapi.proxydata.TeamDriver;
import com.jjkeller.kmbapi.proxydata.compare.EmployeeLogEldEventDateComparator;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.jjkeller.kmbapi.configuration.GlobalState.getContext;

public class EmployeeLogUtilities {
    public static final int CONTINUE_IN_DRIVING_STATUS_PROMPT_TIMEOUT = 60000;

    private static final int TWENTY_FOUR_HOUR_PERIOD = 1440;
    private static final int ONE_HOUR_PERIOD = 60;


    private static EmployeeLog createNewLog(Context ctx, User user, Date now) {

        // log date needs the time component removed
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(user.getHomeTerminalTimeZone().toTimeZone());
        cal.setTime(now);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date logDate = cal.getTime();

        EmployeeLog empLog = new EmployeeLog();
        empLog.setEmployeeId(user.getCredentials().getEmployeeId());
        empLog.setLogDate(logDate);
        empLog.setDriverType(user.getDriverType());
        empLog.setRuleset(user.getRulesetTypeEnum());

        if (user.getHomeTerminalTimeZone().getValue() != TimeZoneEnum.NULL) {
            empLog.setTimezone(user.getHomeTerminalTimeZone());
        }
        empLog.getCanadaDeferralType().setValue(CanadaDeferralTypeEnum.NONE);
        if (user.getRulesetTypeEnum().isCanadianRuleset()) {
            // when under canadian rules
            empLog.setHasReturnedToLocation(false);
            empLog.setIsHaulingExplosives(false);
            empLog.setIsOperatesSpecificVehiclesForOilfield(false);
        } else if (user.getRulesetTypeEnum().isAnyOilFieldRuleset()) {
            empLog.setHasReturnedToLocation(false);
            empLog.setIsHaulingExplosives(false);
            empLog.setIsOperatesSpecificVehiclesForOilfield(user.getIsOperatesSpecificVehiclesForOilfield());
        } else {
            // must be under US rules
            empLog.setHasReturnedToLocation(user.getIsShorthaulException());
            if (user.getIsHaulingExplosivesAllowed()) {
                empLog.setIsHaulingExplosives(user.getIsHaulingExplosivesDefault());
            } else {
                empLog.setIsHaulingExplosives(false);
            }
        }

        // set default Trailer and Shipment Info if there
        if (GlobalState.getInstance().getFeatureService().getDefaultTripInformation()) {
            // Feature Toggle is on
            SharedPreferences userPref = ctx.getSharedPreferences(ctx.getString(R.string.sharedpreferencefile), 0);
            if (userPref.getBoolean(ctx.getString(R.string.setdefaulttrailernumber), false)) {
                empLog.setTrailerNumbers(userPref.getString(ctx.getString(R.string.defaulttrailernumber), ""));
                empLog.setTrailerPlate(userPref.getString(ctx.getString(R.string.defaulttrailerplate), ""));
            }
            if (userPref.getBoolean(ctx.getString(R.string.setdefaultshipmentnumber), false)) {
                empLog.setShipmentInformation(userPref.getString(ctx.getString(R.string.defaultshipmentnumber), ""));
            }
        }
        return empLog;
    }

    /// <summary>
    /// Create a new log for the user, given the current clock time.
    /// This will initialize the log with a 24-hour event using the initial
    /// duty status value passed in
    /// </summary>
    /// <param name="user">user to create the log for</param>
    /// <param name="now">current time now, in home terminal time zone units</param>
    /// <param name="initialDutyStatus">duty status that the 24-hour event is created with</param>
    /// <param name="initialLocation">initial location for the log</param>
    /// <returns></returns>
    public static EmployeeLog CreateNewLog(Context ctx, User user, Date now,
                                           DutyStatusEnum initialDutyStatus,
                                           Location initialLocation) {
        return CreateNewLog(ctx, user, now, initialDutyStatus, initialLocation, null);
    }

    /// <summary>
    /// Create a new log for the user, given the current clock time.
    /// This will initialize the log with a 24-hour event using the initial
    /// duty status value passed in
    /// </summary>
    /// <param name="user">user to create the log for</param>
    /// <param name="now">current time now, in home terminal time zone units</param>
    /// <param name="initialDutyStatus">duty status that the 24-hour event is created with</param>
    /// <param name="initialLocation">initial location for the log</param>
    /// <returns></returns>
    public static EmployeeLog CreateNewLog(Context ctx, User user, Date now,
                                           DutyStatusEnum initialDutyStatus,
                                           Location initialLocation, Integer recordOrigin) {

        EmployeeLog empLog = createNewLog(ctx, user, now);
        /*
        NOTE: This method is used to create new logs in various contexts, as such we are passing in the
        duty status to assign initially. For example, if this log is created as a transitional log,
        we need the last duty status from the log to be transitioned.
         */
        String motionPictureAuthorityId = null, motionPictureProductionId = null;
        IAPIController eventController = MandateObjectFactory.getInstance(ctx, GlobalState.getInstance().getFeatureService()).getCurrentEventController();

        //Add Motion picture authority and production for transition events
        if (GlobalState.getInstance().getCompanyConfigSettings(ctx).getIsMotionPictureEnabled()) {
            motionPictureAuthorityId = GlobalState.getInstance().get_currentMotionPictureAuthorityId();
            motionPictureProductionId = GlobalState.getInstance().get_currentMotionPictureProductionId();
        }

        eventController.CreateInitialEventForLog(empLog, initialDutyStatus, now, user, initialLocation, user.getRulesetTypeEnum(), motionPictureProductionId, motionPictureAuthorityId);

        if(recordOrigin != null) {
            EmployeeLogEldEvent initialEvent = empLog.getEldEventList().getEldEventList(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus)[0];
            initialEvent.setEventRecordOrigin(recordOrigin);
        }
        empLog.setTimeSyncFailureList(new FailureReportList());
        empLog.setEobrFailureList(new FailureReportList());

        return empLog;
    }

    public static EmployeeLog CreateNewLog(Context ctx, User user, Date now, EmployeeLogEldEvent initialEvent) {

        EmployeeLog empLog = createNewLog(ctx, user, now);
        EmployeeLogFacade facade = new EmployeeLogFacade(ctx, user);
        facade.Save(empLog, 1);

        if (initialEvent != null) {
            initialEvent.setPrimaryKey(-1);
            initialEvent.setLogKey((int) empLog.getPrimaryKey());
            empLog.getEldEventList().setEldEventList(new EmployeeLogEldEvent[]{initialEvent});
        }

        empLog.setTimeSyncFailureList(new FailureReportList());
        empLog.setEobrFailureList(new FailureReportList());

        facade.Save(empLog, 1);

        return empLog;
    }

    /// Answer the timestamp of the start of the log.
    /// This timestamp represents the log date plus the
    /// daily log start time.
    public static Date CalculateLogStartTime(Context ctx, Date logDate, TimeZoneEnum homeTerminalTimeZone) {

        String dailyLogStartTime = GlobalState.getInstance().getCompanyConfigSettings(ctx).getDailyLogStartTime();
        return CalculateLogStartTime(dailyLogStartTime, logDate, homeTerminalTimeZone);
    }

    /**
     * Answer the timestamp of the start of the log.
     * This timestamp represents the log date plus the
     * daily log start time.
     *
     * @param dailyLogStartTime
     * @param logDate
     * @param homeTerminalTimeZone
     * @return
     */
    public static Date CalculateLogStartTime(String dailyLogStartTime, Date logDate, TimeZoneEnum homeTerminalTimeZone) {
        TimeZone tz = homeTerminalTimeZone.toTimeZone();

        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(tz);
        cal.setTime(logDate);

        String[] timeParse = dailyLogStartTime.split(":");

        if (timeParse.length > 2) {
            cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeParse[0]));
            cal.set(Calendar.MINUTE, Integer.parseInt(timeParse[1]));
            cal.set(Calendar.SECOND, Integer.parseInt(timeParse[2]));
        } else if (timeParse.length > 1) {
            cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeParse[0]));
            cal.set(Calendar.MINUTE, Integer.parseInt(timeParse[1]));
            cal.set(Calendar.SECOND, 0);
        } else if (timeParse.length > 0) {
            cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeParse[0]));
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
        } else {
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
        }
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTime();
    }

    /**
     * Answer the timestamp of the end of the log.
     * This timestamp represents the log date plus the
     * daily log start time.
     *
     * @param dailyLogStartTime
     * @param logDate
     * @param homeTerminalTimeZone
     * @return
     */
    public static Date CalculateLogEndTime(String dailyLogStartTime, Date logDate, TimeZoneEnum homeTerminalTimeZone) {
        TimeZone tz = homeTerminalTimeZone.toTimeZone();

        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(tz);
        cal.setTime(logDate);

        cal.add(Calendar.DATE, 1);
        Date tomorrowsStartTime = CalculateLogStartTime(dailyLogStartTime, cal.getTime(), homeTerminalTimeZone);

        cal.setTime(tomorrowsStartTime);
        cal.add(Calendar.SECOND, -1);

        return cal.getTime();
    }

    /// <summary>
    /// Answer the log from the list with the given log date
    /// </summary>
    /// <param name="logDate">date to search for</param>
    /// <param name="logList">list of logs to search</param>
    public static EmployeeLog FindLog(Date logDate, List<EmployeeLog> logList) {
        EmployeeLog answer = null;
        if (logList != null) {
            for (EmployeeLog empLog : logList) {
                if (logDate.equals(empLog.getLogDate())) {
                    // found a log matching the date
                    answer = empLog;
                    break;
                }
            }
        }
        return answer;
    }

    /// <summary>
    /// Answer the last chronological event in the log.
    /// </summary>
    /// <param name="empLog"></param>
    /// <returns></returns>
    public static EmployeeLogEldEvent GetLastEventInLog(EmployeeLog empLog) {
        return GetLastEventInLog(EldEventAdapterList.ListAccessorModifierEnum.ALL, empLog);
    }

    public static EmployeeLogEldEvent GetLastEventInLog(EmployeeLog empLog, Enums.EmployeeLogEldEventType eventType) {
        return GetLastEventInLog(EldEventAdapterList.ListAccessorModifierEnum.ALL, empLog, eventType);
    }

    public static EmployeeLogEldEvent GetLogEventForEdit(EmployeeLog log) {
        return GetLogEventForEdit(log, false);
    }

    public static EmployeeLogEldEvent GetLogEventForEdit(EmployeeLog log, boolean forceDBRead) {
        if (GlobalState.getInstance().getLogEventForEdit() != null && !forceDBRead) {
            return GlobalState.getInstance().getLogEventForEdit();
        } else {
            return EmployeeLogUtilities.GetLastEventInLog(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus, log);
        }
    }

    public static EmployeeLogEldEvent GetLastEventInLog(EldEventAdapterList.ListAccessorModifierEnum listAccessorModifierEnum, EmployeeLog empLog) {
        EmployeeLogEldEvent lastEvent = null;
        EmployeeLogEldEvent[] logEventList = empLog.getEldEventList().getActiveEldEventList(listAccessorModifierEnum);
        if (logEventList.length > 0) {
            // The events are sorted chronologically, so get the last one
            int lastIndex = logEventList.length - 1;
            lastEvent = logEventList[lastIndex];
        }
        return lastEvent;
    }

    public static EmployeeLogEldEvent GetLastEventInLog(EldEventAdapterList.ListAccessorModifierEnum listAccessorModifierEnum, EmployeeLog empLog, Enums.EmployeeLogEldEventType eventType) {
        EmployeeLogEldEvent lastEvent = null;
        EmployeeLogEldEvent[] logEventList = empLog.getEldEventList().getActiveEldEventList(listAccessorModifierEnum);

        if (logEventList.length > 0) {
            int lastIndex = logEventList.length - 1;

            if (eventType == null) {
                // The events are sorted chronologically, so get the last one
                lastEvent = logEventList[lastIndex];
            } else {
                for (int i = lastIndex; i >= 0; i--) {
                    if (logEventList[i].getEventType() == eventType) {
                        lastEvent = logEventList[i];
                        break;
                    }
                }
            }
        }

        return lastEvent;
    }

    public static EmployeeLogEldEvent getNextActiveEventAfterDate(List<EmployeeLogEldEvent> eldEventList, Date eldRequestedTime) {
        return getNextEventAfterDate(eldEventList, eldRequestedTime, Arrays.asList(Enums.EmployeeLogEldEventRecordStatus.Active.getValue()));
    }

    public static EmployeeLogEldEvent getNextEventAfterDate(List<EmployeeLogEldEvent> eldEventList, Date eldRequestedTime, List<Integer> eventRecordStatuses) {
        if (eldEventList == null || eldEventList.size() == 0) {
            return null;
        }
        Collections.sort(eldEventList, new EmployeeLogEldEventDateComparator());
        for (int i = 0; i < eldEventList.size(); i++) {
            EmployeeLogEldEvent eldEvent = eldEventList.get(i);
            if (eldEvent.getEventDateTime().after(eldRequestedTime) && eldEvent.getEventRecordStatus() != null) {
                for (int eventRecordStatus : eventRecordStatuses) {
                    if (eldEvent.getEventRecordStatus() == eventRecordStatus) {
                        return eldEvent;
                    }
                }
            }
        }
        return null;
    }


    public static EmployeeLogEldEvent getLastActiveDutyStatusChange(EmployeeLog log) {
        List<EmployeeLogEldEvent> activeDutyStatusChanges = Arrays.asList(log.getEldEventList().getActiveEldEventList(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus));
        EmployeeLogEldEvent returnValue = null;
        if (activeDutyStatusChanges == null || activeDutyStatusChanges.size() == 0) {
            return null;
        }
        Collections.sort(activeDutyStatusChanges, new EmployeeLogEldEventDateComparator());
        return activeDutyStatusChanges.get(activeDutyStatusChanges.size() - 1);
    }

    public static Date GetMostRecentLoginEventTimeForToday(Context context, EmployeeLog log) {
        Date loginEventTime = null;
        EmployeeLogEldEvent[] nonDutyStatusEventsForToday = log.getEldEventList().getActiveEldEventList(EldEventAdapterList.ListAccessorModifierEnum.NonDutyStatus);
        if (nonDutyStatusEventsForToday.length > 0) {
            // The events are sorted in reverse, so get the first one that is a Login Event
            Collections.reverse(Arrays.asList(nonDutyStatusEventsForToday));
            for (EmployeeLogEldEvent evt : nonDutyStatusEventsForToday) {
                if (evt.getEventType() == Enums.EmployeeLogEldEventType.LoginLogout && evt.getEventCode() == EmployeeLogEldEventCode.Login) {
                    loginEventTime = evt.getStartTime();
                    break;

                }
            }
        }

        //check to see if there's a login event that isn't associated with the log yet
        //(the tie is made at several different points in the login process depending on
        // exempt logs, team driver etc)
        IAPIController controller = MandateObjectFactory.getInstance(context, GlobalState.getInstance().getFeatureService()).getCurrentEventController();
        EmployeeLogEldEvent event = null;
        try {
            event = controller.GetMostRecentLoginEvent(log);
        } catch (Exception ex) {
            Log.e("UnhandledCatch", ex.getMessage(), ex);
        }

        if (event != null && (loginEventTime == null || event.getEventDateTime().compareTo(loginEventTime) > 0)) {
            loginEventTime = event.getEventDateTime();
        }

        return loginEventTime;
    }

    public static EmployeeLogEldEvent GetLastEventInLogWithMotionPictureInfo(EmployeeLog empLog) {
        EmployeeLogEldEvent[] logEventList = empLog.getEldEventList().getEldEventList();
        EmployeeLogEldEvent retEldEvent = null;
        if (logEventList != null) {
            for (int i = logEventList.length - 1; i > -1; i--) {
                if (logEventList[i].getMotionPictureProductionId() != null) {
                    retEldEvent = logEventList[i];
                    break;
                }
            }
        }
        return retEldEvent;
    }

    public static EmployeeLogEldEvent GetLastYMEventInLog(EldEventAdapterList.ListAccessorModifierEnum listAccessorModifierEnum, EmployeeLog empLog) {
        EmployeeLogEldEvent lastYMEvent = null;
        EmployeeLogEldEvent[] logEventList = empLog.getEldEventList().getEldEventList(listAccessorModifierEnum);
        ArrayList<EmployeeLogEldEvent> yardMoveList = new ArrayList<EmployeeLogEldEvent>();
        for (EmployeeLogEldEvent event : logEventList) {
            if (event.getEventType() == Enums.EmployeeLogEldEventType.ChangeInDriversIndication && event.getEventCode() == 2) {

                yardMoveList.add(event);

            }
            boolean hasNonTerminatedYardMoveEvent = yardMoveList.size() % 2 != 0;
            if (!yardMoveList.isEmpty() && yardMoveList.size() > 0 && hasNonTerminatedYardMoveEvent) {
                lastYMEvent = yardMoveList.get(0);
            }
        }
        return lastYMEvent;
    }

    /// <summary>
    /// Answer the second to last chronological event in the log.
    /// This is the event just prior to the last event.
    /// </summary>
    /// <param name="empLog"></param>
    /// <returns></returns>
    public static EmployeeLogEldEvent GetSecondToLastEventInLog(EmployeeLog empLog) {
        return GetSecondToLastEventInLog(EldEventAdapterList.ListAccessorModifierEnum.ALL, empLog);
    }

    public static EmployeeLogEldEvent GetSecondToLastEventInLog(EldEventAdapterList.ListAccessorModifierEnum listAccessorModifierEnum, EmployeeLog empLog) {
        EmployeeLogEldEvent lastEvent = null;
        EmployeeLogEldEventList logEventList = empLog.getEldEventList();
        if (logEventList.getEldEventList(listAccessorModifierEnum).length > 1) {
            // there is at least one event in the log
            // since the events in the log are stored chronologically,
            // directly index the second to last one
            int lastIndex = logEventList.getEldEventList(listAccessorModifierEnum).length - 2;
            lastEvent = logEventList.getEldEventList(listAccessorModifierEnum)[lastIndex];
        }
        return lastEvent;
    }

    /// <summary>
    /// Answer the first chronological event in the log.
    /// </summary>
    /// <param name="empLog"></param>
    /// <returns></returns>
    public static EmployeeLogEldEvent GetFirstEventInLog(EmployeeLog empLog) {
        return GetFirstEventInLog(EldEventAdapterList.ListAccessorModifierEnum.ALL, empLog);
    }

    public static EmployeeLogEldEvent GetFirstEventInLog(EldEventAdapterList.ListAccessorModifierEnum listAccessorModifierEnum, EmployeeLog empLog) {
        EmployeeLogEldEvent firstEvent = null;
        EmployeeLogEldEventList logEventList = empLog.getEldEventList();
        if (logEventList.getEldEventList(listAccessorModifierEnum).length > 0) {
            // The events are sorted chronologically, so get the first one
            firstEvent = logEventList.getEldEventList(listAccessorModifierEnum)[0];
        }
        return firstEvent;
    }

    /// <summary>
    /// Remove all log events from the log that occur after the timestamp.
    /// If an event matches the timestamp, it is not removed.
    /// Answer if any events were removed from the log.
    /// </summary>
    public static boolean RemoveAllEventsAfter(EmployeeLog empLog, Date timestamp) {
        return RemoveAllEventsAfter(empLog, timestamp, false);
    }

    /// <summary>
    /// Remove all log events from the log that occur after the timestamp.
    /// If an event matches the timestamp, it is not removed.
    /// Answer if any events were removed from the log.
    /// </summary>
    public static boolean RemoveAllEventsAfter(EmployeeLog empLog, Date timestamp, boolean shouldRemoveValidatedStartTime) {
        return RemoveAllEventsAfter(EldEventAdapterList.ListAccessorModifierEnum.ALL, empLog, timestamp, shouldRemoveValidatedStartTime);
    }

    public static boolean RemoveAllEventsAfter(EldEventAdapterList.ListAccessorModifierEnum listAccessorModifierEnum, EmployeeLog empLog, Date timestamp, boolean shouldRemoveValidatedStartTime) {
        boolean changesMade = false;
        EmployeeLogEldEvent[] logEventList = empLog.getEldEventList().getEldEventList(listAccessorModifierEnum);
        if (logEventList.length > 0) {
            // there are events to work with
            // create the new log event list.
            // all event that should remain on the log will be copied into here
            ArrayList<EmployeeLogEldEvent> newList = new ArrayList<>(logEventList.length);

            // look at each event in the list, backwards
            for (int index = logEventList.length - 1; index >= 0; index--) {
                EmployeeLogEldEvent evt = logEventList[index];
                boolean shouldAddEvent = false;

                if (evt.getStartTime() != null && evt.getStartTime().compareTo(timestamp) <= 0) {
                    // found an event that happened before the timestamp, so add the event
                    shouldAddEvent = true;
                }

                if (evt.getIsStartTimeValidated() && !shouldRemoveValidatedStartTime) {
                    shouldAddEvent = true;
                }

                if (shouldAddEvent) {
                    newList.add(evt);
                }
            }

            changesMade = newList.size() != logEventList.length;
            if (changesMade) {
                Collections.sort(newList, new GenericEventComparer());

                // put the event list back in the log
                empLog.getEldEventList().setEldEventList(newList.toArray(new EmployeeLogEldEvent[newList.size()]));
            }
        }
        return changesMade;
    }

    /// <summary>
    /// Answer the last chronological event in the log that occurs immediately
    /// prior to the timestamp
    /// </summary>
    /// <returns>LogEvent that occurs just before the timestamp</returns>
    public static EmployeeLogEldEvent GetEventPriorToTime(EmployeeLog empLog, Date timestamp) {
        return GetEventPriorToTime(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus, empLog, timestamp);
    }

    public static EmployeeLogEldEvent GetEventPriorToTime(EldEventAdapterList.ListAccessorModifierEnum listAccessorModifierEnum, EmployeeLog empLog, Date timestamp) {
        EmployeeLogEldEvent lastEvent = null;
        EmployeeLogEldEvent[] logEventList = empLog.getEldEventList().getEldEventList(listAccessorModifierEnum);

        // 4/12/11 JHM - Added "timestamp != null" check.  Was causing an error (Defect 9884) when
        // downloading logs after submit. MobileLogStartTimestamp wasn't set and the compareTo was failing when
        // timestamp was null. This is likely a side effect of our Date defaults being null and not a MinValue.
        if (logEventList.length > 0 && timestamp != null) {
            // look at each event
            for (int index = 0; index < logEventList.length; index++) {
                EmployeeLogEldEvent evt = logEventList[index];
                if (evt.getStartTime() != null && evt.getStartTime().compareTo(timestamp) < 0) {
                    // this event occurred before the timestamp, so remember this event in case it's the last one
                    lastEvent = evt;
                } else {
                    // this event ocurred after the timestamp, so we're done searching
                    break;
                }
            }
        }
        return lastEvent;
    }


    public static EmployeeLogEldEvent AddEventToEndOfLog(EmployeeLog empLog, Date timestamp, DutyStatusEnum dutyStatus, Location location, boolean isStartTimeValidated, RuleSetTypeEnum ruleset, String logRemark, Date logRemarkDate, String motionPictureProductionId, String motionPictureAuthorityId) {
        //Since this is used within Mandate code, get ALL events
        return AddEventToLog(empLog, timestamp, dutyStatus, location, isStartTimeValidated, ruleset, logRemark, logRemarkDate, true, EobrReader.getInstance().getEobrSerialNumber(), motionPictureProductionId, motionPictureAuthorityId);
    }

    /// <summary>
    /// Add an event to the log.
    /// Keep the list of log events sorted by start time of the event.
    /// If there is already an event defined at that time in the log,
    /// the existing event is first removed, then the new event is added.
    /// If the last event on the log has the same dutyStatus as the one being
    /// added, then don't add the new event.
    /// </summary>
    /// <returns>the new Log Event created</returns>
    public static EmployeeLogEldEvent AddEventToLog(EmployeeLog empLog, Date timestamp, DutyStatusEnum dutyStatus, Location location, boolean isStartTimeValidated, RuleSetTypeEnum ruleset, String logRemark, Date logRemarkDate) {
        return AddEventToLog(empLog, timestamp, dutyStatus, location, isStartTimeValidated, ruleset, logRemark, logRemarkDate, false, EobrReader.getInstance().getEobrSerialNumber(), null, null);

    }

    public static EmployeeLogEldEvent AddEventToLog(EmployeeLog empLog, Date timestamp, DutyStatusEnum dutyStatus, Location location, boolean isStartTimeValidated, RuleSetTypeEnum ruleset, String logRemark, Date logRemarkDate, String motionPictureProductionId, String motionPictureAuthorityId) {
        return AddEventToLog(empLog, timestamp, dutyStatus, location, isStartTimeValidated, ruleset, logRemark, logRemarkDate, false, EobrReader.getInstance().getEobrSerialNumber(), motionPictureProductionId, motionPictureAuthorityId);
    }

    private static EmployeeLogEldEvent AddEventToLog(EmployeeLog empLog, Date timestamp, DutyStatusEnum dutyStatus, Location location, boolean isStartTimeValidated, RuleSetTypeEnum ruleset, String logRemark, Date logRemarkDate, boolean forceAddAtEndOfLog, String eobrSerialNumber, String motionPictureProductionId, String motionPictureAuthorityId) {
        EmployeeLogEldEventCode translatedCode = new EmployeeLogEldEventCode(EmployeeLogEldEvent.translateDutyStatusEnumToMandateStatus(dutyStatus));
        return AddEventToLog(empLog, timestamp, Enums.EmployeeLogEldEventType.DutyStatusChange, translatedCode, location, isStartTimeValidated, ruleset, logRemark, logRemarkDate, forceAddAtEndOfLog, eobrSerialNumber, motionPictureProductionId, motionPictureAuthorityId);
    }

    private static EmployeeLogEldEvent AddEventToLog(EmployeeLog empLog, Date timestamp, Enums.EmployeeLogEldEventType eventType, EmployeeLogEldEventCode employeeLogEldEventCode, Location location, boolean isStartTimeValidated, RuleSetTypeEnum ruleset, String logRemark, Date logRemarkDate, boolean forceAddAtEndOfLog, String eobrSerialNumber, String motionPictureProductionId, String motionPictureAuthorityId) {
        EmployeeLogEldEvent newEvent;
        // if no ruleset come in, then use the rule from the log
        if (ruleset == null) {
            ruleset = empLog.getRuleset();
        }

        // create the new event
        newEvent = new EmployeeLogEldEvent(timestamp, employeeLogEldEventCode, eventType);
        newEvent.setEobrSerialNumber(eobrSerialNumber);
        newEvent.setLocation(location);
        newEvent.setIsStartTimeValidated(isStartTimeValidated);
        newEvent.setRulesetType(ruleset);
        newEvent.setMotionPictureProductionId(motionPictureProductionId);
        newEvent.setMotionPictureAuthorityId(motionPictureAuthorityId);
        newEvent.setLogRemark(logRemark);
        newEvent.setLogRemarkDate(logRemarkDate);
        newEvent.setEventRecordOrigin(Enums.EmployeeLogEldEventRecordOrigin.EditedEnteredByDriver);

        return AddEventToLog(empLog, newEvent, timestamp, forceAddAtEndOfLog);
    }

    /**
     * Method designed to take a constructed new event and determine if it exists on the log.
     * Using the last event in log object, determine further if we are to add it or not
     *
     * @param empLog             log to add event to
     * @param newEvent           event to add
     * @param timestamp          Timestamp at which new event was created. Used to get last event before
     *                           that moment in time
     * @param forceAddAtEndOfLog boolean indicating if this event should be the last event
     * @return new Event
     */
    private static EmployeeLogEldEvent AddEventToLog(EmployeeLog empLog, EmployeeLogEldEvent newEvent, Date timestamp, boolean forceAddAtEndOfLog) {
        EmployeeLogEldEvent lastEvent;
        List<EmployeeLogEldEvent> newList = null;
        //If this is a specific mergeLog call, use the provided category
        if (forceAddAtEndOfLog) {
            // when adding the event to the end of the log, then get the very last event on the log
            lastEvent = GetLastEventInLog(EldEventAdapterList.ListAccessorModifierEnum.ALL, empLog);
        } else {
            // when the new event should be inserted in place, then get the event that occurs just prior to the new one
            // this is done so that we can ensure that the new event is different
            lastEvent = GetEventPriorToTime(EldEventAdapterList.ListAccessorModifierEnum.ALL, empLog, timestamp);
        }

        if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
            // first, detect if an event already exists at that time
            GenericEventComparer eventComparer = new GenericEventComparer();
            Integer existsAtIndex;
            ArrayList<EmployeeLogEldEvent> currentEldEventList = new ArrayList<>(Arrays.asList(empLog.getEldEventList().getEldEventList()));
            Collections.sort(currentEldEventList, eventComparer);
            existsAtIndex = Collections.binarySearch(currentEldEventList, newEvent, eventComparer);

            if (existsAtIndex >= 0) {
                currentEldEventList.remove((int) existsAtIndex);
                currentEldEventList.trimToSize();
                if (existsAtIndex > 0) {
                    lastEvent = currentEldEventList.get(existsAtIndex - 1);
                }
                empLog.getEldEventList().setEldEventList(currentEldEventList);
            }
            //Get all events from log after determining if duplicate exists (and conditionally adding/removing an event)
            newList = new LinkedList<>(Arrays.asList(empLog.getEldEventList().getEldEventList(EldEventAdapterList.ListAccessorModifierEnum.ALL)));
        } else {
            // On non-mandate make sure to truncate the seconds.
            newEvent.setEventDateTime(DateUtility.ConvertToPreviousLogEventTime(newEvent.getEventDateTime()));
            EmployeeLogEldEventDateComparator comparator = new EmployeeLogEldEventDateComparator();
            newList = new LinkedList<>(Arrays.asList(empLog.getEldEventList().getEldEventList(EldEventAdapterList.ListAccessorModifierEnum.ALL)));
            int eventIndex = Collections.binarySearch(newList, newEvent, comparator);

            if (eventIndex >= 0) {
                newList.remove(eventIndex);
                if (eventIndex > 0) {
                    lastEvent = (EmployeeLogEldEvent) newList.toArray()[--eventIndex];
                }
            }
        }

                /*
        Need to ensure that in the following conditional statement, if the dutyStatus comparison is
        used, that it's ONLY used on DutyStatusChange events.
        As such, it makes sense to remove the logic from the conditional and set a boolean value
        based on that criteria
         */

        //Determine if the events in question are duty status or not
        boolean lastEventIsDutyStatus = lastEvent != null && lastEvent.getEventType() == Enums.EmployeeLogEldEventType.DutyStatusChange;
        boolean newEventIsDutyStatus = newEvent.getEventType() == Enums.EmployeeLogEldEventType.DutyStatusChange;
        boolean ruleSetsAreDifferent = lastEvent == null || !lastEvent.getRulesetType().equals(newEvent.getRulesetType());
        boolean isSameDutyStatus = lastEvent != null && lastEvent.getDutyStatusEnum().equals(newEvent.getDutyStatusEnum());
        boolean isSameEventType = lastEvent != null && lastEvent.getEventType().getValue() == newEvent.getEventType().getValue();
        boolean isSameEventCode = lastEvent != null && lastEvent.getEventCode() == newEvent.getEventCode();
        boolean isSameRecordStatus = lastEvent != null && lastEvent.getEventRecordStatus() == newEvent.getEventRecordStatus();
        boolean isSameMotionPicture = (lastEvent != null && lastEvent.getMotionPictureProductionId() != null && newEvent.getMotionPictureProductionId() != null) && lastEvent.getMotionPictureProductionId().equals(newEvent.getMotionPictureProductionId());


        if (forceAddAtEndOfLog && lastEvent != null && lastEvent.getStartTime() != null && lastEvent.getStartTime().compareTo(newEvent.getStartTime()) > 0) {
            // 2014.05.01 sjn - The startTime of the new event is before the last event on the log
            //                  When this happens, the startTime of the new log event should be changed to be 1 minute after the last log event
            Date newEventTime = DateUtility.AddMinutes(lastEvent.getStartTime(), 1);
            String msg = String.format("Detected an attempt to add a log event prior to the last event, changing the new event startTime - lastEvent.dutyStatus: {%s} lastEvent.startTime {%s}, newEvent.dutyStatus {%s}, newEvent.startTime {%s}, newEventTime {%s}",
                    lastEvent.getDutyStatusEnum(),
                    lastEvent.getStartTime(),
                    newEvent.getDutyStatusEnum(),
                    newEvent.getStartTime(),
                    newEventTime);
            Log.d("LogEvent", msg);
            ErrorLogHelper.RecordMessage(msg);

            // Prevent the addition of a minute when the stop event ocurrs one minute before midnight
            // 23:59:00 to avoid logging the event the next day.
            if(DateUtility.IsSameDay(empLog.getLogDate(), newEventTime)) {
                newEvent.setStartTime(newEventTime);
            }
        }

        /*
        Add new event if:
        last event is null
        rule sets differ between last and new events
        duty status differs between last and new events
        duty status is the same and timestamps are the same (remove/replace scenario)
        if one event type ISN'T DutyStatusChange (Mandate)
         */

        /**
         *  add the new event to the list, if the new status, or ruleset, is different than old status
         */
        if (lastEvent == null || ruleSetsAreDifferent || !isSameDutyStatus || !isSameEventType || !isSameEventCode || !isSameRecordStatus || !isSameMotionPicture) {
            StringBuilder logSB = new StringBuilder();
            String logLabel;
            if (newEventIsDutyStatus) {
                logLabel = "DutyStatusEvent";
                logSB.append("EmpLogUtilities: Adding Duty Status Event %s");
            } else {
                logLabel = "NonDutyStatusEvent";
                logSB.append("EmpLogUtilities: Adding Non Duty Status Event %s");
            }

            Log.d(logLabel, String.format(logSB.toString(), newEvent.toLogString()));
            newList.add(newEvent);
        } else {
            // the new event has the same duty status as the last event in the log
            if (lastEventIsDutyStatus && lastEvent.getDutyStatusEnum().getValue() == DutyStatusEnum.DRIVING) {
                // when two driving segments are merged together (as may happen
                // when claiming an unassigned), the odometers may need to be updated

                if (newEvent.getLocation().getOdometerReading() > 0 &&
                        newEvent.getLocation().getOdometerReading() < lastEvent.getLocation().getOdometerReading()) {
                    // update that start odometer because it lower
                    lastEvent.getLocation().setOdometerReading(newEvent.getLocation().getOdometerReading());
                }
                if (newEvent.getLocation().getEndOdometerReading() > lastEvent.getLocation().getEndOdometerReading()) {
                    // update the end odometer because it
                    lastEvent.getLocation().setEndOdometerReading(newEvent.getLocation().getEndOdometerReading());
                }
            }
            newEvent = lastEvent;
        }

        empLog.getEldEventList().setEldEventList(newList.toArray(new EmployeeLogEldEvent[newList.size()]));

        // make sure that the rule of the log represents the rule
        // of the last log event
        if (newList.size() > 0) {
            empLog.setRuleset(newList.get(newList.size() - 1).getRulesetType());
        }

        return newEvent;
    }

    /// <summary>
    /// Answer the last chronological event in the log.
    /// </summary>
    /// <param name="empLog"></param>
    /// <returns></returns>
    public static EmployeeLogEldEvent UpdateEventLocationAtTime(EmployeeLog empLog, Location location, Date timestampToUpdate) {
        return UpdateEventLocationAtTime(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus, empLog, location, timestampToUpdate);
    }

    /// <summary>
    /// Answer the last chronological event in the log.
    /// </summary>
    /// <param name="empLog"></param>
    /// <returns></returns>
    public static EmployeeLogEldEvent UpdateEventLocationAtTime(EldEventAdapterList.ListAccessorModifierEnum listAccessorModifierEnum, EmployeeLog empLog, Location location, Date timestampToUpdate) {
        EmployeeLogEldEvent event = null;
        EmployeeLogEldEvent[] logEventList = empLog.getEldEventList().getEldEventList(listAccessorModifierEnum);
        for (int i = logEventList.length - 1; i >= 0; i--) {
            // if an event exists with a starttime that matches the timestampToUpdate
            // update the location
            if (logEventList[i].getStartTime() != null && logEventList[i].getStartTime().compareTo(timestampToUpdate) == 0) {
                event = logEventList[i];
                logEventList[i].setLocation(location);

                // if event being updated is not a driving event and location has not been successfully decoded, identify event as requiring manual location
                if (event.getDutyStatusEnum().getValue() != DutyStatusEnum.DRIVING && location.getGpsInfo() != null && location.getGpsInfo().getDecodedInfo() != null && location.getGpsInfo().getDecodedInfo().IsEmpty()) {
                    logEventList[i].setRequiresManualLocation(true);
                }

                break;
            }
        }

        return event;
    }


    /**
     * Remove log events that occur before the timestamp and are stored on the server.
     * If an event matches the timestamp, it is not removed.
     * If an event doesn't have a EncompassClusterPK (not on the server) it is not removed.  only for Mandate
     *
     * @param empLog Log to remove events from.
     * @param timestamp Remove events before this time.
     */
    protected static void removeEventsForMerging(EmployeeLog empLog, EmployeeLog sourceLog, Date timestamp, boolean isEldMandateEnabled) {
        EldEventAdapterList.ListAccessorModifierEnum listAccessorModifierEnum = EldEventAdapterList.ListAccessorModifierEnum.ALL;
        EmployeeLogEldEventList logEventList = empLog.getEldEventList();
        EmployeeLogEldEvent[] logEventArray = logEventList.getEldEventList(listAccessorModifierEnum);
        if (logEventArray.length == 0){
            return;
        }

        // there are events to work with

        // create the new log event list.
        // all event earlier than the timestamp will be copied into here
        ArrayList<EmployeeLogEldEvent> newList = new ArrayList<>(logEventList.getEldEventList(listAccessorModifierEnum).length);

        // look at each event
        for (int index = 0; index < logEventList.getEldEventList(listAccessorModifierEnum).length; index++) {
            EmployeeLogEldEvent evt = logEventList.getEldEventList(listAccessorModifierEnum)[index];

            //timestamp will be null during the mergeLog process for today's log if in view only mode
            //and the user has never logged into KMB proper today. if null, discard all events.
            boolean eventIsAfterTimestamp = timestamp != null && evt != null && evt.getStartTime() != null && evt.getStartTime().compareTo(timestamp) >= 0;
            boolean mandateEventIsNotUploaded = isEldMandateEnabled && evt != null && evt.getEncompassClusterPK() == 0;
            boolean mandateEventIsInactive = isEldMandateEnabled && evt != null && evt.getEventRecordStatus() != Enums.EmployeeLogEldEventRecordStatus.Active.getValue();
            boolean mandateEventIsLocalMidnightEvent = evt != null
                    && isEldMandateEnabled
                    && evt.getStartTime() != null
                    && evt.getStartTime().compareTo(timestamp) < 0
                    && evt.getStartTime().compareTo(empLog.getLogDate()) == 0               // Midnight event
                    && evt.getEncompassClusterPK() == 0                                     // Hasn't been uploaded to Encompass yet
                    && evt.getEventRecordStatus() == Enums.EmployeeLogEldEventRecordStatus.Active.getValue();

            // We want to keep the event on the local log (i.e. empLog) if its after the timestamp, inactive, or hasn't been uploaded yet
            if (eventIsAfterTimestamp || mandateEventIsNotUploaded || mandateEventIsInactive) {
                // Even if the event has passed the above checks (particularly it hasn't been uploaded) we need to keep us from adding the
                // local midnight event to the log, because the downloadedLog (sourceLog) will also have a midnight event that will be added
                // to the log later/below (avoid duplicate midnight events - Defect 64957)
                if (!mandateEventIsLocalMidnightEvent) {
                    newList.add(evt);
                }
            }
        }

        Collections.sort(newList, new GenericEventComparer());

        // put the event list back in the log
        empLog.getEldEventList().setEldEventList(newList.toArray(new EmployeeLogEldEvent[newList.size()]));

        if (isEldMandateEnabled) {
            //We need to remove events in the source log that we haven't removed in the empLog
            HashSet<Long> clusterPkInLog = new HashSet<>();
            for (EmployeeLogEldEvent event : empLog.getEldEventList().getEldEventList(listAccessorModifierEnum)) {
                clusterPkInLog.add(event.getEncompassClusterPK());
            }

            EmployeeLogEldEvent[] employeeLogEldEvents = sourceLog.getEldEventList().getEldEventList();
            ArrayList<EmployeeLogEldEvent> updatedEventList = new ArrayList<>(employeeLogEldEvents.length);
            for (EmployeeLogEldEvent event : employeeLogEldEvents) {
                boolean eventIsAfterTimestamp = timestamp != null && event != null && event.getStartTime() != null && event.getStartTime().compareTo(timestamp) >= 0;
                boolean eventIsInLog = event != null && clusterPkInLog.contains(event.getEncompassClusterPK());
                if (eventIsAfterTimestamp || !eventIsInLog) {
                    updatedEventList.add(event);
                }
            }
            sourceLog.getEldEventList().setEldEventList(updatedEventList);
        }
    }

    /// <summary>
    /// Remove the event with the given timestamp from the log.
    /// The event's startTime must exactly match the timestamp
    /// </summary>
    public static void RemoveEventFromLog(EmployeeLog empLog, Date timestamp) {
        RemoveEventFromLog(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus, empLog, timestamp);
    }

    public static void RemoveEventFromLog(EldEventAdapterList.ListAccessorModifierEnum listAccessorModifierEnum, EmployeeLog empLog, Date timestamp) {
        EmployeeLogEldEvent[] logEventList = empLog.getEldEventList().getEldEventList(listAccessorModifierEnum);
        if (logEventList.length > 0) {
            // there are events to work with

            // create the new log event list.
            // all event earlier than the timestamp will be copied into here
            ArrayList<EmployeeLogEldEvent> newList = new ArrayList<>(logEventList.length);

            // look at each event
            for (int index = 0; index < logEventList.length; index++) {
                EmployeeLogEldEvent evt = logEventList[index];
                if (evt.getStartTime() != null && evt.getStartTime().compareTo(timestamp) != 0) {
                    // as long as the event is not the one we're looking for
                    // add it to the list
                    newList.add(evt);
                }
            }
            Collections.sort(newList, new GenericEventComparer());

            // put the event list back in the log
            empLog.getEldEventList().setEldEventList(newList.toArray(new EmployeeLogEldEvent[newList.size()]));
        }
    }

    /**
     * Merge the source log into the destination.
     * 1. look at the destination log and remove all events that occur
     *   before the startup time.
     * 2. look at the source log's events, and copy all events that
     *   occur before the startup time into the destination.
     * 3. look at the team drivers and merge those

     * For merging a server log, the destination log is local and source log is from the server.

     * @param context Application context to access the database with.
     * @param destinationLog Log that we're merging into.  This will be mutated to the merged log.
     * @param sourceLog Log to merge with.
     */
    public static void mergeLog(Context context, EmployeeLog destinationLog, EmployeeLog sourceLog) {
        //Note: cutoffTime will be null if in view only mode
        //and the user has never logged into KMB proper today
        EldEventAdapterList.ListAccessorModifierEnum listAccessorModifierEnum = EldEventAdapterList.ListAccessorModifierEnum.ALL;

        Date cutoffTime;
        boolean isEldMandateEnabled = GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled();
        if (isEldMandateEnabled) {
            //The method GetMostRecentLoginEvent on the mandate controller is for a very specific situation and does not work here, so a new method was created
            cutoffTime = GetMostRecentLoginEventTimeForToday(context, destinationLog);
        } else {
            cutoffTime = destinationLog.getMobileStartTimestamp();
        }


        // remove everything in the destination log that occurs before the login event time
        removeEventsForMerging(destinationLog, sourceLog, cutoffTime, isEldMandateEnabled);

        Boolean cutOffTimeIsNull = (cutoffTime == null);
        Boolean srcEventTimeBeforeCutoff;
        Boolean eventRecordStatusIsInactiveChangeRequested;
        Boolean eventRecordStatusIsInactiveChangedNotSubmitted;

        // add all events from the source log that occur before the cutoff time
        // OR ELDEventRecord Status is InactiveChangeRequested (3) from ENC
        // Don't add in events that are already in the source log.
        for (EmployeeLogEldEvent srcEvent : sourceLog.getEldEventList().getEldEventList(listAccessorModifierEnum)) {

            srcEventTimeBeforeCutoff = (srcEvent != null && srcEvent.getStartTime() != null && !cutOffTimeIsNull && srcEvent.getStartTime().compareTo(cutoffTime) < 0);
            eventRecordStatusIsInactiveChangeRequested = (srcEvent.getEventRecordStatus() == Enums.EmployeeLogEldEventRecordStatus.InactiveChangeRequested.getValue());
            eventRecordStatusIsInactiveChangedNotSubmitted = (srcEvent.getEventRecordStatus() == Enums.EmployeeLogEldEventRecordStatus.InactiveChanged.getValue());

            //if cutofftime is null, take all events from DMO
            if (cutOffTimeIsNull || srcEventTimeBeforeCutoff || eventRecordStatusIsInactiveChangeRequested || eventRecordStatusIsInactiveChangedNotSubmitted) {
                AddEventToLog(destinationLog, srcEvent, srcEvent.getStartTime(), false);
            }
        }

        if (sourceLog.getEldEventList().getEldEventList(listAccessorModifierEnum).length > 0) {
            destinationLog.setRuleset(sourceLog.getEldEventList().getEldEventList(listAccessorModifierEnum)[sourceLog.getEldEventList().getEldEventList(listAccessorModifierEnum).length - 1].getRulesetType());
        }

        destinationLog.setTotalLogDistance(sourceLog.getTotalLogDistance());
        destinationLog.setIsOperatesSpecificVehiclesForOilfield(sourceLog.getIsOperatesSpecificVehiclesForOilfield());
        destinationLog.setIsNonCDLShortHaulExceptionUsed(sourceLog.getIsNonCDLShortHaulExceptionUsed());
        destinationLog.setShipmentInformation(sourceLog.getShipmentInformation());
        destinationLog.setTrailerNumbers(sourceLog.getTrailerNumbers());
        destinationLog.setTractorNumbers(sourceLog.getTractorNumbers());
        destinationLog.setVehiclePlate(sourceLog.getVehiclePlate());
        destinationLog.setTrailerPlate(sourceLog.getTrailerPlate());

        if (destinationLog.getTeamDriverList() == null || destinationLog.getTeamDriverList().IsEmpty()) {
            // no team drivers exist on the destination log, so move over the entire list from the source
            destinationLog.setTeamDriverList(sourceLog.getTeamDriverList());
        } else if (sourceLog.getTeamDriverList() != null && !sourceLog.getTeamDriverList().IsEmpty()) {
            // team drivers exist on the source
            // look at each of the team drivers on the source to determine if they should be copied to the destination
            for (TeamDriver srcTeamDriver : sourceLog.getTeamDriverList().getTeamDriverList()) {
                // determine if the team driver exists already in the destination
                boolean isFound = false;
                for (TeamDriver destTeamDriver : destinationLog.getTeamDriverList().getTeamDriverList()) {
                    if (destTeamDriver.getEmployeeCode().compareTo(srcTeamDriver.getEmployeeCode()) == 0) {
                        // found an existing driver, check to see if the time periods can be combined

                        // 7/17/12 JHM - Check for a matching period (prevent duplication)
                        if (srcTeamDriver.getStartTime() != null && srcTeamDriver.getEndTime() != null && destTeamDriver.getStartTime() != null && destTeamDriver.getEndTime() != null) {
                            if (srcTeamDriver.getStartTime().compareTo(destTeamDriver.getStartTime()) == 0 && srcTeamDriver.getEndTime().compareTo(destTeamDriver.getEndTime()) == 0) {
                                isFound = true;
                            }
                        }

                        // check if the destination's start time is contained in the time period of the source
                        // Ex: src 8:00am - 11:00am   dest 9:00am - 10:00am
                        // 12/29/11 JHM - Check for null times before doing comparisons
                        if (srcTeamDriver.getStartTime() != null && srcTeamDriver.getEndTime() != null && destTeamDriver.getStartTime() != null) {
                            if (srcTeamDriver.getStartTime().compareTo(destTeamDriver.getStartTime()) <= 0 && destTeamDriver.getStartTime().compareTo(srcTeamDriver.getEndTime()) <= 0) {
                                // it is, so adjust the start time
                                destTeamDriver.setStartTime(srcTeamDriver.getStartTime());
                                isFound = true;
                            }
                        }

                        // check if the destination's end time is contained in the time period of the source
                        // 12/29/11 JHM - Check for null times before doing comparisons
                        if (srcTeamDriver.getStartTime() != null && srcTeamDriver.getEndTime() != null && destTeamDriver.getEndTime() != null) {
                            if (srcTeamDriver.getStartTime().compareTo(destTeamDriver.getEndTime()) <= 0 && destTeamDriver.getEndTime().compareTo(srcTeamDriver.getEndTime()) <= 0) {
                                // it is, so adjust the end time
                                destTeamDriver.setEndTime(srcTeamDriver.getEndTime());
                                isFound = true;
                            }
                        }
                    }
                }

                if (!isFound) {
                    // no match for the source was found in the destination,
                    // so just add it to the destination
                    destinationLog.getTeamDriverList().Add(srcTeamDriver);
                }
            }
        }

        // Merge failures if necessary
        if (destinationLog.getEobrFailureList() == null || destinationLog.getEobrFailureList().IsEmpty()) {
            // No failures exist on destination, so move over whatever the source has
            destinationLog.setEobrFailureList(sourceLog.getEobrFailureList());
        } else if (sourceLog.getEobrFailureList() != null && !sourceLog.getEobrFailureList().IsEmpty()) {
            // Failures exist on the source, so determine which should be moved over
            FailureReport[] sourceFailureList = sourceLog.getEobrFailureList().getFailureReportList();
            for (FailureReport sourceFailure : sourceFailureList) {
                boolean isFound = false;
                FailureReport[] destinationFailureList = destinationLog.getEobrFailureList().getFailureReportList();
                for (FailureReport destinationFailure : destinationFailureList) {
                    if (sourceFailure.getCategory() != null && destinationFailure.getCategory() != null && sourceFailure.getCategory().getValue() == destinationFailure.getCategory().getValue()) {
                        boolean startTimesMatch = (sourceFailure.getStartTime() != null && destinationFailure.getStartTime() != null && sourceFailure.getStartTime().equals(destinationFailure.getStartTime()));
                        if (startTimesMatch) {
                            isFound = true;
                            break;
                        }
                    }
                }

                if (!isFound) {
                    destinationLog.getEobrFailureList().Add(sourceFailure);
                }
            }
        }

        // handle the merging of the weekly reset fields
        if (destinationLog.getWeeklyResetStartTimestamp() == null || !destinationLog.getIsWeeklyResetUsedOverridden()) {
            // if there is no reset on the destination yet, or if there is a reset that has not been overridden
            // then copy all of the fields
            destinationLog.setWeeklyResetStartTimestamp(sourceLog.getWeeklyResetStartTimestamp());
            destinationLog.setIsWeeklyResetUsed(sourceLog.getIsWeeklyResetUsed());
            destinationLog.setIsWeeklyResetUsedOverridden(sourceLog.getIsWeeklyResetUsedOverridden());
        }
    }

    /// <summary>
    /// Combine the two log lists into one.  Answer the combined list.
    /// The primary list is the more important list.  All logs from the primary
    /// list will be included.  Only the logs from the secondary list will be
    /// included if they are not duplicates from the primary.
    /// </summary>
    /// <param name="primaryList"></param>
    /// <param name="secondaryList"></param>
    /// <returns></returns>
    public static List<EmployeeLog> CombineLogLists(List<EmployeeLog> primaryList, List<EmployeeLog> secondaryList) {
        List<EmployeeLog> logList = new ArrayList<EmployeeLog>();

        // add all logs from the primary list
        logList.addAll(primaryList);

        LogComparer comparer = new LogComparer("Ascending");

        for (EmployeeLog employeeLog : secondaryList) {
            // look at each log in the secondary list
            // if it's not already in the list, then add it
            int index = Collections.binarySearch(logList, employeeLog, comparer);
            if (index < 0) {
                logList.add(employeeLog);
            }
        }

        // sort the list, ascending based on log date of the logs
        Collections.sort(logList, comparer);

        return logList;
    }

    public static EmployeeLogEldEventList CombineLogEvents(EmployeeLogEldEventList primaryList, EmployeeLogEldEvent additionalLogEvent) {
        return CombineLogEvents(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus, primaryList, additionalLogEvent);
    }

    public static EmployeeLogEldEventList CombineLogEvents(EldEventAdapterList.ListAccessorModifierEnum listAccessorModifierEnum, EmployeeLogEldEventList primaryList, EmployeeLogEldEvent additionalLogEvent) {
        List<EmployeeLogEldEvent> logEventList = new ArrayList<>();

        if (primaryList != null && primaryList.getEldEventList(listAccessorModifierEnum) != null && primaryList.getEldEventList(listAccessorModifierEnum).length > 0) {
            logEventList.addAll(new ArrayList<>(Arrays.asList(primaryList.getEldEventList(listAccessorModifierEnum))));
        }

        if (additionalLogEvent != null) {
            logEventList.add(additionalLogEvent);
        }

        EmployeeLogEldEvent[] newArray = new EmployeeLogEldEvent[logEventList.size()];
        newArray = logEventList.toArray(newArray);

        EmployeeLogEldEventList target = new EmployeeLogEldEventList();
        target.setEldEventList(newArray);

        return target;
    }

    public static EmployeeLogEldEventList CombineLogEvents(EmployeeLogEldEventList primaryList, EmployeeLogEldEventList secondaryList) {
        return CombineLogEvents(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus, primaryList, secondaryList);
    }

    public static EmployeeLogEldEventList CombineLogEvents(EldEventAdapterList.ListAccessorModifierEnum listAccessorModifierEnum, EmployeeLogEldEventList primaryList, EmployeeLogEldEventList secondaryList) {
        List<EmployeeLogEldEvent> logEventList = new ArrayList<>();

        if (primaryList != null && primaryList.getEldEventList(listAccessorModifierEnum) != null && primaryList.getEldEventList(listAccessorModifierEnum).length > 0) {
            logEventList.addAll(new ArrayList<EmployeeLogEldEvent>(Arrays.asList(primaryList.getEldEventList(listAccessorModifierEnum))));
        }

        if (secondaryList != null && secondaryList.getEldEventList(listAccessorModifierEnum) != null && secondaryList.getEldEventList(listAccessorModifierEnum).length > 0) {
            List<EmployeeLogEldEvent> logEventsToAdd = new ArrayList<EmployeeLogEldEvent>();

            for (EmployeeLogEldEvent secondaryEvent : secondaryList.getEldEventList(listAccessorModifierEnum)) {
                boolean timeCollision = false;

                //check to see if there's already a log event for the given time
                if (primaryList != null && primaryList.getEldEventList(listAccessorModifierEnum) != null) {
                    for (EmployeeLogEldEvent primaryEvent : primaryList.getEldEventList(listAccessorModifierEnum)) {
                        if (primaryEvent.getStartTime() != null && primaryEvent.getStartTime().equals(secondaryEvent.getStartTime())) {
                            timeCollision = true;
                            break;
                        }
                    }
                }

                if (!timeCollision) {
                    logEventsToAdd.add(secondaryEvent);
                }
            }

            logEventList.addAll(logEventsToAdd);
        }

        EmployeeLogEldEvent[] logEvents = new EmployeeLogEldEvent[logEventList.size()];
        logEvents = logEventList.toArray(logEvents);

        EmployeeLogEldEventList target = new EmployeeLogEldEventList();
        target.setEldEventList(logEvents);

        return target;
    }

    /// Convert the logdate and the first event starttime to reflect
    /// the user's new home terminal timezone.  All other events will
    /// be converted automatically.
    /// Remove events from the log whose adjusted start time does not
    /// fall between the starting and ending timestamp of the log.
    public static void AdjustLogEventsForTimezoneChange(Context ctx, User user, EmployeeLog empLog) {
        AdjustLogEventsForTimezoneChange(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus, ctx, user, empLog);
    }

    public static void AdjustLogEventsForTimezoneChange(EldEventAdapterList.ListAccessorModifierEnum listAccessorModifierEnum, Context ctx, User user, EmployeeLog empLog) {
        TimeZoneEnum oldTimezone = empLog.getTimezone();
        TimeZoneEnum newTimezone = user.getHomeTerminalTimeZone();

        if (!oldTimezone.equals(newTimezone)) {
            // change in timezone detected
            empLog.setTimezone(newTimezone);
            List<EmployeeLogEldEvent> newEventList = new ArrayList<>();

            // adjust log date and start time of first event to new timezone
            DateTime logDateAdjustedTime = new DateTime(empLog.getLogDate()).toDateTime(DateTimeZone.forTimeZone(newTimezone.toTimeZone()));
            // Date logDateAdjustedTime = DateUtility.ConvertLocalTime(oldTimezone, newTimezone, empLog.getLogDate());
            empLog.setLogDate(logDateAdjustedTime.toDate());
            empLog.getEldEventList().getEldEventList(listAccessorModifierEnum)[0].setStartTime(logDateAdjustedTime.toDate());

            // determine the starting and ending times for the log date
            Date logStartTime = CalculateLogStartTime(ctx, empLog.getLogDate(), newTimezone);

            // look at each event in the log and adjust the start time to the new timezone
            for (EmployeeLogEldEvent evt : empLog.getEldEventList().getEldEventList(listAccessorModifierEnum)) {
                if (logStartTime.compareTo(evt.getStartTime()) <= 0) {
                    // keep this event
                    newEventList.add(evt);
                }
            }

            // sort the list ascending based on the event times
            Collections.sort(newEventList, new GenericEventComparer());
            // put the event list back in the log
            empLog.getEldEventList().setEldEventList(newEventList.toArray(new EmployeeLogEldEvent[newEventList.size()]));
        }
    }


    /// <summary>
    /// If there is an outstanding failure on the log that matches
    /// the category, then stop that failure.
    /// Answer if any failures needed to be changed.
    /// </summary>
    public static boolean StopPendingFailure(EmployeeLog empLog, FailureCategoryEnum category, DateTime stopTimestamp) {
        boolean changesMade = false;
        FailureReportList reportList = null;
        switch (category.getValue()) {
            case FailureCategoryEnum.CLOCKSYNCHRONIZATION:
                reportList = empLog.getTimeSyncFailureList();
                break;
            case FailureCategoryEnum.EOBRDEVICE:
                reportList = empLog.getEobrFailureList();
                break;
        }

        if (reportList.getFailureReportList() != null) {
            for (FailureReport rpt : reportList.getFailureReportList()) {
                if (rpt.getStopTime() == null) {
                    rpt.setStopTime(stopTimestamp);

                    if (stopTimestamp.compareTo(rpt.getStartTime()) < 0) {
                        // detected a report where the start/stop times are reversed (stop time is earlier than the start)
                        // swap the start and stop times around
                        rpt.setStopTime(rpt.getStartTime());
                        rpt.setStartTime(stopTimestamp);
                    }

                    changesMade = true;
                }
            }
        }
        return changesMade;
    }

    /// <summary>
    /// Add the failure report to the current employee's log
    /// </summary>
    /// <param name="empLog"></param>
    /// <param name="failureReport"></param>
    public static void AddFailureReport(EmployeeLog empLog, FailureReport failureReport) {
        FailureReportList reportList = null;
        switch (failureReport.getCategory().getValue()) {
            case FailureCategoryEnum.CLOCKSYNCHRONIZATION:
                reportList = empLog.getTimeSyncFailureList();
                break;
            case FailureCategoryEnum.EOBRDEVICE:
                reportList = empLog.getEobrFailureList();
                break;
        }

        List<FailureReport> list = null;

        if (reportList.getFailureReportList() != null) {
            list = new ArrayList<FailureReport>(Arrays.asList(reportList.getFailureReportList()));
        } else {
            list = new ArrayList<FailureReport>();
        }

        list.add(failureReport);
        reportList.setFailureReportList(list.toArray(new FailureReport[list.size()]));

    }

    /**
     * Calculate the total amount of time (in # of minutes) for log events in the log with the given status.
     *
     * @param empLog           employee log to use
     * @param dutyStatus       status to look for
     * @param currentClockTime the current clock time, or null if the last event should run to the end of the day
     * @return
     */
    public static long CalculateLogEventTotal(EmployeeLog empLog, DutyStatusEnum dutyStatus, Date currentClockTime, TimeUnit timeUnit) {
        long totalTime = 0;

        EmployeeLogEldEventList logEventList = empLog.getEldEventList();
        // Sort ascending by Date (not by default sequential number)
        List<EmployeeLogEldEvent> eldEvents = new ArrayList<EmployeeLogEldEvent>(Arrays.asList(logEventList.getEldEventList(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus)));
        Collections.sort(eldEvents, new GenericEventComparer());
        if (eldEvents.size() > 0) {
            int duration;
            EmployeeLogEldEvent previousEvent = null;
            for (int index = 0; index < eldEvents.size(); index++) {
                // process every log event in the log
                EmployeeLogEldEvent evt = eldEvents.get(index);
                if (evt.getEventType() == null || evt.getEventType() != Enums.EmployeeLogEldEventType.DutyStatusChange) {
                    continue;
                }
                if (evt.getEventRecordStatus() == null || evt.getEventRecordStatus() != Enums.EmployeeLogEldEventRecordStatus.Active.getValue()) {
                    continue;
                }
                if (evt.getEventDateTime() == null) {
                    continue;
                }
                if (evt.getDutyStatusEnum() == null) {
                    continue;
                }

                // note: the very first event in the log list is ignored
                if (previousEvent != null) {
                    if (previousEvent.getDutyStatusEnum().equals(dutyStatus)) {
                        // found an event matching the desired status
                        // calculate the total duration for the event by subtracting from the previous event
                        long diffInMs = evt.getStartTime().getTime() - previousEvent.getStartTime().getTime();
                        duration = (int) timeUnit.convert(diffInMs, TimeUnit.MILLISECONDS);

                        // accumulate the total # of minutes spent
                        totalTime += duration;
                    }
                }
                previousEvent = evt;
            }

            // check to see if the last event in the list is one that needs to counted as well
            if (previousEvent != null) {
                if (previousEvent.getDutyStatusEnum().equals(dutyStatus)) {
                    // the last event on the log also need to be accumulated
                    Date endLogTime = currentClockTime;
                    if (currentClockTime == null) {
                        endLogTime = DateUtility.AddDays(eldEvents.get(0).getStartTime(), 1);
                    }

                    long diffInMs = endLogTime.getTime() - previousEvent.getStartTime().getTime();

                    duration = (int) timeUnit.convert(diffInMs, TimeUnit.MILLISECONDS);
                    totalTime += duration;
                }
            }
        }

        return totalTime;
    }

    /// <summary>
    /// Calculate the continguous time (in number of minutes) spent in off-duty
    //  statuses (either OFF-DUTY or SLEEPER).  Calculate from the beginning of
    //  the log until the first non-offduty status is encountered in the log
    /// </summary>
    /// <param name="empLog">log to calculate</param>
    /// <param name="currentClockTime">current clock time</param>
    /// <returns></returns>
    public static int CalculateContiguousOffDutyStart(Context ctx, EmployeeLog empLog, Date currentClockTime) {
        int totalTime = 0;

        EmployeeLogEldEventList logEventList = empLog.getEldEventList();
        if (logEventList.getEldEventList(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus).length > 0) {
            // there are log events to process
            int duration = 0;

            EmployeeLogEldEvent firstEvent = logEventList.getEldEventList(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus)[0];
            if (firstEvent.getDutyStatusEnum().getValue() == DutyStatusEnum.OFFDUTY || firstEvent.getDutyStatusEnum().getValue() == DutyStatusEnum.SLEEPER) {
                // first event is off-duty
                // search the rest of the events for the first on-duty event
                EmployeeLogEldEvent offDutyEvent = firstEvent;

                EmployeeLogEldEvent onDutyEvent = null;
                for (int index = 1; index < logEventList.getEldEventList(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus).length; index++) {
                    // look at the rest of the events in the log
                    EmployeeLogEldEvent currentEvent = logEventList.getEldEventList(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus)[index];

                    if (currentEvent.getDutyStatusEnum().getValue() == DutyStatusEnum.DRIVING || currentEvent.getDutyStatusEnum().getValue() == DutyStatusEnum.ONDUTY) {
                        // found an on-duty event (either Driving or On-Duty status)
                        onDutyEvent = currentEvent;
                        break;
                    }
                }

                if (onDutyEvent != null) {
                    // found an on-duty event in the log
                    // calc the duration between the off-duty and on-duty events
                    long diffInMs = onDutyEvent.getStartTime().getTime() - offDutyEvent.getStartTime().getTime();
                    duration = (int) DateUtility.ConvertMillisecondsToMinutes(diffInMs);
                } else {
                    // no on-duty event detected, so the entire log is off-duty
                    if (currentClockTime == null) {
                        // 24 hour off-duty period
                        duration = (isFallDaylightSavingsTransition(offDutyEvent.getStartTime()) ? TWENTY_FOUR_HOUR_PERIOD + ONE_HOUR_PERIOD : TWENTY_FOUR_HOUR_PERIOD);
                    } else {
                        // a current clock is passed in,
                        // are we processing today's log?
                        if (currentClockTime.equals(empLog.getLogDate())) {
                            long diffInMs = currentClockTime.getTime() - offDutyEvent.getStartTime().getTime();
                            duration = (int) DateUtility.ConvertMillisecondsToMinutes(diffInMs);
                        } else {
                            // current clock does NOT indicate that we're processing today's log
                            // this is a 24 hour off-duty period
                            duration = (isFallDaylightSavingsTransition(offDutyEvent.getStartTime()) ? TWENTY_FOUR_HOUR_PERIOD + ONE_HOUR_PERIOD : TWENTY_FOUR_HOUR_PERIOD);
                        }
                    }
                }
            }

            totalTime = duration;
        }

        return totalTime;
    }

    /// <summary>
    /// Calculate the continguous time spent in off-duty statuses (either OFF-DUTY or SLEEPER)
    /// Calculate from the end of the log until the first non-offduty status
    /// is encountered in the log
    /// </summary>
    /// <param name="empLog">log to calculate</param>
    /// <returns></returns>
    public static int CalculateContiguousOffDutyEnd(Context ctx, EmployeeLog empLog) {
        int totalTime = 0;
        EmployeeLogEldEventList logEventList = empLog.getEldEventList();
        if (logEventList.getEldEventList(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus).length > 0) {
            // there are log events to process
            int duration = 0;

            EmployeeLogEldEvent lastEvent = logEventList.getEldEventList(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus)[logEventList.getEldEventList(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus).length - 1];

            if (lastEvent.getDutyStatusEnum().getValue() == DutyStatusEnum.OFFDUTY || lastEvent.getDutyStatusEnum().getValue() == DutyStatusEnum.SLEEPER) {
                // last event on the log is off-duty
                // search the rest of the events for the first on-duty event
                EmployeeLogEldEvent offDutyEvent = lastEvent;

                for (int index = logEventList.getEldEventList(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus).length - 1; index >= 0; index--) {
                    // look backwards through the rest of the events in the log
                    EmployeeLogEldEvent currentEvent = logEventList.getEldEventList(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus)[index];

                    if (currentEvent.getDutyStatusEnum().getValue() == DutyStatusEnum.DRIVING || currentEvent.getDutyStatusEnum().getValue() == DutyStatusEnum.ONDUTY) {
                        // found an on-duty event (either Driving or On-Duty status)
                        break;
                    } else {
                        offDutyEvent = currentEvent;
                    }
                }

                // calc the duration from the end of the log to the
                // last off duty event found in the log
                EmployeeLogEldEvent firstEvent = logEventList.getEldEventList(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus)[0];
                Date logEndTime = DateUtility.AddDays(firstEvent.getStartTime(), 1);

                long diffInMs = logEndTime.getTime() - offDutyEvent.getStartTime().getTime();
                duration = (int) DateUtility.ConvertMillisecondsToMinutes(diffInMs);
            }

            totalTime = duration;
        }

        return totalTime;
    }

    /**
     * Remove all log events from the log that occur between the start and end timestamp.
     * If an event matches the either timestamp, it is not removed.
     *
     * @param empLog
     * @param startTimestamp
     * @param endTimestamp
     * @return
     */
    public static EmployeeLogEldEvent RemoveAllEventsBetween(EmployeeLog empLog, Date startTimestamp, Date endTimestamp) {
        return RemoveAllEventsBetween(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus, empLog, startTimestamp, endTimestamp);
    }

    public static EmployeeLogEldEvent RemoveAllEventsBetween(EldEventAdapterList.ListAccessorModifierEnum listAccessorModifierEnum, EmployeeLog empLog, Date startTimestamp, Date endTimestamp) {
        EmployeeLogEldEvent lastEventInPeriod = null;

        EmployeeLogEldEventList logEventList = empLog.getEldEventList();
        if (logEventList.getEldEventList(listAccessorModifierEnum).length > 0) {
            // there are events to work with

            // create the new log event list.
            // all event earlier than the timestamp will be copied into here
            List<EmployeeLogEldEvent> newList = new ArrayList<>(logEventList.getEldEventList(listAccessorModifierEnum).length);

            // look at each event
            for (int index = 0; index < logEventList.getEldEventList(listAccessorModifierEnum).length; index++) {
                EmployeeLogEldEvent evt = logEventList.getEldEventList(listAccessorModifierEnum)[index];
                if (evt.getStartTime() != null && evt.getStartTime().compareTo(startTimestamp) < 0 || evt.getStartTime().compareTo(endTimestamp) > 0) {
                    // this event happened outside of the timestamp range, so keep it
                    newList.add(evt);
                } else {
                    // found an event that occurred within the duration
                    lastEventInPeriod = evt;
                }
            }
            GenericEventComparer eventComparer = new GenericEventComparer();

            // sort the list ascending based on the event times
            Collections.sort(newList, eventComparer);

            // put the event list back in the log
            empLog.getEldEventList().setEldEventList(newList.toArray(new EmployeeLogEldEvent[newList.size()]));
        }
        return lastEventInPeriod;
    }

    /// <summary>
    /// Answer if there is at least one validated event on the
    /// log in question.
    /// </summary>
    /// <param name="empLog"></param>
    /// <returns>true if at least on validated event exists, false otherwise</returns>
    public static boolean ValidatedEventExists(EmployeeLog empLog) {
        return ValidatedEventExists(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus, empLog);
    }

    /// <summary>
    /// Answer if there is at least one validated event on the
    /// log in question.
    /// </summary>
    /// <param name="empLog"></param>
    /// <returns>true if at least on validated event exists, false otherwise</returns>
    public static boolean ValidatedEventExists(EldEventAdapterList.ListAccessorModifierEnum listAccessorModifierEnum, EmployeeLog empLog) {
        boolean answer = false;

        // look at each event
        EmployeeLogEldEventList logEventList = empLog.getEldEventList();
        if (logEventList != null) {
            for (int index = 0; index < logEventList.getEldEventList(listAccessorModifierEnum).length; index++) {
                EmployeeLogEldEvent evt = logEventList.getEldEventList(listAccessorModifierEnum)[index];
                if (evt.getIsStartTimeValidated()) {
                    answer = true;
                    break;
                }
            }
        }

        return answer;
    }

    private static boolean isFallDaylightSavingsTransition(Date date) {
        TimeZone tz = GlobalState.getInstance().getCurrentUser().getHomeTerminalTimeZone().toTimeZone();
        Date nextDay = DateUtility.AddDays(date, 1);
        return tz.inDaylightTime(date) && !tz.inDaylightTime(nextDay);
    }


    /// <summary>
    /// Answer if a driving event exists on the log that occurs between the start/end times.
    /// </summary>
    public static boolean DrivingEventExistsBetween(EmployeeLog empLog, Date startTime, Date endTime) {
        return DrivingEventExistsBetween(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus, empLog, startTime, endTime);
    }

    public static boolean DrivingEventExistsBetween(EldEventAdapterList.ListAccessorModifierEnum listAccessorModifierEnum, EmployeeLog empLog, Date startTime, Date endTime) {
        boolean answer = false;

        // look at each event
        EmployeeLogEldEventList logEventList = empLog.getEldEventList();
        if (logEventList != null) {
            for (int index = 0; index < logEventList.getEldEventList(listAccessorModifierEnum).length; index++) {
                EmployeeLogEldEvent evt = logEventList.getEldEventList(listAccessorModifierEnum)[index];
                if (evt.getStartTime() != null && evt.getStartTime().compareTo(startTime) >= 0 && evt.getStartTime().compareTo(endTime) <= 0 && evt.getDutyStatusEnum().getValue() == DutyStatusEnum.DRIVING) {
                    // located a driving period that occurs between the start/end time
                    answer = true;
                    break;
                }
            }
        }

        return answer;
    }

    /// <summary>
    /// Answer if a driving event exists on the log that occurs between the start/end times.
    /// </summary>
    public static boolean ContainsDrivingOrOnDutyEvent(EmployeeLog empLog) {
        return ContainsDrivingOrOnDutyEvent(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus, empLog);
    }

    public static boolean ContainsDrivingOrOnDutyEvent(EldEventAdapterList.ListAccessorModifierEnum listAccessorModifierEnum, EmployeeLog empLog) {
        boolean answer = false;

        // look at each event
        EmployeeLogEldEventList logEventList = empLog.getEldEventList();
        if (logEventList != null) {
            for (int index = 0; index < logEventList.getEldEventList(listAccessorModifierEnum).length; index++) {
                EmployeeLogEldEvent evt = logEventList.getEldEventList(listAccessorModifierEnum)[index];
                if (evt.getDutyStatusEnum().getValue() == DutyStatusEnum.DRIVING || evt.getDutyStatusEnum().getValue() == DutyStatusEnum.ONDUTY) {
                    // located a valid event
                    answer = true;
                    break;
                }
            }
        }

        return answer;
    }

    public static boolean ShouldUseReducedGpsPrecision() {
        return GlobalState.getInstance().getIsInPersonalConveyanceDutyStatus() && GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled();
    }

    public static float GetReducedPrecisionGPSForFloat(float coordinate) {
        //round up value and reduce precision to one decimal place
        //used for Eld Mandate PC Duty Status
        float result = RoundFloatByDecimalPlace(coordinate, 1);

        return result;
    }

    public static float RoundFloatByDecimalPlace(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        float result = bd.floatValue();
        return result;
    }


    public static Double GetReducedPrecisionGPSForDouble(Double coordinate, boolean isInPersonalConveyance) {
        int decimalPlace = 2;
        if (isInPersonalConveyance) {
            decimalPlace = 1;
        }

        BigDecimal bd = new BigDecimal(Double.toString(coordinate));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.doubleValue();
    }

    //Enum to allow assignment of handlers
    public class UtilityHandlerType extends EnumBase {
        public static final int NULL = 0;
        public static final int EXEMPTLOGCONVERTEDHANDLER = 1;
        public static final int EXEMPTLOGPREVIOUSLOGCONVERTEDHANDLER = 2;
        public static final String DmoEnum_Null = "NULL";
        public static final String DmoEnum_ExemptLogConvertedHandler = "ExemptLogConvertedHandler";
        public static final String DmoEnum_ExemptLogPreviousLogConvertedHandler = "ExemptLogPreviousLogConvertedHandler";

        public UtilityHandlerType(int value) {
            super(value);
        }

        @Override
        public String toDMOEnum() {
            if (value == NULL) {
                return DmoEnum_Null;
            } else if (value == EXEMPTLOGCONVERTEDHANDLER) {
                return DmoEnum_ExemptLogConvertedHandler;
            } else if (value == EXEMPTLOGPREVIOUSLOGCONVERTEDHANDLER) {
                return DmoEnum_ExemptLogPreviousLogConvertedHandler;
            } else {
                return DmoEnum_Null;
            }
        }

        @Override
        protected int getArrayId() {
            return 0;
        }

        @Override
        public void setValue(int value) throws IndexOutOfBoundsException {
            switch (value) {
                case EXEMPTLOGCONVERTEDHANDLER:
                case EXEMPTLOGPREVIOUSLOGCONVERTEDHANDLER:
                    this.value = value;
                default:
                    super.setValue(value);
            }
        }
    }

    public static boolean hasValidHandler(int type) {
        switch (type) {
            case UtilityHandlerType.EXEMPTLOGCONVERTEDHANDLER:
                return _exemptLogConvertedHandler != null;
            case UtilityHandlerType.EXEMPTLOGPREVIOUSLOGCONVERTEDHANDLER:
                return _exemptLogPreviousLogConvertedHandler != null;
            default:
                return false;
        }
    }

    public static void assignEventListenerToHandler(Object args, int handlerType) {
        switch (handlerType) {
            case UtilityHandlerType.EXEMPTLOGCONVERTEDHANDLER:
                if (args != null) {
                    _exemptLogConvertedHandler.onEventChange((ExemptLogConvertedToGridLogEventArgs) args);
                }
            case UtilityHandlerType.EXEMPTLOGPREVIOUSLOGCONVERTEDHANDLER:
                if (args != null) {
                    _exemptLogPreviousLogConvertedHandler.onEventChange((ExemptLogPreviousLogsConvertedToGridLogEventArgs) args);
                }
        }

    }

    public static void executeNewReverseGeocodeTaskAsync(Context ctx, EmployeeLog empLog, GpsLocation gpsLocation, IReverseGeocodeLocationListener listener) {
        new ReverseGeocodeLocationTask(ctx, empLog, gpsLocation, listener).execute();
    }

    public static void RegisterForExemptLogConversionEvents(IEventHandler<ExemptLogConvertedToGridLogEventArgs> eventHandler) {
        _exemptLogConvertedHandler = eventHandler;
    }

    public static void RegisterForExemptLogPreviousLogConversionEvents(IEventHandler<ExemptLogPreviousLogsConvertedToGridLogEventArgs> eventHandler) {
        _exemptLogPreviousLogConvertedHandler = eventHandler;
    }

    private static IEventHandler<ExemptLogPreviousLogsConvertedToGridLogEventArgs> _exemptLogPreviousLogConvertedHandler;

    private static IEventHandler<ExemptLogConvertedToGridLogEventArgs> _exemptLogConvertedHandler;

    private static final AtomicInteger reverseGeocodingTaskCount = new AtomicInteger();

    public static boolean IsReverseGeocodingLocationAsync() {
        return (reverseGeocodingTaskCount.get() > 0);
    }

    private static class ReverseGeocodeLocationTask extends AsyncTask<Void, Void, Void> {
        private final WeakReference<Context> context;
        private final EmployeeLog log;
        private final GpsLocation location;

        private final IReverseGeocodeLocationListener listener;

        public ReverseGeocodeLocationTask(Context context, EmployeeLog log, GpsLocation location, IReverseGeocodeLocationListener listener) {
            this.context = new WeakReference<Context>(context);
            this.log = log;
            this.location = location;
            this.listener = listener;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            reverseGeocodingTaskCount.incrementAndGet();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                MandateObjectFactory.getInstance(context.get(), GlobalState.getInstance().getFeatureService()).getCurrentEventController().ReverseGeocodeLocation(log, location);
            } finally {
                reverseGeocodingTaskCount.decrementAndGet();
                if (listener != null) {
                    try {
                        listener.onResult(log, location);
                    } catch (Exception ex) {
                        Log.e("UnhandledCatch", ex.getMessage(), ex);
                    }
                }
            }
            return null;
        }


    }

    public static String TripInfoToString(HashMap<EmployeeLog.TripInfoPropertyKey, HashSet<String>> tripInfoMap, EmployeeLog.TripInfoPropertyKey key) {
        String result = "";
        if (tripInfoMap.containsKey(key)) {
            HashSet<String> itemData = tripInfoMap.get(key);
            if (!itemData.isEmpty()) {
                result = TextUtils.join(",", itemData);
            }
        }
        return result;
    }

    public static List<Pair<EmployeeLogEldEvent, Enums.SpecialDrivingCategory>> loadEventsIncludingSpecialDrivingCategories(Context context, EmployeeLog employeeLog) {
        IAPIController empLogCtrlr = MandateObjectFactory.getInstance(context, GlobalState.getInstance().getFeatureService()).getCurrentEventController();
        //It returns all events for On-Duty, Off-duty, Personal Conveyance and Yard move
        EmployeeLogEldEvent[] eldEventArray = empLogCtrlr.fetchEldEventsByEventTypes(
                (int) employeeLog.getPrimaryKey(),
                Arrays.asList(
                        Enums.EmployeeLogEldEventType.DutyStatusChange.getValue(),
                        Enums.EmployeeLogEldEventType.ChangeInDriversIndication.getValue()),
                Arrays.asList(Enums.EmployeeLogEldEventRecordStatus.Active.getValue()));
        return loadEventsIncludingSpecialDrivingCategories(context, employeeLog, eldEventArray);
    }


    public static List<Pair<EmployeeLogEldEvent, Enums.SpecialDrivingCategory>> loadEventsIncludingSpecialDrivingCategories(Context context, EmployeeLog employeeLog, EmployeeLogEldEvent[] eldEventArray) {
        List<Pair<EmployeeLogEldEvent, Enums.SpecialDrivingCategory>> eventWithSpecialDrivingCategoryList = new ArrayList<>();
        EmployeeLogWithProvisionsController logWithProvisionsController = new EmployeeLogWithProvisionsController(context);

        Arrays.sort(eldEventArray, new Comparator<EmployeeLogEldEvent>() {
            public int compare(EmployeeLogEldEvent event1, EmployeeLogEldEvent event2) {
                int result = event1.getEventDateTime().compareTo(event2.getEventDateTime());
                if (result == 0) {
                    if (event1.getEventType() == Enums.EmployeeLogEldEventType.ChangeInDriversIndication) {
                        result = -1;
                    } else if (event2.getEventType() == Enums.EmployeeLogEldEventType.ChangeInDriversIndication) {
                        result = 1;
                    } else {
                        result = 0;
                    }
                }
                return result;
            }
        });

        List<EmployeeLogEldEvent> pCYMCategorizedEvents = new ArrayList<>();

        for (int i = 0; i <= eldEventArray.length - 1; i++) {
            EmployeeLogEldEvent currentEvent = eldEventArray[i];
            EmployeeLogWithProvisions provisions = logWithProvisionsController.GetLogWithProvisionsForLogEldEventKey(employeeLog, currentEvent.getPrimaryKey());
            EmployeeLogEldEvent nextEvent = i < eldEventArray.length - 1 ? eldEventArray[i + 1] : null;

            if (provisions != null) {
                //It links a LogWithProvisions row to an EldLogEvent for Hyrail or Non-regulated if the relationship exists

                if (provisions.getProvisionTypeEnum() == EmployeeLogProvisionTypeEnum.HYRAIL.getValue()) {
                    eventWithSpecialDrivingCategoryList.add(new Pair<>(currentEvent, Enums.SpecialDrivingCategory.Hyrail));
                } else if (provisions.getProvisionTypeEnum() == EmployeeLogProvisionTypeEnum.NONREGULATED.getValue()) {
                    eventWithSpecialDrivingCategoryList.add(new Pair<>(currentEvent, Enums.SpecialDrivingCategory.NonRegulated));
                }
            } else if (nextEvent != null &&
                    (nextEvent.getEventType() == Enums.EmployeeLogEldEventType.DutyStatusChange &&
                            (nextEvent.getEventCode() == 1 || nextEvent.getEventCode() == 4)) &&
                    currentEvent.getEventType() == Enums.EmployeeLogEldEventType.ChangeInDriversIndication) {
                //It links a PC or YM row to its corresponding On Duty or Off Duty record

                if ((nextEvent.getEventType() == Enums.EmployeeLogEldEventType.DutyStatusChange &&
                        (nextEvent.getEventCode() == 1 || nextEvent.getEventCode() == 4))
                        && currentEvent.getEventType() == Enums.EmployeeLogEldEventType.ChangeInDriversIndication) {

                    if (currentEvent.getEventCode() == 1) {
                        eventWithSpecialDrivingCategoryList.add(new Pair<>(nextEvent, Enums.SpecialDrivingCategory.PersonalConveyance));
                        pCYMCategorizedEvents.add(nextEvent);
                    } else if (currentEvent.getEventCode() == 2) {
                        eventWithSpecialDrivingCategoryList.add(new Pair<>(nextEvent, Enums.SpecialDrivingCategory.YardMove));
                        pCYMCategorizedEvents.add(nextEvent);
                    }
                }
            } else if (!pCYMCategorizedEvents.contains(currentEvent) && currentEvent.getEventType() == Enums.EmployeeLogEldEventType.DutyStatusChange) {
                // Add all the events which correspond to DutyStatusChange and that do not have special driving category
                eventWithSpecialDrivingCategoryList.add(new Pair<>(currentEvent, Enums.SpecialDrivingCategory.None));
            }else{
                // add all other events as NONE
                eventWithSpecialDrivingCategoryList.add(new Pair<>(currentEvent, Enums.SpecialDrivingCategory.None));
            }
        }

        return eventWithSpecialDrivingCategoryList;
    }

    public static void invalidatePCRecords(Context context, int PCPrimaryKey) {
        IAPIController empLogCtrlr = MandateObjectFactory.getInstance(context, GlobalState.getInstance().getFeatureService()).getCurrentEventController();
        //It returns all events Personal Conveyance associated to that key.
        EmployeeLogEldEvent[] eldEventArray = empLogCtrlr.fetchEldPCRecords(PCPrimaryKey);
        if (eldEventArray.length > 0) {
            EmployeeLogEldEventFacade facade = new EmployeeLogEldEventFacade(context, GlobalState.getInstance().getCurrentUser());
            for (EmployeeLogEldEvent event : eldEventArray) {
                event.setEventRecordStatus(Enums.EmployeeLogEldEventRecordStatus.InactiveChanged.getValue());
                facade.Save(event);
            }
        }
    }

    /**
     *  In the event that a driver stops driving prior to midnight, but the DRIVE_OFF hasn't fired yet
     *  or the driver hasn't yet responded to the "Continue in driving status?" prompt while midnight is
     *  crossed, we need to replace the driving event on the current log with the appropriate duty status.
     * @param log
     * @param dutyStatus
     * @return the log that should have a status change performed - yesterday's if this was the over-midnight scenario
     */
    public static EmployeeLog switchOverMidnightDrivingToDutyStatus(EmployeeLog log, DutyStatusEnum dutyStatus, int eventRecordOrigin) {
        EmployeeLogEldEvent[] events = log.getEldEventList().getActiveEldEventList(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus);
        if(events.length == 1) {
            EmployeeLogEldEvent todayEvent = events[0];
            if(isAutomaticDrivingEvent(todayEvent))
            {
                IAPIController empLogEventController = MandateObjectFactory.getInstance(GlobalState.getInstance(), GlobalState.getInstance().getFeatureService()).getCurrentEventController();
                Date yesterdaysLogDate = DateUtility.AddDays(log.getLogDate(), -1);
                User currentUser = GlobalState.getInstance().getLoggedInUser(log.getEmployeeId());

                EmployeeLog yesterdaysLog = empLogEventController.GetEmployeeLog(currentUser, yesterdaysLogDate);
                if(yesterdaysLog != null) {
                    EmployeeLogEldEvent yesterdayLastEvent = EmployeeLogUtilities.GetLastEventInLog(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus, yesterdaysLog);
                    if(yesterdayLastEvent.getDutyStatusEnum().getValue() == DutyStatusEnum.DRIVING) {
                        Date stopTime = GlobalState.getInstance().getPotentialDrivingStopTimestamp();
                        if(stopTime.before(log.getLogDate())) {
                            todayEvent.setDutyStatusEnum(dutyStatus);
                            todayEvent.setEventRecordOrigin(eventRecordOrigin);
                            empLogEventController.SaveLocalEmployeeLog(currentUser, log);

                            return yesterdaysLog;
                        }
                    }
                }
            }
        }

        return log;
    }

    private static boolean isAutomaticDrivingEvent(EmployeeLogEldEvent event) {
        return event.getDutyStatusEnum().getValue() == DutyStatusEnum.DRIVING
                && event.getEventRecordOrigin() != null
                && event.getEventRecordOrigin() == Enums.EmployeeLogEldEventRecordOrigin.AutomaticallyRecorded;
    }
}
