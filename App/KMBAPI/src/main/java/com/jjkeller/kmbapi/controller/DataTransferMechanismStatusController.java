package com.jjkeller.kmbapi.controller;

import android.content.Context;
import android.util.Log;

import com.jjkeller.kmbapi.R;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.dataaccess.DataTransferMechanismStatusFacade;
import com.jjkeller.kmbapi.controller.share.ControllerBase;
import com.jjkeller.kmbapi.controller.utility.RESTWebServiceHelper;
import com.jjkeller.kmbapi.employeelogeldevents.EmployeeLogEldEventCode;
import com.jjkeller.kmbapi.proxydata.DataTransferMechanismStatus;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;

import org.joda.time.DateTime;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;
import java.util.List;

/**
 * Created by Kris Larsen on 2/20/2017.
 */

public class DataTransferMechanismStatusController extends ControllerBase {

    private int _dataTransferMechanismSuccessDaysToNextTransfer;
    private int _dataTransferMechanismFailedDaysToNextTransfer;
    private int consecutiveSuccessfulTransfers = 0;
    private int totalFailedTransfers = 0;

    public DataTransferMechanismStatusController(Context ctx) {
        super(ctx);
        _dataTransferMechanismSuccessDaysToNextTransfer = GlobalState.getInstance().getAppSettings(ctx).getDataTransferMechanismSuccessDaysToNextTransfer();
        _dataTransferMechanismFailedDaysToNextTransfer = GlobalState.getInstance().getAppSettings(ctx).getDataTransferMechanismFailedDaysToNextTransfer();
    }

    public DataTransferMechanismStatus AddNewKMBRecord(Date dateScheduledToTransfer)
    {
        DataTransferMechanismStatus data = new DataTransferMechanismStatus();

        // Create the entry for the KMB database
        UUID uuid = UUID.randomUUID();
        data.setTransferId(uuid.toString());
        data.setDateScheduledToTransfer(dateScheduledToTransfer);
        data.setWasSuccessful(false);

        // Save it to the KMB db
        SetKMBDataTransferMechanismStatus(data);

        return data;
    }

    public DataTransferMechanismStatus GetKMBDataTransferMechanismStatus() {
        DataTransferMechanismStatus data;

        if (this.getCurrentUser() == null) return new DataTransferMechanismStatus();

        DataTransferMechanismStatusFacade facade = new DataTransferMechanismStatusFacade(this.getContext(),
               GlobalState.getInstance().getCurrentUser());
        data = facade.FetchCurrentTransfer();

        if (data == null) {
            data = AddNewKMBRecord(DateTime.now().toDate());
        }

        // Set global next transfer date variable
        GlobalState.getInstance().setDataTransferMechanismStatus(data);

        return data;
    }

    public void SetKMBDataTransferMechanismStatus(DataTransferMechanismStatus data) {

        DataTransferMechanismStatusFacade facade = new DataTransferMechanismStatusFacade(this.getContext(),
                GlobalState.getInstance().getCurrentUser());

        // Set global next transfer date variable
        GlobalState.getInstance().setDataTransferMechanismStatus(data);

        facade.Save(data);

        return;
    }

    public void SetConsecutiveSuccessfulTransfers(int consecutiveSuccessfulTransfers) {
        this.consecutiveSuccessfulTransfers = consecutiveSuccessfulTransfers;
    }

    public int GetConsecutiveSuccessfulTransfers() {
        return this.consecutiveSuccessfulTransfers;
    }

    public void SetTotalFailedTransfers(int totalFailedTransfers) {
        this.totalFailedTransfers = totalFailedTransfers;
    }

    public int GetTotalFailedTransfers() {
        return this.totalFailedTransfers;
    }

