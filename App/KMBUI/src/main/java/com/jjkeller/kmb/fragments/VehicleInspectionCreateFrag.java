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
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.TextView;

import com.jjkeller.kmb.interfaces.IVehicleInspectionCreate.VehicleInspectionCreateControllerMethods;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.enums.InspectionDefectType;
import com.jjkeller.kmbapi.enums.InspectionTypeEnum;
import com.jjkeller.kmbapi.proxydata.EobrConfiguration;
import com.jjkeller.kmbapi.proxydata.VehicleInspection;
import com.jjkeller.kmbui.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class VehicleInspectionCreateFrag extends BaseFragment {
	VehicleInspectionCreateControllerMethods controllerListener;

	private View _pnlTractorNbr;
	private View _pnlTrailerNbr;
	private View _pnlNoDefects;
	private View _pnlDefects;
	private View _pnlDetail;
	private View _pnlRemarks;
	
	private CheckBox _chkSatisfactoryCondition;
    private Button _btnInspectionDate;
    private Button _btnInspectionTime;
    private Date _inspectionDate;
    private Spinner _cboUnitNbr;
    private TextView _txtTrailerNbr;
    private TextView _txtRemarks;
    private GridView _grdDefects;
    
    private boolean _isPoweredUnit = true;
    
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
        // Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.f_vehicleinspectioncreate, container, false);
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
		_pnlTractorNbr = (View)v.findViewById(R.id.pnlTractorNbr);
		_pnlTrailerNbr = (View)v.findViewById(R.id.pnlTrailerNbr);
		_pnlNoDefects = (View)v.findViewById(R.id.pnlNoDefects);
		_pnlDefects = (View)v.findViewById(R.id.pnlDefects);
		_pnlDetail = (View)v.findViewById(R.id.pnlDetail);
		_pnlRemarks = (View)v.findViewById(R.id.pnlRemarks);

		_btnInspectionDate = (Button)v.findViewById(R.id.btnInspectionDate);
		_btnInspectionTime = (Button)v.findViewById(R.id.btnInspectionTime);
		_chkSatisfactoryCondition = (CheckBox)v.findViewById(R.id.chkNoDefects);
		_cboUnitNbr = (Spinner)v.findViewById(R.id.cboUnitNbr);
		_txtTrailerNbr = (TextView)v.findViewById(R.id.txtTrailerNbr);
		_txtRemarks = (TextView)v.findViewById(R.id.txtRemarks);
		_grdDefects = (GridView)v.findViewById(R.id.grdDefects);
		
		_btnInspectionDate.setOnClickListener(
	            new OnClickListener() {
	            	public void onClick(View v) {
	            		ShowDatePickerDialog(_btnInspectionDate);
	            	}
	            });
		
		_btnInspectionTime.setOnClickListener(
				new OnClickListener() {
					public void onClick(View v) {
						ShowTimePickerDialog(_btnInspectionTime);
					}
				});

		_chkSatisfactoryCondition.setOnCheckedChangeListener(
	            new OnCheckedChangeListener() {
	            	public void onCheckedChanged(CompoundButton btn, boolean isChecked) {
	            		VehicleInspectionCreateFrag.this.ShowDefects(!isChecked);
	            	}
	            });
	}
	
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
        	controllerListener = (VehicleInspectionCreateControllerMethods) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement " + VehicleInspectionCreateControllerMethods.class.getSimpleName());
        }
    }
    
    private void loadControls(Bundle savedInstanceState)
	{
		int vehicleInspectionType = getActivity().getIntent().getIntExtra(getString(R.string.parm_vehicleinspectiontype), InspectionTypeEnum.POSTTRIP);			
		_isPoweredUnit = getActivity().getIntent().getBooleanExtra(getString(R.string.parm_vehicleinspectionpoweredunit), true);			
				
		controllerListener.StartInspection(new InspectionTypeEnum(vehicleInspectionType), _isPoweredUnit);
		VehicleInspection inspection = controllerListener.getCurrentVehicleInspection();
		
		if(savedInstanceState != null && savedInstanceState.containsKey("defectlist"))
			controllerListener.PutSerializableDefectList(savedInstanceState.getIntArray("defectlist"));

		_inspectionDate = inspection.getInspectionTimeStamp();
		Calendar cal = Calendar.getInstance();
		cal.setTime(_inspectionDate);
		updateDateDisplay(cal);
	    
	    if(_isPoweredUnit){	    	
	    	this.BuildTractorNumberList(inspection.getTractorNumber());
	    }
	    
	    this.ShowTractor(_isPoweredUnit);
	    this.ShowDefects(!inspection.getIsConditionSatisfactory());
	    this.ShowRemarks(false);
	    
	    this.BindDefectList();
	    
	    _chkSatisfactoryCondition.setChecked(inspection.getIsConditionSatisfactory());

		if(savedInstanceState != null)
		{
			Spinner cboUnitNbr = (Spinner)getActivity().findViewById(R.id.cboUnitNbr);
			cboUnitNbr.setSelection(savedInstanceState.getInt("selectedUnitIndex"));
			
			CheckBox chkNoDefects = (CheckBox)getActivity().findViewById(R.id.chkNoDefects);
			chkNoDefects.setChecked(savedInstanceState.getBoolean("isSatisfactory"));

			_inspectionDate = new Date(savedInstanceState.getLong("inspectionDate"));
			cal = Calendar.getInstance();
			cal.setTime(_inspectionDate);
			this.updateDateDisplay(cal);
			
			TextView txtTrailerNbr = (TextView)getActivity().findViewById(R.id.txtTrailerNbr);
			txtTrailerNbr.setText(savedInstanceState.getCharSequence("trailerNbr"));
			
			TextView txtRemarks = (TextView)getActivity().findViewById(R.id.txtRemarks);
			txtRemarks.setText(savedInstanceState.getCharSequence("remarks"));
			
			this.ShowRemarks(savedInstanceState.getBoolean("showRemarks"));
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		outState.putInt("selectedUnitIndex", this.GetUnitNumberDropdown().getSelectedItemPosition());
		
		outState.putBoolean("isSatisfactory", this.GetSatisfactoryConditionCheckbox().isChecked());

		outState.putLong("inspectionDate", this.GetInspectionDate().getTime());	
		
		outState.putCharSequence("trailerNbr", this.GetTrailerNbrTextbox().getText());
		
		outState.putCharSequence("remarks", this.GetRemarksTextbox().getText());
		
		outState.putBoolean("showRemarks", GetRemarksPanel().getVisibility() == View.VISIBLE);
		
		outState.putIntArray("defectlist", controllerListener.GetSerializableDefectList());

		super.onSaveInstanceState(outState);
		
	}
	
	// updates the time we display in the TextView
	private void updateDateDisplay(Calendar c) {
		Date inspectionDate = c.getTime();
		_btnInspectionDate.setText(DateUtility.getDateFormat().format(inspectionDate));
		_btnInspectionTime.setText(DateUtility.getHomeTerminalTime12HourFormat().format(inspectionDate));
        controllerListener.AssignInspectionDate(inspectionDate);
    }
		
	private void ShowTractor(boolean isTractor) {
		if(isTractor){
			_pnlTractorNbr.setVisibility(View.VISIBLE);
			_pnlTrailerNbr.setVisibility(View.GONE);
		}
		else{
			_pnlTractorNbr.setVisibility(View.GONE);
			_pnlTrailerNbr.setVisibility(View.VISIBLE);
		}	
	}

	public void ShowDefects(boolean isDefect) {
		if(isDefect){
			_pnlDefects.setVisibility(View.VISIBLE);			
			_pnlNoDefects.setVisibility(View.GONE);
		}
		else{
			_pnlDefects.setVisibility(View.GONE);
			_pnlNoDefects.setVisibility(View.VISIBLE);
		}	
	}
	
	private void ShowRemarks(boolean displayRemarks){
		if(displayRemarks){
			_pnlRemarks.setVisibility(View.VISIBLE);
			_pnlDetail.setVisibility(View.GONE);
		}
		else{
			_pnlRemarks.setVisibility(View.GONE);			
			_pnlDetail.setVisibility(View.VISIBLE);
		}
	}
	
	private void BuildTractorNumberList(String eobrTractorNumber) {
		List<EobrConfiguration> allEobrDevices = controllerListener.AllEobrDevices();
		
		if(allEobrDevices.size() > 0)
		{			
			// add each tractor number to the list.
			ArrayAdapter<EobrConfiguration> spinnerArrayAdapter = new ArrayAdapter<EobrConfiguration>(getActivity(), R.layout.kmb_spinner_item, allEobrDevices);
    	    spinnerArrayAdapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
    	    _cboUnitNbr.setAdapter(spinnerArrayAdapter);
    	    _cboUnitNbr.setEnabled(true);
    	    
     	    if(eobrTractorNumber != null && eobrTractorNumber.length() > 0) {
     	   	    // set the selected item in the tractor dropdown
	    	    for(int index = 0; index<_cboUnitNbr.getCount(); index++)
	    	    {
	    	    	if(_cboUnitNbr.getItemAtPosition(index).toString().compareTo(eobrTractorNumber) == 0){
	    	    		_cboUnitNbr.setSelection(index);
	    	    		_cboUnitNbr.setEnabled(false);
	    	    		break;
	    	    	}
	    	    }
    	    }
    	    
    	    if(_cboUnitNbr.getSelectedItemPosition() < 0) _cboUnitNbr.setSelection(0);
		}		
	}
	
    public void BindDefectList(){
    	List<InspectionDefectType> defectList = new ArrayList<InspectionDefectType>();
    	
        if (_isPoweredUnit)
        {
        	defectList.add(new InspectionDefectType(InspectionDefectType.AIRCOMPRESSOR));
        	defectList.add(new InspectionDefectType(InspectionDefectType.AIRLINES));
        	defectList.add(new InspectionDefectType(InspectionDefectType.BATTERY));
        	defectList.add(new InspectionDefectType(InspectionDefectType.BODY));
        	defectList.add(new InspectionDefectType(InspectionDefectType.BRAKEACCESSORIES));
        	defectList.add(new InspectionDefectType(InspectionDefectType.BRAKESPARKING));
        	defectList.add(new InspectionDefectType(InspectionDefectType.BRAKESSERVICE));
        	defectList.add(new InspectionDefectType(InspectionDefectType.CLUTCH));
        	defectList.add(new InspectionDefectType(InspectionDefectType.COUPLINGDEVICES));
        	defectList.add(new InspectionDefectType(InspectionDefectType.DEFROSTERHEATER));
        	defectList.add(new InspectionDefectType(InspectionDefectType.DRIVELINE));
        	defectList.add(new InspectionDefectType(InspectionDefectType.ENGINE));
        	defectList.add(new InspectionDefectType(InspectionDefectType.EXHAUST));
        	defectList.add(new InspectionDefectType(InspectionDefectType.FIFTHWHEEL));
        	defectList.add(new InspectionDefectType(InspectionDefectType.FRAMEANDASSEMBLY));
        	defectList.add(new InspectionDefectType(InspectionDefectType.FRONTAXLE));
        	defectList.add(new InspectionDefectType(InspectionDefectType.FUELTANK));
        	defectList.add(new InspectionDefectType(InspectionDefectType.HORN));
        	defectList.add(new InspectionDefectType(InspectionDefectType.LIGHTSHEAD));
        	defectList.add(new InspectionDefectType(InspectionDefectType.LIGHTSTAIL));
        	defectList.add(new InspectionDefectType(InspectionDefectType.LIGHTSSTOP));
        	defectList.add(new InspectionDefectType(InspectionDefectType.LIGHTSDASH));
        	defectList.add(new InspectionDefectType(InspectionDefectType.LIGHTSTURNINDICATORS));
        	defectList.add(new InspectionDefectType(InspectionDefectType.MIRRORS));
        	defectList.add(new InspectionDefectType(InspectionDefectType.MUFFLER));
        	defectList.add(new InspectionDefectType(InspectionDefectType.OILPRESSURE));
        	defectList.add(new InspectionDefectType(InspectionDefectType.RADIATOR));
        	defectList.add(new InspectionDefectType(InspectionDefectType.REAREND));
        	defectList.add(new InspectionDefectType(InspectionDefectType.REFLECTORS));
        	defectList.add(new InspectionDefectType(InspectionDefectType.SAFETYFIREEXTINGUISHER));
        	defectList.add(new InspectionDefectType(InspectionDefectType.SAFETYFLAGS));
        	defectList.add(new InspectionDefectType(InspectionDefectType.SAFETYFLARES));
        	defectList.add(new InspectionDefectType(InspectionDefectType.SAFETYREFLECTIVETRIANGLES));
        	defectList.add(new InspectionDefectType(InspectionDefectType.SAFETYSPAREBULBFUSES));
        	defectList.add(new InspectionDefectType(InspectionDefectType.SAFETYSPARESEALBEAM));
        	defectList.add(new InspectionDefectType(InspectionDefectType.STARTER));
        	defectList.add(new InspectionDefectType(InspectionDefectType.STEERING));
        	defectList.add(new InspectionDefectType(InspectionDefectType.SUSPENSIONSYSTEM));
        	defectList.add(new InspectionDefectType(InspectionDefectType.TACHOGRAPH));
        	defectList.add(new InspectionDefectType(InspectionDefectType.TIRES));
        	defectList.add(new InspectionDefectType(InspectionDefectType.TIRECHAINS));
        	defectList.add(new InspectionDefectType(InspectionDefectType.TRANSMISSION));
        	defectList.add(new InspectionDefectType(InspectionDefectType.WHEELSANDRIMS));
        	defectList.add(new InspectionDefectType(InspectionDefectType.WINDOWS));
        	defectList.add(new InspectionDefectType(InspectionDefectType.WINDSHIELDWIPERS));
        	defectList.add(new InspectionDefectType(InspectionDefectType.OTHER));       
        }
        else
        {
        	defectList.add(new InspectionDefectType(InspectionDefectType.BRAKECONNECTIONS));
        	defectList.add(new InspectionDefectType(InspectionDefectType.BRAKES));
        	defectList.add(new InspectionDefectType(InspectionDefectType.COUPLINGDEVICES));
        	defectList.add(new InspectionDefectType(InspectionDefectType.COUPLINGKINGPIN));
        	defectList.add(new InspectionDefectType(InspectionDefectType.DOORS));
        	defectList.add(new InspectionDefectType(InspectionDefectType.HITCH));
        	defectList.add(new InspectionDefectType(InspectionDefectType.LANDINGGEAR));
        	defectList.add(new InspectionDefectType(InspectionDefectType.LIGHTSALL));
        	defectList.add(new InspectionDefectType(InspectionDefectType.ROOF));
        	defectList.add(new InspectionDefectType(InspectionDefectType.SUSPENSIONSYSTEM));
        	defectList.add(new InspectionDefectType(InspectionDefectType.TARPAULIN));
        	defectList.add(new InspectionDefectType(InspectionDefectType.TIRES));
        	defectList.add(new InspectionDefectType(InspectionDefectType.WHEELSANDRIMS));
        	defectList.add(new InspectionDefectType(InspectionDefectType.OTHER));        	
        }

		_grdDefects.setAdapter(new InspectionDefectTypeAdapter(getActivity(), R.layout.grdinspectiondefect, defectList ));
    }
    
	public boolean GetIsPoweredUnit()
	{
		return _isPoweredUnit;
	}
	
	public Date GetInspectionDate()
	{
		return _inspectionDate;
	}
	public void SetInspectionDate(Date d)
	{
		_inspectionDate = d;
	}

	public Button GetInspectionDateButton()
	{
    	if (_btnInspectionDate == null)
    	{
    		_btnInspectionDate = (Button)getView().findViewById(R.id.btnInspectionDate);
    	}
    	return _btnInspectionDate;
	}
	
	public Button GetInspectionTimeButton()
	{
		if (_btnInspectionTime == null)
		{
			_btnInspectionTime = (Button)getView().findViewById(R.id.btnInspectionTime);
		}
		return _btnInspectionTime;
	}

	public CheckBox GetSatisfactoryConditionCheckbox()
	{
    	if (_chkSatisfactoryCondition == null)
    	{
    		_chkSatisfactoryCondition = (CheckBox)getView().findViewById(R.id.chkNoDefects);
    	}
    	return _chkSatisfactoryCondition;		
	}

	public Spinner GetUnitNumberDropdown()
	{
    	if (_cboUnitNbr == null)
    	{
    		_cboUnitNbr = (Spinner)getView().findViewById(R.id.cboUnitNbr);
    	}
    	return _cboUnitNbr;		
	}
	
	public TextView GetTrailerNbrTextbox()
	{
    	if (_txtTrailerNbr == null)
    	{
    		_txtTrailerNbr = (TextView)getView().findViewById(R.id.txtTrailerNbr);
    	}
    	return _txtTrailerNbr;		
	}

	public TextView GetRemarksTextbox()
	{
    	if (_txtRemarks == null)
    	{
    		_txtRemarks = (TextView)getView().findViewById(R.id.txtRemarks);
    	}
    	return _txtRemarks;		
	}

	public GridView GetDefectsGrid()
	{
    	if (_grdDefects == null)
    	{
    		_grdDefects = (GridView)getView().findViewById(R.id.grdDefects);
    	}
    	return _grdDefects;		
	}
    
	public View GetTractorNbrPanel()
    {
    	if (_pnlTractorNbr == null)
    	{
    		_pnlTractorNbr = getView().findViewById(R.id.pnlTractorNbr);
    	}
    	return _pnlTractorNbr;
    }

	public View GetTrailerNbrPanel()
    {
    	if (_pnlTrailerNbr == null)
    	{
    		_pnlTrailerNbr = getView().findViewById(R.id.pnlTrailerNbr);
    	}
    	return _pnlTrailerNbr;
    }

	public View GetNoDefectsPanel()
    {
    	if (_pnlNoDefects == null)
    	{
    		_pnlNoDefects = getView().findViewById(R.id.pnlNoDefects);
    	}
    	return _pnlNoDefects;
    }
	
	public View GetDefectsPanel()
    {
    	if (_pnlDefects == null)
    	{
    		_pnlDefects = getView().findViewById(R.id.pnlDefects);
    	}
    	return _pnlDefects;
    }
	
	public View GetDetailPanel()
    {
    	if (_pnlDetail == null)
    	{
    		_pnlDetail = getView().findViewById(R.id.pnlDetail);
    	}
    	return _pnlDetail;
    }

	public View GetRemarksPanel()
    {
    	if (_pnlRemarks == null)
    	{
    		_pnlRemarks = getView().findViewById(R.id.pnlRemarks);
    	}
    	return _pnlRemarks;
    }

    public void ToggleRemarksPanel(boolean displayRemarks)
    {
    	this.ShowRemarks(displayRemarks);
    }

	private class InspectionDefectTypeAdapter extends ArrayAdapter<InspectionDefectType>
	{
		public InspectionDefectTypeAdapter(Context context, int textViewResourceId, List<InspectionDefectType> items) {
			super(context, textViewResourceId, items);
		}
		
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
        	View v = convertView;
        	if (v == null) {
        		LayoutInflater li = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        		v = li.inflate(R.layout.grdinspectiondefect, null);
        	}
        	
        	final InspectionDefectType defect = this.getItem(position);

        	if(defect != null)
        	{            	
            	CheckBox chkDefect = (CheckBox)v.findViewById(R.id.chkDefect);
            	
            	String defectName = defect.getString(v.getContext()); 
            	chkDefect.setText(defectName);
            	
            	boolean isChecked = VehicleInspectionCreateFrag.this.controllerListener.DoesInspectionContainDefect(defect);
            	chkDefect.setChecked(isChecked);
            	//Log.v("VehicleInspection", String.format("setting checkbox: %s isChecked: %s", defectName, isChecked));
            	
        		chkDefect.setOnClickListener(
        	            new OnClickListener() {
        	            	public void onClick(View v) {
        	        			CheckBox chkDefect = (CheckBox)v;        	            		
        	            		if(chkDefect.isChecked())
        	            			VehicleInspectionCreateFrag.this.controllerListener.AddDefectToInspection(defect);
        	            		else
        	            			VehicleInspectionCreateFrag.this.controllerListener.RemoveDefectFromInspection(defect);
        	            	}
        	            });
        	}
        	
        	return v;
        }
	}
}
