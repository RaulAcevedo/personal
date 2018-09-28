package com.jjkeller.kmbapi.realtime;

import com.jjkeller.kmbapi.kmbeobr.EventRecord;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;

/**
 * Realtime Process
 *
 * Interface for a process that needs pseudo-realtime execution.
 *
 * Created by Charles Stebbins on 2/24/2017.
 */

public interface RealtimeProcess {

    /**
     * Setup the RealtimeProcess
     */
    void setup();

    /**
     * Process a single event record.
     * @param eventRecord
     */
    void processEvent(EventRecord eventRecord);

    /**
     * Runs the realtime process.
     * Expect this method to be run on a frequent timer.
     */
    void post();
}
