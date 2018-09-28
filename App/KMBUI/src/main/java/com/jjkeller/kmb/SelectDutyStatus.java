package com.jjkeller.kmb;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Spinner;

import com.actionbarsherlock.view.Menu;
import com.jjkeller.kmb.fragments.LeftNavFrag;
import com.jjkeller.kmb.fragments.LeftNavImgFrag;
import com.jjkeller.kmb.fragments.SelectDutyStatusFrag;
import com.jjkeller.kmb.interfaces.ILogDownloaderHost;
import com.jjkeller.kmb.interfaces.IOdometerCalibrationRequiredHost;
import com.jjkeller.kmb.interfaces.ISelectDutyStatus;
import com.jjkeller.kmb.interfaces.ISelectDutyStatus.SelectDutyStatusFragActions;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmb.share.ELDCommon;
import com.jjkeller.kmbapi.common.TimeKeeper;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.EmployeeLogEldMandateController;
import com.jjkeller.kmbapi.controller.LogEntryController;
import com.jjkeller.kmbapi.controller.TeamDriverController;
import com.jjkeller.kmbapi.controller.interfaces.IAPIController;
import com.jjkeller.kmbapi.controller.share.KmbApplicationException;
import com.jjkeller.kmbapi.controller.share.MandateObjectFactory;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.controller.utility.EmployeeLogUtilities;
import com.jjkeller.kmbapi.enums.DutyStatusEnum;
import com.jjkeller.kmbapi.enums.ExemptLogTypeEnum;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;
import com.jjkeller.kmbui.R;

import java.util.Arrays;
import java.util.Date;
import java.util.List;


