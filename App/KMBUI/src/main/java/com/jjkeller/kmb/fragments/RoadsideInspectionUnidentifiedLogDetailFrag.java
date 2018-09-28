package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.jjkeller.kmb.interfaces.IRoadsideInspectionMandate;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.employeelogeldevents.Enums;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;
import com.jjkeller.kmbui.R;

import java.util.Calendar;

/**
 * Created by jhm2586 on 1/4/2017.
 */

public class RoadsideInspectionUnidentifiedLogDetailFrag extends BaseFragment {

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
        View v = inflater.inflate(R.layout.f_roadsideinspectionunidentifiedlogdetail, container, false);
        findControls(v);
        return v;
    }

    protected void findControls(View v) {

        _table = (TableLayout)v.findViewById(R.id.table_rsiLogDetail);
    }

    public void loadControls(EmployeeLogEldEvent[] events) {
        int dayTicker = 0;
        int monthTicker = 0;
        int yearTicker = 0;
        // Clear any previous rows from table
        if(_table != null) {
            cleanTable(_table);
            if (events != null) {
                // Add event detail rows
                for (int i = 0; i < events.length; i++) {
                    TableRow row;
                    row = (TableRow) LayoutInflater.from(this.getActivity()).inflate(R.layout.tablerow_unidentifiedlogdetail, null);
                    if (row != null) {
                        EmployeeLogEldEvent evt = events[i];
                        Calendar eventDate = Calendar.getInstance();
                        eventDate.setTime(evt.getEventDateTime());
                        int eventMonth = eventDate.get(Calendar.MONTH);
                        int eventYear = eventDate.get(Calendar.YEAR);
                        int eventDayOfMonth = eventDate.get(Calendar.DAY_OF_MONTH);
                        if (dayTicker == 0 || dayTicker > eventDayOfMonth || monthTicker > eventMonth || yearTicker > eventYear) {
                            dayTicker = eventDayOfMonth;
                            monthTicker = eventMonth;
                            yearTicker = eventYear;
                            TableRow dateRow = (TableRow) LayoutInflater.from(this.getActivity()).inflate(R.layout.tablerow_logdetail_date, null);
                            if(dateRow != null) {
                                ((TextView) dateRow.findViewById(R.id.tvDate)).setText(DateUtility.getHomeTerminalDateFormat().format(evt.getEventDateTime()));
                                _table.addView(dateRow);
                            }
                        }
                        ((TextView) row.findViewById(R.id.tvTime)).setText(DateUtility.getHomeTerminalTime24HourFormat().format(evt.getEventDateTime()));
                        ((TextView) row.findViewById(R.id.tvEventType)).setText(this.displayEventType(evt));
                        ((TextView) row.findViewById(R.id.tvEventCode)).setText(String.valueOf(evt.getEventCode()));
                        ((TextView) row.findViewById(R.id.tvSequenceId)).setText(this.displaySequenceId(evt));
                        ((TextView) row.findViewById(R.id.tvRecordStatus)).setText(this.displayEventRecordStatus(evt));
                        ((TextView) row.findViewById(R.id.tvOrigin)).setText(this.displayOriginText(evt));
                        ((TextView) row.findViewById(R.id.tvLocation)).setText((displayLocation(evt)));
                        ((TextView) row.findViewById(R.id.tvOdometerCalibration)).setText(this.displayAccumulatedVehicleMiles(evt));
                        ((TextView) row.findViewById(R.id.tvEngineHours)).setText(this.isNumberNull(evt.getEngineHours()) ? sZeroDecVal : String.format("%.1f", evt.getEngineHours()));
                        _table.addView(row);
                    }
                }
                _table.requestLayout();     // Not sure if this is needed.
            }
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

    private String displayEventRecordStatus(EmployeeLogEldEvent evt)
    {
        switch (evt.getEventType().setFromInt(evt.getEventType().getValue()))
        {
            case DutyStatusChange:
            case IntermediateLog:
            case ChangeInDriversIndication:
                return String.valueOf(evt.getEventRecordStatus());
            default:
                return "";
        }
    }

    private String displayEventType(EmployeeLogEldEvent evt)
    {
        switch (evt.getEventType().setFromInt(evt.getEventType().getValue()))
        {
            case Certification: {
                return DateUtility.getHomeTerminalDateFormat().format(evt.getEventDateTime());
            }
            default:
                return String.valueOf(evt.getEventType().getValue());
        }
    }

    private String displayLocation(EmployeeLogEldEvent evt) {

        if (evt.getEventType() == Enums.EmployeeLogEldEventType.DutyStatusChange || evt.getEventType() == Enums.EmployeeLogEldEventType.EnginePowerUpPowerDown) {
            return evt.getGeolocation();
        }
        else {
            return EMPTY_STRING;
        }
    }

    private String displayAccumulatedVehicleMiles(EmployeeLogEldEvent evt) {
        return this.isNumberNull(evt.getAccumulatedVehicleMiles()) ? sZeroIntVal : String.valueOf(evt.getAccumulatedVehicleMiles());
    }

    private String displayOriginText(EmployeeLogEldEvent evt)
    {
        switch (evt.getEventType().setFromInt(evt.getEventType().getValue()))
        {
            case Certification:
                return sDriverText;
            default:
                return String.valueOf(evt.getEventRecordOrigin());
        }
    }

    private boolean isNumberNull(Number n)
    {
        return n == null;
    }
}
