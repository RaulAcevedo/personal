package com.jjkeller.kmbapi.controller.utility;

import android.content.res.Resources;
import android.util.DisplayMetrics;

/**
 * Created by T000694 on 5/9/2017.
 */
public final class DeviceUtilities {

    private DeviceUtilities(){}

    public static float ConvertDpToPixel(float dp){
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);

        return Math.round(px);
    }
}
