package com.jjkeller.kmb;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.actionbarsherlock.view.MenuItem;
import com.jjkeller.kmb.fragments.LeftNavFrag;
import com.jjkeller.kmb.fragments.RptDOTAuthorityFrag;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmbui.R;

/**
 * Created by T000695 on 5/22/2017.
 */

public class RptDOTAuthority extends BaseActivity implements LeftNavFrag.OnNavItemSelectedListener, LeftNavFrag.ActivityMenuItemsListener {

    private RptDOTAuthorityFrag _contentFragment;

    //region Activity life cycle

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.rptdotauthority);

        if (savedInstanceState == null) {
            loadFragment(R.id.content_fragment, RptDOTAuthorityFrag.newInstance());
        }

        // Used for handling highlighting the selected item in the leftnav
        // If not using multiple fragments within an activity, we have to manually set the selected item
        this.setLeftNavSelectedItem(0);
        this.setLeftNavAllowChange(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        this.setLeftNavSelectedItem(0);
        loadLeftNavFragment();
    }

    //endregion

    @Override
    public String getActivityMenuItemList() {
        return getString(R.string.rptdailyhours_actionitems);
    }

    @Override
    public void onNavItemSelected(int itemPosition) {
        handleMenuItemSelected(itemPosition);
    }

    @Override
    protected void InitController() {}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        //See if home button was pressed
        GoHome(item, this.getController());
        handleMenuItemSelected(item.getItemId());

        return true;
    }

    @Override
    public void setFragments() {
        super.setFragments();
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
        _contentFragment = (RptDOTAuthorityFrag) fragment;
    }

    private void handleMenuItemSelected(int itemPosition) {
        if (itemPosition == 0 || itemPosition==  android.R.id.home) {
            this.finish();
            this.startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
    }

}