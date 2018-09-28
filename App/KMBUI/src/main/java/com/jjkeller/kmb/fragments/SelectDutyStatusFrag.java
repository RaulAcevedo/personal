package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.jjkeller.kmb.interfaces.ISelectDutyStatus;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbapi.common.TimeKeeper;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.controller.utility.EmployeeLogUtilities;
import com.jjkeller.kmbapi.enums.DutyStatusEnum;
import com.jjkeller.kmbapi.enums.ExemptLogTypeEnum;
import com.jjkeller.kmbui.R;

import java.util.ArrayList;
import java.util.Date;



public class SelectDutyStatusFrag extends BaseFragment{
	ISelectDutyStatus.SelectDutyStatusFragActions actionsListener;
	ISelectDutyStatus.SelectDutyStatusFragControllerMethods controlListener;
	
	private RadioButton _radioSleeper;
	private RadioButton _radioOffDutyWellSite;
	private CheckBox _cbPersonalConveyance;
	private CheckBox _chkMaintainDriving;

	private EditText _txtAnnotationOnDuty;
	private EditText _txtAnnotationOffDuty;

	private TextView _lblLocationCode;
	private TextView _lblActualLocation;
	private EditText _txtLocationCode;
	private EditText _txtActualLocation;

	private CheckBox _cbYardMove;
	private CheckBox _cbHyrail;
	private CheckBox _cbNonRegDriving;
	private String _dutyStatus;

	private String initialStatusValue;
	private Date logStartTime;

	private TextView _errorMessage;

	private LinearLayout _linearLayoutAnnotationOnDuty;
	private LinearLayout _linearLayoutAnnotationOffDuty;

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        // Inflate the layout for this fragment

