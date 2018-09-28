package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.jjkeller.kmb.EldEventEdit;
import com.jjkeller.kmb.interfaces.IEditLog;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.employeelogeldevents.Enums;
import com.jjkeller.kmbapi.enums.DutyStatusEnum;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;
import com.jjkeller.kmbui.R;

import java.util.Date;
import java.util.List;

import static com.jjkeller.kmb.util.DisplayUtil.getStatusDisplayText;


public class EditLogFrag extends BaseFragment {
    private GridView _grid;
    private Button _btnAddEvent;

    protected IEditLog.EditLogFragControllerMethods controlListener;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.f_editlog, container, false);
        findControls(v);
        return v;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            controlListener = (IEditLog.EditLogFragControllerMethods) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement IEditLog.EditLogFragControllerMethods");
        }
    }

    protected void findControls(View v) {
        _grid = (GridView) v.findViewById(R.id.grdViewEditLog);
        _btnAddEvent = (Button) v.findViewById(R.id.btnAddEvent);
    }

    public void setDataSource(List<Pair<EmployeeLogEldEvent, Enums.SpecialDrivingCategory>> eventWithProvisions, int position) {
        _grid.setAdapter(new EditLogAdapter(getActivity(), R.layout.grdvieweditlog, eventWithProvisions, this.getIsExemptFromELDUse()));
        _grid.setSelection(position);
    }

    public void enableAddEventButton(final int parentEmployeeLogKey, final Date selectedDate) {
        _btnAddEvent.setBackgroundResource(R.drawable.button_blue);
        _btnAddEvent.setEnabled(true);
        _btnAddEvent.setOnClickListener(null);
        _btnAddEvent.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        startEldEventEditActivity(parentEmployeeLogKey, selectedDate, -1);
                    }
                });
    }

    public void startEldEventEditActivity(int employeeLogKey, Date eventDateTime, int currentEventIndex) {
        Bundle bundle = new Bundle();
        bundle.putInt(EldEventEdit.EXTRA_EMPLOYEELOGKEY, employeeLogKey);
        bundle.putInt(EldEventEdit.EXTRA_CURRENTEVENTINDEX, currentEventIndex);    // -1 indicates new record
        bundle.putString(EldEventEdit.EXTRA_EVENTDATETIME, DateUtility.getHomeTerminalReferenceTimestampFormat().format(eventDateTime));
        startActivity(EldEventEdit.class, bundle);
    }

    public void disableAddEventButton(final String displayMessage) {
        _btnAddEvent.setBackgroundResource(R.drawable.button_blue_disabled);
        _btnAddEvent.setEnabled(true);
        _btnAddEvent.setOnClickListener(null);
        _btnAddEvent.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        ShowToastMessage(displayMessage);
                    }
                });

    }


    private class EditLogAdapter extends ArrayAdapter<Pair<EmployeeLogEldEvent, Enums.SpecialDrivingCategory>> {

        private List<Pair<EmployeeLogEldEvent, Enums.SpecialDrivingCategory>> items;
        private boolean isExemptFromELDUse;

        public EditLogAdapter(Context context, int textViewResourceId, List<Pair<EmployeeLogEldEvent, Enums.SpecialDrivingCategory>> items, boolean isExemptFromELDUse) {
            super(context, textViewResourceId, items);
            this.items = items;
            this.isExemptFromELDUse = isExemptFromELDUse;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater li = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = li.inflate(R.layout.grdvieweditlog, null);
            }
            final EmployeeLogEldEvent logEvent = items.get(position).first;

            if (logEvent != null) {

                TextView tvTime = (TextView) v.findViewById(R.id.tvEditLogTime);
                tvTime.setText(DateUtility.getHomeTerminalTime12HourFormatWithSeconds().format(logEvent.getEventDateTime()));

                TextView tvStatus = (TextView) v.findViewById(R.id.tvEditLogStatus);

                boolean isTheDriver = ((BaseActivity) getActivity()).IsTheDriver(GlobalState.getInstance().getCurrentUser());

                String statusText = getStatusDisplayText(items.get(position), isTheDriver, isExemptFromELDUse);
                // if DRIVING, display (manual) if origin != automatic (1)
                if (logEvent.isManualDrivingEvent()) {
                    statusText = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? statusText + " (M)" : statusText + " (manual)";
                }

                tvStatus.setText(statusText);

                Button btnEdit = (Button) v.findViewById(R.id.btnEditLog);

                if (isExemptFromELDUse) {
                    btnEdit.setBackgroundResource(R.drawable.button_blue_disabled);
                    btnEdit.setOnClickListener(
                            new View.OnClickListener() {
                                public void onClick(View v) {
                                    ShowToastMessage(getString(R.string.cannot_edit_exempt_log_message));
                                }
                            });
                } else if (items.get(position).second == Enums.SpecialDrivingCategory.PersonalConveyance
                        && GlobalState.getInstance().getIsInPersonalConveyanceDrivingSegment()
                        && position == items.size() - 1) {
                    btnEdit.setBackgroundResource(R.drawable.button_blue_disabled);
                    btnEdit.setOnClickListener(
                            new View.OnClickListener() {
                                public void onClick(View v) {
                                    ShowToastMessage(getString(R.string.cannot_edit_active_pc_event_message));
                                }
                            });
                } else {
                    btnEdit.setOnClickListener(
                            new View.OnClickListener() {
                                public void onClick(View v) {
                                    startEldEventEditActivity(logEvent.getLogKey(), logEvent.getEventDateTime(), position);
                                }
                            });
                }
            }
            return v;
        }
    }
}
