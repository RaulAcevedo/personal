package com.jjkeller.kmb;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.jjkeller.kmb.interfaces.IOdometerCalibrationRequiredHost;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmbapi.controller.interfaces.IAPIController;
import com.jjkeller.kmbapi.controller.utility.ErrorLogHelper;
import com.jjkeller.kmbui.R;

public class OdometerCalibrationRequiredTask extends AsyncTask<Void, Void, Void> {
	ProgressDialog pd;
	Exception ex;
	boolean odometerCalibrationRequired = false;
	
	IAPIController _logController;
	BaseActivity _activity;
	IOdometerCalibrationRequiredHost _host;
	
	public OdometerCalibrationRequiredTask(IOdometerCalibrationRequiredHost host, IAPIController logController)
	{
		_activity = host.getActivity();
		_logController = logController;
		_host = host;
	}		
	
	protected void onPreExecute()
	{
		_activity.LockScreenRotation();
		showProgressDialog();
	}
	
	protected Void doInBackground(Void... params) {
		try{
			odometerCalibrationRequired = _logController.IsOdometerCalibrationRequired();
		}
		catch(Exception excp){ this.ex = excp; }
		
		return null;
	}

	protected void onProgressUpdate(Void... unused) {
	}

    protected void onPostExecute(Void unused) {
    	dismissProgressDialog();
    	        	
    	_host.OnOdometerCalibrationRequired(odometerCalibrationRequired);
    	
        _activity.UnlockScreenRotation();
    }
    
	// Added public methods so that dialogs and context can be re-established 
	// after an orientation change (ie. activity recreated).
    public void showProgressDialog()
    {
    	if(!_activity.isFinishing())
    		pd = ProgressDialog.show(_activity, "", _activity.getString(R.string.msgretreiving));
    }
    
    public void dismissProgressDialog()
    {
    	try
    	{
    		if(pd != null && pd.isShowing()) pd.dismiss();
    	}
    	catch (Exception ex){
    		ErrorLogHelper.RecordMessage(String.format(BaseActivity.MSGASYNCDIALOGEXCEPTION, _activity.getClass().getSimpleName(), this.getClass().getSimpleName()));
    	}
    }
}
