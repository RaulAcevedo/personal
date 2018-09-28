package com.jjkeller.kmb;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.jjkeller.kmb.fragments.LeftNavFrag;
import com.jjkeller.kmb.fragments.RptDutyStatusFrag;
import com.jjkeller.kmb.share.OffDutyBaseActivity;
import com.jjkeller.kmb.share.ViewOnlyModeNavHandler;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.MotionPictureController;
import com.jjkeller.kmbapi.controller.calcengine.LogEventSummary;
import com.jjkeller.kmbapi.controller.interfaces.IAPIController;
import com.jjkeller.kmbapi.controller.share.MandateObjectFactory;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.enums.DutyStatusEnum;
import com.jjkeller.kmbapi.enums.ExemptLogTypeEnum;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.proxydata.MotionPictureAuthority;
import com.jjkeller.kmbapi.proxydata.MotionPictureProduction;
import com.jjkeller.kmbui.R;

import org.joda.time.Duration;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class RptDutyStatus extends OffDutyBaseActivity implements AdapterView.OnItemSelectedListener, LeftNavFrag.OnNavItemSelectedListener, LeftNavFrag.ActivityMenuItemsListener
{
	private ViewOnlyModeNavHandler _viewOnlyHandler;
	private RptDutyStatusFrag _contentFragment;

	protected static final int DATE_DIALOG_ID = 0;
	
	private List<Date> _empLogDateList;
	private List<LogEventSummary> _eventList;
	private EmployeeLog _empLog;
	private boolean _loading = false;
	private String _selectedItem = "";
	private Date _logDate;
	private LoadSelectedLogTask _loadSelectedLogTask;

	private int _myIndex;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		_viewOnlyHandler = new ViewOnlyModeNavHandler(this);
		_viewOnlyHandler.setCurrentActivity(ViewOnlyModeNavHandler.ViewOnlyModeActivity.SUMMARY);
		
		_loading = true;

		setContentView(R.layout.rptdutystatus);

		loadContentFragment(new RptDutyStatusFrag());
		
		if(_viewOnlyHandler.getIsViewOnlyMode())
			_myIndex = _viewOnlyHandler.getCurrentActivity().index();
		else
			_myIndex = 1;
		
		// Used for handling highlighting the selected item in the leftnav
		// If not using multiple fragments within an activity, we have to manually set the selected item
		this.setLeftNavSelectedItem(_myIndex);
		this.setLeftNavAllowChange(true);

		mFetchLocalDataTask = new FetchLocalDataTask(this.getClass().getSimpleName(), savedInstanceState);
		mFetchLocalDataTask.setAutoUnlockScreenRotationOnPostExecute(false);
		mFetchLocalDataTask.execute();
	}

	@Override
	public void onResume()
	{
		this.setLeftNavSelectedItem(_myIndex);
		loadLeftNavFragment();
		
		super.onResume();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if(GlobalState.getInstance().getIsViewOnlyMode())
		{
			//disable the back button in view only mode
			if (keyCode == KeyEvent.KEYCODE_BACK)
			{
			    return true;
			}
		}
		
		return super.onKeyDown(keyCode, event);
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
			logDateArray[index] = DateUtility.getHomeTerminalDateFormat().format(logDate);
		}

		ArrayAdapter<String> logDateAdapter = new ArrayAdapter<String>(this, R.layout.kmb_spinner_item, logDateArray);
		logDateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		_contentFragment.getLogDateSpinner().setAdapter(logDateAdapter);
		_contentFragment.getLogDateSpinner().setOnItemSelectedListener(this);

		if (savedInstanceState != null)
		{	
			_contentFragment.getLogDateSpinner().setSelection(savedInstanceState.getInt(getResources().getString(R.string.state_logdate)));			
		}else{
			EmployeeLog empLog = getMyController().getSelectedLogForReport();
			if(empLog != null){
				_contentFragment.setLogDateSpinner(DateUtility.getHomeTerminalDateFormat().format(empLog.getLogDate()));
			}
		}
				
		_selectedItem = _contentFragment.getLogDateSpinner().getSelectedItem().toString();

		Date selectedDate = null;
		try
		{
			selectedDate = DateUtility.getHomeTerminalDateFormat().parse(_selectedItem);
		}
		catch (ParseException e)
		{
			
        	Log.e("UnhandledCatch", e.getMessage() + ": " + Log.getStackTraceString(e));
		}

		this.findSelectedLog(selectedDate);

		this.loadGrid();
	}

	@Override
	public void setFragments()
	{
		super.setFragments();
		Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
		_contentFragment = (RptDutyStatusFrag)fragment;
	}

	private void findSelectedLog(Date selectedDate)
	{
		_logDate = selectedDate;

		_loadSelectedLogTask = new LoadSelectedLogTask();
		_loadSelectedLogTask.execute(new Void[0]);
	}

	private void loadSelectedLog()
	{
		_empLog = getMyController().EmployeeLogsForDutyStatusReport(_logDate).get(0);
		getMyController().setSelectedLogForReport(_empLog);
		_eventList = getMyController().LogEventSummaryFor(_empLog);
	}

	public void onItemSelected(AdapterView<?> parent, View v, int position, long id)
	{
		if (_loading == true)
			_loading = false;
		else
		{
			try
			{
				this.findSelectedLog(DateUtility.getHomeTerminalDateFormat().parse(_contentFragment.getLogDateSpinner().getSelectedItem().toString()));
			}
			catch (ParseException ex)
			{ 
				
	        	Log.e("UnhandledCatch", ex.getMessage() + ": " + Log.getStackTraceString(ex));
			}

			this.loadGrid();
		}

	}

	public void onNothingSelected(AdapterView<?> parent)
	{
	}

	private void loadGrid()
	{
		if (_empLog != null && _contentFragment != null)
		{
			ArrayList<LogEventSummary> eventList = new ArrayList<LogEventSummary>();

			// put the log date in an array for the spinner control.
			for (int index = 0; index < _eventList.size(); index++)
			{
				eventList.add(_eventList.get(index));
			}

			LogEventAdapter adapter = new LogEventAdapter(this, R.layout.grddutystatus, eventList);
			_contentFragment.getGrid().setAdapter(adapter);
			_contentFragment.setExemptLabel(_empLog.getExemptLogType().getValue() != ExemptLogTypeEnum.NULL);
		}
	}

	protected IAPIController getMyController()
	{
		return (IAPIController) this.getController();
	}

	protected void InitController()
	{
		this.setController(MandateObjectFactory.getInstance(this, GlobalState.getInstance().getFeatureService()).getCurrentEventController());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		this.CreateOptionsMenu(menu, false);
        if (GlobalState.getInstance().isReviewEldEvent())
            return false;
		return true;
	}

	@Override
	public String getActivityMenuItemList()
	{
		return _viewOnlyHandler.getActivityMenuItemList(getString(R.string.rptdutystatus_actionitems_tablet));	
	}
	
	private void handleMenuItemSelected(int itemPosition)
	{				
		if(_viewOnlyHandler.getIsViewOnlyMode())
		{
			Intent intent = _viewOnlyHandler.handleMenuItemSelected(itemPosition);
			
			if(intent != null)
			{
				intent.putExtra(getResources().getString(R.string.state_logdate), _contentFragment.getLogDateSpinner().getSelectedItem().toString());
				intent.putExtra(getResources().getString(R.string.state_keepdate), true);
				
				this.finish();
                this.startActivity(intent);
			}
		}
		else
		{
			switch (itemPosition)
			{
				case 0:
					Bundle extras = new Bundle();
					extras.putString(getResources().getString(R.string.state_logdate), _contentFragment.getLogDateSpinner().getSelectedItem().toString());
					extras.putBoolean(getResources().getString(R.string.state_keepdate), true);
					this.startActivity(RptGridImage.class, extras);
                    finish();
					break;
				case 1:	
					// DO NOTHING
					//ClearRecentlyStartedActivityUri();
					//this.startActivity(RptDutyStatus.class);
					break;
				case 2:
					// 1/17/12 JHM - This is done to fix a defect navigating to Duty
					// Status Detail after it was just viewed.
					ClearRecentlyStartedActivityUri();
					this.startActivity(RptLogDetail.class);
                    finish();
					break;
				case 3:
					this.finish();
                    if (GlobalState.getInstance().isReviewEldEvent())
                        this.startActivity(EditLogRequest.class);
                    else
					    this.startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    GlobalState.getInstance().setIsReviewEldEvent(false);
					break;
			}
		}
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
	public void onNavItemSelected(int itemPosition)
	{
		handleMenuItemSelected(itemPosition);
	}

	private class LogEventAdapter extends ArrayAdapter<LogEventSummary>
	{
		private ArrayList<LogEventSummary> items;
		private Context _ctx;
		private SimpleDateFormat _durationFormat = new SimpleDateFormat("H:mm");

		public LogEventAdapter(Context context, int textViewResourceId, ArrayList<LogEventSummary> items)
		{
			super(context, textViewResourceId, items);
			this.items = items;
			this._ctx = context;
			_durationFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{

            boolean isMandateEnabled = GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled();
			View v = convertView;
			if (v == null)
			{
				LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.grddutystatus, null);
			}
			LogEventSummary eventSummary = items.get(position);

			if (eventSummary != null)
			{
				String time = DateUtility.createHomeTerminalTimeString(eventSummary.getStartTime(), isMandateEnabled);
				TextView tvTime = (TextView)v.findViewById(R.id.tvTime);
				tvTime.setText(time);

				TextView tvStatus = (TextView)v.findViewById(R.id.tvStatus);
				DutyStatusEnum statusEnum =eventSummary.getDutyStatusEnum();
				tvStatus.setText(statusEnum.toFriendlyName());
				String duration = "";


				if (eventSummary.getDuration() >= DateUtility.MILLISECONDS_PER_DAY)
				{
					duration = "24:00";
				}
				else if (eventSummary.getDuration() < 0 )
				{
					duration = "-";
				}
				else
				{
					long durationInterval = eventSummary.getDuration();
					duration = DateUtility.createTimeDurationString(durationInterval, isMandateEnabled);
				}

				TextView tvDuration = (TextView)v.findViewById(R.id.tvDuration);
				tvDuration.setText(duration);

				String loc = eventSummary.getLocation();
				TextView tvLocation = (TextView)v.findViewById(R.id.tvLocation);
				tvLocation.setText(loc);

				String ruleSet = eventSummary.getRuleSetTypeEnum().getString(getContext());
				TextView tvRuleset = (TextView)v.findViewById(R.id.tvRuleset);
				tvRuleset.setText(ruleSet);

				MotionPictureController controller = new MotionPictureController(getContext());
				TextView tvDotAuthority = (TextView) v.findViewById(R.id.tvDotAuthority);
				TextView lblDotAuthority = (TextView) v.findViewById(R.id.lblDOTAuthority);
				String authorityName = null;
				MotionPictureAuthority motionPictureAuthority = controller.GetAuthorityByAuthorityId(eventSummary.getMotionPictureAuthorityId());
				if(motionPictureAuthority != null){
					authorityName = motionPictureAuthority.GetNameAndDOTNumber();
					tvDotAuthority.setText(authorityName);
					lblDotAuthority.setVisibility(View.VISIBLE);
					tvDotAuthority.setVisibility(View.VISIBLE);
				}else {
					lblDotAuthority.setVisibility(View.GONE);
					tvDotAuthority.setVisibility(View.GONE);
				}


				TextView tvProduction = (TextView) v.findViewById(R.id.tvProduction);
				TextView lblProduction = (TextView) v.findViewById(R.id.lblProduction);
				String productionName = null;
				MotionPictureProduction motionPictureProduction = controller.GetProductionByProductionId(eventSummary.getMotionPictureProductionId());
				if(motionPictureProduction != null){
					productionName = motionPictureProduction.getName();
					tvProduction.setText(productionName);
					lblProduction.setVisibility(View.VISIBLE);
					tvProduction.setVisibility(View.VISIBLE);
				}else {
					lblProduction.setVisibility(View.GONE);
					tvProduction.setVisibility(View.GONE);
				}

				String remarks = eventSummary.getRemarks();
				TextView tvRemarks = (TextView)v.findViewById(R.id.tvRemarks);
				tvRemarks.setText(remarks);

			}
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
			if(!RptDutyStatus.this.isFinishing())
				_pd = ProgressDialog.show(RptDutyStatus.this, "", getString(R.string.msgretreiving));
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
	protected void onSaveInstanceState(Bundle outState)
	{
		if (_contentFragment.getLogDateSpinner() != null)
			outState.putInt(getResources().getString(R.string.state_logdate), (int)_contentFragment.getLogDateSpinner().getSelectedItemId());
		super.onSaveInstanceState(outState);
	}


}
