package com.jjkeller.kmb;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.jjkeller.kmb.fragments.CertifyLogsFrag;
import com.jjkeller.kmb.interfaces.ICertifyLogs.CertifyLogsControllerMethods;
import com.jjkeller.kmb.interfaces.ICertifyLogs.CertifyLogsFragActions;
import com.jjkeller.kmb.share.LogoutBaseActivity;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.interfaces.IAPIController;
import com.jjkeller.kmbapi.controller.share.MandateObjectFactory;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.enums.DutyStatusEnum;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbui.R;

import java.text.ParseException;
import java.util.Date;
import java.util.List;


public class CertifyLogs extends LogoutBaseActivity implements CertifyLogsFragActions, CertifyLogsControllerMethods {

    private CertifyLogsFrag _contentFrag;
    private IAPIController _employeeLogController = MandateObjectFactory.getInstance(this,GlobalState.getInstance().getFeatureService()).getCurrentEventController();
    protected boolean _calledFromMenu = false;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.baselayout);

        this._dataTransferWorkflow = this.getIntent().getBooleanExtra(getString(R.string.data_transfer_workflow),false);
        _calledFromMenu = this.getIntent().getBooleanExtra(this.getString(R.string.logout),false);


        loadContentFragment(new CertifyLogsFrag());
        setFragments();
    }

    @Override
    public void setFragments()
    {
        super.setFragments();

        super.loadLeftNavFragment();

        Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
        _contentFrag = (CertifyLogsFrag)f;
        if (_contentFrag!=null && _calledFromMenu){
            _contentFrag.getMessageTextView().setText(R.string.certifylogs_alternate_text);
            _contentFrag.getTitleTextView().setText(R.string.certifylogs_alternate_title);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        return true;
    }

    public String getActivityMenuItemList() {
        return "Cancel";
    }

    public void onNavItemSelected(int itemPosition) {
        int menuItemIndex = -1;
        String itemText = this.getLeftNavFragment().GetNavItemText(itemPosition);

        if (itemText.equalsIgnoreCase("Cancel")) {
            menuItemIndex = 0;
        }

        handleNavItem(menuItemIndex);
    }

    private void handleNavItem(int itemPosition) {
        if (itemPosition == 0) {
            // Cancel
            this.handleCancelButtonClick();
        }
    }

    public ShowMessageClickListener getCertifyAgreeClickListener()
    {
        return new ShowMessageClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // disable certify button
                _contentFrag.getCertifyButton().setEnabled(false);



                if (GlobalState.getInstance().getIsUserLoggingOut()) {
                    // startLogout will also submit the logs, and then exit the app
                    startLogout(true);
                } else {
                    List<Date> selectedLogDates = _contentFrag.getSelectedLogDates();
                    CreateCertificationEvents(selectedLogDates);
                    // the user has certified logs, and therefor needs to upload them to encompass
                    mSubmittingUser = getMyController().getCurrentUser();
                    new SubmitLogsTask(false).execute();
                }
                // enable certify button
                _contentFrag.getCertifyButton().setEnabled(true);
                // Call super.onClick to release screen orientation lock
                super.onClick(dialog, id);
            }
        };
    }

    public ShowMessageClickListener getCertifyCancelListener()
    {
        return new ShowMessageClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                GlobalState.getInstance().setIsUserLoggingOut(false);
                // Call super.onClick to release screen orientation lock
                super.onClick(dialog, id);
            }
        };
    }

    public void handleSubmitButtonClick()
    {
        if (_contentFrag.getSelectedLogDates().isEmpty()) {
            if (GlobalState.getInstance().getIsUserLoggingOut()) {
                // the user does not want to certify any logs, just submit the logs
                startLogout(true);
            } else {
                // just submit the logs
                mSubmittingUser = getMyController().getCurrentUser();
                new SubmitLogsTask(false).execute();
            }
        } else {
            this.ShowConfirmationMessage(this, R.string.lblcertifymessagetitle, getString(R.string.lblcertificationacknowledge), R.string.btncertifyagree, getCertifyAgreeClickListener(), R.string.btnCertifyNotReady, getCertifyCancelListener());
        }
    }

    public void handleLogoutButtonClick()
    {
        // not implemented
    }

    public void handleCancelButtonClick()
    {
        this.Return();
    }

    public void handleDutyStatusTimeSelect()
    {
        // not implemented
    }

    public List<Date> getUncertifiedLogDates()
    {
        if (GlobalState.getInstance().getIsUserLoggingOut() || _calledFromMenu || _dataTransferWorkflow) {
            return _employeeLogController.GetUncertifiedLogDates();
        } else {
            return _employeeLogController.GetUncertifiedLogDatesExceptToday(getMyController().getCurrentUser());
        }
    }

    public List<Date> getCertifiedUnsubmittedLogDates()
    {
        return _employeeLogController.GetCertifiedUnsubmittedLogDates();
    }

    private void startLogout(boolean shouldSubmitLogs)
    {
        if (mLogoutTask == null || mLogoutTask.getStatus() == Status.FINISHED)
        {
            // verify that everything is valid first, if not then don't continue
            if (!Validate())
            {
                _contentFrag.getCertifyButton().setEnabled(true);
                return;
            }

            GlobalState.getInstance().setIsUserLoggingOut(true);

            // first, log the user out
            // note: it's important to get the current user before that user
            // is logged out. In a team driving situation, a different member
            // of the team will be the current user after Logout is complete.
            mSubmittingUser = getMyController().getCurrentUser();

            List<Date> selectedLogDates = _contentFrag.getSelectedLogDates();

            String selectedOffset = getSelectedTimeOffset();
            Date exactTime = null;

            if (selectedOffset.equals(this.getString(R.string.exacttime))) {
                exactTime = CreateExactTime();
            }

            int selectedDutyStatus = this.getIntent().getIntExtra(getString(R.string.extra_selectedDutyStatus), DutyStatusEnum.NULL);
            String locationText = this.getIntent().getStringExtra(this.getResources().getString(R.string.extra_location_text));

            mLogoutTask = new LogoutTask(shouldSubmitLogs, selectedOffset, selectedLogDates, exactTime, selectedDutyStatus, locationText, "");
            mLogoutTask.execute();
        }
    }

    @Override
    protected boolean Validate()
    {
        boolean isValid = true;

        String selectedOffset = getSelectedTimeOffset();

        if (selectedOffset.equals(this.getString(R.string.exacttime)) )//&& dteOffDuty.Enabled)
        {
            Date exactTime = CreateExactTime();
            Date now = this.getMyController().getCurrentClockHomeTerminalTime();

            if (exactTime.compareTo(now) <= 0)
            {
                isValid = false;
                Toast msg = Toast.makeText(this, this.getString(R.string.msgtimemustbeinfuture), Toast.LENGTH_SHORT);
                msg.show();
            }
        }

        return isValid;
    }

    private String getSelectedTimeOffset()
    {
        Bundle extras = getIntent().getExtras();
        String selectedOffset = this.getString(R.string.exacttime); // Default to current time if logoff time was not carried to this Activity
        if (extras != null && extras.containsKey(getString(R.string.logoff_date)))
        {
            selectedOffset = extras.getString(getString(R.string.logoff_date));
        }
        return selectedOffset;
    }

    @Override
    protected Date CreateExactTime()
    {
        String currentDate = DateUtility.getHomeTerminalDateFormat().format(this.getController().getCurrentClockHomeTerminalTime());
        Bundle extras = getIntent().getExtras();
        String strExactTime = currentDate;
        if (extras != null && extras.containsKey(getString(R.string.exact_logoff_time)))
        {
            strExactTime = extras.getString(getString(R.string.exact_logoff_time));
        }

        Date exactTime = null;
        try {
            exactTime = DateUtility.getHomeTerminalDateTimeFormat12Hour().parse(currentDate + " " + strExactTime);
        } catch (ParseException e) {
            Log.e("UnhandledCatch", e.getMessage() + ": " + Log.getStackTraceString(e));
        }
        return exactTime;
    }

    @Override
    protected void Return() {
        this.finish();

        if (this._dataTransferWorkflow) {
            Bundle extras = new Bundle();
            extras.putBoolean(getString(R.string.data_transfer_workflow), true);
            startActivity(RoadsideInspectionDataTransfer.class, Intent.FLAG_ACTIVITY_CLEAR_TOP, extras);
        }
    }
}
