package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.jjkeller.kmb.interfaces.IViewTripInfo.ViewTripInfoFragActions;
import com.jjkeller.kmb.interfaces.IViewTripInfo.ViewTripInfoFragControllerMethods;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.abstracts.APIControllerBase;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.controller.utility.EmployeeLogUtilities;
import com.jjkeller.kmbapi.enums.CanadaDeferralTypeEnum;
import com.jjkeller.kmbapi.enums.RuleSetTypeEnum;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbui.R;

import java.util.Date;

public class ViewTripInfoFrag extends BaseFragment {
	ViewTripInfoFragControllerMethods controlListener;
	ViewTripInfoFragActions actionsListener;
	
	private TextView _txtTrailer;
    private TextView _txtTrailerPlate;
    private TextView _lblTrailerPlate;
	private TextView _txtShipmentInfo;
	private TextView _txtVehiclePlate;
    private TextView _lblVehiclePlate;
    private TextView _txtTractorNumber;
    private View _dividerTrailerPlate;
    private View _dividerVehiclePlate;
    private View _dividerTractorNumber;
    private TextView _lblTractorNumber;
	private TextView _lblDeferral;
	private TextView _txtDeferral;
	private Button _btnedittripinfo;	
	private CheckBox _chkReturnToWorkLocation;
	private CheckBox _chkIsHaulingExplosives;
	private CheckBox _chkIsWeeklyResetUsed;
	private CheckBox _chkIsOperatesSpecificVehiclesForOilField;
	private ArrayAdapter<CharSequence> cdDeferralAdapter;
	private CanadaDeferralTypeEnum canadaDeferralTypeEnum;
	private Bundle returnVals;	
	private Context _ctx;
    private View _dividerState;
	private TextView _lblState;
	private TextView _txtState;
	private View _dividerLicenseNumber;
	private TextView _lblDriverLicenseNumber;
	private TextView _txtDriverLicenseNumber;

	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
        // Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.f_viewtripinfo, container, false);
		findControls(v);
		return v;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		this.loadControls(savedInstanceState);
	}
	
	protected void findControls(View v)
	{
		_ctx = getActivity();
		_txtTrailer = (TextView)v.findViewById(R.id.txtTrailertripinfo);
		_txtTrailerPlate = (TextView)v.findViewById(R.id.txtTrailerPlatetripinfo);
		_txtShipmentInfo = (TextView)v.findViewById(R.id.txtShipmentInfotripinfo);
		_txtVehiclePlate = (TextView)v.findViewById(R.id.txtVehiclePlatetripinfo);
        _txtTractorNumber = (TextView)v.findViewById(R.id.txtTractorNumbertripinfo);
        _lblTractorNumber = (TextView)v.findViewById(R.id.lblTractorNumbertripinfo);
        _lblTrailerPlate= (TextView)v.findViewById(R.id.lblTrailerPlatetripinfo);
        _lblVehiclePlate= (TextView)v.findViewById(R.id.lblVehiclePlatetripinfo);
        _dividerTrailerPlate=(View) v.findViewById(R.id.dividerTrailerPlate);
		_dividerVehiclePlate=(View) v.findViewById(R.id.dividerVehiclePlate);
        _dividerTractorNumber=(View) v.findViewById(R.id.dividerTractorNumber);
		_lblDeferral = (TextView)v.findViewById(R.id.lblDeferraltripinfo);
		_btnedittripinfo = (Button)v.findViewById(R.id.btnedittripinfo);
		_txtDeferral = (TextView)v.findViewById(R.id.txtDeferraltripinfo);
		_chkReturnToWorkLocation = (CheckBox)v.findViewById(R.id.chkReturnToWorkLocation);
		_chkIsHaulingExplosives = (CheckBox)v.findViewById(R.id.chkIsHaulingExplosives);
		_chkIsWeeklyResetUsed = (CheckBox)v.findViewById(R.id.chkIsWeeklyResetUsed);
		_chkIsOperatesSpecificVehiclesForOilField = (CheckBox)v.findViewById(R.id.chkIsOperatesSpecificVehicleForOilField);


		_btnedittripinfo.setOnClickListener(
				new OnClickListener() {					
					public void onClick(View v) {
						actionsListener.handleEditButtonClick(_ctx);
					}
				});
	}

	public void disableEditButton(final String displayMessage) {
		_btnedittripinfo.setBackgroundResource(R.drawable.button_blue_disabled);
		_btnedittripinfo.setEnabled(true);
		_btnedittripinfo.setOnClickListener(null);
		_btnedittripinfo.setOnClickListener(
				new View.OnClickListener() {
					public void onClick(View v) {
						ShowToastMessage(displayMessage);
					}
				});

	}

	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
        	actionsListener = (ViewTripInfoFragActions) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement ViewTripInfoFragActions");
        }
        
        try{
        	controlListener = (ViewTripInfoFragControllerMethods) activity;
        } catch (ClassCastException e) {
        	throw new ClassCastException(activity.toString() + " must implement ViewTripInfoPeriodsFragControllerMethods");
        }
    }
	
	protected void loadControls(Bundle savedInstanceState)
	{
        if (!GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
            _txtTractorNumber.setVisibility(View.GONE);
            _lblTractorNumber.setVisibility(View.GONE);
            _dividerTractorNumber.setVisibility(View.GONE);

        }

		RuleSetTypeEnum currentUserRuleset = ((APIControllerBase)controlListener.getMyController()).getCurrentUser().getRulesetTypeEnum();

        cdDeferralAdapter = ArrayAdapter.createFromResource(getActivity(), CanadaDeferralTypeEnum.ARRAYID, R.layout.kmb_spinner_item);
        cdDeferralAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        returnVals = controlListener.getMyController().GetTripInfo();
		canadaDeferralTypeEnum = new CanadaDeferralTypeEnum(returnVals.getInt(this.getString(R.string.canadadeferraltype)));
        
		//if in roadside inspection mode, hide the edit button
		if(GlobalState.getInstance().getRoadsideInspectionMode())
			_btnedittripinfo.setVisibility(View.GONE);
		else
			_btnedittripinfo.setVisibility(View.VISIBLE);

        // If current ruleset is Canadian, show the Off Duty Deferral dropdown.
		if(currentUserRuleset.isCanadianRuleset() )
		{
			_chkReturnToWorkLocation.setVisibility(View.GONE);
			_chkIsHaulingExplosives.setVisibility(View.GONE);
			_lblDeferral.setVisibility(View.VISIBLE);
			_txtDeferral.setVisibility(View.VISIBLE);
			
			_txtDeferral.setText(canadaDeferralTypeEnum.getString(getActivity()));			
		}
		else if (currentUserRuleset.getValue() == RuleSetTypeEnum.TEXASOILFIELD)
		{
			_chkReturnToWorkLocation.setVisibility(View.GONE);
			_chkIsWeeklyResetUsed.setVisibility(View.GONE);
			_chkIsHaulingExplosives.setVisibility(View.GONE);

	        if (returnVals.containsKey(this.getActivity().getString(R.string.state_isoperatesspecificvehiclesforoilfield)))
	        	_chkIsOperatesSpecificVehiclesForOilField.setVisibility(View.VISIBLE);
	        else
	        	_chkIsOperatesSpecificVehiclesForOilField.setVisibility(View.GONE);
			
			_lblDeferral.setVisibility(View.GONE);
			_txtDeferral.setVisibility(View.GONE);			
		}
		else
		{
			// Check for an extra passed from the Login screen
			// If present, hide the Return to Work checkbox
	        if( getActivity().getIntent().hasExtra(this.getResources().getString(R.string.extra_tripinfomsg)) ){
	        	_chkReturnToWorkLocation.setVisibility(View.GONE);
	        }
	        else{
	        	// the Trip Info is shown *after* the login workflow
	        	_chkReturnToWorkLocation.setVisibility(View.VISIBLE);
	        	
				// only when the trip info contains the Weekly Reset Used field, show the checkbox
				if(returnVals.containsKey(this.getActivity().getString(R.string.isweeklyresetused)))
					_chkIsWeeklyResetUsed.setVisibility(View.VISIBLE);		
				else
					_chkIsWeeklyResetUsed.setVisibility(View.GONE);		        	
	        }
	        
	        if(((APIControllerBase)controlListener.getMyController()).getCurrentUser().getIsHaulingExplosivesAllowed())
	        	_chkIsHaulingExplosives.setVisibility(View.VISIBLE);
	        
	        if (returnVals.containsKey(this.getActivity().getString(R.string.state_isoperatesspecificvehiclesforoilfield)))
	        	_chkIsOperatesSpecificVehiclesForOilField.setVisibility(View.VISIBLE);
	        else
	        	_chkIsOperatesSpecificVehiclesForOilField.setVisibility(View.GONE);
	        
			_txtDeferral.setVisibility(View.GONE);
			_lblDeferral.setVisibility(View.GONE);
		}
		
		// 10/3/11 JHM - Only load from state if one of the expected values is contained in bundle.
		if(savedInstanceState != null &&
			(savedInstanceState.containsKey(getResources().getString(R.string.state_trailer)) ||
			 savedInstanceState.containsKey(getResources().getString(R.string.state_trailerplate)) ||
                    savedInstanceState.containsKey(getResources().getString(R.string.state_tractornumber)) ||
			 savedInstanceState.containsKey(getResources().getString(R.string.state_shipment)) ||
			 savedInstanceState.containsKey(getResources().getString(R.string.state_vehicleplate)) ||
			 savedInstanceState.containsKey(getResources().getString(R.string.state_returntoworklocation)) ||
			 savedInstanceState.containsKey(getResources().getString(R.string.state_isoperatesspecificvehiclesforoilfield)) ) )
		{
			_txtTrailer.setText(savedInstanceState.getCharSequence(getResources().getString(R.string.state_trailer)));
			_txtTrailerPlate.setText(savedInstanceState.getCharSequence(getResources().getString(R.string.state_trailerplate)));
            _txtTractorNumber.setText(savedInstanceState.getCharSequence(getResources().getString(R.string.state_tractornumber)));
			_txtShipmentInfo.setText(savedInstanceState.getCharSequence(getResources().getString(R.string.state_shipment)));
			_txtVehiclePlate.setText(savedInstanceState.getCharSequence(getResources().getString(R.string.state_vehicleplate)));
			_chkReturnToWorkLocation.setChecked(savedInstanceState.getBoolean(getResources().getString(R.string.state_returntoworklocation)));
			_chkIsHaulingExplosives.setChecked(savedInstanceState.getBoolean(getResources().getString(R.string.state_ishaulingexplosives)));
			if(savedInstanceState.containsKey(this.getActivity().getString(R.string.isweeklyresetused))){
				_chkIsWeeklyResetUsed.setChecked(savedInstanceState.getBoolean(getActivity().getString(R.string.isweeklyresetused)));
			}
			_chkIsOperatesSpecificVehiclesForOilField.setChecked(savedInstanceState.getBoolean(getResources().getString(R.string.state_isoperatesspecificvehiclesforoilfield)));
		}
		else{
			_txtTrailer.setText(returnVals.getString(getActivity().getString(R.string.trailernumbers)));
            _txtTrailerPlate.setText(returnVals.getString(getActivity().getString(R.string.trailerplate)));
            _txtTractorNumber.setText(returnVals.getString(getActivity().getString(R.string.tractornumber)));
			_txtShipmentInfo.setText(returnVals.getString(getActivity().getString(R.string.shipmentinfo)));
            _txtVehiclePlate.setText(returnVals.getString(getActivity().getString(R.string.vehicleplate)));
			_chkReturnToWorkLocation.setChecked(returnVals.getBoolean(getActivity().getString(R.string.returntoworklocation)));
			_chkIsHaulingExplosives.setChecked(returnVals.getBoolean(getActivity().getString(R.string.ishaulingexplosives)));
			if(returnVals.containsKey(this.getActivity().getString(R.string.isweeklyresetused))){
				_chkIsWeeklyResetUsed.setChecked(returnVals.getBoolean(getActivity().getString(R.string.isweeklyresetused)));
			}
			_chkIsOperatesSpecificVehiclesForOilField.setChecked(returnVals.getBoolean(getActivity().getString(R.string.state_isoperatesspecificvehiclesforoilfield)));
		}
		
		controlListener.updateExemptLogStatus();
        Date currentHomeTerminalTime = DateUtility.CurrentHomeTerminalTime(GlobalState.getInstance().getCurrentUser() );
        Date todaysLogDate = EmployeeLogUtilities.CalculateLogStartTime(this.getActivity(), currentHomeTerminalTime,GlobalState.getInstance().getCurrentUser().getHomeTerminalTimeZone());
        EmployeeLog employeeLog= controlListener.getMyController().GetEmployeeLog(todaysLogDate);
        if(!controlListener.getMyController().logHasCanadianRulesets(employeeLog) ){
            _lblTrailerPlate.setVisibility(View.GONE);
            _lblVehiclePlate.setVisibility(View.GONE);
            _txtTrailerPlate.setVisibility(View.GONE);
            _txtVehiclePlate.setVisibility(View.GONE);
            _dividerVehiclePlate.setVisibility(View.GONE);
            _dividerTrailerPlate.setVisibility(View.GONE);
        }
	}

}
