package com.jjkeller.kmb.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbapi.calcengine.RulesetBase;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.EmployeeRuleController;
import com.jjkeller.kmbapi.controller.HosAuditController;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.enums.ExemptLogTypeEnum;
import com.jjkeller.kmbapi.proxydata.DutySummary;
import com.jjkeller.kmbui.R;

/**
 * Report Available Hours Fragment
 *
 * Displays Available Hours from the Hours of Service Audit Controller.
 */

public class RptAvailHoursFrag extends BaseFragment
{
	private TextView _tvHoursWeekly;
	private TextView _tvHoursAvailWeekly;
	private TextView _tvHoursUsedWeekly;
	private TextView _tvRegWeekly;

	private TextView _tvHoursDuty;
	private TextView _tvHoursAvailDuty;
	private TextView _tvHoursUsedDuty;
	private TextView _tvRegDuty;

	private TextView _tvHoursDrive;
	private TextView _tvHoursAvailDrive;
	private TextView _tvHoursUsedDrive;
	private TextView _tvRegDrive;
	
	private TextView _lblRestBreak;
	
	private TextView _tvHoursRestBreak;
	private TextView _tvHoursAvailRestBreak;
	private TextView _tvHoursUsedRestBreak;
	private TextView _tvRegRestBreak;
	
	private TextView _lblHoursRestBreak;
	private TextView _lblUsedRestBreak;
	private TextView _lblAvailRestBreak;
	private TextView _lblRegRestBreak;

