package com.jjkeller.kmbapi.geotabengine.interfaces;

import com.jjkeller.kmbapi.CodeBlocks;
import com.jjkeller.kmbapi.HosMessageProcessor.interfaces.IHOSMessage;
import com.jjkeller.kmbapi.HosMessageProcessor.interfaces.IHosMessageProcessor;
import com.jjkeller.kmbapi.kmbeobr.EventRecord;

import java.util.ArrayList;

/**
 * Created by jhm2586 on 9/30/2016.
 */

public interface IGeotabMessageProcessor extends IHosMessageProcessor {

    CodeBlocks.Action2<IHOSMessage, ArrayList<EventRecord>> getGeotabDataProcessed();
    void setGeotabDataProcessed(CodeBlocks.Action2<IHOSMessage, ArrayList<EventRecord>> geotabDataProcessed);

    String getVehicleId();
    void setVehicleId(String vehicleId);
}
