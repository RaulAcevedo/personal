package com.jjkeller.kmb;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;

import com.actionbarsherlock.view.Menu;
import com.jjkeller.kmb.firmware.EobrServiceShim;
import com.jjkeller.kmb.fragments.EobrConfigFrag;
import com.jjkeller.kmb.interfaces.IEobrConfig.EobrConfigFragActions;
import com.jjkeller.kmb.interfaces.IEobrConfig.EobrConfigFragControllerMethods;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.EOBR.EobrReader;
import com.jjkeller.kmbapi.controller.EOBR.FirmwareUpdateService;
import com.jjkeller.kmbapi.controller.EOBR.FirmwareUpgraderFactory;
import com.jjkeller.kmbapi.controller.EobrConfigController;
import com.jjkeller.kmbapi.controller.interfaces.IEobrService;
import com.jjkeller.kmbapi.controller.interfaces.IFirmwareUpgrader;
import com.jjkeller.kmbapi.controller.share.KmbApplicationException;
import com.jjkeller.kmbapi.enums.DatabusTypeEnum;
import com.jjkeller.kmbui.R;

public class EobrConfig extends BaseActivity implements EobrConfigFragControllerMethods, EobrConfigFragActions {
	private EobrConfigFrag _contentFragment;

	private boolean _showingPrompt = false;
	private boolean _loginProcess = false;


	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.baselayout);

		mFetchLocalDataTask = new FetchLocalDataTask(this.getClass().getSimpleName());
		mFetchLocalDataTask.execute();

		//if we're supposed to go to TripInfo after activity, then we're part of the login process
		_loginProcess = getIntent().getBooleanExtra(this.getResources().getString(R.string.extra_displaytripinfo), _loginProcess);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (_loginProcess) {
			//disable the back button in view only mode
			if (keyCode == KeyEvent.KEYCODE_BACK) {
				return true;
			}
		}

		return super.onKeyDown(keyCode, event);
	}

	public EobrConfigController getMyController() {
		return (EobrConfigController) this.getController();
	}

	@Override
	protected void InitController() {
		this.setController(new EobrConfigController(this));
	}

	@Override
	public void setFragments() {
		super.setFragments();
		Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
		_contentFragment = (EobrConfigFrag) fragment;
	}

	@Override
	protected void loadControls() {
		super.loadControls();
		loadContentFragment(new EobrConfigFrag());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		this.CreateOptionsMenu(menu, false);
		return true;
	}

	@Override
	protected void Return(boolean success) {
		boolean showTripInfo = false;
		boolean isTeamDriver = false;

		if (success) {
			this.finish();
			boolean isExemptLogEnabled = GlobalState.getInstance().getCurrentUser().getIsMobileExemptLogAllowed();
			boolean isExemptFromEldUse = GlobalState.getInstance().getCurrentUser().getExemptFromEldUse();
            boolean isELDMandateEnabled = GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled();

			isTeamDriver = getIntent().getBooleanExtra(getString(R.string.extra_teamdriverlogin), isTeamDriver);
			showTripInfo = getIntent().getBooleanExtra(this.getResources().getString(R.string.extra_displaytripinfo), showTripInfo);

			if (isTeamDriver) {
				startActivity(TeamDriverDeviceType.class);
			} else if (showTripInfo) {
				Bundle extras = new Bundle();
				extras.putString(this.getResources().getString(R.string.extra_tripinfomsg), this.getResources().getString(R.string.extra_tripinfomsg));
				extras.putBoolean(getString(R.string.extra_isloginprocess), _loginProcess);

				// if Exempt is Enabled and the EmployeeLog ruleset is US60||US70, go to ExemptLogType screen, else SelectDutyStaus screen
				// So, if the EmployeeLog contains nothing, then the users default ruleset is used.
				if ((isExemptLogEnabled && GlobalState.getInstance().getCurrentUser().getRulesetTypeEnum().isUSFederalRuleset()) || (isExemptFromEldUse && isELDMandateEnabled)) {
					startActivity(ExemptLogType.class, Intent.FLAG_ACTIVITY_SINGLE_TOP, extras);
				}
				//Go to SelectDutyStatus if on Mandate or AOBRD
				else if(_loginProcess){
					startActivity(SelectDutyStatus.class, Intent.FLAG_ACTIVITY_SINGLE_TOP, extras);
				}
			} else if(_loginProcess) {
				//Go to SelectDutyStatus if showTripInfo is false
				this.startActivity(SelectDutyStatus.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
			}
			else{
				//if not part of the login process (e.g. coming from the SetEobrConfig screen, return to RODS
				this.startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
			}
		} else {
			if (!_showingPrompt) {
				String lblMessage = this.getString(R.string.msg_eobrconfigerr);
				if (_contentFragment.getDatabusType().getSelectedItemPosition() == 0) {
					lblMessage += "\n";
					lblMessage += this.getString(R.string.msg_eobrconfigerr_autodetect);
				}

				ShowMessage(this, null, lblMessage,
						new ShowMessageClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								super.onClick(dialog, id);

								/*
								Go to SelectDutyStatus before
								RODS Entry screen if EOBR is not online so user can select
								duty status	before attempting to reconnect to ELD
								*/
								if (!EobrConfig.this.getMyController().IsEobrDeviceOnline() && _loginProcess) {
									Bundle extras = new Bundle();
									extras.putBoolean(getString(R.string.extra_isloginprocess), _loginProcess);
									EobrConfig.this.finish();
									EobrConfig.this.startActivity(SelectDutyStatus.class, Intent.FLAG_ACTIVITY_CLEAR_TOP, extras);
								}
								//if not part of the login process (e.g. coming from the SetEobrConfig screen, return to RODS
								else if (!EobrConfig.this.getMyController().IsEobrDeviceOnline()) {
									EobrConfig.this.finish();
									EobrConfig.this.startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
								}
							}
						});
			}

			_showingPrompt = false;
		}
	}

	public void handleCancelButtonClick() {
		this.Return();
	}

	public void handleSaveButtonClick() {
		if (IsConfigurationValid()) {
			if (getMyController().getIsNetworkAvailable()) {
				// need to kick off async task as network cannot be accessed on main thread
				checkUpgradeRequired();
			} else {
				ShowMessage(this, getString(R.string.msgnetworknotavailable));
			}
		}
	}

	public void saveConfig() {
		mSaveLocalDataTask = new SaveLocalDataTask(this.getClass().getSimpleName());
		mSaveLocalDataTask.execute();
	}


	@Override
	protected boolean saveData() {
		boolean isSuccessful = false;

		String tractorNumber = _contentFragment.getCurrentTractorNumber().getText().toString();
		String busType;
		if(_contentFragment.getDatabusType().getSelectedItem() != null)
			busType = _contentFragment.getDatabusType().getSelectedItem().toString();
		else {
			// Just set EobrId for Geotab devices.
			// Default bus type sent to prevent auto-detect from being performed.
			busType = new DatabusTypeEnum(DatabusTypeEnum.J1939).toDMOEnum();
		}

		if (tractorNumber.length() > 0) {
			try {
				isSuccessful = this.getMyController().PerformEobrConfiguration(tractorNumber, busType);

				if (isSuccessful) {
					SharedPreferences userPref = this.getSharedPreferences(getString(R.string.sharedpreferencefile), 0);

					//if this EOBR was set as default then we need to update the default tractor number
					if (userPref.getString(getString(R.string.defaulteobr), "").equalsIgnoreCase(_contentFragment.getInitialTractorNumberValue())) {
						SharedPreferences.Editor editor = userPref.edit();
						editor.putString(getString(R.string.defaulteobr), tractorNumber);
						editor.commit();
					}
				}
			} catch (final KmbApplicationException kae) {
				_showingPrompt = true;

				this.runOnUiThread(new Runnable() {
					public void run() {
						EobrConfig.this.HandleException(kae);
					}
				});
			}
		}

		return isSuccessful;
	}

	private boolean IsConfigurationValid() {
		boolean isValid = true;

		if (_contentFragment.getCurrentTractorNumber().getText() == null || _contentFragment.getCurrentTractorNumber().getText().length() == 0) {
			// tractor number is required
			this.ShowMessage(this, this.getString(R.string.msg_eobrconfigtractornumber));
			return false;
		}

		return isValid;
	}

	private void checkUpgradeRequired() {
		DatabusTypeEnum busType = getSelectedDatabusType();

		if (busType.getValue() != DatabusTypeEnum.DUALMODEJ1708J1939F && busType.getValue() != DatabusTypeEnum.J1939F) {
			saveConfig();
			return;
		}

		IsUpgradeRequiredTask isUpgradeRequiredTask = new IsUpgradeRequiredTask();
		isUpgradeRequiredTask.execute();
	}

	private void promptFirmwareUpdateMessage() {
		ShowConfirmationWithCancelMessage(this, this.getString(R.string.lblfirwareupdate),
				new ShowMessageClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						super.onClick(dialog, id);

						FirmwareUpdateService.startFirmwareUpgrade(EobrConfig.this, getFirmwareUpgrader(), false);
					}
				},
				new ShowMessageClickListener()
		);
	}

	private DatabusTypeEnum getSelectedDatabusType() {
		int busTypePosition = _contentFragment.getDatabusType().getSelectedItemPosition() + 1;

		return new DatabusTypeEnum(busTypePosition);
	}


	private IFirmwareUpgrader getFirmwareUpgrader() {
		DatabusTypeEnum busType = getSelectedDatabusType();

		IEobrService eobrService = (EobrService) GlobalState.getInstance().getEobrService();
		if (eobrService == null) {
			eobrService = new EobrServiceShim();
		}

		EobrReader eobrReader = EobrReader.getInstance();
		return FirmwareUpgraderFactory.GetFirmwareUpgrader(eobrReader, eobrService, busType);

	}

	private class IsUpgradeRequiredTask extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... voids) {
			return getFirmwareUpgrader().getIsFirmwareUpgradeRequired();
		}

		@Override
		protected void onPostExecute(Boolean shouldUpgrade) {
			if(shouldUpgrade) {
				promptFirmwareUpdateMessage();
			} else {
				saveConfig();
			}
		}
	}

}
