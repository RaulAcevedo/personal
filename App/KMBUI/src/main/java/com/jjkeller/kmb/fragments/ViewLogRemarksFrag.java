package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.jjkeller.kmb.interfaces.IViewLogRemarks.DeleteLogRemarksFragActions;
import com.jjkeller.kmb.interfaces.IViewLogRemarks.DeleteLogRemarksFragControllerMethods;
import com.jjkeller.kmb.interfaces.IViewLogRemarks.EditLogRemarksFragActions;
import com.jjkeller.kmb.interfaces.IViewLogRemarks.EditLogRemarksFragControllerMethods;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.LogEntryController;
import com.jjkeller.kmbapi.controller.MotionPictureController;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.enums.DutyStatusEnum;
import com.jjkeller.kmbapi.proxydata.EldEventAdapterList;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;
import com.jjkeller.kmbapi.proxydata.MotionPictureAuthority;
import com.jjkeller.kmbapi.proxydata.MotionPictureProduction;
import com.jjkeller.kmbui.R;

import org.joda.time.Duration;

import java.util.Date;

public class ViewLogRemarksFrag extends BaseFragment{
	EditLogRemarksFragActions actionsListener;
	EditLogRemarksFragControllerMethods controlListener;
	DeleteLogRemarksFragActions actionsListenerDelete;
	DeleteLogRemarksFragControllerMethods controlListenerDelete;
	private EmployeeLogEldEvent[] _logEventList;
	private GridView _grid;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
        // Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.f_viewlogremarks, container, false);
		findControls(v);
		return v;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		this.loadControls();
	}
	
	protected void findControls(View v) {		
		_grid = (GridView)v.findViewById(R.id.grdViewLogRemarks);		
	}
	
	protected void loadControls() {
		_logEventList = controlListener.getMyLogEntryController().getCurrentEmployeeLog().getEldEventList().getActiveEldEventList(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus);
		_grid.setAdapter(new LogRemarksAdapter(getActivity(), R.layout.grdviewlogremarks, _logEventList, controlListener.getMyLogEntryController()));
	}
	
	public GridView getViewLogRemarksGridView(){
		if(_grid == null){
			_grid = (GridView)getView().findViewById(R.id.grdViewLogRemarks);
		}
		return _grid;
	}
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
        	actionsListener = (EditLogRemarksFragActions) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement EditLogRemarksFragActions");
        }
        
        try{
        	controlListener = (EditLogRemarksFragControllerMethods) activity;
        } catch (ClassCastException e) {
        	throw new ClassCastException(activity.toString() + " must implement EditLogRemarksFragControllerMethods");
        }
        
        try {
        	actionsListenerDelete = (DeleteLogRemarksFragActions) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement DeleteLogRemarksFragActions");
        }
        
        try{
        	controlListenerDelete = (DeleteLogRemarksFragControllerMethods) activity;
        } catch (ClassCastException e) {
        	throw new ClassCastException(activity.toString() + " DeleteLogRemarksFragControllerMethods");
        }
    }
	
	private class LogRemarksAdapter extends ArrayAdapter<EmployeeLogEldEvent> {

        private EmployeeLogEldEvent[] items;
        private Context _ctx;
        private LogEntryController _logEntryController;
        
        public LogRemarksAdapter(Context context, int textViewResourceId, EmployeeLogEldEvent[] items, LogEntryController logEntryController) {
                super(context, textViewResourceId, items);
                this.items = items;
                this._ctx = context;
                this._logEntryController = logEntryController;
        }

        @Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater li = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = li.inflate(R.layout.grdviewlogremarks, null);
			}
			final EmployeeLogEldEvent logEvent = items[position];

			String durationStr = "";

			if (position < items.length - 1)
			{
				EmployeeLogEldEvent nextEvt = items[position + 1];
				long duration = nextEvt.getStartTime().getTime() - logEvent.getStartTime().getTime();
				durationStr = DateUtility.createTimeDurationString(duration, GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled());
			}

			if (logEvent != null) {

				TextView tvTime = (TextView) v.findViewById(R.id.tvTime);
				tvTime.setText(DateUtility.createHomeTerminalTimeString(logEvent.getStartTime(), GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()));

				TextView tvStatus = (TextView) v.findViewById(R.id.tvStatus);
				DutyStatusEnum statusEnum =logEvent.getDutyStatusEnum();
				tvStatus.setText(statusEnum.toFriendlyName());

				TextView tvDuration = (TextView) v.findViewById(R.id.tvDuration);
				tvDuration.setText(durationStr);

				TextView tvLocation = (TextView) v.findViewById(R.id.tvLocation);
				tvLocation.setText(logEvent.getLocation().ToLocationString());

				TextView tvRuleset = (TextView) v.findViewById(R.id.tvRuleset);
				tvRuleset.setText(logEvent.getRulesetType().getString(getContext()));

				MotionPictureController controller = new MotionPictureController(getContext());
				TextView tvDotAuthority = (TextView) v.findViewById(R.id.tvDotAuthority);
				String authorityName = null;
				MotionPictureAuthority motionPictureAuthority = controller.GetAuthorityByAuthorityId(logEvent.getMotionPictureAuthorityId());
				if(motionPictureAuthority != null){
					authorityName = motionPictureAuthority.GetNameAndDOTNumber();
				}
				tvDotAuthority.setText(authorityName);

				TextView tvProduction = (TextView) v.findViewById(R.id.tvProduction);
				String productionName = null;
				MotionPictureProduction motionPictureProduction = controller.GetProductionByProductionId(logEvent.getMotionPictureProductionId());
				if(motionPictureProduction != null){
					productionName = motionPictureProduction.getName();
				}
				tvProduction.setText(productionName);

				Button btnAdd = (Button) v.findViewById(R.id.btnAddLogRemarks);
				btnAdd.setOnClickListener(
						new OnClickListener() {
							public void onClick(View v) {
								_logEntryController.PerformEditLogRemark(logEvent);
								actionsListener.handleEditLogRemarksClick(_ctx);
							}
						});

				Button btnEdit = (Button) v.findViewById(R.id.btnEditLogRemarks);
				btnEdit.setOnClickListener(
						new OnClickListener() {
							public void onClick(View v) {
								_logEntryController.PerformEditLogRemark(logEvent);
								actionsListener.handleEditLogRemarksClick(_ctx);
							}
						});

				Button btnDelete = (Button) v.findViewById(R.id.btnDeleteLogRemarks);
				btnDelete.setOnClickListener(
						new OnClickListener() {
							public void onClick(View v) {
								_logEntryController.PerformEditLogRemark(logEvent);
								actionsListenerDelete.handleDeleteLogRemarksClick(_ctx);
							}
						});

				// If the log has remarks, then display the remarks section
				TextView lblRemark = (TextView) v.findViewById(R.id.lblRemark);
				TextView tvRemark = (TextView) v.findViewById(R.id.tvRemark);
				Date lastSubmitted = controlListener.getMyLogEntryController().getCurrentUser().getCredentials().getLastSubmitTimestampUtc();
				Date logEventTime = logEvent.getStartTime();
				if (lastSubmitted == null || logEventTime.compareTo(lastSubmitted) >= 0) {
					if (logEvent.getLogRemark() != null && logEvent.getLogRemark().length() > 0) {
						lblRemark.setVisibility(View.VISIBLE);
						tvRemark.setVisibility(View.VISIBLE);
						btnEdit.setVisibility(View.VISIBLE);
						btnDelete.setVisibility(View.VISIBLE);
						btnAdd.setVisibility(View.GONE);
						tvRemark.setText(logEvent.getLogRemark());
					} else {
						lblRemark.setVisibility(View.GONE);
						tvRemark.setVisibility(View.GONE);
						btnEdit.setVisibility(View.GONE);
						btnDelete.setVisibility(View.GONE);
						btnAdd.setVisibility(View.VISIBLE);
					}
				} else {
					btnEdit.setVisibility(View.GONE);
					btnDelete.setVisibility(View.GONE);
					btnAdd.setVisibility(View.GONE);
					if (logEvent.getLogRemark() != null && logEvent.getLogRemark().length() > 0) {
						lblRemark.setVisibility(View.VISIBLE);
						tvRemark.setVisibility(View.VISIBLE);
						tvRemark.setText(logEvent.getLogRemark());
					} else {
						lblRemark.setVisibility(View.GONE);
						tvRemark.setVisibility(View.GONE);
					}
				}

				TextView lblDotAuthority = (TextView) v.findViewById(R.id.lblDOTAuthority);
				TextView lblProduction = (TextView) v.findViewById(R.id.lblProduction);
				if (GlobalState.getInstance().getCompanyConfigSettings(getActivity().getBaseContext()).getIsMotionPictureEnabled()) {
					lblDotAuthority.setVisibility(View.VISIBLE);
					tvDotAuthority.setVisibility(View.VISIBLE);
					lblProduction.setVisibility(View.VISIBLE);
					tvProduction.setVisibility(View.VISIBLE);
				} else {
					lblDotAuthority.setVisibility(View.GONE);
					tvDotAuthority.setVisibility(View.GONE);
					lblProduction.setVisibility(View.GONE);
					tvProduction.setVisibility(View.GONE);
				}
				
				// Do not allow Add/Edit/Delete functions when in RSI
				boolean isRoadSideInspection = GlobalState.getInstance().getRoadsideInspectionMode();
				if (isRoadSideInspection)
				{
					btnEdit.setVisibility(View.GONE);
					btnDelete.setVisibility(View.GONE);
					btnAdd.setVisibility(View.GONE);
				}
			}
			return v;
		}
	}
}
