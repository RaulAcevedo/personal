package com.jjkeller.kmbapi.controller.calcengine;

import android.content.Context;

import com.jjkeller.kmbapi.R;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.controller.utility.EmployeeLogUtilities;
import com.jjkeller.kmbapi.controller.utility.LogComparer;
import com.jjkeller.kmbapi.enums.DriverTypeEnum;
import com.jjkeller.kmbapi.enums.DutyStatusEnum;
import com.jjkeller.kmbapi.enums.RuleSetTypeEnum;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class HOSAudit {

    private static final int US_WEEKLY_TOTAL_RESET_HOUR_THRESHOLD = 2040;  //Note:  in minutes
    private static final int CD_CYCLE1_WEEKLY_TOTAL_RESET_HOUR_THRESHOLD = 2160;
    private static final int CD_CYCLE2_WEEKLY_TOTAL_RESET_HOUR_THRESHOLD = 4320;
    private static final int TWENTY_FOUR_HOUR_PERIOD = 1440;
    private static final int ONE_HOUR_PERIOD = 60;
    
	public List<LogSummary> PerformCompleteAudit(User user, Context ctx, List<EmployeeLog> logList, Date currentClockTime)
	{
		LinkedHashMap<Date, LogSummary> logDict = new LinkedHashMap<Date, LogSummary>();
		LogSummary summary = null;
		
		Date endDate = null;
		
		if (logList.size() > 0)
		{
			LogComparer logComparer = new LogComparer(ctx.getString(R.string.ascending));
			Collections.sort(logList, logComparer);
			
			Date startDate = logList.get(0).getLogDate();
			endDate = logList.get(logList.size()-1).getLogDate();
			
			Date auditDate = startDate;
			while (auditDate.compareTo(endDate) <=0)
			{
				EmployeeLog empLog = EmployeeLogUtilities.FindLog(auditDate, logList);
				summary = this.Audit(ctx, user, empLog, auditDate, currentClockTime, true);
				
				logDict.put(auditDate, summary);
				
				auditDate = DateUtility.AddDays(auditDate, 1);
			}
		}
		
		for (LogSummary logSummary : logDict.values())
		{
			if (logSummary.getLogDate().equals(endDate))
			{
				this.CalculateWeeklyTotals(user, ctx, logDict, logSummary);
			}
		}
		
        // create the results to send back
        // move the summaries from the temporary dictionary into the outbound list
		List<LogSummary> logSummaries = new ArrayList<LogSummary>();
		for (LogSummary item : logDict.values())
		{
			logSummaries.add(item);
		}
		
		Collections.reverse(logSummaries);
		
		return logSummaries;						
	}
	
	private LogSummary Audit(Context ctx, User user, EmployeeLog empLog, Date auditDate, Date currentClockTime, boolean calcDailyResetTotals)
	{
		LogSummary summary = new LogSummary();
		
		summary.setLogDate(auditDate);
		if (empLog != null)
		{
			Date logDate = empLog.getLogDate();
			Date endLogTime = null;
			
			if (EmployeeLogUtilities.CalculateLogStartTime(ctx, currentClockTime, user.getHomeTerminalTimeZone()).equals(logDate))
			{
				endLogTime = currentClockTime;
			}
			
			summary.setLogExists(true);
			summary.setIsComplete(true);
			summary.setDistance(empLog.getTotalLogDistance() + empLog.getMobileDerivedDistance());
			summary.setDrivingDuration(EmployeeLogUtilities.CalculateLogEventTotal(empLog, new DutyStatusEnum(DutyStatusEnum.DRIVING), endLogTime, TimeUnit.MILLISECONDS));
			summary.setOnDutyDuration(EmployeeLogUtilities.CalculateLogEventTotal(empLog, new DutyStatusEnum(DutyStatusEnum.ONDUTY), endLogTime, TimeUnit.MILLISECONDS));
			summary.setDriverType(empLog.getDriverType());
			summary.setRuleset(empLog.getRuleset());
			summary.setExemptLogType(empLog.getExemptLogType());
			
			summary.setWeeklyTotalDuration(null);
			
			if (calcDailyResetTotals)
			{
				summary.setOffDutyStart(EmployeeLogUtilities.CalculateContiguousOffDutyStart(ctx, empLog, currentClockTime));
				summary.setOffDutyEnd(EmployeeLogUtilities.CalculateContiguousOffDutyEnd(ctx, empLog));
			}
		}
		else
		{
			summary.setDrivingDuration(null);
			summary.setOnDutyDuration(null);
			summary.setDistance(-1);
			summary.setWeeklyTotalDuration(null);
		}
		
		return summary;
	}

    private void CalculateWeeklyTotals(User user, Context ctx, LinkedHashMap<Date, LogSummary> logSummaries, LogSummary logSummary)
    {
        Boolean performWeeklyTotalReset = false;
        int weeklyResetMinutes = 0;
        Date endDate = logSummary.getLogDate();
        Date startDate = null;

        switch(logSummary.getRuleset().getValue())
        {
        	case RuleSetTypeEnum.US60HOUR:
        	case RuleSetTypeEnum.USMOTIONPICTURE_7DAY:
        	case RuleSetTypeEnum.ALASKA_7DAY:
            	startDate = DateUtility.AddDays(endDate, -6);
                performWeeklyTotalReset = logSummary.getDriverType().getValue() == DriverTypeEnum.PROPERTYCARRYING && user.getIs34HourResetAllowed();
                weeklyResetMinutes = US_WEEKLY_TOTAL_RESET_HOUR_THRESHOLD;
            	break;
        	case RuleSetTypeEnum.US70HOUR:
        	case RuleSetTypeEnum.USMOTIONPICTURE_8DAY:
        	case RuleSetTypeEnum.ALASKA_8DAY:
        	case RuleSetTypeEnum.CALIFORNIA_INTRASTATE:
            	startDate = DateUtility.AddDays(endDate, -7);
                performWeeklyTotalReset = logSummary.getDriverType().getValue() == DriverTypeEnum.PROPERTYCARRYING && user.getIs34HourResetAllowed();
                weeklyResetMinutes = US_WEEKLY_TOTAL_RESET_HOUR_THRESHOLD;
            	break;
            case RuleSetTypeEnum.CANADIAN_CYCLE1:
            	startDate = DateUtility.AddDays(endDate, -6);
                performWeeklyTotalReset = user.getIs34HourResetAllowed();
                weeklyResetMinutes = CD_CYCLE1_WEEKLY_TOTAL_RESET_HOUR_THRESHOLD;
                break;
            case RuleSetTypeEnum.CANADIAN_CYCLE2:
            	startDate = DateUtility.AddDays(endDate, -13);
                performWeeklyTotalReset = user.getIs34HourResetAllowed();
                weeklyResetMinutes = CD_CYCLE2_WEEKLY_TOTAL_RESET_HOUR_THRESHOLD;
                break;
            case RuleSetTypeEnum.TEXAS:
            	startDate = DateUtility.AddDays(endDate, -6);
            	performWeeklyTotalReset = user.getIs34HourResetAllowed();
            	weeklyResetMinutes = US_WEEKLY_TOTAL_RESET_HOUR_THRESHOLD;
            	break;
        	case RuleSetTypeEnum.FLORIDA_7DAY:
            	startDate = DateUtility.AddDays(endDate, -6);
                performWeeklyTotalReset = user.getIs34HourResetAllowed();
                weeklyResetMinutes = US_WEEKLY_TOTAL_RESET_HOUR_THRESHOLD;
            	break;
        	case RuleSetTypeEnum.FLORIDA_8DAY:
            	startDate = DateUtility.AddDays(endDate, -7);
                performWeeklyTotalReset = user.getIs34HourResetAllowed();
                weeklyResetMinutes = US_WEEKLY_TOTAL_RESET_HOUR_THRESHOLD;
            	break;
			case RuleSetTypeEnum.WISCONSIN_7DAY:
				startDate = DateUtility.AddDays(endDate, -6);
				performWeeklyTotalReset = user.getIs34HourResetAllowed();
				weeklyResetMinutes = US_WEEKLY_TOTAL_RESET_HOUR_THRESHOLD;
				break;
			case RuleSetTypeEnum.WISCONSIN_8DAY:
				startDate = DateUtility.AddDays(endDate, -7);
				performWeeklyTotalReset = user.getIs34HourResetAllowed();
				weeklyResetMinutes = US_WEEKLY_TOTAL_RESET_HOUR_THRESHOLD;
				break;
        	case RuleSetTypeEnum.USOILFIELD:
			case RuleSetTypeEnum.TEXASOILFIELD:
        		startDate = DateUtility.AddDays(endDate, -7);
        		performWeeklyTotalReset = user.getIs34HourResetAllowed();
        		weeklyResetMinutes = (isFallDaylightSavingsTransition(logSummary.getLogDate()) ? TWENTY_FOUR_HOUR_PERIOD + ONE_HOUR_PERIOD : TWENTY_FOUR_HOUR_PERIOD);
        		break;
			case RuleSetTypeEnum.USCONSTRUCTION_7DAY:
				startDate = DateUtility.AddDays(endDate, -6);
				performWeeklyTotalReset = user.getIs34HourResetAllowed();
				weeklyResetMinutes = US_WEEKLY_TOTAL_RESET_HOUR_THRESHOLD;
				break;
			case RuleSetTypeEnum.USCONSTRUCTION_8DAY:
				startDate = DateUtility.AddDays(endDate, -7);
				performWeeklyTotalReset = user.getIs34HourResetAllowed();
				weeklyResetMinutes = US_WEEKLY_TOTAL_RESET_HOUR_THRESHOLD;
				break;
			case RuleSetTypeEnum.CALIFORNIA_MP_80:
				startDate = DateUtility.AddDays(endDate, -7);
				performWeeklyTotalReset = user.getIs34HourResetAllowed();
				weeklyResetMinutes = US_WEEKLY_TOTAL_RESET_HOUR_THRESHOLD;
        	default:
        		break;
        }

        long totalHoursOnDutyInPeriodMilliseconds = 0;
        LogSummary currentSummary = null;
        int totalContiguousOffDutyHours = 0;
        Date currentLogDate = startDate;
        while (currentLogDate.compareTo(endDate) <= 0)
        {
            if (logSummaries.containsKey(currentLogDate))
            {
                // there is a summary for the current log date being processed
                currentSummary = logSummaries.get(currentLogDate);

                // does the summary have valid data in it?
                if (currentSummary.getIsComplete())
                {
                    // keep track of the contiguous hours spent in off-duty status
                    // cumulate the off-duty hours from the start of the log
                    totalContiguousOffDutyHours += currentSummary.getOffDutyStart();

                    if (performWeeklyTotalReset && totalContiguousOffDutyHours >= weeklyResetMinutes)
                    {
                        // the total contiguous hours has exceeded the reset count,
                        // and the driver type is PropertyCarrying
                        // so the total hours in audit period is should be reset to 0
                        // for PropertyCarrying accumulator
                        totalHoursOnDutyInPeriodMilliseconds = 0;
                    }

                    if (currentSummary.getOffDutyStart() < (isFallDaylightSavingsTransition(currentLogDate) ? TWENTY_FOUR_HOUR_PERIOD + ONE_HOUR_PERIOD : TWENTY_FOUR_HOUR_PERIOD))
                    {
                        // not a complete day off-duty, so the reset period 
                        // starts with the total off duty chunk from the end of the log
                        totalContiguousOffDutyHours = currentSummary.getOffDutyEnd();
                    }

                    // accumulate the current daily total for the summary
                    totalHoursOnDutyInPeriodMilliseconds += currentSummary.getDailyDurationTotal() != null ? currentSummary.getDailyDurationTotal() : 0;
                }
                else
                {
                    // no summary found for the date being auditted
                    // reset the contiguous hour counter
                    totalContiguousOffDutyHours = 0;
                }
            }

            currentLogDate = DateUtility.AddDays(currentLogDate, 1);
        }

        // save weekly total for the summary
        logSummary.setWeeklyTotalDuration(totalHoursOnDutyInPeriodMilliseconds);
    }
    
    private boolean isFallDaylightSavingsTransition(Date date)
    {
        TimeZone tz = GlobalState.getInstance().getCurrentUser().getHomeTerminalTimeZone().toTimeZone();
    	Date nextDay = DateUtility.AddDays(date, 1);    	
    	if(tz.inDaylightTime(date) && !tz.inDaylightTime(nextDay))
    	{
    		return true;     		
    	}    	
    	return false; 
    }
}
