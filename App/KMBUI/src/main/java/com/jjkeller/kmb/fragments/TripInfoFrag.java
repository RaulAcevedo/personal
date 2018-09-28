package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import com.jjkeller.kmb.TripInfo;
import com.jjkeller.kmb.interfaces.ITripInfo.TripInfoFragActions;
import com.jjkeller.kmb.interfaces.ITripInfo.TripInfoFragControllerMethods;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.EOBR.EobrReader;
import com.jjkeller.kmbapi.controller.abstracts.APIControllerBase;
import com.jjkeller.kmbapi.controller.interfaces.IAPIController;
import com.jjkeller.kmbapi.controller.share.MandateObjectFactory;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.controller.utility.EmployeeLogUtilities;
import com.jjkeller.kmbapi.enums.CanadaDeferralTypeEnum;
import com.jjkeller.kmbapi.enums.RuleSetTypeEnum;
import com.jjkeller.kmbapi.proxydata.EldEventAdapterList;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;
import com.jjkeller.kmbui.R;

import java.util.ArrayList;
import java.util.Date;

public class TripInfoFrag extends BaseFragment {
	TripInfoFragControllerMethods controlListener;
	TripInfoFragActions actionsListener;
	
	private TextView _txtTrailer;
	private TextView _txtTrailerPlate;
	private TextView _txtShipmentInfo;
	private TextView _txtVehiclePlate;
    private TextView _txtTractorNumber;
    private TextView _lblTractorNumber;
    private TextView _lblTrailerPlate;
    private TextView _lblVehiclePlate;
	private TextView _lblMotionPictureProduction;
	private Spinner _cboMotionPictureProduction;
	private TextView _lblMotionPictureAuthority;
	private TextView _txtMotionPictureAuthority;
	private TextView _lblDeferral;
	private Spinner _cboDeferral;
	private Button _btnLoginOK;	
	private CheckBox _chkReturnToWorkLocation;
	private ArrayAdapter<CharSequence> cdDeferralAdapter;
	private CanadaDeferralTypeEnum canadaDeferralTypeEnum;
	private Bundle returnVals;	
	private CheckBox _chkIsHaulingExplosives;
	private CheckBox _chkIs30MinRestBreakExempt;
	private CheckBox _chkIsWeeklyResetUsed;
	private CheckBox _chkIsOperateSpecificVehicleForOilField;
	private CheckBox _chkSetDefaultTrailerNumber;
	private CheckBox _chkSetDefaultShipmentNumber; 
	private TextView _lblTitle;
	private TextView _lblTripInformationDriverTwo;

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
        // Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.f_tripinfo, container, false);
		findControls(v);
		return v;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) 
	{
		super.onActivityCreated(savedInstanceState);
		this.loadControls(savedInstanceState);
		loadDefaultTripInformation();
	}
	
	protected void findControls(View v)
	{
		_txtTrailer = (TextView)v.findViewById(R.id.txtTrailer);
		_txtTrailerPlate = (TextView)v.findViewById(R.id.txtTrailerPlate);
		_txtShipmentInfo = (TextView)v.findViewById(R.id.txtShipmentInfo);
        _txtVehiclePlate = (TextView)v.findViewById(R.id.txtVehiclePlate);
        _txtTractorNumber = (TextView)v.findViewById(R.id.txtTractorNumber);
        _lblTractorNumber = (TextView)v.findViewById(R.id.lblTractorNumber);
        _lblTrailerPlate = (TextView) v.findViewById(R.id.lblTrailerPlate);
        _lblVehiclePlate = (TextView) v.findViewById(R.id.lblVehiclePlate);
		_lblMotionPictureProduction = (TextView) v.findViewById(R.id.lblMotionPictureProduction);
		_cboMotionPictureProduction = (Spinner)v.findViewById(R.id.cboMotionPictureProduction);
		_lblMotionPictureAuthority = (TextView) v.findViewById(R.id.lblMotionPictureAuthority);
		_txtMotionPictureAuthority = (TextView) v.findViewById(R.id.txtMotionPictureAuthority);
		_lblDeferral = (TextView)v.findViewById(R.id.lblDeferral);
		_btnLoginOK = (Button)v.findViewById(R.id.btnloginok);
		_cboDeferral = (Spinner)v.findViewById(R.id.cboDeferral);
		_chkReturnToWorkLocation = (CheckBox)v.findViewById(R.id.chkReturnToWorkLocation);
		_chkIsHaulingExplosives = (CheckBox)v.findViewById(R.id.chkIsHaulingExplosives);
		_chkIs30MinRestBreakExempt = (CheckBox)v.findViewById(R.id.chkIs30MinRestBreakExempt);
		_chkIsWeeklyResetUsed = (CheckBox)v.findViewById(R.id.chkIsWeeklyResetUsed);
		_chkIsOperateSpecificVehicleForOilField = (CheckBox)v.findViewById(R.id.chkIsOperatesSpecificVehicleForOilField);
		_chkSetDefaultTrailerNumber = (CheckBox)v.findViewById(R.id.chkSetDefaultTrailerNumber);
		_chkSetDefaultShipmentNumber = (CheckBox)v.findViewById(R.id.chkSetDefaultShipmentNumber);
		_lblTitle = (TextView)v.findViewById(R.id.lblTitle);
		_lblTripInformationDriverTwo = (TextView)v.findViewById(R.id.lbltripinformationdrivertwo);
		
		_btnLoginOK.setOnClickListener(
				new OnClickListener() {					
					public void onClick(View v) {
						actionsListener.handleOKButtonClick();
					}
				});
		
		_chkSetDefaultTrailerNumber.setOnClickListener(
				new OnClickListener() {
					public void onClick(View v) {
						actionsListener.handleSetTrailerInformationClick();

					}
				}); 
		
		_chkSetDefaultShipmentNumber.setOnClickListener(
				new OnClickListener() {
					public void onClick(View v) {
						actionsListener.handleSetShipmentInformationClick();
						
					}
				});

		_cboMotionPictureProduction.setOnItemSelectedListener(
				new AdapterView.OnItemSelectedListener() {
					public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
						actionsListener.handleMotionPictureProductionSelect();
					}

					public void onNothingSelected(AdapterView<?> arg0) {
					}
				});
	}
	
	protected void loadDefaultTripInformation() 
	{
		if (GlobalState.getInstance().getFeatureService().getDefaultTripInformation())
		{
			// Display Default Trip Information CheckBoxes 
			_chkSetDefaultTrailerNumber.setVisibility(View.VISIBLE);
			_chkSetDefaultShipmentNumber.setVisibility(View.VISIBLE);
						
			Context ctx = GlobalState.getInstance().getApplicationContext();
			SharedPreferences userPref = ctx.getSharedPreferences(this.getString(R.string.sharedpreferencefile), 0);
			
			String defaultTrailerNumber = userPref.getString(getString(R.string.defaulttrailernumber), "");
			String defaultTrailerPlate = userPref.getString(getString(R.string.defaulttrailerplate), "");
			String defaultShipmentNumber = userPref.getString(getString(R.string.defaultshipmentnumber), "");
						
			getDefaultShipmentNumber().setChecked(userPref.getBoolean(getString(R.string.setdefaultshipmentnumber), false)); 
			getDefaultTrailerInfo().setChecked(userPref.getBoolean(getString(R.string.setdefaulttrailernumber), false));

			if(_chkSetDefaultTrailerNumber.isChecked() || _chkSetDefaultShipmentNumber.isChecked())
			{
				// Trailer Number
				if (_chkSetDefaultTrailerNumber.isChecked() && defaultTrailerNumber.length() > 0 && _txtTrailer.getText().length() == 0)
				{
					_txtTrailer.setEnabled(false);
					_txtTrailer.setText(defaultTrailerNumber);
				}
				else if (_chkSetDefaultTrailerNumber.isChecked() && defaultTrailerNumber.length() == 0 && _txtTrailer.getText().length() == 0)
				{
					_txtTrailer.setEnabled(true);
					_chkSetDefaultTrailerNumber.setChecked(false);
				}
				else if (_chkSetDefaultTrailerNumber.isChecked() && !defaultTrailerNumber.equals(_txtTrailer.getText().toString()))
				{
					SharedPreferences.Editor editor = userPref.edit(); 
					editor.putString(getString(R.string.defaulttrailernumber),_txtTrailer.getText().toString());	
					editor.commit();
				}

				// Trailer Plate
				if (_chkSetDefaultTrailerNumber.isChecked() && defaultTrailerPlate.length() > 0 && _txtTrailerPlate.getText().length() == 0)
				{
					_txtTrailerPlate.setEnabled(false);
					_txtTrailerPlate.setText(defaultTrailerPlate);
				}
				else if (_chkSetDefaultTrailerNumber.isChecked() && defaultTrailerPlate.length() == 0 && _txtTrailerPlate.getText().length() == 0)
				{
					_txtTrailerPlate.setEnabled(true);
					_chkSetDefaultTrailerNumber.setChecked(false);
				}
				else if (_chkSetDefaultTrailerNumber.isChecked() && !defaultTrailerPlate.equals(_txtTrailerPlate.getText().toString()))
				{
					SharedPreferences.Editor editor = userPref.edit();
					editor.putString(getString(R.string.defaulttrailerplate),_txtTrailerPlate.getText().toString());
					editor.commit();
				}

				if (_chkSetDefaultShipmentNumber.isChecked() && defaultShipmentNumber.length() > 0 && _txtShipmentInfo.getText().length() == 0)
				{
					_txtShipmentInfo.setEnabled(false);
					_txtShipmentInfo.setText(defaultShipmentNumber);
				}
				else if (_chkSetDefaultShipmentNumber.isChecked() && defaultShipmentNumber.length() == 0 && _txtShipmentInfo.getText().length() == 0)
				{
					_txtShipmentInfo.setEnabled(true);
					_chkSetDefaultShipmentNumber.setChecked(false); 
				}
				else if (_chkSetDefaultShipmentNumber.isChecked() && !defaultShipmentNumber.equals(_txtShipmentInfo.getText().toString()))
				{
					SharedPreferences.Editor editor = userPref.edit(); 
					editor.putString(getString(R.string.defaultshipmentnumber), _txtShipmentInfo.getText().toString());
					editor.commit();
				}
				
			}
			// Enable/Disable Trailer and shipment TextBoxes
			_txtTrailer.setEnabled(!_chkSetDefaultTrailerNumber.isChecked());
			_txtTrailerPlate.setEnabled(!_chkSetDefaultTrailerNumber.isChecked());
			_txtShipmentInfo.setEnabled(!_chkSetDefaultShipmentNumber.isChecked());
		}
		else
		{

			// Hide Default Trip Information CheckBoxes			
			_chkSetDefaultTrailerNumber.setVisibility(View.GONE);
			_chkSetDefaultShipmentNumber.setVisibility(View.GONE);
		}

	}
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
        	actionsListener = (TripInfoFragActions) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement TripInfoFragActions");
        }
        
        try{
        	controlListener = (TripInfoFragControllerMethods) activity;
        } catch (ClassCastException e) {
        	throw new ClassCastException(activity.toString() + " must implement TripInfoPeriodsFragControllerMethods");
        }
    }
	
	protected void loadControls(Bundle savedInstanceState)
	{
        if (!GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
            _txtTractorNumber.setVisibility(View.GONE);
            _lblTractorNumber.setVisibility(View.GONE);
			_txtTrailer.setHint("");
			_txtShipmentInfo.setHint("");
        }

		if (GlobalState.getInstance().getIsMultipleUsersLogin() || GlobalState.getInstance().getTeamDriverMode() == GlobalState.TeamDriverModeEnum.SHAREDDEVICE)
			_lblTitle.setText(GlobalState.getInstance().getCurrentUser().getCredentials().getEmployeeFullName() + " - Trip Information");
		
		RuleSetTypeEnum currentUserRuleset = ((APIControllerBase)controlListener.getMyController()).getCurrentUser().getRulesetTypeEnum();

        cdDeferralAdapter = ArrayAdapter.createFromResource(getActivity(), CanadaDeferralTypeEnum.ARRAYID, R.layout.kmb_spinner_item);
        cdDeferralAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        
        if (GlobalState.getInstance().getLoggedInUserList().size() > 1) {
			// show the *Change Trip Information if different than Driver #1 lbl
			_lblTripInformationDriverTwo.setVisibility(View.VISIBLE);
			// populate data into controls
			IAPIController emp_log = MandateObjectFactory.getInstance(this.getActivity(),GlobalState.getInstance().getFeatureService()).getCurrentEventController();
			User user = GlobalState.getInstance().getCurrentUser();
			if(this.getActivity().getIntent().getBooleanExtra(getString(R.string.extra_isloginprocess), false)){
				ArrayList<User> users = GlobalState.getInstance().getLoggedInUserList();
				user = users.get(0);
			}
			Date currentHomeTerminalTime = DateUtility.CurrentHomeTerminalTime(user);
			Date todaysLogDate = EmployeeLogUtilities.CalculateLogStartTime(this.getActivity(), currentHomeTerminalTime, user.getHomeTerminalTimeZone());
			EmployeeLog localLogForToday = emp_log.GetLocalEmployeeLog(user, todaysLogDate);
			
			returnVals = controlListener.getMyController().GetTripInfo(localLogForToday);
		}
		else
		{
			_lblTripInformationDriverTwo.setVisibility(View.GONE);

			returnVals = controlListener.getMyController().GetTripInfo();
		}
			        
		canadaDeferralTypeEnum = new CanadaDeferralTypeEnum(returnVals.getInt(this.getString(R.string.canadadeferraltype)));
        
        // If current ruleset is Canadian, show the Off Duty Deferral dropdown.
		if(currentUserRuleset.isCanadianRuleset() )
		{
			ArrayList<User> userList = GlobalState.getInstance().getLoggedInUserList();
			User user = userList.get(0);
			if(user.AreBothInternationalRulesetsAvailable(this.getActivity())){
				if(user.getIsExemptFrom30MinBreakRequirement()) {
					returnVals.putBoolean(getString(R.string.state_isExemptFrom30MinBreakRequirement), user.getIsExemptFrom30MinBreakRequirement());
					_chkIs30MinRestBreakExempt.setVisibility(View.VISIBLE);
				}
				else
					_chkIs30MinRestBreakExempt.setVisibility(View.GONE);
			}else{
				_chkIs30MinRestBreakExempt.setVisibility(View.GONE);
			}

			_chkReturnToWorkLocation.setVisibility(View.GONE);
			_chkIsHaulingExplosives.setVisibility(View.GONE);
			_chkIsOperateSpecificVehicleForOilField.setVisibility(View.GONE);
			
			_lblDeferral.setVisibility(View.VISIBLE);
			_cboDeferral.setVisibility(View.VISIBLE);
			
	        _cboDeferral.setAdapter(cdDeferralAdapter);
	        _cboDeferral.setSelection(cdDeferralAdapter.getPosition(canadaDeferralTypeEnum.getString(getActivity())));
		}
		else if (currentUserRuleset.getValue() == RuleSetTypeEnum.TEXASOILFIELD)
		{
			_chkReturnToWorkLocation.setVisibility(View.GONE);
			_chkIsWeeklyResetUsed.setVisibility(View.GONE);
			_chkIsHaulingExplosives.setVisibility(View.GONE);

	        if (returnVals.containsKey(this.getActivity().getString(R.string.state_isoperatesspecificvehiclesforoilfield)))
	        	_chkIsOperateSpecificVehicleForOilField.setVisibility(View.VISIBLE);
	        else
	        	_chkIsOperateSpecificVehicleForOilField.setVisibility(View.GONE);
			
			_lblDeferral.setVisibility(View.GONE);
			_cboDeferral.setVisibility(View.GONE);

			_chkIs30MinRestBreakExempt.setVisibility(View.GONE);
		}
		else
		{	       
			// Check for an extra passed from the Login screen
			// If present, hide the extra TripInfo controls
	        if( getActivity().getIntent().hasExtra(this.getResources().getString(R.string.extra_tripinfomsg))
	        		|| getActivity().getIntent().hasExtra(this.getResources().getString(R.string.extra_teamdriverlogin)))
	        	_chkReturnToWorkLocation.setVisibility(View.GONE);	        
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
	        	_chkIsOperateSpecificVehicleForOilField.setVisibility(View.VISIBLE);
	        else
	        	_chkIsOperateSpecificVehicleForOilField.setVisibility(View.GONE);

			if(currentUserRuleset.is30MinuteBreakExemptValid()  ){
				if(returnVals.containsKey(this.getActivity().getString(R.string.state_isExemptFrom30MinBreakRequirement))){
					ArrayList<User> userList = GlobalState.getInstance().getLoggedInUserList();
					User user = userList.get(0);
					returnVals.putBoolean(getString(R.string.state_isExemptFrom30MinBreakRequirement), user.getIsExemptFrom30MinBreakRequirement());
					boolean isExempt = returnVals.getBoolean(getString(R.string.state_isExemptFrom30MinBreakRequirement), true);
					if(isExempt)
						_chkIs30MinRestBreakExempt.setVisibility(View.VISIBLE);
					else
						_chkIs30MinRestBreakExempt.setVisibility(View.GONE);
				}
			}
			else
				_chkIs30MinRestBreakExempt.setVisibility(View.GONE);

	        _cboDeferral.setVisibility(View.GONE);
			_lblDeferral.setVisibility(View.GONE);


		}

		// 10/3/11 JHM - Only load from state if one of the expected values is contained in bundle.
		if(savedInstanceState != null &&
			(savedInstanceState.containsKey(getResources().getString(R.string.state_trailer)) ||
					savedInstanceState.containsKey(getResources().getString(R.string.state_trailerplate)) ||
					savedInstanceState.containsKey(getResources().getString(R.string.state_shipment)) ||
                    savedInstanceState.containsKey(getResources().getString(R.string.state_tractornumber)) ||
					savedInstanceState.containsKey(getResources().getString(R.string.state_vehicleplate)) ||
					savedInstanceState.containsKey(getResources().getString(R.string.state_returntoworklocation)) ||
					savedInstanceState.containsKey(getResources().getString(R.string.state_ishaulingexplosives)) ||
					savedInstanceState.containsKey(getResources().getString(R.string.state_isExemptFrom30MinBreakRequirement)) ||
					savedInstanceState.containsKey(getResources().getString(R.string.state_isoperatesspecificvehiclesforoilfield))))
		{
			_txtTrailer.setText(savedInstanceState.getCharSequence(getResources().getString(R.string.state_trailer)));
			_txtTrailerPlate.setText(savedInstanceState.getCharSequence(getResources().getString(R.string.state_trailerplate)));
			_txtShipmentInfo.setText(savedInstanceState.getCharSequence(getResources().getString(R.string.state_shipment)));
            _txtTractorNumber.setText(savedInstanceState.getCharSequence(getResources().getString(R.string.state_tractornumber)));
			_txtVehiclePlate.setText(savedInstanceState.getCharSequence(getResources().getString(R.string.state_vehicleplate)));
			_chkReturnToWorkLocation.setChecked(savedInstanceState.getBoolean(getResources().getString(R.string.state_returntoworklocation)));
			_chkIsHaulingExplosives.setChecked(savedInstanceState.getBoolean(getResources().getString(R.string.state_ishaulingexplosives)));
			_chkIs30MinRestBreakExempt.setChecked(savedInstanceState.getBoolean(getResources().getString(R.string.state_isExemptFrom30MinBreakRequirement)));
			if(savedInstanceState.containsKey(this.getActivity().getString(R.string.isweeklyresetused))){
				_chkIsWeeklyResetUsed.setChecked(savedInstanceState.getBoolean(getActivity().getString(R.string.isweeklyresetused)));
			}
			_chkIsOperateSpecificVehicleForOilField.setChecked(savedInstanceState.getBoolean(getResources().getString(R.string.state_isoperatesspecificvehiclesforoilfield)));
		}
		else{
			_txtTrailer.setText(returnVals.getString(getActivity().getString(R.string.trailernumbers)));
			_txtTrailerPlate.setText(returnVals.getString(getActivity().getString(R.string.trailerplate)));
			_txtShipmentInfo.setText(returnVals.getString(getActivity().getString(R.string.shipmentinfo)));
            _txtTractorNumber.setText(returnVals.getString(getActivity().getString(R.string.tractornumber)));
			_txtVehiclePlate.setText(returnVals.getString(getActivity().getString(R.string.vehicleplate)));
			_chkReturnToWorkLocation.setChecked(returnVals.getBoolean(getActivity().getString(R.string.returntoworklocation)));
			_chkIsHaulingExplosives.setChecked(returnVals.getBoolean(getActivity().getString(R.string.ishaulingexplosives)));
			_chkIs30MinRestBreakExempt.setChecked(returnVals.getBoolean(getResources().getString(R.string.state_isExemptFrom30MinBreakRequirement)));
			if(returnVals.containsKey(this.getActivity().getString(R.string.isweeklyresetused))){
				_chkIsWeeklyResetUsed.setChecked(returnVals.getBoolean(getActivity().getString(R.string.isweeklyresetused)));
			}
			_chkIsOperateSpecificVehicleForOilField.setChecked(returnVals.getBoolean(getActivity().getString(R.string.state_isoperatesspecificvehiclesforoilfield)));
		}

		// Eobr Activate fetched the Unit currently associated to this Eobr and stored it's License Plate Number
		if (_txtVehiclePlate.getText().length() == 0) {
			if (getActivity().getClass() == TripInfo.class) {  // only default on a new Trip not Edit (from ViewTrip)
				_txtVehiclePlate.setText(EobrReader.getInstance().getUnitLicensePlateNumber());
			}
		}

		Boolean isMandate = GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled();
		Boolean isLogin = this.getActivity().getIntent().getBooleanExtra(getString(R.string.extra_isloginprocess), false);
        if (_txtTractorNumber.getText().length() == 0 || (isMandate && isLogin)) {
            if (getActivity().getClass() == TripInfo.class) {
                _txtTractorNumber.setText(EobrReader.getInstance().getEobrIdentifier());
            }
        }

        Date currentHomeTerminalTime = DateUtility.getCurrentHomeTerminalTime(GlobalState.getInstance().getCurrentUser());
		Date todaysLogDate = EmployeeLogUtilities.CalculateLogStartTime(this.getActivity(), currentHomeTerminalTime, GlobalState.getInstance().getCurrentUser().getHomeTerminalTimeZone());
		EmployeeLog employeeLog = controlListener.getMyController().GetEmployeeLog(todaysLogDate);
        if(!controlListener.getMyController().logHasCanadianRulesets(employeeLog)){
            _lblTrailerPlate.setVisibility(View.GONE);
            _lblVehiclePlate.setVisibility(View.GONE);
            _txtTrailerPlate.setVisibility(View.GONE);
            _txtVehiclePlate.setVisibility(View.GONE);
        }

		if (GlobalState.getInstance().getCompanyConfigSettings(getActivity().getBaseContext()).getIsMotionPictureEnabled()) {
			actionsListener.loadMotionPicture();
			_cboMotionPictureProduction.setEnabled(true);
			_lblMotionPictureProduction.setVisibility(View.VISIBLE);
			_cboMotionPictureProduction.setVisibility(View.VISIBLE);
			_lblMotionPictureAuthority.setVisibility(View.VISIBLE);
			_txtMotionPictureAuthority.setVisibility(View.VISIBLE);
		}

		/* if this is at LOGIN, make sure it's getting the TRIP INFO from the EmployeeLogEldEvent, not from EmployeeLog.
		   after submit/certify, TRIP INFO will be concatenated and keep growing with each submit.
		   we just need the last saved values for TRIP INFO (shipment, trailer number) */
		if(isLogin && isMandate) {
			EmployeeLogEldEvent lastEvent;
			EmployeeLogEldEvent[] logEventList = employeeLog.getEldEventList().getActiveEldEventList(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus);
			if (logEventList != null && logEventList.length > 2) {
				// get second to last DutyStatus event, since last DutyStatus event is from login and contains the EmployeeLog values for TRIP INFO
				int lastIndex = logEventList.length - 2;
				lastEvent = logEventList[lastIndex];
				_txtShipmentInfo.setText(lastEvent.getShipmentInfo());
				_txtTrailer.setText(lastEvent.getTrailerNumber());
			}
		}
		//AOBRD we get these from the EmployeeLog not the EmployeLogEldEvent table
		else if(isLogin) {
			_txtTrailer.setText(employeeLog.getTrailerNumbers());
			_txtShipmentInfo.setText((employeeLog.getShipmentInformation()));
		}

		if(EobrReader.getInstance().getCurrentConnectionState() == EobrReader.ConnectionState.ONLINE) {
			_txtTractorNumber.setHint("Required");
		}
		else {
			_txtTractorNumber.setHint("Optional");
		}

	}

	public TextView getTrailerTextView(){
		if(_txtTrailer == null)
			_txtTrailer = (TextView)getView().findViewById(R.id.txtTrailer);
		return _txtTrailer;		
	}

	public TextView getTrailerPlateTextView(){
		if(_txtTrailerPlate == null)
			_txtTrailerPlate = (TextView)getView().findViewById(R.id.txtTrailerPlate);
		return _txtTrailerPlate;
	}

	public TextView getShipmentInfoTextView(){
		if(_txtShipmentInfo == null)
			_txtShipmentInfo = (TextView)getView().findViewById(R.id.txtShipmentInfo);
		return _txtShipmentInfo;		
    }

    public TextView getTractorNumberTextView(){
        if (_txtTractorNumber==null)
            _txtTractorNumber= (TextView) getView().findViewById(R.id.txtTractorNumber);
        return _txtTractorNumber;
    }

	public TextView getVehiclePlateTextView(){
		if(_txtVehiclePlate == null)
			_txtVehiclePlate = (TextView)getView().findViewById(R.id.txtVehiclePlate);
		return _txtVehiclePlate;
	}

	public CheckBox getReturnToWorkLocationCheckbox(){
		if(_chkReturnToWorkLocation == null)
			_chkReturnToWorkLocation = (CheckBox)getView().findViewById(R.id.chkReturnToWorkLocation);
		return _chkReturnToWorkLocation;		
	}
	
	public CheckBox getIsHaulingExplosivesCheckbox(){
		if(_chkIsHaulingExplosives == null)
			_chkIsHaulingExplosives = (CheckBox)getView().findViewById(R.id.chkIsHaulingExplosives);
		return _chkIsHaulingExplosives;
	}

	public CheckBox getIs30MinRestBreakExemptCheckbox(){
		if(_chkIs30MinRestBreakExempt == null)
			_chkIs30MinRestBreakExempt = (CheckBox)getView().findViewById(R.id.chkIs30MinRestBreakExempt);
		return _chkIs30MinRestBreakExempt;
	}
	
	public CheckBox getIsWeeklyResetUsedCheckbox(){
		if(_chkIsWeeklyResetUsed == null)
			_chkIsWeeklyResetUsed = (CheckBox)getView().findViewById(R.id.chkIsWeeklyResetUsed);
		return _chkIsWeeklyResetUsed;		
	}
	
	public Spinner getDeferralSpinner(){
		if(_cboDeferral == null)
			_cboDeferral = (Spinner)getView().findViewById(R.id.cboDeferral);
		return _cboDeferral;
	}

	public Spinner getMotionPictureProductionSpinner() {
		if(_cboMotionPictureProduction == null)
			_cboMotionPictureProduction = (Spinner)getView().findViewById(R.id.cboMotionPictureProduction);
		return _cboMotionPictureProduction;
	}

	public TextView getMotionPictureAuthorityTextView() {
		if(_txtMotionPictureAuthority == null)
			_txtMotionPictureAuthority = (TextView)getView().findViewById(R.id.txtMotionPictureAuthority);
		return _txtMotionPictureAuthority;
	}
	
	public ArrayAdapter<CharSequence> getDeferralAdapter(){
		return cdDeferralAdapter;			
	}
	
	public CheckBox getIsOperatesSpecificVehiclesForOilField(){
		if (_chkIsOperateSpecificVehicleForOilField == null)
			_chkIsOperateSpecificVehicleForOilField = (CheckBox)getView().findViewById(R.id.chkIsOperatesSpecificVehicleForOilField);
		return _chkIsOperateSpecificVehicleForOilField;
	}
	
	public CheckBox getDefaultTrailerInfo(){
		if (_chkSetDefaultTrailerNumber == null)
			_chkSetDefaultTrailerNumber = (CheckBox)getView().findViewById(R.id.chkSetDefaultTrailerNumber);
		return _chkSetDefaultTrailerNumber;
	}
	
	public CheckBox getDefaultShipmentNumber(){ 
		if (_chkSetDefaultShipmentNumber == null)
			_chkSetDefaultShipmentNumber = (CheckBox)getView().findViewById(R.id.chkSetDefaultShipmentNumber);
		return _chkSetDefaultShipmentNumber; 
	}

}
