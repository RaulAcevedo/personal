package com.jjkeller.kmb;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.view.MenuItem;
import com.jjkeller.kmb.fragments.DateSelectorFrag;
import com.jjkeller.kmb.fragments.LeftNavFrag;
import com.jjkeller.kmb.interfaces.IDateSelectorFrag;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmb.util.DisplayUtil;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.interfaces.IAPIController;
import com.jjkeller.kmbapi.controller.share.MandateObjectFactory;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.controller.utility.EmployeeLogUtilities;
import com.jjkeller.kmbapi.employeelogeldevents.Enums;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;
import com.jjkeller.kmbui.R;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class RptEldEvents extends BaseActivity implements LeftNavFrag.OnNavItemSelectedListener, LeftNavFrag.ActivityMenuItemsListener, IDateSelectorFrag.DateSelectorControllerMethods {

    private Date _selectedDate;
    private List<Pair<EmployeeLogEldEvent, Enums.SpecialDrivingCategory>> eventWithSpecialDrivingCatList;
    private int _editLogEventPosition;
    private String _editLogEventTitle = "";
    private ListView listView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            _editLogEventPosition = savedInstanceState.getInt(DateSelectorFrag.BUNDLE_SELECTOR_POSITION, 0);
            _editLogEventTitle = savedInstanceState.getString(DateSelectorFrag.BUNDLE_SELECTOR_TITLE, "");
        }
        Date currentDate = this.getController().getCurrentClockHomeTerminalTime();
        if (_selectedDate == null) {
            _selectedDate = currentDate;
        }
        setContentView(R.layout.rpteldevents);
        listView = (ListView) findViewById(R.id.listItems);
        loadDateNavFragment();
        this.loadControls(savedInstanceState);
    }

    protected IAPIController getMyController() {
        return (IAPIController) this.getController();
    }

    @Override
    protected void InitController() {
        this.setController(MandateObjectFactory.getInstance(this, GlobalState.getInstance().getFeatureService()).getCurrentEventController());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(DateSelectorFrag.BUNDLE_SELECTOR_POSITION, _editLogEventPosition);
        outState.putString(DateSelectorFrag.BUNDLE_SELECTOR_TITLE, _editLogEventTitle);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void loadData() {
        if (_selectedDate != null) {
            EmployeeLog empLog = getMyController().GetEmployeeLog(_selectedDate);
            if (empLog != null) {
                IAPIController empLogCtrlr = MandateObjectFactory.getInstance(this, GlobalState.getInstance().getFeatureService()).getCurrentEventController();
                EmployeeLogEldEvent[] eventList = empLogCtrlr.fetchEldEventsByEventTypes(
                        (int) empLog.getPrimaryKey(),
                        Arrays.asList(
                                Enums.EmployeeLogEldEventType.DutyStatusChange.getValue(),
                                Enums.EmployeeLogEldEventType.IntermediateLog.getValue(),
                                Enums.EmployeeLogEldEventType.ChangeInDriversIndication.getValue(),
                                Enums.EmployeeLogEldEventType.Certification.getValue(),
                                Enums.EmployeeLogEldEventType.LoginLogout.getValue(),
                                Enums.EmployeeLogEldEventType.EnginePowerUpPowerDown.getValue(),
                                Enums.EmployeeLogEldEventType.Malfunction_DataDiagnosticDetection.getValue()
                        ),
                        Arrays.asList(Enums.EmployeeLogEldEventRecordStatus.Active.getValue()));
                eventWithSpecialDrivingCatList = EmployeeLogUtilities.loadEventsIncludingSpecialDrivingCategories(this, empLog, eventList);
            }
            if (eventWithSpecialDrivingCatList != null && !eventWithSpecialDrivingCatList.isEmpty()) {
                listView.setAdapter(new EldEventsAdapter(this, eventWithSpecialDrivingCatList));
            }
        }
    }

    private class EldEventsAdapter extends ArrayAdapter<Pair<EmployeeLogEldEvent, Enums.SpecialDrivingCategory>> {

        private List<Pair<EmployeeLogEldEvent, Enums.SpecialDrivingCategory>> eventsSpecialDrivingItems;
        private boolean isExemptFromELDUse;
        private GlobalState globalState;

        public EldEventsAdapter(Context context, List<Pair<EmployeeLogEldEvent, Enums.SpecialDrivingCategory>> eventsSpecialDriving) {
            super(context, R.layout.grdrpteldevents, eventsSpecialDriving);
            this.eventsSpecialDrivingItems = eventsSpecialDriving;
            globalState = GlobalState.getInstance();
            isExemptFromELDUse = getIsExemptFromELDUse();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final Pair<EmployeeLogEldEvent, Enums.SpecialDrivingCategory> eventPair = eventsSpecialDrivingItems.get(position);
            ViewHolder holder;

            if (convertView== null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.grdrpteldevents, parent, false);
                holder = new ViewHolder();
                holder.tvTime = (TextView) convertView.findViewById(R.id.tvLogTime);
                holder.tvType = (TextView) convertView.findViewById(R.id.tvLogType);
                holder.btnEdit = (Button) convertView.findViewById(R.id.btnLogDetails);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            if (eventPair != null) {
                holder.tvTime.setText(DateUtility.createHomeTerminalTimeString(eventPair.first.getEventDateTime(), globalState.getFeatureService().getIsEldMandateEnabled()));

                // if DRIVING, display (manual) if origin != automatic (1)
                String statusText;
                if (eventPair.first.getEventType() == Enums.EmployeeLogEldEventType.DutyStatusChange) {
                    statusText = DisplayUtil.getStatusDisplayText(eventPair, globalState.getIsCurrentUserTheDesignatedDriver(), isExemptFromELDUse);
                }else {
                    statusText =  eventPair.first.getCompositeEventCodeType(eventPair.first.getEventType(), eventPair.first.getEventCode());
                }
                if (eventPair.first.isManualDrivingEvent()) {
                    statusText = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? statusText + " (M)" : statusText + " (manual)";
                }
                holder.tvType.setText(statusText);

                holder.btnEdit.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Bundle bundle = new Bundle();
                        bundle.putInt(EldEventEdit.EXTRA_PRIMARYKEY, (int) eventPair.first.getPrimaryKey());
                        startActivity(RptEldEventDetail.class, bundle);
                    }
                });
            }
            return convertView;
        }
    }
    private static class ViewHolder {
        TextView tvType;
        TextView tvTime;
        Button btnEdit;
    }
    @Override
    public String getActivityMenuItemList() {
        return getString(R.string.rptdailyhours_actionitems);
    }

    private void handleMenuItemSelected(int itemPosition) {
        if (itemPosition == 0) {
            this.finish();
            this.startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
    }

    @Override
    public void onNavItemSelected(int itemPosition) {
        handleMenuItemSelected(itemPosition);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //See if home button was pressed
        this.GoHome(item, this.getController());
        handleMenuItemSelected(item.getItemId());
        super.onOptionsItemSelected(item);
        return true;
    }

    public void loadDateNavFragment() {
        // Keeping the title on Global state
        FrameLayout layout = (FrameLayout) findViewById(R.id.datenav_fragment);
        if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
            _editLogEventTitle = getString(R.string.mnu_reportsmenu_eldevents);

            Bundle bundle = new Bundle();
            bundle.putInt(DateSelectorFrag.BUNDLE_SELECTOR_POSITION, _editLogEventPosition);
            bundle.putString(DateSelectorFrag.BUNDLE_SELECTOR_TITLE, _editLogEventTitle);

            DateSelectorFrag _dateSelectorFrag = new DateSelectorFrag();
            _dateSelectorFrag.setArguments(bundle);
            loadFragment(R.id.datenav_fragment, _dateSelectorFrag);
        } else {
            if (layout != null) {
                layout.setVisibility(View.GONE);
            }
        }

    }

    public void handleEditLog() {
// this is a method for the date selection fragment, used on the edit log screen but not used here.
    }

    public void handleDateChange(Date selectedDate, int position) {
        _selectedDate = selectedDate;
        _editLogEventPosition = position;
        loadData();
    }

    @Override
    public IAPIController getEmployeeLogController() {
        return this.getMyController();
    }
}