		View v = inflater.inflate(R.layout.f_selectdutystatus, container, false);
		findControls(v);

		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		this.loadControls(savedInstanceState);
	}

	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		// 10/3/11 JHM - Don't store state (likely blank values) if the FetchLocalData task hasn't completed.
		// 8/3/12 ACM - getLocalDataTask checks if FetchLocalData finished or not (true = finished)
		if(actionsListener.getLocalDataTask()){
			outState.putString("locationcode", _txtLocationCode.getText().toString());
			outState.putString("location", _txtActualLocation.getText().toString());
			outState.putBoolean("maintainDriving", _chkMaintainDriving.isChecked());
			outState.putInt("maintainVis", _chkMaintainDriving.getVisibility());
		}
		super.onSaveInstanceState(outState);
	}

	private void loadControls(Bundle savedInstanceState){

		if (!controlListener.isCurrentLogCreated()) {
			ArrayList<String> statusList = new ArrayList<>();
			statusList.add(DutyStatusEnum.Friendly_OffDuty);
			statusList.add(DutyStatusEnum.Friendly_Sleeping);
			statusList.add(DutyStatusEnum.Friendly_OnDuty);

			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getActivity(), R.layout.kmb_spinner_item, statusList);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

			logStartTime = EmployeeLogUtilities.CalculateLogStartTime(GlobalState.getInstance().getApplicationContext()
					, TimeKeeper.getInstance().getCurrentDateTime().toDate()
					, GlobalState.getInstance().getCurrentUser().getHomeTerminalTimeZone());

				String suggestion = controlListener.getSuggestedInitialDutyStatus();
				if (suggestion != null) {
					initialStatusValue = suggestion;
				} else {
					//If the app was just installed and there are no previous logs, default to "Off Duty"
					initialStatusValue = DutyStatusEnum.Friendly_OffDuty;
				}
		}

		if(savedInstanceState != null)
		{
			CharSequence answer;
			if(savedInstanceState.containsKey("locationcode")) {
				answer = savedInstanceState.getCharSequence("locationcode");
				_txtLocationCode.setText(answer);
			}

			if(savedInstanceState.containsKey("location")) {
				answer = savedInstanceState.getCharSequence("location");
				_txtActualLocation.setText(answer);
			}

			if(savedInstanceState.containsKey("maintainDriving")) {
				_chkMaintainDriving.setChecked(savedInstanceState.getBoolean("maintainDriving"));
			}

			if(savedInstanceState.containsKey("maintainVis")) {
				if (savedInstanceState.getInt("maintainVis") == 0)
					_chkMaintainDriving.setVisibility(View.VISIBLE);
				else
					_chkMaintainDriving.setVisibility(View.GONE);
			}
		}
		else {
			// enable the "Maintain Driving Status" checkbox only when the
			// driving segment can be extended
			_chkMaintainDriving.setChecked(false);
			boolean isExtDrivingSegmentEnabled = controlListener.getMyLogEntryController().getIsExtendDrivingSegmentEnabled();

			if(isExtDrivingSegmentEnabled)
				_chkMaintainDriving.setVisibility(View.VISIBLE);
			else
				_chkMaintainDriving.setVisibility(View.GONE);
		}

		_errorMessage.setVisibility(View.GONE);
		//_errorMessage.setText(Html.fromHtml("<b>Driver's Annotation</b> must be at least four characters long."));

		if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
			boolean shouldShowManualLocation = controlListener.ShouldShowManualLocation();
			boolean areLocationCodesAvail = controlListener.getMyLogEntryController().getAreLocationCodesAvailable();
			showHideActualLocation(shouldShowManualLocation);
			if (shouldShowManualLocation) {
				showHideLocationCode(shouldShowManualLocation && areLocationCodesAvail);
				if (areLocationCodesAvail) {
					AddTextChangedListener();
				}
			}
			else {
				showHideLocationCode(false);
			}
		}
		else {
			showHideLocationCode(false);
			showHideActualLocation(false);
		}
	}

	protected void findControls(View v) {
		RadioGroup radioDutyStatusGroup = (RadioGroup) v.findViewById(R.id.radDutyStatusGroup);
		_radioSleeper = (RadioButton) v.findViewById(R.id.radio_sleeper);
		Button btnSubmitStatus = (Button)v.findViewById(R.id.btnsubmitstatus);
		_cbPersonalConveyance = (CheckBox)v.findViewById(R.id.rcb_chkauthorizepersonalconveyance);
		_cbHyrail = (CheckBox)v.findViewById(R.id.rcb_chkhyrail);
		_cbYardMove = (CheckBox)v.findViewById(R.id.rcb_chkyardmove);
		_cbNonRegDriving = (CheckBox)v.findViewById(R.id.rcb_chknonreg);
		_chkMaintainDriving = (CheckBox)v.findViewById(R.id.chkMainDrivingStatus);

		_linearLayoutAnnotationOnDuty = (LinearLayout)v.findViewById(R.id.rns_linearLayoutAnnotationonduty);
		_linearLayoutAnnotationOffDuty =(LinearLayout)v.findViewById(R.id.rns_linearLayoutAnnotationoffduty);
		_txtAnnotationOnDuty = (EditText)v.findViewById(R.id.rns_txtannotationonduty);

		_txtAnnotationOffDuty = (EditText)v.findViewById(R.id.rns_txtannotatioffduty);

		_lblLocationCode = (TextView)v.findViewById(R.id.lblLocationCode);
		_lblActualLocation = (TextView)v.findViewById(R.id.lblActualLocation);
		_txtLocationCode = (EditText)v.findViewById(R.id.tvLocationCode);
		_txtActualLocation = (EditText)v.findViewById(R.id.tvActualLocation);

		btnSubmitStatus.setOnClickListener(
	            new OnClickListener() {
	            	public void onClick(View v) {
	            		
	            		actionsListener.handleSubmitButtonClick();
	            	}
	            });


		_errorMessage = (TextView)v.findViewById(R.id.txtError);
		radioDutyStatusGroup.setOnCheckedChangeListener(
				new RadioGroup.OnCheckedChangeListener(){
					public void onCheckedChanged(RadioGroup group, int checkedId) {

						if(checkedId == R.id.radio_onDuty) {
							_dutyStatus = getResources().getString(R.string.onduty);

						}
						else if(checkedId == R.id.radio_offDuty) {
							_dutyStatus = getResources().getString(R.string.offduty);

						}
						else if (checkedId == R.id.radio_offDutyWellSite) {
							_dutyStatus = getResources().getString(R.string.offdutywellsite);

						}
						else if( checkedId == R.id.radio_sleeper) {
							_dutyStatus = getResources().getString(R.string.sleeper);

						}

						actionsListener.HandleDutyStatusChange(_dutyStatus);
					}
				}
		);

		_cbPersonalConveyance.setOnClickListener(
				new OnClickListener() {
					public void onClick(View v) {
						actionsListener.HandlePersonalConveyanceCheckBoxClick();
					}
				});

		_cbHyrail.setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						actionsListener.HandleHyrailCheckBoxClick();
					}
				}
		);

		_cbYardMove.setOnClickListener(
				new OnClickListener() {
					public void onClick(View v) {
						actionsListener.HandleYardMoveCheckBoxClick();
					}
				});

		_cbNonRegDriving.setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						actionsListener.HandleNonRegDrivingCheckBoxClick();
					}
				}
		);
	}

	@Override
    public void onAttach(Activity activity) {
		super.onAttach(activity);
        try {
        	actionsListener = (ISelectDutyStatus.SelectDutyStatusFragActions) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement SelectDutyStatusFragActions");
        }

		try{
			controlListener = (ISelectDutyStatus.SelectDutyStatusFragControllerMethods) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement SelectDutyStatusFragControllerMethods");
		}
    }

	public void setExemptLogType(ExemptLogTypeEnum exemptLogType){
		//if this is an exempt log then hide the sleeper status
		if(exemptLogType.getValue() != ExemptLogTypeEnum.NULL)
			_radioSleeper.setVisibility(View.GONE);
	}

	public RadioButton getOffDutyWellSiteRadioButton() {
		if (_radioOffDutyWellSite == null)
			_radioOffDutyWellSite = (RadioButton)getView().findViewById(R.id.radio_offDutyWellSite);
		return _radioOffDutyWellSite;
	}


	public CheckBox getPersonalConveyanceCheckbox(){
		if(_cbPersonalConveyance == null)
			_cbPersonalConveyance  = (CheckBox)getView().findViewById(R.id.rcb_chkauthorizepersonalconveyance);
		return _cbPersonalConveyance;
	}

	public CheckBox getYardMoveCheckbox(){
		if(_cbYardMove == null)
			_cbYardMove = (CheckBox)getView().findViewById(R.id.rcb_chkyardmove);
		return _cbYardMove;
	}

	public CheckBox getUtilizeHyrailCheckbox(){
		if(_cbHyrail == null)
			_cbHyrail  = (CheckBox)getView().findViewById(R.id.rcb_chkhyrail);
		return _cbHyrail;
	}

	public CheckBox getNonRegDrivingCheckbox(){
		if(_cbNonRegDriving == null)
			_cbNonRegDriving  = (CheckBox)getView().findViewById(R.id.rcb_chknonreg);
		return _cbNonRegDriving;
	}

	public String getLocationText() {
		return _txtActualLocation.getText().toString();
	}

	public EditText getLocationEditText() {
		if (_txtActualLocation == null) {
			_txtActualLocation = (EditText) getView().findViewById(R.id.tvActualLocation);
		}
		return _txtActualLocation;
	}

	public void showAnnotationOnDuty(){

		_linearLayoutAnnotationOnDuty.setVisibility(View.VISIBLE);
	}

	private void showHideLocationCode(boolean shouldShow) {
		if (shouldShow) {
			_lblLocationCode.setVisibility(View.VISIBLE);
			_txtLocationCode.setVisibility(View.VISIBLE);
		}
		else {
			_lblLocationCode.setVisibility(View.GONE);
			_txtLocationCode.setVisibility(View.GONE);
		}
	}

	private void showHideActualLocation(boolean shouldShow) {
		if (shouldShow) {
			_lblActualLocation.setVisibility(View.VISIBLE);
			_txtActualLocation.setVisibility(View.VISIBLE);
		}
		else {
			_lblActualLocation.setVisibility(View.GONE);
			_txtActualLocation.setVisibility(View.GONE);
		}
	}

	public void hideAnnotationOnDuty(){
		_linearLayoutAnnotationOnDuty.setVisibility(View.GONE);
	}

	public void showAnnotationOffDuty(){
		_linearLayoutAnnotationOffDuty.setVisibility(View.VISIBLE);
	}

	public void hideAnnotationOffDuty(){
		_linearLayoutAnnotationOffDuty.setVisibility(View.GONE);
	}
	public EditText getAnnotationOnDuty(){
		if(_txtAnnotationOnDuty == null)
			_txtAnnotationOnDuty =(EditText)getView().findViewById(R.id.rns_txtannotationonduty);
		return _txtAnnotationOnDuty;
	}
	public EditText getAnnotationOffDuty(){
		if(_txtAnnotationOffDuty == null)
			_txtAnnotationOffDuty = (EditText)getView().findViewById(R.id.rns_txtannotatioffduty);
		return _txtAnnotationOffDuty;
	}

	public TextView getErrorMessageTextView(){
		if(_errorMessage == null)
			_errorMessage = (TextView) getView().findViewById(R.id.txtError);
		return _errorMessage;
	}

	public String getInitialDutyStatus(){
		return initialStatusValue;
	}

	public void showAnnotationErrorMessage() {
		_errorMessage.setText(Html.fromHtml(getResources().getString(R.string.requireddriversannotation)));
		_errorMessage.setVisibility(View.VISIBLE);
	}

	public void showLocationMinimumLengthErrorMessage() {
		_errorMessage.setText(Html.fromHtml(getResources().getString(R.string.msgactuallocationminimumlengtherror)));
		_errorMessage.setVisibility(View.VISIBLE);
	}

	public void hideErrorMessage(){
		_errorMessage.setVisibility(View.GONE);
	}

	public LinearLayout getLinearLayoutAnnotationOnDuty() {
		if(_linearLayoutAnnotationOnDuty == null)
			_linearLayoutAnnotationOnDuty = (LinearLayout) getView().findViewById(R.id.rns_linearLayoutAnnotationonduty);
		return _linearLayoutAnnotationOnDuty;
	}

	public LinearLayout getLinearLayoutAnnotationOffDuty() {
		if(_linearLayoutAnnotationOffDuty == null)
			_linearLayoutAnnotationOffDuty = (LinearLayout) getView().findViewById(R.id.rns_linearLayoutAnnotationoffduty);
		return _linearLayoutAnnotationOffDuty;
	}

	private void AddTextChangedListener(){
		_txtLocationCode.addTextChangedListener(new TextWatcher(){
			public void onTextChanged(CharSequence s, int start, int before, int count)
			{
				// decode it, and put the actual location in the textbox
				String code = _txtLocationCode.getText().toString();
				_txtActualLocation.setText("");
				String location = controlListener.getMyLogEntryController().getLocationForCode(code);
				if(location != null)
					_txtActualLocation.setText(location);
			}

			public void afterTextChanged(Editable s){

			}

			public void beforeTextChanged(CharSequence arg0, int arg1,
										  int arg2, int arg3) {
				// TODO Auto-generated method stub
			}
		});
	}
}
