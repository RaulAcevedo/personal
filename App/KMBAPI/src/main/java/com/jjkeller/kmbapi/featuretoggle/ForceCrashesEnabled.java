package com.jjkeller.kmbapi.featuretoggle;

import com.jjkeller.kmbapi.configuration.AppSettings;

// PBI #49690
public class ForceCrashesEnabled extends  FeatureToggle<ForceCrashesEnabled> {
    public ForceCrashesEnabled(AppSettings _appSettings){
        super (ForceCrashesEnabled.class, _appSettings);
    }
}