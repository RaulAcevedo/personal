package com.jjkeller.kmb;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.jjkeller.kmb.fragments.LeftNavFrag;
import com.jjkeller.kmb.fragments.VehicleInspectionReviewFrag;
import com.jjkeller.kmb.interfaces.IVehicleInspectionReview.VehicleInspectionReviewFragActions;
import com.jjkeller.kmb.interfaces.IVehicleInspectionReview.VehicleInspectionReviewFragControllerMethods;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmbapi.controller.VehicleInspectionController;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.controller.utility.ErrorLogHelper;
import com.jjkeller.kmbapi.proxydata.EobrConfiguration;
import com.jjkeller.kmbapi.proxydata.VehicleInspection;
import com.jjkeller.kmbapi.proxydata.VehicleInspectionDefect;
import com.jjkeller.kmbui.R;

import java.util.List;

public class VehicleInspectionReview extends BaseActivity 
										implements VehicleInspectionReviewFragActions, VehicleInspectionReviewFragControllerMethods,
										LeftNavFrag.ActivityMenuItemsListener, LeftNavFrag.OnNavItemSelectedListener{
	VehicleInspectionReviewFrag _contentFrag;
	private static final int TRACTOR = 0;
	private static final int TRAILER = 1;
	private static final int PRETRIP = 0;
	private static final int POSTTRIP = 1;
	private int _currentSelectedInspection = -1;
	
	VehicleInspection _currentInspection = null;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.vehicleinspectionreview);
		
		mFetchLocalDataTask = new FetchLocalDataTask(this.getClass().getSimpleName(), savedInstanceState);
		mFetchLocalDataTask.execute();
	}
	
	
	public VehicleInspectionController getMyController()
	{
		return (VehicleInspectionController)this.getController();
	}
	
	@Override
	protected void InitController() {
		VehicleInspectionController inspectionCtrl = new VehicleInspectionController(this);
	
		this.setController(inspectionCtrl);	
	}
	
	@Override
	public void setFragments(){
		super.setFragments();
		
		Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
		_contentFrag = (VehicleInspectionReviewFrag)f;
	}
	
	@Override
	protected void loadControls(Bundle savedInstanceState)
	{		
		super.loadControls(savedInstanceState);
		loadContentFragment(new VehicleInspectionReviewFrag());
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {	
		this.CreateOptionsMenu(menu, false);
		return true;
	}

	public int getCurrentSelectedInspection(){
		return _currentSelectedInspection;
	}

	public String getActivityMenuItemList()
	{
		if(_currentInspection != null) {
			if (_currentInspection.getCertifiedByDate() != null)
				return this.getString(R.string.vehicleinspectionreview_nosubmit);
			else
				return this.getString(R.string.vehicleinspectionreview_actionitems);
		}else
			return this.getString(R.string.vehicleinspectionreview_nosubmit);
	}

	private void handleMenuItemSelected(int itemPosition)
	{	
		switch (itemPosition)
		{
			case 0:
				// Change Tractor
				this.ClearInspectionDisplay();
				if (_contentFrag.getUnitNumberSpinner().getVisibility() == View.GONE)
					_contentFrag.ShowInspectionForVehicleType(TRACTOR);
				else
					_contentFrag.getUnitNumberSpinner().setEnabled(true);
				break;
			case 1:
				// Switch to Trailer
				this.ClearInspectionDisplay();
				_contentFrag.ShowInspectionForVehicleType(TRAILER);
				break;
			case 2:
				// Download
				this.ClearInspectionDisplay();
				this.DownloadAction();
				break;
			case 3:
				if(_currentInspection != null)
				{
					// Certify and Submit
					this.SubmitAction();
				}
				else
					finishActivity();
				
				break;
			case 4:
				// Close
				finishActivity();
				break;
		}
	}

	private void finishActivity()
	{
		this.finish();
		this.startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
	}
	
	public void onNavItemSelected(int menuItem)
	{
		handleMenuItemSelected(menuItem);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		//See if home button was pressed
		this.GoHome(item, this.getController());
		
		handleMenuItemSelected(item.getItemId());
		super.onOptionsItemSelected(item);
		return true;
	}
	
	private void ShowRecentInspections()
	{	
		this.ClearInspectionDisplay();
        VehicleInspection inspection = this.getMyController().getCurrentVehicleInspection();
        VehicleInspection preinspection = this.getMyController().getCurrentVehiclePreInspection();
        
       	if (inspection == null && preinspection == null)
        {
            this.ShowMessage(this.getMyController().getContext(), this.getString(R.string.msg_noinspectionfound));
            return;
        }
        
       	if (inspection != null)
       	{
       		_contentFrag.get_PostTripDateTextView()
       					.setText(DateUtility.getHomeTerminalDateTimeFormat12Hour().format(inspection.getInspectionTimeStamp()));	
       		_contentFrag.getPostTripPanel().setVisibility(View.VISIBLE);
       	}
       	
       	if (preinspection != null)
       	{
            _contentFrag.get_PreTripDateTextView()
            			.setText(DateUtility.getHomeTerminalDateTimeFormat12Hour().format(preinspection.getInspectionTimeStamp()));         
            _contentFrag.getPreTripPanel().setVisibility(View.VISIBLE);	
       	}         
	}
		
	private void showInspection(int inspectiontype)
	{
		this.ClearInspectionDisplay();
		
		_currentInspection = (inspectiontype == POSTTRIP) 
				? this.getMyController().getCurrentVehicleInspection() 
				: this.getMyController().getCurrentVehiclePreInspection();

		_currentSelectedInspection = (inspectiontype == POSTTRIP)
				? POSTTRIP
				: PRETRIP;
	
		//rebuild the menu to get the certify and submit to show
		getLeftNavFragment().BuildMenu();
				
        if (_currentInspection == null)
        {
            this.ShowMessage(this.getMyController().getContext(), this.getString(R.string.msg_noinspectionfound));
                       
            return;
        }
        
        _contentFrag.getInspectionDateTextView()
        			.setText(DateUtility.getHomeTerminalDateTimeFormat12Hour().format(_currentInspection.getInspectionTimeStamp()));
        _contentFrag.getDatePanel().setVisibility(View.VISIBLE);         
        
        if (_currentInspection.getIsConditionSatisfactory())
        { 
        	_contentFrag.getSatisfactoryPanel().setVisibility(View.VISIBLE);
        	_contentFrag.getDefectsPanel().setVisibility(View.INVISIBLE);
        }
        else
        { 
        	_contentFrag.getSatisfactoryPanel().setVisibility(View.INVISIBLE);
        	_contentFrag.getDefectsPanel().setVisibility(View.VISIBLE);
        	
            if (_currentInspection.getAreDefectsCorrected())
            {
            	_contentFrag.getCorrectionsMadeTextView().setText(getString(R.string.btnyes));
            }
            else
            {
            	_contentFrag.getCorrectionsMadeTextView().setText(getString(R.string.btnno));
            }
            
            _contentFrag.getCertifiedByTextView().setText(_currentInspection.getCertifiedByName());
            
            if (_currentInspection.getCertifiedByDate() == null)
            {
            	_contentFrag.getCertifyDateTextView().setText("");
            }
            else
            {
            	_contentFrag.getCertifyDateTextView()
            				.setText(DateUtility.getHomeTerminalDateFormat().format(_currentInspection.getCertifiedByDate()));
            }

            if (_currentInspection.getAreCorrectionsNotNeeded())
            {
            	_contentFrag.getNoCorrectionsNeededTextView().setVisibility(View.VISIBLE);
            	_contentFrag.getDefectsLabel().setVisibility(View.INVISIBLE);
            	_contentFrag.getDefectsListTextView().setVisibility(View.INVISIBLE);
            }
            else
            {
            	_contentFrag.getNoCorrectionsNeededTextView().setVisibility(View.INVISIBLE);
            	_contentFrag.getDefectsLabel().setVisibility(View.VISIBLE);
            	_contentFrag.getDefectsListTextView().setVisibility(View.VISIBLE);
                this.ShowDefectList(_currentInspection);
            }
        }
	}
	
	private void ShowDefectList(VehicleInspection inspection)
	{
		VehicleInspectionDefect[] defectList = inspection.getDefectList().getDefectList();
		
        if (defectList != null && defectList.length > 0)
        {
            StringBuilder sb = new StringBuilder();
            
            for (VehicleInspectionDefect vehicleInspectionDefect : defectList) {
            	String displayItem = vehicleInspectionDefect.getInspectionDefectType().getString(this);
            	
            	if(sb.length() > 0) sb.append(", ");
            		sb.append(displayItem);
			}
            
            _contentFrag.getDefectsListTextView().setText(sb.toString());            
        }
        else
        {
        	_contentFrag.getDefectsListTextView().setText("");
        }
	}
	
	private void ClearInspectionDisplay(){
		//reset the current inspection being reviewed
		_currentInspection = null;
		getLeftNavFragment().BuildMenu();
		
		_contentFrag.getDefectsPanel().setVisibility(View.GONE);
		_contentFrag.getSatisfactoryPanel().setVisibility(View.GONE);
		_contentFrag.getDatePanel().setVisibility(View.GONE);
		_contentFrag.getPostTripPanel().setVisibility(View.GONE);	
		_contentFrag.getPreTripPanel().setVisibility(View.GONE);	
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		outState.putInt("InspectionVehicleType", _contentFrag.getVehicleTypeInt());
		super.onSaveInstanceState(outState);
	}
	
	public void DownloadAction()
	{
        if (_contentFrag.getVehicleTypeInt() == TRAILER &&
        		(_contentFrag.getTrailerNumberEditText().getText() == null || _contentFrag.getTrailerNumberEditText().getText().length() == 0))
        {
    		// trailer number is required for trailer DVIR
    		this.ShowMessage(this, this.getString(R.string.msg_dvirnotrailernumber));
        }
        else
        {
        	mDownloadDVIRTask = new DownloadDVIRTask();
        	mDownloadDVIRTask.execute();
        }
	}	
	
	public void handleViewPostButtonClick(Context ctx)
	{
		showInspection(POSTTRIP);
	}

	public void showSelectedInspection(int inspectionType){
		showInspection(inspectionType);
	}
	
	public void handleViewPreButtonClick(Context ctx)
	{
		showInspection(PRETRIP);
	}
	
	protected boolean saveData() 
	{        
		this.getMyController().CertifyAsCorrected(_currentInspection);
		boolean isSuccessful = this.getMyController().Save(_currentInspection, this);
		if(isSuccessful)
		{
//			if(this.getMyController().getIsNetworkAvailable()) 
			if(this.getMyController().getIsWebServicesAvailable()) 
			{				
				isSuccessful = this.getMyController().Submit(_currentInspection);	
				if(!isSuccessful){
					// something bad happened while sending to DMO, could be an EOBR not linked to a unit
					runOnUiThread( new Runnable() {
				        public void run() {		
				        	VehicleInspectionReview.this.ShowMessage(VehicleInspectionReview.this, 0, VehicleInspectionReview.this.getString(R.string.msg_dvirunabletosubmiterroroccurred), onOkSubmitMessage );
				        }});
				}	
			}
			else
			{
				// no network is available
				isSuccessful = false;
				runOnUiThread( new Runnable() {
			        public void run() {		
			        	VehicleInspectionReview.this.ShowMessage(VehicleInspectionReview.this, 0, VehicleInspectionReview.this.getString(R.string.msg_dvirunabletosubmitnetworkunavailable), onOkSubmitMessage );
			        }});				
			}			
		}
		else {	
			// save to the database failed
			// in this case, do not close the activity when the ok button is tapped, leave the user here to manually cancel
			runOnUiThread( new Runnable() {
		        public void run() {		
		        	VehicleInspectionReview.this.ShowMessage(VehicleInspectionReview.this, 0, VehicleInspectionReview.this.getString(R.string.msg_dvirunabletosave), null );
		        }});	
		}

		return isSuccessful;
	}
	
	ShowMessageClickListener onOkSubmitMessage = new ShowMessageClickListener() {
		      @Override
		      public void onClick(DialogInterface dialog, int id) {
		    	  VehicleInspectionReview.this.Return(true);
		 
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

	private void SubmitAction()
	{		
		if(_currentInspection == null)
		{
			this.ShowMessage(this, this.getString(R.string.msg_dvirunabletosubmitnoinspectionfound));
		}
		else
		{
			mSaveLocalDataTask = new SaveLocalDataTask(this.getClass().getSimpleName(), this.getString(R.string.msg_dvirsavedialog));
			mSaveLocalDataTask.execute();
		}
	}
	
	private static DownloadDVIRTask mDownloadDVIRTask;
	private class DownloadDVIRTask extends AsyncTask<Void, Void, Boolean> {
		ProgressDialog pd;
		VehicleInspection inspection = null;
		VehicleInspection preinspection = null;
		
		protected void onPreExecute()
		{
			LockScreenRotation();
			showProgressDialog();
		}
		
		protected Boolean doInBackground(Void... params) {
			boolean isSuccessful = false;
			
			try
			{
//		        if (_cboUnitNbr.getVisibility() == View.VISIBLE)
				if (_contentFrag.getVehicleTypeInt() == TRACTOR)
				{
		        	EobrConfiguration selectedEobrDevice = null;
		    		List<EobrConfiguration> allEobrDevices = getMyController().AllEobrDevices();
		        	
			    	// there is an online EOBR, attempt to auto-download that inspection
					for(EobrConfiguration item : allEobrDevices)
					{
						if(item.getTractorNumber().contains(_contentFrag.getUnitNumberSpinner().getSelectedItem().toString()))
							selectedEobrDevice = item;
					}

					inspection = getMyController().DownloadRecentInspectionFor(selectedEobrDevice);
			    	getMyController().setCurrentVehicleInspection(inspection);
			    	
			    	preinspection = getMyController().DownloadRecentPreInspectionFor(selectedEobrDevice);
			    	getMyController().ReviewRecentTractorPreInspectionFor(selectedEobrDevice);
		        }
		        else
		        {
	            	String trailerNbr = _contentFrag.getTrailerNumberEditText().getText().toString();
	            	inspection = getMyController().DownloadRecentTrailerInspection(trailerNbr);
	            	getMyController().ReviewRecentTrailerInspectionFor(trailerNbr);            		
	            	
	            	preinspection = getMyController().DownloadRecentTrailerPreInspection(trailerNbr);
	            	getMyController().ReviewRecentTrailerPreInspectionFor(trailerNbr);            		
		        }
		        
		        isSuccessful = true;
			}
			catch(Throwable e){
				ErrorLogHelper.RecordException(VehicleInspectionReview.this, e);
			}

			return isSuccessful;
		}

        protected void onPostExecute(Boolean isSuccessful) {
        	dismissProgressDialog();
			UnlockScreenRotation();

			if(inspection == null)
				getMyController().setCurrentVehicleInspection(null);
			
			if(preinspection == null)
				getMyController().setCurrentVehiclePreInspection(null);

			ShowRecentInspections();
        }
        
		// Added public methods so that dialogs and context can be re-established 
		// after an orientation change (ie. activity recreated).
        public void showProgressDialog()
        {
        	pd = CreateFetchDialog(getString(R.string.msg_dvirdownloadinginspection));
        }
        
        public void dismissProgressDialog()
        {
        	DismissProgressDialog(VehicleInspectionReview.this, this.getClass(), pd);
        }
	}
}
