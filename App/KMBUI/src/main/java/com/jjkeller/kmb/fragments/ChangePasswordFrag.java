package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.jjkeller.kmb.interfaces.IChangePassword.ChangePasswordFragActions;
import com.jjkeller.kmb.interfaces.IChangePassword.ChangePasswordFragControllerMethods;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbui.R;

public class ChangePasswordFrag extends BaseFragment{
	ChangePasswordFragActions actionsListener;
	ChangePasswordFragControllerMethods controlListener;
	
	private TextView _tvOldPassword;
	private TextView _tvNewPassword;
	private TextView _tvConfirmPassword;
	private Button _btnOK;
	private Button _btnCancel;	
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
        // Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.f_changepassword, container, false);
		findControls(v);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		this.loadControls();
	}
	
	private void loadControls(){
	}
	
	protected void findControls(View v)
	{
		_tvOldPassword = (TextView)v.findViewById(R.id.tvOldPassword);
		_tvNewPassword = (TextView)v.findViewById(R.id.tvNewPassword);
		_tvConfirmPassword = (TextView)v.findViewById(R.id.tvConfirmPassword);
		_btnCancel = (Button)v.findViewById(R.id.cp_btnCancel);
		_btnOK = (Button)v.findViewById(R.id.cp_btnOk);
		
		_btnOK.setOnClickListener(
			new OnClickListener(){
				public void onClick(View v){
					actionsListener.handleOkButtonClick();
				}
							
			});

		_btnCancel.setOnClickListener(
			new OnClickListener(){
				public void onClick(View v){
					actionsListener.handleCancelButtonClick();
				}
			});
	}
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
        	actionsListener = (ChangePasswordFragActions) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement ChangePasswordFragActions");
        }
        
        try{
        	controlListener = (ChangePasswordFragControllerMethods) activity;
        } catch (ClassCastException e) {
        	throw new ClassCastException(activity.toString() + " must implement ChangePasswordFragControllerMethods");
        }
    }
	
	public TextView getOldPasswordTextView()
	{
		if(_tvOldPassword == null)
			_tvOldPassword = (TextView)getView().findViewById(R.id.tvOldPassword);
		return _tvOldPassword;
	}
	
	public TextView getNewPasswordTextView()
	{
		if(_tvNewPassword == null)
			_tvNewPassword = (TextView)getView().findViewById(R.id.tvNewPassword);
		return _tvNewPassword;
	}
	public TextView getConfirmPasswordTextView()
	{
		if(_tvConfirmPassword == null)
			_tvConfirmPassword = (TextView)getView().findViewById(R.id.tvConfirmPassword);
		return _tvConfirmPassword;
	}
}
