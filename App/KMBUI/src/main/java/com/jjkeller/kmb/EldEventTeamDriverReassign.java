package com.jjkeller.kmb;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.actionbarsherlock.view.Menu;
import com.jjkeller.kmb.adapters.TeamDriverItem;
import com.jjkeller.kmb.fragments.EldEventTeamDriverReassignFrag;
import com.jjkeller.kmb.fragments.LeftNavFrag;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.EmployeeLogEldMandateController;
import com.jjkeller.kmbapi.controller.dataaccess.db.LogTeamDriverPersist;
import com.jjkeller.kmbapi.controller.interfaces.IAPIController;
import com.jjkeller.kmbapi.controller.share.MandateObjectFactory;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.proxydata.TeamDriver;
import com.jjkeller.kmbui.R;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Activity for Reassigning Driving event to another team driver on a shared or separate device.
 */
public class EldEventTeamDriverReassign extends BaseActivity implements LeftNavFrag.OnNavItemSelectedListener, LeftNavFrag.ActivityMenuItemsListener {

    public static final String EXTRA_EMPLOYEELOGKEY = "employeeLogKey";
    public static final String EXTRA_ELDEVENTKEY = "eldEventKey";
    public static final String EXTRA_EVENTSTARTTIME = "eventStartTime";
    public static final String EXTRA_EVENTENDTIME = "eventEndTime";
    public static final String EXTRA_ISEDITING = "isEditing";

    private int _employeeLogKey = 0;
    private int _eldEventKey = 0;
    private Date _eventStartTime;
    private Date _eventEndTime;
    private boolean _isEditing = true;

    private EldEventTeamDriverReassignFrag _contentFrag;
    private List<TeamDriverItem> _teamDriverList = new ArrayList<TeamDriverItem>();

    public EmployeeLogEldMandateController getMandateController() {
        if (getController() != null)
            return (EmployeeLogEldMandateController) getController();

        return null;
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.baselayout);

        if (savedInstanceState != null) {  // screen rotation
            _employeeLogKey = savedInstanceState.getInt(EXTRA_EMPLOYEELOGKEY);
            _eldEventKey = savedInstanceState.getInt(EXTRA_ELDEVENTKEY);

            String date = savedInstanceState.getString(EXTRA_EVENTSTARTTIME);
            if (date.length() == 0)
                _eventStartTime = null;
            else
                _eventStartTime = getDateFromString(date);

            date = savedInstanceState.getString(EXTRA_EVENTENDTIME);
            if (date.length() == 0)
                _eventEndTime = null;
            else
                _eventEndTime = getDateFromString(date);

            _isEditing = savedInstanceState.getBoolean(EXTRA_ISEDITING);
        }
        else { // intent
            Bundle bundle = getIntent().getExtras();
            _employeeLogKey = bundle.getInt(EXTRA_EMPLOYEELOGKEY);
            _eldEventKey = bundle.getInt(EXTRA_ELDEVENTKEY);
            _eventStartTime = getDateFromString(getIntent().getExtras().getString(EXTRA_EVENTSTARTTIME));
            _eventEndTime = getDateFromString(getIntent().getExtras().getString(EXTRA_EVENTENDTIME));
        }

