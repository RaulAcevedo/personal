package com.jjkeller.kmb.adapters;

/**
 * Represents info about Team Driver spinner data
 */
public class TeamDriverItem {
    private long _primaryKey;
    private String _employeeCode;
    private String _kmbUserName;
    private String _displayName;

    public long getPrimaryKey() {
        return _primaryKey;
    }

    public String getEmployeeCode() {
        return _employeeCode;
    }

    public String getKMBUserName() {
        return _kmbUserName;
    }

    public String getDisplayName() {
        return _displayName;
    }

    public TeamDriverItem(long primaryKey, String employeeCode, String kmbUserName, String displayName) {
        this._primaryKey = primaryKey;
        this._employeeCode = employeeCode;
        this._kmbUserName = kmbUserName;
        this._displayName = displayName;
    }
}