package com.jjkeller.kmb;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.jjkeller.kmb.fragments.LeftNavFrag;
import com.jjkeller.kmb.fragments.SubmitLogsViewOnlyFrag;
import com.jjkeller.kmb.interfaces.ISubmitLogsViewOnly.SubmitLogsViewOnlyFragActions;
import com.jjkeller.kmb.interfaces.ISubmitLogsViewOnly.SubmitLogsViewOnlyFragControllerMethods;
import com.jjkeller.kmb.share.OffDutyBaseActivity;
import com.jjkeller.kmb.share.ViewOnlyModeNavHandler;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.abstracts.APIControllerBase;
import com.jjkeller.kmbapi.controller.interfaces.IAPIController;
import com.jjkeller.kmbapi.controller.share.KmbApplicationException;
import com.jjkeller.kmbapi.controller.share.MandateObjectFactory;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbui.R;

import java.util.Date;
import java.util.List;

public class SubmitLogsViewOnly  extends OffDutyBaseActivity
        implements LeftNavFrag.OnNavItemSelectedListener, LeftNavFrag.ActivityMenuItemsListener, SubmitLogsViewOnlyFragActions, SubmitLogsViewOnlyFragControllerMethods {

    SubmitLogsViewOnlyFrag _submitLogsViewOnlyFrag;

    private ViewOnlyModeNavHandler _viewOnlyHandler;
    private int _myIndex;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SupressOffDutyCounter(false);

        _viewOnlyHandler = new ViewOnlyModeNavHandler(this);
        _viewOnlyHandler.setCurrentActivity(ViewOnlyModeNavHandler.ViewOnlyModeActivity.SUBMITLOGS);

        _myIndex = _viewOnlyHandler.getCurrentActivity().index();

        // Used for handling highlighting the selected item in the leftnav
        // If not using multiple fragments within an activity, we have to manually set the selected item
        this.setLeftNavSelectedItem(_myIndex);
        this.setLeftNavAllowChange(true);

        setContentView(R.layout.submitlogsviewonly);

        mFetchLocalDataTask = new FetchLocalDataTask(this.getClass().getSimpleName());
        mFetchLocalDataTask.execute();

        if (savedInstanceState == null) {
            _submitLogsViewOnlyFrag = new SubmitLogsViewOnlyFrag();
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.content_fragment, _submitLogsViewOnlyFrag, "Content")
                    .commit();
        } else {
            _submitLogsViewOnlyFrag = (SubmitLogsViewOnlyFrag) getSupportFragmentManager()
                    .findFragmentByTag("Content");
        }
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

    public IAPIController getMyController()
    {
        return (IAPIController) this.getController();
    }

    @Override
    protected void InitController() {

        this.setController(MandateObjectFactory.getInstance(this, GlobalState.getInstance().getFeatureService()).getCurrentEventController());
    }

    public String getActivityMenuItemList()
    {
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

    public void handleSubmitButtonClick()
    {
        _submitLogsViewOnlyFrag.getSubmitButton().setEnabled(false);

        // pass list of log dates into SubmitLogsViewOnlyTask?
        SubmitLogsViewOnlyTask _submitLogsTask = new SubmitLogsViewOnlyTask();
        _submitLogsTask.execute(new Void[0]);
    }

    public void handleOkButtonClick()
    {
        _submitLogsViewOnlyFrag.getSubmitButton().setEnabled(true);
    }

    @Override
    protected void Return()
    {
        this.finish();
		/* Display login activity */
        this.startActivity(Login.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);

    }

    private AlertDialog CreateConfirmationMessage(Context ctx, String msg)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setMessage(msg)
                .setCancelable(false)
                .setPositiveButton(R.string.button_try_again, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        handleSubmitButtonClick();
                    }
                })
                .setNegativeButton(R.string.btnok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        handleOkButtonClick();
                    }
                });
        AlertDialog alert = builder.create();
        return alert;
    }

    private AlertDialog displayAlert(Context context, String title, String message)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message)
                .setCancelable(false)
                .setTitle(title)
                .setPositiveButton(getString(R.string.btnok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // removed submitted logs from the grid
                        _submitLogsViewOnlyFrag.removeSelectedLogsFromGrid();
                    }
                });
        AlertDialog alert = builder.create();
        return alert;
    }

    private void createCertificationEvents(User user, List<Date> datesToCertify)
    {
        IAPIController empLogController = MandateObjectFactory.getInstance(this, GlobalState.getInstance().getFeatureService()).getCurrentEventController();
        for (Date date : datesToCertify)
        {
            EmployeeLog log = getMyController().GetEmployeeLog(date);
            try
            {
                empLogController.CertifyEmployeeLog(log);
            }
            catch (Throwable e)
            {
                Log.e("UnhandledCatch", "Failed to log a certification event", e);
            }
        }
    }

    /// <summary>
    /// Attempt to submit the selected EmployeeLogs to DMO.
    /// If the network is not available, then display a message.
    /// Answer if everything was submitted successfully.
    /// </summary>
    /// <returns></returns>
    private boolean PerformSubmitLogs(User submittingUser, List<Date> datesToSubmit)
    {
        boolean isSuccessful = false;
        try
        {
            if (((APIControllerBase)this.getMyController()).getIsWebServicesAvailable())
            {
                createCertificationEvents(submittingUser, datesToSubmit);
                isSuccessful = this.getMyController().SubmitUsersLocalLogs(submittingUser, datesToSubmit);
                this.getMyController().DownloadRecordsForCompliance(submittingUser, true);
            }
            else
            {
                Toast.makeText(this, R.string.msgnetworknotavailable, Toast.LENGTH_SHORT).show();
            }
        }
        catch (Exception ex)
        {

            Log.e("UnhandledCatch", ex.getMessage() + ": " + Log.getStackTraceString(ex));
        }
        return isSuccessful;
    }

    private class SubmitLogsViewOnlyTask extends AsyncTask<Void, String, Boolean> {
        ProgressDialog pd;
        Exception ex;

        protected void onPreExecute()
        {
            LockScreenRotation();
            if(!SubmitLogsViewOnly.this.isFinishing())
                pd = ProgressDialog.show(SubmitLogsViewOnly.this, "", getString(R.string.msgcontacting));
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean isSubmitted = false;

            // submit all the logs
            isSubmitted = PerformSubmitLogs(((APIControllerBase)getMyController()).getCurrentUser(), _submitLogsViewOnlyFrag.getLogDatesToSubmitList());
            if (isSubmitted) {
                // successfully submitted, so we're done
                publishProgress(getString(R.string.msgsuccessfullycompleted));
            } else {
                // some problem with submitting the logs
                publishProgress(getString(R.string.msgerrorsoccured));
            }

            return isSubmitted;
        }

        @Override
        protected void onProgressUpdate(String... values) {

        }

        @Override
        protected void onPostExecute(Boolean result) {
            if(pd != null && pd.isShowing()) pd.dismiss();
            if(ex != null)
            {
                if (ex.getClass() == KmbApplicationException.class)
                    HandleException((KmbApplicationException)ex);
            }
            if(!result)
            {
                if(!SubmitLogsViewOnly.this.isFinishing())
                {
                    AlertDialog submitRetryDialog = CreateConfirmationMessage(SubmitLogsViewOnly.this, getString(R.string.msgerroroccuredsubmitagain));
                    submitRetryDialog.show();
                }
            }
            else
            {
                // display message
                AlertDialog successDialog = displayAlert(SubmitLogsViewOnly.this, getString(R.string.msg_success), getString(R.string.msg_success_logs_submitted));
                successDialog.show();
            }

            UnlockScreenRotation();
        }

    }
}
