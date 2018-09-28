package com.jjkeller.kmb;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.jjkeller.kmb.fragments.EditLogLocationsFrag;
import com.jjkeller.kmb.fragments.LeftNavFrag;
import com.jjkeller.kmb.interfaces.IEditLogLocations.EditLogLocationsFragActions;
import com.jjkeller.kmb.interfaces.IEditLogLocations.EditLogLocationsFragControllerMethods;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmbapi.controller.LogEntryController;
import com.jjkeller.kmbui.R;

public class EditLogLocations extends BaseActivity
								implements EditLogLocationsFragActions, EditLogLocationsFragControllerMethods,
								LeftNavFrag.OnNavItemSelectedListener, LeftNavFrag.ActivityMenuItemsListener{
	EditLogLocationsFrag _contentFrag;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.editloglocations);
		
		new FetchLocalDataTask(this.getClass().getSimpleName()).execute();
	}
	
	@Override
	protected void InitController() {
		LogEntryController logEntryCtrl = new LogEntryController(this);		
		this.setController(logEntryCtrl);	
	}

	@Override
	protected void loadControls() {
		super.loadControls();		
		loadContentFragment(new EditLogLocationsFrag());
	}
	
	@Override
	public void setFragments()
	{
		super.setFragments();

		Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
		_contentFrag = (EditLogLocationsFrag)f;
	}
	
	public LogEntryController getMyController()
	{
		return (LogEntryController)this.getController();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		this.CreateOptionsMenu(menu, false);
		return true;
	}

	public String getActivityMenuItemList()
	{
		return getString(R.string.btndone);
	}

	private void handleMenuItemSelected(int itemPosition)
	{
		if (itemPosition == 0)
		{
			this.finish();
			this.startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		//See if home button was pressed
		this.GoHome(item, this.getController());
		
		handleMenuItemSelected(item.getItemId());
		super.onOptionsItemSelected(item);
		return true;
	}

	public void onNavItemSelected(int item)
	{
		handleMenuItemSelected(item);
	}
	
	public void handleEditLogLocationsClick(Context ctx){
    	Bundle extras = new Bundle();
    	extras.putString(getString(R.string.calledfrom), ctx.getClass().getName());
    	startActivity(RodsEditLocation.class, extras);
	}
}
