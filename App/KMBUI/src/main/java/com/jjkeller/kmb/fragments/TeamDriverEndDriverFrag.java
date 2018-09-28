package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

import com.jjkeller.kmb.interfaces.ITeamDriverEndDriver.TeamDriverEndDriverFragActions;
import com.jjkeller.kmb.interfaces.ITeamDriverEndDriver.TeamDriverEndDriverFragControllerMethods;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbapi.proxydata.TeamDriver;
import com.jjkeller.kmbui.R;

import java.util.ArrayList;
import java.util.Calendar;

public class TeamDriverEndDriverFrag extends BaseFragment{
	TeamDriverEndDriverFragControllerMethods controlListener;
	TeamDriverEndDriverFragActions actionsListener;
	
	private Spinner _cboDriver;
	private CheckBox _chkEnd;
	private Button _btnExactTime;
	private Button _btnCancel;
	private Button _btnOK;
    private ArrayAdapter<TeamDriver> _teamDriverListAdapter;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        // Inflate the layout for this fragment

		View v = inflater.inflate(R.layout.f_teamdriverenddriver, container, false);
		findControls(v);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		this.loadControls();
	}
	
	private void loadControls(){
		new TimePickerDialogFrag(_btnExactTime);
		Calendar cal = Calendar.getInstance();
		cal.setTime(controlListener.getMyController().getCurrentClockHomeTerminalTime());
		updateTimeDisplay(_btnExactTime, cal);
		
        // build the cbo of available drivers
		ArrayList<TeamDriver> list = controlListener.getMyController().TeamDriversAvailableToBeEnded();
        if (list != null && list.size() > 0)
        {
        	_teamDriverListAdapter = new ArrayAdapter<TeamDriver>(getActivity(), R.layout.kmb_spinner_item, list);
        	_teamDriverListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    		_cboDriver.setAdapter(_teamDriverListAdapter);
            _btnOK.setEnabled(true);
        }
        else
        {
        	Toast.makeText(getActivity(), getString(R.string.msg_noteamdrivertoend), Toast.LENGTH_LONG).show();
            _btnOK.setEnabled(false);
        }
	}
	
	protected void findControls(View v){
		_cboDriver = (Spinner)v.findViewById(R.id.cboTeamDriver);
		_chkEnd = (CheckBox)v.findViewById(R.id.chkEnd);
		_btnExactTime = (Button)v.findViewById(R.id.btnExactTime);
		_btnCancel = (Button)v.findViewById(R.id.btnCancel);

		_btnOK = (Button)v.findViewById(R.id.btnOK);
		_btnOK.setOnClickListener(
	            new OnClickListener() {
	            	public void onClick(View v) {
	            		actionsListener.handleOKButtonClick();
	            	}
	            });

		_btnCancel.setOnClickListener(
	            new OnClickListener() {
	            	public void onClick(View v) {
	            		actionsListener.handleCancelButtonClick();
	            	}
	            });

		_chkEnd.setOnClickListener(
	            new OnClickListener() {
	            	public void onClick(View v) {
	            		actionsListener.handleTeamAtEndChecked();
	            	}
	            });
		
		_btnExactTime.setOnClickListener(
	            new OnClickListener() {
	            	public void onClick(View v) {
	            		new TimePickerDialogFrag(_btnExactTime).show(getFragmentManager(), "time");
	            	}
	            });		
	}
	
	@Override
    public void onAttach(Activity activity) {
		super.onAttach(activity);
        try {
        	actionsListener = (TeamDriverEndDriverFragActions) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement TeamDriverEndDriverFragActions");
        }
        
        try{
        	controlListener = (TeamDriverEndDriverFragControllerMethods) activity;
        } catch (ClassCastException e) {
        	throw new ClassCastException(activity.toString() + " must implement TeamDriverEndDriverFragControllerMethods");
        }
    }
	
	public Button getExactTimeButton(){
		if(_btnExactTime == null)
			_btnExactTime = (Button)getView().findViewById(R.id.btnExactTime);
		return _btnExactTime;
	}
	
	public Spinner getDriverSpinner(){
		if(_cboDriver == null)
			_cboDriver = (Spinner)getView().findViewById(R.id.cboTeamDriver);
		return _cboDriver;
	}
	
	public CheckBox getEndCheckbox(){
		if(_chkEnd == null)
			_chkEnd = (CheckBox)getView().findViewById(R.id.chkEnd);
		return _chkEnd;
	}
	
	public ArrayAdapter<TeamDriver> getDriverListAdapter(){
		return _teamDriverListAdapter;
	}

}