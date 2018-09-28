package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jjkeller.kmb.RoadsideInspectionMandate;
import com.jjkeller.kmb.interfaces.IRoadsideInspectionMandate;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbapi.common.TimeKeeper;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.FmcsaEldInfoController;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.controller.utility.EmployeeLogUtilities;
import com.jjkeller.kmbui.R;

import org.joda.time.LocalTime;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Date;

public class RoadsideInspectionMandateFrag extends BaseFragment {

    private IRoadsideInspectionMandate.RoadsideInspectionMandateFragControllerMethods controllerListener;

    // Driver section
    private TextView driverNameLabel;
    private TextView driverIdLabel;
    private TextView licenseStateLabel;
    private TextView licenseNumberLabel;
    private TextView coDriverLabel;
    private TextView coDriverIdLabel;
    private TextView currentLocationLabel;
    private TextView unidentifiedDrivingRecordsLabel;
    private TextView exemptDriverStatusLabel;
    private TextView timeZoneLabel;
    private TextView shippingIdLabel;
    private TextView trailerNumberLabel;
    // Vehicle section
    private TextView currentOdometerLabel;
    private TextView engineHoursLabel;
    private TextView truckTractorIdLabel;
    private TextView truckTractorVinLabel;
    // ELD section
    private TextView eldIdLabel;
    private TextView eldProviderLabel;
    private TextView eldMalfunctionStatusLabel;
    private TextView dataDiagnosticStatusLabel;
    // Other section
    private TextView dateOfRecordLabel;
    private TextView logStartTimeLabel;
    private TextView carrierLabel;
    private TextView dateOfDisplayLabel;
    // Grid section
    private TextView milesTodayLabel;
    private TextView totalHoursInWorkingDaySoFarLabel;

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
        View v = inflater.inflate(R.layout.f_roadsideinspectionmandate, container, false);
        findControls(v);
        return v;
    }

    protected void findControls(View v) {
        // Driver section
        driverNameLabel = (TextView) v.findViewById(R.id.driver_name);
        driverIdLabel = (TextView) v.findViewById(R.id.driver_id);
        licenseStateLabel = (TextView) v.findViewById(R.id.license_state);
        licenseNumberLabel = (TextView) v.findViewById(R.id.license_number);
        coDriverLabel = (TextView) v.findViewById(R.id.co_driver);
        coDriverIdLabel = (TextView) v.findViewById(R.id.co_driver_id);
        currentLocationLabel = (TextView) v.findViewById(R.id.current_location);
        unidentifiedDrivingRecordsLabel = (TextView) v.findViewById(R.id.unidentified_driving_records);
        exemptDriverStatusLabel = (TextView) v.findViewById(R.id.exempt_driver_status);
        timeZoneLabel = (TextView) v.findViewById(R.id.time_zone);
        shippingIdLabel = (TextView) v.findViewById(R.id.shipping_id);
        trailerNumberLabel = (TextView) v.findViewById(R.id.trailer_number);
        // Vehicle section
        currentOdometerLabel = (TextView) v.findViewById(R.id.current_odometer);
        engineHoursLabel = (TextView) v.findViewById(R.id.engine_hours);
        truckTractorIdLabel = (TextView) v.findViewById(R.id.truck_tractor_id);
        truckTractorVinLabel = (TextView) v.findViewById(R.id.truck_tractor_vin);
        // ELD section
        eldIdLabel = (TextView) v.findViewById(R.id.eld_id);
        eldProviderLabel = (TextView) v.findViewById(R.id.eld_provider);
        eldMalfunctionStatusLabel = (TextView) v.findViewById(R.id.eld_malfunction_status);
        dataDiagnosticStatusLabel = (TextView) v.findViewById(R.id.data_diagnostic_status);
        // Other section
        dateOfRecordLabel = (TextView) v.findViewById(R.id.date_of_record);
        logStartTimeLabel = (TextView) v.findViewById(R.id.log_start_time);
        carrierLabel = (TextView) v.findViewById(R.id.carrier);
        dateOfDisplayLabel = (TextView) v.findViewById(R.id.date_of_display);
        // Grid section
        milesTodayLabel = (TextView) v.findViewById(R.id.miles_today);
        totalHoursInWorkingDaySoFarLabel = (TextView) v.findViewById(R.id.total_hours_in_working_day_so_far);
    }

    public void loadControls() {
        RoadsideInspectionMandate.RoadsideInspectionMandateData data = controllerListener.getRoadsideInspectionMandateData();
        if (data != null) {
            loadDriverSection(data);
            loadVehicleSection(data);
            loadEldSection(data);
            loadOtherSection(data);
            loadGridSection(data);
        }
    }

    private void loadDriverSection(RoadsideInspectionMandate.RoadsideInspectionMandateData data) {
        driverNameLabel.setText(getString(R.string.driver_name_label, data.user.getCredentials().getEmployeeFullName()));
        driverIdLabel.setText(getString(R.string.driver_id_label, data.user.getCredentials().getUsername()));
        licenseStateLabel.setText(getString(R.string.license_state_label, data.user.getCredentials().getDriverLicenseState()));
        licenseNumberLabel.setText(getString(R.string.license_number_label, data.user.getCredentials().getDriverLicenseNumber()));
        if (data.coDriver != null) {
            coDriverLabel.setText(getString(R.string.co_driver_label, data.coDriver.getCredentials().getEmployeeFullName()));
            coDriverIdLabel.setText(getString(R.string.co_driver_id_label, data.coDriver.getCredentials().getUsername()));
        } else {
            coDriverLabel.setText(getString(R.string.co_driver_label, ""));
            coDriverIdLabel.setText(getString(R.string.co_driver_id_label, ""));
        }
        if (data.currentLocation != null && !data.currentLocation.IsEmpty()) {
            currentLocationLabel.setText(getString(R.string.current_location_label, data.currentLocation.ToLocationString()));
        } else {
            currentLocationLabel.setText(getString(R.string.current_location_label, ""));
        }
        unidentifiedDrivingRecordsLabel.setText(getString(R.string.unidentified_driving_records_label, data.isUnidentifiedDrivingRecordsIndicatorActive ? "Yes" : "None"));
        exemptDriverStatusLabel.setText(getString(R.string.exempt_driver_status_label, data.currentLog.getIsExemptFromELDUse() ? "E" : "0"));
        timeZoneLabel.setText(getString(R.string.time_zone_label, data.user.getHomeTerminalTimeZone().getString(getActivity())));
        if (data.shippingId != null) {
            shippingIdLabel.setText(getString(R.string.shipping_id_label, data.shippingId));
        } else {
            shippingIdLabel.setText(getString(R.string.shipping_id_label, ""));
        }
        if (data.trailerNumber != null) {
            trailerNumberLabel.setText(getString(R.string.trailer_number_label, data.trailerNumber));
        } else {
            trailerNumberLabel.setText(getString(R.string.trailer_number_label, ""));
        }
    }

    private void loadVehicleSection(RoadsideInspectionMandate.RoadsideInspectionMandateData data) {
        if (data.accumulatedDistanceAndHours != null) {
            NumberFormat engineHoursFormat = NumberFormat.getNumberInstance();
            engineHoursFormat.setMaximumFractionDigits(1);
            engineHoursFormat.setRoundingMode(RoundingMode.DOWN);
            engineHoursLabel.setText(getString(R.string.engine_hours_label, engineHoursFormat.format(data.accumulatedDistanceAndHours.getEngineHours())));
        } else {
            currentOdometerLabel.setText(getString(R.string.current_odometer_label, ""));
            engineHoursLabel.setText(getString(R.string.engine_hours_label, ""));
        }
        if (data.truckTractorId == null) {
            truckTractorIdLabel.setText(getString(R.string.truck_tractor_id_label, ""));
        } else {
            truckTractorIdLabel.setText(getString(R.string.truck_tractor_id_label, data.truckTractorId));
        }
        if (data.currentOdometer >= 0) {
            NumberFormat odometerFormat = NumberFormat.getIntegerInstance();
            odometerFormat.setRoundingMode(RoundingMode.DOWN);
            currentOdometerLabel.setText(getString(R.string.current_odometer_label, odometerFormat.format(data.currentOdometer)));
        } else {
            currentOdometerLabel.setText(getString(R.string.current_odometer_label, ""));
        }

        truckTractorVinLabel.setText(getString(R.string.truck_tractor_vin_label, data.truckVin)); // TODO populate the VIN when it is available
    }

    private void loadEldSection(RoadsideInspectionMandate.RoadsideInspectionMandateData data) {
        String logDate = DateUtility.getDateFormat().format(data.currentLog.getLogDate());
        String currentDate = DateUtility.getDateFormat().format(DateUtility.getCurrentDateTime().toDate());
        if(logDate.equalsIgnoreCase(currentDate)){
            FmcsaEldInfoController fmcsaEldInfoController = new FmcsaEldInfoController(GlobalState.getInstance());
            eldIdLabel.setText(getString(R.string.eld_id_label, fmcsaEldInfoController.getCurrentEldIdentifier(fmcsaEldInfoController.getEobrDeviceSerialNumber())));
        }else{
            eldIdLabel.setText(getString(R.string.eld_id_label, "-"));
        }
        eldProviderLabel.setText(getString(R.string.eld_provider_label, getString(R.string.jjkeller)));
        eldMalfunctionStatusLabel.setText(getString(R.string.eld_malfunction_status_label, data.malfunctionIndicatorStatus ? "1" : "0"));
        dataDiagnosticStatusLabel.setText(getString(R.string.data_diagnostic_status_label, data.dataDiagnosticIndicatorStatus ? "1" : "0"));
    }

    private void loadOtherSection(RoadsideInspectionMandate.RoadsideInspectionMandateData data) {
        String logDate = DateUtility.getDateFormat().format(data.currentLog.getLogDate());
        dateOfRecordLabel.setText(getString(R.string.date_of_record_label, logDate));
        String currentDate = DateUtility.getDateFormat().format(DateUtility.getCurrentDateTime().toDate());
        dateOfDisplayLabel.setText(getString(R.string.date_of_display_label, currentDate));

        String logStartTimeValue;
        if (data.logStartTime.isEqual(LocalTime.MIDNIGHT)) {
            logStartTimeValue = getString(R.string.midnight);
        } else if (data.logStartTime.isEqual(new LocalTime(12, 0))) {
            logStartTimeValue = getString(R.string.noon);
        } else {
            logStartTimeValue = data.logStartTime.toDateTimeToday().toString(DateUtility.getHomeTerminalTime24HourFormat().toPattern());
        }
        logStartTimeLabel.setText(getString(R.string.log_start_time_label, logStartTimeValue));

        String carrier;
        if (data.user.getCredentials().getHomeTerminalDOTNumber() == null) {
            carrier = String.format("[%s]", data.companyConfigSettings.getDmoCompanyName());
        } else {
            carrier = String.format("%s [%s]", data.user.getCredentials().getHomeTerminalDOTNumber(), data.companyConfigSettings.getDmoCompanyName());
        }
        carrierLabel.setText(getString(R.string.carrier_label, carrier));
    }

    private void loadGridSection(RoadsideInspectionMandate.RoadsideInspectionMandateData data) {
        if (data.accumulatedDistanceAndHours != null) {
            milesTodayLabel.setText(getString(R.string.miles_today_label, String.valueOf(data.accumulatedDistanceAndHours.getTotalVehicleMiles())));
        } else {
            milesTodayLabel.setText(getString(R.string.miles_today_label, ""));
        }

        // for today - calculate work time since start of day
        // for previous days - assume logs totals to 24 hours :)
        String totalHoursInWorkingDaySoFar = getActivity().getString(R.string.twenty_four_hours);
        RoadsideInspectionMandate activity = (RoadsideInspectionMandate) getActivity();
        if (activity != null) {
            if (activity._isSelectedDateToday) {
                String dailyLogStartTime = GlobalState.getInstance().getCompanyConfigSettings(getActivity().getApplicationContext()).getDailyLogStartTime();
                Date startDate = EmployeeLogUtilities.CalculateLogStartTime(dailyLogStartTime, data.currentLog.getLogDate(), GlobalState.getInstance().getCurrentUser().getHomeTerminalTimeZone());

                long diffInMs = TimeKeeper.getInstance().getCurrentDateTime().toDate().getTime() - startDate.getTime();
                totalHoursInWorkingDaySoFar = DateUtility.createTimeDurationString(diffInMs, true);
            }
        }

        totalHoursInWorkingDaySoFarLabel.setText(getString(R.string.total_hours_in_working_day_so_far_label, totalHoursInWorkingDaySoFar));
    }
}
