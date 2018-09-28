package com.jjkeller.kmbapi.controller.utility;

import android.content.Context;

public class RestWebServiceHelperFactory {
    private static IRESTWebServiceHelper _instance = null;

    public static IRESTWebServiceHelper getInstance(Context context) {
        if (_instance == null)
            _instance = new RESTWebServiceHelper(context);

        return _instance;
    }

    /**
     * Used for providing mocked implementations for unit testing
     */
    public static void setInstance(IRESTWebServiceHelper restWebServiceHelper) {
        _instance = restWebServiceHelper;
    }
}
