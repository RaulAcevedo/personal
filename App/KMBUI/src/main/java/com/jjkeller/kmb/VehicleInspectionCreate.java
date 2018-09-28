package com.jjkeller.kmb;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.jjkeller.kmb.fragments.LeftNavFrag;
import com.jjkeller.kmb.fragments.VehicleInspectionCreateFrag;
import com.jjkeller.kmb.interfaces.IVehicleInspectionCreate.VehicleInspectionCreateControllerMethods;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmbapi.controller.EOBR.EobrReader;
import com.jjkeller.kmbapi.controller.VehicleInspectionController;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.enums.InspectionDefectType;
import com.jjkeller.kmbapi.enums.InspectionTypeEnum;
import com.jjkeller.kmbapi.proxydata.EobrConfiguration;
import com.jjkeller.kmbapi.proxydata.VehicleInspection;
import com.jjkeller.kmbui.R;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class VehicleInspectionCreate extends BaseActivity
									implements VehicleInspectionCreateControllerMethods,
									LeftNavFrag.OnNavItemSelectedListener,
									LeftNavFrag.ActivityMenuItemsListener {

	VehicleInspectionCreateFrag _contentFrag;
    
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.vehicleinspectioncreate);

        Object retained = getLastNonConfigurationInstance();
        CheckSaveTaskRetention(retained);

        mFetchLocalDataTask = new FetchLocalDataTask(this.getClass().getSimpleName(), savedInstanceState);
		mFetchLocalDataTask.execute();
		
		// Since this screen doesn't load any data, handle the app
		// process being killed while on this screen here
		if (this.getMyController().getCurrentUser() == null)
		{
			this.finish();
			
            Intent loginIntent = new Intent(this, com.jjkeller.kmb.Login.class);
            loginIntent.putExtra(this.getString(R.string.restartapp), true);
       	 	this.startActivity(loginIntent);
		}
	}
	
	protected VehicleInspectionController getMyController()
	{
		return (VehicleInspectionController)this.getController();
	}
	
	@Override
	protected void InitController() {
		VehicleInspectionController ctrl = new VehicleInspectionController(this);	
		this.setController(ctrl);	
	}
	
	@Override
	protected void loadData()
	{
	}
	
	@Override
	public void setFragments()
	{
		super.setFragments();

		Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
		_contentFrag = (VehicleInspectionCreateFrag)f;
	}
	
	@Override
	protected void loadControls(Bundle savedInstanceState)
	{
		super.loadControls(savedInstanceState);
		loadContentFragment(new VehicleInspectionCreateFrag());
	}

	@Override
	protected boolean saveData() 
	{        
		this.UpdateInspection();
		boolean isSuccessful = this.getMyController().Save(getMyController().getCurrentVehicleInspection(), this);
		if(isSuccessful)
		{
			if(this.getMyController().getIsWebServicesAvailable()) 
			{				
				isSuccessful = this.getMyController().Submit(getMyController().getCurrentVehicleInspection());	
				if(!isSuccessful){
					// something bad happened while sending to DMO, could be an EOBR not linked to a unit
					runOnUiThread( new Runnable() {
				        public void run() {		
				        	VehicleInspectionCreate.this.ShowMessage(VehicleInspectionCreate.this, 0, VehicleInspectionCreate.this.getString(R.string.msg_dvirunabletosubmiterroroccurred), onOkSubmitMessage );
				        }});
				}	
			}
			else
			{
				// no network is available
				isSuccessful = false;
				runOnUiThread( new Runnable() {
			        public void run() {		
			        	VehicleInspectionCreate.this.ShowMessage(VehicleInspectionCreate.this, 0, VehicleInspectionCreate.this.getString(R.string.msg_dvirunabletosubmitnetworkunavailable), onOkSubmitMessage );
			        }});				
			}			
		}
		else {	
			// save to the database failed
			// in this case, do not close the activity when the ok button is tapped, leave the user here to manually cancel
			runOnUiThread( new Runnable() {
		        public void run() {		
		        	VehicleInspectionCreate.this.ShowMessage(VehicleInspectionCreate.this, 0, VehicleInspectionCreate.this.getString(R.string.msg_dvirunabletosave), null );
		        }});	
		}

		return isSuccessful;
	}
	
	ShowMessageClickListener onOkSubmitMessage = new ShowMessageClickListener() {
		      @Override
		      public void onClick(DialogInterface dialog, int id) {
		        VehicleInspectionCreate.this.Return(true);
		 
		        // Call super.onClick to release screen orientation lock
		        super.onClick(dialog, id);
		      }
		   };
	 
	@Override
    protected void Return(boolean success)
    {
		if(success)
		{		
			/* Display rodsentry activity */
	        Bundle extras = new Bundle();
	        
	        this.startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_CLEAR_TOP, extras);
	        this.finish();
		}  
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		this.CreateOptionsMenu(menu, false);
		return true;
	}

	public String getActivityMenuItemList()
	{
		String changeVehicle = getString(R.string.change_vehicle) + ",";
		StringBuilder sb = new StringBuilder();
		sb.append("Clear Defects,");
		if ((_contentFrag != null && _contentFrag.GetIsPoweredUnit()) || IsPoweredUnit())
			sb.append(changeVehicle);
		sb.append("Remarks,");
		sb.append("Submit,");
		sb.append("Cancel");
		return sb.toString();
	}

	public void onNavItemSelected(int itemPosition)
	{
		int menuItemIndex = -1;
		String itemText = this.getLeftNavFragment().GetNavItemText(itemPosition);

		if (itemText.equalsIgnoreCase("Clear Defects"))
			menuItemIndex = 0;
		else if (itemText.equalsIgnoreCase(getString(R.string.change_vehicle)))
			menuItemIndex = 1;
		else if (itemText.equalsIgnoreCase("Remarks"))
			menuItemIndex = 2;
		else if (itemText.equalsIgnoreCase("Submit"))
			menuItemIndex = 3;
		else if (itemText.equalsIgnoreCase("Cancel"))
			menuItemIndex = 4;

		handleNavItem(menuItemIndex);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		//See if home button was pressed
		this.GoHome(item, this.getController());
		
		int menuItemIndex = -1;
		String itemText = item.getTitle().toString();
		if (itemText.equalsIgnoreCase("Clear Defects"))
			menuItemIndex = 0;
		else if (itemText.equalsIgnoreCase(getString(R.string.change_vehicle)))
			menuItemIndex = 1;
		else if (itemText.equalsIgnoreCase("Remarks"))
			menuItemIndex = 2;
		else if (itemText.equalsIgnoreCase("Submit"))
			menuItemIndex = 3;
		else if (itemText.equalsIgnoreCase("Cancel"))
			menuItemIndex = 4;
				
		handleNavItem(menuItemIndex);
		super.onOptionsItemSelected(item);
		return true;
	}

	private void handleNavItem(int itemPosition)
	{
		switch (itemPosition)
		{
			case 0:
				// Clear Defects
				this.ClearDefectsAction();
				break;
			case 1:
				// Change Tractor
				this.ChangeTractorAction();
				break;
			case 2:
				// Remarks
				_contentFrag.ToggleRemarksPanel(_contentFrag.GetRemarksPanel().getVisibility() == View.GONE);
				break;
			case 3:
				// Submit
				this.SubmitAction();
				break;
			case 4:
				// Cancel
				this.Return();
				break;
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		//Handle the back button like a cancel during login
		if (keyCode == KeyEvent.KEYCODE_BACK && getIntent().hasExtra(this.getResources().getString(R.string.extra_tripinfomsg))) {

			Return(true);

			// Say that we've consumed the event
			return true;
		}
		
		// Otherwise let system handle keypress normally
		return super.onKeyDown(keyCode, event);
	} 
	
	private void ClearDefectsAction(){
		_contentFrag.GetSatisfactoryConditionCheckbox().setChecked(true);
		this.getMyController().RemoveAllDefects();
		_contentFrag.BindDefectList();
	}
	
	private void ChangeTractorAction(){
		_contentFrag.GetUnitNumberDropdown().setEnabled(!_contentFrag.GetUnitNumberDropdown().isEnabled());
	}

	private void SubmitAction()
	{
		if(this.IsInspectionValid()){
			mSaveLocalDataTask = new SaveLocalDataTask(this.getClass().getSimpleName(), this.getString(R.string.msg_dvirsavedialog));
			mSaveLocalDataTask.execute();
		}
	}
	
    private void UpdateInspection()
    {
        if (_contentFrag.GetIsPoweredUnit())
        {
        	// If the drop down is enabled, get the EOBR info from the drop down
        	//
        	// If the drop down is not enabled, get the EOBR info from the connected EOBR
        	Boolean isEnabled = _contentFrag.GetUnitNumberDropdown().isEnabled();
        	String tractorNumber = "";
        	String serialNumber = "";
        	if(isEnabled){
        		// Not connected to EOBR
        		EobrConfiguration eobrDevice = (EobrConfiguration)_contentFrag.GetUnitNumberDropdown().getSelectedItem();
        		tractorNumber = eobrDevice.getTractorNumber();
            	serialNumber = eobrDevice.getSerialNumber();
        	}else{
        		// Connected to EOBR
        		EobrReader eobr = EobrReader.getInstance();        
        		tractorNumber = eobr.getEobrIdentifier();
            	serialNumber = eobr.getEobrSerialNumber();
        	}
        	
        	this.getMyController().AssignEobrDevice(tractorNumber, serialNumber);
    		this.getMyController().AssignEobrOdometer();
    		
        }
        else
        {
        	if(_contentFrag.GetTrailerNbrTextbox().getText().length() > 0)
        		this.getMyController().AssignTrailerNumber(_contentFrag.GetTrailerNbrTextbox().getText().toString());
        }

        this.getMyController().getCurrentVehicleInspection().setIsConditionSatisfactory(_contentFrag.GetSatisfactoryConditionCheckbox().isChecked());
        if(_contentFrag.GetRemarksTextbox().getText().length() >0)
        	this.getMyController().getCurrentVehicleInspection().setNotes(_contentFrag.GetRemarksTextbox().getText().toString());
        else
        	this.getMyController().getCurrentVehicleInspection().setNotes(null);

    }
    
    private boolean IsInspectionValid(){
    	boolean isValid = true;
    	
    	if(!_contentFrag.GetSatisfactoryConditionCheckbox().isChecked() &&
    			this.getMyController().getCurrentVehicleInspection().getDefectList().IsEmpty()) 
    	{
    		// no defect is selected when the condition of the vehicle is not satisfactory    		
    		this.ShowMessage(this, this.getString(R.string.msg_dvirnodefects));
    		return false;
    	}
    	
    	if(!_contentFrag.GetIsPoweredUnit() && (_contentFrag.GetTrailerNbrTextbox().getText() == null || _contentFrag.GetTrailerNbrTextbox().getText().length() == 0) ){
    		// trailer number is required for trailer DVIR
    		this.ShowMessage(this, this.getString(R.string.msg_dvirnotrailernumber));
    		return false;
    	}
    	
    	if(_contentFrag.GetIsPoweredUnit() && (_contentFrag.GetUnitNumberDropdown().getSelectedItem() == null) ){
    		// tractor number is required for tractor DVIR
    		this.ShowMessage(this, this.getString(R.string.msg_dvirnotractornumber));
    		return false;
    	}
    	
    	return isValid;
    }
    
    public boolean IsPoweredUnit()
    {
    	return this.getIntent().getBooleanExtra(getString(R.string.parm_vehicleinspectionpoweredunit), true);	
    }
    
	public boolean DoesInspectionContainDefect(InspectionDefectType defect)
	{
		return this.getMyController().DoesInspectionContainDefect(defect);
	}
	public void AddDefectToInspection(InspectionDefectType defect)
	{
		this.getMyController().AddDefectToInspection(defect);
	}
	public void RemoveDefectFromInspection(InspectionDefectType defect)
	{
		this.getMyController().RemoveDefectFromInspection(defect);
	}
	public int[] GetSerializableDefectList()
	{
		return this.getMyController().GetSerializableDefectList();
	}
    public void PutSerializableDefectList(int[] defectList)
    {
    	this.getMyController().PutSerializableDefectList(defectList);
    }
    public void StartInspection(InspectionTypeEnum inspectionType, boolean isPoweredUnit)
    {
    	this.getMyController().StartInspection(inspectionType, isPoweredUnit);
    }
    public VehicleInspection getCurrentVehicleInspection()
    {
    	return this.getMyController().getCurrentVehicleInspection();
    }
    public List<EobrConfiguration> AllEobrDevices()
    {
    	return this.getMyController().AllEobrDevices();
    }
    public void AssignInspectionDate(Date inspectionDate)
    {
    	this.getMyController().AssignInspectionDate(inspectionDate);
    }

    // updates the time we display in the TextView
	public void updateDateDisplay(Button dateDialogButton, Calendar newInspectionDate)
	{
		Date inspectionDate = mergeDateTime(newInspectionDate.getTime(), _contentFrag.GetInspectionDate());
		dateDialogButton.setText(DateUtility.getDateFormat().format(inspectionDate));
		_contentFrag.SetInspectionDate(inspectionDate);
        this.AssignInspectionDate(inspectionDate);
    }
	
	@Override
	public void updateTimeDisplay(Button timeButton, Calendar newInspectionTime)
	{
		super.updateTimeDisplay(timeButton, newInspectionTime);
		Date inspectionDate = mergeDateTime(_contentFrag.GetInspectionDate(), newInspectionTime.getTime());
		_contentFrag.SetInspectionDate(inspectionDate);
        this.AssignInspectionDate(inspectionDate);
	}
	
	private Date mergeDateTime(Date date, Date time)
	{
		Calendar dateCalendar = Calendar.getInstance();
		dateCalendar.setTime(date);

		Calendar result = Calendar.getInstance();
		result.setTime(time);
		result.set(Calendar.SECOND, 0);
		result.set(Calendar.MILLISECOND, 0);
		result.set(dateCalendar.get(Calendar.YEAR), dateCalendar.get(Calendar.MONTH), dateCalendar.get(Calendar.DAY_OF_MONTH));
		return result.getTime();
	}

}
