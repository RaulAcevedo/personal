package com.jjkeller.kmb;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.jjkeller.kmb.fragments.EldEventEditFrag;
import com.jjkeller.kmb.fragments.LeftNavFrag;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.EmployeeLogEldMandateController;
import com.jjkeller.kmbapi.controller.dataaccess.EmployeeLogFacade;
import com.jjkeller.kmbapi.controller.interfaces.IAPIController;
import com.jjkeller.kmbapi.controller.share.MandateObjectFactory;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.controller.utility.EmployeeLogUtilities;
import com.jjkeller.kmbapi.controller.utility.ErrorLogHelper;
import com.jjkeller.kmbapi.employeelogeldevents.Enums;
import com.jjkeller.kmbapi.enums.EmployeeLogProvisionTypeEnum;
import com.jjkeller.kmbapi.enums.TimeZoneEnum;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEventList;
import com.jjkeller.kmbapi.employeelogeldevents.EmployeeLogEldEventCode;
import com.jjkeller.kmbapi.proxydata.TeamDriver;
import com.jjkeller.kmbapi.controller.dataaccess.db.LogTeamDriverPersist;
import com.jjkeller.kmbui.R;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Activity for Add or Edit of EmployeeLogEldEvent record.
 */
public class EldEventEdit extends BaseActivity implements LeftNavFrag.OnNavItemSelectedListener, LeftNavFrag.ActivityMenuItemsListener {

    public static final String EXTRA_PRIMARYKEY = "primaryKey";
    public static final String EXTRA_EMPLOYEELOGKEY = "employeeLogKey";
    public static final String EXTRA_ELD_EVENT_LIST = "eldEventList";
    public static final String EXTRA_EVENTDATETIME = "eventDateTime";
    public static final String EXTRA_SPECIAL_DRIVING_CONDITION = "specialDrivingCondition";
    public static final String EXTRA_CURRENTEVENTINDEX = "currentEventIndex";
    public static final String EXTRA_ISEDITING = "isEditing";
    public static final String EXTRA_HUDMESSAGE = "hudMessage";
    public static final String EXTRA_HUDISADDING = "hudIsEditing";
    public static final String EXTRA_HUDISVISIBLE = "hudIsVisible";
    public static final String EXTRA_ISREASSIGNABLE = "isRessignable";
    public static final String EXTRA_ISCURRENTDAY = "isCurrentDay";

    private List<Pair<EmployeeLogEldEvent, Enums.SpecialDrivingCategory>> _eldEventList = new ArrayList<>();
    private int _parentEmployeeLogKey = 0;
    private EmployeeLogProvisionTypeEnum _specialDrivingCondition;
    private Date _eventDateTime;
    private int _currentIndex = 0;
    private boolean _isEditing = false;


    private EldEventEditFrag _contentFrag;

    private TextView _txtLogDate;
    private Button _btnPreviousEvent;
    private TextView _txtEventXofX;
    private Button _btnNextEvent;
    private boolean _navigationInProgress = false;

    private LinearLayout _linearHUD;
    private TextView _txtError;
    private TextView _txtSuccess;
    private String _hudMessage = "";
    private boolean _hudIsAdding = false;
    private boolean _hudIsVisible = false;
    private boolean _isReassignable = false;

    List<TeamDriver> _teamDriverList = new ArrayList<>();
    boolean _isEndTimeTodaysCurrentTime = false;
    boolean _isCurrentDay;
    public EmployeeLogEldMandateController getMandateController() {
        if (getController() != null)
            return (EmployeeLogEldMandateController) getController();

        return null;
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View baseLayout = LayoutInflater.from(this).inflate(R.layout.baselayout, null);

        // This Activity layout will contain the Date, Navigation buttons and Heads-Up Display text;
        // To accomplish this, programmatically swap the content_fragment with the eldeventedit.xml.xml layout
        // which contains the same content_fragment but also additional Views.
        FrameLayout contentFragment = (FrameLayout) baseLayout.findViewById(R.id.content_fragment);
        ViewGroup parent = ((ViewGroup) contentFragment.getParent());
        parent.removeView(contentFragment);

        float weight = 80.0f;
        Configuration config = getResources().getConfiguration();
        if (config.orientation == Configuration.ORIENTATION_PORTRAIT)
            weight = 65.0f; // Reassign button is rather long text

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, weight);
        View eldeventLayout = LayoutInflater.from(this).inflate(R.layout.eldeventedit, null);
        parent.addView(eldeventLayout, params);

