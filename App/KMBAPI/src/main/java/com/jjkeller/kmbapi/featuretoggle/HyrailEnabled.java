package com.jjkeller.kmbapi.featuretoggle;
import com.jjkeller.kmbapi.configuration.AppSettings;

// PBI #32111
public class HyrailEnabled extends FeatureToggle<HyrailEnabled> {
    public HyrailEnabled(AppSettings _appSettings)
    {
        super(HyrailEnabled.class, _appSettings);
    }
}