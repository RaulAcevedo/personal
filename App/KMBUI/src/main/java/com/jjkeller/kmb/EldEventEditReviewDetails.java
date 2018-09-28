package com.jjkeller.kmb;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.jjkeller.kmb.fragments.EldEventEditReviewDetailsFrag;
import com.jjkeller.kmb.fragments.LeftNavFrag;
import com.jjkeller.kmb.fragments.NavButtonsFrag;
import com.jjkeller.kmb.interfaces.INavButtonsFrag;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmbapi.calcengine.Enums;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.interfaces.IAPIController;
import com.jjkeller.kmbapi.controller.share.MandateObjectFactory;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;
import com.jjkeller.kmbui.R;

import java.util.List;
import java.util.Date;
import java.util.TimeZone;

public class EldEventEditReviewDetails extends BaseActivity implements LeftNavFrag.OnNavItemSelectedListener, LeftNavFrag.ActivityMenuItemsListener, INavButtonsFrag.NavButtonsControllerMethods {
    public static final String EXTRA_EMPLOYEELOGKEY = "employeeLogKey";
    public static final String EXTRA_EMPLOYEELOGDATE = "employeeLogDate";
    public static final String EXTRA_CURRENTEVENTINDEX = "currentEventIndex";
    private EldEventEditReviewDetailsFrag  _contentFrag;
    private NavButtonsFrag _navButtonsFrag;
    Integer _logKey = 0;
    Date _logDate = null;
    Integer _currEventIdx=0;
    Integer _eventId=0;
    IAPIController _controller = MandateObjectFactory.getInstance(this, GlobalState.getInstance().getFeatureService()).getCurrentEventController();;
    public EmployeeLogEldEvent[] _eldEvents;

    private String _navButtonTitle;
    public String getNavButtonsTitle(){ return _navButtonTitle; }
    public void setNavButtonsTitle(String value) { _navButtonTitle = value; }

    public int getCurrentItemIndex() { return _currEventIdx; }
    public int getTotalItemCount() { return _eldEvents == null ? 0 : _eldEvents.length; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.eldeventeditreviewdetails);

        _logKey = getIntent().getIntExtra(EXTRA_EMPLOYEELOGKEY, 1);
        _logDate = DateUtility.getDateFromString(getIntent().getStringExtra(EXTRA_EMPLOYEELOGDATE));
        _currEventIdx = getIntent().getIntExtra(EXTRA_CURRENTEVENTINDEX, 0);

        String title = getString(R.string.lbllogdateformat, DateUtility.getHomeTerminalFullMonthDateFormat().format(_logDate));
        this.setNavButtonsTitle(title);

        if (savedInstanceState==null){
            _logKey = getIntent().getIntExtra(EXTRA_EMPLOYEELOGKEY, 1);
            _currEventIdx = getIntent().getIntExtra(EXTRA_CURRENTEVENTINDEX, 0);
        }
        else {
            _logKey = savedInstanceState.getInt(EXTRA_EMPLOYEELOGKEY, 1);
            _currEventIdx = savedInstanceState.getInt(EXTRA_CURRENTEVENTINDEX, 0);
        }

        List<EmployeeLogEldEvent> _eldEventList = _controller.getReconcileChangeRequestedEldEvents(_logKey, Enums.ReconcileChangeRequestedEldEventsEnum.ACCEPT_PREVIEW);
        _eldEvents = _eldEventList.toArray(new EmployeeLogEldEvent[_eldEventList.size()]);

        loadContentFragment(new EldEventEditReviewDetailsFrag());
        this.loadControls(savedInstanceState);
        loadNavFragment();

		TextView _lblEditLogRequestSubtitle = (TextView) findViewById(R.id.lblEditLogRequestSubtitle);
        String subtitle = getString(R.string.editlogrequestsubtitle);
        _lblEditLogRequestSubtitle.setText(Html.fromHtml(subtitle));
    }

    @Override
    public void handleBtnPrevious() {
        if (_currEventIdx>0) {
            _currEventIdx--;
            loadData();
        }
    }

    @Override
    public void handleBtnNext() {
        if (_currEventIdx<_eldEvents.length-1){
            _currEventIdx++;
            loadData();
        }
    }

    @Override
    protected void InitController() {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.CreateOptionsMenu(menu, false);

        //  Keep left sidebar links but remove the upper right Menu - we want the dialog to behave like a modal dialog so a user can't link to another workflow
        return false;
    }


    @Override /* leftnav fragment */
    public String getActivityMenuItemList()
    {
        return getString(R.string.rptdailyhours_actionitems);
    }

    @Override /* leftnav fragment */
    public void onNavItemSelected(int itemPosition){
        if (itemPosition == 0) {
            _recentlyStartedActivityUri = null; //This prevents an error which would stop navigation to this same activity for the same event on a second selection of "View" (on older Android Versions)
            this.finish();
        }
    }

    @Override
    public void setFragments()
    {
        super.setFragments();
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
        _contentFrag = (EldEventEditReviewDetailsFrag)f;
        loadData();
    }

    @Override
    protected void loadData()
    {
        if (_currEventIdx>=0 && _currEventIdx<_eldEvents.length ) {
            _eventId = (int) _eldEvents[_currEventIdx].getPrimaryKey();
            _contentFrag.setEvent(_eldEvents, _currEventIdx);
        }
        else {
            _contentFrag.setEvent(_eldEvents, 0);
        }
        _navButtonsFrag.updateCurrentItemDisplay();
    }

    @Override
    protected void loadControls() {
        super.loadControls();
    }

    private void loadNavFragment(){
        // Keeping the title on Global state
        FrameLayout layout = (FrameLayout) findViewById(R.id.datenav_fragment);
        if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
            _navButtonsFrag = new NavButtonsFrag();
            loadFragment(R.id.datenav_fragment, _navButtonsFrag);
            if (layout != null)
                layout.setVisibility(View.VISIBLE);
        }
        else{
            if (layout != null)
                layout.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
            outState.putInt(EXTRA_EMPLOYEELOGKEY, _logKey);
            outState.putInt(EXTRA_CURRENTEVENTINDEX, _currEventIdx );

        }
}
