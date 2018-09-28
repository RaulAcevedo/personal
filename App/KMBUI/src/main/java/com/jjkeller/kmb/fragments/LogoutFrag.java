package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.jjkeller.kmb.interfaces.ILogout.LogoutControllerMethods;
import com.jjkeller.kmb.interfaces.ILogout.LogoutFragActions;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.enums.DutyStatusEnum;
import com.jjkeller.kmbui.R;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class LogoutFrag extends BaseFragment {

	private LogoutFragActions actionsListener;
	private LogoutControllerMethods controllerListener;

	private Button _btnExactTime;
	private Button _btnSubmitLogs;
	private Spinner _cboTimeOffset;
	private Spinner _cboDutyStatusOnLogout;
	private TextView _lblDutyStatusOnLogout;
	private TextView _lblActualOffDuty;
	private CheckBox _chkSubmitLogs;
	private TextView _txtError;
	private TextView _lblLocationCode;
	private TextView _lblActualLocation;
	private EditText _txtLocationCode;
	private EditText _txtActualLocation;

	private ArrayAdapter<CharSequence> timeOffsetAdapter;
	private ArrayAdapter<DutyStatusAdapterItem> dutyStatusAdapter;

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
        // Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.f_logout, container, false);
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
		_btnExactTime = (Button)v.findViewById(R.id.btnExactTime);
		_cboTimeOffset = (Spinner)v.findViewById(R.id.cboOffDuty);
		_cboDutyStatusOnLogout = (Spinner)v.findViewById(R.id.cboDutyStatus);
		_lblDutyStatusOnLogout = (TextView)v.findViewById(R.id.lblDutyStatusOnLogout);
		_lblActualOffDuty = (TextView)v.findViewById(R.id.lblActualOffDuty);
        _chkSubmitLogs = (CheckBox)v.findViewById(R.id.chkSubmitLogs);
        _btnSubmitLogs = (Button)v.findViewById(R.id.btnSubmit);
		_txtError = (TextView) v.findViewById(R.id.txtError);
		_lblLocationCode = (TextView)v.findViewById(R.id.lblLocationCode);
		_lblActualLocation = (TextView)v.findViewById(R.id.lblActualLocation);
		_txtLocationCode = (EditText)v.findViewById(R.id.tvLocationCode);
		_txtActualLocation = (EditText)v.findViewById(R.id.tvActualLocation);

		v.findViewById(R.id.btnLogout).setOnClickListener(
	            new OnClickListener() {
	            	public void onClick(View v) {
	            		actionsListener.handleLogoutButtonClick();
	            	}
	            });		
		
		v.findViewById(R.id.btnCancel).setOnClickListener(
	            new OnClickListener() {
	            	public void onClick(View v) {
	            		actionsListener.handleCancelButtonClick();
	            	}
	            });

		v.findViewById(R.id.btnSubmit).setOnClickListener(
				new OnClickListener() {
					public void onClick(View v) {
						actionsListener.handleSubmitButtonClick();
					}
				});

        _btnExactTime.setOnClickListener(
	            new OnClickListener() {
	            	public void onClick(View v) {
	            		ShowTimePickerDialog(_btnExactTime);
	            	}
	            });

		_cboTimeOffset.setOnItemSelectedListener(
        		new AdapterView.OnItemSelectedListener() {
        		    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
        		    	actionsListener.handleDutyStatusTimeSelect();
    			   }
					public void onNothingSelected(AdapterView<?> arg0) {
					}
	            });


	}
	
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
        	actionsListener = (LogoutFragActions) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement LogoutFragActions");
        }
        try {
        	controllerListener = (LogoutControllerMethods) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement LogoutControllerMethods");
        }
    }
    
	private void loadControls(Bundle savedInstanceState)
	{
		timeOffsetAdapter = ArrayAdapter.createFromResource(
                getActivity(), R.array.TimeOffsetKind_array, R.layout.kmb_spinner_item);
        timeOffsetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        _cboTimeOffset.setAdapter(timeOffsetAdapter);
		_txtError.setVisibility(View.GONE);
		_txtError.setText(Html.fromHtml("<b>Driver's Annotation</b> must be at least four characters long."));

		if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
			_chkSubmitLogs.setVisibility(View.GONE);
			dutyStatusAdapter = new ArrayAdapter<>(getActivity(), R.layout.kmb_spinner_item, getDutyStatusItems());
			dutyStatusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			_cboDutyStatusOnLogout.setAdapter(dutyStatusAdapter);

			CharSequence answer;
			if(savedInstanceState != null && savedInstanceState.containsKey("locationcode")) {
				answer = savedInstanceState.getCharSequence("locationcode");
				_txtLocationCode.setText(answer);
			}

			if(savedInstanceState != null && savedInstanceState.containsKey("location")) {
				answer = savedInstanceState.getCharSequence("location");
				_txtActualLocation.setText(answer);
			}

			boolean shouldShowManualLocation = controllerListener.ShouldShowManualLocation();
			boolean areLocationCodesAvail = controllerListener.getMyLogEntryController().getAreLocationCodesAvailable();
			showHideActualLocation(shouldShowManualLocation);
			if (shouldShowManualLocation) {
				_txtActualLocation.setText(this.getLastEnteredManualLocation(GlobalState.getInstance().getCurrentEmployeeLog()));
				showHideLocationCode(shouldShowManualLocation && areLocationCodesAvail);
				if (areLocationCodesAvail) {
					AddTextChangedListener();
				}
			}
			else {
				showHideLocationCode(false);
			}

			if(this.getIsExemptFromELDUse())
			{
				_btnExactTime.setVisibility(View.GONE);
				_cboTimeOffset.setVisibility(View.GONE);
				_cboDutyStatusOnLogout.setVisibility(View.GONE);
				_lblDutyStatusOnLogout.setVisibility(View.GONE);
				_lblActualOffDuty.setVisibility(View.GONE);

			}else {
				_lblDutyStatusOnLogout.setVisibility(View.VISIBLE);
				_cboDutyStatusOnLogout.setVisibility(View.VISIBLE);
				_lblActualOffDuty.setText(getText(R.string.lblLogoutDutyStatusTitle));
				_cboTimeOffset.setPrompt(getString(R.string.TimeOffsetKindMandate_prompt));
			}
		} else {
			_lblDutyStatusOnLogout.setVisibility(View.GONE);
			_cboDutyStatusOnLogout.setVisibility(View.GONE);
            _chkSubmitLogs.setVisibility(View.VISIBLE);
            _btnSubmitLogs.setVisibility(View.GONE);
			_lblActualOffDuty.setText(getText(R.string.lblactualoffdutytitle));
			showHideLocationCode(false);
			showHideActualLocation(false);
		}

		SharedPreferences userPref = getActivity().getSharedPreferences(this.getString(R.string.sharedpreferencefile), 0);
		String logoutOffset = userPref.getString(this.getString(R.string.logoutoffset), "");
		if (!logoutOffset.equals(""))
		{
			_cboTimeOffset.setSelection(timeOffsetAdapter.getPosition(logoutOffset));
		}

        _btnExactTime.setEnabled(false);
        Calendar c = Calendar.getInstance();
        if (savedInstanceState != null){
        	_cboTimeOffset.setSelection(savedInstanceState.getInt(getResources().getString(R.string.state_timeoffset)));

        	try {
        		c.setTime(DateUtility.getHomeTerminalTime12HourFormat().parse(savedInstanceState.getString("time")));
        	}
        	catch(ParseException e){
            	Log.e("UnhandledCatch", e.getMessage() + ": " + Log.getStackTraceString(e));
        	}
        }
        else
        	c.setTime(controllerListener.getCurrentClockHomeTerminalTime());

        updateTimeDisplay(_btnExactTime, c);		
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

	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		if(_btnExactTime != null)
			outState.putString("time", _btnExactTime.getText().toString());
		
		if(_cboTimeOffset != null)
			outState.putInt(getResources().getString(R.string.state_timeoffset), (int)_cboTimeOffset.getSelectedItemId());

		if (_txtLocationCode.getText().toString().length() > 0) {
			outState.putString("locationcode", _txtLocationCode.getText().toString());
		}

		if (_txtActualLocation.getText().toString().length() > 0) {
			outState.putString("location", _txtActualLocation.getText().toString());
		}

		super.onSaveInstanceState(outState);
	}
	
	public Button GetExactTimeButton()
	{
    	if (_btnExactTime == null)
    	{
    		_btnExactTime = (Button)getView().findViewById(R.id.btnExactTime);
    	}
    	return _btnExactTime;		
	}

	public String GetSelectedTimeOffset()
	{
		if (timeOffsetAdapter == null || _cboTimeOffset == null)
			return "";
		else
			return timeOffsetAdapter.getItem(_cboTimeOffset.getSelectedItemPosition()).toString();
	}

	public int GetSelectedDutyStatus()
	{
		if (dutyStatusAdapter == null || _cboDutyStatusOnLogout == null)
			return -1;

		DutyStatusAdapterItem selectedItem = dutyStatusAdapter.getItem(_cboDutyStatusOnLogout.getSelectedItemPosition());
		return selectedItem == null ? -1 : selectedItem.getValue();
	}


	public Boolean GetSubmitLogsCheckBoxValue() {
		return _chkSubmitLogs != null && _chkSubmitLogs.isChecked();
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

	public void showLocationErrorMessage() {
		_txtError.setText(Html.fromHtml("<b>Location</b> is required."));
		_txtError.setVisibility(View.VISIBLE);
	}

	public void hideErrorMessage(){
		_txtError.setVisibility(View.GONE);
	}

	private List<DutyStatusAdapterItem> getDutyStatusItems() {
		ArrayList<DutyStatusAdapterItem> items = new ArrayList<>();
		items.add(new DutyStatusAdapterItem(DutyStatusEnum.OFFDUTY));
		items.add(new DutyStatusAdapterItem(DutyStatusEnum.ONDUTY));
		items.add(new DutyStatusAdapterItem(DutyStatusEnum.SLEEPER));
		if (controllerListener.canUseOffDutyWellSite())
			items.add(new DutyStatusAdapterItem(DutyStatusEnum.OFFDUTYWELLSITE));
		return items;
	}

	private static class DutyStatusAdapterItem extends DutyStatusEnum {
		DutyStatusAdapterItem(int value) {
			super(value);
		}

		@Override
		public String toString() {
			return this.getString(GlobalState.getInstance());
		}
	}

	private void AddTextChangedListener(){
		_txtLocationCode.addTextChangedListener(new TextWatcher(){
			public void onTextChanged(CharSequence s, int start, int before, int count)
			{
				// decode it, and put the actual location in the textbox
				String code = _txtLocationCode.getText().toString();
				_txtActualLocation.setText("");
				String location = controllerListener.getMyLogEntryController().getLocationForCode(code);
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
