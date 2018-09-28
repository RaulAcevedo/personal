package com.jjkeller.kmb.test;


import android.content.Context;

import com.jjkeller.kmbapi.configuration.AppSettings;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.configuration.LoginCredentials;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.featuretoggle.FeatureToggleService;

import org.junit.Before;
import org.robolectric.Robolectric;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by jld5296 on 11/3/16.
 */
public abstract class KmbRoboTestBase {


    @Before
    public void setUp() throws Exception {
        GlobalState globalState = (GlobalState)Robolectric.application;

        FeatureToggleService ftService = mock(FeatureToggleService.class);
        when(ftService.getIsEldMandateEnabled()).thenReturn(true);

        LoginCredentials creds = mock(LoginCredentials.class);
        when(creds.getEmployeeId()).thenReturn("12345678-1234-1234-1234-123456789012");

        User user = mock(User.class);
        when(user.getCredentials()).thenReturn(creds);

        globalState.setCurrentUser(user);
    }

    protected void setupAppSettingsMocks() {
        AppSettings appSettinsMock = mock(AppSettings.class);
        when(appSettinsMock.getStatusRecordLogIntervalMS()).thenReturn(60000);
    }


    protected Context getContext(){
        return Robolectric.getShadowApplication().getApplicationContext();
    }
}
