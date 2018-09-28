package com.jjkeller.kmb.share;

import android.content.Intent;
import android.content.res.Resources;

import com.jjkeller.kmbapi.controller.EOBR.EobrReader;
import com.jjkeller.kmbapi.controller.EmployeeLogEldMandateController;
import com.jjkeller.kmbapi.controller.LogEntryController;
import com.jjkeller.kmbapi.enums.Malfunction;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.proxydata.GpsLocation;
import com.jjkeller.kmbui.R;


/**
 * Created by t000622 on 5/10/2017.
 */

public class ELDCommon {

    private Intent _intent;
    private Resources _resources;
    private LogEntryController _logEntryController;
    private EmployeeLogEldMandateController _emplLogEldMandateController;

    public ELDCommon(Intent intent, Resources resources, LogEntryController logEntryController, EmployeeLogEldMandateController emplLogEldMandateController) {
        _intent = intent;
        _resources = resources;
        _logEntryController = logEntryController;
        _emplLogEldMandateController = emplLogEldMandateController;
    }

    public boolean ShouldShowManualLocation() {
        if (_logEntryController.IsCurrentLogCreated()) {
            EmployeeLog emplLog = _logEntryController.getCurrentEmployeeLog();
            if (_emplLogEldMandateController.isMalfunctioning(emplLog, Malfunction.POSITIONING_COMPLIANCE)) {
                _intent.putExtra(_resources.getString(R.string.extra_lat_lon_status), "E");
                return false;
            }
        }

        if (EobrReader.getIsEobrDevicePhysicallyConnected() && _emplLogEldMandateController.isGPSValid()) {
            // now need to get the lat/lon from gps
            GpsLocation location = _logEntryController.getCurrentGPSLocation();

            if (location != null) {
                _intent.putExtra(_resources.getString(R.string.extra_latitude), (double) location.getLatitudeDegrees());
                _intent.putExtra(_resources.getString(R.string.extra_Longitude), (double) location.getLongitudeDegrees());
                _intent.putExtra(_resources.getString(R.string.extra_lat_lon_status), "");
                return false;
            }
        }

        _intent.putExtra(_resources.getString(R.string.extra_lat_lon_status), "M");
        return true;
    }
}
