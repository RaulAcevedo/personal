package com.jjkeller.kmbapi.featuretoggle;

import com.jjkeller.kmbapi.configuration.AppSettings;

public class PersonalConveyanceEnabled extends FeatureToggle<PersonalConveyanceEnabled> {
	public PersonalConveyanceEnabled(AppSettings _appSettings)
	{
		super(PersonalConveyanceEnabled.class, _appSettings);
	}
}
