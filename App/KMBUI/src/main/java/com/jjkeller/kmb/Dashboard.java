package com.jjkeller.kmb;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.jjkeller.kmb.fragments.DashBoardFrag;
import com.jjkeller.kmb.fragments.LeftNavFrag;
import com.jjkeller.kmb.interfaces.IDashBoard.DashBoardFragActions;
import com.jjkeller.kmb.interfaces.IDashBoard.DashBoardFragControllerMethods;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.DashboardController;
import com.jjkeller.kmbapi.controller.EOBR.EobrReader;
import com.jjkeller.kmbapi.controller.share.KmbApplicationException;
import com.jjkeller.kmbapi.controller.utility.ErrorLogHelper;
import com.jjkeller.kmbapi.kmbeobr.Enums.DeviceErrorFlags;
import com.jjkeller.kmbapi.kmbeobr.StatusRecord;
import com.jjkeller.kmbui.R;

public class Dashboard extends BaseActivity 
									implements DashBoardFragActions, DashBoardFragControllerMethods,
										LeftNavFrag.OnNavItemSelectedListener, LeftNavFrag.ActivityMenuItemsListener{
	DashBoardFrag _contentFrag;
    private static final int START_STOP_ENGINE = 0x00010010;
    private static final int RESET_TRIP = 0x00080010;
    private static final int RESET_ODOMETER = 0x00100010;
    private static final int STATUS_ERROR_CLEAR = 0x80000010;
    private static final int ACCELLERATE = 0x00020010;
    private static final int DECELLERATE = 0x00040010;
    private Handler _checkEngineHandler = new Handler();
	private boolean _isEngineOn = false;


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String noDeviceMessage;
		setContentView(R.layout.dashboard);
		if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
			noDeviceMessage = getString(R.string.no_eld_available);
		} else {
			noDeviceMessage = getString(R.string.no_eobr_available);
		}
		try {
			if(!EobrReader.getIsEobrDeviceOnlineOrReadingHistory() || !EobrReader.getInstance().IsDevicePhysicallyConnected(this, false))
			{
				this.showMsg(noDeviceMessage);
				finish();
			}
			else
				new FetchLocalDataTask(this.getClass().getSimpleName()).execute();
		} catch (KmbApplicationException e) {
			this.showMsg(noDeviceMessage);
			finish();
		}
	}
	
	@Override
	public void setFragments()
	{
		super.setFragments();

		Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
		_contentFrag = (DashBoardFrag)f;
	}

	@Override
	protected void loadControls()
	{
		super.loadControls();
		loadContentFragment(new DashBoardFrag());
	}
	
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	
    	// Stop the timer when we leave the page
    	_checkEngineHandler.removeCallbacks(mCheckEngineTask);
    }
    
	public DashboardController getMyController()
	{
		return (DashboardController)this.getController();
	}
	
	@Override
	protected void InitController() {
		DashboardController dashboardCtrl = new DashboardController(this);
	
		this.setController(dashboardCtrl);	
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.clear();
		this.CreateOptionsMenu(menu, false);
		return true;
	}

	public String getActivityMenuItemList() {
		return getString(R.string.dashboard_actionitems);
	}

	private void handleMenuItemSelected(int itemPosition) {
		switch (itemPosition)
		{
			case 0:
				// Done
				Return(true);
				break;
			case 1:
				//Start/Stop Engine
				handleStartStopClick();
				break;
			case 2:
				//Change Speed
				handleChangeSpeedClick();
				break;
			case 3:
				//Reset Odometer
				handleResetOdometerClick();
				break;
			case 4:
				//Reset Trip 
				handleResetTripClick();
				break;
			case 5:
				//Set Device Error 
				handleDeviceErrorClick();
				break;
			case 6:
				// Clear Error 
				handleClearDeviceErrorClick();
				break;
			case 7:
				// Send TAB Command
				handleSendTABCommandClick();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)	{
		//See if home button was pressed
		this.GoHome(item, this.getController());
				
		handleMenuItemSelected(item.getItemId());
		super.onOptionsItemSelected(item);		
		return true;
	}

	public void onNavItemSelected(int menuItem)	{
		handleMenuItemSelected(menuItem);
	}
	
	private void handleClearDeviceErrorClick() {
        this.SendCode(STATUS_ERROR_CLEAR);
        
        // Reset device error selection
        _contentFrag.getDeviceErrorSpinner().setSelection(0);
	}

	private void handleDeviceErrorClick() {
		// Handle sending chosen error to EOBR
		if (_contentFrag.getDeviceErrorSpinner().getSelectedItemPosition() > 0)
		{
			String error = _contentFrag.getDeviceErrorSpinner().getSelectedItem().toString();
			if(error != getString(R.string.deviceerror_none))
			{
				int code = STATUS_ERROR_CLEAR;
				if(error == getString(R.string.deviceerror_gps))
					code += DeviceErrorFlags.GPS;
				else if(error == getString(R.string.deviceerror_speedometer))
					code += DeviceErrorFlags.Speedometer;
				else if(error == getString(R.string.deviceerror_odometer))
					code += DeviceErrorFlags.Odometer;
				else if(error == getString(R.string.deviceerror_memfulleobr))
					code += DeviceErrorFlags.MemFullEOBR;
				else if(error == getString(R.string.deviceerror_rtc))
					code += DeviceErrorFlags.RTC;
				else if(error == getString(R.string.deviceerror_jbus))
					code += DeviceErrorFlags.JBus;
				else if(error == getString(R.string.deviceerror_externalflash))
					code += DeviceErrorFlags.ExternalFlash;
				else if(error == getString(R.string.deviceerror_internaleobr))
					code += DeviceErrorFlags.InternalEOBR;
				else if(error == getString(R.string.deviceerror_internalflash))
					code += DeviceErrorFlags.InternalFlash;
				
				this.SendCode(code);
			}
		}
	}
	
	private Runnable mCheckEngineTask = new Runnable()
    {
        public void run() 
        {
        	_checkEngineHandler.removeCallbacks(mCheckEngineTask);
		    ProcessCheckEnginePop();
        }
    };
    
    private void ProcessCheckEnginePop()
    {
    	if (EobrReader.getInstance().isEobrGen1()) {    	
	    	boolean _isSuccessful = false;
	    	
	    	_isSuccessful = this.CheckEngine();
			
	        if (_isSuccessful)
	        {
	            if (_contentFrag.getDesiredSpeedEditText().getText().length() > 0)
	            {
	                try
	                {
	                    float desiredSpeed = Float.parseFloat(_contentFrag.getDesiredSpeedEditText().getText().toString());
	                    if (desiredSpeed > 0.0F)
	                    {
	                        if (_contentFrag.getCurrentSpeed() != desiredSpeed)
	                        {
	                            this.ChangeSpeed();
	                        }
	                    }
	                    else if (_contentFrag.getCurrentSpeed() > 0)
	                    {
	                        // trying to stop
	                        this.ChangeSpeed();
	                    }
	                }
	                catch (Exception e) {
	                	
	                	Log.e("UnhandledCatch", e.getMessage() + ": " + Log.getStackTraceString(e));
	                }
	            }
	
	            _checkEngineHandler.postDelayed(mCheckEngineTask, 1000);
	        }
    	}
    }
    
    StatusRecord _rec;
    public boolean CheckEngine()
    {
        int rc = -1;
        try
        {
            StatusRecord rec = new StatusRecord();
        	_rec = rec; 
            rc = this.getMyController().GetCurrentData(rec);
            this.HandleReturnCode(rc);
            if (rec != null && !rec.IsEmpty())
            {
            	// Make sure changes to the UI are on the right thread because this is called 
            	// from the post-delayed timer also
	    		this.runOnUiThread(new Runnable() {
	    	        public void run() {
		                _isEngineOn = _rec.getIsEngineRunning();
		                _contentFrag.setCurrentSpeed(_rec.getSpeedometerReading());
		                _contentFrag.getCurrentSpeedTextView().setText(Float.toString(_rec.getSpeedometerReading()));
		                _contentFrag.getOdometerTextView().setText(Float.toString(_rec.getOdometerReading()));
		                _contentFrag.getTachometerTextView().setText(Float.toString(_rec.getTachometer()));
		
		                if (_isEngineOn)
		                    _contentFrag.getEngineTextView().setText("running");
		                else
		                    _contentFrag.getEngineTextView().setText("off");
	    	        }					
	            });	

            }
        }
        catch (Exception excp)
        {
        	ErrorLogHelper.RecordMessage(this, "Error in CheckEngine - " + excp.getMessage());
        }

        return rc == 0;
    }
	
	public void ChangeSpeed()
    {
        try
        {
            float desiredSpeed = Float.parseFloat(_contentFrag.getDesiredSpeedEditText().getText().toString());

            if (desiredSpeed != _contentFrag.getCurrentSpeed())
            {
                if (desiredSpeed > _contentFrag.getCurrentSpeed())
                {
                    this.SendCode(ACCELLERATE);
                }
                if (desiredSpeed < _contentFrag.getCurrentSpeed())
                {
                    this.SendCode(DECELLERATE);
                }
            }
        }
        catch (Exception e) {
        	
        	Log.e("UnhandledCatch", e.getMessage() + ": " + Log.getStackTraceString(e));
        }
    }
    
    private void SendCode(int code)
    {
    	int rc = -1;
    	
        try
        {
            rc = this.getMyController().SendCode(code);
            this.HandleReturnCode(rc);
        	_checkEngineHandler.removeCallbacks(mCheckEngineTask);
        	// Accellerate & Decellerate (which are issued through ChangeSpeed) do their own postDelayed calls.
            if (rc == 0 && code != ACCELLERATE && code != DECELLERATE)
            {
        		_checkEngineHandler.postDelayed(mCheckEngineTask, 500);
            }

        }
        catch (Exception excp)
        {
    		ErrorLogHelper.RecordMessage(this, "Error in SendCode - " + excp.getMessage());
        }
    }

	private void handleResetTripClick() {
		this.SendCode(RESET_TRIP);
	}

	private void handleResetOdometerClick() {
        this.SendCode(RESET_ODOMETER);
	}

	private void handleChangeSpeedClick() {
		this.ChangeSpeed();		
		
		// Restart timer 
		_checkEngineHandler.removeCallbacks(mCheckEngineTask);
		_checkEngineHandler.postDelayed(mCheckEngineTask, 500);
	}

	private void handleStartStopClick() {
		this.SendCode(START_STOP_ENGINE);		
	}
	
	private void handleSendTABCommandClick() {
		
		try {
			String commandToIssue = _contentFrag.getDesiredTabCmdEditText().getText().toString();
			if (commandToIssue != null && commandToIssue.length() > 0) {
				this.getMyController().sendConsoleCommand(commandToIssue);
			}
		}
		catch (Exception e) {
			Log.e("UnhandledCatch", e.getMessage() + ": " + Log.getStackTraceString(e));
		}
		
	}

	
	@Override
    protected void Return(boolean success)
    {
		if(success)
		{
			if(this.getIntent().hasExtra("FromLogin"))
			{
				finish();
			}
			else
			{
				// Clear the menu in the process of returning to the RodsEntry activity.
				this.finish();
				this.startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
			}
		}        
    }
    
    public void HandleReturnCode(int rc)
    {
        if (rc != 0)
        {
        	this.showMsg("Failed with return code [" + rc + "]");
        }
    } 
      
    public void printException(String ex)
    {
    	ErrorLogHelper.RecordMessage(this, "Error in CheckEngine - " + ex);
    }
    
    public void startTimer(){
    	_checkEngineHandler.postDelayed(mCheckEngineTask, 500);
    }
}
