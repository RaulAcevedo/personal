package com.jjkeller.kmbapi.proxydata;

import com.jjkeller.kmbapi.controller.utility.GenericEventComparer;
import com.jjkeller.kmbapi.employeelogeldevents.Enums;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Abstract class that represents a list of EldEventAdapters and defines default behavior for access
 * @param <T> Type of object to store that extends EldEventAdapter
 */
public abstract class EldEventAdapterList<T extends EldEventAdapter> extends ProxyBase {
    /**
     * Public Enum used in accessor calls to determine which list to return
     */
    public enum ListAccessorModifierEnum {
        ALL,
        DutyStatus,
        NonDutyStatus
    }

    /**
     * Class field representing the implementing concrete class. Used to initialize arrays
     */
    private final transient Class<T> implementedEventType;

    /**
     * Private arrays of T[] to store objects
     */
    private T[] nonDutyStatusEvents;
    private T[] dutyStatusEvents;

    /**
     * Hashmap of HashSets of Longs, representing event keys that have been persisted and using
     * the InternalListEnum object to seperate duty status keys from non duty status keys
     */
    private HashMap<ListAccessorModifierEnum, HashSet<Long>> persistedEventKeys = new HashMap<>();

    /**
     * ArrayList containing the values passed in from the setter to be manipulated
     */
    private ArrayList<T> incomingEventsList;

    /**
     * Internal instance of the GenericEventComparer to be used to sort events
     */
    private final GenericEventComparer internalComparator = new GenericEventComparer();

    /**
     * Constructor for EldEventAdapterList, accepting a Class object
     *
     * @param implementingObjClass class that is implementing EldEventAdapterList. Used for array
     *                             instantiation/sizing
     */
    public EldEventAdapterList(Class<T> implementingObjClass) {
        this.implementedEventType = implementingObjClass;
    }

    /**
     * Getter to return all events via overloaded call
     *
     * @return Array of T Events
     */
    protected T[] getEldEventList() {
        return this.getEldEventList(ListAccessorModifierEnum.ALL);
    }

    @Override
    public String toString() {
        return "EldEventAdapterList{" +
                "implementedEventType=" + implementedEventType +
                ", nonDutyStatusEvents=" + Arrays.toString(nonDutyStatusEvents) +
                ", dutyStatusEvents=" + Arrays.toString(dutyStatusEvents) +
                ", persistedEventKeys=" + persistedEventKeys +
                ", incomingEventsList=" + incomingEventsList +
                ", internalComparator=" + internalComparator +
                '}';
    }

    /**
     * Getter that returns an array of T, filtered based on provided ListAccessorModifierEnum
     *
     * @param listAccessorModifierEnum Desired set or subset of events
     * @return
     */
    protected T[] getEldEventList(ListAccessorModifierEnum listAccessorModifierEnum) {
        //If the accessorModifier is null, return an empty array
        if (listAccessorModifierEnum != null) {
            if (this.nonDutyStatusEvents == null && this.dutyStatusEvents == null && this.incomingEventsList == null) {
                //Return blank array if we have nothing
                return (T[]) Array.newInstance(implementedEventType, 0);
            } else if (((this.nonDutyStatusEvents == null || this.nonDutyStatusEvents.length == 0) ||
                    (this.dutyStatusEvents == null || this.dutyStatusEvents.length == 0) &&
                            this.incomingEventsList != null)) {
                /*
                If we have events in the incomingEventsList, but none in the two sorted arrays,
                create a sizer array, and then call the setter using the current incomingEventsList
                 */
                //trim incoming list
                if (this.incomingEventsList != null && this.incomingEventsList.size() > 0) {
                    this.incomingEventsList.trimToSize();
                    int sizeValueHolder = this.incomingEventsList.size();
                    T[] searchSizer = (T[]) Array.newInstance(implementedEventType, sizeValueHolder);
                    this.setEldEventList(this.incomingEventsList.toArray(searchSizer));
                }
            }

            //Sorted list holder. Will (in this case) only have one (1) list, either combined or a
            //subset based on provided enum
            List<List<T>> sortEventList;

            switch (listAccessorModifierEnum) {
                case DutyStatus:
                    //First item in List is duty status events list
                    sortEventList = this.sortEvents(new ArrayList<>(Arrays.asList(this.dutyStatusEvents)), null);
                    break;
                case NonDutyStatus:
                    //First item in List is non duty status events list
                    sortEventList = this.sortEvents(new ArrayList<>(Arrays.asList(this.nonDutyStatusEvents)), null);
                    break;
                default:
                case ALL:
                    //First item in List is combined and sorted list of ALL events
                    sortEventList = this.sortEvents(Arrays.asList(this.dutyStatusEvents), Arrays.asList(this.nonDutyStatusEvents), true);
                    break;
            }
            //Always want the first entry from this process
            if (sortEventList != null) {
                List<T> eventList = sortEventList.get(0);
                //Trim the array to size
                ArrayList<T> returnListForTrimming = new ArrayList<>(eventList);
                returnListForTrimming.trimToSize();
                //Create sizer array and use as return
                T[] combinedEvents = (T[]) Array.newInstance(implementedEventType, returnListForTrimming.size());
                return returnListForTrimming.toArray(combinedEvents);
            }
        }
        return (T[]) Array.newInstance(implementedEventType, 0);
    }

