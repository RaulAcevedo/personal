package com.jjkeller.kmb;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.jjkeller.kmb.fragments.LeftNavFrag;
import com.jjkeller.kmb.fragments.RptDutyFailuresFrag;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.interfaces.IAPIController;
import com.jjkeller.kmbapi.controller.share.MandateObjectFactory;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.enums.FailureCategoryEnum;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.proxydata.FailureReport;
import com.jjkeller.kmbui.R;

import java.util.Date;
import java.util.List;

import static com.jjkeller.kmbapi.controller.utility.DateUtility.isNullOrEpoch;

public class RptDutyFailures extends BaseActivity implements AdapterView.OnItemSelectedListener, LeftNavFrag.OnNavItemSelectedListener, LeftNavFrag.ActivityMenuItemsListener
{
	private RptDutyFailuresFrag _contentFragment;
	private List<Date> _empLogDateList;
	private String _selectedItem = "";
	private boolean _loading = false;
	private Date _logDate;
	private EmployeeLog _empLog;
	private LoadSelectedLogTask _loadSelectedLogTask;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		_loading = true;

		setContentView(R.layout.baselayout);

		loadContentFragment(new RptDutyFailuresFrag());

		mFetchLocalDataTask = new FetchLocalDataTask(this.getClass().getSimpleName(), savedInstanceState);
		mFetchLocalDataTask.setAutoUnlockScreenRotationOnPostExecute(false);
		mFetchLocalDataTask.execute();
	}

	@Override
	protected void loadData()
	{
		_empLogDateList = getMyController().GetLogDateListForReport();
	}

	@Override
	protected void loadControls(Bundle savedInstanceState)
	{
		super.loadControls(savedInstanceState);

		String[] logDateArray = new String[_empLogDateList.size()];

		// put the log date in an array for the spinner control.
		for (int index = 0; index < _empLogDateList.size(); index++)
		{
			Date logDate = _empLogDateList.get(index);
			logDateArray[index] = DateUtility.getDateFormat().format(logDate);
		}

		ArrayAdapter<String> logDateAdapter = new ArrayAdapter<String>(this, R.layout.kmb_spinner_item, logDateArray);
		logDateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		_contentFragment.getLogDate().setAdapter(logDateAdapter);
		_contentFragment.getLogDate().setOnItemSelectedListener(this);

		if (savedInstanceState != null)
		{
			_contentFragment.getLogDate().setSelection(savedInstanceState.getInt(getResources().getString(R.string.state_logdate)));
		}

		_selectedItem = _contentFragment.getLogDate().getSelectedItem().toString();

		Date selectedDate = new Date(_selectedItem);

		this.findSelectedLog(selectedDate);

		this.loadGrid();
	}

	@Override
	public void setFragments()
	{
		super.setFragments();
		Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
		_contentFragment = (RptDutyFailuresFrag)fragment;
	}

	protected IAPIController getMyController()
	{
		return (IAPIController) this.getController();
	}

	protected void InitController()
	{
		this.setController(MandateObjectFactory.getInstance(this, GlobalState.getInstance().getFeatureService()).getCurrentEventController());
	}

	private void findSelectedLog(Date selectedDate)
	{
		_logDate = selectedDate;
		_loadSelectedLogTask = new LoadSelectedLogTask();
		_loadSelectedLogTask.execute(new Void[0]);
	}

	private void loadSelectedLog()
	{
		_empLog = getMyController().EmployeeLogsForDutyFailureReport(_logDate).get(0);
		getMyController().setSelectedLogForReport(_empLog);
	}

	private void loadGrid()
	{
		EmployeeLog empLog = this.getMyController().getSelectedLogForReport();

		if (empLog != null)
		{
			List<FailureReport> listFailureReport = this.getMyController().getFailuresForDutyFailureReport(empLog);
			if (listFailureReport.isEmpty())
			{
				FailureReport failureReport = new FailureReport();
				failureReport.setStartTime(null);
				failureReport.setMessage(" ");
				listFailureReport.add(failureReport);
			}

			FailureReportAdapter adapter = new FailureReportAdapter(this, R.layout.grdfailures, listFailureReport);
			_contentFragment.getGrid().setAdapter(adapter);
		}
	}

	public void onItemSelected(AdapterView<?> parent, View v, int position, long id)
	{
		if (_loading == true)
			_loading = false;
		else
			this.findSelectedLog(new Date(_contentFragment.getLogDate().getSelectedItem().toString()));
	}

	public void onNothingSelected(AdapterView<?> parent)
	{
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
				LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.grdfailures, null);
			}

			FailureReport failureReport = items.get(position);

			TextView tvStart = (TextView)v.findViewById(R.id.tvStart);
			TextView tvStop = (TextView)v.findViewById(R.id.tvStop);
			TextView tvType = (TextView)v.findViewById(R.id.tvType);
			TextView tvMessage = (TextView)v.findViewById(R.id.tvMessage);

			if ( ! isNullOrEpoch(failureReport.getStartTime()))
			{
				tvStart.setText(DateUtility.getHomeTerminalTime12HourFormat().format(failureReport.getStartTime().toDate()));
			}
			else
			{
				tvStart.setText("");
			}

			if ( ! isNullOrEpoch(failureReport.getStopTime()))
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



	private class LoadSelectedLogTask extends AsyncTask<Void, String, Boolean>
	{
		ProgressDialog _pd;

		@Override
		protected void onPreExecute()
		{
			LockScreenRotation();
			
			if(!RptDutyFailures.this.isFinishing())
				_pd = ProgressDialog.show(RptDutyFailures.this, "", getString(R.string.msgretreiving));
		}

		@Override
		protected Boolean doInBackground(Void... arg0)
		{
			try
			{
				loadSelectedLog();
			}
			catch (Exception e)
			{
				return false;
			}

			return true;
		}

		@Override
		protected void onPostExecute(Boolean result)
		{
			if (_pd != null && _pd.isShowing())
				_pd.dismiss();

			if (result)
			{
				findActivityControls();
				loadGrid();
			}

			UnlockScreenRotation();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		this.CreateOptionsMenu(menu, false);
		return true;
	}

	@Override
	public String getActivityMenuItemList()
	{
		return getString(R.string.rptlocationcodes_actionitems);
	}

	private void handleMenuItemSelected(int itemPosition)
	{
		if (itemPosition == 0)
			this.startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
	}

	@Override
	public void onNavItemSelected(int itemPosition)
	{
		handleMenuItemSelected(itemPosition);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		//See if home button was pressed
		this.GoHome(item, this.getController());
		
		handleMenuItemSelected(item.getItemId());
		super.onOptionsItemSelected(item);
		return true;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		if (_contentFragment.getLogDate() != null)
			outState.putInt(getResources().getString(R.string.state_logdate), (int)_contentFragment.getLogDate().getSelectedItemId());

		super.onSaveInstanceState(outState);
	}
}
