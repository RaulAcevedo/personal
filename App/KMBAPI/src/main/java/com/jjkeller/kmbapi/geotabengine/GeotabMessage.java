package com.jjkeller.kmbapi.geotabengine;

/**
 * Created by jhm2586 on 8/31/2016.
 */
public class GeotabMessage extends HOSMessage {
    private byte status;
    private int vehicleId;
//    private boolean isProcessed;
//    private long eventDataRecordId;

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
        this.setStatusBits(status);
    }

    public int getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }

//    public boolean isProcessed() {
//        return isProcessed;
//    }
//
//    public void setProcessed(boolean processed) {
//        isProcessed = processed;
//    }
//
//    public long getEventDataRecordId() {
//        return eventDataRecordId;
//    }
//
//    public void setEventDataRecordId(long eventDataRecordId) {
//        this.eventDataRecordId = eventDataRecordId;
//    }

    private void setStatusBits(byte status)
    {
        super.setGpsValid((status & 0x01) != 0);
        super.setIgnitionOn((status & 0x02) != 0);
        super.setEngineActivityDetected((status & 0x04) != 0);
        super.setDatetimeValid((status & 0x08) != 0);
    }
}
