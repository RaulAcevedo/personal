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
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.TextView;

import com.jjkeller.kmb.interfaces.IDownloadLogs.DownloadLogsFragActions;
import com.jjkeller.kmb.interfaces.IDownloadLogs.DownloadLogsFragControllerMethods;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbapi.controller.abstracts.APIControllerBase;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbui.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DownloadLogsFrag extends BaseFragment {
	DownloadLogsFragActions actionsListener;
	DownloadLogsFragControllerMethods controlListener;
	
	private List<Date> _missingDateList;
	private GridView _grid;
	
	private TextView _tvMessage;
	private Button _btnDownload;
	
	public List<String> _offDutyLogList = new ArrayList<String>();
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
        // Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.f_downloadlogs, container, false);
		findControls(v);
		return v;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		this.loadControls(savedInstanceState);
	}
	
	protected void findControls(View v)
	{	
		_btnDownload = (Button)v.findViewById(R.id.btnDLDownload);
		_btnDownload.setOnClickListener(
	            new OnClickListener() {
	            	public void onClick(View v) {
	            		actionsListener.HandleDownloadClick(v);
	            	}
	            });

		v.findViewById(R.id.btnDLDone).setOnClickListener(
				new OnClickListener() {
	            	public void onClick(View v) {
	            		actionsListener.HandleDoneClick();
	            	}
	            });
		
		_grid =(GridView)v.findViewById(R.id.grdMissingLogDates);
		_tvMessage = (TextView)v.findViewById(R.id.tvMessage);		
	}
	
	protected void loadControls(Bundle savedInstanceState) {
		_missingDateList = controlListener.getMyController().GetMissingLogDateList(((APIControllerBase)controlListener.getMyController()).getCurrentUser());
		if (!((APIControllerBase)controlListener.getMyController()).getIsNetworkAvailable())
			_btnDownload.setEnabled(false);
		
		if(savedInstanceState != null && savedInstanceState.containsKey("offDutyLogs"))
        {
			for(int i = 0; i < savedInstanceState.getStringArray("offDutyLogs").length; i++)
			{
				_offDutyLogList.add(i, savedInstanceState.getStringArray("offDutyLogs")[i]);
			}
        }
		
		if(getActivity() != null)
		{
			LogDateAdapter logDateAdapter = new LogDateAdapter(getActivity(), R.id.chkLogDate, _missingDateList);
			_grid.setAdapter(logDateAdapter);
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState)
	{
    	// 7/10/12 ACM - Save each checked checkbox in state bundle
		String[] checkedList = new String[_offDutyLogList.size()];
		for(int i = 0; i < checkedList.length; i++)
		{
			checkedList[i] = _offDutyLogList.get(i);
		}		
		outState.putStringArray("offDutyLogs", checkedList);	
		
		super.onSaveInstanceState(outState);		
	}
	
	public GridView getGrid()
	{
		if(_grid == null)
			_grid = (GridView)getView().findViewById(R.id.grdMissingLogDates);
		return _grid;
	}
	
	public Button getDownloadButton(){
		if(_btnDownload == null)
			_btnDownload = (Button)getView().findViewById(R.id.btnDLDownload);
		return _btnDownload;
	}
	
	public GridView getDownloadLogsGridView(){
		if(_grid == null){
			_grid = (GridView)getView().findViewById(R.id.grdMissingLogDates);
		}
		return _grid;
	}
	
	public TextView getMessageTextView(){
		if(_tvMessage == null)
			_tvMessage = (TextView)getView().findViewById(R.id.tvMessage);
		return _tvMessage;
	}
	
	public void Reload()
	{
		loadControls(null);
	}
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
        	actionsListener = (DownloadLogsFragActions) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement DownloadLogsFragActions");
        }
        
        try{
        	controlListener = (DownloadLogsFragControllerMethods) activity;
        } catch (ClassCastException e) {
        	throw new ClassCastException(activity.toString() + " must implement DownloadLogsFragControllerMethods");
        }
    }
	
	public class LogDateAdapter extends ArrayAdapter<Date> implements OnClickListener {
		private List<Date> items;
		public static final int ACTIVITY_CREATE = 10;
		public LogDateAdapter(Context c, int textViewResourceId, List<Date> items){
			super(c, textViewResourceId, items);
			this.items = items;
		}
		
		public int getCount() {
			if (items != null)
				return items.size();
			else
				return 0;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View v;

			LayoutInflater li = getActivity().getLayoutInflater();
			v = li.inflate(R.layout.downloadlogsrow, null);
			v.setId(position);
			
			GridView grid = (GridView)parent; 
	        grid.setFocusable(false); 
	        grid.setFocusableInTouchMode(false); 

			if (items != null)
			{
				CheckBox cb = (CheckBox)v.findViewById(R.id.chkLogDate);
				cb.setText(DateUtility.getDateFormat().format(items.get(position)));	
				
				if(_offDutyLogList != null && _offDutyLogList.contains(cb.getText().toString()))
					cb.setChecked(true);
				else
					cb.setChecked(false);
				
				cb.setOnClickListener(this);
			}

			return v;
		}
		
		public long getItemId(int position) {
			return 0;
		}

		public void onClick(View arg0) {
			CheckBox cb = (CheckBox)arg0;
			
			if (cb.isChecked())
			{
				if (_offDutyLogList == null)
				{
					_offDutyLogList = new ArrayList<String>();
					_offDutyLogList.add(cb.getText().toString());
				}
				else
				{
					if (!_offDutyLogList.contains(cb.getText().toString()))
					{
						_offDutyLogList.add(cb.getText().toString());
					}
				}												
			}
			else
			{
				if (_offDutyLogList != null)
				{
					if (_offDutyLogList.contains(cb.getText().toString()))
					{
						_offDutyLogList.remove(cb.getText().toString());
					}
				}
			}
		}
	}
}
