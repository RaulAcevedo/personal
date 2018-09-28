package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jjkeller.kmb.interfaces.IActivation.ActivationFragActions;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbui.R;

public class ActivationFrag extends BaseFragment {
	ActivationFragActions btnClickListener;
	TextView _tvActivationCode;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
        // Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.f_activation, container, false);
		findControls(v);
		return v;
	}
	
	protected void findControls(View v)
	{
		_tvActivationCode = (TextView)v.findViewById(R.id.txtactivationcode);

		v.findViewById(R.id.btnactivate).setOnClickListener(
            new OnClickListener() {
            	public void onClick(View v) {
            		btnClickListener.handleActivateButtonClick();
            	}
            });

		v.findViewById(R.id.btncancel).setOnClickListener(
            new OnClickListener() {
            	public void onClick(View v) {
            		btnClickListener.handleCancelButtonClick();
            	}
            });		
	}
	
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
        	btnClickListener = (ActivationFragActions) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement ActivationFragActions");
        }
    }
    
    public TextView getActivationCodeTextbox()
    {
    	if (_tvActivationCode == null)
    	{
    		_tvActivationCode = (TextView)getView().findViewById(R.id.txtactivationcode);
    	}
    	return _tvActivationCode;
    }
}
