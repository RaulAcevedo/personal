package com.jjkeller.kmbapi.calcengine;

import com.jjkeller.kmbapi.calcengine.Enums.SearchDirectionTypeEnum;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.dataaccess.EmployeeLogFacade;
import com.jjkeller.kmbapi.controller.interfaces.IEmployeeLogFacade;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;

import java.text.ParseException;
import java.util.Date;

public class Exempt100AirMileLogValidator extends ExemptLogValidatorBase implements IExemptLogValidator 
{
	
    public Exempt100AirMileLogValidator()
    {
    	super(new EmployeeLogFacade(GlobalState.getInstance().getApplicationContext(), GlobalState.getInstance().getCurrentUser()));
    }
    
    public Exempt100AirMileLogValidator(IEmployeeLogFacade empLogFacade)
    {
    	super(empLogFacade);
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
			if (this.getLogEvents()[currentLogEventIndex].isExemptOnDutyStatus())
			{
				boolean violationExists = Calc_CheckForExemptDutyViolation(currentLogEventIndex, requiredOffDutyTime, 12.0);
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
		
        return true;
	}
	
    
    public Date DetermineValidationAuditStartDateFor(EmployeeLog empLog)  { 
    	Date answer = null; 
    	
    	if(empLog != null) 
    	{ 
    		// for 100 air mile exempt, it's always just the 1 day prior to the current log that we need
    		answer = DateUtility.AddDays(empLog.getLogDate(), -1); 
    	}
    	
    	return answer; 
    }
	
    /// <summary>
    /// Perform audit for exempt duty violation starting at an On Duty or Driving event.
    /// Return true if the exempt log is in violation and false otherwise.
    /// </summary>
    /// <param name="logEventIndex">The index of an On Duty or Driving event to start at</param>
    /// <param name="breakHours">The number of break hours required for a full break</param>
    /// <param name="dutyHours">The maximum number of duty hours allowed per duty tour</param>
    /// <returns>true if the exempt log is in violation and false otherwise</returns>
    private boolean Calc_CheckForExemptDutyViolation(int logEventIndex, double breakHours, double dutyHours) throws ParseException
	{
		int currentLogEventIndex = logEventIndex - 1;
        boolean violationExists = false;

        // loop through the prior log events
        while (currentLogEventIndex >= this.getStartLogEventIndex())
        {
            Date onDutyStartTime = new Date(0L);

            // if processing an off duty period or a sleeper period, check for a full break
            if (this.getLogEvents()[currentLogEventIndex].isExemptOffDutyStatus())
            {
                // if a full break is found, determine if we are in violation from the end time
                // of the full break to the end time of the current event
                if (this.Calc_ConsecutiveOffDutyPeriod(currentLogEventIndex, breakHours, SearchDirectionTypeEnum.Backward))
                {
                    onDutyStartTime = GetEndTime(this.getLogEvents(), currentLogEventIndex);

                    int startOfDutyTourIndex = currentLogEventIndex + 1;
                    this.getStartOfDutyTourEventIndices().add(startOfDutyTourIndex);

                    // if end of current event is more than 12 hours after end of full break,
                    // there is a violation 
                    if (DateUtility.ConvertMillisecondsToHours(GetEndTime(this.getLogEvents(), logEventIndex).getTime() - onDutyStartTime.getTime()) > dutyHours)
                    {
                        // this will only be a violation if start of the duty tour is for the current log being edited,
                        // or if the current log event being evaluated is on the current log
                        if (DateUtility.GetDateFromDateTime(this.getLogEvents()[startOfDutyTourIndex].getStartTime()).compareTo(this.getAuditDate()) == 0 ||
                        		DateUtility.GetDateFromDateTime(this.getLogEvents()[logEventIndex].getStartTime()).compareTo(this.getAuditDate()) == 0)
                            violationExists = true;
                    }

                    break;
                }
                else
                {
                    onDutyStartTime = GetEndTime(this.getLogEvents(), currentLogEventIndex);

                    // if the end of this sleeper period is more than 12 hours prior to the beginning
                    // of the current event, we know we are in violation at the start of the current
                    // event, we can stop looking
                    if (DateUtility.ConvertMillisecondsToHours(GetEndTime(this.getLogEvents(), logEventIndex).getTime() - onDutyStartTime.getTime()) > dutyHours)
                    {
                        return true;
                    }
                }
            }

            currentLogEventIndex--;
        }

        return violationExists;
	}

}
