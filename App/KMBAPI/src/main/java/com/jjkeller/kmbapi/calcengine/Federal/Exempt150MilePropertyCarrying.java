package com.jjkeller.kmbapi.calcengine.Federal;

import android.os.Bundle;

import com.jjkeller.kmbapi.calcengine.DutyInfo;
import com.jjkeller.kmbapi.calcengine.HoursOfServiceSummary;
import com.jjkeller.kmbapi.calcengine.LogProperties;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.ILogCheckerComplianceDatesController;
import com.jjkeller.kmbapi.controller.dataaccess.EmployeeLogFacade;
import com.jjkeller.kmbapi.controller.interfaces.IEmployeeLogFacade;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.controller.utility.EmployeeLogUtilities;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;

import java.util.Date;
import java.util.Enumeration;

public abstract class Exempt150MilePropertyCarrying extends PropertyCarrying {

	protected long DAILY_DUTY_TIME_ALLOWED_WITH_NON_CDL_EXEMPTION;
	
	private static final String REGULATORY_SECTION_DAILY_DUTY_150Mile = "395.1(e)(2)";
	
	protected Exempt150MilePropertyCarrying(com.jjkeller.kmbapi.calcengine.RulesetProperties properties) {
		super(properties);
	}
	
	protected Exempt150MilePropertyCarrying(com.jjkeller.kmbapi.calcengine.RulesetProperties properties, ILogCheckerComplianceDatesController complianceDateController) {
		super(properties, complianceDateController);
	} 
	
	@Override
	public void PrepareStartOfLog(LogProperties logProperties)
	{
		super.PrepareStartOfLog(logProperties);
		
		if (CanExceptionBeUsed())
		{
			this.DAILY_DUTY_TIME_ALLOWED = this.DAILY_DUTY_TIME_ALLOWED_WITH_NON_CDL_EXEMPTION;
		}
		else
		{
			this.DAILY_DUTY_TIME_ALLOWED = this.DAILY_DUTY_TIME_ALLOWED_STANDARD;
		}
	}

    @Override
    public Bundle DailyDutySummary(HoursOfServiceSummary summary)
    {
    	Bundle bundle = super.DailyDutySummary(summary);
    	bundle.putString(REGSECTION, REGULATORY_SECTION_DAILY_DUTY_150Mile);
    	return bundle;
    }
	
	/**
	 * Process an on-duty status event.
	 * Accumulate the duty time accordingly.
	 */
	@Override
    protected void ProcessOnDuty(long length)
    {
        this.Summary.setDutyTimeAccumulated(this.Summary.getDutyTimeAccumulated() + length);
        this.Summary.setWeeklyDutyTimeAccumulated(this.Summary.getWeeklyDutyTimeAccumulated() + length);
        this.Summary.setDailyResetAmount(DAILY_OFFDUTY_HOURS_FOR_RESET);
        this.Summary.setWeeklyResetAmount(WEEKLY_OFFDUTY_HOURS_FOR_RESET);
        this.setOriginalValidWeeklyResetDutyStartTimestamp(null);

        this.MarkAsDutyTour();

        this.Summary.getCombinableOffDutyPeriod().ProcessOnDutyTime(length);
    }
	
	/**
	 * Process the off-duty event.
	 */
	@Override
    protected void ProcessOffDuty(long length)
    {
		// when less than 10 hours, accumulate as a duty event
        if (DateUtility.ConvertMillisecondsToHours(length) < 10.0)
            this.Summary.setDutyTimeAccumulated(this.Summary.getDutyTimeAccumulated() + length);

        if (this.getOriginalValidWeeklyResetDutyStartTimestamp() == null)
        	this.setOriginalValidWeeklyResetDutyStartTimestamp(this.Summary.getRecentDutyTimestamp());
        
        this.CalculateDailyReset(length);
        this.CalculateWeeklyReset(length);
    }
	
