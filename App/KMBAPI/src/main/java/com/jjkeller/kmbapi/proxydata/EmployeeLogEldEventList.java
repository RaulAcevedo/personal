package com.jjkeller.kmbapi.proxydata;


import com.jjkeller.kmbapi.employeelogeldevents.Enums;

import java.util.ArrayList;
import java.util.List;

public class EmployeeLogEldEventList extends EldEventAdapterList<EmployeeLogEldEvent> {

    public EmployeeLogEldEventList() {
        super(EmployeeLogEldEvent.class);
    }

    @Override
    public EmployeeLogEldEvent[] getEldEventList() {
        return super.getEldEventList();
    }

    @Override
    public EmployeeLogEldEvent[] getEldEventList(ListAccessorModifierEnum listAccessorModifierEnum) {
        return super.getEldEventList(listAccessorModifierEnum);
    }

    public EmployeeLogEldEvent[] getActiveEldEventList(ListAccessorModifierEnum listAccessorModifierEnum){
        //Create a List Array of ELD Events
        ArrayList<EmployeeLogEldEvent> activeEvents = new ArrayList<>() ;

        EmployeeLogEldEvent[] allEvents = getEldEventList(listAccessorModifierEnum);

        //Loop through the events
        for (EmployeeLogEldEvent event: allEvents) {
            //ONLY add events that are EventRecordStatus == 1  which is ACTIVE
            if (event.getEventRecordStatus() == Enums.EmployeeLogEldEventRecordStatus.Active.getValue()) {
                activeEvents.add(event);
            }
        }

        //Convert the list to the correct type (array list of the object)
        EmployeeLogEldEvent[] arrayListOfActiveEvents = new EmployeeLogEldEvent[activeEvents.size()];
        arrayListOfActiveEvents = activeEvents.toArray(arrayListOfActiveEvents);

        return arrayListOfActiveEvents;
    }

    @Override
    public void setEldEventList(EmployeeLogEldEvent[] eldEvents) {
        super.setEldEventList(eldEvents);
    }

    public void setEldEventList(List<EmployeeLogEldEvent> eldEvents){
        super.setEldEventList(eldEvents.toArray(new EmployeeLogEldEvent[eldEvents.size()]));
    }

    public EmployeeLogEldEvent[] getInactiveChangeRequestedEldEventList() {
        //Create a List Array of ELD Events
        ArrayList<EmployeeLogEldEvent> inactiveChangeRequestedEvents = new ArrayList<>() ;

        EmployeeLogEldEvent[] allEldEvents = getEldEventList();

        //Loop through the events
        for (EmployeeLogEldEvent event: allEldEvents) {

            if (event.getEventRecordStatus() == Enums.EmployeeLogEldEventRecordStatus.InactiveChangeRequested.getValue()) {
                inactiveChangeRequestedEvents.add(event);
            }
        }

        //Convert the list to the correct type (array list of the object)
        EmployeeLogEldEvent[] arrayListOfInactiveChangeRequestedEvents = new EmployeeLogEldEvent[inactiveChangeRequestedEvents.size()];
        arrayListOfInactiveChangeRequestedEvents = inactiveChangeRequestedEvents.toArray(arrayListOfInactiveChangeRequestedEvents);

        return arrayListOfInactiveChangeRequestedEvents;
    }

    public EmployeeLogEldEvent[] getInactiveChangedEldEventList() {
        //Create a List Array of ELD Events
        ArrayList<EmployeeLogEldEvent> inactiveChangedEvents = new ArrayList<>() ;

        EmployeeLogEldEvent[] allEldEvents = getEldEventList();

        //Loop through the events
        for (EmployeeLogEldEvent event: allEldEvents) {

            if (event.getEventRecordStatus() == Enums.EmployeeLogEldEventRecordStatus.InactiveChanged.getValue()) {
                inactiveChangedEvents.add(event);
            }
        }

        //Convert the list to the correct type (array list of the object)
        EmployeeLogEldEvent[] arrayListOfInactiveChangedEvents = new EmployeeLogEldEvent[inactiveChangedEvents.size()];
        arrayListOfInactiveChangedEvents = inactiveChangedEvents.toArray(arrayListOfInactiveChangedEvents);

        return arrayListOfInactiveChangedEvents;
    }

    public EmployeeLogEldEvent getEldEventByClusteredKey(long encompassClusterPK) {

        EmployeeLogEldEvent[] allEldEvents = getEldEventList();

        EmployeeLogEldEvent eventToReturn = null;

        //Loop through the events
        for (EmployeeLogEldEvent event: allEldEvents) {
            if (event.getEncompassClusterPK() == encompassClusterPK) {
                eventToReturn = event;
                break;
            }
        }

        return eventToReturn;
    }
}
