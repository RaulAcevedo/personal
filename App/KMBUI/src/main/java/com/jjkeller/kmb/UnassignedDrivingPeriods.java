package com.jjkeller.kmb;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.jjkeller.kmb.fragments.LeftNavFrag;
import com.jjkeller.kmb.fragments.UnassignedDrivingPeriodsFrag;
import com.jjkeller.kmb.interfaces.IUnassignedDrivingPeriods.UnassignedDrivingPeriodsFragActions;
import com.jjkeller.kmb.interfaces.IUnassignedDrivingPeriods.UnassignedDrivingPeriodsFragControllerMethods;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.ControllerFactory;
import com.jjkeller.kmbapi.controller.GeotabController;
import com.jjkeller.kmbapi.controller.LogEntryController;
import com.jjkeller.kmbapi.controller.UnassignedPeriodController;
import com.jjkeller.kmbapi.controller.UnclaimedDrivingPeriod;
import com.jjkeller.kmbapi.controller.share.CurrentEvent;
import com.jjkeller.kmbapi.controller.share.KmbApplicationException;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.proxydata.KMBUnassignedPeriodIsClaimable;
import com.jjkeller.kmbapi.proxydata.UnassignedDrivingPeriod;
import com.jjkeller.kmbui.R;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jjkeller.kmbapi.kmbeobr.Constants.FROM_MENU_EXTRA;

