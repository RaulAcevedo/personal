package com.jjkeller.kmbapi.proxydata;

/**
 * Created by djm5973 on 10/11/2016.
 */

public class MotionPictureProductionList {
    ///////////////////////////////////////////////////////////////////////////////////////
    // private members
    ///////////////////////////////////////////////////////////////////////////////////////
    private MotionPictureProduction[] motionPictureProductions;

    ///////////////////////////////////////////////////////////////////////////////////////
    // public get/set methods
    ///////////////////////////////////////////////////////////////////////////////////////
    public MotionPictureProduction[] getMotionPictureProductions()
    {
        return motionPictureProductions;
    }
    public void setMotionPictureProductions(MotionPictureProduction[] values)
    {
        this.motionPictureProductions = values;
    }
}
