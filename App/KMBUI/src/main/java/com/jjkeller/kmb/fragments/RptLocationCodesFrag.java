package com.jjkeller.kmb.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbui.R;

public class RptLocationCodesFrag extends BaseFragment
{
	private GridView _grid;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.f_rptlocationcodes, container, false);
		findControls(v);
		return v;
	}

	protected void findControls(View v)
	{
		_grid = (GridView)v.findViewById(R.id.lcr_grdLocationCodes);
	}

	public GridView getGrid()
	{
		return _grid;
	}
}
