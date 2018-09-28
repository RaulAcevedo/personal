package com.jjkeller.kmb;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.jjkeller.kmb.fragments.LeftNavFrag;
import com.jjkeller.kmb.fragments.LeftNavImgFrag;
import com.jjkeller.kmb.fragments.TeamDriverAddDriverFrag;
import com.jjkeller.kmb.interfaces.ITeamDriverAddDriver.TeamDriverAddDriverFragActions;
import com.jjkeller.kmb.interfaces.ITeamDriverAddDriver.TeamDriverAddDriverFragControllerMethods;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.configuration.LoginCredentials;
import com.jjkeller.kmbapi.controller.TeamDriverController;
import com.jjkeller.kmbapi.controller.share.KmbApplicationException;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbui.R;

import java.text.ParseException;
import java.util.Date;

public class TeamDriverAddDriver extends BaseActivity implements
		TeamDriverAddDriverFragActions,
		TeamDriverAddDriverFragControllerMethods,
		LeftNavFrag.OnNavItemSelectedListener,
		LeftNavFrag.ActivityMenuItemsListener,
		LeftNavImgFrag.ActivityMenuIconItems {
	TeamDriverAddDriverFrag _contentFrag;

	private String _userName; 
	private String _employeeCode;
	private String _driverFullName;
	private Date _startTime;

	public BaseActivity getActivity() {
		return this;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.baselayout);

		mFetchLocalDataTask = new FetchLocalDataTask(this.getClass()
				.getSimpleName());
		mFetchLocalDataTask.execute();
	}

	@Override
	protected void loadControls() {
		super.loadControls();
		loadContentFragment(new TeamDriverAddDriverFrag());

		loadLeftNavFragment();
	}

	@Override
	public void setFragments() {
		super.setFragments();

		Fragment f = getSupportFragmentManager().findFragmentById(
				R.id.content_fragment);
		_contentFrag = (TeamDriverAddDriverFrag) f;
	}

	public TeamDriverController getMyController() {
		return (TeamDriverController) this.getController();
	}

	@Override
	protected void InitController() {
		TeamDriverController teamDriverCtrl = new TeamDriverController(this);

		this.setController(teamDriverCtrl);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (GlobalState.getInstance().getPassedRods() == true)
			this.CreateOptionsMenu(menu, false);
		return true;
	}

	@Override
	protected void Return(boolean success) {
		if (success)
			this.startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);

		finish();
	}

	public void handleCancelButtonClick() {
		Return();
	}

	public void handleOKButtonClick() {
		_userName = _contentFrag.getDriverUserNameEditText().getText().toString(); 
		_employeeCode = _contentFrag.getEmployeeCode(); 
		_driverFullName = _contentFrag.getDriverFullNameText().getText().toString(); 
		_startTime = null;

		if (!_contentFrag.getStartCheckbox().isChecked()) {
			_startTime = CreateExactTime();
		}

		if (this.Validate(_userName,_employeeCode, _driverFullName, _startTime)) {
			mSaveLocalDataTask = new SaveLocalDataTask(this.getClass()
					.getSimpleName());
			mSaveLocalDataTask.execute();
		}
	}

	public void handleCheckNameButtonClick() {
		
		if (_contentFrag.getDriverUserNameEditText().getText().length() > 0) {
				String userName = _contentFrag.getDriverUserNameEditText()
						.getText().toString();
				new DownloadEmployeeNameTask(userName).execute();
				
		} else {
			this.ShowMessage(this, getString(R.string.msg_driverusername_missing));
		}
		
	}

	public void handleTeamFromStartChecked() {
		_contentFrag.getExactTimeButton().setEnabled(
				!_contentFrag.getStartCheckbox().isChecked());
	}

	@Override
	protected boolean saveData() {
		boolean isSuccessful = false;
		this.getMyController().StartTeamDriver(_employeeCode,
				_driverFullName, _startTime, _userName);
		
		//New TeamDriver WorkFlow set the mode of the application, 
		if (GlobalState.getInstance().getTeamDriverMode() != GlobalState.TeamDriverModeEnum.SEPARATEDEVICE)
			GlobalState.getInstance().setTeamDriverMode(GlobalState.TeamDriverModeEnum.SEPARATEDEVICE);
		
		isSuccessful = true;
		return isSuccessful;
	}

	// Need to Modified to fit new driver workflow
	private boolean Validate(String userName, String employeeCode, String driverFullName,
			Date startTime) {
		boolean isValid = true;

		// both fields are required
		if (userName.length() == 0) {
			ShowMessage(this, getString(R.string.msg_driverusername_required));
			isValid = false;
		} 
		else if (driverFullName.length() == 0) {
			// Function will respond as false, but shown dialog will allow
			// kicking off the save data task
			ShowMessage(this, getString(R.string.msg_checkusername_teamdriver));
			isValid = false;
			
			//String msg = getString(R.string.msg_checkusername_teamdriver); 
			//_confirmationDialog = this.CreateConfirmationMessage(this, msg);
			//_confirmationDialog.show();
		}
		else {
			String msg = this.getMyController().ValidateStart(employeeCode,
					driverFullName, startTime);
			if (msg != null && msg.length() > 0) {
				// Function will respond as false, but shown dialog will allow
				// kicking off the save data task
				isValid = false;

				msg = msg + getString(R.string.newline)
						+ getString(R.string.newline)
						+ getString(R.string.msg_ok_change_startime);
				_confirmationDialog = this.CreateConfirmationMessage(this, msg);
				_confirmationDialog.show();
			}
		}

		return isValid;
	}

	private Date CreateExactTime() {
		String currentDate = DateUtility.getDateFormat().format(
				this.getController().getCurrentClockHomeTerminalTime());
		String strExactTime = _contentFrag.getExactTimeButton().getText()
				.toString();
		Date exactTime = null;
		try {
			exactTime = DateUtility.getHomeTerminalDateTimeFormat12Hour()
					.parse(currentDate + " " + strExactTime);
		} catch (ParseException e) {

			Log.e("UnhandledCatch",
					e.getMessage() + ": " + Log.getStackTraceString(e));
		}
		return exactTime;
	}

	AlertDialog _confirmationDialog = null;

	private AlertDialog CreateConfirmationMessage(Context ctx, String msg) {
		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
		builder.setMessage(msg)
				.setCancelable(false)
				.setPositiveButton(this.getString(R.string.btnyes),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								new SaveLocalDataTask(this.getClass()
										.getSimpleName()).execute();
							}
						})
				.setNegativeButton(this.getString(R.string.btnno),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								_confirmationDialog.dismiss();
							}
						});
		AlertDialog alert = builder.create();
		return alert;
	}

	@Override
	protected void loadLeftNavFragment() {
		if (!isFinishing()) {
			View leftNavLayout = findViewById(R.id.leftnav_fragment);
			if (leftNavLayout != null) {
				// Create new fragment and transaction
				FragmentTransaction transaction = getSupportFragmentManager()
						.beginTransaction();

				setLeftNavFragment(new LeftNavImgFrag(
						R.layout.leftnav_item_imageandtext));

				// Replace fragment
				transaction
						.replace(R.id.leftnav_fragment, getLeftNavFragment());

				// Commit the transaction
				transaction.commit();

				setLeftNavSelectionItems();
				leftNavLayout.setBackgroundColor(getResources().getColor(
						R.color.menugray));
			}
		}
	}

	@Override
	public String getActivityMenuItemList() {
		return getString(R.string.tripinfo_actionitems_separatedevice);
	}

	public String getActivityMenuIconList() {
		return getString(R.string.tripinfo_actionitemsicons_separatedevice);
	}

	private class DownloadEmployeeNameTask extends AsyncTask<Void, Void, Void> {
		ProgressDialog pd;
		Exception ex;
		LoginCredentials teamDriverInfo; 
		String userName;

		public DownloadEmployeeNameTask(String userName) {
			this.userName = userName;
		}

		protected void onPreExecute() {
			LockScreenRotation();
			if (!TeamDriverAddDriver.this.isFinishing())
				pd = ProgressDialog.show(TeamDriverAddDriver.this, "",
						getString(R.string.msgcontacting));
		}

		protected Void doInBackground(Void... params) {
			try {
				teamDriverInfo = TeamDriverAddDriver.this.getMyController()
						.GetAuthenticationInformation(userName);
			} catch (KmbApplicationException kae) {
				this.ex = kae;
			}

			return null;
		}

		protected void onProgressUpdate(Void... unused) {
		}

		protected void onPostExecute(Void unused) {
			if (pd != null && pd.isShowing())
				pd.dismiss();
			if (ex != null) {
				if (ex.getClass() == KmbApplicationException.class)
					HandleException((KmbApplicationException) ex);
			} else {
				if (teamDriverInfo == null) {
					Toast.makeText(
							TeamDriverAddDriver.this,
							String.format(
									getString(R.string.msg_driverforusername_notfound),
									userName), Toast.LENGTH_SHORT).show();
				}
				
				_contentFrag.setTeamDriverAuthenicationInfo(teamDriverInfo);
			}
			UnlockScreenRotation();
		}
	}

}
