package com.jjkeller.kmb;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;

import com.actionbarsherlock.view.Menu;
import com.jjkeller.kmb.fragments.FeatureToggleFrag;
import com.jjkeller.kmb.interfaces.IFeatureToggle.FeatureToggleFragActions;
import com.jjkeller.kmb.interfaces.IFeatureToggle.FeatureToggleFragControllerMethods;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.FeatureToggleController;
import com.jjkeller.kmbui.R;


public class FeatureToggle extends BaseActivity 
								implements FeatureToggleFragActions, FeatureToggleFragControllerMethods{
		
	FeatureToggleFrag _contentFrag;
	private Intent nextActivity;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.featuretoggle);
				
		mFetchLocalDataTask = new FetchLocalDataTask(this.getClass().getSimpleName(), savedInstanceState);
		mFetchLocalDataTask.execute();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		//if there's an activity we're supposed to go to
		//after this one, disable the back button
		if(nextActivity != null)
		{
			if (keyCode == KeyEvent.KEYCODE_BACK)
			{
			    return true;
			}
		}
		
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	protected void loadControls(Bundle savedIntanceState)
	{
		super.loadControls();		
		loadContentFragment(new FeatureToggleFrag());
	}
	
	@Override
	public void setFragments()
	{
		super.setFragments();

		Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
		_contentFrag = (FeatureToggleFrag)f;
	}
	
	@Override
	protected void InitController() {

		FeatureToggleController ctrlr = new FeatureToggleController(this);
		this.setController(ctrlr);	
	}
	
	@Override
	protected void Return()
	{
		this.finish();
		
		if(nextActivity != null)
			startActivity(nextActivity);
	}
	
	protected FeatureToggleController getMyController()
	{
		return (FeatureToggleController)this.getController();
	}
	
	public void handleOkButtonClick()
	{
		this.ShowMessage(this,null, this.getString(R.string.msgsuccessfullycompleted),new ShowMessageClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				super.onClick(dialog, id);

				boolean isSelectiveFeatureTogglesEnabled = _contentFrag.getSelectiveFeatureTogglesEnabled();
				getMyController().SetSelectiveFeatureTogglesEnabled(isSelectiveFeatureTogglesEnabled);

				boolean isEldMandateEnabled = _contentFrag.getIsEldMandateEnabled();
				getMyController().SetEldMandateEnabled(isEldMandateEnabled);
				
				boolean isIgnoreServerTime = _contentFrag.getIgnoreServerTime();
				getMyController().SetIgnoreServerTime(isIgnoreServerTime);
				
				boolean isShowDebugFunctions = _contentFrag.getShowDebugFunctions();
				getMyController().SetShowDebugFunctions(isShowDebugFunctions);
				
				boolean useCloudServices = _contentFrag.getUseCloudServices();
				getMyController().SetUseCloudServices(useCloudServices);
				
				boolean defaultTripInformation = _contentFrag.getDefaultTripInformation(); 
				getMyController().SetDefaultTripInformation(defaultTripInformation);
				
				boolean alkCopilotEnabled = _contentFrag.getAlkCopilotEnabled();
				getMyController().SetAlkCopilotEnabled(alkCopilotEnabled);
				
				boolean personalConveyanceEnabled = _contentFrag.getPersonalConveyanceEnabled();
				getMyController().SetPersonalConveyanceEnabled(personalConveyanceEnabled);

				boolean ignoreFirmwareUpdateEnabled = _contentFrag.getIgnoreFirmwareUpdate();
				getMyController().SetIgnoreFirmwareUpdate(ignoreFirmwareUpdateEnabled);
				
				boolean forceComplianceTabletMode = _contentFrag.getForceComplianceTabletMode();
				getMyController().SetForceComplianceTabletMode(forceComplianceTabletMode);

				boolean setMobileStartTimestampToNull = _contentFrag.getSetMobileStartTimestampToNull();
				getMyController().SetSetMobileStartTimestampToNull(setMobileStartTimestampToNull);

				boolean hyrailEnabled = _contentFrag.getHyrailEnabled();
				getMyController().SetHyrailEnabled(hyrailEnabled);

				boolean nonRegDrivingEnabled = _contentFrag.getNonRegDrivingEnabled();
				getMyController().SetNonRegDrivingEnabled(nonRegDrivingEnabled);

				boolean geotabInjectDataStallsEnabled = _contentFrag.getGeotabInjectDataStallsEnabled();
				getMyController().SetGeotabInjectDataStallsEnabled(geotabInjectDataStallsEnabled);

				boolean isForceCrashesEnabled = _contentFrag.getIsForceCrashesEnabled();
				getMyController().SetForceCrashesEnabled(isForceCrashesEnabled);
				Return();
			}
		});
		
	}
	
	public void handleCancelButtonClick()
	{
		this.Return();
	}
	

		
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		if (GlobalState.getInstance().getPassedRods() == true)
			this.CreateOptionsMenu(menu, false);	
		return true;
	}

}
