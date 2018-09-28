package com.jjkeller.kmb;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.Button;

import com.actionbarsherlock.view.Menu;
import com.jjkeller.kmb.fragments.ConsoleDumpFrag;
import com.jjkeller.kmb.interfaces.IConsoleDump.ConsoleDumpFragActions;
import com.jjkeller.kmb.interfaces.IConsoleDump.ConsoleDumpFragControllerMethods;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmbapi.common.TimeKeeper;
import com.jjkeller.kmbapi.controller.FileUploadController;
import com.jjkeller.kmbapi.controller.utility.ErrorLogHelper;
import com.jjkeller.kmbui.R;

import java.util.Calendar;
import java.util.Date;

public class ConsoleDump extends BaseActivity 
								implements ConsoleDumpFragActions, ConsoleDumpFragControllerMethods{
		
	ConsoleDumpFrag _contentFrag;
	private String _validationMsg = "";

	private static final int MILLISECONDS_IN_DAY = 86400000;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.consoledump);
	
		mFetchLocalDataTask = new FetchLocalDataTask(this.getClass().getSimpleName(), savedInstanceState);
		mFetchLocalDataTask.execute();
	}
	
	@Override
	protected void loadControls(Bundle savedIntanceState)
	{
		super.loadControls();		
		loadContentFragment(new ConsoleDumpFrag());
	}
	
	@Override
	public void setFragments()
	{
		super.setFragments();

		Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
		_contentFrag = (ConsoleDumpFrag)f;
	}
	
	@Override
	protected void InitController() {
		FileUploadController ctrlr = new FileUploadController(this);
		this.setController(ctrlr);	
	}
	
	protected FileUploadController getMyController()
	{
		return (FileUploadController)this.getController();
	}
	
	public void handleOkButtonClick()
	{
		boolean isValid = this.Validate();
		if(isValid)
		{
			mConsoleDumpTask = new ConsoleDumpTask();
			mConsoleDumpTask.execute();
		}
		else if(_validationMsg.length()>0)
		{
			this.ShowMessage(this, _validationMsg);
		}
	}
	
	public void handleCancelButtonClick()
	{
		this.Return(false);
	}
		
	@Override
	protected void Return(boolean success)
	{
		this.finish();

		this.startActivity(UploadDiagnostics.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		this.CreateOptionsMenu(menu, false);	
		return true;
	}
	
	@Override
	public void updateDateDisplay(Button button, Calendar c) {
		Calendar currentDate = Calendar.getInstance();
		
		if(button.getId() == R.id.btnConsoleDumpStartDate)
			currentDate.setTime(_contentFrag.GetStartDate());
		else
			currentDate.setTime(_contentFrag.GetEndDate());
		
		c.set(Calendar.HOUR_OF_DAY, currentDate.get(Calendar.HOUR_OF_DAY));
		c.set(Calendar.MINUTE, currentDate.get(Calendar.MINUTE));
		
		if(button.getId() == R.id.btnConsoleDumpStartDate)
			_contentFrag.SetStartDate(c.getTime());
		else
			_contentFrag.SetEndDate(c.getTime());
    }
	
	@Override
	public void updateTimeDisplay(Button button, Calendar c) {
		Calendar currentDate = Calendar.getInstance();
		
		if(button.getId() == R.id.btnConsoleDumpStartTime)
			currentDate.setTime(_contentFrag.GetStartDate());
		else
			currentDate.setTime(_contentFrag.GetEndDate());
		
		currentDate.set(Calendar.HOUR_OF_DAY, c.get(Calendar.HOUR_OF_DAY));
		currentDate.set(Calendar.MINUTE, c.get(Calendar.MINUTE));
		
		if(button.getId() == R.id.btnConsoleDumpStartTime)
			_contentFrag.SetStartDate(currentDate.getTime());
		else
			_contentFrag.SetEndDate(currentDate.getTime());
	}
	
	private static ConsoleDumpTask mConsoleDumpTask;
	private class ConsoleDumpTask extends AsyncTask<Void, Void, Boolean> {
		ProgressDialog pd;
		
		protected void onPreExecute()
		{
			LockScreenRotation();
			showProgressDialog();
		}
		
		protected Boolean doInBackground(Void... params) {
			boolean isSuccessful = false;
			
			try
			{
				isSuccessful = getMyController().SaveConsoleLog(_contentFrag.GetStartDate(), _contentFrag.GetEndDate());
			}
			catch(Throwable e){
				ErrorLogHelper.RecordException(ConsoleDump.this, e);
			}

			return isSuccessful;
		}

        protected void onPostExecute(Boolean isSuccessful) {
        	dismissProgressDialog();
			UnlockScreenRotation();

			if(isSuccessful){
				showMsg(getString(R.string.msgsuccessfullycompleted));
				Return(isSuccessful);
			}
			else{
				showMsg(getString(R.string.msgerrorsoccured));
			}
        }
        
		// Added public methods so that dialogs and context can be re-established 
		// after an orientation change (ie. activity recreated).
        public void showProgressDialog()
        {
        	pd = CreateFetchDialog(getString(R.string.msgsaving));
        }
        
        public void dismissProgressDialog()
        {
        	DismissProgressDialog(ConsoleDump.this, this.getClass(), pd);
        }
	}
	
	private boolean Validate()
	{		
		_validationMsg = "";
		
		if(_contentFrag.GetStartDate().compareTo(_contentFrag.GetEndDate()) >= 0)
			_validationMsg = this.getResources().getString(R.string.msg_startmustbeless);
		else if ((_contentFrag.GetEndDate().getTime() - (TimeKeeper.getInstance().now()).getTime()) > 0)
			_validationMsg = this.getResources().getString(R.string.msg_endcantbeinfuture);
		else if((_contentFrag.GetEndDate().getTime() - _contentFrag.GetStartDate().getTime()) > MILLISECONDS_IN_DAY) 
			_validationMsg = this.getResources().getString(R.string.msg_max24hours);
		
		return _validationMsg == "";
	}
}
