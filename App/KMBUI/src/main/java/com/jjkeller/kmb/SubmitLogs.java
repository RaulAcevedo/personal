package com.jjkeller.kmb;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.jjkeller.kmb.fragments.SubmitLogsFrag;
import com.jjkeller.kmb.interfaces.ISubmitLogs.SubmitLogsFragActions;
import com.jjkeller.kmb.interfaces.ISubmitLogs.SubmitLogsFragControllerMethods;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.abstracts.APIControllerBase;
import com.jjkeller.kmbapi.controller.interfaces.IAPIController;
import com.jjkeller.kmbapi.controller.share.KmbApplicationException;
import com.jjkeller.kmbapi.controller.share.MandateObjectFactory;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbui.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SubmitLogs extends BaseActivity 
							implements SubmitLogsFragActions, SubmitLogsFragControllerMethods{
	SubmitLogsFrag _contentFrag;
	SubmitLogsTask _submitLogsTask;
	AlertDialog submitRetryDialog;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.submitlogs);

		mFetchLocalDataTask = new FetchLocalDataTask(this.getClass().getSimpleName());
		mFetchLocalDataTask.execute();

        if (savedInstanceState == null) {
            _contentFrag = new SubmitLogsFrag();
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.content_fragment, _contentFrag, "Content")
                    .commit();
        } else {
            _contentFrag = (SubmitLogsFrag) getSupportFragmentManager()
                    .findFragmentByTag("Content");
        }
	}
	
	@Override
	protected void loadControls()
	{
		super.loadControls();
	}
	
	@Override
	public void setFragments()
	{
		super.setFragments();
	}
	
	public IAPIController getMyController()
	{
		return this.getController(APIControllerBase.class);
	}
	
	@Override
	protected void InitController() {
		IAPIController empLogCtrl = MandateObjectFactory.getInstance(this, GlobalState.getInstance().getFeatureService()).getCurrentEventController();
	
		this.setController(empLogCtrl);	
	}

	public void handleDoneButtonClick(){
		if(_submitLogsTask != null)
			_submitLogsTask.cancel(true);
		Return();
	}

	public void handleSubmitButtonClick()
	{
		_contentFrag.getSubmitButton().setEnabled(false);
		
		_submitLogsTask = new SubmitLogsTask();
		_submitLogsTask.execute(new Void[0]);
	}
   
	@Override
    protected void Return()
    {
		this.finish();
		
		/* Display rodsentry activity */
        this.startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		this.CreateOptionsMenu(menu, false);
		return true;
	}

    private AlertDialog CreateConfirmationMessage(Context ctx, String msg)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setMessage(msg)
               .setCancelable(false)
               .setPositiveButton(this.getString(R.string.button_try_again), new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                        handleSubmitButtonClick();
                   }
               })
               .setNegativeButton(this.getString(R.string.btnok), new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       Return();
                   }
               });
        AlertDialog alert = builder.create();
        return alert;
    }

 	/// <summary>
    /// Attempt to submit the all the records to DMO.
    /// If the network is not available, then display a message.
    /// Answer if everything was submitted successfully.
    /// </summary>
    /// <returns></returns>
    private boolean PerformSubmitLogsAOBRD(User submittingUser)
    {
    	boolean isSuccessful = false;
        try
        {
            if (((APIControllerBase)this.getMyController()).getIsWebServicesAvailable())
            {
				createCertificationEvents();

                isSuccessful = this.getMyController().SubmitAllRecords(submittingUser, true);
            }
            else
            {
            	Toast.makeText(this, this.getString(R.string.msgnetworknotavailable), Toast.LENGTH_SHORT).show();
            }
        }
        catch (Exception ex)
        {
        	
        	Log.e("UnhandledCatch", ex.getMessage() + ": " + Log.getStackTraceString(ex));
        }
        return isSuccessful;
    }

	/**
	 * Add Certification event only to the most recent LogSourceStatusEnum
	 */
	private void createCertificationEvents()
	{
		if (!GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled())
			return;

		IAPIController empLogController = MandateObjectFactory.getInstance(this, GlobalState.getInstance().getFeatureService()).getCurrentEventController();
		List<Date> selectedLogDates = empLogController.GetUncertifiedLogDatesExceptToday(((APIControllerBase)getMyController()).getCurrentUser());

		for (Date date : selectedLogDates) {

			EmployeeLog logForDate = empLogController.GetEmployeeLogToCertify(date);

			try {
				if (!logForDate.getIsCertified()) {
					empLogController.CertifyEmployeeLog(logForDate);
				}
				// else assume upload failed and we don't want to re-add Certification event
			} catch (Throwable e) {
				Log.e("UnhandledCatch", "Failed to log a certification event", e);
			}
		}
	}

	private class SubmitLogsTask extends AsyncTask<Void, String, Boolean> {
		ProgressDialog pd;
		Exception ex;
		
		protected void onPreExecute()
		{
			LockScreenRotation();
			if(!SubmitLogs.this.isFinishing())
				pd = ProgressDialog.show(SubmitLogs.this, "", getString(R.string.msgcontacting));
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			boolean isSubmitted = false;

			// submit all the logs
			isSubmitted = PerformSubmitLogsAOBRD(((APIControllerBase)getMyController()).getCurrentUser());
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
			_contentFrag.getMessageTextView().setText(values[0]);
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
				if(!SubmitLogs.this.isFinishing())
				{
					submitRetryDialog = CreateConfirmationMessage(SubmitLogs.this, getString(R.string.msgerroroccuredsubmitagain));
					submitRetryDialog.show();
				}
			}
			
			UnlockScreenRotation();
        }

	}

}
