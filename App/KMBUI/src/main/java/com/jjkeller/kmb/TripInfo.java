package com.jjkeller.kmb;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.jjkeller.kmb.fragments.LeftNavFrag;
import com.jjkeller.kmb.fragments.LeftNavImgFrag;
import com.jjkeller.kmb.fragments.TripInfoFrag;
import com.jjkeller.kmb.interfaces.ILogDownloaderHost;
import com.jjkeller.kmb.interfaces.IOdometerCalibrationRequiredHost;
import com.jjkeller.kmb.interfaces.ITripInfo.TripInfoFragActions;
import com.jjkeller.kmb.interfaces.ITripInfo.TripInfoFragControllerMethods;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.EOBR.EobrReader;
import com.jjkeller.kmbapi.controller.LogEntryController;
import com.jjkeller.kmbapi.controller.MotionPictureController;
import com.jjkeller.kmbapi.controller.TeamDriverController;
import com.jjkeller.kmbapi.controller.interfaces.IAPIController;
import com.jjkeller.kmbapi.controller.share.MandateObjectFactory;
import com.jjkeller.kmbapi.controller.utility.EmployeeLogUtilities;
import com.jjkeller.kmbapi.enums.CanadaDeferralTypeEnum;
import com.jjkeller.kmbapi.proxydata.EldEventAdapterList;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;
import com.jjkeller.kmbapi.proxydata.MotionPictureProduction;
import com.jjkeller.kmbui.R;

import java.util.List;

