package com.jjkeller.kmbapi.kmbeobr;

public class DistanceAndHours {
    private int accumulatedVehicleMiles;
    private int totalVehicleMiles;
    private double engineHours;

    public int getAccumulatedVehicleMiles() {
        return accumulatedVehicleMiles;
    }

    public void setAccumulatedVehicleMiles(int accumulatedVehicleMiles) {
        this.accumulatedVehicleMiles = accumulatedVehicleMiles;
    }

    public int getTotalVehicleMiles() {
        return totalVehicleMiles;
    }

    public void setTotalVehicleMiles(int totalVehicleMiles) {
        this.totalVehicleMiles = totalVehicleMiles;
    }

    public double getEngineHours() {
        return engineHours;
    }

    public void setEngineHours(double engineHours) {
        this.engineHours = engineHours;
    }
}
