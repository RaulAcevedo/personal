package com.jjkeller.kmb.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.jjkeller.kmb.adapters.EldEventDutyStatusAdapter;
import com.jjkeller.kmb.adapters.EldEventDutyStatusItem;
import com.jjkeller.kmb.adapters.TeamDriverAdapter;
import com.jjkeller.kmb.adapters.TeamDriverItem;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbapi.employeelogeldevents.EmployeeLogEldEventCode;
import com.jjkeller.kmbapi.employeelogeldevents.Enums;
import com.jjkeller.kmbui.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for Reassigning Driving event to another team driver on a shared device.
 */
public class EldEventTeamDriverReassignFrag extends BaseFragment {

    private static final String EXTRA_HUDMESSAGE = "hudMessage";
    private static final String EXTRA_HUDVISIBLE = "hudIsVisible";
    private static final String EXTRA_CBODRIVER = "cboDriver";
    private static final String EXTRA_CBODUTYSTATUS = "cboDutyStatus";
    private static final String EXTRA_EDITDRIVERSANNOTATION = "editDriversAnnotation";

    private LinearLayout _linearHUD;
    private TextView _txtError;
    private TextView _txtSuccess;
    private String _hudMessage = "";
    private boolean _hudIsVisible = false;

    EldEventDutyStatusAdapter _dutystatusAdapter;

