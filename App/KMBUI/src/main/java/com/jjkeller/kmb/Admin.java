package com.jjkeller.kmb;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.SeekBar;

import com.actionbarsherlock.view.Menu;
import com.jjkeller.kmb.fragments.AdminFrag;
import com.jjkeller.kmb.interfaces.IAdmin.AdminFragActions;
import com.jjkeller.kmb.interfaces.IAdmin.AdminFragControllerMethods;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmbapi.common.TimeKeeper;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.AdminController;
import com.jjkeller.kmbapi.controller.DataUsageController;
import com.jjkeller.kmbapi.controller.EOBR.EobrReader;
import com.jjkeller.kmbapi.controller.GeotabController;
import com.jjkeller.kmbapi.controller.LoginController;
import com.jjkeller.kmbapi.controller.dataaccess.AbstractDBAdapter;
import com.jjkeller.kmbapi.controller.dataaccess.EmployeeLogEldEventFacade;
import com.jjkeller.kmbapi.controller.interfaces.IAPIController;
import com.jjkeller.kmbapi.controller.share.MandateObjectFactory;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.controller.utility.EmployeeLogUtilities;
import com.jjkeller.kmbapi.controller.utility.StorageFiller;
import com.jjkeller.kmbapi.enums.DutyStatusEnum;
import com.jjkeller.kmbapi.enums.RuleSetTypeEnum;
import com.jjkeller.kmbapi.kmbeobr.Enums.EobrReturnCode;
import com.jjkeller.kmbapi.eobrengine.IEobrEngine;
import com.jjkeller.kmbapi.proxydata.EldEventAdapterList;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;
import com.jjkeller.kmbapi.proxydata.Location;
import com.jjkeller.kmbui.R;

