package com.jjkeller.kmbapi.calcengine;

import com.jjkeller.kmbapi.calcengine.Enums.SearchDirectionTypeEnum;
import com.jjkeller.kmbapi.controller.interfaces.IEmployeeLogFacade;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.controller.utility.EmployeeLogUtilities;
import com.jjkeller.kmbapi.controller.utility.GenericEventComparer;
import com.jjkeller.kmbapi.employeelogeldevents.EmployeeLogEldEventCode;
import com.jjkeller.kmbapi.enums.DriverTypeEnum;
import com.jjkeller.kmbapi.enums.DutyStatusEnum;
import com.jjkeller.kmbapi.enums.ExemptLogTypeEnum;
import com.jjkeller.kmbapi.enums.RuleSetTypeEnum;
import com.jjkeller.kmbapi.proxydata.EldEventAdapterList;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEventList;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public abstract class ExemptLogValidatorBase {
	
	private EmployeeLogEldEventList _logEventList;
	protected EmployeeLogEldEventList getEldEventList() { return _logEventList; }
	protected void setEldEventList(EmployeeLogEldEventList value) { _logEventList = value;	}
	protected EmployeeLogEldEvent[] getLogEvents() { return _logEventList.getEldEventList(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus); }
	
	private int _startLogEventIndex = -1;
	protected int getStartLogEventIndex() {	return _startLogEventIndex;	}
	protected void setStartLogEventIndex(int value) { _startLogEventIndex = value; }

	private int _endLogEventIndex = -1;
	protected int getEndLogEventIndex() { return _endLogEventIndex;	}
	protected void setEndLogEventIndex(int _endLogEventIndex) { this._endLogEventIndex = _endLogEventIndex;	}

	private List<Integer> _startOfDutyTourEventIndices = new ArrayList<Integer>();
	protected List<Integer> getStartOfDutyTourEventIndices() { return _startOfDutyTourEventIndices; }
	protected void setStartOfDutyTourEventIndices(List<Integer> _startOfDutyTourEventIndices) { this._startOfDutyTourEventIndices = _startOfDutyTourEventIndices; }

	private Date _auditDate;
	protected Date getAuditDate() { return _auditDate; }
	protected void setAuditDate(Date _auditDate) { this._auditDate = _auditDate; }

	private String _employeeId;
	protected String getEmployeeId() { return _employeeId; }
	protected void setEmployeeId(String _employeeId) { this._employeeId = _employeeId; }

	private EmployeeLog _currentLog;
	protected EmployeeLog getCurrentLog() { return _currentLog; }
	protected void setCurrentLog(EmployeeLog _currentLog) {this._currentLog = _currentLog;	}
	
	private EmployeeLogEldEventList _currentLogEvents;
	protected EmployeeLogEldEventList getCurrentLogEvents() { return _currentLogEvents; }
	protected void setCurrentLogEvents(EmployeeLogEldEventList value) {this._currentLogEvents = value; }
	
	protected IEmployeeLogFacade _empLogFacade;
	
	protected ExemptLogValidatorBase(IEmployeeLogFacade employeeLogFacade)
	{
		_empLogFacade = employeeLogFacade;
	}
	
	protected void Initialize(EmployeeLog currentLog, EmployeeLogEldEventList currentLogEvents, Date endingTime)
	{
		this.setCurrentLog(currentLog);
        this.setAuditDate(currentLog.getLogDate());
        this.setEmployeeId(currentLog.getEmployeeId());
        this.setCurrentLogEvents(currentLogEvents);
        
        this.setEldEventList(this.CreateLogEventList());

        //Note: we need to add the log events to the logEventList going backwards in time
        //to address an issue where 0-duration driving events at midnight were overwriting
        //the actual duty statuses of midnight events on the next day's logs.
        //E.g. - Day 1 has Driving at 8:30 PM, behind the scenes Encompass puts another driving event actually at midnight on Day 2, but belonging to Day 2's log
        //     - Day 2 has On Duty at 12:00 AM, but if Day 2's events are merged into Day 1's, then this midnight event loses out since there was already a Day 2 midnight event in the list
        
        // get the next day's log, and add those events to the collection
        EmployeeLog nextLog = _empLogFacade.GetLogByDate(DateUtility.AddDays(currentLog.getLogDate(), 1));
        if (nextLog != null)
        {
            EmployeeLogEldEventList nextLogEvents = nextLog.getEldEventList();

            this.setEldEventList(EmployeeLogUtilities.CombineLogEvents(this.getEldEventList(), nextLogEvents));
        }
        else
        {
            this.AddMissingLogEvent(DateUtility.AddDays(currentLog.getLogDate(), 1));
        }


        // add the current log's events.
        // note: the current log's events will not be loaded from the database because the current log has not been saved yet
        this.setEldEventList(EmployeeLogUtilities.CombineLogEvents(this.getEldEventList(), currentLogEvents));
        
        // add the previous log's events to the collection
        EmployeeLog previousLog = _empLogFacade.GetLogByDate(DateUtility.AddDays(currentLog.getLogDate(), -1));
        if (previousLog != null)
        {
            EmployeeLogEldEventList previousLogEvents = previousLog.getEldEventList();
            this.setEldEventList(EmployeeLogUtilities.CombineLogEvents(this.getEldEventList(), previousLogEvents));
        }
        else
        {
            this.AddMissingLogEvent(DateUtility.AddDays(currentLog.getLogDate(), -1));
        }

        // add an off-duty event as of the endingTime so that we the logEvent just prior to the endingTime has a shortened duration
        // This can be used to determine eligibility as if the current log ended at the endingTime
        if(endingTime != null) 
        {
            int mandateStatusAsInt = EmployeeLogEldEvent.translateDutyStatusEnumToMandateStatus(new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
            EmployeeLogEldEvent offDutyEvent = new EmployeeLogEldEvent(endingTime, new EmployeeLogEldEventCode(mandateStatusAsInt), com.jjkeller.kmbapi.employeelogeldevents.Enums.EmployeeLogEldEventType.DutyStatusChange);
            offDutyEvent.setRulesetType(this.getCurrentLog().getRuleset());
            this.setEldEventList(EmployeeLogUtilities.CombineLogEvents(this.getEldEventList(), offDutyEvent));
        }

        // Sort the Array Ascending
        List<EmployeeLogEldEvent> newList = new LinkedList<>(Arrays.asList(this.getEldEventList().getEldEventList(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus)));
        Collections.sort(newList, new GenericEventComparer());

        // put the event list back in the log
        this.getEldEventList().setEldEventList(newList.toArray(new EmployeeLogEldEvent[newList.size()]));
        
        this.setStartLogEventIndex(0);
        this.setEndLogEventIndex(this.getEldEventList().getEldEventList(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus).length - 1);
	}
	
	protected boolean Calc_ExemptLogDrivingTime(int startOfDutyTourEventIndex, double drivingLimitHours, double breakHours)
    {
		EmployeeLogEldEvent startOfDutyTourLogEvent = this.getLogEvents()[startOfDutyTourEventIndex];
        int currentLogEventIndex = startOfDutyTourEventIndex;
        boolean violationExists = false;        
        long driveTimeAccumulator = 0L; 

        // loop through the log events
        while (currentLogEventIndex < this.getEndLogEventIndex())
        {
        	EmployeeLogEldEvent currentLogEvent = this.getLogEvents()[currentLogEventIndex];
        	DutyStatusEnum dutyStatus = currentLogEvent.getDutyStatusEnum();
            if (dutyStatus.getValue() == DutyStatusEnum.DRIVING)
            {
                // for a DRV event, accumulate the durations
                // it is a violation to be over the driving limit
                driveTimeAccumulator += GetDuration(this.getLogEvents(), currentLogEventIndex);
                if (DateUtility.ConvertMillisecondsToHours(driveTimeAccumulator) > drivingLimitHours)
                {
                    // this will only be a violation if log event is for the current log being edited or if the current log with driving spanning multiple days
                    if ( (DateUtility.GetDateFromDateTime(currentLogEvent.getStartTime()).compareTo(this.getAuditDate()) == 0) 
                    		|| (this.getCurrentLog().getExemptLogType().getValue() != ExemptLogTypeEnum.NULL && DateUtility.GetDateFromDateTime(startOfDutyTourLogEvent.getStartTime()).compareTo(this.getAuditDate()) == 0))
                    {
                        violationExists = true;
                        break;
                    }
                }
            }
            else if (currentLogEvent.isExemptOffDutyStatus())
            {
                // if a full break is found, then stop looking for any more driving time
                if (this.Calc_ConsecutiveOffDutyPeriod(currentLogEventIndex, breakHours, SearchDirectionTypeEnum.Forward))
                {
                    break;
                }
            }

            currentLogEventIndex++;
        }

        return violationExists;
    }
	
	/// <summary>
    /// Determine if a contiguous off duty period exists (any combination of off duty and sleeper) that
    /// has a duration equal to or greater than the identified consecutive hours
    /// </summary>
    /// <param name="logEventIndex">Index into log event collection of event we are processing</param>
    /// <param name="consecutiveHours">Number of consecutive hours we are looking for</param>
    /// <param name="searchDirection">Direction to search in the log event collection</param>
    /// <returns></returns>
    protected boolean Calc_ConsecutiveOffDutyPeriod(int logEventIndex, double consecutiveHours, SearchDirectionTypeEnum searchDirection)
    {
        // if current event duration >= number of break hours required, this event is
        // a full break - return true
        if (DateUtility.ConvertMillisecondsToHours(GetDuration(this.getLogEvents(), logEventIndex)) >= consecutiveHours)
        {
            return true;
        }
        // if a missing log assumes a full break,we are at the beginning of a log (prior log must not exist), set return value
        // to true - 
        else if (searchDirection == SearchDirectionTypeEnum.Backward && logEventIndex == 0 
        		|| (searchDirection == SearchDirectionTypeEnum.Forward && logEventIndex == this.getEldEventList().getEldEventList(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus).length))
        {
            return true;
        }
        // else, determine if there is a contiguous break of off duty and/or sleeper time
        else
        {
            int newLogEventIndex;
            long totalDuration = 0L;

            switch (searchDirection)
            {
                case Backward:
                    totalDuration = GetDuration(this.getLogEvents(), logEventIndex);
                    newLogEventIndex = logEventIndex - 1;
                    break;
                case Forward:
                    totalDuration = GetDuration(this.getLogEvents(), logEventIndex);
                    newLogEventIndex = logEventIndex + 1;
                    break;
                default:
                    newLogEventIndex = logEventIndex;
                    while (newLogEventIndex - 1 >= 0)
                    {
                        if (this.getLogEvents()[newLogEventIndex - 1].isExemptOffDutyStatus())
                            newLogEventIndex--;
                        else
                            break;
                    }

                    searchDirection = SearchDirectionTypeEnum.Forward;
                    break;
            }

            while (true)
            {
                if ((searchDirection == SearchDirectionTypeEnum.Backward && newLogEventIndex < 0) ||
                    (searchDirection == SearchDirectionTypeEnum.Forward && 
                    newLogEventIndex >= this.getEldEventList().getEldEventList(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus).length))
                {
                    break;
                }
                else  if (this.getLogEvents()[newLogEventIndex].isExemptOffDutyStatus())
                {
                    totalDuration += GetDuration(this.getLogEvents(), newLogEventIndex);

                    if (DateUtility.ConvertMillisecondsToHours(totalDuration) >= consecutiveHours)
                        return true;
                }
                else
                {
                    break;
                }

                if (searchDirection == SearchDirectionTypeEnum.Backward)
                {
                	// if we get to the beginning event (prior log doesn't exist), return
                    // missingLogAssumesBreak value
                    if (newLogEventIndex == 0)
                        return true;
                    else
                        newLogEventIndex--;
                }
                else
                {
                    newLogEventIndex++;
                }
            }
        }

        return false;
    }
	
	protected EmployeeLogEldEventList CreateLogEventList()
    {
        EmployeeLogEldEventList logEventCollection = new EmployeeLogEldEventList();
        
        return logEventCollection;
    }
	
	private void AddMissingLogEvent(Date logDate)
    {
        EmployeeLogEldEvent offDutyEvent = new EmployeeLogEldEvent(logDate, new EmployeeLogEldEventCode(EmployeeLogEldEvent.translateDutyStatusEnumToMandateStatus(new DutyStatusEnum(DutyStatusEnum.OFFDUTY))), com.jjkeller.kmbapi.employeelogeldevents.Enums.EmployeeLogEldEventType.DutyStatusChange);
        offDutyEvent.setRulesetType(this.getCurrentLog().getRuleset());
        
        this.setEldEventList(EmployeeLogUtilities.CombineLogEvents(this.getEldEventList(), offDutyEvent));
    }
	
	protected static boolean IsValidRulesetType(EmployeeLogEldEventList currentLogEvents)
    {
        for (int i = 0; i < currentLogEvents.getEldEventList(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus).length; i++)
        {
        	RuleSetTypeEnum rulesetType = currentLogEvents.getEldEventList(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus)[i].getRulesetType();
            if (rulesetType.getValue() != RuleSetTypeEnum.US60HOUR && rulesetType.getValue() != RuleSetTypeEnum.US70HOUR)
                return false;
        }

        return true;
    }
	
	protected static boolean IsValidRulesetType(EmployeeLog employeeLog)
	{
		return employeeLog.getRuleset().getValue() == RuleSetTypeEnum.US60HOUR 
				|| employeeLog.getRuleset().getValue() == RuleSetTypeEnum.US70HOUR;
	}
	
	public static Date GetEndTime(EmployeeLogEldEvent[] logEvents, int currentIndex)
	{
		// if this is the last item, set the end time to the start time, so essentially 0 duration
		if (currentIndex >= logEvents.length - 1) 
			return logEvents[logEvents.length - 1].getStartTime();

        EmployeeLogEldEvent nextLogEvent = logEvents[currentIndex + 1];
		
		return nextLogEvent.getStartTime();
	}
	
	public static long GetDuration(EmployeeLogEldEvent[] logEvents, int currentIndex)
	{
		long duration = GetEndTime(logEvents, currentIndex).getTime() - logEvents[currentIndex].getStartTime().getTime();
		
		return duration;
	}
	
	public boolean IsExemptLogEligible(EmployeeLog employeeLog, EmployeeLogEldEventList currentLogEvents) throws ParseException
	{
		return this.IsExemptLogEligible(employeeLog, currentLogEvents, null); 
	}
	
	public boolean IsExemptLogEligible(EmployeeLog employeeLog, EmployeeLogEldEventList currentLogEvents, Date endingTime) throws ParseException
	{
		if (!employeeLog.getHasReturnedToLocation())
			return false;
		
		this.Initialize(employeeLog, currentLogEvents, endingTime);
		
		if (!IsValidRulesetType(employeeLog))
			// if the current log contains log events for invalid ruleset types, then this is is not a valid Exempt Log
			return false;
		
		double requiredOffDutyTime = employeeLog.getDriverType().getValue() == DriverTypeEnum.PROPERTYCARRYING ? 10.0d : 8.0d;
		double driveTimeLimit =  employeeLog.getDriverType().getValue() == DriverTypeEnum.PROPERTYCARRYING ? 11.0d : 10.0d;
				
		boolean isValid = Validate(requiredOffDutyTime, driveTimeLimit);
		
		return isValid;
	}
	
	protected abstract boolean Validate(double requiredOffDutyTime, double driveTimeLimit) throws ParseException;
}
