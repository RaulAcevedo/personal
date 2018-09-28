package com.jjkeller.kmbapi.geotab.tests.thirdparty;

import com.jjkeller.kmbapi.common.Hex;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.featuretoggle.FeatureToggleService;
import com.jjkeller.kmbapi.geotabengine.GeotabDataEnhanced;
import com.jjkeller.kmbapi.geotabengine.GeotabUsbService;
import com.jjkeller.kmbapi.geotabengine.ThirdParty;
import com.jjkeller.kmbapi.common.TestBase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by jld5296 on 10/4/16.
 */
@RunWith(RobolectricTestRunner.class)
@Config(application = GlobalState.class,
        manifest = "/build/intermediates/bundles/encompass/debug/AndroidManifest.xml"
)
public class ThirdPartyTest extends TestBase {

    private GlobalState app;

    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        app = (GlobalState) RuntimeEnvironment.application;

        FeatureToggleService ftService = mock(FeatureToggleService.class);
        when(ftService.getIsEldMandateEnabled()).thenReturn(false);
    }

    @Test
    public void extractEnhancedHOSData() throws Exception {
        GeotabUsbService service = mock(GeotabUsbService.class);

        ThirdParty tp = new ThirdParty(service);
        byte[] abData = Hex.ConvertToHex("02 21 24 4F D4 C2 1B F4 45 64 1A DD B3 46 CB 28 D0 07 E8 03 00 00 0F 01 00 00 00 00 00 00 00 00 00 00 00 59 1B D9 20 F4 35 03");
        tp.ExtractEnhancedHOSData(abData);
        GeotabDataEnhanced data = tp.getGeotabData();
    }

}