package com.jjkeller.kmbapi.featuretoggle;

import com.jjkeller.kmbapi.configuration.AppSettings;

public class AlkCopilotEnabled extends FeatureToggle<AlkCopilotEnabled> {
	public AlkCopilotEnabled(AppSettings _appSettings)
	{
		super(AlkCopilotEnabled.class, _appSettings);
	}
}
