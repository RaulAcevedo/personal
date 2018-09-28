package com.jjkeller.kmb;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.LogEntryController;
import com.jjkeller.kmbapi.controller.LogPersonalConveyanceController;
import com.jjkeller.kmbapi.controller.interfaces.IAPIController;
import com.jjkeller.kmbapi.controller.interfaces.IBluetoothDrivingManager;
import com.jjkeller.kmbapi.controller.interfaces.ISpecialDrivingController;
import com.jjkeller.kmbapi.controller.share.MandateObjectFactory;
import com.jjkeller.kmbapi.controller.share.SpecialDrivingFactory;
import com.jjkeller.kmbapi.enums.DutyStatusEnum;
import com.jjkeller.kmbapi.kmbeobr.EventRecord;
import com.jjkeller.kmbapi.kmbeobr.EventTypeEnum;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbui.R;

import java.util.Date;

/**
 * Management of the bluetooth disconnection and reconnection between the
 * KMB app and the EOBR device while automatic driving in Mandate mode.
 */
public class BluetoothDrivingManagerMandate implements IBluetoothDrivingManager {
	protected static final String TAG = "BTDisconnect";

	private ThresholdTimerStateEnum mThresholdTimerStateEnum = ThresholdTimerStateEnum.STATE_IDLE;
	private Date mPotentialDrivingStopTimestamp;
	private final Handler mHandler = new Handler(Looper.getMainLooper());

	public Date getPotentialDrivingStopTimestamp() { return mPotentialDrivingStopTimestamp; }

	/**
	 * Enums representing the current state of the threshold timer.
	 */
	private enum ThresholdTimerStateEnum {
		STATE_IDLE,
		STATE_RUNNING
	}

	private Runnable _mBluetoothDisconnectedDuringDrivingRunnable = new Runnable() {
		public void run() {

			// use Handler to run on UI thread
			new Handler().post(new Runnable() {
				public void run() {

					//Log.d(TAG, "Bluetooth Disconnected During Driving Threshold Timer at " + new SimpleDateFormat("hh:mm:ss aa").format(Calendar.getInstance().getTime()));

					GlobalState.getInstance().setIsInDriveOnPeriod(false);

					// Use a instance of the current activity to display the end of driving dialog
					BaseActivity baseActivity = (BaseActivity) GlobalState.getInstance().getCurrentActivity();
					if (baseActivity != null && !baseActivity.isFinishing()) {
						baseActivity.DisplayVerifyDrivingDutyStatusEndDialog();
					} else {
						// If the current activity is null for any reason. then just automatically add the onDuty event to the user's log
						new CreateAutomaticOnDutyNotDrivingAsync().execute(mPotentialDrivingStopTimestamp);
					}

					// cancel timer
					stopBluetoothDisconnectedDuringDrivingTimer();
				}
			});
		}
	};

	/**
	 * Properly track when CMV has not been in-motion for 5 consecutive minutes when a Bluetooth
	 * disconnection occurs when current duty status is an automatic driving duty status.
	 *
	 * If PotentialStopTimestamp is already set (i.e. vehicle has already stopped moving) the timer
	 * threshold should be taken from that value.
	 */
	@Override
	public void setBluetoothDisconnectedDuringDriving(Date timestamp, long drivingStopTimeMillis, boolean isInYardMoveDrivingSegment, boolean isInPersonalConveyanceDrivingSegment, boolean isInHyrailDrivingSegment, boolean isInNonRegDrivingSegment) {
		if (!isThresholdTimerRunning() && drivingStopTimeMillis > 0) {

			if (isInYardMoveDrivingSegment) {
				mThresholdTimerStateEnum = ThresholdTimerStateEnum.STATE_RUNNING;
				mPotentialDrivingStopTimestamp = timestamp;	// time to check ignition cycle since

				// fall-through logic to dismiss DOT Clocks
			}
			else if (isInPersonalConveyanceDrivingSegment) {
				mThresholdTimerStateEnum = ThresholdTimerStateEnum.STATE_RUNNING;
				mPotentialDrivingStopTimestamp = timestamp;	// time to check ignition cycle since

				// special driving will just dismiss the dialog "Vehicle is being operated under authorized use of Personal Conveyance"
				new DismissSpecialDrivingDialog(R.string.msg_personalconveyancedrivingallowed).execute();
			}
			else if (isInHyrailDrivingSegment || isInNonRegDrivingSegment) {
				// special driving will just dismiss the clocks but remain in special driving status -- no 5-minute threshold timer
				mThresholdTimerStateEnum = ThresholdTimerStateEnum.STATE_RUNNING;
				mPotentialDrivingStopTimestamp = null;
			}
			else {
				// plain old driving
				startBluetoothDisconnectedDuringDrivingTimer(timestamp, drivingStopTimeMillis);
			}

			// If Bluetooth disconnect occurs while the vehicle is being driven - dismiss the DOT Clocks screen - we can no longer detect if the vehicle is moving or not.
			if (GlobalState.getInstance().getCurrentActivity() != null && GlobalState.getInstance().getCurrentActivity().getLocalClassName().equalsIgnoreCase("com.jjkeller.kmb.DOTClocks")) {
				new DismissDotClocksActivity().execute();
			}

			//Log.d(TAG, "Bluetooth Disconnected During Driving at " + new SimpleDateFormat("hh:mm:ss aa").format(timestamp) + " timer duration seconds = " + TimeUnit.MILLISECONDS.toSeconds(drivingStopTimeMillis));
		}
	}

