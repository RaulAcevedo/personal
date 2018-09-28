package com.jjkeller.kmb.fragments;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbapi.enums.DataDiagnosticEnum;
import com.jjkeller.kmbapi.enums.Describable;
import com.jjkeller.kmbapi.enums.Malfunction;
import com.jjkeller.kmbui.R;

import java.util.List;

public class RptMalfunctionAndDataDiagnosticFrag extends BaseFragment{

    private TextView malfunctionBadgeHeader;
    private TextView malfunctionHeader;
    private LinearLayout malfunctionBadgeLayout;
    private LinearLayout malfunctionDisplayLayout;

    private TextView diagnosticBadgeHeader;
    private TextView diagnosticHeader;
    private LinearLayout diagnosticBadgeLayout;
    private LinearLayout diagnosticDisplayLayout;

    private TextView malfunctionMessage;
    private TextView diagnosticMessage;


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.f_rptmalfunctionanddatadiagnostic, container, false);
        setControlText(v);
        return v;

    }

    protected void setControlText(View v) {
        malfunctionMessage = (TextView) v.findViewById(R.id.malfunction_message);
        diagnosticMessage = (TextView) v.findViewById(R.id.data_diagnostic_message);

        // malfunction text values
        malfunctionBadgeHeader = (TextView) v.findViewById(R.id.malf_header_top);
        malfunctionHeader = (TextView) v.findViewById(R.id.malf_header_name);
        malfunctionBadgeLayout = (LinearLayout) v.findViewById(R.id.malfunction_badge_list);
        malfunctionDisplayLayout = (LinearLayout) v.findViewById(R.id.malfunction_list);

        diagnosticBadgeHeader = (TextView) v.findViewById(R.id.diag_header_top);
        diagnosticHeader = (TextView) v.findViewById(R.id.diag_header_name);
        diagnosticBadgeLayout = (LinearLayout) v.findViewById(R.id.diagnostic_badge_list);
        diagnosticDisplayLayout = (LinearLayout) v.findViewById(R.id.diagnostic_list);
    }

    public void setDataDiagnosticList(List<DataDiagnosticEnum> dataDiagnosticList){
        if (dataDiagnosticList.isEmpty()){
            diagnosticBadgeHeader.setVisibility(View.GONE);
            diagnosticHeader.setVisibility(View.GONE);
            return;
        }
        boolean isFirst = true;
        for (DataDiagnosticEnum dataDiagnosticEnum : dataDiagnosticList){
            diagnosticBadgeLayout.addView(createBadge(dataDiagnosticEnum.toDMOEnum(), false));
            diagnosticDisplayLayout.addView(createDisplay(dataDiagnosticEnum.toDMOEnum(), dataDiagnosticEnum, isFirst, false));
            isFirst = false;
        }
    }

    public void setMalfunctionList(List<Malfunction> malfunctionList){
        if (malfunctionList.isEmpty()){
            malfunctionBadgeHeader.setVisibility(View.GONE);
            malfunctionHeader.setVisibility(View.GONE);
            return;
        }

        boolean isFirst = true;
        for (Malfunction malfunction : malfunctionList){
            malfunctionBadgeLayout.addView(createBadge(malfunction.getDmoValue(), true));
            malfunctionDisplayLayout.addView(createDisplay(malfunction.getDmoValue(), malfunction, isFirst, true));

            isFirst = false;
        }
    }

    private LinearLayout createDisplay(String code, Describable describable, boolean isFirst, boolean isMalfunction){
        LinearLayout display = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.view_malfunction_diagonstic_display, null);

        TextView displayCode = (TextView) display.findViewById(R.id.display_code);
        if (isMalfunction) {
            setBackground(displayCode, R.drawable.rounded_corner_red);
        } else {
            setBackground(displayCode, R.drawable.rounded_corner_gold);
        }

        TextView displayName = (TextView) display.findViewById(R.id.display_name);
        TextView displayDescription = (TextView) display.findViewById(R.id.display_description);

        displayCode.setText(code);
        displayName.setText(describable.getDescriptionKey());
        displayDescription.setText(describable.getFullDescriptionKey());

        if (isFirst){
            display.findViewById(R.id.display_separator).setVisibility(View.GONE);
        }

        return display;
    }

    private TextView createBadge(String code, boolean  isMalfunction){
        TextView badge = (TextView) getActivity().getLayoutInflater().inflate(R.layout.view_malfunction_diagonstic_badge, null);
        if (isMalfunction) {
            setBackground(badge, R.drawable.rounded_corner_red);
        } else {
            setBackground(badge, R.drawable.rounded_corner_gold);
        }
        badge.setText(code);
        return badge;
    }

    private void setBackground(View view, int backgroundResource){
        int paddingLeft = view.getPaddingLeft();
        int paddingRight = view.getPaddingRight();
        int paddingTop = view.getPaddingTop();
        int paddingBottom = view.getPaddingBottom();

        view.setBackgroundResource(backgroundResource);
        view.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
    }

    public void displayMalfunctionMessage(){
        malfunctionMessage.setVisibility(View.VISIBLE);
        diagnosticMessage.setVisibility(View.GONE);
    }

    public void displayDiagnosticMessage(){
        malfunctionMessage.setVisibility(View.GONE);
        diagnosticMessage.setVisibility(View.VISIBLE);
    }

}
