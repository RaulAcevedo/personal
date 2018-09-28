package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;

import com.jjkeller.kmb.interfaces.IFeatureToggle.FeatureToggleFragActions;
import com.jjkeller.kmb.interfaces.IFeatureToggle.FeatureToggleFragControllerMethods;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.utility.DeviceInfo;
import com.jjkeller.kmbui.R;
import com.jjkeller.kmbui.R.id;

public class FeatureToggleFrag extends BaseFragment {
	FeatureToggleFragActions actionsListener;
	FeatureToggleFragControllerMethods controlListener;

	private CheckBox _chkSelectiveFeatureToggles;
	private CheckBox _chkEldMandate;
	private CheckBox _chkIgnoreServerTime;
	private CheckBox _chkShowDebugFunctions;
	private CheckBox _chkUseCloudServices;
	private CheckBox _chkDefaultTripInformation;
	private CheckBox _chkAlkCopilotEnabled;
	private CheckBox _chkPersonalConveyanceEnabled;
	private CheckBox _chkIgnoreFirmwareUpdate;
	private CheckBox _chkForceComplianceTabletMode;
	private CheckBox _chkSetMobileTimestampToNull;
	private CheckBox _chkHyrailEnabled;
	private CheckBox _chkNonRegDrivingEnabled;
	private CheckBox _chkGeotabInjectDataStallsEnabled;
	private CheckBox _chkForceCrashes;

