package com.jjkeller.kmbapi.proxydata;

/**
 * Created by Kris Larsen on 7/17/2017.
 */

public class EmployeeLogUnidentifiedELDEventStatus extends ProxyBase {
    ///////////////////////////////////////////////////////////////////////////////////////
    // private members
    ///////////////////////////////////////////////////////////////////////////////////////
    private long ClusterPK;
    private long RelatedEventClusterPK;
    private int EventRecordStatus;


    ///////////////////////////////////////////////////////////////////////////////////////
    // public get/set methods
    ///////////////////////////////////////////////////////////////////////////////////////
    public long getClusterPK() {
        return ClusterPK;
    }
    public void setClusterPK(long clusterPK) {
        this.ClusterPK = clusterPK;
    }
    public long getRelatedEventClusterPK() {
        return RelatedEventClusterPK;
    }
    public void setRelatedEventClusterPK(long relatedEventClusterPK) {
        this.RelatedEventClusterPK = relatedEventClusterPK;
    }
    public int getEventRecordStatus() {
        return EventRecordStatus;
    }
    public void setEventRecordStatus(int eventRecordStatus) {
        this.EventRecordStatus = eventRecordStatus;
    }
}
