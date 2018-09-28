package com.jjkeller.kmb;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.actionbarsherlock.view.Menu;
import com.jjkeller.kmb.fragments.FuelPurchaseTypeFrag;
import com.jjkeller.kmb.interfaces.IFuelPurchaseType.FuelPurchaseTypeFragActions;
import com.jjkeller.kmb.interfaces.IFuelPurchaseType.FuelPurchaseTypeFragControllerMethods;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmbapi.common.TimeKeeper;
import com.jjkeller.kmbapi.controller.FuelPurchaseController;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.enums.FuelClassificationEnum;
import com.jjkeller.kmbui.R;

import java.text.ParseException;
import java.util.Date;

public class FuelPurchaseType extends BaseActivity 
								implements FuelPurchaseTypeFragControllerMethods, FuelPurchaseTypeFragActions{
	FuelPurchaseTypeFrag _contentFrag;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.baselayout);
	
		mFetchLocalDataTask = new FetchLocalDataTask(this.getClass().getSimpleName(), savedInstanceState);
		mFetchLocalDataTask.execute();
	}
	
	public FuelPurchaseController getMyController(){
		return (FuelPurchaseController)this.getController();
	}
	
	@Override
	protected void InitController() {
		FuelPurchaseController fuelPurchaseCtrl = new FuelPurchaseController(this);
	
		this.setController(fuelPurchaseCtrl);	
	}
	
	@Override
	protected void loadControls(Bundle savedIntanceState){
		super.loadControls();		
		loadContentFragment(new FuelPurchaseTypeFrag());
	}
	
	@Override
	public void setFragments(){
		super.setFragments();

		Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
		_contentFrag = (FuelPurchaseTypeFrag)f;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		this.CreateOptionsMenu(menu, false);	
		return true;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		if(_contentFrag.getPurchaseTypeSpinner() != null)
			outState.putInt(getResources().getString(R.string.state_fuelpurchasetype), _contentFrag.getPurchaseTypeSpinner().getSelectedItemPosition());
		if(_contentFrag.getDateOfPurchaseButton() != null)
			outState.putString(getResources().getString(R.string.state_fuelpurchasedate), _contentFrag.getDateOfPurchaseButton().getText().toString());
		if(_contentFrag.getTimeOfPurchaseButton() != null)
			outState.putString(getResources().getString(R.string.state_fuelpurchasetime), _contentFrag.getTimeOfPurchaseButton().getText().toString());
		
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
        FuelClassificationEnum classification = new FuelClassificationEnum(FuelClassificationEnum.NULL);
        switch (_contentFrag.getPurchaseTypeSpinner().getSelectedItemPosition())
        {
            case 0:
                classification.setValue(FuelClassificationEnum.RECEIPTED);
                break;
            case 1:
                classification.setValue(FuelClassificationEnum.NONRECEIPTED);
                break;
            case 2:
                classification.setValue(FuelClassificationEnum.TAXPAIDBULK);
                break;
            case 3:
                classification.setValue(FuelClassificationEnum.TAXNOTPAIDBULK);
                break;
            case 4:
                classification.setValue(FuelClassificationEnum.REEFER);
                break;
        }

        Date purchaseDate = TimeKeeper.getInstance().now();
		try {
			purchaseDate = DateUtility.getHomeTerminalDateTimeFormat12Hour().parse(_contentFrag.getDateOfPurchaseButton().getText().toString() + " " + _contentFrag.getTimeOfPurchaseButton().getText().toString());
		} catch (ParseException e) {
			
        	Log.e("UnhandledCatch", e.getMessage() + ": " + Log.getStackTraceString(e));
		}
		
        this.getMyController().AddFuelTypeToPurchase(classification, purchaseDate);

        if (_contentFrag.getPurchaseTypeSpinner().getSelectedItemPosition() == 0)
        {
    		// Go to the Fuel Purchase receipt entry screen
    		this.startActivity(FuelPurchaseReceipt.class);
        }
        else
        {
        	// For types other than "Receipted", just save
        	mSaveLocalDataTask = new SaveLocalDataTask(this.getClass().getSimpleName());
        	mSaveLocalDataTask.execute();
        }
    }
	
	// Used to set TimeDialog in BaseActivity
	public void setTimeDialogButton(){
		mTimeDialogButton = _contentFrag.getTimeOfPurchaseButton();
	}
	
	// Used to set DateDialog in BaseActivity
	public void setDateDialogButton(){
		mDateDialogButton = _contentFrag.getDateOfPurchaseButton();
	}
	
	@Override
	protected boolean saveData()
	{
		boolean isSuccessful = false;
        this.getMyController().SaveFuelPurchase();
		isSuccessful = true;
		return isSuccessful;
	}
}
