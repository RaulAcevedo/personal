package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.jjkeller.kmb.interfaces.IFuelPurchaseReceipt.FuelPurchaseReceiptFragActions;
import com.jjkeller.kmb.interfaces.IFuelPurchaseReceipt.FuelPurchaseReceiptFragControllerMethods;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbapi.proxydata.FuelPurchase;
import com.jjkeller.kmbui.R;

import java.text.DecimalFormat;

public class FuelPurchaseReceiptFrag extends BaseFragment{
	FuelPurchaseReceiptFragActions actionsListener;
	FuelPurchaseReceiptFragControllerMethods controlListener;
	
	private FuelPurchase _fp;
    private EditText _txtPurchasePrice;
    private EditText _txtFuelVendor;
    private EditText _txtInvoiceNumber;
    private Button _btnOK;
    private Button _btnCancel;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
        // Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.f_fuelpurchasereceipt, container, false);
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
			if(_txtPurchasePrice != null)
				_txtPurchasePrice.setText(savedInstanceState.getString(getString(R.string.state_fuelpurchaseprice)));
			if(_txtFuelVendor != null)
				_txtFuelVendor.setText(savedInstanceState.getString(getString(R.string.state_fuelpurchasevendor)));
			if(_txtInvoiceNumber != null)
				_txtInvoiceNumber.setText(savedInstanceState.getString(getString(R.string.state_fuelpurchaseinvoicenumber)));
		}
		else
		{
	        if (_fp.getFuelCost() > 0F)
	        {
	    		try
	    		{
	    			String s = new DecimalFormat("#.00").format(_fp.getFuelCost());
	    			_txtPurchasePrice.setText(s);
	    		}
	    		catch (IllegalArgumentException ex){ 
	    			
	            	Log.e("UnhandledCatch", ex.getMessage() + ": " + Log.getStackTraceString(ex));
	    		}  // Unlikely to occur, but ignore value we can't format
	        }
	        else
	        {
	            _txtPurchasePrice.setText("");
	        }
	        _txtFuelVendor.setText(_fp.getVendorName());
	        _txtInvoiceNumber.setText(_fp.getInvoiceNumber());
	
	        _txtPurchasePrice.requestFocus();
		}
	}
	
	protected void findControls(View v)
	{
		_txtPurchasePrice = (EditText)v.findViewById(R.id.txtPurchasePrice);
		_txtFuelVendor = (EditText)v.findViewById(R.id.txtFuelVendor);
		_txtInvoiceNumber = (EditText)v.findViewById(R.id.txtInvoiceNumber);
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
	}
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
        	actionsListener = (FuelPurchaseReceiptFragActions) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement FuelPurchaseReceiptFragActions");
        }
        
        try{
        	controlListener = (FuelPurchaseReceiptFragControllerMethods) activity;
        } catch (ClassCastException e) {
        	throw new ClassCastException(activity.toString() + " must implement FuelPurchaseReceiptFragControllerMethods");
        }
    }
	
	public EditText getPurchasePriceEditText(){
		if(_txtPurchasePrice == null)
			_txtPurchasePrice = (EditText)getView().findViewById(R.id.txtPurchasePrice);
		return _txtPurchasePrice;
	}
	
	public EditText getFuelVendorEditText(){
		if(_txtFuelVendor == null)
			_txtFuelVendor = (EditText)getView().findViewById(R.id.txtFuelVendor);
		return _txtFuelVendor;
	}
	
	public EditText getInvoiceNumberEditText(){
		if(_txtInvoiceNumber == null)
			_txtInvoiceNumber = (EditText)getView().findViewById(R.id.txtInvoiceNumber);
		return _txtInvoiceNumber;
	}
}
