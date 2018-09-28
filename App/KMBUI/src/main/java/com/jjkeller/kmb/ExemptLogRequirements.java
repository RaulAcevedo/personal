package com.jjkeller.kmb;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.actionbarsherlock.view.Menu;
import com.jjkeller.kmb.fragments.ExemptLogRequirementsFrag;
import com.jjkeller.kmb.fragments.LeftNavFrag;
import com.jjkeller.kmb.fragments.LeftNavImgFrag;
import com.jjkeller.kmb.interfaces.IExemptLogRequirements.ExemptLogRequirementsFragActions;
import com.jjkeller.kmb.interfaces.IExemptLogRequirements.ExemptLogRequirementsFragControllerMethods;
import com.jjkeller.kmb.interfaces.ILogDownloaderHost;
import com.jjkeller.kmb.interfaces.IOdometerCalibrationRequiredHost;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.LogEntryController;
import com.jjkeller.kmbapi.controller.LoginController;
import com.jjkeller.kmbapi.controller.share.MandateObjectFactory;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.enums.DutyStatusEnum;
import com.jjkeller.kmbapi.enums.ExemptLogTypeEnum;
import com.jjkeller.kmbui.R;


public class ExemptLogRequirements extends BaseActivity 
							implements ExemptLogRequirementsFragActions, ExemptLogRequirementsFragControllerMethods,
							ILogDownloaderHost, IOdometerCalibrationRequiredHost,
							LeftNavFrag.OnNavItemSelectedListener, LeftNavFrag.ActivityMenuItemsListener,
							LeftNavImgFrag.ActivityMenuIconItems{	
	ExemptLogRequirementsFrag _contentFrag;
	
	private Intent nextActivity;
	private boolean _isTeamDriver;	
	private boolean _isSharedDevice = GlobalState.getInstance().getTeamDriverMode() == GlobalState.TeamDriverModeEnum.SHAREDDEVICE;
	protected boolean _loginProcess = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.baselayout);

		mFetchLocalDataTask = new FetchLocalDataTask(this.getClass().getSimpleName());
		mFetchLocalDataTask.execute();
		
		if(GlobalState.getInstance().getTeamDriverMode() == GlobalState.TeamDriverModeEnum.NONE)
			_isTeamDriver = false;
		else
			_isTeamDriver = true;
		
		_loginProcess = getIntent().hasExtra(this.getResources().getString(R.string.extra_isloginprocess));
	}

	@Override
	protected void loadControls()
	{
		super.loadControls();
		loadContentFragment(new ExemptLogRequirementsFrag());
		
		if(_isTeamDriver) {
			loadLeftNavFragment();
		}
	}
	
	@Override
	public void setFragments()
	{
		super.setFragments();

		Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
		_contentFrag = (ExemptLogRequirementsFrag)f;
	}
	
	public LoginController getMyController()
	{
		return (LoginController)this.getController();
	}
	
	@Override
	protected void InitController() {
		LoginController loginCtrl = new LoginController(this);
	
		this.setController(loginCtrl);	
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		if (GlobalState.getInstance().getPassedRods() == true)
			this.CreateOptionsMenu(menu, false);	
		return true;
	}
	
	@Override
    protected void Return()
	{
		if(nextActivity != null) {
			startActivity(nextActivity);
		}
			
		this.finish();
	}
	
	@Override
	protected void loadLeftNavFragment()
	{
		if (!isFinishing())
		{
			View leftNavLayout = findViewById(R.id.leftnav_fragment);
			if (leftNavLayout != null)
			{
				// Create new fragment and transaction
				FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

				setLeftNavFragment(new LeftNavImgFrag(R.layout.leftnav_item_imageandtext));
				
				// Replace fragment
				transaction.replace(R.id.leftnav_fragment, getLeftNavFragment());

				// Commit the transaction
				transaction.commit();
				
				setLeftNavSelectionItems();
				leftNavLayout.setBackgroundColor(getResources().getColor(R.color.menugray));
			}
		}
	}	
	
	@Override
	public String getActivityMenuItemList()
	{
		if(_isTeamDriver)
		{
			if(_isSharedDevice)
				return getString(R.string.tripinfo_actionitems_shareddevice);				
			else
				return getString(R.string.tripinfo_actionitems_separatedevice);
		}
		else 
			return null;
	}
	

	public String getActivityMenuIconList()
	{
		if(_isTeamDriver)
		{
			if(_isSharedDevice)
			{
				return getString(R.string.tripinfo_actionitemsicons_shareddevice);				
			}
			else
			{
				return getString(R.string.tripinfo_actionitemsicons_separatedevice);
			}
		}
		else 
			return null;
	}
	
	public void handleYesButtonClick() {
        User currentUser = GlobalState.getInstance().getCurrentUser();

                            
        // if solo driver, DutyStatus then download logs & Odometer Calibration
        // if team driver, go to DutyStatus screen
        if(_isTeamDriver || GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
        	nextActivity = new Intent(this, SelectDutyStatus.class);
        	nextActivity.putExtra(this.getResources().getString(R.string.extra_exemptlogtype), currentUser.getExemptLogType().getValue());
        	Return();
        } else {
        	//create the exempt log
        	new LogEntryController(this).CreateCurrentLog(new DutyStatusEnum(DutyStatusEnum.ONDUTY), currentUser.getExemptLogType());
			nextActivity = new Intent(this, SelectDutyStatus.class);
			nextActivity.putExtra(this.getResources().getString(R.string.extra_exemptlogtype), currentUser.getExemptLogType().getValue());

        	if(!_isTeamDriver) {
        		EmployeeLogDownloader downloader = new EmployeeLogDownloader(this);
        		downloader.DownloadLogs();
        	} else {
				Return();
			}
        }
	}

	
	
	public void handleNoButtonClick() {
		// go to TripInfo screen when solo driver
		// go to DutyStatus screen when team drivers
		if(_isTeamDriver || GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled())
		{
			nextActivity = new Intent(this, SelectDutyStatus.class);
			nextActivity.putExtra(this.getResources().getString(R.string.extra_exemptlogtype), ExemptLogTypeEnum.NULL);
		} else {
			nextActivity = new Intent(this, SelectDutyStatus.class);
		}
		
		nextActivity.putExtra(this.getResources().getString(R.string.extra_tripinfomsg), this.getString(R.string.extra_tripinfomsg));
		
		if(_loginProcess)
			nextActivity.putExtra(getString(R.string.extra_isloginprocess), true);
		
		// If the user answers no to meeting requirements, then show this message
		this.ShowMessage(this, 0, getString(R.string.lblexemptexception_notmeetingrequirements), onOkSubmitMessage);
	}
	
	ShowMessageClickListener onOkSubmitMessage = new ShowMessageClickListener() {
	      @Override
	      public void onClick(DialogInterface dialog, int id) {
	 
	        // Call super.onClick to release screen orientation lock
	        super.onClick(dialog, id);
	        
	        Return();
	      }
	   };
	   
	   // Download logs confirmation message
	   public void ShowConfirmationMessage(String message,
				final Runnable yesAction,
				final Runnable noAction) {
			
			ShowConfirmationMessage(this, message,
				new ShowMessageClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						super.onClick(dialog, id);
						
						if(yesAction != null)
							yesAction.run();
					}
				},
				
				new ShowMessageClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						super.onClick(dialog, id);
						
						if(noAction != null)
							noAction.run();
					}
				}
			);
		}

		public BaseActivity getActivity() {
			return this;
		}

		public void onLogDownloadFinished(boolean logsFound) {
			finalOdometerForSoloExemptDriver();
		}

		public void onOffDutyLogsCreated(boolean logsCreated) {
			finalOdometerForSoloExemptDriver();
		}
		
		protected void createExtras()
		{
	        // if not team driver login, don't download logs and prompt for off duty logs on rods entry,
	        // otherwise if team driver login, then download logs and prompt for off duty logs
	        if (!getIntent().hasExtra(this.getString(R.string.extra_teamdriverlogin)))
	        	nextActivity.putExtra(this.getString(R.string.extra_displayoffdutylogs), false);
	        else
	        	nextActivity.putExtra(this.getString(R.string.extra_displayoffdutylogs), true);
		}
		
		public void OnOdometerCalibrationRequired(boolean calibrationRequired) {
	        if(calibrationRequired){
	        	/* Display OdometerCalibration activity */ 
		        Bundle extras = new Bundle();
		        extras.putBoolean(getString(R.string.extra_displayoffdutylogs), true);
	        	extras.putBoolean(getString(R.string.extra_isloginprocess), true);
	        	
		        startActivity(OdometerCalibration.class, Intent.FLAG_ACTIVITY_CLEAR_TOP, extras);
	        }
	       	else{
				Bundle extras = new Bundle();
				extras.putInt(this.getResources().getString(R.string.extra_exemptlogtype), GlobalState.getInstance().getCurrentUser().getExemptLogType().getValue());
				startActivity(SelectDutyStatus.class, Intent.FLAG_ACTIVITY_SINGLE_TOP, extras);
			}
	        finish();
		}
		
		public void finalOdometerForSoloExemptDriver() {
			OdometerCalibrationRequiredTask calibrationTask = new OdometerCalibrationRequiredTask(this, MandateObjectFactory.getInstance(this,GlobalState.getInstance().getFeatureService()).getCurrentEventController());
        	calibrationTask.execute();
		}
}
