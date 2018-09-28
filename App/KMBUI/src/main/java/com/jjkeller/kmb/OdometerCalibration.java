package com.jjkeller.kmb;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.jjkeller.kmb.fragments.OdometerCalibrationFrag;
import com.jjkeller.kmb.interfaces.IOdometerCalibration.OdometerCalibrationFragActions;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.EobrConfigController;
import com.jjkeller.kmbapi.controller.TeamDriverController;
import com.jjkeller.kmbui.R;

public class OdometerCalibration extends BaseActivity implements OdometerCalibrationFragActions
{
	private OdometerCalibrationFrag _contentFragment;

	private static final String ODOMETER_KEY = "odometer";
	private float _dashOdom = 0.0F;
	private float _dashOdomValue = 0.0F;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.odometercalibration);
		
		if (savedInstanceState != null)
			_dashOdom = savedInstanceState.getFloat(ODOMETER_KEY);

		loadContentFragment(new OdometerCalibrationFrag());

		mFetchLocalDataTask = new FetchLocalDataTask(this.getClass().getSimpleName());
		mFetchLocalDataTask.execute();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		float odometer = 0f;
		try
		{
			odometer = Float.parseFloat(_contentFragment.getEditOdometerTextBox().getText().toString());
		}
		catch (Exception ex)
		{
		}
		outState.putFloat(ODOMETER_KEY, odometer);
	}

	@Override
	protected void InitController()
	{
		this.setController(new EobrConfigController(this));
	}

	protected EobrConfigController getMyController()
	{
		return (EobrConfigController)this.getController();
	}

	@Override
	protected void loadData()
	{
		if (_dashOdom == 0f) // No odometer value was loaded from saved instance state
			_dashOdom = this.getMyController().CalculateDashboardOdometer();
	}

	@Override
	protected void loadControls()
	{
		super.loadControls();

		if (_dashOdom > 0)
		{
			_contentFragment.getEditOdometerTextBox().setText(String.format("%.1f", _dashOdom));
		}
		else if (_dashOdom == 0)
		{
			_contentFragment.getEditOdometerTextBox().setText("");
		}

		if (_dashOdom < 0)
		{
			// this means that no EOBR is connected
			// so disable the OK button
			_contentFragment.getSaveButton().setEnabled(false);
		}
		else
		{
			// an eobr is connected, check if calibration is required
			if (this.getMyController().IsOdometerCalibrationRequired())
			{
				_contentFragment.getMessageTextView().setText(this.getString(R.string.msg_calibrationisrequired));
			}
		}
	}

	@Override
	public void setFragments()
	{
		super.setFragments();
		Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
		_contentFragment = (OdometerCalibrationFrag)fragment;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (GlobalState.getInstance().getPassedRods() == true)
			this.CreateOptionsMenu(menu, false);
		return true;
	}

	// 10/25/2012 AMO: Adding function for when the actionbar home button is pressed
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// if the item is the home button
		if(item.getItemId() == android.R.id.home){	
			// if the app is passed the login
			if (this.getController() != null && this.getController().getCurrentUser() != null && GlobalState.getInstance().getPassedRods() == true)
			{
				// Call the return function to handle the navigation
				Return(true);
			}
		}	
		super.onOptionsItemSelected(item);
		
		return true;
	}
	
	@Override
	protected void Return(boolean success)
	{
		boolean isLogin = getIntent().getBooleanExtra(getString(R.string.extra_isloginprocess), false);
		boolean isTeamDriver = getIntent().getBooleanExtra(getString(R.string.extra_teamdriverlogin), false);
		boolean displayOffDutyLogs = getIntent().getBooleanExtra(getString(R.string.extra_displayoffdutylogs), false);
		
		if(isLogin && isTeamDriver && !GlobalState.getInstance().getCompanyConfigSettings(this).getMultipleUsersAllowed())
		{
			if (GlobalState.getInstance().getLoggedInUserList().size() <= 1)
			{
				if (GlobalState.getInstance().getTeamDriverMode() == GlobalState.TeamDriverModeEnum.SHAREDDEVICE)
				{
					Bundle extras = new Bundle();
					extras.putBoolean(getString(R.string.extra_teamdriverlogin), true);

					startActivity(TeamDriverNextStep.class, Intent.FLAG_ACTIVITY_SINGLE_TOP, extras);
				}
				else
				{
					startActivity(TeamDriverAddDriver.class, Intent.FLAG_ACTIVITY_SINGLE_TOP);
				}
			}
			else
			{
				TeamDriverController ctrlr = new TeamDriverController(this);

				if (ctrlr.IsTeamDriversOnDuty())
				{
					//both on-duty.
					this.startActivity(TeamDriverFirstDriver.class, Intent.FLAG_ACTIVITY_SINGLE_TOP);
				}
				else
				{
					//one or none are on duty.
					ctrlr.SetTeamDriver();
					startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_SINGLE_TOP);
				}
			}
		}
		else {
			Bundle extras = new Bundle();
			
			if(displayOffDutyLogs) {
				extras.putBoolean(getResources().getString(R.string.extra_displayoffdutylogs), true);
			}
			startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_CLEAR_TOP, extras);
		}

		finish();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		// Handle the back button like a cancel during login
		if (keyCode == KeyEvent.KEYCODE_BACK && getIntent().hasExtra(this.getResources().getString(R.string.extra_displayoffdutylogs)))
		{

			Return(true);

			// Say that we've consumed the event
			return true;
		}

		// Otherwise let system handle keypress normally
		return super.onKeyDown(keyCode, event);
	}

	public void handleCancelButtonClick()
	{
		this.Return();
	}

	public void handleSaveButtonClick()
	{
		if (_contentFragment.getEditOdometerTextBox().getText().length() <= 0)
			_dashOdomValue = -1;
		else
			_dashOdomValue = Float.valueOf(_contentFragment.getEditOdometerTextBox().getText().toString());

		if (_dashOdomValue < 0)
		{
			_contentFragment.getMessageTextView().setText(this.getString(R.string.msg_dashboardisrequired));
			return;
		}
		else if (_dashOdomValue >= 10000000F)
		{
			_contentFragment.getMessageTextView().setText(getString(R.string.msg_dashboardlimitexceeded));
			return;
		}

		boolean isEobrOdometerValid = true;
		Bundle bundleOdometerValid = this.getMyController().IsOdometerOffSetValid(_dashOdomValue);
		
		int isNotConnected = bundleOdometerValid.getInt(this.getString(R.string.isconnectedforodometercal));
		if (isNotConnected == 10) {
			_contentFragment.getSaveButton().setEnabled(false);
			if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
				_contentFragment.getMessageTextView().setText(R.string.msg_elddevicenotconnected);
			} else {
				_contentFragment.getMessageTextView().setText(R.string.msg_eobrdevicenotconnected);
			}
		}
		else {
			boolean isOdometerOffsetValid = bundleOdometerValid.getBoolean(this.getString(R.string.isodometeroffsetvalid));
			isEobrOdometerValid = bundleOdometerValid.getBoolean(this.getString(R.string.iseobrodometervalid));
	
			if (!isEobrOdometerValid)
			{
				_contentFragment.getMessageTextView().setText(R.string.msg_erroroccuredduringcalibration);
			}
			else if (!isOdometerOffsetValid)
			{
				handleOdometerCalibrationClick(String.format(this.getString(R.string.msg_odometerdoesnotappeartobevalid), String.valueOf(_dashOdomValue)));
			}
			else
			{
				this.PerformCalibration();
			}
		}
	}

	private void handleOdometerCalibrationClick(String msg){
		OdometerCalibration.this.ShowConfirmationMessage(OdometerCalibration.this, msg,
				new ShowMessageClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						super.onClick(dialog, id);
						OdometerCalibration.this.PerformCalibration();
					}
				}, new ShowMessageClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						super.onClick(dialog, id);		
						_contentFragment.getEditOdometerTextBox().setText("");
						OdometerCalibration.this.UnlockScreenRotation();
					}
				});
	}

	private void PerformCalibration()
	{
		mOdometerCalibrationTask = new OdometerCalibrationTask();
		mOdometerCalibrationTask.execute();
		OdometerCalibration.this.UnlockScreenRotation();
	}

	private static OdometerCalibrationTask mOdometerCalibrationTask;

	private class OdometerCalibrationTask extends AsyncTask<Void, Void, Integer>
	{
		ProgressDialog pd;

		protected void onPreExecute()
		{
			LockScreenRotation();
			showProgressDialog();
		}

		protected Integer doInBackground(Void... params)
		{
			int calibrationStatus = 0;
			if (_contentFragment.getEditOdometerTextBox().length() > 0)
			{
				int status = 0;
				try
				{
					status = OdometerCalibration.this.getMyController().PerformOdometerCalibrationOffset(_dashOdomValue);
				}
				catch (Exception e)
				{
					status = 0;
				}

				calibrationStatus = status;
			}

			return calibrationStatus;
		}

		protected void onPostExecute(Integer calibrationStatus)
		{
			dismissProgressDialog();
			UnlockScreenRotation();

			if (calibrationStatus == 0)
				_contentFragment.getMessageTextView().setText(R.string.msg_erroroccuredduringcalibration);
			else if (calibrationStatus == -1)
				_contentFragment.getMessageTextView().setText(R.string.msg_odomCalibration_EngineNotOn);
			else
				OdometerCalibration.this.Return();
		}

		// Added public methods so that dialogs and context can be
		// re-established
		// after an orientation change (ie. activity recreated).
		public void showProgressDialog()
		{
			pd = CreateFetchDialog(getString(R.string.msgsaving));
		}

		public void dismissProgressDialog()
		{
			DismissProgressDialog(OdometerCalibration.this, this.getClass(), pd);
		}
	}
}
