package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.TextView;

import com.jjkeller.kmb.UnassignedDrivingPeriods;
import com.jjkeller.kmb.interfaces.IUnassignedDrivingPeriods.UnassignedDrivingPeriodsFragActions;
import com.jjkeller.kmb.interfaces.IUnassignedDrivingPeriods.UnassignedDrivingPeriodsFragControllerMethods;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.EOBR.EobrReader;
import com.jjkeller.kmbapi.controller.GeotabController;
import com.jjkeller.kmbapi.controller.UnclaimedDrivingPeriod;
import com.jjkeller.kmbapi.controller.share.KmbApplicationException;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.controller.MotionPictureController;
import com.jjkeller.kmbapi.proxydata.UnassignedDrivingPeriod;
import com.jjkeller.kmbapi.proxydata.MotionPictureProduction;
import com.jjkeller.kmbui.R;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.jjkeller.kmbapi.kmbeobr.Constants.FROM_MENU_EXTRA;

public class UnassignedDrivingPeriodsFrag extends BaseFragment {
	UnassignedDrivingPeriodsFragActions actionsListener;
	UnassignedDrivingPeriodsFragControllerMethods controlListener;
	
	private GridView _gvUnassignedDrivingPeriods;
	private List<UnclaimedDrivingPeriod> _periodList;
	private Button _btnClaim;
	public boolean[] _checkedKeys;
	public String[] _selectedMotionPictureProductions;
	private boolean fromMenu;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		Bundle bundle = getArguments();
		if (bundle != null) {
			fromMenu = bundle.getBoolean(FROM_MENU_EXTRA, false);
		}
        // Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.f_unassigneddrivingperiods, container, false);
		findControls(v);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		this.loadControls(savedInstanceState);
	}
	
	private void loadControls(Bundle savedInstanceState){
		_periodList = new ArrayList<>();
		if(GlobalState.getInstance().getCompanyConfigSettings(getActivity()).getIsGeotabEnabled() && fromMenu) {
			GeotabController geotabController = new GeotabController(getActivity());
			downloadAsynchronously(geotabController, savedInstanceState, EobrReader.getInstance().getEobrSerialNumber());

		} else  {
			_periodList = controlListener.getMyController().GetUnclaimedDrivingPeriodsForCurrentLog();
			handleSavedInstanceAndSetAdapter(savedInstanceState);
		}
		//initialize _checkedKeys to false
		if(_checkedKeys == null){
			_checkedKeys = new boolean[_periodList.size()];
			Arrays.fill(_checkedKeys, Boolean.FALSE);
		}
		//check if claim button needs to be enabled
		enableDisableClaimBtn();
	}
	public void handleSavedInstanceAndSetAdapter(Bundle savedInstanceState) {
		if(savedInstanceState != null && savedInstanceState.containsKey("unassignedDrivingPeriodsItemsChecked")) {
			_checkedKeys = savedInstanceState.getBooleanArray("unassignedDrivingPeriodsItemsChecked");
			_selectedMotionPictureProductions = savedInstanceState.getStringArray("unassignedDrivingPeriodsProductions");
			for(int i = 0; i < _periodList.size(); i++) {
				_periodList.get(i).getUnassignedDrivingPeriod().setMotionPictureProductionId(_selectedMotionPictureProductions[i]);
			}
		}
		_gvUnassignedDrivingPeriods.setAdapter(new UnassignedDrivingPeriodsAdapter(getActivity(), R.layout.grdunassigneddrivingperiods , _periodList ));

	}
	public void downloadAsynchronously(final GeotabController geotabController, final Bundle savedInstanceState, final String eobrSerialNumber) {
		final ProgressDialog progressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.lbldownloading));

		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... voids) {
				try {
					if (eobrSerialNumber != null) {
						geotabController.DownloadUnassignedDrivingPeriodsForCurrentLog(eobrSerialNumber);
					}
				} catch (KmbApplicationException e) {
					Log.e("GeoTab", e.getMessage(), e);
				}

				return null;
			}

			@Override
			protected void onPostExecute(Void aVoid) {
				super.onPostExecute(aVoid);

				_periodList = controlListener.getMyController().GetUnclaimedDrivingPeriodsForCurrentLog();
				handleSavedInstanceAndSetAdapter(savedInstanceState);

				if (progressDialog.isShowing()) {
					progressDialog.dismiss();
				}
			}
		}.execute();
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState)
	{		
		if(_periodList != null && _periodList.size() > 0)
		{
			_selectedMotionPictureProductions = new String[_periodList.size()];
			for(int i = 0; i < _periodList.size(); i++)
			{
				UnclaimedDrivingPeriod udp = _periodList.get(i);
				_selectedMotionPictureProductions[i] = udp.getUnassignedDrivingPeriod().getMotionPictureProductionId();
			}		
			outState.putBooleanArray("unassignedDrivingPeriodsItemsChecked", _checkedKeys);
			outState.putStringArray("unassignedDrivingPeriodsProductions", _selectedMotionPictureProductions);
		}
		
		super.onSaveInstanceState(outState);		
	}
	
	protected void findControls(View v)
	{
		_gvUnassignedDrivingPeriods = (GridView)v.findViewById(R.id.upd_grdunassigneddrivingperiods);		
		_btnClaim = (Button)v.findViewById(R.id.btnClaim);
		_btnClaim.setOnClickListener(
				new OnClickListener() {
					
					public void onClick(View v) {
						actionsListener.handleClaimButtonClick();
					}
				});
	}
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
        	actionsListener = (UnassignedDrivingPeriodsFragActions) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement UnassignedDrivingPeriodsFragActions");
        }
        
        try{
        	controlListener = (UnassignedDrivingPeriodsFragControllerMethods) activity;
        } catch (ClassCastException e) {
        	throw new ClassCastException(activity.toString() + " must implement UnassignedDrivingPeriodsFragControllerMethods");
        }
    }
	
	public List<UnclaimedDrivingPeriod> getPeriodList(){
		return _periodList;
	}

	public class UnassignedDrivingPeriodsAdapter extends ArrayAdapter<UnclaimedDrivingPeriod>
	{
		public List<UnclaimedDrivingPeriod> items;

		public UnassignedDrivingPeriodsAdapter(Context context, 
				int textViewResourceId, List<UnclaimedDrivingPeriod> items) {
			super(context, textViewResourceId, items);
			this.items = items;
		}
		UnassignedDrivingPeriods udp = (UnassignedDrivingPeriods)getActivity();
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
        	View v = convertView;
        	if (v == null) {
        		getActivity();
				LayoutInflater li = (LayoutInflater)getActivity().getSystemService(FragmentActivity.LAYOUT_INFLATER_SERVICE);
        		v = li.inflate(R.layout.grdunassigneddrivingperiods, null, false);
        	}
        	final UnclaimedDrivingPeriod period = items.get(position);

        	if(period != null)
        	{        		
        		UnassignedDrivingPeriod upd = period.getUnassignedDrivingPeriod();
            	TextView tvStartTime = (TextView)v.findViewById(R.id.tvStart);
            	tvStartTime.setText(DateUtility.getHomeTerminalTime12HourFormat().format(upd.getStartTime()));
        		
            	TextView tvEndTime = (TextView)v.findViewById(R.id.tvEnd);
            	tvEndTime.setText(DateUtility.getHomeTerminalTime12HourFormat().format(upd.getStopTime()));

            	TextView tvDistance = (TextView)v.findViewById(R.id.tvDistance);
            	NumberFormat numberFormat = new DecimalFormat("#,###.00");
            	
            	// convert distance to kilometers if user is setup to display
            	// as kilometers
            	float distance = upd.getDistance();
            	
            	if (udp.getController().getCurrentUser().getDistanceUnits().equalsIgnoreCase(getActivity().getString(R.string.kilometers)))            			
            		distance = distance * GlobalState.MilesToKilometers;
            	
            	tvDistance.setText(numberFormat.format(distance));

            	TextView tvUnit = (TextView)v.findViewById(R.id.tvUnit);
            	tvUnit.setText(upd.getEobrId());

				if (GlobalState.getInstance().getCompanyConfigSettings(getActivity().getBaseContext()).getIsMotionPictureEnabled()){
					TextView lblProduction = (TextView)v.findViewById(R.id.lblProduction);
					Spinner cboProduction = (Spinner)v.findViewById(R.id.cboProduction);
					TextView lblAuthority = (TextView)v.findViewById(R.id.lblAuthority);
					final TextView txtAuthority = (TextView)v.findViewById(R.id.txtAuthority);
					lblProduction.setVisibility(View.VISIBLE);
					cboProduction.setVisibility(View.VISIBLE);
					lblAuthority.setVisibility(View.VISIBLE);
					txtAuthority.setVisibility(View.VISIBLE);

					MotionPictureController controller = new MotionPictureController(this.getContext());
					List<MotionPictureProduction> _motionPictureProductions = controller.GetActiveMotionPictureProductions();

					if (_motionPictureProductions.size() > 1){
						ArrayAdapter<MotionPictureProduction> spinnerAdapter = new ArrayAdapter<>(this.getContext(), R.layout.kmb_spinner_item, _motionPictureProductions);
						spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
						cboProduction.setAdapter(spinnerAdapter);

						if (period.getUnassignedDrivingPeriod().getMotionPictureProductionId() != null) {
							for (int i = 0; i < spinnerAdapter.getCount(); i++) {
								if (period.getUnassignedDrivingPeriod().getMotionPictureProductionId().compareTo(spinnerAdapter.getItem(i).getMotionPictureProductionId()) == 0) {
									cboProduction.setSelection(i);
									period.getUnassignedDrivingPeriod().setMotionPictureProductionId(spinnerAdapter.getItem(i).getMotionPictureProductionId());
									period.getUnassignedDrivingPeriod().setMotionPictureAuthorityId(spinnerAdapter.getItem(i).getMotionPictureAuthorityId());
									txtAuthority.setText(spinnerAdapter.getItem(i).getMotionPictureAuthority().getName());
								}
							}
						}
					}

					cboProduction.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
						@Override
						public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
							MotionPictureProduction production = (MotionPictureProduction)adapterView.getItemAtPosition(i);
							period.getUnassignedDrivingPeriod().setMotionPictureProductionId(production.getMotionPictureProductionId());
							period.getUnassignedDrivingPeriod().setMotionPictureAuthorityId(production.getMotionPictureAuthorityId());
							txtAuthority.setText(production.getMotionPictureAuthority().getName());
						}

						@Override
						public void onNothingSelected(AdapterView<?> adapterView) {

						}
					});
				}

            	final CheckBox chkClaim = (CheckBox)v.findViewById(R.id.chkClaim);
            	
            	if(_checkedKeys != null && _checkedKeys.length > position){
            		// when there is saved instance state, then setup the checkbox and and unclaimed period accordingly
            		boolean isClaimed = _checkedKeys[position];
            		chkClaim.setChecked(isClaimed);
            		period.setIsClaimed(isClaimed);
            	}

            	chkClaim.setOnClickListener(
            			new OnClickListener() {
            				public void onClick(View v) {
            					period.setIsClaimed(chkClaim.isChecked());
								//change the selection in the _checkedKeys
								if(_checkedKeys != null && _checkedKeys.length > position){
									_checkedKeys[position] = chkClaim.isChecked();
								}
								//check if claim button needs to be enabled
								enableDisableClaimBtn();
            				}
            			});
        	}
        	return v;
        }
	}

	/**
	 * Check if there is any position in _checkedKeys checked to enable or disable the _btnClaim
	 */
	private void enableDisableClaimBtn(){
		Boolean flag = false;
		for(Boolean value : _checkedKeys) {
			if (value) {
				flag = true;
			}
		}
		if(flag){
			_btnClaim.setEnabled(true);
		}else{
			_btnClaim.setEnabled(false);
		}
	}
}
