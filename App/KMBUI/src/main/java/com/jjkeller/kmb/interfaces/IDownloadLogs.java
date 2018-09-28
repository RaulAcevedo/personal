package com.jjkeller.kmb.interfaces;

import android.view.View;

import com.jjkeller.kmbapi.controller.interfaces.IAPIController;

public interface IDownloadLogs {
	public interface DownloadLogsFragControllerMethods{
		public IAPIController getMyController();
	}
	
	public interface DownloadLogsFragActions{
		public void HandleDoneClick();
		public void HandleDownloadClick(View v);
	}
}
