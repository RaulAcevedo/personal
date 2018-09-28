package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.jjkeller.kmb.RodsNewStatus;
import com.jjkeller.kmb.interfaces.IRodsNewStatus.RodsNewStatusFragActions;
import com.jjkeller.kmb.interfaces.IRodsNewStatus.RodsNewStatusFragControllerMethods;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.enums.DutyStatusEnum;
import com.jjkeller.kmbui.R;
import java.util.Date;
import java.util.List;

public class RodsNewStatusFrag extends BaseFragment{
	RodsNewStatusFragActions actionsListener;
	RodsNewStatusFragControllerMethods controlListener;

	private StringBuilder _sb;
	private DutyStatusEnum _currentDutyStatus;

	private TextView _tvrns_lbltime;
	private TextView _errorMessage;
	private CheckBox _cbDrivingStatus;
	private CheckBox _cbPersonalConveyance;
	private CheckBox _cbUtilizeHyRail;
	private CheckBox _cbYardMove;
	private CheckBox _cbNonRegDriving;
	private TextView _lblAnnotation;
	private TextView _lblAnnotationAsterisk;
	private EditText _txtAnnotation;
	private Spinner _spinner_rns_spndutystatus;
	private Button _btnOk;
	private Button _btnCancel;
	//MotionPicture controls
	private TextView _lblMotionPictureProduction;
	private Spinner _cboMotionPictureProduction;
	private TextView _lblMotionPictureAuthority;
	private TextView _txtMotionPictureAuthority;
	//end MotionPicture controls

	private TextView _lblLocationCode;
	private TextView _lblActualLocation;
	private EditText _txtLocationCode;
	private EditText _txtActualLocation;

