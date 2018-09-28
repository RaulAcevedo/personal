package com.jjkeller.kmb.share;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.jjkeller.kmb.CertifyLogs;
import com.jjkeller.kmb.DOTClocks;
import com.jjkeller.kmb.Dashboard;
import com.jjkeller.kmb.DataTransferFileStatusBroadcastListener;
import com.jjkeller.kmb.EditFuelPurchaseList;
import com.jjkeller.kmb.Login;
import com.jjkeller.kmb.Logout;
import com.jjkeller.kmb.RoadsideInspection;
import com.jjkeller.kmb.RoadsideInspectionDataTransfer;
import com.jjkeller.kmb.RodsEditLocation;
import com.jjkeller.kmb.RodsEditTime;
import com.jjkeller.kmb.RodsEntry;
import com.jjkeller.kmb.RodsNewStatus;
import com.jjkeller.kmb.RptMalfunctionAndDataDiagnostic;
import com.jjkeller.kmb.SubmitLogs;
import com.jjkeller.kmb.SupportContact;
import com.jjkeller.kmb.SwitchUser;
import com.jjkeller.kmb.SystemMenu;
import com.jjkeller.kmb.ViewLog;
import com.jjkeller.kmb.WifiSettings;
import com.jjkeller.kmb.firmware.FirmwareUpgradeBroadcastListener;
import com.jjkeller.kmb.firmware.IFirmwareUpdateProgressListener;
import com.jjkeller.kmb.fragments.DatePickerDialogFrag;
import com.jjkeller.kmb.fragments.LeftNavFrag;
import com.jjkeller.kmb.fragments.LeftNavFrag.ActivityMenuItemsListener;
import com.jjkeller.kmb.fragments.TimePickerDialogFrag;
import com.jjkeller.kmb.fragments.TimeWithSecondsPickerDialogFrag;
import com.jjkeller.kmb.interfaces.IBaseDialogFragment.BaseDialogFragmentActions;
import com.jjkeller.kmb.interfaces.IBaseFragment.BaseFragmentActions;
import com.jjkeller.kmbapi.common.JsonUtil;
import com.jjkeller.kmbapi.common.LogCat;
import com.jjkeller.kmbapi.common.TimeKeeper;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.ControllerFactory;
import com.jjkeller.kmbapi.controller.EOBR.EobrReader;
import com.jjkeller.kmbapi.controller.EmployeeLogEldMandateController;
import com.jjkeller.kmbapi.controller.GeotabController;
import com.jjkeller.kmbapi.controller.LogEntryController;
import com.jjkeller.kmbapi.controller.LoginController;
import com.jjkeller.kmbapi.controller.dataaccess.EmployeeLogFacade;
import com.jjkeller.kmbapi.controller.interfaces.IAPIController;
import com.jjkeller.kmbapi.controller.interfaces.ISpecialDrivingController;
import com.jjkeller.kmbapi.controller.share.ControllerBase;
import com.jjkeller.kmbapi.controller.share.KmbApplicationException;
import com.jjkeller.kmbapi.controller.share.MandateObjectFactory;
import com.jjkeller.kmbapi.controller.share.SpecialDrivingFactory;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.controller.utility.DeviceInfo;
import com.jjkeller.kmbapi.controller.utility.EmployeeLogUtilities;
import com.jjkeller.kmbapi.controller.utility.ErrorLogHelper;
import com.jjkeller.kmbapi.controller.utility.NetworkUtilities;
import com.jjkeller.kmbapi.employeelogeldevents.Enums;
import com.jjkeller.kmbapi.enums.DataDiagnosticEnum;
import com.jjkeller.kmbapi.enums.DataProfileEnum;
import com.jjkeller.kmbapi.enums.DutyStatusEnum;
import com.jjkeller.kmbapi.enums.EmployeeLogProvisionTypeEnum;
import com.jjkeller.kmbapi.enums.Malfunction;
import com.jjkeller.kmbapi.enums.RuleSetTypeEnum;
import com.jjkeller.kmbapi.kmbeobr.EventRecord;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;
import com.jjkeller.kmbapi.proxydata.Location;
import com.jjkeller.kmbui.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.jjkeller.kmbapi.controller.ControllerFactory.getInstance;

