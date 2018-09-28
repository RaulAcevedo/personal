package com.jjkeller.kmb;

import android.os.Bundle;

import com.actionbarsherlock.view.Menu;
import com.jjkeller.kmb.fragments.GeotabConfigFrag;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmbapi.controller.GeotabController;
import com.jjkeller.kmbui.R;

public class GeotabConfig extends BaseActivity {

    GeotabConfigFrag _frag;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.baselayout);

        mFetchLocalDataTask = new FetchLocalDataTask(this.getClass().getSimpleName());
        mFetchLocalDataTask.execute();
    }

    @Override
    protected void loadControls() {
        super.loadControls();

        _frag = new GeotabConfigFrag();
        loadContentFragment(_frag);
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

    public void onNavItemSelected(int itemPosition)
    {
        handleMenuItemSelected(itemPosition);
    }

    private void handleMenuItemSelected(int itemPosition)
    {
        if (itemPosition == 0 /* Done */)
            finish();
    }

    @Override
    protected void InitController()
    {
        this.setController(new GeotabController(this));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    public GeotabController getGeoTabController() {
        return (GeotabController)this.getController();
    }
}