    private Spinner _cboDriver;
    private Spinner _cboDutyStatus;
    private EditText _editDriversAnnotation;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.f_eldeventteamdriverreassign, container, false);

        if (savedInstanceState != null) {
            // screen rotation
            _hudIsVisible = savedInstanceState.getBoolean(EXTRA_HUDVISIBLE);
            _hudMessage = savedInstanceState.getString(EXTRA_HUDMESSAGE);
        }

        findControls(v, savedInstanceState);

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        loadControls(savedInstanceState);
    }

    /**
     * Called during screen rotation to persist values so the screen can be re-created.
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putString(EXTRA_HUDMESSAGE, _hudMessage);
        outState.putBoolean(EXTRA_HUDVISIBLE, _hudIsVisible);
        outState.putInt(EXTRA_CBODRIVER, _cboDriver.getSelectedItemPosition());
        outState.putInt(EXTRA_CBODUTYSTATUS, _cboDutyStatus.getSelectedItemPosition());
        outState.putString(EXTRA_EDITDRIVERSANNOTATION, _editDriversAnnotation.getText().toString());

        super.onSaveInstanceState(outState);
    }

    /**
     * Store a handle to each control (child view).
     */
    protected void findControls(View v, Bundle savedInstanceState) {
        _linearHUD = (LinearLayout) v.findViewById(R.id.linearHUD);
        _txtError = (TextView) v.findViewById(R.id.txtError);
        _txtSuccess = (TextView) v.findViewById(R.id.txtSuccess);

        // Populate Duty Status combo with special adapter
        // Status: Off Duty, Sleeper, On Duty
        String[] standardStrings = getResources().getStringArray(R.array.EldEventDutyStatus_array_team_driver_reassign_event);
        List<EldEventDutyStatusItem> standardChoices = new ArrayList<EldEventDutyStatusItem>();
        standardChoices.add(new EldEventDutyStatusItem(getString(R.string.notspecifiedprompt), null, Enums.EmployeeLogEldEventType.DutyStatusChange, EmployeeLogEldEventCode.DutyStatus_NULL, null));
        standardChoices.add(new EldEventDutyStatusItem(standardStrings[0], null, Enums.EmployeeLogEldEventType.DutyStatusChange, EmployeeLogEldEventCode.DutyStatus_OffDuty, null));
        standardChoices.add(new EldEventDutyStatusItem(standardStrings[1], null, Enums.EmployeeLogEldEventType.DutyStatusChange, EmployeeLogEldEventCode.DutyStatus_Sleeper, null));
        // No Driving
        standardChoices.add(new EldEventDutyStatusItem(standardStrings[3], null, Enums.EmployeeLogEldEventType.DutyStatusChange, EmployeeLogEldEventCode.DutyStatus_OnDuty, null));

        _cboDriver = (Spinner) v.findViewById(R.id.cboTeamDriver);

        _dutystatusAdapter = new EldEventDutyStatusAdapter(getActivity(), standardChoices);
        _cboDutyStatus = (Spinner) v.findViewById(R.id.cboDutyStatus);
        _cboDutyStatus.setAdapter(_dutystatusAdapter);

        _editDriversAnnotation = (EditText) v.findViewById(R.id.editDriversAnnotation);

        if (savedInstanceState != null) {
            // Fragment is being rebuilt after screen orientation change
            int driverSelectionIndex = savedInstanceState.getInt(EXTRA_CBODRIVER);
            if (driverSelectionIndex > -1) {
                _cboDriver.setSelection(driverSelectionIndex);
            }

            int dutySelectionSelectionIndex = savedInstanceState.getInt(EXTRA_CBODUTYSTATUS);
            if (dutySelectionSelectionIndex > -1) {
                _cboDutyStatus.setSelection(dutySelectionSelectionIndex);
            }

            _editDriversAnnotation.setText(savedInstanceState.getString(EXTRA_EDITDRIVERSANNOTATION));
        }
    }

    /**
     * Populate lookup controls with choices.
     */
    protected void loadControls(Bundle savedInstanceState) {

        if (_hudIsVisible)
            showHUDMessage(_hudMessage);
        else
            hideHUD();

    }

    public void setTeamDrivers(List<TeamDriverItem> teamDriverList) {
        _cboDriver.setAdapter(new TeamDriverAdapter(getActivity(), teamDriverList));
    }

    /**
     * Valid the object and return list of broken rules (if any)
     */
    public List<String> getBrokenRules() {
        List<String> brokenRules = new ArrayList<String>();

        // Driver is required
        int position = _cboDriver.getSelectedItemPosition();
        TeamDriverItem driver = (TeamDriverItem) _cboDriver.getAdapter().getItem(position);
        if (driver.getEmployeeCode().isEmpty())
            brokenRules.add(getString(R.string.requireddriver));

        // Duty Status is required
        position = _cboDutyStatus.getSelectedItemPosition();
        EldEventDutyStatusItem item = (EldEventDutyStatusItem) _cboDutyStatus.getAdapter().getItem(position);
        if (item.getEventCode() == EmployeeLogEldEventCode.DutyStatus_NULL)
            brokenRules.add(getString(R.string.requireddutystatus));

        // Drivers Annotation must be 4 characters or longer
        if (_editDriversAnnotation.getText().toString().trim().length() < 4)
            brokenRules.add(getString(R.string.requireddriversannotation));

        return brokenRules;
    }

    /**
     * Shows messages in the Heads-Up Display.
     */
    public void showHUDMessage(String message) {
        if (message.length() == 0) {
            _txtError.setVisibility(View.GONE);

            _txtSuccess.setText(getString(R.string.eldeventeditsuccess));
            _txtSuccess.setVisibility(View.VISIBLE);

            _cboDriver.setEnabled(false);
            _cboDutyStatus.setEnabled(false);
            _editDriversAnnotation.setEnabled(false);

            clearFocusAndDismissSIP();
        }
        else {
            _txtSuccess.setVisibility(View.GONE);
            _txtError.setText(Html.fromHtml(message));
            _txtError.setVisibility(View.VISIBLE);
        }

        _linearHUD.setVisibility(View.VISIBLE);

        _hudMessage = message;
        _hudIsVisible = true;
    }

    public void hideHUD() {
        _linearHUD.setVisibility(View.GONE);
        _txtError.setVisibility(View.GONE);
        _txtSuccess.setVisibility(View.GONE);

        _hudMessage = "";
        _hudIsVisible = false;
    }

    /**
     * Property Helper methods
     */

    public TeamDriverItem getTeamDriverItem() {
        if (_cboDriver != null) {
            TeamDriverAdapter adapter = (TeamDriverAdapter) _cboDriver.getAdapter();
            return (TeamDriverItem) adapter.getItem( _cboDriver.getSelectedItemPosition() );
        }

        return null;
    }

    public int getEventCode() {
        if (_cboDutyStatus != null) {
            EldEventDutyStatusAdapter adapter = (EldEventDutyStatusAdapter) _cboDutyStatus.getAdapter();
            EldEventDutyStatusItem item = (EldEventDutyStatusItem) adapter.getItem( _cboDutyStatus.getSelectedItemPosition() );
            return item.getEventCode();
        }

        return EmployeeLogEldEventCode.DutyStatus_NULL;
    }

    public String getAnnotation() {
        if (_editDriversAnnotation != null)
            return _editDriversAnnotation.getText().toString();

        return "";
    }

    /**
     * Clear focus from active control and dismiss the keyboard if showing.
     */
    private void clearFocusAndDismissSIP() {
        //Clear focus from all controls
        View current = getActivity().getCurrentFocus();
        if (current != null) {
            current.clearFocus();

            // hide keyboard
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(current.getWindowToken(), 0);
        }
    }
}