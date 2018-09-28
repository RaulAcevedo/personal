package com.jjkeller.kmbapi.controller.utility;

import com.jjkeller.kmbapi.common.TestBase;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.featuretoggle.IFeatureToggleService;
import com.jjkeller.kmbapi.proxydata.CompanyConfigSettings;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(application = GlobalState.class,
        manifest = "/build/intermediates/bundles/encompass/debug/AndroidManifest.xml"
)
public class WebAPIServiceHelperFactoryTest extends TestBase {

    private GlobalState app;
    private IFeatureToggleService featureToggleService;
    private CompanyConfigSettings companyConfigSettings;
    private WebAPIServiceHelperFactory webAPIServiceHelperFactory;

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        app = (GlobalState) RuntimeEnvironment.application;
    }

    @After
    public void tearDown() throws NoSuchFieldException, IllegalAccessException {
        closeDatabase(app);
    }

    @Test
    public void getInstance_should_return_AzureIoTHubServiceHelper_if_all_settings_are_enabled() throws Exception {
        givenCloudServicesFeatureToggle(true);
        givenCompanyConfigSettings(true);
        givenWebAPIServiceHelperFactory();

        IWebAPIServiceHelper instance = webAPIServiceHelperFactory.getInstance(app, companyConfigSettings);

        assertTrue(instance instanceof AzureIoTHubServiceHelper);
    }

    @Test
    public void getInstance_should_return_RESTWebServiceHelper_if_cloud_service_feature_toggle_is_disabled() throws Exception {
        givenCloudServicesFeatureToggle(false);
        givenCompanyConfigSettings(true);
        givenWebAPIServiceHelperFactory();

        IWebAPIServiceHelper instance = webAPIServiceHelperFactory.getInstance(app, companyConfigSettings);

        assertTrue(instance instanceof RESTWebServiceHelper);
    }

    @Test
    public void getInstance_should_return_RESTWebServiceHelper_if_company_config_setting_is_disabled() throws Exception {
        givenCloudServicesFeatureToggle(true);
        givenCompanyConfigSettings(false);
        givenWebAPIServiceHelperFactory();

        IWebAPIServiceHelper instance = webAPIServiceHelperFactory.getInstance(app, companyConfigSettings);

        assertTrue(instance instanceof RESTWebServiceHelper);
    }

    private void givenCloudServicesFeatureToggle(boolean cloudServicesFeatureToggleEnabled) {
        featureToggleService = Mockito.mock(IFeatureToggleService.class);
        when(featureToggleService.getUseCloudServices()).thenReturn(cloudServicesFeatureToggleEnabled);
    }

    private void givenCompanyConfigSettings(boolean useKmbWebApiServices) {
        companyConfigSettings = new CompanyConfigSettings();
        companyConfigSettings.setUseKmbWebApiServices(useKmbWebApiServices);
    }

    private void givenWebAPIServiceHelperFactory() {
        webAPIServiceHelperFactory = new WebAPIServiceHelperFactory(featureToggleService);
    }

}
