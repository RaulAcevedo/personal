package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.jjkeller.kmb.interfaces.ISubmitLogs.SubmitLogsFragActions;
import com.jjkeller.kmb.interfaces.ISubmitLogs.SubmitLogsFragControllerMethods;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbapi.controller.abstracts.APIControllerBase;
import com.jjkeller.kmbapi.controller.dataaccess.EmployeeLogFacade;
import com.jjkeller.kmbapi.controller.interfaces.IEmployeeLogFacade;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbui.R;

import java.util.List;

public class SubmitLogsFrag extends BaseFragment{
	SubmitLogsFragActions actionsListener;
	SubmitLogsFragControllerMethods controlListener;
	
	private Button _btnSubmit;
    private Button _btnDone;
	private TextView _lblMessage;
	int _localLogCount;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
        // Inflate the layout for this fragment
        this.setRetainInstance(true);
		View v = inflater.inflate(R.layout.f_submitlogs, container, false);
		findControls(v);
		return v;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
        if (savedInstanceState == null) {
            this.loadControls();
        }
        else {
            this.getMessageTextView().setText(savedInstanceState.getCharSequence("message"));
            this.getSubmitButton().setEnabled(savedInstanceState.getBoolean("submitEnabled"));
        }
	}

    public void onSaveInstanceState(Bundle outState)
    {
        outState.putCharSequence("message", this.getMessageTextView().getText());
        outState.putBoolean("submitEnabled", this.getSubmitButton().isEnabled());

        super.onSaveInstanceState(outState);
    }
	
	protected void findControls(View v)
	{
		_btnSubmit = (Button)v.findViewById(R.id.btnSubmit);
        _btnDone = (Button)v.findViewById(R.id.btnDone);
		_btnSubmit.setOnClickListener(
                new OnClickListener() {
                    public void onClick(View v) {
                        actionsListener.handleSubmitButtonClick();
                    }
                });
        _btnDone.setOnClickListener(
                new OnClickListener() {
                    public void onClick(View v) {
                        actionsListener.handleDoneButtonClick();
                    }
                });

		_lblMessage = (TextView)v.findViewById(R.id.lblMessage);
	}
	
	protected void loadControls()
	{
		_localLogCount = GetLocalLogCount();
        _lblMessage.setText("");
        if (_localLogCount > 0)
            _btnSubmit.setEnabled(true);
        else
        {
        	Toast.makeText(getActivity(), this.getString(R.string.msgnologsfoundtosubmit), Toast.LENGTH_LONG).show();
            _btnSubmit.setEnabled(false);
        }
	}

    private int GetLocalLogCount()
    {
        APIControllerBase listenerCast = (APIControllerBase)this.controlListener.getMyController();
        IEmployeeLogFacade facade = new EmployeeLogFacade(listenerCast.getContext(), listenerCast.getCurrentUser());
        List<EmployeeLog> localLogList = facade.GetLocalLogList(true);

        return localLogList.size();
    }
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
        	actionsListener = (SubmitLogsFragActions) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement SubmitLogsFragActions");
        }
        
        try{
        	controlListener = (SubmitLogsFragControllerMethods) activity;
        } catch (ClassCastException e) {
        	throw new ClassCastException(activity.toString() + " must implement SubmitLogsControllerMethods");
        }
    }
	
	public TextView getMessageTextView(){
		if(_lblMessage == null)
			_lblMessage = (TextView)getView().findViewById(R.id.lblMessage);
		return _lblMessage;
	}
	
	public Button getSubmitButton(){
		if(_btnSubmit == null)
			_btnSubmit = (Button)getView().findViewById(R.id.btnSubmit);
		return _btnSubmit;
	}

    public Button getDoneButton(){
        if(_btnDone == null)
            _btnDone = (Button)getView().findViewById(R.id.btnDone);
        return _btnDone;
    }
}
