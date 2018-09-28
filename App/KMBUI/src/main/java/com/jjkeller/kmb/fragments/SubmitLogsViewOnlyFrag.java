package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.TextView;

import com.jjkeller.kmb.interfaces.ISubmitLogsViewOnly.SubmitLogsViewOnlyFragActions;
import com.jjkeller.kmb.interfaces.ISubmitLogsViewOnly.SubmitLogsViewOnlyFragControllerMethods;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbapi.controller.abstracts.APIControllerBase;
import com.jjkeller.kmbapi.controller.dataaccess.EmployeeLogFacade;
import com.jjkeller.kmbapi.controller.interfaces.IEmployeeLogFacade;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbui.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class SubmitLogsViewOnlyFrag extends BaseFragment {

    SubmitLogsViewOnlyFragActions actionsListener;
    SubmitLogsViewOnlyFragControllerMethods controlListener;

    private List<Date> _logDatesToSubmitList = new ArrayList<Date>();
    private List<Date> _unsubmittedEmployeeLogDates;
    private LogSubmissionAdapter _logDateAdapter;

    private GridView _grid;
    private Button _btnSubmit;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        this.setRetainInstance(true);
        View v = inflater.inflate(R.layout.f_submitlogsviewonly, container, false);
        findControls(v);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        this.loadControls(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        // Save each checked checkbox in state bundle
        long[] checkedList = new long[_logDatesToSubmitList.size()];
        for(int i = 0; i < checkedList.length; i++)
        {
            checkedList[i] = _logDatesToSubmitList.get(i).getTime();
        }
        outState.putLongArray("logsToSubmit", checkedList);

        super.onSaveInstanceState(outState);
    }

    protected void findControls(View v)
    {
        _btnSubmit = (Button)v.findViewById(R.id.btnSubmit);
        _btnSubmit.setOnClickListener(
                new OnClickListener() {
                    public void onClick(View v) {
                        actionsListener.handleSubmitButtonClick();
                    }
                });
        _grid = (GridView) v.findViewById(R.id.grdSubmitLogs);
    }

    protected void loadControls(Bundle savedInstanceState)
    {
        APIControllerBase listenerCast = (APIControllerBase)this.controlListener.getMyController();
        IEmployeeLogFacade facade = new EmployeeLogFacade(listenerCast.getContext(), listenerCast.getCurrentUser());
        List<EmployeeLog> unsubmittedEmployeeLogs = facade.GetLocalLogList(false);

        _unsubmittedEmployeeLogDates = new ArrayList<Date>();

        if (!unsubmittedEmployeeLogs.isEmpty())
        {
            // Load unsubmitted log dates into the getUnsubmittedEmployeeLogDates var
            for (EmployeeLog log : unsubmittedEmployeeLogs) {
                _unsubmittedEmployeeLogDates.add(log.getLogDate());
            }

            // sort the unsubmitted local log dates with the newest on top
            Collections.sort(_unsubmittedEmployeeLogDates, Collections.reverseOrder());
            _logDateAdapter = new LogSubmissionAdapter(getActivity(), R.id.chkLogDate, _unsubmittedEmployeeLogDates);
            _grid.setAdapter(_logDateAdapter);
        }
        else
        {
            // display message
            ((BaseActivity)getActivity()).ShowMessage(getActivity(), getString(R.string.msg_alert),getString(R.string.msg_alert_no_logs_exist_to_submit));
        }

        if (savedInstanceState != null && savedInstanceState.containsKey("logsToSubmit"))
        {
            long[] dates = savedInstanceState.getLongArray("logsToSubmit");
            for (int i = 0; i < dates.length; i++) {
                Calendar calSaved = Calendar.getInstance();
                calSaved.setTimeZone(TimeZone.getDefault());
                calSaved.setTimeInMillis(dates[i]);

                if (!getLogDatesToSubmitList().contains(calSaved.getTime()))
                {
                    getLogDatesToSubmitList().add(calSaved.getTime());
                }
            }
        }

        refreshSumbitButton();
    }

    // removes the selected dates from the unsubmitted date grid
    public void removeSelectedLogsFromGrid() {
        for (Date date : _logDatesToSubmitList) {
            _logDateAdapter.remove(date);
        }

        // clear the list of dates to submit as they have now been submitted
        _logDatesToSubmitList = new ArrayList<Date>();
    }


    // disable / enable the submit button depending on if the user has selected date(s) to submit
    private void refreshSumbitButton() {
        if (getLogDatesToSubmitList().size() > 0)
            getSubmitButton().setEnabled(true);
        else
            getSubmitButton().setEnabled(false);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            actionsListener = (SubmitLogsViewOnlyFragActions) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement SubmitLogsFragActions");
        }

        try {
            controlListener = (SubmitLogsViewOnlyFragControllerMethods) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement SubmitLogsControllerMethods");
        }
    }

    public Button getSubmitButton() {
        if(_btnSubmit == null)
            _btnSubmit = (Button)getView().findViewById(R.id.btnSubmit);
        return _btnSubmit;
    }

    public List<Date> getLogDatesToSubmitList() {
        if (_logDatesToSubmitList == null)
            _logDatesToSubmitList = new ArrayList<Date>();
        return _logDatesToSubmitList;
    }

    public class LogSubmissionAdapter extends ArrayAdapter<Date> implements OnClickListener
    {
        private List<Date> unsubmittedLogs;

        public LogSubmissionAdapter(Context c, int textViewResourceId, List<Date> unsubmittedLogs)
        {
            super(c, textViewResourceId, unsubmittedLogs);
            this.unsubmittedLogs = unsubmittedLogs;
        }

        public View getView(int position, View convertView, ViewGroup parent)
        {
            View v = convertView;
            if (v == null)
            {
                LayoutInflater li = getActivity().getLayoutInflater();
                v = li.inflate(R.layout.submitlogsviewonlyrow, parent, false);
                v.setId(position);
            }

            GridView grid = (GridView) parent;
            grid.setFocusable(false);
            grid.setFocusableInTouchMode(false);

            if (!unsubmittedLogs.isEmpty())
            {
                Date currentLogDate = unsubmittedLogs.get(position);

                CheckBox cb = (CheckBox) v.findViewById(R.id.chkLogDate);
                TextView dateLabel = (TextView) v.findViewById(R.id.lblCLDate);

                cb.setTag(currentLogDate);
                cb.setOnClickListener(this);

                if (getLogDatesToSubmitList().contains(currentLogDate))
                    cb.setChecked(true);
                else
                    cb.setChecked(false);

                dateLabel.setText(DateUtility.getDateFormat().format(currentLogDate));
            }

            return v;
        }

        public void onClick(View view)
        {
            CheckBox checkBox = (CheckBox) view.findViewById(R.id.chkLogDate);
            Date date = (Date) checkBox.getTag();

            if (checkBox.isChecked())
            {
                if (!getLogDatesToSubmitList().contains(date))
                {
                    getLogDatesToSubmitList().add(date);
                }
            }
            else
            {
                if (getLogDatesToSubmitList().contains(date))
                {
                    getLogDatesToSubmitList().remove(date);
                }
            }
            // only enable the submit button if the user has checked at least one unsubmitted date
            if (getLogDatesToSubmitList().size() > 0)
            {
                getSubmitButton().setEnabled(true);
            }
            else
            {
                getSubmitButton().setEnabled(false);
            }
        }
    }
}
