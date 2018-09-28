package com.jjkeller.kmb;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.jjkeller.kmb.fragments.WifiSettingsSecondaryAuthenticationFrag;
import com.jjkeller.kmb.interfaces.IWifiSettingsSecondaryAuth;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmbapi.controller.utility.NetworkUtilities;
import com.jjkeller.kmbui.R;

public class WifiSettingsSecondaryAuth extends BaseActivity implements
		IWifiSettingsSecondaryAuth.WifiSettingsSecondaryAuthFragControllerMethods,
		IWifiSettingsSecondaryAuth.WifiSettingsSecondaryAuthFragActions
{
	private boolean shouldLoadPage = true;
	private WifiSettingsSecondaryAuthenticationFrag _contentFrag;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.baselayout);
		
		mFetchLocalDataTask = new FetchLocalDataTask(getClass().getSimpleName());
		mFetchLocalDataTask.execute();
	}

	@Override
	protected void InitController()
	{
	}

	@Override
	protected void loadControls()
	{
		super.loadControls();
		loadContentFragment(new WifiSettingsSecondaryAuthenticationFrag());
	}
	
	@Override
	public void setFragments()
	{
		super.setFragments();
		Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
		_contentFrag = (WifiSettingsSecondaryAuthenticationFrag) f;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		CreateOptionsMenu(menu, false);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		return super.onOptionsItemSelected(item);
	}

	public void testSecondaryAuthUrl()
	{
		new TestSecondaryAuthenticationTask().execute();
	}

	public void onNewUrlLoading(String url)
	{
		testSecondaryAuthUrl();
	}
	
	/**
	 * An async task to test the network to see if a secondary authentication is still required 
	 */
	private class TestSecondaryAuthenticationTask extends AsyncTask<Void, Void, String>
	{
		private ProgressDialog loadingDialog;
		
		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			runOnUiThread(new Runnable()
			{
				public void run()
				{
					loadingDialog = CreateFetchDialog(getString(R.string.checking_connectivity));
				}
			});
		}

		@Override
		protected String doInBackground(Void... params)
		{
			return NetworkUtilities.SecondaryAuthenticationUrl(WifiSettingsSecondaryAuth.this);
		}
		
		@Override
		protected void onPostExecute(String result)
		{
			super.onPostExecute(result);
			DismissProgressDialog(WifiSettingsSecondaryAuth.this, getClass(), loadingDialog);
			if (result != null)
			{
				if (shouldLoadPage && _contentFrag != null && _contentFrag.getWebView() != null)
				{
					shouldLoadPage = false;
					_contentFrag.getWebView().loadUrl(result);
					_contentFrag.listenToNewUrls();
				}
			}
			else
			{
				// We're no longer redirected
				finish();
			}
		}
	}
}
