package com.jjkeller.kmb.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.HttpAuthHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.jjkeller.kmb.interfaces.IWifiSettingsSecondaryAuth.WifiSettingsSecondaryAuthFragActions;
import com.jjkeller.kmb.interfaces.IWifiSettingsSecondaryAuth.WifiSettingsSecondaryAuthFragControllerMethods;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmb.share.HttpAuthenticationDialog;
import com.jjkeller.kmbui.R;

public class WifiSettingsSecondaryAuthenticationFrag extends BaseFragment
{
	private WifiSettingsSecondaryAuthFragControllerMethods _controllerListener;
	private WifiSettingsSecondaryAuthFragActions _actionsListener;
	
	private WebView _webView;
	
	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);

        try {
        	_controllerListener = (WifiSettingsSecondaryAuthFragControllerMethods) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement WifiSettingsSecondaryAuthFragControllerMethods");
        }

        try {
        	_actionsListener = (WifiSettingsSecondaryAuthFragActions) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement WifiSettingsSecondaryAuthFragActions");
        }
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.f_wifisettingssecondaryauthentication, container, false);
		findControls(v);
		loadControls();
		return v;
	}

	protected void findControls(View v)
	{
		_webView = (WebView) v.findViewById(R.id.web_view);
	}

	@SuppressLint("SetJavaScriptEnabled")
	protected void loadControls()
	{
		_webView.getSettings().setJavaScriptEnabled(true);
		_controllerListener.testSecondaryAuthUrl();
	}
	
	public WebView getWebView()
	{
		return _webView;
	}
	
	public void listenToNewUrls()
	{
		_webView.setWebViewClient(new WebViewClient()
		{
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon)
			{
				super.onPageStarted(view, url, favicon);
				_actionsListener.onNewUrlLoading(url);
			}
			
			@Override
			public void onReceivedHttpAuthRequest(WebView view, final HttpAuthHandler handler, String host, String realm)
			{
				HttpAuthenticationDialog dialog = new HttpAuthenticationDialog(getActivity(), host, realm);
				dialog.setOkListener(new HttpAuthenticationDialog.OkListener() {
				    public void onOk(String host, String realm, String username, String password) {
				        handler.proceed(username, password);
				    }
				});
				dialog.setCancelListener(new HttpAuthenticationDialog.CancelListener() {
				    public void onCancel() {
				        handler.cancel();
				    }
				});
				dialog.show();
			}
		});
	}
}
