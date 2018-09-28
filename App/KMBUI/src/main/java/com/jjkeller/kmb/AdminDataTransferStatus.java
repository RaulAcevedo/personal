package com.jjkeller.kmb;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import com.jjkeller.kmb.fragments.AdminDataTransferStatusFrag;
import com.jjkeller.kmb.interfaces.IAdminDataTransferStatus;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmbapi.controller.DataTransferMechanismStatusController;
import com.jjkeller.kmbui.R;

/**
 * Created by klarsen on 3/28/2017.
 */

public class AdminDataTransferStatus  extends BaseActivity
        implements IAdminDataTransferStatus.AdminDataTransferStatusFragActions, IAdminDataTransferStatus.AdminDataTransferStatusFragControllerMethods {
    AdminDataTransferStatusFrag _contentFrag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.baselayout);

        new FetchLocalDataTask(this.getClass().getSimpleName()).execute();
    }

    @Override
    protected void loadControls() {
        super.loadControls();
        loadContentFragment(new AdminDataTransferStatusFrag());
    }

    @Override
    public void setFragments()
    {
        super.setFragments();

        Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
        _contentFrag = (AdminDataTransferStatusFrag)f;
    }

    @Override
    protected void InitController() {

        DataTransferMechanismStatusController ctrlr = new DataTransferMechanismStatusController(this);
        this.setController(ctrlr);
    }

    public DataTransferMechanismStatusController getMyController()
    {
        return (DataTransferMechanismStatusController)this.getController();
    }

    @Override
    public void handleAdminAddDataTransferFailureClick() {
        this.getMyController().AddDataTransferFailure();
    }

    @Override
    public void handleAdminAddDataTransferSuccessClick() {
        this.getMyController().AddDataTransferSuccess();
    }

    @Override
    public void handleAdminClearDataTransferRecordsClick() {
        this.getMyController().ClearDataTransferRecords();
    }
}
