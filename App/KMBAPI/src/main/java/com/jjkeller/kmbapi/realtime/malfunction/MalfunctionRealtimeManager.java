package com.jjkeller.kmbapi.realtime.malfunction;

import android.util.Log;

import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.kmbeobr.EventRecord;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.realtime.RealtimeProcess;
import com.jjkeller.kmbapi.realtime.ScheduledProcess;
import com.jjkeller.kmbapi.realtime.datadiagnostic.EngineSyncDataDiagnosticRealtimeProcess;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;


/**
 * Malfunction Realtime Manager
 *
 * Handles running the realtime processes that generate and clear Malfunctions.
 * Mimics a Subscribe/Publish model.
 *
 * Created by Charles Stebbins on 2/24/2017.
 */

public class MalfunctionRealtimeManager {
    private static MalfunctionRealtimeManager malfunctionRealtimeManager;
    private static final int PROCESS_POST_INTERVAL_MS = 5000;
    private Collection<RealtimeProcess> realtimeProcesses;


    private MalfunctionRealtimeManager(){
        int gpsMalfunctionMinutes = GlobalState.getInstance().getAppSettings(GlobalState.getInstance().getApplicationContext()).getPositionMalfunctionMinutes();
        int engineSyncMalfunctionMinutes = GlobalState.getInstance().getAppSettings(GlobalState.getInstance().getApplicationContext()).getEngineSyncMalfunctionMinutes();

        realtimeProcesses = new LinkedList<>();
        realtimeProcesses.add(new PositionMalfunctionRealtimeProcess(gpsMalfunctionMinutes));
        realtimeProcesses.add(new EngineSyncMalfunctionRealtimeProcess(engineSyncMalfunctionMinutes));
        realtimeProcesses.add(new EngineSyncDataDiagnosticRealtimeProcess());

        for (RealtimeProcess process : realtimeProcesses){
            process.setup();
        }

    }

    public static boolean isInstantiated() {
        return malfunctionRealtimeManager != null;
    }

    public static synchronized MalfunctionRealtimeManager getInstance(){
        if (malfunctionRealtimeManager == null){
            malfunctionRealtimeManager = new MalfunctionRealtimeManager();
        }
        return malfunctionRealtimeManager;
    }

    public void processEvent(EventRecord eventRecord){
        for (RealtimeProcess process : realtimeProcesses){
            process.processEvent(eventRecord);
        }
    }

    public ScheduledProcess getProcessPostRunnable(){
        return processPostRunnable;
    }

    private ScheduledProcess processPostRunnable = new ScheduledProcess() {
        @Override
        public long getTimeDelay() {
            return PROCESS_POST_INTERVAL_MS;
        }

        @Override
        public TimeUnit getTimeUnit() {
            return TimeUnit.MILLISECONDS;
        }

        @Override
        public void run() {
            Log.v("RealtimeManager", "Running realtime process");
            for (RealtimeProcess process : realtimeProcesses){
                process.post();
            }
        }
    };
}
