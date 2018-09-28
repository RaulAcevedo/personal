package com.jjkeller.kmbapi.controller.interfaces;

import com.jjkeller.kmbapi.controller.AppUpdateEventArgs;

public interface IAppUpdateEvent {
	public abstract void onAppUpdateDownload(AppUpdateEventArgs e);
}