public class SelectDutyStatus extends BaseActivity
							implements SelectDutyStatusFragActions, ISelectDutyStatus.SelectDutyStatusFragControllerMethods,
							ILogDownloaderHost, IOdometerCalibrationRequiredHost, LeftNavFrag.OnNavItemSelectedListener, LeftNavFrag.ActivityMenuItemsListener,
							LeftNavImgFrag.ActivityMenuIconItems{
	private SelectDutyStatusFrag _contentFrag;

	private boolean _isTeamDriver;
	private boolean _isSharedDevice = GlobalState.getInstance().getTeamDriverMode() == GlobalState.TeamDriverModeEnum.SHAREDDEVICE;
	private ExemptLogTypeEnum _exemptLogType;
	private DutyStatusEnum _dutyStatusEnum;
	protected boolean _loginProcess = false;
	private LogEntryController _logEntryController = null;
	private EmployeeLogEldMandateController _employeeLogEldMandateController = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.baselayout);

		mFetchLocalDataTask = new FetchLocalDataTask(this.getClass().getSimpleName());
		mFetchLocalDataTask.execute();

		String extra = getString(R.string.extra_exemptlogtype);
		int exemptLogType = this.getIntent().getIntExtra(extra, ExemptLogTypeEnum.NULL);

		_exemptLogType = new ExemptLogTypeEnum(exemptLogType);

		_isTeamDriver = GlobalState.getInstance().getTeamDriverMode() != GlobalState.TeamDriverModeEnum.NONE;

		_loginProcess = getIntent().hasExtra(this.getResources().getString(R.string.extra_isloginprocess));
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(_loginProcess) {
			//disable the back button if this is in the login process
			if (keyCode == KeyEvent.KEYCODE_BACK) {
				return true;
			}
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
    protected void loadControls() {
        super.loadControls();
        loadContentFragment(new SelectDutyStatusFrag());

        if (_isTeamDriver)
            loadLeftNavFragment();
    }

	@Override
    public void setFragments() {
        super.setFragments();

        Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
        _contentFrag = (SelectDutyStatusFrag) f;
        _contentFrag.setExemptLogType(_exemptLogType);

		if (isHyrailUseAllowed()) {
			_contentFrag.getUtilizeHyrailCheckbox().setVisibility(View.VISIBLE);
		}

		if (isYardMoveUseAllowed()) {
            _contentFrag.getYardMoveCheckbox().setVisibility(View.VISIBLE);
        }

		if (isNonRegDrivingAllowed()) {
			_contentFrag.getNonRegDrivingCheckbox().setVisibility(View.VISIBLE);
		}

		if (this.getController().getCurrentUser().isOffDutyWellSiteAllowed()) {
			_contentFrag.getOffDutyWellSiteRadioButton().setVisibility(View.VISIBLE);
		}
    }

	public TeamDriverController getMyController()
	{
		return (TeamDriverController)this.getController();
	}

	@Override
	protected void InitController() {
		TeamDriverController teamDriverCtrl = new TeamDriverController(this);

		this.setController(teamDriverCtrl);

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
        if (_contentFrag.getPersonalConveyanceCheckbox() != null && _contentFrag.getPersonalConveyanceCheckbox().isChecked()) {
            outState.putBoolean("isPersonalConveyanceChecked", true);
        }

        if (_contentFrag.getYardMoveCheckbox() != null && _contentFrag.getYardMoveCheckbox().isChecked()) {
            outState.putBoolean("isYardMoveChecked", true);
        }

        if (_contentFrag.getAnnotationOffDuty() != null && _contentFrag.getAnnotationOffDuty().getText().length() > 0 && _contentFrag.getPersonalConveyanceCheckbox().isChecked()) {
            outState.putString("annotationOffDuty", _contentFrag.getAnnotationOffDuty().getText().toString());
        }

        if (_contentFrag.getAnnotationOnDuty() != null && _contentFrag.getAnnotationOnDuty().getText().length() > 0) {
            if (_contentFrag.getYardMoveCheckbox().isChecked()) {
                outState.putString("annotationOnDuty", _contentFrag.getAnnotationOnDuty().getText().toString());
            }
        }

        if (_contentFrag.getErrorMessageTextView().getVisibility() == View.VISIBLE) {
            outState.putBoolean("showErrorMessage", true);
        }

		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState.getBoolean("isPersonalConveyanceChecked")) {
            _contentFrag.showAnnotationOffDuty();
        }

        if (savedInstanceState.getBoolean("isYardMoveChecked")) {
            _contentFrag.showAnnotationOnDuty();
        }

        if (savedInstanceState.getBoolean("showErrorMessage")) {
            _contentFrag.showAnnotationErrorMessage();
        } else {
            _contentFrag.hideErrorMessage();
        }
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		if (GlobalState.getInstance().getPassedRods())
			this.CreateOptionsMenu(menu, false);
		return true;
	}

	@Override
    protected void Return(boolean success) {
		if (_exemptLogType.getValue() != ExemptLogTypeEnum.NULL)
		{
			FixLogEntries();

			if (GlobalState.getInstance().getLoggedInUserList().size() > 1)
				getMyController().SetTeamDriver();

			if (_isTeamDriver)
            {
				//if this is not a standard grid log then the user won't
				//go to Trip Info, so we need to make sure that the active
				//user is the designated driver on this screen
				getMyController().SetCurrentUserToDesignatedDriver();

				TeamDriverController ctrlr = new TeamDriverController(this.getActivity());

				//if there are multiple drivers logged in and they're both on duty
				if (GlobalState.getInstance().getLoggedInUserList().size() > 1 && ctrlr.IsTeamDriversOnDuty() && !GlobalState.getInstance().getCompanyConfigSettings(this.getActivity()).getMultipleUsersAllowed()) {
					this.startActivity(TeamDriverFirstDriver.class);
					this.finish();
				} else {
					//if there's only one user logged in then check if odometer calibration is required
					if (GlobalState.getInstance().getLoggedInUserList().size() <= 1 && GlobalState.getInstance().getTeamDriverMode() != GlobalState.TeamDriverModeEnum.NONE) {
						OdometerCalibrationRequiredTask calibrationTask = new OdometerCalibrationRequiredTask(this, MandateObjectFactory.getInstance(this,GlobalState.getInstance().getFeatureService()).getCurrentEventController());
						calibrationTask.execute();
					} else {
						//both users logged in, don't need them to choose which one is starting to drive.
						this.startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
						this.finish();
					}
				}
			}
			else
			{
                this.startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
                this.finish();
			}
		}
		else
		{
			Intent intent = new Intent(this, TripInfo.class);
			intent.putExtra(this.getResources().getString(R.string.extra_tripinfomsg), this.getString(R.string.extra_tripinfomsg));
			intent.putExtra(getString(R.string.extra_isloginprocess), true);
			intent.putExtra(getString(R.string.extra_teamdriverlogin), _isTeamDriver);

			startActivity(intent);
			finish();
		}
	}

	public LogEntryController getMyLogEntryController() {
		if (_logEntryController == null) {
			_logEntryController = new LogEntryController(this);
		}
		return _logEntryController;
	}

	public EmployeeLogEldMandateController getMyEmployeeLogELDMandateController() {
		if (_employeeLogEldMandateController == null) {
			_employeeLogEldMandateController = new EmployeeLogEldMandateController(this);
		}
		return _employeeLogEldMandateController;
	}

	@Override
	public boolean isCurrentLogCreated(){
		Date now = TimeKeeper.getInstance().getCurrentDateTime().toDate();
		return  getMyEmployeeLogELDMandateController().isLogCreatedForDate(now);
	}

	private static final List<Integer> VALID_INITIAL_DUTY_STATUS_LIST = Arrays.asList(new Integer[] {DutyStatusEnum.OFFDUTY, DutyStatusEnum.ONDUTY, DutyStatusEnum.SLEEPER});

	@Override
	public String getSuggestedInitialDutyStatus() {
		String suggestion = null;
		Date now = TimeKeeper.getInstance().getCurrentDateTime().toDate();
		EmployeeLog yesterdayLog = getMyEmployeeLogELDMandateController().GetLocalEmployeeLog(GlobalState.getInstance().getCurrentUser(), DateUtility.AddDays(now, -1));
		if (yesterdayLog != null) {
			EmployeeLogEldEvent yesterdaysLastStatusChange = EmployeeLogUtilities.getLastActiveDutyStatusChange(yesterdayLog);
			if(yesterdaysLastStatusChange != null && VALID_INITIAL_DUTY_STATUS_LIST.contains(yesterdaysLastStatusChange.getEventCode())) {
				suggestion = yesterdaysLastStatusChange.getDutyStatusEnum().toFriendlyName();
			}
		}
		return  suggestion;
	}

	public void handleSubmitButtonClick() {
		DutyStatusEnum dutyStatus = getDutyStatusEnum();
		DutyStatusEnum startOfLogStatus = null;

		String initialDutyStatus = _contentFrag.getInitialDutyStatus();
		if (initialDutyStatus != null) {
			startOfLogStatus = DutyStatusEnum.valueOfDMOEnum(initialDutyStatus);
		}

        if (_contentFrag.getLinearLayoutAnnotationOnDuty().getVisibility() == View.VISIBLE && _contentFrag.getAnnotationOnDuty().getText().toString().trim().length() < 4) {
            _contentFrag.showAnnotationErrorMessage();
            return;
        } else {
            _contentFrag.hideErrorMessage();
        }

        if (_contentFrag.getLinearLayoutAnnotationOffDuty().getVisibility() == View.VISIBLE && _contentFrag.getAnnotationOffDuty().getText().toString().trim().length() < 4) {
            _contentFrag.showAnnotationErrorMessage();
            return;
        } else {
            _contentFrag.hideErrorMessage();
        }

		if (_contentFrag.getLocationEditText().getVisibility() == View.VISIBLE && _contentFrag.getLocationText().trim().length() < 5) {
			_contentFrag.showLocationMinimumLengthErrorMessage();
			return;
		}
		else {
			_contentFrag.hideErrorMessage();
		}

		LogEntryController logEntryCtrl = getMyLogEntryController();

		Date now = DateUtility.CurrentHomeTerminalTime(GlobalState.getInstance().getCurrentUser());
		boolean isSpecialDrivingSelected = setSpecialDutyStatus();
		if (isSpecialDrivingSelected) {
			logEntryCtrl.PerformManualStatusChange(now, dutyStatus, null, null, null,_contentFrag.getAnnotationOnDuty().getText().toString(), null, null, startOfLogStatus);
		}

		if (GlobalState.getInstance().getLoggedInUserList().size() > 1) {
            User newTeamDriver = GlobalState.getInstance().getCurrentUser();
		    try {
                if (GlobalState.getInstance().getTeamDriverMode() != GlobalState.TeamDriverModeEnum.NONE) {
		            //This is a normal team driver scenario
                    getMyController().LoginTeamDriver(newTeamDriver, now, dutyStatus, _exemptLogType, startOfLogStatus);
                }
            } catch (KmbApplicationException e) {
                HandleException(e);
            }
        }
        //loads or creates current log.
		EmployeeLog emplLog = logEntryCtrl.CreateCurrentLog(startOfLogStatus, dutyStatus, _exemptLogType);

		// Update location information
		EmployeeLogEldEvent logEvent = getMyLogEntryController().getLogEventForEdit();
		logEvent.setLatitudeStatusCode(getIntent().getStringExtra(this.getResources().getString(R.string.extra_lat_lon_status)));
		logEvent.setLongitudeStatusCode(logEvent.getLatitudeStatusCode());
		if (getIntent().hasExtra(this.getResources().getString(R.string.extra_latitude))) {
			logEvent.setLatitude(getIntent().getExtras().getDouble(this.getResources().getString(R.string.extra_latitude)));
		}
		if (getIntent().hasExtra(this.getResources().getString(R.string.extra_Longitude))) {
			logEvent.setLongitude(getIntent().getExtras().getDouble(this.getResources().getString(R.string.extra_Longitude)));
		}
		if (_contentFrag.getLocationText().length() > 0) {
			this.getMyLogEntryController().UpdateCurrentEventLocation(_contentFrag.getLocationText(), true);
		}
		getMyLogEntryController().setLogEventForEdit(logEvent);
		getMyLogEntryController().UpdateCurrentLog(emplLog);

        if (_exemptLogType.getValue() != ExemptLogTypeEnum.NULL) {
            EmployeeLogDownloader downloader = new EmployeeLogDownloader(this);
            downloader.DownloadLogs();
        } else {
            Return();
        }
    }

    public void HandleDutyStatusChange(String dutyStatus) {
        setDutyStatusEnum(dutyStatus);
        _contentFrag.hideErrorMessage();

        if (dutyStatus.equals(getResources().getString(R.string.offduty)) && isPersonalConveyanceAvailable()) {
            _contentFrag.getPersonalConveyanceCheckbox().setVisibility(View.VISIBLE);

        } else {
            _contentFrag.getPersonalConveyanceCheckbox().setVisibility(View.GONE);
            _contentFrag.getPersonalConveyanceCheckbox().setChecked(false);
            _contentFrag.hideAnnotationOffDuty();
        }

        if (dutyStatus.equals(getResources().getString(R.string.onduty)) && isHyrailUseAllowed()) {
            _contentFrag.getUtilizeHyrailCheckbox().setVisibility(View.VISIBLE);
        } else {
            _contentFrag.getUtilizeHyrailCheckbox().setVisibility(View.GONE);
            _contentFrag.getUtilizeHyrailCheckbox().setChecked(false);
        }

        if (dutyStatus.equals(getResources().getString(R.string.onduty)) && isYardMoveUseAllowed()) {
            _contentFrag.getYardMoveCheckbox().setVisibility(View.VISIBLE);
        } else {
            _contentFrag.getYardMoveCheckbox().setVisibility(View.GONE);
            _contentFrag.getYardMoveCheckbox().setChecked(false);
            _contentFrag.hideAnnotationOnDuty();
        }

		if (dutyStatus.equals(getResources().getString(R.string.onduty)) && isNonRegDrivingAllowed()) {
			_contentFrag.getNonRegDrivingCheckbox().setVisibility(View.VISIBLE);
		} else {
			_contentFrag.getNonRegDrivingCheckbox().setVisibility(View.GONE);
			_contentFrag.getNonRegDrivingCheckbox().setChecked(false);
		}

	}

    public void HandlePersonalConveyanceCheckBoxClick() {
        _contentFrag.hideErrorMessage();
        if (_contentFrag.getPersonalConveyanceCheckbox().isChecked()
				&& GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
            _contentFrag.showAnnotationOffDuty();
        } else {
            _contentFrag.hideAnnotationOffDuty();
        }
    }

	public void HandleHyrailCheckBoxClick() {
		_contentFrag.hideErrorMessage();
		if (_contentFrag.getUtilizeHyrailCheckbox().isChecked()) {
			_contentFrag.getYardMoveCheckbox().setChecked(false);
			_contentFrag.getNonRegDrivingCheckbox().setChecked(false);
			_contentFrag.hideAnnotationOnDuty();
		}
	}

	public void HandleYardMoveCheckBoxClick() {
        _contentFrag.hideErrorMessage();
        if (_contentFrag.getYardMoveCheckbox().isChecked()) {
            _contentFrag.getUtilizeHyrailCheckbox().setChecked(false);
			_contentFrag.getNonRegDrivingCheckbox().setChecked(false);
            _contentFrag.showAnnotationOnDuty();
        } else {
            _contentFrag.hideAnnotationOnDuty();
        }
    }

	public void HandleNonRegDrivingCheckBoxClick() {
		_contentFrag.hideErrorMessage();
		if (_contentFrag.getNonRegDrivingCheckbox().isChecked()) {
			_contentFrag.getUtilizeHyrailCheckbox().setChecked(false);
			_contentFrag.getYardMoveCheckbox().setChecked(false);
			_contentFrag.hideAnnotationOnDuty();
		}
	}

    private boolean isPersonalConveyanceAvailable() {
		return GlobalState.getInstance().getFeatureService().getPersonalConveyanceEnabled()
				&& this.getMyController().getCurrentUser().getIsPersonalConveyanceAllowed();
	}

	private boolean isHyrailUseAllowed() {
		return GlobalState.getInstance().getFeatureService().getHyrailEnabled()
				&& this.getController().getCurrentUser().getIsHyrailAllowed();
	}

	private boolean isYardMoveUseAllowed() {
		return (this.getController().getCurrentUser().getYardMoveAllowed() && GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled());
	}

	private boolean isNonRegDrivingAllowed() {
		return GlobalState.getInstance().getFeatureService().getNonRegDrivingEnabled()
				&& this.getMyController().getCurrentUser().getIsNonRegDrivingAllowed();
	}

	public boolean ShouldShowManualLocation(){
		ELDCommon eldCommon = new ELDCommon(getIntent(),this.getResources(),getMyLogEntryController(),getMyEmployeeLogELDMandateController());
		return eldCommon.ShouldShowManualLocation();
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

				setLeftNavFragment(new LeftNavImgFrag(R.layout.leftnav_item_imageandtext));


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
	public String getActivityMenuItemList()
	{
		if (_isTeamDriver)
		{
			if (_isSharedDevice || GlobalState.getInstance().getTeamDriverMode() == GlobalState.TeamDriverModeEnum.NONE)
			{
				return getString(R.string.tripinfo_actionitems_shareddevice);
			}
			else
			{
				return getString(R.string.tripinfo_actionitems_separatedevice);
			}
		}
		else
			return null;
	}

	public String getActivityMenuIconList()
	{
		if (_isTeamDriver)
		{
			if (_isSharedDevice || GlobalState.getInstance().getTeamDriverMode() == GlobalState.TeamDriverModeEnum.NONE)
			{
				return getString(R.string.tripinfo_actionitemsicons_shareddevice);
			}
			else
			{
				return getString(R.string.tripinfo_actionitemsicons_separatedevice);
			}
		}
		else
			return null;
	}

	public void ShowConfirmationMessage(String message, final Runnable yesAction, final Runnable noAction) {
		ShowConfirmationMessage(this, message,
			new ShowMessageClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int id) {
					super.onClick(dialog, id);

					if (yesAction != null)
						yesAction.run();
				}
			},

			new ShowMessageClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int id) {
					super.onClick(dialog, id);

					if (noAction != null)
						noAction.run();
				}
			}
		);
	}

	public BaseActivity getActivity() {
		return this;
	}

	public void onLogDownloadFinished(boolean logsFound) {
		if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()){
			FixLogEntries();
			StartOdometerCalibration();
		}
		else {
			Return();
		}
	}

	public void onOffDutyLogsCreated(boolean logsCreated) {
		if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()){
			FixLogEntries();
			StartOdometerCalibration();
		}
		else {
			Return();
		}
	}

	private void StartOdometerCalibration() {
		OdometerCalibrationRequiredTask calibrationTask = new OdometerCalibrationRequiredTask(this, MandateObjectFactory.getInstance(this, GlobalState.getInstance().getFeatureService()).getCurrentEventController());
		calibrationTask.execute();
	}

	public void OnOdometerCalibrationRequired(boolean calibrationRequired) {
        if (calibrationRequired) {
            /* Display OdometerCalibration activity */
	        Bundle extras = new Bundle();
	        extras.putBoolean(getString(R.string.extra_displayoffdutylogs), true);
        	extras.putBoolean(getString(R.string.extra_isloginprocess), true);
        	extras.putBoolean(getString(R.string.extra_teamdriverlogin), _isTeamDriver);

	        startActivity(OdometerCalibration.class, Intent.FLAG_ACTIVITY_CLEAR_TOP, extras);
        } else {
            if (_isTeamDriver) {
                if (GlobalState.getInstance().getLoggedInUserList().size() <= 1) {
                    Bundle extras = new Bundle();
                    extras.putBoolean(getString(R.string.extra_teamdriverlogin), true);

                    if (GlobalState.getInstance().getTeamDriverMode() == GlobalState.TeamDriverModeEnum.SHAREDDEVICE)
                        startActivity(TeamDriverNextStep.class, extras);
                    else
                        startActivity(TeamDriverAddDriver.class);

                } else {

					TeamDriverController ctrlr = getMyController();

                    if (ctrlr.IsTeamDriversOnDuty() && !GlobalState.getInstance().getCompanyConfigSettings(this.getActivity()).getMultipleUsersAllowed())
					{
						//both on-duty.
						this.startActivity(TeamDriverFirstDriver.class, Intent.FLAG_ACTIVITY_SINGLE_TOP);
					}
					else if(!ctrlr.IsTeamDriversOnDuty())
					{
						//one or none are on duty.
						ctrlr.SetTeamDriver();
						startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_SINGLE_TOP);
					}
					else
					{
						startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_SINGLE_TOP);
					}
                }
            } else {
                startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_SINGLE_TOP);
            }
        }
        finish();
	}

	public boolean getLocalDataTask() {
		// return true if FetchLocalData is complete
		if(mFetchLocalDataTask == null)
			return true;
		else
			return false;
	}

    private void setDutyStatusEnum(String dutyStatus) {
        if (dutyStatus.equals(getResources().getString(R.string.offduty))) {
			_dutyStatusEnum = new DutyStatusEnum(DutyStatusEnum.OFFDUTY);
		} else if (dutyStatus.equals(getResources().getString(R.string.offdutywellsite))) {
			_dutyStatusEnum = new DutyStatusEnum(DutyStatusEnum.OFFDUTYWELLSITE);
        } else if (dutyStatus.equals(getResources().getString(R.string.onduty))) {
            _dutyStatusEnum = new DutyStatusEnum(DutyStatusEnum.ONDUTY);
        } else if (dutyStatus.equals(getResources().getString(R.string.sleeper))) {
            _dutyStatusEnum = new DutyStatusEnum(DutyStatusEnum.SLEEPER);
        } else {
            _dutyStatusEnum = new DutyStatusEnum(DutyStatusEnum.ONDUTY);
        }
    }

    public DutyStatusEnum getDutyStatusEnum() {
        if (_dutyStatusEnum == null)
            _dutyStatusEnum = new DutyStatusEnum(DutyStatusEnum.ONDUTY);
        return _dutyStatusEnum;
    }

    public boolean setSpecialDutyStatus() {
		boolean isSpecial = false;
        if (_contentFrag.getPersonalConveyanceCheckbox() != null && _contentFrag.getPersonalConveyanceCheckbox().isChecked()) {
			isSpecial = true;
			GlobalState.getInstance().setIsInPersonalConveyanceDutyStatus(true);
        } else if (_contentFrag.getUtilizeHyrailCheckbox() != null && _contentFrag.getUtilizeHyrailCheckbox().isChecked()) {
			isSpecial = true;
			GlobalState.getInstance().setIsInHyrailDutyStatus(true);
        } else if (_contentFrag.getYardMoveCheckbox() != null && _contentFrag.getYardMoveCheckbox().isChecked()) {
			isSpecial = true;
			GlobalState.getInstance().setIsInYardMoveDutyStatus(true);
		} else if (_contentFrag.getNonRegDrivingCheckbox() != null && _contentFrag.getNonRegDrivingCheckbox().isChecked()) {
			GlobalState.getInstance().setIsInNonRegDrivingDutyStatus(true);
			isSpecial = true;
		}
		return isSpecial;
	}

	private void FixLogEntries()
	{
		//we need to make sure we link back the user's login and initial duty status event with the log
		//since the user won't be going to TripInfo
		IAPIController empLogController = MandateObjectFactory.getInstance(this, GlobalState.getInstance().getFeatureService()).getCurrentEventController();

		try {
			empLogController.UpdateLoginEvent();
		} catch (Throwable throwable) {
			throwable.printStackTrace();
		}
	}

}
