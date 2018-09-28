package com.jjkeller.kmb.test;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.TextView;

import com.jjkeller.kmb.SplashScreen;
import com.jjkeller.kmb.TestableSplashScreen;
import com.jjkeller.kmbapi.configuration.GlobalState;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class KMBUISplashScreenTest  {

    private TextView mView;
    private String resourceString;

    @Rule
    public ActivityTestRule<TestableSplashScreen> mActivityRule
            = new ActivityTestRule<>(TestableSplashScreen.class);

    @Before
    public void setUp() throws Exception {

        SplashScreen mActivity = mActivityRule.getActivity();
        mView = (TextView) mActivity.findViewById(com.jjkeller.kmbui.R.id.lblversionnumber);
        resourceString = GlobalState.getInstance().getPackageVersionName();
    }

    @SuppressWarnings("unused")
    @Test
    public void testPreconditions() {
        Assert.assertNotNull(mView);
    }

    @SuppressWarnings("unused")
    @Test
    public void testText() {
        Assert.assertEquals(resourceString, (String) mView.getText());
    }
}