	/**
	 * Upon reconnect and UnidentifiedEldEvents claimed, determine if engine cycle occurred while disconnected that would end Special Driving (Yard Move or Personal Conveyance)
	 */
	@Override
	public void onUnidentifiedEldEventsClaimed(boolean isInYardMoveDrivingSegment, boolean isInPersonalConveyanceDrivingSegment) {

		//Log.d(TAG, "Bluetooth onUnidentifiedEldEventsClaimed at " + new SimpleDateFormat("hh:mm:ss aa").format(Calendar.getInstance().getTime()));

		if (isInYardMoveDrivingSegment && mPotentialDrivingStopTimestamp != null) {

			LogEntryController logEntryController = new LogEntryController(GlobalState.getInstance().getApplicationContext());
			Date lastIgnitionOff = logEntryController.lastIgnitionOffByDriver(GlobalState.getInstance().getCurrentDriversLog().getEmployeeId());

			// engine cycle for mandate defines ignition cycle as Off)
			if (lastIgnitionOff != null) {

				IAPIController empLogController = MandateObjectFactory.getInstance(GlobalState.getInstance().getApplicationContext(), GlobalState.getInstance().getFeatureService()).getCurrentEventController();

				if (empLogController != null) {
					EventRecord eventRecord = new EventRecord();
					eventRecord.setEventType(EventTypeEnum.DTCSTATUSCHANGE);

					BaseActivity baseActivity = (BaseActivity) GlobalState.getInstance().getCurrentActivity();
					if (baseActivity != null) {
						logEntryController.EndYardMoveAndChangeDutyStatus(GlobalState.getInstance().getCurrentEmployeeLog(), empLogController, eventRecord, baseActivity.getString(R.string.ignition_off_cycle_end_ym));
					}

					// publish to the UI to update the screen so Duty Status no longer reflects PC
					logEntryController.PublishDutyStatusChange();

					mThresholdTimerStateEnum = ThresholdTimerStateEnum.STATE_IDLE;
					mPotentialDrivingStopTimestamp = null;
				}
			}
		}
		else if (isInPersonalConveyanceDrivingSegment && mPotentialDrivingStopTimestamp != null) {

			LogEntryController logEntryController = new LogEntryController(GlobalState.getInstance().getApplicationContext());
			Date lastIgnitionOn = logEntryController.hasIngitionOffOnCycleOccuredSince(mPotentialDrivingStopTimestamp);

			// engine cycle for mandate defines ignition cycle as Off -> ON)
			if (lastIgnitionOn != null) {

				ISpecialDrivingController specialDrivingController = SpecialDrivingFactory.getControllerInDrivingSegment();
				if(specialDrivingController != null && specialDrivingController.getClass() == LogPersonalConveyanceController.class) {

					EventRecord eventRecord = new EventRecord();
					eventRecord.setEventType(EventTypeEnum.DTCSTATUSCHANGE);

					specialDrivingController.EndSpecialDrivingStatus(eventRecord, GlobalState.getInstance().getLastLocation(), GlobalState.getInstance().getCurrentEmployeeLog());

					// publish to the UI to update the screen so Duty Status no longer reflects PC
					logEntryController.PublishDutyStatusChange();

					mThresholdTimerStateEnum = ThresholdTimerStateEnum.STATE_IDLE;
					mPotentialDrivingStopTimestamp = null;
				}
			}
		}

		//Log.d(TAG, "Bluetooth onUnidentifiedEldEventsClaimed End Special Driving at " + new SimpleDateFormat("hh:mm:ss aa").format(Calendar.getInstance().getTime()));
	}

