package com.jjkeller.kmbapi.common;

import android.util.Log;

import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.kmbeobr.*;
import com.jjkeller.kmbapi.kmbeobr.Constants;

import java.util.Locale;

/**
 * Created by T000684 on 7/19/2017.
 */
public class TabDataConversionUtil {

    private TabDataConversionUtil(){

    }

    /**
     * ELDMandate should record odometer values rounded to the nearest whole mile.
     * AOBRD should record odometer values to the nearest tenth of a mile.
     * @param tabOdometerValue  32-bit signed integer value of Tab odometer in KM * 10.
     * @return                  Resulting mile reading of type float.
     */
    public static float convertOdometerReading(int tabOdometerValue){
        float value = Math.round((((float)tabOdometerValue / 10f) * Constants.MILES_PER_KILOMETER) * 10f) / 10f;
        if(GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()){
            int roundedValue =(int)(value  * 10) / 10;
            value = (float) roundedValue;
        }
        return value;
    }

    /**
     * ELDMandate should record odometer values rounded to the nearest whole mile.
     * AOBRD should record odometer values to the nearest tenth of a mile.
     * @param tabOdometerValue  64-bit integer value of Tab odometer in KM * 10.
     * @return                  Resulting mile reading of type float.
     */
    public static float convertOdometerReading(long tabOdometerValue){
        float value = Math.round((((float)tabOdometerValue / 10f) * Constants.MILES_PER_KILOMETER) * 10f) / 10f;
        if(GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()){
            long roundedValue =(long)(value  * 10) / 10;
            value = (float) roundedValue;
        }
        return value;
    }

    /**
     * Odometer values need to display 1 decimal place in AOBRD and none for Mandate driving.
     * @param odometer  Float value of odometer to convert to a displayable String.
     * @return          Resulting display value for the odometer value provided.
     */
    public static String getOdometerValueForDisplay(float odometer) {
        if(GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
            return String.format(Locale.US, "%d", (int) odometer);
        } else {
            return String.format(Locale.US, "%.1f", odometer);
        }
    }

}
