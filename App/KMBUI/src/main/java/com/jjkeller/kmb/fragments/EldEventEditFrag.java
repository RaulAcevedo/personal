package com.jjkeller.kmb.fragments;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;

import com.jjkeller.kmb.EldEventEdit;
import com.jjkeller.kmb.adapters.EldEventDutyStatusAdapter;
import com.jjkeller.kmb.adapters.EldEventDutyStatusItem;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbapi.common.TimeKeeper;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.EmployeeLogEldMandateController;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.eldmandate.EventDataDiagnosticsChecker;
import com.jjkeller.kmbapi.employeelogeldevents.EmployeeLogEldEventCode;
import com.jjkeller.kmbapi.employeelogeldevents.Enums;
import com.jjkeller.kmbapi.enums.EmployeeLogProvisionTypeEnum;
import com.jjkeller.kmbapi.enums.RuleSetTypeEnum;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;
import com.jjkeller.kmbui.R;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Fragment for Add or Edit of EmployeeLogEldEvent record.
 */
public class EldEventEditFrag extends BaseFragment {
    private static final String EXTRA_LOGKEY = "logKey";
    private static final String EXTRA_PRIMARYKEY = "primaryKey";
    private static final String EXTRA_LOGDATE = "logDate";
    private static final String EXTRA_ORIGINALEVENTTYPE = "originalEventType";
    private static final String EXTRA_ORIGINALEVENTCODE = "originalEventCode";
    private static final String EXTRA_ORIGINALEVENTRECORDORIGIN = "originalEventRecordOrigin";
    private static final String EXTRA_ORIGINALSTARTTIME = "originalStartTime";
    private static final String EXTRA_ORIGINALENDTIME = "originalEndTime";
    private static final String EXTRA_ORIGINALSPECIALDRIVINGCONDITION = "originalSpecialDrivingCondition";
    private static final String EXTRA_ISENDTIMETODAYSCURRENTTIME = "isEndTimeTodaysCurrentTime";
    private static final String EXTRA_ORIGINALSUBSTATUS = "originalSubStatus";
    private static final String EXTRA_STATUS = "status";
    private static final String EXTRA_STARTTIME = "startTime";
    private static final String EXTRA_ENDTIME = "endTime";
    private static final String EXTRA_LOCATION = "location";
    private static final String EXTRA_DISTANCESINCELASTCOORDINATES = "distanceSinceLastCoordinates";
    private static final String EXTRA_TRACTORNUMBER = "tractorNumber";
    private static final String EXTRA_TRAILERNUMBER = "trailerNumber";
    private static final String EXTRA_SHIPMENTINFO = "shipmentInfo";
    private static final String EXTRA_RULESET = "ruleset";
    private static final String EXTRA_LOGREMARK = "logRemark";
    private static final String EXTRA_DRIVINGDISTANCE = "drivingDistance";
    private static final String EXTRA_DRIVERANNOTATION = "driversAnnotation";
    private static final String STATUS_MANUAL_LOCATION = "M";

    private Boolean _updatingModelToView = false;

    private Integer _logKey;
    private Long _primaryKey;
    private String _logDate;
    private Enums.EmployeeLogEldEventType _originalEventType;
    private Integer _originalEventCode;
    private Integer _originalEventRecordOrigin;
    private Date _originalStartTime;
    private Date _originalEndTime;
    private Enums.SpecialDrivingCategory _originalSubStatus;
    private EmployeeLogProvisionTypeEnum _originalSpecialDrivingCondition;
    private Float _originalDistanceSinceLastCoordinates = 0.0F;
    private boolean _isEndTimeTodaysCurrentTime = false;

    EldEventDutyStatusAdapter _dutystatusAdapterStandard;
    EldEventDutyStatusAdapter _dutystatusAdapterAutomaticallyRecordedDriving;

    private ScrollView _scrollView;
    private TextView _txtNoRecords;

    private Drawable _spinnerBackground;
    private Drawable _textViewBackground;

