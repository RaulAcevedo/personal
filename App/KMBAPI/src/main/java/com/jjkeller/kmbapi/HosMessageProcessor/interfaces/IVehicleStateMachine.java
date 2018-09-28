package com.jjkeller.kmbapi.HosMessageProcessor.interfaces;

import com.jjkeller.kmbapi.kmbeobr.EventRecord;
import com.jjkeller.kmbapi.kmbeobr.Thresholds;

import java.util.ArrayList;

/**
 * Created by ief5781 on 8/31/16.
 */
public interface IVehicleStateMachine {
    void setThresholds(Thresholds thresholds);

    ArrayList<EventRecord> processMessage(IHOSMessage message);

    void resetVehicleState();
}
