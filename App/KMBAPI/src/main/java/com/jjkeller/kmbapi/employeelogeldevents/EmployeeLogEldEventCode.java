package com.jjkeller.kmbapi.employeelogeldevents;

import com.jjkeller.kmbapi.enums.EnumBase;

/**
 * Created by bja6001 on 6/24/16.
 */
public class EmployeeLogEldEventCode extends EnumBase {

    public EmployeeLogEldEventCode(int i){ super(i); }

    @Override
    public void setValue(int value) throws IndexOutOfBoundsException {
        if (0 <= value && value <= Certification_MaxValue) {
            this.value = value;
        } else {
            super.setValue(value);
        }
    }

    @Override
    public String toDMOEnum() {
        return null;
    }

    @Override
    protected int getArrayId() {
        return 0;
    }

    //Used for EmployeeLogEldEventType.DutyStatusChange
    public static final int DutyStatus_OffDuty = 1;
    public static final int DutyStatus_Sleeper = 2;
    public static final int DutyStatus_Driving = 3;
    public static final int DutyStatus_OnDuty = 4;
    public static final int DutyStatus_OffDutyWellSite = 5;
    public static final int DutyStatus_NULL = 0;

    //Used for EmployeeLogEldEventType.IntermediateLog
    public static final int IntermediateLog_ConventionalLocationPrecision = 1;
    public static final int IntermediateLog_ReducedLocationPrecision  = 2;

    //Used for EldType.ChangeInDriversIndication
    public static final int ChangeInDriversIndication_PersonalUse = 1;
    public static final int ChangeInDriversIndication_YardMoves = 2;
    public static final int ChangeInDriversIndication_PCYMWT_Cleared = 0;

    //Not really codes, but MaxValue allowed for n'th certification
    //Start at 1 and continue incrementing every time a re-certification occurs
    //Max is 9.  continue using 9 for each re-certification thereafter
    //7.1.19
    public static final int Certification_MaxValue = 9;

    //Used for EmployeeLogEldEventType.LoginLogout
    public static final int Login = 1;
    public static final int Logout = 2;

    //used for EmployeeLogEldEventType.EnginePowerUpPowerDown
    public static final int PowerUp_ConventionalLocationPrecision = 1;
    public static final int PowerUp_ReducedLocationPrecision = 2;
    public static final int PowerDown_ConventionalLocationPrecision = 3;
    public static final int PowerDown_ReducedLocationPrecision = 4;

    //Used for EmployeeLogEldEventType.Malfunction_DataDiagnosticDetection
    public static final int None = 0;
    public static final int EldMalfunctionLogged = 1;
    public static final int EldMalfunctionCleared = 2;
    public static final int EldDataDiagnosticLogged = 3;
    public static final int EldDataDiagnosticCleared = 4;
}
