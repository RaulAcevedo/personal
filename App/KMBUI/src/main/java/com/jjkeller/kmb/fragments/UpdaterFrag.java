package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.jjkeller.kmb.interfaces.IUpdater.UpdaterFragActions;
import com.jjkeller.kmb.interfaces.IUpdater.UpdaterFragControllerMethods;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbui.R;

public class UpdaterFrag extends BaseFragment {

	UpdaterFragControllerMethods controlListener;
	UpdaterFragActions actionsListener;

	private TextView _lblMessage;
	private Button _btnDownload;
	private Button _btnDone;

	// InstanceState keys
	public static final String UPDATE_AVAILABLE = "updateAvailable";
	public static final String MESSAGE = "message";
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
        // Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.f_updater, container, false);
		findControls(v);
		return v;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		this.loadControls(savedInstanceState);
	}
	
	protected void findControls(View v)
	{
		_lblMessage = (TextView)v.findViewById(R.id.lblmessage);
		_btnDownload = (Button)v.findViewById(R.id.updater_btnDownload);

		_btnDownload.setOnClickListener(
	            new OnClickListener() {
	            	public void onClick(View v) {
	            		actionsListener.handleDownloadButtonClick();
	            	}
	            });

		_btnDone = (Button)v.findViewById(R.id.updater_btnDone);

		_btnDone.setOnClickListener(
				new OnClickListener() {
					public void onClick(View v) {
						actionsListener.handleDoneButtonClick();
					}
				});
	}
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        
        try {
        	actionsListener = (UpdaterFragActions) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement " + UpdaterFragActions.class.getSimpleName());
        }

        try{
        	controlListener = (UpdaterFragControllerMethods) activity;
        } catch (ClassCastException e) {
        	throw new ClassCastException(activity.toString() + " must implement " + UpdaterFragControllerMethods.class.getSimpleName());
        }

    }
	
	protected void loadControls(Bundle savedInstanceState)
	{
		_btnDownload.setEnabled(controlListener.getMyController().getIsNetworkAvailable());

		if(savedInstanceState == null)
		{
			controlListener.ExecuteCheckForUpdatesTask();
		}
		else
		{
			loadControlsFromBundle(savedInstanceState);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		// 10/3/11 JHM - Don't store state (likely blank values) if the FetchLocalData task hasn't completed.
		outState.putBoolean(UPDATE_AVAILABLE, _btnDownload.isEnabled());
		outState.putString(MESSAGE, _lblMessage.getText().toString());
		
		super.onSaveInstanceState(outState);
		
	}
	
	public void loadControlsFromBundle(Bundle bundle)
	{
		CharSequence answer = bundle.getCharSequence(MESSAGE);
		_lblMessage.setText(answer);
		boolean isUpdateAvailable = bundle.getBoolean(UPDATE_AVAILABLE);
		_btnDownload.setEnabled(isUpdateAvailable);
		_btnDone.setEnabled(!isUpdateAvailable);
	}
	
	public TextView getMessageLabel(){
		if(_lblMessage == null)
			_lblMessage = (TextView)getView().findViewById(R.id.lblmessage);
		return _lblMessage;		
	}
	
	public Button getDownloadButton(){
		if(_btnDownload == null)
			_btnDownload = (Button)getView().findViewById(R.id.updater_btnDownload);
		return _btnDownload;		
	}
	
	public Button getDoneButton(){
		if(_btnDone == null)
			_btnDone = (Button)getView().findViewById(R.id.updater_btnDone);
		return _btnDone;		
	}
}
