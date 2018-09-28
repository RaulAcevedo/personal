package com.jjkeller.kmbapi.controller.utility;

import com.jjkeller.kmbapi.proxydata.EngineRecordList;
import com.jjkeller.kmbapi.proxydata.EventDataRecordList;
import com.jjkeller.kmbapi.proxydata.RoutePositionList;
import com.jjkeller.kmbapi.proxydata.TripRecordList;

import java.io.IOException;

public interface IWebAPIServiceHelper {
    void SubmitEventDataRecords(EventDataRecordList eventDataRecords) throws IOException;
    void SubmitEngineRecords(EngineRecordList engineRecords) throws IOException;
    void SubmitRoutePositions(RoutePositionList routePositions) throws IOException;
    void SubmitTripRecords(TripRecordList tripRecords) throws IOException;
}
