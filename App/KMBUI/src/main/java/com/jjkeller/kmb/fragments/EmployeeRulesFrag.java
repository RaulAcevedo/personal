package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TableRow;
import android.widget.TextView;

import com.jjkeller.kmb.interfaces.IEmployeeRules.EmployeeRulesFragActions;
import com.jjkeller.kmb.interfaces.IEmployeeRules.EmployeeRulesFragControllerMethods;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbui.R;

import java.text.DecimalFormat;

public class EmployeeRulesFrag extends BaseFragment{
	EmployeeRulesFragActions actionsListener;
	EmployeeRulesFragControllerMethods controlListener;	
	
	private TextView _lblMessage;
	private TextView _tvTimeZone;
	private TextView _tvRuleset;
	private TextView _tvDriverType;
	private TextView _tvDriving;
	private TextView _tvStopTime;
	private TextView _tvDataProfile;
	private TextView _tvDistanceUnits;
	private CheckBox _chk34HourReset;
	private CheckBox _chkShortHaul;
	private CheckBox _chkHaulingExplosivesAllowed;
	private Button _btnDownload;
	private Button _btnChangeRuleset;
	private TableRow _trOperatesSpecificVehiclesForOilField;
	private CheckBox _chkOperatesSpecificVehiclesForOilField;
	private CheckBox _chkIsPersonalConveyanceAllowed;
    private CheckBox _chkIsHyrailUseAllowed;
	private CheckBox _chkIsMobileExemptAllowed;
	private CheckBox _chkIsExemptFrom30MinBreakRequirement;
	private TextView _tvExemptLogType;
	private CheckBox _chkExemptFromEldUse;
	private TableRow _trExemptFromEldUseComment;
	private TextView _tvExemptFromEldUseComment;
	private CheckBox _chkYardMoveAllowed;
	private CheckBox _chkIsNonRegDrivingAllowed;

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        // Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.f_employeerules, container, false);
		findControls(v);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		this.loadControls();
	}
	
	private void loadControls(){
		User usr = controlListener.getMyController().getCurrentUser();
		controlListener.getMyController().getCurrentUser().getHomeTerminalTimeZone();
		
		_tvTimeZone.setText(usr.getHomeTerminalTimeZone().getString(getActivity()));
		_tvRuleset.setText(usr.getRulesetTypeEnum().getString(getActivity()));
		_tvDriverType.setText(usr.getDriverType().getString(getActivity()));
		_tvDriving.setText((new DecimalFormat("#0.0")).format(usr.getDrivingStartDistanceMiles()) + " " + getActivity().getString(R.string.eerules_miles));
		_tvStopTime.setText(Integer.toString(usr.getDrivingStopTimeMinutes()) + " " + this.getString(R.string.eerules_minutes));
		_tvDataProfile.setText(usr.getDataProfile().getString(getActivity()));
		_tvDistanceUnits.setText(usr.getDistanceUnits());
		_chk34HourReset.setChecked(usr.getIs34HourResetAllowed());
		_chkShortHaul.setChecked(usr.getIsShorthaulException());
		_chkHaulingExplosivesAllowed.setChecked(usr.getIsHaulingExplosivesAllowed());
		_btnDownload.setEnabled(controlListener.getMyController().getIsNetworkAvailable());
		_btnChangeRuleset.setEnabled(true);
		_chkIsPersonalConveyanceAllowed.setChecked(usr.getIsPersonalConveyanceAllowed());
        _chkIsHyrailUseAllowed.setChecked(usr.getIsHyrailAllowed());
		_tvExemptLogType.setText(usr.getExemptLogType().getString(getActivity()));
		_chkIsMobileExemptAllowed.setChecked(usr.getIsMobileExemptLogAllowed());
		_chkIsExemptFrom30MinBreakRequirement.setChecked(usr.getIsExemptFrom30MinBreakRequirement());
		_chkExemptFromEldUse.setChecked(usr.getExemptFromEldUse());
		_chkYardMoveAllowed.setChecked(usr.getYardMoveAllowed());
		_chkIsNonRegDrivingAllowed.setChecked(usr.getIsNonRegDrivingAllowed());
		_tvExemptFromEldUseComment.setText(usr.getExemptFromEldUseComment());

		if (usr.getRulesetTypeEnum().isAnyOilFieldRuleset())
		{
			_trOperatesSpecificVehiclesForOilField.setVisibility(View.VISIBLE);
			_chkOperatesSpecificVehiclesForOilField.setChecked(usr.getIsOperatesSpecificVehiclesForOilfield());
		}
		else
		{
			_trOperatesSpecificVehiclesForOilField.setVisibility(View.GONE);
			_chkOperatesSpecificVehiclesForOilField.setChecked(false);
		}	

		if(_chkExemptFromEldUse.isChecked())
			_trExemptFromEldUseComment.setVisibility(View.VISIBLE);
		else
			_trExemptFromEldUseComment.setVisibility(View.GONE);

		//Feature Toggle
		if(GlobalState.getInstance().getFeatureService().getPersonalConveyanceEnabled())
			_chkIsPersonalConveyanceAllowed.setVisibility(View.VISIBLE);
		else _chkIsPersonalConveyanceAllowed.setVisibility(View.GONE);

        //Feature Toggle
        if(GlobalState.getInstance().getFeatureService().getHyrailEnabled())
            _chkIsHyrailUseAllowed.setVisibility(View.VISIBLE);
        else _chkIsHyrailUseAllowed.setVisibility(View.GONE);

		//Feature Toggle
		if(GlobalState.getInstance().getFeatureService().getNonRegDrivingEnabled())
			_chkIsNonRegDrivingAllowed.setVisibility(View.VISIBLE);
		else _chkIsNonRegDrivingAllowed.setVisibility(View.GONE);

		//Feature Toggle
		if(GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
			_chkExemptFromEldUse.setVisibility(View.VISIBLE);
			_chkYardMoveAllowed.setVisibility(View.VISIBLE);
		}
		else {
			_chkExemptFromEldUse.setVisibility(View.GONE);
			_chkYardMoveAllowed.setVisibility(View.GONE);
			_trExemptFromEldUseComment.setVisibility(View.GONE);
		}
		
	}
	
	protected void findControls(View v){
		_lblMessage = (TextView)v.findViewById(R.id.eerules_lblMessage);
		_tvTimeZone = (TextView)v.findViewById(R.id.eerules_tvTimeZone);
		_tvRuleset = (TextView)v.findViewById(R.id.eerules_tvRuleset);
		_tvDriverType = (TextView)v.findViewById(R.id.eerules_tvDriverType);
		_tvDriving = (TextView)v.findViewById(R.id.eerules_tvDriving);
		_tvStopTime = (TextView)v.findViewById(R.id.eerules_tvStopTime);
		_tvDataProfile = (TextView)v.findViewById(R.id.eerules_tvDataProfile);
		_tvDistanceUnits = (TextView)v.findViewById(R.id.eerules_tvDistanceUnits);
		_chk34HourReset = (CheckBox)v.findViewById(R.id.eerules_chk34HrReset);
		_chkShortHaul = (CheckBox)v.findViewById(R.id.eerules_chkSortHaul);
		_chkHaulingExplosivesAllowed = (CheckBox)v.findViewById(R.id.eerules_chkHaulingExplosivesAllowed);
		_btnDownload = (Button)v.findViewById(R.id.eerules_btnDownload);
		_btnChangeRuleset = (Button)v.findViewById(R.id.eerules_btnChangeRuleset);
		_trOperatesSpecificVehiclesForOilField = (TableRow)v.findViewById(R.id.eerules_trIsOperatesSpecificVehicleForOilField);
		_chkOperatesSpecificVehiclesForOilField = (CheckBox)v.findViewById(R.id.eerules_chkIsOperatesSpecificVehicleForOilField);
		_chkIsPersonalConveyanceAllowed = (CheckBox)v.findViewById(R.id.eerules_chkIsPersonalConveyanceAllowed);
        _chkIsHyrailUseAllowed = (CheckBox)v.findViewById(R.id.eerules_chkIsHyrailUseAllowed);
		_chkIsMobileExemptAllowed = (CheckBox)v.findViewById(R.id.eerules_chkIsMobileExemptAllowed);
		_chkIsExemptFrom30MinBreakRequirement = (CheckBox)v.findViewById(R.id.eerules_chkIsExemptFrom30MinBreakRequirement);
		_tvExemptLogType = (TextView)v.findViewById(R.id.eerules_tvExemptLogType);
		_chkExemptFromEldUse = (CheckBox)v.findViewById(R.id.eerules_chkExemptFromEldUse);
		_chkYardMoveAllowed = (CheckBox)v.findViewById(R.id.eerules_chkYardMoveAllowed);
		_chkIsNonRegDrivingAllowed = (CheckBox)v.findViewById(R.id.eerules_chkIsNonRegDrivingAllowed);
		_trExemptFromEldUseComment = (TableRow)v.findViewById(R.id.eerules_trExemptFromEldUseComment);
		_tvExemptFromEldUseComment = (TextView)v.findViewById(R.id.eerules_tvExemptFromEldUseComment);

		_btnDownload.setOnClickListener(
	            new OnClickListener() {
	            	public void onClick(View v) {
	            		actionsListener.handleDownloadButtonClick();
	            	}
	            });

		_btnChangeRuleset.setOnClickListener(
	            new OnClickListener() {
	            	public void onClick(View v) {
	            		actionsListener.handleChangeRulesetButtonClick();
	            	}
	            });
	}
	
	@Override
    public void onAttach(Activity activity) {
		super.onAttach(activity);
        try {
        	actionsListener = (EmployeeRulesFragActions) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement EmployeeRulesFragActions");
        }
        
        try{
        	controlListener = (EmployeeRulesFragControllerMethods) activity;
        } catch (ClassCastException e) {
        	throw new ClassCastException(activity.toString() + " must implement EmployeeRulesFragControllerMethods");
        }
    }
	
	public Button getDownloadButton(){
		if(_btnDownload == null)
			_btnDownload = (Button)getView().findViewById(R.id.btnDownload);
		return _btnDownload;
	}
	
	public TextView getMessageTextView(){
		if(_lblMessage == null)
			_lblMessage = (TextView)getView().findViewById(R.id.lblMessage);
		return _lblMessage;
	}
}