	private Button _btnOK;
	private Button _btnCancel;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.f_featuretoggle, container, false);
		findControls(v);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		this.loadControls();
	}

	private void loadControls() {
		_chkSelectiveFeatureToggles.setChecked(GlobalState.getInstance().getFeatureService().getSelectiveFeatureTogglesEnabled());
		_chkEldMandate.setChecked(GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled());
		_chkIgnoreServerTime.setChecked(GlobalState.getInstance().getFeatureService().getIgnoreServerTime());
		_chkShowDebugFunctions.setChecked(GlobalState.getInstance().getFeatureService().getShowDebugFunctions());
		_chkUseCloudServices.setChecked(GlobalState.getInstance().getFeatureService().getUseCloudServices());
		_chkDefaultTripInformation.setChecked(GlobalState.getInstance().getFeatureService().getDefaultTripInformation());
		_chkAlkCopilotEnabled.setChecked(GlobalState.getInstance().getFeatureService().getAlkCopilotEnabled());
		_chkPersonalConveyanceEnabled.setChecked(GlobalState.getInstance().getFeatureService().getPersonalConveyanceEnabled());
		_chkIgnoreFirmwareUpdate.setChecked(GlobalState.getInstance().getFeatureService().getIgnoreFirmwareUpdate());
		_chkForceComplianceTabletMode.setChecked(GlobalState.getInstance().getFeatureService().getForceComplianceTabletMode());
		_chkSetMobileTimestampToNull.setChecked(GlobalState.getInstance().getFeatureService().getSetMobileStartTimestampToNull());
		_chkHyrailEnabled.setChecked(GlobalState.getInstance().getFeatureService().getHyrailEnabled());
		_chkNonRegDrivingEnabled.setChecked(GlobalState.getInstance().getFeatureService().getNonRegDrivingEnabled());
		_chkGeotabInjectDataStallsEnabled.setChecked(GlobalState.getInstance().getFeatureService().getGeotabInjectDataStallsEnabled());
		_chkForceCrashes.setChecked(GlobalState.getInstance().getFeatureService().getIsForceCrashesEnabled());

		if (!(DeviceInfo.IsComplianceTablet())) {
			_chkAlkCopilotEnabled.setVisibility(View.GONE);
		}
	}

	protected void findControls(View v) {
        _chkSelectiveFeatureToggles = (CheckBox) v.findViewById(id.chkSelectiveFeatureToggles);
		_chkEldMandate = (CheckBox) v.findViewById(R.id.chkEldMandate);
		_chkIgnoreServerTime = (CheckBox) v.findViewById(R.id.chkIgnoreServerTime);
		_chkShowDebugFunctions = (CheckBox) v.findViewById(R.id.chkShowDebugFunctions);
		_chkUseCloudServices = (CheckBox) v.findViewById(R.id.chkUseCloudServices);
		_chkDefaultTripInformation = (CheckBox) v.findViewById(R.id.chkDefaultTripInformation);
		_chkAlkCopilotEnabled = (CheckBox) v.findViewById(R.id.chkAlkCopilotEnabled);
		_chkPersonalConveyanceEnabled = (CheckBox) v.findViewById(R.id.chkPersonalConveyanceEnabled);
		_chkIgnoreFirmwareUpdate = (CheckBox) v.findViewById(R.id.chkIgnoreFirmwareUpdate);
		_chkForceComplianceTabletMode = (CheckBox) v.findViewById(id.chkForceComplianceTabletMode);
		_chkSetMobileTimestampToNull = (CheckBox) v.findViewById(id.chkSetMobileTimestampToNull);
		_chkHyrailEnabled = (CheckBox) v.findViewById(R.id.chkHyrailEnabled);
		_chkNonRegDrivingEnabled = (CheckBox) v.findViewById(id.chkNonRegDrivingEnabled);
		_chkGeotabInjectDataStallsEnabled = (CheckBox) v.findViewById(id.chkGeotabInjectDataStallsEnabled);
		_chkForceCrashes = (CheckBox) v.findViewById(id.chkForceCrashes);
		_btnCancel = (Button) v.findViewById(R.id.ft_btnCancel);
		_btnOK = (Button) v.findViewById(R.id.ft_btnOk);

		_btnOK.setOnClickListener(
				new OnClickListener() {
					public void onClick(View v) {
						actionsListener.handleOkButtonClick();
					}

				});

		_btnCancel.setOnClickListener(
				new OnClickListener() {
					public void onClick(View v) {
						actionsListener.handleCancelButtonClick();
					}
				});
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			actionsListener = (FeatureToggleFragActions) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement FeatureToggleFragActions");
		}

		try {
			controlListener = (FeatureToggleFragControllerMethods) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement FeatureToggleFragControllerMethods");
		}
	}

	public boolean getSelectiveFeatureTogglesEnabled() {
        return _chkSelectiveFeatureToggles.isChecked();
    }

	public boolean getIsEldMandateEnabled() {
		return _chkEldMandate.isChecked();
	}

	public boolean getIgnoreServerTime() {
		return _chkIgnoreServerTime.isChecked();
	}

	public boolean getShowDebugFunctions() {
		return _chkShowDebugFunctions.isChecked();
	}

	public boolean getUseCloudServices() {
		return _chkUseCloudServices.isChecked();
	}

	public boolean getDefaultTripInformation() {
		return _chkDefaultTripInformation.isChecked();
	}

	public boolean getAlkCopilotEnabled() {
		return _chkAlkCopilotEnabled.isChecked();
	}

	public boolean getPersonalConveyanceEnabled() {
		return _chkPersonalConveyanceEnabled.isChecked();
	}


	public boolean getIgnoreFirmwareUpdate() {
		return _chkIgnoreFirmwareUpdate.isChecked();
	}

	public boolean getForceComplianceTabletMode() {
		return _chkForceComplianceTabletMode.isChecked();
	}

	public boolean getSetMobileStartTimestampToNull() {
		return _chkSetMobileTimestampToNull.isChecked();
	}

	public boolean getHyrailEnabled() {
		return _chkHyrailEnabled.isChecked();
	}

	public boolean getNonRegDrivingEnabled() {
		return _chkNonRegDrivingEnabled.isChecked();
	}

	public boolean getGeotabInjectDataStallsEnabled() {
		return _chkGeotabInjectDataStallsEnabled.isChecked();}

	public boolean getIsForceCrashesEnabled(){ return  _chkForceCrashes.isChecked(); }
}


