package com.jjkeller.kmbapi.realtime.malfunction;

import android.os.Bundle;
import android.util.Log;

import com.jjkeller.kmbapi.configuration.AppSettings;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.EOBR.EobrReader;
import com.jjkeller.kmbapi.controller.EmployeeLogEldMandateController;
import com.jjkeller.kmbapi.controller.FailureController;
import com.jjkeller.kmbapi.controller.utility.ErrorLogHelper;
import com.jjkeller.kmbapi.enums.Malfunction;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.realtime.ScheduledProcess;

import java.util.Date;
import java.util.concurrent.TimeUnit;


public class ClearTimingMalfunctionProcess extends TimingMalfunction implements ScheduledProcess {

        private FailureController failureController;
        private long timeDelay = 30;

        public ClearTimingMalfunctionProcess(final EobrReader aEobrReaderInstance,
                                             final FailureController aFailureController,
                                             final EmployeeLogEldMandateController anEmployeeLogEldMandateController) {
            super(aEobrReaderInstance, anEmployeeLogEldMandateController);
            failureController = aFailureController;

            if(GlobalState.getInstance().getApplicationContext() != null) {
                AppSettings settings = GlobalState.getInstance().getAppSettings(GlobalState.getInstance().getApplicationContext());
                if(settings != null){
                    timeDelay = settings.getTimingMalfunctionClearMinutes();
                }
            }
        }

        @Override
        public long getTimeDelay() {
            return timeDelay;
        }

        @Override
        public TimeUnit getTimeUnit() {
            return TimeUnit.MINUTES;
        }

        @Override
        public void run() {
            Log.i("TimeSync", "Checking to see if timing malfunction is resolved");
            EmployeeLog log = GlobalState.getInstance().getCurrentEmployeeLog();

            if(getEldMandateController().isMalfunctioning(log, Malfunction.TIMING_COMPLIANCE)) {
                Date dmoTime = failureController.GetDmoTime();
                Date gpsTime = getEobrReader().Technician_ReadGPSTime();
                Date tabRtc = getEobrReader().Technician_ReadClockUniversalTime(GlobalState.getInstance().getApplicationContext());
                Date sourceOfTruthTime = dmoTime == null ? (gpsTime == null ? null : gpsTime) : dmoTime;
                if (resolveTimingMalfunction(sourceOfTruthTime, tabRtc)) {
                    ErrorLogHelper.RecordMessage("Timing malfunction resolved!!!");
                    Log.i("TimeSync", "Timing malfunction resolved!!!");
                }
            }
    }

    private boolean resolveTimingMalfunction(Date sourceOfTruthTime, Date tabClock){
        boolean resolved = false;
        Bundle answer = isTimingInCompliance(sourceOfTruthTime, tabClock);
        boolean isCompliant = answer.getBoolean("isCompliant", false);
        if (isCompliant) {
            try {
                sourceOfTruthTime = sourceOfTruthTime == null ? tabClock : sourceOfTruthTime;
                getEldMandateController().clearMalfunctionForLoggedInUsers(sourceOfTruthTime, Malfunction.TIMING_COMPLIANCE);
                resolved = true;
            } catch (Throwable t) {
                Log.e("TimeSync", "error clearing malfunction at logout", t);
            }
        }
        return resolved;
    }



}
