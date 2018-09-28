package com.jjkeller.kmbapi.controller.share;

import org.joda.time.Instant;

public class IoTHubSettings {
    private String deviceId;
    private String token;
    private Instant expirationUtc;
    private String eventsUri;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Instant getExpirationUtc() {
        return expirationUtc;
    }

    public void setExpirationUtc(Instant expirationUtc) {
        this.expirationUtc = expirationUtc;
    }

    public String getEventsUri() {
        return eventsUri;
    }

    public void setEventsUri(String eventsUri) {
        this.eventsUri = eventsUri;
    }

    @Override
    public String toString() {
        return String.format("deviceId: %s\ntoken: %s\nexpirationUtc: %s\neventsUri: %s", deviceId, token, expirationUtc, eventsUri);
    }
}
