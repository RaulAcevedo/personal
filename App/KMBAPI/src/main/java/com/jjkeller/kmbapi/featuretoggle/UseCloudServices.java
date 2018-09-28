package com.jjkeller.kmbapi.featuretoggle;

import com.jjkeller.kmbapi.configuration.AppSettings;

public class UseCloudServices extends FeatureToggle<UseCloudServices> {

	public UseCloudServices(AppSettings _appSettings) {
		super(UseCloudServices.class, _appSettings);
	}

}
