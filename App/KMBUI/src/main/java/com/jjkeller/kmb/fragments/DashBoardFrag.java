package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.jjkeller.kmb.interfaces.IDashBoard.DashBoardFragActions;
import com.jjkeller.kmb.interfaces.IDashBoard.DashBoardFragControllerMethods;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbui.R;

import java.util.ArrayList;

public class DashBoardFrag extends BaseFragment{
	DashBoardFragActions actionsListener;
	DashBoardFragControllerMethods controlListener;
	
	private EditText _txtDesiredSpeed;
	private TextView _lblEngine;
	private TextView _lblCurrentSpeed;
	private TextView _lblTachometer;
	private TextView _lblOdometer;
	private Spinner _cboDeviceError;
	private EditText _txtTabCmd;
	private float _currentSpeed = 0F;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        // Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.f_dashboard, container, false);
		findControls(v);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		this.loadControls();
	}
	
	private void loadControls(){
		controlListener.getMyController().SuspendReading();
		try{
			controlListener.CheckEngine();
            if (_currentSpeed >= 0)
            {
                _txtDesiredSpeed.setText(Float.toString(_currentSpeed));
            }
            
            // Add device errors to combobox
            ArrayList<String> deviceErrorList = new ArrayList<String>();
            deviceErrorList.add(getResources().getString(R.string.deviceerror_none));
            deviceErrorList.add(getResources().getString(R.string.deviceerror_gps));
            deviceErrorList.add(getResources().getString(R.string.deviceerror_speedometer));
            deviceErrorList.add(getResources().getString(R.string.deviceerror_odometer));
            deviceErrorList.add(getResources().getString(R.string.deviceerror_memfulleobr));
            deviceErrorList.add(getResources().getString(R.string.deviceerror_rtc));
            deviceErrorList.add(getResources().getString(R.string.deviceerror_jbus));
            deviceErrorList.add(getResources().getString(R.string.deviceerror_externalflash));
            deviceErrorList.add(getResources().getString(R.string.deviceerror_internaleobr));
            deviceErrorList.add(getResources().getString(R.string.deviceerror_internalflash));

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.kmb_spinner_item, deviceErrorList);
    		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    		_cboDeviceError.setAdapter(adapter);
        	
    		// Start Timer
    		actionsListener.startTimer();
		}
		finally{
			controlListener.getMyController().ResumeReading();
		}
	}
	
	protected void findControls(View v){
		_lblEngine = (TextView)v.findViewById(R.id.lblEngineVal);	
		_lblCurrentSpeed = (TextView)v.findViewById(R.id.lblCurrentSpeedVal);	
		_lblTachometer = (TextView)v.findViewById(R.id.lblTachometerVal);	
		_lblOdometer = (TextView)v.findViewById(R.id.lblOdometerVal);	
		_txtDesiredSpeed = (EditText)v.findViewById(R.id.txtDesiredSpeed);	
		_cboDeviceError = (Spinner)v.findViewById(R.id.cboDeviceError);
		_txtTabCmd = (EditText)v.findViewById(R.id.txtTabCmd);
	}
	
	@Override
    public void onAttach(Activity activity) {
		super.onAttach(activity);
        try {
        	actionsListener = (DashBoardFragActions) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement DailyPasswordFragActions");
        }
        
        try{
        	controlListener = (DashBoardFragControllerMethods) activity;
        } catch (ClassCastException e) {
        	throw new ClassCastException(activity.toString() + " must implement DailyPasswordFragControllerMethods");
        }
    }
    
    public float getCurrentSpeed()
    {
    	return _currentSpeed;
    }
    
    public void setCurrentSpeed(float speed)
    {
    	_currentSpeed = speed;
    }
    
	public Spinner getDeviceErrorSpinner()
	{
		if(_cboDeviceError == null)
			_cboDeviceError = (Spinner)getView().findViewById(R.id.cboDeviceError);
		return _cboDeviceError;
	}
	
	public TextView getEngineTextView()
	{
		if(_lblEngine == null)
			_lblEngine = (TextView)getView().findViewById(R.id.lblEngineVal);
		return _lblEngine;
	}
	
	public TextView getCurrentSpeedTextView()
	{
		if(_lblCurrentSpeed == null)
			_lblCurrentSpeed = (TextView)getView().findViewById(R.id.lblCurrentSpeedVal);
		return _lblCurrentSpeed;
	}
	
	public TextView getTachometerTextView()
	{
		if(_lblTachometer == null)
			_lblTachometer = (TextView)getView().findViewById(R.id.lblTachometerVal);
		return _lblTachometer;
	}
	
	public TextView getOdometerTextView()
	{
		if(_lblOdometer == null)
			_lblOdometer = (TextView)getView().findViewById(R.id.lblOdometerVal);
		return _lblOdometer;
	}
	
	public EditText getDesiredSpeedEditText()
	{
		if(_txtDesiredSpeed == null)
			_txtDesiredSpeed = (EditText)getView().findViewById(R.id.txtDesiredSpeed);
		return _txtDesiredSpeed;
	}
	
	public EditText getDesiredTabCmdEditText() {
		
		if (_txtTabCmd == null) {
			_txtTabCmd = (EditText)getView().findViewById(R.id.txtTabCmd);
		}
		
		return _txtTabCmd;
	}
	
}