package com.jjkeller.kmb.interfaces;

import com.jjkeller.kmb.share.BaseActivity;

public interface ILogDownloaderHost {
	public void ShowConfirmationMessage(String message, Runnable yesAction, Runnable noAction);

	public BaseActivity getActivity();
	
	public void onLogDownloadFinished(boolean logsFound);
	public void onOffDutyLogsCreated(boolean logsCreated);
}