	/**
	 * The KMB app can stop monitoring against the 5-minute threshold if a manual duty status is entered by the driver.
	 */
	@Override
	public void setManualDutyStatusChange() {
		if (mThresholdTimerStateEnum == ThresholdTimerStateEnum.STATE_RUNNING) {

			stopBluetoothDisconnectedDuringDrivingTimer();

			//Log.d(TAG, "Bluetooth Disconnected During Driving - user manually changed Duty Status " + new SimpleDateFormat("hh:mm:ss aa").format(TimeKeeper.getInstance().getCurrentDateTime().toDate()));
		}
	}

	/**
	 * The KMB app can stop monitoring against the 5-minute threshold if the vehicle starts moving again.
	 */
	@Override
	public void setVehicleDriving() {
		if (mThresholdTimerStateEnum == ThresholdTimerStateEnum.STATE_RUNNING) {

			stopBluetoothDisconnectedDuringDrivingTimer();

			//Log.d(TAG, "Bluetooth Disconnected During Driving - user started driving " + new SimpleDateFormat("hh:mm:ss aa").format(TimeKeeper.getInstance().getCurrentDateTime().toDate()));
		}
	}

	/**
	 * Return true if the 5-minute threshold timer is currently running; otherwise false;
	 */
	@Override
	public boolean isThresholdTimerRunning() {

		BaseActivity baseActivity = (BaseActivity) GlobalState.getInstance().getCurrentActivity();
		if (baseActivity != null) {
			// if Bluetooth is reconnecting WHILE the 'Continue in Driving?' prompt is displaying, act like the threshold timer is still running so we eat the DRIVE_OFF
			if (baseActivity.IsAlertMessageShowing() && baseActivity.ContainsAlertMessage(baseActivity.getString(R.string.continue_driving_status_message))) {
				//Log.d(TAG, "Bluetooth Disconnected During Driving - prompt dialog already displaying " + new SimpleDateFormat("hh:mm:ss aa").format(TimeKeeper.getInstance().getCurrentDateTime().toDate()));

				return true;
			}
		}

		return mThresholdTimerStateEnum == ThresholdTimerStateEnum.STATE_RUNNING;
	}

	//
	// Helper Methods
	//


	/**
	 * Start a 5-minute threshold timer since the vehicle stopped driving
	 */
	private void startBluetoothDisconnectedDuringDrivingTimer(Date potentialDrivingStopTimestamp, long drivingStopTimeMillis) {
		stopBluetoothDisconnectedDuringDrivingTimer();

		mThresholdTimerStateEnum = ThresholdTimerStateEnum.STATE_RUNNING;
		mPotentialDrivingStopTimestamp = potentialDrivingStopTimestamp;

		mHandler.postDelayed(_mBluetoothDisconnectedDuringDrivingRunnable, drivingStopTimeMillis);
	}

	/**
	 * Stop a 5-minute threshold timer since the vehicle stopped driving
	 */
	private void stopBluetoothDisconnectedDuringDrivingTimer() {
		if (_mBluetoothDisconnectedDuringDrivingRunnable != null) {
			mHandler.removeCallbacks(_mBluetoothDisconnectedDuringDrivingRunnable);

			mThresholdTimerStateEnum = ThresholdTimerStateEnum.STATE_IDLE;
			mPotentialDrivingStopTimestamp = null;
		}
	}

	/**
	 * If Bluetooth disconnect occurs while the vehicle is being driven - dismiss the DOT Clocks screen.
	 * The duty status should still indicate driving, but the clocks can come down since we can no longer detect if the vehicle is moving or not.
	 */
	private class DismissDotClocksActivity extends AsyncTask<Void, Void, Void> {
		protected Void doInBackground(Void... params) {
			return null;
		}

