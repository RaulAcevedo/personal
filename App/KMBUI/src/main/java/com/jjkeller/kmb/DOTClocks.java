package com.jjkeller.kmb;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.jjkeller.kmb.fragments.DOTClocksFrag;
import com.jjkeller.kmb.fragments.LeftNavFrag;
import com.jjkeller.kmb.fragments.RecapHoursFrag;
import com.jjkeller.kmb.interfaces.IRecapHours;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmb.share.ClockData;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.EOBR.EobrReader;
import com.jjkeller.kmbapi.controller.HosAuditController;
import com.jjkeller.kmbapi.controller.interfaces.IAPIController;
import com.jjkeller.kmbapi.controller.share.MandateObjectFactory;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.proxydata.DutySummary;
import com.jjkeller.kmbui.R;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class DOTClocks extends BaseActivity implements
		LeftNavFrag.ActivityMenuItemsListener, LeftNavFrag.OnNavItemSelectedListener,
		IRecapHours.RecapHoursFragControllerMethods
{
	private DOTClocksFrag _contentFrag;

	protected static final long CLOCK_TICK_MILLISECONDS = 1000;

	private ClockData _driveTimeData;
	private ClockData _driveTimeData_ResetBreak;
	private ClockData _dailyOnDutyData;
	private ClockData _weeklyOnDutyData;
	private Timer _clockUpdateTimer;

	private IAPIController _controllerEmp = null;
	private DutySummary driveTimeSummary_ResetBreak;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dotclocks);
		_controllerEmp = MandateObjectFactory.getInstance(this.getApplicationContext(),GlobalState.getInstance().getFeatureService()).getCurrentEventController();
		// Used for handling highlighting the selected item in the leftnav
		// We have to allow the leftnav to highlight the selected item
		boolean nightMode = getDOTClocks_NightMode();
		if(nightMode){
			this.setLeftNavSelectedItem(0);
			this.setLeftNavAllowChange(true);
		}

	}

	@Override
	protected void onResume()
	{
		super.onResume();
		
		//if there's a pending clock change, don't update
		//the clocks until we've had a chance to adjust
		//our timekeeper (which happens on a timer pop)
		if(EobrReader.getInstance().IsClockUpdatePending())
		{
			final Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				public void run() {
					refreshData();
				}
			}, 20000);
		}
		else
			refreshData();
	}

	private void refreshData()
	{
		// Somewhere around Android version 3.0, the behavior of the AsyncTask was changed from being a parallel
		// operation to a serialized operation.  Therefore, in order for newer versions of Android to operate as 
		// KMB expects (in a parallel fashion), we need to add the following code.
		mFetchLocalDataTask = new FetchLocalDataTask(this.getClass().getSimpleName());
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) 
		{
			mFetchLocalDataTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
		else 
		{
			mFetchLocalDataTask.execute();
		}
	}
	
	@Override
	protected void onPause()
	{
		super.onPause();
		stopTimer();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// The phone and tablet items are the same, so get the tablet items
		String menuOptions = getActivityMenuItemList();
		if (menuOptions != null && menuOptions.length() > 0)
		{
			this.CreateOptionsMenu(menu, false);
			return true;
		}
		return false;
	}

	public String getActivityMenuItemList()
	{
		String menu;
		
		// add the night mode option to the left nav (at the top)
		boolean nightMode = getDOTClocks_NightMode();
		if(nightMode){
			menu = getString(R.string.dotclocks_daylightmode);
		}else{
			menu = getString(R.string.dotclocks_nightmode);
		}
		
		if (GlobalState.getInstance().getLoggedInUserList().size() > 1)		// must be MUA or TD
		{
			if (GlobalState.getInstance().getCompanyConfigSettings(this.getBaseContext()).getMultipleUsersAllowed())	// MUA explicitly declared
			{
				menu += "," + getString(R.string.mnuadditionaluserswitch);
			}
			else	//assume TD is used
			{
				// in team driving scenario, add team driver switch menu
				menu += "," + getString(R.string.mnuteamdriverswitch);
			}

			if (GlobalState.getInstance().getFeatureService().getShowDebugFunctions())
			{
				// Showing debug functions, add dashboard menu
				menu += "," + getString(R.string.lbldashboardtitle);
			}
		}
		else if (GlobalState.getInstance().getFeatureService().getShowDebugFunctions())
		{
			// Showing debug functions, so offer a menu option to the dashboard
			menu += "," + getString(R.string.lbldashboardtitle);
		}

		return menu;
	}

	private void handleMenuItemSelected(int itemPosition)
	{
		boolean isTeamDriver = (GlobalState.getInstance().getLoggedInUserList().size() > 1);
		boolean isDebugOn = (GlobalState.getInstance().getFeatureService().getShowDebugFunctions());

		// if team driver scenario and debug is on, two menu choices
		if (isTeamDriver && isDebugOn)
		{
			switch (itemPosition)
			{
				case 0: // first option is night mode
					_contentFrag.ToggleDOTClocks_NightMode();  
					_contentFrag.setNightMode();
					_contentFrag.dimScreen();	
					
					// Invalidate the menu to update the items
				    supportInvalidateOptionsMenu();
					break;
				case 1: // second option is switch team driver
					this.finish();
					this.startActivity(SwitchUser.class);
					break;
				case 2: // third option is dashboard
					this.finish();
					this.startActivity(Dashboard.class);
					break;
			}
		}
		// else, only one menu choice (other than night mode) - either switch team driver or dashboard
		else
		{
			switch (itemPosition)
			{
				case 0: // first option is night mode
					_contentFrag.ToggleDOTClocks_NightMode();  
					_contentFrag.setNightMode();
					_contentFrag.dimScreen();	

					// Invalidate the menu to update the items
				    supportInvalidateOptionsMenu();
					break;
				case 1:
					// if team driver scenario, start team driver switch activity
					
					if (isTeamDriver)
					{
						this.finish();
						this.startActivity(SwitchUser.class);
						
					}
					// else debug on scenario, start dashboard activity
					else if (isDebugOn)
					{
						this.finish();
						this.startActivity(Dashboard.class);
					}
					break;
			}
		}
		
		// Used for handling highlighting the selected item in the leftnav
		// We have to allow the leftnav to highlight the selected item
		boolean nightMode = getDOTClocks_NightMode();
		if(nightMode){
			this.setLeftNavSelectedItem(0);							
		}else{
			this.setLeftNavSelectedItem(-1);	
		}
		this.setLeftNavAllowChange(true);
		loadLeftNavFragment();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{		
		handleMenuItemSelected(item.getItemId());
		super.onOptionsItemSelected(item);
		return true;
	}

	public void onNavItemSelected(int menuItem)
	{
		handleMenuItemSelected(menuItem);
	}

	@Override
	public void onBackPressed()
	{
		// Do nothing so the user can't leave
	}

	@Override
	protected void InitController()
	{
		setController(new HosAuditController(this));
	}

	public HosAuditController getMyController()
	{
		return (HosAuditController)getController();
	}

	protected IAPIController getMyEmpController() {
		return _controllerEmp;
	}

	@Override
	protected void loadControls()
	{
		super.loadControls();
		loadContentFragment(new DOTClocksFrag(), true);
		loadFragment(R.id.recap_hours_fragment, new RecapHoursFrag());

        if (_contentFrag != null) {
            boolean	isHyRailUtilized = GlobalState.getInstance().getIsInHyrailDutyStatus();
            boolean isYardMoveUtilized = GlobalState.getInstance().getIsInYardMoveDutyStatus();
            boolean isNonRegDrivingStatusUtilized = GlobalState.getInstance().getIsInNonRegDrivingDutyStatus();

            boolean forceExempt = isHyRailUtilized || isYardMoveUtilized || isNonRegDrivingStatusUtilized;

            _contentFrag.updateClocks(_driveTimeData, _driveTimeData_ResetBreak, _dailyOnDutyData, _weeklyOnDutyData, driveTimeSummary_ResetBreak, forceExempt);
        }

		startTimer();
	}

	@Override
	public void setFragments()
	{
		super.setFragments();
		Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
		_contentFrag = (DOTClocksFrag)f;
        this.loadData();
	}

	@Override
	protected void loadData()
	{
		HosAuditController controller = getMyController();

		controller.UpdateForCurrentLogEvent();

		_driveTimeData = new ClockData(controller.DriveTimeSummary());

        //Logic depends on having _driveTimeData_resetBreak being null.
		_driveTimeData_ResetBreak = null;
		driveTimeSummary_ResetBreak = controller.DriveTimeRestBreakSummary();
		if (driveTimeSummary_ResetBreak != null) {
			_driveTimeData_ResetBreak = new ClockData(driveTimeSummary_ResetBreak);
		}

		_dailyOnDutyData = new ClockData(controller.DailyDutySummary());
		_weeklyOnDutyData = new ClockData(controller.WeeklyDutySummary());


	}


	private boolean shouldCloseBecauseTimeout()
	{
		boolean shouldClose = true;

		User user = GlobalState.getInstance().getCurrentUser();
		Date lastTimestamp = GlobalState.getInstance().getLastTimerPop();
		if (user != null && lastTimestamp != null)
		{		
			Date now = DateUtility.getCurrentDateTimeUTC();
			long differenceMillis = now.getTime() - lastTimestamp.getTime();
			double differenceMinutes = DateUtility.ConvertMillisecondsToMinutes(differenceMillis);
			double timeoutMinutes = (double)user.getDrivingStopTimeMinutes();
			shouldClose = (differenceMinutes >= timeoutMinutes);
		}
		
		return shouldClose;
	}

	public void startTimer()
	{
		stopTimer();

		_clockUpdateTimer = new Timer();
		_clockUpdateTimer.scheduleAtFixedRate(new TimerTask()
		{
			@Override
			public void run()
			{
				if (shouldCloseBecauseTimeout())
				{
					finish();
				}
				else
				{
					// Decrease all counters
					_driveTimeData.decreaseAvailable(CLOCK_TICK_MILLISECONDS);
					if (_driveTimeData_ResetBreak != null)
						_driveTimeData_ResetBreak.decreaseAvailable(CLOCK_TICK_MILLISECONDS);
					_dailyOnDutyData.decreaseAvailable(CLOCK_TICK_MILLISECONDS);
					_weeklyOnDutyData.decreaseAvailable(CLOCK_TICK_MILLISECONDS);
					runOnUiThread(new Runnable()
					{
						public void run()
						{
                            boolean	isHyRailUtilized = GlobalState.getInstance().getIsInHyrailDutyStatus();
                            boolean isYardMoveUtilized = GlobalState.getInstance().getIsInYardMoveDutyStatus();
                            boolean isNonRegDrivingStatusUtilized = GlobalState.getInstance().getIsInNonRegDrivingDutyStatus();

                            boolean forceExempt = isHyRailUtilized || isYardMoveUtilized || isNonRegDrivingStatusUtilized;

                            _contentFrag.updateClocks(_driveTimeData, _driveTimeData_ResetBreak, _dailyOnDutyData, _weeklyOnDutyData, driveTimeSummary_ResetBreak, forceExempt);
						}
					});
				}
			}
		}, CLOCK_TICK_MILLISECONDS, CLOCK_TICK_MILLISECONDS);
	}

	private void stopTimer()
	{
		if (_clockUpdateTimer != null)
			_clockUpdateTimer.cancel();
	}

	public IAPIController getEmployeeLogController() {
		return getMyEmpController();
	}

}
