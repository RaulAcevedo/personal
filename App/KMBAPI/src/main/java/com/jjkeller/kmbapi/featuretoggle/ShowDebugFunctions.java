package com.jjkeller.kmbapi.featuretoggle;

import com.jjkeller.kmbapi.configuration.AppSettings;

public class ShowDebugFunctions extends FeatureToggle<ShowDebugFunctions> {

	public ShowDebugFunctions(AppSettings _appSettings) {
		super(ShowDebugFunctions.class, _appSettings);
	}

}
