package com.jjkeller.kmbapi.featuretoggle;

import com.jjkeller.kmbapi.configuration.AppSettings;

public class NewTeamDriverWorkflowEnabled extends FeatureToggle<NewTeamDriverWorkflowEnabled> {
	public NewTeamDriverWorkflowEnabled(AppSettings _appSettings)
	{
		super(NewTeamDriverWorkflowEnabled.class, _appSettings);
	}
}
