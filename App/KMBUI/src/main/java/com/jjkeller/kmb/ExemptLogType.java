package com.jjkeller.kmb;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.actionbarsherlock.view.Menu;
import com.jjkeller.kmb.fragments.ExemptLogTypeFrag;
import com.jjkeller.kmb.fragments.LeftNavFrag;
import com.jjkeller.kmb.fragments.LeftNavImgFrag;
import com.jjkeller.kmb.interfaces.IExemptLogType.ExemptLogTypeFragActions;
import com.jjkeller.kmb.interfaces.IExemptLogType.ExemptLogTypeFragControllerMethods;
import com.jjkeller.kmb.interfaces.ILogDownloaderHost;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmbapi.calcengine.Enums;
import com.jjkeller.kmbapi.common.TimeKeeper;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.LogEntryController;
import com.jjkeller.kmbapi.controller.LoginController;
import com.jjkeller.kmbapi.controller.interfaces.IAPIController;
import com.jjkeller.kmbapi.controller.share.MandateObjectFactory;
import com.jjkeller.kmbapi.enums.DutyStatusEnum;
import com.jjkeller.kmbapi.enums.ExemptLogTypeEnum;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbui.R;

import org.joda.time.DateTime;

public class ExemptLogType extends BaseActivity 
							implements ExemptLogTypeFragActions, ExemptLogTypeFragControllerMethods,
							ILogDownloaderHost,
							LeftNavFrag.OnNavItemSelectedListener, LeftNavFrag.ActivityMenuItemsListener,
							LeftNavImgFrag.ActivityMenuIconItems{	
	ExemptLogTypeFrag _contentFrag;

	private ExemptLogType _activity = null;
	private Intent nextActivity;
	private boolean _isTeamDriver = false;
	private boolean _isSharedDevice = GlobalState.getInstance().getTeamDriverMode() == GlobalState.TeamDriverModeEnum.SHAREDDEVICE;
	protected boolean _loginProcess = false;
	private EmployeeLog _employeeLog = null;
	private int _logType = ExemptLogTypeEnum.NULL;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.baselayout);

		mFetchLocalDataTask = new FetchLocalDataTask(this.getClass().getSimpleName());
		mFetchLocalDataTask.execute();
		
		if(GlobalState.getInstance().getTeamDriverMode() == GlobalState.TeamDriverModeEnum.NONE)
			_isTeamDriver = false;
		else
			_isTeamDriver = true;
		
		_loginProcess = getIntent().hasExtra(this.getResources().getString(R.string.extra_isloginprocess));
		_activity = this;
	}

	@Override
	protected void loadControls()
	{
		super.loadControls();
		loadContentFragment(new ExemptLogTypeFrag());
		
		if (_isTeamDriver) {
			loadLeftNavFragment();
		}

		_employeeLog = this.getMyController().GetLocalEmployeeLog(DateTime.now().toDate());

		if(_employeeLog == null)
		{
			_employeeLog = this.getMyController().GetLocalEmployeeLogOrCreateNew(GlobalState.getInstance().getCurrentUser(),DateTime.now().toDate(),new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
			_employeeLog.setExemptLogType(new ExemptLogTypeEnum(ExemptLogTypeEnum.UNDEFINED));
			_employeeLog.setMobileStartTimestamp(TimeKeeper.getInstance().getCurrentDateTime().toDate());
			this.getMyController().SaveLocalEmployeeLog(_employeeLog);
		}

		if(GlobalState.getInstance().getCurrentUser().getExemptFromEldUse()){
			SetCurrentLogExemptFromELDUse(true);
			ShowExemptFromELDUseMessage(false);
		}
	}
	
	@Override
	public void setFragments()
	{
		super.setFragments();

		Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
		_contentFrag = (ExemptLogTypeFrag)f;
	}

	private LoginController _loginController = null;
	public LoginController getMyLoginController()
	{
		return _loginController;
	}
	public void setMyLoginController( LoginController ctrlr)
	{
		_loginController = ctrlr;
	}

	public IAPIController getMyController()
	{
		return (IAPIController) this.getController();
	}

	@Override
	protected void InitController() {
		LoginController loginCtrl = new LoginController(this);
		this.setMyLoginController(loginCtrl);

		IAPIController empLogCtrl = MandateObjectFactory.getInstance(this,GlobalState.getInstance().getFeatureService()).getCurrentEventController();
		this.setController(empLogCtrl);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		if (GlobalState.getInstance().getPassedRods() == true)
			this.CreateOptionsMenu(menu, false);	
		return true;
	}
	
	@Override
    protected void Return()
	{
		if(nextActivity != null)
			startActivity(nextActivity);
		
		this.finish();
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
		if(_isTeamDriver || GlobalState.getInstance().getLoggedInUserList().size() > 1)
		{
			if(_isSharedDevice || GlobalState.getInstance().getTeamDriverMode() == GlobalState.TeamDriverModeEnum.NONE)
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

	public BaseActivity getActivity() {
		return this;
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

	public void onLogDownloadFinished(boolean logsFound) {
		if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()){
			FixLogEntries();
			TransferToRODS();
			//StartOdometerCalibration();
		}
		else {
			Return();
		}
	}

	public void onOffDutyLogsCreated(boolean logsCreated) {
		if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()){
			FixLogEntries();
			TransferToRODS();
			//StartOdometerCalibration();
		}
		else {
			Return();
		}
	}

	private void TransferToRODS()
	{
		nextActivity = null;
		Intent activity = new Intent(getApplicationContext(), RodsEntry.class);
		activity.putExtra(getResources().getString(R.string.extra_selectedDutyStatus), DutyStatusEnum.OFFDUTY);
		activity.putExtra(getResources().getString(R.string.extra_IsExemptFromELDUse), _logType == 2);
		activity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(activity);
		finish();
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

	public String getActivityMenuIconList()
	{
		if(_isTeamDriver || GlobalState.getInstance().getLoggedInUserList().size() > 1)
		{
			if(_isSharedDevice || GlobalState.getInstance().getTeamDriverMode() == GlobalState.TeamDriverModeEnum.NONE)
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

	private void ShowExemptFromELDUseMessage(boolean showCancelButton) {

		if(showCancelButton) {
			this.ShowConfirmationWithCancelMessage(this,
					R.string.exempt_from_eld_dialog_title,
					getString(R.string.exempt_from_eld_dialog_message_2),
					new ShowMessageClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							super.onClick(dialog, id);
							SetCurrentLogExemptFromELDUse(true);
							DownloadLogs();
						}
					},
					new ShowMessageClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							super.onClick(dialog, id);
						}
					}
			);
		}
		else
		{
			this.ShowMessage(this,
					getString(R.string.exempt_from_eld_dialog_title),
					getString(R.string.exempt_from_eld_dialog_message_2),
					new ShowMessageClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							super.onClick(dialog, id);
							DownloadLogs();
						}
					}
			);
		}
	}

	private void SetCurrentLogExemptFromELDUse (boolean exemptFromELDUse)
	{
		_employeeLog.setIsExemptFromELDUse(exemptFromELDUse);
		UpdateCurrentLog(_employeeLog);
	}

	private void UpdateCurrentLog(EmployeeLog emplLog)
	{
		this.getMyController().SaveLocalEmployeeLog(emplLog);
		GlobalState.getInstance().setCurrentEmployeeLog(emplLog);
	}

	private void DownloadLogs ()
	{
		EmployeeLogDownloader downloader = new EmployeeLogDownloader(this);
		downloader.DownloadLogs();
	}

	public void handleOKButtonClick() {
		_logType = _contentFrag.getDesignatedLogTypeRadioButtonIndex();

		if(_logType == 2) {
			ShowExemptFromELDUseMessage(true);
		}
		else {
			if (_logType == 1)
				nextActivity = new Intent(this, ExemptLogRequirements.class);
			else {
				if (_isTeamDriver || GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
					nextActivity = new Intent(this, SelectDutyStatus.class);
					nextActivity.putExtra(this.getResources().getString(R.string.extra_exemptlogtype), ExemptLogTypeEnum.NULL);
				} else {
					nextActivity = new Intent(this, SelectDutyStatus.class);
				}
			}

			nextActivity.putExtra(this.getResources().getString(R.string.extra_tripinfomsg), this.getString(R.string.extra_tripinfomsg));

			if(_loginProcess)
				nextActivity.putExtra(getString(R.string.extra_isloginprocess), true);

			Return();
		}
	}

	
}
