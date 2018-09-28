package com.jjkeller.kmb;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.jjkeller.kmb.fragments.CancelAcceptDialogFragment;
import com.jjkeller.kmb.fragments.LoginFrag;
import com.jjkeller.kmb.interfaces.ILogin.LoginControllerMethods;
import com.jjkeller.kmb.interfaces.ILogin.LoginFragActions;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmb.share.MobileDeviceHandler;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.AppUpdateController;
import com.jjkeller.kmbapi.controller.AppUpdateFactory;
import com.jjkeller.kmbapi.controller.EOBR.EobrReader;
import com.jjkeller.kmbapi.controller.FmcsaEldInfoController;
import com.jjkeller.kmbapi.controller.LogEntryController;
import com.jjkeller.kmbapi.controller.LoginController;
import com.jjkeller.kmbapi.controller.SystemStartupController;
import com.jjkeller.kmbapi.controller.interfaces.IAPIController;
import com.jjkeller.kmbapi.controller.interfaces.IAppUpdateCheck;
import com.jjkeller.kmbapi.controller.share.KmbApplicationException;
import com.jjkeller.kmbapi.controller.share.MandateObjectFactory;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.controller.utility.DeviceInfo;
import com.jjkeller.kmbapi.controller.utility.ErrorLogHelper;
import com.jjkeller.kmbapi.controller.utility.FileUtility;
import com.jjkeller.kmbapi.controller.utility.NotificationUtilities;
import com.jjkeller.kmbapi.controller.utility.RESTWebServiceHelper;
import com.jjkeller.kmbapi.employeelogeldevents.Enums.CompositeEmployeeLogEldEventTypeEventCodeEnum;
import com.jjkeller.kmbapi.eobrengine.EobrDeviceDescriptor;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.wifi.WifiStatus;
import com.jjkeller.kmbui.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Login extends BaseActivity implements LoginFragActions,
		LoginControllerMethods, CancelAcceptDialogFragment.OnDialogActionClickedListener {

	private static final String EULA_DIALOG_TAG = "EULA_DIALOG_TAG";
	private MobileDeviceHandler _mobileDevice;

	//boolean to check for multiple user functionality which when true will override team driver
	private boolean _multipleUsersEnabled;

	public Login() {
		_loginHandler = new FirstUserLoginHandler();
		_viewMode = new EditViewMode();
	}

	public abstract class ViewMode {
		public abstract void SuccessfulAuthentication();
		public abstract void doInBackgroundAuthenticateTask(Date startDateTime);
	}

	public class EditViewMode extends ViewMode {

		@Override
		public void SuccessfulAuthentication() {
			_loginHandler.SuccessfulAuthentication(Login.this);
		}

		public void doInBackgroundAuthenticateTask(Date startDateTime)
		{
			_loginHandler.doInBackgroundAuthenticateTask(startDateTime);
		}
	}

	public class ReadOnlyViewMode extends ViewMode {

		@Override
		public void SuccessfulAuthentication() {
			mViewOnlyLoginTask = new ViewOnlyLoginTask();
			mViewOnlyLoginTask.execute();
		}

		public void doInBackgroundAuthenticateTask(Date startDateTime)
		{
			// don't create a log because we are just trying to view hours
		}
	}

	public abstract class LoginHandler {
		protected boolean _asTeam = false;

		public void Return() {
			Login.this.finish();
		}

		public abstract void onPreExecuteAuthenticateTask(Date startDateTime);

		public abstract void SuccessfulAuthentication(Context ctx);

		public abstract void doInBackgroundAuthenticateTask(Date startDateTime);

		public void setAsTeam(boolean asTeam) { _asTeam = asTeam; }
		public boolean getAsTeam() { return _asTeam; }
	}

	public class FirstUserLoginHandler extends LoginHandler {
		@Override
		public void SuccessfulAuthentication(Context ctx) {
			//ELDLoginEvent
			IAPIController empLogController = MandateObjectFactory.getInstance(ctx, GlobalState.getInstance().getFeatureService()).getCurrentEventController();
			try {
				empLogController.CreateLoginLogoutEvent(CompositeEmployeeLogEldEventTypeEventCodeEnum.LoginEvent);
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				// DO NOTHING FOR NOW.  Email being sent from TH to TD
				// aaz 10/14/14
				e.printStackTrace();
			}


			SharedPreferences userPref = getSharedPreferences(
					getString(R.string.sharedpreferencefile), 0);

			if(GlobalState.getInstance().getCompanyConfigSettings(ctx).getIsGeotabEnabled()){
				handleDiscovery(null);
			}else{
				connectEobr(userPref);
			}
		}

		public void Return() {
			// 11/2/11 JHM - Make sure that we release an EOBR if we connected
			// during startup.
			try {
				(new SystemStartupController(Login.this)).ShutdownEobrDevice();
			} catch (KmbApplicationException kae) {
				Login.this.HandleException(kae);
			}

			// If we cancel from login, make sure we're properly disposing of
			// our global state/application object.
			// Only tear down if we're in the login process.
			GlobalState.getInstance().tearDown();

			// Remove notifications when exiting app
			NotificationUtilities.CancelAllNotifications(Login.this);

			Process.killProcess(Process.myPid());

			super.Return();
		}

		@Override
		public void onPreExecuteAuthenticateTask(Date startDateTime) {
		}

		@Override
		public void doInBackgroundAuthenticateTask(Date startDateTime) {
		}
	}

	public class MultipleUsersLoginHandler extends  LoginHandler {
		@Override
		public void SuccessfulAuthentication(Context ctx) {
			Intent intent = null;

			//ELDLoginEvent
			IAPIController empLogController = MandateObjectFactory.getInstance(ctx, GlobalState.getInstance().getFeatureService()).getCurrentEventController();
			try {
				empLogController.CreateLoginLogoutEvent(CompositeEmployeeLogEldEventTypeEventCodeEnum.LoginEvent);
			} catch (Throwable e) {
				e.printStackTrace();
			}

			if (getMyController().getIsUserRequiredToChangePassword())
			{
				// need to change password, but indicate that we should go to trip
				// info after that
				intent = new Intent(Login.this, ChangePassword.class);
				intent.putExtra(ctx.getString(R.string.extra_nextActivity), ctx.getString(R.string.activity_tripInfo));
			}
			else
			{
				boolean isExemptLogEnabled = GlobalState.getInstance().getCurrentUser().getIsMobileExemptLogAllowed();
				boolean isExemptFromEldUse = GlobalState.getInstance().getCurrentUser().getExemptFromEldUse();
				boolean isELDMandateEnabled = GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled();

				// if Exempt is Enabled and the EmployeeLog ruleset is US60||US70, go to ExemptLogType screen, else SelectDutyStatus screen
				if ((isExemptLogEnabled && GlobalState.getInstance().getCurrentUser().getRulesetTypeEnum().isUSFederalRuleset()) || (isExemptFromEldUse && isELDMandateEnabled))
				{
					intent = new Intent(Login.this, ExemptLogType.class);
					intent.putExtra(ctx.getString(R.string.extra_nextActivity), ctx.getString(R.string.activity_tripInfo));
				}
				else
				{
					//PBI 49820 - Go to SelectDutyStatus if on Mandate or AOBRD
					intent = new Intent(Login.this, SelectDutyStatus.class);
				}
				intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			}
			intent.putExtra(getString(R.string.extra_isloginprocess), true);

			if (intent != null) {
				Login.this.finish();
				startActivity(intent);
			}
		}

		@Override
		public void onPreExecuteAuthenticateTask(Date startDateTime) {

		}

		@Override
		public void doInBackgroundAuthenticateTask(Date startDateTime) {

		}

		public void Return() {
			super.Return();
		}
	}

	public class TeamDriverLoginHandler extends LoginHandler {
		@Override
		public void SuccessfulAuthentication(Context ctx) {
			Intent intent = null;

			//ELDLoginEvent
			IAPIController empLogController = MandateObjectFactory.getInstance(ctx, GlobalState.getInstance().getFeatureService()).getCurrentEventController();
			try {
				empLogController.CreateLoginLogoutEvent(CompositeEmployeeLogEldEventTypeEventCodeEnum.LoginEvent);
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				// DO NOTHING FOR NOW.  Email being sent from TH to TD
				// aaz 10/14/14
				e.printStackTrace();
			}


			// Set the TeamDriverMode to Shared Device
			GlobalState.getInstance().setTeamDriverMode(GlobalState.TeamDriverModeEnum.SHAREDDEVICE);

			if (getMyController().getIsUserRequiredToChangePassword())
			{
				// need to change password, but indicate that we should go to trip
				// info after that
				intent = new Intent(Login.this, ChangePassword.class);
				intent.putExtra(ctx.getString(R.string.extra_nextActivity), ctx.getString(R.string.activity_tripInfo));
				intent.putExtra(getString(R.string.extra_teamdriverlogin), true);
			}
			else
			{
					boolean isExemptLogEnabled = GlobalState.getInstance().getCurrentUser().getIsMobileExemptLogAllowed();
					boolean isExemptFromEldUse = GlobalState.getInstance().getCurrentUser().getExemptFromEldUse();
					boolean isELDMandateEnabled = GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled();

					// if Exempt is Enabled and the EmployeeLog ruleset is US60||US70, go to ExemptLogType screen, else SelectDutyStatus screen
					if ((isExemptLogEnabled && GlobalState.getInstance().getCurrentUser().getRulesetTypeEnum().isUSFederalRuleset()) || (isExemptFromEldUse && isELDMandateEnabled))
					{
						intent = new Intent(Login.this, ExemptLogType.class);
					}
					else
					{
						intent = new Intent(Login.this, SelectDutyStatus.class);
					}

				intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			}

			if (intent != null) {
				Login.this.finish();
				startActivity(intent);
			}
		}

		public void Return() {
			super.Return();
		}

		@Override
		public void onPreExecuteAuthenticateTask(Date startDateTime) {

		}

		@Override
		public void doInBackgroundAuthenticateTask(Date startDateTime) {

		}
	}

	private static final int REQUEST_ENABLE_BT = 1;

	private Bundle _configChangedState;
	private String _username;
	private String _password;
	LoginFrag _contentFrag;
	private SharedPreferences _userPref;
	boolean _DisclaimerAccepted = false;
	boolean _defaultEobrIdentified = false;
	boolean _loginKMB;
	private boolean _ignoreOptionalUpdate = false;
	LoginHandler _loginHandler;
	ViewMode _viewMode;
	PackageManager _pm;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		_userPref = getSharedPreferences(
				this.getString(R.string.sharedpreferencefile), 0);

		_mobileDevice = new MobileDeviceHandler(this);

		setContentView(R.layout.baselayout);

		if (GlobalState.getInstance().getRunLockedDownAppStartup()) {
			// Add icon to status bar to indicate the app is running
			NotificationUtilities.UpdateAppRunningNotification(this,
					StartupActivity.class, "Application is running.");
			GlobalState.getInstance().setRunLockedDownAppStartup(false);
		}

		if (getIntent().getStringExtra(getString(R.string.msgconnectedto)) != null
				&& getIntent().getStringExtra(
				getString(R.string.msgconnectedto)).length() > 0)
			this.showMsg(getIntent().getStringExtra(
					getString(R.string.msgconnectedto)));

		if (getIntent().getBooleanExtra(getString(R.string.restartapp), false)) {
			mRestartAppTask = new RestartAppTask();
			mRestartAppTask.execute();
		} else {
			showEulaDialog(savedInstanceState);
		}
		_pm = this.getPackageManager();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// this prevents Login recreation on Configuration changes
		// (device orientation changes or hardware keyboard open/close).
		// just do nothing on these changes: Prevents "Leaked Window"
		// warning/error on Disclaimer.
		super.onConfigurationChanged(newConfig);

		_configChangedState = new Bundle();
		_configChangedState.putCharSequence(getResources().getString(R.string.state_username), _contentFrag.GetUsernameTextbox().getText());
		_configChangedState.putCharSequence(getResources().getString(R.string.state_password), _contentFrag.GetPasswordTextbox().getText());

		// we need to reload the login fragment on rotation change to adjust the layout
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		_contentFrag = new LoginFrag();
		transaction.replace(R.id.content_fragment, _contentFrag);
		transaction.addToBackStack(null);
		transaction.commitAllowingStateLoss();
	}

	ShowMessageClickListener onOkSubmitMessage = new ShowMessageClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int id) {
			_DisclaimerAccepted = true;

			// Call super.onClick to release screen orientation lock
			super.onClick(dialog, id);
		}
	};

	@Override
	protected void onResume() {
		super.onResume();

		loadControls();
	}

	@Override
	protected void loadControls() {
		super.loadControls();

		_userPref = getSharedPreferences(
				this.getString(R.string.sharedpreferencefile), 0);

		_multipleUsersEnabled = GlobalState.getInstance().getCompanyConfigSettings(this).getMultipleUsersAllowed();

		if (getMyController().getCurrentUser() != null)
		{
			// already a user logged in, this is the team driver login, or multi-user login process
			if (_multipleUsersEnabled)
			{
				_loginHandler = new MultipleUsersLoginHandler();
				GlobalState.getInstance().setIsMultipleUsersLogin(true);
				//Set Team Driver Mode to None
				GlobalState.getInstance().setTeamDriverMode(GlobalState.TeamDriverModeEnum.NONE);
			}
			else
			{
				_loginHandler = new TeamDriverLoginHandler();
				GlobalState.getInstance().setIsTeamLogin(true);
			}
			//No need to check for optional update
			_ignoreOptionalUpdate = true;
		}
		else
		{
			GlobalState.getInstance().setIsTeamLogin(false);
		}

		loadContentFragment(new LoginFrag());
	}

	@Override
	public void setFragments() {
		super.setFragments();

		Fragment f = getSupportFragmentManager().findFragmentById(
				R.id.content_fragment);
		_contentFrag = (LoginFrag) f;
	}

	protected LoginController getMyController() {
		return (LoginController) this.getController();
	}

	protected void InitController() {
		LoginController loginCtrl = new LoginController(this);

		this.setController(loginCtrl);

	}

	public void handleLoginSoloButtonClick() {
		GlobalState.getInstance().setIsViewOnlyMode(false);
		_loginHandler.setAsTeam(false);

		startAuthentication(true);
	}

	public void handleLoginTeamButtonClick() {
		GlobalState.getInstance().setIsViewOnlyMode(false);
		_loginHandler.setAsTeam(true);

		startAuthentication(true);
	}

	public void handleCancelButtonClick() {
		if (GlobalState.getInstance().getLoggedInUserList().size() == 0)
		{
			this.Return();
		}
		else
		{
			// Clear the menu in the process of returning to the RodsEntry activity.
			/* Display rodsentry activity */
			Bundle extras = !_multipleUsersEnabled ? new Bundle() : createExtras();

			this.startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_CLEAR_TOP, extras);
			this.finish();
		}
	}

	private Bundle createExtras()
	{
		Bundle extras = new Bundle();

		// if not team driver login, don't download logs and prompt for off duty logs on rods entry,
		// otherwise if team driver login, then download logs and prompt for off duty logs
		if (!getIntent().hasExtra(this.getString(R.string.extra_teamdriverlogin)) || !getIntent().hasExtra(this.getString(R.string.extra_multipleuserslogin)))
			extras.putBoolean(this.getString(R.string.extra_displayoffdutylogs), false);
		else
			extras.putBoolean(this.getString(R.string.extra_displayoffdutylogs), true);

		return extras;
	}

	public void handleLoginViewOnlyButtonClick() {
		GlobalState.getInstance().setIsViewOnlyMode(true);
		GlobalState.getInstance().setOffDutyMsgCloseBtnPressed(false);

		startAuthentication(false);
	}

	public void handleFeatureToggleButtonClick()
	{
		this.startActivity(FeatureToggle.class, Intent.FLAG_ACTIVITY_SINGLE_TOP);
	}

	public void startAuthentication(boolean loginKMB) {
		_username = _contentFrag.GetUsernameTextbox().getText().toString()
				.trim();
		_password = _contentFrag.GetPasswordTextbox().getText().toString()
				.trim();

		mAuthenticateTask = new AuthenticateTask(this.getClass()
				.getSimpleName(), loginKMB);
		mAuthenticateTask.execute();
	}

	@Override
	public void Return() {
		_loginHandler.Return();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.clear();
		if (this.getCurrentUser() != null) {
			if (GlobalState.getInstance().getFeatureService().getShowDebugFunctions())
				this.CreateOptionsMenu(menu, false);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// See if home button was pressed
		this.GoHome(item, this.getController());

		switch (item.getItemId()) {
			case 0:
				Bundle b = new Bundle();
				b.putBoolean("FromLogin", true);
				startActivity(Dashboard.class, b);
				break;
			default:
				super.onOptionsItemSelected(item);
				break;
		}

		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// Handle the back button
		if (keyCode == KeyEvent.KEYCODE_BACK) {

			// Say that we've consumed the event
			return true;
		}

		// Otherwise let system handle keypress normally
		return super.onKeyDown(keyCode, event);
	}

	private void handleUpdateClick(String msg, final AppUpdateController updateController) {
		final ShowMessageClickListener cancelClickListener = new ShowMessageClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				super.onClick(dialog, id);
				Login.this.UnlockScreenRotation();
				Return();
			}
		};
		Login.this.ShowConfirmationMessage(Login.this, msg,
				new ShowMessageClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						super.onClick(dialog, id);
						Login.this.UnlockScreenRotation();
						if (shouldShowUpdateRequiresWifiPrompt(updateController))
							showUpdateRequiresWifiPrompt(cancelClickListener);
						else
							startActivity(Updater.class, Intent.FLAG_ACTIVITY_SINGLE_TOP);
					}
				}, cancelClickListener);
	}

	private void handleOptionalUpdateClick(String msg, final AppUpdateController updateController) {
		final ShowMessageClickListener cancelClickListener = new ShowMessageClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				super.onClick(dialog, id);
				Login.this.UnlockScreenRotation();
				mAuthenticateTask = new AuthenticateTask(this.getClass().getSimpleName(), _loginKMB);
				mAuthenticateTask.execute();
			}
		};
		Login.this.ShowConfirmationMessage(Login.this, msg,
				new ShowMessageClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						super.onClick(dialog, id);
						Login.this.UnlockScreenRotation();
						//need this because the user is not yet logged in
						GlobalState.getInstance().setIsAutoUpdate(true);
						//Set back to false just in case the check hours button was clicked
						GlobalState.getInstance().setIsViewOnlyMode(false);
						if (shouldShowUpdateRequiresWifiPrompt(updateController))
							showUpdateRequiresWifiPrompt(cancelClickListener);
						else
							startActivity(Updater.class, Intent.FLAG_ACTIVITY_SINGLE_TOP);
					}
				}, cancelClickListener);
	}

	private boolean shouldShowUpdateRequiresWifiPrompt(AppUpdateController updateController)
	{
		return DeviceInfo.IsComplianceTablet() && !WifiStatus.isConnected(this) && updateController.isWifiRequired();
	}

	private void showUpdateRequiresWifiPrompt(ShowMessageClickListener cancelClickListener)
	{
		ShowConfirmationMessage(this, R.string.update_requires_wifi_title,
				getString(R.string.update_requires_wifi_message),
				R.string.wifi_settings,
				new ShowMessageClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int id)
					{
						super.onClick(dialog, id);
						Bundle extras = new Bundle();
						extras.putBoolean(WifiSettings.EXTRA_SHOW_UPDATER_WHEN_DONE, true);
						startActivity(WifiSettings.class, extras);
					}
				},
				R.string.cancellabel, cancelClickListener);
	}

	private static DownloadCompanyConfigAtLoginTask mDownloadCompanyConfigAtLoginTask;

	private class DownloadCompanyConfigAtLoginTask extends
			AsyncTask<Void, Void, Void> {


		@Override
		protected Void doInBackground(Void... params) {
			try {
				performAuthenticationOnLogin();
			} catch (Exception ex) {
				ErrorLogHelper.RecordMessage(String.format(
						BaseActivity.MSGASYNCDIALOGEXCEPTION, Login.class.toString(),
						this.getClass().getSimpleName()));
			}
			return null;
		}
	}

	private static DownloadFeatureTogglesTask mDownloadFeatureTogglesTask;
	private class DownloadFeatureTogglesTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			try {
				LoginController loginController = new LoginController(GlobalState.getContext());
				loginController.getFeatureToggleSettings();
			} catch (Exception ex) {
				ErrorLogHelper.RecordMessage(String.format(
						BaseActivity.MSGASYNCDIALOGEXCEPTION, Login.class.toString(),
						this.getClass().getSimpleName()));
			}
			return null;
		}

	}

	//3/21/14 MEC: Added a new enum to handle optional updates
	enum AuthenticateResultEnum {
		Null, IsAuthenticated, IsAppUpdateAvailable, IsAlreadyLoggedIn, IsAppOptionalUpdateAvailable
	}

	private static AuthenticateTask mAuthenticateTask;

	private class AuthenticateTask extends
			AsyncTask<Void, String, AuthenticateResultEnum> {
		ProgressDialog pd;
		Throwable ex;

		private AppUpdateController updateController;
		private Date startDateTime;
		private String _className;
		GlobalState gs = GlobalState.getInstance();

		public AuthenticateTask(String className, boolean loginKMB) {
			updateController = new AppUpdateController(gs);

			this._className = className;
			_loginKMB = loginKMB;

			if (!loginKMB) {
				_viewMode = new ReadOnlyViewMode();
			}
			else { // needed if the user hits check hours and has no logs, then clicks login
				_viewMode = new EditViewMode();
			}
		}

		protected void onPreExecute() {
			LockScreenRotation();
			showProgressDialog();

			mDownloadCompanyConfigAtLoginTask = new DownloadCompanyConfigAtLoginTask();
			mDownloadCompanyConfigAtLoginTask.execute();

			_loginHandler.onPreExecuteAuthenticateTask(startDateTime);
		}

		protected AuthenticateResultEnum doInBackground(Void... params) {
			AuthenticateResultEnum result = AuthenticateResultEnum.Null;

			publishProgress();

			try {
				// 7/7/11 JHM - Added because service/engine record polling was changing the user id on the log
				EobrReader.getInstance().SuspendReading();

				FmcsaEldInfoController fmcsaEldInfoController = new FmcsaEldInfoController(gs);
				fmcsaEldInfoController.SynchronizeFmcsaEldInfo();

				if (AppUpdateFactory.getInstance().areAppUpdateChecksEnabled() && updateController.getAppUpdateCheck().isAppUpdateAvailable(true)) {
					GlobalState.getInstance().setIsAutoUpdate(true);
					result = AuthenticateResultEnum.IsAppUpdateAvailable;
				} else if (AppUpdateFactory.getInstance().areAppUpdateChecksEnabled() && !_ignoreOptionalUpdate && DeviceInfo.IsAppSideloaded(_pm) && updateController.getAppUpdateCheck().isAppUpdateAvailable(false)){
					// Check for non-forced update
					result = AuthenticateResultEnum.IsAppOptionalUpdateAvailable;
					_ignoreOptionalUpdate = true;
				} else {
					Bundle bundle = getMyController().PerformLogin(_username, _password);

					if (bundle.getBoolean("isAuthenticated")) {
						result = AuthenticateResultEnum.IsAuthenticated;

						// 8/16/12 JHM - Moved code from onPostExecute because
						// to remove network access
						// from UI thread (ReverseGeocoding)
						// only do if logging into KMB... if just viewing hours
						// we can't modify the log
						_viewMode.doInBackgroundAuthenticateTask(startDateTime);

					} else if (bundle.getBoolean("isAlreadyLoggedIn"))
						result = AuthenticateResultEnum.IsAlreadyLoggedIn;
					else
						result = AuthenticateResultEnum.Null;
				}
			} catch (Exception e) {
				// I replaced the check for KmbApplicationException with the more generic Exception
				// because the "dummy" updater for the playStore flavor does not throw any
				// KmbApplicationException's. I think it's dumb that Java makes that an error and
				// not a warning or "suggestion."
				this.ex = e;
			} catch (Throwable t) {
				this.ex = t;
			}

			return result;
		}

		protected void onPostExecute(AuthenticateResultEnum result) {
			try {
				if (pd != null && pd.isShowing())
					pd.dismiss();
			} catch (Exception ex) {
				ErrorLogHelper.RecordMessage(String.format(
						BaseActivity.MSGASYNCDIALOGEXCEPTION, this._className,
						this.getClass().getSimpleName()));
			}

			if (result != AuthenticateResultEnum.IsAppUpdateAvailable)
				UnlockScreenRotation();

			mAuthenticateTask = null;

			if (ex != null) {
				if (ex.getClass() == KmbApplicationException.class)
					HandleException((KmbApplicationException) ex);
				else {
					ex.printStackTrace();
					ErrorLogHelper.RecordException(ex);

					ShowMessage(Login.this,
							getString(R.string.user_authentication_failed));
				}
			} else {
				switch (result) {
					case IsAuthenticated:
						SharedPreferences.Editor editor = _userPref.edit();

						editor.putString(getString(R.string.username), _username);
						editor.commit();

						_viewMode.SuccessfulAuthentication();
						break;
					case IsAppOptionalUpdateAvailable:
						// Notify the user that there is an optional update
						handleOptionalUpdateClick(getString(R.string.msg_update_available_optional), updateController);
						break;
					case IsAppUpdateAvailable:
						handleUpdateClick(getString(R.string.msg_update_available), updateController);
						break;
					case IsAlreadyLoggedIn:
						ShowMessage(Login.this,
								getString(R.string.user_already_logged_in));
						break;
					case Null:
					default:
						ShowMessage(Login.this,
								getString(R.string.user_authentication_failed));
						break;
				}
			}

			if (result != AuthenticateResultEnum.IsAuthenticated && result != AuthenticateResultEnum.IsAppOptionalUpdateAvailable) {
				// reset the view only flag
				GlobalState.getInstance().setIsViewOnlyMode(false);
			}

			//Attempt to download Feature Toggles from DMO
			mDownloadFeatureTogglesTask = new DownloadFeatureTogglesTask();
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				// In sequential processing all Async tasks run in a single thread and thus have to wait before the previous task ends.
				// If you need to execute code immediately, you need tasks to be processed in parallel in separate threads.
				mDownloadFeatureTogglesTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			}
			else {
				mDownloadFeatureTogglesTask.execute();
			}

			// Attempt to submit device information to DMO
			_mobileDevice.UploadMobileDeviceInfo();

			// 7/7/11 JHM - Added because service/engine record polling was
			// changing the user id on the log
			EobrReader.getInstance().ResumeReading();

		}

		// 9/29/11 JHM - Added public methods so that dialogs and context can be
		// re-established after an orientation change (ie. activity recreated).
		public void showProgressDialog() {
			if(!Login.this.isFinishing())
				pd = ProgressDialog.show(Login.this, "", Login.this.getResources()
						.getString(R.string.msgauthenticating));
		}
	}

	private void performAuthenticationOnLogin()
	{
		try
		{
			if(!GlobalState.getInstance().isNewActivation()) {
				LoginController loginController = new LoginController(this);
				loginController.DownloadCompanyConfigSettingsIntoGlobalState();
			}
		}
		catch (KmbApplicationException kae)
		{
			HandleException(kae);
		}
	}

	private void connectEobr(SharedPreferences userPref) {
		this.LockScreenRotation();
		String macAddress = userPref.getString(getString(R.string.macaddress),
				"");

		if (macAddress != null && macAddress.length() > 0) {
			_defaultEobrIdentified = true;

			// try to connect to a default EOBR
			mConnectDefaultEobrTask = new ConnectDefaultEobrTask();
			mConnectDefaultEobrTask.execute();
		} else {
			mConnectPairedDeviceTask = new ConnectPairedDeviceTask();
			mConnectPairedDeviceTask.execute();
		}
	}

	private ViewOnlyLoginTask mViewOnlyLoginTask;

	private class ViewOnlyLoginTask extends AsyncTask<Void, Void, Void> {
		ProgressDialog pd;

		@Override
		protected void onPreExecute() {
			LockScreenRotation();
		}

		@Override
		protected void onProgressUpdate(Void... unused) {
			if(!Login.this.isFinishing())
				pd = ProgressDialog.show(Login.this, "",
						getString(R.string.msgdownloadinglogdata));
		}

		@Override
		protected Void doInBackground(Void... params) {
			publishProgress();

			User user = GlobalState.getInstance().getCurrentUser();
			IAPIController empLogController = MandateObjectFactory.getInstance(Login.this,GlobalState.getInstance().getFeatureService()).getCurrentEventController();

			// attempt to download the latest logs from DMO
			try {
				empLogController.DownloadRecordsForCompliance(user, true);
			} catch (KmbApplicationException ex) {
				HandleException(ex);
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void unused) {
			if (pd != null && pd.isShowing())
				pd.dismiss();

			LogEntryController logEntryController = new LogEntryController(
					Login.this);

			// grab the most recent log - this will be local (unsubmitted) or
			// from DMO
			EmployeeLog currentLog = logEntryController.getCurrentEmployeeLog();

			if (currentLog == null) {
				// no log found - prompt the user that they can't continue
				ShowMessage(Login.this, getString(R.string.viewOnlyNoLogs));

				// authenticating actually signs in the user... need to sign
				// out.
				getMyController().PerformReadOnlyLogout();

				// set view only mode to false.  If they login or click check hours it will be reset accordingly.
				GlobalState.getInstance().setIsViewOnlyMode(false);
			} else {
				GlobalState.getInstance().setCurrentEmployeeLog(currentLog);

				Login.this.finish();

				if (getMyController().getIsUserRequiredToChangePassword()) {
					Intent intent = new Intent(Login.this, ChangePassword.class);
					intent.putExtra(Login.this
							.getString(R.string.extra_nextActivity), Login.this
							.getString(R.string.activity_rptGridImage));

					Login.this.startActivity(intent);
				} else {
					Intent intent = new Intent(Login.this,RptGridImage.class);
					intent.putExtra(Login.this.getString(R.string.state_keepdate), false);
					Login.this.startActivity(intent);
				}


			}

			UnlockScreenRotation();
		}
	}

	private RestartAppTask mRestartAppTask;

	private class RestartAppTask extends AsyncTask<Void, Void, Void> {
		ProgressDialog pd;
		BluetoothAdapter btAdapter;

		GlobalState gs = GlobalState.getInstance();

		protected void onPreExecute() {
			if(!Login.this.isFinishing())
				pd = ProgressDialog.show(Login.this, "",
						getString(R.string.devicediscovery));
			btAdapter = BluetoothAdapter.getDefaultAdapter();
		}

		protected Void doInBackground(Void... params) {
			Log.w("KMB Task","RestartAppTask");
			if (btAdapter != null && btAdapter.isEnabled()) {
				SharedPreferences userPref = getSharedPreferences(
						getString(R.string.sharedpreferencefile), 0);
				String macAddress = userPref.getString(
						getString(R.string.macaddress), "");

				// if a default eobr has been identified, try to connect to it
				if (macAddress != null && macAddress.length() > 0) {
					SystemStartupController startupCtrllr = new SystemStartupController(
							gs);
					startupCtrllr.PerformSystemStartup_EobrDevice(macAddress,
							btAdapter);
				}
			}

			IAPIController empLogCtrllr = MandateObjectFactory.getInstance(gs,GlobalState.getInstance().getFeatureService()).getCurrentEventController();
			empLogCtrllr.setAppRestartFlag(true);

			return null;
		}

		protected void onPostExecute(Void unused) {
			if (pd != null && pd.isShowing())
				pd.dismiss();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// Don't store state (likely blank values) if the FetchLocalData task hasn't completed.
		if (mFetchLocalDataTask == null) {
			outState.putBoolean(getString(R.string.state_disclaimeraccepted), _DisclaimerAccepted);
		}

		super.onSaveInstanceState(outState);
	}

	public User getCurrentUser() {
		return this.getMyController().getCurrentUser();
	}

	public Date getCurrentClockHomeTerminalTime() {
		return this.getMyController().getCurrentClockHomeTerminalTime();
	}

	public void handleConfigurationChangedState()
	{
		if (_configChangedState != null)
		{
			if (_configChangedState.containsKey(getResources().getString(R.string.state_username)))
			{
				CharSequence x = _configChangedState.getCharSequence(getResources().getString(R.string.state_username));
				_contentFrag.GetUsernameTextbox().setText(x);
			}

			if (_configChangedState.containsKey(getResources().getString(R.string.state_password)))
			{
				CharSequence x = _configChangedState.getCharSequence(getResources().getString(R.string.state_password));
				_contentFrag.GetPasswordTextbox().setText(x);
			}
			_configChangedState = null;
		}
	}

	private ConnectDefaultEobrTask mConnectDefaultEobrTask;

	private class ConnectDefaultEobrTask extends AsyncTask<Void, Void, Void> {
		ProgressDialog pd;
		BluetoothAdapter btAdapter;
		Bundle bundle;
		boolean btAlreadyOn;

		GlobalState gs = GlobalState.getInstance();


		protected void onPreExecute() {
			if(!Login.this.isFinishing())
				pd = ProgressDialog.show(Login.this, "",
						getString(R.string.devicediscovery));
			btAdapter = BluetoothAdapter.getDefaultAdapter();
		}

		protected Void doInBackground(Void... params) {
			Log.w("KMB Task", "ConnectDefaultEobrTask");
			if (btAdapter == null || !btAdapter.isEnabled()) {
				// BT is not on, request that it be enabled.
				btAlreadyOn = false;
				Intent enableIntent = new Intent(
						BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			} else {
				btAlreadyOn = true;

				SharedPreferences userPref = getSharedPreferences(
						getString(R.string.sharedpreferencefile), 0);
				String macAddress = userPref.getString(
						getString(R.string.macaddress), "");

				// if a default eobr has been identified, try to connect to it
				if (macAddress != null && macAddress.length() > 0) {
					SystemStartupController startupCtrllr = new SystemStartupController(
							gs);
					bundle = startupCtrllr.PerformSystemStartup_EobrDevice(
							macAddress, btAdapter);

					if (bundle != null) {
						String eobrDeviceName = bundle.getString(getString(R.string.devicename), "");
						if (eobrDeviceName.length() > 0) {
							// Call the REST API to get the License Plate Number for the Unit currently associated to the active EOBR
							RESTWebServiceHelper rwsh = new RESTWebServiceHelper(Login.this);
							String unitLicensePlateNumber = null;
							try {
								unitLicensePlateNumber = rwsh.GetEobrUnitLicensePlateNumber(EobrReader.getInstance().getEobrSerialNumber());
							} catch (IOException e) {
								e.printStackTrace();
							}
							EobrReader.getInstance().setUnitLicensePlateNumber(unitLicensePlateNumber);
						}
					}
				}
			}

			return null;
		}

		protected void onPostExecute(Void unused) {
			if (pd != null && pd.isShowing())
				pd.dismiss();

			// if BT wasn't on then we'll handle navigation
			// when the request to turn it on is returned
			if (btAlreadyOn)
				Login.this.handleDiscovery(bundle);
		}
	}

	private ConnectPairedDeviceTask mConnectPairedDeviceTask;

	public class ConnectPairedDeviceTask extends AsyncTask<Void, Void, Boolean> {
		private ProgressDialog pd;
		BluetoothAdapter btAdapter;
		boolean btAlreadyOn;
		Bundle bundle;

		public ConnectPairedDeviceTask() {
		}

		public void onPreExecute() {
			LockScreenRotation();
			if(!Login.this.isFinishing())
				pd = ProgressDialog.show(Login.this, "", getString(R.string.devicediscovery));
			btAdapter = BluetoothAdapter.getDefaultAdapter();
		}

		public Boolean doInBackground(Void... unused) {
			if (btAdapter == null || !btAdapter.isEnabled()) {
				// BT is not on, request that it be enabled.
				btAlreadyOn = false;
				Intent enableIntent = new Intent(
						BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			} else {
				btAlreadyOn = true;
				GlobalState gs = GlobalState.getInstance();
				SystemStartupController startupCtrllr = new SystemStartupController(
						gs);

				List<BluetoothDevice> btDevices = new ArrayList<BluetoothDevice>(
						btAdapter.getBondedDevices());

				if (btDevices != null && btDevices.size() > 0) {
					List<EobrDeviceDescriptor> pairedEobrDevices = startupCtrllr.PerformSystemStartup_PairedDevices(btDevices, btAdapter);
					if (pairedEobrDevices != null
							&& pairedEobrDevices.size() == 1) {
						try {
							startupCtrllr.ActivateEobrDevice(pairedEobrDevices
									.get(0).getName(), pairedEobrDevices.get(0)
									.getAddress(), pairedEobrDevices.get(0)
									.getCrc(), pairedEobrDevices.get(0)
									.getEobrGen());

							bundle = new Bundle();
							bundle.putString(Login.this.getString(R.string.devicename), pairedEobrDevices.get(0).getName());

							// Call the REST API to get the License Plate Number for the Unit currently associated to the active EOBR
							RESTWebServiceHelper rwsh = new RESTWebServiceHelper(Login.this);
							String unitLicensePlateNumber = null;
							try {
								unitLicensePlateNumber = rwsh.GetEobrUnitLicensePlateNumber(EobrReader.getInstance().getEobrSerialNumber());
							} catch (IOException e) {
								e.printStackTrace();
							}
							EobrReader.getInstance().setUnitLicensePlateNumber(unitLicensePlateNumber);
						} catch (KmbApplicationException kae) {
							// activation failed, set bundle to null which will
							// require discovery
							// from discovery screen
							bundle = null;
						}
					}
				}
			}

			return null;
		}

		public void onPostExecute(Boolean success) {
			if (pd != null && pd.isShowing())
				pd.dismiss();

			UnlockScreenRotation();

			// if BT wasn't on then we'll handle navigation
			// when the request to turn it on is returned
			if (btAlreadyOn) {
				Login.this.handleDiscovery(bundle);
			}
		}
	}

	private void handleDiscovery(Bundle bundle) {
		Intent intent;

		// discovery wasn't successful or there wasn't a default to begin with
		boolean unsuccessfulDiscovery = bundle == null
				|| !bundle.containsKey(Login.this
				.getString(R.string.devicename))
				|| bundle.getString(Login.this.getString(R.string.devicename)) == null
				|| bundle.getString(Login.this.getString(R.string.devicename))
				.equals("");

		if (unsuccessfulDiscovery) {
			if (getMyController().getIsUserRequiredToChangePassword()) {
				// need to change password, but indicate that we should go to
				// discovery after that
				intent = new Intent(Login.this, ChangePassword.class);
				intent.putExtra(this.getString(R.string.extra_nextActivity),
						this.getString(R.string.activity_discovery));
			} else {
				// couldn't connect to the default EOBR, go to discovery
				if(GlobalState.getInstance().getCompanyConfigSettings(this).getIsGeotabEnabled())
					intent = new Intent(Login.this, DeviceDiscoveryGeoTab.class);
				else
					intent = new Intent(Login.this, DeviceDiscovery.class);
			}
		} else {
			// show a toast that we've connected
			Login.this
					.showMsg(String.format(getString(R.string.msgconnectedto),
							bundle.getString(Login.this
									.getString(R.string.devicename))));

			if (getMyController().getIsUserRequiredToChangePassword()) {
				// need to change password, but indicate that we should go to
				// trip info after that
				intent = new Intent(Login.this, ChangePassword.class);

				if (getMyController().IsEobrConfigurationNeeded())
					intent.putExtra(
							this.getString(R.string.extra_nextActivity),
							this.getString(R.string.activity_eobrConfig));
				else {

					if(_loginHandler.getAsTeam() || (_multipleUsersEnabled && GlobalState.getInstance().getLoggedInUserList().size() > 1)) {
						intent.putExtra(
								this.getString(R.string.extra_nextActivity),
								this.getString(R.string.activity_teamDriverDeviceType));
					} else {
						if(GlobalState.getInstance().getCurrentUser().getIsMobileExemptLogAllowed())
						{
							intent.putExtra(
									this.getString(R.string.extra_nextActivity),
									this.getString(R.string.activity_exemptlogtype));
						}
						else {
							intent.putExtra(
									this.getString(R.string.extra_nextActivity),
									this.getString(R.string.activity_selectDutyStatus)); //PBI 49820
						}
					}
				}

			} else {
				if (getMyController().IsEobrConfigurationNeeded()) {
					intent = new Intent(Login.this, EobrConfig.class);
					intent.putExtra(getString(R.string.extra_displaytripinfo),
							true);
				} else {

					//if intending to join a team, go select the device type
					if(_loginHandler.getAsTeam()) {
						intent = new Intent(Login.this, TeamDriverDeviceType.class);
					} else {
						boolean isExemptLogEnabled = GlobalState.getInstance().getCurrentUser().getIsMobileExemptLogAllowed();
						boolean isExemptFromEldUse = GlobalState.getInstance().getCurrentUser().getExemptFromEldUse();
						boolean isELDMandateEnabled = GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled();

						if ((isExemptLogEnabled && GlobalState.getInstance().getCurrentUser().getRulesetTypeEnum().isUSFederalRuleset()) || (isExemptFromEldUse && isELDMandateEnabled))
						{
							intent = new Intent(Login.this, ExemptLogType.class);
						}
						//PBI 49820 - Go to SelectDutyStatus if on Mandate or AOBRD
						else {
							intent = new Intent(Login.this, SelectDutyStatus.class);
						}
					}
				}
			}
		}

		intent.putExtra(getString(R.string.extra_isloginprocess),  true);
		intent.putExtra(getString(R.string.extra_teamdriverlogin), _loginHandler.getAsTeam());

		finish();
		startActivity(intent);
	}

	private void showEulaDialog(Bundle savedInstanceState){
		SharedPreferences _userPref = this.getSharedPreferences(this.getString(R.string.sharedpreferencefile), 0);
		final boolean isEulaAgreementAccepted = _userPref.getBoolean(getString(R.string.pref_is_eula_agreement_accepted), false);
		final boolean isEulaAgreementAcceptanceUpToDate = _userPref.getString(getString(R.string.pref_last_kmb_version_with_accepted_eula_agreement), "").equals(GlobalState.getInstance().getPackageVersionName());

		if (savedInstanceState == null){
			if (!isEulaAgreementAccepted || !isEulaAgreementAcceptanceUpToDate){
				String message = FileUtility.InputStreamToString(getResources().openRawResource(R.raw.eula));
				CancelAcceptDialogFragment dialogFragment = CancelAcceptDialogFragment.newInstance(R.string.eula_mobile_agreement_title, message, R.string.cancellabel, R.string.accept, R.color.light_orange);

				dialogFragment.setOnDialogActionClickedListener(this);
				dialogFragment.show(getSupportFragmentManager(), EULA_DIALOG_TAG);
			}else{
				showSafetyMessage(null);
			}

		}else{
			if (!isEulaAgreementAccepted || !isEulaAgreementAcceptanceUpToDate){
				CancelAcceptDialogFragment dialogFragment = (CancelAcceptDialogFragment) getSupportFragmentManager().findFragmentByTag(EULA_DIALOG_TAG);

				dialogFragment.setOnDialogActionClickedListener(this);
			}else{
				showSafetyMessage(savedInstanceState);
			}
		}
	}

	private void showSafetyMessage(Bundle savedInstanceState){
		if (savedInstanceState == null)
			this.ShowMessage(this, 0, getString(R.string.lbldisclaimertext), onOkSubmitMessage);
		else if (!savedInstanceState.getBoolean(getString(R.string.state_disclaimeraccepted), _DisclaimerAccepted))
			this.ShowMessage(this, 0, getString(R.string.lbldisclaimertext), onOkSubmitMessage);
		else if (savedInstanceState.getBoolean(getString(R.string.state_disclaimeraccepted), _DisclaimerAccepted))
			this._DisclaimerAccepted = true;
	}

	//region CancelAcceptDialogFragment.OnDialogActionClickedListener

	@Override
	public void onDialogCancelClick() {
		handleCancelButtonClick();
	}

	@Override
	public void onDialogAcceptClick() {
		CancelAcceptDialogFragment dialogFragment = (CancelAcceptDialogFragment) getSupportFragmentManager().findFragmentByTag(EULA_DIALOG_TAG);
		SharedPreferences _userPref = this.getSharedPreferences(this.getString(R.string.sharedpreferencefile), 0);

		_userPref
				.edit()
				.putBoolean(getString(R.string.pref_is_eula_agreement_accepted), true)
				.putString(getString(R.string.pref_last_kmb_version_with_accepted_eula_agreement), GlobalState.getInstance().getPackageVersionName())
				.apply();
		dialogFragment.dismiss();

		showSafetyMessage(null);
	}

	//endregion

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case REQUEST_ENABLE_BT:
				// When the request to enable Bluetooth returns
				if (resultCode == Activity.RESULT_OK) {
					if (_defaultEobrIdentified) {
						mConnectDefaultEobrTask = new ConnectDefaultEobrTask();
						mConnectDefaultEobrTask.execute();
					} else {
						mConnectPairedDeviceTask = new ConnectPairedDeviceTask();
						mConnectPairedDeviceTask.execute();
					}
				} else {
					// User did not enable Bluetooth or an error occured
					this.showMsg(this.getString(R.string.bt_not_enabled));
					this.handleDiscovery(null);
				}
		}
		this.UnlockScreenRotation();
	}
}
