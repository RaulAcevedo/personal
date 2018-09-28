package com.jjkeller.kmb;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.jjkeller.kmb.fragments.EditFuelPurchaseListFrag;
import com.jjkeller.kmb.fragments.LeftNavFrag;
import com.jjkeller.kmb.interfaces.IEditFuelPurchaseList.EditFuelPurchaseListFragActions;
import com.jjkeller.kmb.interfaces.IEditFuelPurchaseList.EditFuelPurchaseListFragControllerMethods;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmbapi.controller.FuelPurchaseController;
import com.jjkeller.kmbapi.proxydata.FuelPurchase;
import com.jjkeller.kmbui.R;

public class EditFuelPurchaseList extends BaseActivity
									implements EditFuelPurchaseListFragControllerMethods, EditFuelPurchaseListFragActions,
									LeftNavFrag.ActivityMenuItemsListener, LeftNavFrag.OnNavItemSelectedListener{
	
	EditFuelPurchaseListFrag _contentFrag;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.baselayout);
		
		mFetchLocalDataTask = new FetchLocalDataTask(this.getClass().getSimpleName());
		mFetchLocalDataTask.execute();
	}
	
	@Override
	protected void loadControls() {
		super.loadControls();
		loadContentFragment(new EditFuelPurchaseListFrag());
	}
	
	@Override
	public void setFragments()
	{
		super.setFragments();

		Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
		_contentFrag = (EditFuelPurchaseListFrag)f;
	}

	@Override
	protected void InitController() {
		this.setController(new FuelPurchaseController(this));	
	}

	public FuelPurchaseController getMyController()
	{
		return (FuelPurchaseController)this.getController();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		this.CreateOptionsMenu(menu, false);
		return true;
	}

	public String getActivityMenuItemList()
	{
		return getString(R.string.editfuelpurchaselist_actionitems);
	}

	private void handleMenuItemSelected(int itemPosition)
	{
		switch (itemPosition)
		{
			case 0:
				this.getMyController().setWorkingFuelPurchase(null); // Start with an empty fuel purchase
				this.startActivity(FuelPurchaseEdit.class);
				break;
			case 1:
				this.finish();
				this.startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
				break;
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
	
	public void handleEditButtonClick(FuelPurchase fuelPurchase) {
		getMyController().setWorkingFuelPurchase(fuelPurchase);
    	startActivity(FuelPurchaseEdit.class);
	}
}
