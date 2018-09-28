package com.jjkeller.kmbapi.kmbeobr;

public class DriverCountResponse {
    private int driverCount = 0;
    private int[] driverIds = new int[16];

    public int getDriverCount() {
        return driverCount;
    }

    public void setDriverCount(int driverCount) {
        this.driverCount = driverCount;
    }

    public int[] getDriverIds() {
        return driverIds;
    }

    public void setDriverIds(int[] driverIds) {
        this.driverIds = driverIds;
    }
}
