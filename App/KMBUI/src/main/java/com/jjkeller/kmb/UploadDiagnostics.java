package com.jjkeller.kmb;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.jjkeller.kmb.fragments.LeftNavFrag;
import com.jjkeller.kmb.fragments.UploadDiagnosticsFrag;
import com.jjkeller.kmb.interfaces.IUploadDiagnostics.UploadDiagnosticsFragActions;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.EOBR.EobrReader;
import com.jjkeller.kmbapi.controller.FileUploadController;
import com.jjkeller.kmbapi.controller.utility.FileUtility;
import com.jjkeller.kmbui.R;

public class UploadDiagnostics extends BaseActivity implements UploadDiagnosticsFragActions, LeftNavFrag.OnNavItemSelectedListener, LeftNavFrag.ActivityMenuItemsListener
{
	private ProgressDialog _uploadProgress;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.baselayout);

		mFetchLocalDataTask = new FetchLocalDataTask(this.getClass().getSimpleName());
		mFetchLocalDataTask.execute();
	}

	@Override
	protected void Return(boolean success)
	{
		if (success)
			this.startActivity(RodsEntry.class);
	}

	@Override
	protected void loadControls()
	{
		super.loadControls();
		loadContentFragment(new UploadDiagnosticsFrag());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		this.CreateOptionsMenu(menu, false);
		return true;
	}

	public String getActivityMenuItemList()
	{
		return this.getString(R.string.btndone);
	}
	
	private void handleMenuItemSelected(int itemPosition)
	{
		if (itemPosition == 0)
			this.startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		//See if home button was pressed
		this.GoHome(item, this.getController());
		
		handleMenuItemSelected(item.getItemId());
		super.onOptionsItemSelected(item);
		return true;
	}

	public void onNavItemSelected(int itemPosition)
	{
		handleMenuItemSelected(itemPosition);
	}

	protected FileUploadController getMyController()
	{
		return (FileUploadController)this.getController();
	}

	@Override
	protected void InitController()
	{
		this.setController(new FileUploadController(this));
	}

	public void handleUploadButtonClick()
	{
		if (this.getMyController().getIsNetworkAvailable())
		{
			_uploadProgress = new ProgressDialog(this);
			_uploadProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			_uploadProgress.setIcon(null);
			_uploadProgress.setCancelable(false);
			LockScreenRotation();
			this.getMyController().UploadDiagnosticPackage(_uploadProgress);
		}
		else
		{
			ShowMessage(this, getString(R.string.no_network_connection));
		}
	}

	public void handleUploadBackupLogsButtonClick()
	{
		if (this.getMyController().getIsNetworkAvailable())
		{
			_uploadProgress = new ProgressDialog(this);
			_uploadProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			_uploadProgress.setIcon(null);
			_uploadProgress.setCancelable(false);
			LockScreenRotation();
			this.getMyController().UploadBackupErrorLogs(_uploadProgress);
		}
		else
		{
			ShowMessage(this, getString(R.string.no_network_connection));
		}

	}

	public void handleClearEobrButtonClick()
	{
		ProgressDialog pd = new ProgressDialog(this);
		LockScreenRotation();
		this.getMyController().ClearEobrDiagnostics(this, pd);
	}

	public void handleClearKmbButtonClick()
	{
		ProgressDialog pd = new ProgressDialog(this);
		LockScreenRotation();
		this.getMyController().ClearKmbDiagnostics(this, pd);
	}

	public void handleMoveToSDCardButtonClick()
	{
		if (FileUtility.IsExternalStorageMounted())
		{
			LockScreenRotation();
			boolean success = FileUtility.MoveDiagnosticsToSDCard(this.getFilesDir());
			if (success)
				Toast.makeText(this, getString(R.string.msgmovetosdcardsuccessful), Toast.LENGTH_SHORT).show();
			else
				Toast.makeText(this, getString(R.string.msgmovetosdcardfailed), Toast.LENGTH_SHORT).show();
			UnlockScreenRotation();
		}
		else
		{
			Toast.makeText(this, getString(R.string.msgnosdcardavailable), Toast.LENGTH_SHORT).show();
		}
	}	
	
	public void handleConsole()
	{
		if(EobrReader.getIsEobrDeviceAvailable()) {
			startActivity(ConsoleDump.class);
		} else {
			if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
				this.showMsg(getString(R.string.no_eld_available));
			} else {
				this.showMsg(getString(R.string.no_eobr_available));

			}
		}
	}
}