public class TripInfo extends BaseActivity 
						implements TripInfoFragControllerMethods, TripInfoFragActions, ILogDownloaderHost, IOdometerCalibrationRequiredHost,
						LeftNavFrag.OnNavItemSelectedListener, LeftNavFrag.ActivityMenuItemsListener,
						LeftNavImgFrag.ActivityMenuIconItems{
	TripInfoFrag _contentFrag;

	private boolean _isLogin;
	private boolean _isTeamDriver;
	private boolean _isSharedDevice = GlobalState.getInstance().getTeamDriverMode() == GlobalState.TeamDriverModeEnum.SHAREDDEVICE;
	private TeamDriverController _tdController;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.baselayout);

		mFetchLocalDataTask = new FetchLocalDataTask(this.getClass().getSimpleName());

        //Load controls fires off a async task that locks the screen.  so don't auto unlock.
        mFetchLocalDataTask.setAutoUnlockScreenRotationOnPostExecute(false);
		mFetchLocalDataTask.execute();

		_isLogin = getIntent().getBooleanExtra(getString(R.string.extra_isloginprocess), false);
		_isTeamDriver = getIntent().getBooleanExtra(getString(R.string.extra_teamdriverlogin), false);

	}

	// 10/25/2012 AMO: Adding function for when the actionbar home button is pressed
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// if the item is the home button
		if(item.getItemId() == android.R.id.home){
			// if the app is passed the login
			if (this.getController() != null && this.getController().getCurrentUser() != null && GlobalState.getInstance().getPassedRods() == true){
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
		if(success)
		{
			TeamDriverController ctrlr = new TeamDriverController(this.getActivity());
	        if (_isTeamDriver && GlobalState.getInstance().getLoggedInUserList().size() > 1 && ctrlr.IsTeamDriversOnDuty())
	        {
	        		this.startActivity(TeamDriverFirstDriver.class);
	        		this.finish();
        	}
        	else
        	{
        		if( !getIntent().hasExtra(this.getResources().getString(R.string.extra_tripinfomsg)) )
		        {
		        	// Clear the menu in the process of returning to the RodsEntry activity.
					/* Display rodsentry activity */
		        	Bundle extras = new Bundle();

			        this.startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_CLEAR_TOP, extras);
			        this.finish();
		        }
		        else
		        {
		        	mOdometerCalibrationRequiredTask = new OdometerCalibrationRequiredTask(this, getMyController());
		        	mOdometerCalibrationRequiredTask.execute();
		        }
        	}
		}

		else
        {
            if (!GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled())
			// previously the only reason to get an unsuccessful return code is when the weekly reset cannot be used on this log
			this.ShowMessage(this, this.getString(R.string.weeklyResetCannotBeUsed_message));
		}
    }

	public void handleCancelButtonClick() {
		//not implemented
	}

	public void handleOKButtonClick()
	{
		boolean validData = isValidData();


		if (validData)
		{
			mSaveLocalDataTask = new SaveLocalDataTask(this.getClass().getSimpleName());
			mSaveLocalDataTask.execute();
		}

	}


	public void handleSetTrailerInformationClick() {

    	SharedPreferences _userPref = this.getSharedPreferences(this.getString(R.string.sharedpreferencefile), 0);
		SharedPreferences.Editor editor = _userPref.edit();
		if(!_contentFrag.getDefaultTrailerInfo().isChecked())
		{
			// when clearing the checkbox, the remove the default settings
			editor.putString(getString(R.string.defaulttrailernumber), "");
			editor.putString(getString(R.string.defaulttrailerplate), "");
			editor.putBoolean(getString(R.string.setdefaulttrailernumber), false);

			//Enabled the ability to add Trailer Number
			_contentFrag.getTrailerTextView().setEnabled(true);
			_contentFrag.getTrailerPlateTextView().setEnabled(true);
		}
		else
		{
			TextView tvTrailer = _contentFrag.getTrailerTextView();
			TextView tvTrailerPlate = _contentFrag.getTrailerPlateTextView();
			editor.putString(getString(R.string.defaulttrailernumber), tvTrailer.getText().toString());
			editor.putString(getString(R.string.defaulttrailerplate), tvTrailerPlate.getText().toString());
			editor.putBoolean(getString(R.string.setdefaulttrailernumber), true);

			//Disable the ability to add Trailer Number
			_contentFrag.getTrailerTextView().setEnabled(false);
			_contentFrag.getTrailerPlateTextView().setEnabled(false);
		}

		editor.commit();
	}

	public void handleSetShipmentInformationClick() {

    	SharedPreferences _userPref = this.getSharedPreferences(this.getString(R.string.sharedpreferencefile), 0);
		SharedPreferences.Editor editor = _userPref.edit();
		if(!_contentFrag.getDefaultShipmentNumber().isChecked())
		{
			// when clearing the checkbox, the remove the default settings
			editor.putString(getString(R.string.defaultshipmentnumber), "");
			editor.putBoolean(getString(R.string.setdefaultshipmentnumber), false);

			//Enabled the ability to add Shipment Number
			_contentFrag.getShipmentInfoTextView().setEnabled(true);
		}
		else{
				TextView tvShipment = _contentFrag.getShipmentInfoTextView();
				editor.putString(getString(R.string.defaultshipmentnumber), tvShipment.getText().toString());
				editor.putBoolean(getString(R.string.setdefaultshipmentnumber), true);

				//Disable the ability to add Shipment Number
				_contentFrag.getShipmentInfoTextView().setEnabled(false);
			}

		editor.commit();
	}

	public void handleMotionPictureProductionSelect() {
		MotionPictureProduction production = (MotionPictureProduction)_contentFrag.getMotionPictureProductionSpinner().getSelectedItem();

		String authorityName = production.getMotionPictureAuthority().GetNameAndDOTNumber();
		_contentFrag.getMotionPictureAuthorityTextView().setText(authorityName);
	}

	@Override
	protected boolean saveData()
	{
		boolean isSuccessful = true;
		CanadaDeferralTypeEnum canadaDeferralType = new CanadaDeferralTypeEnum(CanadaDeferralTypeEnum.NONE);

		// Check if a selection was made from the off duty deferral dropdown
		if (_contentFrag.getDeferralSpinner().getSelectedItemPosition() >= 0)
        {
			canadaDeferralType = CanadaDeferralTypeEnum.valueOf(this, _contentFrag.getDeferralAdapter().getItem(_contentFrag.getDeferralSpinner().getSelectedItemPosition()).toString());
		}
		String productionId=null,authorityId=null;
		if (GlobalState.getInstance().getCompanyConfigSettings(this).getIsMotionPictureEnabled() && _contentFrag.getMotionPictureProductionSpinner().getSelectedItem() != null){
			MotionPictureProduction production = (MotionPictureProduction)_contentFrag.getMotionPictureProductionSpinner().getSelectedItem();
			productionId=production.getMotionPictureProductionId();
			authorityId=production.getMotionPictureAuthorityId();
			GlobalState.getInstance().set_currentMotionPictureAuthorityId( authorityId);
			GlobalState.getInstance().set_currentMotionPictureProductionId(productionId);
		}

        String trailerNumbers,trailerPlate,tractorNumbers,shipmentInfo,vehiclePlate;

		trailerNumbers = _contentFrag.getTrailerTextView().getText().toString();
		trailerPlate =  _contentFrag.getTrailerPlateTextView().getText().toString();
		vehiclePlate =  _contentFrag.getVehiclePlateTextView().getText().toString();
		shipmentInfo = _contentFrag.getShipmentInfoTextView().getText().toString();
		tractorNumbers =  _contentFrag.getTractorNumberTextView().getText().toString();

		this.getMyController().SaveTripInfo(trailerNumbers,trailerPlate,shipmentInfo ,vehiclePlate, _contentFrag.getReturnToWorkLocationCheckbox().isChecked(), canadaDeferralType, _contentFrag.getIsHaulingExplosivesCheckbox().isChecked(), _contentFrag.getIsOperatesSpecificVehiclesForOilField().isChecked(), _contentFrag.getIs30MinRestBreakExemptCheckbox().isChecked(),tractorNumbers, authorityId, productionId);

		this.updateLoginEvent();

		//2016.05.10 Add these values to the GlobalState for use in ELDEvent generation
		GlobalState.getInstance().set_currentTrailerNumbers(trailerNumbers);
		GlobalState.getInstance().set_currentTrailerPlate(trailerPlate);
		GlobalState.getInstance().set_currentVehiclePlate(vehiclePlate);
		GlobalState.getInstance().set_currentShipmentInfo(shipmentInfo);
		GlobalState.getInstance().set_currentTractorNumbers(tractorNumbers);

		if(_contentFrag.getIsWeeklyResetUsedCheckbox().getVisibility() == View.VISIBLE){
			// the weekly reset checkbox is up, so process it
			boolean isResetUsed = _contentFrag.getIsWeeklyResetUsedCheckbox().isChecked();
			if(isResetUsed){
				if(this.getMyController().IsValidToUseWeeklyResetOnCurrentLog()){
					this.getMyController().SaveWeeklyResetUsed(true);
				}
				else{
					// see the Return method for the error thrown when the var below is false
					isSuccessful = false;
				}
			}
			else{
				this.getMyController().SaveWeeklyResetUsed(false);
			}
		}

		return isSuccessful;
	}

	@Override
	protected void loadControls() {
		super.loadControls();
		loadContentFragment(new TripInfoFrag());

		if(_isLogin) {
			EmployeeLogDownloader downloader = new EmployeeLogDownloader(this);
			downloader.DownloadLogs();
		} else {
            UnlockScreenRotation();
        }

		if(_isTeamDriver) {
			loadLeftNavFragment();
		}
	}

	@Override
	protected void InitController() {
		IAPIController empLogCtrl = MandateObjectFactory.getInstance(this,GlobalState.getInstance().getFeatureService()).getCurrentEventController();

		this.setController(empLogCtrl);

		_tdController = new TeamDriverController(this);

	}

	public IAPIController getMyController()
	{
		return (IAPIController) this.getController();
	}

	public TeamDriverController getTeamDriverController()
	{
		return _tdController;
	}

	@Override
	public void setFragments()
	{
		super.setFragments();

		Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
		_contentFrag = (TripInfoFrag)f;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (GlobalState.getInstance().getPassedRods() == true)
			this.CreateOptionsMenu(menu, false);
		return true;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		// 10/3/11 JHM - Don't store state (likely blank values) if the FetchLocalData task hasn't completed.
		if(mFetchLocalDataTask == null)
		{
			TextView tvTrailer = (TextView)findViewById(R.id.txtTrailer);
			outState.putCharSequence(getResources().getString(R.string.state_trailer), tvTrailer.getText());

			TextView tvTrailerPlate = (TextView)findViewById(R.id.txtTrailerPlate);
			outState.putCharSequence(getResources().getString(R.string.state_trailerplate), tvTrailerPlate.getText());

            TextView tvTractorNumber = (TextView) findViewById(R.id.txtTractorNumber);
            outState.putCharSequence(getResources().getString(R.string.state_tractornumber), tvTractorNumber.getText());


			TextView tvShipment = (TextView)findViewById(R.id.txtShipmentInfo);
			outState.putCharSequence(getResources().getString(R.string.state_shipment), tvShipment.getText());

			TextView tvVehiclePlate = (TextView)findViewById(R.id.txtVehiclePlate);
			outState.putCharSequence(getResources().getString(R.string.state_vehicleplate), tvVehiclePlate.getText());

			CheckBox chkReturnToWorkLocation = (CheckBox)findViewById(R.id.chkReturnToWorkLocation);
			outState.putBoolean(getResources().getString(R.string.state_returntoworklocation), chkReturnToWorkLocation.isChecked());

			CheckBox chkIsHaulingExplosives = (CheckBox)findViewById(R.id.chkIsHaulingExplosives);
			outState.putBoolean(getResources().getString(R.string.state_ishaulingexplosives), chkIsHaulingExplosives.isChecked());

			CheckBox chkIs30MinRestBreakExempt = (CheckBox)findViewById(R.id.chkIs30MinRestBreakExempt);
			outState.putBoolean(getResources().getString(R.string.state_isExemptFrom30MinBreakRequirement), chkIs30MinRestBreakExempt.isChecked());

			CheckBox chkIsOperatesSpecificVehiclesForOilField = (CheckBox)findViewById(R.id.chkIsOperatesSpecificVehicleForOilField);
			outState.putBoolean(getResources().getString(R.string.state_isoperatesspecificvehiclesforoilfield), chkIsOperatesSpecificVehiclesForOilField.isChecked());
		}

		super.onSaveInstanceState(outState);

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		//Handle the back button like a cancel during login
		if (keyCode == KeyEvent.KEYCODE_BACK && getIntent().hasExtra(this.getResources().getString(R.string.extra_tripinfomsg))) {
            if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled())
                Return(false);
            else
			    Return(true);

			// Say that we've consumed the event
			return true;
		}

		// Otherwise let system handle keypress normally
		return super.onKeyDown(keyCode, event);
	}

	private Bundle createExtras()
	{
        Bundle extras = new Bundle();

        // if not team driver login, don't download logs and prompt for off duty logs on rods entry,
        // otherwise if team driver login, then download logs and prompt for off duty logs
        if (!getIntent().hasExtra(this.getString(R.string.extra_teamdriverlogin)))
        	extras.putBoolean(this.getString(R.string.extra_displayoffdutylogs), false);
        else
        	extras.putBoolean(this.getString(R.string.extra_displayoffdutylogs), true);

        return extras;
	}

	private static OdometerCalibrationRequiredTask mOdometerCalibrationRequiredTask;

	public void ShowConfirmationMessage(String message,
			final Runnable yesAction,
			final Runnable noAction) {

		ShowConfirmationMessage(this, message,
			new ShowMessageClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int id) {
					super.onClick(dialog, id);

					if(yesAction != null)
						yesAction.run();
				}
			},

			new ShowMessageClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int id) {
					super.onClick(dialog, id);

					if(noAction != null)
						noAction.run();
				}
			}
		);
	}

	public BaseActivity getActivity() {
		return this;
	}

	public void onLogDownloadFinished(boolean logsFound) {
        UnlockScreenRotation();
	}

	public void onOffDutyLogsCreated(boolean logsCreated) {
		UnlockScreenRotation();
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
		if(_isTeamDriver)
		{
			if(_isSharedDevice || GlobalState.getInstance().getTeamDriverMode() == GlobalState.TeamDriverModeEnum.NONE) {
				return getString(R.string.tripinfo_actionitems_shareddevice);				
			} else
			{
				return getString(R.string.tripinfo_actionitems_separatedevice);
			}
		}
		else 
			return null;
	}
	

	public String getActivityMenuIconList()
	{
		if(_isTeamDriver)
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

	public void OnOdometerCalibrationRequired(boolean calibrationRequired) {
        if(calibrationRequired && GlobalState.getInstance().getLoggedInUserList().size() <= 1){
        	/* Display OdometerCalibration activity */ 
	        Bundle extras = new Bundle();
	        extras.putBoolean(getString(R.string.extra_displayoffdutylogs), true);
	        
	        if(_isLogin)
	        	extras.putBoolean(getString(R.string.extra_isloginprocess), true);
	        if(_isTeamDriver)
	        	extras.putBoolean(getString(R.string.extra_teamdriverlogin), true);
	        
	        startActivity(OdometerCalibration.class, Intent.FLAG_ACTIVITY_CLEAR_TOP, extras);
        }
       	else
        {
       		if(_isLogin && _isTeamDriver && GlobalState.getInstance().getLoggedInUserList().size() <= 1)
       		{
    			Bundle extras = new Bundle();
       			
		        if(_isTeamDriver)
		        	extras.putBoolean(getString(R.string.extra_teamdriverlogin), true);
		        
		        if(GlobalState.getInstance().getTeamDriverMode() == GlobalState.TeamDriverModeEnum.SHAREDDEVICE)
		        	startActivity(TeamDriverNextStep.class, extras);	
        		else if(GlobalState.getInstance().getTeamDriverMode() == GlobalState.TeamDriverModeEnum.SEPARATEDEVICE)
        			startActivity(TeamDriverAddDriver.class);
				else
					 //Multiple User scenario
					startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_SINGLE_TOP, extras);
       		}
       		else
            {
       			
       			// Check for TeamDriver Where both Drivers are not currently on duty.
       			if(_isTeamDriver && !_tdController.IsTeamDriversOnDuty()) {
                    _tdController.SetTeamDriver();
                }
       			Bundle extras = new Bundle();

	        	 /* Display RodsEntry activity */ 
	        	startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_SINGLE_TOP, extras);
       		}
       	}
        finish();		
	}

	public void loadMotionPicture(){
		if (GlobalState.getInstance().getCompanyConfigSettings(getBaseContext()).getIsMotionPictureEnabled()){
			MotionPictureController controller = new MotionPictureController(getBaseContext());
			List<MotionPictureProduction> _motionPictureProductions = controller.GetActiveMotionPictureProductions();
			LogEntryController logEntryController = new LogEntryController(getBaseContext());

			if (_motionPictureProductions.size() > 1){
				ArrayAdapter<MotionPictureProduction> spinnerAdapter = new ArrayAdapter<>(this, R.layout.kmb_spinner_item, _motionPictureProductions);
				String productionId =  _tdController.getCurrentDesignatedDriver().getUserState().getMotionPictureProductionId();
				if (productionId == null){
					EmployeeLog empLog = logEntryController.getCurrentDriversLog();
					EmployeeLogEldEvent eldEvent=  EmployeeLogUtilities.GetLastEventInLogWithMotionPictureInfo(empLog);
					if (eldEvent != null)
						productionId = eldEvent.getMotionPictureProductionId();
				}
				spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				_contentFrag.getMotionPictureProductionSpinner().setAdapter(spinnerAdapter);


				if (productionId != null && productionId.length() > 0) {
					for (int i=0; i < spinnerAdapter.getCount(); i++){
						MotionPictureProduction production = spinnerAdapter.getItem(i);
						if (productionId.equals(production.getMotionPictureProductionId())){
							_contentFrag.getMotionPictureProductionSpinner().setSelection(i);
							_contentFrag.getMotionPictureAuthorityTextView().setText(production.getMotionPictureAuthority().GetNameAndDOTNumber());
							break;
						}
					}
				}
			}
		}
	}

	public void updateLoginEvent() {
		if(this._isLogin) {
			IAPIController empLogController = MandateObjectFactory.getInstance(this, GlobalState.getInstance().getFeatureService()).getCurrentEventController();
			try {
				empLogController.UpdateLoginEvent();

			} catch (Throwable e) {
				Log.e("UnhandledCatch", "Failed to update login event", e);
			}
		}
	}

	private boolean isValidData() {

		boolean validData = true;

		// data is not valid if oilfield specific setting is not checked and the off duty wellsite status is used on the log
		if(_contentFrag.getIsOperatesSpecificVehiclesForOilField().getVisibility() == View.VISIBLE
				&& !_contentFrag.getIsOperatesSpecificVehiclesForOilField().isChecked()
				&& getMyController().IsUSOilFieldOffDutyStatusInLog())
		{
			this.ShowMessage(this, this.getResources().getString(R.string.msg_oilfieldsetting_cannotbeunselected));
			validData = false;
		}

		EobrReader instance = EobrReader.getInstance();
		boolean eobrConnected = instance.getCurrentConnectionState() == EobrReader.ConnectionState.ONLINE;
		boolean unitNumberEmpty = (_contentFrag.getTractorNumberTextView().getText().toString()).isEmpty() ||  _contentFrag.getTractorNumberTextView().getText() == null;
		//unit number cannot be empty when connected to device
		if(eobrConnected && unitNumberEmpty) {

			this.ShowMessage(this, this.getResources().getString(R.string.msg_unitnumbersetting_isrequired_when_connected));
			validData = false;
		}

		return validData;
	}

}
