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

import com.jjkeller.kmb.interfaces.IEditLogLocations.EditLogLocationsFragActions;
import com.jjkeller.kmb.interfaces.IEditLogLocations.EditLogLocationsFragControllerMethods;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.LogEntryController;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.proxydata.EldEventAdapterList;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;
import com.jjkeller.kmbui.R;

public class EditLogLocationsFrag extends BaseFragment{
	EditLogLocationsFragActions actionsListener;
	EditLogLocationsFragControllerMethods controlListener;
    private GridView _grid;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
        // Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.f_editloglocations, container, false);
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
		_grid = (GridView)v.findViewById(R.id.grdEditLogLocations);		
	}
	
	protected void loadControls() {
        EmployeeLogEldEvent[] _logEventList = controlListener.getMyController().getCurrentEmployeeLog().getEldEventList().getEldEventList(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus);
		_grid.setAdapter(new LogLocationsAdapter(getActivity(), R.layout.grdeditloglocations, _logEventList, controlListener.getMyController()));
	}

	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
        	actionsListener = (EditLogLocationsFragActions) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement EditLogLocationsFragActions");
        }
        
        try{
        	controlListener = (EditLogLocationsFragControllerMethods) activity;
        } catch (ClassCastException e) {
        	throw new ClassCastException(activity.toString() + " must implement EditLogLocationsFragControllerMethods");
        }
    }
	
	private class LogLocationsAdapter extends ArrayAdapter<EmployeeLogEldEvent> {

        private EmployeeLogEldEvent[] items;
        private Context _ctx;
        private LogEntryController _logEntryController;
        
        public LogLocationsAdapter(Context context, int textViewResourceId, EmployeeLogEldEvent[] items, LogEntryController logEntryController) {
                super(context, textViewResourceId, items);
                this.items = items;
                this._ctx = context;
                this._logEntryController = logEntryController;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
                View v = convertView;
                if (v == null) {
                    LayoutInflater li = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    v = li.inflate(R.layout.grdeditloglocations, null);
                }
                final EmployeeLogEldEvent logEvent = items[position];
                
                if (logEvent != null) {
					
                	TextView tvTime = (TextView)v.findViewById(R.id.tvTime);
                    tvTime.setText(DateUtility.createHomeTerminalTimeString(logEvent.getStartTime(), GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()));
                	
                	TextView tvStatus = (TextView)v.findViewById(R.id.tvStatus);
					tvStatus.setText(logEvent.getDutyStatusEnum().getString(getContext()));
                	
                	TextView tvLocation = (TextView)v.findViewById(R.id.tvLocation);
                	tvLocation.setText(logEvent.getLocation().ToLocationString());
                	
                	Button btn = (Button)v.findViewById(R.id.btnEditLogLocation);
                	
					btn.setOnClickListener (
							new OnClickListener() {
					        public void onClick(View v) {
					        	_logEntryController.PerformEditLogLocation(logEvent);
					        	actionsListener.handleEditLogLocationsClick(_ctx);
					        }
					    }); 
					
                }
                return v;
        }
	}
}
