package com.jjkeller.kmbapi.controller.EOBR;

import com.jjkeller.kmbapi.kmbeobr.EventRecord;

import java.util.ArrayList;


public class EobrGenIIHistoryEventArgs {

    public EobrGenIIHistoryEventArgs(ArrayList<EventRecord> eventList)
    {
    	_eventList = eventList;
    }

    private ArrayList<EventRecord> _eventList;
    public ArrayList<EventRecord> getEventList()
    {
        return _eventList;
    }
    public void setEventList(ArrayList<EventRecord> eventList)
    {
    	_eventList = eventList;
    }

    private boolean _hasFailureOccurred = false;
    public boolean getHasFailureOccurred()
    {
        return _hasFailureOccurred; 
    }
    public void setHasFailureOccurred(boolean hasFailureOccurred)
    {
         _hasFailureOccurred = hasFailureOccurred;
    }	
	
}