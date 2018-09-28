package com.jjkeller.kmb;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.jjkeller.kmb.fragments.DOTClocksFrag;
import com.jjkeller.kmb.fragments.LeftNavFrag;
import com.jjkeller.kmb.fragments.RecapHoursFrag;
import com.jjkeller.kmb.fragments.RptAvailHoursFrag;
import com.jjkeller.kmb.interfaces.IRecapHours;
import com.jjkeller.kmb.share.ClockData;
import com.jjkeller.kmb.share.OffDutyBaseActivity;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.HosAuditController;
import com.jjkeller.kmbapi.controller.interfaces.IAPIController;
import com.jjkeller.kmbapi.controller.share.MandateObjectFactory;
import com.jjkeller.kmbapi.proxydata.DutySummary;
import com.jjkeller.kmbui.R;

public class RptAvailHours extends OffDutyBaseActivity implements
        LeftNavFrag.OnNavItemSelectedListener, LeftNavFrag.ActivityMenuItemsListener,
        IRecapHours.RecapHoursFragControllerMethods {
    private RptAvailHoursFrag _availHoursFrag;

    private DutySummary _driveTimeSummary_ResetBreak;

    private int _currentFrag;
    private IAPIController _controllerEmp = null;
    private DOTClocksFrag _clocksFrag;

    protected static final int VIEW_CLOCKS = 0;
    protected static final int VIEW_HOURS = 1;

    private int _lastItemIndex;
    private int _currentItemIndex;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _controllerEmp = MandateObjectFactory.getInstance(this,GlobalState.getInstance().getFeatureService()).getCurrentEventController();

        setContentView(R.layout.rptavailhours);

        if (savedInstanceState != null) {
            _currentFrag = savedInstanceState.getInt("currentFrag");
            _currentItemIndex = savedInstanceState.getInt("currentItemIndex");
        }

        // Used for handling highlighting the selected item in the leftnav
        // We have to allow the leftnav to highlight the selected item
        this.setLeftNavSelectedItem(_currentItemIndex);
        this.setLeftNavAllowChange(true);

        if (_currentFrag <= 0) {
            _currentFrag = VIEW_CLOCKS;
            loadContentFragment(new DOTClocksFrag());
            loadFragment(R.id.recap_hours_fragment, new RecapHoursFrag());
        }

        loadControls(savedInstanceState);
    }

    @Override
    protected void loadControls(Bundle savedInstanceState) {
        super.loadControls(savedInstanceState);
    }

    @Override
    public void setFragments() {
        super.setFragments();
        Fragment recap = getSupportFragmentManager().findFragmentById(R.id.recap_hours_fragment);
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_fragment);

        if (_currentFrag == VIEW_CLOCKS)
        {
            _clocksFrag = (DOTClocksFrag) f;
            this.setController(new HosAuditController(this));
        }
        else if (_currentFrag == VIEW_HOURS)
        {
            _availHoursFrag = (RptAvailHoursFrag) f;
            this.setController(new HosAuditController(this));
            removeFragment(R.id.recap_hours_fragment, recap);
        }

        loadData();
    }

    @Override
    protected void InitController() {
        this.setController(new HosAuditController(this));
    }

    @Override
    protected void loadData() {
        super.loadData();
        if (_currentFrag == VIEW_CLOCKS) {
            HosAuditController controller = getMyController();

            controller.UpdateForCurrentLogEvent();

            ClockData _driveTimeData = new ClockData();
            DutySummary driveTimeSummary = controller.DriveTimeSummary();
            if (driveTimeSummary != null) {
                _driveTimeData = new ClockData(driveTimeSummary);
            }

            ClockData _driveTimeData_ResetBreak = null;
            _driveTimeSummary_ResetBreak = controller.DriveTimeRestBreakSummary();
            if (_driveTimeSummary_ResetBreak != null) {
                _driveTimeData_ResetBreak = new ClockData(_driveTimeSummary_ResetBreak);
            }

            ClockData _dailyOnDutyData = new ClockData();
            DutySummary dailyDutySummary = controller.DailyDutySummary();
            if (dailyDutySummary != null) {
                _dailyOnDutyData = new ClockData(dailyDutySummary);
            }

            ClockData _weeklyOnDutyData = new ClockData();
            DutySummary weeklyDutySummary = controller.WeeklyDutySummary();
            if (weeklyDutySummary != null) {
                _weeklyOnDutyData = new ClockData(weeklyDutySummary);
            }
            _clocksFrag.updateClocks(_driveTimeData, _driveTimeData_ResetBreak, _dailyOnDutyData, _weeklyOnDutyData, _driveTimeSummary_ResetBreak);
        } else if (_currentFrag == VIEW_HOURS) {
            _availHoursFrag.init(getMyController());
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("currentFrag", _currentFrag);
        outState.putInt("currentItemIndex", _currentItemIndex);

        super.onSaveInstanceState(outState);
    }

    protected HosAuditController getMyController() {
        return (HosAuditController) this.getController();
    }

    public String getActivityMenuItemList() {
        return getString(R.string.rptavailhours_actionitems);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.CreateOptionsMenu(menu, false);
        return true;
    }

    public void onNavItemSelected(int itemPosition) {
        switch (itemPosition) {
            case 0:
                loadContentFragment(new DOTClocksFrag());
                loadFragment(R.id.recap_hours_fragment, new RecapHoursFrag());
                _currentFrag = VIEW_CLOCKS;
                _currentItemIndex = itemPosition;
                break;
            case 1:
                loadContentFragment(new RptAvailHoursFrag());
                _currentFrag = VIEW_HOURS;
                _currentItemIndex = itemPosition;
                break;
            case 2:
                this.finish();
                this.startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
                break;
        }

        // When using the drop down menu, we have to manually set the index and simulate the leftnav item click
        if (itemPosition <= 1 && _lastItemIndex == 999999999 && itemPosition != 999999999) {
            _lastItemIndex = itemPosition;
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


    protected IAPIController getMyEmpController() {
        return _controllerEmp;
    }

    public IAPIController getEmployeeLogController() {
        return getMyEmpController();
    }

}
