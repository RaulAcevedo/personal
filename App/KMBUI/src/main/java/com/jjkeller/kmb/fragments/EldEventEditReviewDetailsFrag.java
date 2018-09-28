package com.jjkeller.kmb.fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
//import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.interfaces.IAPIController;
import com.jjkeller.kmbapi.controller.share.MandateObjectFactory;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.employeelogeldevents.Enums;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;
import com.jjkeller.kmbui.R;

import java.util.Date;

public class EldEventEditReviewDetailsFrag extends BaseFragment{

    private EmployeeLogEldEvent[] _eldEvents;
    private int _currentIndex;
    TextView tvStartTime;
    TextView tvStatus;
    TextView tvEndTime;
    TextView tvMiles;
    TextView tvHours;
    TextView tvLatitiude;
    TextView tvLongitude;
    TextView tvRuleset;
    TextView tvTrailerNumber;
    TextView tvUnitNumber;
    TextView tvLocation;
    TextView tvShipmentInfo;
    TextView lblDrivingDistance;
    TextView tvDrivingDistance;
    View hrDrivingDistance;
    TextView tvReasonForEdit;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.f_eldeventeditreviewdetails, container, false);
        findControls(v);
        return v;
    }

    private void findControls(View v) {
        tvStartTime = (TextView) v.findViewById(R.id.tvStartTime);
        tvStatus = (TextView) v.findViewById(R.id.tvStatus);
        tvEndTime = (TextView) v.findViewById(R.id.tvEndTime);
        tvMiles = (TextView) v.findViewById(R.id.tvMiles);
        tvHours = (TextView) v.findViewById(R.id.tvHours);
        tvLatitiude = (TextView) v.findViewById(R.id.tvLatitude);
        tvLongitude = (TextView) v.findViewById(R.id.tvLongitude);
        tvRuleset = (TextView) v.findViewById(R.id.tvRuleset);
        tvTrailerNumber = (TextView) v.findViewById(R.id.tvTrailerNumber);
        tvUnitNumber = (TextView) v.findViewById(R.id.tvUnitNumber);
        tvLocation = (TextView) v.findViewById(R.id.tvLocation);
        tvShipmentInfo = (TextView) v.findViewById(R.id.tvShipmentInfo);
        lblDrivingDistance = (TextView) v.findViewById(R.id.lblDrivingDistance);
        tvDrivingDistance = (TextView) v.findViewById(R.id.tvDrivingDistance);
        hrDrivingDistance = v.findViewById(R.id.hrDrivingDistance);
        tvReasonForEdit = (TextView) v.findViewById(R.id.tvReasonForEdit);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.loadControls(savedInstanceState);
    }

    private void loadControls(Bundle savedInstanceState) {
        loadData();
    }

    void loadData(){
        EmployeeLogEldEvent eldEvent = _eldEvents[_currentIndex];

        if (eldEvent!=null){
            Context ctx = GlobalState.getInstance().getBaseContext();
            String starttime = eldEvent.getEventDateTime() == null ? "" : DateUtility.getHomeTerminalTime12HourFormatWithSeconds().format(eldEvent.getEventDateTime());

            String endtime = "11:59 PM";   // if on the last Event, assume end-of-day
            if (_eldEvents.length - 1 != _currentIndex)
                endtime = DateUtility.getHomeTerminalTime12HourFormatWithSeconds().format(_eldEvents[_currentIndex + 1].getEventDateTime());

            String miles = eldEvent.getAccumulatedVehicleMiles() == null ? "" : eldEvent.getAccumulatedVehicleMiles().toString();
            String hours = eldEvent.getEngineHours() == null ? "" : eldEvent.getEngineHours().toString();
            String latitude = eldEvent.getLatitude() == null ? "" : eldEvent.getLatitude().toString();
            String longitude = eldEvent.getLongitude() == null ? "" : eldEvent.getLongitude().toString();
            tvStartTime.setText(starttime);
            tvStatus.setText(eldEvent.getCompositeEventCodeType( eldEvent.getEventType(),eldEvent.getEventCode()));
            tvEndTime.setText(endtime);
            tvMiles.setText(miles);
            tvHours.setText(hours);
            tvLatitiude.setText(latitude);
            tvLongitude.setText(longitude);
            tvLocation.setText(eldEvent.getDriversLocationDescription());
            tvRuleset.setText(eldEvent.getRuleSet().getString(ctx));
            tvUnitNumber.setText(eldEvent.getTractorNumber());
            tvTrailerNumber.setText(eldEvent.getTrailerNumber());
            tvShipmentInfo.setText(eldEvent.getShipmentInfo());

            // display Distance for Automatic and Manual Driving events
            lblDrivingDistance.setVisibility(eldEvent.isDrivingEvent() ? View.VISIBLE : View.GONE );
            tvDrivingDistance.setVisibility(eldEvent.isDrivingEvent() ? View.VISIBLE : View.GONE );
            hrDrivingDistance.setVisibility(eldEvent.isDrivingEvent() ? View.VISIBLE : View.GONE );

            if (eldEvent.getDistance() != null) {
                tvDrivingDistance.setText(String.valueOf(eldEvent.getDistance()));
            }

            tvReasonForEdit.setText(eldEvent.getEventComment());
        }

        int intColor = Color.BLACK;
        if (eldEvent.getEventRecordStatus() == Enums.EmployeeLogEldEventRecordStatus.InactiveChangeRequested.getValue()) {
            intColor = Color.RED;
        }

        setColor(tvStartTime, intColor);
        setColor(tvStatus, intColor);
        setColor(tvEndTime, intColor);
        setColor(tvMiles, intColor);
        setColor(tvHours, intColor);
        setColor(tvLatitiude, intColor);
        setColor(tvLongitude, intColor);
        setColor(tvLocation, intColor);
        setColor(tvRuleset, intColor);
        setColor(tvUnitNumber, intColor);
        setColor(tvTrailerNumber, intColor);
        setColor(tvShipmentInfo, intColor);
        setColor(tvDrivingDistance, intColor);
        setColor(tvReasonForEdit, intColor);
    }

    private void setColor(TextView tv, int intColor) {
        tv.setTextColor(intColor);
    }

    public void setEvent(EmployeeLogEldEvent[] eldEvents, int currentIndex) {
        _eldEvents = eldEvents;
        _currentIndex = currentIndex;
        loadData();
    }
}
