package com.jjkeller.kmb;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.jjkeller.kmb.fragments.DOTClocksFrag;
import com.jjkeller.kmb.fragments.DateSelectorFrag;
import com.jjkeller.kmb.fragments.EditLogFrag;
import com.jjkeller.kmb.fragments.LeftNavFrag;
import com.jjkeller.kmb.fragments.NavButtonsFrag;
import com.jjkeller.kmb.fragments.RecapHoursFrag;
import com.jjkeller.kmb.fragments.ReviewEditRequestsFrag;
import com.jjkeller.kmb.fragments.RptAvailHoursFrag;
import com.jjkeller.kmb.fragments.RptDailyHoursFrag;
import com.jjkeller.kmb.fragments.RptGridImageFrag;
import com.jjkeller.kmb.fragments.TripInfoFrag;
import com.jjkeller.kmb.fragments.ViewLogRemarksEditFrag;
import com.jjkeller.kmb.fragments.ViewLogRemarksFrag;
import com.jjkeller.kmb.fragments.ViewTripInfoFrag;
import com.jjkeller.kmb.interfaces.IDateSelectorFrag;
import com.jjkeller.kmb.interfaces.IEditLog;
import com.jjkeller.kmb.interfaces.INavButtonsFrag;
import com.jjkeller.kmb.interfaces.IRecapHours;
import com.jjkeller.kmb.interfaces.IReviewEditRequests;
import com.jjkeller.kmb.interfaces.IRptDailyHours;
import com.jjkeller.kmb.interfaces.ITripInfo.TripInfoFragActions;
import com.jjkeller.kmb.interfaces.ITripInfo.TripInfoFragControllerMethods;
import com.jjkeller.kmb.interfaces.IViewLogRemarks.DeleteLogRemarksFragActions;
import com.jjkeller.kmb.interfaces.IViewLogRemarks.DeleteLogRemarksFragControllerMethods;
import com.jjkeller.kmb.interfaces.IViewLogRemarks.EditLogRemarksFragActions;
import com.jjkeller.kmb.interfaces.IViewLogRemarks.EditLogRemarksFragControllerMethods;
import com.jjkeller.kmb.interfaces.IViewLogRemarksEdit.CancelLogRemarksFragActions;
import com.jjkeller.kmb.interfaces.IViewLogRemarksEdit.CancelLogRemarksFragControllerMethods;
import com.jjkeller.kmb.interfaces.IViewLogRemarksEdit.SaveLogRemarksFragActions;
import com.jjkeller.kmb.interfaces.IViewLogRemarksEdit.SaveLogRemarksFragControllerMethods;
import com.jjkeller.kmb.interfaces.IViewLogRemarksEdit.SelectLogRemarksFragActions;
import com.jjkeller.kmb.interfaces.IViewLogRemarksEdit.SelectLogRemarksFragControllerMethods;
import com.jjkeller.kmb.interfaces.IViewTripInfo.ViewTripInfoFragActions;
import com.jjkeller.kmb.interfaces.IViewTripInfo.ViewTripInfoFragControllerMethods;
import com.jjkeller.kmb.share.ClockData;
import com.jjkeller.kmb.share.GridLogData;
import com.jjkeller.kmb.share.OffDutyBaseActivity;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.HosAuditController;
import com.jjkeller.kmbapi.controller.LogEntryController;
import com.jjkeller.kmbapi.controller.LogGridSummary;
import com.jjkeller.kmbapi.controller.MotionPictureController;
import com.jjkeller.kmbapi.controller.abstracts.APIControllerBase;
import com.jjkeller.kmbapi.controller.interfaces.IAPIController;
import com.jjkeller.kmbapi.controller.share.KmbApplicationException;
import com.jjkeller.kmbapi.controller.share.MandateObjectFactory;
import com.jjkeller.kmbapi.controller.utility.EmployeeLogUtilities;
import com.jjkeller.kmbapi.employeelogeldevents.Enums;
import com.jjkeller.kmbapi.enums.CanadaDeferralTypeEnum;
import com.jjkeller.kmbapi.enums.ExemptLogTypeEnum;
import com.jjkeller.kmbapi.enums.TimeZoneEnum;
import com.jjkeller.kmbapi.proxydata.DutySummary;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;
import com.jjkeller.kmbapi.proxydata.MotionPictureProduction;
import com.jjkeller.kmbui.R;

import java.util.Date;
import java.util.List;

