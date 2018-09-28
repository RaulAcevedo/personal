package com.jjkeller.kmb.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.TextView;

import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbui.R;

public class RptDutyStatusFrag extends BaseFragment
{
	private Spinner _cboLogDate;
	private GridView _grid;
	private TextView _exemptlbl;
    private TextView _txtLogDate;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.f_rptdutystatus, container, false);
		findControls(v);
		return v;
	}
	
	public void setExemptLabel(boolean isVisible)
	{
		
		if (_exemptlbl != null)
		{
			if (!isVisible)
				_exemptlbl.setVisibility(View.GONE);
			else
				_exemptlbl.setVisibility(View.VISIBLE);
		}

	}
	protected void findControls(View v)
	{
		_cboLogDate = (Spinner)v.findViewById(R.id.cboLogDate);
		_grid = (GridView)v.findViewById(R.id.grdDutyStatus);
		_exemptlbl = (TextView)v.findViewById(R.id.txtExemptLog);
        _txtLogDate = (TextView)v.findViewById(R.id.txtLogDate);
        _txtLogDate.setVisibility(View.GONE);
        if (GlobalState.getInstance().isReviewEldEvent()) {
            _cboLogDate.setVisibility(View.GONE);
            _txtLogDate.setText(DateUtility.getHomeTerminalDateFormat().format(GlobalState.getInstance().getReviewEldEventDate()));
            _txtLogDate.setVisibility(View.VISIBLE);
        }

	}

	public Spinner getLogDateSpinner()
	{
		return _cboLogDate;
	}
	
	public void setLogDateSpinner(String logDate)
	{
		for(int i = 0; i < _cboLogDate.getCount(); i++){
			if(_cboLogDate.getItemAtPosition(i).toString().equals(logDate))
				_cboLogDate.setSelection(i);
		}
	}

	public GridView getGrid()
	{
		return _grid;
	}
}
