package com.jjkeller.kmb.interfaces;

public interface IUploadDiagnostics
{
	public interface UploadDiagnosticsFragActions
	{
	    public void handleUploadButtonClick();
	    public void handleUploadBackupLogsButtonClick();
	    public void handleClearEobrButtonClick();
	    public void handleClearKmbButtonClick();
	    public void handleMoveToSDCardButtonClick();
		public void handleConsole();
	}
}
