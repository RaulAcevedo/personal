package com.jjkeller.kmbapi.controller;

import android.content.Context;

import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.abstracts.LogSpecialDrivingController;
import com.jjkeller.kmbapi.controller.utility.EmployeeLogUtilities;
import com.jjkeller.kmbapi.employeelogeldevents.Enums;
import com.jjkeller.kmbapi.enums.EmployeeLogProvisionTypeEnum;
import com.jjkeller.kmbapi.kmbeobr.EventRecord;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;
import com.jjkeller.kmbapi.proxydata.Location;

import java.util.Date;


public class LogHyrailController extends LogSpecialDrivingController {

    public LogHyrailController(Context ctx){
        super(ctx);
    }

    @Override
    public EmployeeLogProvisionTypeEnum getDrivingCategory() {
        return EmployeeLogProvisionTypeEnum.HYRAIL;
    }

    @Override
    public boolean getIsFeatureToggleEnabled() {
        return GlobalState.getInstance().getFeatureService().getHyrailEnabled();
    }

    @Override
    public boolean getIsInSpecialDutyStatus() {
        return GlobalState.getInstance().getIsInHyrailDutyStatus();
    }

    @Override
    public void setIsInSpecialDutyStatus(boolean value) {
        GlobalState.getInstance().setIsInHyrailDutyStatus(value);
    }

    @Override
    public boolean getIsInSpecialDrivingSegment() {
        return GlobalState.getInstance().getIsInHyrailDrivingSegment();
    }

    @Override
    public void setIsInSpecialDrivingSegment(boolean value) {
        GlobalState.getInstance().setIsInHyrailDrivingSegment(value);
    }

    @Override
    public void StartSpecialDrivingStatus(Date startTime, Location location, EmployeeLog empLog) {
        super.StartSpecialDrivingStatus(startTime, location, empLog);
        setIsInSpecialDrivingSegment(true);

    }

    @Override
    public void EndSpecialDrivingStatus(Date endTime, Location location, EmployeeLog empLog) {
        super.EndSpecialDrivingStatus(endTime, location, empLog);
        if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
            AddMandateEndingLogRemark(empLog);
        }
        // Note: Turn off Hyrail mode in the app.
        //       This is done so that if the user forgets to acknowledge the prompt confirming Hyrail mode, that the default action is to end Hyrail
        setIsInSpecialDrivingSegment(false);
    }
}
