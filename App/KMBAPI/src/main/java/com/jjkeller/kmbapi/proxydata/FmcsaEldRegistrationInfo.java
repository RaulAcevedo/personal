package com.jjkeller.kmbapi.proxydata;

import com.google.gson.annotations.SerializedName;

public class FmcsaEldRegistrationInfo {
    @SerializedName("Name")
    private String name;
    public String getName() { return name; }
    public void setName(String value) { this.name = value; }

    @SerializedName("Identifier")
    private String identifier;
    public String getIdentifier() { return identifier; }
    public void setIdentifier(String value) { this.identifier = value; }

    @SerializedName("FirmwareType")
    private String firmwareType;
    public String getFirmwareType() { return firmwareType; }
    public void setFirmwareType(String value) { this.firmwareType = value; }

    @SerializedName("MinAppVersion")
    private String minAppVersion;
    public String getMinAppVersion() { return minAppVersion; }
    public void setMinAppVersion(String value) { this.minAppVersion = value; }

    @SerializedName("MaxAppVersion")
    private String maxAppVersion;
    public String getMaxAppVersion() { return maxAppVersion; }
    public void setMaxAppVersion(String value) { this.maxAppVersion = value; }

    @SerializedName("RegistrationId")
    private String registrationId;
    public String getRegistrationId() {
        return registrationId;
    }
    public void setRegistrationId(String registrationId) {
        this.registrationId = registrationId;
    }
}
