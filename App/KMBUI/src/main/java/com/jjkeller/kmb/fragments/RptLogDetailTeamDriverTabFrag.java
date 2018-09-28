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
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.proxydata.TeamDriver;
import com.jjkeller.kmbui.R;

import java.util.List;

public class RptLogDetailTeamDriverTabFrag extends BaseFragment
{
	private GridView _grdTeamDriver;
	private boolean _teamDriverListIsEmpty = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.tabteamdrivers, container, false);
		findControls(v);
		loadControls();
		return v;
	}

	private void findControls(View v)
	{
		_grdTeamDriver = (GridView)v.findViewById(R.id.grdTeamDriver);
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
			List<TeamDriver> listTeamDriver = controller.getTeamDriversOnLog(empLog);
			if (listTeamDriver.isEmpty())
			{
				_teamDriverListIsEmpty = true;
				TeamDriver driver = new TeamDriver();
				driver.setDisplayName("");
				listTeamDriver.add(driver);
			}

			TeamDriverAdapter adapter = new TeamDriverAdapter(getActivity(), R.layout.grddutystatus, listTeamDriver);
			_grdTeamDriver.setAdapter(adapter);
		}
	}

	private class TeamDriverAdapter extends ArrayAdapter<TeamDriver>
	{
		private List<TeamDriver> items;

		public TeamDriverAdapter(Context context, int textViewResourceId, List<TeamDriver> items)
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
				v = vi.inflate(R.layout.grdteamdrivers, null);
			}
			TeamDriver driver = items.get(position);

			TextView tvStart = (TextView)v.findViewById(R.id.tvStart);
			TextView tvEnd = (TextView)v.findViewById(R.id.tvEnd);
			TextView tvName = (TextView)v.findViewById(R.id.tvName);

			String startTime = null;
			String endTime = null;
			if (_teamDriverListIsEmpty)
			{
				// dummy record created in loadGrid() sets the start time to TimeKeeper.getInstance().now()
				// if start time hasn't been set, then this is a blank record.
				startTime = "";
				endTime = "";
			}
			else
			{
				if (driver.getStartTime() == null)
					startTime = "-";
				else
					startTime = DateUtility.getHomeTerminalTime12HourFormat().format(driver.getStartTime());

				if (driver.getEndTime() == null)
					endTime = "-";
				else
					endTime = DateUtility.getHomeTerminalTime12HourFormat().format(driver.getEndTime());
			}

			tvStart.setText(startTime);
			tvEnd.setText(endTime);

			if (driver.getDisplayName().length() > 0)
				tvName.setText(String.format("%1$s, (%2$s)", driver.getDisplayName(), driver.getEmployeeCode()));
			else
				tvName.setText("");

			return v;
		}
	}
}
