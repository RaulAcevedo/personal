package com.jjkeller.kmb.test.kmbapi.calcengine.Federal;

import android.test.AndroidTestCase;
import android.test.InstrumentationTestCase;

import com.jjkeller.kmbapi.configuration.AppSettings;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by jld5296 on 11/3/16.
 */
public class TestBase extends InstrumentationTestCase {

    public void setUp(){
        System.setProperty(
                "dexmaker.dexcache",
                getInstrumentation().getTargetContext().getCacheDir().getPath());
    }

    protected void setupAppSettingsMocks() {
        AppSettings appSettinsMock = mock(AppSettings.class);
        when(appSettinsMock.getStatusRecordLogIntervalMS()).thenReturn(60000);
    }
}
