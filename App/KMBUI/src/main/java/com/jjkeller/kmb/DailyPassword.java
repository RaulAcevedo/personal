package com.jjkeller.kmb;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.actionbarsherlock.view.Menu;
import com.jjkeller.kmb.fragments.DailyPasswordFrag;
import com.jjkeller.kmb.interfaces.IDailyPassword.DailyPasswordFragActions;
import com.jjkeller.kmb.interfaces.IDailyPassword.DailyPasswordFragControllerMethods;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmbapi.controller.AdminController;
import com.jjkeller.kmbui.R;

public class DailyPassword extends BaseActivity 		
								implements DailyPasswordFragActions, DailyPasswordFragControllerMethods{
	DailyPasswordFrag _contentFrag;		
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.baselayout);
		
		new FetchLocalDataTask(this.getClass().getSimpleName()).execute();
	}
	
	@Override
	protected void loadControls() {
		super.loadControls();
		loadContentFragment(new DailyPasswordFrag());
	}
	
	@Override
	protected void InitController() {

		AdminController ctrlr = new AdminController(this);
		this.setController(ctrlr);	
	}
	
	@Override
	public void setFragments()
	{
		super.setFragments();

		Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
		_contentFrag = (DailyPasswordFrag)f;
	}
	
	public AdminController getMyController()
	{
		return (AdminController)this.getController();
	}
	
	public void handleOkButton()
	{
		String dailyPassword = _contentFrag.getDailyPasswordTextView().getText().toString();
		if(this.getMyController().IsDailyPasswordValid(dailyPassword)){
			// the daily password is correct, so continue on to the Admin screen
			this.startActivity(Admin.class);
			this.finish();
		}
		else{
			// daily password is not correct
			this.ShowMessage(this, "Admin password is incorrect");
		}		
	}	

	public void handleCancelButton()
	{
		// when cancelling, navigate back to the home screen
		this.startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);				
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		return true;
	}
	
}
