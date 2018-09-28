package com.jjkeller.kmbapi.eldmandate;

import android.text.TextUtils;

import com.jjkeller.kmbapi.controller.EmployeeLogEldMandateController;
import com.jjkeller.kmbapi.employeelogeldevents.EmployeeLogEldEventCode;
import com.jjkeller.kmbapi.employeelogeldevents.Enums.EmployeeLogEldEventType;
import com.jjkeller.kmbapi.enums.DataDiagnosticEnum;
import com.jjkeller.kmbapi.enums.DatabusTypeEnum;
import com.jjkeller.kmbapi.kmbeobr.StatusRecord;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;

import java.util.HashMap;
import java.util.Map;

public class EventDataDiagnosticsChecker {
    private final Map<EmployeeLogEldEventType, CheckEvent> eventCheckers = createEventMap();
    private Map<EmployeeLogEldEventType, CheckEvent> createEventMap() {
        Map<EmployeeLogEldEventType, CheckEvent> map = new HashMap<>();
        map.put(EmployeeLogEldEventType.DutyStatusChange, new DutyStatusChangeChecker());
        map.put(EmployeeLogEldEventType.IntermediateLog, new IntermediateLogChecker());
        map.put(EmployeeLogEldEventType.ChangeInDriversIndication, new DriverIndicationChangeChecker());
        map.put(EmployeeLogEldEventType.LoginLogout, new LoginLogoutChecker());
        map.put(EmployeeLogEldEventType.EnginePowerUpPowerDown, new PowerChangeChecker());
        map.put(EmployeeLogEldEventType.Malfunction_DataDiagnosticDetection, new DiagnosticEventChecker());

        return map;
    }

    public DataDiagnosticEnum CheckForError(EmployeeLogEldEvent event, boolean isEditedEvent, StatusRecord statusAtEventTime) {
        if (ShouldCreateDataDiagnosticEvent(event, isEditedEvent, statusAtEventTime)) {
            return DataDiagnosticEnum.MISSING_REQUIRED_DATA_ELEMENTS;
        }

        return null;
    }

    private boolean ShouldCreateDataDiagnosticEvent(EmployeeLogEldEvent event, boolean isEditedEvent, StatusRecord statusRecord) {
        CheckEvent checker = eventCheckers.get(event.getEventType());
        if (checker == null) return false; //Skip processing for event types we don't currently handle

        return checker.ShouldGenerateDiagnosticEvent(event, isEditedEvent, statusRecord);
    }

    public abstract class CheckEvent {
        protected abstract boolean areRequiredFieldsMissing(EmployeeLogEldEvent event, boolean isEditedEvent, StatusRecord statusRecord);

        public boolean ShouldGenerateDiagnosticEvent(EmployeeLogEldEvent event, boolean isEditedEvent, StatusRecord statusRecord) {
            if(shouldSkipFurtherValidation(event, statusRecord)) {
                return false;
            }

            return areRequiredFieldsMissing(event, isEditedEvent, statusRecord);
        }

        protected boolean shouldSkipFurtherValidation(EmployeeLogEldEvent event, StatusRecord statusRecord) {
            if(shouldSkipDueToMissingLogKey(event)) {
                return true;
            }
            if(shouldSkipDueToMissingSerialNumber(event)) {
                return true;
            }
            if(shouldSkipDueToEngineOff(event, statusRecord)) {
                return true;
            }

            return false;
        }

        protected boolean isLatOrLongNullWhenGPSIsValid(EmployeeLogEldEvent event) {
            boolean isLatOrLongNull = event.getLatitude() == null || event.getLatitude() == 0
                                || event.getLongitude() == null || event.getLongitude() == 0;
            Float distanceSinceLastCoords = event.getDistanceSinceLastCoordinates();

            if (distanceSinceLastCoords != null && distanceSinceLastCoords < EmployeeLogEldMandateController.MAX_VALID_UNCERTAINTY_MILES && isLatOrLongNull) {
                return true;
            }

            return false;
        }

        protected boolean isDriversLocationDescriptionMissing(EmployeeLogEldEvent event) {
            if (event.getDistanceSinceLastCoordinates() == null) {
                return false;
            }

            boolean isGpsDistanceUncertain = event.getDistanceSinceLastCoordinates() > EmployeeLogEldMandateController.MAX_VALID_UNCERTAINTY_MILES;
            return isGpsDistanceUncertain && TextUtils.isEmpty(event.getDriversLocationDescription());
        }

        protected boolean isOdometerMissing(EmployeeLogEldEvent event) {
            if (event.getOdometer() == null || event.getOdometer() < 0) {
                return true;
            }

            return false;
        }

