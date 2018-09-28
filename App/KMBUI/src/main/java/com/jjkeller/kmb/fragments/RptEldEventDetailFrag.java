package com.jjkeller.kmb.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jjkeller.kmb.Login;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.LogEntryController;
import com.jjkeller.kmbapi.controller.interfaces.IAPIController;
import com.jjkeller.kmbapi.controller.share.MandateObjectFactory;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.employeelogeldevents.Enums;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;
import com.jjkeller.kmbapi.proxydata.EmployeeRule;
import com.jjkeller.kmbui.R;

import java.util.Date;

public class RptEldEventDetailFrag extends BaseFragment{

    TextView _tvEventDate;
    TextView _tvEventTime;
    TextView _tvSequenceId;
    TextView _tvEventType;
    TextView _tvEventCode;
    TextView _tvRecordOriginator;
    TextView _tvVehicleIdentifier;
    TextView _tvMotorCarrier;
    TextView _tvShippingDoc;
    TextView _tvEventRecordStatus;
    TextView _tvEventRecordOrigin;
    TextView _tvDateOfCertifiedRecord;
    TextView _tvTimeZoneOffset;
    TextView _tvAccVehicleMiles;
    TextView _tvTotalVehicleMiles;
    TextView _tvElapsedEngineHours;
    TextView _tvTotalEngineHours;
    TextView _tvLatitude;
    TextView _tvLongitude;
    TextView _tvDistanceSinceLastValidGpsCoord;
    TextView _tvDriversLocationDesc;
    TextView _tvMalfDataDiagStatus;
    TextView _tvMalfDiagCodes;
    TextView _tvEventCommentAnnotation;
    TextView _tvEventDataCheck;

