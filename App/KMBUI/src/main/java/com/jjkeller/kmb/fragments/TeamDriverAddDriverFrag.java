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
import android.widget.TextView;

import com.jjkeller.kmb.interfaces.ITeamDriverAddDriver.TeamDriverAddDriverFragActions;
import com.jjkeller.kmb.interfaces.ITeamDriverAddDriver.TeamDriverAddDriverFragControllerMethods;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbapi.configuration.LoginCredentials;
import com.jjkeller.kmbui.R;

import java.util.Calendar;

public class TeamDriverAddDriverFrag extends BaseFragment{
	TeamDriverAddDriverFragActions actionsListener;
	TeamDriverAddDriverFragControllerMethods controlListener;

	private EditText _txtDriverUserName;
	private TextView _lblDriverFullName;
	private CheckBox _chkStart;
	private Button _btnExactTime;
	private Button _btnCheckName;
	private Button _btnOK;
	private Button _btnCancel;	
	
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        // Inflate the layout for this fragment

		View v = inflater.inflate(R.layout.f_teamdriveradddriver, container, false);
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

		_txtDriverUserName.requestFocus();
	}
	
	protected void findControls(View v){	
		_txtDriverUserName = (EditText)v.findViewById(R.id.txtDriverUserName);
		_lblDriverFullName = (TextView)v.findViewById(R.id.lblDriverFullNameValue);
		_chkStart = (CheckBox)v.findViewById(R.id.chkStart);
		_btnExactTime = (Button)v.findViewById(R.id.btnExactTime);
		_btnCheckName = (Button)v.findViewById(R.id.btnCheckName);
		_btnOK = (Button)v.findViewById(R.id.btnOK);
		_btnCancel = (Button)v.findViewById(R.id.btnCancel);
		
		_lblDriverFullName.setVisibility(View.VISIBLE);
		
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

		_btnCheckName.setOnClickListener(
	            new OnClickListener() {
	            	public void onClick(View v) {
	            		actionsListener.handleCheckNameButtonClick();
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
        	actionsListener = (TeamDriverAddDriverFragActions) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement TeamDriverAddDriverFragActions");
        }
        
        try{
        	controlListener = (TeamDriverAddDriverFragControllerMethods) activity;
        } catch (ClassCastException e) {
        	throw new ClassCastException(activity.toString() + " must implement TeamDriverAddDriverFragControllerMethods");
        }
    }
	
	private String _employeeCode = ""; 
	public String getEmployeeCode() { 
		return _employeeCode; 
	}
	
	private LoginCredentials _teamDriverAuthenticationInfo = new LoginCredentials(); 
	public LoginCredentials getTeamDriverAuthenticationInfo(){ 
		return _teamDriverAuthenticationInfo;  
	}
	
	public void setTeamDriverAuthenicationInfo(LoginCredentials value){ 
		_teamDriverAuthenticationInfo = value;
		
		if (value != null && value.getEmployeeFullName() != null)
		{
			// 	Set the Driver Full Name
			this.getDriverFullNameText().setText(value.getEmployeeFullName());
			this._employeeCode = value.getEmployeeCode(); 
		}
		else
		{
			this.getDriverFullNameText().setText(""); 
			this._employeeCode = "";
		}
			
	}
	
	public EditText getDriverUserNameEditText(){
		if(_txtDriverUserName == null)
			_txtDriverUserName = (EditText)getView().findViewById(R.id.txtDriverUserName);
		return _txtDriverUserName;
	}
	
	public TextView getDriverFullNameText(){
		if(_lblDriverFullName == null)
			_lblDriverFullName = (TextView)getView().findViewById(R.id.lblDriverFullNameValue);
		return _lblDriverFullName;
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
