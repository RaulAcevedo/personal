package com.jjkeller.kmbapi.kmbeobr;

import com.jjkeller.kmbapi.common.NumberUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class StatusBuffer {
    private long timecode;
    private int resets;
    private int cumulativeUptimeSeconds;
    private int uptimeSeconds;
    private int numberOfTrips;
    private int runTimeSeconds;
    private int ignitionOnTimeSeconds;
    private int ignitionOffTimeSeconds;
    private float odometerKilometers;
    private int totalConsoleCommandCount;
    private long bluetoothPacketTxCount;
    private long bluetoothPacketRxCount;
    private long bluetoothPacketTxByteCount;
    private long bluetoothPacketRxByteCount;
    private double cumulativeMAFGrams;
    private int overallComponentStatus;
    private int driverId;
    private int activeBusType;
    private int lastEobrId;
    private long eobrReferenceTimestamp;
    private long eventReferenceTimestamp;
    private long tripReferenceTimestamp;
    private long histogramReferenceTimestamp;
    private long dtcReferenceTimestamp;
    private double syntheticOdometerMeters;
    private int odometerOffsetMeters;
    private long cumulativeIMAP;
    private double totalFuelUsedLiters;
    private int engineOnTimeSeconds;

    public static StatusBuffer FromByteBuffer(ByteBuffer payload) {
        payload.order(ByteOrder.LITTLE_ENDIAN);

        StatusBuffer result = new StatusBuffer();
        result.setTimecode(payload.getLong());
        result.setResets(payload.getInt());
        result.setCumulativeUptimeSeconds(payload.getInt());
        result.setUptimeSeconds(payload.getInt());
        result.setNumberOfTrips(payload.getInt());
        result.setRunTimeSeconds(payload.getInt());
        result.setIgnitionOnTimeSeconds(payload.getInt());
        result.setIgnitionOffTimeSeconds(payload.getInt());
        result.setOdometerKilometers(payload.getInt() / 10f); // stored as (km * 10)
        result.setTotalConsoleCommandCount(payload.getInt());
        result.setBluetoothPacketTxCount(NumberUtil.toUnsignedLong(payload.getInt()));
        result.setBluetoothPacketRxCount(NumberUtil.toUnsignedLong(payload.getInt()));
        result.setBluetoothPacketTxByteCount(NumberUtil.toUnsignedLong(payload.getInt()));
        result.setBluetoothPacketRxByteCount(NumberUtil.toUnsignedLong(payload.getInt()));
        result.setCumulativeMAFGrams(payload.getLong() / 100.0); // stored as (gm * 100)
        result.setOverallComponentStatus(payload.getInt());
        result.setDriverId(payload.getInt());
        result.setActiveBusType(payload.getInt());
        result.setLastEobrId(payload.getInt());
        result.setEobrReferenceTimestamp(payload.getLong());
        result.setEventReferenceTimestamp(payload.getLong());
        result.setHistogramReferenceTimestamp(payload.getLong());
        result.setTripReferenceTimestamp(payload.getLong());
        result.setDtcReferenceTimestamp(payload.getLong());
        result.setSyntheticOdometerMeters(payload.getLong() / 1000.0); // stored as (m * 1000)
        result.setOdometerOffsetMeters(payload.getInt());
        result.setCumulativeIMAP(payload.getLong());
        result.setTotalFuelUsedLiters(NumberUtil.toUnsignedLong(payload.getInt()) / 100.0); // stored as (L * 100)
        result.setEngineOnTimeSeconds(payload.getInt());
        return result;
    }

    public long getTimecode() {
        return timecode;
    }

    public void setTimecode(long timecode) {
        this.timecode = timecode;
    }

    public int getResets() {
        return resets;
    }

    public void setResets(int resets) {
        this.resets = resets;
    }

    public int getCumulativeUptimeSeconds() {
        return cumulativeUptimeSeconds;
    }

    public void setCumulativeUptimeSeconds(int cumulativeUptimeSeconds) {
        this.cumulativeUptimeSeconds = cumulativeUptimeSeconds;
    }

    public int getUptimeSeconds() {
        return uptimeSeconds;
    }

    public void setUptimeSeconds(int uptimeSeconds) {
        this.uptimeSeconds = uptimeSeconds;
    }

    public int getNumberOfTrips() {
        return numberOfTrips;
    }

    public void setNumberOfTrips(int numberOfTrips) {
        this.numberOfTrips = numberOfTrips;
    }

    public int getRunTimeSeconds() {
        return runTimeSeconds;
    }

    public void setRunTimeSeconds(int runTimeSeconds) {
        this.runTimeSeconds = runTimeSeconds;
    }

    public int getIgnitionOnTimeSeconds() {
        return ignitionOnTimeSeconds;
    }

    public void setIgnitionOnTimeSeconds(int ignitionOnTimeSeconds) {
        this.ignitionOnTimeSeconds = ignitionOnTimeSeconds;
    }

    public int getIgnitionOffTimeSeconds() {
        return ignitionOffTimeSeconds;
    }

    public void setIgnitionOffTimeSeconds(int ignitionOffTimeSeconds) {
        this.ignitionOffTimeSeconds = ignitionOffTimeSeconds;
    }

    public float getOdometerKilometers() {
        return odometerKilometers;
    }

    public void setOdometerKilometers(float odometerKilometers) {
        this.odometerKilometers = odometerKilometers;
    }

    public int getTotalConsoleCommandCount() {
        return totalConsoleCommandCount;
    }

    public void setTotalConsoleCommandCount(int totalConsoleCommandCount) {
        this.totalConsoleCommandCount = totalConsoleCommandCount;
    }

    public long getBluetoothPacketTxCount() {
        return bluetoothPacketTxCount;
    }

    public void setBluetoothPacketTxCount(long bluetoothPacketTxCount) {
        this.bluetoothPacketTxCount = bluetoothPacketTxCount;
    }

    public long getBluetoothPacketRxCount() {
        return bluetoothPacketRxCount;
    }

    public void setBluetoothPacketRxCount(long bluetoothPacketRxCount) {
        this.bluetoothPacketRxCount = bluetoothPacketRxCount;
    }

    public long getBluetoothPacketTxByteCount() {
        return bluetoothPacketTxByteCount;
    }

    public void setBluetoothPacketTxByteCount(long bluetoothPacketTxByteCount) {
        this.bluetoothPacketTxByteCount = bluetoothPacketTxByteCount;
    }

    public long getBluetoothPacketRxByteCount() {
        return bluetoothPacketRxByteCount;
    }

    public void setBluetoothPacketRxByteCount(long bluetoothPacketRxByteCount) {
        this.bluetoothPacketRxByteCount = bluetoothPacketRxByteCount;
    }

    public double getCumulativeMAFGrams() {
        return cumulativeMAFGrams;
    }

    public void setCumulativeMAFGrams(double cumulativeMAFGrams) {
        this.cumulativeMAFGrams = cumulativeMAFGrams;
    }

    public int getOverallComponentStatus() {
        return overallComponentStatus;
    }

    public void setOverallComponentStatus(int overallComponentStatus) {
        this.overallComponentStatus = overallComponentStatus;
    }

    public int getDriverId() {
        return driverId;
    }

    public void setDriverId(int driverId) {
        this.driverId = driverId;
    }

    public int getActiveBusType() {
        return activeBusType;
    }

    public void setActiveBusType(int activeBusType) {
        this.activeBusType = activeBusType;
    }

    public int getLastEobrId() {
        return lastEobrId;
    }

    public void setLastEobrId(int lastEobrId) {
        this.lastEobrId = lastEobrId;
    }

    public long getEobrReferenceTimestamp() {
        return eobrReferenceTimestamp;
    }

    public void setEobrReferenceTimestamp(long eobrReferenceTimestamp) {
        this.eobrReferenceTimestamp = eobrReferenceTimestamp;
    }

    public long getEventReferenceTimestamp() {
        return eventReferenceTimestamp;
    }

    public void setEventReferenceTimestamp(long eventReferenceTimestamp) {
        this.eventReferenceTimestamp = eventReferenceTimestamp;
    }

    public long getTripReferenceTimestamp() {
        return tripReferenceTimestamp;
    }

    public void setTripReferenceTimestamp(long tripReferenceTimestamp) {
        this.tripReferenceTimestamp = tripReferenceTimestamp;
    }

    public long getHistogramReferenceTimestamp() {
        return histogramReferenceTimestamp;
    }

    public void setHistogramReferenceTimestamp(long histogramReferenceTimestamp) {
        this.histogramReferenceTimestamp = histogramReferenceTimestamp;
    }

    public long getDtcReferenceTimestamp() {
        return dtcReferenceTimestamp;
    }

    public void setDtcReferenceTimestamp(long dtcReferenceTimestamp) {
        this.dtcReferenceTimestamp = dtcReferenceTimestamp;
    }

    public double getSyntheticOdometerMeters() {
        return syntheticOdometerMeters;
    }

    public void setSyntheticOdometerMeters(double syntheticOdometerMeters) {
        this.syntheticOdometerMeters = syntheticOdometerMeters;
    }

    public int getOdometerOffsetMeters() {
        return odometerOffsetMeters;
    }

    public void setOdometerOffsetMeters(int odometerOffsetMeters) {
        this.odometerOffsetMeters = odometerOffsetMeters;
    }

    public long getCumulativeIMAP() {
        return cumulativeIMAP;
    }

    public void setCumulativeIMAP(long cumulativeIMAP) {
        this.cumulativeIMAP = cumulativeIMAP;
    }

    public double getTotalFuelUsedLiters() {
        return totalFuelUsedLiters;
    }

    public void setTotalFuelUsedLiters(double totalFuelUsedLiters) {
        this.totalFuelUsedLiters = totalFuelUsedLiters;
    }

    public int getEngineOnTimeSeconds() {
        return engineOnTimeSeconds;
    }

    public void setEngineOnTimeSeconds(int engineOnTimeSeconds) {
        this.engineOnTimeSeconds = engineOnTimeSeconds;
    }
}
