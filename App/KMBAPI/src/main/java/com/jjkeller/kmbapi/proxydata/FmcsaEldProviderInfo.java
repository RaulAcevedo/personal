package com.jjkeller.kmbapi.proxydata;

import com.google.gson.annotations.SerializedName;

public class FmcsaEldProviderInfo extends ProxyBase {
    @SerializedName("ProviderName")
    private String providerName;
    public String getProviderName() { return providerName; }
    public void setProviderName(String value) { this.providerName = value; }

    @SerializedName("RegistrationId")
    private String registrationId;
    public String getRegistrationId() { return registrationId; }
    public void setRegistrationId(String value) { this.registrationId = value; }
}
