package com.jjkeller.kmb;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.jjkeller.kmb.fragments.RodsNewStatusFrag;
import com.jjkeller.kmb.interfaces.IRodsNewStatus.RodsNewStatusFragActions;
import com.jjkeller.kmb.interfaces.IRodsNewStatus.RodsNewStatusFragControllerMethods;
import com.jjkeller.kmb.share.ELDCommon;
import com.jjkeller.kmb.share.OffDutyBaseActivity;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.EOBR.EobrReader;
import com.jjkeller.kmbapi.controller.EmployeeLogEldMandateController;
import com.jjkeller.kmbapi.controller.LogEntryController;
import com.jjkeller.kmbapi.controller.MotionPictureController;
import com.jjkeller.kmbapi.controller.interfaces.ISpecialDrivingController;
import com.jjkeller.kmbapi.controller.share.SpecialDrivingFactory;
import com.jjkeller.kmbapi.controller.utility.EmployeeLogUtilities;
import com.jjkeller.kmbapi.employeelogeldevents.Enums;
import com.jjkeller.kmbapi.enums.DutyStatusEnum;
import com.jjkeller.kmbapi.enums.EmployeeLogProvisionTypeEnum;
import com.jjkeller.kmbapi.enums.ExemptLogTypeEnum;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;
import com.jjkeller.kmbapi.proxydata.Location;
import com.jjkeller.kmbapi.proxydata.MotionPictureProduction;
import com.jjkeller.kmbui.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RodsNewStatus extends OffDutyBaseActivity implements RodsNewStatusFragActions, RodsNewStatusFragControllerMethods {
	private RodsNewStatusFrag _contentFrag;
	private boolean _isManualLocationAfterAutomaticOnDuty = false;

    private EmployeeLogEldMandateController _employeeLogEldMandateController = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.baselayout);

		_isManualLocationAfterAutomaticOnDuty = this.getIntent().getBooleanExtra(this.getString(R.string.ismanuallocation_after_automatic_onduty),false);

		loadContentFragment(new RodsNewStatusFrag());

		//Make sure the clock is correct for the new log event that will be created
		EobrReader.getInstance().VerifyClockConsistency(this);

		mFetchLocalDataTask = new FetchLocalDataTask(this.getClass().getSimpleName(), savedInstanceState);
		mFetchLocalDataTask.execute();

        // When _isManualLocationAfterAutomaticOnDuty is true... if driving event starts while in this activity, we need to react accordingly.
		if (_isManualLocationAfterAutomaticOnDuty) {
			LocalBroadcastManager.getInstance(this).registerReceiver(drivingStartedReceiver, new IntentFilter(getString(R.string.bm_driving_started)));
		}
	}

	protected void loadControls(Bundle savedInstanceState){
		super.loadControls(savedInstanceState);

		//automatic duty status change to On Duty occurred and it's been more than 5 miles since last valid GPS, so a location is required to be entered.
		if (_isManualLocationAfterAutomaticOnDuty) {
			_contentFrag.disableCancelButton(this.getResources().getString(R.string.msgactuallocationrequired));
		}
	}

	public void HandleOKButtonClick() {
		if (_contentFrag.getDrivingStatusCheckbox().getVisibility() == View.VISIBLE && _contentFrag.getDrivingStatusCheckbox().isChecked()) {
			this.getMyController().ManuallyExtendDrivingSegment();
			this.Return();
		} else {
			if (_contentFrag.getDutyStatusSpinner().getSelectedItem() != null) {
                Bundle currentEvents = this.getMyController().GetCurrentEventValues()
                        .toBundle(
                                getResources().getString(R.string.eventvalues_logdate),
                                getResources().getString(R.string.eventvalues_starttime),
                                getResources().getString(R.string.eventvalues_dutystatus),
								getResources().getString(R.string.eventvalues_isautomaticdrivingevent),
                                getBaseContext());
                String currentDutyStatus = currentEvents.getString(getResources().getString(R.string.eventvalues_dutystatus));


                if (currentDutyStatus.equals(getResources().getString(R.string.driving)) && this.getMyController().IsVehicleInMotion()) {
                    // the vehicle is in motion, show message and return to rods
                    this.ShowMessage(this, getString(R.string.cantaddnewstatus));
                    this.Return();
                } else if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled() && _contentFrag.getPersonalConveyanceCheckbox().isChecked()
                        && !isAnnotationFieldValid()
                        && !GlobalState.getInstance().getIsInPersonalConveyanceDutyStatus()) {
                    // the eld mandate feature is turned on AND personal conveyance checkbox is checked AND the annotation text box has less than 4 characters && the user in not already in personal conveyance
                    _contentFrag.showAnnotationErrorMessage();
                } else if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled() && _contentFrag.getYardMoveCheckbox().isChecked()
                        && !isAnnotationFieldValid()
                        && !GlobalState.getInstance().getIsInYardMoveDutyStatus()) {
                    _contentFrag.showAnnotationErrorMessage();
				}else if (_contentFrag.getLocationEditText().getVisibility() == View.VISIBLE && _contentFrag.getLocationText().toString().trim().length() < 5) {
					_contentFrag.showLocationMinimumLengthErrorMessage();
				} else {
					boolean isDoneAfterLocationUpdate = false;

					if (_isManualLocationAfterAutomaticOnDuty && _contentFrag.getLocationText().length() > 0) {

						// Update location information
						String latLonStatus = getIntent().getStringExtra(this.getResources().getString(R.string.extra_lat_lon_status));
						getMyController().MandateUpdateLatLongStatusCode(latLonStatus, _contentFrag.getLocationText());

						// If duty status is still set to default of On Duty, we're done.
						// If duty status was changed to anything else, we will create a new event with the new duty status.
						String dutyStatus = _contentFrag.getDutyStatusSpinner().getSelectedItem().toString();
						if (dutyStatus.equals(getResources().getString(R.string.onduty))) {
							isDoneAfterLocationUpdate = true;
						}
					}

					if (isDoneAfterLocationUpdate) {
                        updateMissingDataDiagnosticTask.execute();

					} else {
						if (IsTheDriver(GlobalState.getInstance().getCurrentUser())) {
							if ((GlobalState.getInstance().getIsInPersonalConveyanceDutyStatus()) && !_contentFrag.getPersonalConveyanceCheckbox().isChecked()) {
								GlobalState.getInstance().setIsEndingActivePCYMWT_Status(true);
							} else if ((GlobalState.getInstance().getIsInHyrailDutyStatus()) && !_contentFrag.getUtilizeHyrailCheckbox().isChecked()) {
								GlobalState.getInstance().setIsInHyrailDutyStatus(false);
							} else if ((GlobalState.getInstance().getIsInYardMoveDutyStatus()) && !_contentFrag.getYardMoveCheckbox().isChecked()) {
								GlobalState.getInstance().setIsEndingActivePCYMWT_Status(true);
							}
						}
						performSave();
					}
				}
			} else {
				this.ShowMessage(this, getString(R.string.timeorstatusnotselected));
			}
		}
	}

	public void HandleCancelButtonClick() {
		Return();
	}

	public void HandleDutyStatusChange() {
		_contentFrag.hideAnnotation();
		_contentFrag.hideErrorMessage();

		ShowControlsForSelectedDutyStatus();
	}

	public void ShowControlsForSelectedDutyStatus() {
		String dutyStatus = _contentFrag.getDutyStatusSpinner().getSelectedItem().toString();

		if (dutyStatus.equals(getResources().getString(R.string.offduty)) && isPersonalConveyanceAvailable()) {
			// if the user selects off duty and personal conveyance feature is available
			_contentFrag.getPersonalConveyanceCheckbox().setVisibility(View.VISIBLE);

			if (GlobalState.getInstance().getIsInPersonalConveyanceDutyStatus()) {
				// is currently in personal conveyance
				_contentFrag.getPersonalConveyanceCheckbox().setChecked(true);
			} else {
				// is not in personal conveyance
				if (_contentFrag.getPersonalConveyanceCheckbox().isChecked() && GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
					_contentFrag.showAnnotation();
				}
			}
		} else {
			// personal conveyance is not available
			_contentFrag.getPersonalConveyanceCheckbox().setVisibility(View.GONE);
			_contentFrag.getPersonalConveyanceCheckbox().setChecked(false);
		}

		if (dutyStatus.equals(DutyStatusEnum.Friendly_OnDuty)&& isHyrailUseAllowed()) {
			_contentFrag.getUtilizeHyrailCheckbox().setVisibility(View.VISIBLE);
		} else {
			_contentFrag.getUtilizeHyrailCheckbox().setVisibility(View.GONE);
			_contentFrag.getUtilizeHyrailCheckbox().setChecked(false);
		}

		if (dutyStatus.equals(DutyStatusEnum.Friendly_OnDuty) && isYardMoveUseAllowed()) {
			_contentFrag.getYardMoveCheckbox().setVisibility(View.VISIBLE);

			if (GlobalState.getInstance().getIsInYardMoveDutyStatus()) {
				_contentFrag.getYardMoveCheckbox().setChecked(true);
			} else {
				if (_contentFrag.getYardMoveCheckbox().isChecked() && GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
					_contentFrag.showAnnotation();
				}
			}
		} else {
			_contentFrag.getYardMoveCheckbox().setVisibility(View.GONE);
			_contentFrag.getYardMoveCheckbox().setChecked(false);
		}

		if (dutyStatus.equals(DutyStatusEnum.Friendly_OnDuty) && isNonRegDrivingAllowed()) {
			_contentFrag.getNonRegDrivingCheckbox().setVisibility(View.VISIBLE);

			if (GlobalState.getInstance().getIsInNonRegDrivingDutyStatus()) {
				_contentFrag.getNonRegDrivingCheckbox().setChecked(true);
			}
		} else {
			_contentFrag.getNonRegDrivingCheckbox().setVisibility(View.GONE);
			_contentFrag.getNonRegDrivingCheckbox().setChecked(false);
		}
	}

	public void HandlePersonalConveyanceCheckBoxClick() {
		if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled() && _contentFrag.getPersonalConveyanceCheckbox().isChecked() && !GlobalState.getInstance().getIsInPersonalConveyanceDutyStatus()) {
			// if the eld mandate feature toggle is on AND the personal conveyance check box is visible AND that check box is checked AND the user in not already in personal conveyance,
			// display the annotation text box
			_contentFrag.showAnnotation();
		} else {
			// otherwise, hide the annotation text box
			_contentFrag.hideAnnotation();
			_contentFrag.hideErrorMessage();
		}
	}

	public void HandleYardMoveCheckBoxClick() {
		if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled() && _contentFrag.getYardMoveCheckbox().isChecked()) {
			_contentFrag.getPersonalConveyanceCheckbox().setChecked(false);
			_contentFrag.getUtilizeHyrailCheckbox().setChecked(false);
			_contentFrag.getNonRegDrivingCheckbox().setChecked(false);

			if(!GlobalState.getInstance().getIsInYardMoveDutyStatus())
				_contentFrag.showAnnotation();
		} else {
			_contentFrag.hideAnnotation();
			_contentFrag.hideErrorMessage();
		}
	}

	public void HandleNonRegDrivingCheckBoxClick() {
		if (GlobalState.getInstance().getFeatureService().getNonRegDrivingEnabled() && _contentFrag.getNonRegDrivingCheckbox().isChecked()) {
			_contentFrag.hideAnnotation();
			_contentFrag.hideErrorMessage();
			_contentFrag.getUtilizeHyrailCheckbox().setChecked(false);
			_contentFrag.getYardMoveCheckbox().setChecked(false);
			_contentFrag.getPersonalConveyanceCheckbox().setChecked(false);
		}
	}

	public void HandleHyrailCheckBoxClick() {
		if (GlobalState.getInstance().getFeatureService().getHyrailEnabled() && _contentFrag.getUtilizeHyrailCheckbox().isChecked() && !GlobalState.getInstance().getIsInHyrailDutyStatus()) {
			_contentFrag.hideAnnotation();
			_contentFrag.hideErrorMessage();
			_contentFrag.getNonRegDrivingCheckbox().setChecked(false);
			_contentFrag.getYardMoveCheckbox().setChecked(false);
			_contentFrag.getPersonalConveyanceCheckbox().setChecked(false);
		}
	}

	private void performSave() {

		if (IsTheDriver(GlobalState.getInstance().getCurrentUser()) && _contentFrag.getPersonalConveyanceCheckbox() != null)
			GlobalState.getInstance().setIsInPersonalConveyanceDutyStatus(_contentFrag.getPersonalConveyanceCheckbox().isChecked());

		if (IsTheDriver(GlobalState.getInstance().getCurrentUser()) && _contentFrag.getUtilizeHyrailCheckbox() != null)
			GlobalState.getInstance().setIsInHyrailDutyStatus(_contentFrag.getUtilizeHyrailCheckbox().isChecked());

		if (IsTheDriver(GlobalState.getInstance().getCurrentUser()) && _contentFrag.getYardMoveCheckbox() != null)
			GlobalState.getInstance().setIsInYardMoveDutyStatus(_contentFrag.getYardMoveCheckbox().isChecked());

		if (IsTheDriver(GlobalState.getInstance().getCurrentUser()) && _contentFrag.getNonRegDrivingCheckbox() != null)
			GlobalState.getInstance().setIsInNonRegDrivingDutyStatus(_contentFrag.getNonRegDrivingCheckbox().isChecked());


		mSaveLocalDataTask = new SaveLocalDataTask(this.getClass().getSimpleName());
		mSaveLocalDataTask.execute();
	}

	public void handleMotionPictureProductionSelect() {
		MotionPictureProduction production = (MotionPictureProduction)_contentFrag.getMotionPictureProductionSpinner().getSelectedItem();

		String authorityName = production.getMotionPictureAuthority().GetNameAndDOTNumber();
		_contentFrag.getMotionPictureAuthorityTextView().setText(authorityName);
	}

	@Override
	protected boolean saveData() {
		Location currentLocation = this.getMyController().GetCurrentEventValues_Location();

		// get current time - don't use time from label - screen may have been
		// open for a while
		Date timestamp = this.getMyController().getCurrentClockHomeTerminalTime();

		// get selected duty status, if "off duty" or "on duty", need to remove
		// the space and store the enum string value

		String dutyStatus = _contentFrag.getDutyStatusSpinner().getSelectedItem().toString();

		if (dutyStatus.equals(getResources().getString(R.string.offduty)))
			dutyStatus = "OffDuty";
		else if (dutyStatus.equals(getResources().getString(R.string.offdutywellsite)))
			dutyStatus = "OffDutyWellSite";
		else if (dutyStatus.equals(getResources().getString(R.string.onduty)))
			dutyStatus = "OnDuty";

		String productionId= null,  authorityId = null;
		if (_contentFrag.getMotionPictureProductionSpinner().getSelectedItem() != null) {
		// Set Motion Picture values based off of selection from ComboBox/Spinner
			MotionPictureProduction production = (MotionPictureProduction)_contentFrag.getMotionPictureProductionSpinner().getSelectedItem();
			productionId = production.getMotionPictureProductionId();
			authorityId = production.getMotionPictureAuthorityId();
			// MP values are now saved on User State to be able to handle team drivers
			GlobalState.getInstance().set_currentMotionPictureAuthorityId(authorityId);
			GlobalState.getInstance().set_currentMotionPictureProductionId(productionId);
		}


		// DEBUG Code - use gps coordinates entered to test reverse geocoding
		// if debug flag is turned on and latitude and longitude are entered
		if (GlobalState.getInstance().getFeatureService().getShowDebugFunctions() && _contentFrag.getLatitudeTextView().getText().length() > 0 && _contentFrag.getLongitudeTextView().getText().length() > 0) {
			this.getMyController().PerformManualStatusChange(timestamp, DutyStatusEnum.valueOfDMOEnum(dutyStatus), currentLocation.getName(), _contentFrag.getLatitudeTextView().getText().toString(), _contentFrag.getLongitudeTextView().getText().toString(), _contentFrag.getAnnotationEditTextView().getText().toString().trim(), productionId, authorityId, null);
			// END Debug code
		} else {
			String lat = null;
			String lon = null;
			String location = currentLocation.getName();
			boolean mandateEnabled = GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled();

			if (mandateEnabled && getIntent().hasExtra(this.getResources().getString(R.string.extra_latitude))) {
				lat = getIntent().getExtras().getString(this.getResources().getString(R.string.extra_latitude));
			}
			if (mandateEnabled && getIntent().hasExtra(this.getResources().getString(R.string.extra_Longitude))) {
				lon = getIntent().getExtras().getString(this.getResources().getString(R.string.extra_Longitude));
			}
			if (mandateEnabled && _contentFrag.getLocationText().length() > 0) {
				location = _contentFrag.getLocationText();
			}

			EmployeeLog empLog = GlobalState.getInstance().getCurrentEmployeeLog();

			if(empLog != null) {
				empLog = EmployeeLogUtilities.switchOverMidnightDrivingToDutyStatus(empLog,
						DutyStatusEnum.valueOfDMOEnum(dutyStatus),
						Enums.EmployeeLogEldEventRecordOrigin.EditedEnteredByDriver
				);
			}

			this.getMyController().PerformManualStatusChange(timestamp, DutyStatusEnum.valueOfDMOEnum(dutyStatus),
					location, lat, lon, _contentFrag.getAnnotationEditTextView().getText().toString().trim(),
					productionId, authorityId, null, empLog);

			String latLonStatus = getIntent().getStringExtra(this.getResources().getString(R.string.extra_lat_lon_status));

			if(mandateEnabled && latLonStatus.length() > 0){
				if (mandateEnabled && _contentFrag.getLocationText().length() > 0) {
					getMyController().MandateUpdateLatLongStatusCode(latLonStatus, location);
				}
				else {
					getMyController().MandateUpdateLatLongStatusCode(latLonStatus);
				}
			}
		}

		// The KMB app can stop monitoring against the 5-minute threshold if a manual duty status is entered by the driver
		GlobalState.getInstance().getEobrService().getBluetoothDrivingManager().setManualDutyStatusChange();

		return true;
	}

	@Override
	public void setFragments() {
		super.setFragments();

		Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
		_contentFrag = (RodsNewStatusFrag) f;
	}

	@Override
	protected void Return(boolean success) {
		if (success) {
			setOffDutyMsgCloseBtnPressed(false);
			this.startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_SINGLE_TOP);
		} else
			Toast.makeText(this, this.getString(R.string.msgsavingstatusfailed), Toast.LENGTH_SHORT).show();
	}

	public void loadDutyStatus(DutyStatusEnum currentDutyStatus) {
		ArrayList<String> dutyStatusList = new ArrayList<>();

		boolean isStandardLog = this.getMyController().getCurrentEmployeeLog().getExemptLogType().getValue() == ExemptLogTypeEnum.NULL;

		if (this.getController().getCurrentUser().getRulesetTypeEnum().isAnyOilFieldRuleset()) {
			boolean isOperateSpecificVehicleForOilField = this.getMyController().getCurrentEmployeeLog().getIsOperatesSpecificVehiclesForOilfield();

			if (currentDutyStatus.getValue() == DutyStatusEnum.OFFDUTY) {
				if (isPersonalConveyanceAvailable())
					dutyStatusList.add(getResources().getString(R.string.offduty));
				if (isOperateSpecificVehicleForOilField)
					dutyStatusList.add(getResources().getString(R.string.offdutywellsite));
				if (isStandardLog)
					dutyStatusList.add(getResources().getString(R.string.sleeper));
				dutyStatusList.add(getResources().getString(R.string.onduty));
			} else if (currentDutyStatus.getValue() == DutyStatusEnum.OFFDUTYWELLSITE) {
				dutyStatusList.add(getResources().getString(R.string.offduty));
				if (isStandardLog)
					dutyStatusList.add(getResources().getString(R.string.sleeper));
				dutyStatusList.add(getResources().getString(R.string.onduty));
			} else if (currentDutyStatus.getValue() == DutyStatusEnum.SLEEPER) {
				dutyStatusList.add(getResources().getString(R.string.offduty));
				if (isOperateSpecificVehicleForOilField)
					dutyStatusList.add(getResources().getString(R.string.offdutywellsite));
				dutyStatusList.add(getResources().getString(R.string.onduty));
			} else if (currentDutyStatus.getValue() == DutyStatusEnum.DRIVING) {
				dutyStatusList.add(getResources().getString(R.string.offduty));
				if (isOperateSpecificVehicleForOilField)
					dutyStatusList.add(getResources().getString(R.string.offdutywellsite));
				if (isStandardLog)
					dutyStatusList.add(getResources().getString(R.string.sleeper));
				dutyStatusList.add(getResources().getString(R.string.onduty));
			} else if (currentDutyStatus.getValue() == DutyStatusEnum.ONDUTY) {
				dutyStatusList.add(getResources().getString(R.string.offduty));
				if (isOperateSpecificVehicleForOilField)
					dutyStatusList.add(getResources().getString(R.string.offdutywellsite));
				if (isStandardLog)
					dutyStatusList.add(getResources().getString(R.string.sleeper));
				// if _isManualLocationAfterAutomaticOnDuty is true, driver can stay in On-Duty and just set location.
				if (isHyrailUseAllowed() || isYardMoveUseAllowed() || isNonRegDrivingAllowed() || _isManualLocationAfterAutomaticOnDuty) {
					dutyStatusList.add(getResources().getString(R.string.onduty));
				}
			}
		} else {
			if (currentDutyStatus.getValue() == DutyStatusEnum.OFFDUTY) {
				if (isPersonalConveyanceAvailable())
					dutyStatusList.add(getResources().getString(R.string.offduty));
				if (isStandardLog)
					dutyStatusList.add(getResources().getString(R.string.sleeper));
				dutyStatusList.add(getResources().getString(R.string.onduty));
			} else if (currentDutyStatus.getValue() == DutyStatusEnum.SLEEPER) {
				dutyStatusList.add(getResources().getString(R.string.offduty));
				dutyStatusList.add(getResources().getString(R.string.onduty));
			} else if (currentDutyStatus.getValue() == DutyStatusEnum.DRIVING) {
				dutyStatusList.add(getResources().getString(R.string.offduty));
				if (isStandardLog)
					dutyStatusList.add(getResources().getString(R.string.sleeper));
				dutyStatusList.add(getResources().getString(R.string.onduty));
			} else if (currentDutyStatus.getValue() == DutyStatusEnum.ONDUTY) {
				dutyStatusList.add(getResources().getString(R.string.offduty));
				if (isStandardLog)
					dutyStatusList.add(getResources().getString(R.string.sleeper));
				// if _isManualLocationAfterAutomaticOnDuty is true, driver can stay in On-Duty and just set location.
				if (isHyrailUseAllowed() || isYardMoveUseAllowed() || isNonRegDrivingAllowed() || _isManualLocationAfterAutomaticOnDuty) {
					dutyStatusList.add(getResources().getString(R.string.onduty));
				}
			}
		}

		ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.kmb_spinner_item, dutyStatusList);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		_contentFrag.getDutyStatusSpinner().setAdapter(adapter);

		// if _isManualLocationAfterAutomaticOnDuty is true, default to On-Duty status.
		if (_isManualLocationAfterAutomaticOnDuty) {
			_contentFrag.getDutyStatusSpinner().setSelection(adapter.getPosition(getResources().getString(R.string.onduty)));
		}
	}


	public void loadMotionPicture(){

		MotionPictureController controller = new MotionPictureController(getBaseContext());
		List<MotionPictureProduction> _motionPictureProductions = controller.GetActiveMotionPictureProductions();

		if (_motionPictureProductions.size() > 1){
			ArrayAdapter<MotionPictureProduction> spinnerAdapter = new ArrayAdapter<>(this, R.layout.kmb_spinner_item, _motionPictureProductions);
			_contentFrag.getMotionPictureProductionSpinner().setAdapter(spinnerAdapter);
			String productionId =  getMyController().getCurrentDesignatedDriver().getUserState().getMotionPictureProductionId();
			spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			if (productionId != null && productionId.length() > 0) {
				for (int i=0; i < spinnerAdapter.getCount(); i++){
					MotionPictureProduction production = spinnerAdapter.getItem(i);
					if (productionId.equals(production.getMotionPictureProductionId())){
						_contentFrag.getMotionPictureProductionSpinner().setSelection(i);
						_contentFrag.getMotionPictureAuthorityTextView().setText (production.getMotionPictureAuthority().GetNameAndDOTNumber());
						break;
					}
				}
			}
		}
	}

	public LogEntryController getMyController() {
		return (LogEntryController) this.getController();
	}

	public EmployeeLogEldMandateController getMyEmployeeLogELDMandateController() {
		if (_employeeLogEldMandateController == null) {
			_employeeLogEldMandateController = new EmployeeLogEldMandateController(this);
		}
		return _employeeLogEldMandateController;
	}

	@Override
	protected void InitController() {
		LogEntryController ctrlr = new LogEntryController(this);
		this.setController(ctrlr);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		this.CreateOptionsMenu(menu, false);
		return true;
	}

	private boolean isPersonalConveyanceAvailable() {
		return GlobalState.getInstance().getFeatureService().getPersonalConveyanceEnabled()
				&& this.getMyController().getCurrentUser().getIsPersonalConveyanceAllowed()
				&& IsTheDriver(GlobalState.getInstance().getCurrentUser());
	}

	@Override
	protected void onSpecialDrivingEndResponse(EmployeeLogProvisionTypeEnum drivingCategory, boolean ended) {
		ISpecialDrivingController controller = SpecialDrivingFactory.getControllerInDrivingSegment();

		if(ended) {
			if(controller != null) {
				// The period is being ended prematurely
				// must end the special driving segment
				controller.EndSpecialDrivingStatus();
			}

			performSave();
		} else {
			//was only being done for PC prior to refactoring
			if(controller != null && drivingCategory == EmployeeLogProvisionTypeEnum.PERSONALCONVEYANCE) {
				controller.setIsInSpecialDutyStatus(true);
			}
		}
	}

	private boolean isHyrailUseAllowed() {
		return GlobalState.getInstance().getFeatureService().getHyrailEnabled()
				&& this.getController().getCurrentUser().getIsHyrailAllowed()
				&& IsTheDriver(GlobalState.getInstance().getCurrentUser());
	}

	public boolean isAnnotationFieldValid() {
		return _contentFrag.getAnnotationEditTextView().getText().toString().trim().length() >= 4;
	}

	private boolean isYardMoveUseAllowed() {
		return (this.getController().getCurrentUser().getYardMoveAllowed() && IsTheDriver(GlobalState.getInstance().getCurrentUser()) && GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled());
	}

	private boolean isNonRegDrivingAllowed() {
		return (this.getController().getCurrentUser().getIsNonRegDrivingAllowed() && IsTheDriver(GlobalState.getInstance().getCurrentUser()) && GlobalState.getInstance().getFeatureService().getNonRegDrivingEnabled());
	}

	public boolean ShouldShowManualLocation(){
		ELDCommon eldCommon = new ELDCommon(getIntent(),this.getResources(),getMyController(),getMyEmployeeLogELDMandateController());
		return eldCommon.ShouldShowManualLocation();
	}

	public boolean getLocalDataTask() {
		// return true if FetchLocalData is complete
		if(mFetchLocalDataTask == null)
			return true;
		else
			return false;
	}

	public void processNoLocationAfterAutomaticOnDuty() {
		// this would only happen if the vehicle started moving and a driving period
		// was generated while the NewDutyStatus screen was displayed

		EmployeeLogEldMandateController mandateCtrl = new EmployeeLogEldMandateController(GlobalState.getInstance());

		// record 'X' as lat/lon status code on auto-generated On Duty event
		getMyController().MandateUpdateLatLongStatusCode(mandateCtrl.POSITION_MALFUNCTION_WARNING_STATUS_CODE);

		LocalBroadcastManager.getInstance(this).unregisterReceiver(drivingStartedReceiver);
	}

	private BroadcastReceiver drivingStartedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			processNoLocationAfterAutomaticOnDuty();
		}
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		LocalBroadcastManager.getInstance(this).unregisterReceiver(drivingStartedReceiver);
	}

    private AsyncTask<Void, Void, Void> updateMissingDataDiagnosticTask = new AsyncTask<Void, Void, Void>() {
        ProgressDialog saveDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            LockScreenRotation();
            saveDialog = CreateSaveDialog();
            saveDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            EmployeeLog employeeLog = getMyController().getCurrentEmployeeLog();
            EmployeeLogEldEvent editedEvent = getMyController().getLogEventForEdit();
            try {
                getMyEmployeeLogELDMandateController().updateMissingDataDiagnosticForEvent(employeeLog, editedEvent);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            saveDialog.dismiss();
            UnlockScreenRotation();
            Return();
        }
    };

}
