package com.jjkeller.kmbapi.enums;

import android.content.res.Resources;

import com.jjkeller.kmbapi.R;

/**
 * Malfunction Enum - handles mapping between codes and user facing text for Malfunctions.
 *
 * Created by T000684 on 3/1/2017.
 */

public enum Malfunction implements Describable {
    NONE(0,"None", R.string.malfunction_none, R.string.malfunction_none),
    POWER_COMPLIANCE(1, "P", R.string.malfunction_powercompliance, R.string.malfunction_powercompliance_desc),
    ENGINE_SYNCHRONIZATION_COMPLIANCE(2, "E", R.string.malfunction_enginesynchronizationcompliance, R.string.malfunction_enginesynchronizationcompliance_desc),
    TIMING_COMPLIANCE(3, "T", R.string.malfunction_timingcomplance, R.string.malfunction_timingcomplance_desc),
    POSITIONING_COMPLIANCE(4, "L", R.string.malfunction_positioningcompliance, R.string.malfunction_positioningcompliance_desc),
    DATA_RECORDING_COMPLIANCE(5, "R", R.string.malfunction_datarecordingcompliance, R.string.malfunction_datarecordingcompliance_desc),
    DATA_TRANSFER_COMPLIANCE(6, "S", R.string.malfunction_datatransfercompliance, R.string.malfunction_datatransfercompliance_desc),
    OTHER_ELD_DETECTED(7, "O", R.string.malfunction_otherelddetected,R.string.malfunction_otherelddetected_desc );

    private int malfunctionCode;
    private String dmoValue;
    private int descriptionKey;
    private int fullDescriptionKey;

    Malfunction(final int malfunctionValue, final String dmoMalfunctionValue, int descripKey, int fullDescKey){
        this.malfunctionCode = malfunctionValue;
        this.dmoValue = dmoMalfunctionValue;
        this.descriptionKey = descripKey;
        this.fullDescriptionKey = fullDescKey;
    }

    public static Malfunction valueOfDMOEnum(String dmovalue) throws IllegalArgumentException {
            for (Malfunction malfunction : values()){
                    if (malfunction.getDmoValue().equals(dmovalue)){
                            return malfunction;
                    }
            }
            throw new IllegalArgumentException("Enum value undefined");
    }

    public String getDmoValue() {
            return dmoValue;
    }

    public int getMalfunctionCode() {
            return malfunctionCode;
    }

    public int getDescriptionKey() {
            return descriptionKey;
    }

    public int getFullDescriptionKey() {
            return fullDescriptionKey;
    }


}