	private boolean CanExceptionBeUsed()
	{
		boolean canExceptionBeUsed = true;

        Date logDate = DateUtility.GetDateFromDateTime(this.Summary.getLogStartTimestamp());
        boolean done = false;
        int logDaysExamined = 0;
        int nonCDLExempts = 0;
        
        DutyInfo dutyInfo = this.getDutyInfoByDate().get(logDate);

        // unless working with today's log, the short haul exception can not be used
        if (dutyInfo != null)
        {
            if (!dutyInfo.getIsTodaysLog())
            {
                done = true;
                canExceptionBeUsed = false;
            }
        }

        boolean checkExceptionUsed = true;
        while (!done)
        {
            // try to look chronologically backwards for one of the following:
            // 1. a weekly reset
            // 2. a non-cdl exception that's used within the last 7 days (excluding today)
            logDaysExamined++;
            
            // Populate the DutyInfoByDate if it doesn't exist
            if (this.getDutyInfoByDate().containsKey(logDate) == false) {
            	IEmployeeLogFacade empLogFacade = new EmployeeLogFacade(GlobalState.getInstance().getApplicationContext(), GlobalState.getInstance().getCurrentUser());
            	EmployeeLog empLog = null;
            	
            	try 
            	{
            		empLog = empLogFacade.GetLogByDate(logDate);
            	}
            	catch(Exception ex) 
				{
            		// if something bad happens getting the log, just ignore it
            		// this catch handler is primarily in place to allow the unit tests to run
            	}
            	
            	if (empLog != null) {
            		dutyInfo = new DutyInfo();
            		
            		// set the duty info properties from this log
            		dutyInfo.setHasReturnedToWorkLocation(empLog.getHasReturnedToLocation());
            		dutyInfo.setShortHaulExceptionUsed(empLog.getIsShortHaulExceptionUsed());
            		dutyInfo.setIsDutyTour(EmployeeLogUtilities.ContainsDrivingOrOnDutyEvent(empLog));
            		dutyInfo.setIsNonCDLShortHaulExceptionUsed(empLog.getIsNonCDLShortHaulExceptionUsed());
            		
            		this.getDutyInfoByDate().put(logDate, dutyInfo);
            	}
            	else
            	{
            		// no log was found for the date, are there any more in the db that are earlier than this logDate
            		boolean anyMoreLogsExist = false;
                    
            		try
					{
            			anyMoreLogsExist = empLogFacade.ExistAnyLogEarlierThan(logDate);
            		}
                	catch(Exception ex) {
                		// if something bad happens getting the log, just ignore it
                		// this catch handler is primarily in place to allow the unit tests to run
                	}
            		
            		if(anyMoreLogsExist == false) {
            			// no more exist in the database, check to see if they're in the duty info list
            			// this is primarily so that the unit tests can load up the duty info array
            			// try to detect an earlier logDate in the dutyInfo list than the current one we're working with
                        
            			for(Enumeration<Date> e = this.getDutyInfoByDate().keys(); e.hasMoreElements();) {
            				Date dte = e.nextElement();
            				if (dte.compareTo(logDate) < 0){
            					anyMoreLogsExist = true;
            					break;
            				}
            			}
            		}
            		
            		if(anyMoreLogsExist == false){
            			// if no more logs exist earlier than this in the database, or in the dutyInfo array, then we're done
            			done = true;
            		}
            	}
            }
            
            if (dutyInfo != null)
            {
                if (!done && checkExceptionUsed)
                {
                	if (dutyInfo.getIsNonCDLShortHaulExceptionUsed() && !dutyInfo.getIsTodaysLog())
                		nonCDLExempts++;
                	
                	if (logDaysExamined <= 7 && nonCDLExempts == 2)
                    {
                        // found a usage of the short-haul exception within the last 7 days
                        // and it's not today's log
                        canExceptionBeUsed = false;
                    }
                }
                
                // if we find a weekly reset, we reset the counter to zero
                if (!done && checkExceptionUsed && this.getWeeklyResetEndingTimestamp() != null &&
					this.getWeeklyResetEndingTimestamp() == logDate)
                {
                    nonCDLExempts = 0;
                }
            }
            
            logDate = DateUtility.AddDays(logDate, -1);
            dutyInfo = this.getDutyInfoByDate().get(logDate);
            
            // if we figure out that the exception cannot be used, then we're done
            if (!done && !canExceptionBeUsed) done = true;
        }

        return canExceptionBeUsed;
	}
}