    /**
     * Method that allows setting of event list
     *
     * @param eldEvents events to assign to internal array/arrays
     */
    protected void setEldEventList(T[] eldEvents) {
        if (eldEvents == null) {
            // Clear existing list
            this.incomingEventsList = new ArrayList<>();
        } else {
            // Assign internal list to new incoming event array
            this.incomingEventsList = new ArrayList<>(Arrays.asList(eldEvents));
        }
        // Sort and process new event array
        processIncomingEvents();
    }

    /**
     * Method that uses internal incomingEventsList to pull events from and sort them into groups
     * Sorts each group based on Comparator logic
     */
    protected void processIncomingEvents() {
        //Return if we don't have any events to process
        if (this.incomingEventsList == null)
            return;
        //Process events from the incoming list that already have keys cached
        ArrayList<T> dutyStatusArrayList = this.processKnownEvents(ListAccessorModifierEnum.DutyStatus, this.incomingEventsList);
        ArrayList<T> nonDutyStatusArrayList = this.processKnownEvents(ListAccessorModifierEnum.NonDutyStatus, this.incomingEventsList);

        //Remove events we know about from list
        if (dutyStatusArrayList != null && dutyStatusArrayList.size() > 0)
            this.incomingEventsList.removeAll(dutyStatusArrayList);
        if (nonDutyStatusArrayList != null && nonDutyStatusArrayList.size() > 0)
            this.incomingEventsList.removeAll(nonDutyStatusArrayList);
        //Process the remaining events
        HashMap<ListAccessorModifierEnum, ArrayList<T>> eventMap = processIncomingEvents(this.incomingEventsList);

        //Add newly processed events to their corresponding storage objects
        if (eventMap.containsKey(ListAccessorModifierEnum.DutyStatus))
            dutyStatusArrayList.addAll(eventMap.get(ListAccessorModifierEnum.DutyStatus));

        if (eventMap.containsKey(ListAccessorModifierEnum.NonDutyStatus))
            nonDutyStatusArrayList.addAll(eventMap.get(ListAccessorModifierEnum.NonDutyStatus));

        //Sort both lists using event comparator
        List<List<T>> sortedEventSet = this.sortEvents(dutyStatusArrayList, nonDutyStatusArrayList);
        if (sortedEventSet != null) {
            //Trim the newly sorted and processed lists to ensure size/length consistency
            dutyStatusArrayList.trimToSize();
            nonDutyStatusArrayList.trimToSize();

            //Instantiate sizer arrays to use in .toArray() call
            T[] dutyStatusSizerArray = (T[]) Array.newInstance(implementedEventType, dutyStatusArrayList.size());
            T[] nonDutyStatusSizerArray = (T[]) Array.newInstance(implementedEventType, nonDutyStatusArrayList.size());

            //Set internal arrays of events to their sorted and ordered list representations
            this.dutyStatusEvents = sortedEventSet.get(0).toArray(dutyStatusSizerArray);
            this.nonDutyStatusEvents = sortedEventSet.get(1).toArray(nonDutyStatusSizerArray);
        } else {
            //Nothing was sorted, return empty Arrays
            this.dutyStatusEvents = (T[]) Array.newInstance(implementedEventType, 0);
            this.nonDutyStatusEvents = (T[]) Array.newInstance(implementedEventType, 0);
        }
    }

    /**
     * Method that accepts up to two (2) array lists, and sorts them. Sorts the lists independently
     * of each other, rather than combining them
     *
     * @param evtList1 First list to sort
     * @param evtList2 Second list to sort
     * @return List of sorted ArrayLists representing two (2) sets of events
     */
    protected List<List<T>> sortEvents(ArrayList<T> evtList1, ArrayList<T> evtList2) {
        //Trim incoming lists
        if (evtList1 != null)
            evtList1.trimToSize();
        if (evtList2 != null)
            evtList2.trimToSize();
        //Sort lists without combining them
        return this.sortEvents(evtList1, evtList2, false);
    }

