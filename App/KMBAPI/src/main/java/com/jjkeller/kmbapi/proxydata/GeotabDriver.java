package com.jjkeller.kmbapi.proxydata;

/**
 * Created by rab5795 on 3/30/18.
 */

public class GeotabDriver {

    private String DriverId;
    private String eobrSerialNumber;
    private String ResponseMessage;


    public String getResponseMessage() {
        return ResponseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        ResponseMessage = responseMessage;
    }

    public String getDriverId() {
        return DriverId;
    }

    public void setDriverId(String driverId) {
        DriverId = driverId;
    }


    public String getEobrSerialNumber() {
        return eobrSerialNumber;
    }

    public void setEobrSerialNumber(String eobrSerialNumber) {
        this.eobrSerialNumber = eobrSerialNumber;
    }



}