	// Debug code to allow entry of different gps coordinates to test
	// reverse geocoding against handheld database
	private TextView _lblLatitude;
	private TextView _lblLongitude;
	private TextView _txtLatitude;
	private TextView _txtLongitude;
	// End Debug code

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.f_rodsnewstatus, container, false);
		findControls(v);

		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		this.loadControls(savedInstanceState);
	}

	/**
	 * Called after onRestoreInstanceState(Bundle), onRestart(), or onPause(), for your fragment to start interacting with the user.
	 */
	@Override
	public void onResume() {
		super.onResume();

		// to avoid listeners from firing while initially creating the screen or re-creating it from orientation change, add listeners when all is clear
		addChangeListeners();
	}

	/**
	 * Called as part of the lifecycle when an fragment is going into the background, but has not (yet) been killed.
	 */
	@Override
	public void onPause() {
		super.onPause();

		removeChangeListeners();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (getDutyStatusSpinner() != null)
			outState.putInt(getResources().getString(R.string.state_dutystatus), (int) getDutyStatusSpinner().getSelectedItemId());

		if (getPersonalConveyanceCheckbox() != null && getPersonalConveyanceCheckbox().isChecked())
			outState.putBoolean("isPersonalConveyanceChecked", true);

		if (getYardMoveCheckbox() != null && getYardMoveCheckbox().isChecked())
			outState.putBoolean("isYardMoveChecked", true);

		if (getUtilizeHyrailCheckbox() != null && getUtilizeHyrailCheckbox().isChecked())
			outState.putBoolean("isHyrailChecked", true);

		if (getNonRegDrivingCheckbox() != null && getNonRegDrivingCheckbox().isChecked())
			outState.putBoolean("isNonRegDrivingChecked", true);

		if (getAnnotationEditTextView() != null && getAnnotationEditTextView().getText().length() > 0)
			outState.putString("annotation", getAnnotationEditTextView().getText().toString());

		if (getErrorMessageTextView() != null && getErrorMessageTextView().getVisibility() == View.VISIBLE)
			outState.putString("displayError", getErrorMessageTextView().getText().toString());

		// 10/3/11 JHM - Don't store state (likely blank values) if the FetchLocalData task hasn't completed.
		// 8/3/12 ACM - getLocalDataTask checks if FetchLocalData finished or not (true = finished)
		if(actionsListener.getLocalDataTask() == true){
			outState.putString("locationcode", _txtLocationCode.getText().toString());
			outState.putString("location", _txtActualLocation.getText().toString());
		}
	}

	protected void findControls(View v)	{	
		Bundle lastEventValues = controlListener.getMyController().GetCurrentEventValues()
                .toBundle(
                        getResources().getString(R.string.eventvalues_logdate),
                        getResources().getString(R.string.eventvalues_starttime),
                        getResources().getString(R.string.eventvalues_dutystatus),
						getResources().getString(R.string.eventvalues_isautomaticdrivingevent),
                        getActivity().getBaseContext());

		DutyStatusEnum currentDutyStatus = new DutyStatusEnum(DutyStatusEnum.NULL);
		_currentDutyStatus = currentDutyStatus.valueOf(getActivity().getBaseContext(), lastEventValues.getString(getResources().getString(R.string.eventvalues_dutystatus)));

		Date nextEventTime = controlListener.getMyController().getCurrentClockHomeTerminalTime();
		_sb = new StringBuilder(getResources().getString(R.string.lblTime));

		_sb.append("          ");
		_sb.append(DateUtility.createHomeTerminalTimeString(nextEventTime, GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()));

		_btnOk = (Button)v.findViewById(R.id.rns_btnOk);
		_btnOk.setOnClickListener(
				new OnClickListener() {
					public void onClick(View v) {
						actionsListener.HandleOKButtonClick();
					}
				});

		_btnCancel = (Button)v.findViewById(R.id.rns_btnCancel);
		_btnCancel.setOnClickListener(
				new OnClickListener() {
					public void onClick(View v) {
						actionsListener.HandleCancelButtonClick();
					}
				});

		_tvrns_lbltime = (TextView)v.findViewById(R.id.rns_lbltime);
		_cbDrivingStatus = (CheckBox)v.findViewById(R.id.rns_chkdrvstatus);
		_cbPersonalConveyance = (CheckBox)v.findViewById(R.id.rns_chkauthorizepersonalconveyance);
		_cbYardMove = (CheckBox)v.findViewById((R.id.rns_chkauthorizeyardmove));
		_cbNonRegDriving = (CheckBox)v.findViewById((R.id.rns_chkUtilizeNonRegDriving));
		_cbUtilizeHyRail = (CheckBox)v.findViewById(R.id.rns_utilizehyrailfunctionality);
		_lblAnnotation = (TextView)v.findViewById(R.id.rns_lblannotation);
		_lblAnnotationAsterisk = (TextView)v.findViewById(R.id.rns_lblannotationasterisk);
		_txtAnnotation = (EditText)v.findViewById(R.id.rns_txtannotation);
		_errorMessage = (TextView)v.findViewById(R.id.txtError);
		_spinner_rns_spndutystatus = (Spinner)v.findViewById(R.id.rns_spndutystatus);
		_lblLocationCode = (TextView)v.findViewById(R.id.lblLocationCode);
		_lblActualLocation = (TextView)v.findViewById(R.id.lblActualLocation);
		_txtLocationCode = (EditText)v.findViewById(R.id.tvLocationCode);
		_txtActualLocation = (EditText)v.findViewById(R.id.tvActualLocation);

		// Debug code - test reverse geocoding from handheld db
		_lblLatitude = (TextView)v.findViewById(R.id.rns_lbllatitude);
		_lblLongitude = (TextView)v.findViewById(R.id.rns_lbllongitude);
		_txtLatitude = (TextView)v.findViewById(R.id.rns_txtlatitude);
		_txtLongitude = (TextView)v.findViewById(R.id.rns_txtlongitude);
		// End Debug code

		//MotionPicture
		_lblMotionPictureProduction = (TextView) v.findViewById(R.id.rns_lblMotionPictureProduction);
		_lblMotionPictureProduction.getLayoutParams().height = 0;
		_cboMotionPictureProduction = (Spinner)v.findViewById(R.id.rns_cboMotionPictureProduction);
		_cboMotionPictureProduction.getLayoutParams().height = 0;
		_lblMotionPictureAuthority = (TextView) v.findViewById(R.id.rns_lblMotionPictureAuthority);
		_lblMotionPictureAuthority.getLayoutParams().height = 0;
		_txtMotionPictureAuthority = (TextView) v.findViewById(R.id.rns_txtMotionPictureAuthority);
		_txtMotionPictureAuthority.getLayoutParams().height = 0;

		//End Motion picture
	}

	protected void loadControls(Bundle savedInstanceState) {
		_tvrns_lbltime.setText(_sb.toString());

		actionsListener.loadDutyStatus(_currentDutyStatus);
		if (GlobalState.getInstance().getCompanyConfigSettings(getActivity().getBaseContext()).getIsMotionPictureEnabled()){
			actionsListener.loadMotionPicture();
			_cboMotionPictureProduction.setEnabled(true);
			_lblMotionPictureProduction.setVisibility(View.VISIBLE);
			_lblMotionPictureProduction.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
			_cboMotionPictureProduction.setVisibility(View.VISIBLE);
			_cboMotionPictureProduction.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
			_lblMotionPictureAuthority.setVisibility(View.VISIBLE);
			_lblMotionPictureAuthority.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
			_txtMotionPictureAuthority.setVisibility(View.VISIBLE);
			_txtMotionPictureAuthority.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
		}

		_errorMessage.setVisibility(View.GONE);
		_cbDrivingStatus.setVisibility(View.GONE);
		_cbPersonalConveyance.setVisibility(View.GONE);
		_cbUtilizeHyRail.setVisibility(View.GONE);
		_cbYardMove.setVisibility(View.GONE);
		hideAnnotation();

		if (controlListener.getMyController().getIsExtendDrivingSegmentEnabled()
				&& controlListener.getMyController().IsCurrentUserTheDriver())
			_cbDrivingStatus.setVisibility(View.VISIBLE);

		// DEBUG Code - if debug flag turned on, allow entry of latitude
		// and longitude for testing
		if (GlobalState.getInstance().getFeatureService().getShowDebugFunctions())
		{
			_lblLatitude.setVisibility(View.VISIBLE);
			_lblLongitude.setVisibility(View.VISIBLE);
			_txtLatitude.setVisibility(View.VISIBLE);
			_txtLongitude.setVisibility(View.VISIBLE);
		}
		else
		{
			_lblLatitude.setVisibility(View.GONE);
			_lblLongitude.setVisibility(View.GONE);
			_txtLatitude.setVisibility(View.GONE);
			_txtLongitude.setVisibility(View.GONE);
		}
		// END Debug code

		if(savedInstanceState != null) {
			if (savedInstanceState.containsKey(getResources().getString(R.string.state_dutystatus)))
				_spinner_rns_spndutystatus.setSelection(savedInstanceState.getInt(getResources().getString(R.string.state_dutystatus)), false); // animate = false won't fire initial OnItemSelected

			if (savedInstanceState.containsKey("isPersonalConveyanceChecked") && GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
				_cbPersonalConveyance.setChecked(true);
				showAnnotation();
			}

			if (savedInstanceState.containsKey("isYardMoveChecked") && GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
				_cbYardMove.setChecked(true);
				showAnnotation();
			}

			if (savedInstanceState.containsKey("isHyrailChecked")) {
				_cbUtilizeHyRail.setChecked(true);
				hideAnnotation();
			}

			if (savedInstanceState.containsKey("isNonRegDrivingChecked")) {
				_cbNonRegDriving.setChecked(true);
				hideAnnotation();
			}

			if (savedInstanceState.containsKey("annotation"))
				_txtAnnotation.setText(savedInstanceState.getString("annotation"));

			if (savedInstanceState.containsKey("displayError")) {
				_errorMessage.setText(savedInstanceState.getString("displayError"));
				_errorMessage.setVisibility(View.VISIBLE);
			}

			if (savedInstanceState.containsKey(getResources().getString(R.string.state_motionpictureproduction))) {
				_cboMotionPictureProduction.setSelection(savedInstanceState.getInt(getResources().getString(R.string.state_motionpictureproduction)));
			}

			//location
			CharSequence answer;
			if(savedInstanceState.containsKey("locationcode")) {
				answer = savedInstanceState.getCharSequence("locationcode");
				_txtLocationCode.setText(answer);
			}

			if(savedInstanceState.containsKey("location")) {
				answer = savedInstanceState.getCharSequence("location");
				_txtActualLocation.setText(answer);
			}
		}
		else {
			// set your selection with no animation which causes the on item selected listener to be called.
			// But the listener is null so nothing is run. Then your listener is assigned and onItemSelected will NOT initially fire
			_spinner_rns_spndutystatus.setSelection(0, false);
		}

		// set initial display of controls related to the initialDuty Status
		((RodsNewStatus) getActivity()).ShowControlsForSelectedDutyStatus();

		if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
			boolean shouldShowManualLocation = controlListener.ShouldShowManualLocation();
			boolean areLocationCodesAvail = controlListener.getMyController().getAreLocationCodesAvailable();
			showHideActualLocation(shouldShowManualLocation);
			if (shouldShowManualLocation) {
				_txtActualLocation.setText(this.getLastEnteredManualLocation(GlobalState.getInstance().getCurrentEmployeeLog()));
				showHideLocationCode(areLocationCodesAvail);
				if (areLocationCodesAvail) {
					AddTextChangedListener();
				}
			}
		}
		else {
			showHideLocationCode(false);
			showHideActualLocation(false);
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			actionsListener = (RodsNewStatusFragActions) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement RodsNewStatusFragActions");
		}

		try{
			controlListener = (RodsNewStatusFragControllerMethods) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement RodsNewStatusFragControllerMethods");
		}
	}

	public Spinner getMotionPictureProductionSpinner() {
		if(_cboMotionPictureProduction == null)
			_cboMotionPictureProduction = (Spinner)getView().findViewById(R.id.cboMotionPictureProduction);
		return _cboMotionPictureProduction;
	}

	public TextView getMotionPictureAuthorityTextView() {
		if(_txtMotionPictureAuthority == null)
			_txtMotionPictureAuthority = (TextView)getView().findViewById(R.id.txtMotionPictureAuthority);
		return _txtMotionPictureAuthority;
	}

	public Spinner getDutyStatusSpinner()
	{
		if(_spinner_rns_spndutystatus == null)
			_spinner_rns_spndutystatus = (Spinner)getView().findViewById(R.id.rns_spndutystatus);
		return _spinner_rns_spndutystatus;
	}

	public TextView getLongitudeTextView(){
		if(_txtLongitude == null)
			_txtLongitude = (TextView)getView().findViewById(R.id.rns_txtlongitude);
		return _txtLongitude;
	}

	public TextView getLatitudeTextView(){
		if(_txtLatitude == null)
			_txtLatitude = (TextView)getView().findViewById(R.id.rns_txtlatitude);
		return _txtLatitude;
	}

	public CheckBox getDrivingStatusCheckbox(){
		if(_cbDrivingStatus == null)
			_cbDrivingStatus = (CheckBox)getView().findViewById(R.id.rns_chkdrvstatus);
		return _cbDrivingStatus;
	}

	public CheckBox getPersonalConveyanceCheckbox(){
		if(_cbPersonalConveyance == null)
			_cbPersonalConveyance  = (CheckBox)getView().findViewById(R.id.rns_chkauthorizepersonalconveyance);
		return _cbPersonalConveyance;
	}

	public CheckBox getNonRegDrivingCheckbox(){
		if(_cbNonRegDriving == null)
			_cbNonRegDriving  = (CheckBox)getView().findViewById(R.id.rns_chkUtilizeNonRegDriving);
		return _cbNonRegDriving;
	}

	public CheckBox getYardMoveCheckbox(){
		if(_cbYardMove == null)
			_cbYardMove = (CheckBox)getView().findViewById(R.id.rns_chkauthorizeyardmove);
		return _cbYardMove;
	}

	public CheckBox getUtilizeHyrailCheckbox(){
		if(_cbUtilizeHyRail == null)
			_cbUtilizeHyRail  = (CheckBox)getView().findViewById(R.id.rns_utilizehyrailfunctionality);
		return _cbUtilizeHyRail;
	}

	public TextView getAnnotationTextView() {
		if (_lblAnnotation == null)
			_lblAnnotation = (TextView)getView().findViewById(R.id.rns_lblannotation);
		return _lblAnnotation;
	}

	public TextView getAnnotationAsteriskTextView() {
		if (_lblAnnotationAsterisk == null)
			_lblAnnotationAsterisk = (TextView)getView().findViewById(R.id.rns_lblannotationasterisk);
		return _lblAnnotationAsterisk;
	}

	public EditText getAnnotationEditTextView() {
		if (_txtAnnotation == null)
			_txtAnnotation = (EditText)getView().findViewById(R.id.rns_txtannotation);
		return _txtAnnotation;
	}

	public TextView getErrorMessageTextView() {
		if (_errorMessage == null)
			_errorMessage = (TextView) getView().findViewById(R.id.txtError);
		return _errorMessage;
	}

	public void hideAnnotation() {
		_lblAnnotation.setVisibility(View.GONE);
		_lblAnnotationAsterisk.setVisibility(View.GONE);
		_txtAnnotation.setVisibility(View.GONE);
	}

	public void showAnnotation() {
		_lblAnnotation.setVisibility(View.VISIBLE);
		_lblAnnotationAsterisk.setVisibility(View.VISIBLE);
		_txtAnnotation.setVisibility(View.VISIBLE);
	}

	public void showAnnotationErrorMessage() {
		_errorMessage.setText(Html.fromHtml(getResources().getString(R.string.requireddriversannotation)));
		_errorMessage.setVisibility(View.VISIBLE);
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

	public void showLocationMinimumLengthErrorMessage() {
		_errorMessage.setText(Html.fromHtml(getResources().getString(R.string.msgactuallocationminimumlengtherror)));
		_errorMessage.setVisibility(View.VISIBLE);
	}

	public void hideErrorMessage(){
		_errorMessage.setVisibility(View.GONE);
	}

	public void disableCancelButton(final String displayMessage) {
		_btnCancel.setBackgroundResource(R.drawable.button_blue_disabled);
		_btnCancel.setOnClickListener(null);
		_btnCancel.setOnClickListener(
				new View.OnClickListener() {
					public void onClick(View v) {
						ShowToastMessage(displayMessage);
					}
				});
	}

	/**
	 * VIEW CHANGE LISTENERS REGION
	 */

	private void addChangeListeners() {
		_spinner_rns_spndutystatus.setOnItemSelectedListener(_itemSelectedDutyStatus);
		_cbPersonalConveyance.setOnClickListener(_onClickListenerPersonalConveyance);
		_cbYardMove.setOnClickListener(_onClickListenerYardMove);
		_cbNonRegDriving.setOnClickListener(_onClickListenerNonRegDrivingnew);
		_cbUtilizeHyRail.setOnClickListener(_onClickListenerHyRail);
		_cboMotionPictureProduction.setOnItemSelectedListener(_itemSelectedMotionPictureProduction);
	}

	private void removeChangeListeners() {
		_spinner_rns_spndutystatus.setOnItemSelectedListener(null);
		_cbPersonalConveyance.setOnClickListener(_onClickListenerPersonalConveyance);
		_cbYardMove.setOnClickListener(null);
		_cbNonRegDriving.setOnClickListener(null);
		_cbUtilizeHyRail.setOnClickListener(null);
		_cboMotionPictureProduction.setOnItemSelectedListener(null);
	}

	AdapterView.OnItemSelectedListener _itemSelectedDutyStatus = new AdapterView.OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			actionsListener.HandleDutyStatusChange();
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) { }
	};

	OnClickListener _onClickListenerPersonalConveyance = new OnClickListener() {
		public void onClick(View v) {
			actionsListener.HandlePersonalConveyanceCheckBoxClick();
		}
	};

	OnClickListener	_onClickListenerYardMove = new OnClickListener() {
		public void onClick(View v) {
			actionsListener.HandleYardMoveCheckBoxClick();
		}
	};

	OnClickListener	_onClickListenerNonRegDrivingnew = new OnClickListener() {
		public void onClick(View v) {
			actionsListener.HandleNonRegDrivingCheckBoxClick();
		}
	};

	OnClickListener _onClickListenerHyRail = new OnClickListener() {
		public void onClick(View v) {
			actionsListener.HandleHyrailCheckBoxClick();
		}
	};

	AdapterView.OnItemSelectedListener _itemSelectedMotionPictureProduction = new AdapterView.OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			actionsListener.handleMotionPictureProductionSelect();		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) { }
	};

	private void AddTextChangedListener(){
		_txtLocationCode.addTextChangedListener(new TextWatcher(){
			public void onTextChanged(CharSequence s, int start, int before, int count)
			{
				// decode it, and put the actual location in the textbox
				String code = _txtLocationCode.getText().toString();
				_txtActualLocation.setText("");
				String location = controlListener.getMyController().getLocationForCode(code);
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
