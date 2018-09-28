package com.jjkeller.kmbapi.featuretoggle;

import com.jjkeller.kmbapi.configuration.AppSettings;

public class SelectiveFeatureToggle extends FeatureToggle<SelectiveFeatureToggle> {

    public SelectiveFeatureToggle(AppSettings _appSettings) {
        super(SelectiveFeatureToggle.class, _appSettings);
    }
}