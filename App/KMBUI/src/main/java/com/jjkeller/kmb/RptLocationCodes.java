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
import com.jjkeller.kmb.fragments.RptLocationCodesFrag;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.interfaces.IAPIController;
import com.jjkeller.kmbapi.controller.share.MandateObjectFactory;
import com.jjkeller.kmbapi.proxydata.LocationCode;
import com.jjkeller.kmbui.R;

import java.util.List;

public class RptLocationCodes extends BaseActivity implements LeftNavFrag.OnNavItemSelectedListener, LeftNavFrag.ActivityMenuItemsListener
{
	private RptLocationCodesFrag _contentFragment;
	private List<LocationCode> _locationList;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.baselayout);

		loadContentFragment(new RptLocationCodesFrag());

		mFetchLocalDataTask = new FetchLocalDataTask(this.getClass().getSimpleName());
		mFetchLocalDataTask.execute();
	}

	@Override
	protected void loadData()
	{
		_locationList = this.getMyController().getLocationCodeListForReport();
	}

	@Override
	protected void loadControls()
	{
		super.loadControls();
		this.loadGrid();
	}

	@Override
	public void setFragments()
	{
		super.setFragments();
		Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
		_contentFragment = (RptLocationCodesFrag)fragment;
		this.loadGrid();
	}

	private void loadGrid()
	{
		if (_locationList != null && _contentFragment != null)
		{
			LocationCodeAdapter adapter = new LocationCodeAdapter(this, R.layout.grdlocationcodes, _locationList);
			_contentFragment.getGrid().setAdapter(adapter);
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

	private class LocationCodeAdapter extends ArrayAdapter<LocationCode>
	{
		private List<LocationCode> items;

		public LocationCodeAdapter(Context context, int textViewResourceId, List<LocationCode> items)
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
				v = vi.inflate(R.layout.grdlocationcodes, null);
			}
			LocationCode locationCode = items.get(position);

			if (locationCode != null)
			{
				TextView tvlocationCode = (TextView)v.findViewById(R.id.tvLocationCode);
				tvlocationCode.setText(locationCode.getCode());

				TextView tvlocation = (TextView)v.findViewById(R.id.tvLocation);
				tvlocation.setText(locationCode.getLocation());

			}
			return v;
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
}
