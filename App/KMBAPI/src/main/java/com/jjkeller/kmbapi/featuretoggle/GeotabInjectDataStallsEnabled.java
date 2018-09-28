package com.jjkeller.kmbapi.featuretoggle;
import com.jjkeller.kmbapi.configuration.AppSettings;

// PBI #32111
public class GeotabInjectDataStallsEnabled extends FeatureToggle<GeotabInjectDataStallsEnabled> {
    public GeotabInjectDataStallsEnabled(AppSettings _appSettings)
    {
        super(GeotabInjectDataStallsEnabled.class, _appSettings);
    }
}