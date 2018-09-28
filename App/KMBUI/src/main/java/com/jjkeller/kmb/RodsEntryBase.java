package com.jjkeller.kmb;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Process;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.jjkeller.kmb.crashlytics.Utils;
import com.jjkeller.kmb.fragments.LeftNavFrag;
import com.jjkeller.kmb.fragments.RodsEntryFrag;
import com.jjkeller.kmb.interfaces.ILogDownloaderHost;
import com.jjkeller.kmb.interfaces.IRodsEntry;
import com.jjkeller.kmb.share.ApplicationUpdateListenerFactory;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmb.share.OffDutyBaseActivity;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.DutyStatusChangeEventArgs;
import com.jjkeller.kmbapi.controller.EOBR.EobrEventArgs;
import com.jjkeller.kmbapi.controller.EOBR.EobrGenIIHistoryEventArgs;
import com.jjkeller.kmbapi.controller.EOBR.EobrHistoryEventArgs;
import com.jjkeller.kmbapi.controller.EOBR.EobrReader;
import com.jjkeller.kmbapi.controller.EOBR.EobrReader.ConnectionState;
import com.jjkeller.kmbapi.controller.EOBR.EobrTripReportEventArgs;
import com.jjkeller.kmbapi.controller.EOBR.UnassignedDrivingPeriodEventArgs;
import com.jjkeller.kmbapi.controller.EobrConfigController;
import com.jjkeller.kmbapi.controller.EobrDiagnosticCommandController;
import com.jjkeller.kmbapi.controller.FailureController;
import com.jjkeller.kmbapi.controller.GeotabController;
import com.jjkeller.kmbapi.controller.HosAuditController;
import com.jjkeller.kmbapi.controller.LogEntryController;
import com.jjkeller.kmbapi.controller.SystemStartupController;
import com.jjkeller.kmbapi.controller.TripRecordController;
import com.jjkeller.kmbapi.controller.VehicleInspectionController;
import com.jjkeller.kmbapi.controller.interfaces.IAPIController;
import com.jjkeller.kmbapi.controller.interfaces.IDutyStatusChangeEvent;
import com.jjkeller.kmbapi.controller.interfaces.IEobrHistoryChangeEvent;
import com.jjkeller.kmbapi.controller.interfaces.IEobrHistoryChangeEventGenII;
import com.jjkeller.kmbapi.controller.interfaces.IEobrReaderChangeEvent;
import com.jjkeller.kmbapi.controller.interfaces.IEobrTripReportListener;
import com.jjkeller.kmbapi.controller.interfaces.IEventHandler;
import com.jjkeller.kmbapi.controller.share.ExemptLogConvertedToGridLogEventArgs;
import com.jjkeller.kmbapi.controller.share.ExemptLogPreviousLogsConvertedToGridLogEventArgs;
import com.jjkeller.kmbapi.controller.share.KmbApplicationException;
import com.jjkeller.kmbapi.controller.share.MandateObjectFactory;
import com.jjkeller.kmbapi.controller.share.UnassignedDrivingPeriodResult;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.controller.utility.EmployeeLogUtilities;
import com.jjkeller.kmbapi.controller.utility.ErrorLogHelper;
import com.jjkeller.kmbapi.controller.utility.NotificationUtilities;
import com.jjkeller.kmbapi.enums.DutyStatusEnum;
import com.jjkeller.kmbapi.enums.EmployeeLogProvisionTypeEnum;
import com.jjkeller.kmbapi.enums.RuleSetTypeEnum;
import com.jjkeller.kmbapi.geotabengine.GeotabUsbService;
import com.jjkeller.kmbapi.kmbeobr.Constants;
import com.jjkeller.kmbapi.kmbeobr.Enums.EobrServiceMessages;
import com.jjkeller.kmbapi.kmbeobr.EventRecord;
import com.jjkeller.kmbapi.kmbeobr.StatusRecord;
import com.jjkeller.kmbapi.kmbeobr.TripReport;
import com.jjkeller.kmbapi.kmbeobr.UnidentifiedPairedEvents;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;
import com.jjkeller.kmbapi.proxydata.GpsLocation;
import com.jjkeller.kmbapi.proxydata.Location;
import com.jjkeller.kmbui.R;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

public abstract class RodsEntryBase extends OffDutyBaseActivity implements IRodsEntry.RodsEntryFragControllerMethods, IRodsEntry.RodsEntryFragActions, LeftNavFrag.OnNavItemSelectedListener, LeftNavFrag.ActivityMenuItemsListener, ILogDownloaderHost {

    private static final String STATE_EXEMPT_FROM_ELD_MESSAGE_SHOWN_KEY = "exempt_from_eld_message_shown";

    private DutyStatusEnum _currentDutyStatus = null;
    private RuleSetTypeEnum _currentRuleset;

    Bundle _currentEvents;
    Location _currentLocation;

    RodsEntryFrag _contentFrag;

    boolean _isExemptFromELDUse = false;
    boolean _isExiting = false;
    boolean _hasBeenCreated = false;
    boolean _createOffDutyLogs = true;
    boolean _promptForOffDutyLogs = true;
    boolean _hasBeenStarted = false;
    boolean _timeHandlingReceiverRegistered = false;
    boolean _teamDriversWereShownDOTClocks = false;
    boolean _checkForPendingEOBRDiagnosticCommands = true;
    boolean _shouldDisplayTripInfo = false;
    boolean _hasShownExemptFromELDMessage = false;
    static boolean _checkForDVIR = true;
    static boolean _openDVIRPromptShowing = false;
    private int countForGenerateError;

    private IDutyStatusChangeEvent _onDutyStatusChangedEvent;
    private IEobrReaderChangeEvent _onEobrReaderChangedEvent;
    private IEobrHistoryChangeEvent _onEobrHistoryChangedEvent;
    private IEobrHistoryChangeEventGenII _onEobrHistoryChangedEventGenII;
    private IEobrTripReportListener _onTripReport;
    private IEventHandler<UnassignedDrivingPeriodEventArgs> _unassignedDrivingPeriodEventHandler;
    private IEventHandler<ExemptLogConvertedToGridLogEventArgs> _exemptLogConversionEventHandler;
    private IEventHandler<ExemptLogPreviousLogsConvertedToGridLogEventArgs> _exemptLogPreviousLogConversionEventHandler;

    Messenger mService;
    EobrService mBoundService;
    boolean mBound;
    protected DialogInterface _personalConveyanceDrivingView;

    //Atomic reference that if set denotes a special driving period that needs to be ended
    protected static AtomicReference<EmployeeLogProvisionTypeEnum> provisionTypeEnumAtomicReference = new AtomicReference<>(null);

    private boolean _servicePreviouslyBound = false;

    // Target we publish for clients to send messages to IncomingHandler.
    Messenger mMessenger;

    // Used to keep track of the Heartbeat responses from the EobrService class.
    boolean mReveivedHeartbeatResponse = true;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        mMessenger = new Messenger(new IncomingHandler(this));
        countForGenerateError = 0;

        if (this.getIntent() != null) {
            if (this.getIntent().hasExtra(getString(R.string.extra_IsExemptFromELDUse))) {
                _isExemptFromELDUse = this.getIntent().getBooleanExtra(this.getString(R.string.extra_IsExemptFromELDUse), false);
            }

            // 9/19/12 JHM - Check if we're in the process of exiting the app due to a crash/unhandled exception
            if (this.getIntent().hasExtra(getString(R.string.crash_exit)) || GlobalState.getInstance().getIsCrashDetected()) {
                _isExiting = true;
            }

            if (!_isExiting) {
                _isExiting = this.getIntent().getBooleanExtra(this.getString(R.string.exit), false);
            }
        }

