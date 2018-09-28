package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.jjkeller.kmb.RoadsideInspectionMandate;
import com.jjkeller.kmb.interfaces.IRoadsideInspectionMandate;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.employeelogeldevents.Enums;
import com.jjkeller.kmbapi.enums.DutyStatusEnum;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;
import com.jjkeller.kmbui.R;

/**
 * Created by jhm2586 on 1/4/2017.
 */

public class RoadsideInspectionMandateLogDetailFrag extends BaseFragment {

    private IRoadsideInspectionMandate.RoadsideInspectionMandateFragControllerMethods controllerListener;
    private TableLayout _table;
    private static final String sZeroIntVal = "0";
    private static final String sZeroDecVal = "0.0";
    private static final String sDriverText = "Driver";
    private static final String EMPTY_STRING = "";

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            controllerListener = (IRoadsideInspectionMandate.RoadsideInspectionMandateFragControllerMethods) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement IRoadsideInspectionMandate.RoadsideInspectionMandateFragControllerMethods");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.f_roadsideinspectionmandatelogdetail, container, false);
        findControls(v);
        return v;
    }

    protected void findControls(View v) {

        _table = (TableLayout) v.findViewById(R.id.table_rsiLogDetail);
    }

    public void loadControls() {
        RoadsideInspectionMandate.RoadsideInspectionMandateData data = controllerListener.getRoadsideInspectionMandateData();

        // Clear any previous rows from table
        if (_table != null)
            cleanTable(_table);

        if (data != null) {
            // Add date row
            TableRow dateRow = (TableRow) LayoutInflater.from(this.getActivity()).inflate(R.layout.tablerow_logdetail_date, null);
            if (dateRow != null) {
                ((TextView) dateRow.findViewById(R.id.tvDate)).setText(DateUtility.getHomeTerminalDateFormat().format(data.currentLog.getLogDate()));
                _table.addView(dateRow);
            }

            // Add event detail rows
            EmployeeLogEldEvent[] events = data.currentLog.getEldEventList().getEldEventList();
            for (int i = 0; i < events.length; i++) {
                TableRow row;
                // Inflate row "template" and fill out the fields.
                if (i % 2 == 0)
                    row = (TableRow) LayoutInflater.from(this.getActivity()).inflate(R.layout.tablerow_logdetail, null);
                else
                    row = (TableRow) LayoutInflater.from(this.getActivity()).inflate(R.layout.tablerow_logdetail_alt, null);

                if (row != null) {
                    EmployeeLogEldEvent evt = events[i];

                    int eventCode = evt.getEventCode();
                    // If the getEventType() is a status change then eventCode is the actual status.
                    // If that status is Off-Duty Well Site, change it to Off-Duty.
                    if (evt.getEventType() == Enums.EmployeeLogEldEventType.DutyStatusChange &&
                            eventCode == DutyStatusEnum.OFFDUTYWELLSITE)
                        eventCode = DutyStatusEnum.OFFDUTY;

                    ((TextView) row.findViewById(R.id.tvEventCode)).setText(String.valueOf(eventCode));
                    ((TextView) row.findViewById(R.id.tvTime)).setText(DateUtility.getHomeTerminalTime24HourFormat().format(evt.getEventDateTime()));
                    ((TextView) row.findViewById(R.id.tvEventType)).setText(this.displayEventType(evt));
                    ((TextView) row.findViewById(R.id.tvSequenceId)).setText(this.displaySequenceId(evt));
                    ((TextView) row.findViewById(R.id.tvRecordStatus)).setText(this.displayEventRecordStatus(evt));
                    ((TextView) row.findViewById(R.id.tvOrigin)).setText(this.displayOriginText(evt));
                    ((TextView) row.findViewById(R.id.tvLocation)).setText(displayLocation(evt));
                    ((TextView) row.findViewById(R.id.tvOdometerCalibration)).setText(this.displayOdometer(evt));
                    ((TextView) row.findViewById(R.id.tvEngineHours)).setText(this.isNumberNull(evt.getEngineHours()) ? sZeroDecVal : String.format("%.1f", evt.getEngineHours()));
                    ((TextView) row.findViewById(R.id.tvComment)).setText(evt.getEventComment());

                    _table.addView(row);
                }
            }
            _table.requestLayout();     // Not sure if this is needed.
        }
    }

    private void cleanTable(TableLayout table) {

        int childCount = table.getChildCount();

        // Remove all rows except the first one
        if (childCount > 1) {
            table.removeViews(1, childCount - 1);
        }
    }

    private String displaySequenceId(EmployeeLogEldEvent evt) {
        // display sequence ID for ALL events
        return String.valueOf(evt.getEventSequenceIDNumber());
    }

    private String displayEventRecordStatus(EmployeeLogEldEvent evt) {
        switch (evt.getEventType().setFromInt(evt.getEventType().getValue())) {
            case DutyStatusChange:
            case IntermediateLog:
            case ChangeInDriversIndication:
                return String.valueOf(evt.getEventRecordStatus());
            default:
                return "";
        }
    }

    private String displayEventType(EmployeeLogEldEvent evt) {
        switch (evt.getEventType().setFromInt(evt.getEventType().getValue())) {
            case Certification: {
                return DateUtility.getHomeTerminalDateFormat().format(evt.getEventDateTime());
            }
            default:
                return String.valueOf(evt.getEventType().getValue());
        }
    }

//    private String displayEventCode(EmployeeLogEldEvent evt)
//    {
//        switch (evt.getEventType().setFromInt(evt.getEventType().getValue()))
//        {
//            case DutyStatusChange:
//            case IntermediateLog:
//            case ChangeInDriversIndication:
//                return String.valueOf(evt.getEventCode());
//            default:
//                return "";
//        }
//    }

    private String displayLocation(EmployeeLogEldEvent evt) {

        // Per section 4.5.1, only Event Types 1-3 and 6 are required to record latitude and longitude (which would be needed to generate a geo-location value).
        // Only Event Types 1 and 3 are required to record Driver's Location Description.
        if (evt.getEventType() == Enums.EmployeeLogEldEventType.DutyStatusChange || evt.getEventType() == Enums.EmployeeLogEldEventType.ChangeInDriversIndication) {

            if (TextUtils.isEmpty(evt.getLatitudeStatusCode())) {
                return evt.getGeolocation();
            } else {
                return evt.getDriversLocationDescription();
            }
        } else if (evt.getEventType() == Enums.EmployeeLogEldEventType.EnginePowerUpPowerDown) {
            return evt.getGeolocation();
        } else {
            return EMPTY_STRING;
        }
    }

    private String displayOdometer(EmployeeLogEldEvent evt) {
        switch (evt.getEventType().setFromInt(evt.getEventType().getValue())) {
            case DutyStatusChange:
            case IntermediateLog:
            case ChangeInDriversIndication:
                return this.isNumberNull(evt.getAccumulatedVehicleMiles()) ? sZeroIntVal : String.valueOf(evt.getAccumulatedVehicleMiles());
            default:
                return this.isNumberNull(evt.getOdometer()) ? sZeroIntVal : String.valueOf(evt.getOdometer());
        }
    }

    private String displayOriginText(EmployeeLogEldEvent evt) {
        switch (evt.getEventType().setFromInt(evt.getEventType().getValue())) {
            case Certification:
                return sDriverText;
            default:
                return String.valueOf(evt.getEventRecordOrigin());
        }
    }

    private boolean isNumberNull(Number n) {
        return n == null;
    }
}
