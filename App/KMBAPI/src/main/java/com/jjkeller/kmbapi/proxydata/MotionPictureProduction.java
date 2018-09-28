package com.jjkeller.kmbapi.proxydata;

import android.content.Context;

import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.MotionPictureController;

/**
 * Created by gav6058 on 9/28/2016.
 */
public class MotionPictureProduction extends ProxyBase {

    private String motionPictureProductionId;
    private String name;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String zipCode;
    private String businessHours;
    private String motionPictureAuthorityId;
    private MotionPictureAuthority motionPictureAuthority;
    private int companyKey = 0;
    private boolean isActive = false;

    public String getMotionPictureProductionId() { return motionPictureProductionId; }
    public void setMotionPictureProductionId(String value) { motionPictureProductionId = value; }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getAddressLine1() {
        return addressLine1;
    }
    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }
    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getCity() {
        return city;
    }
    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }
    public void setState(String state) {
        this.state = state;
    }

    public String getZipCode() {
        return zipCode;
    }
    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getBusinessHours() {
        return businessHours;
    }
    public void setBusinessHours(String businessHours) {
        this.businessHours = businessHours;
    }

    public String getMotionPictureAuthorityId() {
        return motionPictureAuthorityId;
    }
    public void setMotionPictureAuthorityId(String value) {
        this.motionPictureAuthorityId = value;
    }

    public int getCompanyKey() {
        return companyKey;
    }
    public void setCompanyKey(int companyKey) {
        this.companyKey = companyKey;
    }

    public boolean getIsActive() { return isActive; }
    public void setIsActive(boolean value) { isActive = value; }

    public MotionPictureAuthority getMotionPictureAuthority() {
        Context ctx = GlobalState.getInstance().getApplicationContext();
        MotionPictureController controller = new MotionPictureController(ctx);
        MotionPictureAuthority authority = controller.GetAuthorityByAuthorityId(this.getMotionPictureAuthorityId());
        return authority;
    }

    @Override
    public String toString() {
        return getCustomName();
    }

    private String getCustomName() {
        String name = this.getName();
        if (!name.equals("(Not Specified)")) {
            if(getMotionPictureAuthority() == null) {
                name += " (" + getMotionPictureAuthority().getName() + ")";
            }
        }
        return name;
    }
}
