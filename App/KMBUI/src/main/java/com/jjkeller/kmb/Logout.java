package com.jjkeller.kmb;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.actionbarsherlock.view.Menu;
import com.jjkeller.kmb.fragments.LogoutFrag;
import com.jjkeller.kmb.share.LogoutBaseActivity;
import com.jjkeller.kmb.share.MobileDeviceHandler;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.ControllerFactory;
import com.jjkeller.kmbapi.controller.EOBR.EobrReader;
import com.jjkeller.kmbapi.controller.UnassignedPeriodController;
import com.jjkeller.kmbapi.controller.UnclaimedDrivingPeriod;
import com.jjkeller.kmbapi.controller.interfaces.IAPIController;
import com.jjkeller.kmbapi.controller.share.MandateObjectFactory;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.kmbeobr.UnidentifiedPairedEvents;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbui.R;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import static com.jjkeller.kmbapi.kmbeobr.Constants.FROM_MENU_EXTRA;

public class Logout extends LogoutBaseActivity {

    private static final String STATE_EXEMPT_FROM_ELD_MESSAGE_SHOWN_KEY = "exempt_from_eld_message_shown";
    private MobileDeviceHandler _mobileDevice;
    private LogoutFrag _contentFrag;
    private boolean _loading;
    private boolean _hasShownExemptFromELDMessage;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.baselayout);

        _mobileDevice = new MobileDeviceHandler(this);

        _loading = true;

        _hasShownExemptFromELDMessage = hasShownExemptFromELDMessage(savedInstanceState);

        loadContentFragment(new LogoutFrag());
        setFragments();

        // Since this screen doesn't perform a FetchData task, handle the app
        // process being killed while on this screen
        if (this.getMyController().getCurrentUser() == null)
        {
            this.finish();

            Intent loginIntent = new Intent(this, com.jjkeller.kmb.Login.class);
            loginIntent.putExtra(this.getString(R.string.restartapp), true);
            this.startActivity(loginIntent);
        }
        else
        {
            // If there are any unsubmitted unidentified/unassigned events, check whether the user would like to review them before logoff.
            boolean isMandate = GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled();
            if (isMandate && hasUnidentifiedEmployeeEventsToClaim()) {
                this.reviewOfUnidentifiedEventsAtLogout();
            } else if (!isMandate && hasUnassignedEmployeeEventsToClaim()) {
                this.reviewOfUnassignedEventsAtLogout();
            }

            if (this.getIsExemptFromELDUse() && !_hasShownExemptFromELDMessage) {
                showExemptFromELDMessage();
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        this.CreateOptionsMenu(menu, false);
        return true;
    }

    @Override
    public void setFragments()
    {
        super.setFragments();

        Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
        _contentFrag = (LogoutFrag)f;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_EXEMPT_FROM_ELD_MESSAGE_SHOWN_KEY, _hasShownExemptFromELDMessage);
    }

    private boolean hasShownExemptFromELDMessage(Bundle savedInstanceState) {
        return savedInstanceState != null && savedInstanceState.getBoolean(STATE_EXEMPT_FROM_ELD_MESSAGE_SHOWN_KEY);
    }

    private void showExemptFromELDMessage() {
        this.ShowMessage(this, getString(R.string.exempt_from_eld_dialog_title), getString(R.string.exempt_from_eld_dialog_message));
        _hasShownExemptFromELDMessage = true;
    }

    private boolean hasUnidentifiedEmployeeEventsToClaim() {
        IAPIController empCon = MandateObjectFactory.getInstance(this, GlobalState.getInstance().getFeatureService()).getCurrentEventController();
        List<UnidentifiedPairedEvents> logsThatNeedToBeReviewed = empCon.LoadUnidentifiedEldEventPairs(true);

        return !logsThatNeedToBeReviewed.isEmpty();
    }

    private boolean hasUnassignedEmployeeEventsToClaim() {
        UnassignedPeriodController upController = ControllerFactory.getInstance().getUnassignedPeriodController();
        List<UnclaimedDrivingPeriod> unclaimedPeriods = upController.GetUnclaimedDrivingPeriodsForCurrentLog();

        return unclaimedPeriods != null && !unclaimedPeriods.isEmpty();
    }

    private void reviewOfUnidentifiedEventsAtLogout() {
        // Unidentified Events exist, so prompt whether they should be reviewed now.

        //final Activity thisActivity = this;
        ShowConfirmationMessage(this, R.string.reviewunidentifiedeventstitle, getString(R.string.reviewunidentifiedeventsmessage),
                R.string.btnyes,
                new ShowMessageClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        super.onClick(dialog, id);
                        dialog.dismiss();
                        Bundle bundle = new Bundle();
                        bundle.putBoolean(getString(R.string.parm_unidentifiedeldeventsshowallunsubmitted), true);
                        bundle.putBoolean(getString(R.string.parm_returntologout), true);
                        startActivity(UnidentifiedELDEvents.class, bundle);
                    }
                },
                R.string.btnno,
                new ShowMessageClickListener()
        );
    }

    private void reviewOfUnassignedEventsAtLogout() {
        // Unassigned Events exist, so prompt whether they should be reviewed now.

        //final Activity thisActivity = this;
        ShowConfirmationMessage(this, R.string.reviewunassignedeventstitle, getString(R.string.reviewunassignedeventsmessage),
                R.string.btnyes,
                new ShowMessageClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        super.onClick(dialog, id);
                        dialog.dismiss();
                        Bundle bundle = new Bundle();
                        bundle.putBoolean(FROM_MENU_EXTRA, true);
                        bundle.putBoolean(getString(R.string.parm_returntologout), true);
                        startActivity(UnassignedDrivingPeriods.class, bundle);
                    }
                },
                R.string.btnno,
                new ShowMessageClickListener()
        );
    }

    public void handleLogoutButtonClick()
    {
        if (EobrReader.getIsEobrDeviceReadingHistory())
        {
            Toast.makeText(this, getString(R.string.logout_readinghistory), Toast.LENGTH_LONG).show();
            handleCancelButtonClick();
        }
        else
        {
            if (isEldMandateEnabled()) {
                if (_contentFrag.getLocationEditText().getVisibility() == View.VISIBLE && _contentFrag.getLocationText().length() == 0) {
                    _contentFrag.showLocationErrorMessage();
                    return;
                } else {
                    _contentFrag.hideErrorMessage();
                }
                performSaveDutyStatusSubStatuses();
                promptForReviewOrCertifyIfNeeded();
            } else {
                if(_contentFrag.GetSubmitLogsCheckBoxValue()) {
                    submitLogsDuringLogOut();
                } else {
                    logoutWithoutSubmittingLogs();
                }
            }
        }
    }

    private void performSaveDutyStatusSubStatuses() {
        if (IsTheDriver(GlobalState.getInstance().getCurrentUser())) {
            boolean isInPCBefore = GlobalState.getInstance().getIsInPersonalConveyanceDutyStatus();
            boolean isInYMBefore = GlobalState.getInstance().getIsInYardMoveDutyStatus();
            if (isInPCBefore || isInYMBefore) {
                GlobalState.getInstance().setIsEndingActivePCYMWT_Status(true);
            }
        }
    }

    private void promptForReviewOrCertifyIfNeeded() {
        // goes to the View Log activity to review log edits if yes is clicked
        DialogInterface.OnClickListener onYesHandler = getReviewLogEditsClickHandler();

        // if we didn't re-direct to the Review Request Edit activity - then prompt for Certify Logs
        DialogInterface.OnClickListener onNoHandler = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                checkForCertifyLogsDialogNeeded();
            }
        };

        // Prompt user to go to "Review Edit Requests" activity
        if (currentUserHasPendingLogEditsToReview())
            this.DisplayReviewLogEditsDialog(onYesHandler, onNoHandler);
        else
            onNoHandler.onClick(null, 0);
    }

    private void checkForCertifyLogsDialogNeeded(){
        // Does the user have logs that need to be certified?
        if (isEldMandateEnabled()) {
            DialogInterface.OnClickListener onYesHandler = new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            // Alex: Changed this because it wasn't logging out after certifying
                            submitLogsDuringLogOut();
                        }
                    });
                }
            };

            // if we didn't re-direct to the Review Request Edit activity - then prompt for Certify Logs
            DialogInterface.OnClickListener onNoHandler = new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    logoutWithoutSubmittingLogs();
                }
            };

            // Prompt user to go to "Certify Logs" activity
            if (!this.getIsExemptFromELDUse() && currentUserHasLogsNeedingCertification())
                this.DisplayCertifyLogsDialog(onYesHandler, onNoHandler);
            else
                onNoHandler.onClick(null, 0);
        } else {
            if (_contentFrag.GetSubmitLogsCheckBoxValue()) {
                submitLogsDuringLogOut();
            } else {
                logoutWithoutSubmittingLogs();
            }
        }
    }

    @Override
    public void submitLogsDuringLogOut()
    {
        // Before submitting and logout, Attempt to submit device information to DMO
        _mobileDevice.UploadMobileDeviceInfo();

        GlobalState.getInstance().setIsUserLoggingOut(true);

        if (isEldMandateEnabled()) {

            //We need to carry across the selected logoff time to the Certification Screen.
            _recentlyStartedActivityUri = null; //This prevents an error which would stop navigation to the Certify screen after it had been visited and the user backed out.
            Intent certification = new Intent(getApplicationContext(), CertifyLogs.class);
            certification.putExtra(getString(R.string.logoff_date), this._contentFrag.GetSelectedTimeOffset());
            certification.putExtra(getString(R.string.exact_logoff_time), this._contentFrag.GetExactTimeButton().getText().toString());
            certification.putExtra(this.getResources().getString(R.string.extra_selectedDutyStatus), _contentFrag.GetSelectedDutyStatus());
            certification.putExtra(this.getResources().getString(R.string.extra_location_text), _contentFrag.getLocationText());
            this.startActivity(certification);
        } else {
            if (mLogoutTask == null){
                // verify that everything is valid first, if not then don't continue
                if (!this.Validate()) {
                    return;
                }

                // first, log the user out
                // note: it's important to get the current user before that user
                // is logged out.  In a team driving situation, a different member
                // of the team will be the current user after Logout is complete.
                mSubmittingUser = this.getMyController().getCurrentUser();

                String selectedOffset = _contentFrag.GetSelectedTimeOffset();
                Date exactTime = null;

                if (selectedOffset.equals(this.getString(R.string.exacttime))) {
                    exactTime = CreateExactTime();
                }

                String latLonStatus = null;
                if(!this.getIsExemptFromELDUse()) {
                    latLonStatus = getIntent().getStringExtra(this.getResources().getString(R.string.extra_lat_lon_status));
                }

                mLogoutTask = new LogoutTask(true, selectedOffset, null, exactTime, _contentFrag.GetSelectedDutyStatus(), _contentFrag.getLocationText(), latLonStatus);
                mLogoutTask.execute();
            }
        }
    }

    private void logoutWithoutSubmittingLogs()
    {
        // verify that everything is valid first, if not then don't continue
        if (!this.Validate()) return;

        GlobalState.getInstance().setIsUserLoggingOut(true);

        String selectedOffset = _contentFrag.GetSelectedTimeOffset();
        Date exactTime = null;

        if (selectedOffset.equals(this.getString(R.string.exacttime))) {
            exactTime = CreateExactTime();
        }

        String latLonStatus = null;
        if(isEldMandateEnabled() && !this.getIsExemptFromELDUse()) {
            latLonStatus = getIntent().getStringExtra(this.getResources().getString(R.string.extra_lat_lon_status));
        }

        mLogoutTask = new LogoutTask(false, selectedOffset, null, exactTime, _contentFrag.GetSelectedDutyStatus(), _contentFrag.getLocationText(), latLonStatus);
        mLogoutTask.execute();
    }

    public void handleCancelButtonClick()
    {
        this.Return(false);
    }

    public void handleDutyStatusTimeSelect()
    {
        if (!_loading) {
            String selectedOffset = _contentFrag.GetSelectedTimeOffset();
            if (selectedOffset.equals(this.getString(R.string.exacttime)))
                _contentFrag.GetExactTimeButton().setEnabled(true);
            else
                _contentFrag.GetExactTimeButton().setEnabled(false);
        } else
            _loading = false;
    }

    // the submit button is only visible when the eld mandate is turned on
    public void handleSubmitButtonClick() {

        if (_contentFrag.getLocationEditText().getVisibility() == View.VISIBLE && _contentFrag.getLocationText().length() == 0) {
            _contentFrag.showLocationErrorMessage();
            return;
        } else {
            _contentFrag.hideErrorMessage();
        }

        performSaveDutyStatusSubStatuses();

        // goes to the View Log activity to review log edits if yes is clicked
        DialogInterface.OnClickListener onYesHandler = getReviewLogEditsClickHandler();

        // if we didn't re-direct to the Review Request Edit activity - then prompt for Certify Logs
        DialogInterface.OnClickListener onNoHandler = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                submitLogsDuringLogOut();
            }
        };

        // Prompt user to go to "Review Edit Requests" activity
        if (currentUserHasPendingLogEditsToReview())
            this.DisplayReviewLogEditsDialog(onYesHandler, onNoHandler);
        else
            onNoHandler.onClick(null, 0);
    }

    private boolean currentUserHasPendingLogEditsToReview() {
        IAPIController empCon = MandateObjectFactory.getInstance(this, GlobalState.getInstance().getFeatureService()).getCurrentEventController();
        List<EmployeeLog> logsThatNeedToBeReviewed = empCon.GetLogsWithUnreviewedEdits(getMyController().getCurrentUser());
        return !logsThatNeedToBeReviewed.isEmpty();
    }

    private boolean currentUserHasLogsNeedingCertification() {
        IAPIController empCon = MandateObjectFactory.getInstance(this,GlobalState.getInstance().getFeatureService()).getCurrentEventController();
        List<Date> datesThatNeedToBeCertified = empCon.GetUncertifiedLogDatesExceptToday(getMyController().getCurrentUser());
        return !datesThatNeedToBeCertified.isEmpty();
    }

    private DialogInterface.OnClickListener getReviewLogEditsClickHandler() {
        return new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        Bundle bundle = new Bundle();
                        bundle.putInt(ViewLog.EXTRA_INITIALFRAGINDEX, 7);
                        startActivity(ViewLog.class, bundle);
                    }
                });
            }
        };
    }

    @Override
    protected boolean Validate()
    {
        boolean isValid = true;

        String selectedOffset = _contentFrag.GetSelectedTimeOffset();

        if (selectedOffset.equals(this.getString(R.string.exacttime)))
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

    @Override
    protected Date CreateExactTime()
    {
        String currentDate = DateUtility.getHomeTerminalDateFormat().format(this.getController().getCurrentClockHomeTerminalTime());
        String strExactTime = _contentFrag.GetExactTimeButton().getText().toString();
        Date exactTime = null;
        try {
            exactTime = DateUtility.getHomeTerminalDateTimeFormat12Hour().parse(currentDate + " " + strExactTime);
        } catch (ParseException e) {
            Log.e("UnhandledCatch", e.getMessage() + ": " + Log.getStackTraceString(e));
        }
        return exactTime;
    }
}