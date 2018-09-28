package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.jjkeller.kmb.interfaces.ITeamDriverNextStep.TeamDriverNextStepFragActions;
import com.jjkeller.kmb.interfaces.ITeamDriverNextStep.TeamDriverNextStepFragControllerMethods;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbui.R;


public class TeamDriverNextStepFrag extends BaseFragment{
	TeamDriverNextStepFragActions actionsListener;
	TeamDriverNextStepFragControllerMethods controlListener;
	
	private Button _btnLogin;
	private Button _btnDashboard;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        // Inflate the layout for this fragment

		View v = inflater.inflate(R.layout.f_teamdrivernextstep, container, false);
		findControls(v);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		this.loadControls();
	}
	
	private void loadControls(){
	}
	
	protected void findControls(View v){	
		_btnLogin = (Button)v.findViewById(R.id.btnLogin);
		_btnDashboard = (Button)v.findViewById(R.id.btnDashboard);

		_btnLogin.setOnClickListener(
	            new OnClickListener() {
	            	public void onClick(View v) {
	            		actionsListener.handleLoginButtonClick();
	            	}
	            });
		_btnDashboard.setOnClickListener(
	            new OnClickListener() {
	            	public void onClick(View v) {
	            		actionsListener.handleDashboardButtonClick();
	            	}
	            });
	}
	
	@Override
    public void onAttach(Activity activity) {
		super.onAttach(activity);
        try {
        	actionsListener = (TeamDriverNextStepFragActions) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement TeamDriverNextStepFragActions");
        }
        
        try{
        	controlListener = (TeamDriverNextStepFragControllerMethods) activity;
        } catch (ClassCastException e) {
        	throw new ClassCastException(activity.toString() + " must implement TeamDriverNextStepFragControllerMethods");
        }
    }
}
