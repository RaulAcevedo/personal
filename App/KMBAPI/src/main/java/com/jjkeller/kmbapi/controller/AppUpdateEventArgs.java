package com.jjkeller.kmbapi.controller;

public class AppUpdateEventArgs {
	public AppUpdateEventArgs() {}
	
    private long _maxSize = 0;
    private long _totalReceived = 0;
    private boolean _hasDownloadCompleted = false;
    private boolean _wasDownloadSuccessful = false;

    /// <summary>
    /// Maximum total expected size of download file
    /// </summary>
    public long getMaxSize(){ return _maxSize; }
    public void setMaxSize(long value) { _maxSize = value; }

    /// <summary>
    /// Total number of bytes received in the download so far
    /// </summary>
    public long getTotalReceived() { return _totalReceived; }
    public void setTotalReceived(long value) { _totalReceived = value; }

    /// <summary>
    /// Answer if the download has completed.   Whether the 
    /// download has completed, does not imply that the
    /// download was successful.
    /// </summary>
    public boolean getHasDownloadCompleted() { return _hasDownloadCompleted; }
    public void setHasDownloadCompleted(boolean value) { _hasDownloadCompleted = value; }

    /// <summary>
    /// Answer if the download is complete and was successful.
    /// </summary>
    public boolean getWasDownloadSuccessful() { return _wasDownloadSuccessful; }
    public void setWasDownloadSuccessful(boolean value) { _wasDownloadSuccessful = value; }
}
