package com.jjkeller.kmbapi.controller.interfaces;

import com.jjkeller.kmbapi.controller.EOBR.EobrHistoryEventArgs;

public interface IEobrHistoryChangeEvent {
	public abstract void onEventChange(EobrHistoryEventArgs e);
}
