package com.jjkeller.kmbapi.controller.hos;

import android.content.Context;
import android.os.Bundle;

import com.jjkeller.kmbapi.R;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.HosAuditController;
import com.jjkeller.kmbapi.controller.interfaces.IHosAlert;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.enums.ExemptLogTypeEnum;
import com.jjkeller.kmbapi.enums.HosAlertTypeEnum;
import com.jjkeller.kmbapi.proxydata.DutySummary;

public class HosAlertDriving extends HosAlertBase implements IHosAlert {

	public HosAlertDriving(Context context) {
		super(context);
	}

	public HosAlertTypeEnum getHosAlertTypeEnum() {
		return HosAlertTypeEnum.StandardDriving;
	}

	public String getAlertMessage() {
		String msg = null;

		if (_alertTime != null) {
			if (DateUtility.getCurrentDateTimeUTC().compareTo(_alertTime) < 0) {
				msg = String.format(_context.getString(R.string.drivingalertmsg_remainingtime), DateUtility.getHomeTerminalDateTimeFormat12Hour().format(_alertTime));
			} else {
				// Determine which message to display
				// When driving, they will either be out of exempt hours, or over the driving limit
				HosAuditController auditController = new HosAuditController(_context);
				
				boolean isOverExemptHours = false;
				if(GlobalState.getInstance().getCurrentEmployeeLog().getExemptLogType().getValue() != ExemptLogTypeEnum.NULL) {
					// for exempt logs, determine they are over exempt hours
					// if there are no available hours remaining for daily duty, then they are out of exempt hours
					DutySummary dailyDutySummary = auditController.DailyDutySummary();
					long dailyDutyAvail = dailyDutySummary.getAvailableMilliseconds();
					if(dailyDutyAvail <= 0)
						isOverExemptHours = true;
				}
				
				if(isOverExemptHours) {
					// when over the exempt hours, display the Exempt Overtime message
					DutySummary dutySummary = auditController.DailyDutySummary();
					msg = String.format(_context.getString(R.string.exemptdutyalertmsg_overtime), dutySummary.getRegulationCode());
				} else {
					// if this is a non-exempt log, or there are exempt hours remaining, then show the standard Driving Overtime message
					DutySummary driveTimeSummary = auditController.DriveTimeSummary();
					msg = String.format(_context.getString(R.string.drivingalertmsg_overtime), driveTimeSummary.getRegulationCode());
				}
			}
		}
		
		return msg;
	}
}
