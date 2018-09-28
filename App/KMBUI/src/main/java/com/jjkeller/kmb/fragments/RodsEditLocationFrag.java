package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.jjkeller.kmb.interfaces.IRodsEditLocation.RodsEditLocationFragActions;
import com.jjkeller.kmb.interfaces.IRodsEditLocation.RodsEditLocationFragControllerMethods;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.enums.DutyStatusEnum;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;
import com.jjkeller.kmbui.R;

public class RodsEditLocationFrag extends BaseFragment{
	RodsEditLocationFragActions actionsListener;
	RodsEditLocationFragControllerMethods controlListener;
	
	private EditText _tvActualLocation;
	private TextView _lblLocationCodeLabel;
	private CheckBox _chkMaintainDriving;
	private EditText _tvLocationCode;
	private TextView _tvStatus;
	private EmployeeLogEldEvent _evt;
	private TextView _tvTimeStamp;
	private Button _btnSave;
	private Button _btnCancel;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
        // Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.f_rodseditlocation, container, false);
		findControls(v);
		return v;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		this.loadControls(savedInstanceState);
	}
	
	protected void findControls(View v)	{
		_evt = controlListener.getMyController().getLogEventForEdit();
		
		_tvStatus = (TextView)v.findViewById(R.id.tvStatus);
		_tvTimeStamp = (TextView)v.findViewById(R.id.tvTime);
		_chkMaintainDriving = (CheckBox)v.findViewById(R.id.chkMainDrivingStatus);
		
		_btnSave = (Button)v.findViewById(R.id.btnSave);
		_btnSave.setOnClickListener(
	            new OnClickListener() {
	            	public void onClick(View v) {
	            		actionsListener.handleSaveButtonClick();
	            	}
	            });
		
		_btnCancel = (Button)v.findViewById(R.id.btnCancel);
		_btnCancel.setOnClickListener(
				new OnClickListener() {
					public void onClick(View v){
						actionsListener.handleCancelButtonClick();
					}
				});
		
		_lblLocationCodeLabel = (TextView)v.findViewById(R.id.lblLocationCode);
		_tvLocationCode = (EditText)v.findViewById(R.id.tvLocationCode);
		_tvActualLocation = (EditText)v.findViewById(R.id.tvActualLocation);
		
		_tvLocationCode.addTextChangedListener(new TextWatcher(){
			public void onTextChanged(CharSequence s, int start, int before, int count)
			{
				// decode it, and put the actual location in the textbox
				String code = _tvLocationCode.getText().toString();
				_tvActualLocation.setText("");
				String location = controlListener.getMyController().getLocationForCode(code);
				if(location != null)
					_tvActualLocation.setText(location);
			}
			
			public void afterTextChanged(Editable s){
				
			}

			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
				// TODO Auto-generated method stub				
			}			
		});		
	}
	

	@Override
	public void onSaveInstanceState(Bundle outState)
	{	
		// 10/3/11 JHM - Don't store state (likely blank values) if the FetchLocalData task hasn't completed.
		// 8/3/12 ACM - getLocalDataTask checks if FetchLocalData finished or not (true = finished)
		if(actionsListener.getLocalDataTask() == true){
			outState.putString("status", _tvStatus.getText().toString());
			outState.putString("time", _tvTimeStamp.getText().toString());
			outState.putString("locationcode", _tvLocationCode.getText().toString());
			outState.putString("location", _tvActualLocation.getText().toString());
			outState.putBoolean("maintainDriving", _chkMaintainDriving.isChecked());
			outState.putInt("maintainVis", _chkMaintainDriving.getVisibility());
		}
		super.onSaveInstanceState(outState);
	}
	
	protected void loadControls(Bundle savedInstanceState) {
		// 10/3/11 JHM - Only load from state if one of the expected values is contained in bundle.
		if(savedInstanceState != null &&
			(savedInstanceState.containsKey("status") || savedInstanceState.containsKey("time") ||
			savedInstanceState.containsKey("locationcode") || savedInstanceState.containsKey("location") ||
			savedInstanceState.containsKey("maintainDriving") || savedInstanceState.containsKey("maintainVis")))
		{
			CharSequence answer = savedInstanceState.getCharSequence("status");
			_tvStatus.setText(answer);

			answer = savedInstanceState.getCharSequence("time");
			_tvTimeStamp.setText(answer);
					
			answer = savedInstanceState.getCharSequence("locationcode");
			_tvLocationCode.setText(answer);
					
			answer = savedInstanceState.getCharSequence("location");
			_tvActualLocation.setText(answer);
			
			_chkMaintainDriving.setChecked(savedInstanceState.getBoolean("maintainDriving"));
			if(savedInstanceState.getInt("maintainVis") == 0)
				_chkMaintainDriving.setVisibility(View.VISIBLE);
			else	
				_chkMaintainDriving.setVisibility(View.GONE);
		}
		else
		{
            boolean isMandateEnabled = GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled();

            DutyStatusEnum statusEnum = controlListener.getMyController().GetCurrentEventValues().dutyStatusEnum;
			String statusText = ((BaseActivity)getActivity()).GetDutyStatusDisplayText(statusEnum, _evt.getRuleSet());
			_tvStatus.setText(statusText);
			_tvTimeStamp.setText(DateUtility.createHomeTerminalTimeString(_evt.getStartTime(), isMandateEnabled));
			_tvActualLocation.setText(_evt.getLocation().ToLocationString());
					
			// enable the "Maintain Driving Status" checkbox only when the		
			// driving segment can be extended
			_chkMaintainDriving.setChecked(false);
			boolean isExtDrivingSegmentEnabled = controlListener.getMyController().getIsExtendDrivingSegmentEnabled();

			if(isExtDrivingSegmentEnabled)
				_chkMaintainDriving.setVisibility(View.VISIBLE);
			else
				_chkMaintainDriving.setVisibility(View.GONE);
		}	
		
		if(controlListener.getMyController().getAreLocationCodesAvailable())
		{
			// location codes are in use
			_lblLocationCodeLabel.setVisibility(View.VISIBLE);
			_tvLocationCode.setVisibility(View.VISIBLE);

			_tvLocationCode.requestFocus();
		}
		else
		{
			// no location codes are available, so turn off those fields
			_lblLocationCodeLabel.setVisibility(View.GONE);
			_tvLocationCode.setVisibility(View.GONE);
			
			_tvActualLocation.requestFocus();
					
		}
				
		// if the location has already been decoded, then do not allow edit
		// 7/15/13 AAZ
		// Removing Check for is DeviceAvailable per Defect 16888 when disconnected, can edit GPS locations
		if(!_evt.getLocation().getGpsInfo().getDecodedInfo().IsEmpty())// && EobrReader.getIsEobrDeviceAvailable())
		{
			// if location codes are available, hide location code fields
			if(controlListener.getMyController().getAreLocationCodesAvailable())
			{
				_lblLocationCodeLabel.setVisibility(View.GONE);
				_tvLocationCode.setVisibility(View.GONE);
			}

			_tvActualLocation.setEnabled(false);
					
			actionsListener.showMessage(getActivity().getResources().getString(R.string.msggpsdecodedlocation));
		}
	}
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
        	actionsListener = (RodsEditLocationFragActions) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement RodsEditLocationFragActions");
        }
        
        try{
        	controlListener = (RodsEditLocationFragControllerMethods) activity;
        } catch (ClassCastException e) {
        	throw new ClassCastException(activity.toString() + " must implement RodsEditLocationFragControllerMethods");
        }
    }
	
	public EditText getActualLocationEditText(){
		if(_tvActualLocation == null)
			_tvActualLocation = (EditText)getView().findViewById(R.id.tvActualLocation);
		return _tvActualLocation;
	}
	
	public CheckBox getMaintainDrivingCheckBox(){
		if(_chkMaintainDriving == null)
			_chkMaintainDriving = (CheckBox)getView().findViewById(R.id.chkMainDrivingStatus);
		return _chkMaintainDriving;
	}
}
