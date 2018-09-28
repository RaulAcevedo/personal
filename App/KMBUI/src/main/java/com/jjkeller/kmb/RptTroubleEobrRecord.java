package com.jjkeller.kmb;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.jjkeller.kmb.fragments.LeftNavFrag.ActivityMenuItemsListener;
import com.jjkeller.kmb.fragments.LeftNavFrag.OnNavItemSelectedListener;
import com.jjkeller.kmb.fragments.RptTroubleEobrRecordFrag;
import com.jjkeller.kmb.interfaces.IRptTroubleEobrRecord.RptTroubleEobrRecordControllerMethods;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.AdminController;
import com.jjkeller.kmbui.R;

public class RptTroubleEobrRecord extends BaseActivity implements RptTroubleEobrRecordControllerMethods, OnNavItemSelectedListener, ActivityMenuItemsListener
{
	RptTroubleEobrRecordFrag _contentFrag;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.baselayout);

		mFetchLocalDataTask = new FetchLocalDataTask(this.getClass().getSimpleName());
		mFetchLocalDataTask.execute();
	}

	public AdminController getMyController()
	{
		return (AdminController)this.getController();
	}

	@Override
	protected void InitController()
	{
		this.setController(new AdminController(this));
	}

	@Override
	protected void loadControls()
	{
		super.loadControls();

		if (this.getMyController().getIsEOBRDeviceOnlineOrReadingHistory())
		{
			loadContentFragment(new RptTroubleEobrRecordFrag());
		}
		else
		{
			// No EOBR Available
			if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
				Toast.makeText(this, this.getString(R.string.no_eld_available), Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(this, this.getString(R.string.no_eobr_available), Toast.LENGTH_SHORT).show();
			}
			Return(); // Return to previous control.
		}
	}

	@Override
	public void setFragments()
	{
		super.setFragments();

		Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
		_contentFrag = (RptTroubleEobrRecordFrag)f;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		this.CreateOptionsMenu(menu, false);
		return true;
	}

	public String getActivityMenuItemList()
	{
		return getString(R.string.btndone);
	}

	private void handleNavItem(int itemPosition)
	{
		if (itemPosition == 0)
			Return();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		//See if home button was pressed
		this.GoHome(item, this.getController());
		
		handleNavItem(item.getItemId());
		super.onOptionsItemSelected(item);
		return true;
	}

	public void onNavItemSelected(int itemPosition)
	{
		handleNavItem(itemPosition);
	}

	@Override
	protected void Return(boolean success)
	{
		this.startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
	}
}