		/**
		 * Run on UI thread
		 */
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			// when the app comes back into the foreground, the DOT Clocks will be dismissed and RodsEntry activity will be visible
			if (GlobalState.getInstance().getCurrentActivity() != null && GlobalState.getInstance().getCurrentActivity().getLocalClassName().equalsIgnoreCase("com.jjkeller.kmb.DOTClocks")) {
				Intent intent = new Intent(GlobalState.getInstance().getApplicationContext(), RodsEntry.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

				GlobalState.getInstance().getCurrentActivity().startActivity(intent);
			}
		}
	}

	/**
	 * If Bluetooth disconnect occurs while the vehicle is being driven - dismiss the Special Driving dialog.
	 */
	private class DismissSpecialDrivingDialog extends AsyncTask<Void, Void, Void> {
		private int mResourceId;

		DismissSpecialDrivingDialog(int resId) {
			mResourceId = resId;
		}

		protected Void doInBackground(Void... params) {
			return null;
		}

		/**
		 * Run on UI thread
		 */
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			// when the app comes back into the foreground, the DOT Clocks will be dismissed and RodsEntry activity will be visible
			if (GlobalState.getInstance().getCurrentActivity() != null && GlobalState.getInstance().getCurrentActivity() instanceof BaseActivity) {
				BaseActivity baseActivity = (BaseActivity) GlobalState.getInstance().getCurrentActivity();

				if (baseActivity.IsAlertMessageShowing() && baseActivity.ContainsAlertMessage(baseActivity.getString(mResourceId))) {
					baseActivity.DismissAlertMessageFor(baseActivity.getString(mResourceId));
				}
			}
		}
	}

	/**
	 * Create an automatic on duty-not driving duty status event
	 *
	 * From StackOverflow:
	 * 		- on pre 4.4 devices the app silently opens the new activity and remains in background. When the user resumes the application,
	 * 		  he is prompted with the second activity already.
	 * 		- on 4.4 devices (tested on 4.4.2 and 4.4.4) the app opens the second activity, but after 3-4 seconds,
	 * 		  the app pops to foreground, interrupting the user.
	 */
	private class CreateAutomaticOnDutyNotDrivingAsync extends AsyncTask<Date, Void, Void> {
		protected Void doInBackground(Date... potentialDrivingStopTimestamp) {
			try {
				// create an automatic on duty-not driving duty status event
				EmployeeLog employeeLog = GlobalState.getInstance().getCurrentEmployeeLog();
				if (employeeLog != null) {

					// create an on duty status change
					LogEntryController ctrl = new LogEntryController(GlobalState.getInstance());
					String productionId = null, authorityId = null;
					if (GlobalState.getInstance().getCompanyConfigSettings(GlobalState.getInstance().getApplicationContext()).getIsMotionPictureEnabled()) {
						authorityId = GlobalState.getInstance().get_currentMotionPictureAuthorityId();
						productionId = GlobalState.getInstance().get_currentMotionPictureProductionId();
					}
					ctrl.PerformStatusChange(GlobalState.getInstance().getCurrentDesignatedDriver(), GlobalState.getInstance().getCurrentDriversLog(), GlobalState.getInstance().getPotentialDrivingStopTimestamp(), new DutyStatusEnum(DutyStatusEnum.ONDUTY), GlobalState.getInstance().getLastLocation(), true, false, null, productionId, authorityId);
				}
			} catch (Exception ex) {
				Log.e(TAG, "Bluetooth Disconnected During Driving Threshold Timer EXCEPTION: " + ex.getMessage());
			} catch (Throwable throwable) {
				throwable.printStackTrace();
				Log.e(TAG, "Bluetooth Disconnected During Driving Threshold Timer THROWABLE: " + throwable.getMessage());
			}

			return null;
		}

		/**
		 * Run on UI thread
		 */
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			// Determine if a manual location is required. If so, display RodsNewStatus screen
			if (GlobalState.getInstance().getCurrentActivity() != null && GlobalState.getInstance().getCurrentActivity() instanceof BaseActivity) {
				BaseActivity base = (BaseActivity) GlobalState.getInstance().getCurrentActivity();
				base.HandleLocationAfterAutomaticOnDuty();
			}
		}
	}
}