    Integer _eventId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.f_rpteldeventdetail, container, false);
        findControls(v);     
        return v;
    }

    private void findControls(View v) {
        _tvEventDate = (TextView) v.findViewById(R.id.tvEventDate);
        _tvEventTime = (TextView) v.findViewById(R.id.tvEventTime);
        _tvSequenceId = (TextView) v.findViewById(R.id.tvSequenceId);
        _tvEventType = (TextView) v.findViewById(R.id.tvEventType);
        _tvEventCode = (TextView) v.findViewById(R.id.tvEventCode);
        _tvRecordOriginator = (TextView) v.findViewById(R.id.tvRecordOriginator);
        _tvVehicleIdentifier = (TextView) v.findViewById(R.id.tvVehicleIdentifier);
        _tvMotorCarrier = (TextView) v.findViewById(R.id.tvMotorCarrier );
        _tvShippingDoc = (TextView) v.findViewById(R.id.tvShippingDoc);
        _tvEventRecordStatus = (TextView) v.findViewById(R.id.tvEventRecordStatus);
        _tvEventRecordOrigin = (TextView) v.findViewById(R.id.tvEventRecordOrigin);
        _tvDateOfCertifiedRecord = (TextView) v.findViewById(R.id.tvDateOfCertifiedRecord );
        _tvTimeZoneOffset = (TextView) v.findViewById(R.id.tvTimeZoneOffset);
        _tvAccVehicleMiles = (TextView) v.findViewById(R.id.tvAccVehicleMiles);
        _tvTotalVehicleMiles = (TextView) v.findViewById(R.id.tvTotalVehicleMiles);
        _tvElapsedEngineHours = (TextView) v.findViewById(R.id.tvElapsedEngineHours);
        _tvTotalEngineHours = (TextView) v.findViewById(R.id.tvTotalEngineHours);
        _tvLatitude = (TextView) v.findViewById(R.id.tvLatitude);
        _tvLongitude = (TextView) v.findViewById(R.id.tvLongitude);
        _tvDistanceSinceLastValidGpsCoord = (TextView) v.findViewById(R.id.tvDistanceSinceLastValidGpsCoord);
        _tvDriversLocationDesc = (TextView) v.findViewById(R.id.tvDriversLocationDesc);
        _tvMalfDataDiagStatus = (TextView) v.findViewById(R.id.tvMalfDataDiagStatus);
        _tvMalfDiagCodes = (TextView) v.findViewById(R.id.tvMalfDiagCodes);
        _tvEventCommentAnnotation = (TextView) v.findViewById(R.id.tvEventCommentAnnotation);
        _tvEventDataCheck = (TextView) v.findViewById(R.id.tvEventDataCheck);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.loadControls(savedInstanceState);
    }

    private void loadControls(Bundle savedInstanceState) {

      //  if (_eventId != null) {
            IAPIController controller = MandateObjectFactory.getInstance(getActivity(), GlobalState.getInstance().getFeatureService()).getCurrentEventController();
            EmployeeLogEldEvent eldEvent = controller.fetchEldEventByKey(_eventId);

            if (eldEvent!=null){
                Context ctx = GlobalState.getInstance().getBaseContext();
                GlobalState gs = GlobalState.getInstance();

                Date eventDateTime = eldEvent.getEventDateTime();
                String DatePart = DateUtility.getHomeTerminalShortDateFormat().format(eventDateTime);
                String TimePart = DateUtility.getHomeTerminalDateTimeFormat().format(eventDateTime);

                _tvEventDate.setText(DatePart);
                _tvEventTime.setText( TimePart );
                _tvSequenceId.setText( String.valueOf(eldEvent.getEventSequenceIDNumber()));
                _tvEventType.setText( eldEvent.getEventType().toString());
                _tvEventCode.setText(String.valueOf(eldEvent.getEventCode()));
                _tvRecordOriginator.setText(String.valueOf(eldEvent.getEventRecordOrigin()));
                _tvVehicleIdentifier.setText(eldEvent.getTractorNumber());

                String motorCarrier = gs.getCompanyConfigSettings(ctx).getDmoCompanyName()
                        + " " + gs.getCompanyConfigSettings(ctx).getCarrierDOTNumber();
                _tvMotorCarrier.setText(motorCarrier);
                _tvShippingDoc.setText( eldEvent.getShipmentInfo());
                _tvEventRecordStatus.setText(String.valueOf(eldEvent.getEventRecordStatus()));

                _tvEventRecordOrigin.setText(String.valueOf(eldEvent.getEventRecordOrigin()));

                if (eldEvent.getEventType() == Enums.EmployeeLogEldEventType.Certification) {
                    _tvDateOfCertifiedRecord.setText(eldEvent.getEventDateTime().toString());
                }
                else
                {
                    _tvDateOfCertifiedRecord.setText("Uncertified");
                }
                _tvTimeZoneOffset.setText(gs.getCurrentUser().getHomeTerminalTimeZone().toDMOEnum());

                _tvAccVehicleMiles.setText(String.valueOf(eldEvent.getAccumulatedVehicleMiles()));
                _tvTotalVehicleMiles.setText(String.valueOf(eldEvent.getOdometer()));

                //
                _tvElapsedEngineHours.setText(String.valueOf(eldEvent.getEngineHours()));
                _tvTotalEngineHours.setText(String.valueOf(eldEvent.getEngineHours()));

                _tvLatitude.setText(String.valueOf(eldEvent.getLatitude()));
                _tvLongitude.setText(String.valueOf(eldEvent.getLongitude()));
                _tvDistanceSinceLastValidGpsCoord.setText(String.valueOf(eldEvent.getDistanceSinceLastCoordinates()));
                _tvDriversLocationDesc.setText(String.valueOf(eldEvent.getDriversLocationDescription()));
                _tvMalfDataDiagStatus.setText(String.valueOf(eldEvent.getDriverDataDiagnosticEventIndicatorStatus()));
                _tvMalfDiagCodes.setText(String.valueOf(eldEvent.getDiagnosticCode()));
                _tvEventCommentAnnotation.setText(String.valueOf(eldEvent.getEventComment()));
                _tvEventDataCheck.setText(String.valueOf(eldEvent.getEventDataCheck()));
            }



        }
   // }

    public void setEventId(int id) {
        _eventId = id;
    }
}
