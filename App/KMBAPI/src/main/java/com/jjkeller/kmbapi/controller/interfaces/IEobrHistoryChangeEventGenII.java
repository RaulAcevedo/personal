package com.jjkeller.kmbapi.controller.interfaces;

import com.jjkeller.kmbapi.controller.EOBR.EobrGenIIHistoryEventArgs;

public interface IEobrHistoryChangeEventGenII {
	public abstract void onEventChange(EobrGenIIHistoryEventArgs e);
}