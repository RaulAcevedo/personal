package com.jjkeller.kmb;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.jjkeller.kmb.fragments.RptLogDetailFailuresTabFrag;
import com.jjkeller.kmb.fragments.RptLogDetailLogDetailTabFrag;
import com.jjkeller.kmb.fragments.RptLogDetailTeamDriverTabFrag;
import com.jjkeller.kmb.share.OffDutyBaseActivity;
import com.jjkeller.kmb.share.ViewOnlyModeNavHandler;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.interfaces.IAPIController;
import com.jjkeller.kmbapi.controller.share.MandateObjectFactory;
import com.jjkeller.kmbui.R;

public class RptLogDetail extends OffDutyBaseActivity
{
	private ViewOnlyModeNavHandler _viewOnlyHandler;
	
	private static final String currentTabStateKey = "currentTab";
	private static final String logDetailTabTag = "detail";
	private static final String teamDriversTabTag = "teamDrivers";
	private static final String logFailuresTabTag = "failures";

	private TabHost mTabHost;
	private TabManager mTabManager;

	private int _myIndex;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		_viewOnlyHandler = new ViewOnlyModeNavHandler(this);
		_viewOnlyHandler.setCurrentActivity(ViewOnlyModeNavHandler.ViewOnlyModeActivity.DETAIL);
		
		setContentView(R.layout.rptlogdetail);
		
		if(_viewOnlyHandler.getIsViewOnlyMode())
			_myIndex = _viewOnlyHandler.getCurrentActivity().index();
		else
			_myIndex = 2;
		
		// Used for handling highlighting the selected item in the leftnav
		// If not using multiple fragments within an activity, we have to manually set the selected item
		this.setLeftNavSelectedItem(_myIndex);
		this.setLeftNavAllowChange(true);

		loadControls(savedInstanceState);			
	}
	
	@Override
	public void onResume()
	{
		this.setLeftNavSelectedItem(_myIndex);
		loadLeftNavFragment();
		
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
	
	protected IAPIController getMyController()
	{
		return (IAPIController) this.getController();
	}
	
	@Override
	protected void loadControls(Bundle savedInstanceState)
	{
		super.loadControls(savedInstanceState);

		mTabHost = (TabHost)findViewById(android.R.id.tabhost);
		mTabHost.setup();

		mTabManager = new TabManager(this, mTabHost);

		mTabManager.addTab(mTabHost.newTabSpec(logDetailTabTag).setIndicator("Log Detail"));
		mTabManager.addTab(mTabHost.newTabSpec(teamDriversTabTag).setIndicator("Team Drivers"));
		mTabManager.addTab(mTabHost.newTabSpec(logFailuresTabTag).setIndicator("Failures"));

		if (savedInstanceState != null)
		{
			mTabHost.setCurrentTabByTag(savedInstanceState.getString(currentTabStateKey));			
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putString(currentTabStateKey, mTabHost.getCurrentTabTag());
	}

	@Override
	protected void InitController()
	{
        this.setController(MandateObjectFactory.getInstance(this, GlobalState.getInstance().getFeatureService()).getCurrentEventController());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		this.CreateOptionsMenu(menu, false);
        if (GlobalState.getInstance().isReviewEldEvent())
            return false;
		return true;
	}

	@Override
	public String getActivityMenuItemList()
	{
		return _viewOnlyHandler.getActivityMenuItemList(getString(R.string.rptdutystatus_actionitems_tablet));
	}
	
	
	private void handleMenuItemSelected(int itemPosition)
	{
		if(_viewOnlyHandler.getIsViewOnlyMode())
		{
			Intent intent = _viewOnlyHandler.handleMenuItemSelected(itemPosition);
			
			if(intent != null)
			{			
				intent.putExtra(getResources().getString(R.string.state_keepdate), true);
				
				this.finish();
                this.startActivity(intent);

			}
		}
		else
		{
			switch (itemPosition)
			{
				case 0:
					Bundle extras = new Bundle();				
					extras.putBoolean(getResources().getString(R.string.state_keepdate), true);
					this.startActivity(RptGridImage.class, extras);
                    finish();
					break;
				case 1:
					this.startActivity(RptDutyStatus.class);
                    finish();
					break;
				case 2:
					// DO NOTHING
					//ClearRecentlyStartedActivityUri();
					//this.startActivity(RptLogDetail.class);
					break;
				case 3:

					this.finish();
                    if (GlobalState.getInstance().isReviewEldEvent())
                        this.startActivity(EditLogRequest.class);
                    else
					    this.startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    GlobalState.getInstance().setIsReviewEldEvent(false);
					break;
			}
		}
	}

	@Override
	public void onNavItemSelected(int itemPosition)
	{
		handleMenuItemSelected(itemPosition);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// if the item is the home button
		if(item.getItemId() == android.R.id.home){			
			// finish activity and go to RODS
			this.finish();
			this.startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
		}
		
		handleMenuItemSelected(item.getItemId());
		super.onOptionsItemSelected(item);
		return true;
	}

	private class TabManager implements TabHost.OnTabChangeListener
	{
		private final Context mContext;
		private final TabHost mTabHost;

		private class DummyTabFactory implements TabHost.TabContentFactory
		{
			private final Context mContext;

			public DummyTabFactory(Context context)
			{
				mContext = context;
			}

			public View createTabContent(String tag)
			{
				View v = new View(mContext);
				v.setMinimumWidth(0);
				v.setMinimumHeight(0);
				return v;
			}
		}

		public TabManager(Context context, TabHost tabHost)
		{
			mContext = context;
			mTabHost = tabHost;
			mTabHost.setOnTabChangedListener(this);
		}

		public void addTab(TabSpec tabSpec)
		{
			mTabHost.addTab(tabSpec.setContent(new DummyTabFactory(mContext)));
		}

		public void onTabChanged(String tabId)
		{
			if (tabId == logDetailTabTag)
			{
				loadContentFragment(new RptLogDetailLogDetailTabFrag());
			}
			else if (tabId == teamDriversTabTag)
			{
				loadContentFragment(new RptLogDetailTeamDriverTabFrag());
			}
			else if (tabId == logFailuresTabTag)
			{
				loadContentFragment(new RptLogDetailFailuresTabFrag());
			}

			getSupportFragmentManager().executePendingTransactions();
		}
	}

}
