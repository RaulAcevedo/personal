package com.jjkeller.kmbapi.controller.interfaces;

import com.jjkeller.kmbapi.controller.DutyStatusChangeEventArgs;

public interface IDutyStatusChangeEvent {
	public abstract void onDutyStatusChanged(DutyStatusChangeEventArgs e);
}
