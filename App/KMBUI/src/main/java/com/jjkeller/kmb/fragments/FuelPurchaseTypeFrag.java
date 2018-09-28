package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.jjkeller.kmb.interfaces.IFuelPurchaseType.FuelPurchaseTypeFragActions;
import com.jjkeller.kmb.interfaces.IFuelPurchaseType.FuelPurchaseTypeFragControllerMethods;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbapi.enums.FuelClassificationEnum;
import com.jjkeller.kmbapi.proxydata.FuelPurchase;
import com.jjkeller.kmbui.R;

import java.util.ArrayList;
import java.util.Calendar;

public class FuelPurchaseTypeFrag extends BaseFragment { 
	FuelPurchaseTypeFragControllerMethods controlListener;
	FuelPurchaseTypeFragActions actionsListener;
	
	ArrayAdapter<String> fuelClassificationAdapter;
    private FuelPurchase _fp;
    private Spinner _cboPurchaseType;
    private Button _btnDateOfPurchase;
    private Button _btnTimeOfPurchase;
    private Button _btnCancel;
    private Button _btnOK;
    
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        // Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.f_fuelpurchasetype, container, false);
		findControls(v);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		this.loadControls(savedInstanceState);
	}
	
	private void loadControls(Bundle savedInstanceState){
		_fp = controlListener.getMyController().getWorkingFuelPurchase();
		ArrayList<String> fuelClassificationArray = new ArrayList<String>();
		fuelClassificationArray.add(getString(R.string.fuelclassification_receipted));
		fuelClassificationArray.add(getString(R.string.fuelclassification_nonreceipted));
		fuelClassificationArray.add(getString(R.string.fuelclassification_taxpaidbulk));
		fuelClassificationArray.add(getString(R.string.fuelclassification_taxnotpaidbulk));
		fuelClassificationArray.add(getString(R.string.fuelclassification_reefer));

		fuelClassificationAdapter = new ArrayAdapter<String>(getActivity(), R.layout.kmb_spinner_item, fuelClassificationArray);
		//ArrayAdapter.createFromResource( this, R.array.fuelclassification_displayarray, android.R.layout.simple_spinner_item);
		fuelClassificationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        _cboPurchaseType.setAdapter(fuelClassificationAdapter);
        
		if(savedInstanceState != null)
		{
			_cboPurchaseType.setSelection(savedInstanceState.getInt(getString(R.string.state_fuelpurchasetype)));
		}
		else
		{
	        switch (_fp.getFuelClassification().getValue())
	        {
	            case FuelClassificationEnum.NONRECEIPTED:
	                _cboPurchaseType.setSelection(1);
	                break;
	            case FuelClassificationEnum.TAXPAIDBULK:
	            	_cboPurchaseType.setSelection(2);
	                break;
	            case FuelClassificationEnum.TAXNOTPAIDBULK:
	            	_cboPurchaseType.setSelection(3);
	                break;
	            case FuelClassificationEnum.REEFER:
	            	_cboPurchaseType.setSelection(4);
	                break;
	            default:
	            	_cboPurchaseType.setSelection(0);
	                break;
	        }
		}
		
		if(savedInstanceState != null &&
				savedInstanceState.containsKey(getString(R.string.state_fuelpurchasedate)) &&
				savedInstanceState.containsKey(getString(R.string.state_fuelpurchasetime)) )
		{
			if(_btnDateOfPurchase != null)
				_btnDateOfPurchase.setText(savedInstanceState.getString(getString(R.string.state_fuelpurchasedate)));
			if(_btnTimeOfPurchase != null)
				_btnTimeOfPurchase.setText(savedInstanceState.getString(getString(R.string.state_fuelpurchasetime)));
		}
		else
		{
			Calendar cal = Calendar.getInstance();

			if(_fp.getPurchaseDate() == null)
	    		cal.setTime(controlListener.getMyController().getCurrentClockHomeTerminalTime());
	        else
	    		cal.setTime(_fp.getPurchaseDate());
	
			updateTimeDisplay(_btnTimeOfPurchase, cal);
			updateDateDisplay(_btnDateOfPurchase, cal);
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		Log.d("FuelPurchaseType","onSaveInstanceState");
		
    	// 7/10/12 ACM - Save "date/time" boolean in state bundle
		outState.putString(getActivity().getString(R.string.state_fuelpurchasedate), _btnDateOfPurchase.getText().toString());
		outState.putString(getActivity().getString(R.string.state_fuelpurchasetime), _btnTimeOfPurchase.getText().toString());
		
		super.onSaveInstanceState(outState);		
	}
	
	protected void findControls(View v){
		_cboPurchaseType = (Spinner)v.findViewById(R.id.cboPurchaseType);
		_btnTimeOfPurchase = (Button)v.findViewById(R.id.btnTimeOfPurcahse);
		_btnDateOfPurchase = (Button)v.findViewById(R.id.btnDateOfPurchase);
		_btnOK = (Button)v.findViewById(R.id.btnOK);
		_btnCancel = (Button)v.findViewById(R.id.btnCancel);
		_btnOK.setOnClickListener(
	            new OnClickListener() {
	            	public void onClick(View v) {
	            		actionsListener.handleOKButtonClick();
	            	}
	            });

		_btnCancel.setOnClickListener(
	            new OnClickListener() {
	            	public void onClick(View v) {
	            		actionsListener.handleCancelButtonClick();
	            	}
	            });

		_btnTimeOfPurchase.setOnClickListener(
	            new OnClickListener() {
	            	public void onClick(View v) {
	            		ShowTimePickerDialog(_btnTimeOfPurchase);
	            	}
	            });
		
		_btnDateOfPurchase.setOnClickListener(
	            new OnClickListener() {
	            	public void onClick(View v) {
	            		ShowDatePickerDialog(_btnDateOfPurchase);
	            	}
	            });
	}
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
        	actionsListener = (FuelPurchaseTypeFragActions) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement FuelPurchaseTypeFragActions");
        }
        
        try{
        	controlListener = (FuelPurchaseTypeFragControllerMethods) activity;
        } catch (ClassCastException e) {
        	throw new ClassCastException(activity.toString() + " must implement FuelPurchaseTypeFragControllerMethods");
        }
    }
	
	public Spinner getPurchaseTypeSpinner(){
		if(_cboPurchaseType == null)
			_cboPurchaseType = (Spinner)getView().findViewById(R.id.cboPurchaseType);
		return _cboPurchaseType;
	}
	
	public Button getTimeOfPurchaseButton(){
		if(_btnTimeOfPurchase == null)
			_btnTimeOfPurchase = (Button)getView().findViewById(R.id.btnTimeOfPurcahse);
		return _btnTimeOfPurchase;
	}
	
	public Button getDateOfPurchaseButton(){
		if(_btnDateOfPurchase == null)
			_btnDateOfPurchase = (Button)getView().findViewById(R.id.btnDateOfPurchase);
		return _btnDateOfPurchase;
	}
}
