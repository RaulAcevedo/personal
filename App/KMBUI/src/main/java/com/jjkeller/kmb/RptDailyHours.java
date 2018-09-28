package com.jjkeller.kmb;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.jjkeller.kmb.fragments.LeftNavFrag;
import com.jjkeller.kmb.fragments.RecapHoursFrag;
import com.jjkeller.kmb.fragments.RptDailyHoursFrag;
import com.jjkeller.kmb.interfaces.IRecapHours;
import com.jjkeller.kmb.interfaces.IRptDailyHours.RptDailyHoursFragControllerMethods;
import com.jjkeller.kmb.share.OffDutyBaseActivity;
import com.jjkeller.kmb.share.ViewOnlyModeNavHandler;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.interfaces.IAPIController;
import com.jjkeller.kmbapi.controller.share.MandateObjectFactory;
import com.jjkeller.kmbui.R;

public class RptDailyHours extends OffDutyBaseActivity
        implements RptDailyHoursFragControllerMethods, LeftNavFrag.OnNavItemSelectedListener,
        LeftNavFrag.ActivityMenuItemsListener, IRecapHours.RecapHoursFragControllerMethods
{
    private ViewOnlyModeNavHandler _viewOnlyHandler;
    RptDailyHoursFrag _contentFrag;
    private int _myIndex;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        SupressOffDutyCounter(true);
        super.onCreate(savedInstanceState);
        _viewOnlyHandler = new ViewOnlyModeNavHandler(this);
        _viewOnlyHandler.setCurrentActivity(ViewOnlyModeNavHandler.ViewOnlyModeActivity.VIEWRECAPINFO );

        setContentView(R.layout.rptdailyhours);

        if(_viewOnlyHandler.getIsViewOnlyMode()){
            SupressOffDutyCounter(false);
            _myIndex = _viewOnlyHandler.getCurrentActivity().index();
        }

        else
            _myIndex = 1;
        this.setLeftNavSelectedItem(_myIndex);
        this.setLeftNavAllowChange(true);
        mFetchLocalDataTask = new FetchLocalDataTask(this.getClass().getSimpleName(), savedInstanceState);
        mFetchLocalDataTask.execute();

    }

    public IAPIController getMyController()
    {
        return (IAPIController) this.getController();
    }

    protected void InitController()
    {
        this.setController(MandateObjectFactory.getInstance(this, GlobalState.getInstance().getFeatureService()).getCurrentEventController());
    }

    @Override
    protected void loadControls(Bundle savedInstanceState)
    {
        super.loadControls();
        loadContentFragment(new RptDailyHoursFrag());
        loadFragment(R.id.recap_hours_fragment, new RecapHoursFrag());
    }

    @Override
    public void setFragments()
    {
        super.setFragments();

        Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
        _contentFrag = (RptDailyHoursFrag) f;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        this.CreateOptionsMenu(menu, false);
        return true;
    }

    public String getActivityMenuItemList()
    {
        if (_viewOnlyHandler.getIsViewOnlyMode())
            return _viewOnlyHandler.getActivityMenuItemList(null);
        else
            return getString(R.string.rptdailyhours_actionitems);
    }

    private void handleMenuItemSelected(int itemPosition)
    {
        if (itemPosition == 0) {
            this.finish();
            this.startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
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

    public void onNavItemSelected(int menuItem)
    {
        if(_viewOnlyHandler.getIsViewOnlyMode())
        {
            Intent intent = _viewOnlyHandler.handleMenuItemSelected(menuItem);

            if(intent != null)
            {
                this.finish();
                this.startActivity(intent);
            }
        }
        else
            handleMenuItemSelected(menuItem);
    }

    @Override
    public IAPIController getEmployeeLogController()
    {
        return getMyController();
    }
}
