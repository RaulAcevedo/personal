package com.jjkeller.kmbapi.geotab.abstraction.base;

import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.SharedState;
import com.jjkeller.kmbapi.kmbeobr.Thresholds;
import com.jjkeller.kmbapi.common.TestBase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Created by jld5296 on 9/16/16.
 */
public abstract class HosProcessorStateMachineTestBase extends TestBase {

    protected DateFormat datetimeFormat = new SimpleDateFormat("yyyy.MM.dd hh:mm");

    protected SharedState sharedState;

    public HosProcessorStateMachineTestBase() throws Exception {
        sharedState = new SharedState();
        sharedState.setThresholds(new Thresholds(3000, 200, 200, .5f, 5, 1, "00000000-0000-0000-0000-000000000000", 10 ));
    }

}
