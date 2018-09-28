package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.jjkeller.kmb.interfaces.IViewLogRemarksEdit.CancelLogRemarksFragActions;
import com.jjkeller.kmb.interfaces.IViewLogRemarksEdit.CancelLogRemarksFragControllerMethods;
import com.jjkeller.kmb.interfaces.IViewLogRemarksEdit.SaveLogRemarksFragActions;
import com.jjkeller.kmb.interfaces.IViewLogRemarksEdit.SaveLogRemarksFragControllerMethods;
import com.jjkeller.kmb.interfaces.IViewLogRemarksEdit.SelectLogRemarksFragActions;
import com.jjkeller.kmb.interfaces.IViewLogRemarksEdit.SelectLogRemarksFragControllerMethods;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbapi.controller.LogRemarksController;
import com.jjkeller.kmbapi.controller.MotionPictureController;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;
import com.jjkeller.kmbapi.proxydata.LogRemarkItem;
import com.jjkeller.kmbapi.proxydata.MotionPictureAuthority;
import com.jjkeller.kmbapi.proxydata.MotionPictureProduction;
import com.jjkeller.kmbui.R;

import java.util.List;

public class ViewLogRemarksEditFrag extends BaseFragment{
	SaveLogRemarksFragActions actionsListener;
	SaveLogRemarksFragControllerMethods controlListener;
	CancelLogRemarksFragActions actionsListenerCancel;
	CancelLogRemarksFragControllerMethods controlListenerCancel;
	SelectLogRemarksFragActions actionsListenerSelect;
	SelectLogRemarksFragControllerMethods controlListenerSelect;
	private EmployeeLogEldEvent _logEvent;
	private TextView _tvRemark;
	Spinner _cboRemarks;
    ArrayAdapter<LogRemarkItem> remarksetAdapter;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
        // Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.f_viewlogremarksedit, container, false);
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
		_tvRemark = (TextView)v.findViewById(R.id.tvRemark);
		_cboRemarks = (Spinner)v.findViewById(R.id.cboRemark);
		
