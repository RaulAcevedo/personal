package com.jjkeller.kmbapi.controller;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.jjkeller.kmbapi.controller.dataaccess.DataTransferFileStatusFacade;
import com.jjkeller.kmbapi.controller.share.ControllerBase;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.controller.utility.EmployeeLogUtilities;
import com.jjkeller.kmbapi.controller.utility.RESTWebServiceHelper;
import com.jjkeller.kmbapi.datatransferfilestatus.DataTransferFileStatusEvent;
import com.jjkeller.kmbapi.datatransferfilestatus.DataTransferFileStatusEvent.DataTransferFileStatusEventType;
import com.jjkeller.kmbapi.datatransferfilestatus.DataTransferFileStatusService;
import com.jjkeller.kmbapi.enums.DataTransferFileStatusEnum;
import com.jjkeller.kmbapi.enums.RoadsideDataTransferMethodEnum;
import com.jjkeller.kmbapi.proxydata.DataTransferFileStatus;
import com.jjkeller.kmbapi.proxydata.DataTransferFileStatusResponse;

import org.greenrobot.eventbus.EventBus;

import java.util.Date;


public class RoadsideInspectionController extends ControllerBase {

    private static final String LOG_TAG = "RoadsideDataTransfer";
    private static final int MAX_ATTEMPT_COUNT = 5;

    public RoadsideInspectionController(Context ctx) {
        super(ctx);
    }

    public String GetDataTransferRoadsideFile(User user, RoadsideDataTransferMethodEnum transferMethod, String outputFileComment, String eldIdentifier) {
        if (transferMethod == null)
            transferMethod = new RoadsideDataTransferMethodEnum(RoadsideDataTransferMethodEnum.EMAIL);
        Date today = EmployeeLogUtilities.CalculateLogStartTime(getContext(), DateUtility.CurrentHomeTerminalTime(user), user.getHomeTerminalTimeZone());
        Date startDate = DateUtility.AddDays(today, -7);
        String comment = (outputFileComment == null) ? "" : outputFileComment.trim();
        String identifier = (eldIdentifier == null) ? "" : eldIdentifier.trim();
        return new RESTWebServiceHelper(getContext()).GetDataTransferRoadsideFile(transferMethod, startDate, today, comment, identifier);
    }

    public void StartEmailDataTransferPolling(String filename) {
        RoadsideDataTransferMethodEnum dataTransferMethod = new RoadsideDataTransferMethodEnum(RoadsideDataTransferMethodEnum.EMAIL);
        SaveInitialDataTransferFileStatus(filename, dataTransferMethod);
        getContext().startService(new Intent(getContext(), DataTransferFileStatusService.class));
    }

    public boolean PollEmailDataTransferStatus() {
        RoadsideDataTransferMethodEnum dataTransferMethod = new RoadsideDataTransferMethodEnum(RoadsideDataTransferMethodEnum.EMAIL);
        boolean transferIsComplete = false;

        DataTransferFileStatusFacade facade = new DataTransferFileStatusFacade(this.getContext(), this.getCurrentUser());
        DataTransferFileStatus statusObj = facade.GetLatestByTransferMethod(dataTransferMethod);

        if (statusObj == null)
            return true;

        if (statusObj.getAttemptCount() < MAX_ATTEMPT_COUNT) {
            DataTransferFileStatusEnum returnedStatus = GetDataTransferStatus(statusObj.getFileName(), dataTransferMethod);
            UpdateDataTransferFileStatus(statusObj, returnedStatus, false);
        }

        if (IsSuccessful(statusObj.getStatus())) {
            transferIsComplete = true;

            if (!statusObj.getWasNotificationDisplayed()) {
                EventBus.getDefault().post(new DataTransferFileStatusEvent(DataTransferFileStatusEventType.SUCCESSFUL));
                statusObj.setWasNotificationDisplayed(true);
            }
        } else if (IsFailure(statusObj.getStatus()) || statusObj.getAttemptCount() >= MAX_ATTEMPT_COUNT) {
            transferIsComplete = true;

            if (!statusObj.getWasNotificationDisplayed()) {
                EventBus.getDefault().post(new DataTransferFileStatusEvent(DataTransferFileStatusEventType.FAILURE));
                statusObj.setWasNotificationDisplayed(true);
            }
        }

        facade.Save(statusObj);

        return transferIsComplete;
    }

