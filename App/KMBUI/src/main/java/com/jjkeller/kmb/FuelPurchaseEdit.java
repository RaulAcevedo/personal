package com.jjkeller.kmb;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.actionbarsherlock.view.Menu;
import com.jjkeller.kmb.fragments.FuelPurchaseEditFrag;
import com.jjkeller.kmb.interfaces.IFuelPurchaseEdit.FuelPurchaseEditFragActions;
import com.jjkeller.kmb.interfaces.IFuelPurchaseEdit.FuelPurchaseEditFragControllerMethods;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmbapi.controller.FuelPurchaseController;
import com.jjkeller.kmbapi.enums.FuelUnitEnum;
import com.jjkeller.kmbui.R;

public class FuelPurchaseEdit extends BaseActivity 
								implements FuelPurchaseEditFragControllerMethods, FuelPurchaseEditFragActions{
	FuelPurchaseEditFrag _contentFrag;
    private static final String STATECODE_LIST = "AB AK AL AR AZ BC CA CO CT DC DE FL GA HI IA ID IL IN KS KY LA MA MB MD ME MI MN MO MS MT MX NB NC ND NE NF NH NJ NM NS NT NV NY OH OK ON OR PA PE PQ PR RI SC SD SK TN TX UT VA VT WA WI WV WY YT";

    //private EditText _txtAmountOfFuel;
    //private Spinner _cboUnit;
    //private EditText _txtStateCode;
    //private EditText _txtTractorNumber;
    //ArrayAdapter<String> fuelUnitAdapter;
    //private FuelPurchase _fp;

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
		loadContentFragment(new FuelPurchaseEditFrag());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		this.CreateOptionsMenu(menu, false);	
		return true;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		if(_contentFrag.getUnitSpinner() != null)
			outState.putInt(getResources().getString(R.string.state_fuelpurchaseunit), _contentFrag.getUnitSpinner().getSelectedItemPosition());
		if(_contentFrag.getAmountOfFuelEditText() != null)
			outState.putString(getResources().getString(R.string.state_fuelpurchaseamount), _contentFrag.getAmountOfFuelEditText().getText().toString());
		if(_contentFrag.getStateCodeEditText() != null)
			outState.putString(getResources().getString(R.string.state_fuelpurchasestatecode), _contentFrag.getStateCodeEditText().getText().toString());
		if(_contentFrag.getTractorNumberEditText() != null)
			outState.putString(getResources().getString(R.string.state_fuelpurchasetractornumber), _contentFrag.getTractorNumberEditText().getText().toString());
	    
	    super.onSaveInstanceState(outState);		
	}
	
	@Override
	public void setFragments()
	{
		super.setFragments();

		Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
		_contentFrag = (FuelPurchaseEditFrag)f;
	}
	
	@Override
    protected void Return(boolean success)
	{
		// Go back to the Fuel Purchase list
		this.startActivity(EditFuelPurchaseList.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
	}

	public void handleDoneButtonClick(){
		Return();
	}
	
	public void handleOKButtonClick()
	{
		if (_contentFrag.getAmountOfFuelEditText().getText().length() == 0 || _contentFrag.getStateCodeEditText().getText().length() == 0 || _contentFrag.getTractorNumberEditText().getText().length() == 0)
        {
            if (_contentFrag.getAmountOfFuelEditText().getText().length() == 0)
            {
            	_contentFrag.getAmountOfFuelEditText().requestFocus();
            }
            else if (_contentFrag.getStateCodeEditText().getText().length() == 0)
            {
            	_contentFrag.getStateCodeEditText().requestFocus();
            }
            else if (_contentFrag.getTractorNumberEditText().getText().length() == 0)
            {
            	_contentFrag.getTractorNumberEditText().requestFocus();
            }
            this.ShowMessage(this, getString(R.string.required_fields_missing));
            //this.showDialog(R.string.required_fields_missing);
            return;
        }

        float fuelAmount = 0.0F;
        try
        {
            fuelAmount = Float.parseFloat(_contentFrag.getAmountOfFuelEditText().getText().toString());
        }
        finally { }
        if (fuelAmount <= 0.0F)
        {
        	_contentFrag.getAmountOfFuelEditText().selectAll();
        	_contentFrag.getAmountOfFuelEditText().requestFocus();
        	this.ShowMessage(this, getString(R.string.msg_fuelpurchase_amount_nan));
        	//this.showDialog(R.string.msg_fuelpurchase_amount_nan);
            return;
        }

        if (fuelAmount > 9999.99F)
        {
        	_contentFrag.getAmountOfFuelEditText().selectAll();
        	_contentFrag.getAmountOfFuelEditText().requestFocus();
        	this.ShowMessage(this, getString(R.string.msg_fuelpurchase_amount_maxvalue));
        	//this.showDialog(R.string.msg_fuelpurchase_amount_maxvalue);
            return;
        }

        // Check for the state code in the list of valid codes.
        String stateCode = _contentFrag.getStateCodeEditText().getText().toString().toUpperCase();
        if(STATECODE_LIST.indexOf(stateCode) < 0)
        {
        	_contentFrag.getStateCodeEditText().selectAll();
        	_contentFrag.getStateCodeEditText().requestFocus();
        	this.ShowMessage(this, getString(R.string.msg_fuelpurchase_statecode_invalid));
            //this.showDialog(R.string.msg_fuelpurchase_statecode_invalid);
            return;
        }

//        try
//        {
            FuelUnitEnum unit = new FuelUnitEnum(FuelUnitEnum.GALLONS);
            if (_contentFrag.getUnitSpinner().getSelectedItemPosition() == 1)
                unit.setValue(FuelUnitEnum.LITERS);

            String tractorNumber = _contentFrag.getTractorNumberEditText().getText().toString();

            this.getMyController().StartFuelPurchase(fuelAmount, unit, stateCode, tractorNumber);

    		// Go to the Fuel Purchase type entry screen
    		this.startActivity(FuelPurchaseType.class);
//        }
//        catch (ApplicationException ex)
//        {
//            this.HandleException(ex);
//        }
//        catch (Exception ex)
//        {
//            this.HandleException(ex);
//        }
    }

}
