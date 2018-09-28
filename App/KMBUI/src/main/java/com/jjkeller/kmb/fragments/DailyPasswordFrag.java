package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.jjkeller.kmb.interfaces.IDailyPassword.DailyPasswordFragActions;
import com.jjkeller.kmb.interfaces.IDailyPassword.DailyPasswordFragControllerMethods;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbui.R;

public class DailyPasswordFrag extends BaseFragment{
	DailyPasswordFragActions actionsListener;
	DailyPasswordFragControllerMethods controlListener;
	
	private TextView _tvDailyPassword;
	private Button _btnOk;
	private Button _btnCancel;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        // Inflate the layout for this fragment

		View v = inflater.inflate(R.layout.f_dailypassword, container, false);
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
		_tvDailyPassword = (TextView)v.findViewById(R.id.tvDailyPassword);
		_btnOk = (Button)v.findViewById(R.id.cp_btnOk);
		_btnOk.setOnClickListener
		(
				new OnClickListener(){
					public void onClick(View v){
						actionsListener.handleOkButton();
					}					
				});
		
		_btnCancel = (Button)v.findViewById(R.id.cp_btnCancel);
		_btnCancel.setOnClickListener
		(
				new OnClickListener(){
					public void onClick(View v){
						actionsListener.handleCancelButton();
					}					
				});
	}
	
	@Override
    public void onAttach(Activity activity) {
		super.onAttach(activity);
        try {
        	actionsListener = (DailyPasswordFragActions) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement DailyPasswordFragActions");
        }
        
        try{
        	controlListener = (DailyPasswordFragControllerMethods) activity;
        } catch (ClassCastException e) {
        	throw new ClassCastException(activity.toString() + " must implement DailyPasswordFragControllerMethods");
        }
    }
	
	public TextView getDailyPasswordTextView()
	{
		if(_tvDailyPassword == null)
			_tvDailyPassword = (TextView)getView().findViewById(R.id.tvDailyPassword);
		return _tvDailyPassword;
	}
}