    /**
     * Method used to sort 1 or 2 event lists, and conditionally do so after combining them.
     *
     * @param evtList1       First list to sort
     * @param evtList2       Second list to sort
     * @param combineAndSort value indicating if the lists should be combined before sorting
     * @return List of sorted ArrayLists representing up to two (2) sets of events
     */
    protected List<List<T>> sortEvents(List<T> evtList1, List<T> evtList2, boolean combineAndSort) {
        List<List<T>> returnList = new LinkedList<>();
        //Always allow sorting if the lists are not null
        boolean canSortFirst = evtList1 != null;
        boolean canSortSecond = evtList2 != null;

        //Return if we can't do anything with either list
        if (!canSortFirst && !canSortSecond)
            return null;

        if (canSortFirst && canSortSecond && combineAndSort) {
            //Combine the lists and then sort
            Set<T> combinedEventSet = new HashSet<>(evtList1.size() + evtList2.size());
            combinedEventSet.addAll(evtList1);
            combinedEventSet.addAll(evtList2);
            //Create new ArrayList from HashSet of events
            ArrayList<T> combinedEventSetAsList = new ArrayList<T>(combinedEventSet);
            //Sort
            Collections.sort(combinedEventSetAsList, internalComparator);
            //resize
            combinedEventSetAsList.trimToSize();
            //Add to return object
            returnList.add(combinedEventSetAsList);
        } else {
            if (canSortFirst) {
                //Sort list 1
                Collections.sort(evtList1, internalComparator);
                returnList.add(evtList1);
            }
            if (canSortSecond) {
                //Sort list 2
                Collections.sort(evtList2, internalComparator);
                returnList.add(evtList2);
            }
        }
        return returnList;
    }

    /**
     * This method accepts a given ListAccessorModifierEnum, and an array of keys associated with that
     * list. It then adds those keys to the persistedEventKeys object to speed up sorting
     *
     * @param listEnum enum denoting which list of keys this is
     * @param keys     keys to add to list associated with given list enum
     */
    protected void updateKeyMap(ListAccessorModifierEnum listEnum, HashSet<Long> keys) {
        //Ensure we have a placeholder for a given enum if one doesn't exist
        if (!this.persistedEventKeys.containsKey(listEnum))
            this.persistedEventKeys.put(listEnum, new HashSet<Long>());
        //Get existing keys for given enum
        HashSet<Long> existingKeys = this.persistedEventKeys.get(listEnum);
        HashSet<Long> newKeyList = new HashSet<>(keys.size() + existingKeys.size());

        //Copy existing keys to new list
        newKeyList.addAll(existingKeys);
        //Copy new keys to new list
        newKeyList.addAll(keys);
        //Remove all instances of -1 from the list to avoid duplication
        newKeyList.remove(-1);
        //Set the key map to the new aggregate list
        this.persistedEventKeys.put(listEnum, newKeyList);
    }

    /**
     * Method to use internal HashMap of type/key to speed up sorting of DutyStatusEvents
     *
     * @param eventCategory Enum representing the category of events in the events param
     * @param events        ArrayList of events to process
     * @return ArrayList of events pulled from incomingEventsList
     */
    protected ArrayList<T> processKnownEvents(ListAccessorModifierEnum eventCategory, ArrayList<T> events) {
        ArrayList<T> returnList = new ArrayList<>();
        switch (eventCategory) {
            case DutyStatus:
                returnList = processKnownDutyStatusEvents(events);
                break;
            case NonDutyStatus:
                returnList = processKnownNonDutyStatusEvents(events);
                break;
            default:
                break;
        }
        //Trim return list before returning
        returnList.trimToSize();
        return returnList;
    }

