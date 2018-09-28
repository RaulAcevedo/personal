package com.jjkeller.kmbapi.controller.utility;

import android.content.Context;

import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.featuretoggle.IFeatureToggleService;
import com.jjkeller.kmbapi.proxydata.CompanyConfigSettings;

public class WebAPIServiceHelperFactory {

    private final IFeatureToggleService featureToggleService;

    public WebAPIServiceHelperFactory() {
        this(GlobalState.getInstance().getFeatureService());
    }

    WebAPIServiceHelperFactory(IFeatureToggleService featureToggleService) {
        this.featureToggleService = featureToggleService;
    }

    public IWebAPIServiceHelper getInstance(Context context) {
        return getInstance(context, GlobalState.getInstance().getCompanyConfigSettings(context));
    }

    public IWebAPIServiceHelper getInstance(Context context, CompanyConfigSettings companyConfigSettings) {
        if (featureToggleService.getUseCloudServices() && companyConfigSettings.getUseKmbWebApiServices())
            return new AzureIoTHubServiceHelper(new RESTWebServiceHelper(context), context);
        return new RESTWebServiceHelper(context);
    }

}
