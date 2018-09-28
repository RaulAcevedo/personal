package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.jjkeller.kmb.interfaces.IAdmin.AdminFragActions;
import com.jjkeller.kmb.interfaces.IAdmin.AdminFragControllerMethods;
import com.jjkeller.kmb.interfaces.IEobrConfig;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.EOBR.EobrReader;
import com.jjkeller.kmbapi.controller.GeotabController;
import com.jjkeller.kmbui.R;

public class AdminFrag extends BaseFragment{
	AdminFragActions actionsListener;
	AdminFragControllerMethods controlListener;
	
	private Button _btnDashboard;
	private Button _btnDeletDB;
	private Button _btnResetHistory;
	private Button _btnResetDataUsage;
	private Button _btnClearHomeSetting;
	private Button _btnResetEobr;
	private Button _btnPowerCycleReset;
	private Button _btnMalfunctionsAndDataDiagnostics;
	private Button _btnAdminDataTransferStatus;
	private Button _btnAdminStorageFiller;
	private Button _btnAdminClearActiveDeviceCrc;
	private Button _btnAdminSaveDuplicateDutyStatus;

	private CheckBox _chkEldReadVin;
	private CheckBox _chkForceGeotabInvalidGpsLatch;
	private CheckBox _chkForceGeotabOdoError;
	private CheckBox _chkForceGeotabVssError;
	private CheckBox _chkForceInvalidGPSDate;
	private boolean _hasLoadedControls = false;
	private IEobrConfig.EobrConfigFragControllerMethods _controlListener;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        // Inflate the layout for this fragment

