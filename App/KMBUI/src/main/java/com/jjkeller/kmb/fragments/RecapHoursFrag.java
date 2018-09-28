package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jjkeller.kmb.interfaces.IRecapHours;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.calcengine.LogSummary;
import com.jjkeller.kmbui.R;

import org.joda.time.Duration;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class RecapHoursFrag extends BaseFragment
{
    private final SimpleDateFormat recapDateFormat = new SimpleDateFormat("EEE MMM d", Locale.US);

    protected IRecapHours.RecapHoursFragControllerMethods controlListener;

    protected TextView _lblRecapDayOne;
    protected TextView _lblRecapDayTwo;
    protected TextView _lblRecapDayThree;
    protected TextView _lblRecapTimeDayOne;
    protected TextView _lblRecapTimeDayTwo;
    protected TextView _lblRecapTimeDayThree;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.f_recaphours, container, false);
        findControls(v);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        loadControls();
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);

        try {
            controlListener = (IRecapHours.RecapHoursFragControllerMethods) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement IRecapHours.RecapHoursFragControllerMethods");
        }
    }

    protected void findControls(View v)
    {
        _lblRecapDayOne = (TextView)v.findViewById(R.id.lblRecapDayOne);
        _lblRecapDayTwo = (TextView)v.findViewById(R.id.lblRecapDayTwo);
        _lblRecapDayThree = (TextView)v.findViewById(R.id.lblRecapDayThree);
        _lblRecapTimeDayOne = (TextView) v.findViewById(R.id.lblRecapTimeDayOne);
        _lblRecapTimeDayTwo = (TextView) v.findViewById(R.id.lblRecapTimeDayTwo);
        _lblRecapTimeDayThree = (TextView) v.findViewById(R.id.lblRecapTimeDayThree);
    }

    protected void loadControls()
    {
        List<LogSummary> logSummaries = controlListener.getEmployeeLogController().EmployeeLogsForDailyHoursRecapReport();
        List<LogSummary> recapSummaries = controlListener.getEmployeeLogController().FutureDaysForDailyHoursRecapReport(logSummaries);
        setRecapText(recapSummaries, 1, _lblRecapDayOne, _lblRecapTimeDayOne);
        setRecapText(recapSummaries, 2, _lblRecapDayTwo, _lblRecapTimeDayTwo);
        setRecapText(recapSummaries, 3, _lblRecapDayThree, _lblRecapTimeDayThree);
    }

    private void setRecapText(List<LogSummary> recapSummaries, int recapDay, TextView dayLabel, TextView timeLabel)
    {
        if (recapSummaries == null || recapSummaries.size() < recapDay) {
            return;
        }

        boolean isMandateEnabled = GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled();

        LogSummary summary = recapSummaries.get(recapDay - 1);

        dayLabel.setText(recapDateFormat.format(summary.getLogDate()));

        // "Total" on missing logs should appear as a dash "-"
        String dailyTotal = "-";
        if (summary.getLogExists() && summary.getDailyDurationTotal() != null) {
            Duration duration = new Duration(summary.getDailyDurationTotal());

            if (isMandateEnabled) {
                dailyTotal = String.format(Locale.US, "%dh %dm %ds", duration.getStandardHours(), duration.getStandardMinutes() % 60, duration.getStandardSeconds() % 60);
            } else {
                dailyTotal = String.format(Locale.US, "%dh %dm", duration.getStandardHours(), duration.getStandardMinutes() % 60);
            }
        }
        timeLabel.setText(dailyTotal);
    }

}