		_cboRemarks.setOnItemSelectedListener(
        		new AdapterView.OnItemSelectedListener() {
        		    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
        		    	actionsListenerSelect.handleRemarkSelect();
    			   }
					public void onNothingSelected(AdapterView<?> arg0) {
					}
	            });
	}
	
	protected void loadControls() {
		_logEvent = controlListener.getMyLogEntryController().getLogEventForEdit();
		View v = this.getView();
		if (v == null) {
			LayoutInflater li = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = li.inflate(R.layout.f_viewlogremarksedit, null);
		}

		if (_logEvent != null) {
			
			
			LogRemarksController logRemarksController = new LogRemarksController(getActivity());
			List<LogRemarkItem> logRemarks = logRemarksController.FetchAll();
			
			// Add the first default item
			LogRemarkItem item = new LogRemarkItem();
			if(_logEvent.getLogRemark() == null || _logEvent.getLogRemark().length() <= 0)
				item.setName("-Select Remark-");
			else
				item.setName("-Re-Select Remark-");
			
			logRemarks.add(0, item);

			remarksetAdapter = new ArrayAdapter<LogRemarkItem>(getActivity(),R.layout.kmb_spinner_item,logRemarks);
	        remarksetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        _cboRemarks.setAdapter(remarksetAdapter);

			TextView tvTime = (TextView) v.findViewById(R.id.tvTime);
			tvTime.setText(DateUtility.getHomeTerminalTime12HourFormat().format(_logEvent.getStartTime()));

			TextView tvStatus = (TextView) v.findViewById(R.id.tvStatus);
			tvStatus.setText(_logEvent.getDutyStatusEnum().toFriendlyName());

			TextView tvDuration = (TextView) v.findViewById(R.id.tvDuration);
			tvDuration.setText("");

			TextView tvLocation = (TextView) v.findViewById(R.id.tvLocation);
			tvLocation.setText(_logEvent.getLocation().ToLocationString());

			TextView tvRuleset = (TextView) v.findViewById(R.id.tvRuleset);
			tvRuleset.setText(_logEvent.getRulesetType().getString(this.getActivity()));

			// Implemented Motion Picture - DOT Authority and Production population
			MotionPictureController controller = new MotionPictureController(this.getActivity());
			TextView tvDotAuthority = (TextView) v.findViewById(R.id.tvDotAuthority);
			TextView tvProduction = (TextView) v.findViewById(R.id.tvProduction);
			TextView lblDotAuthority = (TextView) v.findViewById(R.id.lblDOTAuthority);
			TextView lblProduction = (TextView) v.findViewById(R.id.lblProduction);

			String authorityName = null;
			MotionPictureAuthority motionPictureAuthority = controller.GetAuthorityByAuthorityId(_logEvent.getMotionPictureAuthorityId());
			if(motionPictureAuthority != null){
				lblDotAuthority.setVisibility(View.VISIBLE);
				tvDotAuthority.setVisibility(View.VISIBLE);

				authorityName = motionPictureAuthority.GetNameAndDOTNumber();
				tvDotAuthority.setText(authorityName);
			} else{
				lblDotAuthority.setVisibility(View.GONE);
				tvDotAuthority.setVisibility(View.GONE);
			}

			String productionName = null;
			MotionPictureProduction motionPictureProduction = controller.GetProductionByProductionId(_logEvent.getMotionPictureProductionId());
			if(motionPictureProduction != null){
				lblProduction.setVisibility(View.VISIBLE);
				tvProduction.setVisibility(View.VISIBLE);

				productionName = motionPictureProduction.getName();
				tvProduction.setText(productionName);
			} else {
				lblProduction.setVisibility(View.GONE);
				tvProduction.setVisibility(View.GONE);
			}


			if(_logEvent.getLogRemark() != null)
				_tvRemark.setText(_logEvent.getLogRemark().toString());

			Button btnSave = (Button) v.findViewById(R.id.btnSave);
			final Button btnCancel = (Button) v.findViewById(R.id.btnCancel);

			btnSave.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					controlListener.getMyLogEntryController().PerformEditLogRemark(_logEvent);
					actionsListener.handleSaveLogRemarksClick(getActivity());
				}
			});

			btnCancel.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					actionsListenerCancel.handleCancelLogRemarksClick(getActivity());
				}
			});
		}
	}
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        
        try {
        	actionsListener = (SaveLogRemarksFragActions) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement SaveLogRemarksFragActions");
        }
        
        try{
        	controlListener = (SaveLogRemarksFragControllerMethods) activity;
        } catch (ClassCastException e) {
        	throw new ClassCastException(activity.toString() + " must implement SaveLogRemarksFragControllerMethods");
        }
        
        try {
        	actionsListenerCancel = (CancelLogRemarksFragActions) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement CancelLogRemarksFragActions");
        }
        
        try{
        	controlListenerCancel = (CancelLogRemarksFragControllerMethods) activity;
        } catch (ClassCastException e) {
        	throw new ClassCastException(activity.toString() + " must implement CancelLogRemarksFragControllerMethods");
        }
        
        try {
        	actionsListenerSelect = (SelectLogRemarksFragActions) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement SelectLogRemarksFragActions");
        }
        
        try{
        	controlListenerSelect = (SelectLogRemarksFragControllerMethods) activity;
        } catch (ClassCastException e) {
        	throw new ClassCastException(activity.toString() + " must implement SelectLogRemarksFragControllerMethods");
        }
    }
	
	public TextView getRemarkTextView(){
		
		if(_tvRemark == null)
			_tvRemark = (TextView)getView().findViewById(R.id.tvRemark);
		return _tvRemark;
	}
	
	public void setRemarkTextViewText(String remark){
		
		if(_tvRemark == null)
			_tvRemark = (TextView)getView().findViewById(R.id.tvRemark);
		
		_tvRemark.setText(remark);
	}
	
	public String GetSelectedRemark() 
	{
		if (remarksetAdapter == null || _cboRemarks == null)
			return "";
		else
			return remarksetAdapter.getItem(_cboRemarks.getSelectedItemPosition()).toString();

	}
}