        setContentView(baseLayout);

        if (savedInstanceState != null) {
            // screen rotation
            _parentEmployeeLogKey = savedInstanceState.getInt(EXTRA_EMPLOYEELOGKEY);
            _eventDateTime = getDateFromString(savedInstanceState.getString(EXTRA_EVENTDATETIME));
            _specialDrivingCondition = (EmployeeLogProvisionTypeEnum) savedInstanceState.getSerializable(EXTRA_SPECIAL_DRIVING_CONDITION);
            _currentIndex = savedInstanceState.getInt(EXTRA_CURRENTEVENTINDEX);
            _isEditing = savedInstanceState.getBoolean(EXTRA_ISEDITING);
            _hudMessage = savedInstanceState.getString(EXTRA_HUDMESSAGE);
            _hudIsAdding = savedInstanceState.getBoolean(EXTRA_HUDISADDING);
            _hudIsVisible = savedInstanceState.getBoolean(EXTRA_HUDISVISIBLE);
            _isReassignable = savedInstanceState.getBoolean(EXTRA_ISREASSIGNABLE);
            _isCurrentDay = savedInstanceState.getBoolean(EXTRA_ISCURRENTDAY);
        }
        else {
            Bundle bundle = getIntent().getExtras();
            _parentEmployeeLogKey = bundle.getInt(EXTRA_EMPLOYEELOGKEY);
            _eventDateTime = getDateFromString(getIntent().getExtras().getString(EXTRA_EVENTDATETIME));
            _specialDrivingCondition = (EmployeeLogProvisionTypeEnum) bundle.getSerializable(EXTRA_SPECIAL_DRIVING_CONDITION);
            _currentIndex = getIntent().getExtras().getInt(EXTRA_CURRENTEVENTINDEX);
            _hudMessage = "";
            _hudIsAdding = false;
            _hudIsVisible = false;
        }

        findControls();

