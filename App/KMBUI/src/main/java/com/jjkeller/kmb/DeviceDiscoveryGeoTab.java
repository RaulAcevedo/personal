package com.jjkeller.kmb;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.jjkeller.kmb.fragments.DeviceDiscoveryGeoTabFrag;
import com.jjkeller.kmb.fragments.LeftNavFrag;
import com.jjkeller.kmb.interfaces.IDeviceDiscoveryGeoTab.GeotabDeviceDiscoveryActions;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmb.share.ReleaseDeviceTask;
import com.jjkeller.kmbapi.common.LogCat;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.EOBR.EobrReader;
import com.jjkeller.kmbapi.controller.EobrConfigController;
import com.jjkeller.kmbapi.controller.GeotabController;
import com.jjkeller.kmbapi.controller.SystemStartupController;
import com.jjkeller.kmbapi.controller.share.KmbApplicationException;
import com.jjkeller.kmbapi.controller.utility.ErrorLogHelper;
import com.jjkeller.kmbapi.controller.utility.RESTWebServiceHelper;
import com.jjkeller.kmbapi.enums.USBAccessoryConnectionStatus;
import com.jjkeller.kmbapi.geotabengine.GeotabBroadcasts;
import com.jjkeller.kmbapi.geotabengine.GeotabConstants;
import com.jjkeller.kmbapi.geotabengine.GeotabDataEnhanced;
import com.jjkeller.kmbapi.geotabengine.IGeotabListener;
import com.jjkeller.kmbapi.kmbeobr.Constants;
import com.jjkeller.kmbapi.proxydata.EobrConfiguration;
import com.jjkeller.kmbui.R;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class DeviceDiscoveryGeoTab extends BaseActivity implements GeotabDeviceDiscoveryActions, ReleaseDeviceTask.ITaskHost, LeftNavFrag.OnNavItemSelectedListener, LeftNavFrag.ActivityMenuItemsListener {
    private static String TAG = DeviceDiscoveryGeoTab.class.getSimpleName();

    DeviceDiscoveryGeoTabFrag _contentFrag;
    private boolean _loginProcess = false;
    protected ProgressDialog _progressDialog;
    private USBAccessoryConnectionStatus _status;
    protected boolean _initialDiscoveryPerformed = false;
    protected String _errorMessage = null;
    private boolean _isUsbSupported = true;

    private ActivateTask activateTask;
    private ReleaseDeviceTask releaseDeviceTask;

    BroadcastReceiver _broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "Received broadcast: " + action);

            if (action.equals(GeotabBroadcasts.ON_DEVICE_DETACHED)) {
                onGeotabDetached();
            } else if (action.equals(GeotabBroadcasts.ON_PERMISSION_DENIED)) {
                onPermissionDenied();
            } else if (action.equals(GeotabBroadcasts.ON_PERMISSION_TIMEOUT)) {
                onPermissionTimeout();
            }
        }
    };

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.baselayout);

        registerReceivers();

        _loginProcess = getIntent().hasExtra(this.getResources().getString(R.string.extra_isloginprocess));

        if(savedInstanceState == null)
        {
            mFetchLocalDataTask = new FetchLocalDataTask(this.getClass().getSimpleName());

            if(_loginProcess && !_initialDiscoveryPerformed)
                mFetchLocalDataTask.setAutoUnlockScreenRotationOnPostExecute(false);
            else
                mFetchLocalDataTask.setAutoUnlockScreenRotationOnPostExecute(true);
            mFetchLocalDataTask.execute();
        }

        loadContentFragment(new DeviceDiscoveryGeoTabFrag());

        if(!_loginProcess)
            loadLeftNavFragment();
        else
        {
            View leftNavLayout = findViewById(R.id.leftnav_fragment);
            if(leftNavLayout != null) leftNavLayout.setVisibility(View.GONE);
        }

        //check to see if the device supports USB Accessories
        PackageManager pm = this.getPackageManager();
        _isUsbSupported = pm.hasSystemFeature(PackageManager.FEATURE_USB_ACCESSORY);

        if(!_isUsbSupported) {
            ErrorLogHelper.RecordMessage(
                String.format("USB accessories not supported on this device, unable to interface with Geotab device. Device info: \nOS Version: %s\nSDK Version: %d\nBrand: %s\nModel: %s\n",
                    Build.VERSION.RELEASE,
                    Build.VERSION.SDK_INT,
                    Build.BRAND,
                    Build.MODEL)
            );
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putBoolean("initialDiscoveryPerformed", _initialDiscoveryPerformed);
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        _initialDiscoveryPerformed = savedInstanceState.getBoolean("initialDiscoveryPerformed");
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        try
        {
            if(_progressDialog != null && _progressDialog.isShowing()){
                _progressDialog.dismiss();
            }
        }
        finally{
            this.unregisterReceiver(_broadcastReceiver);
        }
    }

    //Conveniently (and perhaps coincidentally), this method is called when the accessory is plugged into the phone.
    //It is possible for this activity to get "officially" notified of an accessory being attached, but only
    //through specifying an intent-filter in the manifest.  However, by doing that, it would be possible for
    //KMB to launch directly to this activity, which is undesirable.
    @Override
    protected void onResume()
    {
        super.onResume();

        if(_isUsbSupported) {
            if (getMyController().IsDeviceAttached()) {
                onGeotabAttached();

                //only allow activation if the EOBREngine isn't a Geotab instance
                //or if we're not currently connected
                boolean currentlyConnected = getMyController().IsDeviceConnected();
                if(_loginProcess && !_initialDiscoveryPerformed && !currentlyConnected) {
                    _initialDiscoveryPerformed = true;
                    handleActivateButtonClick();
                }
            } else {
                noUsbAccessory();
            }
        } else {
            noUsbSupport();
        }
    }

    @Override
    public void setFragments(){
        super.setFragments();
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
        _contentFrag = (DeviceDiscoveryGeoTabFrag) f;
    }

    //region: LeftNav
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        if(!_loginProcess) {
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
    //endregion

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

    @Override
    protected void InitController()
    {
        this.setController(new GeotabController(this));
    }
    protected GeotabController getMyController() {
        return (GeotabController)getController();
    }

    @Override
    public void Return (boolean success) {
        if(success)
        {
            this.startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
    }

    private void registerReceivers()
    {
        IntentFilter filter = new IntentFilter();
        filter.addAction(GeotabBroadcasts.ON_DEVICE_DETACHED);
        filter.addAction(GeotabBroadcasts.ON_PERMISSION_DENIED);
        filter.addAction(GeotabBroadcasts.ON_PERMISSION_TIMEOUT);

        registerReceiver(_broadcastReceiver, filter);
    }

    public void startNextActivity(){

        DeviceDiscoveryGeoTab.this.UnlockScreenRotation();
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
        } else {
            if (getIntent().getBooleanExtra(getString(R.string.extra_teamdriverlogin), false)) {
                startActivity(TeamDriverDeviceType.class);
            } else {
                Bundle extras = new Bundle();
                extras.putString(this.getResources().getString(R.string.extra_tripinfomsg), this.getString(R.string.extra_tripinfomsg));

                if (_loginProcess)
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

    @Override
    public void handleActivateButtonClick()
    {
        _errorMessage = null;

        Log.v("ian", "handleActivateButtonClick", new Throwable());

        activateTask = new ActivateTask(this);
        activateTask.execute();
    }

    @Override
    public void handleReleaseButtonClick()
    {
        releaseDeviceTask = new ReleaseDeviceTask(this);
        releaseDeviceTask.execute();
    }

    @Override
    public void handleCancelButtonClick() {

        DeviceDiscoveryGeoTab.this.finish();
        if(_loginProcess)
        {
            startNextActivity();
        }else
        {
            this.Return();
        }
    }

    protected void noUsbAccessory()
    {
        _contentFrag.GetMessageLabel().setText(R.string.msgNoUSBAccessory);
        _contentFrag.GetActivateButton().setEnabled(false);
        _contentFrag.GetReleaseButton().setEnabled(false);
    }

    protected void onGeotabDetached() {
        showMessageOrError(R.string.msgGeotabDetached);
        _contentFrag.GetActivateButton().setEnabled(false);
        _contentFrag.GetReleaseButton().setEnabled(false);
    }

    protected void onGeotabAttached() {
        boolean isCurrentlyConnected = getMyController().IsDeviceConnected();

        showMessageOrError(R.string.msgGeotabAttached);
        _contentFrag.GetActivateButton().setEnabled(!isCurrentlyConnected);
        _contentFrag.GetReleaseButton().setEnabled(isCurrentlyConnected);
    }

    protected void onPermissionDenied() {
        showErrorMessage(R.string.msgGeotabPermissionDenied);
        _contentFrag.GetActivateButton().setEnabled(true);
        _contentFrag.GetReleaseButton().setEnabled(false);
    }

    protected void onPermissionTimeout() {
        showErrorMessage(R.string.msgGeotabPermissionTimeout);
        _contentFrag.GetActivateButton().setEnabled(true);
        _contentFrag.GetReleaseButton().setEnabled(false);
    }

    protected void noUsbSupport() {
        showErrorMessage(R.string.msgGeotabNoUsbSupport);
        _contentFrag.GetActivateButton().setEnabled(false);
        _contentFrag.GetReleaseButton().setEnabled(false);
    }

    public class ActivateTask extends AsyncTask<Void, Void, Boolean> {
        private ProgressDialog progress;
        private KmbApplicationException _ex;
        private String _className;

        public ActivateTask(BaseActivity activity) {
            Log.v("ian", "ActivateTask", new Throwable());

            this._className = activity.getClass().getSimpleName();

            if(!DeviceDiscoveryGeoTab.this.isFinishing())
                progress = ProgressDialog.show(DeviceDiscoveryGeoTab.this, "", getString(R.string.lblactivating));
        }
        public void onPreExecute() {
            DeviceDiscoveryGeoTab.this.LockScreenRotation();
            if(progress != null && !progress.isShowing())
                progress.show();
        }

        public Boolean doInBackground(Void... unused) {
            final CountDownLatch receiveDataLatch = new CountDownLatch(1);

            class DataListener implements IGeotabListener {

                @Override
                public void receiveGeotabData(GeotabDataEnhanced data) {
                    if(data != null) {
                        receiveDataLatch.countDown();
                    }
                }
            }

            DataListener listener = new DataListener();

            try
            {
                SystemStartupController startupController = new SystemStartupController(DeviceDiscoveryGeoTab.this);
                if (startupController.ActivateEobrDevice(GeotabConstants.MANUFACTURER, Constants.GENERATION_GEOTAB)) {
                    getMyController().addListener(listener);

                    // Call the REST API to get the License Plate Number for the Unit currently associated to the active EOBR
                    RESTWebServiceHelper rwsh = new RESTWebServiceHelper(DeviceDiscoveryGeoTab.this);
                    String unitLicensePlateNumber = rwsh.GetEobrUnitLicensePlateNumber(EobrReader.getInstance().getEobrSerialNumber());
                    EobrReader.getInstance().setUnitLicensePlateNumber(unitLicensePlateNumber);

                    // Allow the device to settle and read some data so current Geotab data stream has populated GeotabEngine properties
                    receiveDataLatch.await(5000, TimeUnit.MILLISECONDS);
                    EobrReader.getInstance().Technician_SetUniqueIdentifier(getGeotabDeviceName());
                }

            }
            catch(KmbApplicationException kae) {
                _ex = kae;
                LogCat.getInstance().e("DeviceDiscoveryGeoTab", "Caught an Exception", kae);
            } catch (IOException e) {
                e.printStackTrace();
                LogCat.getInstance().e("DeviceDiscoveryGeoTab", "Caught an Exception", e);
            } catch (InterruptedException e) {
                e.printStackTrace();
                LogCat.getInstance().e("DeviceDiscoveryGeoTab", "Caught an Exception", e);
            }
            finally {
                getMyController().removeListener(listener);
            }


            return true;
        }

        public void onPostExecute(Boolean success) {
            if(_ex != null)
            {
                DeviceDiscoveryGeoTab.this.onError(_ex, getString(R.string.msg_nopartnershipestablished) + getString(R.string.msg_activationfailed));
            }
            else
            {
                /**
                 * The connection to the Geotab was successful, so let's get a fresh instance of our Geotab controller
                 * which will have an up-to-dage Geotab engine, otherwise this class won't notice that a socket to the Geotab
                 * is already established.
                 */
                DeviceDiscoveryGeoTab.this.setController(new GeotabController(DeviceDiscoveryGeoTab.this));

                if(_loginProcess)
                    DeviceDiscoveryGeoTab.this.startNextActivity();
                else
                    DeviceDiscoveryGeoTab.this.onGeotabAttached();
            }

            dismissProgressDialog();
            DeviceDiscoveryGeoTab.this.UnlockScreenRotation();

            EobrService eobrService = (EobrService)GlobalState.getInstance().getEobrService();
            if(eobrService != null)
                eobrService.ApplicationUpdate(true);
        }

        // 9/29/11 JHM - Added public methods so that dialogs and context can be
        // re-established after an orientation change (ie. activity recreated).
        public void showProgressDialog()
        {
            if(!DeviceDiscoveryGeoTab.this.isFinishing())
                progress = ProgressDialog.show(DeviceDiscoveryGeoTab.this, "", getString(R.string.lblactivating));
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


        private String getGeotabDeviceName()
        {

            String deviceName = null;
            String serialNumber = EobrReader.getInstance().getEobrSerialNumber();

            // Download config for device from Encompass and set tractor number, if found
            EobrConfigController eobrConfigController = new EobrConfigController(DeviceDiscoveryGeoTab.this);
            deviceName = eobrConfigController.GetTractorNumberFromDMO();

            if (deviceName == null)
            {
                // If no data available from Encompass, use local values
                EobrConfiguration config = eobrConfigController.GetConfigFromDB();
                if(config != null && config.getTractorNumber() != "") {
                    deviceName = config.getTractorNumber();
                }
            }

            if(deviceName != null && deviceName != GeotabConstants.MANUFACTURER)
                return deviceName;
            else
                return serialNumber;

        }
    }

    private void showMessageOrError(int messageId) {
        if(_errorMessage != null) {
            _contentFrag.GetMessageLabel().setText(_errorMessage);
        } else {
            _contentFrag.GetMessageLabel().setText(getString(messageId));
        }
    }

    private void showErrorMessage(int messageId) {
        showErrorMessage(getString(messageId));
    }

    private void showErrorMessage(String message) {
        _errorMessage = message;

        _contentFrag.GetMessageLabel().setText(_errorMessage);
    }

    //region: ReleaseDeviceTask.ITaskHost
    @Override
    public void onReleaseCompletion(String message) {
        _contentFrag.GetActivateButton().setEnabled(true);
        _contentFrag.GetReleaseButton().setEnabled(false);

        _contentFrag.GetMessageLabel().setText(message);
    }

    @Override
    public void onError(KmbApplicationException ex, String message) {
        HandleException(ex);

        if(_errorMessage == null)
            showErrorMessage(message);

        if(getMyController().IsDeviceAttached())
            onGeotabAttached();
        else
            noUsbAccessory();
    }

    @Override
    public BaseActivity getHostActivity() {
        return this;
    }
    //endregion
}
