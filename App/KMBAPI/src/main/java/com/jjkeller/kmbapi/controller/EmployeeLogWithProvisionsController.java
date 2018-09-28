package com.jjkeller.kmbapi.controller;

import android.content.Context;

import com.jjkeller.kmbapi.controller.dataaccess.EmployeeLogWithProvisionsFacade;
import com.jjkeller.kmbapi.controller.share.ControllerBase;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.proxydata.EmployeeLogWithProvisions;

/**
 * Created by T000694 on 3/16/2017.
 */

public class EmployeeLogWithProvisionsController extends ControllerBase{

    public EmployeeLogWithProvisionsController(Context ctx) {
        super(ctx);
    }

    public EmployeeLogWithProvisions GetLogWithProvisionsForLogEldEventKey(EmployeeLog empLog, long logEldEventKey){
        EmployeeLogWithProvisionsFacade facade = new EmployeeLogWithProvisionsFacade(this.getContext());

        return facade.FetchForLogEldEventKey(empLog, logEldEventKey);
    }
}
