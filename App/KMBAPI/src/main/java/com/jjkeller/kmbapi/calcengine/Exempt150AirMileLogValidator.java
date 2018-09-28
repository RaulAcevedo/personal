package com.jjkeller.kmbapi.calcengine;

import com.jjkeller.kmbapi.calcengine.Enums.SearchDirectionTypeEnum;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.LogEntryController;
import com.jjkeller.kmbapi.controller.dataaccess.EmployeeLogFacade;
import com.jjkeller.kmbapi.controller.interfaces.IEmployeeLogFacade;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.controller.utility.EmployeeLogUtilities;
import com.jjkeller.kmbapi.controller.utility.GenericEventComparer;
import com.jjkeller.kmbapi.enums.DriverTypeEnum;
import com.jjkeller.kmbapi.enums.DutyStatusEnum;
import com.jjkeller.kmbapi.proxydata.EldEventAdapterList;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEventList;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Exempt150AirMileLogValidator extends ExemptLogValidatorBase implements IExemptLogValidator 
{
	private static final double FullWeeklyReset = 34.0d;

    private boolean _isAttemptToUseNonCDLShortHaulException = false;
    boolean getIsAttemptToUseNonCDLShortHaulException() { return _isAttemptToUseNonCDLShortHaulException; }
    protected void setIsAttemptToUseNonCDLShortHaulException(boolean value) { _isAttemptToUseNonCDLShortHaulException = value; }

    public Exempt150AirMileLogValidator()
    {
    	super(new EmployeeLogFacade(GlobalState.getInstance().getApplicationContext(), GlobalState.getInstance().getCurrentUser()));
    }
    
    public Exempt150AirMileLogValidator(IEmployeeLogFacade empLogFacade)
    {
    	super(empLogFacade);
    }
    
    @Override
	public boolean IsExemptLogEligible(EmployeeLog employeeLog, EmployeeLogEldEventList currentLogEvents) throws ParseException
	{
		if (employeeLog.getDriverType().getValue() == DriverTypeEnum.PASSENGERCARRYING)
			// this exemption is not valid for passenger-carrying
            return false;
			
		return super.IsExemptLogEligible(employeeLog, currentLogEvents);
	}
	
	/// <summary>
    /// Determine if the log event collection is valid exempt log for the current audit date
    /// </summary>
    /// <returns></returns>
    @Override
	protected boolean Validate(double requiredOffDutyTime, double driveTimeLimit) throws ParseException
	{
		for (int currentLogEventIndex = this.getStartLogEventIndex(); currentLogEventIndex <= this.getEndLogEventIndex(); currentLogEventIndex++)
		{
			// note: the 150 air mile validation only cares about driving events
			if (this.getLogEvents()[currentLogEventIndex].getDutyStatusEnum().getValue() == DutyStatusEnum.DRIVING)
			{
				boolean violationExists = Calc_CheckForExemptDutyViolation(currentLogEventIndex, requiredOffDutyTime);
				if (violationExists) 
					return false;
			}
        }
			
		for (int startOfDutyTourIndex : this.getStartOfDutyTourEventIndices())
		{
			boolean drivingViolationExists = this.Calc_ExemptLogDrivingTime(startOfDutyTourIndex, driveTimeLimit, requiredOffDutyTime);
			if (drivingViolationExists) 
				return false;
		}
		
        if (this.getIsAttemptToUseNonCDLShortHaulException())
        {
            boolean shortHaulUsageViolationExists = this.ValidateNonCDLShortHaulExceptionUsage(this.getCurrentLog(), this.getCurrentLogEvents());
            if (shortHaulUsageViolationExists)
                return false;
        }

        return true;
	}
    
    
    public Date DetermineValidationAuditStartDateFor(EmployeeLog empLog)  { 
    	Date answer = null; 
    	
    	if(empLog != null) 
    	{ 
            // Jason Goodrich had a discussion with editorial discussing how far back we need to go
            // when determining whether or not prior logs need to be converted.  The bottom line is
            // that we'll never need to go back further than 1 day
    		answer = DateUtility.AddDays(empLog.getLogDate(), -1); 
    	}
    	
    	return answer; 
    }
	
    private boolean Calc_CheckForExemptDutyViolation(int logEventIndex, double breakHours) throws ParseException
	{
		int currentLogEventIndex = logEventIndex - 1;
        boolean violationExists = false;
        boolean fullBreakFound = false;
        
        Date endOfDrivingPeriod = GetEndTime(this.getLogEvents(), logEventIndex);
        Date latestEndOfDrivingTime = endOfDrivingPeriod;

        // loop through the prior log events
        while (currentLogEventIndex >= this.getStartLogEventIndex())
        {
            Date onDutyStartTime = new Date(0L);
            EmployeeLogEldEvent logEvent = this.getLogEvents()[currentLogEventIndex];

            // if processing an off duty period or a sleeper period, check for a full break
            if (this.getLogEvents()[currentLogEventIndex].isExemptOffDutyStatus())
            {
                // if a full break is found, determine if we are in violation from the end time
                // of the full break to the end time of the current event
                if (this.Calc_ConsecutiveOffDutyPeriod(currentLogEventIndex, breakHours, SearchDirectionTypeEnum.Backward))
                {
                	fullBreakFound = true;
                    onDutyStartTime = GetEndTime(this.getLogEvents(), currentLogEventIndex);

                    int startOfDutyTourIndex = currentLogEventIndex + 1;
                    this.getStartOfDutyTourEventIndices().add(startOfDutyTourIndex);

                    // if end of current event is more than 16 hours after end of full break,
                    // there is a violation
                    if (DateUtility.ConvertMillisecondsToHours(endOfDrivingPeriod.getTime() - onDutyStartTime.getTime()) > 16)
                    {
                        // this will only be a violation if start of the duty tour is for the current log being edited,
                        // or if the current log event being evaluated is on the current log
                        if (DateUtility.GetDateFromDateTime(this.getLogEvents()[startOfDutyTourIndex].getStartTime()).compareTo(this.getAuditDate()) == 0 ||
                        		DateUtility.GetDateFromDateTime(this.getLogEvents()[logEventIndex].getStartTime()).compareTo(this.getAuditDate()) == 0)
                            violationExists = true;
                    }
                    // else, if end of current event is more than 14 hours after end of full break,
                    // determine if we can use the non cdl shorthaul exception on this log
                    else if (DateUtility.ConvertMillisecondsToHours(endOfDrivingPeriod.getTime() - onDutyStartTime.getTime()) > 14)
                    {
                        // if on the current audit date, then keep track of the fact that an attempt to use short haul exception
                        if (DateUtility.GetDateFromDateTime(this.getLogEvents()[startOfDutyTourIndex].getStartTime()).compareTo(this.getAuditDate()) == 0 ||
                                DateUtility.GetDateFromDateTime(this.getLogEvents()[logEventIndex].getStartTime()).compareTo(this.getAuditDate()) == 0)
                            this.setIsAttemptToUseNonCDLShortHaulException(true);
                    }

                    break;
                }
                else
                {
                    onDutyStartTime = GetEndTime(this.getLogEvents(), currentLogEventIndex);

                    // if the end of the current off duty period is more than 16 hours prior to the end
                    // of the event we are processing, we know we are in violation, we can stop looking
                    if (DateUtility.ConvertMillisecondsToHours(endOfDrivingPeriod.getTime() - onDutyStartTime.getTime()) > 16)
                    {
                        return true;
                    }
                }
            }
            else
            {
            	if(logEvent.getDutyStatusEnum().getValue() == DutyStatusEnum.DRIVING)
            	{
            		Date endOfDriving = GetEndTime(this.getLogEvents(), currentLogEventIndex);
            		
            		//if this is a driving event we want to find the end of that, and keep track of the last
                    // (chronologically speaking) date so that we can compare it to the earliest event we know.
            		if(latestEndOfDrivingTime == null || latestEndOfDrivingTime.compareTo(endOfDriving) < 0)
            			latestEndOfDrivingTime = endOfDriving;
            	}
            }

            currentLogEventIndex--;
        }

        //if we didn't find a full break, check to see if we've had any driving
        //on the log for this audit date.  If so, we've had a violation. No full break
        //after analyzing yesterday's logs means this duty tour has lasted at least 24 hours.
        if(!fullBreakFound)
        {
            if(latestEndOfDrivingTime != null && DateUtility.GetDateFromDateTime(latestEndOfDrivingTime).compareTo(this.getAuditDate()) == 0)
                violationExists = true;
        }
        
        return violationExists;
	}
    
    private boolean ValidateNonCDLShortHaulExceptionUsage(EmployeeLog currentLog, EmployeeLogEldEventList currentLogEvents)
    {
        boolean violationExists = false;

        Map<Date, EmployeeLog> logs = new HashMap<Date, EmployeeLog>();
        EmployeeLogEldEventList logEventCollection = this.CreateLogEventList();

        // get the logs and events for the logs 6 days prior to the Audit Date (but not including the audit date)
        Date startDate = DateUtility.AddDays(this.getAuditDate(), -6);
        List<EmployeeLog> empLogDataBefore = _empLogFacade.GetLogListByDateRange(startDate, DateUtility.AddDays(this.getAuditDate(), -1));
        if (empLogDataBefore != null && empLogDataBefore.size() > 0)
        {
        	for(EmployeeLog log : empLogDataBefore)
        		logs.put(log.getLogDate(), log);

            EmployeeLogEldEventList empLogEventDataBefore = getEldEventList(empLogDataBefore);
            logEventCollection = EmployeeLogUtilities.CombineLogEvents(logEventCollection, empLogEventDataBefore);
        }

        int startOfAuditIndex = (logEventCollection.getEldEventList(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus) == null) ? 0 : logEventCollection.getEldEventList(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus).length;
        
        // now add the events for the Audit Date
        logs.put(currentLog.getLogDate(), currentLog);
        logEventCollection = EmployeeLogUtilities.CombineLogEvents(logEventCollection, currentLogEvents);

        // Note: The Encompass validator also looks at the logs in the future, as compared to the audit date.
        // We're not doing that in KMB, because the only log that can be edited in KMB is the current log -
        // there is no future log.
        
        Arrays.sort(logEventCollection.getEldEventList(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus), new GenericEventComparer());

        this.setEldEventList(logEventCollection);

        violationExists = this.Calc_VerifyShortHaulExceptionUsage(startOfAuditIndex, logs);

        return violationExists;
    }
    
    private EmployeeLogEldEventList getEldEventList(List<EmployeeLog> employeeLogs)
    {
    	List<EmployeeLogEldEvent> logEvents = new ArrayList<>();
    	for(EmployeeLog log: employeeLogs)
    	{
    		logEvents.addAll(new ArrayList<>(Arrays.asList(log.getEldEventList().getEldEventList(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus))));
    	}
    	
    	EmployeeLogEldEvent[] logEventsArray = new EmployeeLogEldEvent[logEvents.size()];
    	logEventsArray = logEvents.toArray(logEventsArray);
    	
    	EmployeeLogEldEventList logEventList = new EmployeeLogEldEventList();
    	logEventList.setEldEventList(logEventsArray);
    	
    	return logEventList;
    }
    
    private boolean Calc_VerifyShortHaulExceptionUsage(int auditStartIndex, Map<Date, EmployeeLog> logs)
    {
        boolean violationExists = false;

        // the rule we're trying to verify is there are no more than 2 short haul exceptions used within a 7 day period
        List<Date> shortHaulExceptionDates = this.GetShortHaulExceptionDates(auditStartIndex, logs);

        // make sure that the current day's log date is in the mix so that it will look at all of these together
        // it's possible that it's in there already
        if (!shortHaulExceptionDates.contains(this.getAuditDate()))
            shortHaulExceptionDates.add(this.getAuditDate());

        // if there is any less than two dates, it will always be valid, so skip the following part
        if (shortHaulExceptionDates.size() > 2)
        {
            // order the list
            Collections.sort(shortHaulExceptionDates);

            // look at each of the short haul exception date and verify that there are only two within a 7 day period
            double dayCount = 7.0d;
            for (int i = 0; i < shortHaulExceptionDates.size(); i++)
            {
                Date shortHaulExceptionDate = shortHaulExceptionDates.get(i);
                int countOfDates = this.CountOfDatesInRange(shortHaulExceptionDates, shortHaulExceptionDate, dayCount);
                if (countOfDates > 1)
                {
                    // if there is more than 1 shortHaulException date within the 7 day range of the checkdate, then a violation exists                       
                    violationExists = true;
                    break;
                }
            }
        }

        return violationExists;
    }

    private List<Date> GetShortHaulExceptionDates(int auditStartIndex, Map<Date, EmployeeLog> logs)
    {
    	LogEntryController logEntryController = new LogEntryController(GlobalState.getInstance().getApplicationContext());
    	
        List<Date> listOfDates = new ArrayList<Date>();
        for (int curLogEventIndex = 0; curLogEventIndex < this.getEldEventList().getEldEventList(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus).length; curLogEventIndex++)
        {
            if (this.getEldEventList().getEldEventList(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus)[curLogEventIndex].isExemptOffDutyStatus())
            {
                // determine if a 34 hour break is found
                if (this.Calc_ConsecutiveOffDutyPeriod(curLogEventIndex, FullWeeklyReset, SearchDirectionTypeEnum.Backward))
                {
                    if (curLogEventIndex <= auditStartIndex)
                    {
                        // when the reset is found prior to the current log, then clear out any short haul exceptions found so far
                        listOfDates.clear();
                    }
                    else
                    {
                        // when the reset is found after the current log, then stop processing and return to the caller
                        break;
                    }
                }
            }

            Date logDate = logEntryController.GetLogDateForEvent(getEldEventList().getEldEventList(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus)[curLogEventIndex]);
            EmployeeLog curLog = logs.get(logDate);
            
            if (curLog.getIsNonCDLShortHaulExceptionUsed() && !listOfDates.contains(logDate))
                listOfDates.add(logDate);
        }

        return listOfDates;
    }

    private int CountOfDatesInRange(List<Date> listOfDates, Date startDate, double dayCount)
    {
        int countOfItems = 0;

        //note: the dayCount is intended not to be inclusive
        //      For example, if the array 5/22, 5/25 and 5/29, with a startDate = 5/22
        //                   then 5/29 should not be counted

        Date endDate = DateUtility.AddDays(startDate, (int)dayCount - 1);
        for (Date sampleDate: listOfDates)
        {
            // note: the StartDate will already be in the list, so don't consider it a match
            if (startDate.compareTo(sampleDate) < 0 && sampleDate.compareTo(endDate) <= 0)
                countOfItems++;
        }

        return countOfItems;
    }
}