    /**
     * Method that accepts an ArrayList of events that need to be sorted, creates ArrayLists
     * from said ArrayList that represent the two (2) categories of events.
     *
     * @param eventsToSort event ArrayList to sort
     * @return HashMap of ArrayLists corresponding to the two (2) categories of events we want
     * sorted
     */
    protected HashMap<ListAccessorModifierEnum, ArrayList<T>> processIncomingEvents(ArrayList<T> eventsToSort) {

        ArrayList<T> nonDutyStatusEvents = new ArrayList<>();
        ArrayList<T> dutyStatusEvents = new ArrayList<>();

        //Create return object with exact size
        HashMap<ListAccessorModifierEnum, ArrayList<T>> returnMap = new HashMap<>(2);
        //If we have a null list, we could have sorted everything, or could have been provided a null
        //list. Thus, return an empty object
        if (eventsToSort == null)
            return returnMap;
        if (eventsToSort.size() > 0)
            eventsToSort.trimToSize();
        //Get size of eventsToSort after above pre-processing
        int initialListSize = eventsToSort.size();

        //Ensure storage capacity
        HashSet<Long> dutyStatusKeys = new HashSet<>(initialListSize);
        HashSet<Long> nonDutyStatusKeys = new HashSet<>(initialListSize);

        for (int x = 0; x < initialListSize; x++) {
            //Get event out of incoming array list
            T evt = eventsToSort.get(x);
            if (evt == null)
                continue;
            //Get PK and determine if it's in the DB
            long evtPk = evt.getPrimaryKey();
            boolean evtHasPk = evtPk > 0;

            //Determine if this event is a duty status event or not and assign to corresponding list
            if (isDutyStatusEvent(evt)) {
                //If we have a PK, add it for faster sorting later
                if (evtHasPk)
                    dutyStatusKeys.add(evtPk);
                //Add the event to the dutyStatusEvent list
                dutyStatusEvents.add(evt);

            } else {
                if (evtHasPk)
                    nonDutyStatusKeys.add(evtPk);
                //Add the event to nonDutyStatusEvent list
                nonDutyStatusEvents.add(evt);
            }
        }

        //Add events with known keys to map
        updateKeyMap(ListAccessorModifierEnum.DutyStatus, dutyStatusKeys);
        updateKeyMap(ListAccessorModifierEnum.NonDutyStatus, nonDutyStatusKeys);
        //Add sorted event arrayLists to return object
        returnMap.put(ListAccessorModifierEnum.DutyStatus, dutyStatusEvents);
        returnMap.put(ListAccessorModifierEnum.NonDutyStatus, nonDutyStatusEvents);

        return returnMap;
    }

    /**
     * Method to process duty status events that we have a cached key for
     *
     * @param events events to process
     * @return ArrayList of known DutyStatusChange events
     */
    private ArrayList<T> processKnownDutyStatusEvents(ArrayList<T> events) {

        HashSet<Long> knownDutyStatusEventIds;
        //Ensure we have a placeholder for a given enum if one doesn't exist
        if (!this.persistedEventKeys.containsKey(ListAccessorModifierEnum.DutyStatus))
            this.persistedEventKeys.put(ListAccessorModifierEnum.DutyStatus, new HashSet<Long>());

        //We can only return as many events as we have known keys
        knownDutyStatusEventIds = this.persistedEventKeys.get(ListAccessorModifierEnum.DutyStatus);
        ArrayList<T> knownEvents = new ArrayList<>(knownDutyStatusEventIds.size());

        if (events == null || (knownEvents == null || knownEvents.size() < 1)) {
            return knownEvents;
        }

        for (int x = 0; x < events.size(); x++) {
            T evt = events.get(x);
            //If this isn't a duty status change, ignore it.
            if (!isDutyStatusEvent(evt))
                continue;
            for (int y = 0; y < knownDutyStatusEventIds.size(); y++) {
                if (knownDutyStatusEventIds.contains(evt.getPrimaryKey())) {
                    knownEvents.add(evt);
                    break;
                }
            }
        }
        return knownEvents;
    }

    /**
     * Method to process non duty status events that we have a cached key for
     *
     * @param events events to process
     * @return ArrayList of known Non-DutyStatusChange events
     */
    private ArrayList<T> processKnownNonDutyStatusEvents(ArrayList<T> events) {
        HashSet<Long> knownNonDutyStatusEvents;
        //Ensure we have a placeholder for a given enum if one doesn't exist
        if (!this.persistedEventKeys.containsKey(ListAccessorModifierEnum.NonDutyStatus))
            this.persistedEventKeys.put(ListAccessorModifierEnum.NonDutyStatus, new HashSet<Long>());

        //We can only return as many events as we have known keys
        knownNonDutyStatusEvents = this.persistedEventKeys.get(ListAccessorModifierEnum.NonDutyStatus);
        ArrayList<T> knownEvents = new ArrayList<>(knownNonDutyStatusEvents.size());

        if (events == null || (knownEvents == null || knownEvents.size() < 1)) {
            return knownEvents;
        }

        for (int x = 0; x < events.size(); x++) {
            T evt = events.get(x);
            //If this is a duty status change, ignore it
            if (isDutyStatusEvent(evt))
                continue;
            if (knownEvents.contains(evt.getPrimaryKey())) {
                knownEvents.add(evt);
                break;
            }
        }
        return knownEvents;
    }

    /**
     * Private method to determine if an event is a duty status change
     *
     * @param evt event to check
     * @return value representing if event is a duty status change
     */
    private boolean isDutyStatusEvent(T evt) {
        //An event is a duty status event if it's translation of it's event code to duty status enum
        //is not NULL
        return evt._event.getEventType() == Enums.EmployeeLogEldEventType.DutyStatusChange;
    }
}
