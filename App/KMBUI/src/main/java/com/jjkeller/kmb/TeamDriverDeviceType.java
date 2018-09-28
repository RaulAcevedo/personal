package com.jjkeller.kmb;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.actionbarsherlock.view.Menu;
import com.jjkeller.kmb.fragments.TeamDriverDeviceTypeFrag;
import com.jjkeller.kmb.interfaces.ITeamDriverDeviceType.TeamDriverDeviceTypeFragActions;
import com.jjkeller.kmb.interfaces.ITeamDriverDeviceType.TeamDriverDeviceTypeFragControllerMethods;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.TeamDriverController;
import com.jjkeller.kmbui.R;

public class TeamDriverDeviceType extends BaseActivity 
							implements TeamDriverDeviceTypeFragActions, TeamDriverDeviceTypeFragControllerMethods{	
	TeamDriverDeviceTypeFrag _contentFrag;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.baselayout);

		mFetchLocalDataTask = new FetchLocalDataTask(this.getClass().getSimpleName());
		mFetchLocalDataTask.execute();
	}

	@Override
	protected void loadControls()
	{
		super.loadControls();
		loadContentFragment(new TeamDriverDeviceTypeFrag());
	}
	
	@Override
	public void setFragments()
	{
		super.setFragments();

		Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
		_contentFrag = (TeamDriverDeviceTypeFrag)f;
	}
	
	public TeamDriverController getMyController()
	{
		return (TeamDriverController)this.getController();
	}
	
	@Override
	protected void InitController() {
		TeamDriverController teamDriverCtrl = new TeamDriverController(this);
	
		this.setController(teamDriverCtrl);	
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		if (GlobalState.getInstance().getPassedRods() == true)
			this.CreateOptionsMenu(menu, false);	
		return true;
	}
	
	public void handleCancelButtonClick(){
		Return();
	}
	
	public void handleSharedDeviceButtonClick()
	{
		GlobalState.getInstance().setTeamDriverMode(GlobalState.TeamDriverModeEnum.SHAREDDEVICE);
		handleClick();
	}
	
	public void handleSeparateDeviceButtonClick()
	{
		GlobalState.getInstance().setTeamDriverMode(GlobalState.TeamDriverModeEnum.SEPARATEDEVICE);
		handleClick();
	}
	
	private void handleClick()
	{
		Intent intent;
		boolean isExemptLogEnabled = GlobalState.getInstance().getCurrentUser().getIsMobileExemptLogAllowed();
		boolean isExemptFromEldUse = GlobalState.getInstance().getCurrentUser().getExemptFromEldUse();
		boolean isELDMandateEnabled = GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled();
		boolean isFederalRuleset = GlobalState.getInstance().getCurrentUser().getRulesetTypeEnum().isUSFederalRuleset();

		// if Exempt is Enabled and the EmployeeLog ruleset is US60||US70, go to ExemptLogType screen, else continue to SelectDutyStatus screen
		if ((isExemptLogEnabled && isFederalRuleset) || (isExemptFromEldUse && isELDMandateEnabled))
			intent = new Intent(this, ExemptLogType.class);
		else
			intent = new Intent(this, SelectDutyStatus.class);
		
		intent.putExtra(getString(R.string.extra_isloginprocess), true);
		startActivity(intent);
		
		Return();
	}
	
	@Override
	protected void Return(boolean success) {
		finish();
	}
}
