package com.jjkeller.kmbapi.kmbeobr;

/**
 * Created by ief5781 on 9/9/16.
 */
public class TripDistanceHours {
    private int accummulatedEngineMinutes;
    private int accummulatedMiles;

    public int getAccummulatedMiles() {
        return accummulatedMiles;
    }

    public void setAccummulatedMiles(int accummulatedMiles) {
        this.accummulatedMiles = accummulatedMiles;
    }

    public int getAccummulatedEngineMinutes() {
        return accummulatedEngineMinutes;
    }

    public void setAccummulatedEngineMinutes(int accummulatedEngineMinutes) {
        this.accummulatedEngineMinutes = accummulatedEngineMinutes;
    }
}
