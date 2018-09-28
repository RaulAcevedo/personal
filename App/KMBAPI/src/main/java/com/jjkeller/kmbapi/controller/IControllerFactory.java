package com.jjkeller.kmbapi.controller;

import android.content.Context;

public interface IControllerFactory {
	LogEntryController getLogEntryController(Context ctx);
	LogCheckerComplianceDatesController getLogCheckerComplianceDateController();
}