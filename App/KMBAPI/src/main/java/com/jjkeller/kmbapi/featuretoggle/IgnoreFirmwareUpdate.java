package com.jjkeller.kmbapi.featuretoggle;

import com.jjkeller.kmbapi.configuration.AppSettings;

public class IgnoreFirmwareUpdate extends FeatureToggle<IgnoreFirmwareUpdate> {

	public IgnoreFirmwareUpdate(AppSettings _appSettings) {
		super(IgnoreFirmwareUpdate.class, _appSettings);
	}

}
