package com.jjkeller.kmb;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.jjkeller.kmb.fragments.CrashDialogFrag;
import com.jjkeller.kmb.fragments.LeftNavFrag;
import com.jjkeller.kmb.interfaces.ICrashDialog.CrashFragActions;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbui.R;

public class CrashDialog extends BaseActivity implements CrashFragActions, LeftNavFrag.OnNavItemSelectedListener, LeftNavFrag.ActivityMenuItemsListener
{
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.baselayout);
		loadContentFragment(new CrashDialogFrag());
		loadLeftNavFragment();
	}

	@Override
	protected void InitController()
	{
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		return false;
	}

	public String getActivityMenuItemList()
	{
		return getString(R.string.systemmenu_actionitems);
	}

	public void onNavItemSelected(int itemPosition)
	{
		if (itemPosition == 0)
			handleCloseButtonClick();
	}

	public void onViewCreated()
	{
		Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
		CrashDialogFrag contentFragment = (CrashDialogFrag)fragment;

		TextView stackTraceView = contentFragment.getCrashMessageTextView();
		stackTraceView.setClickable(false);
		stackTraceView.setLongClickable(false);

		String stackTrace = getIntent().getStringExtra(this.getResources().getString(R.string.extra_crashmsg));
		if (stackTrace != null)
		{
			stackTraceView.setText(stackTrace);
		}
	}

	public void handleCloseButtonClick()
	{
		// 9/19/12 JHM - Insure that Crash detection is set to true.
        GlobalState.getInstance().setIsCrashDetected(true);
		finish();
	}
}
