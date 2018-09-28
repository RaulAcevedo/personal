package com.jjkeller.kmb;

import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.actionbarsherlock.view.Menu;
import com.jjkeller.kmb.fragments.EditLogRequestFrag;
import com.jjkeller.kmb.fragments.LeftNavFrag;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmbapi.calcengine.Enums;
import com.jjkeller.kmbapi.common.JsonUtil;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.interfaces.IAPIController;
import com.jjkeller.kmbapi.controller.share.MandateObjectFactory;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.eldmandate.EventDataDiagnosticsChecker;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEventList;
import com.jjkeller.kmbui.R;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class EditLogRequest extends BaseActivity implements LeftNavFrag.OnNavItemSelectedListener, LeftNavFrag.ActivityMenuItemsListener {
    public static final String EXTRA_EMPLOYEELOGKEY = "employeeLogKey";
    public static final String EXTRA_EMPLOYEELOGDATE = "employeeLogDate";

    public enum SaveActionEnum {
        ACCEPT,
        REJECT
    }

    private int _employeeLogKey = 0;
    private Date _employeeLogDate = null;
    private SaveActionEnum _saveAction;

    private List<EmployeeLogEldEvent> _logEvents = null;
    private List<EmployeeLogEldEvent> _incompleteEvents;

    private EditLogRequestFrag _contentFrag;
    private IAPIController _controllerEmp = MandateObjectFactory.getInstance(this, GlobalState.getInstance().getFeatureService()).getCurrentEventController();

    private boolean _isShowingLeftNavDisplayActions = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            // screen rotation
            _isShowingLeftNavDisplayActions = savedInstanceState.getBoolean("_isShowingLeftNavDisplayActions");
        }
