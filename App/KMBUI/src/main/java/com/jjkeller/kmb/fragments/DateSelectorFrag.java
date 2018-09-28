package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.jjkeller.kmb.interfaces.IDateSelectorFrag.DateSelectorControllerMethods;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbui.R;

import java.text.ParseException;
import java.util.Date;
import java.util.List;


public class DateSelectorFrag extends BaseFragment implements AdapterView.OnItemSelectedListener {

    public static final String BUNDLE_SELECTOR_POSITION = "bundleSelectorPosition";
    public static final String BUNDLE_SELECTOR_TITLE = "bundleSelectorTitle";

    protected DateSelectorControllerMethods controlListener;
    private Spinner _spnLogDate;
    private TextView _lblTitle;
    private TextView _lblSubTitle;
    private Button _btnEditLog;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.f_dateselector, container, false);
        findControls(v);
        return v;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadControls();
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            controlListener = (DateSelectorControllerMethods) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement IDateSelectorFrag.DateSelectorControllerMethods");
        }
    }

    protected void findControls(View v) {
        _spnLogDate = (Spinner) v.findViewById(R.id.spnLogDate);
        _lblTitle = (TextView) v.findViewById(R.id.lblDateSelectorTitle);
        _lblSubTitle = (TextView) v.findViewById(R.id.lblDateSelectorMsg);
        v.findViewById(R.id.btnPreviousDay).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int index = _spnLogDate.getSelectedItemPosition();
                if (index < _spnLogDate.getCount() - 1) {
                    _spnLogDate.setSelection(index + 1);
                    controlListener.handleDateChange(getSelectedDate(), index + 1);

                }
            }
        });
        v.findViewById(R.id.btnNextDay).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int index = _spnLogDate.getSelectedItemPosition();
                if (index > 0) {
                    _spnLogDate.setSelection(index - 1);
                    controlListener.handleDateChange(getSelectedDate(), index - 1);
                }
            }
        });
        _btnEditLog = (Button) v.findViewById(R.id.btnEdit);
        _btnEditLog.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                controlListener.handleEditLog();

            }
        });
    }

    public Date getSelectedDate() {
        Date selectedDate = null;
        try {
            selectedDate = DateUtility.getHomeTerminalShortDateFormat().parse(_spnLogDate.getSelectedItem().toString());
        } catch (ParseException e) {
            Log.e("UnhandledCatch", e.getMessage() + ": " + Log.getStackTraceString(e));
        }
        return selectedDate;
    }


    @Override
    public void setFragments() {
    }

    protected void loadControls() {
        Bundle bundle = getArguments();

        String dateSelectorText = "";
        int selectorPosition = 0;

        if (bundle != null){
            selectorPosition= bundle.getInt(BUNDLE_SELECTOR_POSITION, 0);
            dateSelectorText = bundle.getString(BUNDLE_SELECTOR_TITLE, "");
        }

        _lblTitle.setText(dateSelectorText);
        if (_lblSubTitle != null) {
            if (dateSelectorText.equals(this.getResources().getString(R.string.editLog)))
                _lblSubTitle.setVisibility(View.VISIBLE);
            else
                _lblSubTitle.setVisibility(View.GONE);
        }
        // The edit button is only for view grid
        if (_btnEditLog != null) {
            if (dateSelectorText.equals(this.getResources().getString(R.string.viewLog_Grid)))
                _btnEditLog.setVisibility(View.VISIBLE);
            else
                _btnEditLog.setVisibility(View.GONE);
        }

        List<Date> empLogDateList = controlListener.getEmployeeLogController().GetLogDateListForReport();
        String[] logDateArray = new String[empLogDateList.size()];
        for (int index = 0; index < empLogDateList.size(); index++)
            logDateArray[index] = DateUtility.getHomeTerminalShortDateFormat().format(empLogDateList.get(index));

        ArrayAdapter<String> logDateAdapter = new ArrayAdapter<>(getActivity(), R.layout.kmb_spinner_item, logDateArray);
        logDateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        _spnLogDate.setAdapter(logDateAdapter);
        _spnLogDate.setOnItemSelectedListener(this);
        _spnLogDate.setSelection(selectorPosition);

    }

    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
        controlListener.handleDateChange(getSelectedDate(), position);
    }

    public void onNothingSelected(AdapterView<?> parent) {
    }
}
