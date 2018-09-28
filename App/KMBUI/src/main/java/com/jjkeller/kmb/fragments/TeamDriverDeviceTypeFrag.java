package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.jjkeller.kmb.interfaces.ITeamDriverDeviceType.TeamDriverDeviceTypeFragActions;
import com.jjkeller.kmb.interfaces.ITeamDriverDeviceType.TeamDriverDeviceTypeFragControllerMethods;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbui.R;

public class TeamDriverDeviceTypeFrag extends BaseFragment{
	TeamDriverDeviceTypeFragActions actionsListener;
	TeamDriverDeviceTypeFragControllerMethods controlListener;
	
	private Button _btnSharedDevice;
	private Button _btnSeparateDevice;

	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        // Inflate the layout for this fragment

		View v = inflater.inflate(R.layout.f_teamdriverdevicetype, container, false);
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
		_btnSharedDevice = (Button)v.findViewById(R.id.btnSharedDevice);
		_btnSeparateDevice = (Button)v.findViewById(R.id.btnSeparateDevice);

		_btnSharedDevice.setOnClickListener(
	            new OnClickListener() {
	            	public void onClick(View v) {
	            		actionsListener.handleSharedDeviceButtonClick();
	            	}
	            });

		_btnSeparateDevice.setOnClickListener(
				new OnClickListener() {
	            	public void onClick(View v) {
	            		actionsListener.handleSeparateDeviceButtonClick();
	            	}
	            });
	}
	
	@Override
    public void onAttach(Activity activity) {
		super.onAttach(activity);
        try {
        	actionsListener = (TeamDriverDeviceTypeFragActions) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement TeamDriverDeviceTypeFragActions");
        }
        
        try{
        	controlListener = (TeamDriverDeviceTypeFragControllerMethods) activity;
        } catch (ClassCastException e) {
        	throw new ClassCastException(activity.toString() + " must implement TeamDriverDeviceTypeFragControllerMethods");
        }
    }
}
