package com.jjkeller.kmb;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;

import com.actionbarsherlock.view.MenuItem;
import com.jjkeller.kmb.fragments.DOTClocksFrag;
import com.jjkeller.kmb.fragments.LeftNavFrag;
import com.jjkeller.kmb.fragments.RecapHoursFrag;
import com.jjkeller.kmb.interfaces.IRecapHours;
import com.jjkeller.kmb.share.ClockData;
import com.jjkeller.kmb.share.OffDutyBaseActivity;
import com.jjkeller.kmb.share.ViewOnlyModeNavHandler;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.HosAuditController;
import com.jjkeller.kmbapi.controller.interfaces.IAPIController;
import com.jjkeller.kmbapi.controller.share.MandateObjectFactory;
import com.jjkeller.kmbapi.proxydata.DutySummary;
import com.jjkeller.kmbui.R;

public class ViewClocks extends OffDutyBaseActivity implements
        LeftNavFrag.OnNavItemSelectedListener, LeftNavFrag.ActivityMenuItemsListener,
        IRecapHours.RecapHoursFragControllerMethods
{

    private ViewOnlyModeNavHandler _viewOnlyHandler;

    private IAPIController _controllerEmp = null;

    private DutySummary _driveTimeSummary_ResetBreak;

    DOTClocksFrag _clocksFrag;

    private int _myIndex;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _viewOnlyHandler = new ViewOnlyModeNavHandler(this);
        _viewOnlyHandler.setCurrentActivity(ViewOnlyModeNavHandler.ViewOnlyModeActivity.VIEWCLOCKS);

        _myIndex = _viewOnlyHandler.getCurrentActivity().index();

        // Used for handling highlighting the selected item in the leftnav
        // If not using multiple fragments within an activity, we have to manually set the selected item
        this.setLeftNavSelectedItem(_myIndex);
        this.setLeftNavAllowChange(true);

        setContentView(R.layout.viewclocks);
        _clocksFrag = new DOTClocksFrag();
        loadContentFragment(_clocksFrag);

        loadControls(savedInstanceState);
    }


    @Override
    protected void onResume() {
        this.setLeftNavSelectedItem(_myIndex);
        this.loadLeftNavFragment();

        super.onResume();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (GlobalState.getInstance().getIsViewOnlyMode()) {
            //disable the back button in view only mode
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void loadControls(Bundle savedInstanceState) {

        super.loadControls(savedInstanceState);
        loadFragment(R.id.recap_hours_fragment, new RecapHoursFrag());
    }

    @Override
    public void setFragments() {
        super.setFragments();
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_fragment);

        _clocksFrag = (DOTClocksFrag) f;
        this.setController(new HosAuditController(this));

        loadData();

    }

    @Override
    protected void InitController() {
        this.setController(new HosAuditController(this));
        _controllerEmp = MandateObjectFactory.getInstance(this,GlobalState.getInstance().getFeatureService()).getCurrentEventController();
    }


    @Override
    protected void loadData() {
        super.loadData();

        HosAuditController controller = getMyController();
        controller.UpdateForCurrentLogEvent();

        ClockData _driveTimeData = new ClockData(controller.DriveTimeSummary());
        ClockData _dailyOnDutyData = new ClockData(controller.DailyDutySummary());
        ClockData _weeklyOnDutyData = new ClockData(controller.WeeklyDutySummary());


        //Lo
        ClockData _driveTimeData_ResetBreak = null;
        _driveTimeSummary_ResetBreak = controller.DriveTimeRestBreakSummary();
        if (_driveTimeSummary_ResetBreak != null) {
            _driveTimeData_ResetBreak = new ClockData(_driveTimeSummary_ResetBreak);
        }

        _clocksFrag.updateClocks(_driveTimeData, _driveTimeData_ResetBreak, _dailyOnDutyData, _weeklyOnDutyData, _driveTimeSummary_ResetBreak);
    }

    protected HosAuditController getMyController() {
        return (HosAuditController) this.getController();
    }

    protected IAPIController getMyEmpController() {
        return _controllerEmp;
    }

    public String getActivityMenuItemList() {
        return _viewOnlyHandler.getActivityMenuItemList(null);
    }

    public void onNavItemSelected(int itemPosition) {
        if (_viewOnlyHandler.getIsViewOnlyMode()) {
            Intent intent = _viewOnlyHandler.handleMenuItemSelected(itemPosition);

            if (intent != null) {
                this.finish();
                this.startActivity(intent);
            }
        }

        getSupportFragmentManager().executePendingTransactions();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onNavItemSelected(item.getItemId());
        super.onOptionsItemSelected(item);
        return true;
    }

    public IAPIController getEmployeeLogController() {
        return getMyEmpController();
    }

}