public abstract class BaseActivity extends SherlockFragmentActivity
		implements BaseFragmentActions, BaseDialogFragmentActions, LeftNavFrag.OnNavItemSelectedListener, LeftNavFrag.ActivityMenuItemsListener {

	private static final int None = 0;
	private static final int FIRST = 0;

	protected static final int DATE_DIALOG_ID = 0;
	protected Button mDateDialogButton = null;
	protected static final int TIME_DIALOG_ID = 1;
	protected Button mTimeDialogButton = null;

	protected static final int EXIT_ACTIVITY = 0;

	public static final String MSGASYNCDIALOGEXCEPTION = "AsyncTask DialogDismissException- Context: %s, Task: %s";

	protected static String _recentlyStartedActivityUri;

	protected static boolean _isDOTClocks_NightMode = false;

	private static boolean isOnBackground = false;
	private static boolean isFocused = false;
	private static boolean isBackPressed = false;

	private TextView _tvHeaderName;
	private TextView _tvHeaderDutyStatus;
	private TextView _tvHeaderLocation;
	private TextView _tvHeaderLogEventTS;

	private boolean _isCurrentDriver = true;

	// Used for handling highlighting the selected item in the leftnav
	private int _leftNavSelectedItem;
	private boolean _leftNavAllowChange = false;

	protected abstract void InitController();

	private Context context;

    private Menu menu;

	public Menu getMenu() {
		return menu;
	}

	protected FirmwareUpgradeBroadcastListener firmwareUpgradeBroadcastListener;
	public DataTransferFileStatusBroadcastListener dataTransferFileStatusBroadcastListener;

	private Object _controller = null;
	private IFirmwareUpdateProgressListener firmwareUpdateHandlerPersist;

	public ControllerBase getController()
	{
		if (_controller == null)
			this.InitController();

		return (ControllerBase)_controller;
	}

	public <T extends ControllerBase> T getController(Class<T> controllerType)
	{
		if (_controller == null)
			this.InitController();

		return (T)_controller;
	}

	public void setController(Object controller)
	{
		_controller = controller;
	}

	public static void ClearRecentlyStartedActivityUri() {
		_recentlyStartedActivityUri = null;
	}

	public static void ToggleDOTClocks_NightMode()
	{
		_isDOTClocks_NightMode = !_isDOTClocks_NightMode;
	}
	public static boolean getDOTClocks_NightMode()
	{
		return _isDOTClocks_NightMode;
	}



	//if a notification switches to a new activity it bypasses
	//our startActivity methods so we have to set the uri manually
	//currently only affects RODS entry
	protected void setRecentlyStartedActivityUrl(Class<?> c) {
		Intent intent = new Intent(this, c);

		_recentlyStartedActivityUri = intent.toUri(0);
	}

	private LeftNavFrag _leftNavFrag;
	public LeftNavFrag getLeftNavFragment()
	{
		return _leftNavFrag;
	}

	public void setLeftNavFragment(LeftNavFrag frag)
	{
		_leftNavFrag = frag;
	}

	public void CheckSaveTaskRetention(Object retained)
	{
		if ( retained instanceof SaveLocalDataTask )
		{
			mSaveLocalDataTask = (SaveLocalDataTask)retained;
			if(mSaveLocalDataTask.getStatus() == Status.RUNNING)
			{
				mSaveLocalDataTask.setContext(this);
				mSaveLocalDataTask.showProgressDialog();
			}
		}
	}

	private void onEnterForeground() {
		isOnBackground = false;
	}

	public void onEnterBackground(){
		//Check if the application lost focus
		if(!isFocused){
			isOnBackground = true;
			firmwareUpdateHandlerPersist = firmwareUpgradeBroadcastListener.getPersistedHandler();
		}
	}

	public Dialog onCreateDialog(int dialog)
	{
		AlertDialog alert = null;
		AlertDialog.Builder builder = null;

		// 2/20/2013 JEH: Used Ctrl+1 to convert to if/else because I changed KMBUI to a Library and it required this change.
		if (dialog == R.string.no_network_connection) {
			builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.no_network_connection);
		} else if (dialog == R.string.invalid_activationcode) {
			builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.invalid_activationcode);
		} else if (dialog == R.string.user_authentication_failed) {
			builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.user_authentication_failed);
		} else if (dialog == R.string.cantaddnewstatus) {
			builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.cantaddnewstatus);
		} else if (dialog == R.string.timeorstatusnotselected) {
			builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.timeorstatusnotselected);
		} else if (dialog == R.string.required_fields_missing) {
			builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.required_fields_missing);
		} else if (dialog == R.string.msgconfirmationpassworddoesnotmatch) {
			builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.msgconfirmationpassworddoesnotmatch);
		} else if (dialog == R.string.msgnetworkunavailabledecodefailed) {
			builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.msgnetworkunavailabledecodefailed);
		} else if (dialog == R.string.msg_empcode_missing) {
			builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.msg_empcode_missing);
		} else if (dialog == R.string.msg_empcode_displayname_required) {
			builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.msg_empcode_displayname_required);
		} else if (dialog == R.string.msg_close_application) {
			builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.msg_close_application);
		} else if (dialog == R.string.msg_fuelpurchase_amount_nan) {
			builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.msg_fuelpurchase_amount_nan);
		} else if (dialog == R.string.msg_fuelpurchase_amount_maxvalue) {
			builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.msg_fuelpurchase_amount_maxvalue);
		} else if (dialog == R.string.msg_fuelpurchase_statecode_invalid) {
			builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.msg_fuelpurchase_statecode_invalid);
		} else if (dialog == R.string.msg_fuelreceipt_price_nan) {
			builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.msg_fuelreceipt_price_nan);
		} else if (dialog == R.string.msg_fuelreceipt_price_maxvalue) {
			builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.msg_fuelreceipt_price_maxvalue);
		} else if (dialog == TIME_DIALOG_ID) {
			if(mTimeDialogButton != null)
			{
				Date valTimeDialog = TimeKeeper.getInstance().now();
				try {
					valTimeDialog = DateUtility.getHomeTerminalTime12HourFormat().parse(mTimeDialogButton.getText().toString());
				} catch (ParseException e) {
					// Allow dialog to display default time if parse exception occurs

					Log.e("UnhandledCatch", e.getMessage() + ": " + Log.getStackTraceString(e));
				}

				Calendar cal = Calendar.getInstance();
				cal.setTimeZone(DateUtility.getHomeTerminalDateFormat().getTimeZone());
				cal.setTime(valTimeDialog);

				return new TimePickerDialog(this,
						mTimeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false);
			}
		} else if (dialog == DATE_DIALOG_ID) {
			if(mDateDialogButton != null)
			{
				Calendar valDateDialog = Calendar.getInstance();
				try {
					valDateDialog.setTime(DateUtility.getDateFormat().parse(mDateDialogButton.getText().toString()));
				} catch (ParseException e) {
					// Allow dialog to display default time if parse exception occurs

					Log.e("UnhandledCatch", e.getMessage() + ": " + Log.getStackTraceString(e));
				}

				return new DatePickerDialog(this, mDateSetListener,
						valDateDialog.get(Calendar.YEAR), valDateDialog.get(Calendar.MONTH), valDateDialog.get(Calendar.DAY_OF_MONTH));
			}
		} else if (dialog == 10) {
			builder = new AlertDialog.Builder(this);
			builder.setMessage(this.getText(R.string.msgsampledata));
		} else if (dialog == R.string.msg_elddevicenotconnected) {
			builder = new AlertDialog.Builder(this);
			builder.setMessage(this.getString(R.string.msg_elddevicenotconnected));
		} else if (dialog == R.string.msg_eobrdevicenotconnected) {
			builder = new AlertDialog.Builder(this);
			builder.setMessage(this.getString(R.string.msg_eobrdevicenotconnected));
		} else if (dialog == R.string.msg_appsettings_not_loaded) {
			builder = new AlertDialog.Builder(this);
			builder.setMessage(this.getString(R.string.msg_appsettings_not_loaded));
		} else {
			builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.feature_not_implemented);
		}

		if (builder != null)
		{
			builder.setCancelable(false);
			builder.setNeutralButton(this.getString(R.string.oklabel), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});
			alert = builder.create();
		}

		return alert;
	}

	// the callback received when the user "sets" the time in the dialog
	private TimeWithSecondsPickerDialog.OnTimeWithSecondsSetListener mTimeWithSecondsSetListener =
			new TimeWithSecondsPickerDialog.OnTimeWithSecondsSetListener() {
				public void onTimeWithSecondsSet(TimeWithSecondsPicker view, int hourOfDay, int minute, int second) {
					Calendar cal = Calendar.getInstance();
					cal.setTimeZone(DateUtility.getHomeTerminalDateFormat().getTimeZone());
					cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
					cal.set(Calendar.MINUTE, minute);
					cal.set(Calendar.SECOND, second);
					updateTimeDisplay(cal);
				}
			};

	// the callback received when the user "sets" the time in the dialog
	private TimePickerDialog.OnTimeSetListener mTimeSetListener =
			new TimePickerDialog.OnTimeSetListener() {
				public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
					Calendar cal = Calendar.getInstance();
					cal.setTimeZone(DateUtility.getHomeTerminalDateFormat().getTimeZone());
					cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
					cal.set(Calendar.MINUTE, minute);
					updateTimeDisplay(cal);
				}
			};

	// the callback received when the user "sets" the date in the dialog
	private DatePickerDialog.OnDateSetListener mDateSetListener =
			new DatePickerDialog.OnDateSetListener() {

				public void onDateSet(DatePicker view, int year,
									  int month, int dayOfMonth) {
					Calendar cal = Calendar.getInstance();
					cal.set(Calendar.YEAR, year);
					cal.set(Calendar.MONTH, month);
					cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
					updateDateDisplay(cal);
				}
			};
	protected boolean _isYesValue;

	protected void updateTimeDisplay(Calendar c) {
		mTimeDialogButton.setText(DateUtility.getHomeTerminalTime12HourFormat().format(c.getTime()));
	}
	public void updateTimeDisplay(Button timeButton, Calendar c) {
		timeButton.setText(DateUtility.getHomeTerminalTime12HourFormat().format(c.getTime()));
	}

	public void updateTimeWithSecondsDisplay(Button timeButton, Calendar c) {
		timeButton.setText(DateUtility.getHomeTerminalTime12HourFormatWithSeconds().format(c.getTime()));
	}

	protected void updateDateDisplay(Calendar c) {
		mDateDialogButton.setText(DateUtility.getDateFormat().format(c.getTime()));
	}
	public void updateDateDisplay(Button dateButton, Calendar c) {
		dateButton.setText(DateUtility.getDateFormat().format(c.getTime()));
	}

	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Default menu for any Activity that inherits from BaseActivity.  This event
		// should be overriden in the Activity class if different behavior is Desired:
		// 
		// 1.  Override and do not call CreateOptionsMenu() if the Activity shouldn't have any menu.
		// 2.  Override and call CreateOptionsMenu() if the Activity should have a menu.
		// 	   A. Include Comma delimited string for specific Action items for the Activity.
		//	   B. Specify true/false on whether to display the System Menu.

		boolean addSystemMenu = true;
		this.CreateOptionsMenu(menu, addSystemMenu);
		return true;
	}

	public boolean CreateOptionsMenu(Menu menu, boolean addSystemMenu) {
		return this.CreateOptionsMenu(menu, addSystemMenu, false);
	}

	public boolean CreateOptionsMenu(Menu menu, boolean addSystemMenu, boolean isALKClass) {

		//if in view only mode, no menu
		if(GlobalState.getInstance().getIsViewOnlyMode())
			return true;

		_isCurrentDriver = this.IsTheDriver(GlobalState.getInstance().getCurrentUser());

		// Get the list of items to be displayed in the menu
		Activity activity = BaseActivity.this;
		ActivityMenuItemsListener menuItemsListener = (ActivityMenuItemsListener) activity;
		String mnuItems = "";
		if (menuItemsListener != null) {
			mnuItems = menuItemsListener.getActivityMenuItemList();
			if (mnuItems == null)
				mnuItems = "";

			// TODO: When the leftnav is gone, this can be removed if we do not add them in the RodsEntry getActivityMenuItemList()
			// Remove the System Menu/Exit from the list at this point
			mnuItems = mnuItems.replace("System Menu", "");
			mnuItems = mnuItems.replace("Log Off", "");
		}

		// Add the default items to the menu
		// If in Roadside Inspection, do not add some options
		if (!(GlobalState.getInstance().getRoadsideInspectionMode()) && getClass() != DOTClocks.class && !isALKClass)
			mnuItems = mnuItems + "," + this.getString(R.string.mnu_addsubmenu);

		// Add the ALK CoPilot Menu ONLY on the RODS screen IF the device is a compliance tablet, with ALK activated, and not in RSI mode
		if (!GlobalState.getInstance().getRoadsideInspectionMode() && getClass() == RodsEntry.class && IsComplianceTabletAndAlkEnabled())
			mnuItems = mnuItems + "," + this.getString(R.string.mnu_AlkCopilot);

		// Add the WiFi Settings item ONLY on the RODS screen IF the device is a compliance tablet and not in RSI mode
		if (!GlobalState.getInstance().getRoadsideInspectionMode() && getClass() == RodsEntry.class && DeviceInfo.IsComplianceTablet())
			mnuItems = mnuItems + "," + this.getString(R.string.mnu_wifi_settings);

		if (getClass() == RodsEntry.class && getClass() != DOTClocks.class &&
				GlobalState.getInstance().getCompanyConfigSettings(this).getMultipleUsersAllowed())
			mnuItems = mnuItems + "," + this.getString(R.string.mnu_addadditionalusers);

		if (getClass() != RodsEntry.class && getClass() != DOTClocks.class && !isALKClass)
			mnuItems = mnuItems + "," + this.getString(R.string.mnu_addsystemmenu);

        mnuItems = mnuItems + "," + this.getString(R.string.mnu_filemenu_contantus);

        if (mnuItems.length() > 0) {
			String [] menuItems = mnuItems.split(",");
			int menuCount = 0;
			for (int i = 0; i < menuItems.length; i++) {
				if (menuItems[i].trim().length() > 1) {
					menu.add(None, menuCount, FIRST, menuItems[i].trim());

					// If we're in roadside inspection mode, disable some options
					if (GlobalState.getInstance().getRoadsideInspectionMode() && getClass() == RodsEntry.class)
						menu.getItem(menuCount).setEnabled(false);

					// enable the actions only when the vehicle is at rest
					if (getClass() == RodsEntry.class) {
						LogEntryController ctrl = (LogEntryController) getController();

						boolean isVehicleInMotion = ctrl.IsVehicleInMotion();
						boolean isCurrentUserTheDriver = ctrl.IsCurrentUserTheDriver();

						if (menu.getItem(menuCount).getTitle().equals(getResources().getString(R.string.mnuNewStatus))) {
							// the 'New Status' function is turned on when the driver has manually extended the driving period
							if (!isVehicleInMotion)
								menu.getItem(menuCount).setEnabled(true);
							else if (isVehicleInMotion && isCurrentUserTheDriver == true)
								menu.getItem(menuCount).setEnabled(false);

						}
						else if (menu.getItem(menuCount).getTitle().equals(getResources().getString(R.string.lbldashboardtitle))) {
							boolean showDebug = GlobalState.getInstance().getFeatureService().getShowDebugFunctions();

							// Enable the Dashboard menu item if we are in debug mode AND the vehicle is in motion prior to Clocks screen being displayed.
							if (showDebug && isVehicleInMotion) {
								menu.getItem(menuCount).setEnabled(true);
							}
							else {
								menu.getItem(menuCount).setEnabled(false);
							}
						}
						else if (menu.getItem(menuCount).getTitle().equals(getResources().getString(R.string.mnuteamdriverswitch))) {
							boolean showTeamDriver = (GlobalState.getInstance().getLoggedInUserList().size() > 1);

							// Enable the Team Driver - Switch menu item if we are in "team driving" mode AND the vehicle is in motion prior to Rods
							// screen being displayed.
							if (showTeamDriver && isVehicleInMotion) {
								menu.getItem(menuCount).setEnabled(true);
							}
							else {
								menu.getItem(menuCount).setEnabled(false);
							}
						}
						else if (menu.getItem(menuCount).getTitle().equals(getResources().getString(R.string.mnuadditionaluserswitch))) {
							boolean hasMultipleUsers = (GlobalState.getInstance().getLoggedInUserList().size() > 1);

							// Enable the Additional Users - Switch menu item if we are in "team driving" mode AND the vehicle is in motion prior to Rods
							// screen being displayed.
							if (hasMultipleUsers && GlobalState.getInstance().getCompanyConfigSettings(this.getBaseContext()).getMultipleUsersAllowed()) {		// MUA explicitly declared
								menu.getItem(menuCount).setEnabled(true);
							}
							else {
								menu.getItem(menuCount).setEnabled(false);
							}
						}
						else {
							// Enable the actions only when the vehicle is at rest.  Disable item if current user is also the driver.
							if (isVehicleInMotion && isCurrentUserTheDriver == true)
								menu.getItem(menuCount).setEnabled(false);
						}
					}
					menuCount++;
				}
			}
		}

		// Add the System Menu ONLY if addSystemMenu is true AND the vehicle is NOT in motion.
		if (addSystemMenu)
		{
			MenuInflater inflater = getSupportMenuInflater();
			if( this.getClass() == RodsEntry.class) {
				inflater.inflate(R.menu.rodssystemmenu, menu);

				EmployeeLog currentlog = GlobalState.getInstance().getCurrentEmployeeLog();
				boolean isCurrentLogExemptFromELDUse = currentlog != null ? currentlog.getIsExemptFromELDUse() : false;

				if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled() && GlobalState.getInstance().getCurrentUser() != null && (GlobalState.getInstance().getCurrentUser().getExemptFromEldUse() || isCurrentLogExemptFromELDUse)) {
					// Exempt For ELD Use users do not need to Certify logs
					MenuItem certifyAndSubmit = menu.findItem(R.id.mnuCertityAndSubmit);
					if (certifyAndSubmit != null) {
						certifyAndSubmit.setTitle(R.string.lblsubmitlogstitle);
					}
				}
			}
			else
				inflater.inflate(R.menu.systemmenu, menu);
		}

		// Create the ActionBar Menu

		// Create the sub menu to put all item into
		// Needed to get the "MENU" text in the aciton bar
		SubMenu submenu = menu.addSubMenu(0, 999999999, 1, "Menu");


		// Add Roadside Inspection to the menu while in RSI
		if(GlobalState.getInstance().getRoadsideInspectionMode())
			submenu.add(0, R.string.mnu_filemenu_roadsideinspection, 0, R.string.mnu_filemenu_roadsideinspection);


		//Loop through the menu and add items under the MENU item, so that they come up when MENU is selected
		while (1 < menu.size()) {
			// Checking to see if the item already exists in the menu
			MenuItem checkMenu = submenu.findItem(menu.getItem(0).getItemId());
			if (checkMenu == null) {
				if ((GlobalState.getInstance().getRoadsideInspectionMode()) && menu.getItem(0).getTitle().toString().compareTo("Log Off") == 0) {
					//Skip this item, it is the Exit menu item
				} else if ((GlobalState.getInstance().getRoadsideInspectionMode()) && menu.getItem(0).getTitle().toString().compareTo("Certify and Submit") == 0) {
					// Don't show Certify and Submit while in roadside inspection mode
				} else if (GlobalState.getInstance().getCurrentUser() == null) {
					// Lockdown app restart current user is null
				} else if ((GlobalState.getInstance().getCurrentUser().getDataProfile().getValue() == DataProfileEnum.MINIMUMHOS || GlobalState.getInstance().getCurrentUser().getDataProfile().getValue() == DataProfileEnum.MINIMUMHOSWITHGPS)
						&& menu.getItem(0).getTitle().toString().compareTo("Enter Fuel Receipt") == 0) {
					//If the data profile is too low, remove the Fuel Receipt menu item
				} else if (menu.getItem(0).getTitle().toString().compareTo("Done") == 0) {
					//Ignore the "Done" menu options
				} else if(menu.getItem(0).getTitle().toString().compareTo("Cancel") == 0){
					//Ignore the "Cancel" menu options
				}else {
					submenu.add(0, menu.getItem(0).getItemId(), 0, menu.getItem(0).getTitle());
				}
			}
			menu.removeItem(menu.getItem(0).getItemId());
		}

		//Set the item to be displayed in the actionbar
		menu.getItem(0).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		// Apply All Visibility and Enable/Disable Rules
		if (getClass() == RodsEntry.class) {

			// Vehicle in motion Rule: check differs between Gen1 & Gen2
			boolean motion = false;
			if (EobrReader.getInstance().isEobrGen1())
				motion = GlobalState.getInstance().getVehicleMotionDetector(this).getIsVehicleInMotion();
			else
				motion = (GlobalState.getInstance().getPotentialDrivingStopTimestamp() == null);

			// Is Exempt from ELD Use Rule
			boolean isExemptFromELDUse = this.getIsExemptFromELDUse();
			boolean isMandateEnabled = GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled();

			for (int i = 0; i < submenu.size(); i++) {

				if (motion && _isCurrentDriver) {
					if (!submenu.getItem(i).getTitle().equals(getResources().getString(R.string.lbldashboardtitle))
							&& !submenu.getItem(i).getTitle().equals(getResources().getString(R.string.mnuteamdriverswitch))
							&& !submenu.getItem(i).getTitle().equals(getResources().getString(R.string.mnuadditionaluserswitch)))
						submenu.getItem(i).setEnabled(false);
				}

				if((isMandateEnabled || isExemptFromELDUse) &&
					(submenu.getItem(i).getTitle().equals(getResources().getString(R.string.mnuEditTime))
							|| submenu.getItem(i).getTitle().equals(getResources().getString(R.string.mnuEditLocation)))) {
					submenu.getItem(i).setVisible(false);
				}

				if (isExemptFromELDUse) {
					if (submenu.getItem(i).getTitle().equals(getResources().getString(R.string.mnuNewStatus))
							|| submenu.getItem(i).getTitle().equals(getResources().getString(R.string.mnuBorderCrossing)))
						submenu.getItem(i).setVisible(false);
				}

			}

		}

		// If there is no menu options, then don't show Menu
		if(submenu.size() <= 0) {
			menu.clear();
		}

		// If we're in roadside inspection mode, disable the Exit option
		if(GlobalState.getInstance().getRoadsideInspectionMode() && menu.findItem(R.id.mnuExit) != null)
			menu.findItem(R.id.mnuExit).setEnabled(false);

        getSupportMenuInflater().inflate(R.menu.malfunction_error_menu, menu);

        this.menu = menu;

        checkMalfunctionsAndDiagnostics();

		return true;
	}

	/**
	 * Returns true if the device is a compliance tablet and the ALK CoPilot feature toggle is enabled
	 * @return true if the device is a compliance tablet and the ALK CoPilot feature toggle is enabled, and false otherwise
	 */
	public boolean IsComplianceTabletAndAlkEnabled()
	{
		return DeviceInfo.IsComplianceTablet() && GlobalState.getInstance().getFeatureService().getAlkCopilotEnabled();
	}

	/**
	 * Returns true if the device can use ALK CoPilot.
	 * The device must be a compliance tablet,
	 * the ALK CoPilot feature toggle must be enabled,
	 * and ALK CoPilot must be activated by the company.
	 * @return true if the device can use ALK CoPilot, and false otherwise
	 */
	public boolean IsComplianceTabletAndAlkActivated()
	{
		return DeviceInfo.IsComplianceTablet()
				&& GlobalState.getInstance().getFeatureService().getAlkCopilotEnabled()
				&& GlobalState.getInstance().isAlkCoPilotActivated();
	}

	public boolean IsTheDriver(User user)
	{
		return GlobalState.getInstance().getIsCurrentUserTheDesignatedDriver();
	}

	public void showMsg(String msg)
	{
		Toast toast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER, toast.getXOffset() / 2, toast.getYOffset() / 2);
		toast.show();
	}

	@Override
	public void onBackPressed() {
		//Added to detect if back button was pressed outside the main screen
		if(!(this instanceof RodsEntry)) {
			isBackPressed = true;
		}
		super.onBackPressed();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {

		isFocused = hasFocus;
		//if back button was pressed but outside the main screen and the focus was lost the gain focus
		if (isBackPressed && !hasFocus) {
			isBackPressed = false;
			isFocused = true;
		}

		super.onWindowFocusChanged(hasFocus);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		//See if home button was pressed
		GoHome(item, this.getController());

		if (item != null)
		{
            String itemText = item.getTitle().toString();

			EmployeeLog currentlog = GlobalState.getInstance().getCurrentEmployeeLog();
			boolean isCurrentLogExemptFromELDUse = currentlog != null ? currentlog.getIsExemptFromELDUse() : false;

			if(item.getItemId() == R.id.menuMalfunctionButton || item.getItemId() == R.id.menuDataDiagnosticsButton){
                Collection<DataDiagnosticEnum> dataDiagnostics = ControllerFactory.getInstance().getEmployeeLogEldMandateController().getActiveDataDiagnostics(GlobalState.getInstance().getCurrentEmployeeLog());
                Collection<Malfunction> malfunctions = ControllerFactory.getInstance().getEmployeeLogEldMandateController().getActiveMalfunctions(GlobalState.getInstance().getCurrentEmployeeLog());

                Bundle malfunctionDiagnosticBundle = new Bundle();
                malfunctionDiagnosticBundle.putString(RptMalfunctionAndDataDiagnostic.DATA_DIAGNOSTIC_EVENTS, JsonUtil.getGson().toJson(dataDiagnostics));
                malfunctionDiagnosticBundle.putString(RptMalfunctionAndDataDiagnostic.COMPLIANCE_MALFUNCTIONS, JsonUtil.getGson().toJson(malfunctions));
                malfunctionDiagnosticBundle.putBoolean(RptMalfunctionAndDataDiagnostic.FROM_ALERT_BUTTON_PRESSED, true);

                startActivity(RptMalfunctionAndDataDiagnostic.class, malfunctionDiagnosticBundle);
            }

			else if (itemText.compareToIgnoreCase(this.getString(R.string.mnu_ViewLogs)) == 0) {
				// View Logs
				// 11/16/12 AMO: Adding these values to the EmployeeLogController
				// so that the date will be set for today's date by default.
				try {
					IAPIController empCon = MandateObjectFactory.getInstance(this,GlobalState.getInstance().getFeatureService()).getCurrentEventController();
					EmployeeLog empLog = GlobalState.getInstance().getCurrentEmployeeLog();
					if(empLog != null)
					{
						empCon.setSelectedLogForReport(empLog);
						startActivity(ViewLog.class);
					}
					else
						Toast.makeText(this, getString(R.string.msg_nocurrentlog), Toast.LENGTH_SHORT).show();
				} catch (Exception ex) {
					// There was no log for today
					Log.i("EmployeeLogs", "no log found for today's date: " + TimeKeeper.getInstance().now().toString());
				}
			} else if (itemText.compareToIgnoreCase(this.getString(R.string.mnu_RoadsideInspection)) == 0) {
				// Roadside Inspection
				if(GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()){
					this.startActivity(RoadsideInspectionDataTransfer.class);
				}
				else {
					this.startActivity(RoadsideInspection.class);
				}
			} else if (itemText.compareToIgnoreCase(this.getString(R.string.lbldashboardtitle)) == 0) {
				// Dashboard
				this.startActivity(Dashboard.class);
			} else if (itemText.compareToIgnoreCase(this.getString(R.string.mnu_FuelReceipt)) == 0) {
				// Fuel Receipt
				this.startActivity(EditFuelPurchaseList.class);
			} else if (itemText.compareToIgnoreCase(this.getString(R.string.mnu_VehicleInspection)) == 0) {
				// Vehicle Inspection
				Bundle extras = new Bundle();
				extras.putString(this.getString(R.string.title), this.getString(R.string.vehicleinspection));
				extras.putInt(this.getString(R.string.menu), R.string.mnu_sysmenu_vehicleinspection);
				this.startActivity(SystemMenu.class, extras);
			} else if (itemText.compareToIgnoreCase(this.getString(R.string.mnu_AlkCopilot)) == 0) {
				// CoPilot is not activated, so show a message
				ShowMessage(this, getString(R.string.alk_copilot_not_activated_title), getString(R.string.alk_copilot_not_activated_message));
			} else if (itemText.equalsIgnoreCase(getString(R.string.mnu_wifi_settings))) {
				// WiFi Settings
				this.startActivity(WifiSettings.class);
			}
			else if (itemText.equalsIgnoreCase(getString(R.string.mnu_addadditionalusers))){
				// Add Additional Users
				this.startActivity(Login.class);
			}
			else if (itemText.compareToIgnoreCase(this.getString(R.string.mnu_sysmenu)) == 0) {
				// System Menu
				loadSystemMenuActivity();
			} else if (itemText.compareToIgnoreCase(this.getString(R.string.mnu_filemenu_exit)) == 0) {
				// Exit
				this.startActivity(Logout.class);
			}
			else if (itemText.compareToIgnoreCase(this.getString(R.string.mnu_filemenu_certifyandsubmit)) == 0) {
				if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()){
					Bundle extras;
					extras = new Bundle();
					extras.putBoolean(this.getString(R.string.logout), true);
					this.startActivity(CertifyLogs.class, extras);
				}
				else{
					this.startActivity(SubmitLogs.class);
				}
			}
			else if (itemText.compareToIgnoreCase(this.getString(R.string.lblsubmitlogstitle)) == 0 && GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled() && GlobalState.getInstance().getCurrentUser() != null && (GlobalState.getInstance().getCurrentUser().getExemptFromEldUse() || isCurrentLogExemptFromELDUse)) {
				SubmitLogsWhenExemptForEldUse(false);
			}
			else if (itemText.equals(getString(R.string.mnu_filemenu_contantus))) {
			    this.startActivity(SupportContact.class);
            }
		}
		return true;
	}

	// 10/25/2012 AMO: Adding function for when the actionbar home button is pressed
	public void GoHome(MenuItem item, ControllerBase controller)
	{
		// if the item is the home button
		if(item.getItemId() == android.R.id.home){
			// if the app is passed the login
			if (controller != null && controller.getCurrentUser() != null && GlobalState.getInstance().getPassedRods() == true)
			{
				// finish activity and go to RODS
				if(getClass() != RodsEntry.class && getClass() != DOTClocks.class ){
					this.finish();
					this.startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
				}
			}
		}
	}

	protected void loadSystemMenuActivity(){
		Bundle extras = new Bundle();
		extras.putString(this.getString(R.string.title), this.getString(R.string.menu));
		extras.putInt(this.getString(R.string.menu), R.string.mnu_sysmenu);
		this.startActivity(SystemMenu.class, extras);
	}

	protected void loadFragment(int viewId, Fragment f)
	{
		if (!isFinishing())
		{
			View leftNavLayout = findViewById(viewId);
			if (leftNavLayout != null)
			{
				// Create new fragment and transaction
				FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

				// Replace fragment
				transaction.replace(viewId, f);

				// Commit the transaction
				transaction.commit();
			}
		}
	}

	protected void removeFragment(int viewId, Fragment f)
	{
		if (!isFinishing() && f != null)
		{
			View leftNavLayout = findViewById(viewId);
			if (leftNavLayout != null)
			{
				// Create new fragment and transaction
				FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

				// Replace fragment
				transaction.remove(f);

				// Commit the transaction
				transaction.commit();
			}
		}
	}

	protected void loadContentFragment(Fragment f)
	{
		loadContentFragment(f, false, false);
	}

	protected void loadContentFragment(Fragment f, boolean commitAllowingStateLoss)
	{
		loadContentFragment(f, commitAllowingStateLoss, false);
	}

	protected void loadContentFragment(Fragment f, boolean commitAllowingStateLoss, boolean forceReplaceFragment)
	{
		// Check if there is an existing fragment of this type.  If so, skip adding it 
		if(forceReplaceFragment || getSupportFragmentManager().findFragmentById(R.id.content_fragment) == null
				|| getSupportFragmentManager().findFragmentById(R.id.content_fragment).getClass() != f.getClass())
		{
			// Create new fragment and transaction
			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

			// Replace whatever is in the fragment_container view with this fragment
			transaction.replace(R.id.content_fragment, f, "Content");


			// Commit the transaction
			if (commitAllowingStateLoss)
				transaction.commitAllowingStateLoss();
			else
				transaction.commit();
		}
	}

	protected void loadLeftNavFragment()
	{
		View leftNavLayout = findViewById(R.id.leftnav_fragment);
		if(leftNavLayout != null)
		{
			// Create new fragment and transaction
			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
			_leftNavFrag = new LeftNavFrag();

			// Replace fragment
			transaction.replace(R.id.leftnav_fragment, _leftNavFrag);
			// Commit the transaction
			//This can throw an IllegalStateException if we are trying to commit changes after onSaveInstanceState
			try {
				transaction.commit();
			}
			catch (IllegalStateException ex) {
				//Create a new fragment and attempt to commit it again
				//Ensure the previous transaction is complete before attempting another transaction
				getSupportFragmentManager().executePendingTransactions();
				//Instantiate a new transaction, and add the leftNavFrag to that transaction
				FragmentTransaction innerTransaction = getSupportFragmentManager().beginTransaction();
				innerTransaction.replace(R.id.leftnav_fragment,_leftNavFrag);
				innerTransaction.commitAllowingStateLoss();
			}

			setLeftNavSelectionItems();
		}
	}

	/**
	 * Used for loading LeftNavFragment with specific itemLayoutResource for the Eula Activity
	 * @param itemLayoutResourceId Resource ID of a item layout of a listView
	 */
	protected void loadLeftNavFragment(int itemLayoutResourceId)
	{
		View leftNavLayout = findViewById(R.id.leftnav_fragment);
		if(leftNavLayout != null)
		{
			// Create new fragment and transaction with specific itemLayoutResource
			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
			_leftNavFrag = new LeftNavFrag(itemLayoutResourceId);

			// Replace fragment
			transaction.replace(R.id.leftnav_fragment, _leftNavFrag);
			// Commit the transaction
			//This can throw an IllegalStateException if we are trying to commit changes after onSaveInstanceState
			try {
				transaction.commit();
			}
			catch (IllegalStateException ex) {
				//Create a new fragment and attempt to commit it again
				//Ensure the previous transaction is complete before attempting another transaction
				getSupportFragmentManager().executePendingTransactions();
				//Instantiate a new transaction, and add the leftNavFrag to that transaction
				FragmentTransaction innerTransaction = getSupportFragmentManager().beginTransaction();
				innerTransaction.replace(R.id.leftnav_fragment,_leftNavFrag);
				innerTransaction.commitAllowingStateLoss();
			}

			setLeftNavSelectionItems();
		}
	}

	/**
	 * Used for handling highlighting the selected item in the leftnav
	 * Calling the functions within the LeftNavFrag to set needed values
	 */
	public void setLeftNavSelectionItems(){
		if (_leftNavSelectedItem >= 0)
			_leftNavFrag.setSelectedItem(_leftNavSelectedItem);
		_leftNavFrag.setAllowChange(_leftNavAllowChange);
	}

	/**
	 * Used for handling highlighting the selected item in the leftnav
	 * @param position - The index of the selected item
	 */
	public void setLeftNavSelectedItem(int position) {
		_leftNavSelectedItem = position;
	}

	public void leftNavHighlightSelectedItem(int position) {
		_leftNavFrag.highlightSelectedItem(position);
	}

	public void leftNavUnHighlightSelectedItem(int position) {
		_leftNavFrag.unHighlightSelectedItem(position);
	}

	/**
	 * Used for handling highlighting the selected item in the leftnav
	 * @param allow - Determines if the leftnav should be allowed to highlight the selected item
	 */
	public void setLeftNavAllowChange(boolean allow) {
		_leftNavAllowChange = allow;
	}

	public void BuildLeftNavMenu()
	{
		if(this.getLeftNavFragment() != null)
		{
			this.getLeftNavFragment().BuildMenu();
		}
	}

	protected boolean getIsExemptFromELDUse()
	{
		EmployeeLog currentlog = GlobalState.getInstance().getCurrentEmployeeLog();
        boolean isMandate = GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled();
		return currentlog != null && isMandate ? currentlog.getIsExemptFromELDUse() : false;
	}

	protected void loadDutyStatusHeaderControls()
	{
		// Determine whether we are in RoadsideInspectionMode or not (Some items should not be clickable)
		boolean roadsideInspection = GlobalState.getInstance().getRoadsideInspectionMode();
		boolean isExemptFromELDUse = this.getIsExemptFromELDUse();
		boolean isMandateEnabled = GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled();

		ImageView kmbimageadddriver = (ImageView) findViewById(R.id.kmbimageadddriver);
		if(kmbimageadddriver != null)
		{
			kmbimageadddriver.setVisibility(View.GONE);
			if(GlobalState.getInstance().getLoggedInUserList().size() < 2 && GlobalState.getInstance().getTeamDriverMode() == GlobalState.TeamDriverModeEnum.SHAREDDEVICE)
				kmbimageadddriver.setVisibility(View.VISIBLE);

			kmbimageadddriver.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {

					startActivity(com.jjkeller.kmb.Login.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
				}
			});
		}


		ImageView kmbimageteamdriver = (ImageView) findViewById(R.id.kmbimageteamdriver);
		if(kmbimageteamdriver != null)
		{
			kmbimageteamdriver.setVisibility(View.GONE);
			if(this.IsTheDriver(GlobalState.getInstance().getCurrentUser()) && ((GlobalState.getInstance().getTeamDriverMode() == GlobalState.TeamDriverModeEnum.SHAREDDEVICE)
				|| (GlobalState.getInstance().getCompanyConfigSettings(this).getMultipleUsersAllowed() && GlobalState.getInstance().getLoggedInUserList().size() > 1)))
				kmbimageteamdriver.setVisibility(View.VISIBLE);
		}

		ImageView imgrodsentryshareddevice = (ImageView) findViewById(R.id.imgrodsentryshareddevice);
		if(imgrodsentryshareddevice != null)
		{
			imgrodsentryshareddevice.setVisibility(View.GONE);
			if((GlobalState.getInstance().getTeamDriverMode() == GlobalState.TeamDriverModeEnum.SHAREDDEVICE || GlobalState.getInstance().getCompanyConfigSettings(this).getMultipleUsersAllowed())
					&& (GlobalState.getInstance().getLoggedInUserList().size() > 1))
				imgrodsentryshareddevice.setVisibility(View.VISIBLE);

			imgrodsentryshareddevice.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					LogEntryController ctrl = new LogEntryController(BaseActivity.this);
					boolean isVehicleInMotion = ctrl == null ? false : ctrl.IsVehicleInMotion();
					boolean isCurrentUserTheDriver = ctrl == null ? false : ctrl.IsCurrentUserTheDriver();

					if(!isVehicleInMotion || !isCurrentUserTheDriver)
					{
						startActivity(SwitchUser.class);
					}
				}
			});
		}

		ImageView imgrodsentryseparatedevice = (ImageView) findViewById(R.id.imgrodsentryseparatedevice);
		if(imgrodsentryseparatedevice != null)
		{
			imgrodsentryseparatedevice.setVisibility(View.GONE);
			if(GlobalState.getInstance().getTeamDriverMode() == GlobalState.TeamDriverModeEnum.SEPARATEDEVICE
					&& (GlobalState.getInstance().getLoggedInUserList().size() == 1))
				imgrodsentryseparatedevice.setVisibility(View.VISIBLE);

			imgrodsentryseparatedevice.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {

					startActivity(com.jjkeller.kmb.TeamDriverAddDriver.class);
				}
			});
		}

		_tvHeaderName = (TextView) findViewById(R.id.tvDSHName);
		if (_tvHeaderName != null) {
			if (!roadsideInspection && GlobalState.getInstance().getLoggedInUserList().size() > 1) {
				// Username clickable only in team driving scenarios (Perform Switch)
				_tvHeaderName.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						LogEntryController ctrl = new LogEntryController(BaseActivity.this);
						boolean isVehicleInMotion = ctrl == null ? false : ctrl.IsVehicleInMotion();
						boolean isCurrentUserTheDriver = ctrl == null ? false : ctrl.IsCurrentUserTheDriver();

						if(!isVehicleInMotion || !isCurrentUserTheDriver)
						{
							startActivity(SwitchUser.class);
						}
					}
				});
			} else {
				// Make sure nothing happens
				_tvHeaderName.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						// while in RSI don't do anything
					}
				});
			}
		}

		_tvHeaderDutyStatus = (TextView) findViewById(R.id.tvDSHDutyStatus);
		if (_tvHeaderDutyStatus != null && !roadsideInspection  && !isExemptFromELDUse) {
			_tvHeaderDutyStatus.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					LogEntryController ctrl = new LogEntryController(BaseActivity.this);
					boolean isVehicleInMotion = ctrl == null ? false : ctrl.IsVehicleInMotion();
					boolean isCurrentUserTheDriver = ctrl == null ? false : ctrl.IsCurrentUserTheDriver();

					if(!isVehicleInMotion || !isCurrentUserTheDriver)
					{
						startActivity(RodsNewStatus.class);
					}
				}
			});
		} else if (_tvHeaderDutyStatus != null) {
			_tvHeaderDutyStatus.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					// while in RSI don't do anything
				}
			});
		}

		_tvHeaderLocation = (TextView) findViewById(R.id.tvDSHLocation);

		if (_tvHeaderLocation != null && !roadsideInspection  && !isExemptFromELDUse && !isMandateEnabled) {
			_tvHeaderLocation.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					LogEntryController ctrl = new LogEntryController(BaseActivity.this);
					boolean isVehicleInMotion = ctrl == null ? false : ctrl.IsVehicleInMotion();
					boolean isCurrentUserTheDriver = ctrl == null ? false : ctrl.IsCurrentUserTheDriver();

					if(!isVehicleInMotion || !isCurrentUserTheDriver)
					{
						ctrl.setLogEventForEdit(EmployeeLogUtilities.GetLastEventInLog(ctrl.getCurrentEmployeeLog()));
						startActivity(RodsEditLocation.class, Intent.FLAG_ACTIVITY_SINGLE_TOP);
					}
				}
			});
		} else if (_tvHeaderLocation != null) {
			_tvHeaderLocation.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					// while in RSI don't do anything
				}
			});
		}

		_tvHeaderLogEventTS = (TextView) findViewById(R.id.tvDSHLogEventTimestamp);
		if (_tvHeaderLogEventTS != null && !roadsideInspection  && !isExemptFromELDUse && !isMandateEnabled) {
			_tvHeaderLogEventTS.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					LogEntryController ctrl = new LogEntryController(BaseActivity.this);
					boolean isVehicleInMotion = ctrl == null ? false : ctrl.IsVehicleInMotion();
					boolean isCurrentUserTheDriver = ctrl == null ? false : ctrl.IsCurrentUserTheDriver();

					if(!isVehicleInMotion || !isCurrentUserTheDriver)
					{
						startActivity(RodsEditTime.class);
					}
				}
			});
		} else if (_tvHeaderLogEventTS != null) {
			_tvHeaderLogEventTS.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					// while in RSI don't do anything
				}
			});
		}
	}

	public String getActivityMenuItemList(){return null;}
	public void onNavItemSelected(int menuItem) {}

	public void setFragments(){
		View leftNavLayout = findViewById(R.id.leftnav_fragment);
		if (leftNavLayout != null)
		{
			Fragment ln = getSupportFragmentManager().findFragmentById(R.id.leftnav_fragment);
			if(ln != null)
				this.setLeftNavFragment((LeftNavFrag)ln);
		}
	}

	protected void loadData(){}

	protected void loadControls(){
		loadTabletFragments();
	}
	protected void loadControls(Bundle savedInstanceState){
		loadTabletFragments();
	}
	private void loadTabletFragments()
	{
		loadLeftNavFragment();
		loadDutyStatusHeaderControls();
	}

	protected void findActivityControls(){}


	protected void handleLoadDataTaskFailure(){

		// FetchData task failed.  If current user is null, restart the app
		if (this.getController().getCurrentUser() == null) {
			this.finish();

			Intent loginIntent = new Intent(this, com.jjkeller.kmb.Login.class);
			loginIntent.putExtra(this.getString(R.string.restartapp), true);
			this.startActivity(loginIntent);
		}
	}

	// 9/29/11 JHM - Add static variable to store instance of FetchLocalData AsyncTask
	public static FetchLocalDataTask mFetchLocalDataTask;
	public class FetchLocalDataTask extends AsyncTask<Void, Void, Boolean>
	{
		private ProgressDialog progress;
		private String _className;
		private Bundle _savedInstanceState = null;
		private boolean _callLoadControlsWithInstanceState = false;
		private boolean _autoUnlockScreenRotationOnPostExecute = true;

		public FetchLocalDataTask(String className)
		{
			this._className = className;
			this.progress = CreateFetchDialog();
		}

		public FetchLocalDataTask(String className, Bundle savedInstanceState)
		{
			this._className = className;
			this.progress = CreateFetchDialog();
			this._savedInstanceState = savedInstanceState;
			this._callLoadControlsWithInstanceState = true;
		}

		public FetchLocalDataTask(String className, String fetchDialogMsg,
								  Bundle savedInstanceState)
		{
			this._className = className;
			this.progress = CreateFetchDialog(fetchDialogMsg);
			this._savedInstanceState = savedInstanceState;
			this._callLoadControlsWithInstanceState = true;
		}

		@Override
		public void onPreExecute()
		{
			LockScreenRotation();
			if (progress != null && !progress.isShowing())
				progress.show();
		}

		@Override
		public Boolean doInBackground(Void... unused)
		{
			try
			{
				loadData();
			}
			catch (Throwable e)
			{
				ErrorLogHelper.RecordException(BaseActivity.this, e);
				e.printStackTrace();
				return false;
			}

			return true;
		}

		@Override
		public void onPostExecute(Boolean success)
		{
			try
			{
				try
				{
					if (progress != null && progress.isShowing())
						progress.dismiss();
				}
				catch (Exception ex)
				{
					ErrorLogHelper.RecordMessage(String.format(BaseActivity.MSGASYNCDIALOGEXCEPTION, this._className, this.getClass().getSimpleName()));
					ex.printStackTrace();
				}

				if (!success)
				{
					handleLoadDataTaskFailure();
				}
				else
				{
					findActivityControls();
					if (this._callLoadControlsWithInstanceState)
						loadControls(this._savedInstanceState);
					else
						loadControls();
				}

				mFetchLocalDataTask = null;
			}
			catch (IllegalArgumentException e)
			{
				// nothing
				ErrorLogHelper.RecordException(BaseActivity.this, e);
				e.printStackTrace();
			}
			catch (Throwable e)
			{
				ErrorLogHelper.RecordException(BaseActivity.this, e);
				e.printStackTrace();
			}

			if(_autoUnlockScreenRotationOnPostExecute)
				UnlockScreenRotation();
		}

		// 9/29/11 JHM - Added public methods so that dialogs and context can be 
		// re-established after an orientation change (ie. activity recreated).
		public void showProgressDialog()
		{
			this.progress = CreateFetchDialog();
		}

		public void showProgressDialog(String fetchDialogMsg)
		{
			this.progress = CreateFetchDialog(fetchDialogMsg);
		}

		public void dismissProgressDialog()
		{
			DismissProgressDialog(this.getClass(), progress, _className);
		}

		public void setAutoUnlockScreenRotationOnPostExecute(boolean autoUnlock)
		{
			_autoUnlockScreenRotationOnPostExecute = autoUnlock;
		}
	}

	protected boolean saveData(){ return true; }
	protected void Return(boolean success){}
	protected void Return()	{
		Return(true);
	}


	// 9/29/11 JHM - Add static variable to store instance of SaveLocalData AsyncTask
	public static SaveLocalDataTask mSaveLocalDataTask;
	public class SaveLocalDataTask extends AsyncTask<Void, Void, Boolean> {
		private ProgressDialog progress;

		private String _className;
		private Context ctx;  // Added for consistency with Fetch task and future expansion

		public SaveLocalDataTask(String className) {
			this.progress = CreateSaveDialog();
			this._className = className;
		}

		public SaveLocalDataTask(String className, String saveDialogMsg) {
			this.progress = CreateSaveDialog(saveDialogMsg);
			this._className = className;
		}

		public void onPreExecute() {
			LockScreenRotation();
			if(progress != null && !progress.isShowing())
				progress.show();
		}

		public Boolean doInBackground(Void... unused) {
			boolean isSuccessful = false;
			try
			{
				isSuccessful = saveData();
			}
			catch(Throwable e) {
				ErrorLogHelper.RecordException(BaseActivity.this, e);
			}
			return isSuccessful;
		}

		public void onPostExecute(Boolean success) {
			try{
				dismissProgressDialog();

				mSaveLocalDataTask = null;
				Return(success);
				UnlockScreenRotation();
			}
			catch(Throwable e) {
				ErrorLogHelper.RecordException(BaseActivity.this, e);
			}
		}

		// 9/29/11 JHM - Added public methods so that dialogs and context can be 
		// re-established after an orientation change (ie. activity recreated).
		public void showProgressDialog()
		{
			this.progress = CreateSaveDialog();
		}

		public void showProgressDialog(String fetchDialogMsg)
		{
			this.progress = CreateSaveDialog(fetchDialogMsg);
		}

		public void dismissProgressDialog()
		{
			DismissProgressDialog(this.getClass(), progress, _className);
		}

		public void setContext(Context ctx)
		{
			this.ctx = ctx;
		}
	}

	protected ProgressDialog CreateFetchDialog(String message)
	{
		if(this.isFinishing())
			return null;
		else
			return ProgressDialog.show(this, "", message);
	}

	protected ProgressDialog CreateFetchDialog()
	{
		return this.CreateFetchDialog(this.getString(R.string.msgretreiving));
	}

	protected ProgressDialog CreateSaveDialog(String message)
	{
		if(this.isFinishing())
			return null;
		else
			return ProgressDialog.show(this, "", message);
	}

	protected ProgressDialog CreateSaveDialog()
	{
		return this.CreateFetchDialog(this.getString(R.string.msgsaving));
	}

	@SuppressWarnings("rawtypes")
	protected void DismissProgressDialog(Context ctx, Class asyncTaskClass, ProgressDialog pd)
	{
		try
		{
			if(pd != null && pd.isShowing()) pd.dismiss();
		}
		catch (Exception ex){
			ErrorLogHelper.RecordMessage(ctx, String.format(BaseActivity.MSGASYNCDIALOGEXCEPTION, ctx.getClass().getSimpleName(), asyncTaskClass.getSimpleName()));
		}
	}

	@SuppressWarnings("rawtypes")
	protected void DismissProgressDialog(Class asyncTaskClass, ProgressDialog pd, String className)
	{
		try
		{
			if(pd != null && pd.isShowing()) pd.dismiss();
		}
		catch (Exception ex){
			ErrorLogHelper.RecordMessage(String.format(BaseActivity.MSGASYNCDIALOGEXCEPTION, className, asyncTaskClass.getSimpleName()));
		}
	}

	public void startActivityWithoutUriTracking(Intent intent)
	{
		super.startActivity(intent);
	}

	@Override
	public void startActivity(Intent intent){
		String newActivityUri = intent.toUri(0);
		Boolean allowedToNavigate = false;

		if(_recentlyStartedActivityUri == null || _recentlyStartedActivityUri.compareTo(newActivityUri) != 0)
			// verify that the same activity that is currently up, is not being displayed again
			// this prevents the same screen from being displayed on top of itself
			allowedToNavigate = true;

		else if(intent.getComponent() != null &&
				intent.getComponent().getClassName() != null &&
				intent.getComponent().getClassName().compareTo(SystemMenu.class.getName()) == 0)
			// always allow navigation to the SystemMenu class, regardless of the circumstances
			allowedToNavigate = true;

		if(allowedToNavigate){
			_recentlyStartedActivityUri = newActivityUri;
			Log.v("BaseActivity", String.format("startActivity for URI %s", newActivityUri));
			super.startActivity(intent);
		}
		else{
			Log.v("BaseActivity", String.format("Ignored duplicate startActivity for URI %s", newActivityUri));
		}
	}

	protected void startActivity(String className, Integer intentFlag)
	{
		Intent intent = new Intent();
		intent.setClassName(getPackageName(), className);
		intent.setFlags(intentFlag);
		startActivity(intent);
	}

	protected void startActivity(String className, Integer intentFlag, Bundle extras)
	{
		Intent intent = new Intent();
		intent.setClassName(getPackageName(), className);
		intent.setFlags(intentFlag);
		intent.putExtras(extras);
		startActivity(intent);
	}

	protected void startActivity(Class<?> c)
	{
		Intent intent = new Intent(this, c);
		startActivity(intent);
	}

	public void startActivity(Class<?> c, Integer intentFlag)
	{
		Intent intent = new Intent(this, c);
		intent.setFlags(intentFlag);
		startActivity(intent);
	}

	protected void startActivity(Class<?> c, Integer intentFlag, Bundle extras)
	{
		Intent intent = new Intent(this, c);
		intent.setFlags(intentFlag);
		intent.putExtras(extras);
		startActivity(intent);
	}

	protected void startActivity(Class<?> c, Bundle extras)
	{
		Intent intent = new Intent(this, c);
		intent.putExtras(extras);
		startActivity(intent);
	}

	protected void startActivityForResult(Class<?> c, Integer requestCode)
	{
		Intent intent = new Intent(this, c);
		this.startActivityForResult(intent, requestCode);
	}

	protected void startActivityForResult(Class<?> c, Integer requestCode, Bundle extras)
	{
		Intent intent = new Intent(this, c);
		intent.putExtras(extras);
		this.startActivityForResult(intent, requestCode);
	}

	protected void startActivityForResult(Class<?> c, Integer requestCode, Integer intentFlag, Bundle extras)
	{
		Intent intent = new Intent(this, c);
		intent.setFlags(intentFlag);
		intent.putExtras(extras);
		this.startActivityForResult(intent, requestCode);
	}

	public void handleActivityStart(Class<?> c, Bundle extras)
	{
		if(extras != null)
			startActivity(c, extras);
		else
			startActivity(c);
	}

	/**
	 * Answer if the current activity is for the selected class
	 * @param c
	 * @return
	 */
	protected boolean IsCurrentActivity(Class<?> c){
		// note: using only the simple name of the class to locate in the
		//       URI of the current activity might not be the perfect solution to knowing
		//       if the activity matches.   If the simple name is something like "EditLocation", 
		//       but the URI of the activity is for a class like "EditLocationForDriving", then this
		//       will arbitrarily appear to be match 
		String className = c.getSimpleName();
		return _recentlyStartedActivityUri != null && _recentlyStartedActivityUri.indexOf(className) > 0;
	}

	public void HandleException(KmbApplicationException kae)
	{
		ErrorLogHelper.RecordException(this, kae);

		try{
			this.ShowMessage(this, kae.getCaption(), kae.getDisplayMessage());
		}
		catch(Throwable e){
			// ignore exceptions when trying to display an exception
			ErrorLogHelper.RecordException(this, e);
		}
	}

	/**
	 * Translate input string from string in input resource array to equvalent location in output resource array
	 * @param input
	 * @param inputId
	 * @param outputId
	 * @return String
	 */
	protected String TranslateStringArrays(String input, int inputId, int outputId) {
		String output = input;
		String [] inputList = this.getResources().getStringArray(inputId);
		String [] outputList = this.getResources().getStringArray(outputId);

		for(int i=0; i < inputList.length; i++)
		{
			if(inputList[i].equalsIgnoreCase(input))
			{
				output = outputList[i];
				break;
			}
		}

		return output;
	}

	// 12/27/11 SJN - Store the current dialog message so we can try to tell if a dialog is up,
	// and we can try to prevent repeated alerts from displaying
	private ArrayList<DialogMap> _dialogList = new ArrayList<DialogMap>();

	/**
	 * This method will determine if an alert is currently displayed via use of the ShowMessage method.
	 * Does not apply messages show via ShowConfirmationMessage. 
	 */
	public boolean IsAlertMessageShowing()
	{
		return _dialogList != null && !_dialogList.isEmpty();
	}

	/**
	 * Cause each alert message to be shown on the screen.  This is to fix a problem where occassionally
	 * the alert messages gets displayed underneath the running application.  This will cause all
	 * alert messages to be displayed in the forefront, and be visible to the user.
	 */
	public void DisplayAnyAlertMessages()
	{
		if(_dialogList != null && !_dialogList.isEmpty())
			for(int i=0; i<_dialogList.size(); i++){
				DialogMap dlgMap = _dialogList.get(i);
				dlgMap.show();
			}
	}

	/**
	 * Show the message on the screen with an OK button.
	 * @param ctx - context to display the dialog
	 * @param titleId - resourceId of the title of the dialog
	 * @param message - message to display
	 * @param onOkHandler - handler for when the ok button is clicked
	 */
	public void ShowMessage(Context ctx, int titleId, String message, ShowMessageClickListener onOkHandler) {
		String title = null;
		if(titleId > 0) title = this.getString(titleId);
		this.ShowMessage(ctx, title, message, onOkHandler);
	}
	public void ShowMessage(Context ctx, String title, String message, ShowMessageClickListener onOkHandler)
	{
		// 12/22/11 JHM - Don't show message if it is already being displayed
		if(!this.ContainsAlertMessage(message))
		{
			LockScreenRotation();
			AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
			if(title != null && !title.equalsIgnoreCase("")) builder.setTitle(title);
			builder.setMessage(message)
					.setCancelable(false)
					.setPositiveButton(this.getString(R.string.oklabel), onOkHandler == null ? new ShowMessageClickListener() : onOkHandler);
			DialogInterface dlg = builder.show();
			// 12/22/11 JHM - When dialog shown, store message
			BaseActivity.this.AddAlertMessageFor(message, dlg);
		}
	}

	public void ShowMessage(Context ctx, String message) {
		this.ShowMessage(ctx, -1, message, null);
	}

	public void ShowMessage(Context ctx, String title, String message) {
		this.ShowMessage(ctx, title, message, null);
	}

	public DialogInterface ShowMessageNonDismissable(Context ctx, String title, String message, String okButtonLabel, ShowMessageClickListener onOkHandler)
	{
		DialogInterface dlg = null;

		if(!this.ContainsAlertMessage(message))
		{
			LockScreenRotation();
			AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
			if(title != null && !title.equalsIgnoreCase("")) builder.setTitle(title);
			builder.setMessage(message)
					.setPositiveButton(okButtonLabel, onOkHandler == null ? new ShowMessageClickListener() : onOkHandler)
					.setCancelable(false);
			dlg = builder.show();
			BaseActivity.this.AddAlertMessageFor(message, dlg);
		}

		return dlg;
	}

	public DialogInterface ShowMessageNonDismissable(Context ctx, String title, String message)
	{
		DialogInterface dlg = null;

		if(!this.ContainsAlertMessage(message))
		{
			LockScreenRotation();
			AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
			if(title != null && !title.equalsIgnoreCase("")) builder.setTitle(title);
			builder.setMessage(message)
					.setCancelable(false);
			dlg = builder.show();
			BaseActivity.this.AddAlertMessageFor(message, dlg);
		}

		return dlg;
	}

	public void ShowDialog(Context ctx, String title, String message, View dialogView, String okButtonLabel, ShowMessageClickListener onOkHandler)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
		if (title != null && title.length() > 0)
			builder.setTitle(title);
		builder.setView(dialogView)
				.setPositiveButton(okButtonLabel, onOkHandler == null ? new ShowMessageClickListener() : onOkHandler)
				.setNegativeButton(R.string.cancellabel, new ShowMessageClickListener());
		DialogInterface dlg = builder.show();
		BaseActivity.this.AddAlertMessageFor(message, dlg);
	}

	/**
	 * Shows a dialog and adds it to the dialog list with the given message as an identifier
	 * @param ctx The context in which to show the dialog
	 * @param message The message, which is just an ID in this case
	 * @param dialog The dialog to show
	 */
	public void ShowDialog(Context ctx, String message, Dialog dialog)
	{
		dialog.show();
		BaseActivity.this.AddAlertMessageFor(message, dialog);
	}

	/**
	 * A default click handler for dialog buttons.  Will do unlocking of the screen.
	 *
	 * <br><br><pre>
	 * Example:
	 * new ShowMessageClickListener() {
	 *     @Override
	 *     public void onClick(DialogInterface dialog, int id) {
	 *       &lt;insert code here&gt;
	 *
	 *       // Call super.onClick to release screen orientation lock
	 *       super.onClick(dialog, id);
	 *     }
	 *  }
	 *	</pre>
	 */
	public class ShowMessageClickListener implements DialogInterface.OnClickListener
	{
		public void onClick(DialogInterface dialog, int id) {
			// 12/22/11 JHM - When dialog dismissed, clear message
			BaseActivity.this.RemoveAlertMessageFor(dialog);
			if(_dialogList.size() == 0)
				BaseActivity.this.UnlockScreenRotation();
		}
	}

	/**
	 * Answer if there is an alert message already displayed for the given message text
	 * @param message
	 * @return
	 */
	public boolean ContainsAlertMessage(String message)
	{
		boolean isFound = false;
		if(_dialogList != null && !_dialogList.isEmpty())
			for(int i=0; i<_dialogList.size(); i++){
				DialogMap dlgMap = _dialogList.get(i);
				if(dlgMap.equals(message)) {
					isFound = true;
					break;
				}
			}

		return isFound;
	}

	/**
	 * Add an alert message for the dialog and message text combination
	 * @param message
	 * @param dialog
	 */
	private void AddAlertMessageFor(String message, DialogInterface dialog)
	{
		if (_dialogList == null)
			_dialogList = new ArrayList<DialogMap>();

		boolean isFound = false;
		if (_dialogList != null && !_dialogList.isEmpty()) {
			for (int i = 0; i < _dialogList.size(); i++) {
				DialogMap dlgMap = _dialogList.get(i);
				if(dlgMap.equals(message)) {
					isFound = true;
					break;
				}
			}
		}

		if (!isFound)
			_dialogList.add(new DialogMap(message, dialog));
	}

	/**
	 * Remove the dialog from the list of managed alert dialogs
	 * @param dialog
	 */
	protected void RemoveAlertMessageFor(DialogInterface dialog)
	{
		if (_dialogList == null || _dialogList.isEmpty())
			return;

		for (int i = 0; i < _dialogList.size(); i++) {
			DialogMap dlgMap = _dialogList.get(i);
			if (dlgMap.equals(dialog)) {
				_dialogList.remove(i);
				break;
			}
		}
	}

	/**
	 * Dismiss and remove the dialog from the list of managed alert dialogs
	 * @param message The message in the dialog
	 */
	public void DismissAlertMessageFor(String message)
	{
		if (_dialogList == null || _dialogList.isEmpty())
			return;

		for (int i = 0; i < _dialogList.size(); i++) {
			DialogMap dlgMap = _dialogList.get(i);
			if (dlgMap.equals(message)) {
				dlgMap.getDialog().dismiss();
				_dialogList.remove(i);
				break;
			}
		}
	}

	/**
	 * Show the message on the screen with an OK button. Be sure to unlock screen in the onClick handlers.
	 * @param ctx - context to display the dialog
	 * @param titleId - resourceId of the title of the dialog
	 * @param message - message to display
	 * @param onYesHandler - handler for when the Yes button is clicked
	 * @param onNoHandler - handler for when the No button is clicked
	 */
	public void ShowConfirmationMessage(Context ctx, int titleId, String message, int yesId, DialogInterface.OnClickListener onYesHandler, int noId, DialogInterface.OnClickListener onNoHandler) {
		if (ctx != null && !isFinishing()) {    // don't show dialog if the Activity is being destroyed or context no longer exists [i.e. called after execution of background thread] -- avoids error "BinderProxy@45d459c0 is not valid; is your activity running?"
			LockScreenRotation();
			AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
			if (titleId > 0) builder.setTitle(titleId);
			builder.setMessage(message)
					.setCancelable(false)
					.setPositiveButton(this.getString(yesId), onYesHandler)
					.setNegativeButton(this.getString(noId), onNoHandler);
			DialogInterface dlg = builder.show();
			BaseActivity.this.AddAlertMessageFor(message, dlg);
		}
	}

	/**
	 * Show the message on the screen with an OK button. Be sure to unlock screen in the onClick handlers.
	 * @param ctx - context to display the dialog
	 * @param titleId - resourceId of the title of the dialog
	 * @param message - message to display
	 * @param onYesHandler - handler for when the ok button is clicked
	 * @param onNoHandler - handler for when the ok button is clicked
	 */
	public void ShowConfirmationMessage(Context ctx, int titleId, String message, DialogInterface.OnClickListener onYesHandler, DialogInterface.OnClickListener onNoHandler) {
		this.ShowConfirmationMessage(ctx, -1, message, R.string.btnyes, onYesHandler, R.string.btnno, onNoHandler);
	}

	/**
	 * Show the message on the screen with an OK button.  Be sure to unlock screen in the onClick handlers.
	 * @param ctx - context to display the dialog
	 * @param message - message to display
	 * @param onYesHandler - handler for when the ok button is clicked
	 * @param onNoHandler - handler for when the ok button is clicked
	 */
	public void ShowConfirmationMessage(Context ctx, String message, DialogInterface.OnClickListener onYesHandler, DialogInterface.OnClickListener onNoHandler) {
		this.ShowConfirmationMessage(ctx, -1, message, onYesHandler, onNoHandler);
	}

	public void ShowConfirmationWithCancelMessage(Context ctx, String message, DialogInterface.OnClickListener onOkHandler, DialogInterface.OnClickListener onCancelHandler) {
		this.ShowConfirmationMessage(ctx, -1, message, R.string.btnok, onOkHandler, R.string.btncancel, onCancelHandler);
	}

	public void ShowConfirmationWithCancelMessage(Context ctx, int titleId, String message, DialogInterface.OnClickListener onOkHandler, DialogInterface.OnClickListener onCancelHandler) {
		this.ShowConfirmationMessage(ctx, titleId, message, R.string.btnok, onOkHandler, R.string.btncancel, onCancelHandler);
	}

	protected void ShowTimePickerDialog(Button btn)
	{
		TimePickerDialogFrag diag = new TimePickerDialogFrag(btn);
		diag.setCancelable(false);
		diag.show(getSupportFragmentManager(), "time");
	}

	protected void ShowTimeWithSecondsPickerDialog(Button btn)
	{
		TimeWithSecondsPickerDialogFrag diag = new TimeWithSecondsPickerDialogFrag(btn);
		diag.setCancelable(false);
		diag.show(getSupportFragmentManager(), "time_with_seconds");
	}

	protected void ShowDatePickerDialog(Button btn)
	{
		DatePickerDialogFrag diag = new DatePickerDialogFrag(btn);
		diag.setCancelable(false);
		diag.show(getSupportFragmentManager(), "date");
	}

	@Deprecated
	/**
	 * Note: this should be replaced by ShowConfirmationMessage as the preferred technique
	 */
	protected AlertDialog CreateYesNoMessage(Context ctx, String msg) {
		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
		builder.setMessage(msg)
				.setCancelable(false)
				.setPositiveButton(this.getString(R.string.btnyes), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						handleYesButtonClick();
					}
				})
				.setNegativeButton(this.getString(R.string.btnno), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						handleNoButtonClick();
					}
				});
		AlertDialog alert = builder.create();
		return alert;
	}

	@Deprecated
	protected void handleNoButtonClick() {_isYesValue = false;}

	@Deprecated
	protected void handleYesButtonClick() {_isYesValue = true;}

	// Sets screen rotation as fixed to current rotation setting
	public void LockScreenRotation()
	{
		//* Froyo code is included for completeness. Our app does not support Android Froyo (API Level 8).
		// Source: http://stackoverflow.com/questions/6599770/screen-orientation-lock
		switch (getResources().getConfiguration().orientation){
			default: Log.e("Unknown Orientation", "An unknown screen orientation was detected while attempting to lock the screen rotation. Handling as portrait mode...");
				//Fallthrough and handle as if Portrait mode was detected. This is to prevent errors that might occur if the screen isn't locked.
			case Configuration.ORIENTATION_PORTRAIT:
				if(android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.FROYO){
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //Froyo and below do not support reversed orientations
				} else {
					//the following method is an error on froyo and lower. The code above prevents this from being called from an incompatible OS version so it's not a problem.
					int rotation = getWindowManager().getDefaultDisplay().getRotation();
					if(rotation == android.view.Surface.ROTATION_90 || rotation == android.view.Surface.ROTATION_180){
						setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
					} else {
						setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
					}
				}
				break;

			case Configuration.ORIENTATION_LANDSCAPE:
				if(android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.FROYO){
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); //Froyo and below do not support reversed orientations
				} else {
					int rotation = getWindowManager().getDefaultDisplay().getRotation();
					if(rotation == android.view.Surface.ROTATION_0 || rotation == android.view.Surface.ROTATION_90){
						setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
					} else {
						setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
					}
				}
				break;
		}
	}


	protected void sendHideDialogMessage(){
		EventBus.getDefault().postSticky(BroadcastDialogMessage.buildHideDialogMessage());
	}

	protected void sendShowDialogMessage(String message){
		EventBus.getDefault().postSticky(BroadcastDialogMessage.buildShowDialogMessage(message));
	}

	protected void sendShowDialogMessage(int messageId){
		EventBus.getDefault().postSticky(BroadcastDialogMessage.buildShowDialogMessage(messageId));
	}



	public void UnlockScreenRotation()
	{
		// allow screen rotations again
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		context = this;

        LogCat.getInstance().i("ActivityChange", String.format(Locale.US, "Started Activity: %s", this.getLocalClassName()));

		// Set ActionBar text
		ActionBar actionBar = getSupportActionBar();

		if (actionBar != null)
		{
			if(GlobalState.getInstance().getIsViewOnlyMode())
			{
				actionBar.setTitle(R.string.actionbar_title_viewonly);

				//set the actionbar background color to the orange from encompass
				//to make it more obvious to the user that they're in view-only mode
				actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#F68026")));

				actionBar.setHomeButtonEnabled(false);
			}
			else
			{
				actionBar.setTitle(R.string.actionbar_title);
				actionBar.setHomeButtonEnabled(true);
			}
		}

		// 1/20/12 JHM - Keep the screen alive while this activity is in the foreground
		if (GlobalState.getInstance().getAppSettings(this).getKeepScreenOn())
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		// Setup default error handler
		if (Thread.getDefaultUncaughtExceptionHandler() == null || !(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomGlobalExceptionHandler) )
		{
			Thread.setDefaultUncaughtExceptionHandler(new CustomGlobalExceptionHandler(this));
		}

		// load the saved instance state from the bundle
		if (savedInstanceState != null)
		{
			if (savedInstanceState.containsKey(this.getString(R.string.state_baseactivity_uri)))
			{
				_recentlyStartedActivityUri = savedInstanceState.getString(getResources().getString(
						R.string.state_baseactivity_uri));
			}
		}
	}

	private BroadcastDialog broadcastDialog = new BroadcastDialog();;

    @Override
    protected void onStart() {
        super.onStart();
		firmwareUpgradeBroadcastListener = new FirmwareUpgradeBroadcastListener();
		dataTransferFileStatusBroadcastListener = new DataTransferFileStatusBroadcastListener();
		EventBus.getDefault().register(this);
		broadcastDialog.register(this);
		firmwareUpgradeBroadcastListener.register(this);
		dataTransferFileStatusBroadcastListener.register(this);
		if(isOnBackground){
			if(firmwareUpdateHandlerPersist != null){
				firmwareUpgradeBroadcastListener.setPersistedHandler(firmwareUpdateHandlerPersist);
				firmwareUpgradeBroadcastListener.updateHandler();
			}

		}
		onEnterForeground();
    }


    @Override
	protected void onResume() {
		super.onResume();
        checkMalfunctionsAndDiagnostics();
	}

	@Override
    protected void onStop() {

		onEnterBackground();

		firmwareUpgradeBroadcastListener.unregister();
		broadcastDialog.unregister();
		dataTransferFileStatusBroadcastListener.unregister();
		EventBus.getDefault().unregister(this);

		super.onStop();
    }

    @Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);

		// save the instance state to the bundle
		if(_recentlyStartedActivityUri != null)
			outState.putString(this.getString(R.string.state_baseactivity_uri), _recentlyStartedActivityUri);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// when destroying a screen, reset the last activity, but only if the current activity is the most recent activity 
		// (which means that back button was tapped for navigation)
		// this is necessary so that if the back button is used for navigation, that the
		// same screen can be displayed again (such as the main menu on RODS screen)
		String currentUri = this.getIntent().toUri(0);
		Log.v("BaseActivity", String.format("onDestroy recentURI %s intent: %s", _recentlyStartedActivityUri, currentUri));
		if(_recentlyStartedActivityUri != null && currentUri.compareTo(_recentlyStartedActivityUri)==0)
			_recentlyStartedActivityUri = null;

		Log.v("BaseActivity", String.format("onDestroy end recentURI %s", _recentlyStartedActivityUri));
	}

	public void UpdateDutyStatusHeader(String logDate, String timestamp, DutyStatusEnum dutyStatus, Location location, RuleSetTypeEnum ruleset)
	{
		boolean isExemptFromELDUse = this.getIsExemptFromELDUse();
		boolean isMandateEnabled = GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled();

		if(findViewById(R.id.layoutDutyStatusHeader) != null) {
			LogEntryController ctrl = new LogEntryController(this);

			StringBuilder sb = new StringBuilder();
			if (!ctrl.IsCurrentUserTheDriver()) {
				sb.append(" * ");
			}
			sb.append(ctrl.getCurrentUser().getCredentials().getEmployeeFullName());

			if (_tvHeaderName != null) {
				if (GlobalState.getInstance().getLoggedInUserList().size() > 1)
					_tvHeaderName.setText(CreateUnderlinedText(sb.toString()));
				else
					_tvHeaderName.setText(sb.toString());
			}

			if (_tvHeaderDutyStatus != null) {
				String displayText;
				String dutyStatusText = GetDutyStatusDisplayText(dutyStatus, ruleset);
				//restricting length of duty status on RODs due to space constraints and the need to see the ruleset un-truncated
				if(dutyStatus.getValue() == dutyStatus.ONDUTY)
				{
					displayText =  dutyStatusText.replace("On-Duty Not Driving" ,"On-Duty");
				}
				else
				{
					displayText = dutyStatusText;
				}
				_tvHeaderDutyStatus.setText(isExemptFromELDUse ? displayText : CreateUnderlinedText(displayText));
			}
			if (_tvHeaderLocation != null) {
				String locationName = location.ToLocationString();
				if (locationName != null)
					_tvHeaderLocation.setText((isExemptFromELDUse || isMandateEnabled) ? location.ToLocationString() : CreateUnderlinedText(location.ToLocationString()));
				else if (!EmployeeLogUtilities.IsReverseGeocodingLocationAsync())
					_tvHeaderLocation.setText("");
			}

			if (_tvHeaderLogEventTS != null) {
                Date time = DateUtility.getDateTimeFromString(timestamp);
				String logDateTime = logDate + " " + DateUtility.createHomeTerminalTimeString(time, isMandateEnabled);
				_tvHeaderLogEventTS.setText((isExemptFromELDUse || isMandateEnabled) ? logDateTime : CreateUnderlinedText(logDateTime));
			}
		}
	}

	public String GetDutyStatusDisplayText(DutyStatusEnum dutyStatus, RuleSetTypeEnum ruleset)
	{
		String dutyStatusFormat;
		String dutyStatusText;
		String sDutyStatus = dutyStatus.toFriendlyName();
		boolean isTheDriver = IsTheDriver(GlobalState.getInstance().getCurrentUser());
		boolean isEldMandateEnabled = GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled();
		boolean isExemptFromELDUse = this.getIsExemptFromELDUse();

		if (dutyStatus.getValue()==DutyStatusEnum.DRIVING)
			sDutyStatus=DutyStatusEnum.Friendly_Driving;

		if (dutyStatus.getValue()==DutyStatusEnum.ONDUTY)
			sDutyStatus=DutyStatusEnum.Friendly_OnDuty;

		if (GlobalState.getInstance().getIsInPersonalConveyanceDutyStatus() && isTheDriver) {
			if(isExemptFromELDUse && isEldMandateEnabled){
				dutyStatusFormat = "%1$s - PC (ELD Exempt)";
			}
			else{
				dutyStatusFormat = "%1$s - PC (%2$s)";
			}
			dutyStatusText = String.format(dutyStatusFormat, DutyStatusEnum.Friendly_OffDuty, ruleset.getStringAbbr(this));
		}
		else if (GlobalState.getInstance().getIsInHyrailDutyStatus() && isTheDriver) {
			if(isExemptFromELDUse && isEldMandateEnabled){
				dutyStatusFormat = "%1$s - Hyrail (ELD Exempt)";
			}
			else{
				dutyStatusFormat = "%1$s - Hyrail (%2$s)";
			}
			dutyStatusText = String.format(dutyStatusFormat, DutyStatusEnum.RODSFriendly_OnDuty, ruleset.getStringAbbr(this));
		}
		else if (GlobalState.getInstance().getIsInYardMoveDutyStatus() && isTheDriver) {
			if(isExemptFromELDUse && isEldMandateEnabled){
				dutyStatusFormat = "%1$s - YM (ELD Exempt)";
			}
			else{
				dutyStatusFormat = "%1$s - YM (%2$s)";
			}
			dutyStatusText = String.format(dutyStatusFormat, DutyStatusEnum.RODSFriendly_OnDuty, ruleset.getStringAbbr(this));
		}
		else if (GlobalState.getInstance().getIsInNonRegDrivingDutyStatus() && isTheDriver) {
			if(isExemptFromELDUse && isEldMandateEnabled){
				dutyStatusFormat = "%1$s - NonReg (ELD Exempt)";
			}
			else{
				dutyStatusFormat = "%1$s - NonReg"; // (%2$s)";
			}
			dutyStatusText = String.format(dutyStatusFormat, DutyStatusEnum.RODSFriendly_OnDuty, ruleset.getStringAbbr(this));
		}
		else if(isExemptFromELDUse && isEldMandateEnabled && isTheDriver){
			dutyStatusFormat = "%1$s (ELD Exempt)";

			dutyStatusText = String.format(dutyStatusFormat,sDutyStatus);
		}
		else {
			dutyStatusFormat = "%1$s (%2$s)";
			dutyStatusText = String.format(dutyStatusFormat,sDutyStatus, ruleset.getStringAbbr(this));
		}

		return dutyStatusText;
	}

	private SpannableString CreateUnderlinedText(String s)
	{
		SpannableString content = new SpannableString(s);
		content.setSpan(new UnderlineSpan(), 0, s.length(), 0);
		return content;
	}

	// Use KeyguardManager to determine whether or not the device is locked
	public static boolean IsScreenLocked(Context context) {
		boolean showing = false;
		try {
			KeyguardManager kgMgr = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
			showing = kgMgr.inKeyguardRestrictedInputMode();
		} catch (Exception ex) {
			ErrorLogHelper.RecordException(context, ex);
			ex.printStackTrace();
		}

		return showing;
	}

	// Display Certify Logs
    public void DisplayCertifyLogsDialog(final DialogInterface.OnClickListener onYesHandler, final DialogInterface.OnClickListener onNoHandler)
    {
        runOnUiThread(new Runnable() {
            public void run() {
                ShowConfirmationMessage(BaseActivity.this,
                        R.string.lblcertifyrecertifymessagetitle,
                        getString(R.string.lblYouHavLogsOnYourDeviceThatNeedCertifying),
						R.string.btnyes,
                        new ShowMessageClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                super.onClick(dialog, id);
								onYesHandler.onClick(dialog, id);
                            }
                        },
						R.string.btnno,
                        new ShowMessageClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                super.onClick(dialog, id);
								onNoHandler.onClick(dialog, id);
                            }
                        }
                );
            }
        });
    }

	public void DisplayUDPAvailableDialog(final DialogInterface.OnClickListener onYesHandler, final DialogInterface.OnClickListener onNoHandler)
	{
		runOnUiThread(new Runnable() {
			public void run() {
				ShowConfirmationMessage(BaseActivity.this,
						R.string.lblreviewudptitle,
						getString(R.string.lblreviewudpmessage),
						R.string.btnyes,
						new ShowMessageClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								super.onClick(dialog, id);
								onYesHandler.onClick(dialog, id);
							}
						},
						R.string.btnno,
						new ShowMessageClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								super.onClick(dialog, id);
								onNoHandler.onClick(dialog, id);
							}
						}
				);
			}
		});
	}

    public void DisplayReviewLogEditsDialog(final DialogInterface.OnClickListener onYesHandler, final DialogInterface.OnClickListener onNoHandler)
	{
        runOnUiThread(new Runnable() {
            public void run() {
                ShowConfirmationMessage(BaseActivity.this,
                        R.string.lblreviewlogedits,
                        getString(R.string.lblYouHaveReviewLogEdits),
                        R.string.btnyes,
						new ShowMessageClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								super.onClick(dialog, id);
								onYesHandler.onClick(dialog, id);
						}},
                        R.string.btnno,
						new ShowMessageClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								super.onClick(dialog, id);
								onNoHandler.onClick(dialog, id);
							}}
                );
            }
        });
    }

    public void submitLogsDuringLogOut() {

    }
	private boolean teamDrivingCurrentUserSwitched = false;
	Handler dismissVerifyDrivingStatusHandler;

	private void automaticallyChangeDutyStatusForExemptDriving(){
		IAPIController controller = MandateObjectFactory.getInstance(GlobalState.getInstance(), GlobalState.getInstance().getFeatureService()).getCurrentEventController();
		Date stopTime = GlobalState.getInstance().getPotentialDrivingStopTimestamp();
		try {
			EmployeeLog currentLog = GlobalState.getInstance().getCurrentDriversLog();
			controller.CreateDutyStatusChangedEvent(currentLog, stopTime, new DutyStatusEnum(DutyStatusEnum.OFFDUTY), GlobalState.getInstance().getLastLocation(), true, currentLog.getRuleset(), "Exempt TODO", stopTime, false, null, null);
		} catch (Throwable e){
			throw new RuntimeException("Error saving duty status change after drive end for exempt driver", e);
		}
	}

	public void DisplayVerifyDrivingDutyStatusEndDialog() {
		final String dialogMessage = getString(R.string.continue_driving_status_message);
		if(GlobalState.getInstance().getCurrentDriversLog().getIsExemptFromELDUse()){
			automaticallyChangeDutyStatusForExemptDriving();
		}else {
			dismissVerifyDrivingStatusHandler = new Handler(Looper.getMainLooper());
			dismissVerifyDrivingStatusHandler.postDelayed(new Runnable() {
						@Override
						public void run() {
							if (teamDrivingCurrentUserSwitched) {
								if (GlobalState.getInstance().getCurrentDesignatedDriver().getCredentials().getEmployeeId().equals(GlobalState.getInstance().getCurrentUser().getCredentials().getEmployeeId())) {
									teamDrivingCurrentUserSwitched = false;
									DisplayVerifyDrivingDutyStatusEndDialog();
								} else {
									automaticallyDismissContinueInDrivingPrompt(dialogMessage);
								}
							} else {
								automaticallyDismissContinueInDrivingPrompt(dialogMessage);
							}
						}
					}, EmployeeLogUtilities.CONTINUE_IN_DRIVING_STATUS_PROMPT_TIMEOUT // 60 seconds
			);

			if (GlobalState.getInstance().getCurrentDesignatedDriver().getCredentials().getEmployeeId().equals(GlobalState.getInstance().getCurrentUser().getCredentials().getEmployeeId())) {
				showContinueInDrivingStatusDialogMessage(dialogMessage);
			} else {
				teamDrivingCurrentUserSwitched = true;
			}
		}
	}
	private void automaticallyDismissContinueInDrivingPrompt(String dialogMessage) {
		DismissAlertMessageFor(dialogMessage);

		// After DRIVE_OFF, user was prompted to remain in driving and ignored the prompt.
		// After one minute an On Duty status gets automatically created.

		// create an on duty status change
		LogEntryController ctrl = new LogEntryController(GlobalState.getInstance());
		String productionId = null, authorityId = null;
		if (GlobalState.getInstance().getCompanyConfigSettings(getBaseContext()).getIsMotionPictureEnabled()) {
			authorityId = GlobalState.getInstance().get_currentMotionPictureAuthorityId();
			productionId = GlobalState.getInstance().get_currentMotionPictureProductionId();
		}

		EmployeeLog log = GlobalState.getInstance().getCurrentDriversLog();
		log = EmployeeLogUtilities.switchOverMidnightDrivingToDutyStatus(
				log, new DutyStatusEnum(DutyStatusEnum.ONDUTY),
				Enums.EmployeeLogEldEventRecordOrigin.AutomaticallyRecorded);

		boolean isCurrentOrFutureLog = !log.getLogDate().before(GlobalState.getInstance().getCurrentDriversLog().getLogDate());

		ctrl.PerformStatusChange(GlobalState.getInstance().getCurrentDesignatedDriver(), log, GlobalState.getInstance().getPotentialDrivingStopTimestamp(), new DutyStatusEnum(DutyStatusEnum.ONDUTY), GlobalState.getInstance().getLastLocation(), isCurrentOrFutureLog, false, null, productionId, authorityId);

		dismissVerifyDrivingStatusHandler.removeCallbacksAndMessages(null);

		// For mandate, determine if a manual location is required.
		if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
			HandleLocationAfterAutomaticOnDuty();
		}
	}
	private void showContinueInDrivingStatusDialogMessage(final String dialogMessage) {

		runOnUiThread(new Runnable() {
			public void run() {
				
				ShowConfirmationMessage(BaseActivity.this ,dialogMessage,
						// Yes
						new ShowMessageClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								super.onClick(dialog, id);
								IAPIController empLogEventController = MandateObjectFactory.getInstance(GlobalState.getInstance(), GlobalState.getInstance().getFeatureService()).getCurrentEventController();
								empLogEventController.driverElectedToContinueDriving();
								dismissVerifyDrivingStatusHandler.removeCallbacksAndMessages(null);
							}
						},
						// No
						new ShowMessageClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								super.onClick(dialog, id);
								onEndDrivingDutyStatus();
								dismissVerifyDrivingStatusHandler.removeCallbacksAndMessages(null);
							}
						});
			}
		});
	}
	public void HandleLocationAfterAutomaticOnDuty() {

		LogEntryController logEntryCtrl = new LogEntryController(GlobalState.getInstance());
		EmployeeLogEldMandateController mandateCtrl = new EmployeeLogEldMandateController(GlobalState.getInstance());
		ELDCommon eldCommon = new ELDCommon(getIntent(), getResources(), logEntryCtrl, mandateCtrl);
		if (eldCommon.ShouldShowManualLocation()) {
			logEntryCtrl.setLogEventForEdit(EmployeeLogUtilities.GetLastEventInLog(logEntryCtrl.getCurrentEmployeeLog(), Enums.EmployeeLogEldEventType.DutyStatusChange));
			// A manual location is required... Display New Duty Status screen, with status prefilled to On Duty
			Bundle extras = new Bundle();
			extras.putBoolean(getString(R.string.ismanuallocation_after_automatic_onduty), true);
			startActivity(RodsNewStatus.class, extras);
		} else {
			// Update location information
			EmployeeLogEldEvent logEvent = logEntryCtrl.getLogEventForEdit();
			logEvent.setLatitudeStatusCode(getIntent().getStringExtra(this.getResources().getString(R.string.extra_lat_lon_status)));
			logEvent.setLongitudeStatusCode(logEvent.getLatitudeStatusCode());
			if (getIntent().hasExtra(this.getResources().getString(R.string.extra_latitude))) {
				logEvent.setLatitude(getIntent().getExtras().getDouble(this.getResources().getString(R.string.extra_latitude)));
			}
			if (getIntent().hasExtra(this.getResources().getString(R.string.extra_Longitude))) {
				logEvent.setLongitude(getIntent().getExtras().getDouble(this.getResources().getString(R.string.extra_Longitude)));
			}
		}
	}

	public void DismissVerifyDrivingStatusEndDialog()
	{
		DismissAlertMessageFor(getString(R.string.continue_driving_status_message));
		if (dismissVerifyDrivingStatusHandler != null)
			dismissVerifyDrivingStatusHandler.removeCallbacksAndMessages(null);
	}

    protected void onEndDrivingDutyStatus()
    {
        startActivity(RodsNewStatus.class);
    }

	public void DisplaySpecialDrivingEndDialog(final EmployeeLogProvisionTypeEnum drivingCategory, final EventRecord eventRecord, final Location location, final EmployeeLog empLog) {
		SpecialDrivingDialog dialog = new SpecialDrivingDialogFactory().getDialog(drivingCategory, eventRecord, location, empLog);

		if(dialog != null)
			dialog.displayDialog();
	}

	public void DismissSpecialDrivingEndDialog(EmployeeLogProvisionTypeEnum drivingCategory) {
		DismissSpecialDrivingEndDialog(drivingCategory, null, null, null);
	}

	public void DismissSpecialDrivingEndDialog(EmployeeLogProvisionTypeEnum drivingCategory, EventRecord eventRecord, Location location, EmployeeLog empLog) {
		SpecialDrivingDialog dialog = new SpecialDrivingDialogFactory().getDialog(drivingCategory, eventRecord, location, empLog);

		if(dialog != null)
			dialog.dismiss();
	}

	protected void onSpecialDrivingEndResponse(EmployeeLogProvisionTypeEnum drivingCategory, boolean ended) {
	}

	private class SpecialDrivingDialogFactory {
		public SpecialDrivingDialog getDialog(EmployeeLogProvisionTypeEnum drivingCategory, EventRecord eventRecord, Location location, EmployeeLog empLog) {
			SpecialDrivingDialog dialog = null;

			switch(drivingCategory) {
				case PERSONALCONVEYANCE:
					dialog = new SpecialDrivingDialog(drivingCategory, getString(R.string.msg_endpersonalconveyance), eventRecord, location, empLog);
					break;
				case HYRAIL:
					dialog = new SpecialDrivingDialog(drivingCategory, getString(R.string.msg_endhyrail), eventRecord, location, empLog);
					break;
				case NONREGULATED:
					dialog = new SpecialDrivingDialog(drivingCategory, getString(R.string.msg_endnonregdriving), eventRecord, location, empLog);
					break;
			}

			return dialog;
		}
	}

	private class SpecialDrivingDialog {
		EmployeeLogProvisionTypeEnum _drivingCategory;
		String _message;
		ISpecialDrivingController _controller;
		EventRecord _eventRecord;
		Location _location;
		EmployeeLog _empLog;

		SpecialDrivingDialog(EmployeeLogProvisionTypeEnum drivingCategory, String message, EventRecord eventRecord, Location location, EmployeeLog empLog) {
			_drivingCategory = drivingCategory;
			_controller = SpecialDrivingFactory.getControllerForDrivingCategory(_drivingCategory);
			_message = message;
			_eventRecord = eventRecord;
			_location = location;
			_empLog = empLog;
		}

		void displayDialog() {
			runOnUiThread(new Runnable() {
				public void run() {
					ShowConfirmationMessage(BaseActivity.this, _message,
							// Yes
							new BaseActivity.ShowMessageClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int id) {
									super.onClick(dialog, id);
									SpecialDrivingDialog.this.onEnd();
								}
							},
							// No
							new BaseActivity.ShowMessageClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int id) {
									super.onClick(dialog, id);
									SpecialDrivingDialog.this.onContinue();
								}
							});
				}
			});
		}

		void dismiss() {
			runOnUiThread(new Runnable() {
				public void run() {
					DismissAlertMessageFor(_message);
				}
			});
		}

		void onEnd() {
			if (_controller.getIsInSpecialDrivingSegment()) {

				EmployeeLog log = _empLog != null ? _empLog : GlobalState.getInstance().getCurrentEmployeeLog();
				Location location = _location != null ? _location : GlobalState.getInstance().getLastLocation();
				Date eventTime = _eventRecord!= null ? _eventRecord.getTimecodeAsDate() : _controller.getEndTime(log);

				_controller.DialogPreprocessEndSpecialDrivingStatus(eventTime, location, log);
				_controller.EndSpecialDrivingStatus(eventTime, location, log);
			}

			_controller.setIsInSpecialDutyStatus(false);

			this.startActivity();

			BaseActivity.this.onSpecialDrivingEndResponse(_drivingCategory, true);
		}

		void onContinue() {
			_controller.setIsInSpecialDutyStatus(true);

			this.startActivity();

			BaseActivity.this.onSpecialDrivingEndResponse(_drivingCategory, false);
		}

		protected void startActivity() {
			// standard mode, dismiss the clocks
			BaseActivity.this.startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
		}
	}

    public void checkMalfunctionsAndDiagnostics(){
        if (menu != null && GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
            boolean displayMalfunctionIcon = ! getInstance().getEmployeeLogEldMandateController().getActiveMalfunctions(GlobalState.getInstance().getCurrentEmployeeLog()).isEmpty();
            boolean displayDataDiagnosticIcon = ! getInstance().getEmployeeLogEldMandateController().getActiveDataDiagnostics(GlobalState.getInstance().getCurrentEmployeeLog()).isEmpty();

            //Disable button if vehicle is in motion or we're on the RptMalfunctionAndDataDiagnostics screen from the pressed buttons.
            boolean shouldEnableIcons = ! getInstance().getLogEntryController().IsVehicleInMotion()
                    && ! getIntent().getBooleanExtra(RptMalfunctionAndDataDiagnostic.FROM_ALERT_BUTTON_PRESSED, false);


            menu.findItem(R.id.menuMalfunctionButton).setVisible(displayMalfunctionIcon).setEnabled(shouldEnableIcons);
            menu.findItem(R.id.menuDataDiagnosticsButton).setVisible(displayDataDiagnosticIcon).setEnabled(shouldEnableIcons);
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(Malfunction event){
        checkMalfunctionsAndDiagnostics();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(DataDiagnosticEnum event){
        checkMalfunctionsAndDiagnostics();
    }

    @Subscribe(threadMode =  ThreadMode.MAIN)
    public void onMessageEvent(DutyStatusEnum dutyEnum){
        checkMalfunctionsAndDiagnostics();
    }

    public static void addHeaderEldIdentifier(Context context, ActionBar actionBar, boolean isEldMandate, boolean isInRoadSideInspection){
        if (actionBar == null) {
            return;
        }

        SpannableString titleAOBRD;
        CharSequence currentTitle;

        if (isInRoadSideInspection){
            currentTitle = context.getString(R.string.roadside_inspection_mode);
        } else {
            currentTitle = context.getString(R.string.actionbar_title);
        }

        if (isEldMandate) {
            titleAOBRD = new SpannableString(context.getString(R.string.actionbar_title_eld));
        } else {
            titleAOBRD = new SpannableString(context.getString(R.string.actionbar_title_aobrd));
        }

        //Apply bold style to the new title
        titleAOBRD.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0 , titleAOBRD.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        actionBar.setTitle(TextUtils.concat(currentTitle," ",titleAOBRD));
    }

	/**
	 * Increase the LeftNavFrag weight used on the Eula
	 * @param weight Parameter to set the new weight
	 */
	public void increaseLeftNavWeight(int weight){
		FrameLayout _flLeftNav = (FrameLayout) findViewById(R.id.leftnav_framelayout);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, weight);
		_flLeftNav.setLayoutParams(lp);
	}


	/**
	 * BEGIN: Common base activity for any activity wishing to Logout. Contains the LogoutTask background tasks and retry logic.
	 */

	/**
	 * Attempt to logout the current user.
	 * Returns whether or not the logout was successful
	 *
	 * @return true if the logout was successful
	 */
	protected boolean PerformLogout(String logoutTimeOffset, Date exactTime, int selectedDutyStatus, String locationText, String latLonStatus) {
		boolean isSuccessful = false;

		try {
			// issue the logout command
			LoginController loginController = new LoginController(GlobalState.getInstance().getApplicationContext());
			loginController.PerformLogout(
					logoutTimeOffset,
					exactTime,
					selectedDutyStatus,
					locationText,
					"");

			if(GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled() && !this.getIsExemptFromELDUse()) {
				if (latLonStatus != null && latLonStatus.length() > 0) {
					LogEntryController logEntryController = new LogEntryController(GlobalState.getInstance().getApplicationContext());
					if (!TextUtils.isEmpty(locationText)) {
						logEntryController.MandateUpdateLatLongStatusCode(latLonStatus, locationText);
					}
					else {
						logEntryController.MandateUpdateLatLongStatusCode(latLonStatus);
					}
				}
			}

			isSuccessful = true;
		}
		catch (Exception ex)
		{
			Log.e("UnhandledCatch", ex.getMessage() + ": " + Log.getStackTraceString(ex));
		}

		return isSuccessful;
	}

	protected LogoutTask mLogoutTask;

	public class LogoutTask extends AsyncTask<Void, Void, Boolean> {

		boolean submitLogs;
		private String logoutTime;
		private List<Date> certifyLogDates;
		Date exactTime;
		int selectedDutyStatus;
		String locationText;
		String latLonStatus;

		public LogoutTask(boolean submitLogs, String logoutTimeOffset, List<Date> certifyLogDates, Date exactTime, int selectedDutyStatus, String locationText, String latLonStatus) {
			this.submitLogs = submitLogs;
			this.logoutTime = logoutTimeOffset;
			this.certifyLogDates = certifyLogDates;
			this.exactTime = exactTime;
			this.selectedDutyStatus = selectedDutyStatus;
			this.locationText = locationText;
			this.latLonStatus = latLonStatus;
		}

		protected void onPreExecute() {
			LockScreenRotation();
			if (!BaseActivity.this.isFinishing()) {
				sendShowDialogMessage(R.string.msgloggingout);
			}
		}

		protected Boolean doInBackground(Void... params) {
			return PerformLogout(logoutTime, exactTime, selectedDutyStatus, locationText, latLonStatus);
		}

		protected void onProgressUpdate(Void... unused) {

		}

		protected void onPostExecute(Boolean isLoggedOut) {
			sendHideDialogMessage();

			SharedPreferences userPref = getSharedPreferences(getString(R.string.sharedpreferencefile), 0);
			SharedPreferences.Editor editor = userPref.edit();
			String selectedOffset = logoutTime;
			editor.putString(BaseActivity.this.getString(R.string.logoutoffset), selectedOffset);
			editor.commit();

			//Purge old records for Geotab tables based on the company Log Purge Day Count
			if(GlobalState.getInstance().getCompanyConfigSettings(GlobalState.getInstance().getApplicationContext()).getIsGeotabEnabled()){
				GeotabController _geGeotabController = new GeotabController(BaseActivity.this);
				_geGeotabController.AutoPurgeOldGeotabRecords();
			}

			if (!isLoggedOut) {
				if (submitLogs) {
					// something bad happened while logging out
					// Not sure it's necessary to show this particular message
					// The exception handler in PerformLogout may be good enough.
					BaseActivity.this.ShowConfirmationMessage(BaseActivity.this, getString(R.string.msgerrorloggingoutnorecssubmitted), new ShowMessageClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							super.onClick(dialog, id);
							Return(true);
						}
					}, null);
				} else {
					// errors while logging out
					BaseActivity.this.ShowConfirmationMessage(BaseActivity.this, getString(R.string.msgerrorloggingout), new ShowMessageClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							super.onClick(dialog, id);
							Return(true);
						}
					}, null);
				}
			} else if (submitLogs) {
				if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
					if (GlobalState.getInstance().getCurrentUser() != null && GlobalState.getInstance().getCurrentUser().getExemptFromEldUse()) {
						// no need to Certify
					} else {
						CreateCertificationEvents(certifyLogDates);
					}
				}

				new SubmitLogsTask(true).execute();
			} else {
				Return(true);
			}
		}
	}

	/**
	 * Add Certification event only to the most recent LogSourceStatusEnum
	 */
	public void CreateCertificationEvents(List<Date> selectedLogDates)
	{
		IAPIController empLogController = MandateObjectFactory.getInstance(this, GlobalState.getInstance().getFeatureService()).getCurrentEventController();
		for (Date date : selectedLogDates)
		{
			EmployeeLog logForDate = empLogController.GetEmployeeLogToCertify(date);
			try
			{
				if (!logForDate.getIsCertified()) {
					LoginController loginController = new LoginController(GlobalState.getInstance().getApplicationContext());

					if (DateUtility.IsToday(logForDate.getLogDate(), loginController.getCurrentUser())) {
						empLogController.CertifyEmployeeLog(GlobalState.getInstance().getCurrentEmployeeLog());
					} else {
						empLogController.CertifyEmployeeLog(logForDate);
					}
				}
				// else assume upload failed and we don't want to re-add Certification event
			}
			catch (Throwable e)
			{
				Log.e("UnhandledCatch", "Failed to log a certification event", e);
			}
		}
	}

	/**
	 * END: Common base activity for any activity wishing to Logout. Contains the LogoutTask background tasks and retry logic.
	 */


	/**
	 * BEGIN: Common base activity for any activity wishing to Submit Mandate Logs. Contains the SubmitLogsTask background tasks and retry logic.
	 */

	protected User mSubmittingUser = null;
	protected AlertDialog mRetryDialog = null;

	/**
	 * Attempt to submit the all the records to DMO.
	 * If the network is not available, then display a message.
	 * Returns whether or not everything was submitted successfully.
	 *
	 * @param submittingUser The user submitting logs
	 * @return true if everything was submitted successfully
	 */
	protected boolean PerformSubmitLogs(User submittingUser) {
		boolean isSuccessful = false;
		try {
			if (NetworkUtilities.VerifyWebServiceConnection(this)) {
				IAPIController empLogController = MandateObjectFactory.getInstance(this, GlobalState.getInstance().getFeatureService()).getCurrentEventController();
				isSuccessful =  empLogController.SubmitAllRecords(submittingUser, false);	//Today's log needs to be submitted for ELD and AOBRD
			}
            if(isSuccessful && !GlobalState.getInstance().getIsUserLoggingOut()) {
                //update new employee log record to have a mobilestarttimestamp value
                EmployeeLog empLog = GlobalState.getInstance().getCurrentEmployeeLog();
                EmployeeLogFacade employeeLogFacade = new EmployeeLogFacade(this, GlobalState.getInstance().getCurrentUser());
                if(empLog == null ) {
                    List<EmployeeLog> localLogList = employeeLogFacade.GetLocalLogList();
                    empLog = localLogList.get(localLogList.size() - 1);
                }
                EmployeeLogEldEvent loginDutyStatusEvent = EmployeeLogUtilities.GetLastEventInLog(empLog);
                Date logStartTimestamp = loginDutyStatusEvent.getStartTime();
                empLog.setMobileStartTimestamp(logStartTimestamp);
                employeeLogFacade.Save(empLog, 1);
                GlobalState.getInstance().setCurrentDriversLog(empLog);
            }
		} catch (Exception ex) {
			Log.e("UnhandledCatch", ex.getMessage() + ": " + Log.getStackTraceString(ex));
		}

		return isSuccessful;
	}

	private AlertDialog createSubmitUnsuccessfulDialog(Context ctx, String msg, final boolean shouldExitApp) {
	    int negativeButtonId = shouldExitApp ? R.string.button_exit : R.string.btnok;
	    AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
		builder.setMessage(msg).setCancelable(false).setPositiveButton(R.string.button_try_again, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				new SubmitLogsTask(shouldExitApp).execute();
			}
		}).setNegativeButton(negativeButtonId, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				mRetryDialog.dismiss();
				UnlockScreenRotation();
				Return(shouldExitApp);
			}
		});
		return builder.create();
	}

	/**
	 * When logging out for a user 'Exrmpt For ELD Use' - bypass the Logout activity
	 * (Certify and Duty Status) and immediately submit logs.
	 */
	public void SubmitLogsWhenExemptForEldUse(final boolean shouldExitApp) {

		mSubmittingUser = GlobalState.getInstance().getCurrentUser();

		final Activity thisActivity = this;

		AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setMessage(getString(R.string.exempt_from_eld_dialog_message));
		alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.lblsubmitlogstitle),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {

						if (shouldExitApp) {
							String selectedOffset = thisActivity.getString(R.string.exacttime);

							LogEntryController logEntryController = new LogEntryController(BaseActivity.this);
							Date exactTime = logEntryController.getCurrentClockHomeTerminalTime();

							new LogoutTask(true, selectedOffset, null, exactTime, DutyStatusEnum.OFFDUTY, "", "").execute();
						}
						else {
							new SubmitLogsTask(shouldExitApp).execute();
						}
						dialog.dismiss();
					}
				});
		alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.btncancel),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		alertDialog.show();
	}




	public class SubmitLogsTask extends AsyncTask<Void, Integer, Boolean> {

		boolean shouldExitApp;
		boolean networkNotFound = false;

		public SubmitLogsTask(boolean shouldExitApp) {
			this.shouldExitApp = shouldExitApp;
		}

		protected void onPreExecute() {
			if (!BaseActivity.this.isFinishing()) {
				sendShowDialogMessage(R.string.msgsubmittinglogs);
			}
		}

		protected Boolean doInBackground(Void... params) {
			// submit all the logs
			if (!NetworkUtilities.VerifyWebServiceConnection(GlobalState.getInstance().getApplicationContext())) {
				networkNotFound = true;
				return false;
			} else {
				boolean done = PerformSubmitLogs(mSubmittingUser);
				return done;
			}
		}

		protected void onProgressUpdate(Integer... unused) {
		}

		protected void onPostExecute(Boolean successful) {
			sendHideDialogMessage();
			if (!successful) {
				if (networkNotFound) {
					ShowMessage(BaseActivity.this, getString(R.string.no_network_connection));
					UnlockScreenRotation();
				} else {
					if (!BaseActivity.this.isFinishing()) {
						mRetryDialog = createSubmitUnsuccessfulDialog(BaseActivity.this, getString(R.string.msgerroroccuredsubmitagain), shouldExitApp);
						mRetryDialog.show();
					}
				}
			} else {
				Return(shouldExitApp);
			}
		}
	}

	/**
	 * END: Common base activity for any activity wishing to Submit Mandate Logs. Contains the SubmitLogsTask background tasks and retry logic.
	 */
}
