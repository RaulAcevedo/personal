package com.jjkeller.kmbapi.controller;

import android.content.Context;

import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.hos.HosAlertDriving;
import com.jjkeller.kmbapi.controller.hos.HosAlertExemptDuty;
import com.jjkeller.kmbapi.controller.hos.HosAlertRestBreak;
import com.jjkeller.kmbapi.controller.interfaces.IHosAlert;
import com.jjkeller.kmbapi.controller.share.ControllerBase;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.enums.ExemptLogTypeEnum;
import com.jjkeller.kmbapi.enums.HosAlertTimingEnum;
import com.jjkeller.kmbapi.enums.HosAlertTypeEnum;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HosAlertController extends ControllerBase {
	
	private static List<IHosAlert> _hosAlerts;
	
	private static final long ONE_HOUR = 3600000;
	private static final long TWO_HOURS = 2 * ONE_HOUR;
	private static final long HALF_HOUR = (long) (.5 * ONE_HOUR);
	
	public HosAlertController(Context ctx) {
		super(ctx);
		
		if(_hosAlerts == null)
		{
			_hosAlerts = new ArrayList<IHosAlert>();
			_hosAlerts.add(new HosAlertDriving(ctx));
			_hosAlerts.add(new HosAlertRestBreak(ctx));
			_hosAlerts.add(new HosAlertExemptDuty(ctx));
		}
	}


	public IHosAlert GetAlert(HosAlertTypeEnum alertType) {
		for(IHosAlert alert : _hosAlerts) {
			if(alert.getHosAlertTypeEnum() == alertType)
				return alert;
		}
		
		return null;
	}
	
	// / <summary>
	// / Answer if there is an alert that needs to be shown to the driver.
	// / </summary>
	public boolean AnyMessageRequiresConfirmation() {
		boolean confirm = false;
		
		IHosAlert earliestAlert = GetEarliestAlert();
		
		if(earliestAlert != null)
		{
			confirm = earliestAlert.getAlertRequiresConfirmation();
		}
		
		return confirm;
	}
	
	// <summary>
	// Generate the alert message for the driver.  If more than one violation is being tracked, answer the message for the one that expires first
	// If there is no alert necessary, then answer null.
	// </summary>
	public String AlertMessage() {
		String message = null;
		
		IHosAlert earliestAlert = GetEarliestAlert();
		
		if(earliestAlert != null) {			
			message = earliestAlert.getAlertMessage();
			
			//if there's a exempt alert that is expiring, check to see if we need to transition
			//the log from exempt to a standard grid log
			// note: this is an important section of code here.  
			// This is the spot where an exempt log is monitored in "real-time" and converted to grid if necessary
			if(earliestAlert.getAlertTime() != null)
			{
				ExemptLogTypeEnum currentExemptType = GlobalState.getInstance().getCurrentEmployeeLog().getExemptLogType();
				if(currentExemptType.getValue() != ExemptLogTypeEnum.NULL) {				
					if(DateUtility.getCurrentDateTimeUTC().compareTo(earliestAlert.getAlertTime()) >= 0) {
						ExemptLogValidationController controller = new ExemptLogValidationController(getContext());
						IHosAlert exemptAlert = GetAlert(HosAlertTypeEnum.ExemptDuty);
						// note: if the earliest alert is the exempt alert, then the driver will be notified through
						// this  notification alert
						boolean willDriverBeNotified = earliestAlert == exemptAlert;
						controller.PerformCompleteValidationForCurrentLog(GlobalState.getInstance().getCurrentEmployeeLog(), willDriverBeNotified);
					}
				}				
			}
			
			if(earliestAlert.getAlertTimingEnum() == HosAlertTimingEnum.None)
				// if the alert ever gets to None, we're done alerting on this so reset the alert time
				earliestAlert.Clear();
		}
		
		return message;
	}
		
	private IHosAlert GetEarliestAlert()
	{
		Collections.sort(_hosAlerts, new Comparator<IHosAlert>() {

			public int compare(IHosAlert alert1, IHosAlert alert2) {
				if(alert1.getAlertTime() == null && alert2.getAlertTime() != null)
					// only alert time for the second one
					return 1;
				else if(alert1.getAlertTime() != null && alert2.getAlertTime() == null)
					// only alert time for the first one
					return -1;
				else if(alert1.getAlertTime() == null && alert2.getAlertTime() == null)
					// no alert time for either one
					return 0;
				else {
					// both alerts have a time, so determine which one is first
					// if the alerts are exactly the same time then, if one of them is
					// for exempt duty hours, return that one first
					if(alert1.getAlertTime().compareTo(alert2.getAlertTime()) == 0) {
						IHosAlert exemptAlert = GetAlert(HosAlertTypeEnum.ExemptDuty);
						
						if(alert1 == exemptAlert)
							return -1;
						if(alert2 == exemptAlert)
							return 1;
					}

					return alert1.getAlertTime().compareTo(alert2.getAlertTime());
				}
			}
			
		});
		
		IHosAlert earliestAlert = _hosAlerts.get(0);
		
		if(earliestAlert.getAlertTime() != null)
			return earliestAlert;
		else
			return null;
	}
}
