package com.jjkeller.kmb.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbui.R;

public class RptDataUsageFrag extends BaseFragment
{
	private ListView _grid;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.f_rptdatausage, container, false);
		findControls(v);
		return v;
	}

	protected void findControls(View v)
	{
		_grid = (ListView)v.findViewById(R.id.rdu_griddatausage);
	}

	public ListView getGrid()
	{
		return _grid;
	}
}