    public boolean CheckWebServiceDataTransferStatus(String filename) {
        RoadsideDataTransferMethodEnum dataTransferMethod = new RoadsideDataTransferMethodEnum(RoadsideDataTransferMethodEnum.WEBSERVICE);

        DataTransferFileStatus statusObj = SaveInitialDataTransferFileStatus(filename, dataTransferMethod);
        DataTransferFileStatusEnum status = GetDataTransferStatus(filename, dataTransferMethod);
        UpdateDataTransferFileStatus(statusObj, status, true);

        return IsSuccessful(status);
    }

    private DataTransferFileStatusEnum GetDataTransferStatus(String filename, RoadsideDataTransferMethodEnum dataTransferMethod) {
        DataTransferFileStatusEnum status = null;

        try {
            DataTransferFileStatusResponse response = new RESTWebServiceHelper(getContext()).GetDataTransferFileStatus(filename, dataTransferMethod);
            status = response.getStatus();

            String transferTypeStr = dataTransferMethod.getValue() == RoadsideDataTransferMethodEnum.EMAIL ? "email" : "webservice";
            Log.i(LOG_TAG, String.format("Data Transfer File Status for %s transfer: Sent Request for status with result %d", transferTypeStr, status.getValue()));
        } catch (Exception e) {
            this.HandleException(e);
        }

        return status;
    }

    private DataTransferFileStatus SaveInitialDataTransferFileStatus(String filename, RoadsideDataTransferMethodEnum dataTransferMethod) {
        DataTransferFileStatusFacade facade = new DataTransferFileStatusFacade(this.getContext(), this.getCurrentUser());
        DataTransferFileStatus status = new DataTransferFileStatus();
        status.setFileName(filename);
        status.setCreateDate(this.getCurrentClockHomeTerminalTime());
        status.setAttemptCount(0);
        status.setStatus(new DataTransferFileStatusEnum(DataTransferFileStatusEnum.UNKNOWN));
        status.setRoadsideDataTransferMethod(dataTransferMethod);
        status.setWasNotificationDisplayed(false);
        facade.Save(status);

        Log.i(LOG_TAG, "Data Transfer File Status: New Transfer Status Created");

        return status;
    }

    private void UpdateDataTransferFileStatus(DataTransferFileStatus statusObj, DataTransferFileStatusEnum returnedStatus, boolean wasNotificationDisplayed) {
        DataTransferFileStatusFacade facade = new DataTransferFileStatusFacade(this.getContext(), this.getCurrentUser());
        statusObj.setAttemptCount(statusObj.getAttemptCount() + 1);
        if (returnedStatus != null)
            statusObj.setStatus(returnedStatus);
        statusObj.setWasNotificationDisplayed(wasNotificationDisplayed);
        facade.Save(statusObj);
    }

    private boolean IsSuccessful(DataTransferFileStatusEnum status) {
        if (status != null) {
            switch (status.getValue()) {
                case DataTransferFileStatusEnum.SUCCESS:
                case DataTransferFileStatusEnum.WARNING:
                case DataTransferFileStatusEnum.INFORMATIONAL:
                    return true;
                default:
                    return false;
            }
        } else {
            return false;
        }
    }

    private boolean IsFailure(DataTransferFileStatusEnum status) {
        if (status != null) {
            switch (status.getValue()) {
                case DataTransferFileStatusEnum.ERROR:
                case DataTransferFileStatusEnum.NOTVALIDATED:
                    return true;
                default:
                    return false;
            }
        } else {
            return true;
        }
    }
}
