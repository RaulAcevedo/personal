package com.jjkeller.kmb;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.AdapterView;

import com.jjkeller.kmb.fragments.LeftNavFrag;
import com.jjkeller.kmb.fragments.RptEldEventDetailFrag;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmb.share.OffDutyBaseActivity;
import com.jjkeller.kmbui.R;
public class RptEldEventDetail extends BaseActivity implements LeftNavFrag.OnNavItemSelectedListener, LeftNavFrag.ActivityMenuItemsListener {
    private RptEldEventDetailFrag _contentFrag;


    Integer _eventId = 0;
    @Override
    protected void InitController() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.rpteldeventdetail);

        loadContentFragment(new RptEldEventDetailFrag());
        this.loadControls(savedInstanceState);
        _eventId = getIntent().getIntExtra(EldEventEdit.EXTRA_PRIMARYKEY, 0);
    }

    @Override /* leftnav fragment */
    public String getActivityMenuItemList()
    {
        return getString(R.string.rptdailyhours_actionitems);
    }

    /* leftnav fragment */
    private void handleMenuItemSelected(int itemPosition)
    {
        if (itemPosition == 0)
        {
            this.finish();
        }
    }

    @Override /* leftnav fragment */
    public void onNavItemSelected(int itemPosition)
    {
        handleMenuItemSelected(itemPosition);
    }

    @Override
    public void setFragments()
    {
        super.setFragments();

        Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
        _contentFrag = (RptEldEventDetailFrag)f;
        loadData();
    }

    @Override
    protected void loadData()
    {


        _contentFrag.setEventId(_eventId );
    }
    @Override
    protected void loadControls() {
        super.loadControls();


    }

}