//        else {
//            Bundle bundle = getIntent().getExtras();
//            _someValue = bundle.getInt("someValue");
//        }

        setContentView(R.layout.baselayout);
        loadContentFragment(new EditLogRequestFrag());

        if (getIntent() != null) {
            Bundle extras = getIntent().getExtras();
            if (extras!=null){
                _employeeLogKey = extras.getInt(EXTRA_EMPLOYEELOGKEY);
                _employeeLogDate = DateUtility.getDateFromString(extras.getString(EXTRA_EMPLOYEELOGDATE));
            }
            else{
                _employeeLogKey = GlobalState.getInstance().getReviewEldEventLogKey();
                _employeeLogDate = GlobalState.getInstance().getReviewEldEventDate();
            }



        }

        mFetchLocalDataTask = new FetchLocalDataTask(this.getClass().getSimpleName());
        mFetchLocalDataTask.execute();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // saving state of the leftNav frag
        outState.putBoolean("_isShowingLeftNavDisplayActions", _isShowingLeftNavDisplayActions);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void InitController() {
        this.setController(_controllerEmp);
    }

    @Override
    public void setFragments() {
        super.setFragments();

        Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
        _contentFrag = (EditLogRequestFrag)f;
    }

    /* Called from BaseActivity FetchLocalDataTask so data is loaded on AsyncTask */
    @Override
    protected void loadData() {
        super.loadData();
        _logEvents = _controllerEmp.getReconcileChangeRequestedEldEvents(_employeeLogKey, Enums.ReconcileChangeRequestedEldEventsEnum.ACCEPT_PREVIEW);
    }

    /* Called from BaseActivity FetchLocalDataTask::onPostExecute */
    @Override
    protected void loadControls() {
        super.loadControls();
        _contentFrag.loadControls(_employeeLogDate, _logEvents);

        super.loadLeftNavFragment();
        super.BuildLeftNavMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        this.CreateOptionsMenu(menu, false);

        // we want the left sidebar links but not the upper right Menu - we want the dialog to behave like a modal dialog so a user can't link to another workflow
        return false;
    }

    public String getActivityMenuItemList() {
        StringBuilder sb = new StringBuilder();

        if (_isShowingLeftNavDisplayActions) {
            if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                sb.append(getString(R.string.btnViewOriginalLog_landscape));
                sb.append(",");
            }
            else {
                sb.append(getString(R.string.btnViewOriginalLog_portrait));
                sb.append(",");
            }
            sb.append(getString(R.string.btnAcceptEdit));
            sb.append(",");
            sb.append(getString(R.string.btnRejectEdit));
            sb.append(",");
            sb.append(getString(R.string.btndone));
            sb.append(",");
        }
        else {
            sb.append(getString(R.string.btndone));
            sb.append(",");
        }

        return sb.toString();
    }

    public void onNavItemSelected(int itemPosition) {
        switch (itemPosition)
        {
            case 0: {
                if (_isShowingLeftNavDisplayActions) {
                    GlobalState.getInstance().setReviewEldEventDate(_employeeLogDate);
                    GlobalState.getInstance().setIsReviewEldEvent(true);
                    startActivity(RptGridImage.class);
                    finish();
                }
                else{
                    finish();
                }
                break;
            }
            case 1:
                confirmAcceptOfEdits();
                break;
            case 2:
                confirmRejectOfEdits();
                break;
            case 3:
                _recentlyStartedActivityUri = null; //This prevents an error which would stop navigation to this same activity for the same event on a second selection of "View" (on older Android Versions)
                finish();
                break;
        }
    }

    private void confirmAcceptOfEdits() {
        runOnUiThread(new Runnable() {
            public void run() {
                _contentFrag.hideHUD();
                ShowConfirmationMessage(EditLogRequest.this,        //BaseActivity.this,
                        R.string.lblreviewlogedits,     //title
                        getString(R.string.lblConfirmEditAccepts),   //message
                        R.string.accept,
                        new ShowMessageClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                super.onClick(dialog, id);

                                _saveAction = SaveActionEnum.ACCEPT;

                                // kickoff background thread to save data
                                mSaveLocalDataTask = new SaveLocalDataTask(this.getClass().getSimpleName(), getString(R.string.msgsaving));
                                mSaveLocalDataTask.execute();
                                _contentFrag.showHUDMessageSuccess(getString(R.string.lblAcceptedEditChanges));
                            }
                        },
                        R.string.btncancel,
                        new ShowMessageClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                super.onClick(dialog, id);
                                // do nothing
                            }
                        }
                );
            }
        });
    }

    private void confirmRejectOfEdits() {
        runOnUiThread(new Runnable() {
            public void run() {
                _contentFrag.hideHUD();
                ShowConfirmationMessage(EditLogRequest.this,        //BaseActivity.this,
                        R.string.lblreviewlogedits,     //title
                        getString(R.string.lblConfirmEditRejects),   //message
                        R.string.reject,
                        new ShowMessageClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                super.onClick(dialog, id);

                                _saveAction = SaveActionEnum.REJECT;

                                // kickoff background thread to save data
                                mSaveLocalDataTask = new SaveLocalDataTask(this.getClass().getSimpleName(), getString(R.string.msgsaving));
                                mSaveLocalDataTask.execute();
                            }
                        },
                        R.string.btncancel,
                        new ShowMessageClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                super.onClick(dialog, id);
                                // do nothing
                            }
                        }
                );
            }
        });
    }

    /**
     * SaveLocalDataTask doInBackground call to save data.
     */
    @Override
    protected boolean saveData() {
        // get a list of events that need to be reviewed
        _incompleteEvents = new LinkedList<>();

        // get a list of Change Requested records
        List<EmployeeLogEldEvent> changeRequests = null;

        if (_saveAction == SaveActionEnum.ACCEPT) {
            // get a list of Change Requested records where EventDutyStatus has been changed from 3 - Change Requested to 1 - Active
            changeRequests = _controllerEmp.getReconcileChangeRequestedEldEvents(_employeeLogKey, Enums.ReconcileChangeRequestedEldEventsEnum.ACCEPT_DATABASE);
        }
        else {
            // get a list of Change Requested records where EventDutyStatus has been changed from 3 - Change Requested to 4 - Inactive - Change Rejected
            changeRequests = _controllerEmp.getReconcileChangeRequestedEldEvents(_employeeLogKey, Enums.ReconcileChangeRequestedEldEventsEnum.REJECT_DATABASE);
        }

        // convert List<> to array []
        EmployeeLogEldEvent[] arrayChangeRequests = new EmployeeLogEldEvent[changeRequests.size()];
        changeRequests.toArray(arrayChangeRequests);

        // save multiple records at once so they are wrapped in the same transaction. All will succeed or fail together.
        _controllerEmp.SaveListInSingleTransaction(arrayChangeRequests);


        // reload data to remove Change Requests (red highlighting) to reflect current state
        _logEvents = _controllerEmp.getReconcileChangeRequestedEldEvents(_employeeLogKey, Enums.ReconcileChangeRequestedEldEventsEnum.ACCEPT_PREVIEW);

        // find the events with missing data for this day's log
        if (_saveAction == SaveActionEnum.ACCEPT) {
            for(EmployeeLogEldEvent e : _logEvents){
                if(new EventDataDiagnosticsChecker().new DutyStatusChangeChecker().isDriversLocationDescriptionMissing(e)) {
                    _incompleteEvents.add(e);
                }
            }
        }

        // If editing Server Log (LogSourceStatusEnum=3) - we need to convert it to a local log (LogSourceStatusEnum=1)
        // because only local versions will be submitted back to Encompass
        IAPIController mandateController = MandateObjectFactory.getInstance(this,GlobalState.getInstance().getFeatureService()).getCurrentEventController();
        EmployeeLog empLog = mandateController.GetLocalEmployeeLog(GlobalState.getInstance().getCurrentUser(), _employeeLogDate);
        if (empLog == null) {
            empLog = mandateController.GetEmployeeLog(GlobalState.getInstance().getCurrentUser(), _employeeLogDate);
            if (empLog != null) {

                // Accept will cause the IsCertified flag to be reset to False.
                if (_saveAction == SaveActionEnum.ACCEPT)
                    empLog.setIsCertified(false);

                mandateController.SaveLocalEmployeeLog(empLog);
            }
        }

        // the user is accepting or rejecting the current day's log, we also need to update the in-memory events on the log so they
        // are not purged when the log is saved.
        if (DateUtility.IsToday(empLog.getLogDate(), GlobalState.getInstance().getCurrentUser())) {
            EmployeeLogEldEvent[] allEldEventsArray = mandateController.GetAllEventsByEmployeeLogKey(empLog.getPrimaryKey());
            EmployeeLogEldEventList eldEventList = new EmployeeLogEldEventList();
            eldEventList.setEldEventList(allEldEventsArray);
            GlobalState.getInstance().getCurrentEmployeeLog().setEldEventList(eldEventList);
        }
        return true;
    }

    /**
     * SaveLocalDataTask onPostExecute to run on UI thread.
     */
    @Override
    protected void Return(boolean success) {
        if (success) {

            // if there are new accepted edits that contain missing data, pass the events to the Incomplete Events screen for user input
            if(_incompleteEvents.size() > 0){

                String eventsJson = JsonUtil.getGson().toJson(_incompleteEvents);
                Bundle bundle = new Bundle();
                bundle.putString(UnidentifiedELDEventsReview.BUNDLE_EVENTS_TO_REVIEW, eventsJson);
                bundle.putString(UnidentifiedELDEventsReview.BUNDLE_REVIEW_HEADER, getString(R.string.accepted_edits_eld_events_review_header));
                bundle.putString(UnidentifiedELDEventsReview.BUNDLE_REVIEW_INSTRUCTIONS, getString(R.string.accepted_edits_eld_events_review_instructions));
                bundle.putString(UnidentifiedELDEventsReview.BUNDLE_FROM_SCREEN, "EDITLOG");

                startActivity(UnidentifiedELDEventsReview.class, bundle);
            }

            // reload data to remove Change Requests (red highlighting) to reflect current state
            _contentFrag.loadControls(_employeeLogDate, _logEvents);
            
            // swap "Accept, Reject" menu items for "Done"
            _isShowingLeftNavDisplayActions = false;
            BuildLeftNavMenu();

            if (_saveAction == SaveActionEnum.ACCEPT) {

            }
            else {
                _contentFrag.showHUDMessageError(getString(R.string.lblRejectedEditChanges));
            }

        }
    }

}