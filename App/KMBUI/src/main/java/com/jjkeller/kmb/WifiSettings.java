package com.jjkeller.kmb;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.jjkeller.kmb.fragments.WifiSettingsFrag;
import com.jjkeller.kmb.interfaces.IWifiSettings.WifiSettingsFragActions;
import com.jjkeller.kmb.interfaces.IWifiSettings.WifiSettingsFragControllerMethods;
import com.jjkeller.kmb.share.AccessPointDialog;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmbapi.controller.utility.NetworkUtilities;
import com.jjkeller.kmbapi.wifi.AccessPointState;
import com.jjkeller.kmbapi.wifi.WifiStatus;
import com.jjkeller.kmbui.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class WifiSettings extends BaseActivity implements WifiSettingsFragControllerMethods, WifiSettingsFragActions
{
	public static final String EXTRA_SHOW_UPDATER_WHEN_DONE = "show_updater_when_done";
	private static final String EXTRA_CURRENT_NETWORK = "current_network";
	private static final long SCAN_TIMER_DELAY_MILLIS = 30000;
	private static final int WIFI_CONFIGURATION_FAILED = -1;

	private WifiSettingsFrag _contentFrag;
	private WifiActionReceiver _receiver;
	private ProgressDialog _wifiStateChangingProgress;
	private AccessPointState _currentNetwork;
	private AvailableNetworkListAdapter _availableNetworksAdapter;
	private Timer _scanTimer;
	private boolean showSecondaryAuthIfNecessary = false;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.baselayout);

		if (savedInstanceState != null)
		{
			_currentNetwork = savedInstanceState.getParcelable(EXTRA_CURRENT_NETWORK);
		}

		mFetchLocalDataTask = new FetchLocalDataTask(getClass().getSimpleName());
		mFetchLocalDataTask.execute();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		_receiver = WifiActionReceiver.register(this);
		updateWifiStatusViews();
		startScanTimer();
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		_receiver.unregister();
		hideWifiStateChangingDialog();
		stopScanTimer();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		if (_currentNetwork != null)
			outState.putParcelable(EXTRA_CURRENT_NETWORK, _currentNetwork);
	}

	@Override
	protected void InitController()
	{
	}

	@Override
	protected void loadControls()
	{
		super.loadControls();
		loadContentFragment(new WifiSettingsFrag());
	}

	@Override
	public void setFragments()
	{
		super.setFragments();
		Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
		_contentFrag = (WifiSettingsFrag) f;
		updateWifiStatusViews();
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

	@Override
	public String getActivityMenuItemList()
	{
		return getString(R.string.wifi_settings_action_items);
	}

	@Override
	public void onNavItemSelected(int menuItem)
	{
		// Done is the only action
		if (showUpdaterWhenDone())
			startActivity(Updater.class);
		else
			finish();
	}
	
	private boolean showUpdaterWhenDone()
	{
		return getIntent() != null && getIntent().getBooleanExtra(EXTRA_SHOW_UPDATER_WHEN_DONE, false);
	}

	public void handleIsEnabledClicked()
	{
		WifiManager wifiManager = getWifiManager();
		wifiManager.setWifiEnabled(!wifiManager.isWifiEnabled());
	}

	public void updateWifiStatusViews()
	{
		WifiManager wifiManager = getWifiManager();
		handleWifiStateProgressDialog(wifiManager);

		boolean isEnabled = wifiManager.isWifiEnabled();
		if (_contentFrag != null)
		{
			updateEnabledStatusCheckbox(isEnabled);
			updateEnabledStatusText(isEnabled);
			updateNetworkConnectionStatus(wifiManager);
			updateAvailableNetworksList();
		}
	}

	private void updateEnabledStatusCheckbox(boolean isEnabled)
	{
		if (_contentFrag.getIsEnabledCheckbox() != null)
		{
			_contentFrag.getIsEnabledCheckbox().setChecked(isEnabled);
		}
	}

	private void updateEnabledStatusText(boolean isEnabled)
	{
		if (_contentFrag.getEnabledStatusText() != null)
		{
			if (isEnabled)
				_contentFrag.getEnabledStatusText().setText(R.string.wifi_is_enabled);
			else
				_contentFrag.getEnabledStatusText().setText(R.string.wifi_is_disabled);
		}
	}

	private void updateNetworkConnectionStatus(WifiManager wifiManager)
	{
		if (wifiManager.isWifiEnabled())
		{
			_contentFrag.showAvailableNetworksList(true);
			WifiInfo networkInfo = wifiManager.getConnectionInfo();
			if (networkInfo != null && networkInfo.getSSID() != null)
			{
				_currentNetwork = new AccessPointState(this);
				_currentNetwork.updateFromWifiInfo(networkInfo, WifiStatus.getCurrentDetailedState(this));
			}
		}
		else
		{
			_contentFrag.showAvailableNetworksList(false);
		}
	}

	private void handleWifiStateProgressDialog(WifiManager wifiManager)
	{
		int wifiState = wifiManager.getWifiState();
		switch (wifiState)
		{
			case WifiManager.WIFI_STATE_ENABLING:
				showWifiStateChangingDialog(true);
				break;
			case WifiManager.WIFI_STATE_DISABLING:
				showWifiStateChangingDialog(false);
				break;
			default:
				hideWifiStateChangingDialog();
				break;
		}
	}

	private void showWifiStateChangingDialog(boolean isEnabling)
	{
		if (_wifiStateChangingProgress == null || !_wifiStateChangingProgress.isShowing())
		{
			String progressMessage = getString(isEnabling ? R.string.msg_enabling_wifi : R.string.msg_disabling_wifi);
			_wifiStateChangingProgress = CreateFetchDialog(progressMessage);
		}
	}

	private void hideWifiStateChangingDialog()
	{
		if (_wifiStateChangingProgress != null && _wifiStateChangingProgress.isShowing())
		{
			DismissProgressDialog(this, getClass(), _wifiStateChangingProgress);
			_wifiStateChangingProgress = null;
		}
	}

	public void scanForNetworks()
	{
		getWifiManager().startScan();
		showIsScanning(true);
	}
	
	public void showIsScanning(boolean isScanning)
	{
		if (_contentFrag != null)
			_contentFrag.showIsScanning(isScanning);
	}

	public void updateAvailableNetworksList()
	{
		if (_contentFrag == null || _contentFrag.getAvailableNetworks() == null)
			return;

		WifiManager wifiManager = getWifiManager();
		HashSet<AccessPointState> availableNetworks = new HashSet<AccessPointState>();
		
		// Add all the configured networks
		addConfiguredNetworks(wifiManager, availableNetworks);

		// Update the configured networks with the scan results or add if new
		updateAvailableNetworksWithScanResults(availableNetworks, wifiManager);

		ArrayList<AccessPointState> sortedNetworks = new ArrayList<AccessPointState>(availableNetworks);
		Collections.sort(sortedNetworks);
		_availableNetworksAdapter = new AvailableNetworkListAdapter(this, sortedNetworks, _currentNetwork);
		_contentFrag.getAvailableNetworks().setAdapter(_availableNetworksAdapter);
	}

	private void addConfiguredNetworks(WifiManager wifiManager, HashSet<AccessPointState> availableNetworks)
	{
		List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
		if (configuredNetworks != null)
		{
			for (WifiConfiguration configuredNetwork : configuredNetworks)
			{
				AccessPointState network = new AccessPointState(this);
				network.updateFromWifiConfiguration(configuredNetwork);
				if (_currentNetwork != null)
					network.setPrimary(network.equals(_currentNetwork));
				availableNetworks.add(network);
			}
		}
	}

	private void updateAvailableNetworksWithScanResults(HashSet<AccessPointState> availableNetworks, WifiManager wifiManager)
	{
		List<ScanResult> scanResults = wifiManager.getScanResults();
		if (scanResults != null)
		{
			for (ScanResult result : scanResults)
			{
				AccessPointState scannedNetwork = new AccessPointState(this);
				scannedNetwork.updateFromScanResult(result);
				if (!TextUtils.isEmpty(scannedNetwork.ssid))
				{
					if (availableNetworks.contains(scannedNetwork))
					{
						for (AccessPointState availableNetwork : availableNetworks)
						{
							if (availableNetwork.equals(scannedNetwork))
							{
								// Update the network to mark that we've seen
								// it, but overwrite with the max signal
								int maxSignal = Math.max(availableNetwork.signal, scannedNetwork.signal);
								availableNetwork.updateFromScanResult(result);
								availableNetwork.setSignal(maxSignal);
								break;
							}
						}
					}
					else
					{
						availableNetworks.add(scannedNetwork);
					}
				}
			}
		}
	}

	/**
	 * On a single click, connect to a network if it's open or already
	 * configured or show the password dialog if it's secured and not yet
	 * configured.
	 */
	public void handleAvailableNetworkClicked(int position)
	{
		if (_availableNetworksAdapter != null && _availableNetworksAdapter.getCount() > position)
		{
			AccessPointState network = _availableNetworksAdapter.getItem(position);
			if (!network.hasSecurity())
			{
				updateWifiConfiguration(network);
				connectToNetwork(network);
			}
			else if (network.configured)
			{
				connectToNetwork(network);
			}
			else
			{
				showNetworkPasswordDialog(network);
			}
		}
	}

	/**
	 * On a long click, if it's a secured network, show the password dialog no
	 * matter what. This allows editing a network that is already configured.
	 */
	public void handleAvailableNetworkLongClicked(int position)
	{
		if (_availableNetworksAdapter != null && _availableNetworksAdapter.getCount() > position)
		{
			AccessPointState network = _availableNetworksAdapter.getItem(position);
			if (network.hasSecurity())
			{
				showNetworkPasswordDialog(network);
			}
			else
			{
				updateWifiConfiguration(network);
				connectToNetwork(network);
			}
		}
	}

	private void showNetworkPasswordDialog(final AccessPointState network)
	{
		AccessPointDialog accessPointDialog = new AccessPointDialog(this);
		accessPointDialog.setState(network);
		if (network.isEnterprise())
			accessPointDialog.setAutoSecurityAllowed(false);
		accessPointDialog.setMode(AccessPointDialog.MODE_CONFIGURE);
		accessPointDialog.setCancelButtonClickListener(new ShowMessageClickListener());
		accessPointDialog.setSaveButtonClickListener(new ShowMessageClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int id)
			{
				super.onClick(dialog, id);
				if (updateWifiConfiguration(network))
					connectToNetwork(network);
			}
		});
		ShowDialog(this, getString(R.string.wifi_password_dialog_message), accessPointDialog);
	}

	private boolean updateWifiConfiguration(AccessPointState network)
	{
		WifiManager wifiManager = getWifiManager();
		WifiConfiguration wifiConfig = findOrCreateWifiConfiguration(wifiManager, network);
		network.updateWifiConfiguration(wifiConfig);
		int updateResult;
		if (network.configured)
		{
			updateResult = wifiManager.updateNetwork(wifiConfig);
		}
		else
		{
			updateResult = wifiManager.addNetwork(wifiConfig);
			if (updateResult != WIFI_CONFIGURATION_FAILED)
				network.setNetworkId(updateResult);
		}
		
		if (updateResult == WIFI_CONFIGURATION_FAILED)
		{
			Toast.makeText(this, R.string.wifi_password_incorrect_error, Toast.LENGTH_LONG).show();
			return false;
		}
		return true;
	}
	
	private WifiConfiguration findOrCreateWifiConfiguration(WifiManager wifiManager, AccessPointState network)
	{
		List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
		if (configuredNetworks != null)
		{
			for (WifiConfiguration configuredNetwork : configuredNetworks)
			{
				if (network.matchesWifiConfiguration(configuredNetwork) >= AccessPointState.MATCH_WEAK)
				{
					return configuredNetwork;
				}
			}
		}
		return new WifiConfiguration();
	}

	private void connectToNetwork(AccessPointState network)
	{
		if (network.isConnectable())
		{
			showSecondaryAuthIfNecessary = true;
			getWifiManager().enableNetwork(network.networkId, true);
		}
	}
	
	void onNetworkAuthenticationFailed()
	{
		Toast.makeText(this, R.string.wifi_authentication_error, Toast.LENGTH_LONG).show();
	}
	
	void testForSecondaryAuthentication()
	{
		if (showSecondaryAuthIfNecessary && WifiStatus.isConnected(this))
		{
			showSecondaryAuthIfNecessary = false;
			new TestSecondaryAuthenticationTask().execute();
		}
	}
	
	private WifiManager getWifiManager()
	{
		return (WifiManager) getSystemService(Context.WIFI_SERVICE);
	}
	
	private void startScanTimer()
	{
		stopScanTimer();
		
		_scanTimer = new Timer();
		_scanTimer.scheduleAtFixedRate(new TimerTask()
		{
			@Override
			public void run()
			{
				runOnUiThread(new Runnable()
				{
					public void run()
					{
						scanForNetworks();
					}
				});
			}
		}, 0, SCAN_TIMER_DELAY_MILLIS);
	}
	
	private void stopScanTimer()
	{
		if (_scanTimer != null)
			_scanTimer.cancel();
	}
	
	/**
	 * An inner class to listen for WiFi changes
	 */
	private static class WifiActionReceiver extends BroadcastReceiver
	{
		private WeakReference<WifiSettings> activity;

		private WifiActionReceiver(final WifiSettings activity)
		{
			this.activity = new WeakReference<WifiSettings>(activity);
		}

		public static WifiActionReceiver register(WifiSettings activity)
		{
			WifiActionReceiver receiver = new WifiActionReceiver(activity);
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
			intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
			intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
			intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
			intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
			activity.registerReceiver(receiver, intentFilter);
			return receiver;
		}

		public void unregister()
		{
			WifiSettings activity = this.activity.get();
			if (activity != null)
				activity.unregisterReceiver(this);
		}

		@Override
		public void onReceive(Context context, Intent intent)
		{
			WifiSettings activity = this.activity.get();
			if (activity == null)
				return;

			if (intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION)
					|| intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION))
			{
				activity.updateWifiStatusViews();
			}
			else if (intent.getAction().equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION))
			{
				if (intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, -1) == WifiManager.ERROR_AUTHENTICATING)
				{
					activity.onNetworkAuthenticationFailed();
				}
				activity.updateWifiStatusViews();
			}
			else if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION))
			{
				activity.testForSecondaryAuthentication();
				activity.updateWifiStatusViews();
			}
			else if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
			{
				activity.showIsScanning(false);
				activity.updateAvailableNetworksList();
			}
		}
	}

	/**
	 * A list adapter for the available networks list
	 */
	private static class AvailableNetworkListAdapter extends ArrayAdapter<AccessPointState>
	{
		private static final int SIGNAL_LEVELS = 5;
		private final AccessPointState currentNetwork;

		public AvailableNetworkListAdapter(Context context, List<AccessPointState> networks, AccessPointState currentNetwork)
		{
			super(context, R.layout.wifisettings_list_item, networks);
			this.currentNetwork = currentNetwork;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			if (convertView == null)
			{
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.wifisettings_list_item, parent, false);
			}

			AccessPointState network = getItem(position);

			// Set default values
			CharSequence networkName = network.getHumanReadableSsid();
			CharSequence networkStatus;
			if (network.hasSecurity() && network.configured)
				networkStatus = "Saved, " + network.getHumanReadableSecurity();
			else
				networkStatus = network.getHumanReadableSecurity();

			// If it's the current network, override the defaults
			if (currentNetwork != null && network.equals(currentNetwork))
			{
				SpannableString boldName = new SpannableString(network.getHumanReadableSsid());
				boldName.setSpan(new StyleSpan(Typeface.BOLD), 0, boldName.length(), 0);
				networkName = boldName;
				
				if (currentNetwork.status != null)
				{
					String currentNetworkStatus = WifiStatus.getPrintable(getContext(), currentNetwork.status);
					if (currentNetworkStatus != null)
						networkStatus = currentNetworkStatus;
				}
			}

			TextView nameView = (TextView) convertView.findViewById(R.id.network_name);
			TextView statusView = (TextView) convertView.findViewById(R.id.network_status);
			ImageView signalImageView = (ImageView) convertView.findViewById(R.id.signal);
			nameView.setText(networkName);
			statusView.setText(networkStatus);
			setSignalImage(network, signalImageView);

			return convertView;
		}
		
		private void setSignalImage(AccessPointState network, ImageView signalImageView)
		{
			if (network.seen)
			{
				signalImageView.setVisibility(View.VISIBLE);
				switch (WifiManager.calculateSignalLevel(network.signal, SIGNAL_LEVELS))
				{
					case 4:
						signalImageView.setImageResource(R.drawable.ic_signal_wifi_4_bar_black_36dp);
						break;
					case 3:
						signalImageView.setImageResource(R.drawable.ic_signal_wifi_3_bar_black_36dp);
						break;
					case 2:
						signalImageView.setImageResource(R.drawable.ic_signal_wifi_2_bar_black_36dp);
						break;
					case 1:
						signalImageView.setImageResource(R.drawable.ic_signal_wifi_1_bar_black_36dp);
						break;
					default:
						signalImageView.setImageResource(R.drawable.ic_signal_wifi_0_bar_black_36dp);
						break;
				}
			}
			else
			{
				signalImageView.setVisibility(View.GONE);
			}
		}
	}
	
	/**
	 * An async task to test the network to see if a secondary authentication is required 
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
			return NetworkUtilities.SecondaryAuthenticationUrl(WifiSettings.this);
		}
		
		@Override
		protected void onPostExecute(String result)
		{
			super.onPostExecute(result);
			DismissProgressDialog(WifiSettings.this, getClass(), loadingDialog);
			if (result != null)
			{
				startActivity(WifiSettingsSecondaryAuth.class);
			}
		}
	}
}
