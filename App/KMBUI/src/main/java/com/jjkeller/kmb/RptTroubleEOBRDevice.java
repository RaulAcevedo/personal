package com.jjkeller.kmb;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.jjkeller.kmb.fragments.LeftNavFrag;
import com.jjkeller.kmb.fragments.RptTroubleEOBRDeviceFrag;
import com.jjkeller.kmb.interfaces.IRptTroubleEOBRDevice.RptTroubleEOBRDeviceControllerMethods;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.AdminController;
import com.jjkeller.kmbui.R;

public class RptTroubleEOBRDevice extends BaseActivity implements RptTroubleEOBRDeviceControllerMethods, LeftNavFrag.OnNavItemSelectedListener, LeftNavFrag.ActivityMenuItemsListener
{
	RptTroubleEOBRDeviceFrag _contentFrag;
	
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
			loadContentFragment(new RptTroubleEOBRDeviceFrag());
		}
		else
		{
			if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
				this.showMsg(this.getString(R.string.no_eld_available));
			} else {
				this.showMsg(this.getString(R.string.no_eobr_available));
			}
			this.Return();
		}
	}

	@Override
	public void setFragments()
	{
		super.setFragments();

		Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
		_contentFrag = (RptTroubleEOBRDeviceFrag)f;
	}

	@Override
	public void Return()
	{
		/* Display rodsentry activity */
		this.startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		this.CreateOptionsMenu(menu, false);
		return true;
	}

	public String getActivityMenuItemList()
	{
		return this.getString(R.string.btndone);
	}
	
	private void handleMenuItemSelected(int itemPosition)
	{
		if (itemPosition == 0)
			this.Return();
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

	public void onNavItemSelected(int itemPosition)
	{
		handleMenuItemSelected(itemPosition);
	}
}
