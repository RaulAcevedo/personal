package com.jjkeller.kmb;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.alk.copilot.CopilotService;
import com.alk.cpik.AbstractCopilotEventListener;
import com.alk.cpik.Copilot;
import com.jjkeller.kmb.fragments.ALKPersConvFrag;
import com.jjkeller.kmb.fragments.AlkCopilotFrag;
import com.jjkeller.kmb.fragments.DOTClocksLeftFrag;
import com.jjkeller.kmb.fragments.LeftNavFrag;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmb.share.ClockData;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.ControllerFactory;
import com.jjkeller.kmbapi.controller.HosAuditController;
import com.jjkeller.kmbapi.controller.LogEntryController;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.controller.utility.EmployeeLogUtilities;
import com.jjkeller.kmbapi.enums.DutyStatusEnum;
import com.jjkeller.kmbapi.enums.EmployeeLogProvisionTypeEnum;
import com.jjkeller.kmbapi.proxydata.DutySummary;
import com.jjkeller.kmbapi.proxydata.LogEvent;
import com.jjkeller.kmbui.R;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class AlkCopilot extends BaseActivity implements LeftNavFrag.OnNavItemSelectedListener,
		LeftNavFrag.ActivityMenuItemsListener
{
	protected static final long FIFTEEN_SECONDS = 15000;
	protected static final long MILLISECONDS_PER_MINUTE = 60000;
	protected static final String BUNDLE_KEY_HAS_DRIVING_OCCURRED = "_hasDrivingOccurred";
	
	protected static final int LEFTNAVFRAG_MENU = 1;
	protected static final int LEFTNAVFRAG_DOTCLOCKS = 2;
	protected static final int LEFTNAVFRAG_PERSCONV = 3;
	
	private DOTClocksLeftFrag _contentLeftFrag;
	private AlkCopilotFrag _contentAlkCopilotFrag;

	private ClockData _driveTimeData;
	private ClockData _driveTimeData_ResetBreak;
	private ClockData _dailyOnDutyData;
	private ClockData _weeklyOnDutyData;
	private Timer _alertTimer;
	private Timer _clockUpdateTimer;
	private Timer _leftNavUpdateTimer;

	private int _leftNavFragContent;
	private boolean _hasDrivingOccurred = false;

	private DutySummary _driveTimeSummary_ResetBreak;

	private LogEntryController _logEntryController = ControllerFactory.getInstance().getLogEntryController();

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		if (savedInstanceState != null)
		{
			_hasDrivingOccurred = savedInstanceState.getBoolean(BUNDLE_KEY_HAS_DRIVING_OCCURRED);
		}
		
		setContentView(R.layout.alkcopilot);
		
		bindCoPilotService();
		registerAlkCoPilotStartupListenerIfNotActive();
		
		startAlertTimer();
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		refreshData();
		addCopilotFragmentIfActive();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putBoolean(BUNDLE_KEY_HAS_DRIVING_OCCURRED, _hasDrivingOccurred);
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		stopViewTimers();
		removeCopilotFragment();
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		stopAlertTimer();
		unbindCopilot();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		if (shouldDisplayTeamDriverSwitch())
		{
			this.CreateOptionsMenu(menu, false, true);
		}
		return true;
	}
	
	@Override
	public String getActivityMenuItemList()
	{
		if (shouldDisplayTeamDriverSwitch())
			return getString(R.string.mnuteamdriverswitch);
		else if (shouldDisplayAdditionalUserSwitch())
			return getString(R.string.mnuadditionaluserswitch);
		return getString(R.string.btn_KellerMobile_trademark);
	}

	@Override
	public void onNavItemSelected(int menuItem)
	{
		handleMenuItemSelected(menuItem);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Don't allow home button if clocks are displayed and not team driving
		if (item.getItemId() == android.R.id.home && (_leftNavFragContent != LEFTNAVFRAG_MENU) && !isTeamDriving())
			return false;
		
		if (handleMenuItemSelected(item.getItemId()))
			return true;
		return super.onOptionsItemSelected(item);
	}

	private boolean handleMenuItemSelected(int itemPosition)
	{
		switch (itemPosition)
		{
			case 0:
				if (shouldDisplayTeamDriverSwitch())
				{
					this.finish();
					this.startActivity(SwitchUser.class);
				}
				else
				{
					finishActivity();
				}
				return true;
		}
		return false;
	}

	private boolean shouldDisplayTeamDriverSwitch()
	{
		if (GlobalState.getInstance().getCompanyConfigSettings(this.getBaseContext()).getMultipleUsersAllowed()) {    // MUA explicitly declared
			return false;
		} else {
			return isTeamDriving() && isCurrentUserDriverAndMoving();
		}

	}

	private boolean shouldDisplayAdditionalUserSwitch()
	{
		if (GlobalState.getInstance().getCompanyConfigSettings(this.getBaseContext()).getMultipleUsersAllowed()) {    // MUA explicitly declared
			return isCurrentUserDriverAndMoving();
		} else {
			return false;
		}

	}

	private void finishActivity()
	{
		this.finish();
		this.startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
		return (HosAuditController) getController();
	}

	private void refreshData()
	{
		// Somewhere around Android version 3.0, the behavior of the AsyncTask
		// was changed from being a parallel
		// operation to a serialized operation. Therefore, in order for newer
		// versions of Android to operate as
		// KMB expects (in a parallel fashion), we need to add the following
		// code.
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
	protected void loadControls()
	{
		super.loadControls();

		if (_logEntryController.IsVehicleInMotion())
		{
			if (isPersonallyConveying())
			{
				loadALKPersonalConveyanceFragment();
				_leftNavFragContent = LEFTNAVFRAG_PERSCONV;
			}
			else
			{
				loadDOTClocksLeftNavFragment();
				_leftNavFragContent = LEFTNAVFRAG_DOTCLOCKS;
			}
			updateViews();
		}
		else
		{
			loadLeftNavFragment();
			_leftNavFragContent = LEFTNAVFRAG_MENU;
		}

		startViewTimers();
	}

	@Override
	protected void loadData()
	{
		HosAuditController controller = getMyController();
		controller.UpdateForCurrentLogEvent();

		_driveTimeData = new ClockData(controller.DriveTimeSummary());
        _dailyOnDutyData = new ClockData(controller.DailyDutySummary());
        _weeklyOnDutyData = new ClockData(controller.WeeklyDutySummary());

        //Logic depends on _driveTimeData_ResetBreak being nullable.
		_driveTimeData_ResetBreak = null;
		_driveTimeSummary_ResetBreak = controller.DriveTimeRestBreakSummary();
		if (_driveTimeSummary_ResetBreak != null)
		{
            _driveTimeData_ResetBreak = new ClockData(_driveTimeSummary_ResetBreak);
		}

	}

	@Override
	protected void onSpecialDrivingEndResponse(EmployeeLogProvisionTypeEnum drivingCategory, boolean ended) {
		if(drivingCategory == EmployeeLogProvisionTypeEnum.PERSONALCONVEYANCE)
			finishActivity();
	}

	/**
	 * This method updates views, so this must be called from a UI thread.
	 */
	private void updateViews()
	{
		if (isDrivingFinished())
		{
			finishActivity();
			return;
		}
		
		if (isCurrentUserDriverAndMoving())
		{
			if (isPersonallyConveying())
			{
				if (_leftNavFragContent != LEFTNAVFRAG_PERSCONV)
				{
					loadALKPersonalConveyanceFragment();
					supportInvalidateOptionsMenu();
				}
				
				_leftNavFragContent = LEFTNAVFRAG_PERSCONV;
			}
			else 
			{
				if (_leftNavFragContent != LEFTNAVFRAG_DOTCLOCKS)
				{
					loadDOTClocksLeftNavFragment();
					supportInvalidateOptionsMenu();
				}

				_leftNavFragContent = LEFTNAVFRAG_DOTCLOCKS;
                _contentLeftFrag.updateClocks(_driveTimeData, _driveTimeData_ResetBreak, _dailyOnDutyData, _weeklyOnDutyData, _driveTimeSummary_ResetBreak);
			}
		}
		else
		{
			if (_leftNavFragContent != LEFTNAVFRAG_MENU)
			{
				loadLeftNavFragment();
				supportInvalidateOptionsMenu();
			}
			_leftNavFragContent = LEFTNAVFRAG_MENU;
		}
	}
	
	private boolean isCurrentUserDriverAndMoving()
	{
		return _logEntryController.IsCurrentUserTheDriver() && _logEntryController.IsVehicleInMotion();
	}
	
	private boolean isPersonallyConveying()
	{
		return GlobalState.getInstance().getIsInPersonalConveyanceDutyStatus();
	}
	
	private boolean isDrivingFinished()
	{
		boolean isDrivingFinished = false;
		
		LogEvent lastLogEvent = EmployeeLogUtilities.GetLastEventInLog(_logEntryController.getCurrentDriversLog());
		if (lastLogEvent != null)
		{
			if (_hasDrivingOccurred)
			{
				isDrivingFinished = (lastLogEvent.getDutyStatusEnum().getValue() != DutyStatusEnum.DRIVING);
			}
			else
			{
				_hasDrivingOccurred = (lastLogEvent.getDutyStatusEnum().getValue() == DutyStatusEnum.DRIVING);
			}
		}

		return isDrivingFinished;
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
			double timeoutMinutes = (double) user.getDrivingStopTimeMinutes();
			shouldClose = (differenceMinutes >= timeoutMinutes);
		}

		return shouldClose;
	}
	
	private void displayAlertIfNecessary()
	{
		String alertMessage = _logEntryController.getAlertMessage();
		if (alertMessage != null && alertMessage.length() > 0)
		{
			ShowMessage(this, getString(R.string.msg_title_alert), alertMessage);
		}
	}
	
	private void startAlertTimer()
	{
		stopAlertTimer();
		
		_alertTimer = new Timer();
		_alertTimer.scheduleAtFixedRate(new TimerTask()
		{
			@Override
			public void run()
			{
				runOnUiThread(new Runnable()
				{
					public void run()
					{
						displayAlertIfNecessary();
					}
				});
			}
		}, FIFTEEN_SECONDS, FIFTEEN_SECONDS);
	}
	
	private void stopAlertTimer()
	{
		if (_alertTimer != null)
			_alertTimer.cancel();
	}
	
	private void startViewTimers()
	{
		stopViewTimers();

		_clockUpdateTimer = new Timer();
		_clockUpdateTimer.scheduleAtFixedRate(new TimerTask()
		{
			@Override
			public void run()
			{
				if (_driveTimeData != null && _dailyOnDutyData != null && _weeklyOnDutyData != null)
				{
					// Decrease all counters
					_driveTimeData.decreaseAvailable(MILLISECONDS_PER_MINUTE);
					if (_driveTimeData_ResetBreak != null)
						_driveTimeData_ResetBreak.decreaseAvailable(MILLISECONDS_PER_MINUTE);

					_dailyOnDutyData.decreaseAvailable(MILLISECONDS_PER_MINUTE);
					_weeklyOnDutyData.decreaseAvailable(MILLISECONDS_PER_MINUTE);
				}
			}
		}, MILLISECONDS_PER_MINUTE, MILLISECONDS_PER_MINUTE);

		_leftNavUpdateTimer = new Timer();
		_leftNavUpdateTimer.scheduleAtFixedRate(new TimerTask()
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
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							updateViews();
						}
					});
				}
			}
		}, FIFTEEN_SECONDS, FIFTEEN_SECONDS);
	}

	private void stopViewTimers()
	{
		if (_clockUpdateTimer != null)
			_clockUpdateTimer.cancel();
		if (_leftNavUpdateTimer != null)
			_leftNavUpdateTimer.cancel();
	}
	
	private boolean isTeamDriving()
	{
		return GlobalState.getInstance().getLoggedInUserList().size() > 1;
	}

	////////////////////////////////////////////////////
	// Left nav
	//

	protected void loadDOTClocksLeftNavFragment()
	{
		if (!isFinishing())
		{
			View leftNavLayout = findViewById(R.id.leftnav_fragment);
			if (leftNavLayout != null)
			{
				// Create new fragment and transaction
				FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

				// Replace fragment
//				transaction.replace(R.id.leftnav_fragment, new DOTClocksLeftFrag());

				_contentLeftFrag = new DOTClocksLeftFrag();
				transaction.replace(R.id.leftnav_fragment, _contentLeftFrag);

				// Commit the transaction
				transaction.commit();

				leftNavLayout.setBackgroundColor(getResources().getColor(R.color.white));
			}
		}
	}
	
	protected void loadALKPersonalConveyanceFragment()
	{
		if (!isFinishing())
		{
			View leftNavLayout = findViewById(R.id.leftnav_fragment);
			if (leftNavLayout != null)
			{
				// Create new fragment and transaction
				FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

				// Replace fragment
				transaction.replace(R.id.leftnav_fragment, new ALKPersConvFrag());

				// Commit the transaction
				transaction.commit();

				leftNavLayout.setBackgroundColor(getResources().getColor(R.color.white));
			}
		}
	}
	
	@Override
	protected void loadLeftNavFragment()
	{
		if (!isFinishing())
		{
			View leftNavLayout = findViewById(R.id.leftnav_fragment);
			if (leftNavLayout != null)
			{
				// Create new fragment and transaction
				FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
				setLeftNavFragment(new LeftNavFrag(R.layout.leftnav_item_small));

				// Replace fragment
				transaction.replace(R.id.leftnav_fragment, getLeftNavFragment());

				// Commit the transaction
				transaction.commit();

				setLeftNavSelectionItems();
				leftNavLayout.setBackgroundColor(getResources().getColor(R.color.menugray));
			}
		}
	}

	@Override
	public void setFragments()
	{
		ActionBar actionBar = getSupportActionBar();
		actionBar.setTitle(R.string.actionbar_title);
		actionBar.setHomeButtonEnabled(true);

		Fragment f = getSupportFragmentManager().findFragmentById(R.id.leftnav_fragment);
		if (f instanceof DOTClocksLeftFrag)
			_contentLeftFrag = (DOTClocksLeftFrag) f;
	}

	////////////////////////////////////////////////////
	// CoPilot
	//
	
	private void addCopilotFragmentIfActive()
	{
		if (Copilot.isActive())
		{
			loadCopilotFragment();
		}
	}

	public void loadCopilotFragment()
	{
		if (!isFinishing())
		{
			_contentAlkCopilotFrag = new AlkCopilotFrag();
			loadContentFragment(_contentAlkCopilotFrag, true, true);
		}
	}
	
	private void removeCopilotFragment()
	{
		if (_contentAlkCopilotFrag != null)
			_contentAlkCopilotFrag.removeViewFromParent();
	}

	private void bindCoPilotService()
	{
		Intent intent = new Intent(this, CopilotService.class);
		bindService(intent, _copilotServiceConnection, Context.BIND_AUTO_CREATE);
	}

	private void unbindCopilot()
	{
		if (_copilotServiceConnection != null)
			unbindService(_copilotServiceConnection);
	}

	private ServiceConnection _copilotServiceConnection = new ServiceConnection()
	{
		public void onServiceConnected(ComponentName className, IBinder service)
		{
		}

		public void onServiceDisconnected(ComponentName className)
		{
		}
	};
	
	private void registerAlkCoPilotStartupListenerIfNotActive()
	{
		if (!Copilot.isActive())
		{
			AlkCoPilotStartupListener alkCoPilotStartuplistener = new AlkCoPilotStartupListener(this);
			alkCoPilotStartuplistener.registerListener();
		}
	}

	private static class AlkCoPilotStartupListener extends AbstractCopilotEventListener{

		private WeakReference<AlkCopilot> weakActivity;

		public AlkCoPilotStartupListener(AlkCopilot activity)
		{
			this.weakActivity = new WeakReference<>(activity);
		}

		public void registerListener()
		{
			Copilot.registerListener(this);
		}

		public void unregisterListener()
		{
			Copilot.unregisterListener(this);
		}

		public void onCPStartup()
		{
			unregisterListener();

			final AlkCopilot activity = weakActivity.get();
			if (activity != null)
			{
				activity.runOnUiThread(new Runnable()
				{
					public void run()
					{
						activity.loadCopilotFragment();
					}
				});
			}
		}
	}
}
