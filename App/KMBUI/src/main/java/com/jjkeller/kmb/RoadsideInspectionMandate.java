package com.jjkeller.kmb;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.jjkeller.kmb.fragments.DateSelectorFrag;
import com.jjkeller.kmb.fragments.LeftNavFrag;
import com.jjkeller.kmb.fragments.RoadsideInspectionMandateFrag;
import com.jjkeller.kmb.fragments.RoadsideInspectionMandateLogDetailFrag;
import com.jjkeller.kmb.fragments.RoadsideInspectionUnidentifiedLogDetailFrag;
import com.jjkeller.kmb.fragments.RptDOTAuthorityFrag;
import com.jjkeller.kmb.fragments.RptGridImageFrag;
import com.jjkeller.kmb.interfaces.IDateSelectorFrag;
import com.jjkeller.kmb.interfaces.IRoadsideInspectionMandate;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmb.share.GridLogData;
import com.jjkeller.kmbapi.common.TimeKeeper;
import com.jjkeller.kmbapi.configuration.LoginCredentials;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.ControllerFactory;
import com.jjkeller.kmbapi.controller.EmployeeLogEldMandateController;
import com.jjkeller.kmbapi.controller.EobrConfigController;
import com.jjkeller.kmbapi.controller.LogEntryController;
import com.jjkeller.kmbapi.controller.LogGridSummary;
import com.jjkeller.kmbapi.controller.abstracts.APIControllerBase;
import com.jjkeller.kmbapi.controller.dataaccess.UserFacade;
import com.jjkeller.kmbapi.controller.interfaces.IAPIController;
import com.jjkeller.kmbapi.controller.share.MandateObjectFactory;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.controller.utility.EmployeeLogUtilities;
import com.jjkeller.kmbapi.employeelogeldevents.EmployeeLogEldEventCode;
import com.jjkeller.kmbapi.enums.DataDiagnosticEnum;
import com.jjkeller.kmbapi.enums.TimeZoneEnum;
import com.jjkeller.kmbapi.kmbeobr.DistanceAndHours;
import com.jjkeller.kmbapi.proxydata.CompanyConfigSettings;
import com.jjkeller.kmbapi.proxydata.EldEventAdapterList;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;
import com.jjkeller.kmbapi.proxydata.GpsLocation;
import com.jjkeller.kmbapi.proxydata.TeamDriver;
import com.jjkeller.kmbui.R;

import org.joda.time.LocalTime;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

