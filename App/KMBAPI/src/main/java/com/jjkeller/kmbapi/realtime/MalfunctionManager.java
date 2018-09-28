package com.jjkeller.kmbapi.realtime;

import android.os.Bundle;
import android.util.Log;

import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.ControllerFactory;
import com.jjkeller.kmbapi.controller.EOBR.EobrReader;
import com.jjkeller.kmbapi.controller.EmployeeLogEldMandateController;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.controller.utility.ErrorLogHelper;
import com.jjkeller.kmbapi.enums.Malfunction;
import com.jjkeller.kmbapi.featuretoggle.IFeatureToggleService;
import com.jjkeller.kmbapi.realtime.malfunction.DataRecordingMalfunction;
import com.jjkeller.kmbapi.realtime.malfunction.TimingMalfunction;

import java.util.Date;

public class MalfunctionManager {

    private static final MalfunctionManager MALFUNCTION_MANAGER;

    private DataRecordingMalfunction dataRecordingMalfunction;
    private TimingMalfunction timingMalfunction;
    private IFeatureToggleService featureToggleService;

    static {
        MALFUNCTION_MANAGER = new MalfunctionManager();
    }

    private MalfunctionManager(){
        init();
    }

    //didn't like doing this... but the singleton doesn't play nice with testing. plug for dependency injection.
    public void init(){
        dataRecordingMalfunction = new DataRecordingMalfunction();
        EobrReader reader = EobrReader.getInstance();
        EmployeeLogEldMandateController employeeLogEldMandateController = ControllerFactory.getInstance().getEmployeeLogEldMandateController();
        timingMalfunction = new TimingMalfunction(reader, employeeLogEldMandateController);
        featureToggleService = GlobalState.getInstance().getFeatureService();
    }

    public static MalfunctionManager getInstance(){
        return MALFUNCTION_MANAGER;
    }

    public void checkDataRecordingMalfunction(int queryRecordId, int resultRecordId, Integer statusBufferNumberOfTrips){
        if(featureToggleService.getIsEldMandateEnabled()) {
            dataRecordingMalfunction.checkDataRecordingMalfunction(queryRecordId, resultRecordId, statusBufferNumberOfTrips);
        }
    }

    public void startScheduledProcessesForMalfunctions(){
        if(featureToggleService.getIsEldMandateEnabled()) {
            ScheduledEventProcessor.getInstance().startProcesses();
        }
    }

    public void stopScheduledProcessesForMalfunctions(){
        if(featureToggleService.getIsEldMandateEnabled()) {
            ScheduledEventProcessor.getInstance().stopProcesses();
        }
    }

    public Bundle validateELDMandateTimingCompliance(Date sourceOfTruthTime, Date tabClock){
        if(featureToggleService.getIsEldMandateEnabled()){
            return timingMalfunction.validateTimingMalfunction(sourceOfTruthTime, tabClock);
        }
        return null;
    }

    /** should this be all Malfunction types??? **/
    private static final Malfunction[] CLEAR_AT_LOGOUT = new Malfunction[] {Malfunction.TIMING_COMPLIANCE, Malfunction.DATA_RECORDING_COMPLIANCE, Malfunction.POSITIONING_COMPLIANCE, Malfunction.ENGINE_SYNCHRONIZATION_COMPLIANCE};

    /**
     * clears active malfunctions when releasing the tab.
     * if the user connects to a new tab, the will no longer be in a malfunctioning state.
     * if the user reconnects to the same TAB, the malfunction should be detected again if the TAB is still malfunctioning.
     */
    public void clearActiveMalfunctionsWhenDoneWithTab(){
        Log.i("TimeSync", "Eld Mandate malfunctions clearing at logout");
        if (featureToggleService.getIsEldMandateEnabled()) {
            for(Malfunction malfunction : CLEAR_AT_LOGOUT) {
                try {
                    ControllerFactory.getInstance().getEmployeeLogEldMandateController().clearMalfunctionForLoggedInUsers(
                            DateUtility.getCurrentDateTimeUTC()
                            , malfunction
                    );
                } catch (Throwable t) {
                    Log.e("TimeSync", "error clearing malfunction at logout", t);
                }
            }
        }
    }

    public void clearActiveDataDiagnosticsWhenDoneWithTab(){
        Log.i("TimeSync", "Eld Mandate malfunctions clearing at logout");
        if (featureToggleService.getIsEldMandateEnabled()){
            try{
                ControllerFactory.getInstance().getEmployeeLogEldMandateController().clearDataDiagnosticForLoggedInUsers(DateUtility.getCurrentDateTimeUTC());
            } catch (Throwable throwable) {
                ErrorLogHelper.RecordException(throwable);
            }
        }
    }
}