public class ViewLog extends OffDutyBaseActivity implements ViewTripInfoFragActions,
        ViewTripInfoFragControllerMethods, TripInfoFragActions, TripInfoFragControllerMethods,
        EditLogRemarksFragActions, EditLogRemarksFragControllerMethods, DeleteLogRemarksFragActions,
        DeleteLogRemarksFragControllerMethods, SaveLogRemarksFragActions, SaveLogRemarksFragControllerMethods,
        CancelLogRemarksFragActions, CancelLogRemarksFragControllerMethods, SelectLogRemarksFragActions,
        SelectLogRemarksFragControllerMethods, LeftNavFrag.OnNavItemSelectedListener, LeftNavFrag.ActivityMenuItemsListener,
        IRecapHours.RecapHoursFragControllerMethods, IRptDailyHours.RptDailyHoursFragControllerMethods, IEditLog.EditLogFragControllerMethods, IDateSelectorFrag.DateSelectorControllerMethods, INavButtonsFrag.NavButtonsControllerMethods, IReviewEditRequests.ReviewEditRequestsFragActions {

    public static final String EXTRA_INITIALFRAGINDEX = "initialFragIndex";

    private TripInfoFrag _tripInfoFrag;
    private ViewTripInfoFrag  _viewTripInfoFrag;
	@SuppressWarnings("unused")
    private ClockData _weeklyOnDutyData;
    private DOTClocksFrag _clocksFrag;
    private RptAvailHoursFrag _availHoursFrag;
    private RptGridImageFrag _gridFrag;
    private ViewLogRemarksFrag _viewLogRemarks;
    private ViewLogRemarksEditFrag _viewLogRemarksEdit;
    private EditLogFrag _editLogFrag;
    private ReviewEditRequestsFrag _reviewEditRequestsFrag;
    private DateSelectorFrag _dateSelectorFrag;
    private IAPIController _controllerEmp = null;

    private int _currentFrag;

    protected static final int VIEW_GRID = 0;
    protected static final int VIEW_HOURS = 1;
    protected static final int VIEW_CLOCKS = 2;
    protected static final int VIEW_RECAPINFO = 3;
    protected static final int VIEW_TRIPINFO = 4;
    protected static final int VIEW_TRIPINFOEDIT = 103;
    protected static final int VIEW_LOGREMARKS = 5;
    protected static final int VIEW_LOGREMARKSEDIT = 104;
    protected static final int VIEW_EDITLOG = 105;
    protected static final int VIEW_REVIEWEDITREQUESTS = 106;
    private GridLogData _gridLogData = new GridLogData();
    private int _lastItemIndex;
    private int _currentItemIndex = 0;
    private Date _selectedDate=null;
    boolean _loading;
    private TextView _exemptlbl;
    private String _navButtonTitle;
    private int _editLogEventPosition;
    private String _editLogEventTitle = "";

    public String getNavButtonsTitle(){ return _navButtonTitle; }
    public void setNavButtonsTitle(String value) { _navButtonTitle = value; }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
              setContentView(R.layout.viewlog);
        _loading = true;
        if (savedInstanceState != null) {
            _currentFrag = savedInstanceState.getInt("currentFrag");
            _currentItemIndex = savedInstanceState.getInt("currentItemIndex");
            _editLogEventPosition = savedInstanceState.getInt(DateSelectorFrag.BUNDLE_SELECTOR_POSITION, 0);
            _editLogEventTitle = savedInstanceState.getString(DateSelectorFrag.BUNDLE_SELECTOR_TITLE, "");
        }

        // allow the Intent which tab to initially activate
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            _currentItemIndex = extras.getInt(EXTRA_INITIALFRAGINDEX, 0);
        }

        // Used for handling highlighting the selected item in the leftnav
        // We have to allow the leftnav to highlight the selected item
        this.setLeftNavSelectedItem(_currentItemIndex);
        this.setLeftNavAllowChange(true);

        if (_currentFrag <= 0) {
            onNavItemSelected(_currentItemIndex);
        }

        loadControls(savedInstanceState);
    }

    @Override
    protected void loadControls(Bundle savedInstanceState) {
        super.loadControls(savedInstanceState);
        _exemptlbl = (TextView) this.findViewById(R.id.txtExemptLog);
    }

    @Override
    protected void InitController() {
        _controllerEmp = MandateObjectFactory.getInstance(this,GlobalState.getInstance().getFeatureService()).getCurrentEventController();
        switch (_currentFrag) {
            case VIEW_GRID:
                this.setController(MandateObjectFactory.getInstance(this,GlobalState.getInstance().getFeatureService()).getCurrentEventController());
                loadDateNavFragment(this.getResources().getString(R.string.viewLog_Grid));
                break;
            case VIEW_TRIPINFO:
                this.setController(MandateObjectFactory.getInstance(this,GlobalState.getInstance().getFeatureService()).getCurrentEventController());
                break;
            case VIEW_TRIPINFOEDIT:
                this.setController(MandateObjectFactory.getInstance(this,GlobalState.getInstance().getFeatureService()).getCurrentEventController());
                break;
            case VIEW_CLOCKS:
                this.setController(new HosAuditController(this));
                loadFragment(R.id.recap_hours_fragment, new RecapHoursFrag());
                break;
            case VIEW_HOURS:
                this.setController(new HosAuditController(this));
                break;
            case VIEW_LOGREMARKS:
                this.setController(new LogEntryController(this));
                break;
            case VIEW_LOGREMARKSEDIT:
                this.setController(new LogEntryController(this));
                break;
            case VIEW_RECAPINFO:
                this.setController(getEmployeeLogController());
                loadFragment(R.id.recap_hours_fragment, new RecapHoursFrag());
                break;
            case VIEW_EDITLOG:
                this.setController(getEmployeeLogController());
                loadDateNavFragment(this.getResources().getString(R.string.editLog));
                break;
            case VIEW_REVIEWEDITREQUESTS:
                this.setController(getEmployeeLogController());
                break;
        }
    }

    void getDateSelectorFrag(){
        // Finds the fragment that has the date selector
        if (_dateSelectorFrag ==null)
            _dateSelectorFrag = (DateSelectorFrag) getSupportFragmentManager().findFragmentById(R.id.datenav_fragment);
    }

    @Override
    public void setFragments() {
        super.setFragments();
        Fragment recap = getSupportFragmentManager().findFragmentById(R.id.recap_hours_fragment);
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
        Date currentDate = this.getController().getCurrentClockHomeTerminalTime();

        if (_selectedDate==null)
            _selectedDate=currentDate;
        if (_currentFrag!= VIEW_CLOCKS && _currentFrag != VIEW_RECAPINFO)
            removeFragment(R.id.recap_hours_fragment, recap);
        switch (_currentFrag){
            case VIEW_GRID:
                _gridFrag = (RptGridImageFrag) f;
                //Hide grid legend if Eld Mandate Feature Toggle Off
                if(!GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled())
                {
                    FrameLayout legend = (FrameLayout)findViewById(R.id.layoutgridlegend);
                    if(legend != null){
                        legend.setVisibility(View.GONE);
                    }
                }
                getDateSelectorFrag();
                this.setController(MandateObjectFactory.getInstance(this,GlobalState.getInstance().getFeatureService()).getCurrentEventController());
                loadData();
                break;
            case VIEW_TRIPINFO:
                _viewTripInfoFrag = (ViewTripInfoFrag) f;
                this.setController(MandateObjectFactory.getInstance(this,GlobalState.getInstance().getFeatureService()).getCurrentEventController());
                loadData();
                break;
            case VIEW_TRIPINFOEDIT:
                _tripInfoFrag = (TripInfoFrag) f;
                this.setController(MandateObjectFactory.getInstance(this,GlobalState.getInstance().getFeatureService()).getCurrentEventController());
                break;
            case VIEW_CLOCKS:
                _clocksFrag = (DOTClocksFrag) f;
                this.setController(new HosAuditController(this));
                loadData();
                break;
            case VIEW_HOURS:
                _availHoursFrag = (RptAvailHoursFrag) f;
                this.setController(new HosAuditController(this));
                loadData();
                break;
            case VIEW_LOGREMARKS:
                _viewLogRemarks = (ViewLogRemarksFrag) f;
                this.setController(new LogEntryController(this));
                break;
            case VIEW_LOGREMARKSEDIT:
                _loading = true;
                _viewLogRemarksEdit = (ViewLogRemarksEditFrag) f;
                this.setController(new LogEntryController(this));
                break;
            case VIEW_RECAPINFO:
                this.setController(MandateObjectFactory.getInstance(this,GlobalState.getInstance().getFeatureService()).getCurrentEventController());
                break;

            case VIEW_EDITLOG:
                _editLogFrag = (EditLogFrag) f;
                getDateSelectorFrag();
                this.setController(getEmployeeLogController());
                loadData();
                break;
            case VIEW_REVIEWEDITREQUESTS:
                _reviewEditRequestsFrag = (ReviewEditRequestsFrag) f;
                this.setController(getEmployeeLogController());
                loadData();
                break;
        }
        if (_currentFrag != VIEW_GRID && _currentFrag != VIEW_EDITLOG) {
            // The date selector fragment should only be shown for these two options. Remove otherwise
            Fragment datenav = getSupportFragmentManager().findFragmentById(R.id.datenav_fragment);
            removeFragment(R.id.datenav_fragment, datenav);
            FrameLayout layout = (FrameLayout)findViewById(R.id.datenav_fragment);
            if (layout!=null)
               layout.setVisibility(View.GONE);
        }

    }

    @Override
    protected void loadData() {

        super.loadData();
        switch (_currentFrag){
            case VIEW_GRID:
                _gridLogData.setLogDate(_selectedDate);
                // get list of logs for report (returns one)
                List<EmployeeLog> logs= getMyController().EmployeeLogsForDutyStatusReport(_selectedDate);

                // if the clock went past midnight need to create new day log, re-do the loading after
				// in this case the above method returns no log(s) for this day

                if (logs.size()==0) {
                    LogEntryController lec = new LogEntryController(this.getBaseContext());
                    lec.CreateNewLogIfNecessary(((APIControllerBase)this.getMyController()).getCurrentUser() ,GlobalState.getInstance().getCurrentEmployeeLog(),  _selectedDate);
                    logs= getMyController().EmployeeLogsForDutyStatusReport(_selectedDate);
                }
                // Double check. Since there's some async calls make sure the new log was created, prevent crash
                if (logs.size()>0) {
                    getMyController().setSelectedLogForReport(logs.get(0));
                CreateDataForGrid();
                FormatGrid();
                SetGridHours();
                }
                break;

            case VIEW_CLOCKS:
                HosAuditController controller = getMyHosAuditController();
                controller.UpdateForCurrentLogEvent();
                ClockData _driveTimeData = new ClockData(controller.DriveTimeSummary());
                ClockData _dailyOnDutyData = new ClockData(controller.DailyDutySummary());
                ClockData _weeklyOnDutyData = new ClockData(controller.WeeklyDutySummary());

                ClockData _driveTimeData_ResetBreak = null;
                DutySummary _driveTimeSummary_ResetBreak = controller.DriveTimeRestBreakSummary();
                if (controller.DriveTimeRestBreakSummary() != null) {
                    _driveTimeData_ResetBreak = new ClockData(controller.DriveTimeRestBreakSummary());
                }
                _clocksFrag.updateClocks(_driveTimeData, _driveTimeData_ResetBreak, _dailyOnDutyData, _weeklyOnDutyData, _driveTimeSummary_ResetBreak);
                break;
            case VIEW_HOURS:
                _availHoursFrag.init(getMyHosAuditController());
                break;
            case VIEW_TRIPINFO:
                setTripInfo();
                break;
            case VIEW_EDITLOG:
                populateEditLog();
                break;
            case VIEW_REVIEWEDITREQUESTS:
                IAPIController empLogCtrlr = MandateObjectFactory.getInstance(this, GlobalState.getInstance().getFeatureService()).getCurrentEventController();
                List<EmployeeLog> logsThatNeedToBeReviewed = empLogCtrlr.GetLogsWithUnreviewedEdits(((APIControllerBase)this.getMyController()).getCurrentUser());
                _reviewEditRequestsFrag.setDataSource(logsThatNeedToBeReviewed);
                break;
        }
    }

    public void populateEditLog() {
        if (_selectedDate!=null) {
            EmployeeLog empLog = _controllerEmp.GetEmployeeLog(_selectedDate);
            boolean isExemptFromELDUse = empLog != null ? empLog.getIsExemptFromELDUse() : false;

            List<Pair<EmployeeLogEldEvent, Enums.SpecialDrivingCategory>> eventWithSpecialDrivingCategoryList = null;

            if (empLog != null) {
                eventWithSpecialDrivingCategoryList = EmployeeLogUtilities.loadEventsIncludingSpecialDrivingCategories(this, empLog);

                if (eventWithSpecialDrivingCategoryList != null) {
                    _editLogFrag.setDataSource(eventWithSpecialDrivingCategoryList, _editLogEventPosition);

                    if(!isExemptFromELDUse) {
                        _editLogFrag.enableAddEventButton((int) empLog.getPrimaryKey(), _selectedDate);
                    }else {
                        _editLogFrag.disableAddEventButton(getString(R.string.cannot_edit_exempt_log_message));
                    }
                }
            }
        }
    }

    private void setTripInfo() {
        if (_selectedDate!=null) {
            EmployeeLog empLog = _controllerEmp.GetEmployeeLog(_selectedDate);
            boolean isExemptFromELDUse = empLog != null ? empLog.getIsExemptFromELDUse() : false;

            if (empLog != null) {
                if (isExemptFromELDUse) {
                    _viewTripInfoFrag.disableEditButton(getString(R.string.cannot_edit_exempt_log_message));
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("currentFrag", _currentFrag);
        outState.putInt("currentItemIndex", _currentItemIndex);
        outState.putInt(DateSelectorFrag.BUNDLE_SELECTOR_POSITION, _editLogEventPosition);
        outState.putString(DateSelectorFrag.BUNDLE_SELECTOR_TITLE, _editLogEventTitle);

        // 10/3/11 JHM - Don't store state (likely blank values) if the FetchLocalData task hasn't completed.
        if (_currentFrag == VIEW_TRIPINFOEDIT) {
            TextView tvTrailer = (TextView) findViewById(R.id.txtTrailer);
            outState.putCharSequence(getResources().getString(R.string.state_trailer), tvTrailer.getText());

            TextView tvTrailerPlate = (TextView) findViewById(R.id.txtTrailerPlate);
            outState.putCharSequence(getResources().getString(R.string.state_trailerplate), tvTrailerPlate.getText());

            TextView tvShipment = (TextView) findViewById(R.id.txtShipmentInfo);
            outState.putCharSequence(getResources().getString(R.string.state_shipment), tvShipment.getText());

            TextView tvVehiclePlate = (TextView) findViewById(R.id.txtVehiclePlate);
            outState.putCharSequence(getResources().getString(R.string.state_vehicleplate), tvVehiclePlate.getText());

            CheckBox chkReturnToWorkLocation = (CheckBox) findViewById(R.id.chkReturnToWorkLocation);
            outState.putBoolean(getResources().getString(R.string.state_returntoworklocation), chkReturnToWorkLocation.isChecked());

            CheckBox chkIsOperatesSpecificVehiclesForOilField = (CheckBox) findViewById(R.id.chkIsOperatesSpecificVehicleForOilField);
            outState.putBoolean(getResources().getString(R.string.state_isoperatesspecificvehiclesforoilfield), chkIsOperatesSpecificVehiclesForOilField.isChecked());
        } else if (_currentFrag == VIEW_GRID) {
            _gridLogData.onSaveInstanceState(outState);
        }

        super.onSaveInstanceState(outState);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.CreateOptionsMenu(menu, false);
            return true;
    }

    @Override
    public String getActivityMenuItemList() {
        if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
            return getString(R.string.viewLog_actionitemsmandate);
        }
        else
            return getString(R.string.viewLog_actionitems);
    }

    void loadDateNavFragment(String title){
        // Keeping the title on Global state
        FrameLayout layout = (FrameLayout) findViewById(R.id.datenav_fragment);
        if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
            _editLogEventTitle = title;
            this.setNavButtonsTitle(title);
            _dateSelectorFrag = new DateSelectorFrag();
            Bundle bundle = new Bundle();
            bundle.putInt(DateSelectorFrag.BUNDLE_SELECTOR_POSITION, _editLogEventPosition);
            bundle.putString(DateSelectorFrag.BUNDLE_SELECTOR_TITLE, _editLogEventTitle);
            _dateSelectorFrag.setArguments(bundle);
            NavButtonsFrag _navButtonsFrag = new NavButtonsFrag();
            loadFragment(R.id.datenav_fragment, _dateSelectorFrag);


            if (layout != null) {
                layout.setVisibility(View.VISIBLE);
            }
        }
        else{
            if (layout != null)
                layout.setVisibility(View.GONE);
        }

    }

    @Override
    public void onNavItemSelected(int itemPosition) {

        boolean IsEldMandateEnabled = GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled();
        if (itemPosition==6 && !IsEldMandateEnabled)
            itemPosition=8;
        //if user selects "View Log" <-- (itemPosition==7) from menu tab, inside the context of the View Grid screen,
        //mandate toggle OFF, send them to the VIEW_GRID screen NOT VIEW_REVIEWEDITREQUESTS
        if(itemPosition==7 && !IsEldMandateEnabled)
            itemPosition=0;
        switch (itemPosition) {
            case 0:
                _currentFrag = VIEW_GRID;
                loadContentFragment(new RptGridImageFrag());
                loadDateNavFragment(this.getResources().getString(R.string.viewLog_Grid));
                _currentItemIndex = itemPosition;
                break;
            case 1:
                _currentFrag = VIEW_HOURS;
                loadContentFragment(new RptAvailHoursFrag());
                _currentItemIndex = itemPosition;
                break;
            case 2:
                _currentFrag = VIEW_CLOCKS;
                loadContentFragment(new DOTClocksFrag());
                loadFragment(R.id.recap_hours_fragment, new RecapHoursFrag());
                _currentItemIndex = itemPosition;
                break;
            case 3:
                _currentFrag = VIEW_RECAPINFO;
                loadContentFragment(new RptDailyHoursFrag());
                loadFragment(R.id.recap_hours_fragment, new RecapHoursFrag());
                _currentItemIndex = itemPosition;
                break;
            case 4:
                _currentFrag = VIEW_TRIPINFO;
                loadContentFragment(new ViewTripInfoFrag());
                _currentItemIndex = itemPosition;
                break;
            case 5:
                _currentFrag = VIEW_LOGREMARKS;
                loadContentFragment(new ViewLogRemarksFrag());
                _currentItemIndex = itemPosition;
                break;
            case 6:
                _currentFrag = VIEW_EDITLOG;
                loadContentFragment((new EditLogFrag()));
                loadDateNavFragment(this.getResources().getString(R.string.editLog));
                _currentItemIndex = itemPosition;
                break;
            case 7:
                _currentFrag = VIEW_REVIEWEDITREQUESTS;
                loadContentFragment(new ReviewEditRequestsFrag());
                _currentItemIndex = itemPosition;
                break;
            case 8:
                // finish activity and go to RODS
                this.finish();
                setOffDutyMsgCloseBtnPressed(false);
                this.startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
                break;
        }

        // When using the drop down menu, we have to manually set the index and simulate the leftnav item click
        if (itemPosition <= 6 && _lastItemIndex == 999999999 && itemPosition != 999999999) {
            this.setLeftNavSelectedItem(itemPosition);
            loadLeftNavFragment();
        }

        _lastItemIndex = itemPosition;
        getSupportFragmentManager().executePendingTransactions();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // if the item is the home button
        if (item.getItemId() == android.R.id.home) {
            // finish activity and go to RODS
            this.finish();
            this.startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }

        onNavItemSelected(item.getItemId());
        super.onOptionsItemSelected(item);
        return true;
    }

    public void handleCancelButtonClick() {
        leaveCurrent();
    }

    public void handleOKButtonClick() {
        boolean validData = true;

        // data is not valid if oilfield specific setting is not checked and the off duty wellsite status is used on the log
        if (_currentFrag == VIEW_TRIPINFOEDIT && _tripInfoFrag != null
                && _tripInfoFrag.getIsOperatesSpecificVehiclesForOilField().getVisibility() == View.VISIBLE
                && !_tripInfoFrag.getIsOperatesSpecificVehiclesForOilField().isChecked()
                && getMyController().IsUSOilFieldOffDutyStatusInLog()) {
            this.ShowMessage(this, this.getResources().getString(R.string.msg_oilfieldsetting_cannotbeunselected));
            validData = false;
        }

        if (validData) {
            mSaveLocalDataTask = new SaveLocalDataTask(this.getClass().getSimpleName());
            mSaveLocalDataTask.execute();
        }

    }

    public boolean ValidateTripInfoEditRequiredFields()
    {
        int invalidFieldsCount = 0;
        boolean isValid = false;
        String message = "";

        if(_currentFrag == VIEW_TRIPINFOEDIT && _tripInfoFrag != null
                && _tripInfoFrag.getTrailerTextView().getVisibility() == View.VISIBLE && _tripInfoFrag.getTrailerTextView().getText().length()==0)
        {
            if(message != null && message != "") {
                message = message + ", " + (this.getResources().getString(R.string.msg_trailernumbersetting_isrequired));
            }
            else {
                message = (this.getResources().getString(R.string.msg_trailernumbersetting_isrequired));
            }
            invalidFieldsCount = invalidFieldsCount + 1;
        }

        if(_currentFrag == VIEW_TRIPINFOEDIT && _tripInfoFrag != null
                && _tripInfoFrag.getShipmentInfoTextView().getVisibility() == View.VISIBLE && _tripInfoFrag.getShipmentInfoTextView().getText().length()==0)
        {
            if(message != null && message != "") {
                message = message + ", " + (this.getResources().getString(R.string.msg_shipmentinfosetting_isrequired));
            }
            else {
                message = (this.getResources().getString(R.string.msg_shipmentinfosetting_isrequired));
            }
            invalidFieldsCount = invalidFieldsCount + 1;
        }

        if(_currentFrag == VIEW_TRIPINFOEDIT && _tripInfoFrag != null
                && _tripInfoFrag.getTractorNumberTextView().getVisibility() == View.VISIBLE && _tripInfoFrag.getTractorNumberTextView().getText().length()==0)
        {
            if(message != null && message != "") {
                message = message + ", " + (this.getResources().getString(R.string.msg_unitnumbersetting_isrequired));
            }
            else {
                message = (this.getResources().getString(R.string.msg_unitnumbersetting_isrequired));
            }
            invalidFieldsCount = invalidFieldsCount + 1;
        }

        if(message != null && message != ""){
            this.ShowMessage(this, message);
        }

        if(invalidFieldsCount > 0) {
            isValid = false;
        }
        else {
            isValid = true;
        }
        return isValid;
    }

    public void handleEditButtonClick(Context ctx) {
        loadContentFragment(new TripInfoFrag());
        _currentFrag = VIEW_TRIPINFOEDIT;
    }

    public void handleSetTrailerInformationClick() {

        SharedPreferences _userPref = this.getSharedPreferences(this.getString(R.string.sharedpreferencefile), 0);
        SharedPreferences.Editor editor = _userPref.edit();
        if (!_tripInfoFrag.getDefaultTrailerInfo().isChecked()) {
            // when clearing the checkbox, the remove the default settings
            editor.putString(getString(R.string.defaulttrailernumber), "");
            editor.putString(getString(R.string.defaulttrailerplate), "");
            editor.putBoolean(getString(R.string.setdefaulttrailernumber), false);

            //Enabled the ability to add Trailer Number
            _tripInfoFrag.getTrailerTextView().setEnabled(true);
            _tripInfoFrag.getTrailerPlateTextView().setEnabled(true);
        } else {
            TextView tvTrailer = _tripInfoFrag.getTrailerTextView();
            TextView tvTrailerPlate = _tripInfoFrag.getTrailerPlateTextView();
            editor.putString(getString(R.string.defaulttrailernumber), tvTrailer.getText().toString());
            editor.putString(getString(R.string.defaulttrailerplate), tvTrailerPlate.getText().toString());
            editor.putBoolean(getString(R.string.setdefaulttrailernumber), true);

            //Disable the ability to add Trailer Number
            _tripInfoFrag.getTrailerTextView().setEnabled(false);
            _tripInfoFrag.getTrailerPlateTextView().setEnabled(false);
        }

        editor.commit();
    }

    @Override
    public void handleMotionPictureProductionSelect() {

    }

    public void handleSetShipmentInformationClick() {

        SharedPreferences _userPref = this.getSharedPreferences(this.getString(R.string.sharedpreferencefile), 0);
        SharedPreferences.Editor editor = _userPref.edit();
        if (!_tripInfoFrag.getDefaultShipmentNumber().isChecked()) {
            // when clearing the checkbox, the remove the default settings
            editor.putString(getString(R.string.defaultshipmentnumber), "");
            editor.putBoolean(getString(R.string.setdefaultshipmentnumber), false);

            //Enabled the ability to add Shipment Number
            _tripInfoFrag.getShipmentInfoTextView().setEnabled(true);
        } else {
            TextView tvShipment = _tripInfoFrag.getShipmentInfoTextView();
            editor.putString(getString(R.string.defaultshipmentnumber), tvShipment.getText().toString());
            editor.putBoolean(getString(R.string.setdefaultshipmentnumber), true);

            //Disable the ability to add Shipment Number
            _tripInfoFrag.getShipmentInfoTextView().setEnabled(false);
        }

        editor.commit();
    }

    @Override
    protected void Return(boolean success) {
        super.Return(success);

        if (success && _currentFrag == VIEW_TRIPINFOEDIT) {
            loadContentFragment(new ViewTripInfoFrag());
            _currentFrag = VIEW_TRIPINFO;
        }

        if (!success) {
            // currently the only reason to get an unsuccessful return code is when the weekly reset cannot be used on this log
            this.ShowMessage(this, this.getString(R.string.weeklyResetCannotBeUsed_message));
        }
    }

    @Override
    protected boolean saveData() {
        boolean isSuccessful = false;
        CanadaDeferralTypeEnum canadaDeferralType = new CanadaDeferralTypeEnum(CanadaDeferralTypeEnum.NONE);

        // Check if a selection was made from the off duty deferral dropdown
        if (_tripInfoFrag.getDeferralSpinner().getSelectedItemPosition() >= 0) {
            canadaDeferralType = CanadaDeferralTypeEnum.valueOf(this, _tripInfoFrag.getDeferralAdapter().getItem(_tripInfoFrag.getDeferralSpinner().getSelectedItemPosition()).toString());
        }

        String trailerNumbers =  _tripInfoFrag.getTrailerTextView().getText().toString(),
                trailerPlate = _tripInfoFrag.getTrailerPlateTextView().getText().toString(),
                shipmentInfo = _tripInfoFrag.getShipmentInfoTextView().getText().toString(),
                vehiclePlate = _tripInfoFrag.getVehiclePlateTextView().getText().toString(),
                tractorNumbers = _tripInfoFrag.getTractorNumberTextView().getText().toString();

        String authorityId= null, productionId = null;
        if (GlobalState.getInstance().getCompanyConfigSettings(this).getIsMotionPictureEnabled() && _tripInfoFrag.getMotionPictureProductionSpinner().getSelectedItem() != null) {
            MotionPictureProduction production = (MotionPictureProduction) _tripInfoFrag.getMotionPictureProductionSpinner().getSelectedItem();
            productionId = production.getMotionPictureProductionId();
            authorityId = production.getMotionPictureAuthorityId();
            GlobalState.getInstance().set_currentMotionPictureAuthorityId(authorityId);
            GlobalState.getInstance().set_currentMotionPictureProductionId(productionId);

        }
        this.getMyController().SaveTripInfo(trailerNumbers, trailerPlate, shipmentInfo, vehiclePlate, _tripInfoFrag.getReturnToWorkLocationCheckbox().isChecked(), canadaDeferralType, _tripInfoFrag.getIsHaulingExplosivesCheckbox().isChecked(), _tripInfoFrag.getIsOperatesSpecificVehiclesForOilField().isChecked(), _tripInfoFrag.getIs30MinRestBreakExemptCheckbox().isChecked(), tractorNumbers, authorityId, productionId);

        //2016.05.10 Add these values to the GlobalState for use in ELDEvent generation
        GlobalState.getInstance().set_currentTrailerNumbers(trailerNumbers);
        GlobalState.getInstance().set_currentTrailerPlate(trailerPlate);
        GlobalState.getInstance().set_currentVehiclePlate(vehiclePlate);
        GlobalState.getInstance().set_currentShipmentInfo(shipmentInfo);
        GlobalState.getInstance().set_currentTractorNumbers(tractorNumbers);

        if (_tripInfoFrag.getIsWeeklyResetUsedCheckbox().getVisibility() == View.VISIBLE) {
            // the weekly reset checkbox is up, so process it
            boolean isResetUsed = _tripInfoFrag.getIsWeeklyResetUsedCheckbox().isChecked();
            if (isResetUsed) {
                if (this.getMyController().IsValidToUseWeeklyResetOnCurrentLog()) {
                    this.getMyController().SaveWeeklyResetUsed(true);
                    isSuccessful = true;
                } else {
                    isSuccessful = false;
                }
            } else {
                this.getMyController().SaveWeeklyResetUsed(false);
                isSuccessful = true;
            }
        } else
            isSuccessful = true;

        return isSuccessful;
    }



    public void loadMotionPicture(){
        if (GlobalState.getInstance().getCompanyConfigSettings(getBaseContext()).getIsMotionPictureEnabled()){
            MotionPictureController controller = new MotionPictureController(getBaseContext());
            List<MotionPictureProduction> _motionPictureProductions = controller.GetActiveMotionPictureProductions();

            if (_motionPictureProductions.size() > 1){
                ArrayAdapter<MotionPictureProduction> spinnerAdapter = new ArrayAdapter<>(this, R.layout.kmb_spinner_item, _motionPictureProductions);
                String productionId = GlobalState.getInstance().getCurrentDesignatedDriver().getUserState().getMotionPictureProductionId();
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                _tripInfoFrag.getMotionPictureProductionSpinner().setAdapter(spinnerAdapter);


                if (productionId != null && productionId.length() > 0) {
                    for (int i=0; i < spinnerAdapter.getCount(); i++){
                        MotionPictureProduction production = spinnerAdapter.getItem(i);
                        if (productionId.equals(production.getMotionPictureProductionId())){
                            _tripInfoFrag.getMotionPictureProductionSpinner().setSelection(i);
                            _tripInfoFrag.getMotionPictureAuthorityTextView().setText(production.getMotionPictureAuthority().GetNameAndDOTNumber());
                            break;
                        }
                    }
                }


            }
        }
    }


    private void CreateDataForGrid() {
        TimeZoneEnum currentUserHomeTerminalTimeZone = ((APIControllerBase)this.getMyController()).getCurrentUser().getHomeTerminalTimeZone();
        EmployeeLog empLog = getMyController().GetEmployeeLog(_selectedDate);
        setExemptLabel(empLog.getExemptLogType().getValue() != ExemptLogTypeEnum.NULL);
        Date currentHomeTerminalTimeNow = ((APIControllerBase)getMyController()).getCurrentClockHomeTerminalTime();

        _gridLogData.CreateDataForGrid(currentUserHomeTerminalTimeZone, empLog, currentHomeTerminalTimeNow, this);
    }

    private void FormatGrid() {
        _gridFrag.FormatGrid(_gridLogData);
    }

    private void SetGridHours() {
        EmployeeLog empLog = getMyController().GetEmployeeLog(_selectedDate);
        LogGridSummary summary = getMyController().GetLogGridSummary(empLog);

     _gridFrag.setGridHours(
                summary.getOffDutyMinutesTotal(),
                summary.getSleeperMinutesTotal(),
                summary.getOnDutyMinutesTotal(),
                summary.getDrivingMinutesTotal(),
                summary.getOffDutyWellsiteMinutesTotal(),
                empLog.getExemptLogType()
        );
    }

    public IAPIController getMyController() {
        return getEmployeeLogController();
    }

    public HosAuditController getMyHosAuditController() {
        return (HosAuditController) this.getController();
    }

    public LogEntryController getMyLogEntryController() {
        return (LogEntryController) this.getController();
    }

    public void updateExemptLogStatus() {
        boolean isExemptLog = GlobalState.getInstance().getCurrentEmployeeLog().getExemptLogType().getValue() != ExemptLogTypeEnum.NULL;
        this.setExemptLabel(isExemptLog);
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

    public void handleEditLogRemarksClick(Context ctx) {
        loadContentFragment(new ViewLogRemarksEditFrag());
        _currentFrag = VIEW_LOGREMARKSEDIT;
    }

    public void handleDeleteLogRemarksClick(Context ctx) {
        // Set the current log remark to be empty "delete"
        this.getMyLogEntryController().UpdateCurrentEventRemarks("");

        // Reload grid view
        this._viewLogRemarks.getViewLogRemarksGridView().invalidateViews();
    }

    public void handleSaveLogRemarksClick(Context ctx) {
        String remark = _viewLogRemarksEdit.getRemarkTextView().getText().toString();

        // Save remark
        this.getMyLogEntryController().UpdateCurrentEventRemarks(remark);

        leaveCurrent();
    }

    public void handleCancelLogRemarksClick(Context ctx) {
        leaveCurrent();
    }

    public void handleDateChange(Date selectedDate, int position){
        _selectedDate=selectedDate;
        _editLogEventPosition = position;
        loadData();
    }

    public void handleEditLog(){
        leftNavHighlightSelectedItem(6);
        onNavItemSelected(6);
    }

    public void handleRemarkSelect() {
        if (!_loading) {
            String remark = _viewLogRemarksEdit.GetSelectedRemark();
            _viewLogRemarksEdit.setRemarkTextViewText(remark);
        } else _loading = false;
    }

    private void leaveCurrent() {
        switch (_currentFrag) {
            case VIEW_TRIPINFOEDIT:
                _currentFrag = VIEW_TRIPINFO;
                loadContentFragment(new TripInfoFrag());
                break;
            case VIEW_LOGREMARKSEDIT:
                _currentFrag = VIEW_LOGREMARKS;
                loadContentFragment(new ViewLogRemarksFrag());
                break;
        }

        this.onNavItemSelected(_currentFrag);
    }



    private void setExemptLabel(boolean isVisible) {

        if (_exemptlbl != null) {
            if (!isVisible)
                _exemptlbl.setVisibility(View.GONE);
            else
                _exemptlbl.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public IAPIController getEmployeeLogController() {
        return _controllerEmp;
    }

    @Override
    public int getCurrentItemIndex() {
        return 0;
    }

    @Override
    public int getTotalItemCount() {
        return 0;
    }

    @Override
    public void handleBtnPrevious() {

    }

    @Override
    public void handleBtnNext() {

    }

	
    public void handleDownloadButtonClick()
    {
        _reviewEditRequestsFrag.getDownloadButton().setEnabled(false);
        // start async task to download logs
        DownloadEditLogRequestsTask _downloadEditLogRequestsTask = new DownloadEditLogRequestsTask();
        _downloadEditLogRequestsTask.execute();


    }

	// Async class to download logs with edit requests 
    private class DownloadEditLogRequestsTask extends AsyncTask<Void, String, Boolean> {
        ProgressDialog _pd;
        KmbApplicationException _ex = null;


        @Override
        protected void onPreExecute()
        {
            LockScreenRotation();
            _pd = ProgressDialog.show(ViewLog.this, "", getString(R.string.msgcontacting));
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean isDownloaded = false;

            try {
                isDownloaded = _controllerEmp.DownloadLogsWithEditRequests(GlobalState.getInstance().getCurrentUser());
            }
            catch (Exception e) {
                return isDownloaded;
            }

            return isDownloaded;
        }

        @Override
        protected void onProgressUpdate(String... values) {
         //   nothing, because we have a dialog with a spinner
        }

        @Override
        protected void onPostExecute(Boolean result)
        {
            if(_pd != null && _pd.isShowing()) {
                _pd.dismiss();
                if(result){
                    _reviewEditRequestsFrag.showHUDMessageSuccess(getString(R.string.logeditreviewdownload_confirm));
                    _reviewEditRequestsFrag.getDownloadButton().setEnabled(true);
                } else if (_controllerEmp.getDownloadLogSkipped()){
                    _reviewEditRequestsFrag.showHUDMessageError(getString(R.string.logeditreviewdownload_submitlogs));
                    _reviewEditRequestsFrag.getDownloadButton().setEnabled(false);
                } else {
                    _reviewEditRequestsFrag.showHUDMessageError(getString(R.string.logeditreviewdownload_error));
                    _reviewEditRequestsFrag.getDownloadButton().setEnabled(true);
                }

            }
            if (_ex != null)
                ViewLog.this.HandleException(_ex);

            else if(result)
            {
                _reviewEditRequestsFrag.onActivityCreated(null);
            }

            // Invalidate the menu to update the items
            supportInvalidateOptionsMenu();
            UnlockScreenRotation();
        }
    }



}

