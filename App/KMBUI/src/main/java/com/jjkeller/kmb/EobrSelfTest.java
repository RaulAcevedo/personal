package com.jjkeller.kmb;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.jjkeller.kmb.fragments.EobrSelfTestFrag;
import com.jjkeller.kmb.fragments.LeftNavFrag;
import com.jjkeller.kmb.interfaces.IEobrSelfTest.EobrSelfTestFragActions;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.EobrConfigController;
import com.jjkeller.kmbapi.kmbeobr.Enums.EobrReturnCode;
import com.jjkeller.kmbapi.kmbeobr.EobrSelfTestResult;
import com.jjkeller.kmbui.R;

public class EobrSelfTest extends BaseActivity implements EobrSelfTestFragActions, LeftNavFrag.OnNavItemSelectedListener, LeftNavFrag.ActivityMenuItemsListener
{
	private EobrSelfTestFrag _contentFrag;
	private String _result = null;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		if (savedInstanceState != null)
			_result = savedInstanceState.getString("result");

		setContentView(R.layout.baselayout);

		loadContentFragment(new EobrSelfTestFrag());
		loadControls(savedInstanceState);
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		if (_contentFrag != null)
			outState.putString("result", _contentFrag.getSelfTestResultLabel().getText().toString());
	}

	@Override
	protected void InitController()
	{
		setController(new EobrConfigController(this));
	}

	protected EobrConfigController getMyController()
	{
		return (EobrConfigController)this.getController();
	}

	@Override
	public void setFragments()
	{
		super.setFragments();
		Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
		_contentFrag = (EobrSelfTestFrag)fragment;
		if (_result != null)
			_contentFrag.getSelfTestResultLabel().setText(_result);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		this.CreateOptionsMenu(menu, false);
		return true;
	}
	
	@Override
	public String getActivityMenuItemList()
	{
		return getString(R.string.btndone);
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
	
	@Override
	public void onNavItemSelected(int itemPosition)
	{
		handleMenuItemSelected(itemPosition);
	}

	public void handleStartSelfTestButtonClick()
	{
		if (getMyController().startSelfTest())
			_contentFrag.getSelfTestResultLabel().setText(R.string.self_test_start_successful);
		else
			_contentFrag.getSelfTestResultLabel().setText(R.string.self_test_start_failed);
	}

	public void handleGetSelfTestButtonClick()
	{
		EobrSelfTestResult testResult = new EobrSelfTestResult();
		int returnCode = getMyController().getSelfTest(testResult);
		if (returnCode == EobrReturnCode.S_SUCCESS)
		{
			if (testResult.isSuccessful())
			{
				_contentFrag.getSelfTestResultLabel().setText(R.string.self_test_result_successful);
			}
			else
			{
				_contentFrag.getSelfTestResultLabel().setText(getString(R.string.self_test_result_failed, testResult.getErrorCode()));
			}
		}
		else
		{
			if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
				_contentFrag.getSelfTestResultLabel().setText(R.string.msg_elddevicenotconnected);
			} else {
				_contentFrag.getSelfTestResultLabel().setText(R.string.msg_eobrdevicenotconnected);
			}
		}
	}
}
