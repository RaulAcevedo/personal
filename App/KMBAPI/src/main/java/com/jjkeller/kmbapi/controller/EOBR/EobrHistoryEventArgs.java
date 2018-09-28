package com.jjkeller.kmbapi.controller.EOBR;

import com.jjkeller.kmbapi.kmbeobr.StatusRecord;

import java.util.List;

public class EobrHistoryEventArgs {

    public EobrHistoryEventArgs(List<StatusRecord> historyList)
    {
    	_historyList = historyList;
    }

    private List<StatusRecord> _historyList;
    public List<StatusRecord> getHistoryList()
    {
        return _historyList;
    }
    public void setHistoryList(List<StatusRecord> historyList)
    {
        _historyList = historyList;
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
