package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.jjkeller.kmb.interfaces.IFuelPurchaseEdit.FuelPurchaseEditFragActions;
import com.jjkeller.kmb.interfaces.IFuelPurchaseEdit.FuelPurchaseEditFragControllerMethods;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbapi.enums.FuelUnitEnum;
import com.jjkeller.kmbapi.proxydata.FuelPurchase;
import com.jjkeller.kmbui.R;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class FuelPurchaseEditFrag extends BaseFragment {
	FuelPurchaseEditFragActions actionsListener;
	FuelPurchaseEditFragControllerMethods controlListener;
	
    private EditText _txtAmountOfFuel;
	private Spinner _cboUnit;
	private EditText _txtStateCode;
	private EditText _txtTractorNumber;
	ArrayAdapter<String> fuelUnitAdapter;
	private FuelPurchase _fp;
	    
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
        // Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.f_fuelpurchaseedit, container, false);
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
		_fp = controlListener.getMyController().getWorkingFuelPurchase();
		
		if(savedInstanceState != null)
		{
			if(_txtAmountOfFuel != null)
				_txtAmountOfFuel.setText(savedInstanceState.getString(getString(R.string.state_fuelpurchaseamount)));
			if(_txtStateCode != null)
				_txtStateCode.setText(savedInstanceState.getString(getString(R.string.state_fuelpurchasestatecode)));
			if(_txtTractorNumber != null)
				_txtTractorNumber.setText(savedInstanceState.getString(getString(R.string.state_fuelpurchasetractornumber)));
		}
		else
		{
			if(_fp.getFuelAmount() > 0F)
			{
				try
				{
					String s = new DecimalFormat("#").format(_fp.getFuelAmount());
					_txtAmountOfFuel.setText(s);
				}
				catch (IllegalArgumentException ex)
				{
					
		        	Log.e("UnhandledCatch", ex.getMessage() + ": " + Log.getStackTraceString(ex));
				}  // Unlikely to occur, but ignore value we can't format
				
				
			}
			
	        _txtStateCode.setText(_fp.getStateCode());
	        _txtTractorNumber.setText(_fp.getTractorNumber());
		}
		
		ArrayList<String> fuelUnitArray = new ArrayList<String>();
		fuelUnitArray.add(getString(R.string.fuelunit_gallons));
		fuelUnitArray.add(getString(R.string.fuelunit_liters));

        fuelUnitAdapter = new ArrayAdapter<String>(getActivity(), R.layout.kmb_spinner_item, fuelUnitArray);
        fuelUnitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        _cboUnit.setAdapter(fuelUnitAdapter);

		if(savedInstanceState != null)
		{
			_cboUnit.setSelection(savedInstanceState.getInt(getString(R.string.state_fuelpurchaseunit)));
		}
		else
		{
	        if(_fp.getFuelUnit() != null && _fp.getFuelUnit().getValue() == FuelUnitEnum.LITERS)
	        	_cboUnit.setSelection(1);
	        else
	        	_cboUnit.setSelection(0);
		}

        _txtAmountOfFuel.requestFocus();
	}
	
	protected void findControls(View v)
	{
		_txtAmountOfFuel = (EditText)v.findViewById(R.id.txtAmountOfFuel);
		_cboUnit = (Spinner)v.findViewById(R.id.cboUnit);
		_txtStateCode = (EditText)v.findViewById(R.id.txtStateCode);
		_txtTractorNumber = (EditText)v.findViewById(R.id.txtTractorNumber);
		
		v.findViewById(R.id.btnOK).setOnClickListener(
	            new OnClickListener() {
	            	public void onClick(View v) {
	            		actionsListener.handleOKButtonClick();
	            	}
	            });

		v.findViewById(R.id.btnCancel).setOnClickListener(
	            new OnClickListener() {
	            	public void onClick(View v) {
	            		actionsListener.handleDoneButtonClick();
	            	}
	            });
	}
	
	public Spinner getUnitSpinner(){
		if(_cboUnit == null)
			_cboUnit = (Spinner)getView().findViewById(R.id.cboUnit);
		return _cboUnit;
	}
	
	public EditText getAmountOfFuelEditText(){
		if(_txtAmountOfFuel == null)
			_txtAmountOfFuel = (EditText)getView().findViewById(R.id.txtAmountOfFuel);
		return _txtAmountOfFuel;
	}
	
	public EditText getStateCodeEditText(){
		if(_txtStateCode == null)
			_txtStateCode = (EditText)getView().findViewById(R.id.txtStateCode);
		return _txtStateCode;
	}
	
	public EditText getTractorNumberEditText(){
		if(_txtTractorNumber == null)
			_txtTractorNumber = (EditText)getView().findViewById(R.id.txtTractorNumber);
		return _txtTractorNumber;
	}
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
        	actionsListener = (FuelPurchaseEditFragActions) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement FuelPurchaseEditFragActions");
        }
        
        try{
        	controlListener = (FuelPurchaseEditFragControllerMethods) activity;
        } catch (ClassCastException e) {
        	throw new ClassCastException(activity.toString() + " must implement FuelPurchaseEditFragControllerMethods");
        }
    }
}
