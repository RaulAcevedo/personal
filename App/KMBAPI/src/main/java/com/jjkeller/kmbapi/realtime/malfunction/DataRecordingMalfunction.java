package com.jjkeller.kmbapi.realtime.malfunction;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.ControllerFactory;
import com.jjkeller.kmbapi.controller.EmployeeLogEldMandateController;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.controller.utility.ErrorLogHelper;
import com.jjkeller.kmbapi.enums.Malfunction;
import com.jjkeller.kmbapi.featuretoggle.IFeatureToggleService;

/**
 * Handles creating and clearing Data Recording Malfunction
 *
 * Created by T000684 on 4/7/2017.
 */

public class DataRecordingMalfunction {

    private EmployeeLogEldMandateController eldMandateController;
    private IFeatureToggleService featureToggleService;

    private boolean dataRecordingIssue;
    private boolean isStorageLow;

    public DataRecordingMalfunction(){
        GlobalState globalContext = GlobalState.getInstance();

        eldMandateController = ControllerFactory.getInstance().getEmployeeLogEldMandateController();
        featureToggleService = globalContext.getFeatureService();

        if (featureToggleService.getIsEldMandateEnabled()) {
            isStorageLow = globalContext.registerReceiver(null, new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW)) != null;
            startBroadcastReceiver(globalContext);
            checkToRecordDataMalfunction();
        }
    }

    private void startBroadcastReceiver(Context context){
        context.registerReceiver(lowMemoryReceiver, new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW));
        context.registerReceiver(lowMemoryReceiver, new IntentFilter(Intent.ACTION_DEVICE_STORAGE_OK));
    }

    public void checkDataRecordingMalfunction(int queryRecordId,  int resultRecordId, Integer statusBufferNumberOfTrips) {
        if (queryRecordId != 0) {
            dataRecordingIssue = false;
            if (this.hasInvalidRecordIdForReadingValidation(queryRecordId, resultRecordId, statusBufferNumberOfTrips)) {
                dataRecordingIssue = resultRecordId == 0;
            }
            checkToRecordDataMalfunction();
        }
    }

    private boolean hasInvalidRecordIdForReadingValidation(int queryRecordId,  int resultRecordId, Integer statusBufferNumberOfTrips) {
        // Ignition_On events generated under 6.88.0, after firmware upgrade to newer version (ELD Mandate Version: 6.88.113) will flag a false positive for every one
        // of these events because the Record Id under the Event Record ID in the ELD is stored as a pointer in newer versions of Firmware. The old firmware stored this
        // field as the actual value of the Trip Event Record ID. Therefore, when we read the pointer location on an AOBRD Ignition_On record, we receive 0x0000 or
        // RecordId = 0 because it tries to read from an invalid address location.
        return resultRecordId <= 0 && statusBufferNumberOfTrips != null && queryRecordId > statusBufferNumberOfTrips.intValue();
    }

    private BroadcastReceiver lowMemoryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_DEVICE_STORAGE_LOW)) {
                isStorageLow = true;
            } else if (intent.getAction().equals(Intent.ACTION_DEVICE_STORAGE_OK)) {
                isStorageLow = false;
            }
            checkToRecordDataMalfunction();
        }
    };

    private void checkToRecordDataMalfunction(){
        if (! featureToggleService.getIsEldMandateEnabled()) {
            return;
        }

        try {
            if (isStorageLow || dataRecordingIssue) {
                eldMandateController.createMalfunctionForLoggedInUsers(DateUtility.getCurrentDateTimeUTC(), Malfunction.DATA_RECORDING_COMPLIANCE);
            } else {
                eldMandateController.clearMalfunctionForLoggedInUsers(DateUtility.getCurrentDateTimeUTC(), Malfunction.DATA_RECORDING_COMPLIANCE);
            }
        } catch (Throwable throwable){
            //Log it, this will be executed somewhat frequently due to checkDataRecordingMalfunction getting hit frequently.
            ErrorLogHelper.RecordException(throwable);
        }
    }
}
