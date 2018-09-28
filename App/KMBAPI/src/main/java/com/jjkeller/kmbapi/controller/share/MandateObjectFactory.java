package com.jjkeller.kmbapi.controller.share;

import android.content.Context;

import com.jjkeller.kmbapi.controller.EmployeeLogAobrdController;
import com.jjkeller.kmbapi.controller.EmployeeLogEldMandateController;
import com.jjkeller.kmbapi.controller.interfaces.IAPIController;
import com.jjkeller.kmbapi.controller.interfaces.IEldMandateFactory;
import com.jjkeller.kmbapi.featuretoggle.IFeatureToggleService;

/**
 * Factory class used to return controllers related to the mandate work going forward
 */
public class MandateObjectFactory implements IEldMandateFactory {
    private Context _context = null;
    private IFeatureToggleService _featureToggleServiceInstance = null;
    private static IEldMandateFactory _instance = null;

    public MandateObjectFactory(Context ctx, IFeatureToggleService svc) {
        if (this._context == null)
            this._context = ctx;
        if (this._featureToggleServiceInstance == null)
            this._featureToggleServiceInstance = svc;
    }


    public static IEldMandateFactory getInstance(Context ctx, IFeatureToggleService svc) {
        if (_instance == null)
            return (_instance = new MandateObjectFactory(ctx, svc));
        return _instance;
    }

    protected void setInstance(IEldMandateFactory factory) {
        _instance = factory;
    }

    @Override
    public IAPIController getCurrentEventController() {
        if (this._context == null || this._featureToggleServiceInstance == null)
            return null;
        boolean isMandateEnabled = this._featureToggleServiceInstance.getIsEldMandateEnabled();
        if (isMandateEnabled)
            return new EmployeeLogEldMandateController(this._context);
        else
            return new EmployeeLogAobrdController(this._context);
    }
}

