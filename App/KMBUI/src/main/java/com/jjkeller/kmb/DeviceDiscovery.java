package com.jjkeller.kmb;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.jjkeller.kmb.fragments.DeviceDiscoveryFrag;
import com.jjkeller.kmb.fragments.LeftNavFrag;
import com.jjkeller.kmb.interfaces.IDeviceDiscovery.DeviceDiscoveryActions;
import com.jjkeller.kmb.interfaces.IDeviceDiscovery.DeviceDiscoveryControllerMethods;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmb.share.ReleaseDeviceTask;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.EOBR.EobrReader;
import com.jjkeller.kmbapi.controller.LogEntryController;
import com.jjkeller.kmbapi.controller.interfaces.IAPIController;
import com.jjkeller.kmbapi.controller.SystemStartupController;
import com.jjkeller.kmbapi.controller.share.KmbApplicationException;
import com.jjkeller.kmbapi.controller.share.MandateObjectFactory;
import com.jjkeller.kmbapi.controller.utility.ErrorLogHelper;
import com.jjkeller.kmbapi.controller.utility.RESTWebServiceHelper;
import com.jjkeller.kmbapi.eobrengine.EobrDeviceDescriptor;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbui.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DeviceDiscovery extends BaseActivity
					implements DeviceDiscoveryActions, DeviceDiscoveryControllerMethods,
					LeftNavFrag.OnNavItemSelectedListener, LeftNavFrag.ActivityMenuItemsListener, ReleaseDeviceTask.ITaskHost {

	DeviceDiscoveryFrag _contentFrag;
	
	protected boolean _discovering = false;
	protected boolean _receiversRegistered = false;
	protected boolean _initialDiscoveryPerformed = false;
	protected List<BluetoothDevice> _gen1Devices;
	protected List<BluetoothDevice> _gen2Devices;
	protected BluetoothAdapter _btAdapter;
	private final int BT_DISCOVERY_DELAY = 1000;
	protected ProgressDialog _pd;
	private Handler mHandler = new Handler();
	protected boolean _loginProcess = false;
	private EmployeeLog _employeeLog = null;

	String _newEobrName = "";
	String _newEobrAddress = "";
	
	private int _discTotalCnt = 0;
	private int _discAttemptCnt = 0;
    private final int DISCOVERY_MAX_TOTAL_COUNT = 60;
    private final int DISCOVERY_MAX_ATTEMPT_COUNT = 10;	
    private static final int REQUEST_ENABLE_BT = 1;
    
    private static final int GEN1_BTCLASS = 0x1f00;
    private static final int GEN2_PAN_BTCLASS = 0x0000;
    private static final int GEN2_RVN_BTCLASS = 0x0704;
	private String multipleDeviceMessage;
	private String defaultDeviceString;
	private String setDefaultDeviceString;
	private String serialNumberMissingString;
    
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.baselayout);
		if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
			defaultDeviceString = getString(R.string.lbldefaulteld);
			setDefaultDeviceString = getString(R.string.set_default_eld);
			multipleDeviceMessage = getString(R.string.msg_multipleeldsdetected);
			serialNumberMissingString = getString(R.string.msgEldSerialNumberMissing);
		} else {
			defaultDeviceString = getString(R.string.lbldefaulteobrcolon);
			setDefaultDeviceString = getString(R.string.set_default_eobr);
			multipleDeviceMessage = getString(R.string.msg_multipleeobrsdetected);
			serialNumberMissingString = getString(R.string.msgEobrSerialNumberMissing);
		}
    	_loginProcess = getIntent().hasExtra(this.getResources().getString(R.string.extra_isloginprocess));

    	// 11/14/11 JHM - Check for "discovery" boolean in state bundle
    	if(savedInstanceState != null && savedInstanceState.containsKey("initialdiscovercomplete"))
    		_initialDiscoveryPerformed = savedInstanceState.getBoolean("initialdiscovercomplete"); 
    	
    	if(savedInstanceState == null)
    	{
    		mFetchLocalDataTask = new FetchLocalDataTask(this.getClass().getSimpleName(), savedInstanceState);
			// 11/14/11 JHM - Lock screen rotation if we need to do discovery
			if(_loginProcess && !_initialDiscoveryPerformed)
				mFetchLocalDataTask.setAutoUnlockScreenRotationOnPostExecute(false);
			else
				mFetchLocalDataTask.setAutoUnlockScreenRotationOnPostExecute(true);
			mFetchLocalDataTask.execute();	
    	}

    	try {
    		// if not online to eobr, attempt to discover paired devices
			if (!this.getMyController().IsEobrDeviceOnline())
			{
				if(!_initialDiscoveryPerformed){
					DeviceDiscovery.this.LockScreenRotation();
					mDiscoverPairedDeviceTask = new DiscoverPairedDeviceTask(this.getClass().getSimpleName(), _pd);
					mDiscoverPairedDeviceTask.execute();
				}
			}
			else
			{
				// note: since there is already a device online, this is means that we don't need to discover
				_initialDiscoveryPerformed = true;
			}
		} catch (KmbApplicationException e) {
			// if exception occurred checking if currently online to eobr, do nothing - will not try
			// to identify paired devices
        	Log.e("UnhandledCatch", e.getMessage() + ": " + Log.getStackTraceString(e));
		}
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		
		try
		{
			if(_pd != null && _pd.isShowing()) _pd.dismiss();
		}
		finally{}
		
		mHandler.removeCallbacks(mDiscoveryHandler);
		if(_receiversRegistered)
			this.unregisterReceiver(mReceiver);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if(_loginProcess)
		{
			//disable the back button if this is in the login process
			if (keyCode == KeyEvent.KEYCODE_BACK)
			{
			    return true;
			}
		}
		
		return super.onKeyDown(keyCode, event);
	}
	
	protected SystemStartupController getMyController()
	{
		return (SystemStartupController)this.getController();
	}
	
	@Override
	protected void InitController() {
		SystemStartupController startupCtrl = new SystemStartupController(this);
	
		this.setController(startupCtrl);	
	}
	
	@Override
    protected void Return(boolean success)
    {
		if(success)
		{			
			// Clear the menu in the process of returning to the RodsEntry activity.
			if(_loginProcess) {
				this.startActivity(SelectDutyStatus.class, Intent.FLAG_ACTIVITY_CLEAR_TOP); //PBI 49820
			}
			else {
				this.startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
			}

		}
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		if (!_loginProcess) {
			this.CreateOptionsMenu(menu, false);
		}

		return true;
	}
	
	public String getActivityMenuItemList() {
		return getString(R.string.btndone);
	}

	private void handleMenuItemSelected(int itemPosition)	{
		if (itemPosition == 0)
			this.Return();
	}

	public void onNavItemSelected(int menuItem) {	 
		handleMenuItemSelected(menuItem);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {		
		//See if home button was pressed
		this.GoHome(item, this.getController());
				
		handleMenuItemSelected(item.getItemId());
		super.onOptionsItemSelected(item);
		return true;
	}
	
	@Override
	protected void loadControls(Bundle savedInstanceState)
	{
		loadContentFragment(new DeviceDiscoveryFrag());

		if(!_loginProcess) 
			loadLeftNavFragment();
		else
		{
			View leftNavLayout = findViewById(R.id.leftnav_fragment);
			if(leftNavLayout != null) leftNavLayout.setVisibility(View.GONE);
		}
		setFragments();
    }

	@Override
	public void setFragments()
	{
		super.setFragments();

		Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
		_contentFrag = (DeviceDiscoveryFrag)f;
	}
	
	private ProgressDialog CreateDiscoveringDialog()
	{
		if(this.isFinishing())
			return null;
		else
			return ProgressDialog.show(this, "", getString(R.string.lbldiscovering));
	}
	
	private void RegisterReceivers()
	{
		if(!_receiversRegistered){
	    	IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
	    	this.registerReceiver(mReceiver, filter);
	    	
	    	filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
	    	this.registerReceiver(mReceiver, filter);
	
	    	filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
	    	this.registerReceiver(mReceiver, filter);
	    	
	    	_receiversRegistered = true;
		}
	}
	
	public void handleDiscoverButtonClick() {
		LockScreenRotation();
		
		// company config is BT only
    	_btAdapter = BluetoothAdapter.getDefaultAdapter();
		
        if (_btAdapter == null || !_btAdapter.isEnabled())
		{
            // BT is not on, request that it be enabled.
        	this.RegisterReceivers();
        	
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // 2011.11.22 SJN note:
            //       because BT is not turned on, need to wait for the BT adapter
            //       to turn on.  There is a BluetoothAdapter.ACTION_STATE_CHANGED action
            //       that will be sent when the adapter turns on.
		}
        else{
        	this.InitiateDiscovery();
        }
	}

	private void InitiateDiscovery() {
        // determine if a mac adddress activation is possible due to use of default eobr
		SharedPreferences userPref = getSharedPreferences(getString(R.string.sharedpreferencefile), 0);
		String defaultEobr = userPref.getString(getString(R.string.defaulteobr), "");
		String defaultMacAddress = userPref.getString(getString(R.string.macaddress), "");
        
		// TCH - 4/10/2012 - Provisioning a new EOBR should take precedence over discovery of default EOBR
		// by MAC address when both options are checked.  If the passkey was changed in DMO and trying to
		// get the new passkey to the EOBR, if both options are checked, discovery was done by MAC address.
		// The EOBR would be discovered, but the check to see if the passkey's matched would fail which would
		// result in never discovering the eobr (if both options are checked).
		// If both options are checked, have Provision New EOBR option take precedence and discover the eobr
		// based on serial number provided rather than MAC address
        if(_contentFrag.GetDefaultDeviceCheckbox().isChecked() && defaultMacAddress.length() > 0 && !_contentFrag.GetProvisionNewDeviceCheckbox().isChecked()){
        	// there is a default EOBR, so perform a mac address activation
        	
        	mActivateWithMacAddressTask = new ActivateWithMacAddressTask(this.getClass().getSimpleName(), defaultEobr, defaultMacAddress);
            mActivateWithMacAddressTask.execute();
        }
        else{
			if(_contentFrag.GetProvisionNewDeviceCheckbox().isChecked() && _contentFrag.GetSerialNumberTV().length() != 4)
				this.ShowMessage(this, serialNumberMissingString);
			else
				this.discoverDevices();			
        }
	}
	
	private void discoverDevices()
	{
		// TCH - 1/19/2012 - regardless of company config setting, communication mode will always
		// be bluetoth for Android - remove check of company config setting
		_pd = CreateDiscoveringDialog();
			
		try
		{
			RegisterReceivers();
	        	
	       	// if bluetooth is not enabled on the phone, return empty list
	       	_btAdapter = BluetoothAdapter.getDefaultAdapter();
	       	if (_btAdapter == null || !_btAdapter.isEnabled())
	       	{    
	       		_gen1Devices = null;
	       		_gen2Devices = null;
	       	}
	       	else
	       	{
	       		_discAttemptCnt = 0;
	       		_discTotalCnt = 0;
	       		_discovering = true;
	       		_btAdapter.cancelDiscovery();
	       		boolean started = _btAdapter.startDiscovery();  	        	
	       		if(!started)
	       			Log.v("DeviceDiscovery", "not started correctly");
	       	}
		}
		catch(Exception ex)
		{
			
        	Log.e("UnhandledCatch", ex.getMessage() + ": " + Log.getStackTraceString(ex));
		}
	     
		mHandler.postDelayed(mDiscoveryHandler, BT_DISCOVERY_DELAY);		
	}
	
	public void handleReleaseButtonClick() {
		mReleaseTask = new ReleaseDeviceTask(this);
		mReleaseTask.execute();		
	}
	
	public void handleCancelButtonClick() {
		DeviceDiscovery.this.finish();
		
		if(_loginProcess)
			startNextActivity();
		else
			this.Return();		
	}
	
	public void handleDefaultEobrClick() {
		EobrDeviceDescriptor selectedEobrDevice = null;
		if(!_contentFrag.GetDefaultDeviceCheckbox().isChecked()) {
			_contentFrag.GetDefaultDeviceCheckbox().setText(setDefaultDeviceString);
		} else {
			if(_contentFrag.GetDeviceListDropdown() != null && _contentFrag.GetDeviceListDropdown().getSelectedItem() != null) {
	        	selectedEobrDevice = (EobrDeviceDescriptor) _contentFrag.GetDeviceListDropdown().getSelectedItem();
	        	_contentFrag.GetDefaultDeviceCheckbox().setText(defaultDeviceString + selectedEobrDevice.getName());
			} else if(_newEobrName != null && _newEobrName.length() > 0) {
				_contentFrag.GetDefaultDeviceCheckbox().setText(defaultDeviceString + _newEobrName);
			} else if (getCurrentEobrIdentifier() != null) {
				String defaultDeviceMessage = defaultDeviceString + getCurrentEobrIdentifier();
				_contentFrag.GetDefaultDeviceCheckbox().setText(defaultDeviceMessage);
			}
		}

    	SharedPreferences _userPref = this.getSharedPreferences(this.getString(R.string.sharedpreferencefile), 0);
		SharedPreferences.Editor editor = _userPref.edit(); 
		if(!_contentFrag.GetDefaultDeviceCheckbox().isChecked())
		{
			// when clearing the checkbox, the remove the default settings
			editor.putString(getString(R.string.defaulteobr), null);
			editor.putString(getString(R.string.macaddress), null);						
		}
		else{
			if(selectedEobrDevice == null){
				// when not picking a device from the discovery list, then save the currently active EOBR
				this.getCurrentEobrIdentifier();
				this.getCurrentEobrMacAddress();
				
				editor.putString(getString(R.string.defaulteobr), _newEobrName);
				editor.putString(getString(R.string.macaddress), _newEobrAddress);			
			}
			else{
				// when picking a device from the discovery list
				editor.putString(getString(R.string.defaulteobr), selectedEobrDevice.getName());
				editor.putString(getString(R.string.macaddress), selectedEobrDevice.getAddress());
			}
		}
		editor.commit();
	}

	public void handleProvisionNewEobrClick() {
		if(!_contentFrag.GetProvisionNewDeviceCheckbox().isChecked())
		{
			_contentFrag.GetSerialNumberLabel().setVisibility(View.GONE);
			_contentFrag.GetSerialNumberTV().setVisibility(View.GONE);
		}
		else
		{
			_contentFrag.GetSerialNumberLabel().setVisibility(View.VISIBLE);
			_contentFrag.GetSerialNumberTV().setVisibility(View.VISIBLE);
		}
	}
	
	public void handleActivateButtonClick() {
        if (_contentFrag.GetDeviceListDropdown().getSelectedItemPosition() >= 0)
        {        	
        	EobrDeviceDescriptor selectedEobrDevice = (EobrDeviceDescriptor) _contentFrag.GetDeviceListDropdown().getSelectedItem();
        	
        	mActivateTask = new ActivateTask(this.getClass().getSimpleName(), selectedEobrDevice.getName(), selectedEobrDevice.getAddress(), selectedEobrDevice.getCrc(), selectedEobrDevice.getEobrGen());
            mActivateTask.execute();            
        }
	}

	public BaseActivity getActivity() {
		return this;
	}

	public void ShowConfirmationMessage(String message, final Runnable yesAction, final Runnable noAction) {
		ShowConfirmationMessage(this, message,
				new ShowMessageClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						super.onClick(dialog, id);

						if (yesAction != null)
							yesAction.run();
					}
				},

				new ShowMessageClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						super.onClick(dialog, id);

						if (noAction != null)
							noAction.run();
					}
				}
		);
	}

    private boolean Activate(String deviceName, String deviceAddress, short deviceCrc, int deviceGeneration) throws KmbApplicationException
    {
        // if device name is specified, but address, crc and generation are not, activate by device name
        if (deviceName != null && deviceName.length() > 0 && deviceAddress == null && deviceCrc == -1 && deviceGeneration == -1)
        {
            return this.getMyController().ActivateEobrDevice(deviceName);
        }
        else
        {
        	return this.getMyController().ActivateEobrDevice(deviceName, deviceAddress, deviceCrc, deviceGeneration);
        }
    }


    private void startNextActivity()
    {
    	boolean isExemptLogEnabled = GlobalState.getInstance().getCurrentUser().getIsMobileExemptLogAllowed();
		boolean isExemptFromEldUse = GlobalState.getInstance().getCurrentUser().getExemptFromEldUse();
		boolean isELDMandateEnabled = GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled();

    	this.finish();
    	
    	if (EobrReader.getIsEobrDeviceAvailable() && EobrReader.getInstance().IsEobrConfigurationNeeded())
    	{
			Bundle extras = new Bundle();	        					
			extras.putBoolean(getString(R.string.extra_displaytripinfo), true);	        					
			
	    	String teamDriverExtra = getString(R.string.extra_teamdriverlogin);
	    	if(getIntent().hasExtra(teamDriverExtra))
	    		extras.putBoolean(teamDriverExtra, getIntent().getExtras().getBoolean(teamDriverExtra));
	    	
	    	if(_loginProcess)
	    		extras.putBoolean(getString(R.string.extra_isloginprocess), true);
			
			startActivity(EobrConfig.class, extras);
		} else
		{		
			if(getIntent().getBooleanExtra(getString(R.string.extra_teamdriverlogin), false)) {
				startActivity(TeamDriverDeviceType.class);
			} else {
				Bundle extras = new Bundle();
				extras.putString(this.getResources().getString(R.string.extra_tripinfomsg), this.getString(R.string.extra_tripinfomsg));
				
				if(_loginProcess)
					extras.putBoolean(getString(R.string.extra_isloginprocess), true);
		    	
				// if Exempt is Enabled and the EmployeeLog ruleset is US60||US70, go to ExemptLogType screen, else TripInfo screen
				// So, if the EmployeeLog contains nothing, then the users default ruleset is used.
				if ((isExemptLogEnabled && GlobalState.getInstance().getCurrentUser().getRulesetTypeEnum().isUSFederalRuleset()) || (isExemptFromEldUse && isELDMandateEnabled)) {
					startActivity(ExemptLogType.class, Intent.FLAG_ACTIVITY_SINGLE_TOP, extras);
				}
				//PBI 49820 - Go to SelectDutyStatus if on Mandate or AOBRD
				else {
					startActivity(SelectDutyStatus.class, Intent.FLAG_ACTIVITY_SINGLE_TOP, extras);
				}
			}
		}
    }

	private void RedisplayAfterActivation(String deviceName){
		try
		{
			if(deviceName == null || deviceName.length() == 0)
			{
	            _contentFrag.GetMessageLabel().setText(String.format(getString(R.string.msg_nopartnershipestablished)));				
			}
			else if(_loginProcess && DeviceDiscovery.this.getMyController().IsEobrDeviceOnline())
	        {
				// 2/10/12 JHM - Perform default EOBR logic when activation to a device is succesful.
				// This resolves a defect of clicking the default checkbox before activation pre-Login.
				this.handleDefaultEobrClick();
				
				startNextActivity();
	        }
			else {
				// activated an actual device
	            String connectionState = getString(R.string.lbldisconnected);
	            if (DeviceDiscovery.this.getMyController().IsEobrDeviceOnline()) {
	                connectionState = getString(R.string.lblonline);
	                DeviceDiscovery.this._contentFrag.GetDiscoverButton().setEnabled(false);
	                RodsEntry.resetOpenDVIRFlag();
	            }
	            
	            _contentFrag.GetMessageLabel().setText(String.format(getString(R.string.msg_successfulpartnership), deviceName, connectionState));
	            _contentFrag.GetReleaseButton().setEnabled(true);
	            _contentFrag.GetActivateButton().setEnabled(false);
	            _contentFrag.GetDeviceListDropdown().setVisibility(View.GONE);
	            //DeviceDiscovery.this._chkDefaultEobr.setEnabled(true);
	            
				if(_loginProcess)
					startNextActivity();
			}
            
		}
		catch(KmbApplicationException kae)
		{
			this.HandleException(kae);
			_contentFrag.GetMessageLabel().setText(getString(R.string.msg_nopartnershipestablished) + getString(R.string.msg_activationfailed));
        	//DeviceDiscovery.this._chkDefaultEobr.setEnabled(false);
		}			
	}

	private void updateTractorNumber(String deviceName) {

		try {

			if (!_loginProcess && this.getMyController().IsEobrDeviceOnline() && deviceName != null && deviceName.length() != 0) {

				IAPIController empLogController = MandateObjectFactory.getInstance(this, GlobalState.getInstance().getFeatureService()).getCurrentEventController();
				LogEntryController logEntryCtrl = new LogEntryController(this);

				EmployeeLog currentLog = logEntryCtrl.getCurrentEmployeeLog();
				currentLog.setTractorNumbers(deviceName);

				empLogController.SaveLocalEmployeeLog(currentLog);
				logEntryCtrl.setCurrentEmployeeLog(currentLog);

				GlobalState.getInstance().set_currentTractorNumbers(deviceName);

			}
		} catch (KmbApplicationException e) {
			Log.e("UnhandledCatch", e.getMessage() + ": " + Log.getStackTraceString(e));
		}
	}

	private Runnable mDiscoveryHandler = new Runnable()
	{
		public void run()
		{
			// verify that the number of discovery attempts has not exceeded the maximum that should be attempted
			_discAttemptCnt++;
			_discTotalCnt++;
			Log.v("DeviceDiscovery", String.format("DiscoveryHandler _discovering: %s total: %s att: %s", _discovering, _discTotalCnt, _discAttemptCnt));
			if(_discTotalCnt == DISCOVERY_MAX_TOTAL_COUNT || _discAttemptCnt == DISCOVERY_MAX_ATTEMPT_COUNT){
				Log.v("DeviceDiscovery", String.format("DiscoveryHandler timed out, cancelling"));
				_discovering = false;
            	_btAdapter.cancelDiscovery();
			}
			
			if (_discovering)
			{
				mHandler.postDelayed(mDiscoveryHandler, BT_DISCOVERY_DELAY);
			}
			else
			{
                // discover the list of devices
				_initialDiscoveryPerformed = true;
				mDiscoveryTask = new DiscoveryTask(this.getClass().getSimpleName(), _pd);
				mDiscoveryTask.execute();
			}
		}
	};
	
    // The BroadcastReceiver that listens for discovered devices
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        	if(intent != null)
        	{
	            String action = intent.getAction();
	            if(action != null)
	            {
        			// when a discovery message comes in, reset the attempt count
        			_discAttemptCnt = 0;        			
        			if( BluetoothAdapter.ACTION_STATE_CHANGED.equals(action))
        			{        				
        				if(intent.hasExtra(BluetoothAdapter.EXTRA_STATE)){
	        				int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);	
	        				Log.v("DeviceDiscovery", String.format("BluetoothAdapter.ACTION_STATE_CHANGED state: %s", state));	        				
	        				if(state == BluetoothAdapter.STATE_ON)
	        				{
	        					// the BT adapter just turned itself on, so attempt paired discovery process
	        			    	mDiscoverPairedDeviceTask = new DiscoverPairedDeviceTask(this.getClass().getSimpleName(), _pd);
	        			    	mDiscoverPairedDeviceTask.execute();
	        				}
        				}
        			}
        			
		            // When discovery finds a device       
		            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
		            	// Get the BluetoothDevice object from the Intent
		                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
		                if (device != null)
		                {
		                	BluetoothClass btClass = device.getBluetoothClass();
		                	if(btClass != null)
		                	{		                	
		                		if (btClass.getDeviceClass() == GEN1_BTCLASS)
		                		{
		                			if (_gen1Devices == null)
		                				_gen1Devices = new ArrayList<BluetoothDevice>();
		                			
		                			if (!_gen1Devices.contains(device) && device.getName() != null && _gen1Devices.size() < 16){
		                				Log.v("DeviceDiscovery", String.format("BroadcastReceiver found gen 1 device named: '%s'", device.getName()));			                				                	
		                				_gen1Devices.add(device);
		                			}
		                		}
		                		else if (btClass.getDeviceClass() == GEN2_PAN_BTCLASS || btClass.getDeviceClass() == GEN2_RVN_BTCLASS)
		                		{
		                			if (_gen2Devices == null)
		                				_gen2Devices = new ArrayList<BluetoothDevice>();
			                	
		                			if (!_gen2Devices.contains(device) && device.getName() != null && _gen2Devices.size() < 16){
		                				Log.v("DeviceDiscovery", String.format("BroadcastReceiver found gen 2 device named: '%s'", device.getName()));			                				                	
		                				_gen2Devices.add(device);
		                			}
		                		}
		                	
		                		// if we have found the maximum number of devices, cancel discovery
		                		int numDevicesFound = 0;
		                		if (_gen1Devices != null)
		                			numDevicesFound = _gen1Devices.size();
			                
		                		if (_gen2Devices != null)
		                			numDevicesFound += _gen2Devices.size();
			                
		                		if (numDevicesFound > 16)
		                			_btAdapter.cancelDiscovery();
		                	}
		                }
		            // When discovery is finished, set _discovering flag to false
		            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
		            	_discovering = false;
		            } 
	            }
        	}
        }
    };
    
    private DiscoveryTask mDiscoveryTask;

	private class DiscoveryTask extends AsyncTask<Void, Void, Boolean> {
		private ProgressDialog progress;
		private EobrDeviceDescriptor[] deviceList;
		private String _className;
		
		
		public DiscoveryTask(String className, ProgressDialog pd) {
			this._className = className;
			this.progress = pd;
		}		


		public void onPreExecute() {
			DeviceDiscovery.this.LockScreenRotation();
			if(!DeviceDiscovery.this.isFinishing() && progress != null && !progress.isShowing())
				progress.show();
		}

		public Boolean doInBackground(Void... unused) {
			try
			{
	        	boolean provisionNewDevice = false;
	        	String serialNumber = "";
	        	if(_contentFrag.GetProvisionNewDeviceCheckbox().isChecked() && _contentFrag.GetSerialNumberTV().length() > 0)
	        	{
	        		provisionNewDevice = true;
	        		serialNumber = DeviceDiscovery.this._contentFrag.GetSerialNumberTV().getText().toString().trim();
	        	}
	        	
	        	deviceList = DeviceDiscovery.this.getMyController().PerformFullDeviceDiscovery(_gen1Devices, _gen2Devices, provisionNewDevice, serialNumber);
			}
			catch(Exception e) { 
				return false; 
			}

			return true;
			
		}

		public void onPostExecute(Boolean success) {
			boolean unlockRotation = true;
			try	
			{				
				_contentFrag.GetDeviceListDropdown().setVisibility(View.GONE);
                if (deviceList == null || deviceList.length == 0)
                {
                	_contentFrag.GetMessageLabel().setText(getString(R.string.msg_nodevicesfound));
                	_contentFrag.GetActivateButton().setEnabled(false);
                	//DeviceDiscovery.this._chkDefaultEobr.setEnabled(false);
                    dismissProgressDialog(); 
                }
                else
                {
                	_contentFrag.GetMessageLabel().setText(String.format(multipleDeviceMessage, deviceList.length));
                	_contentFrag.GetActivateButton().setEnabled(true);

                    // build the cbo with each named device
            		//EobrDeviceAdapter eobrAdapter = new EobrDeviceAdapter(DeviceDiscovery.this, android.R.layout.simple_spinner_item, deviceList);
            		ArrayAdapter<EobrDeviceDescriptor> eobrDeviceAdapter = new ArrayAdapter<EobrDeviceDescriptor>(DeviceDiscovery.this, R.layout.kmb_spinner_item, deviceList);
            		eobrDeviceAdapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
            		_contentFrag.GetDeviceListDropdown().setAdapter(eobrDeviceAdapter);
            		_contentFrag.GetDeviceListDropdown().setVisibility(View.VISIBLE);
	                DeviceDiscovery.this._contentFrag.GetDefaultDeviceCheckbox().setEnabled(true);
	                
	                
	                // If there is a default EOBR selected and it's found connect to it automatically.
	                try {Thread.sleep(1000);} catch (InterruptedException e) {
	                	
	                	Log.e("UnhandledCatch", e.getMessage() + ": " + Log.getStackTraceString(e));
	                }
	                SharedPreferences userPref = getSharedPreferences(getString(R.string.sharedpreferencefile), 0);
		    		String defaultEobr = userPref.getString(getString(R.string.defaulteobr), "");
		    		if(defaultEobr != null && defaultEobr.length() > 0)
		    		{
		    			for (int i = 0; i < deviceList.length; i++) {		    				
							if(deviceList[i].getName().compareTo(defaultEobr) ==  0)
							{
								// found the default EOBR in the device list
								_contentFrag.GetDeviceListDropdown().setSelection(i);
								
								mActivateTask = new ActivateTask(this.getClass().getSimpleName(), defaultEobr);
					            mActivateTask.execute();
							}
						}
		    		}
	                
	                dismissProgressDialog(); 
                }
                _initialDiscoveryPerformed = true;
                mDiscoveryTask = null;
			}

			catch(IllegalArgumentException e){
				// nothing
				e.printStackTrace();
                mDiscoveryTask = null;
                Log.e("UnhandledCatch", e.getMessage() + ": " + Log.getStackTraceString(e));
            }
			if(unlockRotation) DeviceDiscovery.this.UnlockScreenRotation();
		}
		
		// 9/29/11 JHM - Added public methods so that dialogs and context can be 
		// re-established after an orientation change (ie. activity recreated).
        @SuppressWarnings("unused")
		public void showProgressDialog()
        {
			progress = DeviceDiscovery.this.CreateDiscoveringDialog();
        }
        
        public void dismissProgressDialog()
        {
        	try
        	{
        		if(progress != null && progress.isShowing()) progress.dismiss();
        	}
        	catch (Exception ex){
        		ErrorLogHelper.RecordMessage(String.format(BaseActivity.MSGASYNCDIALOGEXCEPTION, this._className, this.getClass().getSimpleName()));
        	}
        }
	}
	
	private DiscoverPairedDeviceTask mDiscoverPairedDeviceTask; 
	private class DiscoverPairedDeviceTask extends AsyncTask<Void, Void, Boolean> {
		private ProgressDialog progress;
		private String className;
		BluetoothAdapter btAdapter;
		boolean btAlreadyOn;
		List<EobrDeviceDescriptor> pairedEobrDeviceList = null;
						
		private DiscoverPairedDeviceTask(String className, ProgressDialog pd) {
			this.className = className;
			this.progress = pd;
		}
		
		public void onPreExecute() {
			DeviceDiscovery.this.LockScreenRotation();
			if(!DeviceDiscovery.this.isFinishing())
			{	
				if(progress != null && !progress.isShowing())
					progress.show();
				else
					this.showProgressDialog();	
			}
			btAdapter = BluetoothAdapter.getDefaultAdapter();
		}

		public Boolean doInBackground(Void... unused) {
            if (btAdapter == null || !btAdapter.isEnabled())
        	{
                // BT is not on, request that it be enabled.
            	btAlreadyOn = false;
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        	} 
            else
			{
            	btAlreadyOn = true;
        		GlobalState gs = GlobalState.getInstance();
				SystemStartupController startupCtrllr = new SystemStartupController(gs);

        		List<BluetoothDevice> btDevices = new ArrayList<BluetoothDevice>(btAdapter.getBondedDevices());
        		
        		if (btDevices != null && btDevices.size() > 0)
        		{
        			pairedEobrDeviceList = startupCtrllr.PerformSystemStartup_PairedDevices(btDevices, btAdapter);        			
        		}
			}
            
			return null;			
		}

		public void onPostExecute(Boolean success) {
			dismissProgressDialog(); 
        	
        	//if BT wasn't on then we'll handle navigation
        	//when the request to turn it on is returned
        	if(btAlreadyOn)
        	{
        		if (pairedEobrDeviceList != null)
        		{
                	_contentFrag.GetMessageLabel().setText(String.format(multipleDeviceMessage, pairedEobrDeviceList.size()));
                	_contentFrag.GetActivateButton().setEnabled(true);

            		ArrayAdapter<EobrDeviceDescriptor> eobrDeviceAdapter = new ArrayAdapter<EobrDeviceDescriptor>(DeviceDiscovery.this, R.layout.kmb_spinner_item, pairedEobrDeviceList);
            		eobrDeviceAdapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
            		_contentFrag.GetDeviceListDropdown().setAdapter(eobrDeviceAdapter);
            		_contentFrag.GetDeviceListDropdown().setVisibility(View.VISIBLE);
	                DeviceDiscovery.this._contentFrag.GetDefaultDeviceCheckbox().setEnabled(true);
        		}
        	} 
        	_initialDiscoveryPerformed = true;
        	DeviceDiscovery.this.UnlockScreenRotation();
		}
		
		public void showProgressDialog()
        {
			progress = DeviceDiscovery.this.CreateDiscoveringDialog();
        }
        
        public void dismissProgressDialog()
        {
        	try
        	{
        		if(progress != null && progress.isShowing()) progress.dismiss();
        	}
        	catch (Exception ex){
        		ErrorLogHelper.RecordMessage(String.format(BaseActivity.MSGASYNCDIALOGEXCEPTION, this.className, this.getClass().getSimpleName()));
        	}
        }		
	}
	
	private ActivateTask mActivateTask;
	public class ActivateTask extends AsyncTask<Void, Void, Boolean> {
		private ProgressDialog progress;
		private String _deviceName;
		private KmbApplicationException _ex;
		private String _className;
		private String _deviceAddress = null;
		private short _deviceCrc = -1;
		private int _deviceGen = -1;
		
		public ActivateTask(String className, String deviceName) {
			this._className = className;
			if(!DeviceDiscovery.this.isFinishing())
				progress = ProgressDialog.show(DeviceDiscovery.this, "", getString(R.string.lblactivating));
    		_deviceName = deviceName;
		}
		
		public ActivateTask(String className, String deviceName, ProgressDialog pd) {
			this._className = className;
			progress = pd;
    		_deviceName = deviceName;
		}		
		
		public ActivateTask(String className, String deviceName, String deviceAddress, short deviceCrc, int deviceGeneration) {
			this._className = className;
			this._deviceName = deviceName;
			this._deviceAddress = deviceAddress;
			this._deviceCrc = deviceCrc;
			this._deviceGen = deviceGeneration;
			if(!DeviceDiscovery.this.isFinishing())
				progress = ProgressDialog.show(DeviceDiscovery.this, "", getString(R.string.lblactivating));
		}
		public void onPreExecute() {
			DeviceDiscovery.this.LockScreenRotation();
			if(progress != null && !progress.isShowing())
				progress.show();
		}

		public Boolean doInBackground(Void... unused) {
			try
			{
				if (DeviceDiscovery.this.Activate(_deviceName, _deviceAddress, _deviceCrc, _deviceGen)) {

					// Call the REST API to get the License Plate Number for the Unit currently associated to the active EOBR
					RESTWebServiceHelper rwsh = new RESTWebServiceHelper(DeviceDiscovery.this);
                    String unitLicensePlateNumber = rwsh.GetEobrUnitLicensePlateNumber(EobrReader.getInstance().getEobrSerialNumber());
                    EobrReader.getInstance().setUnitLicensePlateNumber(unitLicensePlateNumber);
				}
			}
			catch(KmbApplicationException kae) {
				_ex = kae;
			} catch (IOException e) {
				e.printStackTrace();
			}

			return true;
		}

		public void onPostExecute(Boolean success) {
			if(_ex != null)
			{
				DeviceDiscovery.this.HandleException(_ex);
				DeviceDiscovery.this._contentFrag.GetMessageLabel().setText(getString(R.string.msg_nopartnershipestablished) + getString(R.string.msg_activationfailed));
				//DeviceDiscovery.this._chkDefaultEobr.setEnabled(false);
			}
			else
			{
				DeviceDiscovery.this.RedisplayAfterActivation(_deviceName);
				DeviceDiscovery.this.updateTractorNumber(_deviceName);
			}
			
			dismissProgressDialog();
			mActivateTask = null;
			DeviceDiscovery.this.UnlockScreenRotation();
			
			// if BTE version is greater than expected, check for app update
        	EobrService eobrService = (EobrService)GlobalState.getInstance().getEobrService();
			if(eobrService != null)
				eobrService.ApplicationUpdate(true);
		}	
		
		// 9/29/11 JHM - Added public methods so that dialogs and context can be 
		// re-established after an orientation change (ie. activity recreated).
        public void showProgressDialog()
        {
        	if(!DeviceDiscovery.this.isFinishing())
        		progress = ProgressDialog.show(DeviceDiscovery.this, "", getString(R.string.lblactivating));
        }
        
        public void dismissProgressDialog()
        {
        	try
        	{
        		if(progress != null && progress.isShowing()) progress.dismiss();
        	}
        	catch (Exception ex){
        		ErrorLogHelper.RecordMessage(String.format(BaseActivity.MSGASYNCDIALOGEXCEPTION, this._className, this.getClass().getSimpleName()));
        	}
        }
        
	}
	
	private ReleaseDeviceTask mReleaseTask;
	
	private ActivateWithMacAddressTask mActivateWithMacAddressTask; 
	public class ActivateWithMacAddressTask extends AsyncTask<Void, Void, Boolean> {
		private ProgressDialog progress;
		private KmbApplicationException _ex;
		private String _className;
		private Bundle _bundle;
		private String _defaultEobrName;
		private String _defaultMacAddress;

		
		public ActivateWithMacAddressTask(String className, String defaultEobrName, String defaultMacAddress) {
			this._className = className;
			_defaultEobrName = defaultEobrName;
			_defaultMacAddress = defaultMacAddress;
			showProgressDialog();
		}		
		
		public void onPreExecute() {
			DeviceDiscovery.this.LockScreenRotation();
			if(progress != null && !progress.isShowing())
				progress.show();
		}

		public Boolean doInBackground(Void... unused) {
     		_bundle = DeviceDiscovery.this.getMyController().PerformSystemStartup_EobrDevice(_defaultMacAddress, null);
			return true;			
		}

		public void onPostExecute(Boolean success) {
			if(_ex != null)
			{
				DeviceDiscovery.this.HandleException(_ex);
				_contentFrag.GetMessageLabel().setText(getString(R.string.msg_releasefailed));
			}
			else
			{
	            if (_bundle != null &&
	            		_bundle.containsKey(DeviceDiscovery.this.getString(R.string.devicename)) &&
			            _bundle.getString(DeviceDiscovery.this.getString(R.string.devicename)) != null )
			    {
	            	// the mac address activation worked, so no need to go further
	            	DeviceDiscovery.this.RedisplayAfterActivation(_bundle.getString(DeviceDiscovery.this.getString(R.string.devicename)));
			    }
			}
			
			dismissProgressDialog();
			mActivateWithMacAddressTask = null;
			DeviceDiscovery.this.UnlockScreenRotation();
			
			// if BTE version is greater than expected, check for app update
        	EobrService eobrService = (EobrService)GlobalState.getInstance().getEobrService();
			if(eobrService != null)
				eobrService.ApplicationUpdate(true);
		}
		
        public void showProgressDialog()
        {
        	String msg = String.format(getString(R.string.lbldiscoveringeobr), _defaultEobrName);
        	if(!DeviceDiscovery.this.isFinishing())
        		progress = ProgressDialog.show(DeviceDiscovery.this, "", msg);        	
        }

        public void dismissProgressDialog()
        {
        	try
        	{
        		if(progress != null && progress.isShowing()) progress.dismiss();
        	}
        	catch (Exception ex){
        		ErrorLogHelper.RecordMessage(String.format(BaseActivity.MSGASYNCDIALOGEXCEPTION, this._className, this.getClass().getSimpleName()));
        	}
        }

	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		// 10/3/11 JHM - Don't store state (likely blank values) if the FetchLocalData task hasn't completed.
		if(mFetchLocalDataTask == null)
		{
//			Button _btnDiscover = frag.GetDiscoverButton();
//			Button _btnActivate = frag.GetActivateButton();
//			Button _btnRelease = frag.GetReleaseButton();
//			TextView _lblMessage = frag.GetMessageLabel();	
//			Spinner _cboDeviceList = frag.GetDeviceListDropdown();
//			CheckBox _chkDefaultEobr = frag.GetDefaultDeviceCheckbox();
//			
	    	// 11/14/11 JHM - Save "discovery" boolean in state bundle
			outState.putBoolean("initialdiscovercomplete", _initialDiscoveryPerformed);
//			outState.putBoolean("btnDiscoverEnabled", _btnDiscover.isEnabled());
//			outState.putBoolean("btnActivateEnabled", _btnActivate.isEnabled());
//			outState.putBoolean("btnReleaseEnabled", _btnRelease.isEnabled());
//			outState.putBoolean("chkDefaultEobrEnabled", _chkDefaultEobr.isEnabled());
//			outState.putString("lblMessage", _lblMessage.getText().toString());
//			if(_cboDeviceList.getVisibility() != View.GONE)
//			{
//				EobrDeviceDescriptor[] devices = new EobrDeviceDescriptor[_cboDeviceList.getAdapter().getCount()];
//				for( int i = 0; i < _cboDeviceList.getAdapter().getCount(); i++)
//				{
//					devices[i] = (EobrDeviceDescriptor) _cboDeviceList.getItemAtPosition(i);
//				}
//				outState.putParcelableArray("cboDeviceList", devices);
//			}
		}
		
		super.onSaveInstanceState(outState);
		
	}	
	
    public String getCurrentEobrIdentifier()
    {
        _newEobrName = this.getMyController().getCurrentEobrIdentifier();
    	return _newEobrName;
    }
    
    public String getCurrentEobrMacAddress()
    {
        _newEobrAddress = this.getMyController().getCurrentEobrMacAddress();
    	return _newEobrAddress;
    }
    
    public boolean IsEobrDeviceOnline() throws KmbApplicationException
    {
        return this.getMyController().IsEobrDeviceOnline();
    }

	//region: ReleaseDeviceTask.ITaskHost
	@Override
	public void onError(KmbApplicationException ex, String message) {
		HandleException(ex);
		_contentFrag.GetMessageLabel().setText(message);
	}

	@Override
	public void onReleaseCompletion(String message) {
		_contentFrag.GetDiscoverButton().setEnabled(true);
		_contentFrag.GetReleaseButton().setEnabled(false);

		_contentFrag.GetMessageLabel().setText(message);
	}

	@Override
	public BaseActivity getHostActivity() {
		return this;
	}
	//endregion
}
