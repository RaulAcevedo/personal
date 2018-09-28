package com.jjkeller.kmb;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.jjkeller.kmb.fragments.LeftNavFrag;
import com.jjkeller.kmb.fragments.RptDataUsageFrag;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmbapi.controller.DataUsageController;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.proxydata.DataUsageSummary;
import com.jjkeller.kmbui.R;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class RptDataUsage extends BaseActivity implements LeftNavFrag.OnNavItemSelectedListener, LeftNavFrag.ActivityMenuItemsListener
{
	private RptDataUsageFrag _contentFragment;

	private List<DataUsageSummary> _usageSummaryList;
	private ArrayList<Bundle> _displayArray;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.baselayout);

		loadContentFragment(new RptDataUsageFrag());

		// State handling is implemented differently specifically for this
		// report, due to issues seemingly caused by invoking the Asynchronous
		// FetchLocalDataTask when configuration changes occur.
		if (!this.loadStateData(savedInstanceState))
		{
			mFetchLocalDataTask = new FetchLocalDataTask(this.getClass().getSimpleName(), savedInstanceState);
			mFetchLocalDataTask.execute();
		}
		else
		{
			this.loadControls(savedInstanceState);
		}
	}

	private boolean loadStateData(Bundle savedInstanceState)
	{
		// This method should probably be implemented in activities that
		// save/restore state. Only activities know the specifics of what's
		// stored in the state object, therefore, they should validate that the
		// state object contains valid data.
		if (savedInstanceState != null && savedInstanceState.getParcelableArrayList("displayArray") != null)
			return true;
		else
			return false;

	}

	protected DataUsageController getMyController()
	{
		return (DataUsageController)this.getController();
	}

	@Override
	protected void InitController()
	{
		this.setController(new DataUsageController(this));
	}

	@Override
	protected void loadData()
	{
		_usageSummaryList = this.getMyController().getDataUsageForReport();
	}

	@Override
	public void setFragments()
	{
		super.setFragments();
		Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
		_contentFragment = (RptDataUsageFrag)fragment;
		this.loadGrid();
	}

	@Override
	protected void loadControls(Bundle savedInstanceState)
	{
		super.loadControls(savedInstanceState);

		if (savedInstanceState == null || savedInstanceState.getParcelableArrayList("displayArray") == null)
			this.buildDisplayList(_usageSummaryList);
		else
			_displayArray = savedInstanceState.getParcelableArrayList("displayArray");

		this.loadGrid();
	}

	private void loadGrid()
	{
		if (_displayArray != null && _contentFragment != null)
		{
			DataUsageAdapter adapter = new DataUsageAdapter(this, R.layout.grddatausage, _displayArray);
			_contentFragment.getGrid().setAdapter(adapter);
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
		return getString(R.string.rptdailyhours_actionitems);
	}

	private void handleMenuItemSelected(int itemPosition)
	{
		if (itemPosition == 0)
		{
			this.finish();
			this.startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
		}
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
		super.onSaveInstanceState(outState);
		outState.putParcelableArrayList("displayArray", _displayArray);
	}

	private void buildDisplayList(List<DataUsageSummary> usageSummaryList)
	{
		_displayArray = new ArrayList<Bundle>();

		Bundle displayBundle;
		DataUsageSummary usageSummary;

		for (int i = 0; i < usageSummaryList.size(); i++)
		{
			usageSummary = usageSummaryList.get(i);
			displayBundle = new Bundle();

			String date = DateUtility.getDateFormat().format(usageSummary.getUsageDate());
			displayBundle.putString("date", date);

			displayBundle.putString("sent", readableFileSize(usageSummary.getSentBytes()));
			displayBundle.putString("received", readableFileSize(usageSummary.getReceivedBytes()));

			_displayArray.add(displayBundle);
		}
	}

	private String readableFileSize(long size)
	{
		if (size <= 0)
			return "0";
		final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
		int digitGroups = (int)(Math.log10(size) / Math.log10(1024));
		return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
	}

	/**
	 * Adapter for the grid items
	 */
	private class DataUsageAdapter extends ArrayAdapter<Bundle>
	{
		private ArrayList<Bundle> items;

		public DataUsageAdapter(Context context, int textViewResourceId, ArrayList<Bundle> items)
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
				v = vi.inflate(R.layout.grddatausage, null);
			}

			Bundle displayBundle = items.get(position);

			TextView tvDate = (TextView)v.findViewById(R.id.gdu_tvDateValue);
			TextView tvDrive = (TextView)v.findViewById(R.id.gdu_tvSentValue);
			TextView tvDuty = (TextView)v.findViewById(R.id.gdu_tvReceivedValue);

			tvDate.setText(displayBundle.getString("date"));
			tvDrive.setText(displayBundle.getString("sent"));
			tvDuty.setText(displayBundle.getString("received"));

			return v;
		}
	}
}
