package com.jjkeller.kmb;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.actionbarsherlock.view.MenuItem;
import com.jjkeller.kmb.fragments.LeftNavFrag;
import com.jjkeller.kmb.fragments.RptMalfunctionAndDataDiagnosticFrag;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmbapi.common.JsonUtil;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.interfaces.IAPIController;
import com.jjkeller.kmbapi.controller.share.MandateObjectFactory;
import com.jjkeller.kmbapi.enums.DataDiagnosticEnum;
import com.jjkeller.kmbapi.enums.Malfunction;
import com.jjkeller.kmbui.R;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class RptMalfunctionAndDataDiagnostic  extends BaseActivity implements LeftNavFrag.OnNavItemSelectedListener, LeftNavFrag.ActivityMenuItemsListener {

    public static final String COMPLIANCE_MALFUNCTIONS = "complianceMalfunctionCodes";
    public static final String DATA_DIAGNOSTIC_EVENTS = "dataDiagnosticEvents";
    public static final String FROM_ALERT_BUTTON_PRESSED = "fromAlertButtonPressed";

    private HashSet<Malfunction> _complianceMalfunctionList = null;
    private HashSet<DataDiagnosticEnum> _dataDiagnosticEventsList = null;
    private boolean fromAlertButtonPressed = false;

    private RptMalfunctionAndDataDiagnosticFrag _contentFragment;

    private List<DataDiagnosticEnum> dataDiagnosticOrder;
    private List<Malfunction> malfunctionOrder;

    public RptMalfunctionAndDataDiagnostic() {
        malfunctionOrder = new LinkedList<>();
        malfunctionOrder.add(Malfunction.POWER_COMPLIANCE);
        malfunctionOrder.add(Malfunction.ENGINE_SYNCHRONIZATION_COMPLIANCE);
        malfunctionOrder.add(Malfunction.TIMING_COMPLIANCE);
        malfunctionOrder.add(Malfunction.POSITIONING_COMPLIANCE);
        malfunctionOrder.add(Malfunction.DATA_RECORDING_COMPLIANCE);
        malfunctionOrder.add(Malfunction.DATA_TRANSFER_COMPLIANCE);
        malfunctionOrder.add(Malfunction.OTHER_ELD_DETECTED);


        dataDiagnosticOrder = new LinkedList<>();
        dataDiagnosticOrder.add(DataDiagnosticEnum.POWER);
        dataDiagnosticOrder.add(DataDiagnosticEnum.ENGINE_SYNCHRONIZATION);
        dataDiagnosticOrder.add(DataDiagnosticEnum.MISSING_REQUIRED_DATA_ELEMENTS);
        dataDiagnosticOrder.add(DataDiagnosticEnum.DATA_TRANSFER);
        dataDiagnosticOrder.add(DataDiagnosticEnum.UNIDENTIFIED_DRIVING_RECORDS);
        dataDiagnosticOrder.add(DataDiagnosticEnum.OTHER_ELD_IDENTIFIED);
    }

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rptmalfunctionanddatadiagnostic);
        loadContentFragment(new RptMalfunctionAndDataDiagnosticFrag());

        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
            if(bundle.containsKey(COMPLIANCE_MALFUNCTIONS)) {
                String complianceMalfunctionJson = bundle.getString(COMPLIANCE_MALFUNCTIONS);
                _complianceMalfunctionList = JsonUtil.getGson().fromJson(complianceMalfunctionJson, JsonUtil.TYPE_SET_OF_MALFUNCTIONS);
            }
            if(bundle.containsKey(DATA_DIAGNOSTIC_EVENTS)) {
                String dataDiagnosticJson = bundle.getString(DATA_DIAGNOSTIC_EVENTS);
                _dataDiagnosticEventsList = JsonUtil.getGson().fromJson(dataDiagnosticJson, JsonUtil.TYPE_SET_OF_DATA_DIAGNOSTIC_ENUMS);
            }
            fromAlertButtonPressed = bundle.getBoolean(FROM_ALERT_BUTTON_PRESSED, false);
        }

        this.loadControls(savedInstanceState);
    }

    protected IAPIController getMyController()
    {
        return (IAPIController) this.getController();
    }

    @Override
    protected void InitController()
    {
        this.setController(MandateObjectFactory.getInstance(this, GlobalState.getInstance().getFeatureService()).getCurrentEventController());
    }

    @Override
    protected void loadData()
    {
        if (_dataDiagnosticEventsList == null || _complianceMalfunctionList == null) {
            _contentFragment.setMalfunctionList(malfunctionOrder);
            _contentFragment.setDataDiagnosticList(dataDiagnosticOrder);
        } else {
            LinkedList<Malfunction> sortedMalfunctionList = new LinkedList<>();
            LinkedList<DataDiagnosticEnum> sortedDiagnosticList = new LinkedList<>();
            for (Malfunction malfunction : malfunctionOrder){
                if (_complianceMalfunctionList.contains(malfunction)){
                    sortedMalfunctionList.add(malfunction);
                }
            }
            for (DataDiagnosticEnum dataDiagnosticEnum : dataDiagnosticOrder){
                if (_dataDiagnosticEventsList.contains(dataDiagnosticEnum)){
                    sortedDiagnosticList.add(dataDiagnosticEnum);
                }
            }

            _contentFragment.setMalfunctionList(sortedMalfunctionList);
            _contentFragment.setDataDiagnosticList(sortedDiagnosticList);

            if (! sortedMalfunctionList.isEmpty()){
                _contentFragment.displayMalfunctionMessage();
            } else if (! sortedDiagnosticList.isEmpty()){
                _contentFragment.displayDiagnosticMessage();
            }
        }

    }

    @Override
    public void setFragments()
    {
        super.setFragments();
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
        _contentFragment = (RptMalfunctionAndDataDiagnosticFrag) fragment;

        loadData();
    }


    @Override
    public String getActivityMenuItemList()
    {
        return getString(R.string.rptdailyhours_actionitems);
    }

    private void handleMenuItemSelected(int itemPosition)
    {
        if (itemPosition == 0)
        {
            this.finish();
            if (! fromAlertButtonPressed) {
                this.startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
            }
        }
    }

    @Override
    public void onNavItemSelected(int itemPosition)
    {
        handleMenuItemSelected(itemPosition);
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
}
