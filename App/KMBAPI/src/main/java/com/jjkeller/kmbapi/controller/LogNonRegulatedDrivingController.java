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

/**
 * Created by rab5795 on 10/6/2016.
 */

public class LogNonRegulatedDrivingController extends LogSpecialDrivingController {

    public LogNonRegulatedDrivingController(Context ctx){super(ctx);}

    @Override
    public EmployeeLogProvisionTypeEnum getDrivingCategory() {
        return EmployeeLogProvisionTypeEnum.NONREGULATED;
    }

    @Override
    public boolean getIsFeatureToggleEnabled() {
        return GlobalState.getInstance().getFeatureService().getNonRegDrivingEnabled();
    }

    @Override
    public boolean getIsInSpecialDutyStatus() {
        return GlobalState.getInstance().getIsInNonRegDrivingDutyStatus();
    }

    @Override
    public void setIsInSpecialDutyStatus(boolean value) {
        GlobalState.getInstance().setIsInNonRegDrivingDutyStatus(value);
    }

    @Override
    public boolean getIsInSpecialDrivingSegment() {
        return GlobalState.getInstance().getIsInNonRegDrivingSegment();
    }

    @Override
    public void setIsInSpecialDrivingSegment(boolean value) {
        GlobalState.getInstance().setIsInNonRegDrivingSegment(value);
    }

    @Override
    public void StartSpecialDrivingStatus(Date startTime, Location location, EmployeeLog empLog) {
        super.StartSpecialDrivingStatus(startTime, location, empLog);
        setIsInSpecialDrivingSegment(true);
    }

    @Override
    public void EndSpecialDrivingStatus(Date endTime, Location location, EmployeeLog empLog){
        super.EndSpecialDrivingStatus(endTime, location, empLog);
        if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
            AddMandateEndingLogRemark(empLog);
        }
        setIsInSpecialDrivingSegment(false);
    }
}
