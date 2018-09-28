package com.jjkeller.kmbapi.controller.interfaces;

import com.jjkeller.kmbapi.enums.DutyStatusEnum;
import com.jjkeller.kmbapi.enums.EmployeeLogProvisionTypeEnum;
import com.jjkeller.kmbapi.kmbeobr.EventRecord;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;
import com.jjkeller.kmbapi.proxydata.Location;

import java.util.Date;

/**
 * Created by ief5781 on 10/26/16.
 */
public interface ISpecialDrivingController {
    EmployeeLogProvisionTypeEnum getDrivingCategory();

    boolean getIsFeatureToggleEnabled();

    boolean getIsInSpecialDutyStatus();

    void setIsInSpecialDutyStatus(boolean value);

    boolean getIsInSpecialDrivingSegment();

    void setIsInSpecialDrivingSegment(boolean value);

    void StartSpecialDrivingStatus(Date startTime, Location location, EmployeeLog empLog);
    void StartSpecialDrivingStatus(Date startTime, Location location, EmployeeLog empLog, boolean forMidnightTransition, EmployeeLog previousLog);

    void VerifySpecialDrivingEnd();

    void DialogPreprocessEndSpecialDrivingStatus(Date endTime, Location location, EmployeeLog empLog);

    void EndSpecialDrivingStatus();
    void EndSpecialDrivingStatus(EventRecord eventRecord, Location location, EmployeeLog empLog);
    void EndSpecialDrivingStatus(EmployeeLog empLog, boolean setPotentialStopTime);
    void EndSpecialDrivingStatus(Date endTime, Location location, EmployeeLog empLog);
    void EndSpecialDrivingStatus(Date endTime, Location location, EmployeeLog empLog, boolean forMidnightTransition);

    Date getEndTime(EmployeeLog log);

    boolean SubmitAllSpecialDrivingItemsToDMO();

    void ProcessNewDutyStatus(DutyStatusEnum dutyStatus, Date time, Location location, EmployeeLog empLog, EventRecord eventRecord);

    void ProcessEvent(EventRecord eventRecord, Location location, EmployeeLog empLog);

    void PublishSpecialDrivingEnd(EventRecord eventRecord, Location location, EmployeeLog empLog);

    void LinkProvisionsToEldEvent(EmployeeLogEldEvent lastEventLog);

    void AddAobrdStartingLogRemark(EmployeeLog empLog, Date startTime, EmployeeLogEldEvent lastEventLog);

}
