package com.jjkeller.kmbapi.geotabengine.events;

/**
 * Created by jld5296 on 9/22/16.
 *
 *
 * */
public class GeoTabSyntheticEventRecordData {

    private final int _driverIdCrc;

    public GeoTabSyntheticEventRecordData(int driverIdCrc){
        _driverIdCrc = driverIdCrc;
    }

    /**
     * checksum of driver's employeeId (From CalcCRC.Calculate(driverId, driverId.length());)
     */
    public int getDriverIdCrc(){
        return _driverIdCrc;
    }

    public int getEventData(){ return 0; } // Not really used on IGN_ON IGN_OFF


}
