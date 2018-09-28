package com.jjkeller.kmb;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.jjkeller.kmb.fragments.LeftNavFrag;
import com.jjkeller.kmb.fragments.RptTroubleApplicationFrag;
import com.jjkeller.kmb.interfaces.IRptTroubleApplication.RptTroubleApplicationFragControllerMethods;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmbapi.controller.AdminController;
import com.jjkeller.kmbui.R;

public class RptTroubleApplication extends BaseActivity implements RptTroubleApplicationFragControllerMethods, LeftNavFrag.OnNavItemSelectedListener, LeftNavFrag.ActivityMenuItemsListener
{
	RptTroubleApplicationFrag _contentFrag;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.baselayout);

		mFetchLocalDataTask = new FetchLocalDataTask(this.getClass().getSimpleName());
		mFetchLocalDataTask.execute();
	}

	@Override
	protected void InitController()
	{
		this.setController(new AdminController(this));
	}

	public AdminController getMyController()
	{
		return (AdminController)this.getController();
	}

	@Override
	protected void loadControls()
	{
		super.loadControls();
		loadContentFragment(new RptTroubleApplicationFrag());
		setFragments();
	}

	@Override
	public void setFragments()
	{
		super.setFragments();

		Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
		_contentFrag = (RptTroubleApplicationFrag)f;
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
			this.startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
