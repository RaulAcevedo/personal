package com.jjkeller.kmb;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.actionbarsherlock.view.Menu;
import com.jjkeller.kmb.fragments.FuelPurchaseReceiptFrag;
import com.jjkeller.kmb.interfaces.IFuelPurchaseReceipt.FuelPurchaseReceiptFragActions;
import com.jjkeller.kmb.interfaces.IFuelPurchaseReceipt.FuelPurchaseReceiptFragControllerMethods;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmbapi.controller.FuelPurchaseController;
import com.jjkeller.kmbui.R;

public class FuelPurchaseReceipt extends BaseActivity 
									implements FuelPurchaseReceiptFragControllerMethods, FuelPurchaseReceiptFragActions{
	FuelPurchaseReceiptFrag _contentFrag;
    private float _price;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.baselayout);
	
		mFetchLocalDataTask = new FetchLocalDataTask(this.getClass().getSimpleName(), savedInstanceState);
		mFetchLocalDataTask.execute();
	}
	
	public FuelPurchaseController getMyController()
	{
		return (FuelPurchaseController)this.getController();
	}
	
	@Override
	protected void InitController() {
		FuelPurchaseController fuelPurchaseCtrl = new FuelPurchaseController(this);
	
		this.setController(fuelPurchaseCtrl);	
	}
	
	@Override
	protected void loadControls(Bundle savedIntanceState)
	{
		super.loadControls();		
		loadContentFragment(new FuelPurchaseReceiptFrag());
	}

	@Override
	public void setFragments()
	{
		super.setFragments();

		Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
		_contentFrag = (FuelPurchaseReceiptFrag)f;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		this.CreateOptionsMenu(menu, false);	
		return true;
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		if(_contentFrag.getPurchasePriceEditText() != null)
			outState.putString(getResources().getString(R.string.state_fuelpurchaseprice), _contentFrag.getPurchasePriceEditText().getText().toString());
		if(_contentFrag.getFuelVendorEditText() != null)
			outState.putString(getResources().getString(R.string.state_fuelpurchasevendor), _contentFrag.getFuelVendorEditText().getText().toString());
		if(_contentFrag.getInvoiceNumberEditText() != null)
			outState.putString(getResources().getString(R.string.state_fuelpurchaseinvoicenumber), _contentFrag.getInvoiceNumberEditText().getText().toString());
	    
	    super.onSaveInstanceState(outState);		
	}
	
	@Override
    protected void Return(boolean success)
	{
		// Go back to the Fuel Purchase list
		this.startActivity(EditFuelPurchaseList.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
	}

	public void handleCancelButtonClick(){
		Return();
	}
	
	public void handleOKButtonClick()
	{
        if (_contentFrag.getPurchasePriceEditText().getText().length() == 0 || _contentFrag.getInvoiceNumberEditText().getText().length() == 0)
        {
            if (_contentFrag.getPurchasePriceEditText().getText().length() == 0)
            {
            	_contentFrag.getPurchasePriceEditText().requestFocus();
            }
            else if (_contentFrag.getInvoiceNumberEditText().getText().length() == 0)
            {
            	_contentFrag.getInvoiceNumberEditText().requestFocus();
            }
            this.ShowMessage(this, getString(R.string.required_fields_missing));
            return;
        }

        _price = 0.0F;
        if (_contentFrag.getPurchasePriceEditText().getText().length() > 0)
        {
            try
            {
                _price = Float.parseFloat(_contentFrag.getPurchasePriceEditText().getText().toString());
            }
            finally { }
            if (_price <= 0.0F)
            {
                _contentFrag.getPurchasePriceEditText().selectAll();
                _contentFrag.getPurchasePriceEditText().requestFocus();
                this.ShowMessage(this, getString(R.string.msg_fuelreceipt_price_nan));
                return;
            }
        }

        if (_price > 9999.99F)
        {
            _contentFrag.getPurchasePriceEditText().selectAll();
            _contentFrag.getPurchasePriceEditText().requestFocus();
            this.ShowMessage(this, getString(R.string.msg_fuelreceipt_price_maxvalue));
            return;
        }

        mSaveLocalDataTask = new SaveLocalDataTask(this.getClass().getSimpleName());
        mSaveLocalDataTask.execute();
    }
	
	@Override
	protected boolean saveData()
	{
		boolean isSuccessful = false;
        this.getMyController().AddFuelReceipt(_price, _contentFrag.getFuelVendorEditText().getText().toString(), _contentFrag.getInvoiceNumberEditText().getText().toString());
		isSuccessful = true;
		return isSuccessful;
	}

}
