package com.jjkeller.kmbapi.realtime;

import android.util.Log;

import com.jjkeller.kmbapi.controller.ControllerFactory;
import com.jjkeller.kmbapi.controller.EOBR.EobrReader;
import com.jjkeller.kmbapi.controller.EmployeeLogEldMandateController;
import com.jjkeller.kmbapi.controller.FailureController;
import com.jjkeller.kmbapi.realtime.malfunction.ClearTimingMalfunctionProcess;
import com.jjkeller.kmbapi.realtime.malfunction.MalfunctionRealtimeManager;

import java.util.concurrent.ScheduledThreadPoolExecutor;

public class ScheduledEventProcessor  {

    private static final String LOG_TAG = "ScheduledEventProcessor";
    private static final ScheduledThreadPoolExecutor EXECUTOR = new ScheduledThreadPoolExecutor(5);
    private static final ScheduledEventProcessor SCHEDULED_EVENT_PROCESSOR;

    static {
        SCHEDULED_EVENT_PROCESSOR = new ScheduledEventProcessor();
    }

    private ScheduledEventProcessor(){
    }

    protected static ScheduledEventProcessor getInstance(){
        return SCHEDULED_EVENT_PROCESSOR;
    }

    protected void startProcesses(){
        Log.d(LOG_TAG,"starting to process scheduled events");

        EobrReader reader = EobrReader.getInstance();
        FailureController failureController = ControllerFactory.getInstance().getFailureController();
        EmployeeLogEldMandateController employeeLogEldMandateController = ControllerFactory.getInstance().getEmployeeLogEldMandateController();

        ClearTimingMalfunctionProcess process = new ClearTimingMalfunctionProcess(reader, failureController, employeeLogEldMandateController);

        SCHEDULED_EVENT_PROCESSOR.scheduleRepeatingProcess(process);
        SCHEDULED_EVENT_PROCESSOR.scheduleRepeatingProcess(MalfunctionRealtimeManager.getInstance().getProcessPostRunnable());
    }

    protected void stopProcesses(){
        Log.d(LOG_TAG,"stop processing scheduled events.");
        EXECUTOR.shutdown();
    }

    protected void removeScheduledProcess(Runnable scheduledProcess){
        Log.d(LOG_TAG,"removing scheduled process." + scheduledProcess.toString());
        EXECUTOR.remove(scheduledProcess);
    }

    protected void scheduleRepeatingProcess(ScheduledProcess scheduledProcess){
        Log.d(LOG_TAG,"adding scheduled process:" + scheduledProcess.toString() + " time delay: " + scheduledProcess.getTimeDelay());
        EXECUTOR.scheduleAtFixedRate(scheduledProcess, scheduledProcess.getTimeDelay(), scheduledProcess.getTimeDelay(), scheduledProcess.getTimeUnit());
    }

}