import org.joda.time.DateTime;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class Admin extends BaseActivity 	
							implements AdminFragActions, AdminFragControllerMethods{
	AdminFrag _contentFrag;
	private String noDeviceString;
	private String resetDeviceString;
	private String successfullyClearedDeviceString;
	private int successfulResetOfDeviceInt;
	private int failedResetOfDeviceInt;
	private int successfulResetOfDeviceInt2;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.baselayout);
		if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
			noDeviceString = getString(R.string.no_eld_available);
			resetDeviceString = getString(R.string.admin_msgpowercyclereseteld);
			successfulResetOfDeviceInt = R.string.admin_msgpowercycleresetsuccessfuleld;
			failedResetOfDeviceInt = R.string.admin_msgpowercycleresetfailedeld;
			successfulResetOfDeviceInt2 = R.string.msg_eld_history_successfully_reset;
			successfullyClearedDeviceString = getString(R.string.msgsuccessfullyclearedeld);
		} else {
			noDeviceString = getString(R.string.no_eobr_available);
			resetDeviceString = getString(R.string.admin_msgpowercyclereseteobr);
			successfulResetOfDeviceInt = R.string.admin_msgpowercycleresetsuccessfuleobr;
			failedResetOfDeviceInt = R.string.admin_msgpowercycleresetfailedeobr;
			successfulResetOfDeviceInt2 = R.string.msg_eobr_history_successfully_reset;
			successfullyClearedDeviceString = getString(R.string.msgsuccessfullyclearedeobr);

		}
		new FetchLocalDataTask(this.getClass().getSimpleName()).execute();
	}
	
	@Override
	protected void loadControls() {
		super.loadControls();
		loadContentFragment(new AdminFrag());
	}

	@Override
	public void setFragments()
	{
		super.setFragments();

		Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
		_contentFrag = (AdminFrag)f;
	}
	
	@Override
	protected void InitController() {

		AdminController ctrlr = new AdminController(this);
		this.setController(ctrlr);	
	}
	
	public AdminController getMyController()
	{
		return (AdminController)this.getController();
	}
	
	public void handleResetHistory()
	{
		if(EobrReader.getIsEobrDevicePhysicallyConnected()) {
			new ResetHistoryTask().execute();
		} else {
			this.showMsg(noDeviceString);
		}
	}	

	public void handleDashboard()
	{
		if(EobrReader.getIsEobrDeviceOnlineOrReadingHistory())
			startActivity(Dashboard.class);
		else
			this.showMsg(noDeviceString);
	}
	
	public void handleResetDataUsage()
	{
		DataUsageController ctrl = new DataUsageController(this);
		ctrl.ClearDataUsage();
		this.showMsg(getString(R.string.msg_cleared_data_usage_from_database));
	}
	
	public void handleClearHomeSetting()
	{
		getPackageManager().clearPackagePreferredActivities(getPackageName());
		this.showMsg(getString(R.string.msg_cleared_kellermobile_as_home_screen));
	}
	
	private void deleteDBConfirmed()
	{
		//If the user confirms they want to delete the DB.
		//stop the eobr.
		EobrReader.getInstance().SuspendReading();
		
		//delete the database.
		boolean isSuccessful =  this.deleteDatabase(AbstractDBAdapter.DATABASE_NAME);
		if (isSuccessful){
			//Just set the user list to a new empty list.  This should remove all users (including team drivers)
			//from the active list of users.
			LoginController cont = new LoginController(this);
			cont.setLoggedInUserList(new ArrayList<User>());

			//exit the app....use the Logout, which does the rods with the exit intent.
			Bundle extras = new Bundle();
	        extras.putBoolean(this.getString(R.string.exit),true);
	        this.startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_CLEAR_TOP, extras);
		}
		else	//The database was not successfully deleted...prompt the user, and turn the reading of the eobr off.
		{
			this.ShowMessage(this, getString(R.string.msg_the_database_was_not_deleted));
			EobrReader.getInstance().ResumeReading();
		}
		
	}
	
	public void handleDeleteDB()
	{
		//prompt the user to make sure they want to Delete the DB.s
		Admin.this.ShowConfirmationMessage(Admin.this, getString(R.string.msg_confirm_delete_db),
				new ShowMessageClickListener() {
					@Override
					public void onClick(
							DialogInterface dialog, int id) {
						super.onClick(dialog, id);
						Admin.this.deleteDBConfirmed();
						Admin.this.UnlockScreenRotation();
					}
				}, new ShowMessageClickListener() {
					@Override
					public void onClick(
							DialogInterface dialog, int id) {
						super.onClick(dialog, id);
						Admin.this.UnlockScreenRotation();
						
					}
				});

	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		this.CreateOptionsMenu(menu, false);	
		return true;
	}
	
	private class ResetHistoryTask extends AsyncTask<Void, Void, Boolean>
	{
		ProgressDialog pd;
		Exception ex;
		
		protected void onPreExecute()
		{
			if(!Admin.this.isFinishing())
				pd = ProgressDialog.show(Admin.this, "", getString(R.string.admin_msgresethistory));
			LockScreenRotation();
		}

		protected Boolean doInBackground(Void... params)
		{
			AdminController adminCtrllr = new AdminController(Admin.this);
        	return adminCtrllr.ResetEobrHistoryData();
		}

        protected void onPostExecute(Boolean success)
        {
        	if(pd != null && pd.isShowing()) pd.dismiss();
        	if(ex != null)
        	{
        		success = false;
        	}
    		
        	int messageId = 0;
        	
    		if(success){
    			messageId = successfulResetOfDeviceInt2;
    		}
    		else{
    			messageId = R.string.msg_error_occurred_reset_failed;
    		}

    		try
    		{
    			AlertDialog.Builder builder1 = new AlertDialog.Builder(Admin.this);
    			builder1.setMessage(messageId);
    			builder1.setPositiveButton("OK", new OnClickListener() {
    				public void onClick(DialogInterface dialog, int which) {		            	
    					UnlockScreenRotation();	
    				}
    			});
    			builder1.show();
    		} catch (final IllegalArgumentException e) {
    			// Handle or log or ignore
    		} catch (final Exception e) {
    			// Handle or log or ignore
    		} finally {
    			UnlockScreenRotation();	
    		} 
        }
	}
	
	public void handleResetEobrButtonClick() {

		if(EobrReader.getIsEobrDeviceAvailable()) {
			GeotabController geotabController = new GeotabController(this.getBaseContext());

			if (geotabController.IsCurrentDeviceGeotab()) {

				this.getMyController().PurgeGeotabTables();
				this.getMyController().ClearEngineState();

				this.showMsg(successfullyClearedDeviceString);

			} else {
				ProgressDialog pd = new ProgressDialog(this);
				LockScreenRotation();
				this.getMyController().ResetEobr(this, pd);
			}
		}
		else {
			this.showMsg(noDeviceString);
		}
	}
	
	public void handleDisableReadEldVin(boolean isChecked, boolean showMessage){
		if(EobrReader.getIsEobrDevicePhysicallyConnected()) {
			EobrReader eobr = EobrReader.getInstance();			
			
			int rc = eobr.SetDisableReadEldVin(isChecked);
	        
			if (!showMessage)
			{
				//do nothing
			}
			else if(rc == EobrReturnCode.S_SUCCESS) 
                this.showMsg(getString(R.string.msgsuccessfullycompleted));     
			else 
                this.showMsg(getString(R.string.msgerrorsoccured));
		}
		else
			this.showMsg(noDeviceString);
	}

	public void handlePowerCycleReset() {
		if(EobrReader.getIsEobrDevicePhysicallyConnected()) {
			new PowerCycleResetTask().execute();
		}
		else
			this.showMsg(noDeviceString);
	}
	
	private class PowerCycleResetTask extends AsyncTask<Void, Void, Boolean>
	{
		ProgressDialog pd;
		Exception ex;
		
		protected void onPreExecute()
		{			
			if(!Admin.this.isFinishing())
				pd = ProgressDialog.show(Admin.this, "", resetDeviceString);
			LockScreenRotation();
		}

		protected Boolean doInBackground(Void... params)
		{
			AdminController adminCtrllr = new AdminController(Admin.this);
        	return adminCtrllr.PerformPowerCycleResetEobr();
		}

        protected void onPostExecute(Boolean success)
        {
        	if(pd != null && pd.isShowing()) pd.dismiss();
        	if(ex != null)
        	{
        		success = false;
        	}
        	
        	int messageId = 0;
        	
    		if(success){
    			messageId = successfulResetOfDeviceInt;
    		}
    		else{
    			messageId = failedResetOfDeviceInt;
    		}

    		try
    		{
    			AlertDialog.Builder builder1 = new AlertDialog.Builder(Admin.this);
    			builder1.setMessage(messageId);
    			builder1.setPositiveButton("OK", new OnClickListener() {
    				public void onClick(DialogInterface dialog, int which) {		            	
    					UnlockScreenRotation();	
    				}
    			});
    			builder1.show();
    		} catch (final IllegalArgumentException e) {
    			// Handle or log or ignore
    		} catch (final Exception e) {
    			// Handle or log or ignore
    		} finally {
    			UnlockScreenRotation();	
    		} 
        }
	}

	@Override
	public void handleAdminMalfunctionAndDataDiagnosticClick() {
		startActivity(AdminMalfunctionAndDataDiagnostic.class);
	}

	@Override
	public void handleAdminDataTransferStatusClick() {
		startActivity(AdminDataTransferStatus.class);
	}

	@Override
	public void handleAdminStorageFillerClick() {
        new AlertDialog.Builder(this)
            .setTitle(R.string.debugStorageFiller)
            .setNegativeButton(R.string.btncancel, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {}
            })
            .setNeutralButton(R.string.btncleanup, new DebugStorageCleanupClickListener())
            .setPositiveButton(R.string.btnfill, new DebugStorageFillerClickListener())
            .create()
            .show();
	}

	public void handleAdminClearActiveDeviceCrcClick() {
		if(EobrReader.getIsEobrDeviceAvailable()) {
			IEobrEngine eobrEngine = EobrReader.getInstance().getEobrEngine();
			if (eobrEngine != null ){
				eobrEngine.ClearActiveDeviceCrc();
				this.showMsg("Successfully cleared the active device's crc.  You should see Crc communication failures now :-(.");
			}
			else {
				this.showMsg("Unable to acquire EobrEngine for the active device.");
			}
		}
		else {
			this.showMsg("Active device is not available to clear.");
		}
	}

	public void handleAdminSaveDuplicateDutyStatusClick() {
		new SaveDuplicateDutyStatusTask().execute();
	}

	private class SaveDuplicateDutyStatusTask extends AsyncTask<Void, Void, Boolean>
	{
		ProgressDialog pd;
		Exception ex;

		protected void onPreExecute()
		{
			if(!Admin.this.isFinishing())
				pd = ProgressDialog.show(Admin.this, "", "Saving duplicate duty status events...");
			LockScreenRotation();
		}

		protected Boolean doInBackground(Void... params)
		{
			IAPIController empLogEventController = MandateObjectFactory.getInstance(GlobalState.getInstance(), GlobalState.getInstance().getFeatureService()).getCurrentEventController();
			EmployeeLog log = GlobalState.getInstance().getCurrentDriversLog();
			Location location = GlobalState.getInstance().getLastLocation();
			RuleSetTypeEnum ruleSetTypeEnum = log.getRuleset();

			try {
				DateTime nowWithMillis =  TimeKeeper.getInstance().getCurrentDateTime();
				Date eventTimestamp = nowWithMillis.withMillisOfSecond(0).toDate();

				EmployeeLogEldEvent previousDutyStatusEventInLog = EmployeeLogUtilities.GetLastEventInLog(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus, log);

				// Testing the common scenario first where we already have an active duty status event at a given time and try to insert another one at the same time (using the IAPIController interface)
				empLogEventController.CreateDutyStatusChangedEvent(log, eventTimestamp, DutyStatusEnum.valueOfDMOEnum(DutyStatusEnum.DmoEnum_Driving), location, true, ruleSetTypeEnum, null, null, true, null, null);

				// The insertion of another active duty status event at the same time as the previous event shouldn't happen and should generate a log message
				empLogEventController.CreateDutyStatusChangedEvent(log, eventTimestamp, DutyStatusEnum.valueOfDMOEnum(DutyStatusEnum.DmoEnum_OnDuty), location, true, ruleSetTypeEnum, null, null, true, null, null);

				// Attempt to update the previous duty status event to the time of the new duty status events above (this should fail as well with a log message for updates)
				Date origEventDateTime = previousDutyStatusEventInLog.getEventDateTime();
				EmployeeLogEldEventFacade facade = new EmployeeLogEldEventFacade(GlobalState.getContext(), GlobalState.getInstance().getCurrentUser());
				previousDutyStatusEventInLog.setEventDateTime(eventTimestamp);
				facade.Save(previousDutyStatusEventInLog);
				previousDutyStatusEventInLog.setEventDateTime(origEventDateTime);	// Restore original event date time so multiple calls to this method don't cause all kinds of duplicate checking issues
			} catch (Throwable throwable) {
				throwable.printStackTrace();
				return false;
			}

			return true;
		}

		protected void onPostExecute(Boolean success)
		{
			if(pd != null && pd.isShowing()) pd.dismiss();
			try
			{
				AlertDialog.Builder builder1 = new AlertDialog.Builder(Admin.this);
				builder1.setMessage(R.string.admin_msgsaveduplicatedutystatuscomplete);
				builder1.setPositiveButton("OK", new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						UnlockScreenRotation();
					}
				});
				builder1.show();
			} catch (final IllegalArgumentException e) {
				// Handle or log or ignore
			} catch (final Exception e) {
				// Handle or log or ignore
			} finally {
				UnlockScreenRotation();
			}
		}
	}

	@Override
	public void handleForceGeotabInvalidGpsLatch(boolean isChecked) {
		GlobalState.getInstance().setForceGeotabInvalidGPS(isChecked);
	}

	final int BYTES_PER_MB = 1048576;
	class DebugStorageFillerClickListener implements OnClickListener {
		private String getMessage(int freeMB, int selectedMB) {
			return String.format(Locale.getDefault(),
				Admin.this.getString(R.string.storageFillPrompt),
				freeMB,
				selectedMB);
		}

		@Override
		public void onClick(DialogInterface dialogInterface, int i) {
			final int freeMB = (int)(Admin.this.getFilesDir().getFreeSpace() / BYTES_PER_MB);

			final SeekBar seekBar = new SeekBar(Admin.this);
			seekBar.setMax(freeMB);
			seekBar.setKeyProgressIncrement(100);

			final AlertDialog alert = new AlertDialog.Builder(Admin.this)
					.setTitle(R.string.debugStorageFiller)
					.setMessage(getMessage(freeMB, 0))
					.setView(seekBar)
					.setPositiveButton(R.string.btnok, new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
							int mb = seekBar.getProgress();
							new StorageFiller(Admin.this).fillStorage(mb);
						}
					})
					.setNegativeButton(R.string.btncancel, new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {}
					})
					.create();

			seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				@Override
				public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
					alert.setMessage(getMessage(freeMB, i));
				}

				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {

				}

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {

				}
			});

			alert.show();
		}
	}

	class DebugStorageCleanupClickListener implements OnClickListener {
		@Override
		public void onClick(DialogInterface dialogInterface, int i) {
			final StorageFiller storageFiller = new StorageFiller(Admin.this);

			File[] files = storageFiller.getFillFiles();
			long size = 0;

			for(File file : files) {
				Log.v("Admin", String.format("Found fill file %s", file.getName()));
				size += file.length();
			}

			size /= BYTES_PER_MB;

			String message = String.format(
					Admin.this.getString(R.string.fillFileCount),
					files.length,
					size);

			int negativeTextId = files.length > 0 ? R.string.btncancel : R.string.btnok;

			final AlertDialog.Builder builder = new AlertDialog.Builder(Admin.this)
					.setTitle(R.string.debugStorageFiller)
					.setMessage(message)
					.setNegativeButton(negativeTextId, new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {

						}
					});

			if(files.length > 0) {
				builder.setPositiveButton(R.string.btnDelete, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						storageFiller.cleanupFiles();
					}
				});
			}

			builder.create().show();
		}
	}

	@Override
	public void handleForceGeotabOdoError(boolean isChecked) {
		GlobalState.getInstance().setForceGeotabInvalidOdo(isChecked);
	}

	@Override
	public void handleForceGeotabVssError(boolean isChecked){
		GlobalState.getInstance().setForceGeotabInvalidVss(isChecked);
	}

	@Override
	public void handleForceInvalidGPSDate(boolean isChecked){
		GlobalState.getInstance().setForceInvalidGPSDate(isChecked);
	}
}