		View v = inflater.inflate(R.layout.f_admin, container, false);
		findControls(v);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		this.loadControls();
	}

	private void loadControls() {
		if (GlobalState.getInstance().getFeatureService().getShowDebugFunctions()) {
			_btnDashboard.setVisibility(View.VISIBLE);
			_btnMalfunctionsAndDataDiagnostics.setVisibility(View.VISIBLE);
			_btnAdminStorageFiller.setVisibility(View.VISIBLE);
			_btnAdminClearActiveDeviceCrc.setVisibility(View.VISIBLE);
			_btnAdminSaveDuplicateDutyStatus.setVisibility(View.VISIBLE);
		} else {
			_btnDashboard.setVisibility(View.GONE);
			_btnMalfunctionsAndDataDiagnostics.setVisibility(View.GONE);
			_btnAdminStorageFiller.setVisibility(View.GONE);
			_btnAdminClearActiveDeviceCrc.setVisibility(View.GONE);
			_btnAdminSaveDuplicateDutyStatus.setVisibility(View.GONE);
		}

		_btnClearHomeSetting.setVisibility(View.GONE);

		//Set the ELD Read Vin toggle.
		//Hide ELD Read VIN for GeoTab Users
		EobrReader eobr = EobrReader.getInstance();
		GeotabController geotabController = new GeotabController(this.getActivity());
		boolean isGeotabDevice =  GlobalState.getInstance().getCompanyConfigSettings(this.getActivity()).getIsGeotabEnabled() && geotabController.IsCurrentDeviceGeotab() ;

		if (eobr.isEobrGen1() || isGeotabDevice )
		{
			_chkEldReadVin.setVisibility(View.GONE);
			_chkForceInvalidGPSDate.setVisibility(View.GONE);
		}
		else
		{
			if(EobrReader.getIsEobrDevicePhysicallyConnected())
			{
				_chkEldReadVin.setChecked(eobr.GetDisableReadEldVin());
			}
			else
				_chkEldReadVin.setChecked(false);
		}

		//Hide EOBR Power Cycle Reset for GeoTab Users
		if(isGeotabDevice) {
			_btnPowerCycleReset.setVisibility(View.GONE);
		}else{
			_chkForceGeotabOdoError.setVisibility(View.GONE);
			_chkForceGeotabInvalidGpsLatch.setVisibility(View.GONE);
			_chkForceGeotabVssError.setVisibility(View.GONE);
		}

		_chkForceGeotabInvalidGpsLatch.setChecked(GlobalState.getInstance().getForceGeotabInvalidGPS());
		_chkForceGeotabOdoError.setChecked(GlobalState.getInstance().getForceGeotabInvalidOdo());
		_chkForceGeotabVssError.setChecked(GlobalState.getInstance().getForceGeotabInvalidVss());
		_chkForceInvalidGPSDate.setChecked(GlobalState.getInstance().getForceInvalidGPSDate());
		_hasLoadedControls = true;

	}
	
	protected void findControls(View v){	
		_btnResetHistory = (Button)v.findViewById(R.id.btnResetHistory);
		_btnResetHistory.setOnClickListener(
			new OnClickListener(){
				public void onClick(View v){
					actionsListener.handleResetHistory();
				}
						
			});
				
		_btnDashboard = (Button)v.findViewById(R.id.btnDashboard);
		_btnDashboard.setOnClickListener(
			new OnClickListener(){
				public void onClick(View v){
					actionsListener.handleDashboard();
				}
					
			});
				
		_btnDeletDB = (Button)v.findViewById(R.id.btnDeletDB);
		_btnDeletDB.setOnClickListener(
			new OnClickListener(){
				public void onClick(View v){
					actionsListener.handleDeleteDB();
				}
				
			});
		
		_btnResetDataUsage = (Button)v.findViewById(R.id.btnResetDataUsage);		
		_btnResetDataUsage.setOnClickListener(
			new OnClickListener(){
				public void onClick(View v){
					actionsListener.handleResetDataUsage();
				}
			});
		
		_btnClearHomeSetting = (Button)v.findViewById(R.id.btnClearHomeSetting);		
		_btnClearHomeSetting.setOnClickListener(
			new OnClickListener(){
				public void onClick(View v){
					actionsListener.handleClearHomeSetting();
				}
			});
		
		_btnResetEobr = (Button)v.findViewById(R.id.btnResetEobr);
		_btnResetEobr.setOnClickListener(
				new OnClickListener(){			
					public void onClick(View v){
						actionsListener.handleResetEobrButtonClick();
					}
				});
		
		_btnPowerCycleReset = (Button)v.findViewById(R.id.btnPowerCycleReset);
		_btnPowerCycleReset.setOnClickListener(
				new OnClickListener(){			
					public void onClick(View v){
						actionsListener.handlePowerCycleReset();
					}
				});

		_btnMalfunctionsAndDataDiagnostics = (Button) v.findViewById(R.id.btnAdminMalfAndDataDiag);
		_btnMalfunctionsAndDataDiagnostics.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				actionsListener.handleAdminMalfunctionAndDataDiagnosticClick();
			}
		});

		_btnAdminDataTransferStatus = (Button) v.findViewById(R.id.btnAdminDataTransferStatus);
		_btnAdminDataTransferStatus.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				actionsListener.handleAdminDataTransferStatusClick();
			}
		});

		_btnAdminStorageFiller = (Button) v.findViewById(R.id.btnAdminStorageFiller);
		_btnAdminStorageFiller.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				actionsListener.handleAdminStorageFillerClick();
			}
		});

		_btnAdminClearActiveDeviceCrc = (Button) v.findViewById(R.id.btnAdminClearActiveDeviceCrc);
		_btnAdminClearActiveDeviceCrc.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				actionsListener.handleAdminClearActiveDeviceCrcClick();
			}
		});

		_btnAdminSaveDuplicateDutyStatus = (Button) v.findViewById(R.id.btnAdminSaveDuplicateDutyStatus);
		_btnAdminSaveDuplicateDutyStatus.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				actionsListener.handleAdminSaveDuplicateDutyStatusClick();
			}
		});

		_chkEldReadVin = (CheckBox) v.findViewById(R.id.chkEldReadVin);
		_chkEldReadVin.setOnCheckedChangeListener(
				new CompoundButton.OnCheckedChangeListener() {
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						actionsListener.handleDisableReadEldVin(isChecked, _hasLoadedControls);
		    }
		});

		_chkForceGeotabInvalidGpsLatch = (CheckBox) v.findViewById(R.id.chkForceGeotabInvalidGpsLatch);
		_chkForceGeotabInvalidGpsLatch.setOnCheckedChangeListener(
				new CompoundButton.OnCheckedChangeListener() {
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						actionsListener.handleForceGeotabInvalidGpsLatch(isChecked);
					}
				});

		_chkForceGeotabOdoError = (CheckBox) v.findViewById(R.id.chkForceGeotabOdoError);
		_chkForceGeotabOdoError.setOnCheckedChangeListener(
				new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						actionsListener.handleForceGeotabOdoError(isChecked);
					}
				}
		);

		_chkForceGeotabVssError = (CheckBox) v.findViewById(R.id.chkForceGeotabVssError);
		_chkForceGeotabVssError.setOnCheckedChangeListener(
				new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						actionsListener.handleForceGeotabVssError(isChecked);
					}
				}
		);

		_chkForceInvalidGPSDate = (CheckBox) v.findViewById(R.id.chkForceInvalidGPSDate);
		_chkForceInvalidGPSDate.setOnCheckedChangeListener(
				new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						actionsListener.handleForceInvalidGPSDate(isChecked);
					}
				}
		);


		if (!GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
			_btnResetHistory.setText(getString(R.string.btn_resetHistoryeobr));
			_btnResetEobr.setText(getString(R.string.clearentireeobrlabel));
			_btnPowerCycleReset.setText(getString(R.string.btn_powercyclereseteobr));
			_chkEldReadVin.setText(getString(R.string.lbl_EobrReadVin));
		}
	}
	
	@Override
    public void onAttach(Activity activity) {
		super.onAttach(activity);
        try {
        	actionsListener = (AdminFragActions) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement AdminFragActions");
        }
        
        try{
        	controlListener = (AdminFragControllerMethods) activity;
        } catch (ClassCastException e) {
        	throw new ClassCastException(activity.toString() + " must implement AdminFragControllerMethods");
        }
    }

}