        protected boolean isEngOnTimeMissing(EmployeeLogEldEvent event, StatusRecord statusRecord) {
            if(statusRecord == null || ! new DatabusTypeEnum(statusRecord.getActiveBusType()).isHeavyBusType()) {
                return false;
            }
            if(event.getEngineHours() == null || event.getEngineHours() < 0) {
                return true;
            }

            return false;
        }

        protected boolean shouldSkipDueToEngineOff(EmployeeLogEldEvent event, StatusRecord statusRecord) {
            return statusRecord != null && !statusRecord.getIsEngineRunning();
        }

        protected boolean shouldSkipDueToMissingLogKey(EmployeeLogEldEvent event) {
            return event.getLogKey() == null || event.getLogKey() <= 0;
        }

        protected boolean shouldSkipDueToMissingSerialNumber(EmployeeLogEldEvent event) {
            return event.getEobrSerialNumber() == null;
        }
    }

    public class DutyStatusChangeChecker extends CheckEvent {
        @Override
        public boolean areRequiredFieldsMissing(EmployeeLogEldEvent event, boolean isEditedEvent, StatusRecord statusRecord) {
            if (isLatOrLongNullWhenGPSIsValid(event)) {
                return true;
            }

            if (isDriversLocationDescriptionMissing(event)) {
                return true;
            }

            if (isEditedEvent && event.getEventComment() == null) {
                return true;
            }

            return false;
        }

        public boolean isDriversLocationDescriptionMissing(EmployeeLogEldEvent event) {
            return super.isDriversLocationDescriptionMissing(event);
        }
    }

    private class IntermediateLogChecker extends CheckEvent {
        @Override
        public boolean areRequiredFieldsMissing(EmployeeLogEldEvent event, boolean isEditedEvent, StatusRecord statusRecord) {
            if (isLatOrLongNullWhenGPSIsValid(event)) {
                return true;
            }

            return false;
        }
    }

    public class DriverIndicationChangeChecker extends CheckEvent {
        @Override
        public boolean areRequiredFieldsMissing(EmployeeLogEldEvent event, boolean isEditedEvent, StatusRecord statusRecord) {
            if (isDriversLocationDescriptionMissing(event)) {
                return true;
            }

            if (event.getEventComment() == null) {
                return true;
            }

            return false;
        }
    }

    private class PowerChangeChecker extends CheckEvent {
        @Override
        public boolean areRequiredFieldsMissing(EmployeeLogEldEvent event, boolean isEditedEvent, StatusRecord statusRecord) {
            if(isLatOrLongNullWhenGPSIsValid(event)) {
                return true;
            }
            if(isOdometerMissing(event)) {
                return true;
            }
            if(isEngOnTimeMissing(event, statusRecord)) {
                return true;
            }

            return false;

        }

        @Override
        protected boolean shouldSkipDueToEngineOff(EmployeeLogEldEvent event, StatusRecord statusRecord) {
            boolean shouldSkip = false;

            //powerdown events by definition won't have the engine running, so we're only checking for PowerUp
            if(event.getEventCode() == EmployeeLogEldEventCode.PowerUp_ConventionalLocationPrecision
                    || event.getEventCode() == EmployeeLogEldEventCode.PowerUp_ReducedLocationPrecision) {
                if(statusRecord == null || !statusRecord.getIsEngineRunning()) {
                    shouldSkip = true;
                }
            }

            return shouldSkip;
        }
    }

    private class LoginLogoutChecker extends CheckEvent {

        @Override
        public boolean areRequiredFieldsMissing(EmployeeLogEldEvent event, boolean isEditedEvent, StatusRecord statusRecord) {
            if(isOdometerMissing(event)) {
                return true;
            }

            if(isEngOnTimeMissing(event, statusRecord)) {
                return true;
            }

            return false;
        }
    }

    private class DiagnosticEventChecker extends CheckEvent {

        @Override
        protected boolean shouldSkipFurtherValidation(EmployeeLogEldEvent event, StatusRecord statusRecord) {
            if(super.shouldSkipFurtherValidation(event, statusRecord)) {
                return true;
            }

            //skip missing data diagnostic events themselves
            return event.getEventCode() == EmployeeLogEldEventCode.EldDataDiagnosticLogged
                    || event.getEventCode() == EmployeeLogEldEventCode.EldDataDiagnosticCleared;
        }

        @Override
        protected boolean areRequiredFieldsMissing(EmployeeLogEldEvent event, boolean isEditedEvent, StatusRecord statusRecord) {
            if(isOdometerMissing(event)) {
                return true;
            }

            if(isEngOnTimeMissing(event, statusRecord)) {
                return true;
            }

            return false;
        }
    }
}