    public int SetKMBNextTransferDate(String transferId, boolean wasSuccessful) {

        int codeToLog =  EmployeeLogEldEventCode.None;

        DataTransferMechanismStatus data;

        if (transferId.isEmpty()) return codeToLog;

        DataTransferMechanismStatusFacade facade = new DataTransferMechanismStatusFacade(this.getContext(),
                GlobalState.getInstance().getCurrentUser());

        data = facade.GetByTransferId(transferId);

        if (data == null) {
            return codeToLog;
        }

        // Retrieve last four transfers
        List<DataTransferMechanismStatus> lastFourTransfers = facade.GetLastFourTransfers();
        Date nextTransferDate = DateTime.now().plusDays(_dataTransferMechanismFailedDaysToNextTransfer).toDate();
        if(lastFourTransfers != null && lastFourTransfers.size() > 0)
        {
            for (int i = 0; i < lastFourTransfers.size(); i++) {
                if (lastFourTransfers.get(i).getWasSuccessful())
                {
                    //we only want to count successful transfers as long as there are no failures in the first 3 records
                    if(totalFailedTransfers < 1) {
                        consecutiveSuccessfulTransfers += 1;
                    }
                }
                else
                {
                    //if the oldest of the 4 records is a failure, do not reset the consecutiveSuccessfulTransfers
                    if(i < 3) {
                        consecutiveSuccessfulTransfers = 0; //if we have a failed transfer, we need to reset the consecutive successes for an accurate count
                        totalFailedTransfers += 1;
                    }
                }
                SetConsecutiveSuccessfulTransfers(consecutiveSuccessfulTransfers);
                SetTotalFailedTransfers(totalFailedTransfers);
            }

            //set the next date to transfer to 7 days from today
            if(consecutiveSuccessfulTransfers > 2 && wasSuccessful){
                nextTransferDate = DateTime.now().plusDays(_dataTransferMechanismSuccessDaysToNextTransfer).toDate();
            }
        }

        data.setDateOfNextTransfer(nextTransferDate);

        data.setDateTransferred(DateTime.now().toDate());
        data.setWasSuccessful(wasSuccessful);

        facade.Save(data);

        DataTransferMechanismStatus newData = AddNewKMBRecord(data.getDateOfNextTransfer());

        // Set global next transfer date variable
        GlobalState.getInstance().setDataTransferMechanismStatus(newData);

        return codeToLog;
    }

    public Boolean GetEncompassDataTransferMechanismStatus(String transferId)
    {
        Boolean response = false;
        try {
            // Retrieve the status of the Encompass Data Transfer Mechanism process
            RESTWebServiceHelper rwsh = new RESTWebServiceHelper(getContext());

            response =  rwsh.SendGetDataTransferMechanismStatus(transferId);
        } catch (IOException e)
        {
            this.HandleException(e, this.getContext().getString(R.string.getencompassdatatransfermechanismstatus));
        }
        catch (Throwable throwable) {
            Log.e(this.getContext().getString(R.string.getencompassdatatransfermechanismstatus), "Error while getting Encompass Data Transfer Mechanism status data", throwable);
        }

        return response;
    }

    public void SetEncompassDataTransferMechanismStatus()
    {
        try {
            // Begin the Encompass Data Transfer Mechanism process
            RESTWebServiceHelper rwsh = new RESTWebServiceHelper(getContext());
            String transferId = GlobalState.getInstance().getDataTransferMechanismStatus().getTransferId();
            rwsh.SendSetDataTransferMechanismStatus(transferId);

        } catch (IOException e)
        {
            this.HandleException(e, this.getContext().getString(R.string.setencompassdatatransfermechanismstatus));
        }
    }

    //Use in the admin menu to add a failure record
    public void AddDataTransferFailure()
    {
        AddDataTransferRecord(false);
    }

    //Use in the admin menu to add a failure record
    public void AddDataTransferSuccess()
    {
        AddDataTransferRecord(true);
    }

    //Method used for the admin menu only
    private void AddDataTransferRecord(Boolean status)
    {
        DataTransferMechanismStatusFacade facade = new DataTransferMechanismStatusFacade(this.getContext(),
                GlobalState.getInstance().getCurrentUser());

        DataTransferMechanismStatus data = new DataTransferMechanismStatus();

        // Add record
        data.setTransferId( UUID.randomUUID().toString());
        data.setDateScheduledToTransfer(DateTime.now().toDate());
        data.setDateTransferred(data.getDateScheduledToTransfer());
        data.setDateOfNextTransfer(data.getDateScheduledToTransfer());
        data.setWasSuccessful(status);

        facade.Save(data);
    }

    //Clears data transfer records from the admin menu
    public void ClearDataTransferRecords()
    {
        DataTransferMechanismStatusFacade facade = new DataTransferMechanismStatusFacade(this.getContext(),
                GlobalState.getInstance().getCurrentUser());

        // Clear all transfer records.
        facade.DeleteAllTransfers();

        // Set global next transfer date variable to null
        GlobalState.getInstance().setDataTransferMechanismStatus(null);
    }
}
