package com.jjkeller.kmb;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.jjkeller.kmb.interfaces.ILogDownloaderHost;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.LogEntryController;
import com.jjkeller.kmbapi.controller.abstracts.APIControllerBase;
import com.jjkeller.kmbapi.controller.interfaces.IAPIController;
import com.jjkeller.kmbapi.controller.share.KmbApplicationException;
import com.jjkeller.kmbapi.controller.share.MandateObjectFactory;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbui.R;

import java.util.Date;
import java.util.List;

public class EmployeeLogDownloader {
	
	ILogDownloaderHost _host;
	BaseActivity _activity;
	List<Date> _missingLogs;
	
	LogEntryController _logEntryController;
	IAPIController _employeeLogController;
	
	public EmployeeLogDownloader(ILogDownloaderHost host) {
		_host = host;
		_activity = host.getActivity();
		
		_logEntryController = new LogEntryController(_activity);
		_employeeLogController = MandateObjectFactory.getInstance(_activity,GlobalState.getInstance().getFeatureService()).getCurrentEventController();
	}
	
	public void DownloadLogs() {
		mDownloadLogsTask = new DownloadLogsTask();
		mDownloadLogsTask.execute();
	}
	
	public void CreateOffdutyLogs() {
		mCreateOffDutyLogsTask = new CreateOffDutyLogsTask();
		mCreateOffDutyLogsTask.execute();
	}
	
	private DownloadLogsTask mDownloadLogsTask;
	private class DownloadLogsTask extends AsyncTask<Void, Void, Void> {
		ProgressDialog pd;
		Exception ex;
		
		protected void onPreExecute()
		{
			_activity.LockScreenRotation();
		}
		
		protected Void doInBackground(Void... params) {
			if (_logEntryController.ShouldDownloadRecords())
			{
				publishProgress();
				try {
					_missingLogs = _logEntryController.DownloadRecordsForCompliance(((APIControllerBase)_employeeLogController).getCurrentUser());

                   if  (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled())
                        _logEntryController.DownloadKMBEncompassUsers();
				} 
				catch (KmbApplicationException kae) {
					this.ex = kae;
				}
			}

			return null;
		}

		protected void onProgressUpdate(Void... unused) {
			pd = ProgressDialog.show(_activity, "", _activity.getString(R.string.msgdownloadinglogdata));
		}

        protected void onPostExecute(Void unused) {
        	if(pd != null && pd.isShowing()) pd.dismiss();
        	if(ex != null)
        	{
        		if (ex.getClass() == KmbApplicationException.class) {
                    _activity.HandleException((KmbApplicationException) ex);
                }
                _activity.UnlockScreenRotation();
        	}
        	else if (_missingLogs != null && _missingLogs.size() > 0)
			{
        		// missing logs exist, so confirm whether off-duty logs should be created        		
				StringBuilder msg = new StringBuilder();
				
				msg.append("There are ");
				msg.append(_missingLogs.size());
				msg.append(" missing logs:\n");

				for (int i = 0; i < _missingLogs.size(); i++) { 
				    Date logDate = _missingLogs.get(i);
				    if (i > 0) msg.append(", ");
				    msg.append(DateUtility.getHomeTerminalDateFormat().format(logDate));
				}
								
				msg.append("\n\nCreate an off duty log for each?");
				
				handleLogsSelection(msg.toString());
			}
        	else{
        		// no missing logs found
        		boolean logsFound = false;
        		_host.onLogDownloadFinished(logsFound);
        	}
        }
	}
	
	private void handleLogsSelection(String msg)
	{
		_host.ShowConfirmationMessage(msg,
			new Runnable() {
				public void run() {
					mCreateOffDutyLogsTask = new CreateOffDutyLogsTask();
					mCreateOffDutyLogsTask.execute();
				}
			},
			new Runnable() {
				public void run() {
					boolean logsCreated = false;
					_host.onOffDutyLogsCreated(logsCreated);
				}
			}
		);
	}
	
	private CreateOffDutyLogsTask mCreateOffDutyLogsTask;
	private class CreateOffDutyLogsTask extends AsyncTask<Void, Void, Void> {
		ProgressDialog pd;
		Exception ex;
		GlobalState gs = GlobalState.getInstance();
		
		protected void onPreExecute()
		{
			pd = ProgressDialog.show(_activity, "", _activity.getString(R.string.msgsaving));
		}
		
		protected Void doInBackground(Void... params) {
			IAPIController empLogCtrllr = MandateObjectFactory.getInstance(gs,GlobalState.getInstance().getFeatureService()).getCurrentEventController();
        	
        	for (Date logDate : _missingLogs)
        	{
        		empLogCtrllr.CreateOffDutyLog(logDate);
        	}
        	_missingLogs = null;
			return null;
		}

		protected void onProgressUpdate(Void... unused) {
		}

        protected void onPostExecute(Void unused) {
        	boolean logsCreated = false;
        	if(pd != null && pd.isShowing()) pd.dismiss();
        	if(ex != null)
        	{
        		if (ex.getClass() == KmbApplicationException.class)
        			_activity.HandleException((KmbApplicationException)ex);
        		
        		logsCreated = true;
        	}
        	_host.onOffDutyLogsCreated(logsCreated);
        }
	}
}