        mFetchLocalDataTask = new FetchLocalDataTask(this.getClass().getSimpleName());
        mFetchLocalDataTask.execute();
    }

    /**
     * Called during screen rotation to persist values so the screen can be re-created.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(EXTRA_EMPLOYEELOGKEY, _employeeLogKey);
        outState.putInt(EXTRA_ELDEVENTKEY, _eldEventKey);
        outState.putString(EXTRA_EVENTSTARTTIME, _eventStartTime == null ? "" : DateUtility.getHomeTerminalReferenceTimestampFormat().format(_eventStartTime));
        outState.putString(EXTRA_EVENTENDTIME, _eventEndTime == null ? "" : DateUtility.getHomeTerminalReferenceTimestampFormat().format(_eventEndTime));
        outState.putBoolean(EXTRA_ISEDITING, _isEditing);

        super.onSaveInstanceState(outState);
    }

    /**
     * Store a handle to each control (child view).
     */
    protected void findControls() {
    }

    @Override
    public void setFragments() {
        super.setFragments();

        Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
        _contentFrag = (EldEventTeamDriverReassignFrag)f;
        _contentFrag.setTeamDrivers(_teamDriverList);   // set adapter data since background loadData completed
    }

    /**
     * Called from BaseActivity FetchLocalDataTask::onPostExecute
     */
    @Override
    protected void loadControls() {
        super.loadControls();

        loadContentFragment(new EldEventTeamDriverReassignFrag());
    }

    @Override
    protected void InitController() {
        IAPIController empLogCtrl = MandateObjectFactory.getInstance(this, GlobalState.getInstance().getFeatureService()).getCurrentEventController();
        this.setController(empLogCtrl);
    }

    /**
     * MENU REGION
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        this.CreateOptionsMenu(menu, false);

        return true;
    }

    public String getActivityMenuItemList() {
        StringBuilder sb = new StringBuilder();

        Resources res = getResources();

        if (_isEditing) {
            sb.append(res.getString(R.string.apply) + ",");
            sb.append(res.getString(R.string.btncancel));
        }
        else {
            sb.append(res.getString(R.string.done));
        }

        return sb.toString();
    }

    public void onNavItemSelected(int itemPosition) {

        if (_isEditing) {
            switch (itemPosition)
            {
                case 0:
                    this.onApplyClick();
                    break;
                case 1:
                    this.onCancelClick();
                    break;
            }
        }
        else {
            this.onDoneClick();
        }
    }

    /**
     * Invoked when the Apply button is clicked.
     */
    private void onApplyClick() {
        List<String> brokenRules = _contentFrag.getBrokenRules();

        if(brokenRules.isEmpty()) {

            _contentFrag.hideHUD();

            mSaveLocalDataTask = new SaveLocalDataTask(this.getClass().getSimpleName(), this.getString(R.string.msgsaving));
            mSaveLocalDataTask.execute();
        }
        else {
            String errorMessage = "";
            String htmlNewline = "<br>";
            for (String error : brokenRules) {
                if (errorMessage.length() > 0)
                    errorMessage += htmlNewline;
                errorMessage += error;
            }

            _contentFrag.showHUDMessage(errorMessage);
        }
    }

    /**
     * Invoked when the Cancel button is clicked.
     */
    private void onCancelClick() {
        finish();
    }

    /**
     * Invoked when the Done button is clicked.
     */
    private void onDoneClick() {
        // since ELD Event is now Inactive - return to the Edit Log List
        Bundle bundle = new Bundle();
        bundle.putInt(ViewLog.EXTRA_INITIALFRAGINDEX, 6);
        startActivity(ViewLog.class, Intent.FLAG_ACTIVITY_CLEAR_TOP, bundle);
    }

    @Override
    public void onBackPressed() {
        if (_isEditing)
            finish();
        else {
            // since ELD Event is now Inactive - return to the Edit Log List
            Bundle bundle = new Bundle();
            bundle.putInt(ViewLog.EXTRA_INITIALFRAGINDEX, 6);
            startActivity(ViewLog.class, Intent.FLAG_ACTIVITY_CLEAR_TOP, bundle);
        }
    }

    /**
     * Called from BaseActivity FetchLocalDataTask so data is loaded on AsyncTask
     */
    @Override
    protected void loadData() {
        // Display driver code and name for each team driver (drop down menu)
        LogTeamDriverPersist<TeamDriver> teamDriverPersist = new LogTeamDriverPersist<TeamDriver>(TeamDriver.class, this.getBaseContext(), GlobalState.getInstance().getCurrentUser(), _employeeLogKey);
        List<TeamDriver> teamDriverList =  teamDriverPersist.FetchList();

        if (_eventStartTime != null && _eventEndTime != null) {
            for (TeamDriver teamDriver : teamDriverList) {
                Date teamStartTime = teamDriver.getStartTime();
                Date teamEndTime = teamDriver.getEndTime();

                if (teamStartTime == null)
                    continue;

                // start/end times of the team driver cover the start/end times of the driving event
                if (teamStartTime != null && teamEndTime != null) {
                    if (teamStartTime.compareTo(_eventStartTime) <= 0 && (teamEndTime.compareTo(_eventEndTime) >= 0)) { // account for dates exactly matching
                        _teamDriverList.add(new TeamDriverItem(teamDriver.getPrimaryKey(), teamDriver.getEmployeeCode(), teamDriver.getKMBUsername(), teamDriver.getDisplayName()));
                    }
                } else if (teamEndTime == null) {
                    if (teamStartTime.compareTo(_eventStartTime) <= 0) { // team driver still logged in
                        _teamDriverList.add(new TeamDriverItem(teamDriver.getPrimaryKey(), teamDriver.getEmployeeCode(), teamDriver.getKMBUsername(), teamDriver.getDisplayName()));
                    }
                }
            }
        }

        // sort ascending by EmployeeCode
        Collections.sort(_teamDriverList, new Comparator<TeamDriverItem>(){
            public int compare(TeamDriverItem obj1, TeamDriverItem obj2) {
                // Ascending order
                return obj1.getEmployeeCode().compareToIgnoreCase(obj2.getEmployeeCode());
            }
        });

        // insert default NULL entry
        _teamDriverList.add(0, new TeamDriverItem(-1, "", "", getString(R.string.notspecifiedprompt)));
    }

    /**
     * Persist the model to the database.
     */
    @Override
    protected boolean saveData() {
        String message = "";

        try {

            GlobalState.TeamDriverModeEnum teamDriverModeEnum = GlobalState.TeamDriverModeEnum.SEPARATEDEVICE;

            // if the team driver that is selected is not currently one of the logged in users, then you have a team driver in separate device
            TeamDriverItem teamDriverItem = _contentFrag.getTeamDriverItem();
            if (teamDriverItem == null) {
                message = "Driver not selected";
            }

            ArrayList<User> loggedInUserList = GlobalState.getInstance().getLoggedInUserList();
            for (User user : loggedInUserList) {
                if (user.getCredentials().getEmployeeCode().equalsIgnoreCase(teamDriverItem.getEmployeeCode())) {
                    teamDriverModeEnum  = GlobalState.TeamDriverModeEnum.SHAREDDEVICE;
                    break;
                }
            }

            boolean success = getMandateController().reassignEldEventTeamDriving(_eldEventKey, _contentFrag.getEventCode(), teamDriverModeEnum, teamDriverItem.getKMBUserName(), _contentFrag.getAnnotation());

            if(!success && teamDriverModeEnum == GlobalState.TeamDriverModeEnum.SEPARATEDEVICE) {
                runOnUiThread( new Runnable() {
                    public void run() {
                        EldEventTeamDriverReassign.this.ShowMessage(EldEventTeamDriverReassign.this, EldEventTeamDriverReassign.this.getResources().getString(R.string.msg_cantsubmiteventreassignmenttoencompass));
                    }});
            }
        } catch (Throwable ex) {
            message = ex.getMessage();
        }

        // show result in Heads-Up Display
        final String hudMessage = message;
        runOnUiThread( new Runnable() {
            public void run() {
                _contentFrag.showHUDMessage(hudMessage);

                if (hudMessage.isEmpty()) {
                    // hide Apply/Cancel and show Done button
                    _isEditing = false;
                    BuildLeftNavMenu();
                }
            }});

        return true;
    }

    /**
     * Called from BaseActivity SaveLocalDataTask::onPostExecute
     */
    @Override
    protected void Return(boolean success) {
    }

    /**
     * Helper functions
     */
    private Date getDateFromString(String dateTimeString) {
        try {
            return DateUtility.getHomeTerminalReferenceTimestampFormat().parse(dateTimeString);
        } catch (ParseException e) {

            Log.e("UnhandledCatch", e.getMessage() + ": " + Log.getStackTraceString(e));
        }

        return  null;
    }
}
