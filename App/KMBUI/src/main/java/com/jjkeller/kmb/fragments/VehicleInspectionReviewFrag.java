package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.jjkeller.kmb.interfaces.IVehicleInspectionReview.VehicleInspectionReviewFragActions;
import com.jjkeller.kmb.interfaces.IVehicleInspectionReview.VehicleInspectionReviewFragControllerMethods;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbapi.controller.EOBR.EobrReader;
import com.jjkeller.kmbapi.proxydata.EobrConfiguration;
import com.jjkeller.kmbui.R;

import java.util.List;

public class VehicleInspectionReviewFrag extends BaseFragment{
	VehicleInspectionReviewFragControllerMethods controlListener;
	VehicleInspectionReviewFragActions actionsListener;
	
	private Spinner _cboUnitNbr;
	private EditText _txtTrailerNumber;
	private TextView _tvInspectionDate;
	private TextView _tvDefectList;
	private TextView _lblDefects;
	private TextView _tvCorrectionsMade;
	private TextView _tvCertifiedBy; 
	private TextView _tvCertifyDate;
	private TextView _tvNoCorrectionsNeeded;
	private TextView _tvPostTripDate;
	private TextView _tvPreTripDate;
	private Button _btnViewPostInspectionDetail;
	private Button _btnViewPreInspectionDetail;
	private View _pnlDate;
	private View _pnlDefects;
	private View _pnlSatisfactory;
	private View _pnlPostTrip;
	private View _pnlPreTrip;
	private TextView _lblVehicleType;
	private List<EobrConfiguration> _allEobrDevices;
	private String _currentInspection;
	private int _vehicleType = 0;
	private static final int TRACTOR = 0;
	private static final int TRAILER = 1;
	private static final int PRETRIP = 0;
	private static final int POSTTRIP = 1;
	private Context _ctx;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		View v = inflater.inflate(R.layout.f_vehicleinspectionreview, container, false);
		findControls(v);
		return v;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		this.loadControls(savedInstanceState);
	}
	
	protected void loadControls(Bundle savedInstanceState){
		if(_vehicleType == TRACTOR)
			_allEobrDevices = controlListener.getMyController().AllEobrDevices();
		
		if(savedInstanceState != null && savedInstanceState.containsKey("InspectionVehicleType")) {


			if(savedInstanceState.getInt("InspectionVehicleType") == TRACTOR) {
				loadInspectionControls(savedInstanceState);

				_cboUnitNbr.setVisibility(View.VISIBLE);
				_txtTrailerNumber.setVisibility(View.GONE);
			}
			else {
				if(savedInstanceState.containsKey("TrailerNumber"))
					getTrailerNumberEditText().setText(savedInstanceState.getString("TrailerNumber"));
				loadInspectionControls(savedInstanceState);
				_txtTrailerNumber.setVisibility(View.VISIBLE);
				_cboUnitNbr.setVisibility(View.GONE);
			}

		}
		else{			
			ShowInspectionForVehicleType(TRACTOR);
		}
	}

	private void loadInspectionControls(Bundle savedInstanceState) {
		// 08/07/2018 mvazquez As per defect 67557, all the other fields should be retrieved in the state bundle
		BuildTractorNumberList(EobrReader.getInstance().getEobrIdentifier());
		_tvDefectList.setText(savedInstanceState.getString("DefectList"));
		_tvInspectionDate.setText(savedInstanceState.getString("InspectionDate"));
		_lblDefects.setText(savedInstanceState.getString("Defects"));
		_tvCorrectionsMade.setText(savedInstanceState.getString("CorrectionsMade"));
		_tvCertifiedBy.setText(savedInstanceState.getString("CertifiedBy"));
		_tvCertifyDate.setText(savedInstanceState.getString("CertifyDate"));
		_tvPostTripDate.setText(savedInstanceState.getString("PostTripDate"));
		_tvPreTripDate.setText(savedInstanceState.getString("PreTripDate"));
		_tvNoCorrectionsNeeded.setText(savedInstanceState.getString("NoCorrectionsNeeded"));
		_lblVehicleType.setText(savedInstanceState.getString("VehicleType"));
		_pnlPostTrip.setVisibility(View.VISIBLE);
		_pnlPreTrip.setVisibility(View.VISIBLE);
		switch(savedInstanceState.getInt("CurrentInspection")){
			case POSTTRIP:
				actionsListener.showSelectedInspection(POSTTRIP);
				break;
			case PRETRIP:
				actionsListener.showSelectedInspection(PRETRIP);
				break;
		}

	}
	
	@Override
	public void onSaveInstanceState(Bundle outState)
	{

		
    	// 7/10/121 ACM - Save "vehicle type" in state bundle
		outState.putInt("InspectionVehicleType", _vehicleType);		
		outState.putString("TrailerNumber", _txtTrailerNumber.getText().toString());
		// 08/07/2018 mvazquez As per defect 67557, all the other fields should be saved in the state bundle
		outState.putString("DefectList", _tvDefectList.getText().toString());
		outState.putString("InspectionDate", _tvInspectionDate.getText().toString());
		outState.putString("Defects", _lblDefects.getText().toString());
		outState.putString("CorrectionsMade", _tvCorrectionsMade.getText().toString());
		outState.putString("CertifiedBy", _tvCertifiedBy.getText().toString());
		outState.putString("CertifyDate", _tvCertifyDate.getText().toString());
		outState.putString("PostTripDate", _tvPostTripDate.getText().toString());
		outState.putString("PreTripDate", _tvPreTripDate.getText().toString());
		outState.putString("NoCorrectionsNeeded", _tvNoCorrectionsNeeded.getText().toString());
		outState.putString("VehicleType", _lblVehicleType.getText().toString());
		outState.putInt("CurrentInspection", actionsListener.getCurrentSelectedInspection());
		super.onSaveInstanceState(outState);
	}
	
	protected void findControls(View v){
		_ctx = getActivity();
		_cboUnitNbr = (Spinner)v.findViewById(R.id.cboUnitNbr);
		_txtTrailerNumber = (EditText)v.findViewById(R.id.txtTrailerNumber);
		_tvInspectionDate = (TextView)v.findViewById(R.id.tvInspectionDate);
		_tvDefectList = (TextView)v.findViewById(R.id.tvDefectList);
		_lblDefects = (TextView)v.findViewById(R.id.lblDefects);
		_tvCorrectionsMade = (TextView)v.findViewById(R.id.tvCorrectionsMade);
		_tvCertifiedBy = (TextView)v.findViewById(R.id.tvCertifiedBy);
		_tvCertifyDate = (TextView)v.findViewById(R.id.tvCertifyDate);
		_tvPostTripDate = (TextView)v.findViewById(R.id.tvPostTripDate);
		_tvPreTripDate = (TextView)v.findViewById(R.id.tvPreTripDate);
		_btnViewPostInspectionDetail = (Button)v.findViewById(R.id.btnViewPostInspectionDetail);
		_btnViewPreInspectionDetail = (Button)v.findViewById(R.id.btnViewPreInspectionDetail);
		_pnlDefects = (View)v.findViewById(R.id.pnlDefects);
		_pnlSatisfactory = (View)v.findViewById(R.id.pnlSatisfactory);
		_pnlDate = (View)v.findViewById(R.id.pnlDate);
		_pnlPostTrip = (View)v.findViewById(R.id.pnlPostTrip);
		_pnlPreTrip = (View)v.findViewById(R.id.pnlPreTrip);
		_tvNoCorrectionsNeeded = (TextView)v.findViewById(R.id.tvNoCorrectionNeeded);
		_lblVehicleType = (TextView)v.findViewById(R.id.lblVehicleType);
		
		_btnViewPostInspectionDetail.setOnClickListener(
				new OnClickListener() {					
					public void onClick(View v) {
						actionsListener.handleViewPostButtonClick(_ctx);
					}});
		
		_btnViewPreInspectionDetail.setOnClickListener(
			new OnClickListener() {
				public void onClick(View v) {
					actionsListener.handleViewPreButtonClick(_ctx);
				}});
	}
	
	public void ShowInspectionForVehicleType(int vehicleType){	
		_vehicleType = vehicleType;
		switch (vehicleType){
		case TRACTOR:			
			BuildTractorNumberList(EobrReader.getInstance().getEobrIdentifier());			
			if(!_cboUnitNbr.isEnabled())
			{
				actionsListener.DownloadAction();
			}
			_cboUnitNbr.setVisibility(View.VISIBLE);
			_txtTrailerNumber.setVisibility(View.GONE);
			_lblVehicleType.setText(getActivity().getString(R.string.lbltractornumber));
			break;
		case TRAILER:
			_txtTrailerNumber.setVisibility(View.VISIBLE);
			_cboUnitNbr.setVisibility(View.GONE);
			_lblVehicleType.setText(getActivity().getString(R.string.lbltrailernumber2));
			if(_txtTrailerNumber.getText() != null && _txtTrailerNumber.getText().length()>0)
			{
				actionsListener.DownloadAction();
			}
			break;
		}
	}
	
	private void BuildTractorNumberList(String eobrTractorNumber) {		  
		if(_allEobrDevices != null && _allEobrDevices.size() > 0)
		{   
			// add each tractor number to the list.
			ArrayAdapter<EobrConfiguration> spinnerArrayAdapter = new ArrayAdapter<EobrConfiguration>(getActivity(), R.layout.kmb_spinner_item, _allEobrDevices);
			spinnerArrayAdapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
			_cboUnitNbr.setAdapter(spinnerArrayAdapter);
			_cboUnitNbr.setEnabled(true);						
					
			if(eobrTractorNumber != null && eobrTractorNumber.length() > 0) {
			// set the selected item in the tractor dropdown
				for(int index = 0; index < _cboUnitNbr.getCount(); index++)
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
	
	@Override
    public void onAttach(Activity activity) {
		super.onAttach(activity);
        try {
        	actionsListener = (VehicleInspectionReviewFragActions) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement VehicleInspectionReviewFragActions");
        }
        
        try{
        	controlListener = (VehicleInspectionReviewFragControllerMethods) activity;
        } catch (ClassCastException e) {
        	throw new ClassCastException(activity.toString() + " must implement VehicleInspectionReviewControllerMethods");
        }
    }
	
	public EditText getTrailerNumberEditText(){
		if(_txtTrailerNumber == null)
			_txtTrailerNumber = (EditText)getView().findViewById(R.id.txtTrailerNumber);
		return _txtTrailerNumber;
	}
	
	public Spinner getUnitNumberSpinner(){
		if(_cboUnitNbr == null)
			_cboUnitNbr = (Spinner)getView().findViewById(R.id.cboUnitNbr);
		return _cboUnitNbr;
	}

	public TextView getDefectsLabel(){
		if(_lblDefects == null)
			_lblDefects = (TextView)getView().findViewById(R.id.lblDefects);
		return _lblDefects;
	}
	
	public int getVehicleTypeInt(){
		return _vehicleType;
	}
	
	public TextView getDefectsListTextView(){
		if(_tvDefectList == null)
			_tvDefectList = (TextView)getView().findViewById(R.id.tvDefectList);
		return _tvDefectList;
	}
	
	public TextView getNoCorrectionsNeededTextView(){
		if(_tvNoCorrectionsNeeded == null)
			_tvNoCorrectionsNeeded = (TextView)getView().findViewById(R.id.tvNoCorrectionNeeded);
		return _tvNoCorrectionsNeeded;
	}
	
	public TextView getCertifyDateTextView(){
		if(_tvCertifyDate == null)
			_tvCertifyDate = (TextView)getView().findViewById(R.id.tvCertifyDate);
		return _tvCertifyDate;
	}

	public TextView getCorrectionsMadeTextView(){
		if(_tvCorrectionsMade == null)
			_tvCorrectionsMade = (TextView)getView().findViewById(R.id.tvCorrectionsMade);
		return _tvCorrectionsMade;
	}
	
	public TextView getInspectionDateTextView(){
		if(_tvInspectionDate == null)
			_tvInspectionDate = (TextView)getView().findViewById(R.id.tvInspectionDate);
		return _tvInspectionDate;
	}
	
	public TextView getCertifiedByTextView(){
		if(_tvCertifiedBy == null)
			_tvCertifiedBy = (TextView)getView().findViewById(R.id.tvCertifiedBy);
		return _tvCertifiedBy;
	}
	
	public TextView get_PostTripDateTextView(){
		if(_tvPostTripDate == null)
		{
			_tvPostTripDate = (TextView)getView().findViewById(R.id.tvPostTripDate);
		}
	
		return _tvPostTripDate;
	}
	
	public TextView get_PreTripDateTextView(){
		if(_tvPreTripDate == null)
		{
			_tvPreTripDate = (TextView)getView().findViewById(R.id.tvPreTripDate);
		}
		
		return _tvPreTripDate;
	}
	
	public View getDefectsPanel(){
		if(_pnlDefects == null)
			_pnlDefects = (View)getView().findViewById(R.id.pnlDefects);
		return _pnlDefects;
	}
	
	public View getSatisfactoryPanel(){
		if(_pnlSatisfactory == null)
			_pnlSatisfactory = (View)getView().findViewById(R.id.pnlSatisfactory);
		return _pnlSatisfactory;
	}

	public View getDatePanel(){
		if(_pnlDate == null)
			_pnlDate = (View)getView().findViewById(R.id.pnlDate);
		return _pnlDate;
	}
	
	public View getPostTripPanel(){
		if(_pnlPostTrip == null) 
		{
			_pnlPostTrip = (View)getView().findViewById(R.id.pnlPostTrip);
		}
		
		return _pnlPostTrip;
	}
	
	public View getPreTripPanel(){
		if(_pnlPreTrip == null) 
		{
			_pnlPreTrip = (View)getView().findViewById(R.id.pnlPreTrip);
		}
		
		return _pnlPreTrip;
	}
	
	public List<EobrConfiguration> getAllEobrDevices(){
		return _allEobrDevices;
	}
}
