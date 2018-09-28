package com.jjkeller.kmbapi.controller.hos;

import android.content.Context;
import android.os.Bundle;

import com.jjkeller.kmbapi.R;
import com.jjkeller.kmbapi.controller.HosAuditController;
import com.jjkeller.kmbapi.controller.interfaces.IHosAlert;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.enums.HosAlertTypeEnum;
import com.jjkeller.kmbapi.proxydata.DutySummary;

public class HosAlertExemptDuty extends HosAlertBase implements IHosAlert {

	public HosAlertExemptDuty(Context context) {
		super(context);
	}

	public HosAlertTypeEnum getHosAlertTypeEnum() {
		return HosAlertTypeEnum.ExemptDuty;
	}

	public String getAlertMessage() {
		String msg = null;

		if (_alertTime != null) {
			if (DateUtility.getCurrentDateTimeUTC().compareTo(_alertTime) < 0) {
				msg = String.format(_context.getString(R.string.exemptdutyalertmsg_remainingtime), DateUtility.getHomeTerminalDateTimeFormat12Hour().format(_alertTime));
			} else {
				DutySummary dutySummary = new HosAuditController(_context).DailyDutySummary();
				msg = String.format(_context.getString(R.string.exemptdutyalertmsg_overtime), dutySummary.getRegulationCode());
			}
		}
		
		return msg;
	}
}
