package com.jjkeller.kmb;

import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.view.Menu;
import com.jjkeller.kmb.fragments.LeftNavFrag;
import com.jjkeller.kmb.fragments.SupportContactFrag;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmbui.R;

/**
 * Created by T000682 on 2/22/2018.
 */

public class SupportContact extends BaseActivity implements LeftNavFrag.OnNavItemSelectedListener, LeftNavFrag.ActivityMenuItemsListener {
    @Override
    protected void InitController() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.baselayout);

        mFetchLocalDataTask = new FetchLocalDataTask(this.getClass().getSimpleName());
        mFetchLocalDataTask.execute();
    }

    @Override
    protected void loadControls() {
        super.loadControls();
        loadContentFragment(new SupportContactFrag());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        this.CreateOptionsMenu(menu, false);
        return true;
    }

    public String getActivityMenuItemList()
    {
        return getString(R.string.btndone);
    }


    @Override
    public void onNavItemSelected(int menuItem)
    {
        handleMenuItemSelected(menuItem);
    }

    private void handleMenuItemSelected(int itemPosition)
    {
        if (itemPosition == 0)
        {
            this.finish();
            this.startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }

    }
}
