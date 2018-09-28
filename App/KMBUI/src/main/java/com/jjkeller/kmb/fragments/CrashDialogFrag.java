package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.jjkeller.kmb.interfaces.ICrashDialog.CrashFragActions;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbui.R;

public class CrashDialogFrag extends BaseFragment
{
	private CrashFragActions _actionsListener;
	private TextView _txtCrashMessage;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
        // Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.f_crashdialog, container, false);
		findControls(v);
        _actionsListener.onViewCreated();
		return v;
	}
	
	protected void findControls(View v)
	{
		_txtCrashMessage = (TextView)v.findViewById(R.id.txtCrashMessage);
		
		View closeButton = v.findViewById(R.id.btnClose);
		if (closeButton != null) {
	        ((Button)closeButton).setOnClickListener(
	            	new View.OnClickListener() {
	            		public void onClick(View view) {
	            			_actionsListener.handleCloseButtonClick();
	            		}
	            	}
	            );
		}
	}
	
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
        	_actionsListener = (CrashFragActions) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement CrashFragActions");
        }
    }
	
	public TextView getCrashMessageTextView()
	{
		return _txtCrashMessage;
	}
}