    private View _dividerRestBreak;
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.f_rptavailhours, container, false);
		findControls(v);
		return v;
	}

    public void init(HosAuditController hosAuditController){
        // determine if the current log is exempt (this is used to determine drive and break display settings)
        ExemptLogTypeEnum currentLogExemptType = GlobalState.getInstance().getCurrentEmployeeLog().getExemptLogType();
        boolean _isExemptLog = (currentLogExemptType.getValue() == ExemptLogTypeEnum.EXEMPTLOGTYPE100AIRMILE) || (currentLogExemptType.getValue() == ExemptLogTypeEnum.EXEMPTLOGTYPE150AIRMILENONCDL);

        //Obtain the rulsetBase and get the _is8HourDrivingRuleEnabled
        RulesetBase iHosRuleset = (RulesetBase) GlobalState.getInstance().getRulesetCalcEngine();
		boolean _is8HourDrivingRuleEnabled = iHosRuleset.getRulesetProperties().getIs8HourDrivingRuleEnabled();

        //Obtain the abbr of the user ruleset using a employeeRuleController
        EmployeeRuleController employeeRuleController = new EmployeeRuleController(getActivity().getApplicationContext());
        String ruleSetAbbr = employeeRuleController.getCurrentUser().getRulesetTypeEnum().getStringAbbr(getActivity());

        hosAuditController.UpdateForCurrentLogEvent();


        DutySummary weeklyDutySummary = hosAuditController.WeeklyDutySummary();
        DutySummary dailyDutySummary = hosAuditController.DailyDutySummary();
        DutySummary driveTimeSummary = hosAuditController.DriveTimeSummary();
        DutySummary restBreakSummary = hosAuditController.DriveTimeRestBreakSummary();

        boolean isMandateEnabled = GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled();

        _tvHoursWeekly.setText(String.valueOf(weeklyDutySummary.getAllowedHours()));
        _tvHoursUsedWeekly.setText(DateUtility.createTimeDurationString(weeklyDutySummary.getUsedMilliseconds(), isMandateEnabled, false));
        _tvHoursAvailWeekly.setText(DateUtility.createTimeDurationString(weeklyDutySummary.getAvailableMilliseconds(), isMandateEnabled, true));
        _tvRegWeekly.setText(ruleSetAbbr);

        _tvHoursDuty.setText(String.valueOf(dailyDutySummary.getAllowedHours()));
        _tvHoursUsedDuty.setText(DateUtility.createTimeDurationString(dailyDutySummary.getUsedMilliseconds(), isMandateEnabled, false));
        _tvHoursAvailDuty.setText(DateUtility.createTimeDurationString(dailyDutySummary.getAvailableMilliseconds(), isMandateEnabled, true));
        _tvRegDuty.setText(ruleSetAbbr);

        if (!_isExemptLog) {
            _tvHoursDrive.setText(String.valueOf(driveTimeSummary.getAllowedHours()));
            _tvHoursUsedDrive.setText(DateUtility.createTimeDurationString(driveTimeSummary.getUsedMilliseconds(), isMandateEnabled, false));
            _tvHoursAvailDrive.setText(DateUtility.createTimeDurationString(driveTimeSummary.getAvailableMilliseconds(), isMandateEnabled, true));
        } else {
            _tvHoursDrive.setText(R.string.notApplicable);
            _tvHoursUsedDrive.setText(R.string.notApplicable);
            _tvHoursAvailDrive.setText(R.string.notApplicable);
        }
        _tvRegDrive.setText(ruleSetAbbr);


        if (restBreakSummary != null && !_isExemptLog && _is8HourDrivingRuleEnabled) {
            restBreakVisible(true);

            long restUsed = restBreakSummary.getUsedMilliseconds();
            long restAvail = restBreakSummary.getAvailableMilliseconds();

            _tvHoursRestBreak.setText(String.valueOf(restBreakSummary.getAllowedHours()));
            _tvHoursUsedRestBreak.setText((restUsed == 0 && restAvail == 0) ? "N/A" : DateUtility.createTimeDurationString(restBreakSummary.getUsedMilliseconds(), isMandateEnabled, false));
            _tvHoursAvailRestBreak.setText((restUsed == 0 && restAvail == 0) ? "N/A" : DateUtility.createTimeDurationString(restBreakSummary.getAvailableMilliseconds(), isMandateEnabled, true));
            _tvRegRestBreak.setText(ruleSetAbbr);
        } else {
            // Don't display the Rest Break section
            restBreakVisible(false);
        }
    }

	protected void findControls(View v)
	{
		_tvHoursWeekly = (TextView)v.findViewById(R.id.tvHoursWeekly);
		_tvHoursAvailWeekly = (TextView)v.findViewById(R.id.tvHoursAvailWeekly);
		_tvHoursUsedWeekly = (TextView)v.findViewById(R.id.tvHoursUsedWeekly);
		_tvRegWeekly = (TextView)v.findViewById(R.id.tvRegWeekly);
		
		_tvHoursDuty = (TextView)v.findViewById(R.id.tvHoursDuty);
		_tvHoursAvailDuty = (TextView)v.findViewById(R.id.tvHoursAvailDuty);
		_tvHoursUsedDuty = (TextView)v.findViewById(R.id.tvHoursUsedDuty);
		_tvRegDuty = (TextView)v.findViewById(R.id.tvRegDuty);
		
		_tvHoursDrive = (TextView)v.findViewById(R.id.tvHoursDrive);
		_tvHoursAvailDrive = (TextView)v.findViewById(R.id.tvHoursAvailDrive);
		_tvHoursUsedDrive = (TextView)v.findViewById(R.id.tvHoursUsedDrive);
		_tvRegDrive = (TextView)v.findViewById(R.id.tvRegDrive);
		
		
		_lblRestBreak = (TextView)v.findViewById(R.id.lblRestBreak);
		
		_tvHoursRestBreak = (TextView)v.findViewById(R.id.tvHoursRestBreak);
		_tvHoursAvailRestBreak = (TextView)v.findViewById(R.id.tvHoursAvailRestBreak);
		_tvHoursUsedRestBreak = (TextView)v.findViewById(R.id.tvHoursUsedRestBreak);
		_tvRegRestBreak = (TextView)v.findViewById(R.id.tvRegRestBreak);
				
		_lblHoursRestBreak = (TextView)v.findViewById(R.id.lblHoursRestBreak);
		_lblUsedRestBreak = (TextView)v.findViewById(R.id.lblUsedRestBreak);
		_lblAvailRestBreak = (TextView)v.findViewById(R.id.lblAvailRestBreak);
		_lblRegRestBreak = (TextView)v.findViewById(R.id.lblRegRestBreak);

	    _dividerRestBreak = v.findViewById(R.id.dividerRestBreak);
	}

	public void restBreakVisible(boolean visible){
		if(visible){
			_lblRestBreak.setVisibility(View.VISIBLE);
			
			_tvHoursRestBreak.setVisibility(View.VISIBLE);
			_tvHoursAvailRestBreak.setVisibility(View.VISIBLE);
			_tvHoursUsedRestBreak.setVisibility(View.VISIBLE); 
			_tvRegRestBreak.setVisibility(View.VISIBLE); 
					
			_lblHoursRestBreak.setVisibility(View.VISIBLE); 
			_lblUsedRestBreak.setVisibility(View.VISIBLE);
			_lblAvailRestBreak.setVisibility(View.VISIBLE);
			_lblRegRestBreak.setVisibility(View.VISIBLE);

			if(_dividerRestBreak != null)
				_dividerRestBreak.setVisibility(View.VISIBLE);
		}else{
			_lblRestBreak.setVisibility(View.GONE);
			
			_tvHoursRestBreak.setVisibility(View.GONE);
			_tvHoursAvailRestBreak.setVisibility(View.GONE);
			_tvHoursUsedRestBreak.setVisibility(View.GONE); 
			_tvRegRestBreak.setVisibility(View.GONE); 
					
			_lblHoursRestBreak.setVisibility(View.GONE); 
			_lblUsedRestBreak.setVisibility(View.GONE);
			_lblAvailRestBreak.setVisibility(View.GONE);
			_lblRegRestBreak.setVisibility(View.GONE);

			if(_dividerRestBreak != null)
				_dividerRestBreak.setVisibility(View.GONE);
		}
	}
}
