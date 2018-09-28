package com.jjkeller.kmbapi.controller.EOBR;

import com.jjkeller.kmbapi.kmbeobr.EventRecord;
import com.jjkeller.kmbapi.kmbeobr.StatusRecord;

public class EobrEventArgs {

    public EobrEventArgs()
    {
        _statusRecord = new StatusRecord();
    }
    public EobrEventArgs(StatusRecord status)
    {
        _statusRecord = status;
    }
    public EobrEventArgs(EventRecord status)
    {
    	_eventRecord = status;
    }

    private int _returnCode = 0;
    public int getReturnCode()
    {
        return _returnCode; 
    }
    public void setReturnCode(int returnCode)
    {
        _returnCode = returnCode;
    }

    private StatusRecord _statusRecord;
    public StatusRecord getStatusRecord()
    {
        return _statusRecord;
    }
    public void setStatusRecord(StatusRecord statusRecord)
    {
        _statusRecord = statusRecord;
    }

    private EobrReader.ConnectionState _connectionState;
    public EobrReader.ConnectionState getConnectionState()
    {
        return _connectionState;
    }
    public void setConnectionState(EobrReader.ConnectionState connectionState)
    {
        _connectionState = connectionState;
    }
    
    private EventRecord _eventRecord;
    public EventRecord getEventRecord()
    {
        return _eventRecord;
    }
    public void setEventRecord(EventRecord eventRecord)
    {
        _eventRecord = eventRecord;        
    }
    
    private boolean _publishDutyStatusChange;
    public boolean getPublishDutyStatusChange()
    {
    	return this._publishDutyStatusChange;
    }
    public void setPublishDutyStatusChange(boolean publishDutyStatusChange)
    {
    	this._publishDutyStatusChange = publishDutyStatusChange;
    }
}
