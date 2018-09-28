package com.jjkeller.kmbapi.controller;

import android.content.Context;

import com.jjkeller.kmbapi.calcengine.ExemptLogValidatorFactory;
import com.jjkeller.kmbapi.calcengine.IExemptLogValidator;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.dataaccess.EmployeeLogFacade;
import com.jjkeller.kmbapi.controller.interfaces.IAPIController;
import com.jjkeller.kmbapi.controller.interfaces.IEmployeeLogFacade;
import com.jjkeller.kmbapi.controller.share.ControllerBase;
import com.jjkeller.kmbapi.controller.share.MandateObjectFactory;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.controller.utility.ErrorLogHelper;
import com.jjkeller.kmbapi.enums.ExemptLogTypeEnum;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ExemptLogValidationController extends ControllerBase {

	public ExemptLogValidationController(Context ctx){
		super(ctx);
	}
	
	public void PerformCompleteValidationForCurrentLog(EmployeeLog currentLog, boolean userBeenNotified) {
		
		if (this.getCurrentUser().getIsMobileExemptLogAllowed())
		{
			IAPIController empLogCtrlr = MandateObjectFactory.getInstance(this.getContext(),GlobalState.getInstance().getFeatureService()).getCurrentEventController();
			
			// Need to get the ExemptLogtype of the currentLog, Need this to create the proper validator
			// If the currentLog is converted to a Grid Log then the exemptLogTypeEnum = NULL and the wrong Validator can be created. 
			ExemptLogTypeEnum exemptLogType = currentLog.getExemptLogType(); 
			boolean isCurrentLogConverted = empLogCtrlr.TransitionExemptLogToStandardLogIfNecessary(currentLog,userBeenNotified); 
			
			List<Date> listOfIneligibleLogs = ValidatePreviousLogsForCurrentLogs(currentLog, exemptLogType);
			empLogCtrlr.TransitionPreviousLogsFromExemptToGridLog(listOfIneligibleLogs);
			
			if(isCurrentLogConverted || !listOfIneligibleLogs.isEmpty()) {
				empLogCtrlr.NotifyAboutTransitionCurrentLogFromExemptToGrid(currentLog, isCurrentLogConverted, listOfIneligibleLogs);
			}
			
		}
		
	}
	
	public List<Date> ValidatePreviousLogsForCurrentLogs(EmployeeLog currentLog, ExemptLogTypeEnum exemptLogType) { 
		
		List<Date> ineligibleLogList = new ArrayList<Date>();
		
		IExemptLogValidator validator = ExemptLogValidatorFactory.GetExemptLogValidator(exemptLogType);
    	if(validator != null)
    	{
    		 
    		try {
    			Date auditDateStart = validator.DetermineValidationAuditStartDateFor(currentLog);
    			if (auditDateStart != null) 
    			{ 
    				//note: the end date is one day prior to the current log because we don't need to include the current log 
    				//   in this validation process. 
    				Date auditDateEnd = DateUtility.AddDays(currentLog.getLogDate(), -1); 
    				ineligibleLogList = PeformPreviousLogValidationFromStart(auditDateStart, auditDateEnd); 
    			}
    		} catch (ParseException e) {
    			ErrorLogHelper.RecordException(getContext(), e);
    		}
    		
    	}
    	
		return ineligibleLogList; 
		
	}
	
	private List<Date> PeformPreviousLogValidationFromStart(Date startDate, Date endDate) { 
		
		List<Date> ineligibleLogList = new ArrayList<Date>(); 
		
		Date currentDate = endDate; 
		while (startDate.compareTo(currentDate) <=0)
		{
			IEmployeeLogFacade empLogFacade = new EmployeeLogFacade(GlobalState.getInstance().getApplicationContext(), GlobalState.getInstance().getCurrentUser()); 
			EmployeeLog empLog = empLogFacade.GetLogByDate(currentDate);

			// if log is already a Standard log - no need to check validator
			if (empLog.getExemptLogType().getValue() != ExemptLogTypeEnum.NULL) {
				IExemptLogValidator validator = ExemptLogValidatorFactory.GetValidatorOrDefault(empLog);
				if (validator != null) {
					Date asOfNow = DateUtility.getCurrentDateTimeUTC();
					asOfNow = DateUtility.AddMinutes(asOfNow, 1);

					try {
						boolean isExemptLogEligible = validator.IsExemptLogEligible(empLog, empLog.getEldEventList(), asOfNow);
						if (!isExemptLogEligible) {
							ineligibleLogList.add(empLog.getLogDate());
						}
					} catch (ParseException e) {
						ErrorLogHelper.RecordException(getContext(), e);
					}

				}
			}
			currentDate = DateUtility.AddDays(currentDate, -1); 
		}
		
		
		return ineligibleLogList; 
	}
	
	
	
}
