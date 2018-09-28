package com.jjkeller.kmbapi.configuration;

/**
 * Created by eth6134 on 10/6/2016.
 */

public class UserState {

    private boolean _isInPersonalConveyanceDutyStatus = false;
    public boolean getIsInPersonalConveyanceDutyStatus()
    {
        return _isInPersonalConveyanceDutyStatus;
    }
    public void setIsInPersonalConveyanceDutyStatus(boolean value)
    {
        _isInPersonalConveyanceDutyStatus = value;
    }

    private boolean _isInYardMoveDutyStatus = false;

    public boolean getIsInYardMoveDutyStatus()
    {
        return _isInYardMoveDutyStatus;
    }

    public void setIsInYardMoveDutyStatus(boolean value)
    {
        _isInYardMoveDutyStatus = value;
    }

    private boolean _isInHyrailDutyStatus = false;
    public boolean getIsInHyrailDutyStatus()
    {
        return _isInHyrailDutyStatus;
    }
    public void setIsInHyrailDutyStatus(boolean value)
    {
        _isInHyrailDutyStatus = value;
    }

    private boolean _isInNonRegDrivingDutyStatus = false;
    public boolean getIsInNonRegDrivingDutyStatus() { return _isInNonRegDrivingDutyStatus; }
    public void setIsInNonRegDrivingDutyStatus(boolean value) { _isInNonRegDrivingDutyStatus = value; }

    private String _currentShipmentInfo;
    public String get_currentShipmentInfo() { return _currentShipmentInfo;}
    public void  set_currentShipmentInfo(String value){_currentShipmentInfo = value;}

    private String _currentTrailerNumbers;
    public String get_currentTrailerNumbers() { return _currentTrailerNumbers;}
    public void set_currentTrailerNumbers(String value){
        _currentTrailerNumbers = value;}

    private String _motionPictureProductionId;
    public String getMotionPictureProductionId() { return _motionPictureProductionId; }
    public void setMotionPictureProductionId(String value) { _motionPictureProductionId = value; }

    private String _motionPictureAuthorityId;
    public String getMotionPictureAuthorityId() { return _motionPictureAuthorityId; }
    public void setMotionPictureAuthorityId(String value) { _motionPictureAuthorityId = value; }
}
