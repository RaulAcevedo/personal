package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jjkeller.kmb.DOTClocks;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmb.share.ClockData;
import com.jjkeller.kmb.share.DOTClock;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.EOBR.EobrReader;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.enums.ExemptLogTypeEnum;
import com.jjkeller.kmbapi.proxydata.DutySummary;
import com.jjkeller.kmbui.R;

public class DOTClocksFrag extends BaseFragment
{

	protected TextView _driverNameText;
	protected TextView _drivingHoursAvailableText;
	protected TextView _dailyOnDutyAvailableText;
	protected TextView _weeklyOnDutyAvailableText;
	protected DOTClock _drivingHoursAvailableClock;
	protected DOTClock _dailyOnDutyAvailableClock;
	protected DOTClock _weeklyOnDutyAvailableClock;
    protected LinearLayout _myLayout;
    
    protected TextView _drivingTitle;
    protected TextView _dailyTitle;
    protected TextView _weeklyTitle;
	protected LinearLayout _layoutHyrailUsed;
	protected LinearLayout _layoutYardMoveUsed;
	protected LinearLayout _layoutNonRegDrivingStatusUsed;

    // ----------- Default values in order to store the last update in case view is not ready ------
    protected ClockData driveTimeClock;
    protected ClockData driveResetBreakClock;
    protected ClockData dailyOnDutyClock;
    protected ClockData weeklyOnDutyClock;
    protected DutySummary driveTimeResetSummary;
    protected boolean forceDisableDrivingClock;
    private boolean isViewReady = false;
    private boolean isMissingData = false;
    // ---------------------------------------------------------------------------------------------

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.f_dotclocks, container, false);
		findControls(v);

		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		this.loadControls(savedInstanceState);
	}

	protected void findControls(View v)
	{
		_driverNameText = (TextView)v.findViewById(R.id.driver_name);
		_drivingHoursAvailableText = (TextView)v.findViewById(R.id.driving_hours_available_text);
		_dailyOnDutyAvailableText = (TextView)v.findViewById(R.id.daily_on_duty_available_text);
		_weeklyOnDutyAvailableText = (TextView)v.findViewById(R.id.weekly_on_duty_available_text);
		_drivingHoursAvailableClock = (DOTClock)v.findViewById(R.id.driving_hours_available);
		_dailyOnDutyAvailableClock = (DOTClock)v.findViewById(R.id.daily_on_duty_available);
		_weeklyOnDutyAvailableClock = (DOTClock)v.findViewById(R.id.weekly_on_duty_available);
		
		_drivingTitle = (TextView)v.findViewById(R.id.drivingTitle);
	    _dailyTitle = (TextView)v.findViewById(R.id.dailyTitle);
	    _weeklyTitle = (TextView)v.findViewById(R.id.weeklyTitle);
		_layoutHyrailUsed = (LinearLayout)v.findViewById(R.id.layoutHyrailUsed);
		_layoutYardMoveUsed = (LinearLayout)v.findViewById(R.id.layoutYardMoveUsed);
		_layoutNonRegDrivingStatusUsed = (LinearLayout)v.findViewById(R.id.layoutNonRegDrivingStatusUsed);

		_myLayout = (LinearLayout) v.findViewById(R.id.dotClcoksLayout);
	}

	protected void loadControls(Bundle savedInstanceState)
	{
		// 11/28/12 AMO: Set the text to be for the current user, so that when in team driving the title is correct.
		_driverNameText.setText(GlobalState.getInstance().getCurrentUser().getCredentials().getEmployeeFullName());

		boolean isDriver = GlobalState.getInstance().getIsCurrentUserTheDesignatedDriver();

		boolean isVehicleInMotion;

		if(EobrReader.getInstance().isEobrGen1())
			isVehicleInMotion = GlobalState.getInstance().getVehicleMotionDetector(GlobalState.getInstance().getBaseContext()).getIsVehicleInMotion();
		else
			isVehicleInMotion = (GlobalState.getInstance().getPotentialDrivingStopTimestamp() == null);

		if(isVehicleInMotion && isDriver && GlobalState.getInstance().getIsInHyrailDutyStatus())
			_layoutHyrailUsed.setVisibility(View.VISIBLE);

		if(isVehicleInMotion && isDriver && GlobalState.getInstance().getIsInYardMoveDutyStatus())
			_layoutYardMoveUsed.setVisibility(View.VISIBLE);

		if(isVehicleInMotion && isDriver && GlobalState.getInstance().getIsInNonRegDrivingDutyStatus())
			_layoutNonRegDrivingStatusUsed.setVisibility(View.VISIBLE);

		// If DOT Clocks then use Night Mode
		Activity parent = getActivity();
		if(parent.getClass() == DOTClocks.class){
			setNightMode();
			dimScreen();
		}
	}

    @Override
    public void onResume() {
        super.onResume();
        isViewReady = true;
        if (isMissingData){
            isMissingData = false;
            updateClocks(driveTimeClock,driveResetBreakClock,dailyOnDutyClock,weeklyOnDutyClock,driveTimeResetSummary,forceDisableDrivingClock);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        isViewReady = false;
    }

    public void setNightMode(){
		boolean nightMode = getDOTClocks_NightMode();
		if(nightMode){
			_myLayout.setBackgroundColor(Color.DKGRAY);
			_driverNameText.setTextColor(Color.GRAY);
			_drivingHoursAvailableText.setTextColor(Color.GRAY);
			_dailyOnDutyAvailableText.setTextColor(Color.GRAY);
			_weeklyOnDutyAvailableText.setTextColor(Color.GRAY);
			_drivingTitle.setTextColor(Color.GRAY);
		    _dailyTitle.setTextColor(Color.GRAY);
		    _weeklyTitle.setTextColor(Color.GRAY);
		}
		else
		{
			_myLayout.setBackgroundColor(Color.WHITE);
			_driverNameText.setTextColor(Color.BLACK);
			_drivingHoursAvailableText.setTextColor(Color.BLACK);
			_dailyOnDutyAvailableText.setTextColor(Color.BLACK);
			_weeklyOnDutyAvailableText.setTextColor(Color.BLACK);
			_drivingTitle.setTextColor(Color.BLACK);
		    _dailyTitle.setTextColor(Color.BLACK);
		    _weeklyTitle.setTextColor(Color.BLACK);
		}
	}
	
	public void dimScreen(){
		
		Activity parent = getActivity();
    	
    	WindowManager.LayoutParams lp = parent.getWindow().getAttributes();
    	
    	boolean nightMode = getDOTClocks_NightMode();
		if(nightMode)
			lp.screenBrightness = 0.1f;
		else
			lp.screenBrightness = -1.0f;
		
		parent.getWindow().setAttributes(lp);
		
	}


	public void setDrivingHoursAvailClock(double percent, boolean eightHourClock, double eightHourPercent)
	{
		if ((percent <= 0.0 || eightHourPercent <= 0.0) && eightHourClock) {
            //noinspection ConstantConditions
            _drivingHoursAvailableClock.setPercentage(0.0, eightHourClock, 0.0);
		} else {
			_drivingHoursAvailableClock.setPercentage(percent, eightHourClock, eightHourPercent);
		}
	}

    public void updateClocks(ClockData driveTimeClock, ClockData driveResetBreakClock, ClockData dailyOnDutyClock, ClockData weeklyOnDutyClock, DutySummary driveTimeResetSummary) {
        updateClocks(driveTimeClock, driveResetBreakClock, dailyOnDutyClock, weeklyOnDutyClock, driveTimeResetSummary, false);
    }
    
    public void updateClocks(ClockData driveTimeClock, ClockData driveResetBreakClock, ClockData dailyOnDutyClock, ClockData weeklyOnDutyClock, DutySummary driveTimeResetSummary, boolean forceDisableDrivingClock) {

        if (driveTimeClock == null || dailyOnDutyClock == null || weeklyOnDutyClock == null || !isViewReady){
            this.driveTimeClock = driveTimeClock;
            if (driveResetBreakClock != null)
                this.driveResetBreakClock = driveResetBreakClock;
            this.dailyOnDutyClock = dailyOnDutyClock;
            this.weeklyOnDutyClock = weeklyOnDutyClock;
            if (driveTimeResetSummary != null)
                this.driveTimeResetSummary = driveTimeResetSummary;
            this.forceDisableDrivingClock = forceDisableDrivingClock;
            isMissingData = true;
            return;
        }

        boolean isMandateEnabled = GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled();

        _dailyOnDutyAvailableText.setText(DateUtility.createTimeDurationString(dailyOnDutyClock.getMillisecondsAvailable(),isMandateEnabled,  true));
        _weeklyOnDutyAvailableText.setText(DateUtility.createTimeDurationString(weeklyOnDutyClock.getMillisecondsAvailable(), isMandateEnabled, true));
        _dailyOnDutyAvailableClock.setPercentage(dailyOnDutyClock.getPercentage(), false, 0.0);
        _weeklyOnDutyAvailableClock.setPercentage(weeklyOnDutyClock.getPercentage(), false, 0.0);

        long restUsed = 0;
        long restAvail = 0;

        if (driveTimeResetSummary != null) {
            restUsed = driveTimeResetSummary.getUsedMilliseconds();
            restAvail = driveTimeResetSummary.getAvailableMilliseconds();
        }

        // determine if the current log is exempt (this is used to determine drive and break display settings)
        ExemptLogTypeEnum currentLogExemptType = GlobalState.getInstance().getCurrentEmployeeLog().getExemptLogType();
        boolean _isExemptLog = (currentLogExemptType.getValue() == ExemptLogTypeEnum.EXEMPTLOGTYPE100AIRMILE) || (currentLogExemptType.getValue() == ExemptLogTypeEnum.EXEMPTLOGTYPE150AIRMILENONCDL);

        if (_isExemptLog || forceDisableDrivingClock) {
            _drivingHoursAvailableClock.setNotApplicable(Color.GRAY);
           setDrivingHoursAvailText(this.getString(R.string.notApplicable), "");
        } else {
            if (driveResetBreakClock == null || (restUsed == 0 && restAvail == 0)) {
                setDrivingHoursAvailClock(driveTimeClock.getPercentage(), false, 0.0);
                setDrivingHoursAvailText(DateUtility.createTimeDurationString(driveTimeClock.getMillisecondsAvailable(), isMandateEnabled, true), "");
            } else {
                setDrivingHoursAvailClock(driveTimeClock.getPercentage(), true, driveResetBreakClock.getPercentage());
                setDrivingHoursAvailText(DateUtility.createTimeDurationString(driveTimeClock.getMillisecondsAvailable(), isMandateEnabled, true), DateUtility.createTimeDurationString(driveResetBreakClock.getMillisecondsAvailable(), isMandateEnabled, true));
            }
        }

    }

	public void setDrivingHoursAvailText(String time, String time_ResetBreak)
	{
		if(time_ResetBreak == "" || time_ResetBreak == "0.0")
			_drivingHoursAvailableText.setText(time);
		else
			_drivingHoursAvailableText.setText(time + " (" + time_ResetBreak + ")");
	}

}
