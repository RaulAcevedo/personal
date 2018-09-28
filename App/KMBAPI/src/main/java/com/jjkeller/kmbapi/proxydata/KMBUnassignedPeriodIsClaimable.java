package com.jjkeller.kmbapi.proxydata;

/**
 * Created by ief5781 on 4/13/17.
 */

public class KMBUnassignedPeriodIsClaimable extends ProxyBase {
    private String id;
    private boolean isClaimable;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isClaimable() {
        return isClaimable;
    }

    public void setClaimable(boolean claimable) {
        isClaimable = claimable;
    }
}
