package com.jjkeller.kmb.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbui.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by T000694 on 5/29/2017.
 */

public class LimitedRangeDatePickerFrag extends BaseFragment implements AdapterView.OnItemSelectedListener {

    private static final String EXTRA_MIN_DATE = "EXTRA_MIN_DATE";
    private static final String EXTRA_MAX_DATE = "EXTRA_MAX_DATE";

    //Constant flags which are used to identify when a spinner automatically fires the event
    private static final int NOT_AUTOMATIC = 0;
    private static final int SPINNER = 1;

    private List<Date> _dateList;
    private Spinner _spDate;
    private OnDatePickListener onDatePickListener;
    private int automaticTriggerCount;

    private View vLayout;

    public static LimitedRangeDatePickerFrag newInstance(Date minDate, Date maxDate){
        LimitedRangeDatePickerFrag fragment = new LimitedRangeDatePickerFrag();
        Bundle bundle = new Bundle();

        bundle.putSerializable(EXTRA_MIN_DATE, minDate);
        bundle.putSerializable(EXTRA_MAX_DATE, maxDate);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        vLayout = inflater.inflate(R.layout.f_limited_range_date_picker, container);

        initDateList();
        loadControls(savedInstanceState);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (_spDate != null) {
            outState.putInt(getResources().getString(R.string.state_logdate), (int) _spDate.getSelectedItemId());
        }
    }

    private void initDateList(){
        Calendar calendar = Calendar.getInstance();
        _dateList = new ArrayList<>();

        calendar.setTime(getExtraMaxDate());

        while (calendar.getTime().compareTo(getExtraMinDate()) >= 0){
            _dateList.add(calendar.getTime());
            calendar.add(Calendar.DAY_OF_MONTH, -1);
        }
    }

    private void loadControls(Bundle savedInstanceState){
        String[] logDateArray = new String[_dateList.size()];

        // put the log date in an array for the spinner control.
        for (int index = 0; index < _dateList.size(); index++) {
            Date logDate = _dateList.get(index);
            logDateArray[index] = DateUtility.getHomeTerminalShortDateFormat().format(logDate);
        }

        _spDate = (Spinner) vLayout.findViewById(R.id.spDate);
        Button _btnPreviousDay = (Button) vLayout.findViewById(R.id.btPrevious);
        Button _btnNextDay = (Button) vLayout.findViewById(R.id.btNext);


        _btnPreviousDay.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int index = _spDate.getSelectedItemPosition();

                if (index < _spDate.getCount() - 1) {
                    _spDate.setSelection(index + 1);
                    if (onDatePickListener != null){
                        onDatePickListener.onDatePick(_dateList.get(_spDate.getSelectedItemPosition()));
                    }
                }
            }
        });

        _btnNextDay.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int index = _spDate.getSelectedItemPosition();

                if (index > 0) {
                    _spDate.setSelection(index - 1);
                    if (onDatePickListener != null){
                        onDatePickListener.onDatePick(_dateList.get(_spDate.getSelectedItemPosition()));
                    }
                }
            }
        });
        ArrayAdapter<String> logDateAdapter = new ArrayAdapter<String>(getActivity(), R.layout.kmb_spinner_item, logDateArray);
        logDateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        _spDate.setAdapter(logDateAdapter);
        _spDate.setOnItemSelectedListener(this);

        if (savedInstanceState != null) {
            _spDate.setSelection(savedInstanceState.getInt(getResources().getString(R.string.state_logdate)));
        }

        automaticTriggerCount = SPINNER;
    }

    private Date getExtraMinDate(){
        return (Date) getArguments().getSerializable(EXTRA_MIN_DATE);
    }

    private Date getExtraMaxDate(){
        return (Date) getArguments().getSerializable(EXTRA_MAX_DATE);
    }

    public void setOnDatePickListener(OnDatePickListener onDatePickListener) {
        this.onDatePickListener = onDatePickListener;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if (automaticTriggerCount == NOT_AUTOMATIC) {
            if (onDatePickListener != null){
                onDatePickListener.onDatePick(_dateList.get(_spDate.getSelectedItemPosition()));
            }
        } else {
            automaticTriggerCount--;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {}

    public interface OnDatePickListener{
        void onDatePick(Date date);
    }
}
