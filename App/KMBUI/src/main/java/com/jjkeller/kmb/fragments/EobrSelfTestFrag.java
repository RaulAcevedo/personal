package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jjkeller.kmb.interfaces.IEobrSelfTest.EobrSelfTestFragActions;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbui.R;

public class EobrSelfTestFrag extends BaseFragment
{
	private EobrSelfTestFragActions _actionListener;
	
	private TextView _selfTestResult;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View v = inflater.inflate(R.layout.f_eobrselftest, container, false);
		findControls(v);
		return v;
	}

	protected void findControls(View v)
	{
		_selfTestResult = (TextView)v.findViewById(R.id.tvSelfTestResult);
		
		v.findViewById(R.id.btnSetSelfTest).setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				_actionListener.handleStartSelfTestButtonClick();
			}
		});

		v.findViewById(R.id.btnGetSelfTest).setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				_actionListener.handleGetSelfTestButtonClick();
			}
		});
		if (!GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
			TextView title = (TextView) v.findViewById(R.id.title);
			title.setText(getString(R.string.lbl_eobr_self_test_title));
		}
	}

	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		try
		{
			_actionListener = (EobrSelfTestFragActions)activity;
		}
		catch (ClassCastException e)
		{
			throw new ClassCastException(activity.toString() + " must implement EobrSelfTestFragActions");
		}
	}

	public TextView getSelfTestResultLabel()
	{
		return _selfTestResult;
	}
}