        if (_isExiting) {
            // note: if an unhandled exception occurs before the RODS screen is fully displayed,
            //       the app may exit through this path
            this.HandleSystemShutdownOnExit();
        } else {

            // Suspend reading history while checking for app update
            EobrReader.getInstance().SuspendReading();
            EobrReader.getInstance().setApplicationUpdateListenerFactory(new ApplicationUpdateListenerFactory(this));

            // 11/11/11 JHM - Retrieve boolean if we've previously completed the prompt for off duty logs.
            if (savedInstanceState != null && savedInstanceState.containsKey(getString(R.string.state_promptforoffdutylogs))) {
                _promptForOffDutyLogs = savedInstanceState.getBoolean(getString(R.string.state_promptforoffdutylogs));
            }

            _hasShownExemptFromELDMessage = savedInstanceState != null && savedInstanceState.getBoolean(STATE_EXEMPT_FROM_ELD_MESSAGE_SHOWN_KEY, false);

            if (savedInstanceState != null && savedInstanceState.containsKey(getString(R.string.servicebound))) {
                _servicePreviouslyBound = savedInstanceState.getBoolean(getString(R.string.servicebound));
            }

            //load data and controls
            reload(false);

            // Establish that the Hours Available Notification message returns to the RodsEntry Activity
            GlobalState.getInstance().setNotificationHoursAvailableClass(RodsEntry.class);
            GlobalState.getInstance().setIsPassedRods(true);

        }
    }

    //J.J. Keller Mobile AOBRD
    //J.J. Keller Mobile AOBRD - RSI
    private void reloadTextNavBar() {
        ActionBar actionBar = getSupportActionBar();
        boolean isEldMandateEnabled = GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled();
        boolean isInRoadsideInspectionMode = GlobalState.getInstance().getRoadsideInspectionMode();

        RodsEntryBase.addHeaderEldIdentifier(this, actionBar, isEldMandateEnabled, isInRoadsideInspectionMode);
    }

    private boolean hasUnidentifiedEmployeeEventsToReview() {
        IAPIController empCon = MandateObjectFactory.getInstance(this, GlobalState.getInstance().getFeatureService()).getCurrentEventController();

        /**
         * See if we have pairs of events that need to be reviewed instead of querying the database for
         * all unidentified events which chould contain an orphaned record caused by an app crash
         * */
        List<UnidentifiedPairedEvents> logsThatNeedToBeReviewed = empCon.LoadUnidentifiedEldEventPairs(false);
        return !logsThatNeedToBeReviewed.isEmpty();
    }

    private void reload(boolean autoUnlockScreenRotation) {
        setContentView(R.layout.rodsentry);

        mFetchLocalDataTask = new FetchLocalDataTask(this.getClass().getSimpleName());
        mFetchLocalDataTask.setAutoUnlockScreenRotationOnPostExecute(autoUnlockScreenRotation);
        mFetchLocalDataTask.execute();
    }

    @Override
    protected void onResume() {
        // force replacing the fragment since we're handling
        // screen rotation manually
        // 2013.12.17 sjn defect 18351, there is a scenario where a commit allowing state loss needs to occur
        loadContentFragment(new RodsEntryFrag(), true, true);

        //if a notification redirects the user to this activity
        //we need to manually set what URI we're at since our
        // methods and the URI will still show
        //that we're on a different screen
        super.setRecentlyStartedActivityUrl(RodsEntry.class);

        //Reload ActionBar title
        // J.J. Keller Mobile AOBRD & Roadside Inspection Mode
        reloadTextNavBar();

        super.onResume();
        if (_hasBeenCreated) {
            this.ShowCurrentStatus(true);
        }
        if (provisionTypeEnumAtomicReference.get() != null) {
            resolveSpecialDrivingState();
        }
    }




    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // When coming from the logout screen after the device is rotated mBoundService is null
        if (mBoundService == null) {
            mBoundService = ((EobrService) GlobalState.getInstance().getEobrService());
        }

        _isExiting = intent.getBooleanExtra(this.getString(R.string.exit), false);

        boolean displayOffDutyLogs = intent.getBooleanExtra(this.getString(R.string.extra_displayoffdutylogs), false);

        // Invalidate the menu to update the items
        supportInvalidateOptionsMenu();

        if (_isExiting) {
            if (intent.hasExtra(getString(R.string.crash_exit)) || this.getMyController().IsOKToShutdownApp()) {
                if (!mBoundService.IsReadingHistory()) {
                    if (_isExiting) {
                        this.HandleSystemShutdownOnExit();
                    }
                } else {
                    Toast.makeText(this, getString(R.string.logout_readinghistory), Toast.LENGTH_LONG).show();
                }
            } else {

                // If isExiting flag is set to true but it is not ok to shutdown the app (team driver still logged in), reset the flag.
                _isExiting = false;

                if (GlobalState.getInstance().getIsTeamLogin() && !GlobalState.getInstance().getCompanyConfigSettings(this).getMultipleUsersAllowed()) {
                    Toast.makeText(this, getString(R.string.msg_additionalteamdrivers), Toast.LENGTH_LONG).show();
                } else if (GlobalState.getInstance().getCompanyConfigSettings(this).getMultipleUsersAllowed()) {
                    Toast.makeText(this, getString(R.string.msg_additionalusersloggedin), Toast.LENGTH_LONG).show();
                }
                mFetchLocalDataTask = new FetchLocalDataTask(this.getClass().getSimpleName());
                mFetchLocalDataTask.execute();
            }
        } else {
            if (displayOffDutyLogs) {
                _promptForOffDutyLogs = true;
                _createOffDutyLogs = true;

                _hasShownExemptFromELDMessage = false;

                // 2/4/14 JHM - Variable reset when team driver is logging in.
                // Ensures off duty log & location prompts occur in proper order.
                _hasBeenCreated = false;

                mFetchLocalDataTask = new FetchLocalDataTask(this.getClass().getSimpleName());

                mFetchLocalDataTask.setAutoUnlockScreenRotationOnPostExecute(false);
                mFetchLocalDataTask.execute();
            } else {
                mFetchLocalDataTask = new FetchLocalDataTask(this.getClass().getSimpleName());

                // 12/22/11 JHM - If a dialog is displayed, prevent FetchLocalDataTask from unlocking screen rotation
                if (this.IsAlertMessageShowing()) {
                    mFetchLocalDataTask.setAutoUnlockScreenRotationOnPostExecute(false);
                }
                mFetchLocalDataTask.execute();
            }
        }
    }

    @Override
    //this is fired when orientation changes - need to handle it
    //ourselves because we don't want this activity to be destroyed and recreated
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);

        //reload data and controls
        //and auto unlock screen rotation
        reload(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (_timerHandler != null) {
            // remove any pending messages from timer handler
            _timerHandler.removeCallbacks(_heatbeatTimerTask);
            _timerHandler = null;
        }

        // in scenario where app process is killed, unregistering the receiver
        // fails with error indicating it isn't registered
        if (_timeHandlingReceiverRegistered) {
            unregisterReceiver(mReceiver);
        }

        if (!_isExiting && mBound) {
            doUnbindEobrService();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Don't store state (likely blank values) if the FetchLocalData task hasn't completed.
        if (mFetchLocalDataTask == null) {
            outState.putBoolean(getString(R.string.state_promptforoffdutylogs), _promptForOffDutyLogs);
            outState.putBoolean(STATE_EXEMPT_FROM_ELD_MESSAGE_SHOWN_KEY, _hasShownExemptFromELDMessage);
            outState.putBoolean(getString(R.string.servicebound), mBound);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //Handle the back button
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 9/27/11 JHM - ignore back button when in RSI mode
            if (!GlobalState.getInstance().getRoadsideInspectionMode()) {
                LockScreenRotation();
                //Ask the user if they want to quit
                new AlertDialog.Builder(this).setMessage("Do you want to logout?").setNegativeButton(R.string.btnno, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        UnlockScreenRotation();
                    }
                }).setPositiveButton(R.string.btnyes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Go to the logout activity
                        UnlockScreenRotation();
                        startActivity(Logout.class);
                    }
                }).show();
            }

            // Say that we've consumed the event
            return true;
        }

        // Otherwise let system handle keypress normally
        return super.onKeyDown(keyCode, event);
    }

    protected LogEntryController getMyController() {
        return (LogEntryController) this.getController();
    }

    protected void InitController() {
        LogEntryController logEntryCtrl = new LogEntryController(this);

        this.setController(logEntryCtrl);
    }

    @Override
    protected void loadData() {
        _currentEvents = this.getMyController().GetCurrentEventValues().toBundle(getResources().getString(R.string.eventvalues_logdate), getResources().getString(R.string.eventvalues_starttime), getResources().getString(R.string.eventvalues_dutystatus), getResources().getString(R.string.eventvalues_isautomaticdrivingevent), getActivity().getBaseContext());
        _currentRuleset = this.getMyController().GetCurrentEventValues_RulesetType();
        _currentLocation = this.getMyController().GetCurrentEventValues_Location();
    }

    @Override
    protected void findActivityControls() {
        _hasBeenCreated = true;
    }

    @Override
    protected void loadControls() {
        super.loadControls();

        //force replacing the fragment since we're handling
        //screen rotation manually
        loadContentFragment(new RodsEntryFrag(), false, true);

        IntentFilter filter = new IntentFilter(Intent.ACTION_TIME_CHANGED);
        this.registerReceiver(mReceiver, filter);

        filter = new IntentFilter(Intent.ACTION_DATE_CHANGED);
        this.registerReceiver(mReceiver, filter);

        filter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
        this.registerReceiver(mReceiver, filter);

        if (!_timeHandlingReceiverRegistered) {
            // note: odometer calibration offset should only be loaded once at startup time
            EobrConfigController eobrConfigController = new EobrConfigController(this);
            eobrConfigController.LoadOdometerOffsetFromEobr();
        }

        _timeHandlingReceiverRegistered = true;

        OnDutyStatusChangedEvent onDutyStatusChangeEvent = new OnDutyStatusChangedEvent();
        this.setOnDutyStatusChangedEvent(onDutyStatusChangeEvent);

        OnEobrReaderChangedEvent onEobrReaderChangedEvent = new OnEobrReaderChangedEvent();
        this.setOnEobrReaderChangedEvent(onEobrReaderChangedEvent);

        OnEobrHistoryChangedEvent onEobrHistoryChangedEvent = new OnEobrHistoryChangedEvent();
        this.setOnEobrHistoryChangedEvent(onEobrHistoryChangedEvent);

        if (!GlobalState.getInstance().getCompanyConfigSettings(this).getIsGeotabEnabled()) {
            OnEobrHistoryChangedEventGenII onEobrHistoryChangedEventGenII = new OnEobrHistoryChangedEventGenII();
            this.setOnEobrHistoryChangEventGenII(onEobrHistoryChangedEventGenII);
        }

        UnassignedDrivingPeriodEventHandler unassignedDrivingPeriodEventHandler = new UnassignedDrivingPeriodEventHandler();
        this.setOnUnassignedDrivingPeriodEvent(unassignedDrivingPeriodEventHandler);

        OnTripReportHandler onTripReportListener = new OnTripReportHandler();
        this.setOnTripReportListener(onTripReportListener);

        _exemptLogConversionEventHandler = new ExemptLogConvertedToGridLogEventHandler();
        EmployeeLogUtilities.RegisterForExemptLogConversionEvents(_exemptLogConversionEventHandler);

        _exemptLogPreviousLogConversionEventHandler = new ExemptLogPreviousLogConvertedToGridLogsEventHandler();
        EmployeeLogUtilities.RegisterForExemptLogPreviousLogConversionEvents(_exemptLogPreviousLogConversionEventHandler);

        if (_promptForOffDutyLogs) {
            _promptForOffDutyLogs = false;
            HandleStartupCompletion(true);
        } else {
            this.HandleStartupCompletion(false);
        }

    }

    @Override
    public void setFragments() {
        super.setFragments();

        Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
        _contentFrag = (RodsEntryFrag) f;
    }

    private void HandleStartupCompletion(boolean autoDisplayEditLocation) {
        // 10/22/12 JHM - Open DVIR logic moved here so it doesn't display simultanous with missing logs, etc.
        // 7/17/12 ACM - _openDVIRPromptShowing checks to see if the task already ran and prompt is showing,
        // without this an error will show up saying AsyncTask can only be ran once
        if (_checkForDVIR && !_openDVIRPromptShowing && EobrReader.getIsEobrDeviceAvailable()) {
            mCheckForOpenDVIR = new CheckForOpenDVIR();
            mCheckForOpenDVIR.execute();
        }

        if (_checkForPendingEOBRDiagnosticCommands) {
            scheduleCheckForPendingEOBRDiagnosticCommands();
        }

        showInitialPrompts();

        // note: allow the system to be started one, and only one, time
        if (_hasBeenStarted) {
            // 2/4/14 JHM - Just show status if startup work has been done previously
            // Handles a team driver login scenario.
            ShowCurrentStatus(autoDisplayEditLocation);
            return;
        }
        _hasBeenStarted = true;

        getMyController().RegisterForUpdates(this, _onDutyStatusChangedEvent, _onEobrReaderChangedEvent, _onEobrHistoryChangedEvent, _onEobrHistoryChangedEventGenII, _unassignedDrivingPeriodEventHandler);

        new TripRecordController(this).RegisterForUpdates(this, _onTripReport);

        startAndBindEobrService();

        ShowCurrentStatus(autoDisplayEditLocation);
        this.CreateHeartbeatTimer();

        if (!_openDVIRPromptShowing) {
            UnlockScreenRotation();
        }

        new HosAuditController(this).HandleStartupCompletion();
    }

    public void HandleSystemShutdownOnExit() {
        ErrorLogHelper.RecordMessage(this, String.format(Locale.getDefault(), "%s %s", this.getString(R.string.app_name), this.getString(R.string.mainappshutdownlabel)));

        // Make sure that we release an EOBR if we're connected and leaving the app.
        try {
            new SystemStartupController(this).ShutdownEobrDevice();
        } catch (KmbApplicationException kae) {
            ErrorLogHelper.RecordException(this, kae);
        }

        GlobalState.getInstance().tearDown();

        // Remove notifications when exiting app
        NotificationUtilities.CancelAllNotifications(this);

        EobrReader.getInstance().Shutdown();
        EobrReader.getInstance().setEobrServiceMessenger(null);

        if (mBound) {
            // 9/19/12 JHM - Stop foreground behavior when shutting down
            doUnbindEobrService();
            mBoundService.stopForeground(true);
            mBoundService.stopSelf();
            mBoundService.Shutdown();
        }

        //stop the Geotab service, if it's running.
        this.stopService(new Intent(this, GeotabUsbService.class));

        try {
            // Sleep to allow processes to finish
            // Otherwise you get a null pointer exception
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Log.e("ThreadSleep", e.getMessage() + ": " + Log.getStackTraceString(e));
        }

        this.finish();


        // If the EXIT information is present & true, this activity should
        // terminate
        // and the app should "end".
        Process.killProcess(Process.myPid());
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        this.CreateOptionsMenu(menu, true);
        return true;
    }

    @Override
    public String getActivityMenuItemList() {
        LogEntryController ctrl = getMyController();
        StringBuilder sb = new StringBuilder();

        // Add RodsEntry specific menu items (New Status, Edit Location, Edit Time)
        String[] menuItems = this.getString(R.string.rodsentry_actionitems).split(",");
        for (String item : menuItems) {
            if (IsRodsEntryMenuItemEnabled(item)) {
                sb.append(item).append(",");
            }
        }

        // Add Border Crossing
        if (ctrl.getCurrentUser() != null) {
            if (ctrl.getCurrentUser().IsInternationalDrivingAllowed() && !GlobalState.getInstance().getRoadsideInspectionMode()) {
                sb.append(getString(R.string.rodsentry_bordercrossing)).append(",");
            }
        }

        // Add the Team Driver - Switch menu item (visible if we are in "team driving" mode and driving)
        if (GlobalState.getInstance().getLoggedInUserList().size() > 1) {
            if (GlobalState.getInstance().getCompanyConfigSettings(this).getMultipleUsersAllowed())    // MUA explicitly declared
            {
                String additionalUserItem = getString(R.string.mnuadditionaluserswitch);

                if (IsRodsEntryMenuItemEnabled(additionalUserItem)) {
                    sb.append(additionalUserItem).append(",");
                }
            } else    //assume TD is used
            {
                // in team driving scenario, add team driver switch menu
                String teamDrivingItem = getString(R.string.mnuteamdriverswitch);

                if (IsRodsEntryMenuItemEnabled(teamDrivingItem)) {
                    sb.append(teamDrivingItem).append(",");
                }
            }
        }

        // Add the Dashboard Menu Item if we are in Debug mode, the vehicle is in motion, but status is still On Duty.
        if (GlobalState.getInstance().getFeatureService().getShowDebugFunctions()) {
            String dashboardItem = getString(R.string.lbldashboardtitle);

            if (IsRodsEntryMenuItemEnabled(dashboardItem)) {
                sb.append(dashboardItem).append(",");
            }
        }

        // Add System Menu and Exit
        menuItems = this.getString(R.string.mnu_addsystemmenu).split(",");
        for (String item : menuItems) {
            if (IsRodsEntryMenuItemEnabled(item)) {
                sb.append(item).append(",");
            }
        }

        // Remove the last comma
        if (sb.substring(sb.length() - 1, sb.length()).compareToIgnoreCase(",") == 0) {
            sb.delete(sb.length() - 1, sb.length());
        }

        return sb.toString();
    }

    private boolean IsRodsEntryMenuItemEnabled(String menuItem) {
        LogEntryController ctrl = getMyController();
        boolean isVehicleInMotion = ctrl.IsVehicleInMotion();

        if (menuItem.equals(getResources().getString(R.string.mnu_sysmenu))) {
            return true;
        } else if (GlobalState.getInstance().getRoadsideInspectionMode()) {
            return false;
        } else if (menuItem.equals(getResources().getString(R.string.mnuNewStatus))) {
            return true;
        } else if (menuItem.equals(getResources().getString(R.string.lbldashboardtitle))) {
            boolean showDebug = GlobalState.getInstance().getFeatureService().getShowDebugFunctions();
            // Allow the Dashboard menu item to be visible if we are in debug mode and driving
            return showDebug && isVehicleInMotion;
        } else if (menuItem.equals(getResources().getString(R.string.mnuteamdriverswitch)) || menuItem.equals(getResources().getString(R.string.mnuadditionaluserswitch))) {
            boolean showTeamDriver = (GlobalState.getInstance().getLoggedInUserList().size() > 1);
            // Allow the Team Driver - Switch menu item to be visible if we are in "team driving" mode and driving
            return showTeamDriver && isVehicleInMotion;
        } else {
            // Allow menu options to be added (they will be disabled if not allowed)
            return true;
        }
    }

    @Override
    public void onNavItemSelected(int itemPosition) {
        int menuItemIndex = -1;

        String itemText = this.getLeftNavFragment().GetNavItemText(itemPosition);

        if (itemText.equalsIgnoreCase("Edit Time")) {
            menuItemIndex = 0;
        } else if (itemText.equalsIgnoreCase("Edit Location")) {
            menuItemIndex = 1;
        } else if (itemText.equalsIgnoreCase("New Status")) {
            menuItemIndex = 2;
        } else if (itemText.equalsIgnoreCase("Border Crossing")) {
            menuItemIndex = 3;
        } else if (itemText.equalsIgnoreCase(getString(R.string.mnu_sysmenu))) {
            menuItemIndex = 4;
        } else if (itemText.equalsIgnoreCase(getString(R.string.mnu_filemenu_exit))) {
            menuItemIndex = 5;
        }

        handleNavItem(menuItemIndex);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemID = item.getItemId();
        String itemText = item.getTitle().toString();

        //Handle border crossing navigation
        if (itemText.equalsIgnoreCase(getResources().getString(R.string.rodsentry_bordercrossing))) {
            itemID = 3;
            handleNavItem(itemID);
        }
        // Check for Team Driver - Switch situation.
        else if (itemText.equalsIgnoreCase(getResources().getString(R.string.mnuteamdriverswitch)) || itemText.equalsIgnoreCase(getResources().getString(R.string.mnuadditionaluserswitch))) {
            itemID = 4;
            handleNavItem(itemID);
        } else {
            switch (itemID) {
                case 0:
                case 1:
                case 2:
                    // Handle the default options for RODs entry
                    handleNavItem(itemID);
                    //Create a case to detect touches on home button
                case android.R.id.home:
                    //If feature toggle "Force Crashes" is enabled, the app will crash after the fifth touch
                    if (GlobalState.getInstance().getFeatureService().getIsForceCrashesEnabled()) {
                        countForGenerateError++;
                        if (countForGenerateError >= 5) { //Generate crash after 5 touches
                            Utils.GenerateError();
                        }
                    }
                    break;
                default:
                    // Handle the default system menu options
                    return super.onOptionsItemSelected(item);
            }
        }

        return true;
    }

    protected void handleNavItem(int itemPosition) {
        switch (itemPosition) {
            case 0:
                this.startActivity(RodsEditTime.class);
                break;
            case 1:
                this.getMyController().setLogEventForEdit(EmployeeLogUtilities.GetLastEventInLog(this.getMyController().getCurrentEmployeeLog()));
                startActivity(RodsEditLocation.class, Intent.FLAG_ACTIVITY_SINGLE_TOP);
                break;
            case 2:
                setOffDutyMsgCloseBtnPressed(false);
                this.startActivity(RodsNewStatus.class);
                break;
            case 3:
                this.startActivity(RodsBorder.class);
                break;
            case 4:
                this.startActivity(SwitchUser.class);
                break;
        }
    }

    public void ShowCurrentStatus(boolean autoDisplayEditLocation) {
        this.loadData();
        String currentLogDate = _currentEvents.getString(getResources().getString(R.string.eventvalues_logdate));
        String currentTimestamp = _currentEvents.getString(getResources().getString(R.string.eventvalues_starttime));
        if (this.getIntent().hasExtra(getString(R.string.extra_selectedDutyStatus))) {
            _currentDutyStatus = new DutyStatusEnum(this.getIntent().getIntExtra(this.getString(R.string.extra_selectedDutyStatus), 0));
        } else {
            _currentDutyStatus = new DutyStatusEnum(DutyStatusEnum.NULL).valueOf(getBaseContext(), _currentEvents.getString(getResources().getString(R.string.eventvalues_dutystatus)));
        }

        Location location = null;
        if ((_currentLocation == null || _currentLocation.IsEmpty()) && !autoDisplayEditLocation) {
            location = new Location();
            location.setName(" ");
        } else {
            location = _currentLocation;
        }

        ShowNewStatus(currentLogDate, currentTimestamp, _currentDutyStatus, location, _currentRuleset);

        if (_contentFrag != null && _contentFrag.getEobrConnectionLabel() != null) {
            ConnectionState state = EobrReader.getInstance().getCurrentConnectionState();
            _contentFrag.updateEOBRConnectionState(state);
            _contentFrag.getEobrConnectionLabel().setText(this.getMyController().getStatusMessage());
        }
    }

    private void ShowNewStatus(String logDate, String timestamp, final DutyStatusEnum dutyStatus, Location location, RuleSetTypeEnum ruleset) {
        if (_isExiting) {
            return;
        }

        this.DisplayAnyAlertMessages();
        boolean isReverseGeocodingLocation = EmployeeLogUtilities.IsReverseGeocodingLocationAsync();

        // a location isn't being reverse geocoded and any of the unassigned driving screens are not up,
        // and location is empty, we are not reading historical data, and (either we are not driving or are not the current driver).
        if (location.IsEmpty() && !isReverseGeocodingLocation && !this.IsCurrentActivity(EditLogLocations.class) && EobrReader.getInstance().getCurrentConnectionState() != ConnectionState.READINGHISTORICAL && (!this.getMyController().IsVehicleInMotion() || !this.getMyController().IsCurrentUserTheDriver())) {
            EmployeeLog currentLog = this.getMyController().getCurrentEmployeeLog();
            EmployeeLogEldEvent lastEventInLog = EmployeeLogUtilities.GetLastEventInLog(currentLog);
            this.getMyController().setLogEventForEdit(lastEventInLog);

            // 11/27/12 AMO: When connected to an EOBR that is providing GPS
            // coords, do not ask for manual location entry when logging in to
            // system

            boolean gpsAvail = location.getGpsInfo() != null && !location.getGpsInfo().IsEmpty();

            //when setting the start time prior to the end of an unassigned driving period
            //we can't assume where the driver was so ask for their location
            if (!gpsAvail && EobrReader.getIsEobrDeviceAvailable() && !lastEventInLog.getRequiresManualLocation()) {
                gpsAvail = this.getMyController().getCurrentGPSLocation(this, dutyStatus, location);
            }

            if (!gpsAvail || lastEventInLog.getRequiresManualLocation()) {
                // If we do not have location information AND this is non-mandate, then ask for manual location entry.
                // (For mandate, location won't be handled here.)
                if (!GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
                    this.startActivity(RodsEditLocation.class);
                }
                return;
            }
        }

        // Otherwise switch to the DOT clocks if necessary
        if (ShouldShowDrivingView(dutyStatus)) {
            DismissVerifyDrivingStatusEndDialog();

            if (!IsAlertMessageShowing()) {
                this.ShowDrivingView();
            }

            //notify any active activities that driving is starting.
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(getString(R.string.bm_driving_started)));
        }

        // else, if vehicle is moving and location screen is displayed, display rods entry screen
        // or if the vehicle stopped moving and the DOT clocks are currently displayed, display the rods entry screen
        else if (ShouldDismissDrivingView()) {
            this.DismissDrivingView();
        }

        if (_contentFrag != null) {
            if (_contentFrag.getEobrConnectionLabel() != null) {
                _contentFrag.getEobrConnectionLabel().setText(this.getMyController().getStatusMessage());
            }

            UpdateDutyStatusHeader(logDate, timestamp, dutyStatus, location, ruleset);
            ConnectionState state = EobrReader.getInstance().getCurrentConnectionState();

            FailureController ctrlr = new FailureController(this.getController().getContext());
            EmployeeLog log = this.getMyController().getCurrentEmployeeLog();
            if (ctrlr.AnyFailuresToReport(log)) {
                state = ConnectionState.DEVICEFAILURE; //If a failure exists on the logs, this shows an error message.
            }
            _contentFrag.updateEOBRConnectionState(state);
        }

        // 8/9/12 JHM - Rebuild LeftNav menu items when we show a new status
        // Should provide enabling/disabling appropriate menu items when we're in motion, RSI mode, etc
        BuildLeftNavMenu();

        // Invalidate the menu to update the items
        supportInvalidateOptionsMenu();

        // 2015.07.23 snelson:  If we need to display trip info, then wait until the driving status is done, and there are no alerts already on the screen
        //                      The need to display trip info like this is due to an exempt log being converted to a grid log
        if (_shouldDisplayTripInfo && dutyStatus.getValue() != DutyStatusEnum.DRIVING && !this.IsDrivingViewDisplayed() && !this.IsAlertMessageShowing()) {
            _shouldDisplayTripInfo = false;
            this.startActivity(TripInfo.class);
        }

        // HOS driving alerts
        displayAlertIfNecessary(dutyStatus);
    }

    protected void displayAlertIfNecessary(final DutyStatusEnum dutyStatus) {
        String alertMessage = getMyController().getAlertMessage();
        if (alertMessage != null && alertMessage.length() > 0) {
            ShowAlertMessage(alertMessage, dutyStatus);
        }
    }

    public void ShowAlertMessage(String alertMessage, final DutyStatusEnum dutyStatus) {
        // show a dialog box with the message
        this.ShowMessage(this, R.string.msg_title_alert, alertMessage, new ShowMessageClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

                //processing the onClick will remove the alert that's being
                //dismissed from the list of messages, so the IsAlertMessageShowing()
                //below will determine if there are any additional alerts pending
                super.onClick(dialog, id);

                if (ShouldShowDrivingView(dutyStatus)) {
                    if (!IsAlertMessageShowing()) {
                        ShowClocksDrivingView();
                    }
                }
            }
        });

        if (this.IsCurrentActivity(DOTClocks.class)) {
            this.startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
    }

    private void showInitialPrompts() {
        if (IsAlertMessageShowing()) {
            return;
        }

        if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
            if (GlobalState.getInstance().getCurrentUser().getExemptFromEldUse() && !_hasShownExemptFromELDMessage) {
                showExemptFromELDMessage();
                _hasShownExemptFromELDMessage = true;
            } else {
                showNextPromptAfterExemptFromELDMessage();
            }

            showConfigurationChangedSpecialDrivingCategoryMessage();
        } else {
            DisplayUnassignedDrivingPeriodsPromptAsNeeded();
        }
    }

    private void showConfigurationChangedSpecialDrivingCategoryMessage() {
        ConfigurationSpecialDrivingCategory configurationSpecialDrivingCategory = new ConfigurationSpecialDrivingCategory(this);
        String message = configurationSpecialDrivingCategory.getSpecialDrivingCategoryConfigurationMessage();

        if (message != null) {
            this.ShowMessage(this, 0, message, null);
        }
    }

    private void showExemptFromELDMessage() {
        ShowMessageClickListener onOkHandler = new ShowMessageClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                super.onClick(dialog, id);
                showNextPromptAfterExemptFromELDMessage();
            }
        };
        this.ShowMessage(this, getString(R.string.exempt_from_eld_dialog_title), getString(R.string.exempt_from_eld_dialog_message), onOkHandler);
    }

    private void showNextPromptAfterExemptFromELDMessage() {
        if (shouldShowReviewLogEditPrompt()) {
            showReviewLogEditPrompt();
        } else {
            showNextPromptAfterReviewLogEdit();
        }
    }

    private boolean shouldShowReviewLogEditPrompt() {
        return !GlobalState.getInstance().getReviewLogEditsDialogBeenDisplayedOnceOnRODS() && userHasPendingLogEditsToReview(getMyController().getCurrentUser());
    }

    private void showReviewLogEditPrompt() {
        DialogInterface.OnClickListener onYesHandler = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        Bundle bundle = new Bundle();
                        bundle.putInt(ViewLog.EXTRA_INITIALFRAGINDEX, 7);
                        startActivity(ViewLog.class, bundle);
                    }
                });
            }
        };

        DialogInterface.OnClickListener onNoHandler = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                showNextPromptAfterReviewLogEdit();
            }
        };

        this.DisplayReviewLogEditsDialog(onYesHandler, onNoHandler);
    }

    private void showNextPromptAfterReviewLogEdit() {
        if (!GlobalState.getInstance().getHasCertifyLogsDialogBeenDisplayedOnceOnRODS()) {
            GlobalState.getInstance().setHasCertifyLogsDialogBeenDisplayedOnceOnRODS(true);
            checkForCertifyLogsDialogNeeded();
        }
    }

    private void DisplayUnassignedDrivingPeriodsPromptAsNeeded() {
        //Check if is GeoTab is enabled to prevent two "Unassigned Event" alert messages when using EOBR
        if (!GlobalState.getInstance().getReviewUDPDialogBeenDisplayedOnceOnRODS() && GlobalState.getInstance().getCompanyConfigSettings(this).getIsGeotabEnabled()) {
            GlobalState.getInstance().setReviewUDPDialogBeenDisplayedOnceOnRODS(true);
            CheckForUnassignedDrivingLogs();
        }
    }

    private boolean userHasPendingLogEditsToReview(User user) {
        GlobalState.getInstance().setReviewLogEditsDialogBeenDisplayedOnceOnRODS(true);
        IAPIController empCon = MandateObjectFactory.getInstance(this, GlobalState.getInstance().getFeatureService()).getCurrentEventController();
        List<EmployeeLog> logsThatNeedToBeReviewed = empCon.GetLogsWithUnreviewedEdits(user);
        return !logsThatNeedToBeReviewed.isEmpty();
    }

    private void checkForCertifyLogsDialogNeeded() {
        // Does the user have logs that need to be certified?
        if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled() && !getIsExemptFromELDUse()) {
            DialogInterface.OnClickListener onYesHandler = new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            startActivity(CertifyLogs.class);
                        }
                    });
                }
            };

            DialogInterface.OnClickListener onNoHandler = new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // do nothing -- stay on RODS screen
                    // Once the Geotab mandate is ready we should do this:
                    // DisplayUnassignedDrivingPeriodsPromptAsNeeded();
                }
            };

            // Prompt user to go to "Certify Logs" activity
            if (userHasLogsNeedingCertification(GlobalState.getInstance().getCurrentUser())) {
                this.DisplayCertifyLogsDialog(onYesHandler, onNoHandler);
            } else {
                onNoHandler.onClick(null, 0);
                // Once the Geotab mandate is ready we should do this:
                // DisplayUnassignedDrivingPeriodsPromptAsNeeded();

            }
        }
    }

    private void CheckForUnassignedDrivingLogs() {
        final GeotabController geotabController = new GeotabController(getBaseContext());
        if (geotabController.CheckForUnassignedDrivingPeriods()) {
            this.DisplayUDPAvailableDialog(new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            startActivity(UnassignedDrivingPeriods.class);
                        }
                    });
                }
            }, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
        }
    }

    private boolean userHasLogsNeedingCertification(User user) {
        IAPIController empCon = MandateObjectFactory.getInstance(this, GlobalState.getInstance().getFeatureService()).getCurrentEventController();
        List<Date> datesThatNeedToBeCertified = empCon.GetUncertifiedLogDatesExceptToday(user);
        return !datesThatNeedToBeCertified.isEmpty();
    }

    public void ShowExemptLogConversionMessage(String alertMessage, final DutyStatusEnum dutyStatus) {
        // show a dialog box with the message
        this.ShowMessage(this, R.string.msg_exemptlog_conversion, alertMessage, new ShowMessageClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

                //processing the onClick will remove the alert that's being
                //dismissed from the list of messages, so the IsAlertMessageShowing()
                //below will determine if there are any additional alerts pending
                super.onClick(dialog, id);

                if (ShouldShowDrivingView(dutyStatus)) {
                    if (!IsAlertMessageShowing()) {
                        ShowClocksDrivingView();
                    }
                }
            }
        });

        if (this.IsCurrentActivity(DOTClocks.class)) {
            this.startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
    }

    public boolean ShouldShowDrivingView(DutyStatusEnum dutyStatus) {
        if (!this.getMyController().IsCurrentUserTheDriver())
        // note: if the current user is not the driver, then do not show a driving view
        {
            return false;
        }

        boolean isDriving = (dutyStatus != null && dutyStatus.getValue() == DutyStatusEnum.DRIVING);

        // note: in these modes when driving, the last log event will not be DRV status, so dutyStatus here will not be DRV
        if (GlobalState.getInstance().getIsInPersonalConveyanceDrivingSegment() || GlobalState.getInstance().getIsInHyrailDrivingSegment() || GlobalState.getInstance().getIsInNonRegDrivingSegment()) {
            isDriving = true;
        }

        //Check if in special yard move condition
        if (!isDriving && dutyStatus != null && dutyStatus.getValue() == DutyStatusEnum.ONDUTY && GlobalState.getInstance().getIsInYardMoveDutyStatus() && getMyController().IsVehicleInMotion()) {
            isDriving = true;
        }

        // For the current driver, return true if the duty status is Driving and the vehicle is in motion and a driving view is not currently displayed
        if (GlobalState.getInstance().getLoggedInUserList().size() <= 1) {
            return isDriving && this.getMyController().IsVehicleInMotion() && !(this.IsDrivingViewDisplayed() || this.IsCurrentActivity(Dashboard.class));
        }

        // For team drivers, it should only show once per driving period
        boolean shouldShow = false;
        if (isDriving) {
            if (_teamDriversWereShownDOTClocks) {
                // Only show if back at the RODS entry screen. Allows browsing of the menu.
                shouldShow = this.getMyController().IsVehicleInMotion() && this.IsCurrentActivity(RodsEntry.class);
            } else if (this.getMyController().IsVehicleInMotion()) {
                // Clocks haven't been shown and vehicle is in motion. Show them.
                _teamDriversWereShownDOTClocks = true;
                shouldShow = true;
            }
        } else {
            _teamDriversWereShownDOTClocks = false;
        }
        return shouldShow;
    }

    protected void ShowDrivingView() {
        if (GlobalState.getInstance().getIsInPersonalConveyanceDutyStatus()) {
            // the user is in PC mode
            if (!this.IsCurrentActivity(getClass())) {
                // not currently viewing the Rods home screen, so navigate here first
                // this is done because the dialog is modal and needs to sit on top of RODS
                this.startActivity(getClass(), Intent.FLAG_ACTIVITY_CLEAR_TOP);
            }
            this.ShowPersonalConveyanceDrivingView();
        } else {
            this.ShowClocksDrivingView();
        }
    }

    /**
     * Returns true if the driving view is display, whether it be the personal
     * conveyance view or the DOT clocks screen.
     *
     * @return true if the driving view is currently being displayed and false otherwise
     */
    private boolean IsDrivingViewDisplayed() {
        // return true if either of driving views is being shown (clocks or
        // personal conveyance view)
        boolean answer = false;
        if (_personalConveyanceDrivingView != null) {
            answer = true;
        } else {
            answer = this.IsCurrentActivity(getDrivingViewClass());
        }
        return answer;
    }

    protected void ShowClocksDrivingView() {
        this.startActivity(getDrivingViewClass());
    }

    protected void ShowPersonalConveyanceDrivingView() {
        if (_personalConveyanceDrivingView != null) {
            return;
        }

        if (!this.getMyController().IsCurrentUserTheDriver()) {
            return;
        }

        final String msg = getResources().getString(R.string.msg_personalconveyancedrivingallowed);

        if (GlobalState.getInstance().getLoggedInUserList().size() > 1) {
            final String switchCaption;
            if (GlobalState.getInstance().getCompanyConfigSettings(this).getMultipleUsersAllowed()) {    // MUA explicitly declared
                switchCaption = getResources().getString(R.string.mnuadditionaluserswitch);
            } else {
                switchCaption = getResources().getString(R.string.mnuteamdriverswitch);
            }

            // in team driver mode
            // add a button for Team Driver Switch functionality
            runOnUiThread(new Runnable() {
                public void run() {
                    _personalConveyanceDrivingView = ShowMessageNonDismissable(RodsEntryBase.this, null, msg, switchCaption, new ShowMessageClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            super.onClick(dialog, id);

                            final Runnable showTeamDriverSwitch = new Runnable() {
                                public void run() {
                                    // this is dismissing the dialog to start the team driver switch
                                    _personalConveyanceDrivingView = null;
                                    startActivity(SwitchUser.class);
                                }
                            };
                            showTeamDriverSwitch.run();
                        }
                    });
                }
            });
        } else {
            runOnUiThread(new Runnable() {
                public void run() {
                    _personalConveyanceDrivingView = ShowMessageNonDismissable(RodsEntryBase.this, null, msg);
                }
            });
        }
    }

    protected boolean ShouldDismissDrivingView() {
        boolean shouldDismiss = false;

        if ((this.getMyController().IsVehicleInMotion() && this.IsCurrentActivity(RodsEditLocation.class)) || (!this.getMyController().IsVehicleInMotion() && this.IsDrivingViewDisplayed())) {
            shouldDismiss = true;
        }
        return shouldDismiss;
    }

    private void DismissDrivingView() {
        if (_personalConveyanceDrivingView != null) {
            // the user is in PC mode
            this.DismissPersonalConveyanceDrivingView();
        } else {
            // standard mode, dismiss the clocks
            DismissVerifyDrivingStatusEndDialog();

            // if mandate, show unidentified employee events (UEE), if any (where isReviewed=false)  This is here if the app is opened while DRIVING
            if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled() && hasUnidentifiedEmployeeEventsToReview()) {
                this.startActivity(UnidentifiedELDEvents.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);  // clear top since this may have been opened before the Clocks were displayed
            } else {
                this.startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
            }
        }
    }


    private void DismissPersonalConveyanceDrivingView() {
        if (_personalConveyanceDrivingView != null) {
            _personalConveyanceDrivingView.dismiss();
            this.RemoveAlertMessageFor(_personalConveyanceDrivingView);
            _personalConveyanceDrivingView = null;
        }
    }

    protected Class<?> getDrivingViewClass() {
        return DOTClocks.class;
    }

    public void handleViewLogClick() {
        // 12/6/12 AMO: If the vehicle is in motion and the current user is the
        // driver, do not handle the selected items.
        boolean isVehicleInMotion = false;
        boolean isCurrentUserTheDriver = false;
        LogEntryController ctrl = this.getMyController();
        if (ctrl != null) {
            isVehicleInMotion = ctrl.IsVehicleInMotion();
            isCurrentUserTheDriver = ctrl.IsCurrentUserTheDriver();
        }
        if (!isVehicleInMotion || !isCurrentUserTheDriver) {
            // 11/16/12 AMO: Adding these values to the EmployeeLogController
            // so that the date will be set for today's date by default.
            // 1/10/13 JHM: Changed to use CurrentEmployeeLog
            EmployeeLog empLog = GlobalState.getInstance().getCurrentEmployeeLog();
            if (empLog != null) {
                IAPIController empCon = MandateObjectFactory.getInstance(this, GlobalState.getInstance().getFeatureService()).getCurrentEventController();
                empCon.setSelectedLogForReport(empLog);
                setOffDutyMsgCloseBtnPressed(false);
                startActivity(ViewLog.class);
            } else {
                Toast.makeText(this, getString(R.string.msg_nocurrentlog), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void handleVehicleInspectionClick() {
        // 12/6/12 AMO: If the vehicle is in motion and the current user is the
        // driver, do not handle the selected items.
        boolean isVehicleInMotion = false;
        boolean isCurrentUserTheDriver = false;
        LogEntryController ctrl = this.getMyController();
        if (ctrl != null) {
            isVehicleInMotion = ctrl.IsVehicleInMotion();
            isCurrentUserTheDriver = ctrl.IsCurrentUserTheDriver();
        }
        if (!isVehicleInMotion || !isCurrentUserTheDriver) {
            Bundle extras = new Bundle();
            extras.putString(this.getString(R.string.title), this.getString(R.string.vehicleinspection));
            extras.putInt(this.getString(R.string.menu), R.string.mnu_sysmenu_vehicleinspection);
            this.startActivity(SystemMenu.class, extras);
        }
    }

    public void handleEobrConnectionClick() {
        // 12/6/12 AMO: If the vehicle is in motion and the current user is the
        // driver, do not handle the selected items.
        boolean isVehicleInMotion = false;
        boolean isCurrentUserTheDriver = false;
        LogEntryController ctrl = this.getMyController();
        if (ctrl != null) {
            isVehicleInMotion = ctrl.IsVehicleInMotion();
            isCurrentUserTheDriver = ctrl.IsCurrentUserTheDriver();
        }
        if (!isVehicleInMotion || !isCurrentUserTheDriver) {
            boolean isGeotabEnabled = GlobalState.getInstance().getCompanyConfigSettings(this).getIsGeotabEnabled();

            if (isGeotabEnabled) {
                startActivity(DeviceDiscoveryGeoTab.class);
            } else {
                startActivity(DeviceDiscovery.class);
            }
        }
    }

    public void handleLogoffClick() {
        // 12/6/12 AMO: If the vehicle is in motion and the current user is the
        // driver, do not handle the selected items.
        boolean isVehicleInMotion = false;
        boolean isCurrentUserTheDriver = false;
        LogEntryController ctrl = this.getMyController();
        if (ctrl != null) {
            isVehicleInMotion = ctrl.IsVehicleInMotion();
            isCurrentUserTheDriver = ctrl.IsCurrentUserTheDriver();
        }
        if (!isVehicleInMotion || !isCurrentUserTheDriver) {

            EmployeeLog currentlog = GlobalState.getInstance().getCurrentEmployeeLog();
            boolean isCurrentLogExemptFromELDUse = currentlog != null ? currentlog.getIsExemptFromELDUse() : false;

            if(GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled() &&
                    GlobalState.getInstance().getCurrentUser() != null &&
                    (GlobalState.getInstance().getCurrentUser().getExemptFromEldUse() || isCurrentLogExemptFromELDUse)) {
                SubmitLogsWhenExemptForEldUse(true);
            }
            else {
                this.startActivity(Logout.class);
            }
        }
    }

    public void handleRoadsideInspectionClick() {
        boolean isVehicleInMotion = false;
        boolean isCurrentUserTheDriver = false;
        LogEntryController ctrl = this.getMyController();
        if (ctrl != null) {
            isVehicleInMotion = ctrl.IsVehicleInMotion();
            isCurrentUserTheDriver = ctrl.IsCurrentUserTheDriver();
        }
        if (!isVehicleInMotion || !isCurrentUserTheDriver) {
            this.startActivity(RoadsideInspectionDataTransfer.class);
        }
    }

    public void setOnDutyStatusChangedEvent(IDutyStatusChangeEvent dutyStatusChangedEvent) {
        this._onDutyStatusChangedEvent = dutyStatusChangedEvent;
    }

    public class OnDutyStatusChangedEvent implements IDutyStatusChangeEvent {
        public void onDutyStatusChanged(DutyStatusChangeEventArgs args) {
            final DutyStatusChangeEventArgs e = args;

            // display the new status values sent in from the controller
            runOnUiThread(new Runnable() {
                public void run() {
                    if (e.getLogDate() != null) {
                        ShowNewStatus(DateUtility.getHomeTerminalDateFormat().format(e.getLogDate()), DateUtility.getHomeTerminalDateTimeFormat().format(e.getTimestamp()), e.getDutyStatus(), e.getLocation(), e.getRuleset());
                    } else {
                        ShowCurrentStatus(true);
                    }
                }
            });
        }
    }

    public void setOnEobrReaderChangedEvent(IEobrReaderChangeEvent eobrReaderChangeEvent) {
        this._onEobrReaderChangedEvent = eobrReaderChangeEvent;
    }

    public class OnEobrReaderChangedEvent implements IEobrReaderChangeEvent {
        public void onEventChange(EobrEventArgs e) {

            // 2015.06.10 sjn - fixed a problem in team driving where each teammember is in differet timezone
            //                  Prior to process the events, set the timezone properly for the designated driver
            //					This is done so that any use of DateUtilities will have the timezone set to that of the driver
            //                  because any ELD activity will only effect the driver
            if (getMyController().getLoggedInUserList().size() > 0) {
                DateUtility.setHomeTerminalTimeDateFormatTimeZone(GlobalState.getInstance().getCurrentDesignatedDriver().getHomeTerminalTimeZone().toTimeZone());
            }

            ProcessEobrReaderEventChange(e);

            // afterwards, reset the default time zone to the current user
            if (getMyController().getLoggedInUserList().size() > 0) {
                DateUtility.setHomeTerminalTimeDateFormatTimeZone(GlobalState.getInstance().getCurrentUser().getHomeTerminalTimeZone().toTimeZone());
            }

            UpdateEOBRConnectionStateThreaded();
        }

        private void UpdateEOBRConnectionStateThreaded() {

            // If the current user is not the designated driver,
            // run updateEOBRConnectionSTate on a UI Thread to update the icon
            // if necessary

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!GlobalState.getInstance().getIsCurrentUserTheDesignatedDriver()) {
                        ConnectionState state = EobrReader.getInstance().getCurrentConnectionState();
                        _contentFrag.updateEOBRConnectionState(state);
                        _contentFrag.getEobrConnectionLabel().setText(getMyController().getStatusMessage());
                    }
                }
            });
        }

        public void onVerifySpecialDrivingEnd(EmployeeLogProvisionTypeEnum drivingCategory, EventRecord eventRecord, Location location, EmployeeLog empLog) {
            if (!_isExiting && !GlobalState.getInstance().getIsUserLoggingOut()) {
                Log.d("verifyDrive", "Fired OnVerifySpecialDriveEnd()");
                DisplaySpecialDrivingEndDialog(drivingCategory, eventRecord, location, empLog);
            }
        }

        public void onVerifyDriveEnd() {
            if (!_isExiting && !GlobalState.getInstance().getIsUserLoggingOut()) {
                Log.d("verifyDrive", "FiredOnverifyDriveEnd()");
                DisplayVerifyDrivingDutyStatusEndDialog();
            }
        }

        @Override
        public void onDismissSpecialDrivingDialog(EmployeeLogProvisionTypeEnum drivingCategory) {
            DismissSpecialDrivingEndDialog(drivingCategory);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ShowCurrentStatus(false);
                }
            });
        }

        @Override
        public void onDismissDrivingView() {
            DismissDrivingView();
        }
    }

    @Override
    protected void onSpecialDrivingEndResponse(EmployeeLogProvisionTypeEnum drivingCategory, boolean ended) {
        if (ended && this.getMyController().IsVehicleInMotion()) {
            GlobalState.getInstance().setIsInDriveOnPeriod(true);

            // create a DRIVING status change
            LogEntryController ctrl = getMyController();

            // save the last known, and valid, GPS position and Odometer into the location
            Location newEventLocation = new Location();
            newEventLocation.setGpsInfo(ctrl.getLastValidGPSLocation());
            newEventLocation.setOdometerReading(ctrl.getLastValidOdometerReading());

            Date newEventTime = DateUtility.ConvertToPreviousLogEventTime(new Date());

            EmployeeLog empLog = ctrl.getCurrentDriversLog();
            ctrl.PerformStatusChange(ctrl.getCurrentDesignatedDriver(), empLog, newEventTime, new DutyStatusEnum(DutyStatusEnum.DRIVING), newEventLocation);
        }
        ShowCurrentStatus(false);
    }

    private void ProcessEobrReaderEventChange(EobrEventArgs args) {
        // 10/12/12 JHM - When Event Record exists, process with Gen2 method
        if (args.getEventRecord() != null) {
            this.getMyController().ProcessEobrReaderEvent_Gen2(args);
        } else {
            this.getMyController().ProcessEobrReaderEvent(args);
        }
    }

    public void setOnEobrHistoryChangedEvent(IEobrHistoryChangeEvent eobrHistoryChangeEvent) {
        this._onEobrHistoryChangedEvent = eobrHistoryChangeEvent;
    }

    public class OnEobrHistoryChangedEvent implements IEobrHistoryChangeEvent {
        public void onEventChange(EobrHistoryEventArgs e) {
            ProcessEobrReaderHistoryEvent(e);
        }
    }


    public void setOnEobrHistoryChangEventGenII(IEobrHistoryChangeEventGenII eobrHistoryChangeEventGenII) {
        this._onEobrHistoryChangedEventGenII = eobrHistoryChangeEventGenII;
    }

    public class OnEobrHistoryChangedEventGenII implements IEobrHistoryChangeEventGenII {
        public void onEventChange(EobrGenIIHistoryEventArgs e) {
            ProcessEobrReaderHistoryEventGenII(e);
        }
    }

    private void ProcessEobrReaderHistoryEventGenII(EobrGenIIHistoryEventArgs e) {
        try {
            UnassignedDrivingPeriodResult result = this.getMyController().ProcessEobrReaderHistoryEventGenII(e);

            ProcessUnassignedDrivingPeriodResult(result);
        } catch (KmbApplicationException kae) {
            HandleException(kae);
        }
    }

    public class ExemptLogConvertedToGridLogEventHandler implements IEventHandler<ExemptLogConvertedToGridLogEventArgs> {
        public void onEventChange(ExemptLogConvertedToGridLogEventArgs e) {
            final DutyStatusEnum dutyStatus = EmployeeLogUtilities.GetLastEventInLog(e.getLog()).getDutyStatusEnum();

            if (!e.hasUserBeenNotified()) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        ShowExemptLogConversionMessage(getString(R.string.exemptLogConversionMessage), dutyStatus);
                    }
                });
            }

            // note: delay display of trip info until we get the change to evaluate the current log event so that it's not driving
            _shouldDisplayTripInfo = true;
        }
    }

    public class ExemptLogPreviousLogConvertedToGridLogsEventHandler implements IEventHandler<ExemptLogPreviousLogsConvertedToGridLogEventArgs> {
        public void onEventChange(ExemptLogPreviousLogsConvertedToGridLogEventArgs e) {
            final DutyStatusEnum dutyStatus = EmployeeLogUtilities.GetLastEventInLog(e.getLog()).getDutyStatusEnum();
            final List<Date> dateList = e.getLogDateList();
            final boolean hasConvertedCurrentLog = e.hasConvertedCurrentLog();

            if (hasConvertedCurrentLog || !dateList.isEmpty()) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        StringBuilder msg = new StringBuilder("");

                        if (hasConvertedCurrentLog) {
                            msg.append("Today's log no longer meets exempt log requirements.\n\n");
                        }

                        if (!dateList.isEmpty()) {
                            msg.append(String.format("The following %s previous log(s) no longer meet exempt log requirements:\n", dateList.size()));

                            for (Date logDate : dateList) {
                                msg.append(String.format("%s\n", DateUtility.getHomeTerminalDateFormat().format(logDate)));
                            }

                            msg.append("\n\nThese have all been converted to grid logs.");
                        }

                        ShowExemptLogConversionMessage(msg.toString(), dutyStatus);
                    }
                });

            }

            // note: delay display of trip info until we get the change to evaluate the current log event so that it's not driving
            _shouldDisplayTripInfo = hasConvertedCurrentLog;
        }
    }

    public void setOnUnassignedDrivingPeriodEvent(IEventHandler<UnassignedDrivingPeriodEventArgs> unassignedDrivingPeriodEventHandler) {
        _unassignedDrivingPeriodEventHandler = unassignedDrivingPeriodEventHandler;
    }

    public class UnassignedDrivingPeriodEventHandler implements IEventHandler<UnassignedDrivingPeriodEventArgs> {
        public void onEventChange(UnassignedDrivingPeriodEventArgs e) {
            ProcessUnassignedDrivingPeriodEvent(e);
        }
    }

    /**
     * Private method that pulls the special driving category that was present during a failure
     * (disconnect/failure in EOBR) and displays the dialog box required to end the driving status
     */
    private void resolveSpecialDrivingState() {
        //Get provided special driving provision type
        EmployeeLogProvisionTypeEnum deferredSpecialDrivingProvisionType = provisionTypeEnumAtomicReference.getAndSet(null);
        //Get last EobrEventRecord, current Driver's log, and last known GPS location as this should only be occurring during a failure scenario
        //where we will need to end Special driving
        EventRecord specialDrivingEventRecord = GlobalState.getInstance().getPreviousEobrEventRecord();
        EmployeeLog currentDriverLog = GlobalState.getInstance().getCurrentDriversLog();
        GpsLocation lastKnownGpsLoc = GlobalState.getInstance().getLastGPSLocation();
        Location lastKnownLoc = new Location();
        lastKnownLoc.setGpsInfo(lastKnownGpsLoc);
        //Display the dialog box
        DisplaySpecialDrivingEndDialog(deferredSpecialDrivingProvisionType, specialDrivingEventRecord, lastKnownLoc, currentDriverLog);
    }

    private void ProcessUnassignedDrivingPeriodEvent(UnassignedDrivingPeriodEventArgs e) {
        UnassignedDrivingPeriodResult result = this.getMyController().ProcessUnassignedDrivingPeriodEvent(e);

        ProcessUnassignedDrivingPeriodResult(result);
    }

    private void ProcessUnassignedDrivingPeriodResult(final UnassignedDrivingPeriodResult result) {

        // if mandate, show unidentified employee events (UEE), if any (where isReviewed=false)
        if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled() && hasUnidentifiedEmployeeEventsToReview()) {
            this.startActivity(UnidentifiedELDEvents.class);
        } else {

            if (result.getDetectedDrivingPeriodsForCurrentLog() || result.getDetectedPreloginDrivingPeriods()) {
                // note: This event is raised from the EobrService, so in order to properly display
                //		 the alert message, this needs to run on the UI thread
                runOnUiThread(new Runnable() {
                    public void run() {
                        LockScreenRotation();

                        // driving periods were detected in the history list
                        // that occurred after the current log was created
                        // when the user ok's the message, then display the Unassigned Driving Period screen so they can claim the periods.
                        ShowMessage(RodsEntryBase.this, R.string.msg_title_alert, getString(R.string.msg_detectedunassigneddriving), new ShowMessageClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                // launch the Claim Unassigned Driving periods screen
                                startActivity(UnassignedDrivingPeriods.class);
                                loadData();

                                // unlock the screen on acknowledge of the unassigned driving periods
                                super.onClick(dialog, id);
                            }
                        });

                    }
                });
            }

        }
    }

    private void ProcessEobrReaderHistoryEvent(EobrHistoryEventArgs args) {
        boolean detectedDrivingPeriods;
        final boolean detectedPreloginDrivingPeriods;
        try {
            // allow the controller to process the history event and create all Unassigned Driving periods
            Bundle b = this.getMyController().ProcessEobrReaderHistoryEvent(args);
            detectedDrivingPeriods = b.getBoolean(EobrReader.DETECTEDDRIVINGPERIODS);
            detectedPreloginDrivingPeriods = b.getBoolean(EobrReader.DETECTEDPRELOGINDRIVINGPERIODS);
        } catch (KmbApplicationException kae) {
            HandleException(kae);
            return;
        }

        if (detectedDrivingPeriods || detectedPreloginDrivingPeriods) {
            // note: This event is raised from the EobrService, so in order to properly display
            //		 the alert message, this needs to run on the UI thread
            runOnUiThread(new Runnable() {
                public void run() {
                    LockScreenRotation();

                    // note: the Unassigned Driving screen to claim is launched in the
                    //       event handler associated with the ShowMessage call
                    if (detectedPreloginDrivingPeriods) {
                        // driving periods were detected from before the app was signed into
                        ShowMessage(RodsEntryBase.this, R.string.msg_title_alert, getString(R.string.msg_detectedunassigneddriving_occurredbeforelogin), null);
                    } else {
                        // driving periods were detected in the history list
                        // that occurred after the current log was created
                        // when the user ok's the message, then display the Unassigned Driving Period screen so they can claim the periods.
                        ShowMessage(RodsEntryBase.this, R.string.msg_title_alert, getString(R.string.msg_detectedunassigneddriving), new ShowMessageClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                // launch the Claim Unassigned Driving periods screen
                                startActivity(UnassignedDrivingPeriods.class);
                                loadData();

                                // unlock the screen on acknowledge of the unassigned driving periods
                                super.onClick(dialog, id);
                            }
                        });
                    }
                }
            });
        }

    }

    public void setOnTripReportListener(IEobrTripReportListener listener) {
        _onTripReport = listener;
    }

    public class OnTripReportHandler implements IEobrTripReportListener {
        public void onTripReport(EobrTripReportEventArgs e) {
            processTripReport(e.getTripReport(), e.getSpeedThreshold(), e.getRpmThreshold());
        }
    }

    private void processTripReport(TripReport tripReport, float speedThreshold, int rpmThreshold) {
        TripRecordController controller = new TripRecordController(this);
        controller.ProcessTripReport(tripReport, speedThreshold, rpmThreshold);
    }

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            if (service == null) {
                return;
            }

            // We've bound to LocalService, cast the IBinder and get LocalService instance
            mBoundService = ((EobrService.LocalBinder) service).getService();
            GlobalState.getInstance().setEobrService(mBoundService);
            mService = new Messenger(((EobrService.LocalBinder) service).getMessengerBinder());
            mBound = true;

            // Give the EobrReader a messenger to interact with
            EobrReader.getInstance().setEobrServiceMessenger(mService);

            // We want to monitor the service for as long as we are connected to it.
            try {
                Message msg = Message.obtain(null, EobrServiceMessages.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mService.send(msg);
            } catch (RemoteException e) {
                Log.e("UnhandledCatch", e.getMessage() + ": " + Log.getStackTraceString(e));

                // In this case the service has crashed before we could even
                // do anything with it; we can count on soon being
                // disconnected (and then reconnected if it can be restarted)
                // so there is no need to do anything here.
            }

            // the onBind method of the service (EobrService) is only ever called the first time the service is bound
            // to.  In scenario where a config change occurs that isn't being handled by onConfigChanged method, after
            // destroying activity and recreating, we re-bind to the service.  But the onBind method isn't called -
            // that's what triggers the timer pops.  So we need to resume reading to get the timer pops firing again.
            if (_servicePreviouslyBound) {
                try {
                    Message msg = Message.obtain(null, EobrServiceMessages.MSG_RESUME_READING);

                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } catch (Throwable e) {
                    Log.e("UnhandledCatch", e.getMessage() + ": " + Log.getStackTraceString(e));
                }
            }

            // Check if there is an app update based on the FW version of the BTE
            if (mBoundService != null) {
                mBoundService.ApplicationUpdate(false);
            }
        }

        public void onServiceDisconnected(ComponentName arg0) {
            mService = null;
            mBound = false;
        }
    };

    /**
     * Handler of incoming messages from service.
     */
    private static class IncomingHandler extends Handler {
        private final WeakReference<RodsEntryBase> activity;
        private final HosAuditController hosAuditController;

        private IncomingHandler(RodsEntryBase activity) {
            this.activity = new WeakReference<RodsEntryBase>(activity);
            this.hosAuditController = new HosAuditController(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

                case EobrServiceMessages.MSG_HEARTBEAT:
                    Log.v("RodsEntry", "Heartbeat received from EobrService");

                    // Keep track of when Heartbeat responses are received from the EobrService
                    if (activity.get() != null) {
                        activity.get().mReveivedHeartbeatResponse = true;
                    }

                    // Also check if a server update should be performed for hours available summaries
                    hosAuditController.PerformPeriodicServerUpdate();

                    //make sure we can present on-duty alert notifications in non-online ELD scenarios
                    //for 100 Air Mile exemption users
                    ConnectionState state = EobrReader.getInstance().getCurrentConnectionState();
                    if (state == ConnectionState.OFFLINE || state == ConnectionState.SHUTDOWN || state == ConnectionState.DEVICEFAILURE) {
                        //find the current user's current duty status
                        EmployeeLog log = GlobalState.getInstance().getCurrentEmployeeLog();
                        if (log != null) {
                            EmployeeLogEldEvent lastEventInLog = EmployeeLogUtilities.GetLastEventInLog(log);

                            if (lastEventInLog != null) {
                                DutyStatusEnum dutyStatus = lastEventInLog.getDutyStatusEnum();

                                if (activity.get() != null) {
                                    activity.get().displayAlertIfNecessary(dutyStatus);
                                }
                            }
                        }
                    }

                    break;

                default:
                    super.handleMessage(msg);
            }
        }
    }

    private void doUnbindEobrService() {
        unbindService(mConnection);
    }

    private CheckForOpenDVIR mCheckForOpenDVIR;

    private class CheckForOpenDVIR extends AsyncTask<Void, Void, Void> {
        boolean hasOpenDVIR = false;
        GlobalState gs = GlobalState.getInstance();

        protected void onPreExecute() {
            _openDVIRPromptShowing = true;
            LockScreenRotation();
        }

        protected Void doInBackground(Void... params) {
            EobrConfigController ecc = new EobrConfigController(gs);

            if (ecc.getIsWebServicesAvailable()) {
                VehicleInspectionController vic = new VehicleInspectionController(gs);

                if (ecc.getSerialNumber() != null) {
                    if (vic.CheckForOpenDVIR(ecc.getSerialNumber()) == true) {
                        hasOpenDVIR = true;
                    } else {
                        //Comes here if there is not an open dvir. Setting to true means it will not
                        //show the notification if they add a new dvir which would then be considered 'open' if it needed review
                        hasOpenDVIR = false;
                        _openDVIRPromptShowing = false;
                    }
                }
            }

            return null;
        }

        protected void onPostExecute(Void unused) {
            if (hasOpenDVIR) {
                ShowMessage(RodsEntryBase.this, 0, getString(R.string.lblopendvir), new ShowMessageClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        _checkForDVIR = false;
                        _openDVIRPromptShowing = false;
                        startActivity(VehicleInspectionReview.class);
                        super.onClick(dialog, id);
                    }
                });

            } else {
                _checkForDVIR = false;
                UnlockScreenRotation();
            }
        }
    }


    private Handler _checkForPendingEOBRDiagnosticCommandsHandler;

    private void scheduleCheckForPendingEOBRDiagnosticCommands() {
        if (_checkForPendingEOBRDiagnosticCommandsHandler == null) {
            _checkForPendingEOBRDiagnosticCommandsHandler = new Handler();
            _checkForPendingEOBRDiagnosticCommandsHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    new CheckForPendingEOBRDiagnosticCommands().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }, 60000);
        }
    }

    private class CheckForPendingEOBRDiagnosticCommands extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            if (EobrReader.getInstance().getCurrentConnectionState() != EobrReader.ConnectionState.FIRMWAREUPDATE) {
                EobrDiagnosticCommandController edcc = new EobrDiagnosticCommandController(RodsEntryBase.this);
                if (edcc.DownloadPendingEobrDiagnosticCommands()) {
                    _checkForPendingEOBRDiagnosticCommands = false;
                }
                edcc.ExecutePendingCommands();
                edcc.SubmitEobrDiagnosticCommandsToDMO();
            }
            return null;
        }

        protected void onPostExecute(Void unused) {
            _checkForPendingEOBRDiagnosticCommandsHandler = null;
        }
    }


    // The BroadcastReceiver that listens for time zone change on device
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (Intent.ACTION_TIME_CHANGED.equals(intent.getAction())) {
                    // The time on the device clock has been changed
                    // There are two possible ways this can happen.
                    // 1. The user manually changed the time
                    // 2. With the Automatic Date/Time setting checked, then cellular network time could set the clock

                    EobrReader.getInstance().ReportDeviceClockTimeChange(context);
                } else if (Intent.ACTION_DATE_CHANGED.equals(intent.getAction())) {
                    // The date of the device clock has been changed.
                    // There are two times that we see this
                    // 1. When the device rolls across midnight, this is fired
                    // 2. If the user manually changes the Date
                    ErrorLogHelper.RecordMessage(RodsEntryBase.this, "Device Clock Date change detected.");
                } else if (Intent.ACTION_DEVICE_STORAGE_LOW.equals(intent.getAction())) {
                    ErrorLogHelper.RecordMessage(RodsEntryBase.this, "Device Storage Low detected.");
                }
            } catch (Throwable e) {
                ErrorLogHelper.RecordException(RodsEntryBase.this, e);
                e.printStackTrace();
            }
        }
    };

    private Handler _timerHandler;

    private void CreateHeartbeatTimer() {
        // create a timer that will pop every 60 seconds and send a HEARTBEAT message to the EOBR Service task
        if (_timerHandler == null) {
            _timerHandler = new Handler();
            _timerHandler.removeCallbacks(_heatbeatTimerTask);
            _timerHandler.postDelayed(_heatbeatTimerTask, 60000);
        }
    }

    private void startAndBindEobrService() {
        Intent intent = new Intent(this, EobrService.class);
        startService(intent);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        // Must reset the heartbeat response variable after restarting the service.
        mReveivedHeartbeatResponse = true;
    }

    private Runnable _heatbeatTimerTask = new Runnable() {
        public void run() {
            // Send a HEARTBEAT message to the EOBR service task.
            // If the Messenger (which is bound to EobrService) is null or we did not receive a Heartbeat message response, then restart the EobrService.
            if (mService == null || !mReveivedHeartbeatResponse) {
                startAndBindEobrService();
            } else {
                try {
                    mReveivedHeartbeatResponse = false;

                    Message msg = Message.obtain(null, EobrServiceMessages.MSG_HEARTBEAT);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } catch (Throwable e) {
                    Log.e("UnhandledCatch", e.getMessage() + ": " + Log.getStackTraceString(e));
                }
            }

            // set the timer to pop again in 60 seconds
            _timerHandler.removeCallbacks(_heatbeatTimerTask);
            _timerHandler.postDelayed(_heatbeatTimerTask, 60000);
        }
    };

    // resets the open DVIR flag so when a new EOBR is activated the notification
    // shows up again if there is an open DVIR
    public static void resetOpenDVIRFlag() {
        _openDVIRPromptShowing = false;
        _checkForDVIR = true;
    }

    public void ShowConfirmationMessage(String message, final Runnable yesAction, final Runnable noAction) {

        ShowConfirmationMessage(this, message, new ShowMessageClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        super.onClick(dialog, id);

                        if (yesAction != null) {
                            yesAction.run();
                        }
                    }
                },

                new ShowMessageClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        super.onClick(dialog, id);

                        if (noAction != null) {
                            noAction.run();
                        }
                    }
                });
    }

    public BaseActivity getActivity() {
        return this;
    }

    public void onLogDownloadFinished(boolean logsFound) {
        if (!logsFound) {
            HandleStartupCompletion(true);
        }
    }

    public void onOffDutyLogsCreated(boolean logsCreated) {
        HandleStartupCompletion(true);
    }

    @Override
    protected void Return(boolean exitApp) {
        if (mRetryDialog == null || !mRetryDialog.isShowing()) {

            if (exitApp) {
                this.finish();

                GlobalState.getInstance().setIsUserLoggingOut(false);

                // exit the app....use the Logout, which does the rods with the exit intent.
                Bundle extras = new Bundle();
                extras.putBoolean(this.getString(R.string.exit),true);
                this.startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_CLEAR_TOP, extras);

                UnlockScreenRotation();
            }
        }
    }
}
