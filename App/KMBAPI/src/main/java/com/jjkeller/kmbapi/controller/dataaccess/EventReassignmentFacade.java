package com.jjkeller.kmbapi.controller.dataaccess;

import android.content.Context;

import com.jjkeller.kmbapi.controller.dataaccess.db.EventReassignmentPersist;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.proxydata.DrivingEventReassignmentMapping;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bja6001 on 2/23/17.
 */

public class EventReassignmentFacade extends FacadeBase {

    public EventReassignmentFacade(Context ctx, User user) {
        super(ctx, user);
    }

    /**
     * Persist method to save a mapping
     *
     * @param drivingEventReassignmentMapping Mapping to save
     */
    public void Save(DrivingEventReassignmentMapping drivingEventReassignmentMapping) {
        EventReassignmentPersist<DrivingEventReassignmentMapping> persist =
                new EventReassignmentPersist<>(DrivingEventReassignmentMapping.class,
                        this.getContext());
        persist.Persist(drivingEventReassignmentMapping);
    }

    /**
     * Public method to return all reassignment entries for a given ELD Event
     *
     * @param relatedEvent Related Eld Event within KMB
     * @return ArrayList of associated mapping entities
     */
    public ArrayList<DrivingEventReassignmentMapping> FetchByRelatedEvent(int relatedEvent) {
        ArrayList<DrivingEventReassignmentMapping> mappings = new ArrayList<>();
        mappings.addAll(this.Fetch(null, relatedEvent, null));
        return mappings;
    }

    /**
     * Public method to return all reassignment entries for a given Driver Id
     *
     * @param driverId Id of driver who has had events reassigned to them
     * @return ArrayList of associated mapping entities
     */
    public ArrayList<DrivingEventReassignmentMapping> FetchByDriverId(String driverId) {
        ArrayList<DrivingEventReassignmentMapping> mappings = new ArrayList<>();
        mappings.addAll(this.Fetch(null, null, driverId));
        return mappings;
    }

    /**
     * Public method to return a single record based on it's primary key
     *
     * @param recordId PK of record
     * @return Single entry for provided record id
     */
    public DrivingEventReassignmentMapping FetchByRecordId(int recordId) {
        ArrayList<DrivingEventReassignmentMapping> returnData = this.Fetch(recordId, null, null);
        if (returnData != null && returnData.size() > 0)
            return returnData.get(0);

        return null;
    }

    /**
     * Public method to return a single record based on it's natural key
     *
     * @param mapping DrivingEventReassignmentMapping object to pull natural key from
     * @return Single entry for provided natural key
     */
    public DrivingEventReassignmentMapping FetchByNaturalKey(DrivingEventReassignmentMapping mapping) {
        ArrayList<DrivingEventReassignmentMapping> returnData = this.Fetch(null, mapping.getRelatedEvent(), mapping.getDriverToAssignEventTo());
        if (returnData != null && returnData.size() > 0)
            return returnData.get(0);

        return null;
    }

    public List<DrivingEventReassignmentMapping> FetchAllUnsubmitted()
    {
        EventReassignmentPersist<DrivingEventReassignmentMapping> persist = new EventReassignmentPersist<>(DrivingEventReassignmentMapping.class, this.getContext());
        return persist.FetchAllUnsubmitted();
    }

    /**
     * Protected method that aggregates all fetch logic into single method
     *
     * @param recordId If provided, the record id we want to return
     * @param driverId If provided, the Id of the driver associated with this reassignment
     * @return ArrayList of Mappings associated with the provided key
     */
    protected ArrayList<DrivingEventReassignmentMapping> Fetch(Integer recordId, Integer relatedEvent, String driverId) {
        EventReassignmentPersist<DrivingEventReassignmentMapping> persist = new EventReassignmentPersist<>(DrivingEventReassignmentMapping.class, this.getContext());
        if (recordId != null) {
            return new ArrayList<>(persist.FetchByPrimaryKey(recordId));
        } else if (relatedEvent != null && driverId != null) {
            //Fetch by full key
            //returns 0-1 entities
            return new ArrayList<>(persist.FetchByNaturalKey(relatedEvent, driverId));
        } else if (relatedEvent != null) {
            //Fetch by EldEventId
            //return 0-N entities
            return new ArrayList<>(persist.FetchByRelatedEvent(relatedEvent));
        } else if (driverId != null) {
            //Fetch by driverId
            //return 0-N entities
            return new ArrayList<>(persist.FetchByDriverEventReassignmentId(driverId));
        }
        return null;
    }
}
