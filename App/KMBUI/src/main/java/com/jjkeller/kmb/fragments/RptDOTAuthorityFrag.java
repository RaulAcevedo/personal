package com.jjkeller.kmb.fragments;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jjkeller.kmb.adapters.DOTAuthorityAdapter;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.MotionPictureController;
import com.jjkeller.kmbapi.controller.dataaccess.EmployeeLogEldEventFacade;
import com.jjkeller.kmbapi.controller.dataaccess.EmployeeLogFacade;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.controller.utility.EmployeeLogUtilities;
import com.jjkeller.kmbapi.employeelogeldevents.Enums;
import com.jjkeller.kmbapi.enums.TimeZoneEnum;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;
import com.jjkeller.kmbapi.proxydata.MotionPictureAuthority;
import com.jjkeller.kmbui.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by T000694 on 5/30/2017.
 */

public class RptDOTAuthorityFrag extends BaseFragment implements LimitedRangeDatePickerFrag.OnDatePickListener{

    private View vLayout;

    private DOTAuthorityListFrag _dotAuthorityListFragment;
    private LimitedRangeDatePickerFrag _datePickerFragment;
    private ArrayList<DOTAuthorityAdapter.DOTItem> dotItems;
    private MotionPictureController motionPictureController;
    private EmployeeLogEldEventFacade employeeLogEldEventFacade;
    private EmployeeLogFacade employeeLogFacade;

    public static RptDOTAuthorityFrag newInstance(){
        RptDOTAuthorityFrag fragment = new RptDOTAuthorityFrag();

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        vLayout = inflater.inflate(R.layout.f_rptdotauthority, container, false);

        initComponents();
        loadFragments(savedInstanceState);

        if (savedInstanceState == null) {
            dotItems = new ArrayList<>();
            onDatePick(new Date());
        }else{
            dotItems = savedInstanceState.getParcelableArrayList(getResources().getString(R.string.state_dot_authority_list));
        }

        return vLayout;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (dotItems != null) {
            outState.putParcelableArrayList(getResources().getString(R.string.state_dot_authority_list), dotItems);
        }
    }

    private void loadFragments(Bundle savedInstanceState){
        if (savedInstanceState == null) {
            Calendar calendar = Calendar.getInstance();
            Date maxDate, minDate;

            maxDate = calendar.getTime();
            calendar.add(Calendar.DAY_OF_MONTH, -7);
            minDate = calendar.getTime();

            _dotAuthorityListFragment = DOTAuthorityListFrag.newInstance();
            _datePickerFragment = LimitedRangeDatePickerFrag.newInstance(minDate, maxDate);
            _datePickerFragment.setOnDatePickListener(this);

            getFragmentManager().beginTransaction()
                    .add(R.id.fl_date_picker_container, _datePickerFragment)
                    .add(R.id.fl_dot_authority_list_container, _dotAuthorityListFragment)
                    .commit();
        }
    }

    @Override
    protected void setFragments() {
        super.setFragments();
        Fragment datePickerFragment = getFragmentManager().findFragmentById(R.id.fl_date_picker_container);
        Fragment dotAuthorityListFragment = getFragmentManager().findFragmentById(R.id.fl_dot_authority_list_container);

        if (datePickerFragment != null) {
            _datePickerFragment = (LimitedRangeDatePickerFrag) datePickerFragment;
            _datePickerFragment.setOnDatePickListener(this);
        }

        if (dotAuthorityListFragment != null) {
            _dotAuthorityListFragment = (DOTAuthorityListFrag) dotAuthorityListFragment;
            _dotAuthorityListFragment.setDataSource(dotItems);
        }
    }

    private void initComponents(){
        employeeLogEldEventFacade = new EmployeeLogEldEventFacade(getActivity(), GlobalState.getInstance().getCurrentUser());
        employeeLogFacade = new EmployeeLogFacade(getActivity(), GlobalState.getInstance().getCurrentUser());
        motionPictureController = new MotionPictureController(getActivity());
    }

    @Override
    public void onDatePick(Date date) {
        new FetchDOTAuthoritiesTask(date).execute();
    }

    private List<EmployeeLogEldEvent> getEmployeeLogEldList(long logPrimaryKey) {
        List<EmployeeLogEldEvent> result = new ArrayList<>();

        EmployeeLogEldEvent[] employeeLogEldEventsArray = employeeLogEldEventFacade.GetByEventTypes((int) logPrimaryKey, Arrays.asList(
                Enums.EmployeeLogEldEventType.DutyStatusChange.getValue(),
                Enums.EmployeeLogEldEventType.ChangeInDriversIndication.getValue()),
                Collections.singletonList(Enums.EmployeeLogEldEventRecordStatus.Active.getValue()));

        result = Arrays.asList(employeeLogEldEventsArray);

        return result;
    }

    private class FetchDOTAuthoritiesTask extends AsyncTask<Void, Void, Void> {

        Date date;
        ProgressDialog progressDialog;

        FetchDOTAuthoritiesTask(Date date) {
            this.date = date;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.msgretreiving));

