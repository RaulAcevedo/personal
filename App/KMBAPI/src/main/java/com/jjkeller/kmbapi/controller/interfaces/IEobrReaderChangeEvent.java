package com.jjkeller.kmbapi.controller.interfaces;

import com.jjkeller.kmbapi.controller.EOBR.EobrEventArgs;
import com.jjkeller.kmbapi.enums.EmployeeLogProvisionTypeEnum;
import com.jjkeller.kmbapi.kmbeobr.EventRecord;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.proxydata.Location;

public interface IEobrReaderChangeEvent {
	void onEventChange(EobrEventArgs e);
    void onVerifyDriveEnd();
	void onVerifySpecialDrivingEnd(EmployeeLogProvisionTypeEnum drivingCategory, EventRecord eventRecord, Location location, EmployeeLog empLog);
	void onDismissSpecialDrivingDialog(EmployeeLogProvisionTypeEnum drivingCategory);
	void onDismissDrivingView();
}
