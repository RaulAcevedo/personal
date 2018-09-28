package com.jjkeller.kmbapi.proxydata;

/**
 * Created by djm5973 on 10/11/2016.
 */

public class MotionPictureAuthorityList {
    ///////////////////////////////////////////////////////////////////////////////////////
    // private members
    ///////////////////////////////////////////////////////////////////////////////////////
    private MotionPictureAuthority[] motionPictureAuthorities;

    ///////////////////////////////////////////////////////////////////////////////////////
    // public get/set methods
    ///////////////////////////////////////////////////////////////////////////////////////
    public MotionPictureAuthority[] getMotionPictureAuthorities()
    {
        return motionPictureAuthorities;
    }
    public void setMotionPictureAuthorities(MotionPictureAuthority[] values)
    {
        this.motionPictureAuthorities = values;
    }
}
