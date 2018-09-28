package com.jjkeller.kmbapi.proxydata;

/**
 * Created by T000451 on 9/29/2016.
 */
public class MotionPictureAuthority extends ProxyBase {

    private String motionPictureAuthorityId;
    private String name;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String zipCode;
    private String businessHours;
    private String dotNumber;
    private int companyKey = 0;
    private boolean isActive = false;

    public String getMotionPictureAuthorityId(){ return this.motionPictureAuthorityId; }

    public void setMotionPictureAuthorityId(String value){ this.motionPictureAuthorityId = value; }

    public String getName(){ return this.name; }

    public void setName(String name){ this.name = name; }

    public String getAddressLine1(){ return this.addressLine1; }

    public void setAddressLine1(String addressLine1){ this.addressLine1 = addressLine1; }

    public String getAddressLine2(){ return this.addressLine2; }

    public void setAddressLine2(String addressLine2){ this.addressLine2 = addressLine2; }

    public String getCity(){ return this.city; }

    public void setCity(String city){ this.city = city; }

    public String getState(){ return this.state; }

    public void setState(String state){ this.state = state; }

    public String getZipCode(){ return this.zipCode; }

    public void setZipCode(String zipCode){ this.zipCode = zipCode; }

    public String getBusinessHours(){ return this.businessHours; }

    public void setBusinessHours(String businessHours){ this.businessHours = businessHours; }

    public String getDOTNumber(){ return this.dotNumber; }

    public void setDOTNumber(String dOTNumber){ this.dotNumber = dOTNumber; }

    public int getCompanyKey(){ return this.companyKey; }

    public void setCompanyKey(int companyKey){ this.companyKey = companyKey; }

    public boolean getIsActive() { return isActive; }

    public void setIsActive(boolean value) { isActive = value; }

    public String GetNameAndDOTNumber() {
        StringBuilder nameAndDOTNumber  = new StringBuilder(this.name);

        //Don't append DOT number if DOT number does not exist or equals zero
        if(this.dotNumber != null && !this.dotNumber.equals("") && !this.dotNumber.equals("0")){
            nameAndDOTNumber.append(" (" + this.dotNumber + ")");
        }

        return nameAndDOTNumber.toString();
    }

    @Override
    public String toString() {
        return this.getName();
    }
}
