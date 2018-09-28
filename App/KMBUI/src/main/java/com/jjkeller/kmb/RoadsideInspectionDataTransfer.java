package com.jjkeller.kmb;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.actionbarsherlock.view.Menu;
import com.jjkeller.kmb.fragments.RoadsideInspectionDataTransferFrag;
import com.jjkeller.kmb.fragments.RoadsideInspectionDataTransferMethodFrag;
import com.jjkeller.kmb.interfaces.IRoadsideInspectionDataTransfer;
import com.jjkeller.kmb.interfaces.IRoadsideInspectionDataTransferMethod;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.FmcsaEldInfoController;
import com.jjkeller.kmbapi.controller.LoginController;
import com.jjkeller.kmbapi.controller.RoadsideInspectionController;
import com.jjkeller.kmbapi.controller.interfaces.IAPIController;
import com.jjkeller.kmbapi.controller.share.MandateObjectFactory;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.enums.RoadsideDataTransferMethodEnum;
import com.jjkeller.kmbui.R;

import java.util.Date;
import java.util.List;

public class RoadsideInspectionDataTransfer extends BaseActivity
        implements IRoadsideInspectionDataTransfer.RoadsideInspectionDataTransferActions, IRoadsideInspectionDataTransferMethod.RoadsideInspectionDataTransferMethodActions {

    private static final String STATE_KEY_VIEW_STATE = "VIEW_STATE";
    private ViewState viewState;

    private final RoadsideInspectionController roadsideInspectionController = new RoadsideInspectionController(this);
    private final LoginController loginController = new LoginController(this);

    private boolean isDataTransferFileStatusSuccess = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.baselayout);

        mFetchLocalDataTask = new FetchLocalDataTask(this.getClass().getSimpleName(), savedInstanceState);
        mFetchLocalDataTask.execute();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(STATE_KEY_VIEW_STATE, viewState);
    }

    @Override
    protected void InitController() {
        setController(roadsideInspectionController);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    protected void loadControls(Bundle savedInstanceState) {
        super.loadControls(savedInstanceState);
        setViewState(savedInstanceState);
        loadFragmentForState();
    }

    public String getActivityMenuItemList()
    {
        return getString(R.string.roadside_data_transfer_actionitems);
    }

    private void handleMenuItemSelected(int itemPosition)
    {
        switch (itemPosition)
        {
            case 0:
                this.startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
                break;
        }
    }

    public void onNavItemSelected(int item)
    {
        handleMenuItemSelected(item);
    }

    private void setViewState(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            viewState = new ViewState();
            boolean workFlow = this.getIntent().getBooleanExtra(getString(R.string.data_transfer_workflow), false);//May be set from Certify/Submit
            viewState.workflowAlreadyStarted = workFlow;
        } else {
            viewState = savedInstanceState.getParcelable(STATE_KEY_VIEW_STATE);
        }
    }

    private void loadFragmentForState() {
        if (viewState.workflowAlreadyStarted)
            viewState.step = Step.SELECT_METHOD;

        switch (viewState.step) {
            case START:
                showStartFragment();
                break;
            case SELECT_METHOD:
                showTransferMethodFragment();
                break;
        }
    }

    private void showStartFragment() {
        viewState.step = Step.START;
        loadContentFragment(new RoadsideInspectionDataTransferFrag());
    }

    private void showTransferMethodFragment() {
        if (userHasLogsNeedingCertification(getController().getCurrentUser()) && !viewState.workflowAlreadyStarted) {
            showCertifyLogsPrompt();
        }
        else {
            viewState.step = Step.SELECT_METHOD;
            loadContentFragment(new RoadsideInspectionDataTransferMethodFrag());
        }
    }

    @Override
    public void onDataTransferButtonClick() {
        ShowMessageClickListener onYesClicked = new ShowMessageClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                super.onClick(dialog, id);
                showTransferMethodFragment();
            }
        };
        ShowMessageClickListener onNoClicked = new ShowMessageClickListener();
        this.ShowConfirmationMessage(this, getString(R.string.roadside_data_transfer_confirmation_message), onYesClicked, onNoClicked);
    }

    @Override
    public void onRoadsideInspectionModeButtonClick() {
        startActivity(RoadsideInspection.class);
    }

    @Override
    public void onTransferMethodOkButtonClick(RoadsideDataTransferMethodEnum transferMethod, String comment) {
        viewState.transferMethod = transferMethod;
        viewState.outputFileComment = comment;

        if (!getController().getIsNetworkAvailable()) {
            ShowMessage(this, getString(R.string.no_network_connection));
        }
        else {
            startSaveTask();
        }
    }

    @Override
    public void onTransferMethodCancelButtonClick() {
        showStartFragment();
    }


    private void showCertifyLogsPrompt() {
        ShowMessageClickListener onYesClicked = new ShowMessageClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                super.onClick(dialog, id);
                Bundle extras = new Bundle();
                extras.putBoolean(getString(R.string.data_transfer_workflow), true);
                startActivity(CertifyLogs.class, Intent.FLAG_ACTIVITY_CLEAR_TOP, extras);
            }
        };
        ShowMessageClickListener onNoClicked = new ShowMessageClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                super.onClick(dialog, id);
                viewState.workflowAlreadyStarted = true; // set this true because the driver just answered no to certify
                showTransferMethodFragment();
            }
        };
        DisplayCertifyLogsDialog(onYesClicked, onNoClicked);
    }

    private void startSaveTask() {
        mSaveLocalDataTask = new SaveLocalDataTask(this.getClass().getSimpleName());
        mSaveLocalDataTask.execute();
    }

    @Override
    protected boolean saveData() {
        boolean result = false;

        User user = roadsideInspectionController.getCurrentUser();
        boolean submitSuccessful = loginController.SubmitAllRecords(user, false);

        if (submitSuccessful) {
            FmcsaEldInfoController fmcsaEldInfoController = new FmcsaEldInfoController(GlobalState.getInstance());
            String eldIdentifier = fmcsaEldInfoController.getCurrentEldIdentifier(fmcsaEldInfoController.getEobrDeviceSerialNumber());
            String file = roadsideInspectionController.GetDataTransferRoadsideFile(user, viewState.transferMethod, viewState.outputFileComment, eldIdentifier);
            result = file != null && !file.isEmpty();

            if (result) {
                if (viewState.transferMethod.getValue() == RoadsideDataTransferMethodEnum.EMAIL) {
                    isDataTransferFileStatusSuccess = true;
                    roadsideInspectionController.StartEmailDataTransferPolling(file);
                }
                else {
                    isDataTransferFileStatusSuccess = roadsideInspectionController.CheckWebServiceDataTransferStatus(file);
                }
            }
        }

        return result;
    }

    @Override
    protected void Return(boolean success) {
        if (success) {
            String msg;
            if (viewState.transferMethod.getValue() == RoadsideDataTransferMethodEnum.EMAIL) {
                msg = getString(R.string.roadside_data_transfer_email_sent_message);
            } else {
                msg = isDataTransferFileStatusSuccess ? getString(R.string.roadside_data_transfer_success_message) : getString(R.string.roadside_data_transfer_web_service_failure_message);
            }
            ShowMessage(this, null, msg, new ShowMessageClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    super.onClick(dialog, id);
                    finish();
                }
            });
        } else {
            ShowMessage(this, getString(R.string.roadside_data_transfer_failure_message));
        }
    }

    private boolean userHasLogsNeedingCertification(User user) {
        IAPIController empCon = MandateObjectFactory.getInstance(this, GlobalState.getInstance().getFeatureService()).getCurrentEventController();
        List<Date> datesThatNeedToBeCertified = empCon.GetUncertifiedLogDates();
        return !datesThatNeedToBeCertified.isEmpty();
    }

    private enum Step {
        START,
        SELECT_METHOD
    }

    private static class ViewState implements Parcelable {
        Step step = Step.START;
        RoadsideDataTransferMethodEnum transferMethod = new RoadsideDataTransferMethodEnum(RoadsideDataTransferMethodEnum.EMAIL);
        String outputFileComment;
        Boolean workflowAlreadyStarted = false;

        ViewState() {
        }

        ViewState(Parcel in) {
            step = (Step) in.readSerializable();
            transferMethod = new RoadsideDataTransferMethodEnum(in.readInt());
            outputFileComment = in.readString();
            workflowAlreadyStarted = (Boolean)in.readSerializable();
        }

        public static final Creator<ViewState> CREATOR = new Creator<ViewState>() {
            @Override
            public ViewState createFromParcel(Parcel in) {
                return new ViewState(in);
            }

            @Override
            public ViewState[] newArray(int size) {
                return new ViewState[size];
            }
        };

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeSerializable(step);
            dest.writeInt(transferMethod.getValue());
            dest.writeString(outputFileComment);
            dest.writeSerializable(workflowAlreadyStarted);
        }

        @Override
        public int describeContents() {
            return 0;
        }
    }

}
