package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.jjkeller.kmb.interfaces.IUploadDiagnostics.UploadDiagnosticsFragActions;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.GeotabController;
import com.jjkeller.kmbui.R;

public class UploadDiagnosticsFrag extends BaseFragment
{
	private UploadDiagnosticsFragActions _actionsListener;

	private Button _btnUpload;
	private Button _btnUploadOldDiagnostics;
	private Button _btnClearEobrDiagnostics;
	private Button _btnClearKmbDiagnostics;	
	private Button _btnMoveFilesToSDCard;
	private Button _btnConsole;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.f_uploaddiagnostics, container, false);
		findControls(v);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		this.loadControls();
	}

	private void loadControls() {
		//Hide clear EOBR for all users
		_btnClearEobrDiagnostics.setVisibility(View.GONE);

		//Hide Console Log for GeoTab Users
		GeotabController geotabController = new GeotabController(this.getActivity());
		if(GlobalState.getInstance().getCompanyConfigSettings(this.getActivity()).getIsGeotabEnabled() && geotabController.IsCurrentDeviceGeotab()) {
			_btnConsole.setVisibility(View.GONE);

		}
	}
	
	protected void findControls(View v)
	{
		_btnUpload = (Button)v.findViewById(R.id.btnUpload);
		_btnUpload.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				_actionsListener.handleUploadButtonClick();
			}
		});

		_btnUploadOldDiagnostics = (Button)v.findViewById(R.id.btnUploadOldDiagnostics);
		_btnUploadOldDiagnostics.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				_actionsListener.handleUploadBackupLogsButtonClick();
			}
		});

		_btnClearEobrDiagnostics = (Button)v.findViewById(R.id.btnClearEobrDiagnostics);
		_btnClearEobrDiagnostics.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				_actionsListener.handleClearEobrButtonClick();
			}
		});

		_btnClearKmbDiagnostics = (Button)v.findViewById(R.id.btnClearKmbDiagnostics);
		_btnClearKmbDiagnostics.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				_actionsListener.handleClearKmbButtonClick();
			}
		});
		
		_btnMoveFilesToSDCard = (Button)v.findViewById(R.id.btnMoveFilesToSDCard);
		_btnMoveFilesToSDCard.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				_actionsListener.handleMoveToSDCardButtonClick();
			}
		});
		
		_btnConsole = (Button)v.findViewById(R.id.btnConsoleDump);
		_btnConsole.setOnClickListener(
			new OnClickListener(){
				public void onClick(View v){
					_actionsListener.handleConsole();
				}
			});
		if (!GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
			_btnClearEobrDiagnostics.setText(getString(R.string.cleareobrdiagnosticslabel));
		}
	}

	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		try
		{
			_actionsListener = (UploadDiagnosticsFragActions)activity;
		}
		catch (ClassCastException e)
		{
			throw new ClassCastException(activity.toString() + " must implement UploadDiagnosticsFragActions");
		}
	}
}
