package com.jjkeller.kmb;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;

import com.actionbarsherlock.view.MenuItem;
import com.jjkeller.kmb.fragments.LeftNavFrag;
import com.jjkeller.kmb.fragments.RptAvailHoursFrag;
import com.jjkeller.kmb.share.OffDutyBaseActivity;
import com.jjkeller.kmb.share.ViewOnlyModeNavHandler;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.HosAuditController;
import com.jjkeller.kmbui.R;

public class ViewHours extends OffDutyBaseActivity implements LeftNavFrag.OnNavItemSelectedListener, LeftNavFrag.ActivityMenuItemsListener  {

	private ViewOnlyModeNavHandler _viewOnlyHandler;
	private RptAvailHoursFrag _availHoursFrag;

	protected static final long MILLISECONDS_PER_MINUTE = 60000;

	private int _myIndex;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		_viewOnlyHandler = new ViewOnlyModeNavHandler(this);
		_viewOnlyHandler.setCurrentActivity(ViewOnlyModeNavHandler.ViewOnlyModeActivity.VIEWHOURS);
		
		_myIndex = _viewOnlyHandler.getCurrentActivity().index();
		
		// Used for handling highlighting the selected item in the leftnav
		// If not using multiple fragments within an activity, we have to manually set the selected item
		this.setLeftNavSelectedItem(_myIndex);
		this.setLeftNavAllowChange(true);
		
		setContentView(R.layout.viewhours);
		loadContentFragment(new RptAvailHoursFrag());

		loadControls(savedInstanceState);			
	}
	
	@Override
	protected void onResume()
	{	
		this.setLeftNavSelectedItem(_myIndex);
		this.loadLeftNavFragment();
		
		super.onResume();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if(GlobalState.getInstance().getIsViewOnlyMode())
		{
			//disable the back button in view only mode
			if (keyCode == KeyEvent.KEYCODE_BACK)
			{
			    return true;
			}
		}
		
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	protected void loadControls(Bundle savedInstanceState)
	{
		super.loadControls(savedInstanceState);
	}
	
	@Override
	public void setFragments()
	{
		super.setFragments();
		Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
		
		_availHoursFrag = (RptAvailHoursFrag)f;			
		this.setController(new HosAuditController(this));	
		
		loadData();		
	}

	@Override
	protected void InitController()
	{
		this.setController(new HosAuditController(this));
	}

	@Override
	protected void loadData()
	{
		super.loadData();
        _availHoursFrag.init(getMyController());
	}
	
	protected HosAuditController getMyController()
	{
		return (HosAuditController)this.getController();
	}

	public String getActivityMenuItemList()
	{
		return _viewOnlyHandler.getActivityMenuItemList(null);
	}

	public void onNavItemSelected(int itemPosition)
	{
		if(_viewOnlyHandler.getIsViewOnlyMode())
		{
			Intent intent = _viewOnlyHandler.handleMenuItemSelected(itemPosition);
			
			if(intent != null)
			{
				this.finish();
                this.startActivity(intent);

            }
		}
		
		getSupportFragmentManager().executePendingTransactions();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		onNavItemSelected(item.getItemId());
		super.onOptionsItemSelected(item);
		return true;
	}
}
