package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.jjkeller.kmb.interfaces.IRequestLogs.RequestLogsFragActions;
import com.jjkeller.kmb.interfaces.IRequestLogs.RequestLogsFragControllerMethods;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbui.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by t000253 on 2/1/2016.
 */
public class RequestLogsFrag extends BaseFragment {
    RequestLogsFragActions actionsListener;
    RequestLogsFragControllerMethods controlListener;

    private Button _btnSubmit;
    private Button _btnStartDate;
    private Button _btnEndDate;
    private CheckBox _cbRememberMe;
    private TextView _tvEmailAddress;

    private Date _startDate = DateUtility.AddDays(DateUtility.getSixMonthDateTimeUTC(), 0);
    private Date _endDate = DateUtility.getCurrentDateTimeUTC();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        //this.setRetainInstance(true);
        View v = inflater.inflate(R.layout.f_requestlogs, container, false);
        findControls(v);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        this.loadControls();
        this.loadDefaultInfo();
    }

    public void loadDefaultInfo(){
        Context ctx = GlobalState.getInstance().getApplicationContext();
        SharedPreferences userPref = ctx.getSharedPreferences(this.getString(R.string.sharedpreferencefile), 0);

        if(userPref.getBoolean(this.getString(R.string.remembermechecked), false)){
            String defaultEmailAddress = userPref.getString(this.getString(R.string.defaultemailaddress), "");
            if(defaultEmailAddress.length()> 0){
                this.SetEmailAddress(defaultEmailAddress);
                this._cbRememberMe.setChecked(userPref.getBoolean(this.getString(R.string.remembermechecked), false));
            }
        }
    }

    protected void findControls(View v)
    {
        _tvEmailAddress = (TextView)v.findViewById(R.id.tvemailaddress);

        _btnStartDate = (Button)v.findViewById(R.id.btnrequeststartDate);
        _btnStartDate.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        ShowDatePickerDialog(_btnStartDate);
                    }
                });

        _btnEndDate = (Button)v.findViewById(R.id.btnrequestendDate);
        _btnEndDate.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        ShowDatePickerDialog(_btnEndDate);
                    }
                });

        _btnSubmit = (Button)v.findViewById(R.id.btnSubmit);
        _btnSubmit.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        actionsListener.handleSubmitButtonClick();
                    }
                });

        _cbRememberMe = (CheckBox)v.findViewById(R.id.cbrememberme);
        _cbRememberMe.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        actionsListener.handleRememberMe(isChecked);
                    }
                });
    }

    public void SetStartDate(Date startDate)
    {
        _startDate = startDate;
        updateDateDisplays();
    }

    public Date GetStartDate()
    {
        String dateString = _btnStartDate.getText().toString();
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");

        Date d = null;
        try {
            d = formatter.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return d;
    }

    public void SetEndDate(Date endDate)
    {
        _endDate = endDate;
        updateDateDisplays();
    }

    public void SetEmailAddress(String emailAddress){
        _tvEmailAddress.setText(emailAddress);
    }

    public Date GetEndDate()
    {
        String dateString = _btnEndDate.getText().toString();
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");

        Date d = null;
        try {
            d = formatter.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return d;
    }

    private void updateDateDisplays()
    {
        _btnStartDate.setText(DateUtility.getDateFormat().format(_startDate));
        _btnEndDate.setText(DateUtility.getDateFormat().format(_endDate));
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            controlListener = (RequestLogsFragControllerMethods) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement RequestLogsFragControllerMethods");
        }

        try {
            actionsListener = (RequestLogsFragActions) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement RequestLogsFragActions");
        }
    }

    protected void loadControls()
    {
        updateDateDisplays();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putLong("startTime", GetStartDate().getTime());
        outState.putLong("endTime", GetEndDate().getTime());

        super.onSaveInstanceState(outState);
    }

    public Button getSubmitButton(){
        if(_btnSubmit == null)
            _btnSubmit = (Button)getView().findViewById(R.id.btnSubmit);
        return _btnSubmit;
    }

    public TextView getEmailAddressTextView()
    {
        if(_tvEmailAddress == null)
            _tvEmailAddress = (TextView)getView().findViewById(R.id.tvemailaddress);
        return _tvEmailAddress;
    }

}
