package com.jjkeller.kmbapi.featuretoggle;

import com.jjkeller.kmbapi.configuration.AppSettings;

public class DefaultTripInformation  extends FeatureToggle<DefaultTripInformation> {

	public DefaultTripInformation(AppSettings _appSettings) {
		super(DefaultTripInformation.class, _appSettings);
	}

}