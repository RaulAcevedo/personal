package com.jjkeller.kmbapi.controller.interfaces;

import com.jjkeller.kmbapi.proxydata.UnassignedDrivingPeriod;

/**
 * Created by jld5296 on 10/27/16.
 */
public interface IEnforceMinimumLengthStrategy {
    boolean execute(UnassignedDrivingPeriod drivingPeriod);
}
