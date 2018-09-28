package com.jjkeller.kmbapi.controller.hos;

import android.content.Context;

import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.enums.DrivingNotificationTypeEnum;
import com.jjkeller.kmbapi.enums.HosAlertTimingEnum;

import java.util.Date;

public abstract class HosAlertBase {
	protected Date _alertTime;
	protected HosAlertTimingEnum _alertTimingEnum;
	protected Context _context;
	
	protected static final long ONE_HOUR = 3600000;
	protected static final long TWO_HOURS = 2 * ONE_HOUR;
	protected static final long HALF_HOUR = (long) (.5 * ONE_HOUR);
	
	public HosAlertBase(Context context)
	{
		_context = context;
		_alertTimingEnum = HosAlertTimingEnum.None;
	}
	
	public Date getAlertTime() {
		return _alertTime;
	}
	public void setAlertTime(Date alertTime) {
		this._alertTime = alertTime;
	}
	
	public HosAlertTimingEnum getAlertTimingEnum() {
		return _alertTimingEnum;
	}
	public void setAlertTimingEnum(HosAlertTimingEnum alertTimingEnum) {
		this._alertTimingEnum = alertTimingEnum;
	}	
	
	public boolean getAlertRequiresConfirmation()
	{
		boolean confirm = false;

		// ignore if the driver should not get alerts
		if (GlobalState.getInstance().getCurrentDesignatedDriver().getDrivingNotificationTypeEnum().getValue() == DrivingNotificationTypeEnum.NOALERT)
			return false;
		
		if (_alertTimingEnum != HosAlertTimingEnum.None) {

			if (_alertTime != null) {
				Date now = DateUtility.getCurrentDateTimeUTC();
				if (now.compareTo(_alertTime) < 0) {
					// the violation is still in the future and there are messages left to show determine if it's time to show an alert
					long timeToViolation = _alertTime.getTime() - now.getTime();

					switch (_alertTimingEnum) {
					case TwoHour:
						if (timeToViolation <= TWO_HOURS) {
							confirm = true;
							this.setAlertTimingEnum(HosAlertTimingEnum.OneHour);
						}
						break;

					case OneHour:
						if (timeToViolation <= ONE_HOUR) {
							confirm = true;
							this.setAlertTimingEnum(HosAlertTimingEnum.HalfHour);
						}
						break;

					case HalfHour:
						if (timeToViolation <= HALF_HOUR) {
							confirm = true;
							this.setAlertTimingEnum(HosAlertTimingEnum.ZeroHour);
						}
						break;

					case ZeroHour:
						if (timeToViolation <= 0) {
							confirm = true;
							this.setAlertTimingEnum(HosAlertTimingEnum.None);
						}
						break;

					default:
						break;
					}
				} else {
					// the violation time has expired, but there is an alert to
					// show
					confirm = true;
					this.setAlertTimingEnum(HosAlertTimingEnum.None);
				}
			}
		}
		
		return confirm;
	}
	
	public void setNextAlertTiming(long currentTimeAvailable)
	{
		HosAlertTimingEnum nextAlertTiming = HosAlertTimingEnum.None;
		
		//determine the next alert type that needs to be displayed
		switch(GlobalState.getInstance().getCurrentUser().getDrivingNotificationTypeEnum().getValue())
		{
			case DrivingNotificationTypeEnum.ONEHOUR:
				if(currentTimeAvailable > HALF_HOUR) {
					// more than 30 minutes left until the vio, display 3 alerts
					// (OneHour, HalfHour, and ZeroHour)
					nextAlertTiming = HosAlertTimingEnum.OneHour;
				} else if(currentTimeAvailable > 0) {
					//between 0-30 minutes remaining until the vio, display 2
					//alerts (HalfHour and ZeroHour)
					nextAlertTiming = HosAlertTimingEnum.HalfHour;
				} else {
					//the vio must have expired, so only display the final alert
					nextAlertTiming = HosAlertTimingEnum.ZeroHour;
				}
				break;
				
			case DrivingNotificationTypeEnum.TWOHOUR:
				if(currentTimeAvailable > ONE_HOUR) {
					// more than 30 minutes left until the vio, display 3 alerts
					// (OneHour, HalfHour, and ZeroHour)
					nextAlertTiming = HosAlertTimingEnum.TwoHour;
				} else if(currentTimeAvailable > HALF_HOUR) {
					//between 0-30 minutes remaining until the vio, display 2
					//alerts (HalfHour and ZeroHour)
					nextAlertTiming = HosAlertTimingEnum.OneHour;
				} else if(currentTimeAvailable > 0) {
					//between 0-30 minutes remaining until the vio, display 2
					//alerts (HalfHour and ZeroHour)
					nextAlertTiming = HosAlertTimingEnum.HalfHour;
				} else {
					//the vio must have expired, so only display the final alert
					nextAlertTiming = HosAlertTimingEnum.ZeroHour;
				}
				break;
				
			default:
				break;
		}
		
		if(_alertTimingEnum != HosAlertTimingEnum.None)
		{
			//there is a current alert being tracked, make sure that the next alert is not higher in frequency than the current one
			switch(_alertTimingEnum) {
				case TwoHour:
					//nothing to do here, the next alert will not be higher
					break;
					
				case OneHour:
					if(nextAlertTiming == HosAlertTimingEnum.TwoHour)
						nextAlertTiming = _alertTimingEnum;
					break;
				
				case HalfHour:
					if(nextAlertTiming == HosAlertTimingEnum.TwoHour
						|| nextAlertTiming == HosAlertTimingEnum.OneHour)
					{
						nextAlertTiming = _alertTimingEnum;
					}
					break;
				
				case ZeroHour:
					if(nextAlertTiming == HosAlertTimingEnum.TwoHour
						|| nextAlertTiming == HosAlertTimingEnum.OneHour
						|| nextAlertTiming == HosAlertTimingEnum.HalfHour)
					{
						nextAlertTiming = _alertTimingEnum;
					}
					break;
					
				default:
					break;
			}
		}
		
		_alertTimingEnum = nextAlertTiming;
	}
	
	public void Clear()
	{
		this.setAlertTime(null);
		this.setAlertTimingEnum(HosAlertTimingEnum.None);	
	}
}
