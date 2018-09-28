package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.jjkeller.kmb.interfaces.ITeamDrvAdd.TeamDriverAddFragActions;
import com.jjkeller.kmb.interfaces.ITeamDrvAdd.TeamDriverAddFragControllerMethods;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbui.R;

import java.util.Calendar;

public class TeamDrvAddFrag extends BaseFragment{
	TeamDriverAddFragActions actionsListener;
	TeamDriverAddFragControllerMethods controlListener;
	
	private EditText _txtEmpCode;
	private EditText _txtDisplayName;
	private CheckBox _chkStart;
	private Button _btnExactTime;
	private Button _btnDownload;
	private Button _btnOK;
	private Button _btnCancel;	
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        // Inflate the layout for this fragment

		View v = inflater.inflate(R.layout.f_teamdrvadd, container, false);
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

        _btnDownload.setEnabled(controlListener.getMyController().getIsNetworkAvailable());

        _txtEmpCode.requestFocus();
	}
	
	protected void findControls(View v){	
		_txtEmpCode = (EditText)v.findViewById(R.id.txtEmpCode);
		_txtDisplayName = (EditText)v.findViewById(R.id.txtDisplayName);
		_chkStart = (CheckBox)v.findViewById(R.id.chkStart);
		_btnExactTime = (Button)v.findViewById(R.id.btnExactTime);
		_btnDownload = (Button)v.findViewById(R.id.btnDownload);
		_btnOK = (Button)v.findViewById(R.id.btnOK);
		_btnCancel = (Button)v.findViewById(R.id.btnCancel);
		
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

		_btnDownload.setOnClickListener(
	            new OnClickListener() {
	            	public void onClick(View v) {
	            		actionsListener.handleDownloadButtonClick();
	            	}
	            });

		_chkStart.setOnClickListener(
	            new OnClickListener() {
	            	public void onClick(View v) {
	            		actionsListener.handleTeamFromStartChecked();
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
        	actionsListener = (TeamDriverAddFragActions) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement TeamDriverAddFragActions");
        }
        
        try{
        	controlListener = (TeamDriverAddFragControllerMethods) activity;
        } catch (ClassCastException e) {
        	throw new ClassCastException(activity.toString() + " must implement TeamDriverAddFragControllerMethods");
        }
    }
	
	public EditText getEmpCodeEditText(){
		if(_txtEmpCode == null)
			_txtEmpCode = (EditText)getView().findViewById(R.id.txtEmpCode);
		return _txtEmpCode;
	}
	
	public EditText getDisplayNameEditText(){
		if(_txtDisplayName == null)
			_txtDisplayName = (EditText)getView().findViewById(R.id.txtDisplayName);
		return _txtDisplayName;
	}
	
	public Button getExactTimeButton(){
		if(_btnExactTime == null)
			_btnExactTime = (Button)getView().findViewById(R.id.btnExactTime);
		return _btnExactTime;
	}

	public CheckBox getStartCheckbox(){
		if(_chkStart == null)
			_chkStart = (CheckBox)getView().findViewById(R.id.chkStart);
		return _chkStart;
	}
}