    private Spinner _cboDutyStatus;
    private TextView _lblReqDutyStatus;
    private Button _btnStartTime;
    private Button _btnEndTime;
    private EditText _editLocation;
    private EditText _editUnitNumber;
    private EditText _editTrailerNumber;
    private EditText _editShipmentInfo;
    private ArrayAdapter<String> _rulesetAdapter;
    private Spinner _cboRuleset;
    private EditText _editDriversAnnotation;
    private TextView _btnEndTimeAsterisk;
    private TableRow _rowDrivingDistance;
    private EditText _editDrivingDistance;
    private TextView _textDrivingDistanceRequiredAsterisk;
    private TextView _lblEventOrigin;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.f_eldeventedit, container, false);

        findControls(v);

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        loadControls(savedInstanceState);
    }

    /**
     * Called after onRestoreInstanceState(Bundle), onRestart(), or onPause(), for your fragment to start interacting with the user.
     */
    @Override
    public void onResume() {
        super.onResume();

        addChangeListeners();
    }

    /**
     * Called as part of the lifecycle when an fragment is going into the background, but has not (yet) been killed.
     */
    @Override
    public void onPause() {
        super.onPause();

        removeChangeListeners();
    }

    /**
     * Called during screen rotation to persist values so the screen can be re-created.
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(EXTRA_LOGKEY, _logKey);
        outState.putLong(EXTRA_PRIMARYKEY, _primaryKey);
        outState.putString(EXTRA_LOGDATE, _logDate);

        outState.putString(EXTRA_ORIGINALEVENTTYPE, _originalEventType == null ? "" : Integer.toString(_originalEventType.getValue()));
        outState.putString(EXTRA_ORIGINALEVENTCODE, _originalEventCode == null ? "" : _originalEventCode.toString());
        outState.putString(EXTRA_ORIGINALEVENTRECORDORIGIN, _originalEventRecordOrigin == null ? "" : _originalEventRecordOrigin.toString());
        outState.putString(EXTRA_ORIGINALSTARTTIME, _originalStartTime == null ? "" : DateUtility.getHomeTerminalReferenceTimestampFormat().format(_originalStartTime));
        outState.putString(EXTRA_ORIGINALENDTIME, _originalEndTime == null ? "" : DateUtility.getHomeTerminalReferenceTimestampFormat().format(_originalEndTime));
        outState.putSerializable(EXTRA_ORIGINALSPECIALDRIVINGCONDITION, _originalSpecialDrivingCondition);
        outState.putSerializable(EXTRA_ORIGINALSUBSTATUS, _originalSubStatus);
        outState.putBoolean(EXTRA_ISENDTIMETODAYSCURRENTTIME, _isEndTimeTodaysCurrentTime);

        outState.putInt(EXTRA_STATUS, _cboDutyStatus.getSelectedItemPosition());
        outState.putString(EXTRA_STARTTIME, _btnStartTime.getText().toString().trim());
        outState.putString(EXTRA_ENDTIME, _btnEndTime.getText().toString().trim());
        outState.putString(EXTRA_LOCATION, _editLocation.getText().toString().trim());
        outState.putFloat(EXTRA_DISTANCESINCELASTCOORDINATES, _originalDistanceSinceLastCoordinates);
        outState.putString(EXTRA_TRACTORNUMBER, _editUnitNumber.getText().toString().trim());
        outState.putString(EXTRA_TRAILERNUMBER, _editTrailerNumber.getText().toString().trim());
        outState.putString(EXTRA_SHIPMENTINFO, _editShipmentInfo.getText().toString().trim());
        outState.putString(EXTRA_RULESET, _cboRuleset.getSelectedItem().toString().trim());
        outState.putString(EXTRA_DRIVINGDISTANCE, _editDrivingDistance.getText().toString().trim());
        outState.putString(EXTRA_DRIVERANNOTATION, _editDriversAnnotation.getText().toString().trim());

        super.onSaveInstanceState(outState);
    }

    /**
     * Store a handle to each control (child view).
     */
    protected void findControls(View v) {
        _scrollView = (ScrollView) v.findViewById(R.id.scrollView);
        _cboDutyStatus = (Spinner) v.findViewById(R.id.cboDutyStatus);
        _spinnerBackground = _cboDutyStatus.getBackground();
        _lblReqDutyStatus = (TextView) v.findViewById(R.id.reqDutyStatus);

        _btnStartTime = (Button) v.findViewById(R.id.btnStartTime);
        _btnStartTime.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ShowTimeWithSecondsPickerDialog(_btnStartTime);
            }
        });

        _btnEndTime = (Button) v.findViewById(R.id.btnEndTime);
        _btnEndTime.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ShowTimeWithSecondsPickerDialog(_btnEndTime);
            }
        });
        _btnEndTimeAsterisk = (TextView) v.findViewById(R.id.btnEndTimeAsterisk);
        _editLocation = (EditText) v.findViewById(R.id.editLocation);
        _editUnitNumber = (EditText) v.findViewById(R.id.editUnitNumber);
        _editTrailerNumber = (EditText) v.findViewById(R.id.editTrailerNumber);
        _editShipmentInfo = (EditText) v.findViewById(R.id.editShipmentInfo);
        _cboRuleset = (Spinner) v.findViewById(R.id.cboRuleset);
        _rowDrivingDistance = (TableRow) v.findViewById(R.id.rowDrivingDistance);
        _editDrivingDistance = (EditText) v.findViewById(R.id.editDrivingDistance);
        _textDrivingDistanceRequiredAsterisk = (TextView) v.findViewById(R.id.textDrivingDistanceRequiredAsterisk);
        _textViewBackground = _editDrivingDistance.getBackground();
        _editDriversAnnotation = (EditText) v.findViewById(R.id.editDriversAnnotation);
        _lblEventOrigin = (TextView) v.findViewById(R.id.lblEventOrigin);

        /**
         * Load adapters here instead of LoadControls so there isn't as noticeable
         * blank white space in the fragment layout as it waits to load.
         */

        // Populate spinner with available Rulesets
        List<String> rulesetAvailList = new ArrayList<String>();
        for (RuleSetTypeEnum ruleset : GlobalState.getInstance().getCurrentUser().getAvailableRulesets()) {
            if (ruleset.getString(getActivity()).length() > 0) {
                rulesetAvailList.add(ruleset.getString(getActivity()));
            }
        }
        Collections.sort(rulesetAvailList);

        _rulesetAdapter = new ArrayAdapter<String>(getActivity(), R.layout.kmb_spinner_item, rulesetAvailList);
        _rulesetAdapter.setDropDownViewResource(android.R.layout.simple_list_item_checked);
        _cboRuleset.setAdapter(_rulesetAdapter);

        _txtNoRecords = (TextView) v.findViewById(R.id.txtNoRecords);
    }

    private int setPosition(Enums.SpecialDrivingCategory subStatus, EmployeeLogEldEvent event, EldEventDutyStatusAdapter adapter) {
        int position;
        if (subStatus != Enums.SpecialDrivingCategory.None) {
            position = adapter.getSelectionIndex(subStatus);
        } else {
            position = adapter.getSelectionIndex(event.getEventType(), event.getEventCode());
        }
        return position;
    }

    /**
     * Populate DutyStatus Adapters with choices.
     */
    protected void loadDutyStatusAdapter(Enums.SpecialDrivingCategory specialDrivingCategory) {
        List<EldEventDutyStatusItem> standardChoices = new ArrayList<EldEventDutyStatusItem>();
        List<EldEventDutyStatusItem> automaticallyRecordedDrivingChoices = new ArrayList<EldEventDutyStatusItem>();

        /**
         * Standard
         */

        // Status: Off Duty, Sleeper, Driving, On Duty
        String[] standardStrings = getResources().getStringArray(R.array.EldEventDutyStatus_array_edit);
        standardChoices.add(new EldEventDutyStatusItem(standardStrings[0], null, Enums.EmployeeLogEldEventType.DutyStatusChange, EmployeeLogEldEventCode.DutyStatus_OffDuty, null));

        if (specialDrivingCategory != null && specialDrivingCategory != Enums.SpecialDrivingCategory.None) {
            if (isPersonalConveyanceAllowed()) {
                standardChoices.add(new EldEventDutyStatusItem(standardStrings[0], getResources().getString(R.string.personalconveyance), Enums.EmployeeLogEldEventType.DutyStatusChange, EmployeeLogEldEventCode.DutyStatus_OffDuty, Enums.SpecialDrivingCategory.PersonalConveyance));
            }
        }

        standardChoices.add(new EldEventDutyStatusItem(standardStrings[1], null, Enums.EmployeeLogEldEventType.DutyStatusChange, EmployeeLogEldEventCode.DutyStatus_Sleeper, null));
        standardChoices.add(new EldEventDutyStatusItem(getString(R.string.drivingManual), null, Enums.EmployeeLogEldEventType.DutyStatusChange, EmployeeLogEldEventCode.DutyStatus_Driving, null));
        standardChoices.add(new EldEventDutyStatusItem(standardStrings[3], null, Enums.EmployeeLogEldEventType.DutyStatusChange, EmployeeLogEldEventCode.DutyStatus_OnDuty, null));

        if (specialDrivingCategory != null && specialDrivingCategory != Enums.SpecialDrivingCategory.None) {
            if (isYardMoveUseAllowed()) {
                standardChoices.add(new EldEventDutyStatusItem(standardStrings[3], getResources().getString(R.string.yardmove), Enums.EmployeeLogEldEventType.DutyStatusChange, EmployeeLogEldEventCode.DutyStatus_OnDuty, Enums.SpecialDrivingCategory.YardMove));
            }
            if (isHyrailAllowed()) {
                standardChoices.add(new EldEventDutyStatusItem(standardStrings[3], getResources().getString(R.string.hyrail), Enums.EmployeeLogEldEventType.DutyStatusChange, EmployeeLogEldEventCode.DutyStatus_OnDuty, Enums.SpecialDrivingCategory.Hyrail));
            }
            if (isNonRegulatedAllowed()) {
                standardChoices.add(new EldEventDutyStatusItem(standardStrings[3], getResources().getString(R.string.nonregulated), Enums.EmployeeLogEldEventType.DutyStatusChange, EmployeeLogEldEventCode.DutyStatus_OnDuty, Enums.SpecialDrivingCategory.NonRegulated));
            }
        }

        if (GlobalState.getInstance().getCurrentUser().isOffDutyWellSiteAllowed()) {
            standardChoices.add(new EldEventDutyStatusItem(standardStrings[4], null, Enums.EmployeeLogEldEventType.DutyStatusChange, EmployeeLogEldEventCode.DutyStatus_OffDutyWellSite, null));
        }

        _dutystatusAdapterStandard = new EldEventDutyStatusAdapter(getActivity(), standardChoices);


        /**
         * Special category
         */

        // Status: Off Duty - PersonalConveyance, Driving, On Duty - Yard Move
        if (isPersonalConveyanceAllowed()) {
            automaticallyRecordedDrivingChoices.add(new EldEventDutyStatusItem(getString(R.string.offduty), getString(R.string.personalconveyance), Enums.EmployeeLogEldEventType.ChangeInDriversIndication, EmployeeLogEldEventCode.ChangeInDriversIndication_PersonalUse, Enums.SpecialDrivingCategory.PersonalConveyance));
        }

        automaticallyRecordedDrivingChoices.add(new EldEventDutyStatusItem(getString(R.string.driving), null, Enums.EmployeeLogEldEventType.DutyStatusChange, EmployeeLogEldEventCode.DutyStatus_Driving, null));

        if (isYardMoveUseAllowed()) {
            automaticallyRecordedDrivingChoices.add(new EldEventDutyStatusItem(getString(R.string.onduty), getString(R.string.yardmove), Enums.EmployeeLogEldEventType.ChangeInDriversIndication, EmployeeLogEldEventCode.ChangeInDriversIndication_YardMoves, Enums.SpecialDrivingCategory.YardMove));
        }

        // Hyrail, non-reg
        if (isHyrailAllowed()) {
            automaticallyRecordedDrivingChoices.add(new EldEventDutyStatusItem(getString(R.string.onduty), getString(R.string.hyrail), Enums.EmployeeLogEldEventType.DutyStatusChange, EmployeeLogEldEventCode.DutyStatus_OnDuty, Enums.SpecialDrivingCategory.Hyrail));
        }

        if (isNonRegulatedAllowed()) {
            automaticallyRecordedDrivingChoices.add(new EldEventDutyStatusItem(getString(R.string.onduty), getString(R.string.nonregulated), Enums.EmployeeLogEldEventType.DutyStatusChange, EmployeeLogEldEventCode.DutyStatus_OnDuty, Enums.SpecialDrivingCategory.NonRegulated));
        }

        _dutystatusAdapterAutomaticallyRecordedDriving = new EldEventDutyStatusAdapter(getActivity(), automaticallyRecordedDrivingChoices);
    }


    /**
     * Populate lookup controls with choices.
     */
    protected void loadControls(Bundle savedInstanceState) {
        EldEventEdit eldEventEdit = (EldEventEdit) getActivity();
        if (savedInstanceState == null) {
            eldEventEdit.updateFragmentUI();
        } else {  /* orientation change */
            _logKey = savedInstanceState.getInt(EXTRA_LOGKEY);
            _primaryKey = savedInstanceState.getLong(EXTRA_PRIMARYKEY);
            _logDate = savedInstanceState.getString(EXTRA_LOGDATE);

            String eventType = savedInstanceState.getString(EXTRA_ORIGINALEVENTTYPE);
            if (eventType.length() == 0) {
                _originalEventType = null;
            } else {
                _originalEventType = Enums.EmployeeLogEldEventType.setFromInt(Integer.parseInt(eventType));
            }

            String eventCode = savedInstanceState.getString(EXTRA_ORIGINALEVENTCODE);
            if (eventCode.length() == 0) {
                _originalEventCode = null;
            } else {
                _originalEventCode = Integer.parseInt(eventCode);
            }

            String eventRecordOrigin = savedInstanceState.getString(EXTRA_ORIGINALEVENTRECORDORIGIN);
            if (eventRecordOrigin.length() == 0) {
                _originalEventRecordOrigin = null;
            } else {
                _originalEventRecordOrigin = Integer.parseInt(eventRecordOrigin);
            }

            String date = savedInstanceState.getString(EXTRA_ORIGINALSTARTTIME);
            if (date.length() == 0) {
                _originalStartTime = null;
            } else {
                _originalStartTime = getDateFromString(date);
            }

            date = savedInstanceState.getString(EXTRA_ORIGINALENDTIME);
            if (date.length() == 0) {
                _originalEndTime = null;
            } else {
                _originalEndTime = getDateFromString(date);
            }

            _originalSpecialDrivingCondition = (EmployeeLogProvisionTypeEnum) savedInstanceState.getSerializable(EXTRA_ORIGINALSPECIALDRIVINGCONDITION);
            _originalSubStatus = (Enums.SpecialDrivingCategory) savedInstanceState.getSerializable(EXTRA_ORIGINALSUBSTATUS);
            _originalDistanceSinceLastCoordinates = savedInstanceState.getFloat(EXTRA_DISTANCESINCELASTCOORDINATES);

            _isEndTimeTodaysCurrentTime = savedInstanceState.getBoolean(EXTRA_ISENDTIMETODAYSCURRENTTIME);
            loadDutyStatusAdapter(_originalSubStatus);
            int eventCodeInt = getEventCodeFromAdapter(savedInstanceState.getInt(EXTRA_STATUS));
            EmployeeLogEldEvent model = new EmployeeLogEldEvent(getDateFromString(_logDate, savedInstanceState.getString(EXTRA_STARTTIME)), new EmployeeLogEldEventCode(eventCodeInt), Enums.EmployeeLogEldEventType.DutyStatusChange);
            if (savedInstanceState.getString(EXTRA_STARTTIME).length() == 0) {
                model.setEventDateTime(null);
            }
            model.setPrimaryKey(_primaryKey);
            model.setDriversLocationDescription(savedInstanceState.getString(EXTRA_LOCATION));
            model.setDistanceSinceLastCoordinates(_originalDistanceSinceLastCoordinates);
            model.setTractorNumber(savedInstanceState.getString(EXTRA_TRACTORNUMBER));
            model.setTrailerNumber(savedInstanceState.getString(EXTRA_TRAILERNUMBER));
            model.setShipmentInfo(savedInstanceState.getString(EXTRA_SHIPMENTINFO));
            model.setRuleSet(RuleSetTypeEnum.valueOf(getActivity(), savedInstanceState.getString(EXTRA_RULESET)));
            model.setLogRemark(savedInstanceState.getString(EXTRA_LOGREMARK));

            String savedDrivingDistance = savedInstanceState.getString(EXTRA_DRIVINGDISTANCE);
            if (!TextUtils.isEmpty(savedDrivingDistance)) {
                try {
                    model.setDistance(Integer.parseInt(savedDrivingDistance));
                } catch (NumberFormatException ex) {
                    // do nothing
                }
            }

            model.setEventComment(savedInstanceState.getString(EXTRA_DRIVERANNOTATION));

            Pair<EmployeeLogEldEvent, Enums.SpecialDrivingCategory> pairEldEventWithSubStatus = new Pair<>(model, _originalSubStatus);
            // update the UI based on rotation saved state
            updateModelToView(pairEldEventWithSubStatus, _logKey, _logDate, getDateFromString(_logDate, savedInstanceState.getString(EXTRA_ENDTIME)), _isEndTimeTodaysCurrentTime, eldEventEdit.getIsCurrentDay(), true);
        }
    }

    /**
     * Update UI from the model to the view.
     * Store the original value in the controls Tag field so we can set isEditing only
     * when the value has changed otherwise onLayout will fire unwanted selection change event.
     */
    public void updateModelToView(Pair<EmployeeLogEldEvent, Enums.SpecialDrivingCategory> pairEldEventWithSubStatus, Integer logKey, String logDate, Date impliedEndTime, boolean isEndTimeTodaysCurrentTime, boolean _isCurrentDay, boolean updateDueToOrientationChange) {
        EmployeeLogEldEvent event = pairEldEventWithSubStatus.first;
        Enums.SpecialDrivingCategory subStatus = pairEldEventWithSubStatus.second;
        //Set asterisk visibility depending on event
        if (isEndTimeTodaysCurrentTime || (event.getPrimaryKey() == -1 && _isCurrentDay)) {
            _btnEndTimeAsterisk.setVisibility(View.INVISIBLE);
        } else {
            _btnEndTimeAsterisk.setVisibility(View.VISIBLE);
        }

        _updatingModelToView = true;

        // store parent fields necessary for adding new records
        _logKey = logKey;
        _primaryKey = event.getPrimaryKey();
        _logDate = logDate;

        // store the original times so we can compare against during validation
        if (!updateDueToOrientationChange) {
            _originalEventType = event.getPrimaryKey() == -1 ? null : event.getEventType();
            _originalEventCode = event.getPrimaryKey() == -1 ? null : event.getEventCode();
            _originalEventRecordOrigin = event.getPrimaryKey() == -1 ? null : event.getEventRecordOrigin();
            _originalStartTime = event.getPrimaryKey() == -1 ? null : event.getEventDateTime();
            _originalEndTime = impliedEndTime;
            _isEndTimeTodaysCurrentTime = isEndTimeTodaysCurrentTime;
            _originalSubStatus = subStatus;
            _originalDistanceSinceLastCoordinates = event.getDistanceSinceLastCoordinates() != null ? event.getDistanceSinceLastCoordinates() : 0.0F;
            loadDutyStatusAdapter(subStatus);
        }

        int position;
        if (_originalEventType != null && _originalEventType == Enums.EmployeeLogEldEventType.DutyStatusChange && _originalEventRecordOrigin != null && _originalEventRecordOrigin == Enums.EmployeeLogEldEventRecordOrigin.AutomaticallyRecorded) {

            if (_originalEventCode != null && _originalEventCode == EmployeeLogEldEventCode.DutyStatus_Driving) {
                position = setPosition(subStatus, event, _dutystatusAdapterAutomaticallyRecordedDriving);
                _cboDutyStatus.setAdapter(_dutystatusAdapterAutomaticallyRecordedDriving);
            } else {
                position = setPosition(subStatus, event, _dutystatusAdapterStandard);
                _cboDutyStatus.setAdapter(_dutystatusAdapterStandard);
            }
        } else {
            position = setPosition(subStatus, event, _dutystatusAdapterStandard);
            _cboDutyStatus.setAdapter(_dutystatusAdapterStandard);
        }

        // if this is an automatic driving event, disable the dropdown
        if (this.isEditingAutomaticDrivingEvent()) {
            _cboDutyStatus.setEnabled(false);
            _cboDutyStatus.setBackgroundColor(Color.TRANSPARENT);
            _lblReqDutyStatus.setTextColor(Color.TRANSPARENT);
        } else {
            _cboDutyStatus.setEnabled(true);
            _cboDutyStatus.setBackgroundDrawable(_spinnerBackground);
            _lblReqDutyStatus.setTextColor(getResources().getColor(R.color.red));
        }

        _cboDutyStatus.setTag(position);
        _cboDutyStatus.setSelection(position);

        if (event.getPrimaryKey() > -1 || updateDueToOrientationChange) {
            String time = event.getEventDateTime() == null ? "" : DateUtility.getHomeTerminalTime12HourFormatWithSeconds().format(event.getEventDateTime());
            _btnStartTime.setTag(time);
            _btnStartTime.setText(time);

            time = impliedEndTime == null ? "" : DateUtility.getHomeTerminalTime12HourFormatWithSeconds().format(impliedEndTime);
            _btnEndTime.setTag(time);
            _btnEndTime.setText(time);
            _btnEndTime.setEnabled(!isEndTimeTodaysCurrentTime);
        }

        _editLocation.setTag(event.getDriversLocationDescription() == null ? "" : event.getDriversLocationDescription());
        _editLocation.setText(event.getDriversLocationDescription());

        _editUnitNumber.setTag(event.getTractorNumber() == null ? "" : event.getTractorNumber());
        _editUnitNumber.setText(event.getTractorNumber());

        _editTrailerNumber.setTag(event.getTrailerNumber() == null ? "" : event.getTrailerNumber());
        _editTrailerNumber.setText(event.getTrailerNumber());

        _editShipmentInfo.setTag(event.getShipmentInfo() == null ? "" : event.getShipmentInfo());
        _editShipmentInfo.setText(event.getShipmentInfo());

        position = _rulesetAdapter.getPosition(event.getRuleSet().getString(getActivity().getBaseContext()));
        _cboRuleset.setTag(position);
        _cboRuleset.setSelection(position);

        String drivingDistance = event.getDistance() == null ? "" : String.valueOf(event.getDistance());
        _editDrivingDistance.setTag(drivingDistance);
        _editDrivingDistance.setText(drivingDistance);

        // control the visibility of the Driving Distance fields
        setDrivingDistanceVisibilityBasedOnDutyStatus(event.getEventType(), event.getEventCode());

        _editDriversAnnotation.setTag(event.getEventComment() == null ? "" : event.getEventComment());
        _editDriversAnnotation.setText(event.getEventComment());

        if (event.getPrimaryKey() != -1 && event.isDrivingEvent() && _originalEventRecordOrigin != null && _originalEventRecordOrigin != com.jjkeller.kmbapi.employeelogeldevents.Enums.EmployeeLogEldEventRecordOrigin.AutomaticallyRecorded) {
            _lblEventOrigin.setVisibility(View.VISIBLE);
        } else {
            _lblEventOrigin.setVisibility(View.GONE);
        }

        _updatingModelToView = false;
    }

    /**
     * Transfer UI data from the view to the model.
     */
    public Pair<EmployeeLogEldEvent, Enums.SpecialDrivingCategory> updateViewToModel(EmployeeLogEldEvent model) {

        int position = _cboDutyStatus.getSelectedItemPosition();
        EldEventDutyStatusItem item = (EldEventDutyStatusItem) _cboDutyStatus.getAdapter().getItem(position);

        model.setEventType(item.getEventType());
        model.setEventCode(item.getEventCode());
        model.setDriversLocationDescription(_editLocation.getText().toString().trim());

        model.setTractorNumber(_editUnitNumber.getText().toString().trim());
        model.setTrailerNumber(_editTrailerNumber.getText().toString().trim());
        model.setShipmentInfo(_editShipmentInfo.getText().toString().trim());

        model.setRuleSet(RuleSetTypeEnum.valueOf(getActivity(), (String) _cboRuleset.getSelectedItem()));
        model.setEventComment(_editDriversAnnotation.getText().toString().trim());
        model.setEventDateTime(getStartTime());

        // if changing existing Manual Drive to another Duty Status - null data related to driving
        if (isDrivingEvent(_originalEventType, _originalEventCode) && !isDrivingEvent(item.getEventType(), item.getEventCode())) {
            model.setAccumulatedVehicleMiles(null);
            model.setDistance(null);
            model.setEngineHours(null);
            model.setDistanceSinceLastCoordinates(null);
            model.setEndOdometer(null);
        } else if (_editDrivingDistance.getVisibility() == View.VISIBLE && _editDrivingDistance.isEnabled()) {
            try {
                model.setDistance(Integer.parseInt(_editDrivingDistance.getText().toString().trim()));
            } catch (NumberFormatException ex) {
            }

            // add 'M' for manual location when adding new Event
            if (model.getPrimaryKey() == -1) {
                model.setEventRecordOrigin(Enums.EmployeeLogEldEventRecordOrigin.EditedEnteredByDriver);
                model.setLatitudeStatusCode(STATUS_MANUAL_LOCATION);
                model.setLongitudeStatusCode(STATUS_MANUAL_LOCATION);
                model.setGeolocation(null);
            }
        }

        return new Pair<>(model, item.getSubStatus());
    }

    public void displayNoRecords() {
        _scrollView.setVisibility(View.GONE);
        _txtNoRecords.setVisibility(View.VISIBLE);
    }

    /**
     * Helper method to get/set the parent Activities isEditing flag.
     */
    private Boolean getIsEditing() {
        // if in the process of updateViewToModel() fake the onChange listeners so
        // we don't slow down navigation by checking if control value has changed.
        if (_updatingModelToView) {
            return true;
        }

        return ((EldEventEdit) getActivity()).getIsEditing();
    }

    public void setIsEditing() {
        ((EldEventEdit) getActivity()).setIsEditing(true);

        // clear the Annotation text to force the user to add a new comment for the current changes.
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                _editDriversAnnotation.setText("");
            }
        });
    }

    /**
     * Helper method to return Start Time as a Date object.
     */
    private Date getStartTime() {
        String time = _btnStartTime.getText().toString();

        return getDateFromString(_logDate, time);
    }

    /**
     * Helper method to return EventCode from DutyStatus adapter after orientation change.
     */
    private int getEventCodeFromAdapter(int dutyStatusPosition) {

        EldEventDutyStatusItem dutyStatusItem = null;
        if (_originalEventType != null && _originalEventType == Enums.EmployeeLogEldEventType.DutyStatusChange && _originalEventRecordOrigin != null && _originalEventRecordOrigin == Enums.EmployeeLogEldEventRecordOrigin.AutomaticallyRecorded) {

            if (_originalEventCode != null && _originalEventCode == EmployeeLogEldEventCode.DutyStatus_Driving) {
                dutyStatusItem = (EldEventDutyStatusItem) _dutystatusAdapterAutomaticallyRecordedDriving.getItem(dutyStatusPosition);
            } else {
                dutyStatusItem = (EldEventDutyStatusItem) _dutystatusAdapterStandard.getItem(dutyStatusPosition);
            }
        } else {
            dutyStatusItem = (EldEventDutyStatusItem) _dutystatusAdapterStandard.getItem(dutyStatusPosition);
        }

        return dutyStatusItem.getEventCode();
    }

    /**
     * Helper method to determine if the driver's Employee Rule is configured for 'Yard Move Allowed?'
     */
    private boolean isYardMoveUseAllowed() {
        return (((EldEventEdit) getActivity()).getMandateController().getCurrentUser().getYardMoveAllowed() && GlobalState.getInstance().getIsCurrentUserTheDesignatedDriver() && GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled());
    }

    /**
     * Helper method to determine if the driver's Employee Rule is configured for 'Is Personal Conveyance Allowed?'
     */
    private boolean isPersonalConveyanceAllowed() {
        return (((EldEventEdit) getActivity()).getMandateController().getCurrentUser().getIsPersonalConveyanceAllowed() && GlobalState.getInstance().getIsCurrentUserTheDesignatedDriver() && GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) && GlobalState.getInstance().getFeatureService().getPersonalConveyanceEnabled();
    }

    private boolean isHyrailAllowed() {
        return (((EldEventEdit) getActivity()).getMandateController().getCurrentUser().getIsHyrailAllowed() && GlobalState.getInstance().getIsCurrentUserTheDesignatedDriver() && GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) && GlobalState.getInstance().getFeatureService().getHyrailEnabled();
    }

    private boolean isNonRegulatedAllowed() {
        return (((EldEventEdit) getActivity()).getMandateController().getCurrentUser().getIsNonRegDrivingAllowed() &&
                GlobalState.getInstance().getIsCurrentUserTheDesignatedDriver() &&
                GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) && GlobalState.getInstance().getFeatureService().getNonRegDrivingEnabled();
    }

    /**
     * Helper method to return End Time as a Date object.
     */
    public Date getEndTime() {
        String time = _btnEndTime.getText().toString();

        return getDateFromString(_logDate, time);
    }

    private Date getDateFromString(String date, String time) {
        try {
            return DateUtility.getHomeTerminalDateTimeFormat12HourWithSeconds().parse(date + " " + time);
        } catch (ParseException e) {

            Log.e("UnhandledCatch", e.getMessage() + ": " + Log.getStackTraceString(e));
        }

        return null;
    }

    private Date getDateFromString(String dateTimeString) {
        try {
            return DateUtility.getHomeTerminalReferenceTimestampFormat().parse(dateTimeString);
        } catch (ParseException e) {

            Log.e("UnhandledCatch", e.getMessage() + ": " + Log.getStackTraceString(e));
        }

        return null;
    }

    /**
     * Helper method to use existing CheckEvent to determine if Location should be required or not
     * (i.e. DistanceSinceLastCoordinate > 5)
     */
    private Boolean isDriversLocationRequired(EmployeeLogEldEvent event) {
        try {
            EmployeeLogEldEvent eventClone = (EmployeeLogEldEvent) event.clone();
            eventClone.setDriversLocationDescription(null);

            return new EventDataDiagnosticsChecker().new DutyStatusChangeChecker().isDriversLocationDescriptionMissing(eventClone);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        return true;
    }

    // validating location
    public static boolean isValidLocation(String location) {
        String regexAddress = "^[a-zA-Z0-9 ',.-]*$";

        Pattern pattern = Pattern.compile(regexAddress);
        Matcher matcher = pattern.matcher(location);
        return matcher.matches();
    }

    /**
     * Valid the object and return list of broken rules (if any)
     */
    public List<String> getBrokenRules(EmployeeLogEldEvent[] eldEvents, boolean isCurrentDay) {
        List<String> brokenRules = new ArrayList<String>();

        // Start and end times are always required
        if (_btnStartTime.getText().toString().length() == 0) {
            brokenRules.add(getString(R.string.requiredstarttime));
        }

        if (_btnEndTime.getText().toString().length() == 0 && !isCurrentDay) {
            brokenRules.add(getString(R.string.requiredendtime));
        }

        // Start time can only be edited to an earlier time
        if (_originalStartTime != null) {
            Date startTime = getStartTime();
            if (startTime != null && startTime.after(_originalStartTime)) {
                brokenRules.add(getString(R.string.starttimeonlyeditedearlier));
            }
        }

        // End time can only be edited to a later time
        if (_originalEndTime != null) {
            Date endTime = getEndTime();
            if (endTime != null && endTime.before(_originalEndTime)) {
                brokenRules.add(getString(R.string.endtimeonlyeditedlater));
            }
        }

        // End time must be after Start time
        if (_btnStartTime.getText().toString().length() > 0 && _btnEndTime.getText().toString().length() > 0) {
            if (getStartTime().compareTo(getEndTime()) > -1) {
                brokenRules.add(getString(R.string.starttimemustbebeforeendtime));
            }
        }

        // End time can not be changed to a future time
        if (getEndTime() != null && TimeKeeper.getInstance().getCurrentDateTime().toDate().before(getEndTime())) {
            brokenRules.add(getString(R.string.endtimecanotbeinfuture));
        }

        // Location must be 5 characters or longer
        if (_editLocation.getText().toString().trim().length() < 5) {
            brokenRules.add(getString(R.string.msgactuallocationminimumlengtherror));
        }

        if (!isValidLocation(_editLocation.getText().toString().trim())) {
            brokenRules.add(getString(R.string.requiredlocationnospecialcharacters));
        }

        int position = _cboDutyStatus.getSelectedItemPosition();
        EldEventDutyStatusItem item = (EldEventDutyStatusItem) _cboDutyStatus.getAdapter().getItem(position);

        // Distance required and must be numeric for Manual Driving
        if (isDrivingEvent(item.getEventType(), item.getEventCode()) && !isEditingAutomaticDrivingEvent()) {
            List<String> manualDriveBrokenRules = getManualDrivingBrokenRules(_editDrivingDistance.getText().toString(), getActivity().getApplicationContext());
            for (String error : manualDriveBrokenRules) {
                brokenRules.add(error);
            }
        }

        // Drivers Annotation must be 4 characters or longer
        if (_editDriversAnnotation.getText().toString().trim().length() < 4) {
            brokenRules.add(getString(R.string.requireddriversannotation));
        }

        // Determine if the Edit will overlap automatically recorded driving time
        if (((EldEventEdit) getActivity()).getMandateController().willEditInvalidateAutomaticDriveTime(eldEvents, _primaryKey, getStartTime(), getEndTime(), null) != EmployeeLogEldMandateController.InvalidateAutomaticDriveTimeEnum.NO_OVERLAP) {
            brokenRules.add(getString(R.string.editwouldoverwriteautogendrivetime));
        }

        // Determine if automatically recorded driving time has been extended AND DutyStatus changed because you can't do both.
        if (didEditChangeAutomaticDriveTimeAndDutyStatus()) {
            brokenRules.add(getString(R.string.editchangedautomaticdrivetimeanddutystatus));
        }

        return brokenRules;
    }

    /**
     * Distance is required and must be numeric for Manual Driving
     */
    private List<String> getManualDrivingBrokenRules(String manualDrivingDistanceText, Context context) {
        List<String> brokenRules = new ArrayList<String>();

        String manualDrivingDistance = TextUtils.isEmpty(manualDrivingDistanceText) ? "" : manualDrivingDistanceText.trim();
        if (manualDrivingDistance.length() == 0) {
            brokenRules.add(context.getString(R.string.requiredDistance));
        }
        else {
            try {
                Integer.parseInt(manualDrivingDistance);
            }
            catch (NumberFormatException ex) {
                brokenRules.add(context.getString(R.string.distancemustbenumeric));
            }
        }

        return brokenRules;
    }

    /**
     * Determine if automatically recorded driving time has been extended AND DutyStatus changed because you can't do both.
     */
    private Boolean didEditChangeAutomaticDriveTimeAndDutyStatus() {
        // is adding
        if (_primaryKey == -1) {
            return false;
        }

        if (_originalEventType == null || _originalEventCode == null || _originalEventRecordOrigin == null) {
            return false;
        }

        if (_originalEventType != Enums.EmployeeLogEldEventType.DutyStatusChange || _originalEventCode != EmployeeLogEldEventCode.DutyStatus_Driving || _originalEventRecordOrigin != Enums.EmployeeLogEldEventRecordOrigin.AutomaticallyRecorded) {
            return false;  // not editing automatically recorded driving time
        }

        int position = _cboDutyStatus.getSelectedItemPosition();
        EldEventDutyStatusItem item = (EldEventDutyStatusItem) _cboDutyStatus.getAdapter().getItem(position);
        if (item.getEventType() == Enums.EmployeeLogEldEventType.DutyStatusChange && item.getEventCode() == EmployeeLogEldEventCode.DutyStatus_Driving) {
            return false;  // DutyStatus hasn't changed from automatically recorded driving time
        }

        Date startTime = getStartTime();
        Date endTime = getEndTime();
        if (startTime == null || endTime == null) {
            return false;
        }

        if (startTime.compareTo(_originalStartTime) != 0 || endTime.compareTo(_originalEndTime) != 0) {
            return true;
        }

        return false;
    }

    /**
     * Show Distance field for Driving events only; editable for manual Driving and read-only for automatic Driving.
     */
    private void setDrivingDistanceVisibilityBasedOnDutyStatus(Enums.EmployeeLogEldEventType eventType, int eventCode) {

        if (isDrivingEvent(eventType, eventCode)) {
            // show Distance fields - but make read only if automatic Drive
            if (isEditingAutomaticDrivingEvent()) {
                _editDrivingDistance.setEnabled(false);
                _editDrivingDistance.setBackgroundColor(Color.TRANSPARENT);   // make it look like a label - obvious to user that it's read only
                _textDrivingDistanceRequiredAsterisk.setTextColor(Color.TRANSPARENT);
            } else {
                _editDrivingDistance.setEnabled(true);
                _editDrivingDistance.setBackgroundDrawable(_textViewBackground);
                _textDrivingDistanceRequiredAsterisk.setTextColor(getResources().getColor(R.color.red));
            }

            _rowDrivingDistance.setVisibility(View.VISIBLE);
        } else {
            if (getIsEditing()) {
                _editDrivingDistance.setText(null);
            }

            // not a automatic or manual Drive event so hide Driving Distance input field
            _rowDrivingDistance.setVisibility(View.GONE);
        }
    }

    private boolean isDrivingEvent(Enums.EmployeeLogEldEventType eventType, Integer eventCode) {
        if (eventType == null || eventCode == null) {
            return false;
        }

        return eventType == Enums.EmployeeLogEldEventType.DutyStatusChange && eventCode == EmployeeLogEldEventCode.DutyStatus_Driving;
    }

    private boolean isEditingAutomaticDrivingEvent() {
        return isDrivingEvent(_originalEventType, _originalEventCode) && _originalEventRecordOrigin != null && _originalEventRecordOrigin == Enums.EmployeeLogEldEventRecordOrigin.AutomaticallyRecorded;
    }


    /**
     * VIEW CHANGE LISTENERS REGION
     */

    /**
     * Hook up appropriate view Change Listeners so if any changes made will place you in dedicated Edit mode
     * and disable certain UI components so you can't leave the screen.
     */
    private void addChangeListeners() {
        _cboDutyStatus.setOnItemSelectedListener(_itemSelectedDutyStatus);
        _editLocation.addTextChangedListener(_txtWatcherLocation);
        _editUnitNumber.addTextChangedListener(_txtWatcherUnitNumber);
        _editTrailerNumber.addTextChangedListener(_txtWatcherTrailerNumber);
        _editShipmentInfo.addTextChangedListener(_txtWatcherShipmentInfo);
        _cboRuleset.setOnItemSelectedListener(_itemSelectedRuleset);
        _editDrivingDistance.addTextChangedListener(_txtWatcherDrivingDistance);
        _editDriversAnnotation.addTextChangedListener(_txtWatcherDriversAnnotation);
    }

    private void removeChangeListeners() {
        _cboDutyStatus.setOnItemSelectedListener(null);
        _editLocation.removeTextChangedListener(_txtWatcherLocation);
        _editUnitNumber.removeTextChangedListener(_txtWatcherUnitNumber);
        _editTrailerNumber.removeTextChangedListener(_txtWatcherTrailerNumber);
        _editShipmentInfo.removeTextChangedListener(_txtWatcherShipmentInfo);
        _editDrivingDistance.removeTextChangedListener(_txtWatcherDrivingDistance);
        _cboRuleset.setOnItemSelectedListener(null);
        _editDriversAnnotation.removeTextChangedListener(_txtWatcherDriversAnnotation);
    }

    AdapterView.OnItemSelectedListener _itemSelectedDutyStatus = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (!getIsEditing()) {
                int originalPosition = (int) _cboDutyStatus.getTag();
                if (originalPosition != position) {
                    setIsEditing();
                }
            }

            if (parent.getSelectedItem() instanceof EldEventDutyStatusItem) {
                EldEventDutyStatusItem selectedItem =
                        ((EldEventDutyStatusItem) parent.getSelectedItem());

                //Disable start and end time controls when selecting hyrail or non-regulated
                if (selectedItem.getSubStatus() != null || isEditingAutomaticDrivingEvent()) {
                    _btnStartTime.setEnabled(false);
                    _btnEndTime.setEnabled(false);
                } else {
                    _btnStartTime.setEnabled(true);
                    _btnEndTime.setEnabled(!_isEndTimeTodaysCurrentTime);
                }

                // control the visibility of the Driving Distance field
                setDrivingDistanceVisibilityBasedOnDutyStatus(selectedItem.getEventType(),
                        selectedItem.getEventCode());

                // When changing status from PC to Driving, clear the Location so the user must
                // enter it (since the PC event was recorded with a lower GPS precision than
                // what is required for a driving event).
                if (validatePcToDrivingStatus()) {
                    _editLocation.setText(null);
                }
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    TextWatcher _txtWatcherLocation = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (!getIsEditing()) {
                String originalValue = (String) _editLocation.getTag();
                if (!originalValue.equals(s.toString())) {
                    setIsEditing();
                }
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    TextWatcher _txtWatcherUnitNumber = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (!getIsEditing()) {
                String originalValue = (String) _editUnitNumber.getTag();
                if (!originalValue.equals(s.toString())) {
                    setIsEditing();
                }
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    TextWatcher _txtWatcherTrailerNumber = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (!getIsEditing()) {
                String originalValue = (String) _editTrailerNumber.getTag();
                if (!originalValue.equals(s.toString())) {
                    setIsEditing();
                }
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    TextWatcher _txtWatcherShipmentInfo = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (!getIsEditing()) {
                String originalValue = (String) _editShipmentInfo.getTag();
                if (!originalValue.equals(s.toString())) {
                    setIsEditing();
                }
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    AdapterView.OnItemSelectedListener _itemSelectedRuleset = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (!getIsEditing()) {
                int originalPosition = (int) _cboRuleset.getTag();
                if (originalPosition != position) {
                    setIsEditing();
                }
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    TextWatcher _txtWatcherDriversAnnotation = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (!getIsEditing()) {
                String originalValue = (String) _editDriversAnnotation.getTag();
                if (!originalValue.equals(s.toString())) {
                    setIsEditing();
                }
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    TextWatcher _txtWatcherDrivingDistance = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (!getIsEditing()) {
                String originalValue = (String) _editDrivingDistance.getTag();
                if (!originalValue.equals(s.toString())) {
                    setIsEditing();
                }
            }
        }

        @Override
        public void afterTextChanged(Editable s) { }
    };

    public boolean validatePcToDrivingStatus() {
        // Original Sub Status is PC
        // Event type = 1 & Event Code = 3 is Driving new status
        int position = _cboDutyStatus.getSelectedItemPosition();
        if ((_originalSubStatus != null && _originalSubStatus == Enums.SpecialDrivingCategory.PersonalConveyance) &&
                ((EldEventDutyStatusItem) _cboDutyStatus.getAdapter().getItem(position)).getEventType() == Enums.EmployeeLogEldEventType.DutyStatusChange &&
                ((EldEventDutyStatusItem) _cboDutyStatus.getAdapter().getItem(position)).getEventCode() == 3) {
            return true;
        } else {
            return false;
        }
    }
    //end method validatePcToDrivingStatus
}