public class RoadsideInspectionMandate extends BaseActivity
        implements IRoadsideInspectionMandate.RoadsideInspectionMandateFragControllerMethods,
        IDateSelectorFrag.DateSelectorControllerMethods,
        LeftNavFrag.OnNavItemSelectedListener,
        LeftNavFrag.ActivityMenuItemsListener {


    private static final String BUNDLE_NAVIGATION_INDEX = "index";

    private int _currentItemIndex;
    private Date _selectedDate = null;
    public boolean _isSelectedDateToday = false;
    private RoadsideInspectionMandateData _data;
    private RoadsideInspectionMandateFrag _contentFrag;
    private EmployeeLogEldMandateController _eldMandateController;
    private LogEntryController _logEntryController;
    private EobrConfigController _eobrConfigController;
    private IAPIController _controllerEmp;
    private RptGridImageFrag _gridImageFrag;
    private GridLogData _gridLogData = new GridLogData();
    private RoadsideInspectionMandateLogDetailFrag _logDetailFrag;
    private RoadsideInspectionUnidentifiedLogDetailFrag _unidentifiedLogDetailFrag;
    private boolean _dataLoaded = false;
    private boolean _unidentifiedDriverInfo = false;
    private boolean activity_result;
    private LinearLayout driver_wrapper;
    private FrameLayout unidentifiedDriverFrame;
    private FrameLayout dateNavFrame;
    private ScrollView scrollView;
    private View flRptDotAuthorityReportContainer;
    private EmployeeLogEldEvent[] unidentifiedEvents;
    private int _editLogEventPosition;
    private String _editLogEventTitle;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        activity_result = true;
    }

    @Override
    public void onResume() {
        if (activity_result) {
            setLeftNavSelectedItem(_currentItemIndex);
            loadLeftNavFragment();
        }
        super.onResume();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (savedInstanceState != null) {
            _editLogEventPosition = savedInstanceState.getInt(DateSelectorFrag.BUNDLE_SELECTOR_POSITION, 0);
            _editLogEventTitle = savedInstanceState.getString(DateSelectorFrag.BUNDLE_SELECTOR_TITLE, "");
        }

        //controller set up
        _eldMandateController = ControllerFactory.getInstance().getEmployeeLogEldMandateController();
        _logEntryController = ControllerFactory.getInstance().getLogEntryController();
        _eobrConfigController = ControllerFactory.getInstance().getEobrConfigController();
        _controllerEmp = MandateObjectFactory.getInstance(this,GlobalState.getInstance().getFeatureService()).getCurrentEventController();
        unidentifiedEvents =  _controllerEmp.fetchPreviousWeekUnidentifiedDriverEvents(_eobrConfigController.getSerialNumber());

        //initialize views
        setContentView(R.layout.roadsideinspectionmandate);
        scrollView = (ScrollView) findViewById(R.id.rsi_mandate_scroll_view);
        driver_wrapper = (LinearLayout) findViewById(R.id.driver_wrapper);
        unidentifiedDriverFrame = (FrameLayout) findViewById(R.id.unidentified_log_detail_fragment);
        dateNavFrame = (FrameLayout) findViewById(R.id.datenav_fragment);
        flRptDotAuthorityReportContainer = findViewById(R.id.fl_rpt_dot_authority_report_container);

        //load fragments
        loadContentFragment(new RoadsideInspectionMandateFrag());
        loadFragment(R.id.grid_image_fragment, new RptGridImageFrag());
        loadDateNavFragment();
        loadFragment(R.id.log_detail_fragment, new RoadsideInspectionMandateLogDetailFrag());
        loadFragment(R.id.unidentified_log_detail_fragment, new RoadsideInspectionUnidentifiedLogDetailFrag());

        flRptDotAuthorityReportContainer.setVisibility(View.GONE);

        final boolean isMotionPictureEnabled = GlobalState.getInstance().getCompanyConfigSettings(this).getIsMotionPictureEnabled();
        final boolean areThereUnidentifiedEvents = unidentifiedEvents != null && unidentifiedEvents.length != 0;

        if (savedInstanceState == null){

            if (isMotionPictureEnabled) {
                loadFragment(R.id.fl_rpt_dot_authority_report_container, RptDOTAuthorityFrag.newInstance());
            }

            _unidentifiedDriverInfo = false;
        }else{
            _currentItemIndex = savedInstanceState.getInt(BUNDLE_NAVIGATION_INDEX);
            if (_currentItemIndex == 0) {
                _unidentifiedDriverInfo = false;
                flRptDotAuthorityReportContainer.setVisibility(View.GONE);
            } else if (_currentItemIndex == 1){
                if (areThereUnidentifiedEvents){
                    driver_wrapper.setVisibility(View.GONE);
                    dateNavFrame.setVisibility(View.GONE);
                    flRptDotAuthorityReportContainer.setVisibility(View.GONE);
                    unidentifiedDriverFrame.setVisibility(View.VISIBLE);
                    _unidentifiedDriverInfo = true;
                }else if (isMotionPictureEnabled){
                    flRptDotAuthorityReportContainer.setVisibility(View.VISIBLE);
                }
            }else if (_currentItemIndex == 2 && isMotionPictureEnabled){
                flRptDotAuthorityReportContainer.setVisibility(View.VISIBLE);
            }


        }

        this.setLeftNavSelectedItem(_currentItemIndex);
        this.setLeftNavAllowChange(true);
        loadLeftNavFragment();

        //set ActionBar title and color
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            boolean isEldMandateEnabled = GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled();
            BaseActivity.addHeaderEldIdentifier(this, actionBar, isEldMandateEnabled, true);
            actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.header_orange)));
        }
    }

    @Override
    protected void loadData() {
        super.loadData();
        loadMandateData();
        loadGridData(null);
        _dataLoaded = true;
    }

    @Override
    protected void loadControls(Bundle savedInstanceState) {
        super.loadControls(savedInstanceState);
        if (_contentFrag != null) {
            _contentFrag.loadControls();
        }

        if(_gridImageFrag != null){
            FormatGrid();
        }
        loadGridData(savedInstanceState);

        if (_unidentifiedLogDetailFrag != null) {
            _unidentifiedLogDetailFrag.loadControls(unidentifiedEvents);
        }
        if(_logDetailFrag != null)
        {
            _logDetailFrag.loadControls();
        }
        setLeftNavSelectedItem(_currentItemIndex);
        setLeftNavSelectionItems();
    }

    public String getActivityMenuItemList() {
        final boolean isMotionPictureEnabled = GlobalState.getInstance().getCompanyConfigSettings(this).getIsMotionPictureEnabled();

        //display only driver info and exit nav items when there is no unidentified driver records
        if (unidentifiedEvents == null || unidentifiedEvents.length == 0) {
            if (isMotionPictureEnabled){
                return getString(R.string.roadside_header_actionitems_no_undentified);
            }else{
                return getString(R.string.roadside_header_actionitems_no_undentified_no_motion_picture);
            }
        }else if (isMotionPictureEnabled){
            return getString(R.string.roadside_header_actionitems);
        }else{
            return getString(R.string.roadside_header_actionitems_no_motion_picture);
        }
    }

    protected void InitController() {
    }

    @Override
    public void setFragments() {
        super.setFragments();

        if (_selectedDate == null) {
            _selectedDate = DateUtility.getCurrentDateTimeUTC();
            _isSelectedDateToday = DateUtility.IsToday(_selectedDate, GlobalState.getInstance().getCurrentUser());
        }

        Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
        _contentFrag = (RoadsideInspectionMandateFrag) f;
        _contentFrag.loadControls();

        Fragment g = getSupportFragmentManager().findFragmentById(R.id.grid_image_fragment);
        _gridImageFrag = (RptGridImageFrag) g;

        if (_gridImageFrag != null && _dataLoaded) {
            FormatGrid();
        }

        Fragment ldDriver = getSupportFragmentManager().findFragmentById(R.id.log_detail_fragment);
        Fragment ldUnidentified = getSupportFragmentManager().findFragmentById(R.id.unidentified_log_detail_fragment);

        _logDetailFrag = (RoadsideInspectionMandateLogDetailFrag) ldDriver;
        if (_logDetailFrag != null) {
            _logDetailFrag.loadControls();
        }

        _unidentifiedLogDetailFrag = (RoadsideInspectionUnidentifiedLogDetailFrag) ldUnidentified;
        if (_unidentifiedLogDetailFrag != null) {
            _unidentifiedLogDetailFrag.loadControls(unidentifiedEvents);
        }
    }

    private User getCoDriver(EmployeeLog employeeLog) {
        GlobalState globalState = GlobalState.getInstance();

        if (_isSelectedDateToday)
        {
            if (globalState.getLoggedInUserList().size() > 1) {
                for (User user : globalState.getLoggedInUserList()) {
                    if (user != globalState.getCurrentUser()) {
                        return user;
                    }
                }
            }
        }
        else
        {
            if (!employeeLog.getTeamDriverList().IsEmpty())
            {
                for (TeamDriver teamDriver : employeeLog.getTeamDriverList().getTeamDriverList())
                {
                    if (!teamDriver.getEmployeeId().equals(globalState.getCurrentUser().getCredentials().getEmployeeId())) {
                        User u = new User();
                        LoginCredentials credentials = new LoginCredentials();
                        credentials.setEmployeeFullName(teamDriver.getDisplayName());
                        credentials.setUsername(teamDriver.getKMBUsername());
                        u.setCredentials(credentials);
                        return u;
                    }
                }
            }
        }
        return null;
    }

    private EmployeeLog getLog() {
        if (_isSelectedDateToday) {
            return GlobalState.getInstance().getCurrentEmployeeLog();
        }
        else {
            return _controllerEmp.GetEmployeeLog(_selectedDate);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(BUNDLE_NAVIGATION_INDEX, _currentItemIndex);
        outState.putInt(DateSelectorFrag.BUNDLE_SELECTOR_POSITION, _editLogEventPosition);
        outState.putString(DateSelectorFrag.BUNDLE_SELECTOR_TITLE, _editLogEventTitle);

        super.onSaveInstanceState(outState);

    }

    private GpsLocation getLocation(EmployeeLogEldEvent lastEldEvent) {
        if (_isSelectedDateToday) {
            return _logEntryController.getCurrentGPSLocation();
        }
        else {
            return lastEldEvent.getLocation().getGpsInfo();
        }
    }

    private float getOdometer(EmployeeLogEldEvent lastEldEvent) {
        if (_isSelectedDateToday) {
            return _eobrConfigController.GetRawOdometer();
        }
        else {
            if (lastEldEvent.getOdometer() != null){
                return lastEldEvent.getOdometer().floatValue();
            }
            else {
                return -1.0F;
            }
        }
    }

    private LocalTime getLogStartTime(User user)
    {
        Date dateWithStartTime = EmployeeLogUtilities.CalculateLogStartTime(this, _selectedDate, user.getHomeTerminalTimeZone());
        return LocalTime.fromDateFields(dateWithStartTime);
    }

    private DistanceAndHours getDistanceAndHours(EmployeeLog empLog) {
        DistanceAndHours distanceAndHours = null;

        if (_isSelectedDateToday) {
            distanceAndHours = _eldMandateController.GetEngineHoursAndAccumulatedVehicleMiles(empLog);
        }
        else {
            distanceAndHours = _eldMandateController.GetEngineHoursAndAccumulatedVehicleMiles(DateUtility.AddDays(_selectedDate,1), empLog);
        }

        if (distanceAndHours == null) {
            distanceAndHours = CalculateEngineHoursAndAccumulatedVehicleMilesFromDriveEvents(empLog);
        }

        return distanceAndHours;
    }

    /**
     * Manually calculate total distance using beginning and ending odometer for each Drive event
     */
    private DistanceAndHours CalculateEngineHoursAndAccumulatedVehicleMilesFromDriveEvents(EmployeeLog employeeLog) {
        int totalVehicleMiles = 0;
        DistanceAndHours result = new DistanceAndHours();

        if (employeeLog != null) {
            EmployeeLogEldEvent[] eldEvents = employeeLog.getEldEventList().getActiveEldEventList(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus);
            if (eldEvents != null) {
                for (int i = 0; i < eldEvents.length; i++) {
                    EmployeeLogEldEvent event = eldEvents[i];

                    if (event.getEventCode() == EmployeeLogEldEventCode.DutyStatus_Driving) {
                        if (event.getOdometer() != null) {
                            if (event.getEndOdometer() == null && event.getDistance() != null) {
                                // EndOdometer is not persisted in Encompass.  Rather, a distance value is calculated and persisted upon submission to Encompass.
                                // So if you are attempting to calculate the distance for a log other than today that has been submitted to and downloaded from Encompass, they probably will not have an EndOdometer value.
                                totalVehicleMiles += event.getDistance();
                            }
                            else if (event.getEndOdometer() != null && event.getEndOdometer() > 0) {
                                totalVehicleMiles += (int)event.getEndOdometer().floatValue() - (int)event.getOdometer().floatValue();
                            }
                        }
                    }
                }
            }
        }

        result.setTotalVehicleMiles(totalVehicleMiles);

        return result;
    }

    private boolean isUnidentifiedDrivingIndicatorActive() {
        if (_selectedDate == null) {
            return _eldMandateController.IsDataDiagnosticActive(DataDiagnosticEnum.UNIDENTIFIED_DRIVING_RECORDS);
        } else {
            Date beforeTimestamp = DateUtility.AddDays(_selectedDate, 1);
            return _eldMandateController.IsDataDiagnosticActive(DataDiagnosticEnum.UNIDENTIFIED_DRIVING_RECORDS, beforeTimestamp);
        }
    }

    private boolean getDataDiagnosticIndicatorStatus(EmployeeLogEldEvent lastEldEvent) {
        if (_isSelectedDateToday) {
            return ! _eldMandateController.getActiveDataDiagnostics(getLog()).isEmpty();
        }
        else
            return lastEldEvent.getDriverDataDiagnosticEventIndicatorStatus();
    }

    private boolean getMalfunctionIndicatorStatus(EmployeeLogEldEvent lastEldEvent) {
        if (_isSelectedDateToday) {

            return ! _eldMandateController.getActiveMalfunctions(getLog()).isEmpty();
        }
        else
            return lastEldEvent.getEldMalfunctionIndicatorStatus();
    }

    private String getShipmentId(HashMap<EmployeeLog.TripInfoPropertyKey, HashSet<String>> tripInfoMap) {
        return EmployeeLogUtilities.TripInfoToString(tripInfoMap, EmployeeLog.TripInfoPropertyKey.ShipmentInfo);
    }

    private String getTrailerNumber(HashMap<EmployeeLog.TripInfoPropertyKey, HashSet<String>> tripInfoMap) {
        return EmployeeLogUtilities.TripInfoToString(tripInfoMap, EmployeeLog.TripInfoPropertyKey.TrailerNumber);
    }

    private String getTruckTractorId(HashMap<EmployeeLog.TripInfoPropertyKey, HashSet<String>> tripInfoMap) {
        return EmployeeLogUtilities.TripInfoToString(tripInfoMap, EmployeeLog.TripInfoPropertyKey.TractorNumber);
    }

    private String getTruckVin() {
        return _eobrConfigController.GetTruckVin();
    }

    @Override
    public RoadsideInspectionMandateData getRoadsideInspectionMandateData() {
        return _data;
    }

    private void loadDateNavFragment() {
        FrameLayout layout = (FrameLayout) findViewById(R.id.datenav_fragment);

        Bundle bundle = new Bundle();
        bundle.putInt(DateSelectorFrag.BUNDLE_SELECTOR_POSITION, _editLogEventPosition);
        bundle.putString(DateSelectorFrag.BUNDLE_SELECTOR_TITLE, "");

        DateSelectorFrag _dateSelectorFrag = new DateSelectorFrag();
        _dateSelectorFrag.setArguments(bundle);
        loadFragment(R.id.datenav_fragment, _dateSelectorFrag);
        if (layout != null)
            layout.setVisibility(View.VISIBLE);
    }

    @Override
    public IAPIController getEmployeeLogController() {
        return _eldMandateController;
    }

    @Override
    public void handleDateChange(Date selectedDate, int position) {
        _selectedDate = selectedDate;
        _editLogEventPosition = position;
        _isSelectedDateToday = DateUtility.IsToday(_selectedDate, GlobalState.getInstance().getCurrentUser());

        mFetchLocalDataTask = new FetchLocalDataTask(this.getClass().getSimpleName(), null);
        mFetchLocalDataTask.execute();
    }

    @Override
    public void handleEditLog() {
        // this is a method for the date selection fragment, but not used here.
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
//        this.CreateOptionsMenu(menu, false);
        return true;
    }

    @Override
    public void onNavItemSelected(int itemPosition) {
        final boolean isMotionPictureEnabled = GlobalState.getInstance().getCompanyConfigSettings(this).getIsMotionPictureEnabled();
        final boolean areThereUnidentifiedEvents = unidentifiedEvents != null && unidentifiedEvents.length != 0;

        switch (itemPosition) {
            case 0:
                // Driver Info
                _currentItemIndex = 0;
                if (_unidentifiedDriverInfo) {
                    _unidentifiedDriverInfo = false;
                    unidentifiedDriverFrame.setVisibility(View.GONE);
                    dateNavFrame.setVisibility(View.VISIBLE);
                    driver_wrapper.setVisibility(View.VISIBLE);
                }

                flRptDotAuthorityReportContainer.setVisibility(View.GONE);

                break;
            case 1:
                _currentItemIndex = 1;

                if (areThereUnidentifiedEvents) {
                    // Unidentified Driver
                    if (!_unidentifiedDriverInfo) {
                        _unidentifiedDriverInfo = true;
                        driver_wrapper.setVisibility(View.GONE);
                        dateNavFrame.setVisibility(View.GONE);
                        flRptDotAuthorityReportContainer.setVisibility(View.GONE);
                        unidentifiedDriverFrame.setVisibility(View.VISIBLE);
                    }
                } else if (isMotionPictureEnabled){
                    flRptDotAuthorityReportContainer.setVisibility(View.VISIBLE);
                }else{
                    startActivityForResult(RoadsideInspection.class, 1);
                }
                break;
            case 2:
                _currentItemIndex = 2;

                if (areThereUnidentifiedEvents && isMotionPictureEnabled){

                    flRptDotAuthorityReportContainer.setVisibility(View.VISIBLE);
                } else{
                    startActivityForResult(RoadsideInspection.class, 1);
                }
                break;
            case 3:
                // Done
                startActivityForResult(RoadsideInspection.class, 1);
                break;
        }
        scrollView.scrollTo(0,0);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Handle the back button
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // Say that we've consumed the event
            return true;
        }

        // Otherwise let system handle keypress normally
        return super.onKeyDown(keyCode, event);
    }


    private void loadMandateData() {
        RoadsideInspectionMandateData data = new RoadsideInspectionMandateData();
        data.companyConfigSettings = GlobalState.getInstance().getCompanyConfigSettings(this);
        data.user = GlobalState.getInstance().getCurrentUser();
        data.currentLog = getLog();
        data.coDriver = getCoDriver(data.currentLog);
        data.lastEldEvent = EmployeeLogUtilities.GetLastEventInLog(data.currentLog);
        data.currentLocation = getLocation(data.lastEldEvent);
        data.currentOdometer = getOdometer(data.lastEldEvent);
        data.accumulatedDistanceAndHours = getDistanceAndHours(data.currentLog);
        data.isUnidentifiedDrivingRecordsIndicatorActive = isUnidentifiedDrivingIndicatorActive();
        data.malfunctionIndicatorStatus = getMalfunctionIndicatorStatus(data.lastEldEvent);
        data.dataDiagnosticIndicatorStatus = getDataDiagnosticIndicatorStatus(data.lastEldEvent);
        data.logStartTime = getLogStartTime(data.user);
        HashMap<EmployeeLog.TripInfoPropertyKey, HashSet<String>> tripInfoMap = _controllerEmp.GetTripInfoForLog(data.currentLog);
        data.shippingId = getShipmentId(tripInfoMap);
        data.trailerNumber = getTrailerNumber(tripInfoMap);
        data.truckTractorId = getTruckTractorId(tripInfoMap);
        data.truckVin = getTruckVin();
        this._data = data;
    }

    private void loadGridData(Bundle savedInstanceState)
    {
        if (savedInstanceState != null)	{
            _gridLogData.loadDataFromSavedState(savedInstanceState);
        }
        else
        {
            boolean keepDate = false;

            if (getIntent().getExtras() != null) {
                // Check to see if the log date should not be changed
                keepDate = getIntent().getExtras().getBoolean(getString(R.string.state_keepdate), false);

                // reset the keepdate flag
                getIntent().getExtras().putBoolean(getString(R.string.state_keepdate), false);
            }

            if (!keepDate && !GlobalState.getInstance().isReviewEldEvent() )
            {
                try {
                    getEmployeeLogController().setSelectedLogForReport(getLog());
                } catch (Exception ex) {
                    // There was no log for today
                    Log.i("EmployeeLogs", "no log found for today's date: " + TimeKeeper.getInstance().now().toString());
                }
            }

            CreateDataForGrid();
        }
    }

    private void CreateDataForGrid()
    {
        TimeZoneEnum currentUserHomeTerminalTimeZone = ((APIControllerBase)this.getEmployeeLogController()).getCurrentUser().getHomeTerminalTimeZone();
        EmployeeLog empLog = getEmployeeLogController().GetEmployeeLog(_selectedDate);
        Date currentHomeTerminalTimeNow = ((APIControllerBase)getEmployeeLogController()).getCurrentClockHomeTerminalTime();

        _gridLogData.CreateDataForGrid(currentUserHomeTerminalTimeZone, empLog, currentHomeTerminalTimeNow, this);
    }

    private void FormatGrid()
    {
        if(_gridLogData != null && _gridLogData.getLogDate() != null) {
            _gridImageFrag.FormatGrid(_gridLogData);
            SetGridHours();
        }
    }

    private void SetGridHours()
    {
        EmployeeLog empLog = getEmployeeLogController().getSelectedLogForReport();
        LogGridSummary summary = getEmployeeLogController().GetLogGridSummary(empLog);
        _gridImageFrag.setGridHours(
                summary.getOffDutyMinutesTotal(),
                summary.getSleeperMinutesTotal(),
                summary.getOnDutyMinutesTotal(),
                summary.getDrivingMinutesTotal(),
                summary.getOffDutyWellsiteMinutesTotal(),
                empLog.getExemptLogType()
        );
    }

    public static class RoadsideInspectionMandateData {
        public CompanyConfigSettings companyConfigSettings;
        public User user;
        public User coDriver;
        public EmployeeLog currentLog;
        public GpsLocation currentLocation;
        public float currentOdometer;
        public String shippingId;
        public String trailerNumber;
        public String truckTractorId;
        public DistanceAndHours accumulatedDistanceAndHours;
        public boolean isUnidentifiedDrivingRecordsIndicatorActive;
        public boolean malfunctionIndicatorStatus;
        public boolean dataDiagnosticIndicatorStatus;
        public LocalTime logStartTime;
        public EmployeeLogEldEvent lastEldEvent;
        public String truckVin;
    }
}
