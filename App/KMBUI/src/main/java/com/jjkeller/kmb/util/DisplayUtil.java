package com.jjkeller.kmb.util;

import android.util.Pair;

import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.employeelogeldevents.Enums;
import com.jjkeller.kmbapi.enums.DutyStatusEnum;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;
import com.jjkeller.kmbui.R;

/**
 * Created by T000684 on 7/28/2017.
 */

public class DisplayUtil {

    private DisplayUtil() {

    }

    private static String getString(int resId) {
        return GlobalState.getInstance().getResources().getString(resId);
    }

    public static String getStatusDisplayText(Pair<EmployeeLogEldEvent, Enums.SpecialDrivingCategory> eventWithProvisions, boolean isTheDriver, boolean isExempt) {
        EmployeeLogEldEvent logEldEvent = eventWithProvisions.first;
        Enums.SpecialDrivingCategory specialDrivingCategory = eventWithProvisions.second;
        String dutyStatusText;
        DutyStatusEnum dutyStatus = new DutyStatusEnum(logEldEvent.getEventCode());

        if ((specialDrivingCategory != null && specialDrivingCategory == Enums.SpecialDrivingCategory.PersonalConveyance) && isTheDriver) {
            dutyStatusText = getFormattedDutyStatusText(DutyStatusEnum.Friendly_OffDuty, getString(R.string.personalconveyance));
        } else if ((specialDrivingCategory != null && specialDrivingCategory == Enums.SpecialDrivingCategory.YardMove) && isTheDriver) {
            dutyStatusText = getFormattedDutyStatusText(DutyStatusEnum.RODSFriendly_OnDuty, getString(R.string.yardmove));
        } else if ((specialDrivingCategory != null && specialDrivingCategory == Enums.SpecialDrivingCategory.Hyrail) && isTheDriver) {
            dutyStatusText = getFormattedDutyStatusText(DutyStatusEnum.RODSFriendly_OnDuty, getString(R.string.hyrail));
        } else if ((specialDrivingCategory != null && specialDrivingCategory == Enums.SpecialDrivingCategory.NonRegulated) && isTheDriver) {
            dutyStatusText = getFormattedDutyStatusText(DutyStatusEnum.RODSFriendly_OnDuty, getString(R.string.nonregulated));
        } else if (dutyStatus.getValue() == DutyStatusEnum.DRIVING) {
            dutyStatusText = DutyStatusEnum.Friendly_Driving;
        } else if (dutyStatus.getValue() == DutyStatusEnum.ONDUTY) {
            dutyStatusText = DutyStatusEnum.Friendly_OnDuty;
        } else {
            dutyStatusText = dutyStatus.toFriendlyName();
        }

        if (isExempt && isTheDriver) {
            dutyStatusText += " " + getString(R.string.eldexempt);
        }

        return dutyStatusText;
    }

    private static String getFormattedDutyStatusText(String dutyStatusEnum, String subStatus) {
        return String.format("%1$s - %2$s", dutyStatusEnum, subStatus);
    }

}
