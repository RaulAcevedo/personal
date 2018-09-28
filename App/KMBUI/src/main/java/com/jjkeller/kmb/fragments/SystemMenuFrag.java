package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.jjkeller.kmb.interfaces.ISystemMenu.SystemMenuFragControllerMethods;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.AppUpdateFactory;
import com.jjkeller.kmbapi.controller.EOBR.EobrReader;
import com.jjkeller.kmbapi.enums.DataProfileEnum;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbui.R;

import java.util.ArrayList;

public class SystemMenuFrag extends BaseFragment {
	SystemMenuFragControllerMethods controlListener;
	private TextView _tvTitle;
    private GridView _grid;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
        // Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.f_systemmenu, container, false);
		findControls(v);
		return v;
	}
    
    @Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		this.loadControls();
	}
    
	protected void findControls(View v)
    {
		_tvTitle = (TextView)v.findViewById((R.id.tvTitle));
        _grid=(GridView)v.findViewById(R.id.grdMenu);
    }
	
	protected void loadControls()
    {
        Bundle bundle = getActivity().getIntent().getExtras();
        String title = bundle.getString(getActivity().getString(R.string.title));
        ArrayList<Integer> mnuItems = new ArrayList<Integer>();

        _tvTitle.setText(title);   
        int displayMenuId = bundle.getInt(getActivity().getString(R.string.menu));

		boolean isRoadSideInspection = GlobalState.getInstance().getRoadsideInspectionMode();
		boolean isExemptFromELDUse = this.getIsExemptFromELDUse();
		boolean isEldMandateEnabled = GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled();

		// 2/20/2013 JEH: Used Ctrl+1 to convert to if/else because I changed KMBUI to a Library and it required this change.
        if (displayMenuId == R.string.mnu_sysmenu) {
			if(!isRoadSideInspection) mnuItems.add(R.string.mnu_sysmenu_records);
			mnuItems.add(R.string.mnu_sysmenu_reports);
			if(!GlobalState.getInstance().getCompanyConfigSettings(this.getActivity()).getMultipleUsersAllowed()) {
				if (!isRoadSideInspection) mnuItems.add(R.string.mnu_sysmenu_teamdriver);
				if (!isRoadSideInspection) mnuItems.add(R.string.mnu_sysmenu_teamdrivershare);
			}
			if(!isRoadSideInspection && 
    				GlobalState.getInstance().getCompanyConfigSettings(getActivity()).getAllowDriversCompleteDVIR())
    			mnuItems.add(R.string.mnu_sysmenu_vehicleinspection);
			mnuItems.add(R.string.mnu_sysmenu_diag);
			mnuItems.add(R.string.mnu_sysmenu_file);
		} else if (displayMenuId == R.string.mnu_sysmenu_records) {
			if(!isExemptFromELDUse)mnuItems.add(R.string.mnu_recordmenu_tripinfo);
			mnuItems.add(R.string.mnu_recordmenu_emprules);
			mnuItems.add(R.string.mnu_recordmenu_download);

			EmployeeLog currentlog = GlobalState.getInstance().getCurrentEmployeeLog();
			boolean isCurrentLogExemptFromELDUse = currentlog != null ? currentlog.getIsExemptFromELDUse() : false;

			if (isEldMandateEnabled && GlobalState.getInstance().getCurrentUser() != null && (GlobalState.getInstance().getCurrentUser().getExemptFromEldUse() || isCurrentLogExemptFromELDUse)) {
				// Exempt For ELD Use users do not need to Certify logs
				mnuItems.add(R.string.lblsubmitlogstitle);
			}
			else {
				mnuItems.add(R.string.mnu_recordmenu_certifysubmit);
			}

			if (isEldMandateEnabled) {
				mnuItems.add(R.string.mnu_recordmenu_unidentifiedeldevents);
			}else {
				mnuItems.add(R.string.mnu_recordmenu_unassigneddriving);
			}
			if(!isExemptFromELDUse && !isEldMandateEnabled) {
				mnuItems.add(R.string.mnu_recordmenu_editlocations);
			}

			DataProfileEnum dataProfile = GlobalState.getInstance().getCurrentUser().getDataProfile();
			if( dataProfile.getValue() != DataProfileEnum.MINIMUMHOS && dataProfile.getValue() != DataProfileEnum.MINIMUMHOSWITHGPS)
				//Only the MinimumHos data profile doesn't get the Fuel Purchase feature
				mnuItems.add(R.string.mnu_recordmenu_editfuelpurchases);
		} else if (displayMenuId == R.string.mnu_sysmenu_reports) {
			mnuItems.add(R.string.mnu_reportsmenu_dutystatus);
            if (isEldMandateEnabled) {
				mnuItems.add(R.string.mnu_reportsmenu_eldevents);
			}
			mnuItems.add(R.string.mnu_reportsmenu_availhours);
			mnuItems.add(R.string.mnu_reportsmenu_dailyhours);
			mnuItems.add(R.string.mnu_reportsmenu_failurereport);
			mnuItems.add(R.string.mnu_reportsmenu_locationcodes);
			mnuItems.add(R.string.mnu_reportsmenu_datausage);
			if (isEldMandateEnabled) {
				mnuItems.add(R.string.mnu_reportsmenu_malfunctionanddatadiagnostic);
			}
			if (GlobalState.getInstance().getCompanyConfigSettings(getActivity().getBaseContext()).getIsMotionPictureEnabled() && !isEldMandateEnabled){
				mnuItems.add(R.string.mnu_reportsmenu_dotauthority);
			}
		} else if (displayMenuId == R.string.mnu_sysmenu_teamdriver) {
			mnuItems.add(R.string.mnu_teamdrivermenu_start);
			mnuItems.add(R.string.mnu_teamdrivermenu_end);
		} else if (displayMenuId == R.string.mnu_sysmenu_teamdrivershare) {
			mnuItems.add(R.string.mnu_teamdriversharemenu_login);
			mnuItems.add(R.string.mnu_teamdriversharemenu_switch);
			mnuItems.add(R.string.mnu_teamdriversharemenu_logout);
		} else if (displayMenuId == R.string.mnu_sysmenu_vehicleinspection) {
			mnuItems.add(R.string.mnu_dvirmenu_newpretrip);
			mnuItems.add(R.string.mnu_dvirmenu_new);
			mnuItems.add(R.string.mnu_dvirmenu_newtrailer);
			mnuItems.add(R.string.mnu_dvirmenu_review);
		} else if (displayMenuId == R.string.mnu_sysmenu_diag) {
			mnuItems.add(R.string.mnu_diagmenu_appsettings);
			if (isEldMandateEnabled) {
				mnuItems.add(R.string.mnu_diagmenu_eldconfig);
				mnuItems.add(R.string.mnu_diagmenu_elddata);
				if(!isRoadSideInspection) mnuItems.add(R.string.mnu_diagmenu_elddiscovery);
				if(!isRoadSideInspection) mnuItems.add(R.string.mnu_diagmenu_seteldconfig);
				if (EobrReader.getIsEobrDeviceAvailable() && !EobrReader.getInstance().isEobrGen1())
					mnuItems.add(R.string.mnu_diagmenu_eldselftest);
			} else {
				mnuItems.add(R.string.mnu_diagmenu_eobrconfig);
				mnuItems.add(R.string.mnu_diagmenu_eobrdata);
				if(!isRoadSideInspection) mnuItems.add(R.string.mnu_diagmenu_eobrdiscovery);
				if(!isRoadSideInspection) mnuItems.add(R.string.mnu_diagmenu_seteobrconfig);
				if (EobrReader.getIsEobrDeviceAvailable() && !EobrReader.getInstance().isEobrGen1())
					mnuItems.add(R.string.mnu_diagmenu_eobrselftest);
			}

			if(GlobalState.getInstance().getCompanyConfigSettings(this.getActivity()).getIsGeotabEnabled())
				mnuItems.add(R.string.mnu_diagmenu_geotabconfig);
			mnuItems.add(R.string.mnu_diagmenu_uploaddiag);
			if(!isRoadSideInspection) mnuItems.add(R.string.mnu_diagmenu_odometercalibration);
		} else if (displayMenuId == R.string.mnu_sysmenu_file) {
			if(!isRoadSideInspection){
	        	mnuItems.add(R.string.mnu_filemenu_changepassword);
	        	mnuItems.add(R.string.mnu_filemenu_roadsideinspection);
	        	if (AppUpdateFactory.getInstance().areAppUpdateChecksEnabled())
	        		mnuItems.add(R.string.mnu_filemenu_checkforupdates);
	        	mnuItems.add(R.string.mnu_filemenu_admin);
				mnuItems.add(R.string.mnu_filemenu_requestlogs);
	        	mnuItems.add(R.string.mnu_filemenu_exit);
                mnuItems.add(R.string.mnu_filemenu_legal);
        	}
        	else
        		mnuItems.add(R.string.mnu_filemenu_roadsideinspectionenabled);
		}
        
        Integer[] menuItemIds = mnuItems.toArray(new Integer[mnuItems.size()]);
        String[] menuItemLabels = new String[mnuItems.size()];
        for(int i=0; i<mnuItems.size(); i++)
        	menuItemLabels[i] = getActivity().getString(menuItemIds[i]);
        
        MenuAdapter menuAdapter = new MenuAdapter(getActivity(), R.id.lblReportName, menuItemLabels, menuItemIds );       
        _grid.setAdapter(menuAdapter);

    }
	
	public void Reload()
	{
		loadControls();
	}
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
        	controlListener = (SystemMenuFragControllerMethods) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement SystemMenuFragControllerMethods");
        }
    }
	
	public GridView getMenuGridView(){
		if(_grid == null){
			_grid = (GridView)getView().findViewById(R.id.grdMenu);
		}
		return _grid;
	}
	
	public class MenuAdapter extends ArrayAdapter<String>{
		Context mContext;
		public static final int ACTIVITY_CREATE = 10;
		private Integer[] menuItemIds;
		
		public MenuAdapter(Context c, int textViewResourceId, String[] menuItemLabels, Integer[] menuItemIds){			                                  
			super(c, textViewResourceId, menuItemLabels);
			mContext = c;
			this.menuItemIds = menuItemIds;
		}
		
		public View getView(int position, View convertView, ViewGroup parent) {
			View v;
			
			LayoutInflater li = getActivity().getLayoutInflater();
			v = li.inflate(R.layout.systemmenuitem, null);

	        GridView grid = (GridView)parent; 
	        grid.setFocusable(false); 
	        grid.setFocusableInTouchMode(false); 
	
	        final int menuItemId = this.menuItemIds[position];
	        String menuItemText = getString(menuItemId);
		
			TextView tv = (TextView)v.findViewById(R.id.lblReportName);
			tv.setText(menuItemText);
			
			// 6/21/11 JHM - Defect 10490 stated that the first item of the system menu might be dimmed when
			// using the back button from a screen when you entered text (Enter text on Change password, back to system menu)
			// The only way I found to recreate this was in the emulator and the fix was the explicitly set the text color
			// as seen below.  I don't think we want to do this at this time because it wasn't reproducable on a device and
			// might enforce a style/text color different than that of the rest of the phone/app.
			// I'm making a note only for completeness and in the case where we need to come back and address this.
			// tv.setTextColor(Color.GRAY);
			
			ImageButton btn = (ImageButton)v.findViewById(R.id.ImageButton01);
			
			v.setId(position);
			btn.setId(position);
			v.setOnClickListener (
				new OnClickListener() {
		        public void onClick(View v) {		        	
		        	controlListener.runMenuAction(menuItemId, controlListener.getMyController().IsVehicleInMotion());
		        }
		    });    
			btn.setOnClickListener (
					new OnClickListener() {
			        public void onClick(View v) {
			        	controlListener.runMenuAction(menuItemId, controlListener.getMyController().IsVehicleInMotion());
			        }
			    });
			return v;
		}

		public long getItemId(int position) {
			return 0;
		}
	}
}
