package com.jjkeller.kmb.fragments;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jjkeller.kmb.EldEventEditReviewDetails;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.employeelogeldevents.Enums;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;
import com.jjkeller.kmbui.R;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class EditLogRequestFrag extends BaseFragment {
    public static final String EXTRA_HUDSUCCESSMESSAGE = "hudSuccessMessage";
    public static final String EXTRA_HUDERRORMESSAGE = "hudErrorMessage";

    private TextView _lblEditLogRequestTitle;
    private TextView _lblEditLogRequestSubtitle;
    private GridView _gridEditRequestLogEvents;
    private Date _logDate;

    private LinearLayout _linearHUD;
    private TextView _txtError;
    private TextView _txtSuccess;
    private String _hudSuccessMessage;
    private String _hudErrorMessage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.f_editlogrequest, container, false);
        findControls(v);

        if (savedInstanceState != null) {
            // screen rotation
            _hudSuccessMessage = savedInstanceState.getString(EXTRA_HUDSUCCESSMESSAGE);
            _hudErrorMessage = savedInstanceState.getString(EXTRA_HUDERRORMESSAGE);
        }
        else {
            _hudSuccessMessage = "";
            _hudErrorMessage = "";
        }

        return v;
    }

    /**
     * Called during screen rotation to persist values so the screen can be re-created.
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(EXTRA_HUDSUCCESSMESSAGE, _hudSuccessMessage);
        outState.putString(EXTRA_HUDERRORMESSAGE, _hudErrorMessage);

        super.onSaveInstanceState(outState);
    }

    protected void findControls(View v) {
        _lblEditLogRequestTitle = (TextView)v.findViewById(R.id.lblEditLogRequestTitle);
        _lblEditLogRequestSubtitle = (TextView)v.findViewById(R.id.lblEditLogRequestSubtitle);
        _gridEditRequestLogEvents = (GridView)v.findViewById(R.id.gridEditRequestLogEvents);
        _linearHUD = (LinearLayout)v.findViewById(R.id.linearHUD);
        _txtError = (TextView)v.findViewById(R.id.txtError);
        _txtSuccess = (TextView)v.findViewById(R.id.txtSuccess);
    }

    public void loadControls(Date logDate, List<EmployeeLogEldEvent> events) {
        _logDate = logDate;
        _lblEditLogRequestTitle.setText(getString(R.string.editlogrequesttitleformat, DateUtility.getHomeTerminalDateFormat().format(logDate)));

        String subtitle = getString(R.string.editlogrequestsubtitle);
        _lblEditLogRequestSubtitle.setText(Html.fromHtml(subtitle));

        if(events != null && !events.isEmpty()) {
            _gridEditRequestLogEvents.setAdapter(new EditRequestAdapter(getActivity(), R.layout.grdeditlogrequestevent, events));
        }

        if (_hudSuccessMessage.length() > 0)
            showHUDMessageSuccess(_hudSuccessMessage);
        else if (_hudErrorMessage.length() > 0)
            showHUDMessageError(_hudErrorMessage);
        else
            hideHUD();
    }



    /**
     * Shows messages in the Heads-Up Display.
     */
    public void showHUDMessageSuccess(String message) {
        if (message.length() > 0) {
            _txtError.setVisibility(View.GONE);
            _txtSuccess.setText(Html.fromHtml(message));
            _txtSuccess.setVisibility(View.VISIBLE);
            _linearHUD.setVisibility(View.VISIBLE);

            _hudSuccessMessage = message;
        }
    }

    public void showHUDMessageError(String message) {
        if (message.length() > 0) {
            _txtSuccess.setVisibility(View.GONE);
            _txtError.setText(Html.fromHtml(message));
            _txtError.setVisibility(View.VISIBLE);
            _linearHUD.setVisibility(View.VISIBLE);

            _hudErrorMessage = message;
        }
    }

    public void hideHUD() {
        _linearHUD.setVisibility(View.GONE);
        _txtError.setVisibility(View.GONE);
        _txtSuccess.setVisibility(View.GONE);
    }



    private class EditRequestAdapter extends ArrayAdapter<EmployeeLogEldEvent> {

        public final int RED_COLOR = (getResources().getColor(R.color.red));
        public final int BLACK_COLOR = (getResources().getColor(R.color.black));

        private List<EmployeeLogEldEvent> items;
        public EditRequestAdapter (Context context, int textViewResourceId, List<EmployeeLogEldEvent> items ) {
            super(context, textViewResourceId, items);

            ListIterator<EmployeeLogEldEvent> eventIterator = items.listIterator();
            while(eventIterator.hasNext()){
                if (eventIterator.next().getEventRecordStatus() == EmployeeLogEldEvent.RECORD_STATUS_INACTIVE_CHANGED){
                    eventIterator.remove();
                }
            }


            this.items = items;
        }

        public class ViewHolder {
            public TextView lblEventTime;
            public TextView txtEventTime;
            public TextView lblEvent;
            public TextView txtEvent;
            public TextView lblTractorNumber;
            public TextView txtTractorNumber;
            public TextView lblLocation;
            public TextView txtLocation;
            public Button btnViewDetails;

            public Boolean hasLabels = false;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder view;

            if(convertView == null) {
                view = new ViewHolder();

                LayoutInflater inflator = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflator.inflate(R.layout.grdeditlogrequestevent, null);

                View vw = convertView.findViewById(R.id.lblEventTime);
                view.hasLabels = vw != null;

                if (view.hasLabels){
                    view.lblEventTime = (TextView) vw;
                    view.lblEvent = (TextView) convertView.findViewById(R.id.lblEvent);
                    view.lblTractorNumber = (TextView) convertView.findViewById(R.id.lblTractorNumber);
                    view.lblLocation = (TextView) convertView.findViewById(R.id.lblLocation);
                }

                view.txtEventTime = (TextView) convertView.findViewById(R.id.tvEventTime);
                view.txtEvent = (TextView) convertView.findViewById(R.id.tvEvent);
                view.txtTractorNumber = (TextView) convertView.findViewById(R.id.tvTractorNumber);
                view.txtLocation = (TextView) convertView.findViewById(R.id.tvLocation);
                view.btnViewDetails = (Button) convertView.findViewById(R.id.btnViewDetails);

                convertView.setTag(view);
            }
            else
            {
                view = (ViewHolder) convertView.getTag();
            }

            EmployeeLogEldEvent event = items.get(position);

            int textColor = BLACK_COLOR;
            if (event.getEventRecordStatus() == Enums.EmployeeLogEldEventRecordStatus.InactiveChangeRequested.getValue()) {
                textColor = RED_COLOR;
            }

            if (view.hasLabels) {
                view.lblEventTime.setTextColor(textColor);
                view.lblEvent.setTextColor(textColor);
                view.lblTractorNumber.setTextColor(textColor);
                view.lblLocation.setTextColor(textColor);
            }

            view.txtEventTime.setTextColor(textColor);
            view.txtEventTime.setText(DateUtility.getHomeTerminalTime12HourFormatWithSeconds().format(event.getEventDateTime()));

            view.txtEvent.setTextColor(textColor);
            view.txtEvent.setText(event.getCompositeEventCodeType(event.getEventType(),event.getEventCode()));

            view.txtTractorNumber.setTextColor(textColor);
            view.txtTractorNumber.setText(event.getTractorNumber());

            view.txtLocation.setTextColor(textColor);
            view.txtLocation.setText(event.getLocation().getName());

            final int logPosition = position;
            view.btnViewDetails.setOnClickListener(null);
            view.btnViewDetails.setOnClickListener(
                    new View.OnClickListener() {
                        public void onClick(View v) {
                            Bundle bundle = new Bundle();
                            bundle.putInt(EldEventEditReviewDetails.EXTRA_EMPLOYEELOGKEY, (int)items.get(logPosition).getLogKey());
                            bundle.putString(EldEventEditReviewDetails.EXTRA_EMPLOYEELOGDATE, DateUtility.getHomeTerminalDateFormat().format(_logDate));
                            bundle.putInt(EldEventEditReviewDetails.EXTRA_CURRENTEVENTINDEX, logPosition);
                            startActivity(EldEventEditReviewDetails.class, bundle);
                        }
                    });

            return convertView;
        }
    }
}
