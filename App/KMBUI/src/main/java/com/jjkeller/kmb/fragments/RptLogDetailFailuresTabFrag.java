package com.jjkeller.kmb.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.interfaces.IAPIController;
import com.jjkeller.kmbapi.controller.share.MandateObjectFactory;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.enums.FailureCategoryEnum;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.proxydata.FailureReport;
import com.jjkeller.kmbui.R;

import org.joda.time.DateTime;

import java.util.List;

import static com.jjkeller.kmbapi.controller.utility.DateUtility.isNullOrEpoch;

public class RptLogDetailFailuresTabFrag extends BaseFragment
{
	private GridView _grdFailures;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.tabfailures, container, false);
		findControls(v);
		loadControls();
		return v;
	}

	private void findControls(View v)
	{
		_grdFailures = (GridView)v.findViewById(R.id.grdTeamDriver);
	}

	private void loadControls()
	{
		loadGrid();
	}

	private void loadGrid()
	{
		IAPIController controller = MandateObjectFactory.getInstance(getActivity(), GlobalState.getInstance().getFeatureService()).getCurrentEventController();

		EmployeeLog empLog = controller.getSelectedLogForReport();

		if (empLog != null)
		{
			List<FailureReport> listFailureReport = controller.getFailuresForDutyFailureReport(empLog);
			if (listFailureReport.isEmpty())
			{
				FailureReport failureReport = new FailureReport();
				failureReport.setStartTime(null);
				failureReport.setMessage(" ");
				listFailureReport.add(failureReport);
			}

			FailureReportAdapter adapter = new FailureReportAdapter(getActivity(), R.layout.grdfailures, listFailureReport);
			_grdFailures.setAdapter(adapter);
		}
	}

	private class FailureReportAdapter extends ArrayAdapter<FailureReport>
	{
		private List<FailureReport> items;

		public FailureReportAdapter(Context context, int textViewResourceId, List<FailureReport> items)
		{
			super(context, textViewResourceId, items);
			this.items = items;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			View v = convertView;
			if (v == null)
			{
				LayoutInflater vi = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.grdfailures, null);
			}

			FailureReport failureReport = items.get(position);

			TextView tvStart = (TextView)v.findViewById(R.id.tvStart);
			TextView tvStop = (TextView)v.findViewById(R.id.tvStop);
			TextView tvType = (TextView)v.findViewById(R.id.tvType);
			TextView tvMessage = (TextView)v.findViewById(R.id.tvMessage);

			if ( !isNullOrEpoch(failureReport.getStartTime()))
			{
				tvStart.setText(DateUtility.getHomeTerminalTime12HourFormat().format(failureReport.getStartTime().toDate()));
			}
			else
			{
				tvStart.setText("");
			}

			if ( !isNullOrEpoch(failureReport.getStopTime()) )
			{
				tvStop.setText(DateUtility.getHomeTerminalTime12HourFormat().format(failureReport.getStopTime().toDate()));
			}
			else
			{
				tvStop.setText("");
			}

			String category = "";
			switch (failureReport.getCategory().getValue())
			{
				case FailureCategoryEnum.CLOCKSYNCHRONIZATION:
				case FailureCategoryEnum.EOBRDEVICE:
					category = failureReport.getCategory().getString(getContext());
					break;
			}
			tvType.setText(category);

			tvMessage.setText(failureReport.getMessage());

			return v;
		}
	}


}
