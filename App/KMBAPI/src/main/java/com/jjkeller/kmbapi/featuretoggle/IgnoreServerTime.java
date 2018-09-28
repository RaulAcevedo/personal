package com.jjkeller.kmbapi.featuretoggle;

import com.jjkeller.kmbapi.configuration.AppSettings;

public class IgnoreServerTime extends FeatureToggle<IgnoreServerTime> {

	public IgnoreServerTime(AppSettings _appSettings) {
		super(IgnoreServerTime.class, _appSettings);
	}

}