            if (getActivity() instanceof BaseActivity) {
                ((BaseActivity)getActivity()).LockScreenRotation();
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            EmployeeLog employeeLog = employeeLogFacade.GetLogByDate(date);
            dotItems.clear();

            if (employeeLog != null) {
                List<EmployeeLogEldEvent> employeeLogEldEvents = getEmployeeLogEldList(employeeLog.getPrimaryKey());

                if (employeeLogEldEvents != null && employeeLogEldEvents.size() > 0) {

                    for (int i = 0; i <= employeeLogEldEvents.size() - 1; i++) {
                        EmployeeLogEldEvent event = employeeLogEldEvents.get(i);
                        MotionPictureAuthority motionPictureAuthority = motionPictureController.GetAuthorityByAuthorityId(event.getMotionPictureAuthorityId());
                        EmployeeLogEldEvent nextEldEvent = EmployeeLogUtilities.getNextActiveEventAfterDate(employeeLogEldEvents, event.getEventDateTime());
                        Date nextEventDate = null;

                        //If the motion picture authority is set to not specified, create an empty authority to mark beginning and ends of actual authorities.
                        if (motionPictureAuthority == null || motionPictureAuthority.getPrimaryKey() == 1) {
                            motionPictureAuthority = new MotionPictureAuthority();
                            motionPictureAuthority.setName("");
                            motionPictureAuthority.setDOTNumber("");
                        }

                        if (nextEldEvent != null) {
                            nextEventDate = nextEldEvent.getEventDateTime();
                        }else if (!DateUtility.IsToday(event.getEventDateTime(), GlobalState.getInstance().getCurrentUser())) {

                            // determine end of day for the company
                            TimeZoneEnum timeZoneEnum = GlobalState.getInstance().getCurrentUser().getHomeTerminalTimeZone();
                            String dailyLogStart = GlobalState.getInstance().getCompanyConfigSettings(GlobalState.getInstance().getEobrService()).getDailyLogStartTime();
                            nextEventDate = EmployeeLogUtilities.CalculateLogEndTime(dailyLogStart, event.getEventDateTime(), timeZoneEnum);
                        }
                        else{
                            nextEventDate = DateUtility.getCurrentDateTimeUTC();
                        }


                        if (i > 0 && !dotItems.isEmpty()) {
                            DOTAuthorityAdapter.DOTItem previousItem = dotItems.get(dotItems.size()-1);

                            if (previousItem.getName().equals(motionPictureAuthority.getName())){
                                previousItem.setEndTime(nextEventDate == null ? "" : (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled() ? DateUtility.getHomeTerminalTime12HourFormatWithSeconds().format(nextEventDate) : DateUtility.getHomeTerminalTime12HourFormat().format(nextEventDate)));
                                continue;
                            }
                        }
                        
                        dotItems.add(new DOTAuthorityAdapter.DOTItem(
                                event.getEventDateTime() == null ? "-" : (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled() ? DateUtility.getHomeTerminalTime12HourFormatWithSeconds().format(event.getEventDateTime()) : DateUtility.getHomeTerminalTime12HourFormat().format(event.getEventDateTime())),
                                GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled() ? DateUtility.getHomeTerminalTime12HourFormatWithSeconds().format(nextEventDate) : DateUtility.getHomeTerminalTime12HourFormat().format(nextEventDate),
                                motionPictureAuthority.getDOTNumber(),
                                motionPictureAuthority.getName(),
                                getFormattedAddress(motionPictureAuthority)
                        ));
                    }
                }
            }

            Iterator<DOTAuthorityAdapter.DOTItem> dotItemIterator = dotItems.iterator();
            while(dotItemIterator.hasNext()){
                DOTAuthorityAdapter.DOTItem currentDotItem = dotItemIterator.next();
                if (currentDotItem.getName() == null || currentDotItem.getName().isEmpty()) {
                    dotItemIterator.remove();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (isAdded() && getActivity() != null) {
                if (_dotAuthorityListFragment != null) {
                    _dotAuthorityListFragment.setDataSource(dotItems);
                }

                if (progressDialog != null) {
                    progressDialog.dismiss();
                }

                if (getActivity() instanceof BaseActivity) {
                    ((BaseActivity)getActivity()).UnlockScreenRotation();
                }
            }
        }

        String getFormattedAddress(MotionPictureAuthority motionPictureAuthority) {
            StringBuilder builder = new StringBuilder();
            boolean isCityAppended = false;

            if (motionPictureAuthority.getAddressLine1() != null && motionPictureAuthority.getAddressLine1().length() > 0) {
                builder.append(motionPictureAuthority.getAddressLine1());
                builder.append(' ');
            }
            if (motionPictureAuthority.getCity() != null && motionPictureAuthority.getCity().length() > 0) {
                builder.append(motionPictureAuthority.getCity());
                builder.append(' ');
                isCityAppended = true;
            }
            if (motionPictureAuthority.getState() != null && motionPictureAuthority.getState().length() > 0) {
                builder.append(motionPictureAuthority.getState());
                builder.append(isCityAppended ? ", " : ' ');
            }
            if (motionPictureAuthority.getZipCode() != null && motionPictureAuthority.getZipCode().length() > 0) {
                builder.append(motionPictureAuthority.getZipCode());
            }

            return builder.toString();
        }
    }
}
