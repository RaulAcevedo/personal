package com.jjkeller.kmbapi.featuretoggle;

import com.jjkeller.kmbapi.configuration.AppSettings;

public class ForceComplianceTabletMode extends FeatureToggle<ForceComplianceTabletMode> {

	public ForceComplianceTabletMode(AppSettings appSettings) {
		super(ForceComplianceTabletMode.class, appSettings);		
	}

}
