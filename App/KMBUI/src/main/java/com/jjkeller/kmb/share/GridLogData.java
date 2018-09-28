package com.jjkeller.kmb.share;

import android.content.Context;
import android.os.Bundle;

import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.controller.utility.EmployeeLogUtilities;
import com.jjkeller.kmbapi.controller.utility.GenericEventComparer;
import com.jjkeller.kmbapi.employeelogeldevents.EmployeeLogEldEventCode;
import com.jjkeller.kmbapi.employeelogeldevents.Enums;
import com.jjkeller.kmbapi.enums.DutyStatusEnum;
import com.jjkeller.kmbapi.enums.ExemptLogTypeEnum;
import com.jjkeller.kmbapi.enums.TimeZoneEnum;
import com.jjkeller.kmbapi.proxydata.EldEventAdapterList;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class GridLogData {
    private static final String bndHourVals = "hourVals";
    private static final String bndDutyStatusVals = "dutyStatusVals";
    private static final String bndIsManuallyEditedByKMBUsersVals = "isManuallyEditedByKMBUsersVals";
    private static final String bndLogDateStr = "logDate";
    private static final String bndOilFieldSpecificVehicle = "oilfieldVehicle";
    private static final String bndIsPC = "isPC";
    private static final String bndIsYM = "isYM";
    private static final String bndIsPCYMEnd = "isPCYMEnd";

    private boolean _isOperateSpecificVehicleForOilField;
    private String _logDate;

    List<Double> _dutyStatusVals = new ArrayList<>();
    List<Long> _hourVals = new ArrayList<>();
    List<Boolean> _isManuallyEditedByKMBUserVals = new ArrayList<>();
    List<Boolean> _isPCVals = new ArrayList<>();
    List<Boolean> _isYMVals = new ArrayList<>();
    List<Boolean> _isPCYMEndVals = new ArrayList<>();

    public List<Double> getDutyStatusValues() {
        return _dutyStatusVals;
    }

    public List<Long> getHourValues() {
        return _hourVals;
    }

    public List<Boolean> getIsManuallyEditedByKMBUserValues() {
        return _isManuallyEditedByKMBUserVals;
    }

    public List<Boolean> getIsPCValues() {
        return _isPCVals;
    }

    public List<Boolean> getIsYMValues() {
        return _isYMVals;
    }

    public List<Boolean> getIsPCYMEndValues() {
        return _isPCYMEndVals;
    }

    public boolean getIsOperateSpecificVehicleForOilField() {
        return _isOperateSpecificVehicleForOilField;
    }

    public void setLogDate(Date logDate) {
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yy", Locale.getDefault());
        _logDate = format.format(logDate);
    }

    public String getLogDate() {
        return _logDate;
    }

    public void onSaveInstanceState(Bundle outState) {

        if (_hourVals != null) {
            // Convert to primitive arrays for storing in state Bundle
            outState.putLongArray(bndHourVals, this.ToPrimativeArray(_hourVals.toArray(new Long[_hourVals.size()])));
            outState.putDoubleArray(bndDutyStatusVals, this.ToPrimativeArray(_dutyStatusVals.toArray(new Double[_dutyStatusVals.size()])));
            outState.putBooleanArray(bndIsManuallyEditedByKMBUsersVals, this.ToPrimativeArray(_isManuallyEditedByKMBUserVals.toArray(new Boolean[_isManuallyEditedByKMBUserVals.size()])));
            outState.putString(bndLogDateStr, _logDate);
            outState.putBoolean(bndOilFieldSpecificVehicle, _isOperateSpecificVehicleForOilField);
            outState.putBooleanArray(bndIsPC, this.ToPrimativeArray(_isPCVals.toArray(new Boolean[_isPCVals.size()])));
            outState.putBooleanArray(bndIsYM, this.ToPrimativeArray(_isYMVals.toArray(new Boolean[_isYMVals.size()])));
            outState.putBooleanArray(bndIsPCYMEnd, this.ToPrimativeArray(_isPCYMEndVals.toArray(new Boolean[_isPCYMEndVals.size()])));
        }
    }

    public void loadDataFromSavedState(Bundle savedState) {
        _isOperateSpecificVehicleForOilField = false;

        // Restore to object arrays from bundle
        _dutyStatusVals = Arrays.asList(this.ToObjectArray(savedState.getDoubleArray(bndDutyStatusVals)));
        _hourVals = Arrays.asList(this.ToObjectArray(savedState.getLongArray(bndHourVals)));
        _isManuallyEditedByKMBUserVals = Arrays.asList(this.ToObjectArray(savedState.getBooleanArray(bndIsManuallyEditedByKMBUsersVals)));
        _logDate = savedState.getString(bndLogDateStr);
        _isOperateSpecificVehicleForOilField = savedState.getBoolean(bndOilFieldSpecificVehicle);
        _isPCVals = Arrays.asList(this.ToObjectArray(savedState.getBooleanArray(bndIsPC)));
        _isYMVals = Arrays.asList(this.ToObjectArray(savedState.getBooleanArray(bndIsYM)));
        _isPCYMEndVals = Arrays.asList(this.ToObjectArray(savedState.getBooleanArray(bndIsPCYMEnd)));
    }

    public void CreateDataForGrid(TimeZoneEnum currentUserHomeTerminalTimeZone, EmployeeLog empLog, Date currentHomeTerminalTime, Context context) {
        // Sample data
		/*
		Double[] doubles = {0.5d, 0.5d, 1.5d, 1.5d, 2.5d, 2.5d, 3.5d, 3.5d, 2.5d, 2.5d, 0.5d, 0.5d};
		
		Long[] longs = {
				1306299600L, // 5/24/11 12:00am
				1306328400L, // 5/24/11 8:00 AM central  every 3600 is an hour
				1306328400L, // 5/24/11 8:00 AM central  
				1306339200L, // 5/24/11 11:00 AM central  
				1306339200L, // 5/24/11 11:00 AM central  
				1306343700L, // 5/24/11 12:15 PM central  
				1306343700L, // 5/24/11 12:15 PM central  
				1306353000L, // 5/24/11 2:50 PM central  
				1306353000L, // 5/24/11 2:50 PM central  
				1306359000L, // 5/24/11 4:30 PM central
				1306359000L, // 5/24/11 4:30 PM central
				1306386000L  // 5/25/11 12:00 AM
		};
		_dutyStatusVals = doubles;
		_hourVals = longs;
		_isManuallyEditedByKMBUserVals = Booleans
		*/


        long timezoneDiff = currentUserHomeTerminalTimeZone.toTimeZone()
                .getRawOffset() - TimeZone.getDefault().getRawOffset();

        DutyStatusEnum lastDutyStatus = null;

        _logDate = DateUtility.getHomeTerminalShortDateFormat().format(empLog.getLogDate());

        _isOperateSpecificVehicleForOilField = empLog.getIsOperatesSpecificVehiclesForOilfield();

        _dutyStatusVals = new ArrayList<>();
        _hourVals = new ArrayList<>();
        _isManuallyEditedByKMBUserVals = new ArrayList<>();
        _isPCVals = new ArrayList<>();
        _isYMVals = new ArrayList<>();
        _isPCYMEndVals = new ArrayList<>();

        Long epochTime = null;

        //Sort ascending by Date (not by sequential number)
        List<EmployeeLogEldEvent> eldEvents = new ArrayList<>(Arrays.asList(empLog.getEldEventList().getEldEventList(EldEventAdapterList.ListAccessorModifierEnum.ALL)));
        Collections.sort(eldEvents, new GenericEventComparer());

        Boolean isPC = false;
        Boolean isYM = false;
        Boolean isPCYMEnd;

        for (int i = 0; i < eldEvents.size(); i++) {
            EmployeeLogEldEvent event = eldEvents.get(i);
            DutyStatusEnum dutyStatus = event.getDutyStatusEnum();

            if (event.getEventType() == null ||
                    (event.getEventType() != Enums.EmployeeLogEldEventType.DutyStatusChange &&
                            event.getEventType() != Enums.EmployeeLogEldEventType.ChangeInDriversIndication))
                continue;
            if (event.getEventRecordStatus() == null || event.getEventRecordStatus() != Enums.EmployeeLogEldEventRecordStatus.Active.getValue())
                continue;
            if (event.getEventDateTime() == null)
                continue;
            if ((event.getDutyStatusEnum() == null || event.getDutyStatusEnum().getValue() == DutyStatusEnum.NULL) &&
                    event.getEventType() != Enums.EmployeeLogEldEventType.ChangeInDriversIndication)
                continue;


            if (!isPC) {
                // check if we are beginning PC
                isPC = event.getEventType() == Enums.EmployeeLogEldEventType.ChangeInDriversIndication && event.getEventCode() == EmployeeLogEldEventCode.ChangeInDriversIndication_PersonalUse;
                if (isPC) {
                    dutyStatus = DutyStatusEnum.valueOfDMOEnum(DutyStatusEnum.DmoEnum_OffDuty);
                }
            }

            if (!isYM) {
                // check if we are beginning YM
                isYM = event.getEventType() == Enums.EmployeeLogEldEventType.ChangeInDriversIndication && event.getEventCode() == EmployeeLogEldEventCode.ChangeInDriversIndication_YardMoves;
                if (isYM) {
                    dutyStatus = DutyStatusEnum.valueOfDMOEnum(DutyStatusEnum.DmoEnum_OnDuty);
                }
            }

            if (isPC || isYM) {
                if (event.getEventType() == Enums.EmployeeLogEldEventType.ChangeInDriversIndication && event.getEventCode() == EmployeeLogEldEventCode.ChangeInDriversIndication_PCYMWT_Cleared) {
                    isPC = false;
                    isYM = false;
                }
                if (isPC) {
                    if (event.isDrivingEvent() || event.isOnDutyNotDrivingEvent()) {
                        // check for missing clear PC/YM event
                        isPC = false;
                    }
                }
                if (isYM) {
                    if (event.isDrivingEvent() || event.isOffDutyEvent()) {
                        // check for missing clear PC/YM event
                        isYM = false;
                    }
                }
            }

            if (dutyStatus.getValue() == DutyStatusEnum.NULL || dutyStatus == null) {
                dutyStatus = lastDutyStatus;
            }

            epochTime = (event.getStartTime().getTime() + timezoneDiff) / 1000;

            /**
             * Add end point of the previous status
             */
            if (!_dutyStatusVals.isEmpty()) {
                _dutyStatusVals.add(ConvertDutyStatusEnumToGridPoint(lastDutyStatus, empLog));
                _hourVals.add(epochTime);
                _isManuallyEditedByKMBUserVals.add(false /*end points doesn't matter */);
                _isPCVals.add(false);
                _isYMVals.add(false);
                _isPCYMEndVals.add(false);
            }

            /**
             * Add this Event as a starting point
             */
            _dutyStatusVals.add(ConvertDutyStatusEnumToGridPoint(dutyStatus, empLog));
            _hourVals.add(epochTime);
            _isManuallyEditedByKMBUserVals.add(event.getIsManuallyEditedByKMBUser());
            _isPCVals.add(isPC);
            _isYMVals.add(isYM);
            //_isPCYMEndVals.add(isPCYMEnd);

            lastDutyStatus = dutyStatus;
        }

        /**
         * Build end point of log
         */
        // Determine whether or not the selected log date is today,
        // if so then add the end point to have the grid stop at the current time.
        if (lastDutyStatus != null) {
            // 11/20/12 AMO - Modified it again so that the grid for today would not display longer then NOW
            // 7/11/11 JHM - Modified to show endpoint as current time for today's active log
            Date now = currentHomeTerminalTime;
            // We just need to get the date to compare the currentLogDateStarTime to the logDate
            Date currentLogDateStarTime = EmployeeLogUtilities.CalculateLogStartTime(context, now, currentUserHomeTerminalTimeZone);
            Date logDate = EmployeeLogUtilities.CalculateLogStartTime(context, empLog.getLogDate(), currentUserHomeTerminalTimeZone);

            // Due to time zone issues, the timezoneDiff is added to the time.
            Calendar cal1 = Calendar.getInstance();
            cal1.setTime(logDate);
            cal1.setTimeInMillis(cal1.getTimeInMillis() + timezoneDiff);

            Calendar cal2 = Calendar.getInstance();
            cal2.setTime(currentLogDateStarTime);
            cal2.setTimeInMillis(cal2.getTimeInMillis() + timezoneDiff);

            if (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
                    cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                    cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)) {
                epochTime = (now.getTime() + timezoneDiff) / 1000;
            } else {
                Date endOfDay = null;

                // determine end of day for the company
                TimeZoneEnum timeZoneEnum = GlobalState.getInstance().getCurrentUser().getHomeTerminalTimeZone();
                String dailyLogStart = GlobalState.getInstance().getCompanyConfigSettings(GlobalState.getInstance().getEobrService()).getDailyLogStartTime();
                endOfDay = EmployeeLogUtilities.CalculateLogEndTime(dailyLogStart, logDate, timeZoneEnum);

                if (endOfDay != null)
                    epochTime = (endOfDay.getTime() + timezoneDiff) / 1000;
                else
                    epochTime = 0L;
            }

            _dutyStatusVals.add(ConvertDutyStatusEnumToGridPoint(lastDutyStatus, empLog));
            _hourVals.add(epochTime);
            _isManuallyEditedByKMBUserVals.add(false /*end points doesn't matter */);
            _isPCVals.add(false);
            _isYMVals.add(false);
            _isPCYMEndVals.add(false);
        }
    }

    private Double ConvertDutyStatusEnumToGridPoint(DutyStatusEnum dutyStatus, EmployeeLog empLog) {
        if (_isOperateSpecificVehicleForOilField && !GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
            switch (dutyStatus.getValue()) {
                case DutyStatusEnum.OFFDUTY:
                    return 4.5;
                case DutyStatusEnum.SLEEPER:
                    return 3.5;
                case DutyStatusEnum.DRIVING:
                    return 2.5;
                case DutyStatusEnum.ONDUTY:
                    return 1.5;
                case DutyStatusEnum.OFFDUTYWELLSITE:
                    return 0.5;
            }
        } else {
            if (empLog.getExemptLogType().getValue() == ExemptLogTypeEnum.EXEMPTLOGTYPE100AIRMILE ||
                    empLog.getExemptLogType().getValue() == ExemptLogTypeEnum.EXEMPTLOGTYPE150AIRMILENONCDL) {
                switch (dutyStatus.getValue()) {
                    case DutyStatusEnum.OFFDUTY:
                    case DutyStatusEnum.SLEEPER:
                    case DutyStatusEnum.OFFDUTYWELLSITE:
                        return 3.5;
                    case DutyStatusEnum.DRIVING:
                    case DutyStatusEnum.ONDUTY:
                        return 0.5;
                }
            } else {
                switch (dutyStatus.getValue()) {
                    case DutyStatusEnum.OFFDUTY:
                    case DutyStatusEnum.OFFDUTYWELLSITE:
                        return 3.5;
                    case DutyStatusEnum.SLEEPER:
                        return 2.5;
                    case DutyStatusEnum.DRIVING:
                        return 1.5;
                    case DutyStatusEnum.ONDUTY:
                        return 0.5;
                }
            }
        }

        return null;
    }

    private Long[] ToObjectArray(long[] array) {
        Long[] answer = new Long[array.length];
        for (int i = 0; i < answer.length; i++) {
            answer[i] = Long.valueOf(array[i]);
        }
        return answer;
    }

    private Double[] ToObjectArray(double[] array) {
        Double[] answer = new Double[array.length];
        for (int i = 0; i < answer.length; i++) {
            answer[i] = Double.valueOf(array[i]);
        }
        return answer;
    }

    private Boolean[] ToObjectArray(boolean[] array) {
        Boolean[] answer = new Boolean[array.length];
        for (int i = 0; i < answer.length; i++) {
            answer[i] = Boolean.valueOf(array[i]);
        }
        return answer;
    }

    private long[] ToPrimativeArray(Long[] array) {
        long[] answer = new long[array.length];
        for (int i = 0; i < answer.length; i++) {
            answer[i] = array[i];
        }
        return answer;
    }

    private double[] ToPrimativeArray(Double[] array) {
        double[] answer = new double[array.length];
        for (int i = 0; i < answer.length; i++) {
            answer[i] = array[i];
        }
        return answer;
    }

    private boolean[] ToPrimativeArray(Boolean[] array) {
        boolean[] answer = new boolean[array.length];
        for (int i = 0; i < answer.length; i++) {
            answer[i] = array[i];
        }
        return answer;
    }
}
