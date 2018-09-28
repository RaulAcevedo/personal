package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jjkeller.kmb.interfaces.IOffDuty;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbapi.calcengine.OffDuty;
import com.jjkeller.kmbapi.controller.HosAuditController;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbui.R;

import java.util.Calendar;
import java.util.Date;

public class ShowOffDutyFrag extends BaseFragment {
    IOffDuty.ShowOffDutyFragActions actionsListener;
    private TextView _tvOffDuty;
    private LinearLayout _oDH;
    private ImageButton _btnCloseOffDuty;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.f_offduty , container, false);
        findControls(v);
        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            actionsListener = ( IOffDuty.ShowOffDutyFragActions) activity;
        } catch (ClassCastException e) {
         //   throw new ClassCastException(activity.toString() + " must implement " + IOffDuty.ShowOffDutyFragActions.class.getSimpleName());
        }

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        actionsListener.updateOffDutyMessage();
    }

    protected void findControls(View v)
    {
        _tvOffDuty = (TextView) v.findViewById(R.id.tvOffDuty);
        _oDH= (LinearLayout) v.findViewById(R.id.OffDutyHeader);

        _btnCloseOffDuty = (ImageButton)v.findViewById(R.id.btnCloseOffDuty);
        if(_btnCloseOffDuty != null)
        {
            _btnCloseOffDuty.setOnClickListener(
                    new View.OnClickListener() {
                        public void onClick(View v) {
                            actionsListener.handlebtnCloseOffDutyClick();
                        }
                    });
        }

    }

    public void SetOffDutyMessage(String msg) {
        if (_tvOffDuty != null) {
            _tvOffDuty.setText(msg);
        }
    }

    public void SetOffDutyFragment(){
        try {
                HosAuditController c = new HosAuditController(this.getActivity());
                OffDuty od = c.getOffDutyInfo();
                if (od != null && od.getIsOffDuty()) {
                    Calendar startCal = Calendar.getInstance();
                    startCal.setTime(od.getLastOffDutyTime());
                    Date currentHomeTime = DateUtility.getCurrentHomeTerminalTime(c.getCurrentUser());
                    long secs = (currentHomeTime.getTime() - startCal.getTimeInMillis()) / 1000;
                    int hours = (int) secs / 3600;
                    secs = secs - hours * 3600;
                    int mins = (int) secs / 60;
                    if ((hours == 34 && mins > 0) || hours > 34)
                        SetOffDutyMessage("You have been OFF - DUTY for: 34+ hrs ");
                    else {
                        if (hours > 0)
                            SetOffDutyMessage("You have been OFF - DUTY for: " + hours + "hr " + mins + "m");
                        else if (mins > 0)
                            SetOffDutyMessage("You have been OFF - DUTY for: " + mins + "m ");
                        else
                            SetOffDutyMessage("You have been OFF - DUTY for: 0m ");
                    }
                }
        }
        catch(Exception ex){

        }
    }
}
