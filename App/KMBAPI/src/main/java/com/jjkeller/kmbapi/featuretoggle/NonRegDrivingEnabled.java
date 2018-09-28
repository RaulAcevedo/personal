package com.jjkeller.kmbapi.featuretoggle;
import com.jjkeller.kmbapi.configuration.AppSettings;

// PBI #32111
public class NonRegDrivingEnabled extends FeatureToggle<NonRegDrivingEnabled> {
    public NonRegDrivingEnabled(AppSettings _appSettings)
    {
        super(NonRegDrivingEnabled.class, _appSettings);
    }
}