        mFetchLocalDataTask = new FetchLocalDataTask(this.getClass().getSimpleName());
        mFetchLocalDataTask.execute();
    }
    public boolean getIsCurrentDay() {
        return _isCurrentDay;
    }

    /**
     * Called during screen rotation to persist values so the screen can be re-created.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(EXTRA_EMPLOYEELOGKEY, _parentEmployeeLogKey);
        outState.putString(EXTRA_EVENTDATETIME, DateUtility.getHomeTerminalReferenceTimestampFormat().format(_eventDateTime));

        if (_eldEventList.size() - 1 == _currentIndex && _eldEventList.get(_currentIndex).first.getPrimaryKey() == -1)
            outState.putInt(EXTRA_CURRENTEVENTINDEX, -1);
        else
            outState.putInt(EXTRA_CURRENTEVENTINDEX, _currentIndex);

        outState.putSerializable(EXTRA_SPECIAL_DRIVING_CONDITION, _specialDrivingCondition);
        outState.putBoolean(EXTRA_ISEDITING, _isEditing);
        outState.putString(EXTRA_HUDMESSAGE, _hudMessage);
        outState.putBoolean(EXTRA_HUDISADDING, _hudIsAdding);
        outState.putBoolean(EXTRA_HUDISVISIBLE, _hudIsVisible);
        outState.putBoolean(EXTRA_ISREASSIGNABLE, _isReassignable);
        outState.putBoolean(EXTRA_ISCURRENTDAY, _isCurrentDay);

        super.onSaveInstanceState(outState);
    }

    /**
     * Store a handle to each control (child view).
     */
    protected void findControls() {
        _txtLogDate = (TextView)findViewById(R.id.lblLogDate);
        _txtEventXofX = (TextView)findViewById(R.id.lblEventXofX);

        _linearHUD = (LinearLayout)findViewById(R.id.linearHUD);
        _txtError = (TextView)findViewById(R.id.txtError);
        _txtSuccess = (TextView)findViewById(R.id.txtSuccess);

        _btnPreviousEvent = (Button)findViewById(R.id.btnPreviousEvent);
        _btnPreviousEvent.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        if (!_navigationInProgress) {
                            _navigationInProgress = true;

                            _currentIndex--;

                            clearFocusAndDismissSIP();
                            updateUI();
                            hideHUD();
                            updateFragmentUI();

                            _navigationInProgress = false;
                        }
                    }
                });

        _btnNextEvent = (Button)findViewById(R.id.btnNextEvent);
        _btnNextEvent.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        if (!_navigationInProgress) {
                            _navigationInProgress = true;

                            _currentIndex++;

                            clearFocusAndDismissSIP();
                            updateUI();
                            hideHUD();
                            updateFragmentUI();

                            _navigationInProgress = false;
                        }
                    }
                });
    }

    @Override
    public void setFragments() {
        super.setFragments();

        Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
        _contentFrag = (EldEventEditFrag)f;
    }

    /* Called from BaseActivity FetchLocalDataTask so data is loaded on AsyncTask */
    @Override
    protected void loadData() {
        EmployeeLogFacade employeeLogFacade = new EmployeeLogFacade(getApplicationContext());
        EmployeeLog employeeLog = employeeLogFacade.FetchByKey(_parentEmployeeLogKey);
        _eldEventList = EmployeeLogUtilities.loadEventsIncludingSpecialDrivingCategories(this, employeeLog);

        //fetch any TeamDriver records associated to this EmployeeLog while in background thread
        _teamDriverList = fetchTeamDriverData();

        // if Add Event - append a new, temporary Event to the end of the list
        if (_currentIndex == -1) {
            EmployeeLogEldEvent newEldEvent =  new EmployeeLogEldEvent(_eventDateTime);

            // determine appropriate Ruleset default value
            if (_eldEventList.size() > 0) {
                // Default to a Ruleset used during the same day -- the thought being that if your editing an
                // older EmployeeLog you'll probably be using the same Ruleset for that entire day. Obviously, things
                // get a little inaccurate when you have ELD Events on the same day that cross international borders
                // but we'll try out best since we don't have an established new EventDateTime yet.
                newEldEvent.setRuleSet(_eldEventList.get(_eldEventList.size() - 1).first.getRuleSet());
            }
            else if (GlobalState.getInstance().getCurrentUser() != null){
                // not sure if you can get into this scenario, but if you have no other ELD Events
                // default to your currently active Rulset
                newEldEvent.setRuleSet(GlobalState.getInstance().getCurrentUser().getRulesetTypeEnum());
            }

            // append new Event
            _eldEventList.add(new Pair<EmployeeLogEldEvent, Enums.SpecialDrivingCategory>(newEldEvent, Enums.SpecialDrivingCategory.None));

            _currentIndex = _eldEventList.size() - 1;
            _isEditing = true;
        }
        if (_eldEventList.size() > 0 && _currentIndex < _eldEventList.size()) {
            _isCurrentDay = DateUtility.IsToday(_eldEventList.get(_currentIndex).first.getEventDateTime(), getMandateController().getCurrentUser());
        }

    }

    /* Called from BaseActivity FetchLocalDataTask::onPostExecute */
    @Override
    protected void loadControls() {
        super.loadControls();

        loadContentFragment(new EldEventEditFrag());

        String str = getString(R.string.lbllogdateformat, DateUtility.getHomeTerminalFullMonthDateFormat().format(_eventDateTime));
        _txtLogDate.setText(Html.fromHtml(str));

        updateUI();

        if (_hudIsVisible)
            showHUDMessage(_hudMessage, _hudIsAdding);
        else
            hideHUD();
    }

    @Override
    public void updateTimeDisplay(Button timeButton, Calendar c) {
        super.updateTimeWithSecondsDisplay(timeButton, c);

        // disable navigation controls while editing record
        if (!getIsEditing()) {
            String originalValue = (String) timeButton.getTag();
            if (!originalValue.equals(timeButton.getText().toString()))
                _contentFrag.setIsEditing();
        }
    }

    /**
     * Update UI in this Activity.
     */
    private void updateUI() {
        if (_eldEventList == null || _eldEventList.size() == 0 || _currentIndex >= _eldEventList.size()) {
            String str = getString(R.string.lbleventxofx, String.valueOf(0), String.valueOf(0));
            _txtEventXofX.setText(Html.fromHtml(str));

            // Enable/Disable navigation buttons based on current position
            _btnPreviousEvent.setVisibility(View.VISIBLE);
            _btnNextEvent.setVisibility(View.VISIBLE);
            _btnPreviousEvent.setEnabled(false);
            _btnNextEvent.setEnabled(false);

            _contentFrag.displayNoRecords();

            return;
        }

        // Event X of X
        String str = "";

        if (_eldEventList.get(_currentIndex).first.getPrimaryKey() == -1) {
            str = getString(R.string.adding);
        }
        else {
            str = getString(R.string.lbleventxofx, String.valueOf(_currentIndex + 1), String.valueOf(_eldEventList.size()));
            _txtEventXofX.setText(Html.fromHtml(str));

            // Enable/Disable navigation buttons based on current position
            _btnPreviousEvent.setVisibility(View.VISIBLE);
            _btnNextEvent.setVisibility(View.VISIBLE);
            _btnPreviousEvent.setEnabled(!_isEditing && _currentIndex > 0);
            _btnNextEvent.setEnabled(!_isEditing && _currentIndex < _eldEventList.size() - 1);
        }


        //Each time you navigate through the event list re-evaluate if the Reassign button enables/disables
        if (isDrivingEventReassignable()) {
            //show/enable Reassign button
            _isReassignable = true;
            super.BuildLeftNavMenu();
        }
        else {
            //hide/disable Reassign button
            _isReassignable = false;
            super.BuildLeftNavMenu();
        }
    }


    private void hideHUD() {
        _linearHUD.setVisibility(View.GONE);
        _txtError.setVisibility(View.GONE);
        _txtSuccess.setVisibility(View.GONE);

        _hudMessage = "";
        _hudIsAdding = false;
        _hudIsVisible = false;
    }

    /**
     * Update UI in the fragment based on the current Eld Event index.
     */
    public void updateFragmentUI() {
        if (_eldEventList == null || _currentIndex >= _eldEventList.size())
            return;

        Date impliedEndTime = getEditedEventImpliedEndTime();
        EmployeeLogEldEvent model = _eldEventList.get(_currentIndex).first;

        // update fragment UI
        _contentFrag.updateModelToView(_eldEventList.get(_currentIndex), _parentEmployeeLogKey, DateUtility.getHomeTerminalDateFormat().format(model.getEventDateTime()), impliedEndTime, _isEndTimeTodaysCurrentTime, _isCurrentDay, false);
    }

    @Override
    protected void InitController() {
        IAPIController empLogCtrl = MandateObjectFactory.getInstance(this, GlobalState.getInstance().getFeatureService()).getCurrentEventController();
        this.setController(empLogCtrl);
    }

    /**
     * Flag to determine if changes have been made - if so, disable certain UI components so you can't leave the screen.
     */
    public Boolean getIsEditing() {
        return _isEditing;
    }

    public void setIsEditing(boolean isEditing) {
        if (_isEditing == isEditing)
            return; // same -- do nothing

        _isEditing = isEditing;

        if (_isEditing) {
            _btnPreviousEvent.setEnabled(false);
            _btnNextEvent.setEnabled(false);

            _linearHUD.setVisibility(View.GONE);
            _txtError.setVisibility(View.GONE);
            _txtSuccess.setVisibility(View.GONE);
        }
        else {
            _btnPreviousEvent.setEnabled(_currentIndex > 0);
            _btnNextEvent.setEnabled(_currentIndex < _eldEventList.size() - 1);
        }

        // if Editing show "Apply, Cancel" in menu; otherwise "Done"
        super.BuildLeftNavMenu();
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

        if (_isEditing) {
            sb.append("Apply,");
            sb.append("Cancel");
        }
        else if (!_isEditing && _isReassignable) {
            sb.append("Reassign,");
            sb.append("Done");
        }
        else {
            sb.append("Done,");
        }

        return sb.toString();
    }

    public void onNavItemSelected(int itemPosition) {
        int menuItemIndex = -1;
        String itemText = this.getLeftNavFragment().GetNavItemText(itemPosition);

        if (itemText.equalsIgnoreCase("Done"))
            menuItemIndex = 0;
        else if (itemText.equalsIgnoreCase("Apply"))
            menuItemIndex = 1;
        else if (itemText.equalsIgnoreCase("Cancel"))
            menuItemIndex = 2;
        else if (itemText.equalsIgnoreCase("Reassign"))
            menuItemIndex = 3;

        handleNavItem(menuItemIndex);
    }

    private void handleNavItem(int itemPosition)
    {
        switch (itemPosition)
        {
            case 0:
                // Done
                finish();
                break;
            case 1:
                // Apply
                this.onApplyClick();
                break;
            case 2:
                // Cancel
                this.onCancelClick();
                break;
            case 3:
                // Reassign
                this.onReassignClick();
                break;
        }
    }

    /**
     * APPLY/CANCEL/DONE REGION
     */

    /**
     * Invoked when the Apply button is clicked.
     */
    private void onApplyClick() {

        EmployeeLogEldEvent origEldEvent = _eldEventList.get(_currentIndex).first;
        List<String> brokenRules = _contentFrag.getBrokenRules(pairListToArray(_eldEventList), _isCurrentDay);

        if(brokenRules.isEmpty()) {

            hideHUD();

            Pair<EmployeeLogEldEvent, EmployeeLogProvisionTypeEnum> result;
            EmployeeLogEldEvent editedEldEvent =  new EmployeeLogEldEvent(_eventDateTime);
            editedEldEvent.setLogKey(_parentEmployeeLogKey);
            editedEldEvent.setPrimaryKey(origEldEvent.getPrimaryKey());

            // transfer most recent UI data from the view to the model
            editedEldEvent = _contentFrag.updateViewToModel(editedEldEvent).first;

            EmployeeLogEldEvent[] tempEldEventList = new EmployeeLogEldEvent[_eldEventList.size()];

            // if the RuleSet has changed, check if it's compatible with the existing ELD Events
            if (editedEldEvent.getPrimaryKey() == -1 || editedEldEvent.getRuleSet().getValue() != origEldEvent.getRuleSet().getValue()) {

                // swap out the original event with it's updated values -- don't want to taint the original value incase user clicks Cancel to undo edits
                for (int i = 0; i < _eldEventList.size(); i++) {
                    EmployeeLogEldEvent event = _eldEventList.get(i).first;
                    if (event.getPrimaryKey() == editedEldEvent.getPrimaryKey()) {
                        tempEldEventList[i] = editedEldEvent;
                    } else
                        tempEldEventList[i] = event;
                }

                if (getMandateController().IsUSOilFieldOffDutyStatusInLog(tempEldEventList) && !editedEldEvent.getRuleSet().isAnyOilFieldRuleset()) {
                    this.ShowMessage(this, this.getResources().getString(R.string.msg_ruleset_cannotbechanged));
                    return;
                }
                else if (!getMandateController().isRulesetCombinationAllowed(tempEldEventList, editedEldEvent.getRuleSet())) {
                    this.ShowMessage(this, this.getResources().getString(R.string.rulesetchangecombinationnotallowed));
                    return;
                }
                else if (getMandateController().doesRulesetChangeUpdateOtherEldEvents(tempEldEventList, editedEldEvent.getRuleSet())) {
                    final Activity thisActivity = this;

                    ShowConfirmationMessage(this, 0,
                            getString(R.string.rulesetchangeaffectsotherlogs), R.string.lblcontinue,
                            new ShowMessageClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    super.onClick(dialog, id);
                                    mSaveLocalDataTask = new SaveLocalDataTask(this.getClass().getSimpleName(), thisActivity.getString(R.string.msgsaving));
                                    mSaveLocalDataTask.execute();
                                }
                            }, R.string.cancellabel, new ShowMessageClickListener());
                }
                else {
                    mSaveLocalDataTask = new SaveLocalDataTask(this.getClass().getSimpleName(), this.getString(R.string.msgsaving));
                    mSaveLocalDataTask.execute();
                }
            }
            else {
                mSaveLocalDataTask = new SaveLocalDataTask(this.getClass().getSimpleName(), this.getString(R.string.msgsaving));
                mSaveLocalDataTask.execute();
            }
        }
        else {
            String errorMessage = "";
            String htmlNewline = "<br>";
            for (String error : brokenRules) {
                if (errorMessage.length() > 0)
                    errorMessage += htmlNewline;
                errorMessage += error;
            }

            showHUDMessage(errorMessage, false);
        }
    }

    /**
     * Invoked when the Cancel button is clicked.
     */
    private void onCancelClick() {

        // Cancel Add will dismiss edit dialog
        if (_eldEventList.get(_currentIndex).first.getPrimaryKey() == -1) {
            finish();
            return;
        }

        // swap "Apply, Cancel" menu items for "Done"
        setIsEditing(false);

        // refresh UI with non-edited data
        updateUI();
        hideHUD();
        updateFragmentUI();
        clearFocusAndDismissSIP();
    }

    /**
     * Invoked when the Reassign button is clicked.
     */
    private void onReassignClick() {
        Bundle bundle = new Bundle();
        bundle.putInt(EldEventTeamDriverReassign.EXTRA_EMPLOYEELOGKEY, (int) _parentEmployeeLogKey);
        bundle.putInt(EldEventTeamDriverReassign.EXTRA_ELDEVENTKEY, (int) _eldEventList.get(_currentIndex).first.getPrimaryKey());
        bundle.putString(EldEventTeamDriverReassign.EXTRA_EVENTSTARTTIME, DateUtility.getHomeTerminalReferenceTimestampFormat().format(_eldEventList.get(_currentIndex).first.getEventDateTime()));
        bundle.putString(EldEventTeamDriverReassign.EXTRA_EVENTENDTIME, DateUtility.getHomeTerminalReferenceTimestampFormat().format(getEditedEventImpliedEndTime()));

        startActivity(EldEventTeamDriverReassign.class, bundle);
    }

    /**
     * Persist the model to the database.
     */
    @Override
    protected boolean saveData() {
        Pair<EmployeeLogEldEvent, Enums.SpecialDrivingCategory> eventWithProvisionsPair;
        EmployeeLogEldEvent model = _eldEventList.get(_currentIndex).first;
        Enums.SpecialDrivingCategory subStatus;
        model.setLogKey(_parentEmployeeLogKey);

        // transfer most recent UI data from the view to the model

        eventWithProvisionsPair = _contentFrag.updateViewToModel(model);
        subStatus = eventWithProvisionsPair.second;
        model = eventWithProvisionsPair.first;

        // If motion picture ruleset, populate AuthorityId and ProductionId
//        if (model.getRuleSet().toDMOEnum().contains("MotionPicture"))
//        {
        model.setMotionPictureAuthorityId(GlobalState.getInstance().get_currentMotionPictureAuthorityId());
        model.setMotionPictureProductionId(GlobalState.getInstance().get_currentMotionPictureProductionId());
//        }
        // final needed for method called within runOnUiThread
        final boolean isAdding = model.getPrimaryKey() == -1;

        String message = "";
        try {

            if (_contentFrag.validatePcToDrivingStatus()){
                model.setLogRemark(null);
                model.setLogRemarkDateTime(null);
                EmployeeLogUtilities.invalidatePCRecords(this, (int) model.getPrimaryKey());
            }

            model = getMandateController().saveEldEvent(model, subStatus, _contentFrag.getEndTime(), Enums.ActionInitiatingSaveEnum.EditLog);

            // if first time SaveLocalEmployeeLog - a new local log was created with a new ID
            _parentEmployeeLogKey = model.getLogKey();

            // swap "Apply, Cancel" menu items for "Done"
            runOnUiThread( new Runnable() {
                public void run() {
                    setIsEditing(false);
                }});

            Date editedEventDateTime = model.getEventDateTime();

            // requery events to pick up edits to other events
            loadData();

            // default to end of list -- incase we can't find matching primary key
            _currentIndex = _eldEventList.size() - 1;

            // find the edited record in the new list -- records may have changed or
            // been removed if Start/End Dates overlapped with existing events
            for (int i=0; i < _eldEventList.size(); i++) {
                if (_eldEventList.get(i).first.getEventDateTime().compareTo(editedEventDateTime) == 0) {
                    _currentIndex = i;
                    break;
                }
            }

            if (DateUtility.IsToday(_eventDateTime, GlobalState.getInstance().getCurrentUser()))
            {
                // the user is modifying the current day's log, we also need to update the in-memory events on the log so they
                // are not purged when the log is saved.
                EmployeeLogEldEvent[] allEldEvents = getMandateController().GetAllEventsByEmployeeLogKey(_parentEmployeeLogKey);
                EmployeeLogEldEventList eventList = new EmployeeLogEldEventList();
                eventList.setEldEventList(allEldEvents);
                GlobalState.getInstance().getCurrentEmployeeLog().setEldEventList(eventList);
            }

            runOnUiThread( new Runnable() {
                public void run() {
                    updateUI();
                }});
        } catch (Throwable ex) {
            // show Error message
            ErrorLogHelper.RecordException(ex);
            message = ex.getMessage();
        }

        // show result in Heads-Up Display
        final String hudMessage = message;
        runOnUiThread( new Runnable() {
            public void run() {
                showHUDMessage(hudMessage, isAdding);
            }});

        return true;
    }

    /**
     * Called from BaseActivity SaveLocalDataTask::onPostExecute
     */
    @Override
    protected void Return(boolean success) {
        if (success) {
            updateFragmentUI();
        }
    }

    /**
     * Shows messages in the Heads-Up Display.
     */
    private void showHUDMessage(String message, boolean isAdding) {
        if (message.length() == 0) {
            _txtError.setVisibility(View.GONE);

            if (isAdding)
                _txtSuccess.setText(getString(R.string.eldeventaddsuccess));
            else
                _txtSuccess.setText(getString(R.string.eldeventeditsuccess));
            _txtSuccess.setVisibility(View.VISIBLE);
        }
        else {
            _txtSuccess.setVisibility(View.GONE);
            _txtError.setText(Html.fromHtml(message));
            _txtError.setVisibility(View.VISIBLE);
        }

        _linearHUD.setVisibility(View.VISIBLE);

        clearFocusAndDismissSIP();

        _hudMessage = message;
        _hudIsAdding = isAdding;
        _hudIsVisible = true;
    }

    /**
     * Clear focus from active control and dismiss the keyboard if showing.
     */
    private void clearFocusAndDismissSIP() {
        //Clear focus from all controls
        View current = getCurrentFocus();
        if (current != null) {
            current.clearFocus();

            // hide keyboard
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(current.getWindowToken(), 0);
        }
    }

    private Date getDateFromString(String dateTimeString) {
        try {
            return DateUtility.getHomeTerminalReferenceTimestampFormat().parse(dateTimeString);
        } catch (ParseException e) {

            Log.e("UnhandledCatch", e.getMessage() + ": " + Log.getStackTraceString(e));
        }

        return  null;
    }


    private List<TeamDriver> fetchTeamDriverData(){
        // Get team drivers for the log.
        LogTeamDriverPersist<TeamDriver> teamDriverPersist = new LogTeamDriverPersist<TeamDriver>(TeamDriver.class, this.getBaseContext(), GlobalState.getInstance().getCurrentUser(), _parentEmployeeLogKey);

        return teamDriverPersist.FetchList();
    }

    /**
     * At least one team driver exists on the current log and the start/end times of the team driver cover the start/end times of the driving event
     */
    private boolean isDrivingEventReassignable(){

        if (_eldEventList.get(_currentIndex).first.getPrimaryKey() == -1)
            return false;  // adding new record – can’t be automatic driving then

        if (!isAutomaticallyGeneratedDrivingEvent())
            return false;

        EmployeeLogEldEvent editedEldEvent = _eldEventList.get(_currentIndex).first;

        Date editedEventStartTime = editedEldEvent.getEventDateTime();
        Date editedEventImpliedEndTime = getEditedEventImpliedEndTime();

        // the user should ensure they are in a non-driving status before attempting to reassign
        if (editedEventImpliedEndTime == null || editedEventImpliedEndTime.after(DateUtility.getCurrentDateTimeUTC()))
            return false;

        for (TeamDriver teamDriver : _teamDriverList) {
            Date teamStartTime = teamDriver.getStartTime();
            Date teamEndTime = teamDriver.getEndTime();

            if (teamStartTime == null)
                continue;

            // start/end times of the team driver cover the start/end times of the driving event
            if (teamStartTime != null && teamEndTime != null) {
                if (teamStartTime.compareTo(editedEventStartTime) <= 0 && (teamEndTime.compareTo(editedEventImpliedEndTime) >= 0)) //account for dates exactly matching
                    return true;
            }
            else if (teamStartTime != null && teamEndTime == null){
                if (teamStartTime.compareTo(editedEventStartTime) <= 0) //team driver still logged in
                    return true;
            }
        }

        return false;
    }

    private Date getEditedEventImpliedEndTime() {

        // This Event's End Time is implied to be the Start Time of the next Event. If this is the last Event for the day, assume end-of-day as End Time.
        // The End Time won't be persisted to the database anywhere but is helpful for the user to see and will be used during Save to determine which records will become Inactivated.
        Date impliedEndTime = null;
        EmployeeLogEldEvent model = _eldEventList.get(_currentIndex).first;

        if (model.getPrimaryKey() > -1) {
            if (_currentIndex < _eldEventList.size() - 1) {
                impliedEndTime = _eldEventList.get(_currentIndex + 1).first.getEventDateTime();
                _isEndTimeTodaysCurrentTime = false;
            } else {

                // if Eld Event is the current Event for Now - then default to the current Time()
                if (_isCurrentDay) {
                    _isEndTimeTodaysCurrentTime = true;

                    String date = DateUtility.getHomeTerminalDateFormat().format(model.getEventDateTime());
                    String time = DateUtility.getHomeTerminalTime12HourFormatWithSeconds().format(DateUtility.CurrentHomeTerminalTime(getMandateController().getCurrentUser()).getTime());

                    try {
                        impliedEndTime = DateUtility.getHomeTerminalTime12HourFormatWithSeconds().parse(date + " " + time);
                    } catch (ParseException e) {

                        Log.e("UnhandledCatch", e.getMessage() + ": " + Log.getStackTraceString(e));
                    }
                } else {
                    _isEndTimeTodaysCurrentTime = false;
                    // determine end of day for the company
                    TimeZoneEnum timeZoneEnum = GlobalState.getInstance().getCurrentUser().getHomeTerminalTimeZone();
                    String dailyLogStart = GlobalState.getInstance().getCompanyConfigSettings(GlobalState.getInstance().getEobrService()).getDailyLogStartTime();
                    impliedEndTime = EmployeeLogUtilities.CalculateLogEndTime(dailyLogStart, model.getEventDateTime(), timeZoneEnum);
                }
            }
        }

        return impliedEndTime;
    }

    private EmployeeLogEldEvent[] pairListToArray(List<Pair<EmployeeLogEldEvent, Enums.SpecialDrivingCategory>> list){
        EmployeeLogEldEvent[] array = new EmployeeLogEldEvent[list.size()];

        for (int i = 0; i < list.size(); i++){
            array[i] = list.get(i).first;
        }

        return array;
    }

    public boolean isAutomaticallyGeneratedDrivingEvent() {
        EmployeeLogEldEvent editedEldEvent = _eldEventList.get(_currentIndex).first;
        if (editedEldEvent != null && (editedEldEvent.getEventRecordOrigin() == Enums.EmployeeLogEldEventRecordOrigin.AutomaticallyRecorded && editedEldEvent.getEventCode() == EmployeeLogEldEventCode.DutyStatus_Driving)) {
            return true;
        } else {
            return false;
        }
    }

}
