package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jjkeller.kmb.Eula;
import com.jjkeller.kmb.crashlytics.Utils;
import com.jjkeller.kmb.interfaces.ILogin.LoginControllerMethods;
import com.jjkeller.kmb.interfaces.ILogin.LoginFragActions;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbui.R;

import java.util.Calendar;

public class LoginFrag extends BaseFragment {

	LoginFragActions actionsListener;
	LoginControllerMethods controllerListener;	
	
	private TextView _tvLoginTitle;
	private TextView _tvUsername;
	private TextView _tvPassword;
	private TextView _btnEula;
	private TextView _tvTeamDriverStart;
	private Button _btnTeamDriverStart;
	private Button _btnCancel;
	private Button _btnFeatureToggle;
	private Button _btnLoginTeam;
	private Button _btnLoginSolo;
	private Button _btnLoginViewOnly;
	private SharedPreferences _userPref;
	private TextView _lblAppVersion;
    private int countForGenerateError;

	private boolean _multipleDriverWorkflow = GlobalState.getInstance().getCompanyConfigSettings(this.getActivity()).getMultipleUsersAllowed();

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
        countForGenerateError = 0;
        // Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.f_login, container, false);
		findControls(v);
		return v;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		this.loadControls();
	}
	
	@Override
	public void onResume() {
		super.onResume();

		_multipleDriverWorkflow = GlobalState.getInstance().getCompanyConfigSettings(this.getActivity()).getMultipleUsersAllowed();


		if(_multipleDriverWorkflow )
			_btnLoginTeam.setVisibility(View.GONE);
		else
			_btnLoginTeam.setVisibility(View.VISIBLE);
		if(controllerListener.getCurrentUser() == null || _multipleDriverWorkflow)
		{
			_tvTeamDriverStart.setVisibility(View.GONE);
			_btnTeamDriverStart.setVisibility(View.GONE);
		}
		else {
			_tvTeamDriverStart.setVisibility(View.VISIBLE);
			_btnTeamDriverStart.setVisibility(View.VISIBLE);
			
			Calendar cal = Calendar.getInstance();
			cal.setTime(controllerListener.getCurrentClockHomeTerminalTime());
			
			_btnTeamDriverStart.setEnabled(false);
			updateTimeDisplay(_btnTeamDriverStart, cal);			
		}
	}
	
	protected void findControls(View v)
	{
		_tvLoginTitle = (TextView)v.findViewById(R.id.lbllogin_title);
		_tvUsername = (TextView)v.findViewById(R.id.txtusername);
		_tvPassword = (TextView)v.findViewById(R.id.txtpassword);

		_btnEula = (TextView)v.findViewById(R.id.btneula);
		_btnEula.setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View view) {
						Intent i = new Intent(getActivity(), Eula.class);
						i.putExtra("startedFromLogin", true);
						startActivity(i);
					}
				}
		);

		_btnLoginSolo = (Button)v.findViewById(R.id.btnloginSolo);
		_btnLoginSolo.setOnClickListener(
	            new OnClickListener() {
	            	public void onClick(View v) {
						actionsListener.handleLoginSoloButtonClick();
	            	}
	            });

		_btnLoginTeam = (Button)v.findViewById(R.id.btnloginTeam);
		_btnLoginTeam.setOnClickListener(
	            new OnClickListener() {
	            	public void onClick(View v) {
	            		actionsListener.handleLoginTeamButtonClick();
	            	}
	            });

		_btnLoginViewOnly = (Button)v.findViewById(R.id.btnloginViewOnly);
		_btnLoginViewOnly.setOnClickListener(
				new OnClickListener() {
					public void onClick(View v) {
						actionsListener.handleLoginViewOnlyButtonClick();
					}
				});
		
		_btnCancel = (Button)v.findViewById(R.id.btnlogincancel);
		_btnCancel.setOnClickListener(
	            new OnClickListener() {
	            	public void onClick(View v) {
	            		actionsListener.handleCancelButtonClick();
	            	}
	            });		

		_tvTeamDriverStart = (TextView)v.findViewById(R.id.tvTeamDriverStart);
		_btnTeamDriverStart = (Button)v.findViewById(R.id.btnTeamDriverStart);
		_btnTeamDriverStart.setOnClickListener(
	            new OnClickListener() {
	            	public void onClick(View v) {
	            		ShowTimePickerDialog(_btnTeamDriverStart);
	            	}
	            });	
		
		_btnFeatureToggle = (Button)v.findViewById(R.id.btnFeatureToggle);
		_btnFeatureToggle.setOnClickListener(
	            new OnClickListener() {
	            	public void onClick(View v) {
	            		actionsListener.handleFeatureToggleButtonClick();
	            	}
	            });
		_lblAppVersion = (TextView)v.findViewById(R.id.lblAppVersion);
        _lblAppVersion.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
				//If feature toggle "Force Crashes" is enabled, the app will crash after the fifth touch
				if(GlobalState.getInstance().getFeatureService().getIsForceCrashesEnabled()){
					countForGenerateError++;
					if (countForGenerateError >= 5){ //Generate crash after 5 touches
						Utils.GenerateError();
					}
				}
            }
        });
	}
	
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
        	actionsListener = (LoginFragActions) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement LoginFragActions");
        }
        try {
        	controllerListener = (LoginControllerMethods) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement LoginControllerMethods");
        }
    }
    
	private void loadControls()
	{
		_lblAppVersion.setText(GlobalState.getInstance().getPackageVersionName());
		_userPref = getActivity().getSharedPreferences(this.getString(R.string.sharedpreferencefile), 0);
		String userName = _userPref.getString(this.getString(R.string.username), "");
		if (!userName.equals("") && controllerListener.getCurrentUser() == null)
		{
			_tvUsername.setText(userName);
			_tvPassword.requestFocus();
		}
				
		if( getActivity().getIntent().hasExtra(this.getResources().getString(R.string.msg_elddevicenotconnected)) )
		{
			ShowMessage(getString(R.string.msg_elddevicenotconnected));
		}

		if( getActivity().getIntent().hasExtra(this.getResources().getString(R.string.msg_eobrdevicenotconnected)) )
		{
			ShowMessage(getString(R.string.msg_eobrdevicenotconnected));
		}
		
		if(!GlobalState.getInstance().getAppSettings(getActivity()).getAppSettingsLoaded())
		{
			ShowMessage(getString(R.string.msg_appsettings_not_loaded));
		}
		
		// hide the Feature Toggle button when not set to ShowDebugFunctions
		if(GlobalState.getInstance().getAppSettings(getActivity()).getShowFeatureToggles())
			_btnFeatureToggle.setVisibility(View.VISIBLE);
		else
			_btnFeatureToggle.setVisibility(View.GONE);
		
		if (GlobalState.getInstance().getIsTeamLogin()) {
			this.teamDriverTwoLoginScreen();
		}

		if(GlobalState.getInstance().getCompanyConfigSettings(this.getActivity()).getMultipleUsersAllowed()
				&& GlobalState.getInstance().getLoggedInUserList().size() > 0){
			this.multipleUserLoginScreen();
		}
		
		controllerListener.handleConfigurationChangedState();
	}

	public TextView GetUsernameTextbox()
    {
    	if (_tvUsername == null)
    	{
    		_tvUsername = (TextView)getView().findViewById(R.id.txtusername);
    	}
    	return _tvUsername;
    }

	public TextView GetPasswordTextbox()
    {
    	if (_tvPassword == null)
    	{
    		_tvPassword = (TextView)getView().findViewById(R.id.txtpassword);
    	}
    	return _tvPassword;
    }

	public TextView GetTeamDriverStartLabel()
    {
    	if (_tvTeamDriverStart == null)
    	{
    		_tvTeamDriverStart = (TextView)getView().findViewById(R.id.tvTeamDriverStart);
    	}
    	return _tvTeamDriverStart;
    }

	public Button GetTeamDriverStartButton()
	{
    	if (_btnTeamDriverStart == null)
    	{
    		_btnTeamDriverStart = (Button)getView().findViewById(R.id.btnTeamDriverStart);
    	}
    	return _btnTeamDriverStart;		
	}

	private void teamDriverTwoLoginScreen() {
		_tvLoginTitle.setText(R.string.lblloginteamdrivertwo_title);

		_btnTeamDriverStart.setVisibility(View.GONE);
		_btnFeatureToggle.setVisibility(View.GONE);

		_btnLoginSolo.setText(R.string.btnloginteamdrivertwo);
		RelativeLayout.LayoutParams parms = (RelativeLayout.LayoutParams) _btnCancel
				.getLayoutParams();
		parms.addRule(RelativeLayout.BELOW, R.id.btnloginSolo);
		parms.addRule(RelativeLayout.ALIGN_LEFT, R.id.txtpassword);
		_btnCancel.setLayoutParams(parms);
	}

	private void multipleUserLoginScreen() {
		_tvLoginTitle.setText(R.string.lbllogin_title);

		_btnTeamDriverStart.setVisibility(View.GONE);
		_btnFeatureToggle.setVisibility(View.GONE);

		_btnLoginSolo.setText(R.string.lblmultipleusers_title);
		RelativeLayout.LayoutParams parms = (RelativeLayout.LayoutParams) _btnCancel
				.getLayoutParams();
		parms.addRule(RelativeLayout.BELOW, R.id.btnloginSolo);
		parms.addRule(RelativeLayout.ALIGN_LEFT, R.id.txtpassword);
		_btnCancel.setLayoutParams(parms);
	}
}