public class UnassignedDrivingPeriods extends BaseActivity
										implements UnassignedDrivingPeriodsFragActions, UnassignedDrivingPeriodsFragControllerMethods,
										LeftNavFrag.OnNavItemSelectedListener, LeftNavFrag.ActivityMenuItemsListener{
	UnassignedDrivingPeriodsFrag _contentFrag;
	private boolean _isChanged = false;
	private boolean _cantClaimAll = false;
	private boolean _fromMenu;
	private boolean _returnToLogout;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		_fromMenu = intent.getBooleanExtra(FROM_MENU_EXTRA, false);
		_returnToLogout = this.getIntent().getBooleanExtra(getString(R.string.parm_returntologout), false);
		setContentView(R.layout.baselayout);
		mFetchLocalDataTask = new FetchLocalDataTask(this.getClass().getSimpleName());
		mFetchLocalDataTask.execute();
	}

	@Override
	protected void Return(boolean success) {
		if(success)
		{
			if(_cantClaimAll) {
				ShowMessage(this, 0, getString(R.string.lblcouldntclaimall), new ShowMessageClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						super.onClick(dialog, id);

						UnassignedDrivingPeriods.this.finish();

						if (_returnToLogout) {
							// finish (above) will return to logout page
						} else if(_isChanged) {
							UnassignedDrivingPeriods.this.startActivity(EditLogLocations.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
						} else {
							UnassignedDrivingPeriods.this.startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
						}
					}
				});
			} else {
				if (_returnToLogout) {
					this.finish();	// finish will return to logout page
				} else if(_isChanged){
					this.finish();
					this.startActivity(EditLogLocations.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
				} else {
					this.startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
				}
			}
		}
		else Toast.makeText(this, this.getResources().getString(R.string.msgclaimingperiodsfailed), Toast.LENGTH_LONG).show();
	}

	public void handleClaimButtonClick() {
		mSaveLocalDataTask = new SaveLocalDataTask(this.getClass().getSimpleName());
		mSaveLocalDataTask.execute();		
	}

	@Override
	protected boolean saveData()
	{
		List<UnclaimedDrivingPeriod> unclaimedDrivingPeriods = _contentFrag.getPeriodList();

		if(GlobalState.getInstance().getCompanyConfigSettings(this).getIsGeotabEnabled()) {

			GeotabController geotabController = new GeotabController(getBaseContext());

			Map<String, UnclaimedDrivingPeriod> periodsToClaim = new HashMap<>();
			for(UnclaimedDrivingPeriod item : unclaimedDrivingPeriods) {
				if (item.getIsClaimed()) {
					periodsToClaim.put(item.getUnassignedDrivingPeriod().getEncompassId(), item);
				}
			}

			if(!periodsToClaim.isEmpty()) {
				try {
					//get earliest unassigned event date
					Date earliestUnassignedDate = getEarliestUnassignedDate(periodsToClaim);

					//attempt to claim them on the server and check the response to see if we could
					List<KMBUnassignedPeriodIsClaimable> result = geotabController.ClaimUnassignedDrivingPeriods(periodsToClaim.values());

					//any that we couldn't claim were claimed by or assigned to other drivers so mark them as claimed here
					//and prevent the current driver from claiming them
					for (KMBUnassignedPeriodIsClaimable period : result) {
						if (!period.isClaimable()) {
							UnclaimedDrivingPeriod udp = periodsToClaim.get(period.getId());

							//prevent the period from being put on the driver's log
							udp.setIsClaimed(false);

							//mark it as claimed in the database
							getMyController().MarkUnassignedDrivingPeriodsAsClaimed(udp.getUnassignedDrivingPeriod());

							_cantClaimAll = true;
						}
					}

					//update the user mobile start timestamp
					updateMobileStartTimestamp(earliestUnassignedDate);

				} catch (KmbApplicationException e) {
					Log.e("UnassignedDrivingPeriod", "Error while claiming unassigned periods.", e);

					return false;
				}
			}
		}

		for(UnclaimedDrivingPeriod period : unclaimedDrivingPeriods) {
			if(period.getIsClaimed()) {
				_isChanged = true;
				break;
			}
		}

		this.getMyController().ProcessClaimedDrivingPeriods(_contentFrag.getPeriodList());

		return true;
	}
	
	@Override
	protected void loadControls() {
		super.loadControls();
		Bundle bundle = new Bundle();
		bundle.putBoolean(FROM_MENU_EXTRA, _fromMenu);
		UnassignedDrivingPeriodsFrag unassignedDrivingPeriodsFrag = new UnassignedDrivingPeriodsFrag();
		unassignedDrivingPeriodsFrag.setArguments(bundle);
		loadContentFragment(unassignedDrivingPeriodsFrag);
	}

	@Override
	protected void InitController() {
		UnassignedPeriodController ctrl = ControllerFactory.getInstance().getUnassignedPeriodController();

		this.setController(ctrl);
	}

	public UnassignedPeriodController getMyController()
	{
		return (UnassignedPeriodController)this.getController();
	}	

	@Override
	public void setFragments()
	{
		super.setFragments();

		Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
		_contentFrag = (UnassignedDrivingPeriodsFrag) f;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		this.CreateOptionsMenu(menu, false);
		return true;
	}

	public String getActivityMenuItemList() {
		return getString(R.string.btndone);
	}

	private void handleMenuItemSelected(int itemPosition) {
		if (itemPosition == 0)
		{
			this.finish();

			// When _returnToLogout=true, finish (above) will return to logout page
			// Else go to RODS
			if (!_returnToLogout) {
				this.startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
			}
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		//See if home button was pressed
		this.GoHome(item, this.getController());
		
		handleMenuItemSelected(item.getItemId());
		super.onOptionsItemSelected(item);
		return true;
	}

	public void onNavItemSelected(int item)	{
		handleMenuItemSelected(item);
	}

	/**
	 * Loop through periodsToClaim to get the earliest StartTime date from all the
	 * UnclaimedDrivingPeriod
	 * @param periodsToClaim
	 * @return startTime
	 */
	private Date getEarliestUnassignedDate(Map<String, UnclaimedDrivingPeriod> periodsToClaim){
		Date result = new Date();
		for (Map.Entry<String, UnclaimedDrivingPeriod> entry: periodsToClaim.entrySet()) {
			UnclaimedDrivingPeriod value = entry.getValue();
			if(value.getUnassignedDrivingPeriod().getStartTime().before(result)){
				result = value.getUnassignedDrivingPeriod().getStartTime();
			}
		}
		return result;
	}

	/**
	 * Update the Mobile Start Timestamp in the Current Employee Log Event
	 * @param mobileStartTimestamp
	 */
	private void updateMobileStartTimestamp(Date mobileStartTimestamp ){
		LogEntryController logEntryController = new LogEntryController(getBaseContext());
		EmployeeLog employeeLog = logEntryController.getCurrentEmployeeLog();
		if(employeeLog.getMobileStartTimestamp().after(mobileStartTimestamp)){
			logEntryController.UpdateCurrentEventTimestamp(mobileStartTimestamp);
		}
	}
}
