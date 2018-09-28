package com.jjkeller.kmb.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.Spinner;

import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbui.R;

public class RptDutyFailuresFrag extends BaseFragment
{
	private Spinner _cboLogDate;
	private GridView _grid;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.f_rptdutyfailures, container, false);
		findControls(v);
		return v;
	}

	protected void findControls(View v)
	{
		_cboLogDate = (Spinner)v.findViewById(R.id.cboLogDate);
		_grid = (GridView)v.findViewById(R.id.grdDutyFailures);
	}

	public Spinner getLogDate()
	{
		return _cboLogDate;
	}

	public GridView getGrid()
	{
		return _grid;
	}
}
