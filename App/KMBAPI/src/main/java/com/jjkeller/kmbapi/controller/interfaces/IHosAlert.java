package com.jjkeller.kmbapi.controller.interfaces;

import com.jjkeller.kmbapi.enums.HosAlertTimingEnum;
import com.jjkeller.kmbapi.enums.HosAlertTypeEnum;

import java.util.Date;

public interface IHosAlert {
	public HosAlertTypeEnum getHosAlertTypeEnum();
	
	public Date getAlertTime();
	public void setAlertTime(Date alertTime);
	
	public HosAlertTimingEnum getAlertTimingEnum();
	public void setAlertTimingEnum(HosAlertTimingEnum alertTimingEnum);
	
	public String getAlertMessage();
	public boolean getAlertRequiresConfirmation();
	public void setNextAlertTiming(long currentTimeAvailable);
	public void Clear();
	
}
