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

import com.jjkeller.kmb.interfaces.ICertifyLogs.CertifyLogsControllerMethods;
import com.jjkeller.kmb.interfaces.ICertifyLogs.CertifyLogsFragActions;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbapi.controller.LogEntryController;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbui.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class CertifyLogsFrag extends BaseFragment
{
    private CertifyLogsFragActions actionsListener;
    private CertifyLogsControllerMethods certifyLogsControllerListener;

    private List<Date> _uncertifiedLogList;
    private List<Date> _certifiedUnsubmittedLogList;
    private GridView _grid;

    private TextView _tvMessage;
    private TextView _tvTitle;
    private Button _btnCertify;

    public List<Date> _logsToCertifyList = new ArrayList<Date>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        this.setRetainInstance(true);
        View v = inflater.inflate(R.layout.f_certifylogs, container, false);
        findControls(v);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        this.loadControls(savedInstanceState);
    }

    protected void findControls(View v)
    {
        _btnCertify = (Button) v.findViewById(R.id.btnCLCertify);
        _btnCertify.setOnClickListener(
                new OnClickListener() {
                    public void onClick(View v) {
                        actionsListener.handleSubmitButtonClick();
                    }
                });

        _grid = (GridView) v.findViewById(R.id.grdCertifyLogs);
        _tvMessage = (TextView)v.findViewById(R.id.tvMessage);
        _tvTitle = (TextView)v.findViewById(R.id.tvTitle);
    }

    protected void loadControls(Bundle savedInstanceState)
    {
        _uncertifiedLogList = certifyLogsControllerListener.getUncertifiedLogDates();

        //we also want to display any certified logs that have not been submitted (with a checked disabled checkbox); They will be submitted on the next successful submit.
        _certifiedUnsubmittedLogList = certifyLogsControllerListener.getCertifiedUnsubmittedLogDates();

        for (Date logDate : _certifiedUnsubmittedLogList)
        {
            _uncertifiedLogList.add(logDate);
            _logsToCertifyList.add(logDate);
        }

        Collections.sort(_uncertifiedLogList, Collections.<Date>reverseOrder());

        if (savedInstanceState != null && savedInstanceState.containsKey("logsToCertify"))
        {
            long[] dates = savedInstanceState.getLongArray("logsToCertify");
            for (int i = 0; i < dates.length; i++)
            {
                Calendar calSaved = Calendar.getInstance();
                calSaved.setTimeZone(TimeZone.getDefault());
                calSaved.setTimeInMillis(dates[i]);
                _logsToCertifyList.add(i, calSaved.getTime());
            }
        }

        if (_uncertifiedLogList.isEmpty()&& _logsToCertifyList.isEmpty()) {
            _btnCertify.setVisibility(View.INVISIBLE);
            _tvMessage.setVisibility(View.INVISIBLE);
            _grid.setVisibility(View.INVISIBLE);
            actionsListener.handleSubmitButtonClick();
        }
        else {
            LogCertificationAdapter logDateAdapter = new LogCertificationAdapter(getActivity(), R.id.chkLogDate, _uncertifiedLogList);
            _grid.setAdapter(logDateAdapter);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        // 7/10/12 ACM - Save each checked checkbox in state bundle
        long[] checkedList = new long[_logsToCertifyList.size()];
        for(int i = 0; i < checkedList.length; i++)
        {
            checkedList[i] = _logsToCertifyList.get(i).getTime();
        }
        outState.putLongArray("logsToCertify", checkedList);

        super.onSaveInstanceState(outState);
    }

    public GridView getGrid()
    {
        if (_grid == null)
            _grid = (GridView) getView().findViewById(R.id.grdMissingLogDates);
        return _grid;
    }

    public Button getCertifyButton()
    {
        if (_btnCertify == null)
            _btnCertify = (Button) getView().findViewById(R.id.btnCLCertify);
        return _btnCertify;
    }

    public GridView getLogsGridView()
    {
        if (_grid == null)
            _grid = (GridView) getView().findViewById(R.id.grdMissingLogDates);
        return _grid;
    }

    public TextView getMessageTextView()
    {
        if (_tvMessage == null)
            _tvMessage = (TextView) getView().findViewById(R.id.tvMessage);
        return _tvMessage;
    }
    public TextView getTitleTextView()
    {
        if (_tvTitle == null)
            _tvTitle = (TextView) getView().findViewById(R.id.tvTitle);
        return _tvTitle;
    }

    public List<Date> getSelectedLogDates()
    {
        return this._logsToCertifyList;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            actionsListener = (CertifyLogsFragActions) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement LogoutFragActions");
        }

        try {
            certifyLogsControllerListener = (CertifyLogsControllerMethods) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement CertifyLogsControllerMethods");
        }
    }

    public class LogCertificationAdapter extends ArrayAdapter<Date> implements OnClickListener
    {
        private List<Date> uncertifiedLogs;

        public LogCertificationAdapter(Context c, int textViewResourceId, List<Date> uncertifiedLogs)
        {
            super(c, textViewResourceId, uncertifiedLogs);
            this.uncertifiedLogs = uncertifiedLogs;
        }

        public int getCount()
        {
            if (uncertifiedLogs != null)
                return uncertifiedLogs.size();
            else
                return 0;
        }

        public View getView(int position, View convertView, ViewGroup parent)
        {
            View v = convertView;
            if (v == null)
            {
                LayoutInflater li = getActivity().getLayoutInflater();
                v = li.inflate(R.layout.certifylogsrow, parent, false);
                v.setId(position);
            }

            GridView grid = (GridView) parent;
            grid.setFocusable(false);
            grid.setFocusableInTouchMode(false);

            if (uncertifiedLogs != null)
            {
                Date currentLogDate = uncertifiedLogs.get(position);

                CheckBox cb = (CheckBox) v.findViewById(R.id.chkLogDate);
                TextView dateLabel = (TextView) v.findViewById(R.id.lblCLDate);


                cb.setTag(currentLogDate);
                cb.setOnClickListener(this);
                if (_logsToCertifyList != null && _logsToCertifyList.contains(currentLogDate))
                    cb.setChecked(true);
                else
                    cb.setChecked(false);

                if(_certifiedUnsubmittedLogList != null && _certifiedUnsubmittedLogList.contains(currentLogDate))
                {
                    //disable checkbox for certied but unsubmitted logs
                    cb.setEnabled(false);

                    //in case we want to change this label from Requires Certification to soemthing else....uncomment below code
                    //TextView certificationLabel = (TextView) v.findViewById(R.id.lblCLState);
                    //certificationLabel.setText("Certified/Unsubmitted");
                }

                dateLabel.setText(DateUtility.getDateFormat().format(currentLogDate));
            }

            return v;
        }

        public long getItemId(int position)
        {
            return 0;
        }

        public void onClick(View view)
        {
            CheckBox cb = (CheckBox) view.findViewById(R.id.chkLogDate);
            Date date = (Date) cb.getTag();

            if (cb.isChecked())
            {
                if (_logsToCertifyList == null)
                {
                    _logsToCertifyList = new ArrayList<Date>();
                    _logsToCertifyList.add(date);
                }
                else
                {
                    if (!_logsToCertifyList.contains(date))
                    {
                        _logsToCertifyList.add(date);
                    }
                }
            }
            else
            {
                if (_logsToCertifyList != null)
                {
                    if (_logsToCertifyList.contains(date))
                    {
                        _logsToCertifyList.remove(date);
                    }
                }
            }
        }
    }
}
