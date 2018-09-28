package com.jjkeller.kmbapi.featuretoggle;

import com.jjkeller.kmbapi.configuration.AppSettings;

public class ELDMandate extends FeatureToggle<ELDMandate> {

	public ELDMandate(AppSettings _appSettings) {
		super(ELDMandate.class, _appSettings);
	}

}
