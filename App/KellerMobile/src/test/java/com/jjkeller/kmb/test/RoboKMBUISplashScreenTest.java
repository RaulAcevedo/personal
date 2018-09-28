package com.jjkeller.kmb.test;

import android.annotation.SuppressLint;
import android.widget.TextView;

import com.jjkeller.kmb.SplashScreen;
import com.jjkeller.kmbapi.configuration.GlobalState;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.res.builder.RobolectricPackageManager;

@SuppressLint("ro.build.date.utc")
@RunWith(KMBRoboElectricTestRunner.class)
public class RoboKMBUISplashScreenTest {

    private TextView mView;
    private String resourceString;


    @Before
    public void setUp() throws Exception {
        SplashScreen mActivity = Robolectric.setupActivity(SplashScreen.class);
        mView = (TextView) mActivity.findViewById(com.jjkeller.kmb.R.id.lblversionnumber);
        resourceString = GlobalState.getInstance().getPackageVersionName();
    }

    @Test
    public void testPreconditions() {

        Assert.assertNotNull(mView);
    }

    @Test
    public void testText() {
        Assert.assertEquals(resourceString,(String)mView.getText());
    }
}

