package com.jjkeller.kmb.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbapi.common.TimeKeeper;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.configuration.LoginCredentials;
import com.jjkeller.kmbapi.controller.LogCheckerComplianceDatesController;
import com.jjkeller.kmbapi.controller.abstracts.APIControllerBase;
import com.jjkeller.kmbapi.controller.interfaces.IAPIController;
import com.jjkeller.kmbapi.controller.share.MandateObjectFactory;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.controller.utility.EmployeeLogUtilities;
import com.jjkeller.kmbapi.enums.DriverTypeEnum;
import com.jjkeller.kmbapi.enums.ExemptLogTypeEnum;
import com.jjkeller.kmbapi.enums.LogCheckerComplianceDatesTypeEnum;
import com.jjkeller.kmbapi.enums.RuleSetTypeEnum;
import com.jjkeller.kmbapi.enums.TimeZoneEnum;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbui.R;

import java.util.HashMap;
import java.util.HashSet;

public class RptLogDetailLogDetailTabFrag extends BaseFragment
{

	private TextView _tvLogDate;
	private TextView _tvTractor;
	private TextView _tvTrailer;
	private TextView _tvTrailerPlate;
    private TextView  _lblTrailerPlate;
	private TextView _tvShipment;
	private TextView _tvVehiclePlate;
    private TextView  _lblVehiclePlate;
	private TextView _tvDriverType;
	private TextView _tvTimezone;
	private TextView _tvRuleset;
	private TextView _tvDistance;
	private TextView _tvReturned;
	private TextView _tvDeferral;
	private TextView _tvWeeklyResetStartDate;
	private TextView _tvWeeklyResetStartTime;
	private TextView _tvWeeklyResetUsed;
	private TextView _lblWeeklyResetStartDate;
	private TextView _lblWeeklyResetTime;
	private TextView _lblWeeklyResetUsed;
	private TextView _lblOperateSpecificVehicleOilField;
	private TextView _tvOperateSpecificVehicleOilField;
	private TextView _lblReturned;
	private TextView _lblDeferral;
	private TextView _lblHaulingExplosives;
	private TextView _tvHaulingExplosives;
	private TextView _exemptlbl;
    private TextView _tvOdo;
	private TextView _lblDriverLicenseNumber;
	private TextView _tvDriverLicenseNumber;
	private TextView _lblState;
	private TextView _tvState;
	private TextView _lblDriverType;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.tablogdetail, container, false);
		findControls(v);
		loadControls();
		return v;
	}

	private void findControls(View v)
	{
		_tvLogDate = (TextView)v.findViewById(R.id.tvLogDate);
		_tvTractor = (TextView)v.findViewById(R.id.tvTractor);
		_tvTrailer = (TextView)v.findViewById(R.id.tvTrailer);
		_tvTrailerPlate = (TextView)v.findViewById(R.id.tvTrailerPlate);
        _lblTrailerPlate = (TextView)v.findViewById(R.id.lblTrailerPlate);
		_tvShipment = (TextView)v.findViewById(R.id.tvShipment);
		_tvVehiclePlate = (TextView)v.findViewById(R.id.tvVehiclePlate);
        _lblVehiclePlate = (TextView)v.findViewById(R.id.lblVehiclePlate);
		_tvDriverType = (TextView)v.findViewById(R.id.tvDriverType);
		_tvTimezone = (TextView)v.findViewById(R.id.tvTimeZone);
		_tvRuleset = (TextView)v.findViewById(R.id.tvRuleset);
		_tvDistance = (TextView)v.findViewById(R.id.tvDistance);
        _tvOdo = (TextView)v.findViewById(R.id.tvOdo);

		_tvReturned = (TextView)v.findViewById(R.id.tvReturned);
		_tvDeferral = (TextView)v.findViewById(R.id.tvDeferral);
		_tvWeeklyResetStartDate = (TextView)v.findViewById(R.id.tvWeeklyResetDate);
		_tvWeeklyResetStartTime = (TextView)v.findViewById(R.id.tvWeeklyResetTime);
		_tvWeeklyResetUsed = (TextView)v.findViewById(R.id.tvWeeklyResetUsed);
		_lblWeeklyResetStartDate = (TextView)v.findViewById(R.id.lblWeeklyResetStartDate);
		_lblWeeklyResetTime = (TextView)v.findViewById(R.id.lblWeeklyResetTime);
		_lblWeeklyResetUsed = (TextView)v.findViewById(R.id.lblWeeklyResetUsed);
		_lblOperateSpecificVehicleOilField = (TextView)v.findViewById(R.id.lblOperateSpecificVehicleOilField);
		_tvOperateSpecificVehicleOilField = (TextView)v.findViewById(R.id.tvOperateSpecificVehicleOilField);
		_lblReturned = (TextView)v.findViewById(R.id.lblReturned);
		_lblDeferral = (TextView)v.findViewById(R.id.lblDeferral);
		_lblHaulingExplosives = (TextView)v.findViewById(R.id.lblHaulingExplosives);
		_tvHaulingExplosives = (TextView)v.findViewById(R.id.tvHaulingExplosives);
		_exemptlbl = (TextView)v.findViewById(R.id.txtExemptLog);
		_lblDriverLicenseNumber = (TextView) v.findViewById(R.id.lblDriverLicenseNumber);
		_tvDriverLicenseNumber = (TextView) v.findViewById(R.id.tvDriverLicenseNumber);
		_lblState = (TextView) v.findViewById(R.id.lblState);
		_tvState = (TextView)v.findViewById(R.id.tvState);
		_lblDriverType = (TextView)v.findViewById(R.id.lblDriverType);

	}
	private void setExemptLabel(boolean isVisible)
	{
		
		if (_exemptlbl != null)
		{
			if (!isVisible)
				_exemptlbl.setVisibility(View.GONE);
			else
				_exemptlbl.setVisibility(View.VISIBLE);
		}

	}
	private void loadControls()
	{
		IAPIController controller = MandateObjectFactory.getInstance(getActivity(),GlobalState.getInstance().getFeatureService()).getCurrentEventController();
		LoginCredentials logincredentials = GlobalState.getInstance().getCurrentUser().getCredentials();
		
		EmployeeLog empLog = controller.getSelectedLogForReport();
		setExemptLabel(empLog.getExemptLogType().getValue() != ExemptLogTypeEnum.NULL);
		_tvLogDate.setText(DateUtility.getHomeTerminalDateFormat().format(empLog.getLogDate()));

		//Get all trip info objects
		HashMap<EmployeeLog.TripInfoPropertyKey,HashSet<String>> tripInfoMap = controller.GetTripInfoForLog(empLog);
		for (EmployeeLog.TripInfoPropertyKey tripInfoPropertyKey : EmployeeLog.TripInfoPropertyKey.values()) {
			//TextView representing control matched to enum
			TextView tripInfoControl;
			//Switch based on key in question
			switch (tripInfoPropertyKey) {
				case TractorNumber:
					tripInfoControl = _tvTractor;
					break;
				case TrailerNumber:
					tripInfoControl = _tvTrailer;
					break;
				case TrailerPlate:
					tripInfoControl = _tvTrailerPlate;
					break;
				case ShipmentInfo:
					tripInfoControl = _tvShipment;
					break;
				case VehiclePlate:
					tripInfoControl = _tvVehiclePlate;
					break;
				default:
					tripInfoControl = null;
					break;
			}
			if (tripInfoControl != null) {
				tripInfoControl.setText(EmployeeLogUtilities.TripInfoToString(tripInfoMap, tripInfoPropertyKey));
			}
		}

		switch (empLog.getDriverType().getValue())
		{
			case DriverTypeEnum.PASSENGERCARRYING:
			case DriverTypeEnum.PROPERTYCARRYING:
				_tvDriverType.setText(empLog.getDriverType().getString(getActivity()));
				break;
			default:
				_tvDriverType.setText("");
				break;
		}

		switch (empLog.getTimezone().getValue())
		{
			case TimeZoneEnum.ALASKASTANDARDTIME:
			case TimeZoneEnum.ATLANTICSTANDARDTIME:
			case TimeZoneEnum.CENTRALSTANDARDTIME:
			case TimeZoneEnum.EASTERNSTANDARDTIME:
			case TimeZoneEnum.MOUNTAINSTANDARDTIME:
			case TimeZoneEnum.PACIFICSTANDARDTIME:
				_tvTimezone.setText(empLog.getTimezone().getString(getActivity()));
				break;
			default:
				_tvTimezone.setText("");
				break;
		}

		_tvRuleset.setText(empLog.getRuleset().getString(getActivity()));
		
		// hide/display deferral field based on whether log is for Canadian rule set
		if (!empLog.getRuleset().isCanadianRuleset())
		{
			_lblDeferral.setVisibility(View.GONE);
			_tvDeferral.setVisibility(View.GONE);			
		}
		else
		{
			_lblDeferral.setVisibility(View.VISIBLE);
			_tvDeferral.setVisibility(View.VISIBLE);
			_tvDeferral.setText(empLog.getCanadaDeferralType().getString(getActivity()));			
		}
		
		LogCheckerComplianceDatesController complianceDatesController = new LogCheckerComplianceDatesController(this.getActivity());
		int ruleset = GlobalState.getInstance().getCurrentUser().getRulesetTypeEnum().getValue();
		
		boolean isActive_34HourReset = complianceDatesController.IsLogCheckerComplianceDateActive(LogCheckerComplianceDatesTypeEnum.DEC2014_UNENFORCE34HRRESET, TimeKeeper.getInstance().now(), false);
		
		//disable the weekly reset if under certain rulesets
		isActive_34HourReset &= ruleset != RuleSetTypeEnum.FLORIDA_7DAY && ruleset != RuleSetTypeEnum.FLORIDA_8DAY
				&& ruleset != RuleSetTypeEnum.WISCONSIN_7DAY && ruleset != RuleSetTypeEnum.WISCONSIN_8DAY
				&& ruleset != RuleSetTypeEnum.TEXAS && ruleset != RuleSetTypeEnum.USOILFIELD && ruleset != RuleSetTypeEnum.TEXASOILFIELD;
		
		if(!isActive_34HourReset){
			// when then rule is not active, hide the weekly reset fields
			_tvWeeklyResetStartDate.setVisibility(View.GONE);
			_tvWeeklyResetStartTime.setVisibility(View.GONE);
			_tvWeeklyResetUsed.setVisibility(View.GONE);
			_lblWeeklyResetStartDate.setVisibility(View.GONE);
			_lblWeeklyResetTime.setVisibility(View.GONE);
			_lblWeeklyResetUsed.setVisibility(View.GONE);		
		}
		
		if(empLog.getWeeklyResetStartTimestamp() != null){
			_tvWeeklyResetStartDate.setText(DateUtility.getHomeTerminalDateFormat().format(empLog.getWeeklyResetStartTimestamp()));
			_tvWeeklyResetStartTime.setText(DateUtility.getHomeTerminalTime12HourFormat().format(empLog.getWeeklyResetStartTimestamp()));
			if(empLog.getIsWeeklyResetUsed())
				_tvWeeklyResetUsed.setText("Yes");	
			else
				_tvWeeklyResetUsed.setText("No");
		}
		
		// display distance - if user is setup to display distances as KM, convert
		float dist = empLog.getTotalLogDistance() + empLog.getMobileDerivedDistance();
		if (((APIControllerBase)controller).getCurrentUser().getDistanceUnits().equals(this.getString(R.string.kilometers)))
			dist = dist * GlobalState.MilesToKilometers;
		_tvDistance.setText(String.format("%.1f", dist));
        if (empLog.getRuleset().isCanadianRuleset()) {
            String odomData = controller.getOdometerData(empLog);
            _tvOdo.setText(odomData);
        }
        else{
            _tvOdo.setVisibility(View.GONE);
        }



		// hide/display returned to work location field based on rule sets that allow shorthaul exception
		if (empLog.getRuleset().isUSFederalRuleset() || empLog.getRuleset().getValue() == RuleSetTypeEnum.USOILFIELD)
		{
			_lblReturned.setVisibility(View.VISIBLE);
			_tvReturned.setVisibility(View.VISIBLE);
			if (empLog.getHasReturnedToLocation())
				_tvReturned.setText("Yes");
			else
				_tvReturned.setText("No");
		}
		else
		{
			_lblReturned.setVisibility(View.GONE);
			_tvReturned.setVisibility(View.GONE);
		}
		
		// hide/display operates specific vehicle for oil field value based on if current log
		// uses oil field ruleset
		if (empLog.getRuleset().isAnyOilFieldRuleset())
		{
			_lblOperateSpecificVehicleOilField.setVisibility(View.VISIBLE);
			_tvOperateSpecificVehicleOilField.setVisibility(View.VISIBLE);
			
			if (empLog.getIsOperatesSpecificVehiclesForOilfield())
				_tvOperateSpecificVehicleOilField.setText("Yes");
			else
				_tvOperateSpecificVehicleOilField.setText("No");
		}
		else
		{
			_lblOperateSpecificVehicleOilField.setVisibility(View.GONE);
			_tvOperateSpecificVehicleOilField.setVisibility(View.GONE);
		}
		
		// hide/display hauling explosives based on employee rule and current rule sets that require 8 hour/30 minute provision
		if (GlobalState.getInstance().getCurrentUser().getIsHaulingExplosivesAllowed())
		{
			if (empLog.getRuleset().isUSFederalRuleset() || empLog.getRuleset().getValue() == RuleSetTypeEnum.USOILFIELD)
			{
				_lblHaulingExplosives.setVisibility(View.VISIBLE);
				_tvHaulingExplosives.setVisibility(View.VISIBLE);
				
				if (empLog.getIsHaulingExplosives())
					_tvHaulingExplosives.setText("Yes");					
				else
					_tvHaulingExplosives.setText("No");
			}
			else
			{
				_lblHaulingExplosives.setVisibility(View.GONE);
				_tvHaulingExplosives.setVisibility(View.GONE);
			}
		}
		else
		{
			_lblHaulingExplosives.setVisibility(View.GONE);
			_tvHaulingExplosives.setVisibility(View.GONE);
		}

		// ELD Mandate Driver License and State

		if(GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled())
		{
			_lblDriverLicenseNumber.setVisibility(View.VISIBLE);
			_tvDriverLicenseNumber.setVisibility(View.VISIBLE);
			_lblState.setVisibility(View.VISIBLE);
			_tvState.setVisibility(View.VISIBLE);

			_tvDriverLicenseNumber.setText(logincredentials.getDriverLicenseNumber());
			_tvState.setText(logincredentials.getDriverLicenseState());
		}
		else{
			_lblDriverLicenseNumber.setVisibility(View.GONE);
			_tvDriverLicenseNumber.setVisibility(View.GONE);
			_lblState.setVisibility(View.GONE);
			_tvState.setVisibility(View.GONE);
		}

        if  (!controller.logHasCanadianRulesets(empLog)  ) {
            _tvVehiclePlate.setVisibility(View.GONE);
            _tvTrailerPlate.setVisibility(View.GONE);
            _lblVehiclePlate.setVisibility(View.GONE);
            _lblTrailerPlate.setVisibility(View.GONE);
        }
	}
	
}
